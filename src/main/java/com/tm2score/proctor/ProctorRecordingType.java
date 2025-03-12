package com.tm2score.proctor;

import com.tm2score.file.MediaType;



public enum ProctorRecordingType
{
    NONE(0, "None"),
    IMAGES(0, "Images"),
    AUDIO(10, "Audio"),
    VIDEO(20, "Video");

    private int proctorRecordingTypeId;

    private String key;

    private ProctorRecordingType( int typeId, String k )
    {
        proctorRecordingTypeId = typeId;
        key = k;
    }
    
    public boolean getRequiresOvConnection()
    {
        return equals( VIDEO ) || equals( AUDIO ); //  || equals( IMAGES );
    }
    
    public boolean getRequiresDirectImagesOnly()
    {
        return equals( IMAGES );
    }
    
    public boolean getRequiresOvRecording()
    {
        return equals( VIDEO ) || equals( AUDIO );
    }
    
    public boolean getHasVideo()
    {
        return equals( VIDEO );
    }

    public boolean getHasAudio()
    {
        return equals( VIDEO ) || equals( AUDIO );
    }
        
    
    
    public MediaType getMediaType()
    {
        if( equals(NONE) )
            return MediaType.TEXT;
        if( equals(IMAGES) )
            return MediaType.IMAGE;
        if( equals( AUDIO ) )
            return MediaType.AUDIO;
        return MediaType.VIDEO;
    }
    
    public int getProctorRecordingTypeId()
    {
        return proctorRecordingTypeId;
    }

    public static ProctorRecordingType getValue( int id )
    {
    	ProctorRecordingType[] vals = ProctorRecordingType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getProctorRecordingTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

    public String getName()
    {
        return key;
    }
}
