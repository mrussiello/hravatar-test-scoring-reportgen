/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.nqesh.fbk;

import com.tm2score.service.LogService;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author miker_000
 */
public class NqeshReportUtils {
    
    private final String bundleName;
    private final String defaultBundleName;
    
    private Properties customProperties;
    
    
    public NqeshReportUtils( String bundleName, String defaultBundleName )
    {
        this.bundleName=bundleName;
        this.defaultBundleName = defaultBundleName;
    }
    
    
    public String getKey( String key )
    {
        if( customProperties == null )
            getProperties();
        
        try
        {
            String s = customProperties.getProperty( key, "KEY NOT FOUND" );
            
            if( s.startsWith( "KEY NOT FOUND") )
                s += " (" + key + ")";
            
            return s;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshReportUtils.getKey() " + key );
            return null;
        }
    }
    
    public Properties getProperties()
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
            
            prop.load(in);
            in.close();
            
            customProperties = prop;
            
            // LogService.logIt( "NqeshReportUtils.loadProperties() Properties files has " + prop.size() + " keys.");
        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshReportUtils.loadProperties() " );
        }
    }
    
}
