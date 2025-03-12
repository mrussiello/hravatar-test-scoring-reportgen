/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class TableBackgroundTableEvent implements PdfPTableEvent
{
    
    BaseColor colorToUse = null;
    float radius = 6;


    public TableBackgroundTableEvent( BaseColor colorToUse )
    {
        this.colorToUse=colorToUse;
    }

    @Override
    public void tableLayout(PdfPTable ppt, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] pcbs ) 
    {
        try
        {
            float wid=ppt.getTotalWidth();
            float hgt = ppt.getTotalHeight();
            
            // Draw the bar first
            float llx = widths[0][0];
            float lly = heights[heights.length-1];

            // LogService.logIt( "TableBackgroundCellEvent.tableLayout() wid=" + wid + ", hgt=" + hgt + ", llx=" + llx + ", lly=" + lly );
            
            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setLineWidth(0 );

            //pcb.setColorFill( BaseColor.WHITE );
            //pcb.rectangle(llx, lly, wid, hgt);
            //pcb.fill();
            
            
            pcb.setColorFill( colorToUse );            
            pcb.roundRectangle(llx, lly, wid, hgt, radius);
            pcb.fill();
            
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TableBackgroundTableEvent.tableLayout() " );
        }
    }

}
