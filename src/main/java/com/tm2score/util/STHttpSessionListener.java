/*
 * Created on Dec 31, 2006
 *
 */
package com.tm2score.util;

import com.tm2score.global.Constants;
import java.io.Serializable;
import java.util.Date;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import jakarta.servlet.annotation.WebListener;


@WebListener
public class STHttpSessionListener implements HttpSessionListener, Serializable
{

    /**
     * sessionId : object[] obj[0] = userId obj[1] = fullname
     *
     */
    private Date startDate = null;

    private boolean initComplete = false;

    public STHttpSessionListener()
    {
        startDate = new Date();
    }

    public void init( ServletContext context )
    {
        if( !initComplete && context != null )
        {
            context.setAttribute( Constants.SYSTEM_SESSION_COUNTER, this );

            initComplete = true;
        }
    }

    @Override
    public void sessionCreated( HttpSessionEvent se )
    {
        if( !initComplete )
        {
            init( se.getSession().getServletContext() );
        }
    }

    @Override
    public void sessionDestroyed( HttpSessionEvent se )
    {
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }




}
