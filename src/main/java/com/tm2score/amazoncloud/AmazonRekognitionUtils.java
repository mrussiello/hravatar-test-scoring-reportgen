/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.amazoncloud;

import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.file.FileXferUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.OrientationCorrection;

/**
 *
 * @author miker_000
 */
public class AmazonRekognitionUtils {
    
    // static float DEFAULT_SIMLILARITY_THRESHOLD = 20f;
    
    RekognitionClient rekognitionClient;
    FileXferUtils fileXfer;
    
    private synchronized void initClient() throws Exception
    {
        if( rekognitionClient!=null )
            return;
        
        try
        {
            AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKey" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKey" )).build();            
            StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );
            rekognitionClient = RekognitionClient.builder().region(getClientRegion()).credentialsProvider(bac).build();
            //BasicAWSCredentials bac = new BasicAWSCredentials( RuntimeConstants.getStringValue( "awsAccessKey" ) , RuntimeConstants.getStringValue( "awsSecretKey" ) );
            //rekognitionClient = AmazonRekognitionClientBuilder
            //                        .standard()
            //                        .withRegion(getClientRegion())
            //                        .withCredentials(new AWSStaticCredentialsProvider(bac))
            //                       .build();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonRekognitionUtils.initClient() " );
            throw e;
        }
    }
    
    public Region getClientRegion()
    {
        int rid = RuntimeConstants.getIntValue("awsRekognitionRegionId");
        
        if( rid==1 )
            return Region.US_EAST_1;
        if( rid==12 )
            return Region.US_WEST_2;
        return Region.US_EAST_1;
    }
    
    

    /*
     Data[0] == SUCCESS or ERROR
     data[1] = null or FaceDetail for success, message for Error
     data[2]= null, 0, or orientation. Orientation=0 good, XX=XX degrees counterclockwise (must rotate XX clockwise)
     data[3]= null or number of faces detected. If there's someone standing behind you there would be two faces.
    */
    public Object[] getSingleFaceDetails( UploadedUserFile uuf, boolean multipleFacesOk, boolean useThumbImage)
    {
        Object[] out = new Object[4];
        
        out[2] = ((int) 0);
        out[3] = ((int) 0);
        
        try
        {
            if( uuf==null || 
                (!useThumbImage && !uuf.hasImageFile()) || 
                (useThumbImage && ( uuf.getThumbFilename()==null || uuf.getThumbFilename().isEmpty())) )
                throw new Exception( "UploadedUserFile1 is invalid" );

            initClient();
            
            // float match = 100;
            
            AmazonRekognitionImageInfo arii = new AmazonRekognitionImageInfo( uuf, useThumbImage );
               
            if( fileXfer == null )
                fileXfer = new FileXferUtils();
            
            Image src = arii.getImage(fileXfer);

            if( src==null )
                throw new Exception( "Unable to obtain RekognitionClient.Image for Image File 1" );
            
                        
            DetectFacesRequest  req = DetectFacesRequest.builder().image(src).attributes( Attribute.ALL ).build();

            DetectFacesResponse  res=rekognitionClient.detectFaces(req);

            OrientationCorrection ocor = res.orientationCorrection();
            
            //String oStr = res.getOrientationCorrection();
            
            // LogService.logIt( "AmazonRekognitionUtils.getSingleFaceDetails() orientation String=" + oStr );
            
            int oVal = 0;
            if( ocor!=null )
            {
                switch (ocor) {
                    case ROTATE_180:
                        oVal = 180;
                        break;
                    case ROTATE_270:
                        oVal = 270;
                        break;
                    case ROTATE_90:
                        oVal = 90;
                        break;
                    default:
                        break;
                }
            }            
            out[2]= oVal;
            
            
            //if( oStr!=null && oStr.indexOf("_")>0 )
            //{
            //    String r = null;
            //    try
            //    {
            //        r = oStr.substring( oStr.indexOf("_")+1, oStr.length() );
            //        if( !r.trim().isEmpty() )
           //         {   
            //            oVal = Integer.parseInt( r );
            //            out[2]= ((int) oVal);
            //        }
            //    }
            //    catch( Exception e )
            //    {
            //        LogService.logIt(e,"AmazonRekognitionUtils.getSingleFaceDetails() Parsing orientation String=" + oStr  + ", r=" + r );
            //    }
            //}
            
            //if( !multipleFacesOk && res.getFaceDetails().size() > 1)
            //    LogService.logIt( "AmazonRekognitionUtils.getSingleFaceDetails() Found more than one face: " + res.getFaceDetails().size() );
            
            if( res.faceDetails().size() < 1)
            {
                //LogService.logIt( "AmazonRekognitionUtils.getSingleFaceDetails() No faceDetails found for photo. Returing null" );
                out[0]="SUCCESS";
                out[1]=null;
                return out;
            }
            
            out[0]="SUCCESS";
            out[1]=res.faceDetails().get(0);
            out[3]=(Integer)( res.faceDetails().size() );
            
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AmazonRekognitionUtils.getSingleFaceDetails() File1: " + (uuf==null ? "null" : uuf.toString()) ); 
            out[0]="ERROR";
            out[1]=e.toString();
            return out;
        }
    }


}
