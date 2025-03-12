/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp.itss;

import java.util.List;
import java.util.Map;


/**
 *
 * @author miker_000
 */
public interface Itss {
    
    
    // public ScoreFormatType getScoreFormatType();
    
    public String getBundleName();
    
    public String[] getCompetencies();
    
    public Map<String,List<String>> getCompetencyChildrenToShow();
    
    public String[] getRiasecCompetencies();
    
    public String[] getRoleTitles();
    
    public String getPreferredRolesIntnUniqueId();
    
    
}
