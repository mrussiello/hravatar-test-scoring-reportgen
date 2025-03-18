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
public class DiscManagerReport extends BaseDiscReportTemplate implements ReportTemplate
{
    public DiscManagerReport()
    {
        super();
        manager = true;
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
            
            addLeadingTraitSection();

            addBlueBar();
            
            addHowWorkWithTraitSection();

            addFooterBar( lmsg( "g.TEAMWORK"), false, fontXLargeBoldWhite );
            
            addNewPage();
            
            addDiscEducationSection();

            addFooterBar( lmsg( "g.LEARNMORE"), true, fontXLargeBoldWhite );
                
            addNewPage();            
            
            addKeyActionsToTakeSection();
            
            addFooterBar( lmsg( "g.KEYACTIONS"), false, fontXLargeBoldWhite );
                
            addNewPage();            

            addDiscBuildYourTeamSection();
                        
            addFooterBar( lmsg( "g.ACTIVITIES"), true, fontXLargeBoldWhite );
            
            addNewPage();
            
            addHowBuildTeamsWithDiscSection();

            addBlueBar();
            
            addAvoidSterotypingSection();
            
            addFooterBar( lmsg( "g.NEXTSTEPS"), false, fontXLargeBoldWhite );

            addNewPage();
            
            
            //addDiscManagerInfoSection();

            //addFooterBar( lmsg( "g.TEAMWORK"), false, fontXLargeBoldWhite );
            
            
            //if( !reportData.getReportRuleAsBoolean( "usernotesoff" ) )
            //    addNewPage();

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
        feedbackReportCoverImageUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_12x1741970464889.png";
        feedbackReportFooterImageUrls = new String[] {  "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_9x1741970464749.png",
                                                        "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_11x1741970464884.png",
                                                        "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_10x1741970464834.png"};
        super.specialInit();
    }
    


}
