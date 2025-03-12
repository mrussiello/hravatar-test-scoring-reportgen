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
public class InterviewPriorityComparator implements Comparator<TestEventScore>
{

    @Override
    public int compare(TestEventScore o1, TestEventScore o2)
    {
        if( o2.getScore() != o1.getScore() )
            return new Float( o2.getScore() ).compareTo(o1.getScore() );

         return o1.getName().compareTo(o2.getName() );
    }

}
