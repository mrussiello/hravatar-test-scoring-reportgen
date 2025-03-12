package com.tm2score.custom.coretest2.cefr;

import com.tm2score.util.MessageFactory;
import java.util.Locale;



public enum CefrType
{
    NONE(0, "None", "na"),
    LISTENING(1, "Listening", "listen"),
    READING(2, "Reading", "reading");


    private final int cefrTypeId;

    private final String name;
    private final String stub;


    private CefrType( int id, String n , String s )
    {
        this.cefrTypeId = id;
        this.name = n;
        this.stub=s;
    }

    public String getStub() {
        return stub;
    }


    public static CefrType getValue( int id )
    {
        CefrType[] vals = CefrType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCefrTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getCefrTypeId()
    {
        return cefrTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getName( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(locale, "cefr." + stub);
    }
    
    
    
}
