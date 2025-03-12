/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.report.Report;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class CT2SolidBarGraphicCellEvent implements PdfPCellEvent {

    // public static Image summaryCatNumericMarker=null;

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
    public  BaseColor  scoreWhite = null;
    public  BaseColor  scoreBlack = null;


    // Profile profile;
    Report report;

    private boolean devel = false;

    private TestEventScore tes;

    private ScoreCategoryType sct;
    private ScoreColorSchemeType scolort;

    // private Image  summaryCatNumericMarkerTemplate;

    //private BaseFont numBaseFont;

    // private boolean showRedRange = false;
    //private boolean useRawScore = false;
    //private float[] minMax = null;
    

    public CT2SolidBarGraphicCellEvent( TestEventScore tes, Report report, ScoreCategoryType sct, ScoreColorSchemeType scoreColorSchemeType, Profile profile, CT2Colors ct2Colors, boolean isDevel)
    {
        this.tes = tes;
        this.sct = sct;
        this.scolort=scoreColorSchemeType;
        // this.summaryCatNumericMarkerTemplate = marker;
        //this.numBaseFont = numBaseFont;
        // this.showRedRange = showRed;
        // this.profile = profile;
        this.report = report;
        this.devel = isDevel;
        //this.useRawScore = useRawScore;

        //if( gray == null )
        //{
            gray = new BaseColor(0x80,0x80,0x80);
            lightergray = new BaseColor(0xc5,0xc5,0xc5);
            blue = new BaseColor(0xb8,0xe1,0xe7);


            //profileBlue = new BaseColor(0x00,0x00,0xff);
            //green = new BaseColor(0xa1,0xff,0x8f);
            //yellowgreen = new BaseColor(0xea,0xff,0x8f);
            //yellow = new BaseColor(0xff,0xff,0x8f);
            //redyellow = new BaseColor(0xff,0xe7,0x8f);
            //red = new BaseColor(0xff,0x8f,0x8f);


            
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
                scoreWhite=ct2Colors.scoreWhite;
                scoreBlack=ct2Colors.scoreBlack;
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
                scoreWhite = new BaseColor(0xEF,0xEF,0xEF);
                scoreBlack = new BaseColor(0xC1,0xC1,0xC1);
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
                if( cols[5] != null )
                    scoreWhite = cols[5];
                if( cols[6] != null )
                    scoreBlack = cols[6];
            }
            
        //}
    }

    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {            
            // Get the score
            float hScale = 0.25f;
            float wid = hScale*rctngl.getWidth();
            float hgt = rctngl.getHeight();
            
            BaseColor color2Use = getBaseColorForScoreCatTypeId( sct.getScoreCategoryTypeId() );
            
            // LogService.logIt( "CT2SolidBarGraphicCellEvent.cellLayout() tes.name=" + this.tes.getName() + " wid=" + wid + ", hgt=" + hgt );

            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];
            
            pcb.saveState();

            PdfGState state = new PdfGState();
            state.setFillOpacity(1);
            pcb.setGState(state);

            // pcb.setLineWidth( 0.5f );

            // Define the box
            float llx = rctngl.getLeft() + (rctngl.getWidth()-wid)/2 ;
            float lly = rctngl.getBottom() + 3;
            //float urx = llx + wid;
            //float ury = lly + hgt - 2;

            pcb.setColorFill( color2Use );
            pcb.rectangle(  llx, lly, wid, hgt - 5 );
            pcb.fill();

            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2SolidBarGraphicCellEvent.cellLayout() " + tes.toString() );
        }

    }


    
    public static java.util.List<ScoreCategoryRange> getOverallScoreCatInfoList()
    {
        return ScoreFormatUtils.getOverallScoreCatInfoList(false, null);
    }


    private BaseColor getBaseColorForScoreCatTypeId( int scoreCatTypeId )
    {
        ScoreCategoryType scat = ScoreCategoryType.getType(scoreCatTypeId);

        if( scat.green() )
            return green;
        if( scat.yellowGreen() )
            return yellowgreen;
        if( scat.yellow() )
            return yellow;
        if( scat.redYellow() )
            return redyellow;
        if( scat.white() )
            return scoreWhite;
        if( scat.black() )
            return scoreBlack;
        return red;
    }


}
