/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.twilio;

import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.Base64Encoder;
import com.tm2score.util.HttpUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
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
public class TwilioRestUtils 
{
    
    // URI is:  /2010-04-01/Accounts/{AccountSid}/Recordings/{RecordingSid}
    private static String TWILIO_BASE_URI = "https://api.twilio.com/2010-04-01";
    private static String TWILIO_SID = null;
    private static String TWILIO_AUTH = null;
    
    
    private synchronized static void init()
    {
        if( TWILIO_SID == null )
        {
            TWILIO_SID = RuntimeConstants.getStringValue( "twilio.sid" );
            TWILIO_AUTH = RuntimeConstants.getStringValue( "twilio.auhtoken" );
        }        
    }
    
    
    
    
    
    
    public boolean deleteAudioFiles( String audioSid, String extraAudioIds )
    {
        List<String> aidList = new ArrayList<>();
        
        if( extraAudioIds!=null && !extraAudioIds.trim().isEmpty() )
        {
            for( String aid : extraAudioIds.split(";") )
            {
                if( aid==null || aid.trim().isEmpty() )
                    continue;
                
                aidList.add( aid.trim() );
            }
        } 

        int failCount = 0;
        
        if( audioSid!=null && !audioSid.trim().isEmpty() )
            aidList.add( audioSid );
            
        for( String aid : aidList )
        {
            try
            {
                deleteAudioFile( aid );
            }
            
            catch( TwilioException e )
            {
                LogService.logIt( "TwilioRestUtils.deleteAudioFile() aid=" + aid + ", TwilioException " + e.toString() ); 
                failCount++;
            }
        }
        
        return failCount<=0;
    }

    
    
    
    private void deleteAudioFile( String audioSid ) throws TwilioException
    {
        try
        {
            init();
            
            if( audioSid==null || audioSid.trim().isEmpty() )
                throw new Exception( "AudioSid is invalid. " + audioSid );
            
            String url = TWILIO_BASE_URI + "/Accounts/" + TWILIO_SID + "/Recordings/" + audioSid;
            
            LogService.logIt( "TwilioRestUtils.deleteAudioFile() Sending to URI=" + url );
            boolean success = sendApiDelete(url);
            LogService.logIt( "TwilioRestUtils.deleteAudioFile() Back from sending to URI=" + url + ", success=" + success );
            if( success )
                return;
            throw new Exception( "Delete failed." );
        }
        catch( TwilioException e )
        {
            LogService.logIt( "TwilioRestUtils.deleteAudioFile() TwilioException " + e.toString() );
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TwilioRestUtils.deleteAudioFile() " );
            throw new TwilioException( "TwilioRestUtils.deleteAudioFile() audioSid=" + audioSid + ", " + e.toString() );
        }
    }

    
    
    
    public Map<String,String>  getBasicAuthParmsForResultsPost( String username, String pwd )
    {
        Map<String,String> out = new HashMap<>();

        out.put( "username", username );
        out.put( "password", pwd );

        return out;
    }
    
    

    public boolean sendApiDelete( String url ) throws Exception
    {
        // CloseableHttpResponse r = null;

        //int statusCode = 0;

        try
        {
            init();

            try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
            {

                HttpDelete delete;

                LogService.logIt( "TwilioRestUtils.sendApiDelete() Preparing Request" );

                delete = new HttpDelete( url );

                Map<String,String> basicAuthCreds = getBasicAuthParmsForResultsPost( TWILIO_SID, TWILIO_AUTH );

                if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
                {
                    String un = basicAuthCreds.get( "username" );
                    String pwd = basicAuthCreds.get( "password" );

                    String b6 = Base64Encoder.encodeString( un + ":" + pwd );
                    delete.setHeader( "Authorization", "Basic " + b6  );
                    LogService.logIt( "TwilioRestUtils.sendApiDelete() Set basic Auth header: Basic " + b6);
                }                

                LogService.logIt( "TwilioRestUtils.sendApiDelete() Executing Request" );
                client.execute(delete, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "TwilioRestUtils.sendApiDelete() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    //String ss = EntityUtils.toString(entity2);
                    if( entity2!=null )
                        EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "TwilioRestUtils.sendApiDelete() statusCode="+ status + ", reason=" + response.getReasonPhrase() );
                    return null;
                    } );

                // LogService.logIt( "TwilioRestUtils.sendApiDelete() url=" + url + ", Response Status Code : " + r.getCode() );

                // statusCode = r.getCode();

                // if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "TwilioRestUtils.sendApiDelete() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                //    throw new TwilioException( "TwilioRestUtils.sendApiDelete()  Delete failed with Http statuscode " + r.getReasonPhrase() );
                //}
            }

        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "BaseRefClient.sendApiPostCore() STERR " + e.toString() + ", url=" + url + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
                throw new TwilioException(e.toString());
        }        
            catch( IOException e )
            {
                LogService.logIt( "TwilioRestUtils.sendApiDelete() STERR " + e.toString() + ", EEE.1 url=" + url );
                throw new TwilioException(e.toString());
            }
            catch( Exception e )
            {
                LogService.logIt( e, "TwilioRestUtils.sendApiDelete() EEE.2 " + url );
                throw e;
            }
            
            return true;
            
        }
        
        catch( TwilioException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TwilioRestUtils.sendApiDelete() " );            
            throw e;
        }
    }
}
