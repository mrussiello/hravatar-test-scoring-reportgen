/*
 * Created on Jan 1, 2007
 *
 */
package com.tm2score.profile;

import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.profile.ProfileEntry;
import com.tm2score.entity.profile.ProfileProductMap;
import com.tm2score.global.STException;
import com.tm2score.onet.OnetJobZoneType;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;


import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import javax.sql.DataSource;

//// @ManagedBean
//@Named
@Stateless
public class ProfileFacade
{
    @EJB
    UserFacade userFacade;

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    //private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;



    public static ProfileFacade getInstance()
    {
        try
        {
            return (ProfileFacade) InitialContext.doLookup( "java:module/ProfileFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }


    public Profile getProfile( int profileId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            return emmirror.find( Profile.class, profileId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getProfile() " );

            throw new STException( e );
        }
    }

    
    public List<Profile> getMatchingProfileListForProfileUsageTypeIdAndStrParam3( int profileUsageTypeId, String strParam3, OnetJobZoneType minOnetJobZoneType, OnetJobZoneType maxOnetJobZoneType ) throws Exception
    {
        try
        {
            if( minOnetJobZoneType==null )
                minOnetJobZoneType = OnetJobZoneType.ZONE1;
            if( maxOnetJobZoneType==null )
                maxOnetJobZoneType = OnetJobZoneType.ZONE5;
            
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "Profile.findByProfileUsageTypeIdAndStrParam3AndMinMaxJobZones" );
            
            q.setParameter( "profileUsageTypeId", profileUsageTypeId );
            q.setParameter( "strParam3", strParam3 );
            q.setParameter("minJobZoneTypeId", minOnetJobZoneType.getJobZoneId() );
            q.setParameter("maxJobZoneTypeId", maxOnetJobZoneType.getJobZoneId() );

            return q.getResultList();
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "getMatchingProfileListForProfileUsageTypeIdAndStrParam3( profileUsageTypeId=" + profileUsageTypeId + ", strParam3=" + strParam3 + " )" );

            throw new STException( e );
        }
    }

    public List<ProfileEntry> getProfileEntryList( int profileId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "ProfileEntry.findByProfileId" );

            q.setParameter( "profileId", profileId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getProfileEntryList( profileId=" + profileId + " )" );

            throw new STException( e );
        }
    }

    private List<ProfileProductMap> getProfileProductMapListForProductIdAndOrgId( int productId, int orgId) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "ProfileProductMap.findByOrgIdAndProductId" );

            q.setParameter("productId", productId );

            q.setParameter( "orgId", orgId );

            // q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "getProfileProductMapListForProductIdAndOrgId( productId=" + productId + ", orgId=" + orgId + " )" );

            throw new STException( e );
        }
    }

    
    private List<Profile> getAllOrgProfileListForProductIdAndProfileUsageTypeId( int productId, int profileUsageTypeId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT DISTINCT p.profileid FROM profile AS p INNER JOIN profileproductmap AS ppm ON p.profileid=ppm.profileid WHERE p.applyforallorgs=1 AND p.profileusagetypeid=" + profileUsageTypeId + " AND ppm.productid=" + productId;
        
        List<Profile> out = new ArrayList<>();
        
        try (Connection con = pool.getConnection(); 
             Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             ResultSet rs = stmt.executeQuery( sqlStr );

             while( rs.next() )
             {
                 out.add( getProfile( rs.getInt( 1 ) ) );
             }

             rs.close();
             
             return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProfileFacade.getAllOrgProfileListForProductIdAndProfileUsageTypeId() " + sqlStr );

            throw new STException( e );
        }
    }
    
    
    
    
    public List<Profile> getProfileListForProductIdAndOrgIdAndProfileUsageType( int productId , int orgId, int profileUsageTypeId  ) throws Exception
    {
        try
        {            
            List<Profile> out = new ArrayList<>();
            
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            List<ProfileProductMap> ppml = this.getProfileProductMapListForProductIdAndOrgId(productId, orgId);
                        
            Profile p;
            
            for( ProfileProductMap ppm : ppml )
            {
                p = getProfile( ppm.getProfileId() );
                
                if( p==null )
                    continue;
                
                if( p.getProfileUsageTypeId()!=profileUsageTypeId )
                    continue;
                
                if( !p.getProfileStatusType().isActive() )
                    continue;

                out.add( p );
            }
            
            List<Profile> aopl = getAllOrgProfileListForProductIdAndProfileUsageTypeId(productId, profileUsageTypeId );

            for( Profile pp : aopl )
            {
                if( out.contains( pp ) )
                    continue;

                //if( pp.getProfileUsageTypeId()!=profileUsageTypeId )
                //    continue;
                
                if( !pp.getProfileStatusType().isActive() )
                    continue;

                
                out.add(pp);
            }
            
            
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "getProfileListForProductIdAndOrgIdAndProfileUsageType( productId=" + productId + ", orgId=" + orgId + ", profileUsageTypeId=" + profileUsageTypeId + " )" );

            throw new STException( e );
        }        
    }
    
    
    /*
    public Set<Profile> getProfileListForProfileUsageTypeIdProductId( int profileUsageTypeId, int productId ) throws Exception
    {
        try
        {            
            Set<Profile> out = new HashSet<>();
            
            if( tm2Factory == null )
                tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "ProfileProductMap.findByProductId" );

            q.setParameter("productId", productId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<ProfileProductMap> ppml = (List<ProfileProductMap>) q.getResultList();
            
            Profile p;
            
            for( ProfileProductMap ppm : ppml )
            {
                p = getProfile( ppm.getProfileId() );
                
                if( p==null )
                    continue;
                
                if( p.getProfileUsageTypeId()!=profileUsageTypeId )
                    continue;
                
                if( !p.getProfileStatusType().isActive() )
                    continue;
                
                out.add( p );
            }
            
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "getProfileProductMapListForProductId( productId=" + productId + " )" );

            throw new STException( e );
        }
    }
    */



    public void loadProfile( Profile p ) throws Exception
    {
        p.setProfileEntryList( getProfileEntryList( p.getProfileId() ));
    }



}
