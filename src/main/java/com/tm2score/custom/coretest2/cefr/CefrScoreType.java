package com.tm2score.custom.coretest2.cefr;

import com.tm2score.util.MessageFactory;
import java.util.Locale;



public enum CefrScoreType
{
    UNKNOWN(0,"", "Unknown", "cefr.unknown"),
    PREA1(1, "PreA1", "Pre Beginner", "cefr.prea1"),
    A1(2, "A1","Beginner", "cefr.a1"),
    A2(3, "A2","Elementary", "cefr.a2"),
    B1(4, "B1","Intermediate", "cefr.b1"),
    B2(5, "B2","Upper Intermediate", "cefr.b2"),
    C1(6, "C1","Advanced", "cefr.c1"),
    C2(7, "C2","Proficiency", "cefr.c2");


    private final int cefrScoreTypeId;

    private final String name;
    private final String textVal;
    private final String key;


    private CefrScoreType( int s, String t , String n, String k )
    {
        this.cefrScoreTypeId = s;
        this.textVal=t;
        this.name = n;
        this.key=k;
    }

    public String getTextVal() {
        return textVal;
    }

    
    
    public static CefrScoreType getFromText( String text )
    {
        CefrScoreType[] vals = CefrScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].textVal.equalsIgnoreCase(text) )
                return vals[i];
        }

        return UNKNOWN;
    }

    
    public static CefrScoreType getValueForCaveat( String caveat, Locale locale )
    {
        CefrScoreType[] vals = CefrScoreType.values();

        // check name or for (XX)
        for( int i=0; i<vals.length ; i++ )
        {
            if( caveat.contains( "~" + vals[i].name) || caveat.contains( "(" + vals[i].textVal + ")") )
                return vals[i];            
        }

        // check localized name
        for( int i=0 ; i<vals.length ; i++ )
        {
            if( caveat.contains( "~" + MessageFactory.getStringMessage(locale, vals[i].key) ) )
                return vals[i];
        }

        return PREA1;
    }

    public static CefrScoreType getValue( int id )
    {
        CefrScoreType[] vals = CefrScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCefrScoreTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }


    public int getCefrScoreTypeId()
    {
        return cefrScoreTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getName( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(locale, key);
    }
    
    public String getDescription( Locale locale, String stub )
    {
        if( locale==null )
            locale=Locale.US;
        
        if( stub==null )
            stub="";
        if( !stub.startsWith("."))
            stub = "." + stub;
        
        return MessageFactory.getStringMessage(locale, key + ".descrip" + stub);
    }
    
    
}
