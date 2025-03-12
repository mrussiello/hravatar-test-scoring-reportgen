package com.tm2score.proctor;


public enum RemoteProctorEventStatusType
{
    /**
     0 = Event Not Completed
     10 = Event completed. 
     20 = Post Media Processing and Conversion Completed
     100 = Analysis Completed
     * 
     */
    EVENT_STARTED(0, "Event Started"),
    EVENT_COMPLETE(10, "Event Complete"),
    MEDIA_PROCESSING_COMPLETE(20, "Media Processing Complete"),
    IMAGE_COMPARISONS_COMPLETE(30, "Image Comparisons Complete"),
    ANALYSIS_COMPLETED(100, "Analysis Complete"),
    COMPLETED_MEDIA_ERRORS(101, "Complete Media Errors");

    private int remoteProctorEventStatusTypeId;

    private String key;

    private RemoteProctorEventStatusType( int typeId, String k )
    {
        remoteProctorEventStatusTypeId = typeId;
        key = k;
    }

    public boolean getCompleteOrHigher()
    {
        return remoteProctorEventStatusTypeId>=100; 
    }
    
    public boolean getEventCompleteOrHigher()
    {
        return remoteProctorEventStatusTypeId>=10; 
    }
    
    public int getRemoteProctorEventStatusTypeId()
    {
        return remoteProctorEventStatusTypeId;
    }

    public static RemoteProctorEventStatusType getValue( int id )
    {
    	RemoteProctorEventStatusType[] vals = RemoteProctorEventStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRemoteProctorEventStatusTypeId() == id )
                return vals[i];
        }

        return EVENT_STARTED;
    }

    public String getName()
    {
        return key;
    }
}
