/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.voicevibes;

import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.MessageFactory;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;

/**
 *
 * @author miker_000
 */
public class VoiceVibesScaleScore {
    
    VoiceVibesScaleType voiceVibesScaleType;
    
    JsonObject jsonObjectTop;
    int inBand;
    int aboveBand;
    int belowBand;
    int wordCount;
    
    Map<String,Integer> wordSpotMap;
    Locale locale;
    
    float score;

    public VoiceVibesScaleScore( VoiceVibesScaleType vvst, float score ) throws Exception
    {
        this.voiceVibesScaleType = vvst;
        this.score = score;
    }

    
    public VoiceVibesScaleScore( VoiceVibesScaleType vvst, JsonNumber scr, JsonObject jo, Locale loc, int wordCount ) throws Exception
    {
        this.voiceVibesScaleType = vvst;
        this.score = scr==null ? 0 : 10*(float) scr.doubleValue();
        this.jsonObjectTop=jo;
        this.locale = loc;
        this.wordCount=wordCount;
        
        // Pause is pause to talk ratio (%pause time over total time )
        // Ideal pause to talk is 32 - 45
        // Below 32 is to little, above is too much
        // make it so that 
        //      - 38 is best = score of 50 (best)
        //      - 32 = a 35
        //      - 45 = a 65
        if( vvst.equals( VoiceVibesScaleType.PAUSES ) )
        {
            // this.score = scr==null ? 0 : (float) scr.doubleValue();
            
            score = 50f + (15f/6f)*( ((float) scr.doubleValue()) - 38f );
            
            if( score>100 )
                score = 100;
            
            if( score<0 )
                score = 0;
        }

        // Pace is syllables per second. 
        // The ideal pace is 3.3 - 4.52 syllables per second.
        // below 3.3 is too slow.
        // above is too fast.
        // Make it so that:
        //   - 3.9=50 (best)
        //     3.3 = 35
        //     4.5 = 65
        if( vvst.equals( VoiceVibesScaleType.PACE ) )
        {
            // this.score = scr==null ? 0 : (float) scr.doubleValue();
            
            score = 50f + (15f/0.6f)*( ((float) scr.doubleValue()) - 3.9f );
            
            if( score>100 )
                score = 100;
            
            if( score<0 )
                score = 0;
        }
        
        readFromJsonObject();
    }
    
    public void setLocale( Locale l )
    {
        this.locale=l;
    }

    @Override
    public String toString() {
        return "VoiceVibesScaleScore{" + "voiceVibesScaleType=" + voiceVibesScaleType.getName() + ", score=" + score + '}';
    }
    
    public String getColorGraphImgUrlParams()
    {
        StringBuilder sb = new StringBuilder();
        
        //sb.append( "/ta/ivrvvscorechart/" + te.getTestEventIdEncrypted() + "_" + vss.getVoiceVibesScaleType().getVoiceVibesScaleTypeId() + ".png?" );

        String scoreStr = I18nUtils.getFormattedNumber( Locale.US, getScore(), 0 );
        sb.append("?v=" + getVoiceVibesScaleType().getVoiceVibesScaleTypeId() + "&s=" + scoreStr + "&tw=" + Constants.IVR_COLORGRAPHWID + "&h=" + Constants.IVR_COLORGRAPHHGT );
        
        return sb.toString();
    }
    
    
    private void readFromJsonObject() throws Exception
    {
        try
        {
            if( voiceVibesScaleType!=null && voiceVibesScaleType.containsObject() )
            {
                if( this.jsonObjectTop==null )
                    throw new Exception( "jsonObjectTop is null" );
                
                if( voiceVibesScaleType.isVariety() )
                {
                    inBand = jsonObjectTop.containsKey("inBand") ? jsonObjectTop.getInt("inBand") : 0;
                    aboveBand = jsonObjectTop.containsKey("aboveBand") ? jsonObjectTop.getInt("aboveBand") : 0;
                    belowBand = jsonObjectTop.containsKey("belowBand") ? jsonObjectTop.getInt("belowBand") : 0;
                    
                    // Mostly in band is great!
                    if( inBand >= 65 )
                        score=50;
                    
                    else if( inBand >= 50 )
                    {
                        if( aboveBand > belowBand  )
                            score = 55;
                        else 
                            score = 45;
                    }
                    
                    else if( inBand >= 40 ) 
                    {
                        if( aboveBand > belowBand  )
                            score = 70;
                        else 
                            score = 30;
                    }
                    
                    else if( inBand >= 25 )
                    {
                        if( aboveBand > belowBand  )
                            score = 80;
                        else 
                            score = 20;                        
                    }

                    else
                    {
                        if( aboveBand > belowBand  )
                            score = 90;
                        else 
                            score = 10;                        
                    }
                    
                    /*
                    else if( inBand+aboveBand+belowBand > 0 )
                    {
                        score = 0;
                        float core = Math.abs(inBand-50);
                        
                        if( core<=5 )
                            score +=50;
                        
                        else if( core<=10 )
                            score +=30;
                        
                        else if( core<=20 )
                            score +=10;
                        
                        core = Math.abs( aboveBand-belowBand );
                        
                        if( core<=4 )
                            score += 50;
                        
                        else if( core <=8 )
                            score += 30;
                        
                        else if( core <= 12 )
                            score+=10;
                    }
                    */
                }
                
                if( voiceVibesScaleType.isVibe() )
                {
                    // LogService.logIt( "VoiceVibesScaleScore.parseResult() jsonKey=" + voiceVibesScaleType.getJsonKey() + ", contains=" + jsonObjectTop.containsKey( voiceVibesScaleType.getJsonKey() ) );
                    // score = 10f* (jsonObjectTop.containsKey( voiceVibesScaleType.getJsonKey() ) && !jsonObjectTop.isNull( voiceVibesScaleType.getJsonKey() ) ? (float) jsonObjectTop.getJsonNumber( voiceVibesScaleType.getJsonKey() ).doubleValue() : 0 );
                    score = 10f* getJsonNumberFloat(jsonObjectTop, voiceVibesScaleType.getJsonKey(), 0 ); // && !jsonObjectTop.isNull( voiceVibesScaleType.getJsonKey() ) ? (float) jsonObjectTop.getJsonNumber( voiceVibesScaleType.getJsonKey() ).doubleValue() : 0 );
                }
                
                if( voiceVibesScaleType.isWordspot() )
                {
                    parseWordSpotMap();
                    
                    float wdSpotsPer100 = getTotalWordSpotsPer100();
                    
                    if( wdSpotsPer100 < 3 )
                        score =100;
                    
                    else if( wdSpotsPer100 >= 20 )
                        score=0;
                    
                    else
                    {
                        wdSpotsPer100 -=3;
    
                        score = 100f - wdSpotsPer100*100f/17f;
                    }
                } 
                
                
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "VoiceVibesScaleScore.readFromJsonObject() " );            
        }
    }

    
    public float getJsonNumberFloat( JsonObject jo, String key, float defaultVal )
    {
        if( jo!=null && jo.containsKey(key) && !jo.isNull( key ) )
        {
            try
            {
                return (float) jo.getJsonNumber(key).doubleValue();            
            }
            catch( Exception e )
            {
                LogService.logIt( e, "VoiceVibesScaleScore.getJsonNumberFloat() key=" + key + ", jsonObject=" + JsonUtils.getJsonObjectAsString(jo) );
            }
            
        }
        return defaultVal;
    }
    
    public Map<String, Integer> getWordSpotMap() {
        return wordSpotMap;
    }

    public VoiceVibesScaleType getVoiceVibesScaleType() {
        return voiceVibesScaleType;
    }

    public float getScore() {
        return score;
    }
        
    
    
    public float getTotalWordSpotsPer100() throws Exception
    {
        Map<String,Float> map = getWordSpotMapPer100();
        
        Float v;
        
        float total = 0;
        
        for( String key : map.keySet() )
        {
            v = map.get( key );
            
            total += v.floatValue();
        }
        
        return total;
    }
    
    public Map<String,Float> getWordSpotMapPer100() throws Exception
    {
        if( wordSpotMap==null )
            throw new Exception( "wordSpotMap is null. Cannot convert anything to words per 100" );
                
        Map<String,Float> out = new HashMap<>();
        
        Integer val;
        float per100Words = 0;
        
        for( String key : wordSpotMap.keySet() )
        {
            val = wordSpotMap.get(key);
            
            if( val==null )
                continue;
            
            per100Words = wordCount>0 ? 100*val/wordCount : val;
            
            out.put( key, new Float(per100Words) );            
        }

        return out;        
    }


    private void parseWordSpotMap() throws Exception
    {
        wordSpotMap = new HashMap<>();
        
        for( VoiceVibesWordSpotType vvwst : VoiceVibesWordSpotType.values() )
        {
            if( !jsonObjectTop.containsKey( vvwst.getJsonKey() ) )
                continue;
            
            wordSpotMap.put( vvwst.getJsonKey(), new Integer(jsonObjectTop.getInt( vvwst.getJsonKey() )) );
        }
    }


    
    public int getTotalWordSpotCount() throws Exception
    {
        if( wordSpotMap ==null || wordSpotMap.isEmpty() )
            return 0;

        int ct = 0;
        
        for( Integer c : wordSpotMap.values() )
            ct += c;
        
        return ct;
    }
    
    
    public String getSystemName()
    {
        return voiceVibesScaleType==null ? "Unknown - Null" : voiceVibesScaleType.getName();
    }

    public String getLocalizedName()
    {
        return  voiceVibesScaleType==null ? "Unknown - Null" : MessageFactory.getStringMessage(locale, voiceVibesScaleType.getLangKey(), null );
    }

    public void setWordSpotMap(Map<String, Integer> wordSpotMap) {
        this.wordSpotMap = wordSpotMap;
    }
    
    public String getScaleLowKey()
    {
        return this.getVoiceVibesScaleType().getScaleLowKey();
    }
    public String getScaleHighKey()
    {
        return this.getVoiceVibesScaleType().getScaleHighKey();
    }
    
    
}
