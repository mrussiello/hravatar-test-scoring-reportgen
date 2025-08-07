package com.tm2score.global;

public class Constants
{
    public static String[] AFFILIATEIDS_THAT_PULL_PDF_REPORTS = new String[]{"midot"};
    
     // Countries where texting is allowed. To get name of country use cntry.PY.
    public static String[] SMS_OK_COUNTRIES = new String[] {"AU","CA","GB","MX","PE", "SG","US","UK","ZA"};
    public static int[] SMS_OK_COUNTRY_CODES = new int[] {61,1,44,52,51,65,1,44,27};
    
    
    public static String[] NUMERIC_1_TO_10_TICKVALS = new String[] {"1","2","3","4","5","6","7","8","9","10"};
    public static String[] NUMERIC_0_TO_100_TICKVALS = new String[] {"0","20","40","60","80","100"};
    public static String[] NUMERIC_0_TO_3_TICKVALS = new String[] {"0.0","1.0","2.0","3.0"};
    public static String[] NUMERIC_1_TO_5_TICKVALS = new String[] {"1","2","3","4","5"};
        
    
    // public final static boolean USE_CAVEATS2 = true;
    
    public final static int YES = 1;

    public final static String DELIMITER = "@#*@";

    public final static String SYSTEM_SESSION_COUNTER = "Tm2ScoreSystemSessionCounter";

    public final static String DEFAULT_RESOURCE_BUNDLE = "com.tm2score.resources.TM2Messages";
        
    public static String SENTINO_BUNDLE = "com.tm2score.custom.sentino.SentinoMessages";    

    public final static int MIN_NORM_COUNT_FOR_BATTERY = 100;
    public final static int MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS = 100;
    public final static int MIN_NORM_COUNT_FOR_TEST_VERSION = 100; // 25;

    public final static String[] AUTO_EXCLUDE_EMAILS = new String[] { "@clicflic.com", "@hravatar.com", "qamail" };

    public static int IDLE_SESSION_TIMEOUT_MINS = 20;
    
    public static final String SERVER_START_LOG_MARKER = "*************************************** SERVER START ******************************************";

    //public static String SUPPORT_EMAIL = "support@hravatar.com";

    //public static String SYSTEM_ADMIN_EMAIL = "mike@hravatar.com";

    //public static String SUPPORT_EMAIL_NOREPLY = "no-reply@hravatar.com";
    
    public static final int MAX_DAYS_PREV_RCCHECK = 90;
    
    // public static String MEDIA_CORPIMAGEICON_DIRECTORY = "/tacorp";

    // public static int MAX_BAD_PIN_HELP = 5;

    // public static int PUBLIC_ORG_ID = 2;

    // public static int PUBLIC_SUBORG_ID = 2;
    public static int MAX_FAILED_LOGON_ATTEMPTS  = 5;    
    public static int LOGON_LOCKOUT_MINUTES = 30;
    
    public static int MAX_PASSWORD_AGE_MONTHS = 12;    

    public static int VIBES_WAIT_TIME_MINS = 6;
    
    public static int ARCHIVE_DELAY_SECS = 60;

    
    public final static String TKCOMPLETEMSG = "TKCOMPLETEMSG";
    public final static String TKCOMPLETEMSGSUBJ = "TKCOMPLETEMSGSUBJ";
    public final static String TKREMINDEREMAIL = "TKREMINDEREMAIL";
    public final static String TKREMINDEREMAILSUBJ = "TKREMINDEREMAILSUBJ";
    public final static String TKREMINDERTEXT = "TKREMINDERTEXT";
    
    

    public static String ESSAYTITLE = "ESSAYTITLE";
    public static String TRANSLATECOMPARE = "TRANSLATECOMPARE";
    public static String SCALEDSCORECEILING = "SCALEDSCORECEILING";
    public static String SCALEDSCOREFLOOR = "SCALEDSCOREFLOOR";
    public static String SCORETEXTCAVEAT = "SCORETEXTCAVEAT";

    public static String DESCRIPTIONKEY = "DESCRIPTION";
    public static String CEFRTYPE = "CEFRTYPE";
    public static String CEFRLEVEL = "CEFRLEVEL";
    public static String CEFRLEVELTEXT = "CEFRLEVELTEXT";
    public static String CAVEATSKEY = "CAVEATS";
    public static String CAVEAT2_KEY = "CAVEAT2";

    public static String STD_RISKFACTORSKEY = "STDRISKFACTORS";
    public static String DETAILINTROKEY = "DETAILINTRO";
    
    public static String RIASEC_COMPACT_INFO_KEY = "RIASECINFO";
    public static String EEOCAT_COMPACT_INFO_KEY = "EEOCATINFO";

    public static String FINDLYATTEMPTSKEY = "FINDLYATTEMPTS";
    
    public static String EARLYEXITBATTERYKEY = "EARLYEXITBATTERY";
    
    public static String CATEGORYINFOKEY = "CATEGINFO";

    public static String OVERRIDESHOWRAWSCOREKEY = "OVERRIDESHOWRAWSCOREKEY";
    
    public static String ITEMSCOREINFOKEY = "ITEMSCOREINFO";
    
    public static String COMPETENCYSPECTRUMKEY = "SPECTRUMINFO";
    
    public static String SCOREVALUEKEY = "SCOREVALUE";
    public static String SCORETEXTKEY = "SCORETEXT";
    
    public static String INTERVIEWKEY = "INTERVIEW";
    public static String ANCHORLOWKEY = "ANCHORLOW";
    public static String ANCHORMEDKEY = "ANCHORMED";
    public static String ANCHORHIKEY = "ANCHORHI";
    
    public static String PREFERREDROLENAMES = "PREFERREDROLENAMES";
    
    public static String IBMINSIGHT = "IBMINSIGHT";
    // public static String IBMLOWWORDSERROR = "IBMLOWWORDSERROR";
    
    // public static int MIN_WORDS_4_INSIGHT = 600;

    public static int MIN_TEXT_LENGTH_FOR_AI_SCORING = 100;
    public static int MIN_TEXT_LENGTH_FOR_AI_SUMMARY = 300;
    public static String AI_SUMMARY_TEXTTITLE_KEY = "AISUMMARY";

    
    /**
     * Comma Delimited list.
     */
    public static String SPELLING_IGNORE_KEY = "SPELLIGNORE";
    public static String IDEAL_RESPONSE_KEY = "IDEALRESPONSE";
    public static String AI_INSTRUCTIONS_KEY = "AIINSTRUCTIONS";
    public static String AI_PROMPT_KEY = "AIPROMPT";

    
    public static String TOPIC_KEY = "TOPIC";
    public static String POINTS_KEY = "POINTS";
    public static String NONCOMPTITL_KEY = "NONCOMPTITLE";
    public static String AUDIO_KEY = "AUDIO";
    public static String TEXTINPUTTYPE_KEY = "TEXTINPUTTYPE";
    public static String ESSAYPROMPT_KEY = "ESSAYPROMPT";

    public static int MIN_PERCENTILE_COUNT = 10;

    public static int CT2_COLORGRAPHWID = 180;

    public static int CT2_COLORGRAPHWID_EML = 220;

    public static int IVR_COLORGRAPHWID = 200;
    public static int IVR_COLORGRAPHHGT = 11;
    
    public static int DEFAULT_TESTKEY_BATCH_SIZE = 100;

    public static int MAX_ESSAY_SUBMITS_PER_BATCH = 15;
    
    public static float WEB_PLAG_CHECK_MAX_MATCH = 82;
    public static String ESSAY_PLAGIARIZED = "PLAGIARIZED";
    public static String ESSAY_WPM = "WPM";
    public static String ESSAY_HIGH_WPM = "HIGHWPM";
    

    public static String DATA_ENTRY_RESP_VALS_KEY = "###DEKRC";

    public static String IBMINSIGHT_SCORE_GRAPH_COLORS = "rFeFeFe,ryE1F0Fd,yBFE0FE,yg94CCFE,g62B4FE";
            
    public static String[] SCORE_GRAPH_COLS = new String[] {"r","ry","y","yg", "g" };
    public static String[] SCORE_GRAPH_COLS_SEVEN = new String[] {"b","r","ry","y","yg", "g", "w" };
    
    public static int IFRAMETEST_AUTOCOMPLETE_HOURS = 48;
        
    public static int ORGAUTOTEST_EXPIRATION_WARNING_HOURS = 12;
    public static int ORGAUTOTEST_EXPIRATION_WARNING_WINDOW_MINS = 15;
    
    public static int[] SUBSCRIPTION_EXPIRATION_WARNING_DAYS = new int[]{30,7,1};
    
    public static int[] SUBSCRIPTION_EXPIRATION_ADMIN_NOTIFICATION_DAYS = new int[]{180,150,120,90,60, 45, 21, 14, 5};

    public static int[] CREDITS_EXPIRATION_WARNING_DAYS = new int[]{30};
    

    public static float IMAGE_CAPTURE_HIGH_RISK_CUTOFF = 33.33f;
    public static float IMAGE_CAPTURE_MED_RISK_CUTOFF = 75f;
    public static int MAX_IDENTITY_IMAGES_IN_REPORT = 25;
    public static int MAX_IDCARD_IMAGES_IN_REPORT = 3;
    public static int MAX_INITIAL_PHOTO_IMAGES_IN_REPORT = 2;
    
    public static final String CSVEMAILHEADER="CSVEMAILHEADER";
    public static final String CSVEMAILFOOTER="CSVEMAILFOOTER";
    public static final String CSVPOSTTESTCONTACTSTR="CSVPOSTTESTCONTACTSTR";
    
    public static final float MIN_METASCORE_CONFIDENCE = 0.4f;
    public static float MIN_CONFIDENCE_AI = 0.5F;
    public static float MIN_CONFIDENCE_DISCERN = 0.1F;
}
