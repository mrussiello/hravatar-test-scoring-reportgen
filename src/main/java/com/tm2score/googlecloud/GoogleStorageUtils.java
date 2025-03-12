/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.googlecloud;


import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import java.io.InputStream;

/**
 *
 * @author miker_000
 */
public class GoogleStorageUtils {
    
    
    public static String GOOGLE_PROJECT_NAME = null;
    public static String GOOGLE_VOICE_BUCKET = null;
    
    public synchronized static void init()
    {
        if( GOOGLE_PROJECT_NAME == null )
        {
            GOOGLE_PROJECT_NAME = RuntimeConstants.getStringValue( "gcloud.projectname" );
            GOOGLE_VOICE_BUCKET = RuntimeConstants.getStringValue( "gcloud.voicestoragebucket" );
        }
    }
    
    
    public static String storeVoiceFile( InputStream inptStrm, String fileName, String contentType ) throws Exception
    {
        init();
        
        return storeFile( GOOGLE_VOICE_BUCKET, inptStrm, fileName, contentType );
    }
    
    
    
    public static String storeFile( String bucketName, InputStream inptStrm, String fileName, String contentType ) throws Exception
    {
        try
        {
            init();
            
            // LogService.logIt( "GoogleStorageUtils.storeFile() START fileName=" + fileName + ", bucket=" + bucketName );   
            
            Storage storage = StorageOptions.getDefaultInstance().getService();
            
            Bucket bucket = storage.get( bucketName );
            
            Blob blob = bucket.create(fileName, inptStrm, contentType );
            
            BlobId blobId = BlobId.of( bucketName, fileName);
            //Long blobGeneration = blobId.getGeneration();
            //BlobId blobId = BlobId.of(GOOGLE_VOICE_BUCKET, fileName, blobGeneration);
            Acl acl = storage.createAcl(blobId, Acl.of(User.ofAllUsers(), Role.READER));            

            // LogService.logIt( "GoogleStorageUtils.storeFile() COMPLETE fileName=" + fileName + ", savedSize=" + blob.getSize() );   
            
            Tracker.addGoogleCloudStorageReq();
            
            return getGoogleUriForFile(  bucketName,  fileName );
        }
        
        catch( StorageException e )
        {
            LogService.logIt( e, "GoogleStorageUtils.storeFile() fileName=" + fileName + ", bucket=" + bucketName + ", " + e.toString() ); 
            
            throw new GoogleApiException( "GoogleStorageUtils.storeFile() fileName=" + fileName + ", bucket=" + bucketName, e.getCode(), e.getMessage() );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleStorageUtils.storeFile() " );
            
            throw e;
        }
    }
    
    public static String getGoogleUriForVoiceFile( String fileName )
    {
        init();
        
        return "gs://" + GOOGLE_VOICE_BUCKET + "/" + fileName;
    }

    public static String getGoogleUriForFile( String bucketName, String fileName )
    {
        init();
        
        return "gs://" + bucketName + "/" + fileName;
    }

    
    public static void deleteVoiceFile( String fileName ) throws Exception
    {
        deleteFile( GOOGLE_VOICE_BUCKET, fileName );
    }
    
    public static void deleteFile( String bucketName, String fileName ) throws Exception
    {
        try
        {
            init();
            
            Storage storage = StorageOptions.getDefaultInstance().getService();
            
            BlobId blobId = BlobId.of( GOOGLE_VOICE_BUCKET, fileName);
            boolean deleted = storage.delete(blobId);
            if (deleted) 
            {
                // LogService.logIt( "GoogleStorageUtils.deleteFile() deleted fileName=" + fileName );            
              // the blob was deleted
            } 
            else 
            {
                LogService.logIt( "GoogleStorageUtils.deleteFile() fileName=" + fileName + " was not found!" );            
              // the blob was not found
            }  
            
            Tracker.addGoogleCloudStorageReq();
            
        }
        
        catch( StorageException e )
        {
            LogService.logIt( e, "GoogleStorageUtils.deleteFile() " ); 
            
            throw e;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleStorageUtils.storeFile() " );
            
            throw e;
        }
    }

}
