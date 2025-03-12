/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr.item;

import com.tm2score.av.AvItemScoringStatusType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.service.LogService;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.tm2score.av.AvItemScorer;
import com.tm2score.av.BaseAvItemScorer;
import com.tm2score.util.UrlEncodingUtils;

/**
 *
 * @author miker_000
 */
public class Type2IvrItemScorer extends BaseAvItemScorer implements AvItemScorer {
        
    public Type2IvrItemScorer( Locale loc, String teIpCountry) {
        this.locale=loc;
        this.teIpCountry=teIpCountry;
    }

    @Override
    public void scoreAvItem( SimJ.Intn intn, AvItemResponse iir ) throws Exception
    {
        float similarity = 0;
        float confidence = 0;
        float confidenceFactor = 0;
        float score = 0;

        try
        {
            if( iir.getAvItemScoringStatusType().isSkipped() || iir.getAvItemScoringStatusType().isInvalid() || iir.getSpeechTextStatusType().isAnyError() )
            {
                iir.setConfidence( 0 );
                iir.setSimilarity(0);
                iir.setRawScore( 0 );
                iir.setScore( 0 );        
                iir.setAssignedPoints( 0 );
                
                if( iir.getSpeechTextStatusType().isAnyError() )
                    iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId()  );                
                else
                    iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED_SKIPPED.getScoringStatusTypeId()  );
            }
            
            else
            {
                boolean correct; 
                float points=0;

                Speech2TextResult sr = getSpeechToTextResult(iir );

                List<String> inStrList = getSpeechTextVals(sr, false, true );

                List<String> matchStrList = getMatchValuesFmTextScoreParams( intn );

                boolean hasAnyMatchStr = IvrItemScoringUtils.containsAnyMatchStr( inStrList, matchStrList );
                
                // Has a direct match. 
                if( hasAnyMatchStr )
                {
                    correct=true;
                    similarity=1;
                    confidence=1;
                    confidenceFactor=1;
                    selectedValue = IvrItemScoringUtils.getMatchedStr( inStrList, matchStrList, false );
                }
                
                // Partial Matches.
                else
                {
                    similarity = IvrItemScoringUtils.getHighestTextSimilarity( inStrList, matchStrList );

                    confidence = sr.getLastConfidence();

                    selectedValue = IvrItemScoringUtils.getHighestTextSimilarityString( inStrList, matchStrList, false );
                    
                    if( similarity <= 0.2f )
                    {
                        inStrList = getSpeechTextVals(sr, false, false );

                        float similarity2 = IvrItemScoringUtils.getHighestTextSimilarity( inStrList, matchStrList );

                        if( similarity2 > similarity )
                        {
                            float confidence2 = sr.getHighestConfidence();
                            LogService.logIt( "Type2IvrItemScorer.scoreIvrItem() " + iir.toString() + ", Higher Similarity from NOT USING LAST INPUT similarity=" + similarity + ", similarity2=" + similarity2 + ", confidence=" + confidence + ", confidence2=" + confidence2 );
                            similarity = similarity2;                        
                            confidence = confidence2;                            
                            selectedValue = IvrItemScoringUtils.getMatchedStr( inStrList, matchStrList, false );
                            // selectedValue = sr.getHighestConfidenceEntry();
                        }
                    }

                    confidenceFactor = IvrItemScoringUtils.getConfidenceFactor( confidence );

                    correct = isResponseCorrect( similarity , confidenceFactor );
                    
                    if( correct )
                        points = intn.getTruescore();
                }
                
                // if has a match, correct points here.
                if( hasAnyMatchStr )
                {
                    Map<String,Float> ptsMap = getIntnLevelMatchPointsMap( intn );
                                        
                    Float pf = ptsMap.get( selectedValue );
                    
                    if( pf !=null )
                        points = pf.floatValue();
                }
                
                score = correct ? 1 : 0;

                // LogService.logIt( "Type2IvrItemScorer.scoreIvrItem() avItemResponseId=" + iir.getAvItemResponseId() + "  inStrList.size()=" + inStrList.size() + ", similarity=" + similarity + ", confidenceFactor=" + confidenceFactor + ", score=" + score + ", correct=" + correct );

                iir.setConfidence( confidence );
                iir.setSimilarity(similarity);
                iir.setRawScore( confidenceFactor*similarity );
                iir.setScore( score );
                iir.setAssignedPoints( points );

                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED.getScoringStatusTypeId()  );
            }
        }
        catch( Exception e )            
        {
            LogService.logIt(e, "Type2IvrItemScorer.scoreIvrItem() " + iir.toString()  + ", similarity=" + similarity + ", confidenceFactor=" + confidenceFactor + ", score=" + score + ", error: " + e.toString() );

            iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() );

            iir.setNotes( "Error: similarity=" + similarity + ", confidenceFactor=" + confidenceFactor + ", score=" + score + ", error: " + e.toString() );
        }

    }
        
    @Override
    public SimJ.Intn.Intnitem getSelectedIntnItem( SimJ.Intn intn, AvItemResponse avItemResponse )
    {
        return null;
    }
        
    @Override
    public boolean isCorrect( AvItemResponse avItemResponse )
    {
        return avItemResponse != null && avItemResponse.getScore()==1;
    }

    @Override
    public String getSelectedValueForItemResponse( SimJ.Intn intn, AvItemResponse avItemResponse )
    {
        return null;
    }
    
    @Override
    public Map<Integer,String> getTextInputTypeMap( SimJ.Intn intn, AvItemResponse iir )
    {
        Map<Integer,String> out = new HashMap<>();
        
        if( intn==null || 
            intn.getTextscoreparam1()==null || 
            intn.getTextscoreparam1().trim().isEmpty() || 
            iir==null ||
            iir.getAvItemScoringStatusType().isSkipped() || 
            iir.getAvItemScoringStatusType().isInvalid() || 
            iir.getSpeechTextStatusType().isAnyError() )
            return out;
                
        int textInputTypeId = getSingleTextInputTypeId( UrlEncodingUtils.decodeKeepPlus( intn.getTextscoreparam1() ), iir );
        
        if( textInputTypeId<=0 )
            return out;
        
        try
        {
            Speech2TextResult sr = getSpeechToTextResult( iir );

            String ss = sr.getHighestConfidenceEntry();
            
            if( ss!=null && !ss.trim().isEmpty() )
            {
                out.put( new Integer(textInputTypeId), ss);
            }
        }
        
        catch ( Exception e )
        {
            LogService.logIt( e, "Type2IvrItemScorer.getTextInputTypeMap() intn.getTextscoreparam1()=" + intn.getTextscoreparam1() );
        }
        
        return out;
    }
    
    
    
    
    
}
