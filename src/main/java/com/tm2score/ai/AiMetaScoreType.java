package com.tm2score.ai;

import com.tm2score.util.MessageFactory;
import java.util.Locale;

public enum AiMetaScoreType {
    NONE(0, "mstp.none"),
    ORGTRAITS(1, "mstp.orgtrts"),
    ORGCOMPS(2, "mstp.orgcomps"),
    JOBDESCRIP(3, "mstp.jobdescrip");

    private final int aiMetaScoreTypeId;
    private final String key;

    private AiMetaScoreType(int p, String key)
    {
        this.aiMetaScoreTypeId = p;
        this.key = key;
    }

    public int getAiMetaScoreTypeId()
    {
        return this.aiMetaScoreTypeId;
    }

    public String getNameForReport(Locale locale, String[] strParams)
    {
        if (locale == null)
            locale = Locale.US;

        if( strParams==null || strParams.length<=0 )   
            return MessageFactory.getStringMessage(locale, key);
        
        return MessageFactory.getStringMessage(locale, key + ".full", strParams );        
    }
    
    
    public String getName(Locale locale)
    {
        if (locale == null)
            locale = Locale.US;

        return MessageFactory.getStringMessage(locale, key);
    }

    public String getDescription(Locale locale)
    {
        if (locale == null)
            locale = Locale.US;

        return MessageFactory.getStringMessage(locale, key + ".descrip");
    }

    public String getName()
    {
        return getName(Locale.US);
    }

    public String getKey()
    {
        return key;
    }

    public static AiMetaScoreType getValue(int id)
    {
        AiMetaScoreType[] vals = AiMetaScoreType.values();

        for (int i = 0; i < vals.length; i++)
        {
            if (vals[i].getAiMetaScoreTypeId() == id)
                return vals[i];
        }

        return NONE;
    }

}
