package com.tm2score.event;

import com.tm2score.entity.report.Report;
import com.tm2score.global.Constants;
import com.tm2score.service.LogService;
import com.tm2score.simlet.CompetencyScoreType;
import java.net.URLDecoder;


public enum ScoreFormatType
{
    NUMERIC_1_TO_5(0,"1-5", 3f, 0.83f, 5 ),
    NUMERIC_0_TO_100(1,"0-100", 50, 16.67f, 6 ),
    NUMERIC_0_TO_3(2,"0-3",1.5f, 0.5f, 4 ),
    NUMERIC_1_TO_10(3,"1-10", 5.5f, 1.5f, 10 ),
    OTHER_SCORED(99,"Other, Scored", 0, 0, 0),
    UNSCORED(100,"Unscored",0,0, 0);

    private static final float ABS_MAX_SCORE = 999;
    private static final float ABS_MIN_SCORE = -999;

    private final int scoreFormatTypeId;

    private final String key;

    private final float scaleMean;
    private final float scaleStdDev;

    private final int numberOfGraphicAxisPoints;



    private ScoreFormatType( int p , String key, float mn, float sd, int axisPts )
    {
        this.scoreFormatTypeId = p;

        this.key = key;

        this.scaleMean = mn;

        this.scaleStdDev = sd;
        
        this.numberOfGraphicAxisPoints=axisPts;
    }

    public boolean getSupportsBarGraphic( Report r )
    {
        if( equals( NUMERIC_1_TO_5 ) || equals( NUMERIC_0_TO_3 ) || equals(NUMERIC_1_TO_10 ) || equals( NUMERIC_0_TO_100 ) )
            return true;
        
        if( equals(OTHER_SCORED) && r!=null && r.getFloatParam1()<r.getFloatParam2() )
            return true;
                    
        return false;
        
    }
    
    public float getRandomScore()
    {
        return ((float) Math.random())*(this.getMax() - this.getMin()) + this.getMin();
    }
    
    public int getScorePrecisionDigits()
    {
        if( equals( NUMERIC_1_TO_5 ) )
            return 1;
        if( equals( NUMERIC_0_TO_3 ) )
            return 2;
        if( equals(NUMERIC_1_TO_10 ) )
            return 1;
        if( equals( NUMERIC_0_TO_100 ) )
            return 0;
        
        return 0;        
    }

    
    
    public boolean isNumeric()
    {
        return equals( NUMERIC_1_TO_5 ) || equals( NUMERIC_0_TO_100 ) || equals( NUMERIC_0_TO_3 ) || equals(NUMERIC_1_TO_10 );
    }

    public float getScaleMean() {
        return scaleMean;
    }

    public float getScaleStdDev() {
        return scaleStdDev;
    }

    public float getNCEMean()
    {
        if( equals( NUMERIC_0_TO_100 ) )
            return 50;
        if( equals( NUMERIC_1_TO_5 ) )
            return 3;
        if( equals( NUMERIC_0_TO_3 ) )
            return 1.5f;
        if( equals(NUMERIC_1_TO_10 ) )
            return 5.5f;
        
        return 50;
    }
    
    public float getNCEStdDev()
    {
        if( equals( NUMERIC_0_TO_100 ) )
            return 21.06f;
        if( equals( NUMERIC_1_TO_5 ) )
            return 1.8424f;
        if( equals( NUMERIC_0_TO_3 ) )
            return 0.6318f;
        if( equals(NUMERIC_1_TO_10 ) )
            return 1.895f;
        
        return 21.06f;
        
    }
    
    public boolean getIsLow( float score )
    {
        if( equals( NUMERIC_1_TO_5 ) )
            return score < 2.5f;
        if( equals( NUMERIC_0_TO_100 ) )
            return score < 37.5f;
        if( equals( NUMERIC_0_TO_3 ) )
            return score < 1.25f;
        if( equals(NUMERIC_1_TO_10 ) )
            return score < 4.375f;

        return false;
    }

    public boolean getIsHigh( float score )
    {
        if( equals( NUMERIC_1_TO_5 ) )
            return score > 3.5f;
        if( equals( NUMERIC_0_TO_100 ) )
            return score > 62.5f;
        if( equals( NUMERIC_0_TO_3 ) )
            return score > 2f;
        if( equals(NUMERIC_1_TO_10 ) )
            return score > 6.625f;

        return false;
    }


    public float getMax()
    {
        if( equals( NUMERIC_1_TO_5 ) )
            return 5;

        if( equals( NUMERIC_0_TO_100 ) )
           return 100;

        if( equals( NUMERIC_0_TO_3 ) )
           return 3;

        if( equals(NUMERIC_1_TO_10 ) )
           return 10;
        
        return ABS_MAX_SCORE;
    }

    
    public float invertScore( float scaledScore )
    {
        if(  scaledScore<getMin() )
            scaledScore=getMin();

        if(  scaledScore>getMax())
            scaledScore=getMax();
        
        return getMax() - scaledScore + getMin();        
    }
    
    
    public float getMaxScoreToGiveTestTaker()
    {
        if( equals( NUMERIC_1_TO_5 ) )
            return 5;

        if( equals( NUMERIC_0_TO_100 ) )
           return 100;

        if( equals( NUMERIC_0_TO_3 ) )
           return 3;

        if( equals(NUMERIC_1_TO_10 ) )
           return 10;

        return ABS_MAX_SCORE;
    }

    public float getMin()
    {
        if( equals( NUMERIC_1_TO_5 ) )
            return 1;

        if( equals( NUMERIC_0_TO_100 ) )
           return 0;

        if( equals( NUMERIC_0_TO_3 ) )
           return 0;

        if( equals(NUMERIC_1_TO_10 ) )
           return 1;

        return ABS_MIN_SCORE;
    }

    public float getMinScoreToGiveTestTaker()
    {
        if( equals( NUMERIC_1_TO_5 ) )
            return 1;

        if( equals( NUMERIC_0_TO_100 ) )
           return 1;

        if( equals( NUMERIC_0_TO_3 ) )
           return 0.1f;

        if( equals(NUMERIC_1_TO_10 ) )
           return 1;

        return ABS_MIN_SCORE;
    }


    public float getStandardPercentile( float score )
    {
        if( equals( NUMERIC_1_TO_5 ) )
        {
            if( score < 1 )
                return -1;

            if( score > 5 )
                return -1;

            return (float) Math.rint( 100*100*( score - 1f )/5f )/100f;
        }

        if( equals( NUMERIC_0_TO_100 ) )
        {
            if( score < 0 )
                return -1;

            if( score > 100 )
                return -1;

            return (float) Math.rint( 100*score )/100f;
        }

        if( equals( NUMERIC_0_TO_3 ) )
        {
            if( score < 0 )
                return -1;

            if( score > 3 )
                return -1;

            return (float) Math.rint( 100*100*( score )/3f )/100f;
        }

        if( equals(NUMERIC_1_TO_10 ) )
        {
            if( score < 1 )
                return -1;

            if( score > 10 )
                return -1;

            return (float) Math.rint( 100*100*( score - 1f )/10f )/100f;
        }
        
        return -1;
    }


    public float convertFractionToNormalizedScore( float f , float mean, float stdDev )
    {
        if( equals( OTHER_SCORED ) )
            return f;

        if( stdDev == 0 )
            return scaleMean;

        return scaleMean + (f - mean)*scaleStdDev/stdDev;
    }


    public float convertFractionToLinearScore( float r )
    {
        if( equals( OTHER_SCORED ) )
            return r;

        // return a 1-5 score
        if( equals( NUMERIC_1_TO_5 ) )
            return 1f + 4f*r;

        // return a 0-3 score
        if( equals( NUMERIC_0_TO_3 ) )
            return 3f*r;
        
        // return a 0-3 score
        if( equals(NUMERIC_1_TO_10 ) )
            return 1f + 9f*r;
        
        // return 0-100
        if( equals( NUMERIC_0_TO_100 ) )
            return 100f*r;

        return r;

    }

    //public float convertSourceToLinearScore( int srcSftId, float s )
    //{
    //    if( equals( OTHER_SCORED ) )
    //        return s;

    //    return convertSourceToLinearScore( ScoreFormatType.getValue(srcSftId), s );
    //}

    //private float convertSourceToLinearScore( ScoreFormatType srcSft, float s )
    //{
    //    if( srcSft.equals( NUMERIC_0_TO_100 ) )
    //        return convert0To100ToLinearScore( s );

    //    if( srcSft.equals( NUMERIC_1_TO_5 ) )
    //        return convert1x5ToLinearScore( s );

    //    if( srcSft.equals( NUMERIC_0_TO_3 ) )
    //        return convert0x3ToLinearScore( s );
    //}

    private float convertZScoreToLinearScore( float s )
    {
        if( equals( OTHER_SCORED ) )
            return s;

        return scaleMean + s*scaleStdDev;
    }

    private float convert0To100ToLinearScore( float s )
    {
        if( equals( OTHER_SCORED ) )
            return s;

        // return 0-100
        else if( equals( NUMERIC_0_TO_100 ) )
            return s;

        // return a 1-5 score
        else if( equals(NUMERIC_1_TO_5) )
            return 1f + 4f*(s/100f);
        
        else if( equals(NUMERIC_0_TO_3) )
            return 0f + 3f*(s/100f);
        
        else if( equals(NUMERIC_1_TO_10) )
            return 1f + 9f*(s/100f);
        
        return s;
    }

    public static float convertFromAToBToLinearScore( float s, ScoreFormatType a, ScoreFormatType b )
    {
        if( (a.equals( NUMERIC_0_TO_100 )|| a.equals(NUMERIC_1_TO_5) || a.equals(NUMERIC_0_TO_3) || a.equals(NUMERIC_1_TO_10)) && 
                (b.equals( NUMERIC_0_TO_100 )|| b.equals(NUMERIC_1_TO_5) || b.equals(NUMERIC_0_TO_3) || b.equals(NUMERIC_1_TO_10)) )
            return convertNumRangeA2B(  s, a, b );
        
        return s;
    }
    
    private static float convertNumRangeA2B(  float s, ScoreFormatType a, ScoreFormatType b )
    {
        return b.getMin() + (s - a.getMin())*(b.getMax()-b.getMin())/(a.getMax() - a.getMin());
    }

    
    
    
    public float convert1x5ToLinearScore( float s )
    {
        if( equals( OTHER_SCORED ) )
            return s;

        // no change if already 1-5
        if( equals(NUMERIC_1_TO_5) )
            return s;

        // subtract one then mult by 25 to get 0-100
        if( equals( NUMERIC_0_TO_100 ) )
            return (s-1f)*25f;
        
        if( equals( NUMERIC_0_TO_3 ) )
            return (s-1f)*(3f/4f);
        
        if( equals(NUMERIC_1_TO_10 ) )
            return 1 + (s-1f)*9f/4f;
        
        return s;
    }


    
    public float getUnweightedRawScore( CompetencyScoreType competencyScoreType, float fraction, float total, float stdDeviation, float mean )
    {
        // first, get the number prior to linear scaling
        float scr = 0;

        if( equals( OTHER_SCORED ) )
            scr = total;

        if( equals( UNSCORED ) )
            scr = 0;

        if( competencyScoreType.isAverage() || competencyScoreType.isScoredEssay() || competencyScoreType.isScoredChat() || competencyScoreType.isTypingSpeedAccuracy() || competencyScoreType.isDataEntry() || competencyScoreType.isScoredVoiceSample() || competencyScoreType.isScoredAvUpload() )
            scr = fraction;

        // this is an average of the fraction of match, times 100.
        if( competencyScoreType.isIdentityImageCapture() )
            scr = fraction;
        
        if( competencyScoreType.isPercentOfTotal() )
            scr = fraction*100f;

        if( competencyScoreType.getIsNormScale() )
            scr = stdDeviation > 0 ? (total - mean )/stdDeviation : 0f;

        if( competencyScoreType.isRawTotal() )
            scr = total;

        return scr;

    }

    public float getUnweightedScaledScore( CompetencyScoreType competencyScoreType, float rawScore, float maxPointsPerItem, String lookupTable )
    {
        if( lookupTable != null && !lookupTable.trim().isEmpty() )
            return applyLookupTableToRawScore( rawScore, lookupTable  );


        if( equals( OTHER_SCORED ) )
            return rawScore;

        if( equals( UNSCORED ) )
            return 0;

        // raw is simple decimal (Uminn)
        if( competencyScoreType!=null && competencyScoreType.equals( CompetencyScoreType.AVG_MAX_MINUS_ABS_TRUE_DIFF ) )
        {
            float s = rawScore;
            
            if( maxPointsPerItem <= 0 )
                return s;
            
            float max=5;
            float min=1;
            
            if( equals( ScoreFormatType.NUMERIC_0_TO_100 ) )
            {
                max = 100;
                min = 0;
            }
            
            if( equals( ScoreFormatType.NUMERIC_1_TO_5 ) )
            {
                max = 5;
                min = 1;
            }
            
            if( equals( ScoreFormatType.NUMERIC_0_TO_3 ) )
            {
                max = 3;
                min = 0;
            }

            if( equals(ScoreFormatType.NUMERIC_1_TO_10 ) )
            {
                max = 10;
                min = 1;
            }
            
            s = min + rawScore * (max-min)/maxPointsPerItem;
            
            return s;
        }
        
        if( competencyScoreType!=null )
        {
            if( competencyScoreType.isAverage() || 
                    competencyScoreType.isTypingSpeedAccuracy() || 
                    competencyScoreType.isDataEntry() ||
                    competencyScoreType.isScoredEssay()  || 
                    competencyScoreType.isScoredChat()  || 
                    competencyScoreType.isScoredVoiceSample() || 
                    competencyScoreType.isScoredAvUpload() || 
                    competencyScoreType.isIdentityImageCapture() )
            {
                return rawScore;
            }

            // raw is 0-100 (%)
            if( competencyScoreType.isPercentOfTotal() )
                return convert0To100ToLinearScore( rawScore );

            // raw is number of standard deviations from mean (z-score)
            if( competencyScoreType.getIsNormScale() )
                return convertZScoreToLinearScore( rawScore );

            if( competencyScoreType.isRawTotal() )
                return rawScore;
        }
        
        return rawScore;

    }


    public float applyLookupTableToRawScore( float s, String lookupTable  )
    {
        try
        {
            if( lookupTable == null || lookupTable.trim().isEmpty() )
                return s;

            lookupTable = URLDecoder.decode( lookupTable, "UTF8" );

            //LogService.logIt( "ScoreFormatType.applyLookupTableToRawScore() " + lookupTable );

            String[] vs = lookupTable.split( ",");

            //if( vs.length % 2 != 0 )
            //    throw new Exception( "Lookup table must contain an even number of values so they can be paired." );
            float top=0, topVal=0;

            String vss=null, val=null;

            for( int i=0; i<vs.length-1; i+=2 )
            {
                try
                {
                    vss = vs[i];
                    val = vs[i+1];

                    vss = vss.trim();
                    val = val.trim();

                    top = Float.parseFloat(vss);
                    topVal = Float.parseFloat(val);

                    //LogService.logIt( "ScoreFormatType.applyLookupTableToRawScore() s=" + s + ", top=" + top + ", topVal=" + topVal );

                    if( s<=top )
                        return topVal;
                }

                catch( NumberFormatException e )
                {
                    throw new Exception( "Lookup table contains non-numeric value: vss=" + vss + " val=" + val );
                }
            }

            // if get to this point, s is higher than the highest top value, so return topVal
            return topVal;

        }

        catch( Exception e )
        {
            LogService.logIt(e, "ScoreFormatType.applyLookupTableToRawScore() rawScore=" + s + ", raw lookup table=" + lookupTable  );

            return s;
        }
    }


    public String[] getTickVals()
    {
        if( equals( NUMERIC_0_TO_100 )  )
            return Constants.NUMERIC_0_TO_100_TICKVALS;
        
        if( equals( NUMERIC_0_TO_3 )  )
            return Constants.NUMERIC_0_TO_3_TICKVALS;
        
        if( equals(NUMERIC_1_TO_10 )  )
            return Constants.NUMERIC_1_TO_10_TICKVALS;
        
        if( equals( NUMERIC_0_TO_100 )  )
            return Constants.NUMERIC_0_TO_100_TICKVALS;
        
        return Constants.NUMERIC_0_TO_100_TICKVALS;        
    }
    
    public int getLastTickValWidth()
    {
        if( equals( NUMERIC_0_TO_100 )  )
            return 16;
        
        return 12;
    }


    /*
    public float getUnweightedScore( CompetencyScoreType competencyScoreType, float fraction, float total, float stdDeviation, float mean)
    {
        if( equals( OTHER_SCORED ) )
            return total;

        if( equals( UNSCORED ) )
            return 0;

        if( competencyScoreType.isAverage() )
            return fraction;

        if( competencyScoreType.isPercentOfTotal() )
            return convertFractionToLinearScore( fraction );

        if( competencyScoreType.getIsNormScale() )
            return convertFractionToNormalizedScore(  total , mean, stdDeviation );

        if( competencyScoreType.isRawTotal() )
            return total;

        return total;
    }
    */



    public boolean isUnscored()
    {
        return equals( UNSCORED );
    }


    public int getScoreFormatTypeId()
    {
        return this.scoreFormatTypeId;
    }




    public String getKey()
    {
        return key;
    }

    public int getNumberOfGraphicAxisPoints() {
        return numberOfGraphicAxisPoints;
    }


    public static ScoreFormatType getValue( int id )
    {
        ScoreFormatType[] vals = ScoreFormatType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getScoreFormatTypeId() == id )
                return vals[i];
        }

        return NUMERIC_1_TO_5;
    }

}
