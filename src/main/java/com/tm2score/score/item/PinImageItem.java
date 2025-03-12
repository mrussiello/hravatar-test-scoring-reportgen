/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.item;

import com.tm2score.service.LogService;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class PinImageItem {

    float[] coords;
    
    private Point respPt;
    
    private Point[] points;
    
    float respTime;
    
    boolean isCorrect = false;
    
    boolean hasValidScore = false;
    
    

    public PinImageItem( String keyStr, String respStr )
    {
        parseKeyStr(keyStr);
        parseResponseStr( respStr );

        // LogService.logIt( "DataEntryItem() " + key + ", typed=" + typed + ", secs=" + secs );
    }
    
    public float getResponseTime()
    {
        if( hasValidScore )
            return respTime;
        
        return 0;
    }
    
    
    private void parseResponseStr( String respStr )
    {
        try
        {
            respPt = new Point(-1,-1);

            respTime=0;

            if( respStr == null || respStr.isEmpty() )
                return;

            int xx = respStr.indexOf( ',' );

            if( xx< 1 || xx == respStr.length()-1)
                    return;

            respPt.x = Integer.parseInt( respStr.substring( 0,xx ) );

            int yx = respStr.indexOf( ',',xx+1 );

            if( yx<0 )
            {
                respPt.y = Integer.parseInt( respStr.substring( xx+1,respStr.length() ) );
                respTime=0;
            }

            else
            {
                respPt.y = Integer.parseInt( respStr.substring( xx+1,yx ) );

                if( yx < respStr.length()-1 )
                    respTime = Float.parseFloat( respStr.substring( yx+1, respStr.length() ) );
            }
            
            // LogService.logIt( "PinImageItem.parseResponseStr() parsed " + respStr  + ", to " + respPt.x + "," + respPt.y + "  tm=" + respTime );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "PinImageItem.parseResponseStr() " + respStr );
            
            respPt = new Point(-1,-1);

            respTime=0;            
        }
        
    }    
    
    public boolean getIsCorrect()
    {
        return this.isCorrect;
    }
    
    /**
     * KeyStr format is x1,y1,x2,y2,x3,y3,etc
     * @param inStr 
     */
    private void parseKeyStr( String keyStr )
    {
        try
        {
            if( keyStr == null || keyStr.isEmpty() )
            {
                points = new Point[0];
                return;
            }

            String[] crds = keyStr.split(",");

            List<Integer> sl = new ArrayList<>();

            for( int i=0;i<crds.length;i++ )
            {
                if( crds[i]==null || crds[i].trim().isEmpty() )
                    continue;

                sl.add( new Integer(crds[i]));
            }

            List<Point> pts = new ArrayList<>();

            for( int i=0; i<sl.size()-1; i+=2 )
            {
                pts.add( new Point( sl.get(i),sl.get(i+1) ) );            
            }

            points = new Point[pts.size()];

            points = (Point[]) pts.toArray(points);  
            
           //  LogService.logIt( "PinImageItem.parseKeyStr() parsed " + points.length  + " points." );            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "PinImageItem.parseKeyStr() " + keyStr );
            points = new Point[0];
        }
        
    }

    private boolean containsPoint(Point test) 
    {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++) 
        {
            if ((points[i].y > test.y) != (points[j].y > test.y) &&
                (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y-points[i].y) + points[i].x)) 
            {
                result = !result;
            }
        }
        
        return result;
    } 
    
    
    public void calculate()
    {
        // LogService.logIt( "PinImageItem.calculate() " );
        hasValidScore = false;
        isCorrect = false;

        if( respPt == null || respPt.x<0 || respPt.y<0 || points==null || points.length<3 )
            return;
        
        try
        {
            isCorrect = containsPoint(respPt);
            hasValidScore = true;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "PinImageItem.calculate() "  );
            hasValidScore = false;
        }

        LogService.logIt( "PinImageItem.calculate() hasValidScore=" + hasValidScore + ", isCorrect=" + isCorrect );
    }


    public boolean getHasValidScore() {
        return hasValidScore;
    }

}


