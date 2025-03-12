package com.tm2score.findly;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.faces.model.SelectItem;



/**
 *
 *
 * @author Mike
 */
public enum FindlyTestType
{
    // DEFAULT
    NONE(0,"ftt.none" ),
    BEHAVIORAL(100,"ftt.Behavioral" ),
    COGNITIVE(110,"ftt.Cognitive" ),
    COMBINATION(120,"ftt.Combination" ),
    SIMULATION(130,"ftt.Simulation" ),
    SKILLSKNOWLEDGE(140,"ftt.SkillsKnowledge" );


    /**
     *
ftt.Behavioral=Behavioral
ftt.Cognitive=Cognitive
ftt.Combination=Combination
ftt.Simulation=Simulation
ftt.SkillsKnowledge=Skill/ Knowledge

     */

    private final int findlyTestTypeId;

    private final String key;


    private FindlyTestType( int s , String n )
    {
        this.findlyTestTypeId = s;

        this.key = n;
    }


    public boolean getIsSkillsKnowledgeCompetency()
    {
        return equals( SKILLSKNOWLEDGE ) || equals( SIMULATION ) || equals( COMBINATION );
    }

    public boolean getIsBehavioralCompetency()
    {
        return equals( BEHAVIORAL ) ;
    }

    public boolean getIsCognitiveCompetency()
    {
        return equals( COGNITIVE ) ;
    }


    public static FindlyTestType getValue( int id )
    {
        FindlyTestType[] vals = FindlyTestType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getFindlyTestTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

    public int getFindlyTestTypeId()
    {
        return findlyTestTypeId;
    }

    public String getKey()
    {
        return key;
    }

}
