/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportSettings;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.proctor.ProctorUtils;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.UrlEncodingUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * This class is used to make any CoreTest-Specific Changes to the Standard
 * ReportSettings
 *
 * @author Mike
 */
public class CT2ReportSettings implements ReportSettings {

    
    //public static Font 
    
    public static float CT2_MARGIN = 20;
    public static float CT2_TEXT_EXTRAMARGIN = 25;
    public static float CT2_BOX_EXTRAMARGIN = 25;
    public static float CT2_BOXHEADER_LEFTPAD = 4;
    public static int MIN_HEIGHT_FOR_CONTINUED_TEXT = 45;

    public static int MAX_INTERVIEWQS_PER_COMPETENCY = 10;
    public static int MAX_TABLE_CELL_HEIGHT = 5;

    public static float MAX_CUSTLOGO_W = 250; // 80
    public static float MAX_CUSTLOGO_H = 76;  // 40

    public static float MAX_CUSTLOGO_W_V2 = 110; // 80
    public static float MAX_CUSTLOGO_H_V2 = 60;  // 40
    
    public CT2Colors ct2Colors = null;
    public boolean devel = false;
    // public boolean redYellowGreenGraphs=true;
    public String coverDescrip = null;
    
    public static int BAR_GRAPH_CELL_HEIGHT = 22;
    public static int BELL_GRAPH_CELL_HEIGHT = 26;
    
    /*
    public static String DOCUMENT_JS_API = "this.disclosed=true;\n" +
"function openUrl( url, tgt )\n" +
"{\n" +
"  if( this.external && this.hostContainer )\n" +
"  {\n" +
"    try\n" +
"    {\n" +
"      this.hostContainer.open( url, tgt, '' );\n" +
"      //return true;\n" +
"    }\n" +
"    catch(e)\n" +
"    {\n" +
"      console.show();\n" +
"      console.println( e.toString() );\n" +
"    }  \n" +
"  }\n" +
"  //return false;\n" +
"}"; 
    */
    
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NOTE: NONE OF THESE ARE PUBLIC SO THAT OTHER REPORTS CAN OVERRIDE THEM FOR THAT REPORT but still extend from this class.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // int MIN_COUNT_FOR_PERCENTILE = 10;
    public int XXLFONTSZ = 32;
    public int XLFONTSZ = 14;
    public int LLFONTSZ = 13;
    public int LFONTSZ = 12;
    public int LMFONTSZ = 11;
    public int FONTSZ = 10;
    public int SFONTSZ = 9;
    public int XSFONTSZ = 8;
    public int XXSFONTSZ = 7;

    public BaseFont baseFont;
    public BaseFont baseFontCalibri;
    // public BaseFont baseFontCalibriDark;
    public BaseFont baseFontCalibriBold;
    public BaseFont baseFontCalibriItalic;
    public BaseFont baseFontCalibriBoldItalic;

    public BaseFont headerBaseFont;

    public Font fontXXLarge;
    public Font fontXXLargeBoldDarkBlue;
    public Font fontXXLargeWhite;
    public Font fontXXLargeLight;
    public Font fontXXLargeBold;
    public Font fontXXLargeItalic;
    public Font fontXXLargeBoldItalic;

    public Font headerFontXXLarge;
    public Font headerFontXXLargeWhite;

    public Font fontXLarge;
    public Font fontXLargeLight;
    public Font fontXLargeLightBold;
    public Font fontXLargeBoldDarkBlue;
    public Font fontXLargeBlack;
    public Font fontXLargeWhite;
    public Font fontXLargeBoldWhite;
    public Font fontXLargeBold;
    public Font fontXLargeBoldBlack;
    public Font fontXLargeItalic;
    public Font fontXLargeBoldItalic;

    public Font headerFontXLarge;
    public Font headerFontXLargeWhite;

    public Font fontLL;
    public Font fontLLWhite;
    public Font fontLLLight;
    public Font fontLLLightBold;
    public Font fontLLBold;
    public Font fontLLItalic;
    public Font fontLLBoldItalic;


    public Font fontLarge;
    public Font fontLargeWhite;
    public Font fontLargeWhiteBold;
    public Font fontLargeLight;
    public Font fontLargeLightBold;
    public Font fontLargeBoldDarkBlue;
    public Font fontLargeBold;
    public Font fontLargeBlueBold;
    public Font fontLargeItalic;
    public Font fontLargeBoldItalic;

    public Font fontLm;
    public Font fontLmWhite;
    public Font fontLmLight;
    public Font fontLmLightBold;
    public Font fontLmBold;
    public Font fontLmItalic;
    public Font fontLmBoldItalic;


    public Font headerFontLarge;
    public Font headerFontLargeWhite;

    public Font font;
    public Font fontWhite;
    public Font fontRed;
    public Font fontLight;
    public Font fontLightBold;
    public Font fontLightItalic;
    public Font fontBold;
    public Font fontBoldRed;
    public Font fontItalic;
    public Font fontBoldItalic;

    public Font fontSmall;
    public Font fontSmallWhite;
    public Font fontSmallLight;
    public Font fontSmallLightBold;
    public Font fontSmallLightItalic;
    public Font fontSmallBold;
    public Font fontSmallItalic;
    public Font fontSmallBoldItalic;
    public Font fontSmallItalicRed;
    public Font fontSmallItalicBlue;

    public Font fontXSmall;
    public Font fontXSmallWhite;
    public Font fontXSmallLight;
    public Font fontXSmallBold;
    public Font fontXSmallItalic;
    public Font fontXSmallBoldItalic;

    public Font fontXXSmall;
    public Font fontXXSmallWhite;
    public Font fontXXSmallLight;
    public Font fontXXSmallBold;
    public Font fontXXSmallItalic;
    public Font fontXXSmallBoldItalic;

    public Font fontSectionTitle;

    //public BaseColor whiteFontColor;  // #ffffff
    //public BaseColor darkFontColor;   // #282828
    //public BaseColor lightFontColor;  // #525252

    //public BaseColor scoreBoxHeaderBgColor;  // #e9e9e9
    //public BaseColor scoreBoxShadeBgColor;  // #e9e9e9
    //public BaseColor scoreBoxBgColor;  // #ffffff
    //public BaseColor scoreBoxBorderColor;  // #525252
    public float scoreBoxBorderWidth = 0.8f;
    
    public float lightBoxBorderWidth=0.75f;

    //public BaseColor headerDarkBgColor;    // #3a3a3a
    //public BaseColor titlePageBgColor; // #ffffff
    //public BaseColor pageBgColor;      // #eaeaea
    //public BaseColor hraBaseReportColor;   // #f1592a
    //public BaseColor tablePageBgColor; // #ffffff
    //public BaseColor redShadeColor;
    //public BaseColor keyBackgroundColor = new BaseColor( 0xe6, 0xe6, 0xe6 ); // e6e6e6

    // public BaseColor keyGreenColor = new BaseColor( 0x69, 0xa2, 0x20 ); // "#69a220";
    // public BaseColor keyRedColor = new BaseColor( 0xff, 0x00, 0x00 ); // "#69a220";


    //public BaseColor hraBlue = new BaseColor( 0x27, 0xb2, 0xe7 ); // 27b2e7

    //public BaseColor barGraphCoreShade1 = new BaseColor( 0x27, 0xb2, 0xe7 ); // f68d2f // new BaseColor( 0xf6, 0x8d, 0x2f ); // f68d2f
    //public BaseColor barGraphCoreShade2 = new BaseColor( 0xab, 0xe7, 0xff ); // abe7ff // new BaseColor( 0xfc, 0xab, 0x63 ); // fcab63

     public static String zeroStarsDetailFilename = "report_detail_stars_0.png";
     public static String oneStarDetailFilename = "report_detail_stars_1.png";
     public static String twoStarsDetailFilename = "report_detail_stars_2.png";
     public static String threeStarsDetailFilename = "report_detail_stars_3.png";
     public static String fourStarsDetailFilename = "report_detail_stars_4.png";
     public static String fiveStarsDetailFilename = "report_detail_stars_5.png";

     public static String zeroStarsSummaryFilename = "report_summary_stars_0.png";
     public static String oneStarSummaryFilename = "report_summary_stars_1.png";
     public static String twoStarsSummaryFilename = "report_summary_stars_2.png";
     public static String threeStarsSummaryFilename = "report_summary_stars_3.png";
     public static String fourStarsSummaryFilename = "report_summary_stars_4.png";
     public static String fiveStarsSummaryFilename = "report_summary_stars_5.png";

     public static String colorChartKeyFilename = "key-only-v1.png";
     public static String reportPointerFilename = "reportpointer2.png";
     public static String keyRedBarFilename = "key-red2.png";
     public static String keyRedYellowBarFilename = "key-redyellow2.png";
     public static String keyYellowBarFilename = "key-yellow.png";
     public static String keyYellowGreenBarFilename = "key-yellowgreen.png";
     public static String keyGreenBarFilename = "key-green2.png";
     public static String keyBlueBarFilename = "key-profile-blue.png";

     public static String keyDevelRedBarFilename = "key-devel-red.png";
     public static String keyDevelRedYellowBarFilename = "key-devel-redyellow.png";
     public static String keyDevelYellowBarFilename = "key-devel-yellow.png";
     public static String keyDevelYellowGreenBarFilename = "key-devel-yellowgreen.png";
     public static String keyDevelGreenBarFilename = "key-devel-green.png";
     
     
     public static String interviewStarFilename = "interview_star.png";

     public boolean rtl = false;
     public boolean usesNonAscii = false;
    //Image summaryCatNumericAxis;
    //public static String summaryCatNumericAxisFilename = "report_chart_axis.png";

    //Image summaryCatNumericMarker;
    //public static String summaryCatNumericMarkerFilename = "report_chart_marker.png";

    public Image zeroStarsDetailRow;
    public Image oneStarDetailRow;
    public Image twoStarsDetailRow;
    public Image threeStarsDetailRow;
    public Image fourStarsDetailRow;
    public Image fiveStarsDetailRow;

    public Image zeroStarsSummaryRow;
    public Image oneStarSummaryRow;
    public Image twoStarsSummaryRow;
    public Image threeStarsSummaryRow;
    public Image fourStarsSummaryRow;
    public Image fiveStarsSummaryRow;

    public Image interviewStar;
    public Image reportPointer;

    public Image colorChartKey;
    public Image keyRedBar;
    public Image keyRedYellowBar;
    public Image keyYellowBar;
    public Image keyYellowGreenBar;
    public Image keyGreenBar;
    public Image keyBlueBar;

    public Image hraLogoBlackText;
    public Image hraLogoBlackTextSmall;

    public Image hraLogoWhiteText;
    public Image hraLogoWhiteTextSmall;
    public Image hraCoverPageImage;
    public Image hraCoverPageImage2;
    public Image hraCoverPageBlueArrowImage;
    
    public SimJ engEquivSimJ;
    
    public LanguageUtils languageUtils;
    
    public int fontTypeId = -1;
        
    //Image rainbowBar;

    @Override
    public void initSettings(ReportData reportData) throws Exception {

        // shifting dev report to normal colors. 
        if( 1==1 )
        {
            keyDevelRedBarFilename = keyRedBarFilename;
            keyDevelRedYellowBarFilename = keyRedYellowBarFilename;
            keyDevelYellowBarFilename = keyYellowBarFilename;
            keyDevelYellowGreenBarFilename = keyYellowGreenBarFilename;
            keyDevelGreenBarFilename = keyGreenBarFilename;            
        }
        
        initColors(); 
        
        rtl = I18nUtils.isTextRTL( reportData==null ? Locale.US :  reportData.getLocale() );

        usesNonAscii = rtl || ( reportData==null ? false : reportData.getUsesNonAscii() );
            
        int rdFontTypeId = reportData==null ? -1 : reportData.getReportRuleAsInt("reportfonttypeid");
        if( rdFontTypeId>0 )
            fontTypeId = rdFontTypeId;
        
        if (baseFont == null) 
        {
            String filesRoot = RuntimeConstants.getStringValue("filesroot") + "/coretest/fonts/";

            if( fontTypeId<0 )
                fontTypeId = getFontTypeIdForLocale( reportData==null ? null : reportData.getLocale() );

            // LogService.logIt( "Ct2ReportSettings.initSettings() Locale=" + reportData.getLocale().toString() + ", fontTypeId=" + fontTypeId );
            
            if( fontTypeId==0 && !usesNonAscii )
            {
                baseFont = BaseFont.createFont(filesRoot + "calibri.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                baseFontCalibri = BaseFont.createFont(filesRoot + "calibri.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriBold = BaseFont.createFont(filesRoot + "calibrib.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriItalic = BaseFont.createFont(filesRoot + "calibrii.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "calibriz.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);

                headerBaseFont = BaseFont.createFont(filesRoot + "calibrib.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
            }

            else if( fontTypeId == 1 || (fontTypeId==0 && usesNonAscii) )
            {
                // Mike R 9/8/2016 - Changed for this to Embedded Fonts.
                baseFont = BaseFont.createFont(filesRoot + "arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                baseFontCalibri = BaseFont.createFont(filesRoot + "arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriBold = BaseFont.createFont(filesRoot + "arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriItalic = BaseFont.createFont(filesRoot + "ariali.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "arialbi.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                headerBaseFont = BaseFont.createFont(filesRoot + "arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                //baseFont = BaseFont.createFont(filesRoot + "arial.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                //baseFontCalibri = BaseFont.createFont(filesRoot + "arial.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                //baseFontCalibriBold = BaseFont.createFont(filesRoot + "arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                //baseFontCalibriItalic = BaseFont.createFont(filesRoot + "ariali.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                //baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "arialbi.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                //headerBaseFont = BaseFont.createFont(filesRoot + "arialbd.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

            }

            // Chinese
            else if( fontTypeId == 2 )
            {
                baseFont = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                baseFontCalibri = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriBold = BaseFont.createFont(filesRoot + "msyhbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriItalic = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "msyhbd.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                headerBaseFont = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                //baseFont = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                //baseFontCalibri = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                //baseFontCalibriBold = BaseFont.createFont(filesRoot + "msyhbd.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                //baseFontCalibriItalic = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                //baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "msyhbd.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

                //headerBaseFont = BaseFont.createFont(filesRoot + "msyh.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            }

            // Japanese
            else if( fontTypeId == 3 )
            {
                baseFont = BaseFont.createFont(filesRoot + "KozMinPro-Regular.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                baseFontCalibri = BaseFont.createFont(filesRoot + "KozMinPro-Regular.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriBold = BaseFont.createFont(filesRoot + "KozMinPro-Bold.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriItalic = BaseFont.createFont(filesRoot + "KozMinPro-Regular.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "KozMinPro-Bold.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                headerBaseFont = BaseFont.createFont(filesRoot + "KozMinPro-Regular.otf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                //baseFont = BaseFont.createFont(filesRoot + "meiryo.ttc", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                //baseFontCalibri = BaseFont.createFont(filesRoot + "meiryo.ttc", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                //baseFontCalibriBold = BaseFont.createFont(filesRoot + "meiryob.ttc.", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                //baseFontCalibriItalic = BaseFont.createFont(filesRoot + "meiryo.ttc", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                //baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "meiryob.ttc", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                //headerBaseFont = BaseFont.createFont(filesRoot + "meiryo.ttc", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            }

            else if( fontTypeId==4 )
            {
                baseFont = BaseFont.createFont(filesRoot + "times.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                baseFontCalibri = BaseFont.createFont(filesRoot + "times.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriBold = BaseFont.createFont(filesRoot + "timesbd.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriItalic = BaseFont.createFont(filesRoot + "timesi.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "timesbi.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);

                headerBaseFont = BaseFont.createFont(filesRoot + "timesbd.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
            }

            // cyrillic
            else if( fontTypeId==5 )
            {
                baseFont = BaseFont.createFont(filesRoot + "clearsans.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED); // BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
                baseFontCalibri = BaseFont.createFont(filesRoot + "clearsans.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriBold = BaseFont.createFont(filesRoot + "clearsans-b.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriItalic = BaseFont.createFont(filesRoot + "clearsans-i.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
                baseFontCalibriBoldItalic = BaseFont.createFont(filesRoot + "clearsans-bi.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);

                headerBaseFont = BaseFont.createFont(filesRoot + "clearsans-b.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
            }
            
            
            
// headerBaseFont = BaseFont.createFont(filesRoot + "BNKGOTHM.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED);

            fontXXLarge = new Font(baseFontCalibri, XXLFONTSZ);            
            fontXXLargeBoldDarkBlue = new Font(baseFontCalibriBold, XXLFONTSZ);
            fontXXLargeWhite = new Font(baseFontCalibri, XXLFONTSZ);
            fontXXLargeLight = new Font(baseFontCalibri, XXLFONTSZ);
            fontXXLargeBold = new Font(baseFontCalibriBold, XXLFONTSZ, Font.NORMAL);
            fontXXLargeItalic = new Font(baseFontCalibriItalic, XXLFONTSZ, Font.NORMAL);
            fontXXLargeBoldItalic = new Font(baseFontCalibriBoldItalic, XXLFONTSZ, Font.NORMAL);

            fontXLarge = new Font(baseFontCalibri, XLFONTSZ);
            // fontXLarge.se

            fontXLargeBoldDarkBlue = new Font(baseFontCalibriBold, XLFONTSZ);
            fontXLargeWhite = new Font(baseFontCalibri, XLFONTSZ);
            fontXLargeLight = new Font(baseFontCalibri, XLFONTSZ);
            fontXLargeLightBold = new Font(baseFontCalibriBold, XLFONTSZ);
            fontXLargeBold = new Font(baseFontCalibriBold, XLFONTSZ);
            fontXLargeBlack = new Font(baseFontCalibri, XLFONTSZ);
            fontXLargeBoldBlack = new Font(baseFontCalibriBold, XLFONTSZ, Font.NORMAL);
            fontXLargeBoldWhite = new Font(baseFontCalibriBold, XLFONTSZ, Font.NORMAL);
            fontXLargeItalic = new Font(baseFontCalibriItalic, XLFONTSZ, Font.NORMAL);
            fontXLargeBoldItalic = new Font(baseFontCalibriBoldItalic, XLFONTSZ, Font.NORMAL);

            fontLL = new Font(baseFontCalibri, LLFONTSZ);
            fontLLWhite = new Font(baseFontCalibri, LLFONTSZ);
            fontLLLight = new Font(baseFontCalibri, LLFONTSZ);
            fontLLLightBold = new Font(baseFontCalibriBold, LLFONTSZ);
            fontLLBold = new Font(baseFontCalibriBold, LLFONTSZ, Font.NORMAL);
            fontLLItalic = new Font(baseFontCalibriItalic, LLFONTSZ, Font.NORMAL);
            fontLLBoldItalic = new Font(baseFontCalibriBoldItalic, LLFONTSZ, Font.NORMAL);

            fontLarge = new Font(baseFontCalibri, LFONTSZ);
            fontLargeWhite = new Font(baseFontCalibri, LFONTSZ);
            fontLargeWhiteBold = new Font(baseFontCalibriBold, LFONTSZ);
            fontLargeBoldDarkBlue = new Font(baseFontCalibriBold, LFONTSZ);
            fontLargeLight = new Font(baseFontCalibri, LFONTSZ);
            fontLargeLightBold = new Font(baseFontCalibriBold, LFONTSZ);
            fontLargeBold = new Font(baseFontCalibriBold, LFONTSZ, Font.NORMAL);
            fontLargeItalic = new Font(baseFontCalibriItalic, LFONTSZ, Font.NORMAL);
            fontLargeBoldItalic = new Font(baseFontCalibriBoldItalic, LFONTSZ, Font.NORMAL);
            fontLargeBlueBold = new Font(baseFontCalibriBold, LFONTSZ, Font.NORMAL);


            fontLm = new Font(baseFontCalibri, LMFONTSZ);
            fontLmWhite = new Font(baseFontCalibri, LMFONTSZ);
            fontLmLight = new Font(baseFontCalibri, LMFONTSZ);
            fontLmLightBold = new Font(baseFontCalibriBold, LMFONTSZ);
            fontLmBold = new Font(baseFontCalibriBold, LMFONTSZ, Font.NORMAL);
            fontLmItalic = new Font(baseFontCalibriItalic, LMFONTSZ, Font.NORMAL);
            fontLmBoldItalic = new Font(baseFontCalibriBoldItalic, LMFONTSZ, Font.NORMAL);

            fontSectionTitle = fontLargeLight;

            font = new Font(baseFontCalibri, FONTSZ);
            fontWhite = new Font(baseFontCalibri, FONTSZ);
            fontRed = new Font(baseFontCalibri, FONTSZ);
            fontLight = new Font(baseFontCalibri, FONTSZ);
            fontLightBold = new Font(baseFontCalibriBold, FONTSZ);
            fontLightItalic = new Font(baseFontCalibriItalic, FONTSZ);
            fontBold = new Font(baseFontCalibriBold, FONTSZ, Font.NORMAL);
            fontBoldRed = new Font(baseFontCalibriBold, FONTSZ, Font.NORMAL);
            fontItalic = new Font(baseFontCalibriItalic, FONTSZ, Font.NORMAL);
            fontBoldItalic = new Font(baseFontCalibriBoldItalic, FONTSZ, Font.NORMAL);

            fontSmall = new Font(baseFontCalibri, SFONTSZ);
            fontSmallWhite = new Font(baseFontCalibri, SFONTSZ);
            fontSmallLight = new Font(baseFontCalibri, SFONTSZ);
            fontSmallLightBold = new Font(baseFontCalibriBold, XSFONTSZ);
            fontSmallLightItalic = new Font(baseFontCalibriItalic, XSFONTSZ);
            fontSmallBold = new Font(baseFontCalibriBold, SFONTSZ, Font.NORMAL);
            fontSmallItalic = new Font(baseFontCalibriItalic, SFONTSZ, Font.NORMAL);
            fontSmallBoldItalic = new Font(baseFontCalibriBoldItalic, SFONTSZ, Font.NORMAL);
            fontSmallItalicRed = new Font(baseFontCalibriItalic, SFONTSZ, Font.NORMAL);
            fontSmallItalicBlue = new Font(baseFontCalibriItalic, SFONTSZ, Font.NORMAL);

            fontXSmall = new Font(baseFontCalibri, XSFONTSZ);
            fontXSmallWhite = new Font(baseFontCalibri, XSFONTSZ);
            fontXSmallLight = new Font(baseFontCalibri, XSFONTSZ);
            fontXSmallBold = new Font(baseFontCalibriBold, XSFONTSZ, Font.NORMAL);
            fontXSmallItalic = new Font(baseFontCalibriItalic, XSFONTSZ, Font.NORMAL);
            fontXSmallBoldItalic = new Font(baseFontCalibriBoldItalic, XSFONTSZ, Font.NORMAL);

            fontXXSmall = new Font(baseFontCalibri, XXSFONTSZ);
            fontXXSmallWhite = new Font(baseFontCalibri, XXSFONTSZ);
            fontXXSmallLight = new Font(baseFontCalibri, XXSFONTSZ);
            fontXXSmallBold = new Font(baseFontCalibriBold, XXSFONTSZ, Font.NORMAL);
            fontXXSmallItalic = new Font(baseFontCalibriItalic, XXSFONTSZ, Font.NORMAL);
            fontXXSmallBoldItalic = new Font(baseFontCalibriBoldItalic, XXSFONTSZ, Font.NORMAL);

            headerFontXXLarge = new Font(headerBaseFont, XXLFONTSZ);
            headerFontXXLargeWhite = new Font(headerBaseFont, XXLFONTSZ);
            headerFontXLarge = new Font(headerBaseFont, XLFONTSZ);
            headerFontXLargeWhite = new Font(headerBaseFont, XLFONTSZ);
            headerFontLarge = new Font(headerBaseFont, LFONTSZ);
            headerFontLargeWhite = new Font(headerBaseFont, LFONTSZ);

            //whiteFontColor = BaseColor.WHITE;  // #ffffff
            //darkFontColor = new BaseColor(0x4d,0x4d,0x4d);   // #4d4d4d
            //lightFontColor = new BaseColor(0x80,0x80,0x80);  // #525252

            //scoreBoxHeaderBgColor = hraBlue;  // #e9e9e9
            //scoreBoxBgColor = BaseColor.WHITE;  // #ffffff
            //scoreBoxBorderColor = new BaseColor( 0x92, 0x92, 0x92);  // #525252
            //scoreBoxShadeBgColor = new BaseColor( 0xca, 0xe4, 0xee );

            //headerDarkBgColor = hraBlue; //  new BaseColor(39, 178, 231); // #27b2e7 // new BaseColor( 58,58,58 );    // #3a3a3a
            //titlePageBgColor =  BaseColor.WHITE; // new BaseColor(255, 255, 255); // #ffffff
            //pageBgColor =    BaseColor.WHITE;  // new BaseColor(234, 234, 234);      // #eaeaea
            //hraBaseReportColor = hraBlue; // new BaseColor(39, 178, 231); // #27b2e7   //   new BaseColor( 241,90,41 );   // #f1592a

            //tablePageBgColor = BaseColor.WHITE; //  new BaseColor(0xf9, 0xf9, 0xf9);

            // redShadeColor = new BaseColor(0xf0, 0x80, 0x80);

            BaseColor baseFontColor = ct2Colors.darkFontColor;

            fontXXLarge.setColor(baseFontColor);
            fontXXLargeBoldDarkBlue.setColor(new BaseColor( 0x0b, 0x50, 0x8b));
            fontXXLargeWhite.setColor(ct2Colors.whiteFontColor);
            fontXXLargeLight.setColor(ct2Colors.lightFontColor);
            fontXXLargeBold.setColor(baseFontColor);
            fontXXLargeItalic.setColor(baseFontColor);
            fontXXLargeBoldItalic.setColor(baseFontColor);

            fontXLarge.setColor(baseFontColor);
            fontXLargeBoldDarkBlue.setColor(new BaseColor( 0x0b, 0x50, 0x8b));
            fontXLargeWhite.setColor(ct2Colors.whiteFontColor);
            fontXLargeBlack.setColor(BaseColor.BLACK);
            fontXLargeLight.setColor(ct2Colors.lightFontColor);
            fontXLargeLightBold.setColor(ct2Colors.lightFontColor);
            fontXLargeBold.setColor(baseFontColor);
            fontXLargeBoldWhite.setColor(ct2Colors.whiteFontColor);
            fontXLargeBoldBlack.setColor(BaseColor.BLACK);
            fontXLargeItalic.setColor(baseFontColor);
            fontXLargeBoldItalic.setColor(baseFontColor);

            fontLL.setColor(baseFontColor);
            fontLLWhite.setColor(ct2Colors.whiteFontColor);
            fontLLLight.setColor(ct2Colors.lightFontColor);
            fontLLLightBold.setColor(ct2Colors.lightFontColor);
            fontLLBold.setColor(baseFontColor);
            fontLLItalic.setColor(baseFontColor);
            fontLLBoldItalic.setColor(baseFontColor);

            fontLarge.setColor(baseFontColor);
            fontLargeBoldDarkBlue.setColor(new BaseColor( 0x0b, 0x50, 0x8b));
            fontLargeWhite.setColor(ct2Colors.whiteFontColor);
            fontLargeWhiteBold.setColor(ct2Colors.whiteFontColor);
            fontLargeLight.setColor(ct2Colors.lightFontColor);
            fontLargeLightBold.setColor(ct2Colors.lightFontColor);
            fontLargeBold.setColor(baseFontColor);
            fontLargeItalic.setColor(baseFontColor);
            fontLargeBoldItalic.setColor(baseFontColor);

            fontLargeBlueBold.setColor( ct2Colors.hraBlue );

            fontLm.setColor(baseFontColor);
            fontLmWhite.setColor(ct2Colors.whiteFontColor);
            fontLmLight.setColor(ct2Colors.lightFontColor);
            fontLmLightBold.setColor(ct2Colors.lightFontColor);
            fontLmBold.setColor(baseFontColor);
            fontLmItalic.setColor(baseFontColor);
            fontLmBoldItalic.setColor(baseFontColor);

            font.setColor(baseFontColor);
            fontWhite.setColor(ct2Colors.whiteFontColor);
            fontRed.setColor( BaseColor.RED );

            fontLight.setColor(ct2Colors.lightFontColor);
            fontLightBold.setColor(ct2Colors.lightFontColor);
            fontLightItalic.setColor(ct2Colors.lightFontColor);
            fontBold.setColor(baseFontColor);
            fontBoldRed.setColor( BaseColor.RED );            
            fontItalic.setColor(baseFontColor);
            fontBoldItalic.setColor(baseFontColor);

            fontSmall.setColor(baseFontColor);
            fontSmallWhite.setColor(ct2Colors.whiteFontColor);
            fontSmallLight.setColor(ct2Colors.lightFontColor);
            fontSmallLightBold.setColor(ct2Colors.lightFontColor);
            fontSmallLightItalic.setColor(ct2Colors.lightFontColor);
            fontSmallBold.setColor(baseFontColor);
            fontSmallItalic.setColor(baseFontColor);
            fontSmallBoldItalic.setColor(baseFontColor);
            fontSmallItalicRed.setColor(ct2Colors.red);
            fontSmallItalicBlue.setColor(ct2Colors.blue);

            fontXSmall.setColor(baseFontColor);
            fontXSmallWhite.setColor(ct2Colors.whiteFontColor);
            fontXSmallLight.setColor(ct2Colors.lightFontColor);
            fontXSmallBold.setColor(baseFontColor);
            fontXSmallItalic.setColor(baseFontColor);
            fontXSmallBoldItalic.setColor(baseFontColor);

            fontXXSmall.setColor(baseFontColor);
            fontXXSmallWhite.setColor(ct2Colors.whiteFontColor);
            fontXXSmallLight.setColor(ct2Colors.lightFontColor);
            fontXXSmallBold.setColor(baseFontColor);
            fontXXSmallItalic.setColor(baseFontColor);
            fontXXSmallBoldItalic.setColor(baseFontColor);

            headerFontXXLarge.setColor(baseFontColor);
            headerFontXLarge.setColor(baseFontColor);
            headerFontLarge.setColor(baseFontColor);
            headerFontXXLargeWhite.setColor(ct2Colors.whiteFontColor);
            headerFontXLargeWhite.setColor(ct2Colors.whiteFontColor);
            headerFontLargeWhite.setColor(ct2Colors.whiteFontColor);

            zeroStarsDetailRow = ITextUtils.getITextImage( getLocalImageUrl(zeroStarsDetailFilename));
            oneStarDetailRow = ITextUtils.getITextImage( getLocalImageUrl(oneStarDetailFilename));
            twoStarsDetailRow = ITextUtils.getITextImage(getLocalImageUrl(twoStarsDetailFilename));
            threeStarsDetailRow = ITextUtils.getITextImage(getLocalImageUrl(threeStarsDetailFilename));
            fourStarsDetailRow = ITextUtils.getITextImage(getLocalImageUrl(fourStarsDetailFilename));
            fiveStarsDetailRow = ITextUtils.getITextImage(getLocalImageUrl(fiveStarsDetailFilename));

            zeroStarsSummaryRow = ITextUtils.getITextImage( getLocalImageUrl(zeroStarsSummaryFilename));
            oneStarSummaryRow = ITextUtils.getITextImage( getLocalImageUrl(oneStarSummaryFilename));
            twoStarsSummaryRow = ITextUtils.getITextImage(getLocalImageUrl(twoStarsSummaryFilename));
            threeStarsSummaryRow = ITextUtils.getITextImage(getLocalImageUrl(threeStarsSummaryFilename));
            fourStarsSummaryRow = ITextUtils.getITextImage(getLocalImageUrl(fourStarsSummaryFilename));
            fiveStarsSummaryRow = ITextUtils.getITextImage(getLocalImageUrl(fiveStarsSummaryFilename));

            interviewStar = ITextUtils.getITextImage( getLocalImageUrl(interviewStarFilename) );

            colorChartKey = ITextUtils.getITextImage( getLocalImageUrl( colorChartKeyFilename ) );
            reportPointer = ITextUtils.getITextImage( getLocalImageUrl( reportPointerFilename ) );
            reportPointer.scalePercent(40);

            this.keyRedBar = ITextUtils.getITextImage( getLocalImageUrl( devel ? keyDevelRedBarFilename : keyRedBarFilename ) );
            this.keyRedYellowBar = ITextUtils.getITextImage( getLocalImageUrl( devel ? keyDevelRedYellowBarFilename : keyRedYellowBarFilename ) );
            this.keyGreenBar = ITextUtils.getITextImage( getLocalImageUrl( devel ? keyDevelGreenBarFilename : keyGreenBarFilename ) );
            this.keyYellowBar = ITextUtils.getITextImage( getLocalImageUrl( devel ? keyDevelYellowBarFilename : keyYellowBarFilename ) );
            this.keyYellowGreenBar = ITextUtils.getITextImage( getLocalImageUrl( devel ? keyDevelYellowGreenBarFilename : keyYellowGreenBarFilename ) );
            this.keyBlueBar = ITextUtils.getITextImage( getLocalImageUrl( keyBlueBarFilename ) );

            float barScaleX = 50;
            float barScaleY = 40;

            keyRedBar.scalePercent(barScaleX, barScaleY);
            keyRedYellowBar.scalePercent(barScaleX, barScaleY);
            keyYellowBar.scalePercent(barScaleX, barScaleY);
            keyYellowGreenBar.scalePercent(barScaleX, barScaleY);
            keyGreenBar.scalePercent(barScaleX, barScaleY);
            keyBlueBar.scalePercent(barScaleX, barScaleY);

            hraLogoBlackText = reportData==null ? null : ITextUtils.getITextImage(reportData.getHRALogoBlackTextUrl( devel ));
            hraLogoWhiteText = reportData==null ? null : ITextUtils.getITextImage(reportData.getHRALogoWhiteTextUrl( devel ));
            hraLogoBlackTextSmall = reportData==null ? null : ITextUtils.getITextImage(reportData.getHRALogoBlackTextSmallUrl( devel ));
            
            // LogService.logIt( "CT2ReportSettings.initSettings() devel=" + devel + ", url=" + reportData.getHRALogoWhiteTextSmallUrl( devel ) );
            
            hraLogoWhiteTextSmall = reportData==null ? null : ITextUtils.getITextImage(reportData.getHRALogoWhiteTextSmallUrl( devel ));
            
            hraCoverPageImage = reportData==null ? null : ITextUtils.getITextImage(reportData.getHRACoverPageUrl() );
                                               
            hraCoverPageBlueArrowImage = reportData==null ? null : ITextUtils.getITextImage(reportData.getHRACoverPageBlueArrowUrl() );
            //summaryCatNumericAxis = ITextUtils.getITextImage( getLocalImageUrl(summaryCatNumericAxisFilename));

            //summaryCatNumericMarker = ITextUtils.getITextImage( getLocalImageUrl(summaryCatNumericMarkerFilename));

            // CT2SummaryScoreGraphicCellEvent.summaryCatNumericMarker = summaryCatNumericMarker;

            // rainbowBar = ITextUtils.getITextImage( reportData.getRainbowBarUrl() );

            float highresscale = 100 * 72 / 300;

            // float dw = redDot.getScaledWidth();
            float dotScale = 72;// highresscale; // dw > 40 ? 40/dw : dw/40;

            float summaryStarScale = 60;

            // float whiteAdj = 0.5f;
            hraLogoBlackText.scalePercent(highresscale);
            hraLogoWhiteText.scalePercent(highresscale);
            hraLogoBlackTextSmall.scalePercent(highresscale);
            hraLogoWhiteTextSmall.scalePercent(highresscale );
            // rainbowBar.scalePercent( highresscale);

            oneStarSummaryRow.scalePercent(summaryStarScale);
            twoStarsSummaryRow.scalePercent(summaryStarScale);
            threeStarsSummaryRow.scalePercent(summaryStarScale);
            fourStarsSummaryRow.scalePercent(summaryStarScale);
            fiveStarsSummaryRow.scalePercent(summaryStarScale);


            oneStarDetailRow.scalePercent(dotScale);
            twoStarsDetailRow.scalePercent(dotScale);
            threeStarsDetailRow.scalePercent(dotScale);
            fourStarsDetailRow.scalePercent(dotScale);
            fiveStarsDetailRow.scalePercent(dotScale);

            interviewStar.scalePercent( 52 );

            colorChartKey.scalePercent( 68 );
        }
    }
    
    
    

    public boolean isValidForTestEvent()
    {
        return true;
    }
    
    public void initColors()
    {
        // Nothing. 
        if( ct2Colors == null )
            ct2Colors = CT2Colors.getCt2Colors( devel );  
                
    }
    
    
    public SimJ.Simcompetency getSimCompetencyForTes( TestEventScore tes, SimJ simJ ) throws Exception
    {
        String tn;
        
        for( SimJ.Simcompetency sc : simJ.getSimcompetency() )
        {
            // LogService.logIt( "Ct2ReportSettings.getSimCompetencyForTes() tes.name=" + tes.getName() + " sc.name=" + sc.getName() + ", tes.nameeng=" + tes.getNameEnglish() + ", sc.nameeng=" + sc.getNameenglish() );
            tn = UrlEncodingUtils.decodeKeepPlus( sc.getName() );
            
            if( tes.getName().equals( tn ) )
                return sc;
            
            if( sc.getNameenglish()!= null && 
                !sc.getNameenglish().isEmpty() && 
                tes.getNameEnglish()!=null && 
                !tes.getNameEnglish().isEmpty() )
            {
                tn = UrlEncodingUtils.decodeKeepPlus( sc.getNameenglish() );
                
                if( tn.equals( tes.getNameEnglish() ))
                return sc;
            }
        }

        //for( SimJ.Simcompetency sc : simJ.getSimcompetency() )
        //{
        //    LogService.logIt( "Ct2ReportSettings.getSimCompetencyForTes() tes.name=" + tes.getName() + " sc.name=" + sc.getName() + ", tes.nameeng=" + tes.getNameEnglish() + ", sc.nameeng=" + sc.getNameenglish() );
        //}

        
        return null;
    }


    /*
    public int getScoreCategoryTypeId( SimJ.Simcompetency simCompetencyObj, float scaledScore, ScoreColorSchemeType scst )
    {
        ScoreCategoryType scoreCat = ScoreCategoryType.getForScore( scst,
                                                                    scaledScore,
                                                                    simCompetencyObj.getHighcliffmin(),
                                                                    simCompetencyObj.getGreenmin(),
                                                                    simCompetencyObj.getYellowgreenmin(),
                                                                    simCompetencyObj.getYellowmin(),
                                                                    simCompetencyObj.getRedyellowmin(),
                                                                    0,
                                                                    simCompetencyObj.getCategorydisttype(),
                                                                    simCompetencyObj.getHighclifflevel() );

        if( simCompetencyObj.getCategoryadjustmentthreshold()>0 && scaledScore <= simCompetencyObj.getCategoryadjustmentthreshold() )
            scoreCat = scoreCat.adjustOneLevelUp( scst );

        return scoreCat.getScoreCategoryTypeId();
    }
    */


    
    
    public boolean isOkToAutoTranslate( Locale testContentLoc, Locale rptLoc )
    {
        if( 1==2 )
            return false;
        
        if( testContentLoc==null || rptLoc==null )
            return false;
        
        if( testContentLoc.getLanguage().equalsIgnoreCase(rptLoc.getLanguage() ) )
            return false;
        
        return isOkToAutoTranslate( testContentLoc ) && isOkToAutoTranslate( rptLoc );
    }
    
    public boolean isOkToAutoTranslate( Locale loc )
    {
        
        if( loc==null )
            return false;
        
        String ln = loc.getLanguage();
        
        if( ln.equalsIgnoreCase("en") )
            return true;
        
        if( ln.equalsIgnoreCase("es") )
            return true;
        
        if( ln.equalsIgnoreCase("de") )
            return true;
        
        if( ln.equalsIgnoreCase("fr") )
            return true;
        
        return false;        
    }  
    
    public boolean getIsRTL()
    {
        return rtl;
    }

    
    /**
     * 
     * @param reportData
     * @param ufl  List<UploadedUserFile> of photo images, where thumbWidth, thumbHght are set and tempStr1=fully qualified url, 
     *                                   tempStr2=Date (can be null), 
     *                                   tempInt1=(0 or 1) timeout, 
     *                                   tempInt2=orientation 
     * @param uflId same as ufl but for ID images.
     * @param caveatList
     * @param overallProctorScore
     * @param imagesToShow
     * @param pageWidth
     * @return
     * @throws Exception 
     */
    public PdfPTable generateIdentityImageCaptureTableTop( ReportData reportData, List<String[]> caveatList, float overallProctorScore, float pageWidth) throws Exception
    {   
        try
        {            
            
            // First create the table
            PdfPCell c;
            PdfPTable t;
            
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            int count = 0;                

            // First, add a table
            t = new PdfPTable( new float[] { 1f, 1f } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );   
            
            if( !caveatList.isEmpty() )
            {                
                c = new PdfPCell( new Phrase( MessageFactory.getStringMessage(reportData.getLocale(), "g.ImgCapReportTableAnal" ) , fontLmWhite) );                
                
                // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorder( Rectangle.NO_BORDER );
                
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setColspan(2);
                c.setPadding( 2 );
                c.setPaddingBottom( 4 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, true, false, false ));
                setRunDirection( reportData, c );                 
                t.addCell(c);
                   
                String s1, s2;
                count = 0;                
                BaseColor bgCol = null;
                boolean darkFont = false;
                
                if( overallProctorScore <= 0 )
                {
                    s2 = MessageFactory.getStringMessage(reportData.getLocale(), "g.ImgCapRiskUnavailPrem", null );
                    bgCol = ct2Colors.scoreBoxShadeBgColor;    
                    darkFont=true;
                }
                else if( overallProctorScore <= Constants.IMAGE_CAPTURE_HIGH_RISK_CUTOFF ) // 33.33f )
                {
                    s2 = MessageFactory.getStringMessage(reportData.getLocale(), "g.ImgCapRiskHigh", null );
                    bgCol = ct2Colors.red;
                }
                else if( overallProctorScore <= Constants.IMAGE_CAPTURE_MED_RISK_CUTOFF ) // 75f )
                {
                    s2 = MessageFactory.getStringMessage(reportData.getLocale(), "g.ImgCapRiskMedium", null );
                    bgCol = ct2Colors.yellow; 
                    darkFont = true;
                }
                else
                {
                    s2 = MessageFactory.getStringMessage(reportData.getLocale(), "g.ImgCapRiskLow", null );
                    bgCol = ct2Colors.green;                                        
                }

                c = new PdfPCell( new Phrase( "- " + MessageFactory.getStringMessage(reportData.getLocale(), "g.ImgCapRisk", null ), darkFont ? font : fontWhite ) );                
                //c.setBackgroundColor(bgCol);
                //c.setBorder(  Rectangle.LEFT  | Rectangle.TOP  );
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 2 );
                c.setPaddingLeft( 15 );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), bgCol, false, false, false, true ));
                t.addCell(c);  


                c = new PdfPCell( new Phrase( s2, darkFont ? font : fontWhite ) );                
                // c.setBorder(  Rectangle.RIGHT | Rectangle.TOP  );
                c.setBorder(Rectangle.NO_BORDER);
                // c.setBackgroundColor(bgCol);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 2 );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), bgCol, false, false, true, false ));
                t.addCell(c);  
                
                for( String[] caveat : caveatList )
                {
                    count++;

                    c = new PdfPCell( new Phrase( "- " + caveat[0], font ) );                
    
                    // not last one or can show images and have images to show 
                    //if( count<caveatList.size() ) //  || ( reportData.o.getCandidateImageViewTypeId()<=0 && showImages && hasImages ) )                    
                    //    c.setBorder(count==1 ? Rectangle.LEFT | Rectangle.TOP :  Rectangle.LEFT  );
                    
                    // last one and do not have images or cannot show them
                    //else
                    //    c.setBorder(count==1 ? Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM :  Rectangle.LEFT | Rectangle.BOTTOM  );
                      
                    c.setBorder(Rectangle.NO_BORDER);
                    //c.setBorderWidth( scoreBoxBorderWidth );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setPadding( 2 );
                    c.setPaddingLeft( 15 );
                    if( caveat.length<2 )
                    {
                        c.setColspan(2);
                        //if( count<caveatList.size() ) // || ( reportData.o.getCandidateImageViewTypeId()<=0 && showImages && hasImages ) )                    
                        //    c.setBorder(count==1 ? Rectangle.LEFT | Rectangle.RIGHT | Rectangle.TOP :  Rectangle.LEFT | Rectangle.RIGHT  );

                        // last one and do not have images or cannot show them
                        //else
                        //    c.setBorder(count==1 ? Rectangle.BOX :  Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM  );                        
                    }
                    if( count==caveatList.size() )
                        c.setPaddingBottom(5);
                    t.addCell(c);  

                    if( caveat.length>=2 )
                    {
                        c = new PdfPCell( new Phrase( caveat[1], font ) );                

                        // not last one or can show images and have images to show 
                        //if( ( count<caveatList.size() ) ) // || ( reportData.o.getCandidateImageViewTypeId()<=0 && showImages && hasImages ) )                    
                        //    c.setBorder(count==1 ? Rectangle.RIGHT | Rectangle.TOP : Rectangle.RIGHT  );

                        // last one and do not have images
                        //else
                        //    c.setBorder(count==1 ? Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
                        c.setBorder( Rectangle.NO_BORDER );
                        //c.setBorderWidth( scoreBoxBorderWidth );
                        //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setPadding( 2 );
                        if( count==caveatList.size() )
                            c.setPaddingBottom(5);
                        t.addCell(c);  
                    }                                        
                }
            }
                                    
            return t;
           // LogService.logIt( "BaseCT2ReportTemplate.generateIdentityImageCaptureTableTop() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct2ReportSettings.generateIdentityImageCaptureTableTop()" );
            throw new STException( e );
        }        
    }
    

    public PdfPTable generateIdentityImageCaptureTableImages( ReportData reportData, List<UploadedUserFile> ufl, List<UploadedUserFile> uflId, float pageWidth) throws Exception
    {   
        try
        {
            boolean showImages = !reportData.getReportRuleAsBoolean( "captimgsoff" ) && !reportData.tk.getHideMediaInReports() && reportData.o.getHideProcImagesPdf()<=0;
                 
            if( !showImages )
                return null;
            
            // LogService.logIt(  "CT2ReportSettings.generateIdentityImageCaptureTableImages() showImages=" + showImages + ", reportData.tk.getHideMediaInReports()=" + reportData.tk.getHideMediaInReports() + ", ufl.size=" + ufl.size() + ", ufil.size=" + uflId.size() + ", testEventId=" + reportData.te.getTestEventId() );            
            if( ufl==null )
                ufl = new ArrayList<>();
            if( uflId==null )
                uflId=new ArrayList<>();
            
            boolean hasImages = !ufl.isEmpty() || !uflId.isEmpty();
            
            if( !hasImages )
                return null;
            
            // First create the table
            PdfPCell c;
            
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            int count = 0;                
                        
            PdfPTable t2 = new PdfPTable( new float[] { 1f,1f,1f,1f } );
            t2.setHorizontalAlignment( Element.ALIGN_CENTER );
            t2.setTotalWidth( outerWid - 20 );
            t2.setLockedWidth( true );   
            
            String ts =  MessageFactory.getStringMessage(reportData.getLocale(), "g.Timeout" );            
            Chunk tsChk = new Chunk( "\n" + ts , fontSmallItalicRed );
            
            
            int imgCt = 0;
            // LogService.logIt(  "CT2ReportSettings.generateIdentityImageCaptureTableImages() BBB.1 Adding Images reportData.o.getCandidateImageViewTypeId()=" + reportData.o.getCandidateImageViewTypeId());
            if( reportData.o.getCandidateImageViewTypeId()<=0 && showImages )
            {
                List<UploadedUserFile> uflx = new ArrayList<>();
                int initFacePhotoCt = 0;
                            
                // First photo is face.
                if( !ufl.isEmpty() )
                {
                    for( UploadedUserFile u : ufl )
                    {
                        if( !u.isPreTestImage() )
                            break;
                        initFacePhotoCt++;
                    }

                    if( initFacePhotoCt<=0 )
                        initFacePhotoCt=1;
                    
                    // addImageCells(  ufl.subList(0, 1), t2, tsChk );
                    uflx.addAll(  ufl.subList(0, initFacePhotoCt) );
                    
                    
                    // add the last ones.
                    if( uflx.size()>Constants.MAX_INITIAL_PHOTO_IMAGES_IN_REPORT )
                    {
                        uflx = uflx.subList( uflx.size()-Constants.MAX_INITIAL_PHOTO_IMAGES_IN_REPORT, uflx.size() );
                    }

                    imgCt+=uflx.size();
                }
                
                // next all ids
                if( !uflId.isEmpty() )
                {
                    if( uflId.size()>Constants.MAX_IDCARD_IMAGES_IN_REPORT )
                    {                        
                        uflx.addAll(uflId.subList(uflId.size()-Constants.MAX_IDCARD_IMAGES_IN_REPORT, uflId.size() ) );
                        // addImageCells(  uflId.subList( uflId.size()-Constants.MAX_IDCARD_IMAGES_IN_REPORT, uflId.size() ), t2, tsChk );
                        imgCt+=Constants.MAX_IDCARD_IMAGES_IN_REPORT;
                    }
                    else
                    {
                        uflx.addAll(  uflId );
                        // addImageCells(  uflId, t2, tsChk );
                        imgCt+=uflId.size();
                    }
                }
                
                // next remaining faces.
                if( ufl.size()>initFacePhotoCt )
                {
                    int maxImgs = Constants.MAX_IDENTITY_IMAGES_IN_REPORT - imgCt;
                    ufl = ufl.subList(initFacePhotoCt, ufl.size());
                    if( ufl.size()>maxImgs)
                    {        
                        UploadedUserFile uufLast = ufl.get(ufl.size()-1 );
                        
                        //Collections.shuffle( ufl );
                        uflx.addAll( ufl.subList(0, maxImgs-1) );
                        
                        uflx.add( uufLast );
                        // addImageCells(  ufl.subList(0, maxImgs), t2, tsChk );
                    }
                    else
                        uflx.addAll( ufl );
                        // addImageCells(  ufl, t2, tsChk );
                }
                
                addImageCells(  uflx, t2, tsChk, reportData.getLocale() );                            
            }
            
            return t2;
           // LogService.logIt( "BaseCT2ReportTemplate.generateIdentityImageCaptureTableImages() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct2ReportSettings.generateIdentityImageCaptureTableImages()" );
            throw new STException( e );
        }        
    }

    
    
    private void addImageCells(  List<UploadedUserFile> ufl, PdfPTable t2, Chunk tsChk, Locale locale ) throws Exception
    {
        PdfPCell c;
        PdfPTable t3;
        String dateTimeStr;  
        String infoStr;
        BaseColor borderColor;

        Image iconImage;
        // FileContentType fct;
        String thumbUrl;
        float thumbWid;
        boolean timeout;
        Paragraph par; 
        int count = 0;

        for( UploadedUserFile uuf : ufl )
        {
            thumbUrl=uuf.getTempStr1();
            
            // LogService.logIt( "CT2ReportSettings.addImageCells() ThumbUrl=" + thumbUrl );
            
            timeout = uuf.getTempInt1()==1;
            dateTimeStr =uuf.getTempStr2();
            if( dateTimeStr==null )
                dateTimeStr="";
            infoStr = ProctorUtils.getProctorImageIdStr( uuf, locale );            
            dateTimeStr += (!dateTimeStr.isBlank() ? ", " : "" ) + infoStr;
            
            borderColor = null;
            
            if( uuf.isFailedImage() )
                borderColor = BaseColor.RED;

            // blue
            else if( uuf.isPreTestImage() )
                borderColor = BaseColor.BLUE;

            else if( uuf.getUploadedUserFileType().getIsRemoteProctoringId() )
                borderColor = BaseColor.YELLOW;
            
            else
                borderColor = BaseColor.GREEN;
                        
            thumbWid = uuf.getWidth();

            iconImage = getImageInstance( thumbUrl, uuf.getTestEventId() ); //  ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( thumbUrl ) );
           
            if( iconImage!=null )
            {
                if( thumbWid<=0 )
                    thumbWid = iconImage.getPlainWidth();

                if( thumbWid>0 && thumbWid>120 )
                    iconImage.scalePercent( 100f*120f/((float) thumbWid) );
                else if( thumbWid>0 )
                {}
                else
                    iconImage.scalePercent( 25f );

                if( uuf.getOrientation()!=0 )
                    iconImage.setRotationDegrees( uuf.getOrientationForIText() );

                if( borderColor!=null )
                {
                    // LogService.logIt( "CT2ReportSettings.addImageCells() borderColor is not null. " + borderColor.getRed() + " " + borderColor.getGreen() + " " + borderColor.getBlue() );
                    iconImage.setBorder( Rectangle.BOX );
                    iconImage.setBorderWidth(6);
                    iconImage.setBorderColor(borderColor);
                }
            }
            
            count++;

            t3 = new PdfPTable(1);                

            if( iconImage==null )
                c = new PdfPCell( new Phrase("") );
            else
                c = new PdfPCell( iconImage ); 
            c.setPadding(2);
            c.setBackgroundColor( BaseColor.WHITE );
            c.setBorder( Rectangle.NO_BORDER );
            
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            t3.addCell(c);

            par = new Paragraph();
            par.add( new Phrase( dateTimeStr, this.fontSmallItalic ) );
            if( timeout )
                par.add( tsChk );

            c = new PdfPCell( par ); 
            c.setPadding(2);
            c.setBorder( Rectangle.NO_BORDER );

            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            t3.addCell(c);

            c = new PdfPCell( t3 ); 
            c.setPadding(4);
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            c.setVerticalAlignment( Element.ALIGN_TOP);
            t2.addCell(c);
        }

        // now finish the cells.
        int rem = count%4;
        if( rem>0 )
        {
            for( int i=0;i<4-rem;i++ )
            {
                c = new PdfPCell( new Phrase("") );                
                c.setBorder( 0 );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                t2.addCell( c );                
                count++;                
            }
        }        
    }
    
    
    public Image getImageInstance( String thumbUrl, long testEventId )
    {
        try
        {
            // do this to prevent AWS S3 Slow Down Errors.
            Thread.sleep(10);

            return ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( thumbUrl ) );
        }
        catch( FileNotFoundException e )
        {
            LogService.logIt( "CT2ReportSettings.getImageInstance() XXX.1 ERROR " + e.toString() + ", thumbUrl=" + thumbUrl + ", testEventId=" + testEventId );
            return null;
        }
        catch( IOException | com.itextpdf.text.BadElementException  e )
        {
            LogService.logIt( "CT2ReportSettings.getImageInstance() XXX.2 ERROR " + e.toString() + ", thumbUrl=" + thumbUrl + ", testEventId=" + testEventId );
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT2ReportSettings.getImageInstance() XXX.3 ERROR thumbUrl=" + thumbUrl + ", testEventId=" + testEventId );
            return null;
        }
    }
    
    
    
    public PdfPTable getBasicIdentityImageCaptureTableTop( ReportData reportData, List<TestEventScore> tesl , float pageWidth ) throws Exception
    {   
        try
        {
            float avgScore = 0;
            // int pairsCompared = 0;
            // List<UploadedUserFile> ufl = new ArrayList<>();
            // List<UploadedUserFile> uflId = new ArrayList<>();
            List<String[]> caveatList = new ArrayList<>();
            
            //LogService.logIt( "BaseCT2ReportTemplate.getBasicIdentityImageCaptureTableTop() AAA tesl.size=" + tesl.size() + ", TestEventId=" + reportData.getTestEvent().getTestEventId() );
                                
            if( tesl.isEmpty() )
                return null;
            
            java.util.List<TextAndTitle> ttl = new ArrayList<>();
            List<String> cl = new ArrayList<>();
                                    
            for( TestEventScore tes : tesl )
            {
                ttl.addAll( tes.getTextBasedResponseList( null, true, true ) );
                cl.addAll( tes.getCaveatList() );
                avgScore += tes.getScore();    
                // pairsCompared += (int)(tes.getScore2() - tes.getScore4());
            }

            //LogService.logIt( "BaseCT2ReportTemplate.getBasicIdentityImageCaptureTableTop() AAA.2 ttl.size=" + ttl.size() + ", TestEventId=" + reportData.getTestEvent().getTestEventId() );
            
            if( ttl.isEmpty() )
                return null;
                                    
            avgScore = tesl.size()>1 ? avgScore/tesl.size() : avgScore;
            
            String s1,s2;
            int idx;            
            for( String c : cl )
            {
                s1 = c;
                s2="";
                idx=c.indexOf(":");
                if( idx>0 )
                {
                    s1 = c.substring(0,idx+1);
                    s2= c.substring(idx+1,c.length());
                }
                caveatList.add( new String[]{s1,s2});                
            }
            
            // UploadedUserFile uuf;
            // FileUploadFacade fileUploadFacade = FileUploadFacade.getInstance();
            // String thumbUrl;
            //String dateTimeStr;
            //boolean timeout;
            

            
            return generateIdentityImageCaptureTableTop(reportData, 
                                                        caveatList, 
                                                        avgScore, 
                                                        pageWidth );            
            
           // LogService.logIt( "Ct2ReportSettings.getBasicIdentityImageCaptureTableTop() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct2ReportSettings.getBasicIdentityImageCaptureTableTop()" );
            throw new STException( e );
        }        
    }
    
    
    public PdfPTable getBasicIdentityImageCaptureTableImages( ReportData reportData, List<TestEventScore> tesl , float pageWidth ) throws Exception
    {   
        try
        {
            // float avgScore = 0;
            // int pairsCompared = 0;
            List<UploadedUserFile> ufl = new ArrayList<>();
            List<UploadedUserFile> uflId = new ArrayList<>();
            
            //LogService.logIt( "BaseCT2ReportTemplate.getBasicIdentityImageCaptureTableImages() AAA tesl.size=" + tesl.size() + ", TestEventId=" + reportData.getTestEvent().getTestEventId() );
                                
            if( tesl.isEmpty() )
                return null;
            
            java.util.List<TextAndTitle> ttl = new ArrayList<>();
                                    
            for( TestEventScore tes : tesl )
            {
                ttl.addAll( tes.getTextBasedResponseList( null, true, true ) );
                // pairsCompared += (int)(tes.getScore2() - tes.getScore4());
            }

            //LogService.logIt( "BaseCT2ReportTemplate.getBasicIdentityImageCaptureTableImages() AAA.2 ttl.size=" + ttl.size() + ", TestEventId=" + reportData.getTestEvent().getTestEventId() );
            
            if( ttl.isEmpty() )
                return null;
                                    
            //  avgScore = tesl.size()>1 ? avgScore/tesl.size() : avgScore;
                        
            UploadedUserFile uuf;
            FileUploadFacade fileUploadFacade = FileUploadFacade.getInstance();
            String thumbUrl;
            String dateTimeStr;
            boolean timeout;
            
            for( TextAndTitle tt : ttl )
            {
                uuf = fileUploadFacade.getUploadedUserFile( tt.getUploadedUserFileId(), true );

                if( uuf==null )
                {
                    LogService.logIt( "BaseCT2ReportTemplate.getBasicIdentityImageCaptureTableImages() UploadedUserFile for uufId=" + tt.getUploadedUserFileId() + " NOT FOUND. Could be a stray Scored Response so ignoring. TestEventId=" + reportData.getTestEvent().getTestEventId() );
                    continue;
                }

                if( !uuf.getUploadedUserFileStatusType().getAvailable()  )
                {
                    LogService.logIt( "BaseCT2ReportTemplate.getBasicIdentityImageCaptureTableImages() UploadedUserFile for uufId=" + tt.getUploadedUserFileId() + " is not available (prob pseudonymized). Skipping. TestEventId=" + reportData.getTestEvent().getTestEventId() );
                    continue;
                }

                if( uuf.getFilename()!=null && !uuf.getFilename().isEmpty() )
                {
                    thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( uuf, uuf.getFilename() );                    
                    //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                    //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + uuf.getFilename();
                    //else
                    //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + uuf.getFilename();   

                    uuf.setTempStr1( thumbUrl );
                    //LogService.logIt( "BaseCT2ReportTemplate.getBasicIdentityImageCaptureTableImages() UploadedUserFile for uufId=" + tt.getUploadedUserFileId() + ", set thumbUrl to " + thumbUrl );
                }
                else
                    continue;
                
                dateTimeStr = tt.getTitle() + "\n" + tt.getText();
               
                uuf.setTempStr2(dateTimeStr);

                timeout = tt.getString1()!=null && tt.getString1().equalsIgnoreCase("true");
                uuf.setTempInt1( timeout ? 1 : 0 );                

                uuf.setTempInt2( uuf.getOrientation() );
                ufl.add(uuf);
            }

            
            return generateIdentityImageCaptureTableImages(reportData, 
                                                        ufl, 
                                                        uflId, 
                                                        pageWidth );            
            
           // LogService.logIt( "Ct2ReportSettings.getBasicIdentityImageCaptureTableImages() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct2ReportSettings.getBasicIdentityImageCaptureTableImages()" );
            throw new STException( e );
        }        
    }
    
    
    
    
    public void setRunDirection( ReportData reportData, PdfPCell c )
    {
        if( c == null || reportData == null || reportData.getLocale() == null )
            return;

        if( I18nUtils.isTextRTL( reportData.getLocale() ) )
            c.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }

    public void setRunDirection( ReportData reportData, PdfPTable t )
    {
        if( t == null || reportData == null || reportData.getLocale() == null )
            return;

        if( I18nUtils.isTextRTL( reportData.getLocale() ) )
            t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }
    










    
    
    
    public CompetencyScoreType getCompetencyScoreTypeForSimCompetency( SimJ.Simcompetency sc, SimJ simJ )
    {
        if( simJ==null || sc==null )
            return null;
        
        for( SimJ.Simlet simlet : simJ.getSimlet() )
        {
            for( SimJ.Simlet.Competencyscore cscr : simlet.getCompetencyscore() )
            {
                if( cscr.getSimcompetencyid()==sc.getId() )
                {
                    return CompetencyScoreType.getValue( cscr.getScoretype() );
                }
            }
        }
        
        // At this point it's null. This can happen if it's a combo type.
        
        // LogService.logIt( "CT2ReportSettings.getCompetencyScoreTypeForSimCompetency() Unable to find CompetencyScoreType for simCompetency=" + sc.getName() + ", Sim=" + simJ.getName() + "(" + simJ.getSimid() + " V" + simJ.getSimverid() + ")" );
        
        return null;
    }
    
    public int getFontTypeIdForLocale( Locale locale )
    {
        if( locale == null )
            return 0;

        String lang = locale.getLanguage().toLowerCase();

        if( lang.equals( "en" )|| lang.equals( "fr") || lang.equals( "de") || lang.equals( "it") || lang.equals( "pt" ) )
            return 0;


        else if( lang.equals( "he" ) || lang.equals( "ar")  || lang.equals( "es")  )
            return 1;

        else if( lang.equals( "zh" )   )
            return 2;

        else if( lang.equals( "ja" )   )
            return 3;

        else if( lang.equals( "ru")   )
            return 5;
        
        return 0;
    }

    protected Image getScoreCategoryImg(ScoreCategoryType sct, boolean detail) throws Exception {
        if (sct.red()) {
            return detail ? oneStarDetailRow : oneStarSummaryRow;
        }
        if (sct.redYellow()) {
            return detail ? twoStarsDetailRow : twoStarsSummaryRow;
        }
        if (sct.yellow()) {
            return detail ? threeStarsDetailRow : threeStarsDetailRow;
        }
        if (sct.yellowGreen()) {
            return detail ? fourStarsDetailRow : fourStarsDetailRow;
        }
        if (sct.green()) {
            return detail ? fiveStarsDetailRow : fiveStarsDetailRow;
        }

        LogService.logIt("CTReportSettings.getColorImg() No valid color scoreCategoryTypeId=" + sct.getScoreCategoryTypeId() );
        return null;
        
        // throw new Exception("CTReportSettings.getColorImg() No valid color scoreCategoryTypeId=" + sct.getScoreCategoryTypeId());
    }

    @Override
    public int getXXLFONTSZ() {
        return XXLFONTSZ;
    }

    @Override
    public void setXXLFONTSZ(int XXLFONTSZ) {
        this.XXLFONTSZ = XXLFONTSZ;
    }

    @Override
    public int getXLFONTSZ() {
        return XLFONTSZ;
    }

    @Override
    public void setXLFONTSZ(int XLFONTSZ) {
        this.XLFONTSZ = XLFONTSZ;
    }

    @Override
    public int getLFONTSZ() {
        return LFONTSZ;
    }

    @Override
    public void setLFONTSZ(int LFONTSZ) {
        this.LFONTSZ = LFONTSZ;
    }

    @Override
    public int getFONTSZ() {
        return FONTSZ;
    }

    @Override
    public void setFONTSZ(int FONTSZ) {
        this.FONTSZ = FONTSZ;
    }

    @Override
    public int getSFONTSZ() {
        return SFONTSZ;
    }

    @Override
    public void setSFONTSZ(int SFONTSZ) {
        this.SFONTSZ = SFONTSZ;
    }

    @Override
    public int getXSFONTSZ() {
        return XSFONTSZ;
    }

    @Override
    public void setXSFONTSZ(int XSFONTSZ) {
        this.XSFONTSZ = XSFONTSZ;
    }

    @Override
    public int getXXSFONTSZ() {
        return XXSFONTSZ;
    }

    @Override
    public void setXXSFONTSZ(int XXSFONTSZ) {
        this.XXSFONTSZ = XXSFONTSZ;
    }

    @Override
    public BaseFont getBaseFont() {
        return baseFont;
    }

    @Override
    public void setBaseFont(BaseFont baseFont) {
        this.baseFont = baseFont;
    }

    @Override
    public BaseFont getBaseFontCalibri() {
        return baseFontCalibri;
    }

    @Override
    public void setBaseFontCalibri(BaseFont baseFontCalibri) {
        this.baseFontCalibri = baseFontCalibri;
    }

    @Override
    public BaseFont getBaseFontCalibriBold() {
        return baseFontCalibriBold;
    }

    @Override
    public void setBaseFontCalibriBold(BaseFont baseFontCalibriBold) {
        this.baseFontCalibriBold = baseFontCalibriBold;
    }

    @Override
    public BaseFont getBaseFontCalibriItalic() {
        return baseFontCalibriItalic;
    }

    @Override
    public void setBaseFontCalibriItalic(BaseFont baseFontCalibriItalic) {
        this.baseFontCalibriItalic = baseFontCalibriItalic;
    }

    @Override
    public BaseFont getBaseFontCalibriBoldItalic() {
        return baseFontCalibriBoldItalic;
    }

    @Override
    public void setBaseFontCalibriBoldItalic(BaseFont baseFontCalibriBoldItalic) {
        this.baseFontCalibriBoldItalic = baseFontCalibriBoldItalic;
    }

    @Override
    public BaseFont getHeaderBaseFont() {
        return headerBaseFont;
    }

    @Override
    public void setHeaderBaseFont(BaseFont headerBaseFont) {
        this.headerBaseFont = headerBaseFont;
    }

    @Override
    public Font getFontXXLarge() {
        return fontXXLarge;
    }

    @Override
    public void setFontXXLarge(Font fontXXLarge) {
        this.fontXXLarge = fontXXLarge;
    }

    @Override
    public Font getFontXXLargeBoldDarkBlue() {
        return fontXXLargeBoldDarkBlue;
    }

    @Override
    public Font getFontXXLargeWhite() {
        return fontXXLargeWhite;
    }

    @Override
    public void setFontXXLargeWhite(Font fontXXLargeWhite) {
        this.fontXXLargeWhite = fontXXLargeWhite;
    }

    @Override
    public Font getFontXXLargeLight() {
        return fontXXLargeLight;
    }

    @Override
    public void setFontXXLargeLight(Font fontXXLargeLight) {
        this.fontXXLargeLight = fontXXLargeLight;
    }

    @Override
    public Font getFontXXLargeBold() {
        return fontXXLargeBold;
    }

    @Override
    public void setFontXXLargeBold(Font fontXXLargeBold) {
        this.fontXXLargeBold = fontXXLargeBold;
    }

    @Override
    public Font getFontXXLargeItalic() {
        return fontXXLargeItalic;
    }

    @Override
    public void setFontXXLargeItalic(Font fontXXLargeItalic) {
        this.fontXXLargeItalic = fontXXLargeItalic;
    }

    @Override
    public Font getFontXXLargeBoldItalic() {
        return fontXXLargeBoldItalic;
    }

    @Override
    public void setFontXXLargeBoldItalic(Font fontXXLargeBoldItalic) {
        this.fontXXLargeBoldItalic = fontXXLargeBoldItalic;
    }

    @Override
    public Font getHeaderFontXXLarge() {
        return headerFontXXLarge;
    }

    @Override
    public void setHeaderFontXXLarge(Font headerFontXXLarge) {
        this.headerFontXXLarge = headerFontXXLarge;
    }

    @Override
    public Font getHeaderFontXXLargeWhite() {
        return headerFontXXLargeWhite;
    }

    @Override
    public void setHeaderFontXXLargeWhite(Font headerFontXXLargeWhite) {
        this.headerFontXXLargeWhite = headerFontXXLargeWhite;
    }

    @Override
    public Font getFontXLarge() {
        return fontXLarge;
    }

    @Override
    public void setFontXLarge(Font fontXLarge) {
        this.fontXLarge = fontXLarge;
    }

    @Override
    public Font getFontXLargeLight() {
        return fontXLargeLight;
    }

    @Override
    public void setFontXLargeLight(Font fontXLargeLight) {
        this.fontXLargeLight = fontXLargeLight;
    }

    @Override
    public Font getFontXLargeLightBold() {
        return fontXLargeLightBold;
    }

    @Override
    public void setFontXLargeLightBold(Font fontXLargeLightBold) {
        this.fontXLargeLightBold = fontXLargeLightBold;
    }

    @Override
    public Font getFontXLargeWhite() {
        return fontXLargeWhite;
    }

    @Override
    public Font getFontXLargeBoldDarkBlue() {
        return fontXLargeBoldDarkBlue;
    }

    public Font getFontLargeBoldDarkBlue() {
        return fontLargeBoldDarkBlue;
    }

    @Override
    public void setFontXLargeWhite(Font fontXLargeWhite) {
        this.fontXLargeWhite = fontXLargeWhite;
    }

    @Override
    public Font getFontXLargeBold() {
        return fontXLargeBold;
    }

    public Font getFontXLargeBoldBlack() {
        return fontXLargeBoldBlack;
    }

    public Font getFontXLargeBoldWhite() {
        return fontXLargeBoldWhite;
    }

    public Font getFontXLargeBlack() {
        return fontXLargeBlack;
    }


    @Override
    public void setFontXLargeBold(Font fontXLargeBold) {
        this.fontXLargeBold = fontXLargeBold;
    }

    @Override
    public Font getFontXLargeItalic() {
        return fontXLargeItalic;
    }

    @Override
    public void setFontXLargeItalic(Font fontXLargeItalic) {
        this.fontXLargeItalic = fontXLargeItalic;
    }

    @Override
    public Font getFontXLargeBoldItalic() {
        return fontXLargeBoldItalic;
    }

    @Override
    public void setFontXLargeBoldItalic(Font fontXLargeBoldItalic) {
        this.fontXLargeBoldItalic = fontXLargeBoldItalic;
    }

    @Override
    public Font getHeaderFontXLarge() {
        return headerFontXLarge;
    }

    @Override
    public void setHeaderFontXLarge(Font headerFontXLarge) {
        this.headerFontXLarge = headerFontXLarge;
    }

    @Override
    public Font getHeaderFontXLargeWhite() {
        return headerFontXLargeWhite;
    }

    @Override
    public void setHeaderFontXLargeWhite(Font headerFontXLargeWhite) {
        this.headerFontXLargeWhite = headerFontXLargeWhite;
    }

    @Override
    public Font getFontLarge() {
        return fontLarge;
    }

    @Override
    public void setFontLarge(Font fontLarge) {
        this.fontLarge = fontLarge;
    }

    @Override
    public Font getFontLargeWhite() {
        return fontLargeWhite;
    }

    @Override
    public void setFontLargeWhite(Font fontLargeWhite) {
        this.fontLargeWhite = fontLargeWhite;
    }

    @Override
    public Font getFontLargeLight() {
        return fontLargeLight;
    }

    @Override
    public void setFontLargeLight(Font fontLargeLight) {
        this.fontLargeLight = fontLargeLight;
    }

    @Override
    public Font getFontLargeLightBold() {
        return fontLargeLightBold;
    }

    @Override
    public void setFontLargeLightBold(Font fontLargeLightBold) {
        this.fontLargeLightBold = fontLargeLightBold;
    }

    @Override
    public Font getFontLargeBold() {
        return fontLargeBold;
    }

    @Override
    public void setFontLargeBold(Font fontLargeBold) {
        this.fontLargeBold = fontLargeBold;
    }

    public Font getFontLargeBlueBold() {
        return fontLargeBlueBold;
    }

    public void setFontLargeBlueBold(Font fontLargeBlueBold) {
        this.fontLargeBlueBold = fontLargeBlueBold;
    }


    @Override
    public Font getFontLargeItalic() {
        return fontLargeItalic;
    }

    @Override
    public void setFontLargeItalic(Font fontLargeItalic) {
        this.fontLargeItalic = fontLargeItalic;
    }

    @Override
    public Font getFontLargeBoldItalic() {
        return fontLargeBoldItalic;
    }

    @Override
    public void setFontLargeBoldItalic(Font fontLargeBoldItalic) {
        this.fontLargeBoldItalic = fontLargeBoldItalic;
    }

    @Override
    public Font getHeaderFontLarge() {
        return headerFontLarge;
    }

    @Override
    public void setHeaderFontLarge(Font headerFontLarge) {
        this.headerFontLarge = headerFontLarge;
    }

    @Override
    public Font getHeaderFontLargeWhite() {
        return headerFontLargeWhite;
    }

    @Override
    public void setHeaderFontLargeWhite(Font headerFontLargeWhite) {
        this.headerFontLargeWhite = headerFontLargeWhite;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public Font getFontWhite() {
        return fontWhite;
    }

    @Override
    public void setFontWhite(Font fontWhite) {
        this.fontWhite = fontWhite;
    }

    @Override
    public Font getFontLight() {
        return fontLight;
    }

    @Override
    public void setFontLight(Font fontLight) {
        this.fontLight = fontLight;
    }

    @Override
    public Font getFontLightBold() {
        return fontLightBold;
    }

    @Override
    public void setFontLightBold(Font fontLightBold) {
        this.fontLightBold = fontLightBold;
    }

    @Override
    public Font getFontLightItalic() {
        return fontLightItalic;
    }

    @Override
    public void setFontLightItalic(Font fontLightItalic) {
        this.fontLightItalic = fontLightItalic;
    }

    @Override
    public Font getFontBold() {
        return fontBold;
    }

    @Override
    public void setFontBold(Font fontBold) {
        this.fontBold = fontBold;
    }

    @Override
    public Font getFontItalic() {
        return fontItalic;
    }

    @Override
    public void setFontItalic(Font fontItalic) {
        this.fontItalic = fontItalic;
    }

    @Override
    public Font getFontBoldItalic() {
        return fontBoldItalic;
    }

    @Override
    public void setFontBoldItalic(Font fontBoldItalic) {
        this.fontBoldItalic = fontBoldItalic;
    }

    @Override
    public Font getFontSmall() {
        return fontSmall;
    }

    @Override
    public void setFontSmall(Font fontSmall) {
        this.fontSmall = fontSmall;
    }

    @Override
    public Font getFontSmallWhite() {
        return fontSmallWhite;
    }

    @Override
    public void setFontSmallWhite(Font fontSmallWhite) {
        this.fontSmallWhite = fontSmallWhite;
    }

    @Override
    public Font getFontSmallLight() {
        return fontSmallLight;
    }

    @Override
    public void setFontSmallLight(Font fontSmallLight) {
        this.fontSmallLight = fontSmallLight;
    }

    @Override
    public Font getFontSmallLightBold() {
        return fontSmallLightBold;
    }

    @Override
    public void setFontSmallLightBold(Font fontSmallLightBold) {
        this.fontSmallLightBold = fontSmallLightBold;
    }

    @Override
    public Font getFontSmallLightItalic() {
        return fontSmallLightItalic;
    }

    @Override
    public void setFontSmallLightItalic(Font fontSmallLightItalic) {
        this.fontSmallLightItalic = fontSmallLightItalic;
    }

    @Override
    public Font getFontSmallBold() {
        return fontSmallBold;
    }

    @Override
    public void setFontSmallBold(Font fontSmallBold) {
        this.fontSmallBold = fontSmallBold;
    }

    @Override
    public Font getFontSmallItalic() {
        return fontSmallItalic;
    }

    @Override
    public void setFontSmallItalic(Font fontSmallItalic) {
        this.fontSmallItalic = fontSmallItalic;
    }

    @Override
    public Font getFontSmallBoldItalic() {
        return fontSmallBoldItalic;
    }

    @Override
    public void setFontSmallBoldItalic(Font fontSmallBoldItalic) {
        this.fontSmallBoldItalic = fontSmallBoldItalic;
    }

    @Override
    public Font getFontXSmall() {
        return fontXSmall;
    }

    @Override
    public void setFontXSmall(Font fontXSmall) {
        this.fontXSmall = fontXSmall;
    }

    @Override
    public Font getFontXSmallWhite() {
        return fontXSmallWhite;
    }

    @Override
    public void setFontXSmallWhite(Font fontXSmallWhite) {
        this.fontXSmallWhite = fontXSmallWhite;
    }

    @Override
    public Font getFontXSmallLight() {
        return fontXSmallLight;
    }

    @Override
    public void setFontXSmallLight(Font fontXSmallLight) {
        this.fontXSmallLight = fontXSmallLight;
    }

    @Override
    public Font getFontXSmallBold() {
        return fontXSmallBold;
    }

    @Override
    public void setFontXSmallBold(Font fontXSmallBold) {
        this.fontXSmallBold = fontXSmallBold;
    }

    @Override
    public Font getFontXSmallItalic() {
        return fontXSmallItalic;
    }

    @Override
    public void setFontXSmallItalic(Font fontXSmallItalic) {
        this.fontXSmallItalic = fontXSmallItalic;
    }

    @Override
    public Font getFontXSmallBoldItalic() {
        return fontXSmallBoldItalic;
    }

    @Override
    public void setFontXSmallBoldItalic(Font fontXSmallBoldItalic) {
        this.fontXSmallBoldItalic = fontXSmallBoldItalic;
    }

    @Override
    public Font getFontXXSmall() {
        return fontXXSmall;
    }

    @Override
    public void setFontXXSmall(Font fontXXSmall) {
        this.fontXXSmall = fontXXSmall;
    }

    @Override
    public Font getFontXXSmallWhite() {
        return fontXXSmallWhite;
    }

    @Override
    public void setFontXXSmallWhite(Font fontXXSmallWhite) {
        this.fontXXSmallWhite = fontXXSmallWhite;
    }

    @Override
    public Font getFontXXSmallLight() {
        return fontXXSmallLight;
    }

    @Override
    public void setFontXXSmallLight(Font fontXXSmallLight) {
        this.fontXXSmallLight = fontXXSmallLight;
    }

    @Override
    public Font getFontXXSmallBold() {
        return fontXXSmallBold;
    }

    @Override
    public void setFontXXSmallBold(Font fontXXSmallBold) {
        this.fontXXSmallBold = fontXXSmallBold;
    }

    @Override
    public Font getFontXXSmallItalic() {
        return fontXXSmallItalic;
    }

    @Override
    public void setFontXXSmallItalic(Font fontXXSmallItalic) {
        this.fontXXSmallItalic = fontXXSmallItalic;
    }

    @Override
    public Font getFontXXSmallBoldItalic() {
        return fontXXSmallBoldItalic;
    }

    @Override
    public void setFontXXSmallBoldItalic(Font fontXXSmallBoldItalic) {
        this.fontXXSmallBoldItalic = fontXXSmallBoldItalic;
    }

    @Override
    public Font getFontSectionTitle() {
        return fontSectionTitle;
    }

    @Override
    public void setFontSectionTitle(Font fontSectionTitle) {
        this.fontSectionTitle = fontSectionTitle;
    }

    @Override
    public BaseColor getWhiteFontColor() {
        return ct2Colors.whiteFontColor;
    }

    @Override
    public void setWhiteFontColor(BaseColor whiteFontColor) {
        ct2Colors.whiteFontColor = whiteFontColor;
    }

    @Override
    public BaseColor getDarkFontColor() {
        return ct2Colors.darkFontColor;
    }

    @Override
    public void setDarkFontColor(BaseColor darkFontColor) {
        ct2Colors.darkFontColor = darkFontColor;
    }

    @Override
    public BaseColor getLightFontColor() {
        return ct2Colors.lightFontColor;
    }

    @Override
    public void setLightFontColor(BaseColor lightFontColor) {
        ct2Colors.lightFontColor = lightFontColor;
    }

    @Override
    public BaseColor getScoreBoxHeaderBgColor() {
        return ct2Colors.scoreBoxHeaderBgColor;
    }

    @Override
    public void setScoreBoxHeaderBgColor(BaseColor scoreBoxHeaderBgColor) {
        ct2Colors.scoreBoxHeaderBgColor = scoreBoxHeaderBgColor;
    }

    @Override
    public BaseColor getScoreBoxBgColor() {
        return ct2Colors.scoreBoxBgColor;
    }

    @Override
    public void setScoreBoxBgColor(BaseColor scoreBoxBgColor) {
        ct2Colors.scoreBoxBgColor = scoreBoxBgColor;
    }

    @Override
    public BaseColor getScoreBoxBorderColor() {
        return ct2Colors.scoreBoxBorderColor;
    }

    @Override
    public void setScoreBoxBorderColor(BaseColor scoreBoxBorderColor) {
        ct2Colors.scoreBoxBorderColor = scoreBoxBorderColor;
    }

    @Override
    public BaseColor getHeaderDarkBgColor() {
        return ct2Colors.headerDarkBgColor;
    }

    @Override
    public void setHeaderDarkBgColor(BaseColor headerBgColor) {
        ct2Colors.headerDarkBgColor = headerBgColor;
    }

    @Override
    public BaseColor getTitlePageBgColor() {
        return ct2Colors.titlePageBgColor;
    }

    @Override
    public void setTitlePageBgColor(BaseColor titlePageBgColor) {
        ct2Colors.titlePageBgColor = titlePageBgColor;
    }

    @Override
    public BaseColor getPageBgColor() {
        return ct2Colors.pageBgColor;
    }

    @Override
    public void setPageBgColor(BaseColor pageBgColor) {
        ct2Colors.pageBgColor = pageBgColor;
    }

    @Override
    public BaseColor getHraBaseReportColor() {
        return ct2Colors.hraBaseReportColor;
    }

    @Override
    public void setHraBaseReportColor(BaseColor c) {
        ct2Colors.hraBaseReportColor = c;
    }

    @Override
    public BaseColor getTablePageBgColor() {
        return ct2Colors.tablePageBgColor;
    }

    @Override
    public void setTablePageBgColor(BaseColor c) {
        ct2Colors.tablePageBgColor = c;
    }

    @Override
    public BaseColor getRedShadeColor() {
        return ct2Colors.redShadeColor;
    }

    @Override
    public void setRedShadeColor(BaseColor redShadeColor) {
        ct2Colors.redShadeColor = redShadeColor;
    }

    @Override
    public BaseColor getBarGraphCoreShade1() {
        return BaseColor.WHITE;
    }

    @Override
    public void setBarGraphCoreShade1(BaseColor o) {
    }

    @Override
    public BaseColor getBarGraphCoreShade2() {
        return BaseColor.WHITE;
    }

    @Override
    public void setBarGraphCoreShade2(BaseColor o) {
    }


    @Override
    public Image getRedDot() {
        return oneStarDetailRow;
    }

    @Override
    public void setRedDot(Image redDot) {
        this.oneStarDetailRow = redDot;
    }

    @Override
    public Image getRedYellowDot() {
        return twoStarsDetailRow;
    }

    @Override
    public void setRedYellowDot(Image redYellowDot) {
        this.twoStarsDetailRow = redYellowDot;
    }

    @Override
    public Image getYellowDot() {
        return threeStarsDetailRow;
    }

    @Override
    public void setYellowDot(Image yellowDot) {
        this.threeStarsDetailRow = yellowDot;
    }

    @Override
    public Image getYellowGreenDot() {
        return fourStarsDetailRow;
    }

    @Override
    public void setYellowGreenDot(Image yellowGreenDot) {
        this.fourStarsDetailRow = yellowGreenDot;
    }

    @Override
    public Image getGreenDot() {
        return fiveStarsDetailRow;
    }

    @Override
    public void setGreenDot(Image greenDot) {
        this.fiveStarsDetailRow = greenDot;
    }

    @Override
    public Image getHraLogoBlackText() {
        return hraLogoBlackText;
    }


    public Image getHraCoverImage() {
        return this.hraCoverPageImage;
    }

    public Image getHraCoverImage2() {
        return this.hraCoverPageImage2;
    }
    
    
    @Override
    public void setHraLogoBlackText(Image hraLogoBlackText) {
        this.hraLogoBlackText = hraLogoBlackText;
    }

    @Override
    public Image getHraLogoBlackTextSmall() {
        return hraLogoBlackTextSmall;
    }

    @Override
    public void setHraLogoBlackTextSmall(Image hraLogoBlackTextSmall) {
        this.hraLogoBlackTextSmall = hraLogoBlackTextSmall;
    }

    @Override
    public Image getHraLogoWhiteText() {
        return hraLogoWhiteText;
    }

    @Override
    public void setHraLogoWhiteText(Image hraLogoWhiteText) {
        this.hraLogoWhiteText = hraLogoWhiteText;
    }

    @Override
    public Image getHraLogoWhiteTextSmall() {
        
        //if( hraLogoWhiteTextSmall==null )
        //    LogService.logIt( "CT2ReportSettings.getHraLogoWhiteTextSmall() hraLogoWhiteTextSmall is null!" );
        
        return hraLogoWhiteTextSmall;
    }

    @Override
    public void setHraLogoWhiteTextSmall(Image hraLogoWhiteTextSmall) {
        this.hraLogoWhiteTextSmall = hraLogoWhiteTextSmall;
    }

    public String getBaseImageUrl() {
        return RuntimeConstants.getStringValue("baseurl") + "/resources/images/coretest2";
    }

    
    public URL getLocalImageUrl(String fn) {
        return com.tm2score.util.HttpUtils.getURLFromString( getBaseImageUrl() + "/" + fn);
    }

    @Override
    public Image getRainbowBar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRainbowBar(Image rainbowBar) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getGreenCatColor1() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getGreenCatColor2() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getRedCatColor1() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getRedCatColor2() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getRedYellowCatColor1() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getRedYellowCatColor2() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getYellowCatColor1() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getYellowCatColor2() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getYellowGreenCatColor1() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BaseColor getYellowGreenCatColor2() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGreenCatColor1(BaseColor greenCatColor1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setGreenCatColor2(BaseColor greenCatColor2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRedCatColor1(BaseColor redCatColor1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRedCatColor2(BaseColor redCatColor2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRedYellowCatColor1(BaseColor redYellowCatColor1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setRedYellowCatColor2(BaseColor redYellowCatColor2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setYellowCatColor1(BaseColor yellowCatColor1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setYellowCatColor2(BaseColor yellowCatColor2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setYellowGreenCatColor1(BaseColor yellowGreenCatColor1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setYellowGreenCatColor2(BaseColor yellowGreenCatColor2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Font getFontRed() {
        return fontRed;
    }
    
    public Font getFontBoldRed() {
        return fontBoldRed;
    }

    public void setFontBoldRed(Font fontBoldRed) {
        this.fontBoldRed = fontBoldRed;
    }

}
