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
public class TESScoreComparator implements Comparator<TestEventScore>
{
    boolean desc = false;
    
    public TESScoreComparator()
    {}
    
    public TESScoreComparator( boolean descending )
    {
        this.desc = descending;
    }
    
    
    
    @Override
    public int compare(TestEventScore a, TestEventScore b)
    {
        if( desc )    
            return ((Float) b.getScore() ).compareTo( a.getScore() );

        return ((Float) a.getScore() ).compareTo( b.getScore() );
    }

}
