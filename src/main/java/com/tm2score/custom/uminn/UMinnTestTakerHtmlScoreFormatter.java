/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2score.format.StandardTestTakerHtmlScoreFormatter;
import com.tm2score.global.I18nUtils;
import com.tm2score.util.StringUtils;

/**
 *
 * @author miker_000
 */
public class UMinnTestTakerHtmlScoreFormatter extends StandardTestTakerHtmlScoreFormatter {
    
    public UMinnTestTakerHtmlScoreFormatter()
    {
        super();
    }

    
    
    
    
    

    @Override
    public Object[] getStandardHeaderSection( boolean tog, boolean includeTop, boolean testTakerOnly, String topNoteHtml, String introLangKey, String customMsg)
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

            // String style, s0, s1, s2;

            String label;
            String value;

            if( includeTop && topNoteHtml != null && !topNoteHtml.isEmpty() )
                sb.append("<tr " + rowStyle0 + "><td colspan=\"5\" style=\"border-bottom:0px solid black;padding-bottom:8px\">" + topNoteHtml + "</td></tr>\n" );

            String intro = customMsg;

            if( (intro == null || intro.isEmpty()) && introLangKey != null && !introLangKey.isEmpty() )
               intro = ""; //  lmsg(  introLangKey , params );

            if( intro != null && !intro.isEmpty() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"5\" style=\"border-bottom:0px solid black;padding:10px\">" + intro + "</td></tr>\n" );

            tog = true;
            String style = tog ? rowStyle1 : rowStyle2;

            if( includeTop )
            {
                
                sb.append( "<tr style=\"background-color:#7a0019\"><td style=\"background-color:#7a0019;padding-top:8px;padding-left:8px;padding-right:4px;vertical-align:middle\">" + 
                                "<img src=\"https://cdn.hravatar.com/web/orgimage/Q8vQJ8K3q0E-/img_12x1481102616782.png\" width=\"120\" alt=\"UMinn Logo\"/></td>" +
                               "<td colspan=\"4\" style=\"font-weight:bold;vertical-align:top;color:white;background-color:#7a0019;padding:2px;vertical-align:middle;font-size:14pt;font-weight:bold\"><div style=\"font-size:18pt;padding-top:5px;padding-bottom:2px\">University of Minnesota</div><div>Professionalism and Interpersonal Skill Self-Development Exercise</div></td></tr>\n" );
                
                // title Row
                label = lmsg(  "g.TestEventData2" , null );
                sb.append( getRowTitle( rowStyleHdr, label, null, null, null ) );

                String nameKey = "g.NameC";
                
                if( u.getUserType().getUserId() )
                    nameKey = "g.UserIdC";
                
                else if( u.getUserType().getUsername() )
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
                    if( u.getUserType().getNamed() && !StringUtils.isCurlyBracketed( u.getEmail() ) )
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
                value = params[0];
                label = lmsg(  "g.AssessmentC" , null );
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
            } // if includeTop


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    

    
}
