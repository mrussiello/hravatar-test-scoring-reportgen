/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class CoreTestTestTakerActivityReport extends BaseCoreTestTestTakerActivityReportTemplate implements ReportTemplate
{

    public CoreTestTestTakerActivityReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            LogService.logIt( "CoreTestTestTakerActivityReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage();

            addNewPage();

            addReportInfoHeader();

            addAssessmentOverview();

            addActivityListSection();

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
            LogService.logIt( e, "TestTakerACoreTestTestTakerActivityReportctivityReport.generateReport() " );

            throw new STException( e );
        }
    }

}
