package com.tm2score.sim;



public enum SimCompetencyRawScoreCalcType
{
    DEFAULT(0,"Derive from Simlet Competency Score Type(default)"),
    ZSCORE_BASED_ON_TOTALS(1,"Z-Score w Ttl Crct/Pts (Reqs MN/SD in Stats)");

    private final int simCompetencyRawScoreCalcTypeId;

    private final String name;


    private SimCompetencyRawScoreCalcType( int s , String n )
    {
        this.simCompetencyRawScoreCalcTypeId = s;

        this.name = n;
    }
    
    public boolean getIsZScore()
    {
        return equals( ZSCORE_BASED_ON_TOTALS );
    }



    public static SimCompetencyRawScoreCalcType getValue( int id )
    {
        SimCompetencyRawScoreCalcType[] vals = SimCompetencyRawScoreCalcType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencyRawScoreCalcTypeId() == id )
                return vals[i];
        }

        return DEFAULT;
    }


    public int getSimCompetencyRawScoreCalcTypeId()
    {
        return simCompetencyRawScoreCalcTypeId;
    }

    public String getName()
    {
        return name;
    }

}
