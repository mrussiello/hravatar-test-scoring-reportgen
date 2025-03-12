/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.report.ReportSettings;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportData;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 *
 * @author Mike
 */
public class StandardHeaderFooter extends PdfPageEventHelper
{
    public float HEADER_HEIGHT = 0;
    public float HEADER_TITLE_WIDTH = 0;
    public float HEADER_TITLE_HEIGHT = 0;
    public float FOOTER_HEIGHT = 0;

    public float HEADER_TITLE_X = 0;
    public float HEADER_TITLE_Y = 0;

    public float HEADER_IMAGE_X = 5;
    public float HEADER_IMAGE_Y = 0;

    public float PAD = 5;

    public int pageNumber = 0;

    ReportData reportData;

    float pageWidth = 0;
    float pageHeight = 0;

    Locale locale;

    PdfTemplate totalPages;

    String title = null;

    String copyright;

    ReportSettings reportSettings;



    public StandardHeaderFooter( Document d , Locale l, String t, ReportData rd, ReportSettings rs ) throws Exception
    {
        reportData = rd;

        reportSettings = rs;

        pageWidth = d.getPageSize().getWidth();
        pageHeight = d.getPageSize().getHeight();

        locale = l == null ? Locale.US : l;

        title = t;

        // dateStr = I18nUtils.getFormattedDate(locale, new Date() );

        Calendar c = new GregorianCalendar();

        copyright = MessageFactory.getStringMessage(locale, "g.copyrightforreport", new String[]{ Integer.toString( c.get( Calendar.YEAR) ), RuntimeConstants.getStringValue("default-site-name") } );
    }


    @Override
    public void onOpenDocument( PdfWriter w, Document d )
    {
        totalPages = w.getDirectContent().createTemplate(30, 16);
    }

    @Override
    public void onStartPage( PdfWriter w, Document d )
    {
        pageNumber++;

        //LogService.logIt( "HeaderFooter.onStartPage() pageNumber=" + pageNumber );

        // create background
        if( pageNumber == 1 )
            return;

        try
        {
            ITextUtils.addDirectColorRect( w, reportSettings.getPageBgColor(), 0, 0, pageWidth, pageHeight, 0, 1, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "HeaderFooter.onStartPage() drawing background. " );
        }
    }


    public void init( PdfWriter w ) throws Exception
    {
        if( HEADER_HEIGHT == 0 )
        {
            HEADER_TITLE_X = 100 + pageWidth/2;
            HEADER_TITLE_WIDTH = pageWidth - PAD - HEADER_TITLE_X;

            Font fnt = reportSettings.getHeaderFontLargeWhite();

            float ldg = fnt.getSize() + 1;

            HEADER_TITLE_HEIGHT = ITextUtils.getDirectTextHeight( w, title, HEADER_TITLE_WIDTH, Element.ALIGN_LEFT, ldg, fnt );

            HEADER_HEIGHT = Math.max( reportSettings.getHraLogoWhiteTextSmall().getScaledHeight(), HEADER_TITLE_HEIGHT ) + 10;

            HEADER_IMAGE_X = 10;
            HEADER_IMAGE_Y = pageHeight - ((HEADER_HEIGHT - reportSettings.getHraLogoWhiteTextSmall().getScaledHeight())/2 ) - reportSettings.getHraLogoWhiteTextSmall().getScaledHeight();

            HEADER_TITLE_Y = pageHeight - ((HEADER_HEIGHT - HEADER_TITLE_HEIGHT)/2 ) - HEADER_TITLE_HEIGHT + 2; // - CTReportSettings.headerFontLarge.getSize();

            // LogService.logIt( "CTHEaderFooter.init() HEADER_HEIGHT=" + HEADER_HEIGHT + ", HEADER_TITLE_HEIGHT=" + HEADER_TITLE_HEIGHT + ", HEADER_TITLE_Y=" + HEADER_TITLE_Y );

            PdfPTable t = getFooterTable();

            FOOTER_HEIGHT = t.getTotalHeight();
        }
    }

    public float[] getHeaderFooterHeights( PdfWriter w ) throws Exception
    {
        init( w );
        return new float[] {HEADER_HEIGHT,FOOTER_HEIGHT};
    }



    @Override
    public void onEndPage( PdfWriter w, Document d )
    {
        try
        {
            // Skip first page altogether.
            if( pageNumber <= 1 )
                return;

            // PdfContentByte under = w.getDirectContentUnder();

            // First data page has a dark header at the top.
            if( pageNumber >= 2 ) // if( pageNumber == 2 )
                addHeaderDark( w, d );

            // other pages have light header
            else
                addHeader( w, d );

            // create the footer
            addFooter( w, d );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "HeaderFooter.onEndPage() ");
        }

    }

    @Override
    public void onCloseDocument( PdfWriter w, Document d )
    {
        // LogService.logIt( "HeaderFooter.onCloseDocument() " );

        ColumnText.showTextAligned( totalPages, Element.ALIGN_LEFT, new Phrase( String.valueOf( w.getPageNumber()-1), reportSettings.getFontSmallLightItalic() ), 0, 10, 0 );
    }

    private void addHeaderDark( PdfWriter w, Document d )
    {
        createHeader( w, d, true );
        // LogService.logIt( "HeaderFooter.addHeaderDark() pageNumber=" + pageNumber + ", HEADER_HEIGHT=" + HEADER_HEIGHT + ", HEADER_TITLE_HEIGHT=" + HEADER_TITLE_HEIGHT + ", HEADER_TITLE_Y=" + HEADER_TITLE_Y + ", HEADER_IMAGE_Y=" + HEADER_IMAGE_Y );
    }

    private void addHeader( PdfWriter w, Document d )
    {
        createHeader( w, d, false );
        // LogService.logIt( "HeaderFooter.addHeader() DONE pageNumber=" + pageNumber );
    }


    private void createHeader( PdfWriter w, Document d, boolean dark )
    {
        try
        {
            init( w );

            Font fnt = dark ? reportSettings.getHeaderFontLargeWhite() : reportSettings.getHeaderFontLarge();

            // add the title in upper layer
            Rectangle rect = new Rectangle( HEADER_TITLE_X, HEADER_TITLE_Y, HEADER_TITLE_X + HEADER_TITLE_WIDTH, HEADER_TITLE_Y + HEADER_TITLE_HEIGHT  );
            ITextUtils.addDirectText( w, title, rect, Element.ALIGN_LEFT, fnt.getSize() + 1, fnt, false );

            // Create the background in lower layer
            if( dark )
                ITextUtils.addDirectColorRect(w, reportSettings.getHeaderDarkBgColor(), 0, pageHeight - HEADER_HEIGHT, pageWidth, HEADER_HEIGHT, 0, 1, true);

            // Add the logo on top of the background in lower layer
            ITextUtils.addDirectImage( w, dark ? reportSettings.getHraLogoWhiteTextSmall() : reportSettings.getHraLogoBlackTextSmall() , HEADER_IMAGE_X, HEADER_IMAGE_Y, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "HeaderFooter.createHeader() dark=" + dark );
        }
    }







    private void addFooter( PdfWriter w, Document d )
    {
        try
        {
            init( w );

            // First, add a table
            PdfPTable t = getFooterTable();

            // LogService.logIt( "HeaderFooter.addFooter() pageNumber=" + pageNumber + ", t.getTotalHeight()=" + t.getTotalHeight() );

            t.writeSelectedRows(0, -1, PAD, FOOTER_HEIGHT + 2, w.getDirectContent() );

            // LogService.logIt( "HeaderFooter.addFooter() DONE pageNumber=" + pageNumber );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "HeaderFooter.addFooter() " );
        }
    }



    private PdfPTable getFooterTable()
    {
        // First, add a table
        PdfPTable t = new PdfPTable( 3 );

        try
        {
            //  t.setTableEvent( tableEvent );
            Font f = reportSettings.getFontSmallLightItalic();
            t.setWidths( reportData.getIsLTR() ? new int[] { 24,24,2 } :new int[] { 2,24,24 } );

            t.setTotalWidth( pageWidth - 2*PAD );
            t.setLockedWidth(true);
            t.setHorizontalAlignment( Element.ALIGN_CENTER );

            PdfPCell dc = t.getDefaultCell();

            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding( 2 );
            Phrase p = new Phrase( copyright, f );
            t.addCell(p);

            dc.setHorizontalAlignment( Element.ALIGN_RIGHT );
            p = new Phrase( MessageFactory.getStringMessage( reportData.getLocale(), "g.PageXOf", new String[] {Integer.toString(pageNumber)} ), f );
            t.addCell(p);


            if( totalPages != null )
            {
                PdfPCell c = new PdfPCell( Image.getInstance( totalPages ) );
                c.setBorderWidth(0);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setBorder( Rectangle.NO_BORDER );
                // c.setPadding( 2 );
                c.setPaddingTop(3);
                t.addCell(c);
            }

            else
                t.addCell( new Phrase( "X", f ) );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "HeaderFooter.getFooterTable() ");
        }

        return t;
    }
}
