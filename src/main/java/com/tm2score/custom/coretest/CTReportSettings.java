/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.tm2score.report.ReportSettings;
import com.tm2score.format.*;
import com.tm2score.report.ReportData;

/**
 *
 * This class is used to make any CoreTest-Specific Changes to the Standard ReportSettings
 *
 * @author Mike
 */
public class CTReportSettings extends StandardReportSettings implements ReportSettings
{
    @Override
    public void initSettings( ReportData reportData ) throws Exception
    {
        super.initSettings( reportData );
    }
    
    public void initColors()
    {
        // Nothing. 
    }
    
}
