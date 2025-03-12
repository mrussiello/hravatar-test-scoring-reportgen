/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.user;

import com.tm2score.entity.user.User;
import com.tm2score.entity.user.UserAction;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import java.util.Date;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 *
 * @author Mike
 */
@Stateless
public class UserActionFacade {

    private static EntityManagerFactory tm2Factory;


    public static UserActionFacade getInstance()
    {
        try
        {
            return (UserActionFacade) InitialContext.doLookup( "java:module/UserActionFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserActionFacade.getInstance() " );

            return null;
        }
    }


    public void saveMessageAction( User user, String subject, long testKeyId, int orgAutoTestId, int userActionTypeId)
    {
        try
        {
            if( userActionTypeId<=0 )
                throw new Exception( "UserActionTypeId invalid: " + userActionTypeId );

            if( user == null )
                throw new Exception( "User is null" );

           //  if( user.getEmail()==null || user.getEmail().isEmpty() || !EmailUtils.validateEmailNoErrors( user.getEmail()))
           //      throw new Exception( "Cannot send invalid email: " + user.toString() );

            UserAction ua = new UserAction();

            ua.setCreateDate( new Date());

            ua.setUserId( user.getUserId() );
            ua.setOrgId( user.getOrgId() );
            ua.setLongParam1(testKeyId);
            ua.setLongParam2( orgAutoTestId );

            if( user.getUserId() <= 0 )
            {
                ua.setUserId(RuntimeConstants.getLongValue( "defaultMarketingAccountAnonymousUserId" ));
                ua.setOrgId( RuntimeConstants.getIntValue( "defaultMarketingAccountOrgId" ) );
            }

            ua.setIpCity( user.getIpCity());
            ua.setIpCountry( user.getIpCountry());
            ua.setIpState( user.getIpState() );
            ua.setStrParam3(subject);
            ua.setStrParam4( user.getEmail());
            ua.setStrParam5(user.getFullname());
            ua.setStrParam6( user.getMobilePhone() );

            ua.setUserActionTypeId(userActionTypeId);

            saveUserActionRecord( ua );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "UserActionFacade.saveUserAction() NONFATAL " + ( user == null ? "null" : user.toString()) + ", subject=" + subject + ", userActionTypeId=" + userActionTypeId );

            // throw new Exception( "UserActionFacade.saveUserAction() " + userAction.toString() + " " + e.toString() );
        }
    }

    public UserAction saveUserActionRecord( UserAction ua ) throws Exception
    {
        try
        {
            if( ua.getUserId()<=0 )
                throw new Exception( "UserAction.userId is required" );

            if( ua.getCreateDate()==null )
                ua.setCreateDate( new Date() );

            Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ua.getUserActionId() > 0 )
                em.merge(ua );

            else
                em.persist(ua );

            // em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "UserActionFacade.saveUserAction() NONFATAL " + ua.toString() );

            // throw new Exception( "UserActionFacade.saveUserAction() " + userAction.toString() + " " + e.toString() );
        }

        return ua;
    }




}
