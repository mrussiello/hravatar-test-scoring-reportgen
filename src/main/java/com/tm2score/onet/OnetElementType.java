package com.tm2score.onet;

import java.util.Map;
import java.util.TreeMap;



public enum OnetElementType
{
    UNKNOWN(0,"Unknown"),
    ABILITY(1,"Ability"),
    KNOWLEDGE(2,"Knowledge"),
    SKILL(3,"Skill"),
    WK_STYLE(4, "Work Style"),
    WK_ACTIVITY(5, "Work Activity"),
    TASK(6, "Task" ),
    ELEMENTGROUP(7, "Group"),
    INTEREST(8,"Interest"),
    WK_CONTEXT(9, "Work Context");


    private final int onetElementTypeId;

    private final String name;


    private OnetElementType( int s , String n )
    {
        this.onetElementTypeId = s;

        this.name = n;
    }


    public static OnetElementType getForElementId( String onetElementId )
    {
        if( onetElementId == null || onetElementId.isEmpty() )
            return UNKNOWN;

        onetElementId = onetElementId.toLowerCase();

        if( onetElementId.startsWith( "1.a" ) )
            return ABILITY;

        else if( onetElementId.startsWith( "2.c" ) )
            return KNOWLEDGE;

        else if( onetElementId.startsWith( "2.a" ) || onetElementId.startsWith( "2.b" ) )
            return SKILL;

        else if( onetElementId.startsWith( "1.c" ) )
            return WK_STYLE;

        return UNKNOWN;
    }



    public static Map<String,Integer> getMap()
    {
        Map<String,Integer> outMap = new TreeMap<>();

        OnetElementType[] vals = OnetElementType.values();

        //String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            outMap.put( vals[i].getName() , new Integer( vals[i].getOnetElementTypeId() ) );
        }

        return outMap;
    }




    public static OnetElementType getValue( int id )
    {
        OnetElementType[] vals = OnetElementType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOnetElementTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }


    public int getOnetElementTypeId()
    {
        return onetElementTypeId;
    }

    public String getName()
    {
        return name;
    }

}
