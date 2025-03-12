/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.metrobank.moss;

import com.tm2score.custom.hraph.bsp.itss.*;

/**
 *
 * @author miker_000
 */
public class MossTestEventScorer extends com.tm2score.score.scorer.ItssTestEventScorer {
    
    public MossTestEventScorer()
    {
        super();
        this.itss= new MossData();
    }
}
