package com.tm2score.event;


public enum ItemResponseType
{
    ANSWERED(0,"Answered, Incorrect or Not Scored"),
    ANSWERED2(1,"Answered Correctly"),
    NOT_ANSWERED(-1,"Not Answered"),
    NOT_ANSWERED_TIMEOUT(-2,"Timed Out");


    private final int itemResponseTypeId;

    private final String key;


    private ItemResponseType( int p , String key )
    {
        this.itemResponseTypeId = p;

        this.key = key;
    }


    public int getItemResponseTypeId()
    {
        return this.itemResponseTypeId;
    }


    public String getKey()
    {
        return key;
    }


    public static ItemResponseType getValue( int id )
    {
        ItemResponseType[] vals = ItemResponseType.values();

        for( int i=0; i<vals.length; i++ )
        {
            if( vals[i].getItemResponseTypeId() == id )
                return vals[i];
        }

        return NOT_ANSWERED;
    }

}
