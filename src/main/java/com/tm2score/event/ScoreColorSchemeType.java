package com.tm2score.event;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.sim.CategoryDistType;


public enum ScoreColorSchemeType
{
    THREECOLOR(0,"Three Colors"),
    FIVECOLOR(1,"Five Colors"),
    SEVENCOLOR(2,"Seven Colors");


    private final int scoreColorSchemeTypeId;

    private String key;


    private ScoreColorSchemeType( int p , String key )
    {
        this.scoreColorSchemeTypeId = p;

        this.key = key;
    }

    public boolean getIsFiveOrSevenColor()
    {
        return equals( FIVECOLOR) || equals(SEVENCOLOR);
    }

    public boolean getIsThreeColor()
    {
        return equals( THREECOLOR );
    }



    public boolean getIsFiveColor()
    {
        return equals( FIVECOLOR );
    }

    public boolean getIsSevenColor()
    {
        return equals( SEVENCOLOR );
    }


    public int getScoreColorSchemeTypeId()
    {
        return this.scoreColorSchemeTypeId;
    }


    /**
     * REturns a String delimited by semicolons,
     *
     *    ScoreCategoryTypeId;low value;high value~ScoreCategoryTypeId;low value;high value~ScoreCategoryTypeId;low value;high value;
     *
     *
     *
     * @param simCompetencyObj
     * @return
     */
    public String getScoreCategoryInfoString( SimJ.Simcompetency simCompetencyObj, int scoreFormatTypeId )
    {
        StringBuilder sb = new StringBuilder();
        
        ScoreFormatType sft = ScoreFormatType.getValue(scoreFormatTypeId);

        // Get dist type
        CategoryDistType cdt = CategoryDistType.getValue( simCompetencyObj.getCategorydisttype() );
                
        // Normal Dis
        if( cdt.equals( CategoryDistType.NORMAL ) )
        {
            if( equals( ScoreColorSchemeType.THREECOLOR ) )
            {
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), sft.getMin(), simCompetencyObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simCompetencyObj.getYellowmin(), simCompetencyObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), simCompetencyObj.getGreenmin(), sft.getMax() ).getPackedString() );
            }

            else if( equals( ScoreColorSchemeType.FIVECOLOR ) || equals( ScoreColorSchemeType.SEVENCOLOR ) )
            {
                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.BLACK.getScoreCategoryTypeId(), sft.getMin(), simCompetencyObj.getRedmin()).getPackedString() );
                    sb.append( "~" );
                    
                }                    
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simCompetencyObj.getRedmin() : sft.getMin(), simCompetencyObj.getRedyellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simCompetencyObj.getRedyellowmin(), simCompetencyObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simCompetencyObj.getYellowmin(), simCompetencyObj.getYellowgreenmin()).getPackedString() );
                sb.append( "~" );   
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simCompetencyObj.getYellowgreenmin(), simCompetencyObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), simCompetencyObj.getGreenmin(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simCompetencyObj.getWhitemin() : sft.getMax() ).getPackedString() );
                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( "~" );
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.BLACK.getScoreCategoryTypeId(), simCompetencyObj.getWhitemin(), sft.getMax() ).getPackedString() );                    
                }                    
            }
        }

        else
        {
            float highClfMin = simCompetencyObj.getHighcliffmin();
            int highClfLevel = simCompetencyObj.getHighclifflevel();
            
            // Note that highclifflevel values are NOT the same as ScoreCategoryLevels. Must convert.
            int highClfLevelScoreCategoryId = ScoreCategoryType.getHighCliffCat(highClfLevel).getScoreCategoryTypeId();

            float greenMax = highClfMin>0 && highClfMin<sft.getMax() ? highClfMin : sft.getMax();

            if( equals( ScoreColorSchemeType.THREECOLOR ) )
            {
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), sft.getMin(), simCompetencyObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simCompetencyObj.getYellowmin(), simCompetencyObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simCompetencyObj.getGreenmin(), greenMax ).getPackedString() );

                if( highClfMin > 0 && highClfMin<sft.getMax() )
                {
                    sb.append( "~" );
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.getValue(highClfLevelScoreCategoryId).getScoreCategoryTypeId(), greenMax, sft.getMax() ).getPackedString() );
                }
            }

            else if( equals( ScoreColorSchemeType.FIVECOLOR ) || equals( ScoreColorSchemeType.SEVENCOLOR ) )
            {
                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.BLACK.getScoreCategoryTypeId(), sft.getMin(), simCompetencyObj.getRedmin()).getPackedString() );
                    sb.append( "~" );                    
                }                    
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simCompetencyObj.getRedmin() : sft.getMin(), simCompetencyObj.getRedyellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId(), simCompetencyObj.getRedyellowmin(), simCompetencyObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simCompetencyObj.getYellowmin(), simCompetencyObj.getYellowgreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId(), simCompetencyObj.getYellowgreenmin(), simCompetencyObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simCompetencyObj.getGreenmin(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simCompetencyObj.getWhitemin() : sft.getMax() ).getPackedString() );

                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( "~" );
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.WHITE.getScoreCategoryTypeId(), simCompetencyObj.getWhitemin(),sft.getMax() ).getPackedString() );                    
                }                    

                if( highClfMin > 0 && highClfMin<sft.getMax() )
                {
                    sb.append( "~" );
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.getValue(highClfLevelScoreCategoryId).getScoreCategoryTypeId(), greenMax, sft.getMax() ).getPackedString() );
                }
            }
        }

        return sb.toString();
    }

    
    /**
     * REturns a String delimited by semicolons,
     *
     *    ScoreCategoryTypeId;low value;high value~ScoreCategoryTypeId;low value;high value~ScoreCategoryTypeId;low value;high value;
     *
     *
     *
     * @param simCompetencyObj
     * @return
     */
    public String getScoreCategoryInfoStringForSim( SimJ simObj )
    {
        return ScoreColorSchemeType.this.getScoreCategoryInfoStringForSim( simObj, 0, 0 );
    }
    
    public String getScoreCategoryInfoStringForSim( SimJ simObj, float frcMin, float frcMax )
    {
        StringBuilder sb = new StringBuilder();

        // Get dist type
        CategoryDistType cdt = CategoryDistType.LINEAR; //  .getValue( simCompetencyObj.getCategorydisttype() );

        ScoreFormatType sft = ScoreFormatType.getValue( simObj.getScoreformat() );
        
        if( frcMin==frcMax )
        {
            frcMin = sft.getMin();
            frcMax = sft.getMax();
        }
        
        // Normal Dis
        if( cdt.equals( CategoryDistType.NORMAL ) )
        {
            if( equals( ScoreColorSchemeType.THREECOLOR ) )
            {
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), frcMin, simObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simObj.getYellowmin(), simObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), simObj.getGreenmin(), frcMax ).getPackedString() );
            }

            else if( equals( ScoreColorSchemeType.FIVECOLOR ) || equals( ScoreColorSchemeType.SEVENCOLOR ) )
            {
                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.BLACK.getScoreCategoryTypeId(), frcMin, simObj.getRedmin()).getPackedString() );
                    sb.append( "~" );
                    
                }                    
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simObj.getRedmin() : frcMin, simObj.getRedyellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simObj.getRedyellowmin(), simObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simObj.getYellowmin(), simObj.getYellowgreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simObj.getYellowgreenmin(), simObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), simObj.getGreenmin(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simObj.getWhitemin() : frcMax ).getPackedString() );
                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( "~" );
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.BLACK.getScoreCategoryTypeId(), simObj.getWhitemin(), frcMax ).getPackedString() );                   
                }                    
            }
        }

        else
        {
            //float highClfMin = 100; // simCompetencyObj.getHighcliffmin();
            //int highClfLevel = 1; // simCompetencyObj.getHighclifflevel();

            float greenMax = frcMax; //  sft.getMax(); //  highClfMin>0 && highClfMin<100 ? highClfMin : 100;

            if( equals( ScoreColorSchemeType.THREECOLOR ) )
            {
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), frcMin, simObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simObj.getYellowmin(), simObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simObj.getGreenmin(), greenMax ).getPackedString() );

                //if( highClfMin > 0 && highClfMin<100 )
                //{
                //    sb.append( "~" );
                //    sb.append( new ScoreCategoryRange( ScoreCategoryType.getValue(highClfLevel).getScoreCategoryTypeId(), greenMax, 100 ).getPackedString() );
                //}
            }

            else if( equals( ScoreColorSchemeType.FIVECOLOR ) || equals( ScoreColorSchemeType.SEVENCOLOR ) )
            {
                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.BLACK.getScoreCategoryTypeId(), frcMin, simObj.getRedmin()).getPackedString() );
                    sb.append( "~" );                    
                }                    
                sb.append( new ScoreCategoryRange( ScoreCategoryType.RED.getScoreCategoryTypeId(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simObj.getRedmin() : frcMin, simObj.getRedyellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId(), simObj.getRedyellowmin(), simObj.getYellowmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), simObj.getYellowmin(), simObj.getYellowgreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId(), simObj.getYellowgreenmin(), simObj.getGreenmin()).getPackedString() );
                sb.append( "~" );
                sb.append( new ScoreCategoryRange( ScoreCategoryType.GREEN.getScoreCategoryTypeId(), simObj.getGreenmin(), equals( ScoreColorSchemeType.SEVENCOLOR ) ? simObj.getWhitemin() : greenMax ).getPackedString() );

                if( equals( ScoreColorSchemeType.SEVENCOLOR ) )
                {
                    sb.append( "~" );
                    sb.append( new ScoreCategoryRange( ScoreCategoryType.WHITE.getScoreCategoryTypeId(), simObj.getWhitemin(),greenMax ).getPackedString() );                    
                }                    
                //if( highClfMin > 0 && highClfMin<100 )
                //{
                //    sb.append( "~" );
                //    sb.append( new ScoreCategoryRange( ScoreCategoryType.getValue(highClfLevel).getScoreCategoryTypeId(), greenMax, 100 ).getPackedString() );
                //}
            }
        }

        return sb.toString();
    }
    
    

    public String getName()
    {
        return key;
    }


    public static ScoreColorSchemeType getType( int typeId )
    {
        return getValue( typeId );
    }



    public String getKey()
    {
        return key;
    }



    public static ScoreColorSchemeType getValue( int id )
    {
        ScoreColorSchemeType[] vals = ScoreColorSchemeType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getScoreColorSchemeTypeId() == id )
                return vals[i];
        }

        return FIVECOLOR;
    }

}
