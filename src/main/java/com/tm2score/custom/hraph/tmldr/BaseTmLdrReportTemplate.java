/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.tmldr;

import com.tm2score.custom.coretest2.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest.ITextUtils;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.MAX_CUSTLOGO_H_V2;
import static com.tm2score.custom.coretest2.CT2ReportSettings.MAX_CUSTLOGO_W_V2;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TESNameComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.format.TableBackground;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.proctor.ProctorHelpUtils;
import com.tm2score.proctor.ProctorUtils;
import com.tm2score.profile.ProfileFacade;
import com.tm2score.profile.ProfileStrParam1Comparator;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.profile.alt.AltScoreCalculatorFactory;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.CategoryDistType;
import com.tm2score.sim.EducType;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.OverallScaledScoreCalcType;
import com.tm2score.sim.RelatedExperType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.sim.TrainingType;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;


/**
 *
 * @author Mike
 */
public abstract class BaseTmLdrReportTemplate extends TmLdrReportSettings implements ReportTemplate
{
    static String[] persCompetencyNames = new String[] {"Develops Relationships", "Expressive and Outgoing", "Enjoys Problem-Solving", "Innovative and Creative", "Adaptable", "Needs Structure", "Seeks Perfection" , "Emotional Self-Control", "Emotional Self-Awareness", "Empathy" };

    static String[] peopleMgmtNames = new String[] { "Frontline Management Fundamentals" };

    static String[] commitmentNames = new String[] {"Engagement", "Exhibits a Positive Work Attitude", "Corporate Citizenship" };

    static String[] motivationNames = new String[] {"Leadership Aspiration", "Competitive" };

    static String[] abcs = new String[] { " " , "A", "B", "C", "D", "E", "F", "G", "H" };

    static String[] detailCompTitleKeys = new String[] {"", "g.TmLdrAbilitiesTitle", "g.TmLdrPersonalCompsTitle", "g.TmLdrPeopleManagementTitle", "g.TmLdrCommitmentTitle", "g.TmLdrMotivationTitle"};
    static String[] detailCompSubtitleKeys = new String[] {"", "g.TmLdrAbilitiesSubtitle", "g.TmLdrPersonalCompsSubtitle", "g.TmLdrPeopleManagementSubtitle", "g.TmLdrCommitmentSubtitle", "g.TmLdrMotivationSubtitle"};

    static Map<String,String> oldNewCompetencyMap;

    Image custLogo = null;

    ReportData reportData = null;
    CT2ReportData ctReportData = null;

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

    ReportUtils reportUtils;
    ProctorUtils proctorUtils;

    float PAD = 5;
    float TPAD = 8;

    // float bxX;
    // float bxWid;
    //float barGrphWid;
    //float barGrphX;
    float lineW = 0.8f;


    float currentYLevel = 0;
    float previousYLevel = 0;

    java.util.List<String> prepNotes;


    @Override
    public abstract byte[] generateReport() throws Exception;


    public synchronized void initFonts() throws Exception
    {
        initSettings( reportData );
        initExtra( reportData );

        String logoUrl = reportData.getReportCompanyImageUrl();

        if( logoUrl == null && reportData.s!=null && reportData.s.getReportLogoUrl()!=null && !reportData.s.getReportLogoUrl().isBlank() )
            logoUrl = reportData.s.getReportLogoUrl() ;

        if( logoUrl == null && reportData.o.getReportLogoUrl()!=null && !reportData.o.getReportLogoUrl().isBlank() )
            logoUrl = reportData.o.getReportLogoUrl() ;

        if( logoUrl != null && StringUtils.isCurlyBracketed( logoUrl ) )
            logoUrl = RuntimeConstants.getStringValue( "translogoimageurl" );

        try
        {
            custLogo = logoUrl == null || logoUrl.isEmpty() ? null : ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( logoUrl ) );
        }

        catch( Exception e )
        {
            LogService.logIt( "BaseTmLdrReportTemplate.initFonts() getting custLogo: " + logoUrl );
        }


        // !reportData.hasCustLogo() ? null : ITextUtils.getITextImage( reportData.getCustLogoUrl() );

        if( custLogo != null )
        {
            float imgSclW = 100;
            float imgSclH = 100;
            // float maxImgWid = 80;
            // float maxImgHgt = 40;

            if( custLogo.getWidth() > MAX_CUSTLOGO_W )
                imgSclW = 100 * MAX_CUSTLOGO_W/custLogo.getWidth();

            if( custLogo.getHeight() > MAX_CUSTLOGO_H )
                imgSclH = 100 * MAX_CUSTLOGO_H/custLogo.getHeight();

            imgSclW = Math.min( imgSclW, imgSclH );

            if( imgSclW < 100 )
                custLogo.scalePercent( imgSclW );

        }

        title = StringUtils.replaceStr( reportData.getReportName(), "[SIMNAME]", reportData.getSimName() );

        String reportCompanyName = reportData.getReportCompanyName();

        if( StringUtils.isCurlyBracketed( reportCompanyName ) )
            reportCompanyName = "                      ";

        if( reportCompanyName == null || reportCompanyName.isEmpty() )
            reportCompanyName = reportData.getOrgName();

        title = StringUtils.replaceStr( title, "[ORGNAME]", reportCompanyName );

        if( reportData.hasUserInfo() )
            title = StringUtils.replaceStr( title, "[USERNAME]", reportData.getUserName() );

        else
            title = StringUtils.replaceStr( title, "[USERNAME]", "" );

        // LogService.logIt( "BaseTmLdrReportTemplate.initFonts() title=" + title );
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

    
    
    
    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CT2ReportData();

        initFonts();
        
        initColors();      
        
        if( 1==1 )
        {
            if( ct2Colors!=null )
                ct2Colors.clearBorders();
            
            scoreBoxBorderWidth = 0;
            lightBoxBorderWidth=0;            
        }
                

        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        CT2HeaderFooter hdr = new CT2HeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this, custLogo );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );

        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );

        headerHgt = hghts[0];
        footerHgt = hghts[1];

        usablePageHeight = pageHeight - headerHgt - footerHgt - 4*PAD;

        // bxX = 5*PAD;
        // bxWid = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN;
        // barGrphWid = 0.77f*bxWid;
        // barGrphX = bxX + ( bxWid - barGrphWid - 2*PAD ); //    bxX + 7*PAD;

        // dataTableWidth = hdr.headerW;

       // document.setMargins( document.leftMargin(), document.rightMargin(), 42, 42 );

        // LogService.logIt( "BaseTmLdrReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

        tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );
    }

    @Override
    public void dispose() throws Exception
    {
        if( baos != null )
            baos.close();
    }

    private synchronized void initNameMap()
    {
        if( oldNewCompetencyMap == null )
        {
            this.oldNewCompetencyMap = new HashMap<>();

            oldNewCompetencyMap.put( "Analytical Thinking", "Analytical Thinking" );
            oldNewCompetencyMap.put( "Attention to Detail", "Attention to Detail" );
            oldNewCompetencyMap.put( "Writing", "Writing" );
            oldNewCompetencyMap.put( "Develops Relationships", "Sociability" );
            oldNewCompetencyMap.put( "Expressive and Outgoing", "Extraversion" );
            oldNewCompetencyMap.put( "Enjoys Problem-Solving", "Problem-Solving" );
            oldNewCompetencyMap.put( "Innovative and Creative", "Innovative" );
            oldNewCompetencyMap.put( "Adaptable", "Adaptability" );
            oldNewCompetencyMap.put( "Needs Structure", "Process Orientation" );
            oldNewCompetencyMap.put( "Seeks Perfection", "Quality Orientation" );
            oldNewCompetencyMap.put( "Frontline Management Fundamentals", "First-Line Supervision" );
            oldNewCompetencyMap.put( "Engagement", "Engagement" );
            oldNewCompetencyMap.put( "Exhibits a Positive Work Attitude", "Positive Work Attitude" );
            oldNewCompetencyMap.put( "Corporate Citizenship", "Corporate Citizenship" );
            oldNewCompetencyMap.put( "Leadership Aspiration", "Leadership Aspiration" );
            oldNewCompetencyMap.put( "Competitive", "Competitiveness" );
        }
    }

    public String getNewCompetencyName( String oldName )
    {
        initNameMap();

        String n = oldNewCompetencyMap.get( oldName );
        return n != null && !n.isEmpty() ? n : oldName;
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

    @Override
    public String getReportGenerationNotesToSave()
    {
        return null;
    }


    public void addReportInfoHeader() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.Overall" ), null );

            y -= TPAD;

            String scr = I18nUtils.getFormattedNumber( reportData.getLocale(), reportData.getTestEvent().getOverallScore(), reportData.getTestEvent().getScorePrecisionDigits() );

            TestEventScore tes = reportData.getTestEvent().getOverallTestEventScore();

            if( reportData.hasProfile() )
                 tes.setProfileBoundaries( reportData.getOverallProfileData() );

            java.util.List<TextAndTitle> ct3RiskFactors = ScoreFormatUtils.getTextTitleList(reportData.getTestEvent(), CT3Constants.CT3RISKFACTORS );

            ct3RiskFactors.addAll( ScoreFormatUtils.getTextTitleList(reportData.getTestEvent(), Constants.STD_RISKFACTORSKEY ) );

            // LogService.logIt( "Found " + ct3RiskFactors.size() + " CT3 Risk Factors." );

            ScoreCategoryType sct = tes.getScoreCategoryType();

            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .4f, .1f, .25f, .25f} : new float[] { .25f, .25f, .1f, .4f};

            boolean includeNumScores = true; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean includeStars = reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean includeColorGraph = reportData.getReport().getIncludeCompetencyColorScores()==1;
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" ); 

            if( !includeStars && !includeColorGraph )
            {
                // includeStars = false;
                cols -= 2;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f } : new float[] { .2f, .4f};
            }

            else if( !includeColorGraph )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f, .35f} : new float[] { .35f, .2f, .45f};
            }

            else if( !includeStars )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f, .35f} : new float[] { .35f, .2f, .45f};
            }

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Candidate"), fontLargeWhite ) );
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            touter.addCell(c);

            if( includeNumScores )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
                c.setColspan( 1 );

                if( !includeStars )
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
                //    c.setBorder( Rectangle.TOP );
                //else
                // c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                if( includeStars )
                    c.setBackgroundColor(  ct2Colors.hraBlue );
                touter.addCell(c);

                if( includeColorGraph )
                {
                    c = new PdfPCell( new Phrase( lmsg( "g.Interpretation"), fontLargeWhite ) );
                    c.setColspan( 1 );

                    if( !includeStars )
                        c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
                    //    c.setBorder( Rectangle.TOP );
                    //else
                    //    c.setBorder( Rectangle.TOP | Rectangle.RIGHT );

                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( 25 );
                    if( includeStars )
                        c.setBackgroundColor(  ct2Colors.hraBlue );
                    touter.addCell(c);
                }
            }

            if( includeStars )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.JobMatch"), this.fontLargeWhite ) );
                // c.setBorder( Rectangle.TOP | Rectangle.RIGHT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setBackgroundColor(  ct2Colors.hraBlue );
                c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
                touter.addCell(c);
            }

            // header row is finished.

            // t.setWidthPercentage( 0.8f );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            //c = new PdfPCell( new Phrase( reportData.getSimName(), this.getFontLargeLightBold() ) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment(Element.ALIGN_LEFT );
            //c.setVerticalAlignment( Element.ALIGN_BOTTOM );
            //c.setPadding( 2 );
            //t.addCell( c );

            // t.addCell( new Phrase( reportData.getSimName(), getFontLargeLightBold() ) );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setColspan(cols);
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            touter.addCell( c );


            int colspan = 1;

            if( reportData.getReport().getIncludeOverallScore()!=1 )
                colspan++;

            // NAME
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            c.setColspan(colspan);
            c.setPadding( 1 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setVerticalAlignment( Element.ALIGN_BOTTOM );
            c.setBorder( Rectangle.LEFT );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            touter.addCell( c );

            // Score (if)
            c = new PdfPCell( new Phrase( reportData.getReport().getIncludeOverallScore()==1 ? scr : "" , fontLargeBold ) );
            c.setPadding( 2 );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_BOTTOM );
            c.setBorder( includeStars ||includeColorGraph ? Rectangle.NO_BORDER : Rectangle.RIGHT );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            touter.addCell( c );

            if( includeColorGraph )
            {
                PdfPTable t2 = new PdfPTable( 1 );

                t2.setWidthPercentage( 100 );
                // t2.setLockedWidth( true );

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setPadding( 0 );
                c.setFixedHeight(16);
                c.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, false, ct2Colors, devel, false, false, true, 0 ) );
                t2.addCell(c);


                c = new PdfPCell( t2 ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( includeStars ? Rectangle.NO_BORDER : Rectangle.RIGHT );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                c.setPaddingTop( 6 );




                // c.setCellEvent( new CT2OverallScoreGraphicCellEvent( tes , sct, baseFontCalibri ) );
                touter.addCell( c );
            }

            if( includeStars )
            {
                Image scrCatImg = getScoreCategoryImg( sct, false );

                c = scrCatImg==null ? new PdfPCell( new Phrase("",font) ) : new PdfPCell( scrCatImg ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.RIGHT );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                c.setPadding( 0 );
                c.setPaddingTop( 9 );

                touter.addCell( c );
            }

            if( reportData.getUser()==null )
                reportData.u = new User();

            // Next Row - Email
            if( reportData.getUser().getUserType().getNamed() &&
                reportData.getUser().getEmail() != null &&
                !reportData.getUser().getEmail().isEmpty() &&
                !StringUtils.isCurlyBracketed( reportData.getUser().getEmail() ) )
            {
                c = new PdfPCell( new Phrase( reportData.getUser().getEmail(), getFontLight() ) );
                c.setColspan( cols );
                c.setPadding( 1 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                touter.addCell( c );
            }

            // Next row - test name
            c = new PdfPCell( new Phrase( reportData.getSimName(), getFontLight() ) );
            c.setColspan( cols );
            c.setPadding( 1 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            touter.addCell( c );

            // Next Row, test date
            if( includeDates )
            {
                c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFontLight() ) );
                c.setColspan( cols );
                c.setPadding( 1 );
                // c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                touter.addCell( c );
            }

            if( reportData.getUser().getMobilePhone()!=null && !reportData.getUser().getMobilePhone().isEmpty() )
            {
                c = new PdfPCell( new Phrase( "(m) " + reportData.getUser().getMobilePhone(), getFontLight() ) );
                c.setColspan( cols );
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                touter.addCell( c );
            }

            /*
            if( reportData.getUser().getOfficePhone()!=null && !reportData.getUser().getOfficePhone().isEmpty() )
            {
                c = new PdfPCell( new Phrase( reportData.getUser().getOfficePhone(), getFontLargeLight() ) );
                c.setColspan( cols );
                c.setPadding( 1 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setPaddingBottom( 6 );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                touter.addCell( c );
            }
            */


            // Next row - Text
            String scrTxt = reportData.getOverallScoreText(); // getTestEvent().getOverallTestEventScore().getScoreText();

            if( reportData.getReport().getIncludeScoreText() != 1  )
                scrTxt = null;

            boolean showRiskFactors = ct3RiskFactors.size()>0 && reportData.getR2Use().getIntParam1()==0;

            if( scrTxt != null && !scrTxt.isEmpty() || showRiskFactors )
            {
                c = new PdfPCell();
                c.setPadding( 1 );
                setRunDirection(c);

                if( scrTxt != null && !scrTxt.isEmpty() )
                {
                    Paragraph pp = new Paragraph( scrTxt, getFontLight() );

                    pp.setSpacingBefore( 1 );
                    pp.setLeading( showRiskFactors ? 9 : 10 );

                    c.addElement( pp );
                }

                if( showRiskFactors )
                {
                    String rfHdrKey = "g.CT3RiskFactorsHdr";
                    String rfFtrKey = "g.CT3RiskFactorsFtr";

                    float spcg = 8;

                    Chunk chnk = new Chunk( lmsg( "g.CT3RiskFactorsHdrTitle" ) + " " , this.getFontBoldRed() );

                    Chunk chnk2 = new Chunk(  lmsg( rfHdrKey ) , getFontLight() );

                    Phrase ph = new Phrase();

                    ph.add( chnk );
                    // ph.add( chnk2 );

                    Paragraph cHdr = new Paragraph( ph );

                    // Paragraph cHdr = new Paragraph( lmsg( rfHdrKey ) , getFontSmall() );
                    cHdr.setSpacingBefore( spcg );
                    cHdr.setSpacingAfter( 5 );
                    cHdr.setLeading( 10 );
                    Paragraph cFtr=new Paragraph( lmsg( rfFtrKey ) , getFontLight() );
                    cFtr.setSpacingBefore( 4 );
                    cFtr.setLeading( 10 );

                    com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                    cl.setListSymbol( "\u2022");
                    cl.setIndentationLeft( 10 );
                    cl.setSymbolIndent( 10 );


                    String rftxt;

                    for( TextAndTitle tt : ct3RiskFactors )
                    {
                        LogService.logIt( "BaseTmLdrReportTemplate.getReportInfoHeader() Text=" + tt.getText() + ", title=" + tt.getTitle()  + " flags=" + tt.getFlags() );

                        if( tt.getText()==null || tt.getText().isEmpty() )
                            continue;

                        rftxt = tt.getText();

                        if( rftxt.indexOf( "[FACET]" ) >= 0 )
                            rftxt = rftxt.substring(0,rftxt.indexOf( "[FACET]" ) );

                        cl.add( new ListItem( 9,  rftxt, getFontLight() ) );
                        // cl.add( new ListItem( new Phrase( tt.getText(), getFontLight() ) ) );
                    }

                    c.addElement( cHdr );
                    c.addElement( cl );
                    c.addElement( cFtr );
                }

                colspan = cols;

                if( includeColorGraph )
                {
                    colspan--;

                    if( includeStars )
                        colspan--;
                }

                c.setColspan( colspan );

                c.setBorder( includeColorGraph ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );

                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 0 );
                c.setPaddingTop( 6 );
                c.setPaddingRight( 4 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingBottom( 10 );
                touter.addCell( c );

                if( includeColorGraph )
                {
                    c = new PdfPCell( getScoreKeyTable() );
                    c.setColspan( includeStars ? 2 : 1 );
                    c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                        c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT  );

                    c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 0 );
                    c.setPaddingTop( 6 );
                    c.setPaddingBottom( 10 );
                    touter.addCell( c );
                }
            }

            else if( includeColorGraph )
            {
                c = new PdfPCell( getScoreKeyTable() );
                c.setColspan( cols );
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 0 );
                c.setPaddingTop( 6 );
                c.setPaddingBottom( 10 );
                touter.addCell( c );
            }

            // float y = pageHeight - headerHgt - 4*PAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addReportInfoHeader()" );

            throw e;
        }
    }


    public PdfPTable getScoreKeyTable()
    {
            // First, add a table
            PdfPTable t = new PdfPTable( new float[]{0.2f,0.8f} );

            // float importanceWidth = 25;

            t.setTotalWidth( 150 );
            t.setLockedWidth( true );
            // t.setHeaderRows( 1 );


            PdfPCell c = t.getDefaultCell();
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor(  ct2Colors.keyBackgroundColor);

            // First Row
            c = new PdfPCell( new Phrase( lmsg( "g.Key"), fontLightBold ) );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( 3 );
            c.setColspan( 2 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor(  ct2Colors.keyBackgroundColor);
            t.addCell( c );

            // Pointer
            // First Row
            c = new PdfPCell( reportPointer );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.CandidateScore"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor(  ct2Colors.keyBackgroundColor);
            t.addCell(c);

            c = new PdfPCell( keyRedBar );
            c.setPaddingLeft( 4 );
            c.setPaddingTop( 2 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.HigherRisk"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor(  ct2Colors.keyBackgroundColor);
            t.addCell(c);

            c = new PdfPCell( keyGreenBar );
            c.setPaddingLeft( 4 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.LowerRisk"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor(  ct2Colors.keyBackgroundColor);
            t.addCell(c);

            c = new PdfPCell( keyBlueBar );
            c.setPaddingLeft( 4 );
            c.setPaddingTop( 4 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            c.setPaddingBottom( 4 );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.CustomProfile"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingBottom( 4 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor(  ct2Colors.keyBackgroundColor);
            t.addCell(c);

            c = new PdfPCell( new Phrase( "", fontSmallLight ) );
            c.setPadding( 0 );
            c.setColspan(2);
            c.setPaddingBottom( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( BaseColor.WHITE );
            t.addCell(c);

            return t;
    }





    public void addReportSummaryChart() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseTmLdrReportTemplate.addReportSummaryChart() Using locale: " + reportData.getLocale().toString() );

            // If no info to present.
            //if( reportData.getReport().getIncludeCompetencyScores()!=1 ||
            //    ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
            //      reportData.getReport().getIncludeSubcategoryNumeric()!=1 )  )
            //      return;

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.Overall" ), null );

            y -= TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            int cols = 3;
            float[] colRelWids = reportData.getIsLTR()? new float[] { .35f, .1f, .55f} : new float[] { .55f, .1f, .35f};

            //boolean includeNumScores = true; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            //boolean includeStars = reportData.getReport().getIncludeSubcategoryCategory()==1;
            //boolean includeColorGraph = reportData.getReport().getIncludeCompetencyColorScores()==1;
            
// int totalComps = 0;

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Candidate"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor(  ct2Colors.hraBlue );
            t.addCell(c);

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Interpretation"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false ));
            t.addCell(c);

            // int totalRows = 1;

            TestEventScore teslOver = this.reportData.te.getOverallTestEventScore();

            // totalComps = 0;

            addTmLdrOverallSummaryChartSection( t, teslOver );

            Paragraph par = new Paragraph();

            Chunk ck = new Chunk( lmsg( "g.TmLdrAssessOverScore_1") + " ", font );
            par.add( ck );

            String val;

            boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(reportData.getOrg(), reportData.getTestEvent()) && reportData.getTestEvent().getOverallTestEventScore()!=null; // && reportData.getTestEvent().getOverallTestEventScore().getRawScore()>=0;
           
            float scrValue = useRawOverallScore ? reportData.getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow() : reportData.getTestEvent().getOverallScore();        
            
            // val = TmLdrTestEventScorer.getScoreTextForOverallScore(reportData.getTestEvent());
            
            if( scrValue>=70 )
                val = lmsg( "g.TmLdrAssessOverScoreHigh");

            else if( scrValue< 70 && scrValue>=30 )
                val = lmsg( "g.TmLdrAssessOverScoreMedium");

            else
                val = lmsg( "g.TmLdrAssessOverScoreLow");

            ck = new Chunk( val, this.fontBold );
            par.add( ck );

            ck = new Chunk( " " + lmsg( "g.TmLdrAssessOverScore_2"), font );
            par.add( ck );

            c = new PdfPCell( par );
            // c.setBorder( Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setColspan( 3 );
            c.setPadding( 3 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            t.addCell(c);

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - t.calculateHeights() - 2*TPAD;
            y = currentYLevel;
            previousYLevel =  currentYLevel;
                        
            java.util.List<TestEventScore> tesl;

            int counter = 1;
            
            TmLdrCompetencyType tmLdrCompetencyType;
            
            TestEventScore groupTestEventScore;
            
            for( int i=1;i<=5;i++ )
            {
                tmLdrCompetencyType = TmLdrCompetencyType.getValue(i);
                
                groupTestEventScore = TmLdrScoreUtils.getGroupTestEventScore( tmLdrCompetencyType, reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() ) );
                
                tesl = getTmLdrTestEventScoreList( TestEventScoreType.COMPETENCY , tmLdrCompetencyType.getTmLdrCompetencyTypeId() );

                // LogService.logIt( "BaseTmLdrReportTemplate.addReportSummaryChart() found " + tesl.size() );

                if( tesl.isEmpty() )
                    continue;

                t = new PdfPTable( cols );
                t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
                t.setWidths( colRelWids );
                t.setLockedWidth( true );
                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );                
                // totalComps++;

                addTmLdrCompetencySummaryChartSection(t, tesl, tmLdrCompetencyType.getKey(), counter, groupTestEventScore );

                counter++;
                
                if( y - t.calculateHeights() < 60 )
                {
                    addNewPage();
                    y = currentYLevel;
                }
                
                t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
                
                currentYLevel = y - t.calculateHeights() - 2*TPAD;
                y = currentYLevel;
                previousYLevel =  currentYLevel;
                
            }



            //t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            //currentYLevel = y - t.calculateHeights();
            //previousYLevel =  currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addReportSummaryChart()" );

            throw new STException( e );
        }
    }

    
    

    public void addTmLdrOverallSummaryChartSection( PdfPTable tbl, TestEventScore tes )
    {
        // Cell 1 = Name, Email, Test, Date
        String tmp = reportData.getUserName() + "\n";
        Paragraph p = new Paragraph();

        Font fnt = this.getFont();

        p.add( new Chunk( tmp, this.getFontLargeBold() ) );

        if( reportData.getUser().getUserType().getNamed() )
            p.add( new Chunk( this.reportData.getUser().getEmail()+"\n", fnt ) );
        
        p.add( new Chunk( this.reportData.getSimName() + "\n", fnt ) );

        boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );

        if( includeDates )
        {
            String date = this.reportData.getSimCompleteDateFormatted();
            p.add( new Chunk( date+"\n", fnt ) );
        }
        
        PdfPCell c = new PdfPCell( p );
        c.setBorder( Rectangle.NO_BORDER );
        c.setPadding( 3 );
        c.setPaddingBottom( 5 );
        // c.setBorderWidthRight( 0 );

        tbl.addCell( c );

        boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(reportData.getOrg(), reportData.getTestEvent()) && reportData.getTestEvent().getOverallTestEventScore()!=null; // && reportData.getTestEvent().getOverallTestEventScore().getRawScore()>=0;
        
        float scrValue = useRawOverallScore ? reportData.getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow() : reportData.getTestEvent().getOverallScore();        
        // String[] params = new String[] { (int) tes.getScore() + "" };
        String[] params = new String[] { I18nUtils.getFormattedNumber( reportData.getLocale(), scrValue, 0 ) };
        // String[] params = new String[] { I18nUtils.getFormattedNumber( reportData.getLocale(), tes.getScore(), 0 ) };

        String ovrScr = lmsg( "g.TmLdrOverall", params );

        PdfPTable t2 = new PdfPTable( 1 );

        c = new PdfPCell( new Phrase( ovrScr, this.getFontLargeBold() ) );

        c.setBorder( Rectangle.NO_BORDER );
        c.setPaddingLeft( 33 );
        c.setBorderWidthBottom( 0 );
        c.setBorderWidthLeft( 0 );
        c.setBorderWidthTop( 0 );
        t2.addCell( c );


        // Next is the graphic cell
        c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
        c.setBorder( Rectangle.NO_BORDER );

        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        c.setVerticalAlignment( Element.ALIGN_MIDDLE );

        // c.setPadding(8);

        c.setPaddingTop( 7 );
        c.setPaddingBottom( 8 );

        c.setBorderWidthTop( 0 );
        c.setBorderWidthLeft( 0 );

        c.setCellEvent(new TmLdrSummaryScoreGraphicCellEvent( tes, baseFontCalibri, useRawOverallScore ) );

        t2.addCell( c );

        c = new PdfPCell( t2 );
        c.setBorder( Rectangle.NO_BORDER );
        c.setBorderWidth( 0 );
        c.setColspan(2);
        tbl.addCell( c );
    }


    public void addTmLdrCompetencySummaryChartSection( PdfPTable tbl, java.util.List<TestEventScore> tesl, String sectionTitleKey, int index, TestEventScore groupTestEventScore)
    {
        // LogService.logIt("BaseTmLdrReportTemplate.addTmLdrCompetencySummaryChartSection() key=" +  sectionTitleKey + ", tes list=" + tesl.size() );

        // Cell 1 = Name, Email, Test, Date
        String tmp = abcs[index] + ". " + lmsg( sectionTitleKey );

        PdfPCell c = new PdfPCell( new Phrase( tmp, this.fontLargeBlueBold ) );
        // c.setBorderWidthRight( 0 );

        c.setBackgroundColor( ct2Colors.scoreBoxShadeBgColor);
        c.setBorder( Rectangle.NO_BORDER );
        c.setColspan( 1 );
        c.setPadding( 5 );
        tbl.addCell( c );
        
        String groupScore = "";
        
        if( groupTestEventScore != null )
            groupScore = "Score: " + I18nUtils.getFormattedNumber(reportData.getLocale(), groupTestEventScore.getScore(), 0 );
        
        c = new PdfPCell( new Phrase( groupScore, getFont() ) );
        // c.setBorderWidthRight( 0 );

        c.setBackgroundColor( ct2Colors.scoreBoxShadeBgColor);
        c.setBorder( Rectangle.NO_BORDER );
        c.setColspan( 2 );
        c.setHorizontalAlignment( Element.ALIGN_RIGHT );
        c.setPadding( 5 );
        tbl.addCell( c );        

        String[] params = null;

        for( TestEventScore tes : tesl )
        {
            c = new PdfPCell( new Phrase( reportData.getCompetencyName(tes) , this.getFont() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(2);
            c.setPaddingLeft( 5 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            tbl.addCell( c );

            // c = new PdfPCell( new Phrase( (int) tes.getScore() + "", this.getFontBold() ) );

            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 0 ), this.getFontBold() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(2);
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );

            tbl.addCell( c );

            // Next is the graphic cell
            c = new PdfPCell( new Phrase("\n\n", this.getFont()) ); // new PdfPCell( summaryCatNumericAxis );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            // c.setPadding(8);

            c.setPaddingTop( 4 );
            c.setPaddingBottom( 2 );

            // c.setBorderWidth( 0 );

            c.setCellEvent(new TmLdrSummaryScoreGraphicCellEvent( tes, baseFontCalibri, false ) );

            tbl.addCell( c );
        }
    }



    public void addComparisonSection() throws Exception
    {
        try
        {
            // Be sure there's no rule to skip this section.
            // String hasRule1 = this.reportData.getReportRule( "skipcomparisonsection" );

            if( reportData.getReportRuleAsBoolean(  "skipcomparisonsection" ) ) //  hasRule1 != null && hasRule1.equals( "1" ) )
                return;
            
            if( reportData.getReport().getIncludeNorms()==1 && !reportData.getTestEvent().getOverallTestEventScore().getHasValidNorms() )
                prepNotes.add( lmsg( "g.RptNote_NoNorms_InsufficientData" ) );

            if( reportData.getReport().getIncludeNorms()==0 )
                return;

            //if( reportData.getReport().getIncludeNorms()==0 || !reportData.getTestEvent().getOverallTestEventScore().getHasValidNorms() )
            //     return;

            String reportCompanyName = reportData.getReportCompanyName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                      ";

            if( reportCompanyName == null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();



            String[] names = new String[] { lmsg( "g.Overall" ), getCountryName( reportData.getTestEvent().getPercentileCountry()!=null && !reportData.getTestEvent().getPercentileCountry().isEmpty() ? reportData.getTestEvent().getPercentileCountry() : reportData.getTestEvent().getIpCountry() ), StringUtils.truncateString(reportCompanyName, 22 ) };

            float[] percentiles = new float[] { reportData.getTestEvent().getOverallPercentile(), reportData.getTestEvent().getCountryPercentile(),reportData.getTestEvent().getAccountPercentile() };

            int[] counts = new int[] { reportData.getTestEvent().getOverallPercentileCount(), reportData.getTestEvent().getCountryPercentileCount(),reportData.getTestEvent().getAccountPercentileCount() };

            if( !reportData.getTestEvent().getOverallTestEventScore().getHasValidOverallNorm() )
                prepNotes.add( lmsg( "g.RptNote_NoOverallNorms_InsufficientData" ) );

            if( !reportData.getTestEvent().getOverallTestEventScore().getHasValidCountryNorm() )
                prepNotes.add( lmsg( "g.RptNote_NoCountryNorms_InsufficientData" ) );

            if( !reportData.getTestEvent().getOverallTestEventScore().getHasValidAccountNorm() )
                prepNotes.add( lmsg( "g.RptNote_NoAccountNorms_InsufficientData" ) );

            int validCount = 0;

            if( reportData.getTestEvent().getOverallTestEventScore().getHasValidOverallNorm() )
                validCount++;

            if( reportData.getTestEvent().getOverallTestEventScore().getHasValidCountryNorm() )
                validCount++;

            if( reportData.getTestEvent().getOverallTestEventScore().getHasValidAccountNorm() )
                validCount++;


            //for( float p : percentiles )
            //{
            //    if( p>0 )
            //        validCount++;
            //}


            // no data to show.
            //if( validCount == 0 )
            //{
                // reportComparisonBoxY = reportSummaryBoxY;
            //    return;
            //}

            previousYLevel =  currentYLevel; // - TPAD;

            float y = previousYLevel;

            float thgt =0;// t!= null  t.calculateHeights();

            PdfPTable t = null;
            PdfPCell c;

            // First, add a table
            t = new PdfPTable( 3 );

            // float extraMargin = 25;
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( new float[] {22,13,72} );
            t.setLockedWidth( true );

            if( validCount > 0 )
            {
                int rowCount = 0;

                // Now create the graph
                // First create the table

                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );

                // Create header
                c = new PdfPCell( new Phrase( lmsg("g.TesterGroup"), fontLargeWhite ) );
                // c.setBorder( Rectangle.TOP | Rectangle.LEFT  );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBorderWidth( scoreBoxBorderWidth );
                //c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setColspan( 1 );
                c.setPadding( 1 );
                //c.setPaddingBottom( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                // c.setBackgroundColor( ct2Colors.scoreBoxShadeBgColor );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg("g.Percentile"), fontLargeWhite ) );
                // c.setBorder( Rectangle.TOP  );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setColspan( 1 );
                c.setPadding( 1 );
                //c.setPaddingBottom( 3 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                c.setBackgroundColor( ct2Colors.hraBlue );
                t.addCell(c);

                // Create the cell for the full table
                c = new PdfPCell( new Phrase( "" ) );
                c.setBackgroundColor(  ct2Colors.hraBlue );
                // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setCellEvent(new PercentileHeaderCellEvent( baseFontCalibri , validCount, ct2Colors ) );
                t.addCell(c);

                // Next, for each row
                if( reportData.getTestEvent().getOverallTestEventScore().getHasValidOverallNorm() )
                {
                    rowCount++;
                    addComparisonTableRow( t, names[0], percentiles[0], counts[0], rowCount==validCount );
                }

                if( reportData.getTestEvent().getOverallTestEventScore().getHasValidCountryNorm() )
                {
                    rowCount++;
                    addComparisonTableRow( t, names[1], percentiles[1], counts[1], rowCount==validCount );
                }

                if( reportData.getTestEvent().getOverallTestEventScore().getHasValidAccountNorm() )
                {
                    rowCount++;
                    addComparisonTableRow( t, names[2], percentiles[2], counts[2], rowCount==validCount );
                }

                /*
                if( validCount == 0 )
                {
                    Paragraph p = new Paragraph();

                    Chunk ch = new Chunk( lmsg( "g.NoteC" ) + " ", this.fontBoldItalic  );

                    p.add( ch );

                    ch = new Chunk(  lmsg( "g.InsufficientDataForComparisons" ), this.fontItalic );

                    p.add( ch );

                    c = new PdfPCell( p );
                    c.setColspan( 3 );
                    c.setPadding( 4 );
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                    t.addCell(c);
                } */

                thgt = t.calculateHeights();
            }

            else
            {
                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );

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
                //c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                t.addCell(c);

                thgt = t.calculateHeights();
            }

            // LogService.logIt( "BaseTmLdrReportTemplate.addComparisonSection() validCount=" + validCount + ", thgt=" + thgt + ", rowCount=" + rowCount );

            if( thgt + 45 > y )
            {
                addNewPage();

                y = currentYLevel;
            }

            y = addTitle( y, lmsg( "g.ComparisonPcts" ), validCount > 0 ? lmsg( "g.PercentileNote" ) : null );

            y -= TPAD;

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y- thgt - PAD;

            // currentYLevel = addText( lmsg( "g.PercentileNote" ), fnt );

            // currentYLevel -= PAD;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addComparisonSection()" );

            throw new STException( e );
        }
    }


    private void addComparisonTableRow( PdfPTable t, String name, float percentile, int count, boolean last )
    {
            // LogService.logIt( "BaseTmLdrReportTemplate.addComparisonTableRow() name=" + name + ", percentile=" + percentile + ", last =" + last );

            PdfPCell c;

            Font fnt = this.font;

            c = new PdfPCell( new Phrase( name, fnt ) );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBorder( last ? Rectangle.LEFT | Rectangle.BOTTOM  : Rectangle.LEFT );
            //c.setBorderWidth( scoreBoxBorderWidth );
            //c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 1 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            t.addCell(c);

            String perStr = I18nUtils.getFormattedNumber( reportData.getLocale(), percentile, 0 ) + NumberUtils.getPctSuffix( reportData.getLocale(), percentile, 0 );

            // I18nUtils.getFormattedInteger( reportData.getLocale(), (int) percentile );

            // Create the cell for the full table
            c = new PdfPCell( new Phrase( perStr, fnt ) );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBorder( last ? Rectangle.BOTTOM : Rectangle.NO_BORDER );
            //c.setBorderWidth( scoreBoxBorderWidth );
            //c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            t.addCell(c);


            c = new PdfPCell( new Phrase( " ", this.getFontLargeWhite() ) );
            c.setPadding( 1 );
            //if( last )
            //    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
            //else
            //    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

            c.setBorder( Rectangle.NO_BORDER );
            String ctStr = lmsg( "g.PercentileCountStr1", new String[] { Integer.toString(count)}  );

            if( 1==1 )
                ctStr = "";

            //c.setBorderWidth( scoreBoxBorderWidth );
            //c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new PercentileBarCellEvent( percentile , ctStr, ct2Colors.barGraphCoreShade2, ct2Colors.barGraphCoreShade1, baseFontCalibri ) );
            t.addCell(c);
    }



    public String getCountryName( String countryCode )
    {
        if( countryCode == null || countryCode.isEmpty() )
            countryCode = "US";

        String c = lmsg( "cntry." + countryCode );

        if( c == null || c.isEmpty() )
            return lmsg( "g.Country" );

        return c;
    }


    /**
     * Returns true if added a new page.
     *
     * @return
     * @throws Exception
     */
    public boolean addAssessmentOverview() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            // if( reportData.getReport().getIncludeOverviewText()==0 )
            //     return false;

            // currentYLevel = 0;

            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() " + ovrTxt );

            // if( ovrTxt == null || ovrTxt.isEmpty() )
            //     return false;

            Font fnt =   getHeaderFontXLarge();

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            float y = addTitle( previousYLevel, "\n\n" +  lmsg( "g.AssessmentOverview" ), null );

            y -= PAD;

            // Change getFont()
            fnt =  getFont();

            float thgt = 0;

            // float spaceLeft = y - 2*PAD - footerHgt;
            float spaceLeft = y - footerHgt;

            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float leading = fnt.getSize();

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            // Add paragraph 1
            String txt1 =  lmsg( "g.TmLdrAssessOverLine1" );

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, txt1, txtW, Element.ALIGN_LEFT, leading, fnt);

            //  y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );


            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN;
            float txtUrx = txtLlx + txtW;

            // y -= txtHght;
            Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );
            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

            ITextUtils.addDirectText(  pdfWriter, txt1, rect, Element.ALIGN_LEFT, leading, fnt, false );
            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
            currentYLevel = y - txtHght;

            y -= txtHght;

            // Graphic table
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable innerT = new PdfPTable( 1 );

            innerT.setTotalWidth( 0.35f*(pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN)/2 );

            c = innerT.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBorderWidth(0.5f);
            //c.setBorderColor( BaseColor.BLACK );

            c = new PdfPCell( new Phrase( lmsg( "g.TmLdrAssessOverBox" ), fnt ) );
            c.setBorder(Rectangle.NO_BORDER);
            innerT.addCell(c);

            thgt = innerT.calculateHeights();

            // float innerTHgt = innerT.getTotalHeight();


            // First, add a table
            PdfPTable t = new PdfPTable( new float[] {0.5f,0.5f} );

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            // t.setWidths( colRelWids );
            t.setLockedWidth( true );

            // t.setHeaderRows( 1 );


            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            // Create Cell
            c = new PdfPCell( fiveCircleImage );
            c.setBorder(Rectangle.NO_BORDER);
            c.setPadding( 1 );
            //c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            t.addCell(c);

            c = new PdfPCell( innerT );
            c.setBorder(Rectangle.NO_BORDER);
            c.setPadding( 16 );
            c.setPaddingTop( 56 );
            c.setPaddingBottom( 56 );

            //c.setPaddingBottom( 5 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            t.addCell(c);

            thgt = t.calculateHeights();

            y -= 2*TPAD;

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            y = y- thgt - 2*PAD;

            currentYLevel = y; // - thgt - PAD;



            txt1 =  "\n\n" + lmsg( "g.TmLdrAssessOverLine2" );

            txtHght = ITextUtils.getDirectTextHeight( pdfWriter, txt1, txtW, Element.ALIGN_LEFT, leading, fnt);
            txtUrx = txtLlx + txtW;
            rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );
            ITextUtils.addDirectText(  pdfWriter, txt1, rect, Element.ALIGN_LEFT, leading, fnt, false );

            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );

            y -= txtHght + 2*TPAD;

            currentYLevel = y; // - txtHght;

            // Explaination Table
            t = new PdfPTable( new float[] {0.4f,0.6f} );

            t.setTotalWidth( 0.65f*(pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN) );

            float tLeftMargin = (pageWidth - t.getTotalWidth())/2;

            t.setHorizontalAlignment( Element.ALIGN_CENTER );

            float tWidth = t.getTotalWidth();

            // t.setWidths( colRelWids );
            t.setLockedWidth( true );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.TmLdrAssessOverTableH1"), fontLargeWhite ) );
            //c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.TmLdrAssessOverTableH2"), fontLargeWhite ) );
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false ));
            t.addCell(c);

            String lftCol, rgtCol;

            for( int i=1;i<=5; i++ )
            {
                lftCol = i + ". " + lmsg( "g.TmLdrAssessOver_" + i + "_a");
                rgtCol = lmsg( "g.TmLdrAssessOver_" + i + "_b");

                c = new PdfPCell( new Phrase( lftCol, fnt ) );
                c.setBorder( Rectangle.BOX );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 3 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setBackgroundColor( BaseColor.WHITE );
                t.addCell(c);

                c = new PdfPCell( new Phrase( rgtCol, fnt ) );
                c.setBorder( Rectangle.BOX );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 3 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setBackgroundColor( BaseColor.WHITE );
                t.addCell(c);
            }

            thgt = t.calculateHeights();

            y -= 2*TPAD;

            t.writeSelectedRows(0, -1, tLeftMargin, y, pdfWriter.getDirectContent() );
            // t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y- thgt - 2*PAD;

            return false;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addAssessmentOverview()" );

            throw new STException( e );
        }
    }





    public void addDetailedReportInfoHeader() throws Exception
    {
        try
        {
            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            else if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";

            String reportCompanyAdminEmail = "";

            String reportCompanyAdminName = reportData==null ? null : reportData.getReportCompanyAdminName();

            if( (reportCompanyAdminName==null || reportCompanyAdminName.isEmpty()) && reportData.getTestKey().getAuthUser() != null  )
            {
                reportCompanyAdminName = reportData.getTestKey().getAuthUser().getFullname();
                reportCompanyAdminEmail = reportData.getTestKey().getAuthUser().getEmail();
            }

            else if( StringUtils.isCurlyBracketed( reportCompanyAdminName ) )
                reportCompanyAdminName = "                        ";

            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" ); 

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.Detail" ), null );

            y -= PAD;

            Font fntRgt = fontXLarge;
            Font fntRgt2 = font;
            Font fntLft = font;

            float x = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN;

            // Now, let's create a table!
            PdfPTable t = new PdfPTable( 2 );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            t.setTotalWidth( txtW );
            t.setLockedWidth( true );
            t.setWidths( new float[] {5,29} );


            t.setHorizontalAlignment( Element.ALIGN_CENTER );

            PdfPCell dc = t.getDefaultCell();
            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setVerticalAlignment( Element.ALIGN_BOTTOM );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding( 2 );

            Phrase p = new Phrase( lmsg( "g.CandidateC"), fntLft );
            t.addCell( p );

            Chunk c = new Chunk( reportData.getUserName() + ( !reportData.u.getUserType().getNamed() || reportData.u.getEmail()==null || StringUtils.isCurlyBracketed( reportData.u.getEmail() ) ? "" : ", "), fntRgt );
            p = new Phrase();
            p.add( c );
            c = new Chunk( (!reportData.u.getUserType().getNamed() || reportData.u.getEmail()==null || StringUtils.isCurlyBracketed( reportData.u.getEmail() ) ? "" : reportData.u.getEmail()), fntRgt2 );
            p.add( c );
            t.addCell( p );

            if( reportData.u.getHasAltIdentifierInfo() )
            {
                String ainame = reportData.u.getAltIdentifierName();

                if( ainame == null || ainame.isEmpty() )
                    ainame = lmsg(  "g.DefaultAltIdentifierName" );


                t.addCell( new Phrase(  ainame + ":", fntLft ) );
                t.addCell( new Phrase( reportData.u.getAltIdentifier(), fntLft ) );
            }

            t.addCell( new Phrase( lmsg( "g.AssessmentC"), fntLft ) );
            t.addCell( new Phrase( reportData.getSimName(), fntLft ) );



            String[] params = new String[] { I18nUtils.getFormattedDate( reportData.getLocale(), reportData.getTimeZone(), reportData.getTestKey().getStartDate() ),
                                             reportCompanyAdminName == null ? "" : reportCompanyAdminName,
                                             reportCompanyName == null ? "" : reportCompanyName,
                                             reportCompanyAdminEmail==null ? "" : reportCompanyAdminEmail };

            String auth = reportData.getTestKey().getAuthUser() == null ? lmsg( "g.AuthStr" + (includeDates ? "" : "NoDates") , params ) : lmsg( "g.AuthStrCombined" + (includeDates ? "" : "NoDates") , params );

            t.addCell( new Phrase( lmsg( "g.AuthorizedC"), fntLft ) );
            t.addCell( new Phrase( auth, fntLft ) );

            if( includeDates )
            {
                t.addCell( new Phrase( lmsg( "g.StartedC"), fntLft ) );
                t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getStartDate(), reportData.getUser().getTimeZone() ), fntLft ) );

                t.addCell( new Phrase( lmsg( "g.FinishedC"), fntLft ) );
                t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getLastAccessDate(), reportData.getUser().getTimeZone() ), fntLft ) );
            }
            
            boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(reportData.getOrg(), reportData.getTestEvent()) && reportData.getTestEvent().getOverallTestEventScore()!=null; //  && reportData.getTestEvent().getOverallTestEventScore().getRawScore()>=0;
           
            float scrValue = useRawOverallScore ? reportData.getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow() : reportData.getTestEvent().getOverallScore();        
                        
            t.addCell( new Phrase( lmsg( "g.OverallScoreC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedNumber( reportData.getLocale(), scrValue, reportData.getTestEvent().getScorePrecisionDigits()), fntLft ) );

            t.writeSelectedRows(0, -1, x, y, pdfWriter.getDirectContent() );

           //  LogService.logIt( "BaseTmLdrReportTemplate.addDetailedReportHeader() t.calculateHeights()=" + t.calculateHeights() + ", currentY=" + y );

            currentYLevel = y - t.calculateHeights();

            // LogService.logIt( "BaseTmLdrReportTemplate.addDetailedReportHeader() currentYLevel=" + currentYLevel );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addDetailedReportInfoHeader()" );

            throw new STException( e );
        }
    }


    protected java.util.List<TestEventScore> getTmLdrTestEventScoreList( TestEventScoreType test , int tmLdrCompetencyTypeId )
    {
        return TmLdrScoreUtils.getTmLdrTestEventScoreList( tmLdrCompetencyTypeId, reportData.getTestEvent().getTestEventScoreList( test.getTestEventScoreTypeId() ) );
    }

    protected java.util.List<TestEventScore> getTestEventScoreListToShow( TestEventScoreType test, SimCompetencyClass scc )
    {
        java.util.List<TestEventScore> out = new ArrayList<>();

        for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( test.getTestEventScoreTypeId() ) )
        {
            if( tes.getSimCompetencyClassId() != scc.getSimCompetencyClassId() )
                continue;

            // if supposed to hide
            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;

            out.add( tes );
        }

        Collections.sort(out, new TESNameComparator() );


        return out;
    }



    protected void addAllDetailCompsInfoSections() throws Exception
    {
        for( int i=1;i<=5; i++ )
        {
            addDetailCompsInfo( i,  BaseTmLdrReportTemplate.detailCompTitleKeys[i],   BaseTmLdrReportTemplate.detailCompSubtitleKeys[i] );
        }
    }


    protected void addDetailCompsInfo( int index, String titleKey, String subtitleKey ) throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            java.util.List<TestEventScore> tesl = this.getTmLdrTestEventScoreList(TestEventScoreType.COMPETENCY, index );

            //        getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY ); // new ArrayList<>();

            if( tesl.size() <= 0 )
                return;

            // LogService.logIt( "BaseTmLdrReportTemplate.addDetailCompsInfo() index=" + index + "  found " + tesl.size() );

            addAnyCompetenciesInfo( tesl, titleKey, subtitleKey, "g.Detail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addDetailCompsInfo()" );

            throw new STException( e );
        }
    }




    protected void addEducTrainingInfo() throws Exception
    {
        try
        {
            if( !reportData.includeEducTypeDescrip() && ! reportData.includeTrainingTypeDescrip() && !reportData.includeRelatedExperTypeDescrip() )
                return;

            TestEvent te = reportData.getTestEvent();

            // no data
            if( te.getEducTypeId() == 0 && te.getExperTypeId()==0 && te.getTrainTypeId()==0 )
                return;

            // At this point we will create a report

            previousYLevel =  currentYLevel;

            java.util.List<TextAndTitle> ttl = new ArrayList<>();

            if( reportData.includeEducTypeDescrip() && te.getEducTypeId() > 0)
                ttl.add( new TextAndTitle( EducType.getValue( te.getEducTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinEducLevel" ) ) );

            if( reportData.includeTrainingTypeDescrip()&& te.getTrainTypeId() > 0)
                ttl.add( new TextAndTitle( TrainingType.getValue( te.getTrainTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinTrainType" ) ) );

            if( reportData.includeRelatedExperTypeDescrip()&& te.getExperTypeId() > 0)
                ttl.add( new TextAndTitle( RelatedExperType.getValue( te.getTrainTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinRelatedExp" ) ) );

            if( ttl.isEmpty() )
                return;

            float y = addTitle( previousYLevel, lmsg( "g.MinQualGuidelines" ), lmsg( "g.MinQualGuidelinesSubtitle" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            //t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor(  ct2Colors.hraBlue );

            c = new PdfPCell(new Phrase( lmsg( "g.Item" ) , fontLmWhite));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            t.addCell(c);
            
            c = new PdfPCell(new Phrase( "" , fontLmWhite));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            t.addCell(c);
            
            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.Item" ) , fontLmWhite) );
            //t.addCell( new Phrase( "" , fontLmWhite) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( this. ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( this. ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                t.addCell( c );

            }

            currentYLevel = addTableToDocument( y, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addEducTrainingInfo()" );

            throw new STException( e );
        }
    }

    protected void addPreparationNotesSection() throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseTmLdrReportTemplate.addPreparationNotesSection() START" );

            if( reportData.getReport().getIncludeNorms()>0 )
                 prepNotes.add( 0, lmsg( "g.CT3ComparisonVsOverallNote" ) );



            prepNotes.add( 0, lmsg( "g.CT3RptCaveat" ) );
						prepNotes.add( lmsg( "g.CT3RptGraphKey" ) );
            prepNotes.add( lmsg( "g.CT3RptGraphKeyLinear" ) );
            prepNotes.add( lmsg( "g.CT3RptGraphKeyNonlinear" ) );

            Product p = reportData.getTestEvent().getProduct();

            if( 1==2 &&  p != null && p.getOnetSoc()!=null && !p.getOnetSoc().isEmpty() )
            {
                prepNotes.add( lmsg( "g.OnetDescrip", null ));

                prepNotes.add( lmsg( "g.OnetSocX", new String[]{p.getOnetSoc()} ));

                if( p.getOnetVersion()!=null && !p.getOnetVersion().isEmpty() )
                    prepNotes.add( lmsg( "g.OnetVersionX", new String[]{p.getOnetVersion()} ));
            }

            Calendar cal = new GregorianCalendar();            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");            
            String dtStr = df.format( cal.getTime() );

            if( reportData.getReportRuleAsBoolean( "hidedatespdf" ) )
                dtStr="***";

            prepNotes.add( lmsg( "g.SimIdAndVersion", new String[]{ Long.toString( reportData.getTestEvent().getSimId()) , Integer.toString(reportData.getTestEvent().getSimVersionId() ), Long.toString( reportData.getTestEvent().getTestKeyId()), Long.toString( reportData.getTestEvent().getTestEventId()), Long.toString( reportData.getReport().getReportId() ), Integer.toString( reportData.getTestKey().getProductId() ), dtStr } ));

            if( prepNotes.isEmpty() )
                return;

            this.addNewPage();
            
            previousYLevel =  currentYLevel;

            
            float y = addTitle( previousYLevel, lmsg( "g.PreparationNotes" ), null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 1f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
            //Paragraph cHdr=null;
            //Paragraph cFtr=null;
            //float spcg = 8;
            cl.setListSymbol( "\u2022");

            for( String ct : prepNotes )
            {
                if( ct.isEmpty() )
                    continue;

                cl.add( new ListItem( new Paragraph( ct , getFont() ) ) );
            }

            c = new PdfPCell();
            c.setBorder( Rectangle.BOX );
            c.setBackgroundColor( BaseColor.WHITE );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPaddingTop( 8 );
            c.setPaddingLeft(10);
            c.setPaddingRight(5);
            c.setPaddingBottom( 14 );
            c.addElement( cl );
            t.addCell(c);

            currentYLevel = addTableToDocument( y, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addPreparationNotesSection()" );

            throw new STException( e );
        }
    }




    public void addAltScoreSection() throws Exception
    {
        try
        {
            Profile pr;
            
            if( reportData.te.getAltScoreProfileList() == null && reportData.te.getTestEventScoreList( TestEventScoreType.ALT_OVERALL.getTestEventScoreTypeId() ).isEmpty() )
            {
                ProfileFacade profileFacade = ProfileFacade.getInstance();

                List<Profile> pl = profileFacade.getProfileListForProductIdAndOrgIdAndProfileUsageType( reportData.te.getProductId() , reportData.te.getOrgId(), ProfileUsageType.ALTERNATE_OVERALL_COMPETENCY_WEIGHTS.getProfileUsageTypeId()  );

                reportData.te.setAltScoreProfileList( pl );

                Collections.sort( pl, new ProfileStrParam1Comparator() );                
                
                ListIterator<Profile> li = pl.listIterator();
                
                while( li.hasNext() )
                {
                    pr = li.next();
                    
                    if( pr.getProfileEntryList()==null )
                        pr.setProfileEntryList( profileFacade.getProfileEntryList( pr.getProfileId() ));
                    
                    pr.setAltScoreCalculator( AltScoreCalculatorFactory.getAltScoreCalculator( null, null, null, reportData.te, pr ));
                    
                    if( !pr.getAltScoreCalculator().hasValidScore() )
                        li.remove();
                }
            }
            
            if( reportData.te.getAltScoreProfileList() == null || reportData.te.getAltScoreProfileList().isEmpty() )
                return;
            
            LogService.logIt(  "BaseTmLdrReportTemplate.addAltScoreSection() START - Found " +  reportData.te.getAltScoreProfileList().size() + " valid Alt Profiles." );

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.AltScoresTitle" ), lmsg( "g.AltScoresSubtitle" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 4f,6f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            //t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingBottom( 4 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );

            c = new PdfPCell(new Phrase( lmsg( "g.Position" ) , fontLmWhite));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            t.addCell(c);
            
            c = new PdfPCell(new Phrase( lmsg( "g.Score" ) , fontLmWhite ));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            t.addCell(c);
            
            
            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.Position" ) , fontLmWhite) );
            //t.addCell( new Phrase( lmsg( "g.Score" ) , fontLmWhite ) );

            c.setBackgroundColor( BaseColor.WHITE );

            // Phrase ep = new Phrase( "", getFontSmall() );

            // Phrase p;

            for( Profile tt : reportData.te.getAltScoreProfileList() )
            {

                c = new PdfPCell( new Phrase( tt.getStrParam1(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( Integer.toString( (int) tt.getAltScoreCalculator().getScore() ), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

            } // each alt score

            currentYLevel = addTableToDocument( y, t );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addAltScoreSection()" );

            throw new STException( e );
        }
        
    }

    
    /*
    protected void addTasksInfo() throws Exception
    {
        try
        {
            if( !reportData.includeTaskInfo() )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            SimCompetencyClass scc;

            for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                if( !scc.getIsTask() )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            LogService.logIt( "BaseTmLdrReportTemplate.addTasksInfo()" );

            addAnyCompetenciesInfo( tesl, "g.Tasks", null, "g.TaskDetail", "g.TaskDescription", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addTasksInfo()" );

            throw new STException( e );
        }
    }
    */



    protected void addAnyCompetenciesInfo( java.util.List<TestEventScore> teslst,
                                            String titleKey,
                                            String subtitleKey,
                                            String detailKey,
                                            String descripKey,
                                            String caveatHeaderKey,
                                            String caveatFooterKey,
                                            boolean withInterview,
                                            boolean noInterviewLimit) throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel - 10;

            if( previousYLevel <= footerHgt )
            {
                document.newPage();

                currentYLevel = 0;
                previousYLevel = 0;
            }


            int interviewQsPerComp = reportData.getReport().getMaxInterviewQuestionsPerCompetency();
            boolean numeric = this.reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean rating = this.reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean norms = this.reportData.getReport().getIncludeSubcategoryNorms()==1;
            boolean interpretation = reportData.getReport().getIncludeSubcategoryInterpretations()==1;

            boolean includeTesNorm = norms;

            // rating = false;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            for( TestEventScore tes : teslst )
            {
                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                // skip non-auto-scored competencies.
                if( tes.getScore()<0 )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            // Collections.sort( tesl, new TestEventScoreWeightNameComparator() );

            float y = addTitle( previousYLevel, lmsg( titleKey ), ( subtitleKey==null ?  null : lmsg( subtitleKey ) ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            // This tells iText to always use the first row as a header on subsequent pages.
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );

            c = new PdfPCell(new Phrase( lmsg( detailKey ) , fontLmWhite));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            t.addCell(c);
            
            c = new PdfPCell(new Phrase( lmsg( withInterview ? "g.InterviewGuide" : descripKey ) , fontLmWhite ));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            t.addCell(c);
            
            
            // Add header row.
            //t.addCell( new Phrase( lmsg( detailKey ) , fontLmWhite) );
            //t.addCell( new Phrase( lmsg( withInterview ? "g.InterviewGuide" : descripKey ) , fontLmWhite ) );

            c.setBackgroundColor( BaseColor.WHITE );

            PdfPTable compT;
            PdfPTable igT;
            // PdfPTable numGrphT;
            ScoreCategoryType sct;
            Image dotImg;

            java.util.List<InterviewQuestion> igL;
            InterviewQuestion ig;

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            String scoreText;

            // String caveatText;
            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            boolean useGrayBg = false;

            
            // For each competency
            for( TestEventScore tes : tesl )
            {
                // LogService.logIt( "BaseTmLdrReportTemplate.addAnyCompetenciesInfo() adding " + tes.toString() );
                includeTesNorm = norms && SimCompetencyClass.getValue( tes.getSimCompetencyClassId() ).getSupportsPercentiles();

                // First do the score info.
                compT = new PdfPTable( new float[] {6,4} );
                compT.setHorizontalAlignment( Element.ALIGN_CENTER );
                compT.setTotalWidth( 0.9f*outerWid*3.5f/9f );
                compT.setLockedWidth(true);

                c = compT.getDefaultCell();
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 1 );

                sct = tes.getScoreCategoryType();
                dotImg = getScoreCategoryImg( sct, true );

                // Name Cell
                c = new PdfPCell( new Phrase( reportData.getCompetencyName(tes)  , getFontBold() ) );
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( 1 );
                compT.addCell( c );

                // Dot Cell
                c = rating && dotImg!=null ?  new PdfPCell( dotImg ) : new PdfPCell( new Phrase( "" , getFontSmall() ) );
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( 1 );
                compT.addCell( c );

                // Score && Percentile
                if( numeric || norms )
                {
                    c = new PdfPCell( new Phrase( numeric ? lmsg("g.ScoreC") + " " + (tes.getScore()>=0 ? I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 0) : "-") : "", fontSmall ) );
                    c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    compT.addCell( c );

                    StringBuilder srcStr = new StringBuilder();

                    if( includeTesNorm || norms )
                        srcStr.append( lmsg("g.PercentileC") + " " + ( includeTesNorm && tes.getPercentile()>=0 ? Integer.toString((int) tes.getPercentile())+NumberUtils.getPctSuffix( reportData.getLocale(), tes.getPercentile(), 0 ) : lmsg("g.NA") ) );

                    c = new PdfPCell( new Phrase( srcStr.toString(), fontSmall ) );
                    c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    c.setPaddingRight( 2 );
                    compT.addCell( c );
                }

                // ScoreGraphCell
                if( numeric )
                {
                    //numGrphT  = new PdfPTable( 1 );
                    //numGrphT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    //numGrphT.setTotalWidth( 0.8f*outerWid*3.5f/9f );
                    //numGrphT.setLockedWidth(true);

                    //c = new PdfPCell( new Phrase( NumberUtils.roundIt( tes.getScore(), 1) + "" , getFontBold() ) );
                    //c.setBorder( Rectangle.NO_BORDER );
                    //c.setVerticalAlignment( Element.ALIGN_CENTER );
                    //c.setPadding( 2 );
                    //c.setPaddingBottom( 5 );
                    //numGrphT.addCell( c );

                    c = new PdfPCell( new Phrase( "" , fontXLarge ) );
                    c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan( 2 );
                    c.setPadding( 9 );
                    
                    c.setFixedHeight(16);
                    c.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, devel, false, false, true, 0 ) );
                    //numGrphT.addCell( c );
                    compT.addCell( c );

                    // c = new PdfPCell( numGrphT );
                    //c.setBorder( Rectangle.NO_BORDER );
                    //c.setColspan( 2 );
                    //c.setPadding( 2 );
                    // compT.addCell( c );
                }

                if( interpretation )
                {
                    c = new PdfPCell( new Phrase( lmsg( "g.InterpretationC" ) , fontSmallItalic ) );
                    c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setColspan( 2 );
                    c.setPadding( 1 );
                    // c.setPaddingTop(1);
                    compT.addCell( c );

                    c = new PdfPCell( new Phrase( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), fontSmall ) );
                    c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setColspan( 2 );
                    c.setPadding( 1 );
                    compT.addCell( c );
                }


                // The Big text cell
                //p = new Phrase();


                scoreText = reportData.getCompetencyScoreText( tes ); // tes.getScoreText();

                if( scoreText == null )
                    scoreText = "";

                List<CaveatScore> topicCsl = reportData.getReportRuleAsBoolean( "cmptytopicsoff" ) ? new ArrayList<>() :  reportData.getTopicCaveatScoreList(tes);
                String cHdrStr=null;
                String cFtrStr=null;
                
                if( !topicCsl.isEmpty() )
                {
                    if( caveatHeaderKey != null && !caveatHeaderKey.isEmpty() )
                        cHdrStr = lmsg( caveatHeaderKey );

                    if( caveatFooterKey != null && !caveatFooterKey.isEmpty() )
                        cFtrStr = lmsg( caveatFooterKey );
                }
                
                PdfPTable scoreTextAndCaveatTable = getScoreTextAndTopicCaveatScoreTable(scoreText, cHdrStr, cFtrStr, topicCsl, getFontSmall(), tes.getSimCompetencyClassId() );
                
                
                //Phrase pg;
                //java.util.List<String[]> topicCaveatList = reportData.getReportRuleAsBoolean( "cmptytopicsoff" ) ? new ArrayList<>() :  ReportUtils.getParsedTopicScores(reportData.getCaveatList(tes), reportData.getLocale(), tes.getSimCompetencyClassId() );
                
                //com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                //Paragraph cHdr=null;
                //Paragraph cFtr=null;
                //float spcg = 8;
                //cl.setListSymbol( "\u2022");

                //Phrase cst = new Phrase( new Phrase( scoreText, getFontSmall() ) );
                //cst.setLeading( 10 );

                //for( String ct : reportData.getCaveatList(tes) )
                //{
                 //   if( ct.isEmpty() )
                //        continue;

                //    if( ct.startsWith( Constants.TOPIC_KEY + "~" ) )
                //        continue;
                    
                //    cl.add( new ListItem( new Paragraph( ct , getFontSmall() ) ) );
                //}

                //for( String[] ct : topicCaveatList )
                //{                                        
                    // String[] ct = topicCaveatList.get(0);
                //    cl.add( new ListItem( new Paragraph( ct[1] + ": " + ct[2] , getFontSmall() ) ) );                    
                //}                
                
                //if( cl.size()>0 )
                //{
                //    if( caveatHeaderKey != null && !caveatHeaderKey.isEmpty() )
                //    {
                //        cHdr = new Paragraph( lmsg( caveatHeaderKey ) , getFontSmall() );
                //        cHdr.setSpacingBefore( spcg );
                //        cHdr.setSpacingAfter( spcg );
                //        cHdr.setLeading( 10 );
                //    }

                //    if( caveatFooterKey != null && !caveatFooterKey.isEmpty() )
                //    {
                //        cFtr = new Paragraph( lmsg( caveatFooterKey ) , getFontSmall() );
                //       cFtr.setSpacingBefore( spcg );
                //        cFtr.setLeading( 10 );
                //    }
                //}

                // ScoreText Cell
                c = new PdfPCell(); // new Phrase( scoreText, getFontSmall() )  );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setColspan( 2 );
                c.setPaddingTop( 10 );
                c.addElement( scoreTextAndCaveatTable );
                //c.addElement( cst );
                //if( cl.size()>0 )
                //{
                //    c.addElement( cHdr );
                //    c.addElement( cl );
                //    c.addElement( cFtr );
                //}

                compT.addCell(c);

                // add to table.
                c = new PdfPCell();
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                //c.setBorderWidth( 0.5f );
                c.setPadding(3);
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.addElement( compT );
                t.addCell( c );

                if( withInterview )
                {
                    // next, the interview guide
                    igT = new PdfPTable( 15 );
                    igT.setTotalWidth( 0.9f * outerWid*5.5f/9f );
                    igT.setLockedWidth(true);
                    igT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c = igT.getDefaultCell();
                    c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setPadding( 2 );

                    int iqs = 0;

                    int maxInt = noInterviewLimit ? MAX_INTERVIEWQS_PER_COMPETENCY : interviewQsPerComp;

                    maxInt = Math.min( maxInt, MAX_INTERVIEWQS_PER_COMPETENCY );

                    igL = reportData.getInterviewQuestionList(tes, maxInt); //tes.getInterviewQuestionList( maxInt );

                    // for( int i=0; i<interviewQsPerComp; i++ )
                    for( int i=0; i<maxInt && i<igL.size(); i++ )
                    {
                        if( i >= igL.size() )
                            break;

                        // need a boundary
                        if( i > 0 )
                        {
                            LineSeparator ls = new LineSeparator( 1,90,BaseColor.BLACK,Element.ALIGN_CENTER, 0 );
                            c = new PdfPCell();
                            c.addElement( ls );
                            c.setBorder( Rectangle.NO_BORDER );
                            c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                            c.setColspan( 15 );
                            c.setPadding(6);
                            c.setPaddingBottom( 10 );
                            igT.addCell(c);
                        }

                        ig = igL.get(i);

                        // ROW 1 - the question
                        c = new PdfPCell( new Phrase( ig.getQuestion(), getFontSmall() ) );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setColspan( 15 );
                        c.setPadding(2);
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);

                        // Row 2 - Color Dots
                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement(redDot);
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( redYellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( yellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( yellowGreenDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( greenDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        // ROW 3 - numbers
                        c = igT.getDefaultCell();
                        c.setHorizontalAlignment( Element.ALIGN_CENTER);
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setPadding( 0 );

                        igT.addCell(ep);
                        igT.addCell( new Phrase( "1", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "2", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "3", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "4", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "5", getFontSmall() ) );
                        igT.addCell(ep);

                        c = igT.getDefaultCell();
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setPadding( 2 );
                        c.setPaddingBottom( 10 );


                        // row 4 - anchors
                        c = new PdfPCell( new Phrase( ig.getAnchorLow(), getFontSmall() ) );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 4 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( ig.getAnchorMed(), getFontSmall() ) );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 5 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( ig.getAnchorHi(), getFontSmall() ) );
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 4 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);

                        iqs++;
                    }

                    if( iqs >= 0 )
                    {
                        c = new PdfPCell();
                        c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                        //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                        //c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                        //c.setBorderWidth( 0.5f );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setPadding( 10 );
                        c.addElement( igT );
                        t.addCell( c );
                    }

                }

                // else, with description
                else
                {
                    String d = reportData.getCompetencyDescription( tes ); // ScoreFormatUtils.getDescripFromTextParam( tes.getTextParam1() );
                    c = new PdfPCell( new Phrase( d == null ? "" : d, getFontSmall() ) );
                    c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( 0.5f );
                    c.setPadding( 6 );
                    t.addCell( c );
                }
                
                useGrayBg = !useGrayBg;
            } // each competency

            currentYLevel = addTableToDocument( y, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addAnyCompetenciesInfo() titleKey=" + titleKey );

            throw new STException( e );
        }
    }


    protected void addCalculationSection(boolean newPage) throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseTmLdrReportTemplate.addCalculationSection START" );
            
            //if( reportData.getReport().getIncludeScoreCalculationInfo()!=1 )
            //    return;
            
            if( reportData.getOrg().getIncludeScoreCalcInfoInReports()!=1 )
                return;

            boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(reportData.getOrg(), reportData.getTestEvent()) && reportData.getTestEvent().getOverallTestEventScore()!=null; //  && reportData.getTestEvent().getOverallTestEventScore().getRawScore()>=0;
            
            
            TestEvent te = reportData.te;
            SimJ simJ = te.getSimXmlObj();
            
            if( simJ == null )
            {
                ReportUtils ru = new ReportUtils();                
                ru.loadTestEventSimXmlObject(te);
                simJ = te.getSimXmlObj();
            }
            
            String keyStub = "";
            
            if( reportData.getTestEvent().getScoreFormatType().equals( ScoreFormatType.NUMERIC_0_TO_3) )
                keyStub = ".zeroto3";
            
            else if( reportData.getTestEvent().getScoreFormatType().equals(ScoreFormatType.NUMERIC_1_TO_10) )
                keyStub = ".oneto10";
            
            else if( reportData.getTestEvent().getScoreFormatType().equals( ScoreFormatType.NUMERIC_1_TO_5) )
                keyStub = ".oneto5";
            
            ProfileUtils.applyProfileToSimXmlObj( te );
            
            // addNewPage();
            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 1f, 1f, 1f, 1f, 1f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection(t );
                                  
            c = t.getDefaultCell();
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Competency"), this.fontBold ) );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Score"), this.fontBold ) );
            c.setPadding( 1 );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Distribution"), this.fontBold ) );
            c.setPadding( 1 );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( lmsg( "g.ValueUsedInCalc"), this.fontBold ) );
            c.setPadding( 1 );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( lmsg( "g.WeightPct"), this.fontBold ) );
            c.setPadding( 1 );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);
            
            SimJ.Simcompetency simCompetency;
            
            String tempStr;
            float scrUsed;
            
            float totalWeights = 0;
            
            float weightPct = 0;
            float totalComps = 0;
            
            float score = 0;
            
            SimCompetencyClass scc;
            
            for( TestEventScore tes : te.getTestEventScoreList() )
            {
                if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY )  )
                    continue;
                
                if( tes.getWeight()<= 0 && !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;
                
                simCompetency = getSimCompetencyForTes( tes, simJ );

                if( simCompetency == null )
                    continue;
                
                // If competency set to include in overall only if below X and score is above X, skip it.
                if( simCompetency.getIncludeinoverallifbelow() > 0 && tes.getRawScore() >= simCompetency.getIncludeinoverallifbelow() )
                    continue;

                scc = SimCompetencyClass.getValue( simCompetency.getClassid() );
                
                // if have non-task type of competency and not supposed to include in overall, skip it.
                if(  ( scc.getIsDirectCompetency() || scc.getIsAggregate() || scc.getIsCombo() ) && simJ.getIncludecompetenciesoverall() != 1 )
                    continue;

                if(  scc.getIsInterest() && simJ.getIncludeinterestoverall() != 1 )
                    continue;

                if(  scc.getIsExperience() && simJ.getIncludeexperienceoverall() != 1 )
                    continue;

                if(  scc.getIsBiodata() && simJ.getIncludebiodataoverall() != 1 )
                    continue;
                
                totalWeights += tes.getWeight();
                
                totalComps++;
            }
            
            if( totalComps <= 0 )
            {
                LogService.logIt( "BaseTmLdrReportTemplate.addCalculationSection No competencies found in TestEvent. returning. " + te.toString() );
                return;
            }
            
            StringBuilder sb = new StringBuilder();       
            
            
            for( TestEventScore tes : te.getTestEventScoreList() )
            {
                //LogService.logIt( "BaseTmLdrReportTemplate.addCalculationSection Competency: " + tes.getName() + ", Tyep=" + tes.getTestEventScoreTypeId() );                
                                
                if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                    continue;
                
                if( tes.getWeight()<= 0 && !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;
                                
                simCompetency = getSimCompetencyForTes( tes, simJ );

                //LogService.logIt( "BaseTmLdrReportTemplate.addCalculationSection Competency: " + tes.getName() + ", simcompetency=" + (simCompetency==null ? "null" : "found simCompetency.getIncludeinoverallifbelow()=" + simCompetency.getIncludeinoverallifbelow() ) );                

                if( simCompetency == null )
                    continue;

                
                // If competency set to include in overall only if below X and score is above X, skip it.
                if( simCompetency.getIncludeinoverallifbelow() > 0 && tes.getRawScore() >= simCompetency.getIncludeinoverallifbelow() )
                    continue;

                scc = SimCompetencyClass.getValue( simCompetency.getClassid() );
                
                // If we have a task-type of competency, and not supposed to include tasks in the overall score, don't include it.
                if(  scc.getIsTask() ) //  && simJ.getIncludetasksoverall() != 1 )
                    continue;

                // if have non-task type of competency and not supposed to include in overall, skip it.
                if(  ( scc.getIsDirectCompetency() || scc.getIsAggregate() || scc.getIsCombo() ) && simJ.getIncludecompetenciesoverall() != 1 )
                    continue;

                if(  scc.getIsInterest() && simJ.getIncludeinterestoverall() != 1 )
                    continue;

                if(  scc.getIsExperience() && simJ.getIncludeexperienceoverall() != 1 )
                    continue;

                if(  scc.getIsBiodata() && simJ.getIncludebiodataoverall() != 1 )
                    continue;
                                
                weightPct = totalWeights>0 ? 100*tes.getWeight()/totalWeights  : 100f/totalComps;
                
                c = new PdfPCell( new Phrase( reportData.getCompetencyName(tes) , this.font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);
                
                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 4 ), font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);
                
                simCompetency = getSimCompetencyForTes( tes, simJ );
                
                if( simCompetency == null || ( CategoryDistType.getValue( simCompetency.getCategorydisttype() ).getLinear() && simCompetency.getUsecategforoverall()!=1 ) )
                {
                    tempStr = lmsg( "g.Linear" );
                    scrUsed = tes.getScore();
                }

                //else if( ( CategoryDistType.getValue( simCompetency.getCategorydisttype() ).getLinear() && simCompetency.getUsecategforoverall()==1 ) )
                //{
                //    tempStr = lmsg( "g.ColorCategory" );
                //    scrUsed = tes.getScore();
                //}

                else
                {
                    tempStr = lmsg( "g.ColorCategory" );
                    
                    int catId = ScoreUtils.getScoreCategoryTypeId( simCompetency, tes.getScore(), ScoreColorSchemeType.getType( simJ.getScorecolorscheme() ));

                    scrUsed = ScoreCategoryType.getValue( catId ).getNumericEquivScore( simJ.getScoreformat() );                    
                }
                
                c = new PdfPCell( new Phrase( tempStr, font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scrUsed, 4 ), font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), weightPct, 4 ), font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);
                
                sb.append( "TmLdrReport.calcScores() Competency: " + tes.getName() + ", score=" + tes.getScore() + ", weight=" + tes.getWeight() + " scrUsed=" + scrUsed + " scrUsed*Weight=" + scrUsed*tes.getWeight() + ", score*weight=" + tes.getScore()*tes.getWeight() + "\n" );
                
                score += scrUsed*( totalWeights > 0 ? tes.getWeight() : 1f );
            }
            
            if( score <= 0 )
            {
                LogService.logIt( "BaseTmLdrReportTemplate.addCalculationSection No Scores found in TestEvent. returning. " + te.toString() );
                return;
            }
            
            // LogService.logIt( "BaseTmLdrReportTemplate.addCalculationSection \n" + sb.toString() + " TotalWeights: " + totalWeights + ", totalWeights*Scores=" + score + " \n" );
            
            
            score = score / (totalWeights > 0 ? totalWeights : totalComps );

            c = new PdfPCell( new Phrase( lmsg( "g.WeightedAverageC") , font ) );
            c.setColspan(4);
            c.setPadding( 1 );
            c.setBorder( Rectangle.TOP );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), score, 4 ) , font ) );
            c.setColspan(1);
            c.setPadding( 1 );
            c.setBorder( Rectangle.TOP );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);
            
            OverallScaledScoreCalcType overallScoreCalcType = OverallScaledScoreCalcType.getValue( simJ.getOverallscaledscorecalctype() );

            // if normall NCE but forcing Raw.
            if( useRawOverallScore && ( overallScoreCalcType.equals( OverallScaledScoreCalcType.NCE ) || overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) ) )
                overallScoreCalcType = OverallScaledScoreCalcType.RAW;
            
            float mean = 0;
            float std = 0;
            
            if( overallScoreCalcType.equals( OverallScaledScoreCalcType.NCE ) || overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) )
            {
                mean = simJ.getMean();
                std = simJ.getStddeviation();

                if( std<=0 )
                {
                    float[] stdvals = EventFacade.getInstance().getRawScoreStatisticsForProductId( te.getProductId() );

                    if( stdvals[0] <= 10 )
                    {
                        // throw new Exception( "Cannot convert score to z-score. No values for mean and standard deviation in sim or in database. Hits found=" + stdvals[0] );
                        LogService.logIt( "BaseTmLdrReportTemplate.addCalculationSection()  Cannot convert score to z-score. No values for mean and standard deviation in sim or in database. Hits found=" + stdvals[0] );
                    }

                    else
                    {
                        mean = stdvals[1];
                        std = stdvals[2];
                    }
                }            


                if( std > 0 )
                {
                    c = new PdfPCell( new Phrase( lmsg( "g.MeanUsedC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), mean, 4 ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( lmsg( "g.StdUsedC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), std, 4 ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);


                    float zScore = (score - mean)/std;

                    c = new PdfPCell( new Phrase( lmsg( "g.StdZScrC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), zScore, 4 ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    float nceScore = zScore*21.06f + 50f;

                    ScoreFormatType sft = ScoreFormatType.getValue( simJ.getScoreformat() );

                    if( nceScore < sft.getMinScoreToGiveTestTaker())
                        nceScore = sft.getMinScoreToGiveTestTaker();
                    else if( nceScore > sft.getMaxScoreToGiveTestTaker())
                        nceScore = sft.getMaxScoreToGiveTestTaker();

                    c = new PdfPCell( new Phrase( lmsg( "g.FinalNCEScrC" + keyStub ) , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), nceScore, 4 ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);
                }            
            }
            
            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.LOOKUP )  )
            {
                    c = new PdfPCell( new Phrase( lmsg( "g.FinalLookupScrC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), te.getOverallScore(), reportData.getTestEvent().getScorePrecisionDigits() ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);
                
            }            

            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.RAW )  )
            {
                    c = new PdfPCell( new Phrase( lmsg( "g.FinalOverallScrC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(),useRawOverallScore ? te.getOverallTestEventScore().getOverallRawScoreToShow() : te.getOverallScore() , reportData.getTestEvent().getScorePrecisionDigits() ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);
                
            }            

            
            
            String scoreCalcKey = "g.ScoreCalculationExplanation" + keyStub;
            
            String[] params = null;
            
            if( overallScoreCalcType.equals( OverallScaledScoreCalcType.NCE )  )
                scoreCalcKey = "g.ScoreCalculationExplanation" + keyStub;
            
            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) )
            {
                scoreCalcKey = "g.ScoreCalculationExplanationNormalTrans" + keyStub;
                
                params = new String[] {Float.toString(mean), Float.toString(std)};
            }
            
            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.RAW ) )
                 scoreCalcKey = "g.ScoreCalculationExplanationRaw" + keyStub;
            
            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.LOOKUP ) )
            {
                scoreCalcKey = "g.ScoreCalculationExplanationLookup" + keyStub;
            }
            
            
            if( newPage )
                addNewPage();
            
            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.ScoreCalculation" ), lmsg( scoreCalcKey, params ) );
            
            currentYLevel = addTableToDocument(y, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addPreparationNotesSection()" );

            throw new STException( e );
        }
        
    }
    

    public void addWritingSampleInfo() throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseCT2ReportTemplate.addWritingSampleInfo() " + reportData.getReport().getIncludeWritingSampleInfo() );

            previousYLevel =  currentYLevel;

            if( reportData.getReport().getIncludeWritingSampleInfo()==0 )
                return;

            java.util.List<TextAndTitle> ttl = ScoreFormatUtils.getNonCompTextListTable( reportData.getTestEvent(), NonCompetencyItemType.WRITING_SAMPLE );

            if( ttl.isEmpty() )
                return;

            float y;
            
            // float y = addTitle( previousYLevel, lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            // t.setSplitLate( false );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = new PdfPCell(new Phrase( lmsg( "g.WritingSampleQuestion" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true));
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( lmsg( "g.Response" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false));
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);
            
            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.WritingSampleQuestion" ) , fontLmWhite) );
            //t.addCell( new Phrase( lmsg( "g.Response" ) , fontLmWhite) );

            // c.setBackgroundColor( BaseColor.WHITE );

            // Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;
            Paragraph textPar;

            String misSpells;

            String theText;
            String transText;
            String summary;
            

            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            boolean useGrayBg = true;            
            
            
            // For each competency
            for( TextAndTitle tt : ttl )
            {
                useGrayBg = !useGrayBg;
                
                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                theText = tt.getTitle();
                if( StringUtils.getHasHtml(theText) )
                    theText = StringUtils.convertHtml2PlainText(theText, true );

                c = new PdfPCell( new Phrase( theText, getFontSmall() ) );
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

                theText = tt.getText();
                if( StringUtils.getHasHtml(theText) )
                    theText = StringUtils.convertHtml2PlainText(theText, true );

                
                misSpells = tt.getString1();
                transText = tt.getString3();
                summary = tt.getString4();

                textPar = new Paragraph();
                if( summary !=null && !summary.isBlank() )
                {
                    textPar.add( new Chunk( lmsg("g.SummaryAI") + ": ", getFontSmallBold() ));
                    textPar.add( new Chunk( summary + "\n\n", getFontSmall()));
                    textPar.add( new Chunk( lmsg("g.FromCandidate") + ": ", getFontSmallBold() ) );
                }                

                if( misSpells!=null && !misSpells.isEmpty() )
                {
                    theText += "\n\n[" + lmsg( "g.MisSpelledWordsC" ) + " " + misSpells + "]";
                }

                if( transText!=null && !transText.isEmpty() )
                {
                    theText += "\n\n[" + lmsg( "g.ReverseTranslatedC" ) + " " + transText + "]";
                }

                textPar.add( new Chunk( theText, getFontSmall()) );
                
                c = new PdfPCell( textPar );
                // c = new PdfPCell( new Phrase( theText, getFontSmall() ) );
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

            } // each writing sample

            float titleHeight = getTitleHeight(lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ));

            // if need a new page just for title, do so.
            if( previousYLevel>0 )
            {
                float ulY = previousYLevel - PAD - titleHeight;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    currentYLevel = pageHeight - PAD -  headerHgt;
                    previousYLevel = currentYLevel;
                }
            }
                        
            float t2Height = t.calculateHeights();
            
            float ulY = previousYLevel - 2*PAD - t2Height - titleHeight;

            // both won't fit. Add a new page, then add title
            if( ulY < footerHgt + 3*PAD )
            {
                // see if title plus header plus first row is too much
                ulY = previousYLevel - 2*PAD - titleHeight - getHeaderPlusFirstRowHeight( t );
                
                if( ulY < footerHgt + 3*PAD )
                {   
                    document.newPage();
                    currentYLevel = pageHeight - PAD -  headerHgt;
                    previousYLevel = currentYLevel;
                }
                
                // otherwise we should be OK.
            }
            
            // add title
            y = addTitle( previousYLevel, lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ) );
                        
            currentYLevel = addTableToDocument(y, t );

           //  LogService.logIt( "BaseCT2ReportTemplate.addWritingSampleInfo()" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addWritingSampleInfo()" );

            throw new STException( e );
        }
    }

    
    public float getTitleHeight( String title, String subtitle ) throws Exception
    {
        try
        {
            Font fnt =   getHeaderFontXLarge();
            // Change getFont()
            float leading = fnt.getSize();

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            // float y = previousYLevel - 6*PAD - getFont().getSize();
            float h = ITextUtils.getDirectTextHeight( pdfWriter, title, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

            h += PAD;
                        
            // No subtitle
            if( subtitle==null || subtitle.isEmpty() )
                return h;

            // Change getFont()
            fnt =  getFont();
            leading = fnt.getSize();

            h += ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);
            h += PAD;
            
            return h;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTitleAndSubtitle()" );
            throw new STException( e );
        }
    }
    
    

    public float getHeaderPlusFirstRowHeight( PdfPTable t ) throws Exception
    {
        float tableHeight = 0;
        try
        {
            tableHeight = t.calculateHeights(); //  + 500;
            float tableHeaderHeight = t.getHeaderHeight();

            int rowCount = t.getRows().size(); //  - t.getHeaderRows() - t.getFooterRows();

            float[] rowHgts = new float[rowCount];

            for( int i=0; i<rowCount; i++ )
            {
                rowHgts[i]=t.getRowHeight(i);
            }
                        
            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;
            
            return tableHeaderHeight + firstRowHgt;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTableToDocument() testEventId=" + reportData.te.getTestEventId() );
            return tableHeight;
        }
    }
    
    
    

    /*
    public void addWritingSampleInfo() throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseTmLdrReportTemplate.addWritingSampleInfo() " + reportData.getReport().getIncludeWritingSampleInfo() );

            previousYLevel =  currentYLevel;

            if( reportData.getReport().getIncludeWritingSampleInfo()==0 )
                return;

            java.util.List<TextAndTitle> ttl = ScoreFormatUtils.getNonCompTextListTable( reportData.getTestEvent(), NonCompetencyItemType.WRITING_SAMPLE );

            if( ttl.isEmpty() )
                return;

            float y = addTitle( previousYLevel, lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            // t.setSplitLate( false );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );


            c = new PdfPCell(new Phrase( lmsg( "g.WritingSampleQuestion" ) , fontLmWhite));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            t.addCell(c);
            
            c = new PdfPCell(new Phrase( lmsg( "g.Response" ) , fontLmWhite ));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            t.addCell(c);
            

            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.WritingSampleQuestion" ) , fontLmWhite) );
            //t.addCell( new Phrase( lmsg( "g.Response" ) , fontLmWhite) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                t.addCell( c );

            } // each writing sample

            currentYLevel = addTableToDocument( y, t );

            // LogService.logIt( "BaseTmLdrReportTemplate.addWritingSampleInfo()" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addWritingSampleInfo()" );

            throw new STException( e );
        }
    }
    */

    /*
    public void addMinQualsApplicantDataInfo() throws Exception
    {
        try
        {
            LogService.logIt(  "BaseTmLdrReportTemplate.addMinQualsApplicantDataInfo() START" );

            previousYLevel =  currentYLevel;

            if( !reportData.includeApplicantData() && !reportData.includeMinQuals() )
                return;

            java.util.List<TextAndTitle> ttl = new ArrayList<>();

            if( reportData.includeApplicantData() )
                ttl.addAll( ScoreFormatUtils.getNonCompTextListTable(reportData.getTestEvent(), NonCompetencyItemType.APPLICANT_INFO ) );

            if( reportData.includeMinQuals() )
                ttl.addAll( ScoreFormatUtils.getNonCompTextListTable( reportData.getTestEvent(), NonCompetencyItemType.MIN_QUALS ) );

            if( ttl.isEmpty() )
                return;

            float y = addTitle( previousYLevel, lmsg( "g.AppDataAndMinQualsTitle" ), lmsg( "g.AppDataAndMinQualsSubtitle" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            //t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingBottom( 4 );
            c.setBackgroundColor(  ct2Colors.hraBlue );
            c.setBorderColor(  ct2Colors.scoreBoxBorderColor );

            // Add header row.
            t.addCell( new Phrase( lmsg( "g.Item" ) , fontLmWhite) );
            t.addCell( new Phrase( "" , fontLmWhite ) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor(  ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

            } // each writing sample

            currentYLevel = addTableToDocument( y, t );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addMinQualsApplicantDataInfo()" );

            throw new STException( e );
        }
    }
    */






    public void addNotesSection() throws Exception
    {
        addTitle( 0 , lmsg("g.Notes"), lmsg( "g.NotesSubtitle" ) );
    }




    public void addCoverPageV2(boolean includeDescriptiveText) throws Exception
    {
        try
        {
            //if( reportData.getReportRuleAsBoolean( "legacycoverpage" ) )
            //{
            //    addCoverPage();
            //    return;
            //}

            if( reportData.getReportRuleAsBoolean( "covrdescripoff" ) )
                includeDescriptiveText = false;

            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 50;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();
            java.util.List<Chunk> cl = new ArrayList<>();

            boolean coverInfoOk = !reportData.getReportRuleAsBoolean( "omitcoverinfopdf" );

            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";

            String reportCompanyAdminName = reportData==null ? null : reportData.getReportCompanyAdminName();

            if( reportData.getTestKey().getAuthUser() != null && (reportCompanyAdminName==null || reportCompanyAdminName.isEmpty())  )
                reportCompanyAdminName = reportData.getTestKey().getAuthUser().getFullname();

            else if( StringUtils.isCurlyBracketed( reportCompanyAdminName ) )
                reportCompanyAdminName = "                        ";


            if( reportCompanyAdminName != null && reportCompanyAdminName.indexOf( "AUTOGEN" )>=0 )
                reportCompanyAdminName = null;

            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            if( !includeCompanyInfo )
            {
                reportCompanyName = "";
                custLogo = null;
            }

            boolean compNameForAdmin = reportData.getReportRuleAsBoolean("compnameforprep") && includeCompanyInfo;

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage()  CCC.1 includeCompanyInfo=" + includeCompanyInfo + ", reportCompanyName=" + reportCompanyName + ", compNameForAdmin=" + compNameForAdmin );

            if( compNameForAdmin && !reportData.hasCustLogo() )
                includeCompanyInfo = false;


            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );

            boolean includePreparedFor = includeCompanyInfo && reportCompanyAdminName!=null && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            //boolean sports = includeCompanyInfo && reportData.getReportRuleAsBoolean( "sportstest" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            //if( sports || reportData.getReportRuleAsBoolean( "legacycoverpage" ) )
            //{
            //    addCoverPage();
            //    return;
            //}

            Image hraCover = this.getHraCoverImage();

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPageV2() START page dims=" + pageWidth + "," + pageHeight + ", imageDims=" + hraCover.getWidth() + "," + hraCover.getHeight() );

            hraCover.scalePercent( 100*pageHeight/hraCover.getHeight());

            ITextUtils.addDirectImage( pdfWriter, hraCover, pageWidth-hraCover.getScaledWidth() + 1, 0, true );

            boolean clientLogoInHeader = reportData.getReportRuleAsBoolean( "clientlogopdfhdr" ) && (includeCompanyInfo || compNameForAdmin) && reportData.hasCustLogo();
            if( clientLogoInHeader )
            {
                //float lwid = custLogo.getScaledWidth();
                ITextUtils.addDirectImage( pdfWriter, custLogo, 2*CT2_MARGIN, y, false );
            }
            else if( !reportData.getReportRuleAsBoolean( "hidehralogoinreports" ))
                ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), 2*CT2_MARGIN, y, false );

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage()  CCC.2 includePreparedFor=" + includePreparedFor + ", clientLogoInHeader=" + clientLogoInHeader  +", reportData.hasCustLogo()=" + reportData.hasCustLogo() + ", custLogo!=null " + (custLogo!=null));

            String reportTitle = reportData.getReportName();

            BaseFont baseTitleFont = baseFontCalibriBold;

            int titleFontHeight = 56;

            //if( 1==2 )
            //    reportTitle = "Test Results and Interview Guilde";

            if( reportTitle.length()>40)
                titleFontHeight = 36;

            else if( reportTitle.length()>32)
                titleFontHeight = 44;

            else if( reportTitle.length()>24)
                titleFontHeight = 48;

            else if( reportTitle.length()>16)
                titleFontHeight = 52;

            Font titleFont = new Font(baseTitleFont, titleFontHeight );
            titleFont.setColor(ct2Colors.hraBlue);
            String reportSubtitle = reportData.getR2Use().getStrParam5()!=null && !reportData.getR2Use().getStrParam5().isBlank() ? reportData.getR2Use().getStrParam5() : null;

            //if( 1==2 )
            //    reportSubtitle="This is a dummy subtitle for report";

            PdfPTable t = new PdfPTable( 2 );

            t.setWidths(reportData.getIsLTR() ?  new float[] {3,9}: new float[] {9,3} );
            t.setTotalWidth( (pageWidth-2*CT2_MARGIN)*0.8f);
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setVerticalAlignment(Element.ALIGN_TOP);
            setRunDirection(c);


            c = new PdfPCell( new Phrase( reportTitle, titleFont ) );
            c.setColspan(2);
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( 0 );
            c.setVerticalAlignment(Element.ALIGN_TOP);
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPaddingBottom(3);
            setRunDirection(c);
            t.addCell(c);

            if( reportSubtitle !=null && !reportSubtitle.isBlank() )
            {
                Font subtitleFont = new Font(baseTitleFont, 12 );
                subtitleFont.setColor(ct2Colors.hraBlue);

                c = new PdfPCell( new Phrase( reportSubtitle, subtitleFont ) );
                c.setColspan(2);
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( 0 );
                c.setVerticalAlignment(Element.ALIGN_TOP);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPaddingBottom(3);
                setRunDirection(c);
                t.addCell(c);
            }

            // Blue Bar
            c = new PdfPCell( new Phrase( "\n\n\n\n", font ) );
            c.setColspan(2);
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( 0 );
            setRunDirection(c);
            c.setCellEvent( new CoverBlueBarCellEvent() );
            t.addCell(c);

            String testTakerTitle = lmsg( devel ? "g.PreparedForC" : "g.CandidateC" );

            //if( sports )
            //    testTakerTitle = lmsg( "g.AthleteC" );

            Font cpFont = this.fontXLarge;

            // Name stuff
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setVerticalAlignment(Element.ALIGN_TOP);
            setRunDirection(c);

            if( coverInfoOk )
            {
                t.addCell(new Phrase( testTakerTitle , cpFont ) );
                t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

                t.addCell(new Phrase( lmsg( "g.AssessmentC" ) , cpFont ) );
                t.addCell(new Phrase( reportData.getSimName(), cpFont ) );

                if( includeDates )
                {
                    t.addCell(new Phrase( lmsg( "g.CompletedC" ) , cpFont ) );
                    t.addCell(new Phrase( reportData.getSimCompleteDateFormatted(), cpFont ) );
                }
            }

            String nameForPrep = compNameForAdmin  ? reportCompanyName : reportCompanyAdminName;

            if( includeCompanyInfo && includePreparedFor && nameForPrep!=null && !nameForPrep.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage() Adding prepared " + lmsg( "g.PreparedForC" ) + ", includeCompanyInfo=" + includeCompanyInfo + ", devel=" + devel );

                if( coverInfoOk && !devel )
                {
                    t.addCell(new Phrase( lmsg( "g.PreparedForC" ) + " " , cpFont ) );
                    t.addCell(new Phrase( nameForPrep, cpFont ) );
                }
            }

            float lowerTableAdj = 0;
            float lineHeight = 10;

            if( includeCompanyInfo )
            {
                if( coverInfoOk || (reportData.hasCustLogo() && custLogo!=null && !clientLogoInHeader) )
                    t.addCell(new Phrase( coverInfoOk && (devel) ? lmsg( "g.SponsoredByC" ) : "" , cpFont ) );

                if( reportData.hasCustLogo() && custLogo!=null && !clientLogoInHeader )
                {
                    float imgSclW=100;
                    float imgSclH = 100;

                    if( custLogo.getWidth() > MAX_CUSTLOGO_W_V2 )
                        imgSclW = 100 * MAX_CUSTLOGO_W_V2/custLogo.getWidth();

                    if( custLogo.getHeight() > MAX_CUSTLOGO_H_V2 )
                        imgSclH = 100 * MAX_CUSTLOGO_H_V2/custLogo.getHeight();

                    imgSclW = Math.min( imgSclW, imgSclH );

                    if( imgSclW < 100 )
                        custLogo.scalePercent( imgSclW );

                    c = new PdfPCell( custLogo );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment(Element.ALIGN_LEFT );
                    c.setPadding( 0 );
                    c.setPaddingTop( 12 );
                    setRunDirection(c);
                    t.addCell( c );

                    lowerTableAdj += custLogo.getScaledHeight() + 12 - lineHeight;
                }

                else if( coverInfoOk )
                    t.addCell( new Phrase( reportCompanyName, fontXLarge ) );
            }

            float tableH = t.calculateHeights(); //  + 500;
            float tableY = pageHeight-175;


            // float tableW = t.getTotalWidth();

            float tableX = 2*CT2_MARGIN; // (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            y = tableY - tableH;

            List<String> whatsContained = new ArrayList<>();

            String includedCustom = reportData.getReportRuleAsString("includedinreportall" );  //
            if( includedCustom!=null && !includedCustom.isBlank() )
            {
                for( String ss : includedCustom.split("~") )
                {
                    if( ss.isBlank() )
                        continue;
                    whatsContained.add( ss.trim() );
                }
            }

            else
            {
                includedCustom = reportData.getReportRuleAsString("includedinreporttop" );  //
                if( includedCustom!=null && !includedCustom.isBlank() )
                {
                    for( String ss : includedCustom.split("~") )
                    {
                        if( ss.isBlank() )
                            continue;
                        whatsContained.add( ss.trim() );
                    }
                }

                if( includesOverall() )
                    whatsContained.add( lmsg("g.Cvr2Overall") );

                if( includesCompSummary() )
                    whatsContained.add( lmsg("g.Cvr2CompSummary") );

                if( includesComparison() )
                    whatsContained.add( lmsg("g.Cvr2Percentile") );

                if( includesCompetencyDetail() )
                {
                    if( !devel )
                        whatsContained.add( lmsg( reportData.includeInterview() ? "g.Cvr2DetailInterview" : "g.Cvr2Detail") );
                    else
                        whatsContained.add( lmsg( "g.Cvr2DetailDevel") );
                }
            }

            if( includesAvUploads() )
                whatsContained.add( lmsg("g.Cvr2AvUploads") );

            includedCustom = reportData.getReportRuleAsString("includedinreportbot" );  //
            if( includedCustom!=null && !includedCustom.isBlank() )
            {
                for( String ss : includedCustom.split("~") )
                {
                    if( ss.isBlank() )
                        continue;
                    whatsContained.add( ss.trim() );
                }
            }

            if( whatsContained.size()>3 )
                lowerTableAdj += (whatsContained.size()-3)*lineHeight;


            int returnCt = 0;

            if( includeDescriptiveText )
            {
                if( coverDescrip!= null &&  !coverDescrip.isEmpty() )
                {}

                else if( reportData.getReport() != null && reportData.getReport().getTextParam1()!=null && !reportData.getReport().getTextParam1().isEmpty() )
                {
                    coverDescrip = reportData.getReport().getTextParam1();
                    coverDescrip = coverDescrip;
                }

                else
                {
                    String coverDetailKey = reportData.getReport()!=null && reportData.getReport().getStrParam1()!=null && !reportData.getReport().getStrParam1().isEmpty() ? reportData.getReport().getStrParam1() : "g.CT2CoverDescrip";
                    coverDescrip = lmsg( coverDetailKey, new String[] {reportData.getSimName()} );
                }

                if( coverDescrip!=null )
                {
                    returnCt++;

                    int idx = coverDescrip.indexOf("\n" );
                    while( idx>=0 )
                    {
                        returnCt++;
                        idx = coverDescrip.indexOf("\n" , idx+1);
                    }
                }

                // cuont \n's in coverDescrip
            }

            Paragraph descripPar = null;

            if( includeDescriptiveText )
            {
                Font f = fontLm;
                Font fb = fontLmBold;

                if( coverDescrip!=null && coverDescrip.length()>=900 )
                {
                    f = fontSmall;
                    fb = fontSmallBold;
                }
                else if( coverDescrip!=null && coverDescrip.length()>=600 )
                {
                    f = font;
                    fb = fontBold;
                }

                if( coverDescrip!=null && coverDescrip.length()>=800 )
                    lowerTableAdj += 2*lineHeight;


                descripPar = new Paragraph();
                Chunk chk = new Chunk( lmsg("g.Cvr2ImportantNote") + ": ", fb );
                descripPar.add(chk);
                chk =  new Chunk( coverDescrip, f );
                descripPar.add(chk);
            }

            boolean addTable = false;
            t = new PdfPTable( 2 );
            t.setWidths( new float[] {0.1f, 4f} );
            t.setTotalWidth( (pageWidth-2*CT2_MARGIN)*0.7f);
            t.setLockedWidth( true );
            setRunDirection(t);


            if( !whatsContained.isEmpty() )
            {
                addTable = true;

                c = new PdfPCell( new Phrase( "\n\n\n" , this.getFontXLargeBold()) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding(0);
                c.setColspan(2);
                c.setCellEvent( new CoverIncludedBarCellEvent(lmsg("g.Cvr2WhatsIncluded") , this.getFontXLargeBold()));
                setRunDirection(c);
                t.addCell( c );

                for( String inc : whatsContained )
                {
                    c = new PdfPCell( new Phrase( "\u2022" , cpFont ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment(Element.ALIGN_TOP);
                    c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c.setPadding(0 );
                    c.setPaddingRight(2);
                    setRunDirection(c);
                    t.addCell( c );

                    c = new PdfPCell( new Phrase( inc , cpFont ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment(Element.ALIGN_TOP);
                    c.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c.setPadding(1 );
                    c.setPaddingLeft(2);
                    setRunDirection(c);
                    t.addCell( c );
                }
            }

            if( includeDescriptiveText && descripPar!=null )
            {
                addTable = true;

                c = new PdfPCell( descripPar );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                c.setPaddingTop(whatsContained.isEmpty() ? 40 : 12);
                setRunDirection(c);
                t.addCell( c );
            }

            if( addTable )
            {
                // tableW = t.getTotalWidth();
                tableH = t.calculateHeights(); //  + 500;

                // tableY = y - tableH;
                y -= Math.max( 20, (120 - lowerTableAdj));

                t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            }

            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);


            c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFont() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            // tableH = t.calculateHeights(); //  + 500;

            tableY = 20;
            float tableW = t.getTotalWidth();
            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );


        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addCoverPageV2()" );
        }
    }
    
    
    public boolean includesOverall()
    {
        if( reportData==null )
            return false;

        if( reportData.getR2Use().getIncludeOverallScore()==0 && reportData.getR2Use().getIncludeOverallCategory()==0 )
            return false;

        if( reportData.getReportRuleAsBoolean("ovroff") )
            return false;

        return !reportData.getReportRuleAsBoolean( "ovrdetailedoff" );
    }


    public boolean includesCompSummary()
    {
        if( reportData==null )
            return false;

        // If no info to present.
        if( reportData.getReportRuleAsBoolean("cmptysumoff") ||
            reportData.getReport().getIncludeCompetencyScores()!=1 ||
            ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
              reportData.getReport().getIncludeSubcategoryNumeric()!=1 &&
              reportData.getReport().getIncludeCompetencyColorScores()!=1 )  )
              return false;


        java.util.List<TestEventScore> teslA = reportData.getTestEvent().getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId());
        if( teslA.isEmpty() )
            return false;

        for( TestEventScore tes : teslA )
        {
            if( tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDAVUPLOAD) || tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDIMAGEUPLOAD))
                continue;

            if( SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
            {
                return true;
            }
        }
        return false;
    }

    public boolean includesComparison()
    {
        if( reportData==null )
            return false;

        if( reportData.getReportRuleAsBoolean( "skipcomparisonsection" ) )
            return false;

        if( reportData.getReport().getIncludeNorms()==0 )
            return false;

        TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();

        if( otes==null )
            return false;

        if( !otes.getHasValidNorms() && !otes.getHasValidOverallZScoreNorm() )
        {
            return false;
        }

        return true;
    }

    public boolean includesCompetencyDetail()
    {
        if( reportData==null )
            return false;

        if( !reportData.includeCompetencyScores() )
            return false;

        if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
            return false;

        java.util.List<TestEventScore> teslA = reportData.getTestEvent().getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId());
        if( teslA.isEmpty() )
            return false;

        for( TestEventScore tes : teslA )
        {
            if( tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDAVUPLOAD) || tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDIMAGEUPLOAD))
                continue;

            if( SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
            {
                if( !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.getValueForSimCompetencyClass(tes.getSimCompetencyClass()).getSimCompetencyGroupTypeId() ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean includesAvUploads()
    {
        return false;
    }
    
    

    /*
    public void addCoverPage() throws Exception
    {
        try
        {
            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 20;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

            if( !reportData.getReportRuleAsBoolean( "hidehralogoinreports" ))
                ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), CT2_MARGIN, y, false );

            java.util.List<Chunk> cl = new ArrayList<>();

            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";

            String reportCompanyAdminName = reportData==null ? null : reportData.getReportCompanyAdminName();

            if( reportData.getTestKey().getAuthUser() != null && (reportCompanyAdminName==null || reportCompanyAdminName.isEmpty())  )
                reportCompanyAdminName = reportData.getTestKey().getAuthUser().getFullname();

            else if( StringUtils.isCurlyBracketed( reportCompanyAdminName ) )
                reportCompanyAdminName = "                        ";

            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" ); 

            cl.add( new Chunk( lmsg( "g.CandidateC" ), getFontXLarge() ) );
            cl.add( new Chunk( lmsg( "g.AssessmentC" ), getFontXLarge() ) );
            if( includeDates )
                cl.add( new Chunk( lmsg( "g.CompletedC" ), getFontXLarge() ) );

            if( reportCompanyAdminName!=null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
                cl.add( new Chunk( lmsg( "g.PreparedForC" ), getFontXLarge() ) );

            float titleWid = ITextUtils.getMaxChunkWidth( cl ) + 20;

            cl.clear();

            cl.add( new Chunk( reportData.getUserName(), getFontXLargeBold() ) );
            cl.add( new Chunk( reportData.getSimName(), getFontXLarge() ) );
            if( includeDates )
                cl.add( new Chunk( reportData.getSimCompleteDateFormatted(), getFontXLarge() ) );

            if( reportCompanyAdminName != null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                cl.add( new Chunk( reportCompanyAdminName, getFontXLarge() ) );

                if( (!reportData.hasCustLogo() || custLogo==null) && reportCompanyName != null && !reportCompanyName.isEmpty() )
                    cl.add( new Chunk( reportCompanyName, getFontXLarge() ) );
            }

            float infoWid = ITextUtils.getMaxChunkWidth( cl ) + 10;

            if( custLogo!=null && custLogo.getScaledWidth()>infoWid )
                infoWid = custLogo.getScaledWidth();

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( 2 );

            t.setTotalWidth( new float[] { titleWid+4, infoWid+14 } );
            t.setLockedWidth( true );

            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );

            setRunDirection(c);

            Font font = this.fontXLarge;

            t.addCell( new Phrase( lmsg( "g.CandidateC" ) , font ) );
            t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

            t.addCell( new Phrase( lmsg( "g.AssessmentC" ) , font ) );
            t.addCell( new Phrase( reportData.getSimName(), font ) );

            if( includeDates )
            {
                t.addCell( new Phrase( lmsg( "g.CompletedC" ) , font ) );
                t.addCell( new Phrase( reportData.getSimCompleteDateFormatted(), font ) );
            }

            if( reportCompanyAdminName!=null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                // LogService.logIt( "BaseTmLdrReportTemplate.addCoverPage() Adding prepared " + lmsg( "g.PreparedForC" ) );

                t.addCell( new Phrase( lmsg( "g.PreparedForC" ) + " " , font ) );
                t.addCell( new Phrase( reportCompanyAdminName, font ) );

                t.addCell( new Phrase( "" , font ) );

                if( reportData.hasCustLogo() && custLogo!=null )
                {
                    c = new PdfPCell( custLogo );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment(Element.ALIGN_LEFT );
                    c.setPadding( 0 );
                    c.setPaddingTop( 12 );
                    setRunDirection(c);
                    t.addCell( c );
                }

                else
                    t.addCell( new Phrase( reportCompanyName, fontXLarge ) );
            }

            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, getHraOrangeColor(), false );

            // Add the blue below
            ITextUtils.addDirectColorRect( pdfWriter, getHraBaseReportColor(), 0, 0, pageWidth, pageHeight/2, 0, 1, true );


            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            // c.setBorder( Rectangle.BOX );
            //c.setBorderWidth( 0.5f );
            //c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            //c.setBorderColor( BaseColor.DARK_GRAY );

            t.addCell( "\n\n\n\n\n\n\n\n\n" );

            c = new PdfPCell( new Phrase( lmsg( "g.TestResultsAndInterviewGuide" ) , getHeaderFontXXLargeWhite() ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            t.addCell( "\n\n\n\n\n\n\n\n\n" );

            String coverDescrip = lmsg( "g.TmLdrCoverDescrip", new String[] {reportData.getSimName()} );

            c = new PdfPCell( new Phrase( coverDescrip , fontLLWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection(c);

            t.addCell( c );

            t.addCell( "\n\n\n" );
            c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFontWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            tableH = t.calculateHeights(); //  + 500;

            tableY = pageHeight/2 - (pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseTmLdrReportTemplate.addCoverPage()" );
        }
    }
    */




    /*
    public float addTitle( float startY, String title, String subtitle) throws Exception
    {
            Font fnt =  getHeaderFontXLarge();

            if( startY > 0 )
            {
                float ulY = startY - 15* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }

            float y = startY>0 ? startY - fnt.getSize() - 4*PAD :  pageHeight - headerHgt - fnt.getSize() - 4*PAD;

            float x = CT2_MARGIN + PAD;

            ITextUtils.addDirectText( pdfWriter, title, x, y, Element.ALIGN_LEFT, fnt, false );

            return y;
    }
    */


    public float addTitle( float startY, String title, String subtitle ) throws Exception
    {
        try
        {
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
            ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, Element.ALIGN_LEFT, fnt, false);

            // No subtitle
            if( subtitle==null || subtitle.isEmpty() )
                return y;

            // Change getFont()
            fnt =  getFont();

            float leading = fnt.getSize();

            float spaceLeft = y - PAD - footerHgt;

            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, Element.ALIGN_LEFT, leading, fnt);

             y -=  PAD;//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();
            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, subtitle, rect, Element.ALIGN_LEFT, leading, fnt, false );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                Phrase p = new Phrase( subtitle, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() adding second column "  );

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
            LogService.logIt( e, "BaseTmLdrReportTemplate.addTitleAndSubtitle()" );

            throw new STException( e );
        }
    }



    public float addText( String text, Font fnt ) throws Exception
    {
        try
        {
            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, text, txtW, Element.ALIGN_LEFT, fnt.getSize(), fnt);

            if( currentYLevel - txtHght - footerHgt - PAD < 0 )
                addNewPage();

            previousYLevel =  currentYLevel;

            float y = currentYLevel;

            float leading = fnt.getSize();

            float spaceLeft = currentYLevel - leading - footerHgt;

            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, text, rect, Element.ALIGN_LEFT, leading, fnt, false );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                Phrase p = new Phrase( text, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    LogService.logIt( "BaseTmLdrReportTemplate.addText() adding second column "  );

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
            LogService.logIt( e, "BaseTmLdrReportTemplate.addTtext()" );

            throw new STException( e );
        }
    }


    public float addTableToDocument( float startY, PdfPTable t ) throws Exception
    {
            float ulY = startY - 2*PAD;  // 4* PAD;

            float tableHeight = t.calculateHeights(); //  + 500;
            float headerHeight = t.getHeaderHeight();

            int rowCount = t.getRows().size(); //  - t.getHeaderRows() - t.getFooterRows();

            float maxRowHeight=0;

            float[] rowHgts = new float[rowCount];

            for( int i=0; i<rowCount; i++ )
            {
                rowHgts[i]=t.getRowHeight(i);
                maxRowHeight = Math.max( maxRowHeight, rowHgts[i] );
                // LogService.logIt( "BaseTmLdrReportTemplate.addTableToDocument() row=" + i + ", rowHeight=" + rowHgts[i] );
            }

            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;

            float heightAvailNewPage = pageHeight - headerHgt - 3*PAD - footerHgt - 3*PAD - headerHeight;

            if( maxRowHeight >= heightAvailNewPage*0.5 )
                t.setSplitLate(false);

            // If first row doesn't fit on this page
            else if( firstRowHgt > ulY- footerHgt - 3*PAD - headerHeight ) // ulY < footerHgt + 8*PAD )
            {
                // LogService.logIt( "BaseTmLdrReportTemplate.addTableToDocument() adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
            }


            //if( maxRowHeight > usablePageHeight )
            //    t.setSplitLate(false);
            float tableXlft = CT2_MARGIN + CT2_BOX_EXTRAMARGIN;
            float tableXrgt = CT2_MARGIN + CT2_BOX_EXTRAMARGIN + t.getTotalWidth();

            Rectangle colDims = new Rectangle( tableXlft, footerHgt + 3*PAD, tableXrgt, ulY );
            // LogService.logIt( "BaseTmLdrReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

            float heightNoHeader = tableHeight - headerHeight;


            Object[] dta = calcTableHghtUsed( colDims.getTop() - colDims.getBottom() - headerHeight, 0, t.getHeaderRows(), rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
            int nextIndex = (Integer) dta[0];
            float heightUsedNoHeader = (Float) dta[1];
            float residual = (Float) dta[2];

            // LogService.logIt( "BaseTmLdrReportTemplate.addTableToDocument() tableHeight=" + t.calculateHeights() + ", headerHeight=" + headerHeight + ", maxRowHeight=" + maxRowHeight + ", heightAvailNewPage=" + heightAvailNewPage + ", initial heightUsedNoHeader=" + heightUsedNoHeader + ", residual=" + residual );


            ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

            // NOTE - this forces Composite mode (using ColumnText.addElement)
            ct.addElement( t );

            ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );
            // ct.setSimpleColumn( colDims1 );


            int status = ct.go();

            // int linesWritten = ct.getLinesWritten();

            // LogService.logIt( "BaseTmLdrReportTemplate.addTableToDocument() initial lines written. NO_MORE_COLUMN=" + ColumnText.NO_MORE_COLUMN + ", NO_MORE_TEXT=" + ColumnText.NO_MORE_TEXT  );

            int pages = 0;

            float heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

            float hgtUsedThisPage = 0;

            // If need to add any pages
            while( ColumnText.hasMoreText( status ) && heightNeededNoHeader>0 && pages<10 )
            {
                // Top of writable area
                ulY = pageHeight - headerHgt - 3*PAD;


                dta = calcTableHghtUsed( heightAvailNewPage, residual, nextIndex, rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
                nextIndex = (Integer) dta[0];
                hgtUsedThisPage = (Float) dta[1];
                residual = (Float) dta[2];

                heightUsedNoHeader += hgtUsedThisPage;

                heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

                // LogService.logIt( "BaseTmLdrReportTemplate.addTableToDocument() AFTER adding next page. hgtUsedThisPage=" + hgtUsedThisPage +  ", Total HeightNeededNoHeader=" + heightNeededNoHeader + ", Total HeightUsedNoHeader=" + heightUsedNoHeader  );

                colDims = new Rectangle( tableXlft, ulY - heightAvailNewPage , tableXrgt, ulY );

                document.newPage();

                ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );

                ct.setYLine( colDims.getTop() );

                status = ct.go();

                // linesWritten += ct.getLinesWritten();

                //  LogService.logIt( "BaseTmLdrReportTemplate.addTableToDocument() status=" + status  );

                pages++;
            }

            return ct.getYLine();
    }



    /**
     * Returns
     *    Next Index -- if three is a residual, it's the index of the residual, else it's the next index
     *    Amount of height used
     *    Residual height unused from split cell
     *
     * @param maxRoom
     * @param startIndex
     * @param maxIndex
     * @param isSplitLate
     * @param rowHgts
     * @return
     */
    public Object[] calcTableHghtUsed( float maxRoom, float prevResidual, int startIndex, int maxIndex, boolean isSplitLate, float[] rowHgts )
    {
        // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed( maxRoom=" + maxRoom + ", prevResidual=" + prevResidual + ", startIndex=" + startIndex + ", maxIndex=" + maxIndex + ", isSplitLate=" + isSplitLate + ", " + ")");

        Object[] dta = new Object[] {new Integer(startIndex) , new Float(0), new Float(0) };

        if( rowHgts.length<=startIndex )
            return dta;

        float hgt = 0;
        float resid = 0;

        if( prevResidual>0 )
        {
            // Bigger than max
            if( prevResidual>= maxRoom )
            {
                dta[1] = Float.valueOf(maxRoom);
                dta[2] = Float.valueOf(prevResidual -  maxRoom);

                if( prevResidual== maxRoom)
                {
                    dta[0]=startIndex+1;
                    dta[2] = Float.valueOf(0);
                }

                return dta;
            }

            hgt = prevResidual;
            maxRoom -= prevResidual;
            startIndex++;
        }

        for( int i=startIndex; i<rowHgts.length && i<=maxIndex; i++ )
        {
            if( rowHgts[i] + hgt == maxRoom )
            {
                dta[0]=Integer.valueOf(i+1);
                dta[1] = Float.valueOf(hgt);
                return dta;
            }

            if( rowHgts[i] + hgt > maxRoom )
            {
                if( i==startIndex || !isSplitLate )
                {
                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() AAA i=" + i + ", hgt=" + hgt );

                    resid = rowHgts[i] - (maxRoom-hgt);
                    dta[2] = Float.valueOf(resid);
                    hgt = maxRoom;

                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() BBB hgt=" + hgt + ", resid=" + resid );
                }

                dta[0] = Integer.valueOf(i);
                dta[1] = Float.valueOf(hgt);
                return dta;
            }

            hgt += rowHgts[i];
        }

        dta[0] = maxIndex+1;
        dta[1] = Float.valueOf(hgt);
        return dta;
    }


    public void addNewPage() throws Exception
    {
        document.newPage();
        this.currentYLevel = pageHeight - PAD -  headerHgt;
    }



    public String lmsg( String key )
    {
        return lmsg( key, null );
    }

    public String lmsg( String key, String[] prms )
    {
        return MessageFactory.getStringMessage( reportData.getLocale() , key, prms );
    }

    
    public void setRunDirection( PdfPCell c )
    {
        if( c == null || reportData == null || reportData.getLocale() == null )
            return;

        if( I18nUtils.isTextRTL( reportData.getLocale() ) )
            c.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }

    public void setRunDirection( PdfPTable t )
    {
        if( t == null || reportData == null || reportData.getLocale() == null )
            return;

        if( I18nUtils.isTextRTL( reportData.getLocale() ) )
            t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }
    
    
    public void addIdentityImageCaptureSection() throws Exception
    {
        // LogService.logIt(  "BaseCT2ReportTemplate.addIdentityImageCaptureInfo() UsePremium=" + ProctorHelpUtils.getUseExternalProctoring(reportData.getTestKey()) );

        if( ProctorHelpUtils.getUseExternalProctoring(reportData.getTestKey()) )
        {
            if( reportData.getTestEvent().getRemoteProctorEvent()==null )
            {
                if( proctorUtils==null )
                    proctorUtils = new ProctorUtils();
                proctorUtils.setupRemoteProctorEvent( reportData.getLocale(), reportData.getUser().getTimeZone(), reportData.getTestEvent() );
            }

            if( reportData.getTestKey().getOnlineProctoringType().getIsPremiumWithImageCap() || reportData.getTestEvent().getRemoteProctorEvent()!=null )
            {
                addPremiumIdentityImageCaptureSection();
                return;
            }

            // if external but no images, do not include.
            //return;
        }

    }
    
    /*
    public void addIdentityImageCaptureSection() throws Exception
    {   
        try
        {
            // boolean showImages = !reportData.getReportRuleAsBoolean( "captimgsoff" ) && !reportData.tk.getHideMediaInReports();
                                    
            // LogService.logIt(  "BaseCT2ReportTemplate.addIdentityImageCaptureInfo() showImages=" + showImages + ", reportData.tk.getHideMediaInReports()=" + reportData.tk.getHideMediaInReports() + ", testEventId=" + reportData.te.getTestEventId() );
            List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDIMAGEUPLOAD );
            
            if( tesl.isEmpty() )
                return;
            
            java.util.List<TextAndTitle> ttl = new ArrayList<>();
                        
            for( TestEventScore tes : tesl )
            {
                ttl.addAll( tes.getTextBasedResponseList( null, true, true ) );
            }

            if( ttl.isEmpty() )
                return;
                  
            PdfPTable t = getBasicIdentityImageCaptureTableTop( reportData, tesl , pageWidth );
            if( t==null )
                return;
            
            addNewPage();
            previousYLevel =  currentYLevel;
            float y = addTitle( previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle" ) );
            currentYLevel = addTableToDocument(y, t );
            
            t = getBasicIdentityImageCaptureTableImages( reportData, tesl , pageWidth );
            if( t==null )
                return;

            previousYLevel = currentYLevel;
            y = previousYLevel; // addTitle( previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle" ) );
            currentYLevel = addTableToDocument(y, t );

           // LogService.logIt( "BaseCT2ReportTemplate.addIdentityImageCaptureSection() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addIdentityImageCaptureSection()" );
            throw new STException( e );
        }        
    }
    */
    

    public void addPremiumIdentityImageCaptureSection() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() Start. " );
            if( !ProctorHelpUtils.getUseExternalProctoring(reportData.tk) )
                return;

            if( reportData.getReportRuleAsBoolean( "hideimgcaptpdf" ) || reportData.getReportRuleAsBoolean( "allprocpdf")  )
                return;

            RemoteProctorEvent rpe = reportData.te.getRemoteProctorEvent();
            if( proctorUtils==null )
                proctorUtils = new ProctorUtils();
            if( rpe==null || rpe.getUploadedUserFileList()==null )
            {
                proctorUtils.setupRemoteProctorEvent( reportData.getLocale(), reportData.getUser().getTimeZone(), reportData.te );
                rpe = reportData.te.getRemoteProctorEvent();
            }

            //LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() rpe=" + (rpe==null ? "null" : "id=" + rpe.getRemoteProctorEventId() + ", status=" + rpe.getRemoteProctorEventStatusType().getName()) + ", uploadedUserFiles=" + (rpe!=null && rpe.getUploadedUserFileList()!=null ? rpe.getUploadedUserFileList().size() : " null - 0")   );

            if( rpe==null )
                return;

            // boolean imageComparisonsComplete = rpe.getRemoteProctorEventStatusTypeId()>=RemoteProctorEventStatusType.IMAGE_COMPARISONS_COMPLETE.getRemoteProctorEventStatusTypeId();

            // No photos at all.
            List<UploadedUserFile> ufl = rpe.getUploadedUserFileListForPhotos();
            List<UploadedUserFile> uflRec = rpe.getOnlineProctoringType().getRecordsVideo() ? rpe.getUploadedUserFileListForRecordings() : null;
            if( uflRec!=null && !uflRec.isEmpty() )
                ufl.addAll(uflRec);

            List<UploadedUserFile> uflIds = rpe.getUploadedUserFileListForIds();
            if( ufl.isEmpty() && uflIds.isEmpty() )
            {
                //LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() ufl.size=" + ufl.size() + ", uflIds.size=" + uflIds.size() );
                return;
            }

            boolean showImages = reportData.o.getCandidateImageViewTypeId()<=0 && !reportData.getReportRuleAsBoolean( "captimgsoff" ) && !reportData.getReportRuleAsBoolean( "captimgsoffpdfonly" ) && !reportData.tk.getHideMediaInReports() && reportData.o.getHideProcImagesPdf()<=0;

            boolean forceIncludeAllImages = false; // showImages && (rpe.getMultiFaceThumbs()>0);

            int maxImagesToShow = 22;

            //float avgScoreId = rpe.getThumbScore();
            //float avgScoreFace = rpe.getIdFaceMatchPercent();
            //float overallProctorScore = rpe.getOverallProctorScore();
            //float tot;
            //float count=0;

            if( proctorUtils==null )
                proctorUtils = new ProctorUtils();
            List<UploadedUserFile> ufl2 = showImages ? proctorUtils.getFauxUploadedUserFileListForReportThumbs( ufl, forceIncludeAllImages, maxImagesToShow ) : new ArrayList<>();
            List<UploadedUserFile> uflId2 = showImages ? proctorUtils.getFauxUploadedUserFileListForReportThumbs( uflIds, forceIncludeAllImages, maxImagesToShow ) : new ArrayList<>();

            boolean hasMax = ufl2.size()>=maxImagesToShow;

            List<String[]> caveatList = proctorUtils.getPremiumCaveatList( reportData.getTestKey().getProctoringIdCaptureTypeId(),  rpe, reportData.getLocale(), hasMax );

            //LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() showImages=" + showImages + ", ufl2.size=" + ufl2.size() + ", uflId2.size=" + uflId2.size() + ", caveatList.size=" + caveatList.size() );

            PdfPTable t = generateIdentityImageCaptureTableTop(reportData,
                                                             caveatList,
                                                             rpe.getOverallProctorScore(),
                                                             pageWidth );

            if( t==null )
                return;

            addNewPage();
            previousYLevel =  currentYLevel;
            float y = addTitle(previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle.Premium" )); // , null, null );

            currentYLevel = addTableToDocument(y, t ); //, false, true );

            t = generateIdentityImageCaptureTableImages(reportData,
                                                             ufl2,
                                                             uflId2,
                                                             pageWidth );

            if( t==null )
                return;

            previousYLevel =  currentYLevel;
            y = previousYLevel; // addTitle( previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle.Premium" ) );

            currentYLevel = addTableToDocument(y, t ); //, false, true );

           // LogService.logIt( "BaseCT2ReportTemplate.addIdentityImageCaptureSection() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() testEventId=" + (reportData.te==null ? "null" : reportData.te.getTestEventId()) + ", reportId=" + (reportData.r2Use==null ? "null" : reportData.r2Use.getReportId()) );
            throw new STException( e );
        }

    }
    
    public PdfPTable getScoreTextAndTopicCaveatScoreTable( String scoreText, String headerStr, String footerStr, List<CaveatScore> csl, Font fontToUse, int simCompetencyClassId )
    {
        if( (scoreText==null ||scoreText.isBlank()) && 
            (csl==null || csl.isEmpty()))
            return null;
        
        //if( (csl==null || csl.isEmpty())  )
        //    return null;

        try
        {
            PdfPTable t = new PdfPTable(2);
            setRunDirection(t);
            t.setHorizontalAlignment(Element.ALIGN_LEFT);

            PdfPCell c;
            
            
            if( scoreText!=null && !scoreText.isBlank() )
            {
                c = new PdfPCell(new Phrase(scoreText, fontToUse));
                c.setColspan(2);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setPaddingBottom(10);
                c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);                
            }
            
            if( headerStr!=null && !headerStr.isBlank() )
            {
                c = new PdfPCell(new Phrase(headerStr, fontToUse));
                c.setColspan(2);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);                
            }
            
            for (CaveatScore cs : csl)
            {
                if (cs.getLocale() == null)
                    cs.setLocale(reportData.getLocale());

                // Either the last or the second to last CS can be a Topic.
                if( cs.getIsTopic() )
                {
                    String[] dd = ReportUtils.parseTopicCaveatScore(cs, csl.size()==1, simCompetencyClassId);

                    c = new PdfPCell(new Phrase("\u2022 " + dd[1] + ":", fontToUse));
                    c.setColspan(1);
                    c.setBorderWidth(0);
                    c.setPadding(2);
                    c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    setRunDirection(c);
                    t.addCell(c);

                    c = new PdfPCell(new Phrase(dd[2], fontToUse));
                    c.setColspan(1);
                    c.setBorderWidth(0);
                    c.setPadding(2);
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    setRunDirection(c);
                    t.addCell(c);
                    
                    continue;
                }
                
                c = new PdfPCell(new Phrase("\u2022 " + cs.getCol1() + (cs.getCaveatScoreType().getColspan()==1 ? ":" : ""), fontToUse));
                c.setColspan(cs.getCaveatScoreType().getColspan());
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);
                
                if(cs.getCaveatScoreType().getColspan()>1)
                    continue;
                
                c = new PdfPCell(new Phrase(cs.getCol2(), fontToUse));
                c.setColspan(1);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);
            }
            
            // Footer
            if( footerStr!=null && !footerStr.isBlank() )
            {
                c = new PdfPCell(new Phrase(footerStr, fontToUse));
                c.setColspan(2);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);                
            }

            return t;
        } catch (Exception e)
        {
            LogService.logIt(e, "BaseTmLdrReportTemplate.getCaveatScoreTable() ");
            throw e;
        }        
    }
    
    

}
