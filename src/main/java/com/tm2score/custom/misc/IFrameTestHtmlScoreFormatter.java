/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.misc;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.report.Report;
import com.tm2score.format.*;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class IFrameTestHtmlScoreFormatter implements ScoreFormatter
{    

    public IFrameTestHtmlScoreFormatter()
    {
        super();
    }

    @Override
    public void init( TestKey tk, TestEvent te, Report r, Locale l, int addLimitedAccessLinkInfo ) throws Exception
    {        
    }


    @Override
    public Locale getLocale()
    {
        return Locale.US;
    }

    @Override
    public String[] getParams()
    {
        return new String[0];
    }
    
    

    @Override
    public String getTextContent() throws Exception
    {
        return null;
    }

    @Override
    public String getEmailSubj() throws Exception
    {
        return null;
    }


    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IFrameTestHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }



}
