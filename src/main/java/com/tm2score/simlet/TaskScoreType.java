package com.tm2score.simlet;



public enum TaskScoreType
{
    NONE(0,"No Score"),
    STATE_VARIABLE(1,"State Variable Value"),
    PCT_CORRECT(2,"Pct Correct Across Simlet" ),
    POINTS_AT_ENDPOINT(3,"Points at Simlet Endpoint" );

    private final int taskScoreTypeId;

    private final String name;


    private TaskScoreType( int s , String n )
    {
        this.taskScoreTypeId = s;

        this.name = n;
    }

    public boolean isNoScore()
    {
        return equals( NONE );
    }



    public boolean isStateVariable()
    {
        return equals( STATE_VARIABLE );
    }

    public boolean isPercentCorrect()
    {
        return equals( PCT_CORRECT );
    }

    public boolean isPointsAtEndpoint()
    {
        return equals( POINTS_AT_ENDPOINT );
    }


    public static TaskScoreType getValue( int id )
    {
        TaskScoreType[] vals = TaskScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTaskScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getTaskScoreTypeId()
    {
        return taskScoreTypeId;
    }

    public String getName()
    {
        return name;
    }

}
