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
public class DisplayOrderComparator implements Comparator<DisplayOrderObject>
{

    @Override
    public int compare(DisplayOrderObject o1, DisplayOrderObject o2)
    {
        return ( (Integer) o1.getDisplayOrder() ).compareTo( o2.getDisplayOrder() );
    }

}
