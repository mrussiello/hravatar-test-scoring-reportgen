/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.bestjobs.BestJobsReportFacade;
import com.tm2score.custom.bestjobs.BestJobsReportUtils;
import com.tm2score.custom.ivr.IvrReportUtils;
import com.tm2score.dist.DistManager;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.proctor.ProctorEntry;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.onet.Soc;
import com.tm2score.entity.proctor.ProctorSuspension;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.profile.ProfileEntry;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.entity.user.User;
import com.tm2score.event.*;
import com.tm2score.file.FileContentType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.onet.OnetFacade;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.purchase.ProductType;
import com.tm2score.proctor.ProctorFacade;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.scorer.BaseTestEventScorer;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.sim.CategoryDistType;
import com.tm2score.sim.OverallRawScoreCalcType;
import com.tm2score.sim.OverallScaledScoreCalcType;
import com.tm2score.sim.ScorePresentationType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyRawScoreCalcType;
import com.tm2score.sim.SimCompetencySortType;
import com.tm2score.sim.SimJUtils;
import com.tm2score.user.UserFacade;
import com.tm2score.util.SSLUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.JaxbUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;

/**
 *
 * @author Mike
 */
public class ReportManager extends BaseReportManager
{
    public static boolean FIRST_BATCH = true;

    public static boolean OK_TO_START_ANY = true;

    public static boolean BATCH_IN_PROGRESS = false;

    
    public ReportManager()
    {
        if( !INIT_COMPLETE )
            init();
    }

    public synchronized void init()
    {
        if( INIT_COMPLETE )
            return;
        
        if( RuntimeConstants.getBooleanValue( "disableCertificateVerification" ) )
            SSLUtils.disableSslVerification();
        
        INIT_COMPLETE = true;
    }
    
    public void doReportBatch(boolean noThread)
    {
        // LogService.logIt( "ReportManager.doReportBatch() Starting BATCH_IN_PROGRESS=" + BATCH_IN_PROGRESS + ", ScoreManager.BATCH=" + ScoreManager.BATCH_IN_PROGRESS + ", DistManager.BATCH=" + DistManager.BATCH_IN_PROGRESS );

        // Check for tests needing score
        try
        {
            if( !OK_TO_START_ANY )
                return;

            if( RuntimeConstants.getBooleanValue( "ForceNoThreadBasedBatchesInScoreReportDistrib") )
                noThread = true;
            
            
            if( noThread && ( ScoreManager.BATCH_IN_PROGRESS || ReportManager.BATCH_IN_PROGRESS || DistManager.BATCH_IN_PROGRESS ) )
                return;
            //if( BATCH_IN_PROGRESS )
            //    return;

            if( noThread )
                BATCH_IN_PROGRESS = true;

            // ReportManager rm = new ReportManager();

            int[] count = generateReportBatch(false, noThread, false );

            FIRST_BATCH = false;
            
            if( noThread )
                BATCH_IN_PROGRESS = false;

            // LogService.logIt( "ReportManager.doReportBatch() Completed generating reports for " + count[0] + " TestKeys, total of " + count[1] + " TestEvents" );
        }

        catch( STException e )
        {
            // do nothing. Already logged.
            if( noThread )
                BATCH_IN_PROGRESS = false;
        }

        catch( Exception e )
        {
            BATCH_IN_PROGRESS = false;
            LogService.logIt(e, "ReportManager.doReportBatch() " );
        }

    }


    /*
      out[0]=test keys processed.
      out[1]=test events
      out[2]=errors
     out[3]=partial battery test events
    
    */
    public int[] generateReportBatch(boolean withArchive, boolean noThread, boolean skipCompleted ) throws Exception
    {
        // LogService.logIt( "ReportManager.generateReportBatch() Starting batch. " );

        Tracker.addReportBatch();

        int[] out = new int[4];

        if( !OK_TO_START_ANY )
        {
            LogService.logIt( "ReportManager.generateReportBatch() Starting batch. Not OK To Start Any" );
            return out;
        }

        if( eventFacade == null ) 
            eventFacade = EventFacade.getInstance();

        if( FIRST_BATCH && RuntimeConstants.getBooleanValue("seekStartedReportsFirstBatch") )
        {
            int[] vs = clearStartedReportTestKeys();
            if( vs[0]>0 )
                LogService.logIt( "ReportManager.generateReportBatch() Reset " + vs[0] + " previously started (reports) test keys for retry. Advanced " + vs[1] + " to error status." );
        }
        
        
        if( RuntimeConstants.getBooleanValue("retryErroredReportsInBatch") )
        {
            int[] vs = clearReportErrorTestKeys();
            if( vs[0]>0 )
                LogService.logIt( "ReportManager.generateReportBatch() Cleared " + vs[0] + " Report_Error test keys for retry." );
        }
            
        List<Integer> orgIdsToSkip = RuntimeConstants.getIntList("OrgIdsToSkip",",");
        
        List<TestKey> tkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.SCORED.getTestKeyStatusTypeId(), 0, withArchive, -1, orgIdsToSkip );
        
        //if( FIRST_BATCH && RuntimeConstants.getBooleanValue("seekStartedReportsFirstBatch") )
        //{
        //    tkl.addAll(eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.REPORTS_STARTED.getTestKeyStatusTypeId(), -1, true, -1 ) );
        //}
        //if( RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes" ) > 0 )
        
        if( tkl.size() < 20 || withArchive || FIRST_BATCH )
            tkl.addAll(eventFacade.getNextBatchOfTestKeyArchivesToScore(TestKeyStatusType.SCORED.getTestKeyStatusTypeId(), orgIdsToSkip, 0 ) );

        removeTestKeysRequiringDelay( tkl );
        
        // if( tkl.size()>0 )
        //     LogService.logIt( "ReportManager.generateReportBatch() found " + tkl.size() + " testkeys to generate report for." );

        int errCount = 0;

        boolean useThread = !noThread;

        long delayBetweenReports = 250;
        
        // for each testkey, get the test events
        testkeyloop:
        for( TestKey tk : tkl )
        {
            if( !BaseScoreManager.isScoringFirstTimeOrRepeatAllowed( tk.getTestKeyId() ) )
            {
                LogService.logIt("ReportManager.generateReportBatch() Last Scoring Call was within min last date window. IGNORING Report Gen for this Test Key for now. testKeyId=" + tk.getTestKeyId() );  
                continue;                                                    
            } 
            
            BaseScoreManager.addTestKeyToDateMap( tk.getTestKeyId() );
                        
            errCount = 0;

            // refresh just in case a parallel process got it while waiting.
            tk = eventFacade.getTestKey(tk.getTestKeyId(), true );

            tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_STARTED.getTestKeyStatusTypeId() );

            eventFacade.saveTestKey( tk );

            if( tk.getTestEventList()==null )
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );
            
            for( TestEvent te : tk.getTestEventList() )
            {
                if( te.getProduct()==null )
                    te.setProduct( eventFacade.getProduct( te.getProductId() ));
            }

            // Sort to place the Riasec events at the end. So, other reports are generated first.
            Collections.sort( tk.getTestEventList(), new TestEventProductInParam4Comparator() );
            // get events
            // tk.setTestEventList(eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ));

            useThread = !noThread;
            
            if( useThread && ScoreManager.getTestKeysAndPartialEventsInScoringCount()>=ScoreManager.MAX_THREAD_TESTKEY_SCORE_COUNT )
            {
                LogService.logIt( "ReportManager.generateReportBatch() Shifting to NoThread for testKeyId=" + tk.getTestKeyId() );  
                useThread=false;
                delayBetweenReports = 1000;
            }            
            
            if( !useThread )
            {

                for( TestEvent te : tk.getTestEventList() )
                {
                    // skip if report complete
                    if( te.getTestEventStatusTypeId() >= TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() )
                        continue;

                    // this should not happen, but in case.
                    if( te.getTestEventStatusTypeId() < TestEventStatusType.SCORED.getTestEventStatusTypeId() )
                    {
                        LogService.logIt( "ReportManager.generateReportBatch() Scoring testKeyId=" + tk.getTestKeyId() + ", but member TestEventId=" + te.getTestEventId() + " has invalid TestEventStatusTypeId=" + te.getTestEventStatusTypeId() + ", so skipping report generation for this testKey for now." );  
                        continue testkeyloop;
                    }

                    try
                    {
                        //if( noThread )
                        //{
                            generateReports(te, tk, 0, false, false, null, skipCompleted, errCount);
                            out[1]++;
                        //}
                        //else
                        //{
                        //    LogService.logIt( "ReportManager.generateReportBatch() Starting Report Thread teid=" + te.getTestEventId() + ", tkid=" + tk.getTestKeyId() );
                        //    new Thread(new TestKeyReportThread( te, tk, 0, false )).start(); 
                        //    Thread.sleep(100);
                        //}
                    }

                    catch( ReportException e )
                    {
                        saveErrorInfo(e);
                        out[2]++;
                    }
                }

                if( errCount <= 0 )
                {
                    tk.setTestKeyStatusTypeId( tk.getFirstDistComplete()==1 ? TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() : TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId()  );
                    tk.setErrorCnt( 0 );
                }

                else
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId()  );
                    tk.setErrorCnt( tk.getErrorCnt()+1 );
                    Tracker.addReportError();
                }

                eventFacade.saveTestKey( tk );
                
                // since processing for this test key will end here, remove from list
                BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );

                out[0]++;

                if( tk.getTestKeyStatusTypeId()==TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() && tk.getTestKeyArchiveId()<=0 )
                {
                    (new EventArchiver()).archiveTestKey(tk);
                }
                
                Thread.sleep(delayBetweenReports);
            }
            
            // Threaded
            else
            {
                try
                {
                    // LogService.logIt( "ReportManager.generateReportBatch() Starting Report Thread tkid=" + tk.getTestKeyId() );
                    new Thread(new TestKeyReportThread( tk, 0, false, skipCompleted )).start(); 
                    Thread.sleep(delayBetweenReports);                
                }
                catch( Exception e )
                {
                    LogService.logIt( e, "ReportManager.generateReportBatch() Exception Starting Report Thread tkid=" + tk.getTestKeyId() );
                    out[2]++;
                    // since processing for this test key will end here, remove from list
                    BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );
                }
            }
        }
        
        List<Long> partialCompleteBatteryTestEventIdsToScore = eventFacade.getIncompleteBatteryTestEventIdsToScore( 100, true, 0 );
        // LogService.logIt( "ReportManager.generateReportBatch() partialCompleteBatteryTest.size=" + partialCompleteBatteryTesteventIdsToScore.size() );
        TestEvent te;
        for( Long teid : partialCompleteBatteryTestEventIdsToScore )
        {
            te = eventFacade.getTestEvent(teid, true);
            if( !te.getTestEventStatusType().getIsScored())
            {
                LogService.logIt( "ReportManager.generateReportBatch() skipping partialCompleteBatteryTestEventId=" + teid + " because testEvent is no longer in scored status" );
                continue;
            }
            te.setTestKey( eventFacade.getTestKey( te.getTestKeyId(), true));
            if( te.getTestKey().getTestKeyStatusType().getIsCompleteOrHigher() )
            {
                LogService.logIt( "ReportManager.generateReportBatch() skipping partialCompleteBatteryTestEventId=" + teid + " because test key is now in complete or higher status" );
                continue;
            }
            
            if( !BaseScoreManager.isPartialBatteryTestEventScoringFirstTimeOrRepeatAllowed(te.getTestEventId() ) )
            {
                LogService.logIt("ReportManager.generateReportBatch() Last Scoring Call for partial testevent was within min last date window for partially complete battery test event. IGNORING REPORT GEN for Test Event for now. testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId()  ); 
                continue;
            } 
            
            BaseScoreManager.addTestEventToPartialDateMapIfNew( te.getTestEventId() );
            
            te.setPartialBatteryTestEvent(true);
            
            if( noThread )
            {
                TestKeyReportThread tkrt = new TestKeyReportThread(te, 0, false );
                if( tkrt.generatePartialBatteryReport() )
                    out[3]++;
            }
            
            else
            {
                // LogService.logIt( "ScoreManager.scoreBatch() Starting Score Thread tkid=" + tk.getTestKeyId() );
                new Thread( new TestKeyReportThread( te, 0, false ) ).start();
                Thread.sleep(100);                
            }
        }        

        return out;
    }

    
    private void removeTestKeysRequiringDelay( List<TestKey> tkl )
    {        
        // OLD Method, delay by a certain amount.
        /*
        if( RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes") > 0 )
        {
            // In order to give Optional Interest Inventory Tests time to have companion Job Specific Tests 
            // fully scored (some will be delayed by essay scoring), 
            // force them to wait 30 mins.
            ListIterator<TestKey> li = tkl.listIterator();

            TestKey tk;

            Calendar cal=null;

            while( li.hasNext() )
            {
                tk = li.next();

                // Only this type of test gets Delayed, so that others it will use for scoring can complete their scoring. 
                if( tk.getTestKeySourceTypeId()==TestKeySourceType.OPTIONALAUTOTEST.getTestKeySourceTypeId() )
                {
                    if( cal == null )
                    {
                        cal = new GregorianCalendar();

                        cal.add( Calendar.MINUTE, -1*RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes") );
                    }

                    // if not at least 30 mins old, skip this one. 
                    if( tk.getLastAccessDate()!=null && tk.getLastAccessDate().after( cal.getTime() ) )
                    {
                        LogService.logIt( "EventFacade.removeTestKeysRequiringDelay() waiting to score OptionalSurvey test TestKeyId=" + tk.getTestKeyId() );
                        li.remove();
                    }
                }
            }
        }
        */
        
        // New method, just wait until any associated JobSpec Keys are graded.
        
        // If the test doesn't have any riasec competencies, keep (product.intParam4=1) 
        // if the test is not a standalone interest inventory, keep
        // so, if the test is a standalone and has riasec, see if there's a close-by 
        try
        {
            // In order to give Optional Interest Inventory Tests time to have companion Job Specific Tests 
            // fully scored (some will be delayed by essay scoring), 
            // force them to wait 30 mins.
            ListIterator<TestKey> li = tkl.listIterator();

            TestKey tk;
            
            Product p;
            
            Battery b;

            Calendar cal=null;
            
            long testEventId = 0;

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();
            
            if( bestJobsReportFacade == null )
                bestJobsReportFacade = BestJobsReportFacade.getInstance();
            
            TestEvent mte;
            
            while( li.hasNext() )
            {
                testEventId=0;
                mte = null;
                
                tk = li.next();

                p = eventFacade.getProduct( tk.getProductId() );
                
                // If the testkey is a battery. 
                if( p.getProductType().getIsAnyBattery() && tk.getBatteryId()>0 )
                {
                   //boolean hasRiasecStandalone = false;
                   
                   b = eventFacade.getBattery( tk.getBatteryId() );
                   
                   for( Integer bp : b.getProductIdList() )
                   {
                       p = eventFacade.getProduct( bp );
                       
                       if( p==null )
                           continue;
                       
                       if( p.getIntParam4()==1 && getRiasecStandaloneProductIdsList().contains( p.getProductId()) )
                           break;
                       
                       p=null;
                   }
                }

                // not a battery, see if it has comeptencies and if it is a standalone
                else if( p.getIntParam4()!=1 || !getRiasecStandaloneProductIdsList().contains( p.getProductId()) )
                    p=null;
                
                // no Riasec Product found.
                if( p==null )
                    continue;
                
                if( tk.getTestEventList()==null )
                    tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );
                
                // if p!=null, then this is the Riasec Stanlone in this TestKey, now get TestEvent
                List<TestEvent> tel = tk.getTestEventList(); // eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true );

                for( TestEvent te : tel )
                {
                    if( te.getProductId()==p.getProductId() )
                    {
                        testEventId = te.getTestEventId();
                        break;
                    }
                }
                
                // No matching TestEvent found
                if( testEventId<=0 )
                    continue;
                
                mte = bestJobsReportFacade.findMatchingJobSpecificTestEvent( testEventId, tk.getUserId(), tk.getOrgId() );

                // none found, or it's already got reports, or it's in the same testkey - yea.
                if( mte==null || 
                    mte.getTestEventStatusType().getIsReportsCompleteOrHigher() || 
                    ( mte.getTestKeyId()==tk.getTestKeyId() && mte.getTestEventStatusType().getIsScoredOrHigher() ) )
                    continue;
                
                
                // If already delayed 60 mins.
                if( cal == null )
                {
                    cal = new GregorianCalendar();

                    cal.add( Calendar.MINUTE, -1*RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes") );
                }

                // if more than 60 mins old, let it go 
                if( tk.getLastAccessDate()!=null && tk.getLastAccessDate().before( cal.getTime() ) )
                    continue;

                // OK, delay it.
                LogService.logIt( "EventFacade.removeTestKeysRequiringDelay() waiting to score OptionalSurvey test TestKeyId=" + tk.getTestKeyId() );
                li.remove();
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportManager.removeTestKeysRequiringDelay() " );
        }
    }    
    
    
    private List<Integer> getRiasecStandaloneProductIdsList()
    {
        String ids = RuntimeConstants.getStringValue("Hra_Interest_Inventory_ProductIds_ALL" );
        
        String[] idz = ids.split(",");
        
        List<Integer> out = new ArrayList<>();
        
        for( String s : idz )
        {
            try
            {
              out.add(Integer.valueOf(s));  
            }
            catch( NumberFormatException e )
            {}
        }
        
        return out;
    }
    

    public int genOrRegenReportsTestKey( long testKeyId, long reportId, boolean forceCalcSection, boolean sendResendCandidateReports, Date forceSendCandidatemaxCandidateSendDate, boolean skipCompleted) throws Exception
    {
        try
        {
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(testKeyId, true );

            if( tk == null )
                throw new Exception( "TestKey not found." );

            if( tk.getTestKeyStatusTypeId() < TestKeyStatusType.SCORED.getTestKeyStatusTypeId() )
                throw new Exception( "TestKey is not scored." );

            if( tk.getTestKeyStatusTypeId() < TestKeyStatusType.REPORTS_STARTED.getTestKeyStatusTypeId() ||
                tk.getTestKeyStatusTypeId() == TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId() )
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_STARTED.getTestKeyStatusTypeId() );
                eventFacade.saveTestKey(tk);
            }

            if( userFacade == null ) 
                userFacade = UserFacade.getInstance();

            tk.setOrg( userFacade.getOrg( tk.getOrgId() ) );

            if( tk.getSuborgId() > 0 )
                tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));

            tk.setUser( userFacade.getUser( tk.getUserId() ) );

            if( tk.getAuthorizingUserId() > 0 )
                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ) );

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

            
            if( tk.getTestEventList()==null )
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );
            
            List<TestEvent> tel = tk.getTestEventList(); // eventFacade.getTestEventsForTestKeyId(testKeyId, true );

            int count = 0;
            int errCount = 0;

            BaseScoreManager.addTestKeyToDateMapIfNew( tk.getTestKeyId() );
            
            for( TestEvent te : tel )
            {
                te.setTestKey( tk );
                te.setProduct( eventFacade.getProduct( te.getProductId() ) );

                try
                {
                    generateReports(te, tk, reportId > 0 ? reportId : 0, forceCalcSection, false, null, skipCompleted, errCount);
                    count++;
                }

                catch( ReportException e )
                {
                    saveErrorInfo(e);
                    errCount++;
                }
            }


            if( errCount > 0 && tk.getTestKeyStatusTypeId() > TestKeyStatusType.COMPLETED_PENDING_EXTERNAL.getTestKeyStatusTypeId() && tk.getTestKeyStatusTypeId() < TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() )
            {
                Tracker.addReportError();
                tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORT_ERROR.getTestKeyStatusTypeId() );
                tk.setErrorCnt( tk.getErrorCnt()+1 );
                eventFacade.saveTestKey( tk );
            }

            BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );
            
            if( errCount == 0 && tk.getTestKeyStatusTypeId() < TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() )
            {
                tk.setTestKeyStatusTypeId( tk.getFirstDistComplete()==1 ? TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() : TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() );
                tk.setErrorCnt( 0 );
                eventFacade.saveTestKey( tk );

                if( tk.getTestKeyStatusTypeId()==TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() && tk.getTestKeyArchiveId()<=0 )
                    (new EventArchiver()).archiveTestKey(tk);
            }

            
            return count;
        }

        catch( ScoringException e )
        {
            BaseScoreManager.removeTestKeyFromDateMap( testKeyId );
            throw e;
        }
        
        catch( STException e )
        {
            BaseScoreManager.removeTestKeyFromDateMap( testKeyId );
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.genOrRegenReportsTestKey() testKeyId=" + testKeyId + ", reportId=" + reportId );
            BaseScoreManager.removeTestKeyFromDateMap( testKeyId );
            throw new STException( e );
        }
    }

    public List<Report> genRegenReportTestEvent( long testEventId, long reportId, boolean forceCalcSection, boolean skipCompleted, boolean sendResendCandidateReportEmails, Date maxLastCandidateSendDate) throws Exception
    {
        
        List<TestEventScore> tesl;
        
        try
        {
            // LogService.logIt("ReportManager.genRegenReportTestEvent() testEventId=" + testEventId + ", reportId=" + reportId + " Locale=" + (forceLocale == null ? "None" : forceLocale.toString() ) );

            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            TestEvent te = eventFacade.getTestEvent(testEventId, true );

            if( te == null )
                throw new Exception( "Could not find TestEvent " + testEventId );

            TestKey tk = eventFacade.getTestKey( te.getTestKeyId(), true );

            if( tk == null )
                throw new Exception( "TestKey " + te.getTestKeyId() + " not found." );

            // TestKeyStatusType oldTkst = tk.getTestKeyStatusType();
            
            if( tk.getTestKeyStatusTypeId() < TestKeyStatusType.SCORED.getTestKeyStatusTypeId() )
                throw new Exception( "TestKey " + te.getTestKeyId() + "  is not scored." );

            if( te.getTestEventStatusTypeId() < TestEventStatusType.SCORED.getTestEventStatusTypeId() )
                throw new Exception( "TestEvent " + te.getTestKeyId() + "  is not scored." );

            if( te.getTestEventStatusTypeId() < TestEventStatusType.REPORT_STARTED.getTestEventStatusTypeId() ||
                te.getTestEventStatusTypeId() == TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId() )
            {
                te.setTestEventStatusTypeId( TestEventStatusType.REPORT_STARTED.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
            }

            if( userFacade == null ) 
                userFacade = UserFacade.getInstance();

            if( tk.getOrg() == null )
                tk.setOrg( userFacade.getOrg( tk.getOrgId() ) );

            if( tk.getSuborgId() > 0 && tk.getSuborg()==null )
                tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));

            if( tk.getUser() == null )
                tk.setUser( userFacade.getUser( tk.getUserId() ) );

            if( tk.getAuthorizingUserId() > 0 )
                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ) );

            te.setTestKey( tk );

            if( te.getProduct() == null )
                te.setProduct( eventFacade.getProduct( te.getProductId() ) );

            tesl = eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true );
            
            // Remove report-level info from report TestEventScores
            for( TestEventScore tes : tesl )
            {
                if( !tes.getTestEventScoreType().getIsReport() )
                    continue;

                if( tes.getTextParam1()!=null && !tes.getTextParam1().isBlank() )
                {
                    String b = StringUtils.getBracketedArtifactFromString(tes.getTextParam1(), Constants.RIASEC_COMPACT_INFO_KEY);
                    if( b==null || b.isBlank() )
                        b = StringUtils.getBracketedArtifactFromString(tes.getTextParam1(), Constants.EEOCAT_COMPACT_INFO_KEY);
                    if( b!=null && !b.isBlank() )
                    {
                        b = StringUtils.removeBracketedArtifactFmStr(tes.getTextParam1(), Constants.RIASEC_COMPACT_INFO_KEY);
                        if( b==null )
                            b = "";
                        if(!b.isBlank() )
                            b = StringUtils.removeBracketedArtifactFmStr(b, Constants.EEOCAT_COMPACT_INFO_KEY);
                        tes.setTextParam1(b);
                        eventFacade.saveTestEventScore(tes);
                    }
                }
            }
            
            
            //if( forceLocale != null )
            //{
                //tk.setLocaleStrReport( forceLocale.toString() );
                //te.setLocaleStrReport( forceLocale.toString() );
            //}
            BaseScoreManager.addTestEventToPartialDateMapIfNew(te.getTestEventId());
            
            
            
            List<Report> out = generateReports(te, tk, reportId > 0 ? reportId : 0, forceCalcSection, sendResendCandidateReportEmails, maxLastCandidateSendDate, skipCompleted, 0);

            /*
            if( r != null )
                out.add( r );

            if( reportId <= 0 && te.getProduct().getLongParam3()> 0 )
            {
                r = generateReports( te, tk, te.getProduct().getLongParam3() );

                if( r != null )
                    out.add( r );
            }
            */
            BaseScoreManager.removeTestEventFromPartialDateMap(testEventId);

            return out;
        }

        catch( ScoringException e )
        {
            BaseScoreManager.removeTestEventFromPartialDateMap(testEventId);
            throw e;
        }        
        catch( ReportException e )
        {
            saveErrorInfo(e);
            BaseScoreManager.removeTestEventFromPartialDateMap(testEventId);
            throw new Exception( e.getMessage() );
        }
        catch( STException e )
        {
            BaseScoreManager.removeTestEventFromPartialDateMap(testEventId);
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.genRegenReportTestEvent() testEventId=" + testEventId + ", reportId=" + reportId );
            BaseScoreManager.removeTestEventFromPartialDateMap(testEventId);
            throw new STException( e );
        }
    }




    public TestEventScore generateReportForTestEventAndLanguage( long testEventId, long reportId, String langStr, int includeEnglishReport, boolean forceCalcSection, boolean forceSendCandidateReports, Date maxLastCandidateSendDate) throws Exception
    {
        try
        {
            Date procStart = new Date();

            // LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() START creating report.  testEventId=" + testEventId + ", reportId=" + reportId + ", langStr=" + langStr   );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            TestEvent te = eventFacade.getTestEvent(testEventId, true );

            if( te == null )
            {
                throw new ReportException( "Cannot generate report. Test Event is null. testEventId=" + testEventId, ReportException.PERMANENT, null, reportId, langStr );
                // throw new Exception( "Test Event not found. " + testEventId );
            }

            if(  te.getTestEventStatusTypeId()<TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() || te.getTestEventStatusType().getIsError() )
            {
                throw new ReportException( "Cannot generate report. Test Event is in invalid status: " + te.toString(), ReportException.PERMANENT, te, reportId, langStr );
                // throw new Exception( "Cannot generate report. Test Event is in invalid status: " + te.toString() );
            }

            te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( testEventId, true ));

            TestKey tk = eventFacade.getTestKey( te.getTestKeyId() , true );

            if( te == null )
                throw new ReportException( "Test Event not found. testKeyId=" + te.getTestKeyId() + ", testEventId=" + testEventId, ReportException.PERMANENT, null, reportId, langStr );
                // throw new Exception( "Test Event not found. testKeyId=" + te.getTestKeyId() );

            Profile prof =  ProfileUtils.getLiveProfileForProductIdAndOrgId( te.getTestEventId(), te.getProductId(), te.getOrgId(), ProfileUsageType.REPORT_RANGES.getProfileUsageTypeId() );

            te.setProfile( prof );

            Product product = eventFacade.getProduct( te.getProductId() );

            if( product == null )
                throw new ReportException( "ProductId is invalid. ProductId=" + te.getProductId() + ", testKeyId=" + te.getTestKeyId() + ", testEventId=" + testEventId, ReportException.PERMANENT, null, reportId, langStr );

            te.setProduct(product);

            long simId = product.getLongParam1();

            if( simId <=0 )
                throw new ReportException( "Product.simId invalid: simId=" + simId + ", ProductId=" + te.getProductId() + ", not found. testKeyId=" + te.getTestKeyId() + ", testEventId=" + testEventId, ReportException.PERMANENT, null, reportId, langStr );

            SimDescriptor sd = eventFacade.getSimDescriptor(simId, 0, true );

            if( sd == null )
                throw new Exception( "No sim descriptor found for simId=" + simId + ", " + product.toString() );

            te.setSimDescriptor(sd);

            if( reportId <= 0 )
                reportId = te.getReportId();

            if( reportId<=0 )
                reportId = product.getLongParam2();

            if( reportId<= 0 )
                throw new ReportException( "Invalid reportId: reportId=" + reportId + ", simId=" + simId + ", ProductId=" + te.getProductId() + ", not found. testKeyId=" + te.getTestKeyId() + ", testEventId=" + testEventId, ReportException.PERMANENT, null, reportId, langStr );

            // LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() reportId= " + reportId  );

            if( userFacade == null )
                userFacade = UserFacade.getInstance();

            // tk.Org
            Org org = userFacade.getOrg( tk.getOrgId() );

            // tk.User
            User user = userFacade.getUser( tk.getUserId() );

            // tk.authUser
            User authUser = userFacade.getUser( tk.getAuthorizingUserId() );

            tk.setOrg( org );

            if( tk.getSuborgId()> 0 && tk.getSuborg()== null )
                tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));

            tk.setUser( user );
            tk.setAuthUser(authUser);
            
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

            Report r = eventFacade.getReport(reportId);

            if( r == null )
                throw new Exception( "Report not found: " + reportId );
            
            r = (Report) r.clone();

            if( tk.getAuthUser() == null && tk.getAuthorizingUserId()> 0 )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
            }

            if( tk.getAiMetaScoreList()==null )
                tk.setAiMetaScoreList( eventFacade.getReportableAiMetaScoreListForTestKey( tk.getTestKeyId()));
            
            
            
            ReportData rd = new ReportData( tk, te, r, tk.getUser(), tk.getOrg(), te.getProfile() );

            if( forceCalcSection )
                rd.forceCalcSection = forceCalcSection;
            
            te.setReport( r );

            if( langStr != null && !langStr.isEmpty() )
                r.setLocaleForReportGen( I18nUtils.getLocaleFromCompositeStr( langStr ) );
            else if( te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
                r.setLocaleForReportGen( I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() ) );
            else if( r.getLocaleStr()!= null && !r.getLocaleStr().isEmpty() )
                r.setLocaleForReportGen( I18nUtils.getLocaleFromCompositeStr( r.getLocaleStr() ) );
            else if( tk.getAuthUser()!=null && tk.getAuthUser().getLocaleStr()!=null && !tk.getAuthUser().getLocaleStr().isEmpty()  )
                r.setLocaleForReportGen( I18nUtils.getLocaleFromCompositeStr( tk.getAuthUser().getLocaleStr() ) );
            else
                r.setLocaleForReportGen( Locale.US );

            Locale rptLocale = r.getLocaleForReportGen();
            
            // LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() BBB creating report for " + te.toString() + ", " + r.toString() + ", report language=" + r.getLocaleForReportGen().toString() );

            boolean createPdfDoc = r.getNoPdfDoc()==0;

            byte[] rptBytes = null;

            
            // SimJ simJ = JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() );
            
            long engEquivSimId = product.getLongParam4();
            
            TestEventScore tes = null;         
            
            if( createPdfDoc )
            {
                long langEquivSimId = 0;
                long langEquivReportId = 0;
            
                // Report Locale is different from Product Locale.
                if( rptLocale != null && 
                    (te.getProduct().getProductType().getIsSimOrCt5Direct() ) && // It's a sim
                    !I18nUtils.getLanguageFromLocale(rptLocale).equals( I18nUtils.getLanguageFromLocaleStr(te.getProduct().getLangStr()) ) // Report language different from Test language
                  )
                {
                    langEquivSimId = getLangEquivSimIdForProductAndLang( te.getProduct(), rptLocale );
                    
                    if( langEquivSimId>0 )
                        langEquivReportId = getLangEquivReportIdForProductReportAndLang(te.getProduct(), r, rptLocale);

                    LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() CCC target report language is " + rptLocale.toString() + ", simId=" + te.getSimId() + ", langEquivSimId=" + langEquivSimId + ", reportId = " + reportId + ", languageEquivReportId=" + langEquivReportId  );
                }
                        
                // If forced to generate in a different language from product, use langEquivSimId
                if( langEquivSimId>0 )
                    //    rptLocale != null && te.getProduct().getProductType().getIsSim() && // It's a sim
                    //!I18nUtils.getLanguageFromLocale(rptLocale).equals( I18nUtils.getLanguageFromLocaleStr(te.getProduct().getLangStr()) ) && 
                    //r.getLongParam1()>0 && // Report has an english equivalent report
                    //te.getProduct().getProductType().getIsSim() && // It's a sim
                    //te.getProduct().getLangStr()!=null && !te.getProduct().getLangStr().startsWith( "en" ) &&  // Test not written in english
                    //te.getProduct().getLongParam4()> 0 )  // test has an english equivalent Sim
                {
                    LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() DDD.1 since target report language is " + rptLocale.toString() + " AND the Sim is in "  + te.getProduct().getLangStr() + " AND there is an equivalent sim for this sim in the target report language (langEquivSimId=" + langEquivSimId + "), generating an equivalent report. reportId = " + reportId + ", languageEquivReportId=" + langEquivReportId  );
                    Object[] ot = generateLanguageEquivReport(te, tk, reportId, rptLocale, langEquivSimId, langEquivReportId, 0, false, forceCalcSection );  
                    
                    // THIS IS NOT SAVED AT THIS TIME. Could be an existing, could be not.
                    tes = (TestEventScore) ot[2];
                    
                    if( tes!=null )
                        rptBytes = tes.getReportBytes();

                    LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() DDD.2 tes=" + (tes==null ? "null" : "not null, testEventScoreId=" + tes.getTestEventScoreId() +", bytes=" + (rptBytes==null ? "null" : rptBytes.length))  );
                    
                    // No need to do this twice.
                    if( I18nUtils.getLanguageFromLocale(rptLocale).equals("en") )
                        includeEnglishReport = -1; 
                }
                
                // Only log if the languages are different.
                else if( !I18nUtils.getLanguageFromLocale(rptLocale).equals( I18nUtils.getLanguageFromLocaleStr(te.getProduct().getLangStr()) ) )
                    LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() No target language equivalent Sim found for requested rptLocale=" + rptLocale.toString() + " where sim product locale is "  + te.getProduct().getLangStr()  );
                    
                
                // Did not generate a Language Equivalent Report
                if( rptBytes==null )
                {
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

                    // LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() GeneratingReport() START" );

                    rptBytes = rt.generateReport();
                    LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() DDDX.5 GeneratingReport() FINISH " + ( rptBytes == null ? "null" : rptBytes.length ) );
                    rt.dispose();
                }
                

                if( rptBytes==null || rptBytes.length == 0 )
                {    
                    if( te.getProduct().getProductType().getIsFindly() )
                    {
                        LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() Findly Report for test event is empty. " + te.getProduct().toString() + ", " + r.toString() + ", " + te.toString() );
                        throw new Exception( "Generated Findly report is empty." );
                    }
                    
                    throw new Exception( "Generated report is empty." );
                }
                
                // Generate an English Equivalent Report?
                if( rptLocale!=null && !rptLocale.getLanguage().equals( "en" ) && engEquivSimId > 0 )
                {                    
                    // Org value
                    int includeEng = tk.getOrg().getIncludeEnglishReport();

                    // Test Key value
                    if( tk.getIncludeEnglishReportValue()==1 )
                        includeEng = 1;

                    else if( tk.getIncludeEnglishReportValue()==2 )
                        includeEng = 0;

                    // method param value
                    if( includeEnglishReport == 1  )
                        includeEng = 1;
                    
                    if( includeEnglishReport ==2  )
                        includeEng = 0;

                    // Already done.
                    if( includeEnglishReport < 0 )
                        includeEng = 0;
                    
                    // Indicates we should generate.
                    if( includeEng==1 )
                    {
                        Object[] ot = generateLanguageEquivReport(te, tk, reportId, Locale.US, te.getProduct().getLongParam4(), r.getLongParam1(), 0, true, forceCalcSection );

                        //TestEventScore eeTes = (TestEventScore) ot[2];
                        //if( eeTes == null )
                        //{
                        //    
                        //}
                    }
                }
            }

                        // List<TestEventScore> tesl = te.getTestEventScoreListForReportId(reportId);
            tes = te.getTestEventScoreForReportId( r.getReportId(), rptLocale.toString() );

            if( tes==null )
            {
                tes = new TestEventScore();
                tes.setDisplayOrder( te.getTestEventScoreList().size() + 1 );
            }
            
            tes.setTestEventId( te.getTestEventId() );
            tes.setTestEventScoreTypeId( TestEventScoreType.REPORT.getTestEventScoreTypeId() );
            tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
            tes.setReportId( r.getReportId() );
            tes.setStrParam1( rptLocale.toString() );

            tes.setReportBytes( rptBytes );

            if( createPdfDoc )
            {
                tes.setReportFileContentTypeId( 500 ); // PDF
                tes.setReportFilename(getReportFilename( te ) + ".PDF" );
            }

            else
            {
                tes.setReportFileContentTypeId( 0 );
                tes.setReportFilename( "NoReport" );
            }

            // tes.setName( rd.getReportName() );
            tes.setName( rd.getReportName() );
            // tes.setName( te.getReport().getNameForReportDocument( te.getProduct().getName() ) );

            tes.setCreateDate( new Date() );
            
            eventFacade.saveTestEventScore(tes);

            LogService.logIt( "ReportManager.generateReportForTestEventAndLanguage() completed report " + tes.getReportFilename() + ", " + ( tes.getReportBytes() == null ? "No Document in Report." :  tes.getReportBytes().length + " bytes.  testEventScoreId=" + tes.getTestEventScoreId() ) );

            Tracker.addResponseTime( "Generate Single Report", new Date().getTime() - procStart.getTime() );

            if( forceSendCandidateReports  && r.getEmailTestTaker()==1 )
            {
                if( distManager==null )
                    distManager = new DistManager();      

                if( tk.getBatteryProduct()==null )
                    tk.setBatteryProduct(eventFacade.getProduct(tk.getProductId() ));

                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true));
                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true));
                
                for( TestEventScore tesr : te.getTestEventScoreList(TestEventScoreType.REPORT.getTestEventScoreTypeId()))
                {
                    if( tesr.getReportId()>0 && tesr.getReport()==null )
                        tesr.setReport(eventFacade.getReport(tesr.getReportId()));
                }
                te.setReport(r);
                distManager.emailReportsToTestTaker(tk, te, true, r.getReportId(), te.getTestEventId(), maxLastCandidateSendDate );                        
                LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() Sending report to TestTaker. reportId=" + reportId + " forceSendCandidateReports=" + forceSendCandidateReports );
                
            }
            
            return tes;
        }

        catch( ReportException e )
        {
            LogService.logIt("ReportManager.generateReportForTestEventAndLanguage() " + e.toString() + ", testEventId=" + testEventId + ", reportId=" + reportId +", langStr=" + langStr  );
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.generateReportForTestEventAndLanguage() testEventId=" + testEventId + ", reportId=" + reportId +", langStr=" + langStr  );
            return null;
        }

    }

    private byte[] getSampleReportBytes( String url ) throws Exception
    {
        if( url!=null && !url.isBlank() )
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = null;
            try
            {
                URL u = com.tm2score.util.HttpUtils.getURLFromString(url);

                is = u.openStream ();
                byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
                int n;

                while ( (n = is.read(byteChunk)) > 0 ) {
                  baos.write(byteChunk, 0, n);
                }
                return baos.toByteArray();
            }                
            catch( Exception e )
            {
                LogService.logIt( e, "ReportManager.getSampleReportBytes() Problem Downloading Static Sample Report, url=" + url );
            }
            finally 
            {
                if (is != null) 
                { 
                    is.close(); 
                }
            }
        }
        return null;        
    }
    

    public TestEventScore generateSampleReport( int productId, long reportId, boolean withProfile, boolean useReport2, boolean useReport3, Locale locale) throws Exception
    {
        try
        {
            if( locale == null )
                locale = Locale.US;
            
            // LogService.logIt("ReportManager.generateSampleReport() creating sample report.  productId=" + productId + ", reportId=" + reportId + ", locale=" + locale.toString()   );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            Product product = eventFacade.getProduct(productId);

            if( product==null )
            {
                LogService.logIt("ERROR: ProductId is invalid (no product found in dbms). productId=" + productId );
                return null;
                // throw new Exception( "ProductId is invalid" );
            }

            if( product.getProductTypeId()!=ProductType.SIM.getProductTypeId() && product.getProductTypeId()!=ProductType.CT5DIRECTTEST.getProductTypeId() ) //  && product.getProductTypeId()!= ProductType.IVR.getProductTypeId() )
            {
                LogService.logIt("ERROR: ReportManager.generateSampleReport() ERROR Product is wrong type. Expecting 40 (Sim) or 46 (CT5 Direct) " + product.toString() );
                return null;
                // throw new Exception( "Product is wrong type. Expecting 40 (Sim) " + product.toString() );
            }

            TestEventScore otes;

            boolean includeProctoring = (product.getProductType().getIsSimOrCt5Direct() ) && !product.getConsumerProductType().equals( ConsumerProductType.ASSESSMENT_VIDEOINTERVIEW) && !product.getConsumerProductType().equals( ConsumerProductType.ASSESSMENT_VIDEOINTERVIEW_LIVE);
            // boolean proctoringTesAdded = false;
            
            long simId = product.getLongParam1();

            if( simId <=0 )
                throw new Exception( "Product.simId invalid: " + simId );

            SimDescriptor sd = eventFacade.getSimDescriptor(simId, 0, true );

            if( sd == null )
            {
                LogService.logIt( "ReportManager.generateSampleReport() ERROR No sim descriptor found for simId=" + simId + ", Returning null, " + product.toString() );
                return null;
                // throw new Exception( "No sim descriptor found for simId=" + simId + ", " + product.toString() );
            }

            // Get report from Product record.
            if( useReport2 && product.getLongParam3()>0 )
                reportId = product.getLongParam3();

            // Get report from Product record.
            else if( useReport3 && product.getLongParam5()>0 )
                reportId = product.getLongParam5();
            
            // report 1
            if( reportId<=0 )
                reportId = product.getLongParam2();

            if( reportId<= 0 )
            {
                LogService.logIt( "ReportManager.generateSampleReport() ERROR No reportId provided or found in product record. simId=" + simId + ", Returning null, " + product.toString() );
                return null;
                // throw new Exception( "Invalid reportId: " + reportId + ", " + product.toString() );
            }
            
            String onetSoc = sd.getOnetSoc();
            
            // LogService.logIt("ReportManager.generateReport() ReportId = " + reportId  );

            TestKey tk = new TestKey();

            tk.setOrgId( RuntimeConstants.getIntValue( "samplereportorgid" ) );
            tk.setUserId( RuntimeConstants.getLongValue( "samplereportuserid" ) );
            tk.setAuthorizingUserId( RuntimeConstants.getLongValue( "samplereportauthuserid" ) );

            if( userFacade == null )
                userFacade = UserFacade.getInstance();

            // tk.Org
            Org org = userFacade.getOrg( tk.getOrgId() );

            Suborg suborg = tk.getSuborgId()>0 && tk.getSuborg()==null ? userFacade.getSuborg( tk.getSuborgId() ) : null;

            // tk.User
            User user = userFacade.getUser( tk.getUserId() );

            if( user!=null )
                user.setResume( userFacade.getResumeForUser( user.getUserId()));
            
            // tk.authUser
            User authUser = userFacade.getUser( tk.getAuthorizingUserId() );

            Report report = reportId>0 ? eventFacade.getReport(reportId) : null;
            
            tk.setProductId(productId);
            tk.setProductTypeId( product.getProductTypeId() );
            tk.setCorpId( RuntimeConstants.getIntValue( "defaultcorpid" ));
            tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORED.getTestKeyStatusTypeId() );
            tk.setUser(user);
            // tk.setUserId( user.getUserId() );
            tk.setLocaleStr( locale.toString() );
            tk.setLocaleStrReport( locale.toString() );
            tk.setAuthUser(authUser);
            tk.setAuthorizingUserId( authUser.getUserId() );
            tk.setOrg(org);
            tk.setSuborg(suborg);
            tk.setFirstAccessDate( new Date() );
            tk.setLastAccessDate( new Date() );
            tk.setStartDate( new Date() );


            TestEvent te = new TestEvent();
            te.setOrgId( tk.getOrgId() );
            te.setUserId( tk.getUserId() );
            te.setProduct( product );
            te.setProductId(productId);
            te.setOrg( org );
            te.setUser( user );
            te.setStartDate( new Date() );
            te.setLastAccessDate( new Date() );
            te.setIpAddress( "96.241.65.10" );
            te.setLocaleStrReport( locale.toString() );
            te.setLocaleStr(locale.toString() );

            te.setSimId( sd.getSimId() );
            te.setSimVersionId( sd.getSimVersionId() );
            te.setProductTypeId( product.getProductTypeId() );
            te.setReportId( reportId );
            te.setTotalTestTime( 1605 );
            te.setUserAgent( "Mozilla/5.0 (Windows NT 6.3; Trident/7.0; Touch; rv:11.0) like Gecko" );
            te.setExcludeFmNorms( 1 );


            SimJ simJ = JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() );

            te.setSimXmlObj( simJ );

            te.setEducTypeId( simJ.getEduc() );
            te.setExperTypeId( simJ.getExper() );
            te.setTrainTypeId( simJ.getTrn() );
            
            ReportRules reportRules = new ReportRules( org, null, te.getProduct(), report, null );
            
            //if( simJ.getUsesstdhrascoring()==1 )
            te.setStdHraScoring(SimJUtils.getHasAnyNormativeScoring( simJ ) && !reportRules.getReportRuleAsBoolean("bellgraphsoff") ? 1 : 0 );
                        
            List<TestEventScore> tesl = new ArrayList<>();

            List<SimJ.Simcompetency> scl = simJ.getSimcompetency();

            SimCompetencySortType scst = SimCompetencySortType.getValue( simJ.getSimcompetencyreportingsorttype() );
            
            Collections.sort( scl, new SimJSimCompetencyWeightComparator( scst ) );

            float rnd;

            float totalScore=0;
            // float totalRawScore=0;
            float totalWeight=0;

            String tp;
            String cvs;
            String categs;

            ScoreColorSchemeType scoreColorSchemeType = ScoreColorSchemeType.getValue( simJ.getScorecolorscheme() );

            CategoryDistType categoryDistType;
            ScorePresentationType scorePresentationType;

            boolean hasWriting = false;
            boolean hasAudio = false;
            // boolean hasChat = false;

            float grnLow;
            float grnTop;

            float scr;
            float rawScr;

            String cname;
            String cnameenglish;
            // String cnameuser;

            Profile pf=null;

            ProfileEntry pe;

            List<ProfileEntry> pel=null;

            float prolow,prohigh;
            float prolowrng = 15;
            float prohighrng = 10;

            boolean isJobSpec = product.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId();            
            boolean isProductTypeForImageProc = product.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId() || 
                                                product.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_SKILLS.getConsumerProductTypeId() || 
                                                product.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_COGNITIVE.getConsumerProductTypeId() || 
                                                product.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_PERSONALITY.getConsumerProductTypeId(); 
            
            boolean isValidAvReportId = BestJobsReportUtils.isValidAvReportId( reportId );
            boolean isValidCT2ReportId = BestJobsReportUtils.isValidCT2ReportId( reportId );
            boolean isValidSportsReportId = BestJobsReportUtils.isValidSportsReportId( reportId );
            boolean isValidUMinnReportId = BestJobsReportUtils.isValidUminnReportId( reportId );
            boolean isCareerScoutEmployerReport = isValidCT2ReportId && BestJobsReportUtils.isValidCareerScoutProductId( product.getProductId() );
            boolean isValidJobMatchReportId = BestJobsReportUtils.isValidJobMatchReportId( reportId );
            boolean includeJobMatchDataInsideCt2 = (isJobSpec && isValidCT2ReportId) || isCareerScoutEmployerReport;
            // boolean isVideoInterview = product.getConsumerProductTypeId()==ConsumerProductType.ASSESSMENT_VIDEOINTERVIEW.getConsumerProductTypeId();
            
            boolean isStdVoiceReportId = false; // IvrReportUtils.isStandardVoiceReport(reportId);
            
            boolean includeImageCapture =  isProductTypeForImageProc  || isValidAvReportId || isValidCT2ReportId || isCareerScoutEmployerReport;
            
            includeProctoring = includeProctoring && includeImageCapture;
            
            tk.setIntCustomParameterValue( "onlineproctortype" , 5 );

            //LogService.logIt( "ReportManager.sampleReport includeProctoring=" + includeProctoring + ", onlineProctoringTypeId=" + tk.getIntCustomParameterValue("onlineproctortype") );
            
            if( isValidUMinnReportId )
                tk.setCustom1("Internal Medicine Residency");
            
            
            if( !isValidCT2ReportId && !isStdVoiceReportId && !isValidAvReportId )
                withProfile = false;
            
            if( withProfile )
            {
                pf = new Profile();
                pel = new ArrayList<>();
                // Overall
                pf.setOverallLowVal(65);
                pf.setOverallHighVal(100);
            }
            
            ScoreFormatType sft = ScoreFormatType.getValue( simJ.getScoreformat() );

            OverallRawScoreCalcType overallRawScoreCalcType = OverallRawScoreCalcType.getValue( simJ.getOverallscorecalctype());
            OverallScaledScoreCalcType overallScoreCalcType = OverallScaledScoreCalcType.getValue( simJ.getOverallscaledscorecalctype() );

            boolean usesRawZScoresForOverallRaw = overallScoreCalcType.getIsTransform() && overallRawScoreCalcType.getUsesRawCompetencyScores();
            
            SimCompetencyClass scc;
            
            TestEventScore clonedTes;
            //TestEventScore extraInfoTes=null; 
            // List<TestEventScore> clonedOTesList = new ArrayList<>();

            boolean hasScoredAv = false;

            RemoteProctorEvent rpe = null;
            
            // For each competency
            for( SimJ.Simcompetency sjc : scl )
            {
                scc = SimCompetencyClass.getValue( sjc.getClassid() );

                if( scc.isScoredAvUpload()  )
                    hasScoredAv = true;
            }
            
            SimCompetencyRawScoreCalcType competencyRawScoreCalcType;
            
            boolean hasCorpCit = false;
            boolean hasIntegrity = false;
            boolean isAudioOnly = product.getIntParam6()==3;
            
            // For each competency
            for( SimJ.Simcompetency sjc : scl )
            {
                cname = UrlEncodingUtils.decodeKeepPlus( sjc.getName(), "UTF8" );
                cnameenglish = sjc.getNameenglish()==null ? null : UrlEncodingUtils.decodeKeepPlus( sjc.getNameenglish(), "UTF8" );
                // cnameuser = sjc.get

                if(cname != null && cname.contains("History Survey - Unproductive Behavior") )
                    continue;

                if(cnameenglish != null && cnameenglish.contains("History Survey - Unproductive Behavior") )
                    continue;

                // If it's a valid CT2 report and it's a Career Scout Employer report, do not include RIASEC competencies in the fake competency list.
                if( isCareerScoutEmployerReport && isValidCT2ReportId && sjc.getClassid()==SimCompetencyClass.SCOREDINTEREST.getSimCompetencyClassId() )
                {
                    // Skip this for career scout employer reports. Just use the fake approach below.
                    continue;
                }
                
                if( isValidSportsReportId && sjc.getClassid()==SimCompetencyClass.SCOREDIMAGEUPLOAD.getSimCompetencyClassId() )
                    continue;
                
                scc = SimCompetencyClass.getValue( sjc.getClassid() );

                // Skip scored essay if has video interview questions since they are probably backups if no video camera.
                if( scc.isScoredEssay() && hasScoredAv )
                    continue;
                
                if( scc.isScoredEssay()  )
                    hasWriting = true;
                
                //if( scc.isScoredChat() )
                //    hasChat = true;

                // scored audio ivr or  video interview
                if( scc.isScoredAudio() ||  (scc.isScoredAvUpload() && sjc.getSubclassid()<0) )
                    hasAudio = true;

                if( scc.isScoredImageUpload() )
                    continue;
                
                competencyRawScoreCalcType = SimCompetencyRawScoreCalcType.getValue( sjc.getRawscorecalctypeid() );
                
                categoryDistType = CategoryDistType.getValue( sjc.getCategorydisttype() );
                scorePresentationType = ScorePresentationType.getValue( sjc.getPresentationtype() );

                if( categoryDistType.equals( CategoryDistType.LINEAR ) )
                {
                    if( sjc.getHighcliffmin()>0 && sjc.getHighcliffmin() <100 )
                        grnTop = sjc.getHighcliffmin() + 0.85f*(100-sjc.getHighcliffmin());
                    else
                        grnTop = 100;

                    grnLow = sjc.getYellowmin() + 0.85f*(sjc.getYellowgreenmin() - sjc.getYellowmin()); //sjc.getYellowgreenmin();
                }

                //Normal
                else
                {
                    grnLow = sjc.getYellowmin()*0.85f;   //sjc.getYellowmin();
                    if( grnLow < 0 )
                        grnLow = sjc.getYellowmin();

                    grnTop = sjc.getYellowgreenmin()*1.15f;

                }

                if( grnTop > 99 )
                    grnTop = 99;

                if( !sft.equals( ScoreFormatType.NUMERIC_0_TO_100 ) )
                {
                    scr = sft.getRandomScore();
                }

                else if( ( cname != null && cname.equalsIgnoreCase( "Corporate Citizenship" ) ) ||  ( cnameenglish != null && cnameenglish.equalsIgnoreCase( "Corporate Citizenship" )) )
                {
                    hasCorpCit = true;
                    scr = 10;
                }
                else if( ( cname != null && cname.equalsIgnoreCase( "Integrity" ) ) ||  ( cnameenglish != null && cnameenglish.equalsIgnoreCase( "Integrity" )) )
                {
                    hasIntegrity = true;
                    scr = 10;
                }

                else
                {
                    rnd = (float) Math.random();
                    scr = grnLow + rnd*( grnTop - grnLow );
                    if( scr >=100 )
                        scr = 100;
                }

                int scoreCategoryId = SampleReportUtils.getScoreCategoryTypeId( sjc, scr, scoreColorSchemeType );
                                
                clonedTes = null;
                //extraInfoTes = null;
                
                //if( scc.isScoredImageUpload() )
                //{
                    // clonedTes= ((TestEventScore) (eventFacade.getTestEventScore( RuntimeConstants.getLongValue( "SampleReportImgCaptureTestEventScoreId") )).clone());
                    //clonedTes.setTestEventId(0);
                    //clonedTes.setTestEventScoreId( 0 );
                    //scr = clonedTes.getScore();
                    //scoreCategoryId = SampleReportUtils.getScoreCategoryTypeId( sjc, scr, scoreColorSchemeType );
                    //proctoringTesAdded=true;                    
                //}
                
                // this should mean it's a video interview
                if( scc.isScoredAvUpload() && sjc.getSubclassid()<0 )
                {                    
                    clonedTes = ((TestEventScore) (eventFacade.getTestEventScore( RuntimeConstants.getLongValue( isAudioOnly ? "SampleReportAudioOnlyCaptureTestEventScoreId" : "SampleReportVideoCaptureTestEventScoreId") )).clone());
                    clonedTes.setTestEventId(0);
                    clonedTes.setTestEventScoreId( 0 );
                    scr = clonedTes.getScore();
                    scoreCategoryId = SampleReportUtils.getScoreCategoryTypeId( sjc, scr, scoreColorSchemeType );
                }
                
                // this should mean it's NOT a video interview - instead it's a scored audio test 
                //if( scc.isScoredAvUpload() && sjc.getSubclassid()>=0 ) //  && !isVideoInterview )
                //{                    
                //    extraInfoTes = ((TestEventScore) (eventFacade.getTestEventScore( RuntimeConstants.getLongValue( isAudioOnly ? "SampleReportAudioOnlySpokenTestEventScoreId" : "SampleReportVideoSpokenTestEventScoreId") )).clone());                    
                //}
                
                if( scc.isScoredChat() )
                {
                    clonedTes = ((TestEventScore) (eventFacade.getTestEventScore( RuntimeConstants.getLongValue( "SampleReportChatTestEventScoreId") )).clone());
                    
                    if( clonedTes !=null )
                    {
                        clonedTes.setTestEventId(0);
                        clonedTes.setTestEventScoreId( 0 );
                        scr = clonedTes.getScore();
                        scoreCategoryId = SampleReportUtils.getScoreCategoryTypeId( sjc, scr, scoreColorSchemeType );
                    }
                }                
                
                if( withProfile  && pel!=null)
                {
                    pe = new ProfileEntry();

                    prolow =  grnLow - 0.5f*prolowrng + ((float)Math.random())*prolowrng;  // grnLow*(0.85f + ((float)Math.random()*0.15f) );

                    prohigh = grnTop + - 0.5f*prohighrng + ((float)Math.random())*prohighrng; //  ((100-grnTop)*((float)Math.random()*0.15f) );

                    if( prolow<0 )
                        prolow=0;

                    if( prohigh>97 )
                        prohigh = 100;

                    pe.setLowVal(prolow);
                    pe.setHighVal(prohigh);

                    pe.setName( cname );
                    pel.add(pe);
                }

                rawScr = scr;
                
                // If we have a task-type of competency, and not supposed to include tasks in the overall score, don't include it.
                if(  scc.getIsTask() ) // && simJ.getIncludetasksoverall() != 1 )
                {}

                // If we have an Identity Image Capture competency. Don't include in overall score. 
                else if( scc.equals( SimCompetencyClass.SCOREDIMAGEUPLOAD ) )
                {}
                        
                // if have non-task type of competency and not supposed to include in overall, skip it.
                else if(  ( scc.getIsDirectCompetency() || scc.getIsAggregate() || scc.getIsCombo() ) && simJ.getIncludecompetenciesoverall() != 1 )
                {}

                else if(  scc.getIsInterest() && simJ.getIncludeinterestoverall() != 1 )
                {}

                else if(  scc.getIsExperience() && simJ.getIncludeexperienceoverall() != 1 )
                {}

                else if(  scc.getIsBiodata() && simJ.getIncludebiodataoverall() != 1 )
                {}
                                
                else
                {
                    if( usesRawZScoresForOverallRaw  )
                    {

                        if( competencyRawScoreCalcType.getIsZScore() && sjc.getStddeviation()>0 )
                        {
                            // Go backwards.
                            rawScr = ScoreUtils.getEquivalentZScore( scr, sjc.getScaledmean(), sjc.getScaledstddeviation());

                            //LogService.logIt( "ReportManager.sampleReport comp=" + sjc.getName() + ", scr=" + scr + ", sc.stddev=" + sjc.getStddeviation()  +" raw Z score=" + rawScr + " weight=" + sjc.getWeight() );

                            if( CategoryDistType.getValue( sjc.getCategorydisttype() ).getIsNormal() || sjc.getUsecategforoverall()==1  )
                            {
                                float adjRawScr = -1*Math.abs(rawScr);
                                //LogService.logIt( "ReportManager.sampleReport raw Z score=" + rawScr + ", adJRawScr=" + adjRawScr );

                                totalScore += adjRawScr*sjc.getWeight();
                            }

                            else
                                totalScore += rawScr*sjc.getWeight();

                            totalWeight += sjc.getWeight();
                        }

                        // skip - do not count. 
                        else
                        {
                            //LogService.logIt( "ReportManager.sampleReport Skipping in overall because no stats present or not a Z type. comp=" + sjc.getName() );
                        }
                    }

                    else
                    {
                        if( CategoryDistType.getValue( sjc.getCategorydisttype() ).getIsNormal() || sjc.getUsecategforoverall()==1)
                        {
                            //LogService.logIt( "ReportManager.sampleReport NON-Z comp=" + sjc.getName() + ", scr=" + scr + ", weight=" + sjc.getWeight() );
                            totalScore += ScoreCategoryType.getValue( scoreCategoryId ).getNumericEquivScore( simJ.getScoreformat() )*sjc.getWeight();
                        }

                        else
                            totalScore += scr*sjc.getWeight();

                        totalWeight += sjc.getWeight();
                    }
                }                
                
                if( clonedTes == null )
                {
                    otes =  new TestEventScore();
                    otes.setCreateDate( new Date() );
                    otes.setDisplayOrder( tesl.size() + 2 );
                    otes.setTestEventScoreTypeId( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );
                    otes.setName( UrlEncodingUtils.decodeKeepPlus( sjc.getName(), "UTF8" ) );
                    otes.setNameEnglish( cnameenglish );
                    otes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
                    otes.setScore( scr );
                    otes.setRawScore( rawScr );
                    otes.setScoreText( SampleReportUtils.getScoreText( sjc, otes.getScore(), scoreColorSchemeType ) );
                    otes.setScoreFormatTypeId( simJ.getScoreformat() );
                    
                    scc = SimCompetencyClass.getValue( sjc.getClassid() );
                    
                    if( scc.getSupportsSubclass() && sjc.getSubclassid()>=0 )
                    {
                        otes.setIntParam1( scc.getSimCompetencyClassId() );
                        otes.setSimCompetencyClassId( sjc.getSubclassid() );
                    }
                    else
                        otes.setSimCompetencyClassId( sjc.getClassid() );
                    otes.setSimCompetencyId( sjc.getId() );
                    otes.setWeight( sjc.getWeight() );
                    otes.setHide( sjc.getHide() );

                    otes.setScoreCategoryId( SampleReportUtils.getScoreCategoryTypeId( sjc, otes.getScore(), scoreColorSchemeType ) );

                    otes.setInterviewQuestions( SampleReportUtils.packInterviewQuestions( sjc ) );
                    

                    tp = "";
                    cvs = "";

                    if( sjc.getDescrip() != null )
                        tp += "[" + Constants.DESCRIPTIONKEY + "]" + UrlEncodingUtils.decodeKeepPlus( sjc.getDescrip(), "UTF8" );

                    if( scorePresentationType.getIsSpectrum() )
                    {
                        otes.setReportFileContentTypeId( scorePresentationType.getScorePresentationTypeId() );                        
                        tp += "[" + Constants.COMPETENCYSPECTRUMKEY + "]" + (sjc.getLowendname()==null ? "" : sjc.getLowendname()) + Constants.DELIMITER + (sjc.getHighendname()==null ? "" : sjc.getHighendname() );
                    }
                    
                    
                    if( !isStdVoiceReportId  )
                    {
                        for( String cv : SampleReportUtils.getStandardCaveatList( locale, sjc.getName(), sjc.getClassid() )  )
                        {
                            if( cv.isEmpty() )
                                continue;

                            if( !cvs.isEmpty() )
                                cvs += Constants.DELIMITER;

                            cvs += cv; // URLDecoder.decode( cv, "UTF8" );
                        }
                    }

                    if( scc.isScoredImageUpload() )
                        cvs = RuntimeConstants.getStringValue( "SampleReportImgCaptureCaveats" );


                    if( !cvs.isEmpty() )
                        tp += "[" + Constants.CAVEATSKEY + "]" + cvs;

                    categs = scoreColorSchemeType.getScoreCategoryInfoString( sjc, simJ.getScoreformat() );

                    if( !categs.isEmpty() )
                        tp += "[" + Constants.CATEGORYINFOKEY + "]" + categs;

                    if( tp.isEmpty() )
                        tp = null;

                    otes.setTextParam1( tp );

                    otes.setPercentile( (int) (otes.getScore()*0.92f) );
                    otes.setOverallPercentileCount( 203 );
                    otes.setAccountPercentile( (int)(otes.getPercentile()*0.99) );
                    otes.setAccountPercentileCount( 36 );
                    otes.setCountryPercentile(  (int)(otes.getAccountPercentile()*0.99)  );
                    otes.setCountryPercentileCount( 121 );

                    //if( extraInfoTes!=null  )
                    //{
                    //    if( 1==1 && scc.isScoredAvUpload() && sjc.getSubclassid()>=0 )
                    //        otes.setTextParam1(extraInfoTes.getTextParam1() );
                    //}

                    
                }
                
                else
                {
                    otes = clonedTes;
                    
                }
                
                tesl.add( otes );
            }

            // if( includeProctoring && !proctoringTesAdded )
            if( includeProctoring && rpe==null )
            {
                rpe = (RemoteProctorEvent) ProctorFacade.getInstance().getRemoteProctorEventForTestEventId(RuntimeConstants.getLongValue( "SampleReportImgCaptureTestEventId") ).clone();
                
                rpe.setTestEventId(0);
                rpe.setOrgId(0);
                te.setRemoteProctorEvent(rpe);
                
                //LogService.logIt( "ReportManager.sampleReport RemoteProctorEventId=" + (rpe==null ? "null" : rpe.getRemoteProctorEventId()) );
                //clonedTes= ((TestEventScore) (eventFacade.getTestEventScore( RuntimeConstants.getLongValue( "SampleReportImgCaptureTestEventScoreId") )).clone());
                //clonedTes.setTestEventId(0);
                //clonedTes.setTestEventScoreId( 0 );
                //tesl.add( clonedTes );
                //scr = clonedTes.getScore();
                //scoreCategoryId =  SampleReportUtils.getScoreCategoryTypeId( sjc, scr, scoreColorSchemeType );
                // proctoringTesAdded=true;  
            }
            
            float overallRawScore = totalScore;
                    
            if( totalWeight>0 )
            {
                // converts the overall score to a weighted average. 
                overallRawScore /= totalWeight;
                totalScore /= totalWeight;                     
            }
            
            //LogService.logIt( "ReportManager.sampleReport Overall Raw Score=" + overallRawScore + ", totalWeight=" + totalWeight );

            if( !usesRawZScoresForOverallRaw )
            {
                if( overallRawScore> 98 )
                    overallRawScore = 98;

                if( overallRawScore<0 )
                    overallRawScore = 0;
            }
            
            if(     simJ.getStddeviation()>0 && 
                    ( overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) || overallScoreCalcType.equals( OverallScaledScoreCalcType.NCE ) ) && 
                    !usesRawZScoresForOverallRaw )
            {
                
                float overallMean = simJ.getMean();
                float overallSd = simJ.getStddeviation();
                  
                // Next, convert to z score
                float zScore = (overallRawScore - overallMean)/overallSd;
                
                float m2 = overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) ? simJ.getRawtoscaledfloatparam1() : 50f;
                float std2 = overallScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) ? simJ.getRawtoscaledfloatparam2() : 21.06f;
                
                totalScore = zScore*(std2) + m2;

            }

            if( overallScoreCalcType.getIsTransform() && 
                overallRawScoreCalcType.getUsesRawCompetencyScores() && 
                simJ.getRawtoscaledfloatparam2()>0 )
            {
                float normalizedRawScore = overallRawScore;
                
                if( usesRawZScoresForOverallRaw && overallRawScoreCalcType.getRawNormalized() && simJ.getRawtoscaledfloatparam4()>0 )
                    normalizedRawScore = (overallRawScore - simJ.getRawtoscaledfloatparam3())/simJ.getRawtoscaledfloatparam4();
                
                totalScore = normalizedRawScore*simJ.getRawtoscaledfloatparam2() + simJ.getRawtoscaledfloatparam1();
                
                //LogService.logIt( "ReportManager.sampleReport NORMALIZED Overall Raw Score=" + normalizedRawScore + ", Final scaled score=" + totalScore );
                
            }

            
            // ScoreFormatType sft = ScoreFormatType.getValue( simJ.getScoreformat() );
            
            if( totalScore > sft.getMaxScoreToGiveTestTaker() )
                totalScore = sft.getMaxScoreToGiveTestTaker();

            // This is for sample reports only to be sure we don't give anyone a 100 overall.
            if( totalScore > 0.99f*sft.getMaxScoreToGiveTestTaker() )
                totalScore = 0.99f*sft.getMaxScoreToGiveTestTaker();
            
            
            if( totalScore<sft.getMinScoreToGiveTestTaker() )
                totalScore = sft.getMinScoreToGiveTestTaker();

            te.setOverallScore(totalScore);

            otes = new TestEventScore();
            otes.setCreateDate( new Date() );
            otes.setDateParam1(new Date());
            otes.setDisplayOrder( 1 );
            otes.setTestEventScoreTypeId( TestEventScoreType.OVERALL.getTestEventScoreTypeId() );
            otes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
            otes.setScore( totalScore );
            otes.setRawScore( overallRawScore );

            if( otes.getScore() >= simJ.getGreenmin() )
                otes.setScoreCategoryId( ScoreCategoryType.GREEN.getScoreCategoryTypeId() );
            else if( otes.getScore() >= simJ.getYellowgreenmin() )
                otes.setScoreCategoryId( ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId() );
            else if( otes.getScore() >= simJ.getYellowmin() )
                otes.setScoreCategoryId( ScoreCategoryType.YELLOW.getScoreCategoryTypeId() );
            else if( otes.getScore() >= simJ.getRedyellowmin() )
                otes.setScoreCategoryId( ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId() );
            else
                otes.setScoreCategoryId( ScoreCategoryType.RED.getScoreCategoryTypeId() );

            te.setOverallRating( otes.getScoreCategoryId() );

            otes.setPercentile( (int)totalScore );
            otes.setOverallPercentileCount( 203 );
            
            tp = "";

            // save the report overview text.
            if( te.getSimXmlObj() != null &&  te.getSimXmlObj().getReportoverviewtext() != null )
            {
                tp +=  "[" + Constants.DESCRIPTIONKEY + "]" + UrlEncodingUtils.decodeKeepPlus( te.getSimXmlObj().getReportoverviewtext(), "UTF8" );
                // tes.setTextParam1( Constants.DESCRIPTIONKEY + URLDecoder.decode( te.getSimXmlObj().getReportoverviewtext(), "UTF8" ) );
            }

            String categInfo = scoreColorSchemeType.getScoreCategoryInfoStringForSim( te.getSimXmlObj() );

            if( !categInfo.isEmpty() )
                tp += "[" + Constants.CATEGORYINFOKEY + "]" + categInfo;
            
            otes.setTextParam1( tp );
            // otes.setTextParam1( URLDecoder.decode(  simJ.getReportoverviewtext()==null ? "" : simJ.getReportoverviewtext(), "UTF8" ) );

            te.setOverallPercentile((int)totalScore);
            te.setOverallPercentileCount( otes.getOverallPercentileCount() );

            totalScore = 0.92f * totalScore;

            otes.setAccountPercentile( (int) totalScore );
            otes.setAccountPercentileCount( 36 );

            te.setAccountPercentile( otes.getAccountPercentile() );
            te.setAccountPercentileCount( otes.getAccountPercentileCount() );

            totalScore = 0.9f * totalScore;

            otes.setCountryPercentile( (int) totalScore );
            otes.setCountryPercentileCount( 121 );

            te.setCountryPercentile( otes.getCountryPercentile());
            te.setCountryPercentileCount( otes.getCountryPercentileCount() );
            te.setIpCountry( "US" );
            te.setIpState( "Virginia" );
            te.setIpCity( "Aldie" );

            tesl.add( otes );

            te.setScoreFormatTypeId( simJ.getScoreformat() );
            te.setOverallRating( otes.getScoreCategoryId() ); // ScoreCategoryType.getForScore( scoreColorSchemeType, te.getOverallScore() , 0, simJ.getGreenmin(), simJ.getYellowgreenmin(), simJ.getYellowmin(), simJ.getRedyellowmin() , simJ.getScoreformat(), 0, 0 ).getScoreCategoryTypeId() );
            otes.setScoreText( BaseTestEventScorer.getScoreTextForOverallScore( scoreColorSchemeType, te.getOverallScore(), te ) );
            // otes.setScoreCategoryId( te.getOverallRating() );
            otes.setScoreFormatTypeId( simJ.getScoreformat() );

            otes.setTextbasedResponses(SampleReportUtils.packGeneralNoncompetencyResponses(locale, hasWriting, hasAudio, hasCorpCit, hasIntegrity, isValidCT2ReportId && !isStdVoiceReportId ) );

            te.setTestEventScoreList( tesl );
            // Next, need to get the SimObject


            // LogService.logIt("ReportManager.generateSampleReport() isJobSpec=" + isJobSpec + ", canIncludeJobMatch=" + isValidCT2ReportId + ", includeJobMatchDataInsideCt2=" + includeJobMatchDataInsideCt2 + ", isValidJobMatchReportId=" + isValidJobMatchReportId );
            
            if( includeJobMatchDataInsideCt2 )
            {
                String riasecData = RuntimeConstants.getStringValue( "samplereportriasecinfo" );
                
                // Change SOc names to match this test.
                if( onetSoc != null && !onetSoc.isEmpty() )
                {
                    OnetFacade onetFacade = OnetFacade.getInstance();
                    
                     List<Soc> sl = onetFacade.getRelatedSocList( onetSoc );
                     
                     // LogService.logIt( "ReportManager.generateSampleReport() related soclist for soc=" + onetSoc + " is " + sl.size() );
                     if( sl!=null && !sl.isEmpty() )
                     {
                         BestJobsReportUtils bjru = new BestJobsReportUtils(false);
                         String mrdata = bjru.modifyCompactDataForSampleReport( riasecData, sl );
                         
                         // LogService.logIt( "ReportManager.generateSampleReport() revised riasec data=" + mrdata );
                         
                         riasecData = mrdata;
                     }
                }
                
                otes = new TestEventScore();
                otes.setCreateDate( new Date() );
                otes.setDisplayOrder( tesl.size() + 2 );
                otes.setTestEventScoreTypeId( TestEventScoreType.REPORT.getTestEventScoreTypeId() );
                otes.setReportId( reportId );
                otes.setTextParam1( riasecData );
                otes.setStrParam1( locale.toString() );
                tesl.add( otes );
            }
            
            
            if( isValidJobMatchReportId )
                BestJobsReportUtils.setRiasecCompetencyScoresForSampleReport( tesl );
            
            if( isStdVoiceReportId || isValidAvReportId )
            {
                IvrReportUtils.setVoiceVibesScoresForSampleReport( locale, tesl );
            }
            
            
            if( !isValidJobMatchReportId && withProfile && pel!=null && pf!=null )
            {
                pf.setProfileEntryList(pel);

                te.setProfile(pf);
            }

            TestEventScore tes = null;
            
            if( !useReport2 &&  product.getStrParam13()!=null && !product.getStrParam13().isBlank() )
            {
                byte[] reportBytes = this.getSampleReportBytes( product.getStrParam13() );
                
                if( reportBytes!=null && reportBytes.length>0 )
                {
                    LogService.logIt("ReportManager.generateSampleReport() productId=" + productId + ", reportId=" + reportId + " using product.strParam13 as the sample report url=" + product.getStrParam13() );
                    tes = new TestEventScore();
                    tes.setReportBytes(reportBytes);
                    tes.setReportFilename("SampleReport.pdf" );
                    tes.setReportFileContentTypeId(FileContentType.DOCUMENT_PDF.getFileContentTypeId() );  
                    tes.setTestEventScoreId( product.getProductId() );
                    tes.setTestEventScoreTypeId( TestEventScoreType.REPORT.getTestEventScoreTypeId() );
                    tes.setTestEventId( product.getProductId() );
                }
                else
                    LogService.logIt("ReportManager.generateSampleReport() NONFATAL ERROR productId=" + productId + ", reportId=" + reportId + " product.strParam13=sample report url=" + product.getStrParam13() + " but no document found at URL." );
            }
            
            if( tes==null )
            {
                Object[] ot = generateSingleReport(te, tk, reportId, true, 0, false );

                Report rpt = (Report) ot[0];

                if( rpt == null )
                    throw new Exception( "No report returned." );

                tes = testEventScoreList == null ? null : testEventScoreList.get(0); // te.getTestEventScoreForReportId( rpt.getReportId() );                
            }
            

            if( tes == null )
                throw new Exception( "No TestEventScore for report found reportId=" + reportId + ", useReport2=" + useReport2 + ", TestEventScoreList.size()=" + te.getTestEventScoreList().size() + ", Report TestEventScores found: " + te.getTestEventScoreList( TestEventScoreType.REPORT.getTestEventScoreTypeId() ).size() );

            return tes;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.generateSampleReport() productId=" + productId + ", reportId=" + reportId  );

            return null;
        }
    }

    
    
    


    


    public List<TestEventScore> getTestEventScoreList() {
        return testEventScoreList;
    }

    public void setTestEventScoreList(List<TestEventScore> testEventScoreList) {
        this.testEventScoreList = testEventScoreList;
    }




}
