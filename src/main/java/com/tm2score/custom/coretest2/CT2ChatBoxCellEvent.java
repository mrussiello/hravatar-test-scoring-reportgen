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
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.service.LogService;

/**
 *
 * @author miker_000
 */
public class CT2ChatBoxCellEvent  implements PdfPCellEvent {
    
    BaseColor color;
    BaseColor bgColor;
    
    public CT2ChatBoxCellEvent( BaseColor col, BaseColor bgCol )
    {
        this.color = col;
        this.bgColor = bgCol;
    }
    
    
public void cellLayout( PdfPCell ppc, Rectangle rect, PdfContentByte[] pcbs )
    {
        try
        {            
            
            // LogService.logIt( "CT2ChatBoxCellEvent.cellLayout() " );

            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];
            
            pcb.roundRectangle( rect.getLeft() + 1.5f, rect.getBottom() + 1.5f, rect.getWidth()-3, rect.getHeight()-3, 4 );
            pcb.setColorFill(bgColor);
            pcb.fill();
            
            pcb = pcbs[ PdfPTable.LINECANVAS ];
            
            pcb.roundRectangle( rect.getLeft() + 1.5f, rect.getBottom() + 1.5f, rect.getWidth()-3, rect.getHeight()-3, 4 );            
            pcb.setLineWidth(1.5f);
            pcb.setColorStroke(color);
            pcb.stroke();            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2ChatBoxCellEvent.cellLayout() " );
        }

    }    
    
}
