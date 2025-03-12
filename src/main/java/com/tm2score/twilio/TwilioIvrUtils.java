/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.twilio;

import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.av.AvEventFacade;
import com.tm2score.av.AvItemAudioStatusType;
import com.tm2score.file.HttpFileUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 *
 * @author miker_000
 */
public class TwilioIvrUtils {
    
    AvEventFacade avEventFacade = null;
    
    
    public void saveAudioFileAsMp3ToDb( AvItemResponse avItemResponse ) throws Exception
    {

        try
        {
            
            if( avItemResponse==null || avItemResponse.getAvItemResponseId()<=0 )
                throw new Exception( "avItemResponse is required" );
            
            if( avItemResponse.getAudioBytes()!=null && avItemResponse.getAudioBytes().length>0 )
            {
                if( avItemResponse.getAudioStatusTypeId() < AvItemAudioStatusType.STORED_LOCALLY_DELETED_REMOTELY.getAudioStatusTypeId() )
                {
                    avItemResponse.setAudioStatusTypeId(AvItemAudioStatusType.STORED_LOCALLY_DELETED_REMOTELY.getAudioStatusTypeId() );
                    this.saveAvItemResponse(avItemResponse);
                }
                
                return;
            }
            
            if( avItemResponse.getAudioUri()==null || avItemResponse.getAudioUri().isEmpty() )
                 throw new Exception( "avItemResponse.getAudioUrl is missing. " );

            if( avItemResponse.getAvItemAudioStatusType().isPendingFromSource() )
                throw new Exception( "avItemResponse Audio is still in a pending status. " );

            // get MP3 for saving to DBMS
            byte[] bytes = HttpFileUtils.getBinaryFileAsBytes( avItemResponse.getAudioUri() + ".mp3" );                
            avItemResponse.setAudioBytes(bytes);
                
            LogService.logIt( "TwilioIvrUtils.saveAudioFileAsMp3ToDb() avItemResponse=" + avItemResponse.getAvItemResponseId() + ", unique=" + avItemResponse.getItemUniqueId() + ", bytes: " + (bytes==null ? "null" : bytes.length ) );
            
            if( avItemResponse.getAudioStatusTypeId() < AvItemAudioStatusType.STORED_LOCALLY_DELETED_REMOTELY.getAudioStatusTypeId() )
                avItemResponse.setAudioStatusTypeId(AvItemAudioStatusType.STORED_LOCALLY_READY_NOT_DELETED_REMOTELY.getAudioStatusTypeId() );
    
            saveAvItemResponse( avItemResponse );                
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TwilioIvrUtils.saveAudioFileAsMp3ToDb() " +  (avItemResponse==null ? "AvItemResponse is null" : avItemResponse.toString() )  );   
            
            throw e;
        }
    }

    
    public InputStream getAudioFileAsInputStream( AvItemResponse avItemResponse  ) throws Exception
    {
        try
        {            
            if( avItemResponse==null || avItemResponse.getAvItemResponseId()<=0 )
                throw new Exception( "avItemResponse is required" );
            
            if( avItemResponse.getAudioUri()==null || avItemResponse.getAudioUri().isEmpty() )
                 throw new Exception( "avItemResponse.getAudioUrl is missing. " );

            if( avItemResponse.getAvItemAudioStatusType().isPendingFromSource() )
                throw new Exception( "avItemResponse Audio is still in a pending status. " );
                        
            return HttpFileUtils.getBinaryFileAsInputStream( avItemResponse.getAudioUri() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TwilioIvrUtils.getAudioFileAsInputStream() " +  (avItemResponse==null ? "AvItemResponse is null" : avItemResponse.toString() )  );   
            
            if( e instanceof FileNotFoundException )
                return new ByteArrayInputStream( new byte[0] ); 
            
            throw e;
        }        
    }

    public String getAudioFileAsBase64( AvItemResponse avItemResponse  ) throws Exception
    {
        try
        {            
            if( avItemResponse==null || avItemResponse.getAvItemResponseId()<=0 )
                throw new Exception( "avItemResponse is required" );
            
            if( avItemResponse.getAudioUri()==null || avItemResponse.getAudioUri().isEmpty() )
                 throw new Exception( "avItemResponse.getAudioUrl is missing. " );

            if( avItemResponse.getAvItemAudioStatusType().isPendingFromSource() )
                throw new Exception( "avItemResponse Audio is still in a pending status. " );

            byte[] bytes = null;
                        
            String audioUri = avItemResponse.getAudioUri();
            
            boolean isLocalHost = RuntimeConstants.getBooleanValue( "isLocalHostForTranscription" );

            if( isLocalHost )
                audioUri = "http://" + RuntimeConstants.getStringValue("mediaServerDomain") + "/" + RuntimeConstants.getStringValue("mediaServerWebapp") + "/ful/hra" + audioUri;
            
            return HttpFileUtils.getBinaryFileAsBase64Str( audioUri );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TwilioIvrUtils.getAudioFileAsBase64() " +  (avItemResponse==null ? "AvItemResponse is null" : avItemResponse.toString() )  );   
            
            return null;
            // throw e;
        }
    }

    
    private void saveAvItemResponse( AvItemResponse avItemResponse ) throws Exception
    {
        if( avEventFacade==null )
            avEventFacade = AvEventFacade.getInstance();

        avEventFacade.saveAvItemResponse(avItemResponse);        
    }    
    
}
