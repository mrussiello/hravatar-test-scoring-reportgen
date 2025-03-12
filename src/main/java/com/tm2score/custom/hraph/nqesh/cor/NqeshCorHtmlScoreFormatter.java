/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.nqesh.cor;

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
public class NqeshCorHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public static String emailSigLogo = "https://cdn.hravatar.com/web/orgimage/Qwib6aTeMlI-/img_1721211959695.png";
    

    public NqeshCorHtmlScoreFormatter()
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

            LogService.logIt( "NqeshFeedbackHtmlScoreFormatter.getEmailContent() testEventId=" + getTestEvent().getTestEventId() + ", reportId=" + report.getReportId() );

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
            
            LogService.logIt( "NqeshCorHtmlScoreFormatter.getEmailContent() testEventId=" + getTestEvent().getTestEventId() + ", rptTes.testEventScoreId=" + rptTes.getTestEventScoreId() + ", rptTes.reportId=" + rptTes.getReportId() + ", reportId=" + report.getReportId()  );
            
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
                "<p>Congratulations on being part of the FY 2023 National Qualifying Examination for School Heads (NQESH) which was administered on May 26 & June 2, 2024.</p>\n" + 
                "<p><b>Your Certificate of Rating (COR) is now ready.</b>  Please be reminded that if there will be any trace of alteration or erasures in the Certification, it shall be considered null and void.  Please click the link below to download your COR:</p>" +
                "<div style=\"padding:10px;text-align:center\"><a href=\"" + rptUrl + "\">Download COR</a></div>\n" +
                "<p>In addition, you will receive a separate email with the link to download your <b>Individual Development Report</b>.</p>" +
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
        return "Your NQESH 2023 CERTIFICATE OF RATING is READY!";
    }
    

}
