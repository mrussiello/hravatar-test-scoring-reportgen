 package com.tm2score.user;

import com.tm2score.entity.user.LimitedAccessLink;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Stateless
public class LimitedAccessLinkFacade
{
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    // @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    // EntityManager emmirror;


    public static LimitedAccessLinkFacade getInstance()
    {
        try
        {
            return (LimitedAccessLinkFacade) InitialContext.doLookup( "java:module/LimitedAccessLinkFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LimitedAccessLinkFacade.getInstance() " );

            return null;
        }
    }

    
    public LimitedAccessLink getLimitedAccessLinkForEmail( String email, long testKeyId ) throws Exception
    {
        if(email==null || email.isEmpty() )
            return null;
              
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "LimitedAccessLink.findByEmailAndTestKeyId" );

            q.setParameter( "email", email );
            q.setParameter( "testKeyId", testKeyId );

            List<LimitedAccessLink> lall = q.getResultList();
            
            LimitedAccessLink lax = null;
            
            for( LimitedAccessLink lal : lall )
            {
                if( lal.getStatusTypeIdBoolean() && lal.getIsExpired() )
                {
                    lal.setStatusTypeIdBoolean(false);
                    saveLimitedAccessLink(lal);
                }
                
                if( !lal.getIsExpired() && lax==null )
                    lax = lal;
            }
            
            return lax;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LimitedAccessLinkFacade.getLimitedAccessLinkForEmail( email=" + email + ", testKeyId=" + testKeyId + " )" );

            throw new STException( e );
        }        
        
        
    }
    
    
    
    
    public LimitedAccessLink getLimitedAccessLink( long limitedAccessLinkId ) throws Exception
    {
        try
        {
            if( limitedAccessLinkId <= 0 )
                throw new Exception( "limitedAccessLinkId is invalid " + limitedAccessLinkId );

            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            // else it's a system type (0 or 1)
            return em.find( LimitedAccessLink.class,  limitedAccessLinkId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LimitedAccessLinkFacade.getLimitedAccessLink( " + limitedAccessLinkId + " )" );

            throw new STException( e );
        }
    }
    
    
    public LimitedAccessLink saveLimitedAccessLink( LimitedAccessLink lal ) throws Exception
    {
        try
        {
            if( lal.getOrgId()<=0 )
                throw new Exception( "OrgId is required." );
            
            if( lal.getTestKeyId()<=0 )
                throw new Exception( "TestKeyId is required." );
            
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );
            
            if( lal.getCreateDate()==null )
                lal.setCreateDate( new Date() );
            
            if( lal.getExpireDate() == null )
            {
                Calendar cal = new GregorianCalendar();
                
                cal.add( Calendar.DAY_OF_MONTH, 10 );
                
                lal.setExpireDate( cal.getTime() );                
            }
            
            else if( lal.getStatusTypeId()>0 && lal.getExpireDate().before( new Date() ) )
                lal.setStatusTypeId(0);

            if( lal.getLimitedAccessLinkId() > 0 )
            {
                em.merge(lal );
            }

            else
            {
                em.detach(lal );

                em.persist(lal );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return lal;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "LimitedAccessLinkFacade.saveLimitedAccessLink() " + lal.toString() );

            throw new STException( e );
        }
    }

    


}
