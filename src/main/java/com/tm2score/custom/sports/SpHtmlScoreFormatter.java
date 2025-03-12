/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.sports;

import com.tm2score.battery.BatteryType;
import com.tm2score.custom.coretest2.*;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class SpHtmlScoreFormatter extends CT2HtmlScoreFormatter implements ScoreFormatter
{

    SpData spData;
    
    
    public String rowStyleHdrSports = " style=\"background-color:#007f3f;vertical-align:top;color:white\"";
    public String hraSportsLogoWhiteTextUrl = "https://cdn.hravatar.com/web/orgimage/O3TtwpoP4zk-/img_12x1562615187933.png";


    public SpHtmlScoreFormatter()
    {
        super();
    }

    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            // LogService.logIt( "CqTestHtmlScoreFormatter.getEmailContent() STARTING" );
            if( spData == null )
                spData = new SpData();
            
            spData.init();
                        
            StringBuilder sb = new StringBuilder();

            boolean isBatt = getTestKey().getBatteryId()>0 && getTestKey().getTestEventList().size() >1 ;
            
            // Header Section
            Object[] out = getSpHeaderSection( tog, includeTop, topNoteHtml, isBatt ? "g.ScoringCompleteMsgBattery" : "g.ScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            
            
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
                    sb.append( getRowTitle( rowStyleHdrSports, label, null, null, null ) );
                    
                    String style = tog ? rowStyle1 : rowStyle2;
                    String value = getTestEvent().getProduct().getName();
                    //label = lmsg(  "g.TestC" , null );
                    if( value != null && value.length() > 0 )
                        sb.append( getRowTitle( style, " &nbsp;&nbsp;&nbsp;" + value, null, null, null ) );
                        // sb.append( getRowTitle( style, label + " " + value, null, null, null ) );
                }

                if( 1==2 && isIncludeOverall() && !getReportRuleAsBoolean( "ovroff" )  )
                {
                    tog = !tog;
                    out = getStandardOverallScoreSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Report Section
                if( !getReportRuleAsBoolean( "rptdwnldoff" ) && !getReportRuleAsBoolean( "emlrptdwnldoff" ) )
                {
                    out = getStandardReportSection(tog, false, rowStyleHdrSports );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]).booleanValue();
                        if( !isBatt )
                            sb.append( getRowSpacer( rowStyle0 ) );
                    }
                }

                // To turn off competencies in email, must have both set to off.
                boolean hideCompDet = getReportRuleAsBoolean("cmptysumoff") && getReportRuleAsBoolean( "hidecompetencydetail" );
                
                if( !hideCompDet )
                {
                    // Competency Section
                    out = getSpCompetenciesSection( tog );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]).booleanValue();
                        if( !isBatt )
                            sb.append( getRowSpacer( rowStyle0 ) );
                    }

                }
                
               

                // Writing Sample Section
                out = getStandardWritingSampleSection(tog, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                // IBM Insight Section
                out = getStandardIbmInsightScoresSection(tog, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                
                // Identity Image Capture Section
                out = getStandardImageCaptureSection(tog, rowStyleHdrSports, includeTop );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }


                // Min Quals Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.MIN_QUALS, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Applicant Data Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.APPLICANT_INFO, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Text Section
                out = getStandardCompetencyTaskTextAndTitleSection(tog, true, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Task Text Section
                out = getStandardCompetencyTaskTextAndTitleSection(tog, false, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Has Uploaded Files Section
                out = getStandardUploadedFilesSection(tog, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                if( isIncludeOverall() && !getReportRuleAsBoolean( "ovroff" )  )
                {
                    out = getTrailingRiskFactorsSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Item Responses Section
                out = getStandardItemResponsesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }

                

                // Notes section
                // Has Uploaded Files Section
                out = this.getStandardHRANotesSection(tog, rowStyleHdrSports );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                }


            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "CqTestHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }

    
    public Object[] getSpHeaderSection( boolean tog, boolean includeTop, String topNoteHtml, String introLangKey, String customMsg )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        String battName = null;
        if( isBattery() )
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
            sb.append( "<tr " + rowStyleHdrSports + "><td colspan=\"5\" style=\"border-bottom:0px;padding:5px\"><img src=\"" + hraSportsLogoWhiteTextUrl + "\" alt=\"HR Avatar Sports Logo Graphic\" /></td></tr>\n" );

            if( topNoteHtml != null && !topNoteHtml.isEmpty() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"5\" style=\"border-bottom:0px solid black;padding-bottom:8px\">" + topNoteHtml + "</td></tr>\n" );

            String intro = customMsg;

            if( (intro == null || intro.isEmpty()) && introLangKey != null && !introLangKey.isEmpty() )
            {
                if( isBattery() && battName!=null )
                {
                    String tn = params[0];
                    params[0]=battName;
                    intro = spData.bmsg( locale, introLangKey , params );
                    params[0]=tn;
                }
                else
                    intro = spData.bmsg( locale, introLangKey , params );
            }

            if( intro != null && !intro.isEmpty() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"5\" style=\"border-bottom:0px solid black;padding:10px\">" + intro + "</td></tr>\n" );
        }

        tog = true;
        String style; //  = tog ? rowStyle1 : rowStyle2;

        if( includeTop )
        {
            // title Row
            label = lmsg(  "g.TestEventData2" , null );
            sb.append( getRowTitle( rowStyleHdrSports, label, null, null, null ) );

            String nameKey = "g.AthleteC";

            //if( getUser().getUserType().getUserId() )
            //    nameKey = "g.UserIdC";

            //else if( getUser().getUserType().getUsername() )
            //    nameKey = "g.UsernameC";

            // this is the TestTaker name, or anonymous.
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            value = params[3];
            label = spData.bmsg( locale, nameKey );
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


            // Sim Name
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            // value = params[0];
            if( isBattery() )
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
                label = spData.bmsg( locale, "g.TestC" , null );
                value = params[0];            
            }

            if( value != null && value.length() > 0 )
                sb.append( getRow( style, label, value, false ) );

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

            // include only if there is an auth user name.
            if( includeCompanyInfo && !getUser().getRoleType().getIsPersonalUser() && params[8] != null && !params[8].isEmpty() )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value = params[8];
                label = spData.bmsg(  locale, "g.AuthorizedByC" , null );
                if( value != null && value.length() > 0 )
                        sb.append( getRow( style, label, value, false ) );

                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value = getOrg().getName();

                String logoUrl = org.getReportLogoUrl();
                
                if( suborg!=null && suborg.getReportLogoUrl() != null && !suborg.getReportLogoUrl().isBlank() )
                    logoUrl = suborg.getReportLogoUrl();
                
                if( logoUrl!=null && !logoUrl.isBlank() )
                    value = "<img src=\"" + logoUrl + "\" alt=\"" +  StringUtils.replaceStandardEntities( org.getName() ) + "\" style=\"max-width:150px\"/>";

                //if( org.getReportLogoUrl() != null && !org.getReportLogoUrl().isEmpty() )
                //     value = "<img src=\"" + org.getReportLogoUrl() + "\" alt=\"" +  StringUtils.replaceStandardEntities( org.getName() ) + "\"/>";

                label = lmsg(  "g.OrganizationC" , null );
                if( value != null && value.length() > 0 )
                        sb.append( getRow( style, label, value, false ) );
            }

            if( isBattery() && getBatteryScore()!=null && isIncludeOverall() )
            {
                if( isIncludeNumeric() )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    value = I18nUtils.getFormattedNumber(locale, getBatteryScore().getScore(), 0);
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
            }


        } // if includeTop


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    
    
    
    
    
    
    
    
    
    
    

    protected Object[] getSpCompetenciesSection( boolean tog )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeIt = false;

        tog = true;

        includeIt = isIncludeCompetencyScores();

        if( includeIt )
        {
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

                tesList2.add( tes );
            }
            
            boolean showNumeric = isIncludeSubcategoryNumeric() && !getReportRuleAsBoolean( "cmptynumoff" );
            boolean showGraph = !getReportRuleAsBoolean( "cmptygrphoff" );

            if( tesList2.size() > 0 && (showNumeric || showGraph) )
            {
                String style = rowStyle1;

                String key = "g.ClusterAndDimensionSummary" ;

                sb.append(getRowTitle(rowStyleHdrSports, spData.bmsg( getLocale(), key ), isIncludeSubcategoryNumeric() ? lmsg( "g.Score" ) : " ", " " , null ) );

                String label;
                String value,value2;

                int scrDigits;
                String scrCatTxt;
                int scrCat;
                
                // boolean percentiles =  isIncludeSubcategoryNorms() && !getReportRuleAsBoolean( "skipcomparisonsection" );
            // String ctxt = reportData.getReportRule( "hidecaveats" );
                // boolean metas = !getReportRuleAsBoolean( "hidecaveats" ) && !getReportRuleAsBoolean( "cmptymetasoff" );
                // boolean showCaveats = !getReportRuleAsBoolean( "hidecaveats" ) && !getReportRuleAsBoolean( "cmptymetasoff" ) && !getReportRuleAsBoolean("hidecompetencydetail");
                // boolean showTopics = !getReportRuleAsBoolean( "cmptytopicsoff" ) && !getReportRuleAsBoolean("hidecompetencydetail");
                Object[] tdd; 
                
                ScoreFormatType sft = getTestEvent().getScoreFormatType();                
                TestEventScore tes;                
                String[] comps = spData.getCompetencies();
                
                String nameEnglish;
                String name;
                
                
                for( String comp : comps )
                {
                    name = spData.getName(locale, comp );
                    nameEnglish = spData.getName(Locale.US, comp );  

                    tes=getTesForItem( name, nameEnglish, tesList2 );

                    if( tes==null )
                    {
                        LogService.logIt( "SpHtmlScoreFormatter.getSpCompetenciesSection() No TestEventScore found for competency " + name );
                        continue;
                    }

                    if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                    {
                        LogService.logIt( "SpHtmlScoreFormatter.getSpCompetenciesSection() TestEventScore.hide prevents showing in reports, for competency " + name );
                        continue;
                    }
                    
                    scrCat = spData.getScoreCode( tes.getScore() );
                    scrCatTxt = spData.getScoreCategoryText(locale, scrCat, false);
                    
                    label = ReportUtils.getCompetencyNameToUseInReporting( te, tes, te.getSimXmlObj(), te.getProduct(), locale );
                    
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;

                    scrDigits = getReport().getIntParam3() >= 0 ? getReport().getIntParam3() :  tes.getScoreFormatType().getScorePrecisionDigits();
                    
                    // LogService.logIt( "BaseScoreFormatter.getCompetencyTaskSection() key=" + key + ", (report=" + (getReport()==null ? "null" : "not null: id=" + getReport().getReportId() + ", int3=" + getReport().getIntParam3()));
                    
                    if( showNumeric && tes.getIncludeNumericScoreInResults() )
                       value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), scrDigits ) + ": ";

                    else
                        value =  "";

                    value += scrCatTxt;
                    
                    value2 = "";

                    
                    if( !showNumeric ) // || !isIncludeSubcategoryNumeric() )
                        value = "";
                    
                    if( showGraph )
                        sb.append(getRowWithColorGraphAndCategoryStars(style, label, value, false, tes.getScoreCategoryType(), tes, showGraph, false, false, false) );

                    else
                        sb.append( getRow(style, label, value, value2, false ) );                                        
                }
            }
        }

        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    
    
    protected TestEventScore getTesForItem( String name, String nameEnglish, List<TestEventScore> tesl )
    {
        if( ( name==null || name.isEmpty() ) && ( nameEnglish==null || nameEnglish.isEmpty() ) || tesl==null )
            return null;
        
        for( TestEventScore tes : tesl )
        {
            if( StringUtils.isValidNameMatch(name, nameEnglish, tes.getName(), tes.getNameEnglish() ) )
                return tes;
        }
        
        return null;            
    }
    
    
    

}
