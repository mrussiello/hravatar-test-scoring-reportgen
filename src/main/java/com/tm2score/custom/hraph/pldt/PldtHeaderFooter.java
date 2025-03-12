/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.pldt;

import com.tm2score.custom.coretest2.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.custom.coretest.ITextUtils;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.report.ReportSettings;
import com.tm2score.report.ReportData;
import com.tm2score.service.LogService;
import java.util.Locale;


/**
 * Use this class to make any changes to CoreTest that are different than standard report.
 *
 * @author Mike
 */
public class PldtHeaderFooter extends PdfPageEventHelper
{
    public float CT2_MARGIN = CT2ReportSettings.CT2_MARGIN;

    public float HEADER_HEIGHT = 0;
    public float HEADER_TITLE_WIDTH = 0;
    public float HEADER_TITLE_HEIGHT = 0;
    //public float FOOTER_HEIGHT = 0;

    public float HEADER_TITLE_X = 0;
    public float HEADER_TITLE_Y = 0;

    public float HEADER_IMAGE_X = CT2_MARGIN;
    public float HEADER_IMAGE_Y = 0;
    public float HEADER_CLNTIMAGE_MAXHGT = 40;
   
    public float PAD = 5;

    // public int pageNumber = 0;

    ReportData reportData;

    float pageWidth = 0;
    float pageHeight = 0;

    // Locale rptLocale;

    PdfTemplate totalPages;
    
    final static String hdrRightText = "Confidential";

    // String title = null;

    // String reportTitle = null;

    ReportSettings reportSettings;
    
    Image custLogo = null;


    public PldtHeaderFooter( Document d , Locale l, String t, ReportData rd, ReportSettings rs, Image custLogo ) throws Exception
    {
        reportData = rd;

        reportSettings = rs;

        pageWidth = d.getPageSize().getWidth();
        pageHeight = d.getPageSize().getHeight();

        // rptLocale = l == null ? Locale.US : l;

        // reportTitle = t;

        // dateStr = I18nUtils.getFormattedDate(locale, new Date() );

        //Calendar c = new GregorianCalendar();

        //copyright = lmsg( "g.copyrightforreport", new String[]{ Integer.toString( c.get( Calendar.YEAR) ), RuntimeConstants.getStringValue("default-site-name") } );
        
        // copyright = MessageFactory.getStringMessage(rptLocale, "g.copyrightforreport", new String[]{ Integer.toString( c.get( Calendar.YEAR) ), RuntimeConstants.getStringValue("default-site-name") } );

        // LogService.logIt( "CT2HeaderFooter() t=" + t );
        
        //if( t == null || t.isEmpty() )
        //    t = rd.getReportName();
        
        //title = t;
        
        //if( title==null || title.isEmpty() )
        //{
        //    title = lmsg( "g.TestResultsAndInterviewGuide", new String[]{ Integer.toString( c.get( Calendar.YEAR) ) } );
            // title = MessageFactory.getStringMessage( rptLocale, "g.TestResultsAndInterviewGuide", new String[]{ Integer.toString( c.get( Calendar.YEAR) ) } );
        //}

        this.custLogo=custLogo;
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
        // pageNumber++;

        //LogService.logIt( "HeaderFooter.onStartPage() pageNumber=" + pageNumber );

        // create background
        //if( pageNumber == 1 )
        //    return;

        //try
        //{
        //    ITextUtils.addDirectColorRect( w, reportSettings.getPageBgColor(), 0, 0, pageWidth, pageHeight, 0, 1, true );
        //}

        //catch( Exception e )
        //{
        //    LogService.logIt( e, "HeaderFooter.onStartPage() drawing background. " );
        //}
    }


    public void init( PdfWriter w ) throws Exception
    {
        if( HEADER_HEIGHT == 0 )
        {
            Font fnt = reportSettings.getFontLargeItalic();
            float ldg = fnt.getSize() + 1;
            // HEADER_TITLE_X = 100 + pageWidth/2;
            HEADER_TITLE_WIDTH = ITextUtils.getChunckWidth( new Chunk( hdrRightText, fnt) );   // pageWidth - PAD - HEADER_TITLE_X;
            HEADER_TITLE_HEIGHT = ITextUtils.getDirectTextHeight( w,  hdrRightText, HEADER_TITLE_WIDTH, Element.ALIGN_LEFT, ldg, fnt );

            HEADER_TITLE_X = pageWidth - CT2_MARGIN - HEADER_TITLE_WIDTH;

            HEADER_HEIGHT = Math.max( custLogo==null ? 0 : custLogo.getScaledHeight(), HEADER_TITLE_HEIGHT ) + 10;

            HEADER_IMAGE_Y = pageHeight - ((HEADER_HEIGHT - (custLogo==null ? 0 : custLogo.getScaledHeight()))/2 ) - (custLogo==null ? 0 : custLogo.getScaledHeight()) - 1;
            HEADER_TITLE_Y = pageHeight - ((HEADER_HEIGHT - HEADER_TITLE_HEIGHT)/2 ) - HEADER_TITLE_HEIGHT + 2; // - CTReportSettings.headerFontLarge.getSize();

            // LogService.logIt( "CTHEaderFooter.init() HEADER_HEIGHT=" + HEADER_HEIGHT + ", HEADER_TITLE_HEIGHT=" + HEADER_TITLE_HEIGHT + ", HEADER_TITLE_Y=" + HEADER_TITLE_Y );

            //PdfPTable t = getFooterTable();

            //FOOTER_HEIGHT = t.getTotalHeight();
        }
    }

    public float[] getHeaderFooterHeights( PdfWriter w ) throws Exception
    {
        init( w );
        return new float[] {HEADER_HEIGHT,0};
        // return new float[] {HEADER_HEIGHT,FOOTER_HEIGHT};
    }



    @Override
    public void onEndPage( PdfWriter w, Document d )
    {
        try
        {
            // Skip first page altogether.
            //if( pageNumber <= 1 )
            //    return;

            // PdfContentByte under = w.getDirectContentUnder();

            // First data page has a dark header at the top.
            //if( pageNumber >= 2 ) // if( pageNumber == 2 )
            //    addHeaderDark( w, d );

            // other pages have light header
            //else
                addHeader( w, d );

            // create the footer
            // addFooter( w, d );
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

        // ColumnText.showTextAligned( totalPages, Element.ALIGN_LEFT, new Phrase( String.valueOf( w.getPageNumber()-1), reportSettings.getFontSmallLightItalic() ), 0, 10, 0 );
    }

    //private void addHeaderDark( PdfWriter w, Document d )
    //{
    //    createHeader( w, d, true );
        // LogService.logIt( "HeaderFooter.addHeaderDark() pageNumber=" + pageNumber + ", HEADER_HEIGHT=" + HEADER_HEIGHT + ", HEADER_TITLE_HEIGHT=" + HEADER_TITLE_HEIGHT + ", HEADER_TITLE_Y=" + HEADER_TITLE_Y + ", HEADER_IMAGE_Y=" + HEADER_IMAGE_Y );
    //}

    private void addHeader( PdfWriter w, Document d )
    {
        createHeader( w, d );    /// was false, but all dark now
        // LogService.logIt( "HeaderFooter.addHeader() DONE pageNumber=" + pageNumber );
    }


    private void createHeader( PdfWriter w, Document d )
    {
        try
        {
            init( w );

            // First, add a table
            PdfPTable t = getHeaderTable();

            //2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN 
            t.writeSelectedRows(0, -1, CT2_MARGIN+CT2_BOX_EXTRAMARGIN-5, pageHeight - PAD - 1, w.getDirectContent() );
            // t.writeSelectedRows(0, -1, PAD, pageHeight - PAD - 1, w.getDirectContent() );

            // float hgt = t.getTotalHeight();

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

            //  ITextUtils.addDirectColorRect(w, reportSettings.getHeaderDarkBgColor(), 0, pageHeight - hgt - 2*PAD - 1, pageWidth, hgt + 2*PAD + 1, 0, 1, true);


            // Add the logo on top of the background in lower layer
            //ITextUtils.addDirectImage( w, dark ? reportSettings.getHraLogoWhiteTextSmall() : reportSettings.getHraLogoBlackTextSmall() , HEADER_IMAGE_X, HEADER_IMAGE_Y, true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "HeaderFooter.createHeader()");
        }
    }


    private PdfPTable getHeaderTable()
    {
        // First, add a table
        PdfPTable t = new PdfPTable( 3 );

        try
        {
            t.setWidths( new int[] { 25, 30, 55 } );

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            
            // t.setTotalWidth( pageWidth - 2*PAD );
            t.setLockedWidth(true);
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setRunDirection( reportData.getTextRunDirection() );

            PdfPCell dc = t.getDefaultCell();

            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setBorder( Rectangle.NO_BORDER );
            // dc.setBackgroundColor( reportSettings.getHeaderDarkBgColor() );
            dc.setPadding( 2 );
            dc.setRunDirection( reportData.getTextRunDirection() );

            //LogoCell
            PdfPCell c = custLogo==null ? new PdfPCell(new Phrase("")) : new PdfPCell( custLogo );
            //c.setBackgroundColor( reportSettings.getHeaderDarkBgColor() );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setPadding( 1 );
            c.setPaddingTop( 5 );
            c.setRunDirection( reportData.getTextRunDirection() );
            t.addCell( c );

            //Middle Cell
            t.addCell( " " );

            //Right Cell
            Font fnt = reportSettings.getFontLargeItalic();

            c = new PdfPCell( new Phrase( hdrRightText , fnt ) );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );                
            c.setRunDirection( reportData.getTextRunDirection() );
            t.addCell( c );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "PldtHeaderFooter.getHeaderTable() ");
        }
        return t;
    }




}
