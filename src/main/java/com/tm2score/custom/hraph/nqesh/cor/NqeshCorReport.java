/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.nqesh.cor;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.custom.hraph.nqesh.fbk.NqeshReportUtils;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.TableBackground;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.report.ReportData;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import com.tm2score.util.QRCodeUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.JaxbUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import javax.imageio.ImageIO;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class NqeshCorReport extends BaseCT2ReportTemplate implements ReportTemplate
{    
    NqeshReportUtils nqeshReportUtils;
        
    static String headerText = "Republic of the Philippines\n" + 
                    "DEPARTMENT OF EDUCATION\n"+
                    "Bureau of Human Resources and Organizational Development\n";

    static String headerText2 = "Human Resource and Development Division\n";
    
    static String headerText3 = "4th Floor, Mabini Building, DepEd Complex, Meralco Avenue, Pasig City";
        
    static String headerSnText = "SN NQESH2023-B0230";
        
    static String footerText = "Reference:\n"+
                    "DepEd Memorandum No. 025, S 2024\n" + 
                    "Amendment to Depe Memorandum No. 100, s.2022 (Results of the Fiscal Year 2021 National Qualifying Examination for School Heads) and\n" +
                    "Clarification on the Use of NQESH or Principal’s Test Results in Relation to DepEd Order No. 007, s. 2023 (Guidelines on Recruitment, Selection, and Appointment in the Department of Education";

    static String footerText2 = "Notes:\n"+"The verified PDF copy of this Certificate of Rating can be accessed by scanning the QR Code provided above. It is hosted on the HR Avatar website at https://www.hravatar.com and will remain accessible for a period of three years from the date it was completed.";
                    // "The PDF file of the Certificate of Rating, accessible via the QR code, is hosted on the HR Avatar website: https://www.hravatar.com.   This file will be retained in the system for a period of 3 years.";
    
    String secName = "WILFREDO E. CABRAL";
    String secTitle = "Regional Director,\nOfficer-In-Charge\nOffice of the Undersecretary for HROD";
    
    
    static String titleText = "CERTIFICATE OF RATING";
        
    static String titleSubtext = "2023 NATIONAL QUALIFYING EXAMINATION FOR SCHOOL HEADS (NQESH)\n";
    
    
    String custLogo1Url = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Qwib6aTeMlI-/img_1717496877624.png";
    String custLogo2Url = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Qwib6aTeMlI-/img_1717496885168.png";
    String custSignatureUrl = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/Qwib6aTeMlI-/img_1720784731650.png";
    
    Image custLogo1 = null;
    Image custLogo2 = null;
    Image custSignature = null;
    Image verifyQRCode = null;
    
    String categoryRating;
    String categoryStatus;
    String[] userDemoData;
    String reportUrl = null;
    
    
    public NqeshCorReport()
    {
        super();
        
        this.devel = false;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            LogService.logIt( "NqeshCorReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );

            addHeader();
            
            addScoreTable();
            
            addFooter();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NqeshCorReport.generateReport() testEventId=" + (reportData.getTestEvent()==null ? "null" : reportData.getTestEvent().getTestEventId()) );
            throw new STException( e );
        }
    }

    
    

    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        initLocales();
        
        initFonts();
        
        initColors();  
        
        initImages();

        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        document.open();

        document.setMargins(36, 36, 36, 36 );

        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        
        // float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );
        headerHgt = 0; // hghts[0];
        footerHgt = 0; // hghts[1];

        usablePageHeight = pageHeight - headerHgt - footerHgt - 4*PAD;

        dataTableEvent = new TableBackground( BaseColor.DARK_GRAY , 0.2f, BaseColor.LIGHT_GRAY );
        
        categoryRating = computeCategoryRating();
        categoryStatus = computeCategoryStatus();
        
        setUserDemoData();
        
    }
    
    private void initImages()
    {
        try
        {
            custLogo1 = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( custLogo1Url ) );  //   getImageInstance(logoUrl, reportData.te.getTestEventId());
            custLogo2 = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( custLogo2Url ) );  //   getImageInstance(logoUrl, reportData.te.getTestEventId());
            custSignature = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( custSignatureUrl ) );  //   getImageInstance(logoUrl, reportData.te.getTestEventId());

            custLogo1.scalePercent(50f);
            custLogo2.scalePercent(50f);
            custSignature.scalePercent(60f);

            reportUrl = RuntimeConstants.getStringValue("adminappbasuri") + "/rptverifx/" + reportData.getTestEvent().getTestEventIdEncrypted() + "/" + reportData.getReport().getReportId() + "/verify.pdf";
            // LogService.logIt( "NqeshCorReport.initImages() reportUrl=" + reportUrl );
            
            if( reportUrl!=null && !reportUrl.isBlank() )
            {
                BufferedImage bi = QRCodeUtils.generateQRCodeImage(reportUrl, 150, 150 );
                
                ByteArrayOutputStream ibaos = new ByteArrayOutputStream();
                ImageIO.write(bi, "png", ibaos);
                verifyQRCode = Image.getInstance(ibaos.toByteArray());
                verifyQRCode.scalePercent(70f );
            }
            else
                LogService.logIt( "NqeshCorReport.initImages() No TestEventScore for barcode found." );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NqeshCorReport.initImages() " );
        }
        
    }

    private void addHeader() throws Exception
    {
        try
        {
            int cols = 3;
            float[] colRelWids = new float[] {.1f, .65f, .25f};

            // LogService.logIt( "NqeshCorReport.addHeader() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );                        

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( custLogo1 );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            Paragraph par = new Paragraph();
            Chunk chk = new Chunk( headerText, fontSmall );
            par.add(chk);
            chk = new Chunk( headerText2, fontSmallBold );
            par.add(chk);
            chk = new Chunk( headerText3, fontSmall );
            par.add(chk);
            
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 2 );
            c.setPaddingLeft( 10 );
            setRunDirection( c );
            t.addCell(c);

            PdfPTable t2 = new PdfPTable( 1 );
            
            c = new PdfPCell( custLogo2 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_TOP);
            c.setPadding( 2 );
            setRunDirection( c );
            t2.addCell(c);                    

            c = new PdfPCell( new Phrase( headerSnText, fontBold ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_TOP);
            c.setPadding( 2 );
            setRunDirection( c );
            t2.addCell(c);                    

            
            c = new PdfPCell( t2 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_TOP);
            c.setPadding( 2 );
            setRunDirection( c );
            t.addCell(c);                    

            previousYLevel =  currentYLevel;

            float y = addTableToDocument(previousYLevel, t, false, true );            
            currentYLevel = y - 2*PAD;
            previousYLevel =  currentYLevel;
            
            
            t = new PdfPTable( 1 );            
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setLockedWidth( true );
            setRunDirection( t );
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            
            c = new PdfPCell( new Phrase( titleText, this.fontXLargeBold ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 10 );
            c.setPaddingLeft( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            Date tdate = reportData.getTestKey().getExpireDate();
            Calendar cal = new GregorianCalendar();
            cal.setTime(tdate);
            cal.setTimeZone(reportData.getTimeZone() );
            TimeZone tz = TimeZone.getTimeZone( ZoneId.ofOffset("UTC", ZoneOffset.ofHours(8)));
            
            DateFormat sdf = new SimpleDateFormat( "MMMM d, yyyy");
            sdf.setTimeZone( tz);
            
            String sbb = sdf.format(tdate);
            
            c = new PdfPCell( new Phrase( titleSubtext + sbb, this.fontLarge ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 10 );
            c.setPaddingLeft( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            y = addTableToDocument(currentYLevel, t, false, true );            
            currentYLevel = y - 2*PAD;
            previousYLevel =  currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshCorReport.addHeader() testEventId=" + (reportData.getTestEvent()==null ? "null" : reportData.getTestEvent().getTestEventId()) );
            throw new STException( e );
        }        
    }
    
    private void addScoreTable() throws Exception
    {
        try
        {
            Font tableFont = font;
            Font tableItalicFont = this.fontItalic;
            Font tableBoldFont = this.fontBold;
            
            
            int cols = 2;
            float[] colRelWids = new float[] {.4f, .5f};

            // LogService.logIt( "NqeshCorReport.addScoreTable() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );                        

            // First create the table
            PdfPCell c;

            PdfPTable touter = new PdfPTable( cols );
            float[] colRelWidsOuter = new float[] {.4f, .1f};
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWidsOuter );
            touter.setLockedWidth( true );
            setRunDirection( touter );
            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            
            
            // First, add a table
            PdfPTable t = new PdfPTable( cols );
            t.setTableEvent(dataTableEvent);
            t.setTotalWidth( .8f*(pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN) );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            c = new PdfPCell( new Phrase( "Examinee Number:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            // c = new PdfPCell( new Phrase( Long.toString( reportData.getTestEvent().getUserId()), tableFont ) );
            c = new PdfPCell( new Phrase( reportData.getTestEvent().getExtRef(), tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            
            c = new PdfPCell( new Phrase( "Name:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( reportData.getUser().getFullname(), tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            //c = new PdfPCell( new Phrase( "Position:", tableFont ) );
            //c.setBorder(Rectangle.NO_BORDER );
            //c.setPadding( 5 );
            //c.setHorizontalAlignment( Element.ALIGN_LEFT);
            //c.setVerticalAlignment( Element.ALIGN_TOP);
            //setRunDirection( c );
            //t.addCell(c);
            
            //c = new PdfPCell( new Phrase( userDemoData[0], tableFont ) );
            //c.setBorder(Rectangle.NO_BORDER );
            //c.setPadding( 5 );
            //c.setHorizontalAlignment( Element.ALIGN_LEFT);
            //c.setVerticalAlignment( Element.ALIGN_TOP);
            //setRunDirection( c );
            //t.addCell(c);

            c = new PdfPCell( new Phrase( "Region:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( userDemoData[1], tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "SDO:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( userDemoData[2], tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( "Email Address:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( reportData.getUser().getEmail(), tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "Percentage Score:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(Locale.US, reportData.getTestEvent().getOverallTestEventScore().getScore(), 2), tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "Qualification:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( categoryRating, tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "Status:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( categoryStatus, tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( "CERTIFIED correct according to the records of this office:", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setColspan(2);
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "", tableFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            PdfPTable tx = new PdfPTable( 1 );
            tx.setLockedWidth(true);
            tx.setTotalWidth(170);
            tx.setHorizontalAlignment( Element.ALIGN_LEFT);
            
            //c = new PdfPCell( this.custSignature );
            //c.setBorder(Rectangle.NO_BORDER );
            //c.setPadding( 5 );
            //c.setPaddingLeft(25);
            //c.setPaddingBottom(0);
            // c.setBackgroundColor(BaseColor.CYAN);
            //c.setHorizontalAlignment( Element.ALIGN_LEFT);
            //c.setVerticalAlignment( Element.ALIGN_TOP);
            //setRunDirection( c );
            // tx.addCell(c);

            // c = new PdfPCell( new Phrase( "", tableFont ) );
            //c.setBorder(Rectangle.NO_BORDER );
            //c.setPadding( 5 );
            //c.setHorizontalAlignment( Element.ALIGN_LEFT);
            //c.setVerticalAlignment( Element.ALIGN_TOP);
            //setRunDirection( c );
            //t.addCell(c);
            
            c = new PdfPCell( new Phrase( secName, tableBoldFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPaddingLeft( 5 );
            c.setPaddingRight( 5 );            
            c.setPaddingTop( 35 );            
            c.setPaddingBottom(1);
            // c.setBackgroundColor(BaseColor.CYAN);
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            c.setCellEvent(new NqeshCorSigBlockCellEvent( custSignature ));
            setRunDirection( c );
            tx.addCell(c);
            
            //c = new PdfPCell( new Phrase( "", tableFont ) );
            //c.setBorder(Rectangle.NO_BORDER );
            //c.setPadding( 5 );
            //c.setHorizontalAlignment( Element.ALIGN_LEFT);
            //c.setVerticalAlignment( Element.ALIGN_TOP);
            //setRunDirection( c );
            //t.addCell(c);
            
            c = new PdfPCell( new Phrase( secTitle, tableItalicFont ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setPaddingTop(1);
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            tx.addCell(c);
            
            c = new PdfPCell( tx );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 0 );
            c.setPaddingRight(0);
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            
            c = new PdfPCell( t );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            touter.addCell(c);
            
            
            t = new PdfPTable( 1 );
            t.setTotalWidth( .2f*(pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN) );
            t.setLockedWidth( true );
            setRunDirection( t );
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            
            c = new PdfPCell(new Phrase("\n\n\n\n\n\nVerification:", fontItalic ) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            if( verifyQRCode==null )
                c = new PdfPCell(new Phrase(""));
            else
                c = new PdfPCell( verifyQRCode );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            setRunDirection( c );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_BOTTOM);
            t.addCell(c);
            

            c = new PdfPCell( t );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setPaddingTop(102);
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            touter.addCell(c);
            
            previousYLevel =  currentYLevel;

            float y = addTableToDocument(previousYLevel, touter, false, true );            
            currentYLevel = y - 2*PAD;
            previousYLevel =  currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshCorReport.addScoreTable() testEventId=" + (reportData.getTestEvent()==null ? "null" : reportData.getTestEvent().getTestEventId()) );
            throw new STException( e );
        }                
    }
    
    private void addFooter() throws Exception
    {
        try
        {

            // LogService.logIt( "NqeshCorReport.addHeader() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );                        

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setLockedWidth( true );
            setRunDirection( t );
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase("ANY ERASURE OR ALTERATION HEREON NULLIFIES THIS CERTIFICATION.", this.fontLargeBold) );
            c.setBorder(Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            setRunDirection( c );
            t.addCell(c);
            
            previousYLevel =  currentYLevel;
            float y = addTableToDocument(previousYLevel, t, false, true );            
            currentYLevel = y - 2*PAD;
            previousYLevel =  currentYLevel;
            
            
            int cols = 2;
            float[] colRelWids = new float[] {.15f, .85f};
            t = new PdfPTable( cols );
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );            
            
            Font tableFont = font;

            c = new PdfPCell( new Phrase( "The following describes the qualification status of takers depending on their rating:", tableFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setColspan(2);
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( "Qualification", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( "Description", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "A", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( "Overall score is 65 and above, and at least 4 of the Domains are 65 and above.", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "B", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( "Overall score is 65 and above, and at least 3 of the Domains are 65 and above.", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "C", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT  | Rectangle.BOTTOM );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( "Overall score is 64 and below or Overall score is 65 and above but only 2 Domains are 65 and above.", tableFont ) );
            c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 5 );
            setRunDirection( c );
            t.addCell(c);
            
            
            // removed.
            if( 1==1 )
            {
                c = new PdfPCell( new Phrase( footerText2, fontSmallItalic ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setColspan(2);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setPadding( 5 );
                setRunDirection( c );
                t.addCell(c);
            }
            
            previousYLevel =  currentYLevel;
            y = addTableToDocument(previousYLevel, t, false, true );            
            currentYLevel = y - 2*PAD;
            previousYLevel =  currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshCorReport.addFooter() testEventId=" + (reportData.getTestEvent()==null ? "null" : reportData.getTestEvent().getTestEventId()) );
            throw new STException( e );
        }        
        
    }
    
    
    /*
      data[0][=Position
      data[1]=Suborg
      data[2]=Custom Field 2
      
      
    */
    private synchronized void setUserDemoData() throws Exception
    {
        try
        {
            if( userDemoData!=null )
                return;

            userDemoData=new String[]{"","","",""};
            
            String position = null;
            String otherPosition = null;

            TestEvent te = reportData.getTestEvent();

            Clicflic cf = te.getResultXmlObj();
            if( cf==null && te.getResultXml()!=null && !te.getResultXml().isBlank() )
            {
                cf = JaxbUtils.ummarshalImoResultXml(te.getResultXml());
                te.setResultXmlObj(cf);
            }
            if( cf!=null )
            {
                Clicflic.History.Intn intn;
                for( Object obj : cf.getHistory().getIntnOrClip() )
                {
                    if( !(obj instanceof Clicflic.History.Intn) )
                        continue;
                    intn = (Clicflic.History.Intn) obj;

                    if( intn.getUnqid()!=null && intn.getUnqid().equals("biodataQ2_Position" ) )
                    {
                        String sel = intn.getValue();
                        while( sel.contains("~") )
                            sel = sel.substring( sel.indexOf("~"), sel.length() );

                        if( sel.isBlank() )
                            throw new Exception( "Unable to parse value for biodataQ2_Position" );

                        Integer selVal = Integer.valueOf(sel);
                        switch (selVal) {
                            case 1:
                                position =  "Teacher I";
                                break;
                            case 2:
                                position =  "Teacher II";
                                break;
                            case 3:
                                position =  "Teacher III";
                                break;
                            case 4:
                                position =  "Head Teacher I";
                                break;
                            case 5:
                                position =  "Head Teacher II";
                                break;
                            case 6:
                                position =  "Head Teacher IIII";
                                break;
                            case 7:
                                position =  "Head Teacher IV";
                                break;
                            case 8:
                                position =  "Head Teacher V";
                                break;
                            case 9:
                                position =  "Head Teacher VI";
                                break;
                            case 10:
                                position =  "Master Teacher I";
                                break;
                            case 11:
                                position =  "Master Teacher II";
                                break;
                            case 12:
                                position =  "Master Teacher III";
                                break;
                            case 13:
                                position =  "Master Teacher IV";
                                break;
                            case 14:
                                position =  "Assistant Principal I";
                                break;
                            case 15:
                                position =  "Assistant Principal II";
                                break;
                            case 16:
                                position =  "Assistant Principal III";
                                break;
                            case 17:
                                position =  "OTHER";
                                break;
                            default:
                                break;
                        }

                    }

                    if( intn.getUnqid()!=null && intn.getUnqid().equals("BiodataQ2_Position-Others" ) )
                    {
                        String sel = intn.getValue();

                        if( sel!=null && !sel.isBlank() )
                        {
                            while( sel.contains("~") )
                                sel = sel.substring( sel.indexOf("~"), sel.length() );                        
                        }
                        otherPosition = StringUtils.getUrlDecodedValue(sel);
                    }                                
                }

                if( position==null || position.equalsIgnoreCase("OTHER") )
                    position = otherPosition;

            }

            if( position==null || position.isBlank() )
                position = "Not Available";

            if( reportData.getTestEvent().getSuborgId()>0 )
            {
                if( reportData.getTestEvent().getSuborg()==null )
                    reportData.getTestEvent().setSuborg( UserFacade.getInstance().getSuborg(reportData.getTestEvent().getSuborgId()));
            }

            userDemoData[0]=position;
            userDemoData[1]=reportData.getTestEvent().getSuborg()==null ? "Not Available" : reportData.getTestEvent().getSuborg().getName();
            
            String cust2 = reportData.getTestKey().getCustom2()==null ? "" : reportData.getTestKey().getCustom2();            
            userDemoData[2]=cust2;                        
        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshCorReport.setUserDemoData() testEventId=" + (reportData.getTestEvent()==null ? "null" : reportData.getTestEvent().getTestEventId()) );
            throw e;
        }
        
    }
    
    /*
        Category A - Overall Score of 65 & above and 4 out of 5 Domains scores must be 65 and above.
        Category B- Overall Score of 65 & above and 3 out of 5 Domains scores must be 65 and above. 
        Category  C - Overall Score of 64.99 and below.     
    */
    private String computeCategoryRating()
    {
        float score = reportData.getTestEvent().getOverallScore();
        
        if( score<65f )
            return "C";
        
        // need to count domains. 
        int count = 0;
        int totalCount = 0;
        for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()))
        {
            if( !tes.getName().startsWith("[Domain") )
                continue;
            totalCount++;
            if( tes.getScore()>=65f )
                count++;
        }
        
        if( totalCount!=5 )
            LogService.logIt( "NqeshCorReport.computeCategoryRating() Did not find the expected number of domain scores. Expected 5. Found " + totalCount + ", passCount=" + count + ",  testEventId=" + (reportData.getTestEvent()==null ? "null" : reportData.getTestEvent().getTestEventId()));
            
        if( count<=2 )
            return "C";
        
        if( count<=3 )
            return "B";
        
        return "A";
    }
    
    public String computeCategoryStatus()
    {
        if( this.categoryRating==null || this.categoryRating.equalsIgnoreCase("A") )
            return "ELIGIBLE for appointment to entry-level school principal position subject to evaluative and eligibility assessment.";
        
        return "MAY RETAKE the next NQESH.";
    }

}
