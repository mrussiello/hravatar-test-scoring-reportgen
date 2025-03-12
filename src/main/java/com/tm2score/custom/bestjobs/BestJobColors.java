/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.bestjobs;

import com.itextpdf.text.BaseColor;
import com.tm2score.custom.coretest2.CT2Colors;


/**
 *
 * @author miker_000
 */
public class BestJobColors extends CT2Colors
{
    public static BaseColor baseReportShade  = null;
    
    public static CT2Colors getCt2Colors( boolean dev )
    {
        CT2Colors ctc = new CT2Colors();
        
        // baseReportShade = new BaseColor( 0x57, 0x5c, 0xc3 );
        // baseReportShade = new BaseColor( 0x08, 0x51, 0x8b );
        
        // change to purple
        // ctc.hraBlue =  baseReportShade;
        
        ctc.whiteFontColor = BaseColor.WHITE;  // #ffffff
        ctc.darkFontColor = new BaseColor(0x4d,0x4d,0x4d);   // #4d4d4d
        ctc.lightFontColor = new BaseColor(0x80,0x80,0x80);  // #525252

        ctc.scoreBoxHeaderBgColor = ctc.hraBlue;  // #e9e9e9
        ctc.scoreBoxBgColor = BaseColor.WHITE;  // #ffffff
        ctc.scoreBoxBorderColor = new BaseColor( 0x92, 0x92, 0x92);  // #525252
        ctc.scoreBoxShadeBgColor = new BaseColor( 0xca, 0xe4, 0xee );

        ctc.headerDarkBgColor = ctc.hraBlue; //  new BaseColor(39, 178, 231); // #27b2e7 // new BaseColor( 58,58,58 );    // #3a3a3a
        ctc.titlePageBgColor =  BaseColor.WHITE; // new BaseColor(255, 255, 255); // #ffffff
        ctc.pageBgColor =    BaseColor.WHITE;  // new BaseColor(234, 234, 234);      // #eaeaea
        ctc.hraBaseReportColor = ctc.hraBlue; // new BaseColor(39, 178, 231); // #27b2e7   //   new BaseColor( 241,90,41 );   // #f1592a

        ctc.tablePageBgColor = BaseColor.WHITE; //  new BaseColor(0xf9, 0xf9, 0xf9);
        
        ctc.markerBlack = new BaseColor(0x00,0x00,0x00);   
        
        ctc.gray = new BaseColor(0x80,0x80,0x80);
        ctc.lightergray = new BaseColor(0xc5,0xc5,0xc5);
        // ctc.blue =  new BaseColor(0x21,0x96,0xf3); 
        ctc.blue = new BaseColor(0x00, 0x77, 0xcc); 
        
        // ctc.profileBlue = new BaseColor(0x21,0x96,0xf3);   
        ctc.profileBlue = new BaseColor(0x00, 0x77, 0xcc);   

        ctc.green = new BaseColor(0x38,0xDC,0x30); // new BaseColor(0x69,0xa2,0x20);
        // ctc.green = new BaseColor(0x1F,0x72,0xED); // new BaseColor(0x69,0xa2,0x20);
        //ctc.yellowgreen = new BaseColor(0x1F,0x72,0xED); // new BaseColor(0x8c,0xc6,0x3f);
        ctc.yellow = new BaseColor(0xFE,0xFE,0x0E);
        //ctc.redyellow = new BaseColor(0x38,0xDC,0x30); // new BaseColor(0xf1,0x75,0x23);
        ctc.red = new BaseColor(0x1F,0x72,0xED); // new BaseColor(0xff,0x00,0x00);    
        // ctc.red = new BaseColor(0x38,0xDC,0x30); // new BaseColor(0xff,0x00,0x00);    

        ctc.yellowgreen = ctc.green;
        ctc.redyellow = ctc.yellow;
        
        ctc.lightBoxBorderColor=new BaseColor( 0xea,0xea,0xea );

        ctc.lighterBoxBorderColor=new BaseColor( 0xf7,0xf7,0xf7 );
        
        
        return ctc;
    }
    
}
