/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfShading;
import com.itextpdf.text.pdf.PdfShadingPattern;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class PercentileBarCellEvent implements PdfPCellEvent
{
    static  BaseColor gray = null;
    //static  BaseColor lightergray = null;


    BaseColor color = null;
    BaseColor color2 = null;
    float percentile =0;
    // int count = 0;
    BaseFont font = null;
    String ctStr;


    public PercentileBarCellEvent( float pct, String ctStr, BaseColor col, BaseColor col2, BaseFont bFont)
    {
        this.color = col;
        this.color2 = col2;
        this.font = bFont;
        this.percentile = pct;
        // this.count = ct;
        this.ctStr = ctStr;

        if( percentile>100 )
            percentile = 100;
        if( percentile<0 )
            percentile=0;

        if( gray == null )
        {
            gray = new BaseColor(0x80,0x80,0x80);
        //    lightergray = new BaseColor(0xc5,0xc5,0xc5);
        }
    }



    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            pcb.saveState();

            pcb.setColorFill( color );
            pcb.setLineWidth(0.5f);
            pcb.setColorStroke( color );

            // Draw the bar first
            float wid = rctngl.getWidth()-2;
            float hgt = rctngl.getHeight();
            float llx = rctngl.getLeft();
            float lly = rctngl.getBottom() + 2;

            // LogService.logIt( "PercentileBarCellEvent.cellLayout() wid=" + wid + ", hgt=" + hgt + ", percentile=" + percentile );

            float lineHgt = 9;

            float barWid = wid*(percentile/100);

            PdfShading shade = PdfShading.simpleAxial( pcb.getPdfWriter(), llx, lly, llx+barWid, lly+lineHgt, color, color2, false, false );
            // pcb.paintShading(shade);

            PdfShadingPattern sp = new PdfShadingPattern(shade);
            pcb.setShadingFill(sp);
            pcb.rectangle( llx, lly, barWid, lineHgt );
            pcb.fillStroke();


            float txtPosX;
            float txtPosY = lly + 2;

            //pcb.rectangle( llx, lly, barWid, lineHgt );
            //pcb.fill();

            if( ctStr != null && !ctStr.isEmpty() )
            {
                pcb.beginText();
                pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
                pcb.setFontAndSize( font, 7 );


                if( percentile > 20 )
                {
                    txtPosX = llx + barWid-2;
                    pcb.setColorFill( BaseColor.WHITE );
                    pcb.showTextAligned( Element.ALIGN_RIGHT, ctStr, txtPosX, txtPosY, 0);
                }

                else
                {
                    txtPosX = llx + barWid + 2;
                    pcb.setColorFill( gray );
                    pcb.showTextAligned( Element.ALIGN_LEFT, ctStr, txtPosX, txtPosY, 0);
                }

                pcb.endText();
            }

            pcb.restoreState();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "PercentileBarCellEvent.cellLayout() " );
        }


    }



}
