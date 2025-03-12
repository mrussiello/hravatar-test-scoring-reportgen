/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.report.ReportSettings;
import com.tm2score.report.ReportTemplate;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.LogService;
import com.tm2builder.sim.xml.SimJ;
import java.io.IOException;
import java.net.URLDecoder;


/**
 *
 * @author Mike
 */
public abstract class BaseCoreTestTestTakerActivityReportTemplate extends BaseCoreTestReportTemplate implements ReportTemplate, ReportSettings
{
    @Override
    public abstract byte[] generateReport() throws Exception;


protected void addActivityListSection()  throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( reportUtils == null )
                reportUtils = new ReportUtils();

            TestEvent te = reportData.getTestEvent();

            java.util.List<Activity> activityList = reportUtils.getActivityList( te );

            if( activityList == null || activityList.isEmpty() )
                return;

            float y = addTitle( currentYLevel, lmsg( "g.ActivitiesPerformed" ), null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[]{1f} );

            float outerWid = bxWid*0.95f;

            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.BOX );
            c.setBorderWidth( 0.5f );
            c.setPadding( 2 );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            c.setBorderColor( BaseColor.DARK_GRAY );

            // Add header row.
            t.addCell( new Phrase( lmsg( "g.ActivitiesPerformed" ) , getFont()) );

            c.setBackgroundColor( BaseColor.WHITE );

            // Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;
            Font fnt;
            String buffer;

            for( Activity a : activityList )
            {

                if( a.getLevel()==1 )
                {
                    fnt = this.getFontBoldItalic();
                    buffer = "";
                }

                else if( a.getLevel()==2 )
                {
                    fnt = getFontItalic();
                    buffer = "   ";
                }

                else //if( a.getLevel()==3 )
                {
                    fnt = getFont();
                    buffer = "      ";
                }

                c = new PdfPCell( new Phrase( buffer + a.getName(), fnt ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 4 );
                t.addCell( c );
            }

            currentYLevel = addTableToDocument( y, t );
        }

        catch( IOException | DocumentException e )
        {
            LogService.logIt( e, "BaseTestTakerActivityReportTemplate.addActivityListSection()" );

            throw e;
        }

    }

}
