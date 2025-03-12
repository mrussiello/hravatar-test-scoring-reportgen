/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.metascorer;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.score.TextAndTitle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class BaseMetaScorer implements MetaScorer {

    protected boolean validScore;
    
    protected TestEvent testEvent;
    
    protected Locale locale;
    protected Locale reportLocale;
    
    
    
    @Override
    public void calculate() {
        
    }

    @Override
    public Map<String, int[]> getTopicMap() {
        return null;
    }

    @Override
    public List<TextAndTitle> getTextAndTitleList() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasValidScore() {
        return validScore;
    }  
    
    @Override
    public String getMetaScoreContentKey()
    {
        return null;
    }
    
}
