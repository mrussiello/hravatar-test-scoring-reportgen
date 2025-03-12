package com.tm2score.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



/**
 * variety of static convenience methods for working with cookies.
 */
public class CookieUtils
{
    //  private static String DEFAULT_COOKIE_NAME = "stdcookie";

    public static boolean getAreCookiesSupported( HttpServletRequest request )
    {

        Cookie[] cookies = request.getCookies();

        if( cookies != null && cookies.length > 0 )
            return true;

        return false;
        // Cookie cookie = getCookie( request ,
        //        DEFAULT_COOKIE_NAME );

        // return cookie != null;
    }



    /*
    public static void setDefaultCookie( HttpServletResponse response )
    {
        setCookie(  response ,
                    DEFAULT_COOKIE_NAME ,
                    "setok" ,
                    "/" ,
                    3600
                   );
    }
    */


    /**
     * Returns the cookie requested or null
     */
    public static Cookie getCookie( HttpServletRequest request ,
                                    String cookieName )
    {
        // get the cookies
        Cookie[] cookies = request.getCookies();

        // if cookies found
        if( cookies != null )
        {
            for( int i=0; i< cookies.length ; i++ )
            {
                if( cookies[i].getName().equals( cookieName )  )
                    return cookies[i];
            }
        }

        return null;
    }



    /**
     * Sets a cookie
     */
    public static void setCookie( HttpServletResponse response ,
                                  String name ,
                                  String value ,
                                  String path ,
                                  int maxAge
                                 )
    {
        Cookie cookie = new Cookie( name , value );

        if( path != null )
            cookie.setPath( path );

        cookie.setMaxAge( maxAge );

        // place in response
        response.addCookie( cookie );
    }

    public static void removeCookie( HttpServletResponse response ,
            String name ,
            String value ,
            String path
           )
    {
        Cookie cookie = new Cookie( name , value );

        if( path != null )
            cookie.setPath( path );

        cookie.setMaxAge( 0 );

        // place in response
        response.addCookie( cookie );
    }


}