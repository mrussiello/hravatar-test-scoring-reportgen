/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

import com.tm2score.service.LogService;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 *
 * @author miker_000
 */
public class UrlEncodingUtils {
    
    public static String encode( String inStr )
    {
        if( inStr==null || inStr.isEmpty() )
            return inStr;
        
        try
        {
            return URLEncoder.encode(inStr, "UTF8");
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UrlEncodingUtils.encode() NON-FATAL, Returning original String. inStr=" + inStr );
            return inStr;
        }
    }
    
    public static String decodeKeepPlus( String inStr )
    {
        return decodeKeepPlus(  inStr, "UTF8" );
    }
    

    public static String decodeKeepPlus( String inStr, String enc )
    {
        if( inStr==null || inStr.trim().isEmpty() )
            return inStr;
        
        // presence of any of these indicates that it's not encoded.
        // first one - no valid hex chars after percent. 
        // second one no two hex chars after percent.
        if( inStr.indexOf( "% " )>=0 || inStr.indexOf( "%|" )>=0 || inStr.indexOf( "%;" )>=0 || inStr.lastIndexOf( "%" )>inStr.length()-2 )
            return inStr;
        
        try
        {
            String p = inStr.replaceAll( "\\+" , "%2B");
            
            return URLDecoder.decode(p, enc );
        }
        catch( IllegalArgumentException e )
        {
            LogService.logIt( "UrlEncodingUtils.decode() NON-FATAL, " + e.toString() + ", inStr=" + inStr );
            return inStr;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UrlEncodingUtils.decode() NON-FATAL, Returning original String. inStr=" + inStr );
            return inStr;
        }
    }
    

    
}
