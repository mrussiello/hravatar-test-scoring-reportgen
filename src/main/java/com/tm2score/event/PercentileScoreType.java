package com.tm2score.event;


public enum PercentileScoreType
{
    LEGACY(0,"Legacy" ),
    WEIGHTED_AVG_ZSCORES(1,"Weighted Average of Z Scores" );

    private int percentileScoreTypeId;

    private String key;

    private PercentileScoreType( int typeId , String key )
    {
        this.percentileScoreTypeId = typeId;

        this.key = key;
    }



    public static PercentileScoreType getValue( int id )
    {
        PercentileScoreType[] vals = PercentileScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getPercentileScoreTypeId() == id )
                return vals[i];
        }

        return LEGACY;
    }

    public int getPercentileScoreTypeId()
    {
        return percentileScoreTypeId;
    }

    public String getKey()
    {
        return key;
    }

}
