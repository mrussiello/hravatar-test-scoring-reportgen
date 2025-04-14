/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.nqesh.fbk;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.EventFacade;
import com.tm2score.format.TableBackground;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.JaxbUtils;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class NqeshFeedbackReport extends BaseCT2ReportTemplate implements ReportTemplate
{
    String bundleToUse = null;
    String defaultBundleToUse = null;

    NqeshReportUtils nqeshReportUtils;

    List<TestEventScore> comboCompList;
    Map<String,List<TestEventScore>> memberCompMap;




    public NqeshFeedbackReport()
    {
        super();

        this.devel = false;
        // this.redYellowGreenGraphs=true;

    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            specialInit();

            initSpec();

            LogService.logIt( "NqeshFeedbackReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );

            addCoverPage(false);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();

            addNewPage();

            addDomainScoreFeedbackTables();

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
            LogService.logIt( e, "NqeshFeedbackReport.generateReport() " );
            throw new STException( e );
        }
    }




    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CT2ReportData();

        initLocales();

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

        // pdfWriter = new PdfCopy(document, baos);
        // pdfWriter.addJavaScript( "" );

        // LogService.logIt( "BaseCT2ReportTemplate.init() title=" + rd.getReportName() );

        NqeshFeedbackHeaderFooter hdr = new NqeshFeedbackHeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this, custLogo );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );


        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        // pdfWriter.addJavaScript( DOCUMENT_JS_API );


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




    private synchronized void initSpec() throws Exception
    {
        if( comboCompList!=null )
            return;

        // get all Combination Competencies.
        comboCompList = new ArrayList<>();
        for( TestEventScore tes : reportData.te.getTestEventScoreList() )
        {
            if( tes.getSimCompetencyClass().getIsCombo() )
                comboCompList.add(tes);
        }

        // Sort Combination Competencies.
        Collections.sort(comboCompList);

        // LogService.logIt( "NqeshFeedbackReport.initSpec() AAA.1 Found " + comboCompList.size() + " combo SimCompetencies." );

        memberCompMap = new HashMap<>();

        List<TestEventScore> memberCompList;
        // For each combo competency, gather member competencies and sort.
        for( TestEventScore domainTes : comboCompList )
        {
            memberCompList = new ArrayList<>();

            for( TestEventScore tes : reportData.te.getTestEventScoreList() )
            {
                if( !tes.getTestEventScoreType().getIsCompetency() )
                    continue;

                // skip combos
                if( tes.getSimCompetencyClass().getIsCombo() )
                    continue;

                if( doesTestEventScoreBelongToCombo(domainTes, tes ) )
                    memberCompList.add( tes );
            }

            Collections.sort( memberCompList );

            // LogService.logIt("NqeshFeedbackReport.initSpec() AAA.2 Found " + memberCompList.size() + " member Competencies for Combo Competency " + domainTes.getName() );

            memberCompMap.put(domainTes.getName(), memberCompList );
        }
    }



    @Override
    public void addCompetencySummaryChart() throws Exception
    {
        try
        {
            // If no info to present.
            if( reportData.getReportRuleAsBoolean("cmptysumoff")  )
                  return;

            initSpec();
            // LogService.logIt( "NqeshFeedbackReport.addCompetencySummaryChart() Using locale: " + reportData.getLocale().toString() + ", g.AssessmentOverview=" + lmsg( "g.AssessmentOverview" ) );

            int totalComps = 0;
            List<TestEventScore> dtesl;
            for( TestEventScore domainTes : this.comboCompList )
            {
                dtesl = memberCompMap.get( domainTes.getName() );
                totalComps += dtesl==null ? 0 : dtesl.size();
            }

            LogService.logIt( "NqeshFeedbackReport.addCompetencySummaryChart() Total domains: " + comboCompList.size() + ", Total sub-competencies: " + totalComps );

            if( comboCompList.isEmpty() )
            {
                LogService.logIt( "NqeshFeedbackReport.addCompetencySummaryChart() No Competencies found to include in Summary Chart. Not including chart at all" );
                return;
            }


            int scrDigits;
            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .05f, .55f, .1f, .4f} : new float[] { .4f, .1f, .55f, .05f};

            // ScoreFormatType sft = ScoreFormatType.NUMERIC_0_TO_100; // reportData.getTestEvent().getScoreFormatType();

            // LogService.logIt( "NqeshFeedbackReport.addCompetencySummaryChart() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );

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
            c = new PdfPCell( new Phrase( lmsg( "g.Competency"), fontLargeWhite ) );
            c.setColspan(2);
            // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            // c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER);
            // c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            // c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg("g.Interpretation"), fontLargeWhite ) );
            // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            // c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            setRunDirection( c );
            t.addCell(c);

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.CompetencySummary"), null, null, null );

            y -= TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            boolean isLastDomain;
            boolean isLastSub = false;
            int domainCt = 0;
            int subCt = 0;
            String scoreStr;
            PdfPTable graphT;
            PdfPCell c2;

            for( TestEventScore domainTes : comboCompList )
            {

                scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  domainTes.getScoreFormatType().getScorePrecisionDigits();

                domainCt++;
                isLastDomain = domainCt>=comboCompList.size() || domainCt==2 || domainCt==4;

                // new table on a new page
                if( domainCt==3 || domainCt==5 )
                {
                    addTableToDocument(y, t, false, true );
                    addNewPage();
                    y = currentYLevel;

                    t = new PdfPTable( cols );

                    t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
                    t.setWidths( colRelWids );
                    t.setLockedWidth( true );
                    setRunDirection( t );
                    c = t.getDefaultCell();
                    c.setPadding( 0 );
                    c.setBorder( Rectangle.NO_BORDER );
                    setRunDirection( c );

                    // Create header
                    c = new PdfPCell( new Phrase( lmsg( "g.Competency"), fontLargeWhite ) );
                    c.setColspan(2);
                    // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                    c.setBorder( Rectangle.NO_BORDER);
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER);
                    // c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
                    // c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
                    c.setBorder( Rectangle.NO_BORDER);
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( 25 );
                    c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( lmsg("g.Interpretation"), fontLargeWhite ) );
                    // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                    c.setBorder( Rectangle.NO_BORDER);
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);
                }

                isLastSub=false;

                scoreStr = I18nUtils.getFormattedNumber(reportData.getLocale(), domainTes.getScore(), scrDigits );

                dtesl = memberCompMap.get( domainTes.getName() );

                c = new PdfPCell( new Phrase( removeSquareBrackets(domainTes.getName(), true), fontBold ) );
                c.setColspan(2);
                //if( dtesl.isEmpty() )
                //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                //else
                //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 2 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                setRunDirection( c );
                t.addCell(c);


                c = new PdfPCell( new Phrase( scoreStr, fontBold ) );
                //if( dtesl.isEmpty() )
                //    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );
                //else
                //    c.setBorder( Rectangle.TOP );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setPadding( 2 );
                setRunDirection( c );
                t.addCell(c);


                graphT = new PdfPTable( 1 );
                graphT.setHorizontalAlignment( Element.ALIGN_CENTER );
                c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
                c2.setBorder( Rectangle.NO_BORDER );
                c2.setPadding( 5 );
                c2.setPaddingTop(0);
                c2.setVerticalAlignment( Element.ALIGN_TOP );
                c2.setFixedHeight(22);
                c2.setCellEvent(new CT2SummaryScoreGraphicCellEvent( domainTes , reportData.getR2Use(), reportData.p, null, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, false, false, false, true, 1 ) );
                graphT.addCell(c2);

                c = new PdfPCell( graphT );
                //if( dtesl.isEmpty() )
                //    c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                //else
                //    c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.LEFT  | Rectangle.BOTTOM);
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setPadding( 5 );
                c.setPaddingLeft(2*PAD );
                c.setPaddingRight(2*PAD );
                setRunDirection( c );
                t.addCell(c);

                // c.setBackgroundColor( BaseColor.LIGHT_GRAY);

                if( dtesl==null || dtesl.isEmpty() )
                {
                    LogService.logIt( "NqeshFeedbackReport.addCompetencySummaryChart() Domain (combo-competency) " + domainTes.getName() + " does not have any member-competencies. testEventId=" + reportData.getTestEvent().getTestEventId() );
                    continue;
                }

                for( TestEventScore tes : dtesl )
                {
                    if( isLastDomain )
                    {
                        subCt++;
                        if( subCt>=dtesl.size() )
                            isLastSub=true;
                    }

                    scoreStr = I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), scrDigits );

                    c = new PdfPCell( new Phrase( "", font ) );
                    //if( isLastSub )
                    //    c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.TOP | Rectangle.LEFT : Rectangle.BOTTOM | Rectangle.TOP | Rectangle.RIGHT );
                    //else
                    //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 2 );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( removeSquareBrackets(tes.getName(), true), font ) );
                    //if( isLastSub )
                    //    c.setBorder( Rectangle.BOTTOM | Rectangle.TOP );
                    //else
                    //    c.setBorder( Rectangle.TOP );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 2 );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( scoreStr, font ) );
                    //if( isLastSub )
                    //    c.setBorder( Rectangle.BOTTOM | Rectangle.TOP );
                    //else
                    //    c.setBorder( Rectangle.TOP );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);

                    graphT = new PdfPTable( 1 );
                    graphT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
                    c2.setBorder( Rectangle.NO_BORDER );
                    c2.setPadding( 5 );
                    c2.setPaddingTop(0);
                    c2.setVerticalAlignment( Element.ALIGN_TOP );
                    c2.setFixedHeight(22);
                    c2.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, null, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, false, false, false, true, 1 ) );
                    graphT.addCell(c2);

                    c = new PdfPCell( graphT );
                    //if( isLastSub )
                    //    c.setBorder( reportData.getIsLTR() ?  Rectangle.BOTTOM | Rectangle.TOP | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.TOP | Rectangle.LEFT );
                    //else
                    //    c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    c.setPadding( 5 );
                    c.setPaddingLeft(2*PAD );
                    c.setPaddingRight(2*PAD );
                    setRunDirection( c );
                    t.addCell(c);
                }
            }


            float thgt = t.calculateHeights();

            if( thgt> pageHeight )
                t.setHeaderRows( 1 );

            y = addTableToDocument(y, t, false, true );
            // t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            // currentYLevel = y - t.calculateHeights();
            currentYLevel = y - 2*PAD;

            previousYLevel =  currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NqeshFeedbackReport.addCompetencySummaryChart()" );
            throw new STException( e );
        }
    }








    public void addDomainScoreFeedbackTables() throws Exception
    {
        try
        {
            initSpec();

            // Add table for combo and members.
            int totalComps = 0;
            List<TestEventScore> dtesl;
            for( TestEventScore domainTes : this.comboCompList )
            {
                dtesl = memberCompMap.get( domainTes.getName() );
                totalComps += dtesl==null ? 0 : dtesl.size();
            }

            // LogService.logIt( "NqeshFeedbackReport.addDomainScoreFeedbackTables() Total domains: " + comboCompList.size() + ", Total sub-competencies: " + totalComps );

            if( comboCompList.isEmpty() )
            {
                LogService.logIt( "NqeshFeedbackReport.addDomainScoreFeedbackTables() No Competencies found to include in Summary Chart. Not including chart at all" );
                return;
            }

            float thgt;
            int scrDigits;
            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .05f, .6f, .4f} : new float[] { .4f, .6f, .05f};

            // LogService.logIt( "NqeshFeedbackReport.addDomainScoreFeedbackTables() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t;

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, "Individual Domains Detailed Information", null, null, null );

            y -= 2*TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            boolean isLastSub = false;
            int subCt;
            String scoreStr;
            PdfPTable graphT;
            PdfPCell c2;


            List<String> devSugList;
            // Chunk chnk = new Chunk( "Suggested Developmental Interventions:", font );
            // Phrase cHdr = new Phrase();
            // cHdr.add( chnk );
            com.itextpdf.text.List cl;

            PdfPCell cc;
            PdfPTable t3;

            int domainCounter = 0;

            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            boolean useGrayBg = false;
            
            for( TestEventScore domainTes : comboCompList )
            {
                domainCounter++;

                useGrayBg = false;

                t = new PdfPTable( cols );
                t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
                t.setWidths( colRelWids );
                t.setLockedWidth( true );
                t.setHorizontalAlignment( Element.ALIGN_CENTER);
                setRunDirection( t );

                c = t.getDefaultCell();
                c.setPadding( PAD );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );


                scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  domainTes.getScoreFormatType().getScorePrecisionDigits();
                scoreStr = I18nUtils.getFormattedNumber(reportData.getLocale(), domainTes.getScore(), scrDigits );

                // ROW 1 = name, score, graph for domain
                c = new PdfPCell( new Phrase( removeSquareBrackets(domainTes.getName(), true) + "\nScore: " + scoreStr, fontBold ) );
                c.setColspan(2);
                //c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( PAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                setRunDirection( c );
                t.addCell(c);

                graphT = new PdfPTable( 1 );
                graphT.setHorizontalAlignment( Element.ALIGN_CENTER );
                c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
                c2.setBorder( Rectangle.NO_BORDER );
                c2.setPadding( PAD );
                c2.setVerticalAlignment( Element.ALIGN_TOP );
                c2.setFixedHeight(22);
                c2.setCellEvent(new CT2SummaryScoreGraphicCellEvent( domainTes , reportData.getR2Use(), reportData.p, null, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, false, false, false, true, 1 ) );
                graphT.addCell(c2);

                c = new PdfPCell( graphT );
                //c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setPadding( PAD );
                c.setPaddingLeft(2*PAD );
                c.setPaddingRight(2*PAD );
                setRunDirection( c );
                t.addCell(c);

                // Row 2 - Description
                Paragraph p = new Paragraph();
                p.add( new Chunk( removeDomain(domainTes.getName()) + " ", fontBold ) );
                p.add( new Chunk( getDescriptionStr(domainTes), font) );
                c = new PdfPCell( p );
                c.setColspan(3);
                //c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( PAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                setRunDirection( c );
                t.addCell(c);

                dtesl = memberCompMap.get( domainTes.getName() );

                if( dtesl==null || dtesl.isEmpty() )
                {
                    LogService.logIt( "NqeshFeedbackReport.addCompetencySummaryChart() Domain (combo-competency) " + domainTes.getName() + " does not have any member-competencies. testEventId=" + reportData.getTestEvent().getTestEventId() );
                    continue;
                }

                subCt = 0;

                for( TestEventScore tes : dtesl )
                {
                    // dev sugg table
                    devSugList = getDevelopmentSugList( domainTes, tes);
                    // LogService.logIt( "NqeshFeedbackReport.addCompetencySummaryChart() devSugList.size=" + devSugList.size() + ", tes.name=" + tes.getName() );

                    if( devSugList.isEmpty() )
                        continue;

                    cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                    cl.setListSymbol( "\u2022");
                    cl.setIndentationLeft( 10 );
                    cl.setSymbolIndent( 10 );

                    for( String devSug : devSugList )
                    {
                        cl.add( new ListItem( 9,  devSug, font ) );
                    }

                    t3 = new PdfPTable( 1 );
                    setRunDirection( t3 );
                    t3.setWidthPercentage( 100 );
                    cc = new PdfPCell( new Phrase("Suggested Developmental Interventions:", font) );
                    cc.setBorder( Rectangle.NO_BORDER );
                    cc.setPadding(PAD);
                    setRunDirection( cc );
                    cc.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    // cc.addElement( cHdr );
                    t3.addCell( cc );

                    cc = new PdfPCell();
                    cc.setBorder( Rectangle.NO_BORDER );
                    cc.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    cc.setPadding(PAD);
                    setRunDirection( cc );
                    cc.addElement( cl );
                    t3.addCell( cc );

                    subCt++;
                    if( subCt>=dtesl.size() )
                        isLastSub=true;

                    scoreStr = I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), scrDigits );

                    // Sub Row 1 - name, score, graph
                    c = new PdfPCell( new Phrase( "", font ) );
                    //c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.TOP | Rectangle.LEFT : Rectangle.BOTTOM | Rectangle.TOP | Rectangle.RIGHT );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    //c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    c.setPadding( PAD );
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( removeSquareBrackets(tes.getName(), true) + "\nScore: " + scoreStr, font ) );
                    //c.setBorder( Rectangle.BOTTOM | Rectangle.TOP );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    //c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    c.setPadding( PAD );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    setRunDirection( c );
                    t.addCell(c);

                    graphT = new PdfPTable( 1 );
                    graphT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
                    c2.setBorder( Rectangle.NO_BORDER );
                    c2.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    c2.setPadding( PAD );
                    c2.setVerticalAlignment( Element.ALIGN_TOP );
                    c2.setFixedHeight(22);
                    c2.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, null, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, false, false, false, true, 1 ) );
                    graphT.addCell(c2);

                    c = new PdfPCell( graphT );
                    //c.setBorder( reportData.getIsLTR() ?  Rectangle.BOTTOM | Rectangle.TOP | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.TOP | Rectangle.LEFT );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    //c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    c.setPadding( PAD );
                    c.setPaddingLeft(2*PAD );
                    c.setPaddingRight(2*PAD );
                    setRunDirection( c );
                    t.addCell(c);

                    // Sub Row 2 - dev suggestions
                    c = new PdfPCell( new Phrase( "", font ) );
                    //if( isLastSub )
                    //    c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.TOP | Rectangle.LEFT : Rectangle.BOTTOM | Rectangle.TOP | Rectangle.RIGHT );
                    //else
                    //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    //c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    c.setPadding( PAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( t3 );
                    //if( isLastSub )
                    //    c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.TOP | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.TOP | Rectangle.LEFT );
                    //else
                    //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                    c.setColspan(2);
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    //c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    c.setPadding( PAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    setRunDirection( c );
                    t.addCell(c);
                }

                thgt = t.calculateHeights();
                //if( thgt> pageHeight )
                //    t.setHeaderRows( 1 );

                if( domainCounter>1 )
                {
                    this.addNewPage();
                    y = currentYLevel;
                }

                // y = currentYLevel - 2*PAD;

                y = addTableToDocument(y, t, false, true );
                // t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
                y -= 2*PAD;

                // currentYLevel = y - t.calculateHeights();
                currentYLevel = y - 2*PAD;
            }



            previousYLevel =  currentYLevel;


        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshFeedbackReport.addDomainScoreFeedbackTables()" );
            throw e;
        }
    }




    @Override
    protected void addPreparationNotesSection() throws Exception
    {
        try
        {

            Calendar cal = new GregorianCalendar();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");
            String dtStr = df.format( cal.getTime() );

             prepNotes.add( lmsg( "g.SimIdAndVersion", new String[]{ Long.toString( reportData.getTestEvent().getSimId()) , Integer.toString(reportData.getTestEvent().getSimVersionId() ), Long.toString( reportData.getTestEvent().getTestKeyId()), Long.toString( reportData.getTestEvent().getTestEventId()), Long.toString( reportData.getReport().getReportId() ), Integer.toString( reportData.getTestKey().getProductId() ), dtStr } ));

            if( prepNotes.isEmpty() )
                return;

            addNewPage();

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.PreparationNotes" ), null, null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 1f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            if( reportData.getIsLTR() )
            {
                com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );


                //Paragraph cHdr=null;
                //Paragraph cFtr=null;
                //float spcg = 8;
                cl.setListSymbol( "\u2022");

                for( String ct : prepNotes )
                {
                    if( ct==null || ct.isEmpty() )
                        continue;

                    cl.add( new ListItem( new Paragraph( ct , getFont() ) ) );
                }

                c = new PdfPCell();
                c.setBorder( Rectangle.BOX );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPaddingTop( 8 );
                c.setPaddingLeft(10);
                c.setPaddingRight(5);
                c.setPaddingBottom( 14 );
                c.addElement( cl );
                setRunDirection( c );
                t.addCell(c);
            }

            else
            {
                PdfPTable tt = new PdfPTable( new float[] { 1f } );

                // t.setHorizontalAlignment( Element.ALIGN_CENTER );
                tt.setTotalWidth( outerWid - 20 );
                tt.setLockedWidth( true );
                setRunDirection( tt );

                c = tt.getDefaultCell();
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 5 );
                setRunDirection( c );

                for( String ct : prepNotes )
                {
                    if( ct.isEmpty() )
                        continue;

                    tt.addCell( new Phrase(ct, getFont() ) );
                }

                c = new PdfPCell();
                c.setBorder( Rectangle.BOX );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPaddingTop( 8 );
                c.setPaddingLeft(10);
                c.setPaddingRight(5);
                c.setPaddingBottom( 14 );
                c.addElement( tt );
                setRunDirection( c );
                t.addCell(c);

            }

            currentYLevel = addTableToDocument(y, t, false, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NqeshFeedbackReport.addPreparationNotesSection()" );

            throw new STException( e );
        }
    }



    private String getNameStub( String name )
    {
        if( name == null )
            return "";

        return StringUtils.alphaCharsOnly(name).toLowerCase();
    }




    private void specialInit()
    {
        try
        {
            if( this.nqeshReportUtils== null )
            {

                if( reportData.getReport().getStrParam6() !=null && !reportData.getReport().getStrParam6().isEmpty() )
                    bundleToUse = reportData.getReport().getStrParam6();

                if( bundleToUse==null || bundleToUse.isEmpty() )
                {
                    Locale loc = reportData.getLocale();

                    defaultBundleToUse = "nqeshfeedback.properties";

                    String stub = "";
                    if( loc.getLanguage().equalsIgnoreCase( "en" ) )
                        bundleToUse = "nqeshfeedback.properties";
                    else
                        bundleToUse = "nqeshfeedback_" + loc.getLanguage().toLowerCase() + ".properties";

                }

                nqeshReportUtils = new NqeshReportUtils( bundleToUse, defaultBundleToUse );
            }

            if( reportData.te.getSimXmlObj()==null )
            {
                EventFacade eventFacade = EventFacade.getInstance();
                reportData.te.setSimDescriptor( eventFacade.getSimDescriptor( reportData.te.getSimId(), reportData.te.getSimVersionId(), false ));
                reportData.te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( reportData.te.getSimDescriptor().getXml() ));
            }
        }
        catch( Exception e )
        {

        }
    }

    protected boolean doesTestEventScoreBelongToCombo( TestEventScore domainTes, TestEventScore tes ) throws Exception
    {
        SimJ.Simcompetency csc = null;
        SimJ.Simcompetency memberSc = null;
        for( SimJ.Simcompetency sc : reportData.getTestEvent().getSimXmlObj().getSimcompetency() )
        {
            if( SimCompetencyClass.getValue(sc.getClassid()).getIsCombo() && StringUtils.getUrlDecodedValue( sc.getName()).equals( domainTes.getName() ))
            {
                csc = sc;
            }

            if( !SimCompetencyClass.getValue(sc.getClassid()).getIsCombo() && StringUtils.getUrlDecodedValue( sc.getName()).equals( tes.getName() ))
            {
                memberSc = sc;
            }

            if( csc!=null && memberSc!=null )
                break;
        }

        if( csc==null )
            throw new Exception( "Unable to find SimCompetency matching domain TES.name=" + domainTes.getName() );

        if( memberSc==null )
            throw new Exception( "Unable to find SimCompetency matching member TES.name=" + tes.getName() +", for domain TES.name=" + domainTes.getName() );

        String combinationsimcompetencyids = csc.getCombinationsimcompetencyids();

        if( combinationsimcompetencyids==null )
            throw new Exception( "Domain TES.name=" + domainTes.getName() + " sim competency does not have any combinationsimcompetencyids in SimJ.simcompetency object" );

        for( String scid : combinationsimcompetencyids.split(",") )
        {
            if( Long.parseLong(scid)==memberSc.getId() )
                return true;
        }

        return false;
    }


    protected String getDescriptionStr( TestEventScore tes )
    {

        String key = "description." + getNameStub( tes.getName() );

        String out = this.lmsg_spec(key);

        if( out!=null && out.startsWith("KEY NOT FOUND" ) )
            out = null;

        if( out==null || out.isEmpty() )
        {
            if( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() && !tes.getNameEnglish().equals( tes.getName() ) )
            {
                key =  "description." + getNameStub( tes.getNameEnglish() ); // "scoresummary." + getNameStub( tes.getNameEnglish() ) + "."  + category;
                out = this.lmsg_spec(key);

                if( out!=null && out.startsWith("KEY NOT FOUND" ) )
                    out = null;
            }
        }

        if( out==null )
            out = "";

        out=out.trim();

        return out;
    }


    protected java.util.List<String> getDevelopmentSugList( TestEventScore domainTes, TestEventScore tes )
    {
        java.util.List<String> out = new ArrayList<>();

        String scoreStub = getScoreStub( tes.getScore() );

        out.addAll( getTipsStrList( tes ) );

        out.addAll( getScoreSummaryStrList( tes, scoreStub ) );

        return out;
    }

    protected String getScoreStub( float score )
    {
        if( score<33.33f )
            return "low";
        if( score<66.66f )
            return "medium";
        return "high";
    }

    protected java.util.List<String> getScoreSummaryStrList( TestEventScore tes, String stub )
    {
        java.util.List<String> out = new ArrayList<>();

        String key = "scoresummary." + getNameStub( tes.getName() ) + "." + stub;
        //String keyEng = "scoresummary." + getNameStub( tes.getName() ) + "." + stub;

        int counter = 1;
        // LogService.logIt( "NqeshFeedbackReport.getScoreSummaryStrList() tes.name=" + tes.getName() + ", key=" + key + " stub=" + stub);
        String val = lmsg_spec(key + "." + counter );
        //if( val==null || val.isBlank() || val.contains("KEY NOT FOUND") )
        //    val = lmsg_spec(keyEng + "." + counter );

        while( val!=null && !val.isBlank() && !val.contains("KEY NOT FOUND") )
        {
            out.add(val );
            counter++;
            val = lmsg_spec(key + "." + counter );
            //if( val==null || val.isBlank() || val.contains("KEY NOT FOUND") )
            //    val = lmsg_spec(keyEng + "." + counter );
        }

        return out;
    }


    protected java.util.List<String> getTipsStrList( TestEventScore tes )
    {
        java.util.List<String> out = new ArrayList<>();

        String key = "tips." + getNameStub( tes.getName() );
        String keyEng = "tips." + getNameStub( tes.getName() );

        int counter = 1;
        String tip = lmsg_spec(key + "." + counter );
        if( tip==null || tip.isBlank() || tip.contains("KEY NOT FOUND") )
            tip = lmsg_spec(keyEng + "." + counter );

        while( tip!=null && !tip.isBlank() && !tip.contains("KEY NOT FOUND") )
        {
            out.add( tip );
            counter++;
            tip = lmsg_spec(key + "." + counter );
            if( tip==null || tip.isBlank() || tip.contains("KEY NOT FOUND") )
                tip = lmsg_spec(keyEng + "." + counter );
        }

        return out;
    }













    public String lmsg_spec( String key )
    {
        specialInit();

        return nqeshReportUtils.getKey(key );
    }

    public String lmsg_spec( String key, String[] prms )
    {
        specialInit();

        String msgText = nqeshReportUtils.getKey(key );

        return MessageFactory.substituteParams( reportData.getLocale() , msgText, prms );
    }


    public String removeSquareBrackets( String inStr, boolean addColon)
    {
        if( inStr==null )
            return inStr;

        inStr = StringUtils.removeChar(inStr, '[');

        if( addColon )
            inStr = StringUtils.replaceChar(inStr, ']', ":" );
        else
            inStr = StringUtils.removeChar(inStr, ']');

        return inStr;
    }

    public String removeDomain( String inStr )
    {
        if( inStr==null || !inStr.contains("]"))
            return inStr;
        return inStr.substring(inStr.indexOf(']')+1, inStr.length()).trim();
    }


}
