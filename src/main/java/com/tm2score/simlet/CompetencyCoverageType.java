package com.tm2score.simlet;



public enum CompetencyCoverageType
{
    NONE(0,"0 - No Coverage"),
    LIGHT(1,"1 - Light Coverage"),
    LIGHT_MED(3,"3 - Light-Medium"),
    MEDIUM(5,"5 - Medium Coverage"),
    MED_SOLID(7,"4 - Medium-Solid"),
    SOLID(10,"10 - Solid Coverage");

    private final int competencyCoverageTypeId;

    private final String name;


    private CompetencyCoverageType( int s , String n )
    {
        this.competencyCoverageTypeId = s;

        this.name = n;
    }


    public static CompetencyCoverageType getValue( int id )
    {
        CompetencyCoverageType[] vals = CompetencyCoverageType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCompetencyCoverageTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getCompetencyCoverageTypeId()
    {
        return competencyCoverageTypeId;
    }

    public String getName()
    {
        return name;
    }

}
