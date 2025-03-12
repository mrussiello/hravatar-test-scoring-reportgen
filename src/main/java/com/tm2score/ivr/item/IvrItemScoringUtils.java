/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr.item;

import com.tm2score.util.TextProcessingUtils;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class IvrItemScoringUtils {
    
    
    /**
     * confidenceFactor is = 1 if confidence 100 - 0.85, 
     *                       0.9 if confidence is 0.85 - 0.7, 
     *                       0.8 if confidence is 0.7 - 0.5; 
     *                       0.7 for confidence below 0.5
     * 
     * @param confidence
     * @return 
     */
    public static float getConfidenceFactor( float confidence )
    {
        if( confidence >= 0.85f )
            return 1;
        
        if( confidence >= 0.7f )
            return 0.9f;

        if( confidence >= 0.5f )
            return 0.8f;
        
        return 0.7f;
    }
    
    public static float getHighestTextSimilarity( List<String> speechTextVals, List<String> matchVals ) throws Exception
    {
        if( speechTextVals==null || speechTextVals.isEmpty() )
            return 0;
        
        if( matchVals==null || matchVals.isEmpty() )
            return 0;
                
        float maxSim = 0;
        
        float simVal;
        
        for( String inStr : speechTextVals )
        {
            if( inStr==null || inStr.trim().isEmpty() )
                continue;
            
            //LogService.logIt( "IvrItemScoringUtils.getHighestTextSimilarity() AAA inStr=" + inStr );
            inStr = removeNonAlphaNumSpace( inStr );

            //LogService.logIt( "IvrItemScoringUtils.getHighestTextSimilarity() BBB inStr=" + inStr );
            
            inStr = inStr.toLowerCase();
            
            for( String matchStr : matchVals )
            {
                if( matchStr==null || matchStr.trim().isEmpty() )
                    continue;
                
                matchStr = removeNonAlphaNumSpace( matchStr );
                
                matchStr = matchStr.toLowerCase();
                
                // LogService.logIt( "IvrItemScoringUtils.getHighestTextSimilarity() inStr=" + inStr + ", ")
                
               simVal = getHighestTestSimilarityVal( inStr, matchStr );
               
                if( simVal>=1f )
                    return 1;
            
               if( simVal>maxSim )
                   maxSim = simVal;
            }
        }
        
        return maxSim;
    }


    public static String getHighestTextSimilarityString( List<String> speechTextVals, List<String> matchVals, boolean returnMatchStr ) throws Exception
    {
        if( speechTextVals==null || speechTextVals.isEmpty() )
            return null;
        
        if( matchVals==null || matchVals.isEmpty() )
            return null;
                
        float maxSim = 0;
        
        String matchedStr = null;
        
        float simVal;
        
        for( String inStr : speechTextVals )
        {
            if( inStr==null || inStr.trim().isEmpty() )
                continue;
            
            //LogService.logIt( "IvrItemScoringUtils.getHighestTextSimilarity() AAA inStr=" + inStr );
            inStr = removeNonAlphaNumSpace( inStr );

            //LogService.logIt( "IvrItemScoringUtils.getHighestTextSimilarity() BBB inStr=" + inStr );
            
            inStr = inStr.toLowerCase();
            
            for( String matchStr : matchVals )
            {
                if( matchStr==null || matchStr.trim().isEmpty() )
                    continue;
                
                matchStr = removeNonAlphaNumSpace( matchStr );
                
                matchStr = matchStr.toLowerCase();
                
                // LogService.logIt( "IvrItemScoringUtils.getHighestTextSimilarity() inStr=" + inStr + ", ")
                
               simVal = getHighestTestSimilarityVal( inStr, matchStr );
               
                if( simVal>=1f )
                    return returnMatchStr ? matchStr : inStr;
            
               if( simVal>maxSim )
               {
                   maxSim = simVal;
                   matchedStr = returnMatchStr ? matchStr : inStr;
               }
            }
        }
        
        return matchedStr;
    }
    
    
    public static String removeNonAlphaNumSpace( String inStr )
    {
        if( inStr==null || inStr.isEmpty() )
            return inStr;
        
        inStr = inStr.trim();
        
        return inStr.replaceAll( "[^\\p{IsAlphabetic}^\\p{IsDigit}^ ]", "" );
    }
    
    public static float getHighestTestSimilarityVal( String str1, String str2 )
    {
        double[] vals = TextProcessingUtils.getTextSimilarityVals( str1, str2 );
        
        float maxVal = 0;
        
        for( double val : vals )
        {
            if( val>=1 )
                return 1;
            
            if( val>maxVal )
                maxVal = (float) val;
        }
        
        return maxVal;
    }
    

    public static boolean containsAnyMatchStr( List<String> speechTextVals, List<String> matchVals ) throws Exception
    {
        if( speechTextVals==null || speechTextVals.isEmpty() )
            return false;
        
        if( matchVals==null || matchVals.isEmpty() )
            return false;
                
        for( String inStr : speechTextVals )
        {
            if( inStr==null || inStr.trim().isEmpty() )
                continue;
            
            inStr = inStr.toLowerCase();
            
            for( String matchStr : matchVals )
            {
                if( matchStr==null || matchStr.trim().isEmpty() )
                    continue;
                
                matchStr = matchStr.toLowerCase();
                
                if( inStr.contains( matchStr ) )
                    return true;
            }
        }
        
        return false;
    }

    public static String getMatchedStr( List<String> speechTextVals, List<String> matchVals, boolean returnMatchVal ) throws Exception
    {
        if( speechTextVals==null || speechTextVals.isEmpty() )
            return null;
        
        if( matchVals==null || matchVals.isEmpty() )
            return null;
                
        for( String inStr : speechTextVals )
        {
            if( inStr==null || inStr.trim().isEmpty() )
                continue;
            
            // inStr = inStr.toLowerCase();
            
            for( String matchStr : matchVals )
            {
                if( matchStr==null || matchStr.trim().isEmpty() )
                    continue;
                
                // matchStr = matchStr.toLowerCase();
                
                if( inStr.toLowerCase().contains( matchStr.toLowerCase() ) )
                    return returnMatchVal ? matchStr : inStr;
            }
        }
        
        return null;
    }



    
    public static float containsPhrase( List<String> speechTextVals, List<String> matchVals ) throws Exception
    {
        if( speechTextVals==null || speechTextVals.isEmpty() )
            return 0;
        
        if( matchVals==null || matchVals.isEmpty() )
            return 0;
                
        for( String inStr : speechTextVals )
        {
            if( inStr==null || inStr.trim().isEmpty() )
                continue;
            
            inStr = inStr.toLowerCase();
            
            for( String matchStr : matchVals )
            {
                if( matchStr==null || matchStr.trim().isEmpty() )
                    continue;
                
                matchStr = matchStr.toLowerCase();
                
                if( inStr.contains( matchStr ) )
                    return 1;
            }
        }
        
        return getPhraseSimilarity( speechTextVals, matchVals );
    }

    
    public static float getPhraseSimilarity( List<String> speechTextVals, List<String> matchVals ) throws Exception
    {
        if( speechTextVals==null || speechTextVals.isEmpty() )
            return 0;
        
        if( matchVals==null || matchVals.isEmpty() )
            return 0;
                
        float maxSim = 0;        
        float simVal;
        
        for( String inStr : speechTextVals )
        {
            if( inStr==null || inStr.trim().isEmpty() )
                continue;
            
            for( String matchStr : matchVals )
            {
                if( matchStr==null || matchStr.trim().isEmpty() )
                    continue;
                
               simVal = getHighestPhraseSimilarityVal( inStr, matchStr );
               
                if( simVal>=1f )
                    return 1;
                           
               if( simVal>maxSim )
                   maxSim = simVal;
            }
        }
        
        return maxSim;
    }

    
    public static float getHighestPhraseSimilarityVal( String inStr, String matchStr ) throws Exception
    {
        int matchWordCt = StringUtils.numWords(matchStr);
        
        int inWordCt = StringUtils.numWords(inStr);
        
        if( inWordCt<= matchWordCt )
            return getHighestTestSimilarityVal( inStr, matchStr );
        
        float maxSim = 0;
        float simVal = 0;
                
        List<String> words = new ArrayList<>();
        
        for( String wd : inStr.split(" ") )
        {
            if( wd.trim().isEmpty() )
                continue;
            
            words.add(wd.trim());
        }
        
        StringBuilder sb;
        
        for( int i=0; i<words.size()-matchWordCt; i++ )
        {
            sb = new StringBuilder();
            
            for( int j=0; j<matchWordCt; j++ )
            {
                if( sb.length()>0 )
                    sb.append( " " );
                
                sb.append( words.get( i+j ) );
            }
            
            simVal = getHighestPhraseSimilarityVal( sb.toString(), matchStr );
            
            if( simVal>=1f )
                return 1;
            
            if( simVal>maxSim )
                maxSim = simVal;   
        }

        return maxSim;
    }
    
}
