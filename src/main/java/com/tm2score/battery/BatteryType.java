package com.tm2score.battery;
import java.util.Map;
import java.util.TreeMap;


public enum BatteryType
{
    MULTIUSE( 0, "Multi Use" ),
    SINGLEUSE( 1, "Single Use" );

    private final int batteryTypeId;

    private String key;

    private BatteryType( int p , String key )
    {
        this.batteryTypeId = p;

        this.key = key;
    }


    public int getBatteryTypeId()
    {
        return this.batteryTypeId;
    }


    public static Map<String,Integer> getMap()
    {
        Map<String,Integer> outMap = new TreeMap<>();

        BatteryType[] vals = BatteryType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            outMap.put(vals[i].getKey() , new Integer( vals[i].getBatteryTypeId() ) );
        }

        return outMap;
    }



    public String getName()
    {
        return key;
    }




    public static BatteryType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }



    public static BatteryType getValue( int id )
    {
        BatteryType[] vals = BatteryType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBatteryTypeId() == id )
                return vals[i];
        }

        return MULTIUSE;
    }

}
