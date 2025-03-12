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
public class LastAccessDateComparator implements Comparator<TestEvent> {

    @Override
    public int compare(TestEvent o1, TestEvent o2) {
        
        if( o1.getLastAccessDate() != null && o2.getLastAccessDate() != null )
            return o1.getLastAccessDate().compareTo( o2.getLastAccessDate() );

        if( o1.getStartDate() != null && o2.getStartDate() != null )
            return o1.getStartDate().compareTo( o2.getStartDate() );
        
        return new Long( o1.getTestEventId() ).compareTo( new Long( o2.getTestEventId() ) );
    }
    
}
