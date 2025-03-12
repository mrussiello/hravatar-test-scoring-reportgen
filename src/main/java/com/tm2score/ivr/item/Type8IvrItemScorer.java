/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr.item;

import java.util.Locale;
import com.tm2score.av.AvItemScorer;

/**
 *
 * @author miker_000
 */
public class Type8IvrItemScorer extends Type6IvrItemScorer implements AvItemScorer {
    
    public Type8IvrItemScorer( Locale loc, String teIpCountry) {
        super(loc, teIpCountry );
    }
        
}
