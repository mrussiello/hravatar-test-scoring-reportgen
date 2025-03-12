package com.tm2score.battery;


public enum BatteryScoreType
{
    NONE( 0, "battscore.none" ),
    AVERAGE( 1, "battscore.average" ),
    WEIGHTED_AVERAGE( 2, "battscore.weightedaverage" ),
    REPORT_ONLY( 3, "battscore.reportonly" );

    private final int batteryScoreTypeId;

    private String key;

    private BatteryScoreType( int p , String key )
    {
        this.batteryScoreTypeId = p;

        this.key = key;
    }


    public boolean needsScore()
    {
        return !equals( NONE )&& !equals( REPORT_ONLY );
    }

    public boolean needsBatteryScoreObject()
    {
        return !equals( NONE );
    }
    
    
    public int getBatteryScoreTypeId()
    {
        return this.batteryScoreTypeId;
    }



    public String getKey()
    {
        return key;
    }



    public static BatteryScoreType getValue( int id )
    {
        BatteryScoreType[] vals = BatteryScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBatteryScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

}
