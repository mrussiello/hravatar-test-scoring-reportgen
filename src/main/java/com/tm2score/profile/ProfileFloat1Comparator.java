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
public class ProfileFloat1Comparator implements Comparator<Profile> {


    public ProfileFloat1Comparator()
    {
    }


    @Override
    public int compare(Profile a, Profile b)
    {
        return Float.valueOf(a.getFloatParam1()).compareTo( b.getFloatParam1());
    }


}
