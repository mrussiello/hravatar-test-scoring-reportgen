package com.tm2score.essay;


import java.util.Map;
import java.util.TreeMap;



public enum EssayPromptStatusType
{
    ACTIVE(0,"Active"),
    DISABLED(1,"Disabled");


    private final int essayPromptStatusTypeId;

    private final String name;


    private EssayPromptStatusType( int s , String n )
    {
        this.essayPromptStatusTypeId = s;

        this.name = n;
    }



    public static EssayPromptStatusType getValue( int id )
    {
        EssayPromptStatusType[] vals = EssayPromptStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getEssayPromptStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }


    public int getEssayPromptStatusTypeId()
    {
        return essayPromptStatusTypeId;
    }

    public String getName()
    {
        return name;
    }

}
