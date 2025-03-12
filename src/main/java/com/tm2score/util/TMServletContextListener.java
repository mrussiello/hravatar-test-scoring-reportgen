/*
 * Created on Dec 31, 2006
 *
 */
package com.tm2score.util;

import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import jakarta.servlet.annotation.WebListener;

@WebListener
public class TMServletContextListener implements ServletContextListener
{
    public void contextDestroyed( ServletContextEvent arg0 )
    {}

    public void contextInitialized( ServletContextEvent arg0 )
    {
        try
        {
            ServletContext servletContext = arg0.getServletContext();


            LogService.logIt( Constants.SERVER_START_LOG_MARKER );

            
            servletContext.getSessionCookieConfig().setHttpOnly( true );
            servletContext.getSessionCookieConfig().setSecure( RuntimeConstants.getHttpsOnly() );
            

        }

        catch( Exception e )
        {
            LogService.logIt( e, "TMServletContextListener.contextInitialized() creating SessionListener" );
        }

    }

}
