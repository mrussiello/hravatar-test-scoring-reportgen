package com.tm2score.custom.hraph.tmldr;

import java.util.ArrayList;
import java.util.List;





public enum TmLdrCompetencyType
{
    
    /*
Ability
Personal Competence
Commitment
People Management
Motivation    
    */

    ABILITY(1,"Ability"),
    PERSONALCOMPETENCE(2,"Personal Competence" ),
    PEOPLEMANAGEMENT(3,"People Management" ),
    COMMITMENT(4,"Commitment" ),
    MOTIVATION(5,"Motivation" );

    private final int tmLdrCompetencyTypeId;

    private final String name;
    


    private TmLdrCompetencyType( int s , String n )
    {
        this.tmLdrCompetencyTypeId = s;

        this.name = n;
    }

    
    public static List<TmLdrCompetencyType> getList()
    {
        List<TmLdrCompetencyType> out = new ArrayList<>();
        
        for( TmLdrCompetencyType b : TmLdrCompetencyType.values() )
        {
            out.add(b);
        }
        
        return out;
    }

    public static List<String> getNameList()
    {
        List<String> out = new ArrayList<>();
        
        for( TmLdrCompetencyType b : TmLdrCompetencyType.values() )
        {
            out.add(b.getName());
        }
        
        return out;
    }


    public String getKey()
    {
        return "g.TmLdrAssessOver_" + this.tmLdrCompetencyTypeId + "_a";
    }

    public String getKeyDesc()
    {
        return "g.TmLdrAssessOver_" + this.tmLdrCompetencyTypeId + "_b";
    }

    
    public static TmLdrCompetencyType getValue( int id )
    {
        TmLdrCompetencyType[] vals = TmLdrCompetencyType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTmLdrCompetencyTypeId() == id )
                return vals[i];
        }

        return null;
    }


    public int getTmLdrCompetencyTypeId()
    {
        return tmLdrCompetencyTypeId;
    }

    public String getName()
    {
        return name;
    }


}
