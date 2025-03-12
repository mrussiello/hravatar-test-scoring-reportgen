package com.tm2score.av;

import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.List;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class AvEventFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    // @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    // EntityManager emmirror;
    


    public static AvEventFacade getInstance()
    {
        try
        {
            return (AvEventFacade) InitialContext.doLookup( "java:module/AvEventFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AvEventFacade.getInstance() " );

            return null;
        }
    }

    


    public List<AvItemResponse> getAvItemResponsesForTestEventId( long testEventId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery("AvItemResponse.findByTestEventId",  AvItemResponse.class );

            q.setParameter( "testEventId", testEventId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "AvEventFacade.getAvItemResponsesForTestEventId( " + testEventId + " ) " );

            throw new STException( e );
        }
    }


    
    public AvItemResponse saveAvItemResponse( AvItemResponse ir ) throws Exception
    {
        try
        {
            if( ir.getTestEventId()<=0 )
                throw new Exception( "AvItemResponse.testEventId must be non-zero." );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getAvItemResponseId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );

                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            // em.flush();

            return ir;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "AvEventFacade.saveAvItemResponse() " + ir.toString() );

            throw new STException( e );
        }
    }
    
    
    public AvItemResponse getAvItemResponse( long avItemResponseId, boolean refresh) throws Exception
    {
        try
        {
            if( avItemResponseId <= 0 )
                return null;

            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();
            
            if( refresh )
            {
                Query q = em.createNamedQuery("AvItemResponse.findByAvItemResponseId",  AvItemResponse.class ).setParameter( "avItemResponseId", avItemResponseId ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
                return (AvItemResponse) q.getSingleResult();                
            }
            return em.find(AvItemResponse.class,  avItemResponseId );
        }

        catch( NoResultException e )
        {
            return null;
        }
    }
    
    
    
}
