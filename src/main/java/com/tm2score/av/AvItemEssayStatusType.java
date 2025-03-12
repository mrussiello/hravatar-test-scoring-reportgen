package com.tm2score.av;

public enum AvItemEssayStatusType
{
    NOT_SET(0, "Not set" ),
    NOT_REQUESTED(1, "Not Requested" ),
    REQUESTED(2, "Requested" ),
    COMPLETE(3, "Completed and Successful" ),
    NOT_REQUIRED(9, "Not required" ),
    ERROR(10, "Unsuccessful, Errored" );

    private final int essayStatusTypeId;

    private String key;


    private AvItemEssayStatusType( int p,
                         String key )
    {
        this.essayStatusTypeId = p;
        this.key = key;
    }

    public boolean requiresEssayScore()
    {
        return !equals(NOT_SET) && !equals(NOT_REQUIRED);
    }
    
    public boolean isCompleteOrPermanentError()
    {
        return equals(COMPLETE) || isError();                
    }
    
    public boolean isNotRequired()
    {
        return equals(NOT_REQUIRED);           
    }
    
    public boolean isComplete()
    {
        return equals(COMPLETE);                
    }

    public boolean isError()
    {
        return equals(ERROR);                
    }
        
    public boolean isUnset()
    {
        return equals(NOT_SET);                
    }
    
    public boolean isRequested()
    {
        return equals(REQUESTED);                
    }
    
   
    public String getName()
    {
        return this.key;
    }

    public int getEssayStatusTypeId() {
        return essayStatusTypeId;
    }
    
    public static AvItemEssayStatusType getValue( int id )
    {
        AvItemEssayStatusType[] vals = AvItemEssayStatusType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getEssayStatusTypeId() == id )
                return vals[i];
        }

        return NOT_SET;
    }
      
}
