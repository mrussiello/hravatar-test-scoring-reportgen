/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr.item;

import java.util.Locale;
import com.tm2score.av.AvItemScorer;
import com.tm2score.av.BaseAudioSampleAvItemScorer;
import com.tm2score.entity.user.User;

/**
 *
 * @author miker_000
 */
public class Type3IvrItemScorer extends BaseAudioSampleAvItemScorer implements AvItemScorer {
    
    
    public Type3IvrItemScorer( Locale loc, String teIpCountry, User user) {
        super(loc, teIpCountry, user, null);
    }

}
