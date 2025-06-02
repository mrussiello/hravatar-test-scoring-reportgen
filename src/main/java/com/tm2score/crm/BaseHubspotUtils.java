/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.crm;

import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.User;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import com.tm2score.util.HttpUtils;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.voicevibes.VoiceVibesException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.io.IOException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

/**
 *
 * @author miker_000
 */
public class BaseHubspotUtils {
    
    protected static Boolean HUBSPOT_API_ON = null;
    protected static String ACCESS_TOKEN = null;
    protected static String BASE_URL = null;
    protected static String BASE_PATH_CONTACTS = null;
    protected static String BASE_PATH_COMPANIES = null;
    protected static String BASE_PATH_DEALS = null;
    protected static String BASE_PATH_TICKETS = null;
    protected static String DEFAULT_DEAL_OWNER_EMAIL = null;
    protected static String DEFAULT_DEAL_OWNER_ID = null;
    protected static String DEFAULT_TICKET_OWNER_ID = null;
    
    
    
    protected static String SEARCH_SFX = "/search";
    protected static List<String> USER_PROPS = null;
    protected static List<String> COMPANY_PROPS = null;
    protected static List<String> DEAL_PROPS = null;
    protected static List<String> TICKET_PROPS = null;
    protected static Integer MIN_ORGID_FOR_UPDATE = null;
    protected static Boolean INCLUDE_HRA_IDS = null;
    
    
    
    protected static synchronized void init()
    {
        if( ACCESS_TOKEN!=null )
            return;
        
        HUBSPOT_API_ON = RuntimeConstants.getBooleanValue( "hubspot_api_on" );
        ACCESS_TOKEN = RuntimeConstants.getStringValue( "hubspot_private_app_access_token" );
        BASE_URL = RuntimeConstants.getStringValue( "hubspot_api_base_url" );
        BASE_PATH_CONTACTS = RuntimeConstants.getStringValue( "hubspot_api_base_contacts_path" );
        BASE_PATH_COMPANIES = RuntimeConstants.getStringValue( "hubspot_api_base_companies_path" );
        BASE_PATH_DEALS = RuntimeConstants.getStringValue( "hubspot_api_base_deals_path" );
        BASE_PATH_TICKETS = RuntimeConstants.getStringValue( "hubspot_api_base_tickets_path" );
        MIN_ORGID_FOR_UPDATE = RuntimeConstants.getIntValue("hubspot_min_orgid_for_update");        
        INCLUDE_HRA_IDS = RuntimeConstants.getBooleanValue("hubspot_include_hra_ids");
        DEFAULT_DEAL_OWNER_EMAIL = RuntimeConstants.getStringValue( "hubspot_default_deal_owner_email" );
        DEFAULT_DEAL_OWNER_ID = RuntimeConstants.getStringValue( "hubspot_default_deal_owner_id" );
        DEFAULT_TICKET_OWNER_ID = RuntimeConstants.getStringValue( "hubspot_default_ticket_owner_id" );
        
        /*
        44514782	Matthew	Santos	matt@hravatar.com
        44481531	Mike	Russiello	mike@hravatar.com
        10361838	Steve	Henson	steve@hravatar.com

        */
        
        USER_PROPS = new ArrayList<>();
        USER_PROPS.add("email");
        USER_PROPS.add("firstname");
        USER_PROPS.add("lastname");
        USER_PROPS.add("company");
        USER_PROPS.add("orgid");
        USER_PROPS.add("hra_userid");
        USER_PROPS.add("mobilephone");   
        USER_PROPS.add("phone");   
        USER_PROPS.add("jobtitle");   
        

        COMPANY_PROPS = new ArrayList<>();
        COMPANY_PROPS.add("name");
        COMPANY_PROPS.add("city");        
        COMPANY_PROPS.add("country");
        COMPANY_PROPS.add("domain");
        COMPANY_PROPS.add("hra_affiliateid");
        COMPANY_PROPS.add("orgid");
        COMPANY_PROPS.add("hra_cpid");

        DEAL_PROPS = new ArrayList<>();
        DEAL_PROPS.add("hs_object_id");
        DEAL_PROPS.add("hubspot_owner_id");
        DEAL_PROPS.add("dealname");        
        DEAL_PROPS.add("pipeline");  // "default"
        DEAL_PROPS.add("amount");   
        DEAL_PROPS.add("hs_acv"); // annual contract value
        DEAL_PROPS.add("dealtype"); // set to newbusiness
        DEAL_PROPS.add("createdate"); 
        DEAL_PROPS.add("closedate");
        DEAL_PROPS.add("description");
        
        
        TICKET_PROPS = new ArrayList<>();
        TICKET_PROPS.add("hubspot_owner_id");
        TICKET_PROPS.add("hs_pipeline_stage");  // set to "open"
        TICKET_PROPS.add("hs_ticket_category");    // Get from CsCaseType    
        TICKET_PROPS.add("createdate");
        TICKET_PROPS.add("hs_pipeline");
        TICKET_PROPS.add("content");
        TICKET_PROPS.add("hs_ticket_priority");  // HIGH MEDIUM LOW
        TICKET_PROPS.add( "subject" ); // Ticket name
    }

    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Functional Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////
    

    protected String getCreateHubspotDeal( String dealName, String description, float amount, String stage, Date closeDate, User user, Org org, String userHubspotId, String orgHubspotId )
    {
        String dealHubspotId = null;
        
        try
        {
            if( user==null  )
            {
                LogService.logIt( "BaseHubspotUtils.getCreateHubspotDeal() User is null" );
                return null;
            }

            if( org==null && user.getOrgId()>0 )
            {
                try
                {
                    org = UserFacade.getInstance().getOrg( user.getOrgId());
                }
                catch( Exception e )
                {
                    LogService.logIt(e, "HubspotUtils.getCreateHubspotDeal() Obtaining Org.orgId= " + user.getOrgId() + ", userId=" + user.getUserId() );
                }
            }
            
            // String orgHubspotId = null;
            
            if( org!=null && (orgHubspotId==null || orgHubspotId.isBlank())  )
            {
                Thread.sleep(4000);
                orgHubspotId = getHubspotCompanyId( INCLUDE_HRA_IDS ? org.getOrgId() : 0 , org.getCompanyUrl(), org.getName() );
            }

            if( userHubspotId==null || userHubspotId.isBlank()  )
            {
                Thread.sleep(4000);
                userHubspotId = getHubspotContactId( INCLUDE_HRA_IDS ? user.getUserId() : 0 , user.getEmail() );
            }

            
            if( userHubspotId==null || userHubspotId.isBlank()) 
            {
                LogService.logIt("BaseHubspotUtils.getCreateHubspotDeal() Cannot find a hubspot contact id for user:userId=" + user.getUserId() + ", firstname=" + user.getFirstName() + ", lastame=" + user.getLastName() + ", and email=" + user.getEmail() );
                return null;
            }
            
            if( closeDate==null )
            {
                GregorianCalendar cal = new GregorianCalendar();
                cal.add( Calendar.MONTH, 2 );
                closeDate = cal.getTime();
            }
            
            if( stage==null || stage.isBlank() )
                stage = "appointmentscheduled";
            
                                    
            JsonObjectBuilder job = Json.createObjectBuilder();
            
            job.add("dealname", dealName );
            job.add("hubspot_owner_id", DEFAULT_DEAL_OWNER_ID );
            job.add("pipeline", "default" );
            job.add( "amount", Float.toString(amount) );
            job.add("dealtype", "newbusiness" );
            job.add( "dealstage", stage );
            job.add( "createdate", getXmlDate( new Date() ));
            job.add( "closedate", getXmlDate( closeDate ));
            if( description!=null && !description.isBlank() )
                job.add( "description", description );
            
            String url = BASE_PATH_DEALS;

            JsonObjectBuilder job2 = Json.createObjectBuilder();
            job2.add( "properties", job );
            JsonObject jo = job2.build();
            
            JsonObject joResult = doApiPostOrPatch(url, jo, false);    
            
            if( joResult==null )
                return null;
            
            dealHubspotId = getIdValueFromCreateUpdateResults(joResult);
            
            LogService.logIt( "BaseHubspotUtils.getCreateHubspotDeal() DDD dealHubspotId=" + dealHubspotId + ", userHubspotId=" + userHubspotId + ", orgHubspotId=" + orgHubspotId );
            
            if( dealHubspotId!=null && !dealHubspotId.isBlank() )
            {
                doApiDealToContactPut(dealHubspotId, userHubspotId );

                if( orgHubspotId!=null && !orgHubspotId.isBlank() )
                    doApiDealToCompanyPut(dealHubspotId, orgHubspotId );
            }
            
            return dealHubspotId;
        }
        catch( STException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseHubspotUtils.getCreateHubspotDeal() userId=" + (user==null ? "null" : user.getUserId()) + ", userHubspotId=" + userHubspotId + ", dealHubspotId=" + dealHubspotId );            
            return null;            
        }
    }    
    
    
    protected String getCreateUpdateContact( User user, Org org, String userHubspotId, String orgHubspotId )
    {
        try
        {
            init();

            if( user==null  )
            {
                LogService.logIt( "BaseHubspotUtils.getCreateUpdateContact() User is null" );
                return null;
            }

            if( org==null && user.getOrgId()>0 )
            {
                try
                {
                    org = UserFacade.getInstance().getOrg( user.getOrgId());
                }
                catch( Exception e )
                {
                    LogService.logIt(e, "HubspotUtils.getCreateUpdateContact() Obtaining Org.orgId= " + user.getOrgId() + ", userId=" + user.getUserId() );
                }
            }
            
            // String orgHubspotId = null;
            
            if( org!=null && (orgHubspotId==null || orgHubspotId.isBlank())  )
            {
                Thread.sleep(4000);
                orgHubspotId = this.getHubspotCompanyId( INCLUDE_HRA_IDS ? org.getOrgId() : 0 , org.getCompanyUrl(), org.getName() );
            }
            
            if( (userHubspotId==null || userHubspotId.isBlank()) && 
                (user.getFirstName()==null || user.getFirstName().isBlank() || user.getLastName()==null || user.getLastName().isBlank() || user.getEmail()==null || user.getEmail().isBlank()) )
            {
                LogService.logIt("BaseHubspotUtils.getCreateUpdateContact() User data is invalid. Requires firstname=" + user.getFirstName() + ", lastame=" + user.getLastName() + ", and email=" + user.getEmail() );
                return null;
            }
            
            if( (userHubspotId==null || userHubspotId.isBlank()) && 
                !EmailUtils.validateEmailNoErrors(user.getEmail()) )
            {
                LogService.logIt("BaseHubspotUtils.getCreateUpdateContact() User data is invalid. Email is not a valid email address. Will be rejected by Hubspot. email=" + user.getEmail() );
                return null;
            }
                        
            JsonObjectBuilder job = Json.createObjectBuilder();
            if( user.getEmail()!=null && !user.getEmail().isBlank() && EmailUtils.validateEmailNoErrors( user.getEmail()) )
                job.add("email", user.getEmail() );

            if( user.getFirstName()!=null && !user.getFirstName().isBlank() )
                job.add("firstname", user.getFirstName());

            if( user.getLastName()!=null && !user.getLastName().isBlank() )
                job.add("lastname", user.getLastName());
            
            if( user.getTitle()!=null && !user.getTitle().isBlank() )
                job.add("jobtitle", user.getTitle());

            if( user.getMobilePhone()!=null && !user.getMobilePhone().isBlank() )
            {
                job.add("mobilephone", user.getMobilePhone());
                job.add("phone", user.getMobilePhone());
            }


            
            if( org!=null && org.getCompanyUrl()!=null && !org.getCompanyUrl().isBlank() )
            {
                String d2 = StringUtils.getTopDomain( org.getCompanyUrl() );
                // LogService.logIt( "HubspotUtils.getCreateUpdateContact() org.getCompanyUrl()=" + org.getCompanyUrl() + ", parsed=" + d2 );
                job.add("website", d2);
            }
            
            if( org!=null && org.getName()!=null && !org.getName().isBlank() )
                job.add("company", org.getName() );
            
            if( INCLUDE_HRA_IDS && org!=null && orgHubspotId!=null && !orgHubspotId.isBlank() && org.getOrgId()!=user.getOrgId() )
                job.add("orgid", org.getOrgId() );
            
            else if( INCLUDE_HRA_IDS && user.getOrgId()>0 )
                job.add("orgid", user.getOrgId() );
            
            if( INCLUDE_HRA_IDS && user.getUserId()>0 )
                job.add("hra_userid", user.getUserId() );
            
            String url = BASE_PATH_CONTACTS;

            // if we have a hubspotId this is an update.
            if( userHubspotId!=null && !userHubspotId.isBlank() )
                url += "/" + userHubspotId;

            JsonObjectBuilder job2 = Json.createObjectBuilder();
            job2.add( "properties", job );
            JsonObject jo = job2.build();
            
            JsonObject joResult = doApiPostOrPatch(url, jo, userHubspotId!=null && !userHubspotId.isBlank() );    
            
            if( joResult==null )
                return null;
            
            if( userHubspotId==null || userHubspotId.isBlank() )
                userHubspotId = this.getIdValueFromCreateUpdateResults(joResult);
            
            // LogService.logIt( "BaseHubspotUtils.getCreateUpdateContact() DDD userHubspotId=" + userHubspotId + ", orgHubspotId=" + orgHubspotId );
            
            if( orgHubspotId!=null && !orgHubspotId.isBlank() )
            {
                if( userHubspotId==null || userHubspotId.isBlank() )
                {
                    userHubspotId = getIdValueFromCreateUpdateResults(joResult);
                    if( userHubspotId == null || userHubspotId.isBlank() )
                        userHubspotId = this.getHubspotContactId( INCLUDE_HRA_IDS ? user.getUserId() : 0, user.getEmail() );

                    // LogService.logIt( "BaseHubspotUtils.getCreateUpdateContact() EEE userHubspotId=" + userHubspotId + ", orgHubspotId=" + orgHubspotId );
                }
                
                if( userHubspotId!=null && !userHubspotId.isBlank() )
                {
                    boolean success = doApiContactToCompanyPut(userHubspotId, orgHubspotId );
                    if( success )
                        doApiCompanyToContactPut(orgHubspotId, userHubspotId );
                }
            }
            
            return userHubspotId;
        }
        catch( STException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseHubspotUtils.getCreateUpdateContact() org=" + org.toString() + ", hubspotId=" + userHubspotId );            
            return null;            
        }
    }    


    protected String getCreateUpdateCompany( Org org, String orgHubspotId )
    {
        try
        {
            init();

            if( org==null  )
            {
                LogService.logIt( "BaseHubspotUtils.getCreateUpdateCompany() Org is null" );
                return null;
            }
            
            if( INCLUDE_HRA_IDS && org.getOrgId()>0 && org.getOrgId()<MIN_ORGID_FOR_UPDATE )
            {
                LogService.logIt( "BaseHubspotUtils.getCreateUpdateCompany() OrgId is invalid for create or update: " + org.getOrgId() );
                return null;                
            }
            
            if( (orgHubspotId==null || orgHubspotId.isBlank()) && (org.getCompanyUrl()==null || org.getCompanyUrl().isBlank() || !StringUtils.isValidURL(org.getCompanyUrl())  ) )
            {
                LogService.logIt( "BaseHubspotUtils.getCreateUpdateCompany() Cannot create company. Org.companyUrl is invalid: " + org.getCompanyUrl() );
                return null;                
            }
            
            if( (orgHubspotId==null || orgHubspotId.isBlank()) && 
                (org.getCompanyUrl()==null || org.getCompanyUrl().isBlank() || org.getName()==null || org.getName().isBlank()) )
            {
                LogService.logIt( "BaseHubspotUtils.getCreateUpdateCompany() hubspotId is null and Org data is invalid. Requires both companyUrl=" + org.getCompanyUrl() + ", and Name=" + org.getName() );
                return null;
            }

            JsonObjectBuilder job = Json.createObjectBuilder();
            
            if( org.getName()!=null && !org.getName().isBlank() )
                job.add("name", org.getName() );
            
            if( org.getCompanyUrl()!=null && !org.getCompanyUrl().isBlank() )
            {
                String d2 = StringUtils.getTopDomain( org.getCompanyUrl() );
                LogService.logIt( "HubspotUtils.getHubspotCompanyId() org.getCompanyUrl()=" + org.getCompanyUrl() + ", parsed=" + d2 );
                
                if( d2!=null && !d2.isBlank() )
                    job.add("domain", d2 );
            }

            if( org.getHqCountry()!=null && !org.getHqCountry().isBlank() )
                job.add("country", org.getHqCountry());

            if( org.getHqCity()!=null && !org.getHqCity().isBlank() )
                job.add("city", org.getHqCity());

            if( INCLUDE_HRA_IDS && org.getCampaignCode()!=null && !org.getCampaignCode().isBlank() )
                job.add("hra_cpid", org.getCampaignCode());

            if(INCLUDE_HRA_IDS && org.getOrgId()>0 )
                job.add("orgid", org.getOrgId());

            if( INCLUDE_HRA_IDS && org.getAffiliateId()!=null && !org.getAffiliateId().isBlank() )
                job.add("hra_affiliateid", org.getAffiliateId());

            String url = BASE_PATH_COMPANIES;
            // if we have a hubspotId this is an update.
            if( orgHubspotId!=null && !orgHubspotId.isBlank() )
                url += "/" + orgHubspotId;
            
            JsonObjectBuilder job2 = Json.createObjectBuilder();
            job2.add( "properties", job );
            JsonObject jo = job2.build();

            JsonObject jox =  doApiPostOrPatch(url, jo, orgHubspotId!=null && !orgHubspotId.isBlank() );  
            
            if( jox!=null )
                return this.getIdValueFromCreateUpdateResults(jox);
            
            return null;
        }
        catch( STException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseHubspotUtils.getCreateUpdateCompany() org=" + org.toString() + ", hubspotId=" + orgHubspotId );            
            return null;            
        }
    }    


    public String getHubspotCompanyId( int orgId, String domain, String name)
    {
        
        init();

        if( !HUBSPOT_API_ON )
            return null;
        
        if( !INCLUDE_HRA_IDS )
            orgId=0;
        // LogService.logIt( "HubspotUtils.getHubspotCompanyId() INCLUDE_HRA_IDS=" + INCLUDE_HRA_IDS + ", orgId=" + orgId + ", domain=" + domain + ", name=" + name );
        JsonObject jo = null;
        if( orgId>0 )
            jo = getCompanyByOrgId( orgId );
        // LogService.logIt( "HubspotUtils.getHubspotCompanyId() BBB " + (jo!=null) );

        if( jo==null && domain!=null && !domain.isBlank() )
        {
            String d2 = StringUtils.getTopDomain( domain );
            LogService.logIt( "HubspotUtils.getHubspotCompanyId() domain=" + domain + ", parsed=" + d2 );
            jo = getCompanyByDomain( d2 );
        }

        //LogService.logIt( "HubspotUtils.getHubspotCompanyId() CCC " + (jo!=null) );

        if( jo==null && name!=null && !name.isBlank() )
            jo = getCompanyByName( name );
        
        //LogService.logIt( "HubspotUtils.getHubspotCompanyId() DDD " + (jo!=null) );
        
        if( jo==null )
            return null;

        //LogService.logIt( "HubspotUtils.getHubspotCompanyId() EEE jo=" + JsonUtils.convertJsonObjecttoString(jo) );
        
        return getIdValueFromSearchResults( jo );
    }

    
    public String getHubspotContactId( long userId, String email)
    {
        init();

        if( !HUBSPOT_API_ON )
            return null;
        
        if( !INCLUDE_HRA_IDS )
            userId=0;
        // LogService.logIt( "HubspotUtils.getHubspotContactId() INCLUDE_HRA_IDS=" + INCLUDE_HRA_IDS + ", userId=" + userId + ", email=" + email );
        JsonObject jo = null;
        if( userId>0 )
            jo = getContactByUserId( userId );
        //LogService.logIt( "HubspotUtils.getHubspotContactId() AAA jo=" + (jo!=null) );
        if( jo==null && email!=null && !email.isBlank() )
            jo = getContactByEmail( email );
        //LogService.logIt( "HubspotUtils.getHubspotContactId() BBB jo=" + (jo!=null) );
        if( jo==null )
            return null;
        //LogService.logIt( "HubspotUtils.getHubspotContactId() CCC jo=" + JsonUtils.convertJsonObjecttoString(jo) );
        String v = getIdValueFromSearchResults( jo );

        // LogService.logIt( "HubspotUtils.getHubspotContactId() returning: " + v + ", DDD jo=" + JsonUtils.convertJsonObjecttoString(jo) );
        
        return v;
    }
    
    
    protected String getIdValueFromCreateUpdateResults( JsonObject jo )
    {
        if( jo==null )
            return null;
        
        try
        {
            if( jo.containsKey("id") && !jo.isNull( "id") )
                return jo.getString("id");
            
            
            if( jo.containsKey("properties") && !jo.isNull("properties"))
            {
                JsonObject ja = jo.getJsonObject("properties");

            if( jo.containsKey("hs_object_id") && !jo.isNull( "hs_object_id") )
                return jo.getString("hs_object_id");
                
            }
            return null;
        }
        catch( Exception e )
        {
            try
            {
                LogService.logIt( e, "BaseHubspotUtils.getIdValueFromCreateUpdateResults() " + JsonUtils.convertJsonObjecttoString(jo) ); 
            }
            catch( Exception ee )
            {
                LogService.logIt( ee, "BaseHubspotUtils.getIdValueFromCreateUpdateResults() Error converting jo to string." );                 
            }
            return null;          
        }                
    }
    
    
    
    protected String getIdValueFromSearchResults( JsonObject jo )
    {
        if( jo==null )
            return null;
        
        try
        {
            if( jo.containsKey("total") )
            {
                int total = jo.getInt("total");
                // LogService.logIt( "BaseHubspotUtils.getIdValueFromSearchResults() total=" + total ); 
                if( total<=0 )
                    return null;
            }
            
            if( jo.containsKey("results") && !jo.isNull( "results"))
            {
                JsonArray ja = jo.getJsonArray("results");
                for( JsonObject jox : ja.getValuesAs(JsonObject.class) )
                {
                    if( jox.containsKey("id") && !jox.isNull("id") )
                        return jox.getString("id");
                    
                    if( jox.containsKey( "properties") && !jox.isNull( "properties") )
                    {
                        JsonObject props = jox.getJsonObject("properties");
                        if( props.containsKey("hs_object_id") && !props.isNull("hs_object_id"))
                            return props.getString("hs_object_id");
                    }
                }
            }
            return null;
        }
        catch( Exception e )
        {
            try
            {
                LogService.logIt( e, "BaseHubspotUtils.getIdValueFromSearchResults() " + JsonUtils.convertJsonObjecttoString(jo) ); 
            }
            catch( Exception ee )
            {
                LogService.logIt( ee, "BaseHubspotUtils.getIdValueFromSearchResults() Error converting jo to string." );                 
            }
            return null;          
        }        
    }
    
    
    protected JsonObject getContactByHubspotId( String hubspotId )
    {
        init();
        String url = BASE_PATH_CONTACTS + "/" + hubspotId;
        return doApiGet( url );
    }    

    protected JsonObject getCompanyByHubspotId( String hubspotId )
    {
        init();
        String url = BASE_PATH_COMPANIES + "/" + hubspotId;
        return doApiGet( url );
    }    
    
    protected JsonObject getContactByUserId( long userId )
    {
        return getContactByValue( "hra_userid", userId );
    }    
        
    protected JsonObject getContactByEmail( String email )
    {
        return getContactByValue( "email", email );
    }    
    
    protected JsonObject getCompanyByName( String companyName )
    {
        return getCompanyByValue( "name", companyName );
    }
    
    protected JsonObject getCompanyByDomain( String webDomain )
    {
        String d2 = StringUtils.getTopDomain( webDomain );
        return getCompanyByValue( "domain", d2 );
    }
    
    protected JsonObject getCompanyByOrgId( int orgId )
    {
        return getCompanyByValue( "orgid", orgId );
    }

    private JsonObject getContactByValue( String propertyName, Object value )
    {
        init();            
        return getJsonObjectByValue( BASE_PATH_CONTACTS, propertyName, value, USER_PROPS );
    }
    
    protected JsonObject getCompanyByValue( String propertyName, Object value )
    {
        init();            
        return getJsonObjectByValue( BASE_PATH_COMPANIES, propertyName, value, COMPANY_PROPS );
    }

    protected JsonObject getJsonObjectByValue( String basePath, String propertyName, Object value, List<String> returnProps )
    {
        try
        {
            init();            
            String url = basePath + SEARCH_SFX;            
            Map<String,Object> vals = new HashMap<>();
            vals.put("value", value );
            vals.put("propertyName", propertyName );
            vals.put("operator", "EQ" );                        
            JsonObject jo = getSearchJsonObject( vals, returnProps );
            
            //LogService.logIt( "BaseHubspotUtils.getJsonObjectByValue() search payload=" + JsonUtils.convertJsonObjecttoString(jo));
            
            JsonObject j2 = doApiPostOrPatch( url, jo, false );   

            //LogService.logIt( "BaseHubspotUtils.getJsonObjectByValue() search response=" + JsonUtils.convertJsonObjecttoString(j2));
            
            return j2;
        }
        catch( STException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseHubspotUtils.getCompanyByValue() propertyName=" + propertyName + ", value=" + value.toString() );            
            return null;          
        }
    }

    
    protected JsonObject getSearchJsonObject( Map<String,Object> vals, List<String> returnProps ) throws Exception
    {
            JsonObjectBuilder job = Json.createObjectBuilder();
            
            // filter groups array
            JsonArrayBuilder jab = Json.createArrayBuilder();

            JsonObjectBuilder job2 = Json.createObjectBuilder();
            
            JsonArrayBuilder jab2 = Json.createArrayBuilder();
            
            JsonObjectBuilder job3 = Json.createObjectBuilder();
            
            Object o;
            for( String key : vals.keySet() )
            {
                o = vals.get(key);                
                
                // LogService.logIt( "BaseHubspotUtils.getSearchJsonObject() key=" + key + ", value=" + o.toString() );
                
                if( o instanceof Integer )
                    job3.add(key, (Integer)o );
                else if( o instanceof Long )
                    job3.add(key, (Long)o );
                else if( o instanceof Float )
                    job3.add(key, (Float)o );
                else if( o instanceof String )
                    job3.add(key, (String)o );
            }        
            
            // add the criteria to the filters array.
            jab2.add(job3);

            // add filters array to the filterGroup object
            job2.add("filters", jab2 );

            // add the filter group object to the filter groups array
            jab.add(job2);

            // add the filter groups array to the top object
            job.add("filterGroups", jab );
            
            if( returnProps!=null && !returnProps.isEmpty() )
            {
                JsonArrayBuilder jab3 = Json.createArrayBuilder();
                for( String s : returnProps )
                {
                    if( s==null || s.isBlank() )
                        continue;
                    
                    jab3.add(s);
                }
                job.add("properties",jab3);
            }
        
            return job.build();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // API Core
    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    // /crm/v3/objects/contacts/{contactId}/associations/{toObjectType}/{toObjectId}/{associationType}
    protected boolean doApiContactToCompanyPut( String contactHubspotId, String orgHubspotId ) throws Exception
    {
        init();
        String url = BASE_PATH_CONTACTS + "/" + contactHubspotId + "/associations/company/" + orgHubspotId + "/contact_to_company";
        boolean out = sendApiPut( url );        
        return out;
    }

    
    /*
     * /crm/v3/objects/companies/{companyId}/associations/{toObjectType}/{toObjectId}/{associationType}
    */
    protected boolean doApiCompanyToContactPut( String orgHubspotId, String contactHubspotId ) throws Exception
    {
        init();
        String url = BASE_PATH_COMPANIES + "/" + orgHubspotId + "/associations/contact/" + contactHubspotId + "/company_to_contact";
        boolean out = sendApiPut( url );
        return out;
    }

    
    /*
     * /crm/v3/objects/deals/{dealId}/associations/{toObjectType}/{toObjectId}/{associationType}
    */
    protected boolean doApiDealToCompanyPut( String dealHubspotId, String orgHubspotId ) throws Exception
    {
        init();
        String url = BASE_PATH_DEALS + "/" + dealHubspotId + "/associations/company/" + orgHubspotId + "/deal_to_company";
        boolean out = sendApiPut( url );        
        return out;
    }
    
    /*
     * /crm/v3/objects/deals/{dealId}/associations/{toObjectType}/{toObjectId}/{associationType}
    */
    protected boolean doApiDealToContactPut( String dealHubspotId, String contactHubspotId ) throws Exception
    {
        init();
        String url = BASE_PATH_DEALS + "/" + dealHubspotId + "/associations/contact/" + contactHubspotId + "/deal_to_contact";
        boolean out = sendApiPut( url );        
        return out;
    }    
    
    
    protected JsonObject doApiPostOrPatch( String url, JsonObject jo, boolean patch ) throws Exception
    {
        String payload = null;
        try
        {
            payload = JsonUtils.convertJsonObjecttoString(jo);
            //LogService.logIt( "BaseHubspotUtils.doApiPostOrPatch() AAA url=" + url + ", payload=" + payload ); 
            String resJo = sendApiPostOrPatch( url, null, payload, patch );
            if( resJo==null || resJo.isBlank() )
            {
                LogService.logIt( "BaseHubspotUtils.doApiPostOrPatch() BBB response json is missing. url=" + url + ", payload=" + payload ); 
                return null;
            }
            return JsonUtils.getJsonObject(resJo);
        }
        catch( STException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseHubspotUtils.doApiPost() XXX url=" + url + ", payload=" + payload );            
            throw new STException(e);
        }
    }

    
    
    private String sendApiPostOrPatch( String url, Map<String,String> params, String payload, boolean patch ) throws Exception
    {
        // CloseableHttpResponse r = null;
        try
        {
            return sendApiPostOrPatchCore(  url, params, payload, patch );            
            // StringBuilder sb = processAPIResponse( r );         
            // LogService.logIt( "BaseHubspotUtils.sendApiPostOrPatch() response=" + sb.toString() ); 

            // return sb.toString();
        }
        catch( STException e )
        {
            throw e;
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseHubspotUtils.sendApiPostOrPatch() url=" + url + ", patch=" + patch + ", payload=" + payload );            
            throw new STException(e);
        }

        //finally
        //{
        //    if( r != null )
        //    {
        //        if( r.getEntity()!=null )
        //            EntityUtils.consume(r.getEntity());
        //        r.close();
        //    }
        //}        
    }
    

    private String sendApiPostOrPatchCore( String url, Map<String,String> paramMap, String payload, boolean patch ) throws Exception
    {
        // String r = null;
        // int statusCode = 0;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
        {
            init();

            HttpUriRequestBase postOrPatch;

            if( !url.contains(BASE_URL) )
                url = BASE_URL + url;
            
            // LogService.logIt( "BaseHubspotUtils.sendApiPostCore() Preparing Request" );

            postOrPatch = patch ? new HttpPatch( url ) :  new HttpPost( url );            
            postOrPatch.setEntity( new StringEntity( payload ) );

            //int length = payload == null ? 0 : payload.length();
            
            //post.setHeader( "Content-Length", length + "" );
            String[] mimes = new String[]{"application/json","application/json"};
            postOrPatch.addHeader( "Content-Type", mimes[0] );
            postOrPatch.addHeader( "Accept", mimes[1] );

            postOrPatch.setHeader( "Authorization", "Bearer " + ACCESS_TOKEN  );
            
            if( paramMap!=null && !paramMap.isEmpty() )
            {
                List<NameValuePair> params = new ArrayList<>();
                
                for( String key : paramMap.keySet() )
                {
                    if( paramMap.get(key) != null && !paramMap.get(key).isEmpty() )
                        params.add(new BasicNameValuePair(key, paramMap.get(key)));   
                }
                
                postOrPatch.addHeader( "Content-Type", "application/x-www-form-urlencoded; charset=utf-8" );
                postOrPatch.setEntity(new UrlEncodedFormEntity(params));                
            }

            // LogService.logIt( "BaseHubspotUtils.sendApiPostCore() Executing Request" );
            return client.execute(postOrPatch, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "BaseHubspotUtils.sendApiPostCore() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "BaseHubspotUtils.sendApiPostCore() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    } );
            
            // LogService.logIt( "BaseHubspotUtils.sendApiPostCore() url=" + url + ", Response Status Code : " + r.getCode() + ", patch=" + patch );
            
            // statusCode = r.getCode();

            //if( !isStatusCodeOk( statusCode ) )
            // {
            //    StringBuilder sb = null;

            //    try
            //    {
            //        sb = processAPIResponse( r );
             //   }
            //    catch( Exception ee )
            //    {
            //        LogService.logIt( ee, "BaseHubspotUtils.sendApiPostOrPatchCore() ProcessingAPIResponse. url=" + url + ", payload=" + payload );
            //    }
            //                    
            //    LogService.logIt( "BaseHubspotUtils.sendApiPostOrPatchCore() Method failed: " + r.getReasonPhrase() + ", url=" + url + ", response=" + (sb==null ? "" : sb.toString()) + ", payload=" + payload );                
            //    throw new STException( "g.PassThru", "BaseHubspotUtils.sendApiPostOrPatchCore()  " + (patch ? "Patch" : "Post") + " failed to " + url + " with Http statuscode " + r.getReasonPhrase() + " responseContent=" + (sb==null ? "null" : sb.toString()) + ", payload=" + payload );
            //}
                        
            // return r;
        }        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "BaseHubspotUtils.sendApiPostOrPatchCore() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new STException( "g.PassThru", "BaseHubspotUtils.sendApiPostOrPatchCore()  " + (patch ? "Patch" : "Post") + " failed to " + url + ", " + e.toString() + ", payload=" + payload );
        }        
        
        catch( IOException e )
        {
            LogService.logIt( "BaseHubspotUtils.sendApiPostOrPatchCore() STERR " + e.toString() + ", url=" + url + ", patch=" + patch + ", payload=" + payload );            
            throw new STException( "g.PassThru", "BaseHubspotUtils.sendApiPostOrPatchCore()  " + (patch ? "Patch" : "Post") + " failed to " + url + ", " + e.toString() + ", payload=" + payload );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseHubspotUtils.sendApiPostOrPatchCore() url=" + url + ", patch=" + patch + ", payload=" + payload );            
            throw new STException(e);
        }        
    }
    
   
    
    private JsonObject doApiGet( String url )
    {
        try
        {
            String joStr = sendApiGet( url );

            if( joStr==null )
                return null;

            return JsonUtils.convertJsonStringToObject(joStr);
        }
        catch(STException e)
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseHubspotUtils.doApiGet() url=" + url );
            return null;            
            // throw new STException(e);
        }        
    }
   

    private boolean sendApiPut( String url )
    {
        try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
        {
            init();

            if( !url.contains(BASE_URL) )
                url = BASE_URL + url;
                        
            //LogService.logIt( "BaseHubspotUtils.sendApiPut() Preparing Request url=" + url );

            HttpPut get = new HttpPut( url );

            get.setHeader( "Authorization", "Bearer " + ACCESS_TOKEN  );

            //LogService.logIt( "BaseHubspotUtils.sendApiPut() Executing Request" );
            //try( CloseableHttpResponse r = client.execute( get ) )
            //{
            client.execute( get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "BaseHubspotUtils.sendApiPut() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    //String ss = EntityUtils.toString(entity2);
                    if( entity2!=null )
                        EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "BaseHubspotUtils.sendApiPut() statusCode="+ status + ", reason=" + response.getReasonPhrase() );
                    return null;
                    });

                //LogService.logIt( "BaseHubspotUtils.sendApiPut() url=" + url + ", Response Code : " + r.getCode() );

                //int statusCode = r.getCode();

                //if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "BaseHubspotUtils.sendApiPut() Method failed: " + r.getReasonPhrase() + ", url=" + url );
                //    sendInternalNotificationEmail( "HubSpot REST API Error", "BaseHubspotUtils.sendApiPut() Method failed: " + r.getReasonPhrase() + ", url=" + url );
                //    return false;
                //}
                
                //if( r.getEntity()!=null )
                 //   EntityUtils.consume(r.getEntity());

                return true;
            // }
        }        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "BaseHubspotUtils.sendApiGet() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            return false;
        }        
        catch( IOException e )
        {
            LogService.logIt( "BaseHubspotUtils.sendApiGet() " + e.toString() + ", url=" + url );            
            sendInternalNotificationEmail( "HubSpot REST API Error", "BaseHubspotUtils.sendApiGet() " + e.toString() + " url=" + url);
            return false;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseHubspotUtils.sendApiGet() " );            
            sendInternalNotificationEmail( "HubSpot REST API Error", "BaseHubspotUtils.sendApiGet() " + e.toString() + " url=" + url);
            return false;
        }
    }
    
        
    
    
    private String sendApiGet( String url ) throws Exception
    {
        // CloseableHttpResponse r = null;

        // int statusCode = 0;

        try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
        {
            init();

            if( !url.contains(BASE_URL) )
                url = BASE_URL + url;
                        
            //LogService.logIt( "BaseHubspotUtils.sendApiGet() Preparing Request url=" + url );

            HttpGet get = new HttpGet( url );

            get.addHeader( "Accept", "application/json" );

            get.setHeader( "Authorization", "Bearer " + ACCESS_TOKEN  );

            //LogService.logIt( "BaseHubspotUtils.sendApiGet() Executing Request" );
            //try( CloseableHttpResponse r = client.execute( get ) )
            //{
            return client.execute( get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "BaseHubspotUtils.sendApiGet() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "BaseHubspotUtils.sendApiGet() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    });

                //LogService.logIt( "BaseHubspotUtils.sendApiGet() url=" + url + ", Response Code : " + r.getCode() );

                //StringBuilder sb = processAPIResponse( r );

                //LogService.logIt( "BaseHubspotUtils.sendApiGet() Response is: " + sb.toString() );

                //statusCode = r.getCode();

                //if( !isStatusCodeOk( statusCode ) )
                //{
                //    LogService.logIt( "BaseHubspotUtils.sendApiGet() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                //    throw new STException( "g.PassThru", "BaseHubspotUtils.sendApiGet() Get failed with Http statuscode " + r.getReasonPhrase() );
                //}

                //if( r.getEntity()!=null )
                //    EntityUtils.consume(r.getEntity());
                //LogService.logIt( "BaseHubspotUtils.sendApiGet() downloaded json: " + sb.toString() );

                //return sb.toString();
            //}
        }        
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "BaseHubspotUtils.sendApiGet() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new STException( "g.PassThru", e.toString() );
        }        
        catch( IOException e )
        {
            // LogService.logIt( "BaseHubspotUtils.sendApiGet() " );
            
            sendInternalNotificationEmail( "HubSpot REST API Error", "BaseHubspotUtils.sendApiGet() " + e.toString() + " url=" + url);
            throw new STException( "g.PassThru", e.toString() );
            // throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseHubspotUtils.sendApiGet() " );
            
            sendInternalNotificationEmail( "HubSpot REST API Error", "BaseHubspotUtils.sendApiGet() " + e.toString() + " url=" + url);
            throw new STException(e);
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
    
    
    
    
    protected static void sendInternalNotificationEmail( String subject, String content )
    {
        try
        {

            EmailUtils emailUtils = EmailUtils.getInstance();

            if( content == null || content.isEmpty() )
                throw new Exception( "Content is missing." );
            
            if( subject==null || subject.isEmpty() )
                subject = "HR Avatar Hubspot API Integration Error Message";
            
            Map<String, Object> emailMap = new HashMap<>();

            emailMap.put( EmailUtils.SUBJECT, subject );

            emailMap.put( EmailUtils.CONTENT, content );

            emailMap.put( EmailUtils.TO, RuntimeConstants.getStringValue( "forwardSystemErrorsEmails" )  );

            StringBuilder sb = new StringBuilder();
            sb.append( RuntimeConstants.getStringValue("support-email") );
            emailMap.put( EmailUtils.FROM, sb.toString() );

            //LogService.logIt("BaseHubspotUtils.sendInternalNotificationEmail() content=" + content );

            emailUtils.sendEmail( emailMap );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseHubspotUtils.sendInternalNotificationEmail() SUBJ: " + subject + ", MSG: " + content );
        }

    }
    
    private String getXmlDate( Date d ) throws Exception
    {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.setTimeZone( TimeZone.getTimeZone("UTC"));

        XMLGregorianCalendar x = getXmlDate( gc );
        
        // LogService.logIt( "BasehubspotUtils.getXmlDate() XML Date Str=" + x.toXMLFormat() );
        
        return x.toXMLFormat();
    }
    

    private XMLGregorianCalendar getXmlDate( GregorianCalendar gc ) throws Exception
    {
        // return getXmlDate( gc.getTime() );
        return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
    }

    private String convertDateToString( Date d )
    {        
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");        
        format.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return format.format(d);
    }
        
    
}
