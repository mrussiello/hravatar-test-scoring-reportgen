/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.crm;

import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.User;
import com.tm2score.service.LogService;
import java.util.Date;

/**
 *
 * @author miker_000
 */
public class HubspotUtils extends BaseHubspotUtils {
    
   
    public boolean getIsHubspotOn()
    {
        init();
        return HUBSPOT_API_ON;
    }
    
    public String createOrUpdateHubspotCompany( Org org )
    {
        if( org==null )
            return null;
        
        init();

        if( !HUBSPOT_API_ON )
            return null;
        
        String hubspotId = getHubspotCompanyId(INCLUDE_HRA_IDS && org.getOrgId()>0 ? org.getOrgId() : 0, org.getCompanyUrl(), org.getName() );        

        if( INCLUDE_HRA_IDS && hubspotId!=null && !hubspotId.isBlank() && org.getCompanyUrl()!=null && !org.getCompanyUrl().isBlank() && org.getOrgId()>0 )
        {
            String hubspotId2 =getHubspotCompanyId(0, org.getCompanyUrl(), org.getName() );  
            if( hubspotId2!=null && !hubspotId2.isBlank() && !hubspotId.equals(hubspotId2) )
            {
                LogService.logIt( "HubspotUtils.createOrUpdateHubspotCompany() ERROR There is a mismatch between org.companyUrl and orgId. Hubspot Company Id based on orgId=" + hubspotId + " but Hubspot company id based on org.companyUrl=" + hubspotId2 + ".  So, we cannot save this orgId/companyUrl combination. orgId=" + org.getOrgId() + ", org.companyUrl=" + org.getCompanyUrl() + ", org.name=" + org.getName() );
                return null;
            }
        }  
        
        String orgHubspotId = getCreateUpdateCompany( org, hubspotId );
        LogService.logIt( "HubspotUtils.createOrUpdateHubspotCompany() Success=" + (orgHubspotId!=null && !orgHubspotId.isBlank()) + ", orgHubspotId=" + orgHubspotId + ", orgId=" + org.getOrgId() + ", domain=" + org.getCompanyUrl() + ", name=" + org.getName() + ", original hubspot.id=" + hubspotId );
        
        return orgHubspotId;
    }

    
    public String createOrUpdateHubspotContact( User user, Org org, String forceUserHubspotId, String hubspotCompanyId)
    {
        init();

        if( !HUBSPOT_API_ON )
            return null;
                
        if( user==null )
            return null;
                                
        String hubspotId = forceUserHubspotId !=null && !forceUserHubspotId.isBlank() ? forceUserHubspotId : getHubspotContactId(INCLUDE_HRA_IDS && user.getUserId()>0 ? user.getUserId() : 0, user.getEmail());  
        
        if( INCLUDE_HRA_IDS && hubspotId!=null && !hubspotId.isBlank() && user.getEmail()!=null && !user.getEmail().isBlank() && user.getUserId()>0 )
        {
            String hubspotId2 = getHubspotContactId(0, user.getEmail());
            if( hubspotId2!=null && !hubspotId2.isBlank() && !hubspotId.equals(hubspotId2) )
            {
                LogService.logIt("HubspotUtils.createOrUpdateHubspotContact() ERROR There is a mismatch between user email and userId. Hubspot Contact Id based on email=" + hubspotId + " but Hubspot contact id based on userid=" + hubspotId2 + ".  So, we cannot save this user email/userId combination. userId=" + user.getUserId() + ", email=" + user.getEmail() + ", hubspotId=" + hubspotId + ", " + (org==null ? "org is null." : "orgId=" + org.getOrgId() + ", org.domain=" + org.getCompanyUrl() + ", org.name=" + org.getName()) );
                return null;
            }
        }         
        
        
        String userHubspotId = getCreateUpdateContact(user, org, hubspotId, hubspotCompanyId );
        LogService.logIt("HubspotUtils.createOrUpdateHubspotContact() Success=" + (userHubspotId!=null && !userHubspotId.isBlank()) + ", userHubspotId=" + userHubspotId + ", userId=" + user.getUserId() + ", email=" + user.getEmail() + ", initial hubspotId=" + hubspotId + ", " + (org==null ? "org is null." : "orgId=" + org.getOrgId() + ", org.domain=" + org.getCompanyUrl() + ", org.name=" + org.getName()) );
                
        return userHubspotId;
    }
    

    public String createHubspotDeal( User user, Org org, String dealName, String description, float amount, String stage, Date closeDate, String userHubspotId, String orgHubspotId )
    {
        // LogService.logIt("HubspotUtils.createHubspotDeal() START userId=" + (user!=null ? user.getUserId() : "null") + ", orgId=" + (org==null ? "null" : org.getOrgId()) + ", userHubspotId=" + userHubspotId + ", orgHubspotId=" + orgHubspotId );
        init();

        if( !HUBSPOT_API_ON )
            return null;
        
        if( user==null )
            return null;
        
        if( dealName==null || dealName.isBlank() )
        {
            dealName = "HRA Website Action";
            if( org!=null)
                dealName += " " + org.getName();            
        }
           
        if( userHubspotId==null || userHubspotId.isBlank() )
            userHubspotId = getHubspotContactId(INCLUDE_HRA_IDS && user.getUserId()>0 ? user.getUserId() : 0, user.getEmail());  
        
        if( orgHubspotId==null || orgHubspotId.isBlank() )
            orgHubspotId = org==null ? null : getHubspotCompanyId(INCLUDE_HRA_IDS && org.getOrgId()>0 ? org.getOrgId() : 0, org.getCompanyUrl(), org.getName());  
        
        String dealHubspotId = getCreateHubspotDeal(dealName, description, amount, stage, closeDate, user, org, userHubspotId, orgHubspotId);
        
        // LogService.logIt("HubspotUtils.createHubspotDeal() Success=" + (dealHubspotId!=null && !dealHubspotId.isBlank()) + ", dealName=" + dealName + ", userId=" + user.getUserId() + ", email=" + user.getEmail() + ", dealHubspotId=" + dealHubspotId + ", " + (org==null ? "org is null." : "orgId=" + org.getOrgId() + ", org.domain=" + org.getCompanyUrl() + ", org.name=" + org.getName()) );
                
        return dealHubspotId;
    }
        
}
