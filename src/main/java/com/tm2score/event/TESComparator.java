/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.event;

import com.tm2score.entity.event.TestEventScore;
import java.util.Comparator;

/**
 *
 * @author Mike
 */
public class TESComparator implements Comparator<TestEventScore>
{
    @Override
    public int compare(TestEventScore a, TestEventScore b)
    {
        if( a.getTestEventId() != b.getTestEventId() )
            return new Long( a.getTestEventId() ).compareTo( b.getTestEventId() );

        if( a.getTestEventScoreTypeId() != b.getTestEventScoreTypeId() )
             new Integer( a.getTestEventScoreTypeId() ).compareTo( b.getTestEventScoreTypeId() );

        return new Integer( a.getDisplayOrder() ).compareTo( b.getDisplayOrder() );
    }

}
