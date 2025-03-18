/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.disc;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class DiscLegendCellBackgroundCellEvent implements PdfPCellEvent
{
    BaseColor colorToUse = null;
    

    public DiscLegendCellBackgroundCellEvent( BaseColor colorToUse)
    {
        this.colorToUse=colorToUse;
    }
    
    
    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {            
            
            // Draw the bar first
            float wid = Math.min(20, rctngl.getWidth());
            float hgt = wid; // rctngl.getHeight();
            float llx = rctngl.getLeft();
            float lly = rctngl.getBottom();

            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setLineWidth(0 );
            float borderWidth = 0;
            pcb.setColorFill( colorToUse );   
            pcb.setColorStroke(colorToUse);
            pcb.rectangle(llx, lly + borderWidth, wid, hgt-2*borderWidth);
            pcb.fillStroke();
            
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscLegendCellBackgroundCellEvent.cellLayout() " );
        }


    }



}
