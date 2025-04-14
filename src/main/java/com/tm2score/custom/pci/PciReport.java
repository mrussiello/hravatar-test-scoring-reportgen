/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.pci;



import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class PciReport extends BasePciReportTemplate implements ReportTemplate
{
    
    public static String[] VALUES1 = new String[] {"Excellence" , "Human Potential" , "Integrity" , "Culture", "Conscientiousness" , "Positive Future Outlook" }; 
    public static String VALUES2 = "Expectation of Being Led by a Servant Leader"; 
    public static String[] CAND_CRITERIA = new String[] {"Emotional Intelligence" , "Positive Future Outlook" }; 

    

    
    public PciReport()
    {
        super();        
        this.devel = false;        
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            initPciSpecial();
            // this.redYellowGreenGraphs=false;
            // LogService.logIt( "CTSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(true);

            addNewPage();

            addReportInfoHeader();

            addPciCompetencySummaryTable1();
            
            // 3/14/2021 - Disabled per email from Cherren
            // addPciCompetencySummaryTable2();

            addComparisonSection();

            addNewPage();

            addAssessmentOverview();

            addDetailedReportInfoHeader();
            
            // addAltScoreSection();            

            //// Tasks before competencies
            //if( reportData.getReport().getIncludeTaskInfo() == 1)
            //    addTasksInfo();

            
            addAbilitiesInfo();

            addKSInfo();

            addAIMSInfo();

            addBiodataInfo();

            addEQInfo();
            
            addAIInfo();

            addWritingSampleInfo();
            
            //addIbmInsightSection();

            addIdentityImageCaptureSection();

            //addSuspiciousActivitySection();

            //addSuspensionsSection();

            //addItemScoresSection();


            // addCompetencyInfo();

            // Tasks after competencies
            //if( reportData.getReport().getIncludeTaskInfo() == 2)
            //    addTasksInfo();

            addTopJobMatchesSummarySection();

            //addMinQualsApplicantDataInfo();

            //addEducTrainingInfo();

            //addReportRiskFactorSection();

            addUploadedFilesSection();

            addPreparationNotesSection();

            addCalculationSection(true);

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
            LogService.logIt( e, "PciReport.generateReport() " );
            throw new STException( e );
        }
    }


    
    public void addPciCompetencySummaryTable1() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseCT2ReportTemplate.addPciCompetencySummaryTable1() Using locale: " + reportData.getLocale().toString() + ", g.AssessmentOverview=" + lmsg( "g.AssessmentOverview" ) );
            java.util.List<TestEventScore> teslA = new ArrayList<>();            
            for( String n : VALUES1 )
            {
                for( TestEventScore tx : reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
                {
                    if( StringUtils.isValidNameMatch( n, n, tx.getName(), tx.getNameEnglish() ) )
                    {
                        teslA.add(tx);
                        break;
                    }
                }
            }
            TestEventScore tes2 = null;
            
            // 3/14/2021 - Disabled per email from Cherren.
            if( 1==2 )
            {
                for( TestEventScore tx : reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
                {
                    if( StringUtils.isValidNameMatch( VALUES2, VALUES2, tx.getName(), tx.getNameEnglish() ) )
                    {
                        tes2 = tx;
                        break;
                    }
                }
            }            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t;
            
            if( tes2==null && teslA.isEmpty() )
            {
                LogService.logIt( "PciReport.addPciCompetencySummaryTable1() No TestEventScores found for Table 1");
                return;
            }
            
            currentYLevel -= 2*TPAD;
            previousYLevel =  currentYLevel;
            float y = addTitle(previousYLevel, "Selecting Candidates on Our Values", null, null, null );
            y -= TPAD;  

            // First, add a table
            t = new PdfPTable( 3 );
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( new float[] { .45f, .2f, .45f} );
            t.setLockedWidth( true );
            setRunDirection( t );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            addHeaderRow( "Value", t );

            // Add first four competencies.
            addCompetencySummaryChartSection(t, teslA, null, null, true, false, false, true, false, false, tes2==null, false, false );

            // Add Expectation of Being Led by a Servant Leader statement
            if( tes2!=null )
            {
                c = new PdfPCell( new Phrase( "  " + tes2.getName() , fontLight ) );
                c.setPadding( 1 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM );
                c.setPaddingBottom( 7 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( "There is no scientifically validated measure for this value.", fontLight ) );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding(1);
                c.setPaddingBottom( 7 );
                c.setColspan(2);
                //c.setPaddingTop( 3 );
                //c.setPaddingBottom( 3 );
                c.setPaddingRight( 8 );
                setRunDirection( c );
                c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );                    
                t.addCell( c );
            }

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - t.calculateHeights() - 4*TPAD;            
            previousYLevel =  currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PciReport.addPciCompetencySummaryTable1()" );

            throw new STException( e );
        }
    }
    

    public void addPciCompetencySummaryTable2() throws Exception
    {
        try
        {           
            // LogService.logIt( "BaseCT2ReportTemplate.addPciCompetencySummaryTable1() Using locale: " + reportData.getLocale().toString() + ", g.AssessmentOverview=" + lmsg( "g.AssessmentOverview" ) );
            java.util.List<TestEventScore> teslB = new ArrayList<>();
            for( String n : CAND_CRITERIA )
            {
                for( TestEventScore tx : reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
                {
                    if( StringUtils.isValidNameMatch( n, n, tx.getName(), tx.getNameEnglish() ) )
                    {
                        teslB.add(tx);
                        break;
                    }
                }
            }

            if( teslB.isEmpty() )
            {
                LogService.logIt( "PciReport.addPciCompetencySummaryTable2() No TestEventScores found for Table 2");
                return;
            }
            
            previousYLevel =  currentYLevel;
            float y = addTitle(previousYLevel, "Other Candidate Criteria", null, null, null );
            y -= TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            PdfPCell c;
            
            // First, add a table
            PdfPTable t = new PdfPTable( 3 );
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( new float[] { .45f, .2f, .45f} );
            t.setLockedWidth( true );
            setRunDirection( t );


            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            addHeaderRow( "Characteristic", t );

            // addCompetencySummaryChartSection(   PdfPTable t, java.util.List<TestEventScore> tesl, String ttlKey, String titleText, boolean includeNumScores, boolean incCompetencyDescrips, boolean includeColorGraph, boolean hasSpectrum, boolean includeStars, boolean last, boolean showRedRange, boolean useTesNameOnly) throws Exception
            // Add first four competencies.
            addCompetencySummaryChartSection(t, teslB, null, null, true, false, false, true, false, false, true, false, false );

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - t.calculateHeights();            
            previousYLevel =  currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PciReport.addPciCompetencySummaryTable2()" );

            throw new STException( e );
        }
    }

    private void addHeaderRow( String cola, PdfPTable t )
    {
            PdfPCell c;
            
            // Create header
            c = new PdfPCell( new Phrase( cola, fontLargeWhite ) );
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

            c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
            c.setColspan( 1 );
            c.setBorder( Rectangle.TOP ); 
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( "Comparison", fontLargeWhite ) );
            c.setColspan( 1 );
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT );                    
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
