package com.tm2score.sim;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;



public enum SimCompetencyType
{
    ONET_ELEMENT(0,"O*Net Element"),    // When an ONET Soc is selected
    NON_ONET(1,"Not O*Net-Based"),
    ONET_GROUP(2,"O*Net Group");      // Manually created simlet maps.


    private final int simCompetencyTypeId;

    private final String name;


    private SimCompetencyType( int s , String n )
    {
        this.simCompetencyTypeId = s;

        this.name = n;
    }

    public boolean getIsOnet()
    {
        return equals( ONET_ELEMENT ) || equals( ONET_GROUP );
    }


    public static SimCompetencyType getValue( int id )
    {
        SimCompetencyType[] vals = SimCompetencyType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencyTypeId() == id )
                return vals[i];
        }

        return NON_ONET;
    }


    public int getSimCompetencyTypeId()
    {
        return simCompetencyTypeId;
    }


    public String getName()
    {
        return name;
    }

}
