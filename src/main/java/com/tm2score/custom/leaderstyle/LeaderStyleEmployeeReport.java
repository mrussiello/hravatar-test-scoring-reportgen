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
public class LeaderStyleEmployeeReport extends BaseLeaderStyleReportTemplate implements ReportTemplate
{
    
    public LeaderStyleEmployeeReport()
    {
        super();
        manager = false;
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

            addHowUseThisReport();

            addFooterBar( lmsg( "g.SCOREBREAKDOWN"), true, fontXLargeBoldWhite );

            addNewPage();

            addTopTraitSection();

            addFooterBar( lmsg( "g.TRAITS"), false, fontXLargeBoldWhite );
            
            addNewPage();
            
            addLeaderStylesExplained();

            addCitationsSection();

            addMinimalPrepNotesSection();
                                    
            addFooterBar( lmsg_spec( "ls.StylesExpl").toUpperCase(), true, fontXLargeBoldWhite );
                        
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
            LogService.logIt( e, "LeaderStyleEmployeeReport.generateReport() " );

            //if( reportData!=null && reportData.getTestEvent()!=null && reportData.getTestEvent().getTestEventId()>0 )
            //    TestEventLogUtils.createTestEventLogEntry( reportData.getTestEvent().getTestEventId(),  "Ct2SelectionReport.generateReport()  Exception generating report. " + e.toString() );

            throw new STException( e );
        }
    }

    @Override
    public synchronized void specialInit()
    {
        feedbackReportCoverImageUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_13x1741970464932.png";
        
        feedbackReportFooterImageUrls = new String[] { "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_17x1741983088934.png",
                                                       "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_8x1741970464652.png",
                                                       "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_11x1741970464884.png",
                                                        "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_9x1741970464749.png",
                                                        "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_10x1741970464834.png"};

        super.specialInit();
        
        // must come after super.specialInit
        Map<String,Object> custRepVals = new HashMap<>();
        List<String> whatsIncludedList = new ArrayList<>();
        for( int i=1;i<=3;i++ )
        {
            whatsIncludedList.add( lmsg_spec("ls.WhatsIncluded.fbk." + i ));
        }
        custRepVals.put( "whatsincludedlist", whatsIncludedList );
        reportData.setCustomReportValues(custRepVals);
        
    }
    
    
    

}
