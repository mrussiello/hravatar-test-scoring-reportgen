/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.bestjobs;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class BestJobsInterestScoreGraphicCellEvent implements PdfPCellEvent {

    // public static Image summaryCatNumericMarker=null;

    public  BaseColor gray = null;
    public  BaseColor lightergray = null;
    public  BaseColor blue = null;
    public  BaseColor markerBlack  = null;
    
    public BaseColor barColor = null;

    private TestEventScore tes;

    private BaseFont numBaseFont;    

    public BestJobsInterestScoreGraphicCellEvent( TestEventScore tes, BaseFont numBaseFont )
    {
        this.tes = tes;
        // this.summaryCatNumericMarkerTemplate = marker;
        this.numBaseFont = numBaseFont;

        //if( gray == null )
        //{
            gray = new BaseColor(0x80,0x80,0x80);
            lightergray = new BaseColor(0xc5,0xc5,0xc5);
            blue = new BaseColor(0xb8,0xe1,0xe7);
            
            barColor = new BaseColor( 0xda,0xda,0x2d);
            

            markerBlack = new BaseColor(0x00,0x00,0x00);                
            
        //}
    }

    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            // Get the score
            float hScale = 0.70f;
            float wid = hScale*rctngl.getWidth();
            float hgt = rctngl.getHeight();
            
            // LogService.logIt( "BestJobsInterestScoreGraphicCellEvent.cellLayout() tes.name=" + this.tes.getName() + " wid=" + wid + ", hgt=" + hgt );
            // float cellLeft = rctngl.getLeft();
            // float gridWidth = 110;
            // float gridLeftMargin = 5;
            // float gridRightMargin = 5;

            float score = tes.getScore();

            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            pcb.saveState();

            PdfGState state = new PdfGState();
            state.setFillOpacity(1);
            pcb.setGState(state);

            pcb.setLineWidth( 0.5f );

            // First draw the lines
            float llx = rctngl.getLeft() + (rctngl.getWidth()-wid)/2 ;
            float lly = rctngl.getBottom() + 2;
            float urx = llx + wid;
            float ury = lly + hgt - 7;

            float midy = lly + (ury - lly)/2;

            // LogService.logIt( "BestJobsInterestScoreGraphicCellEvent.cellLayout() tes.name=" + this.tes.getName() + " wid=" + wid + ", hgt=" + hgt + ", llx=" + llx + ", urx=" + urx );            
            
            // Marker
            float umkry = rctngl.getBottom() + hgt - 2;
            float mkrTpWid=4f;
            float lmkry = midy+1;

            

            // Color bars
            float ubary = ury;
            float lbary = midy + 0.5f;
            float barhgt = ubary-lbary;
            float lbarx;
            float ubarx;

            float xincr = (urx - llx); ///(sft.getNumberOfGraphicAxisPoints()-1);
            float txty = lly;

            // There is only one barm, one range - 0 - 100.

            // All
            if( 1==1 )
            {                
                // java.util.List<ScoreCategoryRange> scrLst = tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) ? getOverallScoreCatInfoList()  : tes.getScoreCatInfoList();

                //for( ScoreCategoryRange scr : scrLst )
                //{
                //    if( scr.getMax()<=scr.getMin() )
                //        continue;
                    
                    pcb.setColorFill( barColor );
                    lbarx = llx; //   + (wid)*(scr.getMin())/100; // sft.getMax();
                    ubarx = llx  + (wid); // *(scr.getMax())/100; // sft.getMax();
                    pcb.rectangle(  lbarx, lbary, ubarx - lbarx, barhgt );
                    pcb.fill();
                //}
            }


            // LogService.logIt( "BestJobsInterestScoreGraphicCellEvent.cellLayout EVENT() llx=" + llx + ", lly=" + lly + ", urx=" + urx + ", ury=" + ury + ", midy=" + midy + ", rect.wid=" + rctngl.getWidth() + "x" + rctngl.getHeight() );

            // Next, Line graph
            pcb.setColorStroke( lightergray );

            // Outer lines
            pcb.moveTo( llx , ury );
            pcb.lineTo( llx, midy );
            pcb.lineTo( urx, midy );
            pcb.lineTo( urx, ury);
            pcb.lineTo( llx, ury);

            // inner lines
            //for( int i=1;i<sft.getNumberOfGraphicAxisPoints()-1; i++ )
            //{
            //    pcb.moveTo(llx + (i*xincr) , ury );
            //    pcb.lineTo(llx + (i*xincr), midy );
            //}
            // pcb.closePath();
            pcb.stroke();

            // pcb.setColorStroke( lightergray );

            // pcb.setColorFill( lightergray );
            pcb.setColorFill( gray );
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize(numBaseFont, 5);

            // int val = 0;

            String[] tickVals = new String[] {"0","100"}; // sft.getTickVals();
            
            // for( int i=0;i<sft.getNumberOfGraphicAxisPoints()-1; i++ )
            for( int i=0;i<1; i++ )
            {
                pcb.setTextMatrix( llx + (i*xincr) - (i>0 ? 3 : 0) , txty);
                pcb.showText( tickVals[i] );
                //pcb.showText( val + "" );
                //val += 20;
            }

            pcb.setTextMatrix( urx - 7, txty);
            pcb.showText( tickVals[tickVals.length-1] );
            pcb.endText();

            // pcb.setColorStroke(BaseColor.WHITE);
            // float markerX = llx  + (wid)*(score)/(sft.getMax());
            float markerX = llx  + (wid)*(score)/(100);

            markerX = (float) Math.round( markerX );

            pcb.newPath();
            pcb.setColorFill( markerBlack );
            pcb.moveTo( markerX-mkrTpWid/2, umkry );
            pcb.lineTo( markerX+mkrTpWid/2, umkry );
            pcb.lineTo( markerX, lmkry );
            pcb.closePath();
            pcb.fill();

            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BestJobsInterestScoreGraphicCellEvent.cellLayout() " + tes.toString() );
        }

    }
}
