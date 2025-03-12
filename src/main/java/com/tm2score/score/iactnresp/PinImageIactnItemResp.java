/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.score.SimletScore;
import com.tm2score.score.item.PinImageItem;
import com.tm2score.service.LogService;

/**
 *
 * @author miker_000
 */
public class PinImageIactnItemResp extends IactnItemResp 
{
    PinImageItem pinImageItem = null;
    
    public PinImageIactnItemResp( IactnResp ir, SimJ.Intn.Intnitem ii, Clicflic.History.Intn iro)
    {
        super(ir, ii, iro, null);        
    }
    
    
    
    @Override
    public void init( SimletScore ss, TestEvent te )
    {
        try
        {
            g2ChoiceFormatType = G2ChoiceFormatType.getValue(  intnItemObj.getFormat()  );

            if( intnItemObj.getCompetencyscoreid() > 0 && g2ChoiceFormatType.supportsSubnodeLevelSimletAutoScoring() )
                simletCompetencyScore = ss.getSimletCompetencyScore( intnItemObj.getCompetencyscoreid() );

            String respStr = this.getRespValue();
            String keyStr = this.intnItemObj.getTextscoreparam1();

            pinImageItem = new PinImageItem( keyStr, respStr );

        }
        catch( Exception e )
        {
            LogService.logIt( e, "PinImageIactnItemResp.init() " + toString() );
        }
    }
    
    @Override
    public void calculateScore() throws Exception
    {
        pinImageItem.calculate();
    }    
    
    @Override
    public String toString() {
        return "PinImageIactnItemResp{ iactn=" + (iactnResp==null || iactnResp.intnObj==null ? "iactnResp.intnObj is null" : iactnResp.intnObj.getName()) +
                " ("  +  (iactnResp.intnObj != null ? iactnResp.intnObj.getSeq() : "" ) + "-" + ( intnItemObj==null ? "intnItemObj is null" : intnItemObj.getSeq()) +  "), content=" + (intnItemObj==null ? "" : intnItemObj.getContent()) +
                " simletCompetencyScore.name=" + (simletCompetencyScore==null ? "simletCompetencyScore is null" : simletCompetencyScore.competencyScoreObj.getName()) + ", maxPoints[0]=" + (getMaxPointsArray() == null ? "null" : Float.toString( getMaxPointsArray()[0])) + '}';
    }    
    
    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return true;
    }

    @Override
    public boolean correct()
    {
        return pinImageItem!=null && pinImageItem.getHasValidScore() && pinImageItem.getIsCorrect();
    }

    @Override
    public String getCorrectValue()
    {
        return "Polygon";
    }

    @Override
    public boolean hasCorrectRespForSubnodeDichotomousScoring()
    {
        String cv = intnItemObj.getTextscoreparam1();

        return cv != null && !cv.isEmpty();
    }

    @Override
    public float itemScore()
    {
        return this.pinImageItem.getIsCorrect() ? intnItemObj.getItemscore() : 0;
    }
    
    @Override
    public float getResponseTime()
    {
        if( pinImageItem!=null && pinImageItem.getHasValidScore() )
            return pinImageItem.getResponseTime();
        
        return iactnResp == null ? 0 : iactnResp.getResponseTime();
    }



    @Override
    public void populateItemResponse( ItemResponse ir )
    {
        super.populateItemResponse(ir);
        
        ir.setResponseTime( getResponseTime() );
    }
}
