/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.SurveyEvent;
import com.tm2score.entity.event.TestEventLog;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.Country;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.NormFacade;
import com.tm2score.event.OnlineProctoringType;
import com.tm2score.event.TestEventLogUtils;
import com.tm2score.event.TestEventProductInParam4Comparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeyEventUtils;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.ErrorTxtObject;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.proctor.ProctorFacade;
import com.tm2score.proctor.ProctorHelpUtils;
import com.tm2score.proctor.ProctorRecordingType;
import com.tm2score.proctor.RemoteProctorEventStatusType;
import com.tm2score.profile.ProfileUsageType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.report.TestKeyReportThread;
import com.tm2score.score.scorer.TestEventScorer;
import com.tm2score.score.scorer.TestEventScorerFactory;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.user.OrgCreditUsageType;
import com.tm2score.user.RoleType;
import com.tm2score.user.UserCompanyStatusType;
import com.tm2score.user.UserFacade;
import com.tm2score.user.UserType;
import com.tm2score.util.IpUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.JaxbUtils;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author miker_000
 */
public class BaseScoreManager
{

    protected static final int MAX_SCORE_ERRORS = 5;
    
    protected static final Boolean PARTIAL_BATTERIES_MARKED_COMPLETE_OK = true;
    protected static final int MIN_SCORING_SECS = 180;
    protected static final int MIN_MINUTES_BETWEEN_CLEANINGS = 30;
    protected static final int MIN_PREMIUMVIDEORECORD_WAIT_MINS = 40;
    protected static Date lastCleaning = null;
    public static final int MAX_THREAD_TESTKEY_SCORE_COUNT = 25; // 5-5-2024 Was 100
    protected static final int MAX_MINUTES_TK_SCORING_IS_ACTIVE = 5;
    
    protected static Map<Long,Date> testKeyIdScoreDateMap;
    protected static Map<Long,Date> partialBatteryTestEventIdScoreDateMap;

    
    // @Inject
    protected EventFacade eventFacade;

    protected UserFacade userFacade = null;

    protected NormFacade normFacade = null;
    
    protected ProctorFacade proctorFacade = null;
    
    
    public static synchronized void initAutoScoreMap()
    {
        if( testKeyIdScoreDateMap!=null )
            return;
        testKeyIdScoreDateMap = new ConcurrentHashMap<>();
        lastCleaning = new Date();
        partialBatteryTestEventIdScoreDateMap = new ConcurrentHashMap<>();
    }
    
    public static synchronized void cleanMap()
    {
        if( testKeyIdScoreDateMap==null )
            initAutoScoreMap();

        if( lastCleaning==null )
            lastCleaning=new Date();
        
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MINUTE, -1*MIN_MINUTES_BETWEEN_CLEANINGS );

        if( lastCleaning.after( cal.getTime() ) )
            return;
        
        lastCleaning = new Date();
        Date d;
        for( Long tkid : testKeyIdScoreDateMap.keySet() )
        {
            d = testKeyIdScoreDateMap.get( tkid );
            if( d!=null && d.before( cal.getTime()) )
                testKeyIdScoreDateMap.remove(tkid);
        }

        for( Long tkid : partialBatteryTestEventIdScoreDateMap.keySet() )
        {
            d = partialBatteryTestEventIdScoreDateMap.get( tkid );
            if( d!=null && d.before( cal.getTime()) )
                partialBatteryTestEventIdScoreDateMap.remove(tkid);
        }
        
    }

    public static int getTestKeysAndPartialEventsInScoringCount()
    {
        return getTestKeysInScoringCount() + getPartialTestBatteriesInScoringCount();
    }

    
    public static int getTestKeysInScoringCount()
    {
        if( testKeyIdScoreDateMap==null )
            return 0;
        
        // Look back X minutes.
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -1*MAX_MINUTES_TK_SCORING_IS_ACTIVE );
        int ct = 0;
        Date d;
        for( Long tkid : testKeyIdScoreDateMap.keySet() )
        {
            d = testKeyIdScoreDateMap.get( tkid );
            
            // last date is newer than cutoff. Count as active.
            if( d!=null && d.after( cal.getTime()) )
                ct++;
        }
        return ct;
    }

    public static int getPartialTestBatteriesInScoringCount()
    {
        if( partialBatteryTestEventIdScoreDateMap==null )
            return 0;
        
        // Look back X minutes.
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -1*MAX_MINUTES_TK_SCORING_IS_ACTIVE );
        int ct = 0;
        Date d;
        for( Long tkid : partialBatteryTestEventIdScoreDateMap.keySet() )
        {
            d = partialBatteryTestEventIdScoreDateMap.get( tkid );
            
            // last date is newer than cutoff. Count as active.
            if( d!=null && d.after( cal.getTime()) )
                ct++;
        }
        return ct;
    }
    
    
    public static Date getLastTestKeyScoreDate( long testKeyId )
    {
        if( testKeyIdScoreDateMap==null )
            return null;
        return testKeyIdScoreDateMap.get( testKeyId );
    }

    public static void removeTestKeyFromDateMap( long testKeyId )
    {
        if( testKeyIdScoreDateMap!=null && testKeyIdScoreDateMap.containsKey(testKeyId) )
            testKeyIdScoreDateMap.remove(testKeyId);
    }

    public static void addTestKeyToDateMapIfNew(long testKeyId )
    {
        if( testKeyIdScoreDateMap==null )
            initAutoScoreMap();
        
        if( !testKeyIdScoreDateMap.containsKey(testKeyId) )
            testKeyIdScoreDateMap.put(testKeyId, new Date() );
    }
    
    public static void addTestKeyToDateMap( long testKeyId )
    {
        if( testKeyIdScoreDateMap==null )
            initAutoScoreMap();
        
        testKeyIdScoreDateMap.put(testKeyId, new Date() );
    }

    public static void removeTestEventFromPartialDateMap( long testEventId )
    {
        if( partialBatteryTestEventIdScoreDateMap!=null && partialBatteryTestEventIdScoreDateMap.containsKey(testEventId) )
            partialBatteryTestEventIdScoreDateMap.remove(testEventId);
    }

    public static void addTestEventToPartialDateMapIfNew(long testEventId )
    {
        if( partialBatteryTestEventIdScoreDateMap==null )
            initAutoScoreMap();
        
        if( !partialBatteryTestEventIdScoreDateMap.containsKey(testEventId) )
            partialBatteryTestEventIdScoreDateMap.put(testEventId, new Date() );
    }
    
    public static void addTestEventToPartialDateMap( long testEventId )
    {
        if( partialBatteryTestEventIdScoreDateMap==null )
            initAutoScoreMap();
        
        partialBatteryTestEventIdScoreDateMap.put(testEventId, new Date() );
    }
    
    public static boolean isScoringFirstTimeOrRepeatAllowed( long testKeyId )
    {
        if( testKeyIdScoreDateMap==null )
        {
            initAutoScoreMap();
            return true;
        }
        return isScoringFirstTimeOrRepeatAllowed( testKeyId, testKeyIdScoreDateMap );
    }

    public static boolean isPartialBatteryTestEventScoringFirstTimeOrRepeatAllowed( long testEventId )
    {
        if( partialBatteryTestEventIdScoreDateMap==null )
        {
            initAutoScoreMap();
            return true;
        }
        return isScoringFirstTimeOrRepeatAllowed( testEventId, partialBatteryTestEventIdScoreDateMap );
    }
    
    
    public static boolean isScoringFirstTimeOrRepeatAllowed( long testKeyOrEventId, Map<Long,Date> mapToUse )
    {

            // is repeat?
            Date lastDate = mapToUse.get( testKeyOrEventId );
            if( lastDate==null )
                return true;
            
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.SECOND, -1*MIN_SCORING_SECS );
            cal.add(Calendar.MINUTE, -1*MIN_MINUTES_BETWEEN_CLEANINGS );
            if( lastCleaning==null || lastCleaning.before( cal.getTime() ) )
                cleanMap();
            
            
            // enough time since last
            cal = new GregorianCalendar();
            cal.add( Calendar.SECOND, -1*MIN_SCORING_SECS );
            return lastDate.before( cal.getTime() );
    }
    
    
    /**
     *
     *
     * int[0] = testkeys scored.
     * int[1] = testevents scored.
     * int[2] = testkeys placed in pending status
     * int[3] = testevents placed in pending status
     * int[4] = testevents skipped because they are incompleted (only if partiallyCompleteBatteriesOk=true)
     * int[5] = returned to TestKey completed because hasUnscored.
     *
     * @param tk
     * @return
     * @throws Exception
     */
    public int[] scoreTestKey( TestKey tk, boolean partiallyCompleteBatteriesOk, boolean noThread) throws Exception
    {
        try
        {
            int[] out = new int[6];
            
            if( !isScoringFirstTimeOrRepeatAllowed( tk.getTestKeyId() ) )
            {
                LogService.logIt("BaseScoreManager.scoreTestKey() Last Scoring Call was within min last date window. IGNORING SCORING of Test Key. testKeyId=" + tk.getTestKeyId() );  
                return out;                                                    
            }            
                        
            // Add to the list.
            addTestKeyToDateMap( tk.getTestKeyId() );
            // testKeyIdScoreDateMap.put( tk.getTestKeyId(), new Date() );
            
            boolean scoreWasPending = false;
                        
            // LogService.logIt( "ScoreManager.scoreTestKey() AAA.1 scoring " + tk.getTestKeyId() );

            if( tk.getTestKeyStatusTypeId() < TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() )
            {
                if( eventFacade == null )
                    eventFacade = EventFacade.getInstance();

                boolean isTestKeyActuallyComplete = eventFacade.areAllTestEventsCompletedForTestKey( tk );
                
                //LogService.logIt("ScoreManager.scoreTestKey() testKey.id=" + tk.getTestKeyId() + " appears to not be completed. Check says: " + isTestKeyActuallyComplete + ", partiallyCompleteBatteriesOk=" + partiallyCompleteBatteriesOk );
                
                if( isTestKeyActuallyComplete || partiallyCompleteBatteriesOk )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() );
                    checkTestKeyLastAccessDate(tk);
                    eventFacade.saveTestKey(tk);
                }
                else
                    LogService.logIt("ScoreManager.scoreTestKey() testKey.id=" + tk.getTestKeyId() + " is not completed. Cannot score it." );
                    
            }            
            
            // non permanent error - throw STException
            if( tk.getTestKeyStatusTypeId() < TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() )
                throw new ScoringException( "TestKey is not completed.", ScoringException.NON_PERMANENT, tk );

            if(  tk.getTestKeyStatusTypeId() > TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId() )
                throw new ScoringException( "TestKeyStatusStype higher than ScoringStarted.", ScoringException.NON_PERMANENT, tk );
            
            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();
                       
            //if( tk.getTestKeyStatusTypeId() >= TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId() )
            //    return out;
            
            // refresh just in case a parallel process got it while waiting.
            tk = eventFacade.getTestKey(tk.getTestKeyId(), true );

            // Check Again from DBMS. non permanent error - throw STException
            if( tk.getTestKeyStatusTypeId() < TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() )
                throw new ScoringException( "XX2 TestKey is not completed.", ScoringException.NON_PERMANENT, tk );

            if(  tk.getTestKeyStatusTypeId() > TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId() )
                throw new ScoringException( "XX2 TestKeyStatusStype higher than ScoringStarted.", ScoringException.NON_PERMANENT, tk );
            
            tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId() );

            eventFacade.saveTestKey( tk );

            if( tk.getTestEventList() == null )
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ));

            TestEvent teo;
            
            if( tk.getOrgId()>0 && tk.getOrg()== null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                tk.setOrg( userFacade.getOrg( tk.getOrgId() ));
            }
            
            if( !tk.getOrgCreditUsageCounted() && OrgCreditUsageType.getValue( tk.getOrg().getOrgCreditUsageTypeId() ).getAnyResultCredit() )
            {
                LogService.logIt( "ScoreManager.scoreTestKey(id=" + tk.getTestKeyId() + ") TestKey has no credit charged but account uses Candidate Credits. Charging now. " );
                TestKeyEventUtils tkeu = new TestKeyEventUtils();
                tkeu.updateOrgCreditUsageEventCount(tk.getOrg(), tk);                
            }

            if( tk.getSuborgId()>0 && tk.getSuborg()== null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));
            }

            // ensure correct order if Battery
            if( tk.getBatteryId() > 0 )
            {
                tk.setBattery( eventFacade.getBattery( tk.getBatteryId() ) );

                if( tk.getBattery() != null )
                {
                    List<Integer> pidl = tk.getBattery().getProductIdList();

                    //Clean unfinished test events that are no longer in the battery.
                    ListIterator<TestEvent> li = tk.getTestEventList().listIterator();
                    
                    while( li.hasNext() )
                    {
                        teo = li.next();

                        // Note that partiallyCompleteBatteriesOk is only true when the test key has been marked completed so it's not true when the scoring system 
                        // is called directly by API from test.
                        // (partiallyCompleteBatteriesOk or Not in battery anymore), and not complete.
                        if( ( partiallyCompleteBatteriesOk || !pidl.contains( teo.getProductId() ) ) && !teo.getTestEventStatusType().getIsCompleteOrHigher() )
                        {
                            li.remove();
                            teo.setTestKeyId( RuntimeConstants.getLongValue( "Orphan_RemovedFmBattery_TestKeyId" ) );
                            eventFacade.saveTestEvent(teo);
                        }
                    }

                    tk.setTestEventList( tk.getBattery().setTestEventsInOrder( tk.getTestEventList() ) );
                    
                    if( partiallyCompleteBatteriesOk && pidl.size()>tk.getTestEventList().size() )
                        out[4]=pidl.size() - tk.getTestEventList().size();
                }
            }

            int pendingTestEventCount;
            
            OnlineProctoringType onlineProctoringType = tk.getOnlineProctoringType();
                        
            // PASS 1 - to ensure all test events are complete and are not waiting for external proctor processing.
            for( TestEvent te : tk.getTestEventList() )
            {
                // LogService.logIt("ScoreManager.scoreTestKey(id=" + tk.getTestKeyId() + ") testEvent " + te.toString() );
                te.setOrg( tk.getOrg() );

                if( te.getSuborgId()>0 )
                    te.setSuborg( tk.getSuborg() );

                // if already scored, skip it.
                if( te.getTestEventStatusTypeId() >= TestEventStatusType.SCORED.getTestEventStatusTypeId() )
                    continue;

                // if not completed, fix
                if( te.getTestEventStatusTypeId() < TestEventStatusType.COMPLETED.getTestEventStatusTypeId() )
                {
                    // if actually complete, treat as complete.
                    if( te.getPercentComplete() >= 100f )
                    {
                        String msg = "BaseScoreManager.scoreTestKey() NONFATAL - testEvent percentComplete=" + te.getPercentComplete() + " but testEventStatusTypeId=" + te.getTestEventStatusTypeId() + ", " + te.getTestEventStatusType().getKey() + ", Treating as if it's actually complete. testEventId=" + te.getTestEventId();
                        LogService.logIt( msg);                        
                        TestEventLogUtils.createTestEventLogEntry( te.getTestEventId(), msg ); 
                        
                        te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED.getTestEventStatusTypeId() );
                    }

                    // else TestEvent is not complete, so revert the whole test key
                    else
                    {
                        // revert test key
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.STARTED.getTestKeyStatusTypeId() );
                        eventFacade.saveTestKey(tk);
                        
                        removeTestKeyFromDateMap( tk.getTestKeyId() );

                        // skip entire TestKey
                        return out;
                    }
                }
                                   
                if( ProctorHelpUtils.getUseExternalProctoring(onlineProctoringType) )
                {
                    // 3/17/2024, always reload
                    //if( 1==1 || te.getRemoteProctorEvent()==null )
                    //{
                    if( proctorFacade==null )
                        proctorFacade = ProctorFacade.getInstance();
                    te.setRemoteProctorEvent( proctorFacade.getRemoteProctorEventForTestEventId( te.getTestEventId() ));
                    //}                    
                    
                    if( te.getRemoteProctorEvent()==null )
                    {
                        String m = "BaseScoreManager.scoreTestKey() XX4 TestEvent.remoteProctoringEvent is null. onlineProctoringType=" + onlineProctoringType.getKey() + ",  testEventId=" + te.getTestEventId() + ", testKeyId=" + tk.getTestKeyId();
                        LogService.logIt( m );
                        TestEventLogUtils.createTestKeyLogEntry( te.getTestKeyId(), te.getTestEventId(), 0, m, null, null);
                        // throw new ScoringException( "XX4 TestEvent.remoteProctoringEvent is null. onlineProctoringType=" + onlineProctoringType.getKey() + ",  testEventId=" + te.getTestEventId(), ScoringException.NON_PERMANENT, tk );
                    }
                    
                    // indicates that this RemoteProctorEvent doesn't have it's audio/video or image processing complete yet.
                    else if( !te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getCompleteOrHigher() )
                    {
                        if( te.getTestEventStatusType().getIsCompleteOrHigher() )
                        {
                            if( !te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getEventCompleteOrHigher() )
                            {
                                LogService.logIt( "BaseScoreManager.scoreTestKey() RemoteProctorEvent is not in EventComplete or Higher status but test event is in complete or higher status. Correcting RemoteProctorEventStatus to EventComplete or AnalysisCompleted or MediaProcesssingCompleted as appropriate. testEventId=" + te.getTestEventId() + ", testKeyId=" + te.getTestKeyId() );
                                te.getRemoteProctorEvent().setRemoteProctorEventStatusTypeId( RemoteProctorEventStatusType.EVENT_COMPLETE.getRemoteProctorEventStatusTypeId() );
                                te.getRemoteProctorEvent().setEventCompleteDate( te.getLastAccessDate() );
                                te.getRemoteProctorEvent().setNote( "BaseScoreManager.scoreTestKey() RemoteProctorEvent is not in EventComplete or Higher status but test event is in complete or higher status. Correcting RemoteProctorEventStatus to EventComplete or AnalysisCompleted or MediaProcesssingCompleted as appropriate.");
                            }
                                                        
                            if( !onlineProctoringType.getProctorRecordingType().getRequiresOvConnection() && !onlineProctoringType.getProctorRecordingType().getRequiresDirectImagesOnly() )
                                te.getRemoteProctorEvent().setRemoteProctorEventStatusTypeId( RemoteProctorEventStatusType.ANALYSIS_COMPLETED.getRemoteProctorEventStatusTypeId() );

                            // if just direct images, set to media processing complete.
                            else if( onlineProctoringType.getProctorRecordingType().getRequiresDirectImagesOnly() )
                                te.getRemoteProctorEvent().setRemoteProctorEventStatusTypeId( RemoteProctorEventStatusType.MEDIA_PROCESSING_COMPLETE.getRemoteProctorEventStatusTypeId() );
                                                        
                            if( proctorFacade==null )
                                proctorFacade = ProctorFacade.getInstance();
                            proctorFacade.saveRemoteProctorEvent( te.getRemoteProctorEvent() );
                        }
                        
                        if( !te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getEventCompleteOrHigher() )
                            throw new Exception( "BaseScoreManager.scoreTestKey() RemoteProctorEvent is not in EventComplete or Higher status. Something is wrong. testEventId=" + te.getTestEventId() + ", rpe.remoteProctoringStatusTypeId=" + te.getRemoteProctorEvent().getRemoteProctorEventStatusTypeId() + " (" + te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getName() + ") testKeyId=" + te.getTestKeyId() );
                        
                        // Always wait for images to be processed if images only.
                        boolean pending = onlineProctoringType.getIsPremiumAnyImages() || onlineProctoringType.getIsAnyBasic();
                                                
                        if( onlineProctoringType.getRecordsVideo() ) //  || onlineProctoringType.getRecordsAudioOnly() )
                        {
                            Calendar cal = new GregorianCalendar();
                            cal.add( Calendar.MINUTE, -1*MIN_PREMIUMVIDEORECORD_WAIT_MINS  );
                            
                            if( te.getRemoteProctorEvent().getEventCompleteDate()!=null && te.getRemoteProctorEvent().getEventCompleteDate().after( cal.getTime() ) )
                                pending = true;
                        }
                        if( pending )
                        {
                            if( !tk.getTestKeyStatusType().getIsScorePending() )
                            {
                                // LogService.logIt( "BaseScoreManager.scoreTestKey() placing test key in pending due to proctoring media processing not done. testKeyId=" + tk.getTestKeyId() );
                                tk.setTestKeyStatusTypeId( TestKeyStatusType.COMPLETED_PENDING_EXTERNAL.getTestKeyStatusTypeId() );
                                out[2]=1;
                                eventFacade.saveTestKey(tk);
                            }
                            
                            removeTestKeyFromDateMap( tk.getTestKeyId() );
                            
                            // cannot continue.
                            return out;                            
                        }
                    }                    
                }
            }
            
            pendingTestEventCount = 0;
            
            boolean hasUnscored=false;
            
            // Second PASS - scoring.
            for( TestEvent te : tk.getTestEventList() )
            {
                // LogService.logIt("ScoreManager.scoreTestKey(id=" + tk.getTestKeyId() + ") testEvent " + te.toString() );

                // if already scored, skip it.
                if( te.getTestEventStatusTypeId() >= TestEventStatusType.SCORED.getTestEventStatusTypeId() )
                {
                    // LogService.logIt("ScoreManager.scoreTestKey(id=" + tk.getTestKeyId() + ") skipping already scored test event: " + te.toString() );
                    continue;
                }

                scoreWasPending = te.getTestEventStatusTypeId()==TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId();

                Tracker.addScoreStart();

                try
                {
                    scoreTestEvent(tk, te, null, false, false);

                    if( te.getTestEventStatusTypeId()==TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
                    {
                        //if( !scoreWasPending )
                        Tracker.addScorePending( te.getTestEventId() );
                        //else
                        //    Tracker.addScorePendingNew();

                        pendingTestEventCount++;
                        hasUnscored=true;
                        out[3]++;
                    }

                    else
                    {
                        if( scoreWasPending )
                            Tracker.removeScorePending( te.getTestEventId() );

                        Tracker.addScoreFinish();
                        out[1]++;
                    }
                }

                catch( ScoringException ee )
                {
                    // Not a battery event.
                    if( tk.getBattery()==null )
                    {
                        if( (ee.getErrorTxtObj()==null || ee.getErrorTxtObj() instanceof TestEvent) && ee.getSeverity()==ScoringException.PERMANENT )
                        {
                           tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORE_ERROR.getTestKeyStatusTypeId() );
                           tk.setErrorCnt( tk.getErrorCnt()+1 );
                           eventFacade.saveTestKey(tk);
                        }
                        // Oherwise leave testkey in ScoringStarted status.
                        
                        throw ee;
                    }
                    else
                    {
                        LogService.logIt( "ScoreManager.scoreTestKey() STERR RRR.1 Scoring " + te.toString() + ", " + ee.toString() + ", TestEvent is part of a battery so ignoring this error in scoring process." );
                        saveErrorInfo( ee );
                        hasUnscored=true;
                    }
                }
            }


            // do battery score
            if( !hasUnscored && pendingTestEventCount <= 0 && tk.getBattery() != null && tk.getBattery().getBatteryScoreType().needsBatteryScoreObject() )
            {
                tk.setBatteryProduct( eventFacade.getProduct( tk.getProductId() ) );

                BatteryScore bs =  eventFacade.getBatteryScoreForTestKey( tk.getTestKeyId() );

                bs = tk.getBattery().scoreTestBattery( tk.getTestEventList(), bs );

                if( bs != null )
                {
                    bs.setTestKeyId( tk.getTestKeyId() );
                    bs.setTestKeyId( tk.getTestKeyId() );
                    bs.setStartDate( tk.getFirstAccessDate());
                    bs.setLastAccessDate(tk.getLastAccessDate());
                    bs.setUserId(tk.getUserId());
                    bs.setBatteryId(tk.getBatteryId());
                    bs.setOrgId(tk.getOrgId());
                    bs.setSuborgId(tk.getSuborgId());

                    if( normFacade == null )
                        normFacade = NormFacade.getInstance();

                    float[] norm = tk.getBattery().getBatteryScoreType().needsScore() ? normFacade.getBatteryPercentile(tk.getBattery(), bs, tk.getOrgId(), null ) : new float[2];

                    // bs.setPercentile(norm[0]);
                    bs.setAccountPercentile(norm[0]);
                    bs.setAccountPercentileCount( (int) norm[1]);

                    User u = tk.getUser();

                    if( u == null )
                    {
                        if( userFacade==null )
                            userFacade = UserFacade.getInstance();

                        tk.setUser(userFacade.getUser( tk.getUserId() ) );
                        u = tk.getUser();
                    }

                    if( u!=null && !u.getUserType().getAnonymous() && u.getIpCountry()!=null && !u.getIpCountry().trim().isEmpty() )
                    {
                        norm = tk.getBattery().getBatteryScoreType().needsScore() ? normFacade.getBatteryPercentile(tk.getBattery(), bs, tk.getOrgId(), u.getIpCountry() ) : new float[2];

                        // bs.setPercentile(norm[0]);
                        bs.setCountryPercentile(norm[0]);
                        bs.setCountryPercentileCount( (int) norm[1]);
                    }
                    
                    eventFacade.saveBatteryScore( bs );
                    tk.setBatteryScore( bs );
                }
            }

            if( pendingTestEventCount>0 )
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.COMPLETED_PENDING_EXTERNAL.getTestKeyStatusTypeId() );
                out[2]++;
            }

            // everything is scored or higher.
            else if( !hasUnscored )
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORED.getTestKeyStatusTypeId() );
                tk.setErrorCnt(0);
                out[0]++;
            }
            
            else  //, no pending but is a battery and it has unscored
            {
                String msg;
                // out in error state 
                if( tk.getBatteryId()>0 && tk.getTestKeyStatusTypeId()==TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId() )
                {
                    msg = "BaseScoreManager.scoreTestKey() SSS.1 NONFATAL Battery TestKey.status is ScoringStarted but does have Unscored Test events. Must have been a scoring exception for a battery test event, delayed so can score the other test events in battery. battery error. Setting testKeyStatusType to Scoring Error. tkid=" + tk.getTestKeyId() + ", tk.batteryId=" + tk.getBatteryId();
                    TestEventLogUtils.createTestKeyLogEntry(tk.getTestKeyId(), 0, 0, msg, null, null);
                    LogService.logIt( msg );
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORE_ERROR.getTestKeyStatusTypeId() );
                    tk.setErrorCnt( tk.getErrorCnt()+1 );
                    eventFacade.saveTestKey(tk);
                }
                
                else
                {
                    msg = "BaseScoreManager.scoreTestKey() SSS.2 NONFATAL TestKey has no pending TestEvents but does have Unscored Test events. testKeyStatusType=" + tk.getTestKeyStatusType().getName() + ", tkid=" + tk.getTestKeyId() + ", tk.batteryId=" + tk.getBatteryId();
                    TestEventLogUtils.createTestKeyLogEntry(tk.getTestKeyId(), 0, 0, msg, null, null);
                    LogService.logIt( msg );
                }                    
            }            
            
            // if we are inside thread and status is now scored, keep going to generate reports.
            if( !noThread && tk.getTestKeyStatusType().equals( TestKeyStatusType.SCORED))
            {
                try
                {
                    if( eventFacade==null )
                        eventFacade = EventFacade.getInstance();
                                        
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_STARTED.getTestKeyStatusTypeId() );

                    eventFacade.saveTestKey( tk );

                    // no longer processing.
                    // removeTestKeyFromDateMap( tk.getTestKeyId() );  
                    
                    // reload Test Events to get the current status
                    tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );

                    for( TestEvent te : tk.getTestEventList() )
                    {
                        if( te.getProduct()==null )
                            te.setProduct( eventFacade.getProduct( te.getProductId() ));
                    }

                    // Sort to place the Riasec events at the end. So, other reports are generated first.
                    Collections.sort( tk.getTestEventList(), new TestEventProductInParam4Comparator() );
                                        
                    // LogService.logIt( "BaseScoreManager.scoreTestKey() Starting Report Thread tkid=" + tk.getTestKeyId() );                    
                    TestKeyReportThread tkrt = new TestKeyReportThread( tk, 0, false, true ); 
                    
                    // !noThread indicates this method is already called from a thread, so don't spin a new thread. Just use this one.
                    tkrt.run();
                    // new Thread(new TestKeyReportThread( tk, 0, false )).start(); 
                    return out;               
                }
                
                catch( Exception e )
                {
                    LogService.logIt( e, "BaseScoreManager.scoreTestKey() TTT.1 Exception Starting Report Thread tkid=" + tk.getTestKeyId() );
                    out[2]++;
                }
            }
            
            eventFacade.saveTestKey( tk );

            // no longer processing.
            removeTestKeyFromDateMap( tk.getTestKeyId() );  
            
            return out;
        }

       catch( ScoringException e )
       {
           LogService.logIt( "ScoreManager.scoreTestKey() STERR XXX.1 " + e.toString() + ", "  + tk.toString() );
           removeTestKeyFromDateMap( tk.getTestKeyId() );
           throw e;
       }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt(e, "ScoreManager.scoreTestKey() YYY.1 Exception "  + ( tk==null ? "testKey is null" : tk.toString() ) );           
           if( tk!=null )
               removeTestKeyFromDateMap( tk.getTestKeyId() );           
           throw new ScoringException( e.getMessage() + "ScoreManager.scoreTestKey() YYY.2 " , ScoreUtils.getExceptionPermanancy(e) , tk );
       }

    }
    
    public void checkTestKeyLastAccessDate( TestKey tk) throws Exception
    {
        if( tk.getTestEventList()==null )
        {
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            
            tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true) );
        }
        
        Date lad = tk.getLastAccessDate();
        
        for( TestEvent te : tk.getTestEventList() )
        {
            if( lad==null || ( te.getLastAccessDate()!=null && te.getLastAccessDate().after( lad)))
                lad = te.getLastAccessDate();
        }
        
        if( lad!=null && (tk.getLastAccessDate()==null || lad.after(tk.getLastAccessDate() )) )
            tk.setLastAccessDate(lad);
    }

    
    /*
      out[0] = 0 or 1 for scoring started.
      out[1] = 0 or 1 for scoring finished.
      out[2] = 0 or 1 for score pending proctoring.
      out[3] = 0 or 1 for score pending other external
      out[4] = 0 or 1 for 
    */
    public int[] scorePartiallyCompleteBatteryTestEvent( TestEvent te, boolean noThread) throws Exception
    {
        try
        {
            int[] out = new int[5];
            
            if( !isPartialBatteryTestEventScoringFirstTimeOrRepeatAllowed( te.getTestEventId() ) )
            {
                LogService.logIt("BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() Last Scoring Call was within min last date window. IGNORING SCORING of TestEvent. testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId() );  
                return out;                                                    
            }            
            
            addTestEventToPartialDateMap( te.getTestEventId() );
            
            // partialBatteryTestEventIdScoreDateMap.put( te.getTestEventId(), new Date() );
                                                
            // LogService.logIt( "BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() AAA.1 scoring testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId() );
            
            // non permanent error - throw STException
            if( te.getTestEventStatusTypeId() < TestEventStatusType.COMPLETED.getTestEventStatusTypeId() )
                throw new Exception( "TestEvent is not completed." );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();
                       
            
            // refresh just in case a parallel process got it while waiting.
            TestKey tk = eventFacade.getTestKey(te.getTestKeyId(), true );
            if( tk.getTestKeyStatusType().getIsCompleteOrHigher() )
            {
                LogService.logIt( "BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() AAA.2 TestKey is now in Complete or higher Status. Returning. testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId() );
                return out;
            }
            
            te = eventFacade.getTestEvent( te.getTestEventId(), true);
            if( !te.getTestEventStatusType().getIsCompleteOrPendingExternal())
            {
                LogService.logIt( "BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() AAA.3 TestEvent is not in Complete Status. Returning. testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId() );
                return out;
            }

            te.setTestKey(tk);
            te.setPartialBatteryTestEvent(true);
            //te.setTestEventStatusTypeId( TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() );
            //eventFacade.saveTestKey( tk );

            if( tk.getOrgId()>0 && tk.getOrg()== null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                tk.setOrg( userFacade.getOrg( tk.getOrgId() ));
            }

            if( tk.getSuborgId()>0 && tk.getSuborg()== null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));
            }
            
            OnlineProctoringType onlineProctoringType = tk.getOnlineProctoringType();
            
            // LogService.logIt("ScoreManager.scoreTestKey(id=" + tk.getTestKeyId() + ") testEvent " + te.toString() );
            te.setOrg( tk.getOrg() );

            if( te.getSuborgId()>0 )
                te.setSuborg( tk.getSuborg() );
                                   
            if( ProctorHelpUtils.getUseExternalProctoring(onlineProctoringType) )
            {
                if( te.getRemoteProctorEvent()==null )
                {
                    if( proctorFacade==null )
                        proctorFacade = ProctorFacade.getInstance();
                    te.setRemoteProctorEvent( proctorFacade.getRemoteProctorEventForTestEventId( te.getTestEventId() ));
                }                    

                // indicates that thie RemoteProctorEvent doesn't have it's audio/video or image processing complete yet.
                if( te.getRemoteProctorEvent()!=null && !te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getCompleteOrHigher() )
                {
                    // Not in event Complete status
                    if( te.getTestEventStatusType().getIsCompleteOrHigher() && !te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getEventCompleteOrHigher() )
                    {
                        LogService.logIt( "BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() RemoteProctorEvent is not in EventComplete or Higher status but test event is in complete or higher status. Correcting RemoteProctorEvent. testEventId=" + te.getTestEventId() + ", testKeyId=" + te.getTestKeyId() );
                        te.getRemoteProctorEvent().setRemoteProctorEventStatusTypeId( RemoteProctorEventStatusType.EVENT_COMPLETE.getRemoteProctorEventStatusTypeId() );
                        te.getRemoteProctorEvent().setEventCompleteDate( te.getLastAccessDate() );

                        if( !onlineProctoringType.getProctorRecordingType().getRequiresOvConnection() && !onlineProctoringType.getProctorRecordingType().getRequiresDirectImagesOnly() )
                            te.getRemoteProctorEvent().setRemoteProctorEventStatusTypeId( RemoteProctorEventStatusType.ANALYSIS_COMPLETED.getRemoteProctorEventStatusTypeId() );

                        // if just direct images, set to media processing complete.
                        else if( onlineProctoringType.getProctorRecordingType().getRequiresDirectImagesOnly() )
                            te.getRemoteProctorEvent().setRemoteProctorEventStatusTypeId( RemoteProctorEventStatusType.MEDIA_PROCESSING_COMPLETE.getRemoteProctorEventStatusTypeId() );

                        if( proctorFacade==null )
                            proctorFacade = ProctorFacade.getInstance();
                        proctorFacade.saveRemoteProctorEvent( te.getRemoteProctorEvent() );
                    }

                    if( !te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getEventCompleteOrHigher() )
                        throw new Exception( "BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() RemoteProctorEvent is not in EventComplete or Higher status. Something is wrong. testEventId=" + te.getTestEventId() + ", rpe.remoteProctoringStatusTypeId=" + te.getRemoteProctorEvent().getRemoteProctorEventStatusTypeId() + " (" + te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getName() + ") testKeyId=" + te.getTestKeyId() );

                    // Always wait for images to be processed if images only.
                    boolean pending = onlineProctoringType.getIsPremiumImagesOnly() || onlineProctoringType.getIsAnyBasic();
                    if( onlineProctoringType.getRecordsVideo() )
                    {
                        Calendar cal = new GregorianCalendar();
                        cal.add( Calendar.MINUTE, -1*MIN_PREMIUMVIDEORECORD_WAIT_MINS  );

                        if( te.getRemoteProctorEvent().getEventCompleteDate()!=null && te.getRemoteProctorEvent().getEventCompleteDate().after( cal.getTime() ) )
                            pending = true;
                    }
                    
                    if( pending )
                    {
                        // LogService.logIt( "BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() pending because proctoring media processing not done. testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId()  );
                        out[2]=1;
                        removeTestEventFromPartialDateMap(te.getTestEventId());
                        return out;                            
                    }
                }                    
            }
            
            Tracker.addScoreStart();
            out[0]=1;

            try
            {
                scoreTestEvent(tk, te, null, false, false);
            }
            catch( ScoringException ee )
            {
                LogService.logIt( ee, "ScoreManager.scorePartiallyCompleteBatteryTestEvent() ScoringException scoring " + te.toString() + ", part of battery so ignoring in scoring process." );
                saveErrorInfo( ee );
                removeTestEventFromPartialDateMap( te.getTestEventId());
                return out;
            }

            if( te.getTestEventStatusTypeId()==TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
            {
                // LogService.logIt( "ScoreManager.scorePartiallyCompleteBatteryTestEvent() Scoring is pending external. testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId() );
                Tracker.addScorePending( te.getTestEventId() );
                out[3]=1;
                removeTestEventFromPartialDateMap( te.getTestEventId());
                return out;
            }

            else if( te.getTestEventStatusTypeId()==TestEventStatusType.SCORED.getTestEventStatusTypeId() )
            {
                Tracker.addScoreFinish();
                out[1]=1;
            }
            
            else
            {
                LogService.logIt( "ScoreManager.scorePartiallyCompleteBatteryTestEvent()Expected status of SCORED. But current TestEventStatus is " + te.getTestEventStatusType().getKey() + " testKeyId=" + te.getTestKeyId() + ", testEventId=" + te.getTestEventId() );
                removeTestEventFromPartialDateMap( te.getTestEventId());
                return out;                
            }


            if( !noThread && out[1]==1 && te.getTestEventStatusTypeId()==TestEventStatusType.SCORED.getTestEventStatusTypeId() )
            {
                try
                {
                    if( eventFacade==null )
                        eventFacade = EventFacade.getInstance();
                                        
                    te.setTestEventStatusTypeId( TestEventStatusType.REPORT_STARTED.getTestEventStatusTypeId() );

                    eventFacade.saveTestEvent( te );

                    if( te.getProduct()==null )
                        te.setProduct( eventFacade.getProduct( te.getProductId() ));
                                        
                    // LogService.logIt( "BaseScoreManager.scoreTestKey() Starting Report Thread tkid=" + tk.getTestKeyId() );
                    //TestKeyReportThread tkrt = 
                    new Thread(new TestKeyReportThread( te, 0, false )).start(); 
                    //tkrt.run();
                    // new Thread(new TestKeyReportThread( tk, 0, false )).start(); 
                    return out;               
                }
                
                catch( Exception e )
                {
                    LogService.logIt( e, "BaseScoreManager.scorePartiallyCompleteBatteryTestEvent() Exception Starting Report Thread tkid=" + tk.getTestKeyId() + ", testEventId=" + te.getTestEventId() );
                    out[2]++;
                }
            }

            // All done. Remove from map.
            removeTestEventFromPartialDateMap( te.getTestEventId());
            
            // eventFacade.saveTestKey( tk );
            return out;
        }

        catch( ScoringException e )
        {
           LogService.logIt(e, "ScoreManager.scorePartiallyCompleteBatteryTestEvent() ScoringException "  + te.toString() );
           removeTestEventFromPartialDateMap( te.getTestEventId());
           throw e;
        }
        // unforseen exceptions are permanent. Disable this TestEvent until fixed.
        catch( Exception e )
        {
           LogService.logIt(e, "ScoreManager.scorePartiallyCompleteBatteryTestEvent() Exception "  + te.toString() );
           removeTestEventFromPartialDateMap( te.getTestEventId());
           throw new ScoringException( e.getMessage() + "ScoreManager.scorePartiallyCompleteBatteryTestEvent() " , ScoreUtils.getExceptionPermanancy(e) , te );
        }

    }


    
    public void scoreTestEvent( TestKey tk, TestEvent te, String descripXml, boolean skipVersionCheck, boolean clearExternal) throws Exception
    {
       try
       {

            if( te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED.getTestEventStatusTypeId() &&
                te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
            {
                if( te.getTestEventStatusTypeId() == TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() )
                {
                    LogService.logIt("BaseScoreManager.scoreTestEvent() NONFATAL - TestEvent.status is ScoringStarted (105). May be an odd timing issue. Resetting temporarily (no save) to Completed and continuing. TestEvent " + te.getTestEventId() );
                    te.setTestEventStatusTypeId( TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() );
                }
                
                else
                    throw new ScoringException( "TestEvent is not correct status type. Expecting completed. ", ScoringException.NON_PERMANENT, te );
            }

            te.setProfile( ProfileUtils.getLiveProfileForProductIdAndOrgId(te.getTestEventId(), te.getProductId(), te.getOrgId(), ProfileUsageType.REPORT_RANGES.getProfileUsageTypeId() ) );

            // LogService.logIt("BaseScoreManager.scoreTestEvent() START process. TestEvent " + te.getTestEventId() );
            // LogService.logIt("ScoreManager.scoreTestEvent() START process. TestEvent " + te.toString() + ", " + (te.getProfile()==null ? " Profile is null" : te.getProfile().toString() ) );
            
            
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            // get the product
            if( te.getProduct() == null )
                te.setProduct( eventFacade.getProduct( te.getProductId() ));

            if( te.getProduct() == null )
                throw new ScoringException( "Cannot find product for TestEvent productId=" + te.getProductId(), ScoringException.PERMANENT, te );

            if( te.getProduct().getProductType().getIsSimOrCt5Direct() && te.getProduct().getSimDescriptorId() <= 0 )
                throw new ScoringException( "Cannot score because simDescriptorId is invalid ", ScoringException.PERMANENT, te );

            if( te.getOrg() == null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                te.setOrg( userFacade.getOrg( te.getOrgId() ));
            }

            if( te.getUser() == null && te.getUserId()>0 )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                te.setUser( userFacade.getUser( te.getUserId() ));
                
                if( te.getUser()==null )
                    throw new ScoringException( "Cannot find TestEvent.user userId=" + te.getUserId(), ScoringException.PERMANENT, te );
                
                if( te.getUser() != null && ( te.getUser().getLocaleStr()== null || te.getUser().getLocaleStr().isEmpty() ) )
                {
                    te.getUser().setLocaleStr( te.getLocaleStr() );
                    userFacade.saveUser( te.getUser() );
                }
            }

            if( tk==null )
                tk = eventFacade.getTestKey( te.getTestKeyId(), true );

            te.setTestKey(tk);
            // LogService.logIt("ScoreManager.scoreTestEvent() loaded TestKey.testKeyId=" + (tk==null ? "null" : tk.getTestKeyId() ) + ", te.testEventId=" + te.getTestEventId() );

            if( ProctorHelpUtils.getUseExternalProctoring(tk.getOnlineProctoringType()) && (tk.getOnlineProctoringType().getProctorRecordingType().getRequiresDirectImagesOnly() || !tk.getOnlineProctoringType().getProctorRecordingType().equals(ProctorRecordingType.NONE)) )
            {
                if( te.getRemoteProctorEvent()==null )
                {
                    if( proctorFacade==null )
                        proctorFacade = ProctorFacade.getInstance();
                    te.setRemoteProctorEvent( proctorFacade.getRemoteProctorEventForTestEventId( te.getTestEventId() ));                    
                }   
                if( te.getRemoteProctorEvent()==null )
                {
                    String m = "BaseScoreManager.scoreTestEvent() XX4 TestEvent.remoteProctoringEvent is null. onlineProctoringType=" + tk.getOnlineProctoringType().getKey() + ",  testEventId=" + te.getTestEventId() + ", testKeyId=" + tk.getTestKeyId();
                    LogService.logIt( m );
                    TestEventLogUtils.createTestKeyLogEntry( te.getTestKeyId(), te.getTestEventId(), 0, m, null, null);
                    // throw new ScoringException( "RemoteProctorEvent not found. ", ScoringException.PERMANENT, te );
                }
                
                else if( !te.getRemoteProctorEvent().getRemoteProctorEventStatusType().getCompleteOrHigher() )
                {
                    if( tk.getOnlineProctoringType().getIsAnyBasic() || tk.getOnlineProctoringType().getIsPremiumImagesOnly() )
                        throw new ScoringException( "RemoteProctorEvent is not ready for scoring to take place. ", ScoringException.NON_PERMANENT, te );
                    
                    if( tk.getOnlineProctoringType().getRecordsVideo() ) // || tk.getOnlineProctoringType().getRecordsAudioOnly() )
                    {
                        Calendar cal = new GregorianCalendar();
                        cal.add( Calendar.MINUTE, -1*MIN_PREMIUMVIDEORECORD_WAIT_MINS  );

                        if( te.getRemoteProctorEvent().getEventCompleteDate()!=null && te.getRemoteProctorEvent().getEventCompleteDate().after( cal.getTime() ) )
                            throw new ScoringException( "RemoteProctorEvent is not ready for scoring to take place. Event records audio or video and have not waited at least " + MIN_PREMIUMVIDEORECORD_WAIT_MINS + " mins.", ScoringException.NON_PERMANENT, te );                        
                    }
                }                
            }
            

            
            if( te.getUser() == null )
            {
                // LogService.logIt("ScoreManager.scoreTestEvent() Seeking userId in TestKey te.testEventId=" + te.getTestEventId() );

                if( tk.getUserId()>0 )
                {
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();

                    te.setUser( userFacade.getUser( te.getUserId() ));

                    if( te.getUserId()!=tk.getUserId() )
                    {
                        te.setUserId( tk.getUserId() );
                        eventFacade.saveTestEvent(te);
                    }
                }
            }

            // this can happen with DirectTest object that gather user info inside the IMO - Sims Only
            if( te.getUser() == null && te.getProduct().getProductType().getIsSim() )
            {
                // LogService.logIt("ScoreManager.scoreTestEvent() User is still null - userId not in TestKey or TestEvent. TestKey.testKeyId=" + (tk==null ? "null" : tk.getTestKeyId() ) + ", te.testEventId=" + te.getTestEventId() );
                
                if( tk.getUserId()>0 )
                    throw new ScoringException( "Getting testEvent.user from the ResultXML but there is already a user identified in the TestKey. TestKey.id=" + tk.getTestKeyId() + ", testKey.userId=" + tk.getUserId(), ScoringException.PERMANENT, te );
                
                // Get the score xml and it's object.
                if( te.getResultXml() == null || te.getResultXml().isEmpty() )
                    throw new ScoringException( "Result XML is empty. ", ScoringException.PERMANENT, te );

                te.setResultXmlObj( JaxbUtils.ummarshalImoResultXml( te.getResultXml() ) );

                String fn = te.getResultXmlObj().getUser().getFn();
                String ln = te.getResultXmlObj().getUser().getLn();
                String em = te.getResultXmlObj().getUser().getEm();

                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                User u =userFacade.getUserByEmailAndOrgId( em, te.getOrgId() );

                if( u == null )
                {
                    u = new User();
                    u.setFirstName(fn);
                    u.setLastName(ln);
                    u.setEmail(em);
                    u.setOrgId( te.getOrgId() );
                    u.setSuborgId( te.getSuborgId() );

                    u.setUserTypeId( UserType.NAMED.getUserTypeId() );
                    u.setRoleId( RoleType.NO_LOGON.getRoleTypeId() );
                    u.setUserCompanyStatusTypeId( UserCompanyStatusType.APPLICANT.getUserCompanyStatusTypeId() );
                    u.setUserStatusTypeId( 0 );
                    u.setPassword( StringUtils.generateRandomString( 10 ) );
                    u.setCreateDate( new Date() );
                    u.setLastUpdate( new Date() );

                    String tempUsername;
                    User tempUser;

                    do
                    {
                        tempUsername = StringUtils.generateRandomString( 12 );

                        tempUser = userFacade.getUserByUsername( tempUsername );

                    }  while( tempUser != null );

                    u.setUsername( tempUsername );

                    userFacade.saveUser(u);
                }

                te.setUser(u);
                te.setUserId( u.getUserId() );

                eventFacade.saveTestEvent( te );

                if( tk!=null && tk.getUserId() != u.getUserId() )
                {
                    tk.setUserId( u.getUserId() );
                    eventFacade.saveTestKey( tk );
                }

                // LogService.logIt("ScoreManager.scoreTestEvent() DDD now using userId=" + u.getUserId() + " for TestKey.testKeyId=" + (tk==null ? "null" : tk.getTestKeyId() ) + ", te.testEventId=" + te.getTestEventId() );

            }

            if( te.getSuborgId()> 0 && te.getSuborg()==null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                te.setSuborg( userFacade.getSuborg(te.getSuborgId()) );
            }

            //LogService.logIt( "ScoreManager.scoreTestEvent() getting IP Data, te.testEventId=" + te.getTestEventId() );
            setIpLocationData( te );

            // LogService.logIt( "ScoreManager.scoreTestEvent() back from IP Data, te.testEventId=" + te.getTestEventId() );
            
            List<TestEventScore> tesl = eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true );
                   
            te.setTestEventScoreList(tesl);
            
            //if( tesl.size() > 0 )
            //    eventFacade.clearExternalScoresForEvent(te, false);
            

            // if using provided xml, create a dummy sim descriptor
            if( (te.getProduct().getProductType().getIsSimOrCt5Direct() ) && descripXml != null && !descripXml.isEmpty() )
            {
                LogService.logIt("ScoreManager.scoreTestEvent() Using custom provided SimDescripXml. te.testEventId=" + te.getTestEventId() );

                SimDescriptor sd = new SimDescriptor();

                //sd.setSimId( te.getSimId() );
                //sd.setSimVersionId( te.getSimVersionId() );
                sd.setXml(descripXml);

                te.setSimDescriptor(sd);
            }

            else if( te.getProduct().getProductType().getUsesSimDescriptor() )
            {
                // get the SimDescriptor and it's object.
                te.setSimDescriptor( eventFacade.getSimDescriptor( te.getSimId(), te.getSimVersionId(), true ) );
                // te.setSimDescriptor( eventFacade.getSimDescriptor( te.getProduct().getSimDescriptorId(), true ) );
                // LogService.logIt( "ScoreManager.scoreTestEvent() descripXml: " + te.getSimDescriptor().getXml() );
                
                if( te.getSimDescriptor()==null && te.getProduct().getSimDescriptorId()>0 )
                    te.setSimDescriptor( eventFacade.getSimDescriptor( te.getProduct().getSimDescriptorId() ));
            }

            
            
            if( te.getSimDescriptor() == null && te.getProduct().getProductType().getUsesSimDescriptor() )
                throw new ScoringException( "Cannot find SimDescriptor for SimDescriptorId=" + te.getProduct().getSimDescriptorId(), ScoringException.PERMANENT, te );

            if( te.getProduct().getProductType().getUsesSimDescriptor() && ( te.getSimDescriptor().getXml() == null || te.getSimDescriptor().getXml().isEmpty()) )
                throw new ScoringException( "SimDescriptor XML is empty. simDescriptorId=" + te.getProduct().getSimDescriptorId(), descripXml == null || descripXml.isEmpty() ? ScoringException.PERMANENT : ScoringException.NON_PERMANENT, te );

            Date procStart = new Date();

            //LogService.logIt( "ScoreManager.scoreTestEvent() getting testEventScorer te.testEventId=" + te.getTestEventId());
            
            TestEventScorer ts = TestEventScorerFactory.getTestEventScorer(te);

            //LogService.logIt( "ScoreManager.scoreTestEvent() using " + ts.toString() );
            ts.setClearExternal(clearExternal);
            ts.scoreTestEvent(te, te.getSimDescriptor(), skipVersionCheck );
            
            if( te.getTestEventScoreList()==null || te.getTestEventScoreList().isEmpty() )
                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ) );
            
            TestEventLogUtils.createTestEventLogEntry(te.getTestEventId(),  "ScoreManager.scoreTestEvent() FINISH te.testEventId=" + te.getTestEventId() + " Results: " + getScoreStringForLogs( te, te.getTestEventScoreList() ) + ", Scorer Info: " + ts.getScoreStatusStr() );                    

            // LogService.logIt("ScoreManager.scoreTestEvent() COMPLETED te.testEventId=" + te.getTestEventId());
            
            Tracker.addResponseTime( "Score TestEvent", new Date().getTime() - procStart.getTime() );
       }

       catch( ScoringException e )
       {
           LogService.logIt("ScoreManager.scoreTestEvent() " + e.toString() + ", " + te.toString() );           
           TestEventLogUtils.createTestEventLogEntry( te.getTestEventId(),  "ScoreManager.scoreTestEvent() ScoringException Scoring Test Event. " + e.toString() );                               
           throw e;
       }
       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt(e, "ScoreManager.scoreTestEvent() "  + te.toString() );

           if( te!=null )
                TestEventLogUtils.createTestEventLogEntry( te.getTestEventId(),  "ScoreManager.scoreTestEvent()  Exception Scoring Test Event. " + e.toString() );                    
           
           throw new ScoringException( e.getMessage() + "ScoreManager.scoreTestEvent() " , ScoreUtils.getExceptionPermanancy(e) , te );
       }
    }

    
    protected void saveErrorInfo( ScoringException se )
    {
        TestEventLog testEventLog = null;
        try
        {
            Tracker.addScoreError();

            LogService.logIt( "ScoreManager.saveErrorInfo() " + ( se==null ? "" : (se.getMessage() + ", " + (se.getErrorTxtObj()==null ? "null" : se.getErrorTxtObj().toString()))));

            if( se==null )
                throw new Exception( "ScoringException is null " );
            
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            ErrorTxtObject eo = se.getErrorTxtObj();

            boolean debug = RuntimeConstants.getBooleanValue( "scoreDebugMode" ).booleanValue();
                
            testEventLog = new TestEventLog();
            testEventLog.setLevel(0);
            testEventLog.setLog( "ScoreManager.saveErrorInfo() ScoringException caught. se.msg=" + se.getMessage() ) ;
            
            if( eo instanceof TestEvent && ((TestEvent)eo).getIsSurveyEvent() )
            {
                SurveyEvent sue = (SurveyEvent) ((TestEvent)eo).getSurveyEvent();

                testEventLog.setTestKeyId( sue.getTestKeyId() );
                testEventLog.appendLogEntry( "This entry is for SurveyEventId=" + sue.getSurveyEventId(), 0 );
                
                // if a bad error
                if( !debug && se.getSeverity()==ScoringException.PERMANENT )
                {
                    
                    sue.setSurveyEventStatusTypeId( TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId() );
                    sue.setErrorCnt( sue.getErrorCnt()+1 );
                    eventFacade.saveSurveyEvent( sue );                    
                }
                
            }
            
            
            else if( eo instanceof TestEvent )
            {
                TestEvent te = (TestEvent) eo;
                testEventLog.setTestEventId( te.getTestEventId() );
                testEventLog.setTestKeyId( te.getTestKeyId() );

                // if a bad error
                if( !debug && se.getSeverity()==ScoringException.PERMANENT )
                {
                    
                    te.setTestEventStatusTypeId( TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId() );

                    // also set the Test Key to be in error
                    TestKey tk = eventFacade.getTestKey( te.getTestKeyId(), true );

                    if( tk.getBatteryId()<=0 )
                    {
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORE_ERROR.getTestKeyStatusTypeId() );
                        tk.setErrorCnt( tk.getErrorCnt()+1 );
                    }

                    tk.setErrorTxt( (tk.getErrorTxt()==null ? "" : tk.getErrorTxt()) + ", TestEvent " + te.getTestEventId() + " had a scoring error and was skipped in the battery scoring process. " + se.getMessage() );

                    eventFacade.saveTestKey( tk );
                    eventFacade.saveTestEvent( (TestEvent) eo );
                    
                }
            }
            
            else if( eo instanceof TestKey )
            {
                testEventLog.setTestKeyId( ((TestKey) eo).getTestKeyId() );
                if( !debug && se.getSeverity()==ScoringException.PERMANENT )
                {
                    ((TestKey) eo).setTestKeyStatusTypeId( TestKeyStatusType.SCORE_ERROR.getTestKeyStatusTypeId() );
                    ((TestKey) eo).setErrorCnt( ((TestKey) eo).getErrorCnt()+1 );
                    eventFacade.saveTestKey( (TestKey) eo );
                }
            }
            
            if( testEventLog.getTestKeyId()>0 || testEventLog.getTestEventId()>0 )
                eventFacade.saveTestEventLog(testEventLog);
        }
        catch( ScoringException e )
        {
            LogService.logIt( "ScoreManager.saveErrorInfo() " + e.toString()+ ", testKeyId=" + (testEventLog==null ? "null" : testEventLog.getTestKeyId()) + ", testEventId=" + (testEventLog==null ? "null" : testEventLog.getTestEventId())  );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ScoreManager.saveErrorInfo() "+ ", testKeyId=" + (testEventLog==null ? "null" : testEventLog.getTestKeyId()) + ", testEventId=" + (testEventLog==null ? "null" : testEventLog.getTestEventId()) );
        }
    }
    
    protected void setIpLocationData( TestEvent te )
    {
            if( te.getIpCountry()!=null && !te.getIpCountry().isBlank() )
                return;
        
            User user = null;

            if( te.getIpAddress()!=null && !te.getIpAddress().isBlank() &&
                (te.getIpCountry()==null || te.getIpCountry().isBlank() || te.getIpState()==null || te.getIpState().isBlank() ) )
            {
               //LogService.logIt("ScoreManager.setIpLocationData() START " + te.getIpAddress() );  // v

               try
               {
                  user = te.getUser();

                  if( user == null )
                  {
                      if( userFacade == null )
                          userFacade = UserFacade.getInstance();

                      user = userFacade.getUser( te.getUserId() );
                  }

                  // Appears to be unreliable unless the user is brand new, in which case we need to look it up.
                  //if( 1==2 && user.getIpCountry()!=null && !user.getIpCountry().isEmpty() )
                  //{
                  //    te.setIpCountry( user.getIpCountry() );
                  //    te.setIpState( user.getIpState() );
                   //   te.setIpCity( user.getIpCity() );
                   //   te.setGeographicRegionId( user.getGeographicRegionId() );
                   //   return;
                  //}
                  
                  IpUtils ipUtils = new IpUtils();
                  
                  String[] ipData = ipUtils.getIPLocationData(te.getIpAddress(), 0 );

                  //LogService.logIt("ScoreManager.setIpLocationData() BBB ipData[0]=" + ipData[0] );  // v

                  if( ipData!=null && ipData[0]!=null && !ipData[0].isBlank() )
                  {
                    te.setIpCountry( ipData[0] );
                    te.setIpState( ipData[1] );
                    te.setIpCity( ipData[2] );
                  }
                  
                  te.setUser(user);

                  Country ctry = null;

                  if( te.getIpCountry()!=null && !te.getIpCountry().isEmpty() )
                  {
                      if( userFacade == null )
                          userFacade = UserFacade.getInstance();
                      
                      ctry = userFacade.getCountryByCode( te.getIpCountry() );

                      if( ctry != null )
                          te.setGeographicRegionId( ctry.getGeographicRegionId() );
                  }

                  // Update User
                  if( te.getIpCountry()!=null && !te.getIpCountry().isBlank() &&
                      (user.getIpCountry()==null || user.getIpCountry().isBlank() || !user.getIpCountry().equalsIgnoreCase( te.getIpCountry() )) )
                  {
                    if( ctry != null )
                        user.setCountryCode( ctry.getCountryCode() );

                    user.setIpCountry( te.getIpCountry() );
                    user.setIpState( te.getIpState() );
                    user.setIpCity( te.getIpCity() );
                    user.setGeographicRegionId( te.getGeographicRegionId() );

                  }

                   user.setIpZip( ipData[3] );
                   user.setIpTimezone( ipData[4] );

                   if( user.getIpTimezone()!=null && !user.getIpTimezone().isEmpty() && (user.getTimeZoneId()==null || user.getTimeZoneId().isEmpty() ) )
                       user.setTimeZoneId( user.getIpTimezone() );

                   if( userFacade == null )
                       userFacade = UserFacade.getInstance();
                   
                   userFacade.saveUser(user);
               }

               catch( Exception ee )
               {
                      LogService.logIt( ee, "ScoreManager.setIpLocationData() Reading IP locations ip=" + te.getIpAddress() + ", te.testEventId=" + te.getTestEventId() );
               }
            }

    }

    protected String getScoreStringForLogs( TestEvent te, List<TestEventScore> tesl )
    {
        if( !te.getTestEventStatusType().getIsScoredOrHigher() )
            return " Scoring not completed. teid=" + te.getTestEventId() + ", te.statusId=" + te.getTestEventStatusTypeId() + " " + te.getTestEventStatusType().getKey();
        
        StringBuilder sb = new StringBuilder();
        
        sb.append( " teid=" + te.getTestEventId() + ", te.statusId=" + te.getTestEventStatusTypeId() + ", overallScore=" + te.getOverallScore() + ", Percentiles: Ovrl=" + te.getOverallPercentile() + ", ct=" + te.getOverallPercentileCount() + ", acct=" + te.getAccountPercentile() + ", ctry=" + te.getCountryPercentile() );
        
        if( tesl == null )
            tesl = te.getTestEventScoreList();
        
        // LogService.logIt( "ScoreManager.getScoreStringForLogs() tesl="  + (tesl==null ? "null" : tesl.size()) );
        if( tesl != null )
        {
            for( TestEventScore tes : tesl )
            {
                // if( tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) || tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() ))
                if( tes.getScore()> 0 || tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) || tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() ) )
                    sb.append( "\n" + tes.getName() + ", Raw=" + tes.getRawScore() + ", score=" + tes.getScore() );
            }
        }
        
        return sb.toString();
    }

    
    public Throwable getCause(Throwable e) 
    {
        Throwable cause = null; 
        Throwable result = e;

        while(null != (cause = result.getCause())  && (result != cause) ) 
        {
            result = cause;
        }
        return result;
    }    
    
}
