/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.entity.event.TestEventScore;
import java.util.Comparator;

/**
 *
 * @author Mike
 */
public class TestEventScoreWeightNameComparator implements Comparator<TestEventScore>
{

    @Override
    public int compare(TestEventScore a, TestEventScore b)
    {
        // compare weights in REVERSE order if different
        if( ( a.getWeight()>0 || b.getWeight()>0) && a.getWeight()!=b.getWeight() )
            return new Float( b.getWeight() ).compareTo( a.getWeight() );

        // compare names in NAME order.
        if( a.getName() != null && b.getName() != null )
            return a.getName().compareTo( b.getName() );

        return a.compareTo(b);
    }

}
