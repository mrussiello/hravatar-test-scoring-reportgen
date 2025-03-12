/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.profile;

import com.tm2score.entity.profile.Profile;
import java.util.Comparator;

/**
 *
 * @author Mike
 */
public class ProfileStrParam1Comparator implements Comparator<Profile> {


    public ProfileStrParam1Comparator()
    {
    }


    @Override
    public int compare(Profile a, Profile b)
    {
        if( a.getStrParam1()!=null && b.getStrParam1()!=null )
            return a.getStrParam1().compareTo(b.getStrParam1());
        
        return new Integer(a.getProfileId()).compareTo( b.getProfileId());
    }


}
