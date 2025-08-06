/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.av.AvScoringUtils;
import com.tm2score.ct5.event.Ct5EventFacade;
import com.tm2score.score.scorer.TestEventScorer;
import com.tm2score.score.scorer.StandardSurveyEventScorer;
import com.tm2score.score.scorer.TestEventScorerFactory;
import com.tm2score.dist.DistManager;
import com.tm2score.entity.ai.AiMetaScore;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.entity.ct5.event.Ct5ItemResponse;
import com.tm2score.entity.ct5.event.Ct5TestEvent;
import com.tm2score.entity.event.SurveyEvent;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.user.User;
import com.tm2score.essay.EssayFacade;
import com.tm2score.event.*;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.purchase.ProductType;
import com.tm2score.report.ReportManager;
import com.tm2score.score.scorer.CT5DirectTestEventScorer;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.user.UserFacade;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * NOTE: This is needed to ensure that all database transactions commit before a given record in the DBMS is read again.
 *
 *
 * @author Mike
 */
public class ScoreManager extends BaseScoreManager
{
    public static boolean FIRST_BATCH = true;
    public static boolean OK_TO_START_NEW = false;
    public static boolean OK_TO_START_ANY = true;

    public static boolean BATCH_IN_PROGRESS = false;
    public static boolean DEBUG_SCORING = false;
    
    

    
    // @Inject
    /*
    private EventFacade eventFacade;

    private UserFacade userFacade = null;

    private NormFacade normFacade = null;
    */
    


    public int[] doScoreBatch(boolean withArchive, boolean noThread)
    {
        // LogService.logIt( "ScoreManager.doScoreBatch() Starting OK_TO_START_ANY=" + OK_TO_START_ANY + ", OK_TO_START_NEW=" + OK_TO_START_NEW + ", BATCH_IN_PROGRESS=" + BATCH_IN_PROGRESS + ", ReportManager.BATCH=" + ReportManager.BATCH_IN_PROGRESS + ", DistManager.BATCH=" + DistManager.BATCH_IN_PROGRESS   );

        int[] count = new int[8];

        int surveyCount = 0;

        // Check for tests needing score
        try
        {
            // ScoreManager sm = new ScoreManager();

            // If batches have been turned off.
            if( !OK_TO_START_ANY )
                return count;
            
            if( RuntimeConstants.getBooleanValue( "ForceNoThreadBasedBatchesInScoreReportDistrib") )
                noThread = true;

            if( noThread && ( ScoreManager.BATCH_IN_PROGRESS || ReportManager.BATCH_IN_PROGRESS || DistManager.BATCH_IN_PROGRESS ) )
            {
                LogService.logIt( "ScoreManager.doScoreBatch() Cannot start a new Batch. noThread=true and OK_TO_START_ANY=" + OK_TO_START_ANY + ", OK_TO_START_NEW=" + OK_TO_START_NEW + ", BATCH_IN_PROGRESS=" + BATCH_IN_PROGRESS + ", ReportManager.BATCH=" + ReportManager.BATCH_IN_PROGRESS + ", DistManager.BATCH=" + DistManager.BATCH_IN_PROGRESS   );
                
                // lower the pag check size to speed up existing batches.
                // DiscernFacade.ESSAY_PLAG_CHECK_MAX_OFFSET = 2000;
                return count;
            }
            
            // raise back up. Will lower if we have too many test events.
            // DiscernFacade.ESSAY_PLAG_CHECK_MAX_OFFSET = 5000;
            

            Tracker.addScoreBatch();

            // since the App may have been killed during mid-scoring, need to clean up any partials during restart.
            // This only needs to happen on startup.
            if( !OK_TO_START_NEW )
            {
                // LogService.logIt( "ScoreManager.doScoreBatch() OK_TO_START_NEW=false. Calling clearPartiallyScoredTestKeys() " );               
                clearPartiallyScoredTestKeys();
                OK_TO_START_NEW = true;
            }

            if( noThread )
                BATCH_IN_PROGRESS = true;

            count = scoreBatch( withArchive, noThread );

            Thread.sleep( 200 );

            surveyCount = scoreBatchOfSurveyEvents();

            FIRST_BATCH = false;
            
            if( noThread )
                BATCH_IN_PROGRESS = false;

            // LogService.logIt( "ScoreManagerBean.doScoreBatch() Completed scoring " + count[0] + " TestKeys, total of " + count[1] + " TestEvents, and " + surveyCount + " Surveys." );
        }

        catch( STException e )
        {
            // do nothing. Already logged.
            if( noThread )
                BATCH_IN_PROGRESS = false;
        }

        catch( Exception e )
        {
            if( noThread )
                BATCH_IN_PROGRESS = false;
            LogService.logIt(e, "ScoreManagerBean.doScoreBatch() " );
            EmailUtils.getInstance().sendEmailToAdmin( "ScoreManagerBean.doScoreBatch() Error during score Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );

        }

        return count;

    }



    public int[] rescoreTestKey( long testKeyId, boolean rescoreOnly, boolean clearExternal) throws Exception
    {
        try
        {            
            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(testKeyId, true );

            if( tk == null )
                throw new Exception( "TestKey not found." );

            if( tk.getTestKeyStatusTypeId() < TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() )
                throw new Exception( "TestKey is not completed yet. Cannot score. " );

            if( rescoreOnly && tk.getTestKeyStatusTypeId() < TestKeyStatusType.SCORED.getTestKeyStatusTypeId() )
                throw new Exception( "TestKey status type is below SCORED and rescoreOnly flag is set. Cannot perform rescore. testKeyStatusTypeId=" + tk.getTestKeyStatusTypeId() );
            
            if( !BaseScoreManager.isScoringFirstTimeOrRepeatAllowed(testKeyId) )
                throw new STException( "g.PassThru", new String[] {"TestKey has been active in scoring system in last 5 minutes. Please wait and try again. testKeyId=" + testKeyId } );
            
            resetTestKeyStatusForScoring(tk, true, clearExternal );

            Thread.sleep( 200 );

            LogService.logIt("ScoreManager.rescoreTestKey() " + testKeyId + " testKey and TestEvent status reset for scoring. " + tk.getTestKeyStatusTypeId() );

            try
            {
                /*
                * int[0] = testkeys scored.
                * int[1] = testevents scored.
                * int[2] = testkeys placed in pending status
                * int[3] = testevents placed in pending status
                * int[4] = testevents skipped because they are incompleted (only if partiallyCompleteBatteriesOk=true)     
                  int{5] = returned to testKey.completed because has unscored test events (probably proctoring)
                */
                int[] tko = scoreTestKey(tk, PARTIAL_BATTERIES_MARKED_COMPLETE_OK, true );
                return tko;
            }

            catch( ScoringException e )
            {
                saveErrorInfo(e);
                throw new Exception( e.getMessage() );
            }
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ScoreManager.rescoreTestKey() " + testKeyId );
            throw new STException( e );
        }
    }


    public void recalcPercentilesForTestEvent( long testEventId, String descripXml  ) throws Exception
    {
        try
        {
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            TestEvent te = eventFacade.getTestEvent(testEventId, true );

            if( te == null )
                throw new Exception( "TestEvent not found." );

            if( te.getTestEventStatusTypeId() < TestEventStatusType.SCORED.getTestEventStatusTypeId() )
                throw new Exception( "TestEvent is not completed yet. Cannot score." );

            Product p = te.getProduct()==null ? eventFacade.getProduct( te.getProductId() ) : te.getProduct();

            te.setProduct(p);

            boolean canRecalc = resetTestEventStatusForRecalcPercentiles( te );

            if( canRecalc )
            {
                try
                {
                    if( p.getProductType().getIsSimOrCt5Direct() )
                        te.setSimDescriptor( eventFacade.getSimDescriptor( te.getSimId(), te.getSimVersionId(), true ) );

                    te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(testEventId, true ) );

                    TestEventLogUtils.createTestEventLogEntry(testEventId,  "ScoreManager.recalcPercentilesForTestEvent() Recalculating percentiles. OLD VALUES: " + this.getScoreStringForLogs(te, te.getTestEventScoreList() ) );
                    
                    TestEventScorer ts = TestEventScorerFactory.getTestEventScorer(te);

                    ts.recalculatePercentilesForTestEvent(te, null );

                    te.setTestEventStatusTypeId( TestEventStatusType.SCORED.getTestEventStatusTypeId() );

                    eventFacade.saveTestEvent(te);

                    te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(testEventId, true ) );
                    
                    TestEventLogUtils.createTestEventLogEntry(testEventId,  "ScoreManager.recalcPercentilesForTestEvent() Recalculated percentiles. NEW VALUES: " + this.getScoreStringForLogs(te, te.getTestEventScoreList() ) );
                }

                catch( ScoringException e )
                {
                    saveErrorInfo(e);
                    throw new Exception( e.getMessage() );
                }                                
            }
            
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ScoreManager.recalcPercentilesForTestEvent() " + testEventId );

            throw new STException( e );
        }
    }
    
    
    public void rescoreSurveyEvent( long surveyEventId ) throws Exception
    {
        try
        {
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            SurveyEvent te = eventFacade.getSurveyEvent(surveyEventId);

            if( te == null )
                throw new Exception( "SurveyEvent not found." );

            if( te.getSurveyEventStatusTypeId() < TestEventStatusType.COMPLETED.getTestEventStatusTypeId() )
                throw new Exception( "SurveyEvent is not completed yet. Cannot score." );
            
            eventFacade.deleteItemResponsesForSurveyEventId(surveyEventId);

            this.scoreSurveyEvent(te);
        }
        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ScoreManager.rescoreSurveyEvent() " + surveyEventId );
            throw new STException( e );
        }        
    }

    
    

    public void rescoreTestEvent( long testEventId, String descripXml, boolean clearExternal, boolean skipVersionCheck, boolean resetSpeechText) throws Exception
    {
        try
        {
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            TestEvent te = eventFacade.getTestEvent(testEventId, true );

            if( te == null )
                throw new Exception( "TestEvent not found." );

            if( te.getTestEventStatusTypeId() < TestEventStatusType.COMPLETED.getTestEventStatusTypeId() )
                throw new Exception( "TestEvent is not completed yet. Cannot score." );
            
            boolean canRescore = resetTestEventStatusForScoring(te, true, clearExternal, resetSpeechText );

            if( canRescore )
            {
                try
                {
                    // scoring will only balance ItemResponses for the tempitemresponse table. Need to do this to remove from itemresponse table otherwise will get double.
                    // LogService.logIt( "ScoreManager.rescoreTestEvent() deleting existing itemresponses. testEventId=" + testEventId );
                    eventFacade.deleteItemResponsesForTestEventId(testEventId);
                    
                    if( clearExternal && te.getProductTypeId()==ProductType.CT5DIRECTTEST.getProductTypeId() && te.getResultXml()!=null && !te.getResultXml().isBlank() )
                    {
                        Ct5EventFacade ct5EventFacade = Ct5EventFacade.getInstance();
                        Ct5TestEvent ct5Te = ct5EventFacade.getCt5TestEventForTestEventIdAndSurveyEventId(te.getTestEventId(), 0);
                        if( ct5Te!=null )
                        {
                            List<Ct5ItemResponse> irl = ct5EventFacade.getCt5ItemResponsesForCt5Test(ct5Te.getCt5TestEventId());
                            if( irl!=null && !irl.isEmpty() )
                            {
                                // OK, we can safely delete te.resultXml
                                te.setResultXml(null);
                                eventFacade.saveTestEvent(te);
                            }
                        }
                    }
                    
                    scoreTestEvent(null, te, descripXml, skipVersionCheck, clearExternal );                   
                }

                catch( ScoringException e )
                {
                    saveErrorInfo(e);
                    throw e;
                }
            }
        }

        catch( STException e )
        {
            throw e;
        }
        catch( ScoringException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ScoreManager.rescoreTestEvent() " + testEventId );

            throw new STException( e );
        }
    }



    /**
     *
     * int[0] = testkeys scored.
     * int[1] = testevents scored.
     * int[2] = testkeys placed in pending status
     * int[3] = testevents placed in pending status
     * int[4] = testevents skipped because they are incompleted (only if partiallyCompleteBatteriesOk=true)
     * int[5] = incomplete battery test events scored
     * int[6] = incomplete battery test events in pending.
     * int[7] = testkeys returned to completed because they have unscored testevents (not pending, probably waiting on proctoring)
     * 
     * @return
     * @throws Exception
     */
    private int[] scoreBatch(boolean withArchive, boolean noThread) throws Exception
    {
        int[] out = new int[8];

        if( !OK_TO_START_ANY )
            return out;

        if( eventFacade == null )
            eventFacade = EventFacade.getInstance();

        List<TestKey> tkl = new ArrayList<>();
        
        List<Integer> orgIdsToSkip = RuntimeConstants.getIntList("OrgIdsToSkip",",");
        
        // this is a second check. 
        if( FIRST_BATCH && RuntimeConstants.getBooleanValue("seekStartedScoresFirstBatch") )
        {
            int[] vs = clearPartiallyScoredTestKeys();
            if( vs[0]>0 )
                LogService.logIt( "ScoreManager.scoreBatch() Cleared " + vs[0] + " partially scored test keys for scoring restart." );
        }
        
        // Will retry any testkyes with errors that have not exceeded max errors.
        if( RuntimeConstants.getBooleanValue("retryErroredScoresInBatch") )
        {
            int[] vs = clearScoreErrorTestKeys();
            if( vs[0]>0 )
                LogService.logIt( "ScoreManager.scoreBatch() Cleared " + vs[0] + " test keys for scoring retry." );
        }
                
        // if( tkl.size()<Constants.DEFAULT_TESTKEY_BATCH_SIZE )
        tkl.addAll(eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.COMPLETED_PENDING_EXTERNAL.getTestKeyStatusTypeId(), Constants.DEFAULT_TESTKEY_BATCH_SIZE, withArchive || FIRST_BATCH, -1, orgIdsToSkip ) );
            
        if( tkl.size()<Constants.DEFAULT_TESTKEY_BATCH_SIZE )
            tkl.addAll(eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId(), Constants.DEFAULT_TESTKEY_BATCH_SIZE-tkl.size(), withArchive || FIRST_BATCH, -1, orgIdsToSkip ) );
        
        int[] tko;

        //if( !tkl.isEmpty() )
        //    LogService.logIt( "ScoreManagerBean.scoreBatch() found " + tkl.size() + " test Keys to score." );

        if( tkl.size() >= 30 )
            EssayFacade.ESSAY_PLAG_CHECK_MAX_OFFSET = 2000;
        else
            EssayFacade.ESSAY_PLAG_CHECK_MAX_OFFSET = 4000;
        
        boolean useThread = !noThread;
        
        long delayBetweenScores = 250;
        
        // for each testkey, get the test events
        for( TestKey tk : tkl )
        {
            // always refresh just in case a parallel process got it while waiting.
            tk = eventFacade.getTestKey(tk.getTestKeyId(), true );

            if( !OK_TO_START_ANY )
            {
                LogService.logIt( "ScoreManager.scoreBatch() New Batches are turned off so stopping this batch.  testKeyId=" + tk.getTestKeyId() + ", noThread=" + noThread );
                continue;
            }
                
            
            if( tk.getTestKeyStatusType().getIsScoredOrHigher() )
            {
                LogService.logIt( "ScoreManager.scoreBatch() reload of TestKey has it in a Scored or higher status. Skipping. testKeyId=" + tk.getTestKeyId() );
                continue;
            }
            
            if( !FIRST_BATCH && tk.getTestKeyStatusType().equals( TestKeyStatusType.SCORING_STARTED) )
            {
                LogService.logIt( "ScoreManager.scoreBatch() Not First Batch and reload of TestKey has it in a ScoringStarted status. Skipping. testKeyId=" + tk.getTestKeyId() );
                continue;
            }     
            
            if( !isScoringFirstTimeOrRepeatAllowed( tk.getTestKeyId() ) )
            {
                LogService.logIt("ScoreManager.scoreBatch() Last Scoring Call was within min last date window. IGNORING SCORING of Test Key. testKeyId=" + tk.getTestKeyId() ); 
                continue;
                //return out;                                                    
            } 
            
            // determine if we need to shift to noThread
            useThread = !noThread;
            
            // if supposed to use thread, then check for max parallels
            if( useThread && getTestKeysAndPartialEventsInScoringCount()>=MAX_THREAD_TESTKEY_SCORE_COUNT )
            {
                LogService.logIt( "ScoreManager.scoreBatch() Shifting to NoThread because TestKeysInScoring count is too high (" + getTestKeysInScoringCount() + ") for testKeyId=" + tk.getTestKeyId() );  
                useThread=false;   
                delayBetweenScores = 1000;
            }
            
            try
            {
                if( !useThread )
                {
                    tko = scoreTestKey(tk, PARTIAL_BATTERIES_MARKED_COMPLETE_OK, !useThread );
                    out[0] += tko[0];
                    out[1] += tko[1];
                    out[2] += tko[2];
                    out[3] += tko[3];
                    out[7] += tko[5];
                }
                else
                {
                    // LogService.logIt( "ScoreManager.scoreBatch() Starting Score Thread tkid=" + tk.getTestKeyId() );
                    new Thread(new TestKeyScoreThread( tk, PARTIAL_BATTERIES_MARKED_COMPLETE_OK, 0 )).start(); 
                }
                Thread.sleep(delayBetweenScores);
            }

            catch( ScoringException e )
            {
                saveErrorInfo( e );
                // next test key
            }
        }
        
        //if( 1==2 )
        //    return out;
        
        
        List<Long> partialCompleteBatteryTestEventIdsToScore = eventFacade.getIncompleteBatteryTestEventIdsToScore( 100, false, 10 );
        
        //if( !partialCompleteBatteryTestEventIdsToScore.isEmpty() )
        //    LogService.logIt("ScoreManager.scoreBatch() partialCompleteBatteryTest.size=" + partialCompleteBatteryTestEventIdsToScore.size() );
        TestEvent te;
        int[] intarr;
        for( Long teid : partialCompleteBatteryTestEventIdsToScore )
        {
            te = eventFacade.getTestEvent(teid, true);
            if( !te.getTestEventStatusType().getIsCompleteOrPendingExternal() )
            {
                LogService.logIt( "ScoreManager.scoreBatch() skipping partialCompleteBatteryTestEventId=" + teid + " because testEvent is no longer in complete or complete pending status" );
                continue;
            }
            te.setTestKey( eventFacade.getTestKey( te.getTestKeyId(), true));
            if( te.getTestKey().getTestKeyStatusType().getIsCompleteOrHigher() )
            {
                LogService.logIt( "ScoreManager.scoreBatch() skipping partialCompleteBatteryTestEventId=" + teid + " because test key is now in complete or higher status" );
                continue;
            }
                        
            te.setPartialBatteryTestEvent(true);
            
            // determine if we need to shift to noThread
            useThread = !noThread;
            
            // if supposed to use thread, then check for max parallels
            if( useThread && getTestKeysAndPartialEventsInScoringCount()>=MAX_THREAD_TESTKEY_SCORE_COUNT )
            {
                LogService.logIt( "ScoreManager.scoreBatch() BBB.2 Shifting to NoThread for incomplete Battery testEventId=" + te.getTestEventId() + ", testKeyId=" + te.getTestKeyId() );  
                useThread=false;
            }            
            
            if( !useThread )
            {
                intarr = scorePartiallyCompleteBatteryTestEvent(te, !useThread);
                if( intarr!=null )
                {   
                    if(intarr[1]==1 )
                        out[5]++;
                    if( intarr[2]==1 || intarr[3]==1 )
                        out[6]++;
                }
            }
            
            else
            {
                // LogService.logIt( "ScoreManager.scoreBatch() Starting Score Thread tkid=" + tk.getTestKeyId() );
                new Thread(new TestKeyScoreThread( te, 0 )).start(); 
                //Thread.sleep(100);                
            }
            
            Thread.sleep(delayBetweenScores);                

        }

        return out;
    }


    
    public int scoreBatchOfSurveyEvents() throws Exception
    {

        if( !OK_TO_START_ANY )
            return 0;

        if( eventFacade == null ) 
            eventFacade = EventFacade.getInstance();

        List<SurveyEvent> tkl = new ArrayList<>();

        // try errors again.
        tkl.addAll(eventFacade.getNextBatchOfSurveyEventsToScore(TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId(), 2 ) );
        for( SurveyEvent se : tkl )
        {
            if( se.getSurveyEventStatusTypeId()==TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId())
            {
                LogService.logIt( "ScoreManagerBean.scoreBatchOfSurveyEvents() Setting errored SurveyEvent for retry. surveyEventId=" + se.getSurveyEventId() + ", errorCnt=" + se.getErrorCnt() );
                se.setSurveyEventStatusTypeId(TestEventStatusType.COMPLETED.getTestEventStatusTypeId());
                eventFacade.saveSurveyEvent(se);
            }
        }
        
        // Clear partials.
        if( FIRST_BATCH && RuntimeConstants.getBooleanValue("seekStartedScoresFirstBatch") ) 
        {
            tkl.addAll(eventFacade.getNextBatchOfSurveyEventsToScore(TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId(), -1) );
            for( SurveyEvent se : tkl )
            {
                if( se.getSurveyEventStatusTypeId()==TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId())
                {
                    se.setSurveyEventStatusTypeId(TestEventStatusType.COMPLETED.getTestEventStatusTypeId());
                    eventFacade.saveSurveyEvent(se);
                }
            }
        }

        tkl.addAll(eventFacade.getNextBatchOfSurveyEventsToScore(TestEventStatusType.COMPLETED.getTestEventStatusTypeId(), -1) );
        
        int count = 0;

        //if( tkl.size()>0 )
        //    LogService.logIt( "ScoreManagerBean.scoreBatchOfSurveyEvents() found " + tkl.size() + " Survey Events to score." );

        // for each testkey, get the test events
        for( SurveyEvent se : tkl )
        {

            if( !isScoringFirstTimeOrRepeatAllowed( se.getTestKeyId() ) )
            {
                LogService.logIt("ScoreManager.scoreBatchOfSurveyEvents() Last Scoring Call for this testKeyId was within min last date window. Skipping. surveyEventId=" + se.getSurveyEventId() + ", testKeyId=" + se.getTestKeyId() ); 
                continue;
                //return out;                                                    
            }             
            
            try
            {
                scoreSurveyEvent( se );
                count++;
            }

            catch( ScoringException e )
            {
                saveErrorInfo( e );
            }
        }

        // LogService.logIt( "ScoreManagerBean.scoreBatchOfSurveyEvents()  FINISHED. " + count + " Survey Events scored." );

        return count;
    }



    public boolean recalculateTestKeyBatteryScore( long testKeyId ) throws Exception
    {
        try
        {
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(testKeyId, true );

            if( tk.getBatteryId()<= 0 )
                return false;

            tk.setBattery( eventFacade.getBattery( tk.getBatteryId() ));

            // do battery score
            if( tk.getBattery() == null || !tk.getBattery().getBatteryScoreType().needsBatteryScoreObject())
                return false;

            if( tk.getTestEventList()==null )
                tk.setTestEventList(eventFacade.getTestEventsForTestKeyId(testKeyId, true));

            tk.setBatteryProduct( eventFacade.getProduct( tk.getProductId() ) );

            BatteryScore bs =  eventFacade.getBatteryScoreForTestKey( tk.getTestKeyId() );

            bs = tk.getBattery().scoreTestBattery( tk.getTestEventList(), bs );

            bs.setTestKeyId( tk.getTestKeyId() );
            bs.setStartDate( tk.getFirstAccessDate());
            bs.setLastAccessDate(tk.getLastAccessDate());
            bs.setUserId(tk.getUserId());
            bs.setBatteryId(tk.getBatteryId());
            bs.setOrgId(tk.getOrgId());
            bs.setSuborgId(tk.getSuborgId());

            if( normFacade == null )
                normFacade = NormFacade.getInstance();

            float[] norm = tk.getBattery().getBatteryScoreType().needsScore() ?  normFacade.getBatteryPercentile(tk.getBattery(), bs, tk.getOrgId(), null ) : new float[2];

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

            return true;
        }
        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ScoreManager.recalculateTestKeyBatteryScore() " + testKeyId );

            throw new STException( e );
        }
    }


    
    

    public void scoreSurveyEvent( SurveyEvent se ) throws Exception
    {
       try
       {
            // LogService.logIt( "ScoreManager.scoreSurveyEvent() starting process. " + se.toString() );

            if( se.getSurveyEventStatusTypeId() != TestEventStatusType.COMPLETED.getTestEventStatusTypeId() )
                throw new Exception( "SurveyEvent is not correct status type. Expecting completed. Found " + se.getSurveyEventStatusTypeId() );

                        
            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            Product p = eventFacade.getProduct(se.getProductId());
            
            if( p.getProductType().getIsCt5Direct() && (se.getResultXml()==null || se.getResultXml().isBlank()) )
            {
                CT5DirectTestEventScorer tesc = new CT5DirectTestEventScorer();
                tesc.setResultXmlInTestEvent(null, se, null, false);
            }
            
            TestEvent te = se.getEquivalentTestEvent();

            // get the product
            if( te.getProduct() == null )
                te.setProduct( p );

            
            
            if( te.getProduct() == null )
                throw new ScoringException( "Cannot find product for TestEvent productId=" + te.getProductId(), ScoringException.PERMANENT, te );

            if( te.getProduct().getSimDescriptorId() <= 0 )
                throw new ScoringException( "Cannot score because simDescriptorId is invalid ", ScoringException.PERMANENT, te );

            if( te.getOrg() == null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                te.setOrg( userFacade.getOrg( te.getOrgId() ));
            }

            if( te.getSuborgId()> 0 && te.getSuborg()==null )
            {
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();

                te.setSuborg( userFacade.getSuborg(te.getSuborgId()) );
            }

           // eventFacade.clearItemResponsesForSurveyEvent(se);

           // setIpLocationData( te );

            // get the SimDescriptor and it's object.
            te.setSimDescriptor( eventFacade.getSimDescriptor( te.getProduct().getSimDescriptorId() ) );
            // te.setSimDescriptor( eventFacade.getSimDescriptor( te.getProduct().getSimDescriptorId(), true ) );

            if( te.getSimDescriptor() == null )
                throw new ScoringException( "Cannot find SimDescriptor for SimDescriptorId=" + te.getProduct().getSimDescriptorId(), ScoringException.PERMANENT, te );

            if( te.getSimDescriptor().getXml() == null || te.getSimDescriptor().getXml().isEmpty() )
                throw new ScoringException( "SimDescriptor XML is empty. simDescriptorId=" + te.getProduct().getSimDescriptorId(), ScoringException.NON_PERMANENT, te );

            te.setSurveyEvent(se);
            
            StandardSurveyEventScorer ts = new StandardSurveyEventScorer();

            ts.scoreTestEvent(te, te.getSimDescriptor(), false );

            Tracker.addSurveyScore();
       }

       catch( ScoringException e )
       {
           LogService.logIt( "ScoreManager.scoreSurveyEvent() " + e.toString() + ", "  + se.toString() );
           throw e;
       }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt( e, "ScoreManager.scoreSurveyEvent() "  + se.toString() );

           throw new ScoringException( e.getMessage() + "ScoreManager.scoreSurveyEvent() " , ScoreUtils.getExceptionPermanancy(e) , se.getEquivalentTestEvent() );
       }
    }



    public boolean resetTestKeyStatusForScoring( TestKey tk, boolean fullReset, boolean clearExternal) throws Exception
    {
        try
        {
            // LogService.logIt( "ScoreManager.resetTestKeyStatus() Starting for TestKey=" + tk.getTestKeyId()  );

            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            boolean hasUnscored = false;

            BatteryScore bs;

            Battery b;

            if( tk.getTestEventList()==null )
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true ) );
            
            List<TestEvent> tel = tk.getTestEventList(); // eventFacade.getTestEventsForTestKeyId(tk.getTestKeyId(), true );

            tk.setTestEventList( tel );

            boolean hasPendingTestEvent = false;

            for( TestEvent te : tel )
            {
                // if not completed, reset TstKeyStatus to started and throw ScoringException;
                if( te.getTestEventStatusTypeId()<TestEventStatusType.COMPLETED.getTestEventStatusTypeId()  )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.STARTED.getTestKeyStatusTypeId() );
                    eventFacade.saveTestKey(tk);
                    throw new ScoringException( "TestEvent is not yet complete. " + te.toString(), ScoringException.NON_PERMANENT, te );
                }

                if( resetTestEventStatusForScoring(te, fullReset, clearExternal, false ) )
                    hasUnscored = true;

                if( te.getTestEventStatusTypeId()==TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
                    hasPendingTestEvent = true;
            }

            b = tk.getBatteryId()>0 ? eventFacade.getBattery( tk.getBatteryId() ) : null;

            bs = null;

            // Next check for a battery score, if needed.
            if( b != null && b.getBatteryScoreType().needsScore() )
            {
                bs = eventFacade.getBatteryScoreForTestKey( tk.getTestKeyId() );

                if( bs == null )
                    hasUnscored = true;
            }

            // set the status of this test key
            if( fullReset || hasUnscored )
            {
                tk.setTestKeyStatusTypeId( hasPendingTestEvent ?  TestKeyStatusType.COMPLETED_PENDING_EXTERNAL.getTestKeyStatusTypeId() : TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() );

                if( bs != null && bs.getBatteryScoreId()>0 )
                    eventFacade.deleteBatteryScore( bs );
            }

            else
                tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORED.getTestKeyStatusTypeId() );

            eventFacade.saveTestKey(tk);

            return hasUnscored;
        }

        catch( ScoringException e )
        {
            LogService.logIt(e, "ScoreManager.resetTestKeyStatusForScoring() "  + tk.toString() );
            throw e;
        }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt(e, "ScoreManager.resetTestKeyStatusForScoring() "  + tk.toString() );

           throw new ScoringException( e.getMessage() + "ScoreManager.resetTestKeyStatusForScoring() " , ScoreUtils.getExceptionPermanancy(e), tk );
       }
    }


    private boolean resetTestEventStatusForRecalcPercentiles( TestEvent te ) throws Exception
    {
        try
        {
            // if not completed, return false;
            if( te.getTestEventStatusTypeId() < TestEventStatusType.SCORED.getTestEventStatusTypeId()  )
                return false;

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            // LogService.logIt( "ScoreManager.resetTestEventStatusForRecalcPercentiles()  clearingAllTestEventScores " );

            eventFacade.saveTestEvent(te);

            te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ) );

            //for( TestEventScore tes : te.getTestEventScoreList() )
            //{
            //    if( tes.getTestEventScoreType().getIsReport() )
             //       eventFacade.deleteEntity(tes);
            //}

            //te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ) );

            return true;
        }

        catch( ScoringException e )
        {
            LogService.logIt( e, "ScoreManager.resetTestEventStatusForRecalcPercentiles() "  + te.toString() );
            throw e;
        }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt( e, "ScoreManager.resetTestEventStatusForRecalcPercentiles() "  + te.toString() );

           throw new ScoringException( e.getMessage() + "ScoreManager.resetTestEventStatusForRecalcPercentiles() " , ScoreUtils.getExceptionPermanancy(e), te );
       }
    }

    // returns true if this TestEvent can now be scored.
    private boolean resetTestEventStatusForScoring( TestEvent te, boolean fullReset, boolean clearExternal, boolean resetSpeechText) throws Exception
    {
        try
        {
            // if not completed, return false;
            if( te.getTestEventStatusTypeId()<TestEventStatusType.COMPLETED.getTestEventStatusTypeId()  )
                return false;

            // if this TestEvent was never started, continue
            if( te.getTestEventStatusTypeId() == TestEventStatusType.COMPLETED.getTestEventStatusTypeId() ||
                te.getTestEventStatusTypeId() == TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
                return true;

            
            
            // if not full reset, and already scored and not score error, return false
            if( !fullReset && te.getTestEventStatusTypeId()>=TestEventStatusType.SCORED.getTestEventStatusTypeId() && te.getTestEventStatusTypeId()!=TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId() )
                return false;

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            // LogService.logIt( "ScoreManager.resetTestEventStatusForScoring()  clearingAllTestEventScores " );

            List<TestEventScore> tesl = eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true );

            // OK we can score this test event
            if( te.getOverallScore()>0 || (tesl!=null && !tesl.isEmpty())  )
                TestEventLogUtils.createTestEventLogEntry(te.getTestEventId(),  "ScoreManager.resetTestEventStatusForScoring() Starting Rescore of TestEvent. OLD SCORE VALUES: " + getScoreStringForLogs( te, tesl ) );
            
            AvScoringUtils asu = null;
                        
            // if scoring was started on this TestEvent, clean it, set back to completed, and save.
            // NO LONGER DELETES TestEventScores
            if( clearExternal )
            {
                if( (te.getProductTypeId()==ProductType.FINDLY.getProductTypeId() ) && te.getResultXml()!=null && !te.getResultXml().isEmpty() )
                {
                    TestEventLogUtils.createTestEventLogEntry(te.getTestEventId(),  "ScoreManager.resetTestEventStatusForScoring() Clearing Findly Result Xml for rescore: " + te.getResultXml()  );
                    te.setResultXml(null);
                }
                
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
                                
                EssayFacade essayFacade=EssayFacade.getInstance();
                
                essayFacade.deleteUnscoredEssaysForTestEvent(te.getTestEventId());
                
                // if we still have a Ct5TestEvent clear the result XML and regenerate.
                if( te.getProductTypeId()==ProductType.CT5DIRECTTEST.getProductTypeId() && te.getResultXml()!=null && !te.getResultXml().isEmpty() )
                {
                    
                    // Make sure we have Ct5TestEvent still
                    Ct5EventFacade c5ef = Ct5EventFacade.getInstance();
                    Ct5TestEvent ct5Te = c5ef.getCt5TestEventForTestEventIdAndSurveyEventId(te.getTestEventId(), 0 );
                    List<Ct5ItemResponse> irl = ct5Te==null ? null : c5ef.getCt5ItemResponsesForCt5Test(ct5Te.getCt5TestEventId() );
                    
                    if( ct5Te!=null && irl!=null && !irl.isEmpty() )
                    {
                        te.setResultXml(null);
                        te.setResultXmlObj(null);
                        // eventFacade.saveTestEvent(te);
                    }
                    else
                        LogService.logIt("ScoreManager.resetTestEventStatusForScoring() Could not delete ResultXml for Ct5DirectTest because there are no Ct5TestEvent or Ct5ItemResponse objects available in DBMS. testEventId="  + te.getTestEventId() );
                }
                
                // Removed by Mike 5/28/2018
                // eventFacade.clearExternalScoresForEvent(te);
                asu = new AvScoringUtils();
                asu.resetAvEssayScoresForTestEvent( te.getTestEventId() );
            }
            
            if( resetSpeechText )
            {
                if( asu==null )
                    asu = new AvScoringUtils();
                asu.resetSpeechTextForTestEvent( te.getTestEventId() );
            }

            // now set to completed and save.
            te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED.getTestEventStatusTypeId() );

            eventFacade.saveTestEvent(te);

            //LogService.logIt( "ScoreManager.resetTestEventStatusForScoring() AFTER SAVE "  + te.toString() );

            //if( 1== 1 )
            //    throw new Exception( "FAKE EXCEPTION!" );

            //Thread.sleep( 10000 );

            //te = eventFacade.getTestEvent(te.getTestEventId() , false );

            //LogService.logIt( "ScoreManager.resetTestEventStatusForScoring() AFTER RE-RETRIEVE "  + te.toString() );

            return true;
        }

        catch( ScoringException e )
        {
            LogService.logIt(e, "ScoreManager.resetTestEventStatusForScoring() "  + te.toString() );
            throw e;
        }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt(e, "ScoreManager.resetTestEventStatusForScoring() "  + te.toString() );

           throw new ScoringException( e.getMessage() + "ScoreManager.resetTestEventStatusForScoring() " , ScoreUtils.getExceptionPermanancy(e), te );
       }

    }


    public int[] clearScoreErrorTestKeys() throws Exception
    {
        int[] out = new int[2];

        try
        {
            // LogService.logIt( "ScoreManager.clearScoreErrorTestKeys() Starting "  );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            List<TestKey> ptkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.SCORE_ERROR.getTestKeyStatusTypeId() , -1, true, MAX_SCORE_ERRORS, null );

            // LogService.logIt( "ScoreManager.clearScoreErrorTestKeys() Found  " + ptkl.size() + " keys to clear." );

            boolean hasUnscored;

            for( TestKey tk : ptkl )
            {
                hasUnscored = resetTestKeyStatusForScoring(tk, false, false );

                // increment partials
                if( hasUnscored )
                    out[0]++;

                // increment upgrades
                else
                    out[1]++;
            }

            
            if( out[0]>0 )
                LogService.logIt( "ScoreManager.clearScoreErrorTestKeys() cleaned " + out[0] + " partially completed TestKeys and upgraded " + out[1] + " completed testKeys to score complete status." );

            return out;

        }

        catch( Exception e )
        {
            LogService.logIt(e, "ScoreManager.clearScoreErrorTestKeys() " );
            throw new STException( e );
        }        
    }
    

    /*
        out[0]=Number cleared back to Completed.
        out[1]=Number advancd to 'scored'
        out[2]=number that could not be changed, possibly because a TestEvent is not yet completed.
    */
    public int[] clearPartiallyScoredTestKeys() throws Exception
    {
        int[] out = new int[3];

        try
        {
            // LogService.logIt( "ScoreManager.clearPartiallyScoredTestKeys() Starting "  );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            List<TestKey> ptkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId() , -1, true, -1, RuntimeConstants.getIntList("OrgIdsToSkip",",") );

            // LogService.logIt( "ScoreManager.clearPartiallyScoredTestKeys() Found  " + ptkl.size() + " keys to clear." );

            boolean hasUnscored;

            for( TestKey tk : ptkl )
            {
                if( !BaseScoreManager.isScoringFirstTimeOrRepeatAllowed( tk.getTestKeyId() ))
                {
                    LogService.logIt( "ScoreManager.clearPartiallyScoredTestKeys() Skipping test key because it's been active in scoring system too recently. testKeyId=" + tk.getTestKeyId() );
                    continue;
                }
                
                try
                {
                    hasUnscored = resetTestKeyStatusForScoring(tk, false, false );
                }
                catch( ScoringException e )
                {
                    LogService.logIt( "ScoreManager.clearPartiallyScoredTestKeys() unable to reset TestKeyStatus for scoringstarted testkey. Skipping. " + e.toString() + " testKeyId=" + tk.getTestKeyId() );
                    out[2]++;
                    continue;
                }

                // increment partials
                if( hasUnscored )
                    out[0]++;

                // increment upgrades
                else
                    out[1]++;
            }

            // LogService.logIt( "ScoreManager.clearPartiallyScoredTestKeys() cleaned " + out[0] + " partially completed TestKeys and upgraded " + out[1] + " completed testKeys to score complete status." );

            return out;

        }

        catch( Exception e )
        {
            LogService.logIt(e, "ScoreManager.clearPartiallyScoredTestKeys() " );

            throw new STException( e );
        }
    }



}
