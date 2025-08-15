/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.api;

import com.tm2score.entity.event.TestKey;
import com.tm2score.global.RuntimeConstants;
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
    static String[] FULL_POST_AFFILIATEIDS;
    static String[] FULL_POST_DOMAINS;
    
    
    protected static synchronized void init()
    {
        if( FULL_POST_AFFILIATEIDS!=null )
            return;
        
        String temp = RuntimeConstants.getStringValue("resultpostdomains-for-full-resultpost" );
        FULL_POST_DOMAINS = temp==null || temp.isBlank() ? new String[0] : temp.toLowerCase().split(",");
        
        temp = RuntimeConstants.getStringValue("org-affiliateids-for-full-resultpost" );
        FULL_POST_AFFILIATEIDS = temp==null || temp.isBlank() ? new String[0] : temp.split(",");
    }
    
    UserFacade userFacade;
    
    protected int getDefaultIncludeScoreCode( TestKey tk  )
    {
        if( tk.getOmitPdfReportFromResultsPost() )
            return 1;
        
        if( FULL_POST_AFFILIATEIDS==null )
            BaseResultPoster.init();
        
        String includeScoreCodeStr = tk.getCustomParameterValue( "includeScoreCode" );
        if( includeScoreCodeStr!=null && !includeScoreCodeStr.isBlank() )
        {
            int tempCode = Integer.parseInt( includeScoreCodeStr.trim() );

            // include scores but no PDFs
            if( tempCode == 1 )
                return 1;

            // Inlude no score info
            else if( tempCode == 4 )
                return 0;                    
        }
        
        if( tk.getOrg()==null )
        {
            try
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                tk.setOrg( userFacade.getOrg(tk.getOrgId() ) );                
            }
            catch( Exception e )
            {
                LogService.logIt( e, "BaseResultPoster.getDefaultIncludeScoreCode() Loading org. testKeyId=" + tk.getTestKeyId() );
            }
        }
        
        if( tk.getResultPostUrl()!=null && !tk.getResultPostUrl().isBlank() )
        {
            String rpu = tk.getResultPostUrl().toLowerCase();
            
            for( String s : FULL_POST_DOMAINS)
            {
                if( s.isBlank() )
                    continue;

                // include score and PDFs
                if( rpu.contains(s))
                    return 2;
            }
        }
        
        if( tk.getOrg()!=null && tk.getOrg().getAffiliateId()!=null && !tk.getOrg().getAffiliateId().isBlank() )
        {
            for( String s : FULL_POST_AFFILIATEIDS)
            {
                if( s.isBlank() )
                    continue;

                // include score and PDFs
                if( tk.getOrg().getAffiliateId().equals(s))
                    return 2;
            }
        }
               
        // default scores but no PDFs
        return 1;
    }
    
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
