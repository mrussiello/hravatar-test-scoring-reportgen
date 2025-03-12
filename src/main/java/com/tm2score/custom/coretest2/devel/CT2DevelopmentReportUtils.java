/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2.devel;

import com.tm2score.service.LogService;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author miker_000
 */
public class CT2DevelopmentReportUtils {
    
    private final String bundleName;
    private final String bundleName2;
    private final String defaultBundleName;
    
    private Properties customProperties;
    private Properties customProperties2;
    
    
    public CT2DevelopmentReportUtils( String bundleName, String bundleName2, String defaultBundleName)
    {
        this.bundleName=bundleName;
        this.bundleName2=bundleName2;
        this.defaultBundleName = defaultBundleName;
    }
    
    
    public String getKey( String key )
    {
        if( customProperties==null )
            getProperties();
        
        try
        {
            String s = customProperties.getProperty( key, "KEY NOT FOUND" );
            
            if( s.startsWith( "KEY NOT FOUND") && customProperties2!=null )
                s = customProperties2.getProperty( key, "KEY NOT FOUND" );
            
            if( s.startsWith( "KEY NOT FOUND") )
                s += " (" + key + ")";
            
            return s;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT2DevelopmentReportUtils.getKey() " + key );
            return null;
        }
    }
    
    public synchronized Properties getProperties()
    {
        if( customProperties== null )
            loadProperties();
        
        
        return customProperties;
    }
    
    private synchronized void loadProperties()
    {
        try
        {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream( bundleName );
            
            if( in==null && defaultBundleName!=null )
                in = getClass().getResourceAsStream( defaultBundleName );
            
            if( in!=null )
            {
                prop.load(in);
                in.close();
            }
            else
                LogService.logIt( "CT2DevelopmentReportUtils.loadProperties() BBB.1 Unable to load properties for Bundle=" + bundleName );
            
            customProperties = prop;
            
            if( bundleName2!=null && !bundleName2.isBlank() )
            {
                prop = new Properties();
                in = getClass().getResourceAsStream( bundleName2 );
            
                if( in!=null )
                {
                    prop.load(in);
                    in.close();
                    customProperties2 = prop;
                }
                else
                    LogService.logIt( "CT2DevelopmentReportUtils.loadProperties() CCC.1 Unable to load properties for Bundle 2 bundleName2=" + bundleName2 );
            }
            
            LogService.logIt( "CT2DevelopmentReportUtils.loadProperties() " + bundleName + ", Properties files has " + customProperties.size() + " keys. " + (customProperties2==null ? "Bundle 2 is null." : " Bundle 2 has " + customProperties2.size() + " keys." ) );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT2DevelopmentReportUtils.loadProperties() " );
        }
    }
    
}
