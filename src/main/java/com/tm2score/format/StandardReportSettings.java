/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.report.ReportSettings;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportData;

/**
 *
 * @author Mike
 */
public class StandardReportSettings implements ReportSettings
{
    public static int MAX_INTERVIEWQS_PER_COMPETENCY = 10;
    public static int MAX_TABLE_CELL_HEIGHT = 5;

    public static float MAX_CUSTLOGO_W = 80;
    public static float MAX_CUSTLOGO_H = 40;
    
    public static String DOCUMENT_JS_API = "this.disclosed=true;\n" +
"function openUrl( url, target )\n" +
"{\n" +
"  if( this.external && this.hostContainer )\n" +
"  {\n" +
"    try\n" +
"    {\n" +
"      this.hostContainer.window.open( url, target );\n" +
"      return true;\n" +
"    }\n" +
"    catch(e)\n" +
"    {\n" +
"      console.show();\n" +
"      console.println( e.toString() );\n" +
"    }  \n" +
"  }\n" +
"  return false;\n" +
"}";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NOTE: NONE OF THESE ARE PUBLIC SO THAT OTHER REPORTS CAN OVERRIDE THEM FOR THAT REPORT but still extend from this class.
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // int MIN_COUNT_FOR_PERCENTILE = 10;

    int XXLFONTSZ = 28;
    int XLFONTSZ = 14;
    int LFONTSZ = 11;
    int FONTSZ = 10;
    int SFONTSZ = 8;
    int XSFONTSZ = 7;
    int XXSFONTSZ = 6;

    BaseFont baseFont;
    BaseFont baseFontCalibri;
    BaseFont baseFontCalibriBold;
    BaseFont baseFontCalibriItalic;
    BaseFont baseFontCalibriBoldItalic;

    BaseFont headerBaseFont;

    Font fontXXLarge;
    Font fontXXLargeWhite;
    Font fontXXLargeLight;
    Font fontXXLargeBold;
    Font fontXXLargeItalic;
    Font fontXXLargeBoldItalic;

    Font headerFontXXLarge;
    Font headerFontXXLargeWhite;

    Font fontXLarge;
    Font fontXLargeLight;
    Font fontXLargeLightBold;
    Font fontXLargeWhite;
    Font fontXLargeBold;
    Font fontXLargeItalic;
    Font fontXLargeBoldItalic;

    Font headerFontXLarge;
    Font headerFontXLargeWhite;

    Font fontLarge;
    Font fontLargeWhite;
    Font fontLargeLight;
    Font fontLargeLightBold;
    Font fontLargeBold;
    Font fontLargeItalic;
    Font fontLargeBoldItalic;

    Font headerFontLarge;
    Font headerFontLargeWhite;

    Font font;
    Font fontWhite;
    Font fontLight;
    Font fontLightBold;
    Font fontLightItalic;
    Font fontBold;
    Font fontItalic;
    Font fontBoldItalic;

    Font fontSmall;
    Font fontSmallWhite;
    Font fontSmallLight;
    Font fontSmallLightBold;
    Font fontSmallLightItalic;
    Font fontSmallBold;
    Font fontSmallItalic;
    Font fontSmallBoldItalic;

    Font fontXSmall;
    Font fontXSmallWhite;
    Font fontXSmallLight;
    Font fontXSmallBold;
    Font fontXSmallItalic;
    Font fontXSmallBoldItalic;

    Font fontXXSmall;
    Font fontXXSmallWhite;
    Font fontXXSmallLight;
    Font fontXXSmallBold;
    Font fontXXSmallItalic;
    Font fontXXSmallBoldItalic;

    Font fontSectionTitle;

    BaseColor whiteFontColor;  // #ffffff
    BaseColor darkFontColor;   // #282828
    BaseColor lightFontColor;  // #525252


    BaseColor scoreBoxHeaderBgColor;  // #e9e9e9
    BaseColor scoreBoxBgColor;  // #ffffff
    BaseColor scoreBoxBorderColor;  // #525252

    BaseColor headerDarkBgColor;    // #3a3a3a
    BaseColor titlePageBgColor; // #ffffff
    BaseColor pageBgColor;      // #eaeaea
    BaseColor hraBaseReportColor;   // #f1592a
    BaseColor tablePageBgColor; // #ffffff
    BaseColor redShadeColor;

    BaseColor barGraphCoreShade1 = new BaseColor( 0x17, 0xb4, 0xee ); // f68d2f // new BaseColor( 0xf6, 0x8d, 0x2f ); // f68d2f
    BaseColor barGraphCoreShade2 = new BaseColor( 0x17, 0xb4, 0xee ); // f68d2f // new BaseColor( 0xfc, 0xab, 0x63 ); // fcab63

    BaseColor redCatColor1 = new BaseColor( 0xff , 0x00, 0x00);   // ff0000
    BaseColor redCatColor2 = new BaseColor( 0xff , 0x3c, 0x3c);  // ff3c3c
    BaseColor redYellowCatColor1 = new BaseColor( 0xf7 , 0x94, 0x1e);//  f7941e
    // BaseColor redYellowCatColor2 = new BaseColor( 0xf8 , 0xa9, 0x4b);// f8a94b
    BaseColor redYellowCatColor2 = new BaseColor( 0xff , 0xff, 0x00);// f8a94b
    BaseColor yellowCatColor1 = new BaseColor( 0xff , 0xf2, 0x00);//  fff200
    BaseColor yellowCatColor2 = new BaseColor( 0xff , 0xff, 0x00);// ffff00
    // BaseColor yellowCatColor2 = new BaseColor( 0xfb , 0xf3, 0x5a);// fbf35a
    BaseColor yellowGreenCatColor1 = new BaseColor( 0x8c , 0xc6, 0x3f);// 8cc63f
    // BaseColor yellowGreenCatColor2 = new BaseColor( 0xba , 0xee, 0x76);// baee76
    BaseColor yellowGreenCatColor2 = new BaseColor( 0xff , 0xff, 0x00);// baee76
    BaseColor greenCatColor1 = new BaseColor( 0x37 , 0xa8, 0x66);  // 37a866
    BaseColor greenCatColor2 = new BaseColor( 0x00 , 0xff, 0x00);  // 0b9444


    Image redDot;
    Image redYellowDot;
    Image yellowDot;
    Image yellowGreenDot;
    Image greenDot;

    Image hraLogoBlackText;
    Image hraLogoBlackTextSmall;
    Image hraLogoWhiteText;
    Image hraLogoWhiteTextSmall;
    Image rainbowBar;


    protected BaseColor[] getColorsForScoreCategoryType( ScoreCategoryType sct )
    {
        BaseColor[] out = new BaseColor[2];

        if( sct.red() )
        {
            out[0] = redCatColor1;
            out[1] = redCatColor2;
        }
        else if( sct.redYellow() )
        {
            out[0] = redYellowCatColor1;
            out[1] = redYellowCatColor2;
        }
        else if( sct.yellow() )
        {
            out[0] = yellowCatColor1;
            out[1] = yellowCatColor2;
        }
        else if( sct.yellowGreen() )
        {
            out[0] = yellowGreenCatColor1;
            out[1] = yellowGreenCatColor2;
        }
        else if( sct.green() )
        {
            out[0] = greenCatColor1;
            out[1] = greenCatColor2;
        }

        return out;
    }

    @Override
    public void initSettings( ReportData reportData ) throws Exception
    {
        if( baseFont == null )
        {
            String filesRoot = RuntimeConstants.getStringValue( "filesroot" ) + "/coretest/fonts/";

            baseFont = BaseFont.createFont( BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED );
            baseFontCalibri = baseFont; // BaseFont.createFont( filesRoot + "calibri.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED );
            baseFontCalibriBold = baseFont; // BaseFont.createFont( filesRoot + "calibrib.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED );
            baseFontCalibriItalic = baseFont; // BaseFont.createFont( filesRoot + "calibrii.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED );
            baseFontCalibriBoldItalic = baseFont; // BaseFont.createFont( filesRoot + "calibriz.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED );

            headerBaseFont = BaseFont.createFont( filesRoot + "BNKGOTHM.TTF", BaseFont.WINANSI, BaseFont.EMBEDDED );

            fontXXLarge = new Font( baseFontCalibri, XXLFONTSZ );
            fontXXLargeWhite = new Font( baseFontCalibri, XXLFONTSZ );
            fontXXLargeLight = new Font( baseFontCalibri, XXLFONTSZ );
            fontXXLargeBold = new Font( baseFontCalibriBold, XXLFONTSZ, Font.BOLD );
            fontXXLargeItalic = new Font( baseFontCalibriItalic, XXLFONTSZ, Font.ITALIC );
            fontXXLargeBoldItalic = new Font( baseFontCalibriBoldItalic, XXLFONTSZ, Font.BOLDITALIC );

            fontXLarge = new Font( baseFontCalibri, XLFONTSZ );
            fontXLargeWhite = new Font( baseFontCalibri, XLFONTSZ );
            fontXLargeLight = new Font( baseFontCalibri, XLFONTSZ );
            fontXLargeLightBold = new Font( baseFontCalibriBold, XLFONTSZ );
            fontXLargeBold = new Font( baseFontCalibriBold, XLFONTSZ, Font.BOLD );
            fontXLargeItalic = new Font( baseFontCalibriItalic, XLFONTSZ, Font.ITALIC );
            fontXLargeBoldItalic = new Font( baseFontCalibriBoldItalic, XLFONTSZ, Font.BOLDITALIC );

            fontLarge = new Font( baseFontCalibri, LFONTSZ );
            fontLargeWhite = new Font( baseFontCalibri, LFONTSZ );
            fontLargeLight = new Font( baseFontCalibri, LFONTSZ );
            fontLargeLightBold = new Font( baseFontCalibriBold, LFONTSZ );
            fontLargeBold = new Font( baseFontCalibriBold, LFONTSZ, Font.BOLD );
            fontLargeItalic = new Font( baseFontCalibriItalic, LFONTSZ, Font.ITALIC );
            fontLargeBoldItalic = new Font( baseFontCalibriBoldItalic, LFONTSZ, Font.BOLDITALIC );

            fontSectionTitle = fontLargeLight;

            font = new Font( baseFontCalibri, FONTSZ );
            fontWhite = new Font( baseFontCalibri, FONTSZ );
            fontLight = new Font( baseFontCalibri, FONTSZ );
            fontLightBold = new Font( baseFontCalibriBold, FONTSZ );
            fontLightItalic = new Font( baseFontCalibriItalic, FONTSZ );
            fontBold = new Font( baseFontCalibriBold, FONTSZ, Font.BOLD );
            fontItalic = new Font( baseFontCalibriItalic, FONTSZ, Font.ITALIC );
            fontBoldItalic = new Font( baseFontCalibriBoldItalic, FONTSZ, Font.BOLDITALIC );

            fontSmall = new Font( baseFontCalibri, SFONTSZ );
            fontSmallWhite = new Font( baseFontCalibri, SFONTSZ );
            fontSmallLight = new Font( baseFontCalibri, SFONTSZ );
            fontSmallLightBold = new Font( baseFontCalibriBold, XSFONTSZ );
            fontSmallLightItalic = new Font( baseFontCalibriItalic, XSFONTSZ );
            fontSmallBold = new Font( baseFontCalibriBold, SFONTSZ, Font.BOLD );
            fontSmallItalic = new Font( baseFontCalibriItalic, SFONTSZ, Font.ITALIC );
            fontSmallBoldItalic = new Font( baseFontCalibriBoldItalic, SFONTSZ, Font.BOLDITALIC );

            fontXSmall = new Font( baseFontCalibri, XSFONTSZ );
            fontXSmallWhite = new Font( baseFontCalibri, XSFONTSZ );
            fontXSmallLight = new Font( baseFontCalibri, XSFONTSZ );
            fontXSmallBold = new Font( baseFontCalibriBold, XSFONTSZ, Font.BOLD );
            fontXSmallItalic = new Font( baseFontCalibriItalic, XSFONTSZ, Font.ITALIC );
            fontXSmallBoldItalic = new Font( baseFontCalibriBoldItalic, XSFONTSZ, Font.BOLDITALIC );

            fontXXSmall = new Font( baseFontCalibri, XXSFONTSZ );
            fontXXSmallWhite = new Font( baseFontCalibri, XXSFONTSZ );
            fontXXSmallLight = new Font( baseFontCalibri, XXSFONTSZ );
            fontXXSmallBold = new Font( baseFontCalibriBold, XXSFONTSZ, Font.BOLD );
            fontXXSmallItalic = new Font( baseFontCalibriItalic, XXSFONTSZ, Font.ITALIC );
            fontXXSmallBoldItalic = new Font( baseFontCalibriBoldItalic, XXSFONTSZ, Font.BOLDITALIC );

            headerFontXXLarge = new Font( headerBaseFont, XXLFONTSZ );
            headerFontXXLargeWhite = new Font( headerBaseFont, XXLFONTSZ );
            headerFontXLarge = new Font( headerBaseFont, XLFONTSZ );
            headerFontXLargeWhite = new Font( headerBaseFont, XLFONTSZ );
            headerFontLarge = new Font( headerBaseFont, LFONTSZ );
            headerFontLargeWhite = new Font( headerBaseFont, LFONTSZ );

            whiteFontColor = new BaseColor( 255,255,255 );  // #ffffff
            darkFontColor = new BaseColor( 40,40,40 );   // #282828
            lightFontColor = new BaseColor( 82,82,82 );  // #525252

            scoreBoxHeaderBgColor = new BaseColor( 233,233,233 );  // #e9e9e9
            scoreBoxBgColor = new BaseColor( 255,255,255 );  // #ffffff
            scoreBoxBorderColor = new BaseColor( 82,82,82 );  // #525252

            headerDarkBgColor =  new BaseColor(33, 150, 243); // new BaseColor(39,178,231); // #27b2e7 // new BaseColor( 58,58,58 );    // #3a3a3a
            titlePageBgColor = new BaseColor( 255,255,255 ); // #ffffff
            pageBgColor = new BaseColor( 234,234,234 );      // #eaeaea
            hraBaseReportColor =  new BaseColor(33, 150, 243); // = new BaseColor( 39,178,231 ); // #27b2e7   //   new BaseColor( 241,90,41 );   // #f1592a
            tablePageBgColor = new BaseColor( 0xf9, 0xf9, 0xf9 );
            redShadeColor = new BaseColor( 0xf0, 0x80, 0x80 );

            BaseColor baseFontColor = darkFontColor;

            fontXXLarge.setColor( baseFontColor  );
            fontXXLargeWhite.setColor( whiteFontColor  );
            fontXXLargeLight.setColor( lightFontColor  );
            fontXXLargeBold.setColor( baseFontColor  );
            fontXXLargeItalic.setColor( baseFontColor  );
            fontXXLargeBoldItalic.setColor( baseFontColor  );

            fontXLarge.setColor( baseFontColor  );
            fontXLargeWhite.setColor( whiteFontColor  );
            fontXLargeLight.setColor( lightFontColor  );
            fontXLargeLightBold.setColor( lightFontColor  );
            fontXLargeBold.setColor( baseFontColor  );
            fontXLargeItalic.setColor( baseFontColor  );
            fontXLargeBoldItalic.setColor( baseFontColor  );

            fontLarge.setColor( baseFontColor  );
            fontLargeWhite.setColor( whiteFontColor  );
            fontLargeLight.setColor( lightFontColor  );
            fontLargeLightBold.setColor( lightFontColor  );
            fontLargeBold.setColor( baseFontColor  );
            fontLargeItalic.setColor( baseFontColor  );
            fontLargeBoldItalic.setColor( baseFontColor  );

            font.setColor( baseFontColor  );
            fontWhite.setColor( whiteFontColor  );
            fontLight.setColor( lightFontColor  );
            fontLightBold.setColor( lightFontColor  );
            fontLightItalic.setColor( lightFontColor  );
            fontBold.setColor( baseFontColor  );
            fontItalic.setColor( baseFontColor  );
            fontBoldItalic.setColor( baseFontColor  );

            fontSmall.setColor( baseFontColor  );
            fontSmallWhite.setColor( whiteFontColor  );
            fontSmallLight.setColor( lightFontColor  );
            fontSmallLightBold.setColor( lightFontColor  );
            fontSmallLightItalic.setColor( lightFontColor  );
            fontSmallBold.setColor( baseFontColor  );
            fontSmallItalic.setColor( baseFontColor  );
            fontSmallBoldItalic.setColor( baseFontColor  );

            fontXSmall.setColor( baseFontColor  );
            fontXSmallWhite.setColor( whiteFontColor  );
            fontXSmallLight.setColor( lightFontColor  );
            fontXSmallBold.setColor( baseFontColor  );
            fontXSmallItalic.setColor( baseFontColor  );
            fontXSmallBoldItalic.setColor( baseFontColor  );

            fontXXSmall.setColor( baseFontColor  );
            fontXXSmallWhite.setColor( whiteFontColor  );
            fontXXSmallLight.setColor( lightFontColor  );
            fontXXSmallBold.setColor( baseFontColor  );
            fontXXSmallItalic.setColor( baseFontColor  );
            fontXXSmallBoldItalic.setColor( baseFontColor  );


            headerFontXXLarge.setColor( baseFontColor  );
            headerFontXLarge.setColor( baseFontColor  );
            headerFontLarge.setColor( baseFontColor  );
            headerFontXXLargeWhite.setColor( whiteFontColor  );
            headerFontXLargeWhite.setColor( whiteFontColor  );
            headerFontLargeWhite.setColor( whiteFontColor  );


            redDot = ITextUtils.getITextImage( reportData.getRedDotUrl());
            redYellowDot = ITextUtils.getITextImage( reportData.getRedYellowDotUrl());
            yellowDot = ITextUtils.getITextImage( reportData.getYellowDotUrl());
            yellowGreenDot = ITextUtils.getITextImage( reportData.getYellowGreenDotUrl());
            greenDot = ITextUtils.getITextImage( reportData.getGreenDotUrl());

            hraLogoBlackText = ITextUtils.getITextImage( reportData.getHRALogoBlackTextUrl() );
            hraLogoWhiteText = ITextUtils.getITextImage( reportData.getHRALogoWhiteTextUrl() );
            hraLogoBlackTextSmall= ITextUtils.getITextImage( reportData.getHRALogoBlackTextSmallUrl() );
            hraLogoWhiteTextSmall= ITextUtils.getITextImage( reportData.getHRALogoWhiteTextSmallUrl() );
            rainbowBar = ITextUtils.getITextImage( reportData.getRainbowBarUrl() );

            float highresscale = 100*72/300;

            // float dw = redDot.getScaledWidth();

            float dotScale = 28;// highresscale; // dw > 40 ? 40/dw : dw/40;


            // float whiteAdj = 0.5f;

            hraLogoBlackText.scalePercent(highresscale );
            hraLogoWhiteText.scalePercent( highresscale);
            hraLogoBlackTextSmall.scalePercent( highresscale);
            hraLogoWhiteTextSmall.scalePercent( highresscale );
            // rainbowBar.scalePercent( highresscale);


            redDot.scalePercent( dotScale );
            redYellowDot.scalePercent(dotScale );
            yellowDot.scalePercent(dotScale );
            yellowGreenDot.scalePercent(dotScale );
            greenDot.scalePercent(dotScale );
        }
    }


    public boolean isValidForTestEvent()
    {
        return true;
    }
    
    
    protected Image getColorImg( ScoreCategoryType sct ) throws Exception
    {
        if( sct.red() )
            return redDot;
        if( sct.redYellow() )
            return redYellowDot;
        if( sct.yellow() )
            return yellowDot;
        if( sct.yellowGreen() )
            return yellowGreenDot;
        if( sct.green() )
            return greenDot;

        throw new Exception( "CTReportSettings.getColorImg() No valid color scoreCategoryTypeId=" + sct.getScoreCategoryTypeId() );
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
    public void setFontXLargeWhite(Font fontXLargeWhite) {
        this.fontXLargeWhite = fontXLargeWhite;
    }

    @Override
    public Font getFontXLargeBold() {
        return fontXLargeBold;
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
        return whiteFontColor;
    }

    @Override
    public void setWhiteFontColor(BaseColor whiteFontColor) {
        this.whiteFontColor = whiteFontColor;
    }

    @Override
    public BaseColor getDarkFontColor() {
        return darkFontColor;
    }

    @Override
    public void setDarkFontColor(BaseColor darkFontColor) {
        this.darkFontColor = darkFontColor;
    }

    @Override
    public BaseColor getLightFontColor() {
        return lightFontColor;
    }

    @Override
    public void setLightFontColor(BaseColor lightFontColor) {
        this.lightFontColor = lightFontColor;
    }

    @Override
    public BaseColor getScoreBoxHeaderBgColor() {
        return scoreBoxHeaderBgColor;
    }

    @Override
    public void setScoreBoxHeaderBgColor(BaseColor scoreBoxHeaderBgColor) {
        this.scoreBoxHeaderBgColor = scoreBoxHeaderBgColor;
    }

    @Override
    public BaseColor getScoreBoxBgColor() {
        return scoreBoxBgColor;
    }

    @Override
    public void setScoreBoxBgColor(BaseColor scoreBoxBgColor) {
        this.scoreBoxBgColor = scoreBoxBgColor;
    }

    @Override
    public BaseColor getScoreBoxBorderColor() {
        return scoreBoxBorderColor;
    }

    @Override
    public void setScoreBoxBorderColor(BaseColor scoreBoxBorderColor) {
        this.scoreBoxBorderColor = scoreBoxBorderColor;
    }

    @Override
    public BaseColor getHeaderDarkBgColor() {
        return headerDarkBgColor;
    }

    @Override
    public void setHeaderDarkBgColor(BaseColor headerBgColor) {
        this.headerDarkBgColor = headerBgColor;
    }

    @Override
    public BaseColor getTitlePageBgColor() {
        return titlePageBgColor;
    }

    @Override
    public void setTitlePageBgColor(BaseColor titlePageBgColor) {
        this.titlePageBgColor = titlePageBgColor;
    }

    @Override
    public BaseColor getPageBgColor() {
        return pageBgColor;
    }

    @Override
    public void setPageBgColor(BaseColor pageBgColor) {
        this.pageBgColor = pageBgColor;
    }

    @Override
    public BaseColor getHraBaseReportColor() {
        return hraBaseReportColor;
    }

    @Override
    public void setHraBaseReportColor(BaseColor hraOrangeColor) {
        this.hraBaseReportColor = hraOrangeColor;
    }

    @Override
    public BaseColor getTablePageBgColor() {
        return tablePageBgColor;
    }

    @Override
    public void setTablePageBgColor(BaseColor tablePageBgColor) {
        this.tablePageBgColor = tablePageBgColor;
    }

    @Override
    public BaseColor getRedShadeColor() {
        return redShadeColor;
    }

    @Override
    public void setRedShadeColor(BaseColor redShadeColor) {
        this.redShadeColor = redShadeColor;
    }

    @Override
    public BaseColor getBarGraphCoreShade1() {
        return barGraphCoreShade1;
    }

    @Override
    public void setBarGraphCoreShade1(BaseColor o) {
        this.barGraphCoreShade1 = o;
    }

    @Override
    public BaseColor getBarGraphCoreShade2() {
        return barGraphCoreShade2;
    }

    @Override
    public void setBarGraphCoreShade2(BaseColor o) {
        this.barGraphCoreShade2 = o;
    }

    @Override
    public BaseColor getRedCatColor1() {
        return redCatColor1;
    }

    @Override
    public void setRedCatColor1(BaseColor redCatColor1) {
        this.redCatColor1 = redCatColor1;
    }

    @Override
    public BaseColor getRedCatColor2() {
        return redCatColor2;
    }

    @Override
    public void setRedCatColor2(BaseColor redCatColor2) {
        this.redCatColor2 = redCatColor2;
    }

    @Override
    public BaseColor getRedYellowCatColor1() {
        return redYellowCatColor1;
    }

    @Override
    public void setRedYellowCatColor1(BaseColor redYellowCatColor1) {
        this.redYellowCatColor1 = redYellowCatColor1;
    }

    @Override
    public BaseColor getRedYellowCatColor2() {
        return redYellowCatColor2;
    }

    @Override
    public void setRedYellowCatColor2(BaseColor redYellowCatColor2) {
        this.redYellowCatColor2 = redYellowCatColor2;
    }

    @Override
    public BaseColor getYellowCatColor1() {
        return yellowCatColor1;
    }

    @Override
    public void setYellowCatColor1(BaseColor yellowCatColor1) {
        this.yellowCatColor1 = yellowCatColor1;
    }

    @Override
    public BaseColor getYellowCatColor2() {
        return yellowCatColor2;
    }

    @Override
    public void setYellowCatColor2(BaseColor yellowCatColor2) {
        this.yellowCatColor2 = yellowCatColor2;
    }

    @Override
    public BaseColor getYellowGreenCatColor1() {
        return yellowGreenCatColor1;
    }

    @Override
    public void setYellowGreenCatColor1(BaseColor yellowGreenCatColor1) {
        this.yellowGreenCatColor1 = yellowGreenCatColor1;
    }

    @Override
    public BaseColor getYellowGreenCatColor2() {
        return yellowGreenCatColor2;
    }

    @Override
    public void setYellowGreenCatColor2(BaseColor yellowGreenCatColor2) {
        this.yellowGreenCatColor2 = yellowGreenCatColor2;
    }

    @Override
    public BaseColor getGreenCatColor1() {
        return greenCatColor1;
    }

    @Override
    public void setGreenCatColor1(BaseColor greenCatColor1) {
        this.greenCatColor1 = greenCatColor1;
    }

    @Override
    public BaseColor getGreenCatColor2() {
        return greenCatColor2;
    }

    @Override
    public void setGreenCatColor2(BaseColor greenCatColor2) {
        this.greenCatColor2 = greenCatColor2;
    }

    @Override
    public Image getRedDot() {
        return redDot;
    }

    @Override
    public void setRedDot(Image redDot) {
        this.redDot = redDot;
    }

    @Override
    public Image getRedYellowDot() {
        return redYellowDot;
    }

    @Override
    public void setRedYellowDot(Image redYellowDot) {
        this.redYellowDot = redYellowDot;
    }

    @Override
    public Image getYellowDot() {
        return yellowDot;
    }

    @Override
    public void setYellowDot(Image yellowDot) {
        this.yellowDot = yellowDot;
    }

    @Override
    public Image getYellowGreenDot() {
        return yellowGreenDot;
    }

    @Override
    public void setYellowGreenDot(Image yellowGreenDot) {
        this.yellowGreenDot = yellowGreenDot;
    }

    @Override
    public Image getGreenDot() {
        return greenDot;
    }

    @Override
    public void setGreenDot(Image greenDot) {
        this.greenDot = greenDot;
    }

    @Override
    public Image getHraLogoBlackText() {
        return hraLogoBlackText;
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
        return hraLogoWhiteTextSmall;
    }

    @Override
    public void setHraLogoWhiteTextSmall(Image hraLogoWhiteTextSmall) {
        this.hraLogoWhiteTextSmall = hraLogoWhiteTextSmall;
    }

    @Override
    public Image getRainbowBar() {
        return rainbowBar;
    }

    @Override
    public void setRainbowBar(Image rainbowBar) {
        this.rainbowBar = rainbowBar;
    }


}
