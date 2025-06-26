package com.tm2score.ai;


public enum AiItemResponseProcessingType
{
    NONE(0, "None"),
    TEXT_2_PROMPT(1,"Evaluate Text to Prompt"),
    TEXT_2_QUESTION(2,"Evaluate Text to Question"),
    TEXT_2_IDEAL(3, "Evaluate Text to Question and Ideal Response");

    private final int aiItemResponseProcessingTypeId;
    private final String name;


    private AiItemResponseProcessingType( int p, String name )
    {
        this.aiItemResponseProcessingTypeId = p;
        this.name = name;
    }


    public int getAiItemResponseProcessingTypeId()
    {
        return this.aiItemResponseProcessingTypeId;
    }

    public String getName()
    {
        return name;
    }

    public static AiItemResponseProcessingType getValue( int id )
    {
        AiItemResponseProcessingType[] vals = AiItemResponseProcessingType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getAiItemResponseProcessingTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

}
