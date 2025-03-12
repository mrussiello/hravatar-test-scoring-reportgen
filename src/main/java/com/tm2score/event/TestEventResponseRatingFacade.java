package com.tm2score.event;

import com.tm2score.entity.event.TestEventResponseRating;
import com.tm2score.service.LogService;
import java.util.List;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class TestEventResponseRatingFacade
{
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;


    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;


    public static TestEventResponseRatingFacade getInstance()
    {
        try
        {
            return (TestEventResponseRatingFacade) InitialContext.doLookup( "java:module/TestEventResponseRatingFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestEventResponseRatingFacade.getInstance() " );

            return null;
        }
    }

    public List<TestEventResponseRating> getTestEventResponseRatingsForTestEventId( long testEventId ) throws Exception
    {
        try
        {
            TypedQuery<TestEventResponseRating> q = em.createNamedQuery( "TestEventResponseRating.findForTestEventId", TestEventResponseRating.class );

            q.setParameter( "testEventId", testEventId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );            
            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestEventResponseRatingFacade.getTestEventResponseRatingsForTestEventId( testEventId=" + testEventId + " )" );
            throw e;
        }
    }
    
}
