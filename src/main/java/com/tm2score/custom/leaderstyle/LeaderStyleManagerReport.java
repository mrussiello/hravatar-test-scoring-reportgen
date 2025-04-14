/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.leaderstyle;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class LeaderStyleManagerReport extends BaseLeaderStyleReportTemplate implements ReportTemplate
{
    public LeaderStyleManagerReport()
    {
        super();
        manager = true;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "LeaderStyleEmployeeReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPageV2(true);

            addNewPage();

            addReportInfoHeader();

            addFooterBar( lmsg( "g.SCOREBREAKDOWN"), true, fontXLargeBoldWhite );

            addNewPage();

            addTopTraitSection();

            addNewPage();
            
            addPurposeOfAssessment();
                        
            addFooterBar( lmsg( "g.Purpose").toUpperCase(), true, fontXLargeBoldWhite );
                        
            addNewPage();

            addLeaderStylesExplained();
            
            addFooterBar( lmsg_spec( "ls.StylesExpl").toUpperCase(), true, fontXLargeBoldWhite );
                                    
            addNewPage();
            
            addCitationsSection();
            
            addMinimalPrepNotesSection();
                        
            addFooterBar( lmsg( "g.TRAITS"), false, fontXLargeBoldWhite );
            
            addNewPage();

            addNotesSection();

            addFooterBar( lmsg( "g.NOTES"), false, fontXLargeBoldWhite );
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LeaderStyleManagerReport.generateReport() " );

            //if( reportData!=null && reportData.getTestEvent()!=null && reportData.getTestEvent().getTestEventId()>0 )
            //    TestEventLogUtils.createTestEventLogEntry( reportData.getTestEvent().getTestEventId(),  "Ct2SelectionReport.generateReport()  Exception generating report. " + e.toString() );

            throw new STException( e );
        }
    }
    
    @Override
    public synchronized void specialInit()
    {
        feedbackReportCoverImageUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_12x1741970464889.png";
        feedbackReportFooterImageUrls = new String[] {  "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_9x1741970464749.png",
                                                        "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_11x1741970464884.png",
                                                        "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_10x1741970464834.png"};

        super.specialInit();
        
        // must come after super.specialInit
        Map<String,Object> custRepVals = new HashMap<>();
        List<String> whatsIncludedList = new ArrayList<>();
        for( int i=1;i<=3;i++ )
        {
            whatsIncludedList.add( lmsg_spec("ls.WhatsIncluded.mgr." + i ));
        }
        custRepVals.put( "whatsincludedlist", whatsIncludedList );
        reportData.setCustomReportValues(custRepVals);
    }
    


}
