/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.misc;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.service.LogService;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class IFrameTestReport implements ReportTemplate
{

    public IFrameTestReport()
    {
        super();
    }


    
    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IFrameTestReport.generateReport() " );
            throw new STException( e );
        }
    }

    @Override
    public void init( ReportData reportData ) throws Exception
    {}

    @Override
    public long addReportToOtherTestEventId() throws Exception
    {
        return 0;
    }
    
    @Override
    public boolean isValidForTestEvent()
    {
        return true;
    }
    
    @Override
    public boolean getIsReportGenerationPossible()
    {
        return true;
    }

    @Override
    public void dispose() throws Exception
    {}
    
    @Override
    public Locale getReportLocale()
    {
        return Locale.US;
    }
    
    @Override
    public String getReportGenerationNotesToSave()
    {
        return null;
    }

}
