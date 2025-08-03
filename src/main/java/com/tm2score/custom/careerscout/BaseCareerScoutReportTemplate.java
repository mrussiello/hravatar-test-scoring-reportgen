/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.careerscout;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.custom.bestjobs.BaseBestJobsReportTemplate2;
import com.tm2score.custom.coretest.ITextUtils;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import com.tm2score.custom.coretest2.CellBackgroundCellEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.profile.Profile;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.StringUtils;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public abstract class BaseCareerScoutReportTemplate extends BaseBestJobsReportTemplate2 implements ReportTemplate
{
    static String[] NORMAL_SCORE_COMPS = new String[]{ "Adaptable","Develops Relationships", "Expressive and Outgoing","Needs Structure","Seeks Perfection" };
    
    private final static float HI_SCORE_KSA = 75;
    private final static float LOW_SCORE_KSA = 25;
    private final static float HI_SCORE_NONCOG = 75;
    private final static float LOW_SCORE_NONCOG = 25;
    private final static float HI_SCORE_BIO = 75;
    private final static float LOW_SCORE_BIO = 25;
    //private final static float HI_SCORE_EDUC = 75;
    //private final static float LOW_SCORE_EDUC = 25;
    private final static float LOW_SCORE_EXPERIENCE = 5;
    private final static float LOW_SCORE_EDUCATION = 4;
    private final static float LOW_SCORE_TRAINING = 5;
    
    
    
    @Override
    public void addCoverPage(boolean includeDescriptiveText) throws Exception
    {
        try
        {
            PdfPCell c;

            PdfPTable t;            
            
            getHraLogoBlackText().scalePercent( 100 * 72 / 400 );
            
            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 25;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

            ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), CT2_MARGIN, y, false );

            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            
            c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLarge() ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y - 20; //  tableH; // + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );            
            
            y = tableY - tableH - 20;
            
            float imageX = (pageWidth - scout.getScaledWidth())/2; 

            ITextUtils.addDirectImage( pdfWriter, scout, imageX, y - scout.getScaledHeight()-20, false );
            
            y -= scout.getScaledHeight()+20;
            
            java.util.List<Chunk> cl = new ArrayList<>();

            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";
           
            cl.add( new Chunk( lmsg( "g.PreparedForC" ), getFontXLarge() ) );
            // cl.add( new Chunk( lmsg( "g.AssessmentC" ), getFontXLarge() ) );
            cl.add( new Chunk( bmsg( "b.PreparedDateC" ), getFontXLarge() ) );
            

            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            if( !includeCompanyInfo )
                reportCompanyName = "                        ";
            
            if( includeCompanyInfo && getHasSponsor() )
                cl.add( new Chunk( bmsg( "b.SponsoringEmployerC" ), getFontXLarge() ) );

            float titleWid = ITextUtils.getMaxChunkWidth( cl ) + 20;

            cl.clear();

            cl.add( new Chunk( reportData.getUserName(), getFontXLargeBold() ) );
            // cl.add( new Chunk( reportData.getSimName(), getFontXLarge() ) );
            cl.add( new Chunk( getCurrentDateFormatted(), getFontXLarge() ) );

            float infoWid = ITextUtils.getMaxChunkWidth( cl ) + 10;

            if( includeCompanyInfo && getHasSponsor() )
            {
                // cl.add( new Chunk( reportCompanyAdminName, getFontXLarge() ) );

                if( (!reportData.hasCustLogo() || custLogo==null) && reportCompanyName != null && !reportCompanyName.isEmpty() )
                    cl.add( new Chunk( reportCompanyName, getFontXLarge() ) );

                if( custLogo!=null && custLogo.getScaledWidth()>infoWid )
                    infoWid = custLogo.getScaledWidth();            
            }
            
            // First create the table
            
            // First, add a table
            t = new PdfPTable( 2 );

            t.setTotalWidth( reportData.getIsLTR() ?  new float[] { titleWid+4, infoWid+14 } : new float[] { infoWid+14,titleWid+4 } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            Font font = this.fontXLarge;

            t.addCell( new Phrase( lmsg( "g.PreparedForC" ) , font ) );
            t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

            //t.addCell( new Phrase( lmsg( "g.AssessmentC" ) , font ) );
            //t.addCell( new Phrase( reportData.getSimName(), font ) );

            t.addCell( new Phrase( bmsg( "b.PreparedDateC" ) , font ) );
            t.addCell( new Phrase( getCurrentDateFormatted(), font ) );


            if( includeCompanyInfo && getHasSponsor() )
            {
                // LogService.logIt( "BaseCareerScoutReportTemplate.addCoverPage()"  );

                t.addCell( new Phrase( bmsg( "b.SponsoringEmployerC" ) + " " , font ) );

                // t.addCell( new Phrase( "" , font ) );

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

            tableH = t.calculateHeights(); //  + 500;

            tableY = y -20; //   y + 10 - (y - pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, getHraOrangeColor(), false );

            // Add the blue below
            ITextUtils.addDirectColorRect( pdfWriter, getHraBaseReportColor(), 0, 0, pageWidth, pageHeight/3, 0, 1, true );


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

            // t.addCell( "\n\n\n\n\n\n\n\n\n" );
            
            if( !includeDescriptiveText )
                t.addCell( "\n\n\n\n\n" );
            
            c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            //t.addCell( c );

            t.addCell( "\n\n\n\n\n\n\n\n\n" );

            //String coverDescrip=null; 

            if( includeDescriptiveText )
            {
                if( coverDescrip != null &&  !coverDescrip.isEmpty() )
                {}

                else if( reportData.getReport() != null && reportData.getReport().getTextParam1()!=null && !reportData.getReport().getTextParam1().isEmpty() )
                {
                    coverDescrip = reportData.getReport().getTextParam1();
                    
                    if( !reportData.getLocale().getLanguage().equalsIgnoreCase( reportData.r2Use.getLocaleStr()==null || reportData.r2Use.getLocaleStr().isEmpty() ? "en" : I18nUtils.getLocaleFromCompositeStr( reportData.r2Use.getLocaleStr()).getLanguage() ) )
                    {
                        Locale srcLoc = reportData.r2Use.getLocaleStr()==null || reportData.r2Use.getLocaleStr().isEmpty() ? Locale.US : I18nUtils.getLocaleFromCompositeStr( reportData.r2Use.getLocaleStr());
                        coverDescrip = tctrans( coverDescrip, srcLoc, false );
                    }
                    
                }

                else 
                {
                    String coverDetailKey = reportData.getReport()!=null && reportData.getReport().getStrParam1()!=null && !reportData.getReport().getStrParam1().isEmpty() ? reportData.getReport().getStrParam1() : "s.CoverBlurb";            

                    coverDescrip = bmsg( coverDetailKey, new String[] {reportData.getSimName()} );                
                }  
                  
                if( 1==1 )
                {
                    Paragraph pr = new Paragraph(); 

                    pr.add( new Phrase( coverDescrip + "\n\n\n\n" , fontLLWhite ) );


                    //pr.add( new Chunk( bmsg( "b.NoScoreCaveat1" ) + " " , fontXLargeBoldWhite ) );
                    //pr.add( new Chunk( bmsg( "b.NoScoreCaveat2" ) + " " , fontLLWhite ) );


                    c = new PdfPCell( pr );
                    c.setBorder( Rectangle.NO_BORDER );
                    setRunDirection(c);

                    t.addCell( c );
                }
            }
            //t.addCell( "\n\n\n" );
            //c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFontWhite() ) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( Element.ALIGN_CENTER );
            //setRunDirection(c);
            //t.addCell( c );

            tableH = t.calculateHeights(); //  + 500;

            tableY = pageHeight/2 - (pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);


            c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFontWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            // tableH = t.calculateHeights(); //  + 500;

            tableY = 20;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );
        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseCareerScoutReportTemplate.addCoverPage()" );
        }
    }

    
    
    
    public void addCompetencySuggestionSection() throws Exception
    {
        try
        {
            java.util.List<TestEventScore> teslA = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.ABILITY, true );
            teslA.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.ABILITY_COMBO, true ) );

            // Must resort since added a few groups
            Collections.sort(teslA, new DisplayOrderComparator() );  // new TESNameComparator());
            
            java.util.List<TestEventScore> teslK = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.KNOWLEDGE, true );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.CORESKILL, true ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDESSAY, true ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDAUDIO, true ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDAVUPLOAD, true ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDTYPING, true ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDDATAENTRY, true ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDCHAT, true ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SKILL_COMBO, true ) );

            // Must resort since added a few groups
            Collections.sort(teslK, new DisplayOrderComparator() );  // new TESNameComparator());

            java.util.List<TestEventScore> teslP = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOGNITIVE, true );
            teslP.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOG_COMBO, true ) );
            teslP.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.INTERESTS_COMBO, true ) );
            Collections.sort(teslP, new DisplayOrderComparator() );  // new TESNameComparator());

            java.util.List<TestEventScore> teslE = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.EQ, true );
            // Collections.sort(teslE, new TESNameComparator());
            
            java.util.List<TestEventScore> teslB = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDBIODATA, true );
            teslB.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.BIODATA_COMBO, true ) );
            
            // Collections.sort(teslB, new TESNameComparator());

            // java.util.List<TestEventScore> teslI = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDINTEREST );
            // Collections.sort(teslE, new TESNameComparator());
            
            
            
            Map<String,List<String>> infoMapA = getCompetencySuggestionInfoMap( teslA );
            Map<String,List<String>> infoMapK = getCompetencySuggestionInfoMap( teslK );
            Map<String,List<String>> infoMapP = getCompetencySuggestionInfoMap( teslP );
            Map<String,List<String>> infoMapE = getCompetencySuggestionInfoMap( teslE );
            Map<String,List<String>> infoMapB = getCompetencySuggestionInfoMap( teslB );
            // Map<String,List<String>> infoMapI = getCompetencySuggestionInfoMap( teslI );
            
            // If there are no suggestions
            if( infoMapA.isEmpty() && infoMapK.isEmpty() && infoMapP.isEmpty() && infoMapE.isEmpty() && infoMapB.isEmpty() )
            {
                addNoCompetencySuggestionsSection();
                return;
            }
            
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel - 10;

            currentYLevel = addTitle( previousYLevel, bmsg( "s.CompetencySuggestions" ), null );

            currentYLevel -= TPAD;

            if( !infoMapA.isEmpty() )
            {
                addCompetencySuggestionSection( teslA, infoMapA, "s.SuggestionsAbilities"  );
                currentYLevel -= 8;
            }
            
            if( !infoMapK.isEmpty() )
            {
                addCompetencySuggestionSection( teslK, infoMapK, "s.SuggestionsKnowledgeSkills"  );
                currentYLevel -= 8;
            }
            
            if( !infoMapP.isEmpty() )
            {
                addCompetencySuggestionSection( teslP, infoMapP, "s.SuggestionsPersonalityFactors"  );
                currentYLevel -= 8;
            }
            
            if( !infoMapE.isEmpty() )
            {
                addCompetencySuggestionSection( teslE, infoMapE, "s.SuggestionsEmotionalIntels"  );
                currentYLevel -= 8;
            }
            
            if( !infoMapB.isEmpty() )
            {
                addCompetencySuggestionSection( teslB, infoMapB, "s.SuggestionsPastHistory"  );
                currentYLevel -= 8;
            }
                        
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCareerSoutReportTemplate.addCompetencySuggestionSection()" );

            throw e;
        }        
    }
    
    public float addCompetencySuggestionSection( List<TestEventScore> tesl, Map<String,List<String>> infoMap, String titleKey  ) throws Exception
    {
        try
        {
            float y = currentYLevel;
            
            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.20f,  .15f, .65f } : new float[] { 0.65f, .15f, .20f };

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t2 = new PdfPTable( cols );

            setRunDirection(t2 );
            // float importanceWidth = 25;

            t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            t2.setWidths( colRelWids );
            t2.setLockedWidth( true );
            t2.setHorizontalAlignment( Element.ALIGN_CENTER );
            t2.setKeepTogether( false );
            t2.setHeaderRows(1);
            // touter.set
            
            // Create header (two rows
            c = new PdfPCell( new Phrase( bmsg( titleKey ), fontWhite ) );
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );            
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setBorderWidthBottom( 0 );
            c.setColspan( 3 );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, false, false) );
            setRunDirection( c );
            t2.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "s.Name" ), fontWhite ) );
            c.setColspan( 1 );
            c.setBorder( Rectangle.NO_BORDER );

            //c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM ); // | Rectangle.RIGHT );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM ); // | Rectangle.RIGHT );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM ); // | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderColorTop( BaseColor.WHITE );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setBorderWidthBottom( 0 );
            c.setBorderWidthTop( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( BaseColor.DARK_GRAY ); // ct2Colors.hraBlue );
            setRunDirection( c );
            t2.addCell(c);

            c = new PdfPCell( new Phrase( bmsg( "s.Score" ), fontWhite ) );
            c.setColspan( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            // c.setBorder( Rectangle.TOP  | Rectangle.BOTTOM );
            //c.setBorderColor( ct2Colors.lightBoxBorderColor );
            //c.setBorderColorTop( BaseColor.WHITE );
            //c.setBorderWidth( lightBoxBorderWidth );
            //c.setBorderWidthBottom( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( BaseColor.DARK_GRAY ); // ct2Colors.hraBlue );
            setRunDirection( c );
            t2.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "s.Suggestions" ), fontWhite ) );
            c.setColspan( 1 );
            c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT :  Rectangle.LEFT );
            // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT  | Rectangle.BOTTOM:  Rectangle.TOP | Rectangle.LEFT  | Rectangle.BOTTOM);
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderColorTop( BaseColor.WHITE );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setBorderWidthBottom( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( BaseColor.DARK_GRAY ); // ct2Colors.hraBlue );
            setRunDirection( c );
            t2.addCell(c);
                                 
            List<String> sugsForTes;
            String scoreStr;
            
            // OK, hearder done, now for the profiles
            for( TestEventScore tes : tesl )
            {
                sugsForTes = infoMap.get( tes.getName() );
                
                if( sugsForTes == null || sugsForTes.isEmpty() )
                    continue;
                
                scoreStr = getScoreStrForCompetency( tes, SimCompetencyClass.getValue( tes.getSimCompetencyClassId() ) );
                                
                c = new PdfPCell( new Phrase( tctrans( tes.getName(), false ), fontBold ) );
                
                c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT  | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setColspan( 1 );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                if( sugsForTes.size()>1 )
                    c.setBorderWidthBottom( 0 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                setRunDirection( c );
                t2.addCell(c);
                
                c = new PdfPCell( new Phrase( scoreStr, font ) );
                c.setColspan( 1 );
                c.setBorder( Rectangle.BOTTOM);
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( sugsForTes.size()>1 )
                    c.setBorderWidthBottom( 0 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t2.addCell(c);

                if( sugsForTes.size()>=1 )
                {
                    c = new PdfPCell( new Phrase( sugsForTes.get(0), font ) );
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    if( sugsForTes.size()>1 )
                        c.setBorderWidthBottom( 0 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t2.addCell(c);                                    
                }
                else
                {
                    c = new PdfPCell( new Phrase( "", font ) );
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t2.addCell(c);                                                        
                }

                // Next Row
                
                if( sugsForTes.size()>1 )
                {
                    c = new PdfPCell( new Phrase( "", font ) );
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
                    // c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setColspan(1);
                    // c.setPadding( 1 );
                    // c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    // c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    // t2.addCell(c);                                    

                    if( reportData.getIsLTR() )
                    {
                        // PdfPTable tt = ;
                        // c = new PdfPCell( getBulletTableForSuggestions( sugsForTes ) );

                        c = new PdfPCell();
                        c.addElement( getBulletListForSuggestions( sugsForTes.subList( 1, sugsForTes.size() ) ) );
                        c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.LEFT | Rectangle.BOTTOM );
                        c.setBorderColor( ct2Colors.lightBoxBorderColor );
                        c.setBorderWidth( lightBoxBorderWidth );
                        c.setColspan(3);
                        c.setPadding( 5 );
                        c.setPaddingLeft( 20 );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT );
                        // c.setBackgroundColor( ct2Colors.hraBlue );
                        setRunDirection( c );
                        t2.addCell(c);                                    
                    }

                    else
                    {
                        PdfPTable tt = new PdfPTable( new float[] { 1f } );

                        // t.setHorizontalAlignment( Element.ALIGN_CENTER );
                        tt.setTotalWidth( 0.65f*(pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN) - 20 );
                        tt.setLockedWidth( true );
                        setRunDirection( tt );

                        c = tt.getDefaultCell();
                        c.setBorder( Rectangle.NO_BORDER );
                        setRunDirection( c );

                        for( String ct : sugsForTes.subList( 1, sugsForTes.size() ) )
                        {
                            if( ct.isEmpty() )
                                continue;

                            tt.addCell( new Phrase(ct, getFont() ) );
                        }

                        c = new PdfPCell();
                        c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.LEFT | Rectangle.BOTTOM );
                        c.setBorderColor( ct2Colors.lightBoxBorderColor );
                        c.setBorderWidth( lightBoxBorderWidth );
                        c.setColspan(3);
                        c.setPadding( 5 );
                        c.setPaddingLeft( 20 );
                        c.addElement( tt );
                        setRunDirection( c );
                        t2.addCell(c);                    
                    }
                }                

            }
            
            currentYLevel = addTableToDocument(y, t2 );

            return currentYLevel;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "BaseCareerScoutReportTemplate.addCompetencySuggestionSection() infoMap.size=" + infoMap.size() + ", titleKey=" + titleKey );
            
            throw e;
        }
        
    }
    
    /*
    @Override
    public String getReportGenerationNotesToSave()
    {
        StringBuilder sb = new StringBuilder();
        int ct = 0;
        
        if( bestProfilesList!=null && !bestProfilesList.isEmpty() )
        {
            ct = 0;
            
            sb.append( "[" + Constants.RIASEC_COMPACT_INFO_KEY + "]" );

            sb.append( getDetailHraInfoHeadersCSV() );

            for( Profile p : bestProfilesList )
            {
                ct++;
                sb.append( "|" + getDetailHraInfoCSV( ct, p) );
            }

        }
        
        if( eeoMatchList!=null && !eeoMatchList.isEmpty() )
        {
            sb.append( "[" + Constants.EEOCAT_COMPACT_INFO_KEY + "]" );
            sb.append( getDetailHraInfoHeadersEeoCatCSV() );
            
            ct = 0;
            
            for( EeoMatch m : eeoMatchList )
            {
                ct++;
                sb.append( "|" + getDetailHraInfoEeoCatCSV( ct, m ) );                
            }            
        }
                
        return sb.toString();
    }
    
    private String getDetailHraInfoCSV( int rank, Profile p )
    {
        return rank + "," + p.getProfileId() + "," + StringUtils.getUrlEncodedValue(p.getStrParam1()) + "," + p.getFloatParam1() + "," + p.getFloatParam2() + "," + p.getFloatParam3() + "," + p.getFloatParam4() + "," + p.getStrParam4();
        // return reportData.getTestEvent().getTestEventId() + "," + rank + "," + p.getProfileId() + "," + Csv.escape(p.getStrParam1()) + "," + p.getFloatParam4() + "," + p.getFloatParam2() + "," + p.getFloatParam1() + "," + p.getFloatParam3();
    }

    private String getDetailHraInfoHeadersCSV()
    {
        return "rank,pid,pname,jobspec,riasec,combined,educexp,soccode";
        // return "teid,rank,pid,pname,jobspec,riasec,combined,educexp";
    }
    
    private String getDetailHraInfoHeadersEeoCatCSV()
    {
        return "rank,title,eeocattypeid,percentmatch";        
    }
    
    private String getDetailHraInfoEeoCatCSV( int rank, EeoMatch m )
    {
        return rank + "," + StringUtils.getUrlEncodedValue(m.getEeoTitle()) + "," + m.getEeoJobCategoryId()+ "," + m.getAveragePercentMatch();
    }
    */

    
    
    

    public float addNoCompetencySuggestionsSection() throws Exception
    {
        try
        {
            Paragraph p = new Paragraph();
            
            p.add( new Phrase( bmsg( "s.CompetencySuggestions"), fontXLargeBold ) );
            
            List<Object> content = new ArrayList<>();
            
            content.add( bmsg("s.CompetencySuggestionsNoneP1") );
            
            addOneColTable(p, // title para
                                null, // subtitle para
                                content, 
                                false, // header, 
                                fontLarge, 
                                null, // headerFont2Use,
                                0, 
                                0 ); // borderWidth )     
            

            return currentYLevel;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "BaseCareerScoutReportTemplate.addNoCompetencySuggestionSection() " );
            
            throw e;
        }
        
    }

    
    
    public PdfPTable getBulletTableForSuggestions( java.util.List<String> sugLst ) throws Exception
    {
            int cols = 2;
            
            float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.1f,  .9f } : new float[] { 0.9f, .15f };

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t2 = new PdfPTable( cols );
            t2.setTotalWidth( 0.62f*(pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN) );
            setRunDirection(t2 );
            t2.setLockedWidth(true);
            t2.setHorizontalAlignment( Element.ALIGN_LEFT );
            // float importanceWidth = 25;

            // touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            //touter.setWidths( colRelWids );
            //touter.setLockedWidth( true );
            t2.setHorizontalAlignment( Element.ALIGN_LEFT );
            //touter.setKeepTogether( false );
            //touter.setHeaderRows(0);
            // touter.set
            String sug;
            
            for( int i=0;i<sugLst.size();i++ )
            {
                sug = sugLst.get(i);
                
                if( sug==null || sug.isEmpty() )
                    continue;
                
                if( i==0 )
                {
                    c = new PdfPCell( new Phrase( sug , font ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan(2);
                    c.setPadding( 2 );
                    c.setPaddingBottom( 5 );
                    // c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t2.addCell(c);        
                }
                
                else
                {
                    c = new PdfPCell( new Phrase( " - ", font ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan(1);
                    c.setPadding( 2 );
                    // c.setPaddingBottom( 5 );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    // c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t2.addCell(c);        

                    c = new PdfPCell( new Phrase( sug , font ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan(1);
                    c.setPadding( 2 );
                    // c.setPaddingBottom( 5 );
                    // c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t2.addCell(c);        
                    
                }
                
            }
            
            return t2;        
    }

    public com.itextpdf.text.List getBulletListForSuggestions( java.util.List<String> sugLst ) throws Exception
    {
        com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );

        //Paragraph cHdr=null;
        //Paragraph cFtr=null;
        //float spcg = 8;
        cl.setListSymbol( "\u2022");

        for( String ct : sugLst )
        {
            if( ct==null || ct.isEmpty() )
                continue;

            cl.add( new ListItem( new Paragraph( ct , getFont() ) ) );
        }
            
        return cl;        
    }

    
    
    public Map<String,List<String>> getCompetencySuggestionInfoMap( List<TestEventScore> tesl ) throws Exception
    {
        Map<String,List<String>> out = new HashMap<>();
        
        List<String> sugs;
        
        String stub;
        
        SimCompetencyClass scc;
        
        for( TestEventScore tes : tesl )
        {
            scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );
            
            stub = getNameStubForCompetencyName( tes.getName(), tes.getNameEnglish(), tes.getSimCompetencyClassId() );
            
            stub += ".suggestions.";
            
            if( getIsHi(scc, tes.getScore(), tes.getName(), tes.getNameEnglish() ) )
            {
                sugs = getSugStringList( tes, stub, false );

                if( !sugs.isEmpty() )
                    out.put( tes.getName(), sugs );
            }
            
            if( getIsLow(scc, tes.getScore(), tes.getName(), tes.getNameEnglish() ) )
            {
                sugs = getSugStringList( tes, stub, true );

                if( !sugs.isEmpty() )
                    out.put( tes.getName(), sugs );            
            }
        }
        
        return out;
    }
    
    
    private java.util.List<String> getSugStringList( TestEventScore tes, String stub, boolean low )
    {
        List<String> out = new ArrayList<>();
        
        stub += low ? "lo" : "hi";
        
        String base = bmsg( stub );
        
        if( base == null || base.isEmpty() )
            return out;
        
        out.add( base );
        
        for( int i=1;i<10;i++ )
        {
            base = bmsg( stub + ".b" + i );

            if( base == null || base.isEmpty() )
                return out;

            out.add( base );
        }
        
        return out;
    }
    
    
    @Override
    public void addTopMatchesSummarySection() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel - 10;

            float y = addTitle( previousYLevel, bmsg( "b.TopMatches" ), null );

            y -= TPAD;

            int cols = 5;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.1f,  .25f, .15f, .15f, .15f } : new float[] { 0.15f, 0.15f, 0.15f, .25f, .1f };

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( true );
            
            
            
            // Create header
            c = new PdfPCell( new Phrase( bmsg( "b.Rank"), fontWhite ) );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setColspan( 1 );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            setRunDirection( c );
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "b.JobTitle" ), fontWhite ) );
            c.setColspan( 1 );
            //c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            
            /*
            c = new PdfPCell( new Phrase( bmsg( "s.CompetencyMatch" ), fontWhite ) );
            c.setBorder( Rectangle.TOP );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            */

            c = new PdfPCell( new Phrase( bmsg( "s.InterestMatch" ), fontWhite ) );
            //c.setBorder( Rectangle.TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( bmsg( "s.EducExperienceMatch" ), fontWhite ) );
            // c.setBorder( Rectangle.TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( bmsg( "s.OverallMatch" ), fontWhite ) );
            //c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT :  Rectangle.TOP | Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            setRunDirection( c );
            touter.addCell(c);

            
            int counter = 0;
            
            float match = 0;
            
            // OK, hearder done, now for the profiles
            for( Profile p : this.bestProfilesList )
            {
                counter++;
                
                c = new PdfPCell( new Phrase( Integer.toString(counter), font ) );
                
                if( counter== this.bestProfilesList.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setColspan( 1 );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( otrans( p.getSoc().getTitleSingular() ), font ) );
                c.setColspan( 1 );
                if( counter== this.bestProfilesList.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);

                /*
                match = Math.max(0, p.getFloatParam4());
                if( match>100 )
                    match=100;
                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber( reportData.getLocale(), match, 0 ), font ) );
                c.setColspan( 1 );
                if( counter== this.bestProfilesList.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                */

                match = Math.max(0, p.getFloatParam2());
                if( match>100 )
                    match=100;
                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber( reportData.getLocale(), match, 0 ), font ) );
                c.setColspan( 1 );
                if( counter== this.bestProfilesList.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                
                match = Math.max(0, p.getFloatParam3());
                if( match>100 )
                    match=100;
                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber( reportData.getLocale(), match, 0 ), font ) );
                c.setColspan( 1 );
                if( counter== this.bestProfilesList.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                
                
                // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getFloatParam1(), true), font ) );
                if( counter== this.bestProfilesList.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                
            }
            
            currentYLevel = addTableToDocument( y, touter );
            
            //touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCareerSoutReportTemplate.addTopMatchesSummarySection()" );

            throw e;
        }
        
    }
    
    
    
    public void addCompetencySummarySection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            
            Paragraph p = new Paragraph();
            
            p.add( new Phrase( bmsg( "s.CompetencySummaryTitle"), fontXLargeBold ) );
            
            List<Object> content = new ArrayList<>();
            
            content.add( bmsg("s.CompetencySummaryP1") );
            addOneColTable(p, // title para
                                null, // subtitle para
                                content, 
                                false, // header, 
                                fontLarge, 
                                null, // headerFont2Use,
                                0, 
                                0 ); // borderWidth )     
                                
            java.util.List<TestEventScore> teslA = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.ABILITY, false );
            teslA.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.ABILITY_COMBO, false ) );

            // Must resort since added a few groups
            Collections.sort(teslA, new DisplayOrderComparator() );  // new TESNameComparator() );
            
            java.util.List<TestEventScore> teslK = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.KNOWLEDGE, false );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.CORESKILL, false ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDESSAY, false ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDAUDIO, false ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDAVUPLOAD, false ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDTYPING, false ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDDATAENTRY, false ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDCHAT, false ) );
            teslK.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SKILL_COMBO, false ) );

            // Must resort since added a few groups
            Collections.sort(teslK, new DisplayOrderComparator() );  // new TESNameComparator());

            java.util.List<TestEventScore> teslP = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOGNITIVE, false );
            teslP.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOG_COMBO, true ) );
            teslP.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.INTERESTS_COMBO, true ) );
            Collections.sort(teslP, new DisplayOrderComparator() );  // new TESNameComparator());

            java.util.List<TestEventScore> teslE = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.EQ, false );
            Collections.sort(teslE, new DisplayOrderComparator() );  // new TESNameComparator());
            
            java.util.List<TestEventScore> teslB = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDBIODATA, false );
            teslB.addAll(getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.BIODATA_COMBO, false ) );
            Collections.sort(teslB, new DisplayOrderComparator() );  // new TESNameComparator());

            //java.util.List<TestEventScore> teslI = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDINTEREST, false );
            // Collections.sort(teslE, new TESNameComparator());
            
            int totalComps = teslA.size() + teslK.size() + teslP.size() + teslB.size() + teslE.size(); //  + teslI.size();
            
            LogService.logIt( "BaseCareerSoutReportTemplate.addCompetencySummarySection() BBB found " + totalComps + " competencies for summary section." );  
            
            if( !teslA.isEmpty() )
            {
                addCompetencySummarySection(teslA, "s.Abilities", SimCompetencyClass.ABILITY  );
                currentYLevel -= 8;
            }
            
            if( !teslK.isEmpty() )
            {
                addCompetencySummarySection(teslK, "s.KnowledgeSkills", SimCompetencyClass.KNOWLEDGE  );
                currentYLevel -= 8;
            }
            
            if( !teslP.isEmpty() )
            {
                addCompetencySummarySection(teslP, "s.PersonalityFactors", SimCompetencyClass.NONCOGNITIVE  );
                currentYLevel -= 8;
            }
            
            if( !teslE.isEmpty() )
            {
                addCompetencySummarySection(teslE, "s.EmotionalIntels", SimCompetencyClass.EQ  );
                currentYLevel -= 8;
            }
            
            if( !teslB.isEmpty() )
            {
                addCompetencySummarySection(teslB, "s.PastHistory", SimCompetencyClass.SCOREDBIODATA  );
                currentYLevel -= 8;
            }
            
            //if( !teslI.isEmpty() )
            //{
            //    addCompetencySummarySection(teslI, "s.Interests", SimCompetencyClass.SCOREDINTEREST  );
            //    currentYLevel -= 8;
            //}
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCareerSoutReportTemplate.addCompetencySummarySection()" );

            throw e;
        }                
    }
    
    
    public boolean showPercentile( SimCompetencyClass scc )
    {
        if( scc==null )
            return false;
        
        if( scc.isAbility() || scc.isKSA() || scc.isScoredEssay() || scc.isScoredAudio() || scc.isScoredAvUpload() || scc.isScoredTyping() || scc.isScoredDataEntry() || scc.isScoredChat()  )
            return true;
        
        return false;
    }
    
    
    
    @Override
    public void addPostDetailNotesSection() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();

            pars.add( bmsg("s.HowWeCreatedP1") );
            pars.add( bmsg("s.HowWeCreatedP2") );
            
            Paragraph tt = new Paragraph();            
            tt.add(new Phrase( bmsg( "s.HowWeCreatedT" ), fontXLargeBold ));
            
            addOneColTable(tt, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );     
            
            
            tt = new Paragraph();
            
            tt.add( new Phrase( bmsg( "b.WhyNotestScoresT" ), fontXLargeBold ) );
            
            pars.clear();
            
            pars.add( bmsg("b.WhyNotestScoresP1") );
            
            if( 1==1 )
                addOneColTable(tt, //Paragraph tableTitle, 
                                null, // Paragraph tableSubtitle, 
                                pars, 
                                false, // boolean header, 
                                fontLarge, //Font font2Use, 
                                null , 
                                0, 
                                0 );   
            
            float imageX = (pageWidth - mosaic3.getScaledWidth())/2; 

            ITextUtils.addDirectImage( pdfWriter, mosaic3, imageX, currentYLevel - mosaic3.getScaledHeight() - 20, false );
            
            currentYLevel -= mosaic3.getScaledHeight() + 30;
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCareerScoutReportTemplate.addPostDetailNotesSection()" );

            throw e;
        }        
    }
    
    
    
    
    public float addCompetencySummarySection( List<TestEventScore> tesl, String titleKey, SimCompetencyClass baseScc) throws Exception
    {
        try
        {
            boolean showPercentile = showPercentile( baseScc );
            
            SimCompetencyClass scc;
            
            float y = currentYLevel;
            
            int cols = showPercentile ? 4 : 3;
            float[] colRelWids = null;
            
            if( showPercentile )
                colRelWids = reportData.getIsLTR() ? new float[] { 0.25f,  .35f, .2f, .2f } : new float[] { 0.2f, .2f, .35f, .25f };
            
            else
                colRelWids = reportData.getIsLTR() ? new float[] { 0.25f,  .55f, .2f } : new float[] { 0.2f, .55f, .25f };
                

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( false );
            touter.setHeaderRows(1);
            // touter.set
            
            
            
            // Create header
            c = new PdfPCell( new Phrase( bmsg( titleKey ), fontWhite ) );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT  | Rectangle.BOTTOM );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setColspan( 2 );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            setRunDirection( c );
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "s.YourScore" ), fontWhite ) );
            c.setColspan( 1 );
            if( showPercentile )
                c.setBackgroundColor( ct2Colors.hraBlue );
            //    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM ); // | Rectangle.RIGHT );
            else
                c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
            //    c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT  | Rectangle.BOTTOM:  Rectangle.TOP | Rectangle.LEFT  | Rectangle.BOTTOM);
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            
            if( showPercentile  )
            {
                c = new PdfPCell( new Phrase( bmsg( "s.Percentile" ), fontWhite ) );
                // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT  | Rectangle.BOTTOM:  Rectangle.TOP | Rectangle.LEFT  | Rectangle.BOTTOM);
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
                setRunDirection( c );
                touter.addCell(c);
            }
            
            int counter = 0;
            
            String descrip;            
            String scoreStr;
            String percentileStr;
            
            // OK, header done
            for( TestEventScore tes : tesl )
            {
                scc =  SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );
                
                descrip = getDescriptionForCompetency( tes, scc);
                scoreStr = getScoreStrForCompetency( tes, scc );
                percentileStr = getPercentileStrForCompetency( tes, scc );
                
                counter++;
                
                c = new PdfPCell( new Phrase(  tctrans( tes.getName(), false), font ) );
                
                c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT  | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setColspan( 1 );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                setRunDirection( c );
                touter.addCell(c);
                
                

                c = new PdfPCell( new Phrase( descrip, font ) );
                c.setColspan( 1 );
                c.setBorder( Rectangle.BOTTOM);
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( scoreStr, font ) );
                c.setColspan( 1 );
                if( showPercentile )
                    c.setBorder( Rectangle.BOTTOM);
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                
                
                if( showPercentile  )
                {
                    // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                    c = new PdfPCell( new Phrase( percentileStr, font ) );
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    touter.addCell(c);
                }                
            }
            
            currentYLevel = addTableToDocument( y, touter );

            return currentYLevel;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "BaseCareerScoutReportTemplate.addCompetencySummarySection() tesl.size=" + tesl.size() );
            
            throw e;
        }
    }
    
    protected String getNameForCompetency( TestEventScore tes, SimCompetencyClass scc) throws Exception
    {
        String key = getNameStubForCompetencyName( tes.getName(), tes.getNameEnglish(), scc.getSimCompetencyClassId() );

        //LogService.logIt( "key: " + key);

        String v = bmsg( key );
        
        if( v==null )
            return key;
        
        return v;
    }
    
    protected String getDescriptionForCompetency( TestEventScore tes, SimCompetencyClass scc) throws Exception
    {
        String nameStub = getNameStubForCompetencyName( tes.getName(), tes.getNameEnglish(), scc.getSimCompetencyClassId() );

        //LogService.logIt( "NameStub: " + nameStub);

        String key = nameStub + ".description";

        String v = bmsg( key );

        if( v==null )
            return "Not found: " + key; //  key;

        return v;
    }

    protected String getNameStubForCompetencyName( String name, String nameEnglish, int simCompetencyClassId ) throws Exception
    {
        String n = nameEnglish==null || nameEnglish.trim().isEmpty() || nameEnglish.equals(name) ? name.trim() : nameEnglish.trim();

        return "sc." + simCompetencyClassId + "." + URLEncoder.encode(n ,"UTF8");
    }

    protected String getScoreStrForCompetency( TestEventScore tes, SimCompetencyClass scc) throws Exception
    {
        if( scc.equals( SimCompetencyClass.SCOREDEXPERIENCE ) )
        {
            String nameStub = getNameStubForCompetencyName( tes.getName(), tes.getNameEnglish(), scc.getSimCompetencyClassId() );

            LogService.logIt( "NameStub: " + nameStub);

            String key = nameStub + ".score." + ((int) tes.getScore());
            
            String v = bmsg( key );

            if( v==null )
                return key;

            return v;
        }

        // Normal Distribution Score Str
        if( scc.getIsInterest() )
        {
            if( tes.getScore() < 20 )
                return bmsg( "s.ScInterestVerySmall" );
            if( tes.getScore() < 40 )
                return bmsg( "s.ScInterestSmall" );
            if( tes.getScore() < 60 )
                return bmsg( "s.ScInterestMedium" );
            if( tes.getScore() < 80 )
                return bmsg( "s.ScInterestHigh" );            
            return bmsg( "s.ScInterestVeryHigh" );
        }
        
        
        // Normal Distribution Score Str
        if( showNormalScore(tes.getName(), tes.getNameEnglish(), scc ) )
        {
            if( tes.getScore() < 20 )
                return bmsg( "s.ScNormScoreVeryLow" );
            if( tes.getScore() < 40 )
                return bmsg( "s.ScNormScoreLow" );
            if( tes.getScore() < 60 )
                return bmsg( "s.ScNormScore" );
            if( tes.getScore() < 80 )
                return bmsg( "s.ScNormScoreHigh" );            
            return bmsg( "s.ScNormScoreVeryHigh" );
        }
        
        // Score vs Average String
        if( showScoreVsAvg( tes.getName(), tes.getNameEnglish(), scc ) )
        {
            if( tes.getScore() < 20 )
                return bmsg( "s.ScVeryLowAvg" );
            if( tes.getScore() < 40 )
                return bmsg( "s.ScLowAvg" );
            if( tes.getScore() < 60 )
                return bmsg( "s.ScAvg" );
            if( tes.getScore() < 80 )
                return bmsg( "s.ScHighAvg" );            
            return bmsg( "s.ScVeryHighAvg" );
        }

        // Numeric Score
        if( showNumericScore( scc ) )
            return I18nUtils.getFormattedNumber( reportData.getLocale(), tes.getScore(), 0 );
        
        // else score category
        if( tes.getScore() < 20 )
            return bmsg( "s.ScVeryLow" );
        if( tes.getScore() < 40 )
            return bmsg( "s.ScLow" );
        if( tes.getScore() < 60 )
            return bmsg( "s.Sc" );
        if( tes.getScore() < 80 )
            return bmsg( "s.ScHigh" );
        return bmsg( "s.ScVeryHigh" );
        
    }

    
    protected String getPercentileStrForCompetency( TestEventScore tes, SimCompetencyClass scc) throws Exception
    {
        if( !showPercentile( scc ) )
            return "-";
        
        if( tes.getPercentile()<0 )
            return "-";
        
        if( showNumericScore( scc ) )
            return I18nUtils.getFormattedNumber( reportData.getLocale(), tes.getPercentile(), 0 );
        
        return "-";
    }
    
    boolean showNumericScore( SimCompetencyClass scc )     
    {
        return scc.isKSA();
    }
                
    
    boolean showScoreVsAvg( String name, String nameEnglish, SimCompetencyClass scc )
    {        
        
        if( !scc.equals( SimCompetencyClass.EQ ) && !scc.equals( SimCompetencyClass.NONCOGNITIVE ) && !scc.equals( SimCompetencyClass.NONCOG_COMBO ) && !scc.equals( SimCompetencyClass.INTERESTS_COMBO ) )
            return false;        
        
        // Not normal
        if( showNormalScore(name, nameEnglish, scc) )
            return false;
            
        return true;
    }
    
    boolean showNormalScore( String name, String nameEnglish, SimCompetencyClass scc )
    {
        if( nameEnglish!=null && !nameEnglish.isEmpty() )
        {
            for( String s : NORMAL_SCORE_COMPS )
            {
                if( s.equalsIgnoreCase( nameEnglish ) )
                    return true;
            }
        }

        for( String s : NORMAL_SCORE_COMPS )
        {
            if( s.equalsIgnoreCase( name ) )
                return true;
        }

        return false;
    }
                
    @Override
    public void addIntroSection() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, reportData.getReportName(), null );

            y -= TPAD;

            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.35f, .35f, .2f } : new float[] { .2f, .35f , 0.35f };


            // String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            // String thirdPartyTestEventIdentifierName = reportData.getThirdPartyTestEventIdentifierName();

            // boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            // LogService.logIt( "BaseMdtReportTemplate.addCoverPage() thirdPartyTestEventIdentifierName=" + thirdPartyTestEventIdentifierName + ", hasThirdPartyId=" + hasThirdPartyId );


            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t2 = new PdfPTable( cols );

            setRunDirection(t2 );
            // float importanceWidth = 25;

            t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t2.setWidths( colRelWids );
            t2.setLockedWidth( true );
            // t.setHeaderRows( 1 );


            c = t2.getDefaultCell();
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            c = new PdfPCell( new Phrase( bmsg("b.ForC") , fontLarge ) );
            c.setColspan(1);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            c.setColspan(2);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            c = new PdfPCell( new Phrase( bmsg("b.EmailC") , fontLarge ) );
            c.setColspan(1);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            if( reportData.getUser().getUserType().getNamed() )
            {
                c = new PdfPCell( new Phrase( reportData.getUser().getEmail() , fontLarge ) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );
            }
            
            c = new PdfPCell( new Phrase( bmsg("b.PreparedDateC") , fontLarge ) );
            c.setColspan(1);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            c = new PdfPCell( new Phrase( getCurrentDateFormatted() , fontLarge ) );
            c.setColspan(2);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            if( getHasSponsor() )
            {
                c = new PdfPCell( new Phrase( bmsg( "b.SponsoringEmployerC" ) , fontLarge ) );
                c.setColspan(1);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );

                c = new PdfPCell( new Phrase( reportData.getOrgName() , fontLarge ) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );
            }
            
            
            c = new PdfPCell( new Phrase( "\n" + bmsg("s.IntroWhatTest")  , fontLarge ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            if( reportData.getTestEvent()!=null )
            {
                c = new PdfPCell( new Phrase( "\u2022" + " " + reportData.getSimName() , fontLarge ) );
                c.setColspan(1);
                c.setBorder( Rectangle.NO_BORDER );
                c.setPaddingLeft( 5 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                setRunDirection( c );
                t2.addCell( c );

                c = new PdfPCell( new Phrase( bmsg( "b.CompletedOnX", new String[] { I18nUtils.getFormattedDate(reportData.getLocale(), reportData.getTimeZone(), reportData.getTestEvent().getLastAccessDate() )}) , fontLarge ) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );                
            }


            c = new PdfPCell( new Phrase( "\n" + bmsg("b.HowToUseReport")  , this.fontXLargeBoldBlack ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            c = new PdfPCell( new Phrase( bmsg("s.HowToUseReportP1")  , fontLarge ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            c = new PdfPCell( new Phrase( bmsg("s.HowToUseReportP2")  , fontLarge ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            
            c = new PdfPCell( new Phrase( ( "\n\n\n\n\n\n\n\n\n\n" ) + bmsg("b.IntroNotesContinue")  , fontXLargeBold ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            setRunDirection( c );
            // touter.addCell( c );
            
            t2.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - t2.calculateHeights();
            
            float imageX = (pageWidth - mosaic2.getScaledWidth())/2; 

            ITextUtils.addDirectImage( pdfWriter, mosaic2, imageX, currentYLevel - mosaic2.getScaledHeight()-20, false );
            
            currentYLevel -= mosaic2.getScaledHeight()+30;
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCareerSoutReportTemplate.addIntroSection()" );

            throw e;
        }
    }

    
    @Override
    public void findMatchingJobSpecificTestEvent() throws Exception
    {
        jobSpecificTestEvent = reportData.getTestEvent();
    }
    
    /**
     * A test is eligible if its associated product is a  job-specific test. that's all it takes.  
     * @return 
     *
    @Override
    public boolean isValidForTestEvent()
    {
        try
        {
            loadBestProfilesAndEeoCategoryScores();

            if( bestProfilesList==null && this.eeoMatchList==null ) // || bestProfilesList.size() < MIN_MATCHING_PROFILES_TO_CREATE_REPORT )
            {
                LogService.logIt( "BaseCareerSoutReportTemplate.isValidForTestEvent() Denying report generation because not enough profiles match. ProfileCount=" + (bestProfilesList == null ? "0" : bestProfilesList.size() ) );
                return false;
            }

            if( reportData.getTestEvent()==null )
            {
                LogService.logIt( "BaseCareerSoutReportTemplate.isValidForTestEvent() Denying report generation because TestEvent is null" );
                LogService.logIt( "BaseCareerSoutReportTemplate.isValidForTestEvent() Denying report generation because not enough profiles match. ProfileCount=" + (bestProfilesList == null ? "0" : bestProfilesList.size() ) );
                return false;
            }

            if( reportData.getTestEvent().getProduct() == null )
            {
                LogService.logIt( "BaseCareerSoutReportTemplate.isValidForTestEvent() Denying report generation because product is null" );
                return false;
            }

            
            if( BestJobsReportUtils.hasRiasecCompetencies( reportData.getTestEvent() ) )
                return true;
            
            
            return false;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCareerSoutReportTemplate.isValidForTestEvent() " + ( reportData.getTestEvent()!=null ? reportData.getTestEvent().toString() : "Report data testEvent is null") );
            
            return false;
        }
    }
    */
    
    
    protected java.util.List<TestEventScore> getTestEventScoreListToShow( TestEventScoreType test, SimCompetencyClass scc, boolean hiLowOnly)
    {
        java.util.List<TestEventScore> out = new ArrayList<>();

        for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( test.getTestEventScoreTypeId() ) )
        {
            if( tes.getSimCompetencyClassId() != scc.getSimCompetencyClassId() )
                continue;

            // if supposed to hide
            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;
            
            if( hiLowOnly && !getIsHiLow( scc, tes.getScore(), tes.getName(), tes.getNameEnglish() ) )
                continue;
                

            out.add( tes );
        }

        Collections.sort( out, new DisplayOrderComparator() );  // new TESNameComparator() );

        return out;
    }
    
    public boolean getIsHiLow( SimCompetencyClass scc, float score, String name, String nameEnglish )
    {
        if( getIsHi( scc, score, name, nameEnglish ) )
            return true;

        if( getIsLow( scc, score, name, nameEnglish ) )
            return true;

        return false;
    }

    public boolean getIsHi( SimCompetencyClass scc, float score, String name, String nameEnglish )
    {
        if( scc.isScoredExperience() )
        {
            if( nameMatches( "Experience", name, nameEnglish )  )
                return score>1000;

            if( nameMatches( "Education", name, nameEnglish ) )
                return score>1000;

            if( nameMatches( "Training", name, nameEnglish ) )
                return score>1000;
        }
        
        if( scc.isKSA() )
            return score>=HI_SCORE_KSA;

        if( scc.isAIMS() || scc.isEQ() )
             return score>=HI_SCORE_NONCOG;

        if( scc.isBiodata() )
             return score>=HI_SCORE_BIO;
        
        return false;
    }

    public boolean getIsLow( SimCompetencyClass scc, float score, String name, String nameEnglish )
    {
        if( scc.isScoredExperience() )
        {
            if( nameMatches( "Experience", name, nameEnglish )  )
                return score<=LOW_SCORE_EXPERIENCE;

            if( nameMatches( "Education", name, nameEnglish ) )
                return score<=LOW_SCORE_EDUCATION;

            if( nameMatches( "Training", name, nameEnglish ) )
                return score<=LOW_SCORE_TRAINING;
        }
        
        if( scc.isKSA() )
            return score<=LOW_SCORE_KSA;

        if( scc.isAIMS() || scc.isEQ() )
             return score<=LOW_SCORE_NONCOG;

        if( scc.isBiodata() )
             return score<=LOW_SCORE_BIO;
        
        return false;
    }

    
    public boolean nameMatches( String toMatch, String name, String nameEnglish )
    {
        if( nameEnglish!=null && !nameEnglish.isEmpty() && toMatch.equalsIgnoreCase( nameEnglish ) )
            return true;

        if( name!=null && !name.isEmpty() && toMatch.equalsIgnoreCase( name ) )
            return true;
        
        return false;
    }

}
