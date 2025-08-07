/*
 * Created on Dec 12, 2006
 *
 */
package com.tm2score.global;

import com.tm2score.score.TestKeyEventSelectionType;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

public class RuntimeConstants
{
    // private static char pathSeparator = ' ';

    private static Map<String, Object> cache = null;

    public static boolean DEBUG = false;

    public static TestKeyEventSelectionType TESTKEYEVENT_SELECTION_TYPE = TestKeyEventSelectionType.ALL;

    public static SecretKey sealedObjectSecretKey = null;


    /**
     * Init
     */
    static
    {
        TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );

        cache = new TreeMap<>();

        cache.put( "services/email/mailon", true);

        cache.put( "services/log/logfilepattern", "/work/tm2score5/log/tm2score_%g_%u.log");

        // cache.put(  "newSupportRequestNotifyEmail", "mike@hravatar.com" );

        cache.put( "propertiesFile", "/work/tm2score5/zzapplication.conf" );
        cache.put( "secretsFile", "/work/hraconfig/hraglobals-discern.conf" );

        cache.put( "filesroot", "/work/tm2score5/files" );
        cache.put( "fileTreeRootDirectory" , "/work/sm1/web" );

        
        cache.put( "adminappbasuri", "https://www.hravatar.com/ta" );

        cache.put( "baseadmindomain", "www.hravatar.com" );
        cache.put( "testingappbasedomain", "test.hravatar.com" );

        cache.put( "testingappprotocol", "https" );

        cache.put( "default-site-name", "HR Avatar" );
        cache.put( "default-site-name-cap", "HR Avatar" );
        cache.put( "no-reply-email", "no-reply@hravatar.com" );        
        cache.put( "support-email", "support@hravatar.com" );        
        cache.put( "system-admin-email", "mike@hravatar.com" );        
        

        cache.put( "testingappcontextroot", "tt" );

        cache.put( "baseprotocol", "https" );
        cache.put( "httpsONLY", true );

        cache.put( "baseurl", "https://ts.hravatar.com/ts" );

        cache.put( "baselogourl", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_8x1715795136855.png" );
        cache.put( "baseiconurl", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_21x1717875839610.png" );
        cache.put( "default-email-logo", "https://cdn.hravatar.com/web/orgimage/zrWvh1uNWrg-/img_4x1715795136829.png");
        
        
        cache.put( "defaultskinid", (int)( 1 ) );

        cache.put( "defaultcorpid", (int)( 6 ) );

        cache.put( "defaultorgid", (int)( 1 ) );

        cache.put( "defaultsuborgid", (int)( 0 ) );

        cache.put( "pinTestProductId", (int)( 1 ) );

        cache.put( "userFileUploadBaseDir", "/hra" );

        // cache.put( "uploadedUserFileBaseUrl", "http://ful-hravatar-com/hra" );
        cache.put( "awsS3BaseUrl", "https://s3.amazonaws.com/" );

        cache.put( "uploadedUserFileBaseUrl", "https://s3.amazonaws.com/ful-hravatar-com/hra" );
        cache.put( "uploadedUserFileBaseUrlHttps", "https://s3.amazonaws.com/ful-hravatar-com/hra" );

        cache.put( "tempVoiceFilesBaseUrl", "https://s3.amazonaws.com/ful-hravatar-com/temporaryvoicefiles" );

        cache.put( "localFsRoot", "/work/sm1/web" );


        cache.put( "mediaServerWebapp", "sm" );

        cache.put( "mediaServerDomain", "media.clicflic.com" );

        cache.put( "mediaServerPort", (int)( 80 ) );

        cache.put( "applicationSystemId", (int)( 401 ) );

        cache.put( "stringEncryptorKey",  "" );
        cache.put( "stringEncryptorKeyFileSafe",  "" );
        
        
        
        cache.put( "autoScoreOk", true);
        cache.put( "autoRemindersOk", true);
        cache.put( "autoInvitationsOk", true );
        cache.put( "autoTkExpWarningsOk", true );
        cache.put( "autoOrgAutoTestExpirationOk", true );
        cache.put( "autoOrgSubscriptionExpirationOk", true );
        cache.put( "autoOrgCreditsExpirationOk", true );
        
        cache.put( "scoreDebugMode", false );
        cache.put( "reportDebugMode", false );

        cache.put( "webServicesAuthId", "" );
        cache.put( "webServicesAuthCode", "" );

        cache.put( "colorDotsFolder", "misc/images/hravatar" );

        cache.put( "disableCertificateVerification", false );


        cache.put( "Orphan_RemovedFmBattery_TestKeyId",  (long)( 19406 ) );

        cache.put( "translogoimageurl" , "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_1429216573950.png" );
        cache.put( "hraCompanyLogoSmall", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_1416868391352.png" );
        cache.put( "ivrCustomTestAudioPlayIconUrl", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_3x1517685008793.png" );
        cache.put( "ivrCustomTestAudioPlayIconUrlEmail", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_3x1517685008793.png" );        // cache.put( "ivrCustomTestAudioPlayIconUrlEmail", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_2x1504516866957.png" );
        cache.put( "avCustomTestVideoPlayIconUrl", "https://s3.amazonaws.com/cfmedia-hravatar-com/web/orgimage/zrWvh1uNWrg-/img_2x1517685008793.png" );

        cache.put("hraLogoBlackTextFilename","hra-two-color-tagline-logo-trans-800.png");
        cache.put("hraLogoBlackTextPurpleFilename","hra-two-color-tagline-logo-trans-800.png"); 

        cache.put("hraLogoWhiteTextFilename","hra-white-tagline-logo-trans-800.png"); 
        cache.put("hraLogoWhiteTextPurpleFilename","hra-white-tagline-logo-trans-800.png"); 

        cache.put("hraLogoBlackTextSmallFilename","hra-two-color-tagline-logo-trans-420.png"); 
        cache.put("hraLogoBlackTextSmallPurpleFilename","hra-two-color-tagline-logo-trans-420.png"); 

        cache.put("hraLogoWhiteTextSmallFilename","hra-white-tagline-logo-trans-412.png");      
        cache.put("hraLogoWhiteTextSmallPurpleFilename","hra-white-tagline-logo-trans-412.png"); 

        cache.put( "bell-rainbow-base-graphic-url" , "https://cfmedia-hravatar-com.s3.us-east-1.amazonaws.com/web/misc/report/rainbow-bell-228x18.png" );
        cache.put( "bell-rainbow-base-graphic-url-v2" , "https://cfmedia-hravatar-com.s3.us-east-1.amazonaws.com/web/misc/report/rainbow-bell-228x18-v2.png" );

        cache.put( "bell-rainbow-base-downarrow-url" , "https://cfmedia-hravatar-com.s3.us-east-1.amazonaws.com/web/misc/report/pointer-down-6x12.png" );
        
        cache.put( "hraCoverPageFilename", "https://cfmedia-hravatar-com.s3.us-east-1.amazonaws.com/web/misc/report/cover-bg-2.png" );
        cache.put( "hraCoverIncludedArrowFilename", "https://cfmedia-hravatar-com.s3.us-east-1.amazonaws.com/web/misc/report/cover-blue-arrow-solid.png");
        
        cache.put("rainbowBarFilename","rainbowbar_new.png");
        
        cache.put( "testingapphttpsOK", true );

        cache.put( "defaultMarketingAccountOrgId",  (int)( 24 ) );
        cache.put( "defaultMarketingAccountSuborgId",  (int)( 0 ) );

        cache.put( "testkeyeventselectiontypeid", (int)(0) );

        cache.put( "seekStartedScoresFirstBatch", true );
        cache.put( "retryErroredScoresInBatch", true );

        cache.put( "seekStartedReportsFirstBatch", true );
        cache.put( "retryErroredReportsInBatch", true );
        cache.put( "seekStartedDistribsFirstBatch", true );
        cache.put( "retryErroredDistribsInBatch", true );

        cache.put( "ForceNoThreadBasedBatchesInScoreReportDistrib", false );

        
        cache.put("create_reports_init_as_archived", false );

        //////////////////////////////////////////////////////////////////////////////
        // TM2 SCORE API
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "tm2score_rest_api_username", "" );
        cache.put( "tm2score_rest_api_password", "" );
        
        
        //////////////////////////////////////////////////////////////////////////////
        // Pay-Per-Result Params
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "LowARThreshold",  5 );
        
        // ////////////////////////////////////////////////////////////////////////////
        // Org-specific CS Email addresses. Comma-separated.
        // ////////////////////////////////////////////////////////////////////////////
        cache.put( "AdditionalAffiliateSourceEmails_OrgId_107", "support@aseametrics.com" );
        
        
        //////////////////////////////////////////////////////////////////////////////
        // VOLUME Testing
        //////////////////////////////////////////////////////////////////////////////
        cache.put( "OrgIdsToSkip", null );


        //////////////////////////////////////////////////////////////////////////////
        // Voice
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "stdVoiceReportIds", "45" );  // ; delimited

        cache.put( "isLocalHostForTranscription", false );


        //////////////////////////////////////////////////////////////////////////////
        // Candidate Feedback Report
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "feedbackReportId", (long)(53) );

        cache.put( "allTestTakerDevReports", "33,34,35,36,40,41,42,53,116,118,121");
        

        //////////////////////////////////////////////////////////////////////////////
        // Best Jobs / Career Scout
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "BestJobsReportId", (int)(98) );

        cache.put( "jobMatchValidReportIds", "41;42;80;98" );  // ; delimited

        cache.put( "Hra_Interest_Inventory_ProductIds_ALL",  "1451,1674,3500,4700,4762,5532" );

        //cache.put( "Hra_Interest_Inventory_ProductId_en_US",  (int)( 1674 ) );
        //cache.put( "Hra_Interest_Inventory_ProductId_en",  (int)( 1674 ) );

        cache.put( "Hra_OptionalTest_ScoreDelay_Minutes",  (int)( 60 ) );
        cache.put( "BestJobsReport_MaxSearchGap",  (int)( 25 ) );

        cache.put( "CareerScoutReportId", (int)(98) );

        cache.put( "forwardNewCsRequestsAddressJob", "mike@hravatar.com,sandy@hravatar.com" );

        cache.put( "lowCreditsBccEmails", "mike@hravatar.com,sandy@hravatar.com" );
        
        //////////////////////////////////////////////////////////////////////////////
        // AWS Params
        //////////////////////////////////////////////////////////////////////////////


        cache.put( "useAwsMediaServer", true);
        cache.put( "awsBaseUrl", "https://s3.amazonaws.com/cfmedia-hravatar-com/web" );

        // Note: This is the Amazon user S3AndAIAdministrator who only has access to S3 and various AI tools like Polly and Translate and Rekognition
        cache.put( "awsAccessKey", "" );
        cache.put( "awsSecretKey", "" );
        cache.put( "awsRekognitionRegionId", (Integer)1 );

        cache.put( "awsBucket", "cfmedia-hravatar-com" );
        cache.put( "awsBucketLvRecording", "lv-hravatar-com" );
        cache.put( "awsBucketFileUpload", "ful-hravatar-com" );
        cache.put( "awsBucketProctorRecording", "rp-hravatar-com" );

        cache.put( "awsBucketRefRecording", "ref-hravatar-com" );
        cache.put( "awsBaseKeyRefRecording", "refrecordings/" );
        cache.put( "awsBaseKeyRefRecordingTest", "refrecordingstest/" );
        cache.put( "awsBucketRegionIdRefRecording", (Integer)1 );

        cache.put( "awsBucketOvProRecording", "ov.hravatar.com" );
        cache.put( "awsBaseKeyOvProRecording", "recordings/" );
        cache.put( "awsBucketRegionIdOvProRecording", (Integer)1 );

        cache.put( "awsBaseKey", "web/" );
        cache.put( "awsBaseKeyFileUpload", "hra/" );
        cache.put( "awsBaseKeyLvRecording", "recordings/" );
        cache.put( "awsBaseKeyLvRecordingTest", "recordingstest/" );
        cache.put( "awsBaseKeyProctorRecording", "proctorrecordings/" );
        cache.put( "awsBaseKeyProctorRecordingTest", "proctorrecordingstest/" );


        cache.put( "awsBucketRegionId", (Integer)1 );
        cache.put( "awsBucketRegionIdFileUpload", (Integer)1 );
        cache.put( "awsBucketRegionIdProctorRecording", (Integer)1 );
        cache.put( "awsBucketRegionIdLvRecording", (Integer)1 );


        cache.put( "awsAccessKeyTranscribe", "" );
        cache.put( "awsSecretKeyTranscribe", "VTDHUQ/" );
        cache.put("UseAmazonTranscribe", true);

        cache.put( "awsAccessKeyTranslate", "" );
        cache.put( "awsSecretKeyTranslate", "KSXXBx/" );

        cache.put( "aws.voicestoragebucketdirectory", "temporaryvoicefiles" );

        cache.put( "useAwsTempUrlsForMedia", true );
        cache.put( "awsTempUrlMinutes", ((Integer)15));
        cache.put( "mediaTempUrlSourcePath", "/suri" );

        //////////////////////////////////////////////////////////////////////////////
        // SAMPLE REPORT sample report Params
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "samplereportorgid", (int)( 14752 ) );
        cache.put( "samplereportuserid", (long)( 1835355 ) );
        cache.put( "samplereportauthuserid", (long)( 1835353 ) );

        cache.put( "samplereportriasecinfo", "[RIASECINFO]1,1124,Museum+Technicians+and+Conservators,53.503788,66.033936,96.6,0.0,25-4013.00|2,976,Nanosystems+Engineers,52.03587,63.545647,96.0,0.0,17-2199.09|3,1022,Remote+Sensing+Scientists+and+Technologists,51.815666,64.46267,92.0,0.0,19-2099.01|4,960,Human+Factors+Engineers+and+Ergonomists,51.156998,61.937305,96.0,0.0,17-2112.01|5,973,Microsystems+Engineers,51.063164,62.293594,94.4,0.0,17-2199.06|6,915,Computer+Network+Support+Specialists,50.971107,66.48113,81.2,0.0,15-1231.00|7,997,Soil+and+Plant+Scientists,50.76175,61.016003,96.6,0.0,19-1013.00|8,1021,Hydrologists,50.657932,62.872017,90.4,0.0,19-2043.00|9,1004,Biologists,50.420216,60.192997,97.2,0.0,19-1029.04|10,1018,Environmental+Restoration+Planners,50.408733,65.78199,80.2,0.0,19-2041.02|11,1134,Commercial+and+Industrial+Designers,50.244965,66.07629,78.4,0.0,27-1021.00|12,967,Automotive+Engineers,50.235115,65.86026,79.0,0.0,17-2141.02|13,1020,Geoscientists%2C+Except+Hydrologists+and+Geographers,50.222523,61.67922,91.6,0.0,19-2042.00|14,1216,Acupuncturists,49.911545,59.856125,95.4,0.0,29-1291.00|15,1008,Foresters,49.893192,65.036545,79.6,0.0,19-1032.00[EEOCATINFO]1,Laborers+and+Helpers,9,46.849396|2,Operatives,8,44.542297|3,Technicians,4,44.48263|4,Craft+Workers,7,44.295315|5,Professionals,3,43.310863|6,Sales+Workers,5,42.831757|7,First%2FMid+Level+Officials+and+Managers,2,42.628986|8,Service+Workers,10,42.428875|9,Executive%2FSenior+Level+Officials+and+Managers,1,41.978943|10,Administrative+Support+Workers,6,40.037876");
        

        cache.put( "SampleReportImgCaptureTestEventId", (long)(926457) );  // testkeyid=1039215, userid=766420 USED in tm2score 10-20-2023

        cache.put( "SampleReportVideoCaptureTestEventScoreId", (long)(28225786) ); // New testeventid=1931610, testkeyid=2027100 testeventscoreid=28225786 userid=1544068   // testeventid=289220  userid=188971   old=3700570  // USED in tm2score 10-20-2023
        
        cache.put( "SampleReportAudioOnlyCaptureTestEventScoreId", (long)(13380350) );  // testeventid=844245, testkeyid=945938   userid=490537  USED in tm2score 10-20-2023


        cache.put( "SampleReportChatTestEventScoreId", (long) (25161845) ); // testeventid=1690110 userid=1383253, testkeyid=1837800   0ld=25161845 (6534088) );  // USED in tm2score 10-20-2023

        //////////////////////////////////////////////////////////////////////////////
        // CT3 Params
        //////////////////////////////////////////////////////////////////////////////

        // This was set to provide a cutoff for sims scored with the new score method. Dec 15th. 2014
        cache.put( "minCt2SimTestEventIdForNorms", (int)( 3168 ) );

        cache.put( "defaultMarketingAccountOrgId",  (int)( 24 ) );
        cache.put( "defaultMarketingAccountSuborgId",  (int)( 0 ) );
        cache.put( "defaultMarketingAccountAnonymousUserId",  (long)( 167 ) );

        cache.put( "ct2ValidReportIds", "13;14;15;16;17;18;19;25;24;28,101,68,91,85" );

        cache.put( "avValidReportIds", "50,69,87,90,104" );

        cache.put( "uminnValidReportIds", "40" );

        cache.put( "careerScoutProductIds", "1492;1493;1464;1768;1770;1854" );

        cache.put( "sportsValidReportIds", "66" );

        //////////////////////////////////////////////////////////////////////////////
        // Google Cloud API Params
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "useGoogleTranslate", true );

        cache.put( "gcloudspeech.HRAVoice.APIKey", "" );
        cache.put( "gcloudspeech.baseUrl", "https://speech.googleapis.com" );
        cache.put( "gcloudspeech.recognizeEndpoint", "/v1/speech:recognize" );
        cache.put( "gcloudspeech.longRecognizeEndpoint", "/v1/speech:longrunningrecognize" );
        cache.put( "gcloudspeech.longRecognizeResultsEndpoint", "/v1/operations/" );   // /v1/operations/{name}


        cache.put( "gcloud.projectname", "hravoice" );
        cache.put( "gcloud.projectid", "624337889158" );
        cache.put( "gcloud.voicestoragebucket", "temporaryvoicefiles" );


        cache.put( "gcloudtranslate.HRAVoice.APIKey", "" );
        cache.put( "gcloudtranslate.baseUrl", "https://translation.googleapis.com" );
        cache.put( "gcloudtranslate.translateEndpoint", "/language/translate/v2" );

        //////////////////////////////////////////////////////////////////////////////
        // IBM Cloud API Params
        //////////////////////////////////////////////////////////////////////////////

        // New 10-15-2020
        cache.put( "sentino.APIKey", "" );
        cache.put( "sentino.baseUrl", "https://api.sentino.org/api" );  // https://api.sentino.org/api/score/items
        cache.put( "sentino.endpoint", "/score/items" );


        // New 10-15-2020
        cache.put( "ibmcloudInsightOn", true );
        cache.put( "ibmcloudinsight.minWordsRequired", (int)(600) );
        cache.put( "ibmcloudinsight.reuseExistingResult", true );
        
        //////////////////////////////////////////////////////////////////////////////
        // Safe Exam Browser Locked SEB seb Seb
        //////////////////////////////////////////////////////////////////////////////

        // This is set to false because have been unable to do a web rtc connection within seb. Last Update 11/24/2021
        cache.put( "ppIsIosOkForSebWithVideo" , false );


        //////////////////////////////////////////////////////////////////////////////
        // Free Geo IP
        //////////////////////////////////////////////////////////////////////////////


        cache.put( "IpStackAccessKey", "" );
        cache.put( "FreeGeoIpURI", "https://api.ipstack.com/" );



        //////////////////////////////////////////////////////////////////////////////
        // Twilio Params
        //////////////////////////////////////////////////////////////////////////////

        cache.put( "sampleAudioIvrItemResponseId", (long)(85) );


        cache.put( "twilio.textingon", true );
        cache.put( "twilio.sid", "" );
        cache.put( "twilio.auhtoken", "" );
        cache.put( "twilio.fromnumber", "+17036353077" );
        cache.put( "twilio.msgstatuscallbackurl", "https://sim.hravatar.com/tb/msgwh/" );

        cache.put( "twilio.sandboxpin", "7760-3166" );

        cache.put( "twilio.useSandbox", false );
        cache.put( "twilio.sandboxphonenumber", "(415) 599-2671" );

        //////////////////////////////////////////////////////////////////////////////
        // VoiceVibes Params
        //////////////////////////////////////////////////////////////////////////////

        cache.put("voiceVibes.VibesOn", true);
        cache.put("voiceVibes.ForceDemo", false);

        cache.put( "voiceVibes.AccountId", "" );
        cache.put( "voiceVibes.ApiKey", "" );

        cache.put( "voiceVibes.AccountIdDemo", "" );
        cache.put( "voiceVibes.ApiKeyDemo", "" );

        cache.put( "voiceVibes.AccountIdHV", "" );
        cache.put( "voiceVibes.ApiKeyHV", "" );


        cache.put( "voiceVibes.BaseUrl", "https://api.voicevibes.io" );
        cache.put( "voiceVibes.VersionPath", "/v1" );

        cache.put( "voiceVibes.forceBase64", false );

        // Docs at:
        // https://voicevibes.s3.amazonaws.com/api-docs/public.html


        // ////////////////////////////////////////////////////////////////////////////
        // Discern (Essay Scoring)
        // ////////////////////////////////////////////////////////////////////////////
        // cache.put( "discernOn", false );
        

        // ////////////////////////////////////////////////////////////////////////////
        // PlagTracker
        // ////////////////////////////////////////////////////////////////////////////

        cache.put( "copyscape_username", "" );
        cache.put( "copyscape_apikey", "" );
        cache.put( "copyscape_api_domain", "https://www.copyscape.com/api/" );


        // ////////////////////////////////////////////////////////////////////////////
        // Findly Tests
        // ////////////////////////////////////////////////////////////////////////////

        // cache.put( "Findly-Main-Account-AccountId", "HR-Avatar2" );
        cache.put( "Findly-Main-Account-AccountId", "" );
        cache.put( "Findly-Main-Account-Username", "" );
        cache.put( "Findly-Main-Account-Password", "" );
        cache.put( "Findly-Main-Account-Identifier", "findly-main" );

        // cache.put( "Findly-Intl-Account-AccountId", "HR-Avatar-Global2" );
        cache.put( "Findly-Intl-Account-AccountId", "" );
        cache.put( "Findly-Intl-Account-Username", "" );
        cache.put( "Findly-Intl-Account-Password", "" );
        cache.put( "Findly-Intl-Account-Identifier", "findly-intl" );


        cache.put( "Findly-Demo-Account-AccountId", "" );
        cache.put( "Findly-Demo-Account-Username", "" );
        cache.put( "Findly-Demo-Account-Password", "" );
        cache.put( "Findly-Demo-Account-Identifier", "findly-demo" );


        // ////////////////////////////////////////////////////////////////////////////
        // HRA Standard Z Scoring
        // ////////////////////////////////////////////////////////////////////////////
        cache.put( "TgtSimCompetencyScaledScoreMean", (float)( 65 ) );
        cache.put( "TgtSimCompetencyScaledScoreStdev", (float)( 15 ) );

        cache.put( "TgtSimScaledScoreMeanSCORE2", (float)( 65 ) );
        cache.put( "TgtSimScaledScoreStdevSCORE2", (float)( 15 ) );

        cache.put( "UseScore2ForPercentiles", (true) );

        cache.put( "SimRawScoreZConversionMeanDefault", (float)(0) );
        cache.put( "SimRawScoreZConversionStdevDefault", (float)(1) );


        // ////////////////////////////////////////////////////////////////////////////
        // Hubspot hubspot hubSpot HubSpot
        // ////////////////////////////////////////////////////////////////////////////

        cache.put( "hubspot_api_on", true );
        cache.put( "hubspot_private_app_access_token", "" );
        cache.put( "hubspotApiKey", "" );
        cache.put( "hubspot_api_base_url", "https://api.hubapi.com" );
        cache.put( "hubspot_api_base_contacts_path", "/crm/v3/objects/contacts" );
        cache.put( "hubspot_api_base_companies_path", "/crm/v3/objects/companies" );
        cache.put( "hubspot_api_base_deals_path", "/crm/v3/objects/deals" );
        cache.put( "hubspot_api_base_tickets_path", "/crm/v3/objects/tickets" );
        cache.put( "hubspot_default_deal_owner_email", "sandy@hravatar.com");
        cache.put( "hubspot_default_deal_owner_id", "175829599");
        cache.put( "hubspot_default_ticket_owner_id", "175829599");


        cache.put( "hubspot_min_orgid_for_update", 50 );
        cache.put( "hubspot_include_hra_ids", true );


        // ////////////////////////////////////////////////////////////////////////////
        // HRA IFrame Tests
        // ////////////////////////////////////////////////////////////////////////////

        cache.put( "autoIframeCompletionOk", true );


        ///////////////////////////////////////////////////////////////////////////
        // PREMIUM REMOTE PROCTORING
        ///////////////////////////////////////////////////////////////////////////

        cache.put( "useTestFoldersForProctorRecordings", false );
        cache.put( "useAwsForProctorRecording", true );

        cache.put( "BasicProctoringIsExternal", true );

        ///////////////////////////////////////////////////////////////////////////
        // Live Video Interviewing
        ///////////////////////////////////////////////////////////////////////////

        cache.put( "useTestFoldersForAwsLvFiles" , false );

        
        
        ///////////////////////////////////////////////////////////////////////////
        // Resume Func
        ///////////////////////////////////////////////////////////////////////////
        
        cache.put( "resumesimid", 18110l );
        cache.put( "resumesimversionid", 1 );
        
        
        cache.put( "tm2ai_rest_api_ok", true );
        cache.put( "tm2ai_evalplan_scoring_ok", true ); 
        cache.put( "ai-essay-scoring-ok", true );
        cache.put( "ai-essay-scoring-use-score2", false );

        
        cache.put( "tm2ai_rest_api_baseuri", "https://ts.hravatar.com/ai/webresources/");
        cache.put( "tm2ai_rest_api_username", "" );
        cache.put( "tm2ai_rest_api_password", "" );
        

        // load properties from file. File overlays everything.
        String propertiesFile = (String) cache.get( "secretsFile" );
        if( propertiesFile != null && !propertiesFile.isBlank() )
            loadProperties( propertiesFile );

        propertiesFile = (String) cache.get( "propertiesFile" );
        if( propertiesFile != null && !propertiesFile.isBlank() )
            loadProperties( propertiesFile );

        TESTKEYEVENT_SELECTION_TYPE = TestKeyEventSelectionType.getValue( (Integer) cache.get("testkeyeventselectiontypeid") );

        convertSecretsToSealedObjects();                
    }
    
    private static synchronized void convertSecretsToSealedObjects()
    {
        if( sealedObjectSecretKey!=null )
            return;
        
        try
        {
            sealedObjectSecretKey = KeyGenerator.getInstance("DES").generateKey();
            Cipher ecipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, sealedObjectSecretKey );

            substituteStringWithSealedObject( "secretsFile", ecipher );
            substituteStringWithSealedObject( "stringEncryptorKey", ecipher );
            substituteStringWithSealedObject( "stringEncryptorKeyFileSafe", ecipher );
            substituteStringWithSealedObject( "sentino.APIKey", ecipher );
            substituteStringWithSealedObject( "awsSecretKey", ecipher );
            substituteStringWithSealedObject( "awsSecretKeyTranscribe", ecipher );
            substituteStringWithSealedObject( "awsSecretKeyTranslate", ecipher );
            substituteStringWithSealedObject( "gcloudspeech.HRAVoice.APIKey", ecipher );
            substituteStringWithSealedObject( "gcloudtranslate.HRAVoice.APIKey", ecipher );
            substituteStringWithSealedObject( "IpStackAccessKey", ecipher );
            substituteStringWithSealedObject( "twilio.auhtoken", ecipher );
            substituteStringWithSealedObject( "voiceVibes.ApiKey", ecipher );
            substituteStringWithSealedObject( "voiceVibes.ApiKeyDemo", ecipher );
            substituteStringWithSealedObject( "voiceVibes.ApiKeyHV", ecipher );
            substituteStringWithSealedObject( "copyscape_apikey", ecipher );
            substituteStringWithSealedObject( "Findly-Main-Account-Password", ecipher );
            substituteStringWithSealedObject( "Findly-Intl-Account-Password", ecipher );
            substituteStringWithSealedObject( "Findly-Demo-Account-Password", ecipher );
            substituteStringWithSealedObject( "hubspot_private_app_access_token", ecipher );
            substituteStringWithSealedObject( "hubspotApiKey", ecipher );
            substituteStringWithSealedObject( "tm2ai_rest_api_password", ecipher );

        }
        catch( Exception e )
        {
            LogService.logIt( e, "RuntimeConstants.convertSecretsToSealedObjects()" );
        }
    }
    
    private static String getStringValueFromSealedObject( String cacheKey, SealedObject so )
    {
        if( cacheKey==null || cacheKey.isBlank() )
            return null;
        
        try
        {
            if( so==null )
            {
                Object o = cache.get(cacheKey);
                if( o ==null )
                    return null;
                else if( o instanceof String )
                    return (String)o;
                else if( o instanceof SealedObject )
                    so = (SealedObject)o;
                else
                    throw new Exception( "Cache value for key=" + cacheKey + " is not a String or SealedObject: " + o.getClass().getName() );                
            }
            Cipher dcipher = Cipher.getInstance("DES");
            dcipher.init(Cipher.DECRYPT_MODE, sealedObjectSecretKey);
            return (String) so.getObject(dcipher);
        }
        catch( Exception e )
        {
            LogService.logIt("RuntimeConstants.getStringValueFromSealedObject() NONFATAL " + e.toString() + ", cacheKey=" + cacheKey ); 
        }
        return null;
    }
    
    private static void substituteStringWithSealedObject( String cacheKey, Cipher cipher )
    {
        try
        {
            if( cacheKey==null || cacheKey.isBlank() )
            {
                LogService.logIt( "RuntimeConstants.substituteStringWithSealedObject() cacheKey is invalid (null or empty). Skipping." );
                return;
            }
            
            Object o = cache.get(cacheKey );
            if( o==null )
                throw new Exception( "no entry found for CacheKey " + cacheKey );
            
            if( !(o instanceof String) )
                throw new Exception( "Value for CacheKey " + cacheKey + " is not a String. Class="  + (o.getClass().getName()) );
            
            SealedObject so = new SealedObject((String)o, cipher);
            cache.put( cacheKey, so );
            
        }
        catch( Exception e )
        {
            LogService.logIt("RuntimeConstants.substituteStringWithSealedObject() NONFATAL " + e.toString() + ", cacheKey=" + cacheKey );
        }
    }

    private static void loadProperties( String propertiesFile )
    {
        // LogService.logIt( "RuntimeConstants.loadProperties() START " + propertiesFile );
        try
        {
            if( propertiesFile != null && !propertiesFile.isBlank() )
            {
                Properties props = new Properties();

                try (FileInputStream  fis = new FileInputStream( propertiesFile ))
                {
                    props.load( fis );
                }
                catch( Exception e )
                {
                    System.out.println( "ERROR Loading RuntimeConstants: " + e.toString() );
                }

                Enumeration propertyNames = props.propertyNames();

                String name = null;

                String strValue = null;

                Object currentValue = null;

                while( propertyNames.hasMoreElements() )
                {
                    name = (String) propertyNames.nextElement();

                    strValue = props.getProperty( name );

                    if( strValue!=null )
                        strValue=strValue.trim();
                        
                    if( name != null && name.length() > 0 && strValue != null && strValue.length() > 0 )
                    {
                        currentValue = cache.get( name );

                        //if( currentValue == null )
                        //    cache.put( name, strValue );

                        if( currentValue!=null )
                        {
                            if( currentValue instanceof Integer )
                                cache.put( name, Integer.parseInt( strValue ) );

                            else if( currentValue instanceof Float )
                                cache.put( name, Float.parseFloat(strValue ) );

                            else if( currentValue instanceof Long )
                                cache.put( name, Long.parseLong(strValue ) );

                            else if( currentValue instanceof Boolean )
                                cache.put( name, Boolean.parseBoolean(strValue ) );

                            else
                                cache.put( name, strValue );
                        }

                        // logIt( "Revised property from file: " + name + " : " + strValue );
                    }
                }
                // LogService.logIt( "RuntimeConstants.loadProperties() Updated " + props.size() + " keys from " + propertiesFile );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RuntimeConstants.loadProperties() reading properties file=" + propertiesFile );
        }
    }



    public static boolean getHttpsOnly()
    {
        return RuntimeConstants.getBooleanValue("httpsONLY");
    }



    public static TestKeyEventSelectionType getTestKeyEventSelectionType()
    {
        return TESTKEYEVENT_SELECTION_TYPE == null ? TestKeyEventSelectionType.ALL : TESTKEYEVENT_SELECTION_TYPE;
    }


    public static String dumpAllValues()
    {
        StringBuilder sb = new StringBuilder( "RuntimeConstants:\n" );

        for( String name : cache.keySet() )
        {
            sb.append( name + "=" + ( cache.get( name ) ).toString() + "\n" );
        }

        try
        {
            LogService.init();
        }

        catch( Exception e )
        {
            System.out.println( "RuntimeConstants.dumpAllValues() " + e.toString() );
        }

        logIt( sb.toString() );

        return sb.toString();
    }

    /**
     * Gets a value from the environment. Returns null if not found.
     */
    public static Object getValue( String theKey )
    {

        return cache.get( theKey );
    }

    public static void setValue( String theKey, Object theValue )
    {
        cache.put( theKey, theValue );
    }

    public static boolean getIsValueAString( String name )
    {
        Object currentValue = cache.get( name );

        return currentValue!=null && currentValue instanceof String;
    }
    
    public static void setValueFmString( String name, String strValue )
    {
        Object currentValue = cache.get( name );

        if( currentValue == null )
            cache.put( name, strValue );

        else
        {
            if( currentValue instanceof Integer )
                cache.put(name, Integer.valueOf(strValue ) );

            else if( currentValue instanceof Float )
                cache.put(name, Float.valueOf(strValue ) );

            else if( currentValue instanceof Long )
                cache.put(name, Long.valueOf( strValue ) );

            else if( currentValue instanceof Boolean )
                cache.put(name, Boolean.valueOf( strValue ) );

            else
                cache.put( name, strValue );
        }
    }

    /**
     * Gets a value from the environment. Returns null if not found.
     */
    public static String getStringValue( String theKey )
    {
        Object o = cache.get( theKey );
        
        if( o==null )
            return null;
        if( o instanceof SealedObject )
            return RuntimeConstants.getStringValueFromSealedObject(theKey, (SealedObject)o);
        
        return (String)o;
    }

    /**
     * Gets a value from the environment. Returns null if not found.
     */
    public static Boolean getBooleanValue( String theKey )
    {
        return (Boolean) cache.get( theKey );
    }

    public static Integer getIntValue( String theKey )
    {
        return (Integer) cache.get( theKey );
    }

    public static Float getFloatValue( String theKey )
    {
        return (Float) cache.get( theKey );
    }

    public static Long getLongValue( String theKey )
    {
        return (Long) cache.get( theKey );
    }

    public static long[] getLongArray( String key, String delimiter )
    {
        List<Long> ll = new ArrayList<>();

        long[] out = null;

        try
        {
            String s = getStringValue( key );

            if( s==null || s.isEmpty() )
                return new long[0];

            String[] tks = s.split( delimiter );

            for( String t : tks )
            {
                if( t!=null && !t.trim().isEmpty() )
                    ll.add( Long.parseLong(t) );
            }

            out = new long[ll.size()];

            for( int i=0; i<ll.size(); i++ )
            {
                out[i] = ll.get(i).longValue();
            }
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( e, "RuntimeConstants.getLongArray() key=" + key + ", delim=" + delimiter );
        }

        return out;


    }

    public static List<Integer> getIntList( String key, String delimiter )
    {
        List<Integer> ll = new ArrayList<>();

        try
        {
            String s = getStringValue( key );
            if( s==null || s.isEmpty() )
                return ll;

            for( String t : s.split( delimiter) )
            {
                if( t!=null && !t.trim().isEmpty() )
                    ll.add(Integer.valueOf(t) );
            }
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( e, "RuntimeConstants.getIntList() key=" + key + ", delim=" + delimiter );
        }

        return ll;
    }
    
    

    /**
     * logs messages
     */
    private static void logIt( String message )
    {
        LogService.getLogger().fine( message );
    }

}
