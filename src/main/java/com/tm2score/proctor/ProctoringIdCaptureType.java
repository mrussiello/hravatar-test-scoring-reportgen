package com.tm2score.proctor;


public enum ProctoringIdCaptureType
{
    NONE(0,"None"),
    CAPTURE_ONLY(5,"Capture Only"),
    CAPTURE_VERIFY(10,"Capture And Verify"),
    CAPTURE_SIDE_BY_SIDE_VERIFY(11,"Capture with Side-by-Side Verify"),
    CAPTURE_VERIFY_SAFE(12,"Capture and Verify Safe"),
    CAPTURE_SIDE_BY_SIDE_VERIFY_SAFE(13,"Capture and Verify Side-by-Side Safe");

    private int proctoringIdCaptureTypeId;

    private String key;

    private ProctoringIdCaptureType( int typeId , String key )
    {
        this.proctoringIdCaptureTypeId = typeId;

        this.key = key;
    }
    
    public boolean getIsEnabled()
    {
        return !equals( NONE );
    }
    
    public boolean getRequiresIdVerification()
    {        
        return equals( CAPTURE_VERIFY );
    }
        
    

    public static ProctoringIdCaptureType getValue( int id )
    {
        ProctoringIdCaptureType[] vals = ProctoringIdCaptureType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getProctoringIdCaptureTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

    public String getName()
    {
        return key;
    }


    public int getProctoringIdCaptureTypeId()
    {
        return proctoringIdCaptureTypeId;
    }

    public String getKey()
    {
        return key;
    }
}
