 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2score.custom.hraph.tmldr.*;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class UMinnDevelopmentReport extends BaseUMinnReportTemplate implements ReportTemplate
{

    public UMinnDevelopmentReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            addCoverPage();

            addNewPage();

            addContents();
            
            addNewPage();

            addPCModelOverview();

            addNewPage();
            
            addFeedbackReportOverview();
            
            addOverallSummary();
            
            addCompetencySummary();
            
            addComparison();

            // addNewPage();
            
            addCompetencyDetails();

            addNewPage();
            
            addScenarioLevelReport();
            
            addPreparationNotesSection();

            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnDevelopmentReport.generateReport() " );

            throw new STException( e );
        }
    }

    public void initForSource()
    {
        // Use all default. Nothing to do here.
    }
    

}
