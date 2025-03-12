package com.tm2score.proctor;


public enum SuspiciousActivityThresholdType
{
    NEVER(0,"satt.never"),
    // ONCE(1,"satt.one"),
    TWICE(2,"satt.two"),
    THREE(3,"satt.three"),
    FOUR(4,"satt.four"),
    FIVE(5,"satt.five"),
    TEN(10,"satt.ten"),
    TWENTY(20,"satt.twenty"),
    THIRTY(30,"satt.thirty"),
    FORTY(40,"satt.forty");

    private final int suspiciousActivityThresholdTypeId;

    private final String key;

    private SuspiciousActivityThresholdType( int typeId , String key )
    {
        this.suspiciousActivityThresholdTypeId = typeId;

        this.key = key;
    }

    public static SuspiciousActivityThresholdType getValue( int id )
    {
        SuspiciousActivityThresholdType[] vals = SuspiciousActivityThresholdType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSuspiciousActivityThresholdTypeId() == id )
                return vals[i];
        }

        return NEVER;
    }

    public int getSuspiciousActivityThresholdTypeId()
    {
        return suspiciousActivityThresholdTypeId;
    }
    
    public String getKey()
    {
        return key;
    }
}
