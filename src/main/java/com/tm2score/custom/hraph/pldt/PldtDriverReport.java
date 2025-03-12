/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.pldt;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class PldtDriverReport extends BasePldtDriverReportTemplate implements ReportTemplate
{

    public PldtDriverReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "BspSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            // addCoverPage(false);

            addNewPage();

            addOverallSection();

            addTest1Section();

            addTest2And3Section();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PldtDriverReport.generateReport() " );

            throw new STException( e );
        }
    }


}
