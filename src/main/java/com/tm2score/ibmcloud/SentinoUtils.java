/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ibmcloud;


import com.tm2score.global.RuntimeConstants;
import com.tm2score.googlecloud.GoogleApiException;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.util.HttpUtils;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
 *
 * @author miker_000
 */
public class SentinoUtils 
{
   private static final String[] SUPPORTED_LANGUAGES = new String[] {"en"}; 

   // private static final String[] SUPPORTED_REPORT_LANGUAGES = new String[] {"ar", "de", "en", "es", "fr", "it", "ja", "ko", "pt", "zh"}; 
      
    private static String API_KEY = null;
    
    private static String API_BASE_URL = null;

    private static String API_ENDPOINT = null;

    private static Boolean SENTINO_ON = null;
    
    // private static String[] STANDARD_PARAMS = new String[] { "version", "4", "consumption_preferences", "false", "raw_scores", "false" };
    
    private static String VERSION_ID = "4";
    
    private static int MIN_TEXT_LENGTH = 10;
    
    private static int MIN_ACCEPTABLE_WORD_COUNT = 600;

    public static float MIN_SENTINO_TRAIT_CONFIDENCE = 0.3f;
    
    
    private static synchronized void init()
    {
        if( API_KEY == null )
        {
            API_KEY = RuntimeConstants.getStringValue("sentino.APIKey");
            API_BASE_URL = RuntimeConstants.getStringValue("sentino.baseUrl");
            API_ENDPOINT = RuntimeConstants.getStringValue("sentino.endpoint");     
            SENTINO_ON = RuntimeConstants.getBooleanValue("ibmcloudInsightOn");            
        }
    }
    
    public String getInsightVersionId()
    {
        return VERSION_ID;
    }
    
    public static boolean getIsHraTrait( TextAndTitle tt )
    {
        return tt!=null && tt.getText()!=null && tt.getText().startsWith("HraTrait");
    }
    
    public static HraTrait getHraTrait( TextAndTitle tt ) throws Exception
    {
        if( !getIsHraTrait( tt ))
            return null;
        
        String[] vals=tt.getText().split(";" );
        if( vals.length<5 )
            throw new Exception( "Packed string appears invalid, text for trait has too few tokens. textAndTitle=" + tt.toString() );
                
        int hraTraitTypeId = Integer.parseInt(vals[1]);
        float hraScore = Float.parseFloat(vals[2]);
        float score = Float.parseFloat(vals[3]);
        float confidence = Float.parseFloat(vals[4]);
                
        return new HraTrait( tt.getTitle(), hraTraitTypeId, hraScore, score, confidence );
        
    }
    
    public static float convertSentinoScoreToHraScore( float sentinoScore )
    {
        float s = (sentinoScore)*100f;
        if( s<1f )
            s=1f;
        if( s>100f )
            s=100f;
        return s;
    }
    
    public static int roundScore( float scr )
    {
        if( scr<=1f )
            return 1;
        
        if( scr>=100 )
            return 100;
        
        return Math.round( scr ); 
    }
    
    
    
    
    /**
     * Returns Object[] 
     *    data[0] = "SUCCESS" or "ERROR"
     *    data[1] = Json Object with results or Error Message
     *    data[2] = Word Count (total)
     *    data[3] = InsightResult (success only)
     *    
     * 
     * @param text
     * @param sourceLocale
     * @return
     * @throws Exception 
     */
    public Object[] evaluateTextForProfile( List<String[]> contentList, Locale sourceLocale, Locale reportLocale, int hraTraitPackageTypeId, Set<Integer> hraTraitTypeIdsToInclude) throws Exception
    {
        init();
        
        Object[] out = new Object[7];
        
        out[0]="ERROR";
        
        if( !SENTINO_ON )
        {
            out[1] = "Sentino is OFF";
            return out;
        }
        
        if( contentList==null  )
            throw new Exception( "contentList is null." );
            
        if( contentList.size()<=0 )
            throw new Exception( "contentList contains no entries." );
        
        int wordCount = getTotalWords( contentList );
        
        out[2] = (int)( wordCount );
        
        String jsonResponse = null;
        
        String payload = null;
        try
        {
            // init();
            
            LogService.logIt("SentinoUtils.evaluateTextForProfile() contentList.size()=" + contentList.size() );
               
            if( sourceLocale==null )
                sourceLocale = Locale.US;

            if( reportLocale==null )
                reportLocale = Locale.US;
                                    
            boolean langSupported = false;
            for( String lang: SUPPORTED_LANGUAGES )
            {
                if( lang.equalsIgnoreCase( sourceLocale.getLanguage() ) )
                {
                    langSupported=true;
                    break;
                }
            }            
            if( !langSupported )
                throw new Exception( "Source language: " + sourceLocale.getLanguage() + " is not supported by IBM." );
            
            langSupported = true;
            //for( String lang: SUPPORTED_REPORT_LANGUAGES )
            //{
            //    if( lang.equalsIgnoreCase( reportLocale.getLanguage() ) )
            //    {
            //        langSupported= true;
            //        break;
             //   }
            //}            
            if( !langSupported )
            {
                LogService.logIt("SentinoUtils.evaluateTextForProfile() ReportLocale=" + reportLocale.toString() + " language is not supported for reports. changing to source language=" + sourceLocale.toString() );  
                reportLocale = sourceLocale;
            }
            
            String postUrl = API_BASE_URL + API_ENDPOINT;

            Map<String,String> paramMap = new HashMap<>();

            //paramMap.put("language", sourceLocale.getLanguage() );
            
            //for( int i=0; i<STANDARD_PARAMS.length-1; i+=2 )
            //{
            //    paramMap.put( STANDARD_PARAMS[i], STANDARD_PARAMS[i+1] );
            //}
            
            Set<SentinoGroupType> sentinoGroupTypeSet = new HashSet<>();

            Set<SentinoTraitType> sentinoTraitTypeSet = new HashSet<>();
            
            HraTraitPackageType hraTraitPackageType = HraTraitPackageType.getValue(hraTraitPackageTypeId);
            
            List<HraTraitType> hraTtl = hraTraitPackageType.getHraTraitTypeList();
            
            if( hraTraitTypeIdsToInclude!=null && !hraTraitTypeIdsToInclude.isEmpty() )
            {
                ListIterator<HraTraitType> iter = hraTtl.listIterator();
                HraTraitType htt;
                while( iter.hasNext() )
                {
                    htt=iter.next();
                    
                    // if not designated to include it, remove it.
                    if( !hraTraitTypeIdsToInclude.contains(htt.getHraTraitTypeId()))
                        iter.remove();
                }
            }
            
            SentinoTraitType stt;
            
            for( HraTraitType tt : hraTtl )
            {
                for( Integer i : tt.getSentinoTraitTypeIds() )
                {
                    stt = SentinoTraitType.getValue( i );
                    if( stt!=null )
                    {
                        sentinoTraitTypeSet.add(stt);
                        sentinoGroupTypeSet.add(stt.getSentinoGroupType());
                    }   
                }
            }

            LogService.logIt("SentinoUtils.evaluateTextForProfile() hraTraitPackageTypeId=" + hraTraitPackageTypeId + ", hraTtl=" + hraTtl.size() + ", sentinoTraitTypeList=" + sentinoTraitTypeSet.size() + ", sentinoGroupTypeList=" + sentinoGroupTypeSet.size() );
            
            payload = getJsonPayloadForProfile(contentList, sourceLocale.getLanguage(), sentinoGroupTypeSet, sentinoTraitTypeSet );

            LogService.logIt( "SentinoUtils.evaluateTextForProfile() url=" + postUrl + ", payload=" + payload );
            
            jsonResponse = sendApiPost(postUrl, paramMap, sourceLocale.getLanguage(), reportLocale.getLanguage(), payload );
            
            LogService.logIt( "SentinoUtils.evaluateTextForProfile() unconditioned text back: " + jsonResponse.length() );
            
            SentinoResult result = new SentinoResult( false, jsonResponse, hraTraitPackageTypeId, hraTraitTypeIdsToInclude);
            
            out[0] = "SUCCESS";
            
            out[1] = jsonResponse;
            
            out[3] = result;
            
            return out;
        }

        catch( IbmApiException e )
        {
            LogService.logIt( "SentinoUtils.evaluateTextForProfile() ERROR with Request. " + e.toString() + ", payload=" + (payload==null ? "null" : payload ) );            
            out[1]=e.getMessage();            
            return out;
        }        
        
        catch( Exception e )
        {
            LogService.logIt(e, "SentinoUtils.evaluateTextForProfile() JsonResponse=" + jsonResponse );            
            out[1]=e.toString();            
            return out;            
        }
    }
    
    
    
    
    private String getJsonPayloadForProfile( List<String[]> contentList, String language, Set<SentinoGroupType> sentinoGroupTypeList, Set<SentinoTraitType> sentinoTraitTypeList )
    {
        try
        {
            JsonObjectBuilder jobTop = Json.createObjectBuilder();
            
            JsonArrayBuilder jab;
            
            JsonObjectBuilder job;
            
            String question;
            String answer;
            
            // first, add Inventories
            jab = Json.createArrayBuilder();
            for( SentinoGroupType sgt : sentinoGroupTypeList )
            {
                jab.add( sgt.getSentinoKey());
            }
            jobTop.add("inventories", jab );
            
            // next, add Indices
            jab = Json.createArrayBuilder();
            for( SentinoTraitType stt : sentinoTraitTypeList )
            {
                jab.add( stt.getSentinoGroupType().getSentinoKey() + "." + stt.getSentinoKey() );
            }
            jobTop.add("indices", jab );

            jab = Json.createArrayBuilder();
            

            for( String[] s : contentList )
            {
                //LogService.logIt( "SentinoUtils.getJsonPayloadForProfile() s=" + s );
                question = s[0];
                answer = s[1];
                
                if( answer==null || question==null)
                    continue;
                
                question = question.trim();
                answer = answer.trim();
                
                if( answer.isBlank() )
                    continue;
                
                // s = StringUtils.escapeTextForJson(s);
                
                job = Json.createObjectBuilder();                                
                // job.add( "testfield" , "testfield" );
                job.add( "question", question );
                job.add( "answer", answer );
                //job.add( "contenttype", "text/plain" );
                //job.add( "language", language );
                
                //LogService.logIt( "SentinoUtils.getJsonPayloadForProfile() Adding job " );
                
                jab.add(job);
                
                //LogService.logIt( "SentinoUtils.getJsonPayloadForProfile() Added " );
            }
            
            
            
            jobTop.add( "items", jab );
            
            JsonObject jo = jobTop.build();
            
            String payload = JsonUtils.convertJsonObjectToString(jo);
            
            // LogService.logIt( "SentinoUtils.getJsonPayloadForProfile() Payload=" + payload );
            
            return payload;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SentinoUtils.getJsonPayloadForProfile() " );
            
            return null;
        }
    }
    
    
    
    public int getTotalWords( List<String[]> contentList )
    {
        if( contentList==null || contentList.isEmpty() )
            return 0;
        
        int c=0;
        
        for( String[] s : contentList )
        {
            if( s==null )
                continue;
            
            c += StringUtils.numWords(s[1]);
        }
        
        return c;
    }
    
    public String sendApiPost( String url, Map<String,String> paramMap, String textLang, String resultLang, String payload ) throws Exception
    {
        if( payload==null || payload.isEmpty() )
            throw new Exception( "Payload is empty");
        
        // CloseableHttpResponse r = null;
        // CloseableHttpClient client = null;
        
        try (CloseableHttpClient client = HttpUtils.getHttpClient(60))
        {
            init();

            StringBuilder paramStr = new StringBuilder();
            
            if( paramMap!=null && !paramMap.isEmpty() )
            {
                // List<NameValuePair> params = new ArrayList<>();
                
                for( String key : paramMap.keySet() )
                {
                    if( paramMap.get(key) != null && !paramMap.get(key).isEmpty() )
                    {
                        // params.add(new BasicNameValuePair(key, paramMap.get(key)));
                        
                        if(paramStr.length()>0)
                            paramStr.append( "&" );
                        
                        paramStr.append( key + "=" + URLEncoder.encode( paramMap.get(key), "UTF-8" ) );
                    }                       
                }
                
                url += "?" + paramStr.toString();
                // httpPost.setEntity( new UrlEncodedFormEntity(params, "UTF-8"));                
            }


            HttpPost httpPost = new HttpPost(url);            
            httpPost.setHeader( "Authorization", "Token " + API_KEY  );            
            httpPost.addHeader( "Content-Type", "application/json" );
            httpPost.addHeader( "Accept", "application/json" );
            httpPost.addHeader( "Content-Language", textLang );
            httpPost.addHeader( "Accept-Language", resultLang );
            httpPost.setEntity( new StringEntity( payload ) );
                
            //try( CloseableHttpResponse r = client.execute(httpPost ) )
            //{
            String json = client.execute(httpPost, (ClassicHttpResponse response) -> {
                int status = response.getCode();
                // LogService.logIt( "SentinoUtils.sendApiPost() statusCode="+ statusCode );
                final HttpEntity entity2 = response.getEntity();
                String ss = EntityUtils.toString(entity2);
                EntityUtils.consume(entity2);
                if( status<200 || status>=300 )
                    throw new IOException( "SentinoUtils.sendApiPost() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                return ss;
                } );

                //int statusCode = r.getCode();

                //if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "SentinoUtils.sendApiPost() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                //    String s = getJsonFromResponse(r);

                //    throw new IbmApiException( "SentinoUtils.sendApiPost() Get failed with Http statuscode " + r.getReasonPhrase() + " uri=" + url , s );
                //}

                //String json =getJsonFromResponse(r);

                //if( r.getEntity()!=null )
                 //   EntityUtils.consume(r.getEntity());
                
                // client.close();

                //if( isJsonAnError( json ) )
                //    throw new IbmApiException( "SentinoUtils.sendApiPost() url=" + url + ", q=" + q + ", tgtLang=" + tgtLang, json );

            return json;
            // }
        }        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "SentinoUtils.sendApiPost() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new IbmApiException( e.toString(), "" );
        }        
        catch( IOException e )
        {
            LogService.logIt( "SentinoUtils.sendApiPost() STERR " + e.toString() + ", url=" + url + ", payload=" + payload );
            throw new IbmApiException( e.toString(), "" );
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "SentinoUtils.sendApiPost() url=" + url + ", payload=" + payload  );
            
            throw e;
        }

        finally
        {
            //if( r != null )
            //    r.close();
        }        
    }
    
    
    private boolean isStatusCodeOk( int statusCode )
    {
        return statusCode>=200 && statusCode<300;
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
