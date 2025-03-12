/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.file;

import com.tm2score.service.LogService;
import com.tm2score.util.Base64Encoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 *
 * @author miker_000
 */
public class HttpFileUtils {
    
    
    public static String getBinaryFileAsBase64Str( String fileFullUrl ) throws Exception
    {
        return getBase64StringFromBytes( getBinaryFileAsBytes( fileFullUrl ) );
    }
    
    public static byte[] getBinaryFileAsBytes( String fileFullUrl ) throws Exception
    {
        return getBytesFromInputStream(getBinaryFileAsInputStream(fileFullUrl ) );
    }
    
    
    
    
    public static InputStream getBinaryFileAsInputStream( String fileFullUrl ) throws Exception
    {
        try
        {
            return new BufferedInputStream( com.tm2score.util.HttpUtils.getURLFromString(fileFullUrl).openStream() );
        }
        catch( Exception e )
        {
            LogService.logIt( "HttpFileUtils.getBinaryFileAsInputStream() " + e.toString() + ", unable to open fileFullUrl=" + fileFullUrl );
            
            if( fileFullUrl.toLowerCase().startsWith("https://"))
            {
                String u2 = "http" + fileFullUrl.substring(5, fileFullUrl.length() );
                try
                {
                    return new BufferedInputStream( com.tm2score.util.HttpUtils.getURLFromString(fileFullUrl).openStream() );
                }
                
                catch( Exception ee )
                {
                    LogService.logIt( "HttpFileUtils.getBinaryFileAsInputStream() Trying HTTP instead of HTTPS, " + e.toString() + ", unable to open fileFullUrl=" + u2 );
                }
            }
            throw e;
        }
    }
    
    
    public static String getBase64StringFromBytes( byte[] bytes )
    {
        if( bytes ==null || bytes.length==0 )
            return "";
        
        return new String( Base64Encoder.encode( bytes ) );     
    }
    
    
    public static byte[] getBytesFromInputStream( InputStream inStream )
    {
        try
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = inStream.read(data, 0, data.length)) != -1) {
              buffer.write(data, 0, nRead);
            }
            
            return buffer.toByteArray();

        }
        catch( Exception e )
        {
            LogService.logIt( e, "HttpFileUtils.getBytesFromInputStream() " );
            
            return null;
        }
    }
        
}
