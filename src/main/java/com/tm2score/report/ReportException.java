/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2score.entity.event.TestEvent;

/**
 *
 * @author Mike
 */
public class ReportException extends Exception
{
    public static final int NON_PERMANENT = 0;
    public static final int PERMANENT = 1;

    // 0 = not permanent. 1=permanent
    private int severity = 0;
    private TestEvent testEvent;
    private long reportId = 0;
    private String reportLangStr;


    public ReportException( String message, int severity, TestEvent te, long reportId, String reportLangStr )
    {
        super( message );

        this.severity = severity;

        testEvent = te;

        this.reportId = reportId;
        this.reportLangStr=reportLangStr;
    }

    public int getSeverity() {
        return severity;
    }

    @Override
    public String toString()
    {
        return "ReportException {" + getMessage() + ", severity=" + this.severity +  ", reportId=" + this.reportId + ", reportLanguage=" + reportLangStr + ", testEvent=" + ( testEvent == null ? "null" : testEvent.toString() ) + "}";

    }

    public TestEvent getTestEvent() {
        return testEvent;
    }


    public long getReportId() {
        return reportId;
    }

    public String getReportLangStr() {
        return reportLangStr;
    }


}
