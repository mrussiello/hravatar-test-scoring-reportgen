/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2.devel;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.user.User;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class CT2DevelopmentReport extends BaseCT2ReportTemplate implements ReportTemplate
{
    String assessoverviewtext = null;

    String howtousereport = null;
    String howtousereport2 = null;
    
    String workcompetenciesintro = null;
    String workcompetenciesintro2 = null;

    JsonObject overallJo = null;

    Map<String,JsonObject> competencyMap = null;
    
    
    
    
    public CT2DevelopmentReport()
    {
        super();
        
        this.devel = true;
        // this.redYellowGreenGraphs=false;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            
            // LogService.logIt( "CT2DevelopmentReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );

            parseJson();

            addCoverPageV2(true);
            
            addNewPage();

            addHowUseReportSection();

            addOverallSection();

            // addNewPage();
            
            addCompetencySummaryChart();

            addComparisonSection();

            addNewPage();

            addCompetencyDetailInfo();
            
            addPreparationNotesSection();
            
            if( !reportData.getReportRuleAsBoolean( "usernotesoff" ) )
                addNewPage();

            addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2DevelopmentReport.generateReport() " );

            throw new STException( e );
        }
    }


    
    protected void addCompetencyDetailInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.NONCOGNITIVE ); // new ArrayList<>();

            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.INTERESTS_COMBO ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.NONCOG_COMBO ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY_COMBO ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CORESKILL ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SKILL_COMBO ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.KNOWLEDGE ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.EQ ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CUSTOM ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CUSTOM_COMBO ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CUSTOM2 ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CUSTOM3) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CUSTOM4 ) );
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CUSTOM5 ) );
            
            if( tesl.size() <= 0 )
                return;
            
            Collections.sort(tesl, new DisplayOrderComparator() );  // new TESNameComparator() );
            

            // LogService.logIt( "CT2DevelopmentReport.addCompetencyDetailInfo() found " + tesl.size() );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
            
            addAnyCompetenciesInfo(tesl, "g.AIMSTitleDevel", ttext, "g.AIMSSubtitleDevel", sttext, "g.Detail", "g.Description", null, null, false, true, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2DevelopmentReport.addCompetencyDetailInfo()" );

            throw new STException( e );
        }
    }
    
    
    
    
    public void addAnyCompetenciesInfo( java.util.List<TestEventScore> teslst, String titleKey, String titleText, String subtitleKey, String subtitleText, String detailKey, String descripKey, String caveatHeaderKey, String caveatFooterKey, boolean singleColumn, boolean withInterview, boolean noInterviewLimit, boolean repeatHeadersNewPages) throws Exception
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


            boolean numeric = this.reportData.getReport().getIncludeSubcategoryNumeric()==1;
            // boolean rating = this.reportData.getReport().getIncludeSubcategoryCategory()==1;
            // boolean interpretation = reportData.getReport().getIncludeSubcategoryInterpretations()==1;

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
            if( titleText==null )
                titleText = lmsg( titleKey );
            
            if( subtitleText==null )
                subtitleText = subtitleKey==null || subtitleKey.isEmpty() ?  null : lmsg( subtitleKey );
            
            float y = addTitle(previousYLevel, titleText, subtitleText, null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( reportData.getIsLTR() ? new float[] { 3.5f, 5.5f } : new float[] { 5.5f, 3.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );


            // This tells iText to always use the first row as a header on subsequent pages.
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );


            c = new PdfPCell(new Phrase( lmsg( detailKey ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true));
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( lmsg( "g.DefinitionAndHelpfulTips" ) , fontLmWhite ));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false));
            setRunDirection( c );
            t.addCell(c);
            
            // Add header row.
            // t.addCell( new Phrase( lmsg( detailKey ) , fontLmWhite) );
            
            // t.addCell( new Phrase( lmsg( "g.DefinitionAndHelpfulTips" ) , fontLmWhite ) );
            // c.setBackgroundColor( BaseColor.WHITE );


            PdfPTable compT;
            //PdfPTable igT;
            // PdfPTable numGrphT;
            ScoreCategoryType sct;
            //Image dotImg;

            
            //java.util.List<String> tips;
            String tip;

            //Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            String scoreText;

            // String caveatText;
            Map<String,String> compScoreTextMap;
            
            // For each competency
            for( TestEventScore tes : tesl )
            {
                compScoreTextMap =  getCompetencyScoreTextMap( tes.getName() );
                
                if( compScoreTextMap == null )
                {
                    LogService.logIt( "CT2DevelopmentReport.addAnyCompetenciesInfo() Can't find a JSON Competency Map for " + tes.getName() );
                    continue;
                }
                
                
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

                sct = tes.getScoreCategoryType();
                //dotImg = getScoreCategoryImg( sct, true );

                // Name Cell
                c = new PdfPCell( new Phrase( reportData.getCompetencyName(tes) , getFontBold() ) );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( 1 );
                setRunDirection( c );
                compT.addCell( c );

                // Dot Cell
                c = new PdfPCell( new Phrase( "" , getFontSmall() ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPadding( 1 );
                setRunDirection( c );
                compT.addCell( c );

                // Score && Percentile
                if( numeric )
                {
                    if( tes.getIncludeNumericScoreInResults() )
                        c = new PdfPCell( new Phrase( numeric ? lmsg("g.ScoreC") + " " + I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 0) : "", fontSmall ) );
                    else
                        c = new PdfPCell( new Phrase( numeric ? lmsg("g.ScoreC") + " -" : "", fontSmall ) );

                    c.setBorder( Rectangle.NO_BORDER );
                    // c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    setRunDirection( c );
                    compT.addCell( c );

                    StringBuilder srcStr = new StringBuilder();

                    //if( includeTesNorm || norms )
                    //    srcStr.append( lmsg("g.PercentileC") + " " + ( includeTesNorm && tes.getPercentile()>=0 ? Integer.toString((int) tes.getPercentile())+NumberUtils.getPctSuffix( reportData.getLocale(), tes.getPercentile(), 0 ) : lmsg("g.NA") ) );

                    c = new PdfPCell( new Phrase( srcStr.toString(), fontSmall ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    // c.setHorizontalAlignment( reportData.getIsLTR() ?  Element.ALIGN_RIGHT : Element.ALIGN_LEFT );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    c.setPaddingRight( 2 );
                    setRunDirection( c );
                    compT.addCell( c );
                }

                // ScoreGraphCell
                if( numeric )
                {
                    c = new PdfPCell( new Phrase( "" , fontXLarge ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan( 2 );
                    c.setPadding( 9 );
                    c.setFixedHeight(reportData.getTestEvent().getUseBellGraphs() ? BELL_GRAPH_CELL_HEIGHT : BAR_GRAPH_CELL_HEIGHT);
                    c.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, devel, false, reportData.getTestEvent().getUseBellGraphs(), true, 0 ) );
                    setRunDirection( c );
                    compT.addCell( c );
                }


                // The Big text cell
                //p = new Phrase();

                
                String descrip = compScoreTextMap.get( "description" );
                
                scoreText = "";
                
                String scoreLevelName = getCatNameForScoreCategoryId( tes );

                String scrT = compScoreTextMap.get( "scoretext" + scoreLevelName );

                if( scrT!=null && !scrT.isEmpty() )
                {
                    if( !scoreText.isEmpty() )
                        scoreText += "\n\n";
                    
                    scoreText += scrT;
                }
                
                //tips = new ArrayList<>();
                
                com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );

                //Paragraph cHdr=null;
                //Paragraph cFtr=null;
                //float spcg = 8;
                cl.setListSymbol( "\u2022");
                
                //StringBuilder tipSb = new StringBuilder();
                
                for( int i=1; i<=3; i++ )
                {
                    tip = compScoreTextMap.get( "tip" + scoreLevelName + i );
                    
                    if( tip!=null && !tip.isEmpty() )
                    {
                        if( tip.startsWith( "\"") )
                            tip = tip.substring(1);
                        
                        if( tip.endsWith( "\"") )
                            tip = tip.substring(0,tip.length()-1);
                        
                        if( tip.isEmpty() )
                            continue;
                        
                        //tips.add( tip );
                        
                        //if( tipSb.length() > 0 )
                        //    tipSb.append( "\n\n" );
                        
                        //tipSb.append( "\u2022 " + tip );
                        
                        cl.add( new ListItem( new Paragraph( tip , getFontSmall() ) ) );
                        
                    }
                    
                }
                
              
                // ScoreText Cell
                c = new PdfPCell( new Phrase( scoreText, getFontSmall() )  );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( BaseColor.WHITE );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setColspan( 2 );
                c.setPaddingTop( 10 );
                setRunDirection( c );

                if( scoreText != null && !scoreText.isEmpty() )
                    compT.addCell(c);

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

                
                PdfPTable compT2 = new PdfPTable( new float[] {1} );
                compT2.setTotalWidth( 0.9f*outerWid*5.5f/9f );
                compT2.setLockedWidth(true);
                compT2.setHorizontalAlignment( Element.ALIGN_CENTER );
                setRunDirection( compT2 );

                c = compT2.getDefaultCell();
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                c.setPadding( 1 );                
                
                
                
                // Tips
                Paragraph para = new Paragraph();
                
                para.add( new Phrase( lmsg( "g.WhatIsX" , new String[] {reportData.getCompetencyName(tes) }) + "\n\n", this.fontItalic ) );
                
                para.add( new Phrase( descrip + "\n\n", getFontSmall() ) );

                para.add( new Phrase( lmsg( "g.HelpfulTips" , new String[] {reportData.getCompetencyName(tes) }), this.fontItalic ) );
                
                //para.add( new Phrase( tipSb.toString(), getFontSmall() ) );
                
                c = new PdfPCell( para );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( BaseColor.WHITE );
                setRunDirection( c );    
                c.setPadding(3);     
                c.setPaddingTop(8);
                compT2.addCell( c );
                
                c = new PdfPCell();
                c.addElement( cl );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( BaseColor.WHITE );
                setRunDirection( c );    
                c.setPadding(3);
                compT2.addCell( c );
                
                
                
                
                //c.addElement( cl );
                c = new PdfPCell();
                c.addElement(compT2);
                // c = new PdfPCell( new Phrase( tipSb.toString(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                setRunDirection( c );
                
                
                
                
                
                t.addCell( c );
                
            } // each competency

            currentYLevel = addTableToDocument(y, t, false, true );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "CT2DevelopmentReport.addAnyCompetenciesInfo() titleKey=" + titleKey );

            throw new STException( e );
        }
    }
    
    
    
    
    
    
    public void addOverallSection() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.Overall" ), null, null, null );

            y -= TPAD;

            String scr = I18nUtils.getFormattedNumber( reportData.getLocale(), reportData.getTestEvent().getOverallScore(), reportData.getTestEvent().getScorePrecisionDigits() );

            TestEventScore tes = reportData.getTestEvent().getOverallTestEventScore();

            if( reportData.hasProfile() )
                 tes.setProfileBoundaries( reportData.getOverallProfileData() );

            // LogService.logIt( "Found " + ct3RiskFactors.size() + " CT3 Risk Factors." );

            ScoreCategoryType sct = tes.getScoreCategoryType();

            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f, .35f}  : new float[] { .35f, .2f, .45f };

            boolean includeNumScores = true; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean includeColorGraph = reportData.getReport().getIncludeCompetencyColorScores()==1;

            if( !includeColorGraph )
            {
                cols -= 1;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f } : new float[] { .2f, .45f };
            }

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
            c = new PdfPCell( new Phrase( lmsg( "g.Participant"), fontLargeWhite ) );
            //c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            //c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            //c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent( new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            setRunDirection( c );

            touter.addCell(c);

            if( includeNumScores )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
                c.setColspan( 1 );

                //if( includeStars )
                //    c.setBorder( Rectangle.TOP );
                //else
                // c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
                c.setBorder( Rectangle.NO_BORDER );

                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                if( !includeColorGraph )
                    c.setCellEvent( new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );                    
                else
                    c.setBackgroundColor( ct2Colors.hraBlue );
                    
                touter.addCell(c);

                if( includeColorGraph )
                {
                    c = new PdfPCell( new Phrase( lmsg( "g.Interpretation"), fontLargeWhite ) );
                    c.setColspan( 1 );

                    c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );

                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( 25 );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    c.setCellEvent( new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );                    
                    setRunDirection( c );
                    touter.addCell(c);
                }
            }

            // header row is finished.

            c = touter.getDefaultCell();
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

            touter.addCell( c );


            int colspan = 1;

            if( reportData.getReport().getIncludeOverallScore()!=1 )
                colspan++;

            // NAME
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            c.setColspan(colspan);
            c.setPadding( 1 );

            if( reportData.getIsLTR() )
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            else
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

            c.setVerticalAlignment( Element.ALIGN_BOTTOM );
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            touter.addCell( c );

            // Score (if)
            c = new PdfPCell( new Phrase( reportData.getReport().getIncludeOverallScore()==1 ? scr : "" , fontLargeBold ) );
            c.setPadding( 2 );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_BOTTOM );
            c.setBorder( includeColorGraph ? Rectangle.NO_BORDER : ( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT ) );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );

            if( includeColorGraph )
            {
                PdfPTable t2 = new PdfPTable( 1 );
                setRunDirection( t2 );

                t2.setWidthPercentage( 100 );
                // t2.setLockedWidth( true );

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setPadding( 0 );
                c.setFixedHeight(reportData.getTestEvent().getUseBellGraphs() ? BELL_GRAPH_CELL_HEIGHT : BAR_GRAPH_CELL_HEIGHT);
                c.setCellEvent(new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, false, this.ct2Colors, devel, false, reportData.getTestEvent().getUseBellGraphs(), true, 0 ) );
                t2.addCell(c);


                c = new PdfPCell( t2 ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                c.setPaddingTop( 6 );


                // c.setCellEvent( new CT2OverallScoreGraphicCellEvent( tes , sct, baseFontCalibri ) );
                touter.addCell( c );
            }

            if( reportData.getUser()==null )
                reportData.u = new User();

            

            // Next row - test name
            c = new PdfPCell( new Phrase( reportData.getSimName(), getFont() ) );
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

            // Next Row, test date
            c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFont() ) );
            c.setColspan( cols );
            c.setPadding( 1 );
            // c.setPaddingBottom( 6 );
            if( reportData.getIsLTR() )
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            else
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );

            // 1=low, 3=med, 5=high
            
            int scoreCategoryTypeId = reportData.getTestEvent().getOverallTestEventScore().getScoreCategoryId();
            
            String scoreCatName = getCatNameForScoreCategoryId( reportData.getTestEvent().getOverallTestEventScore() );

            
            // Next row - Text
            String scrTxt = this.getOverallScoreTextMap().get( scoreCatName );
            
            if( reportData.getReport().getIncludeScoreText() != 1  )
                scrTxt = null;

            PdfPTable t3 = new PdfPTable( 1 );
            setRunDirection( t3 );
            t3.setWidthPercentage( 100 );

            c = t3.getDefaultCell();
            c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            //c.setPhrase(null);
            setRunDirection( c );

            if( scrTxt != null && !scrTxt.isEmpty() )
            {

                if( scrTxt != null && !scrTxt.isEmpty() )
                {
                    //Paragraph pp = new Paragraph( scrTxt, getFontLight() );

                    //pp.setAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );

                    //pp.setSpacingBefore( 1 );
                    //pp.setLeading( showRiskFactors ? 9 : 10 );

                    c = new PdfPCell( new Phrase( scrTxt, getFont() ));
                    c.setPadding( 1 );
                    //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    setRunDirection(c);
                    c.setBorder( Rectangle.NO_BORDER );
                    //c.addElement( pp );
                    //c.addElement( new Phrase( scrTxt, getFontLight() ) );
                   // c.setPhrase(  );

                    //t3.addCell(scrTxt);

                    t3.addCell( c );
                    //showRiskFactors = false;
                }

                colspan = cols;

                if( includeColorGraph )
                {
                    colspan--;
                }

                c = new PdfPCell();
                c.setPadding( 1 );
                setRunDirection(c);

                c.setColspan( colspan );

                if( reportData.getIsLTR() )
                    c.setBorder( includeColorGraph ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );
                else
                    c.setBorder( includeColorGraph ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM | Rectangle.LEFT  );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 0 );
                c.setPaddingTop( 6 );
                c.setPaddingRight( 4 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingBottom( 10 );
                setRunDirection( c );

                c.addElement( t3 );
                touter.addCell( c );

                if( includeColorGraph )
                {
                    c = new PdfPCell( getScoreKeyTable( false ) );
                    c.setColspan( 1 );
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_CENTER );

                    c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.LEFT  );

                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 0 );
                    c.setPaddingTop( 6 );
                    c.setPaddingBottom( 10 );
                    setRunDirection( c );
                    touter.addCell( c );
                }
            }

            else if( includeColorGraph )
            {
                c = new PdfPCell( getScoreKeyTable( false ) );
                c.setColspan( cols );
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_CENTER );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 0 );
                c.setPaddingTop( 6 );
                c.setPaddingBottom( 10 );
                setRunDirection( c );
                touter.addCell( c );
            }

            // float y = pageHeight - headerHgt - 4*PAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();
            
            currentYLevel = this.currentYLevel - 10;

            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2developmentReport.addOverallSection()" );

            throw e;
        }
    }

    
    public String getCatNameForScoreCategoryId( TestEventScore tes )
    {
        java.util.List<ScoreCategoryRange> scrLst = tes.getScoreCatInfoList();
        
       if( scrLst == null || scrLst.isEmpty() )
           scrLst = ScoreFormatUtils.getOverallScoreCatInfoList(false, null);
        
       float score = tes.getScore();
              
       for( ScoreCategoryRange scr : scrLst )
       {
           
            if( score >= scr.getMin() && score<scr.getMax() && scr.getMin()<scr.getMax() )
            {
                if( scr.getScoreCategoryTypeId() == ScoreCategoryType.RED.getScoreCategoryTypeId() )
                    return "low";
                
                if( scr.getScoreCategoryTypeId() == ScoreCategoryType.YELLOW.getScoreCategoryTypeId() )
                    return "medium";

                if( scr.getScoreCategoryTypeId() == ScoreCategoryType.GREEN.getScoreCategoryTypeId() )
                    return "high";                
            }           

            // This covers score=100
            else if( scr.getScoreCategoryTypeId() == ScoreCategoryType.GREEN.getScoreCategoryTypeId() && score >= scr.getMin() && score<=scr.getMax() && scr.getMin()<scr.getMax() )
                return "high";

       }
       
       // Not found, use the old way.
       int scoreCategoryTypeId = tes.getScoreCategoryId();
       
       
        if( scoreCategoryTypeId >= 1 && scoreCategoryTypeId<3 )
            return "low";
        
        if( scoreCategoryTypeId >= 3 && scoreCategoryTypeId<8 )
            return "medium";
        
        return "high";        
    }
    
    

    protected void addHowUseReportSection() throws Exception
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

            String text = "";
            
            //if( assessoverviewtext != null && !assessoverviewtext.isEmpty() )
            //    text += assessoverviewtext;
            
            if( howtousereport!=null && !howtousereport.isEmpty() )
                text += ( text.length()>0 ? "\n\n" : "" ) + howtousereport;
            
            if( howtousereport2!=null && !howtousereport2.isEmpty() )
                text += ( text.length()>0 ? "\n\n" : "" ) + howtousereport2;

            addTitle(previousYLevel, lmsg( "g.HowToUseReport" ), text, null, null );
            
            currentYLevel = this.currentYLevel - 10;

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addHOwUseReportSection() " );

            throw new STException( e );
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    public void parseJson() throws Exception
    {    
        String reportJson = this.reportData.getR2Use().getTextParam2();
        
        try
        {
            if( reportJson == null || reportJson.isEmpty() )
                throw new Exception( "JSON is empty!");
            
            JsonReader jr = Json.createReader(new StringReader( reportJson ) );    
            
            JsonStructure jsonst = jr.read();
            
            JsonObject topJo = (JsonObject) jsonst;

            
            
            if( topJo.containsKey( "assessoverviewtext" ))
                assessoverviewtext = topJo.getString("assessoverviewtext" );
            
            if( assessoverviewtext != null )
                assessoverviewtext = assessoverviewtext.trim();
            
            if( assessoverviewtext != null && !assessoverviewtext.isEmpty() )
                this.coverDescrip = assessoverviewtext;
            
            if( topJo.containsKey( "howtousereport" ))
                howtousereport = topJo.getString("howtousereport" ).trim();

            if( howtousereport != null )
                howtousereport = howtousereport.trim();
            
            if( topJo.containsKey( "howtousereport2" ))
                howtousereport2 = topJo.getString("howtousereport2" );
            
            if( howtousereport2 != null )
                howtousereport2 = howtousereport2.trim();
            
            if( topJo.containsKey( "workcompetenciesintro" ))
                workcompetenciesintro = topJo.getString( "workcompetenciesintro" );
            
            if( workcompetenciesintro != null )
                workcompetenciesintro = workcompetenciesintro.trim();
            
            if( topJo.containsKey( "workcompetenciesintro2" ))
                workcompetenciesintro2 = topJo.getString( "workcompetenciesintro2" );
            
            if( workcompetenciesintro2 != null )
                workcompetenciesintro2 = workcompetenciesintro2.trim();
            
            
            if( workcompetenciesintro != null && !workcompetenciesintro.isEmpty() )
            {
                competencySummaryStr = workcompetenciesintro;
                
                if( workcompetenciesintro2 != null && !workcompetenciesintro2.isEmpty() )
                    competencySummaryStr += "\n\n" + workcompetenciesintro2;                    
            }
                        
            if( !topJo.containsKey( "overall") )
                throw new Exception( "No overall section found in JSON." + reportJson );
            
            
            // LogService.logIt( "CT2DevelopmentReport.parseJson() Adding Overall Section." );
            
            overallJo = topJo.getJsonObject( "overall" );
            
            if( !topJo.containsKey( "competencies") )
                throw new Exception( "No competencies section found in JSON." + reportJson );
            
            JsonArray cs = topJo.getJsonArray( "competencies" );

            // LogService.logIt( "CT2DevelopmentReport.parseJson() Cmopetencies contains " + cs.size() + " entries." );
            
            ListIterator<JsonValue> iter = cs.listIterator();
            
            JsonValue v;
            JsonObject c;
            String cn;
            
            if(competencyMap == null )
                competencyMap = new HashMap<>();
            
            while( iter.hasNext() )
            {
                v = iter.next(); 
                
                c = (JsonObject)v;
                
                if( !c.containsKey("name" ) )
                    throw new Exception( "json competency doesn't have a name! " + reportJson );
                
                cn = c.getString( "name" );
                
                if( cn==null || cn.isEmpty() )
                    throw new Exception( "json competency name is empty! " + reportJson );
                
                cn = cn.trim();
                
                // LogService.logIt( "CT2DevelopmentReport.parseJson() Adding competency " + cn );
                competencyMap.put( cn, c );              
            }
            
            // LogService.logIt( "CT2DevelopmentReport.parseJson() Parse complete. " );
            
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2DevelopmentReport.parseJson() " );
            
            throw e;
        }
        
                    
    }
    
    
    public Map<String,String> getOverallScoreTextMap() throws Exception
    {
        Map<String,String> out = new HashMap<>();
        try
        {
            if( overallJo == null )
                parseJson();
            
            if( overallJo.containsKey( "high" ))
                out.put( "high", overallJo.getString("high" ) );

            if( overallJo.containsKey( "medium" ))
                out.put( "medium", overallJo.getString("medium" ) );

            if( overallJo.containsKey( "low" ))
                out.put( "low", overallJo.getString("low" ) );
        }

        catch( Exception e )        
        {
            LogService.logIt( e, "CT2DevelopmentReport.getOverallScoreTextMap() " );
        }

        return out;
    }

    
    public Map<String,String> getCompetencyScoreTextMap( String competencyName ) throws Exception
    {
        Map<String,String> out = new HashMap<>();
        try
        {
            if( competencyName == null )
                return out;
            
            competencyName = competencyName.trim();
            
            if( competencyMap == null )
                parseJson();            
            
            if( competencyMap.containsKey( competencyName ) )
            {
                JsonObject joc = competencyMap.get( competencyName );
                
                out.put( "name", competencyName );
                
                if( joc.containsKey( "description" ) )
                    out.put( "description", joc.getString( "description" ) );

                parseCompetencyScoreLevel( joc, "high", out );                
                parseCompetencyScoreLevel( joc, "medium", out );                
                parseCompetencyScoreLevel( joc, "low", out );                                
            }
        }

        catch( Exception e )        
        {
            LogService.logIt( e, "CT2DevelopmentReport.getCompetencyScoreTextMap() " + competencyName );
        }

        return out;
    }
    
    
    private void parseCompetencyScoreLevel( JsonObject joc, String level, Map<String,String> out ) throws Exception
    {
        if( joc.containsKey( level ) )
        {
            JsonObject joScr = (JsonObject) joc.getJsonObject( level );

            if( joScr.containsKey( "scoretext" ) )
                out.put( "scoretext" + level, joScr.getString( "scoretext" ) );

            if( joScr.containsKey( "tips" ) )
            {
                JsonArray tipArr = joScr.getJsonArray( "tips" );

                ListIterator<JsonValue> iter = tipArr.listIterator();

                JsonValue jv;
                String tp;
                int count = 0;

                while( iter.hasNext() )
                {
                    jv = iter.next();

                    tp = (String) jv.toString();

                    if( tp != null && !tp.trim().isEmpty() )
                    {
                        count++;
                        out.put( "tip" + level + count, tp );
                    }
                }
            }
        }
        
    }

    
    
    

    

}
