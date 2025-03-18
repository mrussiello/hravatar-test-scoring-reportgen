/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.disc;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


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
public abstract class BaseDiscReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
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
    public DiscReportUtils discReportUtils;


    public float footerHeight = 50;
    public float footerBarHeight = 12;
    public int footerImageIndex = 0;



    /*
      index 0=D
            1=I
            2=S
            3=C
    */
    public float[] discScoreVals;

    /*
      data[0] = top trait index.
      data[1] = secondary trait index or -1 if there is none.
    */
    public int[] topTraitIndexes;

    public String stub;
    public String highNamePair;



    public synchronized void specialInit()
    {
        if( discReportUtils==null )
        {
            if( reportData.getReport().getStrParam6() !=null && !reportData.getReport().getStrParam6().isEmpty() )
                bundleToUse = reportData.getReport().getStrParam6();

            if( bundleToUse==null || bundleToUse.isEmpty() )
            {
                Locale loc = reportData.getLocale();
                // String stub = "";
                if( loc.getLanguage().equalsIgnoreCase( "en" ) )
                    bundleToUse = "discreport.properties";
                else
                    bundleToUse = "discreport_" + loc.getLanguage().toLowerCase() + ".properties";
            }

            discReportUtils = new DiscReportUtils( bundleToUse );
        }

        discScoreVals = DiscReportUtils.getDiscScoreVals( reportData.getTestEvent() );
        topTraitIndexes = DiscReportUtils.getTopTraitIndexes(discScoreVals);
        stub = DiscReportUtils.getCompetencyStub(topTraitIndexes);
        if( topTraitIndexes[1]>=0 )
            highNamePair = lmsg_spec( "disc.TopTraitNamePairXY", new String[] {lmsg_spec(DiscReportUtils.getCompetencyStubLetter(topTraitIndexes[0]) + ".name"), lmsg_spec( DiscReportUtils.getCompetencyStubLetter(topTraitIndexes[1]) + ".name")} );
        else
            highNamePair = lmsg_spec( DiscReportUtils.getCompetencyStubLetter(topTraitIndexes[0]) + ".name" );

        LogService.logIt( "BaseDiscReportUtils.specialInit() feedbackReportCoverImageUrl=" + feedbackReportCoverImageUrl +", topTraitIndexes[0]=" + topTraitIndexes[0] + ", topTraitIndexes[1]=" + topTraitIndexes[1] + ", stub=" + stub + ", highNamePair=" + highNamePair );

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
            LogService.logIt( e, "BaseDiscReport.specialInit() testEventId=" + (reportData!=null && reportData.te!=null ? reportData.te.getTestEventId() : "null") + ", reportId=" + (reportData!=null && reportData.r2Use!=null ? reportData.r2Use.getReportId() : "null") + ", feedbackReportCoverImageUrl=" + feedbackReportCoverImageUrl );
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

            // boolean includeNumScores = !hideOverallNumeric; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean includeColorGraph = true; // !hideOverallGraph; //  && sft.getSupportsBarGraphic(reportData.getReport()) && reportData.getReport().getIncludeColorScores()==1; // && reportData.getReport().getIncludeCompetencyColorScores()==1;
            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();
            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            String scrTxt = lmsg_spec( manager ? "disc.XIsHighInY" : "disc.YouAreHighInY", new String[]{ reportData.getUserName(),highNamePair}); //getTestEvent().getOverallTestEventScore().getScoreText();

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( 1 );

            setRunDirection( touter );
            touter.setTotalWidth( pageWidth ); // - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Candidate"), fontLargeWhite ) );
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
             colRelWids = new float[] { 1f, 1f };

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

            // Note - the date MAY be the last entry in this table if there is no score text.
            boolean lastEntry = !includeColorGraph &&
                                !hasThirdPartyId &&
                                (reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty()) &&
                                (scrTxt == null || scrTxt.isEmpty());

            // Next Row, test date
            if( includeDates )
            {
                c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFontLargeLight() ) );
                c.setColspan(2);
                c.setPadding( 1 );
                // c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                if( lastEntry )
                {
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setPaddingBottom(4);
                }
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            if( hasThirdPartyId )
            {
                lastEntry = !includeColorGraph &&
                            (reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty()) &&
                            (scrTxt == null || scrTxt.isEmpty());

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
                if( lastEntry )
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            if( reportData.getUser().getMobilePhone()!=null && !reportData.getUser().getMobilePhone().isEmpty() )
            {
                lastEntry = !includeColorGraph &&
                            (scrTxt == null || scrTxt.isEmpty());

                c = new PdfPCell( new Phrase( "(m) " + reportData.getUser().getMobilePhone(), getFontLargeLight() ) );
                c.setColspan(2);
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                if( lastEntry )
                {
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setPaddingBottom(4);
                }
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            if( scrTxt != null && !scrTxt.isEmpty())
            {
                lastEntry = !includeColorGraph;

                c = new PdfPCell( new Phrase( scrTxt, fontXLargeBoldDarkBlue ) );
                c.setColspan(2);
                c.setPadding( 5 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                // c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setPaddingLeft( 100 );

                if( lastEntry )
                {
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setPaddingBottom(4);
                }
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;

            y = previousYLevel - TPAD;

            if( includeColorGraph )
            {
                //      Map of  stubletter :
                //                 obj[0] = Name
                //                 obj[1] = value
                Map<String,Object[]> scoreValMap = getScoreValMap();

                float colWid = (pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN)/2f;

                touter = new PdfPTable( 1 );
                touter.setTotalWidth( colWid );
                touter.setLockedWidth( true );

                c = touter.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setBackgroundColor(BaseColor.WHITE);
                c.setPadding( 0 );
                c.setFixedHeight(160 );
                c.setCellEvent( new DiscPieGraphCellEvent( scoreValMap, scrDigits, ct2Colors, reportData.getLocale() ) );
                touter.addCell(c);

                float newTHeight = touter.calculateHeights();

                touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + 40, y, pdfWriter.getDirectContent() );

                PdfPTable legendTable = new PdfPTable(2);
                legendTable.setWidths( new float[] {1f, 10f} );
                legendTable.setTotalWidth( colWid );
                legendTable.setLockedWidth( true );

                c = legendTable.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );

                BaseFont bf = font.getBaseFont();
                Font fnt;
                String styleLetter;
                Object[] data;
                String nm;
                Float val;
                BaseColor bc;

                for( int i=0; i<4; i++ )
                {
                    styleLetter = DiscReportUtils.getCompetencyStubLetter(i);
                    data = scoreValMap.get( styleLetter );
                    nm = (String) data[0];
                    val = (Float) data[1];

                    fnt = new Font(bf, LFONTSZ);
                    bc = DiscReportUtils.sliceBaseColors[i];

                    c = new PdfPCell();
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    c.setCellEvent(new DiscLegendCellBackgroundCellEvent(bc) );
                    setRunDirection( c );
                    legendTable.addCell(c);

                    c = new PdfPCell( new Phrase( nm + " (" + I18nUtils.getFormattedNumber(reportData.getLocale(), val, 1) + ")", fnt ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                    c.setBorderWidth( 0 );
                    c.setPadding( 6 );
                    setRunDirection( c );
                    legendTable.addCell(c);
                }

                float legendTableHeight = legendTable.calculateHeights();

                legendTable.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + colWid + 40, y - (legendTableHeight<newTHeight ? (newTHeight-legendTableHeight)/2f : 0), pdfWriter.getDirectContent() );

                newTHeight = Math.max( newTHeight,legendTableHeight );

                currentYLevel = y - newTHeight;
                previousYLevel = currentYLevel;

            }

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addReportInfoHeader()" );
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
                LogService.logIt( "BaseDiscReportTemplate.addFooterBar() footerImageIndex=" + footerImageIndex + ", footerImageUri=" + footerImageUri + ", footerImage=" + (footerImage!=null) );
                if( footerImage !=null )
                {
                    footerImage.scalePercent(7);
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
                LogService.logIt( "BaseDiscReportTemplate.addFooterBar() footerImageIndex=" + footerImageIndex + ", footerImageUri=" + footerImageUri + ", footerImage=" + (footerImage!=null) );
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
             //   LogService.logIt( "BaseDiscReportTemplate.addFooterBar() footerImageIndex=" + footerImageIndex + ", footerImageUri=" + footerImageUri + ", footerImage=" + (footerImage!=null) );
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

        // LogService.logIt( "BaseDiscReportTemplate.init() title=" + rd.getReportName() );

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



    public void addDiscStylesExplained() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg_spec( "disc.StylesExplained" ), null );

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

            BaseFont bf = fontBold.getBaseFont();
            Font fnt;
            String styleLetter;

            for( int i=0; i<4; i++ )
            {
                styleLetter = DiscReportUtils.getCompetencyStubLetter(i);
                fnt = new Font(bf, LFONTSZ);
                fnt.setColor( DiscReportUtils.sliceBaseColors[i] );
                c = new PdfPCell( new Phrase( lmsg_spec(  styleLetter + ".namefull"), fnt ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                c.setPaddingBottom(12);
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( lmsg_spec(  styleLetter + ".characteristics"), font ) );
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
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscFilesExplained()" );
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
            LogService.logIt( e, "BaseCT2ReportTemplate.addTitleAndSubtitle()" );
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
            LogService.logIt( e, "BaseDiscReportTemplate.addTitleLargeRTLLarge()" );
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
            LogService.logIt( e, "BaseDiscReportTemplate.getTitleHeightLarge()" );
            throw new STException( e );
        }
    }





    public void addTopTraitSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String titleKey = topTraitIndexes[1]>=0 ? "disc.CharsOfPeopleXY" : "disc.CharsOfPeopleX";

            String sectionTitle = topTraitIndexes[1]>=0 ? lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name"), lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name")}) :
                                                   lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name")});

            BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            String ttrait = lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name").toUpperCase();
            Font fnt = new Font(bf, XXLFONTSZ);
            fnt.setColor( DiscReportUtils.sliceBaseColors[topTraitIndexes[0]] );
            Chunk chk = new Chunk( ttrait, fnt);
            Paragraph phr = new Paragraph( chk );

            if( topTraitIndexes[1]>=0 )
            {
                ttrait = " " + lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name").toUpperCase();
                Font fnt2 = new Font(bf, XXLFONTSZ);
                fnt2.setColor( DiscReportUtils.sliceBaseColors[topTraitIndexes[1]] );
                chk = new Chunk( ttrait, fnt2);
                phr.add( chk );
            }

            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( phr );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
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
            // t.setHeaderRows( 1 );

            // Create header
            //c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( Element.ALIGN_LEFT );
            //c.setBorderWidth( scoreBoxBorderWidth );
            //c.setPadding( 1 );
            //c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor( ct2Colors.hraBlue );
            //c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            //setRunDirection( c );
            // touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            List<String> itemList = new ArrayList<>();
            itemList.add(lmsg_spec( stub + ".characteristics") );
            itemList.add(lmsg_spec( stub + ".overview") );

            bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );
            Font listItemFont = fontLarge;
            String listTitle = lmsg_spec( "disc.Overview");

            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);


            listTitle = lmsg_spec( "disc.Strengths");
            itemList = lmsg_spec_list( stub + ".strengths");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            listTitle = lmsg_spec( "disc.WorkplaceStyle");
            itemList = lmsg_spec_list( stub + ".style");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            listTitle = lmsg_spec( "disc.Limitations");
            itemList = lmsg_spec_list( stub + ".limitations");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            listTitle = lmsg_spec( "disc.Motivations");
            itemList = lmsg_spec_list( stub + ".motivations");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            listTitle = lmsg_spec( "disc.Stressors");
            itemList = this.lmsg_spec_list( stub + ".stressors");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            listTitle = lmsg_spec( "disc.HandlingConflict");
            itemList = this.lmsg_spec_list( stub + ".handlingconflict");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);
            itemList.clear();

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addTopTraitSection()" );
            throw e;
        }
    }


    public void addLeadingTraitSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            // String titleKey = topTraitIndexes[1]>=0 ? "disc.LeadingPeopleXY" : "disc.LeadingPeopleX";

            Font blueFont;
            BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            blueFont = new Font(bf, XXLFONTSZ);
            blueFont.setColor( ct2Colors.hraBlue );
            Chunk chk = new Chunk( lmsg_spec( "disc.LeadingPeopleX.P1") + " ", blueFont);
            Paragraph par = new Paragraph( chk );

            String ttrait = lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name").toUpperCase();
            Font fnt = new Font(bf, XXLFONTSZ);
            fnt.setColor( DiscReportUtils.sliceBaseColors[topTraitIndexes[0]] );
            chk = new Chunk( ttrait, fnt);
            par.add( chk );

            if( 1==1 && topTraitIndexes[1]>=0 )
            {
                ttrait = " " + lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name").toUpperCase();
                Font fnt2 = new Font(bf, XXLFONTSZ);
                fnt2.setColor( DiscReportUtils.sliceBaseColors[topTraitIndexes[1]] );
                chk = new Chunk( ttrait, fnt2);
                par.add( chk );
            }

            chk = new Chunk( " " + lmsg_spec( "disc.LeadingPeopleX.P2"), blueFont);
            par.add( chk );

            PdfPTable t = new PdfPTable( 1 );
            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( par );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;

            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = pageHeight - headerHgt - TPAD;
            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            currentYLevel = y - ht - TPAD;
            y = currentYLevel;

            PdfPTable touter = new PdfPTable( 2 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths(new float[]{1,1});
            touter.setLockedWidth( true );


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Font listHeaderFont = fontLargeBold;
            bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );
            Font listItemFont = fontLarge;
            float touterPadding = 6;


            PdfPTable tinner = new PdfPTable( 1 );
            setRunDirection(tinner);
            String listTitle = lmsg_spec( "disc.General");
            List<String> itemList = lmsg_spec_list( stub + ".leading.general");
            addListItemGroupToTable(tinner, listTitle, itemList, listHeaderFont, listItemFont, 0);
            c = new PdfPCell( tinner );
            c.setPadding( touterPadding );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            setRunDirection(c);
            touter.addCell( c );

            tinner = new PdfPTable( 1 );
            setRunDirection(tinner);
            listTitle = lmsg_spec( "disc.ResolvingConflict");
            itemList = lmsg_spec_list( stub + ".leading.resolveconflict");
            addListItemGroupToTable(tinner, listTitle, itemList, listHeaderFont, listItemFont, 0);
            c = new PdfPCell( tinner );
            c.setPadding( touterPadding );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            setRunDirection(c);
            touter.addCell( c );

            tinner = new PdfPTable( 1 );
            setRunDirection(tinner);
            listTitle = lmsg_spec( "disc.RecognizingStress");
            itemList = lmsg_spec_list( stub + ".leading.recognizestress");
            addListItemGroupToTable(tinner, listTitle, itemList, listHeaderFont, listItemFont, 0);
            c = new PdfPCell( tinner );
            c.setPadding( touterPadding );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            setRunDirection(c);
            touter.addCell( c );


            tinner = new PdfPTable( 1 );
            setRunDirection(tinner);
            listTitle = lmsg_spec( "disc.Motivating");
            itemList = lmsg_spec_list( stub + ".leading.motivate");
            addListItemGroupToTable(tinner, listTitle, itemList, listHeaderFont, listItemFont, 0);
            c = new PdfPCell( tinner );
            c.setPadding( touterPadding );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            setRunDirection(c);
            touter.addCell( c );

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addLeadingTraitSection()" );
            throw e;
        }
    }


    public void addHowWorkWithTraitSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            Font blueFont;
            BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            blueFont = new Font(bf, XXLFONTSZ);
            blueFont.setColor( ct2Colors.hraBlue );
            Chunk chk = new Chunk( lmsg_spec( "disc.CollaboratingWith") + " ", blueFont);
            Paragraph par = new Paragraph( chk );

            String ttrait = lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name").toUpperCase();
            Font fnt = new Font(bf, XXLFONTSZ);
            fnt.setColor( DiscReportUtils.sliceBaseColors[topTraitIndexes[0]] );
            chk = new Chunk( ttrait, fnt);
            par.add( chk );

            if( 1==1 && topTraitIndexes[1]>=0 )
            {
                ttrait = " " + lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name").toUpperCase();
                Font fnt2 = new Font(bf, XXLFONTSZ);
                fnt2.setColor( DiscReportUtils.sliceBaseColors[topTraitIndexes[1]] );
                chk = new Chunk( ttrait, fnt2);
                par.add( chk );
            }

            PdfPTable t = new PdfPTable( 1 );
            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( par );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;

            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = currentYLevel; //  pageHeight - headerHgt - TPAD;
            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            currentYLevel = y - ht - TPAD;
            y = currentYLevel;

            //String titleKey = topTraitIndexes[1]>=0 ? "disc.HowWorkWithXY" : "disc.HowWorkWithX";

            //String sectionTitle = topTraitIndexes[1]>=0 ? lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name"), lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name")}) :
            //                                       lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name")});

            //PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );

            // t.setHeaderRows( 1 );

            // Create header
            //c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( Element.ALIGN_LEFT );
            //c.setBorderWidth( scoreBoxBorderWidth );
            //c.setPadding( 1 );
            //c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor( ct2Colors.hraBlue );
            //c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            //setRunDirection( c );
            //touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );


            bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );
            Font listItemFont = fontLarge;


            //Font listHeaderFont = fontBold;
            // Font listItemFont = font;
            String listTitle = lmsg_spec( "disc.OnATeam");
            List<String> itemList = lmsg_spec_list( stub + ".howwork.onteam");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec( "disc.WhenWorkingWithXStyles", new String[]{highNamePair});
            itemList = lmsg_spec_list( stub + ".howwork.with");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            float ulY = currentYLevel - 6*PAD;  // 4* PAD;
            float tableHeight = touter.calculateHeights(); //  + 500;
            if( tableHeight > (ulY - footerHgt - 3*PAD) )
            {
                this.addNewPage();
                previousYLevel = currentYLevel;
                y = previousYLevel;
            }

            // y -= TPAD;

            //float y = addTitle( previousYLevel, lmsg_spec("disc.Collaborating"), null );
            //y -= TPAD;
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addHowWorkWithTraitSection()" );
            throw e;
        }
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
            LogService.logIt( e, "BaseDiscReportTemplate.addBlueBar()" );
            throw e;
        }

    }

    public void addHowXShouldWorkWithYSection( int yTraitIndex ) throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String yTraitLetter = DiscReportUtils.getCompetencyStubLetter(yTraitIndex );
            String yTraitName = lmsg_spec(yTraitLetter+".name");
            String yTraitNameUpper = yTraitName.toUpperCase();

            String xTraitLetter = DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0] );

            BaseColor bgColor = DiscReportUtils.sliceBaseColors[yTraitIndex];

            String titleKey = "disc.HowWorkWithX";

            String sectionTitle = lmsg_spec( titleKey, new String[]{yTraitName});

            Font titleFont = fontXLargeBold;
            BaseFont bf = titleFont.getBaseFont();
            titleFont = new Font(bf, XLFONTSZ);
            titleFont.setColor( ct2Colors.hraBlue );


            float y = addTitleLarge( previousYLevel, lmsg_spec("disc.CollaboratingWithHighY", new String[]{yTraitName} ), titleFont );

            y -= TPAD;

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            // touter.setWidths(new float[] {0.5f, 10f} );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            // touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // Create header
            c = new PdfPCell();
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            // touter.addCell(c);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            // touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font listHeaderFont = fontLargeBold;
            Font listItemFont = fontLarge;
            bf = listHeaderFont.getBaseFont();

            listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );

            String listTitle = lmsg_spec( "disc.OnATeam");
            List<String> itemList = lmsg_spec_list(yTraitLetter + ".howwork.onteam");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec( "disc.WhenWorkingWithXStyles", new String[]{yTraitName});
            itemList = lmsg_spec_list(yTraitLetter + ".howwork.with");
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            if( xTraitLetter.equalsIgnoreCase(yTraitLetter ) )
                listTitle = lmsg_spec( "disc.HowXWorksWithX", new String[]{xTraitLetter.toUpperCase()} );
            else
                listTitle = lmsg_spec("disc.HowXWorksWithY", new String[]{xTraitLetter.toUpperCase(), yTraitLetter.toUpperCase()} );


            c = new PdfPCell( new Phrase( listTitle, listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            String howWorkTogetherStr = lmsg_spec(xTraitLetter + ".howworkwith." + yTraitLetter + ".1");
            c = new PdfPCell( new Phrase( howWorkTogetherStr, listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            String imgUri = sideTabIconUris[yTraitIndex];
            URL imgURL = reportData.getLocalImageUrl( imgUri );
            Image iconImage = ITextUtils.getITextImage( imgURL );
            iconImage.scalePercent(30);
            touter.setTableEvent( new DiscCollaboratingTabTableEvent(bgColor,fontXLargeBoldWhite.getBaseFont(),yTraitNameUpper, iconImage) );

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            currentYLevel -= TPAD;

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addHowXShouldWorkWithYSection()" );
            throw e;
        }
    }



    public void addDiscEducationSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String titleKey = "disc.WhatIsDISC";

            String sectionTitle = lmsg_spec( titleKey);

            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            Font fnt = new Font(this.baseFontCalibriBold, XXLFONTSZ);
            fnt.setColor( ct2Colors.hraBlue );

            PdfPCell c = new PdfPCell( new Phrase(sectionTitle, fnt) );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;

            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = pageHeight - headerHgt - TPAD;

            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );

            currentYLevel = y - ht - TPAD;
            y = currentYLevel;


            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // Create header
            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            // touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font listHeaderFont = fontLargeBold;
            Font listItemFont = fontLarge;

            c = new PdfPCell( new Phrase( lmsg_spec("disc.what.p1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);


            List<String> itemList = new ArrayList<>();
            itemList.add( lmsg_spec("d.name" ) + " (D)");
            itemList.add( lmsg_spec("i.name" ) + " (I)");
            itemList.add( lmsg_spec("s.name" ) + " (S)");
            itemList.add( lmsg_spec("c.name" ) + " (C)");
            addListItemGroupToTable(touter, null, itemList, listHeaderFont, listItemFont, 0);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.what.p2"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingBottom(0);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.what.p3"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingBottom(0);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.History.title"), listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.History.1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.History.2"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);


            c = new PdfPCell( new Phrase( lmsg_spec("disc.HowUsed.title"), listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.HowUsed.1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            itemList = new ArrayList<>();
            itemList.add( lmsg_spec("disc.HowUsed.1.a" ));
            itemList.add( lmsg_spec("disc.HowUsed.1.b" ));
            itemList.add( lmsg_spec("disc.HowUsed.1.c" ));
            itemList.add( lmsg_spec("disc.HowUsed.1.d" ));
            addListItemGroupToTable(touter, null, itemList, listHeaderFont, listItemFont, 0);


            c = new PdfPCell( new Phrase( lmsg_spec("disc.ScoringInfo.title"), listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.ScoringInfo.1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            float ulY = currentYLevel - 6*PAD;  // 4* PAD;
            float tableHeight = touter.calculateHeights(); //  + 500;
            if( tableHeight > (ulY - footerHgt - 3*PAD) )
            {
                this.addNewPage();
                previousYLevel = currentYLevel;
            }

            //float y = addTitle( previousYLevel, lmsg_spec("disc.LearnMore"), null );
            //y -= TPAD;
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscEducationSection()" );
            throw e;
        }
    }

    public void addKeyActionsToTakeSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            Font blueFont = new Font(bf, XXLFONTSZ);
            blueFont.setColor( ct2Colors.hraBlue );

            PdfPTable t = new PdfPTable( 1 );
            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( new Phrase(lmsg_spec( "disc.keyActionsAfterDisc").toUpperCase() + " ", blueFont) );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;
            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = currentYLevel; //  pageHeight - headerHgt - TPAD;
            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            currentYLevel = y - ht - TPAD;
            y = currentYLevel;

            bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );
            Font listItemFont = fontLarge;


            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );

            String listTitle = lmsg_spec("mgr.actions.1.title");
            List<String> itemList = new ArrayList<>();
            for( int i=1;i<=1; i++ )
            {
                itemList.add( lmsg_spec("mgr.actions.1." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec("mgr.actions.2.title");
            itemList = new ArrayList<>();
            for( int i=1;i<=3; i++ )
            {
                itemList.add( lmsg_spec("mgr.actions.2." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec("mgr.actions.3.title");
            itemList = new ArrayList<>();
            for( int i=1;i<=1; i++ )
            {
                itemList.add( lmsg_spec("mgr.actions.3." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec("mgr.actions.4.title");
            itemList = new ArrayList<>();
            for( int i=1;i<=1; i++ )
            {
                itemList.add( lmsg_spec("mgr.actions.4." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec("mgr.actions.5.title");
            itemList = new ArrayList<>();
            for( int i=1;i<=1; i++ )
            {
                itemList.add( lmsg_spec("mgr.actions.5." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addKeyActionsToTakeSection()" );
            throw e;
        }

    }

    public void addDiscManagerInfoSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;


            float y = addTitle( previousYLevel, lmsg_spec("disc.InfoForManagers"), null );

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
                PdfAction pdfa = PdfAction.gotoRemotePage( url , lmsg_spec("disc.ClickToVisit"), false, true );
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
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscEducationSection()" );
            throw e;
        }
    }

    
    public void addHowBuildTeamsWithDiscSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            Font blueFont = new Font(bf, XXLFONTSZ);
            blueFont.setColor( ct2Colors.hraBlue );

            PdfPTable t = new PdfPTable( 1 );
            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( new Phrase(lmsg_spec( "mgr.howbldtm.title").toUpperCase() + " ", blueFont) );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;
            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = currentYLevel; //  pageHeight - headerHgt - TPAD;
            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            currentYLevel = y - ht - TPAD;
            y = currentYLevel;

            bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );
            Font listItemFont = fontLarge;


            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );


            c = new PdfPCell( new Phrase( lmsg_spec("mgr.howbldtm.p1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.howbldtm.p2"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addHowBuildTeamsWithDiscSection()" );
            throw e;
        }
        
    }


    public void addAvoidSterotypingSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            Font blueFont = new Font(bf, XXLFONTSZ);
            blueFont.setColor( ct2Colors.hraBlue );

            PdfPTable t = new PdfPTable( 1 );
            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( new Phrase(lmsg_spec( "mgr.avoids.title").toUpperCase() + " ", blueFont) );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;
            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = currentYLevel; //  pageHeight - headerHgt - TPAD;
            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            currentYLevel = y - ht - TPAD;
            y = currentYLevel;

            bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );
            Font listItemFont = fontLarge;


            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );


            c = new PdfPCell( new Phrase( lmsg_spec("mgr.avoids.p1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addAvoidSterotypingSection()" );
            throw e;
        }        
    }


    
    
    
    public void addDiscBuildYourTeamSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            BaseFont bf = getFontXLargeBoldDarkBlue().getBaseFont();
            Font blueFont = new Font(bf, XXLFONTSZ);
            blueFont.setColor( ct2Colors.hraBlue );

            PdfPTable t = new PdfPTable( 1 );
            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( new Phrase(lmsg_spec( "mgr.tmbld.title").toUpperCase() + " ", blueFont) );
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            float ht = t.calculateHeights(); //  + 500;
            float tw = t.getTotalWidth();
            float tableX = (pageWidth - tw )/2;
            float y = currentYLevel; //  pageHeight - headerHgt - TPAD;
            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            currentYLevel = y - ht - TPAD;
            y = currentYLevel;

            bf = fontLargeBold.getBaseFont();
            Font listHeaderFont = new Font(bf, LFONTSZ);
            listHeaderFont.setColor( ct2Colors.hraBlue );
            Font listItemFont = fontLarge;


            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );


            c = new PdfPCell( new Phrase( lmsg_spec("mgr.tmbld.p1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);


            String listTitle = lmsg_spec("mgr.tmbld.1.title");
            List<String> itemList = new ArrayList<>();
            for( int i=1;i<=1; i++ )
            {
                itemList.add( lmsg_spec("mgr.tmbld.1." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec("mgr.tmbld.2.title");
            itemList = new ArrayList<>();
            for( int i=1;i<=1; i++ )
            {
                itemList.add( lmsg_spec("mgr.tmbld.2." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec("mgr.tmbld.2.2");
            itemList = new ArrayList<>();
            for( int i=1;i<=5; i++ )
            {
                itemList.add( lmsg_spec("mgr.tmbld.2.2." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listItemFont, listItemFont, 20);

            listTitle = lmsg_spec("mgr.tmbld.3.title");
            itemList = new ArrayList<>();
            for( int i=1;i<=1; i++ )
            {
                itemList.add( lmsg_spec("mgr.tmbld.3." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listHeaderFont, listItemFont, 0);

            listTitle = lmsg_spec("mgr.tmbld.3.2");
            itemList = new ArrayList<>();
            for( int i=1;i<=8; i++ )
            {
                itemList.add( lmsg_spec("mgr.tmbld.3.2." + i ) );
            }
            addListItemGroupToTable(touter, listTitle, itemList, listItemFont, listItemFont, 20);

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscBuildYourTeamSection()" );
            throw e;
        }
    }

    

    public String lmsg_spec( String key )
    {
        return discReportUtils.getKey(key );
    }

    public String lmsg_spec( String key, String[] prms )
    {
        String msgText = discReportUtils.getKey(key );
        return MessageFactory.substituteParams( reportData.getLocale() , msgText, prms );
    }

    public List<String> lmsg_spec_list( String key )
    {
        List<String> out = new ArrayList<>();
        String val;
        for( int i=1;i<100;i++ )
        {
            val =  discReportUtils.getKey( key + "." + i );

            if( val==null || val.isBlank() || val.startsWith( "KEY NOT FOUND") )
                break;

            out.add(val);
        }

        return out;
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
        for( int i=0;i<discScoreVals.length; i++ )
        {
            //nameUppercase = DiscReportUtils.getCompetencyStubLetter(i).toUpperCase();
            //out.put( DiscReportUtils.getCompetencyStubLetter(i), new Object[]{ nameUppercase, Float.valueOf(discScoreVals[i])} );
            key = DiscReportUtils.getCompetencyStubLetter(i) + ".name";
            out.put( DiscReportUtils.getCompetencyStubLetter(i), new Object[]{ lmsg_spec(key), Float.valueOf(discScoreVals[i])} );
        }
        return out;
    }


}
