/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.proctor;


import com.tm2score.entity.event.TestEventArchive;
import com.tm2score.entity.proctor.ProctorEntry;
import com.tm2score.entity.proctor.ProctorSuspension;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.proctor.SuspiciousActivity;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

/**
 *
 * @author Mike
 */
@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class ProctorFacade
{

    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;

    public static ProctorFacade getInstance()
    {
        try
        {
            return (ProctorFacade) InitialContext.doLookup( "java:module/ProctorFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProctorFacade.getInstance() " );
            return null;
        }
    }

    
    public RemoteProctorEvent getRemoteProctorEventForTestEventId( long testEventId ) throws Exception
    {
        if( testEventId<=0 )
            return null;
        try
        {
            TypedQuery<RemoteProctorEvent> q = em.createNamedQuery( "RemoteProctorEvent.findByTestEventId", RemoteProctorEvent.class );
            q.setParameter( "testEventId", testEventId );            
            return q.getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getRemoteProctorEventForTestEventId( testEventId=" + testEventId + " )" );
            return null;
        }
    }
    
    
    
    public List<SuspiciousActivity> getSuspiciousActivityForTestEventId( long testKeyId, long testEventId) throws Exception
    {
        try
        {
            if( testKeyId<=0 && testEventId<=0 )
            {
                //LogService.logIt("EventFacade.getSuspiciousActivityForTestEventId( testKeyId=" + testKeyId + ", testEventId=" + testEventId + " ) AAA returning empty list" );
                return new ArrayList<>();
            }
            
            if( testKeyId<=0 )
            {
                LogService.logIt("EventFacade.getSuspiciousActivityForTestEventId( testKeyId=" + testKeyId + ", testEventId=" + testEventId + " ) Warning TestKeyId invalid at " + testKeyId );
            }
            

            //if( testEventId<=0 && testKeyId==RuntimeConstants.getLongValue( "SampleReportImgCaptureTestKeyId" )  )
            //{
            //    LogService.logIt("EventFacade.getSuspiciousActivityForTestEventId( testKeyId=" + testKeyId + ", testEventId=" + testEventId + " ) Sample Report TestKeyid so returning empty set." );
            //    return new ArrayList<>();
            //}

            //if( testKeyId<=0 || testEventId<=0 )
            //{
            //    LogService.logIt("EventFacade.getSuspiciousActivityForTestEventId( testKeyId=" + testKeyId + ", testEventId=" + testEventId + " ) Warning. Either testkeyid or testeventid is 0" );
            //}
            
            TypedQuery<SuspiciousActivity> q = em.createNamedQuery( testEventId<=0 ? "SuspiciousActivity.findByTestKeyIdAndNoTestEventId" : "SuspiciousActivity.findByTestEventIdOrTestKeyId", SuspiciousActivity.class );
            
            q.setParameter( "testEventId", testEventId );
            q.setParameter( "testKeyId", testKeyId );

            return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.getSuspiciousActivityForTestEventId( testKeyId=" + testKeyId + ", testEventId=" + testEventId + " )" );
            throw e;
            // return null;
        }
    }
    
    
    public RemoteProctorEvent saveRemoteProctorEvent( RemoteProctorEvent ir ) throws Exception
    {
        try
        {
            // LogService.logIt( "EventFacade.savePercentile() " + ir.toString() );

            if( ir.getTestEventId()<=0  )
                throw new Exception( "RemoteProctorEvent.testEventId must be non-zero." );

            Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getRemoteProctorEventId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                // em.detach( ir );
                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            // em.flush();

            return ir;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProctorFacade.saveRemoteProctorEvent() " + ir.toString() );
            throw new STException( e );
        }
    }

    
    public SuspiciousActivity saveSuspiciousActivity( SuspiciousActivity ir ) throws Exception
    {
        try
        {
            // LogService.logIt( "EventFacade.savePercentile() " + ir.toString() );

            if( ir.getTestEventId()<=0  )
                throw new Exception( "RemoteProctorEvent.testEventId must be non-zero." );

            if( ir.getCreateDate()==null )
                ir.setCreateDate( new Date() );
            
            if( ir.getLastUpdate()==null )
                ir.setLastUpdate( new Date() );
            
            Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getSuspiciousActivityId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                // em.detach( ir );
                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            // em.flush();

            return ir;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProctorFacade.saveSuspiciousActivity() " + ir.toString() );
            throw new STException( e );
        }
    }

    
    public List<TestEventArchive> getTestEventArchiveListForSameOrgProductIp( long notTestEventId, long notUserId, int orgId, int productId, String ipAddress, Date minLastAccessDate, Date maxLastAccessDate ) throws Exception
    {       
        try
        {
            // LogService.logIt( "ProctorFacade.getTestEventArchiveListForSameOrgProductIp() notTestEventId=" + notTestEventId );

            if( ipAddress==null || ipAddress.isBlank() )
                return new ArrayList<>();
            
            Query q = emmirror.createNamedQuery( "TestEventArchive.findByNotUserNotEventIdOrgIdProductIdIpAddressDate" );

            q.setParameter( "notTestEventId", notTestEventId );
            q.setParameter( "notUserId", notUserId );
            q.setParameter( "orgId", orgId );
            q.setParameter( "productId", productId );
            q.setParameter( "ipAddress", ipAddress );
            q.setParameter( "minLastAccessDate", minLastAccessDate );
            q.setParameter( "maxLastAccessDate", maxLastAccessDate );

            q.setMaxResults( 10 );
            return (List<TestEventArchive>) q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProctorFacade.getTestEventArchiveListForSameOrgProductIp( notTestEventId" + notTestEventId + ", notUserId=" + notUserId + ", orgId=" + orgId + ", productId=" + productId + ", ipAddress=" + ipAddress + " ) " );
            throw new STException( e );
        }        
    }

    public List<ProctorEntry> getProctorEntryListForTestKey(long testKeyId) throws Exception {
        
        try {
            TypedQuery q = em.createNamedQuery("ProctorEntry.findByTestKeyId", ProctorEntry.class);
            q.setParameter("testKeyId", testKeyId);
            q.setHint("jakarta.persistence.cache.retrieveMode", "BYPASS");
            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LogService.logIt(e, "ProctorFacade.getProctorEntryListForTestKey( testKeyId=" + testKeyId + " )");
            return null;
        }
    }

    public List<ProctorSuspension> getProctorSuspensionListForTestKey(long testKeyId ) throws Exception {
        try {
            TypedQuery q = em.createNamedQuery("ProctorSuspension.findByTestKeyId", ProctorSuspension.class);
            q.setParameter("testKeyId", testKeyId);
            q.setHint("jakarta.persistence.cache.retrieveMode", "BYPASS");
            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            LogService.logIt(e, "ProctorFacade.getProctorSuspensionListForTestKey( testKeyId=" + testKeyId + " )");
            return null;
        }
    }
    
    
    
    
}
