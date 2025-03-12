/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.tm2score.service.LogService;

/**
 *
 * @author miker_000
 */
public class CT2ChatBoxTableEvent  implements PdfPTableEvent {
    
    BaseColor bgColor;
    
    public CT2ChatBoxTableEvent( BaseColor bgCol )
    {
        this.bgColor = bgCol;
    }
    
    
    @Override
    public void tableLayout( PdfPTable ppt, float[][] width, float[] height, int headerRows, int rowStart, PdfContentByte[] pcbs )
    {
        try
        {                        
            // LogService.logIt( "CT2ChatBoxTableEvent.tableLayout() " );

            PdfContentByte pcb = pcbs[ PdfPTable.BASECANVAS ];
            
            pcb.saveState();
            
            pcb.setColorFill(bgColor);
            pcb.roundRectangle( width[0][0] - 8, 
                                height[height.length-1]- 4, // -2, 
                                width[0][1] - width[0][0] + 6, 
                                height[0] - height[height.length - 1] + 8, // - 4, 
                                4 );
            pcb.fill();
            
            pcb.restoreState();            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2ChatBoxCellEvent.cellLayout() " );
        }

    }    
    
}
