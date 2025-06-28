package com.tm2score.xml;

import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.net.URLDecoder;

import java.net.URLEncoder;

public class XmlUtils
{

    public static boolean DEBUG = false;

    public static String stripNonValidXMLCharacters(String in) {
        try {
            if (in == null || ("".equals(in))) {
                return ""; // vacancy test.
            }
            StringBuilder out = new StringBuilder();
            char c; // Used to reference the current character.
            for (int i = 0; i < in.length(); i++) {
                c = in.charAt(i);

                if( c==127 )
                {
                    LogService.logIt( "ImoLongXmlUtils.stripNonValidXMLCharacters() removing Char 127");

                    continue;
                }

                if ((c==9) || (c==10) || (c==13) || ((c >= 32) && (c <= 55295)) || ((c >= 57344) && (c <= 65533)) || ((c >= 65536) && (c <= 1114111))) {
                    out.append(c);
                }
            }
            return out.toString();
        } catch (Exception e) {
            LogService.logIt(e, "XmlUtils.stripNonValidXMLCharacters() \n" + in);
            return in;
        }
    }

    
    /* Same as above but uses Hex.
  public static String sanitizeXmlChars(String in) 
  {    
    try
    {
        StringBuilder out = new StringBuilder();
        char current;

        if (in == null || ("".equals(in))) return "";
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i);
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        
        return out.toString();
     }
    catch (Exception e) 
    {
        LogService.logIt(e, "ImoLongXmlUtils.stripNonValidXMLCharacters() \n" + in);
        return in;
    }
    
      
  }  
  */
    


    public static String encodeURIComponent( String inStr  )
    {
    	return encodeURIComponent( inStr , "UTF8" , false );
    }

    public static String encodeURIComponent( String inStr , String encoding )
    {
    	return encodeURIComponent( inStr , encoding , false );
    }


    public static String encodeURIComponent( String inStr , String encoding , Boolean recodeReturns )
    {
        try
        {
            if( inStr == null || inStr.length() == 0 )
                return inStr;

            if( encoding == null || encoding.length()==0 )
                encoding = "UTF8";

            String s = URLEncoder.encode( inStr , encoding ).replaceAll( "\\+", "%20" );

            if( recodeReturns )
            	return StringUtils.replaceStr( s, "%0D%0A" , "%0D" );

            else
            	return s;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "SimBuilder.encodeURIComponent() " + inStr  );

            return inStr;
        }
    }

    /*
    public static String decodeURIComponent( String s )
    {
        if( s == null )
            return s;

        try
        {
            return URLDecoder.decode(s, "UTF8" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "SimBuilder.decodeURIComponent() " + s  );

            return s;

        }
    }
    */

    /*
    public static String decodeURIComponentNoErrors( String s )
    {
        if( s == null )
            return s;

        try
        {
            return URLDecoder.decode(s, "UTF8" );
        }

        catch( Exception e )
        {
            // LogService.logIt( e, "SimBuilder.decodeURIComponent() " + s  );

            return s;

        }
    }
    */



}
