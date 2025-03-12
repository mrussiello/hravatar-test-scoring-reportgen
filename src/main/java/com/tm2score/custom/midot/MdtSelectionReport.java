/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.midot;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class MdtSelectionReport extends BaseMdtReportTemplate implements ReportTemplate
{

    public MdtSelectionReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "MdtSelectionReportgenerateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(true);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();

            addComparisonSection();

            addNewPage();

            addAssessmentOverview();

            addDetailedReportInfoHeader();

            // Tasks before competencies
            if( reportData.getReport().getIncludeTaskInfo() == 1)
                addTasksInfo();

            addAbilitiesInfo();

            addKSInfo();

            addAIMSInfo();

            addEQInfo();
            // addBiodataInfo();

            addWritingSampleInfo();


            // addCompetencyInfo();

            // Tasks after competencies
            //if( reportData.getReport().getIncludeTaskInfo() == 2)
            //    addTasksInfo();

            //addMinQualsApplicantDataInfo();

            //addEducTrainingInfo();

            addPreparationNotesSection();

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
            LogService.logIt( e, "MdtSelectionReportgenerateReport() " );

            throw new STException( e );
        }
    }


}
