/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.disc;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPTableEvent;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class DiscCollaboratingTabTableEvent implements PdfPTableEvent
{
    BaseFont fontToUse;
    String textToUse;
    BaseColor colorToUse;
    Image imageIcon;
    float radius = 0;


    public DiscCollaboratingTabTableEvent( BaseColor colorToUse, BaseFont fnt, String text, Image img )
    {
        this.colorToUse=colorToUse;
        this.fontToUse=fnt;
        this.textToUse=text;
        this.imageIcon=img;
    }

    @Override
    public void tableLayout(PdfPTable ppt, float[][] widths, float[] heights, int headerRows, int rowStart, PdfContentByte[] pcbs )
    {
        try
        {
            float wid=22; //  ppt.getTotalWidth();
            float hgt = ppt.getTotalHeight();

            // Draw the bar first
            float llx = 0; // widths[0][0];
            float lly = heights[heights.length-1];
            
            float imgHgt = imageIcon == null ? 0 : imageIcon.getScaledHeight();


            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setLineWidth(0 );

            //pcb.setColorFill( BaseColor.WHITE );
            //pcb.rectangle(llx, lly, wid, hgt);
            //pcb.fill();


            pcb.setColorFill( colorToUse );
            
            if( radius>0 )
            {
                pcb.roundRectangle(llx, lly, wid, hgt, radius);
                pcb.fill();

                pcb.rectangle(llx, lly + hgt/2, wid/2, (hgt/2));
                pcb.fill();

                pcb.rectangle(llx, lly, wid/2, (hgt/2));
                pcb.fill();
            }
            else
            {
                pcb.rectangle(llx, lly, wid, hgt );
                pcb.fill();
            }

            
            if( imageIcon!=null )
            {
                imageIcon.setAbsolutePosition( 2, lly + hgt - imgHgt - 20 );        
                pcb.addImage(imageIcon);   
            }

            if( textToUse!=null && !textToUse.isBlank() && fontToUse!=null )
            {
                pcb.beginText();
                pcb.setColorFill( BaseColor.WHITE );
                pcb.setColorStroke( BaseColor.WHITE );

                pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );

                pcb.setFontAndSize(fontToUse, 14 );
                // float textWid = ITextUtils.getTextWidth(textToUse, fontToUse, 14);
                // LogService.logIt( "TableBackgroundCellEvent.tableLayout() wid=" + wid + ", hgt=" + hgt + ", llx=" + llx + ", lly=" + lly + ", imgHgt=" + imgHgt );
                pcb.showTextAligned(Element.ALIGN_CENTER, textToUse, 15, lly + (hgt)/2f - imgHgt + 6 , 90);
                pcb.endText();
            }


            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscCollaboratingTabTableEvent.tableLayout() " );
        }
    }

}
