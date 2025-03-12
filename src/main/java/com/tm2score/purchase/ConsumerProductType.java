package com.tm2score.purchase;


public enum ConsumerProductType
{

    ASSESSMENT_JOBSPECIFIC(0, "cpt.AssessJobSpec"),
    ASSESSMENTBATTERY(1, "cpt.Battery"),
    ASSESSMENT_SKILLS(2, "cpt.AssessSkills"),
    ASSESSMENT_PERSONALITY(3, "cpt.AssessPersonality"),
    ASSESSMENT_COGNITIVE(4, "cpt.AssessCognitive"),
    ASSESSMENT_OTHER(5, "cpt.AssessOther"),
    ASSESSMENT_DEVELOPMENT(6, "cpt.AssessDevel"),
    ASSESSMENT_COMPETENCY(7, "cpt.AssessCompetency"),
    ASSESSMENT_POPMSOFFICE(8, "cpt.PopularMSOfficeSims"),
    ASSESSMENT_VOICE(9, "cpt.AssessVoiceIVR"),
    ASSESSMENT_WHOLEPERSON(10, "cpt.AssessWholePerson"),
    ASSESSMENT_VIDEOINTERVIEW(11, "cpt.AssessVidInterview"),
    ASSESSMENT_VIDEOINTERVIEW_LIVE(12, "cpt.AssessVidInterviewLive"),
    ASSESSMENT_ANIMATED_SIM(13, "cpt.AssessAnimatedSim" ),
    PREVIEW(100, "cpt.JobPreview"),
    TRAINING(200, "cpt.Train" ),
    MISC(1000, "cpt.Misc" ),
    INFO_ARTICLE(1001, "cpt.Article"),
    INFO_BOOK(1002, "cpt.Book"),
    INFO_WHITEPAPER(1003, "cpt.Whitepaper"),
    INFO_BLOG(1004, "cpt.BlogEntry"),
    INFO_NEWS(1005, "cpt.NewsEntry"),
    INFO_WEBPAGE(1006, "cpt.WebPage");

    private final int consumerProductTypeId;

    private String key;

    private ConsumerProductType( int p,
                         String key )
    {
        this.consumerProductTypeId = p;

        this.key = key;
    }

    public int getConsumerProductTypeId()
    {
        return this.consumerProductTypeId;
    }

    public boolean getIsVoice()
    {
        return equals( ASSESSMENT_VOICE );
    }
    
    public boolean getIsJobSpecific()
    {
        return equals( ASSESSMENT_JOBSPECIFIC );
    }

    
    public boolean getIsEligibleForFeedbackReport()
    {
        return equals( ASSESSMENT_JOBSPECIFIC ) || equals( ASSESSMENT_SKILLS ) || equals( ASSESSMENT_PERSONALITY ) ||
               equals( ASSESSMENT_WHOLEPERSON );
    }

    public String getKey()
    {
        return key;
    }

    public static ConsumerProductType getValue( int id )
    {
        ConsumerProductType[] vals = ConsumerProductType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getConsumerProductTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
