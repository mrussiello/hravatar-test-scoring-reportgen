package com.tm2score.sim;
import java.util.Locale;



public enum TrainingType
{
    NOT_SET(0,"Not set", "trntyp.none" ),
    TT_0_6M(1,"Less 6 Months", "trntyp.less6"),
    TT_6M_1(2,"6 Months - 1 Year", "trntyp.6mo-1yr"),
    TT_1_2(3,"1 - 2 Years", "trntyp.1-2yrs"),
    TT_2_4(4,"2 - 4 Years", "trntyp.2-4yrs"),
    TT_4_10(5,"4 - 10 Years", "trntyp.4-10yrs"),
    TT_10(6,"Over 10 Years", "trntyp.over10");


    private final int trainingTypeId;

    private final String name;

    private final String key;


    private TrainingType( int s , String n, String k )
    {
        this.trainingTypeId = s;

        this.name = n;

        this.key = k;
    }

    public String getKey() {
        return key;
    }


    public String getName( Locale locale )
    {
        if( locale == null )
            locale = Locale.US;

        return com.tm2score.util.MessageFactory.getStringMessage(locale, key );
    }





    public static TrainingType getValue( int id )
    {
        TrainingType[] vals = TrainingType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTrainingTypeId() == id )
                return vals[i];
        }

        return NOT_SET;
    }


    public int getTrainingTypeId()
    {
        return trainingTypeId;
    }

    public String getName()
    {
        return name;
    }

}
