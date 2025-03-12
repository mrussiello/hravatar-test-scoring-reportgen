/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.bestjobs;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class BestJobsReport extends BaseBestJobsReportTemplate implements ReportTemplate
{

    public BestJobsReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "BestJobsReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            
            addCoverPage(true);

            addNewPage();

            addIntroSection();

            addNewPage();
                  
            addRiasecResultsSection( "b.YourInterests" );
            
            addNewPage();
            
            if(1==2 || (this.bestProfilesList==null || this.bestProfilesList.isEmpty()) )
            {
                addNoMatchesSection();
            }

            else
            {                
                addTopMatchesSummarySection();

                addSummaryNotesSection();

                // addNewPage();

                addDetailJobInfoSection(bestProfilesList, true, true, true, true, true, true, false, true, false);

                addNewPage();

                addPostDetailNotesSection(); 
                
                addMatchInfoToJobSpecReport();
            }

            addHRAInfoSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BestJobsReport.generateReport() " );

            throw new STException( e );
        }
    }

    
    

}
