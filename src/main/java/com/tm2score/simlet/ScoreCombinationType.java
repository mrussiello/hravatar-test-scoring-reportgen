package com.tm2score.simlet;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.service.LogService;
import java.util.List;



public enum ScoreCombinationType
{
    NONE(0,"Do not combine this score value"),
    SUM_ALL(1,"Sum or All Item Scores"),
    AVG_NONZERO(11,"Average of Non-Zero Item Scores"),
    AVG_ALL(12,"Average of All Item Scores"),
    MAX_VALUE(13,"Maximum Value"),
    MIN_VALUE(14,"Minimum Value");


    private final int scoreCombinationTypeId;

    private final String name;


    private ScoreCombinationType( int s , String n )
    {
        this.scoreCombinationTypeId = s;

        this.name = n;
    }

    public static ScoreCombinationType getValue( int id )
    {
        ScoreCombinationType[] vals = ScoreCombinationType.values();
        for (ScoreCombinationType val : vals) {
            if (val.getScoreCombinationTypeId() == id) {
                return val;
            }
        }

        return NONE;
    }


    public float combineScores( List<ScorableResponse> irl , int index )
    {
        if( equals( NONE ) )
            return 0;

        float total=0;
        float count=0;
        float val;
        float f=0;

        if( equals( MIN_VALUE ) )
            f=999999f;
        if( equals( MAX_VALUE ) )
            f=-999999f;
        
        for( ScorableResponse sr : irl )
        {
            
            // LogService.logIt( "ScoreCombinationType.combineScores() index=" + index + ", sr.hasValidScore()=" + sr.hasValidScore() + ", sr.hasMetaScore(index)=" + sr.hasMetaScore(index) + ", Sr=" + sr.toString() );
            if( !sr.hasMetaScore(index) || !sr.hasValidScore() )
                continue;

            val = sr.getMetaScore(index);

            total += val;

            if( val!=0 || !equals( AVG_NONZERO ) )
                count++;

            // LogService.logIt( "ScoreCombinationType.combineScores() Type=" + getName() + ", ADDING index=" + index + ", val=" + val + ", count=" + count + ", sr.hasValidScore()=" + sr.hasValidScore() + ", sr.hasMetaScore(index)=" + sr.hasMetaScore(index) + ", Sr=" + sr.toString() );
            
            if( equals( MIN_VALUE ) && val<f )
                f=val;
            if( equals( MAX_VALUE ) && val>f)
                f=val;
        }

        if( equals( SUM_ALL ) )
            return total;
        
        if( (equals( MAX_VALUE ) || equals( MIN_VALUE )) && count>0 )
            return f;

        // LogService.logIt( "ScoreCombinationType.combineScores() FINAL count=" + count + ", total=" + total + " returning " + (count>0 ? total/count : 0) );

        return count>0 ? total/count : 0;
    }


    public int getScoreCombinationTypeId()
    {
        return scoreCombinationTypeId;
    }

    public String getName()
    {
        return name;
    }

}
