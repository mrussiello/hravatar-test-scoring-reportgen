/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.event;

import com.tm2score.entity.event.TestEvent;
import java.util.Comparator;

/**
 *
 * @author miker_000
 */
public class TestEventProductInParam4Comparator implements Comparator<TestEvent> {

    @Override
    public int compare(TestEvent o1, TestEvent o2) {
        
        if( o1.getProduct()!=null && o2.getProduct()!=null )
            return new Integer(o1.getProduct().getIntParam4()).compareTo( new Integer(o2.getProduct().getIntParam4()));
                   
        return o1.compareTo(o2);
    }
    
}
