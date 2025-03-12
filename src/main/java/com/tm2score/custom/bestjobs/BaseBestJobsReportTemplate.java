/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.bestjobs;

import com.tm2score.custom.coretest2.*;
import com.tm2score.custom.coretest.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.onet.Soc;

import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TESScoreComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.TableBackground;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.onet.OnetElement;
import com.tm2score.onet.OnetImportanceComparator;
import com.tm2score.onet.OnetJobZoneType;
import com.tm2score.profile.ProfileFloat1Comparator;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportRegenerationThread;
import com.tm2score.report.ReportTemplate;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;


/**
 *
 * Report Rules:
 *
 * ct3risktoend=1 means place risk factors at the end of the report
 * ct3riskremove=1 means no risk factors in the report, anywhere.
 *
 * allnointerview=1 means do not include the interview guide
 *
 *
 * @author Mike
 */
public abstract class BaseBestJobsReportTemplate extends CT2ReportSettings implements ReportTemplate
{
    public static String BEST_JOBS_BUNDLE = "com.tm2score.custom.bestjobs.BestJobsMessages";
    
    public static int MAX_PROFILES_TO_SHOW = 15;
    
    // public static List<Long> ct2ReportIds = null;
    
    // public static int MIN_MATCHING_PROFILES_TO_CREATE_REPORT = 2;
    
    public Image custLogo = null;
    public Image mosaic = null;
    public Image mosaic2 = null;
    public Image mosaic3 = null;
    public Image mosaic4 = null;
    public Image scout = null;
    public Image onet = null;
    public Image indeed = null;
    public Image careerBuilder = null;
    public static String mosaicFilename =  "job-match-cover-photo-800.png"; // group-mosaic.jpg"; 
    public static String mosaic2Filename = "opportunities.png"; // "magglass-mosaic2.png"; 
    public static String mosaic3Filename = "peopleatwork3_500.jpg";
    public static String mosaic4Filename = "peopleatwork4_600.jpg";
    public static String scoutFilename = "thescout_160.png";
    public static String onetFilename = "onet-40.png";
    public static String indeedFilename = "indeed-40.png";
    public static String careerBuilderFilename = "careerbuilder-40.png";
    
    
     public ReportData reportData = null;
    public CT2ReportData ctReportData = null;

    public Document document = null;

    public ByteArrayOutputStream baos;

    public PdfWriter pdfWriter;

    public float pageWidth = 0;
    public float pageHeight = 0;
    public float usablePageHeight = 0;

    public String title;

    public float headerHgt;
    public float footerHgt;

    public float lastY = 0;

    public TableBackground dataTableEvent;
    public TableBackground tableHeaderRowEvent;

    public ReportUtils reportUtils;

    public float PAD = 5;
    public float TPAD = 8;

    // float bxX;
    // float bxWid;
    //float barGrphWid;
    //float barGrphX;
    public float lineW = 0.8f;


    public float currentYLevel = 0;
    public float previousYLevel = 0;

    public java.util.List<String> prepNotes;
    
    public String competencySummaryStr = null;
    
    public List<Profile> bestProfilesList = null;
    public List<Profile> oneLevelUpProfilesList = null;
    public List<EeoMatch> eeoMatchList = null;
    
    public TestEvent jobSpecificTestEvent = null;
    
    public Date currentDate = null;

    boolean needsOnetTrans = false;
    
    boolean careerScoutV2 = false;
    
    BestJobsReportUtils bestJobsReportUtils;

    OnetJobZoneType jobZone = null;
    
    @Override
    public abstract byte[] generateReport() throws Exception;


    @Override
    public void init( ReportData rd ) throws Exception
    {
        try
        {
            currentDate = new Date();
            
            reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

            ctReportData = new CT2ReportData();

            initLocales();
                                    
            // LogService.logIt( "BaseBestJobsReportTemplate.init() needsKeyCheck=" + reportData.getNeedsKeyCheck() + ", needsTestContentTrans=" + reportData.getNeedsTestContentTrans() + ", needsOnetTrans=" + needsOnetTrans + ", reportLocale=" + reportData.getLocale() );

            initFonts();

            initColors();        
            
            prepNotes = new ArrayList<>();

            document = new Document( PageSize.LETTER );

            baos = new ByteArrayOutputStream();

            pdfWriter = PdfWriter.getInstance(document, baos);

            LogService.logIt( "BaseBestJobsReportTemplate.init() title=" + rd.getReportName() + ", jobZone=" + (jobZone==null ? "null" : jobZone.getJobZoneId()));

            CT2HeaderFooter hdr = new CT2HeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this, null );

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

            //LogService.logIt( "BaseBestJobsReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

            dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );

            tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );

            findMatchingJobSpecificTestEvent();

            loadBestProfilesAndEeoCategoryScores();   
            
            // loadSocIntoBestProfiles();
            
            if( 1==2 )
            {
                StringBuilder sb = new StringBuilder();
                
                for( Profile p : bestProfilesList )
                {
                    sb.append(  p.getSoc().getTitleTruncatedWithSoc() + ", match=" + p.getFloatParam1() + "\n" );
                }
                // LogService.logIt( "BaseBestJobsReportTemplate.init()\n" +  sb.toString() );
            }
        }

        catch( STException e )
        {
            LogService.logIt( "BaseBestJobsReportTemplate.init() STException, " + this.reportData.toString() );            
            throw e;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.init() " + this.reportData.toString() );            
            throw new STException(e);
        }
    }

    
    
    public void initLocales()
    {            
        Locale rptLoc = reportData.getLocale();

        Locale testContentLoc = reportData.getTestContentLocale();

        if( testContentLoc==null )
            testContentLoc = Locale.US;

        if( !rptLoc.getLanguage().equalsIgnoreCase( Locale.US.getLanguage() ))
        {
            this.needsOnetTrans=isOkToAutoTranslate( rptLoc );
            reportData.setNeedsKeyCheck(isOkToAutoTranslate( rptLoc ));

            if( languageUtils==null )
                languageUtils = new LanguageUtils();
        }

        if( !testContentLoc.getLanguage().equalsIgnoreCase( rptLoc.getLanguage() ) && reportData.equivSimJUtils==null && isOkToAutoTranslate(testContentLoc, rptLoc ) )
        {
            reportData.setNeedsTestContentTrans( true );

            if( languageUtils==null )
                languageUtils = new LanguageUtils();
        }        
    }
    
    
    public synchronized void initFonts() throws Exception
    {
        initSettings( reportData );

        String logoUrl = reportData.getReportCompanyImageUrl();

        if( logoUrl == null && reportData.s!=null && reportData.s.getReportLogoUrl()!=null && !reportData.s.getReportLogoUrl().isBlank() )
            logoUrl = reportData.s.getReportLogoUrl() ;

        if( logoUrl == null && reportData.o.getReportLogoUrl()!=null && !reportData.o.getReportLogoUrl().isBlank() )
            logoUrl = reportData.o.getReportLogoUrl() ;

        if( logoUrl != null && StringUtils.isCurlyBracketed( logoUrl ) )
            logoUrl = RuntimeConstants.getStringValue( "translogoimageurl" );

        try
        {
            custLogo = logoUrl == null || logoUrl.isEmpty() ? null : ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( logoUrl ) );
        }

        catch( Exception e )
        {
            LogService.logIt( "BaseCt2ReportTemplate.initFonts() getting custLogo: " + logoUrl );
        }
        
        mosaic = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.mosaicFilename ));        
        mosaic.scalePercent( 60 );

        mosaic2 = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.mosaic2Filename ));        
        mosaic2.scalePercent( 65 );

        mosaic3 = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.mosaic3Filename ));        
        mosaic3.scalePercent( 50 );

        mosaic4 = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.mosaic4Filename ));        
        mosaic4.scalePercent( 70 );

        scout = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.scoutFilename ));        
        scout.scalePercent( 85 );
        
        onet = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.onetFilename ));        
        onet.scalePercent( 40 );
        
        indeed = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.indeedFilename ));        
        indeed.scalePercent( 40 );
        
        careerBuilder = ITextUtils.getITextImage( getBestJobsImageUrl( BaseBestJobsReportTemplate.careerBuilderFilename ));        
        careerBuilder.scalePercent( 35 );
        

        // !reportData.hasCustLogo() ? null : ITextUtils.getITextImage( reportData.getCustLogoUrl() );

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
        
        String reportCompanyName = reportData.getReportCompanyName();

        if( StringUtils.isCurlyBracketed( reportCompanyName ) )
            reportCompanyName = "                      ";

        if( reportCompanyName == null || reportCompanyName.isEmpty() )
            reportCompanyName = reportData.getOrgName();

        title = StringUtils.replaceStr( title, "[ORGNAME]", reportCompanyName );

        if( reportData.hasUserInfo() )
            title = StringUtils.replaceStr( title, "[USERNAME]", reportData.getUserName() );

        else
            title = StringUtils.replaceStr( title, "[USERNAME]", "" );


        
        // LogService.logIt( "BaseBestJobsReportTemplate.initFonts() title=" + title );
    }


    @Override
    public String getReportGenerationNotesToSave()
    {        
        return BestJobsReportUtils.createCompactInfoString( bestProfilesList, eeoMatchList );
        
        /*
        StringBuilder sb = new StringBuilder();
        int ct = 0;
        
        if( bestProfilesList!=null && !bestProfilesList.isEmpty() )
        {
            ct = 0;
            
            sb.append( "[" + Constants.RIASEC_COMPACT_INFO_KEY + "]" );

            sb.append( getDetailHraInfoHeadersCSV() );

            for( Profile p : bestProfilesList )
            {
                ct++;
                sb.append( "|" + getDetailHraInfoCSV( ct, p) );
            }

        }
        
        if( eeoMatchList!=null && !eeoMatchList.isEmpty() )
        {
            sb.append( "[" + Constants.EEOCAT_COMPACT_INFO_KEY + "]" );
            sb.append( getDetailHraInfoHeadersEeoCatCSV() );
            
            ct = 0;
            
            for( EeoMatch m : eeoMatchList )
            {
                ct++;
                sb.append( "|" + getDetailHraInfoEeoCatCSV( ct, m ) );                
            }            
        }
                
        return sb.toString();
        */
    }
    
    
    @Override
    public void initColors()
    {
        // Nothing. 
        if( ct2Colors == null )
            ct2Colors = BestJobColors.getCt2Colors( devel );
    }

    
    public String getCurrentDateFormatted()
    {
        if( currentDate== null )
            currentDate = new Date();
        
        return I18nUtils.getFormattedDate(reportData.getLocale() , reportData.getTimeZone(), currentDate );
    }
    
    
    
    public void findMatchingJobSpecificTestEvent() throws Exception
    {
        jobSpecificTestEvent = null;
     
        if( careerScoutV2 )
            return;
        
        BestJobsReportFacade bestJobsReportFacade = BestJobsReportFacade.getInstance();
        
        List<TestEvent> tel = bestJobsReportFacade.getMostRecentTestEventsForUserId( reportData.te.getUserId(), reportData.te.getOrgId(), 20 );
        
        if( tel.isEmpty() )
            return;

        EventFacade eventFacade = EventFacade.getInstance();
        
        Product p;
        
        for( TestEvent te : tel )
        {
            p = eventFacade.getProduct( te.getProductId() );
            
            if( p==null )
                continue;
            
            if( !p.getProductType().getIsSimOrCt5Direct() )
                continue;
            
            if( p.getConsumerProductTypeId() != ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId() )
                continue;
            
            if( !te.getTestEventStatusType().getIsCompleteOrHigher() )
                continue;
            
            if( !te.getTestEventStatusType().getIsScoredOrHigher() )
                continue;
            
            // Got one!
            te.setProduct(p);            
            jobSpecificTestEvent = te;
            
            te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), false ) );
            
            te.setProduct( eventFacade.getProduct( te.getProductId() ));
            
            // get the Sim Descriptor
            SimDescriptor sd = eventFacade.getSimDescriptor( te.getSimId(), te.getSimVersionId(), false );
            
            te.setSimDescriptor(sd);
           
            // LogService.logIt( "findMatchingJobSpecificTestEvent() Found a Job Specific Test Event to use: " + te.toString() );
            return;
        }
    }
    
    
    public synchronized void loadBestProfilesAndEeoCategoryScores() throws Exception
    {
        // LogService.logIt( "BaseBestJobsReportTemplate.loadBestProfiles() START jobZone=" + (jobZone==null ? "null" : jobZone.getJobZoneId()) );

        if( bestProfilesList!=null )
            return;
        
        // Next, calculate the top jobs for this person.
        if( bestJobsReportUtils==null )
            bestJobsReportUtils = new BestJobsReportUtils(careerScoutV2);

        loadBestProfilesAndEeoCategoryScoresFromTestEvent( 1 );

        if( bestProfilesList==null || bestProfilesList.isEmpty() )
        {
            eeoMatchList = new ArrayList<>();
            bestProfilesList = bestJobsReportUtils.getTopCT3ProfileMatches(MAX_PROFILES_TO_SHOW, reportData.getTestEvent(), jobSpecificTestEvent, jobZone, -1, 1, 1, true, eeoMatchList );
            Collections.sort( bestProfilesList, new ProfileFloat1Comparator() );
            Collections.reverse(bestProfilesList ); 

            // this will sort in proper order.
            Collections.sort( eeoMatchList );
            Collections.reverse(eeoMatchList );   

            int rank = 1;
            for( EeoMatch m : eeoMatchList )
            {
                m.setRank(rank);
                rank++;
            }
        }            

        if( 1==2 )
        {
            StringBuilder sb = new StringBuilder();

            sb.append( "bestProfilesList.size=" + bestProfilesList.size() );

            for( Profile p : bestProfilesList )
            {
               sb.append(p.getSoc().toString() + "\n" );
               //LogService.logIt( "BaseBestJobsReportTemplate.loadBestProfiles() " +  p.getSoc().toString() );
            }

            LogService.logIt( "BaseBestJobsReportTemplate.loadBestProfiles() " + sb.toString() );
        }
        
        
    }
    
    
    @Override
    public void dispose() throws Exception
    {
        if( baos != null )
            baos.close();
    }

    
    public void loadBestProfilesAndEeoCategoryScoresFromTestEvent( int socDataCode )
    {
        TestEventScore rtes = reportData.r==null ? null : reportData.te.getTestEventScoreForReportId( reportData.r.getReportId(), reportData.getLocale().toString() );
        
        if( rtes==null )
        {
            // LogService.logIt( "BaseBestJobsReportTemplate.loadBestProfilesAndEeoCategoryScoresFromTestEvent() No Report TestEventScore found for testEventId=" + reportData.te.getTestEventId() + " and reportId=" + reportData.getReport().getReportId() );
            return;
        }

        if( rtes.getTextParam1()==null || rtes.getTextParam1().isBlank() )
            return;
        
        String rs = StringUtils.getBracketedArtifactFromString(rtes.getTextParam1(), Constants.RIASEC_COMPACT_INFO_KEY);
        String es = StringUtils.getBracketedArtifactFromString(rtes.getTextParam1(), Constants.EEOCAT_COMPACT_INFO_KEY);
        
        if( (rs==null || rs.isBlank()) && (es==null || es.isBlank()) )
            return;
        
        if( rs!=null && !rs.isBlank() )
        {
            if( bestJobsReportUtils==null )
                bestJobsReportUtils = new BestJobsReportUtils(careerScoutV2);
            bestProfilesList = bestJobsReportUtils.getBestProfilesListWithData(rs, ",", socDataCode );
            //if( bestProfilesList!=null )
            //{
            //    LogService.logIt( "BaseBestJobsReportTemplate.loadBestProfilesAndEeoCategoryScoresFromTestEvent() Found existing profiles: " + bestProfilesList.size() );
                
            //}
        }
        
        if( es!=null && !es.isBlank() )
        {
            eeoMatchList = BestJobsReportUtils.getEeoCategoryListWithData(es, "," );
            //if( eeoMatchList!=null )
            //    LogService.logIt( "BaseBestJobsReportTemplate.loadBestProfilesAndEeoCategoryScoresFromTestEvent() Found existing eeoMatchList: " + eeoMatchList.size() );
        }
    }

    
    @Override
    public Locale getReportLocale()
    {
        if( this.reportData!=null )
            return reportData.getLocale();
        
        return Locale.US;
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




    public void addIntroSection() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, reportData.getReportName(), null );

            y -= TPAD;

            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.2f, .45f, .2f } : new float[] { .2f, .45f , 0.2f };


            // String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            // String thirdPartyTestEventIdentifierName = reportData.getThirdPartyTestEventIdentifierName();

            // boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            // LogService.logIt( "BaseMdtReportTemplate.addCoverPage() thirdPartyTestEventIdentifierName=" + thirdPartyTestEventIdentifierName + ", hasThirdPartyId=" + hasThirdPartyId );
            boolean includeCompanyInfo = !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );


            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t2 = new PdfPTable( cols );

            setRunDirection(t2 );
            // float importanceWidth = 25;

            t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t2.setWidths( colRelWids );
            t2.setLockedWidth( true );
            // t.setHeaderRows( 1 );


            c = t2.getDefaultCell();
            c.setPadding( 6 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            c = new PdfPCell( new Phrase( bmsg("b.ForC") , fontLarge ) );
            c.setColspan(1);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            c = new PdfPCell( new Phrase( reportData.getUserName() , fontXLargeBlack ) );
            c.setColspan(2);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            c = new PdfPCell( new Phrase( bmsg("b.EmailC") , fontLarge ) );
            c.setColspan(1);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            if( reportData.getUser().getUserType().getNamed() )
            {
                c = new PdfPCell( new Phrase( reportData.getUser().getEmail() , fontLarge ) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );
            }
            
            c = new PdfPCell( new Phrase( bmsg("b.PreparedDateC") , fontLarge ) );
            c.setColspan(1);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            c = new PdfPCell( new Phrase( getCurrentDateFormatted() , fontLarge ) );
            c.setColspan(2);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );
            
            if( includeCompanyInfo && getHasSponsor() )
            {
                c = new PdfPCell( new Phrase( bmsg( "b.SponsoringEmployerC" ) , fontLarge ) );
                c.setColspan(1);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );

                c = new PdfPCell( new Phrase( reportData.getOrgName() , fontLarge ) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );
            }
            
            
            c = new PdfPCell( new Phrase( "\n" + bmsg("b.IntroWhatTests")  , fontLarge ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            if( reportData.getTestEvent()!=null )
            {
                c = new PdfPCell( new Phrase( reportData.getSimName() , fontLarge ) );
                c.setColspan(1);
                c.setBorder( Rectangle.NO_BORDER );
                c.setPaddingLeft(20);
                setRunDirection( c );
                t2.addCell( c );

                c = new PdfPCell( new Phrase( bmsg( "b.CompletedOnX", new String[] { I18nUtils.getFormattedDate(reportData.getLocale(), reportData.getTimeZone(), reportData.getTestEvent().getLastAccessDate() )}) , fontLarge ) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );                
            }

            if( jobSpecificTestEvent!=null )
            {
                c = new PdfPCell( new Phrase( jobSpecificTestEvent.getProduct().getName() , fontLarge ) );
                c.setColspan(1);
                c.setBorder( Rectangle.NO_BORDER );
                c.setPaddingLeft(20);
                setRunDirection( c );
                t2.addCell( c );

                c = new PdfPCell( new Phrase( bmsg( "b.CompletedOnX", new String[] { I18nUtils.getFormattedDate(reportData.getLocale(), reportData.getTimeZone(), jobSpecificTestEvent.getLastAccessDate() )}) , fontLarge ) );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );                
            }

            c = new PdfPCell( new Phrase( "\n" + bmsg("b.HowToUseReport")  , this.fontXLargeBoldBlack ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            c = new PdfPCell( new Phrase( bmsg("b.HowToUseReportP1")  , fontLarge ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            t2.addCell( c );

            if( getHasSponsor() )
            {
                Paragraph p1 = new Paragraph();

                p1.add( new Chunk(  bmsg("b.GotTheJobP1", new String[] {reportData.getOrgName()} ) + " " , fontLargeBold ) );
                p1.add( new Chunk(  bmsg("b.GotTheJobP1b", new String[] {reportData.getOrgName()} )  , fontLarge ) );

                c = new PdfPCell( new Phrase( "\n" + bmsg("b.GotTheJob")  , fontXLargeBoldBlack ) );
                c.setColspan(3);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );

                c = new PdfPCell( p1 );
                c.setColspan(3);
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                t2.addCell( c );
            }
            
            c = new PdfPCell( new Phrase( ( this.getHasSponsor() ? "\n\n\n\n\n" : "\n\n\n\n\n\n\n\n\n\n" ) + bmsg("b.IntroNotesContinue")  , fontXLargeBold ) );
            c.setColspan(3);
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            setRunDirection( c );
            // touter.addCell( c );
            
            t2.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - t2.calculateHeights();
            
            float imageX = (pageWidth - mosaic2.getScaledWidth())/2; 

            ITextUtils.addDirectImage( pdfWriter, mosaic2, imageX, currentYLevel - mosaic2.getScaledHeight()-40, false );
            
            currentYLevel -= mosaic2.getScaledHeight()+30;
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addIntroSection()" );

            throw e;
        }
    }

    
    public void addRiasecResultsSection( String titleKey ) throws Exception
    {
        try
        {
            List<TestEventScore> tesl = reportData.te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );
            
            if( tesl.isEmpty() )
                return;
            
            ListIterator<TestEventScore> li = tesl.listIterator();
            
            TestEventScore tesx;
            
            while( li.hasNext() )
            {
                tesx=li.next();
                
                if( tesx.getSimCompetencyClassId()!=SimCompetencyClass.INTERESTS_COMBO.getSimCompetencyClassId() && tesx.getSimCompetencyClassId()!=SimCompetencyClass.NONCOG_COMBO.getSimCompetencyClassId() && tesx.getSimCompetencyClassId()!=SimCompetencyClass.SCOREDINTEREST.getSimCompetencyClassId() )
                    li.remove();
            }
                        
            
            if( tesl.isEmpty() )
            {
                return;
            }
            
            Collections.sort( tesl, new TESScoreComparator() );
            
            Collections.reverse(tesl);
            
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel - 10;

            float y = addTitle( previousYLevel, bmsg( titleKey ), null );

            y -= TPAD;

            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.20f, .2f, .6f } : new float[] { 0.6f, .2f, .20f };

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            setRunDirection(t );
            // float importanceWidth = 25;

            // lightBoxBorderWidth = 0;
            
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setKeepTogether( true );
                                    
            // Create header
            c = new PdfPCell( new Phrase( bmsg( "b.Theme"), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "s.Score100" ), fontLargeWhite ) );
            c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "b.LevelOfInterest" ), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT :  Rectangle.TOP | Rectangle.LEFT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            int counter = 0;
            
            boolean shade = true;
            for( TestEventScore tes : tesl )
            {
                counter++;
                
                c = new PdfPCell( new Phrase( tes.getName(), fontLarge ) );
                
                if( counter== tesl.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD + 10 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 0 ), fontLargeBold ) );
                if( counter== tesl.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );                
                c.setPaddingBottom( 5 );
                setRunDirection( c );
                t.addCell(c);

                // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                c = new PdfPCell( new Phrase("") );
                if( counter== tesl.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
                c.setPadding( 1 );
                
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                // c.setPadding(8);

                c.setPaddingTop( 7 );
                c.setPaddingBottom( 8 );

                c.setFixedHeight(24);
                c.setCellEvent(new BestJobsInterestScoreGraphicCellEvent( tes , baseFontCalibri ) );
                                
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);                
                
                shade = !shade;
            }
            
            currentYLevel = addTableToDocument(y, t );
            
            cols = 2;
            colRelWids = reportData.getIsLTR() ? new float[] { 0.30f, .70f } : new float[] { 0.70f, .30f };

            t = new PdfPTable( cols );

            setRunDirection(t );

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setKeepTogether( true );
            
            String txt;
           
            Chunk ch;
            Phrase ph;
            Chunk nmBChk;
            
            for( TestEventScore tes : tesl )
            {
                c = new PdfPCell( new Phrase( tes.getName(), this.fontXLargeBold ) );
                
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 4 );
                c.setPaddingLeft(1);
                c.setPaddingBottom( 2 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                txt=bmsg( "s.YourScoreC", new String[]{I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 0 )}  );
                
                c = new PdfPCell( new Phrase( txt, fontLarge ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( 1 );
                c.setPaddingTop(2);
                c.setPaddingBottom( 2 );
                setRunDirection( c );
                t.addCell(c);

                nmBChk = new Chunk( tes.getName(), fontLargeBold );
                
                ph = new Phrase();
                
                txt = bmsg( "b.PeopleWith" ) + " ";
                
                ch = new Chunk( txt, fontLarge );
                
                ph.add( ch );
                
                ph.add( nmBChk );
                
                txt = " " + bmsg( "b.Descrip." + ( tes.getNameEnglish()==null || tes.getNameEnglish().equals(tes.getName()) ? tes.getName() : tes.getNameEnglish() ) + ".1" ) + " ";
                
                ch = new Chunk( txt, fontLarge );
                
                ph.add( ch );
                
                
                // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                c = new PdfPCell( ph );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 1 );
                c.setColspan(2);
                c.setPaddingBottom( 12 );                
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                setRunDirection( c );
                t.addCell(c);
                
            }
            
            currentYLevel = addTableToDocument(currentYLevel, t );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addRiasecResultsSection()" );

            throw e;
        }        
    }
    
    
    public void addEEOMatchesSummarySection() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel - 10;

            float y = addTitle( previousYLevel, bmsg( "b.JobCategoryMatches" ), null );

            y -= TPAD;
            
            addEEOMatchTable(this.eeoMatchList, y, true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addTopMatchesSummarySection()" );
            throw e;
        }        
    }

    public void addEEOMatchTable( List<EeoMatch> matchList, float y, boolean showPercentMatch) throws Exception
    {
        try
        {
            if( matchList==null )
                return; // matchList=new ArrayList<>();
            
            if( matchList.isEmpty() )
                return;
            
            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .05f, .13f, .47f, .1f } : new float[] { .1f,.47f,.13f,.05f };

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( true );
            
            
            // lightBoxBorderWidth=0;
            
            // Create header
            c = new PdfPCell( new Phrase( bmsg( "b.Rank"), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setColspan( 1 );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "b.JobType" ), fontLargeWhite ) );
            c.setColspan( 1 );
            c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( bmsg( "b.WorkStylesH2" ), fontLargeWhite ) );
            c.setColspan( 1 );
            c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);

            
            c = new PdfPCell( new Phrase( bmsg( "b.DegreeMatch" ), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT :  Rectangle.TOP | Rectangle.LEFT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            
            int counter = 0;
            
            boolean shade = true;
            
            // OK, header done, now for the profiles
            for( EeoMatch p : matchList )
            {
                counter++;
                
                c = new PdfPCell( new Phrase( Integer.toString(counter), fontLarge ) );
                
                if( counter== matchList.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
                c.setColspan( 1 );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( otrans( p.getEeoTitle(), false ), font ) );
                c.setColspan( 1 );
                if( counter== matchList.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( otrans( p.getEeoJobCategoryType().getDescription(reportData.getLocale()), false ), font ) );
                c.setColspan( 1 );
                if( counter== matchList.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                
                
                // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getAveragePercentMatch(), showPercentMatch), font ) );
                if( counter== matchList.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                
                shade = !shade;
            }
            
            currentYLevel = addTableToDocument( y, touter );
            
            //touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addEEOMatchesSummarySection()" );
            throw e;
        }        
    }
    
    public void addTopMatchesSummarySection() throws Exception
    {
        try
        {
            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel - 10;

            float y = addTitle( previousYLevel, bmsg( "b.TopMatches" ), null );

            y -= TPAD;
            
            addMatchTable(this.bestProfilesList, y, true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addTopMatchesSummarySection()" );
            throw e;
        }        
    }

    public void addMatchTable( List<Profile> matchList, float y, boolean showPercentMatch) throws Exception
    {
        try
        {
            if( matchList==null )
                matchList=new ArrayList<>();
            
            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.15f, .5f, .35f } : new float[] { 0.15f, .5f, .35f };

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( true );
            
            
            // lightBoxBorderWidth=0;
            
            // Create header
            c = new PdfPCell( new Phrase( bmsg( "b.Rank"), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setColspan( 1 );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "b.JobTitle" ), fontLargeWhite ) );
            c.setColspan( 1 );
            c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            
            c = new PdfPCell( new Phrase( bmsg( "b.DegreeMatch" ), fontLargeWhite ) );
            c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT :  Rectangle.TOP | Rectangle.LEFT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            touter.addCell(c);
            
            int counter = 0;
            
            boolean shade = true;
            
            // OK, header done, now for the profiles
            for( Profile p : matchList )
            {
                counter++;
                
                c = new PdfPCell( new Phrase( Integer.toString(counter), fontLarge ) );
                
                if( counter== matchList.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
                c.setColspan( 1 );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( otrans( p.getSoc().getTitleSingular(), false ), setBoldMatch( p.getFloatParam1() ) ? fontLargeBold : fontLarge ) );
                c.setColspan( 1 );
                if( counter== matchList.size() )
                    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM);
                else
                    c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);

                // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getFloatParam1(), showPercentMatch), fontLarge ) );
                if( counter== matchList.size() )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                c.setBorderColor( ct2Colors.lighterBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                if( shade )
                    c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
                
                shade = !shade;
            }
            
            currentYLevel = addTableToDocument( y, touter );
            
            //touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addMatchTable()" );

            throw e;
        }
        
    }
    
    

    public void addHRAInfoSection() throws Exception
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            
            Map<String,Float> rm = BestJobsReportUtils.getScoreMap( reportData.getTestEvent() );
            
            // RIASEC:  20:30:50:39:39:22 ter: 4939399 tej: 4003030, o: 3999 ex:1 ed: 2 tr:5 jz:3-4
            sb.append( "Riasec:" );
            
            Float scr = rm.get("Realistic");            
            sb.append( scr==null ? "00" : Math.round( scr ) );
            
            scr = rm.get("Investigative");            
            sb.append( ":" + (scr==null ? "00" : Math.round( scr ) ) );            

            scr = rm.get("Artistic");            
            sb.append( ":" + (scr==null ? "00" : Math.round( scr ) ) );            

            scr = rm.get("Social");            
            sb.append( ":" + (scr==null ? "00" : Math.round( scr ) ) );            
            
            scr = rm.get("Enterprising");            
            sb.append( ":" + (scr==null ? "00" : Math.round( scr ) ) );            
            
            scr = rm.get("Conventional");            
            sb.append( ":" + (scr==null ? "00" : Math.round( scr ) ) );            
            
            sb.append( " Ter:" + reportData.getTestEvent().getTestEventId() );

            sb.append( " Tej:" + ( jobSpecificTestEvent==null ? "00" : this.jobSpecificTestEvent.getTestEventId() ) );

            sb.append( " O:" + reportData.getOrg().getOrgId() );
            
            sb.append( " Ex:" );
            scr = rm.get("Experience");            
            sb.append( scr==null ? "00" : Math.round( scr )  );

            sb.append( " Ed:" );
            scr = rm.get("Education");            
            sb.append( scr==null ? "00" : Math.round( scr )  );

            sb.append( " Tr:" );
            scr = rm.get("Training");            
            sb.append( scr==null ? "00" : Math.round( scr )  );

            // Font fnt = getFontXLarge();


            // String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            // String thirdPartyTestEventIdentifierName = reportData.getThirdPartyTestEventIdentifierName();

            // boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();
            List<Object> sbl = new ArrayList<>();
            
            sbl.add( sb.toString() );
            
            Paragraph titleP = new Paragraph();
            
            titleP.add( new Phrase( bmsg( "b.HRAUseOnly"), fontLargeBold ) );
            
            // LogService.logIt( "BaseMdtReportTemplate.addCoverPage() thirdPartyTestEventIdentifierName=" + thirdPartyTestEventIdentifierName + ", hasThirdPartyId=" + hasThirdPartyId );
            addOneColTable(titleP, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            sbl, 
                            false, // boolean header, 
                            font, //Font font2Use, 
                            null , 
                            0, 
                            0 );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addHRAInfoSection()" );

            throw e;
        }        
        
    }

    
    public void addNoMatchesSection() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();

            pars.add( bmsg("b.NoMatchesP1") );
            pars.add( bmsg("b.NoMatchesP2") );
            // pars.add( "\n\n\n\n\n\n" + bmsg( "b.SummaryNotesp4") );
            
            Paragraph p = new Paragraph();
            
            p.add( new Phrase( bmsg( "b.NoMatchesT" ), fontXLargeBold ) );
            
            addOneColTable(p, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );
            
             // addText( "\n\n\n\n\n\n\n" + bmsg( "b.SummaryNotesp4" ), fontXLargeBold, true );   
             
            float imageX = (pageWidth - mosaic4.getScaledWidth())/2; 

            ITextUtils.addDirectImage( pdfWriter, mosaic4, imageX, currentYLevel - mosaic4.getScaledHeight() - 20, false );
            
            currentYLevel -= mosaic4.getScaledHeight() + 30;
            
             
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addNoMatchesSection()" );

            throw e;
        }                
    }
    
    
    public void addSummaryNotesSection() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();

            // pars.add( bmsg("b.SummaryNotesp1") );
            pars.add( bmsg("b.SummaryNotesp2") );
            pars.add( bmsg("b.SummaryNotesp3") );
            // pars.add( "\n\n\n\n\n\n" + bmsg( "b.SummaryNotesp4") );
            
            Paragraph pp = new Paragraph();
            
            pp.add( new Phrase(  bmsg("b.SummaryNotesp1"), fontLargeBold ) );
            
            addOneColTable(pp, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );

            
            pp = new Paragraph();            
            pp.add( new Phrase(  bmsg("b.DetailTitle"), fontLargeBold ) );

            pars = new ArrayList<>();
            pars.add( bmsg("b.DetailTitle.P1") );
            
            addOneColTable(pp, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );

            
             // addText( "\n\n\n\n\n\n\n" + bmsg( "b.SummaryNotesp4" ), fontXLargeBold, true );   
             
            //float imageX = (pageWidth - mosaic4.getScaledWidth())/2; 

            //ITextUtils.addDirectImage( pdfWriter, mosaic4, imageX, currentYLevel - mosaic4.getScaledHeight() - 20, false );
            
            //currentYLevel -= mosaic4.getScaledHeight() + 30;
            
             
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addSummaryNotesSection()" );

            throw e;
        }        
    }
    

    public void addPostDetailNotesSection() throws Exception
    {
        try
        {
            List<Object> pars = new ArrayList<>();

            pars.add( bmsg("b.HowWeCreatedP1") );
            pars.add( bmsg("b.HowWeCreatedP2") );
            
            Paragraph tt = new Paragraph();            
            tt.add(new Phrase( bmsg( "b.HowWeCreatedT" ), fontXLargeBold ));
            
            addOneColTable(tt, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );     
            
            tt = new Paragraph();
            
            tt.add( new Phrase( bmsg( "b.WhyNotestScoresT" ), fontXLargeBold ) );
            
            pars.clear();
            
            pars.add( bmsg("b.WhyNotestScoresP1") );
            
            addOneColTable(tt, //Paragraph tableTitle, 
                            null, // Paragraph tableSubtitle, 
                            pars, 
                            false, // boolean header, 
                            fontLarge, //Font font2Use, 
                            null , 
                            0, 
                            0 );   
            
            float imageX = (pageWidth - mosaic3.getScaledWidth())/2; 

            ITextUtils.addDirectImage( pdfWriter, mosaic3, imageX, currentYLevel - mosaic3.getScaledHeight() - 20, false );
            
            currentYLevel -= mosaic3.getScaledHeight() + 30;
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addPostDetailNotesSection()" );

            throw e;
        }        
    }
    
    
    
    public void addDetailJobInfoSection(List<Profile> plist, boolean tasks, boolean requirements, boolean context, boolean traits, boolean skills, boolean abilities, boolean combinedKsa, boolean related, boolean isOneLevelUp) throws Exception
    {
        try
        {
            if( plist==null || plist.isEmpty() )
                return;
            
            float y = 0;
            
            int counter = 0;
            
            Soc soc;
            
            StringBuilder sbx;
            Chunk chk;
            Paragraph par;
            PdfAction pdfa;
            
            for( Profile p : plist )
            {
                addNewPage();

                counter++;
                
                if( counter>10 )
                {
                    requirements=false; 
                    context=false; 
                    traits=false;  
                    skills=false; 
                    abilities=false; 
                }
                
                previousYLevel =  currentYLevel - 10;

                soc = p.getSoc();
                
                y = addTitle( previousYLevel, bmsg( isOneLevelUp ? "b.MatchXYOneJzUp" : "b.MatchXY", new String[] {Integer.toString(counter), this.otrans( soc.getTitle() )} ), null );
                y -= TPAD;

                if( isOneLevelUp )
                {
                    y = addTitle( y, otrans(soc.getTitle()), null);
                    y -= TPAD;
                }
                
                currentYLevel = y;

                List<Object> left = new ArrayList<>();
                
                List<Object> right = new ArrayList<>();
                
                left.add( bmsg("b.DescriptionC") );
                right.add( otrans( soc.getDescription() ) );
                
                if( soc.getAlternateTitlesList()!=null && !soc.getAlternateTitlesList().isEmpty() )
                {
                    left.add( bmsg("b.AltTitlesC") );
                    // left.add( bmsg("b.AltTitlesC") + "\n\n" + bmsg("b.AltTitles2") );
                    right.add( otrans( soc.getAlternateTitlesAsString( 40 ) ) );
                }
                
                left.add( bmsg("b.JobLevelC") );
                right.add( bmsg( soc.getJobZoneType().getShortDescKey() ) );
                
                if( soc.getBlsEmployment()>0 )
                {
                    left.add( bmsg("b.TotalJobsUSC") );
                    right.add( I18nUtils.getFormattedInteger( reportData.getLocale(), soc.getBlsEmployment() ) );
                }

                if( soc.getBlsAverageAnnualSalary()>0 )
                {
                    left.add( bmsg("b.AvgSalaryUSC") );
                    right.add( I18nUtils.getFormattedCurrency(Locale.US, soc.getBlsAverageAnnualSalary(),0 ) );
                }
                
                //left.add( bmsg("b.MoreInfo") + ":");
                left.add( onet);
                par = new Paragraph();
                chk = new Chunk( getOnetLink(p),fontSmallItalicBlue );
                pdfa = PdfAction.gotoRemotePage( getOnetLink(p) , lmsg("b.Click2Visit"), false, true );                                
                chk.setAction( pdfa );
                par.add( chk );                                
                right.add( par );

                // left.add( bmsg("b.SearchIndeed") + ":");
                left.add(indeed);
                par = new Paragraph( );
                chk = new Chunk( getIndeedLink(p),fontSmallItalicBlue );
                pdfa = PdfAction.gotoRemotePage( getIndeedLink(p) , lmsg("b.Click2Visit"), false, true );                                
                chk.setAction( pdfa );
                par.add( chk );                                
                right.add( par );

                // left.add( bmsg("b.SearchCareerbuilder") + ":");
                left.add( careerBuilder );
                par = new Paragraph();
                chk = new Chunk( getCareerBuilderLink(p),fontSmallItalicBlue );
                pdfa = PdfAction.gotoRemotePage( getCareerBuilderLink(p) , lmsg("b.Click2Visit"), false, true );                                
                chk.setAction( pdfa );
                par.add( chk );                                
                right.add( par );
                
                addTwoColTable(null, null, left, right , false, 2 );                

                Paragraph taskTitle = null;

                if( tasks )
                {
                    left.clear();
                    right.clear();

                    left.add( bmsg( "b.TasksH") + " - " + bmsg( "b.InOrderOfImportance" ) );
                    right.add( bmsg( "b.TasksH2" ) );

                    for( OnetElement t : soc.getTasks() )
                    {
                        left.add( otrans( t.getName() ) );

                        if( t.getDetWorkActivities() != null && !t.getDetWorkActivities().isEmpty() )
                        {
                            sbx = new StringBuilder();

                            for( String[] dwa : t.getDetWorkActivities() )
                            {
                                sbx.append( " - " + otrans( dwa[1] ) + "\n" );
                            }

                            right.add( sbx.toString() );
                        }

                        else
                            right.add( "" );
                    }

                    if( soc.getTasks().size()>0 )
                    {
                        taskTitle = new Paragraph();
                        taskTitle.add( new Phrase(bmsg( "b.Tasks1", new String[] { otrans( soc.getTitle() ) } ), fontLargeBold));                
                        addOneColTable(taskTitle, null, left , true, null, null, lightBoxBorderWidth, 1 );
                        // addTwoColTable( bmsg( "b.Tasks1"), null, left, right , true );                
                    }
                }
                
                if( context )
                {
                    // Work Context
                    left.clear();
                    right.clear();

                    left.add( bmsg( "b.WorkContextH") );
                    right.add( bmsg( "b.WorkContextH2" ) );

                    for( OnetElement t : soc.getWorkContexts() )
                    {
                        left.add( this.otrans( t.getName() ) );

                        if( t.getContextCategory() != null && !t.getContextCategory().isEmpty() )
                        {

                            right.add( otrans( t.getContextCategory() ) );
                        }

                        else
                            right.add( "" );
                    }
                
                    taskTitle = new Paragraph();
                    taskTitle.add( new Phrase(bmsg( "b.WorkContextT", new String[] { otrans( soc.getTitle()) }), fontLargeBold));                
                    // addOneColTable( taskTitle, null, left , true, null, null, lightBoxBorderWidth );
                    addTwoColTable(taskTitle, null, left, right , true, 1 );                
                }

                
                if( requirements )
                {
                    // Work Styles
                    left.clear();
                    right.clear();

                    left.add( bmsg( "b.WorkStylesH") );
                    right.add( bmsg( "b.WorkStylesH2" ) );

                    for( OnetElement t : soc.getWorkStyles())
                    {
                        left.add( otrans( t.getName()) );

                        right.add( otrans(t.getDescription()) );
                    }

                    if( !soc.getWorkStyles().isEmpty() )
                    {
                        taskTitle = new Paragraph();
                        taskTitle.add( new Phrase(bmsg( "b.WorkStylesT", new String[] { otrans(soc.getTitleSingular())} ), fontLargeBold));                
                        // addOneColTable( taskTitle, null, left , true, null, null, lightBoxBorderWidth );
                        addTwoColTable(taskTitle, null, left, right , true, 1 );                
                    }
                }
                
                boolean vowelStart = false;
                String ts = soc.getTitleSingular().toLowerCase();
                if( ts.startsWith("a") || ts.startsWith("e")||ts.startsWith("i")||ts.startsWith("o")||ts.startsWith("u"))
                    vowelStart=true;
                
                par = new Paragraph();
                par.add( new Phrase( bmsg( vowelStart ? "b.TraitsHv" : "b.TraitsH", new String[] {otrans( soc.getTitleSingular() )} ), fontLargeBold ) );                
                addParagraphViaTable( par, 4, 0 );
                        
                if( skills || abilities )
                    addKSATable(soc, skills, abilities, combinedKsa );
                                
                if( related )
                {
                    if( soc.getRelatedJobs()!=null && !soc.getRelatedJobs().isEmpty() )
                    {
                        par = new Paragraph();
                        par.add( new Phrase( bmsg( "b.RelatedJobsH", new String[] {soc.getTitle()} ), fontLargeBold ) );                
                        addParagraphViaTable( par, 8, 7);

                        String relatedJobs = otrans(soc.getRelatedJobsAsString( 40 ));

                        this.addText(relatedJobs, font, false );                    
                    }
                }
                
                if( !this.careerScoutV2 )
                {
                    String detailHRAUseInfo = getDetailHraInfo( p );

                    addText( detailHRAUseInfo, font, false );                    
                }
                
                // this.addNewPage();
                // Font fnt = getFontXLarge();

            }  // Next Profile
            
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addDetailJobInfoSection()" );

            throw e;
        }     
    }
    
    private String getOnetLink( Profile p)
    {
        return "https://www.onetonline.org/link/summary/" + StringUtils.getUrlEncodedValue(p.getSoc().getSocCode());
    }
    
    private String getIndeedLink( Profile p )
    {
        return "https://www.indeed.com/jobs?q=" + StringUtils.getUrlEncodedValue(p.getSoc().getTitle());
    }
    
    private String getCareerBuilderLink( Profile p )
    {
        return "https://www.careerbuilder.com/jobs?keywords=" + StringUtils.getUrlEncodedValue(p.getSoc().getTitle());
    }
    
    private String getDetailHraInfo( Profile p )
    {
        return "\nHR Avatar Use Only: pid:" + p.getProfileId() + " js:" + p.getFloatParam4() + " rs:" + p.getFloatParam2() + " cs:" + p.getFloatParam1() + " ee:" + p.getFloatParam3();
    }

    /*
    private String getDetailHraInfoCSV( int rank, Profile p )
    {
        return rank + "," + p.getProfileId() + "," + StringUtils.getUrlEncodedValue(p.getStrParam1()) + "," + p.getFloatParam1() + "," + p.getFloatParam2() + "," + p.getFloatParam3() + "," + p.getFloatParam4() + "," + p.getStrParam4();
        // return reportData.getTestEvent().getTestEventId() + "," + rank + "," + p.getProfileId() + "," + Csv.escape(p.getStrParam1()) + "," + p.getFloatParam4() + "," + p.getFloatParam2() + "," + p.getFloatParam1() + "," + p.getFloatParam3();
    }

    private String getDetailHraInfoHeadersCSV()
    {
        return "rank,pid,pname,jobspec,riasec,combined,educexp,soccode";
        // return "teid,rank,pid,pname,jobspec,riasec,combined,educexp";
    }
    
    private String getDetailHraInfoHeadersEeoCatCSV()
    {
        return "rank,title,eeocattypeid,percentmatch";        
    }
    
    private String getDetailHraInfoEeoCatCSV( int rank, EeoMatch m )
    {
        return rank + "," + StringUtils.getUrlEncodedValue(m.getEeoTitle()) + "," + m.getEeoJobCategoryId()+ "," + m.getAveragePercentMatch();
    }
    */

    
    @Override
    public long addReportToOtherTestEventId() throws Exception
    {
        if( this.jobSpecificTestEvent==null || jobSpecificTestEvent.getTestEventId()==reportData.getTestEvent().getTestEventId() )
            return 0;
        
        return jobSpecificTestEvent.getTestEventId();
    }

    
    
    
    protected void addMatchInfoToJobSpecReport() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseBestJobsReportTemplate.addMatchInfoToJobSpecReport() AAA " );
            if( jobSpecificTestEvent==null || ((this.bestProfilesList==null || this.bestProfilesList.isEmpty()) && (this.eeoMatchList==null || this.eeoMatchList.isEmpty())) )
                return;
            
            // qualified Report
            TestEventScore jsTesReport = null;
            
            if( jobSpecificTestEvent.getTestEventScoreList()==null )
            {
                LogService.logIt( "BaseBestJobsReportTemplate.addMatchInfoToJobSpecReport() No testEventScores in jobSpecificTestEvent. " + reportData.toString() );
                return;
            }
            
            for( TestEventScore tes : jobSpecificTestEvent.getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() ) )
            {
                if( tes.getHasReport() && BestJobsReportUtils.isValidCT2ReportId( tes.getReportId() ) )
                {
                    jsTesReport = tes;
                    break;
                }
            }
            
            if( jsTesReport==null )
            {
                // LogService.logIt( "BaseBestJobsReportTemplate.addMatchInfoToJobSpecReport() No qualifying CT2 Report testEventScore found in jobSpecificTestEvent. " + reportData.toString() );
                return;
            }
            
            String sb = BestJobsReportUtils.createCompactInfoString(bestProfilesList, eeoMatchList );
            
            jsTesReport.setTextParam1( sb );
            
            EventFacade eventFacade = EventFacade.getInstance();
            
            eventFacade.saveTestEventScore(jsTesReport);   
            
            ReportRegenerationThread rrg = new ReportRegenerationThread( jobSpecificTestEvent.getTestEventId(), jsTesReport.getReportId(), false );
            
            Thread t = new Thread( rrg );
            t.start();
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addMatchInfoToJobSpecReport() " + reportData.toString()  );
        }
    }
    
    
    
    /*
    private boolean isValidCT2ReportForRegen( long reportId )
    {
        for( Long rid : ct2ReportIds )
        {
            if( rid.longValue()==reportId )
                return true;
        }
        
        return false;
    }
    */
    
    private void addKSATable( Soc soc, boolean skills, boolean abilities, boolean combineAll) throws Exception
    {
        int cols = 2;
        float[] colRelWids ;

        // Two column table
        PdfPCell c;
                
        previousYLevel =  currentYLevel;

        if( previousYLevel <= footerHgt )
        {
            document.newPage();

            currentYLevel = 0;
            previousYLevel = 0;
        }        
        
        colRelWids = reportData.getIsLTR() ? new float[] { 0.3f, .6f } : new float[] { .6f, .3f };                

        PdfPTable t2;
        List<OnetElement> el;        
        
        if( combineAll )
        {
            List<OnetElement> ell = new ArrayList<>();
            el = soc.getKnowledge();
            if( el!=null )
                ell.addAll(el);
            if( skills )
            {
                el = soc.getSkills();
                if( el!=null )
                    ell.addAll(el);
            }
            if( abilities )
            {
                el = soc.getAbilities();
                if( el!=null )
                    ell.addAll(el);
                
            }
            
            Collections.sort( ell, new OnetImportanceComparator() );
            Collections.reverse(ell);
            
            if( ell.size()>10 )
                ell = ell.subList(0, 10);
            
            t2 = new PdfPTable( cols );
            setRunDirection( t2 );
            t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t2.setWidths( colRelWids );
            t2.setLockedWidth( true );
            addTraitsToTable( t2, el, bmsg ("b.TraitsKnowledgeSkillsAbilities") + " - " + bmsg( "b.InOrderOfImportance" ), true );
            currentYLevel = addTableToDocument( currentYLevel, t2 ); 
            
            return;
        }
            
        
        el = soc.getKnowledge();        
        if( el!=null && !el.isEmpty() )
        {
            t2 = new PdfPTable( cols );
            setRunDirection( t2 );
            t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t2.setWidths( colRelWids );
            t2.setLockedWidth( true );

            addTraitsToTable( t2, el, bmsg( "b.TraitsKnowledge" ) + " - " + bmsg( "b.InOrderOfImportance" ), true );            
            currentYLevel = addTableToDocument( previousYLevel, t2 ); 
        }
        // t.setHeaderRows( 1 );                


        el = soc.getSkills();        
        if( skills && el!=null && !el.isEmpty() )
        {
            t2 = new PdfPTable( cols );
            setRunDirection( t2 );
            t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t2.setWidths( colRelWids );
            t2.setLockedWidth( true );
            addTraitsToTable( t2, el, bmsg ("b.TraitsSkills") + " - " + bmsg( "b.InOrderOfImportance" ), true );
            currentYLevel = addTableToDocument( currentYLevel, t2 ); 
        }

        el = soc.getAbilities();        
        if( abilities && el!=null && !el.isEmpty() )
        {
            t2 = new PdfPTable( cols );
            setRunDirection( t2 );
            t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t2.setWidths( colRelWids );
            t2.setLockedWidth( true );
            addTraitsToTable( t2, el, bmsg("b.TraitsAbilities") + " - " + bmsg( "b.InOrderOfImportance" ), true );
            currentYLevel = addTableToDocument( currentYLevel, t2 ); 
        }
   }
    
    
    
    private void addTraitsToTable( PdfPTable t2, List<OnetElement> el, String subtitle, boolean isLast )
    {
        if( el.isEmpty() )
            return;
        
        t2.setHeaderRows(1);
        
        // First, header
        PdfPCell c = new PdfPCell( new Phrase( subtitle , this.fontWhite )  );
        c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM );
        c.setBorderColor( ct2Colors.lighterBoxBorderColor );
        c.setBorderWidth( lightBoxBorderWidth );
        c.setColspan(2);
        c.setPadding( 4 );
        c.setPaddingBottom( 5 );
        c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
        c.setBackgroundColor( ct2Colors.hraBlue );
        setRunDirection( c );
        t2.addCell(c);

        // Next, each
        int count = 0;
        
        boolean shade = true;
        for( OnetElement oe : el )
        {            
            count++;
            
            c = new PdfPCell( new Phrase( otrans(oe.getName()) , font )  );
            c.setColspan(1);
            
            c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.LEFT : Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            if( shade )
                c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
            setRunDirection( c );
            t2.addCell( c );

            c = new PdfPCell( new Phrase( otrans(oe.getDescription()) , font  ) );
            c.setColspan(1);
            c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );            
            if( shade )
                c.setBackgroundColor(ct2Colors.lighterBoxBorderColor);
            setRunDirection( c );
            t2.addCell( c );   
            
            shade = !shade;
        }        
    }
    
    /**
     * rowShadeCode 0=none, 1=alternate, 2=solid
     * 
     * @param tableTitle
     * @param tableSubtitle
     * @param left
     * @param right
     * @param header
     * @param rowShadeCode
     * @throws Exception 
     */
    private void addTwoColTable( Paragraph tableTitle, Paragraph tableSubtitle, List<Object> left, List<Object> right, boolean header, int rowShadeCode) throws Exception
    {
        if( tableTitle != null  )
            addParagraphViaTable( tableTitle, 4, 0 );
        
        if( tableSubtitle != null )
            addParagraphViaTable( tableSubtitle, 1, 1 );
        
        int cols = 2;
        float[] colRelWids ;

        // Two column table
        PdfPCell c;
                
        previousYLevel =  currentYLevel;

        if( previousYLevel <= footerHgt )
        {
            document.newPage();

            currentYLevel = 0;
            previousYLevel = 0;
        }        
        
        colRelWids = reportData.getIsLTR() ? new float[] { 0.3f, .6f } : new float[] { .6f, .3f };                

        PdfPTable t2 = new PdfPTable( cols );
        setRunDirection( t2 );
        t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
        t2.setWidths( colRelWids );
        t2.setLockedWidth( true );
        // t.setHeaderRows( 1 );                

        int rowCount = 0;

        String leftStr, rightStr;
                
        if( header )
        {
            t2.setHeaderRows(1);
            
            leftStr = left.size()>0 ? (String)left.get(0) : "";            
            rightStr = right.size()>0 ? (String)right.get(0) : "";
                    
            c = new PdfPCell( new Phrase( leftStr , this.fontWhite )  );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.LEFT | Rectangle.BOTTOM );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            if( left.size()==1 )
                c.setBorderWidthBottom(0);
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t2.addCell(c);
            
            c = new PdfPCell( new Phrase( rightStr, fontWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT : Rectangle.TOP | Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t2.addCell(c);
            
            rowCount++;
        }
                
        Object o1,o2;
        boolean valignMiddle = false;
        boolean shade=rowShadeCode>0 ? true : false;
        for( int i=rowCount;i<left.size(); i++ )
        {
            valignMiddle = false;
            o1=left.get(i);
            o2=right.size()>i ? right.get(i) : "";
            //leftStr = left.size()>i ? left.get(i) : "";            
            //rightStr = right.size()>i ? right.get(i) : "";
                
            if( o1 instanceof String )
                c = new PdfPCell( new Phrase( (String)o1 , font )  );
            else
            {
                c = new PdfPCell( (Image)o1 );
                valignMiddle = true;
            }
            c.setColspan(1);
            if( i==0 )          
                c.setBorder( reportData.getIsLTR() ? Rectangle.TOP |  Rectangle.BOTTOM | Rectangle.LEFT : Rectangle.TOP |  Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT );
            else
                c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.LEFT : Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT );
                
            if( valignMiddle )
            {
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c.setPadding(3);
            }
            
            else
                c.setPadding(2);
            
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            if( shade )
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
            setRunDirection( c );
            t2.addCell( c );

            if( o2 instanceof String )
                c = new PdfPCell( new Phrase( (String)o2 , font  ) );
            else if( o2 instanceof Image )
                c = new PdfPCell( (Image)o2 );
            else if( o2 instanceof Paragraph )
                c = new PdfPCell( (Paragraph)o2 );
            else
                c = new PdfPCell( new Phrase( "Unknown ContentType " + o2.toString() ) );
                            
            c.setColspan(1);
            if( i==0 )          
                c.setBorder( reportData.getIsLTR() ? Rectangle.TOP |  Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT : Rectangle.TOP |  Rectangle.BOTTOM | Rectangle.RIGHT );
            else
                c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.LEFT | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.RIGHT );
            
           
            if( valignMiddle )
            {
                c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            }
            else
                c.setPadding(2);
            
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );            
            if( shade )
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);
            setRunDirection( c );
            t2.addCell( c );
            
            if(rowShadeCode==1) 
                shade=!shade;
        }
           
        currentYLevel = addTableToDocument( previousYLevel, t2 );
        //t2.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, previousYLevel, pdfWriter.getDirectContent() );
        //currentYLevel = previousYLevel - t2.calculateHeights();
    }

    /**
     * rowShadeCode = 0=none
     *                1=alternate
     *                2=solid
     * 
     * @param tableTitle
     * @param tableSubtitle
     * @param left
     * @param header
     * @param font2Use
     * @param headerFont2Use
     * @param borderWidth
     * @param rowShadeCode
     * @throws Exception 
     */
    public void addOneColTable( Paragraph tableTitle, Paragraph tableSubtitle, List<Object> left, boolean header, Font font2Use, Font headerFont2Use, float borderWidth, int rowShadeCode ) throws Exception
    {
        if( tableTitle != null  )
            addParagraphViaTable( tableTitle, 4, 0 );
        
        if( tableSubtitle != null )
            addParagraphViaTable( tableSubtitle, 1, 1 );
        
        if( borderWidth<0 )
            borderWidth = lightBoxBorderWidth;
        
        // Two column table
        PdfPCell c;
                
        previousYLevel =  currentYLevel;

        if( previousYLevel <= footerHgt )
        {
            document.newPage();

            currentYLevel = 0;
            previousYLevel = 0;
        }        
        
        PdfPTable t2 = new PdfPTable( 1 );
        setRunDirection( t2 );
        t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
        t2.setLockedWidth( true );
        // t.setHeaderRows( 1 );                

        int rowCount = 0;

        String leftStr;
        
        if( header )
        {
            t2.setHeaderRows(1);
            leftStr = left.size()>0 ? (String) left.get(0) : "";       
                    
            c = new PdfPCell( new Phrase( leftStr , headerFont2Use==null ? fontWhite : headerFont2Use )  );
            c.setBorder(borderWidth<=0 ? Rectangle.NO_BORDER : (left.size()>1 ? Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT : Rectangle.BOX ) );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            
            if( borderWidth > 0 )
                c.setBorderWidth( borderWidth );
            // c.setBorderWidthBottom(0);
            c.setPaddingBottom( 3 );
            c.setPaddingTop( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t2.addCell(c);
                        
            rowCount++;
        }
              
        Object o;
        
        boolean shade=rowShadeCode>0 ? true : false;
        for( int i=rowCount;i<left.size(); i++ )
        {
            o = left.size()>i ? left.get(i) : "";  
            
            if( o==null )
            {
                LogService.logIt( "BaseBestJobsReportTemplate.addOneColTable() o is null. Skipping this row. " );
                continue;        
            }
            else if( o instanceof String )
                c = new PdfPCell( new Phrase( (String)o ,font2Use==null ? font : font2Use )  );
            else if( o instanceof Image )
                c = new PdfPCell( (Image)o );
            else if( o instanceof Paragraph )
                c = new PdfPCell( (Paragraph)o );
            else
                c = new PdfPCell( new Phrase( "Unknown ContentType " + o.toString() ) );
            
            c.setColspan(1);
            c.setBorder(borderWidth<=0 ? Rectangle.NO_BORDER : (i<left.size()-1 ? Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT : Rectangle.BOX ) );
            c.setBorderColor( ct2Colors.lighterBoxBorderColor );
            
            c.setPaddingTop( 3 );
            c.setPaddingBottom( 3 );
            
            if( borderWidth > 0 )
                c.setBorderWidth(borderWidth);
            if( shade )
                c.setBackgroundColor( ct2Colors.lighterBoxBorderColor);            
            // c.setBorderWidthBottom(0);
            setRunDirection( c );
            t2.addCell( c );
            
            if( rowShadeCode==1 )
                shade = !shade;
        }
                
        currentYLevel = addTableToDocument( previousYLevel, t2 );

        //t2.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, previousYLevel, pdfWriter.getDirectContent() );
        //currentYLevel = previousYLevel - t2.calculateHeights();
    }

    
    private void addParagraphViaTable( Paragraph p, int extraPadAbove, int extraPadBelow ) throws Exception
    {
        PdfPCell c = new PdfPCell( p );
        c.setPadding( 1 );
        c.setBorder( Rectangle.NO_BORDER );
        if( extraPadAbove> 0 )
            c.setPaddingTop( extraPadAbove + 1 );

        if( extraPadBelow> 0 )
            c.setPaddingBottom( extraPadBelow + 1 );

        setRunDirection( c );        
        addCellViaTable( c );
    }
    
    /*
    private void addPhraseViaTable( Phrase p, int extraPadAbove, int extraPadBelow ) throws Exception
    {
        PdfPCell c = new PdfPCell( p );
        c.setPadding( 1 );
        c.setBorder( Rectangle.NO_BORDER );
        if( extraPadAbove> 0 )
            c.setPaddingTop( extraPadAbove + 1 );

        if( extraPadBelow> 0 )
            c.setPaddingBottom( extraPadBelow + 1 );

        setRunDirection( c );
        addCellViaTable( c );
    }
    */
    
    
    private void addCellViaTable( PdfPCell c ) throws Exception
    {
        previousYLevel =  currentYLevel;

        if( previousYLevel <= footerHgt )
        {
            document.newPage();

            currentYLevel = 0;
            previousYLevel = 0;
        }        
                
        PdfPTable t2 = new PdfPTable(1);
        
        setRunDirection( t2 );
        t2.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
        t2.setLockedWidth( true );
        // t2.setLockedWidth( true );
        // t.setHeaderRows( 1 );                

        t2.addCell(c);
            
        currentYLevel = addTableToDocument( previousYLevel, t2 );

        //t2.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, previousYLevel, pdfWriter.getDirectContent() );
        //currentYLevel = previousYLevel - t2.calculateHeights();
    }

    
    

    public void addCoverPage(boolean includeDescriptiveText) throws Exception
    {
        try
        {
            PdfPCell c;

            PdfPTable t;            
            
            getHraLogoBlackText().scalePercent( 100 * 72 / 400 );
            
            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 25;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

            ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), CT2_MARGIN, y, false );


            java.util.List<Chunk> cl = new ArrayList<>();

            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";
           
            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            
            if( !includeCompanyInfo )
            {
                reportCompanyName = "";
                custLogo = null;
            }
            
            
            cl.add( new Chunk( lmsg( "g.PreparedForC" ), getFontXLarge() ) );
            // cl.add( new Chunk( lmsg( "g.AssessmentC" ), getFontXLarge() ) );
            cl.add( new Chunk( bmsg( "b.PreparedDateC" ), getFontXLarge() ) );
            
            if( includeCompanyInfo && getHasSponsor() )
                cl.add( new Chunk( bmsg( "b.SponsoringEmployerC" ), getFontXLarge() ) );

            float titleWid = ITextUtils.getMaxChunkWidth( cl ) + 20;

            cl.clear();

            cl.add( new Chunk( reportData.getUserName(), getFontXLargeBold() ) );
            // cl.add( new Chunk( reportData.getSimName(), getFontXLarge() ) );
            cl.add( new Chunk( getCurrentDateFormatted(), getFontXLarge() ) );

            float infoWid = ITextUtils.getMaxChunkWidth( cl ) + 10;

            if( includeCompanyInfo && getHasSponsor() )
            {
                // cl.add( new Chunk( reportCompanyAdminName, getFontXLarge() ) );

                if( (!reportData.hasCustLogo() || custLogo==null) && reportCompanyName != null && !reportCompanyName.isEmpty() )
                    cl.add( new Chunk( reportCompanyName, getFontXLarge() ) );

                if( custLogo!=null && custLogo.getScaledWidth()>infoWid )
                    infoWid = custLogo.getScaledWidth();            
            }

            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth*0.74f - 2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            BaseFont baseTitleFont = baseFontCalibriBold;

            int titleFontHeight = 44;

            float tableY = y - 80; //  tableH; // + 10 - (y - pageHeight/2 - tableH)/2;
            
            if( includeCompanyInfo && getHasSponsor() )
            {
                tableY -= 15;
                
                if( custLogo!=null )
                    tableY -= (custLogo.getScaledHeight() + 5);
            }

            Font titleFont = new Font(baseTitleFont, titleFontHeight );
            titleFont.setColor(ct2Colors.hraBlue);
            
            c = new PdfPCell( new Phrase( reportData.getReportName() , titleFont ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            float tableH = t.calculateHeights(); //  + 500;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );            
            
            y = tableY - tableH - 10;

            
            // First create the table
            
            // First, add a table
            t = new PdfPTable( 2 );

            t.setTotalWidth( reportData.getIsLTR() ?  new float[] { titleWid+4, infoWid+14 } : new float[] { infoWid+14,titleWid+4 } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            Font font = this.fontXLarge;

            t.addCell( new Phrase( lmsg( "g.PreparedForC" ) , font ) );
            t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

            //t.addCell( new Phrase( lmsg( "g.AssessmentC" ) , font ) );
            //t.addCell( new Phrase( reportData.getSimName(), font ) );

            t.addCell( new Phrase( bmsg( "b.PreparedDateC" ) , font ) );
            t.addCell( new Phrase( getCurrentDateFormatted(), font ) );


            if( includeCompanyInfo && getHasSponsor() )
            {
                // LogService.logIt( "BaseBestJobsReportTemplate.addCoverPage()"  );

                t.addCell( new Phrase( bmsg( "b.SponsoringEmployerC" ) + " " , font ) );

                // t.addCell( new Phrase( "" , font ) );

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

                else
                    t.addCell( new Phrase( reportCompanyName, fontXLarge ) );
            }

            tableH = t.calculateHeights(); //  + 500;

            tableY = y - 20; //   y + 10 - (y - pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, getHraOrangeColor(), false );
            // y = tableY - tableH - 20;
            
            float imageX = (pageWidth - mosaic.getScaledWidth())/2; 
            // float mosaicHeight = mosaic.getScaledHeight();
            float imageY = pageHeight/3;
                        
            ITextUtils.addDirectImage( pdfWriter, mosaic, imageX, imageY, false );
            // ITextUtils.addDirectImage( pdfWriter, mosaic, imageX, y - mosaic.getScaledHeight()-10, false );
            
            // y -= mosaic.getScaledHeight()+10;
            
            
            
            // Add the blue below
            ITextUtils.addDirectColorRect( pdfWriter, getHraBaseReportColor(), 0, 0, pageWidth, pageHeight/3, 0, 1, true );


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

            // t.addCell( "\n\n\n\n\n\n\n\n\n" );
            
            if( !includeDescriptiveText )
                t.addCell( "\n\n\n\n\n" );
            
            c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            //t.addCell( c );

            t.addCell( "\n\n\n\n\n\n\n\n\n" );

            //String coverDescrip=null; 

            if( includeDescriptiveText )
            {
                if( coverDescrip != null &&  !coverDescrip.isEmpty() )
                {}

                else if( reportData.getReport() != null && reportData.getReport().getTextParam1()!=null && !reportData.getReport().getTextParam1().isEmpty() )
                {
                    coverDescrip = reportData.getReport().getTextParam1();                    
                    
                    if( !reportData.getLocale().getLanguage().equalsIgnoreCase( reportData.r2Use.getLocaleStr()==null || reportData.r2Use.getLocaleStr().isEmpty() ? "en" : I18nUtils.getLocaleFromCompositeStr( reportData.r2Use.getLocaleStr()).getLanguage() ) )
                    {
                        Locale srcLoc = reportData.r2Use.getLocaleStr()==null || reportData.r2Use.getLocaleStr().isEmpty() ? Locale.US : I18nUtils.getLocaleFromCompositeStr( reportData.r2Use.getLocaleStr());
                        coverDescrip = tctrans( coverDescrip, srcLoc, false );
                    }
                }

                else 
                {
                    String coverDetailKey = reportData.getReport()!=null && reportData.getReport().getStrParam1()!=null && !reportData.getReport().getStrParam1().isEmpty() ? reportData.getReport().getStrParam1() : "b.CoverBlurb";            

                    coverDescrip = bmsg( coverDetailKey, new String[] {reportData.getSimName()} );                
                }  
                                
                Paragraph pr = new Paragraph(); 
                
                pr.add( new Phrase( coverDescrip + "\n\n" , fontLLWhite ) );

                
                pr.add( new Chunk( bmsg( "b.NoScoreCaveat1" ) + " " , fontXLargeBoldWhite ) );
                pr.add( new Chunk( bmsg( "b.NoScoreCaveat2" ) + " " , fontLLWhite ) );
                
            
                c = new PdfPCell( pr );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection(c);

                t.addCell( c );
            }
            //t.addCell( "\n\n\n" );
            //c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFontWhite() ) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( Element.ALIGN_CENTER );
            //setRunDirection(c);
            //t.addCell( c );

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
            LogService.logIt( e, "BaseBestJobsReportTemplate.addCoverPage()" );
        }
    }

    
    public boolean setBoldMatch( float matchPercent )
    {
        return matchPercent>=85f;
    }

    public String getDegreeOfMatchString( float matchPercent, boolean includeNumeric)
    {
        String pct = "";
        
        if( includeNumeric )
        {
            int i = Math.round(matchPercent);
            if( i<1 )
                i=1;
            if( i>100 )
                i=100;
            pct += " (" + i + "%)";
        }
        
        if( matchPercent<=50 )
            return bmsg( "b.DegreeSome" ) + pct;
        if( matchPercent<=75 )
            return bmsg( "b.DegreeMedium" ) + pct;
        if( matchPercent<=90 )
            return bmsg( "b.DegreeStrong" ) + pct;
            
        return bmsg( "b.DegreeVeryStrong" ) + pct;
    }
    
  
    public boolean getHasSponsor()
    {
        if( jobSpecificTestEvent==null )
            return false;
        
        if( reportData.getOrg()==null  )
            return false;
        
        return reportData.getOrg().getOrgId() != RuntimeConstants.getIntValue( "defaultMarketingAccountOrgId" );
    }


    public float addTitle( float startY, String title, String subtitle ) throws Exception
    {
        try
        {
            if( !reportData.getIsLTR() )
                return addTitleRTL( startY,  title,  subtitle );

            if( startY > 0 )
            {
                float ulY = startY - 16* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt =   getHeaderFontXLarge();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            // No subtitle
            if( subtitle==null || subtitle.isEmpty() )
                return y;

            // Change getFont()
            fnt =  getFont();

            float leading = fnt.getSize();

            float spaceLeft = y - PAD - footerHgt;

            // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

             y -=  PAD;//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();
            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText. If RTL need to use Column Text anyway.
            if( reportData.getIsLTR() && txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, subtitle, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt, false );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() RTL or overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                setRunDirection( ct );

                Phrase p = new Phrase( subtitle, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() adding second column "  );

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
            LogService.logIt( e, "BaseBestJobsReportTemplate.addTitleAndSubtitle()" );

            throw new STException( e );
        }
    }


    public float addTitleRTL( float startY, String title, String subtitle ) throws Exception
    {
        try
        {
            if( startY > 0 )
            {
                float ulY = startY - 16* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt =   getHeaderFontXLarge();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            t.addCell( new Phrase( title , fnt ) );
            
            if( subtitle != null && !subtitle.isEmpty() )
            {
                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                setRunDirection(c);

                t.addCell( new Phrase( subtitle ,  getFont() ) );                
            }

            float ht = t.calculateHeights(); //  + 500;

            // float yy = pageHeight/2 - (pageHeight/2 - ht)/2;

            float tw = t.getTotalWidth();

            float tableX = (pageWidth - tw )/2;

            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );


            currentYLevel = y - ht;

            return currentYLevel;

            // Add Title
            // ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            // return y;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addTitleRTL()" );

            throw new STException( e );
        }
    }




    public float addText( String text, Font fnt, boolean centered) throws Exception
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

            // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( reportData.getIsLTR() && txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, text, rect, centered ? Element.ALIGN_CENTER : Element.ALIGN_LEFT, leading, fnt, false );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );
                setRunDirection( ct );

                Phrase p = new Phrase( text, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                currentYLevel = ct.getYLine();

                while( ColumnText.hasMoreText(status) )
                {
                    // LogService.logIt( "BaseBestJobsReportTemplate.addText() adding second column "  );

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
            LogService.logIt( e, "BaseBestJobsReportTemplate.addTtext()" );

            throw new STException( e );
        }
    }




    public float addTableToDocument( float startY, PdfPTable t ) throws Exception
    {
            float ulY = startY - 2*PAD;  // 4* PAD;

            float tableHeight = t.calculateHeights(); //  + 500;
            float headerHeight = t.getHeaderHeight();

            int rowCount = t.getRows().size(); //  - t.getHeaderRows() - t.getFooterRows();

            float maxRowHeight=0;

            float[] rowHgts = new float[rowCount];

            for( int i=0; i<rowCount; i++ )
            {
                rowHgts[i]=t.getRowHeight(i);
                maxRowHeight = Math.max( maxRowHeight, rowHgts[i] );
                // LogService.logIt( "BaseBestJobsReportTemplate.addTableToDocument() row=" + i + ", rowHeight=" + rowHgts[i] );
            }

            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;

            float heightAvailNewPage = pageHeight - headerHgt - 3*PAD - footerHgt - 3*PAD - headerHeight;

            if( maxRowHeight >= heightAvailNewPage*0.5 )
                t.setSplitLate(false);

            // If first row doesn't fit on this page
            else if( firstRowHgt > ulY- footerHgt - 3*PAD - headerHeight ) // ulY < footerHgt + 8*PAD )
            {
                // LogService.logIt( "BaseBestJobsReportTemplate.addTableToDocument() adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
            }


            //if( maxRowHeight > usablePageHeight )
            //    t.setSplitLate(false);
            float tableXlft = CT2_MARGIN + CT2_BOX_EXTRAMARGIN;
            float tableXrgt = CT2_MARGIN + CT2_BOX_EXTRAMARGIN + t.getTotalWidth();

            Rectangle colDims = new Rectangle( tableXlft, footerHgt + 3*PAD, tableXrgt, ulY );
            // LogService.logIt( "BaseBestJobsReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

            float heightNoHeader = tableHeight - headerHeight;


            Object[] dta = calcTableHghtUsed( colDims.getTop() - colDims.getBottom() - headerHeight, 0, t.getHeaderRows(), rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
            int nextIndex = (Integer) dta[0];
            float heightUsedNoHeader = (Float) dta[1];
            float residual = (Float) dta[2];

            // LogService.logIt( "BaseBestJobsReportTemplate.addTableToDocument() tableHeight=" + t.calculateHeights() + ", headerHeight=" + headerHeight + ", maxRowHeight=" + maxRowHeight + ", heightAvailNewPage=" + heightAvailNewPage + ", initial heightUsedNoHeader=" + heightUsedNoHeader + ", residual=" + residual );


            ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );
            setRunDirection( ct );

            // NOTE - this forces Composite mode (using ColumnText.addElement)
            ct.addElement( t );

            ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );
            // ct.setSimpleColumn( colDims1 );


            int status = ct.go();

            // int linesWritten = ct.getLinesWritten();

            // LogService.logIt( "BaseBestJobsReportTemplate.addTableToDocument() initial lines written. NO_MORE_COLUMN=" + ColumnText.NO_MORE_COLUMN + ", NO_MORE_TEXT=" + ColumnText.NO_MORE_TEXT  );

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

                // LogService.logIt( "BaseBestJobsReportTemplate.addTableToDocument() AFTER adding next page. hgtUsedThisPage=" + hgtUsedThisPage +  ", Total HeightNeededNoHeader=" + heightNeededNoHeader + ", Total HeightUsedNoHeader=" + heightUsedNoHeader  );

                colDims = new Rectangle( tableXlft, ulY - heightAvailNewPage , tableXrgt, ulY );

                document.newPage();

                ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );

                ct.setYLine( colDims.getTop() );

                status = ct.go();

                // linesWritten += ct.getLinesWritten();

                //  LogService.logIt( "BaseBestJobsReportTemplate.addTableToDocument() status=" + status  );

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

        Object[] dta = new Object[] {startIndex , 0f, 0f };

        if( rowHgts.length<=startIndex )
            return dta;

        float hgt = 0;
        float resid = 0;

        if( prevResidual>0 )
        {
            // Bigger than max
            if( prevResidual>= maxRoom )
            {
                dta[1] = maxRoom ;
                dta[2] = prevResidual -  maxRoom ;

                if( prevResidual== maxRoom)
                {
                    dta[0]=startIndex+1;
                    dta[2] = 0f ;
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
                dta[0]=i+1;
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

                dta[0] = i;
                dta[1] = hgt;
                return dta;
            }

            hgt += rowHgts[i];
        }

        dta[0] = maxIndex+1;
        dta[1] = hgt;
        return dta;
    }


    public void addNewPage() throws Exception
    {
        document.newPage();
        this.currentYLevel = pageHeight - PAD -  headerHgt;
    }


    // test content trans
    public String tctrans( String srcTxt, boolean dynamic )
    {
        return tctrans( srcTxt, reportData.getTestContentLocale(), dynamic );
    }

    public String tctrans( String srcTxt, Locale srcLocale, boolean dynamic )
    {
        if( !reportData.getNeedsTestContentTrans() )
            return srcTxt;

        if( srcTxt == null )
            return "";
        
        if( srcTxt.trim().isEmpty() )
            return srcTxt;
        
        String s = languageUtils.getTextTranslation(srcTxt, srcLocale, reportData.getLocale(), dynamic );

        if( s!=null )
            return s;
        
        return srcTxt;
    }

    
    public String otrans( String srcTxt )
    {
        return otrans( srcTxt, false );
    }    
    
    // other trans (like ONET Trans
    public String otrans( String srcTxt, boolean dynamic )
    {
        if( !needsOnetTrans )
            return srcTxt;
        
        if( languageUtils==null )
            languageUtils = new LanguageUtils();

        String s = languageUtils.getTextTranslation(srcTxt, Locale.US, reportData.getLocale(), dynamic );

        if( s!=null )
            return s;
        
        return srcTxt;
    }
    

    // Standard Locale key
    public String lmsg( String key )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict( reportData.getTestContentLocale(), reportData.getLocale(), key, null ); 
            
            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage( reportData.getLocale() , key, null );
    }

    
    // Standard Locale key
    public String lmsg( String key, String[] prms )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict( reportData.getTestContentLocale(), reportData.getLocale(), key, prms );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage( reportData.getLocale() , key, prms );
    }

    // BestJobs Locale key
    public String bmsg( String key )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict(BEST_JOBS_BUNDLE, null, reportData.getLocale(), key, null );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage( BEST_JOBS_BUNDLE, reportData.getLocale() , key, null );
    }

    // BestJobs Locale key
    public String bmsg( String key, String[] prms )
    {
        if( reportData.getNeedsKeyCheck() )
        {
            if( languageUtils==null )
                languageUtils = new LanguageUtils();

            String s = languageUtils.getKeyValueStrict(BEST_JOBS_BUNDLE, null, reportData.getLocale(), key, prms );

            if( s!=null )
                return s;
        }
        
        return MessageFactory.getStringMessage( BEST_JOBS_BUNDLE, reportData.getLocale() , key, prms );
    }

    /**
     * A test is eligible if its associated product is a  job-specific test. that's all it takes.  
     * @return 
     *
    @Override
    public boolean isValidForTestEvent()
    {
        try
        {
            loadBestProfilesAndEeoCategoryScores();

            if( bestProfilesList == null ) // || bestProfilesList.size() < MIN_MATCHING_PROFILES_TO_CREATE_REPORT )
            {
                LogService.logIt( "BaseBestJobsReportTemplate.isValidForTestEvent() Denying report generation because not enough profiles match. ProfileCount=" + (bestProfilesList == null ? "0" : bestProfilesList.size() ) );
                return false;
            }

            if( reportData.getTestEvent()==null )
            {
                LogService.logIt( "BaseBestJobsReportTemplate.isValidForTestEvent() Denying report generation because TestEvent is null" );
                LogService.logIt( "BaseBestJobsReportTemplate.isValidForTestEvent() Denying report generation because not enough profiles match. ProfileCount=" + (bestProfilesList == null ? "0" : bestProfilesList.size() ) );
                return false;
            }

            if( reportData.getTestEvent().getProduct() == null )
            {
                LogService.logIt( "BaseBestJobsReportTemplate.isValidForTestEvent() Denying report generation because product is null" );
                return false;
            }

            // LogService.logIt( "BaseBestJobsReportTemplate.isValidForTestEvent() reportData.getTestEvent().getProduct().getConsumerProductTypeId()=" + reportData.getTestEvent().getProduct().getConsumerProductTypeId() + ", isInventory=" + isHraInterestInventory( reportData.getTestEvent().getProductId(), I18nUtils.getLocaleFromCompositeStr( reportData.getTestEvent().getLocaleStr() ) ) );
            
            if( reportData.getTestEvent().getProduct().getConsumerProductTypeId() == ConsumerProductType.ASSESSMENT_OTHER.getConsumerProductTypeId() || 
                isHraInterestInventory( reportData.getTestEvent().getProductId(), I18nUtils.getLocaleFromCompositeStr( reportData.getTestEvent().getLocaleStr() ) ) )
                return true;
            
            if( BestJobsReportUtils.hasRiasecCompetencies( reportData.getTestEvent() ) )
                return true;
            
            
            return false;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.isValidForTestEvent() " + ( reportData.getTestEvent()!=null ? reportData.getTestEvent().toString() : "Report data testEvent is null") );
            
            return false;
        }
    }
    */
    
    
    
    public static boolean isHraInterestInventory( int productId, Locale locale )
    {
        String ids = RuntimeConstants.getStringValue("Hra_Interest_Inventory_ProductIds_ALL" );
        
        String[] idz = ids.split(",");
        
        for( String s : idz )
        {
            try
            {
              if( productId==Integer.valueOf(s) )
                  return true;
            }
            catch( NumberFormatException e )
            {}
        }
        
        return false;        
        //return productId == getOptionalAutoTestProductId( locale );
    }
    
    /*
    public static int getOptionalAutoTestProductId( Locale locale )
    {
        // get the survey productId
        Integer surveyProductId = RuntimeConstants.getIntValue( "Hra_Interest_Inventory_ProductId_" + locale.toString() );

        if( surveyProductId==null )
            surveyProductId = RuntimeConstants.getIntValue( "Hra_Interest_Inventory_ProductId_" + locale.getLanguage() );

        if( surveyProductId==null )
            surveyProductId = RuntimeConstants.getIntValue( "Hra_Interest_Inventory_ProductId_" + Locale.US.toString() );

        return surveyProductId;
    }
    */
    
    
    
    public void setRunDirection( PdfPCell c )
    {
        if( c == null || reportData == null || reportData.getLocale() == null )
            return;

        // if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        c.setRunDirection( reportData.getTextRunDirection() );
    }

    public void setRunDirection( PdfPTable t )
    {
        if( t == null || reportData == null || reportData.getLocale() == null )
            return;

        t.setRunDirection( reportData.getTextRunDirection() );

        //if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        //    t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }

    public void setRunDirection( ColumnText ct )
    {
        if( ct == null || reportData == null || reportData.getLocale() == null )
            return;

        ct.setRunDirection( reportData.getTextRunDirection() );

        //if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        //    t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }

    private URL getBestJobsImageUrl( String fileName )
    {
       return com.tm2score.util.HttpUtils.getURLFromString( getBestJobsBaseImageUrl() + "/" + fileName );
    }
    
    private String getBestJobsBaseImageUrl()
    {
        return RuntimeConstants.getStringValue( "baseurl" ) + "/resources/images/bestjobs";
    }
    
    
    
}
