package com.tm2score.voicevibes;



/**
{
   "data":{
      "recordingId":"04b25972-756b-4176-9dfa-d3c51f3d8684",
      "groupId":"43468d75-2087-47ad-b511-37b76a926ef7",
      "speechId":null,
      "title":"17_IVR_FF_I_2",
      "fileName":"From api.twilio.com",
      "duration":87.87125,
      "createdDate":"2017-08-24T20:54:36.177Z",
      "focusArea":null,
      "submitted":false,
      "submissionDate":null,
      "feedbackRequested":false,
      "feedbackRequestedDate":null,
      "results":{
         "algorithm":"v2-default",
         "scores":{
            "pace":3.97,
            "vibes":{
               "clear":5.51,
               "ditsy":1.07,
               "pushy":2.65,
               "timid":1.73,
               "boring":4.21,
               "nervous":2.09,
               "arrogant":2.54,
               "detached":3.04,
               "assertive":4.35,
               "authentic":4.3,
               "confident":5.12,
               "confusing":2.49,
               "energetic":5.35,
               "organized":5.04,
               "personable":3.42,
               "persuasive":3.9,
               "belligerent":2.09,
               "captivating":3.55,
               "condescending":2.1,
               "unapproachable":2.03
            },
            "clarity":8.6,
            "overall":6.69,
            "upspeak":0,
            "pauseToTalk":35,
            "varietyPace":{
               "inBand":51,
               "aboveBand":25,
               "belowBand":24
            },
            "varietyPitch":{
               "inBand":44,
               "aboveBand":28,
               "belowBand":28
            },
            "wordSpotting":{
               "so":0,
               "like":0,
               "right":0,
               "uh/um":0,
               "simply":0,
               "i think":0,
               "honestly":0,
               "you know":0,
               "basically":0,
               "literally":0
            },
            "varietyVolume":{
               "inBand":50,
               "aboveBand":30,
               "belowBand":20
            },
            "strengthOfOpening":2.95
         },
         "completedDate":"2017-08-24T20:56:03.020Z",
         "status":"completed"
      },
      "playbackUrl":"https://voicevibes.s3.amazonaws.com/playback/04b25972-756b-4176-9dfa-d3c51f3d8684.mp3"
   },
   "serverDate":"2017-08-24T21:17:38.989Z"
}
About
 * 
 * 
 * @author miker_000
 */
public enum VoiceVibesScaleType
{
    // Starts here
    OVERALL(0,"Overall", "overall", "vibesst.overall", 0),
    OPENING(10,"Strength of Opening", "strengthOfOpening", "vibesst.opening", 0),
    CLARITY(11,"Clarity", "clarity", "vibesst.clarity", 0),
    PACE(12,"Pace", "pace", "vibesst.pace", 1),
    PAUSES(13,"Talk to Pause Ratio", "pauseToTalk", "vibesst.pauseToTalk", 1),
    VARIETY_VOLUME(20,"Volume Variety", "varietyVolume", "vibesst.varietyVolume", 1),
    VARIETY_PACE(21,"Pace Variety", "varietyPace", "vibesst.varietyPace", 1),
    VARIETY_PITCH(22,"Pitch Variety", "varietyPitch", "vibesst.varietyPitch", 1),
    VIBE_ARROGANT(30,"Arrogant", "arrogant", "vibesst.vibes.arrogant", 2),
    VIBE_ASSERTIVE(31,"Assertive", "assertive", "vibesst.vibes.assertive", 0),
    VIBE_AUTHENTIC(32,"Authentic", "authentic", "vibesst.vibes.authentic", 0),
    VIBE_BELLIGERENT(33,"Belligerent", "belligerent", "vibesst.vibes.belligerent", 2),
    VIBE_BORING(34,"Boring", "boring", "vibesst.vibes.boring", 2),
    VIBE_CAPTIVATING(35,"Captivating", "captivating", "vibesst.vibes.captivating", 0),
    VIBE_CLEAR(36,"Clear", "clear", "vibesst.vibes.clear", 0),
    VIBE_CONDESCENDING(37,"Condescending", "condescending", "vibesst.vibes.condescending", 2),
    VIBE_CONFIDENT(38,"Confident", "confident", "vibesst.vibes.confident", 0),
    VIBE_CONFUSING(39,"Confusing", "confusing", "vibesst.vibes.confusing", 2),
    VIBE_DETACHED(40,"Detached", "detached", "vibesst.vibes.detached", 2),
    VIBE_DITSY(41,"Ditsy", "ditsy", "vibesst.vibes.ditsy", 2),
    VIBE_ENERGETIC(42,"Energetic", "energetic", "vibesst.vibes.energetic", 0),
    VIBE_NERVOUS(43,"Nervous", "nervous", "vibesst.vibes.nervous", 2),
    VIBE_ORGANIZED(44,"Organized", "organized", "vibesst.vibes.organized", 0),
    VIBE_PERSONABLE(45,"Personable", "personable", "vibesst.vibes.personable", 0),
    VIBE_PERSUASIVE(46,"Persuasive", "persuasive", "vibesst.vibes.persuasive", 0),
    VIBE_PUSHY(47,"Pushy", "pushy", "vibesst.vibes.pushy", 2),
    VIBE_TIMID(48,"Timid", "timid", "vibesst.vibes.timid", 2),
    VIBE_UNAPPROACHABLE(49,"Unapproachable", "unapproachable", "vibesst.vibes.unapproachable", 2),
    WORD_SPOTTING(60,"Word Spotting Counts", "wordSpotting", "vibesst.wordSpotting", 1);

    private final int voiceVibesScaleTypeId;

    private final String name;
    
    private final String jsonKey;
    private final String langKey;
    
    /**
     * 0=High is better
     * 1=middle is best
     * 2=lower is better
     * 
     * 9=Nothing is better
     * 
     */
    private final int scaleTypeId;
    
    private VoiceVibesScaleType( int p , String n, String jsonKey, String langKey, int scaleTypeId )
    {
        this.voiceVibesScaleTypeId = p;

        this.name = n;
        this.jsonKey=jsonKey;
        this.langKey=langKey;
        this.scaleTypeId=scaleTypeId;
    }

    
    public boolean isShowTextForNumScore()
    {
        return equals( PACE ) || equals(PAUSES) || isVariety();
    }
    
    public String getScoreLangKeyForTextNumScore( float score )
    {
        if( !isShowTextForNumScore() )
            return "";
        
        if( score <35 )
            return "g.VVScrTooLittle";
        if( score >=35 && score<42 )
            return "g.VVScrGood";
        if( score >=42 && score<48 )
            return "g.VVScrVeryGood";
        if( score >=48 && score<52 )
            return "g.VVScrExcellent";
        if( score >=52 && score<58 )
            return "g.VVScrVeryGood";
        if( score >=58 && score<65 )
            return "g.VVScrGood";
        if( score >=65 )
            return "g.VVScrTooMuch";
        
        return "";
    }
    
    public boolean isGoodVibe()
    {
        return isVibe() && ( equals (VIBE_ASSERTIVE) || equals(VIBE_AUTHENTIC) ||  equals(VIBE_CAPTIVATING) || equals(VIBE_CLEAR) || 
                             equals(VIBE_CONFIDENT) || equals(VIBE_ENERGETIC) || equals(VIBE_ORGANIZED) || equals(VIBE_PERSONABLE)  || equals(VIBE_PERSUASIVE) );
    }

    public boolean isBadVibe()
    {
        return isVibe() && !isGoodVibe();
    }
    
    
    

    public boolean isOverall()
    {
        return equals (OVERALL);
    }

    public boolean isStructure()
    {
        return equals (OPENING) || equals(CLARITY) ||  equals(PACE) || equals(PAUSES);
    }

    public boolean isHighGood()
    {
        return scaleTypeId==0;
    }

    public boolean isMiddleGood()
    {
        return scaleTypeId==1;
    }
    
    
    public boolean isLowGood()
    {
        return scaleTypeId==2;
    }
    
    public boolean isNoGood()
    {
        return scaleTypeId==9;
    }
    
    
    
    public boolean isVibe()
    {
        return voiceVibesScaleTypeId<60 && voiceVibesScaleTypeId>=30;
    }

    
    
    public boolean isVariety()
    {
        return voiceVibesScaleTypeId<30 && voiceVibesScaleTypeId>=20;
    }

    public boolean isWordspot()
    {
        return voiceVibesScaleTypeId==60;
    }
    
    public boolean containsObject()
    {
        return isVibe() || isWordspot()  || isVariety();
    }
    
    public String getScaleLowKey()
    {
        return getLangKey() + ".lowval";
    }

    public String getScaleHighKey()
    {
        return getLangKey() + ".highval";
    }
    
    public String getTopKey()
    {
        if( !containsObject() )
            return null;
        
        if( isVibe() )
            return "vibes";
        
        if( isWordspot() )
            return getJsonKey();
        
        if( isVariety() )
            return getJsonKey();
        
        return null;
    }
    

    public int getVoiceVibesScaleTypeId()
    {
        return this.voiceVibesScaleTypeId;
    }

    public String getName() {
        return name;
    }

    public String getJsonKey() {
        return jsonKey;
    }

    public String getLangKey() {
        return langKey;
    }

    public int getScaleTypeId() {
        return scaleTypeId;
    }



    public static VoiceVibesScaleType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return name;
    }
    
    
    public static VoiceVibesScaleType getValue( int id )
    {
        VoiceVibesScaleType[] vals = VoiceVibesScaleType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getVoiceVibesScaleTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
