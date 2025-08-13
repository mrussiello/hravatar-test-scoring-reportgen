/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.battery.BatteryType;
import com.tm2score.entity.battery.Battery;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class CompletionOnlyHtmlScoreFormatter1 extends BaseScoreFormatter implements ScoreFormatter
{

    public CompletionOnlyHtmlScoreFormatter1()
    {
        super();
    }


    @Override
    public String getTextContent() throws Exception
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
            
            
            String out = lmsg(  anon ? "g.ResultTextUnscoredAnon" : "g.ResultTextUnscored", params);

            // 160 char limit
            if( out.length()>159 )
                out = lmsg(  anon ? "g.ResultTextShortUnscoredAnon" : "g.ResultTextShortUnscored", params);

            // 160 char limit
            if( out.length()>159 )
                out = lmsg(  anon ? "g.ResultTextVeryShortUnscoredAnon" : "g.ResultTextVeryShortUnscored", params);

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
            // LogService.logIt( "HtmlScoreFormatter.getEmailContent() STARTING" );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection(tog, includeTop, false, topNoteHtml, "g.CoreTestAdminScoringCompletionOnlyMsg", null );
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
                    String label = lmsg(  "g.SimC" , null );
                    if( value != null && value.length() > 0 )
                        sb.append( getRowTitle( style, label + " " + value, null, null, null ) );
                }
            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "HtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }



    @Override
    public String getEmailSubj() throws Exception
    {
        return lmsg(  anon ? "g.ResultEmailSubjCompltdAnon" : "g.ResultEmailSubjCompltd", params);
    }




}
