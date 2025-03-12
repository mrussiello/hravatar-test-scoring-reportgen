package com.tm2score.onet;

import java.util.Map;
import java.util.TreeMap;



public enum OnetTaskFreqType
{
    UNKNOWN(0,"Unknown"),
    YEARLY_LESS(1,"Yearly or less"),
    YEARLY(2,"More than yearly"),
    MONTHLY(3,"More than monthly"),
    WEEKLY(4, "More than Weekly"),
    DAILY(5, "Daily"),
    MORE_DAILY(6, "Several times Daily"),
    MORE_HOURLY(7, "Hourly or more");


    private final int onetTaskFreqTypeId;

    private final String name;


    private OnetTaskFreqType( int s , String n )
    {
        this.onetTaskFreqTypeId = s;

        this.name = n;
    }



    public static Map<String,Integer> getMap()
    {
        Map<String,Integer> outMap = new TreeMap<String,Integer>();

        OnetTaskFreqType[] vals = OnetTaskFreqType.values();

        //String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            outMap.put( vals[i].getName() , new Integer( vals[i].getOnetTaskFreqTypeId() ) );
        }

        return outMap;
    }




    public static OnetTaskFreqType getValue( int id )
    {
        OnetTaskFreqType[] vals = OnetTaskFreqType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOnetTaskFreqTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }


    public int getOnetTaskFreqTypeId()
    {
        return onetTaskFreqTypeId;
    }

    public String getName()
    {
        return name;
    }

}
