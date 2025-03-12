package com.tm2score.custom.bestjobs;

import com.tm2score.event.*;
import com.tm2score.entity.event.*;
import com.tm2score.entity.purchase.Product;
import com.tm2score.global.STException;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.service.LogService;
import java.util.Collections;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.*;


//@RequestScoped
@Stateless // ( name = "EventFacade", mappedName="EventFacade" )
public class BestJobsReportFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;

    
    public static BestJobsReportFacade getInstance()
    {
        try
        {
            // return (EventFacade) InitialContext.doLookup( "java:global/tm2score2/EventFacade" );
            return (BestJobsReportFacade) InitialContext.doLookup( "java:module/BestJobsReportFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BestJobsReportFacade.getInstance() " );

            return null;
        }
    }



    public List<TestEvent> getMostRecentTestEventsForUserId( long userId, int orgId, int maxRows ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "TestEvent.findRecentByUserIdAndOrgId" );

            q.setParameter( "userId", userId );
            q.setParameter( "orgId", orgId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            q.setMaxResults( maxRows>0 ? maxRows : 20 );
            
            List<TestEvent> tel = q.getResultList();

            q = em.createNamedQuery( "TestEventArchive.findRecentByUserIdAndOrgId" );

            q.setParameter( "userId", userId );
            q.setParameter( "orgId", orgId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            q.setMaxResults( maxRows>0 ? maxRows : 20 );
            
            List<TestEventArchive> tela = q.getResultList();

            for( TestEventArchive tea : tela )
            {
                tel.add( tea.getTestEvent() );
            }

            Collections.sort( tel, new LastAccessDateComparator() );  
            
            Collections.reverse( tel );
            
            if( maxRows>0 && tel.size()>maxRows )
                tel = tel.subList(0, maxRows );
            
            return tel;
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getMostRecentTestEventsForUserId( " + userId + " ) " );

            throw new STException( e );
        }
    }
    
    public TestEvent findMatchingJobSpecificTestEvent( long currentTestEventId, long userId, int orgId ) throws Exception
    {
        List<TestEvent> tel = getMostRecentTestEventsForUserId( userId, orgId, 20 );
        
        if( tel.isEmpty() )
            return null;

        // EventFacade eventFacade = EventFacade.getInstance();
        
        Product p;
        
        for( TestEvent te : tel )
        {
            p = getProduct( te.getProductId() );
            
            if( p==null )
                continue;
            
            if( !p.getProductType().getIsSimOrCt5Direct() )
                continue;
            
            if( p.getConsumerProductTypeId() != ConsumerProductType.ASSESSMENT_JOBSPECIFIC.getConsumerProductTypeId() )
                continue;
            
            if( !te.getTestEventStatusType().getIsCompleteOrHigher() )
                continue;
            
            if( !te.getTestEventStatusType().getIsScoredOrHigher() )
                continue;
            
            if( te.getTestEventId()==currentTestEventId )
                continue;
            
            // Got one!
            te.setProduct(p);            
            
            return te;
            
            //te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), false ) );
            
            //te.setProduct( eventFacade.getProduct( te.getProductId() ));
            
            // get the Sim Descriptor
            //SimDescriptor sd = eventFacade.getSimDescriptor( te.getSimId(), te.getSimVersionId(), false );
            
            //te.setSimDescriptor(sd);
           
            //LogService.logIt( "findMatchingJobSpecificTestEvent() Found a Job Specific Test Event to use: " + te.toString() );
            //return;
        }
        
        return null;
    }
    
    
    public Product getProduct( int productId ) throws Exception
    {
        try
        {
            if( productId <= 0 )
                throw new Exception( "productId is invalid " + productId );

            //if( tm2Factory == null ) tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            // else it's a system type (0 or 1)
            return emmirror.find( Product.class,  productId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getProduct( " + productId + " )" );

            throw new STException( e );
        }
    }
    

}
