/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.findly;

import com.tm2score.custom.coretest2.BaseCT2ReportTemplate;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.user.Org;
import com.tm2score.findly.FindlyScoreUtils;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class FindlyReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    public FindlyReportTemplate()
    {
        super();
    }

    @Override
    public byte[] generateReport() throws Exception
    {
        byte[] out = null;

        try
        {
            // LogService.logIt( "FindlyReportTemplate.generateReport() STARTING for " + reportData.getTestEvent().toString()  );

            TestEvent te = reportData.getTestEvent();
            //Org org = reportData.getOrg();
            //Locale rptLoc = getReportLocale();

            FindlyScoreUtils fsu = new FindlyScoreUtils();

            te.setOrg( reportData.getOrg() );

            return fsu.getFindlyPdfReport( te, reportData.getOrg() );
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FindlyReportTemplate.generateReport() " );

            throw new STException( e );
        }
    }


    @Override
    public String getReportGenerationNotesToSave()
    {
        return null;
    }
    
    
    
    @Override
    public Locale getReportLocale()
    {
        TestEvent te = reportData.getTestEvent();
        TestKey tk = reportData.getTestKey();

        Locale reportLocale = Locale.US;

        if( te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
            reportLocale = I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

        else if( te.getReport()!=null && te.getReport().getLocaleStr()!= null && !te.getReport().getLocaleStr().isEmpty() )
            reportLocale =  I18nUtils.getLocaleFromCompositeStr( te.getReport().getLocaleStr() );

        else if( tk!=null && tk.getAuthUser()!=null && tk.getAuthUser().getLocaleStr()!=null && !tk.getAuthUser().getLocaleStr().isEmpty()  )
            reportLocale =  I18nUtils.getLocaleFromCompositeStr( tk.getAuthUser().getLocaleStr() );

        else if( tk.getSuborgId()>0 && tk.getSuborg()!=null && tk.getSuborg().getDefaultReportLang()!=null && !tk.getSuborg().getDefaultReportLang().isEmpty() )
            reportLocale = I18nUtils.getLocaleFromCompositeStr( tk.getSuborg().getDefaultReportLang() );

        else if( tk.getOrg()!=null && tk.getOrg().getDefaultReportLang()!=null && !tk.getOrg().getDefaultReportLang().isEmpty() )
            reportLocale = I18nUtils.getLocaleFromCompositeStr( tk.getOrg().getDefaultReportLang() );

        else
            reportLocale = Locale.US;

        return reportLocale;

    }


}
