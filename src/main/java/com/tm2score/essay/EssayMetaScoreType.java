package com.tm2score.essay;

import com.tm2score.util.MessageFactory;
import java.util.Locale;



public enum EssayMetaScoreType
{
    NONE(0,"None", "emst.none"),
    CLARITY(1,"Clarity and Coherence", "emst.clarity"),
    ARGUMENT(2,"Argument Strength", "emst.argument"),
    MECHANICS(3,"Mechanics", "emst.mechanics");


    private final int essayMetaScoreTypeId;

    private final String name;
    private final String key;


    private EssayMetaScoreType( int s , String n, String k )
    {
        this.essayMetaScoreTypeId = s;
        this.name = n;
        this.key=k;
    }


    public static EssayMetaScoreType getValue( int id )
    {
        EssayMetaScoreType[] vals = EssayMetaScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getEssayMetaScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

    public String getKey()
    {
        return key;
    }

    public String getKeyX()
    {
        return key + "X";
    }


    public int getEssayMetaScoreTypeId()
    {
        return essayMetaScoreTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getName(Locale locale)
    {
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(locale, key);
    }
    
}
