/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class BspSelectionReport extends BaseBspReportTemplate implements ReportTemplate
{

    public BspSelectionReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "BspSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(false);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();

            addComparisonSection();

            addWritingSampleInfo();

            addNewPage();
            
            addAssessmentOverview();

            addDetailedReportInfoHeader();

            addCoreCompetenciesInfo();
            
            addNewPage();
            
            addDevelopmentReportSection();
            
            addPreparationNotesSection();

            addCalculationSection(false);

            //addNewPage();

            //addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BspSelectionReport.generateReport() " );

            throw new STException( e );
        }
    }


}
