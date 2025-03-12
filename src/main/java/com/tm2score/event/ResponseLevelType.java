package com.tm2score.event;

import com.tm2score.entity.event.ItemResponse;


public enum ResponseLevelType
{
    INTERACTION(0,"Interaction-Level", "i"),
    PREV_INTERACTION(1,"Previous Interaction Level", "pi" ),
    RADIOBUTTONGROUP(2,"Radio Button Group", "rb"),
    INTERACTIONITEM(3,"Interaction Item", "ii" );


    private final int responseLevelId;

    private String key;

    private String stub;


    private ResponseLevelType( int p , String key, String s )
    {
        this.responseLevelId = p;

        this.key = key;

        this.stub=s;
    }


    public String computeIdentifier( ItemResponse ir, int index )
    {
        String out;

        if( ir.getSimletNodeUniqueId() != null && !ir.getSimletNodeUniqueId().isEmpty() )
            out = ir.getSimletNodeUniqueId();

        else
            out = ir.getSimletAid() + "~" + ir.getSimletNodeSeq();

        out += "~" + getStub();

        if( index > 0 )
            out += "~" + index;

        return out;
    }

    public String getStub()
    {
        return stub;
    }

    public int getResponseLevelId()
    {
        return this.responseLevelId;
    }


    public String getKey()
    {
        return key;
    }


    public static ResponseLevelType getValue( int id )
    {
        ResponseLevelType[] vals = ResponseLevelType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getResponseLevelId() == id )
                return vals[i];
        }

        return INTERACTION;
    }

}
