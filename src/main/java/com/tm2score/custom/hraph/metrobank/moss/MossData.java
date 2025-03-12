/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.metrobank.moss;

import com.tm2score.custom.hraph.bsp.itss.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author miker_000
 */
public class MossData implements Itss {
    
    public static String REPORT_BUNDLE = "com.tm2score.custom.hraph.bsp.itss.ItssMessages";
    
    static String[] FOUNDATION_COMPETENCIES = new String[] { "Analytical","Communication","Customer Service","Collaboration","Adaptability"}; 

    static String[] COMBINATION_COMPETENCY_CHILDREN = new String[] { "Communication|Grammar and Usage|Writing" }; 
    
    static String[] RIASEC_COMPETENCIES = new String[] {"Realistic","Investigative","Artistic","Social","Enterprising","Conventional" }; 

    static String[] ROLE_TITLES = new String[] {"Credit and Control Measures", "Information Technology", "Analytics and Research", "Business, Marketing and Product Development", "Sales and Revenue", "Backroom Operations and Support", "Frontline Customer Relations"};
        
    static String PREFERRED_ROLES_INTN_UNIQUEID = "MOSS_PREFROLES_I_1";
    
    
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
    public Map<String,List<String>> getCompetencyChildrenToShow()
    {
        Map<String,List<String>> out = new TreeMap<>();
        
        String name;
        List<String> vals;
        
        String[] dv;
        
        for( String str : COMBINATION_COMPETENCY_CHILDREN )
        {
            if( str==null || str.trim().isEmpty() || str.indexOf('|')<0 )
                continue;
            
            dv = str.split("\\|");
            
            name=null;
            vals = new ArrayList<>();
            
            for( int i=0;i<dv.length;i++ )
            {
                if( dv[i].trim().isEmpty() )
                    continue;
                
                if( i==0 )
                    name=dv[i];
                
                else if( !dv[i].trim().isEmpty() )
                    vals.add(dv[i]);
            }
            
            if( name!=null && !name.isEmpty() && !vals.isEmpty() )
                out.put( name, vals);
        }
        
        return out;
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


    
}
