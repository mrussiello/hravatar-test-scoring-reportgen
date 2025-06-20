/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ibmcloud;


import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.util.STStringTokenizer;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author miker_000
 */
public class SentinoResult implements Serializable {
    
    // this is the real output
    private List<HraTrait> hraTraitList;

    Set<Integer> hraTraitTypeIdsToInclude;
    
    /*
     Map of sentinoTraitTypeId, SentinoTrait
    */
    private Map<Integer,SentinoTrait> sentinoTraitMap;
    
    private List<String> warnings;
    
    private int hraTraitPackageTypeId = 0;
    
        
    public SentinoResult( boolean isPacked, String packedStr, int hraTraitPackageTypeId, Set<Integer> hraTraitTypeIdsToInclude) throws Exception
    {        
        this.hraTraitPackageTypeId=hraTraitPackageTypeId;
        this.hraTraitTypeIdsToInclude=hraTraitTypeIdsToInclude;
        
        if( isPacked )
            readFromPacked( packedStr ); 
        else
        {
            readFromJson( packedStr );
            calculateHraTraits();                    
        }
    }
    
    
    
    private void calculateHraTraits()
    {
        hraTraitList = new ArrayList<>();
        
        HraTraitPackageType hraTraitPackageType=HraTraitPackageType.getValue(hraTraitPackageTypeId);
        
        if( sentinoTraitMap==null || hraTraitPackageType.equals(HraTraitPackageType.NONE))
            return;
        
        HraTrait hraTrait;
        List<SentinoTrait> stl;
        
        for( HraTraitType htt : hraTraitPackageType.getHraTraitTypeList() )
        {
            if( hraTraitTypeIdsToInclude!=null && !hraTraitTypeIdsToInclude.contains(htt.getHraTraitTypeId()))
                continue;
            
            stl = new ArrayList<>();
            for( Integer sentinoTraitTypeId : htt.getSentinoTraitTypeIds() )
            {
                if( sentinoTraitMap.containsKey( sentinoTraitTypeId ) )
                {
                    stl.add( sentinoTraitMap.get( sentinoTraitTypeId ) );
                }
            }

            hraTrait = new HraTrait( htt.getHraTraitTypeId(), stl );
            hraTrait.calculate();
            
            //if( hraTrait.isValid() )
            hraTraitList.add( hraTrait );
        }
    }
    
    
    /**
     * Packed string is a list of HRATraits
     *   HraTraitTypeId,score,confidence
     */
    public String getPackedString()
    {
        List<TextAndTitle> ttl = getPackedScoreTextAndTitleList( Locale.US, 0, 0 );
        
        StringBuilder sb = new StringBuilder();
        
        for( TextAndTitle tt : ttl )
        {
            if( sb.length()>0 )
                sb.append( "###" );
            
            sb.append( tt.getTitle() + "###" + tt.getText() + "\n" );
        }
        
        return sb.toString();
    }
    
    /*
    List of TT for each valid Trait, List of TT for each valid SentinoTrait
    */
    public List<TextAndTitle> getPackedScoreTextAndTitleList( Locale locale, float minScore, float minQuantile )
    {
        List<TextAndTitle> out = new ArrayList<>();
        
        try
        {
            if(  !hasValidResults() )
                return out;
            
            TextAndTitle tt;
            
            for( HraTrait htrait : hraTraitList )
            {
                tt = htrait.getScoreTextAndTitle(locale);                
                if( tt==null )
                    continue;
                out.add( tt );
            }
            
            for( SentinoTrait strait : this.sentinoTraitMap.values() )
            {
                tt = strait.getScoreTextAndTitle(locale);
                if( tt==null )
                    continue;
                out.add( tt );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SentinoResult.getPackedScoreTextAndTitleList() " + toString() );
        }
        
        return out;
    }
    
    /*
     This is a series of TextAndTitle values where everything is delimited by ### 
    */
    private void readFromPacked( String packed ) throws Exception
    {
        hraTraitList = new ArrayList<>();
        
        try
        {
            // TODO!!!
            if( packed==null || packed.isBlank() )
                return;
            
            if( !packed.contains("###" ) )
                throw new Exception( "Packed string appears invalid, no ### delimiters. " + packed );
            
            STStringTokenizer stt = new STStringTokenizer(packed,"###");
            
            // TextAndTitle tt;
            HraTrait trait;
            String text,title;
            String[] vals;
            int hraTraitTypeId;
            float hraScore;
            float score;
            float confidence;
            
            while( stt.hasMoreTokens() )
            {
                // title is localized trait name
                title=stt.nextToken().trim();
                
                if( !stt.hasMoreTokens() )
                    throw new Exception( "Packed string appears invalid, has an odd number of ### tokens. " + packed );
                
                // text is  "HraTrait;" +  hraTraitTypeId + ";" + SentinoUtils.roundScore( hraScore ) + ";" + score + ";" + confidence
                text=stt.nextToken().trim();
                
                if( !text.startsWith("HraTrait;") )
                    continue;
                
                vals=text.split(";" );
                if( vals.length<5 )
                    throw new Exception( "Packed string appears invalid, text for trait has too few tokens. text=" + text + ", packed=" + packed );
                
                hraTraitTypeId = Integer.parseInt(vals[1]);
                hraScore = Float.parseFloat(vals[2]);
                score = Float.parseFloat(vals[3]);
                confidence = Float.parseFloat(vals[4]);
                
                trait = new HraTrait( title, hraTraitTypeId, hraScore, score, confidence );
                
                hraTraitList.add( trait );
            }

            LogService.logIt("SentinoResult.readFromPacked() Found hraTraitList=" + hraTraitList.size() + " traits" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SentinoResult.readFromPacked() " + packed );
            
            throw e;
        }
    }
    
    
    
    
    public String getStringSummary()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append( "SentinoResult.HraTraits=" + (hraTraitList==null ? 0 : hraTraitList.size()) + " valid SentinoTraits=" + (this.sentinoTraitMap==null ? 0 : this.sentinoTraitMap.size()) );

        if( this.hraTraitList!=null )
        {
            for( HraTrait t : hraTraitList )
            {
                sb.append( t.getStringSummary() );
            }
        }
        
        if( warnings !=null )
        {
            sb.append( "\n\nWarnings:");
            
            for( String t : warnings )
            {
                sb.append("\n  - " + t );
            }
        }
        
        return sb.toString();
    }
    
    
    public boolean hasValidResults()
    {
        return hraTraitList!=null && !hraTraitList.isEmpty();
    }
    
    private void readFromJson( String json ) throws Exception
    {
        try
        {
            if( json==null || json.isEmpty() )
                throw new Exception( "Json is empty!" );
        
            JsonReader reader = Json.createReader(new StringReader(json));
            
            JsonObject topJson = reader.readObject();     
            
            if( !topJson.containsKey( "scoring" ) )
                throw new Exception("No scoring object");
            
            this.sentinoTraitMap = new HashMap<>();
            hraTraitList = null;

            JsonObject scoringJo = topJson.getJsonObject("scoring");
            
            parseInventoryScores( SentinoGroupType.SIXFACTOR, scoringJo );
            parseInventoryScores( SentinoGroupType.CPI, scoringJo );

            // LogService.logIt("SentinoResult.readFromJson() Found traitMap=" + sentinoTraitMap.size() + " Sentino traits" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SentinoResult.readFromJson() " + json );
            
            throw e;
        }
    }
    
    private void parseInventoryScores( SentinoGroupType sentinoGroupType, JsonObject scoringJo ) throws Exception
    {
        if( scoringJo==null || sentinoGroupType==null )
            return;
        if( !scoringJo.containsKey(sentinoGroupType.getSentinoKey() ) || scoringJo.isNull( sentinoGroupType.getSentinoKey() ) )
            return;
        
        JsonObject inventoryJo = scoringJo.getJsonObject( sentinoGroupType.getSentinoKey() );
        JsonObject traitJo;
        SentinoTrait trt;
        boolean includeSentinoTraitType;
        HraTraitType hraTraitType;
        for( SentinoTraitType t : SentinoTraitType.getForGroupId( sentinoGroupType.getSentinoGroupTypeId() ))
        {
            if( !inventoryJo.containsKey(t.getSentinoKey()) || inventoryJo.isNull( t.getSentinoKey()) )
                 continue;
            
            if( hraTraitTypeIdsToInclude!=null )
            {
                includeSentinoTraitType = false;
                for( Integer hraTraitTypeId : hraTraitTypeIdsToInclude )
                {
                    hraTraitType = HraTraitType.getValue(hraTraitTypeId);
                    if( hraTraitType==null )
                        continue;
                    for( int sttid : hraTraitType.getSentinoTraitTypeIds() )
                    {
                        if( sttid==t.getSentinoTraitTypeId() )
                        {
                            includeSentinoTraitType=true;
                            break;
                        }
                    }
                }
                
                // skip if we don't need this  SentinoTraitType.
                if( !includeSentinoTraitType )
                    continue;
            }
            
            traitJo = inventoryJo.getJsonObject(t.getSentinoKey());
            if( traitJo==null )
                continue;
            
            trt = new SentinoTrait( t.getSentinoTraitTypeId(), traitJo );
            
            //if( trt.getConfidence()>=SentinoUtils.MIN_SENTINO_TRAIT_CONFIDENCE )            
            sentinoTraitMap.put(t.getSentinoTraitTypeId(), trt);
        }
        
    }
    
    
    public List<String> getWarnings()
    {
        return warnings;
    }
    
}
