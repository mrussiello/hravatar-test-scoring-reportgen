/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.voicevibes;

import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.MergableScoreObjectType;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

/**
 *

{
   "data":{
      "recordingId":"20b73c2b-1ca1-4c05-aaf3-80856b2e2415",
      "groupId":"6f52fe89-1643-40c0-ac19-fac73cc92353",
      "speechId":null,
      "userId":952,
      "title":"254021_VCCS_FF_I_6",
      "fileName":"64a0ecaa1529a48fad6cdee21fd1dc06.wav",
      "duration":45.525875,
      "createdDate":"2017-12-18T11:20:43.179Z",
      "focusArea":null,
      "submitted":false,
      "submissionDate":null,
      "feedbackRequested":false,
      "feedbackRequestedDate":null,
      "results":{
         "algorithm":"v2-default",
         "scores":{
            "pace":3,
            "vibes":{
               "clear":4.99,
               "ditsy":1.2,
               "pushy":2.9,
               "timid":1.79,
               "boring":4.36,
               "nervous":2.12,
               "arrogant":2.89,
               "detached":3.07,
               "assertive":3.86,
               "authentic":3.77,
               "confident":5.09,
               "confusing":2.43,
               "energetic":5.17,
               "organized":4.37,
               "personable":2.6,
               "persuasive":3.45,
               "belligerent":2.13,
               "captivating":3.45,
               "condescending":1.99,
               "unapproachable":1.75
            },
            "clarity":7.74,
            "overall":1.69,
            "upspeak":0,
            "pauseToTalk":93,
            "varietyPace":{
               "inBand":18,
               "aboveBand":82,
               "belowBand":0
            },
            "varietyPitch":{
               "inBand":7,
               "aboveBand":0,
               "belowBand":93
            },
            "wordSpotting":{
               "so":2,
               "like":1,
               "right":0,
               "uh/um":0,
               "simply":0,
               "i think":1,
               "honestly":0,
               "you know":0,
               "basically":0,
               "literally":0
            },
            "varietyVolume":{
               "inBand":52,
               "aboveBand":19,
               "belowBand":30
            },
            "strengthOfOpening":2.95
         },
         "completedDate":"2017-12-18T11:21:23.589Z",
         "status":"completed"
      },
      "playbackUrl":"https://voicevibes.s3.amazonaws.com/playback/20b73c2b-1ca1-4c05-aaf3-80856b2e2415.mp3"
   },
   "serverDate":"2017-12-18T11:41:08.525Z"
}
 * 
 * @author miker_000
 */
public class VoiceVibesResult implements MergableScoreObject {
    
    public static float VOICE_VIBES_OVERALL_WEIGHT = 5;
    public static float GOOD_VIBES_WEIGHT = 2;
    public static float BAD_VIBES_WEIGHT = 2;
    public static float VARIETY_WEIGHT = 0;
    public static float WORDSPOT_WEIGHT = 1;
    
    String jsonStr;
    
    List<VoiceVibesScaleScore> scoreList;
    
    Locale locale;
    int wordCount = 0;
    String competencyName;
    
    // Float overall = null;
    
    
    
    public VoiceVibesResult( List<VoiceVibesScaleScore> combinedScaleScoreList, int wordCount )
    {
        scoreList = combinedScaleScoreList;
        this.wordCount = wordCount;
    }
    
    public VoiceVibesResult( String json, Locale loc, int wordCount )
    {
        this.jsonStr=json;
        this.wordCount=wordCount;
        
        parseJsonForScoreList();
        
        locale = loc;
        
        if( locale == null )
            locale = Locale.US;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append( "VoiceVibesResult{ wordCount=" + wordCount + '}' );
        
        if( this.scoreList!=null )
        {
            for( VoiceVibesScaleScore vvss : scoreList )
            {
                sb.append( "\n" + vvss.toString() );
            }
        }
        
        return sb.toString();
    }
    
    
    
    @Override
    public int getMergableScoreObjectTypeId()
    {
        return MergableScoreObjectType.VOICEVIBES.getMergableScoreObjectTypeId();
    }
    
    
    public void setLocale( Locale l )
    {
        this.locale=l;
        
        for( VoiceVibesScaleScore vs : this.scoreList )
        {
            vs.setLocale(l);
        }
    }
    
    public boolean hasResult( VoiceVibesScaleType vvst )
    {
        return getVoiceVibesScaleScore( vvst ) != null;
    }
    
    public VoiceVibesScaleScore getVoiceVibesScaleScore( VoiceVibesScaleType vvst )
    {
        if( scoreList==null )
            return null;
        
        for( VoiceVibesScaleScore vvss : scoreList )
        {
            if( vvss.voiceVibesScaleType.equals( vvst ) )
                return vvss;
        }
        
        return null;
    }
    
    
    /**
     * Score is 0 - 10
     * @return 
     */
    public float getOverallScore()
    {
        VoiceVibesScaleScore vvss = getVoiceVibesScaleScore( VoiceVibesScaleType.OVERALL );
        
        if( vvss == null )
            return 0;
        
        return vvss.getScore();
    }
    
    /**
     * This is the overall score, modified by the vibes scores. 
     * 
     * @return 
     */
    public float computeHRAVoiceVibesOverallScore() throws Exception
    {
        float overallScore = getOverallScore();  // 0 - 100
        
        float goodVibesAvgScore = getAverageVibeScore(true); // 0 - 100, where 100 is good
        
        float invertedBadVibesAvgScore = (100f-getAverageVibeScore(false)); // 0 - 100, where 100 is good
    
        float avgVarietyScore = 0; // getAverageVarietyScore();  // 0 - 100, 100 is good (included in the overall score).
                
        float wordSpotsScore = 0;   // 0 - 100, 100 is good
        
        VoiceVibesScaleScore vvss = getVoiceVibesScaleScore( VoiceVibesScaleType.WORD_SPOTTING );
        
        if( vvss != null )
            wordSpotsScore = vvss.getScore();
        
        float sum = 0;
        float weights = 0;
        
        
        if( overallScore>0 )
        {
            sum += overallScore*VOICE_VIBES_OVERALL_WEIGHT;
            weights += VOICE_VIBES_OVERALL_WEIGHT;            
        }
        
        if( goodVibesAvgScore>0 )
        {
            sum += goodVibesAvgScore*GOOD_VIBES_WEIGHT;
            weights += GOOD_VIBES_WEIGHT;
        }

        if( invertedBadVibesAvgScore>0 )
        {
            sum += invertedBadVibesAvgScore*BAD_VIBES_WEIGHT;
            weights += BAD_VIBES_WEIGHT;
        }
        if( avgVarietyScore>0 )
        {
            sum += avgVarietyScore*VARIETY_WEIGHT;
            weights += VARIETY_WEIGHT;
        }
        if( wordSpotsScore>0 )
        {
            sum += wordSpotsScore*WORDSPOT_WEIGHT;
            weights += WORDSPOT_WEIGHT;
        }

        // float wa = weights>0 ? sum/weights : 0;
        
        // LogService.logIt( "VoiceVibesResult.computeHRAVoiceVibesOverallScore() overallScore=" + overallScore + ", goodVibesAvgScore=" + goodVibesAvgScore + ", badVibesAvgScore=" + badVibesAvgScore + ", wordSpotsScore=" + wordSpotsScore + ", total weights=" + weights + ", wa=" + wa );
        
        if( weights>0 )
            return sum/weights;

        return 0;
    }
    
    
    
    private float getAverageVibeScore( boolean good )
    {
        if( this.scoreList==null )
            return 0;
        
        float count = 0;
        float sum = 0;
        
        for( VoiceVibesScaleScore vvs : scoreList )
        {
            if( !vvs.getVoiceVibesScaleType().isVibe() )
                continue;
            
            if( good && !vvs.getVoiceVibesScaleType().isHighGood() )
                continue;
                 
            if( !good && !vvs.getVoiceVibesScaleType().isLowGood() )
                continue;
                 
            count++;
            sum += vvs.getScore();            
        }
        
        if( count==0 )
            return 0;
        
        return sum/count;            
    }
    

    private float getAverageVarietyScore()
    {
        if( this.scoreList==null )
            return 0;
        
        float count = 0;
        float sum = 0;
        
        for( VoiceVibesScaleScore vvs : scoreList )
        {
            if( !vvs.getVoiceVibesScaleType().isVariety())
                continue;
            
                 
            count++;
            sum += vvs.getScore();            
        }
        
        if( count==0 )
            return 0;
        
        return sum/count;            
    }

    
    private void parseJsonForScoreList()
    {
        scoreList = new ArrayList<>();
        
        try
        {
            if( jsonStr==null || jsonStr.isEmpty() )
                throw new Exception( "No json present to parse. " );
            
            JsonReader jr = Json.createReader(new StringReader( jsonStr ));

            JsonObject joTop = jr.readObject();
            
            if( !joTop.containsKey( "data" ) )
                throw new Exception( "No data key/value found in json. " );
            
            JsonObject joData = joTop.getJsonObject( "data" );
            
            if( !joData.containsKey( "results") )
                throw new Exception( "no results key/value found in joData." );
            
            JsonObject joRes = joData.getJsonObject( "results" );

            if( !joRes.containsKey( "scores") )
                throw new Exception( "no scores key/value found in joResults." );
            
            JsonObject joScores = joRes.getJsonObject( "scores" );
            
            JsonObject joS;
            JsonNumber jnScore;
            VoiceVibesScaleScore vvScaleScr;
            
            for( VoiceVibesScaleType vvst : VoiceVibesScaleType.values())
            {
                // Skip items not there.
                
                // Value
                if( !vvst.containsObject() && (!joScores.containsKey(vvst.getJsonKey()) || joScores.isNull(vvst.getJsonKey()) ) )
                    continue;
                
                // Object
                if( vvst.containsObject() && (!joScores.containsKey(vvst.getTopKey()) || joScores.isNull(vvst.getTopKey())) )
                    continue;
                
                joS = null;
                jnScore = null;
                                       
                
                if( vvst.containsObject() )
                {
                    joS = joScores.getJsonObject( vvst.getTopKey() );
                }
                else
                {
                    jnScore = joScores.getJsonNumber(  vvst.getJsonKey() );
                }
                
                vvScaleScr = new VoiceVibesScaleScore( vvst, jnScore, joS, locale, wordCount );
                
                scoreList.add( vvScaleScr );
            }
            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "VoiceVibesResult.parseJsonForScoreList() jsonStr=" + jsonStr );
        }
    }

    public int getWordCount() {
        return wordCount;
    }
    
    
    public static VoiceVibesResult getFromPackedString( String inStr ) throws Exception
    {
        if( inStr==null || inStr.length()==0 )
            throw new Exception( "Packed String is invalid - too short or null" );
        
        int wordCount = 0;
        
        VoiceVibesScaleScore vvs;
        
        VoiceVibesScaleScore wordSpotVVSS = null;
        
        String temp = StringUtils.getBracketedArtifactFromString(inStr, "VVWORDCOUNT" );
        
        if( temp!=null && !temp.isEmpty() )
            wordCount = Integer.parseInt( temp );
        
        List<VoiceVibesScaleScore> vsssl = new ArrayList<>();
        
        temp = StringUtils.getBracketedArtifactFromString(inStr, "VVSCALES" );

        String idStr;
        String valStr;
        String[] toks;
        
        if( temp != null && !temp.isEmpty() )
        {
            toks = temp.split(";");
            
            for( int i=0;i<toks.length-1; i+=2 )
            {
                idStr = toks[i];
                valStr = toks[i+1];
                
                if( idStr.length()>0 && valStr.length()>0 )
                {
                    vvs = new VoiceVibesScaleScore( VoiceVibesScaleType.getValue( Integer.parseInt( idStr)), Float.parseFloat( valStr ) );
                    
                    vsssl.add( vvs );
                    
                    if( vvs.getVoiceVibesScaleType().isWordspot() )
                        wordSpotVVSS = vvs;
                }
            }
        }
                
        if( wordSpotVVSS!= null )
        {
            temp = StringUtils.getBracketedArtifactFromString(inStr, "VVWORDSPOTS" );

            if( temp != null && !temp.isEmpty() )
            {            
                Map<String,Integer> spotMap = new HashMap<>();
                
                toks = temp.split(";");
                
                VoiceVibesWordSpotType vvwst;

                int wdCount;
                
                for( int i=0;i<toks.length-1; i+=2 )
                {
                    idStr = toks[i];
                    valStr = toks[i+1];

                    if( idStr.length()>0 && valStr.length()>0 )
                    {
                        vvwst = VoiceVibesWordSpotType.getValue( Integer.parseInt( idStr ) );
                        
                        wdCount = Integer.parseInt(valStr);
                        
                        spotMap.put( vvwst.getJsonKey(), wdCount );
                    }
                }
                
                wordSpotVVSS.setWordSpotMap(spotMap);               
            }                    
        }
        
        VoiceVibesResult vrOut = new VoiceVibesResult( vsssl, wordCount );
        
        return vrOut;
    }
    
    
    /**
     * Format is:
     * 
     *    [VVWORDCOUNT]word count[VVSCALES]VVScaleTypeId;score;VVScaleTypeId;score;VVScaleTypeId;score ...[VVWORDSPOTS]VVWordSpotType;per100;VVWordSpotType;per100;etc
     * 
     * @return 
     */
    @Override
    public String getPackedTokenStringForTestEventScore()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "[VVWORDCOUNT]" + wordCount );
        
        VoiceVibesScaleScore wordSpotScaleScore = null;
        
        StringBuilder ss;
        
        if( scoreList!=null && !scoreList.isEmpty())
        {
            sb.append( "[VVSCALES]" );
            
            ss = new StringBuilder();
            
            for( VoiceVibesScaleScore vvss : scoreList )
            {
                if( vvss.getVoiceVibesScaleType().isWordspot() )
                    wordSpotScaleScore = vvss;
                
                if( ss.length()>0 )
                    ss.append( ";" );
                
                ss.append( vvss.getVoiceVibesScaleType().getVoiceVibesScaleTypeId() + ";" + vvss.getScore() );
            }
            
            sb.append( ss.toString() );
        }  
        
        if( wordSpotScaleScore != null && wordSpotScaleScore.getWordSpotMap()!=null && !wordSpotScaleScore.getWordSpotMap().isEmpty() )
        {
            Integer wsCount;
            
            sb.append( "[VVWORDSPOTS]" );
            
            ss = new StringBuilder();
            
            Map<String,Integer> wordSpotMap = wordSpotScaleScore.getWordSpotMap();
            
            for( VoiceVibesWordSpotType vwst : VoiceVibesWordSpotType.values() )
            {
                wsCount = wordSpotMap.get( vwst.getJsonKey() );
                
                if( wsCount==null || wsCount==0 )
                    continue;
                                
                if( ss.length()>0 )
                    ss.append( ";" );
                
                ss.append( vwst.getVoiceVibesWordSpotTypeId() + ";" + wsCount.toString() );
            }
            
            sb.append( ss.toString() );
            
        }
        
        return sb.toString();
    }

    public List<VoiceVibesScaleScore> getScoreList() {
        return scoreList;
    }
    
    
    public List<VoiceVibesScaleScore> getStructureScaleScoreList()
    {
        List<VoiceVibesScaleScore> out = new ArrayList<>();
                
        if( scoreList==null || scoreList.isEmpty() )
            return out;
        
        for( VoiceVibesScaleScore vvss : scoreList )
        {
            if( vvss.getVoiceVibesScaleType().isStructure() )
                out.add(vvss);
        }
        
        return out;
    }

    public List<VoiceVibesScaleScore> getVarietyScaleScoreList()
    {
        List<VoiceVibesScaleScore> out = new ArrayList<>();
                
        if( scoreList==null || scoreList.isEmpty() )
            return out;
        
        for( VoiceVibesScaleScore vvss : scoreList )
        {
            if( vvss.getVoiceVibesScaleType().isVariety())
                out.add(vvss);
        }
        
        return out;
    }

    public List<VoiceVibesScaleScore> getGoodVibesScaleScoreList()
    {
        List<VoiceVibesScaleScore> out = new ArrayList<>();
                
        if( scoreList==null || scoreList.isEmpty() )
            return out;
        
        for( VoiceVibesScaleScore vvss : scoreList )
        {
            if( vvss.getVoiceVibesScaleType().isVibe() && vvss.getVoiceVibesScaleType().isGoodVibe() )
                out.add(vvss);
        }
        
        return out;
    }

    public List<VoiceVibesScaleScore> getBadVibesScaleScoreList()
    {
        List<VoiceVibesScaleScore> out = new ArrayList<>();
                
        if( scoreList==null || scoreList.isEmpty() )
            return out;
        
        for( VoiceVibesScaleScore vvss : scoreList )
        {
            if( vvss.getVoiceVibesScaleType().isVibe() && vvss.getVoiceVibesScaleType().isBadVibe() )
                out.add(vvss);
        }
        
        return out;
    }

    public String getCompetencyName() {
        return competencyName;
    }

    public void setCompetencyName(String competencyName) {
        this.competencyName = competencyName;
    }
    
    
    
}
