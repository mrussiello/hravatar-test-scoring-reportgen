/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.bestjobs;

import static com.tm2score.custom.bestjobs.BaseBestJobsReportTemplate.BEST_JOBS_BUNDLE;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.format.*;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.util.List;

/**
 *
 * @author Mike
 */
public class TestTakerJobMatchHtmlScoreFormatter extends BaseScoreFormatter implements ScoreFormatter
{

    public TestTakerJobMatchHtmlScoreFormatter()
    {
        super();

        rowStyleHdr = " style=\"background-color:#0077cc;vertical-align:top;color:white\"";
        rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
        rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
        rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";
    }


    @Override
    public String getTextContent() throws Exception
    {
        return null; //  lmsg(  "g.CoreTestTestTakerScoringCompleteMsg" , params );
    }


    private TestEvent findMatchingJobSpecificTestEvent() throws Exception
    {
        // TestEvent jobSpecificTestEvent = null;
        
        BestJobsReportFacade bestJobsReportFacade = BestJobsReportFacade.getInstance();
        
        List<TestEvent> tel = bestJobsReportFacade.getMostRecentTestEventsForUserId( getUser().getUserId(), getTestEvent().getOrgId(), 20 );
        
        if( tel.isEmpty() )
            return null;

        EventFacade eventFacade = EventFacade.getInstance();
        
        Product p;
        
        for( TestEvent te : tel )
        {
            p = eventFacade.getProduct( te.getProductId() );
            
            if( p==null )
                continue;
            
            if( !p.getProductType().getIsSimOrCt5Direct() )
                continue;
            
            if( p.getConsumerProductTypeId() != ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId() )
                continue;
            
            if( !te.getTestEventStatusType().getIsCompleteOrHigher() )
                continue;
            
            if( !te.getTestEventStatusType().getIsScoredOrHigher() )
                continue;
                        
            return te;
        }
        
        return null;
    }
        
    private boolean hasSponsoredJobSpec() throws Exception
    {
        TestEvent te = findMatchingJobSpecificTestEvent();
        
        if( te == null )
            return false;
        
        // if non-sponsored account
        if( te.getOrgId() == RuntimeConstants.getIntValue( "defaultMarketingAccountOrgId" ) )
            return false;
        
        return true;
            
    }
    
    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNote) throws Exception
    {
        try
        {
            if( getReport() == null || getTestEvent() == null )
                return null;

            // LogService.logIt( "TestTakerJobMatchHtmlScoreFormatter.getEmailContent() " );

            boolean hasSponsored = hasSponsoredJobSpec();
            
            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getJobMatchHeaderSection( tog, includeTop, topNote, hasSponsored ? "b.TestTakerJobMatchReportCompleteMsgSponsored" : "TestTakerJobMatchReportCompleteMsgStandalone" , getCustomCandidateMsgText(), hasSponsored );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
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

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestTakerJobMatchHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }



    @Override
    public String getEmailSubj() throws Exception
    {
        return bmsg(  "b.TestTakerJobMatchEmailSubj" , params);
    }


    @Override
    public boolean useRatingAndColors()
    {
        return false;
    }


    public String bmsg( String key )
    {
        return lmsg( key, null );
    }

    public String bmsg( String key, String[] prms )
    {
        return MessageFactory.getStringMessage( BEST_JOBS_BUNDLE, locale , key, prms );
    }
    
    
    public Object[] getJobMatchHeaderSection( boolean tog, boolean includeTop, String topNoteHtml, String introLangKey, String customMsg, boolean isSponsored )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

            // String style, s0, s1, s2;

            String label;
            String value;

            if( includeTop && topNoteHtml != null && !topNoteHtml.isEmpty() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"5\" style=\"border-bottom:0px solid black;padding-bottom:8px\">" + topNoteHtml + "</td></tr>\n" );

            String intro = customMsg;

            if( (intro == null || intro.isEmpty()) && introLangKey != null && !introLangKey.isEmpty() )
               intro = bmsg(  introLangKey , params );

            if( intro != null && !intro.isEmpty() )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"5\" style=\"border-bottom:0px solid black;padding:10px\">" + intro + "</td></tr>\n" );

            
            if( isSponsored )
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"5\" style=\"border-bottom:0px solid black;padding:10px\">" + bmsg( "b.NoScoresEmailCaveat", params ) + "</td></tr>\n" );
             
            tog = true;
            String style = tog ? rowStyle1 : rowStyle2;

            if( includeTop )
            {
                // title Row
                label = bmsg(  "b.YourInformation" , null );
                sb.append( getRowTitle( rowStyleHdr, label, null, null, null ) );

                // this is the TestTaker name, or anonymous.
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value = params[3];
                label = lmsg(  "g.NameC" , null );
                if( value != null && value.length() > 0 )
                    sb.append( getRow( style, label, value, false ) );

                if( !isAnonymous() )
                {
                    if( !StringUtils.isCurlyBracketed( u.getEmail() ) )
                    {
                        tog = !tog;
                        style = tog ? rowStyle1 : rowStyle2;
                        value = getUser().getEmail();
                        label = lmsg(  "g.EmailC" , null );
                        if( value != null && value.length() > 0 )
                            sb.append( getRow( style, label, value, false ) );
                    }
                }


                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value =I18nUtils.getFormattedDateTime(locale, getTestEvent().getStartDate(), getTestKey().getUser().getTimeZone() );
                label = bmsg(  "b.InterestSurveyStartedC" , null );
                if( value != null && value.length() > 0 )
                     sb.append( getRow( style, label, value, false ) );

                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                value =   I18nUtils.getFormattedDateTime(locale, getTestEvent().getLastAccessDate(), getTestKey().getUser().getTimeZone() );
                label = bmsg(  "b.InterestSurveyCompletedC" , null );
                if( value != null && value.length() > 0 )
                    sb.append( getRow( style, label, value, false ) );


            } // if includeTop


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    

}
