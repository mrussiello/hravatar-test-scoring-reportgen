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
public class FindlyCompetency implements Comparable<FindlyCompetency> ,Serializable {

    float score;
    String name;
    int testEventScoreTypeId;

    @Override
    public int compareTo(FindlyCompetency o) {
        if( name == null || o.name==null )
            return 0;

        return name.compareTo( o.name );
    }

    @Override
    public String toString() {
        return "FindlyCompetency{" + "score=" + score + ", name=" + name + '}';
    }



}
