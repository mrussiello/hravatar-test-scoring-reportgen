/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ct5;

/**
 *
 * @author miker_000
 */
public enum Ct5ItemResponseStatusType {

    ACTIVE(0,"Active"),
    TEMP_STORED(10,"Response Values Temporarily Stored in Memory"),
    COMPLETE(100,"Complete");

    private final int ct5ItemResponseStatusTypeId;

    private final String key;


    private Ct5ItemResponseStatusType( int s , String n )
    {
        this.ct5ItemResponseStatusTypeId = s;

        this.key = n;
    }

    public boolean getIsTempStord()
    {
        return equals(TEMP_STORED);
    }
    
    public boolean getIsComplete()
    {
        return equals(COMPLETE);
    }
    
    public boolean getTempStoredOrHigher()
    {
        return ct5ItemResponseStatusTypeId >= TEMP_STORED.getCt5ItemStatusTypeId();
    }
    
    public static Ct5ItemResponseStatusType getValue( int id )
    {
        Ct5ItemResponseStatusType[] vals = Ct5ItemResponseStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCt5ItemStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }


    public int getCt5ItemStatusTypeId()
    {
        return ct5ItemResponseStatusTypeId;
    }

    
    public String getName()
    {
        return key;
    }
    
}
