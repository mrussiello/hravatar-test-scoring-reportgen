/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.reminder;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.score.ScoreManager;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import java.util.Date;

/**
 *
 * @author Mike
 */
public class TestKekExpireWarningThread implements Runnable {

    ReminderUtils reminderUtils;
    EmailUtils emailUtils;
    
    public TestKekExpireWarningThread( ReminderUtils ru, EmailUtils eu )
    {
       this.reminderUtils = ru; 
       this.emailUtils = eu;
    }
    
    
    @Override
    public void run() {

        // LogService.logIt( "TestKekExpireWarningThread.run() Starting"  );

        try
        {
            if( RuntimeConstants.getBooleanValue( "autoTkExpWarningsOk" ) && ScoreManager.OK_TO_START_ANY )
            {                
                if( emailUtils==null )
                    emailUtils = EmailUtils.getInstance();
                
                if( reminderUtils == null )
                    reminderUtils = new ReminderUtils( emailUtils );
                                
                try
                {
                    reminderUtils.doTestKeyExpireWarningBatch();                    
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "TestKekExpireWarningThread.run() Error during TestKey Expire Warning Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "ReminderThread.doReminderBatch() Error during TestKey Expire Warning Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }
            }
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "TestKekExpireWarningThread.run() Uncaught Exception during autobatch." );
            
            if( emailUtils==null )
                emailUtils = EmailUtils.getInstance();
            
           emailUtils.sendEmailToAdmin( "TestKekExpireWarningThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }
}
