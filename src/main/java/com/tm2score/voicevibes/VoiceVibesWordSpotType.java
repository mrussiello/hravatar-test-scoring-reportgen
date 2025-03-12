package com.tm2score.voicevibes;



public enum VoiceVibesWordSpotType
{
    // Starts here
    BASICALLY(0,"basically","vibesspot.basically"),
    HONESTLY(1,"honestly","vibesspot.honestly"),
    I_THINK(2,"i think","vibesspot.ithink"),
    LIKE(3,"like","vibesspot.like"),
    LITERALLY(4,"literally","vibesspot.literally"),
    RIGHT(5,"right","vibesspot.right"),
    SIMPLY(6,"simply","vibesspot.simply"),
    SO(7,"so","vibesspot.so"),
    UH_UM(8,"uh/um","vibesspot.uhum"),
    YOU_KNOW(9,"you know","vibesspot.youknow");

    private final int voiceVibesWordSpotTypeId;

    private final String jsonKey;
    
    private final String langKey;

    private VoiceVibesWordSpotType( int p , String key, String langKey )
    {
        this.voiceVibesWordSpotTypeId = p;

        this.jsonKey = key;
        
        this.langKey=langKey;
    }


    public int getVoiceVibesWordSpotTypeId()
    {
        return this.voiceVibesWordSpotTypeId;
    }



    public static VoiceVibesWordSpotType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getJsonKey()
    {
        return jsonKey;
    }

    public String getLangKey() {
        return langKey;
    }

    
    
    public static VoiceVibesWordSpotType getValue( int id )
    {
        VoiceVibesWordSpotType[] vals = VoiceVibesWordSpotType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getVoiceVibesWordSpotTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
