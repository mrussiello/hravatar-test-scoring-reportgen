/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.bot;

import com.tm2score.entity.bot.BotInstance;
import com.tm2score.entity.bot.BotIntent;
import com.tm2score.service.LogService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Format of Score String is:
 * 
 * {  
 * 
 *    totalpoints: totalPoints,
 *    totalmessages: totalMessages,
 *    competencies: [ {name: competencyName, points: points, empathy:0 or 1} ]  
 * }
 * 
 * 
 * 
 * @author miker_000
 */
public class BotScoreUtils {
    
    public static float DEF_SCALED_MEAN = 65;
    public static float DEF_SCALED_SD = 15;
    
    public static float DEF_SPELLING_ERROR_MEAN = 1.5f;
    public static float DEF_SPELLING_ERROR_SD = 0.5f;
    
    public static float DEF_RESPTIME_MEAN = 25f;
    public static float DEF_RESPTIME_SD = 10f;
    
    public static float DEF_RAPPORT_MEAN = 1f;
    public static float DEF_RAPPORT_SD = 0.6f;
    
    public static float MAX_SCORE_DROP_PER_NEGATIVE = 15;
    
    
    public static long parseBotEventId( String imoScoreStr ) throws Exception
    {
        try
        {
            if( imoScoreStr==null || imoScoreStr.trim().isEmpty() || imoScoreStr.indexOf( ";" )<0  )
                throw new Exception( "imoScoreStr is not properly formatted. Expected iframeitemtypeid;boteventid but got " + imoScoreStr );
            
            return Long.parseLong( imoScoreStr.substring( imoScoreStr.indexOf(";")+1, imoScoreStr.length() ).trim() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BotScoreUtils.parseBotEventId() imoScoreStr=" + imoScoreStr );
            throw e;
        }
    }
    
    
    public static float[] getScaledMeanStdev( BotInstance bi )
    {
        if( bi.getFloatParam11()>0 )
            return new float[] { bi.getFloatParam10(), bi.getFloatParam11() };
        
        return new float[] { DEF_SCALED_MEAN, DEF_SCALED_SD };
    }
    
    
    /**
     * String = competency name 
     * float[0] = points
     * float[1] = max points
     * float[2] = assigned points
     * float[3] = percent of max
     * float[4] = z-score
     * float[5] = scaled score
     * float[6] = weight
     * 
     * 
     * @param bil
     * @param cr
     * @return
     * @throws Exception 
     */
    public static Map<String,float[]> computeCompetencyScoreMap( List<BotInstance> bil, ChatResponse cr  ) throws Exception
    {
        Map<String,float[]> out = new TreeMap<>();
        
        if( cr==null )
            return out;
        
        if( bil==null || bil.isEmpty() )
            throw new Exception( "BotScoreUtils.computeCompetencyScoreMap() BotInstanceList is null or empty." );
                
        /*
         Map <Competency Name, float[] where 
         float[0] = max points
         float[1] = weight
         float[2] = mean
         float[3] = standard deviation        
        */
        Map<String,float[]> competencyConfigMap = getCompetencyConfigMap( bil );
        
        BotInstance bi = bil.get(0);
        
        float totalPts;
        float assignedPts;
        
        float pctMaxPts;
        float raw;
        float score;
        float[] configVals;
        float[] scrVals;
        float[] scaleVals = getScaledMeanStdev( bi );
        float scaleMean = scaleVals[0];
        float scaleSd = scaleVals[1];
        
        for( String c : competencyConfigMap.keySet() )
        {
            raw=0;
            score=0;            
            totalPts = getPointsForCompetency( bil, c, cr );
            assignedPts = totalPts;
            configVals = competencyConfigMap.get(c);

            // no config vals (bad) create from scratch.
            if( configVals==null )
            {
                configVals = getDefaultConfigValsForCompetency( c , bil );
                LogService.logIt( "BotScoreUtils.computeCompetencyScoreMap() No config info found. Using defaults for competency=" + c + ", maxPoints=" + configVals[0] );
            }

            // config vals doesn't have max points set, so compute it.
            else if( configVals[0]<=0 )
            {
                configVals[0] = computeMaxPointsForCompetency( bil, c );
                LogService.logIt( "BotScoreUtils.computeCompetencyScoreMap() Re-computed max Points for competency=" + c + ", maxPoints=" + configVals[0] );                
            }


            //if max points present
            if( configVals[0]>0 )
                assignedPts = Math.min( totalPts, configVals[0] );

            pctMaxPts = configVals[0]>0 ? 100f*assignedPts/configVals[0] : 100f;

            LogService.logIt( "BotScoreUtils.computeCompetencyScoreMap() AAA.2 c=" + c + ", totalPoints=" + totalPts + ", assignedPts=" + assignedPts + ", pctMaxPts=" + pctMaxPts + ", stdev=" + configVals[3] );
            // stdev > 0
            if( configVals[3]> 0 )
            {
                // Convert to Z
                raw = (totalPts - configVals[2])/configVals[3];

                // Convert to scaled
                score = (scaleMean * raw) + scaleSd;
            }

            else
            {
                raw = pctMaxPts;
                score = pctMaxPts;
            }

            LogService.logIt( "BotScoreUtils.computeCompetencyScoreMap() AAA.3 c=" + c + ", totalPoints=" + totalPts + ", assignedPts=" + assignedPts + ", raw=" + raw + ", score=" + score );

            scrVals = new float[] {totalPts,configVals[0],assignedPts,pctMaxPts,raw,score,configVals[1]};

            out.put( c, scrVals );
        }
        
        return out;
    }
    
    
    /*
     Map <Competency Name, float[] where 
     float[0] = max points
     float[1] = weight
     float[2] = mean
     float[3] = standard deviation        
    */
    private static Map<String,float[]> getCompetencyConfigMap( List<BotInstance> bil ) throws Exception
    {
        
        Map<String,float[]> out = new HashMap<>();
        
        float[] vals;
        float[] valsX;
        
        Map<String,float[]> ccm;
        
        for( BotInstance bi : bil )
        {
            /*
             Map <Competency Name, float[] where 
             float[0] = max points
             float[1] = weight
             float[2] = mean
             float[3] = standard deviation        
            */
            ccm = bi.getCompetencyConfigMap();
            
            for( String c : ccm.keySet() )
            {
                valsX = ccm.get(c);                
                vals = out.get(c);
                
                // first time.
                if( vals == null )
                {
                    out.put( c, valsX );
                    continue;
                }

                // need to combine max points (add)
                vals[0] += valsX[0];
                
                // No Weight defined, but new one has Weight defined.
                if( vals[1]<=0 && valsX[1]>0 )
                    vals[1] = valsX[1];
                
                // No STDev defined, but new one has STDev defined.
                if( vals[3]<=0 && valsX[3]>0 )
                {
                    vals[2] = valsX[2];  // mean
                    vals[3] = valsX[3];  // stdev                  
                }
            }
        }
        
        return out;
    }
    
    
    private static float getPointsForCompetency( List<BotInstance> bil, String c, ChatResponse cr )
    {
        Map<BotIntent,Integer> hitsPerIntentMap = new HashMap<>();
        
        if( c==null || c.trim().isEmpty() || cr==null )
            return 0;
        
        if( cr.getChatMessageList()==null )
            return 0;
        
        float points = 0;
        
        BotIntent intent;
        Integer intentHits;
        int maxIntentHits;
        
        for( ChatMessage cm : cr.getChatMessageList() )
        {
            if( cm.getType()!=ChatMessageType.BOT_MSG.getChatMessageTypeId() )
                continue;
            
            // LogService.logIt( "BotScoreUtils.getPointsForCompetency() AAA.1 comp=" + c + ", isMatch=" + cm.getIsCompetencyMatch( c ) );
            
            if( !cm.getIsCompetencyMatch( c ) )
                continue;
            
            intent = cm.getIntent()==null || cm.getIntent().isEmpty() ? null : getBotIntentForName( cm, bil );
            
            if( intent == null )
            {
                LogService.logIt( "BotScoreUtils.getPointsForCompetency() AAA.2 comp=" + c + ", Could not find intent " + cm.getIntent() + ", " + cm.toString() );
            }
            
            else if( intent.getIntParam1()>0 )
            {
                // LogService.logIt( "BotScoreUtils.getPointsForCompetency() AAA.3 comp=" + c + ", checking intentHits" );
                intentHits = hitsPerIntentMap.get(intent);
                
                if( intentHits==null )
                    intentHits = new Integer(0);
                
                intentHits++;
                
                hitsPerIntentMap.put(intent, intentHits );
                
                maxIntentHits = intent.getIntParam1()<=0 ? 1 : intent.getIntParam1();
                
                LogService.logIt( "BotScoreUtils.getPointsForCompetency() AAA.4 comp=" + c + ", intentHits=" + intentHits + ", maxIntentHits=" + maxIntentHits );
                
                if( intentHits > maxIntentHits )
                    continue;                
            }
            
            // LogService.logIt( "BotScoreUtils.getPointsForCompetency() AAA.4 comp=" + c + ", adding points=" + cm.getPoints() + " total points=" + points );
            
            points += cm.getPoints();
        }
        
        LogService.logIt( "BotScoreUtils.getPointsForCompetency() AAA.5 comp=" + c + ", total points=" + points );
        return points;        
    }
    
    private static BotIntent getBotIntentForName( ChatMessage cm, List<BotInstance> bil )
    {
        if( cm==null || bil==null )
            return null;
        
        for( BotInstance bi : bil )
        {
            if( cm.getBotInstanceId()==bi.getBotInstanceId() )
                return bi.getBotIntentForName( cm.getIntent() );
        }
        
        return null;
    }
    
        /*
         Map <Competency Name, float[] where 
         float[0] = max points
         float[1] = weight
         float[2] = mean
         float[3] = standard deviation        
        */
    private static float[] getDefaultConfigValsForCompetency( String c, List<BotInstance> bil )
    {
        float maxPts = computeMaxPointsForCompetency( bil, c );
        float mean = 0.5f*maxPts;
        float sd = 1;
        float weight=0;        
        return new float[] {maxPts,weight,mean,sd};
    }
    
    
    private static float computeMaxPointsForCompetency( List<BotInstance> bil, String c )
    {
        if( c==null || c.trim().isEmpty() )
            return 0;
        
        float maxPts = 0;
        float intentMaxPts = 0;
        
        for( BotInstance bi : bil )
        {
            for( BotIntent intent : bi.getBotIntentList() )
            {
                if( !intent.getIsCompetencyMatch( c ) )
                    continue;

                // assume max times for an intent is 1
                intentMaxPts = intent.getPoints();

                // Adjust for max hits
                if( intent.getIntParam1() >0 )
                    intentMaxPts *= intent.getIntParam1();

                maxPts += intentMaxPts;
            }
        }
        
        return maxPts;
    }
    
    
    
}
