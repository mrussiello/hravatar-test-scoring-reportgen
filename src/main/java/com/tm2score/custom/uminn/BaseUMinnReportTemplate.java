/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2score.custom.coretest2.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.tm2score.custom.coretest.ITextUtils;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import static com.tm2score.custom.uminn.UMinnReportSettings.averageScoreCache;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.purchase.Product;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.format.TableBackground;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 *
 * @author Mike
 */
public abstract class BaseUMinnReportTemplate extends UMinnReportSettings implements ReportTemplate
{
    static String[] abcs = new String[] { " " , "A", "B", "C", "D", "E", "F", "G", "H" };

    static String BULLET = "\u2022";


    Document document = null;

    ByteArrayOutputStream baos;

    PdfWriter pdfWriter;

    float pageWidth = 0;
    float pageHeight = 0;
    float usablePageHeight = 0;

    String title;

    float headerHgt;
    float footerHgt;

    float lastY = 0;

    TableBackground dataTableEvent;
    TableBackground tableHeaderRowEvent;

    // ReportUtils reportUtils;

    float PAD = 5;
    float TPAD = 8;

    // float bxX;
    // float bxWid;
    //float barGrphWid;
    //float barGrphX;
    float lineW = 0.8f;


    float currentYLevel = 0;
    float previousYLevel = 0;

    
    int scoreScheme = 0;


    @Override
    public abstract byte[] generateReport() throws Exception;


    public synchronized void initFonts() throws Exception
    {
        initSettings( reportData );
        
        initExtra( reportData );
        
    }

    

    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        // ctReportData = new CT2ReportData();

        XXLFONTSZ = 32;
        XLFONTSZ = 18;
        LLFONTSZ = 15;
        LFONTSZ = 14;
        LMFONTSZ = 13;
        FONTSZ = 12;
        SFONTSZ = 11;
        XSFONTSZ = 10;
        XXSFONTSZ = 9;
        
        
        initFonts();
        
        initColors(); 
        
        averageScoreCache = new HashMap<>();
        
        initData();
        
        /*
         scorescheme=1 = development report
        */
        scoreScheme = reportData.getReportRuleAsInt("scorescheme");
        
        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        UMinnHeaderFooter hdr = new UMinnHeaderFooter( document, rd.getLocale(), lmsg("title"), reportData, this );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );

        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );

        headerHgt = hghts[0];
        footerHgt = hghts[1];

        usablePageHeight = pageHeight - headerHgt - footerHgt - 4*PAD;


        // LogService.logIt( "BaseUMinnReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

        tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );
        
        
    }
    
    @Override
    public Locale getReportLocale()
    {
        if( this.reportData!=null )
            return reportData.getLocale();
        
        return Locale.US;
    }

    
    @Override
    public long addReportToOtherTestEventId() throws Exception
    {
        return 0;
    }

    
    protected void addSceneSectionTableHeaders( PdfPTable t, Font tcFont, int cellPadd ) throws Exception
    {
            PdfPCell c; // 
                        
            // Do the header
            c = new PdfPCell( new Phrase( lmsg("slr.response"), tcFont ));
            c.setPadding( cellPadd );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.LEFT | Rectangle.TOP );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            setRunDirection(c);
            t.addCell( c );

            c = new PdfPCell( new Phrase( lmsg("slr.dim"), tcFont ));
            c.setPadding( cellPadd );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.TOP );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            setRunDirection(c);
            t.addCell( c );

            c = new PdfPCell( new Phrase( lmsg("slr.yourrating"), tcFont ));
            c.setPadding( cellPadd );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.TOP );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            setRunDirection(c);
            t.addCell( c );

            c = new PdfPCell( new Phrase( lmsg("slr.avg"), tcFont ));
            c.setPadding( cellPadd );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.TOP );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            setRunDirection(c);
            t.addCell( c );

            c = new PdfPCell( new Phrase( lmsg("slr.fer"), tcFont ));
            c.setPadding( cellPadd );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.TOP );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            setRunDirection(c);
            t.addCell( c );
            
            c = new PdfPCell( new Phrase( lmsg("slr.ratingdiff"), tcFont ));
            c.setPadding( cellPadd );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            setRunDirection(c);
            t.addCell( c );
        
        
    }
    
    
    @Override
    public String getReportGenerationNotesToSave()
    {
        return null;
    }
    
    
    public void addSceneSection( UMinnScene ums ) throws Exception 
    {
        try
        {
            if( ums.getUMinnItemList()==null || ums.getUMinnItemList().isEmpty() )
            {
                LogService.logIt( "addSceneSection() skipping UMScene because there are no recorded UMinnItems for it. " + ums.toString() );
                return;
            }
            
            addNewPage();
            
            String scnTxt = ums.getSceneText();
            
            
            float y = addTitle( currentYLevel , lmsg("ScenarioX", new String[] {Integer.toString( ums.getIndex()) } ), scnTxt );  

            if( ums.getFollowUpItem() != null )
            {
                String wouldDo = ums.getFollowUpItem().getText();
                
                if( wouldDo !=null && !wouldDo.isEmpty() )
                {
                    y -= TPAD;                    
                    y = addBoldRegPair(y, lmsg( "g.SaidWouldDoC" ), wouldDo);                    
                    y -= PAD;
                    // scnTxt += "\n\n" + lmsg( "g.SaidWouldDoC" ) + "\n\n" + wouldDo;
                }                    
                    
            }
            
            else if( RuntimeConstants.getBooleanValue( "scoreDebugMode" ) )
            {
                String wouldDo = "This is what you said you'd do oh yeah!!!!!! ";
                
                if( wouldDo !=null && !wouldDo.isEmpty() )
                {
                    y -= TPAD;                    
                    y = addBoldRegPair(y, lmsg( "g.SaidWouldDoC" ), wouldDo);
                    y -= PAD;
                    // scnTxt += "\n\n" + lmsg( "g.SaidWouldDoC" ) + "\n\n" + wouldDo;
                }                    
                
            }
            
            
            y -= 2*TPAD;  

            PdfPCell c; // 
            
            Font tcFont = this.getFontBold();
            
            int cellPadd = 3;
            
            int[] widths = new int[] {10, 8, 6, 6, 6, 6 };
            
            // Do the header
            PdfPTable t = new PdfPTable( 6 );
            
            t.setWidths( widths );

            t.setTotalWidth( pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ) );
            
            t.setLockedWidth( true );
            setRunDirection(t);

            addSceneSectionTableHeaders( t, getFontBold(), cellPadd );
                        
            tcFont = getFont();
            
            float tableH;            
 
            float tableY; //  + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW;

            float tableX;
            
            
            if( !ums.getUMinnItemList().isEmpty() )
            {
                ums.getUMinnItemList().get( ums.getUMinnItemList().size()-1 ).setLast(true);
            }
            
            boolean first = true;
            
            ScoreCategoryType sct = null;
            
            BaseColor sctColor;
            
            for( UMinnItem us : ums.getUMinnItemList() )
            {
                sct = us.getScoreCategoryType();
                
                sctColor = getColorForScoreCategoryType( sct );
                
                c = new PdfPCell( new Phrase( us.text, tcFont ));
                c.setPadding( cellPadd );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                
                if( !first )
                   c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM );
                    
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );               
                setRunDirection(c);
                t.addCell( c );

                c = new PdfPCell( new Phrase( us.getUminnCompetency().getName(), tcFont ));
                c.setPadding( cellPadd );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );

                if( !first )
                   c.setBorder( Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );

                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );               
                setRunDirection(c);
                t.addCell( c );

                c = new PdfPCell( new Phrase( us.getRatingStr(), tcFont ));
                c.setPadding( cellPadd );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );

                if( !first )
                   c.setBorder( Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );

                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );               
                setRunDirection(c);
                t.addCell( c );

                c = new PdfPCell( new Phrase( us.getAvgRatingStr(), tcFont ));
                c.setPadding( cellPadd );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                if( !first )
                   c.setBorder( Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );

                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );               
                setRunDirection(c);
                t.addCell( c );
                
                c = new PdfPCell( new Phrase( us.getFacultyRatingStr(), tcFont ));
                c.setPadding( cellPadd );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                if( !first )
                   c.setBorder( Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );

                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );               
                setRunDirection(c);
                t.addCell( c );

                c = new PdfPCell( new Phrase( us.getDifferenceRatingStr(), getFontBold() ));
                c.setPadding( cellPadd );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );

                if( !first )
                   c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM | Rectangle.RIGHT );

                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );  
                c.setBackgroundColor(sctColor);
                setRunDirection(c);
                t.addCell( c );
                
                first = false;
                
                tableH = t.calculateHeights();
                
                // if( us.isLast() || tableH >= pageHeight - headerHgt - footerHgt - 20*TPAD )
                if( us.isLast() || tableH >= y - footerHgt - 11*TPAD )
                {
                    tableH = t.calculateHeights(); //  + 500;

                    tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

                    tableW = t.getTotalWidth();

                    tableX = (pageWidth - tableW)/2;

                    t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
                    
                    t = null;

                    y =  y - tableH - 2*TPAD;

                    currentYLevel = y;  
                    
                    if( !us.isLast() )
                    {
                        addNewPage(); 

                        y = pageHeight - headerHgt - 2*TPAD;
                    
                        t = new PdfPTable( 6 );
                        t.setWidths( widths );

                        t.setTotalWidth( pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ) );

                        t.setLockedWidth( true );
                        setRunDirection(t);

                        addSceneSectionTableHeaders( t, getFontBold(), cellPadd );                    
                    }
                }
  
            }
                        
            currentYLevel = y;                      
            

        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addSceneSection() " + ums.toString() );

            throw new STException( e );
        }                    
    }
    
    
    public BaseColor getColorForScoreCategoryType( ScoreCategoryType sct )
    {
        if( sct.green() )
            return ct2Colors.getGreen();
        else if( sct.yellowGreen())
            return ct2Colors.getYellowgreen();
        else if( sct.yellow() )
            return ct2Colors.getYellow();
        else if( sct.redYellow() )
            return ct2Colors.getRedyellow();
        // 
        else
            return ct2Colors.getRed();
    }
    
    public void addScenarioLevelReport() throws Exception
    {
        try
        {
            if( reportData.getReportRuleAsBoolean( "excludereportsection1") )
                return;
            
            float y = addTitle( currentYLevel , lmsg("slr.title"), null );  

            y -= TPAD;       
            
            PdfPTable t = new PdfPTable( 2 );
            t.setWidths( new int[] {5, 50 } );

            t.setTotalWidth( pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ) );
            
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c; // 
            
            Font tcFont = this.getFontLarge();
            Font bFont = this.getFontXLargeBold();
            
            for( int i=1;i<=4;i++ )            
            {
                c = new PdfPCell( new Phrase( lmsg("slr." + i), tcFont ));
                c.setPadding( 5 );
                c.setColspan(2);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                setRunDirection(c);
                t.addCell( c );
            }

            for( int i=1;i<=2;i++ )            
            {
                c = new PdfPCell( new Phrase( BULLET, bFont ));
                c.setPadding( 5 );
                c.setPaddingRight(0);
                c.setColspan(1);
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                setRunDirection(c);
                t.addCell( c );
                
                c = new PdfPCell( new Phrase( lmsg("slr.4." + i), tcFont ));
                c.setPadding( 5 );
                c.setColspan(1);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                setRunDirection(c);
                t.addCell( c );
            }
            
            c = new PdfPCell( new Phrase( lmsg("slr.5"), tcFont ));
            c.setPadding( 5 );
            c.setColspan(2);
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);
            t.addCell( c );
            
            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
            
            y =  y - tableH - 2;
            
            currentYLevel = y;  
            
            for( UMinnScene uminnScene : uminnSceneList )
            {
                 addSceneSection( uminnScene );                
            }
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addScenarioLevelReport()" );

            throw new STException( e );
        }            
        
    }



    public void addOverallSummary() throws Exception
    {
        try
        {
            boolean numScoresOn = reportData.getReportRuleAsBoolean( "ovrnumon" );
            
            if( !numScoresOn )
                return;
            
            TestEventScore otes = reportData.te.getOverallTestEventScore();
            
            int scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  otes.getScoreFormatType().getScorePrecisionDigits();
            
            float ovrScrToUse = otes.getPercentile();
            
            if( scoreScheme==1 || ovrScrToUse<=0 || otes.getOverallPercentileCount()<=10)
                ovrScrToUse = otes.getScore();
            
            String scrValue = I18nUtils.getFormattedNumber( reportData.getLocale(), ovrScrToUse, scrDigits );
            String titleStr = lmsg("ovrsum.title") + " " + scrValue;
            
            
            //this.addNewPage();
            //LogService.logIt( "BaseUMinnReportTemplate.addCompetencySummary() currentYLevel=" + currentYLevel );
            float y = addTitle( currentYLevel , titleStr, null );  
                        
            y -= TPAD;
                        
            currentYLevel = y;            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addOverallSummary()" );

            throw new STException( e );
        }
        
    }
    
    
    
    
    public void addCompetencyDetails() throws Exception
    {
        try
        {
            boolean first = true;
            
            float y = 0;
            
            PdfPTable t;
            PdfPCell c;
            
            int count = 1;
            
            int cellPad = 4;
            
            PdfPTable t2;

            PdfPTable tHi;
            PdfPTable tLo;
                 
            int ct;
            
            boolean numScoresOn = reportData.getReportRuleAsBoolean( "cmptynumon" );
            
            int scrDigits = 0;
         
            String scrValue;            
            
            for( UMinnCompetency uc : competencyList )
            {
                addNewPage();
                
                if( first )
                {
                    y = addTitle( currentYLevel , lmsg("compdet.title"), null );  

                    y -= 2*TPAD;   
                    
                    first = false;
                }  
                
                else
                    y = pageHeight - headerHgt - 2*TPAD;
                
                // First, add a table
                t = new PdfPTable( 4);

                t.setTotalWidth( pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ) );
                t.setWidths( new int[]{5,50,5,50});
                t.setLockedWidth( true );
                setRunDirection(t);

                Font tcFont = this.getFontLarge();
                Font tcFontB = this.getFontLargeBold();
                Font bFont = this.getFontXLargeBold();

                String str = lmsg( "compdet.dim"  ) + " " + count + ": " + uc.getName();
                
                if( numScoresOn )
                {
                    scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  uc.testEventScore.getScoreFormatType().getScorePrecisionDigits();                    
                    scrValue = I18nUtils.getFormattedNumber(reportData.getLocale(), uc.testEventScore.getScore(), scrDigits );
                    
                    str += " (" + lmsg( "compdet.scorec" ) + " " + scrValue + ")";
                }
                
                c = new PdfPCell( new Phrase( str, tcFontB ) );
                c.setColspan(4);
                c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding( cellPad );
                t.addCell( c );

                
                str = lmsg( "compdet.def1" , new String[]{uc.getName()} );
                
                Phrase ph = new Phrase( new Chunk( str, tcFontB ));          
                ph.add( new Chunk( " " + uc.getDefinition(), tcFont ) );
                c = new PdfPCell( ph );
                c.setColspan(4);
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding( cellPad );
                c.setPaddingBottom( 6 );
                t.addCell( c );

                
                c = new PdfPCell( new Phrase( lmsg( "compdet.behavehi" ), tcFontB ) );
                c.setColspan(2);
                c.setBorder( Rectangle.LEFT | Rectangle.TOP  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( cellPad );
                t.addCell( c );

                c = new PdfPCell( new Phrase( lmsg( "compdet.behavelo" ), tcFontB ) );
                c.setColspan(2);
                c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( cellPad );
                t.addCell( c );

                String[] matrix = uc.getBehMatrix();
                           
                tHi = new PdfPTable(2);
                tLo = new PdfPTable(2);

                tHi.setTotalWidth( (pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ))/2 );
                tHi.setWidths( new int[]{5,50});
                tHi.setLockedWidth( true );
                setRunDirection(tHi);                

                tLo.setTotalWidth( (pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ))/2 );
                tLo.setWidths( new int[]{5,50});
                tLo.setLockedWidth( true );
                setRunDirection(tLo);                
                
                for( int i=0;i<matrix.length-1;i+=2 )
                {
                    
                    c = new PdfPCell( new Phrase( matrix[i].isEmpty() ? "" : BULLET, bFont));
                    c.setColspan(1);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( 2 );
                    c.setPaddingRight(1);
                    
                    if( i==matrix.length-2 )
                        c.setPaddingBottom( 4 );
                    
                    tHi.addCell( c );
                    
                    c = new PdfPCell( new Phrase(matrix[i], tcFont));
                    c.setColspan(1);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( 2 );
                    if( i==matrix.length-2 )
                        c.setPaddingBottom( 4 );
                    tHi.addCell( c );
                    
                    c = new PdfPCell( new Phrase(matrix[i+1].isEmpty() ? "" : BULLET, bFont));
                    c.setColspan(1);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( 2 );
                    if( i==matrix.length-2 )
                        c.setPaddingBottom( 4 );
                    tLo.addCell( c );
                    
                    c = new PdfPCell( new Phrase(matrix[i+1], tcFont));
                    c.setColspan(1);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( 2 );
                    if( i==matrix.length-2 )
                        c.setPaddingBottom( 4 );
                    tLo.addCell( c );
                }  
                
                c = new PdfPCell( tHi );
                c.setColspan(2);
                c.setBorder( Rectangle.LEFT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( cellPad );
                t.addCell( c );

                c = new PdfPCell( tLo );
                c.setColspan(2);
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( cellPad );
                t.addCell( c );
                
                //ph = new Phrase();
                //ph.add(ilist);
                               
                
                str = uc.getSummary();
                
                c = new PdfPCell( new Phrase( str, tcFont ) );
                c.setColspan(4);
                c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( cellPad );
                t.addCell( c );
                
                c = new PdfPCell( new Phrase( lmsg("compdet.recdevops"), tcFontB ) );
                c.setColspan(4);
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding( cellPad );
                t.addCell( c );
                
                ct = 0;
                
                for( String txt : uc.getDevOps())
                {
                    c = new PdfPCell( new Phrase(BULLET, bFont ) );                    
                    c.setColspan(1);
                    if( ct==uc.getDevOps().size()-1 )
                        c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM  );
                    else
                        c.setBorder( Rectangle.LEFT  );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( cellPad );
                    t.addCell( c );

                    c = new PdfPCell( new Phrase(txt, tcFont ) );                    
                    c.setColspan(3);
                    
                    if( ct==uc.getDevOps().size()-1 )
                        c.setBorder( Rectangle.RIGHT | Rectangle.BOTTOM  );
                    else
                        c.setBorder( Rectangle.RIGHT  );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( cellPad );
                    t.addCell( c );
                    
                    ct++;                    
                }                

                 float tableH = t.calculateHeights(); //  + 500;

                 float tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

                 float tableW = t.getTotalWidth();

                 float tableX = (pageWidth - tableW)/2;

                 t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
            
                 count++;

            }     
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addCompetencyDetails()" );

            throw new STException( e );
        }            
        
    }
    
    
    public void addComparison() throws Exception
    {
        try
        {               
            if( reportData.getReportRuleAsBoolean( "skipcomparisonsection" ) )
                return;
            
            if( reportData.getReport().getIncludeNorms()==0 )
                return;
   
            float y = addTitle( currentYLevel , lmsg("compare.title"), lmsg("compare.1") );  
            
            y -= 2*TPAD;
                       
            
            // This is a trick
            if( custom1PercentileData!=null && custom1PercentileData[0]<=0)
            {
                custom1PercentileData[0] = reportData.getTestEvent().getOverallScore();
                
                if( custom1PercentileData[1]<=0 )
                    custom1PercentileData[1]=1;
            }
            
            boolean hasOverall = reportData.getTestEvent().getOverallPercentileCount()>=100;
            
            boolean hasCust = reportData.tk.getCustom1()!=null && 
                              !reportData.tk.getCustom1().isEmpty() && 
                              custom1PercentileData!=null && 
                              custom1PercentileData[0]>0 && 
                              custom1PercentileData[1]>=10;
            
            int validCount = hasOverall ? 1 : 0;
            
            if( hasCust )
                validCount++;
            
            String[] names = new String[validCount];
            float[] percentiles = new float[validCount];
            int[] counts = new int[validCount];

            int idx = 0;
            
            if( hasOverall)
            {
                names[idx]="Overall";
                percentiles[idx]=reportData.getTestEvent().getOverallPercentile();
                counts[idx]=reportData.getTestEvent().getOverallPercentileCount();                
                idx++;
            }

            if( hasCust )
            {
                names[idx]=reportData.tk.getCustom1();
                percentiles[idx]=custom1PercentileData[0];
                counts[idx]=(int)custom1PercentileData[1];                
                idx++;                
            }
                           
            previousYLevel =  currentYLevel; // - TPAD;

            y = previousYLevel - TPAD - 5;

            float thgt =0;// t!= null  t.calculateHeights();

            PdfPTable t = null;
            PdfPCell c;

            // First, add a table
            t = new PdfPTable( 3 );
            setRunDirection( t );

            // float extraMargin = 25;
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( reportData.getIsLTR() ? new float[] {22,13,72} : new float[] {72,13,22} );
            t.setLockedWidth( true );

            if( validCount > 0 )
            {
                int rowCount = 0;

                // Now create the graph
                // First create the table

                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );

                // Create header
                c = new PdfPCell( new Phrase( lmsg("g.TesterGroup"), font ) );
                c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT :Rectangle.TOP | Rectangle.RIGHT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan( 1 );
                c.setPadding( 3 );
                //c.setPaddingBottom( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setBackgroundColor(  ct2Colors.scoreBoxShadeBgColor );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg("g.Percentile"), font ) );
                c.setBorder( Rectangle.TOP  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan( 1 );
                c.setPadding( 3 );
                //c.setPaddingBottom( 3 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setBackgroundColor(  ct2Colors.scoreBoxShadeBgColor );
                setRunDirection( c );
                t.addCell(c);

                // Create the cell for the full table
                c = new PdfPCell( new Phrase( "" ) );
                c.setBackgroundColor( ct2Colors.hraBaseReportColor );
                c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                c.setCellEvent(new PercentileHeaderCellEvent( baseFontCalibri , validCount, ct2Colors ) );
                t.addCell(c);

                for( int i=0;i<names.length;i++ )
                {
                    rowCount++;
                    addComparisonTableRow( t, names[i], percentiles[i], counts[i], rowCount==validCount );
                }
                
                thgt = t.calculateHeights();
            }

            else
            {
                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );

                Phrase p = new Phrase();

                Chunk ch = new Chunk( lmsg( "g.NoteC" ) + " ", this.fontBoldItalic  );

                p.add( ch );

                ch = new Chunk(  lmsg( "g.InsufficientDataForComparisons" ), this.fontItalic );

                p.add( ch );

                c = new PdfPCell( p );
                c.setColspan( 3 );
                c.setPaddingLeft( 4 );
                c.setPaddingRight( 4 );
                c.setPaddingBottom( 7 );
                //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                //c.setBorderWidth( scoreBoxBorderWidth );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell(c);

                thgt = t.calculateHeights();
            }

            // LogService.logIt( "BaseUMinnReportTemplate.addComparisonSection() validCount=" + validCount + ", thgt=" + thgt + ", y=" + y );

            if( thgt + 75 > y )
            {
                addNewPage();

                y = currentYLevel;
            }

            // y = addTitle( y, lmsg( "g.ComparisonPcts" ), validCount > 0 ? lmsg( devel ? "g.PercentileNoteDevel" : "g.PercentileNote" ) : null );

            // y -= TPAD;

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y- thgt - PAD;

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addComparison()" );

            throw new STException( e );
        }        
    }
    
    private void addComparisonTableRow( PdfPTable t, String name, float percentile, int count, boolean last )
    {
            // LogService.logIt( "BaseUMinnReportTemplate.addComparisonTableRow() name=" + name + ", percentile=" + percentile + ", last =" + last );

            if( RuntimeConstants.getBooleanValue("reportDebugMode" ) && percentile <= 1f )
            {
                percentile=80.0f;
                count=100;
            }
            
            else if( percentile <= 1f )
            {
                percentile = reportData.te.getOverallScore();
            }
        
            PdfPCell c;

            Font fnt = this.font;

            c = new PdfPCell( new Phrase( name, fnt ) );

            if( reportData.getIsLTR() )
                c.setBorder( last ? Rectangle.LEFT | Rectangle.BOTTOM  : Rectangle.LEFT );
            else
                c.setBorder( last ? Rectangle.RIGHT | Rectangle.BOTTOM  : Rectangle.RIGHT );

            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 1 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);

            String perStr = I18nUtils.getFormattedNumber( reportData.getLocale(), percentile, 0 ) + NumberUtils.getPctSuffix( reportData.getLocale(), percentile, 0 );

            // I18nUtils.getFormattedInteger( reportData.getLocale(), (int) percentile );

            // Create the cell for the full table
            c = new PdfPCell( new Phrase( perStr, fnt ) );
            c.setBorder( last ? Rectangle.BOTTOM : Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            setRunDirection( c );
            t.addCell(c);


            c = new PdfPCell( new Phrase( " ", this.getFontLargeWhite() ) );
            c.setPadding( 1 );
            if( last )
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
            else
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

            String ctStr = lmsg( "g.PercentileCountStr1", new String[] { Integer.toString(count)}  );

            if( 1==1 )
                ctStr = "";

            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new PercentileBarCellEvent( percentile , ctStr,  ct2Colors.barGraphCoreShade2,  ct2Colors.barGraphCoreShade1, baseFontCalibri ) );
            setRunDirection( c );
            t.addCell(c);
    }
    
    
    
    public void addCompetencySummary() throws Exception
    {
        try
        {
            //this.addNewPage();
            //LogService.logIt( "BaseUMinnReportTemplate.addCompetencySummary() currentYLevel=" + currentYLevel );
            float y = addTitle( currentYLevel , lmsg("compsum.title"), null );  
            
            boolean numScoresOn = reportData.getReportRuleAsBoolean( "cmptynumon" );
            
            y -= TPAD;
            
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( numScoresOn ? 3 : 2 );

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( numScoresOn ? new int[]{60, 20, 20 } : new int[]{ 70, 30 } );
            t.setLockedWidth( true );
            setRunDirection( t );
            // t.setHeaderRows( 1 );


            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "compsum.competency"), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 3 );
            //c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setBackgroundColor( uminnMaroon );
            setRunDirection( c );
            t.addCell(c);
            
            if( numScoresOn )
            {
                c = new PdfPCell( new Phrase( lmsg( "compsum.score"), fontLargeWhite ) );
                c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 3 );
                //c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setBackgroundColor( uminnMaroon );
                setRunDirection( c );
                t.addCell(c);                
            }
            
            c = new PdfPCell( new Phrase( lmsg( "compsum.interp"), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT  | Rectangle.RIGHT : Rectangle.TOP | Rectangle.RIGHT );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 3 );
            //c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setBackgroundColor( uminnMaroon );
            setRunDirection( c );
            t.addCell(c);
            
            /*
            c = new PdfPCell( new Phrase( lmsg( "compsum.interp"), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT  | Rectangle.RIGHT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 3 );
            //c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setBackgroundColor( uminnMaroon );
            setRunDirection( c );
            t.addCell(c);
            */
            
            Font fnt = fontLarge;

            if( competencyList.size()>0 )
                competencyList.get( competencyList.size()-1).setLast(true);
            
            int scrDigits = 0;
         
            String scrValue;            
            
            for( UMinnCompetency uc : competencyList )
            {
                c = new PdfPCell( new Phrase( uc.getName(), fnt ) );
                
                if( uc.isLast() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                //c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                
                setRunDirection( c );
                t.addCell(c);

                
                if( numScoresOn )
                {
                    scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  uc.testEventScore.getScoreFormatType().getScorePrecisionDigits();                    
                    scrValue = I18nUtils.getFormattedNumber(reportData.getLocale(), uc.getScoreToUseInReports( scoreScheme ), scrDigits );
                    // scrValue = I18nUtils.getFormattedNumber(reportData.getLocale(), uc.testEventScore.getScore(), scrDigits );
                    
                    c = new PdfPCell( new Phrase( scrValue, fnt ) );

                    if( uc.isLast() )
                        c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    //c.setPaddingBottom( 5 );
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER);
                    setRunDirection( c );
                    t.addCell(c);
                }                

                c = new PdfPCell( new Phrase( uc.getScoreName(scoreScheme), fnt ) );
                //if( uc.isLast() )
                //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT );
                //else
                //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                if( uc.isLast() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT  | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT  | Rectangle.RIGHT : Rectangle.TOP | Rectangle.RIGHT | Rectangle.LEFT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                //c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                
                setRunDirection( c );
                t.addCell(c);

                /*
                c = new PdfPCell( new Phrase( "", fnt ) );
                if( uc.isLast() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT  | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT  | Rectangle.RIGHT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                //c.setPaddingBottom( 5 );
                //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                // c.setBackgroundColor( uminnMaroon );
                c.setCellEvent(new CT2SummaryScoreGraphicCellEvent( uc.testEventScore , reportData.p, uc.getScoreCategoryType(), baseFontCalibri, true, ct2Colors, false, false ) );
                
                setRunDirection( c );
                t.addCell(c);   
                */
            }            
            
            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
            
            y = y - tableH - 2;
            
            currentYLevel = y;            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addCompetencySummary()" );

            throw new STException( e );
        }
        
    }
    
    public void addFeedbackReportOverview() throws Exception
    {
        try
        {
            float y = addTitle( pageHeight - headerHgt - 30 , lmsg("fro.title"), null );  
            
            y -= TPAD;
            
                        // First, add a table
            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ) );
            
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 5 );
            // c.setPaddingRight( 15 );
            // c.setPaddingBottom( 25 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);
            
            Font tcFont = this.getFontLarge();
            
            for( int i=1;i<=3;i++ )            
            {
                if( useAltusLangKeys && i>1 )
                    break;
                
                t.addCell( new Phrase( lmsg("fro." + i + altusKeySuffix), tcFont ) );
            }

            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
            
            y =  y - tableH - 2;
            
            currentYLevel = y;
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addFeedbackReportOverview()" );

            throw new STException( e );
        }
        
    }
    
    
    public void addPCModelOverview() throws Exception
    {
        try
        {
            float y = addTitle( pageHeight - headerHgt - 30 , lmsg("pcm.title"), null );  
            
            y -= TPAD;
                        // First, add a table
            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ) );
            
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 5 );
            // c.setPaddingRight( 15 );
            // c.setPaddingBottom( 25 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);
            
            Font tcFont = this.getFontLarge();
            
            t.addCell( new Phrase( lmsg("pcm.1"), tcFont ) );

            c = new PdfPCell( pcmImage );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment(Element.ALIGN_CENTER );
            c.setPadding( 0 );
            c.setPaddingTop( 12 );
            c.setPaddingBottom( 12 );
            setRunDirection(c);
            t.addCell( c );            

            t.addCell( new Phrase( lmsg("pcm.2"), tcFont ) );
            
            t.addCell( new Phrase( lmsg("pcm.3"), tcFont ) );

            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addPCModelOverview()" );

            throw new STException( e );
        }        
    }
    
    
    public void addContents() throws Exception
    {
        try
        {
            boolean numScoresOn = reportData.getReportRuleAsBoolean( "ovrnumon" );
            
            float y = addTitle( pageHeight - headerHgt - 60 , lmsg("contents"), null );  
            
            y -= TPAD;
            
                        // First, add a table
            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( pageWidth - 2*(CT2_MARGIN + CT2_TEXT_EXTRAMARGIN - 2 ) );
            
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 5 );
            // c.setPaddingRight( 15 );
            // c.setPaddingBottom( 25 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);
            
            Font tcFont = this.getFontLarge();
            
            Paragraph p = new Paragraph( lmsg("profcompmodel"), tcFont );            
            DottedLineSeparator dls = new DottedLineSeparator();            
            p.add( new Chunk( dls ) );            
            p.add( "3" );
            t.addCell( p );

            String spacer = "   ";
            
            p = new Paragraph( lmsg("feedbackoverview"), tcFont);            
            p.add( new Chunk( dls ) );            
            p.add( "4" );
            t.addCell( p );

            if( numScoresOn )
            {
                p = new Paragraph( spacer + lmsg("fro.ovrallsumm"), tcFont );            
                p.add( new Chunk( dls ) );            
                p.add( "4" );
                t.addCell( p );                
            }
            
            p = new Paragraph( spacer + lmsg("fro.compencysumm"), tcFont );            
            p.add( new Chunk( dls ) );            
            p.add( "4" );
            t.addCell( p );

            p = new Paragraph( spacer + lmsg("fro.compare"), tcFont );            
            p.add( new Chunk( dls ) );            
            p.add( "4" );
            t.addCell( p );
            
            p = new Paragraph( lmsg("competencydetails"), tcFont );            
            p.add( new Chunk( dls ) );            
            p.add( "5" );
            t.addCell( p );

            p = new Paragraph( lmsg("scenariolevelreport"), tcFont );            
            p.add( new Chunk( dls ) );            
            p.add( "12" );
            t.addCell( p );

            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y; //  + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
            
            
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addContents()" );

            throw new STException( e );
        }
    }
    
    
    
    
    public float addTitle( float startY, String title, String subtitle ) throws Exception
    {
        try
        {
            //if( !reportData.getIsLTR() )
            //    return addTitleRTL( startY,  title,  subtitle );

            if( startY > 0 )
            {
                float ulY = startY - 16* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt =   getHeaderFontXLarge();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            // No subtitle
            if( subtitle==null || subtitle.isEmpty() )
                return y;

            // Change getFont()
            fnt =  getFontLarge();

            float leading = fnt.getSize();

            float spaceLeft = y - PAD - footerHgt;

            // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

             y -=  PAD;//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();
            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText. If RTL need to use Column Text anyway.
            if( reportData.getIsLTR() && txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, subtitle, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt, false );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() RTL or overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                setRunDirection( ct );

                Phrase p = new Phrase( subtitle, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() adding second column "  );

                    document.newPage();

                    ct.setSimpleColumn( colDims2.getLeft(), colDims2.getBottom(), colDims2.getRight(), colDims2.getTop() );

                    ct.setYLine( colDims2.getTop() );

                    status = ct.go();

                    currentYLevel = ct.getYLine();
                }


                return currentYLevel;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addTitleAndSubtitle()" );

            throw new STException( e );
        }
    }
    
 
   public float addBoldRegPair( float startY, String boldTxt, String regTxt ) throws Exception
    {
        try
        {
            //if( !reportData.getIsLTR() )
            //    return addTitleRTL( startY,  title,  subtitle );

            if( startY > 0 )
            {
                float ulY = startY - 6* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt =   getFontLargeBold();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, boldTxt, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            // No subtitle
            if( regTxt==null || regTxt.isEmpty() )
                return y;

            // Change getFont()
            fnt =  getFontLarge();

            float leading = fnt.getSize();

            float spaceLeft = y - PAD - footerHgt;

            // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, regTxt, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

             y -=  PAD;//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();
            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText. If RTL need to use Column Text anyway.
            if( reportData.getIsLTR() && txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, regTxt, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt, false );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                LogService.logIt( "BaseUMinnReportTemplate.addBoldRegPair() RTL or overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseUMinnReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                setRunDirection( ct );

                Phrase p = new Phrase( regTxt, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    LogService.logIt( "BaseUMinnReportTemplate.addBoldRegPair() adding second column "  );

                    document.newPage();

                    ct.setSimpleColumn( colDims2.getLeft(), colDims2.getBottom(), colDims2.getRight(), colDims2.getTop() );

                    ct.setYLine( colDims2.getTop() );

                    status = ct.go();

                    currentYLevel = ct.getYLine();
                }


                return currentYLevel;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addBoldRegPair()" );

            throw new STException( e );
        }
    }
     
    
    
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
            PdfPTable t = new PdfPTable( 4 );  
            t.setWidths(new float[] { 20,20,70,20 } );
            t.setTotalWidth( tableWid );
            t.setLockedWidth( true );
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(t);
            
            c = new PdfPCell( new Phrase("") );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor(uminnMaroon);
            setRunDirection( c );
            t.addCell(c);            

            c = new PdfPCell( headerLogo );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setBackgroundColor(uminnMaroon);
            setRunDirection( c );
            t.addCell(c);            

            Phrase ph = new Phrase( lmsg( "g.Univ" + altusKeySuffix ), titleHeaderFont);
            
            if( !useAltusLangKeys )
                ph.add( new Chunk( "\n" + lmsg( "g.driven" ), titleHeaderFont2));
            
            c = new PdfPCell( ph );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 5 );            
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBackgroundColor(uminnMaroon);
            setRunDirection( c );
            t.addCell(c);            

            c = new PdfPCell( new Phrase( "", titleHeaderFont2) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 5 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor(uminnMaroon);
            setRunDirection( c );
            t.addCell(c);            
            
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

            cl.add( new Chunk( lmsg( "url" + altusKeySuffix ), fontXLargeBoldWhite ) );

            if( !useAltusLangKeys )
                cl.add( new Chunk( lmsg( "twitter" ), fontXLargeBoldWhite ) );
           
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
                             
            t.addCell( new Phrase( "", fontXLargeBoldWhite ) );
            t.addCell( new Phrase( lmsg( "url" + altusKeySuffix ), fontXLargeBoldWhite ) );

            
            if( !useAltusLangKeys )
            {
                t.addCell( twitterLogo );
                t.addCell( new Phrase( lmsg( "twitter" ), fontXLargeBoldWhite ) );
            }

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

    
    public void addNewPage() throws Exception
    {
        document.newPage();
        this.currentYLevel = pageHeight - PAD -  headerHgt;
    }

    
    
    
    protected void addPreparationNotesSection() throws Exception
    {
        try
        {
            List<String> prepNotes = new ArrayList<>();
            
            // LogService.logIt(  "BaseUMinnReportTemplate.addPreparationNotesSection() START" );

            //if( reportData.getReport().getIncludeNorms()>0 && hasComparisonData() )
             //    prepNotes.add( 0, lmsg( "g.CT3ComparisonVsOverallNote" ) );



            //if( !devel )
            //    prepNotes.add( 0, lmsg( "g.CT3RptCaveat" ) );
            //else
            //    prepNotes.add( 0, lmsg( "g.CT3RptCaveatDevel" ) );

            Product p = reportData.getTestEvent().getProduct();

            Calendar cal = new GregorianCalendar();            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");            
            String dtStr = df.format( cal.getTime() );
            
            prepNotes.add( lmsg( "g.SimIdAndVersion", new String[]{ Long.toString( reportData.getTestEvent().getSimId()) , Integer.toString(reportData.getTestEvent().getSimVersionId() ), Long.toString( reportData.getTestEvent().getTestKeyId()), Long.toString( reportData.getTestEvent().getTestEventId()), Integer.toString( p.getProductId()), dtStr } ));

            if( prepNotes.isEmpty() )
                return;

            if( currentYLevel <= footerHgt + 200 )            
            {    addNewPage();
                 previousYLevel =  currentYLevel;                 
            }

            
            
            float y = currentYLevel; // addTitle( currentYLevel, lmsg( "g.PreparationNotes" ), null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 4,70 } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            Font tcFont = this.getFont();
            Font bFont = this.getFontXLargeBold();


            c = new PdfPCell( new Phrase( lmsg( "g.PreparationNotes" ), this.getFontBold() ));
            c.setBorder( Rectangle.NO_BORDER );
            c.setColspan(2);
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            //c.setBackgroundColor( BaseColor.WHITE );
            //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 4 );
            setRunDirection( c );
            t.addCell(c);
            
            for( String ct : prepNotes )
            {
                if( ct.isEmpty() )
                    continue;

                c = new PdfPCell( new Phrase( BULLET, bFont ));
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                //c.setBackgroundColor( BaseColor.WHITE );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 3 );
                setRunDirection( c );
                t.addCell(c);
                c = new PdfPCell( new Phrase( ct, tcFont ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                //c.setBackgroundColor( BaseColor.WHITE );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 3 );
                setRunDirection( c );
                t.addCell(c);
            }
            

            float tableH = t.calculateHeights(); //  + 500;            
            
            float tableY = y - tableH; //   pageHeight/2 - (pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseUMinnReportTemplate.addPreparationNotesSection()" );

            throw new STException( e );
        }
    }

    
    
    
    

    @Override
    public void dispose() throws Exception
    {
        if( baos != null )
            baos.close();
    }


    public void closeDoc() throws Exception
    {
        if( document != null && document.isOpen() )
            document.close();

        document = null;
    }


    public byte[] getDocumentBytes() throws Exception
    {
        if( baos == null )
            return null;

        return baos.toByteArray();
    }

    /**
     * Override this method as needed.
     *
     * @return
     */
    @Override
    public boolean getIsReportGenerationPossible()
    {
        return true;
    }


    
    public void setRunDirection( PdfPCell c )
    {
        if( c == null || reportData == null || reportData.getLocale() == null )
            return;

        // if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        c.setRunDirection( reportData.getTextRunDirection() );
    }

    public void setRunDirection( PdfPTable t )
    {
        if( t == null || reportData == null || reportData.getLocale() == null )
            return;

        t.setRunDirection( reportData.getTextRunDirection() );

        //if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        //    t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }

    public void setRunDirection( ColumnText ct )
    {
        if( ct == null || reportData == null || reportData.getLocale() == null )
            return;

        ct.setRunDirection( reportData.getTextRunDirection() );

        //if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        //    t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }
    
    


}
