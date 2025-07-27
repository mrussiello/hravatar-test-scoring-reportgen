/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.amazoncloud;


import com.tm2score.file.FileXferUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.ivr.IvrScoreException;
import com.tm2score.service.LogService;
import com.tm2score.util.HttpUtils;
import com.tm2score.util.JsonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.GetTranscriptionJobResponse;
import software.amazon.awssdk.services.transcribe.model.InternalFailureException;
import software.amazon.awssdk.services.transcribe.model.Media;
import software.amazon.awssdk.services.transcribe.model.Settings;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobRequest;
import software.amazon.awssdk.services.transcribe.model.StartTranscriptionJobResponse;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJob;
import software.amazon.awssdk.services.transcribe.model.TranscriptionJobStatus;
import software.amazon.awssdk.services.transcribe.model.ConflictException;

/**
 *
 * @author miker_000
 */
public class AmazonTranscribeUtils 
{    
    public static final float MIN_AMAZON_S2T_DURATION = 0.90f;
    public static String[] VALID_LANGS = new String[] {"en-US","es-US","en-AU","fr-CA","en-GB","de-DE","pt-BR","fr-FR","it-IT","ko-KR","es-ES","en-IN","hi-IN","ar-SA","ru-RU","zh-CN","nl-NL","id-ID","ta-IN","fa-IR","en-IE","en-AB","en-WL","pt-PT","te-IN","tr-TR","de-CH","he-IL","ms-MY","ja-JP","ar-AE"};
    
    TranscribeClient transcribeClient;
    
    FileXferUtils fileXfer;
    
    private synchronized void initClient() throws Exception
    {
        if( transcribeClient!=null )
            return;
        
        try
        {
            AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKeyTranscribe" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKeyTranscribe" )).build();            
            StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );
            transcribeClient = TranscribeClient.builder().region(getClientRegion()).credentialsProvider(bac).build();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranscribeUtils.initClient() " );
            throw e;
        }
    }
    
    public Region getClientRegion()
    {
        int rid = RuntimeConstants.getIntValue("awsRekognitionRegionId");
        
        if( rid==1 )
            return Region.US_EAST_1;
        if( rid==12 )
            return Region.US_WEST_2;
        return Region.US_EAST_1;
    }
    
    
    
    /**
     * Returns 
     *    SUCCESS - completed.
     *    FAILED  - Failed
     *    PENDING
     *     
     * 
     * @param locale
     * @param mediaFileUri - S3 Bucket URI: https://s3.us-east-1.amazonaws.com/examplebucket/example.mp4
     * @param mediaFormat   - mp3 | mp4 | wav | flac | webm
     * @param outputBucketName - the bucket name to place the transcription in
     */
    public String startTranscriptionJob( Locale locale, String mediaFileUri, String mediaFormat, String outputBucketName, String transJobName ) throws Exception
    {
        try
        {
            initClient();
            
            StartTranscriptionJobRequest.Builder stjrBuilder = StartTranscriptionJobRequest.builder();
            
            String langCode = getLangStr( locale );
                        
            if( !isValidLocale( langCode ) )
                throw new Exception( "LangCode is not valid: " + langCode );
            
            stjrBuilder.languageCode( langCode ); // locale.getLanguage() + "-" + locale.getCountry() );
            
            Media media = Media.builder().mediaFileUri(mediaFileUri).build();            
            // media.setMediaFileUri(mediaFileUri);            
            stjrBuilder.media( media );
            
            stjrBuilder.mediaFormat(mediaFormat);
            stjrBuilder.outputBucketName(outputBucketName);
            
            //stjr.setMediaFormat(mediaFormat);
            //stjr.setOutputBucketName( outputBucketName );
            
            Settings.Builder settingsBuilder = Settings.builder().showAlternatives(Boolean.FALSE).channelIdentification(Boolean.FALSE).showSpeakerLabels(Boolean.FALSE);
            
            //settings.setShowAlternatives(false);
            //settings.setChannelIdentification(false);
            //settings.setShowSpeakerLabels(false);
            
            if( locale.equals( Locale.US ) ) // locale.getLanguage().equalsIgnoreCase( "en" ) )
            {
                settingsBuilder.vocabularyFilterName("Obscene1");
                settingsBuilder.vocabularyFilterMethod("mask");
                //settings.setVocabularyFilterName("Obscene1");
                //settings.setVocabularyFilterMethod("mask");
            }
            else if( locale.toString().equalsIgnoreCase("en_GB") ) // locale.getLanguage().equalsIgnoreCase( "en" ) )
            {
                settingsBuilder.vocabularyFilterName("Obscene1GB");
                settingsBuilder.vocabularyFilterMethod("mask");
                //settings.setVocabularyFilterName("Obscene1GB");
                //settings.setVocabularyFilterMethod("mask");
            }
            else if( locale.toString().equalsIgnoreCase("en_AU") ) // locale.getLanguage().equalsIgnoreCase( "en" ) )
            {
                settingsBuilder.vocabularyFilterName("Obscene1AU");
                settingsBuilder.vocabularyFilterMethod("mask");
                //settings.setVocabularyFilterName("Obscene1AU");
                //settings.setVocabularyFilterMethod("mask");
            }
            else if( locale.toString().equalsIgnoreCase("en_IN") ) // locale.getLanguage().equalsIgnoreCase( "en" ) )
            {
                settingsBuilder.vocabularyFilterName("Obscene1IN");
                settingsBuilder.vocabularyFilterMethod("mask");
                //settings.setVocabularyFilterName("Obscene1IN");
                //settings.setVocabularyFilterMethod("mask");
            }

            stjrBuilder.settings(settingsBuilder.build());
            stjrBuilder.transcriptionJobName(transJobName);
            
            StartTranscriptionJobResponse result = transcribeClient.startTranscriptionJob(stjrBuilder.build());
            
            TranscriptionJob tj = result.transcriptionJob();
            
            TranscriptionJobStatus status = tj.transcriptionJobStatus();

            //TranscriptionJobStatus status = TranscriptionJobStatus.fromValue( tj.getTranscriptionJobStatus() );
            
            if( status.equals( TranscriptionJobStatus.FAILED ) )
            {
                String reason = tj.failureReason();                
                LogService.logIt( "AmazonTranscribeUtils.startTranscriptionJob() Job Failed: locale=" + locale.toString() + ", mediaFileUri=" + mediaFileUri + ", mediaFormat=" + mediaFormat + ", outputBucketName=" + outputBucketName + ", transJobName=" + transJobName + " reason=" + reason );                
                return "FAILED: " + reason;
            }

            if( status.equals( TranscriptionJobStatus.COMPLETED ) )
                return "SUCCESS";

            return "PENDING";            
        }
        catch( InternalFailureException e )
        {
            LogService.logIt( "AmazonTranscribeUtils.startTranscriptionJob() ERROR InternalFailureException returning FAILED. " + e.toString() + ", locale=" + locale.toString() + ", mediaFileUri=" + mediaFileUri + ", mediaFormat=" + mediaFormat + ", outputBucketName=" + outputBucketName + ", transJobName=" + transJobName );
            return "FAILED: " + e.toString();
        }        
        catch( ConflictException e )
        {
            LogService.logIt( "AmazonTranscribeUtils.startTranscriptionJob() NONFATAL ConflictException returning checkTranscriptionJobStatus(), " + e.toString() + ", locale=" + locale.toString() + ", mediaFileUri=" + mediaFileUri + ", mediaFormat=" + mediaFormat + ", outputBucketName=" + outputBucketName + ", transJobName=" + transJobName );
            
            return checkTranscriptionJobStatus(transJobName);
            
            // return "PENDING";
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranscribeUtils.startTranscriptionJob() XXX.1 locale=" + locale.toString() + ", mediaFileUri=" + mediaFileUri + ", mediaFormat=" + mediaFormat + ", outputBucketName=" + outputBucketName + ", transJobName=" + transJobName );
            throw new IvrScoreException( 0, "AmazonTranscribeUtils.startTranscriptionJob() " + e.toString() + ", transJobName=" + transJobName );
        }
    }


    /**
     * Returns 
     *   data[0] = the text
     *   data[1] = average confidence.
     * 
     * @param transcriptUri
     * @param deleteOnSuccess
     * @return
     * @throws Exception 
     */
    public Object[] getTranscriptionText(  String transcriptUri, boolean deleteOnSuccess ) throws Exception
    {
        String jobName = null;
        try
        {
            initClient();
            
            String jsonStr = this.getTranscriptionJsonStr(transcriptUri);
            
            if( jsonStr==null || jsonStr.isBlank() )
            {
                LogService.logIt( "AmazonTranscribeUtils.getTranscriptionText() Result from getTranscriptionJsonStr is null or empty.");               
                throw new IvrScoreException( 0, "AmazonTranscribeUtils.getTranscriptionText() JsonStr is empty. transcriptUri=" + transcriptUri );
            }
            
            JsonObject jo = JsonUtils.convertJsonStringToObject(jsonStr); //  getTranscriptionJsonObject(  transcriptUri );
            
            if( jo==null )
                throw new Exception( "Json Object is null." );
            
            jobName = jo.containsKey( "jobName" ) && !jo.isNull( "jobName" ) ? jo.getString( "jobName" ) : null;
            
            JsonObject res = jo.containsKey( "results" ) && !jo.isNull("results") ? jo.getJsonObject("results") : null;
            
            if( res==null )
                throw new Exception( "No results object inside transcript Json. jobName=" + jobName + ", json=" + jsonStr );
            
            JsonArray transcripts = res.containsKey( "transcripts" ) && !res.isNull("transcripts") ? res.getJsonArray( "transcripts" ) : null;
            
            if( transcripts==null )
                throw new Exception( "No transcripts object inside transcript Json result field. JobName=" + jobName +", json=" + jsonStr );
            
            JsonArray items = res.containsKey( "items" ) && !res.isNull("items") ? res.getJsonArray( "items" ) : null;
            
            StringBuilder fullTran = new StringBuilder();
            
            JsonObject tran;
            
            String text;
            float conf;
            String type;
            float totalConf = 0;
            float confCount = 0;
            
            for( JsonValue jv : transcripts )
            {
                tran = (JsonObject) jv;
                text = tran.containsKey( "transcript" ) && !tran.isNull( "transcript" ) ? tran.getString( "transcript" ) : null;
                
                if( text!=null && !text.isBlank() )
                {
                    if( fullTran.length()>0 )
                        fullTran.append( "\n\n" );
                    
                    fullTran.append( text );
                }
            }
                        
            if( items !=null )
            {
                JsonObject item;
                JsonArray alternatives;
                JsonObject altJo;
                
                // LogService.logIt( "AmazonTranscribeUtils.getTranscriptionText() items=" + items.size() );
                
                for( JsonValue jv : items )
                {
                    item = (JsonObject) jv;
                    
                    type=null;
                    if( item.containsKey("type") && !item.isNull( "type" ) )
                        type = item.getString("type" );
                    
                    // LogService.logIt( "AmazonTranscribeUtils.getTranscriptionText() item.type=" + type );

                    if( type!=null && !type.equalsIgnoreCase( "pronunciation" ))
                        continue;
                    
                    alternatives = item.containsKey( "alternatives" ) && !item.isNull( "alternatives" ) ? item.getJsonArray("alternatives" ) : null;
                    
                    if( alternatives != null )
                    {
                        for( JsonValue jva : alternatives )
                        {
                            altJo = (JsonObject) jva;
                            
                            if( !altJo.containsKey( "confidence" ) )
                                continue;
                            
                            conf = Float.parseFloat( altJo.getString( "confidence" ) );
                            
                            //ValueType vt = altJo.getValueType();
                            
                            //if( vt.equals( ValueType.STRING ) || vt.equals( ValueType.OBJECT ) )
                            //    conf = Float.parseFloat( altJo.getString( "confidence" ) );
                            //else if( vt.equals( ValueType.NUMBER ) )
                            //    conf = (float) altJo.getJsonNumber( "confidence" ).doubleValue();
                            //else
                            //    conf = -1;

                            // LogService.logIt( "AmazonTranscribeUtils.getTranscriptionText() ValueType=" + vt.name() + ", conf=" + conf );
                            
                            // conf = altJo.containsKey( "confidence" ) ? (float) altJo.getJsonNumber( "confidence" ).doubleValue() : -1;

                            if( conf>=0 )
                            {
                                totalConf += conf;
                                confCount++;
                            }
                        }                        
                    }                    
                }                

                // LogService.logIt( "AmazonTranscribeUtils.getTranscriptionText() items=" + items.size() + ", confCount=" + confCount + ", totalConf=" + totalConf );

            }
            
            if( confCount>0 )
                totalConf = totalConf/confCount;
                        
            return new Object[] { fullTran.toString(), (float) totalConf };
        }
        catch( IvrScoreException e )
        {
            LogService.logIt( "AmazonTranscribeUtils.getTranscriptionText() ERROR " + e.toString() + ", JobName=" + jobName + ", transcriptUri=" + transcriptUri );
            throw e;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranscribeUtils.getTranscriptionText() JobName=" + jobName + ", transcriptUri=" + transcriptUri );
            throw e;
        }
    }

    
    
    public String getTranscriptionJsonStr(  String transcriptUri ) throws Exception
    {
        try
        {
            if( fileXfer==null )
                fileXfer = new FileXferUtils();
            
            String jsonStr =  sendHttpGet( transcriptUri );
            
            // String jsonStr = new String(bytes,"UTF8");
            
            return jsonStr;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranscribeUtils.getTranscriptionJsonObject() transcriptUri=" + transcriptUri );
            throw e;
        }
    }
    
    
    
    
    
    
    public String sendHttpGet( String url ) throws Exception
    {
        // CloseableHttpResponse r = null;

        // int statusCode = 0;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(120))
        {
            HttpGet get;

            // LogService.logIt( "AmazonTranscribeUtils.sendApiGet() Preparing Request" );

            get = new HttpGet( url );

            get.addHeader( "Accept", "application/json" );
            
            // LogService.logIt( "AmazonTranscribeUtils.sendApiGet() Executing Request" );
            
            //try( CloseableHttpResponse r = client.execute( get ) )
            //{
            String s = client.execute(get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "AmazonTranscribeUtils.sendApiGet() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "AmazonTranscribeUtils.sendApiGet() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    } );

                // LogService.logIt( "AmazonTranscribeUtils.sendApiGet() url=" + url + ", Response Code : " + r.getCode() );

                // StringBuilder sb = processAPIResponse( r );            

                // LogService.logIt( "AmazonTranscribeUtils.sendApiGet() url=" + url + ", response is: " + sb.toString() );

                // statusCode = r.getCode();


                //if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "AmazonTranscribeUtils.sendApiGet() ERROR Method failed: " + r.getReasonPhrase() + ", url=" + url );
                //    return null;
                    // throw new Exception( "AmazonTranscribeUtils.sendApiGet()  Get failed with Http statuscode " + r.getReasonPhrase() + ", response text was: " + sb.toString() );
                //}

                //if( r.getEntity()!=null )
                //    EntityUtils.consume(r.getEntity());

                // LogService.logIt( "AmazonTranscribeUtils.sendApiGet() response as text: " + sb.toString() );

                return s;
            // }
        }
                
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "AmazonTranscribeUtils.sendApiGet()STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw e;
        }        
        catch( IOException e )
        {
            LogService.logIt( "AmazonTranscribeUtils.sendApiGet() " + e.toString() +", url=" + url );            
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranscribeUtils.sendApiGet() url=" + url );            
            throw e;
        }

        //finally
        //{
            //if( r != null )
            //    r.close();
        //}
    }
    
    //private boolean isStatusCodeOk( int statusCode )
    //{
    //    return statusCode>=200 && statusCode<300;
    //}
    
    /**
     * 
     * @param response
     * @return
     * @throws IOException
     * @throws Exception 
     */
    public static StringBuilder processAPIResponse(CloseableHttpResponse response) throws IOException, Exception {

        StringBuilder sb = null;

        try {
                if( response == null )
                    LogService.logIt( "AmazonTranscribeUtils.processAPIResponse() response is null! Returning null" );
                
                else if( response.getEntity() == null )
                    LogService.logIt( "AmazonTranscribeUtils.processAPIResponse() response.getEntity is null! Returning null" );
                
                else if( response.getEntity().getContent() == null )
                    LogService.logIt( "AmazonTranscribeUtils.processAPIResponse() response.getEntity().getContent() is null! Returning null" );
                
                //if ( response != null ) 
                else {

                        String line = "";
                        sb = new StringBuilder();

                        InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
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

        return sb;
    }
    
    
    
    
    /**
     * Returns the SUCCESS:transcriptURI or FAILED OR PENDING
     * 
     * @param transJobName
     * @return
     * @throws Exception 
     */
    public String checkTranscriptionJobStatus( String transJobName ) throws Exception
    {
        try
        {
            initClient();
            
            GetTranscriptionJobRequest.Builder tjrBuilder = GetTranscriptionJobRequest.builder();
            
            tjrBuilder.transcriptionJobName(transJobName);
            
            GetTranscriptionJobResponse result = transcribeClient.getTranscriptionJob(tjrBuilder.build());
            
            TranscriptionJob tj = result.transcriptionJob();
            
            TranscriptionJobStatus status = tj.transcriptionJobStatus();
            //TranscriptionJobStatus status = TranscriptionJobStatus.fromValue( tj.getTranscriptionJobStatus() );
            
            if( status.equals( TranscriptionJobStatus.FAILED ) )
            {
                String reason = tj.failureReason();                
                LogService.logIt( "AmazonTranscribeUtils.startTranscriptionJob() Job Failed: transJobName=" + transJobName + " reason=" + reason );                
                return "FAILED: " + reason;
            }

            if( status.equals( TranscriptionJobStatus.COMPLETED ) )
                return "SUCCESS:" + tj.transcript().transcriptFileUri();

            return "PENDING";            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranscribeUtils.checkTranscriptionJobStatus() transJobName=" + transJobName );
            throw new IvrScoreException( 0, "AmazonTranscribeUtils.checkTranscriptionJobStatus() transJobName=" + transJobName );
            // throw e;
        }
    }

    public static boolean isValidLocale( Locale l )
    {
        String langStr = getLangStr(  l );
        
        return isValidLocale( langStr );
    }
    
    
    private static boolean isValidLocale( String ln )
    {
        for( String tl : VALID_LANGS )
        {
            if( tl.equalsIgnoreCase(ln) )
                return true;
        }
        return false;
    }
    
    
    public static String getLangStr( Locale locale )
    {
            String langCode = locale.getLanguage().toLowerCase() + "-" + locale.getCountry().toUpperCase();

            if( isValidLocale( langCode ) )
                return langCode;
                        
            if( locale.getLanguage().equalsIgnoreCase("es") )
                langCode = locale.getCountry()!=null && locale.getCountry().equalsIgnoreCase("MX") ? "es-US" :"es-ES";
            
            if( locale.getLanguage().equalsIgnoreCase("fr") )
                langCode = "fr-FR";
            
            if( locale.getLanguage().equalsIgnoreCase("nl") )
                langCode = "nl-NL";
            
            if( locale.getLanguage().equalsIgnoreCase("he") )
                langCode = "he-IL";
            
            if( locale.getLanguage().equalsIgnoreCase("ar") )
                langCode = "ar-SA";
            
            if( isValidLocale( langCode ) )
                return langCode;
                        
            String lang = locale.getLanguage().toLowerCase();
            
            for( String lc : VALID_LANGS )
            {
                if( lc.startsWith(lang) )
                    return lc;
            }
            
            return langCode;
        
    }
}
