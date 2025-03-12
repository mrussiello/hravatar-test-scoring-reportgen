package com.tm2score.sim;

import com.tm2score.global.NumberUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.Locale;


public enum FillBlankType
{
    STRING(0, "Any Alphanumeric"),
    EMAIL(1, "Email"),
    INTEGER(2, "Integer"),
    INTEGER_POSITIVE(7, "Positive Integer (0 OK)"),
    FLOAT(3, "Float Numeric"),
    FLOAT_POSITIVE(6, "Positive Float Numeric (0 OK)"),
    STRING_EXACT(4, "Alphanumeric - Test for Exact Match"),
    WHOLE(5, "Whole Number (0,1,2,..)"),
    CURRENCY(9, "Currency - Float with symbol"),
    PERCENT(10, "Percentage - Float with symbol"),
    TENKEY_DATA_ENTRY_ONLY(11, "Ten Key Data Entry filter (Enter acts as TAB)"),
    ALPHA_DATA_ENTRY_ONLY(12, "Alphanumeric Data Entry Filter (Enter acts as TAB)"),
    STRING_LOOSE(13, "Alphanumeric String - non-case sensitive no whitespace for match");
    
    
    /*
    STRING(0, "Any Alphanumeric"),
    EMAIL(1, "Email"),
    INTEGER(2, "Integer"),
    FLOAT(3, "Float Numeric"),
    STRING_EXACT(4, "Alphanumeric - Test for Exact Match"),
    WHOLE(5, "Whole Number (0,1,2,..)"),
    CURRENCY(9, "Currency - Float with symbol"),
    PERCENT(10, "Percentage - Float with symbol"),
    TENKEY_DATA_ENTRY_ONLY(11, "Ten Key Data Entry filter (Enter acts as TAB)"),
    ALPHA_DATA_ENTRY_ONLY(12, "Alphanumeric Data Entry Filter (Enter acts as TAB)"),
    STRING_LOOSE(13, "Alphanumeric String - non-case sensitive no whitespace for match");
    */
    
    private final int fillBlankTypeId;

    private String name;

    private FillBlankType( int p, String nm )
    {
        this.fillBlankTypeId = p;

        this.name = nm;
    }

    public int getFillBlankTypeId()
    {
        return fillBlankTypeId;
    }


    public static FillBlankType getType( int typeId )
    {
        return getValue( typeId );
    }

    public static FillBlankType getValue( int id )
    {
        FillBlankType[] vals = FillBlankType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getFillBlankTypeId() == id )
                return vals[i];
        }

        return STRING;
    }

    public boolean valuesMatch( String inValue, String matchValue, Locale locale, boolean caseSensitive)
    {
        try
        {
            if( inValue==null )
                inValue="";

            if( matchValue==null )
                matchValue = "";

            if( equals( STRING ) )
            {
                if( caseSensitive )
                    return inValue.trim().equals( matchValue.trim() );
                
                return inValue.trim().equalsIgnoreCase( matchValue.trim() );
            }

            else if( equals( STRING_EXACT ) )
                return inValue.trim().equals( matchValue.trim() );

            else if( equals( INTEGER ) || equals(INTEGER_POSITIVE) || equals( WHOLE ) )
            {
                inValue = StringUtils.removeCurrencyPercentSymbols( inValue );

                inValue = inValue.trim();

                int mv = NumberUtils.parseIntegerInputStr( Locale.US, matchValue ); //  Integer.parseInt( matchValue );

                int iv = NumberUtils.parseIntegerInputStr( Locale.US, inValue ); //  Integer.parseInt( inValue );

                if( ( equals(INTEGER_POSITIVE) || equals( WHOLE ) ) && iv<0 )
                    return false;                
                
                return mv==iv;
            }

            else if( equals( FLOAT ) || equals( FLOAT_POSITIVE ) || equals( CURRENCY ) || equals( PERCENT ) )
            {
                inValue = StringUtils.removeCurrencyPercentSymbols( inValue );
                inValue = inValue.trim();

                
                float mv = NumberUtils.parseFloatInputStr( Locale.US, matchValue ); // Float.parseFloat( matchValue );

                float iv = NumberUtils.parseFloatInputStr( Locale.US, inValue ); // Float.parseFloat( inValue );

                if( equals( FLOAT_POSITIVE ) && iv<0 )
                    return false;                
                
                return iv>=mv-0.0099f && iv<=mv + 0.0099;
            }

            else if( equals( TENKEY_DATA_ENTRY_ONLY ) || equals( ALPHA_DATA_ENTRY_ONLY ) )
                return inValue.trim().equals( matchValue.trim() );

            
            else if( equals( STRING_LOOSE ) )
            {
                if( useCharactersInsteadOfWords( locale ) )
                    return StringUtils.isLooseCharMatch( inValue.trim(), matchValue.trim() );
                
                return StringUtils.isLooseMatch( inValue.trim(), matchValue.trim() );
            }            
        }

        catch( NumberFormatException e )
        {
            LogService.logIt(e, "FillBlankType." + name + ".valuesMatch( " + inValue + ","+ matchValue + " ) returning false." );
        }

        return false;
    }

    private boolean useCharactersInsteadOfWords( Locale locale )
    {
        if( locale!=null )
        {
            String ln = locale.getLanguage();
            if( ln==null || ln.isBlank() )
                return false;
            return ln.equalsIgnoreCase( "zh" ) || ln.equalsIgnoreCase( "ja" ) || ln.equalsIgnoreCase( "ko" );            
        }
        
        return false;
    }
    
    

}
