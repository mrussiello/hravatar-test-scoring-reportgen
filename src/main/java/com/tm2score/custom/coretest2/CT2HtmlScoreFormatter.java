/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.av.AvEventFacade;
import com.tm2score.battery.BatteryScoringUtils;
import com.tm2score.custom.coretest2.cefr.CefrScoreType;
import com.tm2score.custom.coretest2.cefr.CefrUtils;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.file.MediaTempUrlSourceType;
import com.tm2score.purchase.ProductType;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.scorer.BaseTestEventScorer;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.util.StringUtils;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class CT2HtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public static String chartMarkerImgUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_1409433986163.png";
    public static String chartAxisImgUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_1409434017271.png";

    AvEventFacade avEventFacade = null;
    FileUploadFacade fileUploadFacade = null;

    public boolean useOverallRawScore = false;


    public CT2HtmlScoreFormatter()
    {
        super();

        this.MIN_COUNT_FOR_PERCENTILE = 100;
        // this.SCORE_PRECISION = 0;

        rowStyleHdr = " style=\"background-color:#0077cc;vertical-align:top;color:white\"";
        rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
        rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
        rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";
    }


    @Override
    public String getTextContent() throws Exception
    {
        useOverallRawScore = ScoreUtils.getIncludeRawOverallScore(org, te); // getIncludeRawOverallScore();

        if( getReport()==null )
        {
            if( getTestEvent()!=null && getTestEvent().getReport()!=null )
                report = getTestEvent().getReport();
            else
            {

            }
        }

        int scrDigits = getReport().getIntParam2() >= 0 ? getReport().getIntParam2() : getTestEvent().getScorePrecisionDigits();

        if( useOverallRawScore && params!=null )
        {
            TestEventScore tes = te.getOverallTestEventScore();
            params[5]= I18nUtils.getFormattedNumber( getLocale(), tes.getOverallRawScoreToShow(), scrDigits ); //   Integer.toString( Math.round(tes.getOverallRawScoreToShow() ) ); ;
            ScoreCategoryType sct = ScoreCategoryType.getScoreCategoryTypeForRawScore( ScoreFormatType.NUMERIC_0_TO_100, tes.getOverallRawScoreToShow()) ;
            params[6] = sct.getName( locale );
        }

        return getStandardTextContent();
    }



    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            // LogService.logIt( "CoreTestHtmlScoreFormatter.getEmailContent() STARTING" );

            useOverallRawScore = ScoreUtils.getIncludeRawOverallScore(org, te); // getIncludeRawOverallScore();
            if( useOverallRawScore && params!=null )
            {
                TestEventScore tes = te.getOverallTestEventScore();
                params[5]=Integer.toString( Math.round( tes.getOverallRawScoreToShow() ) ); ;
                ScoreCategoryType sct = ScoreCategoryType.getScoreCategoryTypeForRawScore( ScoreFormatType.NUMERIC_0_TO_100, tes.getOverallRawScoreToShow()) ;
                params[6] = sct.getName( locale );
            }

            StringBuilder sb = new StringBuilder();

            boolean isBatt = getTestKey().getBatteryId()>0 && getTestKey().getTestEventList().size() >1 ;


            // Header Section
            Object[] out = getStandardHeaderSection( tog, includeTop, topNoteHtml, isBatt ? "g.CoreTestAdminScoringCompleteMsgBattery" : "g.CoreTestAdminScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getRowSpacer( rowStyle0 ) );
            }


            // not batt.
            if( getTestEvent()!=null )
            {
                if( isBatt )
                {
                    int counter = 1;
                    for( TestEvent ev : tk.getTestEventList() )
                    {
                        if( te.getTestEventId()==ev.getTestEventId() )
                            break;
                        counter++;
                    }

                    String label = lmsg(  "g.TestX" , new String[]{Integer.toString(counter)} );
                    sb.append( getRowTitle( rowStyleHdr, label, null, null, null ) );

                    String style = tog ? rowStyle1 : rowStyle2;
                    String value = getTestEvent().getProduct().getName();
                    //label = lmsg(  "g.TestC" , null );
                    if( value != null && value.length() > 0 )
                        sb.append( getRowTitle( style, " &nbsp;&nbsp;&nbsp;" + value, null, null, null ) );
                        // sb.append( getRowTitle( style, label + " " + value, null, null, null ) );
                }

                // LogService.logIt( "CoreTestHtmlScoreFormatter.getEmailContent() SSSS  isBatt=" + isBatt + ", isIncludeOverall()=" + isIncludeOverall() + ", testEventId=" + (this.getTestEvent()==null ? "null" : this.getTestEvent().getTestEventId()) + ", otes="  + (this.getOverallTes()==null ? "null" : "not null"));
                if( isIncludeOverall() && !getReportRuleAsBoolean( "ovroff" )  )
                {
                    tog = !tog;
                    out = getStandardOverallScoreSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


                // Report Section
                if( !getReportRuleAsBoolean( "rptdwnldoff" ) && !getReportRuleAsBoolean( "emlrptdwnldoff" ) )
                {
                    out = getStandardReportSection(tog, false, null );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }
                }

                // To turn off competencies in email, must have both set to off.
                boolean hideCompDet = getReportRuleAsBoolean("cmptysumoff") && getReportRuleAsBoolean( "hidecompetencydetail" );

                if( !hideCompDet )
                {
                    out = getStandardResponseRatingSummarySection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    // Competency Section
                    for( int i=1; i<=5; i++ )
                    {
                        out = getStandardCustomSection( tog, i );
                        temp = (String) out[0];
                        if( !temp.isEmpty() )
                        {
                            sb.append( temp );
                            tog = ( (Boolean) out[1]);
                            // if( !isBatt )
                            sb.append( getRowSpacer( rowStyle0 ) );
                        }
                    }

                    out = getStandardAbilitiesSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    // Competency Section
                    out = getStandardKsSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    // Intersts Section
                    out = getStandardInterestsSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    out = getStandardAimsSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    out = getStandardEqSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    // Biodata Section
                    out = getBiodataCompetencyTaskSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    out = getStandardAiSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }
                }

                out = getStandardTopInterviewQsSection(tog, null);
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }



                // Task Section
                out = getStandardTaskSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


                // Writing Sample Section
                out = getStandardWritingSampleSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // IBM Insight Section
                out = getStandardIbmInsightScoresSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }



                // Identity Image Capture Section
                out = getStandardImageCaptureSection(tog, null, includeTop );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Suspicious Activity Section
                out = getStandardSuspiciousActivitySection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Suspension History Section
                out = getStandardSuspensionHistorySection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Proctor Certifications Section
                out = getStandardProctorCertificationsSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


                // Min Quals Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.MIN_QUALS, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Applicant Data Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.APPLICANT_INFO, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Text Section
                out = getStandardCompetencyTaskTextAndTitleSection(tog, true, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Task Text Section
                //out = getStandardCompetencyTaskTextAndTitleSection(tog, false, null );
                //temp = (String) out[0];
                //if( !temp.isEmpty() )
                //{
                //    sb.append( temp );
                //    tog = ( (Boolean) out[1]).booleanValue();
                //    if( !isBatt )
                //        sb.append( getRowSpacer( rowStyle0 ) );
                //}

                // Has Uploaded Files Section
                out = getStandardUploadedFilesSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                if( isIncludeOverall() && !getReportRuleAsBoolean( "ovroff" )  )
                {
                    out = getTrailingRiskFactorsSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Item Responses Section
                out = getStandardItemResponsesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Notes section
                // Has Uploaded Files Section
                out = getStandardHRANotesSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "CoreTestHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }




    @Override
    public Object[] getStandardOverallScoreSection( boolean toggle )
    {
        Object[] out = new Object[2];
        out[0] = "";
        out[1] = toggle;

        boolean tog = false; // toggle;

        try
        {
            StringBuilder sb = new StringBuilder();

            tog = false;

            TestEventScore otes = getOverallTes();

            if( !batt && otes==null )
                LogService.logIt( "CT2HtmlScoreFormatter.getStandardOverallScoreSection() OTES is null! Cannot create overall score section. "  + ( te==null ? "te is null" : te.toString()  ) );

            if( isIncludeOverall() && otes!=null )
            {
                boolean showStarRating = isIncludeCategory() && getTestEvent().getScoreCategoryType().hasColor();
                boolean showColorGraph = isIncludeColors() && !getReportRuleAsBoolean("ovrgrphoff");
                boolean showNumeric = isIncludeNumeric() && !getReportRuleAsBoolean("ovrnumoff");
                boolean showPercentiles =  isIncludeNorms() && !getReportRuleAsBoolean("skipcomparisonsection");
                boolean showScoreText = !getReportRuleAsBoolean("ovrscrtxtoff");
                boolean useScoreTextAsNumScore = getReportRuleAsBoolean( "ovrscrtxtasnum" );

                if( !showNumeric && !showColorGraph && !showStarRating && !showScoreText && !showPercentiles )
                    return out;

                boolean useSolidBarGraphs = showColorGraph && getReportRuleAsString( "overallcoloriconasgraph" )!=null && !getReportRuleAsString( "overallcoloriconasgraph" ).equals( "0");

                List<TextAndTitle> ct3RiskFactors = ScoreFormatUtils.getTextTitleList( getTestEvent() , CT3Constants.CT3RISKFACTORS );

                ct3RiskFactors.addAll( ScoreFormatUtils.getTextTitleList(getTestEvent(), Constants.STD_RISKFACTORSKEY ) );

                boolean overallTimeout = otes.getIntParam1()>0;

                String earlyExitStr = StringUtils.getBracketedArtifactFromString( getTestEvent().getTextStr1(), Constants.EARLYEXITBATTERYKEY );
                if( earlyExitStr!=null )
                    earlyExitStr = BatteryScoringUtils.getEarlyExitWarningMessage( getLocale(), earlyExitStr );

                if( earlyExitStr!=null )
                {
                    sb.append( getRowTitle( rowStyleHdrRedTxt, lmsg(  "g.NoteC" , null ), earlyExitStr, null, null ) );
                }


                sb.append( getRowTitle( rowStyleHdr,
                                        lmsg(  "g.OverallResults" , null ),
                                        showNumeric ? lmsg(  "g.Score" , null ) : "",
                                        showColorGraph ? lmsg(  "g.Interpretation" ) : "",
                                        showStarRating ? lmsg(  this.useRatingAndColors() ? "g.Rating" : "g.JobMatch" , null ) : null
                                       ) );


                String style = tog ? rowStyle1 : rowStyle2;
                String value; //  =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() ) : "";

                int scrDigits = getReport().getIntParam2() >= 0 ? getReport().getIntParam2() : getTestEvent().getScorePrecisionDigits();

                String overallScoreTitle = getReport().getStrParam4()!=null && !getReport().getStrParam4().isEmpty() ? getReport().getStrParam4() + ":"  :  lmsg( useOverallRawScore ? "g.ScoreOverallRawC" : "g.ScoreC" );

                if( useOverallRawScore )
                    value =  showNumeric ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow(), scrDigits ) : "";
                else
                    value = showNumeric ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), scrDigits ) : "";


                String cefrLevel = StringUtils.getBracketedArtifactFromString( otes.getTextParam1(), Constants.CEFRLEVEL);
                if( cefrLevel!=null )
                {
                    if( CefrScoreType.getFromText(cefrLevel).equals(CefrScoreType.UNKNOWN))
                        cefrLevel=null;
                }


                String scoreText=null; //  = otes.getScoreText();

                if( !useScoreTextAsNumScore && !showScoreText )
                    scoreText="";

                else if( useOverallRawScore )
                {
                    if( te.getSimXmlObj()==null )
                    {
                        ReportUtils ru = new ReportUtils();

                        ru.loadTestEventSimXmlObject(te);

                        scoreText = BaseTestEventScorer.getScoreTextForOverallScore( ScoreColorSchemeType.FIVECOLOR, otes.getOverallRawScoreToShow(), te );
                    }
                }
                else
                    scoreText = otes.getScoreText();


                if( useScoreTextAsNumScore && showNumeric && scoreText!=null )
                {
                    value = ReportUtils.getScoreValueFromStr(scoreText);

                    if( value==null )
                        value="";
                    // value = scoreText;
                }

                if( !showScoreText || getReport().getIncludeScoreText() != 1 )
                    scoreText = "";

                else
                {
                    scoreText = ReportUtils.getScoreTextFromStr(scoreText);

                    if( cefrLevel!=null )
                    {
                        if( scoreText==null )
                            scoreText="";
                        String stub = (String) CefrUtils.getCefrScoreInfoForOverall(getTestEvent(), getTestEvent().getTestEventScoreList())[1];

                        String cefrScoreText = CefrUtils.getCefrScoreDescription(getLocale(), CefrScoreType.getFromText(cefrLevel), stub); // StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRLEVELTEXT);
                        if( cefrScoreText!=null && !cefrScoreText.isBlank() )
                            scoreText = lmsg("g.CefrEquivScoreText") + " " + cefrScoreText + (scoreText.isBlank() ? "" : "\n\n" + lmsg("g.GeneralScoreText") + " " ) +  scoreText;
                    }

                }


                //if(  )
                //{
                //    String rs = I18nUtils.getFormattedNumber( getLocale(), getTestEvent().getOverallTestEventScore().getRawScore(), getTestEvent().getScorePrecisionDigits() );

                //    if( rs!=null )
                //        // value = lmsg( "g.RawScoreOvrRpt", new String[]{value, rs} );
                //}

                // value2 = getTestEvent().getScoreCategoryType().getName(locale);
                String label = overallScoreTitle; // lmsg(  useOverallRawScore ? "g.ScoreOverallRawC" : "g.ScoreC" , null );

                ScoreCategoryType sct = useOverallRawScore ?  ScoreCategoryType.getScoreCategoryTypeForRawScore(ScoreFormatType.NUMERIC_0_TO_100,getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow()  ) : getScoreCategoryType();

                if( showNumeric && ( showStarRating || showColorGraph ) )
                {
                    // TestEventScore tes = getOverallTes();

                    if( hasProfile() )
                        otes.setProfileBoundaries( getOverallProfileData() );

                    //if( otes != null )
                    //    LogService.logIt( "CT2HtmlScoreFormatter.getStandardOverallScoreSection() " + hasProfile() + ", tes.getProfileBoundaries()=" + (otes.getProfileBoundaries()!=null));


                    //if( tes.getProfileBoundaries()==null )
                    //    sb.append( getRowColorDot( style, label, value, false, getScoreCategoryType() ) );
                    //else
                    sb.append(getRowWithColorGraphAndCategoryStars(style, label, value, false,  sct, otes, showColorGraph, useSolidBarGraphs, showStarRating, useOverallRawScore) );
                }

                else if( showNumeric  )
                    sb.append( getRow( style, label, value, false ) );

                else if( showColorGraph )
                {
                    // TestEventScore tes = getOverallTes();

                    sb.append(getRowWithColorGraphAndCategoryStars(style, label, "", false, sct, otes, showColorGraph, useSolidBarGraphs, showStarRating, useOverallRawScore) );
                }

                else if( showStarRating )
                    sb.append( getRowColorDot( style, label, "", false, sct ) );

                if( cefrLevel!=null )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    label = lmsg( "g.CefrEquivScore" );
                    value = CefrScoreType.getFromText(cefrLevel).getName(getLocale());
                    sb.append( getRow( style, label, value, false ) );
                }

                // LogService.logIt( "CT2HtmlScoreFormatter.getStandardOverallScoreSection() overallTimeout="  + overallTimeout + ", showPercentiles=" + showPercentiles + ", Otes hasValidnorms=" + otes.getHasValidNorms() + " otes.getHasValidOverallNorm()=" + otes.getHasValidOverallNorm() );
                if( overallTimeout )
                {
                    String style2 = style.substring(0, style.lastIndexOf("\"")-1) + ";color:red\" ";

                    sb.append( getRow( style2, "", lmsg("g.OvrTimeout"), true ) );
                }

                if( showPercentiles )
                {
                    if( otes.getHasValidNorms() || (otes.getHasValidOverallZScoreNorm() && te.getProduct()!=null && te.getProduct().getConsumerProductType().getIsJobSpecific()) )
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
                            value =  getNormString( getTestEvent().getCountryPercentile(), getTestEvent().getCountryPercentileCount(), 0 );
                            // label = lmsg(  "g.XPercentileC" , new String[] {this.getCountryName( getUser().getCountryCode())} );
                            label = lmsg(  "g.XPercentileC" , new String[] {this.getCountryName( getTestEvent().getPercentileCountry()!=null && !getTestEvent().getPercentileCountry().isEmpty() ? getTestEvent().getPercentileCountry() : getTestEvent().getIpCountry() )} );
                            sb.append( getRow( style, label, value, false ) );
                        }

                        if( otes.getHasValidAccountNorm() )
                        {
                            tog = !tog;
                            style = tog ? rowStyle1 : rowStyle2;
                            value = getNormString( getTestEvent().getAccountPercentile(), getTestEvent().getAccountPercentileCount(), 0 );
                            label = lmsg(  "g.XPercentileC" , new String[] {getOrg().getName()} );
                            sb.append( getRow( style, label, value, false ) );
                        }
                    }

                    else if( !getReportRuleAsBoolean( "lowpctlcntwrngoff" ) )
                    {
                            tog = !tog;
                            style = tog ? rowStyle1 : rowStyle2;
                            value = lmsg( "g.InsufficientDataForComparisons" );
                            label = lmsg(  "g.PercentileC" , new String[] {getOrg().getName()} );
                            sb.append( getRow( style, label, value, false ) );
                    }

                }



                if( scoreText !=null && !scoreText.isEmpty() )
                {
                    tog = !tog;
                    label = "";
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  StringUtils.replaceStandardEntities( scoreText );
                    sb.append( getRow( style, label, value, false ) );
                }

               //  String hasRule1 = getReportRule( "ct3risktoend" );

                boolean includeRiskFactorsAfterDetail = getReportRuleAsBoolean( "ct3risktoend" );

                // LogService.logIt( "BaseCT2ReportTemplate.getReportInfoHeader() includeRiskFactorsAfterDetail=" + includeRiskFactorsAfterDetail );

                // String hasRule2 = getReportRule( "ct3riskremove" );

                boolean includeRiskFactorsAnywhere = !getReportRuleAsBoolean( "ct3riskremove" ); //  hasRule2 == null || !hasRule2.equals( "1" );

                boolean showRiskFactors = includeRiskFactorsAnywhere && !includeRiskFactorsAfterDetail && ct3RiskFactors != null && ct3RiskFactors.size()>0 && report.getIntParam1()==0;


                if( showRiskFactors )
                {
                    label = "<span style=\"font-weight:bold;color:red\">" + StringUtils.replaceStandardEntities( lmsg( "g.CT3RiskFactorsHdrTitleC" ) ) + "&#160;</span>";

                    String ctrHtml = ""; // StringUtils.replaceStandardEntities( lmsg( "g.CT3RiskFactorsHdr" ) ) + "<br />\n";

                    ctrHtml += "<ul style=\"margin:0;padding:3px 0px 0px 25px\">\n";

                    String rftxt;

                    for( TextAndTitle tt : ct3RiskFactors )
                    {
                        rftxt = tt.getText();

                        if( rftxt.indexOf( "[FACET]" ) >= 0 )
                            rftxt = rftxt.substring(0,rftxt.indexOf( "[FACET]" ) );

                        ctrHtml += "<li>" + StringUtils.replaceStandardEntities( rftxt ) + "</li>";
                    }

                    ctrHtml += "</ul>\n";

                    ctrHtml += lmsg( "g.CT3RiskFactorsFtrEml" );

                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style,label, ctrHtml, false ) );
                }
            }

            out[0] = sb.toString();
            out[1] = tog;
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2HtmlScoreFormatter.getStandardOverallScoreSection() " );

            out[0] = "";
            out[1] = toggle;
            return out;
        }
    }

    public Object[] getTrailingRiskFactorsSection( boolean toggle )
    {
        Object[] out = new Object[2];

        boolean tog = toggle;

        try
        {
            if( getReportRuleAsBoolean( "ct3riskremove" ) )
            {
                out[0] = "";
                out[1] = toggle;
                return out;
            }

            StringBuilder sb = new StringBuilder();

            tog = false;

            if( isIncludeOverall() )
            {
                List<TextAndTitle> ct3RiskFactors = ScoreFormatUtils.getTextTitleList( getTestEvent() , CT3Constants.CT3RISKFACTORS );

                ct3RiskFactors.addAll( ScoreFormatUtils.getTextTitleList(getTestEvent(), Constants.STD_RISKFACTORSKEY ) );

                //TestEventScore otes = getOverallTes();

                // title Row

                String style = tog ? rowStyle1 : rowStyle2;
                //String value =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() ) : "";

                // value2 = getTestEvent().getScoreCategoryType().getName(locale);
                String label = lmsg(  "g.ScoreC" , null );

                String hasRule1 = getReportRuleAsString( "ct3risktoend" );

                boolean includeRiskFactorsAfterDetail = hasRule1 != null && hasRule1.equals( "1" );

                String hasRule2 = getReportRuleAsString( "ct3riskremove" );

                boolean includeRiskFactorsAnywhere = hasRule2 == null || !hasRule2.equals( "1" );

                // LogService.logIt( "BaseCT2ReportTemplate.getReportInfoHeader() includeRiskFactorsAfterDetail=" + includeRiskFactorsAfterDetail );

                boolean showRiskFactors =  includeRiskFactorsAnywhere && includeRiskFactorsAfterDetail && ct3RiskFactors != null && ct3RiskFactors.size()>0 && report.getIntParam1()==0;

                if( showRiskFactors )
                {
                    sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.RiskFactors" , null ), null, null, null ) );

                    label = "<span style=\"font-weight:bold;color:red\">" + StringUtils.replaceStandardEntities( lmsg( "g.CT3RiskFactorsHdrTitleC" ) ) + "&#160;</span>";

                    String ctrHtml = ""; // StringUtils.replaceStandardEntities( lmsg( "g.CT3RiskFactorsHdr" ) ) + "<br />\n";

                    ctrHtml += "<ul style=\"margin:0;padding:3px 0px 0px 25px\">\n";

                    String rftxt;

                    for( TextAndTitle tt : ct3RiskFactors )
                    {
                        rftxt = tt.getText();

                        if( rftxt.indexOf( "[FACET]" ) >= 0 )
                            rftxt = rftxt.substring(0,rftxt.indexOf( "[FACET]" ) );

                        ctrHtml += "<li>" + StringUtils.replaceStandardEntities( rftxt ) + "</li>";
                    }

                    ctrHtml += "</ul>\n";

                    ctrHtml += lmsg( "g.CT3RiskFactorsFtrEml" );

                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style,label, ctrHtml, false ) );
                }
            }

            out[0] = sb.toString();
            out[1] = tog;
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2HtmlScoreFormatter.getTrailingRiskFactorsSection() " );

            out[0] = "";
            out[1] = toggle;
            return out;
        }
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
     *      7=Voice Skills
     *      8=Scored Audio (Voice Sample)
     *      9=Scored AV (Uploaded AV)
     *      10=AI-Derived
     *      11=Interests
     *      21 - 25 Custom
     *
     * @param tog
     * @param typeId
     * @return
     */
    @Override
    protected Object[] getCompetencyTaskSection( boolean tog, int typeId )
    {
        boolean showStarRating = this.isIncludeSubcategoryCategory();
        boolean showColorGraph = this.isIncludeSubcategoryColors() && !getReportRuleAsBoolean( "cmptygrphoff" );
        boolean showNumeric = isIncludeSubcategoryNumeric() && !getReportRuleAsBoolean( "cmptynumoff" );
        //boolean metas = !getReportRuleAsBoolean( "hidecaveats" ) && !getReportRuleAsBoolean( "cmptymetasoff" );
        // boolean useScoreTextAsNumScore = getReportRuleAsBoolean( "cmptyscrtxtasnum" );


        // boolean showCaveats = !getReportRuleAsBoolean( "hidecaveats" ) && !getReportRuleAsBoolean( "cmptymetasoff" ) && !getReportRuleAsBoolean("hidecompetencydetail");
        boolean showTopics = !getReportRuleAsBoolean( "cmptytopicsoff" ) && !getReportRuleAsBoolean("hidecompetencydetail");

        boolean useSolidBarGraphs = showColorGraph && getReportRuleAsString( "competencycoloriconasgraph" )!=null && !getReportRuleAsString( "competencycoloriconasgraph" ).equals( "0");

        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeIt = false;

        tog = true;

        if( typeId == 0 || typeId==3 || typeId==4 || typeId==5 || typeId==6 || typeId==7 || typeId==10 || typeId==11 || (typeId>=21 && typeId<=25) )
            includeIt = isIncludeCompetencyScores();

        else if( typeId == 1 )
            includeIt = isIncludeTaskScores();

        else if( typeId == 2 )
            includeIt = isIncludeBiodataScores();

        else if ( typeId==8 || typeId==9 )
           includeIt = !getReportRuleAsBoolean("hideavsampleinfoforall");

        // Only include 7 for IVR Tests (Legacy)
        if( typeId==7 && te.getProductTypeId()!=ProductType.IVR.getProductTypeId() )
            includeIt = false;

        // LogService.logIt( "CT2HtmlScoreFormatter.getCompetencyTaskSection() includeIt=" + includeIt + ", showNumeric=" + showNumeric + ", showColorGraph=" + showColorGraph );

        if( includeIt )
        {
            SimCompetencyClass scc;

            List<TestEventScore> tesList = getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            List<TestEventScore> tesList2 = new ArrayList<>();

            for( TestEventScore tes : tesList )
            {
                // Skip competencies or task-competencies that were not automatically scored.
                if( tes.getScore()<0  && ( typeId!=8 && typeId!=9 ) )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() && ( typeId!=8 && typeId!=9 ) )
                    continue;

                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() && ( typeId==8 || typeId==9 ) && getReportRuleAsBoolean("hideavsampleinfoforhiddenscores") )
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

                else if( typeId == 6 && scc.isEQ() )
                    tesList2.add( tes );

                else if( typeId == 4 && scc.isAbility() )
                    tesList2.add( tes );

                else if( typeId == 5 && scc.isKS() )
                    tesList2.add( tes );

                else if( typeId == 7 && scc.isCoreSkill() )
                    tesList2.add( tes );

                else if( typeId == 8 && scc.equals( SimCompetencyClass.SCOREDAUDIO ) )
                    tesList2.add( tes );

                else if( typeId == 9 && ( scc.equals( SimCompetencyClass.SCOREDAVUPLOAD ) || tes.getIntParam1()==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId()  ) ) // && tes.getScorableItemResponses()>0 )
                    tesList2.add( tes );

                else if( typeId == 10 && scc.isAIDerived() )
                    tesList2.add( tes );

                else if( typeId == 11 && scc.getIsInterest() )
                    tesList2.add( tes );

                else if( typeId==21 && scc.getIsAnyCustom() && (scc.equals( SimCompetencyClass.CUSTOM) || scc.equals( SimCompetencyClass.CUSTOM_COMBO)) )
                    tesList2.add( tes );
                else if( typeId==22 && scc.getIsAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM2))
                    tesList2.add( tes );
                else if( typeId==23 && scc.getIsAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM3))
                    tesList2.add( tes );
                else if( typeId==24 && scc.getIsAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM4))
                    tesList2.add( tes );
                else if( typeId==25 && scc.getIsAnyCustom() && scc.equals( SimCompetencyClass.CUSTOM5))
                    tesList2.add( tes );
            }

            // LogService.logIt( "CT2HtmlScoreFormatter.getCompetencyTaskSection() typeId==" + typeId + ", tes.size()=" + tesList2.size() + ", showColorGraph=" + showColorGraph + ", showStarIcon=" +  showStarRating );

            boolean hasAnyCefr = false;
            String cefrLevel;
            if( typeId==0 || typeId==5 )
            {
                for( TestEventScore tes : tesList2 )
                {
                    if( !tes.getTestEventScoreType().getIsCompetency() )
                        continue;

                    cefrLevel = StringUtils.getBracketedArtifactFromString(StringUtils.getUrlDecodedValue(tes.getTextParam1()), Constants.CEFRLEVEL );
                    if( cefrLevel==null || cefrLevel.isBlank() )
                        continue;

                    CefrScoreType cefrScoreType = CefrScoreType.getFromText( cefrLevel );
                    // LogService.logIt( "CT2HtmlScoreFormatter.getCompetencyTaskSection() typeId==" + typeId  + ", tes.name=" + tes.getName() + ", cefrLevel=" + cefrLevel + ", cefrScoreType=" + cefrScoreType.getName() + ", tes.size()=" + tesList2.size() + ", showColorGraph=" + showColorGraph + ", showStarIcon=" +  showStarRating );
                    if( !cefrScoreType.equals( CefrScoreType.UNKNOWN ))
                    {
                        hasAnyCefr = true;
                        break;
                    }
                }
            }

            if( !tesList2.isEmpty() )
            {
                String ttext=null;

                String style = rowStyle1;

                String key = null;

                if( typeId==0 )
                {
                    key = "g.KSAEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
                }

                else if( typeId == 1 )
                    key = "g.Tasks";

                else if( typeId == 2 )
                {
                    if( getReportRuleAsBoolean( "biodataisscoredsurvey" ) )
                        key = "g.ScoredSurveyEmTtl";
                    else
                    {
                        key = "g.BiodataEmTtl";
                        ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId() );
                    }
                }

                else if( typeId == 3 )
                {
                    key = "g.AIMSEmTtl";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId() );
                }

                else if( typeId == 4 )
                {
                    key = "g.AbilitiesEmTtl";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId() );
                }

                else if( typeId == 5 )
                {
                    key = "g.KsEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId() );
                }

                else if( typeId == 6 )
                {
                    key = "g.EQEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId() );
                }

                else if( typeId == 7 )
                    key = "g.VoiceSkillsEmTtl";

                else if( typeId == 8 )
                    key = "g.VoiceSamplesEmTtl";

                else if( typeId == 9 )
                    key = "g.AVUploadsEmTtl";

                else if( typeId == 10 )
                {
                    key = "g.AIEmTitle";
                    ttext = this.getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId() );
                }

                else if( typeId == 11 )
                {
                    key = "g.InterestsTitle";
                    ttext = getReportRuleAsString( "competencygrouptitle" + SimCompetencyGroupType.INTERESTS.getSimCompetencyGroupTypeId() );
                }
                else if( typeId>= 21 && typeId<=25 )
                {
                    key = "g.CustomTitle" + (typeId-20);
                    ttext = getReportRuleAsString( "competencygrouptitle" + (typeId + 80) );
                }

                String title = ttext==null ? lmsg( key , null ) : ttext;

                // only include this title row if not scored audio or scored video
                if( typeId!=8 && typeId!=9)
                    sb.append(getRowTitle(rowStyleHdr,
                                            title,
                                            isIncludeSubcategoryNumeric() && (showNumeric || hasAnyCefr) ? lmsg( "g.Score" ) : null,
                                            showColorGraph ? lmsg( "g.Interpretation" ) : "  " ,
                                            showStarRating ? lmsg( useRatingAndColors() ? "g.Rating" : "g.MatchJob" ) : null ) );

                else
                    sb.append( getRowTitle( rowStyleHdr,
                                            title,
                                            null,
                                            "  " ,
                                            null ) );

                //String label;
                //String value,value2;
                boolean showStarIcon;

                Object[] tdd;

                TestEventScore otes;
                List<TextAndTitle> nonCompAvUploads = null;

                if( typeId==9 )
                {
                    otes = te.getOverallTestEventScore();
                    nonCompAvUploads = otes.getTextBasedResponseList( NonCompetencyItemType.AV_UPLOAD.getTitle(), true, true );
                }

                else if( typeId==8 )
                {
                    try
                    {
                         nonCompAvUploads = ScoreFormatUtils.getNonCompTextListTable( te, NonCompetencyItemType.SPEAKING_SAMPLE );
                    }
                    catch( Exception e )
                    {
                        LogService.logIt( e, "CT2HtmlScoreFormatter.getCompetencyTaskSection() Getting non-competency av upload info from test event. typeId=8, testEventId=" + te.getTestEventId()  );
                    }
                }

               // LogService.logIt( "CT2HtmlScoreFormatter.getCompetencyTaskSection() typeId==" + typeId + ", tes.size()=" + tesList2.size() + ", nonCompAvUploads=" + (nonCompAvUploads==null ? "null" : nonCompAvUploads.size()) );

                for( TestEventScore tes : tesList2 )
                {
                    showStarIcon = showStarRating && tes.getScoreCategoryType().hasColor();

                    // Only show score if not an 8 or a 9 type or if there is a score present and we are supposed to show it.
                    if( (typeId!=8 && typeId!=9) && (tes.getScore()>=0 && SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports()) )
                    {
                        tdd = getContentRowForTestEventScore(tes,  typeId, tog, hasAnyCefr, showStarIcon, showStarRating, showColorGraph, useSolidBarGraphs  );
                        sb.append( (String)tdd[0] );
                        tog = (Boolean)tdd[1];

                        //if( showCaveats )
                        //{
                        //    tdd = getNonTopicCaveatScoresRow( tes, typeId, tog );
                        //    sb.append( (String)tdd[0] );
                        //    tog = (Boolean)tdd[1];
                        //}

                        if( showTopics && typeId==5 )
                        {
                            tdd = getTopicScoresRowForKSTest( tes, typeId, tog );
                            sb.append( (String)tdd[0] );
                            tog = (Boolean)tdd[1];
                        }
                    }

                    if( typeId == 8 || typeId == 9 )
                    {
                        tdd = getDetailRowForVoiceSampleOrAvTestEventScore( tes,  typeId, tog );

                        if( tdd != null )
                        {
                            sb.append( (String)tdd[0] );
                            tog = (Boolean)tdd[1];
                        }

                        if( nonCompAvUploads!=null && !nonCompAvUploads.isEmpty() )
                        {
                            tdd = getDetailRowForVoiceSampleOrAvTestEventScore( nonCompAvUploads,  typeId, tog, false );

                            if( tdd != null )
                            {
                                sb.append( (String)tdd[0] );
                                tog = (Boolean)tdd[1];
                            }

                            // do this so it doesn't repeat.
                            nonCompAvUploads = null;
                        }

                    }

                }
            }

            //if( typeId==9 )
            //{
            //    List<TextAndTitle>  ttList = getTestEvent().getOverallTestEventScore().getTextBasedResponseList( NonCompetencyItemType.AV_UPLOAD.getTitle(), true );

            //}
        }


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }


    protected Object[] getDetailRowForVoiceSampleOrAvTestEventScore( TestEventScore tes, int typeId, boolean tog )
    {
        java.util.List<TextAndTitle> ttl = new ArrayList<>(); // ScoreFormatUtils.getNonCompTextListTable( this.te, typeId==8 ? NonCompetencyItemType.SPEAKING_SAMPLE : NonCompetencyItemType.AV_UPLOAD );

        ttl.addAll( tes.getTextBasedResponseList( null, true, false ) );

        //for( TestEventScore tesx : te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ))
        //{
        //    if( typeId==8 && tesx.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() )
        //        ttl.addAll( tesx.getTextBasedResponseList( null, true ) );

        //    else if( typeId==9 && tesx.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() )
        //        ttl.addAll( tesx.getTextBasedResponseList( null, true ) );
        //}

        if( ttl.isEmpty() )
            return null;

        return getDetailRowForVoiceSampleOrAvTestEventScore( ttl, typeId, tog, true );
    }

    protected Object[] getDetailRowForVoiceSampleOrAvTestEventScore( List<TextAndTitle> ttl, int typeId, boolean tog, boolean isScoredTes )
    {
        try
        {
            if( ttl==null || ttl.isEmpty() )
                return null;

            long iirId;
            String question; // Question
            String text ; // text
            String playUrl;
            String iconUrl = RuntimeConstants.getStringValue( "ivrCustomTestAudioPlayIconUrlEmail" );
            String style;
            String sampleHtml;
            boolean hasPlayableMedia;

            boolean isScored = isScoredTes;

           // java.util.List<TextAndTitle> ttl = new ArrayList<>(); // ScoreFormatUtils.getNonCompTextListTable( this.te, typeId==8 ? NonCompetencyItemType.SPEAKING_SAMPLE : NonCompetencyItemType.AV_UPLOAD );

            //for( TestEventScore tesx : te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ))
            //{
            //    if( typeId==8 && tesx.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() )
            //        ttl.addAll( tesx.getTextBasedResponseList( null, true ) );

            //    else if( typeId==9 && tesx.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() )
            //        ttl.addAll( tesx.getTextBasedResponseList( null, true ) );
            //}

            StringBuilder sb = new StringBuilder();

            String speechTextEnglish;
            Locale testContentLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getLangStr() );
            boolean english = testContentLocale.getLanguage().equalsIgnoreCase("en");
            boolean englishReport = this.locale.getLanguage().equalsIgnoreCase( "en" );

            boolean showVideoUrls = !getReportRuleAsBoolean( "emlvideoviewoff" ) && !getTestKey().getHideMediaInReports();

            String reportMediaPlaybackUrl = getTestKey().getReportMediaViewUrl();

            // if can't show images, can't show image
            //if( showVideoUrls && org.getCandidateImageViewTypeId()>0 )
            //    showVideoUrls = false;

            //if( avEventFacade==null )
            //    avEventFacade = AvEventFacade.getInstance();

            // AvItemResponse iir;
            // String speechTextEnglish;
            //if( typeId == 9 )
            //{
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();

            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();
            //}

            AvItemResponse iir;
            UploadedUserFile uuf;

            boolean isVideo;
            String thumbFn;
            // String thumbUrl;
            String transform;

            boolean hideSpeechText = getReportRuleAsBoolean( "speechtextoff" );

            String borderRadStr;

            for( TextAndTitle tt : ttl )
            {
                if( (tt.getText() == null || tt.getText().isEmpty()) && tt.getUploadedUserFileId()<=0 )
                    continue;

                uuf=null;
                isVideo=false;
                thumbFn = null;
                hasPlayableMedia=false;

                iirId = tt.getUploadedUserFileId();

                speechTextEnglish = null;

                transform = "";

                // if( typeId==9 )
                // {
                iir = isScoredTes ? avEventFacade.getAvItemResponse(iirId, false) : null;

                if( iir!=null && iir.getTestEventId()!=te.getTestEventId() )
                    iir=null;

                if( iir==null || (typeId==9 && iir.getUploadedUserFileId()<=0) )
                    isScored = false;

                if( iir!=null && iir.hasPlayableMedia() )
                    hasPlayableMedia=true;

                uuf = isScored ? fileUploadFacade.getUploadedUserFile( iir.getUploadedUserFileId(), true ) : fileUploadFacade.getUploadedUserFile( iirId, true );

                if( uuf!=null && uuf.getTestEventId()!=te.getTestEventId() )
                    uuf=null;

                if( uuf!=null )
                {
                    hasPlayableMedia=true;

                    isVideo = uuf.getFileContentType().getIsVideo();
                    if( isVideo )
                    {
                        thumbFn = uuf.getThumbFilename();
                        if( thumbFn!=null && thumbFn.contains( ".AWSCOUNT." ) )
                            thumbFn = StringUtils.replaceStr( thumbFn, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );

                        else if( thumbFn!=null && thumbFn.contains(  ".IDX." ) )
                            thumbFn = StringUtils.replaceStr( thumbFn, ".IDX." , ".1." );
                    }

                    transform = uuf.getOrientation()<=0 ? "" : ";transform-origin:center;transform:rotate(" + uuf.getOrientation() + "deg)";
                }

                else if( iir==null )
                {
                    LogService.logIt(  "CT2HtmlScoreFormatter.getDetailRowForVoiceSampleOrAvTestEventScore() Unable to find AvItemResponse or UploadedUserFile for iirId=" +iirId + ", isScored=" + isScored + ", testeEventId=" + te.getTestEventId() );
                    continue;
                }

                borderRadStr = "";

                if( thumbFn!=null && !thumbFn.isEmpty() && uuf!=null &&  org.getCandidateImageViewTypeId()<=0  )
                {
                    iconUrl = ReportUtils.getMediaTempUrlSourceLink( tk.getOrgId(), uuf, 1, thumbFn, MediaTempUrlSourceType.FILE_UPLOAD_THUMB );

                    borderRadStr = ";border-radius:6px";
                    //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
                    //    iconUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps") + uuf.getDirectory() + "/" + thumbFn;
                    //else
                    //    iconUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl") + uuf.getDirectory() + "/" + thumbFn;
                }
                else
                    iconUrl = RuntimeConstants.getStringValue( isVideo ? "avCustomTestVideoPlayIconUrl" : "ivrCustomTestAudioPlayIconUrlEmail" );
                // }

                //else if( typeId==8 )
                //{
                //    hasPlayableMedia=true;
                //}


                if( !hideSpeechText && !english && englishReport && isScoredTes )
                {
                    speechTextEnglish = tt.getString1();
                    //if( avEventFacade==null )
                    //    avEventFacade = IvrEventFacade.getInstance();

                    //iir = avEventFacade.getAvItemResponse(iirId);

                    //if( iir != null && iir.getSpeechTextEnglish()!=null && !iir.getSpeechTextEnglish().isEmpty() )
                     //   speechTextEnglish = iir.getSpeechTextEnglish();
                }


                // toggle
                tog = !tog;

                question = tt.getTitle();

                if( question==null )
                    question = "";

                text = tt.getText();

                if( text==null || !isScoredTes )
                    text = lmsg( "g.NoResponseTextAvailable" );

                else if( hideSpeechText )
                    text = lmsg( "g.NoResponseTextHidden" );

                if( text.indexOf("[")>0 )
                    text = text.substring(0, text.indexOf("["));

                if( hasPlayableMedia && getTestKey().getHideMediaInReports() )
                    playUrl = lmsg( "g.NoPlayableMediaAvailableHidden");

                else if( hasPlayableMedia && showVideoUrls )
                {
                    if( uuf!=null && reportMediaPlaybackUrl!=null && !reportMediaPlaybackUrl.isBlank() )
                    {
                        String uufid=EncryptUtils.urlSafeEncrypt( uuf.getUploadedUserFileId() );
                        int type=1;
                        URL uurl = com.tm2score.util.HttpUtils.getURLFromString( reportMediaPlaybackUrl );
                        String qs = uurl.getQuery();
                        if( qs==null )
                            qs = "";
                        if( !qs.isBlank() )
                            qs += "&";
                        qs += "type=" + type + "&uid=" + uufid;

                        playUrl = uurl.getProtocol() + "://" + uurl.getAuthority() + uurl.getPath() + "?" + qs;
                    }
                    else
                        playUrl = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + ( isScored ? "/ta/avpb/" : "/ta/uavpb/" ) + te.getTestEventId() + "/"  + iirId;
                }

                else
                    playUrl = lmsg( "g.NoPlayableMediaAvailable");

                // playUrl = RuntimeConstants.getStringValue( "baseprotocol") + "://"+ RuntimeConstants.getStringValue( "baseadmindomain") + "/ta/misc/av/avpb-entry.xhtml?teid=" + EncryptUtils.urlSafeEncrypt( te.getTestEventId() ) + "&iirid="  + EncryptUtils.urlSafeEncrypt( iirId );

                if( hasPlayableMedia && showVideoUrls && addLimitedAccessLinkInfo==1 && (reportMediaPlaybackUrl==null || reportMediaPlaybackUrl.isBlank()) )
                {
                    if( !playUrl.contains("?") )
                        playUrl += "?";
                    else
                        playUrl+="&";

                    playUrl += "lid=[LIMITEDACCESSLINKIDENC]";
                }

                sampleHtml = "<p style=\"font-weight:bold\">" + lmsg( "g.IvrEmailSampleQuestionXC", new String[] {question} ) + "</p>\n" +
                        ( isScoredTes ? "<p><span style=\"font-weight:bold\">" +  lmsg( "g.IvrEmailResponseC" ) + "</span></p>\n" : "" ) +
                        "<p>" + text + "</p>\n"; // +

                if( speechTextEnglish != null && !speechTextEnglish.isEmpty() && isScoredTes )
                    sampleHtml += "<p><span style=\"font-weight:bold\">" + lmsg( "g.EnglishC" ) + "</span></p>\n<p>" + speechTextEnglish + "</p>\n";

                // only include the icon and video links IF the candidateImageViewType is ALL.
                //if( org.getCandidateImageViewTypeId()<=0 )
                //{
                if( playUrl!=null && !playUrl.isEmpty() && hasPlayableMedia && showVideoUrls )
                {
                    sampleHtml += "<table style=\"\"><tr><td><a href=\"" + playUrl + "\" target=\"_blank\" title=\"" + lmsg( typeId==8 ? "g.IvrEmailClickToListen" : "g.IvrEmailClickToPlayMedia" ) + "\"><img src=\"" + iconUrl + "\" alt=\"Media to Play Icon Graphic\" style=\"max-width:88px;max-height:72px" + borderRadStr + transform + "\"/></a></td><td style=\"font-size:12px;padding-left:5px\">" +
                             "<a href=\"" + playUrl + "\" target=\"_blank\" title=\"" + lmsg( "g.IvrEmailClickToListen" ) + "\">" + playUrl + "</a></td></tr></table>\n";

                }

                else if( playUrl!=null && !playUrl.isEmpty()  )
                {
                    sampleHtml += "<table style=\"\"><tr><td><img src=\"" + iconUrl + "\" alt=\"Media to Play Icon Graphic\" style=\"max-width:88px;max-height:72px" + borderRadStr + "\"/></td><td style=\"font-size:12px;padding-left:5px\">" + playUrl + "</td></tr></table>\n";
                }


                else
                    sampleHtml += "<table style=\"\"><tr><td><img src=\"" + iconUrl + "\" alt=\"Media to Play Icon Graphic\" style=\"max-width:88px;max-height:72px" + borderRadStr + transform + "\"/></td><td style=\"font-size:12px;padding-left:5px\">" +
                                 "</td></tr></table>\n";

                //}

                style = tog ? rowStyle1 : rowStyle2;

                //if( isScoredTes )
                //{
                if( typeId==8 )
                    sb.append( getRow( style, lmsg( "g.IvrEmailVoiceSampleC" ), sampleHtml, false ) );

                else if( typeId==9 )
                    sb.append( getRow( style, lmsg( isVideo ? "g.AvUploadEmailVideoC" : "g.AvUploadEmailAudioC" ), sampleHtml, false ) );
                //}
            }

            Object[] out = new Object[2];
            out[0] = sb.toString();
            out[1] = tog;
            return out;

        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT2HtmlScoreFormatter.getDetailRowForVoiceSampleOrAvTestEventScore() " );
            return null;
        }
    }




    protected Object[] getContentRowForTestEventScore(TestEventScore tes, int typeId, boolean tog, boolean hasCefr, boolean showStarIcon, boolean showStarRating, boolean showColorGraph, boolean useSolidColor4Graph)
    {
        boolean showNumeric = isIncludeSubcategoryNumeric() && !getReportRuleAsBoolean( "cmptynumoff" );
        // boolean percentiles =  isIncludeSubcategoryNorms() && !getReportRuleAsBoolean( "skipcomparisonsection" );
        boolean showCaveats = !getReportRuleAsBoolean( "hidecaveats" ) && !getReportRuleAsBoolean( "cmptymetasoff" ) && !getReportRuleAsBoolean("hidecompetencydetail");
        // boolean metas = !getReportRuleAsBoolean( "cmptymetasoff" );
        boolean useScoreTextAsNumScore = getReportRuleAsBoolean( "cmptyscrtxtasnum" );

        boolean shwGrphForTes = showGraphForTes(tes);

        String label = tes.getName();

        if( tes.getUsesPercentCorrectScoring() )
            label += " *";

        // toggle
        tog = !tog;

        String style = tog ? rowStyle1 : rowStyle2;
        String value;
        String value2;

        StringBuilder sb = new StringBuilder();

        int scrDigits = getReport().getIntParam3() >= 0 ? getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();

        if( tes.getIncludeNumericScoreInResults() && showNumeric )
        {
            value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), scrDigits );

            if( useScoreTextAsNumScore )
            {
                value = tes.getScoreText(); // getCompetencyScoreText( tes ); // tes.getScoreText();

                value = ReportUtils.getScoreValueFromStr( value );

                if( value == null )
                    value = "";
            }
        }

        else
            value =  "-";

        value2 = "";

        if( !showNumeric )
            value = "";

        //value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), 2 );
        //value2 = "";

        // if( !showNumeric )
        //     value = "";

        showStarIcon = showStarRating && tes.getScoreCategoryType().hasColor();

        if( !tes.getIncludeNumericScoreInResults() )
        {
            showStarIcon= false;
            showColorGraph = false;
        }

        List<String> caveats = null;

        if( showCaveats && ( tes.getSimCompetencyClass().isScoredDataEntry() || tes.getSimCompetencyClass().isScoredTyping()  || tes.getSimCompetencyClass().isScoredChat() ) )
        {
            // caveats = tes.getCaveatList();

            caveats = new ArrayList<>();

            for( String ct : tes.getCaveatList() )
            {
                if( ct.isEmpty() )
                    continue;

                if( ct.startsWith( Constants.TOPIC_KEY + "~" ) )
                    continue;

                caveats.add( ct );
            }
        }

        if( tes.getUsesPercentCorrectScoring() )
        {
            if( caveats==null )
                caveats = new ArrayList<>();

            caveats.add( lmsg("g.CompetencyUsesPercentCorrectScoring") );
        }

        // LogService.logIt( "CT2HtmlScoreFormatter.getContentRowForTestEventScore() tes.name=" + tes.getName() + ", hasCefr=" + hasCefr +", tes.getIncludeNumericScoreInResults()=" + tes.getIncludeNumericScoreInResults() + ", value=" + value + ", score=" + tes.getScore() + ", hasColor=" + tes.getScoreCategoryType().hasColor() );
        // LogService.logIt( "CT2HtmlScoreFormatter.getContentRowForTestEventScore() tes.name=" + tes.getName() + ", score=" + tes.getScore() + ", showColorGraph=" + showColorGraph + ", showStarIcon=" +  showStarIcon );

        // No subcategory norms in emails - too confusing.
        if( 1==2 && isIncludeSubcategoryNorms())
        {
            // No percentiles for biodata or personality or Eq
            value2 = (typeId==2 || typeId==3 || typeId==6 ) ? lmsg( "g.NA") : getSubcategoryNormString( tes.getPercentile(), tes.getOverallPercentileCount(), 0 );

            // show rating
            if( showStarIcon )
                sb.append( getRowColorDot(style, label, value, value2, false, tes.getScoreCategoryType() ) );

            // not show rating
            else
                sb.append( getRow(style, label, value, value2, false ) );

        }

        else if( showColorGraph || showStarIcon )
            sb.append(getRowWithColorGraphAndCategoryStars(style, label, value, false, tes.getScoreCategoryType(), tes, showColorGraph && shwGrphForTes, useSolidColor4Graph, showStarIcon, false) );

        else
            sb.append( getRow(style, label, value, value2, false ) );

        if( hasCefr && (typeId==0 || typeId==5) )
        {
            String cefrLevel = StringUtils.getBracketedArtifactFromString(StringUtils.getUrlDecodedValue(tes.getTextParam1()), Constants.CEFRLEVEL );
            if( cefrLevel!=null && !cefrLevel.isBlank() )
            {
                CefrScoreType cefrScoreType = CefrScoreType.getFromText( cefrLevel );
                if( !cefrScoreType.equals( CefrScoreType.UNKNOWN ))
                {
                    label = lmsg("g.CefrEquivScore");
                    value = cefrScoreType.getName( getLocale() );
                    sb.append( getRow(style, label, value, "", false ) );
                }
            }

        }

        if( caveats!=null && !caveats.isEmpty() )
        {
            // LogService.logIt( "CT2HtmlScoreFormatter.getContentRowForTestEventScore() Adding " + caveats.size() + " caveats." );
            sb.append( getRow( style,  caveats  ) );
        }


        return new Object[] {sb.toString(), tog };
    }



    /*
    // Puts the dot in the tird value column
    protected String getRowColorGraph( TestEventScore tes, String style, String label, String value, boolean bold, ScoreCategoryType sct, boolean useRawScore)
    {
        StringBuilder sb = new StringBuilder();

        try
        {
            // Start row
            sb.append("<tr " + style + ">\n<td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td>\n<td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"1\">" + value + "</td>\n" );

            sb.append( "<td style=\"" + getInlineStyle( "ct2competencygroupcell" ) + "\">\n" );

                sb.append( "<div style=\"" + getInlineStyle( "ct2scoregraphouter" ) + "\" style=\"margin-top:5px\">\n" );

                sb.append(getColorGraph(tes, sct, useRawScore, false ) + "\n" );

                // *
                //StringBuilder imgUrl = new StringBuilder();

                //FormatCompetency fc = tes.getFormatCompetency();

                //for( ScoreCategoryRange scr : fc.getScoreCategoryRangeList() )
                //{
                //    if( imgUrl.length()>0 )
                 //       imgUrl.append( "," );

                //    imgUrl.append( getRangeColor( scr.getRangeColor() ) + scr.getAdjustedRangePix() );
                //}

                //int ptrPos = fc.getPointerLeft();

                //String pflPrms = "";

                //if( tes.getProfileBoundaries() != null )
                //    pflPrms = "&prl=" + tes.getProfileBoundaries()[0] + "&prh=" + tes.getProfileBoundaries()[1];

                //sb.append( "<img style=\"width:" + (Constants.CT2_COLORGRAPHWID + 6) + "px;height:20px\" alt=\"" + MessageFactory.getStringMessage(locale, "g.CT2GraphicAlt" ) + "\" src=\"" + RuntimeConstants.getStringValue("baseprotocol") +  "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/ct2scorechart/" + tes.getTestEventScoreIdEncrypted()  + ".png?ss=" + imgUrl.toString() + "&p=" + ptrPos + pflPrms + "\"/>\n" );
                //

                sb.append( "</div>\n" );

            sb.append( "</td>\n" );

            // Add Color (star) image and finish row.
            sb.append("<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td>\n</tr>\n" );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "CT2HtmlScoreFormatter.getRowColorGraph() " + tes.toString() );
        }

        // LogService.logIt( "CT2HtmlScoreFormatter.getRowColorGraph() size=" + sb.length() ); // + "\n" + sb.toString() );

        return sb.toString();
        // return "<tr " + style + "><td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"2\">" + value + "</td>" + "<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }
    */



    //


    /**
     * USed to get color code for image generator.
     * @param color
     * @return
     *
    private String getRangeColor( String color )
    {
        if( color.equals("#ff8f8f") )
            return "r";
        else if( color.equals("#ffe78f") )
            return "ry";
        else if( color.equals("#ffff8f") )
            return "y";
        else if( color.equals("#eaff8f") )
            return "yg";
        else if( color.equals("#a1ff8f") )
            return "g";

        // Yellow
        return "y";
    }
    */



    private String getInlineStyle( String classId )
    {
        if( classId.equalsIgnoreCase( "ct2scoregraphouter" ) )
            return "margin-top:5px;height:20px;width:200px;";

        /*
        if( classId.equalsIgnoreCase( "ct2competencygroupcell" ) )
            return "padding-left:4px;"; //;font-size:11pt;color:#404040";

        if( classId.equalsIgnoreCase( "ct2scoregraphcolorholder" ) )
            return "margin-top:2px;width:180px;height:8px;z-index:5;position:absolute;padding-left:1px;padding-top:1px;";

        if( classId.equalsIgnoreCase( "ct2scoregraphshadebar" ) )
            return "z-index:5;height:8px;float:left;";

        if( classId.equalsIgnoreCase( "ct2scoregraphpointer" ) )
            return "width:9px;margin-top:-5px;z-index:20;position:absolute;";

        if( classId.equalsIgnoreCase( "ct2scoregraph" ) )
            return "width:180px;height:10px;z-index:10;position:absolute;border: 1px solid black;border-top: 0px;";

        if( classId.equalsIgnoreCase( "ct2graphsection" ) )
            return "float:left;width:36px;z-index:10;border-right:1px solid black;height:10px;";

        if( classId.equalsIgnoreCase( "ct2scorescale" ) )
            return "color:#b0b0b0;width:182px;height:10px;z-index:10;position:absolute;";

        if( classId.equalsIgnoreCase( "ct2scalesection" ) )
            return "border:0px !important;margin-top:10px;float:left;width:36px;height:10px;font-size:7pt;";
         */

        return "";
    }


    @Override
    public String getEmailSubj() throws Exception
    {
        // LogService.logIt( "CT2HtmlScoreFormatter.getEmailSubj() " + (params==null ? "null" : params[1] + ", :: " + params[2] + " :: " + params[3]));
        return lmsg(  "g.CoreTestResultEmailSubjAdmin" , params);
    }

    @Override
    public boolean useRatingAndColors()
    {
        return false;
    }

    /**
     * Used to hide interpretation when scored survey
     * @param tes
     * @return
     */
    //public boolean showInterpretationForTes( TestEventScore tes )
    //{
    //    return !tes.getSimCompetencyClass().isBiodata() || !reportData.getReportRuleAsBoolean( "biodataisscoredsurvey" );

    //}

    public boolean showGraphForTes( TestEventScore tes )
    {
        SimCompetencyClass scc = tes.getSimCompetencyClass();

        if( scc.isUnscored() )
            return false;

        if( !scc.getIsBiodata() || !getReportRuleAsBoolean( "biodataisscoredsurvey" ) )
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




}
