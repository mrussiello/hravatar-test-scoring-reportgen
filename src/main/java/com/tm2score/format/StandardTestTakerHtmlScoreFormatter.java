/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class StandardTestTakerHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public StandardTestTakerHtmlScoreFormatter()
    {
        super();
    }



    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNote) throws Exception
    {
        try
        {
            if( getReport() == null || getTestEvent() == null )
                return null;

            // LogService.logIt( "StandardTestTakerHtmlScoreFormatter.getEmailContent() " );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection(tog, includeTop, true, topNote, "g.CoreTestTestTakerScoringCompleteMsg", getCustomCandidateMsgText() );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = (Boolean) out[1];
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Report Section
            out = getStandardReportSection(tog, true, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = (Boolean) out[1];
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            return sb.toString();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestTakerHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }



    @Override
    public String getEmailSubj() throws Exception
    {
        return lmsg(  "g.ResultEmailSubjTestTaker" , params);
    }




}
