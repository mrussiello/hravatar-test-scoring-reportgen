/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.ai.AiMetaScoreType;
import com.tm2score.battery.BatteryScoreType;
import com.tm2score.battery.BatteryScoringUtils;
import com.tm2score.battery.BatteryType;
import com.tm2score.bot.ChatMessageType;
import com.tm2score.entity.ai.AiMetaScore;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.entity.proctor.ProctorEntry;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.proctor.ProctorSuspension;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.proctor.SuspiciousActivity;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Resume;
import com.tm2score.entity.user.Suborg;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TESScoreComparator;
import com.tm2score.event.TestEventResponseRatingUtils;
import com.tm2score.event.TestEventScoreStatusType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.file.FileContentType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.file.MediaTempUrlSourceType;
import com.tm2score.global.Constants;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.ibmcloud.HraTrait;
import com.tm2score.ibmcloud.SentinoUtils;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.json.JsonUtils;
import com.tm2score.proctor.ProctorHelpUtils;
import com.tm2score.proctor.ProctorUtils;
import com.tm2score.proctor.SuspiciousActivityThresholdType;
import com.tm2score.proctor.SuspiciousActivityType;
import com.tm2score.proctor.SuspiciousKeyCodeType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.purchase.ProductType;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportUtils;
import com.tm2score.report.ReportRules;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.ScorePresentationType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.sim.SimJUtils;
import com.tm2score.user.AssistiveTechnologyType;
import com.tm2score.user.ResumeEducation;
import com.tm2score.user.ResumeExperience;
import com.tm2score.user.UserFacade;
import com.tm2score.user.UserType;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.JaxbUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import jakarta.json.JsonObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *
 * @author Mike
 */
public class BaseScoreFormatter
{
    // OK STATIC VALUES
    public static String BLANK_ROW_STYLE = " style=\"background-color:#ffffff;vertical-align:top\"";

    public static String AUDIO_ICON_URL = null;
    public static String VIDEO_ICON_URL = null;
    public static String GENFILE_ICON_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_4x1517691500437.png";
    public static String EXCEL_ICON_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_5x1517691501144.png";
    public static String PPT_ICON_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_7x1517691502319.png";
    public static String WORD_ICON_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_8x1517691502892.png";
    public static String PDF_ICON_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_6x1517691501742.png";
    public static String IMAGE_ICON_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_9x1517693458213.png";



    // DO NOT MAKE STATIC - changed by
    protected int MIN_COUNT_FOR_PERCENTILE = 10;

    // decimal points
    // protected int SCORE_PRECISION = 0;
    public String rowStyleHdr = " style=\"background-color:#e6e6e6;vertical-align:top\"";
    public String rowStyleHdrRedTxt = " style=\"background-color:#e6e6e6;vertical-align:top;color:red\"";
    public String rowStyleHdrRed = " style=\"background-color:#ff0000;vertical-align:top;color:white\"";
    public String rowStyleSubHdr = " style=\"background-color:#ffffff;vertical-align:top\"";
    public String rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
    public String rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
    public String rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";
    public String rowStyle3 = " style=\"background-color:#c0c0c0;vertical-align:top\"";





    public TestKey tk;
    public TestEvent te;


    public BatteryScore bs;

    public TestEventScore overallTes;

    public Report report;

    public Org org;

    public Suborg suborg;

    // public Profile profile;



    public boolean includeOverall;
    public boolean includeNorms;
    public boolean includeOverview;
    public boolean includeWritingSamples;
    public boolean includeIbmInsight;
    public boolean includeAppData;
    public boolean includeMinQuals;
    public boolean includeCompetencyScores;
    public boolean includeTaskScores;
    public boolean includeBiodataScores;
    public boolean includeNumeric;
    public boolean includeCategory;
    public boolean includeColors;
    public boolean includeInterview;
    public boolean includeCompetencyDescriptions;
    public boolean includeEducTypeDesc;
    public boolean includeTrainingTypeDesc;
    public boolean includeExperTypeDesc;
    public boolean includeTaskInterest;
    public boolean includeTaskExper;
    public boolean includeRedFlags;

    public boolean includeSubcategoryNumeric;
    public boolean includeSubcategoryNorms;
    public boolean includeSubcategoryCategory;
    public boolean includeSubcategoryColors;

    // public boolean hasUploadedFiles;

    public Locale locale;

    public String[] params;

    public User u;
    public User authU;

    public UserType userType;
    public boolean anon;
    public boolean pseudo;

    public boolean batt;

    public ScoreFormatType scoreFormatType;
    public ScoreCategoryType scoreCategoryType;
    public ScoreColorSchemeType scoreColorSchemeType;

    /**
     * Map of Topic Name : Equivalent Topic Name
     */
    private Map<String,String> equivTopicNameMap;


    public int scoreCat = 0;
    public float score = 0;

    public static String baseImageUrl;

    // public List<NVPair> reportRules = null;
    public ReportRules reportRules;

    FileUploadFacade fileUploadFacade;

    public int addLimitedAccessLinkInfo = 0;

    public String testNameToUse;

    public ProctorUtils proctorUtils;


    public BaseScoreFormatter()
    {
    }




    public void init( TestKey tk, TestEvent te, Report r , Locale l, int addLimitedAccessLinkInfo ) throws Exception
    {
        if( baseImageUrl == null )
            baseImageUrl = RuntimeConstants.getStringValue( "awsBaseUrl" ) + "/" + RuntimeConstants.getStringValue( "colorDotsFolder" );

        if( AUDIO_ICON_URL== null )
            AUDIO_ICON_URL = RuntimeConstants.getStringValue( "ivrCustomTestAudioPlayIconUrl" );

        if( VIDEO_ICON_URL== null )
            VIDEO_ICON_URL = RuntimeConstants.getStringValue( "avCustomTestVideoPlayIconUrl" );

        this.tk = tk;
        this.te = te;
        this.report = r;
        this.bs = tk.getBatteryScore();
        // this.profile = te.getProfile();
        this.addLimitedAccessLinkInfo = addLimitedAccessLinkInfo;

        try
        {
            if( tk == null )
                throw new Exception( "TestKey is null." );

            if( tk.getUser() == null )
                throw new Exception( "TestKey.user is null." );

            if( tk.getBatteryProduct() == null )
                throw new Exception( "TestKey.product is null." );

            if( tk.getTestEventList() == null )
                throw new Exception( "TestKey.testEventList is null." );

            if( tk.getBatteryId() > 0 && tk.getBattery() !=null && tk.getBattery().getBatteryScoreType().needsScore() && tk.getBatteryScore() == null )
                throw new Exception( "No Battery Score found in TestKey.");

            locale = l; //  tk.getLocaleStr() == null || tk.getLocaleStr().isEmpty() ? Locale.US : I18nUtils.getLocaleFromCompositeStr(  tk.getLocaleStr() );

            u = tk.getUser();

            userType = u.getUserType();
            anon = u.getUserType().getAnonymous();
            pseudo = u.getUserType().getPseudo();

            authU = tk.getAuthUser();

            org = tk.getOrg();

            suborg = tk.getSuborg();

            batt = tk.getBatteryId()> 0; //  && tk.getBatteryScore()!= null;

            params = new String[16];

            //if( batt )
            //{
            //    Battery b = tk.getBattery();

            //    String nm = tk.getBatteryProduct().getName();

            //    if( b!=null && b.getBatteryType().equals( BatteryType.MULTIUSE ) && b.getName()!=null && !b.getName().isEmpty() )
            //        nm = b.getName();

            //    params[0] = nm;
            //}

            if( te == null )
                te = tk.getTestEventList().get( 0 );

            testNameToUse = getThirdPartyTestName();

            if( testNameToUse==null || testNameToUse.isEmpty() )
                testNameToUse = ReportUtils.getTestNameToUseInReporting(te, te.getProduct(), locale );


            params[13] = RuntimeConstants.getStringValue("baseadmindomain");
            params[14] = RuntimeConstants.getStringValue("default-site-name");
            params[15] = getPostCandidateContactStr();

            //else
            params[0] = testNameToUse; // te==null ? tk.getTestEventList().get(0).getProduct().getName() : te.getProduct().getName();

            params[1] = anon || pseudo ? "" : u.getLastName();
            params[2] = anon || pseudo ? "" : u.getFirstName();

            if( userType.getUsername() || userType.getUserId() )
            {
                params[1] = u.getEmail();
                params[2] = "";
                params[3] = u.getEmail();
            }

            else if( pseudo )
                params[3] = MessageFactory.getStringMessage( locale, "g.Pseudonymized" );

            else
                params[3] = anon ? MessageFactory.getStringMessage( locale, "g.Anonymous" ) + (u.getExtRef()==null || u.getExtRef().isEmpty() ? "" : " (" + u.getExtRef() +")") : u.getFullname();

            params[4] = !userType.getNamed() || StringUtils.isCurlyBracketed( u.getEmail() ) ? "" : u.getEmail();

            if( batt && tk.getBatteryScore()!=null )
            {
                bs = tk.getBatteryScore();
                score = bs.getScore();
                scoreCat = bs.getScoreCategoryId();
                scoreFormatType = bs.getScoreFormatType();
            }

            //else
            //{
                if( te == null )
                    te = tk.getTestEventList().get( 0 );

                if( te!=null )
                    scoreColorSchemeType = te.getScoreColorSchemeType();

                if( te!=null && (te.getTestEventScoreList()==null || te.getTestEventScoreList().isEmpty()) )
                {
                    ProductType pt = ProductType.getValue(te.getProductTypeId());

                    // this is OK
                    if( pt.getIsIFrameTest() )
                    {
                        overallTes=new TestEventScore();
                        overallTes.setTestEventScoreTypeId( TestEventScoreType.OVERALL.getTestEventScoreTypeId() );
                        overallTes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
                        overallTes.setTestEventId(te.getTestEventId());
                        overallTes.setScore(te.getOverallScore());
                        overallTes.setCreateDate(new Date());
                        overallTes.setDateParam1( new Date());
                        overallTes.setPercentile( te.getOverallPercentile() );
                        overallTes.setOverallPercentileCount( te.getOverallPercentileCount() );
                        overallTes.setCountryPercentile(te.getCountryPercentile());
                        overallTes.setCountryPercentileCount(te.getCountryPercentileCount());
                        overallTes.setAccountPercentile(te.getAccountPercentile());
                        overallTes.setAccountPercentileCount( te.getAccountPercentileCount() );
                        overallTes.setScoreCategoryId(te.getOverallRating());



                        if(  !batt || tk.getBatteryScore()==null )
                        {
                            score = te.getOverallScore();
                            scoreCat = te.getOverallRating();
                        }
                        List<TestEventScore> tesl = new ArrayList<>();
                        tesl.add(overallTes);
                        te.setTestEventScoreList(tesl);
                    }
                    else
                        throw new Exception( "TestKey.TestEvent.testEventScoreList is null or empty for productType=" + pt.name() );
                }
                else
                {
                    if( te!=null )
                    {
                        overallTes = te.getOverallTestEventScore();
                    } //

                    if( overallTes!=null &&  ( !batt || tk.getBatteryScore()==null ) )
                    {
                        score = overallTes.getScore();
                        scoreCat = overallTes.getScoreCategoryId();
                    }

                    if( scoreColorSchemeType==null && overallTes!=null && overallTes.getTextParam1()!=null && !overallTes.getTextParam1().isBlank() && overallTes.getTextParam1().contains(Constants.CATEGORYINFOKEY) )
                    {
                        String categInfoStr = StringUtils.getBracketedArtifactFromString( overallTes.getTextParam1(), Constants.CATEGORYINFOKEY);
                        if( categInfoStr!=null && !categInfoStr.isBlank() )
                        {
                            StringTokenizer st = new StringTokenizer( categInfoStr, "~" );
                            if( st.countTokens()>=6 )
                            {
                                scoreColorSchemeType = ScoreColorSchemeType.SEVENCOLOR;
                            }
                            else
                                scoreColorSchemeType = ScoreColorSchemeType.FIVECOLOR;

                        }
                    }

                }

                if( te!=null && (!batt || tk.getBatteryScore()==null) )
                    scoreFormatType = te.getScoreFormatType();
            // }

            scoreCategoryType = ScoreCategoryType.getValue( scoreCat );

            if( scoreColorSchemeType==null )
                scoreColorSchemeType = ScoreColorSchemeType.FIVECOLOR; //   ScoreColorSchemeType.getValue( this.scoreColorSchemeTypeId );

            if( report != null )
            {
                includeOverall = report.getIncludeOverallScore()==1;
                includeCompetencyScores = report.getIncludeCompetencyScores()==1;
                includeNorms = report.getIncludeNorms()==1;
                includeOverview = report.getIncludeOverviewText()==1;
                includeTaskScores = report.getIncludeTaskScores()==1;
                includeNumeric = report.getIncludeCompetencyScores()==1;
                includeCategory = report.getIncludeOverallCategory()==1;
                includeColors = report.getIncludeColorScores()==1;
                includeWritingSamples = report.getIncludeWritingSampleInfo()==1;
                includeIbmInsight = report.getIncludeIbmInsight()==1;
                includeAppData = report.getIncludeApplicantDataInfo()==1;
                includeMinQuals = report.getIncludeMinQualsInfo()==1;
                includeInterview = report.getIncludeInterview()==1;
                includeCompetencyDescriptions = report.getIncludeCompetencyDescriptions()==1;
                includeEducTypeDesc = report.getIncludeEducTypeDescrip()==1;
                includeTrainingTypeDesc = report.getIncludeTrainingTypeDescrip()==1;
                includeExperTypeDesc = report.getIncludeRelatedExperTypeDescrip()==1;
                includeTaskInterest = report.getIncludeTaskInterestInfo()==1;
                includeTaskExper = report.getIncludeTaskExperienceInfo()==1;
                includeRedFlags = report.getIncludeRedFlags()==1;
                includeBiodataScores = report.getIncludeBiodataInfo()==1;

                includeSubcategoryNumeric = report.getIncludeSubcategoryNumeric()==1;
                includeSubcategoryCategory = report.getIncludeSubcategoryCategory()==1;
                includeSubcategoryNorms = report.getIncludeSubcategoryNorms()==1;
                this.includeSubcategoryColors = report.getIncludeCompetencyColorScores()==1;

            }

            params[5] = Integer.toString( Math.round(score) );

            params[6] = ScoreCategoryType.getType(scoreCat).getName( locale );


            boolean includeCompanyInfo = !getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );


            boolean includePreparedFor = includeCompanyInfo && !getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );


            params[7] =includeCompanyInfo ? org.getName() : "";



            String authUName = authU == null ? "" : authU.getFullname();

            if( authUName != null && authUName.indexOf( "AUTOGEN" )>=0 )
                authUName = "";

            reportRules = new ReportRules( org, suborg, te==null ? null : te.getProduct(), report, null );

            // org.getReportFlagList(suborg, report, te==null ? null : te.getProduct() );

            params[8] = includePreparedFor ? authUName: "";

            String ru = RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/";

            // View this result
            params[9] = te == null ? ru + "rs.xhtml" : ru + "r.xhtml?t=" + te.getTestEventIdEncrypted() + "&r=0";

            // View recent results
            params[10] = ru + "rs.xhtml";

            params[11] = suborg == null ? "" : suborg.getName();

            // LogService.logIt( "BaseScoreFormatter.init() found " + reportRules.size() + " report rules." );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseScoreFormatter.init() " + (tk==null ? "" : tk.toString() ) );

            throw new STException(e);
        }
    }


    public String getThirdPartyTestName()
    {
        return getCustomParameterValue( "thirdPartyTestName" );
    }



    public String getCustomParameterValue( String name )
    {
        // LogService.logIt( "ReportData.getCustomParameterValue() " + (tk==null ? "tk is null" : tk.getCustomParameters() ) );

        if( tk == null )
            return null;

        if( tk.getCustomParameters()==null || tk.getCustomParameters().isEmpty() )
            return null;

        JsonObject jo = JsonUtils.getJsonObject( tk.getCustomParameters() );

        return jo.getString( name, null );
    }





    public String getReportRuleAsString( String name )
    {
        if( name == null || name.isEmpty() || reportRules == null  )
            return null;
        return reportRules.getReportRuleAsString(name);
    }

    public int getReportRuleAsInt( String name )
    {
        if( name == null || name.isEmpty() || reportRules == null  )
            return 0;
        return reportRules.getReportRuleAsInt(name);
    }

    public boolean getReportRuleAsBoolean( String name )
    {
       return getReportRuleAsInt( name )==1;
    }



    protected String performCandidateSubstitutions( String inStr )
    {
        String s = StringUtils.replaceStr(inStr, "[COMPANY]" , org.getName() );

        String n = tk.getUser().getFullname();

        if( tk.getUser().getUserType().getUserId() || tk.getUser().getUserType().getUsername() )
            n = tk.getUser().getEmail();

        s = StringUtils.replaceStr(s, "[APPLICANT]" , tk.getUser() != null && tk.getUser().getUserType().getNamedUserIdUsername() ? n : MessageFactory.getStringMessage(locale, "g.Applicant" )  );
        s = StringUtils.replaceStr(s, "[CANDIDATE]" , tk.getUser() != null && tk.getUser().getUserType().getNamedUserIdUsername() ? n : MessageFactory.getStringMessage(locale, "g.Candidate" )  );
        s = StringUtils.replaceStr(s, "[EMPLOYEE]" , tk.getUser() != null && tk.getUser().getUserType().getNamedUserIdUsername() ? n : MessageFactory.getStringMessage(locale, "g.Employee" )  );
        s = StringUtils.replaceStr(s, "[DEPARTMENT]" , tk.getUser()!=null&& tk.getSuborgId()>0 && tk.getSuborg()!= null ? tk.getSuborg().getName() : MessageFactory.getStringMessage(locale, "g.NA" )  );
        s = StringUtils.replaceStr(s, "[EXTREFERENCE]" , tk.getExtRef()==null || tk.getExtRef().isBlank() ? "" : tk.getExtRef()  );
        s = StringUtils.replaceStr(s, "[COMPLETEDATE]" , I18nUtils.getFormattedDate(locale, te==null || te.getLastAccessDate()==null ? tk.getLastAccessDate() : te.getLastAccessDate(), DateFormat.MEDIUM )  );
        s = StringUtils.replaceStr(s, "[TEXT]" , tk.getBatteryProduct()!=null ? tk.getBatteryProduct().getName() : ( te==null ? tk.getTestEventList().get(0).getProduct().getName() : te.getProduct().getName() )  );

        return s;
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



    public String getTextContent() throws Exception
    {
        return null;
    }


    public String getStandardTextContent() throws Exception
    {
        try
        {
            if( tk!=null && tk.getBattery()!=null && tk.getBatteryProduct()!=null )
            {
                Battery b = tk.getBattery();

                String nm = tk.getBatteryProduct().getName();

                if( b!=null && b.getBatteryType().equals( BatteryType.MULTIUSE ) && b.getName()!=null && !b.getName().isEmpty() )
                    nm = b.getName();

                params[0] = nm;
            }


            String out = null;

            boolean showNumeric =  !getReportRuleAsBoolean( "ovroff" ) && !getReportRuleAsBoolean( "ovrnumoff" );

            if( !showNumeric || scoreFormatType.isUnscored() )
            {
                out = lmsg(  anon ? "g.ResultTextUnscoredAnon" : "g.ResultTextUnscored", params);

                // 160 char limit
                if( out.length()>159 )
                    out = lmsg(  anon ? "g.ResultTextShortUnscoredAnon" : "g.ResultTextShortUnscored", params);

                // 160 char limit
                if( out.length()>159 )
                    out = lmsg(  anon ? "g.ResultTextVeryShortUnscoredAnon" : "g.ResultTextVeryShortUnscored", params);
            }

            else
            {


                out = lmsg(  anon ? "g.ResultTextAnon" : "g.ResultText", params);

                // 160 char limit
                if( out.length()>159 )
                    out = lmsg(  anon ? "g.ResultTextShortAnon" : "g.ResultTextShort", params);

                // 160 char limit
                if( out.length()>159 )
                    out = lmsg(  anon ? "g.ResultTextVeryShortAnon" : "g.ResultTextVeryShort", params);
            }

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "HtmlScoreFormatter.getTextContent() " );

            throw new STException( e );
        }
    }


    public List<TestEventScore> getReportTestEventScoreList()
    {
        List<TestEventScore> out = new ArrayList<>();

        if( te != null )
            out.addAll( te.getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() ));

        else
        {
            if( tk == null || tk.getTestEventList() == null )
                return out;

            for( TestEvent tev : tk.getTestEventList() )
            {
                out.addAll( tev.getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() ));
            }
        }

        return out;
    }


    protected String getRowTitle( String style, String title, String datcol1, String datcol2, String datcol3)
    {
        boolean hasD1 = datcol1 != null && !datcol1.isEmpty();
        boolean hasD2 = datcol2 != null && !datcol2.isEmpty();
        boolean hasD3 = datcol3 != null && !datcol3.isEmpty();

        // No data Columns,
        // title - 5
        if( !hasD1 && !hasD2 && !hasD3 )
            return "<tr " + style + "><td colspan=\"7\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td></tr>\n";

        // Missing Col 3
        // title - 2
        // d1 - 1
        // d2 - 1
        // blank 1
        else if( !hasD3 )
        {
            return "<tr " + style + "><td colspan=\"2\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td><td style=\"font-weight:bold;vertical-align:top\" colspan=\"1\">" + (hasD1 ? datcol1 : "") + "</td><td colspan=\"4\" style=\"font-weight:bold;vertical-align:top\">" + (hasD2 ? datcol2 : "") + "</td></tr>\n";
        }

        // All
        // title - 2
        // d1 - 1
        // d2 - 1
        // d3 - 1
        else
        {
            return "<tr " + style + "><td colspan=\"2\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td><td style=\"font-weight:bold;vertical-align:top\">" + (hasD1 ? datcol1 : "") + "</td><td style=\"font-weight:bold;vertical-align:top\">" + (hasD2 ? datcol2 : "") + "</td><td colspan=\"3\" style=\"font-weight:bold;vertical-align:top\">" + (hasD3 ? datcol3 : "") + "</td></tr>\n";
        }
    }

    protected String getRowTitleSubtitle( String style, String title, String subtitle )
    {
        boolean hasSubtitle = subtitle != null && !subtitle.isEmpty();

        if( !hasSubtitle )
            return "<tr " + style + "><td colspan=\"7\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td></tr>\n";

        else
            return "<tr " + style + "><td colspan=\"2\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td>"
                    + "<td style=\"font-weight:bold;vertical-align:top\" colspan=\"5\">" + subtitle + "</td></tr>\n";
    }




    protected String getRow( String style, String value, boolean bold )
    {
        return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + " colspan=\"6\">" + value + "</td></tr>\n";
    }

    protected String getRow( String style, String label, String value, boolean bold )
    {
        return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td colspan=\"5\" " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value + "</td></tr>\n";
    }

    protected String getRow( String style, String label, String value, String value2, boolean bold  )
    {
         return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value + "</td><td colspan=\"4\" " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value2 + "</td></tr>\n";
    }

    /*
    protected String getRow( String style, List<String> caveats  )
    {
         if( caveats ==null || caveats.isEmpty() )
             return "";

        String cavs = "<ul>\n";

        for( String c : caveats )
            cavs += "<li style=\"font-weight:normal;vertical-align:top\">" + c + "</li>\n";

        cavs += "</ul>\n";
        return "<tr " + style + "><td></td><td colspan=\"6\" style=\"font-weight:normal;vertical-align:top\"" + ">" + cavs + "</td></tr>\n";
    }
    */

    protected String getRowForCaveatScoreList( String style, List<CaveatScore> caveatScoreList  )
    {
         if( caveatScoreList ==null || caveatScoreList.isEmpty() )
             return "";

        String cavs = "<table cellpadding=\"2\" style=\"margin-left:0\">\n";

        for( CaveatScore c : caveatScoreList )
        {
            cavs += c.getScoreTableRow() + "\n";
            // cavs += "<li style=\"font-weight:normal;vertical-align:top\">" + c + "</li>\n";
        }

        cavs += "</table>\n";
        return "<tr " + style + "><td></td><td colspan=\"6\" style=\"font-weight:normal;vertical-align:top\"" + ">" + cavs + "</td></tr>\n";
    }

    
    protected String getRowHeaderImage( String imgUrl, int maxWid )
    {
        if( maxWid<=0 )
            maxWid=800;

        return "<tr><td colspan=\"7\" style=\"text-align:center\"><img src=\"" + imgUrl + "\" style=\"display-inline-block;width:100%;max-width:" + maxWid + "px\"/></td></tr>\n";
    }

    protected String getRow( String style, String label, String value, String value2, String value3, boolean bold  )
    {
         return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value2 + "</td><td colspan=\"3\"" + ( bold ? "font-weight:bold" : "" ) +  "\">" + value3 + "</td></tr>\n";
    }

    // Puts the dot in the tird value column
    protected String getRowColorDot( String style, String label, String value, String value2, boolean bold, ScoreCategoryType sct)
    {
         return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"1\">" + value + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value2 + "</td><td colspan=\"3\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl(baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }

    // puts the dot in the second value column
    protected String getRowColorDot( String style, String label, String value, boolean bold, ScoreCategoryType sct)
    {
         return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"2\">" + value + "</td><td colspan=\"3\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }

    // puts the dot in the second value column
    protected String getRowWithColorGraphAndCategoryStars( String style, String label, String value, boolean bold, ScoreCategoryType sct, TestEventScore tes, boolean includeColorGraph, boolean useSolidColor4Graph, boolean includeStars, boolean useRawScore)
    {
        // LogService.logIt( "BaseScoreFormatter.getRowColorDotWithOverallProfile() " );

        if( tes.getTestEventScoreType().getIsOverall() )
        {
            return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:middle\"" : "style=\"font-weight:normal;vertical-align:middle\"" ) + ">" + label + "</td><td style=\"font-weight:bold;font-size:16pt\" colspan=\"1\">" + value + "</td>\n" +
                 "<td colspan=\"1\" style=\"vertical-align:bottom\">" + ( includeColorGraph ? getColorGraph(tes, sct, useRawScore, useSolidColor4Graph ) : "" ) + "</td>\n" +
                 "<td colspan=\"3\">" + ( includeStars ? "<img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/>" : "" ) + "</td></tr>\n";
        }

        else
        {
            boolean hasSpectrum = includeColorGraph && tes!=null && tes.getReportFileContentTypeId()==ScorePresentationType.SPECTRUM.getScorePresentationTypeId();

            if( hasSpectrum )
            {
                String[] spectrum = ReportData.getSpectrumVals(tes);

                return "<tr " + style + "><td style=\"width:20px\"></td>" +
                        "<td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td>" +
                        "<td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"1\">" + value + "</td>" +
                        "<td colspan=\"1\" style=\"padding-right:2px;text-align:right;font-style:italic\">" + spectrum[0] + "</td>" +
                        "<td>" + ( includeColorGraph ? getColorGraph(tes, sct, useRawScore, useSolidColor4Graph ) : "" ) + "</td>" +
                        "<td style=\"padding-left:2px;text-align:left;font-style:italic;width:100%\">" + spectrum[1] + "</td>" +
                        "<td colspan=\"1\">" + ( includeStars ? "<img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/>" : "" ) + "</td></tr>\n";

                /*
                return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold || tes.getTestEventScoreType().getIsOverall() ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"1\">" + value + "</td>" +
                     "<td colspan=\"1\">" +
                        "<table cellpadding=\"2\" style=\"margin-left:0;width:100%\"><tr><td style=\"padding-right:2px;text-align:right;font-style:italic;width:20%\">" + spectrum[0] + "</td><td style=\";width:60%\">" +
                        ( includeColorGraph ? getColorGraph(tes, sct, useRawScore, useSolidColor4Graph ) : "" ) +
                        "</td><td style=\"padding-left:2px;text-align:left;font-style:italic;width:20%\">" + spectrum[1] + "</td></tr></table>"  +
                     "</td>" +
                     "<td colspan=\"1\">" + ( includeStars ? "<img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/>" : "" ) + "</td></tr>\n";
                */
            }

            else
                return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold || tes.getTestEventScoreType().getIsOverall() ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"1\">" + value + "</td>" +
                     "<td colspan=\"1\">" + ( includeColorGraph ? getColorGraph(tes, sct, useRawScore, useSolidColor4Graph ) : "" ) + "</td>" +
                     "<td colspan=\"3\">" + ( includeStars ? "<img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/>" : "" ) + "</td></tr>\n";
        }
    }




    protected String getChatResponsesRow( String style, String label, String text )
    {
        StringBuilder sb = new StringBuilder();

        if( getReportRuleAsBoolean( "hidechatresponseinfo" ) )
            return sb.toString();

        if( text==null || text.trim().isEmpty() )
            return sb.toString();

        String[] pairs = text.split( "\\|" );

        if( pairs.length < 2 )
            return sb.toString();

        String typeStr;
        String content;
        int typeId;
        boolean isUser;

        for( int idx=0; idx<pairs.length-1; idx+=2 )
        {
            typeStr = pairs[idx].trim();
            content = pairs[idx+1].trim();

            if( typeStr.isEmpty() || content.isEmpty() )
                continue;

            typeId = Integer.parseInt( typeStr );

            isUser = typeId==ChatMessageType.USER_MSG.getChatMessageTypeId();

            if( isUser )
            {
                sb.append( "<tr><td style=\"width:30%\"></td><td colspan=\"6\"><div style=\"margin:4px;padding:3px;background-color:#0077cc;color:white;border-radius:6px;\">" + content + "</div></td></tr>\n" );
            }

            else
            {
                sb.append( "<tr><td colspan=\"2\"><div style=\"margin:4px;margin-left:20px;padding:3px;background-color:gray;color:white;border-radius:6px;\">" + content + "</div></td><td colspan=\"4\" style=\"width:30%\"></td></tr>\n" );
            }
        }

        if( sb.length()<=0 )
            return sb.toString();

        return "<tr " + style + "><td style=\"width:20px\"></td><td style=\"font-weight:bold;vertical-align:top\">" + label + "</td>\n" +
                "<td colspan=\"5\">\n" +
                "  <table cellpadding=\"0\" cellspacing=\"0\" style=\"margin-top:8px;margin-bottom:6px;margin-left:40px;margin-right:auto;margin-top:4px;width:70%;background-color:lightgray;border-radius:8px;padding:4px\">\n" +
                       sb.toString() +
                "  </table>\n" +
                "</td></tr>\n";
    }


    protected String getPdfDownloadImgLink( String reportUrl )
    {
        // String imgUrl = RuntimeConstants.getStringValue("baseprotocol") +  "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/resources/images/pdf_download2.png";
        String imgUrl = RuntimeConstants.getStringValue("baseprotocol") +  "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/jakarta.faces.resource/pdf_download2.png.xhtml?ln=images";

        return "<a href=\"" + reportUrl + "\" title=\"" + lmsg( "g.ClickToDownloadReport" ) + "\"><img src=\"" + imgUrl + "\" alt=\"" + lmsg( "g.ClickToDownloadReport" ) + "\" style=\"width:24px\"/></a>";
    }


    protected String getColorGraph( TestEventScore tes, ScoreCategoryType sct, boolean useRawScore, boolean useSolidColor )
    {
        if( useSolidColor )
            return getSolidColorGraph( tes, sct );

        StringBuilder sb = new StringBuilder();

        try
        {
            StringBuilder imgUrl = new StringBuilder();

            FormatCompetency fc = tes.getFormatCompetency(true);

            int ptrPos = fc.getPointerLeft(useRawScore);

            String pflPrms = "";

            String custClrs = ""; // rFFFFFF,ryE1F0Fd,yBFE0FE,yg94CCFE,g62B4FE";

            List<ScoreCategoryRange> scrl = null;

            if( tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) )
            {
                scrl = fc.getScoreCategoryRangeList();

                if( scrl == null || scrl.isEmpty() )
                    scrl = ScoreFormatUtils.getOverallScoreCatInfoList(true, scoreColorSchemeType );
            }

            else
                scrl = fc.getScoreCategoryRangeList();

            // List<ScoreCategoryRange> scrl = tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) ? ScoreFormatUtils.getOverallScoreCatInfoList(true) : fc.getScoreCategoryRangeList();
            //if( !tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL) )
            //{
                for( ScoreCategoryRange scr : scrl )
                {
                    if( imgUrl.length()>0 )
                        imgUrl.append( "," );

                    imgUrl.append( getRangeColor( scr.getRangeColor() ) + scr.getAdjustedRangePix(tes.getScoreFormatType()) );
                }

            //}

            //else
            //    pflPrms += "&tw=" + ( Constants.CT2_COLORGRAPHWID + 6 );

            if( tes.getProfileBoundaries() != null )
            {
                ScoreCategoryRange scr = new ScoreCategoryRange( sct.getScoreCategoryTypeId(), tes.getProfileBoundaries()[0], tes.getProfileBoundaries()[1], Constants.CT2_COLORGRAPHWID_EML );

                // pflPrms += "&prl=" + tes.getProfileBoundaries()[0] + "&prh=" + tes.getProfileBoundaries()[1];
                pflPrms += "&prl=" + scr.getMinPix(tes.getScoreFormatType()) + "&prh=" + (scr.getMinPix(tes.getScoreFormatType()) + scr.getAdjustedRangePix(tes.getScoreFormatType()));
            }

            if( tes.getScoreFormatTypeId()!=ScoreFormatType.NUMERIC_0_TO_100.getScoreFormatTypeId() )
                pflPrms += "&sft=" + tes.getScoreFormatTypeId();

            if( te.hasProfile() && te.getProfile()!=null && te.getProfile().getStrParam3()!=null && !te.getProfile().getStrParam3().isEmpty() )
                custClrs = "&cs=" + te.getProfile().getStrParam3().trim();

            sb.append("<img style=\"width:" + (Constants.CT2_COLORGRAPHWID_EML + 8) + "px;height:20px\" alt=\"" + MessageFactory.getStringMessage(locale, "g.CT2GraphicAlt" ) + "\" src=\"" + RuntimeConstants.getStringValue("baseprotocol") +  "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/" + (getTestEvent().getUseBellGraphs() ? "bellscorechart" : "ct2scorechart") + "/" + tes.getTestEventScoreIdEncrypted()  + ".png?ss=" + imgUrl.toString() + "&tw=" + Constants.CT2_COLORGRAPHWID_EML + "&p=" + ptrPos + pflPrms + custClrs + "\"/>" );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "CT2HtmlScoreFormatter.getColorGraph() " + tes.toString() );
        }

        // LogService.logIt( "CT2HtmlScoreFormatter.getColorGraph() size=" + sb.length() ); // + "\n" + sb.toString() );

        return sb.toString();
        // return "<tr " + style + "><td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"2\">" + value + "</td>" + "<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }



    protected String getMetaScoreColorGraph( String name, float score )
    {
        StringBuilder sb = new StringBuilder();

        try
        {
            StringBuilder imgUrl = new StringBuilder();

            // FormatCompetency fc = tes.getFormatCompetency(true);

            int totalPix = Constants.CT2_COLORGRAPHWID_EML;

            int ptrPos = 0; // fc.getPointerLeft(useRawScore);

            if( score<=0 )
                ptrPos = 0 - Math.round(FormatCompetency.MARKER_LEFT_ADJ);

            else if( score>=100 )
                ptrPos = totalPix - Math.round(FormatCompetency.MARKER_LEFT_ADJ);

            else
            {
                int specAdj = score<=0 ? 1 : 0;
                ptrPos = Math.round(((float)totalPix)*(score/100f)) - Math.round(FormatCompetency.MARKER_LEFT_ADJ) + specAdj;
            }


            // String pflPrms = "";

            String custClrs = Constants.IBMINSIGHT_SCORE_GRAPH_COLORS;  // "rFFFFFF,ryE1F0Fd,yBFE0FE,yg94CCFE,g62B4FE";

            String[] colors = this.scoreColorSchemeType!=null && scoreColorSchemeType.getIsSevenColor() ? Constants.SCORE_GRAPH_COLS_SEVEN : Constants.SCORE_GRAPH_COLS; // new String[] {"r","ry","y","yg", "g" };

            float widPer = (1f/(float)colors.length);

            for( int i=0;i<colors.length;i++ )
            {
                if( imgUrl.length()>0 )
                    imgUrl.append( "," );

                imgUrl.append( colors[i] + Math.round( widPer*totalPix ) );
            }

            sb.append("<img style=\"width:" + (Constants.CT2_COLORGRAPHWID_EML + 8) + "px;height:20px\" alt=\"" + MessageFactory.getStringMessage(locale, "g.CT2GraphicAlt" ) + "\" src=\"" + RuntimeConstants.getStringValue("baseprotocol") +  "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/ct2scorechart/" + StringUtils.alphaCharsOnly(name) + ".png?ss=" + imgUrl.toString() + "&tw=" + Constants.CT2_COLORGRAPHWID_EML + "&p=" + ptrPos + "&cs=" + custClrs + "\"/>" );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "CT2HtmlScoreFormatter.getMetaScoreColorGraph() name=" + name + ", score=" + score );
        }

        // LogService.logIt( "CT2HtmlScoreFormatter.getColorGraph() size=" + sb.length() ); // + "\n" + sb.toString() );

        return sb.toString();
        // return "<tr " + style + "><td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"2\">" + value + "</td>" + "<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }


    protected String getSolidColorGraph( TestEventScore tes, ScoreCategoryType sct )
    {

        // LogService.logIt( "CT2HtmlScoreFormatter.getSolidColorGraph() " + tes.getName() ); // + "\n" + sb.toString() );

        try
        {
            // <div style="background-color:#{cc.attrs.testEvent.overallTestEventScore.scoreCategoryType.colorRgb};width:100px;height:20px;margin-left:auto;margin-right:auto">&#160;</div>
            String colorRgb = sct.getRgbColor();

            if( this.hasProfile() && getTestEvent().getProfile().getStrParam3()!=null && !getTestEvent().getProfile().getStrParam3().isEmpty() )
            {
                String[] cols = ProfileUtils.parseBaseColorStr( getTestEvent().getProfile().getStrParam3() );

                if( cols!=null && cols.length>0 )
                    colorRgb = sct.getRgbColor( cols );
            }

            return "<div style=\"background-color:" + colorRgb + ";width:100px;height:20px;margin-left:0;margin-right:auto\">&#160;</div>\n";
        }

        catch( Exception e )
        {
            LogService.logIt(e, "CT2HtmlScoreFormatter.getSolidColorGraph() " + tes.toString() );
            return "";
        }

        // return "<tr " + style + "><td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"2\">" + value + "</td>" + "<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }




    /**
     * USed to get color code for image generator.
     * @param color
     * @return
     */
    private String getRangeColor( String color )
    {
        color = color.toLowerCase();
        if( color.equals("#ff0000") )
            return "r";
        else if( color.equals("#f17523") )
            return "ry";
        else if( color.equals("#fcee21") )
            return "y";
        else if( color.equals("#8cc63f") )
            return "yg";
        else if( color.equals("#69a220") )
            return "g";
        else if( color.equals("#c1c1c1") )
            return "b";
        else if( color.equals("#efefef") )
            return "w";

        // Yellow
        return "y";
    }


    /*
    protected String getRowColor( String style, String label, String value, String value2, boolean bold, String bgColor )
    {
         return "<tr " + style + "><td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value + "</td><td style=\"width:40px;" + ( bold ? "font-weight:bold;" : "" ) + "background-color:" + bgColor + "\"><div style=\"text-align:center\">" + value2 + "</div></td></tr>\n";
    }
    */

    protected String getRowSpacer( String style )
    {
        return "<tr " + style + "><td colspan=\"7\">&#160;</td></tr>\n";
    }

    public String lmsg( String key )
    {
        return lmsg( key, null );
    }

    public String lmsg( String key,String[] prms )
    {
        return MessageFactory.getStringMessage( locale , key, prms );
    }


    public String getNormString( float pct, int cnt, int precision )
    {
        if( pct>=0 && pct<1f )
            pct = 1f;
        if( pct>99f )
            pct=99f;

        String v =  NumberUtils.getPctSuffixStr( locale, pct, precision ); // I18nUtils.getFormattedNumber(locale, pct, 0);

        if( pct>0 || ( cnt >=  MIN_COUNT_FOR_PERCENTILE || cnt==0 ) )
            v = lmsg( "g.PercentileXWCount", new String[] {v, Integer.toString(cnt)} );
        else
            v = lmsg( "g.PercentileLowData" ); // lmsg( "g.PercentileXEstLowData", new String[] {v} );

        return v;
    }


    public String getSubcategoryNormString( float pct, int cnt, int precision )
    {
        if( pct < 0 )
            return "";

        if( pct<1f )
            pct = 1f;
        if( pct>99f )
            pct=99f;


        String v = NumberUtils.getPctSuffixStr( locale, pct, precision );

        // int cnt = getTestEvent().getOverallPercentileCount();

        if( cnt >= MIN_COUNT_FOR_PERCENTILE )
            v = lmsg( "g.Percentile2XNoCountBare", new String[] {v, Integer.toString(cnt)} );

            // v = lmsg( "g.Percentile2XWCount", new String[] {v, Integer.toString(cnt)} );
        else
            v = lmsg( "g.PercentileUnavailable" );

        return v;
    }


    public String getCustomCandidateMsgText()
    {
            // String m = org.getDefaultMessageText();

        String m = null;
        if( suborg!=null )
            m = suborg.getTestTakerCompleteEmail();

        if( m==null || m.isBlank() )
            m = org.getTestTakerCompleteEmail();

        if( m != null && !m.isBlank() )
            return performCandidateSubstitutions( m );

        return null;
    }

    public String getCustomCandidateEmailSubject()
    {
            // String m = org.getDefaultMessageText();

        String m = null;
        if( suborg!=null )
            m = suborg.getTestTakerCompleteEmailSubj();

        if( m==null || m.isBlank() )
            m = org.getTestTakerCompleteEmailSubj();

        if( m != null && !m.isBlank() )
            return performCandidateSubstitutions( m );

        return null;
    }


    public Object[] getStandardHeaderSection( boolean tog, boolean includeTop, boolean testTakerOnly, String topNoteHtml, String introLangKey, String customMsg)
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        String battName = null;
        if( !testTakerOnly && isBattery() )
        {
            Battery b = tk.getBattery();

            battName = tk.getBatteryProduct()!=null ? tk.getBatteryProduct().getName() : null;

            if( b!=null && b.getBatteryType().equals( BatteryType.MULTIUSE ) && b.getName()!=null && !b.getName().isEmpty() )
                battName = b.getName();
        }

        boolean includeCompanyInfo = !getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

        // String style, s0, s1, s2;

        String label;
        String value;

        if( includeTop )
        {
            if( topNoteHtml != null && !topNoteHtml.isBlank() )
                sb.append("<tr " + rowStyle0 + "><td colspan=\"7\" style=\"border-bottom:0px solid black;padding-bottom:8px\">" + topNoteHtml + "</td></tr>\n" );

            String intro = customMsg;

            if( (intro == null || intro.isBlank()) && introLangKey != null && !introLangKey.isBlank() )
            {
                if( !testTakerOnly &&  isBattery() && battName!=null )
                {
                    String tn = params[0];
                    params[0]=battName;
                    intro = lmsg(  introLangKey , params );
                    params[0]=tn;
                }
                else
                    intro = lmsg(  introLangKey , params );
            }

            if( intro != null && !intro.isBlank() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"7\" style=\"border-bottom:0px solid black;padding:10px\">" + intro + "</td></tr>\n" );
        }

        tog = true;
        String style; //  = tog ? rowStyle1 : rowStyle2;

        if( includeTop )
        {
            // title Row
            label = lmsg(  "g.TestEventData" , null );
            sb.append( getRowTitle( rowStyleHdr, label, null, null, null ) );

            String nameKey = "g.NameC";

            if( getUser().getUserType().getUserId() )
                nameKey = "g.UserIdC";

            else if( getUser().getUserType().getUsername() )
                nameKey = "g.UsernameC";

            // this is the TestTaker name, or anonymous.
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            value = params[3];
            label = lmsg(  nameKey , null );
            if( value != null && value.length() > 0 )
                sb.append( getRow( style, label, value, false ) );

            if( !isAnonymous() )
            {
                if( getUser().getUserType().getNamed() && !StringUtils.isCurlyBracketed( u.getEmail() ) )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getUser().getEmail();
                    label = lmsg(  "g.EmailC" , null );
                    if( value != null && value.length() > 0 )
                        sb.append( getRow( style, label, value, false ) );
                }

                if( getUser().getHasAltIdentifierInfo() )
                {
                    String ainame = getUser().getAltIdentifierName();

                    if( ainame == null || ainame.isEmpty() )
                        ainame = lmsg(  "g.DefaultAltIdentifierName" );

                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getUser().getAltIdentifier();
                    label = ainame + ":";
                    if( value != null && value.length() > 0 )
                        sb.append( getRow( style, label, value, false ) );
                }

                if( getOrg().getCustomFieldName1()!=null && !getOrg().getCustomFieldName1().isEmpty()  )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getTestKey().getCustom1()==null ? "" : getTestKey().getCustom1();
                    label = getOrg().getCustomFieldName1() + ":";
                    if( value != null && value.length() > 0 )
                        sb.append( getRow( style, label, value, false ) );
                }
                if( getOrg().getCustomFieldName2()!=null && !getOrg().getCustomFieldName2().isEmpty() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getTestKey().getCustom2()==null ? "" : getTestKey().getCustom2();
                    label = getOrg().getCustomFieldName2() + ":";
                    if( value != null && value.length() > 0 )
                        sb.append( getRow( style, label, value, false ) );
                }
                if( getOrg().getCustomFieldName3()!=null && !getOrg().getCustomFieldName3().isEmpty() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getTestKey().getCustom3()==null ? "" : getTestKey().getCustom3();
                    label = getOrg().getCustomFieldName3() + ":";
                    if( value != null && value.length() > 0 )
                        sb.append( getRow( style, label, value, false ) );
                }
            }

            if( !testTakerOnly && getUser().getHasIpCountry() )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value = getUser().getIpCountryName();
                label = lmsg("g.IpCountry") + ":";
                if( value != null && value.length() > 0 )
                    sb.append( getRow( style, label, value, false ) );
            }
            if(  !testTakerOnly && getUser().getHasIpState() )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value = getUser().getIpState();
                label = lmsg("g.IpState") + ":";
                if( value != null && value.length() > 0 )
                    sb.append( getRow( style, label, value, false ) );
            }
            if(  !testTakerOnly && getUser().getHasIpCity() )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value = getUser().getIpCity();
                label = lmsg("g.IpCity") + ":";
                if( value != null && value.length() > 0 )
                    sb.append( getRow( style, label, value, false ) );
            }


            // Sim Name
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            // value = params[0];
            if( !testTakerOnly && isBattery() )
            {
                Battery b = tk.getBattery();
                String nm = tk.getBatteryProduct().getName();

                if( b!=null && b.getBatteryType().equals( BatteryType.MULTIUSE ) && b.getName()!=null && !b.getName().isEmpty() )
                    nm = b.getName();

                value = nm;

                label = lmsg(  "g.BatteryC" , null );
            }
            else
            {
                label = lmsg(  "g.TestC" , null );
                value = params[0];
            }

            if( value != null && value.length() > 0 )
                sb.append( getRow( style, label, value, false ) );


            if( getTestKey()!=null && getTestKey().getAssistiveTechnologyTypeIds()!=null && !getTestKey().getAssistiveTechnologyTypeIds().isBlank() )
            {
                StringBuilder sbx = new StringBuilder();
                for( Integer sid : getTestKey().getAssistiveTechnologyTypeIdList() )
                {
                    if( sid<=0 )
                        continue;

                    if( sbx.length()>0 )
                        sbx.append( "<br />" );
                    sbx.append( lmsg(AssistiveTechnologyType.getValue( sid ).getKey()) );
                }

                if( tk.getAssistiveTechnologyTypeOtherValue()!=null && !tk.getAssistiveTechnologyTypeOtherValue().isBlank() )
                    sbx.append( "<br />" + tk.getAssistiveTechnologyTypeOtherValue() );

                if( sbx.length()>0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value =sbx.toString();
                    label = lmsg(  "g.AssistiveTech" , null ) + ":";
                    if( value != null && value.length() > 0 )
                         sb.append( getRow( style, label, value, false ) );
                }
            }


            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            value =I18nUtils.getFormattedDateTime(locale, getTestEvent().getStartDate(), getTestKey().getUser().getTimeZone() );
            label = lmsg(  "g.StartedC" , null );
            if( value != null && value.length() > 0 )
                 sb.append( getRow( style, label, value, false ) );

            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            value =   I18nUtils.getFormattedDateTime(locale, getTestEvent().getLastAccessDate(), getTestKey().getUser().getTimeZone() );
            label = lmsg(  "g.CompletedC" , null );
            if( value != null && value.length() > 0 )
                sb.append( getRow( style, label, value, false ) );

            boolean compNameForAdmin = getReportRuleAsBoolean("compnameforprep") && includeCompanyInfo;


            // include only if there is an auth user name.
            if( includeCompanyInfo && !getUser().getRoleType().getIsPersonalUser() && params[8] != null && !params[8].isEmpty() )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value = compNameForAdmin ? getOrg().getName() : params[8];
                label = lmsg(  compNameForAdmin ? "g.PreparedForC" : "g.AuthorizedByC" , null );
                if( value != null && value.length() > 0 )
                    sb.append( getRow( style, label, value, false ) );

                if( !compNameForAdmin || (org.getReportLogoUrl() != null && !org.getReportLogoUrl().isBlank()) || (suborg!=null && suborg.getReportLogoUrl() != null && !suborg.getReportLogoUrl().isBlank()) )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getOrg().getName();

                    String logoUrl = org.getReportLogoUrl();

                    if( suborg!=null && suborg.getReportLogoUrl() != null && !suborg.getReportLogoUrl().isBlank() )
                        logoUrl = suborg.getReportLogoUrl();

                    if( logoUrl!=null && !logoUrl.isBlank() )
                        value = "<img src=\"" + logoUrl + "\" alt=\"" +  StringUtils.replaceStandardEntities( org.getName() ) + "\" style=\"max-width:150px\"/>";

                    label = lmsg(  "g.OrganizationC" , null );
                    if( value != null && value.length() > 0 )
                            sb.append( getRow( style, label, value, false ) );
                }
            }

            // LogService.logIt( "BaseScoreFormatter.getStandardHeaderSection() AAA isBattery()=" + isBattery() + ", bs=" + (getBatteryScore()!=null) + ", isIncludeOverall()=" + isIncludeOverall() );

            if( !testTakerOnly &&  isBattery() && getBatteryScore()!=null  )
            {
                if( isIncludeOverall() )
                {
                    if( isIncludeNumeric() && BatteryScoreType.getValue(getBatteryScore().getBatteryScoreTypeId()).needsScore() )
                    {
                        // need to calc score digits
                        int scrDigits = ScoreFormatType.NUMERIC_0_TO_100.getScorePrecisionDigits();

                        for( TestEvent tev : tk.getTestEventList() )
                        {
                            if( tev!=null )
                            {
                                scrDigits = tev.getScoreFormatType().getScorePrecisionDigits();
                                break;
                            }
                        }


                        tog = !tog;
                        style = tog ? rowStyle1 : rowStyle2;
                        value = I18nUtils.getFormattedNumber(locale, getBatteryScore().getScore(), scrDigits);
                        label = lmsg(  "g.OverallBatteryScoreC" , null );
                        if( value != null && value.length() > 0 )
                        {
                            if( isIncludeCategory() && getScoreCategoryType().hasColor() )
                                sb.append( getRowColorDot( style, label, value, "", true, getScoreCategoryType() ) );

                                // sb.append( getRowColor( style, label, value, value2, false, scoreCategoryType.getRgbColor() ) );

                            else
                                sb.append( getRow( style, label, value, true ) );
                        }
                    }

                    else if( isIncludeCategory() && getScoreCategoryType().hasColor() && BatteryScoreType.getValue(getBatteryScore().getBatteryScoreTypeId()).needsScore() )
                    {
                        tog = !tog;
                        style = tog ? rowStyle1 : rowStyle2;
                        value =  "";
                        label = lmsg(  "g.OverallScoreC" , null );
                        sb.append( getRowColorDot( style, label, value, "", false, getScoreCategoryType() ) );
                    }


                }

                int rptCt = 0;
                for( TestEvent tev : tk.getTestEventList() )
                {
                    rptCt += tev.getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() ).size();
                }

                // LogService.logIt( "BaseScoreFormatter.getStandardHeaderSection() rptCt=" + rptCt );

                if( rptCt>1 )
                {
                    String url = RuntimeConstants.getStringValue( "adminappbasuri" ) + "/battpdfzipdwnld?bsid=" + getBatteryScore().getBatteryScoreIdEncrypted() + "&oid=" + this.getOrg().getOrgIdEncrypted();
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    // String[] spms = new String[]{url};
                    label = "";
                    value = "<a href=\"" + url + "\" title=\"" +  lmsg(  "g.BatZipPdfDwnldInfo" , null ) + "\">" + lmsg(  "g.BatZipPdfDwnld" , null ) + "</a>\n";
                    sb.append( getRow( style, label, value, false ) );
                }

            } // Battery with BatteryScore.

        } // if includeTop


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }




    public Object[] getStandardOverallScoreSection( boolean tog )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        tog = false;

        if( this.isIncludeOverall() )
        {
            TestEventScore otes = getTestEvent().getOverallTestEventScore();

            boolean showColorRating = isIncludeCategory() && getTestEvent().getScoreCategoryType().hasColor();

            String earlyExitStr = StringUtils.getBracketedArtifactFromString( getTestEvent().getTextStr1(), Constants.EARLYEXITBATTERYKEY );
            if( earlyExitStr!=null )
                earlyExitStr = BatteryScoringUtils.getEarlyExitWarningMessage( getLocale(), earlyExitStr );

            if( earlyExitStr!=null )
                sb.append( getRow( rowStyleHdrRedTxt, lmsg(  "g.NoteC" , null ), earlyExitStr, false ) );

            // title Row
            if( isIncludeNumeric() && showColorRating )
                sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), lmsg(  this.useRatingAndColors() ? "g.Rating" : "g.JobMatch" , null ), null ) );

            else if( isIncludeNumeric()  )
                sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), null, null ) );

            else if( showColorRating )
                sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), "", lmsg(  this.useRatingAndColors() ? "g.Rating" : "g.JobMatch" , null ), null ) );

            String style = tog ? rowStyle1 : rowStyle2;
            String value =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() ) : "";

            // value2 = getTestEvent().getScoreCategoryType().getName(locale);
            String label = lmsg(  "g.ScoreC" , null );


            if( isIncludeNumeric() && showColorRating )
                sb.append( getRowColorDot( style, label, value, false, getScoreCategoryType() ) );

            else if( isIncludeNumeric()  )
                sb.append( getRow( style, label, value, false ) );

            else if( showColorRating )
                sb.append( getRowColorDot( style, label, "", false, getScoreCategoryType() ) );

            if( isIncludeNorms() && ( otes.getHasValidNorms() || otes.getHasValidOverallZScoreNorm() ) )
            {
                if( otes.getHasValidOverallNorm() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  getNormString( otes.getPercentile(), otes.getOverallPercentileCount(), 0 );
                    label = lmsg(  "g.OverallPercentileC" , null );
                    sb.append( getRow( style, label, value, false ) );
                }
                else if( otes.getHasValidOverallZScoreNorm() && te.getProduct()!=null && te.getProduct().getConsumerProductType().getIsJobSpecific() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  getNormString( otes.getOverallZScorePercentile(), 0, 0 );
                    label = lmsg(  "g.OverallPercentileApproxC" , null );
                    sb.append( getRow( style, label, value, false ) );
                }

                if( otes.getHasValidCountryNorm() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  getNormString( otes.getCountryPercentile(), otes.getCountryPercentileCount(), 0 );
                    label = lmsg(  "g.XPercentileC" , new String[] {this.getCountryName( otes.getPercentileCountry()!=null && !otes.getPercentileCountry().isEmpty() ? otes.getPercentileCountry() : getTestEvent().getIpCountry())} );
                    sb.append( getRow( style, label, value, false ) );
                }

                if( otes.getHasValidAccountNorm() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getNormString( otes.getAccountPercentile(), otes.getAccountPercentileCount(), 0 );
                    label = lmsg(  "g.XPercentileC" , new String[] {getOrg().getName()} );
                    sb.append( getRow( style, label, value, false ) );
                }
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    public Object[] getStandardTopInterviewQsSection(boolean tog, String rowStyleHeader )
    {
        Object[] out = new Object[2];

        out[0] = "";
        out[1] = tog;

        if( getReportRuleAsBoolean( "topinterviewqsoff") )
            return out;

        int maxPerComp = 1;

        if( report!=null )
        {
            maxPerComp = report.getMaxInterviewQuestionsPerCompetency();
            if( maxPerComp<=0 )
                return out;
        }

        StringBuilder sb = new StringBuilder();

        List<TestEventScore> tl = getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );
        if( tl.isEmpty() )
            return out;

        List<TestEventScore> tl2 = new ArrayList<>();
        tl2.addAll(tl);
        Collections.sort(tl2, new TESScoreComparator(false));

        ListIterator<TestEventScore> iter =tl2.listIterator();
        TestEventScore tesx;
        List<InterviewQuestion> iql = null;

        int count = 0;
        while( iter.hasNext() )
        {
            tesx = iter.next();

            // Skip competencies that are not to be shown in reports.
            if( !SimCompetencyVisibilityType.getValue( tesx.getHide() ).getShowInReports() )
                continue;

            // already have enough.
            if( count>=6 )
            {
                iter.remove();
                continue;
            }

            iql = tesx.getInterviewQuestionList(maxPerComp);

            // no questions.
            if( iql==null || iql.isEmpty() )
            {
                iter.remove();
                continue;
            }

            // add to count;
            count+=iql.size();
        }

        // No interview questions to show.
        if( tl2.isEmpty() || count<=0 )
            return out;

        sb.append( getRowTitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, lmsg( "g.PriIntvwQs", null ), null, null, null ) );

        tog=true;
        String style;

        int ct;
        for( TestEventScore tes : tl2 )
        {
            tog=!tog;
            style = tog ? rowStyle1 : rowStyle2;
            iql = tes.getInterviewQuestionList(maxPerComp);

            ct = 0;
            for( InterviewQuestion iq : iql )
            {
                sb.append( getRow( style,  ct<=0 ? tes.getName() : "", iq.getQuestion(), false ) );
                ct++;
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }




    public Object[] getStandardReportSection( boolean tog, boolean testTakerOnly, String rowStyleHeader)
    {
        Object[] out = new Object[2];

        out[0] = "";
        out[1] = tog;

        if( getReportRuleAsBoolean( "rptdwnldoff") || getReportRuleAsBoolean( "emlrptdwnldoff") )
            return out;

        StringBuilder sb = new StringBuilder();

        // Reports
        List<TestEventScore> tl = getTestEvent().getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() );

        List<TestEventScore> tesList = new ArrayList<>();
        // int rptCt = 0;

        for( TestEventScore tes : tl )
        {
            if( tes.getHasReport() )
            {
                // employers get all reports
                if( !testTakerOnly ) // || tes.getReport().getEmailTestTaker()==1 )
                    tesList.add(tes);

                // test taker - only email the report for this specific Report
                else
                {
                    if( tes.getReport().getEmailTestTaker()==1 && tes.getReportId()==report.getReportId() && tes.getReportFilename()!=null && !tes.getReportFilename().equalsIgnoreCase("NoReport") && tes.getHasReport() )
                        tesList.add(tes);
                }
            }
        }

        if( !tesList.isEmpty() )
        {
            sb.append( getRowTitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, lmsg(  tesList.size()> 1 ? "g.ReportLinks" : "g.ReportLink", null ), null, null, null ) );

            tog=true;
            String style = tog ? rowStyle1 : rowStyle2;
            String link;

            for( TestEventScore tes : tesList )
            {
                if( !tes.getHasReport() )
                    continue;

                tog=!tog;
                style = tog ? rowStyle1 : rowStyle2;
                link = tes.getReportDirectDownloadLink();
                
                if( link==null || link.isBlank() )
                    continue;

                String pdfImgLnk = getPdfDownloadImgLink( link );

                String txtLink = "<a href=\"" + link + "\" title=\"" + lmsg(  "g.ReportLink") + "\">" + lmsg( "g.ClickToDownloadReport" ) + "</a>";

                sb.append( getRow( style,  tes.getName(), pdfImgLnk, txtLink, false ) );
            }

           // sb.append( getRowSpacer( rowStyle0 ) );
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }



    public Object[] getStandardAimsSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 3 );
    }

    public Object[] getStandardEqSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 6 );
    }

    public Object[] getStandardAiCompetenciesSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 10 );
    }



    public Object[] getStandardKsaSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 0 );
    }


    public Object[] getStandardTaskSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 1 );
    }

    public Object[] getBiodataCompetencyTaskSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 2 );
    }

    public Object[] getStandardAbilitiesSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 4 );
    }

    public Object[] getStandardKsSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 5 );
    }

    public Object[] getStandardInterestsSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 11 );
    }

    public Object[] getStandardCustomSection( boolean tog, int customIndex )
    {
        return getCompetencyTaskSection( tog, 20 + customIndex );
    }




    /**
     * typeId
     *      0=KSA
     *      1=Task
     *      2=biodata
     *      3=AIMS
     *      4=Abilities
     *      5=KS
     *      6=EQ
            7=Voice Skills
     *      8=Scored Audio (Voice Sample)
     *      9=Scored AV (Uploaded AV)
     *      10=AI-Derived
     *      11=Interests
     *      21=Custom 1
     *      22=Custom 2
     *      23=Custom 3
     *      24=Custom 4
     *      25=Custom 5
     *
     * SimCompetencyGroup Values for reference only
     *
NONE(0,"None"),    // When an ONET Soc is selected
    ABILITY(1,"Ability"),
    PERSONALITY(2,"Personality"),
    BIODATA(3,"Biodata"),
    SKILLS(4,"Skills"),
    EQ( 5, "Emotional Intelligence"),
    AI( 6, "AI-Derived"),
    INTERESTS( 7, "Intersts"),
    CUSTOM( 100, "Custom" );     *
     *
     * @param tog
     * @param typeId
     * @return
     */
    protected Object[] getCompetencyTaskSection( boolean tog, int typeId )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeIt = false;

        tog = true;

        if( typeId == 0 || typeId==3 || typeId==4 || typeId==5 || typeId==6 || typeId==10 || typeId==11 || (typeId>=21 && typeId<=25) )
            includeIt = isIncludeCompetencyScores();

        else if( typeId == 1 )
            includeIt = isIncludeTaskScores();

        else if( typeId == 2 )
            includeIt = isIncludeBiodataScores();

        if( includeIt )
        {
            SimCompetencyClass scc;

            List<TestEventScore> tesList = getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            List<TestEventScore> tesList2 = new ArrayList<>();

            // List<String> caveats;
            boolean usesPercentCorrectScoring = false;

            for( TestEventScore tes : tesList )
            {
                LogService.logIt( "BaseScoreFormatter.getCompetencyTaskSection() AAA.1 START tes.name=" + tes.getName() + ", typeId=" + typeId + ", score=" + tes.getScore() + ", visibility=" + SimCompetencyVisibilityType.getValue( tes.getHide() ).getName() );

                // Skip competencies or task-competencies that were not automatically scored.
                if( tes.getScore()<0 )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                if( hasProfile() )
                    tes.setProfileBoundaries(getProfileEntryData(tes.getName(), tes.getNameEnglish() ) );

                scc = SimCompetencyClass.getValue(tes.getSimCompetencyClassId());

                // if( typeId == 0 && scc.getIsDirectCompetency() || scc.getIsAggregate() )
                if( typeId == 0 && scc.isKSA() )
                    tesList2.add( tes );

                else if( typeId == 1 && scc.getIsTask() )
                    tesList2.add( tes );

                else if( typeId == 2 && scc.getIsBiodata())
                    tesList2.add( tes );

                else if( typeId == 3 && scc.isAIMS() )
                    tesList2.add( tes );

                else if( typeId == 4 && scc.isAbility() )
                {
                    tesList2.add( tes );
                }

                else if( typeId==5 && scc.isKS() )
                {
                    tesList2.add( tes );
                }

                else if( typeId == 6 && scc.isEQ() )
                    tesList2.add( tes );

                else if( typeId == 10 && scc.isAIDerived())
                    tesList2.add( tes );

                else if( typeId == 11 && scc.getIsInterest())
                    tesList2.add( tes );
                else if( typeId == 21 && scc.isAnyCustom() && (scc.equals( SimCompetencyClass.CUSTOM) || scc.equals( SimCompetencyClass.CUSTOM_COMBO)))
                    tesList2.add( tes );
                else if( typeId == 22 && scc.isAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM2))
                    tesList2.add( tes );
                else if( typeId == 23 && scc.isAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM3))
                    tesList2.add( tes );
                else if( typeId == 24 && scc.isAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM4))
                    tesList2.add( tes );
                else if( typeId == 25 && scc.isAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM5))
                    tesList2.add( tes );
            }

            Collections.sort( tesList2, new DisplayOrderComparator() );  // new TESNameComparator() );

            if( !tesList2.isEmpty() )
            {
                String style = rowStyle1;

                String ttext=null;


                String key = null;

                if( typeId==0 )
                {
                    key = "g.KSAEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
                }

                if( typeId == 1 )
                    key = "g.Tasks";

                if( typeId == 2 )
                {
                    if( getReportRuleAsBoolean( "biodataisscoredsurvey" ) )
                        key = "g.ScoredSurveyEmTtl";
                    else
                    {
                        key = "g.BiodataEmTtl";
                        ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId() );
                    }
                }

                if( typeId==3 )
                {
                    key = "g.AIMSEmTtl";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
                }

                if( typeId==4 )
                {
                    key = "g.AbilitiesEmTtl";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
                }

                if( typeId==5 )
                {
                    key = "g.KsEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
                }

                if( typeId == 6 )
                {
                    key = "g.EQEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId() );
                }

                if( typeId == 10 )
                {
                    key = "g.AIEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId() );
                }

                if( typeId==11 )
                {
                    key = "g.InterestsTitle";
                    ttext = getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.INTERESTS.getSimCompetencyGroupTypeId() );
                }

                if( typeId>=21 && typeId<=25 )
                {
                    key = "g.CustomTitle" + (typeId-20);
                    ttext = getReportRuleAsString( "competencygrouptitle" + (typeId+80) );
                }


                String title = ttext==null || ttext.isBlank() ? lmsg( key , null ) : ttext;

                if( title!=null && (title.equals("[HIDE]") || title.equals("[HIDESUM]")) )
                    title = "";
                
                sb.append( getRowTitle( rowStyleHdr, title, isIncludeSubcategoryNumeric() ? lmsg( "g.Score" ) : null, isIncludeSubcategoryNorms() ? lmsg( "g.Percentile" ) : null , this.isIncludeSubcategoryCategory() ? lmsg(this.useRatingAndColors() ? "g.Rating" : "g.MatchJob" ) : null ) );

                String label;
                String value,value2;
                boolean showColorRating;

                int scrDigits;

                boolean showNumeric = isIncludeSubcategoryNumeric() && !getReportRuleAsBoolean( "cmptynumoff" );
                // boolean showGraph = !getReportRuleAsBoolean( "cmptygrphoff" );
                boolean percentiles =  isIncludeSubcategoryNorms() && !getReportRuleAsBoolean( "skipcomparisonsection" );
            // String ctxt = reportData.getReportRule( "hidecaveats" );
                // boolean metas = !getReportRuleAsBoolean( "hidecaveats" ) && !getReportRuleAsBoolean( "cmptymetasoff" );
                boolean showCaveats = !getReportRuleAsBoolean( "hidecaveats" ) && !getReportRuleAsBoolean( "cmptymetasoff" ) && !getReportRuleAsBoolean("hidecompetencydetail");
                boolean showTopics = !getReportRuleAsBoolean( "cmptytopicsoff" ) && !getReportRuleAsBoolean("hidecompetencydetail");
                Object[] tdd;

                for( TestEventScore tes : tesList2 )
                {
                    // label = tes.getName();

                    label = ReportUtils.getCompetencyNameToUseInReporting( te, tes, te.getSimXmlObj(), te.getProduct(), locale );

                    if( tes.getUsesPercentCorrectScoring() )
                    {
                        usesPercentCorrectScoring=true;
                        label += " *";
                    }

                    // LogService.logIt( "BaseScoreFormatter.getCompetencyTaskSection() label=" + label + ", tes.getUsesPercentCorrectScoring()=" + tes.getUsesPercentCorrectScoring() );

                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    scrDigits = getReport().getIntParam3() >= 0 ? getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();

                    // LogService.logIt( "BaseScoreFormatter.getCompetencyTaskSection() key=" + key + ", (report=" + (getReport()==null ? "null" : "not null: id=" + getReport().getReportId() + ", int3=" + getReport().getIntParam3()));

                    if( showNumeric && tes.getIncludeNumericScoreInResults() )
                       value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), scrDigits );

                    else
                        value =  "-";

                    value2 = "";

                    // caveats = null;

                    //if( showCaveats && (tes.getSimCompetencyClass().isScoredDataEntry() || tes.getSimCompetencyClass().isScoredTyping() ) )
                    //    caveats = tes.getCaveatList();

                    if( !showNumeric ) // || !isIncludeSubcategoryNumeric() )
                        value = "";

                    //if( !tes.getIncludeNumericScoreInResults() )
                    //    value = "-";

                    showColorRating = isIncludeSubcategoryCategory() && tes.getScoreCategoryType().hasColor();

                    if( !tes.getIncludeNumericScoreInResults() || !showNumeric )
                        showColorRating= false;

                    // LogService.logIt( "BaseScoreFormatter.getStandardComptencyTaskSection() tes.name=" + tes.getName() +", tes.getIncludeNumericScoreInResults()=" + tes.getIncludeNumericScoreInResults() + ", value=" + value + ", score=" + tes.getScore() + ", hasColor=" + tes.getScoreCategoryType().hasColor() );


                    if( percentiles )
                    {
                        // No percentiles for biodata, interests, or personality or Emotional Intell
                        value2 = (typeId==2 || typeId==3 || typeId==6 || typeId==11 ) ? lmsg( "g.NA") : getSubcategoryNormString( tes.getPercentile(), tes.getOverallPercentileCount(), 0 );

                        // show rating
                        if( showColorRating && tes.getIncludeNumericScoreInResults() )
                            sb.append( getRowColorDot(style, label, value, value2, false, tes.getScoreCategoryType() ) );

                        // not show rating
                        else
                            sb.append( getRow(style, label, value, value2, false ) );

                    }

                    else if( showColorRating && tes.getIncludeNumericScoreInResults() )
                        sb.append( getRowColorDot(style, label, value, false, tes.getScoreCategoryType() ) );

                    else
                        sb.append( getRow(style, label, value, value2, false ) );

                    // LogService.logIt( "BaseScoreFormatter.getStandardComptencyTaskSection() tes.name=" + tes.getName() +", showCaveats=" + showCaveats +", showTopics=" + showTopics );
                    if( showCaveats )
                    {
                        tdd = getNonTopicCaveatScoresRow(tes, typeId, tog );

                        sb.append( (String)tdd[0] );
                        tog = (Boolean)tdd[1];
                        // sb.append( getNonTopicCaveatScoresRow( tes, typeId, tog ) );
                    }

                    if( showTopics && typeId==5  )
                    {
                        tdd = getTopicScoresRowForKSTest(tes, typeId, tog );

                        sb.append( (String)tdd[0] );
                        tog = (Boolean)tdd[1];
                    }

                    // if( sb.toString().indexOf( "Object;@" )>=0 )
                    //    LogService.logIt( "BaseScoreFormatter.getCompetencyTaskSection() Current SB=" + sb.toString() );
                    //if( caveats!=null && !caveats.isEmpty() )
                    //{
                    //    LogService.logIt( "BaseScoreFormatter.getCompetencyTaskSection() Adding " + caveats.size() + " caveats." );
                    //    sb.append( getRow( style,  caveats  ) );
                    //}
                }

                if( usesPercentCorrectScoring )
                {
                    sb.append( getRow(style, "* " + lmsg("g.CompetencyUsesPercentCorrectScoring"), false ) );
                }
            }
        }


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    protected Object[] getTopicScoresRowForKSTest( TestEventScore tes, int typeId, boolean tog )
    {
        Object[] out = new Object[2];
        out[0]="";
        out[1] = tog;

        //if( getReportRuleAsBoolean( "cmptytopicsoff" ) )
        //    return out;

        List<String[]> cl = ReportUtils.getParsedTopicScoresForCaveatScores(tes.getTopicCaveatScoreList(), locale, tes.getSimCompetencyClassId() );

        if( cl==null || cl.isEmpty() )
            return out;

        if( te.getProduct()!=null &&
            locale !=null && locale.getLanguage().equalsIgnoreCase( "en" ) &&
            !locale.getLanguage().equalsIgnoreCase( te.getProduct().getLocaleFmLangStr().getLanguage() ) &&
            te.getProduct().getLongParam4()>0  )
        {
            try
            {
                if( equivTopicNameMap==null )
                {
                    EventFacade eventFacade = EventFacade.getInstance();

                    SimDescriptor sd = eventFacade.getSimDescriptor( te.getSimId(), te.getSimVersionId(), true );
                    SimJUtils simJUtils = new SimJUtils( JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ));

                    sd = eventFacade.getSimDescriptor( te.getProduct().getLongParam4(), -1, true );
                    SimJUtils equivSimJUtils = new SimJUtils( JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ));

                    equivTopicNameMap = ReportUtils.createEquivTopicNameMap(simJUtils, equivSimJUtils);
                }

                ReportUtils.swapTopicNames( equivTopicNameMap, cl );
            }
            catch( Exception e )
            {
                LogService.logIt( e, "BaseScoreFormatter.getTopicScoresRowForKSTest() testEventId=" + te.getTestEventId() + ", product=" + ( te.getProduct()==null ? "null" : te.getProduct().getName() + " (" + te.getProduct().getProductId() + ") " ) );
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append( "<table cellpadding=\"1\" style=\"margin-left:20px\">\n" );

        for( String[] ct : cl )
            sb.append( "<tr><td>&#8226;</td><td>" + ct[1] + ":</td><td>" + ct[2] + "</td></tr>\n" );

        sb.append( "</table>\n" );

        // tog = !tog;

        String style = tog ? rowStyle1 : rowStyle2;

        String o = getRow( style, sb.toString(), false );

        return new Object[] {o, tog };
    }


    protected Object[] getNonTopicCaveatScoresRow( TestEventScore tes, int typeId, boolean tog )
    {
        Object[] out = new Object[2];
        out[0]="";
        out[1] = tog;

        //if( getReportRuleAsBoolean("hidecaveats") )
        //    return out;

        List<CaveatScore> cl2 = new ArrayList<>();

        // LogService.logIt( "BaseScoreFormatter.getNonTopicCaveatScoresRow() AAA typeId=" + typeId + ", tes=" + tes.getName() + ", tes.cl.size=" + tes.getNonTopicCaveatScoreList().size() );

        for( CaveatScore ct : tes.getNonTopicCaveatScoreList() )
        {
            if( !ct.getHasValidInfo())
                continue;

            if( ct.getCaveatScoreType().getIsTopic() ) // Constants.TOPIC_KEY + "~" ) )
                continue;

            cl2.add( ct );
        }


        // LogService.logIt( "BaseScoreFormatter.getNonTopicCaveatScoresRow() BBB cl2.size=" + cl2.size() );

        if( cl2.isEmpty() )
            return out;

        // tog = !tog;
        String style = tog ? rowStyle1 : rowStyle2;

        String o = getRowForCaveatScoreList( style, cl2  );

        return new Object[] {o, tog };
    }



    public Object[] getStandardIbmInsightScoresSection( boolean tog, String rowStyleHeader) throws Exception
    {
        Object[] out = new Object[2];

        tog = true;

        StringBuilder sb = new StringBuilder();

        if( isIncludeIbmInsight() )
        {
            List<TextAndTitle>  ttList = getTestEvent().getOverallTestEventScore().getTextBasedResponseList( Constants.IBMINSIGHT, false );

            if( ttList.isEmpty() )
            {
                out[0]=sb.toString();
                out[1] = tog;
                return out;
            }

            List<TextAndTitle> hraTraitTtl = new ArrayList<>();

            for( TextAndTitle tt : ttList )
            {
                if( SentinoUtils.getIsHraTrait( tt ) )
                    hraTraitTtl.add( tt );
            }


            if( !hraTraitTtl.isEmpty() )
            {
                Object[] subdat = getStandardIbmInsightScoresSubsection( tog, rowStyleHeader,  hraTraitTtl );
                if( subdat[0]!=null )
                    sb.append( (String) subdat[0] );
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    public Object[] getStandardIbmInsightScoresSubsection( boolean tog, String rowStyleHeader, List<TextAndTitle> ttlAll ) throws Exception
    {
        Object[] out = new Object[2];
        out[0] = "";
        out[1] = tog;

        if( ttlAll.isEmpty() )
            return out;

        List<HraTrait> ttlx = new ArrayList<>();

        HraTrait irtx;

        for( TextAndTitle tt : ttlAll)
        {
            irtx = new HraTrait( tt );

            //if( !irtx.isValid() )
            //{
            //    LogService.logIt(  "BaseScoreFormatter.getStandardIbmInsightScoresSubsection() BBB.2 HraTrait.TextAndTitle is invalid=" + irtx.toString() );
            //    continue;
            //}

            irtx.setLocale( getLocale() );

            ttlx.add( irtx);
        }

        Collections.sort( ttlx );

        if( ttlx.isEmpty() )
        {
            LogService.logIt(  "BaseScoreFormatter.getStandardIbmInsightScoresSubsection() BBB.3 No valid TextAndTitles found" );
            return out;
        }


        tog = true;

        StringBuilder sb = new StringBuilder();

        String style = rowStyle1;

        // String subtitle = insightTraitType.getSubtitleKey()==null ? "" : "<span style=\"font-weight:normal\">" + lmsg( insightTraitType.getSubtitleKey() ) + "</span>";

        sb.append( getRowTitleSubtitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, lmsg( "g.IbmInsightInfoTitle" ), null ) );

        // sb.append( getRowSpacer( s0 ) );

        boolean hasUnavailable=false;

        for( HraTrait irt : ttlx )
        {
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;

            if( !irt.isValid() )
            {
                hasUnavailable=true;
                sb.append( "<tr " + style + "><td style=\"width:20px\"></td><td style=\"font-weight:bold;vertical-align:middle\">" + irt.getNameXhtml() + "</td><td style=\"\" colspan=\"3\">" + lmsg("g.IbmInsightLowConfidenceMsg") + "</td></tr>\n" );
                sb.append( "<tr " + style + "><td style=\"width:20px\"></td><td colspan=\"4\">" + StringUtils.replaceStandardEntities( irt.getDescripStrXhtml()) + "</td></tr>\n" );
                continue;
            }

            sb.append( "<tr " + style + "><td style=\"width:20px\"></td><td style=\"font-weight:bold;vertical-align:middle\">" + irt.getNameXhtml() + "</td><td style=\"font-weight:bold\" colspan=\"1\">" + irt.getHraScoreStr() + "</td>\n" +
                        "<td style=\"vertical-align:bottom\" colspan=\"2\">" +
                        "<img style=\"width:" + (Constants.CT2_COLORGRAPHWID_EML + 8) + "px;height:20px\" alt=\"" + MessageFactory.getStringMessage(locale, "g.CT2GraphicAlt" ) + "\" src=\"" + irt.getColorGraphUrl() + "\"/></td></tr>\n" );

            sb.append( "<tr " + style + "><td style=\"width:20px\"></td><td colspan=\"4\">" + StringUtils.replaceStandardEntities( irt.getDescripStrXhtml()) + "</td></tr>\n" );

            sb.append( "<tr " + style + "><td style=\"width:20px\"></td><td colspan=\"4\">" + StringUtils.replaceStandardEntities( irt.getScoreTextStrXhtml()) + "</td></tr>\n" );
        }

        if( hasUnavailable )
        {
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            sb.append( "<tr " + style + "><td colspan=\"5\">" + lmsg("g.IbmInsightLowConfidenceNote") + "</td></tr>\n" );

        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }




    public Object[] getStandardWritingSampleSection( boolean tog, String rowStyleHeader) throws Exception
    {
        Object[] out = new Object[2];

        tog = true;

        StringBuilder sb = new StringBuilder();

        if( isIncludeWritingSamples() )
        {
            List<TextAndTitle>  ttList = getTestEvent().getOverallTestEventScore().getTextBasedResponseList( NonCompetencyItemType.WRITING_SAMPLE.getTitle(), false );

            if( !ttList.isEmpty() )
            {
                String style = rowStyle1;

                String subtitle = "<span style=\"font-weight:normal\">" + lmsg( "g.WritingSampleSubtitle" ) + "</span>";

                sb.append( getRowTitleSubtitle( rowStyleHdr, lmsg(  ttList.size() > 1 ? "g.WritingSamples" : "g.WritingSample", null ), subtitle ) );

                // sb.append( getRowSpacer( s0 ) );

                String nm;
                String txt;
                String str1;
                String summary;
                
                for( TextAndTitle tt : ttList )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    //String nm  = StringUtils.replaceStandardEntities( XMLUtils.decodeURIComponent(tt.getTitle()) );

                    //String txt = StringUtils.replaceStandardEntities( XMLUtils.decodeURIComponent(tt.getText()) );

                    nm  = StringUtils.getHasHtml( tt.getTitle() ) ? tt.getTitle() : StringUtils.replaceStandardEntities( tt.getTitle() );
                    txt = StringUtils.getHasHtml( tt.getText() ) ? tt.getText() : StringUtils.replaceStandardEntities( tt.getText() );
                    str1 = StringUtils.replaceStandardEntities( tt.getString1() );
                    summary = StringUtils.replaceStandardEntities( tt.getString4() );

                    if( summary!=null && !summary.isBlank() )
                    {
                        txt = "<div style=\"padding-bottom:8px\"><b>" + lmsg( "g.SummaryAI" ) + "</b>: " + summary + "</div><b>" + lmsg("g.FromCandidate") + ":</b> " + txt;
                    }
                    
                    if( str1!=null && !str1.isEmpty() )
                        txt += "<div style=\"padding-top:6px\">[" + lmsg( "g.MisSpelledWordsC" ) + " " + str1 + "]</div>";

                    str1 = StringUtils.replaceStandardEntities( tt.getString3() );

                    if( str1!=null && !str1.isEmpty() )
                        txt += "<div style=\"padding-top:6px\">[" + lmsg( "g.ReverseTranslatedC" ) + " " + str1 + "]</div>";

                    sb.append( getRow( style, nm, txt, false ) );
                 }
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    public Object[] getStandardResponseRatingSummarySection( boolean tog ) throws Exception
    {
        Object[] out = new Object[2];

        tog = true;

        StringBuilder sb = new StringBuilder();
        out[0] = sb.toString();
        out[1] = tog;

        if( getTestEvent().getTestEventResponseRatingList()==null || getTestEvent().getTestEventResponseRatingList().isEmpty() )
            return out;

        if( !TestEventResponseRatingUtils.getHasAnyNonSimCompetencyRatings(getTestEvent().getTestEventResponseRatingList()))
        {
            LogService.logIt( "BaseScoreFormatter.getStandardResponseRatingSummarySection() TestEvent does not have any TestEventResponseRatings that are not associated with a SimCompetency. testEventId=" + getTestEvent().getTestEventId() );
            return out;
        }

        Map<String,String> avgRatingMap = TestEventResponseRatingUtils.getOverallAverageRatingMap( getTestEvent().getTestEventResponseRatingList(), getLocale() );

        if( avgRatingMap==null || avgRatingMap.isEmpty() )
        {
            LogService.logIt( "BaseScoreFormatter.getStandardResponseRatingSummarySection() Average Rating Map is null or empty. testEventId=" + getTestEvent().getTestEventId() );
            return out;
        }

        String style = rowStyle1;

        // String subtitle = "<span style=\"font-weight:normal\">" + lmsg( "g.AvgResponseRatingsSubtitle" ) + "</span>";

        sb.append( getRowTitleSubtitle( rowStyleHdr, lmsg(  "g.AvgResponseRatings", null ), null ) );

        // sb.append( getRow( rowStyleSubHdr, StringUtils.replaceStandardEntities( lmsg(  "g.RatingCategory", null ) ), lmsg("g.AverageRating", null), true ) );

        String value;
        for( String name : avgRatingMap.keySet() )
        {
            value=avgRatingMap.get(name);

            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;

            sb.append( getRow( style, name + ":", value, false ) );
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }

    public Object[] getStandardItemResponsesSection( boolean tog ) throws Exception
    {
        Object[] out = new Object[2];

        tog = true;

        StringBuilder sb = new StringBuilder();
        out[0] = sb.toString();
        out[1] = tog;

        if( getReportRuleAsBoolean( "itmscoresoff" ) )
            return out;

        if( report.getIncludeItemScores() > 0 || getReportRuleAsBoolean( "itmscoreson" ) )
        {
            // LogService.logIt(  "BaseScoreFormatter.addIdentityImageCaptureInfo() " );
            List<TestEventScore> tesl = getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            if( tesl.isEmpty() )
                return out;

            ListIterator<TestEventScore> iter = tesl.listIterator();

            TestEventScore tes;

            String cl = null;

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

            if( tesl.isEmpty() )
                return out;

            String style = rowStyle1;

            String subtitle = "<span style=\"font-weight:normal\">" + lmsg( "g.ItemScoresInfoSubtitle" ) + "</span>";

            sb.append( getRowTitleSubtitle( rowStyleHdr, lmsg(  "g.ItemScoresInfoTitle", null ), subtitle ) );

            String competencyTitle;
            IncludeItemScoresType iist;

            // We have some TES's to write to.
            List<TextAndTitle> ttl;
            String itemRespInfo;
            String theTitle;

            for( TestEventScore tesx : tesl )
            {
                iist = IncludeItemScoresType.getValue( tesx.getIntParam2() );
                cl = StringUtils.getBracketedArtifactFromString(tesx.getTextParam1() , Constants.ITEMSCOREINFOKEY );

                if( cl==null || cl.trim().isEmpty() )
                    continue;

                ttl = ScoreFormatUtils.unpackTextBasedResponses( cl );

                if( ttl.isEmpty() )
                    continue;

                competencyTitle = tesx.getName() + " (" + iist.getName4Reports( getLocale() )  + ") ";

                sb.append( getRow( rowStyleSubHdr, StringUtils.replaceStandardEntities( competencyTitle ), true ) );

                for( TextAndTitle tt : ttl )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    // This is often the question, which can have HTML
                    theTitle = tt.getTitle();
                    if( !StringUtils.getHasHtml(theTitle) )
                        theTitle = StringUtils.replaceStandardEntities( theTitle );

                    itemRespInfo = tt.getText();
                    if( itemRespInfo==null )
                        itemRespInfo="";

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

                    if( !StringUtils.getHasHtml(itemRespInfo) )
                        itemRespInfo = StringUtils.replaceStandardEntities( itemRespInfo );

                    sb.append( getRow( style, theTitle, itemRespInfo, false ) );
                }

            }

        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }



    public Object[] generateImageCaptureSection( boolean tog,
                                                 String rowStyleHeader,
                                                 List<UploadedUserFile> ufl,  // These are Uploaded User Files for each thumb of a FACE
                                                 List<UploadedUserFile> uflId, // These are Uploaded User Files for each thumb of an ID
                                                 List<String[]> caveatList,
                                                 float overallProctorScore) throws Exception
    {
        Object[] out = new Object[2];
        tog = true;
        out[0] = "";
        out[1] = tog;

        StringBuilder sb = new StringBuilder();

        if( ufl==null )
            ufl = new ArrayList<>();
        if( uflId==null )
            uflId=new ArrayList<>();

        boolean hasImages = !ufl.isEmpty() || !uflId.isEmpty();

        boolean showImages = hasImages && !getReportRuleAsBoolean( "captimgsoff") && !getTestKey().getHideMediaInReports();

        if( overallProctorScore<=0 && !showImages )
            return out;

        String style = tog ? rowStyle1 : rowStyle2;
        sb.append( getRowTitleSubtitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, lmsg(  "g.ImgCapReportTitle" ), null ) );

        StringBuilder sb2 = new StringBuilder();

        sb2.append( "<table style=\"width:100%\" cellpadding=\"2\" cellspacing=\"0\">\n" );
        String nm = lmsg( "g.ImgCapRisk" );

        String s2 = null;
        String bgCol = null;
        String fgCol = "white";

        //if( itemsToCompare>1 )
        //{
            if( overallProctorScore <= 0 )
            {
                s2 = lmsg( "g.ImgCapRiskUnavailPrem" );
                bgCol =  "#cccccc";
            }
            else if( overallProctorScore <= 33.33f )
            {
                s2 = lmsg( "g.ImgCapRiskHigh" );
                bgCol =  "#ff0000";
            }

            // yellow
            else if( overallProctorScore <= 75f )
            {
                s2 = lmsg( "g.ImgCapRiskMedium" );
                bgCol = "#fcee21";
                fgCol = "#3d3d3d";
            }
            else
            {
                s2 = lmsg( "g.ImgCapRiskLow" );
                bgCol = "#69a220";
            }


            sb2.append( "<tr><td style=\"width:20px;background-color:" + bgCol +  ";color:" + fgCol + "\"></td><td style=\"font-weight:bold;vertical-align:top;background-color:" + bgCol + ";color:" + fgCol + "\">- " + nm + "</td><td style=\"font-weight:bold;background-color:" + bgCol  +  ";color:" + fgCol + "\">" + s2 + "</td></tr>\n" );
            // sb.append( "<tr " + style + "><td style=\"width:20px\"></td><td style=\"font-weight:normal;vertical-align:top;color:white;background-color:" + bgCol +  "\">- " + nm + "</td><td colspan=\"3\" style=\"color:white;background-color:" + bgCol +  "\">" + s2 + "</td></tr>\n" );
        //}


        String s1;
        int idx;

        // Caveats
        for( String[] c : caveatList )
        {
            if( c.length<2 || c[1].isEmpty() )
                sb2.append( "<tr><td style=\"width:20px\"></td><td colspan=\"2\">- " + c[0] + "</td></tr>\n" );
            else
                sb2.append( "<tr><td style=\"width:20px\"></td><td>- " + c[0] + "</td><td>" + c[1] + "</td></tr>\n" );
        }
        sb2.append( "</table>\n");

        tog = !tog;
        style = tog ? rowStyle1 : rowStyle2;
        sb.append( "<tr " + style + "><td colspan=\"5\">\n" + sb2.toString() + "</td></tr>\n" );

        String ts =  "<br /><span style=\"color:red\">" + lmsg( "g.Timeout" ) + "</span>" ;

        int imgCnt = 0;
        // images for all
        sb2 = new StringBuilder();
        if( org.getCandidateImageViewTypeId()<=0 && showImages )
        {

            List<UploadedUserFile> ufx = new ArrayList<>();

            int initFacePhotoCt = 0;
            // face photos.
            if( ufl.size()>0 )
            {
                for( UploadedUserFile u : ufl )
                {
                    if( !u.isPreTestImage() )
                        break;
                    initFacePhotoCt++;
                }

                if( initFacePhotoCt<=0 )
                    initFacePhotoCt=1;

                ufx.addAll( ufl.subList(0,initFacePhotoCt) );

                if( ufx.size()>Constants.MAX_INITIAL_PHOTO_IMAGES_IN_REPORT )
                    ufx = ufx.subList( ufx.size()-Constants.MAX_INITIAL_PHOTO_IMAGES_IN_REPORT, ufx.size() );
                //sb2.append( addImageCells(  ufl.subList(0,1), ts, true ) );
                //sb2.append( "</tr>\n" );
                imgCnt+=ufx.size();
            }

            // add all ids up to max
            if( !uflId.isEmpty() )
            {
                if( uflId.size()>Constants.MAX_IDCARD_IMAGES_IN_REPORT )
                {
                    ufx.addAll( uflId.subList( uflId.size()-Constants.MAX_IDCARD_IMAGES_IN_REPORT, uflId.size() ) );
                    // sb2.append( addImageCells(  uflId.subList( uflId.size()-Constants.MAX_IDCARD_IMAGES_IN_REPORT, uflId.size() ), ts, true ) );
                    imgCnt+=Constants.MAX_IDCARD_IMAGES_IN_REPORT;
                }
                else
                {
                    ufx.addAll( uflId );
                    // sb2.append( addImageCells(  uflId, ts, true ) );
                    imgCnt+=uflId.size();
                }
                //sb2.append( "</tr>\n" );
            }

            // remaining face photos
            if( ufl.size()>initFacePhotoCt )
            {
                //int maxImgs = Constants.MAX_IDENTITY_IMAGES_IN_REPORT - imgCnt;
                //ufl = ufl.subList(initFacePhotoCt, ufl.size());
                //if( ufl.size()>maxImgs)
                //{
                //    Collections.shuffle( ufl );
                //    ufx.addAll( ufl.subList(0, maxImgs) );
                //    //sb2.append( addImageCells(  ufl.subList(0, maxImgs), ts, true ) );
                //}
                //else
                //{
                    ufx.addAll( ufl.subList(initFacePhotoCt, ufl.size()) );
                    //sb2.append( addImageCells(  ufl, ts, true ) );
                //}
                //sb2.append( "</tr>\n" );
            }

            if( !ufx.isEmpty() )
            {
                sb2.append( "<table style=\"margin-left:auto;margin-right:auto\">\n" );
                sb2.append( addImageCells(  ufx, ts, true ) );
                sb2.append( "</tr>\n" );

                sb2.append( "</table>\n" );
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                sb.append( "<tr " + style + "><td colspan=\"5\">\n" + sb2.toString() + "</td></tr>\n" );
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    private String addImageCells( List<UploadedUserFile> ufl, String timeoutStr, boolean tog )
    {
        String thumbFn;
        String thumbUrl;
        int thumbIndex;
        boolean timeout;

        String dateTimeStr;
        String infoStr;
        String transform;
        String divTransform;
        String imageCss;

        int count=0;
        StringBuilder sb = new StringBuilder();

        for( UploadedUserFile uuf : ufl )
        {
            thumbIndex=uuf.getTempInt3();
            thumbFn = uuf.getThumbFilename();

            // thumbUrl=uuf.getTempStr1();
            thumbUrl=ReportUtils.getMediaTempUrlSourceLink(org.getOrgId(), uuf, thumbIndex, thumbFn, MediaTempUrlSourceType.PROCTOR_THUMB );
            timeout = uuf.getTempInt1()==1;
            dateTimeStr =uuf.getTempStr2();
            if( dateTimeStr==null )
                dateTimeStr="";


            transform = "";
            divTransform = "";

            if( count==0 )
                sb.append( "<tr>" );

            count++;

            if( count>=4 )
            {
                sb.append( "</tr>\n");
                sb.append( "<tr>" );
                count=1;
            }

            transform = uuf.rotationCss( true );
            divTransform = uuf.getRotationOffsetCss();
            imageCss = getProctorImageStatusCss( uuf );

            infoStr = ProctorUtils.getProctorImageIdStr( uuf, locale );

            dateTimeStr += (!dateTimeStr.isBlank() ? ", " : "" ) + infoStr;
            // transform = uuf.getOrientation()<=0 ? ";max-width:200px;" : ";max-width:200px;transform-origin:center;transform:rotate(" + uuf.getOrientation() + "deg);";

            //timeout=true;
            sb.append( "<td style=\"padding:2px;text-align:center\" ><div style=\"padding:2px;" + divTransform + "\"><img src=\"" + thumbUrl + "\" alt=\"Image Capture Thumbnail\" style=\"max-width:200px;" + transform + imageCss + "\"/></div><div style=\"font-size:10pt;font-style:italic;text-align:center;padding:2px\">" + dateTimeStr + (timeout ? timeoutStr : "") + "</div></td>\n" );
        }

        return sb.toString();
    }


    public String getProctorImageStatusCss( UploadedUserFile uuf )
    {
        String out = "";

        if( uuf==null )
            return out;

        if( uuf.isFailedImage() )
            out = "#ff0000";

        // blue
        else if( uuf.isPreTestImage() )
            out = "#0000ff";

        else if( uuf.getUploadedUserFileType().getIsRemoteProctoringId() )
            out = "#ffff00";

        else
            out = "#00ff00";

        if( !out.isBlank() )
            out = ";border-radius:7px;border:3px solid " + out;

        return out;

    }



    public Object[] getStandardPremiumImageCaptureSection( boolean tog, String rowStyleHeader, boolean includeTop) throws Exception
    {
        Object[] out = new Object[2];
        tog = true;
        out[0] = "";
        out[1] = tog;

        if( !ProctorHelpUtils.getUseExternalProctoring(getTestKey()) )
            return out;

        RemoteProctorEvent rpe = getTestEvent().getRemoteProctorEvent();
        if( rpe==null || rpe.getUploadedUserFileList()==null )
        {
            if( proctorUtils==null )
                proctorUtils = new ProctorUtils();
            proctorUtils.setupRemoteProctorEvent( getLocale(), getUser().getTimeZone(), getTestEvent() );
            rpe = getTestEvent().getRemoteProctorEvent();
        }

        if( rpe==null )
            return out;

        // boolean imageComparisonsComplete = rpe.getRemoteProctorEventStatusTypeId()>=RemoteProctorEventStatusType.IMAGE_COMPARISONS_COMPLETE.getRemoteProctorEventStatusTypeId();

        // No photos at all.
        List<UploadedUserFile> ufl = rpe.getUploadedUserFileListForPhotos();
        List<UploadedUserFile> uflRec = rpe.getUploadedUserFileListForRecordings();
        if( !uflRec.isEmpty() )
            ufl.addAll(uflRec);

        List<UploadedUserFile> uflIds = rpe.getUploadedUserFileListForIds();
        if( ufl.isEmpty() && uflIds.isEmpty() )
            return out;

        boolean showImages = getOrg().getCandidateImageViewTypeId()<=0 && !getReportRuleAsBoolean( "captimgsoff" ) && !getTestKey().getHideMediaInReports();

        // For batteries, only show images for the first test event.
        if( showImages && tk.getBatteryId()>0 )
            showImages = includeTop;

        boolean forceIncludeAllImages = false; // showImages && (rpe.getMultiFaceThumbs()>0);

        if( proctorUtils==null )
            proctorUtils = new ProctorUtils();
        List<String[]> caveatList = proctorUtils.getPremiumCaveatList(getTestKey().getProctoringIdCaptureTypeId(), rpe, getLocale(), false );
        List<UploadedUserFile> ufl2 = showImages ? proctorUtils.getFauxUploadedUserFileListForReportThumbs(ufl, forceIncludeAllImages, 0 ) : new ArrayList<>();
        List<UploadedUserFile> uflId2 = showImages ? proctorUtils.getFauxUploadedUserFileListForReportThumbs(uflIds, forceIncludeAllImages, 0 ) : new ArrayList<>();

        if( tk.getBatteryId()>0 && !includeTop && (!ufl.isEmpty() || !uflIds.isEmpty()) )
            caveatList.add( new String[] { lmsg( "g.ProcImgsInTestEvtAbove" ), ""} );

        return generateImageCaptureSection( tog,
                                            rowStyleHeader,
                                            ufl2,
                                            uflId2,
                                            caveatList,
                                            rpe.getOverallProctorScore() );
    }






    public Object[] getStandardImageCaptureSection( boolean tog, String rowStyleHeader, boolean includeTop ) throws Exception
    {
        if( ProctorHelpUtils.getUseExternalProctoring(getTestKey())  )
        {
            if( getTestEvent().getRemoteProctorEvent()==null || getTestEvent().getRemoteProctorEvent().getUploadedUserFileList()==null  )
            {
                if( proctorUtils==null )
                    proctorUtils = new ProctorUtils();
                proctorUtils.setupRemoteProctorEvent( getLocale(), getUser().getTimeZone(), getTestEvent() );
            }

            if( getTestEvent().getRemoteProctorEvent()==null )
            {
                LogService.logIt( "BaseScoreFormatter.getStandardImageCaptureSection() Expected RemoteProctorinEvent but not present. Returning. testKeyId=" + getTestKey().getTestKeyId() + ", onlineProctoringType=" + getTestKey().getOnlineProctoringType().getKey());
                Object[] out = new Object[2];
                tog = true;
                out[0] = "";
                out[1] = tog;
                return out;
            }
            if( getTestKey().getOnlineProctoringType().getIsPremiumWithImageCap() || getTestEvent().getRemoteProctorEvent()!=null )
                return getStandardPremiumImageCaptureSection(tog, rowStyleHeader, includeTop );
        }

        Object[] out = new Object[2];
        tog = true;
        out[0] = "";
        out[1] = tog;
        return out;

        /*
        try
        {


            List<TestEventScore> tesl = new ArrayList<>();
            for( TestEventScore tes : getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                if( tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDIMAGEUPLOAD.getSimCompetencyClassId() )
                    tesl.add( tes );
            }

           if( tesl.isEmpty() )
               return out;

            float overallScore = 0;
            // int pairsCompared = 0;
            List<UploadedUserFile> ufl = new ArrayList<>();
            List<UploadedUserFile> uflId = new ArrayList<>();
            List<String[]> caveatList = new ArrayList<>();

            if( tesl.isEmpty() )
                return out;

            java.util.List<TextAndTitle> ttl = new ArrayList<>();
            List<String> cl = new ArrayList<>();

            for( TestEventScore tes : tesl )
            {
                ttl.addAll( tes.getTextBasedResponseList( null, true, true ) );
                cl.addAll( tes.getCaveatList() );
                overallScore += tes.getScore();
                // pairsCompared += (int)(tes.getScore2() - tes.getScore4());
            }

            if( ttl.isEmpty() )
                return out;

            overallScore = tesl.size()>1 ? overallScore/tesl.size() : overallScore;
            String s1,s2;
            int idx;
            for( String c : cl )
            {
                s1 = c;
                s2="";
                idx=c.indexOf(":");
                if( idx>0 )
                {
                    s1 = c.substring(0,idx+1);
                    s2= c.substring(idx+1,c.length());
                }
                caveatList.add( new String[]{s1,s2});
            }

            UploadedUserFile uuf;
            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();
            String thumbUrl;
            String dateTimeStr;
            boolean timeout;

            for( TextAndTitle tt : ttl )
            {
                uuf = fileUploadFacade.getUploadedUserFile( tt.getUploadedUserFileId(), true );
                if( uuf==null )
                {
                    LogService.logIt( "BaseScoreFormatter,getStandardImageCaptureSection() UploadedUserFile for uufId=" + tt.getUploadedUserFileId() + " NOT FOUND. Could be a stray Scored Response so ignoring. TestEventId=" + getTestEvent().getTestEventId() );
                    continue;
                }

                if( !uuf.getUploadedUserFileStatusType().getAvailable()  )
                {
                    LogService.logIt( "BaseScoreFormatter,getStandardImageCaptureSection() UploadedUserFile for uufId=" + tt.getUploadedUserFileId() + " is not available (prob pseudonymized). Skipping. TestEventId=" + getTestEvent().getTestEventId() );
                    continue;
                }

                if( uuf.getFilename()!=null && !uuf.getFilename().isEmpty() )
                {
                    thumbUrl = ReportUtils.getMediaTempUrlSourceLink( org.getOrgId(), uuf, 0, uuf.getFilename(), MediaTempUrlSourceType.PROCTOR_THUMB );
                    //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                    //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + uuf.getFilename();
                    //else
                    //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + uuf.getFilename();

                    uuf.setTempStr1( thumbUrl );
                }
                else
                    continue;

                dateTimeStr = tt.getTitle() + "\n" + tt.getText();
                uuf.setTempStr2(dateTimeStr);

                timeout = tt.getString1()!=null && tt.getString1().equalsIgnoreCase("true");
                uuf.setTempInt1( timeout ? 1 : 0 );
                uuf.setTempInt2( uuf.getOrientation() );

                ufl.add( uuf );
            }

            return generateImageCaptureSection( tog,
                                                rowStyleHeader,
                                                ufl,
                                                uflId,
                                                caveatList,
                                                overallScore );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseScoreFormatter.getStandardImageCaptureSection() " );
            return out;
        }
        */
    }

    
    public Object[] getStandardGenAISection(boolean tog, String rowStyleHeader) throws Exception
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();
        tog = true;
        out[0] = "";
        out[1] = tog;

        if( getReport().getIncludeAiScores()<=0 || getReportRuleAsBoolean( "skipaiscoressection") || getTestKey().getAiMetaScoreList()==null || getTestKey().getAiMetaScoreList().isEmpty() )
            return out;

        LogService.logIt(  "BaseScoreFormatter.getStandardGenAISection() BBB.1 " );

        int valCount = 0;
        for( AiMetaScore ms : getTestKey().getAiMetaScoreList() )
        {
            if( ms.getAiMetaScoreTypeId()>0 && ms.getScore()>0 && ms.getConfidence()>= Constants.MIN_METASCORE_CONFIDENCE )
                valCount++;
        }
        
        if( valCount<=0 )
            return out;

        String style = rowStyle1;
        int scrDigits = getReport().getIntParam2() >= 0 ? getReport().getIntParam2() : getTestEvent().getScorePrecisionDigits();
        String scr;
        String confScr;
        String scoreText;
        int count = 0;
        AiMetaScoreType metaScoreType;
        String lastUpdate;

        String tt = lmsg( "g.AiGenScoresSubtitle" );
        //String subtitle = "<span style=\"font-weight:normal\">" + tt + "</span>";
        // sb.append( getRowTitleSubtitle( rowStyleHdr, lmsg(  "g.AiGenScoresSht", null ), subtitle ) );
        sb.append( getRowTitle( rowStyleHdr, lmsg("g.AiGenScoresSht", null ), lmsg( "g.Score" ), lmsg( "g.Interpretation" ) + " " + lmsg("g.AiGenScoresSubtitleSht"), null ) );

        for( AiMetaScore metaScore : getTestKey().getAiMetaScoreList() )
        {
            if( metaScore.getAiMetaScoreTypeId()<=0 || metaScore.getScore()<=0 || metaScore.getConfidence()<Constants.MIN_METASCORE_CONFIDENCE )
                continue;
            count++;

            metaScoreType = AiMetaScoreType.getValue(metaScore.getAiMetaScoreTypeId() );

            scr = I18nUtils.getFormattedNumber( getLocale(), metaScore.getScore(), scrDigits );
            
            confScr = "(" + lmsg("g.Confidence") + " " + 
                  I18nUtils.getFormattedNumber( getLocale(), metaScore.getConfidence(), 1) + ") ";                

            metaScore.setLocale(getLocale());
            
            scoreText = confScr + metaScoreType.getDescription(getLocale()); //  + " " + lmsg("g.AiMetaScrInputTypesUsed", new String[]{metaScore.getMetaScoreInputTypesStr()});
            
            if( metaScore.getScoreText()!=null && !metaScore.getScoreText().isBlank() )                
                scoreText += "\n\n" + metaScore.getScoreTextXhtml();

            lastUpdate = I18nUtils.getFormattedDateTime(getLocale(), metaScore.getLastUpdate(), getTestKey().getUser().getTimeZone());
            scoreText += "\n\n" + lmsg("g.AiMetaScrCalcDateX", new String[]{lastUpdate}); 
            
            sb.append( this.getRow(style, metaScoreType.getNameForReport(getLocale(), metaScore.getStrParamsArray() ), scr, scoreText , false) );            
        }        

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }

    public Object[] getStandardResumeSection(boolean tog, String rowStyleHeader ) throws Exception
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();
        tog = true;
        out[0] = "";
        out[1] = tog;

        if( getReport().getIncludeResume()<=0 || getReportRuleAsBoolean( "resumereportsoff") || getUser()==null || getUser().getResume()==null )
            return out;

        // LogService.logIt(  "BaseScoreFormatter.getStandardResumeSection() BBB.1 " );

        Resume resume = getUser().getResume();
        resume.parseJsonStr();

        if( !resume.getHasAnyFormData() )
        {
            LogService.logIt(  "BaseScoreFormatter.getStandardResumeSection() BBB.2 Existing Resume has no form data." );
            return out;
        }

        if( getReport().getIncludeResume()==1 && (resume.getSummary()==null || resume.getSummary().isBlank()) )
        {
            LogService.logIt(  "BaseCT2ReportTemplate.addResumeSection() BBB.2 Existing Resume has no form data." );
            return out;
        }

        String style = rowStyle1;
        
        String tt = lmsg( "g.UpdatedOnX", new String[]{I18nUtils.getFormattedDateTime(getLocale(), resume.getLastInputDate(), getUser().getTimeZone())});
        String subtitle = "<span style=\"font-weight:normal\">" + tt + "</span>";
        sb.append( getRowTitleSubtitle( rowStyleHdr, lmsg(  "g.Resume", null ), subtitle ) );

        if( resume.getSummary()!=null && !resume.getSummary().isBlank() )
        {
            sb.append( this.getRow(style, lmsg(  "g.Summary", null ), true ) );
            sb.append( this.getRow(style, resume.getSummaryXhtml(), false ) );
        }

        if( getReport().getIncludeResume()==2 )
        {
            if( resume.getObjective()!=null && !resume.getObjective().isBlank() )
            {
                sb.append( this.getRow(style, lmsg(  "g.Objective", null ), true ) );
                sb.append( this.getRow(style, resume.getObjectiveXhtml(), false ) );
            }

            if( resume.getEducation()!=null && !resume.getEducation().isEmpty() )
            {
                StringBuilder sb2 = new StringBuilder();
                sb2.append( "<ul style=\"margin-top:-7px\">\n" );

                for( ResumeEducation re : resume.getEducation() )
                {
                    sb2.append( "<li>" + StringUtils.replaceStandardEntities(re.toAiString()) + "</li>\n");
                }
                sb2.append( "</ul>\n" );
                
                sb.append( this.getRow( style, lmsg("g.Education"), sb2.toString(), false ));
            }

            
            if( resume.getExperience()!=null && !resume.getExperience().isEmpty() )
            {
                StringBuilder sb2 = new StringBuilder();
                sb2.append( "<ul style=\"margin-top:-7px\">\n" );

                for( ResumeExperience re : resume.getExperience() )
                {
                    sb2.append( "<li>" + StringUtils.replaceStandardEntities(re.toAiString()) + "</li>\n");
                }
                sb2.append( "</ul>\n" );
                
                sb.append( this.getRow( style, lmsg("g.Experience"), sb2.toString(), false ));
            }

            if( resume.getOtherQuals()!=null && !resume.getOtherQuals().isEmpty() )
            {
                StringBuilder sb2 = new StringBuilder();
                sb2.append( "<ul style=\"margin-top:-7px\">\n" );

                for( String re : resume.getOtherQuals())
                {
                    sb2.append( "<li>" + StringUtils.replaceStandardEntities(re) + "</li>\n");
                }
                sb2.append( "</ul>\n" );
                
                sb.append( this.getRow( style, lmsg("g.OtherQualifications"), sb2.toString(), false ));
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }

    public Object[] getStandardProctorCertificationsSection(boolean tog, String rowStyleHeader ) throws Exception
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();
        tog = true;
        out[0] = "";
        out[1] = tog;

        if( getTestKey().getTestKeyProctorTypeId()<=0 || getTestKey().getProctorEntryList()==null )
            return out;

        List<ProctorEntry> pel = getTestKey().getProctorEntryList();


        String style = rowStyle1;


        sb.append( getRowTitleSubtitle( rowStyleHdr, lmsg(  "g.ProctorCerts", null ), null ) );

        if( pel.isEmpty() )
            sb.append( getRow(style, lmsg(  "g.ProctorCertsNone", null ), false ) );

        for( ProctorEntry pe : pel )
        {
            sb.append( getRow(style, I18nUtils.getFormattedDateTime(locale, pe.getEntryDate(), getUser().getTimeZone()), pe.getProctorUser().getFullname(), pe.getNote()==null ? "" : pe.getNote(), false) );
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }



    public Object[] getStandardSuspensionHistorySection(boolean tog, String rowStyleHeader ) throws Exception
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();
        tog = true;
        out[0] = "";
        out[1] = tog;

        if( !getTestKey().getOnlineProctoringType().getIsAnyPremium() && getTestKey().getTestKeyProctorTypeId()<=0 )
            return out;

        if( getTestEvent().getRemoteProctorEvent()==null && getTestKey().getOnlineProctoringType().getIsAnyPremium() )
        {
            if( proctorUtils==null )
                proctorUtils = new ProctorUtils();
            proctorUtils.setupRemoteProctorEvent( getLocale(), getUser().getTimeZone(), getTestEvent() );
        }

        // no need to report
        if( getTestKey().getTestKeyProctorTypeId()<=0 && getTestEvent().getRemoteProctorEvent()==null )
            return out;

        if( getTestEvent().getRemoteProctorEvent()==null )
        {
            LogService.logIt( "BaseScoreFormatter.getStandardSuspensionHistorySection() Expected RemoteProctorinEvent but not present. Returning. testKeyId=" + getTestKey().getTestKeyId() + ", onlineProctoringType=" + getTestKey().getOnlineProctoringType().getKey());
            return out;
        }

        List<ProctorSuspension> psl = getTestKey().getProctorSuspensionList();

        // no suspensions
        //if( getTestEvent().getRemoteProctorEvent()==null && psl==null )
        //    return out;

        RemoteProctorEvent rpe =getTestEvent().getRemoteProctorEvent();

        SuspiciousActivityThresholdType satt = rpe==null ? SuspiciousActivityThresholdType.NEVER : SuspiciousActivityThresholdType.getValue( rpe.getSuspiciousActivityThresholdTypeId() );

        // no local proctor, no automated threshold, no ability for remote proctor to suspend.
        if( getTestKey().getTestKeyProctorTypeId()<=0 && satt.equals( SuspiciousActivityThresholdType.NEVER) && !getTestKey().getOnlineProctoringType().getIsPremiumWithRemoteSuspensionCapability() )
            return out;

        //if( satt.equals( SuspiciousActivityThresholdType.NEVER) )
        //    return;

        List<String[]> shl = (psl!=null && !psl.isEmpty()) || rpe==null || satt.equals( SuspiciousActivityThresholdType.NEVER) ? null :  rpe.getSuspensionHistoryList();

        int totalSus = (shl==null ? 0 : shl.size());
        int totalProc = (psl==null ? 0 : psl.size());
        int total = totalSus + totalProc;

        String style = rowStyle1;

        String subtitle = "<span style=\"font-weight:normal\">" + lmsg( "g.SuspensionHistorySubtitleX", new String[]{ Integer.toString(satt==null ? 0 : satt.getSuspiciousActivityThresholdTypeId())} ) + "</span>";

        sb.append( getRowTitleSubtitle( total<=0 ? rowStyleHdr : rowStyleHdrRed, lmsg(  "g.SuspensionHistoryTitle", null ), subtitle ) );

        if( total<=0 )
            sb.append( this.getRow(style, lmsg(  "g.NoSuspHist", null ), false ) );

        if( totalSus>0 && shl!=null )
        {
            for( String[] sa : shl )
            {
                sb.append( this.getRow(style, sa[0], sa[1], sa[2], false) );
            }
        }

        if( totalProc>0 && psl!=null )
        {
            String note;
            for( ProctorSuspension sh : psl )
            {
                note = sh.getNote();
                if( note==null )
                    note="";
                note=note.trim();

                if( sh.getProctorSuspensionStatusTypeId()>0 )
                {
                    if( sh.getRemoveUser()!=null )
                    {
                        note += (note.isEmpty() ? "" : "<br />") + "Removed by: " + sh.getRemoveUser().getFullname();

                        if( sh.getRemoveDate()!=null )
                            note += " " + I18nUtils.getFormattedDateTime( getLocale(), sh.getRemoveDate(), getUser().getTimeZone());
                    }
                }

                sb.append( this.getRow(style, I18nUtils.getFormattedDateTime( getLocale(), sh.getCreateDate(), getUser().getTimeZone()), (sh.getProctorUser()==null ? lmsg("g.System") :  sh.getProctorUser().getFullname()), note, false) );
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }



    public Object[] getStandardSuspiciousActivitySection( boolean tog, String rowStyleHeader ) throws Exception
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();
        tog = true;
        out[0] = "";
        out[1] = tog;

        if( getTestEvent().getRemoteProctorEvent()==null )
        {
            if( proctorUtils==null )
                proctorUtils = new ProctorUtils();
            proctorUtils.setupRemoteProctorEvent( getLocale(), getUser().getTimeZone(), getTestEvent() );
        }

        if( getTestEvent().getRemoteProctorEvent()==null )
            return out;

        List<SuspiciousActivity> sal =  getTestEvent().getRemoteProctorEvent().getSuspiciousActivityList();

        if( !getTestKey().getOnlineProctoringType().getIsPremiumWithSuspAct() && sal.isEmpty() )
            return out;

        UserFacade userFacade = null;

        String style = rowStyle1;

        String subtitle = "<span style=\"font-weight:normal\">" + lmsg( "g.SuspiciousActivitySubtitleEm" ) + "</span>";

        sb.append( getRowTitleSubtitle( sal.isEmpty() ? rowStyleHdr : rowStyleHdrRed, lmsg(  "g.SuspiciousActivityTitle", null ), subtitle ) );

        if( sal.isEmpty() )
            sb.append( this.getRow(style, lmsg(  "g.NoSuspActDet", new String[]{RuntimeConstants.getStringValue("baseadmindomain")} ), false ) );

        ListIterator<SuspiciousActivity> iter = sal.listIterator();
        SuspiciousActivity sa;
        SuspiciousActivityType sat;
        String info;
        String instances;
        TimeZone tz = getUser().getTimeZone();
        String dateTime;
        String action;

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

            dateTime = I18nUtils.getFormattedDateTime( getLocale(), sa.getLastUpdate(), tz);
            action = sat.getName(getLocale());

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

            info = "";
            if( sat.getIsKeyPress() )
            {
                if( !SuspiciousKeyCodeType.getValue( sa.getKeyCode() ).getIsUnknown() )
                    info = SuspiciousKeyCodeType.getValue( sa.getKeyCode() ).getName() + (sa.getNote()!=null && !sa.getNote().isBlank() ? " " + sa.getNote() : "");
                else if( sa.getNote()!=null && !sa.getNote().isBlank() )
                        info = sa.getNote();
            }
            else if( sat.getShowTime() )
                info = sa.getSeconds() + " " + lmsg("g.Seconds" );
            else if( sat.getIsMultiFaces() )
                info = lmsg("sat.multiplefaces.detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( sa.getFloatParam1AsInt() )} );
            else if( sat.getIsFaceMissing())
                info = lmsg("sat.candidatefacenotpresent.detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( sa.getFloatParam1AsInt() )} );
            else if( sat.getIsFaceMismatch())
                info = lmsg("sat.facialmismatches.detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( sa.getFloatParam1AsInt() )} );
            else if( sat.getIsAnyIdFaceMismatch())
                info = lmsg( sat.getKey() + ".detail", new String[]{Integer.toString( Math.round(sa.getFloatParam1()*100))} );
            else if( sat.getIsHighPitchYaw())
                info = MessageFactory.getStringMessage( getLocale(), sat.getKey() + ".detail", new String[]{Integer.toString(sa.getIntParam1()), Integer.toString( Math.round( Math.abs(sa.getFloatParam1())))} );
            else if( sat.getIsFrequentPitchYaw())
                info = MessageFactory.getStringMessage( getLocale(), sat.getKey() + ".detail", new String[]{Integer.toString( Math.round( Math.abs(sa.getFloatParam1())))} );
            else if( sat.getIsAltMobile())
                info = MessageFactory.getStringMessage( getLocale(), sat.getKey() + ".detail", new String[]{Integer.toString( sa.getIntParam1())} );
            else if( sat.getSameIpTestEvents() )
            {
                info = MessageFactory.getStringMessage( getLocale(), sat.getKey() + ".detail", null );
                info += "<br />" + MessageFactory.getStringMessage( getLocale(), sat.getKey() + ".detailX", new String[]{getTestEvent().getIpAddress()} );
                info += "<br />" + (new ProctorUtils()).getSameIpUserInfo( getTestEvent().getRemoteProctorEvent(), getLocale(), getUser().getTimeZone(), true );

            }
            else if( sat.getIsAnyNote() )
            {
                if( sat.getIsProctorNote() )
                    info = "(" + lmsg("g.Proctor" ) + ") " + sa.getNote();
                else if( sat.getIsUserNote() )
                    info = "(" + sa.getUser()==null ? "" : sa.getUser().getLastName() + ") " + sa.getNote();
            }

            info += instances;

            sb.append( this.getRow(style, dateTime, action, info, false) );

        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }



    public Object[] getStandardTextAndTitleSection( boolean tog, NonCompetencyItemType ncit, String rowStyleHeader) throws Exception
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        tog = true;

        boolean minQuals = ncit.equals( NonCompetencyItemType.MIN_QUALS);

        boolean includeIt = minQuals ? this.isIncludeMinQuals() : this.isIncludeAppData();

        if( includeIt )
        {
            List<TextAndTitle>  ttList = getTestEvent().getOverallTestEventScore().getTextBasedResponseList( ncit.getTitle(), false );

            if( !ttList.isEmpty() )
            {
                String style = rowStyle1;

                sb.append( getRowTitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, lmsg( includeIt ? "g.MinQuals" : "g.ApplicantInfo" , null ), null, null, null ) );

                // sb.append( getRowSpacer( s0 ) );

                for( TextAndTitle tt : ttList )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    String nm  = StringUtils.replaceStandardEntities( UrlEncodingUtils.decodeKeepPlus(  tt.getTitle(), "UTF8" ) );
                    String txt = StringUtils.replaceStandardEntities( UrlEncodingUtils.decodeKeepPlus(  tt.getText(), "UTF8" ) );

                    if( tt.getUploadedUserFileId() > 0 )
                    {
                        // txt = lmsg( "g.UploadedFile", null );
                        String url = RuntimeConstants.getStringValue( "adminappbasuri" ) + "/duuf/" + tt.getUploadedUserFileId() + "/" + te.getOrgId() + "/" + te.getTestKeyId();

                        txt = "<a href=\"" + url + "\" title=\"" + lmsg( "g.UploadedFileAlt",null ) + "\">" + lmsg( "g.UploadedFileDownloadNow",null ) + "</a>";

                        // this.hasUploadedFiles=true;
                    }

                    if( isIncludeRedFlags() && tt.getRedFlag() )
                        sb.append( getRowColorDot( style, nm, txt, false, ScoreCategoryType.RED ) );

                    else
                        sb.append( getRow( style, nm, txt, false ) );
                 }
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }

    public Object[] getStandardUploadedFilesSection( boolean tog, String rowStyleHeader) throws Exception
    {
        boolean showVideoUrls = !getReportRuleAsBoolean("emlvideoviewoff") && !getTestKey().getHideMediaInReports();

        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        tog = true;

        List<TextAndTitle>  ttList = getTestEvent().getOverallTestEventScore().getTextBasedResponseList( NonCompetencyItemType.FILEUPLOAD.getTitle(), true );
        //List<TextAndTitle>  ttList = getTestEvent().getOverallTestEventScore().getTextBasedResponseList( NonCompetencyItemType.AV_UPLOAD.getTitle(), true );
        //ttList.addAll(  getTestEvent().getOverallTestEventScore().getTextBasedResponseList( NonCompetencyItemType.FILEUPLOAD.getTitle(), true ) );

        if( !ttList.isEmpty() )
        {
            String style = rowStyle1;

            sb.append( getRowTitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, lmsg( "g.UploadedUserFilesTitle" , null ), null, null, null ) );

            // sb.append( getRowSpacer( s0 ) );
            UploadedUserFile uuf;

            // 0=view
            // 1=listen
            // 2=download
            int viewCode = 0;
            String viewKey;
            String thumbUrl;
            FileContentType fct;
            String iconHtml;
            String textHtml;

            String playViewDownloadUrl;
            String thumbFn;
            String summary;

            for( TextAndTitle tt : ttList )
            {
                if( tt.getUploadedUserFileId() <= 0 )
                    continue;

                thumbUrl=null;
                playViewDownloadUrl=null;

                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;

                String nm  = StringUtils.replaceStandardEntities( UrlEncodingUtils.decodeKeepPlus(  tt.getTitle(), "UTF8" ) );
                String txt = StringUtils.replaceStandardEntities( UrlEncodingUtils.decodeKeepPlus(  tt.getText(), "UTF8" ) );

                summary = tt.getString4();
                if( summary!=null && !summary.isBlank() )
                {
                    nm += "<br /><br /><b>" +  lmsg("g.SummaryAI") +  ":</b> " +  StringUtils.replaceStandardEntities( UrlEncodingUtils.decodeKeepPlus(  summary, "UTF8" ));
                }
                
                if( fileUploadFacade==null )
                    fileUploadFacade = FileUploadFacade.getInstance();

                uuf =  fileUploadFacade.getUploadedUserFile( tt.getUploadedUserFileId(), true );

                if( uuf!=null && uuf.getUploadedUserFileStatusType().getAvailable() )
                {
                    fct = FileContentType.getValue( uuf.getFileContentTypeId() );

                    if( fct.getIsVideo() )
                    {
                        if( !getTestKey().getHideMediaInReports() && uuf.getThumbFilename()!=null && !uuf.getThumbFilename().isEmpty() )
                        {
                            thumbFn=uuf.getThumbFilename();
                            if( thumbFn!=null && thumbFn.contains( ".AWSCOUNT." ) )
                                thumbFn = StringUtils.replaceStr( thumbFn, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );
                            else if( thumbFn!=null && thumbFn.contains(  ".IDX." ) )
                                thumbFn = StringUtils.replaceStr( thumbFn, ".IDX." , ".1." );

                            thumbUrl = ReportUtils.getMediaTempUrlSourceLink( tk.getOrgId(), uuf, 1, thumbFn, MediaTempUrlSourceType.FILE_UPLOAD_THUMB );

                            //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                            //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + uuf.getThumbFilename();
                            //else
                            //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + uuf.getThumbFilename();

                            //if( thumbUrl!=null && thumbUrl.contains( ".AWSCOUNT." ) )
                            //    thumbUrl = StringUtils.replaceStr( thumbUrl, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );
                            //else if( thumbUrl!=null && thumbUrl.contains(  ".IDX." ) )
                            //    thumbUrl = StringUtils.replaceStr( thumbUrl, ".IDX." , ".1." );
                        }

                        else
                            thumbUrl = BaseScoreFormatter.VIDEO_ICON_URL;
                    }

                    else if( fct.getIsAudio())
                    {
                        thumbUrl = BaseScoreFormatter.AUDIO_ICON_URL;
                        viewCode = 1;
                    }
                    else if( fct.getIsExcel())
                    {
                        thumbUrl = BaseScoreFormatter.EXCEL_ICON_URL;
                        viewCode = 2;
                    }
                    else if( fct.getIsPpt())
                    {
                        thumbUrl = BaseScoreFormatter.PPT_ICON_URL;
                        viewCode = 2;
                    }
                    else if( fct.getIsPdf())
                    {
                        thumbUrl = BaseScoreFormatter.PDF_ICON_URL;
                        viewCode = 2;
                    }
                    else if( fct.getIsWord())
                    {
                        thumbUrl = BaseScoreFormatter.WORD_ICON_URL;
                        viewCode = 2;
                    }
                    else if( fct.getIsImage())
                    {
                        if( uuf.getFilename()!=null && !uuf.getFilename().isEmpty() )
                        {
                            thumbUrl = ReportUtils.getMediaTempUrlSourceLink(org.getOrgId(), uuf, 0, uuf.getFilename(), MediaTempUrlSourceType.FILE_UPLOAD );
                            //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                            //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + uuf.getFilename();
                            //else
                            //    thumbUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + uuf.getFilename();
                        }

                        else
                            thumbUrl = BaseScoreFormatter.IMAGE_ICON_URL;
                    }
                    else
                    {
                        thumbUrl = BaseScoreFormatter.GENFILE_ICON_URL;
                        viewCode = 2;
                    }

                    if( viewCode==1 )
                        viewKey = "g.Click2ListenC";
                    else if( viewCode==2 )
                        viewKey = "g.Click2DownloadC";
                    else
                        viewKey = "g.Click2ViewC";

                    if( fct.getIsAudio() || fct.getIsVideo() )
                    {
                        if( !showVideoUrls )
                            playViewDownloadUrl = null;

                        else
                        {
                            playViewDownloadUrl = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + "/ta/uavpb/" + getTestEvent().getTestEventId() + "/"  + uuf.getUploadedUserFileId();

                            if( addLimitedAccessLinkInfo==1 )
                            {
                                if( playViewDownloadUrl.indexOf("?")<0 )
                                    playViewDownloadUrl += "?";
                                else
                                    playViewDownloadUrl+="&";

                                playViewDownloadUrl += "lid=[LIMITEDACCESSLINKIDENC]";
                            }
                        }
                    }
                        // playViewDownloadUrl = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + "/ta/misc/av/avpb-entry.xhtml?teid=" + EncryptUtils.urlSafeEncrypt( getTestEvent().getTestEventId() ) + "&uufid="  + EncryptUtils.urlSafeEncrypt( uuf.getUploadedUserFileId() );
                    else
                        playViewDownloadUrl = RuntimeConstants.getStringValue( "adminappbasuri" ) + "/duuf/" + tt.getUploadedUserFileId() + "/" + getTestEvent().getOrgId() + "/" + getTestEvent().getTestKeyId();



                    if( fct.getIsAudio() || fct.getIsVideo() )
                    {
                        if( playViewDownloadUrl != null && !playViewDownloadUrl.isEmpty() )
                            iconHtml = "<a href=\"" + playViewDownloadUrl + "\" target=\"_blank\" title=\"" + lmsg( viewKey ) + "\"><img src=\"" + thumbUrl + "\" alt=\"Icon Graphic to play or download an uploaded file\" style=\"max-width:88px;max-height:72px\"/></a>";
                        else
                            iconHtml = "<img src=\"" + thumbUrl + "\" alt=\"Icon Graphic to play or download an uploaded file\" style=\"max-width:88px;max-height:72px\"/>";
                    }
                    else
                        iconHtml = "<a href=\"" + playViewDownloadUrl + "\" target=\"_blank\" title=\"" + lmsg( viewKey ) + "\"><img src=\"" + thumbUrl + "\" alt=\"Icon Graphic to play or download an uploaded file\" style=\"max-width:62px;max-height:52px\"/></a>";


                    if( playViewDownloadUrl != null && !playViewDownloadUrl.isEmpty() )
                        textHtml = "<div style=\"vertical-align:middle;padding-top:8px\">" +
                                   "<a href=\"" + playViewDownloadUrl + "\" target=\"_blank\" title=\"" + lmsg( viewKey ) + "\">" + playViewDownloadUrl + "</a>" +
                                   "</div>";
                    else
                        textHtml = "";

                    sb.append( getRow( style, nm, iconHtml, textHtml,  false  ) );
                }

                else if( txt!=null && !txt.isEmpty() )
                    sb.append( getRow( style, nm, txt, false ) );
             }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    public Object[] getStandardCompetencyTaskTextAndTitleSection( boolean tog, boolean competency, String rowStyleHeader) throws Exception
    {
        // if( 1==1 && competency )
        //     return new Object[]{"",false};

        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeIt = competency ?  isIncludeCompetencyScores() : isIncludeTaskScores();

        tog = true;

        if( includeIt )
        {
            SimCompetencyClass scc;

            List<TestEventScore> tesList = getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            List<TestEventScore> tesList2 = new ArrayList<>();

            for( TestEventScore tes : tesList )
            {
                scc = SimCompetencyClass.getValue(tes.getSimCompetencyClassId());

                if( !tes.getHasTextResponses())
                    continue;

                if( competency && scc.equals( SimCompetencyClass.SCOREDIMAGEUPLOAD ) )
                    continue;

                // if this is the case these responses were shown elsewhere.
                if( scc.isUnscored() && tes.getIntParam2()>0 && SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowItemScoresInReports() )
                    continue;

                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                if( competency && (scc.getIsDirectCompetency() || scc.getIsAggregate()) )
                    tesList2.add( tes );

                if( !competency && scc.getIsTask() )
                    tesList2.add( tes );
            }

            // LogService.logIt( "BaseScoreFormatter.getStandardCompetencyTaskTextAndTitleSection() AAA teslList2=" + tesList2.size() );


            Collections.sort( tesList2, new DisplayOrderComparator() );  // new TESNameComparator() );

            if( tesList2.size() > 0 )
            {
                String style = rowStyle1;

                sb.append( getRowTitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, lmsg( competency ?  "g.CompetencyExtraResponses" :  "g.TaskExtraResponses" , null ), null, null, null ) );

                for( TestEventScore tes : tesList2 )
                {
                    List<TextAndTitle>  ttList = tes.parseTextBasedResponseList( tes.getTextbasedResponses(), true );

                    if( !ttList.isEmpty() )
                    {
                        sb.append( getRowTitle( rowStyle1, tes.getName(), null, null, null ) );

                        tog = true;

                        // sb.append( getRowSpacer( s0 ) );

                        for( TextAndTitle tt : ttList )
                        {
                            tog = !tog;
                            style = tog ? rowStyle1 : rowStyle2;

                            String nm  = StringUtils.replaceStandardEntities( UrlEncodingUtils.decodeKeepPlus(  tt.getTitle(), "UTF8" ) );
                            String txt = StringUtils.replaceStandardEntities( UrlEncodingUtils.decodeKeepPlus(  tt.getText(), "UTF8" ) );

                           // LogService.logIt( "BaseScoreFormatter.getStandardCompetencyTaskTextAndTitleSection() CCC title=" + tt.getTitle() + ", ufid=" + tt.getUploadedUserFileId() + ", text=" + tt.getText() );


                            if( ( tt.getUploadedUserFileId() > 0 && tes.getSimCompetencyClassId()!=SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() && tes.getSimCompetencyClassId()!=SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() )  )//|| (txt != null && txt.startsWith( "UPLOAD" ) ) )
                            {
                                // txt = lmsg( "g.UploadedFile", null );
                                String url = RuntimeConstants.getStringValue( "adminappbasuri" ) + "/duuf/" + tt.getUploadedUserFileId() + "/" + te.getOrgId() + "/" + te.getTestKeyId();

                                txt = "<a href=\"" + url + "\" title=\"" + lmsg( "g.UploadedFileAlt",null ) + "\">" + lmsg( "g.UploadedFileDownloadNow",null ) + "</a>";

                                // hasUploadedFiles=true;
                                // LogService.logIt( "BaseScoreFormatter.getStandardCompetencyTaskTextAndTitleSection() HasUploadedFiles=true! " + tt.getUploadedUserFileId() );
                            }

                            if( tes.getSimCompetencyClass().isScoredChat() )
                            {
                                sb.append( getChatResponsesRow( style, nm, txt ) );
                            }

                            else if( isIncludeRedFlags() && tt.getRedFlag() )
                                sb.append( getRowColorDot( style, nm, txt, false, ScoreCategoryType.RED ) );

                            else
                                sb.append( getRow( style, nm, txt, false ) );
                         }
                    }
                }
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    public Object[] getStandardHRANotesSection( boolean tog, String rowStyleHeader) throws Exception
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        String style = rowStyle1;



        sb.append( getRowTitle( rowStyleHeader==null || rowStyleHeader.isEmpty() ? rowStyleHdr : rowStyleHeader, "HR Avatar " + lmsg( "g.NotesInternalUse" , null ), null, null, null ) );

        if( te.getProduct()!=null && te.getProduct().getProductType().getUsesSimDescriptor() && te.getProduct().getTempDate()!=null )
        {
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            String suStr = I18nUtils.getFormattedDate( locale, te.getProduct().getTempDate(), DateFormat.SHORT );
            sb.append( getRow( style, lmsg("g.LastScoringUpdate" ) + ":", suStr , false ) );
        }

        tog = !tog;
        style = tog ? rowStyle1 : rowStyle2;
        sb.append( getRow( style, "", "tk: " + this.tk.getTestKeyId() + ", te: " + te.getTestEventId() +", p: " + tk.getProductId() + ", s: " + te.getSimId() , false ) );

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    public String getPostCandidateContactStr()
    {
        if( org==null )
            org = tk.getOrg();
        if( org==null )
            return "";
        String cs = org.getPostTestContactStr();
        if( cs==null || cs.isBlank() )
            return "";
        String ss = cs.toLowerCase();
        if( !ss.startsWith("<div") && !ss.startsWith("<p") )
            cs = "<p>" + cs + "</p>";
        return cs;
    }



    public boolean hasProfile()
    {
        return te!=null && te.getProfile() != null;
    }

    public float[] getOverallProfileData()
    {
        if( !hasProfile() )
            return null;

        return te.getProfile().getOverallProfileData();
    }


    public float[] getProfileEntryData( String name, String nameEnglish)
    {
        if( !hasProfile() )
            return null;

        return te.getProfile().getProfileEntryData(name, nameEnglish, false);
    }



    public boolean useRatingAndColors()
    {
        return true;
    }

    public Locale getLocale() {
        return locale;
    }

    public String[] getParams()
    {
        return this.params;
    }

    public TestKey getTestKey() {
        return tk;
    }

    public TestEvent getTestEvent() {
        return te;
    }

    public BatteryScore getBatteryScore() {
        return bs;
    }

    public TestEventScore getOverallTes() {
        return overallTes;
    }

    public Report getReport() {
        return report;
    }

    public Org getOrg() {
        return org;
    }

    public boolean isIncludeOverall() {
        return includeOverall;
    }

    public boolean isIncludeCompetencyScores() {
        return includeCompetencyScores;
    }

    public boolean isIncludeTaskScores() {
        return includeTaskScores;
    }

    public boolean isIncludeBiodataScores() {
        return includeBiodataScores;
    }

    public boolean isIncludeNumeric() {
        return includeNumeric;
    }

    public boolean isIncludeCategory() {
        return includeCategory;
    }

    public boolean isIncludeColors() {
        return this.includeColors;
    }



    public boolean isIncludeInterview() {
        return includeInterview;
    }

    public boolean isIncludeCompetencyDescriptions() {
        return includeCompetencyDescriptions;
    }

    public boolean isIncludeEducTypeDesc() {
        return includeEducTypeDesc;
    }

    public boolean isIncludeTrainingTypeDesc() {
        return includeTrainingTypeDesc;
    }

    public boolean isIncludeExperTypeDesc() {
        return includeExperTypeDesc;
    }

    public boolean isIncludeTaskInterest() {
        return includeTaskInterest;
    }

    public boolean isIncludeTaskExperience() {
        return includeTaskExper;
    }

    public User getUser() {
        return u;
    }

    public boolean isAnonymous() {
        return anon;
    }

    public boolean isBattery() {
        return batt;
    }

    public ScoreFormatType getScoreFormatType() {
        return scoreFormatType;
    }

    public ScoreCategoryType getScoreCategoryType() {
        return scoreCategoryType;
    }

    public int getScoreCat() {
        return scoreCat;
    }

    public float getScore() {
        return score;
    }

    public boolean isIncludeNorms() {
        return includeNorms;
    }

    public boolean isIncludeOverview() {
        return includeOverview;
    }

    public boolean isIncludeIbmInsight() {
        return includeIbmInsight;
    }

    public boolean isIncludeWritingSamples() {
        return includeWritingSamples;
    }

    public boolean isIncludeAppData() {
        return includeAppData;
    }

    public boolean isIncludeMinQuals() {
        return includeMinQuals;
    }

    public boolean isIncludeRedFlags() {
        return includeRedFlags;
    }

    public boolean isIncludeSubcategoryNumeric() {
        return includeSubcategoryNumeric;
    }

    public boolean isIncludeSubcategoryNorms() {
        return includeSubcategoryNorms;
    }

    public boolean isIncludeSubcategoryCategory() {
        return includeSubcategoryCategory;
    }
    public boolean isIncludeSubcategoryColors() {
        return this.includeSubcategoryColors;
    }


}
