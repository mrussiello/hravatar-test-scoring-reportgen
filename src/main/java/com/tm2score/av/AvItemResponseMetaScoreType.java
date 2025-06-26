package com.tm2score.av;

import com.tm2score.util.MessageFactory;
import java.util.Locale;



public enum AvItemResponseMetaScoreType
{
    NONE(0,"None", "avirmst.none"),
    CLARITY(1,"Clarity", "avirmst.clarity"), // Is the answer easy to understand? Is the language clear and concise?
    RELEVANCE(2,"Relevance to Question", "avirmst.relevance"), // Does the answer directly address the question and provide relevant information?
    COMPLETENESS(3,"Completeness", "avirmst.completeness"); // Does the answer provide all the necessary information to fully answer the question, or are there gaps?


    private final int avItemResponseMetaScoreTypeId;

    private final String name;
    private final String key;


    private AvItemResponseMetaScoreType( int s , String n, String k )
    {
        this.avItemResponseMetaScoreTypeId = s;
        this.name = n;
        this.key=k;
    }


    public static AvItemResponseMetaScoreType getValue( int id )
    {
        AvItemResponseMetaScoreType[] vals = AvItemResponseMetaScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getAvItemResponseMetaScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getAvItemResponseMetaScoreTypeId()
    {
        return avItemResponseMetaScoreTypeId;
    }

    public String getName()
    {
        return getName( Locale.US );
    }
    
    public String getName( Locale loc )
    {
        if( loc==null )
            loc = Locale.US;
        return MessageFactory.getStringMessage( loc, key );
    }

}
