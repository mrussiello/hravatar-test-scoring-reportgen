/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.ai;

import com.tm2score.entity.user.Resume;
import com.tm2score.entity.user.User;
import com.tm2score.service.LogService;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

/**
 *
 * @author miker
 */
public class AiRequestUtils
{
    public static JsonObject getTestAiCallSyncResponse(User user) throws Exception
    {
        try
        {
            if( user==null )
                throw new Exception( "user is null" );

            AiCallType aiCallType = AiCallType.TEST_SYNC;

            JsonObjectBuilder job = getBasePayloadJsonObjectBuilder(aiCallType,0, user );

            JsonObject joReq = job.build();

            AiRequestClient client = new AiRequestClient();

            return client.getJsonObjectFromAiCallRequest( joReq );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AiRequestUtils.getTestAiCallSyncResponse() userId=" + (user==null ? "null" : user.getUserId()) );
            throw e;
        }
    }

    public static JsonObject getTestAiCallAsyncResponse(User user) throws Exception
    {
        try
        {
            if( user==null )
                throw new Exception( "user is null" );

            AiCallType aiCallType = AiCallType.TEST_ASYNC;

            JsonObjectBuilder job = getBasePayloadJsonObjectBuilder(aiCallType,0, user );

            JsonObject joReq = job.build();

            AiRequestClient client = new AiRequestClient();

            return client.getJsonObjectFromAiCallRequest( joReq );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AiRequestUtils.getTestAiCallAsyncResponse() userId=" + (user==null ? "null" : user.getUserId()) );
            throw e;
        }
    }

    public static JsonObject parseResume( Resume resume, User user) throws Exception
    {
        return doResumeParsingCall( resume, user, AiCallType.RESUME_PARSE );
    }
    
    /*
    public static JsonObject getResumeExperience( Resume resume, User user) throws Exception
    {
        return doResumeParsingCall( resume, user, AiCallType.RESUME_EXPERIENCE );
    }
    
    public static JsonObject getResumeEducation( Resume resume, User user) throws Exception
    {
        return doResumeParsingCall( resume, user, AiCallType.RESUME_EDUCATION );
    }

    public static JsonObject getResumeSummary( Resume resume, User user) throws Exception
    {
        return doResumeParsingCall( resume, user, AiCallType.RESUME_SUMMARY );
    }
    */
    
    
    public static JsonObject doResumeParsingCall( Resume resume, User user, AiCallType aiCallType ) throws Exception
    {
        try
        {
            if( resume==null )
                throw new Exception( "Resume is null" );

            if( (resume.getUploadedText()==null || resume.getUploadedText().isBlank()) && (resume.getPlainText()==null || resume.getPlainText().isBlank()) )
                throw new Exception( "Resume does not have enough info for a Summary Call" );

            JsonObjectBuilder job = getBasePayloadJsonObjectBuilder(aiCallType,0, user );

            String textToUse = resume.getUploadedText();
            if( textToUse==null || textToUse.isBlank() )
                textToUse=resume.getPlainText();
            
            if( textToUse==null || textToUse.isBlank() )
                throw new Exception( "No TextToUse to send to Resume Parser." );
            
            job.add("resumeid", resume.getResumeId() );
            job.add("strparam1", textToUse );

            JsonObject joReq = job.build();

            AiRequestClient client = new AiRequestClient();

            return client.getJsonObjectFromAiCallRequest( joReq );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AiRequestUtils.doResumeParsingCall() aiCallType=" +  aiCallType.getName() +", ResumeId=" + (resume==null ? "null" : resume.getResumeId() + ", userId=" + resume.getUserId() + ", orgId=" + resume.getOrgId()) + ", userId=" + (user==null ? "null" : user.getUserId()) );
            throw e;
        }
    }



    private static JsonObjectBuilder getBasePayloadJsonObjectBuilder( AiCallType aiCallType, int jobDescripId, User user ) throws Exception
    {
        AiCallSourceType aiCallSourceType = AiCallSourceType.BUILDER;

        JsonObjectBuilder job = Json.createObjectBuilder();

        job.add( "tran", aiCallType.getTran() );
        job.add( "sourcetypeid", aiCallSourceType.getAiCallSourceTypeId() );
        job.add( "useridenc", user.getUserIdEncrypted() );
        if( jobDescripId>0 )
            job.add( "jobdescripid", jobDescripId );

        return job;

    }
}
