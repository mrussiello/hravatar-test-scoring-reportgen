package com.tm2score.sim;



public enum SimCompetencyScoreCalculationType
{
    INDIVIDUALLY_EVEN(0,"Individually by Simlet, Combine Evenly"),    // When an ONET Soc is selected
    INDIVIDUALLY_COVERAGE(1,"Individually by Simlet, Combine Using Simlet Comp Coverage Values"),
    ACROSSALL(2,"Calculate Across All Simlets");


    private final int simCompetencyScoreCalculationTypeId;

    private final String name;


    private SimCompetencyScoreCalculationType( int s , String n )
    {
        this.simCompetencyScoreCalculationTypeId = s;

        this.name = n;
    }


    public static SimCompetencyScoreCalculationType getValue( int id )
    {
        SimCompetencyScoreCalculationType[] vals = SimCompetencyScoreCalculationType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencyScoreCalculationTypeId() == id )
                return vals[i];
        }

        return INDIVIDUALLY_EVEN;
    }


    public int getSimCompetencyScoreCalculationTypeId()
    {
        return simCompetencyScoreCalculationTypeId;
    }

    public String getName()
    {
        return name;
    }

}
