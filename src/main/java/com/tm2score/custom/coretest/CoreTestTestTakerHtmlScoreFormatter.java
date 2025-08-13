/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.tm2score.format.*;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class CoreTestTestTakerHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public CoreTestTestTakerHtmlScoreFormatter()
    {
        super();
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
            Object[] out = getStandardHeaderSection(tog, includeTop, true, topNote, "g.CoreTestTestTakerScoringCompleteMsg", getCustomCandidateMsgText() );
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




}
