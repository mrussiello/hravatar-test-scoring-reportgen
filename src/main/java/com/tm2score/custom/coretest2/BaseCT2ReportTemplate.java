/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.itextpdf.text.Annotation;
import com.tm2score.custom.coretest.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAction;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
// import com.itseasy.rtf.text.Border;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.ai.MetaScoreType;
import com.tm2score.battery.BatteryScoringUtils;
import com.tm2score.bot.ChatMessageType;
import com.tm2score.custom.bestjobs.BaseBestJobsReportTemplate;
import static com.tm2score.custom.bestjobs.BaseBestJobsReportTemplate.BEST_JOBS_BUNDLE;
import com.tm2score.custom.bestjobs.BestJobsReportUtils;
import com.tm2score.custom.bestjobs.EeoMatch;
import com.tm2score.custom.coretest2.cefr.CefrScoreType;
import com.tm2score.custom.coretest2.cefr.CefrUtils;
import com.tm2score.entity.ai.MetaScore;
import com.tm2score.entity.proctor.ProctorEntry;
import com.tm2score.entity.event.TestEvent;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.global.Constants;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.proctor.ProctorSuspension;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.proctor.SuspiciousActivity;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Resume;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventResponseRatingUtils;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.file.FileContentType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.format.TableBackground;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.ibmcloud.HraTrait;
import com.tm2score.ibmcloud.SentinoUtils;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.proctor.ProctorHelpUtils;
import com.tm2score.proctor.ProctorUtils;
import com.tm2score.proctor.SuspiciousActivityThresholdType;
import com.tm2score.proctor.SuspiciousActivityType;
import com.tm2score.proctor.SuspiciousKeyCodeType;
import com.tm2score.profile.ProfileFacade;
import com.tm2score.profile.ProfileStrParam1Comparator;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.profile.alt.AltScoreCalculatorFactory;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportManager;
import com.tm2score.report.ReportTemplate;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.CaveatScoreType;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.scorer.BaseTestEventScorer;
import com.tm2score.service.LogService;
import com.tm2score.sim.CategoryDistType;
import com.tm2score.sim.EducType;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.OverallRawScoreCalcType;
import com.tm2score.sim.OverallScaledScoreCalcType;
import com.tm2score.sim.RelatedExperType;
import com.tm2score.sim.ScorePresentationType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.sim.SimCompetencyRawScoreCalcType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.sim.TrainingType;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.user.AssistiveTechnologyType;
import com.tm2score.user.ResumeEducation;
import com.tm2score.user.ResumeExperience;
import com.tm2score.user.UserFacade;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


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
public abstract class BaseCT2ReportTemplate extends CT2ReportSettings implements ReportTemplate
{
    public Image custLogo = null;
    // public Image custLogoHdr = null;

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

    public FileUploadFacade fileUploadFacade;

    ProctorUtils proctorUtils;


    @Override
    public abstract byte[] generateReport() throws Exception;


    public static String AUDIO_PLAYBACK_URL = null; // "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_3x1517685008793.png";
    public static String VIDEO_PLAYBACK_URL = null; // "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_2x1517685008793.png";
    public static String GENFILE_DOWNLOAD_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_4x1517691500437.png";
    public static String EXCEL_DOWNLOAD_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_5x1517691501144.png";
    public static String PPT_DOWNLOAD_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_7x1517691502319.png";
    public static String WORD_DOWNLOAD_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_8x1517691502892.png";
    public static String PDF_DOWNLOAD_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_6x1517691501742.png";
    public static String IMAGE_DOWNLOAD_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_9x1517693458213.png";

    public static Image audioPlayImage;
    public static Image videoPlayImage;
    public static Image generalFileDownloadImage;
    public static Image excelFileDownloadImage;
    public static Image pptFileDownloadImage;
    public static Image wordFileDownloadImage;
    public static Image pdfFileDownloadImage;
    public static Image imageFileDownloadImage;

    public static synchronized void initVars()
    {
        if( AUDIO_PLAYBACK_URL== null )
            AUDIO_PLAYBACK_URL = RuntimeConstants.getStringValue( "ivrCustomTestAudioPlayIconUrl" );

        if( VIDEO_PLAYBACK_URL== null )
            VIDEO_PLAYBACK_URL = RuntimeConstants.getStringValue( "avCustomTestVideoPlayIconUrl" );
    }


    public synchronized void initFonts() throws Exception
    {
        initSettings( reportData );

        String logoUrl = reportData.getReportCompanyImageUrl();

        if( logoUrl == null && reportData.s!=null && reportData.s.getReportLogoUrl()!=null && !reportData.s.getReportLogoUrl().isBlank() )
            logoUrl = reportData.s.getReportLogoUrl() ;

        if( logoUrl == null && reportData.o.getReportLogoUrl()!=null && !reportData.o.getReportLogoUrl().isBlank() )
            logoUrl = reportData.o.getReportLogoUrl() ;

        if( logoUrl!=null && !logoUrl.isBlank() )
        {
            String lu = logoUrl.toLowerCase();
            if( !lu.contains(".png") && !lu.contains(".gif") && !lu.contains(".jpg") && !lu.contains(".jpeg") )
            {
                LogService.logIt( "BaseCt2ReportTemplate.initFonts() AAA.1 LogoURL appears to be invalid format. Ignoring. OrgId=" + reportData.o.getOrgId() + ", logo=" + logoUrl );
                logoUrl=null;
            }
        }

        if( logoUrl!= null && StringUtils.isCurlyBracketed( logoUrl ) )
            logoUrl = RuntimeConstants.getStringValue( "translogoimageurl" );

        try
        {
            custLogo = (logoUrl == null || logoUrl.isBlank()) ? null : ITextUtils.getITextImage( (new URI(logoUrl)).toURL() );  //   getImageInstance(logoUrl, reportData.te.getTestEventId());
        }

        catch( Exception e )
        {
            custLogo = null;

            if( e instanceof IOException && logoUrl!=null && logoUrl.trim().toLowerCase().startsWith("https:"))
            {
                LogService.logIt( "BaseCt2ReportTemplate.initFonts() BBB.1 NONFATAL error getting custLogo. Will try http instead of https. logo=" + logoUrl + ", testEventId=" + reportData.te.getTestEventId() );

                String logo2 = "http:" + logoUrl.trim().substring(6, logoUrl.length());

                try
                {
                    custLogo = ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( logo2 ) );
                    // custLogo = getImageInstance(logo2, reportData.te.getTestEventId()); // ITextUtils.getITextImage( com.tm2score.util.HttpUtils.getURLFromString( logo2 ) );
                }
                catch( IOException ee )
                {
                    custLogo=null;
                    int orgId=reportData==null || reportData.getOrg()==null ? 0 : reportData.getOrg().getOrgId();
                    if( orgId<=0 && reportData!=null && reportData.getTestEvent()!=null )
                        orgId = reportData.getTestEvent().getOrgId();
                    LogService.logIt( "BaseCt2ReportTemplate.initFonts() BBB.3 NONFATAL error getting custLogo using http. OrgId=" + orgId + ". Will use null. logo=" + logoUrl + ", logo2=" + logo2 + ", testEventId=" + reportData.te.getTestEventId() );

                    // if the logo URL is bad. Remove it - permanently.
                    if( reportData.getOrg()!=null && !logoUrl.isBlank() && reportData.getOrg().getReportLogoUrl()!=null && reportData.getOrg().getReportLogoUrl().equals(logoUrl))
                    {
                        try
                        {
                            UserFacade uf = UserFacade.getInstance();
                            LogService.logIt( "BaseCt2ReportTemplate.initFonts() Removing erroneous image reference for OrgId=" + orgId + ", logo=" + logoUrl + ", " + ee.toString() + ", " + ee.getMessage() + ", testEventId=" + reportData.te.getTestEventId() );
                            reportData.getOrg().setReportLogoUrl(null);
                            uf.saveOrg(reportData.getOrg());
                        }
                        catch( Exception eee )
                        {
                            LogService.logIt( eee, "BaseCt2ReportTemplate.initFonts() BBB.4 NONFATAL Could not remove erroneous image reference for OrgId=" + orgId + ", testEventId=" + reportData.te.getTestEventId() );
                        }
                    }

                }
                catch( Exception ee )
                {
                    custLogo=null;
                    int orgId=reportData==null || reportData.getOrg()==null ? 0 : reportData.getOrg().getOrgId();
                    if( orgId<=0 && reportData!=null && reportData.getTestEvent()!=null )
                        orgId = reportData.getTestEvent().getOrgId();
                    LogService.logIt( ee, "BaseCt2ReportTemplate.initFonts() BBB.6 NONFATAL error getting custLogo using http. OrgId=" + orgId + ". Will use null. logo=" + logoUrl + ", logo2=" + logo2 + ", testEventId=" + reportData.te.getTestEventId() );
                }
            }

            else
            {
                LogService.logIt( "BaseCt2ReportTemplate.initFonts() CCC.1 NONFATAL error getting custLogo. Will use null. logo= " + logoUrl + ", Exception=" + e.toString() );
            }
        }


        // !reportData.hasCustLogo() ? null : ITextUtils.getITextImage( reportData.getCustLogoUrl() );

        if( custLogo != null )
        {
            // LogService.logIt( "BaseCt2ReportTemplate.initFonts() AAA custLogo size=" + custLogo.getWidth() + "x" + custLogo.getHeight() + ", scaledSize=" + custLogo.getScaledWidth() + "x" + custLogo.getScaledHeight() );
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
            {
                custLogo.scalePercent( imgSclW );
                // LogService.logIt( "BaseCt2ReportTemplate.initFonts() WWW custLogo scalePercent=" + imgSclW + ", size=" + custLogo.getWidth() + "x" + custLogo.getHeight() + ", scaledSize=" + custLogo.getScaledWidth() + "x" + custLogo.getScaledHeight() );
            }

        }

        title = StringUtils.replaceStr( this.tctrans(reportData.getReportName(),false), "[SIMNAME]", reportData.getSimName() );

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

        // LogService.logIt( "BaseCT2ReportTemplate.initFonts() title=" + title );
    }


    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        ctReportData = new CT2ReportData();

        initLocales();

        initFonts();

        parseCustomLogos();

        initColors();

        if( 1==1 )
        {
            if( ct2Colors!=null )
                ct2Colors.clearBorders();

            scoreBoxBorderWidth = 0;
            lightBoxBorderWidth=0;
        }

        parseCustomColors();

        prepNotes = new ArrayList<>();

        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        // pdfWriter = new PdfCopy(document, baos);
        // pdfWriter.addJavaScript( "" );

        // LogService.logIt( "BaseCT2ReportTemplate.init() title=" + rd.getReportName() );

        CT2HeaderFooter hdr = new CT2HeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this, custLogo );

        pdfWriter.setPageEvent(hdr);

        document.open();

        document.setMargins(36, 36, 36, 36 );


        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        // pdfWriter.addJavaScript( DOCUMENT_JS_API );


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


    public void parseCustomColors()
    {
        String custColors = reportData.getReportRuleAsString( "basereportcolors" );
        if( custColors==null || custColors.isBlank() )
            return;

        String[] vals = custColors.split(",");
        String custCol;
        BaseColor bc;
        if( vals.length>0 )
        {
            custCol = vals[0].trim();
            if( !custCol.isEmpty() )
            {
                bc = ITextUtils.getItextBaseColorFromRGBStr(custCol);
                if( bc!=null )
                {
                    ct2Colors.hraBlue = bc;
                    ct2Colors.headerDarkBgColor = ct2Colors.hraBlue;
                }
            }
        }
    }

    public void parseCustomLogos()
    {
        // image used in header / footer AFTER first page. HEADER LOGO
        String whiteTextSmallLogoUrl = reportData.getReportRuleAsString( "reportlogo1" );

        Image tempImg;
        int sp;
        if( whiteTextSmallLogoUrl!=null && !whiteTextSmallLogoUrl.isBlank() )
        {
            // LogService.logIt("BaseVWGAReportTemplate.initFonts() AAA whiteTextSmallLogoUrl=" + whiteTextSmallLogoUrl );
            try
            {
                sp = reportData.getReportRuleAsInt( "reportlogoscale1" );
                // Logo used in standard header/footer

                tempImg = ITextUtils.getITextImage( (new URI(whiteTextSmallLogoUrl)).toURL() );

                // LogService.logIt("BaseVWGAReportTemplate.initFonts() AAA.2 whiteTextSmallLogoUrl HxW" + tempImg.getScaledWidth() + "x" + tempImg.getScaledHeight() + ", sp=" + sp );

                if(tempImg!=null && sp>0 && sp<=100 )
                    tempImg.scalePercent(sp);

                // LogService.logIt("BaseVWGAReportTemplate.initFonts() AAA.3 whiteTextSmallLogoUrl Scaled HxW" + tempImg.getScaledWidth() + "x" + tempImg.getScaledHeight() );

                if( tempImg!=null )
                    hraLogoWhiteTextSmall = tempImg;
            }
            catch( Exception e )
            {
                LogService.logIt(e, "BaseVWGAReportTemplate.initFonts() whiteTextSmallLogoUrl=" + whiteTextSmallLogoUrl );
            }
        }

        // image used in cover page on top. COVER LOGO
        String blackTextLargerLogoUrl = reportData.getReportRuleAsString( "reportlogo2" );
        if( blackTextLargerLogoUrl!=null && !blackTextLargerLogoUrl.isBlank() )
        {
            // LogService.logIt("BaseVWGAReportTemplate.initFonts() BBB.2 blackTextLargerLogoUrl=" + blackTextLargerLogoUrl );
            try
            {
                sp = reportData.getReportRuleAsInt( "reportlogoscale2" );
                // Logo used in standard header/footer
                tempImg = ITextUtils.getITextImage( (new URI(blackTextLargerLogoUrl)).toURL() );

                // LogService.logIt("BaseVWGAReportTemplate.initFonts() BBB.2 blackTextLargerLogo HxW" + tempImg.getScaledWidth() + "x" + tempImg.getScaledHeight() + ", sp=" + sp );

                if( tempImg!=null && sp>0 && sp<=100 )
                    tempImg.scalePercent(sp);

                // LogService.logIt("BaseVWGAReportTemplate.initFonts() BBB.3 blackTextLargerLogo Scaled HxW" + tempImg.getScaledWidth() + "x" + tempImg.getScaledHeight() );

                if( tempImg!=null )
                {
                    hraLogoBlackText=tempImg;
                    custLogo=null;
                }
            }
            catch( Exception e )
            {
                LogService.logIt(e, "BaseVWGAReportTemplate.initFonts() blackTextLargerLogoUrl=" + blackTextLargerLogoUrl );
            }
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
            // this.needsOnetTrans=true;
            reportData.setNeedsKeyCheck( isOkToAutoTranslate( rptLoc ) );

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


    @Override
    public void dispose() throws Exception
    {
        if( baos != null )
            baos.close();
    }



    @Override
    public Locale getReportLocale()
    {
        if( reportData!=null )
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



        @Override
    public String getReportGenerationNotesToSave()
    {
        TestEventScore tes = reportData.getTestEvent().getTestEventScoreForReportId( reportData.getReport().getReportId(), reportData.getReport().getLocaleForReportGen().toString() );

        // regurgitate if needed.
        if( tes == null )
            return null;

        return tes.getTextParam1();
    }



    public void addReportInfoHeader() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            // Font fnt = getFontXLarge();
            if( reportData.getReportRuleAsBoolean( "ovroff" ) )
                return;

            boolean hideOverallNumeric = reportData.getReportRuleAsBoolean( "ovrnumoff" );
            boolean hideOverallGraph = reportData.getReportRuleAsBoolean( "ovrgrphoff" );
            boolean useScoreTextAsNumScore = reportData.getReportRuleAsBoolean( "ovrscrtxtasnum" );
            boolean hideOverallScoreText = reportData.getReportRuleAsBoolean( "ovrscrtxtoff" );
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );
            boolean piiOk = !reportData.getReportRuleAsBoolean( "omitpiipdf" );

            boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(reportData.getOrg(), reportData.getTestEvent()) &&
                                         reportData.getTestEvent().getOverallTestEventScore()!=null; //  && reportData.getTestEvent().getOverallTestEventScore().getRawScore()>=0;

            float y = addTitle(previousYLevel, lmsg( "g.Overall" ), null, null, null );

            y -= TPAD;

            float scrValue = useRawOverallScore ? reportData.getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow() : reportData.getTestEvent().getOverallScore();

            String overallScoreTitle = reportData.getReport().getStrParam4()!=null && !reportData.getReport().getStrParam4().isEmpty() ? reportData.getReport().getStrParam4()  :  lmsg( useRawOverallScore ? "g.ScoreRaw" : "g.Score");

            int scrDigits = reportData.getReport().getIntParam2() >= 0 ? reportData.getReport().getIntParam2() : reportData.getTestEvent().getScorePrecisionDigits();

            String scr = I18nUtils.getFormattedNumber( reportData.getLocale(), scrValue, scrDigits );

            // String scr2 = null;

            TestEventScore tes = reportData.getTestEvent().getOverallTestEventScore();

            if( tes==null )
                throw new Exception( "No Overall TestEventScore found in testEvent. testEventId=" + reportData.getTestEvent().getTestEventId() );

            if( reportData.hasProfile() )
                 tes.setProfileBoundaries( reportData.getOverallProfileData() );

            java.util.List<TextAndTitle> ct3RiskFactors = ScoreFormatUtils.getTextTitleList(reportData.getTestEvent(), CT3Constants.CT3RISKFACTORS );

            correctRiskFactorTextForReportLang( ct3RiskFactors );

            ct3RiskFactors.addAll( ScoreFormatUtils.getTextTitleList(reportData.getTestEvent(), Constants.STD_RISKFACTORSKEY ) );

            // LogService.logIt( "Found " + ct3RiskFactors.size() + " CT3 Risk Factors." );
            ScoreFormatType sft = ScoreFormatType.getValue( tes.getScoreFormatTypeId() );

            ScoreCategoryType sct = useRawOverallScore ? ScoreCategoryType.getScoreCategoryTypeForRawScore( ScoreFormatType.NUMERIC_0_TO_100, scrValue) : tes.getScoreCategoryType();

            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ?  new float[] { .4f, .1f, .25f, .25f } : new float[] { .25f, .25f, .1f, .4f };

            boolean overallTimeout = tes.getIntParam1()>0;

            boolean includeNumScores = !hideOverallNumeric; // reportData.getReport().getIncludeSubcategoryNumeric()==1;
            boolean includeRating = reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean includeColorGraph = !hideOverallGraph && sft.getSupportsBarGraphic(reportData.getReport()) && reportData.getReport().getIncludeColorScores()==1; // && reportData.getReport().getIncludeCompetencyColorScores()==1;

            boolean useSolidBarGraphs = includeColorGraph && reportData.getReportRuleAsBoolean( "overallcoloriconasgraph" ); //  .getReportRule( "overallcoloriconasgraph" )!=null && !reportData.getReportRule( "overallcoloriconasgraph" ).equals( "0");

            boolean includeRiskFactorsAfterDetail = reportData.getReportRuleAsBoolean( "ct3risktoend" ); //  hasRule1 != null && hasRule1.equals( "1" );
            boolean riskFactorsOk = !reportData.getReportRuleAsBoolean( "ct3riskremove" ); //  hasRule2 == null || !hasRule2.equals( "1" );
            boolean showRiskFactors = riskFactorsOk && !includeRiskFactorsAfterDetail && !ct3RiskFactors.isEmpty() && reportData.getR2Use().getIntParam1()==0;
            // LogService.logIt( "BaseCT2ReportTemplate.getReportInfoHeader() includeRiskFactorsAfterDetail=" + includeRiskFactorsAfterDetail + ", riskFactorsOk=" + riskFactorsOk + ", ct3RiskFactors.size()=" + ct3RiskFactors.size() + ", reportData.getR2Use().getIntParam1()=" + reportData.getR2Use().getIntParam1() + " reportId=" + reportData.getR2Use().getReportId() );

            String earlyExitStr = StringUtils.getBracketedArtifactFromString( reportData.te.getTextStr1(), Constants.EARLYEXITBATTERYKEY );
            if( earlyExitStr!=null )
                earlyExitStr = BatteryScoringUtils.getEarlyExitWarningMessage(reportData.getLocale(), earlyExitStr );

            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            // String thirdPartyTestEventIdentifierName = reportData.getThirdPartyTestEventIdentifierName();

            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            String cefrLevel = StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRLEVEL);
            if( cefrLevel!=null )
            {
                if( CefrScoreType.getFromText(cefrLevel).equals(CefrScoreType.UNKNOWN))
                    cefrLevel=null;
            }

            // Next row - Text
            String scrTxt = null; //getTestEvent().getOverallTestEventScore().getScoreText();

            if( !useScoreTextAsNumScore && ( hideOverallScoreText || reportData.getReport().getIncludeScoreText()!=1 )  )
                scrTxt = null;

            else if( useRawOverallScore )
            {
                if( reportData.te.getSimXmlObj()==null )
                {
                    if( reportUtils==null )
                        reportUtils  = new ReportUtils();

                    reportUtils.loadTestEventSimXmlObject(reportData.te);

                    scrTxt = BaseTestEventScorer.getScoreTextForOverallScore( reportData.getTestEvent().getScoreColorSchemeType(), scrValue, reportData.te );
                }
            }

            else
            {
                scrTxt = reportData.getOverallScoreText();
            }

            if( scrTxt!=null )
                scrTxt = tctrans( scrTxt,false );

            if( useScoreTextAsNumScore )
            {
                scr = ReportUtils.getScoreValueFromStr( scrTxt );

                if( scr==null )
                    scr = "";
            }

            if( hideOverallScoreText || reportData.getReport().getIncludeScoreText() != 1 )
                scrTxt = null;
            else
            {
                scrTxt = ReportUtils.getScoreTextFromStr(scrTxt);

                if( cefrLevel!=null )
                {
                    if( scrTxt==null )
                        scrTxt="";
                    String stub = (String) CefrUtils.getCefrScoreInfoForOverall(reportData.getTestEvent(), reportData.getTestEvent().getTestEventScoreList())[1];

                    CefrScoreType cefrScoreType = CefrScoreType.getFromText(cefrLevel);

                    if( cefrScoreType!=null )
                    {
                        if( reportData.equivSimJUtils!=null )
                        {
                            if( reportData.te.getSimXmlObj()==null )
                            {
                                if( reportUtils==null )
                                    reportUtils  = new ReportUtils();

                                reportUtils.loadTestEventSimXmlObject(reportData.te);
                            }

                            ScoreCategoryType cefrScoreCategoryType = CefrUtils.getCefrScoreCategoryType(reportData.te.getSimXmlObj(), cefrScoreType, reportData.getTestEvent().getScoreColorSchemeType() );

                            String s = reportData.equivSimJUtils.getOverallScoreText( cefrScoreCategoryType.getScoreCategoryTypeId(), cefrScoreType.getNumericEquivalentScore( reportData.getTestEvent().getScoreColorSchemeType() ) );

                            if( s!=null && !s.isBlank() )
                            {
                                LogService.logIt( "Found CEFR-Language-Equivalent Score Text=" + s );
                                scrTxt = s;
                            }
                        }

                        String cefrScoreText = CefrUtils.getCefrScoreDescription(reportData.getLocale(), cefrScoreType, stub); // StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRLEVELTEXT);
                        if( cefrScoreText!=null && !cefrScoreText.isBlank() )
                            scrTxt = lmsg("g.CefrEquivScoreText") + " " + cefrScoreText + (scrTxt.isBlank() ? "" : "\n\n" + lmsg("g.GeneralScoreText") + " " ) +  scrTxt;
                    }
                }
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addReportInfoHeader() includeColorGraph=" + includeColorGraph + ", useSolidBarGraphs=" + useSolidBarGraphs );

            // include nothing
            if( !includeNumScores && cefrLevel==null && !includeColorGraph && !includeRating )
            {
                // includeStars = false;
                cols -= 3;
                colRelWids = reportData.getIsLTR() ? new float[] { 1 } : new float[] { 1 };
            }

            // include only Rating
            else if( !includeNumScores && cefrLevel==null && !includeColorGraph )
            {
                // includeStars = false;
                cols -= 2;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f } : new float[] { .2f, .45f };
            }

            // include only Numeric scores
            else if( !includeRating && !includeColorGraph )
            {
                // includeStars = false;
                cols -= 2;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f } : new float[] { .2f, .45f };
            }

            // include only Color Graph scores
            else if( !includeNumScores && cefrLevel==null && !includeRating )
            {
                // includeStars = false;
                cols -= 2;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f } : new float[] { .2f, .45f };
            }

            // include color graph and rating
            else if( !includeNumScores && cefrLevel==null )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ? new float[] { .37f, .35f, .35f} : new float[] { .35f, .35f, .37f };
            }

            // include num scores and rating
            else if( !includeColorGraph )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ? new float[] { .37f, .28f, .35f} : new float[] { .35f, .28f, .37f };
            }

            // include num scores and graph
            else if( !includeRating )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ? new float[] { .45f, .2f, .35f}  : new float[] { .35f, .2f, .45f };
            }


            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            // t.setHeaderRows( 1 );


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            boolean isRightMost = cols==1;

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Candidate"), fontLargeWhite ) );

            //if( isRightMost )
            //    c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
            //else
            //    c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );

            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );

            //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            //c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, true) );
            setRunDirection( c );

            touter.addCell(c);

            if( includeNumScores || cefrLevel!=null )
            {
                isRightMost = cols == 2;

                c = new PdfPCell( new Phrase( overallScoreTitle, fontLargeWhite ) );
                c.setColspan( 1 );

                //if( includeStars )
                //    c.setBorder( Rectangle.TOP );
                //else
                if( isRightMost )
                {
                    // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false ) );
                }
                else
                {
                    // c.setBorder( Rectangle.TOP );
                    c.setBackgroundColor( ct2Colors.hraBlue );
                } // | Rectangle.RIGHT );

                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( scoreBoxBorderWidth );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
            }

            if( includeColorGraph )
            {
                isRightMost = !includeRating;

                c = new PdfPCell( new Phrase( lmsg( "g.Interpretation"), fontLargeWhite ) );
                c.setColspan( 1 );

                if( isRightMost )
                {
                    // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
                }
                else
                {
                    // c.setBorder( Rectangle.TOP );
                    c.setBackgroundColor( ct2Colors.hraBlue );
                }

                c.setBorder( Rectangle.NO_BORDER );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                c.setBackgroundColor( ct2Colors.hraBlue );
                // c.setPaddingLeft( 25 );
                setRunDirection( c );
                touter.addCell(c);
            }

            if( includeRating )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.JobMatch"), this.fontLargeWhite ) );
                // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT :  Rectangle.TOP | Rectangle.LEFT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, true, false) );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);
            }

            // dark header row is finished.

            // t.setWidthPercentage( 0.8f );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setColspan(cols);
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( BaseColor.WHITE );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );

            touter.addCell( c );

            // NAME
            c = new PdfPCell( new Phrase( piiOk ? reportData.getUserName() : "" , fontXLargeBlack ) );
            //c.setColspan(colspan);
            c.setPadding( 1 );
            c.setPaddingTop(4);

            if( reportData.getIsLTR() )
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            else
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

            c.setVerticalAlignment( Element.ALIGN_TOP );

            //if( isRightMost )
            //    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            //else
            //    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );

            c.setBorder( Rectangle.NO_BORDER );

            c.setBorderColor( BaseColor.WHITE );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPaddingBottom( 2 );
            setRunDirection( c );
            touter.addCell( c );

            boolean useCircleScore = includeNumScores && cefrLevel==null && scrDigits<=1 && includeColorGraph;
            int rowSpanCoverCount = 0;
            int rowSpanUsedForScore = 2;

            if(includeNumScores || cefrLevel!=null)
            {
                c = new PdfPCell( new Phrase( !useCircleScore && includeNumScores ? scr : "" , fontLargeBold ) );
                c.setPadding( 2 );
                c.setPaddingTop(4);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                //if( isRightMost )
                //    c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT );
                //else
                c.setBorder(Rectangle.NO_BORDER );
                c.setBorderColor( BaseColor.WHITE );
                c.setBorderWidth( scoreBoxBorderWidth );

                if( useCircleScore )
                {

                    c.setRowspan(rowSpanUsedForScore);
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                    c.setCellEvent( new CircleScoreCellEvent( scr, sct.getBaseColor(), sct.getBaseForegroundColor(), sct.foregroundDark() ? fontLargeBold : fontLargeWhiteBold ));
                }
                else
                    c.setVerticalAlignment( Element.ALIGN_TOP );

                setRunDirection( c );
                touter.addCell( c );
            }

            if( includeColorGraph )
            {
                isRightMost = !includeRating;

                PdfPTable t2 = new PdfPTable( 1 );
                setRunDirection( t2 );
                t2.setWidthPercentage( 100 );
                // t2.setLockedWidth( true );

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setPadding( 0 );
                c.setFixedHeight(reportData.getTestEvent().getUseBellGraphs() ? BELL_GRAPH_CELL_HEIGHT : BAR_GRAPH_CELL_HEIGHT);
                c.setCellEvent( useSolidBarGraphs ? new CT2SolidBarGraphicCellEvent( tes , reportData.getR2Use(), sct, reportData.getTestEvent().getScoreColorSchemeType(), reportData.p, ct2Colors, devel ) :
                                 new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, false, ct2Colors, devel, useRawOverallScore, reportData.getTestEvent().getUseBellGraphs(), true, 0 ) );
                t2.addCell(c);

                c = new PdfPCell( t2 ); // new PdfPCell( summaryCatNumericAxis );
                if( isRightMost )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                else
                    c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( BaseColor.WHITE );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setPaddingTop( 2 );
                touter.addCell( c );
            }

            if( includeRating )
            {
                Image scrCatImg = getScoreCategoryImg( sct, false );

                c = scrCatImg==null ? new PdfPCell( new Phrase("",font) )  : new PdfPCell( scrCatImg ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                c.setPadding( 0 );
                c.setPaddingTop( 9 );
                touter.addCell( c );
            }

            if( reportData.getUser()==null )
                reportData.u = new User();


            // Next Row - CEFR score IF present.
            if( cefrLevel!=null )
            {
                CefrScoreType st = CefrScoreType.getFromText(cefrLevel);

                c = new PdfPCell( new Phrase( lmsg("g.CefrEquivScore") , fontLarge ) );
                c.setPadding( 1 );
                c.setPaddingTop(4);
                c.setPaddingBottom(4);
                if( reportData.getIsLTR() )
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                c.setVerticalAlignment( Element.ALIGN_TOP );

                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPaddingBottom( 2 );
                setRunDirection( c );
                touter.addCell( c );

                // LogService.logIt( "BaseCT2ReportTemplate.addReportInfoHeader() includeColorGraph=" + includeColorGraph + ", useSolidBarGraphs=" + useSolidBarGraphs );

                // CEFR Score
                c = new PdfPCell( new Phrase( st.getName(reportData.getLocale()) , fontLargeBold ) );
                c.setPadding( 2 );
                c.setPaddingTop(4);
                c.setPaddingBottom(4);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setVerticalAlignment( Element.ALIGN_TOP );
                c.setBorder(Rectangle.NO_BORDER );
                setRunDirection( c );
                touter.addCell( c );

                // dummy cell
                if( includeColorGraph )
                {
                    c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPaddingTop( 4 );
                    c.setPaddingBottom( 4 );
                    touter.addCell( c );
                }

                // dummy cell
                if( includeRating )
                {
                    c = new PdfPCell( new Phrase("") );
                    c.setBorder( Rectangle.NO_BORDER  );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                    c.setPadding( 0 );
                    c.setPaddingTop( 4 );
                    c.setPaddingBottom( 4 );
                    touter.addCell( c );
                }
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addReportInfoHeader() overallTimeout=" + overallTimeout );

            // Had an overall timeout
            if( overallTimeout )
            {
                c = new PdfPCell( new Phrase( lmsg("g.OvrTimeout"), getFontBoldRed() ) );

                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                    c.setColspan( 1 );
                else
                    c.setColspan( cols );
                c.setPadding( 1 );

                if( reportData.getIsLTR() )
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                // c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );

                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                {
                    if( cols>2 )
                    {
                        c = new PdfPCell( new Phrase( "", getFontBold() ) );
                        c.setColspan( cols-2 );
                        c.setBorder( Rectangle.NO_BORDER  );
                        touter.addCell( c );
                    }
                    rowSpanCoverCount++;
                }

            }

            // Next Row - Email
            if( piiOk &&
                reportData.getUser().getUserType().getNamed() &&
                reportData.getUser().getEmail() != null &&
                !reportData.getUser().getEmail().isEmpty() &&
                !StringUtils.isCurlyBracketed( reportData.getUser().getEmail() ) )
            {
                c = new PdfPCell( new Phrase( reportData.getUser().getEmail(), getFontLight() ) );
                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                    c.setColspan( 1 );
                else
                    c.setColspan( cols );

                c.setPadding( 1 );

                if( reportData.getIsLTR() )
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );


                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );

                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                {
                    if( cols>2 )
                    {
                        c = new PdfPCell( new Phrase( "", getFontBold() ) );
                        c.setColspan( cols-2 );
                        c.setBorder( Rectangle.NO_BORDER  );
                        touter.addCell( c );
                    }
                    rowSpanCoverCount++;
                }

            }

            // Next row - test name
            c = new PdfPCell( new Phrase( reportData.getSimName(), getFontLight() ) );
            if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                c.setColspan( 1 );
            else
                c.setColspan( cols );
            c.setPadding( 1 );
            if( reportData.getIsLTR() )
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            else
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );

            if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
            {
                if( cols>2 )
                {
                    c = new PdfPCell( new Phrase( "", getFontBold() ) );
                    c.setColspan( cols-2 );
                    c.setBorder( Rectangle.NO_BORDER  );
                    touter.addCell( c );
                }
                rowSpanCoverCount++;
            }


            // Note - the date MAY be the last entry in this table if there is no score text.
            boolean lastEntry = !includeColorGraph &&
                                !hasThirdPartyId &&
                                (reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty()) &&
                                (scrTxt == null || scrTxt.isEmpty()) &&
                                !showRiskFactors &&
                                earlyExitStr==null;


            // Next Row, test date
            if( includeDates )
            {
                c = new PdfPCell( new Phrase( reportData.getSimCompleteDateFormatted(), getFontLight() ) );
                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                    c.setColspan( 1 );
                else
                    c.setColspan( cols );
                c.setPadding( 1 );
                // c.setPaddingBottom( 6 );
                if( reportData.getIsLTR() )
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

                if( lastEntry )
                {
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setPaddingBottom(4);
                }
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );

                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                {
                    if( cols>2 )
                    {
                        c = new PdfPCell( new Phrase( "", getFontBold() ) );
                        c.setColspan( cols-2 );
                        c.setBorder( Rectangle.NO_BORDER  );
                        touter.addCell( c );
                    }
                    rowSpanCoverCount++;
                }
            }

            if( hasThirdPartyId )
            {
                lastEntry = !includeColorGraph &&
                            (reportData.getUser().getMobilePhone()==null || reportData.getUser().getMobilePhone().isEmpty()) &&
                            (scrTxt == null || scrTxt.isEmpty()) &&
                            !showRiskFactors &&
                            earlyExitStr==null;

                //if( thirdPartyTestEventIdentifierName==null || thirdPartyTestEventIdentifierName.isEmpty() )
                //    thirdPartyTestEventIdentifierName = lmsg( "g.ThirdPartyEventIdC" );
                //else
                //    thirdPartyTestEventIdentifierName += ":";

                c = new PdfPCell( new Phrase( thirdPartyId, getFontLight() ) );
                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                    c.setColspan( 1 );
                else
                    c.setColspan( cols );
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );

                if( reportData.getIsLTR() )
                {
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setPaddingBottom(4);
                }
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

                if( lastEntry )
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );

                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                {
                    if( cols>2 )
                    {
                        c = new PdfPCell( new Phrase( "", getFontBold() ) );
                        c.setColspan( cols-2 );
                        c.setBorder( Rectangle.NO_BORDER  );
                        touter.addCell( c );
                    }
                    rowSpanCoverCount++;
                }

            }



            if( piiOk && reportData.getUser().getMobilePhone()!=null && !reportData.getUser().getMobilePhone().isEmpty() )
            {
                lastEntry = !includeColorGraph &&
                            (scrTxt == null || scrTxt.isEmpty()) &&
                            !showRiskFactors &&
                            earlyExitStr==null;

                c = new PdfPCell( new Phrase( "(m) " + reportData.getUser().getMobilePhone(), getFontLight() ) );
                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                    c.setColspan( 1 );
                else
                    c.setColspan( cols );
                c.setPadding( 1 );
                //c.setPaddingBottom( 6 );
                if( reportData.getIsLTR() )
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                else
                    c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );

                if( lastEntry )
                {
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setPaddingBottom(4);
                }
                else
                    c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                touter.addCell( c );

                if( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
                {
                    if( cols>2 )
                    {
                        c = new PdfPCell( new Phrase( "", getFontBold() ) );
                        c.setColspan( cols-2 );
                        c.setBorder( Rectangle.NO_BORDER  );
                        touter.addCell( c );
                    }
                    rowSpanCoverCount++;
                }
            }


            // add dummy columns if still not covered.
            while( useCircleScore && rowSpanCoverCount<rowSpanUsedForScore-1 )
            {
                c = new PdfPCell( new Phrase( "", getFontLight() ) );
                c.setColspan( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );
                touter.addCell( c );
                if( cols>2 )
                {
                    c = new PdfPCell( new Phrase( "", getFontLight() ) );
                    c.setColspan( cols-2 );
                    c.setBorder( Rectangle.NO_BORDER  );
                    touter.addCell( c );
                }
                rowSpanCoverCount++;
            }

            //LogService.logIt( "BaseCT2ReportTemplate.getReportInfoHeader() reportData.getIsLTR()=" + reportData.getIsLTR() + ", includeRiskFactorsAfterDetail=" + includeRiskFactorsAfterDetail + ", showRiskFactors=" + showRiskFactors );

            PdfPTable t3 = new PdfPTable( 1 );
            setRunDirection( t3 );
            t3.setWidthPercentage( 100 );

            c = t3.getDefaultCell();
            c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            //c.setPhrase(null);
            setRunDirection( c );

            if( (scrTxt != null && !scrTxt.isEmpty()) || showRiskFactors || earlyExitStr!=null )
            {
                // if( 1==1 )
                //     scrTxt += " dfsg sdfg sfdg sfdg sfd gsfd gsfdgkj sfdgkjsfhd gkjsfhd gkjsfhd gkjsfhd gksfjdh gksfjdgh sfkdjg sfkdjgh sfkdjgh sfdkjgh sfdkjgh sfdkjgh sfdkjgh sfdkjgh sfdkgjh sfdkjgh sfdkjgh sfdkjgh sfdkgjh fdskjgh sfdkjgh sfdkgjh sfdkgjh sfdkgjh fdkgjh sfdkjgh sfdkgjh sfdkgjh fdkjgh fdkjgh sdfkgj hsfdkgjh sfdkjgh sfdkjgh sfdkjgh sfdkgjh sfdkgjh sfdkgjh sfdkgjh sfdkjg hsfdkjgh sfkdjg hsfkdjgh sfkdjgh sfkdjgh sfkdjgh sjkdfgh skdfgh skdfjgh skdfjgh fdkjgh djkg dfkjgh dfkjgh dkfj ghkdfjgh fkdjgh dfkj ghfdkjgh fkdjgh fkdjg hfkdjg hfkdjgh fkdjgh fdkgjh fdkjgh fdjgh fjdgh fjdgh fjdgh fjdgh fjdgh fdjgh fdjghfjdghfdjgh fdjgh dfjgh dfjgh dfjgh djgh dfjgh djgh";

                if( earlyExitStr!=null )
                {
                    c = new PdfPCell( new Phrase( "\n" + earlyExitStr + "\n ", getFontRed() ));
                    c.setPadding( 1 );
                    setRunDirection(c);
                    c.setBorder( Rectangle.NO_BORDER );
                    t3.addCell( c );
                }

                if( scrTxt != null && !scrTxt.isEmpty() )
                {
                    c = new PdfPCell( new Phrase( scrTxt, getFontLight() ));
                    c.setPadding( 1 );
                    setRunDirection(c);
                    c.setBorder( Rectangle.NO_BORDER );
                    t3.addCell( c );
                }

                if( showRiskFactors )
                {
                    String rfHdrKey = "g.CT3RiskFactorsHdr";
                    String rfFtrKey = "g.CT3RiskFactorsFtr";

                    //float spcg = 8;

                    Chunk chnk = new Chunk( lmsg( "g.CT3RiskFactorsHdrTitle" ) + " " , this.getFontBoldRed() );
                    // Chunk chnk2 = new Chunk(  lmsg( rfHdrKey ) , getFontLight() );

                    Phrase cHdr = new Phrase();
                    cHdr.add( chnk );

                    // Phrase cFtr = new Phrase( lmsg( rfFtrKey ) , getFontLight() );

                    java.util.List<String> clj = new ArrayList<>();
                    com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                    cl.setListSymbol( "\u2022");
                    cl.setIndentationLeft( 10 );
                    cl.setSymbolIndent( 10 );

                    String rftxt;

                    for( TextAndTitle tt : ct3RiskFactors )
                    {
                        // LogService.logIt( "BaseCT2ReportTemplate.getReportInfoHeader() Text=" + tt.getText() + ", title=" + tt.getTitle()  + " flags=" + tt.getFlags() );

                        if( tt.getText()==null || tt.getText().isEmpty() )
                            continue;

                        rftxt = tt.getText();

                        if( rftxt.contains("[FACET]") )
                            rftxt = rftxt.substring(0,rftxt.indexOf( "[FACET]" ) );

                        rftxt = tctrans( rftxt, false );

                        cl.add( new ListItem( 9,  rftxt, getFontLight() ) );
                        clj.add( rftxt );
                    }

                    if( reportData.getIsLTR() )
                    {

                        c.addElement( cHdr );
                        c.addElement( cl );

                        // skipping cFtr
                        // c.addElement( cFtr );
                        t3.addCell( c );
                    }

                    else
                    {
                        c = new PdfPCell( cHdr );
                        c.setPadding( 1 );
                        setRunDirection(c);
                        c.setBorder( Rectangle.NO_BORDER );
                        t3.addCell( c );

                        for( String cs : clj )
                        {
                            c = new PdfPCell( new Phrase( "\u2022 " + cs , getFontLight() ) );
                            c.setPadding( 1 );
                            setRunDirection(c);
                            c.setBorder( Rectangle.NO_BORDER );
                            t3.addCell( c );
                        }

                        // skipping footer
                        // c = new PdfPCell( cFtr );
                        // c.setPadding( 1 );
                        // setRunDirection(c);
                        // c.setBorder( Rectangle.NO_BORDER );
                        // t3.addCell( c );
                    }
                } // End Risk Factors

                int colspan = cols;

                if( includeColorGraph )
                {
                    colspan--;

                    if( includeRating )
                        colspan--;
                }

                c = new PdfPCell();
                c.setPadding( 1 );
                setRunDirection(c);
                c.setColspan( colspan );

                if( reportData.getIsLTR() )
                    c.setBorder( includeColorGraph ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );
                else
                    c.setBorder( includeColorGraph ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM | Rectangle.LEFT  );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 0 );
                c.setPaddingTop( 6 );
                c.setPaddingRight( 4 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingBottom( 10 );
                setRunDirection( c );

                // Add the score text / risk factors table to touter.
                c.addElement( t3 );
                touter.addCell( c );

                // add score key table.
                if( includeColorGraph )
                {
                    PdfPTable keyTable = getScoreKeyTable( !useSolidBarGraphs && reportData.hasProfile() );
                    // c = new PdfPCell( keyTable );
                    c = new PdfPCell();
                    c.addElement(keyTable);
                    c.setColspan(includeRating ? 2 : 1 );
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_CENTER );
                    c.setBorder( reportData.getIsLTR() ? Rectangle.BOTTOM | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.LEFT  );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 0 );
                    c.setPaddingTop( 6 );
                    // c.setPaddingBottom( 10 );
                    // c.setCalculatedHeight( keyTable.calculateHeights() );
                    setRunDirection( c );
                    touter.addCell( c );
                }
            } // End has Score text

            else if( includeColorGraph )
            {
                PdfPTable lt = getScoreKeyTable( !useSolidBarGraphs && reportData.hasProfile() );

                lt.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT );

                c = new PdfPCell( );
                c.addElement( lt );
                // c = new PdfPCell( lt );
                c.setColspan( cols );
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_RIGHT : Element.ALIGN_CENTER );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 0 );
                c.setPaddingTop( 6 );
                c.setPaddingBottom( 10 );
                setRunDirection( c );
                touter.addCell( c );
            }

            // float y = pageHeight - headerHgt - 4*PAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addReportInfoHeader()" );
            throw e;
        }
    }



    public void addUploadedFilesSection() throws Exception
    {
        try
        {
            initVars();

            //if( !reportData.includeCompetencyScores() )
            //    return;
            boolean showVideoUrls = !reportData.getReportRuleAsBoolean( "pdfvideoviewoff" ) && !reportData.getTestKey().getHideMediaInReports();

            java.util.List<TextAndTitle> ttl = new ArrayList<>();

            ttl.addAll( reportData.te.getOverallTestEventScore().getTextBasedResponseList( NonCompetencyItemType.FILEUPLOAD.getTitle(), true ) );
            // ttl.addAll( reportData.te.getOverallTestEventScore().getTextBasedResponseList( NonCompetencyItemType.AV_UPLOAD.getTitle(), true ) );

            if( ttl.isEmpty() )
                return;

            java.util.List<TextAndTitle> ttl2 = new ArrayList<>();

            for( TextAndTitle tt : ttl )
            {
                if( tt.getUploadedUserFileId()<=0 )
                    continue;

                ttl2.add(tt);
            }

            if( ttl2.isEmpty() )
                return;

            ttl=ttl2;

            if( audioPlayImage == null )
                audioPlayImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( AUDIO_PLAYBACK_URL ) );

            if( videoPlayImage == null )
                videoPlayImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( VIDEO_PLAYBACK_URL ) );

            if( generalFileDownloadImage == null )
                generalFileDownloadImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( GENFILE_DOWNLOAD_URL ) );

            if( excelFileDownloadImage == null )
                excelFileDownloadImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( EXCEL_DOWNLOAD_URL ) );

            if( pptFileDownloadImage == null )
                pptFileDownloadImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( PPT_DOWNLOAD_URL ) );

            if( wordFileDownloadImage == null )
                wordFileDownloadImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( WORD_DOWNLOAD_URL ) );

            if( pdfFileDownloadImage == null )
                pdfFileDownloadImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( PDF_DOWNLOAD_URL ) );

            if( imageFileDownloadImage == null )
                imageFileDownloadImage = ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( IMAGE_DOWNLOAD_URL ) );



            previousYLevel =  currentYLevel - 10;

            if( previousYLevel <= footerHgt )
            {
                document.newPage();

                currentYLevel = 0;
                previousYLevel = 0;
            }

            // LogService.logIt( "AvReportTemplate.addAnyCompetenciesInfo() titleKey=" + titleKey + ", subtitleKey=" + subtitleKey + ", )subtitle=" + (subtitleKey==null ? "" : lmsg( subtitleKey ) )  );

            float y = addTitle(previousYLevel, lmsg( "g.UploadedUserFilesTitle" ), lmsg( "g.UploadedUserFilesSubtitle" ), null, null  );

            currentYLevel = y;

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( reportData.getIsLTR() ? new float[] { 4f, 2f, 5f } : new float[] { 5f,2f,4f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            boolean first = true;

            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();

            UploadedUserFile uuf;
            String url;

            Image iconImage;
            FileContentType fct;

            boolean top = true;

            Paragraph par;
            Chunk chk;

            // 0=view
            // 1=listen
            // 2=download
            int viewCode = 0;

            String viewKey;

            String thumbUrl;
            int thumbWid;

            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            boolean useGrayBg = true;

            // For each competency
            for( TextAndTitle tt : ttl )
            {
                uuf = fileUploadFacade.getUploadedUserFile( tt.getUploadedUserFileId(), true );

                if( uuf==null )
                {
                    LogService.logIt( "BaseCT2ReportTemplate.addUploadedFilesSection() UploadedUserFile for uufId=" + tt.getUploadedUserFileId() + " NOT FOUND. Could be a stray Scored Response so ignoring. TestEventId=" + reportData.getTestEvent().getTestEventId() );
                    continue;
                }

                if( !uuf.getUploadedUserFileStatusType().getAvailable() )
                {
                    LogService.logIt( "BaseCT2ReportTemplate.addUploadedFilesSection() UploadedUserFile for uufId=" + tt.getUploadedUserFileId() + " is not available (probably pseudonymized). Skipping. TestEventId=" + reportData.getTestEvent().getTestEventId() );
                    continue;
                }

                thumbUrl=null;

                fct = uuf.getFileContentType();

                if( fct.getIsVideo() )
                {
                    if( !showVideoUrls )
                    {
                        iconImage = Image.getInstance(  videoPlayImage );
                    }
                    else if( uuf.getThumbFilename()!=null && !uuf.getThumbFilename().isEmpty() )
                    {
                        String fn = uuf.getThumbFilename();
                        if( fn!=null && fn.contains( ".AWSCOUNT." ) )
                            fn = StringUtils.replaceStr( fn, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );

                        else if( fn!=null && fn.contains(  ".IDX." ) )
                            fn = StringUtils.replaceStr( fn, ".IDX." , ".1." );

                        thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( uuf, fn );

                        thumbWid = uuf.getThumbWidth();

                        iconImage =  getImageInstance(thumbUrl, reportData.te.getTestEventId()); // ITextUtils.getITextImage(com.tm2score.util.HttpUtils.getURLFromString( thumbUrl ) );

                        if( thumbWid>0 && thumbWid>60 )
                            iconImage.scalePercent( 100f*60f/((float) thumbWid) );
                        else if( thumbWid>0 )
                        {}
                        else
                            iconImage.scalePercent( 24f );
                    }
                    else
                        iconImage = Image.getInstance(videoPlayImage );
                }

                else if( fct.getIsAudio())
                {
                    iconImage = Image.getInstance(  audioPlayImage );
                    viewCode = 1;
                }
                else if( fct.getIsExcel())
                {
                    iconImage = Image.getInstance(  excelFileDownloadImage );
                    viewCode = 2;
                }
                else if( fct.getIsPpt())
                {
                    iconImage = Image.getInstance(  pptFileDownloadImage );
                    viewCode = 2;
                }
                else if( fct.getIsPdf())
                {
                    iconImage = Image.getInstance( pdfFileDownloadImage );
                    viewCode = 2;
                }
                else if( fct.getIsWord())
                {
                    iconImage = Image.getInstance(  imageFileDownloadImage );
                    viewCode = 2;
                }
                else if( fct.getIsImage())
                {
                    if( uuf.getFilename()!=null && !uuf.getFilename().isEmpty() )
                    {
                        thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( uuf, uuf.getFilename() );

                        //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                        //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + uuf.getFilename();
                        //else
                        //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + uuf.getFilename();

                        thumbWid = uuf.getWidth();

                        iconImage = getImageInstance(thumbUrl, reportData.te.getTestEventId());

                        if( thumbWid>0 && thumbWid>60 )
                            iconImage.scalePercent( 100f*60f/((float) thumbWid) );
                        else if( thumbWid>0 )
                        {}
                        else
                            iconImage.scalePercent( 24f );
                    }

                    else
                        iconImage =  Image.getInstance( wordFileDownloadImage );
                }
                else
                {
                    iconImage =  Image.getInstance( generalFileDownloadImage );
                    viewCode = 2;
                }

                if( fct.getIsAudio() || fct.getIsVideo() )
                {
                    if( !showVideoUrls )
                        url=null;

                    else if( uuf.getAvItemResponseId()>0 )
                        url = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + "/ta/avpb/" + reportData.getTestEvent().getTestEventId() + "/"  + uuf.getAvItemResponseId();
                    else
                        url = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + "/ta/uavpb/" + reportData.getTestEvent().getTestEventId() + "/"  + uuf.getUploadedUserFileId();
                        // url = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + "/ta/misc/av/avpb-entry.xhtml?teid=" + EncryptUtils.urlSafeEncrypt( reportData.getTestEvent().getTestEventId() ) + "&uufid="  + EncryptUtils.urlSafeEncrypt( uuf.getUploadedUserFileId() );
                }
                else
                    url = RuntimeConstants.getStringValue( "adminappbasuri" ) + "/duuf/" + tt.getUploadedUserFileId() + "/" + reportData.te.getOrgId() + "/" + reportData.te.getTestKeyId();

                if( thumbUrl==null )
                    iconImage.scalePercent( 32f );

                if( url!=null )
                    iconImage.setAnnotation( new Annotation( 0,0,0,0,url));

                iconImage.setAlignment( Image.ALIGN_MIDDLE | Image.ALIGN_CENTER );

                if( viewCode==1 )
                    viewKey = "g.Click2ListenC";
                else if( viewCode==2 )
                    viewKey = "g.Click2DownloadC";
                else
                    viewKey = "g.Click2ViewC";

                useGrayBg = !useGrayBg;


                // This tells iText to always use the first row as a header on subsequent pages.
                // t.setHeaderRows( 1 );

                c = new PdfPCell( new Phrase(tt.getTitle(),getFontSmall() ) );
                c.setBorder( top ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding( 1 );
                c.setPadding( 3 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );



                c = new PdfPCell( iconImage );
                c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( top ? Rectangle.TOP | Rectangle.BOTTOM : Rectangle.BOTTOM );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding( 1 );
                c.setPadding( 3 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );

                if( url==null || url.isEmpty() )
                    c.setColspan(2);

                setRunDirection( c );
                t.addCell( c );

                if( url!=null && !url.isEmpty() )
                {
                    par = new Paragraph( "\n", getFontSmall() );

                    chk = new Chunk( lmsg(viewKey) + "\n" );

                    par.add( chk );

                    chk = new Chunk( url );

                    PdfAction pdfa = PdfAction.gotoRemotePage( url , lmsg(viewKey), false, true );
                    chk.setAction( pdfa );
                    // chk.setAction( new PdfAction( url, true ) );
                    // chk.setAnchor( playUrl );

                    par.add( chk );

                    c = new PdfPCell( par );
                    c.setBorder( top ? Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                    c.setBackgroundColor(useGrayBg ? graybg : BaseColor.WHITE );
                    c.setPadding( 3 );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    setRunDirection( c );
                    t.addCell( c );
                }

                top=false;
            }

            // Add table
            currentYLevel = addTableToDocument(currentYLevel, t, false, true );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addUploadedFilesSection()" );

            throw e;
        }
    }


    public void correctRiskFactorTextForReportLang( java.util.List<TextAndTitle> ct3RiskFactors ) throws Exception
    {
        if( ct3RiskFactors==null || ct3RiskFactors.isEmpty() )
            return;

        // has report language set, compare to that
        if( reportData.getTestEvent().getLocaleStrReport()!=null &&
            !reportData.getTestEvent().getLocaleStrReport().isBlank() &&
            I18nUtils.getLanguageFromLocaleStr(reportData.getTestEvent().getLocaleStrReport()).equalsIgnoreCase( getReportLocale().getLanguage()))
            return;

        // no report language set, compare to test product language.
        else if( (reportData.getTestEvent().getLocaleStrReport()==null || reportData.getTestEvent().getLocaleStrReport().isBlank() ) &&
             reportData.getTestEvent().getProduct().getLocaleFmLangStr().getLanguage().equalsIgnoreCase( getReportLocale().getLanguage() ) )
            return;

        // LogService.logIt( "BaseCt2ReportTemplate.correctRiskFactorTextForReportLang() Starting. reportLocale=" + getReportLocale().toString() + ", risk factors=" + ct3RiskFactors.size() );

        int ct3RiskFactoryTypeId;
        CT3RiskFactorType c3rf;
        String revisedText;
        for( TextAndTitle tt : ct3RiskFactors )
        {
            if( tt.getTitle()==null || tt.getTitle().isEmpty() )
            {
                LogService.logIt( "BaseCt2ReportTemplate.correctRiskFactorTextForReportLang() No ct3 risk factor id in textAndTitle.title. parsed tt=" + tt.toString() );
                continue;
            }
            try
            {
                ct3RiskFactoryTypeId = Integer.parseInt( tt.getTitle() );
                c3rf = CT3RiskFactorType.getValue( ct3RiskFactoryTypeId );
                if( c3rf==null )
                {
                    LogService.logIt( "BaseCt2ReportTemplate.correctRiskFactorTextForReportLang() no CT3RiskFactory for ID. parsed ct3RiskFactoryTypeId=" + ct3RiskFactoryTypeId );
                    continue;
                }
                revisedText = null;

                // standard does not need params.
                if( c3rf.isStandard() )
                    revisedText = c3rf.getRiskText( getReportLocale(), null );

                // non-standard does need params.
                else
                {
                    String rfstr = null;

                    if( reportData.getTestEvent().getProduct().getProductType().getIsSim() )
                        rfstr = reportData.getTestEvent().getProduct().getStrParam2();
                    else if( reportData.getTestEvent().getProduct().getProductType().getIsCt5Direct())
                        rfstr = reportData.getTestEvent().getProduct().getPreviewHead();


                    if( rfstr!=null && !rfstr.isEmpty()  )
                    {
                        String[] g = rfstr.split("\\|");
                        Map<String,Object> params;
                        for( String cs : g )
                        {
                            if( cs==null || cs.isEmpty() || cs.indexOf( ';' )<=0 )
                                continue;

                            // LogService.logIt( "BaseCt2ReportTemplate.addReportRiskFactorSection() Examining Extra String " + cs );
                            params = CT3RiskFactorType.getParamsFromConfigStr(cs);
                            Integer ti = (Integer) params.get( "id" );
                            if( ti == null || ti<= 0 || ti!=ct3RiskFactoryTypeId )
                                continue;
                            revisedText = c3rf.getRiskText( getReportLocale(), params );
                        }
                    }
                }
                if( revisedText!=null && !revisedText.isBlank() )
                {
                    // LogService.logIt( "BaseCt2ReportTemplate.correctRiskFactorTextForReportLang() replacing text with language correct text: " + revisedText + ",  ct3RiskFactoryTypeId=" + ct3RiskFactoryTypeId );
                    tt.setText( revisedText );
                }
            }
            catch( NumberFormatException e )
            {
                LogService.logIt( "BaseCt2ReportTemplate.correctRiskFactorTextForReportLang() unable to parse ct3riskfactorytrypeid for TextTitle=" + tt.toString() + ", " + e.toString());
            }
        }
    }

    public void addReportRiskFactorSection() throws Exception
    {
        try
        {
            // TestEventScore tes = reportData.getTestEvent().getOverallTestEventScore();

            java.util.List<TextAndTitle> ct3RiskFactors = ScoreFormatUtils.getTextTitleList(reportData.getTestEvent(), CT3Constants.CT3RISKFACTORS );

            correctRiskFactorTextForReportLang( ct3RiskFactors );

            // Language cannot be corrected for standard risk factors.
            ct3RiskFactors.addAll( ScoreFormatUtils.getTextTitleList(reportData.getTestEvent(), Constants.STD_RISKFACTORSKEY ) );

            // String hasRule1 = this.reportData.getReportRule( "ct3risktoend" );

            boolean includeRiskFactorsAfterDetail = reportData.getReportRuleAsBoolean( "ct3risktoend" ); //  hasRule1 != null && hasRule1.equals( "1" );

            // String hasRule2 = this.reportData.getReportRule( "ct3riskremove" );

            boolean riskFactorsOk = !reportData.getReportRuleAsBoolean( "ct3riskremove" ); //  hasRule2 == null || !hasRule2.equals( "1" );

            boolean showRiskFactors =  riskFactorsOk && includeRiskFactorsAfterDetail && !ct3RiskFactors.isEmpty() && reportData.getR2Use().getIntParam1()==0;

            if( !showRiskFactors )
                return;

            // Font fnt = getFontXLarge();

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.RiskFactors" ), null, null, null );

            y -= TPAD;

            LogService.logIt( "BaseCT2ReportTemplate.addReportRiskFactorSection() Found " + ct3RiskFactors.size() + " CT3 Risk Factors." );

            int cols = 1;
            float[] colRelWids = new float[] { 1f };

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( cols );

            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            setRunDirection( touter );
            // t.setHeaderRows( 1 );


            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.RiskFactors" ), fontLargeWhite ) );
            // c.setBorder( Rectangle.TOP | Rectangle.LEFT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, true, true, true ));
            setRunDirection( c );
            touter.addCell(c);

            // header row is finished.

            // t.setWidthPercentage( 0.8f );

            c = touter.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            //c = new PdfPCell( new Phrase( reportData.getSimName(), this.getFontLargeLightBold() ) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment(Element.ALIGN_LEFT );
            //c.setVerticalAlignment( Element.ALIGN_BOTTOM );
            //c.setPadding( 2 );
            //t.addCell( c );

            // t.addCell( new Phrase( reportData.getSimName(), getFontLargeLightBold() ) );

            // Spacer
            c = new PdfPCell( new Phrase( "", fontXSmall ) );
            c.setColspan(cols);
            c.setFixedHeight( 2 );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            setRunDirection( c );
            touter.addCell( c );


            int colspan = 1;

            c = new PdfPCell();
            c.setPadding( 1 );
            setRunDirection(c);

            String rfHdrKey = "g.CT3RiskFactorsHdr";
            String rfFtrKey = "g.CT3RiskFactorsFtr";

            float spcg = 8;

            Chunk chnk = new Chunk( lmsg( "g.CT3RiskFactorsHdrTitle" ) + " " , this.getFontBoldRed() );

            Chunk chnk2 = new Chunk(  lmsg( rfHdrKey ) , getFontLight() );

            Phrase ph = new Phrase();

            ph.add( chnk );
            // ph.add( chnk2 );

            Paragraph cHdr = new Paragraph( ph );

            // Paragraph cHdr = new Paragraph( lmsg( rfHdrKey ) , getFontSmall() );
            cHdr.setSpacingBefore( spcg );
            cHdr.setSpacingAfter( 5 );
            cHdr.setLeading( 10 );
            Paragraph cFtr=new Paragraph( lmsg( rfFtrKey ) , getFontLight() );
            cFtr.setSpacingBefore( 4 );
            cFtr.setLeading( 10 );

            com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
            cl.setListSymbol( "\u2022");
            cl.setIndentationLeft( 10 );
            cl.setSymbolIndent( 10 );


            String rftxt;

            for( TextAndTitle tt : ct3RiskFactors )
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addReportRiskFactorSection() Text=" + tt.getText() + ", title=" + tt.getTitle()  + " flags=" + tt.getFlags() );

                if( tt.getText()==null || tt.getText().isEmpty() )
                    continue;

                rftxt = tt.getText();

                if( rftxt.indexOf( "[FACET]" ) >= 0 )
                    rftxt = rftxt.substring(0,rftxt.indexOf( "[FACET]" ) );

                rftxt = tctrans(rftxt,false);

                cl.add( new ListItem( 9,  rftxt, getFontLight() ) );
                // cl.add( new ListItem( new Phrase( tt.getText(), getFontLight() ) ) );
            }

            c.addElement( cHdr );
            c.addElement( cl );
            c.addElement( cFtr );


            colspan = cols;

            c.setColspan( colspan );

            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT  );

            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 0 );
            c.setPaddingTop( 6 );
            c.setPaddingRight( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingBottom( 10 );
            setRunDirection( c );
            touter.addCell( c );

            // float y = pageHeight - headerHgt - 4*PAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;

            touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addReportRiskFactorSection()" );

            throw e;
        }
    }


    public PdfPTable getScoreKeyTable(boolean showProfileKey)
    {
            // First, add a table
            PdfPTable t = new PdfPTable( reportData.getIsLTR() ?  new float[]{0.2f,0.8f} : new float[]{0.8f,0.2f} );

            // float importanceWidth = 25;

            t.setTotalWidth( 120 );
            t.setLockedWidth( true );
            setRunDirection( t );
            // t.setHeaderRows( 1 );


            PdfPCell c = t.getDefaultCell();
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );

            // First Row
            c = new PdfPCell( new Phrase( lmsg( "g.Key"), fontLightBold ) );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( 3 );
            c.setPaddingRight( 3 );
            c.setColspan( 2 );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell( c );

            // Pointer
            // First Row
            c = new PdfPCell( reportPointer );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( devel ? "g.YourScore" : "g.CandidateScore"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( keyRedBar );
            c.setPaddingLeft( 4 );
            c.setPaddingRight( 4 );
            c.setPaddingTop( 2 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( devel ? "g.NeedsImprovement" : "g.HigherRisk"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);


            if( devel )
            {
                c = new PdfPCell( keyYellowBar );
                c.setPaddingLeft( 4 );
                c.setPaddingRight( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBackgroundColor( ct2Colors.keyBackgroundColor );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg(  devel ? "g.Good" : "g.SomeRisk"), fontSmallLight ) );
                c.setPadding( 1 );
                c.setPaddingRight( 3 );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
                setRunDirection( c );
                t.addCell(c);
            }

            c = new PdfPCell( keyGreenBar );
            c.setPaddingLeft( 4 );
            c.setPaddingRight( 4 );
            if( !showProfileKey )
                c.setPaddingBottom( 5 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg(  devel ? "g.Solid" : "g.LowerRisk"), fontSmallLight ) );
            c.setPadding( 1 );
            c.setPaddingRight( 3 );
            if( !showProfileKey )
                c.setPaddingBottom( 5 );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
            setRunDirection( c );
            t.addCell(c);

            if( showProfileKey &&
                ( !devel || ( reportData.p != null && reportData.p.getStrParam3()!=null && !reportData.p.getStrParam3().isEmpty() ) ) )
            {
                c = new PdfPCell( keyBlueBar );
                c.setPaddingLeft( 4 );
                c.setPaddingRight( 4 );
                c.setPaddingTop( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBackgroundColor( ct2Colors.keyBackgroundColor );
                c.setPaddingBottom( 5 );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg( "g.CustomProfile"), fontSmallLight ) );
                c.setPadding( 1 );
                c.setPaddingRight( 3 );
                c.setPaddingBottom( 5 );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBackgroundColor( ct2Colors.keyBackgroundColor);
                setRunDirection( c );
                t.addCell(c);
            }

            //c = new PdfPCell( new Phrase( " \n ", fontSmallLight ) );
            //c.setPadding( 0 );
            //c.setColspan(2);
            //c.setPaddingBottom( 2 );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setBackgroundColor( BaseColor.WHITE );
           // setRunDirection( c );
            //t.addCell(c);

            t.setTableEvent(new TableBackgroundTableEvent(ct2Colors.keyBackgroundColor));

            return t;
    }




    public static void adjustScrCatListForDevel( java.util.List<ScoreCategoryRange> scrLst, boolean devel )
    {
        //if( 1==1 )
        //    return;

        if( !devel )
            return;

        // get the current values.
        float[] ry= new float[2];
        float[] yg = new float[2];

        for( ScoreCategoryRange scr : scrLst )
        {
            if( scr.getScoreCategoryType().equals( ScoreCategoryType.REDYELLOW ) )
            {
                ry[0] = scr.getMin();
                ry[1] = scr.getMax();
                scr.setMax( scr.getMin() );
            }

            if( scr.getScoreCategoryType().equals( ScoreCategoryType.YELLOWGREEN ) )
            {
                yg[0] = scr.getMin();
                yg[1] = scr.getMax();
                scr.setMax( scr.getMin() );
            }
        }

        // LogService.logIt( "BaseCT2ReportTemplate.adjustScrCatListForDevel() ry=" + ry[0] + "," + ry[1] + ", yg=" + yg[0] + "," + yg[1] );


        for( ScoreCategoryRange scr : scrLst )
        {

            // red stays same
            if( scr.getScoreCategoryType().equals( ScoreCategoryType.RED ) )
            {
                // Keep Same
                //if( ry[0]>0 && ry[1]>ry[0] )
                //    scr.setMax( scr.getMax() + (ry[1]-ry[0])/2 );

                // LogService.logIt( "BaseCT2ReportTemplate.adjustScrCatListForDevel() new red max=" + scr.getMax() );

            }

            // Yellow gains both old red-yellow
            if( scr.getScoreCategoryType().equals( ScoreCategoryType.YELLOW ) )
            {
                //if( ry[0]>0 && ry[1]>ry[0] )
                //    scr.setMin( scr.getMin() - (ry[1]-ry[0])/2 );

                //if( yg[0]>0 && yg[1]>yg[0] )
                //    scr.setMax( scr.getMax() + (yg[1]-yg[0])/2 );

                if( ry[0]>0 && ry[0]<scr.getMin() ) // && ry[1]>ry[0] )
                    scr.setMin( ry[0] ); // scr.getMin() - (ry[1]-ry[0])/2 );

                // if( yg[1]>0 && yg[1]>scr.getMax() ) //  && yg[1]>yg[0] )
                //     scr.setMax( yg[1] ); // scr.getMax() + (yg[1]-yg[0])/2 );


                // LogService.logIt( "BaseCT2ReportTemplate.adjustScrCatListForDevel() new yellow min=" + scr.getMin() + ", yellow max=" + scr.getMax() );
            }

            // Green gains the old yellow-green
            if( scr.getScoreCategoryType().equals( ScoreCategoryType.GREEN ) )
            {
                if( yg[0]>0 && yg[0]<scr.getMin() ) //  && yg[1]>yg[0] )
                    scr.setMin( yg[0] ); // scr.getMax() + (yg[1]-yg[0])/2 );
                //if( yg[0]>0 && yg[1]>yg[0] )
                //    scr.setMin( scr.getMin() - (yg[1]-yg[0])/2 );

                // LogService.logIt( "BaseCT2ReportTemplate.adjustScrCatListForDevel() new green min=" + scr.getMin() );
            }
        }
    }




    public void addCompetencySummaryChart() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseCT2ReportTemplate.addCompetencySummaryChart() Using locale: " + reportData.getLocale().toString() + ", g.AssessmentOverview=" + lmsg( "g.AssessmentOverview" ) );

            // If no info to present.
            if( reportData.getReportRuleAsBoolean("cmptysumoff") ||
                reportData.getReport().getIncludeCompetencyScores()!=1 ||
                ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
                  reportData.getReport().getIncludeSubcategoryNumeric()!=1 &&
                  reportData.getReport().getIncludeCompetencyColorScores()!=1 )  )
                  return;

            // int totalRows = 1;
            boolean usesPercentCorrectScoring = false;

            java.util.List<TestEventScore> teslC1 = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.CUSTOM );
            teslC1.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.CUSTOM_COMBO ) );
            Collections.sort(teslC1, new DisplayOrderComparator() );  // new TESNameComparator() );

            java.util.List<TestEventScore> teslC2 = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.CUSTOM2 );
            Collections.sort(teslC2, new DisplayOrderComparator() );  // new TESNameComparator() );

            java.util.List<TestEventScore> teslC3 = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.CUSTOM3 );
            Collections.sort(teslC3, new DisplayOrderComparator() );  // new TESNameComparator() );

            java.util.List<TestEventScore> teslC4 = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.CUSTOM4 );
            Collections.sort(teslC4, new DisplayOrderComparator() );  // new TESNameComparator() );

            java.util.List<TestEventScore> teslC5 = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.CUSTOM5 );
            Collections.sort(teslC5, new DisplayOrderComparator() );  // new TESNameComparator() );

            java.util.List<TestEventScore> teslA = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.ABILITY );
            teslA.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.ABILITY_COMBO ) );
            Collections.sort(teslA, new DisplayOrderComparator() );  // new TESNameComparator() );

            // Scored interests
            java.util.List<TestEventScore> teslI = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDINTEREST );
            teslI.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.INTERESTS_COMBO ) );
            // Collections.sort(teslP, new TESNameComparator());

            // Must resort since added a few groups
            Collections.sort(teslI, new DisplayOrderComparator() );  // new TESNameComparator() );

            java.util.List<TestEventScore> teslK = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.KNOWLEDGE );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.CORESKILL ) );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDESSAY ) );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDAUDIO ) );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDAVUPLOAD ) );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDTYPING ) );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDDATAENTRY ) );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDCHAT ) );
            teslK.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SKILL_COMBO ) );

            // Must resort since added a few groups
            Collections.sort(teslK, new DisplayOrderComparator() );  // new TESNameComparator());

            for( TestEventScore tes : teslA )
            {
                if( tes.getUsesPercentCorrectScoring() )
                {
                    usesPercentCorrectScoring = true;
                    break;
                }
            }

            for( TestEventScore tes : teslK )
            {
                if( tes.getUsesPercentCorrectScoring() )
                {
                    usesPercentCorrectScoring = true;
                    break;
                }
            }

            java.util.List<TestEventScore> teslP = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOGNITIVE );
            teslP.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOG_COMBO ) );
            // Collections.sort(teslP, new TESNameComparator());

            // Must resort since added a few groups
            Collections.sort(teslP, new DisplayOrderComparator() );  // new TESNameComparator() );

            java.util.List<TestEventScore> teslB = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDBIODATA );
            Collections.sort(teslB, new DisplayOrderComparator() );  // new TESNameComparator());

            java.util.List<TestEventScore> teslE = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.EQ );
            Collections.sort(teslE, new DisplayOrderComparator() );  // new TESNameComparator());

            java.util.List<TestEventScore> teslAI = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.VOICE_PERFORMANCE_INDEX );
            Collections.sort(teslAI, new DisplayOrderComparator() );  // new TESNameComparator());


            int totalComps = teslA.size() + teslI.size() + teslK.size() + teslP.size() + teslB.size() + teslE.size() + teslAI.size() + teslC1.size() +  + teslC2.size() + teslC3.size() + teslC4.size() + teslC5.size();

            if( teslA.isEmpty() && teslI.isEmpty() && teslK.isEmpty() && teslP.isEmpty() && teslB.isEmpty() && teslE.isEmpty() && teslAI.isEmpty() && teslC1.isEmpty() && teslC2.isEmpty() && teslC3.isEmpty() && teslC4.isEmpty() && teslC5.isEmpty() )
            {
                LogService.logIt( "BaseCT2ReportTemplate.addCompetencySummaryChart() No Competencies found to include in Summary Chart. Not including chart at all" );
                return;
            }

            boolean hasSpectrum = hasSpectrumPresentation( teslA );
            if( !hasSpectrum )
                hasSpectrum = hasSpectrumPresentation( teslI );
            if( !hasSpectrum )
                hasSpectrum = hasSpectrumPresentation( teslK );
            if( !hasSpectrum )
                hasSpectrum = hasSpectrumPresentation( teslP );
            if( !hasSpectrum )
                hasSpectrum = hasSpectrumPresentation( teslB );
            if( !hasSpectrum )
                hasSpectrum = hasSpectrumPresentation( teslE );
            if( !hasSpectrum )
                hasSpectrum = hasSpectrumPresentation( teslAI );

            boolean hasCefr = hasCefrScore( teslA );
            if( !hasCefr )
                hasCefr = hasCefrScore( teslK );
            //if( !hasCefr )
            //    hasCefr = hasCefrScore( teslP );
            //if( !hasCefr )
            //    hasCefr = hasCefrScore( teslB );
            //if( !hasCefr )
            //    hasCefr = hasCefrScore( teslE );
            //if( !hasCefr )
            //    hasCefr = hasCefrScore( teslAI );

            int cols = 4;
            float[] colRelWids = reportData.getIsLTR() ? new float[] { .4f, .1f, .25f, .25f} : new float[] { .25f, .25f, .1f, .4f};

            ScoreFormatType sft = reportData.getTestEvent().getScoreFormatType();

            boolean includeNumScores = reportData.getReport().getIncludeSubcategoryNumeric()==1 && !reportData.getReportRuleAsBoolean("cmptynumoff");
            boolean includeStars = reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean includeColorGraph = sft.getSupportsBarGraphic(reportData.getReport()) && reportData.getReport().getIncludeCompetencyColorScores()==1 && !reportData.getReportRuleAsBoolean("cmptygrphoff");

            // if( sft!=null && !sft.getSupportsBarGraphic(reportData.getReport()) )
            //     includeColorGraph = false;



            if( !includeNumScores && !hasCefr && !includeStars && !includeColorGraph )
            {
                // includeStars = false;
                cols -= 3;
                colRelWids = reportData.getIsLTR() ?  new float[] { .45f } : new float[] { .45f };
            }

            // Scores only.
            else if( !includeStars && !includeColorGraph )
            {
                // includeStars = false;
                cols -= 2;
                colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f } : new float[] { .2f, .45f };
            }


            // category only
            else if( !includeNumScores && !hasCefr && !includeColorGraph )
            {
                // includeStars = false;
                cols -= 2;
                colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f } : new float[] { .2f, .45f };
            }

            // Color graph only
            else if( !includeNumScores && !hasCefr && !includeStars )
            {
                // includeStars = false;
                cols -= 2;

                if( hasSpectrum )
                {
                    cols += 2;
                    colRelWids = reportData.getIsLTR() ?  new float[] { .3f, 0.2f, .3f, 0.2f } : new float[] { 0.2f, .3f, 0.2f, .3f };
                }
                else
                    colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .35f } : new float[] { .35f, .45f };
            }

            // color graph and scores
            else if( !includeStars )
            {
                // includeStars = false;
                cols -= 1;
                if( hasSpectrum )
                {
                    cols +=2;
                    colRelWids = reportData.getIsLTR() ?  new float[] { .35f, .15f, 0.17f, .46f, 0.17f} :  new float[] { .17f, 0.46f, 0.17f, .15f, .35f};
                }
                else
                    colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f, .45f} :  new float[] { .45f, .2f, .45f};
            }

            // scores and categories
            else if( !includeColorGraph )
            {
                // includeStars = false;
                cols -= 1;
                colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f, .35f} :  new float[] { .35f, .2f, .45f};
            }

            // color graph and categories
            else if( !includeNumScores && !hasCefr )
            {
                // includeStars = false;
                cols -= 1;
                if( hasSpectrum )
                {
                    cols +=2;
                    colRelWids = reportData.getIsLTR() ?  new float[] { .35f, .15f, 0.17f, .46f, 0.17f} :  new float[] { .17f, 0.46f, 0.17f, .15f, .35f};
                    // colRelWids = reportData.getIsLTR() ?  new float[] { .35f, .15f, 0.15f, .5f, 0.15f} :  new float[] { .15f, 0.5f, 0.15f, .15f, .35f};
                    //colRelWids = reportData.getIsLTR() ?  new float[] { .3f, .15f, 0.15f, .3f, 0.15f} :  new float[] { .15f, 0.3f, 0.15f, .15f, .3f};
                }
                else
                    colRelWids = reportData.getIsLTR() ?  new float[] { .45f, .2f, .45f} :  new float[] { .45f, .2f, .45f};
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addCompetencySummaryChart() Using locale: " + reportData.getLocale().toString() + ", cols=" + cols + ", includeNumScores=" + includeNumScores + ", includeColorGraph=" +  includeColorGraph + ", includeStars=" + includeStars );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( cols );

            float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            setRunDirection( t );


            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            String ttext = lmsg( "g.Competency");
            String tval = reportData.getReportRuleAsString( "competencycolumntitle" );
            if( tval!=null && !tval.isBlank() )
                ttext = tval;

            // Create header
            c = new PdfPCell( new Phrase( ttext, fontLargeWhite ) );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, false) );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            if( includeNumScores || hasCefr )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.Score"), fontLargeWhite ) );
                c.setColspan( 1 );

                //if( includeStars )
                //    c.setBorder( Rectangle.TOP );
                //else
                if( cols==2 )
                {
                    //c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.TOP : Rectangle.LEFT | Rectangle.TOP );
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, false, false) );

                } // | Rectangle.RIGHT );
                else
                {
                    //c.setBorder( Rectangle.TOP );
                    c.setBackgroundColor( ct2Colors.hraBlue );
                } // | Rectangle.RIGHT );

                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                setRunDirection( c );
                t.addCell(c);

            }

            if( includeColorGraph )
            {
                // Low end Text
                if( hasSpectrum )
                {
                    c = new PdfPCell( new Phrase( "" ) );
                    c.setColspan( 1 );
                    // c.setBorder( Rectangle.TOP );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);
                }

                c = new PdfPCell( new Phrase( lmsg( "g.Interpretation" ), fontLargeWhite ) );
                c.setColspan( 1 );
                c.setBorder( Rectangle.NO_BORDER );

                if( includeStars )
                {
                    // c.setBorder( Rectangle.TOP );
                    c.setBackgroundColor( ct2Colors.hraBlue );
                }
                else
                {
                    if( hasSpectrum )
                    {
                        // c.setBorder( Rectangle.TOP );
                        c.setBackgroundColor( ct2Colors.hraBlue );
                    }
                    else
                    {
                        // c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                        c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, false, false) );
                    }
                }

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                setRunDirection( c );
                t.addCell(c);
                //}

                // High end text
                if( hasSpectrum )
                {
                    c = new PdfPCell( new Phrase( "" ) );
                    c.setColspan( 1 );
                    if( includeStars )
                    {
                        //c.setBorder( Rectangle.TOP );
                        c.setBackgroundColor( ct2Colors.hraBlue );
                    }
                    else
                    {
                        //c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                        c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, false, false) );
                    }
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    setRunDirection( c );
                    t.addCell(c);
                }

            }

            if( includeStars )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.JobMatch"), this.fontLargeWhite ) );
                // c.setBorder(reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, false, false) );
                setRunDirection( c );
                t.addCell(c);
            }


            previousYLevel =  currentYLevel;

            ttext = lmsg( "g.CompetencySummary");
            tval = reportData.getReportRuleAsString( "competencysummarytitle" );
            if( tval!=null && !tval.isBlank() )
                ttext = tval;

            float titleHeight = 0;

            if( (ttext!=null && !ttext.isBlank()) )
            {
                titleHeight = this.getTitleHeight(ttext, null);

                // if need a new page just for title, do so.
                if( previousYLevel>0 )
                {
                    float ulY = previousYLevel - PAD - titleHeight;

                    if( ulY < footerHgt + 3*PAD )
                    {
                        document.newPage();
                        currentYLevel = pageHeight - PAD -  headerHgt;
                        previousYLevel = currentYLevel;
                    }
                }
            }

            boolean isLast;

            if( !teslC1.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslC2.isEmpty() && teslC3.isEmpty() && teslC4.isEmpty() && teslC5.isEmpty() && teslK.isEmpty() && teslI.isEmpty() && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.CUSTOM.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslC1, "g.CustomTitle1", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
            }

            if( !teslC2.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslC3.isEmpty() && teslC4.isEmpty() && teslC5.isEmpty() && teslK.isEmpty() && teslI.isEmpty() && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.CUSTOM2.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslC2, "g.CustomTitle2", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
            }

            if( !teslC3.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslC4.isEmpty() && teslC5.isEmpty() && teslK.isEmpty() && teslI.isEmpty() && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.CUSTOM3.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslC3, "g.CustomTitle3", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
            }

            if( !teslC4.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslC5.isEmpty() && teslK.isEmpty() && teslI.isEmpty() && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.CUSTOM4.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslC4, "g.CustomTitle4", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
            }

            if( !teslC5.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslK.isEmpty() && teslI.isEmpty() && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.CUSTOM5.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslC5, "g.CustomTitle5", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
            }

            if( !teslA.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslK.isEmpty() && teslI.isEmpty() && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslA, "g.AbilitiesCognitive", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
                //totalRows += teslA.size() + 1;
            }

            if( !teslK.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslI.isEmpty() && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslK, "g.KnowledgeAndSkills", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false);
                //totalRows += teslK.size() + 1;
            }

            if( !teslI.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslP.isEmpty() && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.INTERESTS.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslI, "g.Interests", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
                //totalRows += teslA.size() + 1;
            }

            if( !teslP.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslE.isEmpty() && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslP, devel ? "g.PersonalityCharsDevel" : "g.PersonalityChars", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast, false, false );
                //totalRows += teslP.size() + 1;
            }

            if( !teslE.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring && teslB.isEmpty() && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslE, "g.EQChars", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast , false, false );
                //totalRows += teslP.size() + 1;
            }

            if( !teslB.isEmpty() && reportData.includeBiodataInfo() )
            {
                String titleKey = "g.BehavioralHistory";

                if( reportData.getReportRuleAsBoolean( "biodataisscoredsurvey" ) )
                    titleKey = "g.BehavioralHistoryScoredSurvey";

                isLast = !usesPercentCorrectScoring && teslAI.isEmpty();
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslB, titleKey, tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars,  isLast, true, false );
                //totalRows += teslB.size() + 1;
            }

            if( !teslAI.isEmpty() )
            {
                isLast = !usesPercentCorrectScoring;
                tval = reportData.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId() );
                addCompetencySummaryChartSection(t, teslAI, "g.AIChars", tval, includeNumScores, hasCefr, false, includeColorGraph, hasSpectrum, includeStars, isLast , false, false );
                //totalRows += teslP.size() + 1;
            }

            if( usesPercentCorrectScoring )
            {
                c = new PdfPCell( new Phrase( "* " + lmsg("g.CompetencyUsesPercentCorrectScoring") , fontLight ) );
                c.setColspan(cols);
                c.setPadding( 1 );
                c.setPaddingBottom( 3);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                t.addCell( c );
            }


            /*
            // Next, add the key
            c = new PdfPCell( colorChartKey );
            c.setColspan( cols );
            c.setPaddingLeft( 10 );
            c.setPaddingRight( 16 );
            c.setBorder( 0 );
            c.setPaddingTop( 2 );
            c.setPaddingBottom( 2 );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            t.addCell( c );
            */

            float thgt = t.calculateHeights();
            if( thgt> pageHeight )
                t.setHeaderRows( 1 );

            //else
            //{
            float ulY = previousYLevel - 2*PAD - thgt - titleHeight;

            // both won't fit. Add a new page, then add title
            if( ulY < footerHgt + 3*PAD )
            {
                // see if title plus header plus first row is too much
                //ulY = previousYLevel - 2*PAD - titleHeight - getHeaderPlusFirstRowHeight( t );

                //if( ulY < footerHgt + 3*PAD )
                //{
                document.newPage();
                currentYLevel = pageHeight - PAD -  headerHgt;
                previousYLevel = currentYLevel;
                //}

                // otherwise we should be OK.
            }
            //}

            float y = addTitle(previousYLevel, ttext, competencySummaryStr, null, null );

            y -= TPAD;  // getHraLogoBlackText().getScaledHeight() - fnt.getSize()*2f;


            float yTemp = addTableToDocument(y, t, false, true );
            // t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );


            float twid = t.getTotalWidth();

            if( totalComps > 8 && reportData.getIsLTR() )
            {
                String importance = lmsg("g.Importance");
                String importance2J = lmsg("g.ImportanceToJob");
                float importWid = ITextUtils.getTextWidth( importance, baseFontCalibri, 9 );
                float import2JobWid = ITextUtils.getTextWidth( importance2J, baseFontCalibri, 9 );
                float importArrowHgt = 25;
                float importArrowWid = 6;

                if( importWid + PAD + importArrowHgt > thgt )
                {
                    importArrowHgt = 12;

                    if( importWid + PAD + importArrowHgt > thgt )
                        importArrowHgt = 6;
                }

                else if( import2JobWid + PAD + importArrowHgt <= thgt )
                {
                    importance = importance2J;
                    importWid = import2JobWid;
                }

                // LogService.logIt( "BaseCT2ReportTemplate.addCompetencySummaryChart() importWid=" + importWid );

                if( thgt > importWid + importArrowHgt - 10 )// 150 )
                {
                    float importX = CT2_MARGIN + twid + importanceWidth + PAD;
                    float importY = y - thgt/2 - PAD/2 - importArrowHgt/2;

                    float importArrowCtrX = importX + importArrowWid/2;
                    float importArrowBotY = importY + importWid/2 + PAD;
                    float importArrowTopY = importArrowBotY + importArrowHgt;

                    PdfContentByte pcb = pdfWriter.getDirectContent();
                    pcb.saveState();

                    pcb.setColorStroke( ct2Colors.lightergray );
                    pcb.setColorFill( ct2Colors.lightergray );

                    pcb.beginText();

                    pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );

                    pcb.setFontAndSize(baseFontCalibri, 9);
                    pcb.showTextAligned(Element.ALIGN_CENTER, importance, importX+5, importY, this.getIsRTL() ? 270 : 90);
                    pcb.endText();
                    // pcb.fill();

                    pcb.setLineWidth( 0.5f );
                    pcb.moveTo(importArrowCtrX, importArrowBotY);
                    pcb.lineTo(importArrowCtrX, importArrowTopY );
                    pcb.stroke();

                    pcb.setColorFill( ct2Colors.lightergray );
                    pcb.newPath();
                    pcb.moveTo(importArrowCtrX, importArrowTopY );
                    pcb.lineTo(importArrowCtrX - importArrowWid/2, importArrowTopY-5 );
                    pcb.lineTo(importArrowCtrX + importArrowWid/2, importArrowTopY-5 );
                    pcb.closePath();
                    pcb.fill();

                    pcb.restoreState();
                }
            }

            y = yTemp;

            // currentYLevel = y - t.calculateHeights();
            currentYLevel = y - 2*PAD;

            if( devel )
                currentYLevel = currentYLevel - 10;


            previousYLevel =  currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addCompetencySummaryChart()" );
            throw new STException( e );
        }
    }

    public boolean hasSpectrumPresentation( List<TestEventScore> tesl )
    {
        for( TestEventScore t : tesl )
        {
            if( t.getReportFileContentTypeId()==ScorePresentationType.SPECTRUM.getScorePresentationTypeId() )
                 return true;
        }
        return false;
    }


    public boolean hasCefrScore( List<TestEventScore> tesl )
    {
        String v;
        for( TestEventScore t : tesl )
        {
            if( !t.getTestEventScoreType().getIsCompetency() )
                continue;
            v = StringUtils.getBracketedArtifactFromString(t.getTextParam1(), Constants.CEFRLEVEL);

            if( v!=null && !v.isBlank() && !CefrScoreType.getFromText(v).equals(CefrScoreType.UNKNOWN) )
                return true;
        }
        return false;
    }

    public void addCompetencySummaryChartSection(   PdfPTable t, java.util.List<TestEventScore> tesl, String ttlKey, String titleText, boolean includeNumScores, boolean hasCefr, boolean incCompetencyDescrips, boolean includeColorGraph, boolean hasSpectrum, boolean includeStars, boolean last, boolean showRedRange, boolean useTesNameOnly) throws Exception
    {
         // Collections.sort( tesl, new TESNameComparator() );

         PdfPCell c;
         PdfPCell c2;

         int cols = 1 + (includeNumScores || hasCefr ? 1 : 0 ) + (includeColorGraph ? 1 : 0 ) + (includeStars ? 1 : 0 ) + (hasSpectrum ? 2 : 0);

         if( titleText!=null && !titleText.isBlank() && ( titleText.equals("[HIDE]") || titleText.equals("[HIDESUM]") ) )
         {
             titleText=null;
             ttlKey=null;
         }

         if( !devel && ( (ttlKey!=null && !ttlKey.isBlank()) || (titleText!=null && !titleText.isBlank())) )
         {
             c = new PdfPCell( new Phrase( titleText!=null && !titleText.isBlank() ? titleText : lmsg( ttlKey ), fontXSmallBold ) );
            c.setColspan( cols );
            c.setPadding( 1 );
            c.setPaddingBottom( 2 );
            c.setVerticalAlignment( Element.ALIGN_MIDDLE );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBackgroundColor(  ct2Colors.scoreBoxShadeBgColor );
            setRunDirection( c );
            t.addCell( c );
         }

         TestEventScore tes;

         ScoreCategoryType sct;
         Image scrCatImg;

         boolean useSolidBarGraphs = includeColorGraph &&
                                     reportData.getReportRuleAsBoolean( "competencycoloriconasgraph" ); // && !reportData.getReportRule( "competencycoloriconasgraph" ).equals( "0");

         boolean useScoreTextAsNumScore = reportData.getReportRuleAsBoolean( "cmptyscrtxtasnum" );

         PdfPTable graphT;


         int scrDigits;

         String scrValue;

         ListIterator<TestEventScore> iter = tesl.listIterator();

         // PdfContentByte directContent = pdfWriter.getDirectContent();
         //PdfTemplate markerTemplate = directContent.createTemplate( summaryCatNumericMarker.getWidth(), summaryCatNumericMarker.getHeight() );

         //markerTemplate.addImage( summaryCatNumericMarker );
         String descrip;
         // Paragraph par;

         String cns;
         String cname;

         boolean shwGrph;

         String[] spectrum;

         CefrScoreType cefrScoreType;

         while( iter.hasNext() )
         {
             tes = iter.next();

             if( tes.getIncludeNumericScoreInResults() && reportData.hasProfile() )
                 tes.setProfileBoundaries(reportData.getProfileEntryData(tes.getName(), tes.getNameEnglish() ) );

             cefrScoreType = null;
             if( hasCefr )
             {
                 cefrScoreType = CefrScoreType.getFromText( StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRLEVEL));
                 if( cefrScoreType.equals(CefrScoreType.UNKNOWN ) )
                     cefrScoreType = null;
             }

             sct = tes.getScoreCategoryType();
             scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();

             // this is an extra check for the situation where we are presenting a scored survey competency. In some cases we include a graph (percentage scoring, or norm scoring) but in others we do not (total points)
             shwGrph = showGraphForTes( tes );

             scrCatImg = getScoreCategoryImg( sct, true );

             descrip=null;

             if( incCompetencyDescrips && reportData.equivSimJUtils!=null )
             {
                 descrip = reportData.equivSimJUtils.getCompetencyDescription(tes);
                 if( descrip!=null && descrip.trim().isEmpty() )
                     descrip=null;
             }

             cns = reportData.getCompetencyName(tes); //  ReportUtils.getCompetencyNameToUseInReporting( reportData.te, tes, reportData.te.getSimXmlObj(), reportData.te.getProduct(), reportData.getLocale() );

             cname = useTesNameOnly ? cns : tctrans( cns, false);
             // cname = useTesNameOnly ? cns : tctrans( reportData.getCompetencyName(tes), false);

             if( cname==null )
                 cname = "";

             cname = cname.trim();

             if( tes.getUsesPercentCorrectScoring() )
                 cname += " *";

             // c = new PdfPCell( new Phrase( "  " + tctrans( reportData.getCompetencyName(tes), false) , fontLight ) );
             // c = new PdfPCell( new Phrase( "  " + ( useTesNameOnly ? tes.getName() : tctrans( reportData.getCompetencyName(tes), false) ) , fontLight ) );

             // ROW 1
             c = new PdfPCell( new Phrase( "  " + cname + (cefrScoreType==null ? "" : "\n     - " + lmsg("g.CefrEquivScore")) , fontLight ) );
             c.setPadding( 1 );
             c.setHorizontalAlignment( Element.ALIGN_LEFT );
             c.setVerticalAlignment( Element.ALIGN_MIDDLE );

             if( !iter.hasNext() && last && descrip==null)
                 c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );

             else
                c.setBorder( reportData.getIsLTR() ?  Rectangle.LEFT : Rectangle.RIGHT );

             c.setBorderColor( ct2Colors.scoreBoxBorderColor );
             c.setBorderWidth( scoreBoxBorderWidth );
             setRunDirection( c );
             t.addCell( c );

             c = t.getDefaultCell();
             c.setHorizontalAlignment( Element.ALIGN_RIGHT );
             c.setVerticalAlignment( Element.ALIGN_MIDDLE );
             c.setPadding(1);
             setRunDirection( c );


            if( includeNumScores || cefrScoreType!=null )
            {
                if( includeNumScores && tes.getIncludeNumericScoreInResults() )
                {
                    scrValue = includeNumScores ? I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), scrDigits ) : "";

                    if( useScoreTextAsNumScore )
                    {
                        scrValue = reportData.getCompetencyScoreText( tes ); // tes.getScoreText();

                        scrValue = ReportUtils.getScoreValueFromStr(scrValue);

                        if( scrValue == null )
                            scrValue = "";
                    }

                    c = new PdfPCell( new Phrase( scrValue + (cefrScoreType==null ? "" : "\n" + cefrScoreType.getName(reportData.getLocale())), fontLight ) );
                }

                else
                    c = new PdfPCell( new Phrase( "-" + (cefrScoreType==null ? "" : "\n" + cefrScoreType.getName(reportData.getLocale())), fontLight ) );

                if( cols==2 )
                {
                     c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
                }
                // c = new PdfPCell( new Phrase( Double.toString( NumberUtils.roundIt( tes.getScore(), 1) ), fontLight ) );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding(1);
                c.setPaddingRight( 8 );
                setRunDirection( c );

                boolean isBottomRow = !iter.hasNext() && last && descrip==null;

                if( cols==2 )
                {
                    if( isBottomRow )
                        c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT  : Rectangle.LEFT );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                }
                else if( isBottomRow )
                {
                    c.setBorder( Rectangle.BOTTOM );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                }

                // Last row
               // if( !iter.hasNext() && last && descrip==null )
               // {
                //    c.setBorder( Rectangle.BOTTOM );
                //    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //    c.setBorderWidth( scoreBoxBorderWidth );
                //}
                else
                    c.setBorder( Rectangle.NO_BORDER );

                t.addCell( c );
            }

            if( includeColorGraph )
            {
                spectrum = hasSpectrum ? ReportData.getSpectrumVals( tes ) : null;
                if( hasSpectrum )
                {
                    c = new PdfPCell( new Phrase(spectrum[0], this.fontLightItalic) ); // new PdfPCell( summaryCatNumericAxis );
                    c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( 1 );
                    //c.setPaddingTop( 8 );
                    c.setPaddingRight(1);

                    if( !iter.hasNext() && last && descrip==null )
                        c.setBorder( Rectangle.BOTTOM );
                    else
                        c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    setRunDirection( c );

                    // c.setBackgroundColor( BaseColor.ORANGE);

                    t.addCell( c );
                }

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                // c.setPadding(8);

                c.setPaddingTop( 0 );
                c.setPaddingBottom( 2 );
                setRunDirection( c );

                if( tes.getIncludeNumericScoreInResults() )
                {
                    // c.setFixedHeight(16);

                    if( shwGrph )
                    {
                        graphT = new PdfPTable( 1 );
                        graphT.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
                        c2.setBorder( Rectangle.NO_BORDER );
                        c2.setColspan( 2 );
                        c2.setPadding( 2 );
                        c2.setPaddingTop(0);
                        c2.setVerticalAlignment( Element.ALIGN_TOP );
                        c2.setFixedHeight(reportData.getTestEvent().getUseBellGraphs() ? BELL_GRAPH_CELL_HEIGHT : BAR_GRAPH_CELL_HEIGHT);
                        c2.setCellEvent( useSolidBarGraphs ? new CT2SolidBarGraphicCellEvent( tes , reportData.getR2Use(), sct, reportData.getTestEvent().getScoreColorSchemeType(), reportData.p, ct2Colors, devel ) :
                                        new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, showRedRange, ct2Colors, devel, false, reportData.getTestEvent().getUseBellGraphs(), true, 1 ) );
                        graphT.addCell(c2);

                        // c.setBackgroundColor( BaseColor.LIGHT_GRAY);

                        // setRunDirection( c );
                        c.addElement( graphT );

                        //c.setCellEvent( useSolidBarGraphs ? new CT2SolidBarGraphicCellEvent( tes , reportData.getR2Use(), sct, reportData.p, ct2Colors, devel ) :
                        //                new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, baseFontCalibri, showRedRange, ct2Colors, devel, false, true ) );
                    }
                }

                if( !hasSpectrum && !includeStars )
                {
                    if( !iter.hasNext() && last && descrip==null )
                        c.setBorder( reportData.getIsLTR() ?  Rectangle.BOTTOM | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.LEFT );
                    else
                        c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT );
                }

                else if( !iter.hasNext() && last && descrip==null )
                    c.setBorder( Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.NO_BORDER );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                t.addCell( c );

                if( hasSpectrum )
                {
                    c = new PdfPCell( new Phrase(spectrum[1], fontLightItalic) ); // new PdfPCell( summaryCatNumericAxis );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setPadding( 1 );
                    //c.setPaddingTop( 8 );
                    c.setPaddingLeft( 1 );

                    if( !includeStars )
                    {
                        if( !iter.hasNext() && last && descrip==null )
                            c.setBorder( reportData.getIsLTR() ?  Rectangle.BOTTOM | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.LEFT );
                        else
                            c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT );
                    }

                    else if( !iter.hasNext() && last && descrip==null )
                        c.setBorder( Rectangle.BOTTOM );
                    else
                        c.setBorder( Rectangle.NO_BORDER );

                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    setRunDirection( c );
                    t.addCell( c );
                }
            }

             if( includeStars )
             {
                c = scrCatImg==null ?  new PdfPCell( new Phrase("",font) ) : new PdfPCell( scrCatImg );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setPadding(1);
                c.setPaddingLeft( 30 );

                if( !iter.hasNext() && last && descrip==null )
                    c.setBorder( reportData.getIsLTR() ?  Rectangle.BOTTOM | Rectangle.RIGHT : Rectangle.BOTTOM | Rectangle.LEFT );
                else
                    c.setBorder( reportData.getIsLTR() ?  Rectangle.RIGHT : Rectangle.LEFT );

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                setRunDirection( c );
                t.addCell( c );
             }

             if( incCompetencyDescrips && reportData.equivSimJUtils!=null )
             {
                 descrip = reportData.equivSimJUtils.getCompetencyDescription(tes);

                if( descrip!=null && !descrip.trim().isEmpty() )
                {
                    descrip ="  (" + descrip + ")";

                    // c = new PdfPCell( new Phrase( "  " + tctrans( reportData.getCompetencyName(tes), false) , fontLight ) );
                    c = new PdfPCell( new Phrase( descrip, fontLight ) );
                    c.setColspan(cols);
                    c.setPadding( 1 );
                    c.setPaddingTop( 0 );
                    c.setPaddingBottom( 4 );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );

                    if( !iter.hasNext() && last )
                        c.setBorder( Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM );

                    else
                        c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    setRunDirection( c );
                    t.addCell( c );
                }
             }
         }
    }


    public boolean hasComparisonData()
    {
            if( reportData.getTestEvent().getOverallTestEventScore().getHasValidOverallNorm() || reportData.getTestEvent().getOverallTestEventScore().getHasValidOverallZScoreNorm())
                return true;

            if( reportData.getTestEvent().getOverallTestEventScore().getHasValidCountryNorm() )
                return true;

            if( reportData.getTestEvent().getOverallTestEventScore().getHasValidAccountNorm() )
                return true;

            return false;
    }


    /**
     * Used to hide interpretation when scored survey
     * @param tes
     * @return
     */
    public boolean showInterpretationForTes( TestEventScore tes )
    {
        return !tes.getSimCompetencyClass().isBiodata() || !reportData.getReportRuleAsBoolean( "biodataisscoredsurvey" );
    }

    public boolean showGraphForTes( TestEventScore tes )
    {
        SimCompetencyClass scc = tes.getSimCompetencyClass();

        if( scc.isUnscored() )
            return false;

        if( !scc.getIsBiodata() || !reportData.getReportRuleAsBoolean( "biodataisscoredsurvey" ) )
            return true;

        //need competency type - percent of total is ok.
        CompetencyScoreType cst = CompetencyScoreType.getValue( tes.getScoreTypeIdUsed() );

        // cold be scroed as percent of total. This is OK for graph.
        if( cst.isPercentOfTotal() )
            return true;

        // Could be normed - would have a STD. This is ok.
        if( tes.getStdDeviation()>0 )
            return true;

        return false;
    }

    
    public void addAiScoresSection() throws Exception
    {
        if( reportData.getReportRuleAsBoolean( "skipaiscoressection" ) )
            return;

        if( reportData.getReport().getIncludeAiScores()==0 )
            return;

        // next see if there are any metascores
        if( reportData.getTestKey().getMetaScoreList()==null || reportData.getTestKey().getMetaScoreList().isEmpty() )
            return;
        
        int valCount = 0;
        for( MetaScore ms : reportData.getTestKey().getMetaScoreList() )
        {
            if( ms.getMetaScoreTypeId()>0 && ms.getScore()>0 && ms.getConfidence()>= Constants.MIN_METASCORE_CONFIDENCE )
                valCount++;
        }
        
        if( valCount<=0 )
            return;
        
        try
        {
            if( ReportManager.DEBUG_REPORTS )
                LogService.logIt(  "BaseCT2ReportTemplate.addAiScoresSection() BBB.1 valCount=" + valCount );
            
            PdfPCell c;
            PdfPTable t;
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // First, add a table
            t = new PdfPTable( new float[] { 2,1,1,4  } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = new PdfPCell( new Phrase( lmsg("g.EstimatedValue"), fontLargeWhite ) );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, false, false, false ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( 1 );
            c.setPaddingLeft( 5 );
            c.setPaddingBottom( 5 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg("g.Score"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1);
            c.setPaddingBottom( 5 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg("g.Confidence"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingLeft( 5 );
            c.setPaddingBottom( 5 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg("g.Interpretation"), fontLargeWhite ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,false, true, false, false ) );
            setRunDirection( c );
            t.addCell(c);

            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            
            // start with gray for odd counts.
            boolean useGrayBg = valCount%2!=0;            
            
            Font fontToUse = font;
            Font smallFontToUse = fontSmall;
            int scrDigits = reportData.getReport().getIntParam2() >= 0 ? reportData.getReport().getIntParam2() : reportData.getTestEvent().getScorePrecisionDigits();
            String scr;
            String scoreText;
            
            int count = 0;
            MetaScoreType metaScoreType;
            String lastUpdate;
            Paragraph par;
            
            for( MetaScore metaScore : reportData.getTestKey().getMetaScoreList() )
            {
                if( metaScore.getMetaScoreTypeId()<=0 || metaScore.getScore()<=0 || metaScore.getConfidence()<Constants.MIN_METASCORE_CONFIDENCE )
                    continue;
                count++;
                
                metaScoreType = MetaScoreType.getValue(metaScore.getMetaScoreTypeId() );

                c = new PdfPCell(new Phrase( metaScoreType.getName(reportData.getLocale()), fontToUse));
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                c.setPaddingBottom( 5 );
                if( useGrayBg )
                    c.setBackgroundColor(graybg);
                if( count==valCount && useGrayBg )
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), graybg,false, false, false, true ) );
                setRunDirection( c );
                t.addCell(c);

                scr = I18nUtils.getFormattedNumber( reportData.getLocale(), metaScore.getScore(), scrDigits );                
                c = new PdfPCell(new Phrase( scr, fontToUse));
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                if( useGrayBg )
                    c.setBackgroundColor(graybg);
                setRunDirection( c );
                t.addCell(c);

                scr = I18nUtils.getFormattedNumber( reportData.getLocale(), metaScore.getConfidence(), 1 );                
                c = new PdfPCell(new Phrase( scr, fontToUse));
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding( 2 );
                c.setPaddingBottom( 5 );
                if( useGrayBg )
                    c.setBackgroundColor(graybg);
                setRunDirection( c );
                t.addCell(c);
                
                scoreText = metaScore.getScoreText();
                
                metaScore.setLocale(reportData.getLocale());
                c = new PdfPCell();
                par = new Paragraph(metaScoreType.getDescription(reportData.getLocale()) + " " + lmsg("g.AiMetaScrInputTypesUsed", new String[]{metaScore.getMetaScoreInputTypesStr()}), smallFontToUse);
                c.addElement(par);

                if( scoreText!=null && !scoreText.isBlank() )
                {
                    par = new Paragraph(scoreText, smallFontToUse);
                    c.addElement(par);
                }
                
                lastUpdate = I18nUtils.getFormattedDateTime(reportData.getLocale(), metaScore.getLastUpdate(), reportData.getTimeZone());
                par = new Paragraph( lmsg("g.AiMetaScrCalcDateX", new String[]{lastUpdate}), smallFontToUse);
                c.addElement(par);  
                
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                c.setPaddingBottom( 5 );
                
                if( useGrayBg )
                    c.setBackgroundColor(graybg);
                if( count==valCount && useGrayBg )
                    c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), graybg,false, false, true, false ) );
                setRunDirection( c );
                t.addCell(c);
                
                // toggle
                useGrayBg = !useGrayBg;
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addAiScoresSection() validCount=" + validCount + ", thgt=" + thgt + ", y=" + y );

            previousYLevel =  currentYLevel; // - TPAD;
            float y = previousYLevel;
            
            float thgt = t.calculateHeights();
            if( thgt + 80 > y )
            {
                addNewPage();

                y = currentYLevel;
            }

            y = addTitle(y, lmsg( "g.AiGenScores" ), lmsg("g.AiGenScoresSubtitle" ), null, null );

            y -= TPAD;
            
            currentYLevel = addTableToDocument(y, t, false, true );
            
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addAiScoresSection()" );
            throw new STException( e );
        }
            
    }

    public void addComparisonSection() throws Exception
    {
        try
        {
            if( reportData.getReportRuleAsBoolean( "skipcomparisonsection" ) )
                return;

            if( reportData.getReport().getIncludeNorms()==0 )
                return;

            // Be sure there's no rule to skip this section.
            //String hasRule1 = this.reportData.getReportRule( "skipcomparisonsection" );

            //if( hasRule1 != null && hasRule1.equals( "1" ) )
            //    return;

            TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();

            if( !otes.getHasValidNorms() && !otes.getHasValidOverallZScoreNorm() )
            {
                prepNotes.add( lmsg( "g.RptNote_NoNorms_InsufficientData" ) );

                if(  reportData.getReportRuleAsBoolean( "lowpctlcntwrngoff" ) )
                    return;
            }

            //if( reportData.getReport().getIncludeNorms()==0 || !otes.getHasValidNorms() )
            //     return;

            boolean includeCompanyInfo = !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );


            boolean orgInfoOk = !reportData.getReportRuleAsBoolean( "omitorgpdf" );

            String reportCompanyName = reportData.getReportCompanyName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                      ";

            if( reportCompanyName == null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( !includeCompanyInfo )
                reportCompanyName = lmsg( "g.Company" );


            String[] names = new String[] { lmsg( "g.OverallPerc" ), getCountryName( reportData.getTestEvent().getPercentileCountry()!=null && !reportData.getTestEvent().getPercentileCountry().isEmpty() ? reportData.getTestEvent().getPercentileCountry() : reportData.getTestEvent().getIpCountry() ), StringUtils.truncateString(reportCompanyName, 22 ) };

            float[] percentiles = new float[] { otes.getPercentile(), otes.getCountryPercentile(),otes.getAccountPercentile() };

            int[] counts = new int[] { otes.getOverallPercentileCount(), otes.getCountryPercentileCount(),otes.getAccountPercentileCount() };

            if( !otes.getHasValidOverallNorm() )
                prepNotes.add( lmsg( otes.getHasValidOverallZScoreNorm() ? "g.RptNote_NoOverallNorms_InsufficientDataZscoreNorm" : "g.RptNote_NoOverallNorms_InsufficientData" ) );

            if( !otes.getHasValidCountryNorm() )
                prepNotes.add( lmsg( "g.RptNote_NoCountryNorms_InsufficientData" ) );

            if( orgInfoOk && !otes.getHasValidAccountNorm() )
                prepNotes.add( lmsg( "g.RptNote_NoAccountNorms_InsufficientData" ) );

            prepNotes.add( lmsg( "g.RptNote_AboutNorms", new String[]{RuntimeConstants.getStringValue("baseadmindomain")} ) );

            int validCount = 0;

            if( otes.getHasValidOverallNorm() || otes.getHasValidOverallZScoreNorm() )
                validCount++;

            if( otes.getHasValidCountryNorm() )
                validCount++;

            if( orgInfoOk && otes.getHasValidAccountNorm() )
                validCount++;

            //if( 1==1 )
            //    validCount=3;

            //for( float p : percentiles )
            //{
            //    if( p>0 )
            //        validCount++;
            //}


            // no data to show.
            //if( validCount == 0 )
            //{
                // reportComparisonBoxY = reportSummaryBoxY;
            //    return;
            //}

            previousYLevel =  currentYLevel; // - TPAD;

            float y = previousYLevel;

            float thgt =0;// t!= null  t.calculateHeights();

            PdfPTable t = null;
            PdfPCell c;

            // First, add a table
            t = new PdfPTable( 3 );
            setRunDirection( t );

            // float extraMargin = 25;
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( reportData.getIsLTR() ? new float[] {22,13,72} : new float[] {72,13,22} );
            t.setLockedWidth( true );

            if( validCount > 0 )
            {
                int rowCount = 0;

                // Now create the graph
                // First create the table

                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );

                // Create header
                c = new PdfPCell( new Phrase( lmsg("g.TesterGroup"), fontLargeWhite ) );
                // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT :Rectangle.TOP | Rectangle.RIGHT  );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan( 1 );
                c.setPadding( 1 );
                c.setPaddingBottom( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                // c.setBackgroundColor(  ct2Colors.hraBlue );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg("g.Percentile"), fontLargeWhite ) );
                c.setBorder( Rectangle.TOP  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan( 1 );
                c.setPadding( 1 );
                c.setPaddingBottom( 3 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setBackgroundColor(  ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);

                // Create the cell for the full table
                c = new PdfPCell( new Phrase( "" ) );
                c.setBackgroundColor( ct2Colors.hraBlue );
                c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                c.setCellEvent(new PercentileHeaderCellEvent( this.baseFontCalibri , validCount, ct2Colors ) );
                t.addCell(c);

                // Next, for each row
                if( otes.getHasValidOverallNorm() )
                {
                    rowCount++;
                    addComparisonTableRow( t, names[0], percentiles[0], counts[0], rowCount==validCount );
                }

                else if( otes.getHasValidOverallZScoreNorm() && reportData.getTestEvent().getProduct()!=null && reportData.getTestEvent().getProduct().getConsumerProductType().getIsJobSpecific() )
                {
                    rowCount++;
                    addComparisonTableRow( t, lmsg( "g.OverallPercApprox" ), otes.getOverallZScorePercentile(), 0, rowCount==validCount );
                }

                if( otes.getHasValidCountryNorm() )
                {
                    rowCount++;
                    addComparisonTableRow( t, names[1], percentiles[1], counts[1], rowCount==validCount );
                }

                if( orgInfoOk && otes.getHasValidAccountNorm() )
                {
                    rowCount++;
                    addComparisonTableRow( t, names[2], percentiles[2], counts[2], rowCount==validCount );
                }

                /*
                if( validCount == 0 )
                {
                    Paragraph p = new Paragraph();

                    Chunk ch = new Chunk( lmsg( "g.NoteC" ) + " ", this.fontBoldItalic  );

                    p.add( ch );

                    ch = new Chunk(  lmsg( "g.InsufficientDataForComparisons" ), this.fontItalic );

                    p.add( ch );

                    c = new PdfPCell( p );
                    c.setColspan( 3 );
                    c.setPadding( 4 );
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    t.addCell(c);
                } */

                thgt = t.calculateHeights();
            }

            else
            {
                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection( c );

                Phrase p = new Phrase();

                Chunk ch = new Chunk( lmsg( "g.NoteC" ) + " ", this.fontBoldItalic  );

                p.add( ch );

                ch = new Chunk(  lmsg( "g.InsufficientDataForComparisons" ), this.fontItalic );

                p.add( ch );

                c = new PdfPCell( p );
                c.setColspan( 3 );
                c.setPaddingLeft( 4 );
                c.setPaddingRight( 4 );
                c.setPaddingBottom( 7 );
                //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                //c.setBorderWidth( scoreBoxBorderWidth );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell(c);

                thgt = t.calculateHeights();
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addComparisonSection() validCount=" + validCount + ", thgt=" + thgt + ", y=" + y );

            if( thgt + 75 > y )
            {
                addNewPage();

                y = currentYLevel;
            }

            y = addTitle(y, lmsg( "g.ComparisonPcts" ), validCount > 0 ? lmsg( devel ? "g.PercentileNoteDevel" : "g.PercentileNote" ) : null, null, null );

            y -= TPAD;

            t.writeSelectedRows(0, -1, CT2_MARGIN + CT2_BOX_EXTRAMARGIN, y, pdfWriter.getDirectContent() );

            currentYLevel = y- thgt - PAD;

            // currentYLevel = addText( lmsg( "g.PercentileNote" ), fnt );

            // currentYLevel -= PAD;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addComparisonSection()" );
            throw new STException( e );
        }
    }


    private void addComparisonTableRow( PdfPTable t, String name, float percentile, int count, boolean last )
    {
            // LogService.logIt( "BaseCT2ReportTemplate.addComparisonTableRow() name=" + name + ", percentile=" + percentile + ", last =" + last );

            PdfPCell c;

            Font fnt = this.font;

            c = new PdfPCell( new Phrase( name, fnt ) );

            if( reportData.getIsLTR() )
                c.setBorder( last ? Rectangle.LEFT | Rectangle.BOTTOM  : Rectangle.LEFT );
            else
                c.setBorder( last ? Rectangle.RIGHT | Rectangle.BOTTOM  : Rectangle.RIGHT );

            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.hraBlue );
            c.setPadding( 1 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);

            if( percentile >=0 && percentile<1f )
                percentile=1f;

            if( percentile>99f )
                percentile=99f;

            String perStr = I18nUtils.getFormattedNumber( reportData.getLocale(), percentile, 0 ) + NumberUtils.getPctSuffix( reportData.getLocale(), percentile, 0 );

            // I18nUtils.getFormattedInteger( reportData.getLocale(), (int) percentile );

            // Create the cell for the full table
            c = new PdfPCell( new Phrase( perStr, fnt ) );
            c.setBorder( last ? Rectangle.BOTTOM : Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER);
            setRunDirection( c );
            t.addCell(c);


            c = new PdfPCell( new Phrase( " ", this.getFontLargeWhite() ) );
            c.setPadding( 1 );
            if( last )
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
            else
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );

            String ctStr = lmsg( "g.PercentileCountStr1", new String[] { Integer.toString(count)}  );

            if( 1==1 || count<=1 )
                ctStr = "";

            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new PercentileBarCellEvent( percentile , ctStr,  ct2Colors.barGraphCoreShade2,  ct2Colors.barGraphCoreShade1, baseFontCalibri ) );
            setRunDirection( c );
            t.addCell(c);
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

            if( reportData.getReportRuleAsBoolean( "ovrviewoff" ) || reportData.getReport().getIncludeOverviewText()==0 )
                return false;

            // currentYLevel = 0;

            String ovrTxt =  reportData.getReportOverviewText(); // ScoreFormatUtils.getDescripFromTextParam( reportData.getTestEvent().getOverallTestEventScore().getTextParam1() );

            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() " + ovrTxt + ", textParam1=" + reportData.getTestEvent().getOverallTestEventScore().getTextParam1() );

            if( ovrTxt == null || ovrTxt.isEmpty() )
                return false;

            ovrTxt = tctrans( ovrTxt, false );

            Font fnt =   getHeaderFontXLarge();

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            float y = addTitle(previousYLevel, lmsg( "g.AssessmentOverview" ), null, null, null );

            y -= PAD;

            // Add Title
            // ITextUtils.addDirectText( pdfWriter, lmsg("g.AssessmentOverview"), CT2_MARGIN, y, Element.ALIGN_LEFT, fnt, false);

            // Change getFont()
            fnt =  getFont();

            // float spaceLeft = y - 2*PAD - footerHgt;
            float spaceLeft = y - footerHgt;

            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float leading = fnt.getSize();

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, ovrTxt, txtW, Element.ALIGN_LEFT, leading, fnt);

            //  y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            //LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );


            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN;
            float txtUrx = txtLlx + txtW;

            // if have room, draw it here. Otherwise, need to use columnText
            if( txtHght <= spaceLeft )
            {
                if( reportData.getIsLTR() )
                {

                    // y -= txtHght;
                    Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                    //LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                    ITextUtils.addDirectText(  pdfWriter, ovrTxt, rect, Element.ALIGN_LEFT, leading, fnt, false );

                    // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                    currentYLevel = y - txtHght;

                    return false;
                }

                else
                {
                    PdfPTable t = new PdfPTable( 1 );

                    t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
                    t.setLockedWidth( true );
                    setRunDirection(t);

                    PdfPCell c = t.getDefaultCell();
                    c.setPadding( 0 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    setRunDirection(c);

                    t.addCell( new Phrase( ovrTxt , fnt ) );

                    float ht = t.calculateHeights(); //  + 500;

                    // float yy = pageHeight/2 - (pageHeight/2 - ht)/2;

                    float tw = t.getTotalWidth();

                    float tableX = (pageWidth - tw )/2;

                    t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );

                    currentYLevel = y - ht;

                    return false;

                }
            }

            else
            {
                //LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                // float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );
                setRunDirection( ct );

                Phrase p = new Phrase( ovrTxt, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() adding second column "  );

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
            LogService.logIt( e, "BaseCT2ReportTemplate.addAssessmentOverview()" );

            throw new STException( e );
        }
    }





    public void addDetailedReportInfoHeader() throws Exception
    {
        try
        {
            if( reportData.getReportRuleAsBoolean( "ovrdetailedoff" ) )
                return;

            boolean piiOk = !reportData.getReportRuleAsBoolean( "omitpiipdf" );
            boolean orgInfoOk = !reportData.getReportRuleAsBoolean( "omitorgpdf" );


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

            if( reportCompanyAdminName != null && reportCompanyAdminName.contains("AUTOGEN") )
                reportCompanyAdminName = null;

            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            if( !includeCompanyInfo )
                reportCompanyName = "                        ";

            boolean includePreparedFor = includeCompanyInfo && reportCompanyAdminName!=null && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            if( !includePreparedFor )
                reportCompanyAdminName = "                        ";

            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );

            String thirdPartyId = reportData.getThirdPartyTestEventIdentifier();

            String thirdPartyTestEventIdentifierName = reportData.getThirdPartyTestEventIdentifierName();

            boolean hasThirdPartyId = thirdPartyId!=null && !thirdPartyId.isEmpty();

            String titleStr = reportData.getReportRuleAsString( "overdetailtitle" );

            if( titleStr==null  )
                titleStr = lmsg( "g.Detail" );

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, titleStr, null, null, null );

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

            Chunk c;
            Phrase p;

            if( piiOk )
            {
                p = new Phrase( lmsg( "g.CandidateC"), fntLft );
                t.addCell( p );

                c = new Chunk( reportData.getUserName() + (!reportData.u.getUserType().getNamed() || reportData.u.getEmail()==null || StringUtils.isCurlyBracketed( reportData.u.getEmail() ) ? "" : ", "), fntRgt );
                p = new Phrase();
                p.add( c );
                c = new Chunk( ( !reportData.u.getUserType().getNamed() || reportData.u.getEmail()==null || StringUtils.isCurlyBracketed( reportData.u.getEmail() ) ? "" : reportData.u.getEmail()), fntRgt2 );
                p.add( c );
                t.addCell( p );
            }

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

            boolean hideOverallNumeric = reportData.getReportRuleAsBoolean( "ovrnumoff" ) || reportData.getReportRuleAsBoolean( "ovroff" );

            String[] params = new String[] { I18nUtils.getFormattedDate(reportData.getLocale(), reportData.getUser().getTimeZone(), reportData.getTestKey().getStartDate() ),
                                             reportCompanyAdminName == null ? "" : reportCompanyAdminName,
                                             reportCompanyName == null ? "" : reportCompanyName,
                                             reportCompanyAdminEmail==null ? "" : reportCompanyAdminEmail };

            if( orgInfoOk && includePreparedFor )
            {
                String auth = reportData.getTestKey().getAuthUser() == null ? lmsg( "g.AuthStr" + (includeDates ? "" : "NoDates") , params ) : lmsg( "g.AuthStrCombined" + (includeDates ? "" : "NoDates") , params );

                t.addCell( new Phrase( lmsg( "g.AuthorizedC"), fntLft ) );
                t.addCell( new Phrase( auth, fntLft ) );
            }

            TestKey tk = reportData.tk;
            Org o = reportData.o;


            if( tk!=null && o!=null )
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

            if( reportData.u!=null )
            {
                if( reportData.u.getHasIpCountry()  )
                {
                    t.addCell( new Phrase( lmsg("g.IpCountry") + ":", fntLft ) );
                    t.addCell( new Phrase( reportData.u.getIpCountryName(), fntLft ) );
                }
                if( reportData.u.getHasIpState()  )
                {
                    t.addCell( new Phrase( lmsg("g.IpState") + ":", fntLft ) );
                    t.addCell( new Phrase( reportData.u.getIpState(), fntLft ) );
                }
                if( reportData.u.getHasIpCity()  )
                {
                    t.addCell( new Phrase( lmsg("g.IpCity") + ":", fntLft ) );
                    t.addCell( new Phrase( reportData.u.getIpCity(), fntLft ) );
                }
            }

            if( tk!=null && tk.getAssistiveTechnologyTypeIds()!=null && !tk.getAssistiveTechnologyTypeIds().isBlank() )
            {
                StringBuilder sbx = new StringBuilder();
                for( Integer sid : tk.getAssistiveTechnologyTypeIdList() )
                {
                    if( sid<=0 )
                        continue;

                    if( sbx.length()>0 )
                        sbx.append( ", " );
                    sbx.append( lmsg(AssistiveTechnologyType.getValue( sid ).getKey()) );
                }

                if( tk.getAssistiveTechnologyTypeOtherValue()!=null && !tk.getAssistiveTechnologyTypeOtherValue().isBlank() )
                {
                    sbx.append( "\n" + tk.getAssistiveTechnologyTypeOtherValue() );
                }

                if( sbx.length()>0 )
                {
                    t.addCell( new Phrase( lmsg("g.AssistiveTech") + ":", fntLft ) );
                    t.addCell( new Phrase( sbx.toString(), fntLft ) );
                }
            }



            if( includeDates )
            {
                t.addCell( new Phrase( lmsg( "g.StartedC"), fntLft ) );
                t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getStartDate(), reportData.getUser().getTimeZone() ), fntLft ) );

                t.addCell( new Phrase( lmsg( "g.FinishedC"), fntLft ) );
                t.addCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), reportData.getTestEvent().getLastAccessDate(), reportData.getUser().getTimeZone() ), fntLft ) );
            }

            if( !hideOverallNumeric )
            {
                boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(reportData.getOrg(), reportData.getTestEvent()) && reportData.getTestEvent().getOverallTestEventScore()!=null; //  && reportData.getTestEvent().getOverallTestEventScore().getRawScore()>=0;

                int scrDigits = reportData.getReport().getIntParam2() >= 0 ? reportData.getReport().getIntParam2() : reportData.getTestEvent().getScorePrecisionDigits();

                float scrValue = useRawOverallScore ? reportData.getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow() : reportData.getTestEvent().getOverallScore();

                String overallScoreTitle = reportData.getReport().getStrParam4()!=null && !reportData.getReport().getStrParam4().isEmpty() ? reportData.getReport().getStrParam4() + ":"  :  lmsg( useRawOverallScore ? "g.OverallRawScoreC" : "g.OverallScoreC");
                t.addCell( new Phrase( overallScoreTitle, fntLft ) );

                String scrStr = I18nUtils.getFormattedNumber( reportData.getLocale(), scrValue, scrDigits );

                boolean useScoreTextAsNumScore = reportData.getReportRuleAsBoolean( "ovrscrtxtasnum" );

                if( useScoreTextAsNumScore )
                {
                    String scrTxt = null;

                    if( useRawOverallScore )
                    {
                        if( reportData.te.getSimXmlObj()==null )
                        {
                            if( reportUtils==null )
                                reportUtils  = new ReportUtils();

                            reportUtils.loadTestEventSimXmlObject(reportData.te);

                            scrTxt = BaseTestEventScorer.getScoreTextForOverallScore( reportData.getTestEvent().getScoreColorSchemeType(), scrValue, reportData.te );
                        }
                    }

                    else
                        scrTxt = reportData.getOverallScoreText();

                    scrStr = ReportUtils.getScoreValueFromStr( scrTxt );

                    if( scrStr==null )
                        scrStr = "";
                }


                t.addCell( new Phrase( scrStr, fntLft ) );
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addDetailedReportHeader() reportData.getTestEvent().getOverallTestEventScore().getIntParam1()=" + reportData.getTestEvent().getOverallTestEventScore().getIntParam1() );


            if( reportData.getTestEvent().getOverallTestEventScore().getIntParam1()>0 )
            {
                t.addCell( new Phrase( "" ) );
                t.addCell( new Phrase( lmsg("g.OvrTimeout"), this.getFontBoldRed() ) );
            }

            t.writeSelectedRows(0, -1, x, y, pdfWriter.getDirectContent() );

            // LogService.logIt( "BaseCT2ReportTemplate.addDetailedReportHeader() t.calculateHeights()=" + t.calculateHeights() + ", currentY=" + y );

            currentYLevel = y - t.calculateHeights();

            // LogService.logIt( "BaseCT2ReportTemplate.addDetailedReportHeader() currentYLevel=" + currentYLevel );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addDetailedReportInfoHeader()" );

            throw new STException( e );
        }
    }

    protected java.util.List<TestEventScore> getTestEventScoreListToShow( TestEventScoreType test, SimCompetencyClass scc )
    {
        return getTestEventScoreListToShow(test, scc, false, false );
    }

    protected java.util.List<TestEventScore> getTestEventScoreListToShow( TestEventScoreType test, SimCompetencyClass scc, boolean ignoreHide, boolean includeSubclassed )
    {
        java.util.List<TestEventScore> out = new ArrayList<>();

        for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( test.getTestEventScoreTypeId() ) )
        {
            // if it's a competency type and we include subclassed and intparam1 = target simcompetencyclassid, do not ignore.
            if( test.getIsCompetency() && includeSubclassed && tes.getIntParam1()==scc.getSimCompetencyClassId() )
            {}

            // ignore if classids don't match.
            else if( tes.getSimCompetencyClassId()!=scc.getSimCompetencyClassId() )
                continue;

            // if supposed to hide
            if( !ignoreHide && !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;

            out.add( tes );
        }

        Collections.sort( out, new DisplayOrderComparator() );  // new TESNameComparator() );

        return out;
    }


    protected void addBiodataInfo() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( !reportData.includeBiodataInfo() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDBIODATA ); // new ArrayList<>();

            if( tesl.size() <= 0 )
                return;

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            // LogService.logIt( "BaseCT2ReportTemplate.addBiodataInfo() found " + tesl.size() );

            String titleKey = "g.BiodataTitle";
            String subtitleKey = "g.BiodataSubtitle";

            if( reportData.getReportRuleAsBoolean( "biodataisscoredsurvey" ) )
            {
                titleKey = "g.BiodataTitleScoredSurvey";
                subtitleKey = "g.BiodataSubtitleScoredSurvey";
            }

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId() );

            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean biodataInterviewQLimit = reportData.getReportRuleAsBoolean( "biodatainterviewlimit" );
            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId() );

            addAnyCompetenciesInfo(tesl, titleKey, ttext, subtitleKey, sttext, "g.Detail", "g.Description", "g.BiodataCaveatHeader", "g.BiodataCaveatFooter", false, showInterview, !biodataInterviewQLimit, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addBiodataInfo()" );

            throw new STException( e );
        }
    }

    protected void addCustomInfo( int customIndex) throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            SimCompetencyClass scc = SimCompetencyClass.getValue( 20 + customIndex );

            if( !scc.getIsAnyCustom() )
                return;

            SimCompetencyGroupType scgt = SimCompetencyGroupType.getValue( 100 + customIndex );

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, scc ); // new ArrayList<>();
            if( customIndex==1 )
                tesl.addAll(  getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CUSTOM_COMBO ) );
            if( tesl.size() <= 0 )
                return;

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            // LogService.logIt( "BaseCT2ReportTemplate.addCustomInfo() found " + tesl.size() + ", customIndex=" + customIndex );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + scgt.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + scgt.getSimCompetencyGroupTypeId() );
            String customStText = getCustomDetailSubtext( scgt.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + scgt.getSimCompetencyGroupTypeId() );

            addAnyCompetenciesInfo(tesl, "g.CustomTitle" + customIndex, ttext, null, sttext, "g.Detail", "g.Description", null, null, false, showInterview, false, true  );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addCustomInfo()" );
            throw new STException( e );
        }
    }



    protected void addAbilitiesInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY_COMBO ) );

            if( tesl.size() <= 0 )
                return;

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            // LogService.logIt( "BaseCT2ReportTemplate.addAbilitiesInfo() found " + tesl.size() );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );

            addAnyCompetenciesInfo(tesl, "g.AbilitiesTitle", ttext, "g.AbilitiesSubtitle", sttext, "g.Detail", "g.Description", null, null, false, showInterview, false, true  );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addAbilitiesInfo()" );
            throw new STException( e );
        }
    }


    protected void addInterestsInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDINTEREST ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.INTERESTS_COMBO ) );

            if( tesl.size() <= 0 )
                return;

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            // LogService.logIt( "BaseCT2ReportTemplate.addInterestsInfo() found " + tesl.size() );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.INTERESTS.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.INTERESTS.getSimCompetencyGroupTypeId() );

            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.INTERESTS.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.INTERESTS.getSimCompetencyGroupTypeId() );
            addAnyCompetenciesInfo(tesl, "g.InterestsTitle", ttext, "g.InterestsSubtitle", sttext, "g.Detail", "g.Description", null, null, false, showInterview, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addInterestsInfo()" );

            throw new STException( e );
        }
    }

    protected String getCustomDetailSubtext( int simCompetencyGroupId )
    {
        if( reportData==null || reportData.getR2Use().getTextParam4()==null || reportData.getR2Use().getTextParam4().isBlank() )
            return null;

        return StringUtils.getBracketedArtifactFromString(reportData.getR2Use().getTextParam4(), Constants.DETAILINTROKEY + Integer.toString(simCompetencyGroupId) );
    }


    protected void addKSInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = new ArrayList<>(); // getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.ABILITY ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.CORESKILL ) ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.KNOWLEDGE ) ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDTYPING ) ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDDATAENTRY ) ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDCHAT ) ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDESSAY ) ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAUDIO ) ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAVUPLOAD ) ); // new ArrayList<>();

            if( tesl.size() <= 0 )
                return;

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            // LogService.logIt( "BaseCT2ReportTemplate.addKSAInfo() found " + tesl.size() );
            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );

            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
            addAnyCompetenciesInfo(tesl, "g.KSTitle", ttext, "g.KSSubtitle", sttext, "g.Detail", "g.Description", null, null, false, showInterview, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addKSAInfo()" );

            throw new STException( e );
        }
    }

    protected void addAIMSInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.NONCOGNITIVE ); // new ArrayList<>();
            tesl.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY , SimCompetencyClass.NONCOG_COMBO ) );

            if( tesl.size() <= 0 )
                return;

            // LogService.logIt( "BaseCT2ReportTemplate.addAIMSInfo() found " + tesl.size() );

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );

            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
            addAnyCompetenciesInfo(tesl, "g.AIMSTitle", ttext, "g.AIMSSubtitle", sttext, "g.Detail", "g.Description", null, null, false, showInterview, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addAIMSInfo()" );

            throw new STException( e );
        }
    }

    protected void addEQInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.EQ ); // new ArrayList<>();

            if( tesl.size() <= 0 )
                return;

            // LogService.logIt( "BaseCT2ReportTemplate.addEQInfo() found " + tesl.size() );

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId() );

            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId() );
            addAnyCompetenciesInfo(tesl, "g.EQTitle", ttext, "g.EQSubtitle", sttext, "g.Detail", "g.Description", null, null, false, showInterview, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addEQInfo()" );

            throw new STException( e );
        }
    }


    protected void addAIInfo() throws Exception
    {
        try
        {
            if( !reportData.includeCompetencyScores() )
                return;

            if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
                return;

            java.util.List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.VOICE_PERFORMANCE_INDEX ); // new ArrayList<>();

            if( tesl.size() <= 0 )
                return;

            // LogService.logIt( "BaseCT2ReportTemplate.addAIInfo() found " + tesl.size() );

            Collections.sort( tesl, new DisplayOrderComparator() );  // new TESNameComparator() );

            String ttext = reportData.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId() );
            String sttext = reportData.getReportRuleAsString("competencygroupsubtitle" + SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId() );
            String customStText = getCustomDetailSubtext( SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId() );
            if( customStText!=null && !customStText.isBlank() )
                sttext = customStText;

            boolean showInterview = reportData.includeInterview() && !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId() );
            addAnyCompetenciesInfo(tesl, "g.AITitle", ttext, "g.AISubtitle", sttext, "g.Detail", "g.Description", null, null, false, showInterview, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addAIInfo()" );

            throw new STException( e );
        }
    }



    protected void addEducTrainingInfo() throws Exception
    {
        try
        {
            if( !reportData.includeEducTypeDescrip() && ! reportData.includeTrainingTypeDescrip() && !reportData.includeRelatedExperTypeDescrip() )
                return;

            // Integer tempInteger = ReportUtils.getReportFlagIntValue( "suppressonetinfoinreports", reportData.getTestKey(), reportData.getSuborg(), reportData.getOrg(), reportData.getReport() );
            boolean suppressOnet = reportData.getReportRuleAsBoolean( "suppressonetinfoinreports" ); //  tempInteger==null || tempInteger.intValue()!=1 ? false : true;

            if( suppressOnet )
                return;

            TestEvent te = reportData.getTestEvent();

            // no data
            if( te.getEducTypeId() == 0 && te.getExperTypeId()==0 && te.getTrainTypeId()==0 )
                return;

            // At this point we will create a report

            previousYLevel =  currentYLevel;

            java.util.List<TextAndTitle> ttl = new ArrayList<>();

            if( reportData.includeEducTypeDescrip() && te.getEducTypeId() > 0)
                ttl.add( new TextAndTitle(  lmsg(EducType.getValue( te.getEducTypeId() ).getKey() ), lmsg( "g.MinEducLevel" ) ) );
//                 ttl.add( new TextAndTitle(  EducType.getValue( te.getEducTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinEducLevel" ) ) );

            if( reportData.includeTrainingTypeDescrip()&& te.getTrainTypeId() > 0)
                ttl.add( new TextAndTitle( lmsg(TrainingType.getValue( te.getTrainTypeId() ).getKey()), lmsg( "g.MinTrainType" ) ) );
                //ttl.add( new TextAndTitle( TrainingType.getValue( te.getTrainTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinTrainType" ) ) );

            if( reportData.includeRelatedExperTypeDescrip()&& te.getExperTypeId() > 0)
                ttl.add( new TextAndTitle( lmsg(RelatedExperType.getValue( te.getTrainTypeId() ).getKey()), lmsg( "g.MinRelatedExp" ) ) );
                // ttl.add( new TextAndTitle( RelatedExperType.getValue( te.getTrainTypeId() ).getName( reportData.getLocale() ), lmsg( "g.MinRelatedExp" ) ) );

            if( ttl.isEmpty() )
                return;

            float y = addTitle(previousYLevel, lmsg( "g.MinQualGuidelines" ), lmsg( "g.MinQualGuidelinesSubtitle" ), null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            //t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );

            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.Item" ) , fontLmWhite) );
            //t.addCell( new Phrase( "" , fontLmWhite) );

            c = new PdfPCell(new Phrase( lmsg( "g.Item" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true));
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( "" , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth);
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false));
            setRunDirection( c );
            t.addCell(c);

            // Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( this.ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tctrans( tt.getText(), false ), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( this.ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

            }

            currentYLevel = addTableToDocument(y, t, false, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addEducTrainingInfo()" );

            throw new STException( e );
        }
    }


    protected void addCalculationSection(boolean newPage) throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseCT2ReportTemplate.addCalculationSection() START reportData.forceCalcSection=" + reportData.forceCalcSection );

            if( !reportData.forceCalcSection )
            {
                if( reportData.getReportRuleAsBoolean("scorecalcoff") )
                    return;

                if( reportData.getReport().getIncludeScoreCalculationInfo()!=1 )
                    return;

                if( reportData.getOrg().getIncludeScoreCalcInfoInReports()!=1 )
                    return;
            }

            TestEvent te = reportData.te;
            SimJ simJ = te.getSimXmlObj();

            if( simJ == null )
            {
                ReportUtils ru = new ReportUtils();
                ru.loadTestEventSimXmlObject(te);
                simJ = te.getSimXmlObj();
            }

            String keyStub = "";

            ScoreFormatType sft = reportData.getTestEvent().getScoreFormatType();

            if( sft.equals( ScoreFormatType.NUMERIC_0_TO_3) )
                keyStub = ".zeroto3";

            else if( sft.equals( ScoreFormatType.NUMERIC_1_TO_5) )
                keyStub = ".oneto5";

            else if( sft.equals(ScoreFormatType.NUMERIC_1_TO_10) )
                keyStub = ".oneto10";

            else if( sft.equals( ScoreFormatType.OTHER_SCORED) )
                keyStub = ".other";

            ProfileUtils.applyProfileToSimXmlObj( te );

            // addNewPage();

            boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(reportData.getOrg(), reportData.getTestEvent()) &&
                                         reportData.getTestEvent().getOverallTestEventScore()!=null; // && reportData.getTestEvent().getOverallTestEventScore().getRawScore()>=0;

            boolean usesHRAStandardZScores = simJ.getOverallscaledscorecalctype()==OverallScaledScoreCalcType.NORMAL_TRANS.getOverallScaledScoreCalcTypeId() &&
                                             ( simJ.getOverallscorecalctype()==OverallRawScoreCalcType.RAW_SCOREWEIGHTS.getOverallRawScoreCalcTypeId() || simJ.getOverallscorecalctype()==OverallRawScoreCalcType.RAW_SCOREWEIGHTS_NORMALIZED.getOverallRawScoreCalcTypeId() ) &&
                                             !useRawOverallScore;

            OverallRawScoreCalcType overallRawScoreCalcType = OverallRawScoreCalcType.getValue( simJ.getOverallscorecalctype() );

            boolean usesWeights = overallRawScoreCalcType.getIsAnyWeights();

            boolean usesSum = overallRawScoreCalcType.getIsSum();

            //if( usesSum )
            //    usesWeights=false;



            //if( usesHRAStandardZScores )
            //    useRawOverallScore = false;


            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = usesWeights ? new PdfPTable( new float[] { 1f, 1f, 1f, 1f, 1f } ) : new PdfPTable( new float[] { 1f, 1f, 1f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection(t );

            c = t.getDefaultCell();
            c.setPadding( 1 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            // Create header
            c = new PdfPCell( new Phrase( lmsg( "g.Competency"), this.fontBold ) );
            // c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setPadding( 1 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            // c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Score"), this.fontBold ) );
            c.setPadding( 1 );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);

            if( usesWeights )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.Distribution"), this.fontBold ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);
            }

            c = new PdfPCell( new Phrase( lmsg( "g.ValueUsedInCalc"), this.fontBold ) );
            c.setPadding( 1 );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);

            if( usesWeights )
            {
                c = new PdfPCell( new Phrase( lmsg( usesSum ? "g.Weight" : "g.WeightPct"), this.fontBold ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);
            }

            SimJ.Simcompetency simCompetency;

            String tempStr;
            float scrUsed;

            float totalWeights = 0;

            float weightPct = 0;
            float totalComps = 0;

            float score = 0;

            SimCompetencyClass scc;

            List<TestEventScore> tesl2 = new ArrayList<>();

            ScoreColorSchemeType scoreColorSchemeType = te.getScoreColorSchemeType();

            for( TestEventScore tes : te.getTestEventScoreList() )
            {
                if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY )  )
                    continue;

                if( usesWeights && tes.getWeight()<= 0.01 && !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                simCompetency = getSimCompetencyForTes( tes, simJ );

                if( simCompetency == null )
                {
                    LogService.logIt( "BaseCt2ReportTemplate.addCalculationSection() Unable to find SimCompetency for tes.id=" + tes.getTestEventScoreId() + ", tes.name=" + tes.getName() + ", tes.nameEnglish=" + tes.getNameEnglish() + ", simId=" + te.getSimId() + ", simVersionId=" + te.getSimVersionId() );
                    continue;
                }

                // If competency set to include in overall only if below X and score is above X, skip it.
                if( simCompetency.getIncludeinoverallifbelow() > 0 && tes.getRawScore() >= simCompetency.getIncludeinoverallifbelow() )
                    continue;

                scc = SimCompetencyClass.getValue( simCompetency.getClassid() );

                // If we have a task-type of competency, and not supposed to include tasks in the overall score, don't include it.
                if(  scc.getIsTask() ) //  && simJ.getIncludetasksoverall() != 1 )
                    continue;

                // If we have an Identity Image Capture competency. Don't include in overall score.
                if( scc.equals( SimCompetencyClass.SCOREDIMAGEUPLOAD ) )
                    continue;

                // if have non-task type of competency and not supposed to include in overall, skip it.
                if(  ( scc.getIsDirectCompetency() || scc.getIsAggregate() || scc.getIsCombo() ) && simJ.getIncludecompetenciesoverall() != 1 )
                    continue;

                if(  scc.getIsInterest() && simJ.getIncludeinterestoverall() != 1 )
                    continue;

                if(  scc.getIsExperience() && simJ.getIncludeexperienceoverall() != 1 )
                    continue;

                if(  scc.getIsBiodata() && simJ.getIncludebiodataoverall() != 1 )
                    continue;

                tesl2.add(tes);

                CompetencyScoreType cst = getCompetencyScoreTypeForSimCompetency( simCompetency, simJ );

                if( cst!=null && usesHRAStandardZScores && ( cst.isTrueDifference() || cst.isAvgZscoreDiff() ) )
                {
                    // Still used with equivalent Z statistic or Z statistic.
                }


                else if( usesHRAStandardZScores &&
                   ( simCompetency.getRawscorecalctypeid()!=SimCompetencyRawScoreCalcType.ZSCORE_BASED_ON_TOTALS.getSimCompetencyRawScoreCalcTypeId() || simCompetency.getStddeviation()<=0 ) )
                {
                    //totalComps++;
                    // Skip these for weight calcs.
                    continue;
                }

                if( usesSum )
                    totalWeights=1;
                else
                    totalWeights += tes.getWeight();
                totalComps++;
            }

            if( totalComps <= 0 )
            {
                LogService.logIt( "BaseCT2ReportTemplate.addCalculationSection() No competencies found in TestEvent. returning. " + te.toString() );
                return;
            }

            StringBuilder sb = new StringBuilder();

            float weightUsed;

            float equivRawScore;
            boolean usesEquivScore;

            int nonlinearCompetencyCount=0;

            CompetencyScoreType cst = null;

            for( TestEventScore tes : tesl2 ) // te.getTestEventScoreList() )
            {
                //LogService.logIt( "BaseCT2ReportTemplate.addCalculationSection() Competency: " + tes.getName() + ", Tyep=" + tes.getTestEventScoreTypeId() );

                //if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                //    continue;

                //if( tes.getWeight()<= 0 && tes.getHide()==1 )
                //    continue;

                simCompetency = getSimCompetencyForTes( tes, simJ );

                //LogService.logIt( "BaseCT2ReportTemplate.addCalculationSection() Competency: " + tes.getName() + ", simcompetency=" + (simCompetency==null ? "null" : "found simCompetency.getIncludeinoverallifbelow()=" + simCompetency.getIncludeinoverallifbelow() ) );

                //if( simCompetency == null )
                //    continue;


                // If competency set to include in overall only if below X and score is above X, skip it.
                ////if( simCompetency.getIncludeinoverallifbelow() > 0 && tes.getRawScore() >= simCompetency.getIncludeinoverallifbelow() )
                //    continue;

                //scc = SimCompetencyClass.getValue( simCompetency.getClassid() );

                // If we have a task-type of competency, and not supposed to include tasks in the overall score, don't include it.
                //if(  scc.getIsTask() && simJ.getIncludetasksoverall() != 1 )
                //    continue;

                // If we have an Identity Image Capture competency. Don't include in overall score.
                //if( scc.equals( SimCompetencyClass.SCOREDIMAGEUPLOAD ) )
                //    continue;

                // if have non-task type of competency and not supposed to include in overall, skip it.
                //if(  ( scc.getIsDirectCompetency() || scc.getIsAggregate() || scc.getIsCombo() ) && simJ.getIncludecompetenciesoverall() != 1 )
                //    continue;

                //if(  scc.getIsInterest() && simJ.getIncludeinterestoverall() != 1 )
                //    continue;

                //if(  scc.getIsExperience() && simJ.getIncludeexperienceoverall() != 1 )
                //    continue;

                //if(  scc.getIsBiodata() && simJ.getIncludebiodataoverall() != 1 )
                //    continue;

                weightUsed = tes.getWeight();

                if( usesSum )
                    weightPct = weightUsed;
                else
                    weightPct = totalWeights>0 ? 100*weightUsed/totalWeights  : 100f/totalComps;

                cst = getCompetencyScoreTypeForSimCompetency( simCompetency, simJ );

                usesEquivScore = false;
                equivRawScore=0;

                if( usesHRAStandardZScores && cst!=null && ( cst.isTrueDifference() ) ) // || cst.isAvgZscoreDiff() ) )
                {
                    usesEquivScore=true;
                    if( simCompetency.getScaledstddeviation()>0 )
                        equivRawScore= ( tes.getScore() - simCompetency.getScaledmean() )/simCompetency.getScaledstddeviation();
                }

                else if( usesHRAStandardZScores && cst!=null && ( cst.isAvgZscoreDiff() ) && simCompetency.getRawscorecalctypeid()!=SimCompetencyRawScoreCalcType.ZSCORE_BASED_ON_TOTALS.getSimCompetencyRawScoreCalcTypeId() )
                {
                    usesEquivScore=true;
                    if( simCompetency.getScaledstddeviation()>0 )
                        equivRawScore= ( tes.getScore() - simCompetency.getScaledmean() )/simCompetency.getScaledstddeviation();
                }

                else if( usesHRAStandardZScores &&
                    ( simCompetency.getRawscorecalctypeid()!=SimCompetencyRawScoreCalcType.ZSCORE_BASED_ON_TOTALS.getSimCompetencyRawScoreCalcTypeId() ||
                      simCompetency.getStddeviation()<=0 ) )
                {
                    weightUsed = 0;
                    weightPct = 0;
                }

                c = new PdfPCell( new Phrase( tctrans( reportData.getCompetencyName(tes),false ) , this.font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), 4 ), font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                // simCompetency = getSimCompetencyForTes( tes, simJ );

                if( simCompetency == null )
                {
                    LogService.logIt( "BaseCT2ReportTemplate.addCalculationSection() Cannot find SimCompetency matching " + tes.toString() + ", teId=" + te.getTestEventId() );
                    tempStr = lmsg( "g.NotUsedInOverall" );
                    scrUsed = 0;
                }

                else if( CategoryDistType.getValue( simCompetency.getCategorydisttype() ).getLinear() && simCompetency.getUsecategforoverall()!=1 )
                {
                    if( usesHRAStandardZScores && usesEquivScore )
                    {
                        tempStr = lmsg( "g.ZScore" );
                        scrUsed = equivRawScore;
                    }

                    else if( usesHRAStandardZScores &&
                        ( simCompetency.getRawscorecalctypeid()!=SimCompetencyRawScoreCalcType.ZSCORE_BASED_ON_TOTALS.getSimCompetencyRawScoreCalcTypeId() ||
                          simCompetency.getStddeviation()<=0 ) )
                    {
                        tempStr = lmsg( "g.NotUsedInOverall" );
                        scrUsed = 0;
                    }

                    else
                    {
                        tempStr = usesHRAStandardZScores ? lmsg( "g.ZScore" ) : lmsg( "g.Linear" );
                        scrUsed = usesHRAStandardZScores ? tes.getRawScore() : tes.getScore();
                    }
                }

                else if( usesHRAStandardZScores )
                {
                    if( usesHRAStandardZScores && usesEquivScore )
                    {
                        tempStr = lmsg( "g.ZScoreAbsValue" );
                        scrUsed = -1*Math.abs(equivRawScore);
                        nonlinearCompetencyCount++;
                        //tempStr = lmsg( "g.ZScore" );
                        //scrUsed = equivRawScore;
                    }

                    else if( simCompetency.getRawscorecalctypeid()!=SimCompetencyRawScoreCalcType.ZSCORE_BASED_ON_TOTALS.getSimCompetencyRawScoreCalcTypeId() || simCompetency.getStddeviation()<=0 )
                    {
                        tempStr = lmsg( "g.NotUsedInOverall" );
                        scrUsed = 0;
                    }

                    else
                    {
                        tempStr = lmsg( "g.ZScoreAbsValue" );
                        scrUsed = -1*Math.abs(tes.getRawScore());
                        nonlinearCompetencyCount++;
                    }
                }

                else
                {
                    tempStr = lmsg( "g.ColorCategory" );
                    int catId = ScoreUtils.getScoreCategoryTypeId(simCompetency, tes.getScore(), scoreColorSchemeType!=null ? scoreColorSchemeType : ScoreColorSchemeType.getType( simJ.getScorecolorscheme() )); // getScoreCategoryTypeId( simCompetency, tes.getScore(), ScoreColorSchemeType.getType( simJ.getScorecolorscheme() ));
                    scrUsed = ScoreCategoryType.getValue( catId ).getNumericEquivScore( simJ.getScoreformat() );
                }

                if( usesWeights )
                {
                    c = new PdfPCell( new Phrase( tempStr, font ) );
                    c.setPadding( 1 );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( 0.5f );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);
                }

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), scrUsed, 4 ), font ) );
                c.setPadding( 1 );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                if( usesWeights )
                {
                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), weightPct, 4 ), font ) );
                    c.setPadding( 1 );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( 0.5f );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);
                }

                sb.append( "CT2Report.calcScores() Competency: " + tes.getName() + ", score=" + tes.getScore() + ", weight used=" + weightUsed + " scrUsed=" + scrUsed + " scrUsed*Weight=" + scrUsed*weightUsed + ", score*weight=" + tes.getScore()*weightUsed + "\n" );

                score += scrUsed*( usesWeights && totalWeights > 0 ? weightUsed : 1f );
            }

            // Remember that score is a weighted average of raw competency scores if usesHRAStandardZScores is true. In this case, the value can be negative.
            if( !usesHRAStandardZScores && sft.getMin()>=0 && score <= 0 )
            {
                LogService.logIt( "BaseCT2ReportTemplate.addCalculationSection() No Scores found in TestEvent. returning. usesHRAStandardZScores=" + usesHRAStandardZScores + ", " + te.toString() );
                return;
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addCalculationSection() \n" + sb.toString() + " TotalWeights: " + totalWeights + ", totalWeights*Scores=" + score + " \n" );


            if( usesWeights )
                score = score / (totalWeights > 0 ? totalWeights : (usesSum ? 1 : totalComps) );

            float weightedAvgScore = score;

            if( usesSum && !usesWeights )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.SumC" ) , font ) );
                c.setColspan(2);
            }

            else
            {
                if( usesWeights && usesSum )
                    c = new PdfPCell( new Phrase( lmsg( usesHRAStandardZScores ? "g.WeightedSumZScoresC" : "g.WeightedSumC") , font ) );
                else
                    c = new PdfPCell( new Phrase( lmsg( usesHRAStandardZScores ? "g.WeightedAverageZScoresC" : "g.WeightedAverageC") , font ) );
            }
            c.setColspan(4);
            c.setPadding( 1 );
            c.setBorder( Rectangle.TOP );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);


            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), weightedAvgScore, 4 ) , font ) );
            c.setBorder( Rectangle.TOP );
            c.setColspan(1);
            c.setPadding( 1 );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( 0.5f );
            c.setHorizontalAlignment( Element.ALIGN_LEFT);
            setRunDirection( c );
            t.addCell(c);


            OverallScaledScoreCalcType overallScoreCalcType = OverallScaledScoreCalcType.getValue( simJ.getOverallscaledscorecalctype() );

            // if normall NCE but forcing Raw.
            if( useRawOverallScore && ( overallScoreCalcType.equals( OverallScaledScoreCalcType.NCE ) || overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) ) )
                overallScoreCalcType = OverallScaledScoreCalcType.RAW;

            float mean = 0;
            float std = 0;

            // OverallRawScoreCalcType overallRawScoreCalcType = OverallRawScoreCalcType.getValue( simJ.getOverallscorecalctype() );

            if( overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) && usesHRAStandardZScores && overallRawScoreCalcType.getRawNormalized() )
            {
                float rawMean = simJ.getRawtoscaledfloatparam3();
                float rawStd = simJ.getRawtoscaledfloatparam4();

                if( rawStd<=0 )
                {
                    rawMean=RuntimeConstants.getFloatValue( "SimRawScoreZConversionMeanDefault" );
                    rawStd=RuntimeConstants.getFloatValue( "SimRawScoreZConversionStdevDefault" );
                }

                //if( nonlinearCompetencyCount>1 )
                //    rawMean += RuntimeConstants.getFloatValue( "SimRawScoreZConversionMeanMultiNonlinearAdjustment" );

                c = new PdfPCell( new Phrase( lmsg( "g.RawMeanC") , font ) );
                c.setColspan(4);
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), rawMean, 4 ) , font ) );
                c.setColspan(1);
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg( "g.RawStdevC") , font ) );
                c.setColspan(4);
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), rawStd, 4 ) , font ) );
                c.setColspan(1);
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);


                weightedAvgScore = (weightedAvgScore-rawMean)/rawStd;

                c = new PdfPCell( new Phrase( lmsg( "g.RawScoreNormalizedC") , font ) );
                c.setColspan(4);
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), weightedAvgScore, 4 ) , font ) );
                c.setColspan(1);
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);
            }


            if( overallScoreCalcType.equals( OverallScaledScoreCalcType.NCE ) || overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) )
            {
                if( usesHRAStandardZScores )
                {
                    mean = simJ.getRawtoscaledfloatparam1();
                    std = simJ.getRawtoscaledfloatparam2();
                }

                else
                {
                    mean = simJ.getMean();
                    std = simJ.getStddeviation();
                }

                if( std<=0 )
                {
                    if( usesHRAStandardZScores )
                    {
                            mean = RuntimeConstants.getFloatValue( "TgtSimScaledScoreMeanSCORE2" );
                            std = RuntimeConstants.getFloatValue( "TgtSimScaledScoreStdevSCORE2" );
                    }

                    else
                    {
                        float[] stdvals = EventFacade.getInstance().getRawScoreStatisticsForProductId( te.getProductId() );

                        if( stdvals[0] <= 10 )
                        {
                            // throw new Exception( "Cannot convert score to z-score. No values for mean and standard deviation in sim or in database. Hits found=" + stdvals[0] );
                            LogService.logIt( "BaseCt2ReportTemplate.addCalculationSection()  Cannot convert score to z-score. No values for mean and standard deviation in sim or in database. Hits found=" + stdvals[0] );
                        }

                        else
                        {
                            mean = stdvals[1];
                            std = stdvals[2];
                        }
                    }
                }


                if( std > 0 )
                {
                    c = new PdfPCell( new Phrase( lmsg( "g.MeanUsedC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), mean, 4 ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( lmsg( "g.StdUsedC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), std, 4 ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    float finalScore = 0;

                    // ScoreFormatType sft = ScoreFormatType.getValue( simJ.getScoreformat() );

                    if( usesHRAStandardZScores )
                    {
                        finalScore = weightedAvgScore*std + mean;
                    }

                    else
                    {
                        float zScore = (score - mean)/std;

                        c = new PdfPCell( new Phrase( lmsg( "g.StdZScrC") , font ) );
                        c.setColspan(4);
                        c.setPadding( 1 );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        setRunDirection( c );
                        t.addCell(c);

                        c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), zScore, 4 ) , font ) );
                        c.setColspan(1);
                        c.setPadding( 1 );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        setRunDirection( c );
                        t.addCell(c);

                        finalScore = zScore*21.06f + 50f;
                    }

                    if( finalScore < sft.getMinScoreToGiveTestTaker())
                        finalScore = sft.getMinScoreToGiveTestTaker();
                    else if( finalScore > sft.getMaxScoreToGiveTestTaker())
                        finalScore = sft.getMaxScoreToGiveTestTaker();

                    c = new PdfPCell( new Phrase( lmsg( usesHRAStandardZScores ? "g.FinalOverallScrC" : ("g.FinalNCEScrC" + keyStub) ) , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), finalScore, 4 ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);
                }
            }

            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.LOOKUP )  )
            {
                    c = new PdfPCell( new Phrase( lmsg( "g.FinalLookupScrC") , font ) );
                    c.setColspan(4);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), te.getOverallScore(), reportData.getTestEvent().getScorePrecisionDigits() ) , font ) );
                    c.setColspan(1);
                    c.setPadding( 1 );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT);
                    setRunDirection( c );
                    t.addCell(c);

            }

            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.RAW )  )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.FinalOverallScrC") , font ) );
                c.setColspan( usesWeights ? 4 : 2 );
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), weightedAvgScore, reportData.getTestEvent().getScorePrecisionDigits() ) , font ) );
                c.setColspan(1);
                c.setPadding( 1 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_LEFT);
                setRunDirection( c );
                t.addCell(c);
            }

            String scoreCalcKey = "g.ScoreCalculationExplanation" + keyStub;

            String[] params = null;

            if( usesHRAStandardZScores )
            {
                if( overallRawScoreCalcType.getRawNormalized() )
                    scoreCalcKey="g.ScoreCalculationExplanationHraZNormalized" + keyStub;
                else
                    scoreCalcKey="g.ScoreCalculationExplanationHraZ" + keyStub;

            }

            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.NCE )  )
                scoreCalcKey = "g.ScoreCalculationExplanation" + keyStub;

            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) )
            {
                scoreCalcKey = "g.ScoreCalculationExplanationNormalTrans" + keyStub;

                params = new String[] {Float.toString(mean), Float.toString(std)};
            }

            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.RAW ) )
                 scoreCalcKey = "g.ScoreCalculationExplanationRaw" + keyStub;

            else if( overallScoreCalcType.equals( OverallScaledScoreCalcType.LOOKUP ) )
            {
                scoreCalcKey = "g.ScoreCalculationExplanationLookup" + keyStub;
            }


            if( newPage )
                addNewPage();

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.ScoreCalculation" ), lmsg( scoreCalcKey, params ), null, null );

            currentYLevel = addTableToDocument(y, t, false, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addPreparationNotesSection()" );

            throw new STException( e );
        }

    }




    public void addTopJobMatchesSummarySection() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;

            if( reportData.getTestEvent().getSimId()<=0  )
                return;

            //  boolean reject = reportData.getTestEvent().getSimId()<=0;

            // if not a career scount and not Job-specific, reject.
            if( !BestJobsReportUtils.isValidCareerScoutProductId( reportData.getTestEvent().getProductId() ) &&
                reportData.getTestEvent().getProduct().getConsumerProductTypeId()!=ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId() )
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addTopJobMatchesSummarySection() SKIPPING for ProductId=" + reportData.getTestEvent().getProductId() + ", consumerProductTypeId=" + reportData.getTestEvent().getProduct().getConsumerProductTypeId() + ", is CareerScout=" + BestJobsReportUtils.isValidCareerScoutProductId( reportData.getTestEvent().getProductId() ) );
                return;
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addTopJobMatchesSummarySection()  Adding for ProductId=" + reportData.getTestEvent().getProductId() );
            // Make sure this is a Sim and it's a Job Specific Consumer Product Type.
            //if( reportData.getTestEvent().getSimId()<=0 ||
            //    reportData.getTestEvent().getProduct().getConsumerProductTypeId()!=ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId() )
            //    return;

            java.util.List<Profile> bestProfilesList = null;

            List<EeoMatch> eeoMatchList = null;


            boolean embeddedRiasec = false;

            BestJobsReportUtils bestJobsReportUtils = null;

            // Competencies are right here (Like in a Career Scout)
            if( BestJobsReportUtils.hasRiasecCompetencies( reportData.getTestEvent()) )
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addTopJobMatchesSummarySection() Has Embedded Competencies productId=" + reportData.getTestEvent().getProductId() );
                embeddedRiasec = true;

                eeoMatchList = new ArrayList<>();

                bestJobsReportUtils = new BestJobsReportUtils(false);

                // No sim descriptor??
                if( reportData.getTestEvent().getSimDescriptor() == null )
                {
                    EventFacade eventFacade = EventFacade.getInstance();

                    // get the Sim Descriptor
                    SimDescriptor sd = eventFacade.getSimDescriptor( reportData.getTestEvent().getSimId(), reportData.getTestEvent().getSimVersionId(), false );

                    reportData.getTestEvent().setSimDescriptor(sd);
                }

                bestProfilesList = bestJobsReportUtils.getTopCT3ProfileMatches(BaseBestJobsReportTemplate.MAX_PROFILES_TO_SHOW, reportData.getTestEvent(), reportData.getTestEvent(), null, -1, 1, 0, true, eeoMatchList );

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

            // embedded in TestEventScore in the report TES for THIS report.
            else
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addTopJobMatchesSummarySection() Has inserted Competencies productId=" + reportData.getTestEvent().getProductId() );
                embeddedRiasec = false;

                TestEventScore rtes = reportData.getTestEvent().getTestEventScoreForReportId( reportData.getReport().getReportId(), null );

                if( rtes == null )
                    return;

                if( rtes.getTextParam1()==null || rtes.getTextParam1().isEmpty() )
                    return;

                bestProfilesList = new BestJobsReportUtils(false).getBestProfilesListWithData(rtes.getTextParam1(), ";", 0 );
            }

            // LogService.logIt( "BaseCT2ReportTemplate.addTopJobMatchesSummarySection()  bestProfilesList = " + (bestProfilesList==null ? "null" : "size: " + bestProfilesList.size() ) );

            if( (bestProfilesList==null || bestProfilesList.isEmpty()) && (eeoMatchList==null || eeoMatchList.isEmpty()) )
                return;

            if( bestJobsReportUtils==null )
                bestJobsReportUtils = new BestJobsReportUtils(false);

            bestJobsReportUtils.setSocsInProfiles( bestProfilesList );

            // Font fnt = getFontXLarge();

            if( bestProfilesList!=null && !bestProfilesList.isEmpty() )
            {

                this.addNewPage();

                previousYLevel =  currentYLevel; // - 10;

                float y = addTitle(previousYLevel, lmsg( "g.BestJobsTopMatches" ), lmsg( embeddedRiasec ? "g.BestJobsTopMatchesInfoEmbedded" : "g.BestJobsTopMatchesInfo" ), null, null );

                y -= TPAD;

                int cols = 5;
                float[] colRelWids = reportData.getIsLTR() ? new float[] { 0.1f,.30f,.15f,.15f,.15f } : new float[] { 0.15f,0.15f,0.15f, .3f, .1f };

                // First create the table
                PdfPCell c;

                // First, add a table
                PdfPTable t = new PdfPTable( cols );

                setRunDirection(t );
                // float importanceWidth = 25;

                t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
                t.setWidths( colRelWids );
                t.setLockedWidth( true );
                t.setHorizontalAlignment( Element.ALIGN_CENTER );
                // t.setKeepTogether( true );
                setRunDirection( t );
                t.setHeaderRows( 1 );


                // Create header
                c = new PdfPCell( new Phrase( lmsg( "g.Rank"), fontWhite ) );
                c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setColspan( 1 );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg( "g.JobTitle" ), fontWhite ) );
                c.setColspan( 1 );
                c.setBorder( Rectangle.TOP ); // | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg( "g.BestJobsMatchInterestsOnly" ), fontWhite ) );
                c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);

                /*
                c = new PdfPCell( new Phrase( lmsg( "g.BestJobsMatchAbilityAndFitOnly" ), fontWhite ) );
                c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);
                */

                c = new PdfPCell( new Phrase( lmsg( "g.BestJobsMatchEducationExperience" ), fontWhite ) );
                c.setBorder( Rectangle.TOP );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase( lmsg( "g.BestJobsMatchOverall" ), fontWhite ) );
                c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT :  Rectangle.TOP | Rectangle.LEFT );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);


                int counter = 0;

                // OK, header done, now for the profiles
                for( Profile p : bestProfilesList )
                {
                    counter++;

                    c = new PdfPCell( new Phrase( Integer.toString(counter), fontSmall ) );

                    if( counter== bestProfilesList.size() )
                        c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT : Rectangle.TOP | Rectangle.RIGHT );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setColspan( 1 );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER);
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell( new Phrase( tctrans( p.getSoc()==null ? p.getStrParam1() : p.getSoc().getTitleSingular(), false ), fontSmall ) );
                    c.setColspan( 1 );
                    if( counter== bestProfilesList.size() )
                        c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );
                    else
                        c.setBorder( Rectangle.TOP );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( 25 );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);

                    // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                    c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getFloatParam2() , false, false ), fontSmall ) );
                    if( counter== bestProfilesList.size() )
                        c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );
                    else
                        c.setBorder( Rectangle.TOP );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);

                    /*
                    // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                    c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getFloatParam4() , false, false ), fontSmall ) );
                    if( counter== bestProfilesList.size() )
                        c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );
                    else
                        c.setBorder( Rectangle.TOP );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);
                    */

                    // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                    c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getFloatParam3() , false, false ), fontSmall ) );
                    if( counter== bestProfilesList.size() )
                        c.setBorder( Rectangle.TOP | Rectangle.BOTTOM );
                    else
                        c.setBorder( Rectangle.TOP );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);

                    // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                    c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getFloatParam1() , true, false ), fontSmall ) );
                    if( counter== bestProfilesList.size() )
                        c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.RIGHT : Rectangle.TOP | Rectangle.LEFT );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 5 );
                    // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    // c.setBackgroundColor( ct2Colors.hraBlue );
                    setRunDirection( c );
                    t.addCell(c);

                }

                currentYLevel = addTableToDocument(y, t, false, true );
            }

            if( embeddedRiasec )
            {
                currentYLevel -= 8;
                java.util.List<TestEventScore> teslI = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.SCOREDINTEREST, true, false );
                teslI.addAll( getTestEventScoreListToShow(TestEventScoreType.COMPETENCY , SimCompetencyClass.INTERESTS_COMBO, true, false ));
            // Collections.sort(teslE, new TESNameComparator());

                if( !teslI.isEmpty() )
                {
                    addScoredInterestCompetencySummarySection(teslI, "s.Interests", SimCompetencyClass.SCOREDINTEREST  );
                    currentYLevel -= 8;
                }
            }

            if( eeoMatchList!=null && !eeoMatchList.isEmpty() )
            {

                addNewPage();

                previousYLevel =  currentYLevel; // - 10;

                float y = addTitle(previousYLevel, lmsg( "g.JobCategoryMatchesCand" ), null, null, null );

                y -= TPAD;

                addEEOMatchTable( eeoMatchList, y, true );
            }

            //touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTopMatchesSummarySection()" );

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
            PdfPTable t = new PdfPTable( cols );

            setRunDirection(t );
            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setKeepTogether( true );


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
            t.addCell(c);

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
            t.addCell(c);

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
            t.addCell(c);


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
            t.addCell(c);

            int counter = 0;

            boolean shade = true;

            // OK, header done, now for the profiles
            for( EeoMatch p : matchList )
            {
                counter++;

                c = new PdfPCell( new Phrase( Integer.toString(counter), font ) );

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
                t.addCell(c);

                c = new PdfPCell( new Phrase( p.getEeoTitle(), font ) );
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
                t.addCell(c);

                c = new PdfPCell( new Phrase( p.getEeoJobCategoryType().getDescription(reportData.getLocale()), font ) );
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
                t.addCell(c);


                // c = new PdfPCell( new Phrase( I18nUtils.getFormattedInteger( reportData.getLocale() , Math.round(p.getFloatParam1())), fontLarge ) );
                c = new PdfPCell( new Phrase( getDegreeOfMatchString(p.getAveragePercentMatch(), showPercentMatch, true), font ) );
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
                t.addCell(c);

                shade = !shade;
            }

            currentYLevel = addTableToDocument(y, t, true, true );
            // currentYLevel = addTableToDocument( y, touter );

            //touter.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - touter.calculateHeights();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseBestJobsReportTemplate.addEEOMatchesSummarySection()" );
            throw e;
        }
    }





    public float addScoredInterestCompetencySummarySection( List<TestEventScore> tesl, String titleKey, SimCompetencyClass scc ) throws Exception
    {
        try
        {
            // boolean showPercentile = false;

            // SimCompetencyClass scc;

            float y = currentYLevel;

            // int cols = 3;
            //int cols = showPercentile ? 4 : 3;
            float[] colRelWids = colRelWids = reportData.getIsLTR() ? new float[] { 0.25f,  .55f, .2f } : new float[] { 0.2f, .55f, .25f };


            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable touter = new PdfPTable( 3 );

            setRunDirection( touter );
            // float importanceWidth = 25;

            touter.setTotalWidth( pageWidth - 2*CT2_MARGIN - 4*CT2_BOX_EXTRAMARGIN );
            touter.setWidths( colRelWids );
            touter.setLockedWidth( true );
            touter.setHorizontalAlignment( Element.ALIGN_CENTER );
            touter.setKeepTogether( false );
            touter.setHeaderRows(1);
            // touter.set



            // Create header
            c = new PdfPCell( new Phrase( getCareerScoutMsg( titleKey ), fontWhite ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.TOP | Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.TOP | Rectangle.RIGHT  | Rectangle.BOTTOM );
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setColspan( 2 );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
            setRunDirection( c );
            touter.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Score" ), fontWhite ) );
            c.setColspan( 1 );

            //if( showPercentile )
            //    c.setBorder( Rectangle.TOP | Rectangle.BOTTOM ); // | Rectangle.RIGHT );
            //else
            //     c.setBorder( reportData.getIsLTR() ?  Rectangle.TOP | Rectangle.RIGHT  | Rectangle.BOTTOM:  Rectangle.TOP | Rectangle.LEFT  | Rectangle.BOTTOM);
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            c.setBorderWidth( lightBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            // c.setPaddingLeft( 25 );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false ));
            setRunDirection( c );
            touter.addCell(c);


            // int counter = 0;

            String descrip;
            String scoreStr;
            // String percentileStr;

            // OK, header done
            for( TestEventScore tes : tesl )
            {
                scc =  SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

                descrip = getDescriptionForInterestCompetency( tes, scc);
                scoreStr = getScoreStrForInterestCompetency( tes, scc );
                // percentileStr = getPercentileStrForCompetency( tes, scc );

                // counter++;

                c = new PdfPCell( new Phrase(  tctrans( tes.getName(), false), font ) );

                c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT  | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setColspan( 1 );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setHorizontalAlignment( Element.ALIGN_CENTER);
                setRunDirection( c );
                touter.addCell(c);



                c = new PdfPCell( new Phrase( descrip, font ) );
                c.setColspan( 1 );
                c.setBorder( Rectangle.BOTTOM);
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);

                c = new PdfPCell( new Phrase( scoreStr, font ) );
                c.setColspan( 1 );
                c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                // c.setPaddingLeft( 25 );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                touter.addCell(c);

            }

            currentYLevel = addTableToDocument(y, touter, false, true );

            return currentYLevel;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "BaseCT2ReportTemplate.addScoredInterestCompetencySummarySection() tesl.size=" + tesl.size() );

            throw e;
        }
    }



    protected String getDescriptionForInterestCompetency( TestEventScore tes, SimCompetencyClass scc) throws Exception
    {
        String nameStub = getNameStubForCompetencyName( tes.getName(), tes.getNameEnglish(), scc.getSimCompetencyClassId() );

        //LogService.logIt( "NameStub: " + nameStub);

        String key = nameStub + ".description";

        String v = getCareerScoutMsg( key );

        if( v==null )
            return "Not found: " + key; //  key;

        return v;
    }

    protected String getNameStubForCompetencyName( String name, String nameEnglish, int simCompetencyClassId ) throws Exception
    {
        String n = nameEnglish==null || nameEnglish.trim().isEmpty() ? name.trim() : nameEnglish.trim();

        return "sc." + simCompetencyClassId + "." + URLEncoder.encode(n ,"UTF8");
    }

    protected String getScoreStrForInterestCompetency( TestEventScore tes, SimCompetencyClass scc) throws Exception
    {

        // Normal Distribution Score Str
        if( scc.getIsInterest() )
        {
            if( tes.getScore() < 20 )
                return getCareerScoutMsg( "s.ScInterestVerySmall" );
            if( tes.getScore() < 40 )
                return getCareerScoutMsg( "s.ScInterestSmall" );
            if( tes.getScore() < 60 )
                return getCareerScoutMsg( "s.ScInterestMedium" );
            if( tes.getScore() < 80 )
                return getCareerScoutMsg( "s.ScInterestHigh" );
            return getCareerScoutMsg( "s.ScInterestVeryHigh" );
        }

        return "";
    }

    public String getCareerScoutMsg( String key )
    {

        return MessageFactory.getStringMessage( BEST_JOBS_BUNDLE, reportData.getLocale() , key, null );
    }




    @Override
    public long addReportToOtherTestEventId() throws Exception
    {
        return 0;
    }


    protected void addMinimalPrepNotesSection() throws Exception
    {
        // LogService.logIt( "BaseCT2ReportTemplate.addMinimalPrepNotesSection() START " );
        Calendar cal = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");
        String dtStr = df.format( cal.getTime() );

        if( reportData.getReportRuleAsBoolean( "hidedatespdf" ) )
            dtStr="***";

        String  note = "HR Avatar Use Only: " + lmsg( "g.SimIdAndVersion", new String[]{ Long.toString( reportData.getTestEvent().getSimId()) , Integer.toString(reportData.getTestEvent().getSimVersionId() ), Long.toString( reportData.getTestEvent().getTestKeyId()), Long.toString( reportData.getTestEvent().getTestEventId()), Long.toString( reportData.getReport().getReportId() ), Integer.toString( reportData.getTestKey().getProductId() ), dtStr } );

        if( reportData.getTestEvent().getUserAgent()!=null && !reportData.getTestEvent().getUserAgent().isBlank() )
            note += "\nUser-Agent: " + reportData.getTestEvent().getUserAgent();

        previousYLevel =  currentYLevel;

        // First create the table
        PdfPCell c;

        // First, add a table
        PdfPTable t = new PdfPTable( new float[] { 1f } );

        float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

        // t.setHorizontalAlignment( Element.ALIGN_CENTER );
        t.setTotalWidth( outerWid );
        t.setLockedWidth( true );
        setRunDirection( t );

        c = new PdfPCell( new Phrase( note, this.font ));
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( BaseColor.WHITE );
        c.setPaddingTop( 12 );
        c.setPaddingLeft(10);
        c.setPaddingRight(5);
        c.setPaddingBottom( 10 );
        setRunDirection( c );
        t.addCell(c);

        currentYLevel = addTableToDocument(currentYLevel, t, false, true );
    }

    protected void addPreparationNotesSection() throws Exception
    {
        try
        {
            // skip if need to.
            if( reportData.getReportRuleAsBoolean("prepnotesoff") )
            {
                addMinimalPrepNotesSection();
                return;
            }

            // addMinimalPrepNotesSection();

            // LogService.logIt(  "BaseCT2ReportTemplate.addPreparationNotesSection() START" );

            // Check for CEFR Note
            String cefrLevel = StringUtils.getBracketedArtifactFromString( reportData.getTestEvent().getOverallTestEventScore().getTextParam1(), Constants.CEFRLEVEL);
            if( cefrLevel==null || cefrLevel.isBlank() )
            {
                for( TestEventScore tes : reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()) )
                {
                    if( !tes.getSimCompetencyClass().isKSA() )
                        continue;
                    cefrLevel = StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRLEVEL);
                    if( cefrLevel!=null && !cefrLevel.isBlank() )
                        break;
                }
            }
            if( cefrLevel!=null && !cefrLevel.isBlank() )
            {
                prepNotes.add( 0, lmsg( "g.CefrPrepNote" ) );
                prepNotes.add( 0, lmsg( "g.CefrPrepNote.2" ) );
            }


            if( reportData.getReport().getIncludeNorms()>0 && hasComparisonData() )
                 prepNotes.add( 0, lmsg( "g.CT3ComparisonVsOverallNote" ) );

            List<String> customNotes = null;

            if( reportData.getReport().getTextParam3()!=null && !reportData.getReport().getTextParam3().isEmpty() )
            {
                customNotes = new ArrayList<>();

                String[] pns = reportData.getReport().getTextParam3().split("\\|");

                for( String pn : pns )
                {
                    pn=pn.trim();
                    if( pn.isEmpty() )
                        continue;
                    customNotes.add(pn);
                }
            }

            ScoreFormatType sft = reportData.getTestEvent().getScoreFormatType();

            boolean is0To100 = sft!=null && sft.equals( ScoreFormatType.NUMERIC_0_TO_100 );

            // Integer tempInteger = ReportUtils.getReportFlagIntValue( "suppressonetinfoinreports", reportData.getTestKey(), reportData.getSuborg(), reportData.getOrg(), reportData.getReport() );
            boolean suppressOnet = reportData.getReportRuleAsBoolean( "suppressonetinfoinreports" ); //  tempInteger==null || tempInteger.intValue()!=1 ? false : true;

            if( !is0To100 )
                suppressOnet = true;

            // tempInteger = ReportUtils.getReportFlagIntValue( "suppressct3comments", reportData.getTestKey(), reportData.getSuborg(), reportData.getOrg(), reportData.getReport() );
            boolean suppressCt3Specific = reportData.getReportRuleAsBoolean( "suppressct3comments" ); // tempInteger==null || tempInteger.intValue()!=1 ? false : true;

            if( !is0To100 )
                suppressCt3Specific = true;

            boolean suppressNumericComp = reportData.getReportRuleAsBoolean( "cmptynumoff" );

            if( !devel )
            {
                prepNotes.add( 0, lmsg( "g.CT3RptCaveat" ) );

                if( !suppressNumericComp && !suppressCt3Specific && is0To100 && sft!=null && sft.getSupportsBarGraphic(reportData.getReport()) )
                {
                    prepNotes.add( lmsg( "g.CT3RptGraphKey" ) );
                    prepNotes.add( lmsg( "g.CT3RptGraphKeyLinear" ) );
                    // prepNotes.add( lmsg( "g.CT3RptGraphKeyNonlinear" ) );
                }
            }
            else if( is0To100 )
            {
                prepNotes.add( 0, lmsg( "g.CT3RptCaveatDevel" ) );
                if( sft!=null &&  sft.getSupportsBarGraphic(reportData.getReport()) )
                    prepNotes.add( lmsg( "g.CT3RptGraphKey" ) );
            }


            Product p = reportData.getTestEvent().getProduct();

            if( p != null && p.getOnetSoc()!=null && !p.getOnetSoc().isEmpty() && !suppressOnet )
            {
                prepNotes.add( lmsg( "g.OnetDescrip", null ));

                prepNotes.add( lmsg( "g.OnetSocX", new String[]{p.getOnetSoc()} ));

                if( p.getOnetVersion()!=null && !p.getOnetVersion().isEmpty() )
                    prepNotes.add( lmsg( "g.OnetVersionX", new String[]{p.getOnetVersion()} ));
            }

            Calendar cal = new GregorianCalendar();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");
            String dtStr = df.format( cal.getTime() );

            if( reportData.getReportRuleAsBoolean( "hidedatespdf" ) )
                dtStr="***";

            if( customNotes !=null && !customNotes.isEmpty() )
                prepNotes = customNotes;

            Date lastScoringUpdate = p==null ? null : p.getTempDate();
            if( lastScoringUpdate!=null )
            {
                //DateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");
                //String suStr = dfs.format( lastScoringUpdate );
                String suStr = I18nUtils.getFormattedDate( getReportLocale(), lastScoringUpdate, DateFormat.SHORT );
                prepNotes.add( lmsg( "g.LastScoringUpdateX", new String[]{ suStr } ));
            }

            prepNotes.add( lmsg( "g.SimIdAndVersion", new String[]{ Long.toString( reportData.getTestEvent().getSimId()) , Integer.toString(reportData.getTestEvent().getSimVersionId() ), Long.toString( reportData.getTestEvent().getTestKeyId()), Long.toString( reportData.getTestEvent().getTestEventId()), Long.toString( reportData.getReport().getReportId() ), Integer.toString( reportData.getTestKey().getProductId() ), dtStr } ));

            if( reportData.getTestEvent().getUserAgent()!=null && !reportData.getTestEvent().getUserAgent().isBlank() )
                 prepNotes.add( "UA: " + reportData.getTestEvent().getUserAgent() );


            if( prepNotes.isEmpty() )
                return;

            addNewPage();

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.PreparationNotes" ), null, null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 1f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            if( reportData.getIsLTR() )
            {
                com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );


                //Paragraph cHdr=null;
                //Paragraph cFtr=null;
                //float spcg = 8;
                cl.setListSymbol( "\u2022");

                for( String ct : prepNotes )
                {
                    if( ct==null || ct.isEmpty() )
                        continue;

                    cl.add( new ListItem( new Paragraph( ct , getFont() ) ) );
                }

                c = new PdfPCell();
                c.setBorder( Rectangle.BOX );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPaddingTop( 8 );
                c.setPaddingLeft(10);
                c.setPaddingRight(5);
                c.setPaddingBottom( 14 );
                c.addElement( cl );
                setRunDirection( c );
                t.addCell(c);
            }

            else
            {
                PdfPTable tt = new PdfPTable( new float[] { 1f } );

                // t.setHorizontalAlignment( Element.ALIGN_CENTER );
                tt.setTotalWidth( outerWid - 20 );
                tt.setLockedWidth( true );
                setRunDirection( tt );

                c = tt.getDefaultCell();
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 5 );
                setRunDirection( c );

                for( String ct : prepNotes )
                {
                    if( ct.isEmpty() )
                        continue;

                    tt.addCell( new Phrase(ct, getFont() ) );
                }

                c = new PdfPCell();
                c.setBorder( Rectangle.BOX );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPaddingTop( 8 );
                c.setPaddingLeft(10);
                c.setPaddingRight(5);
                c.setPaddingBottom( 14 );
                c.addElement( tt );
                setRunDirection( c );
                t.addCell(c);

            }

            currentYLevel = addTableToDocument(y, t, false, true );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addPreparationNotesSection()" );

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

            // LogService.logIt( "BaseCT2ReportTemplate.addTasksInfo()" );
            addAnyCompetenciesInfo(tesl, "g.Tasks", null, null, null, "g.TaskDetail", "g.TaskDescription", null, null, false, reportData.includeInterview() ? true : false, false, true  );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTasksInfo()" );

            throw new STException( e );
        }
    }



    public void addAnyCompetenciesInfo(  java.util.List<TestEventScore> teslst, String titleKey, String titleText, String subtitleKey, String subtitleText, String detailKey, String descripKey, String caveatHeaderKey, String caveatFooterKey, boolean forceSingleColumn, boolean withInterview, boolean noInterviewLimit, boolean repeatHeadersNewPages) throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel - 10;

            if( previousYLevel <= footerHgt )
            {
                document.newPage();
                currentYLevel = pageHeight - PAD -  headerHgt;
                previousYLevel = currentYLevel;
                //currentYLevel = 0;
                //previousYLevel = 0;
            }


            // this indicates tht in cases where there is no interview question column and description would normally go on right, instead place interp on right and descrip on left.
            boolean useInterpretationInBigColumn = (!devel && !withInterview) || reportData.getReportRuleAsBoolean( "cpmtyplaceinterpinbigcolumn" ); //  && reportData.getReportRuleAsBoolean( "cpmtydescripindetailon" );
            //int interviewQsPerComp = devel ? 0 : reportData.getReport().getMaxInterviewQuestionsPerCompetency();
            // boolean numeric = this.reportData.getReport().getIncludeSubcategoryNumeric()==1;
            //boolean rating = this.reportData.getReport().getIncludeSubcategoryCategory()==1;
            //boolean norms = this.reportData.getReport().getIncludeSubcategoryNorms()==1;
            //boolean interpretation = reportData.getReport().getIncludeSubcategoryInterpretations()==1;
            String leftColumnTitle = reportData.getReportRuleAsString( "cmptyleftcolumntitle" );
            String rightColumnTitle = reportData.getReportRuleAsString( "cmptyrightcolumntitle" );


            //boolean includeTesNorm = norms;

            // rating = false;

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

            // LogService.logIt( "BaseCT2ReportTemplate.addAnyCompetenciesInfo() titleKey=" + titleKey + ", subtitleKey=" + subtitleKey + ", )subtitle=" + (subtitleKey==null ? "" : lmsg( subtitleKey ) )  );

            if( titleText==null )
                titleText = lmsg( titleKey );

            else if( titleText.equalsIgnoreCase( "[HIDE]" ) || titleText.equalsIgnoreCase( "[HIDEDETAIL]" ) )
                titleText  = "";

            if( subtitleText==null )
                subtitleText = subtitleKey==null || subtitleKey.isEmpty() ?  null : lmsg( subtitleKey );

            else if( subtitleText.equalsIgnoreCase( "[HIDE]" ) )
                subtitleText  = "";

            float y;

            float titleHeight = 0;

            if( (titleText!=null && !titleText.isBlank()) || (subtitleText!=null && !subtitleText.isBlank()) )
            {
                titleHeight = this.getTitleHeight(titleText, subtitleText);

                // if need a new page just for title, do so.
                if( previousYLevel>0 )
                {
                    float ulY = previousYLevel - PAD - titleHeight;

                    if( ulY < footerHgt + 3*PAD )
                    {
                        document.newPage();
                        currentYLevel = pageHeight - PAD -  headerHgt;
                        previousYLevel = currentYLevel;
                    }
                }
                // y = addTitle(previousYLevel, titleText, subtitleText );
            }

            // if( 1==1 )
            //     y = addContinuedNextPage( y, null );

            boolean singleCol = forceSingleColumn || reportData.getReportRuleAsBoolean( "cmptydetailcolumn2off" );

            // LogService.logIt( "BaseCT2ReportTemplate.addAnyCompetenciesInfo() singleCol=" + singleCol  );

            if( singleCol )
                withInterview = false;

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = singleCol ? new PdfPTable( 1 ) : ( new PdfPTable( reportData.getIsLTR() ? new float[] { 3.5f, 5.5f } : new float[] { 5.5f, 3.5f } ) );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            // This tells iText to always use the first row as a header on subsequent pages.
            if( repeatHeadersNewPages )
                t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( BaseColor.WHITE );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );

            // Add header row.
            if( leftColumnTitle==null )
                leftColumnTitle = lmsg( detailKey );

            c = new PdfPCell(new Phrase( leftColumnTitle, fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setPadding( 1 );
            c.setPaddingBottom( 3 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent( singleCol ?  new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, true, true, true ) : new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
            setRunDirection( c );
            t.addCell( c );


            if( !singleCol )
            {
                if( rightColumnTitle == null )
                {
                    if( devel )
                        rightColumnTitle = lmsg( "g.HelpfulTips" );

                    else if( useInterpretationInBigColumn )
                        rightColumnTitle = lmsg( withInterview ? "g.InterviewGuide" : "g.Interpretation" );
                    else
                        rightColumnTitle = lmsg(withInterview || descripKey==null || descripKey.isBlank() ? "g.InterviewGuide" : descripKey );
                }

                c = new PdfPCell(new Phrase( rightColumnTitle, fontLmWhite));
                // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 1 );
                c.setPaddingBottom( 3 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setPaddingRight( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.hraBlue );
                c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false ));
                setRunDirection( c );
                t.addCell( c );
            }

            addAnyCompetenciesInfoToTable(tesl,
                                           caveatHeaderKey,
                                           caveatFooterKey,
                                           singleCol,
                                           withInterview,
                                           noInterviewLimit,
                                           useInterpretationInBigColumn,
                                           t,
                                           outerWid);


            float t2Height = t.calculateHeights();

            float ulY = previousYLevel - 2*PAD - t2Height - titleHeight;

            // both won't fit. Add a new page, then add title
            if( ulY < footerHgt + 3*PAD )
            {
                // see if title plus header plus first row is too much
                ulY = previousYLevel - 2*PAD - titleHeight - getHeaderPlusFirstRowHeight( t );

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    currentYLevel = pageHeight - PAD -  headerHgt;
                    previousYLevel = currentYLevel;
                }

                // otherwise we should be OK.
            }

            // add title
            y = addTitle(previousYLevel, titleText, subtitleText, null, null );

            currentYLevel = addTableToDocument(y, t, false, true );

            for( TestEventScore tes : tesl )
            {
                if( tes.getSimCompetencyClass().isScoredChat() )
                    addScoredChatDetailInfoTable( tes );
            }
        }

        catch( Exception e )
        {
            LogService.logIt(e, "BaseCT2ReportTemplate.addAnyCompetenciesInfo() titleKey=" + titleKey );

            throw new STException( e );
        }
    }






    protected void addAnyCompetenciesInfoToTable( java.util.List<TestEventScore> tesl, String caveatHeaderKey, String caveatFooterKey, boolean forceSingleColumn, boolean withInterview, boolean noInterviewLimit, boolean placeInterpretationInRightColumn, PdfPTable t, float outerWid) throws Exception
    {
        try
        {
            if( tesl.size() <= 0 )
                return;

            boolean hasSpectrum = false;

            for( TestEventScore tes : tesl )
            {
                if( tes.getReportFileContentTypeId()==ScorePresentationType.SPECTRUM.getScorePresentationTypeId() )
                    hasSpectrum=true;

                if( tes.getIncludeNumericScoreInResults() && reportData.hasProfile() )
                    tes.setProfileBoundaries(reportData.getProfileEntryData(tes.getName(), tes.getNameEnglish() ) );
            }

            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            boolean useGrayBg = false;
            // For each competency
            for( TestEventScore tes : tesl )
            {
                addTesCompetencyInfoToTable(tes,
                                                hasSpectrum,
                                                caveatHeaderKey,
                                                caveatFooterKey,
                                                forceSingleColumn,
                                                withInterview,
                                                noInterviewLimit,
                                                placeInterpretationInRightColumn,
                                                t,
                                                outerWid,
                                                useGrayBg ? graybg : BaseColor.WHITE );
                useGrayBg = !useGrayBg;
            } // each competency
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addAnyCompetenciesInfoToTable() " );

            throw new STException( e );
        }
    }



    protected void addTesCompetencyInfoToTable( TestEventScore tes, boolean hasSpectrum, String caveatHeaderKey, String caveatFooterKey, boolean forceSingleColumn, boolean withInterview, boolean noInterviewLimit, boolean useInterpretationInBigColumn, PdfPTable t, float outerWid, BaseColor forceBackgroundColor) throws Exception
    {
        try
        {
            int interviewQsPerComp = devel ? 0 : reportData.getReport().getMaxInterviewQuestionsPerCompetency();
            boolean numeric = this.reportData.getReport().getIncludeSubcategoryNumeric()==1 && !reportData.getReportRuleAsBoolean( "cmptynumoff" );
            boolean graphic = this.reportData.getReport().getIncludeCompetencyColorScores()==1 && !reportData.getReportRuleAsBoolean( "cmptygrphoff" );
            boolean rating = this.reportData.getReport().getIncludeSubcategoryCategory()==1;
            boolean norms = this.reportData.getReport().getIncludeSubcategoryNorms()==1 && !reportData.getReportRuleAsBoolean( "skipcomparisonsection" );
            boolean interpretation = reportData.getReport().getIncludeSubcategoryInterpretations()==1 && !reportData.getReportRuleAsBoolean( "cmptytxtoff" );
            // boolean metas = !reportData.getReportRuleAsBoolean( "cmptymetasoff" );
            boolean useScoreTextAsNumScore = reportData.getReportRuleAsBoolean( "cmptyscrtxtasnum" );
            boolean singleCol = forceSingleColumn || reportData.getReportRuleAsBoolean( "cmptydetailcolumn2off" );
            boolean hideStandardInterpText = reportData.getReportRuleAsBoolean( "cmptystdinterptextoff" );
            String interpretationTitle = reportData.getReportRuleAsString( "cmptystdinterptitle" );
            String descriptionTitle = reportData.getReportRuleAsString( "cmptystddescriptitle" );

            if( interpretationTitle!=null )
                interpretationTitle=interpretationTitle.trim();
            if( descriptionTitle!=null )
                descriptionTitle=descriptionTitle.trim();

            CefrScoreType cefrScoreType  = CefrUtils.getCefrScoreTypeForTes(tes);
            if( cefrScoreType!=null && cefrScoreType.equals( CefrScoreType.UNKNOWN))
                cefrScoreType=null;

            if( forceBackgroundColor==null )
                forceBackgroundColor=BaseColor.WHITE;
            // LogService.logIt( "BaseCT2ReportTemplate.addAnyCompetenciesInfo() AAA.1 interpretationTitle=" + interpretationTitle + ", descriptionTitle=" + descriptionTitle );

            // Indicates that competency detail should not have description.
            boolean competencyDescriptionInDetail = !reportData.getReportRuleAsBoolean( "cpmtydescripindetailoff" ); //  && reportData.getReportRuleAsBoolean( "cpmtydescripindetailon" );

            // this indicates tht in cases where there is no interview question column and description would normally go on right, instead place interp on right and descrip on left.
            // boolean useInterpretationInBigColumn = placeInterpretationInRightColumn || reportData.getReportRuleAsBoolean( "cpmtyplaceinterpinbigcolumn" ); //  && reportData.getReportRuleAsBoolean( "cpmtydescripindetailon" );

            // LogService.logIt( "BaseCT2ReportTemplate.addAnyCompetenciesInfo() AAA.1 useInterpretationInBigColumn " + useInterpretationInBigColumn + ", competencyDescriptionInDetail=" + competencyDescriptionInDetail+ ", singleCol=" + singleCol + ", withInterview=" + withInterview );

            // if not single column and interview turned off, the descrip will go into the interview area so don't include it in the detailed score unless swapping with interpretation.
            if( !singleCol && !withInterview )
                competencyDescriptionInDetail = useInterpretationInBigColumn ? true : false;

            // single column or with interview, don't move interpretation
            if( singleCol || withInterview )
                useInterpretationInBigColumn = false;

            // LogService.logIt( "BaseCT2ReportTemplate.addAnyCompetenciesInfo() AAA.2 useInterpretationInBigColumn " + useInterpretationInBigColumn + ", singleCol=" + singleCol + ", withInterview=" + withInterview );

            int scrDigits = reportData.getReport().getIntParam3() >= 0 ? reportData.getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();

            boolean useSolidBarGraphs = graphic && reportData.getReportRuleAsBoolean( "competencycoloriconasgraph" ); // !=null && !reportData.getReportRule( "competencycoloriconasgraph" ).equals( "0");

            boolean shwGrph = showGraphForTes( tes );
            boolean shwInterp = showInterpretationForTes( tes );

            if( tes.getReportFileContentTypeId()!=ScorePresentationType.SPECTRUM.getScorePresentationTypeId() )
                hasSpectrum = false;

            if( !graphic  && !shwGrph )
                hasSpectrum=false;

            if( hasSpectrum )
                singleCol=false;

            boolean includeTesNorm = norms;

            // First create the table
            PdfPCell c;

            PdfPTable compT;
            PdfPTable col2T;
            PdfPTable igT;
            PdfPTable spectrumT;
            PdfPTable spectrumGraphT;
            String[] spectrum;

            // PdfPTable numGrphT;
            ScoreCategoryType sct;
            Image dotImg;


            // java.util.List<String> tips;
            java.util.List<InterviewQuestion> igL;


            InterviewQuestion ig;

            Phrase ep = new Phrase( "", getFontSmall() );

            // Phrase p;

            String scoreText;
            String scoreValue;
            // String ctxt = reportData.getReportRule( "hidecaveats" );
            boolean hideCaveats = reportData.getReportRuleAsBoolean( "hidecaveats" ) || reportData.getReportRuleAsBoolean( "cmptymetasoff" ); //  = ctxt != null && ctxt.equals( "1" );

            // hideCaveats=true;

            // String caveatText;

            // LogService.logIt( "BaseCT2ReportTemplate.addAnyCompetenciesInfo() DDD useInterpretationInBigColumn " + useInterpretationInBigColumn );
            // LogService.logIt( "BaseCT2ReportTemplate.addAnyCompetenciesInfo() adding " + tes.getName() + ", " + tes.getNameEnglish() );
            includeTesNorm = norms && SimCompetencyClass.getValue( tes.getSimCompetencyClassId() ).getSupportsPercentiles() && tes.getIncludeNumericScoreInResults();

            int compTCols = 3;

            // First do the score info.
            if( singleCol )
                compT = new PdfPTable( reportData.getIsLTR() ? new float[] {6,4, 18} :  new float[] {4,4,6} );

            else
            {
                compTCols = 2;
                compT = new PdfPTable( reportData.getIsLTR() ? new float[] {6,4} :  new float[] {4,6} );
            }

            compT.setHorizontalAlignment( singleCol ? ( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT ) : Element.ALIGN_CENTER );

            if( singleCol )
                compT.setTotalWidth(0.9f*outerWid*9f/9f );
            else
                compT.setTotalWidth(0.9f*outerWid*3.5f/9f );

            compT.setLockedWidth(true);
            setRunDirection( compT );

            c = compT.getDefaultCell();
            c.setBackgroundColor( forceBackgroundColor );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );
            c.setPadding( 1 );

            sct = tes.getScoreCategoryType();
            dotImg = getScoreCategoryImg( sct, true );

            // Name Cell
            c = new PdfPCell( new Phrase( tctrans( reportData.getCompetencyName(tes),false)  , getFontBold() ) );
            c.setBackgroundColor( forceBackgroundColor );
            c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setPadding( 1 );
            setRunDirection( c );
            compT.addCell( c );

            // Dot Cell
            c = rating && dotImg!=null ?  new PdfPCell( dotImg ) : new PdfPCell( new Phrase( "" , getFontSmall() ) );
            c.setBackgroundColor( forceBackgroundColor );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setPadding( 1 );
            setRunDirection( c );
            compT.addCell( c );

            // finish the name row
            if( singleCol )
            {
                c = new PdfPCell( new Phrase( "" ) );
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.NO_BORDER );
                compT.addCell( c );
            }

            scoreText = getScoreTextForCompetency( tes ); // reportData.getCompetencyScoreText( tes ); // tes.getScoreText();

            if( scoreText == null )
                scoreText = "";

            if( !useScoreTextAsNumScore && ( !interpretation || !tes.getIncludeNumericScoreInResults() || !shwInterp ) )
                scoreText = "";

            if( !useScoreTextAsNumScore )
                scoreText = tctrans( scoreText, false );

            if( cefrScoreType!=null )
            {
                if( reportData.equivSimJUtils!=null )
                {
                    if( reportData.te.getSimXmlObj()==null )
                    {
                        if( reportUtils==null )
                            reportUtils  = new ReportUtils();

                        reportUtils.loadTestEventSimXmlObject(reportData.te);
                    }

                    ScoreCategoryType cefrScoreCategoryType = CefrUtils.getCefrScoreCategoryType(reportData.te.getSimXmlObj(), cefrScoreType, reportData.getTestEvent().getScoreColorSchemeType() );

                    String s = reportData.equivSimJUtils.getCompetencyScoreText( tes.getName(), tes.getNameEnglish(), cefrScoreCategoryType.getScoreCategoryTypeId(), cefrScoreType.getNumericEquivalentScore(reportData.getTestEvent().getScoreColorSchemeType()) );
                    if( s!=null && !s.isBlank() )
                    {
                        LogService.logIt( "Found CEFR-Language-Equivalent COMPETENCY Score Text=" + s );
                        scoreText = s;
                    }
                }

                String cefrScoreText = CefrUtils.getCefrScoreTextForTes( reportData.getLocale(), tes );
                if( cefrScoreText!=null && !cefrScoreText.isBlank() )
                {
                    // cefrScoreText = tctrans( cefrScoreText, false);
                    scoreText = lmsg( "g.CefrEquivScoreText") + " " + cefrScoreText + (scoreText.isBlank() ? "" : "\n\n" + lmsg("g.GeneralScoreText") + " ") + scoreText;
                }
            }


            // Score && Percentile
            if( numeric || norms || cefrScoreType!=null )
            {
                String scoreStr;

                if( tes.getIncludeNumericScoreInResults() )
                {
                    scoreValue = I18nUtils.getFormattedNumber(reportData.getLocale(), tes.getScore(), scrDigits );

                    if( useScoreTextAsNumScore )
                    {
                        scoreValue = ReportUtils.getScoreValueFromStr(scoreText);

                        if( scoreValue==null )
                            scoreValue = "";

                        // scoreValue = scoreText;
                    }

                    scoreStr = numeric ? lmsg("g.ScoreC") + " " + scoreValue : "";

                    if( cefrScoreType!=null )
                        scoreStr += (!scoreStr.isBlank() ? "\n" : "") + lmsg("g.CefrEquivScore") + " " + cefrScoreType.getName(reportData.getLocale());

                    c = new PdfPCell( new Phrase( scoreStr, fontSmall ) );
                }

                else
                {
                    scoreStr = numeric ? lmsg("g.ScoreC") + " -" : "";
                    if( cefrScoreType!=null )
                        scoreStr += (!scoreStr.isBlank() ? "\n" : "") + lmsg("g.CefrEquivScore") + " " + cefrScoreType.getName(reportData.getLocale());
                    c = new PdfPCell( new Phrase( scoreStr, fontSmall ) );
                }

                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.NO_BORDER );
                // c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setColspan(compTCols);
                c.setPadding( 1 );
                // c.setBackgroundColor( BaseColor.LIGHT_GRAY);
                c.setPaddingBottom( 5 );
                setRunDirection( c );
                compT.addCell( c );

                StringBuilder srcStr = new StringBuilder();

                if( includeTesNorm || norms )
                    srcStr.append(lmsg("g.PercentileC") + " " + ( includeTesNorm && tes.getPercentile()>=0 ? Integer.toString((int) tes.getPercentile())+NumberUtils.getPctSuffix( reportData.getLocale(), tes.getPercentile(), 0 ) : lmsg("g.NA") ) );

                c = new PdfPCell( new Phrase( srcStr.toString(), fontSmall ) );
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.NO_BORDER );
                // c.setHorizontalAlignment( reportData.getIsLTR() ?  Element.ALIGN_RIGHT : Element.ALIGN_LEFT );
                c.setColspan(compTCols);
                c.setPadding( 1 );
                c.setPaddingBottom( 5 );
                c.setPaddingRight( 2 );
                setRunDirection( c );
                compT.addCell( c );

                //if( singleCol )
                //{
                //    c = new PdfPCell( new Phrase( "" ) );
               //     c.setBorder( Rectangle.NO_BORDER );
                //    compT.addCell( c );
                //}
            }

            // ScoreGraphCell
            if( numeric || ( !hasSpectrum && graphic  && shwGrph ) )
            {
                c = new PdfPCell( new Phrase( "" , fontXLarge ) );
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.NO_BORDER );
                c.setColspan( 2 );
                c.setPadding( 9 );

                if(  !hasSpectrum && graphic && shwGrph && tes.getIncludeNumericScoreInResults() )
                {
                    c.setFixedHeight(reportData.getTestEvent().getUseBellGraphs() ? BELL_GRAPH_CELL_HEIGHT : BAR_GRAPH_CELL_HEIGHT);
                    c.setCellEvent( useSolidBarGraphs ? new CT2SolidBarGraphicCellEvent( tes , reportData.getR2Use(), sct, reportData.getTestEvent().getScoreColorSchemeType(), reportData.p, ct2Colors, devel ) :
                                    new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, devel, false, reportData.getTestEvent().getUseBellGraphs(), true, 0 ) );
                }

                //numGrphT.addCell( c );
                setRunDirection( c );
                compT.addCell( c );

                // finish row
                if( singleCol )
                {
                    c = new PdfPCell( new Phrase( "" ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    compT.addCell( c );
                }

            }

            String compDescrip = reportData.getCompetencyDescription( tes );

            if( compDescrip==null )
                compDescrip="";

            compDescrip = compDescrip.trim();

            compDescrip = tctrans( compDescrip, false );

            if( interpretationTitle==null  )
                interpretationTitle = lmsg( "g.InterpretationC" );

            if( descriptionTitle==null  )
                descriptionTitle = lmsg( "g.DescriptionC" );

            if( competencyDescriptionInDetail && !compDescrip.isEmpty() )
            {
                if( descriptionTitle!=null && !descriptionTitle.isBlank() )
                {
                    c = new PdfPCell( new Phrase( descriptionTitle , fontSmallItalic ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setColspan(compTCols);
                    // c.setColspan( singleCol ? 3 : 2 );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 2 );
                    setRunDirection( c );
                    compT.addCell( c );
                }

                //Paragraph pp = new Paragraph( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), fontSmall );
                //pp.setAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );

                Phrase pph = new Phrase( compDescrip, fontSmall );
                c = new PdfPCell( pph );
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.NO_BORDER );
                c.setVerticalAlignment( Element.ALIGN_TOP );
                // c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setColspan(compTCols);
                // c.setColspan( singleCol ? 3 : 2 );
                c.setPadding( 1 );
                c.setPaddingBottom(4);
                setRunDirection( c );
                compT.addCell( c );
            }

            if( interpretation && shwInterp && !useInterpretationInBigColumn && tes.getIncludeNumericScoreInResults() )
            {
                if( interpretationTitle!=null && !interpretationTitle.isBlank() )
                {
                    c = new PdfPCell( new Phrase( interpretationTitle , fontSmallItalic ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    c.setColspan(compTCols);
                    // c.setColspan( singleCol ? 3 : 2 );
                    c.setPadding( 1 );
                    c.setPaddingBottom( 2 );
                    setRunDirection( c );
                    compT.addCell( c );
                }

                if( !hideStandardInterpText )
                {
                    Phrase pph = new Phrase( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), fontSmall );
                    c = new PdfPCell( pph );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment( Element.ALIGN_TOP );
                    // c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    c.setColspan(compTCols);
                    // c.setColspan( singleCol ? 3 : 2 );
                    c.setPadding( 1 );
                    setRunDirection( c );
                    compT.addCell( c );
                }
            }



            // reset score text if needed.
            if( !interpretation )
                scoreText = "";

            // parse out any keys.
            scoreText = ReportUtils.getScoreTextFromStr( scoreText );

            if( scoreText==null )
                scoreText = "";

            //Phrase pg;
            // java.util.List<String[]> topicCaveatList = reportData.getReportRuleAsBoolean( "cmptytopicsoff" ) ? new ArrayList<>() :  ReportUtils.getParsedTopicScores(reportData.getCaveatList(tes), reportData.getLocale(), tes.getSimCompetencyClassId() );

            //if( !topicCaveatList.isEmpty() && reportData.simJUtils!=null && reportData.equivSimJUtils!=null )
            //    reportData.swapTopicNamesForLang( topicCaveatList );

            // StringBuilder csb = new StringBuilder();
            String cHdrStr =( caveatHeaderKey != null && !caveatHeaderKey.isEmpty() ? lmsg( caveatHeaderKey ) + "\n" : "" );
            String cFtrStr = ( caveatFooterKey != null && !caveatFooterKey.isEmpty() ? "\n" + lmsg( caveatFooterKey ) : "" );

            List<CaveatScore> csl = new ArrayList<>();
            List<CaveatScore> topicCsl = new ArrayList<>();
            //CaveatScore singleTopicCaveatScore = null;
            //CaveatScore singleNoteCaveatScore = null;
            for( CaveatScore cs : reportData.getCaveatScoreList(tes) )
            {
                cs.setLocale( reportData.getLocale() );
                if( !cs.getHasValidInfo() )
                    continue;
                if( cs.getIsTopic() )
                    topicCsl.add(cs);
                else
                    csl.add(cs);
            }
            
            if( !topicCsl.isEmpty() && reportData.simJUtils!=null && reportData.equivSimJUtils!=null )
                reportData.swapTopicNamesForLangForCaveatScores( topicCsl );
            
            if( topicCsl.size()==1 )
            {
                //singleTopicCaveatScore=topicCsl.get(0);
                csl.add(topicCsl.get(0));
                topicCsl.clear();
            }
            if( tes.getUsesPercentCorrectScoring() )
            {
                CaveatScore cs = new CaveatScore( csl.size(), CaveatScoreType.SCORE_TEXT.getCaveatScoreTypeId(), "* " + lmsg("g.CompetencyUsesPercentCorrectScoring"), reportData.getLocale() );
                csl.add(cs);
                //csb.append( "\n* " + lmsg("g.CompetencyUsesPercentCorrectScoring") + "\n" );
            }

            String scoreTextP2 =null;
            if( tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() &&
                reportData.getReport().getIncludeWritingSampleInfo()==1 )
                scoreTextP2 = lmsg( "g.PleaseSeeEssayBelow" );

            else if( ( tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() || tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() ) &&
                reportData.getReport().getIncludeWritingSampleInfo()==1 )
                scoreTextP2 = lmsg( "g.PleaseSeeAudioSampleTextBelow" );

            
            PdfPTable scoreTextAndCaveatTable = getScoreTextAndCaveatScoreTable(scoreText, cHdrStr, cFtrStr, scoreTextP2, csl, getFontSmall(), tes.getSimCompetencyClassId(), hideCaveats );
            
            //for( CaveatScore ct : reportData.getCaveatScoreList(tes) )
            //{
            //    if( !ct.getHasValidInfo())
            //        continue;
            //    if( ct.getIsTopic() ) //  .startsWith( Constants.TOPIC_KEY + "~" ) )
            //        continue;
             //   // ct = tctrans( ct, false );
            //    if( csb.length()>=0 )
            //        csb.append( "\n" );
            //    csb.append( "\u2022 " + ct );
            //}
            //if( topicCaveatList.size()==1 )
            //{
            //    String[] ct = topicCaveatList.get(0);
            //    csb.append( "\u2022 " + ct[1] + ": " + ct[2] + "\n" );
            //    topicCaveatList.clear();
            //}
            //if( tes.getUsesPercentCorrectScoring() )
            //    csb.append( "\n* " + lmsg("g.CompetencyUsesPercentCorrectScoring") + "\n" );


            //String cssb = csb.length()>0 ?  cHdrStr + csb.toString() + cFtrStr : "";

            //if( !cssb.isEmpty() && !hideCaveats )
            //    scoreText += "\n\n" + cssb;

            //if( tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() &&
             //   reportData.getReport().getIncludeWritingSampleInfo()==1 )
             //   scoreText += "\n\n" + lmsg( "g.PleaseSeeEssayBelow" );

            //else if( ( tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() || tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() ) &&
            //    reportData.getReport().getIncludeWritingSampleInfo()==1 )
            //    scoreText += "\n\n" + lmsg( "g.PleaseSeeAudioSampleTextBelow" );

            // ScoreText Cell

            if( interpretation && shwInterp && !useInterpretationInBigColumn && tes.getIncludeNumericScoreInResults() )
            {
                c = new PdfPCell();
                // c = new PdfPCell( new Phrase( scoreText, getFontSmall() )  );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( forceBackgroundColor );
                // c.setBackgroundColor( BaseColor.ORANGE );
                c.setPadding(0);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                c.setColspan( singleCol ? 3 : 2 );
                c.setPaddingTop( 5 );
                c.addElement(scoreTextAndCaveatTable);
                setRunDirection( c );

                if( !scoreText.isBlank() || (scoreTextP2!=null && !scoreTextP2.isBlank()) || (!hideCaveats && !csl.isEmpty()) )
                    compT.addCell(c);
            }

            // add to table.
            c = new PdfPCell();
            c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
            c.setBorderWidth( 0.5f );
            c.setPadding(1);
            
            c.setBackgroundColor( forceBackgroundColor );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            // c.setRowspan( hasSpectrum ? 2 : 1 );
            c.addElement( compT );
            setRunDirection( c );
            t.addCell( c );

            if( !singleCol )
            {
                col2T = new PdfPTable( 1 );
                col2T.setTotalWidth(outerWid*5.5f/9f );
                col2T.setLockedWidth(true);
                col2T.setHorizontalAlignment( Element.ALIGN_CENTER );

                if( hasSpectrum )
                {
                    spectrumT = new PdfPTable( new float[] {2.5f,8f,2.5f} );
                    spectrumT.setTotalWidth(0.95f * outerWid*5.5f/9f );
                    spectrumT.setLockedWidth(true);
                    spectrumT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    setRunDirection( spectrumT );

                    c = spectrumT.getDefaultCell();
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setPadding( 2 );
                    setRunDirection( c );

                    spectrum = ReportData.getSpectrumVals(tes);
                    c = new PdfPCell( new Phrase( spectrum[0], fontLightItalic ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setPadding(2);
                    c.setPaddingRight( 1 );
                    c.setHorizontalAlignment( Element.ALIGN_RIGHT );
                    // c.setBackgroundColor( BaseColor.ORANGE);

                    c.setPaddingBottom( 8 );
                    setRunDirection( c );
                    spectrumT.addCell(c);


                    // graph
                    c = new PdfPCell( new Phrase( "" , fontXLarge ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setColspan( 1 );
                    c.setPadding( 2 );

                    spectrumGraphT = new PdfPTable( 1 );
                    spectrumT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    PdfPCell c2 = new PdfPCell( new Phrase( "" , fontXLarge ) );
                    c2.setBackgroundColor( forceBackgroundColor );
                    c2.setBorder( Rectangle.NO_BORDER );
                    c2.setColspan( 2 );
                    c2.setPadding( 2 );
                    c2.setFixedHeight(reportData.getTestEvent().getUseBellGraphs() ? BELL_GRAPH_CELL_HEIGHT : BAR_GRAPH_CELL_HEIGHT);
                    c2.setCellEvent( useSolidBarGraphs ? new CT2SolidBarGraphicCellEvent( tes , reportData.getR2Use(), sct, reportData.getTestEvent().getScoreColorSchemeType(), reportData.p, ct2Colors, devel ) :
                                    new CT2SummaryScoreGraphicCellEvent( tes , reportData.getR2Use(), reportData.p, sct, reportData.getTestEvent().getScoreColorSchemeType(), baseFontCalibri, true, ct2Colors, devel, false, reportData.getTestEvent().getUseBellGraphs(), true, 1 ) );

                    // c2.setBackgroundColor( BaseColor.LIGHT_GRAY);

                    spectrumGraphT.addCell(c2);

                    setRunDirection( c );
                    c.addElement( spectrumGraphT );
                    spectrumT.addCell(c);

                    // High end
                    c = new PdfPCell( new Phrase( spectrum[1], fontLightItalic ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setPadding(2);
                    c.setPaddingLeft( 1 );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setPaddingBottom( 8 );
                    setRunDirection( c );
                    spectrumT.addCell(c);

                    c = new PdfPCell();
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER);
                    //c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );
                    //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    //c.setBorderWidth( 0.5f );
                    c.setPadding( 10 );
                    setRunDirection( c );
                    c.addElement( spectrumT );
                    col2T.addCell( c );
                }


                if( withInterview )
                {
                    // next, the interview guide. A 15 column table!
                    igT = new PdfPTable( 15 );
                    igT.setTotalWidth(0.9f * outerWid*5.5f/9f );
                    igT.setLockedWidth(true);
                    igT.setHorizontalAlignment( Element.ALIGN_CENTER );
                    setRunDirection( igT );

                    c = igT.getDefaultCell();
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setPadding( 2 );
                    setRunDirection( c );

                    int iqs = 0;

                    int maxInt = noInterviewLimit ? MAX_INTERVIEWQS_PER_COMPETENCY : interviewQsPerComp;

                    maxInt = Math.min( maxInt, MAX_INTERVIEWQS_PER_COMPETENCY );

                    igL = reportData.getInterviewQuestionList(tes, maxInt); //tes.getInterviewQuestionList( maxInt );

                    // LogService.logIt( "BaseCt2ReportTemplate.addTesCompetencyInfoToTable() XXX maxInt=" + maxInt + ", iql.size=" + igL.size() + " name=" + tes.getName() + ", tes.nameEnglish=" + tes.getNameEnglish() );

                    // for( int i=0; i<interviewQsPerComp; i++ )
                    for( int i=0; i<maxInt && i<igL.size(); i++ )
                    {
                        if( i >= igL.size() )
                            break;

                        // need a boundary
                        if( i > 0 )
                        {
                            LineSeparator ls = new LineSeparator( 1,90,BaseColor.BLACK,Element.ALIGN_CENTER, 0 );
                            Chunk chk = new Chunk( ls );
                            Phrase ph = new Phrase( chk );
                            c = new PdfPCell( ph );
                            // c.addElement( ls );
                            c.setBorder( Rectangle.NO_BORDER );
                            c.setBackgroundColor( forceBackgroundColor );
                            c.setColspan( 15 );
                            c.setPadding(6);
                            c.setPaddingBottom( 10 );
                            setRunDirection( c );
                            igT.addCell(c);
                        }

                        ig = igL.get(i);

                        // LogService.logIt( "BaseCt2ReportTemplate.addTesCompetencyInfoToTable() XXX.1 ig.question=" + ig.getQuestion() );


                        // ROW 1 - the question
                        c = new PdfPCell( new Phrase( tctrans( ig.getQuestion(), false ), getFontSmall() ) );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setColspan( 15 );
                        c.setPadding(2);
                        c.setPaddingBottom( 10 );
                        setRunDirection( c );
                        igT.addCell(c);

                        // Row 2 - Color Dots (stars)
                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement(redDot);
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        setRunDirection( c );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( redYellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setPadding( 1 );
                        setRunDirection( c );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( yellowDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        setRunDirection( c );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( yellowGreenDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        setRunDirection( c );
                        igT.addCell( c );
                        igT.addCell(ep);

                        igT.addCell(ep);
                        c =  new PdfPCell( interviewStar );
                        // c.addElement( greenDot );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setPadding( 1 );
                        setRunDirection( c );
                        igT.addCell( c );
                        igT.addCell(ep);

                        // ROW 3 - numbers
                        c = igT.getDefaultCell();
                        c.setHorizontalAlignment( Element.ALIGN_CENTER);
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setPadding( 0 );
                        setRunDirection( c );

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
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setPadding( 2 );
                        c.setPaddingBottom( 10 );
                        setRunDirection( c );


                        // row 4 - anchors
                        c = new PdfPCell( new Phrase( tctrans( ig.getAnchorLow(), false ), getFontSmall() ) );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 4 );
                        c.setPaddingBottom( 10 );
                        setRunDirection( c );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( tctrans( ig.getAnchorMed(), false ), getFontSmall() ) );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 5 );
                        c.setPaddingBottom( 10 );
                        setRunDirection( c );
                        igT.addCell(c);
                        igT.addCell(ep);

                        c = new PdfPCell( new Phrase( tctrans( ig.getAnchorHi(), false ), getFontSmall() ) );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setBorder( Rectangle.NO_BORDER );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT);
                        c.setVerticalAlignment( Element.ALIGN_TOP );
                        c.setColspan( 4 );
                        c.setPaddingBottom( 10 );
                        setRunDirection( c );
                        igT.addCell(c);

                        iqs++;
                    }

                    // LogService.logIt( "BaseCt2ReportTemplate.addTesCompetencyInfoToTable() XXX.2 iqs.size=" + iqs );
                    if( iqs >= 0 )
                    {
                        c = new PdfPCell();
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setBorder( Rectangle.NO_BORDER);
                        //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                        //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        //c.setBorderWidth( 0.5f );
                        c.setPadding( 10 );
                        setRunDirection( c );
                        c.addElement( igT );
                        col2T.addCell( c );
                    }

                }

                // else, with interpretation
                else if( useInterpretationInBigColumn )
                {
                    c = new PdfPCell( new Phrase( scoreText, getFontSmall() ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER);
                    //c.setBorder( Rectangle.BOX );
                    //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( 0.5f );
                    c.setPadding( 6 );
                    setRunDirection( c );
                    col2T.addCell( c );

                    if( !hideStandardInterpText )
                    {
                        c = new PdfPCell( new Phrase( lmsg( sct.getInterpretationKey(  tes.getSimCompetencyClassId() ) ), getFontSmall() ) );
                        c.setBackgroundColor( forceBackgroundColor );
                        c.setBorder( Rectangle.NO_BORDER);
                        //c.setBorder( Rectangle.BOX );
                        //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setBorderWidth( 0.5f );
                        c.setPadding( 6 );
                        setRunDirection( c );
                        col2T.addCell( c );
                    }
                }
                // else, with description
                else
                {
                    // String d = reportData.getCompetencyDescription( tes ); // ScoreFormatUtils.getDescripFromTextParam( tes.getTextParam1() );
                    c = new PdfPCell( new Phrase( compDescrip, getFontSmall() ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorder( Rectangle.NO_BORDER);
                    //c.setBorder( Rectangle.BOX );
                    //c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( 0.5f );
                    c.setPadding( 6 );
                    setRunDirection( c );
                    col2T.addCell( c );
                }


                c = new PdfPCell();
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 10 );
                setRunDirection( c );
                c.addElement( col2T );
                t.addCell( c );
            } // !single column


            if( !topicCsl.isEmpty() )
            {
                PdfPTable tt = new PdfPTable( new float[]{5,10} );
                tt.setTotalWidth(0.9f * outerWid );
                tt.setLockedWidth( true );
                tt.setHorizontalAlignment( Element.ALIGN_LEFT );
                setRunDirection( tt );

                c = tt.getDefaultCell();
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 2 );
                setRunDirection( c );

                // tctrans(reportData.getCompetencyName(tes),false)
                c = new PdfPCell( new Phrase( lmsg( "g.CaveatTopicScoresTitle4X", new String[]{reportData.getCompetencyName(tes)}  ), fontSmallBold ) );
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorderWidth( 0f );
                c.setColspan(2);
                c.setPadding( 2 );
                setRunDirection( c );
                tt.addCell( c );

                String[] ct;

                for( CaveatScore tcs : topicCsl )
                {
                    if( !tcs.getIsTopic() )
                        continue;
                    ct = ReportUtils.parseTopicCaveatScore(tcs, topicCsl.size()==1, tes.getSimCompetencyClassId());
                    
                    if( ct==null || ct[1]==null || ct[1].isEmpty() )
                        continue;

                    // LogService.logIt( "BaseCT2ReportTemplate.addTesCompetencyInfoToTable() ct[0]=" + ct[0] + ", ct[1]=" + ct[1] + ", ct[2]=" + ct[2] + ", topicCaveatList contains " + topicCaveatList.size() );

                    // c = new PdfPCell( new Phrase( "\u2022 " + ct[1] + ": ", fontSmall ) );
                    c = new PdfPCell( new Phrase( ct[1] + ":", fontSmall ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorderWidth( 0f );
                    c.setPadding( 2 );
                    c.setPaddingLeft(6);
                    // c.setPaddingRight( 15 );
                    setRunDirection( c );
                    tt.addCell( c );

                    c = new PdfPCell( new Phrase( ct[2], fontSmall ) );
                    c.setBackgroundColor( forceBackgroundColor );
                    c.setBorderWidth( 0f );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    tt.addCell( c );
                }

                c = new PdfPCell();
                c.setBackgroundColor( forceBackgroundColor );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 10 );
                c.setPaddingTop(2);
                c.setColspan( singleCol ? 1 : 2);
                setRunDirection( c );
                c.addElement( tt );
                t.addCell( c );
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTesCompetencyInfoToTable() " );

            throw new STException( e );
        }
    }

    public String getScoreTextForCompetency( TestEventScore tes )
    {
        return reportData.getCompetencyScoreText( tes ); // tes.getScoreText();
    }


    /**
     * Each TextAnd
     * @param tes
     * @param t
     * @param cols
     * @throws Exception
     */
    public void addScoredChatDetailInfoTable( TestEventScore tes ) throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseCT2ReportTemplate.addScoredChatDetailInfoToTable() " );

            previousYLevel =  currentYLevel;

            if( reportData.getReportRuleAsBoolean( "hidechatresponseinfo" ) )
                return;

            // Each TextAndTitle contains a full session.

            List<TextAndTitle> ttl = tes.getTextBasedResponseList(null, false);

            if( ttl==null || ttl.isEmpty() )
                return;

            previousYLevel =  currentYLevel - 10;

            if( previousYLevel <= footerHgt )
            {
                document.newPage();

                currentYLevel = 0;
                previousYLevel = 0;
            }

            // First create the table
            PdfPCell c;
            PdfPCell c2;


            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // First, add a table
            PdfPTable tOuter = new PdfPTable( 1 );
            tOuter.setTotalWidth( outerWid );
            tOuter.setLockedWidth( true );
            setRunDirection( tOuter );
            // This tells iText to always use the first row as a header on subsequent pages.
            tOuter.setHeaderRows( 0 );

            PdfPTable tOuter2 = new PdfPTable( 1 );
            tOuter2.setTotalWidth( outerWid );
            tOuter2.setLockedWidth( true );
            setRunDirection( tOuter2 );
            // This tells iText to always use the first row as a header on subsequent pages.
            tOuter2.setHeaderRows( 0 );
            tOuter2.setTableEvent( new CT2ChatBoxTableEvent(new BaseColor(0xea,0xea,0xea)));


            // First, add a table
            PdfPTable t;

            String[] pairs;
            String typeStr;
            String content;
            int typeId;
            // int sessionCount = 0;
            int rowCount;

            String title = null;
            String text;
            boolean first = true;

            for( TextAndTitle tt : ttl )
            {
                if( tt.getText()==null || tt.getText().trim().isEmpty() )
                    continue;


                title = UrlEncodingUtils.decodeKeepPlus(  tt.getTitle(), "UTF8" );
                text = UrlEncodingUtils.decodeKeepPlus(  tt.getText(), "UTF8" );

                pairs = text.split( "\\|" );

                if( pairs.length < 2 )
                    continue;

                // OK we have a valid session.
                t = new PdfPTable( new float[] { 1f,1f,1f } );
                t.setTotalWidth( outerWid*0.8f );
                // t.setTotalWidth( outerWid );
                t.setHorizontalAlignment( Element.ALIGN_CENTER );
                t.setLockedWidth( true );
                setRunDirection( t );
                t.setTableEvent( new CT2ChatBoxTableEvent(new BaseColor(0xea,0xea,0xea)));

                boolean isUser;

                rowCount=0;

                for( int idx=0; idx<pairs.length-1; idx+=2 )
                {
                    typeStr = pairs[idx].trim();
                    content = pairs[idx+1].trim();

                    if( typeStr.isEmpty() || content.isEmpty() )
                        continue;

                    typeId = Integer.parseInt( typeStr );

                    isUser = typeId==ChatMessageType.USER_MSG.getChatMessageTypeId();

                    c = new PdfPCell( new Phrase( content, this.getFontWhite() ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 4 );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                    c.setPaddingBottom(6);
                    c.setColspan(2);

                    c2 = new PdfPCell( new Phrase( "", this.getFontWhite() ) );
                    c2.setBorder( Rectangle.NO_BORDER );
                    c2.setBorderWidth( 0 );
                    c2.setPadding( 4 );
                    c2.setColspan(1);

                    if( isUser )
                    {
                        c.setCellEvent( new CT2ChatBoxCellEvent( ct2Colors.hraBlue, ct2Colors.hraBlue ));
                        // c.setBackgroundColor( ct2Colors.hraBlue );
                        setRunDirection( c );
                        t.addCell(c2);
                        t.addCell(c);
                    }

                    else
                    {
                        c.setCellEvent( new CT2ChatBoxCellEvent( BaseColor.GRAY, BaseColor.GRAY));
                        // c.setBackgroundColor( BaseColor.GRAY );
                        setRunDirection( c );
                        t.addCell(c);
                        t.addCell(c2);
                    }

                    rowCount++;
                }

                if( rowCount<=0 )
                    continue;

                // sessionCount++;

                c = new PdfPCell();
                // c.setBackgroundColor( BaseColor.WHITE );
                //c.setBorder( Rectangle.BOX );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //c.setBorderWidth( 0.5f );
                //c.setPadding( 8 );
                //c.setPaddingTop(4);
                // c.setPaddingLeft( 0 );
                c.setColspan( 1 );
                setRunDirection( c );
                c.addElement( t );
                tOuter2.addCell( c );


                if( !first )
                {
                    c = new PdfPCell( new Phrase( " ", this.getFontLarge() ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    //c.setPadding( 4 );
                    c.setColspan( 1 );
                    setRunDirection( c );
                    tOuter.addCell( c );
                }

                first = false;

                c = new PdfPCell( new Phrase( title, getFontLargeBold() ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( BaseColor.WHITE );
                // c.setPadding( 6 );
                c.setPaddingTop(6);
                c.setPaddingBottom(6);
                c.setColspan( 1 );
                setRunDirection( c );
                tOuter.addCell( c );

                c = new PdfPCell();
                // c.setBackgroundColor( BaseColor.WHITE );
                //c.setBorder( Rectangle.BOX );
                c.setBorder( Rectangle.NO_BORDER );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                //c.setBorderWidth( 0.5f );
                // c.setPadding( 4 );
                c.setPaddingTop(4);
                c.setPaddingBottom(4);
                // c.setPaddingLeft( 0 );
                c.setColspan( 1 );
                setRunDirection( c );
                c.addElement( tOuter2 );
                tOuter.addCell( c );
            }

            currentYLevel = addTableToDocument(previousYLevel, tOuter, true, true );


           //  LogService.logIt( "BaseCT2ReportTemplate.addScoredChatDetailInfoToTable()" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addScoredChatInfo()" );

            throw new STException( e );
        }

    }


    public void addWritingSampleInfo() throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseCT2ReportTemplate.addWritingSampleInfo() " + reportData.getReport().getIncludeWritingSampleInfo() );

            previousYLevel =  currentYLevel;

            if( reportData.getReport().getIncludeWritingSampleInfo()==0 )
                return;

            java.util.List<TextAndTitle> ttl = ScoreFormatUtils.getNonCompTextListTable( reportData.getTestEvent(), NonCompetencyItemType.WRITING_SAMPLE );

            if( ttl.isEmpty() )
                return;

            float y;

            // float y = addTitle( previousYLevel, lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            // t.setSplitLate( false );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = new PdfPCell(new Phrase( lmsg( "g.WritingSampleQuestion" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true));
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( lmsg( "g.Response" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false));
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.WritingSampleQuestion" ) , fontLmWhite) );
            //t.addCell( new Phrase( lmsg( "g.Response" ) , fontLmWhite) );

            // c.setBackgroundColor( BaseColor.WHITE );

            // Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            String misSpells;

            String theText;
            String transText;

            BaseColor graybg = new BaseColor(0xf4,0xf4,0xf4);
            boolean useGrayBg = true;


            // For each competency
            for( TextAndTitle tt : ttl )
            {

                useGrayBg = !useGrayBg;

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                theText = tt.getTitle();
                if( StringUtils.getHasHtml(theText) )
                    theText = StringUtils.convertHtml2PlainText(theText, true );

                c = new PdfPCell( new Phrase( theText, getFontSmall() ) );
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

                theText = tt.getText();
                if( StringUtils.getHasHtml(theText) )
                    theText = StringUtils.convertHtml2PlainText(theText, true );



                misSpells = tt.getString1();
                transText = tt.getString3();

                if( misSpells!=null && !misSpells.isEmpty() )
                {
                    theText += "\n\n[" + lmsg( "g.MisSpelledWordsC" ) + " " + misSpells + "]";
                }

                if( transText!=null && !transText.isEmpty() )
                {
                    theText += "\n\n[" + lmsg( "g.ReverseTranslatedC" ) + " " + transText + "]";
                }

                c = new PdfPCell( new Phrase( theText, getFontSmall() ) );
                c.setBackgroundColor( useGrayBg ? graybg : BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

            } // each writing sample

            float titleHeight = getTitleHeight(lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ));

            // if need a new page just for title, do so.
            if( previousYLevel>0 )
            {
                float ulY = previousYLevel - PAD - titleHeight;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    currentYLevel = pageHeight - PAD -  headerHgt;
                    previousYLevel = currentYLevel;
                }
            }

            float t2Height = t.calculateHeights();

            float ulY = previousYLevel - 2*PAD - t2Height - titleHeight;

            // both won't fit. Add a new page, then add title
            if( ulY < footerHgt + 3*PAD )
            {
                // see if title plus header plus first row is too much
                ulY = previousYLevel - 2*PAD - titleHeight - getHeaderPlusFirstRowHeight( t );

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    currentYLevel = pageHeight - PAD -  headerHgt;
                    previousYLevel = currentYLevel;
                }

                // otherwise we should be OK.
            }

            // add title
            y = addTitle(previousYLevel, lmsg( "g.WritingSampleTitle" ), lmsg( "g.WritingSampleSubtitle" ), null, null );

            currentYLevel = addTableToDocument(y, t, false, true );

           //  LogService.logIt( "BaseCT2ReportTemplate.addWritingSampleInfo()" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addWritingSampleInfo()" );

            throw new STException( e );
        }
    }


    public void addResumeSection() throws Exception
    {
        if( ReportManager.DEBUG_REPORTS )
            LogService.logIt(  "BaseCT2ReportTemplate.addResumeSection() AAA " );

        if( reportData.getR2Use().getIncludeResume()<=0 || reportData.getReportRuleAsBoolean( "resumereportsoff") || reportData.getUser()==null || reportData.getUser().getResume()==null )
            return;

        try
        {
            if( ReportManager.DEBUG_REPORTS )
                LogService.logIt(  "BaseCT2ReportTemplate.addResumeSection() BBB.1 " );

            Resume resume = reportData.getUser().getResume();
            resume.parseJsonStr();

            if( !resume.getHasAnyFormData() )
            {
                LogService.logIt(  "BaseCT2ReportTemplate.addResumeSection() BBB.2 Existing Resume has no form data." );
                return;
            }
            
            if( reportData.getR2Use().getIncludeResume()==1 && (resume.getSummary()==null || resume.getSummary().isBlank()) )
            {
                LogService.logIt(  "BaseCT2ReportTemplate.addResumeSection() BBB.2 Existing Resume has no form data." );
                return;
            }
            
            if( currentYLevel < pageHeight - PAD -  headerHgt - 10 )
            {
                addNewPage();
            }

            previousYLevel =  currentYLevel;
            
            float y = addTitle(previousYLevel, lmsg( "g.Resume" ), null, null, null );

            PdfPCell c;
            PdfPTable t;
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            Font subtitleFont = fontLargeBold;
            Font textFont = font;
            // Font italicFont = fontItalic;

            // First, add a table
            t = new PdfPTable( new float[] { 1f  } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            String tt = lmsg( "g.UpdatedOnX", new String[]{I18nUtils.getFormattedDateTime(reportData.getLocale(), resume.getLastInputDate(), reportData.getUser().getTimeZone())});
            Paragraph par = new Paragraph();
            par.add( new Chunk(lmsg("g.ResumeHdrSum") + "          ", fontLargeWhite ) );
            par.add( new Chunk(tt, fontWhite ) );
            c = new PdfPCell( par );
            c.setCellEvent(new CellBackgroundCellEvent(reportData.getIsLTR(), ct2Colors.hraBlue,true, true, true, true ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPadding( 1 );
            c.setPaddingBottom( 5 );
            c.setPaddingLeft( 25 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            //if( resume.getLastInputDate()!=null  )
            //{
            //    c = new PdfPCell(new Phrase( lmsg( "g.UpdatedOnX", new String[]{I18nUtils.getFormattedDateTime(reportData.getLocale(), resume.getLastInputDate(), reportData.getUser().getTimeZone())}) , italicFont));
            //    c.setBorder( Rectangle.NO_BORDER );
            //    c.setBorderWidth( 0 );
            //    c.setPadding( 2 );
            //    c.setPaddingBottom( 4 );
            //    setRunDirection( c );
            //    t.addCell(c);
            //}

            if( resume.getSummary()!=null && !resume.getSummary().isBlank() )
            {
                c = new PdfPCell(new Phrase( resume.getSummary() , textFont));
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                c.setPadding( 2 );
                c.setPaddingBottom( 4 );
                setRunDirection( c );
                t.addCell(c);
            }
            
            if( reportData.getR2Use().getIncludeResume()==2 )
            {

                if( resume.getObjective()!=null && !resume.getObjective().isBlank() )
                {
                    c = new PdfPCell(new Phrase( lmsg("g.Objective") , subtitleFont));
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);

                    c = new PdfPCell(new Phrase( resume.getObjective() , textFont));
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    c.setPaddingBottom( 4 );
                    setRunDirection( c );
                    t.addCell(c);
                }

                com.itextpdf.text.List cl;

                if( resume.getEducation()!=null && !resume.getEducation().isEmpty() )
                {
                    c = new PdfPCell(new Phrase( lmsg("g.Education") , subtitleFont));
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);

                    cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                    cl.setListSymbol( "\u2022");
                    cl.setIndentationLeft( 10 );
                    cl.setSymbolIndent( 10 );

                    for( ResumeEducation re : resume.getEducation() )
                    {
                        cl.add( new ListItem( 9,  re.toAiString(), textFont ) );                    
                    }

                    c = new PdfPCell();
                    c.addElement(cl);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);                
                }

                if( resume.getExperience()!=null && !resume.getExperience().isEmpty() )
                {
                    c = new PdfPCell(new Phrase( lmsg("g.Experience") , subtitleFont));
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);

                    cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                    cl.setListSymbol( "\u2022");
                    cl.setIndentationLeft( 10 );
                    cl.setSymbolIndent( 10 );

                    for( ResumeExperience re : resume.getExperience() )
                    {
                        cl.add( new ListItem( 9,  re.toAiString(), textFont ) );                    
                    }

                    c = new PdfPCell();
                    c.addElement(cl);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);                
                }

                if( resume.getOtherQuals()!=null && !resume.getOtherQuals().isEmpty() )
                {
                    c = new PdfPCell(new Phrase( lmsg("g.OtherQualifications") , subtitleFont));
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);

                    cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                    cl.setListSymbol( "\u2022");
                    cl.setIndentationLeft( 10 );
                    cl.setSymbolIndent( 10 );

                    for( String re : resume.getOtherQuals())
                    {
                        cl.add( new ListItem( 9,  re, textFont ) );                    
                    }

                    c = new PdfPCell();
                    c.addElement(cl);
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth( 0 );
                    c.setPadding( 2 );
                    setRunDirection( c );
                    t.addCell(c);                
                }
            }
            
            currentYLevel = addTableToDocument(y, t, false, true );

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addResumeSection()" );
            throw new STException( e );
        }


    }

    public void addIbmInsightSection() throws Exception
    {
        if( ReportManager.DEBUG_REPORTS )
            LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSection() AAA " );

        if( !RuntimeConstants.getBooleanValue("ibmcloudInsightOn") || reportData.getR2Use().getIncludeIbmInsight()<=0 || reportData.getReportRuleAsBoolean( "ibminsightoff") )
            return;

        try
        {
            if( ReportManager.DEBUG_REPORTS )
                LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSection() BBB " );

            TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();

            if( otes==null )
                return;

            java.util.List<TextAndTitle> ttl = otes.getTextBasedResponseList( Constants.IBMINSIGHT, false, false );

            if( ttl.isEmpty() )
                return;

            List<TextAndTitle> hraTraitTtl = new ArrayList<>();
            for( TextAndTitle tt : ttl )
            {
                if( SentinoUtils.getIsHraTrait( tt ) )
                    hraTraitTtl.add( tt );
            }

            LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSection() CCC  ttl=" + ttl.size() + ", hraTraitTtl=" + hraTraitTtl.size() );

            /*
            if( ttl.size()==1 )
            {
                TextAndTitle tt = ttl.get(0);
                if( tt.getTitle()!=null && tt.getTitle().equals(Constants.IBMLOWWORDSERROR ) )
                {
                    addIbmInsightLowWordsSection( tt );
                    return;
                }
            }
            */

            //if( previousYLevel < getMinYForNewSection() )
            addIbmInsightSubsection( hraTraitTtl );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addIbmInsightSection()" );
            throw new STException( e );
        }

    }

    /*
    public void addIbmInsightLowWordsSection( TextAndTitle tt ) throws Exception
    {
        if( reportData.getR2Use().getIncludeIbmInsight()<= 0 || reportData.getReportRuleAsBoolean( "ibminsightoff") )
            return;
        if( tt==null )
            return;

        try
        {
            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel,  lmsg( InsightTraitType.PERSONALITY_TOP.getTitleKey() ), lmsg( "g.IbmInsightInfoSubtitle" ) );

            // First create the table
            PdfPTable t = new PdfPTable( 1 );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setSplitLate( false );
            setRunDirection( t );
            t.setHeaderRows( 0 );

            String txt = tt.getText();
            if( txt.indexOf(';')>=0 )
                txt = txt.substring(0, txt.indexOf(';') );

            PdfPCell c = new PdfPCell( new Phrase( txt, fontLargeBold ) );
            c.setBorder( Rectangle.BOX  );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setColspan(1);
            c.setPadding( 10 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection( c );
            t.addCell( c );

            currentYLevel = addTableToDocument(y, t, true, true );

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addIbmInsightLowWordsSection()" );

            throw new STException( e );
        }

    }
    */



    public void addIbmInsightSubsection( java.util.List<TextAndTitle> ttlAll ) throws Exception
    {
        if( ReportManager.DEBUG_REPORTS )
            LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSubsection() AAA " );

        if( reportData.getR2Use().getIncludeIbmInsight()<= 0 || reportData.getReportRuleAsBoolean( "ibminsightoff") )
            return;

        try
        {
            if( ReportManager.DEBUG_REPORTS )
                LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSubsection() BBB ttlAll.size=" + ttlAll.size() );

            if( ttlAll.isEmpty() )
                return;

            List<HraTrait> ttl = new ArrayList<>();

            HraTrait irtx;

            for( TextAndTitle tt : ttlAll)
            {
                irtx = new HraTrait( tt );

                //if( !irtx.isValid() )
                //{
                //    LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSubsection() BBB.2 HraTrait.TextAndTitle is invalid=" + irtx.toString() );
                //    continue;
                //}

                irtx.setLocale( reportData.getLocale() );

                ttl.add( irtx);
            }

            if( ttl.isEmpty() )
            {
                // LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSubsection() BBB.3 No valid TextAndTitles found" );
                return;
            }

            // LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSubsection() BBB.4 ttl=" + ttl.size()  );
            Collections.sort( ttl );

            addNewPage();

            previousYLevel =  currentYLevel;

            float y = currentYLevel;

            y = addTitle(previousYLevel,  lmsg( "g.IbmInsightInfoTitle" ), lmsg( "g.IbmInsightInfoSubtitle"), null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 2f, 1.5f, 2f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setSplitLate( false );
            setRunDirection( t );
            t.setHeaderRows( 0 );


            c = t.getDefaultCell();
            //c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( lightBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.lightBoxBorderColor );
            setRunDirection( c );

            // InsightReportTrait irt;

            PdfPTable t2;

            boolean hasUnavailable = false;

            for( HraTrait irt : ttl )
            {
                if( !irt.isValid() )
                {
                    c = new PdfPCell( new Phrase( irt.getName(), fontBold ) );
                    // c.setBorder( Rectangle.LEFT | Rectangle.TOP  );
                    c.setBorder(Rectangle.NO_BORDER);
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setColspan(1);
                    c.setPadding( 2 );
                    c.setPaddingBottom( 2 );
                    c.setPaddingTop( 4 );
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    // c.setBackgroundColor( ct2Colors.lightergray );
                    // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    setRunDirection( c );
                    t.addCell( c );

                    c = new PdfPCell( new Phrase( lmsg("g.IbmInsightLowConfidenceMsg"), font ) );
                    //c.setBorder( Rectangle.RIGHT | Rectangle.TOP  );
                    c.setBorder(Rectangle.NO_BORDER);
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setColspan(2);
                    c.setPadding( 2 );
                    c.setPaddingBottom( 2 );
                    c.setPaddingTop( 4 );
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    // c.setBackgroundColor( ct2Colors.lightergray );
                    // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    setRunDirection( c );
                    t.addCell( c );

                    c = new PdfPCell( new Phrase( irt.getDescripStr(), font ) );
                    // c.setBorder( Rectangle.LEFT  | Rectangle.RIGHT  | Rectangle.BOTTOM );
                    c.setBorder(Rectangle.NO_BORDER);
                    c.setPaddingBottom( 4 );
                    c.setBorderWidth( lightBoxBorderWidth );
                    c.setBorderColor( ct2Colors.lightBoxBorderColor );
                    c.setColspan( 3 );
                    c.setPadding( 4 );
                    c.setPaddingLeft( 10 );
                    // c.setBackgroundColor( ct2Colors.lightergray );
                    setRunDirection( c );
                    t.addCell( c );

                    hasUnavailable = true;
                    continue;
                }


                c = new PdfPCell( new Phrase( irt.getName(), fontBold ) );
                // c.setBorder( Rectangle.LEFT | Rectangle.TOP  );
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( lightBoxBorderWidth );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingTop( 4 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.lightergray );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( irt.getHraScoreStr(), fontBold ) );
                // c.setBorder( Rectangle.TOP  );
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( lightBoxBorderWidth );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingTop( 4 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.lightergray );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );


                t2 = new PdfPTable( 1 );
                setRunDirection( t2 );

                t2.setWidthPercentage( 100 );
                // t2.setLockedWidth( true );

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setPadding( 0 );
                c.setFixedHeight(22);
                c.setCellEvent( new CT2MetaScoreGraphicCellEvent( baseFontCalibri, irt.getHraScore(), ScoreColorSchemeType.FIVECOLOR ) );
                t2.addCell(c);

                c = new PdfPCell( t2 ); // new PdfPCell( summaryCatNumericAxis );
                // c.setBorder( Rectangle.RIGHT  | Rectangle.TOP );
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                c.setPaddingTop( 4 );
                t.addCell( c );


                c = new PdfPCell( new Phrase( irt.getDescripStr(), font ) );
                // c.setBorder( Rectangle.LEFT  | Rectangle.RIGHT  );
                c.setBorder(Rectangle.NO_BORDER);
                c.setPaddingBottom( 4 );
                c.setBorderWidth( lightBoxBorderWidth );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setColspan( 3 );
                c.setPadding( 4 );
                c.setPaddingLeft( 10 );
                // c.setBackgroundColor( ct2Colors.lightergray );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( irt.getScoreTextStr(), font ) );

                // c.setBorder( Rectangle.LEFT  | Rectangle.RIGHT   | Rectangle.BOTTOM  );
                c.setBorder(Rectangle.NO_BORDER);
                c.setPaddingBottom( 4 );

                c.setBorderWidth( lightBoxBorderWidth );
                c.setBorderColor( ct2Colors.lightBoxBorderColor );
                c.setColspan( 3 );
                c.setPadding( 4 );

                c.setPaddingLeft( 10 );
                // c.setBackgroundColor( ct2Colors.lightergray );
                setRunDirection( c );
                t.addCell( c );


                //t2 = new PdfPTable( 1 );
                //setRunDirection( t2 );

                //t2.setWidthPercentage( 100 );
                // t2.setLockedWidth( true );

            }

            currentYLevel = addTableToDocument(y, t, true, true );

            // LogService.logIt( "BaseCT2ReportTemplate.addIbmInsightSection() SSS.1 currentYLevel=" +  currentYLevel );            
            if( hasUnavailable )
            {
                currentYLevel = addSimpleText( currentYLevel, lmsg("g.IbmInsightLowConfidenceNote") );
            }
            
            // LogService.logIt( "BaseCT2ReportTemplate.addIbmInsightSection() SSS.2 currentYLevel=" +  currentYLevel );            


            
            // addNewPage();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addIbmInsightSection()" );

            throw new STException( e );
        }

    }

    /*
    public void addIbmInsightSection() throws Exception
    {
        if( ReportManager.DEBUG_REPORTS )
            LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSection() AAA " );

        if( reportData.getR2Use().getIncludeIbmInsight()<= 0 || reportData.getReportRuleAsBoolean( "ibminsightoff") )
            return;

        try
        {
            if( ReportManager.DEBUG_REPORTS )
                LogService.logIt(  "BaseCT2ReportTemplate.addIbmInsightSection() BBB " );

            TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();

            if( otes==null )
                return;

            java.util.List<TextAndTitle> ttl = otes.getTextBasedResponseList( Constants.IBMINSIGHT, false, false );

            if( ttl.isEmpty() )
                return;

            //if( previousYLevel < getMinYForNewSection() )
            addNewPage();

            previousYLevel =  currentYLevel;

            float y = addTitle( previousYLevel, lmsg( "g.IbmInsightInfoTitle" ), lmsg( "g.IbmInsightInfoSubtitle" ) );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.0f, 1f, 3f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            // t.setSplitLate( false );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );

            InsightReportTrait irt;

            PdfPTable t2;
            String facet;

            for( TextAndTitle tt : ttl )
            {
                irt = new InsightReportTrait( tt );

                if( !irt.getIsValid() )
                    continue;

                c = new PdfPCell( new Phrase( irt.getName(), fontLargeBold ) );
                c.setBorder( Rectangle.LEFT | Rectangle.TOP  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingTop( 8 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.lightergray );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( irt.getScoreStr(), fontLargeBold ) );
                c.setBorder( Rectangle.TOP  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingTop( 8 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.lightergray );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );


                t2 = new PdfPTable( 1 );
                setRunDirection( t2 );

                t2.setWidthPercentage( 100 );
                // t2.setLockedWidth( true );

                c = new PdfPCell( new Phrase("") ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.NO_BORDER  );
                c.setPadding( 0 );
                c.setFixedHeight(22);
                c.setCellEvent( new CT2MetaScoreGraphicCellEvent( baseFontCalibri, irt.getScore() ) );
                t2.addCell(c);

                c = new PdfPCell( t2 ); // new PdfPCell( summaryCatNumericAxis );
                c.setBorder( Rectangle.RIGHT  | Rectangle.TOP );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                c.setPaddingTop( 6 );
                t.addCell( c );


                c = new PdfPCell( new Phrase( irt.getDescrip(), font ) );

                if( irt.getFacets().isEmpty() )
                    c.setBorder( Rectangle.LEFT  | Rectangle.RIGHT   | Rectangle.BOTTOM  );

                else
                    c.setBorder( Rectangle.LEFT  | Rectangle.RIGHT   );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan( 3 );
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( 10 );
                // c.setBackgroundColor( ct2Colors.lightergray );
                setRunDirection( c );
                t.addCell( c );

                t2 = new PdfPTable( 1 );
                setRunDirection( t2 );

                t2.setWidthPercentage( 100 );
                // t2.setLockedWidth( true );



                // for( String f : irt.getFacets() )
                for( int i=0; i<irt.getFacets().size(); i++ )
                {
                    facet = irt.getFacets().get(i);

                    c = new PdfPCell( new Phrase( "\u2022 " + facet, font ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderWidth(0);
                    if( i==irt.getFacets().size()-1 )
                        c.setPaddingBottom(8);
                    setRunDirection( c );
                    t2.addCell( c );
                }

                if( !irt.getFacets().isEmpty() )
                {
                    c = new PdfPCell( new Phrase( lmsg( "g.NotedFacetsC", null ), font ) );
                    c.setBorder( Rectangle.LEFT   | Rectangle.BOTTOM   );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setColspan( 1 );
                    c.setPadding( 2 );
                    c.setPaddingLeft( 10 );
                    c.setVerticalAlignment(Element.ALIGN_TOP );
                    setRunDirection( c );
                    t.addCell( c );

                    c = new PdfPCell( t2 ); // new PdfPCell( summaryCatNumericAxis );
                    c.setColspan(2);
                    c.setBorder( Rectangle.RIGHT | Rectangle.BOTTOM );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    // c.setVerticalAlignment( Element.ALIGN_BOTTOM );
                    c.setPaddingTop( 6 );
                    t.addCell( c );
                }
            }

            currentYLevel = addTableToDocument(y, t, true );

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addIbmInsightSection()" );

            throw new STException( e );
        }

    }
    */


    public void addResponseRatingSection() throws Exception
    {
        if( ReportManager.DEBUG_REPORTS )
            LogService.logIt(  "BaseCT2ReportTemplate.addResponseRatingSection() AAA  testEventId=" + reportData.te.getTestEventId() );

        if( reportData.te.getTestEventResponseRatingList()==null || reportData.te.getTestEventResponseRatingList().isEmpty() )
            return;

        if( !TestEventResponseRatingUtils.getHasAnyNonSimCompetencyRatings(reportData.te.getTestEventResponseRatingList()))
        {
            LogService.logIt( "BaseCt2ReportTemplate.addResponseRatingSection() TestEvent does not have any TestEventResponseRatings that are not associated with a SimCompetency. testEventId=" + reportData.te.getTestEventId() );
            return;
        }

        try
        {
            Map<String,String> avgRatingMap = TestEventResponseRatingUtils.getOverallAverageRatingMap( reportData.te.getTestEventResponseRatingList(), reportData.getLocale() );

            if( avgRatingMap==null || avgRatingMap.isEmpty() )
            {
                LogService.logIt( "BaseCt2ReportTemplate.addResponseRatingSection() Average Rating Map is null or empty. testEventId=" + reportData.te.getTestEventId() );
                return;
            }

            // LogService.logIt(  "BaseCT2ReportTemplate.addResponseRatingSection() DDD currentYLevel=" + currentYLevel + ", getMinYForNewSection()=" + getMinYForNewSection() );
            previousYLevel =  currentYLevel;

            if( previousYLevel < getMinYForNewSection() )
            {
                addNewPage();
                previousYLevel = currentYLevel;
            }

            float y = addTitle(previousYLevel, lmsg( "g.AvgResponseRatings" ), lmsg( "g.AvgResponseRatingsSubtitle" ), null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 5f, 4f, 10f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            // t.setSplitLate( false );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.NO_BORDER );
            //c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            //c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );

            // Add header row.
            // t.addCell( new Phrase( lmsg( "g.RatingCategory" ) , fontLmWhite) );
            //t.addCell( new Phrase( lmsg( "g.AverageRating" ) , fontLmWhite) );
            //t.addCell( new Phrase( "" , fontLmWhite) );

            c = new PdfPCell( new Phrase( lmsg( "g.RatingCategory" ) , fontLmWhite) );
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( lmsg( "g.AverageRating" ) , fontLmWhite) );
            c.setBorder( Rectangle.TOP );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection( c );
            t.addCell( c );

            c = new PdfPCell( new Phrase( "" , fontLmWhite) );
            // c.setBorder( Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false ));
            setRunDirection( c );
            t.addCell( c );





            c.setBackgroundColor( BaseColor.WHITE );

            int count = 0; // avgRatingMap.size();
            for( String name : avgRatingMap.keySet() )
            {
                count++;
                c = new PdfPCell( new Phrase( name + ":", font ) );
                if( count == avgRatingMap.size() )
                    c.setBorder( Rectangle.LEFT  | Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.LEFT  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( avgRatingMap.get(name), font ) );
                if( count == avgRatingMap.size() )
                    c.setBorder( Rectangle.BOTTOM );
                else
                    c.setBorder( Rectangle.NO_BORDER  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( "", font ) );
                if( count == avgRatingMap.size() )
                    c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT );
                else
                    c.setBorder( Rectangle.RIGHT );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );
            }

            currentYLevel = addTableToDocument(y, t, false, true );


        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addResponseRatingSection()" );

            throw new STException( e );
        }
    }


    public void addItemScoresSection() throws Exception
    {
        if( ReportManager.DEBUG_REPORTS )
            LogService.logIt(  "BaseCT2ReportTemplate.addItemScoresSection() AAA " );

        if( ( reportData.getR2Use().getIncludeItemScores() <= 0 && !reportData.getReportRuleAsBoolean( "itmscoreson" ) ) || reportData.getReportRuleAsBoolean( "itmscoresoff") )
            return;

        try
        {
            if( ReportManager.DEBUG_REPORTS )
                LogService.logIt(  "BaseCT2ReportTemplate.addItemScoresSection() BBB " );

            // returns a new List object so OK to delete from it.
            List<TestEventScore> tesl = reportData.getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );
            if( tesl.isEmpty() )
                return;

            ListIterator<TestEventScore> iter = tesl.listIterator();
            TestEventScore tes;
            String cl;

            while( iter.hasNext() )
            {
                tes = iter.next();

                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowItemScoresInReports() || tes.getIntParam2()<=0 )
                {
                    iter.remove();
                    continue;
                }

                cl = StringUtils.getBracketedArtifactFromString(tes.getTextParam1() , Constants.ITEMSCOREINFOKEY );
                if( cl==null || cl.isEmpty() )
                {
                    iter.remove();
                    continue;
                }
            }

            if( ReportManager.DEBUG_REPORTS )
                LogService.logIt(  "BaseCT2ReportTemplate.addItemScoresSection() CCC tesl.size=" + tesl.size() );
            if( tesl.isEmpty() )
                return;

            // LogService.logIt(  "BaseCT2ReportTemplate.addItemScoresSection() DDD currentYLevel=" + currentYLevel + ", getMinYForNewSection()=" + getMinYForNewSection() );
            previousYLevel =  currentYLevel;

            // java.util.List<TextAndTitle> ttl = ScoreFormatUtils.getNonCompTextListTable( reportData.getTestEvent(), NonCompetencyItemType.WRITING_SAMPLE );

            //if( ttl.isEmpty() )
            //    return;

            if( previousYLevel < getMinYForNewSection() )
            {
                addNewPage();
                previousYLevel = currentYLevel;
            }

            float y = addTitle(previousYLevel, lmsg( "g.ItemScoresInfoTitle" ), lmsg( "g.ItemScoresInfoSubtitle" ), null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.0f, 5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            // t.setSplitLate( false );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = new PdfPCell(new Phrase( lmsg( "g.Question" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, false ));
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( lmsg( "g.ResponseInfo" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, false, false ));
            setRunDirection( c );
            t.addCell(c);

            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.Question" ) , fontLmWhite) );
            //t.addCell( new Phrase( lmsg( "g.ResponseInfo" ) , fontLmWhite) );


            String competencyTitle;
            IncludeItemScoresType iist;

            // We have some TES's to write to.
            List<TextAndTitle> ttl;

            String scoreKeyInfo;

            String itemRespInfo;

            // Locale rloc = reportData.getLocale();
            String theText;

            for( TestEventScore tesx : tesl )
            {
                iist = IncludeItemScoresType.getValue( tesx.getIntParam2() );
                competencyTitle = tesx.getName() + " (" + iist.getName4Reports(reportData.getLocale() )  + ") ";

                scoreKeyInfo = iist.getScoreKeyInfo( reportData.getLocale(), tesx.getSimCompetencyClass() );

                c = new PdfPCell( new Phrase( competencyTitle, font ) );
                c.setBorder( Rectangle.LEFT | Rectangle.TOP  );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setBackgroundColor( ct2Colors.scoreBoxShadeBgColor );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( scoreKeyInfo, font ) );
                c.setBorder( Rectangle.TOP | Rectangle.RIGHT );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setBackgroundColor( ct2Colors.scoreBoxShadeBgColor );
                // c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                setRunDirection( c );
                t.addCell( c );


                cl = StringUtils.getBracketedArtifactFromString(tesx.getTextParam1() , Constants.ITEMSCOREINFOKEY );

                if( cl==null || cl.trim().isEmpty() )
                    continue;

                ttl = ScoreFormatUtils.unpackTextBasedResponses( cl );

                if( ReportManager.DEBUG_REPORTS )
                    LogService.logIt(  "BaseCT2ReportTemplate.addItemScoresSection() DDD tesx=" + tesx.getName() + ", ttl.size=" + ttl.size() );

                if( ttl.isEmpty() )
                    continue;

                for( TextAndTitle tt : ttl )
                {
                    theText = tt.getTitle();
                    if( StringUtils.getHasHtml(theText) )
                        theText = StringUtils.convertHtml2PlainText(theText, true);

                    c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                    c.setBackgroundColor( BaseColor.WHITE );
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderWidth( 0.5f );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setPadding( 6 );
                    setRunDirection( c );
                    t.addCell( c );

                    itemRespInfo = tt.getText();

                    if( itemRespInfo==null )
                        itemRespInfo="";

                    if( StringUtils.getHasHtml(itemRespInfo) )
                        itemRespInfo = StringUtils.convertHtml2PlainText(itemRespInfo, true);

                    if( itemRespInfo.equalsIgnoreCase("Correct"))
                        itemRespInfo = lmsg( "g.Correct" );

                    else if( itemRespInfo.equalsIgnoreCase("Incorrect"))
                        itemRespInfo = lmsg( "g.Incorrect" );

                    else if( itemRespInfo.equalsIgnoreCase("Partial"))
                        itemRespInfo = lmsg( "g.PartiallyCorrect" );

                    else if( itemRespInfo.endsWith("(Correct)"))
                        itemRespInfo = StringUtils.replaceStr( itemRespInfo, "(Correct)", "(" + lmsg( "g.Correct" ) + ")" );

                    else if( itemRespInfo.endsWith("(Incorrect)"))
                        itemRespInfo = StringUtils.replaceStr( itemRespInfo, "(Incorrect)", "(" + lmsg( "g.Incorrect" ) + ")" );

                    else if( itemRespInfo.endsWith("(Partial)"))
                        itemRespInfo = StringUtils.replaceStr( itemRespInfo, "(Partial)", "(" + lmsg( "g.PartiallyCorrect" ) + ")" );


                    c = new PdfPCell( new Phrase( itemRespInfo, getFontSmall() ) );
                    c.setBackgroundColor( BaseColor.WHITE );
                    c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                    c.setBorderWidth( 0.5f );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setPadding( 6 );
                    setRunDirection( c );
                    t.addCell( c );
                }
            }

            currentYLevel = addTableToDocument(y, t, false, true );


        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addItemScoresSection()" );

            throw new STException( e );
        }
    }


    public void addSuspensionsSection() throws Exception
    {
        try
        {
            //LogService.logIt( "BaseCT2ReportTemplate.addSuspensionsSection() AAA.1 ");

            // no need to report
            if( !reportData.tk.getOnlineProctoringType().getIsAnyPremium() && reportData.tk.getTestKeyProctorTypeId()<=0 )
                return;

            if( reportData.getReportRuleAsBoolean( "allprocpdf")  )
                return;


            //LogService.logIt( "BaseCT2ReportTemplate.addSuspensionsSection() AAA.2 ");

            if( reportData.te.getRemoteProctorEvent()==null && reportData.tk.getOnlineProctoringType().getIsAnyPremium() )
            {
                if( proctorUtils==null )
                    proctorUtils = new ProctorUtils();
                proctorUtils.setupRemoteProctorEvent( reportData.getLocale(), reportData.getUser().getTimeZone(), reportData.te );
            }

            // no need to report
            if( reportData.tk.getTestKeyProctorTypeId()<=0 && reportData.te.getRemoteProctorEvent()==null )
                return;

            // no need to report
            if( reportData.te.getRemoteProctorEvent()==null )
            {
                LogService.logIt( "BaseCT2ReportTemplate.addSuspensionsSection() AAA.5  TestEvent.remoteProctorEvent is null. testEventId=" + reportData.te.getTestEventId());
                return;
            }

            List<ProctorSuspension> psl = reportData.tk.getProctorSuspensionList();

           // LogService.logIt( "BaseCT2ReportTemplate.addSuspensionsSection() AAA.3 psl.size=" + (psl==null ? "null" : psl.size()));

            // no remote proctor event and no proctor suspensions
            //if( reportData.te.getRemoteProctorEvent()==null && psl==null )
            //    return;

            RemoteProctorEvent rpe =reportData.te.getRemoteProctorEvent();

            SuspiciousActivityThresholdType satt = rpe==null ? SuspiciousActivityThresholdType.NEVER : SuspiciousActivityThresholdType.getValue( rpe.getSuspiciousActivityThresholdTypeId() );

            // no local proctor, no automated threshold, no ability for remote proctor to suspend.
            if( reportData.tk.getTestKeyProctorTypeId()<=0 && satt.equals( SuspiciousActivityThresholdType.NEVER) && !reportData.tk.getOnlineProctoringType().getIsPremiumWithRemoteSuspensionCapability() )
            {
                return;
            }

            // ignore rpe if we have any ProctorSuspensions
            List<String[]> shl = (psl!=null && !psl.isEmpty()) || rpe==null || satt.equals( SuspiciousActivityThresholdType.NEVER) ? null :  rpe.getSuspensionHistoryList();

            int totalSus = (shl==null ? 0 : shl.size());
            int totalProc = (psl==null ? 0 : psl.size());
            int total = totalSus + totalProc;

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.SuspensionHistoryTitle" ), satt==null || satt.getSuspiciousActivityThresholdTypeId()<=0 ? null : lmsg( "g.SuspensionHistorySubtitleX", new String[]{Integer.toString( satt==null ? 0 : satt.getSuspiciousActivityThresholdTypeId())} ), null, null );

            PdfPCell c;
            PdfPTable t;
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // First, add a table
            t = new PdfPTable( new float[] { 1.5f, 1f, 1.5f  } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( totalSus<=0 && totalProc<=0 ? ct2Colors.hraBlue : ct2Colors.red );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );

            // Add header row.
            // t.addCell( new Phrase( lmsg( "g.DateTime" ) , fontLmWhite) );
            c = new PdfPCell( new Phrase( lmsg( "g.DateTime" ) , fontLmWhite) );
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( totalSus<=0 && totalProc<=0 ? ct2Colors.hraBlue : ct2Colors.red );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), totalSus<=0 && totalProc<=0 ? ct2Colors.hraBlue : ct2Colors.red, true, false, false, true ));

            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            // t.addCell( new Phrase( lmsg( "g.Source" ) , fontLmWhite) );
            c = new PdfPCell( new Phrase( lmsg( "g.Source" ) , fontLmWhite) );
            // c.setBorder( Rectangle.TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( totalSus<=0 && totalProc<=0 ? ct2Colors.hraBlue : ct2Colors.red );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            // t.addCell( new Phrase( lmsg( "g.Notes" ) , fontLmWhite) );
            c = new PdfPCell( new Phrase( lmsg( "g.Notes" ) , fontLmWhite) );
            // c.setBorder( Rectangle.RIGHT | Rectangle.TOP );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( totalSus<=0 && totalProc<=0 ? ct2Colors.hraBlue : ct2Colors.red );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), totalSus<=0 && totalProc<=0 ? ct2Colors.hraBlue : ct2Colors.red, false, true, true, false ));
            setRunDirection( c );
            t.addCell(c);

            if( total<=0 )
            {
                c = new PdfPCell( new Phrase( MessageFactory.getStringMessage(reportData.getLocale(), "g.NoSuspHist" ) , font) );
                // c.setBorder( Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
                c.setBorder( Rectangle.NO_BORDER);
                //c.setBorderWidth( scoreBoxBorderWidth );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(3);
                c.setPadding( 5 );
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                // c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.lightergray );
                setRunDirection( c );
                t.addCell( c );
            }

            if( totalSus>0 )
            {
                ListIterator<String[]> iter = shl.listIterator();
                String[] sh;

                while( iter.hasNext() )
                {
                    sh = iter.next();

                    for( int i=0;i<3;i++ )
                    {
                        c = new PdfPCell( new Phrase( sh[i] , font) );
                        if( i==0 )
                            c.setBorder( iter.hasNext() || totalProc>0 ? Rectangle.LEFT : Rectangle.LEFT | Rectangle.BOTTOM);
                        else if( i==1 )
                            c.setBorder( iter.hasNext() || totalProc>0 ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
                        else if( i==2 )
                            c.setBorder( iter.hasNext() || totalProc>0 ? Rectangle.RIGHT : Rectangle.RIGHT | Rectangle.BOTTOM);
                        c.setBorderWidth( scoreBoxBorderWidth );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setColspan(1);
                        c.setPadding( 2 );
                        c.setPaddingBottom( 2 );
                        c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                        setRunDirection( c );
                        t.addCell( c );
                    }
                }
            }

            if( totalProc>0 )
            {
                ListIterator<ProctorSuspension> iter = psl.listIterator();
                ProctorSuspension sh;

                String notes;

                while( iter.hasNext() )
                {
                    sh = iter.next();

                    notes = sh.getNote();
                    if( notes==null )
                        notes="";
                    notes = notes.trim();

                    if( sh.getProctorSuspensionStatusTypeId()>0 )
                    {
                        if( sh.getRemoveUser()!=null )
                        {
                            notes += (notes.isEmpty() ? "" : "\n\n") + lmsg("g.RemovedByC") + " " + sh.getRemoveUser().getFullname();

                            if( sh.getRemoveDate()!=null )
                                notes += " " + I18nUtils.getFormattedDateTime( reportData.getLocale(), sh.getRemoveDate(), reportData.getTimeZone());
                        }
                    }

                    c = new PdfPCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), sh.getCreateDate(), reportData.getTimeZone()) , font) );
                    c.setBorder( iter.hasNext() ? Rectangle.LEFT : Rectangle.LEFT | Rectangle.BOTTOM);
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setColspan(1);
                    c.setPadding( 2 );
                    c.setPaddingBottom( 2 );
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    setRunDirection( c );
                    t.addCell( c );

                    c = new PdfPCell( new Phrase( sh.getProctorUser()==null ? lmsg("g.System") : sh.getProctorUser().getFullname() , font) );
                     c.setBorder( iter.hasNext() ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setColspan(1);
                    c.setPadding( 2 );
                    c.setPaddingBottom( 2 );
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    setRunDirection( c );
                    t.addCell( c );

                    c = new PdfPCell( new Phrase( notes , font) );
                    c.setBorder( iter.hasNext() ? Rectangle.RIGHT : Rectangle.RIGHT | Rectangle.BOTTOM);
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setColspan(1);
                    c.setPadding( 2 );
                    c.setPaddingBottom( 2 );
                    c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                    setRunDirection( c );
                    t.addCell( c );
                }
            }



            currentYLevel = addTableToDocument(y, t, false, true );
           // LogService.logIt( "BaseCT2ReportTemplate.addSuspiciousActivitySection() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addSuspensionsSection()" );
            throw new STException( e );
        }
    }


    public void addProctorCertificationsSection() throws Exception
    {
        // LogService.logIt( "BaseCT2ReportTemplate.addProctorCertificationsSection() START" );
        try
        {
            if( reportData.getReportRuleAsBoolean( "allprocpdf")  )
                return;

            if( reportData.tk.getTestKeyProctorTypeId()<=0 || reportData.tk.getProctorEntryList()==null  )
                return;

            List<ProctorEntry> pel = reportData.tk.getProctorEntryList();

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.ProctorCerts" ), null, null, null );

            PdfPCell c;
            PdfPTable t;
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // First, add a table
            t = new PdfPTable( new float[] { 1f, 1f, 2f  } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = new PdfPCell(new Phrase( lmsg( "g.DateTime" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( lmsg( "g.Proctor" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( lmsg( "g.Notes" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false ));
            setRunDirection( c );
            t.addCell(c);


            // Add header row.
            //c.setBorder( Rectangle.LEFT | Rectangle.TOP  );
            //t.addCell( new Phrase( lmsg( "g.DateTime" ) , fontLmWhite) );
            //c.setBorder( Rectangle.TOP  );
            //t.addCell( new Phrase( lmsg( "g.Proctor" ) , fontLmWhite) );
            //c.setBorder( Rectangle.RIGHT | Rectangle.TOP  );
            //t.addCell( new Phrase( lmsg( "g.Notes" ) , fontLmWhite) );

            TimeZone tz = reportData.getUser().getTimeZone();

            if( pel.isEmpty() )
            {
                c = new PdfPCell( new Phrase( lmsg("g.ProctorCertsNone") , fontLmWhite) );
                // c = new PdfPCell( new Phrase( MessageFactory.getStringMessage(reportData.getLocale(), "g.NoSuspActDet" ) , fontLmWhite) );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(3);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.lightergray );
                setRunDirection( c );
                t.addCell( c );
            }

            ProctorEntry pe;
            ListIterator<ProctorEntry> iter = pel.listIterator();

            while( iter.hasNext() )
            {
                pe = iter.next();
                c = new PdfPCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), pe.getEntryDate(), tz) , font) );
                c.setBorder( iter.hasNext() ? Rectangle.LEFT : Rectangle.LEFT | Rectangle.BOTTOM);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( pe.getProctorUser().getFullname() , font) );
                c.setBorder( iter.hasNext() ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( pe.getNote()==null ? "" : pe.getNote(), font) );
                c.setBorder( iter.hasNext() ? Rectangle.RIGHT : Rectangle.RIGHT | Rectangle.BOTTOM);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                t.addCell( c );
            }

            currentYLevel = addTableToDocument(y, t, false, true );
           // LogService.logIt( "BaseCT2ReportTemplate.addSuspiciousActivitySection() END" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addProctorCertificationsSection()" );
            throw new STException( e );
        }
    }



    public void addSuspiciousActivitySection() throws Exception
    {
        // LogService.logIt( "BaseCT2ReportTemplate.addSuspiciousActivitySection() START" );
        try
        {
            //if( !reportData.tk.getOnlineProctoringType().getIsAnyPremium() )
            //    return;
            if( reportData.getReportRuleAsBoolean( "allprocpdf")  )
                return;

            if( reportData.te.getRemoteProctorEvent()==null || reportData.getTestEvent().getRemoteProctorEvent().getSuspiciousActivityList()==null )
            {
                if( proctorUtils==null )
                    proctorUtils = new ProctorUtils();
                proctorUtils.setupRemoteProctorEvent( reportData.getLocale(), reportData.getUser().getTimeZone(), reportData.te );
            }

            if( reportData.te.getRemoteProctorEvent()==null )
                return;

            List<SuspiciousActivity> sal = reportData.te.getRemoteProctorEvent().getSuspiciousActivityList();

            // LogService.logIt( "BaseCT2ReportTemplate.addSuspiciousActivitySection() SuspiciousActivity=" + sal.size() );

            // No section if Basic and no SA found.
            if( !reportData.tk.getOnlineProctoringType().getIsPremiumWithSuspAct() && sal.isEmpty() )
                return;

            // LogService.logIt( "BaseCT2ReportTemplate.addSuspiciousActivitySection() SuspiciousActivity=" + sal.size() );

            UserFacade userFacade = null;

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.SuspiciousActivityTitle" ), lmsg( "g.SuspiciousActivitySubtitle",new String[]{RuntimeConstants.getStringValue("baseadmindomain")} ), null, null );

            PdfPCell c;
            PdfPTable t;
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            // First, add a table
            t = new PdfPTable( new float[] { 1.5f, 1f, 1.5f  } );
            // t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = new PdfPCell( new Phrase( lmsg( "g.DateTime" ) , fontLmWhite) );
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( sal.isEmpty() ?  ct2Colors.hraBlue : ct2Colors.red );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), sal.isEmpty() ?  ct2Colors.hraBlue : ct2Colors.red, true, false, false, true ));
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Action" ) , fontLmWhite) );
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setBackgroundColor( sal.isEmpty() ?  ct2Colors.hraBlue : ct2Colors.red );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg( "g.Info" ) , fontLmWhite) );
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER);
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( sal.isEmpty() ?  ct2Colors.hraBlue : ct2Colors.red );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), sal.isEmpty() ?  ct2Colors.hraBlue : ct2Colors.red, false, true, true, false ));
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );
            t.addCell(c);


            // Add header row.
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP  );
            //t.addCell( new Phrase( lmsg( "g.DateTime" ) , fontLmWhite) );
            //c.setBorder( Rectangle.TOP  );
            //t.addCell( new Phrase( lmsg( "g.Action" ) , fontLmWhite) );
            //c.setBorder( Rectangle.RIGHT | Rectangle.TOP  );
            //t.addCell( new Phrase( lmsg( "g.Info" ) , fontLmWhite) );

            if( sal.isEmpty() )
            {
                c = new PdfPCell( new Phrase( lmsg("g.NoSuspActDet", new String[]{RuntimeConstants.getStringValue("baseadmindomain")}) , fontLmWhite) );
                // c = new PdfPCell( new Phrase( MessageFactory.getStringMessage(reportData.getLocale(), "g.NoSuspActDet" ) , fontLmWhite) );
                c.setBorder( Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(3);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                // c.setBackgroundColor( ct2Colors.lightergray );
                setRunDirection( c );
                t.addCell( c );
            }

            ListIterator<SuspiciousActivity> iter = sal.listIterator();
            SuspiciousActivity sa;
            SuspiciousActivityType sat;
            String info;
            String instances;
            TimeZone tz = reportData.getUser().getTimeZone();

            if( tz==null )
                tz = TimeZone.getDefault();

            while( iter.hasNext() )
            {
                sa = iter.next();
                sat = sa.getSuspiciousActivityType();

                if( sat.getIsUserNote() && sa.getUserId()>0 )
                {
                    if( userFacade==null )
                        userFacade=UserFacade.getInstance();
                    sa.setUser( userFacade.getUser( sa.getUserId() ));
                }

                c = new PdfPCell( new Phrase( I18nUtils.getFormattedDateTime( reportData.getLocale(), sa.getLastUpdate(), tz) , font) );
                c.setBorder( iter.hasNext() ? Rectangle.LEFT : Rectangle.LEFT | Rectangle.BOTTOM);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( sat.getName( reportData.getLocale() ) , font) );
                c.setBorder( iter.hasNext() ? Rectangle.NO_BORDER : Rectangle.BOTTOM);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                t.addCell( c );

                info = "";

                if( sat.getUsesCounter() )
                {
                    if( sa.getIntParam2()>0 )
                        instances = " (" + lmsg( "g.PPXInstances2", new String[]{Integer.toString(sa.getIntParam1()),Integer.toString(sa.getIntParam2())} ) + ")";
                    else
                        instances = " (" + lmsg( "g.PPXInstances", new String[]{Integer.toString(sa.getIntParam1())} ) + ")";
                }
                else
                    instances="";

                // instances = sat.getUsesCounter() ? " (" + lmsg( "g.PPXInstances", new String[]{Integer.toString(sa.getIntParam1())} ) + ")" : "";

                if( sat.getIsKeyPress() )
                {
                    if( !SuspiciousKeyCodeType.getValue( sa.getKeyCode() ).getIsUnknown() )
                        info = SuspiciousKeyCodeType.getValue( sa.getKeyCode() ).getName() + (sa.getNote()!=null && !sa.getNote().isBlank() ? " " + sa.getNote() : "");
                    else if( sa.getNote()!=null && !sa.getNote().isBlank() )
                        info = sa.getNote();
                }
                else if( sat.getShowTime() )
                    info = sa.getSeconds() + " " + MessageFactory.getStringMessage( reportData.getLocale(), "g.Seconds" );
                else if( sat.getIsMultiFaces() )
                    info = MessageFactory.getStringMessage( reportData.getLocale(), "sat.multiplefaces.detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( sa.getFloatParam1AsInt() )} );
                else if( sat.getIsFaceMissing())
                    info = MessageFactory.getStringMessage( reportData.getLocale(), "sat.candidatefacenotpresent.detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( sa.getFloatParam1AsInt() )} );
                else if( sat.getIsFaceMismatch())
                    info = MessageFactory.getStringMessage( reportData.getLocale(), "sat.facialmismatches.detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( sa.getFloatParam1AsInt())} );
                else if( sat.getIsAnyIdFaceMismatch())
                    info = MessageFactory.getStringMessage( reportData.getLocale(), sat.getKey() + ".detail", new String[]{Integer.toString( Math.round(sa.getFloatParam1()*100))} );
                else if( sat.getIsHighPitchYaw())
                    info = MessageFactory.getStringMessage( reportData.getLocale(), sat.getKey() + ".detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( Math.round( Math.abs(sa.getFloatParam1())))} );
                else if( sat.getIsFrequentPitchYaw())
                    info = MessageFactory.getStringMessage( reportData.getLocale(), sat.getKey() + ".detail", new String[]{Integer.toString( Math.round( Math.abs(sa.getFloatParam1())))} );
                else if( sat.getIsAltMobile())
                    info = MessageFactory.getStringMessage( reportData.getLocale(), "sat.mobile.detail", new String[]{Integer.toString(sa.getIntParam1())} );
                else if( sat.getSameIpTestEvents() )
                {
                    info = MessageFactory.getStringMessage( reportData.getLocale(), sat.getKey() + ".detail", null );
                    info += "\n" + MessageFactory.getStringMessage( reportData.getLocale(), sat.getKey() + ".detailX", null );
                    info += "\n" + (new ProctorUtils()).getSameIpUserInfo( reportData.te.getRemoteProctorEvent(), reportData.getLocale(), reportData.u.getTimeZone(), false );
                }
                else if( sat.getIsAnyNote() )
                {
                    if( sat.getIsProctorNote() )
                        info = "(" + MessageFactory.getStringMessage( reportData.getLocale(), "g.Proctor" ) + ") " + sa.getNote();
                    else if( sat.getIsUserNote() )
                        info = "(" + sa.getUser()==null ? "" : sa.getUser().getLastName() + ") " + sa.getNote();
                }

                info += instances;

                c = new PdfPCell( new Phrase( info , font) );
                c.setBorder( iter.hasNext() ? Rectangle.RIGHT : Rectangle.RIGHT | Rectangle.BOTTOM);
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setColspan(1);
                c.setPadding( 2 );
                c.setPaddingBottom( 2 );
                c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                setRunDirection( c );
                t.addCell( c );

            }

            currentYLevel = addTableToDocument(y, t, false, true );
           // LogService.logIt( "BaseCT2ReportTemplate.addSuspiciousActivitySection() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addSuspiciousActivitySection()" );
            throw new STException( e );
        }
    }


    public void addPremiumIdentityImageCaptureSection() throws Exception
    {
        try
        {
            // LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() Start. " );
            if( !ProctorHelpUtils.getUseExternalProctoring(reportData.tk) )
                return;

            if( reportData.getReportRuleAsBoolean( "hideimgcaptpdf" ) || reportData.getReportRuleAsBoolean( "allprocpdf")  )
                return;

            RemoteProctorEvent rpe = reportData.te.getRemoteProctorEvent();
            if( proctorUtils==null )
                proctorUtils = new ProctorUtils();
            if( rpe==null || rpe.getUploadedUserFileList()==null )
            {
                proctorUtils.setupRemoteProctorEvent( reportData.getLocale(), reportData.getUser().getTimeZone(), reportData.te );
                rpe = reportData.te.getRemoteProctorEvent();
            }

            //LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() rpe=" + (rpe==null ? "null" : "id=" + rpe.getRemoteProctorEventId() + ", status=" + rpe.getRemoteProctorEventStatusType().getName()) + ", uploadedUserFiles=" + (rpe!=null && rpe.getUploadedUserFileList()!=null ? rpe.getUploadedUserFileList().size() : " null - 0")   );

            if( rpe==null )
                return;

            // boolean imageComparisonsComplete = rpe.getRemoteProctorEventStatusTypeId()>=RemoteProctorEventStatusType.IMAGE_COMPARISONS_COMPLETE.getRemoteProctorEventStatusTypeId();

            // No photos at all.
            List<UploadedUserFile> ufl = rpe.getUploadedUserFileListForPhotos();
            List<UploadedUserFile> uflRec = rpe.getOnlineProctoringType().getRecordsVideo() ? rpe.getUploadedUserFileListForRecordings() : null;
            if( uflRec!=null && !uflRec.isEmpty() )
                ufl.addAll(uflRec);

            List<UploadedUserFile> uflIds = rpe.getUploadedUserFileListForIds();
            if( ufl.isEmpty() && uflIds.isEmpty() )
            {
                //LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() ufl.size=" + ufl.size() + ", uflIds.size=" + uflIds.size() );
                return;
            }

            boolean showImages = reportData.o.getCandidateImageViewTypeId()<=0 && !reportData.getReportRuleAsBoolean( "captimgsoff" ) && !reportData.getReportRuleAsBoolean( "captimgsoffpdfonly" ) && !reportData.tk.getHideMediaInReports() && reportData.o.getHideProcImagesPdf()<=0;

            boolean forceIncludeAllImages = false; // showImages && (rpe.getMultiFaceThumbs()>0);

            int maxImagesToShow = 22;

            //float avgScoreId = rpe.getThumbScore();
            //float avgScoreFace = rpe.getIdFaceMatchPercent();
            //float overallProctorScore = rpe.getOverallProctorScore();
            //float tot;
            //float count=0;

            if( proctorUtils==null )
                proctorUtils = new ProctorUtils();
            List<UploadedUserFile> ufl2 = showImages ? proctorUtils.getFauxUploadedUserFileListForReportThumbs( ufl, forceIncludeAllImages, maxImagesToShow ) : new ArrayList<>();
            List<UploadedUserFile> uflId2 = showImages ? proctorUtils.getFauxUploadedUserFileListForReportThumbs( uflIds, forceIncludeAllImages, maxImagesToShow ) : new ArrayList<>();

            boolean hasMax = ufl2.size()>=maxImagesToShow;

            List<String[]> caveatList = proctorUtils.getPremiumCaveatList( reportData.getTestKey().getProctoringIdCaptureTypeId(),  rpe, reportData.getLocale(), hasMax );

            //LogService.logIt( "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() showImages=" + showImages + ", ufl2.size=" + ufl2.size() + ", uflId2.size=" + uflId2.size() + ", caveatList.size=" + caveatList.size() );

            PdfPTable t = generateIdentityImageCaptureTableTop(reportData,
                                                             caveatList,
                                                             rpe.getOverallProctorScore(),
                                                             pageWidth );

            if( t==null )
                return;

            addNewPage();
            previousYLevel =  currentYLevel;
            float y = addTitle(previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle.Premium" ), null, null );

            currentYLevel = addTableToDocument(y, t, false, true );

            t = generateIdentityImageCaptureTableImages(reportData,
                                                             ufl2,
                                                             uflId2,
                                                             pageWidth );

            if( t==null )
                return;

            previousYLevel =  currentYLevel;
            y = previousYLevel; // addTitle( previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle.Premium" ) );

            currentYLevel = addTableToDocument(y, t, false, true );

           // LogService.logIt( "BaseCT2ReportTemplate.addIdentityImageCaptureSection() END" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addPremiumIdentityImageCaptureSection() testEventId=" + (reportData.te==null ? "null" : reportData.te.getTestEventId()) + ", reportId=" + (reportData.r2Use==null ? "null" : reportData.r2Use.getReportId()) );
            throw new STException( e );
        }

    }


    public void addIdentityImageCaptureSection() throws Exception
    {
        // LogService.logIt(  "BaseCT2ReportTemplate.addIdentityImageCaptureInfo() UsePremium=" + ProctorHelpUtils.getUseExternalProctoring(reportData.getTestKey()) );

        if( ProctorHelpUtils.getUseExternalProctoring(reportData.getTestKey()) )
        {
            if( reportData.getTestEvent().getRemoteProctorEvent()==null )
            {
                if( proctorUtils==null )
                    proctorUtils = new ProctorUtils();
                proctorUtils.setupRemoteProctorEvent( reportData.getLocale(), reportData.getUser().getTimeZone(), reportData.getTestEvent() );
            }

            if( reportData.getTestKey().getOnlineProctoringType().getIsPremiumWithImageCap() || reportData.getTestEvent().getRemoteProctorEvent()!=null )
            {
                addPremiumIdentityImageCaptureSection();
                return;
            }

            // if external but no images, do not include.
            //return;
        }

            /*
            // below is for legacy image captures (Deprecated).

            // boolean showImages = !reportData.getReportRuleAsBoolean( "captimgsoff" ) && !reportData.tk.getHideMediaInReports();

            // LogService.logIt(  "BaseCT2ReportTemplate.addIdentityImageCaptureInfo() reportData.tk.getHideMediaInReports()=" + reportData.tk.getHideMediaInReports() + ", testEventId=" + reportData.te.getTestEventId() );
            List<TestEventScore> tesl = getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDIMAGEUPLOAD );

            if( tesl.isEmpty() )
                return;

            java.util.List<TextAndTitle> ttl = new ArrayList<>();
            tesl.forEach((tes) -> {
                ttl.addAll( tes.getTextBasedResponseList( null, true, true ) );
            });

            if( ttl.isEmpty() )
                return;

            PdfPTable t = getBasicIdentityImageCaptureTableTop( reportData, tesl , pageWidth );
            if( t==null )
                return;

            addNewPage();
            previousYLevel =  currentYLevel;
            float y = addTitle(previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle" ), null, null );
            currentYLevel = addTableToDocument(y, t, false, true );

            t = getBasicIdentityImageCaptureTableImages( reportData, tesl , pageWidth );
            if( t==null )
                return;

            previousYLevel =  currentYLevel;
            y = previousYLevel; // addTitle( previousYLevel, lmsg( "g.ImgCapReportTitle" ), lmsg( "g.ImgCapReportSubtitle" ) );
            currentYLevel = addTableToDocument(y, t, false, true );


           // LogService.logIt( "BaseCT2ReportTemplate.addIdentityImageCaptureSection() END" );
           */
    }



    public void addAltScoreSection() throws Exception
    {
        try
        {
            Profile pr;

            if( reportData.te.getAltScoreProfileList() == null && reportData.te.getTestEventScoreList( TestEventScoreType.ALT_OVERALL.getTestEventScoreTypeId() ).isEmpty() )
            {
                ProfileFacade profileFacade = ProfileFacade.getInstance();

                List<Profile> pl = profileFacade.getProfileListForProductIdAndOrgIdAndProfileUsageType( reportData.te.getProductId() , reportData.te.getOrgId(), ProfileUsageType.ALTERNATE_OVERALL_COMPETENCY_WEIGHTS.getProfileUsageTypeId()  );

                reportData.te.setAltScoreProfileList( pl );

                Collections.sort( pl, new ProfileStrParam1Comparator() );

                ListIterator<Profile> li = pl.listIterator();

                while( li.hasNext() )
                {
                    pr = li.next();

                    if( pr.getProfileEntryList()==null )
                        pr.setProfileEntryList( profileFacade.getProfileEntryList( pr.getProfileId() ));

                    pr.setAltScoreCalculator( AltScoreCalculatorFactory.getAltScoreCalculator( null, null, null, reportData.te, pr ));

                    if( !pr.getAltScoreCalculator().hasValidScore() )
                        li.remove();
                }
            }

            if( reportData.te.getAltScoreProfileList() == null || reportData.te.getAltScoreProfileList().isEmpty() )
                return;

            // LogService.logIt(  "BaseCT2ReportTemplate.addAltScoreSection() START - Found " +  reportData.te.getAltScoreProfileList().size() + " valid Alt Profiles." );

            previousYLevel =  currentYLevel;

            float y = addTitle(previousYLevel, lmsg( "g.AltScoresTitle" ), lmsg( "g.AltScoresSubtitle" ), null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 4f,6f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            //t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setPaddingBottom( 4 );
            c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            setRunDirection( c );

            // Add header row.
            t.addCell( new Phrase( lmsg( "g.Position" ) , fontLmWhite) );
            t.addCell( new Phrase( lmsg( "g.Score" ) , fontLmWhite ) );

            c.setBackgroundColor( BaseColor.WHITE );

            // Phrase ep = new Phrase( "", getFontSmall() );

            // Phrase p;

            for( Profile tt : reportData.te.getAltScoreProfileList() )
            {

                c = new PdfPCell( new Phrase( tt.getStrParam1(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( Integer.toString( (int) tt.getAltScoreCalculator().getScore() ), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

            } // each alt score

            currentYLevel = addTableToDocument(y, t, false, true );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addAltScoreSection()" );

            throw new STException( e );
        }

    }

    public void addMinQualsApplicantDataInfo() throws Exception
    {
        try
        {
            // LogService.logIt(  "BaseCT2ReportTemplate.addMinQualsApplicantDataInfo() START" );

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

            float y = addTitle(previousYLevel, lmsg( "g.AppDataAndMinQualsTitle" ), lmsg( "g.AppDataAndMinQualsSubtitle" ), null, null );

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( new float[] { 3.5f,5.5f } );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            //t.setHorizontalAlignment( Element.ALIGN_CENTER );
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            t.setHeaderRows( 1 );

            c = new PdfPCell(new Phrase( lmsg( "g.Item" ) , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, true, false, false, true ));
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell(new Phrase( "" , fontLmWhite));
            // c.setBorder( Rectangle.LEFT | Rectangle.TOP | Rectangle.RIGHT );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 4 );
            c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            // c.setBackgroundColor( ct2Colors.hraBlue );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setCellEvent(new CellBackgroundCellEvent( reportData.getIsLTR(), ct2Colors.hraBlue, false, true, true, false ));
            setRunDirection( c );
            t.addCell(c);


            // Add header row.
            //t.addCell( new Phrase( lmsg( "g.Item" ) , fontLmWhite) );
            //t.addCell( new Phrase( "" , fontLmWhite ) );

            //c.setBackgroundColor( BaseColor.WHITE );

            // Phrase ep = new Phrase( "", getFontSmall() );

            Phrase p;

            // For each competency
            for( TextAndTitle tt : ttl )
            {

                if( tt.getText() == null || tt.getText().isEmpty() )
                    tt.setText( lmsg( "g.NoResponseEntered" ) );

                c = new PdfPCell( new Phrase( tt.getTitle(), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderWidth( 0.5f );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

                c = new PdfPCell( new Phrase( tctrans( tt.getText(), false ), getFontSmall() ) );
                c.setBackgroundColor( BaseColor.WHITE );
                c.setBorder( Rectangle.LEFT | Rectangle.BOTTOM | Rectangle.RIGHT );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( 0.5f );
                c.setPadding( 6 );
                setRunDirection( c );
                t.addCell( c );

            } // each writing sample

            currentYLevel = addTableToDocument(y, t, false, true );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addMinQualsApplicantDataInfo()" );

            throw new STException( e );
        }
    }






    public void addNotesSection() throws Exception
    {
        if( reportData.getReportRuleAsBoolean( "usernotesoff" ) )
            return;

        addTitle(0 , lmsg("g.Notes"), lmsg( "g.NotesSubtitle" ), null, null );
    }



    public void addCoverPageV2(boolean includeDescriptiveText) throws Exception
    {
        try
        {
            if( reportData.getReportRuleAsBoolean( "legacycoverpage" ) )
            {
                addCoverPage(includeDescriptiveText);
                return;
            }

            if( reportData.getReportRuleAsBoolean( "covrdescripoff" ) )
                includeDescriptiveText = false;

            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 50;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();
            java.util.List<Chunk> cl = new ArrayList<>();

            boolean omitCoverImages = reportData.getReportRuleAsBoolean( "omitcoverimages" );

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


            if( reportCompanyAdminName != null && reportCompanyAdminName.indexOf( "AUTOGEN" )>=0 )
                reportCompanyAdminName = null;

            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            if( !includeCompanyInfo )
            {
                reportCompanyName = "";
                custLogo = null;
            }

            boolean compNameForAdmin = reportData.getReportRuleAsBoolean("compnameforprep") && includeCompanyInfo;

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage()  CCC.1 includeCompanyInfo=" + includeCompanyInfo + ", reportCompanyName=" + reportCompanyName + ", compNameForAdmin=" + compNameForAdmin + ", omitCoverImages=" + omitCoverImages );

            if( compNameForAdmin && !reportData.hasCustLogo() )
                includeCompanyInfo = false;


            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );

            boolean includePreparedFor = includeCompanyInfo && reportCompanyAdminName!=null && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            boolean sports = includeCompanyInfo && reportData.getReportRuleAsBoolean( "sportstest" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            if( sports || reportData.getReportRuleAsBoolean( "legacycoverpage" ) )
            {
                addCoverPage(includeDescriptiveText);
                return;
            }

            Image hraCover = getHraCoverImage();

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPageV2() START page dims=" + pageWidth + "," + pageHeight + ", imageDims=" + hraCover.getWidth() + "," + hraCover.getHeight() );

            hraCover.scalePercent( 100*pageHeight/hraCover.getHeight());

            if( !omitCoverImages )
            {
                ITextUtils.addDirectImage( pdfWriter, hraCover, pageWidth-hraCover.getScaledWidth() + 1, 0, true );

                Image hraCover2 = getHraCoverImage2();
                // LogService.logIt( "BaseCT2ReportTemplate.addCoverPageV2() hraCover2=" + (hraCover2==null ? "null" : "not null") );
                if( hraCover2 !=null )
                {
                    hraCover2.scalePercent(64);
                    ITextUtils.addDirectImage( pdfWriter, hraCover2, pageWidth-hraCover2.getScaledWidth() + 1, 0, true );
                }
            }

            boolean clientLogoInHeader = reportData.getReportRuleAsBoolean( "clientlogopdfhdr" ) && (includeCompanyInfo || compNameForAdmin) && reportData.hasCustLogo();
            if( clientLogoInHeader )
            {
                //float lwid = custLogo.getScaledWidth();
                ITextUtils.addDirectImage( pdfWriter, custLogo, 2*CT2_MARGIN, y, false );
            }
            else if( !reportData.getReportRuleAsBoolean( "hidehralogoinreports" ))
                ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), 2*CT2_MARGIN, y, false );

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage()  CCC.2 includePreparedFor=" + includePreparedFor + ", clientLogoInHeader=" + clientLogoInHeader  +", reportData.hasCustLogo()=" + reportData.hasCustLogo() + ", custLogo!=null " + (custLogo!=null));

            String reportTitle = reportData.getReportName();

            BaseFont baseTitleFont = baseFontCalibriBold;

            int titleFontHeight = 56;

            //if( 1==2 )
            //    reportTitle = "Test Results and Interview Guilde";

            if( reportTitle.length()>40)
                titleFontHeight = 36;

            else if( reportTitle.length()>32)
                titleFontHeight = 44;

            else if( reportTitle.length()>24)
                titleFontHeight = 48;

            else if( reportTitle.length()>16)
                titleFontHeight = 52;

            Font titleFont = new Font(baseTitleFont, titleFontHeight );
            titleFont.setColor(ct2Colors.hraBlue);
            String reportSubtitle = reportData.getR2Use().getStrParam5()!=null && !reportData.getR2Use().getStrParam5().isBlank() ? reportData.getR2Use().getStrParam5() : null;

            //if( 1==2 )
            //    reportSubtitle="This is a dummy subtitle for report";

            PdfPTable t = new PdfPTable( 2 );

            t.setWidths(reportData.getIsLTR() ?  new float[] {3,9}: new float[] {9,3} );
            t.setTotalWidth( (pageWidth-2*CT2_MARGIN)*0.8f);
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setVerticalAlignment(Element.ALIGN_TOP);
            setRunDirection(c);


            c = new PdfPCell( new Phrase( reportTitle, titleFont ) );
            c.setColspan(2);
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( 0 );
            c.setVerticalAlignment(Element.ALIGN_TOP);
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setPaddingBottom(3);
            setRunDirection(c);
            t.addCell(c);

            if( reportSubtitle !=null && !reportSubtitle.isBlank() )
            {
                Font subtitleFont = new Font(baseTitleFont, 12 );
                subtitleFont.setColor(ct2Colors.hraBlue);

                c = new PdfPCell( new Phrase( reportSubtitle, subtitleFont ) );
                c.setColspan(2);
                c.setBorder(Rectangle.NO_BORDER);
                c.setBorderWidth( 0 );
                c.setVerticalAlignment(Element.ALIGN_TOP);
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setPaddingBottom(3);
                setRunDirection(c);
                t.addCell(c);
            }

            // Blue Bar
            c = new PdfPCell( new Phrase( "\n\n\n\n", font ) );
            c.setColspan(2);
            c.setBorder(Rectangle.NO_BORDER);
            c.setBorderWidth( 0 );
            setRunDirection(c);

            if( !omitCoverImages )
                c.setCellEvent( new CoverBlueBarCellEvent() );
            t.addCell(c);

            String testTakerTitle = lmsg( devel ? "g.PreparedForC" : "g.CandidateC" );

            if( sports )
                testTakerTitle = lmsg( "g.AthleteC" );

            Font cpFont = this.fontXLarge;

            // Name stuff
            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setVerticalAlignment(Element.ALIGN_TOP);
            setRunDirection(c);

            if( coverInfoOk )
            {
                t.addCell(new Phrase( testTakerTitle , cpFont ) );
                t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

                t.addCell(new Phrase( lmsg( "g.AssessmentC" ) , cpFont ) );
                t.addCell(new Phrase( reportData.getSimName(), cpFont ) );

                if( includeDates )
                {
                    t.addCell(new Phrase( lmsg( "g.CompletedC" ) , cpFont ) );
                    t.addCell(new Phrase( reportData.getSimCompleteDateFormatted(), cpFont ) );
                }
            }

            String nameForPrep = compNameForAdmin  ? reportCompanyName : reportCompanyAdminName;

            if( includeCompanyInfo && includePreparedFor && nameForPrep!=null && !nameForPrep.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage() Adding prepared " + lmsg( "g.PreparedForC" ) + ", includeCompanyInfo=" + includeCompanyInfo + ", devel=" + devel );

                if( coverInfoOk && !devel && !sports )
                {
                    t.addCell(new Phrase( lmsg( "g.PreparedForC" ) + " " , cpFont ) );
                    t.addCell(new Phrase( nameForPrep, cpFont ) );
                }
            }

            float lowerTableAdj = 0;
            float lineHeight = 10;

            if( includeCompanyInfo )
            {
                if( coverInfoOk || (reportData.hasCustLogo() && custLogo!=null && !clientLogoInHeader) )
                    t.addCell(new Phrase( coverInfoOk && (devel || sports) ? lmsg( "g.SponsoredByC" ) : "" , cpFont ) );

                if( reportData.hasCustLogo() && custLogo!=null && !clientLogoInHeader )
                {
                    float imgSclW=100;
                    float imgSclH = 100;

                    if( custLogo.getWidth() > MAX_CUSTLOGO_W_V2 )
                        imgSclW = 100 * MAX_CUSTLOGO_W_V2/custLogo.getWidth();

                    if( custLogo.getHeight() > MAX_CUSTLOGO_H_V2 )
                        imgSclH = 100 * MAX_CUSTLOGO_H_V2/custLogo.getHeight();

                    imgSclW = Math.min( imgSclW, imgSclH );

                    if( imgSclW < 100 )
                        custLogo.scalePercent( imgSclW );

                    c = new PdfPCell( custLogo );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment(Element.ALIGN_LEFT );
                    c.setPadding( 0 );
                    c.setPaddingTop( 12 );
                    setRunDirection(c);
                    t.addCell( c );

                    lowerTableAdj += custLogo.getScaledHeight() + 12 - lineHeight;
                }

                else if( coverInfoOk )
                    t.addCell( new Phrase( reportCompanyName, fontXLarge ) );
            }

            float tableH = t.calculateHeights(); //  + 500;
            float tableY = pageHeight-175;


            // float tableW = t.getTotalWidth();

            float tableX = 2*CT2_MARGIN; // (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            y = tableY - tableH;

            List<String> whatsContained = new ArrayList<>();

            String includedCustom = reportData.getReportRuleAsString("includedinreportall" );  //
            if( includedCustom!=null && !includedCustom.isBlank() )
            {
                for( String ss : includedCustom.split("~") )
                {
                    if( ss.isBlank() )
                        continue;
                    whatsContained.add( ss.trim() );
                }
            }

            else
            {
                includedCustom = reportData.getReportRuleAsString("includedinreporttop" );  //
                if( includedCustom!=null && !includedCustom.isBlank() )
                {
                    for( String ss : includedCustom.split("~") )
                    {
                        if( ss.isBlank() )
                            continue;
                        whatsContained.add( ss.trim() );
                    }
                }

                if( includesOverall() )
                    whatsContained.add( lmsg("g.Cvr2Overall") );

                if( includesCompSummary() )
                    whatsContained.add( lmsg("g.Cvr2CompSummary") );

                if( includesComparison() )
                    whatsContained.add( lmsg("g.Cvr2Percentile") );

                if( includesCompetencyDetail() )
                {
                    if( !devel )
                        whatsContained.add( lmsg( reportData.includeInterview() ? "g.Cvr2DetailInterview" : "g.Cvr2Detail") );
                    else
                        whatsContained.add( lmsg( "g.Cvr2DetailDevel") );
                }
            }

            if( includesAvUploads() )
                whatsContained.add( lmsg("g.Cvr2AvUploads") );

            includedCustom = reportData.getReportRuleAsString("includedinreportbot" );  //
            if( includedCustom!=null && !includedCustom.isBlank() )
            {
                for( String ss : includedCustom.split("~") )
                {
                    if( ss.isBlank() )
                        continue;
                    whatsContained.add( ss.trim() );
                }
            }

            if( reportData.getCustomReportValues()!=null && reportData.getCustomReportValues().get("whatsincludedlist")!=null )
                whatsContained = (List<String>) reportData.getCustomReportValues().get("whatsincludedlist");

            if( whatsContained.size()>3 )
                lowerTableAdj += (whatsContained.size()-3)*lineHeight;


            int returnCt = 0;

            if( includeDescriptiveText )
            {
                if( coverDescrip!= null &&  !coverDescrip.isEmpty() )
                {}

                else if( reportData.getReport() != null && reportData.getReport().getTextParam1()!=null && !reportData.getReport().getTextParam1().isEmpty() )
                {
                    coverDescrip = reportData.getReport().getTextParam1();
                    coverDescrip = tctrans( coverDescrip, false );
                }

                else
                {
                    String coverDetailKey = reportData.getReport()!=null && reportData.getReport().getStrParam1()!=null && !reportData.getReport().getStrParam1().isEmpty() ? reportData.getReport().getStrParam1() : "g.CT2CoverDescrip";
                    coverDescrip = lmsg( coverDetailKey, new String[] {reportData.getSimName()} );
                }

                if( coverDescrip!=null )
                {
                    returnCt++;

                    int idx = coverDescrip.indexOf("\n" );
                    while( idx>=0 )
                    {
                        returnCt++;
                        idx = coverDescrip.indexOf("\n" , idx+1);
                    }
                }

                // cuont \n's in coverDescrip
            }

            Paragraph descripPar = null;

            if( includeDescriptiveText )
            {
                Font f = fontLm;
                Font fb = fontLmBold;

                if( coverDescrip!=null && coverDescrip.length()>=900 )
                {
                    f = fontSmall;
                    fb = fontSmallBold;
                }
                else if( coverDescrip!=null && coverDescrip.length()>=600 )
                {
                    f = font;
                    fb = fontBold;
                }

                if( coverDescrip!=null && coverDescrip.length()>=800 )
                    lowerTableAdj += 2*lineHeight;


                descripPar = new Paragraph();
                Chunk chk = new Chunk( lmsg("g.Cvr2ImportantNote") + ": ", fb );
                descripPar.add(chk);
                chk =  new Chunk( coverDescrip, f );
                descripPar.add(chk);
            }

            boolean addTable = false;
            t = new PdfPTable( 2 );
            t.setWidths( new float[] {0.1f, 4f} );
            t.setTotalWidth( (pageWidth-2*CT2_MARGIN)*( !omitCoverImages && getHraCoverImage2()!=null ? 0.4f : 0.7f));
            t.setLockedWidth( true );
            setRunDirection(t);


            if( !whatsContained.isEmpty() )
            {
                addTable = true;

                c = new PdfPCell( new Phrase( "\n\n\n" , this.getFontXLargeBold()) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding(0);
                c.setColspan(2);
                if( !omitCoverImages )
                    c.setCellEvent( new CoverIncludedBarCellEvent(lmsg("g.Cvr2WhatsIncluded") , this.getFontXLargeBold()));
                setRunDirection(c);
                t.addCell( c );

                for( String inc : whatsContained )
                {
                    c = new PdfPCell( new Phrase( "\u2022" , cpFont ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment(Element.ALIGN_TOP);
                    c.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c.setPadding(0 );
                    c.setPaddingRight(2);
                    setRunDirection(c);
                    t.addCell( c );

                    c = new PdfPCell( new Phrase( inc , cpFont ) );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setVerticalAlignment(Element.ALIGN_TOP);
                    c.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c.setPadding(1 );
                    c.setPaddingLeft(2);
                    setRunDirection(c);
                    t.addCell( c );
                }
            }

            if( includeDescriptiveText && descripPar!=null )
            {
                addTable = true;

                c = new PdfPCell( descripPar );
                c.setColspan(2);
                c.setBorder( Rectangle.NO_BORDER );
                c.setPaddingTop(whatsContained.isEmpty() ? 40 : 12);
                setRunDirection(c);
                t.addCell( c );
            }

            if( addTable )
            {
                // tableW = t.getTotalWidth();
                tableH = t.calculateHeights(); //  + 500;

                // tableY = y - tableH;
                y -= Math.max( 20, (120 - lowerTableAdj));

                t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );
            }

            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);


            c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFont() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( !omitCoverImages && getHraCoverImage2()!=null ? Element.ALIGN_LEFT : Element.ALIGN_CENTER );
            if( !omitCoverImages && getHraCoverImage2()!=null )
                c.setPaddingLeft( 50 );
            setRunDirection(c);
            t.addCell( c );

            // tableH = t.calculateHeights(); //  + 500;

            tableY = 20;
            float tableW = t.getTotalWidth();
            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );


        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addCoverPageV2()" );
        }
    }


    public void addCoverPage(boolean includeDescriptiveText) throws Exception
    {
        try
        {
            if( reportData.getReportRuleAsBoolean( "coverpagev2" ) )
            {
                addCoverPageV2(includeDescriptiveText);
                return;
            }

            if( reportData.getReportRuleAsBoolean( "covrdescripoff" ) )
                includeDescriptiveText = false;

            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 20;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

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


            if( reportCompanyAdminName != null && reportCompanyAdminName.indexOf( "AUTOGEN" )>=0 )
                reportCompanyAdminName = null;

            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            if( !includeCompanyInfo )
            {
                reportCompanyName = "";
                custLogo = null;
            }

            boolean compNameForAdmin = reportData.getReportRuleAsBoolean("compnameforprep") && includeCompanyInfo;

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage()  CCC.1 includeCompanyInfo=" + includeCompanyInfo + ", reportCompanyName=" + reportCompanyName + ", compNameForAdmin=" + compNameForAdmin );

            if( compNameForAdmin && !reportData.hasCustLogo() )
                includeCompanyInfo = false;


            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );

            boolean includePreparedFor = includeCompanyInfo && reportCompanyAdminName!=null && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            boolean sports = includeCompanyInfo && reportData.getReportRuleAsBoolean( "sportstest" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage() reportData.getReportRuleAsBoolean( hidehralogoinreports )=" + reportData.getReportRuleAsBoolean( "hidehralogoinreports" ) );

            if( !reportData.getReportRuleAsBoolean( "hidehralogoinreports" ))
                ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), CT2_MARGIN, y, false );

            boolean clientLogoInHeader = reportData.getReportRuleAsBoolean( "clientlogopdfhdr" ) && (includeCompanyInfo || compNameForAdmin) && reportData.hasCustLogo();
            if( clientLogoInHeader )
            {
                //float lwid = custLogo.getScaledWidth();
                ITextUtils.addDirectImage( pdfWriter, custLogo, pageWidth - CT2_MARGIN - custLogo.getScaledWidth() - PAD, y-8, false );
            }
            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage()  CCC.2 includePreparedFor=" + includePreparedFor + ", clientLogoInHeader=" + clientLogoInHeader  +", reportData.hasCustLogo()=" + reportData.hasCustLogo() + ", custLogo!=null " + (custLogo!=null));

            if( !includePreparedFor )
                reportCompanyAdminName = "";

            String testTakerTitle = lmsg( devel ? "g.PreparedForC" : "g.CandidateC" );

            if( sports )
                testTakerTitle = lmsg( "g.AthleteC" );

            Font cpFont = this.fontXLarge;

            float titleWid = 20;
            cl.add( new Chunk( testTakerTitle, cpFont ) );
            cl.add( new Chunk( lmsg( "g.AssessmentC" ), cpFont ) );

            if( includeDates )
                cl.add( new Chunk( lmsg( "g.CompletedC" ), cpFont ) );

            if( !devel && !sports && includePreparedFor && reportCompanyAdminName!=null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
                cl.add( new Chunk( lmsg( "g.PreparedForC" ), cpFont ) );
            else if( (devel || sports) && includePreparedFor && includeCompanyInfo && reportData.hasCustLogo() && !clientLogoInHeader ) // reportData.getTestKey().getAuthUser() != null )
                cl.add( new Chunk( lmsg( "g.SponsoredByC" ), cpFont ) );
            else if( sports && includePreparedFor && includeCompanyInfo ) // reportData.getTestKey().getAuthUser() != null )
                cl.add( new Chunk( lmsg( "g.SponsoredByC" ), cpFont ) );

            titleWid = ITextUtils.getMaxChunkWidth( cl ) + 20;

            cl.clear();

            cl.add( new Chunk( reportData.getUserName(), getFontXLargeBold() ) );
            cl.add( new Chunk( reportData.getSimName(), cpFont ) );

            if( includeDates )
                cl.add( new Chunk( reportData.getSimCompleteDateFormatted(), cpFont ) );

            if( !devel && !sports && includePreparedFor && reportCompanyAdminName != null && !reportCompanyAdminName.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                cl.add( new Chunk( compNameForAdmin  ? reportCompanyName : reportCompanyAdminName, cpFont ) );

                if( includeCompanyInfo && (!reportData.hasCustLogo() || custLogo==null || clientLogoInHeader) && reportCompanyName != null && !reportCompanyName.isEmpty() )
                    cl.add( new Chunk( reportCompanyName, cpFont ) );
            }

            else if( sports  && includePreparedFor && includeCompanyInfo )
            {
                cl.add( new Chunk( reportCompanyName, cpFont ) );
            }
            //else if( devel && includePreparedFor && includeCompanyInfo && reportData.hasCustLogo() ) // reportData.getTestKey().getAuthUser() != null )
            //{
            //}


            float maxTotalWidth = pageWidth - 30;

            float infoWid = ITextUtils.getMaxChunkWidth( cl ) + 10;

            if( custLogo!=null && custLogo.getScaledWidth()>infoWid )
                infoWid = custLogo.getScaledWidth();

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( 2 );

            float totalWid = titleWid + 4 + infoWid+14 + 15;

            // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage() totalWid=" + totalWid + ", maxTotalWidth=" + maxTotalWidth + ", titleWid=" + titleWid + ", infoWid=" + infoWid );

            if( maxTotalWidth>0 && totalWid>maxTotalWidth)
            {
                float gap = totalWid - maxTotalWidth;

                titleWid -= gap*0.2f;
                infoWid -= gap*0.8f;

                if( titleWid<80 )
                    titleWid=80;

                // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage() REVISED titleWid=" + titleWid + ", infoWid=" + infoWid );
            }


            t.setTotalWidth( reportData.getIsLTR() ?  new float[] { titleWid+4, infoWid+14 } : new float[] { infoWid+14,titleWid+4 } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setVerticalAlignment(Element.ALIGN_TOP);
            setRunDirection(c);

            if( coverInfoOk )
            {
                t.addCell(new Phrase( testTakerTitle , cpFont ) );
                t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

                t.addCell(new Phrase( lmsg( "g.AssessmentC" ) , cpFont ) );
                t.addCell(new Phrase( reportData.getSimName(), cpFont ) );

                if( includeDates )
                {
                    t.addCell(new Phrase( lmsg( "g.CompletedC" ) , cpFont ) );
                    t.addCell(new Phrase( reportData.getSimCompleteDateFormatted(), cpFont ) );
                }
            }

            String nameForPrep = compNameForAdmin  ? reportCompanyName : reportCompanyAdminName;

            if( includeCompanyInfo && includePreparedFor && nameForPrep!=null && !nameForPrep.isEmpty() ) // reportData.getTestKey().getAuthUser() != null )
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addCoverPage() Adding prepared " + lmsg( "g.PreparedForC" ) + ", includeCompanyInfo=" + includeCompanyInfo + ", devel=" + devel );

                if( coverInfoOk && !devel && !sports )
                {
                    t.addCell(new Phrase( lmsg( "g.PreparedForC" ) + " " , cpFont ) );
                    t.addCell(new Phrase( nameForPrep, cpFont ) );
                }
            }

            if( includeCompanyInfo )
            {
                if( coverInfoOk || (reportData.hasCustLogo() && custLogo!=null && !clientLogoInHeader) )
                    t.addCell(new Phrase( coverInfoOk && (devel || sports) ? lmsg( "g.SponsoredByC" ) : "" , cpFont ) );

                if( reportData.hasCustLogo() && custLogo!=null && !clientLogoInHeader )
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

            // t.addCell( "\n\n\n\n\n\n\n\n\n" );

            String reportSubtitle = reportData.getR2Use().getStrParam5()!=null && !reportData.getR2Use().getStrParam5().isBlank() ? reportData.getR2Use().getStrParam5() : null;

            if( !includeDescriptiveText )
                t.addCell( "\n\n\n\n\n" );

            c = new PdfPCell( new Phrase( tctrans( reportData.getReportName(), false ) , getHeaderFontXXLargeWhite() ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            if( reportSubtitle!=null && !reportSubtitle.isBlank() )
            {
                c = new PdfPCell( new Phrase( reportSubtitle , getFontWhite() ) );
                // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
                c.setBorder( Rectangle.NO_BORDER );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                setRunDirection(c);
                t.addCell( c );
            }


            int returnCt = 0;

            if( includeDescriptiveText )
            {
                if( coverDescrip != null &&  !coverDescrip.isEmpty() )
                {}

                else if( reportData.getReport() != null && reportData.getReport().getTextParam1()!=null && !reportData.getReport().getTextParam1().isEmpty() )
                {
                    coverDescrip = reportData.getReport().getTextParam1();

                    coverDescrip = tctrans( coverDescrip, false );
                }

                else
                {
                    String coverDetailKey = reportData.getReport()!=null && reportData.getReport().getStrParam1()!=null && !reportData.getReport().getStrParam1().isEmpty() ? reportData.getReport().getStrParam1() : "g.CT2CoverDescrip";
                    coverDescrip = lmsg( coverDetailKey, new String[] {reportData.getSimName()} );
                }

                if( coverDescrip!=null )
                {
                    returnCt++;

                    int idx = coverDescrip.indexOf("\n" );
                    while( idx>=0 )
                    {
                        returnCt++;
                        idx = coverDescrip.indexOf("\n" , idx+1);
                    }
                }

                // cuont \n's in coverDescrip
            }

            int rc = 9 - returnCt;

            if( rc<1)
                rc=1;

            String rets = "";
            for( int i=0;i<rc;i++ )
                rets += "\n";

            t.addCell( rets );

            //String coverDescrip=null;

            if( includeDescriptiveText )
            {
                Font f = fontLLWhite;
                if( coverDescrip!=null && coverDescrip.length()>=1100 )
                    f = fontWhite;
                else if( coverDescrip!=null && coverDescrip.length()>=900 )
                    f = fontLmWhite;

                c = new PdfPCell( new Phrase( coverDescrip , f ) );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection(c);

                t.addCell( c );
            }

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
            LogService.logIt( e, "BaseCT2ReportTemplate.addCoverPage()" );
        }
    }



    public float addContinuedNextPage( float startY, String text ) throws Exception
    {
        try
        {
            //LogService.logIt( "BaseCt2ReportTemplate.addContinuedNextPage() AAA startY=" + startY );
            if( startY <= MIN_HEIGHT_FOR_CONTINUED_TEXT ) //12*PAD )
                return startY;

            if( text==null || text.isBlank() )
                text = lmsg( "g.ContdNxtPg");

            previousYLevel =  currentYLevel;

            PdfPTable t = new PdfPTable( 1 );
            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = new PdfPCell( new Phrase(text, getFont()) );
            c.setPadding( TPAD );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            float tableH = t.calculateHeights();
            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            float y = (startY - TPAD )*0.75f;

            if( y < tableH + 20 )
            {
                // table will not fit.
                if( startY < tableH + 15 )
                    return startY;

                y = startY - 10;
            }

            //LogService.logIt( "BaseCt2ReportTemplate.addContinuedNextPage() BBB startY=" + startY + ", tableH=" + tableH +", y=" + y );

            //if( y + tableH + TPAD >= startY )
            //{
                //LogService.logIt( "BaseCt2ReportTemplate.addContinuedNextPage() CCC returning because y + tableH + TPAD=" + (y + tableH + TPAD) + " is greater than startY=" + startY );
            //    return startY;
            //}

            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );

            return y;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addContinuedNextPage()" );
            throw new STException( e );
        }
    }



    public float getTitleHeight( String title, String subtitle ) throws Exception
    {
        try
        {
            Font fnt =   getHeaderFontXLarge();
            // Change getFont()
            float leading = fnt.getSize();

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            // float y = previousYLevel - 6*PAD - getFont().getSize();
            float h = ITextUtils.getDirectTextHeight( pdfWriter, title, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

            h += PAD;

            // No subtitle
            if( subtitle==null || subtitle.isEmpty() )
                return h;

            // Change getFont()
            fnt =  getFont();
            leading = fnt.getSize();

            h += ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);
            h += PAD;

            return h;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTitleAndSubtitle()" );
            throw new STException( e );
        }
    }



    public float addTitle( float startY, String title, String subtitle, Font forceTitleFont, Font forceSubtitleFont) throws Exception
    {
        try
        {
            if( !reportData.getIsLTR() )
                return addTitleRTL( startY,  title,  subtitle );


            float tHeight = getTitleHeight( title, subtitle );
            if( startY > 0 )
            {
                float ulY = startY - PAD - tHeight;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    startY = 0;
                    currentYLevel = pageHeight - PAD -  headerHgt;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt = forceTitleFont!=null ? forceTitleFont : getHeaderFontXLarge();
            // float leading = fnt.getSize();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            // No subtitle
            if( subtitle==null || subtitle.isEmpty() )
                return y;

            // Change getFont()
            fnt = forceSubtitleFont!=null ? forceSubtitleFont : getFont();

            float leading = fnt.getSize();

            float spaceLeft = y - PAD - footerHgt;

            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

             y -=  PAD;//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();
            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText. If RTL need to use Column Text anyway.
            if( reportData.getIsLTR() && txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, subtitle, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt, false );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() RTL or overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

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
                    // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() adding second column "  );

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
            LogService.logIt( e, "BaseCT2ReportTemplate.addTitleAndSubtitle()" );
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
            LogService.logIt( e, "BaseCT2ReportTemplate.addTitleRTL()" );
            throw new STException( e );
        }
    }



    public float addSimpleText( float startY, String subtitle ) throws Exception
    {
        try
        {
            if( startY > 0 )
            {
                float ulY = startY - 6* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();
                    startY = 0;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt = getFont();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            // Change getFont()
            fnt =  getFont();

            float leading = fnt.getSize();

            float spaceLeft = y - PAD - footerHgt;

            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

            // y -=  PAD;//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();
            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText. If RTL need to use Column Text anyway.
            if( reportData.getIsLTR() && txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, subtitle, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt, false );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() RTL or overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

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
                    // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() adding second column "  );

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
            LogService.logIt( e, "BaseCT2ReportTemplate.addTitleAndSubtitle()" );
            throw new STException( e );
        }
    }


    public float getHeaderPlusFirstRowHeight( PdfPTable t ) throws Exception
    {
        float tableHeight = 0;
        try
        {
            tableHeight = t.calculateHeights(); //  + 500;
            float tableHeaderHeight = t.getHeaderHeight();

            int rowCount = t.getRows().size(); //  - t.getHeaderRows() - t.getFooterRows();

            float[] rowHgts = new float[rowCount];

            for( int i=0; i<rowCount; i++ )
            {
                rowHgts[i]=t.getRowHeight(i);
            }

            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;

            return tableHeaderHeight + firstRowHgt;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTableToDocument() testEventId=" + reportData.te.getTestEventId() );
            return tableHeight;
        }
    }



    public float addTableToDocument( float startY, PdfPTable t, boolean onePageIfPossible, boolean addContinuedTextIfNeeded) throws Exception
    {
        try
        {
            float ulY = startY - 2*PAD;  // 4* PAD;

            float tableHeight = t.calculateHeights(); //  + 500;
            float tableHeaderHeight = t.getHeaderHeight();

            int rowCount = t.getRows().size(); //  - t.getHeaderRows() - t.getFooterRows();

            float maxRowHeight=0;

            float[] rowHgts = new float[rowCount];

            for( int i=0; i<rowCount; i++ )
            {
                rowHgts[i]=t.getRowHeight(i);
                maxRowHeight = Math.max( maxRowHeight, rowHgts[i] );
                // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() row=" + i + ", rowHeight=" + rowHgts[i] );
            }


            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;

            float heightAvailNewPage = pageHeight - headerHgt - 3*PAD - footerHgt - 3*PAD - tableHeaderHeight;


            // commented out on 4/8/2022 because we DO have some rows bigger than half a page. Still, do not want to splitLate.
            //if( maxRowHeight >= heightAvailNewPage*0.5 )
            //    t.setSplitLate(false);

            // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() rows=" + rowCount + ", tableHeight=" + tableHeight + ", tableHeaderHeight=" + tableHeaderHeight + ", maxRowHeight=" + maxRowHeight + ", splitLate=" + t.isSplitLate() );

            if( onePageIfPossible && tableHeight<=(heightAvailNewPage - 3*PAD) && tableHeight > (ulY - footerHgt - 3*PAD) )
            {
                if( ulY >= MIN_HEIGHT_FOR_CONTINUED_TEXT )
                    addContinuedNextPage(ulY, null );

                // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
                // currentYLevel = pageHeight - PAD -  headerHgt;
            }

            // If first row doesn't fit on this page
            else if( firstRowHgt > ulY- footerHgt - 3*PAD - tableHeaderHeight ) // ulY < footerHgt + 8*PAD )
            {
                if( ulY >= MIN_HEIGHT_FOR_CONTINUED_TEXT )
                    addContinuedNextPage(ulY, null );

                // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
            }

            //else if( 1==1 )
            //{
            //    if( ulY >= MIN_HEIGHT_FOR_CONTINUED_TEXT )
            //        addContinuedNextPage(ulY, null );

                // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() adding new page. "  );
            //    document.newPage();
            //    ulY = pageHeight - headerHgt - 3*PAD;
            //}

            //if( maxRowHeight > usablePageHeight )
            //    t.setSplitLate(false);
            float tableXlft = CT2_MARGIN + CT2_BOX_EXTRAMARGIN;
            float tableXrgt = CT2_MARGIN + CT2_BOX_EXTRAMARGIN + t.getTotalWidth();

            Rectangle colDims = new Rectangle( tableXlft, footerHgt + 3*PAD, tableXrgt, ulY );
            // LogService.logIt( "BaseCT2ReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

            float heightNoHeader = tableHeight - tableHeaderHeight;


            Object[] dta = calcTableHghtUsed(colDims.getTop() - colDims.getBottom() - tableHeaderHeight, 0, t.getHeaderRows(), rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
            int nextIndex = (Integer) dta[0];
            float heightUsedNoHeader = (Float) dta[1];
            float residual = (Float) dta[2];

            // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() tableHeight=" + t.calculateHeights() + ", headerHeight=" + headerHeight + ", maxRowHeight=" + maxRowHeight + ", heightAvailNewPage=" + heightAvailNewPage + ", initial heightUsedNoHeader=" + heightUsedNoHeader + ", residual=" + residual );


            ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );
            setRunDirection( ct );

            // NOTE - this forces Composite mode (using ColumnText.addElement)
            ct.addElement( t );

            ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );
            // ct.setSimpleColumn( colDims1 );


            int status = ct.go();

            // int linesWritten = ct.getLinesWritten();

            // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() initial lines written. NO_MORE_COLUMN=" + ColumnText.NO_MORE_COLUMN + ", NO_MORE_TEXT=" + ColumnText.NO_MORE_TEXT  );

            int pages = 0;

            float heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

            float hgtUsedThisPage;

            // If need to add any pages
            // while( ColumnText.hasMoreText( status ) && heightNeededNoHeader>0 && pages<20 )
            while( ColumnText.hasMoreText( status ) && heightNeededNoHeader >-300 && pages<20 ) // 6-28-2019 - removed the restriction on height as there's something not quite right.
            {
                // Top of writable area
                ulY = pageHeight - headerHgt - 3*PAD;


                dta = calcTableHghtUsed(heightAvailNewPage, residual, nextIndex, rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
                nextIndex = (Integer) dta[0];
                hgtUsedThisPage = (Float) dta[1];
                residual = (Float) dta[2];

                heightUsedNoHeader += hgtUsedThisPage;

                heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

                // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() AFTER adding next page. hgtUsedThisPage=" + hgtUsedThisPage +  ", Total HeightNeededNoHeader=" + heightNeededNoHeader + ", Total HeightUsedNoHeader=" + heightUsedNoHeader + ", pages=" + pages );

                colDims = new Rectangle( tableXlft, ulY - heightAvailNewPage , tableXrgt, ulY );

                document.newPage();

                ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );

                ct.setYLine( colDims.getTop() );

                status = ct.go();

                // linesWritten += ct.getLinesWritten();

                // LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() status=" + status + ", ColumnText.hasMoreText( status )=" + ColumnText.hasMoreText( status ) + ", pages=" + pages );

                pages++;
            }

            return ct.getYLine();
        }

        catch( ExceptionConverter e  )
        {
            Exception ee = e.getException();

            if( ee!=null && ( ee instanceof com.itextpdf.text.pdf.BadPdfFormatException || ee instanceof com.itextpdf.text.pdf.PdfException ) )
                LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() (ExceptionConverter) " + ee.toString() + ", testEventId=" + reportData.te.getTestEventId() );
            else
                LogService.logIt( e, "BaseCT2ReportTemplate.addTableToDocument() (ExceptionConverter) " + ee.toString() + ", testEventId=" + reportData.te.getTestEventId() );

            return startY;
        }
        catch( com.itextpdf.text.pdf.BadPdfFormatException e  )
        {
            LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() (BadPdfFormat) " + e.toString() + ", testEventId=" + reportData.te.getTestEventId() );
            return startY;
        }
        catch( com.itextpdf.text.pdf.PdfException e )
        {
            LogService.logIt( "BaseCT2ReportTemplate.addTableToDocument() (Pdf) " + e.toString() + ", testEventId=" + reportData.te.getTestEventId() );
            return startY;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseCT2ReportTemplate.addTableToDocument() testEventId=" + reportData.te.getTestEventId() );
            return startY;
        }
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

        Object[] dta = new Object[] {startIndex, Float.valueOf(0), Float.valueOf(0)};

        if( rowHgts.length<=startIndex )
            return dta;

        float hgt = 0;
        float resid = 0;

        if( prevResidual>0 )
        {
            // Bigger than max
            if( prevResidual>= maxRoom )
            {
                dta[1] = Float.valueOf(maxRoom);
                dta[2] = Float.valueOf(prevResidual -  maxRoom);

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
                dta[0]=i+1;
                dta[1] = Float.valueOf(hgt);
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



    public float getMinYForNewSection()
    {
        if( pageHeight>100 )
            return 0.2f*pageHeight;

        return 200f;
    }

    public void addNewPage() throws Exception
    {
        document.newPage();
        currentYLevel = pageHeight - PAD -  headerHgt;
    }


    // test content trans
    public String tctrans( String srcTxt, boolean dynamic )
    {
        if( srcTxt == null )
            return "";

        if( srcTxt.trim().isEmpty() )
            return srcTxt;

        return tctrans( srcTxt, reportData.getTestContentLocale(), dynamic );
    }

    public String tctrans( String srcTxt, Locale srcLocale, boolean dynamic )
    {
        if( srcTxt == null )
            return "";

        if( srcTxt.trim().isEmpty() )
            return srcTxt;

        if( !reportData.getNeedsTestContentTrans() )
            return srcTxt;

        String s = languageUtils.getTextTranslation(srcTxt, srcLocale, reportData.getLocale(), dynamic );

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

            String s = languageUtils.getKeyValueStrict(reportData.getTestContentLocale(), reportData.getLocale(), key, null );

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

            String s = languageUtils.getKeyValueStrict(reportData.getTestContentLocale(), reportData.getLocale(), key, prms );

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

    private String getDegreeOfMatchString( float matchPercent, boolean text, boolean includePercentWithPct)
    {
        String[] mp = new String[] {I18nUtils.getFormattedInteger( reportData.getLocale(), Math.round( matchPercent) ) };

        if( matchPercent<=50 )
            return lmsg( text ? "g.DegreeTxtSome" + (includePercentWithPct ? "Num" : "") : "g.DegreeNumSome", mp );
        if( matchPercent<=75 )
            return lmsg( text ? "g.DegreeTxtMedium" + (includePercentWithPct ? "Num" : "") : "g.DegreeNumMedium", mp );
        if( matchPercent<=90 )
            return lmsg( text ? "g.DegreeTxtStrong" + (includePercentWithPct ? "Num" : "") : "g.DegreeNumStrong", mp );

        return lmsg( text ? "g.DegreeTxtVeryStrong" + (includePercentWithPct ? "Num" : "") : "g.DegreeNumVeryStrong", mp );
    }


    public boolean includesOverall()
    {
        if( reportData==null )
            return false;

        if( reportData.getR2Use().getIncludeOverallScore()==0 && reportData.getR2Use().getIncludeOverallCategory()==0 )
            return false;

        if( reportData.getReportRuleAsBoolean("ovroff") )
            return false;

        return !reportData.getReportRuleAsBoolean( "ovrdetailedoff" );
    }


    public boolean includesCompSummary()
    {
        if( reportData==null )
            return false;

        // If no info to present.
        if( reportData.getReportRuleAsBoolean("cmptysumoff") ||
            reportData.getReport().getIncludeCompetencyScores()!=1 ||
            ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
              reportData.getReport().getIncludeSubcategoryNumeric()!=1 &&
              reportData.getReport().getIncludeCompetencyColorScores()!=1 )  )
              return false;


        java.util.List<TestEventScore> teslA = reportData.getTestEvent().getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId());
        if( teslA.isEmpty() )
            return false;

        for( TestEventScore tes : teslA )
        {
            if( tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDAVUPLOAD) || tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDIMAGEUPLOAD))
                continue;

            if( SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
            {
                return true;
            }
        }
        return false;
    }

    public boolean includesComparison()
    {
        if( reportData==null )
            return false;

        if( reportData.getReportRuleAsBoolean( "skipcomparisonsection" ) )
            return false;

        if( reportData.getReport().getIncludeNorms()==0 )
            return false;

        TestEventScore otes = reportData.getTestEvent().getOverallTestEventScore();

        if( otes==null )
            return false;

        if( !otes.getHasValidNorms() && !otes.getHasValidOverallZScoreNorm() )
        {
            return false;
        }

        return true;
    }

    public boolean includesCompetencyDetail()
    {
        if( reportData==null )
            return false;

        if( !reportData.includeCompetencyScores() )
            return false;

        if( reportData.getReportRuleAsBoolean( "hidecompetencydetail" ) )
            return false;

        java.util.List<TestEventScore> teslA = reportData.getTestEvent().getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId());
        if( teslA.isEmpty() )
            return false;

        for( TestEventScore tes : teslA )
        {
            if( tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDAVUPLOAD) || tes.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDIMAGEUPLOAD))
                continue;

            if( SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
            {
                if( !reportData.getReportRuleAsBoolean("hidecompetencyinterview" + SimCompetencyGroupType.getValueForSimCompetencyClass(tes.getSimCompetencyClass()).getSimCompetencyGroupTypeId() ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean includesAvUploads()
    {
        if( reportData==null )
            return false;

        if( reportData.getReportRuleAsBoolean( "hideavsampleinfoforall") )
            return false;

        java.util.List<TestEventScore> teslst = getTestEventScoreListToShow(TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAVUPLOAD, true, true ); // new ArrayList<>();
            // teslst.addAll( getTestEventScoreListToShow( TestEventScoreType.COMPETENCY, SimCompetencyClass.SCOREDAVUPLOAD, true ) ); // new ArrayList<>();

            //LogService.logIt( "AvReportTemplate.addAvUploadSampleInfo() A1 found " + teslst.size() + " ScoredAVUpload TestEventScore records." );

        if( teslst.isEmpty() )
            return false;

        return true;
    }
    public PdfPTable getScoreTextAndCaveatScoreTable(String scoreText, String headerStr, String footerStr, String scoreTextP2, List<CaveatScore> csl, Font fontToUse, int simCompetencyClassId, boolean hideCaveats ) throws Exception
    {
        if( (scoreText==null ||scoreText.isBlank()) && 
            (scoreTextP2==null || scoreTextP2.isBlank()) && 
            (hideCaveats || csl==null || csl.isEmpty()))
            return null;
        
        //if( (csl==null || csl.isEmpty())  )
        //    return null;

        try
        {
            PdfPTable t = new PdfPTable(new float[]{1.75f,1});
            t.setWidthPercentage(100);
            setRunDirection(t);
            t.setHorizontalAlignment(reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );

            PdfPCell c;
            
            
            if( scoreText!=null && !scoreText.isBlank() )
            {
                c = new PdfPCell(new Phrase(scoreText, fontToUse));
                c.setColspan(2);
                c.setBorderWidth(0);
                c.setPadding(0);
                c.setPaddingTop(2);
                c.setPaddingBottom(10);
                // c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);                
            }
            
            if( headerStr!=null && !headerStr.isBlank() )
            {
                c = new PdfPCell(new Phrase(headerStr, fontToUse));
                c.setColspan(2);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setPaddingLeft(reportData.getIsLTR() ? 0 : 2 );
                c.setPaddingRight(reportData.getIsLTR() ? 2 : 0 );
                // c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);                
            }
            
            for (CaveatScore cs : csl)
            {
                if (cs.getLocale() == null)
                    cs.setLocale(reportData.getLocale());

                // Either the last or the second to last CS can be a Topic.
                if( cs.getIsTopic() )
                {
                    String[] dd = ReportUtils.parseTopicCaveatScore(cs, csl.size()==1, simCompetencyClassId);

                    // c = new PdfPCell(new Phrase("\u2022 " + dd[1] + ":", fontToUse));
                    c = new PdfPCell(new Phrase(dd[1] + ":", fontToUse));
                    //c.setBackgroundColor( BaseColor.ORANGE);
                    c.setColspan(1);
                    c.setBorderWidth(0);
                    c.setPadding(2);                    
                    c.setPaddingLeft(reportData.getIsLTR() ? 0 : 2 );
                    c.setPaddingRight(reportData.getIsLTR() ? 2 : 0 );
                    c.setVerticalAlignment( Element.ALIGN_TOP);
                    //c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    setRunDirection(c);
                    t.addCell(c);

                    c = new PdfPCell(new Phrase(dd[2], fontToUse));
                    //c.setBackgroundColor( BaseColor.ORANGE);
                    c.setColspan(1);
                    c.setBorderWidth(0);
                    c.setPadding(2);
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                    c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                    setRunDirection(c);
                    t.addCell(c);
                    
                    continue;
                }
                
                // c = new PdfPCell(new Phrase("\u2022 " + cs.getCol1() + (cs.getCaveatScoreType().getColspan()==1 ? ":" : ""), fontToUse));
                c = new PdfPCell(new Phrase(cs.getCol1() + (cs.getCaveatScoreType().getColspan()==1 ? ":" : ""), fontToUse));
                //c.setBackgroundColor( BaseColor.ORANGE);
                c.setColspan(cs.getCaveatScoreType().getColspan());
                c.setBorderWidth(0);
                c.setPadding(2);
                    c.setPaddingLeft(reportData.getIsLTR() ? 0 : 2 );
                    c.setPaddingRight(reportData.getIsLTR() ? 2 : 0 );
                //c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setVerticalAlignment( Element.ALIGN_TOP);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);
                
                if(cs.getCaveatScoreType().getColspan()>1)
                    continue;
                
                c = new PdfPCell(new Phrase(cs.getCol2(), fontToUse));
                //c.setBackgroundColor( BaseColor.ORANGE);
                c.setColspan(1);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setVerticalAlignment( Element.ALIGN_MIDDLE);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);
            }
            
            // Footer
            if( footerStr!=null && !footerStr.isBlank() )
            {
                c = new PdfPCell(new Phrase(footerStr, fontToUse));
                c.setColspan(2);
                c.setBorderWidth(0);
                c.setPadding(2);
                c.setPaddingLeft(reportData.getIsLTR() ? 0 : 2 );
                c.setPaddingRight(reportData.getIsLTR() ? 2 : 0 );
                // c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);                
            }

            if( scoreTextP2!=null && !scoreTextP2.isBlank() )
            {
                c = new PdfPCell(new Phrase(scoreTextP2, fontToUse));
                c.setColspan(2);
                c.setBorderWidth(0);
                c.setPadding(0);
                c.setPaddingTop(2);
                c.setPaddingBottom(2);
                
                if( !hideCaveats && csl!=null && !csl.isEmpty() )
                    c.setPaddingTop(10);
                // c.setPaddingLeft(CT2_BOXHEADER_LEFTPAD);
                c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
                setRunDirection(c);
                t.addCell(c);                
            }
            
            return t;
        } catch (Exception e)
        {
            LogService.logIt(e, "Ct2ReportSettings.getCaveatScoreTable() ");
            throw e;
        }
    }
    
    
    
}
