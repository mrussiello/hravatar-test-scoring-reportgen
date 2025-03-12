package com.tm2score.event;
import com.itextpdf.text.BaseColor;
import com.tm2score.service.LogService;
import com.tm2score.sim.CategoryDistType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.util.MessageFactory;
import java.awt.Color;
import java.util.Locale;

/*
NEW colors (10-16-2024):
    RED("#E7191F"),
    REDYELLOW("#FFA629"),
    YELLOW("#FCEE21"),
    YELLOWGREEN("#8CC63F" ),
    GREEN("#14AE5C")


Old colors (10-16-2024):
    RED("#ff0000"),
    REDYELLOW("#f17523"),
    YELLOW("#fcee21"),
    YELLOWGREEN("#8cc63f" ),
    GREEN("#69a220")

*/
public enum ScoreCategoryType
{
    UNRATED(0,"g.Unrated", "#ffffff", "#000000", null, "report_detail_stars_0.png", 0, 1, 0, 0, "g.ScoreCatInterpUnknown" ),
    BLACK(2,"g.Black", "#c0c0c0", "#ffffff", "blackicon24.png", "report_detail_stars_0.png", 0.0f, 1f, 1,  10, "g.ScoreCatInterpBlack"  ),
    RED(1,"g.Red", "#E7191F", "#ffffff", "redicon24.png", "report_detail_stars_1.png", 0.6f, 1f, 1,  10, "g.ScoreCatInterpRed"  ),
    REDYELLOW(3,"g.RedYellow", "#FFA629", "#000000", "redyellowicon24.png", "report_detail_stars_2.png", 1.2f, 2, 3, 30, "g.ScoreCatInterpRedYellow" ),
    YELLOW(5,"g.Yellow", "#FCEE21", "#000000", "yellowicon24.png", "report_detail_stars_3.png", 1.8f, 3, 5, 50, "g.ScoreCatInterpYellow" ),
    YELLOWGREEN(8,"g.YellowGreen", "#8CC63F", "#000000", "yellowgreenicon24.png", "report_detail_stars_4.png", 2.4f, 4, 7, 70, "g.ScoreCatInterpYellowGreen" ),
    GREEN(11,"g.Green", "#14AE5C", "#ffffff", "greenicon24.png", "report_detail_stars_5.png", 3, 5, 9, 90, "g.ScoreCatInterpGreen" ),
    WHITE(12,"g.White", "#eaeaea", "#000000", "whiteicon24.png", "report_detail_stars_7.png", 3, 5, 9, 90, "g.ScoreCatInterpWhite" );
    
    
    private final int scoreCategoryTypeId;

    private String key;

    private final String color;
    private String foregroundColor;

    private final String filenameColors;
    private final String filenameStars;

    private final float oneTo5NumericScore;

    private final float zeroTo10NumericScore;
    private final float zeroTo100NumericScore;
    private final float zeroTo3NumericScore;

    private final String interpretationKey;


    private ScoreCategoryType( int p, String key, String c, String fgCol, String f, String fs, float ot3, float ot5, float zt10, float zt100, String interpKey)
    {
        this.scoreCategoryTypeId = p;

        this.key = key;

        this.color = c;
        this.foregroundColor=fgCol;

        this.filenameColors = f;

        this.filenameStars = fs;

        this.oneTo5NumericScore = ot5;
        this.zeroTo3NumericScore = ot3;

        this.zeroTo10NumericScore = zt10;
        this.zeroTo100NumericScore = zt100;

        this.interpretationKey = interpKey;
    }

    public String getImageUrl( String baseImageUrl, boolean useColor )
    {
        if( useColor )
            return baseImageUrl + "/" + filenameColors;

        return baseImageUrl + "/" + filenameStars;

    }

    public String getImageUrl( String baseImageUrl )
    {
        return baseImageUrl + "/" + filenameColors;
    }

    public String getFilenameColors() {
        return filenameColors;
    }

    public String getFilenameStars() {
        return filenameStars;
    }



    public float getNumericEquivScore( int scoreFormatTypeId )
    {
        if( scoreFormatTypeId == ScoreFormatType.NUMERIC_1_TO_5.getScoreFormatTypeId() )
            return oneTo5NumericScore;

        if( scoreFormatTypeId == ScoreFormatType.NUMERIC_0_TO_3.getScoreFormatTypeId() )
            return zeroTo3NumericScore;

        if( scoreFormatTypeId == ScoreFormatType.NUMERIC_1_TO_10.getScoreFormatTypeId() )
            return zeroTo10NumericScore;

        if( scoreFormatTypeId == ScoreFormatType.NUMERIC_0_TO_100.getScoreFormatTypeId() )
            return zeroTo100NumericScore;

        return 0;
    }


    public boolean hasColor()
    {
        return color != null;
    }

    public String getRgbColor()
    {
        return color;
    }
    
    public BaseColor getBaseColor()
    {
        Color col = Color.decode( color );
        int red = col.getRed();
        int green = col.getGreen();
        int blue = col.getBlue();

        // LogService.logIt( "ScoreCategoryType.getBaseColor() red=" + red + ", green=" + green + ", blue=" + blue+ ", rgb=" + getRgbColor() + ", name=" + this.key);

        return new BaseColor( red, green, blue );        
    }
    
    public String getForegroundRgbColor()
    {
        if( foregroundColor==null )
            foregroundColor = "#000000";
        
        return foregroundColor;
    }

    public BaseColor getBaseForegroundColor()
    {
        if( foregroundColor==null )
            foregroundColor = "#000000";
        
        Color col = Color.decode( foregroundColor );
        int red = col.getRed();
        int green = col.getGreen();
        int blue = col.getBlue();

        // LogService.logIt( "ScoreCategoryType.getBaseForegroundColor() red=" + red + ", green=" + green + ", blue=" + blue + ", rgb=" + getForegroundRgbColor() + ", name=" + this.key );

        return new BaseColor( red, green, blue );        
    }
    
    public boolean foregroundDark()
    {
        return !equals(RED) && !equals(GREEN) && !equals(BLACK);
    }


    
    public String getRgbColor( String[] cols )
    {
        if( cols == null || cols.length==0 )
            return getRgbColor(); 
        
        if( equals( RED ) && cols.length>=1 && cols[0]!=null && !cols[0].isEmpty() ) 
            return "#" + cols[0];
        if( equals( REDYELLOW ) && cols.length>=2 && cols[1]!=null && !cols[1].isEmpty() ) 
            return "#" + cols[1];
        if( equals( YELLOW ) && cols.length>=3 && cols[2]!=null && !cols[2].isEmpty() ) 
            return "#" + cols[2];
        if( equals( YELLOWGREEN ) && cols.length>=4 && cols[3]!=null && !cols[3].isEmpty() ) 
            return "#" + cols[3];
        if( equals( GREEN ) && cols.length>=5 && cols[4]!=null && !cols[4].isEmpty() ) 
            return "#" + cols[4];
        
        return getRgbColor();
    }
    
    
    public static ScoreCategoryType[] getArray3Color()
    {
        return new ScoreCategoryType[] {RED,YELLOW,GREEN};
    }

    public static ScoreCategoryType[] getArray5Color()
    {
        return new ScoreCategoryType[] {RED,REDYELLOW,YELLOW,YELLOWGREEN,GREEN};
    }


    public String getInterpretationKey( int simCompetencyClassTypeId ) {

        if( SimCompetencyClass.getValue( simCompetencyClassTypeId ).getUsesKnowledgeInterpretation() )
            return getInterpretationKeyKnowledge();

        if( SimCompetencyClass.getValue( simCompetencyClassTypeId ).getUsesAbilityInterpretation() )
            return getInterpretationKeyAbility();

        if( SimCompetencyClass.getValue( simCompetencyClassTypeId ).getUsesBioInterpretation() )
            return getInterpretationKeyBio();

        if( SimCompetencyClass.getValue( simCompetencyClassTypeId ).getUsesWritingInterpretation() )
            return getInterpretationKeyWriting();

        return getInterpretationKey();
    }

    public String getInterpretationKey() {
        return interpretationKey;
    }

    public String getInterpretationKeyKnowledge() {
        return interpretationKey + "Knldg";
    }

    public String getInterpretationKeyAbility() {
        return interpretationKey + "Abil";
    }

    public String getInterpretationKeyBio() {
        return interpretationKey + "Bio";
    }

    public String getInterpretationKeyWriting() {
        return interpretationKey + "Writing";
    }



    public boolean red()
    {
        return equals( RED );
    }

    public boolean redYellow()
    {
        return equals( REDYELLOW );
    }

    public boolean yellow()
    {
        return equals( YELLOW );
    }

    public boolean yellowGreen()
    {
        return equals( YELLOWGREEN );
    }

    public boolean green()
    {
        return equals( GREEN );
    }
    public boolean white()
    {
        return equals( WHITE );
    }
    public boolean black()
    {
        return equals( BLACK );
    }


    public static ScoreCategoryType getForScore(    ScoreColorSchemeType scst, float s, float highClf, float whiteMin, float grnMin, float yellowGreenMin, float yellowMin, float redYellowMin, float redMin, int scoreFormatTypeId, int categoryDistTypeId, int highClfLvl)
    {
        if( scst != null && scst.getIsFiveColor() )
            return getForScore5Color( s, highClf, grnMin, yellowGreenMin, yellowMin, redYellowMin, scoreFormatTypeId, categoryDistTypeId, highClfLvl );

        if( scst != null && scst.getIsSevenColor() )
            return getForScore7Color( s, highClf, whiteMin, grnMin, yellowGreenMin, yellowMin, redYellowMin, redMin, scoreFormatTypeId, categoryDistTypeId, highClfLvl );

        
        return getForScore3Color( s, highClf, grnMin, yellowMin, scoreFormatTypeId, categoryDistTypeId, highClfLvl);
    }


    public ScoreCategoryType adjustOneLevelUp(  ScoreColorSchemeType scst )
    {
        if( equals( ScoreCategoryType.UNRATED ) )
            return this;

        switch( this.getScoreCategoryTypeId() )
        {
            case 12:
                return  WHITE;
            case 11:
                return scst!=null && scst.getIsSevenColor() ? WHITE : GREEN;
            case 8:
                return GREEN;
            case 5:
                if( scst != null && scst.getIsFiveOrSevenColor() )
                    return YELLOWGREEN;
                return GREEN;
            case 3:
                return YELLOW;
            case 2:
                return RED;            
            default:
                if( scst != null && scst.getIsFiveColor() )
                    return REDYELLOW;
                if( scst != null && scst.getIsSevenColor() )
                    return REDYELLOW;
                return YELLOW;
        }
    }


    private static ScoreCategoryType getForScore3Color( float s,
            float highClf,
            float grnMin,
            float yellowMin,
            int scoreFormatTypeId,
            int categoryDistTypeId,
            int highClfLvl )
    {
        ScoreFormatType sft = ScoreFormatType.getValue(scoreFormatTypeId);

        boolean normalDist = categoryDistTypeId==CategoryDistType.NORMAL.getCategoryDistTypeId();

        if( sft.equals( ScoreFormatType.UNSCORED ) )
            return UNRATED;

        if( s<0 )
            return UNRATED;

        if( !normalDist && highClfLvl > 0 && highClf > 0 && s >= highClf )
            return getHighCliffCat( highClfLvl );

        else if( s>= grnMin )
            return normalDist ? RED : GREEN;

        else if( s>= yellowMin )
            return normalDist ? GREEN : YELLOW;

        return RED;
    }


    private static ScoreCategoryType getForScore5Color( float s,
            float highClf,
            float grnMin,
            float yellowGreenMin,
            float yellowMin,
            float redYellowMin,
            int scoreFormatTypeId,
            int categoryDistTypeId,
            int highClfLvl)
    {
        ScoreFormatType sft = ScoreFormatType.getValue(scoreFormatTypeId);


        if( sft.equals( ScoreFormatType.UNSCORED ) )
            return UNRATED;

        //if( s<0 )
        //    return UNRATED;

        boolean normalDist = categoryDistTypeId==1;

        if( !normalDist && highClfLvl > 0 && highClf > 0 && s >= highClf )
            return getHighCliffCat( highClfLvl );

        else if( s>= grnMin )
            return normalDist ? RED : GREEN;

        else if( s>= yellowGreenMin )
            return normalDist ? YELLOW : YELLOWGREEN;

        else if( s>= yellowMin )
            return normalDist ? GREEN : YELLOW;

        else if( s>= redYellowMin )
            return normalDist ? YELLOW : REDYELLOW;

        return RED;
    }


    private static ScoreCategoryType getForScore7Color( float s,
            float highClf,
            float whiteMin,
            float grnMin,
            float yellowGreenMin,
            float yellowMin,
            float redYellowMin,
            float redMin,
            int scoreFormatTypeId,
            int categoryDistTypeId,
            int highClfLvl)
    {
        ScoreFormatType sft = ScoreFormatType.getValue(scoreFormatTypeId);


        if( sft.equals( ScoreFormatType.UNSCORED ) )
            return UNRATED;

        //if( s<0 )
        //    return UNRATED;

        boolean normalDist = categoryDistTypeId==1;

        if( !normalDist && highClfLvl > 0 && highClf > 0 && s >= highClf )
            return getHighCliffCat( highClfLvl );

        else if( s>= whiteMin )
            return normalDist ? RED : WHITE;

        else if( s>= grnMin )
            return normalDist ? RED : GREEN;

        else if( s>= yellowGreenMin )
            return normalDist ? YELLOW : YELLOWGREEN;

        else if( s>= yellowMin )
            return normalDist ? GREEN : YELLOW;

        else if( s>= redYellowMin )
            return normalDist ? YELLOW : REDYELLOW;

        else if( s>= redMin )
            return normalDist ? YELLOW : RED;
        
        return BLACK;
    }

    
    
    public static ScoreCategoryType getHighCliffCat( int highClfLvl )
    {
        if( highClfLvl == 1 )
            return RED;

        if( highClfLvl == 2 )
            return REDYELLOW;

        if( highClfLvl == 3 )
            return YELLOW;

        if( highClfLvl == 4 )
            return YELLOWGREEN;

        return GREEN;

    }


    public int getScoreCategoryTypeId()
    {
        return this.scoreCategoryTypeId;
    }



    public String getName( Locale locale )
    {
        if( locale==null )
            locale=Locale.US;
        
        return MessageFactory.getStringMessage(  locale , key );
    }






    public static ScoreCategoryType getType( int typeId )
    {
        return getValue( typeId );
    }



    public String getKey()
    {
        return key;
    }



    public static ScoreCategoryType getValue( int id )
    {
        ScoreCategoryType[] vals = ScoreCategoryType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getScoreCategoryTypeId() == id )
                return vals[i];
        }

        return UNRATED;
    }

    
    public static ScoreCategoryType getScoreCategoryTypeForRawScore( ScoreFormatType sft, float scr)
    {
        if( sft.equals( ScoreFormatType.NUMERIC_0_TO_100 ) )
        {
            if( scr<20 )
                return ScoreCategoryType.RED;
            if( scr<40 )
                return ScoreCategoryType.REDYELLOW;
            if( scr<60 )
                return ScoreCategoryType.YELLOW;
            if( scr<80 )
                return ScoreCategoryType.YELLOWGREEN;
            
            return ScoreCategoryType.GREEN;
        }
        
        if( sft.equals(ScoreFormatType.NUMERIC_1_TO_10 ) )
        {
            if( scr<2 )
                return ScoreCategoryType.RED;
            if( scr<4 )
                return ScoreCategoryType.REDYELLOW;
            if( scr<0 )
                return ScoreCategoryType.YELLOW;
            if( scr<8 )
                return ScoreCategoryType.YELLOWGREEN;
            
            return ScoreCategoryType.GREEN;
        }
        
        if( sft.equals( ScoreFormatType.NUMERIC_0_TO_3 ) )
        {
            if( scr<0.6f )
                return ScoreCategoryType.RED;
            if( scr<1.2f )
                return ScoreCategoryType.REDYELLOW;
            if( scr<1.8f )
                return ScoreCategoryType.YELLOW;
            if( scr<2.4 )
                return ScoreCategoryType.YELLOWGREEN;
            
            return ScoreCategoryType.GREEN;
        }

        if( sft.equals( ScoreFormatType.NUMERIC_1_TO_5 ) )
        {
            if( scr<1.8f )
                return ScoreCategoryType.RED;
            if( scr<2.6f )
                return ScoreCategoryType.REDYELLOW;
            if( scr<3.4f )
                return ScoreCategoryType.YELLOW;
            if( scr<4.2 )
                return ScoreCategoryType.YELLOWGREEN;
            
            return ScoreCategoryType.GREEN;
        }

        return ScoreCategoryType.GREEN;
    }
    
    
}
