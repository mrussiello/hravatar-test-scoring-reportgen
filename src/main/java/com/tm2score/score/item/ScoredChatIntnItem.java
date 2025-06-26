/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.item;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.IframeItemType;
import com.tm2score.bot.BotEventStatusType;
import com.tm2score.bot.BotFacade;
import com.tm2score.bot.BotScoreUtils;
import com.tm2score.bot.ChatResponse;
import com.tm2score.entity.bot.BotInstance;
import com.tm2score.entity.event.BotEvent;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import com.tm2score.essay.LocalEssayScoringUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Mike
 */
public class ScoredChatIntnItem 
{

    String[] EMPATHY_PH_EN = new String[] { "I hear you", "that is terrible", "that is awful", "a problem", "can understand", "i understand", "feel bad", "sorry", "I can imagine", "how you feel", "make you feel", "makes you feel", "feel good", "feel better", "too bad", "so sorry", "I apologize", "very sorry", " happy", " sad" };
    String[] RAPPORT_PH_EN = new String[] { "see what I can do", "how are you", "how do you feel", "are you having a good day", "how is it going", "how's it going", "how goes", "weather", "thank you for your time", "happy to help", "I can help" };
    String[] NEGATIVE_PH_EN = new String[] { "shit", "fuck", "piss", "hell.", " hell ", "hell ", " jerk ", " dick ", " prick ", " damn", "screw you", "suck", "sucks", "stupid", "asshole", " ass ", "idiot", "moron", "imbecile" };
    
    Set<String> empathyPhrases = null;
    Set<String> rapportPhrases = null;
    Set<String> negativePhrases = null;
    
    SimJ.Intn intnObj;
    SimJ.Intn.Intnitem intnItemObj;
    
    TestEvent testEvent;
    
    long botEventId;
    BotEvent botEvent;
    BotInstance botInstance;
    List<BotInstance> allBotInstances;
    ChatResponse chatResponse;
    BotFacade botFacade;
    
    float userMessageCount;
    float wordCount;

    float overallScore; // 0-100
    
    float rapportEmpathyCount;  // raw count
    float rapportEmpathyScore; // 0-100
    
    float spellErrorCount;  // Total number of errors
    float grammarErrorCount;  // Total number of errors
    float spellErrorRate;  // rate of errors per 100 words
    float spellErrorScore; // 0-100
    
    float negativeExpressionCount;  // Raw cont. Weight is 0. Max score is 100-negatives*20.

    float avgRespTimeSecs;  // average seconds to respond
    float avgRespTimeScore; // 0 - 100
    
    /**
     * String = competency name
     * 
     * float[0] = points
     * float[1] = max points
     * float[2] = assigned points
     * float[3] = percent of max
     * float[4] = z-score
     * float[5] = scaled score
     * float[6] = weight
     * 
     */
    Map<String,float[]> competencyMap;
    
    /*
     Metascore 2 = Rapport Empathy Count (To be Avgd)
     Metascore 3 = Spelling Rate (To be Avg'd)
     Metascore 4 = Avg Resp Time Seconds (To be Avg'd)
     Metascore 5 = Negative Expression Count (To be Avg'd)
    */
    float[] metaScores;    
    
    boolean hasValidScore = false;
    
    

    public ScoredChatIntnItem( SimJ.Intn intn, SimJ.Intn.Intnitem ii, String respStr, TestEvent te)
    {
        intnObj = intn;
        intnItemObj = ii;
        testEvent=te;
        parseResponseStr( respStr );

        // LogService.logIt( "DataEntryItem() " + key + ", typed=" + typed + ", secs=" + secs );
    }
        
    private synchronized void init()
    {
        if( empathyPhrases!=null )
            return;
        
        empathyPhrases = new HashSet<>();
        rapportPhrases = new HashSet<>();
        negativePhrases = new HashSet<>();
        
        
        for( String s : EMPATHY_PH_EN )
            empathyPhrases.add( s );
            
        for( String s : RAPPORT_PH_EN )
            rapportPhrases.add( s );

        for( String s : NEGATIVE_PH_EN )
            negativePhrases.add( s );
    }
    
    /**
     * Format is iframeitemtypeid;boteventid
     * 
     * @param respStr 
     */
    private void parseResponseStr( String respStr )
    {
        try
        {
            competencyMap = new HashMap<>();
            
            hasValidScore=false;
            
            // No response str - skip clicked or something like that. 
            if( respStr==null || respStr.isEmpty() )
            {
                LogService.logIt( "ScoredChatIntnItem.parseResponseStr() respStr is null. Looking for BotEvent directly. " + toString() );
                
                if( intnObj!=null )
                {
                    if( botFacade==null )
                        botFacade = BotFacade.getInstance();
                    
                    BotEvent be = botFacade.findBotEvent( testEvent.getTestEventId(), intnObj.getUniqueid(), intnItemObj.getSeq() );
                    
                    if( be!=null )
                    {
                        botEventId = be.getBotEventId();
                        LogService.logIt( "ScoredChatIntnItem.parseResponseStr() Found BotEvent directly. be.getBotEventId=" + botEventId );
                    }
                    else
                    {
                        LogService.logIt( "ScoredChatIntnItem.parseResponseStr() No BotEvent found directly. Will be scored as a zero. " + toString() );
                        setForZeroScore();
                        hasValidScore = true;
                    }
                        
                }
                
                return;
            }
            
            respStr = URLDecoder.decode(respStr, "UTF8" );
            
            String[] vals = respStr.split(";");
            
            int iframeItemTypeId = 0;
            
            if( vals.length>0 )
                iframeItemTypeId = Integer.parseInt( vals[0] );
            
            if( iframeItemTypeId!= IframeItemType.CHAT.getIframeItemTypeId() )
                throw new Exception( "IframeItemTypeId is invalid: " + iframeItemTypeId + ", expected "  + IframeItemType.CHAT.getIframeItemTypeId() );
            
            if( vals.length>1 )
                botEventId = Long.parseLong( vals[1] );
            
            if( botEventId<=0 )
                throw new Exception( "BotEventId invalid: " + botEventId );
            
        }
        
        catch( NumberFormatException e )
        {
            LogService.logIt( "ScoredChatIntnItem.parseResponseStr() " + e.toString() + ", respStr=" + respStr + ", " + toString() );            
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "ScoredChatIntnItem.parseResponseStr() " + respStr + ", " + toString() );            
        }        
    }    
        
    
    private void setForZeroScore()
    {
        overallScore = 0; // 0-100

        /**
         * String = competency name
         * 
         * float[0] = points
         * float[1] = max points
         * float[2] = assigned points
         * float[3] = percent of max
         * float[4] = z-score
         * float[5] = scaled score
         * float[6] = weight
         * 
         */
        competencyMap = new HashMap<>();

        /*
         Metascore 2 = Rapport Empathy Count (To be Avgd)
         Metascore 3 = Spelling Rate (To be Avg'd)
         Metascore 4 = Avg Resp Time Seconds (To be Avg'd)
         Metascore 5 = Negative Expression Count (To be Avg'd)
        */
        metaScores = new float[5];    
        
    }
    
    public void calculate()
    {
        try
        {
            if( hasValidScore )
                return;
            
            if( botEventId<=0 )
            {
                setForZeroScore();
                LogService.logIt( "ScoreChatIntnItem BotEventId invalid: " + botEventId + ", " + toString() );
                return;
            }

            if( botFacade==null )
                botFacade = BotFacade.getInstance();
            
            botEvent = botFacade.getBotEvent(botEventId);
            
            if( botEvent==null )
                throw new Exception( "BotEvent is null! botEventId=" + botEventId );

            if( !botEvent.getBotEventStatusType().isCompleteOrHigher() )
            {
                setForZeroScore();
                LogService.logIt( "ScoreChatIntnItem BotEvent is not complete. Looks like skipped. Scoring as a zero. botEventId=" + botEventId + ", " + toString() );
                return;
                // throw new Exception( "BotEvent is not complete. " );
            }
            
            if( botEvent.getRespJson()==null || botEvent.getRespJson().isEmpty() )
                throw new Exception( "No response JSON found in BotEvent record. " );
            
            botInstance = botFacade.getBotInstance( botEvent.getBotInstanceId() );
            
            if( botInstance==null )
                throw new Exception( "BotInstance is null! botInstanceId=" + botEvent.getBotInstanceId() );
            
            botInstance.setBotIntentList( botFacade.getBotIntentsForBotInstanceId( botInstance.getBotInstanceId() ));
                      
            allBotInstances = new ArrayList<>();
            
            allBotInstances.add( botInstance );
            
            allBotInstances.addAll( botFacade.getReferencedBotInstances( botInstance.getBotInstanceId() ) );
            
            chatResponse = new ChatResponse();
            
            chatResponse.readFromJsonStr( botEvent.getRespJson() );
            
            List<String> allUserResps = chatResponse.getAllUserResponses();
            
            hasValidScore = true;

            userMessageCount = allUserResps.size();
            
            avgRespTimeSecs = chatResponse.getAvgUserRespTime();

            /*
            * String = competency name 
            * float[0] = points
            * float[1] = max points (across all bot instances)
            * float[2] = assigned points
            * float[3] = percent of max
            * float[4] = z-score (uses mean / stdev from first bot instance that has a stdev more than zero)
            * float[5] = scaled score
            * float[6] = weight (uses weight from first bot instance that has a weight value more than zero)            
            */
            competencyMap = BotScoreUtils.computeCompetencyScoreMap( allBotInstances , chatResponse );
                   
            rapportEmpathyCount = 0;
            negativeExpressionCount = 0;
            spellErrorCount = 0;
            grammarErrorCount = 0;
            wordCount = 0;
            
            Object[] d1;
            int[] vals;
            float mean;
            float stdev;
            
            float[] scaledVals = BotScoreUtils.getScaledMeanStdev(botInstance);
            
            //float errorRate = 0;
            //float rapportEmpathyRate = 0;
            //float empathyRate = 0;
            int strWrdCnt;
                        
            for( String resp : allUserResps )
            {
                if( resp==null || resp.trim().isEmpty() )
                    continue;
                
                // dl[0] = int[] vals
                // dl[1] = MAP misspelled words and counts.
                d1 = LocalEssayScoringUtils.getWritingAnalysis(resp, getTestLocale(), getIpCountry(), getWordsToIgnoreLc() );
                
                // total errs [0]
                // spelling [1]
                // grammar [2]
                // style [3]
                // total words[4]
                vals = (int[])d1[0];
                
                wordCount += vals[4];
                strWrdCnt = vals[4];
                
                spellErrorCount += vals[1]; 
                grammarErrorCount += vals[2];
                
                if( strWrdCnt>2 && !hasEndPunc( resp, getTestLocale() ) )
                    grammarErrorCount++;
                
                if( hasRapport( resp ) )
                    rapportEmpathyCount++;
                
                else if( hasEmpathy( resp ))
                    rapportEmpathyCount++;

                if( hasNegativeWord( resp ))
                    negativeExpressionCount++;
            }
            
            spellErrorRate = (spellErrorCount + grammarErrorCount)>0 && wordCount>0 ? 100*(spellErrorCount + grammarErrorCount)/wordCount : 0;
            
            if( spellErrorRate>100 )
                spellErrorRate = 100;

            
            float totalWeight = 0;
            float totalScores = 0;
            float[] compScoreVals;
            
            for( String c : competencyMap.keySet() )
            {
                compScoreVals = competencyMap.get( c );
                
                // skip if zero weight
                if( compScoreVals[6]<=0 )
                    continue;
                
                totalWeight += compScoreVals[6];
                totalScores += Math.min( compScoreVals[5], 100 );
            }
            
            
            mean = botInstance.getFloatParam2();
            stdev = botInstance.getFloatParam3();
            
            if( stdev<=0 )
            {
                mean = BotScoreUtils.DEF_SPELLING_ERROR_MEAN;
                stdev = BotScoreUtils.DEF_SPELLING_ERROR_SD;
            }
            
            // invert this score.
            spellErrorScore = getScaledScore(spellErrorRate, true, mean, stdev, scaledVals );
            
            spellErrorScore = Math.min( spellErrorScore, 100 );
            // spellErrorScore = 100f - spellErrorScore;
            
            // Add spelling to overall if has weight.
            if( botInstance.getFloatParam13()>0 )
            {
                totalWeight += botInstance.getFloatParam13();
                totalScores += spellErrorScore;                
            }
            
            mean = botInstance.getFloatParam7();
            stdev = botInstance.getFloatParam8();
            
            if( stdev<=0 )
            {
                mean = BotScoreUtils.DEF_RAPPORT_MEAN;
                stdev = BotScoreUtils.DEF_RAPPORT_SD;
            }
            
            rapportEmpathyScore = getScaledScore(rapportEmpathyCount, false, mean, stdev, scaledVals );

            rapportEmpathyScore = Math.min( rapportEmpathyScore, 100 );
            
            // Add rapport to overall if has weight.
            if( botInstance.getFloatParam9()>0 )
            {
                totalWeight += botInstance.getFloatParam9();
                totalScores += rapportEmpathyScore;                
            }
                        
            mean = botInstance.getFloatParam5();
            stdev = botInstance.getFloatParam6();
            
            if( stdev<=0 )
            {
                mean = BotScoreUtils.DEF_RESPTIME_MEAN;
                stdev = BotScoreUtils.DEF_RESPTIME_SD;
            }
            
            avgRespTimeScore = getScaledScore(avgRespTimeSecs, false, mean, stdev, scaledVals );

            avgRespTimeScore = Math.min( avgRespTimeScore, 100 );
            
            // Add response time to overall if has weight.
            if( botInstance.getFloatParam4()>0 )
            {
                totalWeight += botInstance.getFloatParam4();
                totalScores += avgRespTimeScore;                
            }

            overallScore = totalWeight>0 ? totalScores/totalWeight : 0;
            
            if( negativeExpressionCount>0 )
            {
                float maxScore = 100f - negativeExpressionCount*BotScoreUtils.MAX_SCORE_DROP_PER_NEGATIVE;
                
                overallScore = Math.min( overallScore, maxScore );
            }
            
            if( botInstance!=null && botInstance.getFloatParam12()>0 && botEvent!=null && botEvent.getTotalPoints()<botInstance.getFloatParam12() )
            {
                LogService.logIt( "ScoreChatIntnItem botEvent.totalPoints (" + botEvent.getTotalPoints() +  ") below threshold (" + botInstance.getFloatParam12() + "). Setting score to 0. botEventId=" + botEventId + ", " + toString() );
                overallScore = 0;
            }

            if( botInstance!=null && botInstance.getIntParam9()>0 && botEvent!=null && wordCount<botInstance.getIntParam9() )
            {
                LogService.logIt( "ScoreChatIntnItem botEvent.getTotalWords (" + wordCount +  ") below threshold (" + botInstance.getIntParam9() + "). Setting score to 0. botEventId=" + botEventId + ", " + toString() );
                overallScore = 0;
            }
            
            
            hasValidScore = true;

            updateBotEventVals();
            
            /*
             Metascore 2 = Rapport Empathy Count (To be Avg'd)
             Metascore 3 = Spelling Rate (To be Avg'd)
             Metascore 4 = Avg Resp Time Seconds (To be Avg'd)
             Metascore 5 = Negative Expression Count (To be Avg'd)
            */
            metaScores = new float[6];
            metaScores[2] = rapportEmpathyCount;
            metaScores[3] = spellErrorRate;
            metaScores[4] = avgRespTimeSecs;
            metaScores[5] = negativeExpressionCount;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ScoredChatIntnItem.calculate() " + toString() );
            hasValidScore = false;
            updateBotEventForError( "ScoredChatIntnItem.calculate() " + e.toString() );
        }        
    }
    
    
    public float getMetaScore( int i )
    {
        return metaScores!=null && metaScores.length>i ? metaScores[i] : 0;
    }
    
    
    private boolean hasEndPunc( String inStr, Locale locale )
    {
        if( inStr==null )
            return false;
        
        if( locale==null || !locale.getLanguage().toLowerCase().equals( "en" ) )
            return false;
        
        inStr = inStr.trim();
        
        if( inStr.isEmpty() )
            return false;
        
        if( inStr.endsWith( "." ) || inStr.endsWith( "?" ) || inStr.endsWith( "!" ) )
            return true;
        
        return false;
    }

    
    private void updateBotEventForError( String msg )
    {
        try
        {
            if( botEvent==null )
                throw new Exception( "BotEvent is null" );
            
            botEvent.appendNote(0, 0, null, msg );
            
            if( botFacade==null )
                botFacade = BotFacade.getInstance();
            
            botEvent.setBotEventStatusTypeId( BotEventStatusType.SCORE_ERROR.getBotEventStatusTypeId() );            
            botFacade.saveBotEvent(botEvent);
        }
        catch( Exception e )
        {
            LogService.logIt( "ScoreChatIntnItem.updateBotEventForError() " + botEventId );
        }        
    }

    
    private void updateBotEventVals()
    {
        try
        {
            if( botEvent==null )
            {
                LogService.logIt( "ScoredChatIntnItm.updateBotEventVals() botEvent is null so returning. " + toString() );
                return;
                // throw new Exception( "BotEvent is null" );
            }
            
            if( botFacade==null )
                botFacade = BotFacade.getInstance();
            
            if( !hasValidScore )
                throw new Exception( "Score is not valid" );  
            
            botEvent.setUserMessageCount( (int) userMessageCount );
            botEvent.setTotalWords( (int) wordCount );
            
            botEvent.setOverallScore(overallScore);
            
            botEvent.setRapportCount( rapportEmpathyCount );
            botEvent.setRapportScore( rapportEmpathyScore );
            botEvent.setRapportWeight( botInstance.getFloatParam9() );
            
            botEvent.setSpellingErrorCount( (int) spellErrorCount );
            botEvent.setGrammarErrorCount((int) grammarErrorCount );
            botEvent.setSpellGrammarErrorRate( spellErrorRate );
            botEvent.setSpellingGrammarScore( spellErrorScore );
            botEvent.setSpellingGrammarWeight( botInstance.getFloatParam13() );
            
            botEvent.setNegativeExpressionCount( (int) negativeExpressionCount );
            
            botEvent.setAverageResponseTime( avgRespTimeSecs );
            botEvent.setResponseTimeScore( avgRespTimeScore );
            botEvent.setResponseTimeWeight( botInstance.getFloatParam4());
            
            // Set<String> competencies = competencyMap.keySet();
            
            List<String> competencies = new ArrayList<>();
            competencies.addAll( competencyMap.keySet() );
            Collections.sort( competencies );
            
            String compName;
            float[] compVals;
            
            for( int i=0;i<competencies.size(); i++ )
            {
                compName = competencies.get( i );
                compVals = competencyMap.get( compName );
                
                botEvent.setCompetencyName( i+1, compName);
                botEvent.setCompetencyPoints( i+1, compVals[0]);
                botEvent.setCompetencyScore( i+1, Math.min(compVals[5],100) );
                botEvent.setCompetencyWeight( i+1, compVals[6]);
            }
                        
            botEvent.setBotEventStatusTypeId( BotEventStatusType.SCORED.getBotEventStatusTypeId() );            
            botFacade.saveBotEvent(botEvent);
        }
        catch( Exception e )
        {
            LogService.logIt( "ScoreChatIntnItem.updateBotEventVals() " + botEventId );
            updateBotEventForError( "ScoredChatIntnItem.updateBotEventVals() " + e.toString() );
        }
    }
    
    private float getScaledScore( float value, boolean invert, float mean, float stdev, float[] scaledStats)
    {
        float scaled = value;
                
        if( stdev>0 )
        {
            float raw = (value - mean)/stdev;
            
            if( invert )
                raw = -1.0f*raw;
            
            scaled = scaledStats[1]*raw + scaledStats[0];
        }
        else if( invert )
            scaled = 100 - value;
        
        if( scaled>100 )
            scaled = 100;
        
        if( scaled<0 )
            scaled=0;
        
        return scaled;
    }
    
    private boolean hasNegativeWord( String s )
    {
        if( s==null || s.trim().isEmpty() )
            return false;
                
        init();
        
        s = s.toLowerCase();
        
        for( String v : negativePhrases )
        {
            if( s.contains(v) )
                return true;
        }
        
        return false;        
    }
    
    private boolean hasRapport( String s )
    {
        if( s==null || s.trim().isEmpty() )
            return false;
                
        init();
        
        s = s.toLowerCase();
        
        for( String v : rapportPhrases )
        {
            if( s.contains(v) )
                return true;
        }
        
        return false;
    }

    private boolean hasEmpathy( String s )
    {
        if( s==null || s.trim().isEmpty() )
            return false;
                
        init();
        
        s = s.toLowerCase();
        
        for( String v : empathyPhrases )
        {
            if( s.contains(v) )
                return true;
        }
        
        return false;
    }

    

    @Override
    public String toString() {
        return "ScoredChatIntnItem{" + "botEventId=" + botEventId +", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId()) + '}';
    }

    /**
     * map of topic name, int[]
     *    int[0] = number correct        ( for this item this means points )
     *    int[1] = number total this topic.  max points for competency
     *    int[2] = number of items that were partially correct.  ( 0 )
     *    int[3] = total number of items this topic. (1) 
     */
    public Map<String,int[]> getTopicMap()  
    {
        if( !hasValidScore || userMessageCount<=0 )
            return null;
        
        Map<String,int[]> out = new TreeMap<>();

        float[] cpts;
        int[] tpx;
        
        
        // Rapport / Empathy
        String cName = botInstance.getStrParam3();
        
        if( cName!=null && !cName.trim().isEmpty() )
        {
            cName = cName.trim();
            
            tpx = new int[4];

            // number correct. Either 1 or 0 for this 
            tpx[0] = (int) rapportEmpathyScore; // points=number correct for this competency
            tpx[1] = 100; // max points = total number available for this competency
            tpx[2] = 0; // no partials
            tpx[3] = 1;
            out.put( cName, tpx );
        }

        // Spelling / Grammar
        cName = botInstance.getStrParam4();
        
        if( cName!=null && !cName.trim().isEmpty() )
        {
            cName = cName.trim();
            
            tpx = new int[4];

            // number correct. Either 1 or 0 for this 
            tpx[0] = (int) spellErrorScore; // points=number correct for this competency
            tpx[1] = 100; // max points = total number available for this competency
            tpx[2] = 0; // no partials
            tpx[3] = 1;
            out.put( cName, tpx );
        }
        
        
        /**
         * String = competency name
         * 
         * float[0] = points
         * float[1] = max points
         * float[2] = assigned points
         * float[3] = percent of max
         * float[4] = z-score
         * float[5] = scaled score
         * float[6] = weight
         * 
         */
        // competencyMap;        
        if( competencyMap==null || competencyMap.isEmpty() )
            return out.size()>0 ? out : null;
        
                
        for( String c : competencyMap.keySet() )
        {            
            cpts = competencyMap.get(c);
            
            if( cpts[1]<=0 )
                continue;
            
            tpx = new int[4];
            
            // number correct. Either 1 or 0 for this 
            tpx[0] = (int) Math.min(cpts[5], 100); // Scaled Score
            tpx[1] = 100; // Scale top
            // tpx[0] = (int) cpts[0]; // points=number correct for this competency
            // tpx[1] = (int) cpts[1]; // max points = total number available for this competency
            tpx[2] = 0; // no partials
            tpx[3] = 1;            
            out.put( c, tpx );
        }
        
        return out;
    }
    
    
    
    public List<TextAndTitle> getTextAndTitleList()
    {
        List<TextAndTitle> out = new ArrayList<>();

        if( !hasValidScore  || userMessageCount<=0 )
            return out;
        
        String title = intnItemObj.getContent();        

        String text = chatResponse.getMessagesAsTextStr( true, getTestLocale() );
        TextAndTitle tt = new TextAndTitle( text, title );
        out.add( tt );

        return out;
    }
    
    
    /**
     * Format is:
     *    iframeitemtypeid;boteventid;totalusermessages;totalwords;avgrespsecs;rapportcount;spellcount;grammarcount;negativecount;
     * 
     * @param respValue
     * @return 
     */
    public String getSelectedRespForItemResponse( String respValue )
    {
        if( respValue==null )
            respValue="NoRespValue";
        
        StringBuilder sb = new StringBuilder( respValue );
        
        sb.append( ";" + userMessageCount );
        sb.append( ";" + wordCount );
        sb.append( ";" + avgRespTimeSecs );
        sb.append( ";" + rapportEmpathyCount );
        sb.append( ";" + spellErrorCount );
        sb.append( ";" + grammarErrorCount );
        sb.append( ";" + negativeExpressionCount );
        
        return sb.toString();
    }
    
    
    
    
    public List<String> getWordsToIgnoreLc()
    {
        List<String> out = new ArrayList<>();
        
        if( this.intnItemObj!=null )    
        {
            String wl = StringUtils.getBracketedArtifactFromString( intnItemObj.getTextscoreparam1(), Constants.SPELLING_IGNORE_KEY );
            if( wl!=null && !wl.isBlank() )
            {
                for( String s : wl.split(",") )
                {
                    s = s.trim();
                    if( s.isBlank() )
                        continue;
                    out.add( s.toLowerCase() );
                }                
            }
        }
                
        if( this.testEvent==null || this.testEvent.getUser()==null )
            return out;

        User user = this.testEvent.getUser();
        
        if( user.getFirstName()!=null && !user.getFirstName().isBlank() )
            out.add( user.getFirstName().toLowerCase() );

        if( user.getLastName()!=null && !user.getLastName().isBlank() )
            out.add( user.getLastName().toLowerCase() );

        if( user.getEmail()!=null && !user.getEmail().isBlank() )
            out.add( user.getEmail().toLowerCase() );

        return out;        
    }
    
    
    
    
    
    private String getIpCountry()
    {
        if( testEvent!=null && testEvent.getIpCountry()!=null && !testEvent.getIpCountry().isEmpty() )
            return testEvent.getIpCountry();
        
        return null;
    }
    
    private Locale getTestLocale()
    {
        if( testEvent!=null && testEvent.getLocaleStr()!=null && !testEvent.getLocaleStr().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( testEvent.getLocaleStr() );

        return Locale.US;
    }
           

    public boolean getHasValidScore() {
        return hasValidScore;
    }


    public Map<String, float[]> getCompetencyMap() {
        return competencyMap;
    }

    public float getOverallScore() {
        return overallScore;
    }

    public float getUserMessageCount() {
        return userMessageCount;
    }

    public float getRapportEmpathyCount() {
        return rapportEmpathyCount;
    }

    public float getRapportEmpathyScore() {
        return rapportEmpathyScore;
    }

    public float getSpellErrorRate() {
        return spellErrorRate;
    }

    public float getSpellErrorScore() {
        return spellErrorScore;
    }

    public float getNegativeExpressionCount() {
        return negativeExpressionCount;
    }

    public float getAvgRespTimeSecs() {
        return avgRespTimeSecs;
    }

    public float getAvgRespTimeScore() {
        return avgRespTimeScore;
    }

    
    
}


