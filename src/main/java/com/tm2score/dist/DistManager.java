/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.dist;

import com.tm2score.entity.event.TestKey;
import com.tm2score.event.*;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.report.ReportManager;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoringException;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.user.UserFacade;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.util.*;

/**
 *
 * @author Mike
 */
public class DistManager extends BaseDistManager
{
    public static boolean FIRST_BATCH = true;

    public static boolean OK_TO_START_ANY = true;

    public static boolean BATCH_IN_PROGRESS = false;



    public int[] doDistBatch(boolean withArchive, boolean noThread)
    {
        // LogService.logIt( "DistManager.doDistBatch() Starting BATCH_IN_PROGRESS=" + BATCH_IN_PROGRESS + ", ScoreManager.BATCH=" + ScoreManager.BATCH_IN_PROGRESS + ", ReportManager.BATCH=" + ReportManager.BATCH_IN_PROGRESS   );

        Tracker.addDistributionBatch();

        int[] count = new int[3];

        int errCount = 0;

        // Check for tests needing score
        long delayBetweenReports = 250;

        try
        {
            if( !OK_TO_START_ANY )
                return count;

            if( RuntimeConstants.getBooleanValue( "ForceNoThreadBasedBatchesInScoreReportDistrib") )
                noThread = true;

            if( noThread && ( ScoreManager.BATCH_IN_PROGRESS || ReportManager.BATCH_IN_PROGRESS || DistManager.BATCH_IN_PROGRESS ) )
                return count;
            // if( BATCH_IN_PROGRESS )
            //     return count;

            if( noThread )
                BATCH_IN_PROGRESS = true;

            int[] tkCount;

            if( userFacade == null )
                userFacade = UserFacade.getInstance();

            // LogService.logIt( "DistManager.doDistBatch()  Created userFacade" );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            if( FIRST_BATCH && RuntimeConstants.getBooleanValue("seekStartedDistribsFirstBatch") )
            {
                int[] vs = clearStartedDistribTestKeys();
                if( vs[0]>0 )
                    LogService.logIt( "DistManager.doDistBatch() Cleared " + vs[0] + " previously started test keys for retry." );
            }

            if( RuntimeConstants.getBooleanValue("retryErroredDistribsInBatch") )
            {
                int[] vs = clearDistribErrorTestKeys();
                if( vs[0]>0 )
                    LogService.logIt( "DistManager.doDistBatch() Cleared " + vs[0] + " Errored Dist test keys for retry." );
            }

            List<Integer> orgIdsToSkip = RuntimeConstants.getIntList("OrgIdsToSkip",",");
            
            List<TestKey> tkl = eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId(), 0, withArchive, -1, orgIdsToSkip );

            //if( FIRST_BATCH && RuntimeConstants.getBooleanValue("seekStartedDistribsFirstBatch") )
            //    tkl.addAll(eventFacade.getNextBatchOfTestKeysToScore(TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId(), -1, true, -1 ) );

            if( tkl.size() < Constants.DEFAULT_TESTKEY_BATCH_SIZE || withArchive || FIRST_BATCH )
                tkl.addAll(eventFacade.getNextBatchOfTestKeyArchivesToScore(TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId(), orgIdsToSkip, 0 ) );

            boolean useThread = !noThread;

            // for each testkey, get the test events
            for( TestKey tk : tkl )
            {
                // Skip dist of rescores.
                if( tk.getFirstDistComplete()==1 )
                {
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() );
                    tk.setErrorCnt(0);
                    eventFacade.saveTestKey(tk);
                    continue;
                }


                if( !BaseScoreManager.isScoringFirstTimeOrRepeatAllowed( tk.getTestKeyId() ) )
                {
                    LogService.logIt("DistManager.doDistBatch() Last Scoring Call was within min last date window. IGNORING Distribution for this Test Key for now. testKeyId=" + tk.getTestKeyId() );
                    continue;
                }

                BaseScoreManager.addTestKeyToDateMap( tk.getTestKeyId() );

                useThread = !noThread;

                if( useThread && ScoreManager.getTestKeysAndPartialEventsInScoringCount()>=ScoreManager.MAX_THREAD_TESTKEY_SCORE_COUNT )
                {
                    LogService.logIt( "DistManager.doDistBatch() Shifting to NoThread for testKeyId=" + tk.getTestKeyId() );
                    useThread=false;
                    delayBetweenReports = 1000;
                }

                try
                {

                    // refresh just in case a parallel process got it while waiting.
                    tk = eventFacade.getTestKey(tk.getTestKeyId(), true );

                    if( tk == null || !tk.getTestKeyStatusType().equals( TestKeyStatusType.REPORTS_COMPLETE ) )
                        continue;

                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId() );

                    eventFacade.saveTestKey(tk);

                    // LogService.logIt( "DistManager.doDistBatch() Distributing TK=" + tk.getTestKeyId()   );

                    if( !useThread )
                    {
                        tkCount = distributeTestKeyResults(tk, false, 0, 0, 0 );
                        count[0] += tkCount[0];
                        count[1] += tkCount[1];
                        count[2] += tkCount[2];
                        BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );
                    }
                    else
                    {
                        // LogService.logIt( "DistManager.doDistBatch() Starting Dist Thread tkid=" + tk.getTestKeyId() );
                        new Thread(new TestKeyDistThread( tk )).start();
                    }

                    Thread.sleep(delayBetweenReports);
                }
                catch( ScoringException e )
                {
                    errCount++;
                    if( tk!=null )
                        BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );
                    LogService.logIt( "DistManager.doDistBatch() XXX.1 " + e.toString() + " Error Distributing TestKey " + (tk==null ? "testkey is null" : " testKeyId=" + tk.getTestKeyId() ) + ", continuing batch. errCount=" + errCount  );
                }
                catch( Exception e )
                {
                    errCount++;
                    if( tk!=null )
                        BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );
                    LogService.logIt( e, "DistManager.doDistBatch() XXX.2 Error Distributing TestKey " + (tk==null ? "testkey is null" : " testKeyId=" + tk.getTestKeyId() ) + ", continuing batch. errCount=" + errCount  );
                }
            }

            // LogService.logIt( "DistManager.doDistBatch() Completed distribution batch. Sent " + count[0] + " test admin emails and " + count[1] + " text messages and " + count[2]  + " test-taker emails for " + tkl.size() + " test keys with " + errCount + " distribution errors." );
            FIRST_BATCH = false;

            if( noThread )
                BATCH_IN_PROGRESS = false;

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
            LogService.logIt(e, "DistManager.doDistBatch() " );
        }

        return count;
    }

    
    public int sendTestKeyResultsViaEmail( TestKey tk, String toEmails, String fromEmail, String fromName, String subj, String note, int addLimitedAccessLinkInfo ) throws Exception
    {
        try
        {
            // get test Key and init.
            // LogService.logIt( "DistManager.sendTestKeyResultsViaEmail() " + tk.toString() );

            if( tk == null )
                throw new Exception( "TestKey null" );

            initTestKey( tk );

            toEmails = toEmails.replaceAll( ";", "," );

            String[] emls = toEmails.split( "," );

            List<String> emailLst = new ArrayList<>();


            for( String eml : emls )
            {
                eml = eml.trim().toLowerCase();

                // skip dupes
                if( emailLst.contains(eml))
                    continue;

                if( !EmailUtils.validateEmailNoErrors( eml ) )
                    continue;

                emailLst.add(eml);
            }

            if( emailLst.isEmpty() )
            {
                LogService.logIt("DistManager.sendTestKeyResultsViaEmail() No valid emails found. Returning. testKeyId=" + tk.getTestKeyId() );
                throw new STException( "", "DistManager.sendTestKeyResultsViaEmail() No valid emails found." );
                // throw new Exception( "No valid emails found." );
            }

            Locale locale = getEmailLocaleFromTestKey( tk );

            if( locale == null )
                locale = Locale.US;

            String tnote = MessageFactory.getStringMessage(locale, note == null || note.isEmpty() ? "g.SendTestKeyResultsEmailNote" : "g.SendTestKeyResultsEmailWithCustNote" , new String[] {fromName, fromEmail, note} );

            String noteHtml = StringUtils.replaceStandardEntities(tnote);

            return emailTestResultsToAdministrator(tk, emailLst, subj, noteHtml, addLimitedAccessLinkInfo );
        }

        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "DistManager.sendTestKeyResultsViaEmail() testKeyId=" + tk.getTestKeyId() );
            throw new STException( e );
        }
    }















}
