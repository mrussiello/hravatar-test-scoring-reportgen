/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.ivr;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.BaseFont;
import com.tm2score.custom.coretest2.CT2Colors;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.profile.Profile;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.voicevibes.VoiceVibesScaleScore;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class BaseVoiceVibesCellEvent {
    
    public  BaseColor gray = null;
    public  BaseColor lightergray = null;
    public  BaseColor blue = null;
    public  BaseColor  green = null;
    public  BaseColor  yellowgreen = null;
    public  BaseColor yellow = null;
    public  BaseColor  redyellow = null;
    public  BaseColor red = null;
    public  BaseColor profileBlue = null;
    public  BaseColor markerBlack  = null;


    Profile profile;

    // private boolean devel = false;

    VoiceVibesScaleScore vvs;
    
    TestEventScore tes;

    ScoreCategoryType sct;

    // private Image  summaryCatNumericMarkerTemplate;

    BaseFont numBaseFont;
    Locale locale;
    
    
    public BaseVoiceVibesCellEvent( VoiceVibesScaleScore vvs, 
                                    Profile profile, 
                                    ScoreCategoryType sct,
                                    CT2Colors ct2Colors,
                                    BaseFont numBaseFont,
                                    Locale locale)
    {
        this.vvs = vvs;
        this.sct = sct;
        this.numBaseFont = numBaseFont;
        this.profile = profile;
        this.locale=locale;

        gray = new BaseColor(0x80,0x80,0x80);
        lightergray = new BaseColor(0xc5,0xc5,0xc5);
        blue = new BaseColor(0xb8,0xe1,0xe7);



        if( ct2Colors != null )
        {
            gray = ct2Colors.gray;
            lightergray = ct2Colors.lightergray;
            blue = ct2Colors.blue;
            profileBlue = ct2Colors.profileBlue;           
            green = ct2Colors.green;
            yellowgreen = ct2Colors.yellowgreen;
            yellow = ct2Colors.yellow;
            redyellow = ct2Colors.redyellow;
            red = ct2Colors.red;
            markerBlack = ct2Colors.markerBlack;
        }

        else
        {
            profileBlue = new BaseColor(0x17,0xb4,0xee);            
            green = new BaseColor(0x69,0xa2,0x20);
            yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
            yellow = new BaseColor(0xfc,0xee,0x21);
            redyellow = new BaseColor(0xf1,0x75,0x23);
            red = new BaseColor(0xff,0x00,0x00);
            markerBlack = new BaseColor(0x00,0x00,0x00);                
        }

        if( profile != null && profile.getStrParam3()!=null && !profile.getStrParam3().isEmpty() )
        {
            BaseColor[] cols = ProfileUtils.parseBaseColorStrAsBaseColors( profile.getStrParam3() );

            if( cols[0] != null )
                red = cols[0];
            if( cols[1] != null )
                redyellow = cols[1];
            if( cols[2] != null )
                yellow = cols[2];
            if( cols[3] != null )
                yellowgreen = cols[3];
            if( cols[4] != null )
                green = cols[4];
        }
    }    
    
}
