/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp;

import com.tm2score.service.LogService;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author miker_000
 */
public class BspReportUtils {
    
    private static Properties bspProperties;
    
    
    public String getKey( String key )
    {
        if( bspProperties == null )
            getProperties();
        
        try
        {
            return bspProperties.getProperty( key, "KEY NOT FOUND" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BspReportUtils.getKey() " + key );
            return null;
        }
    }
    
    public Properties getProperties()
    {
        if( bspProperties== null )
            loadProperties();
        
        
        return bspProperties;
    }
    
    private synchronized void loadProperties()
    {
        try
        {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream("bsp.properties");
            prop.load(in);
            in.close();
            
            bspProperties = prop;
            
            // LogService.logIt( "BspReportUtils.loadProperties() Properties files has " + prop.size() + " keys.");
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BspReportUtils.loadProperties() " );
        }
    }
    
}
