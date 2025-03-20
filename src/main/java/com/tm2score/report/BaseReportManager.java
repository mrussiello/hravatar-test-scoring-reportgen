/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2score.custom.bestjobs.BestJobsReportFacade;
import com.tm2score.dist.DistManager;
import com.tm2score.entity.proctor.ProctorEntry;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventLog;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.proctor.ProctorSuspension;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventLogUtils;
import com.tm2score.event.TestEventResponseRatingFacade;
import com.tm2score.event.TestEventResponseRatingUtils;
import com.tm2score.event.TestEventScoreStatusType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.proctor.ProctorFacade;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.purchase.ProductType;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.sim.SimJUtils;
import com.tm2score.user.UserFacade;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.JaxbUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class BaseReportManager {


    public static boolean INIT_COMPLETE = false;
    public static boolean DEBUG_REPORTS = false;
    public static int MAX_NON_FATAL_ERRORS_PER_TE = 4;
    public static int NON_FATAL_ERROR_WAIT_SECS = 20;
    public static int MAX_REPORT_ERRORS = 5;

    // @Inject
    protected EventFacade eventFacade;

    protected UserFacade userFacade = null;

    protected BestJobsReportFacade bestJobsReportFacade;

    protected ProctorFacade proctorFacade;

    protected TestEventResponseRatingFacade testEventResponseRatingFacade;

    protected List<TestEventScore> testEventScoreList;

    DistManager distManager;


    /*
       frcReportId=999999 means all feedback reports
    */
    public List<Report> generateReports( TestEvent te, TestKey tk, long frcReportId, boolean forceCalcSection, boolean sendResendCandidateReportEmails, Date maxLastCandidateSendDate, boolean skipCompleted, int nonFatalErrCount) throws Exception
    {
        List<Report> out = new ArrayList<>();

        Locale reportLocale = Locale.US;

        try
        {
            // LogService.logIt( "ReportManager.generateReports() START te.testEventId=" + te.getTestEventId() + " creating reports.  frcReportId=" + frcReportId + ", te.reportId=" + te.getReportId() + ", " + te.toString()   );

            Tracker.addReportStart();

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            if( te.getProduct() == null )
                te.setProduct( eventFacade.getProduct( te.getProductId() ));

            if( te.getProduct()!=null && te.getProduct().getProductType().getUsesSimDescriptor() && te.getProduct().getTempDate()==null )
                te.getProduct().setTempDate( eventFacade.getLastScoringUpdate( te.getProduct().getLongParam1() ));

            boolean pdfReportsOff = ReportUtils.getReportFlagBooleanValue("pdfreportsoff", null, te.getProduct(), null, null, null );
            if( pdfReportsOff )
            {
                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
                return out;
            }

            if( tk == null )
                tk = eventFacade.getTestKey( te.getTestKeyId() , false ) ;

            if( te.getTestKey() == null )
                te.setTestKey( tk );

            if( !te.getPartialBatteryTestEvent() &&  tk != null && tk.getTestKeyStatusTypeId() < TestKeyStatusType.SCORED.getTestKeyStatusTypeId() )
                throw new Exception( "TestKey is not scored." );

            if(  te.getTestEventStatusTypeId() < TestEventStatusType.SCORED.getTestEventStatusTypeId() )
            {
                // Wait 5-10 secs for any parallel process to complete
                Thread.sleep( 5000l + (long)(Math.random()*5000d) );

                TestEvent te2 = eventFacade.getTestEvent( te.getTestEventId(), true );

                String msg = "TestEvent is not scored so cannot create report(s). current status=" + te.getTestEventStatusTypeId() + ", " + TestEventStatusType.getValue(te.getTestEventStatusTypeId()).getKey() + ", testEventId=" + te.getTestEventId() + ", testKeyId=" + te.getTestKeyId() + ", testKeyStatusTypeId=" + tk.getTestKeyStatusTypeId();
                if( te2.getTestEventStatusTypeId()< TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() )
                {
                    LogService.logIt("BaseReportManager.generateReports() " + msg);
                    TestEventLogUtils.createTestEventLogEntry( te.getTestEventId(), msg);

                    if( te.getTestEventStatusType().getIsCompleteOrHigher() )
                    {
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() );
                    }
                    else
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.STARTED.getTestKeyStatusTypeId() );
                }

                // this will stop the process.
                throw new Exception( msg );
            }

            if( tk != null && tk.getBatteryId() > 0 && tk.getBatteryProduct()== null )
                tk.setBatteryProduct( eventFacade.getProduct( tk.getProductId() ));

            if( tk != null && tk.getUser() == null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                tk.setUser( userFacade.getUser( tk.getUserId() ) );
            }


            if( tk != null && tk.getOrg() == null )
            {
                if( userFacade == null ) userFacade = UserFacade.getInstance();

                tk.setOrg( userFacade.getOrg( tk.getOrgId() ) );

                if( tk.getSuborgId() > 0 )
                    tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));
            }

            if( tk != null && tk.getAuthUser()== null && tk.getAuthorizingUserId() > 0)
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ) );
            }

            if( tk!=null && tk.getTestKeyProctorTypeId()>0 && tk.getProctorEntryList()==null )
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

            if( tk!=null && (tk.getTestKeyProctorTypeId()>0 || tk.getOnlineProctoringType().getIsAnyPremium()) && tk.getProctorSuspensionList()==null)
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

            long reportId = frcReportId>0 && frcReportId!=999999 ? frcReportId : 0;

            long reportId2 = frcReportId>0 && frcReportId!=999999 ? 0 : (te.getProduct() == null ? 0 : te.getProduct().getLongParam3());

            long reportId3 = frcReportId>0 && frcReportId!=999999 ? 0 : (te.getProduct() == null ? 0 : te.getProduct().getLongParam5());

            // boolean sendJobMatchReport =  frcReportId<=0 && tk.getEmailActivityListOk()==1 && te.getProduct().getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId();

            te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ) );

            long engEquivSimId = 0;

            long feedbackReportId = 0;

            if( frcReportId<=0 || frcReportId==999999 )
            {
                // if no reportId provided, get from Test Event
                if( reportId == 0 && te.getReportId()>0 )
                    reportId = te.getReportId();

                // if no reportId provided in TestEvent, get from Product
                if( reportId == 0 )
                    reportId = te.getProduct().getLongParam2();

                // if not less than zero from Product (indicated no report), get from Sim
                if( reportId == 0 && te.getProduct().getProductType().getUsesSimDescriptor() )
                {
                    SimDescriptor sd = eventFacade.getSimDescriptor( te.getSimId(), te.getSimVersionId(), true );

                    reportId = sd.getReportId(); // JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ).getReport();
                }
            }

            // set in te - this is the primary forced report id that is used to peform email and online formatting. Very important!
            if( reportId>0 && te.getReportId()<=0 )
            {
                te.setReportId(reportId);
                eventFacade.saveTestEvent(te);
            }

            // if there is no report for this TestEvent to generate.
            if( reportId<=0 && reportId2<=0 && reportId3<=0 )
            {
                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
                return null;
            }

            if( te.getProduct().getProductType().getIsSimOrCt5Direct()  )
            {
                te.setProfile( ProfileUtils.getLiveProfileForProductIdAndOrgId( te.getTestEventId(), te.getProductId(), te.getOrgId(), ProfileUsageType.REPORT_RANGES.getProfileUsageTypeId() ) );
                engEquivSimId = te.getProduct().getLongParam4();

                ConsumerProductType cpt = ConsumerProductType.getValue( te.getProduct().getConsumerProductTypeId() );

                if( (frcReportId<=0 || frcReportId==999999) && cpt.getIsEligibleForFeedbackReport() && tk!=null && tk.getFeedbackReportOk() )
                {
                    int customFeedbackReportId = ReportUtils.getReportFlagIntValue("fbkreportid", null, te.getProduct(), te.getSuborg(), te.getOrg(), null );

                    feedbackReportId = customFeedbackReportId>0 ? customFeedbackReportId : RuntimeConstants.getLongValue( "feedbackReportId" );

                    // don't re-generate if already there.
                    if( feedbackReportId==reportId || feedbackReportId==reportId2 || feedbackReportId==reportId3 )
                        feedbackReportId = 0;

                    if( ReportUtils.getReportFlagBooleanValue("fbkreportoff", null, te.getProduct(), null, null, null ) )
                        feedbackReportId = 0;

                    // check that the other reports are not test taker dev reports.
                    if( feedbackReportId>0 && customFeedbackReportId<=0 )
                    {
                        long[] devReports = RuntimeConstants.getLongArray( "allTestTakerDevReports", "," );

                        for( long rid : devReports )
                        {
                            if( (reportId>0 && reportId==rid) || (reportId2>0 && reportId2==rid) || (reportId3>0 && reportId3==rid) )
                            {
                                feedbackReportId=0;
                                break;
                            }
                        }
                    }
                }
            }

            // feedbackReportId = 0;


            // LogService.logIt( "ReportManager.generateReport() Primary reportId= " + reportId  );

            Object[] ot;
            Report r;
            String errorMessage;

            int tesDispOrdr = te.getTestEventScoreList().size()+1;

            if( reportId>0 )
            {
                ot = generateSingleReport(te, tk, reportId, forceCalcSection, tesDispOrdr, skipCompleted );
                tesDispOrdr++;

                r = (Report) ot[0];

                reportLocale = (Locale) ot[1];

                errorMessage = (String) ot[2];

                if( errorMessage!=null && !errorMessage.isEmpty() )
                {
                    r=null;
                    LogService.logIt( "ReportManager.generateReport() reportId=" + reportId + " returned an error message=" + errorMessage );
                }

                if( r!=null )
                    te.setReport( r );

                if( r!=null )
                {
                    out.add( r );
                    Tracker.addSingleReport();

                    if( reportLocale != null && !reportLocale.getLanguage().equals( "en" ) && engEquivSimId>0 )
                    {
                        int includeEng = tk.getOrg().getIncludeEnglishReport();

                        if( tk.getIncludeEnglishReportValue()==1 )
                            includeEng = 1;

                        else if( tk.getIncludeEnglishReportValue()==2 )
                            includeEng = 0;

                        if( includeEng==1 )
                        {
                            ot = generateLanguageEquivReport(te, tk, reportId, Locale.US, te.getProduct().getLongParam4(), r.getLongParam1(), tesDispOrdr, true, forceCalcSection );
                            tesDispOrdr++;

                            r = (Report) ot[0];
                            if( r != null )
                            {
                                out.add( r );
                                Tracker.addSingleReport();
                            }
                        }

                        else if( ((Boolean)ot[3]) )
                            tesDispOrdr++;
                    }

                    if( sendResendCandidateReportEmails && r.getEmailTestTaker()==1 )
                    {
                        if( distManager==null )
                            distManager = new DistManager();

                        if( tk!=null )
                        {
                            if( tk.getBatteryProduct()==null )
                                tk.setBatteryProduct(eventFacade.getProduct(tk.getProductId() ));

                            tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true));
                        }
                        te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true));
                        for( TestEventScore tesr : te.getTestEventScoreList(TestEventScoreType.REPORT.getTestEventScoreTypeId()))
                        {
                            if( tesr.getReportId()>0 )
                                tesr.setReport(eventFacade.getReport(tesr.getReportId()));
                        }
                        te.setReport(r);
                        distManager.emailReportsToTestTaker(tk, te, true, r.getReportId(), te.getTestEventId(), maxLastCandidateSendDate );
                        LogService.logIt("ReportManager.generateReport() Sending report to TestTaker. reportId=" + reportId + " sendResendCandidateReportEmails=" + sendResendCandidateReportEmails );
                    }
                }
            }

            if( reportId2>0 )
            {
                ot = generateSingleReport(te, tk, reportId2, forceCalcSection, tesDispOrdr, skipCompleted );
                tesDispOrdr++;

                Report r2 = (Report) ot[0];

                reportLocale = (Locale) ot[1];
                errorMessage = (String) ot[2];

                if( errorMessage!=null && !errorMessage.isEmpty() )
                {
                    r2=null;
                    LogService.logIt( "ReportManager.generateReport() reportId2=" + reportId2 + " returned an error message=" + errorMessage );
                }

                te.setReport2(r2);


                if( r2!=null )
                {
                    out.add( r2 );
                    Tracker.addSingleReport();

                    if( reportLocale != null && !reportLocale.getLanguage().equals( "en" ) && tk.getOrg()!=null && tk.getOrg().getIncludeEnglishReport()==1 && engEquivSimId>0 )
                    {
                        ot = generateLanguageEquivReport(te, tk, reportId2, Locale.US, te.getProduct().getLongParam4(), r2.getLongParam1(), tesDispOrdr, true, forceCalcSection );
                        tesDispOrdr++;

                        r2 = (Report) ot[0];
                        if( r2 != null )
                        {
                            out.add( r2 );
                            Tracker.addSingleReport();
                        }
                    }

                    if( sendResendCandidateReportEmails && r2.getEmailTestTaker()==1 )
                    {
                        if( distManager==null )
                            distManager = new DistManager();
                        if( tk!=null && tk.getBatteryProduct()==null )
                            tk.setBatteryProduct(eventFacade.getProduct(tk.getProductId() ));
                        te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true));
                        for( TestEventScore tesr : te.getTestEventScoreList(TestEventScoreType.REPORT.getTestEventScoreTypeId()))
                        {
                            if( tesr.getReportId()>0 )
                                tesr.setReport(eventFacade.getReport(tesr.getReportId()));
                        }
                        te.setReport(r2);
                        distManager.emailReportsToTestTaker(tk, te, true, r2.getReportId(), te.getTestEventId(), maxLastCandidateSendDate );
                        LogService.logIt("ReportManager.generateReport() Sending reportId 2 to TestTaker. reportId2=" + r2.getReportId() + " sendResendCandidateReportEmails=" + sendResendCandidateReportEmails );
                    }
                }
            }

            if( reportId3>0 )
            {
                ot = generateSingleReport(te, tk, reportId3, forceCalcSection, tesDispOrdr, skipCompleted );
                tesDispOrdr++;

                Report r3 = (Report) ot[0];

                reportLocale = (Locale) ot[1];
                errorMessage = (String) ot[2];

                if( errorMessage!=null && !errorMessage.isEmpty() )
                {
                    r3=null;
                    LogService.logIt( "ReportManager.generateReport() reportId3=" + reportId3 + " returned an error message=" + errorMessage );
                }

                te.setReport3(r3);


                if( r3!=null )
                {
                    out.add( r3 );
                    Tracker.addSingleReport();

                    if( reportLocale != null && !reportLocale.getLanguage().equals( "en" ) && tk.getOrg()!=null && tk.getOrg().getIncludeEnglishReport()==1 && engEquivSimId>0 )
                    {
                        ot = generateLanguageEquivReport(te, tk, reportId3, Locale.US, te.getProduct().getLongParam4(), r3.getLongParam1(), tesDispOrdr, true, forceCalcSection );
                        tesDispOrdr++;

                        r3 = (Report) ot[0];
                        if( r3 != null )
                        {
                            out.add( r3 );
                            Tracker.addSingleReport();
                        }
                    }

                    if( sendResendCandidateReportEmails && r3.getEmailTestTaker()==1 )
                    {
                        if( distManager==null )
                            distManager = new DistManager();
                        if( tk!=null && tk.getBatteryProduct()==null )
                            tk.setBatteryProduct(eventFacade.getProduct(tk.getProductId() ));
                        te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true));
                        for( TestEventScore tesr : te.getTestEventScoreList(TestEventScoreType.REPORT.getTestEventScoreTypeId()))
                        {
                            if( tesr.getReportId()>0 )
                                tesr.setReport(eventFacade.getReport(tesr.getReportId()));
                        }
                        te.setReport(r3);
                        distManager.emailReportsToTestTaker(tk, te, true, r3.getReportId(), te.getTestEventId(), maxLastCandidateSendDate );
                        LogService.logIt("ReportManager.generateReport() Sending reportId 2 to TestTaker. reportId3=" + r3.getReportId() + " sendResendCandidateReportEmails=" + sendResendCandidateReportEmails );
                    }
                }
            }


            if( feedbackReportId > 0 )
            {
                // LogService.logIt( "ReportManager.generatingReport() generating test taker feedback report feedbackReportId=" + feedbackReportId + " for testEventId=" + te.getTestEventId() );

                ot = generateSingleReport(te, tk, feedbackReportId, false, tesDispOrdr, skipCompleted );
                tesDispOrdr++;

                //Report r2 = (Report) ot[0];

                //reportLocale = (Locale) ot[1];
                errorMessage = (String) ot[2];

                if( errorMessage!=null && !errorMessage.isEmpty() )
                {
                    //r2=null;
                    LogService.logIt("ReportManager.generateReport() Error generating Feedback Report for testEventId=" + te.getTestEventId() + ", feedbackReportId=" + feedbackReportId + " returned an error message=" + errorMessage );
                }
                else
                    Tracker.addCandidateFeedbackReport();
            }

            /*
            if( 1==2 && sendJobMatchReport )
            {
                reportId = RuntimeConstants.getIntValue( "BestJobsReportId" );

                ot = generateSingleReport( te, tk, reportId );

                Report r3 = (Report) ot[0];

                reportLocale = (Locale) ot[1];
                errorMessage = (String) ot[2];

                if( errorMessage!=null && !errorMessage.isEmpty() )
                {
                    r3=null;
                    LogService.logIt( "ReportManager.generateReport() reportId2=" + reportId2 + " returned an error message=" + errorMessage );
                }

                if( r3 != null )
                {
                    out.add( r3 );
                    Tracker.addSingleReport();
                }
            }
            */
            // LogService.logIt( "ReportManager.generateReports() FINISH te.testEventId=" + ( te==null ? "null" : te.getTestEventId())  );

            Tracker.addReportFinish();

            return out;
        }

        catch( ScoringException e )
        {
            throw e;
        }
        catch( ReportException e )
        {
            nonFatalErrCount++;
            if( nonFatalErrCount<MAX_NON_FATAL_ERRORS_PER_TE && e.getSeverity()==ReportException.NON_PERMANENT )
            {
                // wait 20 seconds and try again.
                LogService.logIt("ReportManager.generateReport() CCC.1 Non-Fatal Report Exception waiting and trying again. testEventId=" + te.getTestEventId() + ", testKeyId=" + te.getTestKeyId());
                Thread.sleep( NON_FATAL_ERROR_WAIT_SECS*1000 );
                return generateReports( te, tk, frcReportId,  forceCalcSection, sendResendCandidateReportEmails,maxLastCandidateSendDate,  true,  nonFatalErrCount );
            }

            throw e;
        }
        catch( STException e )
        {
            throw new ReportException( MessageFactory.getStringMessage( Locale.US , e.getKey(), e.getParams() ) , e.getForceNonPermanent() ? ReportException.NON_PERMANENT : ReportException.PERMANENT , te, frcReportId, reportLocale.getLanguage() );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.generateReport() " + te.toString() + ", " + te.toString() );
            throw new ReportException( e.getMessage() , ScoreUtils.getExceptionPermanancy(e) , te, frcReportId, reportLocale.getLanguage() );
        }
    }

    /**
     * Returns Object[0] = Report
     *         Object[1] = Report Locale
     *         Object[2] = Reason why report cannot be created, or null.
     *         Object[3] = true/false - indicates if Lang Equiv Report Generated.
     * @param te
     * @param tk
     * @param reportId
     * @param frcReportLocale
     * @return
     * @throws Exception
     */
    public Object[] generateSingleReport( TestEvent te, TestKey tk, long reportId, boolean forceCalcSection, int tesDispOrdr, boolean skipCompleted) throws Exception
    {
        Object[] out = new Object[4];

        out[3]=false;

        Locale rptLocale = null;

        try
        {
            Date procStart = new Date();

            // LogService.logIt("ReportManager.generateSingleReport() reportId = " + reportId  );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            // LogService.logIt("ReportManager.generateReport() reportId = " + reportId  );

            // if there is no report for this TestEvent to generate.
            if( reportId <= 0 )
                return null;

            Report r = eventFacade.getReport(reportId);

            if( r == null )
                throw new Exception( "Report not found: " + reportId );

            r = (Report) r.clone();

            out[0]=r;

            if( tk.getAuthUser() == null && tk.getAuthorizingUserId()> 0 )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
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

            if( te.getProduct()==null )
                te.setProduct(eventFacade.getProduct( te.getProductId() ));

            if( te.getTestEventScoreList()==null )
                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ));

            ReportData rd = new ReportData( tk, te, r, tk.getUser(), tk.getOrg(), te.getProfile() );

            if( forceCalcSection )
                rd.forceCalcSection = true;

            te.setReport( r );

            if( te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
                r.setLocaleForReportGen( I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() ) );
            else if( r.getLocaleStr()!= null && !r.getLocaleStr().isEmpty() )
                r.setLocaleForReportGen( I18nUtils.getLocaleFromCompositeStr( r.getLocaleStr() ) );
            else if( tk.getAuthUser()!=null && tk.getAuthUser().getLocaleStr()!=null && !tk.getAuthUser().getLocaleStr().isEmpty()  )
                r.setLocaleForReportGen( I18nUtils.getLocaleFromCompositeStr( tk.getAuthUser().getLocaleStr() ) );
            else
                r.setLocaleForReportGen( Locale.US );

            rptLocale = r.getLocaleForReportGen();

            if( te.getTestEventResponseRatingList()==null )
            {
                if( testEventResponseRatingFacade==null )
                    testEventResponseRatingFacade=TestEventResponseRatingFacade.getInstance();
                te.setTestEventResponseRatingList( testEventResponseRatingFacade.getTestEventResponseRatingsForTestEventId(te.getTestEventId()));
            }

            if( te.getTestEventResponseRatingList()!=null && !te.getTestEventResponseRatingList().isEmpty() )
            {
                TestEventResponseRatingUtils.setTestEventResponseRatingNames( tk.getOrg(), te.getProduct(), te.getTestEventScoreList(TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()), rptLocale, te.getTestEventResponseRatingList());
                // Map<String,String> avgRatingMap = TestEventResponseRatingUtils.getOverallAverageRatingMap( te.getTestEventResponseRatingList(), rptLocale );
            }

            // LogService.logIt("ReportManager.generateSingleReport() creating report for " + te.toString() + ", " + r.toString() + ", report language=" + r.getLocaleForReportGen().toString() );

            boolean createPdfDoc = r.getNoPdfDoc()==0;

            byte[] rptBytes = null;

            String reportNotes = null;

            long additionalTestEventIdToAddReportTo = 0;

            // Clean TestEventScores. Remove any without a reportid
            cleanReportTestEventScores( te );

            // te.setReportAndScoringFlags();
            // List<TestEventScore> tesl = te.getTestEventScoreListForReportId(reportId);
            TestEventScore tes = te.getTestEventScoreForReportId( reportId, r.getLocaleForReportGen().toString() );

            if( tes!=null && skipCompleted && tes.getTestEventScoreStatusTypeId()==TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() && tes.getReportBytes()!=null )
            {
                // LogService.logIt( "ReportManager.generateSingleReport() Skipping generation because skipCompleted is true and testEventScore is active. reportId = " + reportId + ", testEventId=" + tes.getTestEventId() );
                out[1] = rptLocale;
                return out;
            }

            if( createPdfDoc )
            {
                long langEquivSimId = 0;
                long langEquivReportId = 0;

                if( rptLocale != null &&
                    te.getProduct().getProductType().getIsSimOrCt5Direct() && // It's a sim
                    !I18nUtils.getLanguageFromLocale(rptLocale).equals( I18nUtils.getLanguageFromLocaleStr(te.getProduct().getLangStr()) ) // Report language different from Test language
                  )
                {
                    langEquivSimId = getLangEquivSimIdForProductAndLang( te.getProduct(), rptLocale );

                    if( langEquivSimId>0 )
                        langEquivReportId = getLangEquivReportIdForProductReportAndLang(te.getProduct(), r, rptLocale);
                }

                // If forced to generate in English, report has an english equiv report id, Test is not written in english, and product has an english EquivSimId, use it
                if( langEquivSimId>0 )
                {
                    LogService.logIt("ReportManager.generateSingleReport() since target report language is " + rptLocale.toString() + " AND the Sim is in "  + te.getProduct().getLangStr() + " AND there is an equivalent sim for this sim in the target report language (langEquivSimId=" + langEquivSimId + "), generating an equivalent report. reportId = " + reportId  );
                    Object[] ot = generateLanguageEquivReport(te, tk, reportId, rptLocale, langEquivSimId, langEquivReportId, tesDispOrdr+1, false, forceCalcSection );
                    out[3]=true;
                    tes = (TestEventScore) ot[2];

                    if( tes != null )
                        rptBytes = tes.getReportBytes();
                }

                if( rptBytes == null )
                {
                    // now get the report template class
                    String tmpltClassname = r.getImplementationClass();

                    if( !r.getReportTemplateType().getIsCustom() )
                        tmpltClassname = r.getReportTemplateType().getImplementationClass();

                    Class<ReportTemplate> tmpltClass = (Class<ReportTemplate>) Class.forName( tmpltClassname );

                    // LogService.logIt( "ReportManager templateClass=" + tmpltClassname + ", class=" + tmpltClass.toString() );

                    Constructor ctor = tmpltClass.getDeclaredConstructor();
                    ReportTemplate rt = (ReportTemplate) ctor.newInstance();
                    // ReportTemplate rt = tmpltClass.newInstance();

                    if( rt == null )
                        throw new Exception( "Could not generate template class instance: " + tmpltClassname );

                    rt.init( rd );

                    if( !rt.isValidForTestEvent() )
                    {
                        out[2]="Report Generation is not valid for this Report and this Test Event: " + r.toString() + ", " + te.toString();
                        return out;
                    }

                    if( !rt.getIsReportGenerationPossible() )
                        throw new Exception( "Report generation not possible." );

                    // update status
                    if(  te.getTestEventStatusTypeId() < TestEventStatusType.REPORT_STARTED.getTestEventStatusTypeId() ||
                        te.getTestEventStatusTypeId() == TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId())
                    {
                        te.setTestEventStatusTypeId( TestEventStatusType.REPORT_STARTED.getTestEventStatusTypeId() );

                        if( te.getTestEventId()> 0 )
                            eventFacade.saveTestEvent( te );
                    }

                    rptBytes = rt.generateReport();

                    rptLocale = rt.getReportLocale();

                    reportNotes = rt.getReportGenerationNotesToSave();

                    additionalTestEventIdToAddReportTo = rt.addReportToOtherTestEventId();

                    out[1] = rptLocale;

                    rt.dispose();

                    if( rptBytes == null || rptBytes.length == 0 )
                    {
                        if( te.getProduct().getProductType().getIsFindly() )
                        {
                            LogService.logIt("ReportManager.generateSingleReport() Findly Report for test event is empty. " + te.getProduct().toString() + ", " + r.toString() + ", " + te.toString() );

                            if(  te.getTestEventStatusTypeId() < TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() )
                            {
                                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() );

                                // LogService.logIt("ReportManager.generateSingleReport() saving test event. with ReportComplete status. teId=" + te.getTestEventId() );

                                if( te.getTestEventId()> 0 )
                                    eventFacade.saveTestEvent( te );
                            }

                            return out;
                        }

                        else
                            throw new Exception( "Generated report is empty." );
                    }

                }
            }


            if( rptLocale == null )
                rptLocale = Locale.US;

            if( tes == null )
            {
                tes = new TestEventScore();
                tes.setDisplayOrder(tesDispOrdr>0 ? tesDispOrdr : te.getTestEventScoreList().size() + 1 );
                tes.setTestEventId( te.getTestEventId() );
            }

            tes.setTestEventScoreTypeId( TestEventScoreType.REPORT.getTestEventScoreTypeId() );
            tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
            tes.setReportId( r.getReportId() );
            tes.setStrParam1( rptLocale.toString() );
            tes.setTextParam1( reportNotes );

            tes.setReportBytes( rptBytes );

            if( createPdfDoc )
            {
                // String suffix = "";

                tes.setReportFileContentTypeId( 500 ); // PDF
                tes.setReportFilename(getReportFilename( te ) + ".PDF" );
            }

            else
            {
                tes.setReportFileContentTypeId( 0 );
                tes.setReportFilename( "NoReport" );
            }

            tes.setName( rd.getReportName() );
            // tes.setName( te.getReport().getNameForReportDocument( te.getProduct().getName() ) );

            tes.setCreateDate( new Date() );

            if( te.getTestEventId()> 0 )
               eventFacade.saveTestEventScore(tes);

            // this is for looking at reports on scoring admin site.
            if( testEventScoreList != null && tes.getHasReport() )
                testEventScoreList.add(tes);

            // LogService.logIt( "ReportManager.generateSingleReport() saving test event. " );

            if(  te.getTestEventStatusTypeId() < TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() )
            {
                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() );

                // LogService.logIt( "ReportManager.generateSingleReport() saving test event. " );

                if( te.getTestEventId()> 0 )
                    eventFacade.saveTestEvent( te );
            }

            // LogService.logIt( "ReportManager.generateSingleReport() completed report " + tes.getReportFilename() + ", " + ( tes.getReportBytes() == null ? "No Document in Report." :  tes.getReportBytes().length + " bytes." ) );

            Tracker.addResponseTime( "Generate Single Report", new Date().getTime() - procStart.getTime() );

            // this is to add the report to a different test event. Typically, this is done for the BestJobs Report.
            if( additionalTestEventIdToAddReportTo>0 && rptBytes!=null && rptBytes.length>0 )
            {
                TestEvent te2 = eventFacade.getTestEvent( additionalTestEventIdToAddReportTo, true );

                if( te2.getTestEventStatusType().getIsReportsCompleteOrHigher() )
                {
                    te2.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( additionalTestEventIdToAddReportTo, true ) );

                    TestEventScore tes2 = te2.getTestEventScoreForReportId(reportId, r.getLocaleForReportGen().toString() );

                    if( tes2==null )
                    {
                        tes2 = new TestEventScore();
                        tes2.setDisplayOrder( te2.getTestEventScoreList().size() + 1 );
                        tes2.setTestEventId( te2.getTestEventId() );
                    }

                    tes2.setTestEventScoreTypeId( tes.getTestEventScoreTypeId() );
                    tes2.setTestEventScoreStatusTypeId( tes.getTestEventScoreStatusTypeId() );
                    tes2.setReportId( tes.getReportId() );
                    tes2.setStrParam1( tes.getStrParam1() );
                    tes2.setTextParam1( tes.getTextParam1() );
                    tes2.setReportBytes( tes.getReportBytes() );
                    tes2.setReportFileContentTypeId( tes.getReportFileContentTypeId() ); // PDF
                    tes2.setReportFilename( tes.getReportFilename() );
                    tes2.setName( tes.getName() );
                    tes2.setNameEnglish( tes.getNameEnglish() );
                    tes2.setCreateDate( tes.getCreateDate() );
                    eventFacade.saveTestEventScore(tes2);

                    // LogService.logIt( "ReportManager.generateSingleReport() Added report for TestEventId=" + te.getTestEventId() + ", to an Additional TestEventId=" + additionalTestEventIdToAddReportTo );
                }
            }

            return out;
        }
        catch( ScoringException e )
        {
             throw e;
        }
        catch( ReportException e )
        {
            if( te!=null )
                TestEventLogUtils.createTestEventLogEntry(te.getTestEventId(),  "ReportManager.generateSingleReport() Exception generating report. " + e.toString() + ", reportId=" + reportId );

            throw e;
        }

        catch( STException e )
        {
            if( rptLocale == null )
                rptLocale = Locale.US;

            String msg = MessageFactory.getStringMessage( Locale.US , e.getKey(), e.getParams() );

            if( te!=null )
                TestEventLogUtils.createTestEventLogEntry(te.getTestEventId(),  "ReportManager.generateSingleReport() Exception generating report. " + msg + ", reportId=" + reportId );

            boolean isPermanent = isPermanentReportError( e );

            throw new ReportException( msg , isPermanent ? ReportException.PERMANENT : ReportException.NON_PERMANENT , te, reportId, rptLocale.toString() );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.generateSingleReport() " + te.toString() + ", " + te.toString() );

            if( rptLocale == null )
                rptLocale = Locale.US;

            if( te!=null )
                TestEventLogUtils.createTestEventLogEntry(te.getTestEventId(),  "ReportManager.generateSingleReport() Exception generating report. " + e.toString() + ", reportId=" + reportId );

            throw new ReportException( e.getMessage() , ScoreUtils.getExceptionPermanancy(e) , te, reportId, rptLocale.toString() );
        }
    }

    public boolean isPermanentReportError( Exception e )
    {
        if( e instanceof ReportException )
            return ((ReportException) e).getSeverity()==ReportException.PERMANENT;

        if( e instanceof STException )
        {
            if( ((STException)e).getForceNonPermanent() )
                return false;

            String arg0 = ((STException) e).getArg0();
            if( arg0==null || arg0.isBlank() )
                return true;
            if( arg0.contains( " 500 for URL ") )
                return false;
            if( arg0.contains(" NONFATAL "))
                return false;
        }

        return true;
    }

    /**
     * out[0]=Report used
     * out[1]=Locale
     * out[2]=TestEventScore containing the English Report
     * @param te
     * @param tk
     * @param reportId
     * @return
     * @throws Exception
     */
    public Object[] generateLanguageEquivReport( TestEvent te, TestKey tk, long reportId, Locale tgtLocale, long langEquivSimId, long langEquivReportId, int tesDispOrdr, boolean saveTestEventScore, boolean forceCalcSection) throws Exception
    {
        Object[] out = new Object[3];

        try
        {
            //if( 1==2 )
            //    return out;

            if( te==null )
                throw new Exception( "TestEvent is null");

            if( te.getProduct()==null )
                throw new Exception( "TestEvent.product is null" );

            if( te.getProductTypeId()!=ProductType.SIM.getProductTypeId() && te.getProductTypeId()!=ProductType.CT5DIRECTTEST.getProductTypeId() )
                throw new Exception( "Product is not correct Type. " + te.getProduct().toString() );

            if( langEquivSimId<=0 )
                throw new Exception( "No LangEquivalent SimId langEquivSimId=" + langEquivSimId + " and te.getProduct().getLongParam4()=" + te.getProduct().getLongParam4() );

            Date procStart = new Date();

            LogService.logIt("ReportManager.generateLanguageEquivReport() reportId = " + reportId + ", TestEvent=" + te.getTestEventId() + ", langEquivSimId=" + langEquivSimId + ", langEquivReportId=" + langEquivReportId );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            // LogService.logIt("ReportManager.generateReport() reportId = " + reportId  );

            // if there is no report for this TestEvent to generate.
            if( reportId <= 0 )
                return null;

            Report r = eventFacade.getReport(reportId);

            if( r == null )
                throw new Exception( "Report not found: " + reportId );

            Report equivReport = langEquivReportId<=0 ? null :  eventFacade.getReport( langEquivReportId );

            if( equivReport!=null )
                equivReport.setLocaleForReportGen( tgtLocale );

            if( tk.getAuthUser() == null && tk.getAuthorizingUserId()> 0 )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
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


            SimDescriptor langEquivSimDescriptor = eventFacade.getSimDescriptor( langEquivSimId, -1, true );

            if( langEquivSimDescriptor == null )
                throw new Exception( "SimDescriptor for EnglishEquiv Sim not found." );

            ReportData rd = new ReportData( tk, te, r, tk.getUser(), tk.getOrg(), te.getProfile() );

            if( forceCalcSection )
                rd.forceCalcSection = true;

            te.setReport( r );

            // this is what indicates its an equivalent report.
            r.setLocaleForReportGen( tgtLocale );

            rd.equivSimJUtils = new SimJUtils( JaxbUtils.ummarshalSimDescriptorXml(langEquivSimDescriptor.getXml() ));

            rd.equivR = equivReport;

            // LogService.logIt("ReportManager.generateLanguageEquivReport() creating report for " + te.toString() + ", " + r.toString() + ", report language=" + r.getLocaleForReportGen().toString() );

            Locale simLocale = te.getProduct()==null ? null : te.getProduct().getLocaleFmLangStr();

            // If the Sim locale is not equal to the target locale, load the sim's SimDescriptor as well for use in swapping specific values embedded in the Sim.
            if(  simLocale!=null && (te.getProduct().getProductType().getIsSimOrCt5Direct() ) && !simLocale.getLanguage().equalsIgnoreCase( tgtLocale.getLanguage() ) )
            {
                SimDescriptor simDescriptor = eventFacade.getSimDescriptor( te.getSimId(), -1, true );

                if( simDescriptor == null )
                    throw new Exception( "SimDescriptor Sim not found. SimId=" + te.getSimId() + ", V" + te.getSimVersionId() );

                rd.simJUtils = new SimJUtils( JaxbUtils.ummarshalSimDescriptorXml(simDescriptor.getXml() ));
            }

            // boolean createPdfDoc = r.getNoPdfDoc()==0;

            if( r.getNoPdfDoc()!=0 )
                return out;

            out[0]=r;

            byte[] rptBytes = null;

            // te.setReportAndScoringFlags();
            // List<TestEventScore> tesl = te.getTestEventScoreListForReportId(reportId);
            TestEventScore tes = te.getTestEventScoreForReportId(reportId, tgtLocale.toString() );

            // now get the report template class
            String tmpltClassname = r.getImplementationClass();

            if( !r.getReportTemplateType().getIsCustom() )
                tmpltClassname = r.getReportTemplateType().getImplementationClass();

            Class<ReportTemplate> tmpltClass = (Class<ReportTemplate>) Class.forName( tmpltClassname );

            Constructor ctor = tmpltClass.getDeclaredConstructor();
            ReportTemplate rt = (ReportTemplate) ctor.newInstance();

            if( rt == null )
                throw new Exception( "Could not generate template class instance: " + tmpltClassname );

            rt.init( rd );

            if( !rt.getIsReportGenerationPossible() )
                throw new Exception( "Report generation not possible." );

            // update status
            if(  te.getTestEventStatusTypeId() < TestEventStatusType.REPORT_STARTED.getTestEventStatusTypeId() ||
                te.getTestEventStatusTypeId() == TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId())
            {
                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_STARTED.getTestEventStatusTypeId() );

                if( te.getTestEventId()> 0 )
                    eventFacade.saveTestEvent( te );
            }

            rptBytes = rt.generateReport();

            out[1] = tgtLocale;

            rt.dispose();

            if( rptBytes == null || rptBytes.length == 0 )
            {
                if( te.getProduct().getProductType().getIsFindly() )
                {
                    LogService.logIt("ReportManager.generateLanguageEquivReport() Findly Report for test event is empty. " + te.getProduct().toString() + ", " + r.toString() + ", " + te.toString() );

                    if(  te.getTestEventStatusTypeId() < TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() )
                    {
                        te.setTestEventStatusTypeId( TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() );

                        // LogService.logIt("ReportManager.generateLanguageEquivReport() saving test event. with ReportComplete status. teId=" + te.getTestEventId() );

                        if( te.getTestEventId()> 0 )
                            eventFacade.saveTestEvent( te );
                    }

                    return out;
                }

                else
                    throw new Exception( "Generated report is empty." );
            }

            if( tes == null )
            {
                tes = new TestEventScore();
                tes.setDisplayOrder( tesDispOrdr>0 ? tesDispOrdr : te.getTestEventScoreList().size() + 1 );
                tes.setTestEventId( te.getTestEventId() );
            }

            tes.setTestEventScoreTypeId( TestEventScoreType.REPORT.getTestEventScoreTypeId() );
            tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
            tes.setReportId( r.getReportId() );
            tes.setStrParam1( tgtLocale.toString() );

            tes.setReportBytes( rptBytes );

            tes.setReportFileContentTypeId( 500 ); // PDF
            tes.setReportFilename(getReportFilename( te ) + "_" + tgtLocale.toString() + ".PDF" );

            tes.setName(rd.getReportName() + " (" + tgtLocale.toString() + ")" );
            // tes.setName( te.getReport().getNameForReportDocument( te.getProduct().getName() ) );

            tes.setCreateDate( new Date() );

            if( saveTestEventScore && te.getTestEventId()> 0 )
               eventFacade.saveTestEventScore(tes);

            // this is for looking at reports on scoring admin site.
            if( saveTestEventScore && testEventScoreList != null && tes.getHasReport() )
                testEventScoreList.add(tes);

            out[2]=tes;

            Tracker.addLanguageEquivalentReport();

            // LogService.logIt( "ReportManager.generateLanguageEquivReport() saving test event. " );

            if( saveTestEventScore && te.getTestEventStatusTypeId() < TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() )
            {
                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() );

                // LogService.logIt( "ReportManager.generateLanguageEquivReport() saving test event. " );

                if( te.getTestEventId()> 0 )
                    eventFacade.saveTestEvent( te );
            }

            // LogService.logIt( "ReportManager.generateEnglishEquivReport() completed report " + tes.getReportFilename() + ", " + ( tes.getReportBytes() == null ? "No Document in Report." :  tes.getReportBytes().length + " bytes." ) );

            Tracker.addResponseTime( "Generate Languge Equivalent Report", new Date().getTime() - procStart.getTime() );

            return out;
        }

        catch( ScoringException e )
        {
             throw e;
        }
        catch( ReportException e )
        {
            throw e;
        }
        catch( STException e )
        {
            throw new ReportException( MessageFactory.getStringMessage( Locale.US , e.getKey(), e.getParams() ) , ReportException.PERMANENT , te, reportId, "en_US" );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.generateEnglishEquivReport() " + ( te==null ? "" : te.toString() ) + ", " + tk.toString() );

            throw new ReportException( e.getMessage() , ScoreUtils.getExceptionPermanancy(e) , te, reportId, "en_US" );
        }
    }

    protected void cleanReportTestEventScores( TestEvent te )
    {
        try
        {
            if( te.getTestEventScoreList()==null )
            {
                if( eventFacade==null )
                    eventFacade = EventFacade.getInstance();

                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true ) );
            }

            ListIterator<TestEventScore> li = te.getTestEventScoreList().listIterator();

            TestEventScore tes;

            while( li.hasNext() )
            {
                tes = li.next();

                if( !tes.getTestEventScoreType().getIsReport() || tes.getReportId()>0 )
                    continue;

                LogService.logIt( "BaseReportManager.cleanReportTestEventScores() found invalid Report TestEventScore. testEventId=" + te.getTestEventId() + ", testEventScoreId=" + tes.getTestEventScoreId() + ", name=" + tes.getName() + ", removing and deleting.");

                li.remove();

                if( eventFacade==null )
                    eventFacade = EventFacade.getInstance();

                eventFacade.deleteEntity( tes );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportManager.cleanReportTestEventScores() testEventId=" + te.getTestEventId() );
        }
    }

    protected long getLangEquivSimIdForProductAndLang( Product p, Locale tgtLocale )
    {
        try
        {
            if( tgtLocale==null )
                return 0;

            String prodLang = I18nUtils.getLanguageFromLocaleStr( p.getLangStr() );

            // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() tgtLocale=" + tgtLocale.toString() + ", productLocale=" + p.getLangStr() );

            // Both the Product and the target are the same language. // MJR - 01-08-2019 Changed to LongParam1 (Sim Id)
            if( tgtLocale.getLanguage().equalsIgnoreCase( prodLang ) && p.getLongParam1()> 0 )
                return p.getLongParam1();

            // target is english and the product names a lang equiv SimId.
            if( tgtLocale.getLanguage().equals( "en" ) && p.getLongParam4()> 0 )
                return p.getLongParam4();

            LangEquivFacade langEquivFacade = LangEquivFacade.getInstance();

            long englishEquivSimId = 0; // tgtLocale.getLanguage().equals( "en" ) ? p.getLongParam4() : 0;

            Product p2 = null;

            // if there is no equiv english report, but the report itself is in english, it could be an english report target, so use it as the english equiv report id.
            if( englishEquivSimId<=0 && prodLang.equalsIgnoreCase( "en" ) )
            {
                // yes it is an english equiv for something.
                //if( langEquivFacade.isProductAnEnglishEquivalent( p ) )
                    englishEquivSimId = p.getLongParam1();
            }

            // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() BBB.1 englishEquivSimId=" + englishEquivSimId + ", tgtLocale=" + tgtLocale.toString() + ", productLocale=" + p.getLangStr() );


            // If the product names an english equivalent simId, use that.
            if( englishEquivSimId<=0 && p.getLongParam4()>0 )
                englishEquivSimId = p.getLongParam4();

            // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() BBB.2 englishEquivSimId=" + englishEquivSimId + ", tgtLocale=" + tgtLocale.toString() + ", productLocale=" + p.getLangStr() );

            // no eng equiv found, keep looking using ONET SOC.
            if( englishEquivSimId<=0 && p.getOnetSoc()!=null && !p.getOnetSoc().isEmpty() && tgtLocale.getLanguage().equals( "en" ) && p.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId()   )
            {
                // look for full match of name, simId, and locale
                p2 = langEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv( p.getOnetSoc(), p.getConsumerProductTypeId(), p.getLangStr() );

                // look for language only match of name, simId, and locale
                if( p2==null )
                    p2 = langEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv( p.getOnetSoc(), p.getConsumerProductTypeId(), prodLang );

                // look for any match of name, simId, and locale
                if( p2==null )
                    p2 = langEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv( p.getOnetSoc(), p.getConsumerProductTypeId(), null );

                // Found something.
                if( p2!=null )
                    englishEquivSimId = p2.getLongParam4();
            }

            // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() BBB.3 englishEquivSimId=" + englishEquivSimId + ", tgtLocale=" + tgtLocale.toString() + ", productLocale=" + p.getLangStr() );

            // no eng equiv found, keep looking using NAME.
            if( englishEquivSimId<=0  && tgtLocale.getLanguage().equals( "en" ) )
            {
                // look for full match of name, simId, and locale
                p2 = langEquivFacade.findAnyProductWithNameAndConsumerTypeThatNamesAnEnglishEquiv( p.getName(), p.getNameEnglish(), p.getConsumerProductTypeId(), p.getLangStr() );

                // look for language only match of name, simId, and locale
                if( p2==null )
                    p2 = langEquivFacade.findAnyProductWithNameAndConsumerTypeThatNamesAnEnglishEquiv(  p.getName(), p.getNameEnglish(), p.getConsumerProductTypeId(), prodLang );

                // look for any match of name, simId, and locale
                if( p2==null )
                    p2 = langEquivFacade.findAnyProductWithNameAndConsumerTypeThatNamesAnEnglishEquiv(  p.getName(), p.getNameEnglish(), p.getConsumerProductTypeId(), null );

                // Found something.
                if( p2!=null )
                    englishEquivSimId = p2.getLongParam4();
            }

            // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() BBB.4 englishEquivSimId=" + englishEquivSimId + ", tgtLocale=" + tgtLocale.toString() + ", productLocale=" + p.getLangStr() );

            if( englishEquivSimId<=0 && p.getOnetSoc()!=null && !p.getOnetSoc().isEmpty()  && p.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId()    )
            {
                p2 = langEquivFacade.findAnyProductWithSocAndConsumerTypeThatIsInEnglish(  p.getOnetSoc(), p.getConsumerProductTypeId() );

                if( p2!=null )
                    englishEquivSimId = p2.getLongParam1();
            }

            // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() BBB.5 englishEquivSimId=" + englishEquivSimId + ", tgtLocale=" + tgtLocale.toString() + ", productLocale=" + p.getLangStr() );

            if( englishEquivSimId>0 && tgtLocale.getLanguage().equals( "en" ) )
                return englishEquivSimId;

            // target NOT english and the report does have a eng equivalent.
            if( englishEquivSimId>0 )
            {
                // Look for full locale match
                p2 = langEquivFacade.findProductWithTgtLangPointingToEngEquivSimId( englishEquivSimId, tgtLocale.toString() );

                // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() CCC.1 p2=" + (p2==null ? "null" : p2.getName() + " " + p2.getProductId() + " simId=" + p.getLongParam1()) + ", englishEquivSimId=" + englishEquivSimId + ", tgtLocale=" + tgtLocale.toString() );

                if( p2!=null )
                    return p2.getLongParam1();

                // Look for just language match
                p2 = langEquivFacade.findProductWithTgtLangPointingToEngEquivSimId( englishEquivSimId, tgtLocale.getLanguage() );

                // LogService.logIt( "ReportManager.getLangEquivReportIdForProductReportAndLang() CCC.2 p2=" + (p2==null ? "null" : p2.getName() + " " + p2.getProductId() + " simId=" + p.getLongParam1()) + ", englishEquivSimId=" + englishEquivSimId + ", tgtLocale=" + tgtLocale.toString() );

                if( p2!=null )
                    return p2.getLongParam1();
            }

            return 0;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportManager.getLangEquivReportIdForProductReportAndLang() " + (p==null ? "product is null" : p.toString() ) + ", tgtLocale=" + tgtLocale.toString() );

            return 0;
        }
    }

    protected long getLangEquivReportIdForProductReportAndLang( Product p, Report r, Locale tgtLocale)
    {
        try
        {
            if( tgtLocale==null )
                return 0;

            String rptLang = I18nUtils.getLanguageFromLocaleStr( r.getLocaleStr() );

            // Both the report and the target are the same language.
            if( tgtLocale.getLanguage().equalsIgnoreCase( rptLang ) )
                return r.getReportId();

            // target is english and this report has an eng equiv - use that.
            if( tgtLocale.getLanguage().equals( "en" ) && r.getLongParam1()> 0 )
                return r.getLongParam1();


            LangEquivFacade langEquivFacade = LangEquivFacade.getInstance();

            long engEquivReportId = r.getLongParam1();

            Report r2;

            // if there is no equiv english report, but the report itself is in english, it could be an english report target, so use it as the english equiv report id.
            if( engEquivReportId<=0 && rptLang.equalsIgnoreCase( "en" ) )
            {
                // yes it is an english equiv for something.
                if( 1==2 && langEquivFacade.isReportAnEnglishEquivalent( r.getReportId() ) )
                    engEquivReportId = r.getReportId();

                // since it is in english, use it.
                else
                    engEquivReportId = r.getReportId();
            }

            // no eng equiv found, keep looking.
            if( engEquivReportId<=0 )
            {
                // look for full match of name, class, and locale
                r2 = langEquivFacade.findAnyReportWithSameNameAndClassThatNamesAnEnglishEquiv( r.getName(), r.getNameEnglish(), r.getImplementationClass(), r.getLocaleStr() );

                // look for language only match of name, class, and locale
                if( r2==null )
                    r2 = langEquivFacade.findAnyReportWithSameNameAndClassThatNamesAnEnglishEquiv( r.getName(), r.getNameEnglish(), r.getImplementationClass(), rptLang );

                // look for any match of name, class, and locale
                if( r2==null )
                    r2 = langEquivFacade.findAnyReportWithSameNameAndClassThatNamesAnEnglishEquiv( r.getName(), r.getNameEnglish(), r.getImplementationClass(), null );

                // Found something.
                if( r2!=null )
                    engEquivReportId = r2.getLongParam1();

                // found nothing
                else
                {
                    long rid = langEquivFacade.findAnyReportWithSameClassThatIsAnEnglishEquiv( r.getImplementationClass() );

                    if( rid>0 )
                        engEquivReportId= rid;
                }
            }

            // target NOT english and the report does have a eng equiv.
            if( engEquivReportId> 0 )
            {
                // Look for full locale match
                long langEquivReportId = langEquivFacade.findReportIdWithTgtLangPointingToEngEquiv( engEquivReportId, tgtLocale.toString() );

                if( langEquivReportId>0 )
                    return langEquivReportId;

                // Look for just language match
                langEquivReportId = langEquivFacade.findReportIdWithTgtLangPointingToEngEquiv( engEquivReportId, tgtLocale.getLanguage() );

                if( langEquivReportId>0 )
                    return langEquivReportId;
            }

            // No equiv report found.
            return 0;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.getLangEquivReportIdForProductReportAndLang() " + (p==null ? "product is null" : p.toString() ) + ", " + (r==null ? "report is null" : r.toString() ) + ", tgtLocale=" + tgtLocale.toString() );

            return 0;
        }
    }

    /*
      int[0]=reset count
      int[1]=Perm Error Count

    */
    protected int[] clearStartedReportTestKeys() throws Exception
    {
        int[] out = new int[2];

        try
        {
            // LogService.logIt( "BaseReportManager.clearStartedReportTestKeys() Starting "  );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            List<TestKey> ptkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.REPORTS_STARTED.getTestKeyStatusTypeId() , -1, true, -1, RuntimeConstants.getIntList("OrgIdsToSkip",",") );

            // LogService.logIt( "BaseReportManager.clearStartedReportTestKeys() Found  " + ptkl.size() + " keys to clear." );
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.MONTH, -1 );

            for( TestKey tk : ptkl )
            {
                // not in correct status
                if( tk.getTestKeyStatusTypeId()!=TestKeyStatusType.REPORTS_STARTED.getTestKeyStatusTypeId() )
                    continue;

                // skip - may be part of a thread.
                if( !BaseScoreManager.isScoringFirstTimeOrRepeatAllowed(tk.getTestKeyId()) )
                    continue;

                // Too many errors.
                if( tk.getErrorCnt()>=MAX_REPORT_ERRORS )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    out[1]++;
                    continue;
                }

                // too old - more than 1 month old.
                if( tk.getLastAccessDate()!=null && tk.getLastAccessDate().before( cal.getTime() ) )
                {
                    tk.setErrorCnt(MAX_REPORT_ERRORS+1);
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    out[1]++;
                    continue;
                }

                // reset but add an error to error count.
                //tk.setErrorCnt(tk.getErrorCnt()+1);
                tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORED.getTestKeyStatusTypeId() );
                eventFacade.saveTestKey(tk);
                out[0]++;
            }

            // LogService.logIt( "BaseReportManager.clearStartedReportTestKeys() reset " + out[0] + " report-started TestKeys. Advanced " + out[1] + " to report error." );
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseReportManager.clearStartedReportTestKeys() " );
            throw new STException( e );
        }
    }



    protected int[] clearReportErrorTestKeys() throws Exception
    {
        int[] out = new int[2];

        try
        {
            // LogService.logIt( "BaseReportManager.clearReportErrorTestKeys() Starting "  );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            List<TestKey> ptkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId() , -1, true, MAX_REPORT_ERRORS, null );

            // LogService.logIt( "BaseReportManager.clearReportErrorTestKeys() Found  " + ptkl.size() + " keys to clear." );

            for( TestKey tk : ptkl )
            {
                // should not happen.
                if( tk.getErrorCnt()>MAX_REPORT_ERRORS || !tk.getTestKeyStatusType().equals(TestKeyStatusType.REPORT_ERROR) )
                    continue;

                // increment partials
                if( resetTestKeyStatusForReports( tk ) )
                    out[0]++;
                // increment upgrades
                else
                    out[1]++;
            }

            if( out[0]>0 )
                LogService.logIt( "BaseReportManager.clearReportErrorTestKeys() cleaned " + out[0] + " partially completed TestKeys and upgraded " + out[1] + " completed testKeys to score complete status." );
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseReportManager.clearReportErrorTestKeys() " );
            throw new STException( e );
        }
    }


    protected boolean resetTestKeyStatusForReports( TestKey tk ) throws Exception
    {
        try
        {
            LogService.logIt( "BaseReportManager.resetTestKeyStatusForReports() Starting for TestKey=" + tk.getTestKeyId()  );

            if( !tk.getTestKeyStatusType().equals( TestKeyStatusType.REPORT_ERROR ) && !tk.getTestKeyStatusType().equals( TestKeyStatusType.REPORTS_STARTED ) )
                return false;

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            if( tk.getTestEventList()==null )
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );

            boolean hasUnreportedTestEvent = false;
            boolean hasCompletedReport = false;

            for( TestEvent te : tk.getTestEventList() )
            {
                // if not scored, return false;
                if( te.getTestEventStatusTypeId()<TestEventStatusType.SCORED.getTestEventStatusTypeId()  )
                    throw new ScoringException( "TestEvent is not yet scored. " + te.toString(), ScoringException.NON_PERMANENT, te );

                else if( te.getTestEventStatusTypeId()==TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId()  )
                    throw new ScoringException( "TestEvent is not yet scored - has a scoring error. " + te.toString(), ScoringException.NON_PERMANENT, te );

                else if( te.getTestEventStatusTypeId()==TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId()  )
                {
                    hasCompletedReport = true;
                    continue;
                }
                else if( te.getTestEventStatusTypeId()==TestEventStatusType.DEACTIVATED.getTestEventStatusTypeId() ||
                         te.getTestEventStatusTypeId()==TestEventStatusType.EXPIRED.getTestEventStatusTypeId()  )
                    continue;

                else if( te.getTestEventStatusTypeId()==TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId()  )
                {
                    hasUnreportedTestEvent = true;
                    te.setTestEventStatusTypeId(TestEventStatusType.SCORED.getTestEventStatusTypeId());
                    eventFacade.saveTestEvent(te);
                }
            }

            // OK to reset to Scored.
            if( hasUnreportedTestEvent )
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORED.getTestKeyStatusTypeId() );
                eventFacade.saveTestKey(tk);
                return true;
            }

            // do this so we stop looking at this TestKey if it doesn't need a report retry.
            else if( tk.getTestKeyStatusType().equals(TestKeyStatusType.REPORT_ERROR) )
            {
                if( hasCompletedReport )
                    tk.setTestKeyStatusTypeId( tk.getFirstDistComplete()==1 ? TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() : TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() );
                else
                    tk.setErrorCnt( MAX_REPORT_ERRORS+1 );

                eventFacade.saveTestKey(tk);
                return false;
            }

            // do this so we stop looking at this TestKey if it doesn't need a report retry.
            else if( tk.getTestKeyStatusType().equals(TestKeyStatusType.REPORTS_STARTED) )
            {
                if( hasCompletedReport )
                    tk.setTestKeyStatusTypeId( tk.getFirstDistComplete()==1 ? TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() : TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId());
                else
                {
                    tk.setTestKeyStatusTypeId(TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId());
                    tk.setErrorCnt( MAX_REPORT_ERRORS+1 );
                    eventFacade.saveTestKey(tk);
                    return false;
                }

                eventFacade.saveTestKey(tk);
                return false;
            }


            return false;
        }
        catch( ScoringException e )
        {
            LogService.logIt( "BaseReportManager.resetTestKeyStatusForReports() " + e.toString() + ", "  + tk.toString() );

            // incrementing report errors
            tk.setTestKeyStatusTypeId(TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId());
            tk.setErrorCnt( MAX_REPORT_ERRORS+1 );
            eventFacade.saveTestKey(tk);
            return false;
            // throw e;
        }

    }

    protected String getReportFilename( TestEvent te ) throws Exception
    {
        String out = "";

        if( userFacade == null )
            userFacade = UserFacade.getInstance();

        User user = userFacade.getUser( te.getUserId() );

        if( user.getUserType().getNamed() )
        {
            out = StringUtils.alphaCharsOnly( user.getLastName() );
            out = StringUtils.removeNonAscii(out);
            out = StringUtils.removeChar( out , ' ' );

            if( out.length() > 25 )
                out = out.substring(0, 25 );

            // No Acsii in name.
            if( out.length()==0 )
                out += Long.toString( te.getTestEventId() );

            out += "_";
        }

        else if( user.getUserType().getNamedUserIdUsername() )
        {
            out = StringUtils.alphaNumCharsOnly(user.getEmail() );
            out = StringUtils.removeNonAscii(out);
            out = StringUtils.removeChar( out , ' ' );
            if( out.length() > 25 )
                out = out.substring(0, 25 );

            // No Acsii in name.
            if( out.length()==0 )
                out += Long.toString( te.getTestEventId() );

            out += "_";
        }

        if( te.getProduct()!=null && te.getProduct().getName()!=null && !te.getProduct().getName().isBlank()  )
        {
            String  prodNm = StringUtils.alphaNumCharsOnly(te.getProduct().getName());
            //prodNm = StringUtils.removeNonAscii(prodNm);
            //prodNm = StringUtils.removeChar( prodNm , ' ' );
            if( prodNm.length() > 25 )
                prodNm = prodNm.substring(0, 25 );
            if( prodNm.length()>5 )
                out += prodNm + "_";
        }


        String comprRptNm = StringUtils.removeChar( te.getReport().getTitle()!=null && !te.getReport().getTitle().isEmpty() ? te.getReport().getTitle() : te.getReport().getName() , ' ' );

        comprRptNm = StringUtils.alphaCharsOnly( comprRptNm );

        if( comprRptNm.length() > 25 )
            comprRptNm = comprRptNm.substring(0, 25 );

        out += comprRptNm + "_";

        Calendar c = new GregorianCalendar();

        out += c.get( Calendar.YEAR ) + "-" + ( c.get( Calendar.MONTH ) + 1 ) + "-" + c.get( Calendar.DAY_OF_MONTH );

        return out;
    }

    protected void saveErrorInfo( ScoringException se )
    {
        LogService.logIt( "ReportManager.saveErrorInfo() IGNORING, since it's a Scoring Exception which is most likely caused by an erroneous . " + se.toString() );
    }

    protected void saveErrorInfo( ReportException se )
    {
        try
        {
            LogService.logIt( "BaseReportManager.saveErrorInfo() " + se.toString() );


            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            TestEvent te = se.getTestEvent();

            TestEventLog testEventLog = new TestEventLog();
            testEventLog.setTestEventId( te.getTestEventId() );
            testEventLog.setTestKeyId( te.getTestKeyId() );
            testEventLog.setLevel(0);
            testEventLog.setLog( "BaseReportManager.saveErrorInfo() ReportException caught. se.msg=" + se.getMessage() + ", testEventId=" + te.getTestEventId() ) ;

            if( te.getTestEventScoreList() == null )
                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true ) );

            TestEventScore tes = te.getTestEventScoreForReportId(se.getReportId(), se.getReportLangStr()==null || se.getReportLangStr().isEmpty() ? "en_US" : se.getReportLangStr() );

            if( tes == null )
            {
                tes = new TestEventScore();
                tes.setTestEventId( te.getTestEventId() );
            }

            tes.setReportId( se.getReportId() );
            tes.setStrParam1(se.getReportLangStr()==null || se.getReportLangStr().isEmpty() ? "en_US" : se.getReportLangStr() );
            tes.setTestEventScoreTypeId( TestEventScoreType.REPORT.getTestEventScoreTypeId() );
            tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.REPORT_ERROR.getTestEventScoreStatusTypeId() );

            // erase any report data.
            tes.setReportBytes(null);
            tes.setReportFilename(null);
            tes.setReportFileContentTypeId( 0 );
            tes.setDisplayOrder( te.getTestEventScoreList().size() + 1 );

            tes.appendErrorTxt(  new Date().toString() + " " + se.getMessage() );

            eventFacade.saveTestEventScore(tes);

            if( se.getSeverity()==ReportException.PERMANENT && te.getTestEventStatusTypeId()<= TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() )
            {
                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent( te );
            }

            te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), false ) );

            if( testEventLog.getTestKeyId()>0 || testEventLog.getTestEventId()>0 )
                eventFacade.saveTestEventLog(testEventLog);

        }

        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.saveErrorInfo() " );
        }
    }

}
