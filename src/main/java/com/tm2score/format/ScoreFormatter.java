/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.report.Report;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public interface ScoreFormatter
{
    public void init( TestKey tk, TestEvent te, Report r, Locale l, int addLimitedAccessLinkInfo ) throws Exception;

    public String getTextContent() throws Exception;

    public String getEmailContent( boolean tog, boolean includeTop, String topNote) throws Exception;

    public String getEmailSubj() throws Exception;

    public Locale getLocale();

    public String[] getParams();

    // public List<TestEventScore> getReportTestEventScoreList();
}
