/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.disc;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
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
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


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
public abstract class BaseDiscReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    public boolean manager = true;
    public String bundleToUse = null;
    public DiscReportUtils discReportUtils;



    /*
      index 0=D
            1=I
            2=S
            3=C
    */
    public float[] discScoreVals;

    /*
      data[0] = top trait index.
      data[1] = secondary trait index or -1 if there is none.
    */
    public int[] topTraitIndexes;

    public String stub;
    public String highNamePair;



    @Override
    public void init( ReportData rd ) throws Exception
    {
        super.init(rd);
        specialInit();
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

            boolean hideOverallNumeric = reportData.getReportRuleAsBoolean( "ovrnumoff" );
            boolean hideOverallGraph = reportData.getReportRuleAsBoolean( "ovrgrphoff" );
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );

            float y = addTitle( previousYLevel, lmsg( "g.Overall" ), null );

            y -= TPAD;

            int scrDigits = reportData.getReport().getIntParam2() >= 0 ? reportData.getReport().getIntParam2() : reportData.getTestEvent().getScorePrecisionDigits();

            float[] colRelWids = new float[] { 1f };

            boolean includeNumScores = !hideOverallNumeric; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean includeColorGraph = !hideOverallGraph; //  && sft.getSupportsBarGraphic(reportData.getReport()) && reportData.getReport().getIncludeColorScores()==1; // && reportData.getReport().getIncludeCompetencyColorScores()==1;
            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();
            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            String scrTxt = lmsg_spec( manager ? "disc.XIsHighInY" : "disc.YouAreHighInY", new String[]{ reportData.getUserName(),highNamePair}); //getTestEvent().getOverallTestEventScore().getScoreText();

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( 1 );

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
            c = new PdfPCell( new Phrase( lmsg( "g.Candidate"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            // header row is finished.

            // t.setWidthPercentage( 0.8f );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( BaseColor.WHITE );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );

            // NAME
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            c.setPadding( 1 );
            c.setPaddingTop(4);
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( BaseColor.WHITE );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            touter.addCell( c );

            // Next Row - Email
            if( reportData.getUser().getUserType().getNamed() &&
                reportData.getUser().getEmail() != null &&
                !reportData.getUser().getEmail().isEmpty() &&
                !StringUtils.isCurlyBracketed( reportData.getUser().getEmail() ) )
            {
                c = new PdfPCell( new Phrase( reportData.getUser().getEmail(), getFontLight() ) );
                c.setPadding( 1 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );
            }

            // Next row - test name
            c = new PdfPCell( new Phrase( reportData.getSimName(), getFontLight() ) );
            c.setPadding( 1 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );

            // Note - the date MAY be the last entry in this table if there is no score text.
            boolean lastEntry = !includeColorGraph &&
                                !hasThirdPartyId &&
                                (reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty()) &&
                                (scrTxt == null || scrTxt.isEmpty());

            // Next Row, test date
            if( includeDates )
            {
                c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFontLight() ) );
                c.setPadding( 1 );
                // c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
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

            if( hasThirdPartyId )
            {
                lastEntry = !includeColorGraph &&
                            (reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty()) &&
                            (scrTxt == null || scrTxt.isEmpty());

                c = new PdfPCell( new Phrase( thirdPartyId, getFontLight() ) );
                c.setPadding( 1 );

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
                lastEntry = !includeColorGraph &&
                            (scrTxt == null || scrTxt.isEmpty());

                c = new PdfPCell( new Phrase( "(m) " + reportData.getUser().getMobilePhone(), getFontLight() ) );
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
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

            if( scrTxt != null && !scrTxt.isEmpty())
            {
                lastEntry = !includeColorGraph;

                c = new PdfPCell( new Phrase( scrTxt, fontLargeBold ) );
                c.setPadding( 5 );
                //c.setPaddingBottom( 6 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                // c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setPaddingLeft( 100 );

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

            if( includeColorGraph )
            {
                //PdfPTable t2 = new PdfPTable( 1 );
                //setRunDirection( t2 );
                //t2.setWidthPercentage(50 );
                //t2.setHorizontalAlignment(Element.ALIGN_CENTER);

                //float cellWid = (pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN);
                //LogService.logIt( "BaseDiscReportTemplate.addReportInfoHeader() cellWid=" + cellWid );

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setBackgroundColor(BaseColor.WHITE);
                c.setPadding( 0 );
                c.setFixedHeight(160 );
                c.setCellEvent( new DiscPieGraphCellEvent( getScoreValMap(), scrDigits, ct2Colors, reportData.getLocale() ) );
                touter.addCell(c);
                //t2.addCell(c);

                //c = new PdfPCell( t2 ); // new PdfPCell( summaryCatNumericAxis );
                //c.setBorder( Rectangle.RIGHT | Rectangle.LEFT   );
               //c.setBorderColor( BaseColor.WHITE );
                //c.setBackgroundColor(BaseColor.WHITE);
                //c.setBorderWidth( scoreBoxBorderWidth );
                //c.setHorizontalAlignment( Element.ALIGN_CENTER );
                //c.setVerticalAlignment( Element.ALIGN_TOP );
                //c.setPaddingTop( 2 );
                ////touter.addCell( c );
            }

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;




        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addReportInfoHeader()" );
            throw e;
        }
    }


    public void addDiscStylesExplained() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg_spec( "disc.StylesExplained" ), null );

            y -= TPAD;


            PdfPCell c;
            PdfPTable touter = new PdfPTable( 2 );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f, 4f } );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            String styleLetter;
            for( int i=0; i<4; i++ )
            {
                styleLetter = DiscReportUtils.getCompetencyStubLetter(i);
                c = new PdfPCell( new Phrase( lmsg_spec(  styleLetter + ".namefull"), fontBold ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( lmsg_spec(  styleLetter + ".characteristics"), font ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                setRunDirection( c );
                touter.addCell(c);
            }

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscFilesExplained()" );
            throw e;
        }
    }


    public void addTopTraitSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String titleKey = topTraitIndexes[1]>=0 ? "disc.CharsOfPeopleXY" : "disc.CharsOfPeopleX";

            String sectionTitle = topTraitIndexes[1]>=0 ? lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name"), lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name")}) :
                                                   lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name")});

            float y = addTitle( previousYLevel, lmsg_spec("disc.Traits"), null );

            y -= TPAD;

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // Create header
            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            List<String> itemList = new ArrayList<>();
            itemList.add(lmsg_spec( stub + ".characteristics") );
            itemList.add(lmsg_spec( stub + ".overview") );

            Font listHeaderFont = fontBold;
            Font listItemFont = font;
            String listTitle = lmsg_spec( "disc.Overview");

            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );


            listTitle = lmsg_spec( "disc.Strengths");
            itemList = lmsg_spec_list( stub + ".strengths");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );
            itemList.clear();

            listTitle = lmsg_spec( "disc.WorkplaceStyle");
            itemList = lmsg_spec_list( stub + ".style");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );
            itemList.clear();

            listTitle = lmsg_spec( "disc.Limitations");
            itemList = lmsg_spec_list( stub + ".limitations");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );
            itemList.clear();

            listTitle = lmsg_spec( "disc.Motivations");
            itemList = lmsg_spec_list( stub + ".motivations");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );
            itemList.clear();

            listTitle = lmsg_spec( "disc.Stressors");
            itemList = this.lmsg_spec_list( stub + ".stressors");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );
            itemList.clear();

            listTitle = lmsg_spec( "disc.HandlingConflict");
            itemList = this.lmsg_spec_list( stub + ".handlingconflict");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );
            itemList.clear();

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addTopTraitSection()" );
            throw e;
        }
    }


    public void addLeadingTraitSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String titleKey = topTraitIndexes[1]>=0 ? "disc.LeadingPeopleXY" : "disc.LeadingPeopleX";

            String sectionTitle = topTraitIndexes[1]>=0 ? lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name"), lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name")}) :
                                                   lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name")});

            float y = addTitle( previousYLevel, lmsg_spec("disc.Leading"), null );

            y -= TPAD;

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // Create header
            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );


            Font listHeaderFont = fontBold;
            Font listItemFont = font;
            String listTitle = lmsg_spec( "disc.General");
            List<String> itemList = lmsg_spec_list( stub + ".leading.general");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            listTitle = lmsg_spec( "disc.ResolvingConflict");
            itemList = lmsg_spec_list( stub + ".leading.resolveconflict");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            listTitle = lmsg_spec( "disc.RecognizingStress");
            itemList = lmsg_spec_list( stub + ".leading.recognizestress");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            listTitle = lmsg_spec( "disc.Motivating");
            itemList = lmsg_spec_list( stub + ".leading.motivate");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addLeadingTraitSection()" );
            throw e;
        }
    }


    public void addHowWorkWithTraitSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String titleKey = topTraitIndexes[1]>=0 ? "disc.HowWorkWithXY" : "disc.HowWorkWithX";

            String sectionTitle = topTraitIndexes[1]>=0 ? lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name"), lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[1])+".name")}) :
                                                   lmsg_spec( titleKey, new String[]{lmsg_spec( DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0])+".name")});

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            
            // t.setHeaderRows( 1 );

            // Create header
            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );


            Font listHeaderFont = fontBold;
            Font listItemFont = font;
            String listTitle = lmsg_spec( "disc.OnATeam");
            List<String> itemList = lmsg_spec_list( stub + ".howwork.onteam");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            listTitle = lmsg_spec( "disc.WhenWorkingWithXStyles", new String[]{highNamePair});
            itemList = lmsg_spec_list( stub + ".howwork.with");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            float ulY = currentYLevel - 6*PAD;  // 4* PAD;
            float tableHeight = touter.calculateHeights(); //  + 500;            
            if( tableHeight > (ulY - footerHgt - 3*PAD) )
            {
                this.addNewPage();
                previousYLevel = currentYLevel;
            }

            float y = addTitle( previousYLevel, lmsg_spec("disc.Collaborating"), null );
            y -= TPAD;
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addHowWorkWithTraitSection()" );
            throw e;
        }
    }

    public void addHowXShouldWorkWithYSection( int yTraitIndex ) throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String yTraitLetter = DiscReportUtils.getCompetencyStubLetter(yTraitIndex );
            String yTraitName = lmsg_spec(yTraitLetter+".name");

            String xTraitLetter = DiscReportUtils.getCompetencyStubLetter( topTraitIndexes[0] );

            String titleKey = "disc.HowWorkWithX";

            String sectionTitle = lmsg_spec( titleKey, new String[]{yTraitName});

            float y = addTitle( previousYLevel, lmsg_spec("disc.CollaboratingWithHighY", new String[]{yTraitName} ), null );

            y -= TPAD;

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // Create header
            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );


            Font listHeaderFont = fontBold;
            Font listItemFont = font;
            String listTitle = lmsg_spec( "disc.OnATeam");
            List<String> itemList = lmsg_spec_list(yTraitLetter + ".howwork.onteam");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            listTitle = lmsg_spec( "disc.WhenWorkingWithXStyles", new String[]{yTraitName});
            itemList = lmsg_spec_list(yTraitLetter + ".howwork.with");
            addListItemGroupToTable( touter, listTitle, itemList, listHeaderFont, listItemFont );

            if( xTraitLetter.equalsIgnoreCase(yTraitLetter ) )
                listTitle = lmsg_spec( "disc.HowXWorksWithX", new String[]{xTraitLetter.toUpperCase()} );
            else
                listTitle = lmsg_spec("disc.HowXWorksWithY", new String[]{xTraitLetter.toUpperCase(), yTraitLetter.toUpperCase()} );


            c = new PdfPCell( new Phrase( listTitle, listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            String howWorkTogetherStr = lmsg_spec(xTraitLetter + ".howworkwith." + yTraitLetter + ".1");
            c = new PdfPCell( new Phrase( howWorkTogetherStr, listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();

            currentYLevel -= TPAD;

            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addHowXShouldWorkWithYSection()" );
            throw e;
        }
    }



    public void addDiscEducationSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            String titleKey = "disc.WhatIsDISC";

            String sectionTitle = lmsg_spec( titleKey);

            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // Create header
            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font listHeaderFont = fontBold;
            Font listItemFont = font;

            c = new PdfPCell( new Phrase( lmsg_spec("disc.what.p1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);


            List<String> itemList = new ArrayList<>();
            itemList.add( lmsg_spec("d.name" ) + " (D)");
            itemList.add( lmsg_spec("i.name" ) + " (I)");
            itemList.add( lmsg_spec("s.name" ) + " (S)");
            itemList.add( lmsg_spec("c.name" ) + " (C)");
            addListItemGroupToTable( touter, null, itemList, listHeaderFont, listItemFont );

            c = new PdfPCell( new Phrase( lmsg_spec("disc.what.p2"), font ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingBottom(0);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.what.p3"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingBottom(0);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.History.title"), listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.History.1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.History.2"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);


            c = new PdfPCell( new Phrase( lmsg_spec("disc.HowUsed.title"), listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.HowUsed.1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            itemList = new ArrayList<>();
            itemList.add( lmsg_spec("disc.HowUsed.1.a" ));
            itemList.add( lmsg_spec("disc.HowUsed.1.b" ));
            itemList.add( lmsg_spec("disc.HowUsed.1.c" ));
            itemList.add( lmsg_spec("disc.HowUsed.1.d" ));
            addListItemGroupToTable( touter, null, itemList, listHeaderFont, listItemFont );


            c = new PdfPCell( new Phrase( lmsg_spec("disc.ScoringInfo.title"), listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingTop(6);
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("disc.ScoringInfo.1"), listItemFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            setRunDirection( c );
            touter.addCell(c);

            float ulY = currentYLevel - 6*PAD;  // 4* PAD;
            float tableHeight = touter.calculateHeights(); //  + 500;            
            if( tableHeight > (ulY - footerHgt - 3*PAD) )
            {
                this.addNewPage();
                previousYLevel = currentYLevel;
            }

            float y = addTitle( previousYLevel, lmsg_spec("disc.LearnMore"), null );
            y -= TPAD;
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscEducationSection()" );
            throw e;
        }
    }

    public void addDiscManagerInfoSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;


            float y = addTitle( previousYLevel, lmsg_spec("disc.InfoForManagers"), null );

            y -= TPAD;

            Font subheaderFont = fontBold;
            Font contentFont = font;
            Font listHeaderFont = subheaderFont;
            Font listItemFont = contentFont;

            int padding=2;


            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );

            // KEY ACTIONS
            String titleKey = "mgr.actions.title";
            String sectionTitle = lmsg_spec( titleKey);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( padding );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);


            Paragraph par = new Paragraph();
            Chunk chk = new Chunk( lmsg_spec("mgr.actions.1.title") + ":\n", subheaderFont );
            par.add(chk );
            chk = new Chunk( lmsg_spec("mgr.actions.1.1"), contentFont );
            par.add( chk );
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            par = new Paragraph();
            chk = new Chunk( lmsg_spec("mgr.actions.2.title") + ":\n", subheaderFont );
            par.add(chk );
            List<String> itemList = new ArrayList<>();
            for( int i=1;i<=3; i++ )
            {
                itemList.add( lmsg_spec("mgr.actions.2." + i ) );
            }
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);            
            addListItemGroupToTable( touter, null, itemList, listHeaderFont, listItemFont );
            

            for( int i=3;i<=5;i++ )
            {
                par = new Paragraph();
                chk = new Chunk( lmsg_spec("mgr.actions." + i + ".title") + ":\n", subheaderFont );
                par.add(chk );
                chk = new Chunk( lmsg_spec("mgr.actions." + i + ".1"), contentFont );
                par.add( chk );
                c = new PdfPCell( par );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( padding );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                touter.addCell(c);
            }

            // BUFFER
            c = new PdfPCell( new Phrase( " ", contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( padding );
            touter.addCell(c);
            
            // HOW TO BUILD A TEAM
            titleKey = "mgr.howbldtm.title";
            sectionTitle = lmsg_spec( titleKey);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( padding );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.howbldtm.p1"), contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.howbldtm.p2"), contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);
            
            // BUFFER
            c = new PdfPCell( new Phrase( " ", contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( padding );
            touter.addCell(c);

            // AVOID STEREOTYPING
            titleKey = "mgr.avoids.title";
            sectionTitle = lmsg_spec( titleKey);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( padding );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.avoids.p1"), contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            chk = new Chunk( lmsg_spec("mgr.avoids.1.title")+"\n", contentFont );
            par = new Paragraph();
            par.add( chk );            
            String url = lmsg_spec("mgr.avoids.1.url");
            if( url!=null && !url.isEmpty() )
            {
                chk = new Chunk( url, fontSmallItalicBlue );
                PdfAction pdfa = PdfAction.gotoRemotePage( url , lmsg_spec("disc.ClickToVisit"), false, true );
                chk.setAction( pdfa );
                par.add( chk );
            }                        
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);
            
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscEducationSection()" );
            throw e;
        }
    }

    public void addDiscBuildYourTeamSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg_spec("disc.BuildYourTeam"), null );

            y -= TPAD;

            Font subheaderFont = fontBold;
            Font contentFont = font;
            Font listHeaderFont = subheaderFont;
            Font listItemFont = contentFont;

            int padding=2;


            PdfPCell c;
            PdfPTable touter = new PdfPTable( 1 );
            List<String> itemList;
            
            setRunDirection( touter );
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );
            
            // TEAM-BUILDING ACTIVITIES
            String titleKey = "mgr.tmbld.title";
            String sectionTitle = lmsg_spec( titleKey);

            c = new PdfPCell( new Phrase( sectionTitle, fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( padding );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true) );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg_spec("mgr.tmbld.p1"), contentFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorderWidth( 0 );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);

            Paragraph par = new Paragraph();
            Chunk chk = new Chunk( lmsg_spec("mgr.tmbld.1.title") + "\n", subheaderFont );
            par.add(chk );
            chk = new Chunk( lmsg_spec("mgr.tmbld.1.1"), contentFont );
            par.add( chk );
            c = new PdfPCell( par );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( padding );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            setRunDirection( c );
            touter.addCell(c);
            
            for( int j=2;j<=3;j++ )
            {
                par = new Paragraph();
                chk = new Chunk( lmsg_spec("mgr.tmbld." + j + ".title") + ":\n", subheaderFont );
                par.add(chk );
                chk = new Chunk( lmsg_spec("mgr.tmbld." + j + ".1"), contentFont );            
                par.add( chk );
                c = new PdfPCell( par );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( padding );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                touter.addCell(c);

                par = new Paragraph();
                chk = new Chunk( lmsg_spec("mgr.tmbld." + j + ".2")+"\n", contentFont );            
                par.add( chk );
                itemList = new ArrayList<>();
                for( int i=1;i<=(j==2? 5 : 8); i++ )
                {
                    itemList.add( lmsg_spec("mgr.tmbld." + j + ".2." + i ) );
                }
                c = new PdfPCell( par );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( padding );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                touter.addCell(c);
                addListItemGroupToTable( touter, null, itemList, listHeaderFont, listItemFont );
            }
            

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( new float[]{1f} );
            touter.setLockedWidth( true );
            
                        
            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - touter.calculateHeights();
            previousYLevel = currentYLevel;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDiscReportTemplate.addDiscEducationSection()" );
            throw e;
        }
    }
    
    

    private synchronized void specialInit()
    {
        if( discReportUtils==null )
        {
            if( reportData.getReport().getStrParam6() !=null && !reportData.getReport().getStrParam6().isEmpty() )
                bundleToUse = reportData.getReport().getStrParam6();

            if( bundleToUse==null || bundleToUse.isEmpty() )
            {
                Locale loc = reportData.getLocale();
                // String stub = "";
                if( loc.getLanguage().equalsIgnoreCase( "en" ) )
                    bundleToUse = "discreport.properties";
                else
                    bundleToUse = "discreport_" + loc.getLanguage().toLowerCase() + ".properties";
            }

            discReportUtils = new DiscReportUtils( bundleToUse );
        }

        discScoreVals = DiscReportUtils.getDiscScoreVals( reportData.getTestEvent() );
        topTraitIndexes = DiscReportUtils.getTopTraitIndexes(discScoreVals);
        stub = DiscReportUtils.getCompetencyStub(topTraitIndexes);
        if( topTraitIndexes[1]>=0 )
            highNamePair = lmsg_spec( "disc.TopTraitNamePairXY", new String[] {lmsg_spec(DiscReportUtils.getCompetencyStubLetter(topTraitIndexes[0]) + ".name"), lmsg_spec( DiscReportUtils.getCompetencyStubLetter(topTraitIndexes[1]) + ".name")} );
        else
            highNamePair = lmsg_spec( DiscReportUtils.getCompetencyStubLetter(topTraitIndexes[0]) + ".name" );

        LogService.logIt( "BaseDiscReportUtils.specialInit() topTraitIndexes[0]=" + topTraitIndexes[0] + ", topTraitIndexes[1]=" + topTraitIndexes[1] + ", stub=" + stub + ", highNamePair=" + highNamePair );
    }

    public String lmsg_spec( String key )
    {
        return discReportUtils.getKey(key );
    }

    public String lmsg_spec( String key, String[] prms )
    {
        String msgText = discReportUtils.getKey(key );
        return MessageFactory.substituteParams( reportData.getLocale() , msgText, prms );
    }

    public List<String> lmsg_spec_list( String key )
    {
        List<String> out = new ArrayList<>();
        String val;
        for( int i=1;i<100;i++ )
        {
            val =  discReportUtils.getKey( key + "." + i );

            if( val==null || val.isBlank() || val.startsWith( "KEY NOT FOUND") )
                break;

            out.add(val);
        }

        return out;
    }


    public void addListItemGroupToTable( PdfPTable touter, String listTitle,List<String> itemList, Font listHeaderFont, Font listItemFont ) throws Exception
    {
        com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
        cl.setListSymbol( "\u2022");
        cl.setIndentationLeft( 10 );
        cl.setSymbolIndent( 10 );

        for( String s : itemList )
        {
            cl.add( new ListItem( 9,  s, listItemFont ) );
        }

        PdfPCell c;

        if( listTitle!=null && !listTitle.isBlank() )
        {
            c = new PdfPCell( new Phrase( listTitle, listHeaderFont ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setPaddingBottom(0);
            setRunDirection( c );
            touter.addCell(c);
        }

        c = new PdfPCell();
        c.addElement( cl );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        c.setVerticalAlignment( Element.ALIGN_TOP );
        c.setBorderWidth( 0 );
        c.setPaddingLeft( 2 );
        c.setPaddingBottom( 2 );
        setRunDirection( c );
        touter.addCell(c);
    }



    public Map<String,Object[]> getScoreValMap()
    {
        Map<String,Object[]> out = new HashMap<>();
        String key;
        for( int i=0;i<discScoreVals.length; i++ )
        {
            key = DiscReportUtils.getCompetencyStubLetter(i) + ".name";
            out.put( DiscReportUtils.getCompetencyStubLetter(i), new Object[]{ lmsg_spec(key), Float.valueOf(discScoreVals[i])} );
        }
        return out;
    }


}
