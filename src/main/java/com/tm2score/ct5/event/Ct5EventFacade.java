package com.tm2score.ct5.event;

import com.tm2score.entity.ct5.event.Ct5ItemResponse;
import com.tm2score.entity.ct5.event.Ct5TestEvent;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class Ct5EventFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    //@PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    //EntityManager emmirror;



    public static Ct5EventFacade getInstance()
    {
        try
        {
            return (Ct5EventFacade) InitialContext.doLookup( "java:module/Ct5EventFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct5EventFacade.getInstance() " );

            return null;
        }
    }
        
    
    public Ct5TestEvent getCt5TestEventForTestEventIdAndSurveyEventId( long testEventId, long surveyEventId) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();
            //if( refresh )
            return (Ct5TestEvent) em.createNamedQuery( "Ct5TestEvent.findByTestEventIdAndSurveyEventId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("testEventId", testEventId ).setParameter("surveyEventId", surveyEventId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "Ct5EventFacade.getCt5TestEventForTestEventId( " + testEventId + " ) " );
            throw new STException( e );
        }
    }   
    
    
    public List<Ct5ItemResponse> getCt5ItemResponsesForCt5Test( long ct5TestEventId  ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();
            
            Query q = em.createNamedQuery( "Ct5ItemResponse.findByCt5TestEventId" );

            q.setParameter( "ct5TestEventId", ct5TestEventId );

            return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "Ct5EventFacade.getCt5ItemResponsesForCt5Test() ct5TestEventId=" + ct5TestEventId );
            throw new STException(e);
        }
    }
    
    
    public Ct5TestEvent saveCt5TestEvent( Ct5TestEvent t ) throws Exception
    {
        try
        {
            if( t.getTestEventId()<=0 )
                throw new Exception( "testEventId is required. " );

            if( t.getOrgId()<=0 )
                throw new Exception( "OrgId is required. " );

            if( t.getUserId()<=0 )
                throw new Exception( "UserId is required. " );

            if( t.getProductId()<=0 )
                throw new Exception( "ProductId is required. " );

            if( t.getTestKeyToken()<=0 )
                throw new Exception( "TestKeyToken is required. " );
            
            if( t.getCreateDate()==null )
                t.setCreateDate(new Date());
                        
            t.setLastUpdate( new Date() );
            
            if( t.getCt5TestEventId()<=0 )
            {
                Ct5TestEvent t2 = this.getCt5TestEventForTestEventIdAndSurveyEventId(t.getTestEventId(), t.getSurveyEventId());
                if( t2!=null )
                {
                    throw new Exception( "Ct5EventFacade.saveCt5TestEvent() found existing Ct5TestEvent: " + t2.toString() );                    
                }
            }
            
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( t.getCt5TestEventId()>0 )
                em.merge(t);
            else
            {
                em.detach(t);
                em.persist(t);
            }
            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct5EventFacade.saveCt5TestEvent() " + t.toString() );
            throw new Exception( "Ct5EventFacade.saveCt5TestEvent() " + t.toString() + " " + e.toString() );
        }

        return t;
    }    

}
