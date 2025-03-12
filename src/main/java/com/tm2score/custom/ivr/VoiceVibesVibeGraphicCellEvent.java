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
import com.tm2score.global.I18nUtils;
import com.tm2score.service.LogService;
import com.tm2score.voicevibes.VoiceVibesScaleScore;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class VoiceVibesVibeGraphicCellEvent extends BaseVoiceVibesCellEvent implements PdfPCellEvent {

    public static float VIBE_BAR_HEIGHT = 8;

    public VoiceVibesVibeGraphicCellEvent( VoiceVibesScaleScore vvs, 
                                            Profile profile, 
                                            ScoreCategoryType sct,
                                            CT2Colors ct2Colors,
                                            BaseFont numBaseFont,
                                            Locale locale)
    {
        super(vvs, 
               profile, 
               sct,
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
            float hScale = 0.97f;
            float wid = hScale*rctngl.getWidth();
            float hgt = rctngl.getHeight();
            
            float barHgt = VIBE_BAR_HEIGHT;

            float score = vvs.getScore();
            
            float maxBarWid = wid; // - 50;
            float barWid = score*maxBarWid/100f;

            BaseColor baseColor = getBaseColorForScoreCatTypeId( sct.getScoreCategoryTypeId() );
            
            // this is the Hi and Low for Profile
            // float[] profileBounds = tes.getProfileBoundaries();

            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];


            // ScoreFormatType sft = ScoreFormatType.getValue( tes.getScoreFormatTypeId());
                        
            pcb.saveState();

            PdfGState state = new PdfGState();
            state.setFillOpacity(1);
            pcb.setGState(state);

            pcb.setLineWidth( 0.5f );

            // bar coords
            float llx = rctngl.getLeft() + 2; //(rctngl.getWidth()-wid)/2 ;
            float lly = rctngl.getBottom() + (hgt-barHgt)/2f;
            float urx = llx + barWid;
            float ury = lly + barHgt;
            float txty = lly;

            String numText = "(" + I18nUtils.getFormattedNumber(locale, score, 0 ) + ")";
            
            float txtx = llx + maxBarWid + 5;
            
            pcb.setColorFill( baseColor );
            pcb.rectangle(  llx, lly, barWid, barHgt );
            pcb.fill();

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
            
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesScaleGraphicCellEvent.cellLayout() " );
        }

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
