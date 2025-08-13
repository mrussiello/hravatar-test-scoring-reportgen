/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2score.dist.TestKeyDistThread;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventArchiver;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.Constants;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author miker_000
 */
public class TestKeyReportThread extends BaseReportManager implements Runnable 
{
    // TestEvent te;
    TestKey tk; 
    TestEvent partialBatteryTestEvent;

    long frcReportId;
    boolean forceCalcSection;
    boolean skipCompleted = false;
    boolean createAsArchived = false;
    
    int reportErrorCt = 0;

    public TestKeyReportThread(TestKey tk, long frcReportId, boolean forceCalcSection, boolean skipCompleted, boolean createAsArchived)
    {
        // this.te = te;
        this.tk = tk;
        this.frcReportId = frcReportId;
        this.forceCalcSection = forceCalcSection;
        this.skipCompleted = skipCompleted;
        this.createAsArchived=createAsArchived;
    }

    public TestKeyReportThread(TestEvent partialBatteryTestEvent, long frcReportId, boolean forceCalcSection, boolean createAsArchived)
    {
        // this.te = te;
        this.partialBatteryTestEvent = partialBatteryTestEvent;
        this.frcReportId = frcReportId;
        this.forceCalcSection = forceCalcSection;
        this.createAsArchived=createAsArchived;
    }

    
    @Override
    public void run() 
    {
        try
        {
            // LogService.logIt( "TestKeyReportThread.run() START, TestKeyId=" + (this.tk==null ? "NULL" : tk.getTestKeyId() ) );
            
            if( tk!=null )
                generateTestKeyReports();
            else if( partialBatteryTestEvent!=null )
                generatePartialBatteryReport();
            //generateReports(te, tk, frcReportId, forceCalcSection );
        }
        
        //catch( ReportException e )
        //{
        //    saveErrorInfo(e);
        //}
        
        catch( Exception e )
        {
            LogService.logIt( e, "TestKeyReportThread.run() " + e.getMessage() + ", TestKeyId=" + (this.tk==null ? "NULL" : tk.getTestKeyId() ) + ", partialBatteryTestEvent.TestEventId=" + (this.partialBatteryTestEvent==null ? "NULL" : partialBatteryTestEvent.getTestEventId() ) );
            if( tk!=null )
                BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );  
        }
    }
    
    public boolean generatePartialBatteryReport() throws Exception
    {
        int errCount = 0;
        TestEvent te = partialBatteryTestEvent;
        te.setPartialBatteryTestEvent(true);
        try
        {
            generateReports(te, tk, frcReportId, forceCalcSection, false, null, skipCompleted, createAsArchived, errCount);
            
            // since there will be no distribution, this is complete. Ok to remove from map
            BaseScoreManager.removeTestEventFromPartialDateMap( te.getTestEventId() );
            return true;
        }

        catch( ScoringException e )
        {
            saveErrorInfo(e);
            BaseScoreManager.removeTestEventFromPartialDateMap( te.getTestEventId() );
            return false;
            //errCount++;
        }
        catch( ReportException e )
        {
            saveErrorInfo(e);
            errCount++;
            BaseScoreManager.removeTestEventFromPartialDateMap( te.getTestEventId() );
            return false;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestKeyReportThread.generatePartialBatteryReport() " + e.getMessage() + ", TestEventId=" + (te==null ? "NULL" : te.getTestEventId()) + ", TestKeyId=" + (this.tk==null ? "NULL" : tk.getTestKeyId() ) );
            errCount++;
            BaseScoreManager.removeTestEventFromPartialDateMap( te.getTestEventId() );
            return false;
        }                
    }
    
    
    private void generateTestKeyReports() throws Exception
    {
        //int errCount = 0;
        
        for( TestEvent te : tk.getTestEventList() )
        {
            try
            {
               //  if( te!=null &&  te.getTestEventStatusTypeId()<TestEventStatusType.SCORED.getTestEventStatusTypeId() )
                // cant generate reports if testEvent is score error.
                if( te!=null &&  (!te.getTestEventStatusType().getIsScoredOrHigher() || te.getTestEventStatusType().equals( TestEventStatusType.SCORE_ERROR )) )
                {
                    if( tk.getBatteryId()>0 )
                    {                        
                        Calendar cal = new GregorianCalendar();
                        cal.add( Calendar.DAY_OF_YEAR, -1 );
                        
                        if( tk.getLastAccessDate()!=null && tk.getLastAccessDate().before( cal.getTime() ))
                        {
                            LogService.logIt( "TestKeyReportThread.generateTestKeyReports() TestKey is a Battery but member TestEvent is not scored. Last access over 24 hours days prior. Possible rescore of test key. Ignoring this test event. testKeyId=" + tk.getTestKeyId() + ", testKeyStatusTypeId=" + tk.getTestKeyStatusTypeId() + ", current TestEvent status=" + te.getTestEventStatusTypeId() + ", " + TestEventStatusType.getValue(te.getTestEventStatusTypeId()).getKey() + ", tk.lastAccessDate=" + tk.getLastAccessDate().toString() );
                            continue;
                            
                        }
                    }
                    // throw new ReportException( String message, int severity, TestEvent te, long reportId, String reportLangStr )
                    throw new ReportException( "TestKeyReportThread.generateTestKeyReports() TestEvent is not scored. testKeyId=" + tk.getTestKeyId() + ", testEventId=" + te.getTestEventId() + ", tk.batteryId=" + tk.getBatteryId() + ", testKeyStatusTypeId=" + tk.getTestKeyStatusTypeId() + ", current TestEvent status=" + te.getTestEventStatusTypeId() + ", " + TestEventStatusType.getValue(te.getTestEventStatusTypeId()).getKey(), 0, te,0, null );
                }
                
                generateReports(te, tk, frcReportId, forceCalcSection, false, null, skipCompleted, createAsArchived, reportErrorCt);
                
                // no exception, so ...
                reportErrorCt=0;
            }

            catch( ScoringException e )
            {
                saveErrorInfo(e);
                //errCount++;
            }
            catch( ReportException e )
            {
                saveErrorInfo(e);
                reportErrorCt++;
            }

            catch( Exception e )
            {
                LogService.logIt( e, "TestKeyReportThread.run() XX1 " + e.getMessage() + ", TestEventId=" + (te==null ? "NULL" : te.getTestEventId()) + ", TestKeyId=" + (this.tk==null ? "NULL" : tk.getTestKeyId() ) );
                reportErrorCt++;
            }
        }
        
        try
        {
        
            if( reportErrorCt <= 0 )
            {
                if( tk.getFirstDistComplete()==2 )
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.API_DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() );
                else
                    tk.setTestKeyStatusTypeId( tk.getFirstDistComplete()==1 ? TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() : TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId()  );
                tk.setErrorCnt(0);
            }

            else
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId()  );
                tk.setErrorCnt( tk.getErrorCnt()+1 );
                Tracker.addReportError();
            }

            if( tk.getTestKeyStatusType().equals ( TestKeyStatusType.REPORTS_COMPLETE ) || tk.getTestKeyStatusType().equals ( TestKeyStatusType.API_DISTRIBUTION_COMPLETE ) )
            {
                if( eventFacade == null )
                    eventFacade = EventFacade.getInstance();

                if( tk.getTestKeyStatusType().equals ( TestKeyStatusType.REPORTS_COMPLETE ) )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId() );
                }

                eventFacade.saveTestKey( tk );
    
                // force a reload of test Events.
                tk.setTestEventList( null );

                TestKeyDistThread tkdt = new TestKeyDistThread( tk );
                
                // Since we are already in a separate thread, don't need to start a new one.
                tkdt.run();
                // new Thread(new TestKeyDistThread( tk )).start(); 
                return;        
            }

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();
            
            eventFacade.saveTestKey( tk );

            // since this process is ending here, remove from map.
            if( tk!=null )
                BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );  
            // out[0]++;

            if( tk.getTestKeyStatusTypeId()==TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() && tk.getTestKeyArchiveId()<=0 )
            {
                // wait one minute before archiving.
                Thread.sleep( Constants.ARCHIVE_DELAY_SECS*1000);
                (new EventArchiver()).archiveTestKey(tk);
            }        
        }
        
        catch( ScoringException e )
        {
            // OK to ingnore. Either it's already archived or it doesn't exist.
            saveErrorInfo(e);
            
            // since this process is ending, remove from map.
            if( tk!=null )
                BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );  
            //errCount++;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestKeyReportThread.run() XX2 " + e.getMessage() + ", TestKeyId=" + (this.tk==null ? "NULL" : tk.getTestKeyId() ) );
            reportErrorCt++;

            // since this process is ending, remove from map.
            if( tk!=null )
                BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );  
        }
        
        
    }
    
}
