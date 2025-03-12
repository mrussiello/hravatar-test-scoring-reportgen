/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.metascorer;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.ct5.Ct5ItemPartType;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.ibmcloud.IbmInsightResult;
import com.tm2score.event.TestEventLogUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.ibmcloud.HraTraitPackageType;
import com.tm2score.ibmcloud.IbmCloudFacade;
import com.tm2score.ibmcloud.SentinoUtils;
import com.tm2score.ibmcloud.SentinoResult;
import com.tm2score.ibmcloud.SentinoTraitType;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.iactnresp.IactnItemResp;
import com.tm2score.score.iactnresp.IactnResp;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.iactnresp.ScoredAvIactnResp;
import com.tm2score.score.iactnresp.ScoredEssayIactnResp;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 * payload:
 * 
 * INVENTORIES = ["big5", "cpi", "hexaco"]
INDICES = ["6fpq.adaptability",
           "bisbas.drive",
           "hexaco.fairness",
           "neo.sympathy",
           "ab5c.impulse control",
           "via.citizenship",
           "ab5c.leadership",
           "mpq.social potency",
           "neo.cooperation", "neo.achievement"]
# COLLECT ANSWERs TO THE QUESTIONS:
ITEMS = [
    {"question": "What adjectives best describe you?", "answer": "..."},
    {"question": "How do you handle criticism?", "answer": "..."},
    {"question": "What motivates you in life?", "answer": "..."},
    {"question": "How do you handle situations when you make a mistake.", "answer": "..."},
    {"question": "Do you often feel emotions of others?", "answer": "..."},
    {"question": "What do you like to do in your spare time?", "answer": "..."},
    {"question": "Describe a situation when you couldn't keep your emotions under control", "answer": "..."},
    {"question": "Do you work better in a team or alone", "answer": "..."},
    {"question": "What are your best skills?", "answer": "..."},
    {"question": "Do you like to be in charge?", "answer": "..."},
    {"question": "Do you like making new connections?", "answer": "..."},
    {"question": "Are you competitive by nature?", "answer": "..."},
    {"question": "What are your biggest interests?", "answer": "..."},
 * 
 * @author miker_000
 */
public class IbmInsightMetaScorer extends BaseMetaScorer implements MetaScorer {
    
    public static Integer MIN_WORDS_REQUIRED = null;
    
    List<ScorableResponse> scorableResponseList;

    List<IactnResp> allResponseList;
    
    IbmInsightResult ibmInsightResult;
    
    SentinoResult insightResult;
    
    // 
    // Set<SentinoTraitType> sentinoTraitTypeSet;

    // Set<SentinoGroupType> sentinoGroupTypeSet;
    
    // these are question-value pairs
    List<String[]> textList;
    
    boolean hasEnglishText = false;
    
    int wordCount;
    
    IbmCloudFacade ibmCloudFacade;
    
    HraTraitPackageType hraTraitPackageType;

    Set<Integer> hraTraitTypeIdsToIncludeList;
    
    List<SentinoTraitType> sentinoTraitTypeList;
    
    //boolean performance;
    //boolean culture;
    
    public IbmInsightMetaScorer( TestEvent testEvent, Locale locale, Locale reportLocale, List<ScorableResponse> scoredRespList, List<IactnResp> allResponses )
    {
        
        scorableResponseList = scoredRespList;
        this.allResponseList = allResponses;
        this.testEvent = testEvent;
        this.locale = locale;
        this.reportLocale = reportLocale==null ? locale : reportLocale;
        hraTraitPackageType = HraTraitPackageType.getValue( testEvent.getSimXmlObj()==null ? 0 : testEvent.getSimXmlObj().getCt5Int1() );
        
        LogService.logIt( "IbmInsightMetaScorer() testEventId="  + testEvent.getTestEventId() + ", testEvent.getSimXmlObj().getCt5Int1()=" + (testEvent.getSimXmlObj()==null ? "null" : testEvent.getSimXmlObj().getCt5Int1()) );
        //this.performance=testEvent.getSimXmlObj()==null ? true : (testEvent.getSimXmlObj().getCt5Int1()==2 || testEvent.getSimXmlObj().getCt5Int1()==3);
        //this.culture=testEvent.getSimXmlObj()==null ? true : (testEvent.getSimXmlObj().getCt5Int1()==1 || testEvent.getSimXmlObj().getCt5Int1()==3);
        
                
        if( locale==null || locale.getLanguage().equalsIgnoreCase("en") )
            hasEnglishText = true;
        
        LogService.logIt( "IbmInsightMetaScorer() testEventId=" + testEvent.getTestEventId() + ", locale=" + (locale==null ? "null" : locale.toString()) + ", reportLocale=" + this.reportLocale.toString() + " respList.size=" + scoredRespList.size() + ", all resp list.size=" + allResponses.size() );
    }
    
    private synchronized void init()
    {
        if( MIN_WORDS_REQUIRED!=null )
            return;
        
        // 600
        MIN_WORDS_REQUIRED = RuntimeConstants.getIntValue( "ibmcloudinsight.minWordsRequired" );        
    }
    
    
    @Override
    public String toString()
    {
        return "IbmInsightMetaScorer testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId() );
    }
    
    public static boolean requiresMetaScore( TestEvent te, List<IactnResp> allResponses )
    {
        try
        {
            return te!=null && te.getSimXmlObj()!=null && te.getSimXmlObj().getCt5Int1()>0 && hasEligibleItems( allResponses );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "IbmInsightMetaScorer.requiresMetaScore() " );
        }
        return false;
    }
    
    @Override
    public void calculate() 
    {
        try
        {
            init();
            
            validScore=false;
            wordCount=0;
            
            if( ibmCloudFacade == null )
                ibmCloudFacade = IbmCloudFacade.getInstance();
                        
            ibmInsightResult = ibmCloudFacade.getIbmInsightResultForTestEventId( testEvent.getTestEventId() );
            
            if( ibmInsightResult!=null && ibmInsightResult.getResultStatusTypeId()==1 && RuntimeConstants.getBooleanValue( "ibmcloudinsight.reuseExistingResult" ) )
            {
                LogService.logIt( "IbmInsightMetaScorer.calculate() AAA Re-using existing IbmInsightResult. testEventId=" + testEvent.getTestEventId() ); 
                insightResult = new SentinoResult( false, ibmInsightResult.getResultJson(), hraTraitPackageType.getHraPackageTypeId(), hraTraitTypeIdsToIncludeList);
                wordCount = ibmInsightResult.getWordCount();
                validScore = true;
                return;
            }

            if( !RuntimeConstants.getBooleanValue("ibmcloudInsightOn") )
            {
                LogService.logIt( "IbmInsightMetaScorer.calculate() testEventId=" + testEvent.getTestEventId() + ", IBM Insight (Sentino) is OFF. " );
                return;
            }
                        
            // Collect text
            collectSentinoText();
            
            countWords();
            
            LogService.logIt("IbmInsightMetaScorer.calculate() BBB testEventId=" + testEvent.getTestEventId() + ", Num Text Snippets=" + (textList==null ? 0 : textList.size()) + ", total words=" + wordCount);
            
            //if( wordCount<MIN_WORDS_REQUIRED )
            //{
            //    LogService.logIt( "IbmInsightMetaScorer.calculate() testEventId=" + testEvent.getTestEventId() + ", not enough words present. " );
            //    return;
            //}

            SentinoUtils iiUtils = new SentinoUtils();
            
            Locale sourceLocale = hasEnglishText ? Locale.US : locale;
            
            Object[] data =iiUtils.evaluateTextForProfile(textList, sourceLocale, reportLocale, hraTraitPackageType.getHraPackageTypeId(), hraTraitTypeIdsToIncludeList );
            
            LogService.logIt( "IbmInsightMetaScorer.calculate() CCC testEventId=" + testEvent.getTestEventId()  );
            
            String result = (String) data[0];
                        
            if( !result.equalsIgnoreCase("SUCCESS") )
            {
                String msg = "IbmInsightMetaScorer.calculate() DDD testEventId=" + testEvent.getTestEventId() + ", Call to IBM Cloud returned error: " + ((String) data[1] );
                
                LogService.logIt( msg  );                
                TestEventLogUtils.createTestEventLogEntry( testEvent.getTestEventId(), 0, msg, null, null );   
                validScore=false;
                return;
            }
            
            insightResult = (SentinoResult) data[3];
            
            String respJson = ((String) data[1] );
            
            if( respJson==null || respJson.isEmpty() )
                throw new Exception( "ResponseJson is null or empty." );
            
            if( ibmInsightResult == null )
            {
                ibmInsightResult = new IbmInsightResult();                
                ibmInsightResult.setTestEventId( testEvent.getTestEventId() );
            }

            ibmInsightResult.setLastUpdate( new Date());
            ibmInsightResult.setResultJson(respJson);
            ibmInsightResult.setResultStatusTypeId(1);
            ibmInsightResult.setWordCount(this.wordCount);
            ibmInsightResult.setVersionId( iiUtils.getInsightVersionId() );
            
            ibmCloudFacade.saveIbmInsightResult(ibmInsightResult);
            
            if( insightResult==null )
                throw new Exception( "InsightResult (data[3]) is null" );
                                    
            if( !insightResult.hasValidResults() )
                throw new Exception( "InsightResult does not have valid results." );
            
            validScore=true;
        }
        catch( Exception e )
        {
            String msg = "IbmInsightMetaScorer.calculate() testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId()) + ", " + e.toString();            
            LogService.logIt( e, msg );            
            TestEventLogUtils.createTestEventLogEntry( testEvent.getTestEventId(), 0, msg, null, null );   
            validScore=false;
        }
    }
    
    
    @Override
    public String getMetaScoreContentKey()
    {
        return Constants.IBMINSIGHT;
    }
    
    @Override
    public List<TextAndTitle> getTextAndTitleList() {
        
        if( !validScore )
        {
            List<TextAndTitle> out = new ArrayList<>();
            
            if( wordCount<MIN_WORDS_REQUIRED )
            {
                String lwe = null;
                     
                 if( wordCount>0 )       
                    lwe =MessageFactory.getStringMessage(locale, "g.IbmInsightLowWordsErr", new String[] {Integer.toString(wordCount), Integer.toString(MIN_WORDS_REQUIRED)} );
                 else
                    lwe =MessageFactory.getStringMessage(locale, "g.IbmInsightNoWordsErr", new String[] {Integer.toString(wordCount), Integer.toString(MIN_WORDS_REQUIRED)} );
                     
                TextAndTitle tt = new TextAndTitle(lwe, "IBMLOWWORDSERROR" );
                out.add(tt);
                
            }
            return out;
        }
        
        return insightResult.getPackedScoreTextAndTitleList(reportLocale, 0, 0);
    }
    
    
    
    private synchronized void countWords()
    {
        wordCount = 0;
        
        if( textList==null )
            return;
        
        for( String[] t : textList )
        {
            wordCount += StringUtils.numWords(t[1]);
        }
    }
    
    
    private void collectSentinoText() throws Exception
    {
        textList = new ArrayList<>();
                
        hraTraitTypeIdsToIncludeList = new HashSet<>();
        
        if( scorableResponseList==null )
            return;
        
        String t;
       
        SimJ.Intn intn;
        SimJ.Intn.Intnitem intnItm;
        // SimJ.Intn.Intnitem qIntnItm;
        
        String question;
        String text;
        // StringBuilder answer;
        
        // boolean hasInsight = false;
        
        Speech2TextResult s2tr;
        
        
        for( ScorableResponse sr : scorableResponseList )
        {
            if( sr instanceof ScoredAvIactnResp )
            {
                ScoredAvIactnResp air = (ScoredAvIactnResp) sr;
                
                AvItemResponse ar = air.getAvItemResponse();
                
                if( ar==null )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() No AvItemResponse found for ScoredAvIactnResp" + air.getExtItemId() + ", " + air.toString() + ", ignoring for IbmInsight processing." );
                    continue;
                }
                
                intn = air.getIntnObj();
                
                if( intn==null )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() No Intn found for ScoredAvIactnResp" + air.getExtItemId() + ", " + air.toString() + ", ignoring for IbmInsight processing." );
                    continue;
                }
                                
                // no sentino traits - ignore it.
                if( intn.getCt5Str1()==null || intn.getCt5Str1().isBlank() )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() Intn is not designated for IbmInsight processing. ScoredAvIactnResp" + air.getExtItemId() + ", " + air.toString() + ", ignoring response for IbmInsight processing." );
                    continue;
                }
                
                hraTraitTypeIdsToIncludeList.addAll( StringUtils.getIntList( intn.getCt5Str1()) );
                                
                question = null;
                
                for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
                {
                    if( ii.getCt5Itemparttypeid()==Ct5ItemPartType.QUESTION.getCt5ItemPartTypeId() || ii.getIsquestionstem()==1 )
                    {
                        question = StringUtils.getUrlDecodedValue(ii.getContent());
                        break;
                    }
                }

                if( question==null || question.isBlank() )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() No question found for Intn.uniqueId=" + intn.getUniqueid() + " ScoredAvIactnResp" + air.getExtItemId() + ", " + air.toString() + ", ignoring for IbmInsight processing." );
                    continue;
                }
                
                // answer = new StringBuilder();
                
                if( ar.getSpeechTextEnglish()!=null && !ar.getSpeechTextEnglish().isBlank() )
                {
                    t = ar.getSpeechTextEnglish();
                    if( t!=null && !t.isBlank() )
                    {
                        textList.add( new String[]{question,t.trim()});
                        hasEnglishText = true;                        
                    }
                }

                else
                {
                    t = ar.getSpeechText();
                    if( t!=null && !t.isBlank() )
                    {
                        s2tr = new Speech2TextResult( t );                         
                        textList.add( new String[]{question,s2tr.getConcatTranscript()});
                    }                        
                }
            }
            
            if( sr instanceof ScoredEssayIactnResp )
            {
                ScoredEssayIactnResp ser = (ScoredEssayIactnResp) sr;

                //List<IactnItemResp> irrl = ser.getEssayIntItemList();
                
                intn = ser.getIntnObj();   
                
                if( intn==null )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() No Intn found for ScoredEssayIactnResp" + ser.getExtItemId() + ", " + ser.toString() + ", ignoring for IbmInsight processing." );
                    continue;
                }
                                
                // no sentino traits - ignore it.
                if( intn.getCt5Str1()==null || intn.getCt5Str1().isBlank() )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() Essay Intn is not designated for IbmInsight processing. ignoring response for IbmInsight processing. " + intn.getUniqueid() );
                    continue;
                }
                
                hraTraitTypeIdsToIncludeList.addAll( StringUtils.getIntList( intn.getCt5Str1()) );

                question = null;
                
                for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
                {
                    if( ii.getCt5Itemparttypeid()==Ct5ItemPartType.QUESTION.getCt5ItemPartTypeId() || ii.getIsquestionstem()==1 )
                    {
                        question = StringUtils.getUrlDecodedValue(ii.getContent());
                        break;
                    }
                }

                if( question==null || question.isBlank() )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() No question found for Intn.uniqueId=" + intn.getUniqueid() + " ScoredEssayIactnResp" + ser.getExtItemId() + ", " + ser.toString() + ", ignoring for IbmInsight processing." );
                    continue;
                }
                
                for( IactnItemResp iir : ser.getEssayIntItemList() )
                {
                    text = iir.getRespValue();
                    if( text != null && !text.isBlank() )
                        textList.add( new String[] {question, text.trim()} );                    
                }
            }            
        }
        
        for( IactnResp ir : allResponseList )
        {
            //hasInsight = false;

            if( ir.getNonCompetencyQuestionTypeId() == NonCompetencyItemType.WRITING_SAMPLE.getNonCompetencyItemTypeId() )
            {
                //hasInsight = true;
                
                intn = ir.getIntnObj();
                
                if( intn==null )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() No Intn found for Writing Sample IactnResp " + ir.getExtItemId() + ", " + ir.getSimletNodeUniqueId() + ", ignoring for IbmInsight processing." );
                    continue;
                }

                // no sentino traits - ignore it.
                if( intn.getCt5Str1()==null || intn.getCt5Str1().isBlank() )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() NonCompetency Intn is not designated for IbmInsight processing. ignoring response for IbmInsight processing. " + intn.getUniqueid() );
                    continue;
                }
                
                hraTraitTypeIdsToIncludeList.addAll( StringUtils.getIntList( intn.getCt5Str1()) );
                
                question = null;
                
                for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
                {
                    if( ii.getCt5Itemparttypeid()==Ct5ItemPartType.QUESTION.getCt5ItemPartTypeId() || ii.getIsquestionstem()==1 )
                    {
                        question = StringUtils.getUrlDecodedValue(ii.getContent());
                        break;
                    }
                }

                if( question==null || question.isBlank() )
                {
                    LogService.logIt( "IbmInsightMetaScorer.collectText() No question found for Writing Sample IactnResp " + ir.getExtItemId() + ", " + ir.getSimletNodeUniqueId() + ", ignoring for IbmInsight processing." );
                    continue;
                }
                
                
                for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
                {
                    if( iitm.getFormat()==G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() && ir.getIactnItemRespLst()!=null )
                    {
                        for( IactnItemResp iir : ir.getIactnItemRespLst() )
                        {
                            if( iir.getIntnItemObj().getSeq()==iitm.getSeq() )
                            {
                                text = iir.getRespValue();

                                if( text != null && !text.isBlank() )
                                    textList.add( new String[] {question, text.trim()} );                    
                            }
                        }
                    }
                }
            }
        }
    }
    
    

    private static boolean hasEligibleItems( List<IactnResp> allResponses )
    {
        if( allResponses==null || allResponses.isEmpty() )
            return false;
        
        SimJ.Intn intn;
        
        for( IactnResp sr : allResponses )
        {
            if( sr instanceof ScoredAvIactnResp )
            {
                ScoredAvIactnResp air = (ScoredAvIactnResp) sr;                
                //AvItemResponse ar = air.getAvItemResponse();                
                intn = air.getIntnObj();   

                if( intn!=null && intn.getCt5Str1()!=null && !intn.getCt5Str1().isBlank() )
                    return true;                
            }
            
            if( sr instanceof ScoredEssayIactnResp )
            {
                ScoredEssayIactnResp ser = (ScoredEssayIactnResp) sr;
                // List<IactnItemResp> irrl = ser.getEssayIntItemList();                
                intn = ser.getIntnObj();      
                
                if( intn!=null && intn.getCt5Str1()!=null && !intn.getCt5Str1().isBlank() )
                    return true;                
            }

        }
        
        return false;
    }


    
}
