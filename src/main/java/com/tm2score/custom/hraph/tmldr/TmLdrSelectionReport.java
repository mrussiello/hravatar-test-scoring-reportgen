 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.tmldr;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class TmLdrSelectionReport extends BaseTmLdrReportTemplate implements ReportTemplate
{

    public TmLdrSelectionReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "TmLdrSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPageV2(true);

            addNewPage();

            addAssessmentOverview();

            addNewPage();


            addReportSummaryChart();

            addComparisonSection();

            addNewPage();

            addDetailedReportInfoHeader();
            
            addAltScoreSection();            

            addAllDetailCompsInfoSections( );

            addWritingSampleInfo();


            // addCompetencyInfo();

            // Tasks after competencies
            //if( reportData.getReport().getIncludeTaskInfo() == 2)
            //    addTasksInfo();

            // addMinQualsApplicantDataInfo();

            // addEducTrainingInfo();

            addPreparationNotesSection();

            addIdentityImageCaptureSection();
            
            addCalculationSection(true);

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
            LogService.logIt( e, "TmLdrSelectionReport.generateReport() " );

            throw new STException( e );
        }
    }


}
