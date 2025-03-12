package com.tm2score.corp;

import com.tm2score.entity.corp.Corp;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.UserTransaction;


import jakarta.ejb.Stateless;
import jakarta.persistence.PersistenceContext;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class CorpFacade
{
    // private static EntityManagerFactory tm2Factory;

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    // @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    // EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


    public static CorpFacade getInstance()
    {
        try
        {
            return (CorpFacade) InitialContext.doLookup( "java:module/CorpFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }


    public CorpFacade()
    {}

    public CorpFacade( UserTransaction utx )
    {
    }



    public Corp getCorp( int corpId ) throws Exception
    {
        try
        {

            // if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            return emmirror.find( Corp.class, corpId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CorpFacade.getCorp( " + corpId + " ) " );

            throw new STException( e );
        }
    }
}
