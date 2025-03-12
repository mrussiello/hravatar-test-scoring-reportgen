package com.tm2score.voicevibes;


public enum VoiceVibesAccountType
{
    // Starts here
    NONE(0,"None"),
    NORMAL(10,"Normal"),
    HIGHVOL(11,"High Volume - Preagreed-Deal"),
    DEMO(20,"Demo"),
    ORG(30,"Defined in Org");

    private final int voiceVibesAccountTypeId;

    private String key;

    private VoiceVibesAccountType( int p , String key )
    {
        this.voiceVibesAccountTypeId = p;

        this.key = key;
    }


    public int getVoiceVibesAccountTypeId()
    {
        return this.voiceVibesAccountTypeId;
    }



    public static VoiceVibesAccountType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }
    
    
    public static VoiceVibesAccountType getValue( int id )
    {
        VoiceVibesAccountType[] vals = VoiceVibesAccountType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getVoiceVibesAccountTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
