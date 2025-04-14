/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.sports;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.user.User;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.TableBackground;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public abstract class BaseSpReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    
    SpData spData;
    
    public BaseColor lightgrayshade = new BaseColor( 0xe8, 0xe8, 0xe8 );
    
    static Image hraSportsLogoBlackText;

    static String hraSportsLogoBlackTextUrl = "https://cdn.hravatar.com/web/orgimage/O3TtwpoP4zk-/img_6x1562601188266.png";
    
    
    public BaseSpReportTemplate()
    {
        super();
        
        spData = new SpData();
        
        devel = true;
        
        // this.redYellowGreenGraphs=false;
    }
    
    
    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CT2ReportData();

        initLocales();
        
        initFonts();
        
        initColors();   
        
        if( hraSportsLogoBlackText == null )
            hraSportsLogoBlackText = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( hraSportsLogoBlackTextUrl ) );
        
        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        // LogService.logIt( "BaseCT2ReportTemplate.init() title=" + rd.getReportName() );
        
        SpHeaderFooter hdr = new SpHeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this );

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

        // LogService.logIt( "BaseCT2ReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

        tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );
    }
    
    
    @Override
    public Image getHraLogoBlackText() {
        return hraSportsLogoBlackText;
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
            
            //boolean hideOverallNumeric = true; //  reportData.getReportRuleAsBoolean( "ovrnumoff" );
            //boolean hideOverallGraph = true; // reportData.getReportRuleAsBoolean( "ovrgrphoff" );
            //boolean hideOverallScoreText = true; // reportData.getReportRuleAsBoolean( "ovrscrtxtoff" );
                        
            float y = addTitle(previousYLevel, lmsg( "g.Overall" ), null, null, null );

            y -= TPAD;

            TestEventScore tes = reportData.getTestEvent().getOverallTestEventScore();
            
            // float scrValue = tes.getScore();
            // 1=low, 2=med, 3=high
            //int scrCode = 1;
            //if( scrValue>=3 )
            //    scrCode=2;
            //if( scrValue>=7)
            //    scrCode=3;
            
            // String overallScoreTitle = reportData.getReport().getStrParam4()!=null && !reportData.getReport().getStrParam4().isEmpty() ? reportData.getReport().getStrParam4()  :  bmsg( "g.AssessmentInfo");
            
            //int scrDigits = reportData.getReport().getIntParam2() >= 0 ? reportData.getReport().getIntParam2() : reportData.getTestEvent().getScorePrecisionDigits();
            
            //String scr = I18nUtils.getFormattedNumber( reportData.getLocale(), scrValue, scrDigits );

            if( reportData.hasProfile() )
                 tes.setProfileBoundaries( reportData.getOverallProfileData() );

            // LogService.logIt( "Found " + ct3RiskFactors.size() + " CT3 Risk Factors." );
            // ScoreFormatType sft = ScoreFormatType.getValue( tes.getScoreFormatTypeId() );
            
            // ScoreCategoryType sct = tes.getScoreCategoryType();

            int cols = 1;
            float[] colRelWids = new float[] { 1 };

            //boolean includeNumScores = !hideOverallNumeric; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            //boolean includeRating = false; // reportData.getReport().getIncludeSubcategoryCategory()==1;
            //boolean includeColorGraph = false; // !hideOverallGraph && sft.getSupportsBarGraphic(reportData.getReport()) && reportData.getReport().getIncludeColorScores()==1; // && reportData.getReport().getIncludeCompetencyColorScores()==1;

            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();
            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            
            
            // int scrLevel = (int) Math.round( Math.floor(scrValue) );
                            
            // Next row - Text
            // String langKey = cqData.getOverallScoreTextKey( scrValue ); //  "cq.overall.level" + scrLevel;
            
            String[] params = new String[] { reportData.u.getFullname() };
            
            //String scrTxt = ""; //  bmsg( langKey, params ); //getTestEvent().getOverallTestEventScore().getScoreText();

            //if( hideOverallScoreText || reportData.getReport().getIncludeScoreText() != 1 )
            //    scrTxt = null;
                 
            // LogService.logIt( "BaseSpReportTemplate.addReportInfoHeader() hideOverallScoreText=" + hideOverallScoreText + ", scrTxt=" + scrTxt );
            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( bmsg( "g.Athlete"), fontLargeWhite ) );            
            c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );                
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);            
                        
            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setColspan( 1 );
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );
            
            // NAME
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            //c.setColspan(colspan);
            c.setPadding( 1 );

            if( reportData.getIsLTR() )
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            else
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

            c.setVerticalAlignment( Element.ALIGN_BOTTOM );
            
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            touter.addCell( c );
            
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

                if( reportData.getIsLTR() )
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );                
            }

            // Next row - test name
            c = new PdfPCell( new Phrase( reportData.getSimName(), getFontLight() ) );
            c.setColspan( cols );
            c.setPadding( 1 );
            if( reportData.getIsLTR() )
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            else
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );
            
            // Note - the date MAY be the last entry in this table if there is no score text. 
            boolean lastEntry = !hasThirdPartyId && 
                                (reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty());
                        
            // Next Row, test date
            c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFontLight() ) );
            c.setColspan( cols );
            c.setPadding( 1 );
            // c.setPaddingBottom( 6 );
            if( reportData.getIsLTR() )
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            else
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

            if( hasThirdPartyId )
            {
                lastEntry = reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty(); 
                
                //if( thirdPartyTestEventIdentifierName==null || thirdPartyTestEventIdentifierName.isEmpty() )
                //    thirdPartyTestEventIdentifierName = lmsg( "g.ThirdPartyEventIdC" );
                //else
                //    thirdPartyTestEventIdentifierName += ":";

                c = new PdfPCell( new Phrase( thirdPartyId, getFontLight() ) );
                c.setColspan( cols );
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );

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
                lastEntry = true; 
                
                c = new PdfPCell( new Phrase( "(m) " + reportData.getUser().getMobilePhone(), getFontLight() ) );
                c.setColspan( cols );
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );
                if( reportData.getIsLTR() )
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                else
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

            // float y = pageHeight - headerHgt - 4*PAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseSpReportTemplate.addReportInfoHeader()" );

            throw e;
        }
    }
    
        
    @Override
    public void initColors()
    {
        // Nothing. 
        if( ct2Colors == null )
            ct2Colors = SpColors.getCt2Colors( devel );
    }

    
    
    
    public void addCompetencySummarySection() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseSpReportTemplate.addCompetencySummarySection() Using locale: " + reportData.getLocale().toString() + ", g.AssessmentOverview=" + lmsg( "g.AssessmentOverview" ) );

            // If no info to present.
            if( reportData.getReportRuleAsBoolean("cmptysumoff") ||
                reportData.getReport().getIncludeCompetencyScores()!=1  )
                return;

            java.util.List<TestEventScore> teslA = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOGNITIVE );
            teslA.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOG_COMBO ) );
            Collections.sort(teslA, new DisplayOrderComparator() );  // new TESNameComparator());            
            
            String[] comps = spData.getCompetencies();
            // Map<String,String[]> clusterDimsMap = cqData.getClusterDimsMap();
            
            if( teslA.isEmpty() )
            {
                LogService.logIt( "BaseSpReportTemplate.addSummaryChart() No Competencies found to include in Summary Chart. Not including chart at all" );
                return;
            }

            previousYLevel =  currentYLevel;
                        
            float y = addTitle(previousYLevel, bmsg( "g.ClusterAndDimensionSummary" ), competencySummaryStr, null, null );

            y -= TPAD;
            
            currentYLevel = y;
            previousYLevel =  currentYLevel;
            
            Locale loc = reportData.getLocale();
            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .4f, .12f, .18f, .4f} : new float[] { .4f, .18f, .12f, .4f};

            ScoreFormatType sft = reportData.getTestEvent().getScoreFormatType();
            
            boolean includeNumScores = reportData.getReport().getIncludeSubcategoryNumeric()==1 && !reportData.getReportRuleAsBoolean("cmptynumoff");
            boolean includeColorGraph = sft.getSupportsBarGraphic(reportData.getReport()) && reportData.getReport().getIncludeCompetencyColorScores()==1;
            
            if( !includeNumScores && !includeColorGraph )
                return;
                        
            // Color graph only
            if( !includeNumScores )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ?  new float[] { .4f, .18f, .4f } : new float[] { .4f, .18f, .4f };
            }            

            // nums only
            else if( !includeColorGraph )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ?  new float[] { .4f, .12f, .18f } :  new float[] { .18f, .12f, .4f};
            }

            // LogService.logIt( "BaseSpReportTemplate.addCompetencySummarySection() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );  

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );
            
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( bmsg( "g.ClusterAndDimension"), fontLargeWhite ) );
            c.setColspan(1);
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            if( includeNumScores )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
                c.setColspan( 1 );

                if( cols==2 )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP : Rectangle.TOP ); // | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
                    
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);
            }
            
            c = new PdfPCell( new Phrase( "", fontLargeWhite ) );
            c.setColspan(1);
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP : Rectangle.TOP );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            if( includeColorGraph )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.Interpretation" ), fontLargeWhite ) );
                c.setColspan( 1 );
                c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);
            }
                                        
            TestEventScore tes;
            String nameEnglish;
            String name;
            boolean last = false;
            String lastComp = comps[comps.length-1];
            
            String comp;
            boolean tog = false;
            for( int idx=0;idx<comps.length; idx++ )
            {
                if( comps.length<=idx )
                    continue;
                
                comp = comps[idx];
                name = spData.getName( loc, comp );
                nameEnglish = spData.getName( Locale.US, comp );  
                
                tes = getTesForItem( name, nameEnglish, teslA );
                
                if( tes==null )
                {
                    LogService.logIt( "BaseSpReportTemplate.addCompetencySummarySection() No TestEventScore found for comp " + name );
                    continue;
                }
                
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                {
                    LogService.logIt( "BaseSpReportTemplate.addCompetencySummarySection() TestEventScore.hide prevents showing in reports, for comp " + name );
                    continue;
                }

                if( comp.equalsIgnoreCase(lastComp) )
                        last=true;
                
                
                addCompetencySummaryRow( tog, name, tes, last, includeNumScores, includeColorGraph, t );
                
                tog=!tog;
                
            }

            currentYLevel = addTableToDocument(currentYLevel, t, false, true );
                        
            previousYLevel =  currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseSpReportTemplate.addCompetencySummarySection()" );
            throw new STException( e );
        }
            
    }
    
    
    
    protected void addCompetencySummaryRow( boolean tog, String name, TestEventScore tes, boolean last, boolean includeNumScores, boolean includeColorGraph, PdfPTable t )
    {
        ScoreCategoryType sct = tes.getScoreCategoryType();
        int scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();    

        int scrCode = spData.getScoreCode( tes.getScore() );
        String scrCatTxt = spData.getScoreCategoryText(reportData.getLocale(), scrCode, false );
        
        PdfPCell c;
        
        Font fontToUse = fontLight;
                
        c = new PdfPCell( new Phrase( name , fontToUse ) );
        c.setPadding( 1 );
        c.setColspan( 1 );
        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        if( last )
            c.setBorder( reportData.getIsLTR() ?  Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT  | Rectangle.BOTTOM );
        else
            c.setBorder( reportData.getIsLTR() ?  Rectangle.LEFT : Rectangle.RIGHT );
        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
        c.setBorderWidth( scoreBoxBorderWidth );
        if( tog )
            c.setBackgroundColor( lightgrayshade );
        setRunDirection( c );
        t.addCell( c );

        if( includeNumScores )
        {
            String scrValue = I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), scrDigits );

            c = new PdfPCell( new Phrase( scrValue , fontToUse ) );

            if( last )
            {
                if( includeColorGraph )
                     c.setBorder( Rectangle.BOTTOM );   
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
            }   

            else if( includeColorGraph )
                c.setBorder( Rectangle.NO_BORDER );   
            else
                c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );  

            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            if( tog )
                c.setBackgroundColor( lightgrayshade );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding(1);
            c.setPaddingRight( 8 );
            setRunDirection( c );
            t.addCell( c );                    
        }     
        
        c = new PdfPCell( new Phrase( scrCatTxt , fontToUse ) );
        c.setPadding( 1 );
        c.setColspan( 1 );
        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        if( last )
            c.setBorder( Rectangle.BOTTOM );
        else
            c.setBorder( Rectangle.NO_BORDER );
        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
        c.setBorderWidth( scoreBoxBorderWidth );
        if( tog )
            c.setBackgroundColor( lightgrayshade );
        setRunDirection( c );
        t.addCell( c );
        

        if( includeColorGraph )
        {
            c = new PdfPCell( new Phrase("") ); 
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setPaddingTop( 7 );
            c.setPaddingBottom( 8 );
            if( tog )
                c.setBackgroundColor( lightgrayshade );
            setRunDirection( c );

            if( tes.getIncludeNumericScoreInResults() )
            {
                c.setFixedHeight(22);
                c.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, false, ct2Colors, devel, false, false, true, 0 ) );
            }

            c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT );

            if( last )
                c.setBorder( reportData.getIsLTR() ?  Rectangle.BOTTOM | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.LEFT );
            else
                c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT );

            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            t.addCell( c );                                     
        }          
    }
    
    
    protected TestEventScore getTesForItem( String name, String nameEnglish, List<TestEventScore> tesl )
    {
        if( ( name==null || name.isEmpty() ) && ( nameEnglish==null || nameEnglish.isEmpty() ) || tesl==null )
            return null;
        
        for( TestEventScore tes : tesl )
        {
            if( StringUtils.isValidNameMatch(name, nameEnglish, tes.getName(), tes.getNameEnglish() ) )
                return tes;
        }
        
        return null;            
    }
    
    
    
    
    protected void addCompetencyDetailSection() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            ScoreFormatType sft = reportData.getTestEvent().getScoreFormatType();            
            boolean includeNumScores = reportData.getReport().getIncludeSubcategoryNumeric()==1 && !reportData.getReportRuleAsBoolean("cmptynumoff");
            boolean includeColorGraph = sft.getSupportsBarGraphic(reportData.getReport()) && reportData.getReport().getIncludeCompetencyColorScores()==1;
                        
            
            java.util.List<TestEventScore> teslA = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOGNITIVE );
            teslA.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOG_COMBO ) );
            Collections.sort(teslA, new DisplayOrderComparator() );  // new TESNameComparator());  

            if( teslA.isEmpty() )
            {
                LogService.logIt( "BaseSpReportTemplate.addCompetencyDetailSection() No Competencies found to include. Not including chart at all" );
                return;
            }
                 
            currentYLevel -= TPAD;
            
            previousYLevel =  currentYLevel;
            
            currentYLevel = addTitle(previousYLevel, bmsg( "g.ClusterAndDimensionDetail" ), competencySummaryStr, null, null );

            // currentYLevel -= TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;
                            
            String[] comps = spData.getCompetencies();             
            for( String comp : comps )
            {
                addCompetencyDetailTable(comp, includeNumScores, includeColorGraph, teslA );
            }

            previousYLevel =  currentYLevel;            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseSpReportTemplate.addCompetencyDetailSection()" );

            throw new STException( e );
        }
    }
    
    protected void addCompetencyDetailTable( String comp, boolean includeNumScores, boolean includeColorGraph, List<TestEventScore> teslA ) throws Exception
    {
        try
        {
            // LogService.logIt("BaseSpReportTemplate.addCompetencyDetailTable() adding comp table for " + comp );
            TestEventScore tes;
            boolean last = false;
            
            String name = spData.getName(reportData.getLocale(), comp );
            String nameEnglish = spData.getName( Locale.US, comp );

            tes = getTesForItem( name, nameEnglish, teslA );

            if( tes==null )
            {
                LogService.logIt( "BaseSpReportTemplate.addCompetencyDetailTable() No TestEventScore found for cluster " + name );
                return;
            }

            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
            {
                LogService.logIt( "BaseSpReportTemplate.addCompetencyDetailTable() TestEventScore.hide prevents showing in reports, for cluster " + name );
                return;
            }

            last=true;

            int scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();   
            
            int cols = 2;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .4f, .6f} : new float[] { .6f, .4f};

            // LogService.logIt( "BaseSpReportTemplate.addCompetencySummary() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );
                        

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );
            
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;
            t.setTotalWidth( outerWid );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            //t.setHeaderRows( 0 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
                 
            // Add the cluster info.
            float scrValue = tes.getScore();
            int scrCode = spData.getScoreCode(scrValue);
            String scrCatTxt=spData.getScoreCategoryText(reportData.getLocale(), scrCode, true );
            String scrTxt = spData.getCompetencyScoreText(reportData.getLocale(), comp, scrCode);
            String descrip = spData.getDescription(reportData.getLocale(), comp);
            String impt = spData.getImportant(reportData.getLocale(), comp);
            List<String> questions = spData.getQuestionList(reportData.getLocale(), comp, scrCode);

            if( scrTxt==null || scrTxt.isEmpty() )
            {
                LogService.logIt("BaseSpReportTemplate.addCompetencyDetailTable() ScrTxt is missing for cluster " + comp + ", score=" + tes.getScore() + ", scrCode=" + scrCode );
                scrTxt="";
            }
            
            
            c = new PdfPCell( new Phrase( name , fontXLargeBoldWhite ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 1 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP : Rectangle.RIGHT | Rectangle.TOP );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( descrip , fontXLargeWhite ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 1 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.TOP : Rectangle.LEFT | Rectangle.TOP );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell( c );
            
            String srcStr = includeNumScores ? bmsg( "g.ScoreCX", new String[]{ I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), scrDigits ) }) : "";
            c = new PdfPCell( new Phrase( srcStr , fontXLargeBold ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 1 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP : Rectangle.RIGHT | Rectangle.TOP );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( lightgrayshade );
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( scrCatTxt , fontXLarge ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 1 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( lightgrayshade );
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( scrTxt , fontLarge ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( lightgrayshade );
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( bmsg( "g.WhyIsXImportant", new String[] {name} ) , fontXLargeBold ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell( c );
            
            c = new PdfPCell( new Phrase( impt , fontLarge ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell( c );
            
            c = new PdfPCell( new Phrase( bmsg( "g.QuestionsToAsk" ) , fontXLargeBold ) );
            c.setPadding( 2 );
            c.setPaddingTop(4);
            c.setPaddingBottom(4);
            c.setColspan( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor( lightgrayshade );
            setRunDirection( c );
            t.addCell( c );
            
            String q;
           
            for( int i=0;i<questions.size(); i++ )
            {
                q = questions.get(i);
                last = i==questions.size()-1;        
                c = new PdfPCell( new Phrase( q , fontLarge ) );
                c.setPadding( 2 );
                c.setPaddingTop(4);
                c.setPaddingBottom(4);
                c.setColspan( 2 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                if( last )
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBackgroundColor( lightgrayshade );
                setRunDirection( c );
                t.addCell( c );                
            }
                                   
            previousYLevel =  currentYLevel;
            
            float y = currentYLevel; 

            y -= TPAD;  

            currentYLevel = addTableToDocument(y, t, true, true );
                        
            previousYLevel =  currentYLevel;            
        }

        catch( Exception e )
        {
            LogService.logIt(e, "CT2DevelopmentReport.addCompetencyDetailTable() cluster=" + comp );

            throw new STException( e );
        }
    }    
    
    
    


    
    public String bmsg( String key )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict(spData.getBundleName(), null, reportData.getLocale(), key, null );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage(spData.getBundleName(), reportData.getLocale() , key, null );
    }

    // 
    public String bmsg( String key, String[] prms )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict(spData.getBundleName(), null, reportData.getLocale(), key, prms );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage(spData.getBundleName(), reportData.getLocale() , key, prms );
    }

    public String bmsg( Locale loc, String key, String[] prms )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict(spData.getBundleName(), null, loc, key, prms );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage(spData.getBundleName(), loc , key, prms );
    }
    
    
}
