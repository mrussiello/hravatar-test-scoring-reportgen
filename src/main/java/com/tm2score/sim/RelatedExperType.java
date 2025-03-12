package com.tm2score.sim;
import java.util.Locale;



public enum RelatedExperType
{
    NOT_SET(0,"None" , "exptype.notset" ),
    RE_1(1,"Less Than 1 Year" , "exptype.less1"),
    RE_1_2(2,"1 - 2 Years" , "exptype.1-2yr"),
    RE_2_4(3,"2 - 4 Years" , "exptype.2-4yr"),
    RE_4_6(4,"4 - 6 Years" , "exptype.4-6yr"),
    RE_6_10(5,"6 - 10 Years" , "exptype.6-10yr"),
    RE_10(6,"Over 10 Years" , "exptype.over10");


    private final int relatedExperTypeId;

    private final String name;

    private final String key;


    private RelatedExperType( int s , String n, String k )
    {
        this.relatedExperTypeId = s;
        this.name = n;
        this.key=k;
    }


    public String getName( Locale locale )
    {
        if( locale == null )
            locale = Locale.US;

        return com.tm2score.util.MessageFactory.getStringMessage(locale, key );
    }

    public String getKey() {
        return key;
    }




    public static RelatedExperType getValue( int id )
    {
        RelatedExperType[] vals = RelatedExperType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRelatedExperTypeId() == id )
                return vals[i];
        }

        return NOT_SET;
    }


    public int getRelatedExperTypeId()
    {
        return relatedExperTypeId;
    }

    public String getName()
    {
        return name;
    }

}
