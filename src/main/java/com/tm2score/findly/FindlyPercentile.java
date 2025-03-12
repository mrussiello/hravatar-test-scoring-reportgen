/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.findly;

import java.io.Serializable;

/**
 *
 * @author Mike
 */
public class FindlyPercentile implements Comparable<FindlyPercentile> ,Serializable {

    int scoreCount;
    float percentile;
    String name;

    @Override
    public int compareTo(FindlyPercentile o) {
        if( name == null || o.name==null )
            return 0;

        return name.compareTo( o.name );
    }



}
