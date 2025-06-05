/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.googlecloud;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.ibmcloud.IbmApiException;
import com.tm2score.ivr.IvrScoreException;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.util.HttpUtils;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.StringUtils;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import org.apache.hc.client5.http.classic.methods.HttpGet;
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
public class GoogleSpeechUtils {
    
    private static final String[] VALID_LANG_CODES = new String[]{"af-ZA","am-ET","hy-AM","az-AZ","id-ID","ms-MY","bn-BD","bn-IN","ca-ES","cs-CZ","da-DK","de-DE","en-AU","en-CA","en-GH","en-GB","en-IN","en-IE","en-KE","en-NZ","en-NG","en-PH","en-ZA","en-TZ","en-US","es-AR","es-BO","es-CL","es-CO","es-CR","es-EC","es-SV","es-ES","es-US","es-GT","es-HN","es-MX","es-NI","es-PA","es-PY","es-PE","es-PR","es-DO","es-UY","es-VE","eu-ES","fil-PH","fr-CA","fr-FR","gl-ES","ka-GE","gu-IN","hr-HR","zu-ZA","is-IS","it-IT","jv-ID","kn-IN","km-KH","lo-LA","lv-LV","lt-LT","hu-HU","ml-IN","mr-IN","nl-NL","ne-NP","nb-NO","pl-PL","pt-BR","pt-PT","ro-RO","si-LK","sk-SK","sl-SI","su-ID","sw-TZ","sw-KE","fi-FI","sv-SE","ta-IN","ta-SG","ta-LK","ta-MY","te-IN","vi-VN","tr-TR","ur-PK","ur-IN","el-GR","bg-BG","ru-RU","sr-RS","uk-UA","he-IL","ar-IL","ar-JO","ar-AE","ar-BH","ar-DZ","ar-SA","ar-IQ","ar-KW","ar-MA","ar-TN","ar-OM","ar-PS","ar-QA","ar-LB","ar-EG","fa-IR","hi-IN","th-TH","ko-KR","cmn-Hant-TW","yue-Hant-HK","ja-JP","cmn-Hans-HK","cmn-Hans-CN"};

    private static String API_KEY = null;
    
    private static String API_BASE_URL = null;

    private static String API_RECOGNIZE_ENDPOINT_SYNCH = null;
    private static String API_RECOGNIZE_ENDPOINT_ASYNCH = null;
    private static String API_RECOGNIZE_ASYNCH_RESULTS_ENDPOINT = null;
    
    private static synchronized void init()
    {
        if( API_KEY == null )
        {
            API_KEY = RuntimeConstants.getStringValue("gcloudspeech.HRAVoice.APIKey");
            API_BASE_URL = RuntimeConstants.getStringValue("gcloudspeech.baseUrl");
            API_RECOGNIZE_ENDPOINT_SYNCH = RuntimeConstants.getStringValue("gcloudspeech.recognizeEndpoint");
            API_RECOGNIZE_ENDPOINT_ASYNCH = RuntimeConstants.getStringValue("gcloudspeech.longRecognizeEndpoint");
            API_RECOGNIZE_ASYNCH_RESULTS_ENDPOINT = RuntimeConstants.getStringValue("gcloudspeech.longRecognizeResultsEndpoint");            
        }
    }
    
       
    public static boolean isLangCodeValidForSpeech2Text( String langCode )
    {
        if( langCode==null || langCode.isEmpty() )
            return false;
        
        for( int i=0;i<VALID_LANG_CODES.length; i++ )
        {
            if( VALID_LANG_CODES[i].equalsIgnoreCase( langCode ) )
                return true;
        }
        
        return true;
    }
    
    
    /**
     * If successful but not done, returns name as String
     * if successful and done, returns Speech2TextResult object
     * if failed, throws an exception
     * 
     * data[0]=String name
     * data[1]= Speech2TextResult if present.
     * 
     * @param gsUrl
     * @param langCode
     * @param maxAlternatives
     * @param hints
     * @return
     * @throws Exception 
     */
    public static Object[] requestTranscribeAudioAsynch( String gsUrl, String langCode, int maxAlternatives, int audioSampleRate, String[] hints) throws Exception
    {
        String payload = null;
        String resultJson = null;
        
        // LogService.logIt("GoogleSpeechUtils.requestTranscribeAudioAsynch() START gsUrl=" + gsUrl );
        
        try
        {     
            init();
            
            if( ( gsUrl==null || gsUrl.isEmpty())  )
                throw new Exception( "Google Storage Url is required but both appear missing. " + gsUrl );
                 
            if( langCode==null || langCode.isEmpty() )
                throw new Exception( "langCode is invalid: " + langCode );
                             
            payload = getRecognizeRequestJson(  null, 
                                                gsUrl, // audioGSUri, 
                                                "LINEAR16", // encoding, 
                                                audioSampleRate, //    int sampleRate, 
                                                langCode, 
                                                maxAlternatives, 
                                                hints );
            
            // LogService.logIt( "GoogleSpeechUtils.requestTranscribeAudioAsynch() CALLING Service payload=" + payload );
            
            resultJson = getSpeech2TextGoogleOperationJsonAsynch( payload );
                  
            // LogService.logIt( "GoogleSpeechUtils.requestTranscribeAudioAsynch() have resultJson=" + resultJson + ", payload=" + payload );
            
            Tracker.addGoogleCloudSpeechReqAsync();
            
            return processAsynchResult( resultJson );            
        }

        catch( GoogleApiException e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.requestTranscribeAudioAsynch() " + e.toString() );
            throw e;
        }        
        
        
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleSpeechUtils.requestTranscribeAudioAsynch() " + ( gsUrl!=null ?  "Base64Str=" + gsUrl + ", length=" + gsUrl.length() : "base64Str is null")   + ", payload=" + StringUtils.truncateStringWithTrailer(payload, 500, false ) + ", resultJson=" + resultJson );
            
            throw e;
        }
    }
    
    private static Object[] processAsynchResult( String resultJson ) throws Exception
    {
        // LogService.logIt( "GoogleSpeechUtils.processAsynchResult() START resultJson=" + resultJson );

        Object[] out = new Object[2];

        // Returns 
        // data[0] = Name
        // data[1] = Boolean done (TRUE or FALSE)
        // data[2] = String  - Metadata
        // data[3] = JSON Object  - ERROR
        // data[4] = JSON Object  - RESPONSE
        Object[] longResults = parseOperationResults( resultJson ); 

        String name = (String) longResults[0];

        out[0] = name;

        Boolean done = (Boolean) longResults[1];

        String metadata = longResults[2]==null ? "" : (String) longResults[2];

        // LogService.logIt( "GoogleSpeechUtils.processAsynchResult() response name=" + name + ", done=" + done + ", metadata=" + metadata );

        JsonObject joErr = longResults[3]==null ? null : (JsonObject) longResults[3];

        if( joErr!=null )
            throw new GoogleApiException( "GoogleSpeechUtils.processAsynchResult() resultJson=" + resultJson , joErr );

        if( !done )
            return out;

        JsonObject joResults = longResults[4]==null ? null : (JsonObject) longResults[4];

        if( joResults != null )
             out[1] = parseSpeech2TextFmResultObj( joResults );

        return out;        
    }
     
    
    
    public static Speech2TextResult requestTranscribeAudioAsynchResults( String googleOpName ) throws Exception
    {
        init();
        
        String getUrl = "";

        try
        {
            if( googleOpName==null || googleOpName.isEmpty() )
                throw new Exception( "googleOpName is emapty." );
            
            getUrl = API_BASE_URL + API_RECOGNIZE_ASYNCH_RESULTS_ENDPOINT + URLEncoder.encode( googleOpName, "UTF8" ) + "?key=" + API_KEY;
            
            String resultJson = sendApiGet( getUrl );
            
            Object[] rr = processAsynchResult( resultJson );
            
            return rr[1]==null ? null : (Speech2TextResult) rr[1];
        }
        
        catch( GoogleApiException e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.requestTranscribeAudioAsynchResults() " + e.toString() );
            throw e;
        }        
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.requestTranscribeAudioAsynchResults() googleOpName=" + googleOpName );
            
            throw e;
        }


    }
    
    
    
    public static Speech2TextResult transcribeAudioSynch( String base64Str, 
            String langCode, 
            int maxAlternatives, 
            int sampleRate, 
            String[] hints ) throws Exception
    {
        String payload = null;
        String resultJson = null;
        
        try
        {        
            init();
            
            if( ( base64Str==null || base64Str.isEmpty())  )
                throw new Exception( "base64Str is required but both appear missing. " + base64Str );
                 
            if( langCode==null || langCode.isEmpty() )
                throw new Exception( "langCode is invalid: " + langCode );
                             
            payload = getRecognizeRequestJson(  base64Str, 
                                                null, // audioGSUri, 
                                                "LINEAR16", // encoding, 
                                                sampleRate, //    int sampleRate, 
                                                langCode, 
                                                maxAlternatives, 
                                                hints );
            
            resultJson = getSpeech2TextAsJsonSynch( payload );
                    
            // LogService.logIt( "GoogleSpeechUtils.transcribeAudioSynch() resultJson=" + resultJson );
            
            Speech2TextResult out = parseSpeech2TextResultJson( resultJson );
            
            // LogService.logIt( "GoogleSpeechUtils.transcribeAudioSynch() result list contains " + (out==null || out.getResultList()==null ? " ZERO - no results returned." : out.getResultList().size() ) + " alternatives." );
            
            Tracker.addGoogleCloudSpeechReqSync();
            return out;
        }

        catch( GoogleApiException e )
        {
            LogService.logIt( "GoogleSpeechUtils.transcribeAudioSynch() " + e.toString() + ", resultJson=" + resultJson );
            throw e;
        }        
        
        catch( IvrScoreException e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.transcribeAudioSynch() resultJson=" + resultJson );
            
            if( e.isFatal() )
                throw e;
            
            return new Speech2TextResult(e);            
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleSpeechUtils.transcribeAudioSynch() " + ( base64Str!=null ?  "Base64Str=" + base64Str + ", length=" + base64Str.length() : "base64Str is null")   + ", payload=" + StringUtils.truncateStringWithTrailer(payload, 500, false ) + ", resultJson=" + resultJson );
            
            throw e;
        }
    }
    
    
    
    // data[0] = String Name
    // data[1] = Boolean done (TRUE or FALSE)
    // data[2] = String  - Metadata
    // data[3] = JSON Object  - ERROR
    // data[4] = JSON Object  - RESPONSE
    private static Object[] parseOperationResults( String resultJson ) throws Exception
    {
        Object[] out = new Object[5];

        try
        {
            JsonReader jr = Json.createReader(new StringReader( resultJson ));

            JsonObject jo = jr.readObject();
            
            String name = jo.containsKey("name") ? jo.getString("name") : null;
            
            if( name==null || name.isEmpty() )
                throw new Exception( "Name is missing." );
            
            out[0] = name;
            
            // if( !jo.containsKey("done") )                
            //     throw new Exception( "resultJson does not have results done" );
            
            boolean done = jo.containsKey("done") ? jo.getBoolean( "done" ) : false;
            
            out[1] = new Boolean(done);
            
            JsonObject metadata = jo.containsKey("metadata") ? jo.getJsonObject("metadata") : null;
            
            if( metadata != null )
                out[2] = JsonUtils.getJsonObjectAsString( (JsonObject) metadata );
            
            if( !done )
                return out;
            
            // { "error": { "code":400, "message":"error message" } }, so out[3] = { "code":400, "message":"error message" }
            out[3] = jo.containsKey( "error") ? jo.getJsonObject( "error" ) : null;
            
            // { "response": { "results": [] } }, so out[4] = { "results": [] }
            out[4] = jo.containsKey( "response") ? jo.getJsonObject( "response" ) : null;
            
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleSpeechUtils.parseOperationResults() " + resultJson );
            
            throw e;
        }
    }
    
    
    /**
     * resultJson={  "results": [    {      "alternatives": [        {          "transcript": "family decided to go to the Beach for their annual summer",          "confidence": 0.81582266        }      ]    }  ]}
     * 
     * @param resultJson
     * @return
     * @throws Exception 
     */
    private static Speech2TextResult parseSpeech2TextResultJson( String resultJson ) throws Exception
    {
        try
        {
            JsonReader jr = Json.createReader(new StringReader( resultJson ));

            JsonObject joResultObj = jr.readObject();
            
            // { "results": [{},{},{}] }
            if( !joResultObj.containsKey( "results") )                
                throw new IvrScoreException( 0,  "resultJson does not have results element" );
            
            return parseSpeech2TextFmResultObj(joResultObj );
        }
        
        catch( IvrScoreException e )
        {
            if( e.isFatal() )
                LogService.logIt( e, "GoogleSpeechUtils.parseSpeech2TextResultJson() resultJson=" + resultJson );
    
            else
                LogService.logIt( e, "GoogleSpeechUtils.parseSpeech2TextResultJson() NONFATAL resultJson=" + resultJson );
                
            if( e.isFatal() )
                throw e;
            
            return new Speech2TextResult( e );            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.parseSpeech2TextResultJson() resultJson=" + resultJson );
            
            throw e;
        } 
    }


    private static Speech2TextResult parseSpeech2TextFmResultObj( JsonObject joResultObj ) throws Exception
    {
        try
        {
            float confidence = -1f;
            String transcript;              
            List<Object[]> resultsList = new ArrayList<>();
            
            List<String> transList;

            if( !joResultObj.containsKey("results") )                
                throw new IvrScoreException( 0,  "resultJson does not have results element" );
            
            JsonObject joResult;
            JsonObject alternative = null;
                
            // For each Result
            for( JsonValue rjv : joResultObj.getJsonArray( "results" ) )
            {
                // Cast result
                joResult = (JsonObject) rjv;
                
                transList = new ArrayList<>();
                confidence=-1;
                
                // look for array of alternatives.
                if( !joResult.containsKey( "alternatives") )
                {
                    String s = "Result object does not contain 'alternatives' element. Skipping. resultsArray.size()="  + joResultObj.getJsonArray( "results" ).size();
                    
                    if( joResultObj.getJsonArray( "results" ).size()==1 )
                        throw new IvrScoreException( 0,  s );
                    
                    // else
                    //    LogService.logIt( "GoogleSpeechUtils.parseSpeech2TextFmResultObj() " + s );
                }
                
                // for each alternative in the result
                for( JsonValue jv : joResult.getJsonArray("alternatives") )
                {
                    alternative = (JsonObject) jv;
                    
                    if( alternative.containsKey( "transcript" ) )
                    {
                        transcript = alternative.getString( "transcript" );
                        
                        if( transcript!=null && !transcript.isEmpty() )
                        {
                            transList.add( transcript );
                            // alternatives.add( new Object[]{transcript, new Float(confidence)} );                            

                            // get the confidence if present.
                            if( confidence <0 &&  alternative.containsKey("confidence") )
                            {
                                JsonNumber jn = alternative.getJsonNumber("confidence");
                                confidence = (float) jn.doubleValue();
                            }
                        }                                                
                    }
                }

                if( !transList.isEmpty() )
                   resultsList.add( new Object[]{transList, new Float(confidence>=0 ? confidence : 0)} );                  
            }
            
            if( resultsList.isEmpty() )
                return null;
            
            // LogService.logIt("GoogleSpeechUtils.parseSpeech2TextFmResultObj() ResultList contains " + resultsList.size() );
            
            return new Speech2TextResult( resultsList );            
        }
        
        catch( IvrScoreException e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.parseSpeech2TextFmResultObj() " );
            
            if( e.isFatal() )
                throw e;
            
            return new Speech2TextResult( e );            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.parseSpeech2TextFmResultObj() " );
            
            throw e;
        }
        
    }

    
    
    
    
    
    /**
     * For voice recordings from Twilio, use base64Audio Str. The encoding is LINEAR16. The sample rate is 8000.
     * 
     * @param thirdPartyAudioId
     * @param base64Audio
     * @param audioGSUri
     * @param encoding
     * @param sampleRate
     * @param langCode
     * @param maxAlternatives
     * @param hints
     * @return
     * @throws Exception 
     */
    private static String getRecognizeRequestJson(  String base64Audio, 
                                                    String audioGSUri, 
                                                    String encoding, 
                                                    int sampleRate, 
                                                    String langCode, 
                                                    int maxAlternatives, 
                                                    String[] hints ) throws Exception
    {
        init();
        
        try
        {        
                 
            if( langCode==null || langCode.isEmpty() )
                throw new Exception( "langCode is invalid: " + langCode );
                                
            JsonObjectBuilder jobTop = Json.createObjectBuilder();

            JsonObjectBuilder jobConfig = Json.createObjectBuilder();
            
            jobConfig.add( "encoding", encoding );
            if( sampleRate>0 )
                jobConfig.add( "sampleRateHertz", sampleRate );
            jobConfig.add( "languageCode", langCode );
            
            if( maxAlternatives>1 )
                jobConfig.add( "maxAlternatives", maxAlternatives );

            if( hints!=null && hints.length>0 )
            {
                JsonObjectBuilder jobContext = Json.createObjectBuilder();
                
                JsonArrayBuilder jaPhrases = Json.createArrayBuilder();
                
                for( String p : hints )
                      jaPhrases.add(p);
                
                jobContext.add( "phrases",  jaPhrases );
                
                JsonArrayBuilder jaContexts = Json.createArrayBuilder();   
                
                jaContexts.add(jobContext );
                
                jobConfig.add( "speechContexts", jaContexts );
            }
            
            jobTop.add("config", jobConfig );
            
            JsonObjectBuilder jobAudio = Json.createObjectBuilder();
            
            if( audioGSUri != null && !audioGSUri.isEmpty() )
                jobAudio.add("uri", audioGSUri );                
            
            else if( base64Audio!=null && !base64Audio.isEmpty() )
                jobAudio.add("content", base64Audio );
            
            else
                throw new Exception( "Either a google storage URI must be provided or the audio must be provided in base64Audio format." );
            
            jobTop.add("audio", jobAudio );
            
            JsonObject jo = jobTop.build();
            
            String payload = JsonUtils.convertJsonObjectToString(jo);
            
            return payload;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleSpeechUtils.getRecognizeRequestJson() base64Audio.length=" + (base64Audio==null ? 0 : base64Audio.length() ) );
            
            throw e;
        }
    }
    
    
    
    private static String getSpeech2TextGoogleOperationJsonAsynch( String payload ) throws Exception
    {
        init();
        
        String postUrl = API_BASE_URL + API_RECOGNIZE_ENDPOINT_ASYNCH + "?key=" + API_KEY;

        try
        {
            return sendApiPost( postUrl, payload );
        }
        
        catch( GoogleApiException e )
        {
            throw e;
        }        
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.getSpeech2TextAsJsonSynch() payload=" + payload );
            
            throw e;
        }
        
    }
    
    
    private static String getSpeech2TextAsJsonSynch( String payload ) throws Exception
    {
        init();
        
        String postUrl = API_BASE_URL + API_RECOGNIZE_ENDPOINT_SYNCH + "?key=" + API_KEY;

        try
        {
            return sendApiPost( postUrl, payload );
        }
        
        catch( GoogleApiException e )
        {
            throw e;
        }        
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.getSpeech2TextAsJsonSynch() payload=" + payload );
            
            throw e;
        }
    }
    
    
    
    public static String sendApiPost( String url, String payload ) throws Exception
    {
        // CloseableHttpResponse r = null;

        try
        {
            init();

            return sendApiPostCore(  url,  payload );
            
            //String json =getJsonFromResponse(r);
            
            //if( isJsonAnError( json ) )
            //    throw new GoogleApiException( "GoogleSpeechUtils.sendApiPost() url=" + url + ", payload.len=" + payload.length() , json );
            
            //return json;
        }
        
        catch( GoogleApiException e )
        {
            throw e;
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.sendApiPost() " );            
            throw e;
        }

        //finally
        //{
        //    if( r != null )
        //    {
        //        if( r.getEntity()!=null )
        //            EntityUtils.consume(r.getEntity());
        //        r.close();
        //    }
        //}
        
    }
    
    
    
    public static String sendApiPostCore( String url, String payload ) throws Exception
    {
        String r = null;

        // int statusCode = 0;

            try (CloseableHttpClient client = HttpUtils.getHttpClient(120))
        {
            init();

            HttpPost post;

            // LogService.logIt( "GoogleSpeechUtils.sendApiPostCore() Preparing Request" );

            post = new HttpPost( url );
            
            post.setEntity( new StringEntity( payload ) );

            //int length = payload == null ? 0 : payload.length();
            
            //post.setHeader( "Content-Length", length + "" );
            post.addHeader( "Content-Type", "application/json; charset=UTF-8" );
            post.addHeader( "Accept", "application/json" );

            // LogService.logIt( "GoogleSpeechUtils.sendApiPostCore() Executing Request" );
            r = client.execute( post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "GoogleSpeechUtils.sendApiPostCore() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "GoogleSpeechUtils.sendApiPostCore() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    } );
            
            // LogService.logIt( "GoogleSpeechUtils.sendApiPostCore() url=" + url + ", Response Status Code : " + r.getCode() );
            
            //statusCode = r.getCode();

            //if( !isStatusCodeOk( statusCode ) )
            //{
            //    LogService.logIt( "GoogleSpeechUtils.sendApiPostCore() Method failed: " + r.getReasonPhrase() + ", url=" + url );
                
            //    String s = getJsonFromResponse(r);
                
            //    throw new GoogleApiException( "GoogleSpeechUtils.sendApiPostCore() Get failed with Http statuscode " + r.getReasonPhrase() + " uri=" + url + ", payload.len=" + payload.length(), s );
            //}
            
            return r;
        }
        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "SGoogleSpeechUtils.sendApiPostCore() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new GoogleApiException( e.toString(), r );
        }        
        catch( IOException e )
        {
            LogService.logIt( "GoogleSpeechUtils.sendApiPostCore() STERR " + e.toString() +", url=" + url + ", payload=" + payload );            
            throw new GoogleApiException( e.toString(), r );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.sendApiPostCore() url=" + url + ", payload=" + payload );            
            throw e;
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
                LogService.logIt( e, "GoogleSpeechUtils.isJsonAnError() Parsing JSON for error" );
            }
        }
        
        return false;        
    }
    
    
    /*
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
    */
    
    
    public static String sendApiGet( String url ) throws Exception
    {
        String r = null;

        // int statusCode = 0;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(120))
        {
            init();

            // CloseableHttpClient client =  HttpClients.createDefault(); //  HttpClientBuilder.create().build();

            HttpGet get;

            // LogService.logIt( "GoogleSpeechUtils.sendApiGet() Preparing Request" );

            get = new HttpGet( url );

            get.addHeader( "Accept", "application/json" );

            // LogService.logIt( "GoogleSpeechUtils.sendApiGet() Executing Request" );
            r = client.execute( get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "GoogleSpeechUtils.sendApiGet() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "GoogleSpeechUtils.sendApiGet() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    } );

            // LogService.logIt( "GoogleSpeechUtils.sendApiGet() url=" + url + ", Response Code : " + r.getCode() );

            // String s = getJsonFromResponse( r );

            // LogService.logIt( "GoogleSpeechUtils.sendApiGet() Response is: " + s + ", url=" + url );

            //statusCode = r.getCode();

           // if( !isStatusCodeOk( statusCode ) )
            //{
            //    LogService.logIt( "GoogleSpeechUtils.sendApiGet() Method failed: " + r.getReasonPhrase() + ", url=" + url );
                
            //    throw new GoogleApiException( "GoogleSpeechUtils.sendApiGet() Get failed with Http statuscode " + r.getReasonPhrase() + " uri=" + url, s );                
            //}
            
            return r;
        }
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "GoogleSpeechUtils.sendApiGet() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new GoogleApiException( e.toString() + ", url=" + url, r );
        }        
        catch( IOException e )
        {
            LogService.logIt( "GoogleSpeechUtils.sendApiGet() STERR " + e.toString() + ", url=" + url );            
            throw new GoogleApiException( e.toString() + ", url=" + url, r );                
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleSpeechUtils.sendApiGet() url=" + url );            
            throw e;
        }

        //finally
        //{
        //    if( r != null )
        //        r.close();
        //}
    }
    
    
    
}
