package com.tm2score.event;


public enum TestEventScoreStatusType
{
    ACTIVE(0,"Active"),
    REPORT_ERROR(1,"Report Error"),
    REPORT_ARCHIVED(10,"Report Archived (Needs Regen)");

    private final int testEventScoreStatusTypeId;

    private String key;


    private TestEventScoreStatusType( int p , String key )
    {
        this.testEventScoreStatusTypeId = p;

        this.key = key;
    }


    public boolean getIsReportArchived()
    {
        return equals( REPORT_ARCHIVED );
    }

    public boolean getIsReportError()
    {
        return equals( REPORT_ERROR );
    }


    public boolean getIsActive()
    {
        return equals( ACTIVE );
    }


    public int getTestEventScoreStatusTypeId()
    {
        return this.testEventScoreStatusTypeId;
    }




    public static TestEventScoreStatusType getType( int typeId )
    {
        return getValue( typeId );
    }



    public String getKey()
    {
        return key;
    }



    public static TestEventScoreStatusType getValue( int id )
    {
        TestEventScoreStatusType[] vals = TestEventScoreStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTestEventScoreStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }

}
