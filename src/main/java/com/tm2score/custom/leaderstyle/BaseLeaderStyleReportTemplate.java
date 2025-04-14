/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.leaderstyle;

import com.tm2score.custom.disc.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import com.tm2score.format.TableBackground;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


/**
 *
 * Report Rules:
 *
 * ct3risktoend=1 means place risk factors at the end of the report
 * ct3riskremove=1 means no risk factors in the report, anywhere.
 *
 * allnointerview=1 means do not include the interview guide
 *
 *
 * @author Mike
 */
public abstract class BaseLeaderStyleReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    public String feedbackReportCoverImageUrl;
    public String[] feedbackReportFooterImageUrls;
    public static String feedbackReportBarFooterImageUri = "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_15x1741971514005.png";

    public static String[] sideTabIconUris = new String[] {
        "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_3x1742049764524.png",
        "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_5x1742049764529.png",
        "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_4x1742049764527.png",
        "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_2x1742049764524.png"
    };


    // public BaseColor hraBlue = new BaseColor( 0x00, 0x77, 0xcc );
    public static BaseColor babyBlue = new BaseColor( 0xd2, 0xec, 0xff );

    public boolean manager = true;
    public String bundleToUse = null;
    public LeaderStyleReportUtils leaderStyleReportUtils;


    public float footerHeight = 50;
    public float footerBarHeight = 12;
    public int footerImageIndex = 0;



    /*
      index 
            0=Authoritarion
            1=Democratic
            2=Laissez Faire
            3=Transactional
            4=Transformational
    */
    public float[] leaderStyleScoreVals;

    /*
      top trait index.
    */
    public int dominantStyleIndex;

    public String dominantStyleStub;
    public String dominantStyleName;



    public synchronized void specialInit()
    {
        if( leaderStyleReportUtils==null )
        {
            if( reportData.getReport().getStrParam6() !=null && !reportData.getReport().getStrParam6().isEmpty() )
                bundleToUse = reportData.getReport().getStrParam6();

            if( bundleToUse==null || bundleToUse.isEmpty() )
            {
                Locale loc = reportData.getLocale();
                // String stub = "";
                if( loc.getLanguage().equalsIgnoreCase( "en" ) )
                    bundleToUse = "leaderstylereport.properties";
                else
                    bundleToUse = "leaderstylereport_" + loc.getLanguage().toLowerCase() + ".properties";
            }

            leaderStyleReportUtils = new LeaderStyleReportUtils( bundleToUse );
        }

        leaderStyleScoreVals = LeaderStyleReportUtils.getLeaderStyleScoreVals( reportData.getTestEvent() );
        dominantStyleIndex = LeaderStyleReportUtils.getTopTraitIndex(leaderStyleScoreVals);
        dominantStyleStub = LeaderStyleReportUtils.getCompetencyStub(dominantStyleIndex);
        dominantStyleName = lmsg_spec(LeaderStyleReportUtils.getCompetencyStubLetter(dominantStyleIndex) + ".name" );

        // LogService.logIt( "BaseDiscReportUtils.specialInit() feedbackReportCoverImageUrl=" + feedbackReportCoverImageUrl +", topTraitIndexes[0]=" + topTraitIndexes[0] + ", topTraitIndexes[1]=" + topTraitIndexes[1] + ", stub=" + stub + ", highNamePair=" + highNamePair );

        try
        {
            URL u = reportData.getLocalImageUrl(feedbackReportCoverImageUrl);
            if( u!=null )
            {
                hraCoverPageImage2 = ITextUtils.getITextImage(u );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.specialInit() testEventId=" + (reportData!=null && reportData.te!=null ? reportData.te.getTestEventId() : "null") + ", reportId=" + (reportData!=null && reportData.r2Use!=null ? reportData.r2Use.getReportId() : "null") + ", feedbackReportCoverImageUrl=" + feedbackReportCoverImageUrl );
        }
    }


    @Override
    public void addReportInfoHeader() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            // Font fnt = getFontXLarge();
            if( reportData.getReportRuleAsBoolean( "ovroff" ) )
                return;

            // boolean hideOverallNumeric = reportData.getReportRuleAsBoolean( "ovrnumoff" );
            // boolean hideOverallGraph = reportData.getReportRuleAsBoolean( "ovrgrphoff" );
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );

            float y = addTitleLarge( previousYLevel, lmsg( "g.Overall" ), getFontXXLargeBoldDarkBlue() );

            y -= TPAD;

            int scrDigits = reportData.getReport().getIntParam2() >= 0 ? reportData.getReport().getIntParam2() : reportData.getTestEvent().getScorePrecisionDigits();

            float[] colRelWids = new float[] { 1f };

            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();
            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            String scrTxt = lmsg_spec(manager ? "ls.DomStyleMgr" : "ls.DomStyleFbk", new String[]{reportData.getUserName(),dominantStyleName}); 
            String scrTxt2 = lmsg_spec(dominantStyleStub  + ".description"); 

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( 1 );

            setRunDirection( touter );
            touter.setTotalWidth( pageWidth ); // - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Leader"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD + CT2_MARGIN + CT2_TEXT_EXTRAMARGIN );
            c.setBackgroundColor( ct2Colors.hraBlue );
            // c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            touter.writeSelectedRows(0, -1,0, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights() - 5;

            y = currentYLevel;

            // Inner Table
             colRelWids = new float[] { 1f, 2f };

            touter = new PdfPTable( 2 );
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setFixedHeight( 2 );
            c.setColspan(2);
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( BaseColor.WHITE );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );

            // NAME
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            c.setColspan(2);
            c.setPadding( 1 );
            c.setPaddingTop(4);
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( BaseColor.WHITE );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            touter.addCell( c );

            // Next Row - Email
            if( reportData.getUser().getUserType().getNamed() &&
                reportData.getUser().getEmail() != null &&
                !reportData.getUser().getEmail().isEmpty() &&
                !StringUtils.isCurlyBracketed( reportData.getUser().getEmail() ) )
            {
                c = new PdfPCell( new Phrase( reportData.getUser().getEmail(), getFontLargeLight() ) );
                c.setColspan(2);
                c.setPadding( 1 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            // Next row - test name
            c = new PdfPCell( new Phrase( reportData.getSimName(), getFontLargeLight() ) );
            c.setColspan(2);
            c.setPadding( 1 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );

            // Next Row, test date
            if( includeDates )
            {
                c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFontLargeLight() ) );
                c.setColspan(2);
                c.setPadding( 1 );
                // c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            if( hasThirdPartyId )
            {
                c = new PdfPCell( new Phrase( thirdPartyId, getFontLargeLight() ) );
                c.setColspan(2);
                c.setPadding( 1 );
                if( reportData.getIsLTR() )
                {
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setPaddingBottom(4);
                }
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            if( reportData.getUser().getMobilePhone()!=null && !reportData.getUser().getMobilePhone().isEmpty() )
            {
                c = new PdfPCell( new Phrase( "(m) " + reportData.getUser().getMobilePhone(), getFontLargeLight() ) );
                c.setColspan(2);
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            if( scrTxt != null && !scrTxt.isEmpty())
            {
                c = new PdfPCell( new Phrase( scrTxt, fontXLargeBoldDarkBlue ) );
                c.setColspan(2);
                c.setPadding( 5 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                // c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setPaddingLeft( 100 );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );

                c = new PdfPCell( new Phrase( scrTxt2, getFontLargeLight() ) );
                c.setColspan(2);
                c.setPadding( 5 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );                
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }
            
            if( !reportData.getReportRuleAsBoolean("cmptysumoff") && !reportData.getReportRuleAsBoolean("cmptynumoff") )
            {
                PdfPTable tscores = new PdfPTable( 2 );
                //tscores.setWidths( new float[]{2,1} );
                tscores.setHorizontalAlignment(Element.ALIGN_LEFT);

                for( int i=0;i<LeaderStyleReportUtils.LEADER_STYLE_STUBS.length;i++ )
                {
                    c = new PdfPCell( new Phrase( lmsg_spec(LeaderStyleReportUtils.LEADER_STYLE_STUBS[i] + ".name") + ":", fontLargeBoldDarkBlue ) );
                    c.setPadding( 2 );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setBorder( Rectangle.NO_BORDER );
                    setRunDirection( c );
                    tscores.addCell( c );

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber( reportData.getLocale(), leaderStyleScoreVals[i], scrDigits), fontLargeBoldDarkBlue ) );
                    c.setPadding( 2 );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setBorder( Rectangle.NO_BORDER );
                    setRunDirection( c );
                    tscores.addCell( c );                
                }

                c = new PdfPCell( new Phrase( lmsg_spec("ls.scoresbystyle") + ":", fontLargeBoldDarkBlue ) );
                c.setPadding( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT  | Rectangle.BOTTOM : Rectangle.RIGHT  | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );

                c = new PdfPCell( tscores );
                c.setPadding( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );                
                c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT  | Rectangle.BOTTOM : Rectangle.LEFT  | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }
            
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addReportInfoHeader()" );
            throw e;
        }
    }

    public void addHowUseThisReport() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg_spec( "ls.howuse" ), null, this.fontXLargeBoldDarkBlue, null );

            y -= TPAD;

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            c = touter.getDefaultCell();
            c.setPadding( 3 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            String val;
            for( int i=1; i<=20; i++ )
            {
                val = lmsg_spec(  "ls.howuse.fbk." + i );
                if( val==null || val.contains("KEY NOT FOUND" ))
                    break;
                c = new PdfPCell( new Phrase( val, font ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setBorderWidth( 0 );
                c.setPadding( 4 );
                setRunDirection( c );
                touter.addCell(c);
            }

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addHowUseThisReport()" );
            throw e;
        }
        
    }
    
    public void addFooterBar( String title, boolean includeImage, Font fnt ) throws Exception
    {
            // First, add a table
            PdfPTable touter = new PdfPTable( includeImage ? 1 : 2 );

            if( !includeImage )
                touter.setWidths( new float[] {10, 2} );

            setRunDirection( touter );
            touter.setTotalWidth( pageWidth );
            touter.setLockedWidth( true );

            BaseFont bf = fnt.getBaseFont();
            Font blueFont = new Font(bf, fnt.getSize()+4);
            blueFont.setColor( babyBlue);

            // Create header
            PdfPCell c = new PdfPCell( new Phrase( title, blueFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment(Element.ALIGN_MIDDLE );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( includeImage ? 16 : 0 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD + CT2_MARGIN + CT2_TEXT_EXTRAMARGIN );
            c.setBackgroundColor( ct2Colors.hraBlue );
            // c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            if( !includeImage )
            {
                String footerImageUri = feedbackReportBarFooterImageUri;
                URL footerImageURL = reportData.getLocalImageUrl( footerImageUri );
                Image footerImage = ITextUtils.getITextImage( footerImageURL );
                // LogService.logIt( "BaseLeaderStyleReportTemplate.addFooterBar() footerImageIndex=" + footerImageIndex + ", footerImageUri=" + footerImageUri + ", footerImage=" + (footerImage!=null) );
                if( footerImage !=null )
                {
                    footerImage.scalePercent(21.875f);
                    // footerImage.scalePercent(7);
                    // ITextUtils.addDirectImage( pdfWriter, footerImage, pageWidth-footerImage.getScaledWidth() + 1, 19, false );
                }

                c = new PdfPCell( footerImage );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 0 );
                c.setBackgroundColor( ct2Colors.hraBlue );
                // c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
                setRunDirection( c );
                touter.addCell(c);
            }

            float y = footerHeight + footerBarHeight;

            touter.writeSelectedRows(0, -1,0, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights() - 5;

            // y = currentYLevel;

            if( includeImage )
            {
                footerImageIndex++;
                if( footerImageIndex>2 )
                    footerImageIndex=0;
                String footerImageUri = feedbackReportFooterImageUrls[footerImageIndex];
                URL footerImageURL = reportData.getLocalImageUrl( footerImageUri );
                Image footerImage = ITextUtils.getITextImage( footerImageURL );
                // LogService.logIt( "BaseLeaderStyleReportTemplate.addFooterBar() footerImageIndex=" + footerImageIndex + ", footerImageUri=" + footerImageUri + ", footerImage=" + (footerImage!=null) );
                if( footerImage !=null )
                {
                    footerImage.scalePercent(60);
                    ITextUtils.addDirectImage( pdfWriter, footerImage, pageWidth-footerImage.getScaledWidth() + 1, 0, false );
                }
            }

            //else
            //{
             //   String footerImageUri = feedbackReportBarFooterImageUri;
             //   URL footerImageURL = reportData.getLocalImageUrl( footerImageUri );
             //   Image footerImage = ITextUtils.getITextImage( footerImageURL );
             //   LogService.logIt( "BaseLeaderStyleReportTemplate.addFooterBar() footerImageIndex=" + footerImageIndex + ", footerImageUri=" + footerImageUri + ", footerImage=" + (footerImage!=null) );
             //   if( footerImage !=null )
             //   {
             //       footerImage.scalePercent(8);
              //      ITextUtils.addDirectImage( pdfWriter, footerImage, pageWidth-footerImage.getScaledWidth() + 1, 19, false );
              //  }

            // }


    }


    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CT2ReportData();

        initLocales();

        initFonts();

        parseCustomLogos();

        initColors();

        if( 1==1 )
        {
            if( ct2Colors!=null )
                ct2Colors.clearBorders();

            scoreBoxBorderWidth = 0;
            lightBoxBorderWidth=0;
        }

        parseCustomColors();

        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        // LogService.logIt( "BaseLeaderStyleReportTemplate.init() title=" + rd.getReportName() );

        // Must come before create HeaderFooter
        specialInit();

        DiscHeaderFooter hdr = new DiscHeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this, custLogo, feedbackReportFooterImageUrls );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );
        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();
        float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );

        headerHgt = hghts[0];
        footerHgt = hghts[1];

        usablePageHeight = pageHeight - headerHgt - footerHgt - 4*PAD;

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

        tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );
    }


    public void addPurposeOfAssessment() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            float y = addTitle(previousYLevel, lmsg_spec( "ls.Purpose" ), null, this.fontXLargeBoldDarkBlue, null );
            y -= TPAD;

            BaseFont bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( new BaseColor( 0x0b, 0x50, 0x8b) );
            Font listItemFont = fontLarge;
            Font listItemFontBold = fontLargeBold;
                        
            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            String summary = lmsg_spec("ls.Purpose.P1");           
            c = new PdfPCell( new Phrase( summary, listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            setRunDirection( c );
            touter.addCell(c);
            
            
            List<String[]> itemListPairs = lmsg_spec_list_pairs("ls.Purpose");
            addListItemPairGroupToTable(touter, null, itemListPairs, listHeaderFont, listItemFontBold, listItemFont, 0);
            itemListPairs.clear();
            
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addPurposeOfAssessment() " + reportData.toString() );
            throw e;
        }
    }
            


    public void addLeaderStylesExplained() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg_spec( "ls.StylesExpl" ), null, this.fontXLargeBoldDarkBlue, null );

            y -= TPAD;

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 2 );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f, 4f } );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            //BaseFont bf = fontBold.getBaseFont();
            Font fnt = this.fontLargeBoldDarkBlue; //  fontBold;
            // String styleLetter;

            for( int i=0; i<5; i++ )
            {
                //fnt = new Font(bf, LFONTSZ);
                //fnt.setColor( DiscReportUtils.sliceBaseColors[i] );
                c = new PdfPCell( new Phrase( lmsg_spec(  LeaderStyleReportUtils.LEADER_STYLE_STUBS[i] + ".name" ), fnt ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                c.setPaddingBottom(10);
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( lmsg_spec(  LeaderStyleReportUtils.LEADER_STYLE_STUBS[i] + ".description" ), font ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                c.setPaddingBottom(12);
                setRunDirection( c );
                touter.addCell(c);
            }

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addLeaderStylesExplained() " + reportData.toString() );
            throw e;
        }
    }

    public float addTitleLarge( float startY, String title, Font fnt ) throws Exception
    {
        try
        {
            if( !reportData.getIsLTR() )
                return addTitleLargeRTL( startY,  title, fnt );

            float tHeight = getTitleHeightLarge( title, fnt );
            if( startY > 0 )
            {
                float ulY = startY - PAD - tHeight;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    startY = 0;
                    currentYLevel = pageHeight - PAD -  headerHgt;
                }
            }

            previousYLevel =  currentYLevel;

            // Font fnt =   getHeaderFontXXLarge();
            // float leading = fnt.getSize();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;

            // Add Title
            ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            return y;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTitleAndSubtitle() " + reportData.toString() );
            throw new STException( e );
        }
    }

    public float addTitleLargeRTL( float startY, String title, Font fnt ) throws Exception
    {
        try
        {
            if( startY>0 )
            {
                float ulY = startY - 16* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    startY = 0;
                }
            }

            previousYLevel =  currentYLevel;

            // Font fnt =   getHeaderFontXXLarge();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;

            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            t.addCell( new Phrase( title , fnt ) );

            float ht = t.calculateHeights(); //  + 500;

            float tw = t.getTotalWidth();

            float tableX = (pageWidth - tw )/2;

            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );

            currentYLevel = y - ht;

            return currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addTitleLargeRTLLarge() " + reportData.toString() );
            throw new STException( e );
        }
    }




    public float getTitleHeightLarge( String title, Font fnt ) throws Exception
    {
        try
        {
            // Font fnt =   getHeaderFontXXLarge();
            // Change getFont()
            float leading = fnt.getSize();

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            // float y = previousYLevel - 6*PAD - getFont().getSize();
            float h = ITextUtils.getDirectTextHeight( pdfWriter, title, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

            h += PAD;
            return h;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.getTitleHeightLarge() " + reportData.toString() );
            throw new STException( e );
        }
    }





    public void addTopTraitSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String titleKey = manager ? "ls.DomStyleMgr" : "ls.DomStyleFbk";

            String sectionTitle = lmsg_spec(titleKey, new String[]{ reportData.getUserName(),  lmsg_spec(LeaderStyleReportUtils.LEADER_STYLE_STUBS[dominantStyleIndex]+".name")});

            //BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            //String ttrait = lmsg_spec(DiscReportUtils.getCompetencyStubLetter(dominantStyleIndex[0])+".name").toUpperCase();
            Font fnt = getFontXXLargeBoldDarkBlue(); //  new Font(bf, XXLFONTSZ);
            Chunk chk = new Chunk( lmsg_spec(LeaderStyleReportUtils.LEADER_STYLE_STUBS[dominantStyleIndex]+".name"), fnt);
            Paragraph phr = new Paragraph( chk );

            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( phr );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;

            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = pageHeight - headerHgt - TPAD;

            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );

            currentYLevel = y - ht - TPAD;
            y = currentYLevel;

            // First, add a table
            PdfPTable touter = new PdfPTable( 1 );

            setRunDirection( touter );
            touter.setTotalWidth( pageWidth ); // - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setLockedWidth( true );

            // Create header
            c = new PdfPCell( new Phrase( sectionTitle, fontXLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD + CT2_MARGIN + CT2_TEXT_EXTRAMARGIN );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);

            touter.writeSelectedRows(0, -1,0, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights() - 5;

            y = currentYLevel - TPAD;

            touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            BaseFont bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( new BaseColor( 0x0b, 0x50, 0x8b) );
            Font listItemFont = fontLarge;
            Font listItemFontBold = fontLargeBold;
            
            // Summary
            String summary = lmsg_spec(LeaderStyleReportUtils.LEADER_STYLE_STUBS[dominantStyleIndex]+".description");           
            c = new PdfPCell( new Phrase( summary, listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            setRunDirection( c );
            touter.addCell(c);
                       

            List<String> itemList; 
            List<String[]> itemListPairs;
            Map<String,List<String>> twoTierMap;
            
            String listTitle = lmsg_spec( "ls.Strengths");
            itemListPairs = lmsg_spec_list_pairs(dominantStyleStub + ".strengths");
            addListItemPairGroupToTable(touter, listTitle, itemListPairs, listHeaderFont, listItemFontBold, listItemFont, 0);
            itemListPairs.clear();

            listTitle = lmsg_spec( "ls.AreasGrowth");
            itemListPairs = lmsg_spec_list_pairs(dominantStyleStub + ".growth");
            addListItemPairGroupToTable(touter, listTitle, itemListPairs, listHeaderFont, listItemFontBold, listItemFont, 0);
            itemListPairs.clear();
            
          
            listTitle = lmsg_spec( "ls.WhenWorks");
            itemList = lmsg_spec_list(dominantStyleStub + ".whenworks");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            listTitle = lmsg_spec( "ls.NotWorks");
            itemList = lmsg_spec_list(dominantStyleStub + ".notworks");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
            
            addFooterBar( lmsg( "g.TRAITS"), false, fontXLargeBoldWhite );
                        
            addNewPage();
            previousYLevel = currentYLevel;
            y = previousYLevel;
            
            touter = new PdfPTable( 1 );
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            setRunDirection( touter );
            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            
            listTitle = lmsg_spec( manager ? "ls.BestDISCForX" : "ls.BestDISCForYou", new String[]{reportData.getUserName()} );
            twoTierMap = lmsg_spec_twotiermap(dominantStyleStub + ".bestdisc");
            addTwoTierListItemGroupToTable(touter, listTitle, twoTierMap, listHeaderFont, listItemFontBold, listItemFont, 0);
            twoTierMap.clear();

            listTitle = lmsg_spec( manager ? "ls.WorstDISCForX" : "ls.WorstDISCForYou", new String[]{reportData.getUserName()});
            twoTierMap = lmsg_spec_twotiermap(dominantStyleStub + ".worstdisc");
            addTwoTierListItemGroupToTable(touter, listTitle, twoTierMap, listHeaderFont, listItemFontBold, listItemFont, 0);
            twoTierMap.clear();

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addTopTraitSection() " + reportData.toString() );
            throw e;
        }
    }

    protected void addCitationsSection() throws Exception
    {
        // LogService.logIt( "BaseCT2ReportTemplate.addMinimalPrepNotesSection() START " );

        previousYLevel =  currentYLevel;

        // First create the table
        PdfPCell c;

        // First, add a table
        PdfPTable t = new PdfPTable( new float[] { 1f } );

        float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

        // t.setHorizontalAlignment( Element.ALIGN_CENTER );
        t.setTotalWidth( outerWid );
        t.setLockedWidth( true );
        setRunDirection( t );

        c = new PdfPCell( new Phrase( lmsg_spec("ls.Citations"), this.fontLargeBold ));
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( BaseColor.WHITE );
        c.setPaddingTop( 5 );
        setRunDirection( c );
        t.addCell(c);

        for( int i=1;i<=2;i++ )
        {
            c = new PdfPCell( new Phrase( lmsg_spec("ls.citation." + i), this.fontItalic ));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( BaseColor.WHITE );
            c.setPaddingTop( 5 );
            setRunDirection( c );
            t.addCell(c);
        }

        currentYLevel = addTableToDocument(currentYLevel, t, false, true );
    }
    
    
    
    public void addBlueBar() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            PdfContentByte pcb = pdfWriter.getDirectContent();
            pcb.saveState();

            pcb.setColorStroke( ct2Colors.hraBlue );
            pcb.setColorFill( ct2Colors.hraBlue );
            pcb.setLineWidth(0 );
            float llx = CT2_MARGIN;
            float lly = previousYLevel - 2*TPAD;
            float wid = pageWidth - 2*CT2_MARGIN;
            float hgt = 3;
            pcb.rectangle(llx, lly, wid, hgt);
            pcb.fill();
            pcb.restoreState();

            currentYLevel = currentYLevel - 3*TPAD - hgt;
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addBlueBar() " + reportData.toString() );
            throw e;
        }

    }


    public void addDiscManagerInfoSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;


            float y = addTitle(previousYLevel, lmsg_spec("ls.InfoForManagers"), null, null, null );

            y -= TPAD;

            Font subheaderFont = fontBold;
            Font contentFont = font;
            Font listHeaderFont = subheaderFont;
            Font listItemFont = contentFont;

            int padding=2;


            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // KEY ACTIONS
            String titleKey = "mgr.actions.title";
            String sectionTitle = lmsg_spec( titleKey);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( padding );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);


            Paragraph par = new Paragraph();
            Chunk chk = new Chunk( lmsg_spec("mgr.actions.1.title") + ":\n", subheaderFont );
            par.add(chk );
            chk = new Chunk( lmsg_spec("mgr.actions.1.1"), contentFont );
            par.add( chk );
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            par = new Paragraph();
            chk = new Chunk( lmsg_spec("mgr.actions.2.title") + ":\n", subheaderFont );
            par.add(chk );
            List<String> itemList = new ArrayList<>();
            for( int i=1;i<=3; i++ )
            {
                itemList.add( lmsg_spec("mgr.actions.2." + i ) );
            }
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);
            addListItemGroupToTable(touter, null, itemList, listHeaderFont, listItemFont, 0);


            for( int i=3;i<=5;i++ )
            {
                par = new Paragraph();
                chk = new Chunk( lmsg_spec("mgr.actions." + i + ".title") + ":\n", subheaderFont );
                par.add(chk );
                chk = new Chunk( lmsg_spec("mgr.actions." + i + ".1"), contentFont );
                par.add( chk );
                c = new PdfPCell( par );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( padding );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                touter.addCell(c);
            }

            // BUFFER
            c = new PdfPCell( new Phrase( " ", contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( padding );
            touter.addCell(c);

            // HOW TO BUILD A TEAM
            titleKey = "mgr.howbldtm.title";
            sectionTitle = lmsg_spec( titleKey);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( padding );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.howbldtm.p1"), contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.howbldtm.p2"), contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            // BUFFER
            c = new PdfPCell( new Phrase( " ", contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( padding );
            touter.addCell(c);

            // AVOID STEREOTYPING
            titleKey = "mgr.avoids.title";
            sectionTitle = lmsg_spec( titleKey);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( padding );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.avoids.p1"), contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            chk = new Chunk( lmsg_spec("mgr.avoids.1.title")+"\n", contentFont );
            par = new Paragraph();
            par.add( chk );
            String url = lmsg_spec("mgr.avoids.1.url");
            if( url!=null && !url.isEmpty() )
            {
                chk = new Chunk( url, fontSmallItalicBlue );
                PdfAction pdfa = PdfAction.gotoRemotePage( url , lmsg_spec("ls.ClickToVisit"), false, true );
                chk.setAction( pdfa );
                par.add( chk );
            }
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseLeaderStyleReportTemplate.addDiscEducationSection()" );
            throw e;
        }
    }
    

    public String lmsg_spec( String key )
    {
        return leaderStyleReportUtils.getKey(key );
    }

    public String lmsg_spec( String key, String[] prms )
    {
        String msgText = leaderStyleReportUtils.getKey(key );
        return MessageFactory.substituteParams( reportData.getLocale() , msgText, prms );
    }

    public List<String> lmsg_spec_list( String key )
    {
        List<String> out = new ArrayList<>();
        String val;
        for( int i=1;i<100;i++ )
        {
            val =  leaderStyleReportUtils.getKey( key + "." + i );

            if( val==null || val.isBlank() || val.startsWith( "KEY NOT FOUND") )
                break;

            out.add(val);
        }

        return out;
    }

    public Map<String,List<String>> lmsg_spec_twotiermap( String key )
    {
        Map<String,List<String>> out = new TreeMap<>();
        String valt;
        String valc;
        List<String> valcList;
        for( int i=1;i<100;i++ )
        {
            valt =  leaderStyleReportUtils.getKey( key + "." + i + ".t" );

            if( valt==null || valt.isBlank() || valt.startsWith( "KEY NOT FOUND") )
                break;
            
            valcList = new ArrayList<>();
            for( int j=1;j<100;j++ )
            {
                valc =  leaderStyleReportUtils.getKey( key + "." + i + ".c." + j );
                if( valc==null || valc.isBlank() || valc.startsWith( "KEY NOT FOUND") )
                    break;
                valcList.add( valc );
            }
            
            out.put(valt,valcList);
        }

        return out;
        
    }
    
    public List<String[]> lmsg_spec_list_pairs( String key )
    {
        List<String[]> out = new ArrayList<>();
        String valt;
        String valc;
        for( int i=1;i<100;i++ )
        {
            valt =  leaderStyleReportUtils.getKey( key + "." + i + ".t" );

            if( valt==null || valt.isBlank() || valt.startsWith( "KEY NOT FOUND") )
                break;
            valc =  leaderStyleReportUtils.getKey( key + "." + i + ".c" );
            if( valc==null || valc.isBlank() || valc.startsWith( "KEY NOT FOUND") )
                valc="";

            out.add(new String[]{valt,valc});
        }

        return out;
    }


    public void addTwoTierListItemGroupToTable( PdfPTable tbl, String listTitle, Map<String,List<String>> twoTierMap, Font listHeaderFont, Font listItemFontBold, Font listItemFont, int extraLeftPadding) throws Exception
    {
        com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
        cl.setListSymbol( "\u2022");
        //cl.setIndentationLeft( 10 );
        //cl.setSymbolIndent( 10 );
        cl.setAutoindent(false);

        com.itextpdf.text.List cl2;
                
        ListItem li;
        ListItem li2;
        List<String> valcList;
        Chunk tc;
        Chunk vc;
        
        for( String s : twoTierMap.keySet() )
        {
            //tc = new Chunk( s, listItemFontBold );
            li = new ListItem(s, listItemFontBold);
           // li.add(tc);
            valcList = twoTierMap.get(s);
            
            cl2 = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED );
            cl2.setListSymbol( "\u2043");
            cl2.setIndentationLeft( 10 );
            cl2.setSymbolIndent( 10 );
            cl2.setAutoindent(false);
            for( String val : valcList )
            {
                li2 = new ListItem( "\n" + val, listItemFont );
                //li2.setLeading(8);
                //li2.setPaddingTop(3);
                //li2.setExtraParagraphSpace(3);      
                cl2.add( li2 );
                //vc = new Chunk(  "\n - " + val,listItemFont) ;
                //li.add( vc );
            }            
            li.add( cl2 );
                        
            li.setLeading(11);
            li.setPaddingTop(4);
            li.setExtraParagraphSpace(4);                        
            cl.add( li );
        }

        PdfPCell c;

        if( listTitle!=null && !listTitle.isBlank() )
        {
            c = new PdfPCell( new Phrase( listTitle, listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 4 );
            c.setPaddingLeft(4 + extraLeftPadding );
            c.setPaddingBottom(4);
            setRunDirection( c );
            tbl.addCell(c);
        }

        c = new PdfPCell();
        c.addElement( cl );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        c.setVerticalAlignment( Element.ALIGN_TOP );
        c.setBorderWidth( 0 );
        c.setPaddingLeft(4 + extraLeftPadding );
        c.setPaddingBottom( 4 );
        setRunDirection( c );
        tbl.addCell(c);
    }
    
    public void addListItemGroupToTable( PdfPTable tbl, String listTitle, List<String> itemList, Font listHeaderFont, Font listItemFont, int extraLeftPadding) throws Exception
    {
        com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
        cl.setListSymbol( "\u2022");
        cl.setIndentationLeft( 10 );
        cl.setSymbolIndent( 10 );

        ListItem li;
        for( String s : itemList )
        {
            li = new ListItem( 11,  s, listItemFont );
            li.setPaddingTop(4);
            li.setExtraParagraphSpace(4);
            cl.add( li );
        }

        PdfPCell c;

        if( listTitle!=null && !listTitle.isBlank() )
        {
            c = new PdfPCell( new Phrase( listTitle, listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 4 );
            c.setPaddingLeft(4 + extraLeftPadding );
            c.setPaddingBottom(4);
            setRunDirection( c );
            tbl.addCell(c);
        }

        c = new PdfPCell();
        c.addElement( cl );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        c.setVerticalAlignment( Element.ALIGN_TOP );
        c.setBorderWidth( 0 );
        c.setPaddingLeft(4 + extraLeftPadding );
        c.setPaddingBottom( 4 );
        setRunDirection( c );
        tbl.addCell(c);
    }


    public void addListItemPairGroupToTable( PdfPTable tbl, String listTitle, List<String[]> itemListPairs, Font listHeaderFont, Font listItemFontBold, Font listItemFont, int extraLeftPadding) throws Exception
    {
        com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
        cl.setListSymbol( "\u2022");
        cl.setIndentationLeft( 10 );
        cl.setSymbolIndent( 10 );

        ListItem li;
        Chunk tc;
        Chunk cc;
        
        for( String[] pair : itemListPairs )
        {
            tc = new Chunk( pair[0] + " ", listItemFontBold );
            cc = new Chunk( pair[1], listItemFont );
            li = new ListItem();
            li.add(tc);
            li.add(cc);
            li.setLeading(11);
            //li = new ListItem( 11,  s, listItemFont );
            
            li.setPaddingTop(4);
            li.setExtraParagraphSpace(4);
            cl.add( li );
        }

        PdfPCell c;

        if( listTitle!=null && !listTitle.isBlank() )
        {
            c = new PdfPCell( new Phrase( listTitle, listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 4 );
            c.setPaddingLeft(4 + extraLeftPadding );
            c.setPaddingBottom(4);
            setRunDirection( c );
            tbl.addCell(c);
        }

        c = new PdfPCell();
        c.addElement( cl );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        c.setVerticalAlignment( Element.ALIGN_TOP );
        c.setBorderWidth( 0 );
        c.setPaddingLeft(4 + extraLeftPadding );
        c.setPaddingBottom( 4 );
        setRunDirection( c );
        tbl.addCell(c);
    }
    

    /*
     Map of  stubletter :
              obj[0] = Name
              obj[1] = value
    */
    public Map<String,Object[]> getScoreValMap()
    {
        Map<String,Object[]> out = new HashMap<>();
        String key;
        // String nameUppercase;
        for( int i=0;i<leaderStyleScoreVals.length; i++ )
        {
            //nameUppercase = DiscReportUtils.getCompetencyStubLetter(i).toUpperCase();
            //out.put( DiscReportUtils.getCompetencyStubLetter(i), new Object[]{ nameUppercase, Float.valueOf(discScoreVals[i])} );
            key = DiscReportUtils.getCompetencyStubLetter(i) + ".name";
            out.put(DiscReportUtils.getCompetencyStubLetter(i), new Object[]{ lmsg_spec(key), Float.valueOf(leaderStyleScoreVals[i])} );
        }
        return out;
    }


}
