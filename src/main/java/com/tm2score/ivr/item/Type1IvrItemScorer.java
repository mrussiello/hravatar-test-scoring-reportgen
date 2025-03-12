/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr.item;

import com.tm2score.av.AvItemScoringStatusType;
import com.tm2score.av.AvItemType;
import com.tm2score.av.AvIntnElementType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.service.LogService;
import java.util.List;
import java.util.Locale;
import com.tm2score.av.AvItemScorer;
import com.tm2score.av.BaseAvItemScorer;

/**
 *
 * @author miker_000
 */
public class Type1IvrItemScorer extends BaseAvItemScorer implements AvItemScorer {

    public Type1IvrItemScorer( Locale loc, String teIpCountry) {
        this.locale=loc;
        this.teIpCountry=teIpCountry;
    }
        
    
    /**
     * Type 1 items are scored 0 - 100 where 0=no match to text and 100 perfect match to text.
     * 
     * @param intn
     * @param iir
     * @throws Exception 
     */
    @Override
    public void scoreAvItem( SimJ.Intn intn, AvItemResponse iir ) throws Exception
    {
        float similarity = 0;
        float confidenceFactor = 0;
        float score = 0;

        try
        {
            // Item is unscorable if skipped, invalid, or the Speech2Text had an error.
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
            
            // OK score it!
            else
            {
                float scoreMax=100;
                
                for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
                {
                    if( iitm.getFormat()==G2ChoiceFormatType.SUBMIT.getG2ChoiceFormatTypeId() && iitm.getItemscore()>scoreMax  )
                        scoreMax = iitm.getItemscore();
                }
                
                Speech2TextResult sr = getSpeechToTextResult(iir );

                List<String> inStrList = getSpeechTextVals(sr, true, false );

                List<String> matchStrList = getMatchValuesFmTextScoreParams( intn );

                // if not found in TextScoreParam, include stem1 value for Type 1 and IVRQ for Type 5.
                if( matchStrList.isEmpty() )
                {
                    // Only if nothing found!
                    if( iir.getAvItemTypeId()==AvItemType.TYPE1.getAvItemTypeId() )
                        matchStrList.add( AvIntnElementType.STEM1.getIntnStringElement( intn ) );
                    
                    else if( iir.getAvItemTypeId()==AvItemType.TYPE5.getAvItemTypeId() )
                        matchStrList.add( AvIntnElementType.IVRQ.getIntnStringElement( intn ) );
                    
                    else
                        matchStrList.add( AvIntnElementType.IVRQ.getIntnStringElement( intn ) );
                }

                similarity = IvrItemScoringUtils.getHighestTextSimilarity( inStrList, matchStrList );

                selectedValue = IvrItemScoringUtils.getHighestTextSimilarityString( inStrList, matchStrList, false );

                confidenceFactor = IvrItemScoringUtils.getConfidenceFactor( sr.getHighestConfidence() );

                score = scoreMax*similarity*confidenceFactor;

                // LogService.logIt("Type1IvrItemScorer.scoreIvrItem() avItemResponseId=" + iir.getAvItemResponseId() + "  inStrList.size()=" + inStrList.size() + ", similarity=" + similarity + ", confidenceFactor=" + confidenceFactor + ", score=" + score );

                iir.setConfidence( sr.getHighestConfidence() );
                iir.setSimilarity(similarity);
                iir.setRawScore( similarity*confidenceFactor );
                iir.setScore( score );
                iir.setAssignedPoints( score );
                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED.getScoringStatusTypeId()  );
            }
            
        }
        catch( Exception e )            
        {
            LogService.logIt(e, "Type1IvrItemScorer.scoreIvrItem() " + iir.toString()  + ", similarity=" + similarity + ", confidenceFactor=" + confidenceFactor + ", score=" + score + ", error: " + e.toString() );

            iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() );

            iir.setNotes( "Error: similarity=" + similarity + ", confidenceFactor=" + confidenceFactor + ", score=" + score + ", error: " + e.toString() );
        }
    }
        
    
    @Override
    public boolean isCorrect( AvItemResponse avItemResponse )
    {
        return avItemResponse != null ? isResponseCorrect( avItemResponse.getSimilarity() , IvrItemScoringUtils.getConfidenceFactor( avItemResponse.getConfidence() ) ) : false;
    }
        

}
