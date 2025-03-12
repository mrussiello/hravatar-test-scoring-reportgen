/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.sim.SimCompetencySortType;
import java.util.Comparator;

/**
 *
 * @author Mike
 */
public class SimJSimCompetencyWeightComparator implements Comparator<SimJ.Simcompetency> {

    SimCompetencySortType scst;
    
    public SimJSimCompetencyWeightComparator( SimCompetencySortType scst )
    {
        this.scst = scst;
        
        if( this.scst==null )
            this.scst = SimCompetencySortType.NAME;
    }
    
    
    @Override
    public int compare(SimJ.Simcompetency o1, SimJ.Simcompetency o2) {

        if( scst.equals( SimCompetencySortType.DISPLAYORDER ) )
            return ((Integer) o1.getDisporder()).compareTo( o2.getDisporder() );

        else if( scst.equals( SimCompetencySortType.WEIGHT ) )
        {
            if( o1.getWeight()!=o2.getWeight() )
                return ((Float) o1.getWeight()).compareTo( o2.getWeight() );

            if( o1.getName()!=null && o2.getName()!=null )
                return o1.getName().compareTo( o2.getName() );
        }
        
        else if( scst.equals( SimCompetencySortType.NAME ) )
            return o1.getName().compareTo( o2.getName() );
        
        else if( scst.equals( SimCompetencySortType.RANK ) )
            return ((Integer) o1.getUserrank()).compareTo( o2.getUserrank() );
        
        return 0;
    }


}
