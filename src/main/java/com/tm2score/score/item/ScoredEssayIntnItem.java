/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.item;

import com.tm2score.entity.essay.EssayPrompt;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.essay.DiscernFacade;
import com.tm2score.essay.DiscernUtils;
import com.tm2score.essay.EssayScoreStatusType;
import com.tm2score.service.LogService;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.user.User;
import com.tm2score.essay.EssayPlagiarismCheckType;
import com.tm2score.essay.EssayScoringUtils;
import com.tm2score.essay.copyscape.CopyScapeUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.Tracker;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.util.TextProcessingUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class ScoredEssayIntnItem {

    // Dummy Essay prompt will have the system perform spell/plagiarism checks but does not incorporate an essay score.
    // So, it still produces a score.
    private static final int DUMMY_ESSAY_PROMPT_ID = 999999;
    
    public static final float AVG_WORDS_PER_MIN_PLAG_CHECK = 15f;
    private static final float MAX_WORDS_PER_MIN_PLAG_CHECK = 27f;
    private static final int MIN_WORDS_FORCE_WEB_PLAG_CHECK = 360;
    private static final int MIN_WORDS_WEB_PLAG_CHECK = 25;
    private static final float MAX_DUPLICATE_WORDS_PCT = 35;
    private static final float MAX_DUPLICATE_LONGWORDS_PCT = 30;
    
    private static String[] VALID_LANGUAGES = new String[] {"en" };
    /**
     * Interaction Item values:
     * ScoreParam1 is the promptId number to use.
     *
     * ScoreParam3 = Max Points (default (0)=100)
     *
     */



    static float MIN_CONFIDENCE = 0.1f;

    SimJ simJ;

    // Machine Score goes between 0 - 100.0
    float machineScore=0;

    // Confidence goes between 0 - 1.0
    float confidence=0;
    
    int spellErrors = 0;
    int otherErrors = 0;
    int plagiarized = 0;
    int maxPlagCheckRows = 0;
    
    float wpm = 0;
    float highWpm = 0;
    int speedError = 0;
    int totalWords = 0;
    float pctDupWords = 0;
    float pctDupLongWords = 0;

    int minWords = 1;
    int maxWords = 0;

    int essayPromptId;
    int ct5ItemId;
    int ct5ItemPartId;
    String essayStr;
    String question;
    int equivWords = 0;
    float composeTime = 0;

    boolean hasValidScore = false;
    long testEventId = 0;
    User user;
    int nodeSeqId = 0;
    int subnodeSeqId = 0;

    boolean pendingExternalScores = false;

    DiscernFacade discernFacade = null;
    DiscernUtils discernUtils = null;
    int webPlagCheckOk = 0;
    String transCompare = null;
    float transCompareScore = -1;
    String translatedText  = null;
    
    Locale txtLocale;
    String teIpCountry;
    String errMsg;
    
    Map<String,Integer> misSpells;
    List<String> spellWordsToIgnore;
    
    int unscoredEssayId;
    
    public boolean hasSpellingGrammarStyle = false;
    



    public ScoredEssayIntnItem( long testEventId, User user, String teIpCountry, SimJ simJ, SimJ.Intn intnObj, SimJ.Intn.Intnitem intItemObj, int promptId, int ct5ItemId, int ct5ItemPartId, String essayStr, String question, int minWds, int maxWords, float cTime, int webPlagCheckOk, int maxPlagCheckRows, String transCompare)
    {
        this.testEventId = testEventId;
        this.user=user;
        this.teIpCountry = teIpCountry;
        this.simJ = simJ;
        this.nodeSeqId = intnObj.getSeq();
        this.subnodeSeqId = intItemObj.getSeq();
        this.essayPromptId = promptId;
        this.ct5ItemId=ct5ItemId;
        this.ct5ItemPartId=ct5ItemPartId;
        this.essayStr = essayStr;
        this.question = question;
        this.minWords = minWds>0 ? minWds : 1;
        this.maxWords = maxWords;
        this.composeTime = cTime;
        this.webPlagCheckOk=webPlagCheckOk;
        this.maxPlagCheckRows=maxPlagCheckRows;
        this.transCompare=transCompare;
        
        if( intItemObj.getLangcode()!=null && !intItemObj.getLangcode().isBlank() )
            txtLocale = I18nUtils.getLocaleFromCompositeStr( intItemObj.getLangcode() );

        else if( simJ.getLang()!=null && !simJ.getLang().isBlank() )
            txtLocale = I18nUtils.getLocaleFromCompositeStr( simJ.getLang() );

        setSpellWordsToIgnore( StringUtils.getBracketedArtifactFromString( intItemObj.getTextscoreparam1(), Constants.SPELLING_IGNORE_KEY ) );
        
        
        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") ndseq=" + nodeSeqId + ", snSeq=" + subnodeSeqId + ", promptId=" + promptId + ", min/max words=" + this.minWords + "/" + this.maxWords );
    }

    
    public ScoredEssayIntnItem( long testEventId, 
            User user, 
            Locale loc, 
            String teIpCountry, 
            int nodeSeq, 
            int subnodeSeq, 
            int promptId, 
            String essayStr, 
            int minWds, 
            int maxWords, 
            float cTime, 
            int webPlagCheckOk, 
            List<String> spellWordsToIgnore)
    {
        this.testEventId = testEventId;
        this.user = user;
        this.txtLocale = loc;
        this.teIpCountry=teIpCountry;
        this.nodeSeqId = nodeSeq;
        this.subnodeSeqId = subnodeSeq;
        this.essayPromptId = promptId;
        this.essayStr = essayStr;
        this.minWords = minWds>0 ? minWds : 1;
        this.maxWords = maxWords;
        this.composeTime = cTime;
        this.webPlagCheckOk=webPlagCheckOk;
        this.spellWordsToIgnore=spellWordsToIgnore;
        
        LogService.logIt("ScoredEssayIntnItem(testEventId=" + testEventId + ") ndseq=" + nodeSeq + ", snSeq=" + subnodeSeq + ", promptId=" + promptId + ", min/max words=" + this.minWords + "/" + this.maxWords );
        
    }
    
    
    
    public void calculate()
    {
        try
        {            
            if( essayStr!=null && !essayStr.isEmpty() )
            {
                String essayWithoutQuestion = removeQuestionFromEssayStr();
                
                equivWords = StringUtils.numWords( essayWithoutQuestion ); //    DataEntryItem.getEquivWords(essayStr);

                if( maxWords> 0 && equivWords>maxWords )
                {
                    essayStr = StringUtils.truncateToMaxWordCt( essayStr, maxWords );
                    equivWords = maxWords;
                }

                // float maxSimilarity = getMaxEssaySimilarity();

                // boolean spellingOk = isSpellingOk();


                if( equivWords>=minWords )
                {
                    // LogService.logIt( "ScoredEssayIntnItem.calculate() AAA testEventId=" + testEventId);
                    
                    if( essayPromptId <= 0 )
                        return;

                    if( discernFacade == null )
                        discernFacade = DiscernFacade.getInstance();

                    UnscoredEssay ue = testEventId<=0 ? null : discernFacade.getUnscoredEssayForMinStatus(testEventId, nodeSeqId, subnodeSeqId, EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId() );

                    EssayPrompt ep = null;

                    boolean hasValidTextForDiscern = hasValidTextForDiscern(essayWithoutQuestion);
                                                               
                    // Found an unscored essay. Yay.
                    if( ue!=null )
                    {
                        ///LogService.logIt( "ScoredEssayIntnItem.calculate() BBB testEventId=" + testEventId + ", ue.getWpm()=" + ue.getWpm() );
                        unscoredEssayId = ue.getUnscoredEssayId();
                        
                        EssayScoreStatusType scoreStatus = EssayScoreStatusType.getValue(  ue.getScoreStatusTypeId() );
                        
                        if( ue.getDuplicateContentWebPct() >= Constants.WEB_PLAG_CHECK_MAX_MATCH || ue.getSimilarUnscoredEssayId()>0 )
                            plagiarized = 1;

                        wpm = ue.getWpm();
                        highWpm = ue.getHighWpm();
                        
                        transCompareScore = calculateTranslateCompareScore(ue);
                        translatedText = ue.getTranslatedEssay();
                        
                        if( !DiscernUtils.isDiscernOn()  && ( scoreStatus.incomplete() || scoreStatus.unsubmitted() ) )
                        {
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.1 testEventId=" + testEventId + ". Discern is Turned Off." );
                            
                            ue.setScoreDate( new Date() );
                            ue.setScoreStatusTypeId( EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId() );
                            discernFacade.saveUnscoredEssay(ue, true);
                            errMsg = "Discern status in progress (incomplete or unsubmitted) but discern is not turned on so marked this as failed.";
                            scoreStatus = EssayScoreStatusType.getValue(  ue.getScoreStatusTypeId() );
                        }

                        transCompareScore=ue.getTranslateCompareScore();
                        if( transCompareScore>=0 )
                            hasValidScore = true;
                        
                        if( ct5ItemId>0 && ue.getCt5ItemId()<=0 )
                        {
                            ue.setCt5ItemId( ct5ItemId);
                            ue.setCt5ItemPartId(ct5ItemPartId);
                            discernFacade.saveUnscoredEssay(ue, false);                            
                        }

                        if( !DiscernUtils.isDiscernOn() )
                        {
                            machineScore = plagiarized==1 ? 0 : ue.getScoreFmErrorRates();
                            confidence=1;
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.2 testEventId=" + testEventId + ". macineScoreFmErrors=" + machineScore );
                        }       
                        
                        
                        if( scoreStatus.skipped() )
                        {
                            if( ue.getScoreDate()==null )
                                ue.setScoreDate( new Date() );
                            
                            hasValidScore = true;
                            machineScore = plagiarized==1 ? 0 : ue.getScoreFmErrorRates();
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.3 testEventId=" + testEventId + ". macineScore=" + machineScore );
                        }
                        
                        else if( !hasValidTextForDiscern || scoreStatus.invalidTextForDiscern() )
                        {
                            ue.setScoreDate( new Date() );
                            ue.setScoreStatusTypeId( EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId() );
                            discernFacade.saveUnscoredEssay(ue, true);
                            scoreStatus = EssayScoreStatusType.getValue(  ue.getScoreStatusTypeId() );                            
                            hasValidScore = true;
                            machineScore = plagiarized==1 ? 0 : ue.getScoreFmErrorRates();
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.43 testEventId=" + testEventId + ". macineScore=" + machineScore );
                        }
                        
                        else if( usesDummyEssayPrompt() )
                        {
                            ue.setScoreDate( new Date() );
                            ue.setScoreStatusTypeId( EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId() );
                            ue.setCt5ItemId( ct5ItemId);
                            ue.setCt5ItemPartId(ct5ItemPartId);

                            discernFacade.saveUnscoredEssay(ue, true);
                            
                            scoreStatus = EssayScoreStatusType.getValue(  ue.getScoreStatusTypeId() );                            
                            hasValidScore = true;
                            machineScore = plagiarized==1 ? 0 : ue.getScoreFmErrorRates();
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.44 testEventId=" + testEventId + ". machineScore=" + machineScore + ", plagiarized=" + plagiarized + ", ue.getScoreFmErrorRates()=" + ue.getScoreFmErrorRates());
                        }
                        

                        // if in incomplete status, see if there are new scores present.
                        if( scoreStatus.incomplete() )
                        {
                            // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Checking for new scores unscoredEssayId=" + ue.getUnscoredEssayId() );
                            checkForNewScores( ue );
                        }
                            
                        if( scoreStatus.unsubmitted() && hasValidTextForDiscern )
                        {
                            ep = discernFacade.getEssayPrompt( essayPromptId );
                            ue.setEssayPromptId( essayPromptId );
                            ue.setCt5ItemId( ct5ItemId);
                            ue.setCt5ItemPartId(ct5ItemPartId);
                            // submit it.
                            submitForExternal(ep, ue, essayWithoutQuestion );
                            pendingExternalScores = true;
                        }

                        else if( scoreStatus.incomplete() )
                            pendingExternalScores = true;

                        else if( scoreStatus.completed() )
                        {
                            confidence = ue.getComputedConfidence();
                            machineScore = ue.getComputedScore();
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.5 testEventId=" + testEventId + ". macineScore=" + machineScore );

                            totalWords = ue.getTotalWords();
                            spellErrors = ue.getSpellingErrors();
                            otherErrors = ue.getGrammarErrors() + ue.getStyleErrors();
                            hasSpellingGrammarStyle = ue.getHasSpellingGrammarStyle()==1;

                            pctDupWords = ue.getPctDuplicateWords();
                            pctDupLongWords = ue.getPctDuplicateLongWords();
                            plagiarized = ue.getSimilarUnscoredEssayId()>0 ? 1 : 0;
                            
                            if( plagiarized<=0 && pctDupWords > MAX_DUPLICATE_WORDS_PCT )
                            {
                                plagiarized = 1;
                                LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") XXX Invalidating Essay score due to too many duplicate words. pctDupWords=" + pctDupWords + ", testEventId=" + testEventId );
                            }
                            else if( plagiarized<=0 && pctDupLongWords > MAX_DUPLICATE_LONGWORDS_PCT )
                            {
                                plagiarized = 1;
                                LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") XXX Invalidating Essay score due to too many duplicate LONG words. pctDupLongWords=" + pctDupLongWords + ", testEventId=" + testEventId );
                            }                            

                            if( confidence>MIN_CONFIDENCE )
                                hasValidScore = true;
                            
                            if( confidence<=MIN_CONFIDENCE )
                                errMsg = "Machine Confidence (" + confidence + ") is below minimum (" + MIN_CONFIDENCE +")."; 
                        }

                        // Just cause to start over.
                        else if( scoreStatus.cancelled() )
                            ue = null;

                        // denote as not scorable.
                        else if( scoreStatus.failed() )
                        {
                            confidence = ue.getComputedConfidence();
                            machineScore = ue.getComputedScore();
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.6 testEventId=" + testEventId + ". machineScore=" + machineScore );
                            // hasValidScore = false;
                            
                            //if( !hasValidScore )
                            errMsg = "Essay scoring process failed.";                             
                        }

                        else if( scoreStatus.notEnabled() || !hasValidTextForDiscern )
                        {
                            //confidence = ue.getComputedConfidence();
                            //machineScore = ue.getComputedScore();
                            ue.setComputedConfidence(confidence);
                            ue.setComputedScore(machineScore);
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.6 testEventId=" + testEventId + ". machineScore=" + machineScore );
                            hasValidScore = true;
                        }
                        
                        if( hasValidScore )
                        {
                            recomputeWritingAnalysis(ue);                            
                        }
                    }

                    // If at this point, ue is null, we need to submit a new request.
                    if( ue==null )
                    {
                        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Submitting new essay." );

                        if( ep == null )
                            ep = discernFacade.getEssayPrompt( essayPromptId );
                        
                        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Submitting new essay BBB - Have Promopt" );
                        ue = new UnscoredEssay();
                        ue.setEssay(essayStr);
                        ue.setEssayPromptId(essayPromptId);
                        ue.setCt5ItemId( ct5ItemId);
                        ue.setCt5ItemPartId(ct5ItemPartId);
                        ue.setNodeSequenceId(nodeSeqId);
                        ue.setScoreStatusTypeId( EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId() );
                        ue.setSubnodeSequenceId(subnodeSeqId);
                        ue.setTestEventId(testEventId);
                        ue.setCreateDate( new Date() );
                        ue.setSecondsToCompose( (int) composeTime );

                        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Submitting new essay CCC" );
                        
                        Object[] d1 = EssayScoringUtils.getWritingAnalysis(essayStr, getTextLocale(), teIpCountry, getWordsToIgnoreLc() );

                        int[] vals = (int[])d1[0];
                        misSpells = (Map<String,Integer>) d1[1];
                        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Submitting new essay DDD misSpells.size=" + misSpells.size() );
                        
                        
                        ue.setTotalWords( vals[4] );
                        ue.setSpellingErrors( vals[1] );
                        ue.setGrammarErrors( vals[2] );
                        ue.setStyleErrors( vals[3] );
                        ue.setPctDuplicateWords( vals[5] );
                        ue.setPctDuplicateLongWords( vals[6] );
                        
                        ue.setHasSpellingGrammarStyle( vals[7] );
                        hasSpellingGrammarStyle = ue.getHasSpellingGrammarStyle()==1;
                        
                        totalWords = ue.getTotalWords();
                        spellErrors = ue.getSpellingErrors();
                        otherErrors = ue.getGrammarErrors() + ue.getStyleErrors();
                        pctDupWords = ue.getPctDuplicateWords();
                        pctDupLongWords = ue.getPctDuplicateLongWords();

                        setHighWordsPerMinute(ue);
                        highWpm = ue.getHighWpm();
                        wpm = ue.getWpm();
                                                                        
                        transCompareScore = calculateTranslateCompareScore(ue);
                        if( transCompareScore>=0 )
                            hasValidScore=true;

                        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Starting Plag Check." );
                        
                        Date procStart = new Date();

                        int maxRowsToCheck = EssayPlagiarismCheckType.getValue( performWebDupContentCheck()? 1 : 0 ).getMaxRowsToCheck();
                        
                        maxPlagCheckRows = Math.max(maxPlagCheckRows, maxRowsToCheck);
                        
                        boolean useCt5ItemId = ct5ItemId>0 && ct5ItemPartId>0 && usesDummyEssayPrompt();
                        
                        // no local plag check if transcompare.
                        UnscoredEssay similarUE = transCompareScore>=0 || testEventId<=0 ? null : discernFacade.findSimilarEssayForPrompt(testEventId, essayPromptId, ct5ItemId, ct5ItemPartId, essayStr, maxRowsToCheck, useCt5ItemId );

                        Tracker.addResponseTime( "Local Plagiarism Test", new Date().getTime() - procStart.getTime() );

                        float forceScore = -1;

                        if( transCompareScore<0 &&  ue.getTotalWords() > 0 )
                        {
                            float[] errsPerWord = new float[4];

                            // fractErrs[0] = ((float) vals[0])/((float) ue.getTotalWords() );
                            errsPerWord[1] = ((float) vals[1])/((float) ue.getTotalWords() );
                            errsPerWord[2] = ((float) vals[2] + vals[3])/((float) ue.getTotalWords() );
                            //fractErrs[3] = ((float) vals[3])/((float) ue.getTotalWords() );

                            if( errsPerWord[1] > 0.25f || errsPerWord[2] > 0.25f )
                                forceScore = 0;
                            
                            else if( pctDupWords > MAX_DUPLICATE_WORDS_PCT )
                            {
                                plagiarized = 1;
                                LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Invalidating Essay score due to too many duplicate words. pctDupWords=" + pctDupWords + ", testEventId=" + testEventId );
                                forceScore = 0;
                            }
                            else if( pctDupLongWords > MAX_DUPLICATE_LONGWORDS_PCT )
                            {
                                plagiarized = 1;
                                LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Invalidating Essay score due to too many duplicate LONG words. pctDupLongWords=" + pctDupLongWords + ", testEventId=" + testEventId );
                                forceScore = 0;
                            }                            
                        }
                        

                        float webDuplicateContentPercent = 0;
                        String webDuplicateContentUrl;
                        // String webDupOut;

                        Object[] webDuplicateContentInfo = null;

                        // do not machine score or check for plag if it's a transcompare or if it's a survey (testEventId<=0)
                        if( testEventId>0 && transCompareScore<0 && forceScore<0 && performWebDupContentCheck() )
                        {
                            // Only look for dup content if there is no plagarism detected and we haven't already forced the score to 0 and essay is in english
                            if( similarUE==null && isTextLocaleOKForWebDupContentCheck( getTextLocale() ) )
                            {
                                // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") No local dupe content." );
                                procStart = new Date();
                                webDuplicateContentInfo = getWebDuplicateContentInfo( essayWithoutQuestion ) ;
                                Tracker.addResponseTime( "External Web Duplicate Content Test", new Date().getTime() - procStart.getTime() );
                            }
                            else
                            {
                                if( similarUE!= null )
                                    LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Found similar UnscoredEssay testEventId=" + testEventId + ", similarUE=" + similarUE.toString() );

                                if( !isTextLocaleOKForWebDupContentCheck( getTextLocale() ) )
                                    LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") skipping web dup check because language of test incompatible " + getTextLocale().toString() );
                            }
                        }
                        
                        if( webDuplicateContentInfo!=null && webDuplicateContentInfo.length>2 )
                        {
                            webDuplicateContentPercent = webDuplicateContentInfo[0]==null ? 0 : (Float) webDuplicateContentInfo[0];
                            webDuplicateContentUrl = (String) webDuplicateContentInfo[1];
                            // webDupOut = (String) webDuplicateContentInfo[2];

                            if( webDuplicateContentPercent>0 )
                            {
                                // LogService.logIt( "ScoredEssayIntItem.webDuplicateContentInfo[] webDuplicateContentPercent=" + webDuplicateContentPercent + ", webDuplicateContentUrl=" + webDuplicateContentUrl + ", output=" + webDupOut );
                                ue.setDuplicateContentWebUrl(webDuplicateContentUrl +", PERCENT MATCH: " + webDuplicateContentPercent );

                            }

                            ue.setDuplicateContentWebPct( webDuplicateContentPercent );
                        }

                        if( webDuplicateContentPercent>=Constants.WEB_PLAG_CHECK_MAX_MATCH )
                        {
                            plagiarized = 1;
                            forceScore = 0;
                        }

                        if( testEventId<=0 )
                            forceScore = 0;
                        
                        else if( similarUE!=null )
                        {
                            ue.setSimilarUnscoredEssayId( similarUE.getUnscoredEssayId() );
                            plagiarized = 1;
                            forceScore = 0;
                        }

                        // THIS IS WHRE WE SEE IF the Language is OK for scoring. If not, we just set forceScore to 0 unless it was plagiarized.
                        else if( plagiarized!=1 && !eligibleForMachineScoring() && !usesDummyEssayPrompt() )
                        {
                            forceScore = 0;
                        }                        

                        if( forceScore>-1 )
                        {
                            ue.setScoreDate( new Date() );
                            ue.setComputedScore( forceScore );
                            ue.setComputedConfidence( 1 );
                            ue.setScoreStatusTypeId( EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() );
                            
                            hasValidScore = true;
                            machineScore = plagiarized==1 ? 0 : forceScore; // CHG 3/15/2018 ue.getScoreFmErrorRates();

                            if( plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1 )
                                ue.setComputedConfidence( 0 );

                            if( testEventId>0 )
                                discernFacade.saveUnscoredEssay(ue, true);
                            
                            unscoredEssayId = ue.getUnscoredEssayId();                            
                        }

                        else if( !DiscernUtils.isDiscernOn() )
                        {                            
                            ue.setScoreDate( new Date() );
                            ue.setScoreStatusTypeId( EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId() );
                            //hasValidScore = false;
                            machineScore = plagiarized==1 ? 0 : ue.getScoreFmErrorRates();
                            if( plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1 )
                                ue.setComputedConfidence( 0 );
                            discernFacade.saveUnscoredEssay(ue, true);
                            unscoredEssayId = ue.getUnscoredEssayId();                            
                            errMsg = "Discern is currently disabled."; 
                        }

                        else if( !hasValidTextForDiscern )
                        {
                            ue.setScoreDate( new Date() );
                            ue.setScoreStatusTypeId( EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId() );
                            ue.setComputedScore( 0 );
                            ue.setComputedConfidence( 1 );
                            
                            hasValidScore = true;
                            machineScore = plagiarized==1 ? 0 : ue.getScoreFmErrorRates();                            
                            if( plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1 )
                                ue.setComputedConfidence( 0 );
                            discernFacade.saveUnscoredEssay(ue, true);
                            unscoredEssayId = ue.getUnscoredEssayId();                            
                            errMsg = "Invalid text for discern"; 
                        }
                        
                        else if( usesDummyEssayPrompt() )
                        {
                            ue.setScoreDate( new Date() );
                            ue.setScoreStatusTypeId( EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId() );
                            ue.setComputedScore( 0 );
                            ue.setComputedConfidence( 1 );
                            
                            hasValidScore = true;
                            machineScore = plagiarized==1 ? 0 : ue.getScoreFmErrorRates();                            
                            if( plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1 )
                                ue.setComputedConfidence( 0 );
                            discernFacade.saveUnscoredEssay(ue, true);
                            unscoredEssayId = ue.getUnscoredEssayId();                            
                            errMsg = "Dummy Prompt"; 
                        }

                        else
                        {
                            // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Submitting for ext scores." );
                            submitForExternal(ep, ue, essayWithoutQuestion );
                            pendingExternalScores = true;
                        }
                    }
                }
                
                else
                {
                    // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Essay equivalent words (" + equivWords + ") is below minimum words (" + minWords + "). ndSeq=" + nodeSeqId + ", snSeq=" + subnodeSeqId );
                    hasValidScore = false;
                }
            }

            else
            {
                LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Essay Str is empty. ndSeq=" + nodeSeqId + ", snSeq=" + subnodeSeqId );
                hasValidScore = false;
            }

            // LogService.logIt( "ScoredEssayIntnItem.calculate() Finished, machineScore="+ machineScore + ", equivWords=" + equivWords + ", confidence=" + confidence );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "ScoredEssayIntnItem.calculate() " + toString() );
            EmailUtils.getInstance().sendEmailToAdmin( "ScoredEssay Error", "ScoredEssayIntnItem.calculate() " + toString() + ", ERROR: " + e.getMessage() );
            hasValidScore = false;
            errMsg = "Exception thrown during processing: ScoredEssayIntnItem.calculate() " + toString(); 
        }

    }
    
    private void setSpellWordsToIgnore( String inStr )
    {
        if( inStr==null || inStr.isBlank() )
            return;
        
        if( spellWordsToIgnore==null )
            this.spellWordsToIgnore = new ArrayList<>();
        for( String s : inStr.split(",") )
        {
            s = s.trim();
            if( s.isBlank() )
                continue;
            if( !spellWordsToIgnore.contains(s.toLowerCase() ))
                spellWordsToIgnore.add( s.toLowerCase() );
        }
    }
    
    
    private void setHighWordsPerMinute(UnscoredEssay ue)
    {
        if( ue==null || ue.getTotalWords()<=20 || ue.getSecondsToCompose()<=1 )
            return;

        wpm = 60f*((float)ue.getTotalWords())/((float)ue.getSecondsToCompose());
        
        ue.setWpm(wpm);
        
        if( wpm>=MAX_WORDS_PER_MIN_PLAG_CHECK )
        {
            highWpm = wpm;
            ue.setHighWpm(wpm);
        }
    }

    
    private float calculateTranslateCompareScore( UnscoredEssay ue ) 
    {
        if( ue.getTranslateCompareScore()>=0 || this.transCompare==null || this.transCompare.isBlank() || !transCompare.contains(",") )
            return ue.getTranslateCompareScore();
        
        int idx = transCompare.indexOf(",");
        String compLang;
        String compStr;
        float compVal = -1;
        try
        {
            compLang = transCompare.substring(0, idx ).trim();
            if( compLang.isBlank() )
                return -1;
            Locale compLoc = I18nUtils.getLocaleFromCompositeStr(compLang);
            
            compStr = transCompare.substring(idx+1,transCompare.length() ).trim();
            
            if( compStr.isBlank() )
                return -1;
            
            LanguageUtils lu = new LanguageUtils();
            String tt = lu.getTextTranslation( essayStr, txtLocale, compLoc, true );
            
            ue.setTranslatedEssay(tt);
            translatedText = tt;
            
            compVal = TextProcessingUtils.getTextSimilarity(tt, compStr);
            ue.setTranslateCompareScore(compVal);
            
            if( discernFacade == null )
                discernFacade = DiscernFacade.getInstance();            
            discernFacade.saveUnscoredEssay(ue, true);
            
            LogService.logIt( "ScoredEssayIntnItem.calculateTranslateCompareScore() transCompare=" + transCompare + ", translated text=" + tt + ", compValScore=" + compVal );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ScoredEssayIntnItem.calculateTranslateCompareScore() transCompare=" + transCompare );
        }
        return compVal;
    }

    
    private String removeQuestionFromEssayStr()
    {
        if( essayStr==null || essayStr.isBlank() || question==null || question.isBlank() )
            return essayStr;
        String t = essayStr.toLowerCase();
        String q = question.toLowerCase();

        int idx = t.indexOf(q);
        if( idx<0 )
            return essayStr;
        
        LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Question appears in EssayStr. Removing. ndSeq=" + nodeSeqId + ", snSeq=" + subnodeSeqId );
        
        List<Integer> il = new ArrayList<>();
        while( idx>=0 )
        {
            il.add(idx);
            idx = t.indexOf( q, idx + q.length() );
        }
        
        StringBuilder sb = new StringBuilder();
        idx=0;
        for( int x : il )
        {
            if( idx<x )
                sb.append( essayStr.substring(idx, x) );
            idx=x+q.length();
        }
        if( idx<essayStr.length() )
            sb.append( essayStr.substring(idx, essayStr.length() ) );
                
        return sb.toString();
    }

    
    
    

    private boolean eligibleForMachineScoring()
    {
        if( transCompareScore>=0 )
            return false;
        
        Locale loc = getTextLocale();

         if( loc == null ||  loc.getLanguage() == null )
             return true;

        if( loc.getLanguage() != null && loc.getLanguage().equalsIgnoreCase( "en") )
            return true;

        return false;
    }

    /*
    private boolean isLocaleValidForMachineScoring()
    {
        Locale l = getTestLocale();

        String lang = l.getLanguage().toLowerCase();

        for( String lg : ScoredEssayIntnItem.VALID_LANGUAGES )
        {
            if( lang.equals( lg ) )
                return true;
        }

        return false;
    }
    */    
    
    private boolean performWebDupContentCheck()
    {
        return (webPlagCheckOk==1 && getTotalWords()>MIN_WORDS_WEB_PLAG_CHECK) || getTotalWords()>MIN_WORDS_FORCE_WEB_PLAG_CHECK;
    }
    
    private boolean isTextLocaleOKForWebDupContentCheck( Locale loc )
    {
        if( loc==null )
            return false;
                
        String lang = loc.getLanguage().toLowerCase();
        
        if( lang==null || lang.isEmpty() )
            return false;
        
        if( lang.equals( "he"  ) || lang.equals( "ar" ) || lang.equals( "zh" ) || lang.equals( "jp" ))
            return false;
        
        return true;
    }
    
    private Locale getTextLocale()
    {
        if( txtLocale!=null )
            return txtLocale;        
                
        if( simJ==null || simJ.getLang() == null || simJ.getLang().isEmpty() )
            return Locale.US;

        return I18nUtils.getLocaleFromCompositeStr( simJ.getLang() );
    }

    private void submitForExternal( EssayPrompt ep, UnscoredEssay ue, String essayWithoutQuestion ) throws Exception
    {
        if( discernFacade == null )
            discernFacade = DiscernFacade.getInstance();
        
        if( ep == null && !usesDummyEssayPrompt() )
            ep = discernFacade.getEssayPrompt( ue.getEssayPromptId() );

        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() Start prompt =" + ep.toString() );
        if( !DiscernUtils.isDiscernOn() )
        {
            ue.setScoreDate( new Date() );
            ue.setScoreStatusTypeId( EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId() );
        }


        // save to make sure we have it stored.
        discernFacade.saveUnscoredEssay(ue, true);
        unscoredEssayId = ue.getUnscoredEssayId();                            

        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() Saved UnscoredEssay" );
        
        if( !DiscernUtils.isDiscernOn() || usesDummyEssayPrompt() )
            return;

        if( discernUtils == null )
            discernUtils = new DiscernUtils();

        // if not submitted to discern. Need to submit.
        if( ue.getDiscernEssayId() <= 0 )
        {
            // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() ue.getDiscernEssayId() <= 0 " );
            
            int discernEssayId = discernUtils.saveDiscernEssay( ep, "test", essayWithoutQuestion==null ? essayStr : essayWithoutQuestion);

            if( discernEssayId > 0 )
                ue.setDiscernEssayId( discernEssayId );

            else
                throw new Exception( "Error submitting essay to Discern." );

            // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() ZZZ" );
        }

        ue.setScoreStatusTypeId( EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId() );
        discernFacade.saveUnscoredEssay(ue, true);
    }


    private void checkForNewScores( UnscoredEssay ue ) throws Exception
    {
        if( usesDummyEssayPrompt() )
        {
            ue.setScoreDate( new Date() );
            ue.setScoreStatusTypeId( EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId() );
            if( discernFacade == null )
                discernFacade = DiscernFacade.getInstance();
            discernFacade.saveUnscoredEssay(ue, true);
            return;
        }
        
        if( !DiscernUtils.isDiscernOn() )
            return;

        if( discernFacade == null )
            discernFacade = DiscernFacade.getInstance();

        if( discernUtils == null )
            discernUtils = new DiscernUtils();

        discernUtils.checkForNewScore(ue);

        if( ue.getScoreStatusTypeId() == EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() && ue.getTotalWords() > 0 && ue.getComputedScore()>0 )
        {
            float maxScore = 100;

            //float newScore = ue.getComputedScore();

            //float scoreAdj = 0;

            float[] fractErrs = new float[4];
            int totalErrors = 0;

            hasSpellingGrammarStyle = ue.getHasSpellingGrammarStyle()==1;
            // fractErrs[0] = ((float) vals[0])/((float) ue.getTotalWords() );
            // fractErrs[1] = ((float) ue.getSpellingErrors())/((float) ue.getTotalWords() );

            if( hasSpellingGrammarStyle )
            {
                fractErrs[2] = ((float) ue.getSpellingErrors() + ue.getGrammarErrors() + ue.getStyleErrors())/((float) ue.getTotalWords() );

                totalErrors = ue.getSpellingErrors() + ue.getGrammarErrors() + ue.getStyleErrors();

                //fractErrs[3] = ((float) vals[3])/((float) ue.getTotalWords() );

                // Fractional Error Counts
                if( fractErrs[2] >= 0.20f )
                    maxScore = Math.min( 10, maxScore);

                else if( fractErrs[2] >= 0.15f )
                    maxScore = Math.min( 20, maxScore);

                else if( fractErrs[2] >= 0.10f )
                    maxScore = Math.min( 40, maxScore);

                //else if( fractErrs[3] >= 0.10f )
                //    maxScore = Math.min( 15, maxScore);

                // Absolute Error Counts
                if( totalErrors > 20 )
                    maxScore = Math.min( 20, maxScore);

                else if( totalErrors > 15 )
                    maxScore = Math.min( 40, maxScore);
            }
            //else if( fractErrs[2]< 0.15f )
            //{
            //    scoreAdj = -1*totalErrors;
            //}

            float newScore = Math.min( maxScore, ue.getComputedScore() );

            //if( newScore != ue.getComputedScore() )
            //    LogService.logIt( "ScoredEssayIntnItem.checkForNewScores() modified Discern Score of " + ue.getComputedScore() + " to " + newScore + ", total Errors=" + totalErrors + ", Error Fraction=" + fractErrs[2] );

            ue.setComputedScore(newScore);
            //else if( totalErrors > 10 )
            //    maxScore = Math.min( 30, maxScore);

            //else if( totalErrors > 5 )
            //    maxScore = Math.min( 40, maxScore);
        }

        discernFacade.saveUnscoredEssay(ue, true);

        // At this point we don't need to keep the data in the discern database. Because of the way the REST service works, all records are loaded with each new essay, so why bother.
        if( ue.getScoreStatusTypeId() == EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() && ue.getDiscernEssayId()>0 )
        {
            discernFacade.deleteDiscernEssayInfo( ue.getDiscernEssayId() );
        }
    }


    private Object[] getWebDuplicateContentInfo( String essayStr )
    {
        try
        {
            if( essayStr == null || essayStr.length() < 50 )
                return null;
            
            Tracker.addWebDuplicateContentCheck();

            // LogService.logIt( "ScoredEssayIntnItem() Performing Web Plag Check. " + this.toString() );
            
            CopyScapeUtils csu = new CopyScapeUtils();

            return csu.submitEssayForWebDuplicateContentCheck(essayStr, 0);
        }
        catch( STException e )
        {
            LogService.logIt( "ScoredEssayIntnItem() NON-FATAL: " + e.toString()  + ", " + essayStr );
            return null;
        }                
        catch( IOException e )
        {
            LogService.logIt( "ScoredEssayIntnItem() NON-FATAL: " + e.toString()  + ", " + essayStr );
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ScoredEssayIntnItem() " + essayStr );
            return null;
        }
    }

    
    private void recomputeWritingAnalysis(UnscoredEssay ue)
    {
        try
        {
            Object[] d1 = EssayScoringUtils.getWritingAnalysis(essayStr, getTextLocale(), teIpCountry, getWordsToIgnoreLc() );

            if( d1==null || d1[0]==null )
                return;
            
            int[] vals = (int[])d1[0];
            this.spellErrors=vals[1];
            this.otherErrors = vals[2] + vals[3];
            this.pctDupWords = vals[5];
            this.pctDupLongWords = vals[6];
            this.hasSpellingGrammarStyle=vals[7]==1;
            
            misSpells = (Map<String,Integer>) d1[1];

            if( ue!=null )
            {
                ue.setTotalWords( vals[4] );
                ue.setSpellingErrors( vals[1] );
                ue.setGrammarErrors( vals[2] );
                ue.setStyleErrors( vals[3] );
                ue.setPctDuplicateWords( vals[5] );
                ue.setPctDuplicateLongWords( vals[6] );

                ue.setHasSpellingGrammarStyle( vals[7] );
                
                if( discernFacade==null )
                    discernFacade=DiscernFacade.getInstance();
                discernFacade.saveUnscoredEssay(ue, false);
            }            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ScoredEssayIntnItem.recomputeWritingAnalysis() " + essayStr );
        }
        
    }
    
    public boolean getHasSpellingGrammarStyle()
    {
        return hasSpellingGrammarStyle;
    }
    
    public List<String> getWordsToIgnoreLc()
    {
        List<String> out = new ArrayList<>();
        
        if( spellWordsToIgnore!=null )
        {
            for( String s : spellWordsToIgnore )
            {
                if( !s.isBlank() )
                    out.add( s.toLowerCase() );
            }
        }
        
        if( user==null  )
            return out;

        if( user.getFirstName()!=null && !user.getFirstName().isBlank() )
            out.add( user.getFirstName().toLowerCase() );

        if( user.getLastName()!=null && !user.getLastName().isBlank() )
            out.add( user.getLastName().toLowerCase() );

        if( user.getEmail()!=null && !user.getEmail().isBlank() )
            out.add( user.getEmail().toLowerCase() );

        return out;        
    }
    
    
    boolean hasValidTextForDiscern( String essayWithoutQuestion)
    {
        String s = essayWithoutQuestion!=null && !essayWithoutQuestion.isBlank() ? essayWithoutQuestion : essayStr;
        
        return DiscernUtils.hasValidTextForDiscern(s);
    }
    
    

    @Override
    public String toString() {
        return "ScoredEssayIntnItem{" + "machineScore=" + machineScore + ", confidence=" + confidence + ", minWords=" + minWords + ", promptId=" + essayPromptId + ", equivWords=" + equivWords + ", hasValidScore=" + hasValidScore + ", testEventId=" + testEventId + ", nodeSeqId=" + nodeSeqId + ", subnodeSeqId=" + subnodeSeqId + ", pendingExternalScores=" + pendingExternalScores + ", essayStr=" + essayStr + '}';
    }

    public boolean usesDummyEssayPrompt()
    {
        return essayPromptId==DUMMY_ESSAY_PROMPT_ID;
    }

    public boolean getHasValidScore() {
        return hasValidScore;
    }

    public float getMachineScore() {
        return machineScore;
    }

    public float getConfidence() {
        return confidence;
    }

    public boolean isPendingExternalScores() {
        return pendingExternalScores;
    }

    public int getSpellErrors() {
        return spellErrors;
    }

    public int getOtherErrors() {
        return otherErrors;
    }

    public int getPlagiarized() {
        return plagiarized;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public int getUnscoredEssayId() {
        return unscoredEssayId;
    }

    public Map<String,Integer> getMisSpells() {
        return misSpells;
    }

    public float getTransCompareScore() {
        return transCompareScore;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public int getSubnodeSeqId() {
        return subnodeSeqId;
    }

    public int getSpeedError() {
        return speedError;
    }

    public void setSpeedError(int speedError) {
        this.speedError = speedError;
    }

    public float getWpm()
    {
        return wpm;
    }

    public float getHighWpm()
    {
        return highWpm;
    }

}


