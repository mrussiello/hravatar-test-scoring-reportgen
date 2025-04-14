/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.pci;



import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.user.Org;
import com.tm2score.event.TESScoreComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class PciTalentCard extends BasePciReportTemplate implements ReportTemplate
{
    
    
    public PciTalentCard()
    {
        super();
        
        this.devel = false;
        
        // this.redYellowGreenGraphs=false;
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            initPciSpecial(); 
            
            // LogService.logIt( "CTSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(true);

            addNewPage();           
            addReportInfoHeader();
            addTalentCardSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PciTalentCard.generateReport() " );                        
            throw new STException( e );
        }
    }


    public void addTalentCardSection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, "Talent Card Input", null, null, null );

            y -= PAD;

            
            List<TestEventScore> valueScores = getTesList(TalentCardText.VALUE_SCALES, true);
            List<TestEventScore> scoreSet2 = getTesList(TalentCardText.SCALESET2, true);
            List<TestEventScore> scoreSet3 = getTesList(TalentCardText.SCALESET3, true);
            List<TestEventScore> scoreSet4 = getTesList(TalentCardText.SCALESET4, false);
            
            
            LogService.logIt( "PciTalentCard.addTalentCardSection() found " + valueScores.size() + " value scores." );
            TestEventScore topValue = valueScores.size()>0 ? valueScores.get(0) : null;
            TestEventScore secondValue = valueScores.size()>1 ? valueScores.get(1) : null;

            List<String> sentences = new ArrayList<>();            
            sentences.add( " " );
            sentences.add( getSentence1() + " " + getSentence2( topValue ) + " " + getSentence3( secondValue ) );
            //sentences.add( getSentence2( topValue ) );
            //sentences.add( getSentence3( secondValue ) );

            sentences.add( " " );

            TestEventScore tesx = scoreSet2==null || scoreSet2.isEmpty() ? null : scoreSet2.get(0);
            TestEventScore tesy = scoreSet3==null || scoreSet3.isEmpty() ? null : scoreSet3.get(0);
            sentences.add( getSentence4( topValue ) + " "  + getSentence5( tesx ) + " " + getSentence6( tesy ) );
            //sentences.add( getSentence5( tesx ) );            
            //sentences.add( getSentence6( tesy ) );
            
            sentences.add( " " );
            tesx = scoreSet4==null || scoreSet4.isEmpty() ? null : scoreSet4.get(0);
            sentences.add( getSentence7( tesx ) + " " + getSentence8() );
            
            // sentences.add( getSentence8() );
                        
            float x = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN;
            
            PdfPTable t = new PdfPTable( 1 );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;
            t.setTotalWidth( txtW );
            t.setLockedWidth( true );
            // t.setWidths( new float[] {1} );
            setRunDirection( t );

            t.setHorizontalAlignment( Element.ALIGN_CENTER );

            PdfPCell dc = t.getDefaultCell();
            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setVerticalAlignment( Element.ALIGN_BOTTOM );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding( 3 );
            Phrase p; 
            
            for( String s : sentences )
            {
                p = new Phrase( s, this.fontXLarge );
                t.addCell( p );
            }
            
            t.writeSelectedRows(0, -1, x, y, pdfWriter.getDirectContent() );
            currentYLevel = y - t.calculateHeights();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "PciTalentCard.addTalentCardSection()" );
            throw new STException( e );
        }        
    }
    

    
    private String getSentence1()
    {
        String out = TalentCardText.S1_P0 + getRandomString( TalentCardText.S1_P1 ) + " " + getRandomString( TalentCardText.S1_P2 ) + " " + getRandomString( TalentCardText.S1_P3 );        
        out = subVals( out );
        return out;
    }
    
    private String getSentence2( TestEventScore highTes )
    {
        String out = getRandomString( TalentCardText.S2_P1 ) + " " + getRandomString( TalentCardText.S2_P2 ) + " ";  
        
        switch( highTes==null ? "Excellence" : highTes.getName() )
        {
            case "Excellence":
                out += TalentCardText.S2_P2_EXC;
                break;
            case "Human Potential":
                out += TalentCardText.S2_P2_HPO;
                break;
            case "Integrity":
                out += TalentCardText.S2_P2_INT;
                break;
            case "Culture":
                out += TalentCardText.S2_P2_CUL;
                break;
            case "Expectation of Being Led by a Servant Leader":
                out += TalentCardText.S2_P2_SER;
                break;
        }
        
        out = subVals( out );
        return out;
    }
    
    

    private String getSentence3( TestEventScore secondTes )
    {
        String out = getRandomString( TalentCardText.S3_P1 ) + " " + getRandomString( TalentCardText.S3_P2 ) + " ";  
        
        switch( secondTes==null ? "Human Potential" : secondTes.getName() )
        {
            case "Excellence":
                out += TalentCardText.S2_P2_EXC;
                break;
            case "Human Potential":
                out += TalentCardText.S2_P2_HPO;
                break;
            case "Integrity":
                out += TalentCardText.S2_P2_INT;
                break;
            case "Culture":
                out += TalentCardText.S2_P2_CUL;
                break;
            case "Expectation of Being Led by a Servant Leader":
                out += TalentCardText.S2_P2_SER;
                break;
        }
        
        out = subVals( out );
        return out;
    }
    
    private String getSentence4( TestEventScore highTes )
    {
        String out = TalentCardText.S4_P0 + " ";  
        
        switch( highTes==null ? "Integrity" : highTes.getName() )
        {
            case "Excellence":
                out += getRandomString( TalentCardText.S4_P1_EXC );
                break;
            case "Human Potential":
                out += getRandomString( TalentCardText.S4_P1_HPO );
                break;
            case "Integrity":
                out += getRandomString( TalentCardText.S4_P1_INT );
                break;
            case "Culture":
                out += getRandomString( TalentCardText.S4_P1_CUL );
                break;
            case "Expectation of Being Led by a Servant Leader":
                out += getRandomString( TalentCardText.S4_P1_SER );
                break;
        }
        
        out = subVals( out );
        return out;
    }
    
    private String getSentence5( TestEventScore highTes )
    {
        String out = "";  
        
        switch( highTes==null ? "Conscientiousness" : highTes.getName() )
        {
            case "Conscientiousness":
                out += getRandomString( TalentCardText.S5_P1_CON );
                break;
            case "Agreeableness":
                out += getRandomString( TalentCardText.S5_P1_AGR );
                break;
            case "Emotional Intelligence":
                out += getRandomString( TalentCardText.S5_P1_EMO );
                break;
        }
        
        out = subVals( out );
        return out;
    }

    private String getSentence6( TestEventScore highTes )
    {
        String out = "";  
        
        switch( highTes==null ? "Positive Future Outlook" : highTes.getName() )
        {
            case "Positive Future Outlook":
                out += getRandomString( TalentCardText.S6_P1_POS );
                break;
            case "Learning":
                out += getRandomString( TalentCardText.S6_P1_LEA );
                break;
        }
        
        out = subVals( out );
        return out;
    }

    
    private String getSentence7( TestEventScore highTes )
    {
        String out = TalentCardText.S7_P0 + " ";   
        
        switch( highTes==null ? "Optimism" : highTes.getName() )
        {
            case "Optimism":
                out += getRandomString( TalentCardText.S7_P1_OPT );
                break;
            case "Goal Orientation":
                out += getRandomString( TalentCardText.S7_P1_IND );
                break;
        }
        
        out = subVals( out );
        return out;
    }

    private String getSentence8()
    {
        String out = getRandomString( TalentCardText.S8_P1 ) + " " + getRandomString( TalentCardText.S8_P2 ) + " " + getRandomString( TalentCardText.S8_P3 );        
        out = subVals( out );
        return out;
    }
    
    
    
    private String subVals( String inStr )
    {
        String fn = reportData.u.getFirstName();        
        inStr = StringUtils.replaceStr(inStr, "[FIRSTNAME]", fn );        
        return inStr;
    }
    
    private String getRandomString( String[] vals )
    {
        return vals[getRandomIndex( vals.length-1 )];
    }
    
    private int getRandomIndex( int maxVal )
    {
        int out = (int) Math.floor((Math.random()*((float) (maxVal + 1) )));
        // LogService.logIt( "PciTalentCard.getRandomIndex() maxVal=" + maxVal + ", returning " + out );        
        return out;
    }
    
    
    
    private List<TestEventScore> getTesList( String[] scales, boolean descending)
    {
        List<TestEventScore> out = new ArrayList<>();
        
        for( TestEventScore tes : reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
        {
            for( String nm : scales )
            {
                if( tes.getName().equalsIgnoreCase( nm ) )
                    out.add( tes );
            }
        }
        
        Collections.sort( out, new TESScoreComparator( descending ) );        
        return out;
    }
    
    
    @Override
    public void addReportInfoHeader() throws Exception
    {
        try
        {
            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            else if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";

            String reportCompanyAdminEmail = "";

            String reportCompanyAdminName = reportData==null ? null : reportData.getReportCompanyAdminName();

            if( (reportCompanyAdminName==null || reportCompanyAdminName.isEmpty()) && reportData.getTestKey().getAuthUser() != null  )
            {
                reportCompanyAdminName = reportData.getTestKey().getAuthUser().getFullname();
                reportCompanyAdminEmail = reportData.getTestKey().getAuthUser().getEmail();
            }

            else if( StringUtils.isCurlyBracketed( reportCompanyAdminName ) )
                reportCompanyAdminName = "                        ";

            if( reportCompanyAdminName != null && reportCompanyAdminName.indexOf( "AUTOGEN" )>=0 )
                reportCompanyAdminName = null;

            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            
            if( !includeCompanyInfo )
                reportCompanyName = "                        ";
            
            boolean includePreparedFor = true; // includeCompanyInfo && reportCompanyAdminName!=null && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            if( !includePreparedFor )
                reportCompanyAdminName = "                        ";

            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            String thirdPartyTestEventIdentifierName = reportData.getThirdPartyTestEventIdentifierName();

            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, "Assessment Information", null, null, null );

            y -= PAD;

            Font fntRgt = fontXLarge;
            Font fntRgt2 = font;
            Font fntLft = font;

            float x = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN;

            // Now, let's create a table!
            PdfPTable t = new PdfPTable( 2 );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            t.setTotalWidth( txtW );
            t.setLockedWidth( true );
            t.setWidths( reportData.getIsLTR() ? new float[] {5,29} :  new float[] {29, 5} );
            setRunDirection( t );


            t.setHorizontalAlignment( Element.ALIGN_CENTER );

            PdfPCell dc = t.getDefaultCell();
            dc.setBorderWidth(0);
            dc.setHorizontalAlignment( Element.ALIGN_LEFT );
            dc.setVerticalAlignment( Element.ALIGN_BOTTOM );
            dc.setBorder( Rectangle.NO_BORDER );
            dc.setPadding( 2 );
            setRunDirection( dc );

            Phrase p = new Phrase( lmsg( "g.CandidateC"), fntLft );
            t.addCell( p );

            Chunk c = new Chunk( reportData.getUserName() + (!reportData.u.getUserType().getNamed() || reportData.u.getEmail()==null || StringUtils.isCurlyBracketed( reportData.u.getEmail() ) ? "" : ", "), fntRgt );
            p = new Phrase();
            p.add( c );
            c = new Chunk( ( !reportData.u.getUserType().getNamed() || reportData.u.getEmail()==null || StringUtils.isCurlyBracketed( reportData.u.getEmail() ) ? "" : reportData.u.getEmail()), fntRgt2 );
            p.add( c );
            t.addCell( p );

            if( hasThirdPartyId )
            {
                if( thirdPartyTestEventIdentifierName==null || thirdPartyTestEventIdentifierName.isEmpty() )
                    thirdPartyTestEventIdentifierName = lmsg( "g.ThirdPartyEventIdC" );
                else
                    thirdPartyTestEventIdentifierName += ":";

                t.addCell( new Phrase( thirdPartyTestEventIdentifierName, fntLft ) );
                t.addCell( new Phrase( thirdPartyId, fntLft ) );
            }



            if( reportData.u.getHasAltIdentifierInfo() )
            {
                String ainame = reportData.u.getAltIdentifierName();

                if( ainame == null || ainame.isEmpty() )
                    ainame = lmsg(  "g.DefaultAltIdentifierName" );

                t.addCell( new Phrase(  ainame + ":", fntLft ) );
                t.addCell( new Phrase( reportData.u.getAltIdentifier(), fntLft ) );
            }

            t.addCell( new Phrase( lmsg( "g.AssessmentC"), fntLft ) );
            t.addCell( new Phrase( reportData.getSimName(), fntLft ) );

            if( reportData.te.getProduct() != null && reportData.te.getProduct().getNeedsNameEnglish() && !reportData.getLocale().getLanguage().equalsIgnoreCase( "en" ) )
            {
                t.addCell( new Phrase( " ", fntLft ) );
                t.addCell( new Phrase( "English: " + reportData.te.getProduct().getNameEnglish(), fntLft ) );
            }

            String[] params = new String[] { I18nUtils.getFormattedDate(reportData.getLocale(), reportData.getTimeZone(), reportData.getTestKey().getStartDate() ),
                                             reportCompanyAdminName == null ? "" : reportCompanyAdminName,
                                             reportCompanyName == null ? "" : reportCompanyName,
                                             reportCompanyAdminEmail==null ? "" : reportCompanyAdminEmail };

            if( includePreparedFor )
            {
                String auth = reportData.getTestKey().getAuthUser() == null ? lmsg( "g.AuthStr" , params ) : lmsg( "g.AuthStrCombined" , params );

                t.addCell( new Phrase( lmsg( "g.AuthorizedC"), fntLft ) );
                t.addCell( new Phrase( auth, fntLft ) );
            }
            
            TestKey tk = reportData.tk;
            Org o = reportData.o;
                
            
            if( tk != null && o!=null )
            {
                if( o.getCustomFieldName1()!= null && !o.getCustomFieldName1().isEmpty()  )
                {
                    t.addCell( new Phrase( o.getCustomFieldName1() + ":", fntLft ) );
                    t.addCell( new Phrase( tk.getCustom1(), fntLft ) );                    
                }
                if( o.getCustomFieldName2()!= null && !o.getCustomFieldName2().isEmpty()  )
                {
                    t.addCell( new Phrase( o.getCustomFieldName2() + ":", fntLft ) );
                    t.addCell( new Phrase( tk.getCustom2(), fntLft ) );                    
                }
                if( o.getCustomFieldName3()!= null && !o.getCustomFieldName3().isEmpty()  )
                {
                    t.addCell( new Phrase( o.getCustomFieldName3() + ":", fntLft ) );
                    t.addCell( new Phrase( tk.getCustom3(), fntLft ) );                    
                }
            }

            t.addCell( new Phrase( lmsg( "g.StartedC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getStartDate(), reportData.getUser().getTimeZone() ), fntLft ) );

            t.addCell( new Phrase( lmsg( "g.FinishedC"), fntLft ) );
            t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getLastAccessDate(), reportData.getUser().getTimeZone() ), fntLft ) );            
                                   
            t.writeSelectedRows(0, -1, x, y, pdfWriter.getDirectContent() );

            currentYLevel = y - t.calculateHeights();

        }
        catch( Exception e )
        {
            LogService.logIt( e, "PciTalentCard.addReportInfoHeader()" );
            throw new STException( e );
        }
    }
    
    
    
    
    

}
