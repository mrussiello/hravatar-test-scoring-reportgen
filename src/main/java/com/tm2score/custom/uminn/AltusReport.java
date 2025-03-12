 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class AltusReport extends BaseUMinnReportTemplate implements ReportTemplate
{

    public AltusReport()
    {
        super();
        initForSource();
    }


    
    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            addCoverPage();

            addNewPage();

            addContents();
            
            addNewPage();

            addPCModelOverview();

            addNewPage();
            
            addFeedbackReportOverview();
            
            addOverallSummary();
            
            addCompetencySummary();
            
            addComparison();

            // addNewPage();
            
            addCompetencyDetails();

            addNewPage();
            
            addScenarioLevelReport();
            
            addPreparationNotesSection();

            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AltusReport.generateReport() " );
            throw new STException( e );
        }
    }

    
    
    @Override
    public synchronized void initFonts() throws Exception
    {
        LogService.logIt( "AltusReport.initFonts() " );
        fontTypeId=1;
        
        initSettings( reportData );
        
        initExtra( reportData );
        
    }

    
    
    
    @Override
    public void addCoverPage() throws Exception
    {
        try
        {
            //ITextUtils.addDirectColorRect( pdfWriter, uminnMaroon, 0, pageHeight-100, pageWidth, 100, 0, 1, true );            
            
            float y = pageHeight; // = pageHeight - headerLogo.getScaledHeight() - 10;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();
            
            //java.util.List<Chunk> cl = new ArrayList<>();

            //cl.add( new Chunk( lmsg( "g.Univ" ), titleHeaderFont) );

            //cl.add( new Chunk( lmsg( "g.driven" ), titleHeaderFont2 ) );
           
            float tableWid = pageWidth; // ITextUtils.getMaxChunkWidth( cl ) + 20 + headerLogo.getScaledWidth();

            //if( tableWid > pageWidth-120 )
            //    tableWid = pageWidth - 120;            
            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( 1 );  
            t.setWidths(new float[] { 100 } );
            t.setTotalWidth( tableWid );
            t.setLockedWidth( true );
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(t);
            
            //c = new PdfPCell( new Phrase("") );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setPadding( 5 );
            //c.setHorizontalAlignment( Element.ALIGN_CENTER );
            //c.setBackgroundColor(uminnMaroon);
            //setRunDirection( c );
            //t.addCell(c); 
            


            Image reportHeaderLogo = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( "https://cdn.hravatar.com/web/orgimage/Q8vQJ8K3q0E-/img_2x1641675046535.png" ) );
            reportHeaderLogo.scalePercent(50);
            c = new PdfPCell( reportHeaderLogo );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 8 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor(uminnMaroon);
            setRunDirection( c );
            t.addCell(c);            

            //Phrase ph = new Phrase( lmsg( "g.Univ" + altusKeySuffix ), titleHeaderFont);
            
            //if( !useAltusLangKeys )
            //    ph.add( new Chunk( "\n" + lmsg( "g.driven" ), titleHeaderFont2));
            
            //c = new PdfPCell( ph );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setPadding( 5 );            
            //c.setHorizontalAlignment( Element.ALIGN_CENTER );
            //c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            //c.setBackgroundColor(uminnMaroon);
            //setRunDirection( c );
            //t.addCell(c);            

            //c = new PdfPCell( new Phrase( "", titleHeaderFont2) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setPadding( 5 );
            //c.setHorizontalAlignment( Element.ALIGN_CENTER );
            //c.setBackgroundColor(uminnMaroon);
            //setRunDirection( c );
            //t.addCell(c);            
            
            int pad = 4;
            
            float tableH = t.calculateHeights(); //  + 500;

            float tableY = pageHeight; // -tableH; //  + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = 0; // (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );            

            // ITextUtils.addDirectColorRect( pdfWriter, uminnMaroon, 0, pageHeight-tableH-2*pad, pageWidth, tableH+2*pad, 0, 1, true );            
            
            // ITextUtils.addDirectImage( pdfWriter, headerLogo, ( pageWidth-headerLogo.getScaledWidth())/2 , y, false );

            y = pageHeight-tableH-2*pad - 150;
            
            Date testDate = reportData.te.getLastAccessDate();
            
            Calendar cal = new GregorianCalendar();
            
            cal.setTime(testDate);
            
            DateFormatSymbols dfs = new DateFormatSymbols();
            
            String monthYearStr = dfs.getMonths()[cal.get(Calendar.MONTH)] + ( useAltusLangKeys ? " " : ", ")  + cal.get( Calendar.YEAR);
                        
            java.util.List<Chunk> cl  = new ArrayList<>();

            cl.add( new Chunk( lmsg( "title" ), headerFontXXLargeMaroon ) );

            cl.add( new Chunk( lmsg( "g.PreparedForC" ), headerFontXLargeMaroon ) );

            cl.add( new Chunk( reportData.getUserName(), headerFontXLargeMaroon ) );
            
            cl.add( new Chunk( monthYearStr, headerFontXLargeMaroon ) );
           
            tableWid = ITextUtils.getMaxChunkWidth( cl ) + 20;

            if( tableWid > pageWidth-120 )
                tableWid = pageWidth - 120;
            
            // First create the table
            // PdfPCell c;

            // First, add a table
            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { tableWid } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 5 );
            // c.setPaddingRight( 15 );
            // c.setPaddingBottom( 25 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            
            t.addCell( new Phrase( lmsg( "title" + altusKeySuffix ), headerFontXXLargeMaroon ) );
            
            t.addCell( new Phrase( "\n\n\n\n\n" + lmsg( "g.PreparedForC" ), headerFontXLargeMaroon ) );
            
            t.addCell( new Phrase( "\n" + reportData.getUserName(), headerFontXLargeMaroon ) );
            
            t.addCell( new Phrase( "\n" + monthYearStr, headerFontXLargeMaroon ) );
            
            tableH = t.calculateHeights(); //  + 500;

            tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            
            
            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, getHraOrangeColor(), false );


            t = new PdfPTable( 2 );
            
            // t.setWidths( new int[] {50,200} );
            
            cl.clear();

            cl.add( new Chunk( " ", fontXLargeBoldWhite ) );
            // cl.add( new Chunk( lmsg( "url" + altusKeySuffix ), fontXLargeBoldWhite ) );

            tableWid = ITextUtils.getMaxChunkWidth( cl ) + twitterLogo.getScaledWidth() + 10;            

            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            
            t.setWidths( new float[]{twitterLogo.getScaledWidth(),  ITextUtils.getMaxChunkWidth( cl )+10});
            t.setTotalWidth( tableWid );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 2 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            setRunDirection(c);
                             
            t.addCell( new Phrase( " ", fontXLargeBoldWhite ) );
            t.addCell( new Phrase( " ", fontXLargeBoldWhite ) );
            // t.addCell( new Phrase( lmsg( "url" + altusKeySuffix ), fontXLargeBoldWhite ) );
            
            tableH = t.calculateHeights(); //  + 500;

            tableY = tableH + 12; //   pageHeight/2 - (pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            // Add the maroon below
            ITextUtils.addDirectColorRect( pdfWriter, getHraBaseReportColor(), 0, 0, pageWidth, tableH + 20, 0, 1, true );

            
        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addCoverPage()" );
        }
    }
    
    
    
    
    
    
    @Override
    public void initForSource()
    {
        useAltusLangKeys = true;
        altusKeySuffix = ".altus";
        // Use all default. Nothing to do here.
        logoUrl = "https://cdn.hravatar.com/web/orgimage/Q8vQJ8K3q0E-/img_29x1639434689694.png";  // darktext-on-white 150  
        pcmUrl = "https://cdn.hravatar.com/web/orgimage/Q8vQJ8K3q0E-/img_34x1639440107136.png";    // diagram
        headerLogoUrl = "https://cdn.hravatar.com/web/orgimage/Q8vQJ8K3q0E-/img_31x1639434689762.png";    // logo white text-transparent 180  
        headerLogoWhiteTransUrl = "https://cdn.hravatar.com/web/orgimage/Q8vQJ8K3q0E-/img_31x1639434689762.png "; // logo white text-transparent 180
        // String twitterLogoUrl = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Q8vQJ8K3q0E-/img_13x1481106795247.jpg";   // twitter
        uminnMaroonColStr = "4c55a5";        
        uminnMaroon = new BaseColor( 0x4c, 0x55, 0xa5 );    
        uminnMaroonLite = new BaseColor( 0xda, 0xdd, 0xff );  // f5b7c3
    
        /*
        String filesRoot = RuntimeConstants.getStringValue("filesroot") + "/coretest/fonts/";
        
        try
        {
            baseFont = BaseFont.createFont(filesRoot + "arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
            baseFontCalibri = BaseFont.createFont(filesRoot + "arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            baseFontCalibriBold = BaseFont.createFont(filesRoot + "arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            baseFontCalibriItalic = BaseFont.createFont(filesRoot + "ariali.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "arialbi.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            headerBaseFont = BaseFont.createFont(filesRoot + "arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); 
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AltusReport.initForSource() " );
        }
        */
        

    }
    
    

}
