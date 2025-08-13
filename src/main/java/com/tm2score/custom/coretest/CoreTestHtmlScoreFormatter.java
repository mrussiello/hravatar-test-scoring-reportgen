/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest;

import com.tm2score.format.*;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;

/**
 *
 * @author Mike
 */
public class CoreTestHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public CoreTestHtmlScoreFormatter()
    {
        super();

        this.MIN_COUNT_FOR_PERCENTILE = 10;
        // this.SCORE_PRECISION = 0;
    }


    @Override
    public String getTextContent() throws Exception
    {
        return getStandardTextContent();
    }



    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            // LogService.logIt( "CoreTestHtmlScoreFormatter.getEmailContent() STARTING" );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection(tog, includeTop, false, topNoteHtml, "g.CoreTestAdminScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = (Boolean) out[1];
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // not batt.
            if( getTestEvent()!=null )
            {
                if( getTestKey().getTestEventList().size() >1 )
                {
                    String style = tog ? rowStyle1 : rowStyle2;
                    String value = getTestEvent().getProduct().getName();
                    String label = lmsg(  "g.TestC" , null );
                    if( value != null && value.length() > 0 )
                        sb.append( getRowTitle( style, label + " " + value, null, null, null ) );
                }

                if( isIncludeOverall()  )
                {
                    tog = !tog;
                    out = getStandardOverallScoreSection( tog );
                    sb.append( (String) out[0] );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Report Section
                out = getStandardReportSection(tog, false, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Section
                out = getStandardKsaSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                out = getStandardAimsSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Task Section
                out = getStandardTaskSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Biodata Section
                out = getBiodataCompetencyTaskSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                out = getStandardEqSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                
                // Writing Sample Section
                out = getStandardWritingSampleSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                out = getStandardTopInterviewQsSection(tog, null);
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                
                
                // Min Quals Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.MIN_QUALS, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Applicant Data Section
                out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.APPLICANT_INFO, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Text Section
                out = getStandardCompetencyTaskTextAndTitleSection(tog, true, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Task Text Section
                out = getStandardCompetencyTaskTextAndTitleSection(tog, false, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Has Uploaded Files Section
                out = getStandardUploadedFilesSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "CoreTestHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }



    @Override
    public String getEmailSubj() throws Exception
    {
        return lmsg(  "g.CoreTestResultEmailSubjAdmin" , params);
    }



}
