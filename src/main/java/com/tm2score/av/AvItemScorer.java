/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.TextAndTitle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public interface AvItemScorer {
        
    public void scoreAvItem( SimJ.Intn intn, AvItemResponse avItemResponse ) throws Exception;
    
    public SimJ.Intn.Intnitem getSelectedIntnItem( SimJ.Intn intn, AvItemResponse avItemResponse );
    
    public boolean isCorrect( AvItemResponse avItemResponse );
    
    public boolean getPartialCreditAssigned( AvItemResponse avItemResponse );
    
    public Locale getLocale();
    
    public float[] getMetaScores( AvItemResponse avItemResponse );
    
    public String getSelectedValueForItemResponse( SimJ.Intn intn, AvItemResponse avItemResponse );
    
    public List<TextAndTitle> getTextAndTitleList();
    
    // public float getPoints();
    
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle();
    
    public Map<Integer,String> getTextInputTypeMap( SimJ.Intn intn, AvItemResponse avItemResponse ); 
    
    public boolean isPendingScoring();
    
    public float[] getMaxPointsArray();
    
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId, ScorableResponse sr );
    
}
