/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.dist;

import com.tm2score.api.ResultPoster;
import com.tm2score.api.ResultPosterFactory;
import com.tm2score.battery.BatteryScoreType;
import com.tm2score.battery.BatteryType;
import com.tm2score.email.EmailBlockFacade;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.proctor.ProctorEntry;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventLog;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.proctor.ProctorSuspension;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.user.LimitedAccessLink;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventArchiver;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventLogUtils;
import com.tm2score.event.TestEventResponseRatingFacade;
import com.tm2score.event.TestEventResponseRatingUtils;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeySourceType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.format.BaseScoreFormatter;
import com.tm2score.format.ScoreFormatter;
import com.tm2score.format.StandardHtmlScoreFormatter;
import com.tm2score.format.StandardTestTakerHtmlScoreFormatter;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.proctor.ProctorFacade;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.twilio.TwilioSmsUtils;
import com.tm2score.user.CandidateAudioVideoViewType;
import com.tm2score.user.LimitedAccessLinkFacade;
import com.tm2score.user.UserActionFacade;
import com.tm2score.user.UserActionType;
import com.tm2score.user.UserFacade;
import com.tm2score.util.GooglePhoneUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.awt.ComponentOrientation;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class BaseDistManager {
    
    public static int MAX_DIST_ERRORS = 8;
    public static boolean DEBUG_DIST = false;
    

    // @Inject
    protected EventFacade eventFacade;

    protected UserFacade userFacade = null;

    protected UserActionFacade userActionFacade;
    
    protected ProctorFacade proctorFacade;
    
    protected LimitedAccessLinkFacade limitedAccessLinkFacade;
    
    protected TestEventResponseRatingFacade testEventResponseRatingFacade;
    
    
    public int[] distributeTestKeyResults( TestKey tk, boolean candFbkOnly, long fbkReportId, long testEventIdOnly, int archiveDelaySecs) throws Exception
    {
        try
        {
            Date procStart = new Date();

            int[] out = new int[3];

            LogService.logIt( "BaseDistManager.distributeTestKeyResults() START " + ( tk==null ? "TestKey is null" : "testKeyId=" + tk.getTestKeyId() +", tk.authUserId=" + tk.getAuthorizingUserId()) + ", candFbkOnly="  + candFbkOnly + ", fbkReportId=" + fbkReportId + ", testEventIdOnly=" + testEventIdOnly ) ;
            
            if( tk==null  )
            {
                LogService.logIt("BaseDistManager.distributeTestKeyResults() AAA.00 TestKey is null. Cannot distribute anything. " );
                return out;
            }

            if( !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_STARTED ) )
            {
                LogService.logIt("BaseDistManager.distributeTestKeyResults() AAA.0.a TestKey is not in proper status. Waiting and reloading. testKeyId=" + tk.getTestKeyId() + ", status=" + tk.getTestKeyStatusType().getName() );
                Thread.sleep(1000+(long)(5000*Math.random()));
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();            
                
                tk = eventFacade.getTestKey(tk.getTestKeyId(), true );
            }

            if( !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_STARTED ) )
            {
                LogService.logIt("BaseDistManager.distributeTestKeyResults() AAA.0.b TestKey is not in proper status AFTER WAIT. testKeyId=" + tk.getTestKeyId() + ", status=" + tk.getTestKeyStatusType().getName() );
                return out;
            }
            
            
            //LogService.logIt( "BaseDistManager.distributeTestKeyResults() BBB " );
            Tracker.addDistributionStart();
            //boolean init = false;

            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();            

            initTestKey( tk );
            
            // load if needed.
            if( tk.getTestEventList() == null  )
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );
                        
            if( tk.getTestEventList().isEmpty() )
            {
                List<TestEvent> tel = eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), false );

                // no test events found.
                if( tel.isEmpty() )
                {
                    if( tk.getExpireDate()!=null && tk.getExpireDate().before( new Date() ) )
                    {
                        LogService.logIt("BaseDistManager.distributeTestKeyResults() AAA.1 TestKey is past expire date and has no test events of any kind but is expired. Testkey status is " + tk.getTestKeyStatusType().getName() +  "?? Changing to Expired," + tk.toString() );
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.EXPIRED.getTestKeyStatusTypeId() );
                        eventFacade.saveTestKey(tk);
                        archiveIfNeeded( tk );
                        return out;                
                    }
                    
                    LogService.logIt("BaseDistManager.distributeTestKeyResults() AAA.2 TestKey has no valid test events but status is " + tk.getTestKeyStatusType().getName() +  "?? Since it is not expired, changing to Started. " + tk.toString() );
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.STARTED.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    return out;                                   
                }
               
                boolean hasUnreadyTestEvent = false;
                // at this point there is at least one test event.                
                //boolean hasScoredTestEvent = false;
                //boolean hasValidTestEvent = false;
                //int deactivated = 0;
                //int expired = 0;
                for( TestEvent te : tel )
                {
                    // single
                    if( te.getProductId()==tk.getProductId() )
                    {
                        // deactivated.
                        if( te.getTestEventStatusType().getIsDeactivated() )
                        {
                            LogService.logIt("BaseDistManager.distributeTestKeyResults() BBB.1 Single product TestKey has only deactivated test events. Testkey status is " + tk.getTestKeyStatusType().getName() +  "?? Changing to Deactivated," + tk.toString() );
                            tk.setTestKeyStatusTypeId( TestKeyStatusType.DEACTIVATED.getTestKeyStatusTypeId() );
                            eventFacade.saveTestKey(tk); 
                            archiveIfNeeded( tk );
                            return out;                                            
                        }   
                        
                        // expired.
                        if( te.getTestEventStatusType().getIsExpired() )
                        {
                            LogService.logIt("BaseDistManager.distributeTestKeyResults() BBB.2 Single product TestKey has only expired test events. Testkey status is " + tk.getTestKeyStatusType().getName() +  "?? Changing to Expired," + tk.toString() );
                            tk.setTestKeyStatusTypeId( TestKeyStatusType.EXPIRED.getTestKeyStatusTypeId() );
                            eventFacade.saveTestKey(tk); 
                            archiveIfNeeded( tk );
                            return out;                                            
                        } 
                        
                        // Error
                        if( te.getTestEventStatusType().getIsError() )
                        {
                            LogService.logIt("BaseDistManager.distributeTestKeyResults() BBB.3 Single product TestKey has only test events in Scoring Error or Report Error status. Testkey status is " + tk.getTestKeyStatusType().getName() +  "?? Changing to same status as test event," + tk.toString() );
                            tk.setTestKeyStatusTypeId( te.getTestEventStatusTypeId() );
                            eventFacade.saveTestKey(tk); 
                            archiveIfNeeded( tk );
                            return out;                                            
                        } 
                        
                        
                        // has at least one and it's valid. Continue. 
                    }                    

                    if( te.getTestEventStatusType().getIsCompleteOrHigher() && !te.getTestEventStatusType().getIsReportsCompleteOrHigher() )
                    {
                        String msg = "BaseDistManager.distributeTestKeyResults() BBB.4 test eventId=" + te.getTestEventId() + " is complete but it is not in a status that is ready for distribution but it is complete. Waiting. Testkey status is " + tk.getTestKeyStatusType().getName() + ", changing test key status to match testeventstatus so that it will continue the process in another batch.";
                        LogService.logIt( msg );
                        TestEventLog testEventLog = new TestEventLog();
                        testEventLog.setTestKeyId( te.getTestKeyId() );
                        testEventLog.setTestEventId(te.getTestEventId());
                        testEventLog.setLevel(0);
                        testEventLog.setLog( "BaseDistManager.distributeTestKeyResults() BBB.4 testKeyId=" + te.getTestKeyId() + ", " + msg ) ;
                        eventFacade.saveTestEventLog(testEventLog);

                        if( tk.getTestKeyStatusTypeId()!=te.getTestEventStatusTypeId() )
                        {
                            tk.setTestKeyStatusTypeId( te.getTestEventStatusTypeId() );
                            eventFacade.saveTestKey(tk); 
                        }
                        
                        hasUnreadyTestEvent = true;
                    }
                }

                if( hasUnreadyTestEvent )
                {
                    LogService.logIt("BaseDistManager.distributeTestKeyResults() BBB.5a TestKey has an unready test event. testKeyId=" + tk.getTestKeyId() );
                    return out;
                }

                
                // Reload Test Events. 
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );
                
                initTestKey( tk );
            }

            
            // No test events??
            if( tk.getTestEventList().isEmpty() )
            {
                if( tk.getExpireDate()!=null && tk.getExpireDate().before( new Date() ) )
                {
                    LogService.logIt("BaseDistManager.distributeTestKeyResults() CCC.1 TestKey is past expire date and has no valid test events of any kind but is expired. Testkey status is " + tk.getTestKeyStatusType().getName() +  "?? Changing to Expired," + tk.toString() );
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.EXPIRED.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    archiveIfNeeded( tk );
                    return out;                
                }

                LogService.logIt("BaseDistManager.distributeTestKeyResults() CCC.2 TestKey has no valid test events but is NOT Expired. testkeystatus is " + tk.getTestKeyStatusType().getName() +  "?? Since it is not expired, changing to Started. " + tk.toString() );
                tk.setTestKeyStatusTypeId( TestKeyStatusType.STARTED.getTestKeyStatusTypeId() );
                eventFacade.saveTestKey(tk);
                return out;                                   
            }                                
            
            boolean isOptionalAutoTest = tk.getTestKeySourceTypeId()==TestKeySourceType.OPTIONALAUTOTEST.getTestKeySourceTypeId();

            if( tk.getAuthorizingUserId()>0 && !isOptionalAutoTest )
            {
                boolean okToSend = okToSendTestKeyByMinScore( tk );
                
                if( okToSend )
                {
                    // LogService.logIt( "BaseDistManager.distributeTestKeyResults() tk.authUserId()=" + tk.getAuthorizingUserId()  );
                    String emto = tk.getEmailResultsTo();

                    // Product can override.
                    if( tk.getProduct().getStrParam9()!=null && !tk.getProduct().getStrParam9().isBlank() )
                        emto = tk.getProduct().getStrParam9();                

                    String txtto = tk.getTextResultsTo();
                    if( tk.getProduct().getStrParam10()!=null && !tk.getProduct().getStrParam10().isBlank() )
                        txtto = tk.getProduct().getStrParam10();                

                    // Email to Test Administrators.
                    if( !candFbkOnly && emto != null && !emto.isEmpty() )
                    {
                        String ems = emto.replaceAll( ",", ";" );

                        ems = ems.replaceAll(":", ";" );

                        ems = ems.replaceAll( "\\|", ";" );

                        String[] emails = ems.split( ";" );

                        List<String> emailLst = new ArrayList<>();

                        for( int i=0; i< emails.length; i++ )
                        {
                            if( emails[i] != null && emails[i].trim().length()>0 )
                            {
                                if( EmailUtils.validateEmailNoErrors( emails[i] ) )
                                    emailLst.add( emails[i].trim() );
                            }
                        }

                        // LogService.logIt( "BaseDistManager.distributeTestKeyResults() " + tk.toString() + ", mailing administrator emails to " + emto + ", ems=" + ems +", emails.length=" + emails.length + " emailLst.size()=" + emailLst.size() );
                        // LogService.logIt( "BaseDistManager.distributeTestKeyResults() " + tk.toString() + ", mailing administrator emails to " + emto + ", ems=" + ems +", emails.length=" + emails.length + " emailLst.size()=" + emailLst.size() + ", testKeyId=" + tk.getTestKeyId() );

                        //init = true;
                        // LogService.logIt( "BaseDistManager.distributeTestKeyResults() " + tk.toString() + ", mailing to " + emailLst.size() + " test administrators. EmailResultsTo=" + emto );

                        if( okToSendTestKeyByMinScore( tk ) )
                            emailTestResultsToAdministrator(tk, emailLst, null, null, 0 );

                        out[0] = emailLst.size();
                    }

                    // Text results to Test Administrators
                    if( !candFbkOnly && txtto != null && !txtto.isEmpty() )
                    {
                        String countryCode = null;

                        if( tk.getAuthUser()==null && tk.getAuthorizingUserId()>0 )
                        {
                            if( userFacade==null )
                                userFacade = UserFacade.getInstance();

                            tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
                        }


                        if( tk.getAuthUser()!=null )
                        {
                            countryCode = tk.getAuthUser().getCountryCode();

                            if( countryCode==null || countryCode.isBlank() )
                                countryCode = tk.getAuthUser().getIpCountry();
                        }

                        if( (countryCode==null || countryCode.isBlank()) )
                        {
                            if( tk.getUser()==null && tk.getUserId()>0 )
                            {
                                if( userFacade==null )
                                    userFacade = UserFacade.getInstance();

                                tk.setUser( userFacade.getUser( tk.getUserId() ));
                            }

                            if( tk.getUser()!=null )
                            {
                                countryCode = tk.getUser().getCountryCode();

                                if( countryCode==null || countryCode.isBlank() )
                                    countryCode = tk.getUser().getIpCountry();
                            }
                        }



                        String tms = txtto.replaceAll(",", ";" );

                        tms = tms.replaceAll(":", ";" );

                        tms = tms.replaceAll("\\|", ";" );

                        String[] txts = tms.split( ";" );

                        // String[] txts = tk.getTextResultsTo().split( ";" );

                        // LogService.logIt( "BaseDistManager.distributeTestKeyResults() phone numbers=" + tms + ", length=" + txts.length );

                        List<String> txtLst = new ArrayList<>();

                        for( int i=0; i< txts.length; i++ )
                        {
                            if( txts[i] != null && txts[i].trim().length()>0 && GooglePhoneUtils.isNumberValid( txts[i], countryCode ) )
                                txtLst.add( txts[i].trim() );
                            else
                                LogService.logIt( "BaseDistManager.distributeTestKeyResults() " + txtto + " contains AN INVALID phone number=" + txts[i] + ", converted=" + tms );
                        }

                        // LogService.logIt( "BaseDistManager.distributeTestKeyResults() have " + txtLst.size()  +  " VALID phone numbers." );
                        //if( !init )
                        //    initTestKey( tk );

                        if( !txtLst.isEmpty() && tk.getTestEventList() != null && !tk.getTestEventList().isEmpty() )
                        {

                            // LogService.logIt( "BaseDistManager.distributeTestKeyResults() " + tk.toString() + ", texting to " + txtLst.size() + ", test administrator phone numbers. " );                        
                            textTestResultsToAdministrator( tk, txtLst );
                            // LogService.logIt( "BaseDistManager.distributeTestKeyResults() Return from Texting." );

                            out[1] = txtLst.size();
                        }
                    }
                }

                // LogService.logIt( "BaseDistManager.distributeTestKeyResults() CCC testKeyId=" + tk.getTestKeyId() + ", hasEmailTestTaker=" + tk.getHasReportsToEmailTestTaker() );

                // Integer tempInteger = ReportUtils.getReportFlagIntValue( "suppressemailfbkreports", tk, tk.getSuborg(), tk.getOrg(), null );
                boolean suppressTestTakerEmails = !candFbkOnly && ReportUtils.getReportFlagBooleanValue("suppressemailfbkreports", tk, null, tk.getSuborg(), tk.getOrg(), null ); //  tempInteger!=null && tempInteger==1;
                
                // Email test reports to Test Takers.
                if( !suppressTestTakerEmails && tk.getHasReportsToEmailTestTaker() )
                {
                    LogService.logIt( "BaseDistManager.distributeTestKeyResults() CCC.2A Attempting to email test taker. testKeyId=" + tk.getTestKeyId() + ", fbkReportId=" + fbkReportId + ", candFbkOnly=" + candFbkOnly + ", out[2]=" + out[2] );
                    out[2] += emailReportsToTestTaker(tk, null, candFbkOnly, fbkReportId, testEventIdOnly, null );
                    LogService.logIt( "BaseDistManager.distributeTestKeyResults() CCC.2B BACK from email test taker. testKeyId=" + tk.getTestKeyId() + ", out[2]=" + out[2] );
                    //if( emailReportsToTestTaker( tk ) )
                    //    out[2]++;
                }
                else if( tk.getHasReportsToEmailTestTaker() )
                    LogService.logIt( "BaseDistManager.distributeTestKeyResults() CCC.3 Did NOT send to test taker because suppressTestTakerEmails=" + suppressTestTakerEmails + ", testKeyId=" + tk.getTestKeyId() + ", hasEmailTestTaker=" + tk.getHasReportsToEmailTestTaker() );
                

                if( !candFbkOnly && 
                    ((tk.getResultPostUrl()!=null && !tk.getResultPostUrl().isEmpty()) ||
                    (tk.getTestKeySourceTypeId()==TestKeySourceType.API.getTestKeySourceTypeId() && tk.getApiType().requiresReportNotification()) 
                    ) )
                {
                    postTestResults( tk );
                }
            }

            // Personal User Account
            else if( !candFbkOnly )
            {
                // LogService.logIt( "BaseDistManager.distributeTestKeyResults() Personal hasEmailTestTaker=" + tk.getHasReportsToEmailTestTaker() );
                
                if( tk.getHasResultsToEmailPersonalTestTaker() )
                {
                   if( emailResultsAndReportsToPersonalTestTaker( tk ) )
                        out[2]++;
                }

                // Email test reports to Test Takers.
                else if( tk.getHasReportsToEmailTestTaker() )
                {
                    out[2] += emailReportsToTestTaker(tk, null, candFbkOnly, fbkReportId, testEventIdOnly, null );
                    //if( emailReportsToTestTaker( tk ) )
                    //    out[2]++;
                }
            }

            tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() );
            tk.setErrorCnt(0);
            tk.setFirstDistComplete( 1 );

            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            eventFacade.saveTestKey(tk);

            Tracker.addResponseTime( "Distribute Test Key", new Date().getTime() - procStart.getTime() );
            Tracker.addDistributionFinish();
            
            if( tk.getTestKeyArchiveId()<=0 )
            {
                try
                {
                    if( archiveDelaySecs>0 )
                        Thread.sleep(archiveDelaySecs*1000 );
                    
                    (new EventArchiver()).archiveTestKey(tk);
                }
                catch( STException e )
                {}
            }

            return out;
        }

        catch( ScoringException e )
        {
            if( e.getSeverity()==ScoringException.PERMANENT )
            {
                LogService.logIt("BaseDistManager.distributeTestKeyResults() ZZZ.1 Saving Permanent Dist Error to TestKeyStatus. " + e.toString() + ", testKeyId=" + tk.getTestKeyId() );
                tk.setErrorCnt(MAX_DIST_ERRORS);
                handleDistErrorInTestKey(tk, "BaseDistManager.distributeTestKeyResults() ZZZ.1 " + e.toString() );                
            }
            else
            {
                LogService.logIt("BaseDistManager.distributeTestKeyResults() ZZZ.2 Non-Permanenet Dist Error.  " + e.toString() + ", testKeyId=" + tk.getTestKeyId() );
                handleDistErrorInTestKey(tk, "BaseDistManager.distributeTestKeyResults() ZZZ.2 " + e.toString() ); 
            }
            
            throw e;
        }        
        catch( Exception e )
        {
            LogService.logIt(e, "BaseDistManager.distributeTestKeyResults() ZZZ.3 " + e.toString() + ", testKeyId=" + (tk==null ? "null" : tk.getTestKeyId())  );
            if( ScoreUtils.isExceptionPermanent(e) )
                handleDistErrorInTestKey(tk, "BaseDistManager.distributeTestKeyResults() ZZZ.3 " + e.toString() + ", testKeyId=" + (tk==null ? "null" : tk.getTestKeyId())  );
            throw new STException( e );
        }
    }
    
    protected void archiveIfNeeded( TestKey tk ) throws Exception
    {
        if( tk.getTestKeyArchiveId()<=0 )
        {
            try
            {
                (new EventArchiver()).archiveTestKey(tk);
            }
            catch( STException e )
            {}                    
        }        
    }

    protected void handleDistErrorInTestKey( TestKey tk, String message)
    {
        try
        {
            Tracker.addDistributionError();

            tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_ERROR.getTestKeyStatusTypeId() );
            tk.setErrorCnt( tk.getErrorCnt()+1 );

            // tk.setFirstDistComplete( 1 );

            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            eventFacade.saveTestKey(tk);
            
            TestEventLog testEventLog = new TestEventLog();
            testEventLog.setTestKeyId( tk.getTestKeyId() );
            testEventLog.setLevel(0);
            testEventLog.setLog( "BaseDistManager.setDistErrorInTestKey() testKeyId=" + tk.getTestKeyId() + ", " + message ) ;
            if( testEventLog.getTestKeyId()>0 )
                eventFacade.saveTestEventLog(testEventLog);
            

            // Next, need to notify
        }

        catch( Exception e )
        {
            LogService.logIt(e, "BaseDistManager.setDistErrorInTestKey() testKeyId=" + tk.getTestKeyId() + ", message=" + message );
        }

    }
    
    protected boolean okToSendTestKeyByMinScore( TestKey tk ) throws Exception
    {
        if( (tk.getSuborg()!=null && tk.getSuborg().getReportFlags()!=null && tk.getSuborg().getReportFlags().contains("minovrscr4msg")) ||
                (tk.getOrg()!=null && tk.getOrg().getReportFlags()!=null && tk.getOrg().getReportFlags().contains("minovrscr4msg")) )
        {
            Float rr = ReportUtils.getReportFlagFloatValue("minovrscr4msg", null, null, tk.getSuborg(), tk.getOrg(), null );
            
            if( rr<=0 )
                return true;
            
            // battery
            if( tk.getBatteryId()>0 )
            {
                // no battery score. Include
                if( tk.getBatteryScore()==null )
                    return true;
                
                BatteryScoreType bst = BatteryScoreType.getValue(tk.getBatteryScore().getBatteryScoreTypeId());

                // battery score not scored, include
                if( !bst.needsScore() )
                    return true;
                
                // test on Battery Score
                return tk.getBatteryScore().getScore()>=rr; 
            }
            
            // no test events, include
            if( tk.getTestEventList()==null )
                return true;
            
            // if any te has score above threshold
            for( TestEvent te : tk.getTestEventList() )
            {
                if( te.getOverallScore()>=rr )
                    return true;
            }
            return false;            
        }

        // no flag, return true;
        return true;
    }
    
    
    protected void initTestKey( TestKey tk ) throws Exception
    {
        if( eventFacade == null ) 
            eventFacade = EventFacade.getInstance();

        if( userFacade == null ) 
            userFacade = UserFacade.getInstance();

        if( tk.getUser()==null )
            tk.setUser( userFacade.getUser( tk.getUserId() ) );

        if( tk.getAuthorizingUserId()>0 && tk.getAuthUser()==null )
            tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));

        if( tk.getOrg()==null )
            tk.setOrg( userFacade.getOrg( tk.getOrgId() ));

        if( tk.getProduct()==null )
            tk.setProduct( eventFacade.getProduct( tk.getProductId() ));
        
        if( tk.getSuborgId()>0 && tk.getSuborg()==null )
            tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));

        if( tk.getTestEventList()==null )
            tk.setTestEventList(eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ));

        if( tk.getBatteryId()>0 )
        {
            if( tk.getBattery()==null )
                tk.setBattery( eventFacade.getBattery( tk.getBatteryId() ));
            
            
            if( tk.getBatteryProduct()==null )
                tk.setBatteryProduct( tk.getProduct() );
            // tk.setBatteryProduct( eventFacade.getProduct( tk.getProductId() ) );
            
            if( tk.getBatteryScore()==null )
                tk.setBatteryScore( tk.getBattery()!=null && tk.getBattery().getBatteryScoreType().needsBatteryScoreObject() ? eventFacade.getBatteryScoreForTestKey( tk.getTestKeyId() ) : null );
        
            if( tk.getBattery()!=null )
                tk.setTestEventList( tk.getBattery().setTestEventsInOrder( tk.getTestEventList() ));
        }

        if( tk.getTestKeyProctorTypeId()>0 && tk.getProctorEntryList()==null )
        {
                if( proctorFacade==null )
                    proctorFacade=ProctorFacade.getInstance();
                
            tk.setProctorEntryList( proctorFacade.getProctorEntryListForTestKey(tk.getTestKeyId()));
            for( ProctorEntry pe : tk.getProctorEntryList() )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                pe.setProctorUser( userFacade.getUser( pe.getProctorUserId() ));
            }
        }
        
        if( (tk.getTestKeyProctorTypeId()>0 || tk.getOnlineProctoringType().getIsAnyPremium()) && tk.getProctorSuspensionList()==null)
        {
            if( proctorFacade==null )
                proctorFacade=ProctorFacade.getInstance();

            tk.setProctorSuspensionList( proctorFacade.getProctorSuspensionListForTestKey( tk.getTestKeyId() ) );
            for( ProctorSuspension ps : tk.getProctorSuspensionList() )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                ps.setProctorUser(userFacade.getUser( ps.getProctorUserId() ));
                if( ps.getProctorSuspensionStatusTypeId()>0 && ps.getRemoveUserId()>0 )
                    ps.setRemoveUser( userFacade.getUser( ps.getRemoveUserId() ));
            }
        }
        
        for( TestEvent te : tk.getTestEventList() )
        {
            //if( !te.getTestEventStatusType().getIsReportsCompleteOrHigher() )
            //    continue;

            if( te.getTestEventScoreList()==null )
                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId() , true ));

            for( TestEventScore tes : te.getTestEventScoreList() )
            {
                if( tes.getReportId()<=0 )
                {
                    if( tes.getTestEventScoreType().getIsReport() )
                    {
                        String m = "BaseDistManager.initTestKey() Report-type TestEventScore has reportId=" + tes.getReportId() + ", testEventId=" + te.getTestEventId();
                        LogService.logIt(m);
                        TestEventLogUtils.createTestKeyLogEntry( te.getTestKeyId(), te.getTestEventId(), 0, m, null, null);
                    }
                    continue;
                }
                
                if( tes.getTestEventScoreType().getIsReport() && tes.getReport()==null )
                {
                    tes.setReport( eventFacade.getReport( tes.getReportId() ));
                }
            }

            if( te.getProduct()==null )
                te.setProduct( eventFacade.getProduct( te.getProductId() ));

            if( te.getProduct()!=null && te.getProduct().getProductType().getUsesSimDescriptor() && te.getProduct().getTempDate()==null )
                te.getProduct().setTempDate( eventFacade.getLastScoringUpdate( te.getProduct().getLongParam1() ));
            
            if( tk.getBatteryProduct()==null )
                tk.setBatteryProduct( te.getProduct() );

            if( te.getReportId() > 0 && te.getReport()==null )
                te.setReport( eventFacade.getReport( te.getReportId()));

            if( te.getProduct().getLongParam3()>0 && te.getReport2()==null )
                te.setReport2( eventFacade.getReport( te.getProduct().getLongParam3() ));

            if( te.getProduct().getLongParam5()>0 && te.getReport3()==null )
                te.setReport3( eventFacade.getReport( te.getProduct().getLongParam5() ));
        }
    }

    protected int emailTestResultsToAdministrator( TestKey tk, List<String> emailLst, String frcSubj, String noteHtml, int addLimitedAccessLinkInfo) throws Exception
    {
        try
        {
            // LogService.logIt( "BaseDistManager.emailTestResultsToAdministrator() START testKeyId=" + (tk==null ? "test key is null" : tk.getTestKeyId() ) + ", addLimitedAccessLinkInfo=" + addLimitedAccessLinkInfo + ", frcSubj=" + (frcSubj==null ? "null" : frcSubj) );
                        
            ScoreFormatter hsf;

            if( tk.getTestEventList()== null )
                return 0;

            if( addLimitedAccessLinkInfo!=0 )
            {
                if( tk.getOrg()==null )
                {
                    if( userFacade==null )
                        userFacade=UserFacade.getInstance();

                    tk.setOrg( userFacade.getOrg( tk.getOrgId() ));
                }

                if( tk.getOrg().getCandidateAudioVideoViewTypeId()==CandidateAudioVideoViewType.ALL_OK.getCandidateAudioVideoViewTypeId() )
                    addLimitedAccessLinkInfo=0;
            }
            
            if( addLimitedAccessLinkInfo!=0 )
            {
                User lu;
                
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                
                boolean needed = false;
                for( String em : emailLst )
                {
                    lu = userFacade.getUserByEmailAndOrgId(em, tk.getOrgId() );
                    
                    if( lu==null || !lu.getRoleType().getCanViewUploadedMedia() )
                        needed = true;       
                }
                
                if( !needed )
                   addLimitedAccessLinkInfo=0;   
            }
                
            
            
            // String textMsg = null;

            String subj = null;

            String content;

            StringBuilder sb = new StringBuilder();

            int count = 0;

            Locale locale =  getEmailLocaleFromTestKey( tk );

            String[] params = null;

            boolean tog = true;
            
            List<TestEventScore> otesl = new ArrayList<>();
            
            // One report per test event. Use report1 if present, otherwise use report2 if present.
            for( TestEvent te : tk.getTestEventList() )
            {
                if( te.getProduct()==null )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    te.setProduct(eventFacade.getProduct( te.getProductId() ));
                }

                if( te.getTestEventScoreList()==null )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ));
                }
                
                //Skip incomplete test events in a battery.
                if( tk.getBatteryId()>0 && !te.getTestEventStatusType().getIsReportsCompleteOrHigher() )
                    continue;

                if( locale == null && te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
                    locale = I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

                else if( locale == null && te.getReport()!= null && te.getReport().getLocaleStr()!= null && !te.getReport().getLocaleStr().isEmpty() )
                    locale = I18nUtils.getLocaleFromCompositeStr( te.getReport().getLocaleStr() );

                else if( locale == null && te.getReport2()!= null && te.getReport2().getLocaleStr()!= null && !te.getReport2().getLocaleStr().isEmpty() )
                    locale = I18nUtils.getLocaleFromCompositeStr( te.getReport2().getLocaleStr() );

                if( te.getTestEventResponseRatingList()==null )
                {
                    if( testEventResponseRatingFacade==null )
                        testEventResponseRatingFacade=TestEventResponseRatingFacade.getInstance();
                    te.setTestEventResponseRatingList( testEventResponseRatingFacade.getTestEventResponseRatingsForTestEventId(te.getTestEventId()));
                }

                if( te.getTestEventResponseRatingList()!=null && !te.getTestEventResponseRatingList().isEmpty() )
                {
                    TestEventResponseRatingUtils.setTestEventResponseRatingNames( tk.getOrg(), te.getProduct(), te.getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()), locale, te.getTestEventResponseRatingList());
                    // Map<String,String> avgRatingMap = TestEventResponseRatingUtils.getOverallAverageRatingMap( te.getTestEventResponseRatingList(), rptLocale );
                }

                
                
                hsf = null;
                
                // use test key formatter from report 1 if present.
                if( te.getReport()!=null && te.getReport().getReportTemplateType().getIsCustom() && te.getReport().getEmailFormatterClass()!= null && !te.getReport().getEmailFormatterClass().isEmpty() )
                {
                    // LogService.logIt( "BaseDistManager.emailTestResultsToAdministrator() Using Custom admin email class " + te.getReport().getEmailFormatterClass() );
                    Class<ScoreFormatter> cls = (Class<ScoreFormatter>) Class.forName( te.getReport().getEmailFormatterClass() );

                    Constructor ctor = cls.getDeclaredConstructor();
                    hsf =(ScoreFormatter) ctor.newInstance();
                }

                // if not there, use from report 2 if present.
                if( hsf==null && te.getReport2()!=null && te.getReport2().getReportTemplateType().getIsCustom() && te.getReport2().getEmailFormatterClass()!= null && !te.getReport2().getEmailFormatterClass().isEmpty() )
                {
                        //LogService.logIt( "BaseDistManager.emailTestResultsToAdministrator() Using Report 2 Custom admin email class " + te.getReport2().getEmailFormatterClass() );
                        Class<ScoreFormatter> cls = (Class<ScoreFormatter>) Class.forName( te.getReport2().getEmailFormatterClass() );

                        Constructor ctor = cls.getDeclaredConstructor();
                        hsf = (ScoreFormatter) ctor.newInstance();
                }
                

                if( hsf==null )
                {
                    // LogService.logIt( "BaseDistManager.emailTestResultsToAdministrator() Using generic email class " );
                    hsf = new StandardHtmlScoreFormatter();
                }

                if( te.getProductId()>0 )
                    te.setProfile( ProfileUtils.getLiveProfileForProductIdAndOrgId( te.getTestEventId(), te.getProductId(), te.getOrgId(), ProfileUsageType.REPORT_RANGES.getProfileUsageTypeId() ) );

                // LogService.logIt( "BaseDistManager.emailTestResultsToAdministrator() TestEventId=" + (te==null ? "null" : te.getTestEventId() + ", TestEventScoreList=" + (te.getTestEventScoreList()==null ? "null" : te.getTestEventScoreList().size() + ", otes=" + (te.getOverallTestEventScore()==null ? "null" : "not null"))) );
                
                // Always init!
                hsf.init(tk, te, te.getReport(), locale, addLimitedAccessLinkInfo );

                content = hsf.getEmailContent( tog, count==0, noteHtml ); // MessageFactory.getStringMessage(locale, "g.ResultEmailContent1", params);

                if( content == null || content.isEmpty() )
                    continue;

                if( count>0 )
                    sb.append( getHtmlTableSpacerRow( BaseScoreFormatter.BLANK_ROW_STYLE ) );

                count++;

                sb.append(content );

                //LogService.logIt( "BaseDistManager.emailTestResultsToAdministrator() BEFORE hsf.class=" + hsf.getClass().getName() );
                subj = hsf.getEmailSubj(); // MessageFactory.getStringMessage(locale, anon ? "g.ResultEmailSubjAnon" : "g.ResultEmailSubj", params);
                //LogService.logIt( "BaseDistManager.emailTestResultsToAdministrator() AFTER subj=" + subj + ", hsf.class=" + hsf.getClass().getName() );

                
                if( locale == null )
                    locale = hsf.getLocale();

                params = hsf.getParams();
                
                if( te.getOverallTestEventScore()!=null )
                    otesl.add( te.getOverallTestEventScore());
            }

            if( locale == null )
                locale = Locale.US;


            if( sb.length()==0 )
                return 0;

            if( count>1 )
            {
                Battery b = tk.getBattery();                
                String nm = tk.getBatteryProduct().getName();
                
                if( b!=null && b.getBatteryType().equals( BatteryType.MULTIUSE ) && b.getName()!=null && !b.getName().isEmpty() )
                    nm = b.getName();
                
                params[0] = nm;                
                
                subj = MessageFactory.getStringMessage( locale , "g.ResultEmailSubjMultiple" , params );
            }

            if( frcSubj != null && !frcSubj.isEmpty() )
                subj = frcSubj;

            // LogService.logIt( "BaseDistManager.emailTestResults() subj=" + subj + ", content=" + getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale ) );

            EmailUtils emailUtils = EmailUtils.getInstance();

            User adminUser = new User();

            LimitedAccessLink limitedAccessLink;
            String contentToUse;
            
            int sent = 0;
            List<String> eml = new ArrayList<>();
            // send the email!
            for( String email : emailLst )
            {
                email = EmailUtils.cleanEmailAddress(email);
                
                if( email==null || email.isBlank() )
                {
                    LogService.logIt( "BaseDistManager.emailTestResults() Skipping blank or empty email. tk.getEmailResultsTo()=" + tk.getEmailResultsTo() + ", testKeyId=" + tk.getTestKeyId() );
                    continue;
                }
                
                if( eml.contains( email.toLowerCase() ))
                {
                    LogService.logIt( "BaseDistManager.emailTestResults() Skipping sending email because email is a duplicate: " + email + ", tk.getEmailResultsTo()=" + tk.getEmailResultsTo() + ", testKeyId=" + tk.getTestKeyId() );
                    continue;
                }
                
                eml.add(email.toLowerCase() );
                
                limitedAccessLink = null;
                //if( EmailBlockFacade.getInstance().hasEmailBlock( email ) )
                //{
                //    LogService.logIt( "BaseDistManager.emailTestResults() Skipping because email is blocked. " + email );
                //    continue;
                //}
                if( !EmailUtils.validateEmailNoErrors(email))
                {
                    LogService.logIt( "BaseDistManager.emailTestResults() Skipping sending email because email is invalid: " + email );
                    continue;
                }
                

                EmailBlockFacade emailBlockFacade = EmailBlockFacade.getInstance();
                if( emailBlockFacade.hasEmailBlock(email.trim(), true, true ) )
                {
                    LogService.logIt( "BaseDistManager.emailTestResults() Skipping. Email Fully blocked for " + email );
                    continue;
                }
                
                
                if( addLimitedAccessLinkInfo==1 )
                    limitedAccessLink = findCreateLimitedAccessLinkForEmailAndTestEventId( email, tk.getTestKeyId(), tk.getOrgId() );
                
                contentToUse =  sb.toString(); //limitedAccessLink==null ? conten

                if( limitedAccessLink!=null )
                {
                    LogService.logIt( "BaseDistManager.emailTestResults() Replacing LimitedAccessLink with " + limitedAccessLink.getLimitedAccessLinkIdIdEncrypted() );
                    contentToUse = StringUtils.replaceStr( contentToUse, "[LIMITEDACCESSLINKIDENC]", limitedAccessLink.getLimitedAccessLinkIdIdEncrypted() );
                }
                
                else
                    contentToUse = StringUtils.replaceStr( contentToUse, "[LIMITEDACCESSLINKIDENC]", "" );
                    
                // LogService.logIt("BaseDistManager.emailTestResultsToAdministrator() testKeyId=" + tk.getTestKeyId() + ", Sending email to " + email ); // + ", " + getHtmlTableStart( locale ) + contentToUse + getHtmlTableEnd( locale ) );
                
                // prepare to send
                Map<String, Object> emailMap = new HashMap<>();

                emailMap.put( EmailUtils.MIME_TYPE , "text/html" );

                emailMap.put( EmailUtils.SUBJECT, subj );

                emailMap.put( EmailUtils.CONTENT, getHtmlTableStart( locale ) + contentToUse + getHtmlTableEnd( locale ) );

                emailMap.put( EmailUtils.TO, email );

                // emailMap.put( EmailUtils.FROM, RuntimeConstants.getStringValue("support-email") + "|" + MessageFactory.getStringMessage( locale , "g.SupportEmailKey", null ) );
                emailMap.put( EmailUtils.FROM, RuntimeConstants.getStringValue("support-email") );

                if( userActionFacade==null)
                    userActionFacade = UserActionFacade.getInstance();

                adminUser.setEmail(email);

                userActionFacade.saveMessageAction(adminUser, subj, tk.getTestKeyId(), tk.getOrgAutoTestId(), UserActionType.SENT_EMAIL.getUserActionTypeId() );

                try
                {
                    emailUtils.sendEmail( emailMap );
                    Tracker.addEmailSent();                    
                    sent++;
                }

                catch( STException e )
                {
                    // skip
                    LogService.logIt("BaseDistManager.emailTestResults() Error sending Email for " + tk.toString()  + ", to address: " + email );
                }

                Thread.sleep( 100 );
            }
            
            if( sent>0 && !otesl.isEmpty() )
            {
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                
                for( TestEventScore otes : otesl )
                {
                    otes.setDateParam2( new Date() );
                    eventFacade.saveTestEventScore(otes);
                }
            }
            
            return sent;

            // LogService.logIt("BaseDistManager.emailTestResultsToAdministrator() FINISH testKeyId=" + (tk==null ? "test key is null" : tk.getTestKeyId() ) );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "BaseDistManager.emailTestResults() " + tk.toString() );

            throw new STException( e );
        }
    }
    
    protected void textTestResultsToAdministrator( TestKey tk, List<String> textLst ) throws Exception
    {
        try
        {
            ScoreFormatter hsf = null;

            if( tk.getTestEventList()== null )
                return;

            String content = null;

            StringBuilder sb = new StringBuilder();

            Locale locale =  getEmailLocaleFromTestKey( tk );

            if( locale == null && tk.getTestEventList()!=null )
            {
                for( TestEvent te : tk.getTestEventList() )
                {
                    if( locale == null && te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
                        locale = I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

                    else if( locale == null && te.getReport()!= null && te.getReport().getLocaleStr()!= null && !te.getReport().getLocaleStr().isEmpty() )
                        locale = I18nUtils.getLocaleFromCompositeStr( te.getReport().getLocaleStr() );

                    else if( locale == null && te.getReport2()!= null && te.getReport2().getLocaleStr()!= null && !te.getReport2().getLocaleStr().isEmpty() )
                        locale = I18nUtils.getLocaleFromCompositeStr( te.getReport2().getLocaleStr() );
                }
            }

            if( locale == null )
                locale = Locale.US;


            if( tk.getAuthUser()==null && tk.getAuthorizingUserId()>0 )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                
                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
            }
            
            if( tk.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();                
                tk.setOrg( userFacade.getOrg( tk.getOrgId() ));
            }

            if( tk.getOrg()!=null && !tk.getOrg().getIsSmsOk() )
                return;
            
            
            String countryCode = null;
            
            if( tk.getAuthUser()!=null )
            {
                countryCode = tk.getAuthUser().getCountryCode();
                
                if( countryCode==null || countryCode.isBlank() )
                    countryCode = tk.getAuthUser().getIpCountry();
            }
            
            if( countryCode==null || countryCode.isBlank() )
            {
                countryCode = tk.getUser().getCountryCode();
                
                if( countryCode==null || countryCode.isBlank() )
                    countryCode = tk.getUser().getIpCountry();
            }
                        
            //int count = 0;
            Report r = null;
            TestEvent te = null;
            List<TestEventScore> otesl = new ArrayList<>();
            
            if( tk.getTestEventList()!=null && tk.getTestEventList().size()==1 )
            {
                te = tk.getTestEventList().get(0);
                //for( TestEvent te : tk.getTestEventList() )
                //{
                if( te.getReportId()>0 && te.getReport()==null )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    te.setReport( eventFacade.getReport( te.getReportId() ) );
                }                
                r = te.getReport();

                if( te.getReport()!=null && te.getReport().getReportTemplateType().getIsCustom() && te.getReport().getEmailFormatterClass()!= null && !te.getReport().getEmailFormatterClass().isBlank() )
                {
                    Class<ScoreFormatter> cls = (Class<ScoreFormatter>) Class.forName( te.getReport().getEmailFormatterClass() );
                    Constructor ctor = cls.getDeclaredConstructor();
                    hsf = (ScoreFormatter) ctor.newInstance();
                    // hsf = (ScoreFormatter) cls.newInstance();
                }
                
                for( TestEvent tex : tk.getTestEventList() )
                {
                    if( tex.getOverallTestEventScore()!=null )
                        otesl.add(tex.getOverallTestEventScore());
                }                    
                    //if( hsf!=null )
                    //    break;
                //}
            }
            
            else if( tk.getTestEventList()!=null )
            {
                for( TestEvent tex : tk.getTestEventList() )
                {
                    if( tex.getOverallTestEventScore()!=null )
                        otesl.add(tex.getOverallTestEventScore());
                }
                
                for( TestEvent tex : tk.getTestEventList() )
                {                    
                    if( tex.getReportId()>0 )
                    {
                        te=tex;
                        if( te.getReport()==null )
                        {
                            if( eventFacade==null )
                                eventFacade=EventFacade.getInstance();
                            te.setReport( eventFacade.getReport( te.getReportId() ) );                            
                        }

                        
                        r=te.getReport();                        
                        break;
                    }
                }
            }
            
            
            
            if( hsf == null )
                hsf = new StandardHtmlScoreFormatter();

            hsf.init(tk, te, r, locale, 0 );
            content = hsf.getTextContent(); // MessageFactory.getStringMessage(locale, "g.ResultEmailContent1", params);

                //if( content == null || content.isEmpty() )
                //    continue;

                //count++;

            sb.append(content );

                //sb.append( ", " );
            //}

            if( sb.length()==0 )
                return;

            if( userActionFacade==null)
                userActionFacade = UserActionFacade.getInstance();

            User u = new User();

            boolean smsOk;
            
            List<String> ttl = new ArrayList<>();
            
            int tsent = 0;
            
            for( String ph : textLst )
            {
                try
                {      
                    if( ph==null || ph.isBlank() )
                        continue;
                    if( ttl.contains(ph))
                        continue;
                    ttl.add(ph);
                    
                    u.setPhonePrefix( ph );

                    smsOk = GooglePhoneUtils.getIsPhoneNumberAllowedForSms( ph, tk.getOrg(), u, tk.getAuthUser());
                    if( !smsOk )
                    {
                        LogService.logIt("BaseDistManager.textTestResultsToAdministrator() texting to international number for this org is not allowed. tkid=" + tk.getTestKeyId() + ", tk.userId=" + tk.getUserId() + ", phone=" + ph );
                        continue;                        
                    }
                    
                    userActionFacade.saveMessageAction(tk.getUser(), sb.toString(), tk.getTestKeyId(), tk.getOrgAutoTestId(), UserActionType.SENT_TEXT.getUserActionTypeId() );

                    int sent = TwilioSmsUtils.sendTextMessageViaThread(ph, countryCode, locale, null, sb.toString() );
                                        
                    // PhoneUtils.sendTextMessage( ph, null, sb.toString() );
                    if( sent>0 )
                    {
                        tsent+= sent;
                        Tracker.addTextMessageSent();
                    }
                    else
                        LogService.logIt( "BaseDistManager.textTestResults() Unable to send text message. testKeyId=" + tk.getTestKeyId() + " msg was " + content + " to address: " + ph + ", sent=" + sent );
                }

                catch(STException e )
                {
                    // skip
                    LogService.logIt( e, "BaseDistManager.textTestResults() Error sending text message. " + tk.toString() + " msg was " + content + " to address: " + ph );
                    Tracker.addTextMessageFailure();
                }
                
                if( tsent>0 && !otesl.isEmpty()  )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();

                    for( TestEventScore otes : otesl )
                    {
                        otes.setDateParam2( new Date() );
                        eventFacade.saveTestEventScore(otes);
                    }
                }

                Thread.sleep( 100 );

            }
            // do the texting here.

        }

        catch( Exception ee )
        {
            LogService.logIt(ee, "BaseDistManager.textTestResults() " + tk.toString() );

            throw new STException( ee );
        }
    }

    public int emailReportsToTestTaker( TestKey tk, TestEvent teToUse, boolean candFbkOnly, long fbkReportId, long testEventIdOnly, Date maxLastCandidateSendDate) throws Exception
    {

        // LogService.logIt("BaseDistManager.emailReportsToTestTaker() AAA.1 STARTING " + ", candFbkOnly=" + candFbkOnly + ", fbkReportId=" + fbkReportId + ", testEventIdOnly=" + testEventIdOnly + ", " + (tk.getUser()==null ? "user is null " : "user anony=" + tk.getUser().getUserType().getAnonymous() + ", email=" + tk.getUser().getEmail() + ", valid=" + EmailUtils.validateEmailNoErrors( tk.getUser().getEmail() ) )  );

        if( teToUse!=null && testEventIdOnly!=teToUse.getTestEventId() )
            teToUse=null;
        
        if( teToUse==null && !tk.getHasReportsToEmailTestTaker() )
        {
            LogService.logIt( "BaseDistManager.emailReportsToTestTaker() BBB.1 Skipping TK does not have any reports to email testtaker." );
            return 0;
        }
        
        if( teToUse!=null && teToUse.getReport()==null )
        {
            LogService.logIt( "BaseDistManager.emailReportsToTestTaker() BBB.2  Skipping teToUse.report is null" );
            return 0;
        }

        if( teToUse!=null && teToUse.getReport().getEmailTestTaker()!=1 )
        {
            LogService.logIt("BaseDistManager.emailReportsToTestTaker() BBB.3  teToUse.report (reportId=" + teToUse.getReport().getReportId() + " does not email testtaker." );
            return 0;
        }
        
        if( tk.getUser()!=null && !tk.getUser().getUserType().getNamed() )
        {
            LogService.logIt( "BaseDistManager.emailReportsToTestTaker() BBB.4  Skipping because user is not named (so email is not a valid email)." );
            return 0;
        }

        if( tk.getUser()==null || tk.getUser().getEmail()==null || tk.getUser().getEmail().isEmpty() )
        {
            LogService.logIt( "BaseDistManager.emailReportsToTestTaker() BBB.5  Skipping because no user or no email. TestKeyId=" + tk.getTestKeyId() + ", user is " + (tk.getUser()==null ? "null" : "not null, email=" + tk.getUser().getEmail()) );
            return 0;
        }

        String email = EmailUtils.cleanEmailAddress(tk.getUser().getEmail());
        if( !EmailUtils.validateEmailNoErrors( email ) )
        {
            LogService.logIt("BaseDistManager.emailReportsToTestTaker() BBB.6  Skipping because email invalid. email=" + email + ", TestKeyId=" + tk.getTestKeyId() );
            return 0;
        }

        if( EmailBlockFacade.getInstance().hasEmailBlock(email, false, false ) )
        {
            LogService.logIt("BaseDistManager.emailReportsToTestTaker() BBB.7  Skipping because email is blocked.  email=" + email + ", TestKeyId=" + tk.getTestKeyId() );
            return 0;
        }

        String subj;

        String content;

        //String textContent = null;

        StringBuilder fullC; //  = new StringBuilder();

        int count=0;
        //int teCount = 0;

        Locale locale = null;

        if( tk.getUser().getLocaleToUseDefaultNull()!=null ) //  tk.getUser().getLocaleStr()!= null && !tk.getUser().getLocaleStr().isEmpty() )
            locale = tk.getUser().getLocaleToUseDefaultNull();

        boolean tog = true;

        // List<Report> rl = new ArrayList<>();

        Report rptToUse;
        ScoreFormatter hsf;
        EmailUtils emailUtils = null;
        Map<String, Object> emailMap;
        String html;
        
        if( tk.getOrg()==null )
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            tk.setOrg( userFacade.getOrg( tk.getOrgId()));
        }
        
        if( tk.getSuborgId()>0 && tk.getSuborg()==null )
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            tk.setSuborg( userFacade.getSuborg( tk.getSuborgId()));
        }
        
        String fromAddr = tk.getOrg().getHasCustomSupportSendEmail() ? tk.getOrg().getSupportSendEmail() : RuntimeConstants.getStringValue("support-email");

        List<TestEventScore> rtesl;
        
        List<TestEvent> rtel;
        
        if( teToUse!=null )
        {
            rtel = new ArrayList<>();
            rtel.add(teToUse);
        }
        else
            rtel = tk.getTestEventList();
        
        try
        {
            for( TestEvent te : rtel )
            {
                // skip test event if restricted.
                if( testEventIdOnly>0 && te.getTestEventId()!=testEventIdOnly )
                {
                    LogService.logIt("BaseDistManager.emailReportsToTestTaker() CCC.1  Skipping testEventId=" + te.getTestEventId() + " because it does not match testEventIdOnly=" + testEventIdOnly + ", TestKeyId=" + tk.getTestKeyId() );
                    continue;
                }
                
                if( te.getProduct()==null )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    te.setProduct(eventFacade.getProduct(te.getProductId()));
                }
                
                if( locale == null && te.getLocaleStr()!=null && !te.getLocaleStr().isEmpty() )
                    locale = I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() );
                
                if( te.getTestEventScoreList()==null )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    te.setTestEventScoreList(eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true));
                }

                rtesl = new ArrayList<>();
                
                for( TestEventScore tes : te.getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() ) )
                {
                    if( tes.getReport()==null && tes.getReportId()>0 )
                    {
                        if( eventFacade==null )
                            eventFacade=EventFacade.getInstance();
                        tes.setReport( eventFacade.getReport( tes.getReportId() ));
                    }
                    
                    // email test taker means it's a feedback report.
                    // if( tes.getHasReport() && tes.getReport()!=null && tes.getReport().getEmailTestTaker()==1 )
                    if( tes.getReport()!=null && tes.getReport().getEmailTestTaker()==1 )
                    {
                        // this is ok. Add to rtesl
                        if( candFbkOnly && fbkReportId>0 && (fbkReportId==tes.getReportId() || fbkReportId>=999999) )
                        {
                            // LogService.logIt("BaseDistManager.emailReportsToTestTaker() CCC.2 Found TES for feedback reportId=" + fbkReportId + ", testKeyId=" + tk.getTestKeyId() + ", testEventId=" + te.getTestEventId() + ", testEventScoreId=" + tes.getTestEventScoreId() ); 
                        }
                        
                        // fbkReportId not specified and candidateFbkOnly and tes.reportId is not correct. Check for custom.
                        else if( candFbkOnly && RuntimeConstants.getLongValue("feedbackReportId")!=tes.getReportId() )
                        {
                            // check for a custom feedback report
                            int customFeedbackReportId = ReportUtils.getReportFlagIntValue("fbkreportid", null, te.getProduct(), tk.getSuborg(), tk.getOrg(), null );
                            
                            // if it's also not a customFeedbackReport. Else it's OK
                            if( tes.getReportId()!=customFeedbackReportId )
                            {
                                LogService.logIt("BaseDistManager.emailReportsToTestTaker() CCC.3 Skipping tes.reportId=" + tes.getReportId() + " because it doesn't match report flag customFeedbackReportId=" + customFeedbackReportId + " or RuntimeConstants.feedbackReportId=" + RuntimeConstants.getLongValue("feedbackReportId") + ", testKeyId=" + tk.getTestKeyId() + ", testEventId=" + te.getTestEventId() + ", testEventScoreId=" + tes.getTestEventScoreId() ); 
                                continue;
                            }
                        }
                        
                        // else not candFbkOnly or tes.reportId=default feedback report id                        
                        rtesl.add( tes );
                    }
                    // else
                    //     LogService.logIt("BaseDistManager.emailReportsToTestTaker() ccc.5 Skipping report TestEventScore testEventScoreId=" + tes.getTestEventScoreId() + ", tes.reportId=" + tes.getReportId() + ", tes.report=" + (tes.getReport()==null ? "null" : "not null, emailResultsToTaker=" + tes.getReport().getEmailTestTaker() + ", tes.getHasReport()=" + tes.getHasReport()) + ", testKeyId=" + tk.getTestKeyId() + ", testEventId=" + te.getTestEventId() ); 
                    
                }

                // LogService.logIt("BaseDistManager.emailReportsToTestTaker() DDD.1 rtesl.size=" + rtesl.size() + ", testKeyId=" + tk.getTestKeyId() + ", testEventId=" + te.getTestEventId() ); 
                
                // Will send a separate email for each matching report.
                for( TestEventScore tes : rtesl )
                {
                    // rl.clear();
                    rptToUse = tes.getReport();
                    
                    if( rptToUse==null || rptToUse.getEmailTestTaker()!=1 )
                        continue;
                    
                    if( maxLastCandidateSendDate!=null && tes.getDateParam1()!=null && tes.getDateParam1().after(maxLastCandidateSendDate) )
                        continue;
                    
                    hsf = null;
                    //teCount = 0;
                    fullC = new StringBuilder();

                    if( locale == null &&  rptToUse.getLocaleStr()!=null && !rptToUse.getLocaleStr().isEmpty() )
                        locale = I18nUtils.getLocaleFromCompositeStr( rptToUse.getLocaleStr() );

                    if( rptToUse.getTesttakerEmailFormatterClass()!= null && !rptToUse.getTesttakerEmailFormatterClass().isEmpty() )
                    {
                        LogService.logIt( "BaseDistManager.emailReportsToTestTaker() CCC.1  using class: " + rptToUse.getTesttakerEmailFormatterClass() );

                        Class<ScoreFormatter> cls = (Class<ScoreFormatter>) Class.forName( rptToUse.getTesttakerEmailFormatterClass() );

                        Constructor ctor = cls.getDeclaredConstructor();
                        hsf = (ScoreFormatter) ctor.newInstance();
                    }

                    if( hsf == null )
                        hsf = new StandardTestTakerHtmlScoreFormatter();

                    if( locale == null )
                        locale = Locale.US;

                    hsf.init(tk, te, rptToUse, locale, 0 );

                    content = hsf.getEmailContent( tog, true, null ); // MessageFactory.getStringMessage(locale, "g.ResultEmailContent1", params);

                    //tog = !tog;

                    if( content == null || content.isEmpty() )
                    {
                        LogService.logIt( "BaseDistManager.emailReportsToTestTaker() CCC.2 Skipping no content. rptToUse=" + rptToUse.getReportId() + ", " + te.toString() );
                        continue;
                    }

                    //if( teCount > 0 )
                    //   fullC.append( getHtmlTableSpacerRow( BaseScoreFormatter.BLANK_ROW_STYLE ) );

                    count++;
                    //teCount++;

                    fullC.append( content );

                    //if( subj == null || subj.isEmpty() )
                    subj = hsf.getEmailSubj(); // MessageFactory.getStringMessage(locale, anon ? "g.ResultEmailSubjAnon" : "g.ResultEmailSubj", params);

                    if( fullC.length()==0 )
                    {
                        LogService.logIt( "BaseDistManager.emailReportsToTestTaker() CCC.3 Skipping for TestEventId=" + te.getTestEventId() + ", reportId=" +  rptToUse.getReportId() +  ", no final content. " + count );
                        continue;
                        // return false;
                    }

                    if( ( subj == null || subj.isEmpty() ) )
                    {
                        if( count>1 && tk.getBatteryProduct()!=null )
                            subj = MessageFactory.getStringMessage( locale , "g.ResultEmailSubjTestTakerMultipleNm" , new String[] {tk.getBatteryProduct().getName()} );

                        else if( count>1 )
                            subj = MessageFactory.getStringMessage( locale , "g.ResultEmailSubjTestTakerMultiple" , new String[] {tk.getBatteryProduct().getName()} );
                    }

                    html = getHtmlTableStart( locale ) + fullC.toString() + getHtmlTableEnd( locale );

                    // LogService.logIt("BaseDistManager.emailReportsToTestTaker() CCC.4 to: " + tk.getUser().getEmail() + ", for TestEventId=" + te.getTestEventId() + ", count=" + count + ", subj=" + subj ); // + ", content=" + html );

                    if( emailUtils==null )
                        emailUtils = EmailUtils.getInstance();

                    // prepare to send
                    emailMap = new HashMap<>();

                    emailMap.put( EmailUtils.MIME_TYPE , "text/html" );

                    emailMap.put( EmailUtils.SUBJECT, subj );

                    // String html = ( textContent == null ? "" : textContent ) + "<br />" + this.getHtmlTableStart() + fullC.toString() + this.getHtmlTableEnd();

                    emailMap.put( EmailUtils.CONTENT, html );

                    emailMap.put( EmailUtils.TO, email);

                    // emailMap.put( EmailUtils.FROM, RuntimeConstants.getStringValue("support-email") + "|" + MessageFactory.getStringMessage( locale , "g.SupportEmailKey", null ) );
                    emailMap.put( EmailUtils.FROM, fromAddr );

                    if( userActionFacade==null)
                        userActionFacade = UserActionFacade.getInstance();

                    userActionFacade.saveMessageAction(tk.getUser(), subj, tk.getTestKeyId(), tk.getOrgAutoTestId(), UserActionType.SENT_EMAIL.getUserActionTypeId() );

                    try
                    {
                        emailUtils.sendEmail( emailMap );
                        Tracker.addEmailSentTestTaker();
                        tes.setDateParam1( new Date() );
                        if( eventFacade==null )
                            eventFacade = EventFacade.getInstance();
                        eventFacade.saveTestEventScore(tes);
                    }

                    catch( STException e )
                    {
                        // skip
                        LogService.logIt("BaseDistManager.emailReportsToTestTaker() DDD.1 Error sending Email for " + tk.toString() + ", for TestEventId=" + te.getTestEventId()  + ", to address: " + tk.getUser().getEmail() );
                    }

                    Thread.sleep( 100 );
                    
                } // Next TES
                
            } // Next TestEvent

            return count;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "BaseDistManager.emailReportsToTestTaker() XXX.1 " + tk.toString() );

            throw new STException( e );
        }
    }
    
    protected void postTestResults( TestKey tk ) throws Exception
    {
        try
        {
            ResultPoster rp = ResultPosterFactory.getResultPosterInstance( tk );

            // LogService.logIt( "BaseDistManager.postTestResults() rp=" + (rp==null ? "null" : "not null") );

            if( rp == null )
                return;

            rp.postTestResults();

            Tracker.addResultsPushAPICalls();
            //return;
            
        }                
        catch( ScoringException e )
        {
            LogService.logIt( "BaseDistManager.postTestResults() ZZZ.1 " + e.toString() + ", testKeyId=" + tk.getTestKeyId() );
            throw e;
        }
        catch( IOException e )
        {
            LogService.logIt( "BaseDistManager.postTestResults() ZZZ.2 " + e.toString() + ", testKeyId=" + tk.getTestKeyId() );
            throw new ScoringException( "BaseDistManager.postTestResults() " + e.toString() + ", testKeyId=" + tk.getTestKeyId(), ScoringException.NON_PERMANENT,tk);
        }
        catch( Exception e )
        {
            throw e;
        }
    }

    protected boolean emailResultsAndReportsToPersonalTestTaker( TestKey tk ) throws Exception
    {

        // LogService.logIt( "BaseDistManager.emailResultsAndReportsToPersonalTestTaker() " + ", " + (tk.getUser()==null ? "user is null " : "user anony=" + tk.getUser().getUserType().getAnonymous() + ",email=" + tk.getUser().getEmail() + ", valid=" + EmailUtils.validateEmailNoErrors( tk.getUser().getEmail() ) )  );

        if( !tk.getHasResultsToEmailPersonalTestTaker())
        {
            // LogService.logIt( "BaseDistManager.emailResultsAndReportsToPersonalTestTaker() Skipping TK does not have any results to email testtaker." );
            return false;
        }

        if( tk.getUser()!=null && !tk.getUser().getUserType().getNamed()  )
        {
            LogService.logIt( "BaseDistManager.emailResultsAndReportsToPersonalTestTaker() Skipping because user is not named (no valid email)." );
            return false;
        }

        if( tk.getAuthorizingUserId()>0 || tk.getUser()==null || tk.getUser().getEmail()==null || tk.getUser().getEmail().isEmpty() || !tk.getUser().getRoleType().getIsPersonalUser() )
        {
            LogService.logIt( "BaseDistManager.emailResultsAndReportsToPersonalTestTaker() Skipping because no user, anonymous yuser, or no email." );
            return false;
        }

        if( !EmailUtils.validateEmailNoErrors( tk.getUser().getEmail() ) )
        {
            LogService.logIt( "BaseDistManager.emailResultsAndReportsToPersonalTestTaker() Skipping because email invalid. " + tk.getUser().getEmail() );
            return false;
        }

        List<String> emailLst = new ArrayList<>();

        emailLst.add( tk.getUser().getEmail() );

        Product p = tk.getTestEventList().get(0).getProduct();

        String frcSubj = MessageFactory.getStringMessage( tk.getUser().getLocaleToUseDefaultUS(), "g.TestResultSubjPersonalUser", new String[] {p.getName()} );

        emailTestResultsToAdministrator(tk, emailLst, frcSubj, null, 0 );

        return true;
    }

    
    public Locale getEmailLocaleFromTestKey( TestKey tk ) throws Exception
    {
            if( tk.getAuthUser()==null && tk.getAuthorizingUserId()>0 )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ) );
            }

            if( tk.getAuthUser()!= null && tk.getAuthUser().getLocaleToUseDefaultNull()!=null )
                return tk.getAuthUser().getLocaleToUseDefaultNull();

            else if( tk.getLocaleStrReport()!=null && !tk.getLocaleStrReport().isEmpty() )
                return I18nUtils.getLocaleFromCompositeStr( tk.getLocaleStrReport() );

            return null;
    }



    protected String getHtmlTableEnd(Locale locale)
    {
        if( locale == null )
            locale = Locale.US;

        boolean ltr = getIsLTR( locale );
        return "</table>\n" +( ltr ? "" : "</dir>" );
    }

    protected String getHtmlTableSpacerRow( String style )
    {
        return "<tr " + style + "><td colspan=\"5\">&#160;</td></tr>\n";
    }


    protected LimitedAccessLink findCreateLimitedAccessLinkForEmailAndTestEventId( String email, long testKeyId, int orgId )
    {
        try
        {
            if( email==null || email.isEmpty() )
                return null;
            
            if( testKeyId<=0 )
                return null;
            
            if( userFacade==null )
                userFacade = UserFacade.getInstance();
            
            User lu = userFacade.getUserByEmailAndOrgId(email, orgId);
            
            if( lu!=null && lu.getRoleType().getCanViewUploadedMedia() )
                return null;
            
            if( limitedAccessLinkFacade==null )
                limitedAccessLinkFacade = LimitedAccessLinkFacade.getInstance();
            
            LimitedAccessLink lal = limitedAccessLinkFacade.getLimitedAccessLinkForEmail(email, testKeyId);
            
            Calendar cal = new GregorianCalendar();
            
            cal.add( Calendar.DAY_OF_MONTH, 10 );
            
            if( lal!=null )
            {
                lal.setExpireDate(cal.getTime());
                lal.setStatusTypeId(1);
            }
            else
            {
                lal = new LimitedAccessLink();
                lal.setEmail(email);
                lal.setTestKeyId(testKeyId);
                lal.setCreateDate(new Date() );
                lal.setExpireDate(cal.getTime());
                lal.setStatusTypeId(1);
                lal.setOrgId( orgId );
            }
            
            limitedAccessLinkFacade.saveLimitedAccessLink(lal);
            
            return lal;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseDistManager.findCreateLimitedAccessLinkForEmailAndTestEventId() " + email + ", testKeyId=" + testKeyId + ", orgId=" + orgId );

            return null;
        }
    }        

    
    /*
      int[0]=reset count
      int[1]=made complete count
      int[2]=Perm Error Count
    
    */
    protected int[] clearStartedDistribTestKeys() throws Exception
    {
        int[] out = new int[3];

        try
        {
            // LogService.logIt( "BaseDistManager.clearDistribErrorTestKeys() Starting "  );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            List<TestKey> ptkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId() , -1, true, -1, RuntimeConstants.getIntList("OrgIdsToSkip",",") );

            // LogService.logIt( "BaseDistManager.clearDistribErrorTestKeys() Found  " + ptkl.size() + " keys to clear." );
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.MONTH, -1 );
            
            for( TestKey tk : ptkl )
            {
                // not in correct status
                if( tk.getTestKeyStatusTypeId()!=TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId() )
                    continue;
                
                // skip - may be part of a thread.
                if( !BaseScoreManager.isScoringFirstTimeOrRepeatAllowed(tk.getTestKeyId()) )
                    continue;
                
                // alreaady dist once. reset to complete.
                if( tk.getFirstDistComplete()==1 )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    out[1]++;
                    continue;
                }
                
                // Too many errors.
                if( tk.getErrorCnt()>=MAX_DIST_ERRORS )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_ERROR.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    out[2]++;
                    continue;                    
                }
                
                // too old - more than 1 month old.
                if( tk.getLastAccessDate()!=null && tk.getLastAccessDate().before( cal.getTime() ) )
                {
                    tk.setErrorCnt(MAX_DIST_ERRORS+1);
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_ERROR.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    out[2]++;
                    continue;                                        
                }

                // reset but add an error to error count.
                //tk.setErrorCnt(tk.getErrorCnt()+1);
                tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() );
                eventFacade.saveTestKey(tk);
                out[0]++;
            }

            // LogService.logIt( "BaseDistManager.clearStartedDistribTestKeys() reset " + out[0] + " dist-started TestKeys. Advanced " + out[1] + " to dist complete, and advanced " + out[2] + " to distribution error." );
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseDistManager.clearStartedDistribTestKeys() " );
            throw new STException( e );
        }                
    }
            
    
    
    protected int[] clearDistribErrorTestKeys() throws Exception
    {
        int[] out = new int[2];

        try
        {
            // LogService.logIt( "BaseDistManager.clearDistribErrorTestKeys() Starting "  );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            List<TestKey> ptkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.DISTRIBUTION_ERROR.getTestKeyStatusTypeId() , -1, true, MAX_DIST_ERRORS, null );

            // LogService.logIt( "BaseDistManager.clearDistribErrorTestKeys() Found  " + ptkl.size() + " keys to clear." );

            for( TestKey tk : ptkl )
            {
                // should not happen.
                if( tk.getErrorCnt()>MAX_DIST_ERRORS || !tk.getTestKeyStatusType().equals(TestKeyStatusType.DISTRIBUTION_ERROR) )
                    continue;
                
                // increment partials
                if( resetTestKeyStatusForDist( tk ) )
                    out[0]++;
                // increment upgrades
                else
                    out[1]++;
            }

            // LogService.logIt( "BaseDistManager.clearDistribErrorTestKeys() cleaned " + out[0] + " partially completed TestKeys and upgraded " + out[1] + " completed testKeys to score complete status." );
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseDistManager.clearDistribErrorTestKeys() " );
            throw new STException( e );
        }                
    }
    
    
    protected boolean resetTestKeyStatusForDist( TestKey tk ) throws Exception
    {
        try
        {
            // LogService.logIt( "BaseDistManager.resetTestKeyStatusForDist() Starting for TestKey=" + tk.getTestKeyId()  );
            if( !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_ERROR ) )
                return false;
            
            if( tk.getTestEventList()==null )
            {
                if( eventFacade == null ) 
                    eventFacade = EventFacade.getInstance();

                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );
            }
            
            boolean hasTestEventToDist = false;
            for( TestEvent te : tk.getTestEventList() )
            {
                // if not scored, return false;
                if( te.getTestEventStatusTypeId()==TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId()  )
                    hasTestEventToDist=true;
            }
            
            // OK to reset to Scored.
            if( hasTestEventToDist )
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() );
                // tk.setErrorCnt( tk.getErrorCnt()+1 );
                if( eventFacade == null ) 
                    eventFacade = EventFacade.getInstance();
                eventFacade.saveTestKey(tk);
                return true;
            }
            
            // do this so we stop looking at this TestKey if it doesn't need a report retry.
            if( tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_ERROR ) )
            {
                tk.setErrorCnt( MAX_DIST_ERRORS+1 );
                if( eventFacade == null ) 
                    eventFacade = EventFacade.getInstance();
                eventFacade.saveTestKey(tk);
            }            
            return false;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseDistManager.resetTestKeyStatusForDist() "  + tk.toString() );
            throw e;
        }
        
    }
    

    protected String getHtmlTableStart( Locale locale )
    {
        if( locale == null )
            locale = Locale.US;

        boolean ltr = getIsLTR( locale );
        return (ltr ? "" : "<div dir=\"rtl\">") + "<table cellpadding=\"3\" cellspacing=\"0\" style=\"width:800px;margin-left:10px;font-family:arial,verdana,tahoma\" " + (ltr ? "" : "dir=\"rtl\"")  + ">\n";
    }

    public boolean getIsLTR( Locale locale )
    {
        return ComponentOrientation.getOrientation( locale ).isLeftToRight();
    }
    
    
}
