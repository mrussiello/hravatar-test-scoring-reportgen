/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.reminder;

import com.tm2score.custom.misc.IframeTestAutoCompleteThread;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author Mike
 */
public class ReminderStarter implements Runnable {

    
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static ScheduledFuture<?> sched = null;
    
    public static ScheduledFuture<?> sched2 = null;

    public static ScheduledFuture<?> sched3 = null;

    public static ScheduledFuture<?> sched4 = null;

    public static ScheduledFuture<?> sched5 = null;
    
    @Override
    public void run() {

        // LogService.logIt( "ReminderStarter.run() Starting"  );

        try
        {
            // LogService.logIt( "ReminderStarter.run() START Send AutoReminders=" + RuntimeConstants.getBooleanValue( "autoRemindersOk" ) );
            
            // Wait 1 minute.
            Thread.sleep( 60000 );
            
            // clear cache
            UserFacade uf = UserFacade.getInstance();
            if( uf==null )
            {
                LogService.logIt( "ReminderStarter.run() Unable to run because UserFacade is null. ");
                return;
            }
            uf.clearSharedCache();
            // uf.clearSharedCacheDiscern();
            
            // LogService.logIt( "ReminderStarter.run() STARTING SETUP  AAA ");
            EmailUtils eu = EmailUtils.getInstance();
            
            // Runs once an hour
            if( RuntimeConstants.getBooleanValue( "autoRemindersOk" ) )
            {
                final Runnable reminderThread = new ReminderThread( new ReminderUtils(eu), eu );

                // final ScheduledFuture<?> sched = scheduler.scheduleAtFixedRate(autoScoreThread, 30, 180, SECONDS);
                // sched = scheduler.scheduleAtFixedRate(reminderThread, 10, 30, SECONDS);
                sched = scheduler.scheduleAtFixedRate(reminderThread, 60*60, 60*60, SECONDS);
            }
            
            // Runs once every 15 minutes.
            if( RuntimeConstants.getBooleanValue( "autoOrgAutoTestExpirationOk" ) )
            {
                final Runnable expireThread = new OrgAutoTestExpireThread( new ReminderUtils(eu), eu );            
                sched2 = scheduler.scheduleAtFixedRate(expireThread, 60*Constants.ORGAUTOTEST_EXPIRATION_WARNING_WINDOW_MINS, 60*Constants.ORGAUTOTEST_EXPIRATION_WARNING_WINDOW_MINS, SECONDS);
                // sched2 = scheduler.scheduleAtFixedRate(expireThread, 10, 30, SECONDS);
            }
            
            // For daily batches, we need to ensure they ONLY run once a day. So, this will be at 6 am EST precisely. 
            // So, if it is after 6 am EST today, the delay will until 6 am EST tomorrow. 
            // If it is before 6 am EST today, the delay will be between now and 6 am EST. 
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
            ZonedDateTime nextRun = now.withHour(6).withMinute(0).withSecond(0);
            if( now.compareTo(nextRun) > 0)
                nextRun = nextRun.plusDays(1);

            Duration duration = Duration.between(now, nextRun);
            long dailyInitialDelaySecs = duration.getSeconds();            

            if( RuntimeConstants.getBooleanValue( "autoOrgSubscriptionExpirationOk" ) )
            {
                final Runnable expireThread = new SubscriptionExpireThread( new ReminderUtils(eu), eu );            
                sched3 = scheduler.scheduleAtFixedRate( expireThread, dailyInitialDelaySecs, 60*60*24, SECONDS);
            }

            if( RuntimeConstants.getBooleanValue( "autoOrgCreditsExpirationOk" ) )
            {
                final Runnable expireThread = new CreditsExpireThread( new ReminderUtils(eu), eu );            
                sched5 = scheduler.scheduleAtFixedRate( expireThread, dailyInitialDelaySecs, 60*60*24, SECONDS);
            }

            if( RuntimeConstants.getBooleanValue( "autoIframeCompletionOk" ) )
            {
                final Runnable expireThread = new IframeTestAutoCompleteThread();            
                sched4 = scheduler.scheduleAtFixedRate(expireThread, 360, 60*60*Constants.IFRAMETEST_AUTOCOMPLETE_HOURS, SECONDS);
            }

            if( RuntimeConstants.getBooleanValue( "autoTkExpWarningsOk" ) )
            {
                final Runnable tkWarnThread = new TestKekExpireWarningThread(new ReminderUtils(eu), eu);            
                sched5 = scheduler.scheduleAtFixedRate(tkWarnThread, 65*60, 2*60*60, SECONDS);
            }

            // LogService.logIt( "ReminderStarter.run() COMPLETED SETUP  BBB ");
        }
        
        catch( Exception ee )
        {
            LogService.logIt(ee, "ReminderStarter.run() Uncaught Exception during autobatch." );
            EmailUtils.getInstance().sendEmailToAdmin( "ReminderStarter.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }



}
