/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class CTSelectionReport extends BaseCoreTestReportTemplate implements ReportTemplate
{

    public CTSelectionReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            LogService.logIt( "CTSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage();

            addNewPage();

            addReportInfoHeader();

            addSummarySection();

            addComparisonSection();

            boolean addedPage = addAssessmentOverview();

            if( !addedPage )
                addNewPage();

            addDetailedReportInfoHeader();

            // Tasks before competencies
            if( reportData.getReport().getIncludeTaskInfo() == 1)
                addTasksInfo();

            addKSAInfo();

            addAIMSInfo();

            // addCompetencyInfo();

            // Tasks after competencies
            if( reportData.getReport().getIncludeTaskInfo() == 2)
                addTasksInfo();

            addBiodataInfo();

            addEQInfo();

            addWritingSampleInfo();

            addMinQualsApplicantDataInfo();

            addEducTrainingInfo();

            addPreparationNotesSection();

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
            LogService.logIt( e, "CTSelectionReport.generateReport() " );

            throw new STException( e );
        }
    }


}
