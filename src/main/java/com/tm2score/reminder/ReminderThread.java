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
public class ReminderThread implements Runnable {

    ReminderUtils reminderUtils;
    EmailUtils emailUtils;
    
    public ReminderThread( ReminderUtils ru, EmailUtils eu )
    {
       this.reminderUtils = ru; 
       this.emailUtils = eu;
    }
    
    
    @Override
    public void run() {

        // LogService.logIt( "ReminderThread.run() Starting"  );

        try
        {
            TestKeyEventSelectionType tkest = RuntimeConstants.getTestKeyEventSelectionType();            
            if( !tkest.getIsAllOrNone() && !tkest.keep(111))
            {
                LogService.logIt( "ReminderThread.run() Since this server is not configured to process test keys ending with 1, reminders are off." );
                return;
            }

            if( RuntimeConstants.getBooleanValue( "autoInvitationsOk" ) && ScoreManager.OK_TO_START_ANY )
            {
                
                // LogService.logIt( "ReminderThread.run() Starting Invitation Batch "  );
                
                if( emailUtils==null )
                    emailUtils = EmailUtils.getInstance();
                
                if( reminderUtils == null )
                    reminderUtils = new ReminderUtils( emailUtils );
                
                try
                {
                    reminderUtils.doInvitationBatch();                    
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "ReminderThread.run() Error during Invitation Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "ReminderThread.doInvitationBatch() Error during Invitation Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }                
            }

            
            if( RuntimeConstants.getBooleanValue( "autoRemindersOk" ) && ScoreManager.OK_TO_START_ANY )
            {
                Tracker.addLastReminderBatch();                
                
                // LogService.logIt( "ReminderThread.run() Starting Reminder Batch "  );
                
                if( emailUtils==null )
                    emailUtils = EmailUtils.getInstance();
                
                if( reminderUtils == null )
                    reminderUtils = new ReminderUtils( emailUtils );
                
                try
                {
                    reminderUtils.doReminderBatch();                    
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "ReminderThread.run() Error during Reminder Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "ReminderThread.doReminderBatch() Error during Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }                
            }
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "ReminderThread.run() Uncaught Exception during autobatch." );
            
            if( emailUtils==null )
                emailUtils = EmailUtils.getInstance();
            
           emailUtils.sendEmailToAdmin( "ReminderThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }
}
