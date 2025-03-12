package com.tm2score.event;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public enum TestKeySourceType
{
    DEFAULT(0,"Default"),
    ORGAUTOTEST(1,"OrgAutoTest"),
    DIRECT(2,"Direct"),
    API(3,"API"),
    ADMINTESTING(4,"Admin Testing on test site"),
    LOGONPORTALREG(5,"Logon Portal Registration"),
    OPTIONALAUTOTEST(6, "Optional AutoTest");

    private final int testKeySourceTypeId;

    private String key;


    private TestKeySourceType( int p , String key )
    {
        this.testKeySourceTypeId = p;

        this.key = key;
    }

    public String getKey() {
        return key;
    }


    public boolean isApi()
    {
        return equals( API );
    }

    public int getTestKeySourceTypeId()
    {
        return this.testKeySourceTypeId;
    }

    public static TestKeySourceType getValue( int id )
    {
        TestKeySourceType[] vals = TestKeySourceType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTestKeySourceTypeId() == id )
                return vals[i];
        }

        return DEFAULT;
    }

}
