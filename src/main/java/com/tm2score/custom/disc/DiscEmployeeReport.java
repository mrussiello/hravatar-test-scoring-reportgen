/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.disc;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class DiscEmployeeReport extends BaseDiscReportTemplate implements ReportTemplate
{
    
    public DiscEmployeeReport()
    {
        super();
        manager = false;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "DiscManagerReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPageV2(true);

            addNewPage();

            addReportInfoHeader();

            addDiscStylesExplained();

            addFooterBar( lmsg( "g.SCOREBREAKDOWN"), true, fontXLargeBoldWhite );
            
            addNewPage();

            addTopTraitSection();

            addFooterBar( lmsg( "g.TRAITS"), false, fontXLargeBoldWhite );
            
            addNewPage();

            // addLeadingTraitSection();

            addHowXShouldWorkWithYSection( 0 );
            
            addBlueBar();

            addHowXShouldWorkWithYSection( 1 );

            addFooterBar( lmsg( "g.TEAMWORK"), true, fontXLargeBoldWhite );
            
            addNewPage();

            addHowXShouldWorkWithYSection( 2 );

            addBlueBar();
            
            addHowXShouldWorkWithYSection( 3 );
            
            addFooterBar( lmsg( "g.TEAMWORK"), false, fontXLargeBoldWhite );
            
            addNewPage();

            addDiscEducationSection();

            addFooterBar( lmsg( "g.LEARNMORE"), true, fontXLargeBoldWhite );
                
            // if( !reportData.getReportRuleAsBoolean( "usernotesoff" ) )
            addNewPage();

            addNotesSection();

            addFooterBar( lmsg( "g.NOTES"), false, fontXLargeBoldWhite );
            
            addMinimalPrepNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscManagerReport.generateReport() " );

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
    }
    
    
    

}
