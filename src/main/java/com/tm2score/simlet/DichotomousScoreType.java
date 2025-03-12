package com.tm2score.simlet;



public enum DichotomousScoreType
{
    ITEMS_ANSWERED(0,"Use number of items answered as total"),
    TOTAL_ITEMS(1,"Use total number of items in simlet as total"),
    RQD_ITEMS(2,"Use number of REQUIRED items in simlet as total");

    private final int dichotomousScoreTypeId;

    private final String name;


    private DichotomousScoreType( int s , String n )
    {
        this.dichotomousScoreTypeId = s;

        this.name = n;
    }

    public static DichotomousScoreType getValue( int id )
    {
        DichotomousScoreType[] vals = DichotomousScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getDichotomousScoreTypeId() == id )
                return vals[i];
        }

        return ITEMS_ANSWERED;
    }


    public int getDichotomousScoreTypeId()
    {
        return dichotomousScoreTypeId;
    }

    public String getName()
    {
        return name;
    }

}
