/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.report;

import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.service.LogService;
import com.tm2score.user.UserUtils;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Mike
 */
public class TestEventRegenReportThread implements Runnable
{
    List<Long> testEventIdList;

    UserUtils userUtils;

    long reportId;

    String description;
    boolean forceCalcSection = false;
    boolean sendResendCandidateReportEmails = false;
    Date startDate;
    Date endDate;
    int orgId;
    int suborgId;
    long simId;
    int simVersionId;
    long minTestEventId = 0;
    Date maxLastCandidateSendDate=null;
    
    EventFacade eventFacade;
    
    

    public TestEventRegenReportThread( long simId, int simVersionId, int orgId, int suborgId, Date startDate, Date endDate, long minTestEventId, List<Long> tel, long reportId, UserUtils uu, String description, boolean forceCalcSection, boolean sendResendCandidateReportEmails, Date maxLastCandidateSendDate)
    {
        this.simId=simId;
        this.simVersionId=simVersionId;
        this.minTestEventId=minTestEventId;
        this.orgId=orgId;
        this.startDate=startDate;
        this.endDate=endDate;
        this.testEventIdList = tel;
        this.userUtils = uu;
        this.description = description;
        this.reportId = reportId;
        this.forceCalcSection = forceCalcSection;
        this.sendResendCandidateReportEmails=sendResendCandidateReportEmails;
        this.maxLastCandidateSendDate=maxLastCandidateSendDate;
        this.suborgId=suborgId;
    }

    @Override
    public void run()
    {
        try
        {
            int batchCount = 1;
            
            if( testEventIdList!=null && !testEventIdList.isEmpty() )
            {
                LogService.logIt( "TestEventRegenReportThread.run() STARTING Batch " + batchCount + ", testEventIdList.size=" + testEventIdList.size() + ", offset=" + 0 + ", " + description);
                userUtils.regenerateReports(testEventIdList, reportId, true, forceCalcSection, sendResendCandidateReportEmails, maxLastCandidateSendDate );
                LogService.logIt( "TestEventRegenReportThread.run() COMPLETED Batch " + batchCount + ", testEventIdList.size=" + testEventIdList.size() + ", " + description );
            }
            
            if( testEventIdList==null || testEventIdList.size()<1000 )
                return;
            
            int offset = 0;
            int maxRows = 1000;
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            while( testEventIdList.size()>=1000 )
            {
                batchCount++;
                offset += 1000;
                
                testEventIdList = eventFacade.getTestEventIdsForSimIdAndOrOrg(simId, simVersionId, orgId, suborgId, minTestEventId, TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId(), TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId(), new int[]{TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId()}, startDate, endDate, maxRows, offset);
            
                if( !testEventIdList.isEmpty() )
                {
                    LogService.logIt( "TestEventRegenReportThread.run() STARTING Batch " + batchCount + ", testEventIdList.size=" + testEventIdList.size() + ", offset=" + offset + ", " + description);
                    userUtils.regenerateReports(testEventIdList, reportId, true, forceCalcSection, sendResendCandidateReportEmails, maxLastCandidateSendDate );
                    LogService.logIt( "TestEventRegenReportThread.run() COMPLETED Batch " + batchCount + ", testEventIdList.size=" + testEventIdList.size() + ", offset=" + offset + ", " + description );
                    
                }
            }
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestEventRescoreThread.run() testEventIdList.size=" + testEventIdList.size() + ", " + description );
        }
    }



}
