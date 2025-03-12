package com.tm2score.sim;



public enum SimCompetencyScaledScoreCalcType
{
    DEFAULT(0,"Derive from Simlet Competency Score Type (default)"),
    TRANSFORMED_Z(1,"Transformed Z-Score (Reqs Scaled Mean/SD)"),
    RAW(3,"Use Competency Raw Score Directly");

    private final int simCompetencyScaledScoreCalcTypeId;

    private final String name;


    private SimCompetencyScaledScoreCalcType( int s , String n )
    {
        this.simCompetencyScaledScoreCalcTypeId = s;

        this.name = n;
    }
    

    public boolean getIsTransform()
    {
        return equals( TRANSFORMED_Z );
    }
    
    public boolean getEqualsRawScore()
    {
        return equals( RAW );
    }
    

    public static SimCompetencyScaledScoreCalcType getValue( int id )
    {
        SimCompetencyScaledScoreCalcType[] vals = SimCompetencyScaledScoreCalcType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencyScaledScoreCalcTypeId() == id )
                return vals[i];
        }

        return DEFAULT;
    }


    public int getSimCompetencyScaledScoreCalcTypeId()
    {
        return simCompetencyScaledScoreCalcTypeId;
    }

    public String getName()
    {
        return name;
    }

}
