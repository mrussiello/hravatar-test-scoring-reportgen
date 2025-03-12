/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.email;

import com.tm2score.entity.email.EmailBlock;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.Date;

/**
 *
 * @author Mike
 */
@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class EmailBlockFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


    public static EmailBlockFacade getInstance()
    {
        try
        {
            return (EmailBlockFacade) InitialContext.doLookup( "java:module/EmailBlockFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EmailBlockFacade.getInstance() " );

            return null;
        }
    }



    public boolean hasEmailBlock( String email, boolean fullBlock, boolean treatBouncesComplaintsAsFullBlock) throws Exception
    {
        try
        {
            if( email==null || email.trim().isEmpty() )
                return false;
            
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );


            if( treatBouncesComplaintsAsFullBlock && !fullBlock )
                treatBouncesComplaintsAsFullBlock = false;
                        
            Query q = em.createNamedQuery( treatBouncesComplaintsAsFullBlock ? "EmailBlock.findFullBlockOrBounceOrComplainForEmail" : (fullBlock ? "EmailBlock.findFullBlockForEmail" : "EmailBlock.findForEmail") );

            q.setParameter( "email", email );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            EmailBlock eb = (EmailBlock) q.getSingleResult();
            if( eb!=null && eb.getExpireDate()!=null && eb.getExpireDate().before(new Date()) )
            {
                em.remove( eb );
                em.flush();
                eb=null;                
            }            
            
            return eb != null;
        }

        catch( NoResultException e )
        {
            return false;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "hasEmailBlock() email=" + email );

            throw new STException( e );
        }

    }
    
}
