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
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.global.Constants;
import com.tm2score.score.ScoreCategoryRange;
// import com.tm2score.profile.ProfileUtils;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class CT2MetaScoreGraphicCellEvent implements PdfPCellEvent {

    // public static Image summaryCatNumericMarker=null;

    
    
    public BaseColor[] colors = new BaseColor[] {   new BaseColor(0xff,0xff,0xff),
                                                    new BaseColor(0xe1,0xf0,0xfd),
                                                    new BaseColor(0xbf,0xe0,0xfe),
                                                    new BaseColor(0x94,0xcc,0xfe),
                                                    new BaseColor(0x62,0xb4,0xfe),
                                                    new BaseColor(0xef,0xef,0xef),  // Score White
                                                    new BaseColor(0xc1,0xc1,0xc1)}; // Score Black
    
    

    public BaseColor markerBlack = new BaseColor(0x00,0x00,0x00);   
    public BaseColor gray = new BaseColor(0x80,0x80,0x80);
    public BaseColor lightergray = new BaseColor(0xc5,0xc5,0xc5);
    
    private float score;
    private BaseFont numBaseFont;
    private ScoreColorSchemeType scoreColorSchemeType = ScoreColorSchemeType.FIVECOLOR;
    // private ScoreFormatType sft;

    private float[] minMax = null;
    

    public CT2MetaScoreGraphicCellEvent( BaseFont numBaseFont, float score, ScoreColorSchemeType scoreColorSchemeType )
    {
        CT2Colors c2c = CT2Colors.getCt2Colors( false );
        
        colors[0]=c2c.red;
        colors[1]=c2c.redyellow;
        colors[2]=c2c.yellow;
        colors[3]=c2c.yellowgreen;
        colors[4]=c2c.green;
        colors[5]=c2c.scoreWhite;
        colors[6]=c2c.scoreBlack;
        
        this.numBaseFont = numBaseFont;
        // this.sft = sft;
        // this.devel = isDevel;
        this.score = score;
        
        this.scoreColorSchemeType = scoreColorSchemeType==null ? ScoreColorSchemeType.FIVECOLOR : scoreColorSchemeType;
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
            
            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            minMax = new float[]{0,100};
            
            float scoreRange = minMax[1] - minMax[0];
            
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

            // LogService.logIt( "CT2MetaScoreGraphicCellEvent.cellLayout() tes.name=" + this.tes.getName() + " wid=" + wid + ", hgt=" + hgt + ", llx=" + llx + ", urx=" + urx );            
            
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

            // COLOR RANGES FIRST

            
            int numberOfGraphicAccessPoints = 6; // sft.getNumberOfGraphicAxisPoints();
            
            java.util.List<ScoreCategoryRange> scrLst = getOverallScoreCatInfoList(false);
            
            // Overall score has CatInfo in tes.textParam1 only if there is a profile that defined it during scoring. 

            float min,max;
            
            ScoreCategoryRange scr;
            
            for( int i=0;i<scrLst.size() && i<5; i++ )
            {
                scr = scrLst.get(i);
                
                if( scr.getMax()<=scr.getMin() )
                    continue;

                pcb.setColorFill( colors[i] );
                lbarx = llx  + (wid)*(scr.getMin() - minMax[0])/scoreRange;
                ubarx = llx  + (wid)*(scr.getMax() - minMax[0])/scoreRange;
                pcb.rectangle(  lbarx, lbary, ubarx - lbarx, barhgt );
                pcb.fill();
            }
            
            
            
            //for( int i=0;i<5; i++ )
            //{
            //    min = i*20f;
            //    max = min+20f;

            //    pcb.setColorFill(  colors[i] );
            //    lbarx = llx  + (wid)*(min - minMax[0])/scoreRange;
            //    ubarx = llx  + (wid)*(max - minMax[0])/scoreRange;
            //    pcb.rectangle(  lbarx, lbary, ubarx - lbarx, barhgt );
            //    pcb.fill();
            //}


            // LogService.logIt( "CT2MetaScoreGraphicCellEvent.cellLayout EVENT() llx=" + llx + ", lly=" + lly + ", urx=" + urx + ", ury=" + ury + ", midy=" + midy + ", rect.wid=" + rctngl.getWidth() + "x" + rctngl.getHeight() );

            // Next, Line graph
            pcb.setColorStroke( lightergray );

            // Outer lines
            pcb.moveTo( llx , ury );
            pcb.lineTo( llx, midy );
            pcb.lineTo( urx, midy );
            pcb.lineTo( urx, ury);

            //if( numberOfGraphicAccessPoints<=0 && scrLst !=null )
            //    numberOfGraphicAccessPoints = scrLst.size() + 1;
                        
            float xincr = (urx - llx)/(numberOfGraphicAccessPoints-1);
            float txty = lly;
            
            // inner lines
            for( int i=1;i<numberOfGraphicAccessPoints-1; i++ )
            {
                pcb.moveTo(llx + (i*xincr) , ury );
                pcb.lineTo(llx + (i*xincr), midy );
            }
            // pcb.closePath();
            pcb.stroke();

            // pcb.setColorStroke( lightergray );

            // pcb.setColorFill( lightergray );
            pcb.setColorFill( gray );
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize(numBaseFont, 5);

            // int val = 0;

            String[] tickVals = Constants.NUMERIC_0_TO_100_TICKVALS;
            
            for( int i=0;i<numberOfGraphicAccessPoints-1; i++ )
            {
                pcb.setTextMatrix( llx + (i*xincr) - (i>0 ? 3 : 0) , txty);
                pcb.showText( tickVals[i] );
                //pcb.showText( val + "" );
                //val += 20;
            }

            pcb.setTextMatrix( urx - 7, txty);
            pcb.showText( tickVals[tickVals.length-1] );
            pcb.endText();

            boolean showMarker = score>=minMax[0] && score<=minMax[1];
            
            if( showMarker )
            {
            
                // pcb.setColorStroke(BaseColor.WHITE);
                float markerX = llx  + (wid)*(score - minMax[0])/(scoreRange);

                markerX = (float) Math.round( markerX );

                pcb.newPath();
                pcb.setColorFill( markerBlack );
                pcb.moveTo( markerX-mkrTpWid/2, umkry );
                pcb.lineTo( markerX+mkrTpWid/2, umkry );
                pcb.lineTo( markerX, lmkry );
                pcb.closePath();
                pcb.fill();
            }
            
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2MetaScoreGraphicCellEvent.cellLayout() score=" + score );
        }

    }


    public java.util.List<ScoreCategoryRange> getOverallScoreCatInfoList(boolean forEmail)
    {
        return ScoreFormatUtils.getOverallScoreCatInfoList(false,scoreColorSchemeType);

        /*
        int wid = forEmail ? Constants.CT2_COLORGRAPHWID_EML : Constants.CT2_COLORGRAPHWID;

        java.util.List<ScoreCategoryRange> scrl = new ArrayList<>();

        ScoreCategoryRange scr = new ScoreCategoryRange(ScoreCategoryType.RED.getScoreCategoryTypeId(), 0,35, wid );
        scrl.add( scr );
        scr = new ScoreCategoryRange(ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId(), 35,50, wid );
        scrl.add( scr );
        scr = new ScoreCategoryRange(ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), 50,65, wid );
        scrl.add( scr );
        scr = new ScoreCategoryRange(ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId(), 65,80, wid );
        scrl.add( scr );
        scr = new ScoreCategoryRange(ScoreCategoryType.GREEN.getScoreCategoryTypeId(), 80,100, wid );
        scrl.add( scr );

        return scrl;
        */

    }
    

}
