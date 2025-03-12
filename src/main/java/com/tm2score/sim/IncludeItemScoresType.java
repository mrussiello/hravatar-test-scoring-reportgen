package com.tm2score.sim;

import com.tm2score.util.MessageFactory;
import java.util.Locale;



public enum IncludeItemScoresType
{
    DO_NOT_INCLUDE(0,"Do not include (default)", "iist.none"),
    INCLUDE_NUMERIC(1,"Include Numeric Itemscore Values", "iist.numeric"),
    CONVERT_TO_ALPHA(2,"Include and convert to Alpha Numeric Values if possible", "iist.alpha"),
    INCLUDE_RESPONSE(3,"Include Response Info rather than Score Info", "iist.responseselection"),
    INCLUDE_CORRECT(4,"Include Correct/Incorrect Info", "iist.correct"),
    INCLUDE_RESPONSE_AND_CORRECT(5,"Include Response and Correct/Incorrect Info", "iist.responsecorrectcorrect");

    private final int includeItemScoresTypeId;

    private final String name;
    private final String key;
    
    private final static String[] ALPHA_VALUES = new String[] { "0", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "Y", "Z" , "AA", "BB", "CC", "DD", "EE", "FF", "GG", "HH", "II", "JJ", "KK", "LL", "MM", "NN", "OO", "PP", "QQ", "RR", "SS", "TT", "UU", "VV", "WW", "YY", "ZZ" };


    private IncludeItemScoresType( int s , String n, String k )
    {
        this.includeItemScoresTypeId = s;
        this.name = n;
        this.key = k;
    }

    
    public String getScoreKeyInfo( Locale loc, SimCompetencyClass scc )
    {
        if( scc.equals( SimCompetencyClass.SCOREDDATAENTRY) && equals(INCLUDE_CORRECT)  )
            return MessageFactory.getStringMessage( loc, "iist.correct.dataentry.keyinfo", null );
        
        return MessageFactory.getStringMessage( loc, key + ".keyinfo", null );
    }
    
    
    
    public String getName4Reports( Locale loc )
    {
        if( loc==null )
            loc = Locale.US;
        
        return MessageFactory.getStringMessage( loc , key );
    }
    
    public boolean isNone()
    {
        return equals( DO_NOT_INCLUDE );
    }
    
    public boolean isResponseCorrect()
    {
        return equals( INCLUDE_RESPONSE_AND_CORRECT );
    }
    
    public boolean isResponseOrResponseCorrect()
    {
        return equals( INCLUDE_RESPONSE ) || equals( INCLUDE_RESPONSE_AND_CORRECT );
    }
    
    public boolean isResponse()
    {
        return equals( INCLUDE_RESPONSE );
    }
    
    public boolean isIncludeCorrect()
    {
        return equals( INCLUDE_CORRECT );
    }
    
    public boolean isIncludeScore()
    {
        return equals( INCLUDE_NUMERIC ) || equals( CONVERT_TO_ALPHA );
    }
    
    public boolean isIncludeNumericScore()
    {
        return equals( INCLUDE_NUMERIC );
    }
    
    public boolean isIncludeAlphaScore()
    {
        return equals( CONVERT_TO_ALPHA );
    }
    
    public static String convertNumericToAlphaScore( float score )
    {
        int intScr = (int) score;
        
        if( intScr<0 || intScr >= ALPHA_VALUES.length )
            return null;
        
        return ALPHA_VALUES[intScr];
    }
    

    public static IncludeItemScoresType getValue( int id )
    {
        IncludeItemScoresType[] vals = IncludeItemScoresType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getIncludeItemScoresTypeId() == id )
                return vals[i];
        }

        return DO_NOT_INCLUDE;
    }


    public int getIncludeItemScoresTypeId()
    {
        return includeItemScoresTypeId;
    }

    public String getName()
    {
        return name;
    }

}
