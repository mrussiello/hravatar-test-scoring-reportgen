/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2score.service.LogService;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author miker_000
 */
public class UMinnReportUtils {
    
    private static Properties uminnProperties;
    
    
    public String getKey( String key )
    {
        if( uminnProperties == null )
            getProperties();
        
        try
        {
            return uminnProperties.getProperty(key, "KEY NOT FOUND" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UminnReportUtils.getKey() " + key );
            return null;
        }
    }
    
    public Properties getProperties()
    {
        if( uminnProperties== null )
            loadProperties();
        
        
        return uminnProperties;
    }
    
    private synchronized void loadProperties()
    {
        try
        {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream("uminn.properties");
            prop.load(in);
            in.close();
            
            uminnProperties = prop;
            
            // LogService.logIt( "UminnReportUtils.loadProperties() Properties files has " + prop.size() + " keys.");
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UminnReportUtils.loadProperties() " );
        }
    }
    
}
