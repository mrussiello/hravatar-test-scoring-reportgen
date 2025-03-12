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
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
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
public class Type6IvrItemScorer extends BaseAvItemScorer implements AvItemScorer {
    
    public Type6IvrItemScorer( Locale loc, String teIpCountry) {
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
                
        selectedValue = iir.getDtmf();

        String a = selectedValue;
                
        float score = 0;

        try
        {
            float points = 0;
            
            if( iir.getAvItemScoringStatusType().isSkipped() || iir.getAvItemScoringStatusType().isInvalid() )
            {
                iir.setScore( 0 );        
                iir.setAssignedPoints( 0 );
                
                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED_SKIPPED.getScoringStatusTypeId()  );
                
                if( iir.getAvItemScoringStatusType().isSkipped() )
                    a = MessageFactory.getStringMessage(locale, "g.ItemSkipped" );

                else if( iir.getAvItemScoringStatusType().isInvalid() )
                    a = MessageFactory.getStringMessage(locale, "g.ItemNotAnswered" );                
            }
            
            else
            {
                String selectedMatch = null;
                
                boolean correct = false;

                String input = IvrStringUtils.getDtmfString( iir.getDtmf() );
                
                if( input==null )
                    input="";
                
                //if( input.endsWith("#") )
                //    input = input.substring(0,input.length()-1);
                
                List<String> matchStrList = getMatchValuesFmTextScoreParams( intn );

                for( String ms : matchStrList )
                {
                    if( ms!=null && !ms.isEmpty() && ms.equalsIgnoreCase(input) )
                    {
                        correct=true;
                        selectedMatch = ms;
                    }
                }
                                
                score = correct ? 1 : 0;

                LogService.logIt( "Type6IvrItemScorer.scoreIvrItem() avItemResponseId=" + iir.getAvItemResponseId() + ", score=" + score + ", correct=" + correct );

                iir.setScore( score );

                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED.getScoringStatusTypeId()  );
                
                points = correct ? intn.getTruescore() : 0;
                
                if( selectedMatch != null )
                {
                    Map<String,Float> ptsMap = getIntnLevelMatchPointsMap( intn );
                    
                    // If there is a PointsMap, use it directly.
                    //if( ptsMap.size()>0 )
                    //    points = 0;
                    
                    Float pf = ptsMap.get( selectedMatch );
                    
                    if( pf==null && selectedValue!=null )
                        pf = ptsMap.get( selectedValue );
                    
                    if( pf !=null )
                    {
                        points = pf.floatValue();
                        // iir.setScore( points );
                    }
                }
            }
            
            iir.setAssignedPoints(points);
        }
        
        catch( Exception e )            
        {
            LogService.logIt(e, "Type6IvrItemScorer.scoreIvrItem() " + iir.toString()  + ", score=" + score + ", error: " + e.toString() );

            iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() );

            iir.setNotes( "Error: score=" + score + ", error: " + e.toString() );
        }
        
        if( intn.getTextscoreparam1() !=null && !intn.getTextscoreparam1().isEmpty() && IvrStringUtils.containsKey("[SHOWQA]", intn.getTextscoreparam1(), true ) )
        {            
            if( q!=null && !q.isEmpty() )
            {
                if( a==null )
                    a = "";
                                
                String idt = this.getTextAndTitleIdentifier(null, intn );
                
                addTextAndTitle(new TextAndTitle( a, q, false, 0, 0, null, idt ) );                
                
                // addTextAndTitle(  new TextAndTitle( a, q ) );
            }
        }
        
        
    }
    
    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle() 
    {
       return true;
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
                
        int textInputTypeId = getSingleTextInputTypeId( intn.getTextscoreparam1(), iir );
        
        if( textInputTypeId<=0 )
            return out;
        
        try
        {
            String ss = iir.getDtmf();
            
            if( ss!=null && !ss.trim().isEmpty() )
                out.put( new Integer(textInputTypeId), ss);
        }
        
        catch ( Exception e )
        {
            LogService.logIt( e, "Type6IvrItemScorer.getTextInputTypeMap() intn.getTextscoreparam1()=" + intn.getTextscoreparam1() );
        }
        
        return out;
    }
    
    
}
