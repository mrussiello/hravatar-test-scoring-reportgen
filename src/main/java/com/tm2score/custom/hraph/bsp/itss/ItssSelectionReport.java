/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp.itss;

import com.tm2score.custom.hraph.bsp.*;
import com.tm2score.custom.coretest2.*;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class ItssSelectionReport extends BaseItssReportTemplate implements ReportTemplate
{

    public ItssSelectionReport()
    {
        super();
        
        this.itss = new ItssData();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "ItssSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            
            addCoverPage(false);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();
                        
            addNewPage();

            addWritingSampleInfo();

            addNewPage();
            
            addRoleFitCompetencySummary();
            
            addAltRoleFitScoreSummary();
            
            addFindingsRecommendationsSummary();
                        
            addPreparationNotesSection();

            // addCalculationSection(false);
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ItssSelectionReport.generateReport() " );

            throw new STException( e );
        }
    }


}
