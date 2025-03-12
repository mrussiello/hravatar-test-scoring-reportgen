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
public class TESNameComparator implements Comparator<TestEventScore>
{
    @Override
    public int compare(TestEventScore a, TestEventScore b)
    {
        if( a.getName() != null && !a.getName().isEmpty() && b.getName() != null && !b.getName().isEmpty() )
            return a.getName().compareTo( b.getName() );

        if( a.getTestEventId() != b.getTestEventId() )
            return ((Long)a.getTestEventId() ).compareTo( b.getTestEventId() );

        if( a.getTestEventScoreTypeId() != b.getTestEventScoreTypeId() )
             ((Integer)a.getTestEventScoreTypeId() ).compareTo( b.getTestEventScoreTypeId() );

        return ((Integer) a.getDisplayOrder() ).compareTo( b.getDisplayOrder() );
    }

}
