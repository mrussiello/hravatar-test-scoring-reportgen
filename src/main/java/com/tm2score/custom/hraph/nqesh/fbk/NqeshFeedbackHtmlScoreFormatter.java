/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.nqesh.fbk;

import static com.tm2score.custom.hraph.nqesh.cor.NqeshCorHtmlScoreFormatter.emailSigLogo;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.List;

/**
 *
 * @author Mike
 */
public class NqeshFeedbackHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{    
    public NqeshFeedbackHtmlScoreFormatter()
    {
        super();
    }
    

    @Override
    public String getTextContent() throws Exception
    {
        return null; 
    }


    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNote) throws Exception
    {
        try
        {
            if( getReport() == null || getTestEvent() == null )
                return null;

            // Reports
            List<TestEventScore> tl = getTestEvent().getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() );                        
            TestEventScore rptTes = null;
            
            for( TestEventScore tes : tl )
            {
                if( tes.getTestEventScoreType().getIsReport() && tes.getReportId()==report.getReportId() )
                {
                    rptTes = tes;
                    break;
                }                    
            }
            
            if( rptTes==null )
            {
                String msg = "NqeshCorHtmlScoreFormatter.getEmailContent() Could not find a TestEventScore for reportId=" + report.getReportId() + ", " + report.getName() + " testEventId=" + getTestEvent().getTestEventId() + ", testKeyId=" + getTestEvent().getTestKeyId();
                LogService.logIt( msg );                
                throw new Exception( msg );
            }

            LogService.logIt( "NqeshFeedbackHtmlScoreFormatter.getEmailContent() testEventId=" + getTestEvent().getTestEventId() + ", rptTes.testEventScoreId=" + rptTes.getTestEventScoreId() + ", rptTes.reportId=" + rptTes.getReportId() );
            
            String rptDownloadUrl = rptTes.getReportDirectDownloadLink();
            
            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getMessageSection( rptDownloadUrl );
            String temp = (String) out[0];
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
            LogService.logIt( e, "NqeshCorHtmlScoreFormatter.getEmailContent() reportId=" + report.getReportId() + ", " + report.getName() + ", testEventId=" + getTestEvent().getTestEventId() + ", testKeyId=" + getTestEvent().getTestKeyId() );
            throw new STException( e );
        }
   }
    
    
    public Object[] getMessageSection( String rptUrl )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();
        
        String topNoteHtml="<p>Dear " + getUser().getFullname() + ",</p>\n"+ 
                "<p>The details of your <b>Individual Development Report</b> highlighting your strengths and areas for improvement with recommended interventions are explained in the feedback report that can be downloaded through the link provided below:</p>\n" + 
                "<div style=\"padding:10px;text-align:center\"><a href=\"" + rptUrl + "\">Download Report</a></div>\n" +
                "<p>This is an automatically generated email.  Please do not reply to this message. For any questions or concerns, you may access the NQESH Assistance Form through this link: <a href=\"https://bit.ly/NQESHAssist\">https://bit.ly/NQESHAssist</a>.</p>" + 
                "<p>Thank you and stay safe.</p>\n" + 
                "<p>Yours truly,<br/><img src=\"" + emailSigLogo + "\" style=\"max-width:130px\"/><br />NQESH Technical Working Group</p>\n" + 
                "<p style=\"font-size:0.92rem\"><b>Important Notice</b><br /><i>This e-mail and any attachments may contain confidential and/or proprietary information intended for the use of the named recipient/s only. If you are not the intended recipient, any unauthorized disclosure, copying, dissemination, or use of any of the information is strictly prohibited. Please notify them and immediately delete this e-mail from your system.</i></p>\n";

        sb.append( "<tr " + rowStyle0 + "><td colspan=\"7\">" + topNoteHtml + "</td></tr>\n" );

        out[0] = sb.toString();
        out[1] = false;
        return out;
    }

    
    
    
    
    @Override
    public String getCustomCandidateMsgText()
    {
        return null;
    }


    @Override
    public String getEmailSubj() throws Exception
    {
        return "Your NQESH 2023 INDIVIDUAL REPORT is READY!";
    }
    

}
