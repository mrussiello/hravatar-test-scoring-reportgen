/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.vwga;

import com.itextpdf.text.BaseColor;
import com.tm2score.custom.coretest.ITextUtils;
import com.tm2score.custom.coretest2.*;
import com.tm2score.custom.coretest2.devel.CT2TestTakerFeedbackReport;
import com.tm2score.report.ReportData;
import com.tm2score.report.ReportTemplate;
import com.tm2score.service.LogService;
import java.net.URI;


/**
 *
 * Report Rules:
 *
 * ct3risktoend=1 means place risk factors at the end of the report
 * ct3riskremove=1 means no risk factors in the report, anywhere.
 *
 * allnointerview=1 means do not include the interview guide
 *
 *
 * @author Mike
 */
public abstract class BaseVWGATestTakerFeedbackTemplate extends CT2TestTakerFeedbackReport implements ReportTemplate
{
    @Override
    public void initColors()
    {
        
        super.initColors();

        // Nothing. 
        if( ct2Colors == null )
            ct2Colors = CT2Colors.getCt2Colors( devel ); 

        if( reportData.getReportRuleAsInt("hidecustombranding")==1 )
            return;
                        
        ct2Colors.hraBlue =  new BaseColor( 0x00, 0x8C, 0x82 );
        ct2Colors.headerDarkBgColor = ct2Colors.hraBlue;
    }
    
    @Override
    public synchronized void initFonts() throws Exception
    {
        super.initFonts();
        
        if( reportData.getReportRuleAsInt("hidecustombranding")==1 )
            return;
        
        // Change logo.
        String whiteTextSmallLogoUrl = "https://cdn.hravatar.com/web/orgimage/tUwRkr0seFg-/img_1733514478967.png";

        String blackTextLargerLogoUrl = "https://cdn.hravatar.com/web/orgimage/tUwRkr0seFg-/img_1733509666396.png";
        
        try
        {
            hraLogoWhiteTextSmall = ITextUtils.getITextImage( (new URI(whiteTextSmallLogoUrl)).toURL() );  
            
            hraLogoWhiteTextSmall.scalePercent(40);

            hraLogoBlackText = ITextUtils.getITextImage((new URI(blackTextLargerLogoUrl)).toURL() );  
            
            hraLogoBlackText.scalePercent(70);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseVWGATestTakerFeedbackTemplate.initFonts() ");
        }
        custLogo=null;
    }
    
    
    
    @Override
    public void init( ReportData rd ) throws Exception
    {
        super.init(rd);
        
        
    }
    


}
