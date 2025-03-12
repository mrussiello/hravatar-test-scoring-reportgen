package com.tm2score.bot;


public enum BotEventStatusType
{
    STARTED(0,"Started"),
    COMPLETED(100,"Completed"),
    COMPLETED_PENDING_EXTERNAL_SCORES(101,"Pending External Scores"),
    SCORING_STARTED(105,"Scoring Started"),
    SCORED(110,"Scored"),
    EXPIRED(201,"Expired"),
    DEACTIVATED(202,"Deactivated"),
    SCORE_ERROR(203,"Scoring Error" );

    private final int botEventStatusTypeId;

    private String key;


    private BotEventStatusType( int p , String key )
    {
        this.botEventStatusTypeId = p;

        this.key = key;
    }


    public String getName()
    {
        return key;
    }


    public String getKey()
    {
        return key;
    }
    
    public boolean isCompleteOrHigher()
    {
        return botEventStatusTypeId>0;
    }
    
    
    public boolean isScored()
    {
        return equals( SCORED );
    }
    
    
    public int getBotEventStatusTypeId() {
        return botEventStatusTypeId;
    }



    public static BotEventStatusType getValue( int id )
    {
        BotEventStatusType[] vals = BotEventStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBotEventStatusTypeId() == id )
                return vals[i];
        }

        return STARTED;
    }

}
