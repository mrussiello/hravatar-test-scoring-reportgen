/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.report;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventFacade;
import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author miker
 */
public class ReportArchiveUtils {
    
    EventFacade eventFacade;
    
    
    public TestEventScore unarchiveReportTestEventScore( TestEventScore tesToUnarchive ) throws Exception
    {
        try
        {
            if( tesToUnarchive==null )
                throw new Exception( "tesToUnarchive is null!" );
            if( !tesToUnarchive.getTestEventScoreType().getIsReport() )
                throw new Exception( "tesToUnarchive is not a report type. " + tesToUnarchive.getTestEventScoreType().getKey() );
            if( !tesToUnarchive.getTestEventScoreStatusType().getIsReportArchived() )
            {
                LogService.logIt( "ReportArchiveUtils.unarchiveReportTestEventScore() BBB.1 tesToUnarchive is not in a ReportArhived status. " + tesToUnarchive.getTestEventScoreStatusType().getKey() + ", testEventId=" + tesToUnarchive.getTestEventId() + ", testEventScoreId=" + (tesToUnarchive==null ? "null" : tesToUnarchive.getTestEventScoreId()));
                if( tesToUnarchive.getTestEventScoreStatusType().getIsActive() && tesToUnarchive.getHasReport() )
                    return tesToUnarchive;
            }
            
            ReportManager rm = new ReportManager();

            rm.setTestEventScoreList( new ArrayList<>() );

            TestEventScore tes = rm.generateReportForTestEventAndLanguage(tesToUnarchive.getTestEventId(), tesToUnarchive.getReportId(), tesToUnarchive.getStrParam1(), 0, false, false, false, null );

            byte[] out = tes==null || tes.getReportBytes()==null || tes.getReportBytes().length==0 ? new byte[0] : tes.getReportBytes();
            
            LogService.logIt( "ReportArchiveUtils.unarchiveReportTestEventScore() XXX.1 Completed unarchiving. bytes.length=" + (out==null ? "null" : out.length) + ", testEventId=" + (tesToUnarchive==null ? "null" : tesToUnarchive.getTestEventId()) + ", testEventScoreId=" + (tesToUnarchive==null ? "null" : tesToUnarchive.getTestEventScoreId()) );
            
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            
            eventFacade.saveTestEventScore(tes);

            return tes;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReportArchiveUtils.unarchiveReportTestEventScore() ZZZ.1 Completed unarchiving. testEventId=" + (tesToUnarchive==null ? "null" : tesToUnarchive.getTestEventId()) + ", testEventScoreId=" + (tesToUnarchive==null ? "null" : tesToUnarchive.getTestEventScoreId()) );
            throw e;
        }
    }

    public TestEventScore unarchiveReportTestEventScore( long testEventId, long reportId, String langStr, int includeEnglishReport, boolean forceCalcSection, boolean forceSendCandidateReports, Date maxLastCandidateSendDate ) throws Exception
    {
        try
        {
            ReportManager rm = new ReportManager();

            rm.setTestEventScoreList( new ArrayList<>() );

            TestEventScore tes = rm.generateReportForTestEventAndLanguage(testEventId, reportId, langStr, includeEnglishReport, false, forceCalcSection, forceSendCandidateReports, maxLastCandidateSendDate );

            byte[] out = tes==null || tes.getReportBytes()==null || tes.getReportBytes().length==0 ? new byte[0] : tes.getReportBytes();

            LogService.logIt("ReportArchiveUtils.unarchiveReportTestEventScore() WWW.1 bytes.length=" + (out==null ? "null" : out.length) + ", testEventId=" + testEventId + ", reportId=" + reportId + ", langStr=" + (langStr==null ? "null" : langStr) + ", forceCalcSection=" + forceCalcSection + ", forceSendCandidateReports=" + forceSendCandidateReports );

            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            
            eventFacade.saveTestEventScore(tes);

            return tes;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReportArchiveUtils.unarchiveReportTestEventScore() YYY.1 testEventId=" + testEventId + ", reportId=" + reportId + ", langStr=" + (langStr==null ? "null" : langStr) + ", forceCalcSection=" + forceCalcSection + ", forceSendCandidateReports=" + forceSendCandidateReports );
            throw e;
        }
    }


}
