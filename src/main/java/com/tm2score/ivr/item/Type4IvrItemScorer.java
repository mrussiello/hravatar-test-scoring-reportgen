/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr.item;

import com.tm2score.av.AvItemScoringStatusType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.util.MessageFactory;
import java.util.Locale;
import com.tm2score.av.AvItemScorer;
import com.tm2score.av.BaseAvItemScorer;
import com.tm2score.util.UrlEncodingUtils;

/**
 *
 * @author miker_000
 */
public class Type4IvrItemScorer extends BaseAvItemScorer implements AvItemScorer {
    
    
    public Type4IvrItemScorer( Locale loc, String teIpCountry) {
        this.locale=loc;
        this.teIpCountry=teIpCountry;
    }
    
    
    @Override
    public void scoreAvItem( SimJ.Intn intn, AvItemResponse iir ) throws Exception
    {
        textAndTitleList=null;        
        
        // SimJ.Intn.Intnitem selectedIitm = null;
        SimJ.Intn.Intnitem ivrQIitm = null;
            
        for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
        {
            if( iitm.getContent()!=null && IvrStringUtils.containsKey("[IVRQ]", iitm.getContent(), true ) )
            {
                ivrQIitm = iitm;
                break;
            }
        } 
        
        
        String q = ivrQIitm==null ? null : IvrStringUtils.removeAllBracketsForVoice( UrlEncodingUtils.decodeKeepPlus( ivrQIitm.getContent() ) );
        
        String a = null;
                
        if( iir.getAvItemScoringStatusType().isSkipped() || iir.getAvItemScoringStatusType().isInvalid()  )
        {
            iir.setConfidence( 0 );
            iir.setSimilarity(0);
            iir.setRawScore( 0 );
            iir.setScore( 0 );              
            // points = 0;
            iir.setAssignedPoints( 0 );
            
            iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED_SKIPPED.getScoringStatusTypeId()  );
            
            if( iir.getAvItemScoringStatusType().isSkipped() )
                a = MessageFactory.getStringMessage(locale, "g.ItemSkipped" );

            else if( iir.getAvItemScoringStatusType().isInvalid() )
                a = MessageFactory.getStringMessage(locale, "g.ItemNotAnswered" );
        }

        else
        {
            boolean correct = false;
            
            int stemCount = 0;
            
            float points = 0;
            
            for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
            {
                //if( iitm.getContent()!=null && iitm.getContent().contains( "[IVRQ]" ) )
                //    ivrQIitm = iitm;
                if( iitm.getContent()!=null && IvrStringUtils.containsKey("[STEM", iitm.getContent(), true ) )
                    stemCount++;
                
                if( iitm.getSeq()==iir.getSelectedSubnodeSeq() )
                {
                    selectedValue= iitm.getContent();
                    // selectedIitm = iitm;

                    // item was
                    if( iitm.getIscorrect()==1 )
                    {
                        correct = true;
                        points = iitm.getItemscore();
                    } 
                    
                    // iir.setAssignedPoints( iitm.getItemscore() );
                    // points = iitm.getItemscore();
                }
            }
            
            if( !correct && iir.getDtmf()!=null && !iir.getDtmf().isEmpty()  )
            {
                boolean hasCorrectNoneResp = intn.getTextscoreparam1()!=null && IvrStringUtils.containsKey("[NONERESPCORRECT]", intn.getTextscoreparam1(), true );
                        
                if( hasCorrectNoneResp )
                {
                    int noneDtmf = stemCount + 1;
                    
                    int singleDtmf = IvrStringUtils.getSingleDtmf( iir.getDtmf() );
                    
                    if( singleDtmf == noneDtmf )
                    {
                        correct = true;
                        points = intn.getTruescore();
                    }
                }
            }            

            iir.setConfidence(0);
            iir.setSimilarity(0);
            iir.setRawScore( correct ? 1 : 0 );
            iir.setScore( correct ? 1 : 0 );
            iir.setAssignedPoints( points );
                        
            iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED.getScoringStatusTypeId() );
            
            if( selectedValue == null )
                a = "";
            
            else
                a = IvrStringUtils.removeAllBracketsForVoice( UrlEncodingUtils.decodeKeepPlus( selectedValue ) );                        
        }
        
        if( intn.getTextscoreparam1() !=null && !intn.getTextscoreparam1().isEmpty() && IvrStringUtils.containsKey("[SHOWQA]", intn.getTextscoreparam1(), true ) )
        {
            
            if( q!=null && !q.isEmpty() )
            {
                if( a==null )
                    a = "";
                
                String idt = this.getTextAndTitleIdentifier(null, intn );
                
                addTextAndTitle(new TextAndTitle( a, q, false, 0, 0, null, idt ) );
            }
        }
    }

    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle() 
    {
       return true;
    }
        
    
    
    @Override
    public boolean isCorrect( AvItemResponse avItemResponse )
    {
        return avItemResponse != null && avItemResponse.getScore()==1;
    }
        
        
}
