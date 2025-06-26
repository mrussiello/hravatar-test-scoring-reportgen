/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av;

import com.tm2score.av.item.Type101AvUploadItemScorer;
import com.tm2score.av.item.Type102AvUploadItemScorer;
import com.tm2score.av.item.Type112AvUploadItemScorer;
import com.tm2score.av.item.Type122AvUploadItemScorer;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class AvItemScorerFactory {
    
    public static AvItemScorer getAvItemScorer( int ivrItemTypeId, Locale loc, String teIpCountry, User user, TestEvent testEvent) throws Exception
    {
        //if( ivrItemTypeId==1 )
        //    return new Type1IvrItemScorer( loc, teIpCountry );
        
        //if( ivrItemTypeId==2 )
        //    return new Type2IvrItemScorer( loc, teIpCountry );
        
        //if( ivrItemTypeId==3 )
        //    return new Type3IvrItemScorer( loc, teIpCountry, user );
        
        //if( ivrItemTypeId==4 )
        //    return new Type4IvrItemScorer( loc, teIpCountry );

        //if( ivrItemTypeId==5 )
        //    return new Type5IvrItemScorer( loc, teIpCountry );
        
        //if( ivrItemTypeId==6 )
        //    return new Type6IvrItemScorer( loc, teIpCountry );
        
        //if( ivrItemTypeId==7 )
        //    return new Type7IvrItemScorer( loc, teIpCountry );
        
        //if( ivrItemTypeId==8 )
        //    return new Type8IvrItemScorer( loc, teIpCountry );
        
        if( ivrItemTypeId==101 )
            return new Type101AvUploadItemScorer( ivrItemTypeId, loc, teIpCountry, user, testEvent );
        
        if( ivrItemTypeId==102 )
            return new Type102AvUploadItemScorer( ivrItemTypeId, loc, teIpCountry, user, testEvent );
        
        if( ivrItemTypeId==112 )
            return new Type112AvUploadItemScorer( ivrItemTypeId, loc, teIpCountry, user, testEvent );
        
        if( ivrItemTypeId==122 )
            return new Type122AvUploadItemScorer( ivrItemTypeId, loc, teIpCountry, user, testEvent );
        
        throw new Exception( "No valid IvrItemScorer is available for this typeid " + ivrItemTypeId );
    }
}
