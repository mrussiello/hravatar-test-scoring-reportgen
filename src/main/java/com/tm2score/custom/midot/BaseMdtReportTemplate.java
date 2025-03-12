/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.midot;

import com.tm2score.custom.coretest2.*;
import com.tm2score.custom.coretest.*;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;


/**
 *
 * @author Mike
 */
public abstract class BaseMdtReportTemplate extends BaseCT2ReportTemplate implements ReportTemplate
{

    @Override
    public abstract byte[] generateReport() throws Exception;


    public void addCoverPage(boolean includeDescriptiveText) throws Exception
    {
        try
        {
            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 20;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

            ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), CT2_MARGIN, y, false );

            java.util.List<Chunk> cl = new ArrayList<>();

            boolean coverInfoOk = !reportData.getReportRuleAsBoolean( "omitcoverinfopdf" );
            
            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";

            String reportCompanyAdminName = reportData==null ? null : reportData.getReportCompanyAdminName();

            if( reportData.getTestKey().getAuthUser() != null && (reportCompanyAdminName==null || reportCompanyAdminName.isEmpty())  )
                reportCompanyAdminName = reportData.getTestKey().getAuthUser().getFullname();

            else if( StringUtils.isCurlyBracketed( reportCompanyAdminName ) )
                reportCompanyAdminName = "                        ";

            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            String thirdPartyTestEventIdentifierName = reportData.getThirdPartyTestEventIdentifierName();

            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            // LogService.logIt( "BaseMdtReportTemplate.addCoverPage() thirdPartyTestEventIdentifierName=" + thirdPartyTestEventIdentifierName + ", hasThirdPartyId=" + hasThirdPartyId );

            float titleWid = 20;

            cl.add( new Chunk( lmsg( "g.CandidateC" ), getFontXLarge() ) );
            cl.add( new Chunk( lmsg( "g.AssessmentC" ), getFontXLarge() ) );
            cl.add( new Chunk( lmsg( "g.CompletedC" ), getFontXLarge() ) );

            if( hasThirdPartyId )
            {
                if( thirdPartyTestEventIdentifierName==null || thirdPartyTestEventIdentifierName.isEmpty() )
                    thirdPartyTestEventIdentifierName = lmsg( "g.ThirdPartyEventIdC" );
                else
                    thirdPartyTestEventIdentifierName += ":";

                cl.add( new Chunk( thirdPartyTestEventIdentifierName, getFontXLarge() ) );
            }


            if( reportCompanyAdminName!=null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
                cl.add( new Chunk( lmsg( "g.PreparedForC" ), getFontXLarge() ) );

            titleWid = ITextUtils.getMaxChunkWidth( cl ) + 20;

            cl.clear();

            cl.add( new Chunk( reportData.getUserName(), getFontXLargeBold() ) );
            cl.add( new Chunk( reportData.getSimName(), getFontXLarge() ) );
            cl.add( new Chunk( reportData.getSimCompleteDateFormatted(), getFontXLarge() ) );

            if( reportCompanyAdminName != null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                cl.add( new Chunk( reportCompanyAdminName, getFontXLarge() ) );

                if( (!reportData.hasCustLogo() || custLogo==null) && reportCompanyName != null && !reportCompanyName.isEmpty() )
                    cl.add( new Chunk( reportCompanyName, getFontXLarge() ) );
            }
            
            float infoWid = ITextUtils.getMaxChunkWidth( cl ) + 10;

            if( custLogo!=null && custLogo.getScaledWidth()>infoWid )
                infoWid = custLogo.getScaledWidth();

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( 2 );

            t.setTotalWidth( reportData.getIsLTR()? new float[] { titleWid+4, infoWid+14 } : new float[] { infoWid+14, titleWid+4 } );
            t.setLockedWidth( true );

            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );

            setRunDirection(c);

            Font font = this.fontXLarge;

            if( coverInfoOk )
            {
                t.addCell( new Phrase( lmsg( "g.CandidateC" ) , font ) );
                t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

                t.addCell( new Phrase( lmsg( "g.AssessmentC" ) , font ) );
                t.addCell( new Phrase( reportData.getSimName(), font ) );

                t.addCell( new Phrase( lmsg( "g.CompletedC" ) , font ) );
                t.addCell( new Phrase( reportData.getSimCompleteDateFormatted(), font ) );

                if( hasThirdPartyId )
                {
                    t.addCell( new Phrase( thirdPartyTestEventIdentifierName , font ) );
                    t.addCell( new Phrase( reportData.getThirdPartyTestEventIdentifier(), font ) );
                }
            }
            
            if( reportCompanyAdminName!=null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                // LogService.logIt( "BaseMdtReportTemplate.addCoverPage() Adding prepared " + lmsg( "g.PreparedForC" ) );

                if( coverInfoOk )
                {
                    t.addCell( new Phrase( lmsg( "g.PreparedForC" ) + " " , font ) );
                    t.addCell( new Phrase( reportCompanyAdminName, font ) );

                    t.addCell( new Phrase( "" , font ) );
                }
                
                if( reportData.hasCustLogo() && custLogo!=null )
                {
                    c = new PdfPCell( custLogo );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment(Element.ALIGN_LEFT );
                    c.setPadding( 0 );
                    c.setPaddingTop( 12 );
                    setRunDirection(c);
                    t.addCell( c );
                }

                else if( coverInfoOk )
                    t.addCell( new Phrase( reportCompanyName, fontXLarge ) );
            }

            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, getHraOrangeColor(), false );

            // Add the blue below
            ITextUtils.addDirectColorRect( pdfWriter, getHraBaseReportColor(), 0, 0, pageWidth, pageHeight/2, 0, 1, true );


            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            // c.setBorder( Rectangle.BOX );
            //c.setBorderWidth( 0.5f );
            //c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            //c.setBorderColor( BaseColor.DARK_GRAY );

            // t.addCell( "\n" );

            c = new PdfPCell( new Phrase( lmsg( "g.TestResultsAndInterviewGuide" ) , getHeaderFontXXLargeWhite() ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            t.addCell( "\n\n\n\n\n" );

            String coverDescrip = lmsg( "g.CT2CoverDescrip_MIDOT", new String[] {reportData.getSimName()} );

            c = new PdfPCell( new Phrase( coverDescrip , fontLLWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection(c);

            t.addCell( c );


            tableH = t.calculateHeights(); //  + 500;

            tableY = pageHeight/2 - (pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );


            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);


            c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFontWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            // tableH = t.calculateHeights(); //  + 500;

            tableY = 20;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );


        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseMdtReportTemplate.addCoverPage()" );
        }
    }


    @Override
    public String getReportGenerationNotesToSave()
    {
        return null;
    }


}
