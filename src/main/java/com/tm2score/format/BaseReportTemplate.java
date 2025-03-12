/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.report.ReportSettings;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventScoreType;
import static com.tm2score.format.StandardReportSettings.MAX_CUSTLOGO_H;
import static com.tm2score.format.StandardReportSettings.MAX_CUSTLOGO_W;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.STException;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;


/**
 *
 * @author Mike
 */
public abstract class BaseReportTemplate extends StandardReportSettings implements ReportTemplate, ReportSettings
{
    public Image custLogo = null;

    public ReportData reportData = null;

    public Document document = null;

    public ByteArrayOutputStream baos;

    public PdfWriter pdfWriter;

    public float pageWidth = 0;
    public float pageHeight = 0;

    public String title;

    public float headerHgt;
    public float footerHgt;

    public float lastY = 0;

    public TableBackground dataTableEvent;
    public TableBackground tableHeaderRowEvent;

    public ReportUtils reportUtils;

    public float PAD = 5;

    public float bxX;
    public float bxWid;
    public float barGrphWid;
    public float barGrphX;
    public float lineW = 0.8f;


    public float currentYLevel = 0;
    public float previousYLevel = 0;

    java.util.List<String> prepNotes;



    @Override
    public abstract byte[] generateReport() throws Exception;


    public synchronized void initFonts() throws Exception
    {
        initSettings( reportData );

        try
        {
            custLogo = !reportData.hasCustLogo() ? null : ITextUtils.getITextImage( reportData.getCustLogoUrl() );
        }
        
        catch( Exception e )
        {
            if( reportData.getCustLogoUrl()!=null )
            {
                String logoUrl = reportData.getCustLogoUrl().toExternalForm().trim().toLowerCase();
                if( logoUrl!=null && logoUrl.startsWith("https:"))
                {
                    LogService.logIt( e, "BaseReportTemplate.initFonts() NONFATAL error getting custLogo. Will try http instead of https. logo=" + logoUrl );

                    String logo2 = "http:" + logoUrl.trim().substring(6, logoUrl.length());

                    try
                    {
                        custLogo = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( logo2 ) );                    
                    }
                    catch( Exception ee )
                    {
                        LogService.logIt( ee, "BaseReportTemplate.initFonts() NONFATAL error getting custLogo using http. Will use null. logo=" + logoUrl + ", logo2=" + logo2 );                    
                    }                
                }            

                else
                    LogService.logIt( e, "BaseReportTemplate.initFonts() NONFATAL error getting custLogo. Will use null. logo=: " + reportData.getCustLogoUrl() );        
            }

            else
                LogService.logIt( e, "BaseReportTemplate.initFonts() NONFATAL error getting custLogo. Will use null. logo=: " + reportData.getCustLogoUrl() );        
        }

        if( custLogo != null )
        {

            float imgSclW = 100;
            float imgSclH = 100;
            // float maxImgWid = 80;
            // float maxImgHgt = 40;

            if( custLogo.getWidth() > MAX_CUSTLOGO_W )
                imgSclW = 100 * MAX_CUSTLOGO_W/custLogo.getWidth();

            if( custLogo.getHeight() > MAX_CUSTLOGO_H )
                imgSclH = 100 * MAX_CUSTLOGO_H/custLogo.getHeight();

            imgSclW = Math.min( imgSclW, imgSclH );

            if( imgSclW < 100 )
                custLogo.scalePercent( imgSclW );

        }

        title = StringUtils.replaceStr( reportData.getReportName(), "[SIMNAME]", reportData.getSimName() );

        title = StringUtils.replaceStr( title, "[ORGNAME]", reportData.getOrgName() );

        if( reportData.hasUserInfo() )
            title = StringUtils.replaceStr( title, "[USERNAME]", reportData.getUserName() );

        else
            title = StringUtils.replaceStr( title, "[USERNAME]", "" );

        LogService.logIt( "BaseReportTemplate.initFonts() report title=" + title + ", orgName=" + reportData.getOrgName() );
    }


    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData =rd;

        initFonts();
        
        initColors();

        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        // pdfWriter.addJavaScript( DOCUMENT_JS_API );
        
        StandardHeaderFooter hdr = new StandardHeaderFooter( document, rd.getLocale(), title, reportData, this );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );

        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );

        headerHgt = hghts[0];
        footerHgt = hghts[1];

        bxX = 5*PAD;
        bxWid = pageWidth - 10*PAD;
        barGrphWid = 0.77f*bxWid;
        barGrphX = bxX + ( bxWid - barGrphWid - 2*PAD ); //    bxX + 7*PAD;

        // dataTableWidth = hdr.headerW;

       // document.setMargins( document.leftMargin(), document.rightMargin(), 42, 42 );

        LogService.logIt( "BaseReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

        tableHeaderRowEvent = new TableBackground( null , 0, tablePageBgColor );
    }

    
    public void initColors() throws Exception
    {
        if( reportData== null || reportData.p == null || reportData.p.getStrParam3()== null || reportData.p.getStrParam3().isEmpty() )
            return;
        
        
        BaseColor[] clrs = ProfileUtils.parseBaseColorStrAsBaseColors( reportData.p.getStrParam3() );


        if( clrs[0] != null )
        {
            redCatColor1 = clrs[0];
            redCatColor2 = clrs[0];
        }

        if( clrs[1] != null )
        {
            redYellowCatColor1 = clrs[1];
            redYellowCatColor2 = clrs[1];
        }

        if( clrs[2] != null )
        {
            yellowCatColor1 = clrs[2];
            yellowCatColor2 = clrs[2];
        }

        if( clrs[3] != null )
        {
            yellowGreenCatColor1 = clrs[3];
            yellowGreenCatColor2 = clrs[3];
        }

        if( clrs[4] != null )
        {
            greenCatColor1 = clrs[4];
            greenCatColor2 = clrs[4];
        }
    }
    
    
    @Override
    public void dispose() throws Exception
    {
        if( baos != null )
            baos.close();
    }



    public void closeDoc() throws Exception
    {
        if( document != null && document.isOpen() )
            document.close();

        document = null;
    }


    protected byte[] getDocumentBytes() throws Exception
    {
        if( baos == null )
            return null;

        return baos.toByteArray();
    }

    /**
     * Override this method as needed.
     *
     * @return
     */
    @Override
    public boolean getIsReportGenerationPossible()
    {
        return true;
    }


    @Override
    public Locale getReportLocale()
    {
        if( this.reportData!=null )
            return reportData.getLocale();
        
        return Locale.US;
    }


    @Override
    public String getReportGenerationNotesToSave()
    {
        return null;
    }
    
    protected void addReportInfoHeader() throws Exception
    {
        try
        {
            Font fnt = fontXLarge;

            float y = pageHeight - headerHgt - fnt.getSize() - 4*PAD;  // hraLogoBlackText.getScaledHeight() - fnt.getSize()*2f;

            float x = bxX + PAD;
            float x2 = 100 + pageWidth/2;

            // y += hraLogoBlackText.getScaledHeight();

            ITextUtils.addDirectText( pdfWriter, reportData.getSimName(), x, y, Element.ALIGN_LEFT, fnt, false );
            ITextUtils.addDirectText( pdfWriter, reportData.getUserName(), x2, y, Element.ALIGN_LEFT, fnt, false );

            fnt = font;
            y -= fnt.getSize()*1.2;

            ITextUtils.addDirectText( pdfWriter, " " + lmsg( "g.JobSimAssessment" ) , x, y, Element.ALIGN_LEFT, fnt, false );
            
            if( reportData.getUser().getUserType().getNamed() )
                ITextUtils.addDirectText( pdfWriter, reportData.getUser().getEmail(), x2, y, Element.ALIGN_LEFT, fnt, false );

            y -= fnt.getSize()*1.2;
            ITextUtils.addDirectText(pdfWriter, I18nUtils.getFormattedDate(reportData.getLocale(), reportData.getTimeZone(), reportData.getTestEvent().getLastAccessDate() ), x2, y, Element.ALIGN_LEFT, fnt, false );

            currentYLevel = y;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addReportInfoHeader()" );

            throw e;
        }
    }



    protected void addSummarySection() throws Exception
    {
        try
        {
            if( reportData.getReport().getIncludeOverallScore()!=1 )
                return;

            String scrTxt = reportData.getOverallScoreText(); //getTestEvent().getOverallTestEventScore().getScoreText();

            float scrTxtW = bxWid - 4*PAD;
            float scrTxtH = ITextUtils.getDirectTextHeight(pdfWriter, scrTxt, scrTxtW, Element.ALIGN_LEFT,  FONTSZ, font );

            float panelHgtBase = 120;

            float bxHght = panelHgtBase + ( scrTxtH > 0 ? scrTxtH + 3*PAD : 0 );
            float bxShdw = 3;

            previousYLevel =  currentYLevel;

            currentYLevel = previousYLevel - 6*PAD - bxHght - bxShdw;

            // LogService.logIt( "BaseReportTemplate.addSummarySection() scrTxtH=" + scrTxtH + ", bxHght=" + bxHght + ", reportSummaryBoxY=" + reportSummaryBoxY );

            // create shadow box
            ITextUtils.addDirectColorRect( pdfWriter, BaseColor.LIGHT_GRAY, bxX + bxShdw,  currentYLevel, bxWid, bxHght, 0, 0.7f, true );

            // create white box
            ITextUtils.addDirectColorRect( pdfWriter, BaseColor.WHITE, bxX, currentYLevel + bxShdw, bxWid, bxHght, 0, 1, true );

            Font fnt =  headerFontXLarge;

            float y = currentYLevel + bxHght + bxShdw - fnt.getSize() - PAD;

            // Add Summary Text
            ITextUtils.addDirectText(pdfWriter, lmsg("g.Summary"), bxX + PAD, y, Element.ALIGN_LEFT, fnt, false);

            fnt =  fontXLarge;

            // Add Overall Score Text
            Rectangle rect = new Rectangle( bxX, y, bxX + bxWid, y + fnt.getSize() );

            String scr = I18nUtils.getFormattedNumber( reportData.getLocale(), reportData.getTestEvent().getOverallScore(), 0 );

            ITextUtils.addDirectText(  pdfWriter, lmsg( "g.OverallScoreXC", new String[] {scr}) , rect, Element.ALIGN_CENTER, fnt.getSize(),fnt, false );

            y -= PAD;

            Image img = rainbowBar;

            float sclP = 100*barGrphWid/img.getWidth();

            img.scalePercent( sclP);

            y -= img.getScaledHeight() + 2*PAD;

            float grphY = y;

            ITextUtils.addDirectImage( pdfWriter, img, barGrphX, y, true );

            ITextUtils.addDirectBox( pdfWriter, lightFontColor, lineW, barGrphX, grphY, img.getScaledWidth(), img.getScaledHeight(), false );

            // Create the Grid lines.
            float grphW = barGrphWid;
            float grphH = panelHgtBase - 14*PAD;
            float interval = grphW/10;

            y -= grphH;

            // graph background color
            ITextUtils.addDirectColorRect( pdfWriter, tablePageBgColor, barGrphX, y, grphW, grphH, 0, 1f, true );

            // draw the border
            ITextUtils.addDirectBox( pdfWriter, lightFontColor, 0.8f, barGrphX, y, grphW, grphH, true );

            // draw the grid lines
            ITextUtils.addDirectVerticalGrid( pdfWriter, lightFontColor, barGrphX, y, grphW, grphH + 2, interval, true );

            float barH = 4*PAD;

            float barLly = y + (grphH - barH)/2;
            float barW = grphW * ( reportData.te.getOverallScore()/100 ) - lineW;

            fnt =  fontLarge;

            // Add Overall:  Text
            rect = new Rectangle( bxX, barLly + ( barH - fnt.getSize() )/2, barGrphX - 10, barLly + barH - ( barH - fnt.getSize() )/2 );
            ITextUtils.addDirectText(  pdfWriter, lmsg( "g.Overall", new String[] {scr}) , rect, Element.ALIGN_RIGHT, fnt.getSize(),fnt, false );

            ScoreCategoryType sct = ScoreCategoryType.getValue(reportData.getTestEvent().getOverallRating());

            BaseColor[] cols = getColorsForScoreCategoryType(sct);

            // LogService.logIt( "BaseReportTemplate.addSummaryBox() sct=" + sct.getName(Locale.US) + ", color1=" + cols[0].getRed() + "," + cols[0].getGreen() + "," + cols[0].getBlue() + ", equals=" + cols[0].equals( yellowGreenCatColor1) );

            ITextUtils.addDirectShadedRect( pdfWriter, cols[0], cols[1], barGrphX + lineW, barLly, barW, barH, 0, 0.9f, true, false );

            BaseColor c = cols[0].brighter();

            ITextUtils.addThreeSidedOutline( pdfWriter, c, barGrphX + lineW, barLly, barW, barH, 0.6f, false );

            // if have score text
            if( scrTxtH > 0 )
            {
                fnt = font;
                y -= 1.5*PAD + scrTxtH - 1.1*fnt.getSize();

                float scrTxtLlx = bxX + 2*PAD;
                float scrTxtUrx = bxX + 2*PAD + scrTxtW;

                rect = new Rectangle( scrTxtLlx, y, scrTxtUrx, scrTxtH );

                ITextUtils.addDirectText(  pdfWriter, scrTxt, rect, Element.ALIGN_LEFT, fnt.getSize(), fnt, false );
            }

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addSummarySection()" );

            throw new STException( e );
        }
    }
    
    @Override
    public long addReportToOtherTestEventId() throws Exception
    {
        return 0;
    }


    protected void addComparisonSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();
            
            if( reportData.getReport().getIncludeNorms()==1 && !otes.getHasValidNorms() )
                prepNotes.add( lmsg( "g.RptNote_NoNorms_InsufficientData" ) );


            if( reportData.getReport().getIncludeNorms()==0 || !otes.getHasValidNorms() )
                return;

            String[] names = new String[] { lmsg( "g.Overall" ), getCountryName( otes.getPercentileCountry()!=null && !otes.getPercentileCountry().isEmpty() ? otes.getPercentileCountry() : reportData.getTestEvent().getIpCountry() ), StringUtils.truncateString(reportData.getOrgName(), 22 ) };

            float[] percentiles = new float[] { otes.getPercentile(), otes.getCountryPercentile(),otes.getAccountPercentile() };

            int[] counts = new int[] { otes.getOverallPercentileCount(), otes.getCountryPercentileCount(),otes.getAccountPercentileCount() };

            //if( !otes.getHasValidOverallNorm() )
            //    prepNotes.add( lmsg( "g.RptNote_NoOverallNorms_InsufficientData" ) );

            if( !otes.getHasValidOverallNorm() )
                prepNotes.add( lmsg( otes.getHasValidOverallZScoreNorm() ? "g.RptNote_NoOverallNorms_InsufficientDataZscoreNorm" : "g.RptNote_NoOverallNorms_InsufficientData" ) );
            
            if( !otes.getHasValidAccountNorm() )
                prepNotes.add( lmsg( "g.RptNote_NoAccountNorms_InsufficientData" ) );

            if( !otes.getHasValidCountryNorm() )
                prepNotes.add( lmsg( "g.RptNote_NoCountryNorms_InsufficientData" ) );

            int validCount = 0;

            for( float p : percentiles )
            {
                if( p>0 )
                    validCount++;
            }

             if( !otes.getHasValidOverallNorm() && otes.getHasValidOverallZScoreNorm() && reportData.getTestEvent().getProduct()!=null && reportData.getTestEvent().getProduct().getConsumerProductType().getIsJobSpecific()  )
             {
                 percentiles[0]=otes.getOverallZScorePercentile();
                 counts[0]=0;
                 validCount++;
             }

            // no data to show.
            if( validCount == 0 )
            {
                // reportComparisonBoxY = reportSummaryBoxY;
                return;
            }

            Font fnt =  headerFontXLarge;

            float y = previousYLevel - 6*PAD;

            // Add Title
            ITextUtils.addDirectText( pdfWriter, lmsg("g.ComparisonPcts"), bxX + PAD, y, Element.ALIGN_LEFT, fnt, false);

            // Now create the graph
            Image img = rainbowBar;

            float sclP = 100*barGrphWid/img.getWidth();

            img.scalePercent( sclP );

            y -= img.getScaledHeight() + 2*PAD;

            // float barGrphX = bxX + ( bxWid - barGrphWid - 2*PAD ); //    bxX + 7*PAD;
            float grphY = y;

            // Ranibow image
            ITextUtils.addDirectImage( pdfWriter, img, barGrphX, y, true );

            // rainbow image outline.
            ITextUtils.addDirectBox( pdfWriter, lightFontColor, lineW, barGrphX, grphY, img.getScaledWidth(), img.getScaledHeight(), false );

            float pctBarHgt = 10;

            float pctBarGap = 1.5f*PAD;

            // Create the Grid lines.
            float grphW = barGrphWid;
            float grphH = validCount*pctBarHgt + (validCount+1)*pctBarGap;

            float interval = grphW/10;

            y -= grphH;

            // shade the back of the box
            ITextUtils.addDirectColorRect( pdfWriter, tablePageBgColor, barGrphX, y, grphW, grphH, 0, 1f, true );


            // Grid Box border
            ITextUtils.addDirectBox( pdfWriter, lightFontColor, 0.8f, barGrphX, y, grphW, grphH, true );

            //  Grid lines
            ITextUtils.addDirectVerticalGrid( pdfWriter, lightFontColor, barGrphX, y, grphW, grphH + 2, interval, true );

            // fnt =  font;

            y += grphH;

            String name;
            String countStr;
            float pct;
            float barW;
            float barLly;
            float labelLly;

            BaseColor[] cols = new BaseColor[] {barGraphCoreShade1, barGraphCoreShade2};

            int barCt = 0;

            for( int i=0; i<names.length; i++ )
            {
                fnt =  font;

                pct = percentiles[i];

                if( counts[i]< Constants.MIN_PERCENTILE_COUNT )
                    continue;

                countStr = counts[i]>=Constants.MIN_PERCENTILE_COUNT ? lmsg( "g.PercentileCountStr1" , new String[] { Integer.toString( counts[i] )} ) : lmsg( "g.PercentileCountStr0" );

                if( pct <= 0 )
                    continue;

                name = names[i] + " (" + I18nUtils.getFormattedNumber( reportData.getLocale(), pct, 0 ) + NumberUtils.getPctSuffix( reportData.getLocale(), pct, 0 ) + ")";
                barW = grphW * pct/100f - lineW;

                barCt++;

                barLly = y - barCt*(pctBarGap + pctBarHgt);

                labelLly = barLly + 1 + (pctBarHgt - fnt.getSize() )/2;

                // Add Title Text
                Rectangle rect = new Rectangle( bxX, labelLly, barGrphX - 10, labelLly + fnt.getSize() );
                ITextUtils.addDirectText(  pdfWriter, name , rect, Element.ALIGN_RIGHT, fnt.getSize(), fnt, false );

                // Add the bar
                ITextUtils.addDirectShadedRect( pdfWriter, cols[0], cols[1], barGrphX + lineW, barLly, barW, pctBarHgt, 0, 0.9f, true, false );

                ITextUtils.addThreeSidedOutline( pdfWriter, cols[0].brighter(), barGrphX + lineW, barLly, barW, pctBarHgt, 0.6f, false );

                fnt = fontSmall;

                if( pct >= 18 )
                    rect = new Rectangle( barGrphX + lineW + 2, barLly + (pctBarHgt-fnt.getSize())/2, barGrphX + lineW + barW - 3,  barLly + pctBarHgt );

                else
                    rect = new Rectangle( barGrphX + barW + 3, barLly + (pctBarHgt-fnt.getSize())/2, barGrphX + grphW - 3,  barLly + pctBarHgt );


                // Add the countStr
                ITextUtils.addDirectText(  pdfWriter, countStr , rect, pct >= 18 ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT, fnt.getSize(), fnt, false );

            }

            currentYLevel = y - grphH - 2*PAD;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addComparisonSection()" );

            throw new STException( e );
        }
    }

    

    protected String getCountryName( String countryCode )
    {
        if( countryCode == null || countryCode.isEmpty() )
            countryCode = "US";

        String c = lmsg( "cntry." + countryCode );

        if( c == null || c.isEmpty() )
            return lmsg( "g.Country" );

        return c;
    }


    /**
     * Returns true if added a new page.
     *
     * @return
     * @throws Exception
     */
    protected boolean addAssessmentOverview() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( reportData.getReport().getIncludeOverviewText()==0 )
                return false;

            currentYLevel = 0;

            String ovrTxt = reportData.getReportOverviewText(); //  ScoreFormatUtils.getDescripFromTextParam( reportData.getTestEvent().getOverallTestEventScore().getTextParam1() );
            // String ovrTxt = reportData.getTestEvent().getOverallTestEventScore().getTextParam1();

            // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() " + ovrTxt );

            if( ovrTxt == null || ovrTxt.isEmpty() )
                return false;

            Font fnt =  headerFontXLarge;

            float y = previousYLevel - 6*PAD - font.getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, lmsg("g.AssessmentOverview"), bxX + PAD, y, Element.ALIGN_LEFT, fnt, false);

            // Change font
            fnt =  font;

            float spaceLeft = y - 2*PAD - footerHgt;

            float leading = fnt.getSize();

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, ovrTxt, bxWid, Element.ALIGN_LEFT, leading, fnt);

             y -= headerFontXLarge.getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            float txtW = bxWid - 4*PAD;

            float txtLlx = bxX + 2*PAD;
            float txtUrx = bxX + 2*PAD + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( txtHght <= spaceLeft )
            {


                Rectangle rect = new Rectangle( txtLlx, y, txtUrx, txtHght );

                ITextUtils.addDirectText(  pdfWriter, ovrTxt, rect, Element.ALIGN_LEFT, leading, fnt, false );

                return false;
            }

            else
            {
                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                Phrase p = new Phrase( ovrTxt, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() adding second column "  );

                    document.newPage();


                    ct.setSimpleColumn( colDims2.getLeft(), colDims2.getBottom(), colDims2.getRight(), colDims2.getTop() );

                    ct.setYLine( colDims2.getTop() );

                    status = ct.go();

                    currentYLevel = ct.getYLine();
                }


                return true;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addAssessmentOverview()" );

            throw new STException( e );
        }
    }



    protected void addDetailedReportInfoHeader() throws Exception
    {
        try
        {

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.Detail" ) );

            y -= 3*PAD;

            Font fntRgt = fontXLarge;
            Font fntRgt2 = font;
            Font fntLft = fontLight;

            float x = bxX + PAD;

            // Now, let's create a table!
            PdfPTable t = new PdfPTable( 2 );

            t.setTotalWidth( bxWid );
            t.setLockedWidth( true );
            t.setWidths( new float[] {5,29} );


            t.setHorizontalAlignment( Element.ALIGN_CENTER );

            PdfPCell dc = t.getDefaultCell();
            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setVerticalAlignment( Element.ALIGN_BOTTOM );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding( 2 );

            Phrase p = new Phrase( lmsg( "g.CandidateC"), fntLft );
            t.addCell( p );

            Chunk c = new Chunk( reportData.u.getFullname() + ", ", fntRgt );
            p = new Phrase();
            p.add( c );
            c = new Chunk( reportData.u.getUserType().getNamed() ? reportData.u.getEmail() : "", fntRgt2 );
            p.add( c );
            t.addCell( p );

            t.addCell( new Phrase( lmsg( "g.AssessmentC"), fntLft ) );
            t.addCell( new Phrase( reportData.getSimName(), fntLft ) );

            String[] params = new String[] { I18nUtils.getFormattedDate(reportData.getLocale(), reportData.getTimeZone(), reportData.getTestKey().getStartDate() ),
                                             reportData.getTestKey().getAuthUser() == null ? "" : reportData.getTestKey().getAuthUser().getFullname(),
                                             reportData.getOrgName(),
                                             reportData.getTestKey().getAuthUser() == null ? "" : reportData.getTestKey().getAuthUser().getEmail() };

            String auth = reportData.getTestKey().getAuthUser() == null ? lmsg( "g.AuthStr" , params ) : lmsg( "g.AuthStrCombined" , params );

            t.addCell( new Phrase( lmsg( "g.AuthorizedC"), fntLft ) );
            t.addCell( new Phrase( auth, fntLft ) );

            t.addCell( new Phrase( lmsg( "g.StartedC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getStartDate(), reportData.getUser().getTimeZone() ), fntLft ) );

            t.addCell( new Phrase( lmsg( "g.CompletedC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getLastAccessDate(), reportData.getUser().getTimeZone() ), fntLft ) );

            t.addCell( new Phrase( lmsg( "g.OverallScoreC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedNumber( reportData.getLocale(), reportData.getTestEvent().getOverallScore(), 0), fntLft ) );

            t.writeSelectedRows(0, -1, x + PAD, y, pdfWriter.getDirectContent() );

            // LogService.logIt( "BaseReportTemplate.addDetailedReportHeader() t.calculateHeights()=" + t.calculateHeights() + ", currentY=" + y );

            currentYLevel = y - t.calculateHeights();

            // LogService.logIt( "BaseReportTemplate.addDetailedReportHeader() currentYLevel=" + currentYLevel );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addDetailedReportInfoHeader()" );

            throw new STException( e );
        }
    }



    protected void addCompetencyInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            SimCompetencyClass scc;

            for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                // LogService.logIt( "BaseReportTemplate.addCompetencyInfo() tes=" + tes.toString() );

                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                if( !scc.getIsDirectCompetency() && !scc.getIsAggregate() )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            addAnyCompetenciesInfo( tesl, "g.Competencies", "g.CompetencyDetail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addCompetencyInfo()" );

            throw new STException( e );
        }
    }



    protected void addTasksInfo() throws Exception
    {
        try
        {
            if( !reportData.includeTaskInfo() )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            SimCompetencyClass scc;

            for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                if( !scc.getIsTask() )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            addAnyCompetenciesInfo( tesl, "g.Tasks", "g.TaskDetail", "g.TaskDescription", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addTasksInfo()" );

            throw new STException( e );
        }
    }


    protected void addBiodataInfo() throws Exception
    {
        try
        {
            if( !reportData.includeBiodataInfo() )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            SimCompetencyClass scc;

            for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                if( !scc.getIsBiodata()) // && !scc.getIsAggregate() )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            addAnyCompetenciesInfo( tesl, "g.ScoredBehaviors", "g.ScoredBehaviorsDetail", "g.Description", "g.BiodataCaveatHeader", "g.BiodataCaveatFooter", reportData.includeInterview() ? true : false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addBiodataInfo()" );

            throw new STException( e );
        }
    }

    protected void addInterestInfo() throws Exception
    {
        try
        {
            if( !reportData.includeTaskInterestInfo() )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            SimCompetencyClass scc;

            for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                if( !scc.getIsInterest()) // && !scc.getIsAggregate() )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            addAnyCompetenciesInfo( tesl, "g.ScoredInterests", "g.ScoredInterestsDetail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addInterestInfo()" );

            throw new STException( e );
        }
    }


    protected void addExperienceInfo() throws Exception
    {
        try
        {
            if( !reportData.includeTaskExperienceInfo() )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            SimCompetencyClass scc;

            for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                if( !scc.getIsExperience()) // && !scc.getIsAggregate() )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            addAnyCompetenciesInfo( tesl, "g.ScoredExperience", "g.ScoredExperienceDetail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addExperienceInfo()" );

            throw new STException( e );
        }
    }






    protected void addAnyCompetenciesInfo( java.util.List<TestEventScore> teslst,
            String titleKey,
            String detailKey,
            String descripKey,
            String caveatHeaderKey,
            String caveatFooterKey,
            boolean withInterview,
            boolean noInterviewLimit ) throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            int interviewQsPerComp = reportData.getReport().getMaxInterviewQuestionsPerCompetency();
            boolean numeric = reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean rating = reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean norms = reportData.getReport().getIncludeSubcategoryNorms()==1;
            boolean interpretation = reportData.getReport().getIncludeSubcategoryInterpretations()==1;

            LogService.logIt( "BaseReportTemplate.addAnyCompetenciesInfo() interviewQsPerComp=" + interviewQsPerComp + ", numeric=" + numeric + ", rating=" + rating );

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            for( TestEventScore tes : teslst )
            {
                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                // skip non-auto-scored competencies.
                if( tes.getScore()<0 )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            // Collections.sort( tesl, new TestEventScoreWeightNameComparator() );

            float y = addTitle( previousYLevel, lmsg( titleKey ) );

            // LogService.logIt( "BaseReportTemplate.addAnyCompetenciesInfo() BBB"  );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = bxWid*0.95f;

            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.BOX );
            c.setBorderWidth( 0.5f );
            c.setPadding( 2 );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            c.setBorderColor( BaseColor.DARK_GRAY );


            // Add header row.
            t.addCell( new Phrase( lmsg( detailKey ) , font) );
            t.addCell( new Phrase( lmsg( withInterview ? "g.InterviewGuide" : descripKey ) , font) );

            c.setBackgroundColor( BaseColor.WHITE );

            PdfPTable compT;
            PdfPTable igT;
            ScoreCategoryType sct;
            Image dotImg;

            java.util.List<InterviewQuestion> igL;
            InterviewQuestion ig;

            Phrase ep = new Phrase( "", fontSmall );

            Phrase p;

            String scoreText;

            // String caveatText;

            // For each competency
            for( TestEventScore tes : tesl )
            {
                // LogService.logIt( "BaseReportTemplate.addAnyCompetenciesInfo() Starting competency " + tes.getName()  );

                // First do the score info.
                compT = new PdfPTable( new float[] {2,10} );
                compT.setHorizontalAlignment( Element.ALIGN_CENTER );
                compT.setTotalWidth( 0.9f*outerWid*3.5f/9f );
                compT.setLockedWidth(true);

                c = compT.getDefaultCell();
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 2 );

                // Dot Cell
                sct = tes.getScoreCategoryType();
                dotImg = getColorImg( sct );
                c = rating ?  new PdfPCell( dotImg ) : new PdfPCell( new Phrase( "" , fontSmall ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setVerticalAlignment( interpretation ? Element.ALIGN_TOP : Element.ALIGN_MIDDLE );
                c.setPadding( 2 );

                if( interpretation )
                    c.setPaddingTop( 10 );
                // c.addElement( dotImg );
                compT.addCell( c );

                p = new Phrase();

                // Name and Score Cell
                p.add( new Chunk(  reportData.getCompetencyName(tes)  + "\n" , getFontBold() ) );

                p.add( new Chunk( numeric ? lmsg( "g.ScoreC") : "", getFontSmallBold() )  );
                p.add( new Chunk( numeric ? " " + I18nUtils.getFormattedNumber( reportData.getLocale(), tes.getScore(), 0 ) : "" , getFontSmall() ) );

                if( norms && tes.getHasValidOverallNorm() )
                {
                    p.add( new Chunk( "\n" + lmsg( "g.PercentileC" ) , getFontSmallBold() )  );
                    p.add( new Chunk( " " + NumberUtils.getPctSuffixStr( reportData.getLocale(), tes.getPercentile(), 0 ), getFontSmall() )  );
                }

                if( interpretation )
                {
                    // p.add( new Chunk( "\n" + lmsg( "g.InterpretationC" ) + " " , getFontSmallBold() )  );
                    p.add( new Chunk( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), getFontSmall() )  );
                }

                c = new PdfPCell();
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding( 4 );
                c.addElement( p );

                if( interpretation )
                {
                    p = new Phrase();
                    p.setLeading( 10 );
                    // p.add( new Chunk( "\n" + lmsg( "g.InterpretationC" ) + " " , getFontSmallBold() )  );
                    p.add( new Chunk( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), getFontSmall() )  );
                    c.addElement( p );
                }

                c.setPaddingTop( 8 );

                compT.addCell( c );

                scoreText = reportData.getCompetencyScoreText( tes ); // tes.getScoreText();

                if( scoreText == null )
                    scoreText = "";

                // caveatText = "";

                List cl = new com.itextpdf.text.List( List.UNORDERED, 12 );
                Paragraph cHdr=null;
                Paragraph cFtr=null;
                float spcg = 8;
                cl.setListSymbol( "\u2022");

                Phrase cst = new Phrase( new Phrase( scoreText, fontSmall ) );
                cst.setLeading( 10 );

                for( String ct : reportData.getCaveatList(tes) )
                {
                    if( ct.isEmpty() )
                        continue;

                    cl.add( new ListItem( new Paragraph( ct , fontSmall ) ) );
                }

                if( cl.size()>0 ) //  !caveatText.isEmpty() )
                {
                    if( caveatHeaderKey != null && !caveatHeaderKey.isEmpty() )
                    {
                        cHdr = new Paragraph( lmsg( caveatHeaderKey ) , fontSmall );
                        cHdr.setLeading( 10 );
                        cHdr.setSpacingBefore( spcg );
                        cHdr.setSpacingAfter( spcg );
                    }

                    if( caveatFooterKey != null && !caveatFooterKey.isEmpty() )
                    {
                        cFtr = new Paragraph( lmsg( caveatFooterKey ) , fontSmall );
                        cFtr.setLeading( 10 );
                        cFtr.setSpacingBefore( spcg );
                    }
                }

                // ScoreText Cell
                c = new PdfPCell(); // new PdfPCell( new Phrase( scoreText, fontSmall )  );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setColspan(2);
                c.setPaddingTop( 10 );
                c.addElement( cst );
                if( cl.size()>0 )
                {
                    c.addElement( cHdr );
                    c.addElement( cl );
                    c.addElement( cFtr );
                }
                compT.addCell(c);

                // add to table.
                c = new PdfPCell();
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding(3);
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorderColor( BaseColor.DARK_GRAY );
                c.addElement( compT );
                t.addCell( c );

                if( withInterview )
                {

                    // next, the interview guide
                    igT = new PdfPTable( 15 );
                    igT.setTotalWidth( 0.9f * outerWid*5.5f/9f );
                    igT.setLockedWidth(true);
                    igT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c = igT.getDefaultCell();
                    c.setBackgroundColor( BaseColor.WHITE );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setPadding( 2 );

                    int iqs = 0;

                    int maxInt = noInterviewLimit ? MAX_INTERVIEWQS_PER_COMPETENCY : interviewQsPerComp;

                    maxInt = Math.min(maxInt, MAX_INTERVIEWQS_PER_COMPETENCY);

                    igL = reportData.getInterviewQuestionList(tes, maxInt); // tes.getInterviewQuestionList( maxInt );

                    // LogService.logIt( "BaseReportTemplate.addAnyCompetenciesInfo() interview question list size=" + igL.size() );

                    for( int i=0; i<maxInt && i<igL.size(); i++ )
                    {
                        if( i >= igL.size() )
                            break;

                        // need a boundary
                        if( i > 0 )
                        {
                            LineSeparator ls = new LineSeparator( 1,90,BaseColor.BLACK,Element.ALIGN_CENTER, 0 );
                            c = new PdfPCell();
                            c.addElement( ls );
                            c.setBorder( Rectangle.NO_BORDER );
                            c.setBackgroundColor( BaseColor.WHITE );
                            c.setColspan( 15 );
                            c.setPadding(6);
                            c.setPaddingBottom( 10 );
                            igT.addCell(c);
                        }

                        ig = igL.get(i);

                        // ROW 1 - the question
                        c = new PdfPCell( new Phrase( ig.getQuestion(), fontSmall ) );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setColspan( 15 );
                        c.setPadding(2);
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);

                        // Row 2 - Color Dots
                        igT.addCell(ep);
                        c =  new PdfPCell( redDot );
                        // c.addElement(redDot);
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( redYellowDot );
                        // c.addElement( redYellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell(yellowDot );
                        // c.addElement( yellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell(yellowGreenDot );
                        // c.addElement( yellowGreenDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell(greenDot );
                        // c.addElement( greenDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        // ROW 3 - numbers
                        c = igT.getDefaultCell();
                        c.setHorizontalAlignment( Element.ALIGN_CENTER);
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setPadding( 0 );

                        igT.addCell(ep);
                        igT.addCell( new Phrase( "1", fontSmall ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "2", fontSmall ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "3", fontSmall ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "4", fontSmall ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "5", fontSmall ) );
                        igT.addCell(ep);

                        c = igT.getDefaultCell();
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setPadding( 2 );
                        c.setPaddingBottom( 10 );


                        // row 4 - anchors
                        c = new PdfPCell( new Phrase( ig.getAnchorLow(), fontSmall ) );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 4 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( ig.getAnchorMed(), fontSmall ) );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 5 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( ig.getAnchorHi(), fontSmall ) );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 4 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);

                        iqs++;
                    }

                    if( iqs >= 0 )
                    {
                        c = new PdfPCell();
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setBorder( Rectangle.BOX );
                        c.setBorderWidth( 0.5f );
                        c.setPadding( 10 );
                        c.addElement( igT );
                        t.addCell( c );
                    }

                    //else
                    //{
                    //    LogService.logIt( "BaseReportTemplate.addAnyCompetenciesInfo() Adding dummy Interview guide section - no questions found" );

                    //}

                } // with interview

                // else, with description
                else
                {
                    String d = reportData.getCompetencyDescription( tes ); // ScoreFormatUtils.getDescripFromTextParam( tes.getTextParam1() );
                    c = new PdfPCell( new Phrase( d == null ? "" : d, fontSmall ) );
                    // c = new PdfPCell( new Phrase( tes.getTextParam1() == null ? "" : tes.getTextParam1(), fontSmall ) );
                    c.setBackgroundColor( BaseColor.WHITE );
                    c.setBorder( Rectangle.BOX );
                    c.setBorderWidth( 0.5f );
                    c.setPadding( 6 );
                    t.addCell( c );
                }
            } // each competency

            currentYLevel = addTableToDocument( y, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addAnyCompetenciesInfo() titleKey=" + titleKey );

            throw new STException( e );
        }
    }





    protected void addWritingSampleInfo() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( reportData.getReport().getIncludeWritingSampleInfo()==0 )
                return;

            java.util.List<TextAndTitle> ttl = ScoreFormatUtils.getNonCompTextListTable( reportData.getTestEvent(), NonCompetencyItemType.WRITING_SAMPLE );

            if( ttl.isEmpty() )
                return;

            float y = addTitle( previousYLevel, lmsg( "g.WritingSample" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = bxWid*0.95f;

            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            // t.setSplitLate( false );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.BOX );
            c.setBorderWidth( 0.5f );
            c.setPadding( 2 );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            c.setBorderColor( BaseColor.DARK_GRAY );

            // Add header row.
            t.addCell( new Phrase( lmsg( "g.WritingSampleQuestion" ) , font) );
            t.addCell( new Phrase( lmsg( "g.Response" ) , font) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", fontSmall );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), fontSmall ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), fontSmall ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

            } // each writing sample

            currentYLevel = addTableToDocument( y, t );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addWritingSampleInfo()" );

            throw new STException( e );
        }
    }

    protected void addMinQualsApplicantDataInfo() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( !reportData.includeApplicantData() && !reportData.includeMinQuals() )
                return;

            java.util.List<TextAndTitle> ttl = new ArrayList<>();

            if( reportData.includeApplicantData() )
                ttl.addAll( ScoreFormatUtils.getNonCompTextListTable(reportData.getTestEvent(), NonCompetencyItemType.APPLICANT_INFO ) );

            if( reportData.includeMinQuals() )
                ttl.addAll( ScoreFormatUtils.getNonCompTextListTable( reportData.getTestEvent(), NonCompetencyItemType.MIN_QUALS ) );

            if( ttl.isEmpty() )
                return;

            float y = addTitle( previousYLevel, lmsg( "g.AppDataAndMinQuals" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = bxWid*0.95f;

            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.BOX );
            c.setBorderWidth( 0.5f );
            c.setPadding( 2 );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            c.setBorderColor( BaseColor.DARK_GRAY );

            // Add header row.
            t.addCell( new Phrase( lmsg( "g.Item" ) , font) );
            t.addCell( new Phrase( "" , font) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", fontSmall );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), fontSmall ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), fontSmall ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

            } // each writing sample

            currentYLevel = addTableToDocument( y, t );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addMinQualsApplicantDataInfo()" );

            throw new STException( e );
        }
    }




    protected void addPreparationNotesSection() throws Exception
    {
        try
        {
            if( prepNotes.isEmpty() )
                return;

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.PreparationNotes" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 1f } );

            float outerWid = bxWid*0.95f;

            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
            //Paragraph cHdr=null;
            //Paragraph cFtr=null;
            //float spcg = 8;
            cl.setListSymbol( "\u2022");

            for( String ct : prepNotes )
            {
                if( ct.isEmpty() )
                    continue;

                cl.add( new ListItem( new Paragraph( ct , getFont() ) ) );
            }

            c = new PdfPCell();
            c.setBorder( Rectangle.BOX );
            c.setBackgroundColor( BaseColor.WHITE );
            c.setBorderColor( BaseColor.DARK_GRAY );
            c.setPaddingTop( 8 );
            c.setPaddingLeft(10);
            c.setPaddingRight(5);
            c.setPaddingBottom( 14 );
            c.addElement( cl );
            t.addCell(c);

            currentYLevel = addTableToDocument( y, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addPreparationNotesSection()" );

            throw new STException( e );
        }
    }



    protected void addNotesSection() throws Exception
    {
        addTitle( 0 , lmsg("g.Notes"));
    }





    protected void addCoverPage() throws Exception
    {
        try
        {
            float y = pageHeight - hraLogoBlackText.getScaledHeight() - 20;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

            ITextUtils.addDirectImage( pdfWriter, hraLogoBlackText, 20, y, false );

            Font fnt = fontLargeLight;

            y += hraLogoBlackText.getScaledHeight() - fnt.getSize()*2f;

            float x = 20+ pageWidth/2;
            float x2 = x + 70;

            // y += hraLogoBlackText.getScaledHeight();

            ITextUtils.addDirectText( pdfWriter, lmsg( "g.CandidateC" ), x, y, Element.ALIGN_LEFT, fnt, false );

            fnt = fontXLargeLightBold;
            ITextUtils.addDirectText( pdfWriter, reportData.getUserName(), x2, y, Element.ALIGN_LEFT, fnt, false );

            fnt = fontLargeLight;
            y -= fnt.getSize()*1.2;
            ITextUtils.addDirectText( pdfWriter, lmsg( "g.AssessmentC" ), x, y, Element.ALIGN_LEFT, fnt, false );
            ITextUtils.addDirectText( pdfWriter, reportData.getSimName(), x2, y, Element.ALIGN_LEFT, fnt, false );
            // addDirectText( MessageFactory.getStringMessage( reportData.getLocale() , "g.CandidateC" ), x, y - 50, baseFontCalibri, 16, lightFontColor, false );

            y -= fnt.getSize()*1.2;
            ITextUtils.addDirectText( pdfWriter, lmsg( "g.CompletedC" ), x, y, Element.ALIGN_LEFT, fnt, false );
            ITextUtils.addDirectText( pdfWriter, reportData.getSimCompleteDateFormatted(), x2, y, Element.ALIGN_LEFT, fnt, false );

            // fnt = getFontXLargeLightBold();
            if( reportData.getTestKey()!=null && reportData.getTestKey().getAuthUser() != null && reportData.getTestKey().getAuthUser().getUserId()!= reportData.getTestEvent().getUserId() )
            {
                y -= fnt.getSize()*2.2;
                ITextUtils.addDirectText( pdfWriter, lmsg( "g.PreparedForC" ), x, y, Element.ALIGN_LEFT, fnt, false );
                ITextUtils.addDirectText( pdfWriter, reportData.getTestKey().getAuthUser().getFullname(), x2, y, Element.ALIGN_LEFT, fnt, false );
            }


            if( reportData.hasCustLogo() && custLogo!=null )
            {
                 y -= fnt.getSize()*1.00f;
                 ITextUtils.addDirectImage( pdfWriter, custLogo, x2, y - custLogo.getScaledHeight(), false );
            }

            else if( reportData.getTestKey().getAuthorizingUserId()>0 )
            {
                fnt = getFontXLargeLightBold();
                y -= fnt.getSize()*1.2;
                ITextUtils.addDirectText( pdfWriter, reportData.getOrgName(), x2, y, Element.ALIGN_LEFT, fnt, false );
            }

            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, hraOrangeColor, false );

            ITextUtils.addDirectColorRect( pdfWriter, hraBaseReportColor, 0, 0, pageWidth, pageHeight/2, 0, 1, true );

            fnt = headerFontXXLargeWhite;
            y = 0.35f*pageHeight;

            // Rectangle rect = new Rectangle( x, y, x+230, 3*1.1f*fnt.getSize() );
            Rectangle rect = new Rectangle( x, 20, x+230, y  );
            ITextUtils.addDirectText( pdfWriter, title, rect, Element.ALIGN_LEFT, fnt.getSize() + 1, fnt, false );
            // ITextUtils.addDirectText( pdfWriter, reportData.getReportName(), rect, Element.ALIGN_LEFT, fnt.getSize() + 1, fnt, false );



            fnt = fontWhite;

            rect = new Rectangle( 1, 5, pageWidth, fnt.getSize() + 10 );

            ITextUtils.addDirectText( pdfWriter, lmsg( "g.ProprietaryAndConfidential" ), rect, Element.ALIGN_CENTER, 0, fnt, false );


        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseReportTemplate.addCoverPage()" );
        }
    }




    protected float addTitle( float startY, String title ) throws Exception
    {
            Font fnt = headerFontXLarge;

            if( startY > 0 )
            {
                float ulY = startY - 15* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }

            float y = startY>0 ? startY - fnt.getSize() - 4*PAD :  pageHeight - headerHgt - fnt.getSize() - 4*PAD;

            float x = bxX + PAD;

            ITextUtils.addDirectText( pdfWriter, title, x, y, Element.ALIGN_LEFT, fnt, false );

            return y;
    }

    /*
    protected float addTableToDocument( float startY, PdfPTable t ) throws Exception
    {
            float ulY = startY - 4* PAD;

            if( ulY < footerHgt + 3*PAD )
            {
                LogService.logIt( "BaseReportTemplate.addTableToDocument() creating a new page." );

                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
            }

            float tableHeight = t.calculateHeights() + 500;
            float headerHeight = t.getHeaderHeight();

            LogService.logIt( "BaseReportTemplate.addTableToDocument() tableHeight=" + tableHeight + ", headerHeight=" + headerHeight );

            Rectangle colDims = new Rectangle( bxX, footerHgt + 3*PAD, bxX + bxWid, ulY );
            // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

            float heightNoHeader = tableHeight - headerHeight;
            float heightUsedNoHeader = colDims.getTop() - colDims.getBottom() - headerHeight;

            ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

            ct.addElement( t );

            ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );
            // ct.setSimpleColumn( colDims1 );

            int status = ct.go();

            while( ColumnText.hasMoreText( status ) )
            {
                ulY = pageHeight - headerHgt - 3*PAD;

                float heightAvailNewPage = pageHeight - headerHgt - 3*PAD - footerHgt - 3*PAD - headerHeight;

                float heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

                // if have enough height in next column
                if( heightNeededNoHeader <= heightAvailNewPage )
                {
                    startY = ulY - heightNeededNoHeader;
                    colDims = new Rectangle( bxX, startY , bxX + bxWid, ulY );
                    heightUsedNoHeader += heightNeededNoHeader;

                }

                // else need entire next page
                else
                {
                    colDims = new Rectangle( bxX, footerHgt + 3*PAD , bxX + bxWid, ulY );
                    heightUsedNoHeader += heightAvailNewPage;
                }
                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() adding second column "  );

                document.newPage();

                ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );

                ct.setYLine( colDims.getTop() );

                status = ct.go();
            }

            return ct.getYLine();
    }
    */




    public float addTableToDocument( float startY, PdfPTable t ) throws Exception
    {
            float ulY = startY - 4* PAD;

            float tableHeight = t.calculateHeights(); //  + 500;
            float headerHeight = t.getHeaderHeight();

            int rowCount = t.getRows().size(); //  - t.getHeaderRows() - t.getFooterRows();

            float maxRowHeight=0;

            float[] rowHgts = new float[rowCount];

            for( int i=0; i<rowCount; i++ )
            {
                rowHgts[i]=t.getRowHeight(i);
                maxRowHeight = Math.max( maxRowHeight, rowHgts[i] );
                // LogService.logIt( "BaseReportTemplate.addTableToDocument() row=" + i + ", rowHeight=" + rowHgts[i] );
            }

            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;

            float heightAvailNewPage = pageHeight - headerHgt - 3*PAD - footerHgt - 3*PAD - headerHeight;

            if( maxRowHeight >= heightAvailNewPage*0.5 )
                t.setSplitLate(false);

            // If first row doesn't fit on this page
            else if( firstRowHgt > ulY- footerHgt - 3*PAD - headerHeight ) // ulY < footerHgt + 8*PAD )
            {
                // LogService.logIt( "BaseReportTemplate.addTableToDocument() adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
            }


            //if( maxRowHeight > usablePageHeight )
            //    t.setSplitLate(false);


            Rectangle colDims = new Rectangle( bxX, footerHgt + 3*PAD, bxX + bxWid, ulY );
            // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

            float heightNoHeader = tableHeight - headerHeight;


            Object[] dta = calcTableHghtUsed( colDims.getTop() - colDims.getBottom() - headerHeight, 0, t.getHeaderRows(), rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
            int nextIndex = (Integer) dta[0];
            float heightUsedNoHeader = (Float) dta[1];
            float residual = (Float) dta[2];

            // LogService.logIt( "BaseReportTemplate.addTableToDocument() tableHeight=" + t.calculateHeights() + ", headerHeight=" + headerHeight + ", maxRowHeight=" + maxRowHeight + ", heightAvailNewPage=" + heightAvailNewPage + ", initial heightUsedNoHeader=" + heightUsedNoHeader + ", residual=" + residual );


            ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

            // NOTE - this forces Composite mode (using ColumnText.addElement)
            ct.addElement( t );

            ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );
            // ct.setSimpleColumn( colDims1 );


            int status = ct.go();

            // int linesWritten = ct.getLinesWritten();

            // LogService.logIt( "BaseReportTemplate.addTableToDocument() initial lines written. NO_MORE_COLUMN=" + ColumnText.NO_MORE_COLUMN + ", NO_MORE_TEXT=" + ColumnText.NO_MORE_TEXT  );

            int pages = 0;

            float heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

            float hgtUsedThisPage = 0;

            // If need to add any pages
            while( ColumnText.hasMoreText( status ) && heightNeededNoHeader>0 && pages<10 )
            {
                // Top of writable area
                ulY = pageHeight - headerHgt - 3*PAD;


                dta = calcTableHghtUsed( heightAvailNewPage, residual, nextIndex, rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
                nextIndex = (Integer) dta[0];
                hgtUsedThisPage = (Float) dta[1];
                residual = (Float) dta[2];

                heightUsedNoHeader += hgtUsedThisPage;

                heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

                // LogService.logIt( "BaseReportTemplate.addTableToDocument() AFTER adding next page. hgtUsedThisPage=" + hgtUsedThisPage +  ", Total HeightNeededNoHeader=" + heightNeededNoHeader + ", Total HeightUsedNoHeader=" + heightUsedNoHeader  );

                colDims = new Rectangle( bxX, ulY - heightAvailNewPage , bxX + bxWid, ulY );

                document.newPage();

                ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );

                ct.setYLine( colDims.getTop() );

                status = ct.go();

                // linesWritten += ct.getLinesWritten();

                // LogService.logIt( "BaseReportTemplate.addTableToDocument() status=" + status  );

                pages++;
            }

            return ct.getYLine();
    }



    /**
     * Returns
     *    Next Index -- if three is a residual, it's the index of the residual, else it's the next index
     *    Amount of height used
     *    Residual height unused from split cell
     *
     * @param maxRoom
     * @param startIndex
     * @param maxIndex
     * @param isSplitLate
     * @param rowHgts
     * @return
     */
    public Object[] calcTableHghtUsed( float maxRoom, float prevResidual, int startIndex, int maxIndex, boolean isSplitLate, float[] rowHgts )
    {
        // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed( maxRoom=" + maxRoom + ", prevResidual=" + prevResidual + ", startIndex=" + startIndex + ", maxIndex=" + maxIndex + ", isSplitLate=" + isSplitLate + ", " + ")");

        Object[] dta = new Object[] {Integer.valueOf(startIndex), Float.valueOf(0), Float.valueOf(0)};

        if( rowHgts.length<=startIndex )
            return dta;

        float hgt = 0;
        float resid = 0;

        if( prevResidual>0 )
        {
            // Bigger than max
            if( prevResidual>= maxRoom )
            {
                dta[1] = maxRoom;
                dta[2] = prevResidual -  maxRoom;

                if( prevResidual== maxRoom)
                {
                    dta[0]=startIndex+1;
                    dta[2] = Float.valueOf(0);
                }

                return dta;
            }

            hgt = prevResidual;
            maxRoom -= prevResidual;
            startIndex++;
        }

        for( int i=startIndex; i<rowHgts.length && i<=maxIndex; i++ )
        {
            if( rowHgts[i] + hgt == maxRoom )
            {
                dta[0]=Integer.valueOf(i+1);
                dta[1] = hgt;
                return dta;
            }

            if( rowHgts[i] + hgt > maxRoom )
            {
                if( i==startIndex || !isSplitLate )
                {
                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() AAA i=" + i + ", hgt=" + hgt );

                    resid = rowHgts[i] - (maxRoom-hgt);
                    dta[2] = resid;
                    hgt = maxRoom;

                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() BBB hgt=" + hgt + ", resid=" + resid );

                }

                dta[0] = Integer.valueOf(i);
                dta[1] = hgt;
                return dta;
            }

            hgt += rowHgts[i];
        }

        dta[0] = Integer.valueOf(maxIndex+1);
        dta[1] = hgt;
        return dta;
    }





    void addNewPage() throws Exception
    {
        document.newPage();
    }



    protected String lmsg( String key )
    {
        return lmsg( key, null );
    }

    protected String lmsg( String key,String[] prms )
    {
        return MessageFactory.getStringMessage( reportData.getLocale() , key, prms );
    }
}
