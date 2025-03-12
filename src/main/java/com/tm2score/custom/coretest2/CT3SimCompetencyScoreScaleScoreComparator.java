/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.tm2score.score.simcompetency.SimCompetencyScore;
import java.util.Comparator;

/**
 *
 * @author Mike
 */
public class CT3SimCompetencyScoreScaleScoreComparator implements Comparator<SimCompetencyScore> {

    @Override
    public int compare(SimCompetencyScore o1, SimCompetencyScore o2) {

        return new Float( o1.getUnweightedScaledScore( false ) ).compareTo( new Float(o2.getUnweightedScaledScore( false )) );

    }



}
