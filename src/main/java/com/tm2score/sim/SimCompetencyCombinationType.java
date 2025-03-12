package com.tm2score.sim;

import com.tm2score.simlet.CompetencyScoreType;



public enum SimCompetencyCombinationType
{
    AVERAGE_RAW_SCORES(0,"Average Raw Scores (Default)"),
    AVERAGE_SCORES(1,"Average Scores" ),
    AVERAGE_ITEM_SCORES(2, "Average Item Scores" ),
    SUM_RAW_SCORES(10, "Sum Raw Scores" ),
    SUM_SCORES(11, "Sum Scores" ),
    SUM_ITEM_SCORES(12, "Sum Item Scores" ),
    ITEMS_CORRECT(13, "Total Items Correct" ),
    PERCENT_CORRECT(20, "Item Percent Correct Across All" ),
    PERCENT_AVAILABLE_POINTS(21, "Item-Level Percent Available Points" ),
    ITEM_LEVEL_SUM_SCORES(101, "INDIVIDUAL Item-Level: Sum Item Scores" ),
    ITEM_LEVEL_AVERAGE_ITEM_SCORES(102,"INDIVIDUAL Item-Level: Average Item Scores"),
    ITEM_LEVEL_ITEMS_CORRECT(103, "INDIVIDUAL Item-Level: Total Items Correct" ),
    ITEM_LEVEL_AVERAGE_RATINGS(104,"INDIVIDUAL Item-Level: Average MANUAL Ratings"),  // used for Essay, Video Interview, File Upload items
    ITEM_LEVEL_PERCENT_CORRECT(120, "INDIVIDUAL Item-Level: Item Percent Correct Across All" );

    private final int simCompetencyCombinationTypeId;

    private final String name;
    
    private SimCompetencyCombinationType( int s , String n )
    {
        this.simCompetencyCombinationTypeId = s;

        this.name = n;
    }

    public boolean isIndividualItemLevel()
    {
        return equals( ITEM_LEVEL_SUM_SCORES ) || equals( ITEM_LEVEL_AVERAGE_ITEM_SCORES ) || equals( ITEM_LEVEL_ITEMS_CORRECT )|| equals( ITEM_LEVEL_PERCENT_CORRECT ) || equals(ITEM_LEVEL_AVERAGE_RATINGS);
    }
        
    public boolean isAverage()
    {
        return equals(AVERAGE_RAW_SCORES) || equals(AVERAGE_SCORES) || equals(AVERAGE_ITEM_SCORES) || equals(ITEM_LEVEL_AVERAGE_ITEM_SCORES) || equals(ITEM_LEVEL_AVERAGE_RATINGS);
    }

    public boolean isSum()
    {
        return equals(SUM_RAW_SCORES) || equals(SUM_SCORES) || equals(SUM_ITEM_SCORES) || equals(ITEM_LEVEL_SUM_SCORES);
    }

    public boolean isRaw()
    {
        return equals(SUM_RAW_SCORES) || equals(AVERAGE_RAW_SCORES);
    }

    public boolean isTotalCorrect()
    {
        return equals( ITEMS_CORRECT ) || equals(ITEM_LEVEL_ITEMS_CORRECT);
    }
    
    
    public boolean isItemLevel()
    {
        return equals(AVERAGE_ITEM_SCORES) || equals(SUM_ITEM_SCORES) || equals( ITEMS_CORRECT ) || equals( PERCENT_CORRECT ) || equals( PERCENT_AVAILABLE_POINTS ) || isIndividualItemLevel();
    }
    
    
    public CompetencyScoreType getForcedCompetencyScoreType()    
    {
        if( equals( AVERAGE_RAW_SCORES ) )
            return null;
        
        if( equals( AVERAGE_ITEM_SCORES ) || equals( AVERAGE_SCORES ) || equals( ITEM_LEVEL_AVERAGE_ITEM_SCORES ) )
            return CompetencyScoreType.AVERAGE_POINTS;
        
        if( equals( SUM_RAW_SCORES ) || equals( SUM_SCORES ) )
            return null;
        
        if( equals( SUM_ITEM_SCORES ) || equals( ITEM_LEVEL_SUM_SCORES ) )
            return CompetencyScoreType.TOTAL_POINTS;
        
        if( equals( ITEMS_CORRECT ) || equals(ITEM_LEVEL_ITEMS_CORRECT) )
            return CompetencyScoreType.TOTAL_CORRECT;
        
        if( equals( PERCENT_CORRECT ) || equals( ITEM_LEVEL_PERCENT_CORRECT ) )
            return CompetencyScoreType.PERCENT_CORRECT;
        
        if( equals( PERCENT_AVAILABLE_POINTS ) )
            return CompetencyScoreType.PERCENT_OF_TOTAL;
        
        return null;
    }


    public static SimCompetencyCombinationType getValue( int id )
    {
        SimCompetencyCombinationType[] vals = SimCompetencyCombinationType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencyCombinationTypeId() == id )
                return vals[i];
        }

        return AVERAGE_RAW_SCORES;
    }


    public int getSimCompetencyCombinationTypeId()
    {
        return simCompetencyCombinationTypeId;
    }

    public String getName()
    {
        return name;
    }
}
