/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_MARGIN;
import static com.tm2score.custom.coretest2.CT2ReportSettings.CT2_TEXT_EXTRAMARGIN;
import com.tm2score.entity.event.TestEvent;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.purchase.Product;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TESNameComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.ScoreFormatUtils;
import static com.tm2score.format.StandardReportSettings.MAX_CUSTLOGO_H;
import com.tm2score.format.TableBackground;
import com.tm2score.format.TestEventScoreWeightNameComparator;
import com.tm2score.global.Constants;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.STException;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.EducType;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.RelatedExperType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.sim.TrainingType;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 *
 * @author Mike
 */
public abstract class BaseCoreTestReportTemplate extends CTReportSettings implements ReportTemplate
{

    Image custLogo = null;

    ReportData reportData = null;
    CTReportData ctReportData = null;

    Document document = null;

    ByteArrayOutputStream baos;

    PdfWriter pdfWriter;

    float pageWidth = 0;
    float pageHeight = 0;
    float usablePageHeight = 0;

    String title;

    float headerHgt;
    float footerHgt;

    float lastY = 0;

    TableBackground dataTableEvent;
    TableBackground tableHeaderRowEvent;

    ReportUtils reportUtils;

    float PAD = 5;

    float bxX;
    float bxWid;
    float barGrphWid;
    float barGrphX;
    float lineW = 0.8f;


    float currentYLevel = 0;
    float previousYLevel = 0;

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
                    LogService.logIt( e, "BaseCoreTestReportTemplate.initFonts() NONFATAL error getting custLogo. Will try http instead of https. logo=" + logoUrl );

                    String logo2 = "http:" + logoUrl.trim().substring(6, logoUrl.length());

                    try
                    {
                        custLogo = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( logo2 ) );                    
                    }
                    catch( Exception ee )
                    {
                        LogService.logIt( ee, "BaseCoreTestReportTemplate.initFonts() NONFATAL error getting custLogo using http. Will use null. logo=" + logoUrl + ", logo2=" + logo2 );                    
                    }                
                }            

                else
                    LogService.logIt( e, "BaseCoreTestReportTemplate.initFonts() NONFATAL error getting custLogo. Will use null. logo=: " + reportData.getCustLogoUrl() );        
            }

            else
                LogService.logIt( e, "BaseCoreTestReportTemplate.initFonts() NONFATAL error getting custLogo. Will use null. logo=: " + reportData.getCustLogoUrl() );        
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

        // LogService.logIt( "BaseCoreTestReportTemplate.initFonts() title=" + title );
    }


    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CTReportData();

        initFonts();

        initColors();

        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        CTHeaderFooter hdr = new CTHeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );

        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );

        headerHgt = hghts[0];
        footerHgt = hghts[1];

        usablePageHeight = pageHeight - headerHgt - footerHgt - 4*PAD;

        bxX = 5*PAD;
        bxWid = pageWidth - 10*PAD;
        barGrphWid = 0.77f*bxWid;
        barGrphX = bxX + ( bxWid - barGrphWid - 2*PAD ); //    bxX + 7*PAD;

        // dataTableWidth = hdr.headerW;

       // document.setMargins( document.leftMargin(), document.rightMargin(), 42, 42 );

        LogService.logIt( "BaseCoreTestReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

        tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );
    }

    @Override
    public long addReportToOtherTestEventId() throws Exception
    {
        return 0;
    }

    
    
    @Override
    public Locale getReportLocale()
    {
        if( this.reportData!=null )
            return reportData.getLocale();
        
        return Locale.US;
    }

    
    
    @Override
    public void dispose() throws Exception
    {
        if( baos != null )
            baos.close();
    }


    @Override
    public String getReportGenerationNotesToSave()
    {
        return null;
    }
    
    

    public void closeDoc() throws Exception
    {
        if( document != null && document.isOpen() )
            document.close();

        document = null;
    }


    public byte[] getDocumentBytes() throws Exception
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



    public void addReportInfoHeader() throws Exception
    {
        try
        {
            Font fnt = getFontXLarge();

            float y = pageHeight - headerHgt - fnt.getSize() - 4*PAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            float x = bxX + PAD;
            float x2 = 100 + pageWidth/2;

            // y += getHraLogoBlackText().getScaledHeight();

            ITextUtils.addDirectText( pdfWriter, reportData.getSimName(), x, y, Element.ALIGN_LEFT, fnt, false );
            ITextUtils.addDirectText( pdfWriter, reportData.getUserName(), x2, y, Element.ALIGN_LEFT, fnt, false );

            fnt = getFont();
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
            LogService.logIt( e, "BaseCoreTestReportTemplate.addReportInfoHeader()" );

            throw e;
        }
    }



    public void addSummarySection() throws Exception
    {
        try
        {
            if( reportData.getReport().getIncludeOverallScore()!=1 )
                return;

            String scrTxt = reportData.getOverallScoreText(); // getTestEvent().getOverallTestEventScore().getScoreText();

            float scrTxtW = bxWid - 4*PAD;
            float scrTxtH = ITextUtils.getDirectTextHeight( pdfWriter, scrTxt, scrTxtW, Element.ALIGN_LEFT,  getFONTSZ(), getFont() );

            float panelHgtBase = 120;

            float bxHght = panelHgtBase + ( scrTxtH > 0 ? scrTxtH + 3*PAD : 0 );
            float bxShdw = 3;

            previousYLevel =  currentYLevel;

            currentYLevel = previousYLevel - 6*PAD - bxHght - bxShdw;

            // LogService.logIt( "BaseCoreTestReportTemplate.addSummarySection() scrTxtH=" + scrTxtH + ", bxHght=" + bxHght + ", reportSummaryBoxY=" + reportSummaryBoxY );

            // create shadow box
            ITextUtils.addDirectColorRect( pdfWriter, BaseColor.LIGHT_GRAY, bxX + bxShdw,  currentYLevel, bxWid, bxHght, 0, 0.7f, true );

            // create white box
            ITextUtils.addDirectColorRect( pdfWriter, BaseColor.WHITE, bxX, currentYLevel + bxShdw, bxWid, bxHght, 0, 1, true );

            Font fnt =   getHeaderFontXLarge();

            float y = currentYLevel + bxHght + bxShdw - fnt.getSize() - PAD;

            // Add Summary Text
            ITextUtils.addDirectText(pdfWriter, lmsg("g.Summary"), bxX + PAD, y, Element.ALIGN_LEFT, fnt, false);

            fnt =  getFontXLarge();

            // Add Overall Score Text
            Rectangle rect = new Rectangle( bxX, y, bxX + bxWid, y + fnt.getSize() );

            String scr = I18nUtils.getFormattedNumber( reportData.getLocale(), reportData.getTestEvent().getOverallScore(), reportData.getTestEvent().getScorePrecisionDigits() );

            ITextUtils.addDirectText(  pdfWriter, lmsg( "g.OverallScoreXC", new String[] {scr}) , rect, Element.ALIGN_CENTER, fnt.getSize(),fnt, false );

            y -= PAD;

            Image img = getRainbowBar();

            float sclP = 100*barGrphWid/img.getWidth();

            img.scalePercent( sclP);

            y -= img.getScaledHeight() + 2*PAD;

            float grphY = y;

            ITextUtils.addDirectImage( pdfWriter, img, barGrphX, y, true );

            ITextUtils.addDirectBox( pdfWriter, getLightFontColor(), lineW, barGrphX, grphY, img.getScaledWidth(), img.getScaledHeight(), false );

            // Create the Grid lines.
            float grphW = barGrphWid;
            float grphH = panelHgtBase - 14*PAD;
            float interval = grphW/10;

            y -= grphH;

            // graph background color
            ITextUtils.addDirectColorRect( pdfWriter, getTablePageBgColor(), barGrphX, y, grphW, grphH, 0, 1f, true );

            // draw the border
            ITextUtils.addDirectBox( pdfWriter, getLightFontColor(), 0.8f, barGrphX, y, grphW, grphH, true );

            // draw the grid lines
            ITextUtils.addDirectVerticalGrid( pdfWriter, getLightFontColor(), barGrphX, y, grphW, grphH - 2, interval, true );

            float barH = 4*PAD;

            float barLly = y + (grphH - barH)/2;
            float barW = grphW * ( (reportData.te.getOverallScore() - reportData.getTestEvent().getScoreFormatType().getMin())/(reportData.getTestEvent().getScoreFormatType().getMax() - reportData.getTestEvent().getScoreFormatType().getMin() ) ) - lineW;

            fnt =  getFontLarge();

            // Add Overall:  Text
            rect = new Rectangle( bxX, barLly + ( barH - fnt.getSize() )/2, barGrphX - 10, barLly + barH - ( barH - fnt.getSize() )/2 );
            ITextUtils.addDirectText(  pdfWriter, lmsg( "g.Overall", new String[] {scr}) , rect, Element.ALIGN_RIGHT, fnt.getSize(),fnt, false );

            ScoreCategoryType sct = ScoreCategoryType.getValue(reportData.getTestEvent().getOverallRating());

            BaseColor[] cols = getColorsForScoreCategoryType(sct);

            // LogService.logIt( "BaseCoreTestReportTemplate.addSummaryBox() sct=" + sct.getName(Locale.US) + ", color1=" + cols[0].getRed() + "," + cols[0].getGreen() + "," + cols[0].getBlue() + ", equals=" + cols[0].equals( CTReportSettings.yellowGreenCatColor1) );

            ITextUtils.addDirectShadedRect( pdfWriter, cols[0], cols[1], barGrphX + lineW, barLly, barW, barH, 0, 0.9f, true, false );

            BaseColor c = cols[0].brighter();

            ITextUtils.addThreeSidedOutline( pdfWriter, c, barGrphX + lineW, barLly, barW, barH, 0.6f, false );

            // if have score text
            if( scrTxtH > 0 )
            {
                fnt = getFont();
                y -= 1.5*PAD + scrTxtH - 1.1*fnt.getSize();

                float scrTxtLlx = bxX + 2*PAD;
                float scrTxtUrx = bxX + 2*PAD + scrTxtW;

                rect = new Rectangle( scrTxtLlx, y, scrTxtUrx, scrTxtH );

                ITextUtils.addDirectText(  pdfWriter, scrTxt, rect, Element.ALIGN_LEFT, fnt.getSize(), fnt, false );
            }

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addSummarySection()" );

            throw new STException( e );
        }
    }


    public void addComparisonSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();
            
            if( reportData.getReport().getIncludeNorms()==1 && !reportData.getTestEvent().getOverallTestEventScore().getHasValidNorms() )
                prepNotes.add( lmsg( "g.RptNote_NoNorms_InsufficientData" ) );


            if( reportData.getReport().getIncludeNorms()==0 || !otes.getHasValidNorms() )
                return;
            
            boolean includeCompanyInfo = !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            
            String reportCompanyName = reportData.getReportCompanyName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                      ";

            if( reportCompanyName == null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( !includeCompanyInfo )
                reportCompanyName = lmsg( "g.Company" );
            
            // TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();

            String[] names = new String[] { lmsg( "g.OverallPerc" ), getCountryName( reportData.getTestEvent().getPercentileCountry()!=null && !reportData.getTestEvent().getPercentileCountry().isEmpty() ? reportData.getTestEvent().getPercentileCountry() : reportData.getTestEvent().getIpCountry() ), StringUtils.truncateString(reportCompanyName, 22 ) };

            float[] percentiles = new float[] { otes.getPercentile(), otes.getCountryPercentile(),otes.getAccountPercentile() };

            int[] counts = new int[] { otes.getOverallPercentileCount(), otes.getCountryPercentileCount(),otes.getAccountPercentileCount() };

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

            if( !otes.getHasValidOverallNorm() && otes.getHasValidOverallZScoreNorm() && reportData.getTestEvent().getProduct()!=null && reportData.getTestEvent().getProduct().getConsumerProductType().getIsJobSpecific() )
            {
                percentiles[0] = otes.getOverallZScorePercentile();
                counts[0]=0;
                names[0] = lmsg("g.OverallPercApprox");
                validCount++;
            }

            // no data to show.
            if( validCount == 0 )
            {
                // reportComparisonBoxY = reportSummaryBoxY;
                return;
            }

            Font fnt =   getHeaderFontXLarge();

            float y = previousYLevel - 6*PAD;

            // Add Title
            ITextUtils.addDirectText( pdfWriter, lmsg("g.ComparisonPcts"), bxX + PAD, y, Element.ALIGN_LEFT, fnt, false);

            // Now create the graph
            Image img = getRainbowBar();

            float sclP = 100*barGrphWid/img.getWidth();

            img.scalePercent( sclP );

            y -= img.getScaledHeight() + 2*PAD;

            // float barGrphX = bxX + ( bxWid - barGrphWid - 2*PAD ); //    bxX + 7*PAD;
            float grphY = y;

            // Ranibow image
            ITextUtils.addDirectImage( pdfWriter, img, barGrphX, y, true );

            // rainbow image outline.
            ITextUtils.addDirectBox( pdfWriter, getLightFontColor(), lineW, barGrphX, grphY, img.getScaledWidth(), img.getScaledHeight(), false );

            float pctBarHgt = 10;

            float pctBarGap = 1.5f*PAD;

            // Create the Grid lines.
            float grphW = barGrphWid;
            float grphH = validCount*pctBarHgt + (validCount+1)*pctBarGap;

            float interval = grphW/10;

            y -= grphH;

            // shade the back of the box
            ITextUtils.addDirectColorRect( pdfWriter, getTablePageBgColor(), barGrphX, y, grphW, grphH, 0, 1f, true );


            // Grid Box border
            ITextUtils.addDirectBox( pdfWriter, getLightFontColor(), 0.8f, barGrphX, y, grphW, grphH, true );

            //  Grid lines
            ITextUtils.addDirectVerticalGrid( pdfWriter, getLightFontColor(), barGrphX, y, grphW, grphH + 2, interval, true );

            //fnt =  getFont();

            y += grphH;

            String name;
            String countStr;
            float pct;
            float barW;
            float barLly;
            float labelLly;

            BaseColor[] cols = new BaseColor[] {getBarGraphCoreShade1(), getBarGraphCoreShade2()};

            int barCt = 0;

            for( int i=0; i<names.length; i++ )
            {
                fnt =  getFont();

                pct = percentiles[i];

                if( counts[i]!=0 && counts[i] <  Constants.MIN_PERCENTILE_COUNT )
                    continue;

                countStr = counts[i]>=  Constants.MIN_PERCENTILE_COUNT ? lmsg( "g.PercentileCountStr1" , new String[] { Integer.toString( counts[i] )} ) : lmsg( "g.PercentileCountStr0" );

                if( pct <= 0 )
                    continue;

                name = names[i] + " (" + I18nUtils.getFormattedNumber( reportData.getLocale(), pct, 0 ) + NumberUtils.getPctSuffix( reportData.getLocale(), pct, 0 ) + ")";
                barW = grphW * pct/100f - lineW;

                // Not going to skip, so draw it.
                barCt++;

                barLly = y - barCt*(pctBarGap + pctBarHgt);

                labelLly = barLly + 1 + (pctBarHgt - fnt.getSize() )/2;

                // Add Title Text
                Rectangle rect = new Rectangle( bxX, labelLly, barGrphX - 10, labelLly + fnt.getSize() );
                ITextUtils.addDirectText(  pdfWriter, name , rect, Element.ALIGN_RIGHT, fnt.getSize(), fnt, false );

                // Add the bar
                ITextUtils.addDirectShadedRect( pdfWriter, cols[0], cols[1], barGrphX + lineW, barLly, barW, pctBarHgt, 0, 0.9f, true, false );

                ITextUtils.addThreeSidedOutline( pdfWriter, cols[0].brighter(), barGrphX + lineW, barLly, barW, pctBarHgt, 0.6f, false );

                fnt = getFontSmall();

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
            LogService.logIt( e, "BaseCoreTestReportTemplate.addComparisonSection()" );

            throw new STException( e );
        }
    }



    public String getCountryName( String countryCode )
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
    public boolean addAssessmentOverview() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( reportData.getReport().getIncludeOverviewText()==0 )
                return false;

            // currentYLevel = 0;

            String ovrTxt = reportData.getReportOverviewText(); // ScoreFormatUtils.getDescripFromTextParam( reportData.getTestEvent().getOverallTestEventScore().getTextParam1() );

            // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() " + ovrTxt );

            if( ovrTxt == null || ovrTxt.isEmpty() )
                return false;

            Font fnt =   getHeaderFontXLarge();

            float y = previousYLevel - 6*PAD - getFont().getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, lmsg("g.AssessmentOverview"), bxX + PAD, y, Element.ALIGN_LEFT, fnt, false);

            // Change getFont()
            fnt =  getFont();

            float spaceLeft = y - 2*PAD - footerHgt;

            // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float leading = fnt.getSize();

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, ovrTxt, bxWid, Element.ALIGN_LEFT, leading, fnt);

             y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            float txtW = bxWid - 4*PAD;

            float txtLlx = bxX + 2*PAD;
            float txtUrx = bxX + 2*PAD + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, ovrTxt, rect, Element.ALIGN_LEFT, leading, fnt, false );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return false;
            }

            else
            {
                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

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
                    LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() adding second column "  );

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
            LogService.logIt( e, "BaseCoreTestReportTemplate.addAssessmentOverview()" );

            throw new STException( e );
        }
    }





    public void addDetailedReportInfoHeader() throws Exception
    {
        try
        {

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.Detail" ), null );

            y -= 7; // 2*PAD;

            Font fntRgt = getFontXLarge();
            Font fntRgt2 = getFont();
            Font fntLft = getFontLight();

            float x = bxX + PAD;

            boolean includeCompanyInfo = !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
                        
            boolean includePreparedFor = includeCompanyInfo && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
           
            // Now, let's create a table!
            PdfPTable t = new PdfPTable( 2 );

            t.setTotalWidth( bxWid );
            t.setLockedWidth( true );
            t.setWidths( reportData.getIsLTR() ?  new float[] {5,29} : new float[] {29,5} );


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
                                             !includePreparedFor || reportData.getTestKey().getAuthUser() == null ? "" : reportData.getTestKey().getAuthUser().getFullname(),
                                             includeCompanyInfo ? reportData.getOrgName() : "",
                                             reportData.getTestKey().getAuthUser() == null ? "" : StringUtils.removeQamail( reportData.getTestKey().getAuthUser().getEmail() ) };

            if( includePreparedFor )
            {
                String auth = reportData.getTestKey().getAuthUser() == null ? lmsg( "g.AuthStr" , params ) : lmsg( "g.AuthStrCombined" , params );

                t.addCell( new Phrase( lmsg( "g.AuthorizedC"), fntLft ) );
                t.addCell( new Phrase( auth, fntLft ) );
            }
            
            t.addCell( new Phrase( lmsg( "g.StartedC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getStartDate(), reportData.getTimeZone() ), fntLft ) );

            t.addCell( new Phrase( lmsg( "g.CompletedC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getLastAccessDate(), reportData.getTimeZone() ), fntLft ) );

            t.addCell( new Phrase( lmsg( "g.OverallScoreC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedNumber( reportData.getLocale(), reportData.getTestEvent().getOverallScore(), reportData.getTestEvent().getScorePrecisionDigits()), fntLft ) );

            t.writeSelectedRows(0, -1, x + PAD, y, pdfWriter.getDirectContent() );

            LogService.logIt( "BaseCoreTestReportTemplate.addDetailedReportHeader() t.calculateHeights()=" + t.calculateHeights() + ", currentY=" + y );

            currentYLevel = y - t.calculateHeights();

            LogService.logIt( "BaseCoreTestReportTemplate.addDetailedReportHeader() currentYLevel=" + currentYLevel );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addDetailedReportInfoHeader()" );

            throw new STException( e );
        }
    }


    protected void addBiodataInfo() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( !reportData.includeBiodataInfo() )
                return;

            // LogService.logIt(  "BaseCoreTestReportTemplate.addBiodataInfo() START " );

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

            LogService.logIt( "BaseCoreTestReportTemplate.addBiodataInfo()" );

            addAnyCompetenciesInfo( tesl, "g.BiodataTitle", "g.BiodataSubtitle", "g.Detail", "g.Description", "g.BiodataCaveatHeader", "g.BiodataCaveatFooter", reportData.includeInterview(), true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addBiodataInfo()" );

            throw new STException( e );
        }
    }



    protected void addKSAInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>();

            SimCompetencyClass scc;

            for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                // Only KSAs
                if( !scc.isKSA() )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            LogService.logIt( "BaseCoreTestReportTemplate.addKSAInfo()" );

            addAnyCompetenciesInfo( tesl, "g.KSATitle", "g.KSASubtitle", "g.Detail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addKSAInfo()" );

            throw new STException( e );
        }
    }

    protected void addAIMSInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.NONCOGNITIVE ); // new ArrayList<>();            
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOG_COMBO ) );

            Collections.sort(tesl, new DisplayOrderComparator() );  // new TESNameComparator());
            
            if( tesl.size() <= 0 )
                return;

            LogService.logIt( "BaseCoreTestReportTemplate.addAIMSInfo()" );

            addAnyCompetenciesInfo( tesl, "g.AIMSTitle", "g.AIMSSubtitle", "g.Detail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addAIMSInfo()" );

            throw new STException( e );
        }
    }

    
    protected void addEQInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.EQ ); // new ArrayList<>();

            if( tesl.size() <= 0 )
                return;

            LogService.logIt( "BaseCoreTestReportTemplate.addEQInfo() found " + tesl.size() );

            addAnyCompetenciesInfo( tesl, "g.EQTitle", "g.EQSubtitle", "g.Detail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addEQInfo()" );

            throw new STException( e );
        }
    }
    




    /*
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
                scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                if( !scc.getIsDirectCompetency() ) // && !scc.getIsAggregate() )
                    continue;

                // if supposed to hide
                if( tes.getHide()>0 )
                    continue;

                tesl.add( tes );
            }

            if( tesl.size() <= 0 )
                return;

            addAnyCompetenciesInfo( tesl, "g.Competencies", null, "g.CompetencyDetail", "g.Description", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addCompetencyInfo()" );

            throw new STException( e );
        }
    }
    */


    protected void addEducTrainingInfo() throws Exception
    {
        try
        {
            if( !reportData.includeEducTypeDescrip() && ! reportData.includeTrainingTypeDescrip() && !reportData.includeRelatedExperTypeDescrip() )
                return;

            TestEvent te = reportData.getTestEvent();

            // no data
            if( te.getEducTypeId() == 0 && te.getExperTypeId()==0 && te.getTrainTypeId()==0 )
                return;

            // At this point we will create a report

            previousYLevel =  currentYLevel;

            java.util.List<TextAndTitle> ttl = new ArrayList<>();

            if( reportData.includeEducTypeDescrip() && te.getEducTypeId() > 0)
                ttl.add( new TextAndTitle( EducType.getValue( te.getEducTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinEducLevel" ) ) );

            if( reportData.includeTrainingTypeDescrip()&& te.getTrainTypeId() > 0)
                ttl.add( new TextAndTitle( TrainingType.getValue( te.getTrainTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinTrainType" ) ) );

            if( reportData.includeRelatedExperTypeDescrip()&& te.getExperTypeId() > 0)
                ttl.add( new TextAndTitle( RelatedExperType.getValue( te.getTrainTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinRelatedExp" ) ) );

            if( ttl.isEmpty() )
                return;

            float y = addTitle( previousYLevel, lmsg( "g.MinQualGuidelines" ), lmsg( "g.MinQualGuidelinesSubtitle" ) );

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
            t.addCell( new Phrase( lmsg( "g.Item" ) , getFont()) );
            t.addCell( new Phrase( "" , getFont()) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

            }

            currentYLevel = addTableToDocument( y, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addEducTrainingInfo()" );

            throw new STException( e );
        }
    }

    protected void addPreparationNotesSection() throws Exception
    {
        try
        {
            LogService.logIt(  "BaseCoreTestReportTemplate.addPreparationNotesSection() START" );

            prepNotes.add( 0, lmsg( "g.CT3RptCaveat" ) );

            Product p = reportData.getTestEvent().getProduct();
            if( p != null && p.getOnetSoc()!=null && !p.getOnetSoc().isEmpty() )
            {
                prepNotes.add( lmsg( "g.OnetDescrip", null ));

                prepNotes.add( lmsg( "g.OnetSocX", new String[]{p.getOnetSoc()} ));

                if( p.getOnetVersion()!=null && !p.getOnetVersion().isEmpty() )
                    prepNotes.add( lmsg( "g.OnetVersionX", new String[]{p.getOnetVersion()} ));
            }

            Calendar cal = new GregorianCalendar();            
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");            
            String dtStr = df.format( cal.getTime() );
                        
             prepNotes.add( lmsg( "g.SimIdAndVersion", new String[]{ Long.toString( reportData.getTestEvent().getSimId()) , Integer.toString(reportData.getTestEvent().getSimVersionId() ), Long.toString( reportData.getTestEvent().getTestKeyId()), Long.toString( reportData.getTestEvent().getTestEventId()), "","", dtStr } ));

            if( prepNotes.isEmpty() )
                return;

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.PreparationNotes" ), null );

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
            LogService.logIt( e, "BaseCoreTestReportTemplate.addPreparationNotesSection()" );

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

            LogService.logIt( "BaseCoreTestReportTemplate.addTasksInfo()" );

            addAnyCompetenciesInfo( tesl, "g.Tasks", null, "g.TaskDetail", "g.TaskDescription", null, null, reportData.includeInterview() ? true : false, false  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addTasksInfo()" );

            throw new STException( e );
        }
    }



    protected void addAnyCompetenciesInfo( java.util.List<TestEventScore> teslst,
                                            String titleKey,
                                            String subtitleKey,
                                            String detailKey,
                                            String descripKey,
                                            String caveatHeaderKey,
                                            String caveatFooterKey,
                                            boolean withInterview,
                                            boolean noInterviewLimit) throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel - 10;

            if( previousYLevel <= footerHgt )
            {
                document.newPage();

                currentYLevel = 0;
                previousYLevel = 0;
            }


            int interviewQsPerComp = reportData.getReport().getMaxInterviewQuestionsPerCompetency();
            boolean numeric = this.reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean rating = this.reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean norms = this.reportData.getReport().getIncludeSubcategoryNorms()==1;
            boolean interpretation = reportData.getReport().getIncludeSubcategoryInterpretations()==1;


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

            float y = addTitle( previousYLevel, lmsg( titleKey ), ( subtitleKey==null ?  null : lmsg( subtitleKey ) ) );


            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = bxWid*0.95f;

            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );

            // This tells iText to always use the first row as a header on subsequent pages.
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.BOX );
            c.setBorderWidth( 0.5f );
            c.setPadding( 2 );
            c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            c.setBorderColor( BaseColor.DARK_GRAY );


            // Add header row.
            t.addCell( new Phrase( lmsg( detailKey ) , getFont()) );
            t.addCell( new Phrase( lmsg( withInterview ? "g.InterviewGuide" : descripKey ) , getFont()) );

            c.setBackgroundColor( BaseColor.WHITE );

            PdfPTable compT;
            PdfPTable igT;
            ScoreCategoryType sct;
            Image dotImg;

            java.util.List<InterviewQuestion> igL;
            InterviewQuestion ig;

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            String scoreText;

            // String caveatText;

            // For each competency
            for( TestEventScore tes : tesl )
            {
                // LogService.logIt( "BaseCoreTestReportTemplate.addAnyCompetenciesInfo() adding " + tes.toString() );

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
                c = rating ?  new PdfPCell( dotImg ) : new PdfPCell( new Phrase( "" , getFontSmall() ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setVerticalAlignment( interpretation ? Element.ALIGN_TOP : Element.ALIGN_MIDDLE );
                c.setPadding( 2 );

                if( interpretation )
                    c.setPaddingTop( 15 );
                // c.addElement( dotImg );
                compT.addCell( c );

                p = new Phrase();

                // Name and Score Cell
                p.add( new Chunk( reportData.getCompetencyName(tes)  + "\n" , getFontBold() ) );

                p.add( new Chunk( numeric ? lmsg( "g.ScoreC") : "", getFontSmallBold() )  );
                p.add( new Chunk( numeric ? " " + I18nUtils.getFormattedNumber( reportData.getLocale(), tes.getScore(), 0 ) : "" , getFontSmall() ) );

                if( norms && tes.getHasValidOverallNorm() )
                {
                    p.add( new Chunk( "\n" + lmsg( "g.PercentileC" ) , getFontSmallBold() )  );
                    p.add( new Chunk( " " + NumberUtils.getPctSuffixStr( reportData.getLocale(), tes.getPercentile(), 0 ), getFontSmall() )  );
                }

                if( interpretation )
                {
                    p.add( new Chunk( "\n" + lmsg( "g.InterpretationC" ) + " " , getFontSmallBold() )  );

                    // LogService.logIt( "BaseCoreTestReportTemplate.addCompetenciesInfo() Competency=" + tes.getName() + ", class=" + tes.getSimCompetencyClassId() + ", inerpKey=" + sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) );

                    //p.add( new Chunk( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), getFontSmall() )  );
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
                    //p.add( new Chunk( "\n" + lmsg( "g.InterpretationC" ) + " " , getFontSmallBold() )  );

                    //LogService.logIt( "BaseCoreTestReportTemplate.addCompetenciesInfo() Competency=" + tes.getName() + ", class=" + tes.getSimCompetencyClassId() + ", inerpKey=" + sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) );

                    p.add( new Chunk( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), getFontSmall() )  );
                    c.addElement( p );
                }

                c.setPaddingTop( 8 );

                compT.addCell( c );

                scoreText = reportData.getCompetencyScoreText( tes ); // tes.getScoreText();

                if( scoreText == null )
                    scoreText = "";

                com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                Paragraph cHdr=null;
                Paragraph cFtr=null;
                float spcg = 8;
                cl.setListSymbol( "\u2022");

                Phrase cst = new Phrase( new Phrase( scoreText, getFontSmall() ) );
                cst.setLeading( 10 );

                for( String ct : reportData.getCaveatList(tes) )
                {
                    if( ct.isEmpty() )
                        continue;

                    cl.add( new ListItem( new Paragraph( ct , getFontSmall() ) ) );
                }

                if( cl.size()>0 )
                {
                    if( caveatHeaderKey != null && !caveatHeaderKey.isEmpty() )
                    {
                        cHdr = new Paragraph( lmsg( caveatHeaderKey ) , getFontSmall() );
                        cHdr.setSpacingBefore( spcg );
                        cHdr.setSpacingAfter( spcg );
                        cHdr.setLeading( 10 );
                    }

                    if( caveatFooterKey != null && !caveatFooterKey.isEmpty() )
                    {
                        cFtr = new Paragraph( lmsg( caveatFooterKey ) , getFontSmall() );
                        cFtr.setSpacingBefore( spcg );
                        cFtr.setLeading( 10 );
                    }
                }

                // ScoreText Cell
                c = new PdfPCell(); // new Phrase( scoreText, getFontSmall() )  );
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

                    maxInt = Math.min( maxInt, MAX_INTERVIEWQS_PER_COMPETENCY );

                    igL = reportData.getInterviewQuestionList(tes, maxInt); //tes.getInterviewQuestionList(maxInt);

                    // for( int i=0; i<interviewQsPerComp; i++ )
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
                        c = new PdfPCell( new Phrase( ig.getQuestion(), getFontSmall() ) );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setColspan( 15 );
                        c.setPadding(2);
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);

                        // Row 2 - Color Dots
                        igT.addCell(ep);
                        c =  new PdfPCell( getRedDot() );
                        // c.addElement(redDot);
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( getRedYellowDot() );
                        // c.addElement( redYellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( getYellowDot() );
                        // c.addElement( yellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( getYellowGreenDot() );
                        // c.addElement( yellowGreenDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( getGreenDot() );
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
                        igT.addCell( new Phrase( "1", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "2", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "3", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "4", getFontSmall() ) );
                        igT.addCell(ep);
                        igT.addCell(ep);
                        igT.addCell( new Phrase( "5", getFontSmall() ) );
                        igT.addCell(ep);

                        c = igT.getDefaultCell();
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setPadding( 2 );
                        c.setPaddingBottom( 10 );


                        // row 4 - anchors
                        c = new PdfPCell( new Phrase( ig.getAnchorLow(), getFontSmall() ) );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 4 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( ig.getAnchorMed(), getFontSmall() ) );
                        c.setBackgroundColor( BaseColor.WHITE );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 5 );
                        c.setPaddingBottom( 10 );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( ig.getAnchorHi(), getFontSmall() ) );
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

                }

                // else, with description
                else
                {
                    String d = reportData.getCompetencyDescription( tes ); //  ScoreFormatUtils.getDescripFromTextParam( tes.getTextParam1() );
                    c = new PdfPCell( new Phrase( d == null ? "" : d, getFontSmall() ) );
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
            LogService.logIt( e, "BaseCoreTestReportTemplate.addAnyCompetenciesInfo() titleKey=" + titleKey );

            throw new STException( e );
        }
    }





    public void addWritingSampleInfo() throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseCoreTestReportTemplate.addWritingSampleInfo() " + reportData.getReport().getIncludeWritingSampleInfo() );

            previousYLevel =  currentYLevel;

            if( reportData.getReport().getIncludeWritingSampleInfo()==0 )
                return;

            java.util.List<TextAndTitle> ttl = ScoreFormatUtils.getNonCompTextListTable(reportData.getTestEvent(), NonCompetencyItemType.WRITING_SAMPLE );

            if( ttl.isEmpty() )
                return;

            float y = addTitle( previousYLevel, lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ) );

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
            t.addCell( new Phrase( lmsg( "g.WritingSampleQuestion" ) , getFont()) );
            t.addCell( new Phrase( lmsg( "g.Response" ) , getFont()) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

            } // each writing sample

            currentYLevel = addTableToDocument( y, t );

            // LogService.logIt( "BaseCoreTestReportTemplate.addWritingSampleInfo()" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addWritingSampleInfo()" );

            throw new STException( e );
        }
    }

    public void addMinQualsApplicantDataInfo() throws Exception
    {
        try
        {
            LogService.logIt(  "BaseCoreTestReportTemplate.addMinQualsApplicantDataInfo() START" );

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

            float y = addTitle( previousYLevel, lmsg( "g.AppDataAndMinQualsTitle" ), lmsg( "g.AppDataAndMinQualsSubtitle" ) );

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
            t.addCell( new Phrase( lmsg( "g.Item" ) , getFont()) );
            t.addCell( new Phrase( "" , getFont()) );

            c.setBackgroundColor( BaseColor.WHITE );

            Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.BOX );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tt.getText(), getFontSmall() ) );
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
            LogService.logIt( e, "BaseCoreTestReportTemplate.addMinQualsApplicantDataInfo()" );

            throw new STException( e );
        }
    }






    public void addNotesSection() throws Exception
    {
        addTitle( 0 , lmsg("g.Notes"), lmsg( "g.NotesSubtitle" ) );
    }





    public void addCoverPage() throws Exception
    {
        try
        {
            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 20;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

            ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), 20, y, false );

            Font fnt = getFontLargeLight();

            y += getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            float x = 2 + pageWidth/2;
            float x2 = x + 86;

            // y += getHraLogoBlackText().getScaledHeight();

            boolean includeCompanyInfo = !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
                        
            boolean includePreparedFor = includeCompanyInfo && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
           
            
            ITextUtils.addDirectText( pdfWriter, lmsg( "g.CandidateC" ), x, y, Element.ALIGN_LEFT, fnt, false );

            fnt = getFontXLargeLightBold();
            ITextUtils.addDirectText( pdfWriter, StringUtils.truncateStringWithTrailer(reportData.getUserName(), 40, false ), x2, y, Element.ALIGN_LEFT, fnt, false );

            fnt = getFontLargeLight();
            y -= fnt.getSize()*1.2;
            ITextUtils.addDirectText( pdfWriter, lmsg( "g.AssessmentC" ), x, y, Element.ALIGN_LEFT, fnt, false );
            ITextUtils.addDirectText( pdfWriter, StringUtils.truncateStringWithTrailer(reportData.getSimName(), 40, false ) , x2, y, Element.ALIGN_LEFT, fnt, false );
            // addDirectText( MessageFactory.getStringMessage( reportData.getLocale() , "g.CandidateC" ), x, y - 50, baseFontCalibri, 16, getLightFontColor(), false );

            y -= fnt.getSize()*1.2;
            ITextUtils.addDirectText( pdfWriter, lmsg( "g.CompletedC" ), x, y, Element.ALIGN_LEFT, fnt, false );
            ITextUtils.addDirectText( pdfWriter, reportData.getSimCompleteDateFormatted(), x2, y, Element.ALIGN_LEFT, fnt, false );


            if( includePreparedFor && reportData.getTestKey().getAuthUser() != null )
            {
                // fnt = getFontXLargeLightBold();
                y -= fnt.getSize()*2.2;
                ITextUtils.addDirectText( pdfWriter, lmsg( "g.PreparedForC" ), x, y, Element.ALIGN_LEFT, fnt, false );
                ITextUtils.addDirectText( pdfWriter, StringUtils.truncateStringWithTrailer(reportData.getTestKey().getAuthUser().getFullname(), 40, false ), x2, y, Element.ALIGN_LEFT, fnt, false );
            }


            if( includeCompanyInfo && reportData.hasCustLogo() && custLogo!=null )
            {
                 y -= fnt.getSize()*1.00f;
                 // float ix = x2;
                 ITextUtils.addDirectImage( pdfWriter, custLogo, x2, y - custLogo.getScaledHeight(), false );
            }

            else if( includeCompanyInfo && reportData.getTestKey().getAuthUser() != null )
            {
                fnt = getFontXLargeLightBold();
                y -= fnt.getSize()*1.2;
                ITextUtils.addDirectText( pdfWriter, reportData.getOrgName(), x2, y, Element.ALIGN_LEFT, fnt, false );
            }


            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, getHraOrangeColor(), false );

            ITextUtils.addDirectColorRect( pdfWriter, getHraBaseReportColor(), 0, 0, pageWidth, pageHeight/2, 0, 1, true );

            fnt = getHeaderFontXXLargeWhite();
            y = 0.35f*pageHeight;

            // Rectangle rect = new Rectangle( x, y, x+230, 3*1.1f*fnt.getSize() );
            Rectangle rect = new Rectangle( x, 20, x+230, y  );
            ITextUtils.addDirectText( pdfWriter, reportData.getReportName(), rect, Element.ALIGN_LEFT, fnt.getSize() + 1, fnt, false );



            fnt = getFontWhite();

            rect = new Rectangle( 1, 5, pageWidth, fnt.getSize() + 10 );

            ITextUtils.addDirectText( pdfWriter, lmsg( "g.ProprietaryAndConfidential" ), rect, Element.ALIGN_CENTER, 0, fnt, false );


        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addCoverPage()" );
        }
    }




    /*
    public float addTitle( float startY, String title, String subtitle) throws Exception
    {
            Font fnt =  getHeaderFontXLarge();

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
    */


    public float addTitle( float startY, String title, String subtitle ) throws Exception
    {
        try
        {
            if( startY > 0 )
            {
                float ulY = startY - 18* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }


            previousYLevel =  currentYLevel;

            Font fnt =   getHeaderFontXLarge();

            float y = startY>0 ? startY - fnt.getSize() - 4*PAD :  pageHeight - headerHgt - fnt.getSize() - 4*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, title, bxX + PAD, y, Element.ALIGN_LEFT, fnt, false);

            // No subtitle
            if( subtitle==null || subtitle.isEmpty() )
                return y;

            // Change getFont()
            fnt =  getFont();

            float spaceLeft = y - 2*PAD - footerHgt;

            // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float leading = fnt.getSize();

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, subtitle, bxWid, Element.ALIGN_LEFT, leading, fnt);

             y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            float txtW = bxWid - 4*PAD;

            float txtLlx = bxX + 2*PAD;
            float txtUrx = bxX + 2*PAD + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, subtitle, rect, Element.ALIGN_LEFT, leading, fnt, false );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                Phrase p = new Phrase( subtitle, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() adding second column "  );

                    document.newPage();

                    ct.setSimpleColumn( colDims2.getLeft(), colDims2.getBottom(), colDims2.getRight(), colDims2.getTop() );

                    ct.setYLine( colDims2.getTop() );

                    status = ct.go();

                    currentYLevel = ct.getYLine();
                }


                return currentYLevel;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCoreTestReportTemplate.addTitleAndSubtitle()" );

            throw new STException( e );
        }
    }



    public float addText( String text, Font fnt ) throws Exception
    {
        try
        {
            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, text, txtW, Element.ALIGN_LEFT, fnt.getSize(), fnt);

            if( currentYLevel - txtHght - footerHgt - PAD < 0 )
                addNewPage();

            previousYLevel =  currentYLevel;

            float y = currentYLevel;

            float leading = fnt.getSize();

            float spaceLeft = currentYLevel - leading - footerHgt;

            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, text, rect, Element.ALIGN_LEFT, leading, fnt, false );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                Phrase p = new Phrase( text, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    LogService.logIt( "BaseCT2ReportTemplate.addText() adding second column "  );

                    document.newPage();

                    ct.setSimpleColumn( colDims2.getLeft(), colDims2.getBottom(), colDims2.getRight(), colDims2.getTop() );

                    ct.setYLine( colDims2.getTop() );

                    status = ct.go();

                    currentYLevel = ct.getYLine();
                }


                return currentYLevel;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTtext()" );

            throw new STException( e );
        }
    }




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
                // LogService.logIt( "BaseCoreTestReportTemplate.addTableToDocument() row=" + i + ", rowHeight=" + rowHgts[i] );
            }

            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;

            float heightAvailNewPage = pageHeight - headerHgt - 3*PAD - footerHgt - 3*PAD - headerHeight;

            if( maxRowHeight >= heightAvailNewPage*0.5 )
                t.setSplitLate(false);

            // If first row doesn't fit on this page
            else if( firstRowHgt > ulY- footerHgt - 3*PAD - headerHeight ) // ulY < footerHgt + 8*PAD )
            {
                // LogService.logIt( "BaseCoreTestReportTemplate.addTableToDocument() adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
            }


            //if( maxRowHeight > usablePageHeight )
            //    t.setSplitLate(false);


            Rectangle colDims = new Rectangle( bxX, footerHgt + 3*PAD, bxX + bxWid, ulY );
            // LogService.logIt( "BaseCoreTestReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

            float heightNoHeader = tableHeight - headerHeight;


            Object[] dta = calcTableHghtUsed( colDims.getTop() - colDims.getBottom() - headerHeight, 0, t.getHeaderRows(), rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
            int nextIndex = (Integer) dta[0];
            float heightUsedNoHeader = (Float) dta[1];
            float residual = (Float) dta[2];

            // LogService.logIt( "BaseCoreTestReportTemplate.addTableToDocument() tableHeight=" + t.calculateHeights() + ", headerHeight=" + headerHeight + ", maxRowHeight=" + maxRowHeight + ", heightAvailNewPage=" + heightAvailNewPage + ", initial heightUsedNoHeader=" + heightUsedNoHeader + ", residual=" + residual );


            ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

            // NOTE - this forces Composite mode (using ColumnText.addElement)
            ct.addElement( t );

            ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );
            // ct.setSimpleColumn( colDims1 );


            int status = ct.go();

            // int linesWritten = ct.getLinesWritten();

            // LogService.logIt( "BaseCoreTestReportTemplate.addTableToDocument() initial lines written. NO_MORE_COLUMN=" + ColumnText.NO_MORE_COLUMN + ", NO_MORE_TEXT=" + ColumnText.NO_MORE_TEXT  );

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

                // LogService.logIt( "BaseCoreTestReportTemplate.addTableToDocument() AFTER adding next page. hgtUsedThisPage=" + hgtUsedThisPage +  ", Total HeightNeededNoHeader=" + heightNeededNoHeader + ", Total HeightUsedNoHeader=" + heightUsedNoHeader  );

                colDims = new Rectangle( bxX, ulY - heightAvailNewPage , bxX + bxWid, ulY );

                document.newPage();

                ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );

                ct.setYLine( colDims.getTop() );

                status = ct.go();

                // linesWritten += ct.getLinesWritten();

                //  LogService.logIt( "BaseCoreTestReportTemplate.addTableToDocument() status=" + status  );

                pages++;
            }

            return ct.getYLine();
    }

    protected java.util.List<TestEventScore> getTestEventScoreListToShow( TestEventScoreType test, SimCompetencyClass scc )
    {
        java.util.List<TestEventScore> out = new ArrayList<>();

        for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( test.getTestEventScoreTypeId() ) )
        {
            if( tes.getSimCompetencyClassId() != scc.getSimCompetencyClassId() )
                continue;

            // if supposed to hide
            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;

            out.add( tes );
        }

        Collections.sort(out, new DisplayOrderComparator());  // new TESNameComparator() );

        return out;
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

        Object[] dta = new Object[] {new Integer(startIndex) , new Float(0), new Float(0) };

        if( rowHgts.length<=startIndex )
            return dta;

        float hgt = 0;
        float resid = 0;

        if( prevResidual>0 )
        {
            // Bigger than max
            if( prevResidual>= maxRoom )
            {
                dta[1] = new Float( maxRoom );
                dta[2] = new Float( prevResidual -  maxRoom );

                if( prevResidual== maxRoom)
                {
                    dta[0]=startIndex+1;
                    dta[2] = new Float( 0 );
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
                dta[0]=new Integer( i+1 );
                dta[1] = new Float(hgt);
                return dta;
            }

            if( rowHgts[i] + hgt > maxRoom )
            {
                if( i==startIndex || !isSplitLate )
                {
                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() AAA i=" + i + ", hgt=" + hgt );

                    resid = rowHgts[i] - (maxRoom-hgt);
                    dta[2] = new Float( resid );
                    hgt = maxRoom;

                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() BBB hgt=" + hgt + ", resid=" + resid );
                }

                dta[0] = new Integer(i);
                dta[1] = new Float(hgt);
                return dta;
            }

            hgt += rowHgts[i];
        }

        dta[0] = new Integer(maxIndex+1);
        dta[1] = new Float(hgt);
        return dta;
    }


    public void addNewPage() throws Exception
    {
        document.newPage();
        this.currentYLevel = pageHeight - 3*PAD -  headerHgt;
    }



    public String lmsg( String key )
    {
        return lmsg( key, null );
    }

    public String lmsg( String key,String[] prms )
    {
        return MessageFactory.getStringMessage( reportData.getLocale() , key, prms );
    }



}
