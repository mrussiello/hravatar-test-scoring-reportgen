/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av.item;

import com.tm2score.av.AvItemScorer;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import java.util.Locale;

/**
 *
 * Loose Comparison.
 * 
 * 
 * 
 */
public class Type122AvUploadItemScorer extends Type112AvUploadItemScorer implements AvItemScorer {
    
    public Type122AvUploadItemScorer( int avItemTypeId, Locale locale, String teIpCountry, User user, TestEvent testEvent)
    {
        super(avItemTypeId, locale, teIpCountry, user, testEvent );
        this.isLooseCompare = true;
    }
}
