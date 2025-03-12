package com.tm2score.proctor;


public enum SuspiciousKeyCodeType
{
    UNKNOWN(0, "Unknown"),
    CTRL(17, "CTRL"),
    ALT(18, "ALT"),
    WIN(91, "WIN/CMD"),
    PRNTSCRN(44, "PRNTSCRN");

    private int suspiciousKeyCodeTypeId;

    private String key;

    private SuspiciousKeyCodeType( int typeId, String k )
    {
        suspiciousKeyCodeTypeId = typeId;
        key = k;
    }

    public boolean getIsUnknown()
    {
        return equals(UNKNOWN);
    }
    
    public int getSuspiciousKeyCodeTypeId()
    {
        return suspiciousKeyCodeTypeId;
    }

    public static SuspiciousKeyCodeType getValue( int id )
    {
    	SuspiciousKeyCodeType[] vals = SuspiciousKeyCodeType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSuspiciousKeyCodeTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }

    public String getName()
    {
        return key;
    }
}
