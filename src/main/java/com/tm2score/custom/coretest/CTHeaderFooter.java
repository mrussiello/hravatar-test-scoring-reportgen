/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.tm2score.report.ReportSettings;
import com.tm2score.format.StandardHeaderFooter;
import com.tm2score.report.ReportData;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * Use this class to make any changes to CoreTest that are different than standard report.
 * 
 * @author Mike
 */
public class CTHeaderFooter extends StandardHeaderFooter
{

    public CTHeaderFooter( Document d , Locale l, String t, ReportData rd, ReportSettings rs ) throws Exception
    {
        super( d, l, t, rd, rs );
    }


}
