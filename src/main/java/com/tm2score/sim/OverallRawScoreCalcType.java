package com.tm2score.sim;



public enum OverallRawScoreCalcType
{
    WEIGHTS(0,"Use Sim Competency Weight Values and Scaled Scores"),
    ONETIMPORTANCE(1,"Use Onet Importance Values as Weights and Scaled Scores"),
    SIMCOMPETENCYRANKS(2,"Use Sim Competency Rank Values as Weights and Scaled Scores"),
    RAW_SCOREWEIGHTS(3,"Use Sim Competency Weight Values and RAW Scores"),
    RAW_SCOREWEIGHTS_NORMALIZED(4,"Weighted average of Competency RAW Scores, Normalized using Mean/STD"),
    RAW_SCORES_SUM(5,"Sum of Competency RAW Scores"),
    COMPETENCY_SCORES_SUM(6,"Sum of Competency Scaled Scores"),
    RAW_SCORES_WEIGHTED_SUM(7,"Weighted Sum of Competency RAW Scores"),
    COMPETENCY_SCORES_WEIGHTED_SUM(8,"Weighted Sum of Competency Scaled Scores");


    private final int overallRawScoreCalcTypeId;

    private final String name;


    private OverallRawScoreCalcType( int s , String n )
    {
        this.overallRawScoreCalcTypeId = s;

        this.name = n;
    }
    

    public boolean getIsSum()
    {
        return equals(RAW_SCORES_SUM) || equals(COMPETENCY_SCORES_SUM) || equals( RAW_SCORES_WEIGHTED_SUM) || equals(COMPETENCY_SCORES_WEIGHTED_SUM);
    }
    
    public boolean getIsAnyWeights()
    {
        return equals( WEIGHTS ) || equals( RAW_SCOREWEIGHTS ) || equals( RAW_SCOREWEIGHTS_NORMALIZED ) || equals( RAW_SCORES_WEIGHTED_SUM) || equals(COMPETENCY_SCORES_WEIGHTED_SUM);
    }
    
    public boolean getRawNormalized()
    {
        return equals( RAW_SCOREWEIGHTS_NORMALIZED );
    }
    
    public boolean getUsesRawCompetencyScores()
    {
        return equals( RAW_SCOREWEIGHTS ) || equals( RAW_SCOREWEIGHTS_NORMALIZED )|| equals(RAW_SCORES_SUM);
    }

    public static OverallRawScoreCalcType getValue( int id )
    {
        OverallRawScoreCalcType[] vals = OverallRawScoreCalcType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOverallRawScoreCalcTypeId() == id )
                return vals[i];
        }

        return WEIGHTS;
    }


    public int getOverallRawScoreCalcTypeId()
    {
        return overallRawScoreCalcTypeId;
    }

    public String getName()
    {
        return name;
    }

}
