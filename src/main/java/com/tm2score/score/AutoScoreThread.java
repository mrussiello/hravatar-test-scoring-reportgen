/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.dist.DistManager;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportManager;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import jakarta.ejb.EJBException;
import java.util.Date;

/**
 *
 * @author Mike
 */
public class AutoScoreThread implements Runnable {

    @Override
    public void run() {

        // LogService.logIt( "AutoScoreThread.run() Starting"  );

        try
        {
            if( RuntimeConstants.getBooleanValue( "autoScoreOk" ) )
            {
                Tracker.addLastAutoScoreBatch();                
                
                // LogService.logIt( "AutoScoreThread.doScoreAutoBatch() Starting Score Batch "  );
                ScoreManager sm = new ScoreManager();

                try
                {
                    sm.doScoreBatch(true, false);
                }
                catch( EJBException e )
                {
                    if( e.getMessage().contains("STOPPED" ) )
                        LogService.logIt( "AutoScoreThread.run() Error during Score Batch. " + e.toString() );
                    else
                    {
                        LogService.logIt(e, "AutoScoreThread.run() Error during Score Batch." );
                        EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.doReportAutoBatch() Error during Score Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                    }
                }
                catch( Exception e )
                {
                    LogService.logIt(e, "AutoScoreThread.run() Error during Score Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.doReportAutoBatch() Error during Score Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }

                try
                {
                    Thread.sleep( 2000 );   // 5000
                }
                catch( InterruptedException e )
                {
                    LogService.logIt( "AutoScoreThread.run() Waiting for starting the report batch. " + e.toString() );
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "AutoScoreThread.run() Waiting for starting the report batch." );
                }

                // LogService.logIt( "AutoScoreThread.doReportAutoBatch() Starting Report Batch "  );
                ReportManager rm = new ReportManager();

                rm.setTestEventScoreList(null);

                try
                {
                    rm.doReportBatch(false);
                }
                catch( EJBException e )
                {
                    if( e.getMessage().contains("STOPPED" ) )
                        LogService.logIt( "AutoScoreThread.run() Error during Report Batch. " + e.toString() );
                    else
                    {
                        LogService.logIt(e, "AutoScoreThread.run() Error during Report Batch." );
                        EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.doReportAutoBatch() Error during Report Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                    }
                }
                catch( Exception e )
                {
                    LogService.logIt(e, "AutoScoreThread.run() Error during Report Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.doReportAutoBatch() Error during Report Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }

                try
                {
                    Thread.sleep( 2000 );  // 5000
                }
                catch( InterruptedException e )
                {
                    LogService.logIt( "AutoScoreThread.run() Waiting for starting the distribution. " + e.toString() );
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "AutoScoreThread.run() Waiting for starting the distribution." );
                }

                // LogService.logIt( "AutoScoreThread.run() Starting Dist Batch "  );
                DistManager dm = new DistManager();

                try
                {
                    dm.doDistBatch(false, false);
                }
                catch( EJBException e )
                {
                    if( e.getMessage().contains("STOPPED" ) )
                        LogService.logIt( "AutoScoreThread.run() Error during Distribution Batch. " + e.toString() );
                    else
                    {
                        LogService.logIt(e, "AutoScoreThread.run() Error during Distribution Batch." );
                        EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.run() Error during Distribution Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                   }
                }
                catch( Exception e )
                {
                    LogService.logIt(e, "AutoScoreThread.run() Error during Distribution Batch." );
                    EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.run() Error during Distribution Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );

                }
            }

        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "AutoScoreThread.run() Uncaught Exception during autobatch." );
            EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }



}
