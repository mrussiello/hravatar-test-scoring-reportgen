/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.reminder;

import com.tm2score.score.*;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import java.util.Date;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 *
 * @author Mike
 */
// @Singleton
//@LocalBean
// @TransactionAttribute(value=NEVER)   // MJR 14 May, to avoid the transactional nature and expunge
// @TransactionAttribute(value=NOT_SUPPORTED)

// @Singleton
// @Startup
// @TransactionAttribute(value=NEVER)

//@WebServlet(name="startup", loadOnStartup=2)
@WebListener
public class ReminderService implements ServletContextListener {
    
    // private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // private static boolean initComplete = false;
    
   
  // @PostConstruct
  // public void startup()
    
  @Override
  public void contextInitialized(ServletContextEvent evt) 
  {
  
        try
        {
            // if( initComplete )
            //     return;
            
            // initComplete = true;
            
            // LogService.logIt( "ReminderService.contextInitialized() STARTING SETUP  AAAA ");
            (new Thread(new ReminderStarter())).start();                       
            // LogService.logIt( "ReminderService.contextInitialized() COMPLETED SETUP  BBBB ");
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderService.contextInitialized() ");
            EmailUtils.getInstance().sendEmailToAdmin( "ReminderService.contextInitialized() Error during Reminder Setup.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
            
        }
    }
  
  
    @Override
  public void contextDestroyed(ServletContextEvent evt) 
  {
      try
      {
          
        if( ReminderStarter.sched != null )
            ReminderStarter.sched.cancel(false);
        
        if( ReminderStarter.scheduler != null )
            ReminderStarter.scheduler.shutdownNow();
          
        if( AutoScoreStarter.sched != null )
            AutoScoreStarter.sched.cancel(false);
        
        if( AutoScoreStarter.scheduler != null )
            AutoScoreStarter.scheduler.shutdownNow();
      }
      
      catch( Exception e )
      {
          LogService.logIt(e, "AutoScoreService.contextDestroyed() Stopping AutoScoreStarter." );
      }
 }  
    

}
