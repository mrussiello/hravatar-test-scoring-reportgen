/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.sports;



import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class SpReport extends BaseSpReportTemplate implements ReportTemplate
{
    String assessoverviewtext = null;

    
    
    
    
    public SpReport()
    {
        super();
        
        this.devel = false;
        
        // this.redYellowGreenGraphs=false;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            this.coverDescrip = bmsg( "sp.coverpagetext" );

            LogService.logIt( "CqReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(true);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummarySection();

            addNewPage();

            // addAssessmentOverview();
            
            // addDetailedReportInfoHeader();

            addCompetencyDetailSection();

            addIdentityImageCaptureSection();

            addMinimalPrepNotesSection();
            // addPreparationNotesSection();

            addNewPage();

            addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CqReport.generateReport() " );

            //if( reportData!=null && reportData.getTestEvent()!=null && reportData.getTestEvent().getTestEventId()>0 )
            //    TestEventLogUtils.createTestEventLogEntry( reportData.getTestEvent().getTestEventId(),  "Ct2SelectionReport.generateReport()  Exception generating report. " + e.toString() );                    
                        
            throw new STException( e );
        }
    }

    
    

}
