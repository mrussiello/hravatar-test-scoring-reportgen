package com.tm2score.util;


import com.tm2score.service.LogService;
import org.jsoup.Jsoup;



/**
 *
 * @author miker
 */
public class HtmlUtils {
        
    public static String removeAllHtmlTags( String inStr )
    {
        if( inStr==null || inStr.isBlank() || !StringUtils.getHasHtml(inStr) )
            return inStr;

        try
        {
            // LogService.logIt( "HtmlUtils.removeAllHtmlTags() \ninStr=" + inStr + "\noutput=" +  Jsoup.parse(inStr).text());           
            return Jsoup.parse(inStr).text();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "HtmlUtils.removeAllHtmlTags() " + inStr );
            return inStr;
        }
    }
        
}
