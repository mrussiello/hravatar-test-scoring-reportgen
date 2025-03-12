/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av;

import com.tm2score.entity.event.AvItemResponse;
import java.util.Comparator;

/**
 *
 * @author miker_000
 */
public class AvItemScoreDurationComparator implements Comparator<AvItemResponse>{

    @Override
    public int compare(AvItemResponse o1, AvItemResponse o2) {
        return (new Float(o1.getDuration())).compareTo( o2.getDuration() );
    }
    
    
}
