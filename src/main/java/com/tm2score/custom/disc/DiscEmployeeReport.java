/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.disc;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class DiscEmployeeReport extends BaseDiscReportTemplate implements ReportTemplate
{

    public DiscEmployeeReport()
    {
        super();
        manager = false;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "DiscManagerReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPageV2(true);

            addNewPage();

            addReportInfoHeader();

            addDiscStylesExplained();

            addNewPage();

            addTopTraitSection();

            addNewPage();

            // addLeadingTraitSection();

            addHowXShouldWorkWithYSection( 0 );

            addHowXShouldWorkWithYSection( 1 );

            addHowXShouldWorkWithYSection( 2 );

            addHowXShouldWorkWithYSection( 3 );
            
            addNewPage();

            addDiscEducationSection();
                        
            addMinimalPrepNotesSection();
                
            if( !reportData.getReportRuleAsBoolean( "usernotesoff" ) )
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
            LogService.logIt( e, "DiscManagerReport.generateReport() " );

            //if( reportData!=null && reportData.getTestEvent()!=null && reportData.getTestEvent().getTestEventId()>0 )
            //    TestEventLogUtils.createTestEventLogEntry( reportData.getTestEvent().getTestEventId(),  "Ct2SelectionReport.generateReport()  Exception generating report. " + e.toString() );

            throw new STException( e );
        }
    }


}
