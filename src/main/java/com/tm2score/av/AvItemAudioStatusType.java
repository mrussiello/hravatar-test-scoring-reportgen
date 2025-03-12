package com.tm2score.av;

public enum AvItemAudioStatusType
{
    NOT_PRESENT(0, "No Audio" ),
    RECORDED(1, "Audio recorded, not retrieved, not ready for retrieval" ),
    READY_REMOTELY(2, "Audio recorded, ready for retrieval" ),
    STORED_LOCALLY_READY_NOT_DELETED_REMOTELY(10, "Audio Retrieved and stored in HRA database. Source deleted. Ready for Speech to Text" ),
    STORED_LOCALLY_DELETED_REMOTELY(20, "Audio Retrieved and stored in HRA database. Source deleted." ),
    NOT_STORED_LOCALLY_DELETED_REMOTELY(30, "Audio disgarded because no longer needed." );

    private final int audioStatusTypeId;

    private String key;


    private AvItemAudioStatusType( int p,
                         String key )
    {
        this.audioStatusTypeId = p;
        this.key = key;
    }

    public boolean isPresent()
    {
        return !equals(NOT_PRESENT);
    }
    
    public boolean isPendingFromSource()
    {
        return equals(RECORDED);
    }
   
    public boolean isStoredLocally()
    {
        return equals( STORED_LOCALLY_DELETED_REMOTELY ) || equals( STORED_LOCALLY_READY_NOT_DELETED_REMOTELY );
    }
    
    public boolean isDeletedRemotely()
    {
        return equals( STORED_LOCALLY_DELETED_REMOTELY ) || equals( NOT_STORED_LOCALLY_DELETED_REMOTELY );
    }
    
    public String getName()
    {
        return this.key;
    }

    public int getAudioStatusTypeId() {
        return audioStatusTypeId;
    }
    
    public static AvItemAudioStatusType getValue( int id )
    {
        AvItemAudioStatusType[] vals = AvItemAudioStatusType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getAudioStatusTypeId() == id )
                return vals[i];
        }

        return NOT_PRESENT;
    }
      
}
