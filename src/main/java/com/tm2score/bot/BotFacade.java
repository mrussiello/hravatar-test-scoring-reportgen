/*
 * Created on Jan 1, 2007
 *
 */
package com.tm2score.bot;



import com.tm2score.entity.bot.BotInstance;
import com.tm2score.entity.bot.BotIntent;
import com.tm2score.entity.event.BotEvent;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import javax.sql.DataSource;

//// @ManagedBean
//@Named
@Stateless
public class BotFacade
{

    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


    public static BotFacade getInstance()
    {
        try
        {
            return (BotFacade) InitialContext.doLookup( "java:module/BotFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.getInstance() " );

            return null;
        }
    }


    // @Resource
//    protected UserTransaction utx;




    public BotInstance getBotInstance( int botInstanceId ) throws Exception
    {
        try
        {
            TypedQuery<BotInstance> q = emmirror.createNamedQuery( "BotInstance.findByBotInstanceId", BotInstance.class ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            q.setParameter( "botInstanceId", botInstanceId );

            BotInstance bi = q.getSingleResult();

            // BotInstance bi = (BotInstance) em.find( BotInstance.class, botInstanceId );
            
            if( bi==null )
                return null;
            
            bi.setBotIntentList( getBotIntentsForBotInstanceId(botInstanceId));
            
            return bi;
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.getBotInstance() " );

            throw new STException( e );
        }
    }
    
    public BotIntent getBotIntent( int botIntentId ) throws Exception
    {
        try
        {
            TypedQuery<BotIntent> q = emmirror.createNamedQuery( "BotIntent.findByBotIntentId", BotIntent.class ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            q.setParameter( "botIntentId", botIntentId );

            return q.getSingleResult();
            
            // return em.find( BotIntent.class, botIntentId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.getBotIntent() " );

            throw new STException( e );
        }
    }
    
    
    public List<BotIntent> getBotIntentsForBotInstanceId( int botInstanceId ) throws Exception
    {
        try
        {
            TypedQuery<BotIntent> q = emmirror.createNamedQuery( "BotIntent.findByBotInstanceId", BotIntent.class );

            q.setParameter( "botInstanceId", botInstanceId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            
            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.getBotIntentsForBotInstanceId( botInstanceId=" + botInstanceId + " )" );

            return new ArrayList<>();
        }
    }
    
      

    public BotEvent getBotEvent( long botEventId ) throws Exception
    {
        try
        {
            return (BotEvent) em.find( BotEvent.class, botEventId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.getBotEvent() " );
            throw new STException( e );
        }
    }



    public BotEvent findBotEvent( long testEventId, String intnUniqueId, int intnItemSeq ) throws Exception
    {
        try
        {
            TypedQuery<BotEvent> q = em.createNamedQuery( "BotEvent.findByTestEventIdAndIntn", BotEvent.class );

            q.setParameter( "testEventId", testEventId );
            q.setParameter( "intnUniqueId", intnUniqueId );
            q.setParameter( "intnItemSeq", intnItemSeq );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            
            return (BotEvent) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.findBotEvent( testEventId=" + testEventId + ", intnUniqueId=" + intnUniqueId + ", intnItemSeq=" + intnItemSeq + " )" );

            return null;
        }
        
    }
    

    public BotEvent saveBotEvent( BotEvent botEvent ) throws Exception
    {
        try
        {
            if( botEvent.getBotInstanceId()<=0 )
                throw new Exception( "BotInstanceId invalid: " + botEvent.getBotInstanceId() );
            
            if( botEvent.getUserId()<=0 )
                throw new Exception( "UserId invalid: " + botEvent.getUserId() );
            
            if( botEvent.getOrgId()<=0 )
                throw new Exception( "OrgId invalid: " + botEvent.getOrgId() );
                        
            if( botEvent.getStartDate()==null )
                botEvent.setStartDate( new Date() );

            botEvent.setLastUpdate( new Date() );

            // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            // EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            // utx.begin();

            if( botEvent.getBotEventId() > 0 )
            {
                em.merge( botEvent );
            }

            else
            {
                em.persist( botEvent );
            }

            em.flush();
            // utx.commit();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.saveBotEvent() " + botEvent.toString() );

            throw new STException( e );
        }

        return botEvent;
    }



    public Set<Integer> getReferencedBotInstanceIdList( int botInstanceId )  throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "SELECT bi.intparam3 FROM botintent AS bi WHERE bi.botinstanceid=" + botInstanceId + " AND bi.actioncodetypeid=1 AND bi.intparam3>0"; //  = "SELECT ct3simid FROM ct3sim WHERE ct3simtypeid=" + ct3Sim.getCt3SimTypeId() + " onetsoc='" + ct3Sim.getOnetSoc() + "' AND identifier='" + ct3Sim.getIdentifier() + "' AND localestr='en_US' AND (affiliateid IS NULL OR affiliateid='') ";

        Set<Integer> out = new HashSet<>();
        
        Set<Integer> tt = new HashSet<>();
        
        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             ResultSet rs = stmt.executeQuery(sql);
             
             while( rs.next() )
             {
                tt.add( rs.getInt(1) ) ;
             }
             
             rs.close();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BotFacade.getReferencedBotInstanceIdList() " + sql );

            throw new STException( e );
        } 
        
        for( Integer biid : tt )
        {
            if( out.contains( biid ) )
                continue;
            
            out.add( biid );
            
            out.addAll( getReferencedBotInstanceIdList( biid ) );
        }
        
        return out;        
    }
    
    public List<BotInstance> getReferencedBotInstances( int botInstanceId ) throws Exception
    {
        Set<Integer> idl = getReferencedBotInstanceIdList(botInstanceId);
        
        List<BotInstance> out = new ArrayList<>();
        
        for( Integer id : idl )
        {
            out.add( getBotInstance(id) );
        }
        
        Collections.sort( out );
        
        return out;
    }
    
    
    
    
    
    
    
}
