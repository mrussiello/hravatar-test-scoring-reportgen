/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;

/**
 *
 * @author miker_000
 */
public class CT2Colors 
{
    public  BaseColor gray = null;
    public  BaseColor lightergray = null;
    public  BaseColor blue = null;
    public  BaseColor  scoreWhite = null;
    public  BaseColor  green = null;
    public  BaseColor  yellowgreen = null;
    public  BaseColor yellow = null;
    public  BaseColor  redyellow = null;
    public  BaseColor red = null;
    public  BaseColor  scoreBlack = null;
    public  BaseColor profileBlue = null;
    public  BaseColor markerBlack  = null;
    
    
    public BaseColor whiteFontColor;  // #ffffff
    public BaseColor darkFontColor;   // #282828
    public BaseColor lightFontColor;  // #525252

    
    public BaseColor scoreBoxHeaderBgColor;  // #e9e9e9
    public BaseColor scoreBoxShadeBgColor;  // #e9e9e9
    public BaseColor scoreBoxBgColor;  // #ffffff
    public BaseColor scoreBoxBorderColor;  // #525252
    public float scoreBoxBorderWidth = 0.8f;

    public BaseColor lightBoxBorderColor;  // #eaeaea
    public BaseColor lighterBoxBorderColor;  // #f4f4f4
    
    public BaseColor headerDarkBgColor;    // #3a3a3a
    public BaseColor titlePageBgColor; // #ffffff
    public BaseColor pageBgColor;      // #eaeaea
    public BaseColor hraBaseReportColor;   // #f1592a
    public BaseColor tablePageBgColor; // #ffffff
    public BaseColor redShadeColor;
    public BaseColor keyBackgroundColor = new BaseColor( 0xe6, 0xe6, 0xe6 ); // e6e6e6    
    
    // public BaseColor hraBlue = new BaseColor( 0x21, 0x96, 0xf3 );
    public BaseColor hraBlue = new BaseColor( 0x00, 0x77, 0xcc );

    // public BaseColor barGraphCoreShade1 = new BaseColor( 0x21, 0x96, 0xf3 );
    public BaseColor barGraphCoreShade1 = new BaseColor( 0x00, 0x77, 0xcc );

    public BaseColor barGraphCoreShade2 =  new BaseColor( 0x51, 0xb1, 0xfa );// new BaseColor( 0xab, 0xe7, 0xff );
    
    
    
    
    public static CT2Colors getCt2Colors( boolean dev )
    {
        CT2Colors ctc = new CT2Colors();
        
        if( 1==1 )
            dev=false;
        
        if( dev )
        {
            // change to purple
            // ctc.hraBlue =  new BaseColor( 0xa2, 0x30, 0x99 );// new BaseColor( 0xc9, 0x6b, 0xe1 ); // new BaseColor( 0x27, 0xb2, 0xe7 ); // c96be1
            ctc.hraBlue =  new BaseColor( 0x0, 0x67, 0xb6 );// new BaseColor( 0xc9, 0x6b, 0xe1 ); // new BaseColor( 0x27, 0xb2, 0xe7 ); // c96be1
            
        }
        
        ctc.whiteFontColor = BaseColor.WHITE;  // #ffffff
        ctc.darkFontColor = new BaseColor(0x4d,0x4d,0x4d);   // #4d4d4d
        ctc.lightFontColor = new BaseColor(0x80,0x80,0x80);  // #525252

        ctc.scoreBoxHeaderBgColor = ctc.hraBlue;  // #e9e9e9
        ctc.scoreBoxBgColor = BaseColor.WHITE;  // #ffffff
        ctc.scoreBoxBorderColor = new BaseColor( 0x92, 0x92, 0x92);  // #525252
        ctc.scoreBoxShadeBgColor = new BaseColor( 0xea, 0xea, 0xea ); // new BaseColor( 0xca, 0xe4, 0xee );

        ctc.headerDarkBgColor = ctc.hraBlue; //  new BaseColor(39, 178, 231); // #27b2e7 // new BaseColor( 58,58,58 );    // #3a3a3a
        ctc.titlePageBgColor =  BaseColor.WHITE; // new BaseColor(255, 255, 255); // #ffffff
        ctc.pageBgColor =    BaseColor.WHITE;  // new BaseColor(234, 234, 234);      // #eaeaea
        ctc.hraBaseReportColor = ctc.hraBlue; // new BaseColor(39, 178, 231); // #27b2e7   //   new BaseColor( 241,90,41 );   // #f1592a

        ctc.tablePageBgColor = BaseColor.WHITE; //  new BaseColor(0xf9, 0xf9, 0xf9);
        
        ctc.markerBlack = new BaseColor(0x00,0x00,0x00);   
        
        ctc.lightBoxBorderColor=new BaseColor( 0xea,0xea,0xea );

        ctc.lighterBoxBorderColor=new BaseColor( 0xf7,0xf7,0xf7 );

        ctc.scoreWhite = new BaseColor(0xEF,0xEF,0xEF); // new BaseColor(0x69,0xa2,0x20);
        ctc.scoreBlack = new BaseColor(0xC1,0xC1,0xC1); // new BaseColor(0x69,0xa2,0x20);
        
        if( 1==2 && dev )
        {
            ctc.gray = new BaseColor(0x80,0x80,0x80);
            ctc.lightergray = new BaseColor(0xc5,0xc5,0xc5);

            // ctc.blue =  new BaseColor(0x21,0x96,0xf3); // new BaseColor(0xb8,0xe1,0xe7);
            ctc.blue =  new BaseColor(0x00, 0x77, 0xcc); 

            // ctc.profileBlue = new BaseColor(0x21,0x96,0xf3);   // new BaseColor(0x17,0xb4,0xee);   
            ctc.profileBlue = new BaseColor(0x00, 0x77, 0xcc); 
            
            ctc.green = new BaseColor(0x38,0xDC,0x30); // new BaseColor(0x69,0xa2,0x20);
            // ctc.green = new BaseColor(0x1F,0x72,0xED); // new BaseColor(0x69,0xa2,0x20);
            //ctc.yellowgreen = new BaseColor(0x1F,0x72,0xED); // new BaseColor(0x8c,0xc6,0x3f);
            ctc.yellow = new BaseColor(0xFE,0xFE,0x0E);
            //ctc.redyellow = new BaseColor(0x38,0xDC,0x30); // new BaseColor(0xf1,0x75,0x23);
            ctc.red = new BaseColor(0x1F,0x72,0xED); // new BaseColor(0x1F,0x72,0xED); 
            // ctc.red = new BaseColor(0x38,0xDC,0x30); // new BaseColor(0xff,0x00,0x00);    
            
            ctc.yellowgreen = ctc.green;
            ctc.redyellow = ctc.yellow;
        }
        
        else
        {
            ctc.gray = new BaseColor(0x80,0x80,0x80);
            ctc.lightergray = new BaseColor(0xc5,0xc5,0xc5);

            //ctc.blue =  new BaseColor(0x21,0x96,0xf3); //new BaseColor(0xb8,0xe1,0xe7);
            ctc.blue =  new BaseColor(0x00, 0x77, 0xcc); 

            //ctc.profileBlue = new BaseColor(0x21,0x96,0xf3); // new BaseColor(0x17,0xb4,0xee);            
            ctc.profileBlue = new BaseColor(0x00, 0x77, 0xcc); 
            
            /*
            ctc.green = new BaseColor(0x69,0xa2,0x20);
            ctc.yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
            ctc.yellow = new BaseColor(0xfc,0xee,0x21);
            ctc.redyellow = new BaseColor(0xf1,0x75,0x23);
            ctc.red = new BaseColor(0xff,0x00,0x00); 
            */
            
            ctc.green = new BaseColor(0x14,0xae,0x5c);
            ctc.yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
            ctc.yellow = new BaseColor(0xfc,0xee,0x21);
            ctc.redyellow = new BaseColor(0xff,0xa6,0x29);
            ctc.red = new BaseColor(0xe7,0x19,0x1f); 
        }
        
        
        
        return ctc;
    }
    
    
    public void clearBorders()
    {
        scoreBoxBorderColor=BaseColor.WHITE;
        scoreBoxBorderWidth = 0f;

        lightBoxBorderColor=BaseColor.WHITE;
        lighterBoxBorderColor=BaseColor.WHITE;        
    }
    
    public BaseColor getGray() {
        return gray;
    }

    public void setGray(BaseColor gray) {
        this.gray = gray;
    }

    public BaseColor getLightergray() {
        return lightergray;
    }

    public void setLightergray(BaseColor lightergray) {
        this.lightergray = lightergray;
    }

    public BaseColor getBlue() {
        return blue;
    }

    public void setBlue(BaseColor blue) {
        this.blue = blue;
    }

    public BaseColor getGreen() {
        return green;
    }

    public void setGreen(BaseColor green) {
        this.green = green;
    }

    public BaseColor getYellowgreen() {
        return yellowgreen;
    }

    public void setYellowgreen(BaseColor yellowgreen) {
        this.yellowgreen = yellowgreen;
    }

    public BaseColor getYellow() {
        return yellow;
    }

    public void setYellow(BaseColor yellow) {
        this.yellow = yellow;
    }

    public BaseColor getRedyellow() {
        return redyellow;
    }

    public void setRedyellow(BaseColor redyellow) {
        this.redyellow = redyellow;
    }

    public BaseColor getRed() {
        return red;
    }

    public void setRed(BaseColor red) {
        this.red = red;
    }

    public BaseColor getProfileBlue() {
        return profileBlue;
    }

    public void setProfileBlue(BaseColor profileBlue) {
        this.profileBlue = profileBlue;
    }

    public BaseColor getMarkerBlack() {
        return markerBlack;
    }

    public void setMarkerBlack(BaseColor markerBlack) {
        this.markerBlack = markerBlack;
    }

    public BaseColor getLightBoxBorderColor() {
        return lightBoxBorderColor;
    }


    
    
}
