/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.amazoncloud;

import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.file.BucketType;
import com.tm2score.file.FileXferUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

/**
 *
 * @author miker_000
 */
public class AmazonRekognitionImageInfo {
    
    private static Boolean useAwsForS3;
    private static String dirBase;
    
    UploadedUserFile uuf;
    // byte[] bytes;
    // S3Object s3Object;
    boolean useThumb = false;
    
    public AmazonRekognitionImageInfo( UploadedUserFile uuf, boolean useThumb)
    {
        this.uuf = uuf;
        this.useThumb=useThumb;
    }
    
    private static synchronized void init()
    {
        if( useAwsForS3!=null )
            return;
        
        useAwsForS3 = RuntimeConstants.getBooleanValue( "useAwsMediaServer" );
        
        dirBase = RuntimeConstants.getStringValue( "userFileUploadBaseDir" );   // /hra or /ful/hra or locals        
    }
    
    
    public String toString()
    {
        return "AmazonRekognitionImageInfo " + (uuf==null ? "UploadedUserFile is null" : uuf.toString() );
    }

    
    private boolean usesS3()
    {
        init();
        return useAwsForS3;
    }
    
    
    public Image getImage( FileXferUtils fileXfer )
    {
        init();
            
        S3Object s30 = null;
        try
        {
            if( usesS3() )
            {
                s30 = getS3Object();
                return Image.builder().s3Object(s30).build();
                // return new Image().withS3Object( s30 );
            }
            else
            {
                return Image.builder().bytes( getSdkBytes( fileXfer )).build();
                //return new Image().withBytes( getByteBuffer( fileXfer ));
            }
        }
        catch( Exception e )
        {
            
            LogService.logIt( e, "AmazonRekognitionImageInfo.getImage() " + toString() + ", useS3=" + usesS3() );
            return null;
        }
    }

    private SdkBytes getSdkBytes( FileXferUtils fileXfer )
    {
        return SdkBytes.fromByteArray(getBytes( fileXfer ) );
    }
    
    //private ByteBuffer getByteBuffer( FileXferUtils fileXfer )
    //{
    //    return ByteBuffer.wrap( getBytes( fileXfer ) );
    //}
    
    
    private byte[] getBytes( FileXferUtils fileXfer )
    {
        try
        {
            init();
            
            if( usesS3() )
                return null;
            
            String filename = useThumb ? uuf.getThumbFilename() : uuf.getFilename();
            
            String directory = dirBase + uuf.getDirectory();
            return fileXfer.getFileLocal(directory, filename, BucketType.USERUPLOAD.getBucketTypeId() );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonRekognitionImageInfo.getBytes() " + toString() );
            return null;
        }
    }
    
    private S3Object getS3Object()
    {
        init();
        
        if( !usesS3() )
            return null;
        
        BucketType bucketType = BucketType.USERUPLOAD;
        
        String fn = useThumb ? uuf.getThumbFilename() : uuf.getFilename();
        
        if( useThumb )
        {
            if( fn!=null && fn.contains( ".AWSCOUNT." ) )
                fn = StringUtils.replaceStr( fn, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );        
            else if( fn!=null && fn.contains(  ".IDX." ) )
                fn = StringUtils.replaceStr( fn, ".IDX." , ".1." );                
        }

        // String key = bucketType.getBaseKey() + dirBase + uuf.getDirectory()  + "/" + fn;
        String key = bucketType.getBaseKey() +  uuf.getDirectory().substring(1, uuf.getDirectory().length())  + "/" + fn;
        
        if( key.startsWith("/"))
            key = key.substring(1,key.length());
        
        String bucket = bucketType.getBucket();
        
        // LogService.logIt( "AmazonRekognitionImageInfo.getS3Object() bucket=" + bucket + ", key=" + key );
        
        return S3Object.builder().name(key).bucket(bucket).build();
        //return new S3Object().withName( key ).withBucket( bucket );
    }
}
