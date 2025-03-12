package com.tm2score.profile;

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.model.SelectItem;


public enum ProfileStatusType
{
    INACTIVE(0, "Inactive"),
    ACTIVE(1, "Active");

    private int profieStatusTypeId;

    private String key;

    private ProfileStatusType( int typeId, String k )
    {
        profieStatusTypeId = typeId;
        key = k;
    }


    public int getProfileStatusTypeId()
    {
        return profieStatusTypeId;
    }

    public static ProfileStatusType getValue( int id )
    {
        for( ProfileStatusType val : ProfileStatusType.values() )
        {
            if( id == val.getProfileStatusTypeId())
                return val;
        }

        return INACTIVE;
    }

    public boolean isActive()
    {
        return equals( ACTIVE );
    }


    public static List<SelectItem> getSelectItemList()
    {
        List<SelectItem> outMap = new ArrayList<>();

        ProfileStatusType[] vals = ProfileStatusType.values();

       for( int i=0 ; i<vals.length ; i++ )
        {
            outMap.add(new SelectItem( new Integer( vals[i].getProfileStatusTypeId() ), vals[i].key )  );
        }

        return outMap;
    }



    public String getKey()
    {
        return key;
    }
}
