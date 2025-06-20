/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.user;

import com.tm2score.ai.AiCallType;
import com.tm2score.ai.AiRequestUtils;
import com.tm2score.entity.user.Resume;
import com.tm2score.entity.user.User;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import jakarta.json.JsonObject;

/**
 *
 * @author miker
 */
public class ResumeHelpUtils {
    
    public static boolean parseResumeByAiNoErrors( User user, Resume resume)
    {
        try
        {
            return parseResumeByAi( user, resume);
        }
        catch (STException e)
        {
            return false;
        }
        catch (Exception e)
        {
            LogService.logIt(e, "ResumeUtilsHelp.parseResumeByAiNoErrors() Resume=" + (resume==null ? "null" : resume.toString()) );
            return false;
        }
                
    }
    
    public static boolean parseResumeByAi( User user, Resume resume) throws STException
    {
        try
        {
            if( !AiRequestUtils.getIsAiSystemAvailable() )
                return false;
            
            if( resume==null )
                throw new Exception( "Resume is null.");

            if( resume.getUploadedText()==null || resume.getUploadedText().isBlank() )
                throw new Exception( "Resume.uploadedText is null or empty." );
            
            if( !resume.getHasAnyDataForAiParseCall())
                throw new STException( "g.ResumeNoDataForAi", new String[]{} );

            JsonObject responseJo = AiRequestUtils.parseResume(resume, user, true);
            AiRequestUtils.checkAiCallResponse( responseJo );            
            return true;
        }
        catch (STException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            LogService.logIt(e, "ResumeUtilsHelp.ResumeHelpUtils() Resume=" + (resume==null ? "null" : resume.toString()) );
            throw new STException(e);            
        }
    }
    
    
    
}
