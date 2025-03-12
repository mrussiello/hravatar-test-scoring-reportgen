/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.sports;

import com.tm2score.custom.bestjobs.*;
import com.itextpdf.text.BaseColor;
import com.tm2score.custom.coretest2.CT2Colors;


/**
 *
 * @author miker_000
 */
public class SpColors extends CT2Colors
{
    public static BaseColor baseReportShade  = null;
    
    public static CT2Colors getCt2Colors( boolean dev )
    {
        CT2Colors ctc = new CT2Colors();
        
        // The green shade.
        baseReportShade = new BaseColor( 0x00, 0x7f, 0x3f );
        
        // change to purple
        ctc.hraBlue =  baseReportShade;
        
        ctc.whiteFontColor = BaseColor.WHITE;  // #ffffff
        ctc.darkFontColor = new BaseColor(0x4d,0x4d,0x4d);   // #4d4d4d
        ctc.lightFontColor = new BaseColor(0x80,0x80,0x80);  // #525252

        ctc.scoreBoxHeaderBgColor = ctc.hraBlue;  // #e9e9e9
        ctc.scoreBoxBgColor = BaseColor.WHITE;  // #ffffff
        ctc.scoreBoxBorderColor = new BaseColor( 0x92, 0x92, 0x92);  // #525252
        ctc.scoreBoxShadeBgColor = new BaseColor( 0xba, 0xf7, 0xd8 );

        ctc.headerDarkBgColor = ctc.hraBlue; //  new BaseColor(39, 178, 231); // #27b2e7 // new BaseColor( 58,58,58 );    // #3a3a3a
        ctc.titlePageBgColor =  BaseColor.WHITE; // new BaseColor(255, 255, 255); // #ffffff
        ctc.pageBgColor =    BaseColor.WHITE;  // new BaseColor(234, 234, 234);      // #eaeaea
        ctc.hraBaseReportColor = ctc.hraBlue; // new BaseColor(39, 178, 231); // #27b2e7   //   new BaseColor( 241,90,41 );   // #f1592a

        ctc.tablePageBgColor = BaseColor.WHITE; //  new BaseColor(0xf9, 0xf9, 0xf9);
        
        ctc.markerBlack = new BaseColor(0x00,0x00,0x00);   
        
        ctc.gray = new BaseColor(0x80,0x80,0x80);
        ctc.lightergray = new BaseColor(0xc5,0xc5,0xc5);
        ctc.blue = new BaseColor(0xb8,0xe1,0xe7);
        ctc.profileBlue = new BaseColor(0x17,0xb4,0xee);   

        ctc.green = new BaseColor(0x69,0xa2,0x20);
        ctc.yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
        ctc.yellow = new BaseColor(0xfc,0xee,0x21);
        ctc.redyellow = new BaseColor(0xf1,0x75,0x23);
        ctc.red = new BaseColor(0xff,0x00,0x00);        
        
        return ctc;
    }
    
}
