/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.custom.disc;

import java.util.Comparator;


/**
 *
 * @author miker
 */
public class DiscScoreNameValueComparator implements Comparator<Object[]> {

    @Override
    public int compare(Object[] o1, Object[] o2) 
    {
        return ((Float)o1[1]).compareTo((Float)o2[1]);
    }
    
}
