package com.tm2score.service;

import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.score.ScoreManager;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Tracker
{
    private static int errors = 0;

    private static Set<Long> pendingTestEventIds;

    private static Map<String,ResponseTimeTracker> responseTimeMap;

    private static Date lastReminderBatch = null;
    private static Date lastAutoScoreBatch = null;
    private static Date secondLastAutoScoreBatch = null;
    private static Date lastOrgAutoTestExpireBatch = null;
    private static Date lastSubscriptionExpireBatch = null;
    private static Date lastCreditsExpireBatch = null;
    
    private static int distributionErrors = 0;
    private static int reportErrors = 0;
    private static int scoreErrors = 0;
    private static int sampleReportErrors = 0;

    private static int webServiceErrors = 0;
    private static int totalSampleReports = 0;
    
    

    private static int startedScores = 0;
    private static int surveyScores = 0;
    private static int webDuplicateContentChecks = 0;

    private static int tkExpWarningBatches = 0;
    private static int reminderBatches = 0;
    private static int reminderEmails = 0;
    private static int reminderTexts = 0;
    //private static int scoresPendingOld = 0;
    // private static int scoresPending = 0;
    private static int finishedScores = 0;
    private static int startedReports = 0;
    private static int finishedReports = 0;
    private static int singleReports = 0;
    private static int candFbkReports = 0;
    private static int langEquivReports = 0;
    private static int startedDistributions = 0;
    private static int finishedDistributions = 0;
    private static int redistributions = 0;
    private static int scoreBatches = 0;
    private static int reportBatches = 0;
    private static int distributionBatches = 0;
    private static int emailsSent = 0;
    private static int textMessagesSent = 0;
    private static int emailsSentTestTaker = 0;
    private static int emailsSentToAdmin = 0;

    private static int testKeysArchived = 0;
    private static int testEventsArchived = 0;
    
    private static int textMessagesFailed = 0;

    private static int resultsPushAPICalls = 0;
    
    private static int googleCloudTransReqs = 0;
    private static int googleCloudSpeechReqsSync = 0;
    private static int googleCloudSpeechReqsAsync = 0;
    private static int googleCloudStorageReqs = 0;
    
    private static int avItemResponseCompletions = 0;
    private static int avItemResponseStarts = 0;
    private static int ivrScores = 0;
    
    

    public static void addResponseTime( String name, long amountMs )
    {
        if( name == null || name.isEmpty() )
            return;

        if( responseTimeMap == null )
            responseTimeMap = new HashMap<>();

        ResponseTimeTracker rtt = responseTimeMap.get(name);

        if( rtt == null )
        {
            rtt = new ResponseTimeTracker( name );
            responseTimeMap.put( name , rtt);
        }

        rtt.addValue(amountMs);
    }    

    
    
    public static void addAvItemResponseStart()
    {
        avItemResponseStarts++;
    }
    
    public static void addAvItemResponseCompletion()
    {
        avItemResponseCompletions++;
    }
    
    public static void addIvrScores()
    {
        ivrScores++;
    }
    
    public static void addGoogleCloudTranslateReq()
    {
        googleCloudTransReqs++;
    }
    
    public static void addGoogleCloudSpeechReqSync()
    {
        googleCloudSpeechReqsSync++;
    }

    public static void addGoogleCloudSpeechReqAsync()
    {
        googleCloudSpeechReqsAsync++;
    }
    
    
    public static void addGoogleCloudStorageReq()
    {
        googleCloudStorageReqs++;
    }
    
    
    
    public static void addTkExpWarningBatch()
    {
        tkExpWarningBatches++;
    }
    
    public static void addReminderBatch()
    {
        reminderBatches++;
    }

    public static void addReminderText()
    {
        reminderTexts++;
    }

    public static void addReminderEmail()
    {
        reminderEmails++;
    }




    
    public static void addLastAutoScoreBatch()
    {
        secondLastAutoScoreBatch = lastAutoScoreBatch;
        lastAutoScoreBatch = new Date();
    }

    public static void addLastReminderBatch()
    {
        lastReminderBatch = new Date();
    }
    public static void addLastOrgAutoTestExpireBatch()
    {
        lastOrgAutoTestExpireBatch = new Date();
    }
    
    public static void addLastSubscriptionExpireBatch()
    {
        lastSubscriptionExpireBatch = new Date();
    }

    public static void addLastCreditsExpireBatch()
    {
        lastCreditsExpireBatch = new Date();
    }

        
    public static void addWebDuplicateContentCheck()
    {
        webDuplicateContentChecks++;
    }


    public static void addEmailToAdmin()
    {
        emailsSentToAdmin++;
    }

    public static void addTestKeyArchive()
    {
        testKeysArchived++;
    }

    public static void addTestEventArchive()
    {
        testEventsArchived++;
    }


    public static void addSurveyScore()
    {
        surveyScores++;
    }

    public static void addDistributionError()
    {
        distributionErrors++;
    }

    public static void addRedistribution()
    {
        redistributions++;
    }

    
    
    public static void addWebServiceError()
    {
        webServiceErrors++;
    }
    public static void addSampleReport()
    {
        totalSampleReports++;
    }
    
    public static void addCandidateFeedbackReport()
    {
        candFbkReports++;        
    }
    
    public static void addSampleReportError()
    {
        sampleReportErrors++;
    }
    
    public static void addReportError()
    {
        reportErrors++;
    }

    public static void addScoreError()
    {
        scoreErrors++;
    }


    public static void addError()
    {
        errors++;
    }

    public static void addScoreStart()
    {
        startedScores++;
    }

    public static void addScoreFinish()
    {
        finishedScores++;
    }

    public static void addScorePending( long teId )
    {
        // scoresPending++;
        if( pendingTestEventIds==null )
            pendingTestEventIds = new HashSet<>();
            
        pendingTestEventIds.add( teId );
    }

    public static void removeScorePending( long teId )
    {
        if( pendingTestEventIds==null )
            pendingTestEventIds = new HashSet<>();
            
        pendingTestEventIds.remove( teId );
    }
    
    
    //public static void addScorePendingOld()
    //{
    //    scoresPendingOld++;
    //}

    public static void addSingleReport()
    {
        singleReports++;
    }

    public static void addLanguageEquivalentReport()
    {
        langEquivReports++;
    }

    

    public static void addReportStart()
    {
        startedReports++;
    }

    public static void addReportFinish()
    {
        finishedReports++;
    }
    public static void addDistributionStart()
    {
        startedDistributions++;
    }

    public static void addDistributionFinish()
    {
        finishedDistributions++;
    }

    public static void addScoreBatch()
    {
        scoreBatches++;
    }

    public static void addReportBatch()
    {
        reportBatches++;
    }

    public static void addDistributionBatch()
    {
        distributionBatches++;
    }

    public static void addEmailSent()
    {
        emailsSent++;
    }

    public static void addEmailSentTestTaker()
    {
        emailsSentTestTaker++;
    }

    public static void addTextMessageSent()
    {
        textMessagesSent++;
    }

    public static void addTextMessageFailure()
    {
        textMessagesFailed++;
    }
    
    
    public static void addResultsPushAPICalls()
    {
        resultsPushAPICalls++;
    }




    public static List<String[]> getStatusList()
    {
        if( pendingTestEventIds==null )
            pendingTestEventIds = new HashSet<>();
                
        List<String[]> ot = new ArrayList<>();

        ot.add( new String[] {"TestKey Event Selection Type", RuntimeConstants.getTestKeyEventSelectionType().getName() } );

        
        ot.add( new String[] {"Score Batch: Last score batch", lastAutoScoreBatch==null ? "none" : I18nUtils.getFormattedDateTime(Locale.US, lastAutoScoreBatch, DateFormat.LONG, DateFormat.LONG, null) } );
        ot.add( new String[] {"Score Batch: Second-to-last score batch", secondLastAutoScoreBatch==null ? "none" : I18nUtils.getFormattedDateTime(Locale.US, secondLastAutoScoreBatch, DateFormat.LONG, DateFormat.LONG, null) } );
        ot.add( new String[] {"Reminder Batch: Last reminder batch", lastReminderBatch==null ? "none" : I18nUtils.getFormattedDateTime(Locale.US, lastReminderBatch, DateFormat.LONG, DateFormat.LONG, null) } );
        ot.add( new String[] {"Expire Batch: Last m-use link expire batch", lastOrgAutoTestExpireBatch==null ? "none" : I18nUtils.getFormattedDateTime(Locale.US, lastOrgAutoTestExpireBatch, DateFormat.LONG, DateFormat.LONG, null) } );
        ot.add( new String[] {"Expire Batch: Last subscription expire batch", lastSubscriptionExpireBatch==null ? "none" : I18nUtils.getFormattedDateTime(Locale.US, lastSubscriptionExpireBatch, DateFormat.LONG, DateFormat.LONG, null) } );
        ot.add( new String[] {"Expire Batch: Last credits expire batch", lastCreditsExpireBatch==null ? "none" : I18nUtils.getFormattedDateTime(Locale.US, lastCreditsExpireBatch, DateFormat.LONG, DateFormat.LONG, null) } );
        
        
        ot.add( new String[] {"Errors: ", Integer.toString( errors ) } );
        ot.add( new String[] {"Errors: Emails Sent to Admin: ", Integer.toString( emailsSentToAdmin ) } );

        ot.add( new String[] {"Distribution Errors: ", Integer.toString( distributionErrors ) } );
        ot.add( new String[] {"Report Gen Errors: ", Integer.toString( reportErrors ) } );
        ot.add( new String[] {"Scoring Errors: ", Integer.toString( scoreErrors ) } );

        ot.add( new String[] {"Web Service Errors: ", Integer.toString( webServiceErrors ) } );
        ot.add( new String[] {"Sample Report Errors: ", Integer.toString( sampleReportErrors ) } );
        ot.add( new String[] {"Total Sample Reports: ", Integer.toString( totalSampleReports ) } );
        
        ot.add( new String[] {"TestKey Exp Warning: Batches", Integer.toString( tkExpWarningBatches ) } );
        
        ot.add( new String[] {"Reminders: Batches", Integer.toString( reminderBatches ) } );
        ot.add( new String[] {"Reminders: Emails", Integer.toString( reminderEmails ) } );
        ot.add( new String[] {"Reminders: Texts", Integer.toString( reminderTexts ) } );

        ot.add( new String[] {"Scores: Batches", Integer.toString( scoreBatches ) } );
        ot.add( new String[] {"Scores: Test Events Started", Integer.toString( startedScores ) } );
        ot.add( new String[] {"Scores: Test Events Completed", Integer.toString( finishedScores ) } );
        //ot.add( new String[] {"Scores: Test Events Newly Pending", Integer.toString( scoresPendingNew ) } );
        ot.add( new String[] {"Scores: Test Events Pending External", Integer.toString( pendingTestEventIds.size() ) } );
        ot.add( new String[] {"Scores: Post-Test Surveys Scored", Integer.toString( surveyScores ) } );
        ot.add( new String[] {"Scores: Web Duplicate Content Checks", Integer.toString( webDuplicateContentChecks ) } );
        ot.add( new String[] {"Scores: Active Test Keys / Partial Events:", Integer.toString( ScoreManager.getTestKeysAndPartialEventsInScoringCount()) } );


        ot.add( new String[] {"Reports: Batches", Integer.toString( reportBatches ) } );
        ot.add( new String[] {"Reports: Test Events Started", Integer.toString( startedReports ) } );
        ot.add( new String[] {"Reports: Test Events Completed", Integer.toString( finishedReports ) } );
        ot.add( new String[] {"Reports: Total Reports Generated", Integer.toString( singleReports ) } );
        ot.add( new String[] {"Reports: Candidate Feedback Reports", Integer.toString( candFbkReports ) } );
        ot.add( new String[] {"Reports: Language Equivalent Reports", Integer.toString( langEquivReports ) } );
        

        ot.add( new String[] {"Distributions: Batches", Integer.toString( distributionBatches ) } );
        ot.add( new String[] {"Distributions: Test Keys Started", Integer.toString( startedDistributions ) } );
        ot.add( new String[] {"Distributions: Test Keys Completed", Integer.toString( finishedDistributions ) } );
        ot.add( new String[] {"Distributions: Test Keys Re-Distributed", Integer.toString( redistributions ) } );
        ot.add( new String[] {"Distributions: Emails Sent Employer", Integer.toString( emailsSent ) } );
        ot.add( new String[] {"Distributions: Text Messages Sent Employer", Integer.toString( textMessagesSent ) } );
        ot.add( new String[] {"Distributions: Text Messages Failed", Integer.toString( textMessagesFailed ) } );
        ot.add( new String[] {"Distributions: Emails Sent Test Taker", Integer.toString( emailsSentTestTaker ) } );
        ot.add( new String[] {"Distributions: Results Push POST Calls Sent", Integer.toString( resultsPushAPICalls ) } );

        ot.add( new String[] {"Google Cloud: Translation Requests", Integer.toString( googleCloudTransReqs ) } );
        ot.add( new String[] {"Google Cloud: Speech-2-Text Requests (Synch)", Integer.toString( googleCloudSpeechReqsSync ) } );
        ot.add( new String[] {"Google Cloud: Speech-2-Text Requests (ASynch)", Integer.toString( googleCloudSpeechReqsAsync ) } );
        ot.add( new String[] {"Google Cloud: Storage Save/Delete Requests", Integer.toString( googleCloudStorageReqs ) } );

        ot.add( new String[] {"IVR: Item Response Starts", Integer.toString( avItemResponseStarts ) } );
        ot.add( new String[] {"IVR: Item Response Completions", Integer.toString( avItemResponseCompletions ) } );
        ot.add( new String[] {"IVR: Ivr Test Scores", Integer.toString( ivrScores ) } );
        

        ot.add( new String[] {"Archive: Test Keys Archived", Integer.toString( testKeysArchived ) } );
        ot.add( new String[] {"Archive: Test Events Archived", Integer.toString( testEventsArchived ) } );

        
        return ot;
    }

    public static Map<String, Float> getStatusMap()
    {
        Map<String, Float> outMap = new TreeMap<>();

        if( responseTimeMap != null && responseTimeMap.size()>0 )
        {
            for( ResponseTimeTracker rtt : responseTimeMap.values() )
            {
                if( rtt.count==0 )
                    continue;

                outMap.put( "Avg Resp Tm " + rtt.name  + " (count=" + rtt.count + ")", new Float(rtt.getAverage())  );
            }
        }

        return outMap;
    }



}
