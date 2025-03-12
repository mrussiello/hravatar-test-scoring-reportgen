/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp;

import java.util.Comparator;

/**
 *
 * @author miker_000
 */
public class BspCompetencyTypeDOComparator implements Comparator<BspCompetencyType> {

    @Override
    public int compare(BspCompetencyType o1, BspCompetencyType o2) {
        return ((Integer)o1.getDisplayOrder()).compareTo(o2.getDisplayOrder());
    }
    
    
}
