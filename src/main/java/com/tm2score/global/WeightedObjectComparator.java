/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.global;

import java.util.Comparator;

/**
 *
 * @author Mike
 */
public class WeightedObjectComparator implements Comparator<WeightedObject>
{
    boolean desc = false;
    boolean useName = false;

    public WeightedObjectComparator( boolean descending, boolean useName )
    {
        this.desc = descending;
        this.useName = useName;
    }


    @Override
    public int compare( WeightedObject o1, WeightedObject o2)
    {
        // name is always in ascending order.
        if( useName || ( o1.getWeightUsed()== o2.getWeightUsed() ) )
            return o1.getName().compareTo(o2.getName() );
        
        if( desc )
            return ( (Float) o2.getWeightUsed() ).compareTo( o1.getWeightUsed() );

        return ( (Float) o1.getWeightUsed() ).compareTo( o2.getWeightUsed() );
    }

}
