/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author Mike
 */
public class AutoScoreStarter implements Runnable {

    
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static ScheduledFuture<?> sched = null;
    
    
    @Override
    public void run() {

        // LogService.logIt( "AutoScoreStarter.run() Starting"  );

        try
        {
            // LogService.logIt( "AutoScoreStarter.run() START AutoBatches On=" + RuntimeConstants.getBooleanValue( "autoScoreOk" ) );
            
            if( !RuntimeConstants.getBooleanValue( "autoScoreOk" ) )
                return;
            
            // wait one minute
            // Thread.sleep( 60000 );
            // Thread.sleep( 60000 );
            
            // LogService.logIt( "AutoScoreStarter.run() STARTING SETUP  BBBB ");
            final Runnable autoScoreThread = new AutoScoreThread();
    
            // final ScheduledFuture<?> sched = scheduler.scheduleAtFixedRate(autoScoreThread, 30, 180, SECONDS);
            sched = scheduler.scheduleAtFixedRate( autoScoreThread, 60, 120, SECONDS );
            LogService.logIt( "AutoScoreStarter.run() COMPLETED SETUP  CCCC ");
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "AutoScoreStarter.run() Uncaught Exception during autobatch." );
            EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreStarter.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }



}
