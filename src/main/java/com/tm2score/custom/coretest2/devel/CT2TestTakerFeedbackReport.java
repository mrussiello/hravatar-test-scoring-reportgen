/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2.devel;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class CT2TestTakerFeedbackReport extends BaseCT2ReportTemplate implements ReportTemplate
{
    public String bundleToUse = null;
    public String defaultBundleToUse = null;
    
    public CT2DevelopmentReportUtils developmentReportUtils;
    
    
    
    public CT2TestTakerFeedbackReport()
    {
        super();
        
        this.devel = true;
        // this.redYellowGreenGraphs=true;

    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            specialInit();
            
            // LogService.logIt( "CT2TestTakerFeedbackReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );

            addCoverPageV2(true);
            
            addNewPage();
            
            addReportInfoHeader();
            
            for( int i=1;i<=5;i++ )
                addCustomInfo( i );

            addAbilitiesInfo();

            addKSInfo();

            addAIMSInfo();

            addEQInfo();
            
            addBiodataInfo();
            
            addPreparationNotesSection();
            
            //addNewPage();

            //addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2TestTakerFeedbackReport.generateReport() " );

            throw new STException( e );
        }
    }

    
    @Override
    public void addReportInfoHeader() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel;
            
            float y = addTitle(previousYLevel, lmsg( "g.Introduction" ), null, null, null );

            y -= TPAD;
                        
            TestEventScore tes = reportData.getTestEvent().getOverallTestEventScore();

            int cols = 2;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f } : new float[] { .2f, .45f };

            // String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            //boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();
            boolean includeCompanyInfo = !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
                        
            // boolean includePreparedFor = includeCompanyInfo && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            setRunDirection(t );
            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            // t.setHeaderRows( 1 );


            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.AssessmentInformation"), fontLargeWhite ) );
            c.setColspan(2);
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            t.addCell(c);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );


            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setColspan(cols);
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            t.addCell( c );

            // NAME
            c = new PdfPCell( new Phrase( lmsg( "g.ParticipantC" ), getFontLight() ) );
            c.setColspan(1);
            c.setPadding( 1 );
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            t.addCell( c );
                        
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            c.setColspan(1);
            c.setPadding(1);
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            t.addCell( c );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setColspan(cols);
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            t.addCell( c );
            
            // Test
            c = new PdfPCell( new Phrase( lmsg( "g.AssessmentC" ), getFontLight() ) );
            c.setColspan(1);
            c.setPadding( 1 );
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            t.addCell( c );
                        
            c = new PdfPCell( new Phrase( reportData.getSimName(), getFontLight() ) );
            c.setColspan(1);
            c.setPadding(1);
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            t.addCell( c );

            // Date
            c = new PdfPCell( new Phrase( lmsg( "g.CompletedC" ), getFontLight() ) );
            c.setColspan(1);
            c.setPadding( 1 );
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            t.addCell( c );
                        
            c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFontLight() ) );
            c.setColspan(1);
            c.setPadding(1);
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            t.addCell( c );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setColspan(cols);
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            t.addCell( c );
            
            
            // Employer
            if( includeCompanyInfo )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.SponsoringEmpC" ), getFontLight() ) );
                c.setColspan(1);
                c.setPadding( 1 );
                c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPaddingBottom( 2 );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( reportData.getOrgName(), getFontLight() ) );
                c.setColspan(1);
                c.setPadding(1);
                c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPaddingBottom( 2 );
                setRunDirection( c );
                t.addCell( c );
            }
            
            t.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - t.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2TestTakerFeedbackReport.addReportInfoHeader()" );

            throw e;
        }
    }

    
    @Override
    public void addAnyCompetenciesInfo(  java.util.List<TestEventScore> teslst, String titleKey, String titleText, String subtitleKey, String subtitleText, String detailKey, String descripKey, String caveatHeaderKey, String caveatFooterKey, boolean singleColumn, boolean withInterview, boolean noInterviewLimit, boolean repeatHeadersNewPages) throws Exception
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

            SimCompetencyClass scc = null;

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
                
                if( scc==null )
                    scc =  SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );
            }

            if( tesl.isEmpty() )
                return;

            // LogService.logIt( "CT2TestTakerFeedbackReport.addAnyCompetenciesInfo() titleKey=" + titleKey + ", subtitleKey=" + subtitleKey + ", )subtitle=" + (subtitleKey==null ? "" : lmsg( subtitleKey ) )  );
            
            String title = titleText!=null && !titleText.isBlank() ? titleText : getSubstituteTitleStr( scc );
                    
            if( title==null || title.isEmpty() )        
                   title = titleKey==null || titleKey.isEmpty() ?  null : lmsg( titleKey );
            
            
            String subtitle = subtitleText!=null && !subtitleText.isBlank() ? subtitleText : getSubstituteLangStr(scc, "subtitle." );
                    
            if( subtitle==null || subtitle.isEmpty() )        
                   subtitle = subtitleKey==null || subtitleKey.isEmpty() ?  null : lmsg( subtitleKey );
                        
            currentYLevel = addTitle(previousYLevel, title, subtitle, null, null );
            
            java.util.List<TestEventScore> tesl2 = new ArrayList<>();
            
            String tipStr;
            String tipLinkStr;
            
            TestEventScore tes;
            boolean last = false;
            String nameTitleStr;
            
            for( int i=0; i<tesl.size(); i++ )
            {
                tes = tesl.get(i);
                
                if(i==tesl.size()-1)
                    last=true;
                
                tesl2.add( tes );
                
                tipStr = getTipsStr( tes, last );
                tipLinkStr = getTipLinkStr(tes, last );

                if( last || (tipStr!=null && !tipStr.isBlank()) || (tipLinkStr!=null && !tipLinkStr.isBlank()) )
                {
                    scc =  SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );
                    
                    nameTitleStr = this.getSubstituteLangStr(scc, "titlename." );
                    
                    addTesGroupCompetencyInfoToDocument(tesl2, tipStr, tipLinkStr, nameTitleStr, caveatHeaderKey, caveatFooterKey );  
                    
                    if( !last )
                        tesl2 = new ArrayList<>();
                }                    
            } // each competency
        }

        catch( Exception e )
        {
            LogService.logIt(e, "CT2TestTakerFeedbackReport.addAnyCompetenciesInfo() titleKey=" + titleKey );

            throw new STException( e );
        }
    }
    
    
    
    
    protected void addTesGroupCompetencyInfoToDocument( java.util.List<TestEventScore> tesl, String tipStr, String tipLinkStr, String nameTitleStr, String caveatHeaderKey, String caveatFooterKey) throws Exception
    {
        try
        {
            boolean graphic = reportData.getReport().getIncludeCompetencyColorScores()==1 && !reportData.getReportRuleAsBoolean( "cmptygrphoff" ); // reportData.getR2Use().getIncludeCompetencyColorScores()==1; // true;

            // First, add a table
            PdfPTable t = new PdfPTable( reportData.getIsLTR() ? new float[] { 3.5f, 5.5f } : new float[] { 5.5f, 3.5f } );
            PdfPCell c;

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            
            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );

            c = new PdfPCell(new Phrase( nameTitleStr==null || nameTitleStr.isEmpty() ? lmsg( "g.Name" ) : nameTitleStr , fontLmWhite));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell(new Phrase( lmsg( "g.Summary" ), fontLmWhite));
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            // Add header row.
            //t.addCell(new Phrase( nameTitleStr==null || nameTitleStr.isEmpty() ? lmsg( "g.Name" ) : nameTitleStr , fontLmWhite) );            
            //t.addCell( new Phrase( lmsg( "g.Summary" ) , fontLmWhite ) );
              
            TestEventScore tes;
            boolean last = false;
            PdfPTable compT;
            ScoreCategoryType sct;
            
            String descrip;
            String link;
            Paragraph par;
            
            //String scoreText;
            
            StringBuilder csb;
            String cHdrStr;
            String cFtrStr;
            String cssb;
                
            // String ctxt = reportData.getReportRule( "hidecaveats" );
            boolean hideCaveats = reportData.getReportRuleAsBoolean(  "hidecaveats" );  // ctxt != null && ctxt.equals( "1" );
                        
            for( int i=0; i<tesl.size(); i++ )
            {
                tes = tesl.get(i);
                
                if( i==tesl.size()-1 )
                    last=true;
                
                c.setBackgroundColor( BaseColor.WHITE );

                sct = tes.getScoreCategoryType();

                // First do the score info.
                compT = new PdfPTable( reportData.getIsLTR() ? new float[] {6,4} :  new float[] {4,6} );
                compT.setHorizontalAlignment( Element.ALIGN_CENTER );
                compT.setTotalWidth( 0.9f*outerWid*3.5f/9f );
                compT.setLockedWidth(true);
                setRunDirection( compT );

                c = compT.getDefaultCell();
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                c.setPadding( 1 );

                
                //Phrase pg;
                // java.util.List<String[]> topicCaveatList = ReportUtils.getParsedTopicScores(reportData.getCaveatList(tes), reportData.getLocale(), tes.getSimCompetencyClassId() );
                java.util.List<CaveatScore> topicCaveatList = reportData.getTopicCaveatScoreList(tes);

                csb = new StringBuilder();
                cHdrStr =( caveatHeaderKey != null && !caveatHeaderKey.isEmpty() ? lmsg( caveatHeaderKey ) + "\n" : "" );
                cFtrStr = ( caveatFooterKey != null && !caveatFooterKey.isEmpty() ? "\n" + lmsg( caveatFooterKey ) : "" );                
                if( topicCaveatList.size()==1 )
                {                                        
                    String[] ct = ReportUtils.parseTopicCaveatScore(topicCaveatList.get(0), true, tes.getSimCompetencyClassId()); // topicCaveatList.get(0);
                    
                    csb.append( "\u2022 " + ct[1] + ": " + ct[2] ); 
                    
                    topicCaveatList.clear();
                }
                
                cssb = csb.length()>0 ?  cHdrStr + csb.toString() + cFtrStr : "";

                //if( !cssb.isEmpty() && !hideCaveats )
                //    scoreText += "\n\n" + cssb;
                
                
                // Name Cell
                c = new PdfPCell( new Phrase( tctrans(reportData.getCompetencyName(tes),false)  , getFontBold() ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setColspan(2);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( 1 );
                setRunDirection( c );
                compT.addCell( c );

                // ScoreGraphCell
                if( graphic )
                {
                    c = new PdfPCell( new Phrase( "" , fontXLarge ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan( 2 );
                    c.setPadding( 9 );

                    if( graphic && tes.getIncludeNumericScoreInResults() )
                    {
                        c.setFixedHeight(16);
                        c.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, devel, false, false, false, 0 ) );
                    }
                    //numGrphT.addCell( c );
                    setRunDirection( c );
                    compT.addCell( c );
                }

                descrip = getDescriptionStr(tes);
                
                if( descrip==null || descrip.isEmpty() )
                    descrip = reportData.getCompetencyDescription( tes ); 

                if( descrip==null )
                    descrip="";
                
                if( !cssb.isEmpty() && !hideCaveats )
                    descrip += ( descrip.isEmpty() ? "" : "\n\n" ) + cssb;
                
                if( !descrip.isEmpty() )
                {
                    c = new PdfPCell( new Phrase( tctrans(descrip,false)  , getFontSmall() ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan(2);
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( 1 );
                    c.setPaddingTop(6);
                    setRunDirection( c );
                    compT.addCell( c );                
                }

                // add to table.
                c = new PdfPCell();
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setPadding(3);
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.addElement( compT );
                setRunDirection( c );
                t.addCell( c );

                String summaryText = getSummaryText( tes, sct, true );

                if( summaryText == null )
                    summaryText = "";

                summaryText = tctrans( summaryText, false );

                link = getCompetencyLinkStr(tes);
                
                par = new Paragraph();
                par.add( new Phrase( summaryText, getFontSmall() ) );
                
                if( link!=null && !link.isBlank() )
                {
                    String tipsIntro = lmsg_spec( "competencylink.titleC" );

                    Chunk chk = new Chunk( "\n\n" + tipsIntro + " ", getFontSmall() );                
                    par.add( chk );

                    chk = new Chunk( link,fontSmallItalicBlue );
                    PdfAction pdfa = PdfAction.gotoRemotePage( link , lmsg("b.Click2Visit"), false, true );                                
                    chk.setAction( pdfa );
                    par.add( chk );                                
                }
                c = new PdfPCell( par );
                
                c.setBackgroundColor( BaseColor.WHITE );
                // c.setBorder( Rectangle.BOX );
                if( last )  
                    c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );                    
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );
            }
            
            // String tipsStr = getTipsStr( tes, false );
            
            if( tipStr!=null && !tipStr.isEmpty() )
            {
                c = new PdfPCell( new Phrase( "", this.getFontXLargeWhite()) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 0 );
                c.setPaddingTop( 2 );
                c.setPaddingBottom( 2 );
                setRunDirection( c );
                t.addCell(c);
                
                String tipsIntro = this.lmsg_spec( "tips.titleC" );
                
                Chunk chk = new Chunk( tipsIntro, this.getFontBold() );
                
                Paragraph pg = new Paragraph();
                
                pg.add( chk );
                
                chk = new Chunk( " " + tipStr, this.getFont() );                
                
                pg.add( chk );                
                
                c = new PdfPCell( pg );
                c.setColspan(2);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 0 );
                c.setPaddingTop( 4 );
                c.setPaddingBottom( 10 );
                setRunDirection( c );
                t.addCell(c);
            }
            
            if( tipLinkStr!=null && !tipLinkStr.isBlank() )
            {
                c = new PdfPCell( new Phrase( "", this.getFontXLargeWhite()) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 0 );
                c.setPaddingTop( 2 );
                c.setPaddingBottom( 2 );
                setRunDirection( c );
                t.addCell(c);
                
                String tipsIntro = lmsg_spec( "tiplink.titleC" );
                
                Chunk chk = new Chunk( tipsIntro + " ", this.getFontBold() );                
                Paragraph pg = new Paragraph();                
                pg.add( chk );
                
                chk = new Chunk( tipLinkStr,fontSmallItalicBlue );
                PdfAction pdfa = PdfAction.gotoRemotePage( tipLinkStr , lmsg("b.Click2Visit"), false, true );                                
                chk.setAction( pdfa );
                pg.add( chk );                                
                
                c = new PdfPCell( pg );
                c.setColspan(2);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 0 );
                c.setPaddingTop( 4 );
                c.setPaddingBottom( 10 );
                setRunDirection( c );
                t.addCell(c);
            }
            
            
            currentYLevel = addTableToDocument(currentYLevel, t, true, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2TestTakerFeedbackReport.addTesGroupCompetencyInfoToDocument() " );

            throw new STException( e );
        }
    }
    
    
    
    @Override
    protected void addPreparationNotesSection() throws Exception
    {
        try
        {
            // Integer tempInteger = ReportUtils.getReportFlagIntValue( "suppressonetinfoinreports", reportData.getTestKey(), reportData.getSuborg(), reportData.getOrg(), reportData.getReport() );            
            boolean suppressOnet = reportData.getReportRuleAsBoolean( "suppressonetinfoinreports" ); //  tempInteger==null || tempInteger.intValue()!=1 ? false : true;
            
            // LogService.logIt(  "BaseCT2ReportTemplate.addPreparationNotesSection() START" );
            prepNotes.add( 0, lmsg( "g.CT3RptCaveatDevel" ) );
            prepNotes.add( lmsg( "g.CT3RptGraphKeyFbk" ) );
            prepNotes.add( lmsg( "g.CT3RptFbkDisagree" ) );


            /*
            Product p = reportData.getTestEvent().getProduct();
            if( !suppressOnet && p != null && p.getOnetSoc()!=null && !p.getOnetSoc().isEmpty() )
            {
                prepNotes.add( lmsg( "g.OnetDescrip", null ));

                prepNotes.add( lmsg( "g.OnetSocX", new String[]{p.getOnetSoc()} ));

                if( p.getOnetVersion()!=null && !p.getOnetVersion().isEmpty() )
                    prepNotes.add( lmsg( "g.OnetVersionX", new String[]{p.getOnetVersion()} ));
            }
            */
            
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
            LogService.logIt( e, "CT2TestTakerFeedbackReport.addPreparationNotesSection()" );

            throw new STException( e );
        }
    }


    protected String getSubstituteTitleStr( SimCompetencyClass scc )
    {
        if( scc==null )
            return null;
        
        String stub = "";
        
        if( scc.getIsBiodata() )
            stub = "biodata";
        else if( scc.isAbility())
            stub = "ability";
        else if( scc.isAIMS())
            stub = "aims";
        else if( scc.isEQ() )
            stub = "eq";
        else if( scc.isKS() )
            stub = "ks";
        
        if( stub.isEmpty() )
            return null;
        
        String v = this.lmsg_spec( "title." + stub );
        
        if( v==null || v.startsWith("KEY NOT FOUND") )
            return null;
        
        return v.trim();
    }
    

    
    protected String getSubstituteLangStr( SimCompetencyClass scc, String prefix)
    {
        if( scc==null )
            return null;
        
        String stub = "";
        
        if( scc.getIsBiodata() )
            stub = "biodata";
        else if( scc.isAbility())
            stub = "ability";
        else if( scc.isAIMS())
            stub = "aims";
        else if( scc.isEQ() )
            stub = "eq";
        else if( scc.isKS() )
            stub = "ks";
        
        if( stub.isEmpty() )
            return null;
        
        String v = this.lmsg_spec( prefix + stub );
        
        if( v==null || v.startsWith("KEY NOT FOUND") )
            return null;
        
        return v.trim();
    }
    
    
    
    /**
     * Summary Text is score-category and score dependant. 
     *   All competencies - use score. 
     * 
     * 
     * @param tes
     * @param sct
     * @return 
     */
    public String getSummaryText( TestEventScore tes, ScoreCategoryType sct, boolean includeGeneric )
    {
        // TODOD
        String category = "veryhigh";
        
        if( tes.getScore() < 35f )
            category = "verylow";
        
        else if( tes.getScore() < 50f )
            category = "low";
        
        else if( tes.getScore() < 65f )
            category = "medium";
        
        else if( tes.getScore() < 80f )
            category = "high";
        
        String key = "scoresummary." + getNameStub( tes.getName() ) + "."  + category;

        String out = this.lmsg_spec(key);
        
        if( out!=null && out.startsWith("KEY NOT FOUND" ) )
            out = null;
        
        if( out==null || out.isEmpty() )
        {
            if( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() && !tes.getNameEnglish().equals( tes.getName() ) )
            {
                key = "scoresummary." + getNameStub( tes.getNameEnglish() ) + "."  + category;
                out = lmsg_spec(key);
                
                if( out!=null && out.startsWith("KEY NOT FOUND" ) )
                    out = null;        
            }
        }
        
        if( out==null )
            out = "";
        
        out=out.trim();
        
        if( out.isEmpty() && includeGeneric )
        {
            LogService.logIt( "CT2TestTakerFeedbackReport.getSummaryText()  Unable to find score summary for tes.name=" + tes.getName() + ", nameEnglish=" + tes.getNameEnglish() + " score=" + tes.getScore() + ", scorestub=" + category  + ", lastkey=" + key + " shifting to generic for class." );
            
            SimCompetencyClass scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );
            
            String gk = null;
            
            if( scc.isAIMS() || scc.isEQ() || scc.getIsBiodata() )
                gk="noncognitive";
            else if( scc.isAbility() )
                gk="ability";
            else if( scc.isEQ())
                gk="eq";
            else if( scc.isCoreSkill())
                gk="skill";
            else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
                gk="writing";
            else if( scc.isKS( ))
                gk="skill";
            else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
                gk="writing";
            
            if( gk==null )
                gk="noncognitive";
            
            if( gk!=null )
            {
                key = "scoresummary." + gk + "."  + category;
                out = lmsg_spec(key);
                
                if( out!=null && out.startsWith("KEY NOT FOUND" ) )
                    out = null;
        
                if( out==null )
                    out = "";
                
                out=out.trim();
            }

            if( out.isEmpty() )
                LogService.logIt( "CT2TestTakerFeedbackReport.getSummaryText()  Unable to find GENERIC score summary for tes.name=" + tes.getName() + ", nameEnglish=" + tes.getNameEnglish() + " score=" + tes.getScore() + ", scorestub=" + category  + ", lastkey=" + key + " Returning empty." );
        }
        
        return out;
    }
    
    
    
    private String getNameStub( String name )
    {
        if( name == null )
            return "";
        
        return StringUtils.alphaCharsOnly(name).toLowerCase();        
    }
    
    
    
    
    private void specialInit()
    {
        if( this.developmentReportUtils== null )
        {

            if( reportData.getReport().getStrParam6() !=null && !reportData.getReport().getStrParam6().isEmpty() )
                bundleToUse = reportData.getReport().getStrParam6();

            if( bundleToUse==null || bundleToUse.isEmpty() )
            {
                Locale loc = reportData.getLocale();
                
                defaultBundleToUse = "candidatefeedback.properties";
                
                // String stub = "";
                if( loc.getLanguage().equalsIgnoreCase( "en" ) )                
                    bundleToUse = "candidatefeedback.properties";   
                else
                    bundleToUse = "candidatefeedback_" + loc.getLanguage().toLowerCase() + ".properties";   
                    
            }

            developmentReportUtils = new CT2DevelopmentReportUtils( bundleToUse, null, defaultBundleToUse );
        }        
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
    

    protected String getCompetencyLinkStr( TestEventScore tes )
    {
        
        String key = "competencylink." + getNameStub( tes.getName() );

        String out = this.lmsg_spec(key);
        
        if( out!=null && out.startsWith("KEY NOT FOUND" ) )
            out = null;
        
        if( out==null || out.isEmpty() )
        {
            if( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() && !tes.getNameEnglish().equals( tes.getName() ) )
            {
                key =  "competencylink." + getNameStub( tes.getNameEnglish() ); // "scoresummary." + getNameStub( tes.getNameEnglish() ) + "."  + category;
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

    
    protected String getTipsStr( TestEventScore tes, boolean includeGeneric )
    {
        
        String key = "tips." + getNameStub( tes.getName() );

        String out = this.lmsg_spec(key);
        
        if( out!=null && out.startsWith("KEY NOT FOUND" ) )
            out = null;
        
        if( out==null || out.isEmpty() )
        {
            if( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() && !tes.getNameEnglish().equals( tes.getName() ) )
            {
                key =  "tips." + getNameStub( tes.getNameEnglish() ); // "scoresummary." + getNameStub( tes.getNameEnglish() ) + "."  + category;
                out = this.lmsg_spec(key);
                
                if( out!=null && out.startsWith("KEY NOT FOUND" ) )
                    out = null;
            }
        }
        
        if( out==null )
            out = "";
        
        out=out.trim();
        
        if( out.isEmpty() && includeGeneric )
        {
            LogService.logIt( "CT2TestTakerFeedbackReport.getTipsStr()  Unable to find TIPS for tes.name=" + tes.getName() + ", nameEnglish=" + tes.getNameEnglish() + " score=" + tes.getScore()  + ", lastkey=" + key + " shifting to generic for class." );
            
            SimCompetencyClass scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );
            
            String gk = null;
            
            if( scc.isAIMS() || scc.isEQ() || scc.getIsBiodata() )
                gk="noncognitive";
            else if( scc.isAbility() )
                gk="ability";
            else if( scc.isEQ())
                gk="eq";
            else if( scc.isCoreSkill())
                gk="skill";
            else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
                gk="writing";
            else if( scc.isKS( ))
                gk="skill";
            else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
                gk="writing";
            
            if( gk==null )
                gk="noncognitive";
            
            if( gk!=null )
            {
                key = "tips." + gk; // 
                out = lmsg_spec(key);
                
                if( out!=null && out.startsWith("KEY NOT FOUND" ) )
                    out = null;
                
                if( out==null )
                    out = "";
                
                out=out.trim();
            }

            if( out.isEmpty() )
                LogService.logIt( "CT2TestTakerFeedbackReport.getTipsStr()  Unable to find GENERIC TIPS for tes.name=" + tes.getName() + ", nameEnglish=" + tes.getNameEnglish() + " score=" + tes.getScore()  + ", lastkey=" + key + " Returning empty." );
        }
        
        return out;
    }
    
    protected String getTipLinkStr( TestEventScore tes, boolean includeGeneric )
    {
        
        String key = "tiplink." + getNameStub( tes.getName() );

        String out = lmsg_spec(key);
        
        if( out!=null && out.startsWith("KEY NOT FOUND" ) )
            out = null;
        
        if( out==null || out.isEmpty() )
        {
            if( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() && !tes.getNameEnglish().equals( tes.getName() ) )
            {
                key =  "tiplink." + getNameStub( tes.getNameEnglish() ); // "scoresummary." + getNameStub( tes.getNameEnglish() ) + "."  + category;
                out = lmsg_spec(key);
                
                if( out!=null && out.startsWith("KEY NOT FOUND" ) )
                    out = null;
            }
        }
        
        if( out==null )
            out = "";
        
        out=out.trim();
        
        if( out.isEmpty() && includeGeneric )
        {
            SimCompetencyClass scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );
            
            String gk = null;
            
            if( scc.isAIMS() || scc.isEQ() || scc.getIsBiodata() )
                gk="noncognitive";
            else if( scc.isAbility() )
                gk="ability";
            else if( scc.isEQ())
                gk="eq";
            else if( scc.isCoreSkill())
                gk="skill";
            else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
                gk="writing";
            else if( scc.isKS( ))
                gk="skill";
            else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
                gk="writing";
            
            if( gk==null )
                gk="noncognitive";
            
            if( gk!=null )
            {
                key = "tiplink." + gk; // 
                out = lmsg_spec(key);
                
                if( out!=null && out.startsWith("KEY NOT FOUND" ) )
                    out = null;
                
                if( out==null )
                    out = "";
                
                out=out.trim();
            }
        }        
        
        return out;
    }
    
    
    @Override
    public boolean includesOverall()
    {
        return false;
    }
    
    
    @Override
    public boolean includesCompSummary()
    {
        return false;
    }
    
    @Override
    public boolean includesComparison()
    {
        return false;
    }
    
    
    @Override
    public boolean includesAvUploads()
    {
        return false;
    }
    
    
    public String lmsg_spec( String key )
    {
        specialInit();
        
        return developmentReportUtils.getKey(key );
    }

    public String lmsg_spec( String key, String[] prms )
    {
        specialInit();
        
        String msgText = developmentReportUtils.getKey(key );
        
        return MessageFactory.substituteParams( reportData.getLocale() , msgText, prms );
    }
    
    
    
}
