package com.tm2score.user;

import com.tm2score.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public enum GeographicRegionType
{
    AFRICA(1, "georegion.africa"),
    NORTHAMERICA(2, "georegion.northamerica"),
    LATINAMERICA(3, "georegion.latinamerica"),
    SOUTHAMERICA(4, "georegion.southamerica"),
    CENTRALASIA(5, "georegion.centralasia"),
    SOUTHEASTASIA(6, "georegion.southeastasia"),
    WESTERNASIA(7, "georegion.westasia"),
    OCEANIA(8, "georegion.oceania"),
    EASTERNEUROPE(9, "georegion.easteurope"),
    WESTERNEUROPE(10, "georegion.westeurope"),
    RUSSIA(11, "georegion.russia"),
    MIDDLE_EAST(12, "georegion.middleeast");

    private final int geographicRegionTypeId;

    private String key;

    private GeographicRegionType( int p, String key )
    {
        this.geographicRegionTypeId = p;

        this.key = key;
    }

    public boolean getIsNorthAmerican()
    {
        return geographicRegionTypeId==2;
    }

    public int getGeographicRegionTypeId()
    {
        return this.geographicRegionTypeId;
    }

    public static List<GeographicRegionType> getTypeList()
    {
        List<GeographicRegionType> typeList = new ArrayList<>();

        GeographicRegionType[] vals = GeographicRegionType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            typeList.add( vals[i] );
        }

        return typeList;
    }




    public String getGroupName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key + ".group", null );
    }

    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key, null );
    }

    public static GeographicRegionType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }

    public static GeographicRegionType getValue( int id )
    {
        GeographicRegionType[] vals = GeographicRegionType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getGeographicRegionTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
