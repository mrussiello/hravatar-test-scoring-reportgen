/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class IvrIactnResp extends BaseScoredAvIactnResp implements ScorableResponse {
    

    public IvrIactnResp( SimJ.Intn intn, AvItemResponse iir, Locale locale, TestEvent testEvent) throws Exception
    {
        super(intn, iir, locale, testEvent);        
    }

    
}
