package com.tm2score.user;

import java.util.Locale;


public enum LogonType
{
    USER(1,"logontype.userlogon"),
    NEW_REGISTRATION(4,"logontype.newregistration" ),
    SUPERUSER_LOGON(5,"logontype.superuser" ),
    COOKIE_AUTO(6,"logontype.cookieauto" ),
    NEWSLETTER_SUBUNSUB(7,"logontype.newslettersubunsub" ),
    PAYMENT_PROCESSING(8 , "logontype.paymentprocessing" ),
    CORP_LOGON(9 , "logontype.corp" );

    private int logonTypeId;

    private String key;

    private LogonType( int typeId , String key )
    {
        this.logonTypeId = typeId;

        this.key = key;
    }



    public String getName( Locale locale )
    {
        return key;
    }

    public int getLogonTypeId()
    {
        return logonTypeId;
    }

    public String getKey()
    {
        return key;
    }
}
