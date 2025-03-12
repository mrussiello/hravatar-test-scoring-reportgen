package com.tm2score.act;

import java.util.ArrayList;
import java.util.List;

import jakarta.faces.model.SelectItem;


public enum IframeItemType
{
    CHAT(0,"Chat" );

    private final int iframeItemTypeId;

    private final String name;

    private IframeItemType( int s , String n )
    {
        this.iframeItemTypeId = s;

        this.name = n;
    }


    public static List<SelectItem> getSelectItemList()
    {
        List<SelectItem> out = new ArrayList<>();

        IframeItemType[] vals = IframeItemType.values();

        //String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            out.add( new SelectItem( new Integer( vals[i].getIframeItemTypeId() ), vals[i].getName() ) );
        }

        return out;
    }



    public static IframeItemType getValue( int id )
    {
        IframeItemType[] vals = IframeItemType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getIframeItemTypeId() == id )
                return vals[i];
        }

        return CHAT;
    }


    public int getIframeItemTypeId()
    {
        return iframeItemTypeId;
    }

    public String getName()
    {
        return name;
    }

}
