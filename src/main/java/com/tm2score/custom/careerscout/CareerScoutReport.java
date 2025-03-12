/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.careerscout;

import com.tm2score.custom.bestjobs.*;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class CareerScoutReport extends BaseCareerScoutReportTemplate implements ReportTemplate
{

    public CareerScoutReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            LogService.logIt( "CareerScoutReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            
            addCoverPage(true);

            addNewPage();

            addIntroSection();

            addNewPage();
            
            addCompetencySummarySection();
                    
            addNewPage();            
            
            addRiasecResultsSection( "s.YourInterests" );
            
            addNewPage();
            
            if( (bestProfilesList==null || bestProfilesList.isEmpty()) && (this.eeoMatchList==null || this.eeoMatchList.isEmpty()))
            {
                addNoMatchesSection();
                
                addCompetencySuggestionSection();
                
                addPostDetailNotesSection(); 
                
                
            }

            else
            {
                addJobZoneInfoSection();
                
                if( bestProfilesList!=null && !bestProfilesList.isEmpty() )
                {
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
           
                
                // addSummaryNotesSection();
                
                addCompetencySuggestionSection();

                // addNewPage();

                addDetailJobInfoSection(bestProfilesList, true, true, true, true, true, true, false, true, false);

                addNewPage();

                addPostDetailNotesSection(); 
                
                // addMatchInfoToJobSpecReport();
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
            LogService.logIt( e, "CareerScountReport.generateReport() " );

            throw new STException( e );
        }
    }

    
    

}
