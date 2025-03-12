/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.metascorer;

import com.tm2score.score.TextAndTitle;
import java.util.List;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public interface MetaScorer {
   
    void calculate();
    
    Map<String,int[]> getTopicMap();
    
    List<TextAndTitle> getTextAndTitleList();
    
    boolean hasValidScore();
    
    String getMetaScoreContentKey();
    
}
