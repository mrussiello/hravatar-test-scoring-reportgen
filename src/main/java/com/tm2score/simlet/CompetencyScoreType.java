package com.tm2score.simlet;



public enum CompetencyScoreType
{
    NONE(0,"No Score"),
    PERCENT_CORRECT(1,"Percent Correct (Expressed as Percent 0-100)"),
    PERCENT_OF_TOTAL(2,"Pct Available Points (Expressed as Percent 0-100)"),
    NORM_SCALE_POINTS(3,"Total Points Normalized (Expressed as Z-Score)"),
    NORM_SCALE_CORRECT(4,"Total Correct Normalized (Expressed as Z-Score)"),
    TOTAL_POINTS(5,"Total Points (Expressed as Total)"),
    TOTAL_CORRECT(6,"Total Correct (Expressed as Total)"),
    AVERAGE_POINTS(7,"Average Points (Points per Item Answered)"),
    TYPING_SPEEDACCURACY(8,"Typing Speed and Accuracy"),
    SCORED_ESSAY(9,"Scored Essay"),
    TOTAL_ABS_TRUE_DIFF(10,"Total ABS of Difference From True Scores"),
    TOTAL_TRUE_DIFF(11,"Total Difference From True Scores"),
    AVG_ABS_ITEM_ZSCORE_DIFF(12,"Average ABS(Item Z-Score), Diff from True"),
    AVG_ITEM_ZSCORE_DIFF(13,"Average Item Z-Score, Diff from True"),
    AVG_MAX_MINUS_ABS_TRUE_DIFF(14,"Average Max Minus Abs Difference from True Score"),
    SCORED_VOICE_SAMPLE(15,"Scored Voice Sample (Average Points plus meta-scores)"),
    SCORED_AV_UPLOAD(16,"Scored AV Upload (Average Points plus meta-scores)"),
    IDENTITY_IMAGE_CAPTURE(17,"Identity Image Capture (Percent match across all images)"),
    DATA_ENTRY(18,"Data Entry (Keystrokes per hour, conv to 0-100)"),
    SCORED_CHAT(19,"Scored Chat (Average Points plus meta-scores)"),
    VOICE_PERFORMANCE_INDEX(20,"Voice Performance Index");

    private final int competencyScoreTypeId;

    private final String name;


    private CompetencyScoreType( int s , String n )
    {
        this.competencyScoreTypeId = s;

        this.name = n;
    }

    public boolean getIsNormScale()
    {
       return equals( NORM_SCALE_POINTS ) || equals( NORM_SCALE_CORRECT );
    }

    public boolean isDichotomous()
    {
       return equals( PERCENT_CORRECT ) || equals( NORM_SCALE_CORRECT ) || equals( TOTAL_CORRECT );
    }

    public boolean isPointAccum()
    {
        return equals( PERCENT_OF_TOTAL ) || equals( NORM_SCALE_POINTS ) || equals( TOTAL_POINTS ) || equals( AVERAGE_POINTS );
    }

    public boolean isAverage()
    {
        return equals( AVERAGE_POINTS ) || equals( AVG_ABS_ITEM_ZSCORE_DIFF ) || equals( AVG_ITEM_ZSCORE_DIFF ) || equals( AVG_MAX_MINUS_ABS_TRUE_DIFF );
    }

    public boolean isTrueDifference()
    {
        return equals( TOTAL_ABS_TRUE_DIFF ) || equals( TOTAL_TRUE_DIFF ) || equals( AVG_MAX_MINUS_ABS_TRUE_DIFF );
    }

    public boolean isAvgZscoreDiff()
    {
        return equals( AVG_ABS_ITEM_ZSCORE_DIFF ) || equals( AVG_ITEM_ZSCORE_DIFF );
    }
    
    public boolean isAddTotalsForTopics()
    {
        return !equals( SCORED_CHAT );
    }
    
    
    public boolean isPercentOfTotal()
    {
        return equals( PERCENT_OF_TOTAL ) || equals( PERCENT_CORRECT );
    }

    public boolean isPercentCorrect()
    {
        return equals( PERCENT_CORRECT );
    }
    
    
    public boolean isTypingSpeedAccuracy()
    {
        return equals( TYPING_SPEEDACCURACY );
    }

    public boolean isDataEntry()
    {
        return equals( DATA_ENTRY );
    }

    public boolean isScoredEssay()
    {
        return equals( SCORED_ESSAY );
    }

    public boolean isScoredChat()
    {
        return equals( SCORED_CHAT );
    }

    public boolean isScoredVoiceSample()
    {
        return equals( SCORED_VOICE_SAMPLE );
    }

    public boolean isScoredAvUpload()
    {
        return equals( SCORED_AV_UPLOAD );
    }

    public boolean isIdentityImageCapture()
    {
        return equals( IDENTITY_IMAGE_CAPTURE );
    }



    public boolean isRawTotal()
    {
        return equals( TOTAL_POINTS ) || equals( TOTAL_CORRECT ) || equals( TOTAL_ABS_TRUE_DIFF ) || equals( TOTAL_TRUE_DIFF );
    }

    public static CompetencyScoreType getValue( int id )
    {
        CompetencyScoreType[] vals = CompetencyScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCompetencyScoreTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getCompetencyScoreTypeId()
    {
        return competencyScoreTypeId;
    }

    public String getName()
    {
        return name;
    }

}
