/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.ibmcloud;

import com.tm2score.score.TextAndTitle;
import com.tm2score.util.MessageFactory;
import jakarta.json.JsonObject;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 *
 * Traits are organized by group. Each top level trait can have a set of children
 * @author miker
 */
public class SentinoTrait implements Serializable
{
    
    String name;
    int sentinoTraitTypeId;
    float quantile;    
    float score;
    float confidence;
    
    
    public SentinoTrait( int sentinoTraitTypeId, JsonObject jo ) throws Exception
    {
        this.sentinoTraitTypeId=sentinoTraitTypeId;
        readFromJsonObject( jo );
    }

    
   
    public void readFromJsonObject( JsonObject jo  ) throws Exception
    {
        SentinoTraitType stt = SentinoTraitType.getValue(sentinoTraitTypeId);
        if( stt==null )
            throw new Exception( "SentinoTraitType Cannot determine SentinoTraitType for sentinoTraitTypeId=" + sentinoTraitTypeId );
        
        if( jo.containsKey("quantile"))
            quantile = (float) jo.getJsonNumber("quantile").doubleValue();

        if( jo.containsKey("score"))
            score = (float) jo.getJsonNumber("score").doubleValue();
        
        if( jo.containsKey("confidence"))
            confidence = (float) jo.getJsonNumber("confidence").doubleValue();
    }

    
    /**
     * Format is 
     *    title = Name for users
     *    text = SentinoTrait;SentinoTraitTypeId;quantile;score;confidence
     * 
     * @param locale
     */
    public TextAndTitle getScoreTextAndTitle( Locale locale )
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append( "SentinoTrait;" + sentinoTraitTypeId + ";" + quantile + ";" + score + ";" + confidence );
        
        String title = getSentinoTraitType().getName();
                
        return new TextAndTitle( sb.toString(), title );
    }
    
        
    
    public String getStringSummary()
    {
        return name + "; quantile=" + quantile + "; score=" + score + "; confidence=" + confidence;
    }
    
    public SentinoTraitType getSentinoTraitType()
    {
        return SentinoTraitType.getValue( this.sentinoTraitTypeId );
    }
    
    public SentinoGroupType getSentinoGroupType()
    {
        return SentinoGroupType.getValue( getSentinoTraitType().getSentinoGroupTypeId() );
    }
    
    

    public int getSentinoTraitTypeId() {
        return sentinoTraitTypeId;
    }


    public float getQuantile() {
        return quantile;
    }

    public void setQuantile(float quantile) {
        this.quantile = quantile;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
