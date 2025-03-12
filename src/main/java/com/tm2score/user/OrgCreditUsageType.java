package com.tm2score.user;


public enum OrgCreditUsageType
{
    CREDIT(0, "Credit"),
    RESULT_BASIC_PROC(1, "Candidate Credit - Basic Online Proctoring "),
    RESULT_NOPROCTORING(2, "Candidate Credit - No Online Proctoring"),
    RESULT_PREMIUM_PROC(3, "Candidate Credit - Premium Online Proctoring"),
    UNLIMITED_FULL(10, "Unlimited");

    private int orgCreditUsageTypeId;

    private String key;

    private OrgCreditUsageType( int typeId, String k )
    {
        orgCreditUsageTypeId = typeId;
        key = k;
    }

    //public boolean getBasicOnlineProctoringOk()
    //{
    //    return equals( CREDIT ) || equals(UNLIMITED) || equals(UNLIMITED_PREMIUM_PROC);        
    //}    

    //public boolean getPremiumOnlineProctoringOk()
    //{
    //    return equals( CREDIT ) || equals(UNLIMITED) || equals(UNLIMITED_PREMIUM_PROC);        
    //}    

    
    public boolean getUsesCredits()
    {
        return equals( CREDIT );
    }

    public boolean getUnlimited()
    {
        return equals( UNLIMITED_FULL );
    }

    public boolean getAnyResultCredit()
    {
        return equals(RESULT_BASIC_PROC ) || equals(RESULT_NOPROCTORING ) || equals(RESULT_PREMIUM_PROC);
    }


    public int getOrgCreditUsageTypeId()
    {
        return orgCreditUsageTypeId;
    }

    public static OrgCreditUsageType getValue( int id )
    {
        OrgCreditUsageType[] vals = OrgCreditUsageType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOrgCreditUsageTypeId() == id )
                return vals[i];
        }
        
        return CREDIT;
    }


    public String getKey()
    {
        return key;
    }
}
