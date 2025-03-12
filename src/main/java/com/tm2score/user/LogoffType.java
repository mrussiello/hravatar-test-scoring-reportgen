package com.tm2score.user;


public enum LogoffType
{
    USER(1,"logofftype.userlogoff"),
    USER_REPLACEMENT(2,"logofftype.userreplacement"),
    SESSION_EXPIRE(3, "logofftype.sessionexpire" ),
    CORP_LOGON(4,"logofftype.corplogon"),
    CORP_LOGOUT(5,"logofftype.corplogoff");

    private int logoffTypeId;

    private String key;

    private LogoffType( int typeId , String key )
    {
        this.logoffTypeId = typeId;

        this.key = key;
    }


    public int getLogoffTypeId()
    {
        return logoffTypeId;
    }

    public String getKey()
    {
        return key;
    }
}
