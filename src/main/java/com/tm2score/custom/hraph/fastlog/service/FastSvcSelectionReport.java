/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.fastlog.service;

import com.tm2score.custom.hraph.bsp.itss.*;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class FastSvcSelectionReport extends BaseItssReportTemplate implements ReportTemplate
{

    public FastSvcSelectionReport()
    {
        super();
        
        this.itss = new FastSvcData();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "FastSvcSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            
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
                  
            addIdentityImageCaptureSection();

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
            LogService.logIt( e, "FastSvcSelectionReport.generateReport() " );

            throw new STException( e );
        }
    }


}
