package com.tm2score.ibmcloud;


import com.tm2score.entity.ibmcloud.IbmInsightResult;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.Date;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class IbmCloudFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    //@PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    //EntityManager emmirror;


    public static IbmCloudFacade getInstance()
    {
        try
        {
            return (IbmCloudFacade) InitialContext.doLookup( "java:module/IbmCloudFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IbmCloudFacade.getInstance() " );

            return null;
        }
    }


    
    public IbmInsightResult getIbmInsightResultForTestEventId( long testEventId ) throws Exception
    {        
        try
        {
            if( testEventId <=0 )
                throw new Exception( "testEventId invalid: " + testEventId );
                        
            // if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery("IbmInsightResult.findByTestEventId",  IbmInsightResult.class );

            q.setParameter( "testEventId", testEventId );
           
            return (IbmInsightResult) q.getSingleResult();
        }
        
        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "IbmCloudFacade.getIbmInsightResultForTestEventId( " + testEventId + " )" );

            throw new STException( e );
        }
    }
    
    
    
    public IbmInsightResult saveIbmInsightResult( IbmInsightResult ir ) throws Exception
    {
        try
        {
            if( ir.getTestEventId()<=0 )
                throw new Exception( "IbmInsightResult.testEventId is invalid " + ir.getTestEventId() );
            
            if( ir.getResultJson()==null || ir.getResultJson().isEmpty() )
                throw new Exception( "IbmInsightResult.resultJson is missing" );
            
            ir.setLastUpdate( new Date() );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getIbmInsightResultId() > 0 )
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
            LogService.logIt( e, "IbmCloudFacade.saveIbmInsightResult() " + ir.toString() );

            throw new STException( e );
        }
    }


    
    public IbmInsightResult getIbmInsightResult( long ibmInsightResultId ) throws Exception
    {
        //if( tm2Factory == null )
        //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

        //EntityManager em = tm2Factory.createEntityManager();

        try
        {
            return em.find(IbmInsightResult.class, ibmInsightResultId );
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "IbmCloudFacade.getIbmInsightResult() ibmInsightResultId=" + ibmInsightResultId );

            throw e;
        }        
    }
    

    
}
