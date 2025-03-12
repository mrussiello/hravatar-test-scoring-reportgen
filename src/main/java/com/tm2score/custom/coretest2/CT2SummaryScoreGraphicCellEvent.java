/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.custom.coretest2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.report.Report;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.service.LogService;
import java.awt.image.BufferedImage;

/**
 *
 * @author Mike
 */
public class CT2SummaryScoreGraphicCellEvent implements PdfPCellEvent {

    // public static Image summaryCatNumericMarker=null;

    public  BaseColor gray = null;
    public  BaseColor lightergray = null;
    public  BaseColor blue = null;
    public  BaseColor  scoreWhite = null;
    public  BaseColor  green = null;
    public  BaseColor  yellowgreen = null;
    public  BaseColor yellow = null;
    public  BaseColor  redyellow = null;
    public  BaseColor red = null;
    public  BaseColor  scoreBlack = null;
    public  BaseColor profileBlue = null;
    public  BaseColor markerBlack  = null;


    Profile profile;
    Report report;

    private boolean devel = false;
    private boolean useBellGraph = false;

    private TestEventScore tes;

    private ScoreCategoryType sct;
    private ScoreColorSchemeType scolort;

    // private Image  summaryCatNumericMarkerTemplate;

    private BaseFont numBaseFont;

    private boolean showRedRange = false;
    private boolean useRawScore = false;
    private float[] minMax = null;
    private boolean showNums = true;
    private float horizScaleAdj;
    

    public CT2SummaryScoreGraphicCellEvent( TestEventScore tes, Report report, Profile profile, ScoreCategoryType sct, ScoreColorSchemeType scoreColorSchemeType, BaseFont numBaseFont, boolean showRed, CT2Colors ct2Colors, boolean isDevel, boolean useRawScore, boolean useBellGraph, boolean showNums, float hScale)
    {
        this.tes = tes;
        this.sct = sct;
        this.scolort=scoreColorSchemeType;
        // this.summaryCatNumericMarkerTemplate = marker;
        this.numBaseFont = numBaseFont;
        this.showRedRange = showRed;
        this.profile = profile;
        this.report = report;
        this.devel = isDevel;
        this.useRawScore = useRawScore;
        this.showNums=showNums;
        this.horizScaleAdj = hScale;
        this.useBellGraph=useBellGraph;

        //if( gray == null )
        //{
            gray = new BaseColor(0x80,0x80,0x80);
            lightergray = new BaseColor(0xc5,0xc5,0xc5);
            blue = new BaseColor(0xb8,0xe1,0xe7);


            //profileBlue = new BaseColor(0x00,0x00,0xff);
            //green = new BaseColor(0xa1,0xff,0x8f);
            //yellowgreen = new BaseColor(0xea,0xff,0x8f);
            //yellow = new BaseColor(0xff,0xff,0x8f);
            //redyellow = new BaseColor(0xff,0xe7,0x8f);
            //red = new BaseColor(0xff,0x8f,0x8f);


            
            if( ct2Colors != null )
            {
                gray = ct2Colors.gray;
                lightergray = ct2Colors.lightergray;
                blue = ct2Colors.blue;
                profileBlue = ct2Colors.profileBlue; 
                green = ct2Colors.green;
                yellowgreen = ct2Colors.yellowgreen;
                yellow = ct2Colors.yellow;
                redyellow = ct2Colors.redyellow;
                red = ct2Colors.red;
                scoreWhite=ct2Colors.scoreWhite;
                scoreBlack=ct2Colors.scoreBlack;
                markerBlack = ct2Colors.markerBlack;
            }
            
            else
            {
                profileBlue = new BaseColor(0x17,0xb4,0xee);            
                
                /*
                green = new BaseColor(0x69,0xa2,0x20);
                yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
                yellow = new BaseColor(0xfc,0xee,0x21);
                redyellow = new BaseColor(0xf1,0x75,0x23);
                red = new BaseColor(0xff,0x00,0x00);
*/

                green = new BaseColor(0x14,0xae,0x5c);
                yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
                yellow = new BaseColor(0xfc,0xee,0x21);
                redyellow = new BaseColor(0xff,0xa6,0x29);
                red = new BaseColor(0xe7,0x19,0x1f);

                scoreWhite = new BaseColor(0xEF,0xEF,0xEF);
                scoreBlack = new BaseColor(0xC1,0xC1,0xC1);
                markerBlack = new BaseColor(0x00,0x00,0x00);                
            }
            
            if( profile != null && profile.getStrParam3()!=null && !profile.getStrParam3().isEmpty() )
            {
                BaseColor[] cols = ProfileUtils.parseBaseColorStrAsBaseColors( profile.getStrParam3() );
                
                if( cols[0] != null )
                    red = cols[0];
                if( cols[1] != null )
                    redyellow = cols[1];
                if( cols[2] != null )
                    yellow = cols[2];
                if( cols[3] != null )
                    yellowgreen = cols[3];
                if( cols[4] != null )
                    green = cols[4];
                if( cols[5] != null )
                    scoreWhite = cols[5];
                if( cols[6] != null )
                    scoreBlack = cols[6];
            }
            
            if( scolort==null )
                scolort = ScoreColorSchemeType.FIVECOLOR;
        //}
    }

    public void doCellLayoutBellGraph( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            // Get the score
            float hScale = 0.9f;
            
            if( horizScaleAdj >0 )
                hScale = horizScaleAdj;
            
            float wid = hScale*rctngl.getWidth();
            float hgt = rctngl.getHeight();
                        
            Rectangle imgRect = new Rectangle( rctngl.getLeft()+2,rctngl.getBottom()+2,rctngl.getRight()-2,rctngl.getTop()-2);
            
            float score = useRawScore ? tes.getOverallRawScoreToShow() : tes.getScore();

            // this is the Hi and Low for Profile
            float[] profileBounds = tes.getProfileBoundaries();

            ScoreFormatType sft = ScoreFormatType.NUMERIC_0_TO_100;                        
            minMax = new float[]{sft.getMin(),sft.getMax() };
            
            float ptr = CT2GraphUtils.convertScoreToPtr( score );
            // LogService.logIt( "doCellLayoutBellGraph() wid=" + wid + ", hgt=" + hgt + ", score=" + score + ", ptr=" + ptr + ", ptr for 0: " + CT2GraphUtils.convertScoreToPtr( score ) + ", ptr for score=1: " + CT2GraphUtils.convertScoreToPtr( 1 ) );

            if( profileBounds!=null && profileBounds[1]>0 )
            {
                profileBounds[0] = CT2GraphUtils.convertProfileBoundary( profileBounds[0] );
                profileBounds[1] = CT2GraphUtils.convertProfileBoundary( profileBounds[1] );
            }
            
            BufferedImage bufImg = CT2GraphUtils.getBellGraphImage( (int)ptr, profileBounds==null ? 0 : (int)profileBounds[0], profileBounds==null ? 0 : (int)profileBounds[1] );
            
            Image img = Image.getInstance(bufImg, null );
            img.scaleToFit(imgRect);
            //img.scaleAbsoluteWidth(wid);
            //img.scaleAbsoluteHeight(hgt);
            
            img.setAbsolutePosition( rctngl.getLeft()+2, rctngl.getBottom()+2 );
            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            pcb.saveState();
            
            pcb.addImage(img);
            
            pcb.restoreState();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT2SummaryScoreGraphicCellEvent.doCellLayoutBellGraph() " + tes.toString() );
        }
    }
    
    
    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {          
            if( useBellGraph )
            {
                doCellLayoutBellGraph( ppc, rctngl, pcbs );
                return;
            }
            
            // Get the score
            float hScale = 0.90f;
            
            if( horizScaleAdj >0 )
                hScale = horizScaleAdj;
            
            float wid = hScale*rctngl.getWidth();
            float hgt = rctngl.getHeight();
            
            // LogService.logIt( "CT2SummaryScoreGraphicCellEvent.cellLayout() tes.name=" + this.tes.getName() + " wid=" + wid + ", hgt=" + hgt );
            // float cellLeft = rctngl.getLeft();
            // float gridWidth = 110;
            // float gridLeftMargin = 5;
            // float gridRightMargin = 5;

            float score = useRawScore ? tes.getOverallRawScoreToShow() : tes.getScore();

            // this is the Hi and Low for Profile
            float[] profileBounds = tes.getProfileBoundaries();

            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];


            ScoreFormatType sft = ScoreFormatType.getValue( tes.getScoreFormatTypeId());
                        
            if( sft.equals( ScoreFormatType.OTHER_SCORED ) && report!=null )
                minMax = new float[]{ report.getFloatParam1() , report.getFloatParam2() };
            else
                minMax = new float[]{sft.getMin(),sft.getMax() };
            
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

            // LogService.logIt( "CT2SummaryScoreGraphicCellEvent.cellLayout() tes.name=" + this.tes.getName() + " wid=" + wid + ", hgt=" + hgt + ", llx=" + llx + ", urx=" + urx );            
            
            // Marker
            float umkry = rctngl.getBottom() + hgt - 2;
            float mkrTpWid=4f;
            float lmkry = midy+1;

            // Profile bars
            float pbarhgt = 1.2f;
            // float upbary = umkry - 1.5f;  // pbar is profile bar
            float lpbary = umkry - pbarhgt-1.5f;
            float lpbarx = profileBounds != null ? llx  + (wid)*(profileBounds[0] - minMax[0])/scoreRange : 0;
            float rpbarx = profileBounds != null ? llx  + (wid)*(profileBounds[1] - minMax[0])/scoreRange : 0;


            // Color bars
            float ubary = ury;
            float lbary = midy + 0.5f;
            float barhgt = ubary-lbary;
            float lbarx;
            float ubarx;

            // COLOR RANGES FIRST

            java.util.List<ScoreCategoryRange> scrLst = null;
            
            int numberOfGraphicAccessPoints = sft.getNumberOfGraphicAxisPoints();
            
            // All
            if( 1==1 )
            {
                
                // Overall score has CatInfo in tes.textParam1 only if there is a profile that defined it during scoring. 
                if( tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) )
                {
                    // Get the info from the TES - if it's there.
                    scrLst = tes.getScoreCatInfoList();
                    
                    // Not there, use the default.
                    if( scrLst == null || scrLst.isEmpty() )
                    {
                        scrLst = getOverallScoreCatInfoList();
                    }
                    
                }
                
                else
                    scrLst = tes.getScoreCatInfoList();
                
                if( devel )
                    BaseCT2ReportTemplate.adjustScrCatListForDevel( scrLst, devel );
                
                // java.util.List<ScoreCategoryRange> scrLst = tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) ? getOverallScoreCatInfoList()  : tes.getScoreCatInfoList();

                for( ScoreCategoryRange scr : scrLst )
                {
                    if( scr.getMax()<=scr.getMin() )
                        continue;
                    
                    pcb.setColorFill( getBaseColorForScoreCatTypeId( scr.getScoreCategoryTypeId() ));
                    lbarx = llx  + (wid)*(scr.getMin() - minMax[0])/scoreRange;
                    ubarx = llx  + (wid)*(scr.getMax() - minMax[0])/scoreRange;
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


            // LogService.logIt( "CT2SummaryScoreGraphicCellEvent.cellLayout EVENT() llx=" + llx + ", lly=" + lly + ", urx=" + urx + ", ury=" + ury + ", midy=" + midy + ", rect.wid=" + rctngl.getWidth() + "x" + rctngl.getHeight() );

            // Next, Line graph
            pcb.setColorStroke( lightergray );

            // Outer lines
            pcb.moveTo( llx , ury );
            pcb.lineTo( llx, midy );
            pcb.lineTo( urx, midy );
            pcb.lineTo( urx, ury);

            if( numberOfGraphicAccessPoints<=0 && scrLst !=null )
                numberOfGraphicAccessPoints = scrLst.size() + 1;
                        
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
            
            if( showNums )
            {
                pcb.beginText();
                pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
                pcb.setFontAndSize(numBaseFont, 5);

                // int val = 0;

                String[] tickVals = sft.getTickVals();

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
            }
            
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


            //Image markerImage = ITextUtils.getITextImage(summaryCatNumericMarkerTemplate);
            //markerImage.setAbsolutePosition( markerX, rctngl.getBottom());
            //pcb.addImage( markerImage );


            // LogService.logIt( "CT2SummaryScoreGraphicCellEvent.cellLayout EVENT() " + tes.getName() + ", markerX=" + markerX + ", rctngl=" + rctngl.getWidth() + "x" + rctngl.getHeight() );
            // PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];
            //pcb.setColorFill(BaseColor.RED);
            //pcb.rectangle(  rctngl.getLeft(), rctngl.getBottom(), rctngl.getWidth(), rctngl.getHeight() );
            //pcb.fill();
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2SummaryScoreGraphicCellEvent.cellLayout() " + tes.toString() );
        }

    }


    
    public java.util.List<ScoreCategoryRange> getOverallScoreCatInfoList()
    {
        return ScoreFormatUtils.getOverallScoreCatInfoList(false,scolort);
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
        if( scat.white() )
            return scoreWhite;
        if( scat.black() )
            return scoreBlack;
        return red;
    }


}
