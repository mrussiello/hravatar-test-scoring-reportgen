package com.tm2score.event;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public enum TestKeyAdminType
{
    PORTAL(0,"Portal"),
    DIRECT(10,"Direct Test");

    private final int testKeyAdminTypeId;

    private String key;


    private TestKeyAdminType( int p , String key )
    {
        this.testKeyAdminTypeId = p;

        this.key = key;
    }


    public int getTestKeyAdminTypeId()
    {
        return this.testKeyAdminTypeId;
    }

    public static TestKeyAdminType getValue( int id )
    {
        TestKeyAdminType[] vals = TestKeyAdminType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTestKeyAdminTypeId() == id )
                return vals[i];
        }

        return PORTAL;
    }

}
