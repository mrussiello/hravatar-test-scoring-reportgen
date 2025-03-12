/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.preview;

import com.tm2score.format.*;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;

/**
 *
 * @author Mike
 */
public class PreviewTestTakerHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public PreviewTestTakerHtmlScoreFormatter()
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

            StringBuilder sb = new StringBuilder();


            // Header Section
            Object[] out = getStandardHeaderSection( tog, includeTop, topNote, "g.PreviewTestTakerScoringCompleteMsg", getCustomCandidateMsgText() );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }


            if( isIncludeOverall()  )
            {
                tog = !tog;
                out = getStandardOverallScoreSection( tog );
                sb.append( (String) out[0] );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Report Section
            out = getStandardReportSection(tog, true, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Competency Section
            out = getStandardKsaSection( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            out = getStandardAimsSection( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Task Section
            out = getStandardTaskSection( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            out = getStandardEqSection( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            
            // Writing Sample Section
            out = getStandardWritingSampleSection(tog, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Min Quals Section
            out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.MIN_QUALS, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Applicant Data Section
            out = getStandardTextAndTitleSection(tog, NonCompetencyItemType.APPLICANT_INFO, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Competency Text Section
            out = getStandardCompetencyTaskTextAndTitleSection(tog, true, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Task Text Section
            out = getStandardCompetencyTaskTextAndTitleSection(tog, false, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // Has Uploaded Files Section
            out = getStandardUploadedFilesSection(tog, null );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }


            return sb.toString();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PreviewTestTakerHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }



    @Override
    public String getEmailSubj() throws Exception
    {
        return lmsg(  "g.PreviewResultEmailSubjTestTaker" , params);
    }




}
