package com.tm2score.email;

import com.tm2score.entity.email.SmsBlock;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.GooglePhoneUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;


@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class SmsBlockFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;

    public static SmsBlockFacade getInstance()
    {
        try
        {
             return (SmsBlockFacade) InitialContext.doLookup( "java:module/SmsBlockFacade" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SmsBlockFacade.getInstance() " );
            return null;
        }
    }
    

    public SmsBlock getSmsBlock( String phoneNumber ) throws STException
    {
        try
        {
            if( phoneNumber==null || phoneNumber.trim().isEmpty() )
                return null;
            
            phoneNumber = phoneNumber.trim();
            
            String phoneNumberDb = GooglePhoneUtils.cleanPhoneNumberForBlock(phoneNumber);
            if( phoneNumberDb==null || phoneNumberDb.trim().isEmpty() )
                return null;
            
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();
            Query q = emmirror.createNamedQuery( "SmsBlock.findForPhoneNumber" );
            q.setParameter( "phoneNumber", phoneNumberDb );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            return (SmsBlock) q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "getSmsBlock() phoneNumber=" + phoneNumber );
            throw new STException( e );
        }

    }

    
    public SmsBlock getActiveSmsBlock( String phoneNumber ) throws STException
    {
        try
        {
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.MONTH, -1 );
            Date maxDate = cal.getTime();
            
            if( phoneNumber==null || phoneNumber.trim().isEmpty() )
                return null;
            
            phoneNumber = phoneNumber.trim();

            String phoneNumberDb = GooglePhoneUtils.cleanPhoneNumberForBlock(phoneNumber);
            if( phoneNumberDb==null || phoneNumberDb.trim().isEmpty() )
                return null;
            
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();
            Query q = emmirror.createNamedQuery( "SmsBlock.findActiveForPhoneNumber" );
            q.setParameter( "phoneNumber", phoneNumberDb );
            q.setParameter( "maxDate", maxDate );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            return (SmsBlock) q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "SmsBlockFacade.getSmsBlock() phoneNumber=" + phoneNumber );
            throw new STException( e );
        }

    }
    
    public SmsBlock createSmsBlock( String phoneNumber, boolean fullBlock ) throws Exception
    {
        SmsBlock eb = getSmsBlock(phoneNumber);
        
        if( eb==null )
        {
            // do this to avoid issues with duplicates
            Thread.sleep( (long)(Math.random()*3000d) );
                
            eb = getSmsBlock(phoneNumber);
        }
        
        if( eb==null )
        {
            eb = new SmsBlock();            
            eb.setPhoneNumber(GooglePhoneUtils.cleanPhoneNumberForBlock(phoneNumber));
            eb.setPhoneNumberFormatted(phoneNumber);
            eb.setCreateDate(new Date());
            eb.setSmsBlockReasonId(fullBlock ? 1 : 0);
        }
        
        else
        {
            emmirror.detach(eb);
            
            // want full block
            if( fullBlock && eb.getSmsBlockReasonId()!=1 )
                eb.setSmsBlockReasonId(1);

            // want temp block and no block at moment.
            else if( !fullBlock && eb.getSmsBlockReasonId()<0 )
                eb.setSmsBlockReasonId(0);
        }
        
        eb.setLastUpdate(new Date());   
        saveSmsBlock(eb);
        return eb;
    }
    
    public SmsBlock saveSmsBlock( SmsBlock eb ) throws Exception
    {
        try
        {
            eb.setPhoneNumber( GooglePhoneUtils.cleanPhoneNumberForBlock(eb.getPhoneNumber()) );
            
            if( eb.getPhoneNumber()==null || eb.getPhoneNumber().isBlank() )
                throw new Exception( "Phone number is required." + eb.toString() );
            
            if( eb.getCreateDate()==null )
                eb.setCreateDate(new Date());
            
            if( eb.getLastUpdate()==null )
                eb.setLastUpdate(new Date());
            
            // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            // EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );
            
            if( eb.getSmsBlockId()==0 )
                em.persist(eb );

            else
                em.merge(eb );

            em.flush();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "SmsBlockFacade.saveSmsBlock() " + eb.toString() );
            throw new STException( e );
        }

        return eb;
    }
    
    

}
