/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import java.util.Locale;

/**
 *
 * @author Mike
 */
public interface ReportTemplate
{
    void init( ReportData reportData ) throws Exception;

    long addReportToOtherTestEventId() throws Exception;
    
    boolean isValidForTestEvent();    
    
    byte[] generateReport() throws Exception;

    boolean getIsReportGenerationPossible();

    void dispose() throws Exception;
    
    Locale getReportLocale();
    
    String getReportGenerationNotesToSave();

}
