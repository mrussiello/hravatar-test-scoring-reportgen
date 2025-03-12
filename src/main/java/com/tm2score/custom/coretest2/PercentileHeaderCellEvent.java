/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;
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
public class PercentileHeaderCellEvent implements PdfPCellEvent
{
    CT2Colors ct2Colors = null;
    
    //static  BaseColor gray = null;
    //static  BaseColor lightergray = null;

    BaseFont headerBaseFont = null;

    int numRows = 3;
    float radius = 6;

    public PercentileHeaderCellEvent( BaseFont bFont, int rows, CT2Colors ct2Colors)
    {
        this.headerBaseFont = bFont;
        this.numRows = rows;
        
        this.ct2Colors = ct2Colors;

        //if( gray == null )
        //{
        //    gray = new BaseColor(0x80,0x80,0x80);
        //    lightergray = new BaseColor(0xc5,0xc5,0xc5);
        //}

    }

    @Override
    public void cellLayout(PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            // Draw the bar first
            float wid = rctngl.getWidth();
            float hgt = rctngl.getHeight();
            float llx = rctngl.getLeft();
            float lly = rctngl.getBottom();
            
            float borderWidth = 0.4f;

            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setColorStroke(BaseColor.WHITE);
            pcb.setLineWidth(0 );
            pcb.setColorFill( BaseColor.WHITE );
            pcb.rectangle(llx, lly, wid, hgt);
            pcb.fillStroke();
            
            
            pcb.setColorStroke(ct2Colors.hraBlue);
            pcb.setColorFill( ct2Colors.hraBlue );            
            pcb.roundRectangle(llx, lly + borderWidth, wid, hgt-2*borderWidth, radius);
            pcb.fillStroke();
            
            pcb.rectangle(llx, lly+borderWidth, wid/2, hgt-2*borderWidth);
            pcb.fillStroke();
            
            pcb.restoreState();

            
            
            
            
            
            pcb = pcbs[ PdfPTable.LINECANVAS ];

            pcb.saveState();

            pcb.setLineWidth( 0.5f );
            pcb.setColorFill( BaseColor.WHITE );
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize( headerBaseFont, 11 );

            wid = rctngl.getWidth();
            // float hgt = rctngl.getHeight();
            llx = rctngl.getLeft();
            lly = rctngl.getBottom() + 3;
            // float urx = llx + wid;
            // float ury = lly + hgt;

            int val = 0;
            int valIncr = 10;

            float curX = llx + 2;
            float xincr = wid/10;

            pcb.setTextMatrix( curX , lly );
            pcb.showText( val + "" );

            for( int i=1;i<=9; i++ )
            {
                val = i*valIncr;
                curX = llx + i*xincr - 6;

                pcb.setTextMatrix( curX , lly );
                pcb.showText( val + "" );
            }

            curX = llx + 10*xincr - 20;
            pcb.setTextMatrix( curX , lly );
            pcb.showText( "100" );
            pcb.endText();

            pcb.restoreState();

            pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            pcb.saveState();

            // Next do the grid lines
            pcb.setLineWidth( 0.5f );
            pcb.setColorStroke( ct2Colors.lightergray );
            pcb.setLineDash(3,1);
            float dotlinehgt = numRows*15.5f;

            lly = rctngl.getBottom();

            for( int i=1;i<=9; i++ )
            {
                pcb.newPath();
                pcb.moveTo( llx + i*xincr, lly );
                pcb.lineTo( llx + i*xincr, lly - dotlinehgt );
                //pcb.closePath();
                pcb.stroke();
            }

            pcb.restoreState();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "PercentileHeaderCellEvent.cellLayout() " );
        }


    }



}
