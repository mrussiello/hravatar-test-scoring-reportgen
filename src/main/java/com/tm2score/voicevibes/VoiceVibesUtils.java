/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.voicevibes;

import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.purchase.Credit;
import com.tm2score.entity.user.Org;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.av.AvEventFacade;
import com.tm2score.av.AvItemResponsePrepThread;
import com.tm2score.file.HttpFileUtils;
import com.tm2score.purchase.CreditSourceType;
import com.tm2score.purchase.PurchaseFacade;
import com.tm2score.score.ScoreUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.AudioAppender;
import com.tm2score.util.Base64Encoder;
import com.tm2score.util.HttpUtils;
import com.tm2score.util.JsonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
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
public class VoiceVibesUtils {
    
    public static final float OPTIMUM_DURATION_VIBES_SAMPLE = 60;

    public static final float MINIMUM_DURATION_VIBES_SAMPLE = 25;

    
    PurchaseFacade purchaseFacade;
    
    AvEventFacade avEventFacade;
    
    private Boolean voiceVibesOk = null;
    
    private Date lastAuth = null;
    
    private synchronized void init()
    {
        if( voiceVibesOk == null )
            voiceVibesOk = RuntimeConstants.getBooleanValue( "voiceVibes.VibesOn" );
    }
    
    public boolean isVoiceVibesOn()
    {
        init();
        
        return voiceVibesOk.booleanValue();
    }
        
    
    
    public static VoiceVibesResult combineVoiceVibesResults( List<VoiceVibesResult> vvrl ) throws Exception
    {
        // LogService.logIt( "VoiceVibesUtils.combineVoiceVibesResults() START items to combine: " + vvrl.size() );
        
        float count = 0;
        float value;
        
        int totalWordCount = 0;
        VoiceVibesScaleScore vvss;            
        Integer spotCt;
        Integer combinedSpotCt;
        

        for( VoiceVibesResult vvr : vvrl )
        {
            totalWordCount += vvr.getWordCount();
            
            // LogService.logIt( "VoiceVibesUtils.combineVoiceVibesResults() adding " + vvr.toString() );
        }
        
        Map<String,Integer> combinedWordSpotMap = new HashMap<>();

        
        List<VoiceVibesScaleScore> combinedScaleScores = new ArrayList<>();
        
        VoiceVibesScaleScore combinedWordSpotScaleScore = null;
        
        VoiceVibesScaleScore tempScaleScore = null;
        
        
        for( VoiceVibesScaleType vvst : VoiceVibesScaleType.values())
        {
            count=0;
            value=0;

            for( VoiceVibesResult vvr : vvrl )
            {
                vvss = vvr.getVoiceVibesScaleScore(vvst);
                
                if( vvss==null )
                    continue;
                
                count++;
                value += vvss.getScore();
                
                if( vvss.getVoiceVibesScaleType().isWordspot() && vvss.getWordSpotMap()!=null )
                {
                    for( String key : vvss.getWordSpotMap().keySet() )
                    {
                        spotCt = vvss.getWordSpotMap().get(key);
                        
                        if( spotCt==null || spotCt==0 )
                            continue;
                        
                        combinedSpotCt = combinedWordSpotMap.get(key);
                        
                        if( combinedSpotCt==null )
                            combinedSpotCt = new Integer(0);
                        
                        combinedSpotCt = new Integer( combinedSpotCt.intValue() + spotCt );
                        
                        combinedWordSpotMap.put( key, combinedSpotCt );
                    }
                }
            }
            
            if( count> 0 )
                value = value/count;
            
            tempScaleScore = new VoiceVibesScaleScore( vvst, value);
            
            if( tempScaleScore.getVoiceVibesScaleType().isWordspot() )
                combinedWordSpotScaleScore = tempScaleScore;
            
            combinedScaleScores.add( tempScaleScore );
        }
        
        if( combinedWordSpotScaleScore!=null && combinedWordSpotMap!=null && !combinedWordSpotMap.isEmpty() )
            combinedWordSpotScaleScore.setWordSpotMap(combinedWordSpotMap);
        
        VoiceVibesResult vvrx = new VoiceVibesResult( combinedScaleScores, totalWordCount );
        
        // LogService.logIt( "VoiceVibesUtils.combineVoiceVibesResults() COMPLETED " + vvrx.toString() );        
        
        return vvrx;
    }
    
    
    /**
     * Will delete a recording and will add a note if the delete fails. 
     * 
     * @param o
     * @param te
     * @param iir
     * @throws Exception 
     */
    public void deleteVoiceVibesRecording( Org o, TestEvent te, AvItemResponse iir ) throws Exception
    {
        try
        {
            init();            
            
            if( !voiceVibesOk.booleanValue() )
                throw new Exception( "VoiceVibes is not enabled. But VoiceVibesStatusTypeId=" + iir.getVoiceVibesStatusTypeId()  );
             
            if( iir.getVoiceVibesId()==null || iir.getVoiceVibesId().isEmpty() )
                throw new Exception( "VoiceVibesId is missing: " + iir.getVoiceVibesId() );
            
            if( !iir.getVoiceVibesStatusType().equals( VoiceVibesStatusType.ANALYSIS_COMPLETE ) )
                throw new Exception( "VoiceVibesStatus is invalid. Expected " + VoiceVibesStatusType.ANALYSIS_COMPLETE.getVoiceVibesStatusTypeId() + ", but found: " + iir.getVoiceVibesStatusTypeId() );
                                    
            checkVoiceVibesAuth( o, te, iir.getVoiceVibesAccountType() );
            
            String[] creds = getVoiceVibesCredentials( o, te, iir.getVoiceVibesAccountType() );
                        
            String url = getVoiceVibesUrl( "/recordings/"  + iir.getVoiceVibesId() );
            
            this.sendApiDelete(url, creds);

            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.DELETED.getVoiceVibesStatusTypeId() );
            
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();
            
            avEventFacade.saveAvItemResponse( iir );
        }
        
        catch( VoiceVibesException e )
        {
            LogService.logIt( e, "VoiceVibesUtils.deleteVoiceVibesRecording() recording voice vibes error in avItemResponse. " + iir.toString() );
            
            // iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.ERROR.getVoiceVibesStatusTypeId() );
            iir.appendNotes( "VoiceVibesException NonFatal error deleting record from VoiceVibes: " + e.getMessage() );
            
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();    
            
            avEventFacade.saveAvItemResponse(iir);
            
            // throw new Exception( e.getMessage() );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.deleteVoiceVibesRecording() " + iir.toString() );
            
            throw e;
        }
        
    }

    
    
    /**
     * Returns if results are not ready yet. 
     * 
     * Otherwise stores the results and updates the status, or throws an exception if there is an error. 
     * 
     * @param o
     * @param te
     * @param iir
     * @return
     * @throws Exception 
     */
    public void checkForVoiceVibesResults( Org o, TestEvent te, AvItemResponse iir ) throws Exception            
    {
        try
        {
            init();
            
            // Haven't waited long enough.
            if( !iir.isVoiceVibesPostReadyForResultsPull() )
                return;
            
            if( !voiceVibesOk.booleanValue() )
                throw new Exception( "VoiceVibes is not enabled. But VoiceVibesStatusTypeId=" + iir.getVoiceVibesStatusTypeId()  );
             
            if( iir.getVoiceVibesStatusType().isAnyError() )
            {
                LogService.logIt( "VoiceVibesUtils.checkForVoiceVibesResults() VoiceVibes Status om AvItemResponse is Error. " + iir.toString() );
                return;
            }
                        
            if( iir.getVoiceVibesId()==null || iir.getVoiceVibesId().isEmpty() )
                throw new Exception( "VoiceVibesId is missing: " + iir.getVoiceVibesId() );
            
            if( !iir.getVoiceVibesStatusType().equals( VoiceVibesStatusType.POSTED ) )
                throw new Exception( "VoiceVibesStatus is invalid. Expected " + VoiceVibesStatusType.POSTED.getVoiceVibesStatusTypeId() + ", but found: " + iir.getVoiceVibesStatusTypeId() );
            
            checkVoiceVibesAuth( o, te, iir.getVoiceVibesAccountType() );
            
            String[] creds = getVoiceVibesCredentials( o, te, iir.getVoiceVibesAccountType() );
                        
            String url = getVoiceVibesUrl( "/recordings/"  + iir.getVoiceVibesId() );
            
            String responseJson = sendApiGet(url, creds);
            
            // LogService.logIt( "VoiceVibesUtils.checkForVoiceVibesResults()  " + iir.toString() );

            if( responseJson!=null && !responseJson.isEmpty() )
            {
                if( doesResponseJsonHaveResults( responseJson ) )
                {
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.ANALYSIS_COMPLETE.getVoiceVibesStatusTypeId() );
                    iir.setVoiceVibesResponseStr(responseJson);

                    if( avEventFacade==null )
                        avEventFacade = AvEventFacade.getInstance();            
                    avEventFacade.saveAvItemResponse(iir);
                }
                else
                {
                    String status = getJsonStatus( responseJson );
                    
                    if( status!=null && status.equals( "processing" ) )
                    {
                        // LogService.logIt( "VoiceVibesUtils.checkForVoiceVibesResults() VoiceVibes still processing. avItemResponseId=" + iir.getAvItemResponseId() + ", testEventId=" + iir.getTestEventId() );
                    }
                    else
                    {
                        iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.ERROR.getVoiceVibesStatusTypeId() );
                        iir.appendNotes( "VoiceVibesUtils.checkForVoiceVibesResults() ResponseJson status=" + status + ". Treating as an error. ResponseJson=" + responseJson + "\n" );

                        if( avEventFacade==null )
                            avEventFacade = AvEventFacade.getInstance();            
                        avEventFacade.saveAvItemResponse(iir);                    
                        LogService.logIt( "VoiceVibesUtils.checkForVoiceVibesResults() ResponseJson status=" + status + ". Treating as an error. ResponseJson=" + responseJson + ", " + iir.toString() );
                    }
                }
            }
        }        
        catch( IOException e )
        {
            LogService.logIt( "VoiceVibesUtils.checkForVoiceVibesResults() IOException recording voice vibes error in avItemResponse. " + iir.toString() + ", " + e.toString() );
            
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.ERROR.getVoiceVibesStatusTypeId() );
            iir.appendNotes( "VoiceVibesException Posting Audio: " + e.toString());
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();            
            avEventFacade.saveAvItemResponse(iir);
        }
        catch( VoiceVibesException e )
        {
            if( e.toString().contains( "File is mute.") || e.toString().contains("SocketTimeoutException") )
                LogService.logIt( "VoiceVibesUtils.checkForVoiceVibesResults() ERROR " + e.toString() + " recording voice vibes error in avItemResponse. " + e.toString() + ", " + iir.toString()  );
            else
                LogService.logIt( e, "VoiceVibesUtils.checkForVoiceVibesResults() recording voice vibes error in avItemResponse. " + iir.toString() + ", " + e.toString() );
            
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.ERROR.getVoiceVibesStatusTypeId() );
            iir.appendNotes( "VoiceVibesException Posting Audio: " + e.getMessage() );
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();            
            avEventFacade.saveAvItemResponse(iir);
            
            // throw new Exception( e.getMessage() );
        }
        
        catch( Exception e )
        {
            if( e.toString().contains( "File is mute.") )
                LogService.logIt( "VoiceVibesUtils.checkForVoiceVibesResults() ERROR " + e.toString() + ", " + iir.toString() );            
            else
                LogService.logIt( e, "VoiceVibesUtils.checkForVoiceVibesResults() " + iir.toString() + ", " + e.toString() );            
            throw e;
        }
    }

    private boolean doesResponseJsonHaveResults( String responseJson ) throws Exception
    {
        try
        {
            JsonReader jr = Json.createReader(new StringReader( responseJson ));

            JsonObject jo = jr.readObject();
            
            if( !jo.containsKey( "data" ) )
                return false;
            
            JsonObject joData =  jo.getJsonObject("data"); // jr.readObject();
            
            if( !joData.containsKey( "results" ) )
                return false;
            
            
            JsonObject joResults = joData.getJsonObject( "results" );
            
            if( !joResults.containsKey("status") )
                return false;
            
            String status = joResults.getString("status" );
            
            if( status==null || status.isEmpty() )
                return false;
            
            if( status.equalsIgnoreCase("processing" ) )
                return false;
            
            if( status.equalsIgnoreCase("failed" ) )
            {
                String failedReason = JsonUtils.getStringFmJson(joData, "failedReason");                
                LogService.logIt( "VoiceVibesUtils.doesResponseJsonHaveResults() VoiceVibes Failed Message. failedReason=" + failedReason + ", responseJson=" + responseJson );
                return false;
                // throw new Exception( "VoiceVibes Failed Message. failedReason=" + failedReason );
            }
                        
            if( !joResults.containsKey( "completedDate" ) )
                return false;
            
            if( !joResults.containsKey( "scores" ) )
                return false;
            
            return true;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.doesResponseJsonHaveResults() responseJson=" + responseJson );
            
            return false;
        }
    }
    
    private String getJsonStatus( String responseJson ) throws Exception
    {
        try
        {
            JsonReader jr = Json.createReader(new StringReader( responseJson ));

            JsonObject jo = jr.readObject();
            
            if( !jo.containsKey( "data" ) )
                return null;
            
            JsonObject joData =  jo.getJsonObject("data"); // jr.readObject();
            
            if( !joData.containsKey( "results" ) )
                return null;
            
            JsonObject joResults = joData.getJsonObject( "results" );
            
            if( !joResults.containsKey("status") )
                return null;
            
            return joResults.getString("status" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.getJsonStatus() responseJson=" + responseJson );            
            return null;
        }
    }
    
    
    /**
     * Returns VoiceVibesId if successful.
     * 
     * @param o
     * @param te
     * @param iir
     * @return
     * @throws Exception 
     */
    public String postAudioToVoiceVibes( Org o, TestEvent te, AvItemResponse iir, List<AvItemResponse> irl, String base64Str) throws Exception
    {
        try
        {
            init();
            
            if( !voiceVibesOk.booleanValue() )
                throw new Exception( "VoiceVibes is not enabled. But VoiceVibesStatusTypeId=" + iir.getVoiceVibesStatusTypeId()  );
             
            float totalDur = iir.getDuration();

            // find the largest
            AvItemResponse irrMax = null;
            if( irl!=null )
            {
                for( AvItemResponse irr2 : irl )
                {
                    if( irr2.getAudioUri()!=null && !irr2.getAudioUri().isBlank() && irr2.getDuration()>0 && (irrMax==null || irrMax.getDuration()<irr2.getDuration() ))
                        irrMax = irr2;
                }
            }
            
            // No Need to create an appended file.
            if( iir.getAudioUri()!=null && !iir.getAudioUri().isBlank() && iir.getDuration()>=OPTIMUM_DURATION_VIBES_SAMPLE )
            {
                totalDur = iir.getDuration();  
                base64Str = HttpFileUtils.getBinaryFileAsBase64Str( ScoreUtils.getUrlFromAudioUri(iir.getAudioUri()) );       
                LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.1 TestEventId=" + (te==null ? "null" : te.getTestEventId())  +" avItemResponse.duration=" + iir.getDuration() + ", Base64Str.duration=" + totalDur + ", base64Str.length=" + base64Str.length());                            
            }

            // There is one clip that is good enough.
            else if( irrMax!=null && irrMax.getDuration()>=OPTIMUM_DURATION_VIBES_SAMPLE )
            {
               totalDur = irrMax.getDuration();  
               base64Str = HttpFileUtils.getBinaryFileAsBase64Str( ScoreUtils.getUrlFromAudioUri(irrMax.getAudioUri()) );       
               LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.2 TestEventId=" + (te==null ? "null" : te.getTestEventId()) +" Using irrMax avItemResponse.duration=" + irrMax.getDuration() + ", Base64Str.duration=" + totalDur + ", base64Str.length=" + base64Str.length());                                                
            }

            // Need to create an appended file.
            else if( irl!=null )
            {                
                List<AudioInputStream> clipList = new ArrayList<>();                
                
                // start with the first one.
                if( iir.getAudioUri()!=null && !iir.getAudioUri().isBlank() && iir.getDuration()>0 )
                {
                    AudioInputStream ais = AudioSystem.getAudioInputStream( com.tm2score.util.HttpUtils.getURLFromString( ScoreUtils.getUrlFromAudioUri(iir.getAudioUri()) ) );
                    totalDur = iir.getDuration();                        
                    clipList.add( ais );
                }
                
                AudioInputStream ais2;
                
                for( AvItemResponse irr2 : irl )
                {
                    if( totalDur>= OPTIMUM_DURATION_VIBES_SAMPLE )
                        break;

                    // skip current one.
                    if( irr2.getAvItemResponseId()==iir.getAvItemResponseId() )
                       continue;

                    // no audio url
                    if( irr2.getAudioUri()==null || irr2.getAudioUri().isBlank() || irr2.getDuration()<=1 )
                        continue;

                    ais2 = AudioSystem.getAudioInputStream( com.tm2score.util.HttpUtils.getURLFromString( ScoreUtils.getUrlFromAudioUri(irr2.getAudioUri())) );                    
                    clipList.add( ais2 );

                    totalDur += irr2.getDuration();                    

                    if( totalDur>=OPTIMUM_DURATION_VIBES_SAMPLE)
                        break;
                }

                if( !clipList.isEmpty() )
                {
                    byte[] bytes = AudioAppender.getAppendedAudio( clipList );
                    // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.3 TestEventId=" + (te==null ? "null" : te.getTestEventId()) + ", clips used=" + clipList.size() + ", Appending Audios bytes=" + (bytes==null ? "null" : bytes.length ) );  
                    if( bytes!=null && bytes.length>0 ) 
                    {
                        
                        // Something is wrong. Not enough bytes. Go back to irrMax
                        if( totalDur>=MINIMUM_DURATION_VIBES_SAMPLE && bytes.length<1000000 && irrMax!=null && irrMax.getDuration()>=MINIMUM_DURATION_VIBES_SAMPLE )
                        {
                            totalDur = irrMax.getDuration();  
                            base64Str = HttpFileUtils.getBinaryFileAsBase64Str( ScoreUtils.getUrlFromAudioUri(irrMax.getAudioUri()) );       
                            // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.4 Potential Issue with appended audio files. Using IrrMax. TestEventId=" + (te==null ? "null" : te.getTestEventId()) + " Using irrMax.  Base avItemResponse.duration=" + iir.getDuration() + ", irrMax.duration=" + irrMax.getDuration() + ", Base64Str.duration=" + totalDur + ", base64Str.length=" + base64Str.length());                                                
                        }

                        else
                        {
                            base64Str = new String( Base64Encoder.encode(bytes) );
                            // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.5 Appended Multiple Clips TestEventId=" + (te==null ? "null" : te.getTestEventId()) + ", irl.size=" + irl.size() + ", base avItemResponse.duration=" + iir.getDuration() + ", Base64Str.duration=" + totalDur + ", base64Str.length=" + base64Str.length());                            
                        }
                    }

                    else if( base64Str == null )
                    {
                        base64Str = "";
                        totalDur=1;
                        // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.6 base64Str is null. TestEventId=" + (te==null ? "null" : te.getTestEventId()) + ", irl.size=" + irl.size() + ", base avItemResponse.duration=" + iir.getDuration() );                            
                    }
                }

                else
                {
                    base64Str = "";
                    totalDur=1;                    
                    // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.7 no AvItemResponses in List to process. TestEventId=" + (te==null ? "null" : te.getTestEventId()) + ", irl.size=" + irl.size() + ", base avItemResponse.duration=" + iir.getDuration() );                            
                }                   

            }
            
            /*
            // Need to create an appended file.
            if( irl!=null && iir.getAudioUri()!=null && !iir.getAudioUri().isBlank() && iir.getDuration()< OPTIMUM_DURATION_VIBES_SAMPLE )
            {
                AudioInputStream ais = AudioSystem.getAudioInputStream( com.tm2score.util.HttpUtils.getURLFromString(iir.getAudioUri()) );

                totalDur = iir.getDuration();                
                
                List<AudioInputStream> clipList = new ArrayList<>();
                
                clipList.add( ais );
                
                AudioInputStream ais2;

                for( AvItemResponse irr2 : irl )
                {
                    // skip current one.
                    if( irr2.getAvItemResponseId()==iir.getAvItemResponseId() )
                        continue;

                    // no audio url
                    if( irr2.getAudioUri()==null || irr2.getAudioUri().isBlank() )
                        continue;
                    
                    ais2 = AudioSystem.getAudioInputStream( com.tm2score.util.HttpUtils.getURLFromString(irr2.getAudioUri()) );                    
                    clipList.add( ais2 );
                    
                    totalDur += irr2.getDuration();                    
                    
                    if( totalDur>=OPTIMUM_DURATION_VIBES_SAMPLE)
                        break;
                }
                
                if( clipList.size()>1 )
                {
                    byte[] bytes = AudioAppender.getAppendedAudio( clipList );
                    // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() Appending Audios bytes=" + (bytes==null ? "null" : bytes.length ) );  
                    if( bytes!=null && bytes.length>0 ) 
                        base64Str = new String( Base64Encoder.encode(bytes) );
                    else if( base64Str == null )
                    {
                        base64Str = "";
                        totalDur=1;
                    }
                }
                else
                    base64Str = HttpFileUtils.getBinaryFileAsBase64Str( iir.getAudioUri() );                    

                // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC avItemResponse.duration=" + iir.getDuration() + ", Base64Str.duration=" + totalDur + ", base64Str.length=" + base64Str.length());            
                
            }
            */
            
            // else just use the target irr
            else if( iir.getAudioUri()!=null && !iir.getAudioUri().isBlank() && (base64Str==null || base64Str.isBlank() ) && RuntimeConstants.getBooleanValue( "voiceVibes.forceBase64" ) && iir.getDuration()<= 2*OPTIMUM_DURATION_VIBES_SAMPLE )
            {
                String b64 = HttpFileUtils.getBinaryFileAsBase64Str( ScoreUtils.getUrlFromAudioUri(iir.getAudioUri()) );

                // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() CCC.6 TestEventId=" + (te==null ? "null" : te.getTestEventId()) + ", Converting to Base64 String because this is forced. b64=" + (b64==null ? "null" : b64.length()) );
                
                if( b64!=null && !b64.isBlank() )
                    base64Str = b64;
                
                totalDur = iir.getDuration();
            }
            
            if( iir.getVoiceVibesStatusType().isPostedOrHigher() )
            {
                if( iir.getVoiceVibesId()==null || iir.getVoiceVibesId().isEmpty() )
                    throw new Exception( "Status is posted or higher, but no VoiceVibesId is present in record." );
                
                return iir.getVoiceVibesId();
            }
            
            if( iir.getVoiceVibesStatusType().equals(VoiceVibesStatusType.ERROR) || iir.getVoiceVibesStatusType().isPermanentPostError() ) 
                throw new Exception( "VoiceVibes Status on AvItemResponse is Error. " + iir.toString() );
                 
            if( base64Str!=null && !base64Str.isBlank() && totalDur>0 && totalDur<MINIMUM_DURATION_VIBES_SAMPLE )
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_SENT_TOO_SHORT.getVoiceVibesStatusTypeId() );
                iir.appendNotes( "VoiceVibes Error Posting Audio. Audio is too short at: " + totalDur + ", minimum duration is " + MINIMUM_DURATION_VIBES_SAMPLE );
                if( avEventFacade==null )
                    avEventFacade = AvEventFacade.getInstance();            
                avEventFacade.saveAvItemResponse(iir);
                return null;
            }
            
            VoiceVibesAccountType vvat = this.getVoiceVibesAccountTypeToUse(o, te);
            
            iir.setVoiceVibesAccountTypeId( vvat.getVoiceVibesAccountTypeId() );
            
            checkVoiceVibesAuth( o, te, vvat );
            
            String[] creds = getVoiceVibesCredentials( o, te, vvat );
            
            String audioUrlOrBase64 = base64Str==null || base64Str.isEmpty() ? ScoreUtils.getUrlFromAudioUri(iir.getAudioUri()) : "data:audio/wav;base64," + base64Str;
            
            if( audioUrlOrBase64==null || audioUrlOrBase64.isBlank() )
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                iir.appendNotes( "VoiceVibes No AudioUrl or Base64 Str to send to Voice Vibes." );
                if( avEventFacade==null )
                    avEventFacade = AvEventFacade.getInstance();            
                avEventFacade.saveAvItemResponse(iir);
                return null;                
            }
            
            // To do - make POST. Store uuid.
            JsonObjectBuilder jobTop = Json.createObjectBuilder();

            jobTop.add("title", te.getTestEventId() + "_" + iir.getItemUniqueId() );

            jobTop.add( "audio", audioUrlOrBase64 );
            
            jobTop.add( "source", "avatar" );
            
            JsonObject jo = jobTop.build();
            
            String payload = JsonUtils.convertJsonObjectToString(jo);
            
            String url = getVoiceVibesUrl( "/recordings" );
            
            // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() Sending. Payload size=" + payload.length() + ", audioUrlOrBase64=" + (audioUrlOrBase64.length()>250 ? "length=" + audioUrlOrBase64.length() : audioUrlOrBase64 ) );
            
            String responseJson = sendApiPost(url, payload, creds);
            
            String voiceVibesId = getRecordingIdFromJsonResponse( responseJson );
            
            // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() extracted voiceVibesId=" + voiceVibesId + ", from JSON=" + responseJson );
            
            iir.setVoiceVibesId( voiceVibesId );
            iir.setVoiceVibesRequestDate( new Date() );
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POSTED.getVoiceVibesStatusTypeId() );
            
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();            
            avEventFacade.saveAvItemResponse(iir);
            
            return voiceVibesId;
        }
        catch( VoiceVibesException | IOException e )
        {
            LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() XXX.1 " + e.toString() + ", Recording voice vibes error in avItemResponse. " + iir.toString() );            
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR.getVoiceVibesStatusTypeId() );
            iir.setVoiceVibesPostErrorCount(iir.getVoiceVibesPostErrorCount()+1 );
            
            if( iir.getVoiceVibesPostErrorCount()>AvItemResponsePrepThread.MAX_VOICE_VIBES_POST_ERRORS )
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                            
            iir.appendNotes( "VoiceVibesException Posting Audio: " + e.toString() );
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();            
            avEventFacade.saveAvItemResponse(iir);
              
            return null;
            // throw new Exception( e.getMessage() );
        }
        //catch( IOException e )
        //{
        //    LogService.logIt( e, "VoiceVibesUtils.postAudioToVoiceVibes() IO ERROR " + e.toString() + ", " + iir.toString() );            
        //    throw e;
        //}
        catch( Exception e )
        {
            LogService.logIt(e, "VoiceVibesUtils.postAudioToVoiceVibes() YYY.1 " + iir.toString() );            
            throw e;
        }
    }
    
    
    /*
     REtired 4-18-2020
    *
    public String postAudioToVoiceVibesOLD( Org o, TestEvent te, AvItemResponse iir, List<AvItemResponse> irl, String base64Str) throws Exception
    {
        try
        {
            init();
            
            if( !voiceVibesOk.booleanValue() )
                throw new Exception( "VoiceVibes is not enabled. But VoiceVibesStatusTypeId=" + iir.getVoiceVibesStatusTypeId()  );
             
            float totalDur = iir.getDuration();
            
            if( irl!=null && iir.getDuration()< OPTIMUM_DURATION_VIBES_SAMPLE )
            {
                StringBuilder b64sb = new StringBuilder();
                
                String b64;
                
                // Add in the audio forthe current response.
                if( base64Str==null || base64Str.isBlank() )
                {                    
                    b64 = HttpFileUtils.getBinaryFileAsBase64Str( iir.getAudioUri() );

                    if( b64!=null && !b64.isBlank() )
                        b64sb.append(b64);
                }
                else
                    b64sb.append(base64Str);
                
                totalDur = iir.getDuration();                
                
                for( AvItemResponse irr2 : irl )
                {
                    // skip current one.
                    if( irr2.getAvItemResponseId()==iir.getAvItemResponseId() )
                        continue;

                    // no audio url
                    if( irr2.getAudioUri()==null || irr2.getAudioUri().isBlank() )
                        continue;
                    
                    b64 = HttpFileUtils.getBinaryFileAsBase64Str( irr2.getAudioUri() );

                    if( b64!=null && !b64.isBlank() )
                    {
                        b64sb.append(b64);
                        totalDur += irr2.getDuration();

                        if( totalDur>=OPTIMUM_DURATION_VIBES_SAMPLE)
                            break;
                    }
                }
                
                if( b64sb.length()>10 )
                    base64Str = b64sb.toString();

                LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() avItemResponse.duration=" + iir.getDuration() + ", Base64Str.duration=" + totalDur );            
                
            }
            
            else if( (base64Str==null || base64Str.isBlank() ) && RuntimeConstants.getBooleanValue( "voiceVibes.forceBase64" ) && iir.getDuration()<= 2*OPTIMUM_DURATION_VIBES_SAMPLE )
            {
                String b64 = HttpFileUtils.getBinaryFileAsBase64Str( iir.getAudioUri() );

                LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() Converting to Base64 String because this is forced. b64=" + (b64==null ? "null" : b64.length()) );
                
                if( b64!=null && !b64.isBlank() )
                    base64Str = b64;
                
                totalDur = iir.getDuration();
            }
            
            if( iir.getVoiceVibesStatusType().isPostedOrHigher() )
            {
                if( iir.getVoiceVibesId()==null || iir.getVoiceVibesId().isEmpty() )
                    throw new Exception( "Status is posted or higher, but no VoiceVibesId is present in record." );
                
                return iir.getVoiceVibesId();
            }
            
            if( iir.getVoiceVibesStatusType().equals(VoiceVibesStatusType.ERROR) || iir.getVoiceVibesStatusType().isPermanentPostError() ) 
                throw new Exception( "VoiceVibes Status on AvItemResponse is Error. " + iir.toString() );
                 
            if( base64Str!=null && !base64Str.isBlank() && totalDur>0 && totalDur<MINIMUM_DURATION_VIBES_SAMPLE )
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_SENT_TOO_SHORT.getVoiceVibesStatusTypeId() );
                iir.appendNotes( "VoiceVibes Error Posting Audio. Audio is too short at: " + totalDur + ", minimum duration is " + MINIMUM_DURATION_VIBES_SAMPLE );
                if( avEventFacade==null )
                    avEventFacade = AvEventFacade.getInstance();            
                avEventFacade.saveAvItemResponse(iir);
                return null;
            }
            
            VoiceVibesAccountType vvat = this.getVoiceVibesAccountTypeToUse(o, te);
            
            iir.setVoiceVibesAccountTypeId( vvat.getVoiceVibesAccountTypeId() );
            
            checkVoiceVibesAuth( o, te, vvat );
            
            String[] creds = getVoiceVibesCredentials( o, te, vvat );
            
            String audioUrlOrBase64 = base64Str==null || base64Str.isEmpty() ? iir.getAudioUri() : "data:audio/wav;base64," + base64Str;
            
            // To do - make POST. Store uuid.
            JsonObjectBuilder jobTop = Json.createObjectBuilder();

            jobTop.add("title", te.getTestEventId() + "_" + iir.getItemUniqueId() );

            jobTop.add( "audio", audioUrlOrBase64 );
            
            jobTop.add( "source", "avatar" );
            
            JsonObject jo = jobTop.build();
            
            String payload = JsonUtils.convertJsonObjectToString(jo);
            
            String url = getVoiceVibesUrl( "/recordings" );
            
            // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() Sending. Payload size=" + payload.length() + ", audioUrlOrBase64=" + (audioUrlOrBase64.length()>250 ? "length=" + audioUrlOrBase64.length() : audioUrlOrBase64 ) );
            
            String responseJson = sendApiPost(url, payload, creds);
            
            String voiceVibesId = getRecordingIdFromJsonResponse( responseJson );
            
            // LogService.logIt( "VoiceVibesUtils.postAudioToVoiceVibes() extracted voiceVibesId=" + voiceVibesId + ", from JSON=" + responseJson );
            
            iir.setVoiceVibesId( voiceVibesId );
            iir.setVoiceVibesRequestDate( new Date() );
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POSTED.getVoiceVibesStatusTypeId() );
            
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();            
            avEventFacade.saveAvItemResponse(iir);
            
            return voiceVibesId;
        }
        catch( VoiceVibesException e )
        {
            LogService.logIt(e, "VoiceVibesUtils.postAudioToVoiceVibes() recording voice vibes error in avItemResponse. " + iir.toString() );            
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR.getVoiceVibesStatusTypeId() );
            iir.setVoiceVibesPostErrorCount(iir.getVoiceVibesPostErrorCount()+1 );
            
            if( iir.getVoiceVibesPostErrorCount()>AvItemResponsePrepThread.MAX_VOICE_VIBES_POST_ERRORS )
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                            
            iir.appendNotes( "VoiceVibesException Posting Audio: " + e.getMessage() );
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();            
            avEventFacade.saveAvItemResponse(iir);
                        
            throw new Exception( e.getMessage() );
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "VoiceVibesUtils.postAudioToVoiceVibes() " + iir.toString() );
            
            throw e;
        }
    }
    */

    private String getVoiceVibesUrl( String method )
    {
        if( !method.startsWith("/") )
            method = "/" + method;
        
        return RuntimeConstants.getStringValue("voiceVibes.BaseUrl") + RuntimeConstants.getStringValue("voiceVibes.VersionPath") + method;
    }
    
    
    public void checkVoiceVibesAuth( Org o, TestEvent te,  VoiceVibesAccountType vvat ) throws Exception
    {
        if( 1==1 )
            return;
        
        if( lastAuth==null || ( new Date()).getTime() - lastAuth.getTime() > 10*60*1000 )
        {
            performVoiceVibesSignIn(  o,  te,  vvat );
            lastAuth = new Date();
        }
    }
    
    

    
    
    
    private void performVoiceVibesSignIn( Org o, TestEvent te,  VoiceVibesAccountType vvat ) throws Exception
    {
        String[] creds = getVoiceVibesCredentials( o, te, vvat );

        String url =  getVoiceVibesUrl( "/auth/sign-in"); //  getVoiceVibesUrl( "/recordings/"  + iir.getVoiceVibesId() );

        sendApiPut(url, creds);

        // LogService.logIt( "VoiceVibesUtils.performVoiceVibesSignIn() " );
    }
    
    
    
    
    /*
      resultJson={"data":{"recordingId":"04b25972-756b-4176-9dfa-d3c51f3d8684"},"serverDate":"2017-08-24T20:54:36.184Z"}
    */
    private String getRecordingIdFromJsonResponse( String resultJson ) throws Exception
    {
        try
        {        
            JsonReader jr = Json.createReader(new StringReader( resultJson ));

            JsonObject jo = jr.readObject();
            if( !jo.containsKey("data") )                
                throw new Exception( "resultJson does not have data element" );
            
            JsonObject joData = jo.getJsonObject( "data" );
            
            //if( !jo.containsKey("data") )                
            //    throw new Exception( "resultJson does not have data element" );
            
            // JsonObject joData = jo.getJsonObject( "data" );
            
            if( !joData.containsKey( "recordingId" ) )
                throw new Exception( "Cannot find recordingId in json." );
            
            return joData.getString( "recordingId" );

        }        
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.getRecordingIdFromJsonResponse() resultJson=" + resultJson );
            
            throw e;
        }            
    }

    public boolean getVoiceVibesCredentialsAreDemo( Org o, TestEvent te ) throws Exception
    {
        VoiceVibesAccountType vvat = this.getVoiceVibesAccountTypeToUse(o, te);

        if( vvat.equals( VoiceVibesAccountType.DEMO) )
            return true;
        
        checkVoiceVibesAuth( o, te, vvat );

        String[] creds = getVoiceVibesCredentials( o, te, vvat );        
        
        if( creds==null )
            return false;
        if( creds[0]==null || creds[0].isBlank() )
            return false;
        
        return creds[0].equals(RuntimeConstants.getStringValue( "voiceVibes.AccountIdDemo" ));
    }
    
    
    public String[] getVoiceVibesCredentials( Org o, TestEvent te, VoiceVibesAccountType vvat ) throws Exception
    {
        if( vvat!=null && !vvat.equals( VoiceVibesAccountType.NONE ) )
        {
            if( vvat.equals( VoiceVibesAccountType.NORMAL ) )
                return new String[] { RuntimeConstants.getStringValue( "voiceVibes.AccountId" ), RuntimeConstants.getStringValue( "voiceVibes.ApiKey" ) } ;
            
            if( vvat.equals( VoiceVibesAccountType.DEMO ) )
                return new String[] { RuntimeConstants.getStringValue( "voiceVibes.AccountIdDemo" ), RuntimeConstants.getStringValue( "voiceVibes.ApiKeyDemo" ) } ;
            
            if( vvat.equals( VoiceVibesAccountType.HIGHVOL ) )
                return new String[] { RuntimeConstants.getStringValue( "voiceVibes.AccountIdHV" ), RuntimeConstants.getStringValue( "voiceVibes.ApiKeyHV" ) } ;
            
            if( vvat.equals( VoiceVibesAccountType.ORG ) && o.getVoiceVibesCredentials()!= null && !o.getVoiceVibesCredentials().isEmpty() )
            {
                String[] vals = o.getVoiceVibesCredentials().split( ";" );

                if( vals.length<2 )
                    throw new Exception( "Couldn't parse VoiceVibesAccountCredentials: " + o.getVoiceVibesCredentials() );

                LogService.logIt( "VoiceVibesUtils.getVoiceVibesCredentials() o.getVoiceVibesCredentials()=" + o.getVoiceVibesCredentials() + ", username=" + vals[0] );

                return vals;                
            }
        }
        
        // always use custom credentials if present.
        if( o.getVoiceVibesCredentials()!= null && !o.getVoiceVibesCredentials().isEmpty() )
        {
            String[] vals = o.getVoiceVibesCredentials().split( ";" );

            if( vals.length<2 )
                throw new Exception( "Couldn't parse VoiceVibesAccountCredentials: " + o.getVoiceVibesCredentials() );

            LogService.logIt( "VoiceVibesUtils.getVoiceVibesCredentials() o.getVoiceVibesCredentials()=" + o.getVoiceVibesCredentials() + ", username=" + vals[0] );

            return vals;
        }
        
        String[] out = new String[2];

        boolean useDemo = RuntimeConstants.getBooleanValue( "voiceVibes.ForceDemo" );
        
        if( !useDemo && te.getCreditId()>0 )
        {
            if( purchaseFacade==null )
                purchaseFacade = PurchaseFacade.getInstance();
                        
            Credit c = purchaseFacade.getCredit( te.getCreditId() );
            
            if( c != null )
            {
                if( c.getAffiliateDemo()==1 )
                    useDemo = true;

                else if( c.getCreditSourceTypeId() == CreditSourceType.FREE_PROMO.getCreditSourceTypeId() )
                    useDemo = true;

                else if( c.getCreditSourceTypeId() == CreditSourceType.ADMIN_CREATE.getCreditSourceTypeId() && c.getDirectPurchaseAmount()<=0 )
                    useDemo = true;  
            }
        }

        if( useDemo )
        {
            out[0] = RuntimeConstants.getStringValue( "voiceVibes.AccountIdDemo" );
            out[1] = RuntimeConstants.getStringValue( "voiceVibes.ApiKeyDemo" );
        }
        else
        {
            out[0] = RuntimeConstants.getStringValue( "voiceVibes.AccountId" );
            out[1] = RuntimeConstants.getStringValue( "voiceVibes.ApiKey" );            
        }
        
        return out;        
    }
    
    
    public VoiceVibesAccountType getVoiceVibesAccountTypeToUse( Org o, TestEvent te ) throws Exception
    {
        // always use custom credentials if present.
        if( o.getVoiceVibesCredentials()!= null && !o.getVoiceVibesCredentials().isEmpty() )
        {
            String[] vals = o.getVoiceVibesCredentials().split( ";" );

            if( vals.length<2 )
                throw new Exception( "Couldn't parse VoiceVibesAccountCredentials: " + o.getVoiceVibesCredentials() );

            return VoiceVibesAccountType.ORG;
        }
        
        boolean useDemo = RuntimeConstants.getBooleanValue( "voiceVibes.ForceDemo" );
        
        if( !useDemo && te.getCreditId()>0 )
        {
            if( purchaseFacade==null )
                purchaseFacade = PurchaseFacade.getInstance();
                        
            Credit c = purchaseFacade.getCredit( te.getCreditId() );
            
            if( c != null )
            {
                if( c.getAffiliateDemo()==1 )
                    useDemo = true;

                else if( c.getCreditSourceTypeId() == CreditSourceType.FREE_PROMO.getCreditSourceTypeId() )
                    useDemo = true;

                else if( c.getCreditSourceTypeId() == CreditSourceType.ADMIN_CREATE.getCreditSourceTypeId() && c.getDirectPurchaseAmount()<=0 )
                    useDemo = true;  
            }
        }

        if( useDemo )
        {
            return VoiceVibesAccountType.DEMO;
        }

        return VoiceVibesAccountType.NORMAL;
    }
    
    
    /**
     * 
     * Throws a VoiceVibesException if there is an error from VoiceVibes. 
     * 
     * Otherwise, throws a regular exception or returns the response as text. 
     * 
     * Returns a 
     * @param url
     * @param creds
     * @return
     * @throws Exception 
     */
    public String sendApiGet( String url, String[] creds ) throws Exception
    {
        // CloseableHttpResponse r = null;

        // int statusCode = 0;
        String s=null;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(60))
        {
            init();

            HttpGet get;

            // LogService.logIt( "VoiceVibesUtils.sendApiGet() Preparing Request" );

            get = new HttpGet( url );

            get.addHeader( "Accept", "application/json" );
            
            Map<String,String> basicAuthCreds = getBasicAuthParmsForResultsPost( creds[0], creds[1] );

            if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
            {
                String un = basicAuthCreds.get( "username" );
                String pwd = basicAuthCreds.get( "password" );

                String b6 = Base64Encoder.encodeString( un + ":" + pwd );

                get.setHeader( "Authorization", "Basic " + b6  );

                // LogService.logIt( "VoiceVibesUtils.sendApiGet() Set basic Auth header: Basic " + b6);
            }                

            // LogService.logIt( "VoiceVibesUtils.sendApiGet() Executing Request" );
            //try( CloseableHttpResponse r = client.execute( get ) )
            //{
            s = client.execute(get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "VoiceVibesUtils.sendApiGet() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "VoiceVibesUtils.sendApiGet() statusCode="+ status + ", reason=" + response.getReasonPhrase() + ", response=" + ss );
                    return ss;
                    });

                // LogService.logIt( "VoiceVibesUtils.sendApiGet() url=" + url + ", Response Code : " + r.getCode() );

                //StringBuilder sb = processAPIResponse( r );

                // LogService.logIt( "VoiceVibesUtils.sendApiGet() url=" + url + ", response is: " + sb.toString() );

                //statusCode = r.getCode();

                //if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "VoiceVibesUtils.sendApiGet() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                //    throw new VoiceVibesException( "VoiceVibesUtils.sendApiGet()  Get failed with Http statuscode " + r.getReasonPhrase() + ", response text was: " + ( sb==null ? "null" : sb.toString()) );
                //}

                // LogService.logIt( "VoiceVibesUtils.sendApiGet() response as text: " + sb.toString() );

                return s; // sb.toString();
            //}
        }        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "VoiceVibesUtils.sendApiGet() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new VoiceVibesException( "VoiceVibesUtils.sendApiGet()  " + e.toString() + ", url=" + url + ", response=" + s );            
        }        
        catch( IOException e )
        {
            LogService.logIt( "VoiceVibesUtils.sendApiGet() STERR " + e.toString() + ", url=" + url + ", response=" + s );
            throw new VoiceVibesException( "VoiceVibesUtils.sendApiGet()  " + e.toString() + ", url=" + url + ", response=" + s );            
        }
        //catch( VoiceVibesException e )
        //{
        //    LogService.logIt( "VoiceVibesUtils.sendApiGet() STERR VoiceVibesException " + e.toString() + ", url=" + url );            
        //    throw e;            
        //}
        
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.sendApiGet() " );
            
            throw e;
        }

        //finally
        //{
            // if( r != null )
            //     r.close();
        //}
    }


    /**
     * Throws VoiceVibesException VoiceVibes responds that delete failed. 
     * 
     * Otherwise returns void or throws an Exception 
     * 
     * @param url
     * @param creds
     * @return
     * @throws Exception 
     */
    public void sendApiDelete( String url, String[] creds ) throws Exception
    {
        //CloseableHttpResponse r = null;

        //int statusCode = 0;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(60))
        {
            init();

            HttpDelete delete;

            // LogService.logIt( "VoiceVibesUtils.sendApiDelete() Preparing Request" );

            delete = new HttpDelete( url );
                        
            Map<String,String> basicAuthCreds = getBasicAuthParmsForResultsPost( creds[0], creds[1] );

            if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
            {
                String un = basicAuthCreds.get( "username" );
                String pwd = basicAuthCreds.get( "password" );

                String b6 = Base64Encoder.encodeString( un + ":" + pwd );

                delete.setHeader( "Authorization", "Basic " + b6  );

                // LogService.logIt( "VoiceVibesUtils.sendApiDelete() Set basic Auth header: Basic " + b6);
            }                

            // LogService.logIt( "VoiceVibesUtils.sendApiDelete() Executing Request" );
            // r = client.execute(delete );
            client.execute(delete, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "VoiceVibesUtils.sendApiDelete() statusCode="+ statusCode );
                    if( status<200 || status>=300 )
                        throw new IOException( "VoiceVibesUtils.sendApiDelete() statusCode="+ status + ", reason=" + response.getReasonPhrase() );
                    final HttpEntity entity2 = response.getEntity();
                    // String ss = EntityUtils.toString(entity2);
                    if( entity2!=null )
                        EntityUtils.consume(entity2);
                    return null;
                    });
            
            // LogService.logIt( "VoiceVibesUtils.sendApiDelete() url=" + url + ", Response Status Code : " + r.getCode() );
            
            //statusCode = r.getCode();

            //if( !isStatusCodeOk( statusCode ) )
            //{
            //    LogService.logIt( "VoiceVibesUtils.sendApiDelete() Method failed: " + r.getReasonPhrase() + ", url=" + url );
                
            //    throw new VoiceVibesException( "VoiceVibesUtils.sendApiDelete()  Delete failed with Http statuscode " + r.getReasonPhrase() );
            //}
        }        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "VoiceVibesUtils.sendApiDelete() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw e;
        }        
        catch( IOException e )
        {
            LogService.logIt( "VoiceVibesUtils.sendApiDelete() STERR " + e.toString() + ", url=" + url );            
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.sendApiDelete() url=" + url );            
            throw e;
        }
    }
    
    
    
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
                    LogService.logIt( "VoiceVibesUtils.processAPIResponse() response is null! Returning null" );
                
                else if( response.getEntity() == null )
                    LogService.logIt( "VoiceVibesUtils.processAPIResponse() response.getEntity is null! Returning null" );
                
                else if( response.getEntity().getContent() == null )
                {
                    LogService.logIt( "VoiceVibesUtils.processAPIResponse() response.getEntity().getContent() is null! Returning null" );
                    EntityUtils.consume(response.getEntity());
                }
                
                //if ( response != null ) 
                else {

                        String line = "";
                        sb = new StringBuilder();

                        InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent());
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        while ( (line = bufferedReader.readLine()) != null ) {
                                sb.append(line);
                        }
                        EntityUtils.consume(response.getEntity());
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
    
    
    public void sendApiPut( String url, String[] creds ) throws Exception
    {
        // CloseableHttpResponse r = null;

        int statusCode = 0;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(60))
        {
            init();

            HttpPut get;

            LogService.logIt( "VoiceVibesUtils.sendApiPut() Preparing Request" );

            get = new HttpPut( url );

            get.addHeader( "Accept", "application/json" );
            
            Map<String,String> basicAuthCreds = getBasicAuthParmsForResultsPost( creds[0], creds[1] );

            if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
            {
                String un = basicAuthCreds.get( "username" );
                String pwd = basicAuthCreds.get( "password" );

                String b6 = Base64Encoder.encodeString( un + ":" + pwd );

                get.setHeader( "Authorization", "Basic " + b6  );

                LogService.logIt( "VoiceVibesUtils.sendApiPut() Set basic Auth header: Basic " + b6);
            }                

            LogService.logIt( "VoiceVibesUtils.sendApiPut() Executing Request" );
            
            //try(  CloseableHttpResponse r = client.execute( get ) )
            //{
            client.execute(get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "VoiceVibesUtils.sendApiPut() statusCode="+ statusCode );
                    if( status<200 || status>=300 )
                        throw new IOException( "VoiceVibesUtils.sendApiPut() statusCode="+ status + ", reason=" + response.getReasonPhrase() );
                    final HttpEntity entity2 = response.getEntity();
                    // String ss = EntityUtils.toString(entity2);
                    if( entity2!=null )
                        EntityUtils.consume(entity2);
                    return null;
                    });

                // LogService.logIt( "VoiceVibesUtils.sendApiPut() url=" + url + ", Response Code : " + r.getCode() );

                //StringBuilder sb = processAPIResponse( r );

                //LogService.logIt( "VoiceVibesUtils.sendApiPut() url=" + url + ", response is: " + sb.toString() );

                // statusCode = r.getCode();

                //if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "VoiceVibesUtils.sendApiPut() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                //    throw new VoiceVibesException( "VoiceVibesUtils.sendApiPut()  Get failed with Http statuscode " + r.getReasonPhrase() );
                //}

                //LogService.logIt( "VoiceVibesUtils.sendApiGet() response as text: " + sb.toString() );
                //if( r.getEntity()!=null )
                //    EntityUtils.consume(r.getEntity());
                //return; //  sb.toString();
            //}
        }        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "VoiceVibesUtils.sendApiPut() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw e;
        }        
        catch( IOException e )
        {
            LogService.logIt( e, "VoiceVibesUtils.sendApiPut() STERR " + e.toString() + ", url=" + url );            
            throw e;            
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.sendApiPut() url=" + url );            
            throw e;
        }
        //finally
        //{
            //if( r != null )
            //    r.close();
        //}
    }
    
    
    
    /**
     * Throws a VoiceVibesException if there is an error from VoiceVibes. 
     * 
     * Otherwise, throws a regular exception or returns the response as text. 
     * 
     * @param url
     * @param payload
     * @param creds
     * @return
     * @throws Exception 
     */
    public String sendApiPost( String url, String payload, String[] creds ) throws Exception
    {
        //CloseableHttpResponse r = null;

        try
        {
            init();

            return sendApiPostCore(  url,  payload, creds );
        }
        
        catch( VoiceVibesException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.sendApiPost() url=" + url );            
            throw new VoiceVibesException( "VoiceVibesUtils.sendApiPost()  " + e.toString() + ", url=" + url );
            // throw e;
        }
        //finally
        //{
        //    if( r != null )
        //        r.close();
        //}
        
    }
    


    /**
     * Throws a VoiceVibesException if there is an error from VoiceVibes. 
     * 
     * Otherwise, throws a regular exception or returns a CloseableHttpResponse. 
     * 
     * @param url
     * @param payload
     * @param creds
     * @return
     * @throws Exception 
     */
    public String sendApiPostCore( String url, String payload, String[] creds ) throws Exception
    {
        String r = null;

        // int statusCode = 0;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(60))
        {
            init();

            HttpPost post;

            // LogService.logIt( "VoiceVibesUtils.sendApiPostCore() Preparing Request payload=" + payload.length() );

            post = new HttpPost( url );
            
            post.setEntity( new StringEntity( payload ) );

            //int length = payload == null ? 0 : payload.length();
            
            //post.setHeader( "Content-Length", length + "" );
            post.addHeader( "Content-Type", "application/json" );
            post.addHeader( "Accept", "application/json" );

            Map<String,String> basicAuthCreds = getBasicAuthParmsForResultsPost( creds[0], creds[1] );

            if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
            {
                String un = basicAuthCreds.get( "username" );
                String pwd = basicAuthCreds.get( "password" );

                String b6 = Base64Encoder.encodeString( un + ":" + pwd );

                post.setHeader( "Authorization", "Basic " + b6  );

                // LogService.logIt( "VoiceVibesUtils.sendApiPostCore() Set basic Auth header: Basic " + b6);
            }                

            // LogService.logIt( "VoiceVibesUtils.sendApiPostCore() Executing Request" );
            r = client.execute( post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "VoiceVibesUtils.sendApiPostCore() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "VoiceVibesUtils.sendApiPostCore() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    } );
            
            // LogService.logIt( "VoiceVibesUtils.sendApiPostCore() url=" + url + ", Response Status Code : " + r.getCode() );
            
            // statusCode = r.getCode();

            //if( !isStatusCodeOk( statusCode ) )
            //{
            //    LogService.logIt( "VoiceVibesUtils.sendApiPostCore() Method failed: " + r.getReasonPhrase() + ", url=" + url + ", payload: " + payload );
                
            //    StringBuilder sb = null;
                
            //    try
             //   {
             ///       sb = processAPIResponse( r );
             //   }
            //    catch( Exception ee )
            //    {
            //        LogService.logIt( ee, "VoiceVibesUtils.sendApiPostCore() ProcessingAPIResponse. " );
            //    }
                
            //    throw new VoiceVibesException( "VoiceVibesUtils.sendApiPostCore()  Get failed with Http statuscode " + r.getReasonPhrase() + " responseContent=" + (sb==null ? "null" : sb.toString()) );
            //}
                        
            return r;
        }
        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "VoiceVibesUtils.sendApiPostCore() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new VoiceVibesException( "VoiceVibesUtils.sendApiPostCore()  " + e.toString() + ", response=" + r + ", url=" + url );
        }        
        catch( IOException e )
        {
            LogService.logIt( "VoiceVibesUtils.sendApiPostCore() STERR " + e.toString() + ", response=" + r + ", url=" + url);
            throw new VoiceVibesException( "VoiceVibesUtils.sendApiPostCore()  " + e.toString() + ", response=" + r + ", url=" + url );
            
        }
        //catch( VoiceVibesException e )
        //{
        //    throw e;
        //}
        catch( Exception e )
        {
            LogService.logIt( e, "VoiceVibesUtils.sendApiPostCore() response=" + r + ", url=" + url );            
            throw new VoiceVibesException( "VoiceVibesUtils.sendApiPostCore() " + e.toString() + ", response=" + r + ", url=" + url );
            // throw e;
        }
        
    }
    
    //private boolean isStatusCodeOk( int statusCode )
    //{
    //    return statusCode>=200 && statusCode<300;
    //}

    
    public Map<String,String>  getBasicAuthParmsForResultsPost( String username, String pwd )
    {
        Map<String,String> out = new HashMap<>();

        out.put( "username", username );
        out.put( "password", pwd );

        return out;
    }
    
    
}
