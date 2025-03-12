/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.fastlog.service;

/**
 *
 * @author miker_000
 */
public class FastSvcTestEventScorer extends com.tm2score.score.scorer.ItssTestEventScorer {
    
    public FastSvcTestEventScorer()
    {
        super();
        this.itss= new FastSvcData();
    }
}
