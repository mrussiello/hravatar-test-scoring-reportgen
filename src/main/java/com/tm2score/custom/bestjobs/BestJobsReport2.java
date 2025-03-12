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
public class BestJobsReport2 extends BaseBestJobsReportTemplate2 implements ReportTemplate
{

    public BestJobsReport2()
    {
        super();
        careerScoutV2 = true;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "BestJobsReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            
            lightBoxBorderWidth = 0;
            
            addCoverPage(true);

            addNewPage();

            addIntroSection();

            addNewPage();
                  
            addRiasecResultsSection( "b.YourInterests" );
            
            addNewPage();
            
            if(1==2 || ((this.bestProfilesList==null || this.bestProfilesList.isEmpty()) && (this.eeoMatchList==null || this.eeoMatchList.isEmpty())) )
            {
                addNoMatchesSection();
            }

            else
            {                
                addJobZoneInfoSection();
                
                if( bestProfilesList!=null && !bestProfilesList.isEmpty())
                {
                    addNewPage();

                    addTopMatchesSummarySection();

                    addSummaryNotesSection1();

                    addOneLevelUpMatchesSummarySection();                
                }

                if( eeoMatchList!=null && !eeoMatchList.isEmpty() )
                {
                    addNewPage();

                    addEEOMatchesSummarySection();

                    addSummaryNotesSection4();
                }
                                
                addNewPage();
                
                if( bestProfilesList!=null && !bestProfilesList.isEmpty())
                {
                    addSummaryNotesSection2();

                    addDetailJobInfoSection(bestProfilesList, true, true, false, false, true, true, true, true, false);

                    if( oneLevelUpProfilesList!=null && !oneLevelUpProfilesList.isEmpty() )
                    {
                        addNewPage();

                        addSummaryNotesSection3();

                        addDetailJobInfoSection(oneLevelUpProfilesList, false, false, false, false, false, false, false, true, true);
                    }

                    addNewPage();

                    addPostDetailNotesSection(); 

                    addMatchInfoToJobSpecReport();
                }
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
