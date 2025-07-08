/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av.item;

import com.tm2score.av.AvItemScorer;
import com.tm2score.av.BaseAudioSampleAvItemScorer;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class Type101AvUploadItemScorer extends BaseAudioSampleAvItemScorer implements AvItemScorer {
    
    // int avItemTypeId;
    
    public Type101AvUploadItemScorer( int avItemTypeId, Locale locale, String teIpCountry, User user, TestEvent testEvent)
    {
        super(locale, teIpCountry, user, testEvent );
        // this.avItemTypeId=avItemTypeId;
    }
}
