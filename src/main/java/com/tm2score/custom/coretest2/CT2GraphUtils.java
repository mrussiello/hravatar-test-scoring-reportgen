/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.custom.coretest2;

import com.tm2score.event.ScoreFormatType;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import javax.imageio.ImageIO;

/**
 *
 * @author miker
 */
public class CT2GraphUtils {
    
    // private static int DEFAULT_HEIGHT = 30;
    private static final int DEFAULT_WIDTH = 236;
    private static final int GRAPH_WIDTH = 228;
    private static final int COLOR_Y_OFFSET = 3;    
    private static final int X_OFFSET = 4;
    private static final float POINTER_WIDTH = 3f;
    private static final int POINTER_HEIGHT = 12;

    private static String BASE_GRAPHIC_URL = null;
    private static String BASE_DOWNARROW_URL = null;
    private static BufferedImage BASE_GRAPHIC_BIMG = null;
    private static BufferedImage BASE_DOWNARROW_BIMG = null;

    private synchronized static void initVals()
    {
        if( BASE_GRAPHIC_URL!=null )
            return;
        
        BASE_GRAPHIC_URL = RuntimeConstants.getStringValue( "bell-rainbow-base-graphic-url");
        BASE_DOWNARROW_URL = RuntimeConstants.getStringValue( "bell-rainbow-base-downarrow-url");
        
        try
        {
            BASE_GRAPHIC_BIMG = ImageIO.read( com.tm2score.util.HttpUtils.getURLFromString(BASE_GRAPHIC_URL) );
            BASE_DOWNARROW_BIMG = ImageIO.read( com.tm2score.util.HttpUtils.getURLFromString(BASE_DOWNARROW_URL) );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT2BellGraphUtils.initVals() Loading Base graphic. URL=" + BASE_GRAPHIC_URL );
        }
    }
    
    public static float convertProfileBoundary( float val )
    {
        return GRAPH_WIDTH*(val/100f) - 1;
    }
    
    public static float convertScoreToPtr( float score )
    {
        return GRAPH_WIDTH*(score/100f) - POINTER_WIDTH - 1;
    }
    
    public static BufferedImage getBellGraphImage( int ptr, int profileLow, int profileHigh )
    {
        try
        {
            if( 1==2 )
            {
                profileLow = (int)convertScoreToPtr(35 );
                profileHigh = (int)convertScoreToPtr(85 );
                ptr = (int)convertScoreToPtr( 65 );
            }
            
            initVals();

            //if( ptr > -100 )
            //{
            //    float pa = ((float)ptr)*(228f/220f);
            //    ptr = Math.round( pa + 4 - POINTER_WIDTH );
            //}
                    
            boolean hasProfile = profileLow>=0 && profileLow< profileHigh;
            
            ScoreFormatType sft = ScoreFormatType.NUMERIC_0_TO_100;
            
            // size in pixels
            int totalHeight;
            int totalWid = DEFAULT_WIDTH;

            BufferedImage pointerImage = new BufferedImage(BASE_DOWNARROW_BIMG.getWidth(), BASE_DOWNARROW_BIMG.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = pointerImage.createGraphics();
            g2.drawImage(BASE_DOWNARROW_BIMG,0, 0, null); 
            
            BufferedImage rainbowImage = new BufferedImage(BASE_GRAPHIC_BIMG.getWidth(), BASE_GRAPHIC_BIMG.getHeight(), BufferedImage.TYPE_INT_ARGB);
            g2 = rainbowImage.createGraphics();
            g2.setColor( Color.WHITE );
            g2.fillRect(0, 0, rainbowImage.getWidth(), rainbowImage.getHeight() );
            
            // Draw the original image onto the new image
            g2.drawImage(BASE_GRAPHIC_BIMG, 0, 0, null); 
            
            int textHeight = 11; // (int)(11*(((float)rainbowImage.getHeight())/28f));
            
            int extraHeight = hasProfile ? 6 : 0;

            totalHeight=rainbowImage.getHeight() + textHeight + extraHeight;

            // LogService.logIt( "CT2BellGraphUtils.getBellGraphImage() Loaded Base graphic. graphic=" + rainbowImage.getWidth() + "x" + rainbowImage.getHeight() + ", textHeight=" + textHeight + ", total Height=" + totalHeight );
            
            // int tickmarkHeight = 2;
            
            // Create a new image with the extra height
            BufferedImage fullImage = new BufferedImage(totalWid, totalHeight, BufferedImage.TYPE_INT_RGB);

            g2 = fullImage.createGraphics();

            g2.setColor( Color.WHITE );
            g2.fillRect(0, 0, totalWid, totalHeight );
            
            
            // Draw the original image onto the new image
            g2.drawImage(rainbowImage, X_OFFSET, extraHeight, null); 
                 
            // rainbowImage = fullImage;
            
            if( hasProfile )
            {                
                int barht = 3;
                int barYAdj = 0;

                g2.setColor( getProfileColor() );
                g2.fillRect(X_OFFSET + profileLow, COLOR_Y_OFFSET + barYAdj - 1, profileHigh - profileLow, barht );
            }

            g2.setColor( Color.GRAY );
            Stroke bs = new BasicStroke(0.5f);
            g2.setStroke(bs);

            // draw tick marks
            // int tickWid = (int) (((float)totalWid)/((float)sft.getNumberOfGraphicAxisPoints()-1) );
            //for( int i=0;i<sft.getNumberOfGraphicAxisPoints()-1;i++ )
            //{
            //    g2.drawLine(X_OFFSET + i*tickWid, 0, X_OFFSET + i*tickWid, BellGraphicsServlet.COLOR_Y_OFFSET + BellGraphicsServlet.COLOR_HEIGHT );
            //}

            // final line
            //g2.drawLine(X_OFFSET + totalWid, 0, X_OFFSET + totalWid, BellGraphicsServlet.COLOR_Y_OFFSET + BellGraphicsServlet.COLOR_HEIGHT );

            // bottom line
            g2.drawLine(X_OFFSET, totalHeight - textHeight, totalWid - X_OFFSET -1, totalHeight - textHeight );



            // set gradient font of text to be converted to image
            //GradientPaint gradientPaint = new GradientPaint(10, 5, Color.BLUE, 20, 10, Color.LIGHT_GRAY, true);
            //g.setPaint(gradientPaint);
            g2.setColor( Color.GRAY );

            // Font font = new Font("Arial", Font.BOLD, 9);
            Font font = new Font("Arial", Font.PLAIN, 9);
            g2.setFont(font);

            String[] tickVals = new String[] {"0","35","50","65","80","100"}; // sft.getTickVals();

            g2.drawString( tickVals[0], X_OFFSET, totalHeight);

            // 35
            float tv = (35f/100f)*GRAPH_WIDTH - 5;
            g2.drawString( tickVals[1], X_OFFSET + tv, totalHeight);

            // 50
            tv = (50f/100f)*GRAPH_WIDTH - 5;
            g2.drawString( tickVals[2], X_OFFSET + tv, totalHeight);

            // 65
            tv = (65f/100f)*GRAPH_WIDTH - 5;
            g2.drawString( tickVals[3], X_OFFSET + tv, totalHeight);

            // 80
            tv = (80f/100f)*GRAPH_WIDTH - 5;
            g2.drawString( tickVals[4], X_OFFSET + tv, totalHeight);
            
            // for( int i=1;i<sft.getNumberOfGraphicAxisPoints()-1;i++ )
            //for( int i=1;i<5;i++ )
            //{
            //    // g2.drawString( Integer.toString(i*20), X_OFFSET + i*tickWid-5, totalHgt);
            //    g2.drawString( tickVals[i], X_OFFSET + i*tickWid-5, totalHeight);
            //}

            
            // g2.drawString( "100", X_OFFSET + totalWid-16, totalHgt);
            // 100
            g2.drawString( tickVals[5], X_OFFSET + totalWid-sft.getLastTickValWidth()-6, totalHeight);

            // X_OFFSET -> 4 from 3
            // Polygon p = new Polygon( new int[] {X_OFFSET + 1 + 0+ptr,X_OFFSET  + 1+ 6+ptr,X_OFFSET + 1 + 3+ptr}, new int[] {0,0,10}, 3 );
            //

            if( ptr>=-1*X_OFFSET )
            {
                g2.drawImage(pointerImage, X_OFFSET + 1 + 0 + ptr, extraHeight + 3, null); 
                
                //Polygon p = new Polygon( new int[] {X_OFFSET + 1 + 0+ptr,(int)(X_OFFSET+1+2*POINTER_WIDTH+ptr),(int)(X_OFFSET+1 + POINTER_WIDTH+ptr)}, new int[] {extraHeight,extraHeight,POINTER_HEIGHT+extraHeight}, 3 );

                //g2.setColor( Color.BLACK );

                //g2.fillPolygon(p);
            }
            
            
            if( 1==2 && ptr>=0 )
            {
                Polygon p = new Polygon( new int[] {X_OFFSET + 1 + 0+ptr,(int)(X_OFFSET+1+2*POINTER_WIDTH+ptr),(int)(X_OFFSET+1 + POINTER_WIDTH+ptr)}, new int[] {extraHeight,extraHeight,POINTER_HEIGHT+extraHeight}, 3 );

                g2.setColor( Color.BLACK );

                g2.fillPolygon(p);
            }

            // release resources used by graphics context
            g2.dispose();

            Image i2 = makeColorTransparent(fullImage, Color.WHITE );

            return toBufferedImage(i2);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT2GraphUtils.getBellGraphImage() ptr=" + ptr );
            throw e;
        }        
    }

    public static Color getProfileColor()
    {
        return Color.decode( "#0077cc" );
    }



    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }



    public static Image makeColorTransparent(Image im, final Color color) 
    {
        ImageFilter filter = new RGBImageFilter() 
        {
          // the color we are looking for... Alpha bits are set to opaque
          public int markerRGB = color.getRGB() | 0xFF000000;

          @Override
          public final int filterRGB(int x, int y, int rgb) 
          {
            if ((rgb | 0xFF000000) == markerRGB) {
              // Mark the alpha bits as zero - transparent
              return 0x00FFFFFF & rgb;
            } else 
            {
              // nothing to do
              return rgb;
            }
          }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
      }
    
    
    
}
