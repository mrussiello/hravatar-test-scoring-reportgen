/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.sim;

import com.tm2score.service.LogService;


/**
 *
 * @author Mike
 */
public class InteractionScoreUtils
{
    public static boolean hasAnyPointsValues( float[] ptsArray )
    {
        if( ptsArray == null || ptsArray.length==0 )
            return false;

        for( float f : ptsArray )
        {
            if( f != 0 )
                return true;
        }

        return false;
    }

    /**
     * Converts string to array of numbers.
     * 
     * @param ptsStr
     * @return 
     */
    public static float[] getPointsArray( String ptsStr )
    {
        float[] out = new float[4];

        if( ptsStr == null || ptsStr.isEmpty() )
            return out;

        String[] ps = ptsStr.split(",");

        for( int i=0;i<ps.length && i<4; i++ )
        {
            try
            {
                out[i] = Float.parseFloat( ps[i] );
            }
            catch( NumberFormatException e )
            {
                LogService.logIt(e, "InteractionPointsEstimator.getPointsArray() i=" + i + ", " + ps[i] + ", full string=" + ptsStr );
            }
        }

        return out;
    }


}
