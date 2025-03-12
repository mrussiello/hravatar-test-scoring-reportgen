/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.util.StringUtils;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class TopicComparator implements Comparator<String> {

    List<SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic> ct5SubtopicObjList;
    
    
    public TopicComparator( SimJ.Simcompetency simCompetencyObj )
    {
        if( simCompetencyObj!=null && simCompetencyObj.getCt5Subtopics()!=null && simCompetencyObj.getCt5Subtopics().getCt5Subtopic()!=null )
            ct5SubtopicObjList = simCompetencyObj.getCt5Subtopics().getCt5Subtopic();
    }
    
    @Override
    public int compare(String o1, String o2) {
        
        if( o1==null || o2==null )
            return 0;
        
        if( ct5SubtopicObjList==null || ct5SubtopicObjList.isEmpty() )
            return o1.compareTo(o2);
        
        int do1 = getDisplayOrder( o1 );
        int do2 = getDisplayOrder( o2 );
        
        if( do1==do2 )
            return o1.compareTo(o2);

        return Integer.valueOf(do1).compareTo(do2);
    }
    
    private int getDisplayOrder( String nm )
    {
        
        if( ct5SubtopicObjList==null )
            return 0;
        
        String nmU = StringUtils.getUrlEncodedValue(nm);
        
        for( SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic st : ct5SubtopicObjList )
        {
            if( st.getName().equals( nmU) )
                return st.getDisplayorder();
        }
        
        return 0;
    }
    
}
