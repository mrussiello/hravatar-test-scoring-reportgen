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
public class UserRankComparator implements Comparator<UserRankObject>
{

    @Override
    public int compare(UserRankObject o1, UserRankObject o2)
    {
        return ( (Integer) o1.getUserRank() ).compareTo( o2.getUserRank() );
    }

}
