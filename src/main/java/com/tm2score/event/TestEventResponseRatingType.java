package com.tm2score.event;

import com.tm2score.entity.event.TestEventResponseRating;



public enum TestEventResponseRatingType
{
    UPLOADEDUSERFILE(0,"Uploaded User File", "Misc File" ),
    AVITEMRESPONSE(1,"AV Item Response", "Audio/Video"),
    SIMCOMPETENCY(2,"Sim Competency", "Scored Input"),
    NONCOMPETENCY(3,"NON Competency", "Unscored Input");

    private final int testEventResponseRatingTypeId;

    private String key;
    private String name;


    private TestEventResponseRatingType( int p , String key, String n )
    {
        this.testEventResponseRatingTypeId = p;

        this.key = key;
        this.name = n;
    }
    
    public boolean getIsUploadedUserFile()
    {
        return equals( UPLOADEDUSERFILE );
    }

    public boolean getIsAvItemResponse()
    {
       return equals( AVITEMRESPONSE );
    }

    public boolean getIsSimCompetency()
    {
        return equals( SIMCOMPETENCY );
    }

    public boolean getIsNonCompetency()
    {
        return equals( NONCOMPETENCY );
    }

    public int getTestEventResponseRatingTypeId()
    {
        return this.testEventResponseRatingTypeId;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public static TestEventResponseRatingType getType( int typeId )
    {
        return getValue( typeId );
    }

    public static TestEventResponseRatingType getValue( int id )
    {
        TestEventResponseRatingType[] vals = TestEventResponseRatingType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTestEventResponseRatingTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
