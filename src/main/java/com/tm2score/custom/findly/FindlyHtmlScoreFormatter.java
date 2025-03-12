/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.findly;

import com.tm2score.custom.coretest2.*;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class FindlyHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{
    public FindlyHtmlScoreFormatter()
    {
        super();

        this.MIN_COUNT_FOR_PERCENTILE = 10;
        // this.SCORE_PRECISION = 0;

        rowStyleHdr = " style=\"background-color:#0077cc;vertical-align:top;color:white\"";
        rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
        rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
        rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";
    }


    @Override
    public String getTextContent() throws Exception
    {
        return getStandardTextContent();
    }



    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        String dv = te.getProduct().getDetailView();

        if( dv != null && dv.equalsIgnoreCase( "findlytyping" ) )
            return getEmailContentFindlyDataEntryTyping(  tog,  includeTop,  topNoteHtml, true );

        if( dv != null && dv.equalsIgnoreCase( "findlydataentry" ) )
            return getEmailContentFindlyDataEntryTyping(  tog,  includeTop,  topNoteHtml, false );

        try
        {
            // LogService.logIt( "FindlyHtmlScoreFormatter.getEmailContent() STARTING" );

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



                // Scales Scores Section
                out = getFindlyLevelScoresSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Abilities
                out = getFindlyAbilitiesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Section
                out = getFindlyKsSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                out = getFindlyCompetenciesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
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
            LogService.logIt( e, "FindlyHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
    }

    public String getEmailContentFindlyDataEntryTyping( boolean tog, boolean includeTop, String topNoteHtml, boolean isTyping ) throws Exception
    {
        try
        {
            // LogService.logIt( "FindlyHtmlScoreFormatter.getEmailContentFindlyDataEntry() STARTING" );

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
                    out = getFindlyDataEntryTypingOverallScoreSection( tog, false );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }



                // Typing Statistics
                out = isTyping ? getFindlyTypingStatisticsSection( tog ) : getFindlyDataEntryStatisticsSection( tog );
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
            LogService.logIt( e, "FindlyHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }


    public Object[] getFindlyDataEntryTypingOverallScoreSection( boolean tog , boolean isTyping )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        tog = false;

        if( this.isIncludeOverall() )
        {
            sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), null, null ) );

            String style = tog ? rowStyle1 : rowStyle2;

            String value =  I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() );


            // value2 = getTestEvent().getScoreCategoryType().getName(locale);
            String label = lmsg(  "g.AccuracyRatePctC" , null );

            sb.append( getRow( style, label, value, false ) );

            if( isIncludeNorms() && getTestEvent().getOverallTestEventScore().getHasValidNorms() )
            {
                if( getTestEvent().getOverallTestEventScore().getHasValidOverallNorm() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  getNormString( getTestEvent().getOverallPercentile(), getTestEvent().getOverallPercentileCount(), 0 );
                    label = lmsg(  "g.OverallPercentileC" , null );
                    sb.append( getRow( style, label, value, false ) );
                }

                if( getTestEvent().getOverallTestEventScore().getHasValidCountryNorm() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  getNormString( getTestEvent().getCountryPercentile(), getTestEvent().getCountryPercentileCount(), 0 );
                    label = lmsg(  "g.XPercentileC" , new String[] {this.getCountryName( getTestEvent().getPercentileCountry()!=null && !getTestEvent().getPercentileCountry().isEmpty() ? getTestEvent().getPercentileCountry() : getUser().getCountryCode())} );
                    sb.append( getRow( style, label, value, false ) );
                }

                if( getTestEvent().getOverallTestEventScore().getHasValidAccountNorm() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = getNormString( getTestEvent().getAccountPercentile(), getTestEvent().getAccountPercentileCount(), 0 );
                    label = lmsg(  "g.XPercentileC" , new String[] {getOrg().getName()} );
                    sb.append( getRow( style, label, value, false ) );
                }
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }

    public Object[] getFindlyTypingStatisticsSection( boolean tog ) throws Exception
    {
        return getFindlyCompetencySection(tog, TestEventScoreType.SKILLS.getTestEventScoreTypeId(), true, "g.FindlyTypingStatistics", "g.Score" );
    }

    public Object[] getFindlyDataEntryStatisticsSection( boolean tog ) throws Exception
    {
        return getFindlyCompetencySection(tog, TestEventScoreType.SKILLS.getTestEventScoreTypeId(), true, "g.FindlyDataEntryStatistics", "g.Score" );
    }

    public Object[] getFindlyLevelScoresSection( boolean tog ) throws Exception
    {
        return getFindlyCompetencySection(tog, TestEventScoreType.LEVEL_SCORES.getTestEventScoreTypeId(), false, "g.FindlyLevelScores", "g.ScorePctCorrect" );
    }

    public Object[] getFindlyAbilitiesSection( boolean tog ) throws Exception
    {
        return getFindlyCompetencySection(tog, TestEventScoreType.ABILITIES.getTestEventScoreTypeId(), false, "g.FindlyAbilityScores", "g.Score"  );
    }

    public Object[] getFindlyKsSection( boolean tog ) throws Exception
    {
        return getFindlyCompetencySection(tog, TestEventScoreType.SKILLS.getTestEventScoreTypeId(), false, "g.FindlySkillScores", "g.ScorePctCorrect"  );
    }

    public Object[] getFindlyCompetenciesSection( boolean tog ) throws Exception
    {
        return getFindlyCompetencySection(tog, TestEventScoreType.COMPETENCY.getTestEventScoreTypeId(), false, "g.FindlyCompetencyScores", "g.Score"  );
    }


    private Object[] getFindlyCompetencySection( boolean tog, int testEventScoreTypeId, boolean competencyNamesArKeys, String titleKey, String scoreKey) throws Exception
    {
        if( scoreKey == null || scoreKey.isEmpty() )
            scoreKey = "g.Score";

        tog = true;

        Object[] out = new Object[2];
        out[0] = "";
        out[1] = tog;

        // String sectionTitle = MessageFactory.getStringMessage(locale, titleKey );

        StringBuilder sb = new StringBuilder();

        List<TestEventScore> tesl = getTestEvent().getTestEventScoreList( testEventScoreTypeId );

        if( tesl.isEmpty() )
            return out;

        LogService.logIt("FindlyHtmlScoreFormatter.getFindlyCompetencySection() " + titleKey + ", found " + tesl.size() + " scores to include in section." );

        String style; //  = rowStyle1;

        sb.append( getRowTitle( rowStyleHdr, lmsg( titleKey , null ), isIncludeSubcategoryNumeric() ? lmsg( scoreKey ) : null, null , null ) );

        String label;
        String value,value2;

        for( TestEventScore tes : tesl )
        {
            // Skip competencies or task-competencies that were not automatically scored.
            if( tes.getScore()<0 )
                continue;

            // if supposed to hide
            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;

            // if( hasProfile() )
            //     tes.setProfileBoundaries( getProfileEntryData( tes.getName() ) );

            label = tes.getName() + ":";

            if( competencyNamesArKeys )
                label = lmsg(tes.getName(),null) + ":";

            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;

            if( tes.getIncludeNumericScoreInResults() )
               value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), 0 );

            else
                value =  "-";

            value2 = "";

            if( !isIncludeSubcategoryNumeric() )
                value = "";

            sb.append( getRow(style, label, value, value2, "", false ) );
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }



    @Override
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
                LogService.logIt( "FindlyHtmlScoreFormatter.getStandardOverallScoreSection() OTES is null! Cannot create overall score section. "  + ( te==null ? "te is null" : te.toString()  ) );

            if( isIncludeOverall() && otes != null )
            {
                sb.append( getRowTitle( rowStyleHdr, lmsg(  "g.OverallResults" , null ), lmsg(  "g.Score" , null ), null, null ) );

                String style = tog ? rowStyle1 : rowStyle2;
                String value =  isIncludeNumeric() ? I18nUtils.getFormattedNumber(locale, getTestEvent().getOverallScore(), getTestEvent().getScorePrecisionDigits() ) : "";

                String label = lmsg(  "g.ScoreC" , null );

                sb.append( getRow( style, label, value, false ) );

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
                            label = lmsg(  "g.XPercentileC" , new String[] {this.getCountryName( getTestEvent().getPercentileCountry()!=null && !getTestEvent().getPercentileCountry().isEmpty() ? getTestEvent().getPercentileCountry() : getUser().getCountryCode())} );
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

                    /*
                    else
                    {
                            tog = !tog;
                            style = tog ? rowStyle1 : rowStyle2;
                            value = lmsg( "g.InsufficientDataForComparisons" );
                            label = lmsg(  "g.PercentileC" , new String[] {getOrg().getName()} );
                            sb.append( getRow( style, label, value, false ) );
                    }
                    */

                }


                if( otes.getScoreText()!=null && !otes.getScoreText().isEmpty() )
                {
                    tog = !tog;
                    label = "";
                    style = tog ? rowStyle1 : rowStyle2;
                    value =  StringUtils.replaceStandardEntities( otes.getScoreText() );
                    sb.append( getRow( style, label, value, false ) );
                }
            }

            out[0] = sb.toString();
            out[1] = tog;
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FindlyHtmlScoreFormatter.getStandardOverallScoreSection() " );

            out[0] = "";
            out[1] = toggle;
            return out;
        }
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
