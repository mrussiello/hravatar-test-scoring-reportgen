/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2score.entity.report.Report;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class ReportRegenerationThread implements Runnable {
    
    long testEventId;
    long reportId;
    boolean forceCalcSection;
    boolean skipCompleted=false;
    
    public ReportRegenerationThread( long testEventId, long reportId, boolean forceCalcSection) throws Exception
    {
        this.testEventId=testEventId;
        this.reportId=reportId;
        this.forceCalcSection=forceCalcSection;
        
        if( testEventId<=0 )
            throw new Exception( "ReportRegenerationThread() testEventId invalid: " + testEventId );
        
        if( reportId<=0 )
            throw new Exception( "ReportRegenerationThread() reportId invalid: " + reportId + ", testEventId=" + testEventId );
    }
    
    @Override
    public void run()
    {
        try
        {
            Thread.sleep( 20000 );
            
            if( testEventId<=0 )
                throw new Exception( "ReportRegenerationThread() testEventId invalid: " + testEventId );

            if( reportId<=0 )
                throw new Exception( "ReportRegenerationThread() reportId invalid: " + reportId + ", testEventId=" + testEventId );
            
            // LogService.logIt( "ReportRegenerationThread.run() START TestEventId=" + testEventId + ", reportId=" + reportId );
                            
            ReportManager rm = new ReportManager();

            //if( rmb.getTestEventScoreList() == null )
            rm.setTestEventScoreList( new ArrayList<>() );


            List<Report> rl = rm.genRegenReportTestEvent(testEventId, reportId, forceCalcSection, skipCompleted, RuntimeConstants.getBooleanValue("create_reports_init_as_archived"), false, null );

            if( rl != null && !rl.isEmpty()  )
            {
                String s = "";

                for( Report r : rl )
                {
                    if( !s.isEmpty() )
                        s += " and ";

                    s += r.getName();
                }

                LogService.logIt( "ReportRegenerationThread.run() FINISH TestEventId=" + testEventId + ", reportId=" + reportId + ", regenerated: " + s );
            }

            else
                LogService.logIt( "ReportRegenerationThread.run()  Did not generate any reports. TestEventId=" + testEventId + ", reportId=" + reportId );
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "ReportRegenerationThread.run() TestEventId=" + testEventId + ", reportId=" + reportId );
        }
    }    
}
