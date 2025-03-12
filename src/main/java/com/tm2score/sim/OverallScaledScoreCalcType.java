package com.tm2score.sim;


import com.tm2score.event.EventFacade;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.service.LogService;



public enum OverallScaledScoreCalcType
{
    RAW(0,"Use raw score with no changes"),
    LOOKUP(1,"Use lookup table"),
    NCE(2,"Convert (Z Raw) to Normal Curve Equiv (NCE)"),
    NORMAL_TRANS(3,"Transform Z Using Mean and STD");


    private final int overallScaledScoreCalcTypeId;

    private final String name;


    private OverallScaledScoreCalcType( int s , String n )
    {
        this.overallScaledScoreCalcTypeId = s;

        this.name = n;
    }


    public boolean getEqualsRawScore()
    {
        return equals( RAW ); 
    }
    
    
    public float getScaledScore( int productId, float rawScore, float mean, float std, float floatParam1, float floatParam2, int scoreFormatTypeId, String lookupTable, StringBuilder scrSummary) throws Exception
    {
        if( equals( RAW ) )
        {
            if( scrSummary != null )
                scrSummary.append( "Calculate Overall Scaled Score Using Raw Score - Returning Raw Score=" + rawScore + "\n");

            return rawScore;
        }

        else if( equals( LOOKUP ) )
        {
            if( lookupTable == null || lookupTable.isEmpty() )
                return rawScore;

            ScoreFormatType sft = ScoreFormatType.getValue(scoreFormatTypeId);

            if( scrSummary != null )
                scrSummary.append( "Calculate Overall Scaled Score Using Lookup Table: " + sft.applyLookupTableToRawScore(rawScore, lookupTable ) + "\n" );

            return sft.applyLookupTableToRawScore(rawScore, lookupTable );
        }

        else if( equals( NCE ) || equals( NORMAL_TRANS ) )
        {
            ScoreFormatType sft = ScoreFormatType.getValue(scoreFormatTypeId);

            // Force these values for this choice
            if( equals( NCE ) )
            {
                floatParam1 = sft.getNCEMean();
                floatParam2 = sft.getNCEStdDev();
                //floatParam1 = 50;
                //floatParam2 = 21.06f;
            }

            float zScore = 0;

            //If no std present, look it up.
            if( std<=0 )
            {
                float[] stdvals = EventFacade.getInstance().getRawScoreStatisticsForProductId(productId);

                // Number of hits.
                if( stdvals[0] <= 10 )
                    throw new Exception( "Cannot convert score to z-score. No values for mean and standard deviation in sim or in database. Hits found=" + stdvals[0] );

                mean = stdvals[1];
                std = stdvals[2];

                if( scrSummary != null )
                    scrSummary.append( "Calculate Overall Scaled Score Using NCE. Looking up Mean= " + mean + ", and STD=" + std + "\n");

            }

            if( std <= 0 )
            {
                LogService.logIt( "NONFATAL ERROR: Cannot convert score to z-score. No values for mean and standard deviation in sim or in database. Returning Raw Score as scaled score." );
                if( scrSummary != null )
                    scrSummary.append( "Calculate Overall Scaled Score Using NCE. Looking up Mean and STD but not found. Using Raw Score as scaled score.\n" );
                return rawScore;
            }

            // Finish the Z-Score
            zScore = (rawScore - mean )/std;

            LogService.logIt("OverallScaledScoreCalcType.getScaledScore() raw score=" + rawScore + ", z-score=" + zScore + ", transforming using mean=" + floatParam1 + ", and std=" + floatParam2 );

            if( scrSummary != null )
                scrSummary.append( "Calculate Overall Scaled Score Using NCE. Raw score=" + rawScore + ", mean=" + mean + ", stc=" + std + ", Overall z-score=" + zScore + ".\n" );

            // Now convert to NCE
            float score = floatParam2*zScore + floatParam1;

            if( scrSummary != null )
                scrSummary.append( "Calculate Overall Scaled Score Using NCE. Overall NCE score=" + score + ".\n" );

            // Prevent 0 scores. This prevents this data from not being used in percentiles. Set so value still rounds to 0!
            if( score < sft.getMinScoreToGiveTestTaker() ) // + ( (sft.getMax() - sft.getMin()) * 0.004f) )
                score = sft.getMinScoreToGiveTestTaker(); // + ( (sft.getMax() - sft.getMin()) * 0.004f);

            if( score > sft.getMaxScoreToGiveTestTaker() )
                score = sft.getMaxScoreToGiveTestTaker();

            LogService.logIt("OverallScaleScoreCalcType.getScaledScore()  converted rawScore=" + rawScore + " to scaled score=" + score );

            if( scrSummary != null )
                scrSummary.append( "Calculate Overall Scaled Score Using NCE. Overall NCE score after limits (0, 100)=" + score + ".\n" );

            return score;
        }

        if( scrSummary != null )
            scrSummary.append( "Calculate Overall Scaled Score BBB - No option found. Returing Raw Score=" + rawScore + "\n");



        return rawScore;
    }


    public static OverallScaledScoreCalcType getValue( int id )
    {
        OverallScaledScoreCalcType[] vals = OverallScaledScoreCalcType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOverallScaledScoreCalcTypeId() == id )
                return vals[i];
        }

        return RAW;
    }


    public boolean getIsTransform()
    {
        return equals(NORMAL_TRANS);
    }
    
    public boolean getIsNCE()
    {
        return equals(NCE);
    }
    
    public int getOverallScaledScoreCalcTypeId()
    {
        return overallScaledScoreCalcTypeId;
    }

    public String getName()
    {
        return name;
    }

}
