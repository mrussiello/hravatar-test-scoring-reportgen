/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ws.rest;

import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.TestKeyScoreThread;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.JsonUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response; 

/**
 *
 * {
 *    tkid:  encrypted test key id
 *    tran:  transaction name
 * }
 * 
 * The purpose of this resource is to start scoring of a completed test key immediately.
 */
@Path("scorews")
@RequestScoped
public class ScoreResource extends BaseApiResource {
    
    
    EventFacade eventFacade;
    
    
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response doPost( @Context HttpServletRequest request , @Context HttpHeaders headers, String jsonContent ) 
    {        
        long testKeyId = 0;
        String tkid = null;
        
        try
        {
            // LogService.logIt( "ScoreResource.ScoreResource() START. jsonContent=" + jsonContent );
            // Authenticate
            try
            {
                 authenticateRequest( headers );
            }
            catch( ApiException e )
            {
                LogService.logIt( e, "ScoreResource.ScoreResource() AA.1 Authentication Error. jsonContent=" + jsonContent );
                return Response.status( e.getHttpResponseCode(), "Unable to authenticate." ).build();
            }
                        
            if( jsonContent==null || jsonContent.isBlank() )
                throw new ApiException( "Payload is missing.", 150, Response.Status.BAD_REQUEST.getStatusCode() );
                        
            
            // Parse the Json cntent
            JsonObject jo = JsonUtils.getJsonObject(jsonContent);
            
            tran = jo.containsKey( "tran" ) ? jo.getString( "tran" ) : null;
            tkid = jo.containsKey( "tkid" ) ? jo.getString( "tkid" ) : null;
            
            if( tran == null || tran.isBlank() )
                throw new ApiException( "tran is missing.", 155, Response.Status.BAD_REQUEST.getStatusCode() );
                        
            if( tkid == null || tkid.isBlank() )
                throw new ApiException( "tkid is missing.", 180, Response.Status.BAD_REQUEST.getStatusCode() );
                     
            try
            {
                testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt(tkid) );
            }
            catch( NumberFormatException ee )
            {
                LogService.logIt(  "ScoreResource.ScoreResource() NumberFormatException parsing " + tkid + ", jsonContent=" + jsonContent );
                throw new ApiException( "Error parsing tkid", 181, Response.Status.BAD_REQUEST.getStatusCode() );
            }
            catch( Exception ee )
            {
                LogService.logIt(  ee, "ScoreResource.ScoreResource() Exception parsing " + tkid + ", jsonContent=" + jsonContent );
                throw new ApiException( "Error parsing tkid", 182, Response.Status.BAD_REQUEST.getStatusCode() );                
            }

            if( testKeyId<=0 )
                throw new ApiException( "testKeyId is invalid=" + testKeyId, 185, Response.Status.BAD_REQUEST.getStatusCode() );
                                 
            JsonObjectBuilder outJob = null;
            
            if( tran.equals( "tkcomplete" ) )
                outJob = doTestKeyComplete( testKeyId );
            
            else
                throw new ApiException( "tran is invalid=" + tran, 160, Response.Status.BAD_REQUEST.getStatusCode() );
                
            
            JsonObject jo2 = outJob.build();            
            String out = JsonUtils.convertJsonObjectToString(jo2);            
            // LogService.logIt( "ScoreResource.ScoreResource() COMPLETE. output=" + out );
            return Response.ok( out, MediaType.APPLICATION_JSON).status( Response.Status.OK.getStatusCode() ).build();            
        }        
        catch( ApiException e )
        {
            LogService.logIt( "ScoreResource.ScoreResource() " + e.toString() + ", testKeyId=" + testKeyId + ", tran=" + tran + ", orgId=" + getOrgId() );
            String subj = "ScoreResource Exception testKeyId=" + testKeyId;     
            sendErrorEmail( subj, "testKeyId=" + testKeyId + ", API Exception=" + e.toString() );            
            return Response.status( e.getHttpResponseCode(), "Server Error: ScoreResource.ScoreResource() testKeyId=" + testKeyId + ", tran=" + tran + ", orgId=" + getOrgId() + ", " + e.toString() ).build();            
        }                
        catch( Exception e )
        {
            LogService.logIt( e, "ScoreResource.doPost() jsonContent=" + jsonContent );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "ScoreResource.ScoreResource() Unknown Exception testKeyId=" + testKeyId + ", tran=" + tran + ", authUserId=" + (authUser==null ? "null" : authUser.getUserId()) + ", " + e.toString() + ", jsonContent=" + jsonContent ).build();            
            // return getGeneralErrorJson( e, "ScoreResource.doPost() tran=" + tran + ", tkid=" + tkid + ", jsonContent=" + jsonContent );
        }
    }
    
    private JsonObjectBuilder doTestKeyComplete( long testKeyId ) throws Exception
    {
        JsonObjectBuilder outJob = Json.createObjectBuilder();             
        
        if( eventFacade==null )
            eventFacade = EventFacade.getInstance();
        
        TestKey tk = eventFacade.getTestKey(testKeyId, true);
        
        if( tk==null )
            throw new Exception( "TestKey not found for testKeyId=" + testKeyId );
        
        if( !tk.getTestKeyStatusType().getIsCompleteOrHigher() )
            throw new Exception( "TestKey is not yet complete. status=" + tk.getTestKeyStatusType().getName() + " testKeyId=" + testKeyId );
            
        //if( tk.getOnlineProctoringType().getIsAnyBasic() || tk.getOnlineProctoringType().getIsPremiumAnyImages() )
        //    throw new Exception( "TestKey requires proctoring post-processing. OnlineProctoringType=" + tk.getOnlineProctoringType().getKey() + ", testKeyId=" + testKeyId );
        
        boolean started = false;
        String message = "";
        if( tk.getTestKeyStatusType().equals( TestKeyStatusType.COMPLETED) )
        {
            // If batches are turned off. Do not process.
            if( !RuntimeConstants.getBooleanValue( "autoScoreOk" ) || !ScoreManager.OK_TO_START_ANY )
            {
                message = "Scoring not started for TestKey because auto scoring is turned off either by default or in the ScoreManager. RuntimeConstants.autoScoreOk=" + RuntimeConstants.getBooleanValue( "autoScoreOk" ) + ", ScoreManager.OK_TO_START_ANY=" + ScoreManager.OK_TO_START_ANY;
                LogService.logIt( "ScoreResource.doTestKeyComplete() " + message + ", testKeyId=" + tk.getTestKeyId() );                                  
            }
            // determine if we need to shift to noThread
            else if( ScoreManager.getTestKeysAndPartialEventsInScoringCount()>=ScoreManager.MAX_THREAD_TESTKEY_SCORE_COUNT )
            {
                message = "Auto Scoring not started because there are too many scoring processes active. status=" + tk.getTestKeyStatusType().getName();
                LogService.logIt( "ScoreResource.doTestKeyComplete() " + message + ", testKeyId=" + tk.getTestKeyId() );                  
            }            
            else
            {
                new Thread(new TestKeyScoreThread( tk, false, 0 )).start();                       
                started = true;
            }
        }
        else
            message = "Scoring not started because test key status is not Complete: " + tk.getTestKeyStatusType().getName();
        
        outJob.add( "started", started );        
        outJob.add( "message", message );        
        return outJob;        
    }
    
    protected String getGeneralErrorJson( Exception e, String userMessage )
    {
        try
        {
            JsonObjectBuilder job = Json.createObjectBuilder();                        
            job.add( "status", "error" );                        
            job.add("cause", e==null || e.getCause()==null ? "null" : e.getCause().toString()  );                        
            job.add("error", e==null || e.getMessage()==null ? "" : e.getMessage() );                        
            job.add("exception", userMessage==null ? "" : userMessage );                        
            JsonObject jo = job.build();                        
            String out = JsonUtils.convertJsonObjectToString(jo); //.convertJsonObjectToString(jo);
            LogService.logIt( "ScoreResource.getGeneralErrorJson() out=" + out );
            return out;                        
        }
        catch( Exception ee )
        {
            LogService.logIt( ee, "ScoreResource.getGeneralErrorJson() " + e.toString() + ", " + e.getMessage() );
            return "{\"error\": \"" + ee.getMessage() + "\",\"exception:\":\"" + e.toString() + "\"}";
        }
    }
    
    
    
}
