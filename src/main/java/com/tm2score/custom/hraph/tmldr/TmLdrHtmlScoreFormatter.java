/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.tmldr;

import com.tm2score.custom.coretest2.*;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TESNameComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
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
public class TmLdrHtmlScoreFormatter extends CT2HtmlScoreFormatter implements ScoreFormatter
{
    static String[] persCompetencyNames = new String[] {"Develops Relationships", "Expressive and Outgoing", "Enjoys Problem-Solving", "Innovative and Creative", "Adaptable", "Needs Structure", "Seeks Perfection", "Empathy", "Emotional Self-Awareness", "Emotional Self-Control" };

    static String[] peopleMgmtNames = new String[] {"Frontline Management Fundamentals" };

    static String[] commitmentNames = new String[] {"Engagement", "Exhibits a Positive Work Attitude", "Corporate Citizenship" };

    static String[] motivationNames = new String[] {"Leadership Aspiration", "Competitive" };

    static String[] abcs = new String[] { " " , "A", "B", "C", "D", "E", "F", "G", "H" };

    static String[] detailCompTitleKeys = new String[] {"", "g.TmLdrAbilitiesTitle", "g.TmLdrPersonalCompsTitle", "g.TmLdrPeopleManagementTitle", "g.TmLdrCommitmentTitle", "g.TmLdrMotivationTitle"};
    static String[] detailCompSubtitleKeys = new String[] {"", "g.TmLdrAbilitiesSubtitle", "g.TmLdrPersonalCompsSubtitle", "g.TmLdrPeopleManagementSubtitle", "g.TmLdrCommitmentSubtitle", "g.TmLdrMotivationSubtitle"};



    public TmLdrHtmlScoreFormatter()
    {
        super();
    }


    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            useOverallRawScore = ScoreUtils.getIncludeRawOverallScore(org, te); // getIncludeRawOverallScore();
            // LogService.logIt( "TmLdrHtmlScoreFormatter.getEmailContent() STARTING" );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection( tog, includeTop, topNoteHtml, "g.CoreTestAdminScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
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

                if( isIncludeOverall()  )
                {
                    tog = !tog;
                    out = getStandardOverallScoreSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Report Section
                out = getStandardReportSection(tog, false, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Section
                out = getTmLeaderAbilitiesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Section
                out = getTmLeaderPersonalCompetenceSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                out = getTmLeaderPeopleManagementSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Task Section
                out = getTmLeaderCommitmentSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Biodata Section
                out = getTmLeaderMotivationSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Writing Sample Section
                out = getStandardWritingSampleSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Min Quals Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.MIN_QUALS, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Applicant Data Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.APPLICANT_INFO, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Text Section
                out = getStandardCompetencyTaskTextAndTitleSection(tog, true, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Task Text Section
                out = getStandardCompetencyTaskTextAndTitleSection(tog, false, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Has Uploaded Files Section
                out = getStandardUploadedFilesSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Notes section
                // Has Uploaded Files Section
                out = this.getStandardHRANotesSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "TmLdrHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }

    
    public Object[] getStandardOverallScoreSection( boolean toggle )
    {
        Object[] out = new Object[2];

        boolean tog = toggle;

        try
        {
            StringBuilder sb = new StringBuilder();

            tog = false;

            TestEventScore otes = getOverallTes();

            if( otes == null )
                LogService.logIt( "TmLdrHtmlScoreFormatter.getStandardOverallScoreSection() OTES is null! Cannot create overall score section. "  + ( te==null ? "te is null" : te.toString()  ) );

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
                String value; //  =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() ) : "";
                           
                if( useOverallRawScore )
                    value =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow(), getTestEvent().getScorePrecisionDigits() ) : "";
                else
                   value =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() ) : ""; 
                
                // value2 = getTestEvent().getScoreCategoryType().getName(locale);
                String label = lmsg(  "g.ScoreC" , null );

                ScoreCategoryType sct = useOverallRawScore ?  ScoreCategoryType.getScoreCategoryTypeForRawScore(ScoreFormatType.NUMERIC_0_TO_100,getTestEvent().getOverallTestEventScore().getOverallRawScoreToShow()  ) : getScoreCategoryType();

                if( isIncludeNumeric() && ( showStarRating || showColorGraph ) )
                {
                    // TestEventScore tes = getOverallTes();

                    if( hasProfile() )
                        otes.setProfileBoundaries( getOverallProfileData() );

                    //if( otes != null )
                    //    LogService.logIt( "CT2HtmlScoreFormatter.getStandardOverallScoreSection() " + hasProfile() + ", tes.getProfileBoundaries()=" + (otes.getProfileBoundaries()!=null));

                    //if( otes != null )
                    //    LogService.logIt( "CT2HtmlScoreFormatter.getStandardOverallScoreSection() " + hasProfile() + ", tes.getProfileBoundaries()=" + (otes.getProfileBoundaries()!=null));


                    //if( tes.getProfileBoundaries()==null )
                    //    sb.append( getRowColorDot( style, label, value, false, getScoreCategoryType() ) );
                    //else
                    sb.append(getRowWithColorGraphAndCategoryStars(style, label, value, false, sct, otes, showColorGraph, false, showStarRating, useOverallRawScore) );

                }

                else if( isIncludeNumeric()  )
                    sb.append( getRow( style, label, value, false ) );

                else if( showColorGraph )
                {
                    // TestEventScore tes = getOverallTes();

                    sb.append(getRowWithColorGraphAndCategoryStars(style, label, "", false, sct, otes, showColorGraph, false, showStarRating, useOverallRawScore) );
                }

                else if( showStarRating )
                    sb.append( getRowColorDot( style, label, "", false, sct ) );

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

                String scoreText=null; //  = otes.getScoreText();
                
                if( useOverallRawScore )
                {
                    if( te.getSimXmlObj()==null )
                    {
                        ReportUtils ru = new ReportUtils();

                        ru.loadTestEventSimXmlObject(te);

                        scoreText = TmLdrTestEventScorer.getScoreTextForOverallScore(te);
                    }                    
                }
                else
                    scoreText = otes.getScoreText();                
                

                if( scoreText !=null && !scoreText.isEmpty() )
                {
                    tog = !tog;
                    label = "";
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  StringUtils.replaceStandardEntities( scoreText );
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
    
    
    public Object[] getTmLeaderAbilitiesSection( boolean tog )
    {
        return getTmLeaderCompetencySection( tog,TmLdrCompetencyType.ABILITY.getTmLdrCompetencyTypeId() );
    }

    public Object[] getTmLeaderPersonalCompetenceSection( boolean tog )
    {
        return getTmLeaderCompetencySection( tog, TmLdrCompetencyType.PERSONALCOMPETENCE.getTmLdrCompetencyTypeId() );
    }

    public Object[] getTmLeaderPeopleManagementSection( boolean tog )
    {
        return getTmLeaderCompetencySection( tog, TmLdrCompetencyType.PEOPLEMANAGEMENT.getTmLdrCompetencyTypeId() );
    }

    public Object[] getTmLeaderCommitmentSection( boolean tog )
    {
        return getTmLeaderCompetencySection( tog, TmLdrCompetencyType.COMMITMENT.getTmLdrCompetencyTypeId() );
    }


    public Object[] getTmLeaderMotivationSection( boolean tog )
    {
        return getTmLeaderCompetencySection( tog, TmLdrCompetencyType.MOTIVATION.getTmLdrCompetencyTypeId() );
    }




    /**
     * typeId
     *      1=Ability
     *      2=Personal Competency
     *      3=People Management
     *      4=Commitment
     *      5=Motivation
     *
     * @param tog
     * @param tmLdrCompetencyTypeId
     * @return
     */
    protected Object[] getTmLeaderCompetencySection( boolean tog, int tmLdrCompetencyTypeId )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeIt = false;

        tog = true;
        
        TmLdrCompetencyType tmLdrCompetencyType = TmLdrCompetencyType.getValue(tmLdrCompetencyTypeId);
        
        TestEventScore groupTestEventScore = TmLdrScoreUtils.getGroupTestEventScore(tmLdrCompetencyType, this.te.getTestEventScoreList( TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() ));
        
        if( tmLdrCompetencyTypeId == 0 || tmLdrCompetencyTypeId==3 || tmLdrCompetencyTypeId==4 || tmLdrCompetencyTypeId==5 )
            includeIt = isIncludeCompetencyScores();

        else if( tmLdrCompetencyTypeId == 1 )
            includeIt = isIncludeTaskScores();

        else if( tmLdrCompetencyTypeId == 2 )
            includeIt = isIncludeBiodataScores();

        if( includeIt )
        {
            //SimCompetencyClass scc;

            List<TestEventScore> tesList = this.getTmLdrTestEventScoreList(TestEventScoreType.COMPETENCY,  tmLdrCompetencyTypeId );

            List<TestEventScore> tesList2 =  new ArrayList<>();

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

                //scc = SimCompetencyClass.getValue(tes.getSimCompetencyClassId());

                tesList2.add( tes );
            }

            Collections.sort( tesList2, new TESNameComparator() );

            // String nmKey = "g.TmLdrAssessOver_" + typeId + "_a";

            String titleStr = null;
            
            if( tesList2.size() > 0 )
            {
                String style = rowStyle1;

                String key = tmLdrCompetencyType.getKey(); //  "g.TmLdrAssessOver_" + tmLdrCompetencyTypeId + "_a"; // typeId==0 ? "g.KSAEmTitle" : "g.AIMSEmTtl";

                titleStr = lmsg( key , null );
                
                if( groupTestEventScore != null )
                    titleStr += " (Group Score: " + I18nUtils.getFormattedNumber(locale, groupTestEventScore.getScore(), 0 ) + ")";
                
                sb.append( getRowTitle( rowStyleHdr, titleStr, isIncludeSubcategoryNumeric() ? lmsg( "g.Score" ) : null, isIncludeSubcategoryNorms() ? lmsg( "g.Percentile" ) : null , this.isIncludeSubcategoryCategory() ? lmsg(this.useRatingAndColors() ? "g.Rating" : "g.MatchJob" ) : null ) );

                String label;
                String value,value2;
                boolean showColorRating;

                for( TestEventScore tes : tesList2 )
                {
                    label = tes.getName();
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    if( tes.getIncludeNumericScoreInResults() && tes.getScore()>=0 )
                       value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), 0 );

                    else
                        value =  "-";

                    value2 = "";

                    if( !isIncludeSubcategoryNumeric() )
                        value = "";

                    //if( !tes.getIncludeNumericScoreInResults() )
                    //    value = "-";

                    showColorRating = isIncludeSubcategoryCategory() && tes.getScoreCategoryType().hasColor();

                    if( !tes.getIncludeNumericScoreInResults() )
                        showColorRating= false;

                    // LogService.logIt( "TmLdrHtmlScoreFormatter.getTmLeaderComptencyTaskSection() tes.name=" + tes.getName() +", tes.getIncludeNumericScoreInResults()=" + tes.getIncludeNumericScoreInResults() + ", value=" + value + ", score=" + tes.getScore() + ", hasColor=" + tes.getScoreCategoryType().hasColor() );

                    if( isIncludeSubcategoryNorms())
                    {
                        // No percentiles for biodata or personality
                        value2 = (tmLdrCompetencyTypeId==2 || tmLdrCompetencyTypeId==3) ? lmsg( "g.NA") : getSubcategoryNormString( tes.getPercentile(), tes.getOverallPercentileCount(), 0 );

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
                }
            }
        }


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }




    protected java.util.List<TestEventScore> getTmLdrTestEventScoreList( TestEventScoreType test , int tmLdrCompetencyTypeId )
    {
        java.util.List<TestEventScore> out = new ArrayList<>();

        String nm;

        for( TestEventScore tes : te.getTestEventScoreList( test.getTestEventScoreTypeId() ) )
        {
            // if supposed to hide
            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;

            if( tmLdrCompetencyTypeId == 1 )
            {
                if( tes.getSimCompetencyClassId() == SimCompetencyClass.ABILITY.getSimCompetencyClassId() )
                    out.add( tes );

                if( tes.getSimCompetencyClassId() == SimCompetencyClass.ABILITY_COMBO.getSimCompetencyClassId() )
                    out.add( tes );

                else if( tes.getSimCompetencyClassId() == SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() )
                    out.add( tes );

            }

            else if( tmLdrCompetencyTypeId == 2 )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.persCompetencyNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }

            }

            else if( tmLdrCompetencyTypeId == 3 )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.CORESKILL.getSimCompetencyClassId() && tes.getSimCompetencyClassId() != SimCompetencyClass.KNOWLEDGE.getSimCompetencyClassId() )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.peopleMgmtNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }
            }

            else if( tmLdrCompetencyTypeId == 4 )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.commitmentNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }
            }

            else if( tmLdrCompetencyTypeId == 5 )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.motivationNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }

            }
        }

        // TESTING ONLY!
        if( 1==2 && out.isEmpty() )
        {
            out = te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            if( out.size()> 2 )
                out = out.subList( 0 , 2 );
        }


        Collections.sort( out );

        return out;

    }



}
