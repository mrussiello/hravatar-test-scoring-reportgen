/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp.itss;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import static com.tm2score.custom.bestjobs.BaseBestJobsReportTemplate.BEST_JOBS_BUNDLE;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOXHEADER_LEFTPAD;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.purchase.Product;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TESScoreComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.MessageFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


/**
 *
 * @author Mike
 */
public abstract class BaseItssReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    
    public Itss itss;
    
    //public static String REPORT_BUNDLE = "com.tm2score.custom.hraph.bsp.itss.ItssMessages";
    
    //static String[] FOUNDATION_COMPETENCIES = new String[] { "Analytical Thinking","Attention to Detail","Multitasking","Service Orientation","Written Communication","Cooperation and Collaboration","Adaptability"}; 
    
    //static String[] RIASEC_COMPETENCIES = new String[] {"Realistic","Investigative","Artistic","Social","Enterprising","Conventional" }; 
    
    //static String BULLET = "\u2022";

    public Image checkbox_unchecked = null;
    public Image checkbox_checked = null;
    public static String checkbox_uncheckedFilename = "tick-box-unchecked_80.png";
    public static String checkbox_checkedFilename = "tick-box-checked_80.png";

    boolean topScoreMatchesTopPreferred = false;
    boolean topScoreMatchesAnyPreferred = false;
    boolean top3ScoresMatchAnyPreferred = false;
    
    
    //public static String[] COMPETENCYNAMES = new String[] { "Analytical Thinking", 
    //                                                 "Written Communication",
    //                                                 "Service Orientation",
    //                                                 "Collaboration and Cooperation",
    //                                                 "Adaptability" };
    
    @Override
    public abstract byte[] generateReport() throws Exception;


    @Override
    public synchronized void initFonts() throws Exception
    {
        super.initFonts();
        
        initImages();
    }
    
    
    public synchronized void initImages() throws Exception
    {
        
        checkbox_unchecked = ITextUtils.getITextImage( getBspImageUrl( BaseItssReportTemplate.checkbox_uncheckedFilename ));        
        checkbox_unchecked.scalePercent( 60 );

        checkbox_checked = ITextUtils.getITextImage( getBspImageUrl( BaseItssReportTemplate.checkbox_checkedFilename ));        
        checkbox_checked.scalePercent( 60 );
    }
    
    
    
    public void addRoleFitCompetencySummary() throws Exception
    {
        try
        {
            List<String> prefRoleList = getPreferredRoleList();
            
            List<TestEventScore> altRoleTesl = reportData.te.getTestEventScoreList( TestEventScoreType.ALT_OVERALL.getTestEventScoreTypeId() );

            if( altRoleTesl.isEmpty() && prefRoleList.isEmpty() )
            {
                //LogService.logIt( "BaseItssReportTemplate.addRoleFitCompetencySummary() altRoleTesl.size()=" + altRoleTesl.size() + ", prefRoleList.size=" + prefRoleList.size() );
                return;
            }
            
            Collections.sort( altRoleTesl, new TESScoreComparator() );            
            Collections.reverse( altRoleTesl );
            
            previousYLevel =  currentYLevel;
            
            float y = addTitle( previousYLevel, bmsg( "g.RoleFitCompetencySummary" ), competencySummaryStr );

            y -= TPAD;
            
            int cols = 2;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .5f, .5f} : new float[] { .5f, .5f };
                        
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            // t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( bmsg( "g.Top3OnScores"), fontLargeWhite ) );
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
                    
            c = new PdfPCell( new Phrase( bmsg( "g.Top3OnPreferred"), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
                
            String prefRole;
            TestEventScore rankedTestEventScore;
            String val;
            
            for( int i=0;i<3;i++ )
            {
                rankedTestEventScore = altRoleTesl.size()>i ? altRoleTesl.get(i) : null;
                
                prefRole = prefRoleList.size()>i ? prefRoleList.get(i) : null;
                
                if( rankedTestEventScore==null && prefRole==null )
                    break;

                val = rankedTestEventScore==null ? "" : (i+1) + ". " + rankedTestEventScore.getName();                
                c = new PdfPCell( new Phrase( val, font ) );
                if( i<2 )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );                    
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD + TPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setBackgroundColor( BaseColor.WHITE );
                setRunDirection( c );
                t.addCell(c);

                val = prefRole==null ? "" : (i+1) + ". " + prefRole;                
                c = new PdfPCell( new Phrase( val, font ) );
                if( i<2 )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.BOX );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD + TPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setBackgroundColor( BaseColor.WHITE );
                setRunDirection( c );
                t.addCell(c);
            }
            
            currentYLevel = addTableToDocument(y, t, false, true ) - TPAD;
            
            // Next, compute the checkbox values
            topScoreMatchesTopPreferred = false;
            topScoreMatchesAnyPreferred = false;
            top3ScoresMatchAnyPreferred = false;
            
            // Re-sort just in case.
            Collections.sort( altRoleTesl, new TESScoreComparator() );            
            Collections.reverse( altRoleTesl );
            
            if( altRoleTesl.size()>3 )
                altRoleTesl = altRoleTesl.subList( 0,3 );
            
            TestEventScore topRoleScr = altRoleTesl.size()>0 ? altRoleTesl.get(0) : null;
            String topPrefRole = prefRoleList.size()>0 ? prefRoleList.get(0) : null;
            
            //LogService.logIt( "TopRoleScr=" + topRoleScr.getName() + ", topPrefRole=" + topPrefRole );
            
            //for( int i=0;i<3;i++ )
            //{
            //    LogService.logIt( (i+1) + ", RoleScr =" + altRoleTesl.get(i).getName() + ", topPrefRole=" + prefRoleList.get(i) );
                
            //}
            
            if( topRoleScr!=null && topPrefRole!=null && !topPrefRole.isEmpty() && topPrefRole.trim().equalsIgnoreCase( topRoleScr.getName().trim() )  )
                topScoreMatchesTopPreferred = true;
            
            else if( topRoleScr!=null && !prefRoleList.isEmpty() )
            {
                for( String pr : prefRoleList )
                {
                    if( pr!=null && !pr.trim().isEmpty() && pr.trim().equalsIgnoreCase( topRoleScr.getName().trim() ) )
                    {
                        topScoreMatchesAnyPreferred = true;
                        break;
                    }
                }
            }
            
            // If the first two criteria are met, the third is also met
            if( topScoreMatchesTopPreferred || topScoreMatchesAnyPreferred )
                top3ScoresMatchAnyPreferred = true;
            
            // check if any of the score top 3 are preferred
            else
            {
                // top 3 scores
                for( TestEventScore tex : altRoleTesl )
                {                    
                    for( String pr : prefRoleList )
                    {
                        if( pr!=null && !pr.trim().isEmpty() && pr.trim().equalsIgnoreCase( tex.getName().trim() ) )
                        {
                            top3ScoresMatchAnyPreferred = true;
                            break;
                        }
                    }
                }
            }
                        
            // LogService.logIt( "BaseItssReportTemplate.addRoleFitCompetencySummary() topScoreMatchesTopPreferred=" + topScoreMatchesTopPreferred + ", topScoreMatchesAnyPreferred=" + topScoreMatchesAnyPreferred + ", top3ScoresMatchAnyPreferred=" +top3ScoresMatchAnyPreferred );
            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseItssReportTemplate.addRoleFitCompetencySummary()" );

            throw new STException( e );
        }
    }
    
    
    
    public void addAltRoleFitScoreSummary() throws Exception
    {
        try
        {
            List<TestEventScore> altRoleTesl = reportData.te.getTestEventScoreList( TestEventScoreType.ALT_OVERALL.getTestEventScoreTypeId() );

            if( altRoleTesl.isEmpty() )
                return;

            Collections.sort( altRoleTesl, new TESScoreComparator() );
            Collections.reverse(altRoleTesl);

            int count = 0;
            
            ListIterator<TestEventScore> iter = altRoleTesl.listIterator();
            TestEventScore tes;
            
            while( iter.hasNext() )
            {
                tes = iter.next();
                count++;                
                tes.setDisplayOrder(count);
            }
            
            // re-sort by name so always in the same 
            // Collections.sort( altRoleTesl, new TESNameComparator() );
            
            previousYLevel =  currentYLevel;
            
            float y = addTitle( previousYLevel, bmsg( "g.RoleFitScoreSummary" ), competencySummaryStr );

            y -= TPAD;
            
            int cols = altRoleTesl.size() + 1;
            
            float[] colRelWids = new float[cols]; // reportData.getIsLTR() ? new float[] { .5f, .5f} : new float[] { .5f, .5f };
                        
            for( int i=0;i<cols;i++ )
            {
                colRelWids[i]=1f;
            }
            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            // t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( bmsg( "g.JobFamily"), fontLargeWhite ) );
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
            
            iter = altRoleTesl.listIterator();     
            
            // finish the header
            while( iter.hasNext() )
            {
                tes = iter.next();
                
                c = new PdfPCell( new Phrase( tes.getName(), fontWhite ) );
                
                // Not the last column
                if( iter.hasNext() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT  );
                    
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);                
            }
            
            // First row
            c = new PdfPCell( new Phrase( bmsg("g.WeightedScore"), font ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell(c);
            
            iter = altRoleTesl.listIterator();
            
            // finish the row
            while( iter.hasNext() )
            {
                tes = iter.next();

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(this.getReportLocale(), tes.getScore(), 2), font ) );
                                
                // Not the last column
                if( iter.hasNext() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT  );                    
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                c.setBackgroundColor( BaseColor.WHITE );
                setRunDirection( c );
                t.addCell(c);
            }

            // Second row
            c = new PdfPCell( new Phrase( bmsg("g.Rank"), font ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell(c);
            
            iter = altRoleTesl.listIterator();
            
            count = 0;
            
            // finish the row
            while( iter.hasNext() )
            {
                tes = iter.next();
                

                c = new PdfPCell( new Phrase( Integer.toString(tes.getDisplayOrder()), font ) );
                                
                // Not the last column
                if( iter.hasNext() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.BOX );                    
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                c.setBackgroundColor( BaseColor.WHITE );
                setRunDirection( c );
                t.addCell(c);
            }
                        
            currentYLevel = addTableToDocument(y, t, false, true ) - TPAD;            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseItssReportTemplate.addAltRoleFitScoreSummary()" );

            throw new STException( e );
        }
    }
    
    
    
    public void addFindingsRecommendationsSummary() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            

            float y = addTitle( previousYLevel, bmsg( "g.FindingsRecommendations" ), competencySummaryStr );

            y -= TPAD;
            
            int cols = 2;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .10f, .95f} : new float[] { .95f, .10f };
                        
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            // t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            addFindingsTableRow( t, topScoreMatchesTopPreferred, 1 );
            addFindingsTableRow( t, topScoreMatchesAnyPreferred && !topScoreMatchesTopPreferred, 2 );
            addFindingsTableRow( t, top3ScoresMatchAnyPreferred && !topScoreMatchesAnyPreferred && !topScoreMatchesTopPreferred, 3 );
            addFindingsTableRow( t, !top3ScoresMatchAnyPreferred && !topScoreMatchesAnyPreferred && !topScoreMatchesTopPreferred, 4 );
            
            currentYLevel = addTableToDocument(y, t, false, true ) - TPAD;            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseItssReportTemplate.addFindingsRecommendationsSummary()" );

            throw new STException( e );
        }
    }
    
    private void addFindingsTableRow( PdfPTable t, boolean checked, int index ) throws Exception
    {
            Image imageToShow = checked ? this.checkbox_checked : this.checkbox_unchecked;
        
            // First Row
            PdfPCell c = new PdfPCell( imageToShow );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setPadding( 3 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBackgroundColor( BaseColor.WHITE );
            c.setRowspan(2);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( bmsg("g.FindingsTitle" + index ), fontBold ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase(   " " + "\u2022" + " " + bmsg("g.FindingsBullet" + index ), font ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell(c);
        
    }
    
    
    private List<String> getPreferredRoleList() throws Exception
    {
        try
        {
            return ScoreFormatUtils.getSingleStringBasedResponseList( reportData.getTestEvent(), Constants.PREFERREDROLENAMES );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseItssReportTemplate.getPreferredRoleList() " );
            
            throw new STException(e);
        }
    }
    
    
    @Override
    public void addCompetencySummaryChart() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseItssReportTemplate.addCompetencySummary() Using locale: " + reportData.getLocale().toString() + ", g.AssessmentOverview=" + lmsg( "g.AssessmentOverview" ) );

            // If no info to present.
            if( reportData.getReport().getIncludeCompetencyScores()!=1 ||
                ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
                  reportData.getReport().getIncludeSubcategoryNumeric()!=1 )  )
                  return;

            previousYLevel =  currentYLevel;
            

            float y = addTitle( previousYLevel, lmsg( "g.CompetencySummary" ), competencySummaryStr );

            y -= TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .4f, .1f, .25f, .25f} : new float[] { .25f, .25f, .1f, .4f};

            boolean includeNumScores = true; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            // boolean includeStars = reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean includeColorGraph = reportData.getReport().getIncludeCompetencyColorScores()==1;

            //int totalComps = 0;

            if( !includeColorGraph )
            {
                // includeStars = false;
                cols -= 2;
                colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f } : new float[] { .2f, .45f };
            }

            else
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f, .35f} :  new float[] { .35f, .2f, .45f};
            }


            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );
            // t.setHeaderRows( 1 );


            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );


            // int totalRows = 1;

            addCompetencySummaryTableHeaderRow(  t,  includeNumScores,  includeColorGraph  );
            
            java.util.List<TestEventScore> cteslx = getSpecTestEventScoreList( itss.getCompetencies() );
            
            java.util.List<TestEventScore> ctesl = new ArrayList<>();
            
            Map<String,List<String>> comboChildren = itss.getCompetencyChildrenToShow();
            
            TestEventScore tes2;            
            List<String> children;
            
            for( TestEventScore tes : cteslx )
            {
                ctesl.add( tes );
                    
                if( comboChildren==null || comboChildren.isEmpty() || comboChildren.get( tes.getName() )==null )
                    continue;
                
                children = comboChildren.get( tes.getName() );
                
                if( children==null || children.isEmpty() )
                    continue;
                
                for( String child : children )
                {
                    tes2 = this.getTestEventScore( child );
                    
                    if( tes2==null )
                        continue;
                    
                    tes2 = (TestEventScore) tes2.clone();
                    
                    tes2.setName( "      " + tes2.getName() );
                    
                    ctesl.add( tes2 );
                }
            }
            
            java.util.List<TestEventScore> rtesl = getSpecTestEventScoreList( itss.getRiasecCompetencies() );
            
            // Collections.sort( ctesl, new TESNameComparator() );
            Collections.sort( rtesl, new TESScoreComparator( true ) );
            
            //totalComps = ctesl.size();

            if( ctesl.isEmpty() && rtesl.isEmpty()  )
            {
                LogService.logIt( "BaseItssReportTemplate.addSummaryChart() No Competencies found to include in Summary Chart." );
                return;
            }

            if( !ctesl.isEmpty() )
                addCompetencySummaryChartSection(t, ctesl, "g.FoundationComps", null, includeNumScores, false, false, includeColorGraph, false, false, true, false, true );

            if( !rtesl.isEmpty() )
            {
                if( !ctesl.isEmpty() )
                    addCompetencySummaryTableHeaderRow(  t,  includeNumScores,  includeColorGraph  );
                
                addCompetencySummaryChartSection(t, rtesl, "g.CareerInterest", null, includeNumScores, false, true, includeColorGraph, false, false, true, false, false );                
            }

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            float thgt = t.calculateHeights();

            float twid = t.getTotalWidth();

            
            
            currentYLevel = y - t.calculateHeights();

            if( devel )
                currentYLevel = currentYLevel - 10;

            
            previousYLevel =  currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseItssReportTemplate.addSummaryChart()" );

            throw new STException( e );
        }
    }
    
    
    protected void addCompetencySummaryTableHeaderRow( PdfPTable t, boolean includeNumScores, boolean includeColorGraph  )
    {       
            // Create header
            PdfPCell c = new PdfPCell( new Phrase( lmsg( "g.Competency"), fontLargeWhite ) );
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

                //if( includeStars )
                //    c.setBorder( Rectangle.TOP );
                //else
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
                    c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);
                }
            }
    }
    
    
    private List<TestEventScore> getSpecTestEventScoreList( String[] nameList )
    {
        List<TestEventScore> out = new ArrayList<>();
        
        TestEventScore tes;
        
        for( String name : nameList )
        {
            tes = getTestEventScore( name );
            
            if( tes != null )
            {
                out.add( tes );
                
                
            }
        }
        
        if( out.isEmpty() )
        {
            out.addAll( reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) );
        }
        
        // Collections.sort( out, new TESNameComparator() );
                
        return out;
    }
    
    
    private TestEventScore getTestEventScore( String name )
    {
        if( name==null || name.isEmpty() )
            return null;
        
        for( TestEventScore tes : reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
        {
            if( tes.getName()!=null && tes.getName().equals(name))
                return tes;
            if( tes.getNameEnglish()!=null && tes.getNameEnglish().equals(name))
                return tes;
        }
        
        return null;
    }
    
    
    
    
    
    

    @Override
    public PdfPTable getScoreKeyTable(boolean showProfileKey)
    {
        
        TestEventScore tes = reportData.te.getOverallTestEventScore();
        
        if( tes!=null )
        {
            ScoreFormatType sft = ScoreFormatType.getValue( tes.getScoreFormatTypeId() );
            
            if( !sft.equals( ScoreFormatType.NUMERIC_0_TO_3 ))
                return super.getScoreKeyTable( true );            
        }
        
            // First, add a table
            PdfPTable t = new PdfPTable( reportData.getIsLTR() ?  new float[]{0.2f,0.8f} : new float[]{0.8f,0.2f} );

            // float importanceWidth = 25;

            t.setTotalWidth( 150 );
            t.setLockedWidth( true );
            setRunDirection( t );
            // t.setHeaderRows( 1 );


            PdfPCell c = t.getDefaultCell();
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );

            // First Row
            c = new PdfPCell( new Phrase( lmsg( "g.Key"), fontLightBold ) );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( 3 );
            c.setPaddingRight( 3 );
            c.setColspan( 2 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell( c );

            // Pointer
            // First Row
            c = new PdfPCell( reportPointer );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( devel ? "g.YourScore" : "g.CandidateScore"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( keyRedBar );
            c.setPaddingLeft( 4 );
            c.setPaddingRight( 4 );
            c.setPaddingTop( 2 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "Level 0", fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( keyRedYellowBar );
            c.setPaddingLeft( 4 );
            c.setPaddingRight( 4 );
            c.setPaddingTop( 2 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "Level 1", fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);
            
            
            //if( devel )
            //{
                c = new PdfPCell( keyYellowBar );
                c.setPaddingLeft( 4 );
                c.setPaddingRight( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.keyBackgroundColor );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( "Level 2", fontSmallLight ) );
                c.setPadding( 1 );
                c.setPaddingRight( 3 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.keyBackgroundColor);
                setRunDirection( c );
                t.addCell(c);                
            //}//

            c = new PdfPCell( keyGreenBar );
            c.setPaddingLeft( 4 );
            c.setPaddingRight( 4 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "Level 3", fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);

            if( !devel && ( reportData.p != null && reportData.p.getStrParam3()!=null && !reportData.p.getStrParam3().isEmpty() ) )
            {
                c = new PdfPCell( keyBlueBar );
                c.setPaddingLeft( 4 );
                c.setPaddingRight( 4 );
                c.setPaddingTop( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.keyBackgroundColor );
                c.setPaddingBottom( 4 );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg( "g.CustomProfile"), fontSmallLight ) );
                c.setPadding( 1 );
                c.setPaddingRight( 3 );
                c.setPaddingBottom( 4 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.keyBackgroundColor);
                setRunDirection( c );
                t.addCell(c);
            }
            
            c = new PdfPCell( new Phrase( " \n ", fontSmallLight ) );
            c.setPadding( 0 );
            c.setColspan(2);
            c.setPaddingBottom( 2 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell(c);

            return t;
    }
    
    
    
    @Override
    protected void addPreparationNotesSection() throws Exception
    {
        try
        {


            if( !devel )
                prepNotes.add( 0, lmsg( "g.CT3RptCaveat" ) );
            else
                prepNotes.add( 0, lmsg( "g.CT3RptCaveatDevel" ) );

            Product p = reportData.getTestEvent().getProduct();
            
            Calendar cal = new GregorianCalendar();            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");            
            String dtStr = df.format( cal.getTime() );

             prepNotes.add( lmsg( "g.SimIdAndVersion", new String[]{ Long.toString( reportData.getTestEvent().getSimId()) , Integer.toString(reportData.getTestEvent().getSimVersionId() ), Long.toString( reportData.getTestEvent().getTestKeyId()), Long.toString( reportData.getTestEvent().getTestEventId()), Long.toString( reportData.getReport().getReportId() ), Integer.toString( reportData.getTestKey().getProductId() ), dtStr } ));

            if( prepNotes.isEmpty() )
                return;

            addNewPage();
            
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
            LogService.logIt( e, "BaseItssReportTemplate.addPreparationNotesSection()" );

            throw new STException( e );
        }
    }
    
    
    
    
    // BestJobs Locale key
    public String bmsg( String key )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict(itss.getBundleName(), null, reportData.getLocale(), key, null );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage(itss.getBundleName(), reportData.getLocale() , key, null );
    }

    // BestJobs Locale key
    public String bmsg( String key, String[] prms )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict(itss.getBundleName(), null, reportData.getLocale(), key, prms );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage( BEST_JOBS_BUNDLE, reportData.getLocale() , key, prms );
    }


    private URL getBspImageUrl( String fileName )
    {
        return com.tm2score.util.HttpUtils.getURLFromString( getBspBaseImageUrl() + "/" + fileName );
    }
    
    private String getBspBaseImageUrl()
    {
        return RuntimeConstants.getStringValue( "baseurl" ) + "/resources/images/bsp";
    }
    

    
    
}
