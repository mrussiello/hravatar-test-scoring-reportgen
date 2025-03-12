package com.tm2score.simlet;

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.model.SelectItem;



public enum SimletCompetencyStatStatusType
{
    LIVE(0,"Live", "Live and Up-To-Date"),                
    TEMP(10,"Temp","A Temporary filler. Update ASAP");      // Manually created simlet maps.// Manually created simlet maps.


    private final int simletCompetencyStatStatusTypeId;

    private final String name;
    
    private final String shortName;


    private SimletCompetencyStatStatusType( int s, String shortNm , String n )
    {
        this.simletCompetencyStatStatusTypeId = s;
        this.shortName=shortNm;
        this.name = n;
    }

    public static List<SelectItem> getSelectItemList()
    {
        List<SelectItem> out = new ArrayList();

        out.add(new SelectItem( SimletCompetencyStatStatusType.LIVE.getSimletCompetencyStatStatusTypeId(), SimletCompetencyStatStatusType.LIVE.getName() ) );
        out.add(new SelectItem( SimletCompetencyStatStatusType.TEMP.getSimletCompetencyStatStatusTypeId(), SimletCompetencyStatStatusType.TEMP.getName() ) );

        return out;
    }


    public boolean getIsLive()
    {
        return equals(LIVE);
    }

    public boolean getIsTemp()
    {
        return equals(TEMP);
    }

    public boolean getNeedsUpdate()
    {
        return !equals(LIVE);
    }
    
    public static SimletCompetencyStatStatusType getValue( int id )
    {
        SimletCompetencyStatStatusType[] vals = SimletCompetencyStatStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimletCompetencyStatStatusTypeId() == id )
                return vals[i];
        }

        return LIVE;
    }


    public int getSimletCompetencyStatStatusTypeId()
    {
        return simletCompetencyStatStatusTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

}
