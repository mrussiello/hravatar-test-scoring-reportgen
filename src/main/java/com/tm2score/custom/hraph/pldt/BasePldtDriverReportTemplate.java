/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.pldt;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.custom.coretest2.*;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_BOX_EXTRAMARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.TableBackground;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 *
 * @author Mike
 */
public abstract class BasePldtDriverReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{
    static String privacyNote = "DATA PRIVACY NOTICE. These assessment results may constitute Personal Data as defined under the Data Privacy Act of 2012 and its related rules and regulations. Data privacy obligations therein and as detailed in the organization’s data privacy policy must accordingly be carried out in relation to these results. Particularly, these results must be treated confidential and should not be disclosed to another individual or outside the organization, without the consent of the test taker. Results shall only be released to those who have legitimate basis for doing so. For study and research purposes, names (or other identifying information) of examinees (whose test results shall be used as samples) must be omitted or coded.";
    static float[] table2ColWidths = new float[] { 0.2f, 0.2f, .34f, .08f, .18f };
    static float[] table3ColWidths = new float[] { 0.2f, 0.2f, .15f, .19f, .08f, .18f };
    static String[] test1Competencies = new String[]{"Analytical Thinking & Attention to Detail","Adaptable", "Corporate Citizenship","Exhibits a Positive Work Attitude", "Emotional Self-Awareness", "Emotional Self-Control", "Empathy"};
    static int[] test1CompetencyClassIds = new int[]{SimCompetencyClass.ABILITY_COMBO.getSimCompetencyClassId(),SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId(),SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId(),SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId(),SimCompetencyClass.EQ.getSimCompetencyClassId(),SimCompetencyClass.EQ.getSimCompetencyClassId(),SimCompetencyClass.EQ.getSimCompetencyClassId()};
   
    static String[] test1CompetencyDescrips = new String[]{"This indicates the capacity to think in a thoughtful, discerning way, to solve problems, utilize resources, and analyze data. As well as, measure level of thoroughness, accuracy, and being concerned for all areas involved no matter how insignificant.",
        "Reflects how accepting a person is of frequent or substantial changes in his or her work requirements that may cause stress or put pressure on an individual. High scorers usually thrive under changing work conditions, while low scorers may burn out or become paralyzed.", 
        "This indicates the degree to which an individual's behavior embraces the spirit of an organization's mission, objectives, and strategy. High scorers project an attitude characterized by cooperation, trust, and openness. Low scorers often question the motives behind decisions. They may withhold information, display hostility, be defensive, or do just enough to get by.",
        "This indicates the degree to which an individual considers work as a key priority in life. High scorers tend to enjoy working and always applies best energy while taking pride in work reputation. Low scorers tend to view work as a means of income only and may not care about professional reputation.", 
        "The ability to pay attention to, monitor, and understand how and why one reacts a particular way in different situations, and to know how to conduct oneself appropriately and effectively in social situations.", 
        "The ability to manage the desire to satisfy urges or impulses, showing restraint and managing behaviors to ensure appropriate and effective interactions with others.", 
        "The ability to sense and understand other people's feelings, feel sympathy for others, and see things from other people's point of view."};
    
    static String[] test2Competencies = new String[]{"International Safety Signs and Symbols"};
    static String[] test3Competencies = new String[]{"Safety Driving Principles"};
    
    static final int STD_PADDING = 3;
    static final int THIN_PADDING = 2;
    
    static final float CUTOFF_1 = 50f;
    static final float CUTOFF_23 = 80f;
    
    @Override
    public abstract byte[] generateReport() throws Exception;

    static BaseColor pldtDarkGray = BaseColor.DARK_GRAY;
    static BaseColor pldtLiteGray = BaseColor.LIGHT_GRAY;
    static BaseColor pldtTableBorderColor = BaseColor.LIGHT_GRAY;
    
    static Font pldtStdTableFont;
    static Font pldtStdTableFontWhite;
    static Font pldtStdTableFontBold;
    static Font pldtStdTableFontLargeBoldWhite;
    
    static Font pldtGreenFont;
    static Font pldtYellowGreenFont;
    static Font pldtYellowFont;
    static Font pldtRedYellowFont;
    static Font pldtRedFont;
    
    static Image test1GraphicImg;
    static Image test23GraphicImg;
    
    
    static float pldtBorderWid = 0.5f;
    
    float[] scores;
    String[] scoreNames;
    BaseColor[] scoreColors;
    Font[] scoreFonts;
    

    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CT2ReportData();

        initLocales();
        
        initFonts();
        
        initColors();        

        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        // pdfWriter = new PdfCopy(document, baos);
        // pdfWriter.addJavaScript( "" );
        
        // LogService.logIt( "BaseCT2ReportTemplate.init() title=" + rd.getReportName() );
        String logoUrl = "https://cfmedia-hravatar-com.s3.amazonaws.com/web/orgimage/zrWvh1uNWrg-/img_2x1655814930006.png";
        String graphic1Url = "https://cfmedia-hravatar-com.s3.amazonaws.com/web/orgimage/zrWvh1uNWrg-/img_43x1655900509481.png";
        String graphic23Url = "https://cfmedia-hravatar-com.s3.amazonaws.com/web/orgimage/zrWvh1uNWrg-/img_44x1655900509489.png";

        try
        {
            custLogo = getImageInstance(logoUrl, reportData.te.getTestEventId());
            
            if( custLogo !=null )
	            custLogo.scalePercent(64);
            
            test1GraphicImg = getImageInstance(graphic1Url, reportData.te.getTestEventId());
            
            if( test1GraphicImg!=null )
            	test1GraphicImg.scalePercent(50);
            	
            test23GraphicImg = getImageInstance(graphic23Url, reportData.te.getTestEventId());   
            
            if( test23GraphicImg!=null )         
                test23GraphicImg.scalePercent(50);
        }
        catch( Exception e )
        {
            custLogo = null;
            LogService.logIt( e, "BasePldtDriverReportTemplate.initFonts() CCC.1 NONFATAL error getting custLogo. Will use null. logo= " + logoUrl + ", Exception=" + e.toString() );
        }
        

        
        PldtHeaderFooter hdr = new PldtHeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this, custLogo );

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
    
    
    
    @Override
    public synchronized void initFonts() throws Exception
    {
        initSettings( reportData );

        if( pldtStdTableFontBold==null )
        {
            pldtStdTableFont = fontSmall;
            pldtStdTableFontBold = fontSmallBold;
            pldtStdTableFontWhite = fontSmallWhite;
            pldtStdTableFontLargeBoldWhite = new Font(baseFontCalibri, FONTSZ);
            pldtStdTableFontLargeBoldWhite.setColor(BaseColor.WHITE);        
        }
        
        pldtGreenFont =   new Font(baseFontCalibriBold, SFONTSZ);
        pldtGreenFont.setColor(ct2Colors.green);

        pldtYellowGreenFont =   new Font(baseFontCalibriBold, SFONTSZ);
        pldtYellowGreenFont.setColor(ct2Colors.yellowgreen);
        
        pldtYellowFont =   new Font(baseFontCalibriBold, SFONTSZ);
        pldtYellowFont.setColor(ct2Colors.yellow);

        pldtRedYellowFont =   new Font(baseFontCalibriBold, SFONTSZ);
        pldtRedYellowFont.setColor(ct2Colors.redyellow);

        pldtRedFont =   new Font(baseFontCalibriBold, SFONTSZ);
        pldtRedFont.setColor(ct2Colors.red);
        
        setScoreValues();
        
        
        //if( custLogo != null )
        //     custLogo.scalePercent( imgSclW );
    }
    
    
    private synchronized void setScoreValues()
    {
        try
        {
            float score =0;
            float scoreTot =0;
            float weightTot = 0;

            scores = new float[3];
            scoreNames = new String[3];
            scoreColors = new BaseColor[3];
            scoreFonts = new Font[3];
            

            scoreTot=0;
            TestEventScore tes = getTestEventScoreForCompetency( "Analytical Thinking & Attention to Detail", SimCompetencyClass.ABILITY_COMBO.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scoreTot += tes.getScore()*0.1f;
                weightTot +=0.1f;
            }
            
            tes = getTestEventScoreForCompetency( "Adaptable", SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scoreTot += tes.getScore()*0.1f;
                weightTot +=0.1f;
            }            
            tes = getTestEventScoreForCompetency( "Corporate Citizenship", SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scoreTot += tes.getScore()*0.1f;
                weightTot +=0.1f;
            }            
            tes = getTestEventScoreForCompetency( "Exhibits a Positive Work Attitude", SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scoreTot += tes.getScore()*0.1f;
                weightTot +=0.1f;
            }            
            tes = getTestEventScoreForCompetency( "Emotional Self-Awareness", SimCompetencyClass.EQ.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scoreTot += tes.getScore()*0.2f;
                weightTot +=0.2f;
            }            
            tes = getTestEventScoreForCompetency( "Emotional Self-Control", SimCompetencyClass.EQ.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scoreTot += tes.getScore()*0.2f;
                weightTot +=0.2f;
            }            
            tes = getTestEventScoreForCompetency( "Empathy", SimCompetencyClass.EQ.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scoreTot += tes.getScore()*0.2f;
                weightTot +=0.2f;
            }
            
            score = weightTot<=0 ? 0 : scoreTot/weightTot;
            scores[0]=score;
            scoreNames[0] = getTest1ScoreName(score);
            scoreColors[0] = getTest1ScoreColor(score);
            scoreFonts[0] = getRecommendedScoreFontTest1( score );

            tes = getTestEventScoreForCompetency( "International Safety Signs and Symbols", SimCompetencyClass.CORESKILL.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scores[1]=tes.getScore();
                scoreNames[1]=this.getTest23ScoreName(tes.getScore());
                scoreColors[1]=this.getTest23ScoreColor( tes.getScore());
                scoreFonts[1] = getRecommendedScoreFontTest23( tes.getScore() );
            }            
            
            tes = getTestEventScoreForCompetency( "Safety Driving Principles", SimCompetencyClass.CORESKILL.getSimCompetencyClassId() );            
            if( tes!=null )
            {
                scores[2]=tes.getScore();
                scoreNames[2]=this.getTest23ScoreName(tes.getScore());
                scoreColors[2]=this.getTest23ScoreColor( tes.getScore());
                scoreFonts[2] = getRecommendedScoreFontTest23( tes.getScore() );
            }                       
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BasePldtDriverReportTemplate.setScoreValues() testEventId=" + reportData.te.getTestEventId() );
        }
    }

    private boolean getOverallRecommended()
    {
        if( scores[0]<CUTOFF_1 )
            return false;
        if( scores[1]<CUTOFF_23 )
            return false;
        if( scores[2]<CUTOFF_23 )
            return false;
        return true;        
    }
    
    private String getRecommendedStrTest1( float score )
    {
        return score>=CUTOFF_1 ? "Recommended" : "Not Recommended";        
    }
    
    private String getRecommendedStrTest23( float score )
    {
        return score>=CUTOFF_23 ? "Recommended" : "Not Recommended";        
    }
    
    private Font getRecommendedScoreFontTest1( float score )
    {
        return score>=CUTOFF_1 ? pldtGreenFont : pldtRedFont;
    }
    
    private Font getRecommendedScoreFontTest23( float score )
    {
        return score>=CUTOFF_23 ? pldtGreenFont : pldtRedFont;
    }
    
    private boolean test1UseWhiteFont( float score )
    {
        return score>=80 || score<=50;
    }
    
    private String getTest1ScoreName( float score)
    {
        if( score<=35 )
            return "Low";
        else if( score<50 )
            return "Below Average";
        else if( score<65 )
            return "Average";
        else if( score<80 )
            return "Above Average";
        else
            return "Outstanding";
    }

    private Font getScoreFont( BaseColor scoreColor )
    {
        if( scoreColor.equals(ct2Colors.scoreBlack))
            return pldtRedFont;
        if( scoreColor.equals(ct2Colors.red))
            return pldtRedFont;
        if( scoreColor.equals(ct2Colors.redyellow))
            return pldtRedYellowFont;
        if( scoreColor.equals(ct2Colors.yellow))
            return pldtYellowFont;
        if( scoreColor.equals(ct2Colors.yellowgreen))
            return pldtYellowGreenFont;
        if( scoreColor.equals(ct2Colors.green))
            return pldtGreenFont;
        if( scoreColor.equals(ct2Colors.scoreWhite))
            return pldtGreenFont;
        return font;
    }
    
    private BaseColor getTest1ScoreColor( float score)
    {
        if( score<=35 )
            return this.ct2Colors.red;
        else if( score<50 )
            return this.ct2Colors.redyellow;
        else if( score<65 )
            return this.ct2Colors.yellow;
        else if( score<80 )
            return this.ct2Colors.yellowgreen;
        else
            return this.ct2Colors.green;
    }
    

    private BaseColor getTest23ScoreColor( float score)
    {
        if( score<=60 )
            return this.ct2Colors.red;
        else if( score<80 )
            return this.ct2Colors.redyellow;
        else if( score<85 )
            return this.ct2Colors.yellow;
        else if( score<90 )
            return this.ct2Colors.yellowgreen;
        else
            return this.ct2Colors.green;
    }
    
    private boolean test23UseWhiteFont( float score )
    {
        return score>90 || score<80;
    }
    

    
    private String getTest23ScoreName( float score)
    {
        if( score<60 )
            return "Low";
        else if( score<80 )
            return "Below Average";
        else if( score<=85 )
            return "Average";
        else if( score<=90 )
            return "Above Average";
        else
            return "Outstanding";
    }

    
    
    private TestEventScore getTestEventScoreForCompetency( String name, int simCompetencyClassId )
    {
        if( reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId())==null )
            return null;
        
        for( TestEventScore tes : reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()) )
        {
            if( tes.getSimCompetencyClassId()!=simCompetencyClassId)
                continue;
            
            if( tes.getName().equals( name ) || (tes.getNameEnglish()!=null && tes.getNameEnglish().equals(name)) )
                return tes;
        }
        return null;
    }

    
    
    protected void addOverallSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            
            PdfPTable touter = new PdfPTable( new float[] { 0.15f, .35f, .1f, .2f, .2f } );
            // setRunDirection( touter );
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( true );
            
            PdfPCell dc = touter.getDefaultCell();
            dc.setBorderWidth(1);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setVerticalAlignment( Element.ALIGN_MIDDLE );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding(THIN_PADDING);
            
            //////////////////////////////////////
            // Top Row
            //////////////////////////////////////            
            PdfPCell c = new PdfPCell( new Phrase( "Driving Dependency and Safety Assessment", this.fontLargeWhiteBold ) );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setPadding(STD_PADDING);
            c.setPaddingBottom(STD_PADDING + 2 );
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtDarkGray);
            //c.setBorderWidth(pldtBorderWid);
            //c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBackgroundColor( pldtDarkGray );
            c.setColspan(5);
            touter.addCell(c);
            
            //////////////////////////////////////
            // Name 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "Name:", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT  );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( reportData.u.getFullname(), pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorder( Rectangle.NO_BORDER  );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);

            c = new PdfPCell( new Phrase( "Assessment Date: " + I18nUtils.getFormattedDate(Locale.US, reportData.te.getLastAccessDate(), DateFormat.MEDIUM), pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setPaddingRight( 3*STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c.setColspan(3);
            touter.addCell(c);

            //////////////////////////////////////
            // EMAIL 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "Email:", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT  );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( reportData.u.getEmail(), pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            c.setColspan(4);
            touter.addCell(c);

            //////////////////////////////////////
            // Org 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "Organization:", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( reportData.o.getName(), pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            c.setColspan(4);
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( " " , fontXSmallItalic ) );
            c.setPadding(0);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setColspan(5);
            touter.addCell(c);            

            
            //////////////////////////////////////
            // Summary of Scores Row 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "SUMMARY OF SCORES", this.fontLargeBold ) );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setPadding(STD_PADDING);
            c.setPaddingBottom(STD_PADDING + 2 );
            c.setBorderWidth(pldtBorderWid);
            // c.setBorderColor(pldtTableBorderColor);
            c.setBorderColor(pldtLiteGray);
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBackgroundColor( pldtLiteGray );
            c.setColspan(5);
            touter.addCell(c);
            
            
            //////////////////////////////////////
            // scores headers 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "", this.fontXSmallItalic ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT );
            c.setColspan(2);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( "Score", this.fontXSmallItalic ) );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(1);
            c.setPaddingBottom(2);
            touter.addCell(c);            
            
            c = new PdfPCell( new Phrase( "Level", this.fontXSmallItalic ) );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding(1);
            c.setPaddingBottom(2);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( "Status", this.fontXSmallItalic ) );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.RIGHT );
            c.setPadding(1);
            c.setPaddingBottom(2);
            touter.addCell(c);            
            
            
            //////////////////////////////////////
            // Test 1 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "Test 1:", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( "Cognitive and Personality", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);            
            
            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scores[0], 1 ), pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOX );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( scoreNames[0], test1UseWhiteFont(scores[0]) ? pldtStdTableFontWhite : pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBackgroundColor( scoreColors[0] );
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( getRecommendedStrTest1( scores[0] ), scoreFonts[0] ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            //////////////////////////////////////
            // Test 2 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "Test 2:", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( "International Safety Signs and Symbols", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);            
            
            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scores[1], 1 ), pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( scoreNames[1], test23UseWhiteFont(scores[1]) ? pldtStdTableFontWhite : pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBackgroundColor( scoreColors[1] );
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( getRecommendedStrTest23( scores[1] ), scoreFonts[1] ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            
            

            //////////////////////////////////////
            // Test 3 
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "Test 3:", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( "Safety Driving Principles", pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            touter.addCell(c);            
            
            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scores[2], 1 ), pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( scoreNames[2], test23UseWhiteFont(scores[2]) ? pldtStdTableFontWhite : pldtStdTableFont ) );
            c.setPadding(THIN_PADDING);
            c.setBackgroundColor( scoreColors[2] );
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( getRecommendedStrTest23( scores[2] ), scoreFonts[2] ) );
            c.setPadding(THIN_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            touter.addCell(c);            

            c = new PdfPCell( new Phrase( " " , fontXSmallItalic ) );
            c.setPadding(0);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM );
            c.setColspan(5);
            touter.addCell(c);            

            //////////////////////////////////////
            // Overall Row 
            //////////////////////////////////////
            Chunk ch = new Chunk( "OVERALL RECOMMENDATION\n", pldtStdTableFontBold );
            Chunk ch2 = new Chunk( "(must be RECOMMENDED on all tests)", this.fontXSmallItalic );
            Paragraph par = new Paragraph();
            par.add(ch);
            par.add(ch2);            
            c = new PdfPCell( par );
            c.setColspan(3);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            touter.addCell(c);  
            
            boolean rec = getOverallRecommended();
            
            String recStr = rec ? "Recommended" : "Not Recommended";
            
            c = new PdfPCell( new Phrase( recStr, pldtStdTableFontLargeBoldWhite ) );
            c.setColspan(2);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.RIGHT | Rectangle.BOTTOM );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setBackgroundColor( rec ? ct2Colors.green : ct2Colors.red );
            touter.addCell(c);  
                        
            currentYLevel = addTableToDocument(previousYLevel, touter, true, false);            
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "BasePldtDriverReportTemplate.addOverallSection()" );
            throw new STException( e );
        }        
    }
    
    protected void addTest1Section() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
            
            previousYLevel -= 2*PAD;
            
            PdfPTable touter = new PdfPTable( table2ColWidths );
            // setRunDirection( touter );
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( true );
            
            PdfPCell dc = touter.getDefaultCell();
            dc.setBorderWidth(1);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setVerticalAlignment( Element.ALIGN_MIDDLE );
            dc.setBorder( Rectangle.NO_BORDER );            
            dc.setPadding(THIN_PADDING);
            
            //////////////////////////////////////
            // Top Row
            //////////////////////////////////////
            PdfPCell c = new PdfPCell( new Phrase( "Test 1: Cognitive and Personality", this.fontLargeBold ) );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setPadding(STD_PADDING);
            c.setColspan(2);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.TOP );
            touter.addCell(c);
            
            //////////////////////////////////////
            // Graphic Cells
            //////////////////////////////////////
            c = new PdfPCell( test1GraphicImg );
            c.setPadding(THIN_PADDING);
            c.setColspan(3);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT  );
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);

            /*
            PdfPTable graphT = new PdfPTable( 1 );
            graphT.setHorizontalAlignment( Element.ALIGN_CENTER );
            PdfPCell c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
            c2.setBorder( Rectangle.NO_BORDER );
            c2.setColspan( 2 );
            c2.setPadding(THIN_PADDING);
            c2.setPaddingTop(0);
            c2.setVerticalAlignment( Element.ALIGN_TOP );
            c2.setFixedHeight(22);
            c2.setCellEvent( new PldtScoreGraphicCellEvent( 1, scores[0], baseFontCalibri, ct2Colors ) );
            graphT.addCell(c2);
            c.addElement( graphT );
            */
            touter.addCell(c);

            
            //////////////////////////////////////
            // Overall Row
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "" ) );
            c.setPadding(STD_PADDING);
            c.setColspan(2);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( "Overall Weighted Score:",pldtStdTableFont ) );
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scores[0], 1 ), pldtStdTableFont ) );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOX );
            touter.addCell(c);

            Font fullColorFont = getScoreFont( scoreColors[0] );
            c = new PdfPCell( new Phrase( scoreNames[0], fullColorFont ) );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM | Rectangle.TOP | Rectangle.RIGHT );
            touter.addCell(c);

            TestEventScore tes;            
            String comp;            
            BaseColor scoreColor;
            Font scoreFont;
            String scoreName;
            
            //////////////////////////////////////
            // Competency Rows
            //////////////////////////////////////
            for( int i=0;i<test1Competencies.length;i++ )
            {
                comp = test1Competencies[i];
                        
                tes = getTestEventScoreForCompetency(comp, test1CompetencyClassIds[i]);
                if( tes==null )
                    continue;
                
                c = new PdfPCell( new Phrase( tes.getName(), pldtStdTableFont ) );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c.setPadding(STD_PADDING);
                c.setBorderWidth(pldtBorderWid);
                c.setBorderColor(pldtTableBorderColor);
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( test1CompetencyDescrips[i], pldtStdTableFont ) );
                c.setHorizontalAlignment(Element.ALIGN_LEFT);
                c.setPadding(STD_PADDING);
                c.setColspan(2);
                c.setBorderWidth(pldtBorderWid);
                c.setBorderColor(pldtTableBorderColor);
                c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
                touter.addCell(c);
                
                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 0 ), pldtStdTableFont ) );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c.setPadding(STD_PADDING);
                c.setBorderWidth(pldtBorderWid);
                c.setBorderColor(pldtTableBorderColor);
                c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
                touter.addCell(c);

                scoreColor = getTest1ScoreColor( tes.getScore() );
                scoreFont = this.getScoreFont(scoreColor);
                scoreName = this.getTest1ScoreName( tes.getScore() );
                c = new PdfPCell( new Phrase( scoreName, scoreFont ) );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c.setPadding(STD_PADDING);
                c.setBorderWidth(pldtBorderWid);
                c.setBorderColor(pldtTableBorderColor);
                c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
                touter.addCell(c);                
            }
                        
            currentYLevel = addTableToDocument(previousYLevel, touter, true, false);            
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "BasePldtDriverReportTemplate.addTest1Section()" );
            throw new STException( e );
        }        
    }
    
    protected void addTest2And3Section() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            previousYLevel -= 2*PAD;
            
            PdfPTable touter = new PdfPTable( table3ColWidths );
            // setRunDirection( touter );
            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( true );
            
            PdfPCell dc = touter.getDefaultCell();
            dc.setBorderWidth(1);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setVerticalAlignment( Element.ALIGN_MIDDLE );
            dc.setBorder( Rectangle.NO_BORDER );            
            dc.setPadding(THIN_PADDING);

            //////////////////////////////////////
            // Top Row
            //////////////////////////////////////
            PdfPCell c = new PdfPCell( new Phrase( "Test 2 & 3: Driving Knowledge and Principles", this.fontLargeBold ) );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setPadding(STD_PADDING);
            c.setColspan(3);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.TOP );
            touter.addCell(c);
            
            
            //////////////////////////////////////
            // Graphic cells
            //////////////////////////////////////
            c = new PdfPCell( test23GraphicImg );
            c.setPadding(THIN_PADDING);
            c.setColspan(3);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT  );
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);

            /*
            float avgScore = (scores[1] + scores[2])/2;
            PdfPTable graphT = new PdfPTable( 1 );
            graphT.setHorizontalAlignment( Element.ALIGN_CENTER );
            PdfPCell c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
            c2.setBorder( Rectangle.NO_BORDER );
            c2.setColspan( 2 );
            c2.setPadding(THIN_PADDING);
            c2.setPaddingTop(0);
            c2.setVerticalAlignment( Element.ALIGN_TOP );
            c2.setFixedHeight(22);
            c2.setCellEvent( new PldtScoreGraphicCellEvent( 2, avgScore, baseFontCalibri, ct2Colors ) );
            graphT.addCell(c2);
            c.addElement( graphT );
            */
            touter.addCell(c);

            // Buffer Row
            c = new PdfPCell( new Phrase( "" ) );
            c.setPadding(STD_PADDING);
            c.setColspan(6);
            c.setBorder( Rectangle.NO_BORDER );
            touter.addCell(c);

            //////////////////////////////////////
            // Test 2 Row
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "" ) );
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( "Test 2", pldtStdTableFont ) );
            c.setPadding(STD_PADDING);
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            c.setBorder( Rectangle.NO_BORDER );
            touter.addCell(c);
            
            
            c = new PdfPCell( new Phrase( test2Competencies[0],pldtStdTableFont ) );
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c.setColspan(2);
            c.setPadding(STD_PADDING);
            c.setBorder( Rectangle.NO_BORDER );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scores[1], 0 ), pldtStdTableFont ) );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            touter.addCell(c);

            BaseColor scoreColor;
            Font scoreFont;
            String scoreName;

            scoreColor = getTest23ScoreColor( scores[1] );
            scoreFont = this.getScoreFont(scoreColor);
            scoreName = this.getTest23ScoreName( scores[1] );
            
            c = new PdfPCell( new Phrase( scoreName, scoreFont ) );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT );
            touter.addCell(c);

            
            //////////////////////////////////////
            // Test 3 Row
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( "" ) );
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( "Test 3", pldtStdTableFont ) );
            c.setPadding(STD_PADDING);
            c.setHorizontalAlignment(Element.ALIGN_LEFT);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM );
            touter.addCell(c);
            
            
            c = new PdfPCell( new Phrase( test3Competencies[0],pldtStdTableFont ) );
            c.setColspan(2);
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOTTOM );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scores[2], 0 ), pldtStdTableFont ) );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.BOX );
            touter.addCell(c);

            scoreColor = getTest23ScoreColor( scores[2] );
            scoreFont = this.getScoreFont(scoreColor);
            scoreName = this.getTest23ScoreName( scores[2] );
            
            c = new PdfPCell( new Phrase( scoreName, scoreFont ) );
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_MIDDLE);
            c.setPadding(STD_PADDING);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
            touter.addCell(c);
            
            
            //////////////////////////////////////
            // Data Privacy Note
            //////////////////////////////////////
            c = new PdfPCell( new Phrase( privacyNote, pldtStdTableFont ) );
            c.setPadding(STD_PADDING);
            c.setColspan(6);
            c.setBorderWidth(pldtBorderWid);
            c.setBorderColor(pldtTableBorderColor);
            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
            touter.addCell(c);
                        
            currentYLevel = addTableToDocument(previousYLevel, touter, true, false);            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BasePldtDriverReportTemplate.addTest2And3Section()" );
            throw new STException( e );
        }        
    }
    
    
    
    


}
