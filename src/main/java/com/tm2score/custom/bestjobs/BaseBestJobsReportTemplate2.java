/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.bestjobs;

import com.itextpdf.text.Element;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.profile.Profile;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.onet.OnetJobZoneType;
import com.tm2score.profile.ProfileFloat1Comparator;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
public abstract class BaseBestJobsReportTemplate2 extends BaseBestJobsReportTemplate implements ReportTemplate
{
    OnetJobZoneType oneLevelUpJobZone = null;
    
    
    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; 
        // LogService.logIt( "BaseBestJobsReportTemplate2.init() AAA " );
        computeBaseJobZone();
        // LogService.logIt( "BaseBestJobsReportTemplate2.init() BBB jobZone=" + (jobZone==null ? "null" : jobZone.getJobZoneId()) );
                
        super.init(rd);
        
        
    }
    
    
    public void addJobZoneInfoSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, bmsg( "b.JobZonesTitle" ), null );
            // y -= TPAD;
            
            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 1f } );

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            t.setLockedWidth( true );
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setKeepTogether( true );
            setRunDirection( t );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.NO_BORDER );
            c.setPaddingTop( 6 );
            c.setPaddingBottom( 6 );
            
            
            c = new PdfPCell( new Phrase(bmsg("b.JobZones.P4.Your", new String[]{bmsg(jobZone.getNamexKey()),bmsg( jobZone.getDescripKey())} ), fontLargeBold ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPaddingTop( 6 );
            c.setPaddingBottom( 6 );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase(bmsg("b.JobZones.P1"), fontLarge));
            c.setBorder( Rectangle.NO_BORDER );
            c.setPaddingTop( 6 );
            c.setPaddingBottom( 6 );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase(bmsg("b.JobZones.P2"), fontLarge));
            c.setBorder( Rectangle.NO_BORDER );
            c.setPaddingTop( 6 );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            t.addCell(c);
            
            
            com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );

            //Paragraph cHdr=null;
            //Paragraph cFtr=null;
            //float spcg = 8;
            cl.setListSymbol( "\u2022");
            cl.add( new ListItem( new Paragraph( bmsg("b.JobZones.P2.1") , fontLarge ) ) );
            cl.add( new ListItem( new Paragraph( bmsg("b.JobZones.P2.2") , fontLarge ) ) );
            cl.add( new ListItem( new Paragraph( bmsg("b.JobZones.P2.3") , fontLarge ) ) );

            c = new PdfPCell();
            c.setBorder( Rectangle.NO_BORDER );
            c.setPaddingTop( 1 );
            c.setPaddingBottom( 6 );
            c.addElement( cl );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase(bmsg("b.JobZones.P3"), fontLarge));
            c.setBorder( Rectangle.NO_BORDER );
            c.setPaddingTop( 6 );
            c.setPaddingBottom( 6 );
            setRunDirection( c );
            t.addCell(c);
            

            c = new PdfPCell( new Phrase(bmsg("b.JobZones.P5"), fontLarge ));
            c.setBorder( Rectangle.NO_BORDER );
            c.setPaddingTop( 6 );
            c.setPaddingBottom( 4 );
            setRunDirection( c );
            t.addCell(c);
                        
            currentYLevel = addTableToDocument(y, t );
            
            currentYLevel -= TPAD;
            
            y=currentYLevel;
            
            int headPad=4;
            int bodyPad=3;
            
            for( OnetJobZoneType jz : OnetJobZoneType.values() )
            {
                t = new PdfPTable( new float[] { 1f, 5f } );
                t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
                t.setLockedWidth( true );
                t.setHorizontalAlignment( Element.ALIGN_CENTER );
                t.setKeepTogether( true );
                // t.setBreakPoints(4);
                setRunDirection( t );
                
                // header
                c = new PdfPCell( new Phrase(bmsg(jz.getNamexKey()) + ": " + bmsg(jz.getDescripKey()), this.fontLargeWhiteBold));                
                c.setBorder( Rectangle.NO_BORDER );
                c.setColspan(2);
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setBackgroundColor( ct2Colors.hraBlue  );
                c.setPadding( headPad );
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase(bmsg("b.Education") + ":", fontLarge));                
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding( bodyPad );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase(bmsg(jz.getEducationKey()), fontLarge));                
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding( bodyPad );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase(bmsg("b.Experience") + ":", fontLarge));                
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding( bodyPad );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase(bmsg(jz.getExperienceKey()), fontLarge));                
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding( bodyPad );
                setRunDirection( c );
                t.addCell(c);
                
                c = new PdfPCell( new Phrase(bmsg("b.Training") + ":", fontLarge));                
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding( bodyPad );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase(bmsg(jz.getTrainingKey()), fontLarge));                
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding( bodyPad );
                setRunDirection( c );
                t.addCell(c);
                
                currentYLevel = addTableToDocument(y, t );
            
                currentYLevel -= 2*TPAD;  
                y=currentYLevel;
                
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addJobZoneInfoSection()" );
            throw e;
        }        
    }
    
    
    
    @Override
    public void loadBestProfilesAndEeoCategoryScores() throws Exception
    {
        if( bestProfilesList!=null )
            return;
        
        // Next, calculate the top jobs for this person.
        if( bestJobsReportUtils==null )
            bestJobsReportUtils = new BestJobsReportUtils(careerScoutV2);

        loadBestProfilesAndEeoCategoryScoresFromTestEvent( 1 );

        if( bestProfilesList==null || bestProfilesList.isEmpty() )
        {
            eeoMatchList = new ArrayList<>();
            bestProfilesList = bestJobsReportUtils.getTopCT3ProfileMatches(MAX_PROFILES_TO_SHOW, reportData.getTestEvent(), jobSpecificTestEvent, jobZone, -1, 1, 1, true, eeoMatchList );
            Collections.sort( bestProfilesList, new ProfileFloat1Comparator() );
            Collections.reverse(bestProfilesList ); 

            // this will sort in proper order.
            Collections.sort( eeoMatchList );
            Collections.reverse(eeoMatchList );   

            int rank = 1;
            for( EeoMatch m : eeoMatchList )
            {
                m.setRank(rank);
                rank++;
            }
        }            

        if( 1==2 )
        {
            StringBuilder sb = new StringBuilder();
            sb.append( "bestProfilesList.size=" + bestProfilesList.size() );
            for( Profile p : bestProfilesList )
            {
               sb.append(  (p.getSoc()==null ? p.getName() : p.getSoc().toString()) + "\nMatch: " + p.getFloatParam1() + "\n" );
               //LogService.logIt( "BaseBestJobsReportTemplate.loadBestProfiles() " +  p.getSoc().toString() );
            }
            LogService.logIt( "BaseBestJobsReportTemplate2.loadBestProfilesAndEeoCategoryScores() AAA " + sb.toString() );
        }
                        
        StringBuilder sb = new StringBuilder();     
        sb.append( "Base level bestProfilesList.size=" + bestProfilesList.size() + "\n\n" );            
        
        for( Profile p : bestProfilesList )
        {
            sb.append((p.getSoc()==null ? p.getName() : p.getSoc().toString()) + "\nMatch: " + p.getFloatParam1() + "\n" );
        }
        
        if( !jobZone.equals(OnetJobZoneType.ZONE5) )
        {                
            oneLevelUpProfilesList = bestJobsReportUtils.getTopCT3ProfileMatches(MAX_PROFILES_TO_SHOW, reportData.getTestEvent(), jobSpecificTestEvent, oneLevelUpJobZone, 0, 2, 11, true, null );

            Collections.sort( oneLevelUpProfilesList, new ProfileFloat1Comparator() );
            Collections.reverse(oneLevelUpProfilesList );  

            sb.append( "\nOne Level Higher: oneLevelUpProfilesList.size=" + oneLevelUpProfilesList.size() + "\n\n" );            
            for( Profile p : oneLevelUpProfilesList )
            {
                sb.append((p.getSoc()==null ? p.getName() : p.getSoc().toString()) + "\nMatch: " + p.getFloatParam1() + "\n" );
            }
        }
        
        // LogService.logIt( "BaseBestJobsReportTemplate2.loadBestProfilesAndEeoCategoryScores() BBB " + sb.toString() );

    }
    
    
    public void addSummaryNotesSection1() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();

            pars.add( bmsg("b.SummaryNotesp2") );
            pars.add( bmsg("b.SummaryNotesp3") );
            
            Paragraph pp = new Paragraph();
            
            pp.add( new Phrase(  bmsg("b.SummaryNotesp1"), fontLargeBold ) );
            
            addOneColTable(pp, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );

            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate2.addSummaryNotesSection1()" );
            throw e;
        }        
    }

    public void addSummaryNotesSection2() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();
            
            Paragraph pp = new Paragraph();
            
            pp.add( new Phrase(  bmsg("b.DetailTitle"), fontLargeBold ) );

            pars.add( bmsg("b.DetailTitle.P1") );
            
            addOneColTable(pp, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate2.addSummaryNotesSection2()" );
            throw e;
        }        
    }
    
    public void addSummaryNotesSection3() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();
            
            Paragraph pp = new Paragraph();
            
            pp.add( new Phrase(  bmsg("b.DetailTitleOneJzUp"), fontLargeBold ) );

            pars.add( bmsg("b.DetailTitleOneJzUp.P1") );
            
            addOneColTable(pp, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate2.addSummaryNotesSection2()" );
            throw e;
        }        
    }


    public void addSummaryNotesSection4() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();

            pars.add( bmsg("b.SummaryNotesEeop2") );
            pars.add( bmsg("b.SummaryNotesEeop3") );
            
            Paragraph pp = new Paragraph();
            
            pp.add( new Phrase(  bmsg("b.SummaryNotesEeop1"), fontLargeBold ) );
            
            addOneColTable(pp, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate2.addSummaryNotesSection1()" );
            throw e;
        }        
    }
    
    
    
    
    public void addOneLevelUpMatchesSummarySection() throws Exception
    {
        if( oneLevelUpProfilesList==null || oneLevelUpProfilesList.isEmpty() )
            return;
        
        try
        {
            this.addNewPage();
            
            // Font fnt = getFontXLarge();
            previousYLevel =  currentYLevel - 10;
            
            float y = addTitle( previousYLevel, bmsg( "b.TopMatchesOneLevelUp.Pre" ), null );

            y -= TPAD;
            
            currentYLevel = y;
            previousYLevel = y;
                        
            List<Object> pars = new ArrayList<>();
            pars.add( bmsg("b.TopMatchesOneLevelUp.Pre.P1") );                        
            addOneColTable( null, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );
            
                        
            previousYLevel =  currentYLevel - 10;
            
            y = previousYLevel;

            y = addTitle( y, bmsg( "b.TopMatchesOneLevelUp" ), null );

            y -= TPAD;
            
            addMatchTable(oneLevelUpProfilesList, y, true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addOneLevelUpMatchesSummarySection()" );
            throw e;
        }        
    }
   
    
    public void computeBaseJobZone()
    {
        TestEventScore educTes = null;
        TestEventScore experTes = null;

        TestEventScore trainingTes = null;
        
        for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
        {
            if( !tes.getSimCompetencyClass().getIsBiodata() && !tes.getSimCompetencyClass().getIsExperience() )
                continue;
            
            if( StringUtils.isValidNameMatch("Education", "Education", tes.getName(), tes.getNameEnglish() ) )
            {
                educTes = tes;
            }
            
            else if( StringUtils.isValidNameMatch("Experience And Training", "Experience And Training", tes.getName(), tes.getNameEnglish() ) )
            {
                experTes = tes;
            }
            
            // these are only used by legacy career scout assessments
            
            else if( StringUtils.isValidNameMatch("Experience", "Experience", tes.getName(), tes.getNameEnglish() ) )
            {
                experTes = tes;
            }
            
            else if( StringUtils.isValidNameMatch("Training", "Training", tes.getName(), tes.getNameEnglish() ) )
            {
                trainingTes = tes;
            }
        }
        
        // LogService.logIt( "BaseBestJobsReportTemplate2.computeJobZone() educTes.total=" + (educTes==null ? "null" : educTes.getTotalUsed()) + ", experTes.total=" + (experTes==null ? "null" : experTes.getTotalUsed()) );

        if( educTes==null || experTes==null )
        {
            LogService.logIt( "BaseBestJobsReportTemplate2.computeJobZone() missing either educTes or experTes so returning 1 and 2 for one level up. " );
            jobZone = OnetJobZoneType.ZONE1;
            oneLevelUpJobZone = OnetJobZoneType.ZONE2;            
            return;
        }
        
        float educScore = educTes.getTotalUsed();
        float experScore = experTes.getTotalUsed();
        
        // for legacy only
        float trainingScore = trainingTes==null ? 0 : trainingTes.getTotalUsed();       
        experScore += trainingScore;
        
        if( educScore<=2 )
        {
            if( experScore<=(6f/13f))
                jobZone = OnetJobZoneType.ZONE1;
            
            else if( experScore<=(9f/13f))
                jobZone = OnetJobZoneType.ZONE2;
            
            else
                jobZone = OnetJobZoneType.ZONE3;
        }
        else if( educScore<=3 )
        {
            if( experScore<=(5f/13f))
                jobZone = OnetJobZoneType.ZONE2;            
            else
                jobZone = OnetJobZoneType.ZONE3;
        }
        else if( educScore<=4 )
        {
            if( experScore<=(6f/13f))
                jobZone = OnetJobZoneType.ZONE3;            
            else
                jobZone = OnetJobZoneType.ZONE4;
        }
        else //  if( educScore<=5 )
        {
            if( experScore<=(6f/13f))
                jobZone = OnetJobZoneType.ZONE4;            
            else
                jobZone = OnetJobZoneType.ZONE5;
        }
        
        oneLevelUpJobZone = jobZone.equals(OnetJobZoneType.ZONE5) ? jobZone : OnetJobZoneType.getValueForZoneId(jobZone.getJobZoneId()+1);

        LogService.logIt( "BaseBestJobsReportTemplate2.computeJobZone() Selected JobZone=" + jobZone.getName() + " (" + jobZone.getJobZoneId() + "), educScore=" + educScore + ", experScore=" + experScore + ", oneLevelUpJobZone=" + oneLevelUpJobZone.getJobZoneId() );
    }
    
    
}
