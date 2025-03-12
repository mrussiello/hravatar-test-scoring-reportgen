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
public class OrgAutoTestExpireThread implements Runnable {

    ReminderUtils reminderUtils;
    EmailUtils emailUtils;
    
    public OrgAutoTestExpireThread( ReminderUtils ru, EmailUtils eu )
    {
       this.reminderUtils = ru; 
       this.emailUtils = eu;
    }
    
    
    @Override
    public void run() {

        TestKeyEventSelectionType tkest = RuntimeConstants.getTestKeyEventSelectionType();            
        if( !tkest.getIsAllOrNone() && !tkest.keep(111))
        {
            LogService.logIt( "OrgAutoTestExpireThread.run() Since this server is not configured to process test keys ending with 1, OrgAutoTest expiration messages are off." );
            return;
        }        
                
        // LogService.logIt( "OrgAutoTestExpireThread.run() Starting"  );

        try
        {
            if( RuntimeConstants.getBooleanValue( "autoOrgAutoTestExpirationOk" )  && ScoreManager.OK_TO_START_ANY )
            {
                Tracker.addLastOrgAutoTestExpireBatch();
                
                // LogService.logIt( "ReminderThread.run() Starting Reminder Batch "  );
                
                if( emailUtils==null )
                    emailUtils = EmailUtils.getInstance();
                
                if( reminderUtils == null )
                    reminderUtils = new ReminderUtils( emailUtils );
                
                try
                {
                    reminderUtils.doExpireOrgAutoTestRecordsBatch();
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "OrgAutoTestExpireThread.run() Error during Reminder Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "ExpireThread.run() Error during Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }
            }
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "OrgAutoTestExpireThread.run() Uncaught Exception during autobatch." );
            
            if( emailUtils==null )
                emailUtils = EmailUtils.getInstance();
            
           emailUtils.sendEmailToAdmin( "OrgAutoTestExpireThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }
    }
}
