/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.user;

import com.tm2score.affiliate.AffiliateAccountType;
import com.tm2score.entity.user.Country;
import com.tm2score.entity.user.LogonHistory;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.OrgAutoTest;
import com.tm2score.entity.user.Suborg;
import com.tm2score.entity.user.User;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/**
 *
 * @author Mike
 */
@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class UserFacade
{
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;
    
    @PersistenceContext( name = "persistence/discern", unitName = "discern" )
    EntityManager discern;
    
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    
    // private static EntityManagerFactory discernFactory;


    public static UserFacade getInstance()
    {
        try
        {
            return (UserFacade) InitialContext.doLookup( "java:module/UserFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }

    public void clearSharedCache()
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();
            em.getEntityManagerFactory().getCache().evictAll();
            emmirror.getEntityManagerFactory().getCache().evictAll();
            discern.getEntityManagerFactory().getCache().evictAll();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.clearSharedCache() " );
        }
    }

    public void clearSharedCacheDiscern()
    {
        try
        {
            // if( discernFactory==null )
            //     discernFactory=DiscernPersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = discernFactory.createEntityManager();
            discern.getEntityManagerFactory().getCache().evictAll();
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.clearSharedCache() " );
        }
    }
    
    

        
    public Date getLastLogonDate( long userId, long logonHistoryId ) throws Exception
    {
        String sqlStr = "SELECT MAX(logondate) FROM logonhistory WHERE userid=" + userId + ( logonHistoryId>0 ? " AND logonhistoryid<>" + logonHistoryId : "" ) + " AND systemid=" + RuntimeConstants.getIntValue( "applicationSystemId" );

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
            
            ResultSet rs = stmt.executeQuery( sqlStr );

            Date d = null;

            Timestamp ts;

            if( rs.next() )
            {
                ts = rs.getTimestamp(1);

                if( ts != null )
                    d = new Date( ts.getTime() );
                else
                    d = null;
            }

            rs.close();
            return d;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getLastLogonDate() " + sqlStr );

            throw new STException( e );
        }
    }
    

    
    
    public void addUserLogout( long logonHistoryId, int logoffTypeId ) throws Exception
    {

        try
        {
            // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            // EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );
            
            LogonHistory logonHistory = em.find( LogonHistory.class, logonHistoryId );

            if( logonHistory != null )
            {
                logonHistory.setLogoffDate( new Date() );

                logonHistory.setLogoffTypeId( logoffTypeId );

                // utx.begin();

                try
                {
	                em.merge( logonHistory );

	                em.flush();

	                // utx.commit();
                }

                catch( Exception e )
                {
                    // if( utx.isActive() )
	                // utx.rollback();

                    throw e;
                }
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "addUserLogout( logoutHistoryId=" + logonHistoryId + "  ) " );
        }
    }
    
    
    
    public LogonHistory addLogonHistory( User user, int logonTypeId, String userAgent, String ipAddress) throws Exception
    {
        //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

        //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );


        // EntityTransaction utx = em.getTransaction();

        try
        {
            LogonHistory lh = new LogonHistory();

            lh.setUserId( user.getUserId() );

            lh.setLogonDate( new Date() );

            lh.setLogonTypeId( logonTypeId );

            lh.setLogonHistoryId( 0 );

            lh.setOrgId( user.getOrgId() );

            lh.setSuborgId( user.getSuborgId() );

            lh.setSystemId( RuntimeConstants.getIntValue( "applicationSystemId" ) );
            
            lh.setUserAgent( userAgent );
            
            lh.setIpAddress( ipAddress );

            // utx.begin();

            em.persist(lh );

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.

            em.flush();

            // utx.commit();

            return lh;
        }

        catch( Exception e )
        {

            // if( utx.isActive() )
                // utx.rollback();

            LogService.logIt(e, "addLogonHistory() " + ( user == null ? "User is null" : user.toString() ) );

            throw new Exception( "addLogonHistory() " + ( user == null ? "User is null" : user.toString() ) + " " + e.toString() );
        }

    }
    
    
    
    /*
    public void updateIpLocationData( User user , String ipAddress ) throws Exception
    {
        if( ipAddress == null )
            return;

        String[] ipData = getIPLocationData( ipAddress );

        user.setIpCountry( ipData[0] );
        user.setIpState( ipData[1] );
        user.setIpCity( ipData[2] );

        if( user.getIpCountry()!=null && !user.getIpCountry().isEmpty() )
        {
             Country ctry = getCountryByCode( user.getIpCountry() );

             if( ctry != null )
                 user.setGeographicRegionId( ctry.getGeographicRegionId() );

             if( ctry != null )
                 user.setCountryCode( ctry.getCountryCode() );
        }

        if( user.getUserId()> 0 )
            saveUser( user );
    }
    */

    public User saveUser( User user ) throws Exception
    {
        //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
        //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

        try
        {
            if( user.getLocaleStr() == null || user.getLocaleStr().isEmpty() )
                user.setLocaleStr( "en_US" );

            if( user.getUserId() > 0 )
            {
                em.merge( user );
            }

            else
            {
                em.detach( user );
                em.persist( user );
            }

            // em.flush();

            // utx.commit();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.saveUser() " + user.toString() );

            throw new STException( e );
        }

        return user;
    }


    public Org saveOrg(Org org) throws Exception
    {
        //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
        //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

        try
        {
            if( org.getOrgId()>0 )
            {
                em.merge( org );
            }
            else
            {
                em.detach( org );
                em.persist( org );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.saveOrg() " + org.toString() );
            throw new STException( e );
        }
        return org;
    }


    public Country getCountryByCode( String cc ) throws Exception
    {
        try
        {
            if( cc == null || cc.length() == 0 )
                cc = "US";

            //if( tm2Factory == null )
             //   tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "Country.findByCode" );

            q.setParameter( "cc", cc );

            return (Country) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }
    }











    public User getUserByEmailAndOrgId( String email, int orgId ) throws Exception
    {
        try
        {
            if( email == null || email.length() == 0 )
                return null;

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();
            // EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "User.findUserByEmailAndOrgId" );

            q.setParameter( "uemail", email );
            q.setParameter( "orgId", orgId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (User) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }
    }



    public Org getSourceOrgForAffiliateId( String affiliateId ) throws Exception
    {
        try
        {
            if( affiliateId == null || affiliateId.isEmpty() )
                return null;

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();
            
            TypedQuery q = emmirror.createNamedQuery( "Org.findByAffiliateIdAndAffiliateAccountTypeId", Org.class );

            q.setParameter( "affiliateId", affiliateId );
            q.setParameter( "affiliateAccountTypeId", 1 );

            return (Org) q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getSourceOrgForAffiliateId() affiliateId=" + affiliateId );

            return null;
        }
    }
    
    

    public Org getOrg( int orgId ) throws Exception
    {
        try
        {
            if( orgId <= 0 )
                return null;

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();
            return em.find( Org.class,  orgId );
        }

        catch( NoResultException e )
        {
            return null;
        }
    }


    public Suborg getSuborg( int suborgId ) throws Exception
    {
        try
        {
            if( suborgId <= 0 )
                return null;

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            return em.find( Suborg.class,  suborgId );
        }

        catch( NoResultException e )
        {
            return null;
        }
    }


    /**
     * Returns null if none found.
     */
    public User getUserByUsername( String username ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            TypedQuery<User> q = em.createNamedQuery( "User.findByUsername", User.class );

            q.setParameter( "uname", username );

            return q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }
    }




    public boolean checkPassword( long userId, String password ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        try (Connection con = pool.getConnection() )
        {
            if( password == null || password.length() < 1 )
                return false;

            // password is invalid
            if( !password.equals( StringUtils.sanitizeStringFull( password ) ) )
                return false;

            if( userId <= 0 )
                return false;

            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            password = StringUtils.sanitizeForSqlQuery( password );

            PreparedStatement ps = con.prepareStatement( "SELECT username FROM xuser WHERE zpass IS NOT NULL AND zpass=SHA2( ?, 224 ) AND userid=?" );
            ps.setString( 1, password );
            ps.setLong( 2, userId );

            ResultSet rs = ps.executeQuery();

            boolean recordFound = false;

            if( rs.next() )
                recordFound = true;

            rs.close();
            ps.close();
            
            // check for OLD password storage method.
            if( !recordFound )
            {
                ps = con.prepareStatement( "SELECT username FROM xuser WHERE xpass IS NOT NULL AND xpass=MD5( ? ) AND userid=?" );
                ps.setString( 1, password );
                ps.setLong( 2, userId );            
                rs = ps.executeQuery();
                if( rs.next() )
                    recordFound = true;
                rs.close();
                ps.close();   
                
                // if it matched on old password storage, change to new password storage.
                if( recordFound )
                {
                    LogService.logIt( "UserFacade.checkPassword() Converting User to new password storage. userId=" + userId );
                    ps = con.prepareStatement( "UPDATE xuser SET xpass3=null,xpass2=null,xpass1=null,xpass=null,zpass=SHA2( ?, 224 ) WHERE userid=?" );
                    ps.setString( 1, password );
                    ps.setLong( 2, userId );
                    ps.executeUpdate();                    
                }
                
            }

            return recordFound;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.checkPassword() userId=" + userId );

            throw new STException( e );
        }
    }



    /**
     * Returns null if either this username (or email) is not found or the password is invalid.
     *
     */
    public User getUserByLogonInfo( String username, String password ) throws Exception
    {
        try
        {

            // both fields are required.
            if( username == null || username.length() == 0 || password == null || password.length() == 0 )
                return null;

            // first look for username
            User user = getUserByUsername( username );

            // if this is a superuser password
            //if( password != null )
            //{
                // see if username is a userId number
            //    if( user == null )
            //    {
            //        try
            //        {
            //            user = getUser( new Long( username ) );
            //        }

            //        catch( NumberFormatException e )
            //        {}
            //    }

                // return without testing password
            //    return user;
            //}

            // not found?
            if( user == null )
                return null;

            if( !checkPassword( user.getUserId(), password ) )
                return null;

            // found!
            return user;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getUserByLogonInfo( " + username + ", password=" + password + " ) " );

            return null;
        }
    }




    // Changed to always use the latest without cache because people update!
    public User getUser( long userId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "User.findByUserId" );

            q.setParameter( "userId", userId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (User) q.getSingleResult();

            //return em.find(User.class, userId );
        //}
        }

        catch( NoResultException e )
        {
            return null;
        }


        catch( Exception e )
        {
            LogService.logIt( e, "getUser( " + userId + " ) " );

            throw new STException( e );
        }
    }
    
    public Org getAffiliateSourceAccount( String affiliateId ) throws Exception
    {
        try
        {
            if( affiliateId==null || affiliateId.isBlank() )
                return null;

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "Org.findByAffiliateIdAndAffiliateAccountTypeId" );

            q.setParameter( "affiliateId", affiliateId );

            q.setParameter( "affiliateAccountTypeId", AffiliateAccountType.SOURCE.getAffiliateAccountTypeId() );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<Org> ol = q.getResultList();

            if( ol.isEmpty() )
                return null;

            return ol.get(0);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getAffiliateSourceAccount( " + affiliateId + " ) " );

            throw new STException( e );
        }
    }
    
    public List<User> getAdminUsersForOrgId( int orgId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();
            // EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "User.findByMinRoleAndOrgId" );
            q.setParameter( "orgId", orgId );
            q.setParameter( "roleId", RoleType.ACCOUNT_LEVEL3 );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return  q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "UserFacade.getAdminUsersForOrgId()" );
            return new ArrayList<>();
        }
    }
    
    public OrgAutoTest getOrgAutoTest( int orgAutoTestId ) throws Exception
    {
        try
        {
            if( orgAutoTestId <= 0 )
                return null;

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();
            // EntityManager em = tm2Factory.createEntityManager();

            TypedQuery<OrgAutoTest> qq = emmirror.createNamedQuery( "OrgAutoTest.getByOrgAutoTestId", OrgAutoTest.class );
            qq.setParameter( "orgAutoTestId", orgAutoTestId );
            qq.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (OrgAutoTest) qq.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.getOrgAutoTest() orgAutoTestId=" + orgAutoTestId );
            return null;
        }
    }
    
    
    

}
