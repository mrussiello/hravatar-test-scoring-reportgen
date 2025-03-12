/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.imo.xml.Clicflic;
import java.util.Locale;

/**
 * Used to store and score item responses when the same interaction is responded to multiple times and the flag in the interaction says
 * that each response should be scored.
 *
 * @author Mike
 */
public class ScoredAvIactnResp extends BaseScoredAvIactnResp implements ScorableResponse
{
    
    public ScoredAvIactnResp( Clicflic.History.Intn intRespObj, SimJ.Intn intn, TestEvent testEvent)  throws Exception
    {
        super(intRespObj,intn, testEvent);        
    }
    
    @Override
    public void initAv(  AvItemResponse iir, Locale locale, TestEvent testEvent) throws Exception
    {
        super.initAv( iir, locale, testEvent);        
    }



}
