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
public class SubscriptionExpireThread implements Runnable {

    ReminderUtils reminderUtils;
    EmailUtils emailUtils;
    
    public SubscriptionExpireThread( ReminderUtils ru, EmailUtils eu )
    {
       this.reminderUtils = ru; 
       this.emailUtils = eu;
    }
    
    
    @Override
    public void run() {


        TestKeyEventSelectionType tkest = RuntimeConstants.getTestKeyEventSelectionType();            
        if( !tkest.getIsAllOrNone() && !tkest.keep(111))
        {
            // LogService.logIt( "SubscriptionExpireThread.run() Since this server is not configured to process test keys ending with 1, subscription expiration messages are off." );
            return;
        }        
        
        //LogService.logIt( "SubscriptionExpireThread.run() Starting"  );
        try
        {
            if( RuntimeConstants.getBooleanValue( "autoOrgSubscriptionExpirationOk" )  && ScoreManager.OK_TO_START_ANY )
            {
                Tracker.addLastSubscriptionExpireBatch();
                
                // LogService.logIt( "ReminderThread.run() Starting Reminder Batch "  );
                
                if( emailUtils==null )
                    emailUtils = EmailUtils.getInstance();
                
                if( reminderUtils == null )
                    reminderUtils = new ReminderUtils( emailUtils );
                
                try
                {
                    reminderUtils.doExpireSubscriptionBatch();
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "SubscriptionExpireThread.run() Error during Subscription Reminder Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "SubscriptionExpireThread.run() Error during Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }

                
                //try
                //{
                //    reminderUtils.doExpireCreditsBatch();
                //}

                //catch( Exception e )
                //{
                //    LogService.logIt(e, "SubscriptionExpireThread.run() Error during Credits Reminder Batch." );
                //    EmailUtils.getInstance().sendEmailToAdmin( "SubscriptionExpireThread.run() Error during Credits Reminder Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                //}

            }
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "SubscriptionExpireThread.run() Uncaught Exception during autobatch." );
            
            if( emailUtils==null )
                emailUtils = EmailUtils.getInstance();
            
           emailUtils.sendEmailToAdmin( "SubscriptionExpireThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }
    }
}
