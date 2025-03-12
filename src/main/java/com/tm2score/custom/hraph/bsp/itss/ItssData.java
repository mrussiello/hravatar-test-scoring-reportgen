/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp.itss;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author miker_000
 */
public class ItssData implements Itss {
    
    public static String REPORT_BUNDLE = "com.tm2score.custom.hraph.bsp.itss.ItssMessages";
    
    static String[] FOUNDATION_COMPETENCIES = new String[] { "Analytical Thinking","Attention to Detail","Multitasking","Service Orientation","Written Communication","Cooperation and Collaboration","Adaptability"}; 
            
    static String[] RIASEC_COMPETENCIES = new String[] {"Realistic","Investigative","Artistic","Social","Enterprising","Conventional" }; 

    static String[] ROLE_TITLES = new String[] {"Administrative", "Application Systems", "Infrastructure", "IT Acquisition", "Program Management", "Security"};
    
    static String PREFERRED_ROLES_INTN_UNIQUEID = "BSP_PREFROLES_I_1";
    
    
    
    @Override
    public String getPreferredRolesIntnUniqueId()
    {
        return PREFERRED_ROLES_INTN_UNIQUEID;
    }
    
    @Override
    public String getBundleName()
    {
        return REPORT_BUNDLE;
    }
    
    @Override
    public String[] getCompetencies()
    {
        return FOUNDATION_COMPETENCIES;
    }

    @Override
    public String[] getRiasecCompetencies()
    {
        return RIASEC_COMPETENCIES;
    }

    @Override
    public String[] getRoleTitles()
    {
        return ROLE_TITLES;
    }


    @Override
    public Map<String,List<String>> getCompetencyChildrenToShow()
    {
        Map<String,List<String>> out = new TreeMap<>();        
        return out;
    }
    
    
    
}
