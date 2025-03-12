package com.tm2score.global;

public class WebServiceUtils
{

    private static String webServicesAuthId = null;

    private static String webServicesAuthCode = null;


    public static boolean authenticate( String authId , String authCode )
    {
        if( webServicesAuthId == null )
            webServicesAuthId = RuntimeConstants.getStringValue( "webServicesAuthId" );

        if( webServicesAuthCode == null )
            webServicesAuthCode = RuntimeConstants.getStringValue( "webServicesAuthCode" );

        if( authId == null || authId.length() == 0 )
            return false;

        if( authCode == null || authCode.length() == 0 )
            return false;

        if( authId.equals( webServicesAuthId ) && authCode.equals( webServicesAuthCode ) )
            return true;

        return false;
    }


}
