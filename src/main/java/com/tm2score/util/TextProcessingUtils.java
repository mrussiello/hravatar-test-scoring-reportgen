/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

import com.tm2score.service.LogService;
import net.ricecode.similarity.LevenshteinDistanceStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

/**
 *
 * @author miker_000
 */
public class TextProcessingUtils {
    
    public static float getTextSimilarity( String str1, String str2 )
    {
        double out = 1;

        try
        {
            double[] vals =  getTextSimilarityVals( str1, str2 );
            out = vals[2];
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TextProcessingUtils.getTextSimilarity() STR1=" + str1 + ", STR2=" + str2 );
        }

        return (float) out;
    }


    
    public static double getTextSimilarityVal( String str1, String str2 )
    {
        double val = 0;

        if( str1==null || str1.isEmpty() || str2 == null || str2.isEmpty() )
            return val;


        try
        {
            SimilarityStrategy strategy = new LevenshteinDistanceStrategy();
            StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
            val = service.score(str1, str2);

            // LogService.logIt( "EssayUtils.getTextSimilarityVals() levensh=" + val );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TextProcessingUtils.getTextSimilarityVals() STR1=" + str1 + ", STR2=" + str2 );
        }

        return val;
    }


    public static double[] getTextSimilarityVals( String str1, String str2 )
    {
        double[] vals = new double[3];

        if( str1==null || str1.isEmpty() || str2 == null || str2.isEmpty() )
            return vals;


        try
        {
            // SimilarityStrategy strategy = new JaroStrategy();
            // StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
            //vals[0] = service.score(str1, str2);

            //strategy = new JaroWinklerStrategy();
            //service = new StringSimilarityServiceImpl(strategy);
            //vals[1] = service.score(str1, str2);

            SimilarityStrategy strategy = new LevenshteinDistanceStrategy();
            StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
            vals[2] = service.score(str1, str2);

            // LogService.logIt( "EssayUtils.getTextSimilarityVals() str1=" + str1 + ", str2=" + str2 + " Jaro=" + vals[0] + ", J-W=" + vals[1] + ", levensh=" + vals[2] );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TextProcessingUtils.getTextSimilarityVals() STR1=" + str1 + ", STR2=" + str2 );
        }

        return vals;
    }
    
    
}
