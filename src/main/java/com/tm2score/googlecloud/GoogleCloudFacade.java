package com.tm2score.googlecloud;


import com.tm2score.entity.googlecloud.GoogleTranslateCache;
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
public class GoogleCloudFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


    public static GoogleCloudFacade getInstance()
    {
        try
        {
            return (GoogleCloudFacade) InitialContext.doLookup( "java:module/GoogleCloudFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "GoogleCloudFacade.getInstance() " );

            return null;
        }
    }

    
    /**
     * 
     *
    public List<GoogleTranslateCache> getCacheRecordsForKey( String srcLang, String sourceText ) throws Exception
    {        
        String srcCompress=null;
        
        try
        {
            srcCompress = GoogleTranslateUtils.compressString( sourceText );
            
            if( tm2Factory == null )
                tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery(srcLang==null || srcLang.trim().isEmpty() ? "GoogleTranslateCache.findSrcCompress" : "GoogleTranslateCache.findSrcCompressAndSrcLang",  GoogleTranslateCache.class );

            q.setParameter( "srcCompress", srcCompress );
            
            if( srcLang!=null && !srcLang.trim().isEmpty() )
                q.setParameter( "srcLang", srcLang );
                
            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "GoogleCloudFacade.getCacheRecordsForKey( " + sourceText + " ) srcCompress=" + srcCompress );

            throw new STException( e );
        }
    }
    */

    
    
    
    
    /**
     * 
     */
    public GoogleTranslateCache getCacheRecordForKey( String srcLang, String tgtLang, String srcText ) throws Exception
    {        
        String srcCompress=null;
        
        try
        {
            if( srcLang == null || srcLang.isEmpty() )
                throw new Exception( "SrcLang is null" );
            
            if( tgtLang == null || tgtLang.isEmpty() )
                throw new Exception( "tgtLang is null" );
            
            if( srcText == null || srcText.isEmpty() )
                throw new Exception( "sourceText is null" );
            
            srcCompress = GoogleTranslateUtils.compressString(srcText );
            
            // if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery("GoogleTranslateCache.findSrcCompressAndSrcAndTgtLangs",  GoogleTranslateCache.class );

            q.setParameter( "srcCompress", srcCompress );
            
            q.setParameter( "srcLang", srcLang );
            q.setParameter( "tgtLang", tgtLang );
            // q.setParameter( "srcText", srcText );
           
            return (GoogleTranslateCache) q.getSingleResult();
        }
        
        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "GoogleCloudFacade.getCacheRecordForKey( " + srcText + " ) srcCompress=" + srcCompress );

            throw new STException( e );
        }
    }

    
    public synchronized GoogleTranslateCache saveGoogleTranslateCache( GoogleTranslateCache ir ) throws Exception
    {
        try
        {
            if( ir.getSrcLang()==null || ir.getSrcLang().isEmpty() )
                throw new Exception( "GoogleTranslateCache.srcLang is missing" );

            if( ir.getTgtLang()==null || ir.getTgtLang().isEmpty() )
                throw new Exception( "GoogleTranslateCache.tgtLang is missing" );

            if( ir.getSrcText()==null || ir.getSrcText().isEmpty() )
                throw new Exception( "GoogleTranslateCache.SrcText is missing" );

            if( ir.getSrcCompress()==null || ir.getSrcCompress().trim().isEmpty() )
                ir.setSrcCompress( GoogleTranslateUtils.compressString( ir.getSrcText() ) );

            if( ir.getGoogleTranslateCacheId() <= 0 )
            {
               GoogleTranslateCache gct = getCacheRecordForKey( ir.getSrcLang(), ir.getTgtLang(), ir.getSrcText() );
               
               if( gct!=null )
               {
                   LogService.logIt( "GoogleCloudFacade.saveGoogleTranslateCache() Found existing record: " + gct.toString() + ", for new record: " + ir.toString() );
                   gct.setTgtText( ir.getTgtText() );
                   ir = gct;
               }
            }
            
            ir.setLastUpdate( new Date() );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getGoogleTranslateCacheId() > 0 )
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
            LogService.logIt( e, "GoogleCloudFacade.saveGoogleTranslateCache() " + ir.toString() );

            throw new STException( e );
        }
    }
    
}
