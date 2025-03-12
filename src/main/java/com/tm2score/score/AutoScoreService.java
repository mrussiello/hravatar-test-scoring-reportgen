/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

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
public class AutoScoreService implements ServletContextListener {
    
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
            
            // LogService.logIt( "AutoScoreService.contextInitialized() STARTING SETUP  AAAA ");
            (new Thread(new AutoScoreStarter())).start();                       
            // LogService.logIt( "AutoScoreService.contextInitialized() COMPLETED SETUP  BBBB ");
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "AutoScoreService.contextInitialized() ");
            EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreService.doReportAutoBatch() Error during Score Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
            
        }
    }
  
  
    @Override
  public void contextDestroyed(ServletContextEvent evt) 
  {
      try
      {
          
          
        if( AutoScoreStarter.sched != null )
            AutoScoreStarter.sched.cancel(false);
        
        if( AutoScoreStarter.scheduler != null )
            AutoScoreStarter.scheduler.shutdownNow();
      }
      
      catch( Exception e )
      {
          LogService.logIt(e, "AutoScoreService.coonextDestroyed() Stopping AutoScoreStarter." );
      }
 }  
    

}
