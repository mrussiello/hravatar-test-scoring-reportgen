/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.tm2score.format.*;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class CT2TestTakerHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public CT2TestTakerHtmlScoreFormatter()
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

            // LogService.logIt( "CoreTestTestTakerHtmlScoreFormatter.getEmailContent() " );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection( tog, includeTop, topNote, "g.CoreTestTestTakerScoringCompleteMsg", getCustomCandidateMsgText() );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Report Section
            out = getStandardReportSection(tog, true, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "CoreTestTestTakerHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }



    @Override
    public String getEmailSubj() throws Exception
    {
        String c = getCustomCandidateEmailSubject();
        
        if( c!=null && !c.isBlank() )
            return c;
        
        return lmsg(  "g.CoreTestResultEmailSubjTestTaker" , params);
    }


    @Override
    public boolean useRatingAndColors()
    {
        return false;
    }



}
