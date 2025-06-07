/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.av;


import com.tm2score.custom.coretest2.CT2HtmlScoreFormatter;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.voicevibes.VoiceVibesResult;
import com.tm2score.voicevibes.VoiceVibesScaleScore;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class AvHtmlScoreFormatter extends CT2HtmlScoreFormatter implements ScoreFormatter
{
    public AvHtmlScoreFormatter()
    {
        super();

    }


    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            // LogService.logIt( "AvHtmlScoreFormatter.getEmailContent() STARTING" );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection( tog, includeTop, topNoteHtml, "g.CoreTestAdminScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
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

                if( isIncludeOverall()  && !getReportRuleAsBoolean( "ovroff" ) )
                {
                    tog = !tog;
                    out = getStandardOverallScoreSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Report Section
                if( !getReportRuleAsBoolean( "rptdwnldoff" ) && !getReportRuleAsBoolean( "emlrptdwnldoff" ) )
                {
                    out = getStandardReportSection(tog, false, null );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }
                }
                
                out = getStandardResponseRatingSummarySection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                // Competency Section
                // Competency Section
                for( int i=1; i<=5; i++ )
                {
                    out = getStandardCustomSection( tog, i );
                    temp = (String) out[0];
                    if( !temp.isEmpty() )
                    {
                        sb.append( temp );
                        tog = ( (Boolean) out[1]);
                        // if( !isBatt )
                        sb.append( getRowSpacer( rowStyle0 ) );
                    }
                }
                
                
                out = getStandardAbilitiesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Section
                out = getStandardKsSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Intersts Section
                out = getStandardInterestsSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                out = getStandardAimsSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                out = getStandardEqSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                                
                // Task Section
                out = getStandardTaskSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Biodata Section
                out = getBiodataCompetencyTaskSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                out = getStandardAiSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                // Voice Skills Section
                out = getStandardVoiceSkillsSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // AV Uploads Section
                out = getStandardScoredAvUploadsSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // IBM Insight Section
                out = getStandardIbmInsightScoresSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                // Voice Vibes Section
                out = getStandardVoiceVibesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
               
                // Has Uploaded Files Section
                out = getStandardUploadedFilesSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Writing Sample Section
                out = getStandardWritingSampleSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                out = getStandardTopInterviewQsSection(tog, null);
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                
                // Identity Image Capture Section
                out = getStandardSuspiciousActivitySection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Proctor Certifications Section
                out = getStandardProctorCertificationsSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                // Suspension History Section
                out = getStandardSuspensionHistorySection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                
                if( isIncludeOverall()  )
                {
                    out = getTrailingRiskFactorsSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                // Resume
                out = getStandardResumeSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                


                // Notes section
                // Has Uploaded Files Section
                out = this.getStandardHRANotesSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "AvHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }


    public Object[] getStandardVoiceSkillsSection( boolean tog )
    {
        return getCompetencyTaskSection( tog, 7 );
    }

    public Object[] getStandardScoredAvUploadsSection( boolean tog ) 
    {
        return getCompetencyTaskSection( tog, 9 );
    }
    
    
    public Object[] getStandardVoiceVibesSection( boolean tog ) throws Exception
    {
        try
        {
            Object[] out = new Object[2];

            StringBuilder sb = new StringBuilder();

            tog = true;

            SimCompetencyClass scc;

            List<TestEventScore> tesList = getTestEvent().getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

            List<TestEventScore> tesList2 = new ArrayList<>();

            for( TestEventScore tes : tesList )
            {
                scc = SimCompetencyClass.getValue(tes.getSimCompetencyClassId());

                 if( !scc.equals( SimCompetencyClass.SCOREDAUDIO ) && !scc.equals( SimCompetencyClass.SCOREDAVUPLOAD ) && tes.getScoreTypeIdUsed()!=CompetencyScoreType.SCORED_AV_UPLOAD.getCompetencyScoreTypeId() )
                     continue;

                // Skip competencies or task-competencies that were not automatically scored.
                if( tes.getScore()<0 )
                    continue;

                // if supposed to hide even the sample info
                if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() && getReportRuleAsBoolean( "hidevibesinfoforhiddenscores") )
                   continue;
                
                // if supposed to hide
                //if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                //    continue;
                
                //if( tes.getHide()==1 || tes.getHide()==3 )
                //     continue;

                // No vibes info
                if( tes.getTextParam1()==null || tes.getTextParam1().isEmpty() || tes.getTextParam1().indexOf( "VVWORDCOUNT" )<0 )
                    continue;

                if( hasProfile() )
                    tes.setProfileBoundaries(getProfileEntryData(tes.getName(), tes.getNameEnglish() ) );

                tesList2.add( tes );
            }

            // LogService.logIt( "AvHtmlScoreFormatter.getStandardVoiceVibesSection() tes.size()=" + tesList2.size() );

            if( tesList2.size() > 0 )
            {
                // String style = rowStyle1;
                boolean first = true;
                
                String key; //  = "g.IvrEmailVoiceAnalysisTtl";

                //sb.append( getRowTitle( rowStyleHdr,
                //                        lmsg( key , null ),
                //                        null,
                //                        "  " ,
                //                        null ) );


                VoiceVibesResult vvr;
                List<VoiceVibesScaleScore> vvssl;
                
                Object[] dat;

                for( TestEventScore tes : tesList2 )
                {
                    vvr = VoiceVibesResult.getFromPackedString( tes.getTextParam1() );

                    if( vvr==null || vvr.getScoreList()==null || vvr.getScoreList().isEmpty() )
                        continue;

                    if( !first )
                        sb.append( this.getRowSpacer( rowStyle0 ));
                    
                    first = false;
                    
                    key = "g.IvrEmailVoiceAnalysisTtl";

                    sb.append( getRowTitle( rowStyleHdr,
                                            lmsg( key , new String[]{tes.getName()} ),
                                            null,
                                            null ,
                                            null ) );
                    
                    vvssl = vvr.getStructureScaleScoreList();            
                    dat = addVoiceVibesCategoryToTable( tog, vvssl, "g.VoiceVibesStructure" );
                    if( dat != null )
                    {
                        sb.append( (String)dat[0] );
                        tog = (Boolean)dat[1];     
                    }
                    
                    vvssl = vvr.getVarietyScaleScoreList();            
                    dat = addVoiceVibesCategoryToTable( tog, vvssl, "g.VoiceVibesVariety" );
                    if( dat != null )
                    {
                        sb.append( (String)dat[0] );
                        tog = (Boolean)dat[1];     
                    }

                    vvssl = vvr.getGoodVibesScaleScoreList();            
                    dat = addVoiceVibesCategoryToTable( tog, vvssl, "g.VoiceVibesGoodVibes" );
                    if( dat != null )
                    {
                        sb.append( (String)dat[0] );
                        tog = (Boolean)dat[1];     
                    }

                    vvssl = vvr.getBadVibesScaleScoreList();            
                    dat = addVoiceVibesCategoryToTable( tog, vvssl, "g.VoiceVibesBadVibes" );
                    if( dat != null )
                    {
                        sb.append( (String)dat[0] );
                        tog = (Boolean)dat[1];     
                    }
                }
            }
            
            
            out[0] = sb.toString();
            out[1] = tog;

            // LogService.logIt("AvHtmlScoreFormatter.getStandardVoiceVibesSection() testEventId=" + te.getTestEventId() + " Email HTML=\n" + (String)out[0] );

            return out;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "AvHtmlScoreFormatter.getStandardVoiceVibesSection()" + te.toString() );
            
            throw e;
        }
        
    }
    
    

    protected Object[] addVoiceVibesCategoryToTable( boolean tog, List<VoiceVibesScaleScore> sl, String titleKey  ) 
    {
        try
        {
            Object[] out = new Object[2];

            StringBuilder sb = new StringBuilder();

            if( sl==null || sl.isEmpty() )
                return null;
        
            String style =  tog ? rowStyle1 : rowStyle2;
        
            // LogService.logIt( "AvHtmlScoreFormatter.addVoiceVibesCategoryToTable() ScoreList.size=" + sl.size() + ", titleKey=" + titleKey + ", tog=" + tog + ", rowStyle=" + style );
        
            tog = !tog;
            
            // Add Title Row - just bold. 
            sb.append( getRow( style, lmsg( titleKey), true ) );
            
            //tog=!tog;
            
            //style =  tog ? rowStyle1 : rowStyle2;
            // Next, the vibez        
        
            ScoreCategoryType sct = null;
        
            String scoreStr;

            VoiceVibesScaleScore vss;

            //boolean last = false;

            sb.append( "<tr " + style + "><td colspan=\"6\">\n<table style=\"\" cellspacing=\"0\">\n" );
            
            String imgUrl = null;
            
            for( int i=0;i<sl.size(); i++ )
            {
                // style =  tog ? rowStyle1 : rowStyle2;
                
                vss = sl.get(i);

                // last = i==sl.size()-1;

                if( vss.getVoiceVibesScaleType().isGoodVibe() )
                    sct = ScoreCategoryType.GREEN;
                else if( vss.getVoiceVibesScaleType().isBadVibe() )
                    sct = ScoreCategoryType.RED;
                else
                    sct = ScoreCategoryType.YELLOW;

                vss.setLocale( getLocale() );

                if( vss.getVoiceVibesScaleType().isShowTextForNumScore() )
                    scoreStr = lmsg( vss.getVoiceVibesScaleType().getScoreLangKeyForTextNumScore( vss.getScore() ) );

                else
                    scoreStr = I18nUtils.getFormattedNumber( getLocale(), vss.getScore(), 0 );
                
                imgUrl = getVoiceVibesColorGraphImgUrl(vss);

                sb.append( "<tr " + style + "><td style=\"width:21px\"></td>\n"
                        + "<td style=\"vertical-align:top;width:225px\">" + vss.getLocalizedName() + "</td>\n"
                        + "<td style=\"vertical-align:top;width:44px\">" + scoreStr + "</td>\n"
                        + "<td style=\"vertical-align:top;width:100px\">" + lmsg(vss.getScaleLowKey()) + "</td>\n"
                        + "<td style=\"vertical-align:top;width:210px\"><img style=\"width:" + (Constants.IVR_COLORGRAPHWID) + "px;height:20px\" alt=\"" + lmsg( "g.CT2GraphicAlt" ) + "\" src=\"" + imgUrl + "\"/></td>\n"
                        + "<td style=\"vertical-align:top;width:100px\">" + lmsg(vss.getScaleHighKey()) + "</td>\n"
                        + "</tr>\n" );
            }
            
            sb.append( "</table>\n</td>\n</tr>\n" );
                        
            out[0] = sb.toString();
            out[1] = tog;
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AvHtmlScoreFormatter.addVoiceVibesCategoryToTable() " + te.toString() );
            return null;
        }
    }

    
    
    protected String getVoiceVibesColorGraphImgUrl( VoiceVibesScaleScore vss)
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append( RuntimeConstants.getStringValue("baseprotocol") +  "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/ivrvvscorechart/" + te.getTestEventIdEncrypted() + "_" + vss.getVoiceVibesScaleType().getVoiceVibesScaleTypeId() + ".png" );

        sb.append( vss.getColorGraphImgUrlParams() );
        //String scoreStr = I18nUtils.getFormattedNumber( getLocale(), vss.getScore(), 0 );
        //sb.append("v=" + vss.getVoiceVibesScaleType().getVoiceVibesScaleTypeId() + "&s=" + scoreStr + "&tw=" + Constants.IVR_COLORGRAPHWID + "&h=" + Constants.IVR_COLORGRAPHHGT );
        
        return sb.toString();
    }
    
    
}
