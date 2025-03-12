package com.tm2score.simlet;



public enum SimletSubnodeType
{
    UNSET(0,"Please Select ..." ),
    QUESTION(1,"Question To Include In Report" ),
    VALUE_FOR_REPORT(2,"Value To Include In Report" ),
    IGNORED(3,"Ignored - Do Not Include In Report" );

    private final int simletSubnodeTypeId;

    private final String name;

    private SimletSubnodeType( int s , String n )
    {
        this.simletSubnodeTypeId = s;

        this.name = n;
    }


    public static SimletSubnodeType getValue( int id )
    {
        SimletSubnodeType[] vals = SimletSubnodeType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimletSubnodeTypeId() == id )
                return vals[i];
        }

        return UNSET;
    }


    public int getSimletSubnodeTypeId()
    {
        return simletSubnodeTypeId;
    }

    public String getName()
    {
        return name;
    }

}
