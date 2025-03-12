/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.uminn;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.global.I18nUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class UMinnCompetency {
    
    UMinnCompetencyType uMinnCompetencyType;
    
    TestEventScore testEventScore;
    
    Locale locale = null;
    
    String definition;
    
    List<String> highBehs;
    List<String> lowBehs;
    String summary;
    List<String> devOps;
    
    ScoreCategoryType scoreCategoryType;    
    
    List<UMinnItem> scenarioList;
    
    boolean last = false;
    
    public UMinnCompetency(UMinnCompetencyType uMinnCompetencyType, TestEventScore tes, Locale l )
    {
        this.uMinnCompetencyType = uMinnCompetencyType;
        this.testEventScore = tes;
        this.locale = l;

        if( uMinnCompetencyType.getIsHigh(tes.getScore()) )
            scoreCategoryType = ScoreCategoryType.GREEN;
        else if( uMinnCompetencyType.getIsLow(tes.getScore()) )
            scoreCategoryType = ScoreCategoryType.RED;
        else 
            scoreCategoryType = ScoreCategoryType.YELLOW;

        
        //if( tes.getScore()>=66.7f)
        //    scoreCategoryType = ScoreCategoryType.GREEN;
        //else if( tes.getScore()>=33.4f)
        //    scoreCategoryType = ScoreCategoryType.YELLOW;
        //else 
        //    scoreCategoryType = ScoreCategoryType.RED;
    }
    
    public String[] getBehMatrix()
    {
        if( highBehs == null )
            highBehs = new ArrayList<>();
        
        if( lowBehs == null )
            lowBehs = new ArrayList<>();
        
        int l = highBehs.size();
        
        if( lowBehs.size()>l )
            l = lowBehs.size();
        
        String[] out = new String[l*2];
        
        int ct = 0;
        
        for( String s : highBehs )
        {
            out[ct] = s;
            
            ct +=2;
        }
        
        ct = 1;
        for( String s : lowBehs )
        {
            out[ct] = s;
            
            ct +=2;
        } 
        
        for( int i=0;i<out.length;i++ )
        {
            if( out[i]==null )
                out[i] = "";
        }
        
        return out;
    }
    
    public String toString()
    {
        return "UMinnCompetency " + getName() + ", score=" + getScore()
               + ", hiBehs=" + ( highBehs==null ? 0 : highBehs.size() )  
               + ", lowBehs=" + ( lowBehs==null ? 0 : lowBehs.size() )  
               + ", devOps=" + ( devOps==null ? 0 : devOps.size() )  
                + ", scenarios (items)=" + ( scenarioList==null ? 0 : scenarioList.size() );
    }

    
    public String getScore()
    {
        return I18nUtils.getFormattedNumber( Locale.US, testEventScore.getScore(), 0);
    }

    public float getScoreToUseInReports( int scoreScheme )
    {
        // Forced 12/13/2021 per Michael Cullen email.
        if( 1==1 || scoreScheme == 1 )
            return testEventScore.getScore();
        
        // starts with Percentile
        float percentileOrScore = testEventScore.getPercentile();
        
        // Use score instead of percentile.
        if( percentileOrScore<=0 || testEventScore.getOverallPercentileCount()<=10 )
            percentileOrScore = testEventScore.getScore();
        
        return percentileOrScore;
    }

    
    public String getScoreName( int scoreScheme )
    {
        // Implemented 12/13/2021 per Michael Cullen email.
        if( 1==1 )
            return this.uMinnCompetencyType.getScoreNameToUse( testEventScore.getScore());
        
        float percentileOrScore = testEventScore.getPercentile();
        
        
         // Expert	90 - 100
         // Advanced	80 - 89
         // Proficient	60 - 79
         // Basic	40 - 59
         // Novice	39 and below
        if( scoreScheme==1 )
        {
            percentileOrScore = testEventScore.getScore();
            
            if( percentileOrScore>=94 )
                return "Expert";

            if( percentileOrScore>=87 )
                return "Advanced";

            if( percentileOrScore>=75 )
                return "Proficient";

            if( percentileOrScore>=65 )
                return "Basic";

            return "Novice";            
        }
        
        // Use score instead of percentile.
        if( percentileOrScore<=0 || testEventScore.getOverallPercentileCount()<=10 )
        {
            percentileOrScore = testEventScore.getScore();
        
            if( percentileOrScore>=94 )
                return "Significantly Above Average";

            if( percentileOrScore>=87 )
                return "Above Average";

            if( percentileOrScore>=75 )
                return "Average";

            return "Below Average";
        }
        
        else
        {
            if( percentileOrScore>=80 )
                return "Significantly Above Average";

            if( percentileOrScore>=60 )
                return "Above Average";

            if( percentileOrScore>=31 )
                return "Average";

            return "Below Average";            
        }
    }
    
    public String getName()
    {
        if( uMinnCompetencyType != null )
            return uMinnCompetencyType.getName();
        
        return testEventScore.getName();
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<String> getHighBehs() {
        return highBehs;
    }

    public void setHighBehs(List<String> highBehs) {
        this.highBehs = highBehs;
    }

    public List<String> getLowBehs() {
        return lowBehs;
    }

    public void setLowBehs(List<String> lowBehs) {
        this.lowBehs = lowBehs;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getDevOps() {
        return devOps;
    }

    public void setDevOps(List<String> devOps) {
        this.devOps = devOps;
    }

    public List<UMinnItem> getUMinnItemList() {
        return scenarioList;
    }

    public void setUMinnItemList(List<UMinnItem> scenarioList) {
        this.scenarioList = scenarioList;
    }

    public ScoreCategoryType getScoreCategoryType() {
        return scoreCategoryType;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
    
    
    
}
