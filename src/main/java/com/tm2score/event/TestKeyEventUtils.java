/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.event;


import com.tm2score.affiliate.AffiliateAccountType;
import com.tm2score.email.EmailBlockFacade;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.purchase.Credit;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.OrgAutoTest;
import com.tm2score.entity.user.User;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.purchase.CreditType;
import com.tm2score.purchase.PurchaseFacade;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.EmailerFacade;
import com.tm2score.service.LogService;
import com.tm2score.user.OrgCreditUsageType;
import com.tm2score.user.UserActionFacade;
import com.tm2score.user.UserActionType;
import com.tm2score.user.UserFacade;
import com.tm2score.util.MessageFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class TestKeyEventUtils {


    private UserFacade userFacade;
    private EventFacade eventFacade ;
    private PurchaseFacade purchaseFacade;
    private UserActionFacade userActionFacade;

    

    public void updateOrgCreditUsageEventCount( Org o, TestKey tk)
    {
        if( tk.getOrgCreditUsageCounted() || !tk.getTestKeyStatusType().getIsCompleteOrHigher() || tk.getCreditId()>0 )
            return;

        LogService.logIt( "TestKeyEventUtils.updateOrgCreditUsageEventCount() START testKeyId=" + tk.getTestKeyId() + ", tk.creditId=" + tk.getCreditId() + ", tk.ceditIndex=" + tk.getCreditIndex() );
        
        try
        {
            if( o==null )
                o = tk.getOrg();

            if( o==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                o=userFacade.getOrg( tk.getOrgId() );
                tk.setOrg(o);
            }

            if( !OrgCreditUsageType.getValue( o.getOrgCreditUsageTypeId() ).getAnyResultCredit() )
                return;

            if( purchaseFacade==null )
                purchaseFacade = PurchaseFacade.getInstance();

            Credit c = null;
            long[] creditInfo = purchaseFacade.findRcCreditIdToUseForTesting(tk.getOrgId(), tk.getUserId(), Constants.MAX_DAYS_PREV_RCCHECK, CreditType.RESULT.getCreditTypeId()  );
            boolean usingPrevCredit = false;

            if( creditInfo[0]>0 )
            {
                c = purchaseFacade.getCredit(creditInfo[0]);
                usingPrevCredit = true;
                TestEventLogUtils.createTestKeyLogEntry( tk.getTestKeyId(), 0, 2, "SCORE.TestEventUtils.updateOrgCreditUsageEventCount() Noted Active Result Credit from Previous TestKey, RcCheck, or Lvi. creditid-index: " + creditInfo[0] + "-" + creditInfo[1], null, null );                
            }

            else
            {
                // this method needs to be synchronized.
                //synchronized (tk) {
                creditInfo = chargeCredit(tk);
                if( creditInfo[0]>0 )
                {
                    c = purchaseFacade.getCredit(creditInfo[0]);
                }
                // c = purchaseFacade.chargeCredit( tk, null, tk.getOrgId() , tk.getOrg()==null ? 0 : tk.getOrg().getOrgIdToUseForCredits(), 1, CreditType.RESULT.getCreditTypeId() );
                //}
                //creditInfo[0] = c.getCreditId();
                //creditInfo[1] = c.getInitialCount()-c.getRemainingCount();
            }

            if( c!=null )
            {
                boolean sav = tk.getCreditId()<=0 && creditInfo[0]>0;

                tk.setCreditId( creditInfo[0] );
                tk.setCreditIndex( (int)creditInfo[1] );
                tk.setOrgCreditUsageCounted();

                if( sav )
                {
                    // always save right away.
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    tk=eventFacade.saveTestKey(tk);
                }
            }
            else
            {
                tk.setCreditId(-1);
                tk.setCreditIndex( 0 );
                tk.setOrgCreditUsageCounted();
                // always save right away.
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                tk=eventFacade.saveTestKey(tk);
                TestEventLogUtils.createTestKeyLogEntry( tk.getTestKeyId(), 0, 0, "SCORE.TestKeyEventUtils.updateOrgCreditUsageEventCount() UNABLE to CHARGE RESULT CREDIT. No available credits found.", null, null );
            }

            // if we are using a previously charged credit, do not send any credits notifications.
            if( usingPrevCredit )
                return;

            int creditBalance = purchaseFacade.getTotalRemainingCredits(tk.getOrgId() , tk.getOrg()==null ? 0 : tk.getOrg().getOrgIdToUseForCredits(), 0, CreditType.RESULT.getCreditTypeId() );

            // LogService.logIt( "TestKeyEventUtils.updateOrgCreditUsageEventCount() creditBalance=" + creditBalance + ", testKeyId=" + tk.getTestKeyId() );

            if( creditBalance==0 || creditBalance==RuntimeConstants.getIntValue( "LowARThreshold" ) )
            {
                //if( userFacade==null )
                //    userFacade = UserFacade.getInstance();
                //User orgAdmin = userFacade.getUser(o.getAdminUserId() >0 ? o.getAdminUserId() : tk.getAuthorizingUserId() );
                // Locale orgAdminLocale = orgAdmin.getLocaleStr()==null || orgAdmin.getLocaleStr().isBlank() ? Locale.US : I18nUtils.getLocaleFromCompositeStr( orgAdmin.getLocaleStr() );

                if( tk.getProduct()==null )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    tk.setProduct( eventFacade.getProduct( tk.getProductId() ));
                }

                performCreditsNotification(o.getOrgId(),
                        tk.getProduct(),
                        creditBalance,
                        tk );
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "TestKeyEventUtils.updateOrgCreditUsageEventCount() testKeyId=" + tk.getTestKeyId() );
        }
    }


    
    private void performCreditsNotification( int orgId, Product product, int balance, TestKey testKey) throws Exception
    {
         try
         {
             if( orgId <= 0 )
                 throw new Exception( "orgId invalid" );

            // Locale locale = getLocale();

            if( userFacade == null )
                userFacade = UserFacade.getInstance();

            EmailerFacade emailerFacade = EmailerFacade.getInstance();

            Org org = userFacade.getOrg(orgId);

            if( org == null )
                throw new Exception( "org not found " + orgId );

            // LogService.logIt( "TestKeyEventUtils.performCreditsNotification() AAA balance=" + balance + ", tkId=" + (testKey==null ? "null" : testKey.getTestKeyId() ) + ", org=" + org.getName() + " (" + org.getOrgId() + "), org.AdminUserId=" + org.getAdminUserId() );
            
            if( org.getAdminUserId() <= 0 )
                return;
            
            User adminUser = userFacade.getUser( org.getAdminUserId() );

            User affiliateSrcOrgAdminUser = null;

            Org affiliateSourceOrg = null;
            
            if( org.getAffiliateId()!=null && !org.getAffiliateId().isBlank() && org.getAffiliateAccountTypeId()!=AffiliateAccountType.SOURCE.getAffiliateAccountTypeId() )
            {
                affiliateSourceOrg = userFacade.getAffiliateSourceAccount( org.getAffiliateId() );

                if( affiliateSourceOrg == null )
                    LogService.logIt( "TestKeyEventUtils.performCreditsNotification() ERROR Cannot find Affiliate SourceAccount for affiliateId=" + org.getAffiliateId() );

                else
                {
                    if( affiliateSourceOrg.getAdminUserId()>0 )
                        affiliateSrcOrgAdminUser = userFacade.getUser(affiliateSourceOrg.getAdminUserId() );

                    else
                    {
                        List<User> admins = userFacade.getAdminUsersForOrgId(affiliateSourceOrg.getOrgId() );

                        if( admins != null && !admins.isEmpty() )
                            affiliateSrcOrgAdminUser = admins.get(0);
                    }
                }
            }
            
            String extraEmail = null;
            
            if( affiliateSourceOrg!=null && affiliateSourceOrg.getAdminUserId() >0  )
            {
                // affiliateSrcOrgAdminUser = userFacade.getUser(affiliateSourceOrg.getAdminUserId() );

                if( affiliateSrcOrgAdminUser != null )
                    extraEmail = affiliateSrcOrgAdminUser.getEmail();
                
                if( RuntimeConstants.getStringValue( "AdditionalAffiliateSourceEmails_OrgId_" + affiliateSourceOrg.getOrgId() )!=null && 
                    !RuntimeConstants.getStringValue( "AdditionalAffiliateSourceEmails_OrgId_" + affiliateSourceOrg.getOrgId() ).isBlank())
                {
                    if( extraEmail==null )
                        extraEmail="";
                    
                    extraEmail=extraEmail.trim();
                    
                    if( !extraEmail.isBlank() )
                        extraEmail+=",";
                    
                    extraEmail += RuntimeConstants.getStringValue( "AdditionalAffiliateSourceEmails_OrgId_" + affiliateSourceOrg.getOrgId() );
                }
            }

            if( adminUser == null )
            {
                List<User> admins = userFacade.getAdminUsersForOrgId(orgId);

                if( admins != null && !admins.isEmpty() )
                    adminUser = admins.get(0);
            }
            
            User authUser = null;
            if( testKey.getAuthorizingUserId()> 0 )
            {                
                if( userFacade == null )
                    userFacade = UserFacade.getInstance();
                authUser = userFacade.getUser( testKey.getAuthorizingUserId() );
            }  

            if( authUser==null && testKey.getOrgAutoTestId()>0 )
            {
                
                // EventFacade eventFacade = EventFacade.getInstance();
                OrgAutoTest oat = userFacade.getOrgAutoTest( testKey.getOrgAutoTestId() );
                
                if( oat != null )
                {
                    if( oat.getAuthUserId()> 0 )
                    {
                        if( userFacade == null )
                            userFacade = UserFacade.getInstance();
                        authUser = userFacade.getUser( oat.getAuthUserId() );                        
                    }
                }
            }
            
            String testTaker = "";
            
            if( testKey.getUser()!=null )
                testTaker = testKey.getUser().getFullname() + ", " + testKey.getUser().getEmail() + " (" + testKey.getUser().getUserId() + ") ";

            else if( testKey.getTempLastName()!=null && !testKey.getTempLastName().isBlank()  )
            {
                testTaker = testKey.getTempLastName();
                
                if( testKey.getTempFirstName()!=null && !testKey.getTempFirstName().isBlank() )
                    testTaker += ", " + testKey.getTempFirstName();
                
                if( testKey.getTempEmail()!=null && !testKey.getTempEmail().isBlank() )
                    testTaker += ", " + testKey.getTempEmail();
            }                
            
            Locale loc;
            Map<String, Object> emailMap = new HashMap<>();

            String[] params = new String[10];

            // boolean affiliateIsAdmin = false;

            if( adminUser == null )
            {
                String m = "TestKeyEventUtils.performCreditsNotification() ERROR Admin user not found for org: " + org.toString() + ", adminUserId=" + org.getAdminUserId();

                if( affiliateSrcOrgAdminUser==null )
                    throw new Exception( m );

                else
                {
                    // affiliateIsAdmin = true;
                    adminUser = affiliateSrcOrgAdminUser;
                    LogService.logIt( m );
                }
            }
            
            loc = adminUser.getLocaleToUseDefaultNull();

            if( loc==null && authUser!=null )
                loc = authUser.getLocaleToUseDefaultUS();
            
            if( loc==null )
                loc = Locale.US;

            if( !EmailUtils.validateEmailNoErrors( adminUser.getEmail() ) )
            {
                LogService.logIt( "TestKeyEventUtils.performCreditsNotification() BBB Error. Email is invalid: " + adminUser.getEmail() );
                return;
            }

            EmailBlockFacade emailBlockFacade = EmailBlockFacade.getInstance();
            if( emailBlockFacade.hasEmailBlock(adminUser.getEmail().trim(), false, false ) )
            {
                LogService.logIt( "TestKeyEventUtils.performCreditsNotification() Email blocked for " + adminUser.getEmail() );
                return;
            }
            
            
            params[0] = adminUser.getFullname();
            params[1] = Integer.toString( balance );
            params[2] = org.getName() + " (" + org.getOrgId() + ") ";
            
            params[3] = "(" + Long.toString( testKey.getTestKeyId() ) + ")";

            params[4] = authUser==null ? "" : (authUser.getFullname() + " (" + authUser.getUserId() + ")" );
            params[5] = testTaker == null ? "" : testTaker;
            params[6] = product==null ? (testKey.getProduct()==null ? "Id=" + testKey.getProductId() : testKey.getProduct().getName() + "(" + testKey.getProduct().getProductId() + ")" ) : product.getName()  + "(" + product.getProductId() + ")";
            params[7] =  affiliateSourceOrg==null ? "" : affiliateSourceOrg.getAffiliateId();
            
            params[8] = affiliateSrcOrgAdminUser==null ? "" : affiliateSrcOrgAdminUser.getFullname() + ", " + affiliateSrcOrgAdminUser.getEmail();
            
            OrgCreditUsageType orgCreditUsageType = OrgCreditUsageType.getValue(org.getOrgCreditUsageTypeId() );
                        
            // boolean isResultCredit = orgCreditUsageType.getAnyResultCredit();
            // boolean isUnlimited =    orgCreditUsageType.getUnlimited();         

            String subjKey = null;
            String contentKey = null;
            
            //if( isUnlimited )
            //{
                // Indicated a test was denied
            //    if( balance<0 )
            //    {
            //        subjKey = "g.TestDeniedUnlimSubj";
            //        contentKey = "g.TestDeniedUnlimContent";
            //    }                
            //}
            
            //if( isResultCredit )
            //{
                // Indicated a test was denied
                //if( balance<0 )
                //{
                //    subjKey = "g.TestDeniedPkgSubj";
                //    contentKey = "g.TestDeniedPkgContent";
                //}
                
                // Indicated no more ARs
                if( balance == 0 )
                {
                    subjKey = "g.NoCreditsMsgPkgSubj";

                    //if( creditBalance<=0 )
                    contentKey = "g.NoCreditsMsgPkgContent";
                    //else if( creditBalance < 120 )
                    //    contentKey = "g.NoCreditsMsgPkgHasCredsContent";
                    //else
                    //    contentKey = "g.NoCreditsMsgPkgHasManyCredsContent";
                }
                
                // LOW ARs
                else
                {
                    subjKey = "g.LowCreditsMsgPkgSubj";
                    contentKey = "g.LowCreditsMsgPkgContent";                    
                }
                
            //}
            
            // legacy credits
            //else
            //{
             //   subjKey = balance==0 ? "g.NoCreditsMsgSubj" : "g.LowCreditsMsgSubj";
            //    contentKey = balance==0 ? "g.NoCreditsMsgContent" :  "g.LowCreditsMsgContent" ;
            //}
            
            String affiliateTxt = affiliateSourceOrg==null ? "" : ("\n" + MessageFactory.getStringMessage( loc , "g.CreditsAffiliateC" , params));
            
            String subj = MessageFactory.getStringMessage( loc , subjKey , params);
            
            emailMap.put( EmailUtils.SUBJECT, subj  );

            emailMap.put( EmailUtils.CONTENT, MessageFactory.getStringMessage( loc , contentKey, params) + affiliateTxt );

            // StringBuilder sb = new StringBuilder();

            emailMap.put( EmailUtils.TO, adminUser.getEmail() );

            emailMap.put( EmailUtils.FROM, RuntimeConstants.getStringValue("support-email")  );

            
            // emailMap.put(EmailConstants.BCC, RuntimeConstants.getStringValue( "lowCreditsBccEmails" ) + ( affiliateIsAdmin || affiliateSrcOrgAdminUser==null ? "" : "," + affiliateSrcOrgAdminUser.getEmail() ) );
            emailMap.put(EmailUtils.BCC, RuntimeConstants.getStringValue( "lowCreditsBccEmails" ) + ( extraEmail==null ? "" : "," + extraEmail ) );

            boolean sent = emailerFacade.sendEmail( emailMap );
            
            if( !sent )
                return;
            
            if( userActionFacade==null)
                userActionFacade = UserActionFacade.getInstance();

            userActionFacade.saveMessageAction(adminUser, subj, testKey.getTestKeyId(), testKey.getOrgAutoTestId(), UserActionType.SENT_EMAIL.getUserActionTypeId());
         }

         catch( Exception e )
         {
            LogService.logIt(e, "TestKeyEventUtils.performNoCreditsNotification() orgId=" + orgId + ", product=" + (product==null ? "null" : product.getProductId()) + ", balance=" + balance );
         }
    }
    
    
    
    
    
    private long[] chargeCredit( TestKey tk ) throws Exception
    {
        if( tk.getCreditId()>0 && tk.getCreditIndex()>0 )
            return new long[]{tk.getCreditId(),tk.getCreditIndex()};

        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();
        //if( purchaseFacade==null )
        //    purchaseFacade=PurchaseFacade.getInstance();

        long[] cinfo;

        // LogService.logIt( "TestKeyEventUtils.chargeCredit() AAA testKeyId=" + tk.getTestKeyId() + ", current creditId=" + tk.getCreditId() + " , current creditIndex=" + tk.getCreditIndex() +", date=" + d.toString() );
        //synchronized(d)
        //{
            // check that there is no existing credit charged for this test key.
            cinfo = eventFacade.checkCreditIdForTestKey( tk.getTestKeyId() );

            // found a credit / index in TestKey already. Update but no need to save
            if( cinfo[0]>0 )
            {
                tk.setCreditId(cinfo[0]);
                tk.setCreditIndex( (int) cinfo[1]);
                tk.setOrgCreditUsageCounted();
                LogService.logIt( "SCORE.TestKeyEventUtils.chargeCredit() BBB Found charged credit in DBMS testKeyId=" + tk.getTestKeyId() + ", creditId=" + tk.getCreditId() + " , creditIndex=" + tk.getCreditIndex() );
                return cinfo;
            }

            // LogService.logIt( "TestKeyEventUtils.chargeCredit() BBB testKeyId=" + tk.getTestKeyId() );
            if( purchaseFacade==null )
                purchaseFacade=PurchaseFacade.getInstance();

            cinfo = purchaseFacade.chargeCredit( tk, null, tk.getOrgId() , tk.getOrg()==null ? 0 : tk.getOrg().getOrgIdToUseForCredits(), 1, CreditType.RESULT.getCreditTypeId() );
            if( cinfo!=null )
            {
                tk.setCreditId(cinfo[0]);
                tk.setCreditIndex((int) cinfo[1]);
                tk.setOrgCreditUsageCounted();
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                tk=eventFacade.saveTestKey(tk);
                LogService.logIt( "TestKeyEventUtils.chargeCredit() CCC Charged new Credit. testKeyId=" + tk.getTestKeyId() + ", new creditId=" + tk.getCreditId() + " , creditIndex=" + tk.getCreditIndex() );
                TestEventLogUtils.createTestKeyLogEntry( tk.getTestKeyId(), 0, 2, "SCORE.TestKeyEventUtils.chargeCredit() Charged new Result Credit. creditid-index: " + tk.getCreditId() + "-" + tk.getCreditIndex(), null, null );
            }            

            // LogService.logIt( "TestKeyEventUtils.chargeCredit() DDD testKeyId=" + tk.getTestKeyId() + ", new creditId=" + tk.getCreditId() + " , " + tk.getCreditIndex() +", date=" + d.toString() );
        //}

        return cinfo==null ? new long[2] : cinfo;
    }

}
