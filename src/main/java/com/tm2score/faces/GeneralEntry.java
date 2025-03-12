/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.faces;

import com.tm2score.service.LogService;
import com.tm2score.user.LogoffType;
import com.tm2score.user.UserBean;
import com.tm2score.user.UserUtils;
import java.io.Serializable;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;

/**
 *
 * @author Dad
 */
// @ManagedBean
@Named
@RequestScoped
public class GeneralEntry implements Serializable
{
    @Inject 
    UserBean userBean;
    
     
    public void doIdleReset( ComponentSystemEvent ev )
    {
        try
        {
            FacesContext fc = FacesContext.getCurrentInstance();
            
            // If there is someone logged on, log them off (will redirect t
            if( userBean.getUserLoggedOnAsAdmin())
            {
                UserUtils userUtils = UserUtils.getInstance();                
                userUtils.processLogOff(LogoffType.USER.getLogoffTypeId());
            }

            try
            {
                fc.getExternalContext().invalidateSession();
            }
            catch(IllegalStateException e )
            {
                LogService.logIt( "GeneralEntry.doLogonReset() " + e.toString() + " while invalidating session.");
            }

            fc.getExternalContext().redirect( "/ts/index.xhtml" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "GeneralEntry.doLogonReset() " );
        }
    }
     

}