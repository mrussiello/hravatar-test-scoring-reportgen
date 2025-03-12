/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.faces;

import com.tm2score.global.STException;
import com.tm2score.util.MessageFactory;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author Mike
 */
public class FacesUtils {


    public void setInfoMessage( String key, Object[] params )
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // create a FacesMessage
        FacesMessage fm = MessageFactory.getMessage( key, params );

        fm.setSeverity( FacesMessage.SEVERITY_INFO );

        // place in FacesContext
        facesContext.addMessage( null, fm );
    }

    public void setStringInfoMessage( String message )
    {
        String[] params;

        params = new String[1];

        params[0] = message;

        setInfoMessage( "g.PassThru", params );
    }


    public void setStringErrorMessage( String message )
    {
        setErrorMessage( "g.PassThru", new String[] {message} );
    }


    public void setErrorMessage( String key, Object[] params )
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // create a FacesMessage
        FacesMessage fm = MessageFactory.getMessage( key, params );

        fm.setSeverity( FacesMessage.SEVERITY_ERROR );

        // place in FacesContext
        facesContext.addMessage( null, fm );
    }


    public void setMessage( Exception e )
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // create a FacesMessage
        FacesMessage fm;

        if( e instanceof STException )
        {
            fm = MessageFactory.getMessage( ( (STException) e ).getKey(), ( (STException) e ).getParams() );
        }

        else
        {
            Object[] params = new Object[1];

            params[0] = e.toString();

            // create a FacesMessage
            fm = MessageFactory.getMessage( "g.SystemError", params );
        }

        fm.setSeverity( FacesMessage.SEVERITY_ERROR );

        // place in FacesContext
        facesContext.addMessage( null, fm );
    }




}
