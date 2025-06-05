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

    public static JsonObject parseResume( Resume resume, User user, boolean autoUpdate) throws Exception
    {
        return doResumeParsingCall(resume, user, AiCallType.RESUME_PARSE, autoUpdate );
    }
        
    public static JsonObject doResumeParsingCall( Resume resume, User user, AiCallType aiCallType, boolean autoUpdate) throws Exception
    {
        try
        {
            if( resume==null )
                throw new Exception( "Resume is null" );

            if( (resume.getUploadedText()==null || resume.getUploadedText().isBlank()) ) //  && (resume.getPlainText()==null || resume.getPlainText().isBlank()) )
                throw new Exception( "Resume does not have enough info for a Parsing Call" );

            JsonObjectBuilder job = getBasePayloadJsonObjectBuilder(aiCallType, user );

            String textToUse = resume.getUploadedText();
            //if( textToUse==null || textToUse.isBlank() )
            //    textToUse=resume.getPlainText();
            
            if( textToUse==null || textToUse.isBlank() )
                throw new Exception( "No TextToUse to send to Resume Parser." );
            
            job.add("resumeid", resume.getResumeId() );
            job.add("autoupdate", autoUpdate ? 1 : 0 );
            // job.add("strparam1", textToUse );

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



    private static JsonObjectBuilder getBasePayloadJsonObjectBuilder( AiCallType aiCallType, User user ) throws Exception
    {
        AiCallSourceType aiCallSourceType = AiCallSourceType.BUILDER;

        JsonObjectBuilder job = Json.createObjectBuilder();

        job.add( "tran", aiCallType.getTran() );
        job.add( "sourcetypeid", aiCallSourceType.getAiCallSourceTypeId() );
        job.add( "useridenc", user.getUserIdEncrypted() );

        return job;

    }
}
