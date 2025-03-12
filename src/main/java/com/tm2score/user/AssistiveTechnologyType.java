package com.tm2score.user;

import com.tm2score.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.faces.model.SelectItem;


public enum AssistiveTechnologyType
{
    NONE(0,"aatt.none"),
    SCREEN_READER(1, "aatt.screenreader" ),
    BRAILLE_DISPLAY(2, "aatt.brailledisplay" ),
    ALT_COMMS(3, "aatt.aac" ),
    SCREEN_MAGNIFICATION(4, "aatt.screenmagnifier" ),
    OTHER(99, "aatt.other" );

    private final int assistiveTechnologyTypeId;

    private final String key;

    private AssistiveTechnologyType( int typeId , String key )
    {
        this.assistiveTechnologyTypeId = typeId;
        this.key = key;
    }

    public static List<SelectItem> getSelectItemList( Locale locale, boolean noNone)
    {
        List<SelectItem>  outMap = new ArrayList<>();

        AssistiveTechnologyType[] vals = AssistiveTechnologyType.values();

        String name;

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( noNone && vals[i].equals(NONE))
                continue;
            
            name = MessageFactory.getStringMessage( locale, vals[i].getKey() , null );
            outMap.add( new SelectItem( vals[i].getAssistiveTechnologyTypeId(), name ) );
        }

        return outMap;
    }


    public static AssistiveTechnologyType getValue( int id )
    {
        AssistiveTechnologyType[] vals = AssistiveTechnologyType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getAssistiveTechnologyTypeId() == id )
                return vals[i];
        }

        return NONE;
    }
    
    
    public String getName( Locale locale )
    {
        return MessageFactory.getStringMessage( locale, key , null );
    }

    public int getAssistiveTechnologyTypeId()
    {
        return assistiveTechnologyTypeId;
    }

    public String getKey()
    {
        return key;
    }
}
