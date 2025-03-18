/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.disc;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class DiscPageNumberCellEvent implements PdfPCellEvent
{
    BaseColor bgColor;
    
    String pageStr;
    BaseColor fgColor;
    float radius = 5;
    Font font;
    

    public DiscPageNumberCellEvent( String pageStr, BaseColor bgColor, BaseColor fgColor, Font font )
    {
        this.bgColor=bgColor;
        this.pageStr=pageStr;
        this.fgColor=fgColor;
        this.font = font;
    }
    
    
    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {            
            
            // Draw the bar first
            float wid = rctngl.getWidth()*0.4f;
            float hgt = rctngl.getHeight();
            float llx = rctngl.getLeft() + rctngl.getWidth()*0.25f;
            float lly = rctngl.getBottom();

            // LogService.logIt( "DiscPageNumberCellEvent.cellLayout() bgColor=" + bgColor.getRed() + "," + bgColor.getGreen() + "," + bgColor.getBlue() + ", fgColor=" + fgColor.getRed() + "," + fgColor.getGreen() + "," + fgColor.getBlue() );
            
            
            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setLineWidth(0 );

            //pcb.setColorFill( BaseColor.WHITE );
            //pcb.rectangle(llx, lly, wid, hgt);
            //pcb.fill();
                        
            pcb.setColorFill( bgColor );
            pcb.roundRectangle(llx, lly, wid, hgt, lly);
            // pcb.circle(llx + wid/2, lly + hgt/2, radius);
            pcb.fill();
            
            BaseFont bfont = font.getBaseFont();
            // BaseColor color = font.getColor();
            float textHeight = bfont.getDescentPoint(pageStr, font.getSize() ) - bfont.getAscentPoint(pageStr, font.getSize() );
            
            pcb.setColorFill( this.fgColor );
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize(bfont, font.getSize());
            
            pcb.showTextAligned(Element.ALIGN_CENTER, pageStr, llx + wid/2, lly + hgt/2 + textHeight/2, 0);
            pcb.endText();
            
                        
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscPageNumberCellEvent.cellLayout() " );
        }


    }



}
