/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av.item;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.av.AvItemScorer;
import com.tm2score.av.AvItemScoringStatusType;
import com.tm2score.av.BaseAudioSampleAvItemScorer;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import com.tm2score.global.I18nUtils;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This item expects to have a [POINTS] tag in Text Param1. The format of [POINTS] is text|points|text|points|... 
 * 
 * Strict Comparison (Type 112)
 *  Score is 'points value' if any of the 'correct' text phrases are present non-case sensitive but in the exact order presented.
 * 
 * So for [POINTS]Hello There|2.5|Hi There|2.0 and the transcribed text is Hello There, the score is 2.5. If the transcribed text is Hello, the score is 0.
 * 
 * Loose comparison (Type 122)
 *  Score is the highest fraction of words in a 'correct' text phrase that is present times the points assigned to that correct text phrase. 
 * 
 * So for [POINTS]Hello There|2.5|Hi There|2.0 and the transcribed text is Hello There, the score is 2.5. If the transcribed text is Hello, the score is 0.5 * 2.5 = 1.25.
 * 
 * @author miker_000
 */
public class Type112AvUploadItemScorer extends BaseAudioSampleAvItemScorer implements AvItemScorer {
    
    int avItemTypeId;
    boolean isLooseCompare = false;
    float overallMaxPoints = 0;
    SimJ.Intn intn;
    String textIn;
    
    
    public Type112AvUploadItemScorer( int avItemTypeId, Locale locale, String teIpCountry, User user, TestEvent testEvent)
    {
        super(locale, teIpCountry, user, testEvent );
        this.avItemTypeId=avItemTypeId;
    }
    
    @Override
    public void scoreAvItem( SimJ.Intn intn, AvItemResponse iir ) throws Exception
    {
        if( scoringComplete )
            return;
        
        if( iir==null )
            throw new Exception( "Type112AvUploadItemScorer.scoreIvrItem() avItemResponse is null!");

        if( iir.getMediaLocale()!=null )
            locale =  iir.getMediaLocale();
        
        textAndTitleList=new ArrayList<>();
        
        this.selectedValue=null;
        
        this.intn = intn;
        
        if( iir.getAvItemScoringStatusType().isSkipped() || iir.getAvItemScoringStatusType().isInvalid()  )
        {
            iir.setConfidence( 0 );
            iir.setSimilarity(0);
            iir.setRawScore( 0 );
            iir.setScore( 0 );                
            iir.setAssignedPoints( 0 );

            //if( iir.getSpeechTextStatusType().isError() )
            //    iir.setScoringStatusTypeId( IvrItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId()  );                
            //else
            iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED_SKIPPED.getScoringStatusTypeId()  );
        }

        else if( iir.getAvItemScoringStatusType().isScoreError() )
        {}

        else
        {            
            // if( iir.getAudioBytes()==null || iir.getAudioBytes().length==0 )
            //     iir.setScoringStatusTypeId( IvrItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() ); 
            
            iir.setSimilarity(0);

            

            float compareScore = 0;
            float compareScoreTrans = 0;
            
            // if( iir.getSpeechTextStatusType().isComplete() )
            if( iir.getSpeechTextStatusType().isCompleteOrPermanentError() )   // MJR CHANGED 05052018!!! Was if( iir.getSpeechTextStatusType().isComplete() ). Changed because speechtext errors were preventing video from appearing.
            {
                Speech2TextResult s2tr = getSpeechToTextResult( iir );

                if( s2tr!=null )
                {
                    iir.setConfidence( s2tr.getAvgConfidence() );

                    if( locale == null )
                        locale = Locale.US;

                    textIn = s2tr.getConcatTranscript(0, "" );

                    if( textIn==null )
                        textIn = "";
                             
                    compareScore = computeTextPointsMatch(textIn, intn);   
                    
                    // LogService.logIt( "Type112AvUploadItemScorer.scoreAvItem() isLoose=" + isLooseCompare + ", intn=" + intn.getUniqueid() + ", textIn=" + textIn + ", score=" + compareScore );
                }
                
                if( locale!=null && !locale.getLanguage().toLowerCase().contains("en") && iir.getSpeechTextEnglish()!=null && !iir.getSpeechTextEnglish().isBlank() )
                {
                    compareScoreTrans = computeTextPointsMatch(iir.getSpeechTextEnglish(), intn); 
                    if( compareScoreTrans>0 )
                        compareScore = Math.max(compareScore, compareScoreTrans);
                }

                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED.getScoringStatusTypeId() );
            }
            
            //else if(  iir.getSpeechTextStatusType().isPermanentError() )
            //{
            //    
            //}
            
            else
                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() );
               
            
            iir.setScoreStr( Float.toString( compareScore ) );
            
            iir.setRawScore( compareScore );                        
            iir.setScore( compareScore );
            iir.setAssignedPoints( compareScore );       
        } 
        
        scoringComplete = true;                
    }
    
    
    
    @Override
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId, ScorableResponse sr )
    {
        IncludeItemScoresType iist = IncludeItemScoresType.getValue(includeItemScoreTypeId);
        
        if( iist.isNone() )
            return null;
        
        String title = getTextAndTitleIdentifier( sr, intn );   // UrlEncodingUtils.decodeKeepPlus( sr.getExtItemId() );
        
        //if( title == null || title.isEmpty() )
        //{
        //    title = UrlEncodingUtils.decodeKeepPlus( intn.getUniqueid() );
            
        //    if( title==null || title.isEmpty() )
        //        title = UrlEncodingUtils.decodeKeepPlus(intn.getId());

        //    if( title == null || title.isEmpty() )
        //        title = Integer.toString( intn.getSeq() );
        //}
        
        String text = null;       
        
        if( iist.isIncludeCorrect() )
             text = sr.correct() ? "Correct" : ( getPartialCreditAssigned( sr ) ? "Partial" : "Incorrect" );
        
        else if( iist.isIncludeNumericScore() )
        {
            text = I18nUtils.getFormattedNumber(Locale.US, sr.itemScore(), 1 ); //  Float.toString( itemScore() );
            // text = Float.toString( itemScore() );
        }

        else if( iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore( sr.itemScore() );

        else if( iist.isResponseOrResponseCorrect() )
        {
            text = StringUtils.truncateString( textIn, 40 );
            
            if( iist.isResponseCorrect() )
                text += " (" + (sr.correct() ? "Correct" : (getPartialCreditAssigned( sr ) ? "Partial" : "Incorrect" )) + ")";                   
        }        
        
        
        if( text == null || text.isEmpty() )      
            return null;
        
        return new TextAndTitle( text, title );

    }
    
    public boolean getPartialCreditAssigned( ScorableResponse sr )
    {
        if( sr.getSimletItemType().isDichotomous() )
            return false;
        
        if( sr.getSimletItemType().isPoints() )
            return sr.itemScore()>0 && sr.itemScore()<getMaxPointsArray()[0];
        
        return false;
    }
    
    
    
    
    
    
    @Override
    public float[] getMaxPointsArray()
    {
        float maxPts = 0;
        
        String pointsStr = intn==null ? null : IvrStringUtils.getPointsValueFmTextScoreParam(intn.getTextscoreparam1());
        
        if( pointsStr==null || pointsStr.trim().isEmpty() )
            return new float[4];
        
        List<Object[]> textPointsLst = computeTextPointsLst( pointsStr.trim() );
        
        if( textPointsLst==null || textPointsLst.isEmpty() )
        {
            //LogService.logIt( "Type112AvUploadItemScorer.getMaxPointsArray() "   );
            return new float[4];
        }
        
        for( Object[] o : textPointsLst )
        {
            if( maxPts < ((Float)o[1]) )
                maxPts = ((Float)o[1]);
        }                
        
        return new float[] {maxPts,0,0,0};
    }
    
    
    
    
    @Override
    public boolean getPartialCreditAssigned( AvItemResponse avItemResponse )
    {
        if( isLooseCompare )
            return avItemResponse.getAssignedPoints()>0 && avItemResponse.getAssignedPoints()<overallMaxPoints;
        
        return false;
    }    

    
    @Override
    public boolean isCorrect( AvItemResponse avItemResponse )
    {
        if( isLooseCompare )
            return avItemResponse.getAssignedPoints()>=overallMaxPoints;
        
        return avItemResponse.getAssignedPoints()>0;
    }
    
    
    
    
    
    
    
    
    private float computeTextPointsMatch( String textIn, SimJ.Intn intn )
    {        
        //LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsMatch() AAA.2  intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() );
        
        String pointsStr = IvrStringUtils.getPointsValueFmTextScoreParam(intn.getTextscoreparam1());

        // LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsMatch() AAA.3  pointsStr=" + pointsStr + ", textScoreParam1=" + intn.getTextscoreparam1() );
        
        if( pointsStr==null || pointsStr.trim().isEmpty() )
            return 0;
        
        List<Object[]> textPointsLst = computeTextPointsLst( pointsStr.trim() );
        
        if( textPointsLst==null || textPointsLst.isEmpty() )
        {
            LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsMatch() BBB.1  textPointsList missing. intn=" + this.intn.getUniqueid()+ ", textPointsList=" + (textPointsLst==null ? "null" : textPointsLst.size()) );
            return 0;
        }
        
        // LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsMatch() BBB.2  textPointsList=" + textPointsLst.size() );
                
        if( textIn == null || textIn.isEmpty() )
        {
            LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsMatch() BBB.2A No TextIn. No score. textPointsList=" + textPointsLst.size() );
            return 0;
        }

        // LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsMatch() CCC.1 After compute computeTextPointsLst() textPointsLst.size=" + textPointsLst.size() + ", textIn=" + textIn + ", overallMaxPoints=" + overallMaxPoints );

        // Translation can insert punctuation that we don't need to match on.
        textIn = StringUtils.removePunctuation(textIn );
                
        String val;
        
        Object[] vals;
        
        Float pts = null;
        
        overallMaxPoints = 0;
        float maxPts = 0;
        float fractionPresent;
        
        for( int i=0;i< textPointsLst.size(); i++ )
        {
            vals = textPointsLst.get(i);
            
            val = ((String) vals[0]).trim();
            pts = (Float) vals[1];
            
            if( pts>overallMaxPoints )
                overallMaxPoints = pts.floatValue();

            val = StringUtils.removePunctuation(val );
            
            if( isLooseCompare )
            {
                fractionPresent = getFractionOfWordsPresent(val, textIn );
                
                if( fractionPresent*pts > maxPts )
                    maxPts = fractionPresent*pts;
                
                // LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsMatch() CCC.5 LOOSE Compare. fractionPresent=" + fractionPresent + ", maxPts=" + maxPts );
            }
            
            // Strict compare. Look for val inside textIn.
            else
            {
                
                // phrase is contained. 
                if( textIn.toLowerCase().contains( val.toLowerCase() ) )
                {
                    if( maxPts<pts )
                        maxPts=pts;
                }
            }
        }
        
        return maxPts;
    }
    
    
    private boolean useCharactersInsteadOfWords()
    {
        if( locale!=null )
        {
            String ln = locale.getLanguage();
            if( ln==null || ln.isBlank() )
                return false;
            return ln.equalsIgnoreCase( "zh" ) || ln.equalsIgnoreCase( "ja" ) || ln.equalsIgnoreCase( "ko" );            
        }
        
        return false;
    }
    
    private float getFractionOfWordsPresent( String val, String textIn )
    {
        if( val==null || val.trim().isEmpty() )
            return 0;
        
        if( textIn==null || textIn.isEmpty() )
            return 0;
        
        val=val.trim().toLowerCase();
        
        textIn=textIn.trim().toLowerCase();
        
        // Only for special languages.
        boolean compareChars = useCharactersInsteadOfWords();
        
        String[] vWords = compareChars ? val.split("") : val.split(" ");
        
        float matchCount=0;
        float totalCount=0;
        
        for( String vWd : vWords )
        {
            vWd = vWd.trim();
            
            if( vWd.isEmpty() )
                continue;
            
            totalCount++;
            
            if( textIn.contains(vWd ) )
                matchCount++;
        }

        if( totalCount <=0 )
            return 0;
        
        return matchCount/totalCount;        
    }

    
   /**
    *
    * Object[0] = String value to match
    * Object[1] = Points to assign (Float)
   */
    private List<Object[]> computeTextPointsLst( String pointsStr )
    {
        List<Object[]> out = new ArrayList<>();
        
        if( pointsStr==null || pointsStr.trim().isEmpty() )
            return out;
        
        pointsStr=pointsStr.trim();

        String[] vals = pointsStr.split("\\|");

        String v,p;
        float pts;

        for( int i=0;i<vals.length-1; i+=2 )
        {
           v = vals[i].trim(); 
           p = vals[i+1].trim();

           // LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsLst() BBB Next pair:  v=" + v+ ", p=" + p );
           
           if( v.isEmpty() )
               continue;

           try
           {
               pts = p.isEmpty() ? 0 : Float.parseFloat(p);
           }
           catch( NumberFormatException e )
           {
               LogService.logIt( "Type112AvUploadItemScorer.computeTextPointsLst() Unable to parse points value: " + p + ", TextScoreParam1.Points=" + pointsStr );
               pts = 0;
           }

           out.add( new Object[] {v, (Float)(pts)} );
        } 
        
        return out;
    }
    
    
    
    
}
