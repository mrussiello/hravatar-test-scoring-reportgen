/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.pci;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.BaseCT2ReportTemplate;
import com.tm2score.custom.coretest2.CT2ReportData;
import com.tm2score.format.TableBackground;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportData;
import com.tm2score.service.LogService;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 *
 * @author miker_000
 */
public abstract class BasePciReportTemplate extends BaseCT2ReportTemplate {
    
    String pciLogoCoverFilename = "pci-logo-trans-300dpi.png";
    String pciLogoHeaderFilename = "pci-logo-purple-300dpi.png";
    
    Image pciLogoCover;
    Image pciLogoHeader;
    
    
    
    
    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CT2ReportData();

        initLocales();
        
        initFonts();
        
        initColors();  
        
        if( ct2Colors!=null )
            ct2Colors.clearBorders();

        scoreBoxBorderWidth=0;
        lightBoxBorderWidth=0;
        
        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        // LogService.logIt( "BasePciReportTemplate.init() title=" + rd.getReportName() );
        
        PciHeaderFooter hdr = new PciHeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );

        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );

        headerHgt = hghts[0];
        footerHgt = hghts[1];

        usablePageHeight = pageHeight - headerHgt - footerHgt - 4*PAD;

        // bxX = 5*PAD;
        // bxWid = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN;
        // barGrphWid = 0.77f*bxWid;
        // barGrphX = bxX + ( bxWid - barGrphWid - 2*PAD ); //    bxX + 7*PAD;

        // dataTableWidth = hdr.headerW;

       // document.setMargins( document.leftMargin(), document.rightMargin(), 42, 42 );

        // LogService.logIt( "BaseCT2ReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

        tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );
    }
    
    
    public void initPciSpecial()
    {
        try
        {
            pciLogoCover = ITextUtils.getITextImage( getPciLocalImageUrl( pciLogoCoverFilename ) );
            pciLogoHeader = ITextUtils.getITextImage( getPciLocalImageUrl( pciLogoHeaderFilename ) );
            pciLogoCover.scalePercent( 15.7f );
            pciLogoHeader.scalePercent( 14.14f );

        }
        catch( Exception e )
        {
            LogService.logIt(e, "BasePciReportTemplate.initPciSpecial() " );
        }
    }
    
    
    
    public URL getPciLocalImageUrl( String fn )
    {
        return com.tm2score.util.HttpUtils.getURLFromString( getPciBaseImageUrl() + "/" + fn );
    }
    
    
    public String getPciBaseImageUrl()
    {
        return RuntimeConstants.getStringValue( "baseurl" ) + "/resources/images/pci";
    }
    
    
    
    @Override
    public void initColors()
    {
        // Nothing. 
        if( ct2Colors == null )
            ct2Colors = PciColors.getCt2Colors( false );
    }
    
    @Override
    public Image getHraLogoBlackText() {
        return pciLogoCover;
    }

    
    
    
}
