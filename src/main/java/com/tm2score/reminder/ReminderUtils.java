/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.reminder;

import com.tm2score.corp.CorpFacade;
import com.tm2score.crm.HubspotUtils;
import com.tm2score.email.EmailBlockFacade;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.purchase.Credit;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.OrgAutoTest;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.OnlineProctoringType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.purchase.ProductType;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.twilio.TwilioSmsUtils;
import com.tm2score.service.Tracker;
import com.tm2score.user.OrgAutoTestStatusType;
import com.tm2score.user.OrgStatusType;
import com.tm2score.user.UserActionFacade;
import com.tm2score.user.UserActionType;
import com.tm2score.user.UserFacade;
import com.tm2score.util.GooglePhoneUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


/**
 *
 * @author miker_000
 */
public class ReminderUtils {
    
    public static final int MAX_REMINDERS_FOR_TK = 10;
    
    EventFacade eventFacade;
    EmailBlockFacade emailBlockFacade;
    
    ReminderFacade reminderFacade;
    
    EmailUtils emailUtils;
    
    UserFacade userFacade;
    
    UserActionFacade userActionFacade;
    
    CorpFacade corpFacade;
    
    public ReminderUtils( EmailUtils eu )
    {
        eventFacade = EventFacade.getInstance();
    
        reminderFacade = ReminderFacade.getInstance();
    
        emailUtils = eu==null ? EmailUtils.getInstance() : eu;
    
        userFacade = UserFacade.getInstance();
    
        userActionFacade = UserActionFacade.getInstance();
    
        corpFacade = CorpFacade.getInstance();
    }


    public String doInvitationBatch() throws Exception
    {
        try
        {
            // LogService.logIt( "ReminderUtils.doInvitationBatch() START" );                
            
            int emailCount = 0;
            int textCount = 0;
            
            if( reminderFacade == null )
                reminderFacade = ReminderFacade.getInstance();
            
            Set<Long> testKeyIds = reminderFacade.getTestKeyIdsForInvitations(0, 0);
                        
            // LogService.logIt( "ReminderUtils.doInvitationBatch() Found " + testKeyIds.size() + " TestKeys to send to." );
            
            TestKey tk;
            
            Tracker.addReminderBatch();
            
            if( testKeyIds.isEmpty() )
                return "No TestKeyIds found that require invitations.";
            
            Date now = new Date();
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.HOUR, -2 );
            cal.add( Calendar.MINUTE, -15 );
            Date lastAccessCutoff = cal.getTime();
            
            cal.add( Calendar.DAY_OF_YEAR, -35 );
            Date createDateCutoff = cal.getTime();
            
            for( Long tkid : testKeyIds )
            {
                if( eventFacade == null )
                    eventFacade = EventFacade.getInstance();

                tk = eventFacade.getTestKey( tkid, true);
                
                if( tk == null )
                    throw new Exception( "TestKey is null tkId=" + tkid );
                
                if( tk.getTestKeyStatusTypeId()!=TestKeyStatusType.ACTIVE.getTestKeyStatusTypeId() )
                    continue;
                
                if( tk.getSendStartDate()!=null && tk.getSendStartDate().after( now ) )
                    continue;
                     
                // 5/4/2024 Based on query this would never happen.Only unexpired test keys based on date are collected from DBMS
                //if( tk.getExpireDate()!=null && tk.getExpireDate().before(now) )
                //{
                //    tk.setTestKeyStatusTypeId( TestKeyStatusType.EXPIRED.getTestKeyStatusTypeId() );
                //    eventFacade.saveTestKey(tk);
                //}

                if( tk.getStartDate()!=null )
                    continue;
                                                
                
                if( tk.getLastEmailDate()!=null || tk.getLastTextDate()!=null )                                
                    continue;
                
                emailCount += sendInvitationEmail( tk );                    
                
                if((tk.getUserMobilePhone()!=null && !tk.getUserMobilePhone().isBlank()) )
                    textCount += sendInvitationText( tk );                    
            }

            String msg = "ReminderUtils.doInvitationBatch() FINISH Sent " + emailCount + " emails and " + textCount + " text messages." ;
            
            if( emailCount>0 || textCount>0 )
                LogService.logIt( msg );            
            
            return msg;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doInvitationBatch() " );
            
            throw e;
        }
    }

    
    public String doReminderBatch() throws Exception
    {
        try
        {
            // LogService.logIt( "ReminderUtils.doReminderBatch() START" );                
            
            int emailCount = 0;
            int textCount = 0;
            
            if( reminderFacade == null )
                reminderFacade = ReminderFacade.getInstance();
            
            Set<Long> testKeyIds = reminderFacade.getTestKeyIdsForReminders(0, 0);
                        
            // LogService.logIt( "ReminderUtils.doReminderBatch() Found " + testKeyIds.size() + " TestKeys to send to." );
            
            TestKey tk;
            
            Tracker.addReminderBatch();
            
            if( testKeyIds.isEmpty() )
                return "No TestKeyIds found that require reminders.";
            
            Date now = new Date();
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.HOUR, -2 );
            cal.add( Calendar.MINUTE, -15 );
            Date lastAccessCutoff = cal.getTime();
            
            cal.add( Calendar.DAY_OF_YEAR, -35 );
            Date createDateCutoff = cal.getTime();
            
            for( Long tkid : testKeyIds )
            {
                if( eventFacade == null )
                    eventFacade = EventFacade.getInstance();

                tk = eventFacade.getTestKey( tkid, true);
                
                if( tk == null )
                    throw new Exception( "TestKey is null tkId=" + tkid );
                
                if( tk.getTestKeyStatusTypeId()!=TestKeyStatusType.ACTIVE.getTestKeyStatusTypeId() && tk.getTestKeyStatusTypeId()!=TestKeyStatusType.STARTED.getTestKeyStatusTypeId() )
                    continue;
                
                // accessed in last couple hours
                if( tk.getTestKeyStatusType().equals(TestKeyStatusType.STARTED) && (tk.getLastAccessDate()==null || tk.getLastAccessDate().after( lastAccessCutoff))  )
                    continue;
                
                // expired
                // 5/4/2024 Based on Query this would never happen. Only currently unexpired TestKeys are collected from dbms
                //if( tk.getExpireDate()!=null && tk.getExpireDate().before(now) )
                //{
                //    tk.setTestKeyStatusTypeId( TestKeyStatusType.EXPIRED.getTestKeyStatusTypeId() );
                //    eventFacade.saveTestKey(tk);
                //}

                // created too long ago.
                if( tk.getStartDate()!=null && tk.getStartDate().before(createDateCutoff) )
                    continue;
                                
                // no reminder days.
                if( tk.getReminderDays()<=0 )
                    continue;
                
                // Estimate reminder count. Stop at MAX Reminders
                if( (getDaysSinceStart( tk.getStartDate(), now )/tk.getReminderDays())>getMaxRemindersForTk(tk) )
                {
                    // LogService.logIt( "ReminderUtils.doReminderBatch() Halting Reminders for TestKey because max reminders already sent. Days=" + getDaysSinceStart( tk.getStartDate(), now ) + ", reminderDays=" + tk.getReminderDays() + ", testKeyId=" + tk.getTestKeyId() );
                    continue;
                }
                
                // time for a reminder email. Only if emailed in first place.
                if( tk.getLastEmailDate()!=null && tk.getLastEmailDate().getTime() <= now.getTime()-24*60*60*1000*tk.getReminderDays() )                                
                    emailCount += sendReminderEmail( tk );                    
                
                if( (tk.getUserMobilePhone()!=null && !tk.getUserMobilePhone().isBlank()) &&  (tk.getLastTextDate()==null || (tk.getLastTextDate()!=null && tk.getLastTextDate().getTime()<= now.getTime()-24*60*60*1000*tk.getReminderDays()*2)) )                                
                    textCount += sendReminderText( tk );                    
            }

            String msg = "ReminderUtils.doReminderBatch() FINISH Sent " + emailCount + " emails and " + textCount + " text messages." ;
            
            if( emailCount>0 || textCount>0 )
                LogService.logIt( msg );            
            
            return msg;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doReminderBatch() " );
            
            throw e;
        }
    }
    
    public int getMaxRemindersForTk( TestKey tk ) throws Exception
    {
        if( tk.getOrg()==null )
        {
            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            tk.setOrg( userFacade.getOrg( tk.getOrgId() ));                
        }
        
        if( tk.getOrg().getOrgStatusTypeId()!=OrgStatusType.ACTIVE.getOrgStatusTypeId() )
            return 0;
        
        if( tk.getSuborgId()>0 && tk.getSuborg()==null )
        {
            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));                
        }
        
        // If there is a flag set
        if( (tk.getOrg()!=null && tk.getOrg().getReportFlags()!=null && !tk.getOrg().getReportFlags().isBlank() && tk.getOrg().getReportFlags().contains("maxreminderspertestkey")) || 
            (tk.getSuborg()!=null && tk.getSuborg().getReportFlags()!=null && !tk.getSuborg().getReportFlags().isBlank() && tk.getSuborg().getReportFlags().contains("maxreminderspertestkey")) )
        {            
            Integer rr = ReportUtils.getReportFlagIntValue("maxreminderspertestkey", null, null, tk.getSuborg(), tk.getOrg(), null );
            if( rr!=null && rr>0 )
                return rr;            
        }

        return MAX_REMINDERS_FOR_TK;
    }


    public String doTestKeyExpireWarningBatch() throws Exception
    {
        int total = 0;
        int sent = 0;           
        try
        {
            // LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() START" );
                        
            if( reminderFacade == null )
                reminderFacade = ReminderFacade.getInstance();
            
            Set<Long> testKeyIds ;
            TestKey tk;
            
            Tracker.addTkExpWarningBatch();
            
            Date now = new Date();    
            Calendar cal;
            Date winEnd;
            Date maxLastSend;
            boolean validEmail;
            
            String extendUrlBase = RuntimeConstants.getStringValue( "adminappbasuri" ) + "/misc/tkextend/ee.xhtml";
            String extendUrl;
            String expDateStr;
            Locale locale;
            TimeZone tz;
            String[] params = new String[12];
            String subj;
            String content;
            String hralogoUrl = RuntimeConstants.getStringValue("default-email-logo"); // "hraCompanyLogoSmall" );
            Map<String, Object> emailMap;
            String authEm;
            for( int days=1;days<=3;days++ )
            {
                cal = new GregorianCalendar();
                cal.add( Calendar.DAY_OF_MONTH, -1*days );
                maxLastSend = cal.getTime();
                
                
                cal = new GregorianCalendar();
                cal.add( Calendar.DAY_OF_MONTH, days );
                winEnd = cal.getTime();
                                                
                testKeyIds = reminderFacade.getTestKeyIdsForExpirationReminders( days );
                
                // LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() days=" + days + ". Found " + testKeyIds.size() + " TestKeys to send to." );
                
                for( Long tkid : testKeyIds )
                {
                    total++;
                    if( eventFacade == null )
                        eventFacade = EventFacade.getInstance();

                    tk = eventFacade.getTestKey( tkid, true);

                    if( tk==null )
                        throw new Exception( "TestKey is null tkId=" + tkid );

                    if( tk.getTestKeyStatusTypeId()>TestKeyStatusType.STARTED.getTestKeyStatusTypeId() )
                        continue;

                    if( tk.getExpireDate()==null )
                        continue;

                    // must set org before test for expire
                    if( userFacade==null )
                        userFacade=UserFacade.getInstance();    
                    tk.setOrg( userFacade.getOrg( tk.getOrgId() ));
                    
                    expireTestKeyIfNeeded( tk, tk.getOrg() );
                    
                    if( tk.getLastExpireWarningDate()!=null && tk.getLastExpireWarningDate().after( maxLastSend ) )
                    {
                        LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() Not sending because last warning send was more recent than days back. days=" + days + ", maxLastSend date=" + maxLastSend + ", TestKeyId=" + tk.getTestKeyId() );
                        continue;                                                
                    }
                    
                    //if( tk.getExpireDate()!=null && tk.getExpireDate().before(now) )
                    //{
                    //    tk.setTestKeyStatusTypeId( TestKeyStatusType.EXPIRED.getTestKeyStatusTypeId() );
                    //    eventFacade.saveTestKey(tk);
                    //    continue;
                    //}
                    
                    if( tk.getExpireDate()!=null && tk.getExpireDate().after(winEnd) )
                        continue;
                    
                    if( tk.getExpireWarnDays()!=days )
                        continue;

                    
                    if( tk.getOrg().getOrgStatusTypeId()!=0 )
                    {
                        LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() TestKey.Org is not in active status. orgId=" + tk.getOrgId() + ", TestKeyId=" + tk.getTestKeyId() );
                        continue;                        
                    }
                    
                    if( tk.getAuthorizingUserId()>0 )
                        tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
                    
                    if( tk.getAuthUser()==null )
                    {
                        LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() Cannot find AuthUser. authUserId=" + tk.getAuthorizingUserId() + ", TestKeyId=" + tk.getTestKeyId() );
                        continue;
                    }

                    if( !tk.getAuthUser().getUserType().getNamed() )
                    {
                        LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() AuthUser is the wrong UserType. TestKeyId=" + tk.getTestKeyId() );
                        continue;                        
                    }
                                        
                    locale = tk.getAuthUser().getLocaleToUseDefaultUS();
                    
                    tz = tk.getAuthUser().getTimeZone();
                    if( tz==null )
                        tz = TimeZone.getDefault();
                    
                    authEm = EmailUtils.cleanEmailAddress(tk.getAuthUser().getEmail());
                    
                    validEmail = EmailUtils.validateEmailNoErrors(authEm);            
            
                    if( emailBlockFacade==null )
                        emailBlockFacade = EmailBlockFacade.getInstance();

                    if( emailBlockFacade.hasEmailBlock(authEm, false, false ) )
                    {
                        LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() Email blocked for " + authEm );
                        validEmail=false;
                    }
                    
                    if( !validEmail )
                    {
                        // LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() AuthUser does not have a valid email=" + tk.getAuthUser().getEmail() + ", TestKeyId=" + tk.getTestKeyId() );
                        continue;                                                
                    }

                    
                    if( tk.getUserId()>0 )
                        tk.setUser( userFacade.getUser( tk.getUserId() ));
                    
                    expDateStr = I18nUtils.getFormattedDate(locale, tz, tk.getExpireDate() );
                    
                    tk.setProduct( eventFacade.getProduct( tk.getProductId() ));
                    
                    extendUrl = extendUrlBase + "?t=" + tk.getTestKeyIdEncrypted() + "&d=";
                    
                    params[0] = tk.getPin();
                    params[1] = extendUrl;
                    params[2] = tk.getProduct().getName();
                    params[3] = tk.getOrg().getName();
                    params[4] = tk.getUser()==null ? MessageFactory.getStringMessage(locale, "g.TkExpireNoName" )  : tk.getUser().getFullname();
                    params[5] = tk.getUser()==null ? ""  : " (" + tk.getUser().getEmail() + ")";
                    params[6] = tk.getAuthUser().getFullname();
                    params[7] = expDateStr;
                    params[8] = Integer.toString(days);
                    params[9] = hralogoUrl;
                    params[10]= RuntimeConstants.getStringValue("baseadmindomain");
                    params[11]= RuntimeConstants.getStringValue("default-site-name");
                    
                    
                    subj = MessageFactory.getStringMessage(locale, "g.TkExpireWarningSubj", params );
                    content = MessageFactory.getStringMessage(locale, "g.TkExpireWarningContentStart", params ) +  MessageFactory.getStringMessage(locale, "g.TkExpireWarningContent", params );
                    
                    content = EmailUtils.addNoReplyMessage(content, true, locale);
                    // LogService.logIt( "ReminderUtils.doTestKeyExpireWarningBatch() Subject=" + subj + "\n, content=" + content );
                    
                    
                    emailMap = new HashMap<>();
                    emailMap.put( EmailUtils.SUBJECT, subj );
                    emailMap.put( EmailUtils.CONTENT, content );
                    
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append( authEm );        
                    emailMap.put( EmailUtils.TO, authEm );

                    sb = new StringBuilder();
                    sb.append( RuntimeConstants.getStringValue("no-reply-email") ); // + "|" + MessageFactory.getStringMessage( getLocale(), "g.SupportEmailKey", null ) );
                    emailMap.put( EmailUtils.FROM, sb.toString() );

                    emailMap.put( EmailUtils.MIME_TYPE, "text/html" );

                    if( emailUtils.sendEmail( emailMap ) )
                    {
                        sent++;
                        tk.setLastExpireWarningDate( new Date() );
                        eventFacade.saveTestKey(tk);
                    }

                }

                
            }

            String msg = "ReminderUtils.doTestKeyExpireWarningBatch() FINISH total=" + total + ", sent=" + sent ;
            
            //if( total>0 || sent>0 )
            //    LogService.logIt( msg );
            return msg;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doTestKeyExpireWarningBatch() Fatal Error  total=" + total + ", sent=" + sent );            
            throw e;
        }
    }
    
    
    private int getDaysSinceStart( Date d1, Date d2 )
    {
        if( d1==null || d2==null || d2.before(d1) )
            return 0;
        return (int) (d2.getTime() - d1.getTime())/(1000*60*60*24);        
    }
    
    
    
    public String doExpireSubscriptionBatch() throws Exception
    {
        try
        {            
            if( reminderFacade == null )
                reminderFacade = ReminderFacade.getInstance();
            
            Set<Integer> orgIds;
            Org org;
            User adminUser;
            User affiliateAdminUser;
            int count=0;
            StringBuilder sb = new StringBuilder();
            sb.append( "Expiring Subscription Warnings Batch\n" );
   
            for( int expireDays : Constants.SUBSCRIPTION_EXPIRATION_WARNING_DAYS )
            {
                orgIds = reminderFacade.getOrgIdListForExpiration( expireDays );
            
                // LogService.logIt( "ReminderUtils.doExpireSubscriptionBatch() AAA found " + orgIds.size() + " orgIds with subscriptions expiring in " + expireDays );
            
                for( Integer orgId : orgIds )
                {
                    if( userFacade==null )
                        userFacade = UserFacade.getInstance();
                    
                    org = userFacade.getOrg( orgId );
                
                    // Skip all of these./
                    if( org==null || org.getAdminUserId()<=0 || org.getOrgStatusTypeId()!=0  )
                    {
                        continue;
                    }
                    
                    if( org.getAffiliateId()!=null && org.getAffiliateAccountTypeId()>1 )
                    {
                        if( !org.getAffiliateId().equals("icims") && !org.getAffiliateId().equals("jazzhr"))                        
                            continue;
                    }
                    
                    adminUser = userFacade.getUser( org.getAdminUserId() );
                    
                    affiliateAdminUser = null;                    
                    if( org.getAffiliateId()!=null && !org.getAffiliateId().isBlank() && org.getAffiliateAccountTypeId()>1 )
                    {
                        Org affiliateOrg = userFacade.getSourceOrgForAffiliateId( org.getAffiliateId() );
                        if( affiliateOrg!=null && affiliateOrg.getAdminUserId()>0 )
                            affiliateAdminUser = userFacade.getUser( affiliateOrg.getAdminUserId() );
                    }
                    
                    sendSubscriptionExpirationEmail(org, adminUser, affiliateAdminUser, expireDays, org.getAffiliateId() );
                    
                    count++;
                    sb.append( "Emailing Org " + org.getName() + " (" + org.getOrgId() + ") expiration in " + expireDays + " days.\n" );
                }                    

                
                // if( count>0  )
                //   LogService.logIt( sb.toString() );                
            }

            for( int expireDays : Constants.SUBSCRIPTION_EXPIRATION_ADMIN_NOTIFICATION_DAYS )
            {
                orgIds = reminderFacade.getOrgIdListForExpiration( expireDays );
            
                // LogService.logIt( "ReminderUtils.doExpireSubscriptionBatch() BBB found " + orgIds.size() + " orgIds with subscriptions expiring in " + expireDays  + " for ADMIN notification only." );
            
                for( Integer orgId : orgIds )
                {
                    if( userFacade==null )
                        userFacade = UserFacade.getInstance();
                    
                    org = userFacade.getOrg( orgId );
                
                    // Skip all of these./
                    if( org==null || org.getAdminUserId()<=0 || org.getOrgStatusTypeId()!=0 ||  org.getAffiliateAccountTypeId()>1 )
                        continue;
                    
                    adminUser = userFacade.getUser( org.getAdminUserId() );
                    affiliateAdminUser = null;                    
                    if( org.getAffiliateId()!=null && !org.getAffiliateId().isBlank() && org.getAffiliateAccountTypeId()>1 )
                    {
                        Org affiliateOrg = userFacade.getSourceOrgForAffiliateId( org.getAffiliateId() );
                        if( affiliateOrg!=null && affiliateOrg.getAdminUserId()>0 )
                            affiliateAdminUser = userFacade.getUser( affiliateOrg.getAdminUserId() );
                    }
                    
                    sendSubscriptionExpirationEmailToAdmins(org, adminUser, affiliateAdminUser, expireDays, org.getAffiliateId() );
                    
                    count++;
                    sb.append( "Emailing HRA ADMIN ONLY Org " + org.getName() + " (" + org.getOrgId() + ") subscription expiration in " + expireDays + " days.\n" );
                    
                    if( expireDays==60 && (org.getAffiliateId()==null || org.getAffiliateId().isBlank() || hubspotDealTicketOk(org.getAffiliateId()) ) )
                        createHubspotDealForSubscriptionExpire(adminUser, org);
                }                    
                
                // if( count>0  )
                //    LogService.logIt( sb.toString() );                
            }

            
            sb.append( "Total emails sent: " + count  + "\n" );
            
            return sb.toString();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doExpireSubscriptionBatch() " );
            
            throw e;
        }
    }
    
    protected boolean hubspotDealTicketOk( String affiliateId )
    {
        if( affiliateId==null || affiliateId.isBlank() )
            return true;
        
        if( affiliateId.equals("hraph") )
            return false;
        if( affiliateId.equals("midot") )
            return false;
        if( affiliateId.equals("dragnet") )
            return false;
        return true;
        
    }


    public void createHubspotDealForCreditZero( User u, Org o)
    {
        try
        {
            if( u==null || u.getUserId()<=0 || !u.getUserType().getNamed() )
            {
                LogService.logIt( "ReminderUtils.createHubspotDealForCreditZero() User is invalid. " + (u==null ? "null" : u.toString() ) );
                return;
            }
            if( o==null || o.getOrgId()<=0  )
            {
                LogService.logIt( "ReminderUtils.createHubspotDealForCreditZero() Org is invalid. " + (o==null ? "null" : o.toString() ) );
                return;
            }
            
            String dealName = "Credits Exhausted - " + o.getName() + " (" + o.getOrgId() + ")";

            Date closeDate = o.getOrgCreditUsageEndDate();
            if( closeDate==null )
            {
                GregorianCalendar cal = new GregorianCalendar();
                cal.add( Calendar.DAY_OF_MONTH, 60);
                closeDate = cal.getTime();
            }

            String description = "Account: " + o.getName() + " [" + o.getOrgId() + "]\n" +
                                "         " + o.getCompanyUrl() + "\n" + 
                                "Admin User: " + u.getFullname() + ", " + u.getEmail() + " [" + u.getUserId() + "]\n" +
                                "Partner: " + (o.getAffiliateId()==null ? "" : o.getAffiliateId() );               
            
            createHubspotDeal(  u,  o,  closeDate, 0, dealName, description);            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.createHubspotDealForSubscriptionExpire() orgId=" + (o==null ? "null" : o.getOrgId()) );
        }
    }    
    
    
    public void createHubspotDealForSubscriptionExpire( User u, Org o)
    {
        try
        {
            if( u==null || u.getUserId()<=0 || !u.getUserType().getNamed() )
            {
                LogService.logIt( "ReminderUtils.createHubspotDealForSubscriptionExpire() User is invalid. " + (u==null ? "null" : u.toString() ) );
                return;
            }
            if( o==null || o.getOrgId()<=0  )
            {
                LogService.logIt( "ReminderUtils.createHubspotDealForSubscriptionExpire() Org is invalid. " + (o==null ? "null" : o.toString() ) );
                return;
            }
            
            String dealName = "Subscription Renewal - " + o.getName() + " (" + o.getOrgId() + ")";

            Date closeDate = o.getOrgCreditUsageEndDate();
            if( closeDate==null )
            {
                GregorianCalendar cal = new GregorianCalendar();
                cal.add( Calendar.DAY_OF_MONTH, 60);
                closeDate = cal.getTime();
            }

            String description = "Account: " + o.getName() + " [" + o.getOrgId() + "]\n" +
                                "         " + o.getCompanyUrl() + "\n" + 
                                "Subscription Expires: " + o.getOrgCreditUsageEndDate().toString() + "\n" +
                                "Admin User: " + u.getFullname() + ", " + u.getEmail() + " [" + u.getUserId() + "]\n" +
                                "Partner: " + (o.getAffiliateId()==null ? "" : o.getAffiliateId() );               
            
            createHubspotDeal(  u,  o,  closeDate, 0, dealName, description);            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.createHubspotDealForSubscriptionExpire() orgId=" + (o==null ? "null" : o.getOrgId()) );
        }
    }

    public void createHubspotDeal( User u, Org o, Date closeDate, float amount, String dealName, String description)
    {
        try
        {
            if( u==null || u.getUserId()<=0 || !u.getUserType().getNamed() )
            {
                LogService.logIt( "ReminderUtils.createHubspotDeal() User is invalid. " + (u==null ? "null" : u.toString() ) );
                return;
            }
            if( o==null || o.getOrgId()<=0  )
            {
                LogService.logIt( "ReminderUtils.createHubspotDeal() Org is invalid. " + (o==null ? "null" : o.toString() ) );
                return;
            }
            
            if( (dealName==null || dealName.isBlank()) && (description==null || description.isBlank()) )
            {
                LogService.logIt( "ReminderUtils.createHubspotDeal() No deal name or description. No information. " +  o.toString() + ", " + u.toString() );
                return;                
            }

            if( dealName==null || dealName.isBlank() )
                dealName="Unknown Deal";

            if( description==null || description.isBlank() )
                dealName="Empty Description (Tm2Score)";

            
            
            // get the hubspot company.
            // get the hubspot user.
            // create deal.
            // Post to HubSpot
            HubspotUtils hsu = new HubspotUtils();  
            if( hsu.getIsHubspotOn() ) // CSCaseType.getValue(csCase.getCsCaseTypeId()).getIsSalesOrCompany() && user.getNamed() )
            {      

                if( o.getCompanyUrl()==null || o.getCompanyUrl().isBlank() )
                    o.setCompanyUrl("hraorg-" + o.getOrgId() + ".com");

                String hubspotCompanyId = hsu.getHubspotCompanyId(o.getOrgId(), o.getCompanyUrl(), o.getName());

                // create/update Org record on hubspot only if not already there. 
                if( hubspotCompanyId==null || hubspotCompanyId.isBlank() )
                   hubspotCompanyId = hsu.createOrUpdateHubspotCompany(o);

                // user record.
               String hubspotUserId = hsu.createOrUpdateHubspotContact(u, o, null, hubspotCompanyId); 

               // Create Deal if relevant
                if( hubspotUserId!=null && !hubspotUserId.isBlank() )
                {
                    if( closeDate==null )
                    {
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.add( Calendar.DAY_OF_MONTH, 60);
                        closeDate = cal.getTime();
                    }
                    
                                        
                    String hubspotDealId = hsu.createHubspotDeal(u, o, dealName, description, 0, "20030549", closeDate, hubspotUserId, hubspotCompanyId );
                    // LogService.logIt( "ReminderUtils.createHubspotDeal() Created Hubspot Deal Id=" + hubspotDealId + " for Suscription Renewal. orgId=" + o.getOrgId() + ", " + o.getName() );
               }
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.createHubspotDeal() orgId=" + (o==null ? "null" : o.getOrgId()) );
        }
        
        
    }

    

    public String doExpireCreditsBatch() throws Exception
    {
        try
        {            
            if( reminderFacade == null )
                reminderFacade = ReminderFacade.getInstance();
            
            List<Integer[]> orgInfoList;
            Org org;
            User adminUser;
            User affiliateAdminUser;
            int count=0;
            StringBuilder sb = new StringBuilder();
            sb.append( "Expiring Candidate Credits Warnings Batch\n" );
   
            int orgId;
            int creditsExpiring;
            
            for( int expireDays : Constants.CREDITS_EXPIRATION_WARNING_DAYS )
            {
                orgInfoList = reminderFacade.getOrgInfoListForCreditsExpiration( expireDays );
            
                //if( 1==1 )
                //{
                //    orgIds = new ArrayList<>();
                //    orgIds.add( new Integer[]{3,456});
                //}
                
                // LogService.logIt("ReminderUtils.doExpireCreditsBatch() AAA found " + orgInfoList.size() + " orgIds with credits expiring in " + expireDays );
            
                for( Integer[] orgInfo : orgInfoList )
                {
                    orgId = orgInfo[0];
                    creditsExpiring = orgInfo[1];
                    if( creditsExpiring<=0 )
                        continue;
                    
                    if( userFacade==null )
                        userFacade = UserFacade.getInstance();
                    
                    org = userFacade.getOrg( orgId );
                
                    // Skip all of these./
                    if( org==null || org.getAdminUserId()<=0 || org.getOrgStatusTypeId()!=0 ||  org.getAffiliateAccountTypeId()>1 )
                        continue;
                    
                    adminUser = userFacade.getUser( org.getAdminUserId() );
                    
                    affiliateAdminUser = null;                    
                    if( org.getAffiliateId()!=null && !org.getAffiliateId().isBlank() && org.getAffiliateAccountTypeId()>1 )
                    {
                        Org affiliateOrg = userFacade.getSourceOrgForAffiliateId( org.getAffiliateId() );
                        if( affiliateOrg!=null && affiliateOrg.getAdminUserId()>0 )
                            affiliateAdminUser = userFacade.getUser( affiliateOrg.getAdminUserId() );
                    }
                    
                    sendCreditsExpirationEmail(org, creditsExpiring, adminUser, affiliateAdminUser, expireDays, org.getAffiliateId() );
                    
                    count++;
                    sb.append( "Emailing Org " + org.getName() + " (" + org.getOrgId() + ") credit expiration in " + expireDays + " days.\n" );
                }                    

                
                if( count>0  )
                   LogService.logIt( sb.toString() );                
            }

            
            sb.append( "Total emails sent: " + count  + "\n" );
            
            return sb.toString();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doExpireCreditsBatch() " );
            
            throw e;
        }
    }

    

    public String doCreditZeroBatch() throws Exception
    {
        try
        {            
            if( reminderFacade == null )
                reminderFacade = ReminderFacade.getInstance();
            
            Org o;
            User u;
            int count = 0;
            
            StringBuilder sb = new StringBuilder();
            sb.append( "Credit Zero Deals Batch\n" );
            
            List<Credit> pcz = reminderFacade.getPendingCreditZeros();
            sb.append( "Credit Zero Deals Batch. Found " + pcz.size() + " pending credit zeros.\n" );
            
            for( Credit c : pcz  )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();

                o = userFacade.getOrg( c.getOrgId() );
                u = o!=null && o.getAdminUserId()>0 ? userFacade.getUser( o.getAdminUserId() ) : null;
                
                // LogService.logIt("ReminderUtils.doCreditZeroBatch() AAA found creditId=" + c.getCreditId() + ", orgId=" + c.getOrgId() + ", userId=" + (u==null ? "null" : u.getUserId()) +", creditZeroDate=" + c.getCreditZeroDate() + ", creditZeroStatusTypeId=" + c.getCreditZeroStatusTypeId() );
            
                if( o==null )
                {
                    LogService.logIt("ReminderUtils.doCreditZeroBatch() BBB Cannot find org orgId=" + c.getOrgId() );                   
                    continue;
                }
                
                // No deal 
                if( o.getAffiliateId()!=null && !o.getAffiliateId().isBlank() && !hubspotDealTicketOk(o.getAffiliateId()) )
                {
                    continue;
                }

                if( u==null )
                {
                    LogService.logIt("ReminderUtils.doCreditZeroBatch() BBB Cannot find Admin User for org orgId=" + + c.getOrgId() + ",  AdminUserId=" + o.getAdminUserId() );                   
                    continue;
                }
                
                
                if( c.getInitialCount()>=5 )
                {
                    createHubspotDealForCreditZero( u, o);
                    // LogService.logIt("ReminderUtils.doCreditZeroBatch() CCC Created Hubspot Deal for Credit Zero. orgId=" + o.getOrgId() + " " + o.getName() + ", adminUserId=" + u.getUserId() + ", creditId=" + c.getCreditId() );

                    sb.append( "Created Hubspot Deal for Credit Zero. orgId=" + o.getOrgId() + " " + o.getName() + ", adminUserId=" + u.getUserId() + ", creditId=" + c.getCreditId() + ", ");

                    count++;
                }
                
                c.setCreditZeroStatusTypeId(0);
                reminderFacade.saveCredit( c );                
            } 
            
            sb.append( "Total Count: " + count );
            
            return sb.toString();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doExpireCreditsBatch() " );
            
            throw e;
        }
    }


    
    
    public String doExpireOrgAutoTestRecordsBatch() throws Exception
    {
        try
        {
            int dateExpireCount = 0;
            int eventCountExpireCount = 0;
            int emailsSent = 0;
            
            if( reminderFacade == null )
                reminderFacade = ReminderFacade.getInstance();
            
            Set<Integer> OrgAutoTestIds = reminderFacade.getOrgAutoTestIdListForExpiration(Constants.ORGAUTOTEST_EXPIRATION_WARNING_HOURS, Constants.ORGAUTOTEST_EXPIRATION_WARNING_WINDOW_MINS );
            
            // LogService.logIt( "ReminderUtils.doExpireOrgAutoTestRecordsBatch() AAA found " + OrgAutoTestIds.size() + " records to process." );
            Date now = new Date();
            OrgAutoTest oat;
            TimeZone tz = TimeZone.getTimeZone("UTC");
            Calendar calWarnStart = new GregorianCalendar();
            calWarnStart.setTimeZone(tz);
            Calendar calWarnEnd = new GregorianCalendar();
            calWarnEnd.setTimeZone(tz);
            
            calWarnEnd.add(Calendar.HOUR, Constants.ORGAUTOTEST_EXPIRATION_WARNING_HOURS);
            calWarnEnd.add(Calendar.SECOND, 1);
            
            calWarnStart.add(Calendar.HOUR, Constants.ORGAUTOTEST_EXPIRATION_WARNING_HOURS);
            calWarnStart.add(Calendar.MINUTE, -1*Constants.ORGAUTOTEST_EXPIRATION_WARNING_WINDOW_MINS);
            calWarnStart.add(Calendar.SECOND, -1*1);
            
            Date warnStart = calWarnStart.getTime();
            Date warnEnd = calWarnEnd.getTime();
            Org org;
            
            for( Integer orgAutoTestId : OrgAutoTestIds )
            {
                oat = reminderFacade.getOrgAutoTest( orgAutoTestId );
                
                if( oat==null || oat.getOrgAutoTestStatusTypeId()==OrgAutoTestStatusType.INACTIVE.getOrgAutoTestStatusTypeId() )
                    continue;
                
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();
                
                org = userFacade.getOrg(oat.getOrgId() );

                GregorianCalendar c2 = new GregorianCalendar();
                c2.setTime( oat.getExpireDate() );
            
                //LocalDate localDate = LocalDate.now();
                //LocalDateTime localDateTime = LocalDateTime.now();
                
                //ZoneId zoneId = ZoneId.of("-05:00");
                
                //ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, zoneId);
                
                //ogService.logIt( "ReminderUtils.doExpireOrgAutoTestRecordsBatch() AAA.1 localDate=" + localDate.toString() + ", localDateTime=" + localDateTime.toString() + ", zonedDateTime +5=" + zonedDateTime.toString() );
                
                // LogService.logIt( "ReminderUtils.doExpireOrgAutoTestRecordsBatch() AAA.X  orgAutoTestId=" + orgAutoTestId + ", now=" + (new Date()).toString() + ", expireDate=" + oat.getExpireDate().toString() + ", c2=" + c2.getTime().toString() + " warnStart=" + warnStart.toString()+ ", warnEnd=" + warnEnd.toString() + ", c2.timeZone=" + c2.getTimeZone().getID() + ", oat.getExpireDate().after(warnStart)=" + oat.getExpireDate().after(warnStart) + ", oat.getExpireDate().before(warnEnd)=" + oat.getExpireDate().before(warnEnd) );
                
                if( oat.getExpireDate()!=null && oat.getExpireDate().before(now) )
                {
                    LogService.logIt( "ReminderUtils.doExpireOrgAutoTestRecordsBatch() BBB.1 expiring " + oat.getOrgAutoTestId() + ", expireDate=" + oat.getExpireDate().toString() + ", now=" + now.toString() );
                    dateExpireCount++;
                    expireOrgAutoTest( oat );
                    if( oat.getSendExpireNoticeTypeId()==1 && org.getOrgStatusTypeId()==OrgStatusType.ACTIVE.getOrgStatusTypeId() )
                    {
                        sendAutoTestExpirationMsgs( oat, 0 );
                        emailsSent++;
                    }
                }
                else if( oat.getMaxEvents()>0 && oat.getMaxEvents()<=oat.getEventCount() )
                {
                    LogService.logIt( "ReminderUtils.doExpireOrgAutoTestRecordsBatch() BBB.2 disabling due to max events " + oat.getOrgAutoTestId() );
                    eventCountExpireCount++;
                    expireOrgAutoTest( oat );
                    if( oat.getSendExpireNoticeTypeId()==1 && org.getOrgStatusTypeId()==OrgStatusType.ACTIVE.getOrgStatusTypeId() )
                    {
                        sendAutoTestExpirationMsgs( oat, oat.getMaxEvents() );
                        emailsSent++;
                    }
                }
                
                else if( oat.getSendExpireNoticeTypeId()==1 && 
                         oat.getExpireDate()!=null && 
                         oat.getExpireDate().after(warnStart) && 
                         oat.getExpireDate().before(warnEnd) && 
                         org.getOrgStatusTypeId()==OrgStatusType.ACTIVE.getOrgStatusTypeId() )
                {
                    // LogService.logIt( "ReminderUtils.doExpireOrgAutoTestRecordsBatch() BBB.3 Sending expiration warning for " + oat.getOrgAutoTestId() + ", expireDate=" + oat.getExpireDate().toString() + ", now=" + now.toString()  );
                    sendAutoTestExpirationMsgs( oat, -1 );
                    emailsSent++;
                }
            }
            
            String msg = "ReminderUtils.doExpireOrgAutoTestRecordsBatch() Expired " + eventCountExpireCount + " orgAutoTest records due to event count and " + dateExpireCount + " due to date expiration. Emails sent=" + emailsSent;
            if( dateExpireCount>0 || eventCountExpireCount>0 )
                LogService.logIt( msg );
            
            return msg;
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.doExpireOrgAutoTestRecordsBatch() " );
            
            throw e;
        }
    }
    
    
    public void expireTestKeyIfNeeded( TestKey tk, Org org ) throws Exception
    {
        // Expired
        if( tk.getTestKeyStatusTypeId()<= TestKeyStatusType.STARTED.getTestKeyStatusTypeId() && tk.getExpireDate()!=null && tk.getExpireDate().before( new Date() ) )
        {
            boolean expireIt = false;
            
            // Once started, they have 60 days after expiration. If not started, expired immediately.
            
            // if started, add grace period.
            if( tk.getTestKeyStatusTypeId()==TestKeyStatusType.STARTED.getTestKeyStatusTypeId())
            {
                Calendar cal = new GregorianCalendar();
                cal.setTime( tk.getExpireDate() );

                int graceMins = 0;
                int m = ReportUtils.getReportFlagIntValue("tkrestartexpiregracemins", tk, null,null, org, null);
                if( m>0 )
                    graceMins = m; // Integer.parseInt(m.get("tkrestartexpiregracemins"));
                
                // has custom grace mins
                if( graceMins>0 )
                    cal.add( Calendar.MINUTE, graceMins );
                
                // default grace is 24 hours
                else
                    cal.add( Calendar.DAY_OF_YEAR, 1 );

                // add another day to avoid expiring/overwriting a test key that is in process in the test engine.
                cal.add( Calendar.DAY_OF_YEAR, 1 );
                
                if( (new Date()).after( cal.getTime() ) )
                    expireIt=true;
            }
            
            // never started, expire it.
            else
                expireIt=true;
            
            if( expireIt )
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.EXPIRED.getTestKeyStatusTypeId() );
                if( eventFacade == null ) 
                    eventFacade = EventFacade.getInstance();
                eventFacade.saveTestKey(tk);
            }
        }
        
    }
    
    
    public void expireOrgAutoTest( OrgAutoTest oat ) throws Exception
    {
        // LogService.logIt( "ReminderUtils.expireOrgAutoTest() Expiring " + oat.getOrgAutoTestId() );
        
        oat.setOrgAutoTestStatusTypeId(OrgAutoTestStatusType.INACTIVE.getOrgAutoTestStatusTypeId() );
        if( reminderFacade == null )
            reminderFacade = ReminderFacade.getInstance();
        reminderFacade.saveOrgAutoTest(oat);
        
    }
    
    
    public void prepareTestKey( TestKey tk, boolean email ) throws Exception
    {
        if( tk.getUserId()> 0 && tk.getUser()==null )
        {
            if( userFacade==null )
                userFacade = UserFacade.getInstance();
            tk.setUser( userFacade.getUser( tk.getUserId() ));
        }

        if( tk.getOrg()==null )
        {
            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            tk.setOrg( userFacade.getOrg( tk.getOrgId() ));                
        }

        if( tk.getOrg().getCcOnCandEmails()>0 && tk.getAuthorizingUserId()> 0 && tk.getAuthUser()==null )
        {
            if( userFacade==null )
                userFacade = UserFacade.getInstance();
            tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
        }
        
        if( tk.getProduct()==null )
        {
            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();
            
            tk.setProduct( eventFacade.getProduct( tk.getProductId() ));
        }
        
        if( tk.getCorpId()> 0 && tk.getCorp()==null )
        {
            if( corpFacade == null )
                corpFacade = CorpFacade.getInstance();

            tk.setCorp( corpFacade.getCorp( tk.getCorpId() ) );
            
        }
        
        String protocol = RuntimeConstants.getStringValue( "testingappprotocol" );

        if( tk.getCorp()!=null && RuntimeConstants.getBooleanValue("testingapphttpsOK") )
            protocol = "https";

        if( !RuntimeConstants.getBooleanValue("testingapphttpsOK") )
            protocol = "http";

        if( protocol==null || protocol.isBlank() )
            protocol = "http";
        
        if( tk.getStartUrl()==null || tk.getStartUrl().isEmpty() )
        {
            String baseUrl = protocol + "://" + RuntimeConstants.getStringValue( "testingappbasedomain" ) + "/" + RuntimeConstants.getStringValue( "testingappcontextroot" ) + "/c.xhtml";

            tk.setStartUrl(baseUrl + "?tk=" + tk.getPin() + "&rs=0" );
        }
        
        if( !email )
            return;
            
        if( tk.getSuborgId()>0 && tk.getSuborg()==null )
        {
            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            tk.setSuborg( userFacade.getSuborg( tk.getSuborgId() ));                
        }
    }

    private int sendInvitationText( TestKey tk ) throws Exception
    {
        return sendInvitationOrReminderText( tk, true );
    }

    private int sendReminderText( TestKey tk ) throws Exception
    {
        return sendInvitationOrReminderText( tk, false );
    }

        
    private int sendInvitationOrReminderText( TestKey tk, boolean invitation ) throws Exception
    {
        try
        {
            if( !RuntimeConstants.getBooleanValue( "twilio.textingon" ).booleanValue() )
                return 0;

            if( tk.getUserMobilePhone()==null || tk.getUserMobilePhone().isEmpty()  )
            {
                // LogService.logIt( "ReminderUtils.sendReminderText()  Could not send a reminder text because no valid text found. " + tk.toString() );
                return 0;
            }

            prepareTestKey( tk, false );
            
            Tracker.addReminderText();
            
            Locale locale = getLocaleForTestTaker( tk );
    
            String countryCode = null;
            
            if( tk.getUser()==null && tk.getUserId()>0 )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                
                tk.setUser( userFacade.getUser( tk.getUserId() ));
            }

            if( tk.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();                
                tk.setOrg( userFacade.getOrg( tk.getOrgId() ));
            }

            if( tk.getOrg()!=null && !tk.getOrg().getIsSmsOk() )
                return 0;
            
            boolean smsOk = GooglePhoneUtils.getIsPhoneNumberAllowedForSms( tk.getUserMobilePhone(), tk.getOrg(), tk.getUser(), tk.getAuthUser() );
            if( !smsOk )
            {
                //LogService.logIt("ReminderUtils.smsTestKey() testing to international number for this org is not allowed. tkid=" + tk.getTestKeyId() + ", tk.userId=" + tk.getUserId() + ", phone=" + tk.getUserMobilePhone() );
                return 0;                        
            }
            
                        
            if( tk.getUser()!=null )
            {
                countryCode = tk.getUser().getCountryCode();
                
                if( countryCode==null || countryCode.isBlank() )
                    countryCode = tk.getUser().getIpCountry();
            }
            
            if( (countryCode==null || countryCode.isBlank()) )
            {
                if( tk.getAuthUser()==null && tk.getAuthorizingUserId()>0 )
                {
                    if( userFacade==null )
                        userFacade = UserFacade.getInstance();

                    tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
                }
                
                if( tk.getAuthUser()!=null )
                {
                    countryCode = tk.getAuthUser().getCountryCode();

                    if( countryCode==null || countryCode.isBlank() )
                        countryCode = tk.getAuthUser().getIpCountry();
                }
            }

            boolean incPassword = tk.getUserAnonymityType().getHasUsername();
                 
            boolean started = !invitation && tk.getTestKeyStatusType().getIsStarted();
            
            String msg = MessageFactory.getStringMessage( locale , incPassword ? ( started ? "g.TestSMSInvitation_UsernameStarted" : "g.TestSMSInvitation_Username") : (started ? "g.TestSMSInvitationStarted" : "g.TestSMSInvitation") , new String[] { RuntimeConstants.getStringValue( "testingappbasedomain" ), tk.getOrg().getName(), tk.getPin(), tk.getUser()==null ? tk.getTempFirstName() : tk.getUser().getFirstName() } );

            int sent = TwilioSmsUtils.sendTextMessageViaThread(tk.getUserMobilePhone(), countryCode, locale, null, msg );

            if( sent>0 )
            {
                tk.setLastTextDate( new Date() );
                
            }
            else if( sent==0 )
                LogService.logIt( "ReminderUtils.sendReminderText() Unable to send text reminder to " + tk.getUserMobilePhone() + ", testKeyId=" + tk.getTestKeyId() +", sent=" + sent  );
            
            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            eventFacade.saveTestKey(tk);

            //if( userFacade == null )
            //    userFacade = UserFacade.getInstance();

            User tku = new User();

            tku.setEmail( tk.getUserEmail()  );
            tku.setFirstName( tk.getUserFirstName());
            tku.setLastName( tk.getUserLastName());
            tku.setAltIdentifier( tk.getUserAltId() );
            tku.setUserId( tk.getUserId() );
            tku.setOrgId( tk.getOrgId() );
            tku.setSuborgId( tk.getSuborgId() );

            if( userActionFacade == null )
                userActionFacade = UserActionFacade.getInstance();

            userActionFacade.saveMessageAction(tku, msg, tk.getTestKeyId(), tk.getOrgAutoTestId(), UserActionType.SENT_TEXT.getUserActionTypeId() );
            
            return 1;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "ReminderUtils.sendReminderText() " + tk.toString() + ", tk.reminderDays=" + tk.getReminderDays() + ", lastEmailDate=" + tk.getLastTextDate()  );
            return 0;
        }
        
    }


    public void sendSubscriptionExpirationEmail( Org org, User adminUser, User affiliateAdminUser, int expireDays, String affiliateId)
    {
        sendSubscriptionExpirationEmail( org, adminUser, affiliateAdminUser, expireDays, false, affiliateId );
    }    
    
    public void sendSubscriptionExpirationEmailToAdmins( Org org, User adminUser, User affiliateAdminUser, int expireDays, String affiliateId)
    {
        sendSubscriptionExpirationEmail( org, adminUser, affiliateAdminUser, expireDays, true, affiliateId );
    }    
    
    public void sendSubscriptionExpirationEmail( Org org, User adminUser, User affiliateAdminUser, int expireDays, boolean adminsOnly, String affiliateId )
    {
        try
        {
            if( org==null || adminUser==null )
                return;
            
            Locale loc = adminUser.getLocaleToUseDefaultUS();
            
            String email = adminUser.getEmail();
            
            if( email==null )
                email = "";  
            
            email=email.trim();
            
            if( email.startsWith(","))
                email = email.substring(1,email.length());
            
            if( email.endsWith(","))
                email = email.substring(0, email.length()-1);
                            
            if( email.isEmpty() )
            {
                LogService.logIt( "ReminderUtils.sendExpirationEmail() No email to mail to. " + adminUser.toString() + " orgId=" + org.getOrgId() + ", " + org.getName() );
                return;
            }
            
            boolean validEmail = EmailUtils.validateEmailNoErrors(email);            
            
            if( emailBlockFacade==null )
                emailBlockFacade = EmailBlockFacade.getInstance();

            if( emailBlockFacade.hasEmailBlock(email.trim(), false, false ) )
            {
                LogService.logIt( "ReminderUtils.sendExpirationEmail() Email blocked for " + email );
                validEmail=false;
            }
            
            
            if( loc == null )
                loc = Locale.US;
            
            String[] params = new String[]{ org.getName(), Integer.toString(expireDays), adminUser.getFullname() };            
            String subj = MessageFactory.getStringMessage( loc, "g.SubscripExpireWrngSubject", params );    
            
            String msg = MessageFactory.getStringMessage( loc, "g.SubscripExpireWrngContent", params );
            
            msg = MessageFactory.getStringMessage(Locale.US, "g.OatNoteDivStyle", null ) + msg + "</div>";

            if( adminsOnly )
                msg = EmailUtils.addNoReplyMessage(msg, true, loc);
            
            if( adminsOnly )
            {
                String subjPrefix = "[HRA admin only msg]";
                if( affiliateId!=null && !affiliateId.isBlank() )
                    subjPrefix += "[" + affiliateId + "]";
                subj = subjPrefix + " " + subj;
                msg = "<p><b>SENT TO HR AVATAR ADMINS ONLY:</b></p>" + msg;
            }
                        
            // LogService.logIt( "ReminderUtils.sendExpirationEmail() sending email " + org.getName() + " (" + org.getOrgId() + ") subj=" + subj + ", msg=" + msg );
            
            sendRemEmail(subj, msg, validEmail ? email : null, org, adminUser, affiliateAdminUser, !adminsOnly, adminsOnly );            
            
            /*
            if( emailUtils == null )
                emailUtils = EmailUtils.getInstance();
            
            // first, get plain text string used to describe order.
            Map<String, Object> emailMap = new HashMap<>();

            emailMap.put( EmailUtils.SUBJECT, subj );
            emailMap.put( EmailUtils.CONTENT, msg );
            
            
            String adminEmails = RuntimeConstants.getStringValue("forwardNewCsRequestsAddressJob" );
            
            if( affiliateAdminUser!=null && EmailUtils.validateEmailNoErrors( affiliateAdminUser.getEmail() ) )
            {
                if( !adminEmails.isBlank() )
                    adminEmails += ",";
                adminEmails += affiliateAdminUser.getEmail();
            }
            
            if( adminsOnly )
                emailMap.put( EmailUtils.TO, adminEmails );

            else
            {
                emailMap.put( EmailUtils.TO, validEmail ? email : RuntimeConstants.getStringValue("support-email") );            
                emailMap.put( EmailUtils.CC,  adminEmails );
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append( RuntimeConstants.getStringValue("no-reply-email") ); // + "|" + MessageFactory.getStringMessage( getLocale(), "g.SupportEmailKey", null ) );
            emailMap.put( EmailUtils.FROM, sb.toString() );
            emailMap.put( EmailUtils.MIME_TYPE, "text/html" );

            boolean sent = emailUtils.sendEmail( emailMap );
            
            if( !sent )
                return;

            if( userActionFacade == null )
                userActionFacade = UserActionFacade.getInstance();

            userActionFacade.saveMessageAction(adminUser, subj + " sent to : " + email + " for OrgId=" + org.getOrgId(), 0, 0, UserActionType.SENT_EMAIL.getUserActionTypeId() );                
            */
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.sendExpirationEmail() Error sending email " + org.getName() + " (" + org.getOrgId() + ")" );
        }        
    }
    
    
    public void sendCreditsExpirationEmail( Org org, int creditsExpiring, User adminUser, User affiliateAdminUser, int expireDays, String affiliateId )
    {
        try
        {
            if( org==null || adminUser==null )
                return;
            
            Locale loc = adminUser.getLocaleToUseDefaultUS();
            
            String email = adminUser.getEmail();
            
            if( email==null )
                email = "";  
            
            email=email.trim();
            
            if( email.startsWith(","))
                email = email.substring(1,email.length());
            
            if( email.endsWith(","))
                email = email.substring(0, email.length()-1);
                            
            if( email.isEmpty() )
            {
                LogService.logIt( "ReminderUtils.sendCreditsExpirationEmail() No email to mail to. " + adminUser.toString() + " orgId=" + org.getOrgId() + ", " + org.getName() );
                return;
            }
            
            boolean validEmail = EmailUtils.validateEmailNoErrors(email);            
            
            if( emailBlockFacade==null )
                emailBlockFacade = EmailBlockFacade.getInstance();

            if( emailBlockFacade.hasEmailBlock(email.trim(), false, false ) )
            {
                LogService.logIt( "ReminderUtils.sendCreditsExpirationEmail() Email blocked for " + email );
                validEmail=false;
            }
            
            
            if( loc == null )
                loc = Locale.US;
            
            String[] params = new String[]{ org.getName(), Integer.toString(expireDays), adminUser.getFullname(), Integer.toString(creditsExpiring) };            
            String subj = MessageFactory.getStringMessage( loc, "g.CreditsExpireWrngSubject", params );    
            
            String msg = MessageFactory.getStringMessage( loc, "g.CreditsExpireWrngContent", params );
            
            msg = MessageFactory.getStringMessage(Locale.US, "g.OatNoteDivStyle", null ) + msg + "</div>";

            // msg = EmailUtils.addNoReplyMessage(msg, true, loc);
            
            LogService.logIt( "ReminderUtils.sendCreditsExpirationEmail() sending email " + org.getName() + " (" + org.getOrgId() + ") subj=" + subj + ", msg=" + msg );
            
            sendRemEmail(subj, msg, validEmail ? email : null, org, adminUser, affiliateAdminUser, true, false );            
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.sendCreditsExpirationEmail() Error sending email " + org.getName() + " (" + org.getOrgId() + ")" );
        }        
    }
    
    
    
    public void sendAutoTestExpirationMsgs( OrgAutoTest oat, int maxEvents )
    {
        try
        {
            String email = oat.getEmailResultsTo();
            
            Locale loc = null;
            
            if( oat.getLang()!=null && !oat.getLang().isEmpty() )
                loc = I18nUtils.getLocaleFromCompositeStr( oat.getLang() );
            
            User authUser = null;
            User affiliateAdminUser = null;
            String fullname = null;
            Org org = null;
            
            if( oat.getName()==null || oat.getName().isBlank() )
            {
                LogService.logIt( "ReminderUtils.sendAutoTestExpirationMsgs() Oat has no name. Not sending email. " + oat.toString() );
                return;                
            }

            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            org = userFacade.getOrg( oat.getOrgId() );
            
            affiliateAdminUser = null;                    
            if( org!=null && org.getAffiliateId()!=null && !org.getAffiliateId().isBlank() && org.getAffiliateAccountTypeId()>1 )
            {
                Org affiliateOrg = userFacade.getSourceOrgForAffiliateId( org.getAffiliateId() );
                if( affiliateOrg!=null && affiliateOrg.getAdminUserId()>0 )
                    affiliateAdminUser = userFacade.getUser( affiliateOrg.getAdminUserId() );
            }
                        
            if( oat.getAuthUserId()>0 )
            {
                authUser = userFacade.getUser( oat.getAuthUserId() );
                
                if( authUser!=null )
                {
                    loc = authUser.getLocaleToUseDefaultNull();
                    fullname = authUser.getFullname();
                }
                
                if( authUser!=null && authUser.getEmail()!=null && email!=null && !email.isBlank() && email.trim().equalsIgnoreCase( authUser.getEmail().trim( )) )
                    email = null;
            }
            
            if( email==null )
                email = "";
            
            if( fullname==null )
                fullname = email;
                        
            email=email.trim();
            
            if( email.startsWith(","))
                email = email.substring(1,email.length());
            
            if( email.endsWith(","))
                email = email.substring(0, email.length()-1);
                
            email = EmailUtils.cleanEmailAddress(email);
            
            if( !EmailUtils.validateEmailNoErrors(email) )
                email="";
            
            else
            {
                if( emailBlockFacade==null )
                    emailBlockFacade = EmailBlockFacade.getInstance();

                if( emailBlockFacade.hasEmailBlock(email.trim(), false, false ) )
                {
                    LogService.logIt( "ReminderUtils.sendAutoTestExpirationMsgs() Email blocked for " + email );
                    email="";
                }
            }            
            
            if( authUser !=null && authUser.getEmail()!=null && !authUser.getEmail().isBlank() )
            {
                if( emailBlockFacade==null )
                    emailBlockFacade = EmailBlockFacade.getInstance();

                if( emailBlockFacade.hasEmailBlock(authUser.getEmail().trim(), false, false ) )
                    LogService.logIt( "ReminderUtils.sendAutoTestExpirationMsgs() Email blocked for " + authUser.getEmail() );                    

                else
                {
                    if( !email.isEmpty() )
                        email += ",";
                    email += EmailUtils.cleanEmailAddress(authUser.getEmail());
                }
            }
            
            if( email.isEmpty() )
            {
                LogService.logIt( "ReminderUtils.sendAutoTestExpirationMsgs() No email to mail to. " + oat.toString() );
                return;
            }
            
            if( loc == null )
                loc = Locale.US;
            
            boolean warning = maxEvents<0;
            
            String subj;
            String msg;

            String[] params = new String[]{oat.getName(), Integer.toString(maxEvents), fullname, Integer.toString( Constants.ORGAUTOTEST_EXPIRATION_WARNING_HOURS), RuntimeConstants.getStringValue("baseadmindomain")};
            
            if( warning )
            {
                subj = MessageFactory.getStringMessage( loc, "g.OatWarningSubject", params );          
                msg = MessageFactory.getStringMessage( loc, "g.OatWarningContent", params );                
            }
            else
            {
                subj = MessageFactory.getStringMessage( loc, maxEvents<=0 ? "g.OatNoteSubject" : "g.OatNoteSubjectMaxEvents", params );          
                msg = MessageFactory.getStringMessage( loc, maxEvents<=0 ? "g.OatNoteContentDate" : "g.OatNoteContentMaxEvents", params );
            }
            
            msg = MessageFactory.getStringMessage(loc, "g.OatNoteDivStyle", null ) + msg + "</div>";
            
            
            if( emailUtils == null )
                emailUtils = EmailUtils.getInstance();
            
            // first, get plain text string used to describe order.
            Map<String, Object> emailMap = new HashMap<>();

            emailMap.put( EmailUtils.SUBJECT, subj );

            msg = EmailUtils.addNoReplyMessage(msg, true, loc);           
            emailMap.put( EmailUtils.CONTENT, msg );
            emailMap.put( EmailUtils.TO, email );

            StringBuilder sb = new StringBuilder();
            sb.append( RuntimeConstants.getStringValue("no-reply-email") ); // + "|" + MessageFactory.getStringMessage( getLocale(), "g.SupportEmailKey", null ) );
            emailMap.put( EmailUtils.FROM, sb.toString() );

            if( affiliateAdminUser!=null && EmailUtils.validateEmailNoErrors( affiliateAdminUser.getEmail() ) )
                emailMap.put( EmailUtils.CC, affiliateAdminUser.getEmail() );
                        
            emailMap.put( EmailUtils.MIME_TYPE, "text/html" );

            boolean sent = emailUtils.sendEmail( emailMap );

            if( authUser !=null && sent )
            {
                if( userActionFacade == null )
                    userActionFacade = UserActionFacade.getInstance();

                userActionFacade.saveMessageAction(authUser, subj + " sent to : " + email + " for OrgAutoTestId=" + oat.getOrgAutoTestId(), 0, oat.getOrgAutoTestId(), UserActionType.SENT_EMAIL.getUserActionTypeId() );                
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderUtils.sendAutoTestExpirationMsgs() Error sending email " + oat.toString() );
        }
    }
    
    private void sendRemEmail( String subj, String msg, String email, Org org, User adminUser, User affiliateAdminUser, boolean useSupportEmail, boolean adminsOnly)
    {    
        try
        {
            email = EmailUtils.cleanEmailAddress(email);
            if( emailUtils == null )
                emailUtils = EmailUtils.getInstance();
            
            // first, get plain text string used to describe order.
            Map<String, Object> emailMap = new HashMap<>();

            emailMap.put( EmailUtils.SUBJECT, subj );
            emailMap.put( EmailUtils.CONTENT, msg );
                        
            String adminEmails = RuntimeConstants.getStringValue("forwardNewCsRequestsAddressJob" );
            
            if( affiliateAdminUser!=null && EmailUtils.validateEmailNoErrors( affiliateAdminUser.getEmail() ) )
            {
                if( !adminEmails.isBlank() )
                    adminEmails += ",";
                adminEmails += affiliateAdminUser.getEmail();
            }
            
            if( adminsOnly )
                emailMap.put( EmailUtils.TO, adminEmails );

            else
            {
                emailMap.put(EmailUtils.TO, email!=null ? email : RuntimeConstants.getStringValue("support-email") );            
                emailMap.put( EmailUtils.CC,  adminEmails );
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(  useSupportEmail ? RuntimeConstants.getStringValue("support-email") : RuntimeConstants.getStringValue("no-reply-email") ); // + "|" + MessageFactory.getStringMessage( getLocale(), "g.SupportEmailKey", null ) );
            emailMap.put( EmailUtils.FROM, sb.toString() );
            emailMap.put( EmailUtils.MIME_TYPE, "text/html" );

            boolean sent = emailUtils.sendEmail( emailMap );
            
            if( !sent )
                return;

            if( userActionFacade == null )
                userActionFacade = UserActionFacade.getInstance();

            userActionFacade.saveMessageAction(adminUser, subj + " sent to : " + email + " for OrgId=" + org.getOrgId(), 0, 0, UserActionType.SENT_EMAIL.getUserActionTypeId() );                
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ReminderUtils.sendExpirationEmail() Error sending email " + org.getName() + " (" + org.getOrgId() + ")" );
        }        
    }    
    
    
    public int sendInvitationEmail( TestKey tk )
    {
        return sendReminderOrInvitationEmail( tk, true );
    }
    
    public int  sendReminderEmail( TestKey tk )
    {
        return sendReminderOrInvitationEmail( tk, false );
    }
    
    public int sendReminderOrInvitationEmail( TestKey tk, boolean invitation )
    {
        try
        {
            Tracker.addReminderEmail();
            
            prepareTestKey( tk, true );
            
            if( tk.getUserEmail() == null || tk.getUserEmail().isEmpty() || !EmailUtils.validateEmailNoErrors( tk.getUserEmail() ) )
            {
                LogService.logIt( "ReminderUtils.sendReminderOrInvitationEmail()  Could not send email because no valid email found. " + tk.toString() );
                
                tk.setReminderDays(0);
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                eventFacade.saveTestKey(tk);
                return 0;
            }

            String subj = null;
            String msg = null;
            boolean hasCustomSubj = false;
            boolean hasCustomMsg = false;
            
            String temp;

            // Check for custom reminder messages in Suborg
            if( tk.getSuborg()!=null && tk.getSuborg().getDefaultMessageText()!=null && !tk.getSuborg().getDefaultMessageText().isBlank() )
            {
                temp = invitation ? tk.getSuborg().getTestKeyEmailSubj() : tk.getSuborg().getTestKeyReminderEmailSubj();
                if( temp!=null && !temp.isBlank() )
                    subj =  temp; // tk.getSuborg().getTestKeyEmailSubj();
                
                temp =  invitation ? tk.getSuborg().getTestKeyEmailMsg() : tk.getSuborg().getTestKeyReminderEmail();
                if( temp!=null && !temp.isBlank() )
                    msg = temp; // tk.getSuborg().getTestKeyEmailSubj();                
            }


            
            // Check for Custom Reminder Messages in Org
            if( (subj==null || subj.isBlank()) && tk.getOrg().getDefaultMessageText()!=null && !tk.getOrg().getDefaultMessageText().isBlank() )
            {
                temp = invitation ? tk.getOrg().getTestKeyEmailSubj() : tk.getOrg().getTestKeyReminderEmailSubj();
                if( temp!=null && !temp.isBlank() )
                    subj = temp; // tk.getSuborg().getTestKeyEmailSubj();
            }
            if( (msg==null || msg.isBlank()) && tk.getOrg().getDefaultMessageText()!=null && !tk.getOrg().getDefaultMessageText().isBlank() )
            {
                temp = invitation ? tk.getOrg().getTestKeyEmailMsg() : tk.getOrg().getTestKeyReminderEmail();
                if( temp!=null && !temp.isBlank() )
                    msg = temp; // tk.getSuborg().getTestKeyEmailSubj();
            }

            if( subj!=null && !subj.isBlank() )
                hasCustomSubj=true;

            if( msg!=null && !msg.isBlank() )
                hasCustomMsg=true;
            
            
            // No custom, use Custom Test Key Invitation Messages 
            if( !invitation )
            {
                if( (subj==null || subj.isBlank()) && tk.getSuborg()!=null && tk.getSuborg().getTestKeyEmailSubj()!=null && !tk.getSuborg().getTestKeyEmailSubj().isBlank() )
                    subj = tk.getSuborg().getTestKeyEmailSubj();

                if( (subj==null || subj.isBlank()) && tk.getOrg().getTestKeyEmailSubj()!=null && !tk.getOrg().getTestKeyEmailSubj().isBlank() )
                    subj = tk.getOrg().getTestKeyEmailSubj();


                if( (msg==null || msg.isBlank()) && tk.getSuborg()!=null && tk.getSuborg().getTestKeyEmailMsg()!=null && !tk.getSuborg().getTestKeyEmailMsg().isBlank() )
                    msg = tk.getSuborg().getTestKeyEmailMsg();

                if( (msg==null || msg.isBlank()) && tk.getOrg().getTestKeyEmailMsg()!=null && !tk.getOrg().getTestKeyEmailMsg().isBlank() )
                    msg = tk.getOrg().getTestKeyEmailMsg();
            }            
            
            if( emailUtils == null )
                emailUtils = EmailUtils.getInstance();
            
            // boolean usePlain = msg!=null; //  && msg.indexOf("[PLAIN]")>=0;

            boolean hasHtml = msg != null &&  !msg.isEmpty() ? (msg.indexOf( '<') >=0 && msg.indexOf( '>' )>0 ) : false;

            if( msg==null || msg.isEmpty() || hasHtml  )
            {
                return emailTestKeyHtml(tk, subj, msg, invitation, hasCustomSubj, hasCustomMsg);
                // return 0;
            }

            //if( msg!=null )
            msg=StringUtils.replaceStr(msg, "[PLAIN]", "" );

            if( subj!=null )
                subj = StringUtils.replaceStr(subj, "[PLAIN]", "" );

            msg = convertPlainMessageToHtml(msg);
            return emailTestKeyHtml(tk, subj, msg, invitation, hasCustomSubj, hasCustomMsg);
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "ReminderUtils.sendReminderEmail() " + tk.toString() + ", tk.reminderDays=" + tk.getReminderDays() + ", lastEmailDate=" + tk.getLastEmailDate()  );
            return 0;
        }
    }
    
    
    
    
    
    private String convertPlainMessageToHtml( String msg )
    {
        StringBuilder sb = new StringBuilder();

        String[] lines = msg.split( "\n\r|\r" );

        for( String l : lines )
        {
            l = l.trim();

            if( l.isEmpty() )
                continue;

            sb.append( "<p>" + l + "</p>\n" );
        }

        return sb.toString();
    }


    public Locale getLocaleForTestTaker( TestKey tk ) throws Exception
    {
        if( tk.getUser()!=null && tk.getUser().getLocaleToUseDefaultNull()!=null )
            return tk.getUser().getLocaleToUseDefaultNull();

        boolean useTestLang = false;
        
        if( tk.getLocaleStr()!= null && !tk.getLocaleStr().isEmpty() && tk.getLocaleStr().equals("ttln") )
            useTestLang = true;
        
        else if( tk.getSuborg()!= null && tk.getSuborg().getDefaultTestTakerLang()!= null && tk.getSuborg().getDefaultTestTakerLang().equals("ttln") )
            useTestLang = true;
        
        else if( tk.getOrg()!= null && tk.getOrg().getDefaultTestTakerLang()!= null && tk.getOrg().getDefaultTestTakerLang().equals( "ttln") )
            useTestLang = true;
        
        if( useTestLang )
        {
            if( tk.getProduct()==null )
            {
                if( eventFacade==null )
                    eventFacade = EventFacade.getInstance();
                
                tk.setProduct( eventFacade.getProduct(tk.getProductId()));                
            }
            
            return I18nUtils.getLocaleFromCompositeStr( tk.getProduct().getLangStr() );
        }
        
        else if( tk.getLocaleStr()!= null && !tk.getLocaleStr().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( tk.getLocaleStr() );

        else if( tk.getSuborg()!= null && tk.getSuborg().getDefaultTestTakerLang()!= null && !tk.getSuborg().getDefaultTestTakerLang().isEmpty() && !tk.getSuborg().getDefaultTestTakerLang().equals("brln") )
            return I18nUtils.getLocaleFromCompositeStr( tk.getSuborg().getDefaultTestTakerLang() );

        else if( tk.getSuborg()!= null && tk.getSuborg().getDefaultTestTakerLang()!= null && !tk.getSuborg().getDefaultTestTakerLang().isEmpty() && tk.getSuborg().getDefaultTestTakerLang().equals("brln") )
            return Locale.US;

        else if( tk.getOrg()!= null && tk.getOrg().getDefaultTestTakerLang()!= null && !tk.getOrg().getDefaultTestTakerLang().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( tk.getOrg().getDefaultTestTakerLang() );

        else
            return Locale.US;
    }
    

    
    public int emailTestKeyHtml( TestKey tk, String subj, String msg, boolean invitation, boolean hasCustomSubj, boolean hasCustomMsg) throws Exception
    {
        if( emailBlockFacade==null )
            emailBlockFacade = EmailBlockFacade.getInstance();
        
        if( tk.getUserEmail()==null || tk.getUserEmail().isBlank() )
        {
            tk.setReminderDays(0);
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            eventFacade.saveTestKey(tk);
            return 0;
        }
        
        if( emailBlockFacade.hasEmailBlock(tk.getUserEmail().trim(), false, false ) )
        {
            LogService.logIt("ReminderUtils.emailTestKeyHtml() Email blocked for " + tk.getUserEmail() );
            tk.setReminderDays(0);
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            eventFacade.saveTestKey(tk);
            return 0;
        }
        
        if( tk.getAuthUser()==null && tk.getAuthorizingUserId()>0 )
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            tk.setAuthUser(userFacade.getUser( tk.getAuthorizingUserId()));
        }
        
        if( emailUtils == null )
            emailUtils = EmailUtils.getInstance();

        if( tk.getOrg()==null )
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            tk.setOrg( userFacade.getOrg( tk.getOrgId()));
        }
        
        String ccEmails = null;
        
        if( (invitation && tk.getOrg().getCcOnCandEmails()>0) || tk.getOrg().getCcOnCandEmails()>1 )
        {
            if( tk.getAuthUser()!=null && EmailUtils.validateEmailNoErrors( tk.getAuthUser().getEmail() ))
                ccEmails = tk.getAuthUser().getEmail();
        }
            

        if( tk.getProduct()==null )
        {
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            tk.setProduct( eventFacade.getProduct( tk.getProductId() ));
        }

        if( tk.getSuborgId()>0 && tk.getSuborg()==null )
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            tk.setSuborg( userFacade.getSuborg(tk.getSuborgId()));
        }
        
        Product product = tk.getProduct();
        
        OnlineProctoringType opt = tk.getOnlineProctoringType();

        if( opt.getIsAnyPremium() && tk.getCorp()==null )
        {
            int corpId = tk.getCorpId();
            if( corpId<=0 )
                corpId = tk.getOrg().getDefaultCorpId();
            if( corpId<=0 )
                corpId = RuntimeConstants.getIntValue("defaultcorpid"); 
            if( corpFacade==null )
                corpFacade=CorpFacade.getInstance();
            tk.setCorp( corpFacade.getCorp(corpId) );
        }


        Boolean lockedBrowser = tk.getOnlineProcLockedBrowser();
        // Boolean precheckout = tk!=null ? tk.getOnlineProcPrecheckRqd(): oat.getOnlineProctoringPrecheckoutBoolean();
        
        boolean ppMobileOk = !opt.getIsAnyPremium() ? true :  !StringUtils.getBooleanReportFlag( "ppmobiledevsnotok", tk.getOrg(), null, null, null, tk.getCorp()); //  !getBooleanReportFlag("ppmobiledevsnotok", org, corp );
        boolean ppIosSebOk = !lockedBrowser || !opt.getIsAnyPremium() ? true : !StringUtils.getBooleanReportFlag( "ppiossebnotok", tk.getOrg(), null, null, null, tk.getCorp()); //  !getBooleanReportFlag("ppiossebnotok", org, corp );
        if( ppIosSebOk && lockedBrowser && (opt.getRequiresVideo()) )
            ppIosSebOk = RuntimeConstants.getBooleanValue("ppIsIosOkForSebWithVideo");
        
        boolean desktopOnly = opt.getIsAnyPremium() && ( !ppMobileOk || (lockedBrowser && !ppIosSebOk) );
        // boolean windowsOnly = desktopOnly && lockedBrowser && opt.getVideoCheckoutOk();
        boolean sebWinMacOnly = lockedBrowser && opt.getIsAnyPremium() && !ppIosSebOk;
        boolean sebWinMacIosOnly = lockedBrowser && opt.getIsAnyPremium() && ppIosSebOk;
        boolean imgProc = opt.getIsPremiumAnyImages() || opt.getIsAnyBasic();
        boolean audioProc = opt.getVideoCheckoutOk();
        boolean reqsId = imgProc && ( opt.getIsBasicId() || tk.getProctoringIdCaptureTypeId()>0 );
        boolean keyboard = product.getProductType().getIsSimOrCt5Direct() ? product.getIntParam3()==1 : false;
        
        
        boolean jobSpec = product.getProductType().getIsSimOrCt5Direct() ? product.getConsumerProductType().getIsJobSpecific() : false;
        boolean jobSpecLong =  product.getProductType().getIsAnyBattery() || (jobSpec && product.getIntParam11()==0); // jobSpec && product.getIntParam11()==0;

        /*
        * Sim - 0=no file upload, 
        *      1=Includes general file upload, 
        *      2=recorded video file upload. 
        *      3=recorded audio-only upload, 
        *      4=captured image-only media file upload for proctoring, 
        *      5=captured image only not for proctoring  
        *      6=captured images for proctoring, and audio.         
        */
        int fileUpldCode = product.getProductType().getIsSimOrCt5Direct() ? product.getIntParam6() : 0;
        boolean needsCam = fileUpldCode==2 || fileUpldCode==5;
        boolean needsMicrophone = fileUpldCode==2 || fileUpldCode==3;
        boolean hasRefCheck = tk.getRcScriptId()>0;
        
        boolean usesAi = true;
                
        Locale locale = getLocaleForTestTaker( tk );
        

        if( subj==null || subj.isEmpty()  )
            subj = MessageFactory.getStringMessage( getLocaleForTestTaker( tk ), "g.TestKeyEmailSubj" , null );

        subj = subTestKeyVals(subj, tk, locale, false );
        
        boolean started=!invitation && tk.getTestKeyStatusType().getIsStarted();
        
        if( !invitation )
            subj = (!hasCustomSubj ? MessageFactory.getStringMessage(locale, started ? "g.TestKeyEmailReminderStarted" : "g.TestKeyEmailReminder" ) + " " : "" ) + subj;

        String identifier = tk.getTestKeyId() + "_" + (new Date()).getTime();

        String reminderStopUrl = RuntimeConstants.getStringValue( "adminappbasuri" ) + "/user/stoptkremindersentry.xhtml?t=" + EncryptUtils.urlSafeEncrypt( tk.getTestKeyId() );
        
        boolean useSuborgInfo = tk.getSuborg()!=null && tk.getSuborg().getReportLogoUrl()!=null && !tk.getSuborg().getReportLogoUrl().isBlank();
        
        float mins = product.getFloatParam1();
        if( mins<=0 )
        {
            if( ProductType.getValue(tk.getProductTypeId()).getIsAnyBattery() )
                mins = 30;
            else if( tk.getProduct()!=null && ProductType.getValue(tk.getProductTypeId()).getIsSimOrCt5Direct() && tk.getProduct().getConsumerProductType().getIsJobSpecific() )
                mins = 45;
            else
                mins = 15;
        }
                
        
        String params[] = new String[] { useSuborgInfo ? tk.getSuborg().getReportLogoUrl() : tk.getOrg().getReportLogoUrl(),
                                         useSuborgInfo ? tk.getSuborg().getName() : tk.getOrg().getName(),
                                         RuntimeConstants.getStringValue( "default-email-logo"), // "hraCompanyLogoSmall" ),
                                         tk.getPin(),
                                         Long.toString( tk.getTestKeyId() ),
                                         identifier,
                                         RuntimeConstants.getStringValue("baseprotocol") + "://" +  RuntimeConstants.getStringValue( "baseadmindomain" ),
                                         reminderStopUrl,
                                         Integer.toString((int)mins),
                                         RuntimeConstants.getStringValue("baseadmindomain")
        };
        
        String customHdr = tk.getOrg().getCustomStringValue( Constants.CSVEMAILHEADER );        
        String customFtr = tk.getOrg().getCustomStringValue( Constants.CSVEMAILFOOTER );
                
        StringBuilder sb2 = new StringBuilder();
        
        boolean hideAllSupp = false;
        
        if( msg!=null && !msg.isBlank() )
        {
            hideAllSupp = msg.contains("[HIDEALLMSGS]");
            if( hideAllSupp )
                msg = StringUtils.replaceStr(msg, "[HIDEALLMSGS]", "");

            if( msg.contains("[HIDEAIMSG]") )
            {
                usesAi=false;
                msg = StringUtils.replaceStr(msg, "[HIDEAIMSG]", "");
            }    
            if( msg.contains("[HIDESEBMSG]") )
            {
                sebWinMacOnly=false;
                sebWinMacIosOnly=false;
                msg = StringUtils.replaceStr(msg, "[HIDESEBMSG]", "");
            }    
            if( msg.contains("[HIDEALLPPMSGS]") )
            {
                sebWinMacOnly=false;
                sebWinMacIosOnly=false;
                desktopOnly=false;
                imgProc=false;
                audioProc=false;
                msg = StringUtils.replaceStr(msg, "[HIDEALLPPMSGS]", "");
            }                
        }
        
        
        // Need to check for Battery Time Limit.
        int batteryTimeLimit = 0;
        if( product.getProductType().getIsAnyBattery() )
        {
            Battery battery = tk.getBattery();
            
            if( battery==null )
            {
                battery = tk.getBattery();
                if( tk.getBatteryId()>0 )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    battery = eventFacade.getBattery(tk.getBatteryId());
                    tk.setBattery(battery);
                }
            }
                        
            if( battery==null && product.getIntParam1()>0 )
            {
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                battery = eventFacade.getBattery(product.getIntParam1());
            }
            
            if( battery!=null && battery.getTimeLimitSeconds()>60 )
                batteryTimeLimit = (int) battery.getTimeLimitSeconds()/60;
        }
        
        if( !hideAllSupp )
        {
            // Paragraph, WinMac means video no SEB
            if(  sebWinMacOnly )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7WinMac" , params ) );

            // Paragraph Win means video with SEB
            else if( sebWinMacIosOnly )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7WinMacIos" , params ) );

            // Paragraph - desktop only (Prem Proc) AND camera
            else if( desktopOnly && imgProc )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7PPImg" , params ) );            

            // Paragraph - desktop only (prem proc)
            else if( desktopOnly )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7PP" , params ) );   

            // video
            else if( !desktopOnly && (imgProc || needsCam) && (audioProc || needsMicrophone) )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7Vid" , params ) );

            // images
            else if( !desktopOnly && (imgProc || needsCam) && !audioProc && !needsMicrophone )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7Img" , params) );

            // audio
            else if( !desktopOnly && (audioProc || needsMicrophone) && !imgProc && !needsCam )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7Aud" , params) );

            // none
            else
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml7" , params) );

            if( keyboard )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml4Keyboard" , params ) );

            if( reqsId )                
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml9Id" , null ) );

            // Paragraph
            if( usesAi )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml10AiUse" , params ) );


            // ref check
            if( hasRefCheck )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml6RefCheck" , params ) );

            String timeStr = params[8];
            
            
            if( batteryTimeLimit>0 )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml8XBatteryMins" , new String[]{Integer.toString(batteryTimeLimit)}) );            
            else if( jobSpecLong )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml8XMins" , new String[]{timeStr==null || timeStr.isBlank() ? "45" : timeStr}) );
            else if( jobSpec )
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml8XMins" , new String[]{timeStr==null || timeStr.isBlank() ? "30" : timeStr}) );
            else
                sb2.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml8XMins" , new String[]{timeStr==null || timeStr.isBlank() ? "15" : timeStr}) );
        }
        
        // sb2 is a series of paragraphs <p></p>
        
        // No custom message
        if( msg==null || msg.isEmpty() )
        {
            StringBuilder sb = new StringBuilder();

            // Custom header at minimum should have an opening div. Should usually have the rest in a table inside the div.
            if( customHdr!=null && !customHdr.isBlank() )
                sb.append(customHdr);
            
            else
            {            
                // Div open plus table and first column.
                sb.append( MessageFactory.getStringMessage(locale, invitation ? "g.TestKeyEmailMsgHtml1" : (started ? "g.TestKeyEmailMsgHtml1RemindStarted" : "g.TestKeyEmailMsgHtml1Remind") , params ) );

                // finish table.
                if( params !=null && params[0]!=null && !params[0].isBlank() )
                    sb.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml2Img" , params ) );
                else
                    sb.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml2" , params ) );
            }
            // Table is finished.
            
            // Paragraph  Large Screen
            if(tk.getProduct()!=null && tk.getProduct().getIntParam2()==1)
                sb.append( MessageFactory.getStringMessage(locale, started ? "g.TestKeyEmailMsgHtml3LgScrnStarted" : "g.TestKeyEmailMsgHtml3LgScrn" , params ) );
            
            // Paragraph Small Screen
            else
                sb.append( MessageFactory.getStringMessage(locale, started ? "g.TestKeyEmailMsgHtml3Started" : "g.TestKeyEmailMsgHtml3" , params ) );

            // add series of paragraphs
            if( sb2.length()>0 )
                sb.append( sb2.toString() );
                            
            // Paragraph - Click here to start
            sb.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml4" , params ) );

            // Custom footer must at end with a close div
            if( customFtr!=null && !customFtr.isBlank() )
                sb.append(customFtr + "\n");
            
            // close div
            else
                sb.append( MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtml5" , params ) );

            msg = sb.toString();
            // msg = MessageFactory.getStringMessage(locale, tk.getProduct()!=null && tk.getProduct().getIntParam2()==1 ? "g.TestKeyEmailMsgLargeScrnHtml" :  "g.TestKeyEmailMsgHtml" , params );
        }

        // There IS a message
        else
        {
            if( customHdr!=null && !customHdr.isBlank() )
                msg = customHdr + "\n" + msg;
            
            // if( 1==1 || msg.indexOf( "[URL]" )<0 || msg.indexOf( "[TESTKEY]" )<0 )
            // {
            // Open Table
            String msgSup = MessageFactory.getStringMessage(locale, tk.getProduct()!=null && tk.getProduct().getIntParam2()==1 ? "g.TestKeyEmailMsgLargeScrnSupplementHtml" :  "g.TestKeyEmailMsgSupplementHtml" , null );

            if( sb2.length()>0 )
                msgSup += "<tr><td colspan=\"2\">" + sb2.toString() + "</td></tr>\n";
                            
            // rows and then END Table
            msgSup += MessageFactory.getStringMessage(locale, tk.getProduct()!=null && tk.getProduct().getIntParam2()==1 ? "g.TestKeyEmailMsgLargeScrnSupplementEND" :  "g.TestKeyEmailMsgSupplementHtmlEND" , null );

            msg += msgSup + "\n";
                        
            if( customFtr!=null && !customFtr.isBlank() )
            {                
                msg += customFtr;
            }
            
            else            
                msg += "<br />\n" + MessageFactory.getStringMessage( locale, "g.TestKeyEmailMsgImgOnly" , params );
            // }
        }

        // Add Reminder Stop Link
        if( !invitation )
            msg += MessageFactory.getStringMessage(locale, "g.TestKeyEmailMsgHtmlStop" , params );
        
        msg = subTestKeyVals(msg, tk, locale, true );

        
        String fromAddr = tk.getOrg().getHasCustomSupportSendEmail() ? tk.getOrg().getSupportSendEmail() : RuntimeConstants.getStringValue("no-reply-email");
        
        boolean includeVia = !tk.getOrg().getHasCustomSupportSendEmail();
        

        // first, get plain text string used to describe order.
        Map<String, Object> emailMap = new HashMap<>();

        // emailMap.put( EmailUtils.OVERRIDE_BLOCK, "true" );

        // LogService.logIt("ReminderUtils.emailTestKeyHtml() \n" + msg );

        StringBuilder sb = new StringBuilder();

        sb.append(tk.getUserEmail()==null ? "" : EmailUtils.cleanEmailAddress(tk.getUserEmail()) );
        
        
        // sb.append("|" + EmailUtils.correctNameForSend( tk.getUserFullname() ) );

        emailMap.put( EmailUtils.TO, sb.toString() );

        sb = new StringBuilder();
        sb.append( fromAddr ); // + "|" + MessageFactory.getStringMessage( getLocale(), "g.SupportEmailKey", null ) );
        if( includeVia )
        {
            boolean useAdminName = tk.getAuthUser()!=null && ReportUtils.getReportFlagBooleanValue( "initiatornameemail", null, null, null, tk.getOrg(), null);
            String om = useAdminName ? tk.getAuthUser().getFullname() :  tk.getOrg().getName();
            om = StringUtils.replaceStr(om, "\"", "" );
            om = StringUtils.truncateString(om, 60 );
            
            if( includeVia )
                sb.append( "|" + MessageFactory.getStringMessage(locale, "g.TestInviteOrgName" , new String[]{ om, RuntimeConstants.getStringValue("default-site-name") } ));
            else
                sb.append("|" + tk.getOrg().getName() );
        }
        else
            sb.append("|" + tk.getOrg().getName() );

        if( EmailUtils.isNoReplyAddress(fromAddr ) )
            msg = EmailUtils.addNoReplyMessage(msg, true, locale );
                            
        if( ccEmails!=null && !ccEmails.isBlank() )
            emailMap.put( EmailUtils.CC, ccEmails );
                    
        emailMap.put( EmailUtils.SUBJECT, subj );
        emailMap.put( EmailUtils.CONTENT, msg );
        emailMap.put( EmailUtils.FROM, sb.toString() );

        emailMap.put( EmailUtils.MIME_TYPE, "text/html" );

        boolean sent = emailUtils.sendEmail( emailMap );
        
        if( !sent )
            return 0;

        tk.setLastEmailDate( new Date() );
        tk.setSendStartDate(null);

        if( eventFacade == null )
            eventFacade = EventFacade.getInstance();

        eventFacade.saveTestKey(tk);

        //if( userFacade == null )
        //    userFacade = UserFacade.getInstance();

        User tku = new User();

        tku.setEmail( tk.getUserEmail()  );
        tku.setFirstName( tk.getUserFirstName());
        tku.setLastName( tk.getUserLastName());
        tku.setAltIdentifier( tk.getUserAltId() );
        tku.setUserId( tk.getUserId() );
        tku.setOrgId( tk.getOrgId() );
        tku.setSuborgId( tk.getSuborgId() );

        if( userActionFacade == null )
            userActionFacade = UserActionFacade.getInstance();
        
        userActionFacade.saveMessageAction(tku, subj, tk.getTestKeyId(), tk.getOrgAutoTestId(), UserActionType.SENT_EMAIL.getUserActionTypeId() );
        
        return 1;
    }


    public String  subTestKeyVals( String s, TestKey tk, Locale l, boolean html)
    {
        // [APPLICANT],[CANDIDATE],[EMPLOYEE] - name of test taker or Applicant, Candidate, Employee if no name available.\n\
        // [COMPANY], [DEPARTMENT], [TEST], [TESTKEY]\n\
        // [URL], [EXPIRE]
        boolean b = tk.getUserFullname()==null || tk.getUserFullname().isEmpty();

        String t = b ? MessageFactory.getStringMessage( l , "g.Applicant", null ) : tk.getUserFullname();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[APPLICANT]", t );

        t = b ? MessageFactory.getStringMessage( l , "g.Candidate", null ) : tk.getUserFullname();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[CANDIDATE]", t );

        t = b ? MessageFactory.getStringMessage( l , "g.Employee", null ) : tk.getUserFullname();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[EMPLOYEE]", t );

        t = tk.getOrg().getName();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[COMPANY]", t );

        t = tk.getSuborg()==null ? "" : tk.getSuborg().getName();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[DEPARTMENT]", t );

        t = tk.getProduct().getName();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[TEST]", t );

        t = tk.getPin();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[TESTKEY]", t );

        t = tk.getExtRef() == null ? "" : tk.getExtRef();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[EXTREFERENCE]", t );
                
        t = tk.getUser()==null ? "" : tk.getUser().getAltIdentifier();
        if( html )
            t = StringUtils.replaceStandardEntities(t);
        s = StringUtils.replaceStr(s, "[USERALTIDENTIFIER]", t );
                
        t = tk.getStartUrl();
        s = StringUtils.replaceStr(s, "[URL]", t );

        TimeZone tzToUse = tk.getAuthUser()!=null && tk.getAuthUser().getTimeZoneId()!=null && !tk.getAuthUser().getTimeZoneId().isBlank() ? tk.getAuthUser().getTimeZone() : (tk.getUser()==null || tk.getUser().getTimeZoneId()==null || tk.getUser().getTimeZoneId().isBlank() ? TimeZone.getDefault() : tk.getUser().getTimeZone());
        
        t = I18nUtils.getFormattedDateTime(l, tk.getStartDate(), DateFormat.LONG, DateFormat.LONG, tzToUse );
        s = StringUtils.replaceStr(s, "[STARTDATE]", t );
                
        t = I18nUtils.getFormattedDateTime(l, tk.getExpireDate(), DateFormat.LONG, DateFormat.LONG, tzToUse );
        s = StringUtils.replaceStr(s, "[EXPIRE]", t );

        
        t = tk.getCustom1()==null ? "" : tk.getCustom1();
        s = StringUtils.replaceStr(s, "[CUSTOM1]", t );

        t = tk.getCustom2()==null ? "" : tk.getCustom2();
        s = StringUtils.replaceStr(s, "[CUSTOM2]", t );

        t = tk.getCustom3()==null ? "" : tk.getCustom3();
        s = StringUtils.replaceStr(s, "[CUSTOM3]", t );
        
        return s;

    }


    
    
    
}
