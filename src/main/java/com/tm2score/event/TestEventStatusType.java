package com.tm2score.event;


public enum TestEventStatusType
{
    ACTIVE(0,"Active"),
    STARTED(1,"Started"),
    COMPLETED(100,"Completed"),
    COMPLETED_PENDING_EXTERNAL_SCORES(101,"Pending External Scores"),
    SCORING_STARTED(105,"Scoring Started"),
    SCORED(110,"Scored"),
    REPORT_STARTED(111,"Reports Started"),
    REPORT_COMPLETE(120,"Report Complete"),
    // REPORT_COMPLETE_NONORMS(121,"Report Complete, Exclude From Norms"),
    EXPIRED(201,"Expired"),
    DEACTIVATED(202,"Deactivated"),
    SCORE_ERROR(203,"Scoring Error" ),
    REPORT_ERROR(204,"Report Error" );

    private final int testEventStatusTypeId;

    private String key;


    private TestEventStatusType( int p , String key )
    {
        this.testEventStatusTypeId = p;

        this.key = key;
    }


    public boolean getIsError()
    {
        return equals( EXPIRED ) || equals( DEACTIVATED ) || equals( SCORE_ERROR ) || equals( REPORT_ERROR );
    }


    public boolean getIsReportsCompleteOrHigher()
    {
        return equals( REPORT_COMPLETE );
    }

    public boolean getIsComplete()
    {
        return equals( COMPLETED );
    }

    public boolean getIsCompleteOrPendingExternal()
    {
        return equals( COMPLETED ) || equals( COMPLETED_PENDING_EXTERNAL_SCORES );
    }
    
    public boolean getIsExpired()
    {
        return equals( EXPIRED );
    }
    
    public boolean getIsCompleteOrHigher()
    {
        return this.getTestEventStatusTypeId()>= COMPLETED.getTestEventStatusTypeId();
    }

    public boolean getIsScored()
    {
        return this.getTestEventStatusTypeId()== SCORED.getTestEventStatusTypeId();
    }
    
    public boolean getIsScoredOrHigher()
    {
        return this.getTestEventStatusTypeId()>= SCORED.getTestEventStatusTypeId();
    }
    
    public boolean getIsScoredOrHigherNoErrors()
    {
        return getTestEventStatusTypeId()>= SCORED.getTestEventStatusTypeId() && getTestEventStatusTypeId()<EXPIRED.getTestEventStatusTypeId();
    }
    
    
    public boolean getIsDeactivated()
    {
        return equals( DEACTIVATED );
    }
    
    public boolean getIsActiveOrCompleted()
    {
        return equals( STARTED ) || equals( ACTIVE ) || equals( COMPLETED );
    }

    public boolean getIsActive()
    {
        return equals( STARTED ) || equals( ACTIVE );
    }


    public int getTestEventStatusTypeId()
    {
        return this.testEventStatusTypeId;
    }




    public static TestEventStatusType getType( int typeId )
    {
        return getValue( typeId );
    }



    public String getKey()
    {
        return key;
    }



    public static TestEventStatusType getValue( int id )
    {
        TestEventStatusType[] vals = TestEventStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTestEventStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }

}
