/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.hraph.nqesh.cor;

import com.tm2score.custom.coretest2.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
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
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.service.LogService;
import java.awt.image.BufferedImage;

/**
 *
 * @author Mike
 */
public class NqeshCorSigBlockCellEvent implements PdfPCellEvent {

    // public static Image summaryCatNumericMarker=null;

    Image sigImage = null;

    public NqeshCorSigBlockCellEvent( Image sigImage )
    {
        this.sigImage = sigImage;
    }

    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            if( this.sigImage==null )
            {
                LogService.logIt( "NqeshCorSigBlockCellEvent.doCellLayout() sigImage is null." );
                return;
            }
            
            
            float imgHeight = sigImage.getScaledHeight();
            float wid = rctngl.getWidth();
            float hgt = rctngl.getHeight();
                                    
            // LogService.logIt(  "NqeshCorSigBlockCellEvent.doCellLayout() imgHeight=" + imgHeight + ", cell wid=" + wid + ", cell hgt=" + hgt );
                        
            sigImage.setAbsolutePosition( rctngl.getLeft()+26, rctngl.getTop() - imgHeight + 2 );
            PdfContentByte pcb = pcbs[ PdfPTable.LINECANVAS ];

            pcb.saveState();
            
            pcb.addImage(sigImage);
            
            pcb.restoreState();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "NqeshCorSigBlockCellEvent.doCellLayout() " );
        }
    }


    

}
