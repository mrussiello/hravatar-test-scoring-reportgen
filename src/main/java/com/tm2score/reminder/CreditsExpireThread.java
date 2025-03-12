/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.reminder;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.TestKeyEventSelectionType;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import java.util.Date;

/**
 *
 * @author Mike
 */
public class CreditsExpireThread implements Runnable {

    ReminderUtils reminderUtils;
    EmailUtils emailUtils;
    
    public CreditsExpireThread( ReminderUtils ru, EmailUtils eu )
    {
       this.reminderUtils = ru; 
       this.emailUtils = eu;
    }
    
    
    @Override
    public void run() {

        TestKeyEventSelectionType tkest = RuntimeConstants.getTestKeyEventSelectionType();            
        if( !tkest.getIsAllOrNone() && !tkest.keep(111))
        {
            LogService.logIt( "CreditsExpireThread.run()  Since this server is not configured to process test keys ending with 1, credit expiration messages are off." );
            return;
            
        }        
        LogService.logIt( "CreditsExpireThread.run() Starting"  );

        try
        {
            if( RuntimeConstants.getBooleanValue( "autoOrgCreditsExpirationOk" )  && ScoreManager.OK_TO_START_ANY )
            {
                Tracker.addLastCreditsExpireBatch();
                
                // LogService.logIt( "CreditsExpireThread.run() Starting Batch "  );
                
                if( emailUtils==null )
                    emailUtils = EmailUtils.getInstance();
                
                if( reminderUtils == null )
                    reminderUtils = new ReminderUtils( emailUtils );
                
                try
                {
                    reminderUtils.doExpireCreditsBatch();
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "CreditsExpireThread.run() Error during Credits Expire Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "CreditsExpireThread.run() Error during Credits Expire Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }

                try
                {
                    reminderUtils.doCreditZeroBatch();
                }
                catch( Exception e )
                {
                    LogService.logIt(e, "CreditsExpireThread.run() Error during doCreditZeroBatch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "CreditsExpireThread.run() Error during doCreditZeroBatch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }
                
            }
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "CreditsExpireThread.run() Uncaught Exception during autobatch." );
            
            if( emailUtils==null )
                emailUtils = EmailUtils.getInstance();
            
           emailUtils.sendEmailToAdmin( "CreditsExpireThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }
    }
}
