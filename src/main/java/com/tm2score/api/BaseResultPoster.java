/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.api;

import com.tm2score.entity.event.TestKey;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author miker
 */
public class BaseResultPoster 
{
    protected Map<String,String> getBasicAuthCredsFromReportFlags( TestKey testKey ) throws Exception
    {
        if( testKey==null )
            return null;
        
        Map<String,String> out = testKey.getBasicAuthParmsForResultsPost();
            
        if( out!=null && out.containsKey("username") && out.containsKey("password"))
            return out;
        
        try
        {
            if( testKey.getOrg()==null )
                testKey.setOrg( UserFacade.getInstance().getOrg( testKey.getOrgId()));
            
            if( testKey.getOrg()==null )
                throw new Exception( "TestKey.Org is null." );
            
            int authTypeId = ReportUtils.getReportFlagIntValue("resultpostauthtypeid", null, null, null, testKey.getOrg(), null );
            if( authTypeId!=1 )
                return null;
            
            String username = ReportUtils.getReportFlagStringValue("resultpostauthparam1", null, null, null, testKey.getOrg(), null);
            String password = ReportUtils.getReportFlagStringValue("resultpostauthparam2", null, null, null, testKey.getOrg(), null);
            
            if( username==null || username.isBlank() || password==null || password.isBlank() )
            {
                LogService.logIt( "BaseResultPoster.getBasicAuthCredsFromReportFlags() username and/or password invalid. Returning null. username=" + (username==null ? null : username) + ", password.len=" + (password==null ? "null" : password.length()) + ", testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) );
                return null;
            }

            username=username.trim();
            password = password.trim();
            out = new HashMap<>();
            out.put( "username", username);
            out.put( "password", password);
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseResultPoster.getBasicAuthCredsFromReportFlags() testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) );
            return null;
        }
    }            
}
