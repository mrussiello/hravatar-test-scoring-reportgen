/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.googlecloud;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.util.HttpUtils;
import com.tm2score.util.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

/**
 *
 * @author miker_000
 */
public class GoogleTranslateUtils {
    
    private static String API_KEY = null;
    
    private static String API_BASE_URL = null;

    private static String API_TRANSLATE_ENDPOINT = null;
    
    private static synchronized void init()
    {
        if( API_KEY == null )
        {
            API_KEY = RuntimeConstants.getStringValue("gcloudtranslate.HRAVoice.APIKey");
            API_BASE_URL = RuntimeConstants.getStringValue("gcloudtranslate.baseUrl");
            API_TRANSLATE_ENDPOINT = RuntimeConstants.getStringValue("gcloudtranslate.translateEndpoint");            
        }
    }
    
       
    public static String translateTextNoErrors( String text, Locale sourceLocale, Locale targetLocale, boolean unescapeHtmlCharEntities )            
    {
        try
        {
            Object[] o = translateText(text, sourceLocale, targetLocale, unescapeHtmlCharEntities );
            
            if( o[4]!=null )
                return null;

            if( o[5]!=null )
                return null;
            
            return (String) o[1];
        }
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleTranslateUtils.translateTextNoErrors() text=" + text + ", sourceLocale=" + (sourceLocale==null ? "null" : sourceLocale.toString()) + ", targetLocale=" + (targetLocale==null ? "null" : targetLocale.toString())  );            
            return null;
        }
    }
    
    
    /**
     * Returns 
     * 
     *    data[0]=sourceText
     *    data[1]=translated text
     *    data[2]=source language
     *    data[3]=target language
     *    data[4]=Error code if any
     *    data[5]=Error message, if any
     *    data[6]=detected language (only if sourceLang is null)
     * 
     * @param text
     * @param sourceLocale
     * @param targetLocale
     * @return
     * @throws Exception 
     */
    public static Object[] translateText( String text, Locale sourceLocale, Locale targetLocale, boolean unescapeHtmlEntities)
    {
        Object[] out = new Object[7];
        
        out[0]=text;
        
        String jsonResponse = null;
        
        try
        {
            init();

            boolean hasHtml = isHtml( text );
            
            // LogService.logIt( "GoogleTranslateUtils.translateText() hasHtml=" + hasHtml );
            
            // If it has html we don't need to worry about entities.
            if( hasHtml )
                unescapeHtmlEntities = false;
            
            String textX = conditionTextForTranslate(text);
        
            String sourceLang = getGoogleTranslateLangCode( sourceLocale ); // = sourceLocale==null ? null : sourceLocale.getLanguage();
            
            String targetLang = getGoogleTranslateLangCode( targetLocale );
            
            if( targetLang==null ) 
                targetLang = "en";

            out[2] = sourceLang;
            out[3]= targetLang;
            
            if( text == null || text.trim().isEmpty() )
            {
                out[1]=text;
                return out;
            }

            String postUrl = API_BASE_URL + API_TRANSLATE_ENDPOINT;

            // same target and source
            if( sourceLang!=null && sourceLang.equals( targetLang ) )
            {
                out[1]=text;
                return out;
            }

            Map<String,String> paramMap = new HashMap<>();

            paramMap.put( "q", textX );
            paramMap.put("target", targetLang );
            paramMap.put("key", API_KEY );

            if( sourceLang != null && !sourceLang.trim().isEmpty() )
                paramMap.put("source", sourceLang );

            jsonResponse = sendApiPost(postUrl, paramMap );

            // data[0] = translated text
            // data[1] = detectedSourceLang (present only if sourceLang is null)
            String[] dataArr = parseJsonForTextOutAndLang( jsonResponse );

            String textOut = dataArr[0];
            
            // LogService.logIt( "GoogleTranslateUtils.translateText() unconditioned text back: " + textOut );
            
            textOut = restoreTextKeysPostTranslate( textOut);
            
            if( unescapeHtmlEntities )
                textOut = unescapeHtmlCharEntitiesPostTranslate( textOut );
            
            out[1]=textOut; // dataArr[0];
            
            String detLang = null;
            
            if( sourceLang==null )
            {
                detLang = dataArr[1];
                out[6] = detLang;
            }
            
            LogService.logIt("GoogleTranslateUtils.translateText() tgtLang=" + targetLang + ", sourceLang=" + (sourceLang==null ? "" : sourceLang) + ", textIn=" + text + ", textOut=" + out[1] + ", detLang=" + detLang );
            
            return out;
        }

        catch( GoogleApiException e )
        {
            LogService.logIt( e, "GoogleTranslateUtils.translateText() " + e.toString() );
            
            out[4]=new Integer(e.getCode());
            out[5]=e.getMessage();            
            return out;
        }        
        
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleTranslateUtils.translateText() text=" + text + ", sourceLocale=" + (sourceLocale==null ? "null" : sourceLocale.toString()) + ", targetLocale=" + (targetLocale==null ? "null" : targetLocale.toString())  + ", jsonResponse=" + jsonResponse );            
            out[4]=new Integer(999);
            out[5]=e.toString();            
            return out;            
        }
    }
        
    // adapted from post by Phil Haack and modified to match better
    public final static String tagStart= "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)\\>";
    public final static String tagEnd= "\\</\\w+\\>";
    public final static String tagSelfClosing=  "\\<\\w+((\\s+\\w+(\\s*\\=\\s*(?:\".*?\"|'.*?'|[^'\"\\>\\s]+))?)+\\s*|\\s*)/\\>";
    public final static String htmlEntity= "&[a-zA-Z][a-zA-Z0-9]+;";
    public final static Pattern htmlPattern=Pattern.compile( "("+tagStart+".*"+tagEnd+")|("+tagSelfClosing+")|("+htmlEntity+")", Pattern.DOTALL );

    
    /**
     * Will return true if s contains HTML markup tags or entities.
     *
     * @param s String to test
     * @return true if string contains HTML
     */
    public static boolean isHtml(String s) {
        boolean ret=false;
        if (s != null) {
            ret=htmlPattern.matcher(s).find();
        }
        return ret;
    }    
    
    
      
    
    public static String conditionTextForTranslate( String text )
    {
        if( text==null || text.isEmpty() )
            return text;
        
        text =StringUtils.replaceStr(text, "\\nhttp://", " http://" );
        
        text =StringUtils.replaceStr(text, "\\nhttps://", " https://" );
        
        text = StringUtils.replaceStr(text, "HTTP://", "http://" );

        text = StringUtils.replaceStr(text, "HTTPS://", "https://" );
        
        if( text.indexOf( "\n" ) >0 )
            text = text.replaceAll("\\r\\n|\\r|\\n", "<span translate=\"no\">{n}</span>" );

        if( text.indexOf( "&amp;" )>=0 )
            text = StringUtils.replaceStr( text, "&amp;", "AM49393" );

        if( text.indexOf( "&#38;" )>=0 )
            text = StringUtils.replaceStr( text, "&#38;", "AM49393" );
       
        // This can be inside a tag, so putting in another tag won't work.
        if( text.indexOf( "HR AVATAR" )>=0 )
            text = StringUtils.replaceStr( text, "HR AVATAR", "H456959" );
//             text = StringUtils.replaceStr( text, "HR AVATAR", "<span translate=\"no\">HRAVATAR</span>" );
        
        if( text.indexOf( "HR Avatar" )>=0 )
            text = StringUtils.replaceStr( text, "HR Avatar", "H456959" );
//            text = StringUtils.replaceStr( text, "HR Avatar", "<span translate=\"no\">HRAVATAR</span>" );
        
        String subKey;
        
        //Hide Keys inside tags or URLs
        for( int i=0; i<=20; i++ )
        {
            // Inside HTML Tag
            
            // Key inside a tag
            subKey = "\"{"+i+"}\"";            
            // replace {0} with BBRACK0EBRACK             
            if( text.indexOf( subKey ) >0 )
                text = StringUtils.replaceStr( text, subKey, "\"BBRACK"+ i + "EBRACK\"" );     

            // Key inside a URL
            subKey = "/{"+i+"}/";            
            // replace {0} with BBRACK0EBRACK             
            if( text.indexOf( subKey ) >0 )
                text = StringUtils.replaceStr( text, subKey, "/BBRACK"+ i + "EBRACK/" );     

            // Key inside a URL - start
            subKey = "\"{"+i+"}/";            
            // replace {0} with BBRACK0EBRACK             
            if( text.indexOf( subKey ) >0 )
                text = StringUtils.replaceStr( text, subKey, "\"BBRACK"+ i + "EBRACK/" );     
            
            // Key inside a URL - param end
            subKey = "={"+i+"}\"";            
            // replace {0} with BBRACK0EBRACK             
            if( text.indexOf( subKey ) >0 )
                text = StringUtils.replaceStr( text, subKey, "=BBRACK"+ i + "EBRACK\"" );     

            // Key inside a URL - param next
            subKey = "={"+i+"}&";            
            // replace {0} with BBRACK0EBRACK             
            if( text.indexOf( subKey ) >0 )
                text = StringUtils.replaceStr( text, subKey, "=BBRACK"+ i + "EBRACK&" );     

            // Key inside a URL - space next. Note that some languages remove the space so we will re-insert it
            subKey = "={"+i+"} ";            
            // replace {0} with BBRACK0EBRACK             
            if( text.indexOf( subKey ) >0 )
                text = StringUtils.replaceStr( text, subKey, "=BBRACK"+ i + "EBRACKSP " );     
            // 
        }
        
        
        // Now replaceKeys
        for( int i=0; i<=20; i++ )
        {
            subKey = "{"+i+"}";
            
            if( text.indexOf( subKey ) >0 )
                text = StringUtils.replaceStr( text, subKey, "<span translate=\"no\">"+ subKey + "</span>" );            
        }

        int idx = text.indexOf( " http" );
                        
        if( idx >=0 )
        {
            // find first space after url
            int idx2 = text.indexOf( " ", idx+6 );
            
            if( idx2<0 )
                idx2 = text.indexOf( "\n", idx+6 );
            
            if( idx<0 )
                idx2 =text.length();
            
            String t2 = text.substring( 0,idx) + "<span translate=\"no\">" + text.substring(idx, idx2 ) + "</span>";

            if( idx2 < text.length() )
                t2 += text.substring(idx2, text.length() );
            
            text = t2;
        }
        
        for( GoogleTranslateSubstituteKeyType gskt : GoogleTranslateSubstituteKeyType.values() )
        {
            text = StringUtils.replaceStr( text , "\"" + gskt.getKey() + "\"", "\"" + gskt.getSubstituteKey()+ "\"" );
            
            text = StringUtils.replaceStr( text , gskt.getKey(), "<span translate=\"no\">" + gskt.getKey() + "</span>" );
        }
        
        LogService.logIt( "GoogleTranslateUtils.conditionTextForTranslate() Prepared to translate text=" + text );
                
        return text;
    }
    

    
    /**
     * To fix issues in dbms
     * UPDATE googletranslatecache set tgttext = replace(tgttext,'\\n','')
     * @param text
     * @return 
     */
    public static String restoreTextKeysPostTranslate( String text )
    {
        if( text==null || text.isEmpty() )
            return text;
                
        if( text.indexOf( "> <")>0 )
            text = StringUtils.replaceStr(text, "> <", "><" );
        
        if( text.indexOf( "> .")>0 )
            text = StringUtils.replaceStr(text, "> .", ">." );
        
        if( text.indexOf( "> :")>0 )
            text = StringUtils.replaceStr(text, "> :", ">:" );
        
        if( text.indexOf( "> ,")>0 )
            text = StringUtils.replaceStr(text, "> ,", ">," );
        
        if( text.indexOf( "> ;")>0 )
            text = StringUtils.replaceStr(text, "> ;", ">;" );
        
        if( text.indexOf( ">  ")>0 )
            text = StringUtils.replaceStr(text, ">  ", "> " );
        
        
        if( text.indexOf( "] .")>0 )
            text = StringUtils.replaceStr(text, "] .", "]." );
        
        if( text.indexOf( "] :")>0 )
            text = StringUtils.replaceStr(text, "] :", "]:" );
        
        if( text.indexOf( "] ;")>0 )
            text = StringUtils.replaceStr(text, "] ;", "];" );
        
        if( text.indexOf( "] ,")>0 )
            text = StringUtils.replaceStr(text, "] ,", "]," );
        
        if( text.indexOf( "]  ")>0 )
            text = StringUtils.replaceStr(text, "]  ", "] " );
        
        if( text.indexOf( "> [")>0 )
            text = StringUtils.replaceStr(text, "> [", ">[" );
        
        if( text.indexOf( ": ")>0 )
            text = StringUtils.replaceStr(text, ": ", ":" );
             
        if( text.indexOf( ":{")>0 )
            text = StringUtils.replaceStr(text, ":{", ": {" );
             
        
        if( text.indexOf("<span translate=\"no\">{n}</span>")>0 )
            text = StringUtils.replaceStr(text, "<span translate=\"no\">{n}</span>", "\n" );

        if( text.indexOf("\\ n")>0 )
            text = StringUtils.replaceStr(text, "\\ n", "\n" );
                
        if( text.indexOf( "AM49393" )>=0 )
            text = StringUtils.replaceStr( text, "AM49393", "&amp;" );
                
        if( text.contains( "H456959" ) )
            text = StringUtils.replaceStr( text, "H456959", "HR Avatar" );
        
        
        
        int idx = text.indexOf( "<span translate=\"no\">" );
        int idx2;
        String t2;
        while( idx>=0 )
        {
            idx2 = text.indexOf( "</span>", idx+21 ); 
            
            if( idx2<0 )
                break;
            
            t2 = text.substring(0,idx) + text.substring( idx+21, idx2 );
            
            if( idx2 + 7 < text.length() )
                t2 += text.substring(idx2+7, text.length() );
            
            text = t2;
            
            idx = text.indexOf( "<span translate=\"no\">" );
        }
        
        
        
        for( int i=0;i<=20;i++ )
        {
            t2 = "\"BBRACK"+ i + "EBRACK\"";            
            if( text.contains(t2)) 
                text = StringUtils.replaceStr(text, t2, "\"{" + i + "}\"" );

            t2 = "/BBRACK"+ i + "EBRACK/";            
            if( text.contains(t2)) 
                text = StringUtils.replaceStr(text, t2, "/{" + i + "}/" );

            t2 = "\"BBRACK"+ i + "EBRACK/";            
            if( text.contains(t2)) 
                text = StringUtils.replaceStr(text, t2, "\"{" + i + "}/" );

            t2 = "=BBRACK"+ i + "EBRACK\"";            
            if( text.contains(t2)) 
                text = StringUtils.replaceStr(text, t2, "={" + i + "}\"" );

            t2 = "=BBRACK"+ i + "EBRACK&";            
            if( text.contains(t2)) 
                text = StringUtils.replaceStr(text, t2, "={" + i + "}&" );            

            t2 = "=BBRACK"+ i + "EBRACKSP";            
            if( text.contains(t2)) 
                text = StringUtils.replaceStr(text, t2, "={" + i + "} " );            
        }
        
        
        if( text.indexOf( "https : //" ) >=0 )
            text = StringUtils.replaceStr( text, "https : //", "https://" );     
        
        if( text.indexOf( "https :" ) >=0 )
            text = StringUtils.replaceStr( text, "https :", "https:" );     
        
        if( text.indexOf( "http : //" ) >=0 )
            text = StringUtils.replaceStr( text, "http : //", "http://" );     
        
        if( text.indexOf( "http :" ) >=0 )
            text = StringUtils.replaceStr( text, "http :", "https:" );     
        
        if( text.indexOf( ": /" ) >=0 )
            text = StringUtils.replaceStr( text, ": /", ":/" );     
        
        if( text.indexOf( "/ /" ) >=0 )
            text = StringUtils.replaceStr( text, "/ /", "//" );     
        
        for( GoogleTranslateSubstituteKeyType gskt : GoogleTranslateSubstituteKeyType.values() )
        {
            text = StringUtils.replaceStr( text , "\"" + gskt.getSubstituteKey() + "\"", "\"" + gskt.getKey()+ "\"" );
            
            //text = StringUtils.replaceStr( text, gskt.getKey() , gskt.getKey() );
        }

        return text;
        
    }
    


    
    
    
    public static String unescapeHtmlCharEntitiesPostTranslate( String text )
    {
        if( text==null || text.isEmpty() )
            return text;
            
        //text = StringUtils.replaceStr( text, "&quot;" , "\"" );
        //text = StringUtils.replaceStr( text, "&lt;" , "<" );
        //text = StringUtils.replaceStr( text, "&gt;" , ">" );
        //text = StringUtils.replaceStr( text, "&amp;" , "&" );
        //text = StringUtils.replaceStr( text, "&apos;" , "'" );
        //text = StringUtils.replaceStr( text, "&nbsp;" , " " );
        
        text = StringEscapeUtils.unescapeHtml4(text);
        text = StringUtils.replaceStr( text, "</ " , "</" );
        
        return text;
    }
        
    public static String getGoogleTranslateLangCode( Locale loc )
    {
        if( loc == null )
            return null;
        
        String l = loc.getLanguage();
        
        if( l!=null && l.equals("zh") )
                l="zh-CN";
        else if( l!=null && l.indexOf("-")>0 )
            l = l.substring(0, l.indexOf("-"));

        return l;
    }
    
    
    public static String sendApiPost( String url, Map<String,String> paramMap ) throws Exception
    {
        // CloseableHttpResponse r = null;
                
        String s = null;
        
        try (CloseableHttpClient client = HttpUtils.getHttpClient(120))
        {
            init();

            HttpPost httpPost = new HttpPost(url);

            String q = null;
            String tgtLang = null;


            if( paramMap!=null && !paramMap.isEmpty() )
            {
                List<NameValuePair> params = new ArrayList<>();

                for( String key : paramMap.keySet() )
                {
                    if( paramMap.get(key) != null && !paramMap.get(key).isEmpty() )
                        params.add(new BasicNameValuePair(key, paramMap.get(key)));   

                    if( key.equals("q") )
                        q = paramMap.get(key);
                    else if( key.equals("target") )
                        tgtLang = paramMap.get(key);
                }

                httpPost.addHeader( "Content-Type", "application/x-www-form-urlencoded; charset=utf-8" );
                httpPost.setEntity(new UrlEncodedFormEntity(params));                
            }

            //try( CloseableHttpResponse r = client.execute(httpPost) )
            //{
            s = client.execute(httpPost, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "GoogleTranslateUtils.sendApiPost() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "GoogleTranslateUtils.sendApiPost() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    } );

                //int statusCode = r.getCode();

                //if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "GoogleTranslateUtils.sendApiPost() Method failed: " + r.getReasonPhrase() + ", url=" + url + ", q=" + q + ", tgtLang=" + tgtLang );

                //    String s = getJsonFromResponse(r);

                //    throw new GoogleApiException( "GoogleTranslateUtils.sendApiPost() Get failed with Http statuscode " + r.getReasonPhrase() + " uri=" + url + ", q=" + q + ", tgtLang=" + tgtLang, s );
                //}

                //String json =getJsonFromResponse(r);

                //if( r.getEntity()!=null )
                //    EntityUtils.consume(r.getEntity());
                if( isJsonAnError( s ) )
                    throw new GoogleApiException( "GoogleTranslateUtils.sendApiPost() url=" + url + ", q=" + q + ", tgtLang=" + tgtLang, s );
                return s;
            //}
        }
        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "GoogleTranslateUtils.sendApiPost() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new GoogleApiException( e.toString(), s );
        }        
        catch( GoogleApiException e )
        {
            throw e;
        }
        catch( IOException e )
        {
            LogService.logIt( e, "GoogleTranslateUtils.sendApiPost() url=" + url );            
            throw new GoogleApiException( e.toString(), s );
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleTranslateUtils.sendApiPost() url=" + url );            
            throw e;
        }

        finally
        {
            //if( r != null )
            //    r.close();
        }        
    }
    
    
    public static String compressString( String inStr ) throws Exception
    {
        
        String t = inStr;
        
        try
        {
            if( t == null )
                return "";
            
            if( t.length()<= 254 )
                return t.trim();
            
            // Just to make the replace easier, since we will truncate anyway.
            if( t.length()>400 )
                t = t.substring(0, 400 );
            
            t =  t.replaceAll("\\s+","");
            
            if( t.length()>254 )
                t = t.substring(0,254);
            
            return t;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleTranslateUtils.compressString() inStr0=" + inStr + ", inProgStr=" + t );
            return t;            
        }
    }
    

    
    private static String[] parseJsonForTextOutAndLang( String jsonResponse ) throws Exception
    {
        String[] out = new String[2];

        try
        {
            JsonReader jr = Json.createReader(new StringReader( jsonResponse ));

            JsonObject jo = jr.readObject();
            
            JsonObject joData = jo.containsKey("data") ? jo.getJsonObject("data") : null;
            
            if( joData==null  )
                throw new Exception( "data is missing in response." );
            
            if( !joData.containsKey("translations") )
                throw new Exception( "translations is missing in data.response." );
                
            JsonObject joT;
            
            for( JsonValue jv : joData.getJsonArray( "translations" ) )
            {
                joT = (JsonObject) jv;

                if( joT.containsKey( "translatedText" ) )
                    out[0] = joT.getString("translatedText");
                
                else
                    continue;

                if( joT.containsKey( "detectedSourceLanguage" ) )
                    out[1] = joT.getString("detectedSourceLanguage");

                return out;
            }
            
            // should only get here if no translatedText
            LogService.logIt( "GoogleTranslateUtils.parseJsonForTextOut() no TranslatedText key found in response. " + jsonResponse );
            
            return out;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleTranslateUtils.parseJsonForTextOut() " + jsonResponse );
            
            return null;
        }
    }
    
    
    private static boolean isStatusCodeOk( int statusCode )
    {
        return statusCode>=200 && statusCode<300;
    }
    
    
    public static boolean isJsonAnError( String json )
    {
        if( json.indexOf("\"error\":") >= 0 )
        {
            try
            {
                JsonReader jr = Json.createReader(new StringReader( json ));

                JsonObject jo = jr.readObject();

                if( jo.containsKey("error") )                
                    return true;
            }
            
            catch( Exception e )
            {
                LogService.logIt( e, "GoogleTranslateUtils.isJsonAnError() Parsing JSON for error" );
            }
        }
        
        return false;        
    }
    
    
    public static String getJsonFromResponse(CloseableHttpResponse response) throws IOException, Exception {

        StringBuilder sb = null;

        try {
            if ( response != null ) {

                String line = "";
                sb = new StringBuilder();

                InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent(), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                while ( (line = bufferedReader.readLine()) != null ) {
                        sb.append(line);
                }
            }
        } catch (IOException e) {
                throw new Exception(e.getMessage());
        } finally {
                if ( response != null ) {
                        response.close();
                }
        }

        return sb==null ? "" : sb.toString();
    }

}
