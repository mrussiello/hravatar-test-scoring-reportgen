/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class ZeroPointsTestEventScorer extends StandardTestEventScorer implements TestEventScorer {


    public ZeroPointsTestEventScorer()
    {
        super();

        setValidItemsCanHaveZeroMaxPoints(true);
        
        LogService.logIt( "ZeroPointsTestEventScorer() Constructor. com.tm2score.score.scorer.ZeroPointsTestEventScorer" );
    }

    @Override
    public String toString()
    {
        return "ZeroPointsTestEventScorer() ";
    }

}
