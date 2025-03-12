/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2score.custom.coretest2.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.custom.coretest.ITextUtils;
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
public class UMinnHeaderFooter extends PdfPageEventHelper
{
    public float CT2_MARGIN = CT2ReportSettings.CT2_MARGIN;

    public float HEADER_HEIGHT = 0;
    public float HEADER_TITLE_WIDTH = 0;
    public float HEADER_TITLE_HEIGHT = 0;
    public float FOOTER_HEIGHT = 0;

    public float HEADER_TITLE_X = 0;
    public float HEADER_TITLE_Y = 0;

    public float HEADER_IMAGE_X = CT2_MARGIN;
    public float HEADER_IMAGE_Y = 0;

    public float PAD = 5;

    public int pageNumber = 0;

    ReportData reportData;

    float pageWidth = 0;
    float pageHeight = 0;

    Locale locale;

    PdfTemplate totalPages;

    String title = null;

    String reportTitle = null;

    String copyright;

    UMinnReportSettings reportSettings;
    
    UMinnReportUtils uminnReportUtils;
    



    public UMinnHeaderFooter( Document d , Locale l, String t, ReportData rd, UMinnReportSettings rs ) throws Exception
    {
        reportData = rd;

        reportSettings = rs;

        pageWidth = d.getPageSize().getWidth();
        pageHeight = d.getPageSize().getHeight();

        locale = l == null ? Locale.US : l;

        reportTitle = t;
        
        // dateStr = I18nUtils.getFormattedDate(locale, new Date() );

        Calendar c = new GregorianCalendar();

        copyright = lmsg( "g.copyrightforreport" + reportSettings.altusKeySuffix, new String[]{ Integer.toString( c.get( Calendar.YEAR) ) } );

        // LogService.logIt( "CT2HeaderFooter() t=" + t );
        
        if( t == null || t.isEmpty() )
            t = rd.getReportName();
        
        title =  lmsg( "g.headerreporttitle" + reportSettings.altusKeySuffix, new String[]{ reportData.getUserName() } );
        
        // t==null || t.isEmpty() ? MessageFactory.getStringMessage(locale, "g.TestResultsAndInterviewGuide", new String[]{ Integer.toString( c.get( Calendar.YEAR) ) } ) : t;

        // LogService.logIt( "CT2HeaderFooter() title=" + title );
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

        //LogService.logIt( "UMinnHeaderFooter.onStartPage() pageNumber=" + pageNumber );

        // create background
        if( pageNumber == 1 )
            return;

        try
        {
            // ITextUtils.addDirectColorRect( w, reportSettings.getPageBgColor(), 0, 0, pageWidth, pageHeight, 0, 1, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.onStartPage() drawing background. " );
        }
    }


    public void init( PdfWriter w ) throws Exception
    {
        if( HEADER_HEIGHT == 0 )
        {
            Font fnt = reportSettings.getHeaderFontLargeWhite();

            float ldg = fnt.getSize() + 1;
            // HEADER_TITLE_X = 100 + pageWidth/2;
            HEADER_TITLE_WIDTH = ITextUtils.getChunckWidth( new Chunk(title, fnt) );   // pageWidth - PAD - HEADER_TITLE_X;
            HEADER_TITLE_HEIGHT = ITextUtils.getDirectTextHeight( w, title, HEADER_TITLE_WIDTH, Element.ALIGN_LEFT, ldg, fnt );

            HEADER_TITLE_X = pageWidth - CT2_MARGIN - HEADER_TITLE_WIDTH;

            HEADER_HEIGHT = Math.max( reportSettings.getHraLogoWhiteTextSmall().getScaledHeight(), HEADER_TITLE_HEIGHT ) + 20;

            HEADER_IMAGE_Y = pageHeight - ((HEADER_HEIGHT - reportSettings.getHraLogoWhiteTextSmall().getScaledHeight())/2 ) - reportSettings.getHraLogoWhiteTextSmall().getScaledHeight() - 1;
            HEADER_TITLE_Y = pageHeight - ((HEADER_HEIGHT - HEADER_TITLE_HEIGHT)/2 ) - HEADER_TITLE_HEIGHT + 2; // - CTReportSettings.headerFontLarge.getSize();

            // LogService.logIt( "CTHEaderFooter.init() HEADER_HEIGHT=" + HEADER_HEIGHT + ", HEADER_TITLE_HEIGHT=" + HEADER_TITLE_HEIGHT + ", HEADER_TITLE_Y=" + HEADER_TITLE_Y );

            PdfPTable t = getFooterTable();

            FOOTER_HEIGHT = t.getTotalHeight();

            // LogService.logIt( "CTHEaderFooter.init() HEADER_HEIGHT=" + HEADER_HEIGHT + ", HEADER_TITLE_HEIGHT=" + HEADER_TITLE_HEIGHT + ", HEADER_TITLE_Y=" + HEADER_TITLE_Y + " FOOTER_HEIGHT=" + FOOTER_HEIGHT );
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
            LogService.logIt(e, "UMinnHeaderFooter.onEndPage() ");
        }

    }

    @Override
    public void onCloseDocument( PdfWriter w, Document d )
    {
        // LogService.logIt( "UMinnHeaderFooter.onCloseDocument() " );

        ColumnText.showTextAligned( totalPages, Element.ALIGN_LEFT, new Phrase( String.valueOf( w.getPageNumber()-1), reportSettings.getFontSmallLightItalic() ), 0, 10, 0 );
    }

    private void addHeaderDark( PdfWriter w, Document d )
    {
        createHeader( w, d, true );
        // LogService.logIt( "UMinnHeaderFooter.addHeaderDark() pageNumber=" + pageNumber + ", HEADER_HEIGHT=" + HEADER_HEIGHT + ", HEADER_TITLE_HEIGHT=" + HEADER_TITLE_HEIGHT + ", HEADER_TITLE_Y=" + HEADER_TITLE_Y + ", HEADER_IMAGE_Y=" + HEADER_IMAGE_Y );
    }

    private void addHeader( PdfWriter w, Document d )
    {
        createHeader( w, d, true );    /// was false, but all dark now
        // LogService.logIt( "UMinnHeaderFooter.addHeader() DONE pageNumber=" + pageNumber );
    }


    private void createHeader( PdfWriter w, Document d, boolean dark )
    {
        try
        {
            init( w );

            // First, add a table
            PdfPTable t = getHeaderTable( dark );

            t.writeSelectedRows(0, -1, PAD, pageHeight - PAD - 1, w.getDirectContent() );

            float hgt = t.getTotalHeight();

            // Font fnt = dark ? reportSettings.getHeaderFontLargeWhite() : reportSettings.getHeaderFontLarge();

            // add the title in upper layer
            //if( dark )
            //{


                //Rectangle rect = new Rectangle( HEADER_TITLE_X, HEADER_TITLE_Y, HEADER_TITLE_X + HEADER_TITLE_WIDTH, HEADER_TITLE_Y + HEADER_TITLE_HEIGHT  );
                //ITextUtils.addDirectText( w, title, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt.getSize() + 1, fnt, false );
            //}

            // Create the background in lower layer
            //if( 1==2 && dark )
            //    ITextUtils.addDirectColorRect(w, reportSettings.getHeaderDarkBgColor(), 0, pageHeight - HEADER_HEIGHT, pageWidth, HEADER_HEIGHT, 0, 1, true);

             ITextUtils.addDirectColorRect(w, reportSettings.getHeaderDarkBgColor(), 0, pageHeight - hgt - 2*PAD - 1, pageWidth, hgt + 2*PAD + 1, 0, 1, true);


            // Add the logo on top of the background in lower layer
            //ITextUtils.addDirectImage( w, dark ? reportSettings.getHraLogoWhiteTextSmall() : reportSettings.getHraLogoBlackTextSmall() , HEADER_IMAGE_X, HEADER_IMAGE_Y, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.createHeader() dark=" + dark );
        }
    }


    private PdfPTable getHeaderTable( boolean dark )
    {
        // First, add a table
        PdfPTable t = new PdfPTable( 3 );

        try
        {
            //if( !reportData.getIsLTR() )
            //    return getHeaderTableRTL();

            //  t.setTableEvent( tableEvent );
            //Font f = reportSettings.getFontSmallLightItalic();
            t.setWidths( reportData.getIsLTR()? new int[] { 12, 30, 50 } : new int[] { 20, 60, 24 } );
            // t.setWidths( reportData.getIsLTR() ?  new int[] { 24,24,2 } :new int[] { 2,24,24 } );

            t.setTotalWidth( pageWidth - 2*PAD );
            t.setLockedWidth(true);
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setRunDirection( reportData.getTextRunDirection() );

            PdfPCell dc = t.getDefaultCell();

            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setBackgroundColor( reportSettings.getHeaderDarkBgColor() );
            dc.setPadding( 2 );
            dc.setRunDirection( reportData.getTextRunDirection() );

            //LogoCell
            PdfPCell c = new PdfPCell( reportSettings.getHraLogoWhiteTextSmall() );
            c.setBackgroundColor( reportSettings.getHeaderDarkBgColor() );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setPadding( 5 );
            //c.setPaddingTop( 5 );
            //c.setPaddingBottom( 5 );
            c.setRunDirection( reportData.getTextRunDirection() );
            t.addCell( c );

            //Middle Cell
            t.addCell( " " );

            //Right Cell
            Font fnt = dark ? reportSettings.getHeaderFontXLargeWhite() : reportSettings.getHeaderFontLarge();

            c = new PdfPCell( new Phrase( title , fnt ) );
            c.setBackgroundColor( reportSettings.getHeaderDarkBgColor() );
            c.setBorder( Rectangle.NO_BORDER );
            c.setRunDirection( reportData.getTextRunDirection() );
            c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setPadding( 1 );
            c.setPaddingRight( 6 );
            t.addCell( c );


        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.getHeaderTable() ");
        }

        return t;
    }


    /*
    private void createHeaderOld( PdfWriter w, Document d, boolean dark )
    {
        try
        {
            init( w );

            Font fnt = dark ? reportSettings.getHeaderFontLargeWhite() : reportSettings.getHeaderFontLarge();

            // add the title in upper layer
            if( dark )
            {
                Rectangle rect = new Rectangle( HEADER_TITLE_X, HEADER_TITLE_Y, HEADER_TITLE_X + HEADER_TITLE_WIDTH, HEADER_TITLE_Y + HEADER_TITLE_HEIGHT  );
                ITextUtils.addDirectText( w, title, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt.getSize() + 1, fnt, false );
            }

            // Create the background in lower layer
            if( dark )
                ITextUtils.addDirectColorRect(w, reportSettings.getHeaderDarkBgColor(), 0, pageHeight - HEADER_HEIGHT, pageWidth, HEADER_HEIGHT, 0, 1, true);

            // Add the logo on top of the background in lower layer
            ITextUtils.addDirectImage( w, dark ? reportSettings.getHraLogoWhiteTextSmall() : reportSettings.getHraLogoBlackTextSmall() , HEADER_IMAGE_X, HEADER_IMAGE_Y, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.createHeader() dark=" + dark );
        }
    }
    */







    private void addFooter( PdfWriter w, Document d )
    {
        try
        {
            init( w );

            // First, add a table
            PdfPTable t = getFooterTable();


            // LogService.logIt( "UMinnHeaderFooter.addFooter() pageNumber=" + pageNumber + ", t.getTotalHeight()=" + t.getTotalHeight() );

            t.writeSelectedRows(0, -1, PAD, FOOTER_HEIGHT + 2, w.getDirectContent() );

            // LogService.logIt( "UMinnHeaderFooter.addFooter() DONE pageNumber=" + pageNumber );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.addFooter() " );
        }
    }



    private PdfPTable getFooterTable()
    {
        // First, add a table
        PdfPTable t = new PdfPTable( 2 );

        try
        {
            if( !reportData.getIsLTR() )
                return getFooterTableRTL();

            //  t.setTableEvent( tableEvent );
            Font f = reportSettings.fontSmallLightItalicMaroon;
            t.setWidths( new int[] { 24, 24 } );
            // t.setWidths( reportData.getIsLTR() ?  new int[] { 24,24,2 } :new int[] { 2,24,24 } );

            t.setTotalWidth( pageWidth - 2*PAD );
            t.setLockedWidth(true);
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setRunDirection( reportData.getTextRunDirection() );

            PdfPCell dc = t.getDefaultCell();

            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding( 2 );
            dc.setRunDirection( reportData.getTextRunDirection() );

            Phrase p = new Phrase( copyright, f );
            t.addCell(p);

            dc.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT );
            p = new Phrase( MessageFactory.getStringMessage( reportData.getLocale(), "g.PageX", new String[] {Integer.toString(pageNumber)} ), reportSettings.fontSmallLightItalicMaroon );
            t.addCell(p);

            /*
            if( totalPages != null )
            {
                PdfPCell c = new PdfPCell( ITextUtils.getITextImage( totalPages ) );
                c.setBorderWidth(0);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setBorder( Rectangle.NO_BORDER );
                // c.setPadding( 2 );
                c.setPaddingTop(3);
                c.setRunDirection( reportData.getTextRunDirection() );
                t.addCell(c);
            }

            else
                t.addCell( new Phrase( "X", f ) );
            */
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.getFooterTable() ");
        }

        return t;
    }

    private PdfPTable getFooterTableRTL()
    {
        // First, add a table
        PdfPTable t = new PdfPTable( 2 );

        try
        {
            //  t.setTableEvent( tableEvent );
            Font f = reportSettings.getFontSmallLightItalic();
            //t.setWidths( new int[] { 24, 24, 2 } );
            t.setWidths( new int[] { 8, 48 } );
            // t.setWidths( reportData.getIsLTR() ?  new int[] { 24,24,2 } :new int[] { 2,24,24 } );

            t.setTotalWidth( pageWidth - 2*PAD );
            t.setLockedWidth(true);
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setRunDirection( reportData.getTextRunDirection() );

            PdfPCell dc = t.getDefaultCell();

            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding( 2 );
            dc.setRunDirection( reportData.getTextRunDirection() );



            Phrase p = new Phrase( copyright, f );
            t.addCell(p);

            //if( totalPages != null )
            //{
            //   String pages = MessageFactory.getStringMessage( reportData.getLocale(), "g.PageXOf", new String[] {Integer.toString(pageNumber)} ) + " " + totalPages;

            //    p = new Phrase( pages, f );
            //    t.addCell(p);
            //}
            //else
            //    t.addCell( new Phrase( " ", f ) );



            // dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            // dc.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT );
            //p = new Phrase( "Page X of", f );
            // p = new Phrase( MessageFactory.getStringMessage( reportData.getLocale(), "g.PageXOf", new String[] {Integer.toString(pageNumber)} ), f );
            //t.addCell(p);

            
            p = new Phrase( Integer.toString( pageNumber ), f );
            PdfPCell c = new PdfPCell( p );
            c.setBorderWidth(0);
            c.setRunDirection( reportData.getTextRunDirection() );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            // c.setPadding( 2 );
            c.setPaddingTop(3);
            t.addCell(c);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.getFooterTable() ");
        }

        return t;
    }



    /*
    private PdfPTable getHeaderTableRTL()
    {
        // First, add a table
        PdfPTable t = new PdfPTable( 2 );

        try
        {
            //  t.setTableEvent( tableEvent );
            Font f = reportSettings.getFontSmallLightItalic();
            t.setWidths( new int[] { 50, 50 } );
            // t.setWidths( reportData.getIsLTR() ?  new int[] { 24,24,2 } :new int[] { 2,24,24 } );

            t.setTotalWidth( pageWidth - 2*PAD );
            t.setLockedWidth(true);
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setRunDirection( reportData.getTextRunDirection() );

            PdfPCell dc = t.getDefaultCell();

            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setBackgroundColor( reportSettings.getHeaderDarkBgColor() );
            dc.setPadding( 2 );
            dc.setRunDirection( reportData.getTextRunDirection() );

            dc.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT );
            //p = new Phrase( "Page X of", f );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UMinnHeaderFooter.getHeaderTableRTL() ");
        }

        return t;
    }
    */

    public void initMessages()
    {
        if( this.uminnReportUtils==null )
            uminnReportUtils = new UMinnReportUtils();
    }


    public String lmsg( String key )
    {
        initMessages();
        
        return uminnReportUtils.getKey(key );
    }

    public String lmsg( String key, String[] prms )
    {
        initMessages();
        
        String msgText = uminnReportUtils.getKey(key );
        
        return MessageFactory.substituteParams(Locale.US , msgText, prms );
    }
    


}
