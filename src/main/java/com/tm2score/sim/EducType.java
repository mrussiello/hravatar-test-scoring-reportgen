package com.tm2score.sim;

import com.tm2score.util.MessageFactory;
import java.util.Locale;



public enum EducType
{
    NOT_SET(0,"Unknown", "eductype.notset" ),
    NONE(1,"None", "eductype.none"),
    HIGH_SCHOOL(2,"High School", "eductype.hs"),
    ASSCOCIATES(3,"Some College or Associate's Degree", "eductype.assoc"),
    BACHELORS(4,"Bachelor's Degree", "eductype.bach"),
    MASTERS(5,"Master's Degree", "eductype.masters"),
    PHD(6,"Doctoral Degree or Higher", "eductype.phd");


    private final int educTypeId;

    private final String name;

    private final String key;


    private EducType( int s , String n, String k )
    {
        this.educTypeId = s;

        this.name = n;

        this.key = k;
    }

    public String getName( Locale locale )
    {
        if( locale == null )
            locale = Locale.US;

        return MessageFactory.getStringMessage(locale, key );
    }

    
    public String getKey() {
        return key;
    }




    public static EducType getValue( int id )
    {
        EducType[] vals = EducType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getEducTypeId() == id )
                return vals[i];
        }

        return NOT_SET;
    }


    public int getEducTypeId()
    {
        return educTypeId;
    }

    public String getName()
    {
        return name;
    }

}
