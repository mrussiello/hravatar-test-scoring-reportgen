/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.hraph.tmldr;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class TmLdrSummaryScoreGraphicCellEvent implements PdfPCellEvent {

    // public static Image summaryCatNumericMarker=null;


    public static  BaseColor gray = null;
    public static  BaseColor lightergray = null;
    public static   BaseColor blue = null;
    public static  BaseColor  green = null;
    public static  BaseColor  yellowgreen = null;
    public static   BaseColor yellow = null;
    public static  BaseColor  redyellow = null;
    public static  BaseColor red = null;
    public static BaseColor profileBlue = null;
    public static BaseColor markerBlack  = null;




    private TestEventScore tes;

    private ScoreCategoryType sct;

    // private Image  summaryCatNumericMarkerTemplate;

    private BaseFont numBaseFont;

    private boolean showRedRange = false;
    
    private boolean useRawOverallScore = false;

    public TmLdrSummaryScoreGraphicCellEvent( TestEventScore tes, BaseFont numBaseFont, boolean useRawOverallScore)
    {
        this.tes = tes;
        // this.sct = sct;
        // this.summaryCatNumericMarkerTemplate = marker;
        this.numBaseFont = numBaseFont;
        // this.showRedRange = showRed;
        
        this.useRawOverallScore = useRawOverallScore;
        

        if( gray == null )
        {
            gray = new BaseColor(0x80,0x80,0x80);
            lightergray = new BaseColor(0xc5,0xc5,0xc5);
            blue = new BaseColor(0xb8,0xe1,0xe7);


            //profileBlue = new BaseColor(0x00,0x00,0xff);
            //green = new BaseColor(0xa1,0xff,0x8f);
            //yellowgreen = new BaseColor(0xea,0xff,0x8f);
            //yellow = new BaseColor(0xff,0xff,0x8f);
            //redyellow = new BaseColor(0xff,0xe7,0x8f);
            //red = new BaseColor(0xff,0x8f,0x8f);


            profileBlue = new BaseColor(0x17,0xb4,0xee);
            green = new BaseColor(0x69,0xa2,0x20);
            yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
            yellow = new BaseColor(0xfc,0xee,0x21);
            redyellow = new BaseColor(0xf1,0x75,0x23);
            red = new BaseColor(0xff,0x00,0x00);

            markerBlack = new BaseColor(0x00,0x00,0x00);
        }
    }

    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            // Get the score
            float hScale = 0.75f;
            float wid = hScale*rctngl.getWidth();
            float hgt = rctngl.getHeight();

            float topPad = 2;

            hgt -= topPad;

            // float cellLeft = rctngl.getLeft();
            // float gridWidth = 110;
            // float gridLeftMargin = 5;
            // float gridRightMargin = 5;

            float score = useRawOverallScore ? tes.getOverallRawScoreToShow() : tes.getScore();

            float[] profileBounds = tes.getProfileBoundaries();

            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];


            pcb.saveState();

            PdfGState state = new PdfGState();
            state.setFillOpacity(1);
            pcb.setGState(state);

            pcb.setLineWidth( 0.5f );

            // First draw the lines
            float llx = rctngl.getLeft() + (rctngl.getWidth()-wid)/2 ;
            float lly = rctngl.getBottom();
            float urx = llx + wid;
            float ury = lly + hgt - 7 - topPad;

            float midy = lly + (ury - lly)/2;

            // Marker
            float umkry = rctngl.getBottom() + hgt - 2;
            float lmkry = midy+1;

            // Profile bars
            float pbarhgt = 1.2f;
            // float upbary = umkry - 1.5f;  // pbar is profile bar
            float lpbary = umkry - pbarhgt-1.5f;
            float lpbarx = profileBounds != null ? llx  + (wid)*(profileBounds[0])/100f : 0;
            float rpbarx = profileBounds != null ? llx  + (wid)*(profileBounds[1])/100f : 0;


            // Color bars
            float ubary = ury;
            float lbary = midy + 0.5f;
            float barhgt = ubary-lbary;
            float lbarx;
            float ubarx;

            // LogService.logIt( "TmLdrSummaryScoreGraphicCellEvent.cellLayout() barhgt=" + barhgt );

            float mkrTpWid=6f;

            float barFontSize = 8;

            if( barhgt < 8 )
            {
                mkrTpWid = 4f;
                umkry -= 4;
                barFontSize = 5;
            }


            float xincr = (urx - llx)/5;
            float txty = lly + 3;

            // float txty = lly + ( rctngl.getHeight() - barhgt - 2  ) ;


            // COLOR RANGES FIRST

            // All
            if( 1==1 )
            {
                java.util.List<ScoreCategoryRange> scrLst = null;
                
                // Overall score has CatInfo in tes.textParam1 only if there is a profile that defined it during scoring. 
                if( tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) )
                {
                    // Get the info from the TES - if it's there.
                    scrLst = tes.getScoreCatInfoList();
                    
                    // Not there, use the default.
                    if( scrLst == null || scrLst.isEmpty() )
                        scrLst = getOverallScoreCatInfoList();
                    
                }
                
                else
                    scrLst = tes.getScoreCatInfoList();
                                
                // java.util.List<ScoreCategoryRange> scrLst = tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) ? getOverallScoreCatInfoList()  : tes.getScoreCatInfoList();

                for( ScoreCategoryRange scr : scrLst )
                {
                    pcb.setColorFill( getBaseColorForScoreCatTypeId( scr.getScoreCategoryTypeId() ));
                    lbarx = llx  + (wid)*(scr.getMin())/100f;
                    ubarx = llx  + (wid)*(scr.getMax())/100f;
                    pcb.rectangle(  lbarx, lbary, ubarx - lbarx, barhgt );
                    pcb.fill();
                }
            }

            if( profileBounds != null )
            {
                    pcb.setColorFill( profileBlue );
                    pcb.rectangle(  lpbarx, lpbary+1f, rpbarx - lpbarx, pbarhgt );
                    pcb.fill();
            }


            // LogService.logIt( "TmLdrSummaryScoreGraphicCellEvent.cellLayout EVENT() llx=" + llx + ", lly=" + lly + ", urx=" + urx + ", ury=" + ury + ", midy=" + midy + ", rect.wid=" + rctngl.getWidth() + "x" + rctngl.getHeight() );

            // Next, Line graph
            pcb.setColorStroke( lightergray );

            pcb.moveTo( llx , ury );
            pcb.lineTo( llx, midy );
            pcb.lineTo( urx, midy );
            pcb.lineTo( urx, ury);

            for( int i=1;i<=4; i++ )
            {
                pcb.moveTo(llx + (i*xincr) , ury );
                pcb.lineTo(llx + (i*xincr), midy );
            }
            // pcb.closePath();
            pcb.stroke();

            // pcb.setColorStroke( lightergray );

            pcb.setColorFill( lightergray );
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize(numBaseFont, barFontSize);

            int val = 0;

            for( int i=0;i<=4; i++ )
            {
                pcb.setTextMatrix( llx + (i*xincr) , txty);
                pcb.showText( val + "" );
                val += 20;
            }

            pcb.setTextMatrix( urx - 7, txty);
            pcb.showText( "100" );
            pcb.endText();

            // pcb.setColorStroke(BaseColor.WHITE);
            float markerX = llx  + (wid)*(score)/100f;

            markerX = (float) Math.round( markerX );

            pcb.newPath();
            pcb.setColorFill( markerBlack );
            pcb.moveTo( markerX-mkrTpWid/2, umkry );
            pcb.lineTo( markerX+mkrTpWid/2, umkry );
            pcb.lineTo( markerX, lmkry );
            pcb.closePath();
            pcb.fill();



            //Image markerImage = ITextUtils.getITextImage(summaryCatNumericMarkerTemplate);
            //markerImage.setAbsolutePosition( markerX, rctngl.getBottom());
            //pcb.addImage( markerImage );


            // LogService.logIt( "TmLdrSummaryScoreGraphicCellEvent.cellLayout EVENT() " + tes.getName() + ", markerX=" + markerX + ", rctngl=" + rctngl.getWidth() + "x" + rctngl.getHeight() );
            // PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];
            //pcb.setColorFill(BaseColor.RED);
            //pcb.rectangle(  rctngl.getLeft(), rctngl.getBottom(), rctngl.getWidth(), rctngl.getHeight() );
            //pcb.fill();
            pcb.restoreState();



        }

        catch( Exception e )
        {
            LogService.logIt( e, "TmLdrSummaryScoreGraphicCellEvent.cellLayout() " + tes.toString() );
        }

    }


    public static java.util.List<ScoreCategoryRange> getOverallScoreCatInfoList()
    {
        return ScoreFormatUtils.getOverallScoreCatInfoList(false, null);
    }


    private BaseColor getBaseColorForScoreCatTypeId( int scoreCatTypeId )
    {
        ScoreCategoryType scat = ScoreCategoryType.getType(scoreCatTypeId);

        if( scat.green() )
            return green;
        if( scat.yellowGreen() )
            return yellowgreen;
        if( scat.yellow() )
            return yellow;
        if( scat.redYellow() )
            return redyellow;
        return red;
    }


}
