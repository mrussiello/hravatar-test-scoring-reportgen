/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.disc;

import com.tm2score.battery.BatteryScoreType;
import com.tm2score.battery.BatteryType;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.user.AssistiveTechnologyType;
import com.tm2score.util.StringUtils;

/**
 *
 * @author Mike
 */
public class DiscTestTakerHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{
    public static String HEADER_IMAGE_URL = "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_10x1742475075130.png";

    public DiscTestTakerHtmlScoreFormatter()
    {
        super();

        rowStyleHdr = " style=\"background-color:#0077cc;vertical-align:top;color:white\"";
        rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
        rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
        rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";
    }


    @Override
    public String getTextContent() throws Exception
    {
        return null; //  lmsg(  "g.CoreTestTestTakerScoringCompleteMsg" , params );
    }


    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNote) throws Exception
    {
        try
        {
            if( getReport() == null || getTestEvent() == null )
                return null;

            LogService.logIt( "DiscTestTakerHtmlScoreFormatter.getEmailContent() " );

            StringBuilder sb = new StringBuilder();

            // image header
            sb.append( this.getRowHeaderImage( HEADER_IMAGE_URL, 800 ) );
            sb.append( getRowSpacer( rowStyle0 ) );
            
            // Header Section
            Object[] out = getStandardHeaderSection( tog, includeTop, topNote, "g.DiscTestTakerScoringCompleteMsg", getCustomCandidateMsgText() );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Report Section
            out = getStandardReportSection(tog, true, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscTestTakerHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }


    @Override
    public Object[] getStandardHeaderSection( boolean tog, boolean includeTop, String topNoteHtml, String introLangKey, String customMsg )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        boolean includeCompanyInfo = !getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

        // String style, s0, s1, s2;

        String label;
        String value;

        if( includeTop )
        {
            if( topNoteHtml != null && !topNoteHtml.isEmpty() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"7\" style=\"border-bottom:0px solid black;padding-bottom:8px\">" + topNoteHtml + "</td></tr>\n" );

            String intro = customMsg;

            if( (intro == null || intro.isEmpty()) && introLangKey != null && !introLangKey.isEmpty() )
            {
                intro = lmsg(  introLangKey , params );
            }

            if( intro != null && !intro.isEmpty() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"7\" style=\"border-bottom:0px solid black;padding:10px\">" + intro + "</td></tr>\n" );
        }

        tog = true;
        String style; //  = tog ? rowStyle1 : rowStyle2;

        if( includeTop )
        {
            String nameKey = "g.NameC";

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

        } // if includeTop


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    
    

    @Override
    public String getEmailSubj() throws Exception
    {
        String c = getCustomCandidateEmailSubject();
        
        if( c!=null && !c.isBlank() )
            return c;
        
        return lmsg(  "g.DiscTestTakerFeedbackEmailSubj" , params);
    }


    @Override
    public boolean useRatingAndColors()
    {
        return false;
    }



}
