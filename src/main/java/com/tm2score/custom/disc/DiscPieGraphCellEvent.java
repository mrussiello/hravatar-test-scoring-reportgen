/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.disc;

import com.tm2score.custom.coretest2.*;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.service.LogService;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class DiscPieGraphCellEvent implements PdfPCellEvent {

    // public static Image summaryCatNumericMarker=null;

    /*
      index 0=D
            1=I
            2=S
            3=C
    */
    // float[] discScoreVals;
    CT2Colors ct2Colors;
    int scrDigits = 0;
    Map<String,Object[]> scoreValMap;
    Locale locale;



    public DiscPieGraphCellEvent( Map<String,Object[]> scoreValMap, int scrDigits, CT2Colors ct2Colors, Locale locale )
    {
        this.scoreValMap=scoreValMap;
        this.scrDigits=scrDigits;
        this.ct2Colors=ct2Colors;
        this.locale=locale;
    }

    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {

            // Get the score
            // float hScale = 72f/300f;
            float wid = rctngl.getWidth(); // -20; // -4;
            float hgt = rctngl.getHeight(); //  - 20; // -4;

            Rectangle imgRect = new Rectangle( rctngl.getLeft(),rctngl.getBottom(),rctngl.getRight(),rctngl.getTop());

            // LogService.logIt( "DiscPieGraphCellEvent.cellLayout() wid=" + wid + ", hgt=" + hgt );

            BufferedImage bufImg = DiscReportUtils.getDiscPieGraphImage2(scoreValMap, scrDigits, (int) wid, (int)hgt);

            // LogService.logIt( "DiscPieGraphCellEvent.cellLayout() image.width=" + bufImg.getWidth() + ", image.hgt=" + bufImg.getHeight() );

            Image img = Image.getInstance(bufImg, null );
            img.setAbsolutePosition( rctngl.getLeft(), rctngl.getBottom() );
            img.scaleToFit(imgRect);

            // LogService.logIt( "DiscPieGraphCellEvent.cellLayout() Itext.img.width=" + img.getWidth() + ", .itext.image.hgt=" + img.getHeight() );

            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];
            pcb.saveState();
            pcb.addImage(img);
            pcb.restoreState();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DiscPieGraphCellEvent.cellLayout()  " );
        }
    }

}
