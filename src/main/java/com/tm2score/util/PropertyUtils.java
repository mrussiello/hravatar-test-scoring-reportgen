/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

import com.tm2score.global.Constants;
import com.tm2score.global.STException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author miker_000
 */
public class PropertyUtils {
    
    private static Map<String,Set<String>> keyCache = null;


    public static boolean hasKeyForLocale( String key, Locale locale ) throws Exception
    {
        if( locale == null || locale.equals( Locale.US ) )
            return true;
                
        return hasKeyForLocale( key, Constants.DEFAULT_RESOURCE_BUNDLE, locale );
    }
    
    
    
    public static boolean hasKeyForLocale( String key, String bundleNameNoLangStr, Locale locale ) throws Exception
    {
        if( key == null || key.trim().isEmpty() )
            throw new Exception( "key is missing." );
        
        if( locale == null || locale.equals( Locale.US ) )
            return true;

        Set<String> s = getKeysForLocaleProperties( bundleNameNoLangStr, locale );
        
        if( s==null )
            return false;
        
        return s.contains(key);
    }
    
    
    
    private static Set<String> getKeysFmCache( String bundleNameNoLangStr, Locale locale )
    {
        if( keyCache == null )
            return null;
        
        Set<String> s = keyCache.get(bundleNameNoLangStr + "_" + locale.toString() );
        
        if( s!=null )
            return s;
        
        return keyCache.get(bundleNameNoLangStr + "_" + locale.getLanguage() );
    }
    
    private static synchronized void addKeysToCache( String bundleNameWithLangStr,Set<String> keys )
    {
        if( keyCache == null )
            keyCache = new HashMap<>();
        
        keyCache.put(bundleNameWithLangStr , keys );
    }
    
    public static Set<String> getKeysForLocaleProperties( String bundleNameNoLanguage, Locale locale ) throws STException
    {
        if( locale == null )
            locale = Locale.US;
        
        Set<String> s = getKeysFmCache(bundleNameNoLanguage, locale);
        
        if( s!=null )
            return s;
        
        String nm = locale.equals(Locale.US) ? bundleNameNoLanguage : bundleNameNoLanguage + "_" + locale.toString();
        
        Properties p = PropertyLoader.loadProperties( nm );
        
        if( p==null )
        {
            nm = bundleNameNoLanguage + "_" + locale.getLanguage();
            
            p = PropertyLoader.loadProperties( nm );
        }
        
        if( p!=null )
        {
            s = p.stringPropertyNames();
            addKeysToCache( nm, s);
            return s;
        }
        
        return new HashSet<>();
    }
    
    

    public static Set<String> getKeysForProperties( String fullBundleName ) throws STException
    {
        Properties p = PropertyLoader.loadProperties(fullBundleName);
        
        if( p!=null )
            return p.stringPropertyNames();
        
        return new HashSet<String>();
    }
    
    
}
