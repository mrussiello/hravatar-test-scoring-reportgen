package com.tm2score.battery;


public enum BatteryScoreStatusType
{
    ACTIVE( 0, "Active" ),
    //ACTIVE_NONORMS( 10, "Active, Removed from Norm Groups" ),
    DEACTIVATED( 100, "Deactivated" );

    private final int batteryScoreStatusTypeId;

    private String key;

    private BatteryScoreStatusType( int p , String key )
    {
        this.batteryScoreStatusTypeId = p;

        this.key = key;
    }


    public int getBatteryScoreStatusTypeId()
    {
        return this.batteryScoreStatusTypeId;
    }

    public String getName()
    {
        return key;
    }



    public static BatteryScoreStatusType getValue( int id )
    {
        BatteryScoreStatusType[] vals = BatteryScoreStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBatteryScoreStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }

}
