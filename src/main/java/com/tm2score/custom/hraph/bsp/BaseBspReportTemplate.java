/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
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
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


/**
 *
 * @author Mike
 */
public abstract class BaseBspReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    private BspReportUtils bspReportUtils;
    
    static String BULLET = "\u2022";


    //public static String[] COMPETENCYNAMES = new String[] { "Analytical Skills", 
    //                                                 "Written Communication",
    //                                                 "Service Orientation",
    //                                                 "Cooperation and Collaboration",
    //                                                 "Adaptability" };
    
    @Override
    public abstract byte[] generateReport() throws Exception;


    
    
    
    protected void addCoreCompetenciesInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow(); // new ArrayList<>();

            if( tesl.size() <= 0 )
                return;

            // LogService.logIt( "BaseBspReportTemplate.addCoreCompetenciesInfo() found " + tesl.size() );

            addAnyCompetenciesInfo(tesl, "g.CorecompeDetailIntGuideTitle", null, "g.CorecompeDetailIntGuideSubtitle", null, "g.Detail", "g.Description", null, null, false, reportData.includeInterview() ? true : false, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBspReportTemplate.addCoreCompetenciesInfo()" );

            throw new STException( e );
        }
    }

    
    
    @Override
    public void addCompetencySummaryChart() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseBspReportTemplate.addCompetencySummary() Using locale: " + reportData.getLocale().toString() + ", g.AssessmentOverview=" + lmsg( "g.AssessmentOverview" ) );

            // If no info to present.
            if( reportData.getReport().getIncludeCompetencyScores()!=1 ||
                ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
                  reportData.getReport().getIncludeSubcategoryNumeric()!=1 )  )
                  return;

            previousYLevel =  currentYLevel;
            

            float y = addTitle(previousYLevel, lmsg( "g.CompetencySummary" ), competencySummaryStr, null, null );

            y -= TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .4f, .1f, .25f, .25f} : new float[] { .25f, .25f, .1f, .4f};

            boolean includeNumScores = true; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            // boolean includeStars = reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean includeColorGraph = reportData.getReport().getIncludeCompetencyColorScores()==1;

            int totalComps = 0;

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

            float importanceWidth = 25;

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
            c = new PdfPCell( new Phrase( lmsg( "g.Competency"), fontLargeWhite ) );
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


            // int totalRows = 1;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow();

            
            totalComps = tesl.size();

            if( tesl.isEmpty() )
            {
                LogService.logIt( "BaseBspReportTemplate.addSummaryChart() No Competencies found to include in Summary Chart." );
                return;
            }


            if( tesl.size() > 0 )
            {
                addCompetencySummaryChartSection(t, tesl, "g.CoreCompetencies", null, includeNumScores, false, false, includeColorGraph, false, false, true, false, false );
                //totalRows += teslA.size() + 1;
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
            LogService.logIt( e, "BaseBspReportTemplate.addSummaryChart()" );

            throw new STException( e );
        }
    }
    
    
    
    
    protected void addDevelopmentReportSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            
            float y = addTitle(previousYLevel, lmsg( "g.DevelopmentalSuggestions" ), lmsg( "g.DevelopmentalSuggestionsInfo" ), null, null );

            y -= 2*TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;
            
            TestEventScore tes;
            
            for( BspCompetencyType bct : BspCompetencyType.getListDevRpt() )
            {
                if( !bct.includeDevSuggestions() )
                    continue;
                
                tes = this.getTestEventScore( bct.getName() );
                
                if( tes==null )
                    tes = getTestEventScore( bct.getHraName() );
                
                if( tes == null && !RuntimeConstants.getBooleanValue( "reportDebugMode" ) )
                    continue;
                
                y = addDevelopmentTable( bct, tes, y );
                
                y -= TPAD; 
            }
                        
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBspReportTemplate.addDevelopmentReportSection()" );

            throw new STException( e );
        }
        
    }
    
    
    private float addDevelopmentTable( BspCompetencyType bct, TestEventScore tes, float y ) throws Exception
    {
        try
        {
            // LogService.logIt( "BaseBspReportTemplate.addDevelopmentTable() bct=" + bct.getName() + ", " + bct.getHraName() + ", tes=" + (tes==null ? "null" : tes.getName() ) );
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 1f,20f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            //t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.NO_BORDER );
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            //c.setBorderWidth( scoreBoxBorderWidth);
            //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 3 );
            setRunDirection( c );

            Font fontLB = getFontLargeBold();
            Font fontL = getFontLarge();
            Font font = getFont();
            
            String stub = bct.getStub() + ".";
            
            Phrase p = new Phrase( lmsg_spec( stub + "title" ) + ": ", fontLB );            
            p.add( new Chunk( lmsg_spec( stub + "def" ), fontL ) );
            
            c = new PdfPCell(p);            
            c.setColspan( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );                
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 5 );  
            setRunDirection( c );            
            t.addCell( c );
            
            c = new PdfPCell( new Phrase( lmsg_spec( stub + "def2" ), font ) );            
            c.setColspan( 2 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );  
            c.setBorder( Rectangle.BOX );
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 5 );
            setRunDirection( c );            
            t.addCell( c );

            String color = "green";
            //String shadeColor = "green";
           
            Color shadeColor;
            
            BaseColor shadeCol = ct2Colors.getGreen();            
            
            if( tes != null )
            {            
                if( tes.getScore()< 3f - (3f/4f))
                    color = "yellow";

                if( tes.getScore()< 3f/4f)
                    color = "red";
                
                if( tes.getScore()< 3f - (3f/4f))
                {
                    shadeCol = ct2Colors.getYellow();

                }

                if( tes.getScore()< 3f - 2*(3f/4f))
                {
                    shadeCol = ct2Colors.getRedyellow();                    
                }

                if( tes.getScore()< 3f/4f)
                {
                    shadeCol = ct2Colors.getRed();                    
                }
            }
            
            shadeColor = new Color( shadeCol.getRed(), shadeCol.getGreen(),shadeCol.getBlue() );
            
            shadeColor = lighten( shadeColor , 0.4f );

            
            // BaseColor cc = new BaseColor( shadeCol );
            
            
            
            shadeCol = new BaseColor( shadeColor.getRed(), shadeColor.getGreen(),shadeColor.getBlue() );
            
            // shadeCol
            
            List<String> buls = new ArrayList<>();
            
            String st;
                        
            for( int i=1; i<4; i++ )
            {
                st = lmsg_spec( stub + color + "." + i );
                
                if( st==null || st.trim().isEmpty() || st.equalsIgnoreCase("KEY NOT FOUND") )
                    break;
                
                buls.add( st );
            }
            
            for( int i=0; i<buls.size(); i++ )
            {
                c = new PdfPCell( new Phrase( BULLET, font ) );            
                c.setColspan( 1 );
                
                if( i<buls.size()-1 )
                    c.setBorder( Rectangle.LEFT );
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM );
                
                c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                c.setVerticalAlignment( Element.ALIGN_TOP );                
                c.setBorderWidth( scoreBoxBorderWidth);
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                // c.setBackgroundColor(shadeCol);
                c.setPadding( 3 );
                setRunDirection( c );            
                t.addCell( c );

                c = new PdfPCell( new Phrase( buls.get(i), font ) );            
                c.setColspan( 1 );                
                if( i<buls.size()-1 )
                    c.setBorder( Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.RIGHT | Rectangle.BOTTOM );
                
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );                
                c.setBorderWidth( scoreBoxBorderWidth);
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                // c.setBackgroundColor(shadeCol);                
                c.setPadding( 3 );

                if( i<buls.size()-1 )
                    c.setPaddingBottom( 5 );

                setRunDirection( c );            
                t.addCell( c );
            } 
            
            float thgt = t.calculateHeights();    
            
            // LogService.logIt( "BaseCT2ReportTemplate.addDevelopmentTable() thgt=" + thgt + ", y=" + y );

            if( thgt + 50 > y )
            {
                addNewPage();

                y = currentYLevel;
            }

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - thgt - 2*PAD;
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBspReportTemplate.addDevelopmentTable() " + bct.getName() );

            throw new STException( e );            
        }
        
        return currentYLevel;
    }
    
    public static Color lighten(Color inColor, float inAmount)
    {
      return new Color( (int) Math.min(255, inColor.getRed() + 255 * inAmount),
        (int) Math.min(255, inColor.getGreen() + 255 * inAmount ),
        (int) Math.min(255, inColor.getBlue() + 255 * inAmount ) );
    }    
    

    private List<TestEventScore> getTestEventScoreListToShow()
    {
        List<TestEventScore> out = new ArrayList<>();
        
        TestEventScore tes;
        
        // String name;
        
        for( BspCompetencyType bct : BspCompetencyType.getListDevRpt() )
        {
            // name = bct.getName();
            
            tes = getTestEventScore( bct.getName() );
            
            if( tes==null )
                tes = getTestEventScore( bct.getHraName() );
            
            if( tes != null )
                out.add( tes );
        }
        
        if( out.isEmpty() )
        {
            out.addAll( reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) );
        }
        
        Collections.sort( out );  
        
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
    protected void addPreparationNotesSection() throws Exception
    {
        try
        {
            prepNotes.clear();
            
            // LogService.logIt(  "BaseBspReportTemplate.addPreparationNotesSection() START" );

            //if( reportData.getReport().getIncludeNorms()>0 && hasComparisonData() )
            //     prepNotes.add( 0, lmsg( "g.CT3ComparisonVsOverallNote" ) );

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
                    if( ct.isEmpty() )
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
            LogService.logIt( e, "BaseBspReportTemplate.addPreparationNotesSection()" );

            throw new STException( e );
        }
    }

    
    
    
    

    @Override
    public PdfPTable getScoreKeyTable(boolean showProfileKey)
    {
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
    
    
    
    
    public void initMessages()
    {
        if( this.bspReportUtils==null )
            bspReportUtils = new BspReportUtils();
    }


    public String lmsg_spec( String key )
    {
        initMessages();
        
        return bspReportUtils.getKey(key );
    }

    public String lmsg_spec( String key, String[] prms )
    {
        initMessages();
        
        String msgText = bspReportUtils.getKey(key );
        
        return MessageFactory.substituteParams(Locale.US , msgText, prms );
    }
    
    


}
