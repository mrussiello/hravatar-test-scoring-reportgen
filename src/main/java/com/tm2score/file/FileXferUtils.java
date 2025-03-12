package com.tm2score.file;


import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;




// @Stateless
public class FileXferUtils
{
    public static Map<Region, S3Client> regionClientMap;
    // public static Map<Region, TransferManager> regionTransferMap;
    //public static S3Client amazonS3Client;

    //public static TransferManager transferManager;

    public static Boolean useAwsForMedia=null;
    public static Boolean useAwsForProctoring=null;

    public static char PATH_SEPARATOR = '\\';


    /*
    public static synchronized TransferManager getAmazonTransferManager( Region region )
    {
        S3Client client = getS3Client( region );
        if( client==null )
            return null;
        
        if( regionTransferMap==null )
            regionTransferMap = new HashMap<>();
        
        TransferManager tm = regionTransferMap.get( region );
        if( tm!=null )
            return tm;
        
        tm = TransferManagerBuilder.standard().withS3Client(client).build();
        
        regionTransferMap.put( region, tm);
        return tm;
    }
    */
    
    public static synchronized S3Client getS3Client( Region region )
    {
        if( useAwsForMedia==null )
            useAwsForMedia =  RuntimeConstants.getBooleanValue( "useAwsMediaServer" );

        if( useAwsForProctoring==null )
            useAwsForProctoring = RuntimeConstants.getBooleanValue( "useAwsForProctorRecording" );
        
        if( (useAwsForMedia!=null && !useAwsForMedia) && (useAwsForProctoring!=null && !useAwsForProctoring) )
            return null;
        
        if( regionClientMap==null )
            regionClientMap = new HashMap<>();
        
        S3Client client = regionClientMap.get(region);
        
        if( client!=null )
            return client;
        
        AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKey" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKey" )).build();            
        StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );
        client = S3Client.builder().region(region).credentialsProvider(bac).build(); 
        
        //BasicAWSCredentials bac = new BasicAWSCredentials( RuntimeConstants.getStringValue( "awsAccessKey" ) , RuntimeConstants.getStringValue( "awsSecretKey" ) );
        //client = AmazonS3ClientBuilder.standard()
       //                 .withRegion( region )
        //                .withCredentials(new AWSStaticCredentialsProvider(bac))
        //                .build();
        regionClientMap.put( region, client);
        return client;
    }
    
    
    
    
    public static synchronized void init(boolean forceAwsCreds)
    {
        if( useAwsForMedia==null )
            useAwsForMedia= RuntimeConstants.getBooleanValue( "useAwsMediaServer" );

        if( useAwsForProctoring==null )
            useAwsForProctoring=RuntimeConstants.getBooleanValue( "useAwsForProctorRecording" );
        
        //if( !forceAwsCreds && useAwsForMedia!=null && ( !useAwsForMedia ) ) // || amazonS3Client != null ) )
        //    return;
        
        //useAwsForMedia = forceAwsCreds ||  RuntimeConstants.getBooleanValue( "useAwsMediaServer" );
    }





    public static String getPresignedUrlAws( String directory, String filename, int bucketTypeId, String frcBaseKey, int minutes ) throws Exception
    {
        init(false);
        
        String key = null;

        try 
        {

            BucketType bucketType = BucketType.getValue(bucketTypeId);

            if (directory.startsWith("/")) {
                directory = directory.substring(1, directory.length());
            }

            key = bucketType.getBaseKey() + directory + "/" + filename;

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketType.getBucket())
                .key(key)
                .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(minutes))
                .getObjectRequest(getObjectRequest)
                .build();

            AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKey" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKey" )).build();            
            StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );

            S3Presigner presigner = S3Presigner.builder().credentialsProvider( bac )
                                    .region(bucketType.getBucketRegion())
                                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
            String theUrl = presignedGetObjectRequest.url().toExternalForm();
            //LogService.logIt("FileXferUtils.getPresignedUrlAws() url=" + theUrl + ", key=" + key);                
            return theUrl;
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.getPresignedUrlAws() " + key);
            throw new STException(e);
        }

        //return null;
        /*
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init(false);

        String targetKey = null;

        try
        {
            if( targetDirectory.startsWith( "/" ) )
                targetDirectory = targetDirectory.substring( 1, targetDirectory.length() );

            targetKey = ( frcBaseKey==null || frcBaseKey.isBlank() ? bucketType.getBaseKey() : frcBaseKey) + targetDirectory  + "/" + targetFilename;

            // LogService.logIt( "FileXferUtils.getPresignedUrlAws() " + bucketType.getBucket() + "/" + targetKey );
            
            // LogService.logIt( "FileXferUtils.fileExistsAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() );

            S3Client amazonS3 = getS3Client(bucketType.getBucketRegion());
            
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.MINUTE, minutes );
            java.util.Date expiration = cal.getTime();
            
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest( bucketType.getBucket(), targetKey)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);
            URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);            
            
            // LogService.logIt("FileXferUtils.getPresignedUrlAws() " + targetKey + ", minutes=" + minutes + ", " + url.toString() );            
            return url.toString();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getPresignedUrlAws() " + targetKey );
            throw new STException(e);
        }
        */

    }
    

    
    public InputStream getFileAwsIs(String directory, String filename, int bucketTypeId) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        return getFileAwsIs( directory,  filename, bucketType, bucketType.getBaseKey() );
    }

    public InputStream getFileAwsIs(String directory, String filename, BucketType bucketType, String forceBaseKey) throws Exception {
        // initAwsTransferManager();

        String key = null;

        try 
        {
            if (directory.startsWith("/")) 
            {
                directory = directory.substring(1, directory.length());
            }

            if (forceBaseKey==null) 
            {
                key = bucketType.getBaseKey() + directory + "/" + filename;
            } else {
                key = forceBaseKey + directory + "/" + filename;
            }

            S3Client client = getS3Client(bucketType.getBucketRegion());

            GetObjectRequest objectRequest = GetObjectRequest.builder().key(key).bucket(bucketType.getBucket()).build();

            InputStream iss = client.getObject(objectRequest, ResponseTransformer.toInputStream() );
            return iss;
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.getFileAwsIs() " + key);
            throw new STException(e);
        }
    }
    
    
    
    
    


    /*
    private S3Object getS3Object( String directory, String filename, int bucketTypeId ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init(false);

        // LogService.logIt( "AwsFileUtils.getFileAwsIs() bucket=" + bucket + ", " + directory + ", " + filename + " useAws=" + useAws.booleanValue() );

        String key = null;

        try
        {
            if( useAwsForMedia )
            {

                if( directory.startsWith( "/" ) )
                    directory = directory.substring( 1, directory.length() );

                key = bucketType.getBaseKey() + directory  + "/" + filename;

                // LogService.logIt( "AwsFileUtils.getFileAws() " + key );

                S3Object s3o = getS3Client(bucketType.getBucketRegion()).getObject( bucketType.getBucket(), key );

                return s3o; // .getObjectContent();
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getS3Object() " + key );

            throw new STException(e);
        }

        return null;
    }
    */


    
    
    public InputStream getFileInputStream( String directory, String filename, int bucketTypeId ) throws Exception
    {
        init(false);

        // LogService.logIt( "FileXferUtils.getFileInputStream() bucketTypeId=" + bucketTypeId + ", " + directory + ", " + filename + " useAws=" + useAws.booleanValue() );

        //String key = null;

        if( useAwsForMedia )
        {
            return getFileAwsIs( directory,  filename,  bucketTypeId);
            //S3Object s3o = getS3Object(  directory,  filename,  bucketTypeId );
            //return new Object[] { s3o.getObjectContent() , s3o };
        }

        if( filename == null || filename.length() == 0 )
                throw new Exception( "No filename" );

            if( directory == null )
                directory = "";

            if( directory.length() == 0 || directory.charAt( 0 ) != '/' )
                directory = "/" + directory;

            //if( !path.startsWith( getFileTreeRootDirectory() ) )
            //    path = getFileTreeRootDirectory() + path;

            if( !directory.endsWith( "/" ) )
                directory += "/";

            if( !localFileExists( directory + filename, false ) )
                throw new Exception( "File does not exist: " + directory + filename );

            return new FileInputStream(directory + filename);
    }


    public static boolean localFileExists( String pathAndFilename, boolean requireNonZeroBytes ) throws Exception
    {
        if( pathAndFilename.charAt( 0 ) != '/' )
            pathAndFilename = "/" + pathAndFilename;

        File file = new File(  pathAndFilename );

        if( !file.exists() )
            LogService.logIt( "FileXferUtils.fileExists() File missing. " + pathAndFilename + ", file.exists() " + file.exists() );

        if( requireNonZeroBytes )
            return file.exists() && file.length() > 0;

        return file.exists();
    }




    /*
    public byte[] getFile( String directory, String filename, int bucketTypeId ) throws Exception
    {
        initAwsTransferManager(false);

        try
        {
            if( FileXferUtils.useAws )
                return getFileAws( directory, filename, bucketTypeId );
            
            else
                return getFileLocal( directory, filename, bucketTypeId );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFile( directory=" + directory + ", filename=" + filename + ", bucketTypeId=" + bucketTypeId + " )" );

            throw new STException( e );
        }
    }
    */

    
    public byte[] getFileLocal( String directory, String filename, int bucketTypeId ) throws STException
    {
        InputStream fis = null;

        //S3Object s30 = null;
        try
        {
            String localFilesRoot = RuntimeConstants.getStringValue( "localFsRoot" );

            // open a stream to read the file from
            fis = (InputStream) getFileInputStream( localFilesRoot + directory, filename, bucketTypeId );
            // fis = (InputStream) dd[0];
            //s30 = dd[1]==null ? null : (S3Object) dd[1];

            // open a stream to write the bytes in teh file to
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // define a buffer
            byte[] buf = new byte[ 8192 ];

            int len = -1;

            // read bytes from input stream and write to output stream
            while (  ( len = fis.read( buf ) ) > 0  )
            {
               baos.write( buf , 0 , len );
            }

            // close input stream
            // fis.close();

            // convert the outputstream to a byte array and return
            return baos.toByteArray();
        }
        catch( Exception e )
        {
            LogService.logIt( e , "getFileLocal( " + directory + ", " + filename + ", bucketTypeId=" + bucketTypeId + " ) " );
            throw new STException( e );
        }

        finally
        {
            if( fis != null )
            {
                try
                {
                    fis.close();
                }
                catch( IOException e )
                {
                    LogService.logIt( e , "getFileLocal( " + directory + ", " + filename + ", bucketTypeId=" + bucketTypeId + " ) closing file output stream." );
                }
            }
        }
    }
    
        

    public void deleteFileAws( String targetDirectory, String targetFilename, int bucketTypeId) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init(true);

        String targetKey = null;

        try 
        {
            if (targetDirectory.startsWith("/")) 
            {
                targetDirectory = targetDirectory.substring(1, targetDirectory.length());
            }

            targetKey = bucketType.getBaseKey() + targetDirectory + (targetFilename != null && targetFilename.length() > 0 ? "/" + targetFilename : "");

            DeleteObjectRequest dor = DeleteObjectRequest.builder().bucket(bucketType.getBucket()).key(targetKey).build();                            

            DeleteObjectResponse delResp = getS3Client(bucketType.getBucketRegion()).deleteObject(dor);

            LogService.logIt( "FileXferUtils.deleteFileAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() + ", result=" + delResp.toString() );

            // LogService.logIt( "FileXferUtils.deleteFileAws " + targetKey + ", bucket=" + bucketType.getBucket() + ", baseKey=" + bucketType.getBaseKey() );
            // getS3Client(bucketType, bucketType.getBucketRegion()).deleteObject(bucketType.getBucket(), targetKey);

            Thread.sleep(500);
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.deleteFileAws() WWW.1 Target key=" + targetKey + ", bucketTypeId=" + bucketTypeId);
            throw new STException(e);
        }
        
        /*
        BucketType bucketType = BucketType.getValue(bucketTypeId);
        init(true);
        String targetKey = null;
        try
        {
            //if( FileXferUtils.useAws )
            //{
                if( targetDirectory.startsWith( "/" ) )
                    targetDirectory = targetDirectory.substring( 1, targetDirectory.length() );
                targetKey = bucketType.getBaseKey() + targetDirectory   + (targetFilename != null && targetFilename.length()>0 ? "/" + targetFilename : "" );
                // LogService.logIt( "FileXferUtils.deleteFileAws " + targetKey );
                 getS3Client(bucketType.getBucketRegion()).deleteObject( bucketType.getBucket(), targetKey);
                Thread.sleep(500);
            //}
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.deleteFileAws() " + targetKey );
            throw new STException(e);
        }
        */
    }
    
    
    
    /*
    public void deleteFileAws( String s3FileUri ) throws Exception
    {
        init(false);

        try
        {
            AmazonS3URI s3Uri = new AmazonS3URI( s3FileUri );
            // ByteArrayOutputStream baos;

            String bucket = s3Uri.getBucket();
            String key = s3Uri.getKey();
            
            BucketType bucketType = BucketType.getForBucket( bucket );
             getS3Client(bucketType.getBucketRegion()).deleteObject(bucket, key);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.deleteFileAws() " + s3FileUri );
            throw new STException(e);
        }
    }
    */
    
    /*
    public byte[] getFileAws( String s3FileUri ) throws Exception
    {
        init(false);

        try
        {
            AmazonS3URI s3Uri = new AmazonS3URI( s3FileUri );
            ByteArrayOutputStream baos;

            String bucket = s3Uri.getBucket();
            String key = s3Uri.getKey();
            
            BucketType bucketType = BucketType.getForBucket( bucket );
            try (S3Object s3o =  getS3Client(bucketType.getBucketRegion()).getObject( bucket, key ) )
            {
                InputStream iss = s3o.getObjectContent();
                baos = new ByteArrayOutputStream();
                BufferedOutputStream bout = new BufferedOutputStream (baos);
                BufferedInputStream bin = new BufferedInputStream(iss );
                int byte_;
                while ((byte_=bin.read()) != -1)
                {
                    bout.write(byte_);
                }
                bout.close();

                bin.close();
                iss.close();
            }

            return baos.toByteArray();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFileAws() " + s3FileUri );
            throw new STException(e);
        }
    }
    */

    
    
    /*
    public byte[] getFileAws( String directory, String filename, int bucketTypeId ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        initAwsTransferManager(false);

        String key = null;

        try
        {
            if( FileXferUtils.useAws )
            {
                if( directory.startsWith( "/" ) )
                    directory = directory.substring( 1, directory.length() );

                key = bucketType.getBaseKey() + directory  + "/" + filename;
                ByteArrayOutputStream baos;

                try (S3Object s3o =  getAmazonS3Client(bucketType.getBucketRegion()).getObject( bucketType.getBucket(), key) )
                {
                    InputStream iss = s3o.getObjectContent();
                    baos = new ByteArrayOutputStream();
                    BufferedOutputStream bout = new BufferedOutputStream (baos);
                    BufferedInputStream bin = new BufferedInputStream(iss );
                    int byte_;
                    while ((byte_=bin.read()) != -1)
                    {
                        bout.write(byte_);
                    }
                    bout.close();

                    bin.close();
                    iss.close();
                }

                return baos.toByteArray();
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFileAws() " + key );

            throw new STException(e);
        }

        return new byte[0];
    }
    */



    /*
    public void saveFile( String directory, String filename, InputStream iss, String contentType, int fileSize, int bucketTypeId) throws Exception
    {
        initAwsTransferManager(false);

        try
        {
            if( FileXferUtils.useAws )
            {
                saveFileToAws( directory , filename, iss, fileSize, contentType, bucketTypeId );

                return;
            }

            File f = new File( RuntimeConstants.getStringValue( "localFsRoot" ) + directory + "/" );

            f.mkdirs();

            // this is the file itself.
            FileOutputStream fout= new FileOutputStream ( RuntimeConstants.getStringValue( "localFsRoot" ) + directory + "/" + filename );

            // this makes it a buffered stream.
            BufferedOutputStream bout = new BufferedOutputStream (fout);

            // BufferedInputStream bin = new BufferedInputStream( ufw.getUploadedFile().getInputStream() );

            int byte_;
            while ((byte_=iss.read()) != -1)
            {
                 bout.write(byte_);
            }
            bout.close();
            iss.close();

            fout.close();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.saveFile( directory=" + directory + ", filename=" + filename + " )" );

            throw new STException( e );
        }
    }
    */
    
    
    
    
    public void saveFileToAws( String directory, String filename, InputStream iss, long length, String contentType, int bucketTypeId) throws Exception
    {
        init(true);
        
        BucketType bucketType = bucketTypeId <= 0 ? BucketType.CFMEDIA : BucketType.getValue(bucketTypeId);

        String key = null;

        try 
        {
            if (directory.startsWith("/"))
                directory = directory.substring(1, directory.length());

            key = bucketType.getBaseKey() + directory + "/" + filename;

            Map<String, String> metadata = new HashMap<>();

            // ObjectMetadata omd = new ObjectMetadata();
            PutObjectRequest.Builder porb = PutObjectRequest.builder().bucket(bucketType.getBucket()).key(key).metadata(metadata); 

            if (length>0) 
                porb = porb.contentLength(length);

            FileContentType fct = FileContentType.getFileContentTypeFromContentType(contentType, filename);
            if( fct != null && (contentType == null || contentType.length()==0) ) 
                contentType = fct.getBaseContentType();

            if (contentType != null && contentType.length() > 0) 
                porb = porb.contentType(contentType);

            if( fct!=null && fct.isText() )
                porb = porb.contentEncoding("UTF8");

            int maxAge=345600;
            if( fct!=null )
            {
                if( fct.isJavascript() || fct.isCss() )
                    maxAge=0;
                else if( fct.isVideo() || fct.isAudio() )
                    maxAge=345600;
                else if( fct.isImage())
                    maxAge=345600;
            }
            porb = porb.cacheControl("max-age=" + maxAge);

            if (bucketType.getUsesPublicReadAcl()) 
                porb.acl( ObjectCannedACL.PUBLIC_READ);

            PutObjectRequest por = porb.build();  // new PutObjectRequest(bucketType.getBucket(), key, iss, omd);

            PutObjectResponse response = getS3Client(bucketType.getBucketRegion()).putObject(por, RequestBody.fromInputStream(iss, length));
            LogService.logIt("FileXferUtils.saveFileToAws() with inputStream. Saving file " + key + " to bucket " + bucketType.getBucket() + ", response=" + response.toString() + ", contentType=" + contentType + ", length=" + length );
        } 
        catch (Exception e) 
        {
            LogService.logIt(e, "FileXferUtils.saveFileToAws() " + key);
            throw new STException(e);
        }
        
        /*
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        init(true);

        String key = null;

        try
        {
            //if( FileXferUtils.useAws )
            //{

                if( directory.startsWith( "/" ) )
                    directory = directory.substring( 1, directory.length() );

                key = bucketType.getBaseKey() + directory  + "/" + filename;

                ObjectMetadata omd = new ObjectMetadata();

                if( length>0 )
                    omd.setContentLength(length);

                FileContentType fct = FileContentType.getFileContentTypeFromContentType(contentType, filename);
                if( contentType == null || contentType.length()==0 )
                {
                    if( fct != null )
                        contentType = fct.getBaseContentType();
                }

                if( contentType != null && contentType.length()> 0 )
                    omd.setContentType( contentType );

                int maxAge=345600;
                if( fct!=null )
                {
                    if( fct.isJavascript() || fct.isCss() )
                        maxAge=0;
                    else if( fct.isVideo() || fct.isAudio() )
                        maxAge=345600;
                    else if( fct.isImage())
                        maxAge=345600;
                }
                
                //omd.setCacheControl("no-cache");
                //omd.setHeader("Expires", 0 );
                omd.setCacheControl( "max-age=" + maxAge );
                
                PutObjectRequest por = new PutObjectRequest( bucketType.getBucket(), key , iss , omd );

                if( bucketType.getUsesPublicReadAcl() )
                    por.setCannedAcl( CannedAccessControlList.PublicRead );

                Upload myUpload =  getAmazonTransferManager(bucketType.getBucketRegion()).upload( por );

                // LogService.logIt( "FileXferUtils.saveFileToAws() Saving file " + key + " to bucket " + RuntimeConstants.getStringValue( "awsBucket" ) );

                // force wait.
                while(myUpload.isDone() == false)
                {
                     Thread.sleep(500);
                }
            //}
        }

        catch( Exception e )
        {
                LogService.logIt( e, "FileXferUtils.saveFileToAws() " + key );

                throw new STException(e);
        }
        */

    }

    
    
    
    /**
     *
     *
     * @param directory
     * @param filename
     * @param bucket
     * @param testingOnly
     * @return
     * @throws Exception
     *
    public Map<String,Object>  getFileInfoAws( String directory, String filename, int bucketTypeId ) throws Exception
    {
        BucketType bucketType = BucketType.getValue(bucketTypeId);

        Map<String,Object> out = new HashMap<>();
        
        out.put( "filename", filename );
        out.put( "directory", directory );
        out.put( "status", "SUCCESS" );
        
        //List<String> results = new ArrayList<>();
        //results.add( "SUCCESS" );
        //results.add( "" );
        //results.add( filename );
        //results.add( directory );
        //results.add( "" );
        //results.add( "" );

        initAwsTransferManager( true );

        long bytes = 0;
        Date createDate = null;
        String key = null;

        try
        {
            if( directory.startsWith( "/" ) )
                directory = directory.substring( 1, directory.length() );

            key = bucketType.getBaseKey() + directory  + "/" + filename;

            // LogService.logIt( "AwsFileUtils.getFileInfoAws " + key );

            ObjectMetadata omd = amazonS3Client.getObjectMetadata( bucketType.getBucket(), key );

            bytes = omd.getContentLength();

            createDate = omd.getLastModified();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFileInfoAws() File not found on AWS. " + key );

            out.put( "status", "ERROR" );
            out.put( "statusmessage",  e.getMessage() );
            //results.set( 0, "ERROR" );
            //results.set( 1, e.getMessage() );
            //throw new STException(e);
        }

        out.put( "length" , bytes );
        out.put( "date" , createDate );
        
        //results.add( Long.toString( bytes ) );
        //results.add( createDate == null ? "0" : Long.toString( createDate.getTime() ) );
        return out;
    }
    
    
    public Map<String,Object>  getFileInfoLocal( String directory, String filename, int bucketTypeId ) throws Exception
    {
        Map<String,Object> out = new HashMap<>();
        
        out.put( "filename", filename );
        out.put( "directory", directory );
        out.put( "status", "SUCCESS" );
        
        String pathAndFilename = null;
        
        try
        {
            String localFilesRoot = RuntimeConstants.getStringValue( "localFsRoot" );

            if( filename == null || filename.length() == 0 )
                throw new Exception( "No filename" );

            if( directory == null )
                directory = "";

            if( directory.length() == 0 || directory.charAt( 0 ) != '/' )
                directory = "/" + directory;

            //if( !path.startsWith( getFileTreeRootDirectory() ) )
            //    path = getFileTreeRootDirectory() + path;

            if( !directory.endsWith( "/" ) )
                directory += "/";
                        
            pathAndFilename = localFilesRoot + directory + filename;
            
            LogService.logIt( "FileXferUtils.getFileInfoLocal() pathAndFilename=" + pathAndFilename );
            
            File file = new File(  pathAndFilename );
            
            out.put( "length" , file.length() );
            out.put( "date" , null );
        
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FileXferUtils.getFileInfoLocal() File not found " + pathAndFilename );
        }
        return out;
    }
    */
    
    

    public static String removePathFromFilename( String filename )
    {
        if( filename == null || filename.length() == 0 )
            return filename;

        if( filename.contains("/") )
            filename = filename.substring( filename.lastIndexOf( "/" ) + 1, filename.length() );

        if( filename.contains("\\") )
            filename = filename.substring( filename.lastIndexOf( "\\" ) + 1, filename.length() );

        return filename;
    }

    public static String getFileExtension( String filename )
    {
        if( filename == null )
            return null;

        if( filename.lastIndexOf( "." ) <= 0 || filename.endsWith( "." ) )
            return null;

        return filename.substring( filename.lastIndexOf( "." ) + 1, filename.length() ).toLowerCase();
    }



}
