package com.tm2score.sim;

import java.util.Map;
import java.util.TreeMap;



public enum SimCompetencySortType
{
    NAME(0,"Names (Default)"),
    RANK(1,"Ranks"),
    DISPLAYORDER(2,"Display Order"),
    WEIGHT(3,"Weight");

    private final int simCompetencySortTypeId;

    private final String name;


    private SimCompetencySortType( int s , String n )
    {
        this.simCompetencySortTypeId = s;

        this.name = n;
    }

    public static Map<String,Integer> getMap()
    {
        Map<String,Integer> outMap = new TreeMap<>();

        SimCompetencySortType[] vals = SimCompetencySortType.values();

        //String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            outMap.put( vals[i].getName() , vals[i].getSimCompetencySortTypeId() );
        }

        return outMap;
    }


    public static SimCompetencySortType getValue( int id )
    {
        SimCompetencySortType[] vals = SimCompetencySortType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencySortTypeId() == id )
                return vals[i];
        }

        return NAME;
    }


    public int getSimCompetencySortTypeId()
    {
        return simCompetencySortTypeId;
    }

    public String getName()
    {
        return name;
    }

}
