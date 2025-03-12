package com.tm2score.sim;

import com.tm2score.score.simcompetency.SimCompetencyScore;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.model.SelectItem;



public enum MultiCompetenciesPerItemType
{
    ONE_PER(0,"One Competency Per"),
    PKSA(1,"Personality,Knowledge,Skills,Abilities (PKSA)" );

    private final int multiCompetenciesPerItemTypeId;

    private final String name;


    private MultiCompetenciesPerItemType( int s , String n )
    {
        this.multiCompetenciesPerItemTypeId = s;

        this.name = n;
    }


    public boolean hasSpecialCompetencies()
    {
        return equals( PKSA );
    }

    public static MultiCompetenciesPerItemType getValue( int id )
    {
        MultiCompetenciesPerItemType[] vals = MultiCompetenciesPerItemType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getMultiCompetenciesPerItemTypeId() == id )
                return vals[i];
        }

        return ONE_PER;
    }



    public int getMultiCompetenciesPerItemTypeId()
    {
        return multiCompetenciesPerItemTypeId;
    }


    public void addOtherSimCompetencies(  List<SimCompetencyScore> simCompetencyScoreList )
    {
        if( equals( PKSA ) )
        {
            // SimCompetencyScore scs = new SimCompetencyScore();


        }
    }




}
