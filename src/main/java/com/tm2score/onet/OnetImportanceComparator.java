/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.onet;

import java.util.Comparator;

/**
 *
 * @author Mike
 */
public class OnetImportanceComparator implements Comparator<OnetElement> {


    public OnetImportanceComparator()
    {
    }


    @Override
    public int compare(OnetElement a, OnetElement b)
    {
        return new Float(a.getImportance()).compareTo( b.getImportance());
    }


}
