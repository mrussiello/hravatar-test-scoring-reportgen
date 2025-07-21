/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.file;

import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 *
 * @author Mike
 */
@Stateless
public class FileUploadFacade
{
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;


    public static FileUploadFacade getInstance()
    {
        try
        {
            return (FileUploadFacade) InitialContext.doLookup( "java:module/FileUploadFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileUploadFacade.getInstance() " );

            return null;
        }
    }

    
    public List<UploadedUserFile> getUploadedUserFilesForTestKey( long testKeyId, int uploadedUserFileTypeId, long maxTestEventId) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( maxTestEventId>0 ? "UploadedUserFile.findByTestKeyIdAndTypeWithMaxTestEventId" : "UploadedUserFile.findTestKeyIdAndType",  UploadedUserFile.class );

            q.setParameter( "testKeyId", testKeyId );
            q.setParameter( "uploadedUserFileTypeId", uploadedUserFileTypeId );
            if( maxTestEventId>0 )
                q.setParameter("maxTestEventId", maxTestEventId);

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "FileUploadFacade.getUploadedUserFilesForTestKey( " + testKeyId + " ) " );

            throw new STException( e );
        }        
    }
    
    
    public List<UploadedUserFile> getUploadedUserFilesForTestEvent( long testEventId, int uploadedUserFileTypeId ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( uploadedUserFileTypeId<0 ? "UploadedUserFile.findTestEventId" : "UploadedUserFile.findTestEventIdAndType",  UploadedUserFile.class );

            q.setParameter( "testEventId", testEventId );
            if( uploadedUserFileTypeId>=0 )
                q.setParameter( "uploadedUserFileTypeId", uploadedUserFileTypeId );
                
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "FileUploadFacade.getAvItemResponsesForTestEventId( " + testEventId + " ) " );

            throw new STException( e );
        }        
    }
    

    public UploadedUserFile getUploadedUserFile( long uploadedUserFileId, boolean refresh ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            if( refresh )
                return (UploadedUserFile) em.createNamedQuery( "UploadedUserFile.findByUploadedUserFileId", UploadedUserFile.class ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "uploadedUserFileId", uploadedUserFileId ).getSingleResult();

            return em.find( UploadedUserFile.class,  uploadedUserFileId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileUploadFacade.getUploadedUserFile( " + uploadedUserFileId + ", " + refresh + " ) " );

            throw new STException( e );
        }
    }



    public UploadedUserFile saveUploadedUserFile( UploadedUserFile uuf ) throws Exception
    {
        //  utx.begin();

        try
        {
            // 	LogService.logIt( "FileUploadFacade. saving UploadedUserFile "  );

            if( uuf.getTestEventId() == 0 )
                throw new Exception( "testEventId=0" );

            if( uuf.getActId() == 0 )
                throw new Exception( "actId=0" );

            if( uuf.getNodeSeq() == 0 )
                throw new Exception( "nodeSeq=0" );

            if( uuf.getSubnodeSeq() == 0 )
                throw new Exception( "subnodeSeq=0" );

            //if( uuf.getFilename() == null || uuf.getFilename().isEmpty() )
            //    throw new Exception( "filename is missing" );


            if( uuf.getCreateDate() == null )
                uuf.setCreateDate( new Date() );

            if( uuf.getLastUpload() == null )
                uuf.setLastUpload( new Date() );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );
                        
            if( uuf.getUploadedUserFileId() > 0 )
                em.merge( uuf );

            else
                em.persist( uuf );

            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileUploadFacade.saveUploadedUserFile() " + uuf.toString() );
            throw new STException( e );
        }

        return uuf;
    }
    
    public UploadedUserFile getUploadedUserFile( long testEventId, int ct5ItemId, int ct5ItemPartId, int uploadedUserFileTypeId, boolean refresh) throws Exception
    {
        return getUploadedUserFile(testEventId, null, ct5ItemId, ct5ItemPartId, uploadedUserFileTypeId, refresh);
    }
    
    public UploadedUserFile getUploadedUserFile( long testEventId, String nodeUniqueId, int nodeSeq, int subnodeSeq, int uploadedUserFileTypeId, boolean refresh) throws Exception
    {
        try
        {
            // LogService.logIt("FileUploadFacade.getUploadedUserFile() " + testEventId + ", nodeSeq=" + nodeSeq + ", nodeUniqueId=" + nodeUniqueId + ", subnodeSeq=" + subnodeSeq );
            if( nodeUniqueId!=null && !nodeUniqueId.isEmpty() )
            {
                UploadedUserFile uuf = getUploadedUserFileByUniqueId(testEventId,  nodeUniqueId,  subnodeSeq,  refresh, uploadedUserFileTypeId);
                
                if( uuf!=null )
                    return uuf;
            }
            
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            Query q =  em.createNamedQuery( "UploadedUserFile.findTestEventIdAndNodeSubnode" , UploadedUserFile.class );

            q.setParameter( "testEventId", testEventId );
            q.setParameter( "nodeSeq", nodeSeq );
            q.setParameter( "subnodeSeq", subnodeSeq );
            q.setParameter("uploadedUserFileTypeId", uploadedUserFileTypeId>=0 ? uploadedUserFileTypeId : 0);

            if( refresh )
                q.setHint( "jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS );


            return (UploadedUserFile) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "FileUploadFacade.getUploadedUserFile( " + testEventId + ", refresh=" + refresh + " ) " );

            throw new STException( e );
        }
    }


    public UploadedUserFile getUploadedUserFileByUniqueId( long testEventId, String nodeUniqueId, int subnodeSeq, boolean refresh, int uploadedUserFileTypeId) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            Query q;
            
            q =  em.createNamedQuery( "UploadedUserFile.findTestEventIdAndNodeUniqueSubnode" , UploadedUserFile.class );

            q.setParameter( "testEventId", testEventId );
            q.setParameter( "nodeUniqueId", nodeUniqueId );
            q.setParameter( "subnodeSeq", subnodeSeq );
            q.setParameter("uploadedUserFileTypeId", uploadedUserFileTypeId>=0 ? uploadedUserFileTypeId : 0 );

            if( refresh )
                q.setHint( "jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS );

            return (UploadedUserFile) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "FileUploadFacade.getUploadedUserFileByUniqueId( " + testEventId + ", nodeUniqueId=" + nodeUniqueId + " ) " );

            throw new STException( e );
        }
    }
    
    

}
