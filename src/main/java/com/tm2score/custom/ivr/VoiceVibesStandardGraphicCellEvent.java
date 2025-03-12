/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.ivr;

import com.tm2score.custom.coretest2.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.profile.Profile;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.global.I18nUtils;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.service.LogService;
import com.tm2score.voicevibes.VoiceVibesScaleScore;
import com.tm2score.voicevibes.VoiceVibesScaleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class VoiceVibesStandardGraphicCellEvent extends BaseVoiceVibesCellEvent implements PdfPCellEvent {

    public static float STANDARD_BAR_HEIGHT = 8;
    

    
    public VoiceVibesStandardGraphicCellEvent( VoiceVibesScaleScore vvs, 
                                            Profile profile, 
                                            CT2Colors ct2Colors,
                                            BaseFont numBaseFont,
                                            Locale locale)
    {
        super(vvs, 
               profile, 
               null,
               ct2Colors,
               numBaseFont,
               locale);        
    }
    
    

    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            // Get the score
            float hScale = 0.97f; // 0.80f;
            float wid = hScale*rctngl.getWidth();
            float hgt = rctngl.getHeight();
            float barHgt = STANDARD_BAR_HEIGHT;

            float score = vvs.getScore();
            
            float maxBarWid = wid; // - 50;
            // float barWid = score*maxBarWid/100f;

            // BaseColor baseColor = null; // getBaseColorForScoreCatTypeId( sct.getScoreCategoryTypeId() );
            
            // this is the Hi and Low for Profile
            // float[] profileBounds = tes.getProfileBoundaries();

            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            ScoreFormatType sft = ScoreFormatType.NUMERIC_0_TO_100;
            
            // ScoreFormatType sft = ScoreFormatType.getValue( tes.getScoreFormatTypeId());
                        
            pcb.saveState();

            PdfGState state = new PdfGState();
            state.setFillOpacity(1);
            pcb.setGState(state);

            pcb.setLineWidth( 0.5f );

            // full bar coords
            float llx = rctngl.getLeft() + 3; //(rctngl.getWidth()-wid)/2 ;
            float lly = rctngl.getBottom() + (hgt - barHgt )/2;
            float urx = llx + maxBarWid;
            float ury = lly + barHgt;
            float txty = lly;

            float midy = lly + (ury - lly)/2;
            
            // Marker
            float markerX = llx  + (maxBarWid)*(score-sft.getMin())/(sft.getMax()-sft.getMin());
            float umkry = lly + barHgt + 2;
            float mkrTpWid=5f;
            float lmkry = midy-2;
            
            // bar variables
            float lbarx,ubarx;
            
            
            String numText = "(" + I18nUtils.getFormattedNumber(locale, score, 0 ) + ")";
            
            float txtx = llx + maxBarWid + 5;
            
            java.util.List<ScoreCategoryRange> scrLst = getScoreCategoryRangeList( (int) maxBarWid );

            for( ScoreCategoryRange scr : scrLst )
            {
                if( scr.getMax()<=scr.getMin() || scr.getMin()<sft.getMin() )
                    continue;

                pcb.setColorFill( getBaseColorForScoreCatTypeId( scr.getScoreCategoryTypeId() ));
                lbarx = llx  + (wid)*(scr.getMin()-sft.getMin())/(sft.getMax()-sft.getMin());
                ubarx = llx  + (wid)*(scr.getMax()-sft.getMin())/(sft.getMax()-sft.getMin());
                pcb.rectangle(  lbarx, lly, ubarx - lbarx, barHgt );
                pcb.fill();
            }
            
            // TEXT
            if( 1==2 )
            {
                pcb.setColorFill( gray );
                pcb.beginText();
                pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
                pcb.setFontAndSize(numBaseFont, 5);

                pcb.setTextMatrix( txtx, txty );
                pcb.showText( numText );
                pcb.endText();
            }
            
            // MARKER            
            markerX = (float) Math.round( markerX );

            pcb.newPath();
            pcb.setColorFill( markerBlack );
            pcb.moveTo( markerX-mkrTpWid/2, umkry );
            pcb.lineTo( markerX+mkrTpWid/2, umkry );
            pcb.lineTo( markerX, lmkry );
            pcb.closePath();
            pcb.fill();
                        
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesScaleGraphicCellEvent.cellLayout() " + tes.toString() );
        }

    }
    
    
    private List<ScoreCategoryRange> getScoreCategoryRangeList( int barWidMax )
    {
        VoiceVibesScaleType vvst = this.vvs.getVoiceVibesScaleType();
        

        java.util.List<ScoreCategoryRange> scrl = new ArrayList<>();
        
        ScoreCategoryRange scr;
        
        if( vvst.isHighGood() )
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.RED.getScoreCategoryTypeId(), 0,15, barWidMax );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId(), 15,35, barWidMax );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), 35,55, barWidMax );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId(), 55,75, barWidMax );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.GREEN.getScoreCategoryTypeId(), 75,100, barWidMax );
            scrl.add( scr );            
        }
        
        else
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.RED.getScoreCategoryTypeId(), 0,15, barWidMax );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), 15,35, barWidMax );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.GREEN.getScoreCategoryTypeId(), 35,65, barWidMax );
            scrl.add( scr );            
            scr = new ScoreCategoryRange(ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), 65,85, barWidMax );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.RED.getScoreCategoryTypeId(), 85,100, barWidMax );
            scrl.add( scr );
            
        }
        
        return scrl;

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
        return red;
    }


}
