package com.tm2score.user;

import java.util.Map;
import java.util.TreeMap;



public enum OrgAutoTestStatusType
{
    INACTIVE(0,"Inactive"),
    ACTIVE(1,"Active");


    private final int orgAutoTestStatusTypeId;

    private String key;


    private OrgAutoTestStatusType( int id , String key )
    {
        this.orgAutoTestStatusTypeId = id;

        this.key = key;
    }


    public boolean getIsActive()
    {
        return equals( ACTIVE );
    }


    public int getOrgAutoTestStatusTypeId()
    {
        return this.orgAutoTestStatusTypeId;
    }


    public static OrgAutoTestStatusType getValue( int id )
    {
        OrgAutoTestStatusType[] vals = OrgAutoTestStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOrgAutoTestStatusTypeId() == id )
                return vals[i];
        }

        return INACTIVE;
    }


    public String getKey()
    {
        return key;
    }

}
