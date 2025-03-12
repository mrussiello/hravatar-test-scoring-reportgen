package com.tm2score.file;

import com.tm2score.global.RuntimeConstants;
import software.amazon.awssdk.regions.Region;


public enum BucketType
{
    CFMEDIA(1),
    USERUPLOAD(2),
    LVRECORDING(3),
    LVRECORDING_TEST(4),
    PROCTORRECORDING(5),
    PROCTORRECORDING_TEST(6),
    REFRECORDING(7),
    REFRECORDING_TEST(8),
    OV_PRO_RECORDING(9),
    CT5(10),
    CT5_TEST(11);


    private final int bucketTypeId;


    private BucketType( int p )
    {
        this.bucketTypeId = p;
    }


    public int getBucketTypeId()
    {
        return this.bucketTypeId;
    }


    public boolean getUsesPublicReadAcl()
    {
        return equals( CFMEDIA );
    }


    public static BucketType getValue( int id )
    {
        BucketType[] vals = BucketType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBucketTypeId() == id )
                return vals[i];
        }

        return null;
    }

    
    public static BucketType getForBucket( String bucket )
    {
        if( bucket==null || bucket.isBlank() || bucket.equals( RuntimeConstants.getStringValue( "awsBucket" )) )
            return CFMEDIA;
        
        if( bucket.equals( RuntimeConstants.getStringValue( "awsBucketLvRecording" )) )
            return RuntimeConstants.getBooleanValue("useTestFoldersForAwsLvFiles") ? LVRECORDING_TEST : LVRECORDING;

        if( bucket.equals( RuntimeConstants.getStringValue( "awsBucketProctorRecording" )) )
            return RuntimeConstants.getBooleanValue("useTestFoldersForProctorRecordings") ? PROCTORRECORDING_TEST : PROCTORRECORDING;

        if( bucket.equals( RuntimeConstants.getStringValue( "awsBucketRefRecording" )) )
            return RuntimeConstants.getBooleanValue("useAwsTestFoldersForProctoring") ? REFRECORDING_TEST : REFRECORDING;
        
        if( bucket.equals( RuntimeConstants.getStringValue( "awsBucketCt5" )) )
            return RuntimeConstants.getBooleanValue("useAwsTestFoldersForCt5") ? CT5_TEST : CT5;        
        
        if( bucket.equals( RuntimeConstants.getStringValue( "awsBucketOvProRecording" )) )
            return OV_PRO_RECORDING;
        
        if( bucket.equals( RuntimeConstants.getStringValue( "awsBucketFileUpload" )) )
            return USERUPLOAD;
        
        return null;
    }
    
    
    public String getBucket()
    {
        if( equals( CFMEDIA ) )
            return RuntimeConstants.getStringValue( "awsBucket" );

        else if( equals( LVRECORDING )  || equals( LVRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBucketLvRecording" );

        else if( equals( PROCTORRECORDING ) || equals( PROCTORRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBucketProctorRecording" );

        else if( equals( REFRECORDING ) || equals( REFRECORDING_TEST )  )
            return RuntimeConstants.getStringValue( "awsBucketRefRecording" );
        
        else if( equals( OV_PRO_RECORDING )  )
            return RuntimeConstants.getStringValue( "awsBucketOvProRecording" );
        
        else if( equals( CT5 ) || equals( CT5_TEST )  )
            return RuntimeConstants.getStringValue( "awsBucketCt5" );
        
        return RuntimeConstants.getStringValue( "awsBucketFileUpload" );
    }

    public String getBaseKey()
    {
        if( equals( CFMEDIA ) )
            return RuntimeConstants.getStringValue( "awsBaseKey" );

        else if( equals( LVRECORDING ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyLvRecording" );
        
        else if( equals( LVRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyLvRecordingTest" );
        
        else if( equals( PROCTORRECORDING ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyProctorRecording" );

        else if( equals( PROCTORRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyProctorRecordingTest" );

        else if( equals( REFRECORDING ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyRefRecording" );

        else if( equals( REFRECORDING_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyRefRecordingTest" );
        
        else if( equals( CT5 ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyCt5" );

        else if( equals( CT5_TEST ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyCt5Test" );
                
        else if( equals( OV_PRO_RECORDING ) )
            return RuntimeConstants.getStringValue( "awsBaseKeyOvProRecording" );
        
        return RuntimeConstants.getStringValue( "awsBaseKeyFileUpload" );

    }
    
    public Region getBucketRegion()
    {
        if( getBucketRegionId()==1 )
            return Region.US_EAST_1;
        if( getBucketRegionId()==12 )
            return Region.US_WEST_2;
        return Region.US_EAST_1;
    }
    
    public int getBucketRegionId()
    {
        if( equals( CFMEDIA ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionId" );

        else if( equals( PROCTORRECORDING ) || equals( PROCTORRECORDING_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdProctorRecording" );

        else if( equals( LVRECORDING )  || equals( LVRECORDING_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdLvRecording" );
        
        else if( equals( REFRECORDING ) || equals( REFRECORDING_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdRefRecording" );

        else if( equals( OV_PRO_RECORDING ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdOvProRecording" );
                
        else if( equals( CT5 )  || equals( CT5_TEST ) )
            return RuntimeConstants.getIntValue( "awsBucketRegionIdCt5" );
                
        return RuntimeConstants.getIntValue( "awsBucketRegionIdFileUpload" );
    }

    
}
