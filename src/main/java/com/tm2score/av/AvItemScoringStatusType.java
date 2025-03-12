package com.tm2score.av;

public enum AvItemScoringStatusType
{
    INVALID(0, "Saved but invalid. Indicates an invalid audio has been recorded and needs to be deleted." ),
    SKIPPED(1, "Saved but skipped. Typically because too many repeats. Indicates a that the scoring system should score it as wrong." ),
    NOT_READY_FOR_SCORING(10, "Valid but not ready to be scored. Needs processing." ),
    READY_FOR_SCORING(11, "Valid but not scored" ),
    INVALID_READY_FOR_SCORING(12, "Invalid, but not scored. Ready for scoring." ),
    SKIPPED_READY_FOR_SCORING(13, "Skipped, but not scored. Ready for scoring." ),
    SCORED(20, "Scored" ),
    SCORED_SKIPPED(21, "Scored wrong after skip." ),
    SCORE_ERROR(100, "Unscored due to an error" );

    private final int scoringStatusTypeId;

    private String key;


    private AvItemScoringStatusType( int p,
                         String key )
    {
        this.scoringStatusTypeId = p;
        this.key = key;
    }
 
    
    
    public boolean isReadyForScoring()
    {
        return equals( READY_FOR_SCORING ) || equals(INVALID_READY_FOR_SCORING) || equals(SKIPPED_READY_FOR_SCORING);
    }
    
    public boolean isScoringCompleteOrError()
    {
        return equals(SCORED) || equals(SCORED_SKIPPED) || equals(SCORE_ERROR); 
    }
 
    public boolean isScoreError()
    {
        return equals(SCORE_ERROR); 
    }
 
    public boolean isScoreSkipped()
    {
        return equals(SCORED_SKIPPED); 
    }
 
    public boolean isScoreComplete()
    {
        return equals(SCORED); 
    }
 
       
    public boolean isAnyInvalid()
    {
        return equals(INVALID) || equals(INVALID_READY_FOR_SCORING); 
    }
 
    public boolean isInvalid()
    {
        return equals(INVALID); 
    }
   
    public boolean isAnySkipped()
    {
        return equals(SKIPPED)|| equals(SCORED_SKIPPED) || equals(SKIPPED_READY_FOR_SCORING ); 
    }
    public boolean isSkipped()
    {
        return equals(SKIPPED)|| equals(SCORED_SKIPPED); 
    }
    
    
    public String getName()
    {
        return this.key;
    }

    public int getScoringStatusTypeId() {
        return scoringStatusTypeId;
    }
    
    public static AvItemScoringStatusType getValue( int id )
    {
        AvItemScoringStatusType[] vals = AvItemScoringStatusType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getScoringStatusTypeId() == id )
                return vals[i];
        }

        return INVALID;
    }
      
}
