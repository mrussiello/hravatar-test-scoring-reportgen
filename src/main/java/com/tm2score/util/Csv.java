/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.util;

public class Csv
{
	private static final String QUOTE = "\"";
	private static final String ESCAPED_QUOTE = "\"\"";
	private static final char[] CHARACTERS_THAT_MUST_BE_QUOTED = { ',', '"', '\n', '\r', '\f' };

	public static String escape( String s )
	{
            if( s==null || s.isEmpty() )
                return "";

            if( s.length()>1 && s.startsWith( QUOTE ) && s.endsWith( QUOTE ) )
                s = s.substring(1,s.length()-1);

            if ( s.indexOf( QUOTE ) >=0 )
                 s = s.replaceAll( QUOTE, ESCAPED_QUOTE );

            for( int i=0;i<CHARACTERS_THAT_MUST_BE_QUOTED.length; i++ )
            {
                if( s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[i] ) > -1)
                {
                    s = QUOTE + s + QUOTE;
                    break;
                }
            }

            //if( s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[0] ) > -1 || s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[1] ) > -1 ||  s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[2] ) > -1  ||  s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[3] ) > -1 )
            //    s = QUOTE + s + QUOTE;

            return s;
	}


	public static String escapeLongNum( String s )
	{
            if( s==null || s.isEmpty() )
                return "";

            if( s.length()>1 && s.startsWith( QUOTE ) && s.endsWith( QUOTE ) )
                s = s.substring(1,s.length()-1);

            if ( s.indexOf( QUOTE ) >=0 )
                 s = s.replaceAll( QUOTE, ESCAPED_QUOTE );

            //if( s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[0] ) > -1 || s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[1] ) > -1 ||  s.indexOf( CHARACTERS_THAT_MUST_BE_QUOTED[2] ) > -1 )
                s = "=" + QUOTE + s + QUOTE;

            return s;
	}


	public static String unescape( String s )
	{
            if ( s.startsWith( QUOTE ) && s.endsWith( QUOTE ) )
            {
                    s = s.substring( 1, s.length() - 2 );

                    if ( s.indexOf( ESCAPED_QUOTE )>=0 )
                            s = s.replaceAll( ESCAPED_QUOTE, QUOTE );
            }

            return s;
	}


}