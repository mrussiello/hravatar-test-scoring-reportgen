/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2score.custom.coretest2.*;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TESNameComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Mike
 */
public class UMinnHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public static String chartMarkerImgUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_1409433986163.png";
    public static String chartAxisImgUrl = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_1409434017271.png";


    public UMinnHtmlScoreFormatter()
    {
        super();

        this.MIN_COUNT_FOR_PERCENTILE = 100;
        // this.SCORE_PRECISION = 0;

        rowStyleHdr = " style=\"background-color:#0077cc;vertical-align:top;color:white\"";
        rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
        rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
        rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";
    }

    public void initForSource()
    {
        
    }
    

    @Override
    public String getTextContent() throws Exception
    {
        try
        {
            String out = null;

            if( 1==1 || scoreFormatType.isUnscored() )
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

    
    
    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            // LogService.logIt( "UMinnHtmlScoreFormatter.getEmailContent() STARTING" );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection(tog, includeTop, false, topNoteHtml, "g.CoreTestAdminScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = (Boolean) out[1];
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // not batt.
            if( getTestEvent()!=null )
            {
                if( getTestKey().getTestEventList().size() >1 )
                {
                    String style = tog ? rowStyle1 : rowStyle2;
                    String value = getTestEvent().getProduct().getName();
                    String label = lmsg(  "g.TestC" , null );
                    if( value != null && value.length() > 0 )
                        sb.append( getRowTitle( style, label + " " + value, null, null, null ) );
                }

                boolean numScoresOn = this.getReportRuleAsBoolean( "ovrnumon" );
                
                if( numScoresOn &&  isIncludeOverall()   )
                {
                    tog = !tog;
                    out = getStandardOverallScoreSection( tog );
                    sb.append( (String) out[0] );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Report Section
                out = getStandardReportSection(tog, false, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                numScoresOn = getReportRuleAsBoolean( "cmptynumon" );
                
                if( numScoresOn )
                {
                    out = getStandardAimsSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = (Boolean) out[1];
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }

                    
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
    public Object[] getStandardAimsSection( boolean tog )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeIt = false;

        tog = true;

        includeIt = isIncludeCompetencyScores();

        boolean numScoresOn = getReportRuleAsBoolean( "cmptynumon" );
        
        if( numScoresOn && includeIt )
        {
            SimCompetencyClass scc;

            List<TestEventScore> tesList = getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            List<TestEventScore> tesList2 = new ArrayList<>();
            
            // List<String> caveats;

            for( TestEventScore tes : tesList )
            {
                // Skip competencies or task-competencies that were not automatically scored.
                if( tes.getScore()<0 )
                    continue;

                // if supposed to hide
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    continue;

                if( hasProfile() )
                    tes.setProfileBoundaries(getProfileEntryData(tes.getName(), tes.getNameEnglish() ) );

                scc = SimCompetencyClass.getValue(tes.getSimCompetencyClassId());

                if( scc.isAIMS() )
                    tesList2.add( tes );
            }
            
            Collections.sort( tesList2, new TESNameComparator() );

            if( tesList2.size() > 0 )
            {
                String style = rowStyle1;

                String key = "g.AIMSEmTtl";

                sb.append( getRowTitle( rowStyleHdr, lmsg( key , null ), lmsg( "g.Score" ), null , null ) );

                String label;
                String value,value2;

                int scrDigits;
                float scoreValToUse;
                int scoreScheme = getReportRuleAsInt( "scorescheme" );
                
                for( TestEventScore tes : tesList2 )
                {
                    // label = tes.getName();
                    scoreValToUse = tes.getPercentile();
            
                    if( scoreScheme==1 || scoreValToUse<=0 || tes.getOverallPercentileCount()<=10)
                        scoreValToUse = tes.getScore();
                    
                    label = ReportUtils.getCompetencyNameToUseInReporting( te, tes, te.getSimXmlObj(), te.getProduct(), locale );
                    
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    scrDigits = getReport().getIntParam3() >= 0 ? getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();
                    
                    // LogService.logIt( "BaseScoreFormatter.getCompetencyTaskSection() key=" + key + ", (report=" + (getReport()==null ? "null" : "not null: id=" + getReport().getReportId() + ", int3=" + getReport().getIntParam3()));
                    
                    value =  I18nUtils.getFormattedNumber(locale, scoreValToUse, scrDigits );
                    value2 = "";

                    sb.append( getRow(style, label, value, value2, false ) );
                }
            }
        }


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    
    
    public Object[] getStandardOverallScoreSection( boolean toggle )
    {
        Object[] out = new Object[2];

        boolean tog = toggle;

        boolean numScoresOn = getReportRuleAsBoolean( "ovrnumon" );
        
        if( !numScoresOn )
        {
            out[0] = "";
            out[1] = tog;
            return out;
        }
        
        try
        {
            StringBuilder sb = new StringBuilder();

            tog = false;

            TestEventScore otes = getOverallTes();

            if( otes == null )
                LogService.logIt( "UMinnHtmlScoreFormatter.getStandardOverallScoreSection() OTES is null! Cannot create overall score section. "  + ( te==null ? "te is null" : te.toString()  ) );

            if( isIncludeOverall() && otes != null )
            {
                boolean showStarRating = isIncludeCategory() && getTestEvent().getScoreCategoryType().hasColor();
                boolean showColorGraph = isIncludeColors();

                List<TextAndTitle> ct3RiskFactors = ScoreFormatUtils.getTextTitleList( getTestEvent() , CT3Constants.CT3RISKFACTORS );

                ct3RiskFactors.addAll( ScoreFormatUtils.getTextTitleList(getTestEvent(), Constants.STD_RISKFACTORSKEY ) );


                // title Row
                if( isIncludeNumeric() && showColorGraph && showStarRating )
                    sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), lmsg(  "g.Interpretation" ), lmsg(  this.useRatingAndColors() ? "g.Rating" : "g.JobMatch" , null ) ) );

                else if( isIncludeNumeric() && showColorGraph )
                    sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), lmsg(  "g.Interpretation" ), null ) );

                else if( isIncludeNumeric() && showStarRating )
                    sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), lmsg(  this.useRatingAndColors() ? "g.Rating" : "g.JobMatch" , null ), null ) );

                else if( isIncludeNumeric()  )
                    sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), null, null ) );

                else if( showStarRating )
                    sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), "", lmsg(  this.useRatingAndColors() ? "g.Rating" : "g.JobMatch" , null ), null ) );

                String style = tog ? rowStyle1 : rowStyle2;
                
                float ovrScrToUse = otes.getPercentile();
            
                if( getReportRuleAsInt("scorescheme")==1 || ovrScrToUse<=0 || otes.getOverallPercentileCount()<=10)
                    ovrScrToUse = otes.getScore();                
                
                String value =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, ovrScrToUse, getTestEvent().getScorePrecisionDigits() ) : "";

                // value2 = getTestEvent().getScoreCategoryType().getName(locale);
                String label = lmsg(  "g.ScoreC" , null );


                if( isIncludeNumeric() && ( showStarRating || showColorGraph ) )
                {
                    // TestEventScore tes = getOverallTes();

                    if( hasProfile() )
                        otes.setProfileBoundaries( getOverallProfileData() );

                    //if( otes != null )
                    //    LogService.logIt( "CT2HtmlScoreFormatter.getStandardOverallScoreSection() " + hasProfile() + ", tes.getProfileBoundaries()=" + (otes.getProfileBoundaries()!=null));



                    //if( tes.getProfileBoundaries()==null )
                    //    sb.append( getRowColorDot( style, label, value, false, getScoreCategoryType() ) );
                    //else
                    sb.append(getRowWithColorGraphAndCategoryStars(style, label, value, false, getScoreCategoryType(), otes, showColorGraph, false, showStarRating, false) );

                }

                else if( isIncludeNumeric()  )
                    sb.append( getRow( style, label, value, false ) );

                else if( showColorGraph )
                {
                    // TestEventScore tes = getOverallTes();

                    sb.append(getRowWithColorGraphAndCategoryStars(style, label, "", false, getScoreCategoryType(), otes, showColorGraph, false, showStarRating, false) );
                }

                else if( showStarRating )
                    sb.append( getRowColorDot( style, label, "", false, getScoreCategoryType() ) );

                if( isIncludeNorms() )
                {
                    if( getTestEvent().getOverallTestEventScore().getHasValidNorms() )
                    {
                        if( otes.getHasValidOverallNorm() )
                        {
                            tog = !tog;
                            style = tog ? rowStyle1 : rowStyle2;
                            value =  getNormString( getTestEvent().getOverallPercentile(), getTestEvent().getOverallPercentileCount(), 0 );
                            label = lmsg(  "g.OverallPercentileC" , null );
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

                    else
                    {
                            tog = !tog;
                            style = tog ? rowStyle1 : rowStyle2;
                            value = lmsg( "g.InsufficientDataForComparisons" );
                            label = lmsg(  "g.PercentileC" , new String[] {getOrg().getName()} );
                            sb.append( getRow( style, label, value, false ) );
                    }

                }


                if( otes.getScoreText()!=null && !otes.getScoreText().isEmpty() )
                {
                    tog = !tog;
                    label = "";
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  StringUtils.replaceStandardEntities( otes.getScoreText() );
                    sb.append( getRow( style, label, value, false ) );
                }

                String hasRule1 = getReportRuleAsString( "ct3risktoend" );

                boolean includeRiskFactorsAfterDetail = hasRule1 != null && hasRule1.equals( "1" );

                // LogService.logIt( "BaseCT2ReportTemplate.getReportInfoHeader() includeRiskFactorsAfterDetail=" + includeRiskFactorsAfterDetail );

                String hasRule2 = getReportRuleAsString( "ct3riskremove" );

                boolean includeRiskFactorsAnywhere = hasRule2 == null || !hasRule2.equals( "1" );

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
            StringBuilder sb = new StringBuilder();

            tog = false;

            if( isIncludeOverall() )
            {
                List<TextAndTitle> ct3RiskFactors = ScoreFormatUtils.getTextTitleList( getTestEvent() , CT3Constants.CT3RISKFACTORS );

                ct3RiskFactors.addAll( ScoreFormatUtils.getTextTitleList(getTestEvent(), Constants.STD_RISKFACTORSKEY ) );

                TestEventScore otes = getOverallTes();

                // title Row

                String style = tog ? rowStyle1 : rowStyle2;
                String value =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() ) : "";

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
     *
     * @param tog
     * @param typeId
     * @return
     */
    @Override
    protected Object[] getCompetencyTaskSection( boolean tog, int typeId )
    {
        boolean showStarRating = this.isIncludeSubcategoryCategory();
        boolean showColorGraph = this.isIncludeSubcategoryColors();

        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeIt = false;

        tog = true;

        if( typeId == 0 || typeId==3 || typeId==4 || typeId==5 || typeId==6 )
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

            for( TestEventScore tes : tesList )
            {
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

                else if( typeId == 6 && scc.isEQ() )
                    tesList2.add( tes );

                else if( typeId == 4 && scc.isAbility() )
                    tesList2.add( tes );

                else if( typeId == 5 && scc.isKS() )
                    tesList2.add( tes );
            }

            LogService.logIt( "CT2HtmlScoreFormatter.getStandardComptencyTaskSection() typeId==" + typeId + ", tes.size()=" + tesList2.size() + ", showColorGraph=" + showColorGraph + ", showStarIcon=" +  showStarRating );


            if( tesList2.size() > 0 )
            {
                String style = rowStyle1;

                String key = typeId==0 ? "g.KSAEmTitle" : "g.AIMSEmTtl";

                if( typeId == 1 )
                    key = "g.Tasks";

                if( typeId == 2 )
                    key = "g.BiodataEmTtl";

                if( typeId == 4 )
                    key = "g.AbilitiesEmTtl";

                if( typeId == 5 )
                    key = "g.KsEmTitle";

                if( typeId == 6 )
                    key = "g.EQEmTtl";
                
                
                sb.append( getRowTitle( rowStyleHdr,
                                        lmsg( key , null ),
                                        isIncludeSubcategoryNumeric() ? lmsg( "g.Score" ) : null,
                                        showColorGraph ? lmsg( "g.Interpretation" ) : "  " ,
                                        showStarRating ? lmsg( useRatingAndColors() ? "g.Rating" : "g.MatchJob" ) : null ) );

                String label;
                String value,value2;
                boolean showStarIcon;

                for( TestEventScore tes : tesList2 )
                {
                    label = tes.getName();
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    if( tes.getIncludeNumericScoreInResults() )
                       value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), 0 );

                    else
                        value =  "-";

                    value2 = "";

                    if( !isIncludeSubcategoryNumeric() )
                        value = "";

                    //value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), 2 );
                    //value2 = "";

                    if( !isIncludeSubcategoryNumeric() )
                        value = "";

                    showStarIcon = showStarRating && tes.getScoreCategoryType().hasColor();

                    if( !tes.getIncludeNumericScoreInResults() )
                    {
                        showStarIcon= false;
                        showColorGraph = false;
                    }



                    LogService.logIt( "CT2HtmlScoreFormatter.getStandardComptencyTaskSection() tes.name=" + tes.getName() +", tes.getIncludeNumericScoreInResults()=" + tes.getIncludeNumericScoreInResults() + ", value=" + value + ", score=" + tes.getScore() + ", hasColor=" + tes.getScoreCategoryType().hasColor() );
                    // LogService.logIt( "CT2HtmlScoreFormatter.getStandardComptencyTaskSection() tes.name=" + tes.getName() + ", score=" + tes.getScore() + ", showColorGraph=" + showColorGraph + ", showStarIcon=" +  showStarIcon );

                    // No subcategory norms in emails - too confusing.
                    if( 1==2 && isIncludeSubcategoryNorms())
                    {
                        // No percentiles for biodata or personality or Eq
                        value2 = (typeId==2 || typeId==3 || typeId==6) ? lmsg( "g.NA") : getSubcategoryNormString( tes.getPercentile(), tes.getOverallPercentileCount(), 0 );

                        // show rating
                        if( showStarIcon )
                            sb.append( getRowColorDot(style, label, value, value2, false, tes.getScoreCategoryType() ) );

                        // not show rating
                        else
                            sb.append( getRow(style, label, value, value2, false ) );

                    }

                    else if( showColorGraph || showStarIcon )
                        sb.append(getRowWithColorGraphAndCategoryStars(style, label, value, false, tes.getScoreCategoryType(), tes, showColorGraph, false, showStarIcon, false) );

                    else
                        sb.append( getRow(style, label, value, value2, false ) );
                }
            }
        }


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }



    // Puts the dot in the tird value column
    protected String getRowColorGraph( TestEventScore tes, String style, String label, String value, boolean bold, ScoreCategoryType sct)
    {
        StringBuilder sb = new StringBuilder();

        try
        {
            // Start row
            sb.append( "<tr " + style + ">\n<td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td>\n<td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"1\">" + value + "</td>\n" );

            sb.append( "<td style=\"" + getInlineStyle( "ct2competencygroupcell" ) + "\">\n" );

                sb.append( "<div style=\"" + getInlineStyle( "ct2scoregraphouter" ) + "\" style=\"margin-top:5px\">\n" );

                sb.append( getColorGraph(tes, sct, false, false ) + "\n" );

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
                //*/

                sb.append( "</div>\n" );

            sb.append( "</td>\n" );

            // Add Color (star) image and finish row.
            sb.append( "<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td>\n</tr>\n" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2HtmlScoreFormatter.getRowColorGraph() " + tes.toString() );
        }

        // LogService.logIt( "CT2HtmlScoreFormatter.getRowColorGraph() size=" + sb.length() ); // + "\n" + sb.toString() );

        return sb.toString();
        // return "<tr " + style + "><td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"2\">" + value + "</td>" + "<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }



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
        return lmsg(  "g.CoreTestResultEmailSubjAdmin" , params);
    }

    @Override
    public boolean useRatingAndColors()
    {
        return false;
    }



}
