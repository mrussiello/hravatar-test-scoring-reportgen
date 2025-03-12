/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

// import com.tm2score.util.CacheMap2.CacheObject;


/**
 *
 * @author miker_000
 */
public class IpInfoCache {
    
    public static CacheMap2<String,String[]> map = null;
    
    public static String[] getIpInfo( String ipAddress )
    {
        if( map==null )
            return null;
        
        return  map.get( ipAddress );        
    }
    
    public static void putEntry( String ipAddress, String[] ipInfo )
    {
        if( map == null )
            map = new CacheMap2<>( 600, 60, 200 );
        
        if( ipAddress == null || ipAddress.isEmpty() )
            return;
        
        if( ipInfo == null )
            return;
        
        map.put(ipAddress, ipInfo );        
    }
    
}
