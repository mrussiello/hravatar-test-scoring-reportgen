package com.tm2score.score;



public enum MergableScoreObjectType
{
    VOICEVIBES(1, "Voice Vibes Result" ); 

    private final int mergableScoreObjectTypeId;

    private String key;


    private MergableScoreObjectType( int p,
                         String key )
    {
        this.mergableScoreObjectTypeId = p;
        this.key = key;
    }

    
    
    
    public String getName()
    {
        return this.key;
    }

    public int getMergableScoreObjectTypeId() {
        return mergableScoreObjectTypeId;
    }
    
    public static MergableScoreObjectType getValue( int id )
    {
        MergableScoreObjectType[] vals = MergableScoreObjectType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getMergableScoreObjectTypeId() == id )
                return vals[i];
        }

        return null;
    }
    

    

}
