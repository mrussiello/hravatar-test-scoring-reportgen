package com.tm2score.faces;

import com.tm2score.global.Constants;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;


@Named
@RequestScoped
public class IdleMonitorUtils
{

    //@Inject 
    //UserBean userBean; 
      
    public int getSessionTimeoutMilliseconds()
    {
        return Constants.IDLE_SESSION_TIMEOUT_MINS*60*1000;
    }
    
    
    /*
    public void onIdle() 
    {
        FacesContext fc = null;
        try
        {
            // LogService.logIt( "IdleMonitorUtils.onIdle() Tm2Score loggedOnAccount=" + (userBean==null ? "NULL" : userBean.getUserLoggedOnAsAdmin()) );
            // if logged on, logout.
            if( userBean!=null && userBean.getUserLoggedOnAsAdmin() )
            {
                UserUtils userUtils = UserUtils.getInstance();                
                userUtils.processUserLogOff();
            }

            fc = FacesContext.getCurrentInstance();            
            fc.getExternalContext().invalidateSession();
            fc.getExternalContext().redirect( "/ts/index.xhtml" ); 
        }
        catch( Exception e )
        {
            if( e instanceof IllegalStateException || e.toString().contains( "Cannot create a session after the response has been committed" ) )
                LogService.logIt( "IdleMonitorUtils.onIdle() ERROR " + e.toString() + ", userloggedonadmin=" + userBean.getUserLoggedOnAsAdmin() );
    
            else
                LogService.logIt( e, "IdleMonitorUtils.onIdle() " + e.toString() + ", userloggedonadmin=" + userBean.getUserLoggedOnAsAdmin() );
        }
    }
    */
    

}
