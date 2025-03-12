/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr.item;

import com.tm2score.av.AvItemScoringStatusType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.AvItemResponse;
import java.util.Locale;
import com.tm2score.av.AvItemScorer;
import com.tm2score.av.BaseAvItemScorer;

/**
 *
 * @author miker_000
 */
public class Type7IvrItemScorer extends  BaseAvItemScorer implements AvItemScorer {
    
    public Type7IvrItemScorer( Locale loc, String teIpCountry) {
        this.locale=loc;
        this.teIpCountry=teIpCountry;
    }
    
    @Override
    public void scoreAvItem( SimJ.Intn intn, AvItemResponse iir ) throws Exception
    {
        selectedValue=iir.getDtmf();
        iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED.getScoringStatusTypeId()  );
    }
        
    @Override
    public SimJ.Intn.Intnitem getSelectedIntnItem( SimJ.Intn intn, AvItemResponse avItemResponse )
    {
        return null;
    }
        
    @Override
    public boolean isCorrect( AvItemResponse iir )
    {
        return false;
    }
}
