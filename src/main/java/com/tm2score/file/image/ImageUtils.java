package com.tm2score.file.image;

import com.tm2score.file.FileContentType;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUtils
{

public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException 
{
    Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
    BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
    return outputImage;
}    
    
    
    
    
 public static int[] getImageDims( byte[] bytes, String filename, FileContentType fileContentType ) throws Exception
    {
        try
        {
            int[] out = new int[2];

            // now check image info
            ImageInfo imageInfo = new ImageInfo();

            imageInfo.setInput( new ByteArrayInputStream( bytes ) );

            imageInfo.setDetermineImageNumber( true ); // default is false

            imageInfo.setCollectComments( true ); // default is false


            if( !imageInfo.check() )
            {
                LogService.logIt( "ImageInfo: Not a supported image file format. " + filename + ", " + fileContentType.getBaseContentType() + " , using Toolkit" );

                Image img = Toolkit.getDefaultToolkit().createImage( bytes );

                out[0] =  img.getWidth( null );
                out[1] = img.getHeight( null );
            }

            else
            {
                out[0] = imageInfo.getWidth();
                out[1] = imageInfo.getHeight();
            }

            LogService.logIt( "FileUtils.getImageDims() " + filename + ", width=" + out[0] + ", height=" + out[1] );

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getImageDims() " + filename + ", " + fileContentType.getBaseContentType() + " " + bytes.length );

            throw new STException( e );
        }
    }


    public static BufferedImage getBufferedImageFromUrl( String url ) throws Exception
    {
        HttpURLConnection connection = null;
        try 
        {
            connection = (HttpURLConnection) com.tm2score.util.HttpUtils.getURLFromString(url).openConnection();
            connection.connect();
            BufferedImage image = ImageIO.read(connection.getInputStream());
            connection.disconnect();
            return image;
        } 
        catch (IOException e) 
        {
            LogService.logIt( e, "ImageUtils.getBufferedImageFromUrl() url=" + url );
            throw e;
        }        
    }


    public static byte[] getBytesFromBufferedImage( BufferedImage image, String format ) throws Exception
    {
    	String fmt = "jpg";

    	if( format.toLowerCase().endsWith( "png" ) )
    		fmt = "png";

    	else if( format.toLowerCase().endsWith( "gif" ) )
    		fmt = "gif";

    	else if( format.toLowerCase().endsWith( "bmp" ) )
    		fmt = "bmp";

    	// default is jpg

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        /*
        Graphics2D graphics2D = image.createGraphics();

        graphics2D.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );

        graphics2D.drawImage( image, 0, 0, null );
        // save thumbnail image to out stream
         *
         */

        // LogService.logIt( "ImageUtils.getBytesFromBufferedImage() format " + fmt );

        ImageIO.write( image, fmt, result );

        return result.toByteArray();

    }

    public static BufferedImage getBufferedImageFromBytes( byte[] bytes ) throws Exception
    {
    	//convert byte array back to BufferedImage
    	InputStream in = new ByteArrayInputStream(bytes);
    	return ImageIO.read(in);
    }
}