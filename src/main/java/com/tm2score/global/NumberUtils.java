package com.tm2score.global;

import com.tm2score.service.LogService;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.apache.commons.math3.distribution.*;



/**
 * This class provides a variety of useful number-oriented utilities
 */
public class NumberUtils
{

    public static float convertZScoreToPercentile(float zScore)
    {
            double percentile = 0;

            NormalDistribution dist = new NormalDistribution();
            percentile = dist.cumulativeProbability(zScore) * 100;
            return (float) percentile;
    }    
    

    public static float applyNormToZScore( float value, float mean, float std )
    {
        return value*std + mean;
    }

    public static String getPctSuffixStr( Locale locale, float pp, int precision )
    {
        if( 1==1 )
        {
            return I18nUtils.getFormattedNumber( locale , pp, precision ) + getPctSuffix( locale, pp, precision );
        }
        
        float v = (float) roundIt( pp , precision ); //  Float.parseFloat( I18nUtils.getFormattedNumber( locale , pp, precision ) );

        return precision == 0 ? ((int)v) + getPctSuffix( locale, v, precision ) : Float.toString(v);
    }



    public static String getPctSuffix( Locale locale, float pp, int precision )
    {
        if( precision>0 )
            return "";



        // String nm = I18nUtils.getFormattedNumber( locale , ((int)pp), 0 );
        String nm = I18nUtils.getFormattedNumber( locale , pp, 0 );

        if( nm.endsWith( "11" ) || nm.endsWith( "12" ) || nm.endsWith( "13" ) )
            return "th";

        int p = (int) Integer.parseInt( nm );

        // zeroeth
        if( p < 1 )
            return "th";

        int m = p%10;

        // LogService.logIt( "BaseCoreTestReportTemplate.getPctSuffix() p=" + p + ", int value=" + ((int)p) + ", Modulus=" + ((int)p)%10 );

        if( m==0 || m>3 )
            return "th";

        else if( m==1 )
            return "st";

        else if( m==2 )
            return "nd";

        else
            return "rd";

    }






    public static boolean isOdd( int num )
    {
    	return num % 2 != 0;
    }

    /**
     * Returns a number rounded to the requested number of decimal places
     */
    public static double roundIt( double theNumber , int decimalPlaces )
    {
        int bump = 1;

        for( int i=0 ; i<decimalPlaces ; i++ )
            bump *= 10;

        theNumber *= bump;

        theNumber = Math.floor( theNumber );

        return theNumber/bump;

    }


    public static int parseIntegerInputStr( Locale locale , String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
        {
            LogService.logIt( "NumberUtils.parseIntegerInputStr() ERROR Parsing " + inStr + ", EMPTY STRING Returning 0"  );
            return 0;
        }

        try
        {
            return Integer.parseInt(inStr);
        }

        catch( NumberFormatException e )
        {
            // LogService.logIt( "NumberUtils.parseIntegerInputStr() Using Integer ERROR Parsing " + inStr );
        }

        if( locale == null )
            locale = Locale.US;
        
        String inStrU = makeUSFloatStr( locale, inStr );

        // First try locale.
        try
        {
            
            
            NumberFormat fmt = NumberFormat.getInstance( Locale.US );

            fmt.setRoundingMode( RoundingMode.HALF_UP );
                    
            Number n = fmt.parse( inStrU );

            // LogService.logIt( "NumberUtils.parseIntegerInputStr() Parsed " + inStr + " to float value=" + n.floatValue() + ", returing intValue=" + n.intValue() );
            
            return n.intValue();
        }

        catch( ParseException e )
        {
            // LogService.logIt( "NumberUtils.parseIntegerInputStr() ERROR Parsing " + inStr + ", modified Str=" + inStrU + ", locale=" + locale.toString() + ", " + e.toString()  );
        }

        char ch;
        StringBuilder sb = new StringBuilder();

        // Remove all non-numerics
        for( int i=0; i<inStr.length(); i++ )
        {
            ch = inStr.charAt(i);

            if( Character.isDigit( ch ) )
                sb.append( ch );
        }

        if( sb.length()==0 )
            return 0;

        try
        {
            // LogService.logIt( "NumberUtils.parseIntegerInputStr() Plan C - Parsed " + inStr + " to =" + sb.toString() );
            
            return Integer.parseInt( sb.toString() );
        }

        catch( NumberFormatException e )
        {
            LogService.logIt( e, "NumberUtils.parseIntegerInputStr() " + inStr );
        }

        return 0;
    }






    public static float parseFloatInputStr( Locale locale , String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
        {
            LogService.logIt( "NumberUtils.parseFloatInputStr() ERROR Parsing " + inStr + ", EMPTY STRING Returning 0"  );
            return 0;
        }

        // remove any hard returns.
        if( inStr.indexOf( "\n" ) > 0 )
        {
            // LogService.logIt( "NumberUtils.parseFloatInputStr() Removing Hard Returns from num str original=" + inStr + " -> " + inStr.substring( 0, inStr.indexOf( "\n" ) )  );
            inStr = inStr.substring( 0, inStr.indexOf( "\n" ) );
        }

        if( locale == null )
            locale = Locale.US;

        try
        {
            return Float.parseFloat(inStr);
        }

        catch( NumberFormatException e )
        {
            // LogService.logIt( "NumberUtils.parseFloatInputStr() Using Float ERROR Parsing " + inStr  + ", attempting to make the string into a Float." );
        }

        String inStrU = makeUSFloatStr( locale, inStr );


        // First try locale.
        try
        {
            NumberFormat fmt = NumberFormat.getInstance( Locale.US );

            fmt.setRoundingMode( RoundingMode.HALF_UP );
            
            fmt.setMaximumFractionDigits(4);

            Number n = fmt.parse(inStrU);

            return n.floatValue();
        }

        catch( ParseException e )
        {
            LogService.logIt( "NumberUtils.parseFloatInputStr() Using NumberFormat ERROR Parsing " + inStr + ", modified Str=" + inStrU + ", locale=" + locale.toString() + ", " + e.toString() );
        }

        char ch;
        StringBuilder sb = new StringBuilder();

        // Remove all non-numerics except , . and space
        for( int i=0; i<inStrU.length(); i++ )
        {
            ch = inStr.charAt(i);

            if( ch == ',' || ch=='.' || ch==' ' || Character.isDigit( ch ) )
                sb.append( ch );
        }

        if( sb.length()==0 )
            return 0;

        // LogService.logIt( "NumberUtils.parseFloatInputStr() cleaned AAA str=" + sb.toString() );

        // if string has changed, try that.
        if( !sb.toString().equals( inStrU ) )
            return parseFloatInputStr( locale , sb.toString() );

        return 0;

    }

    public static String makeUSFloatStr( Locale locale, String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
            return "0";

        // it's Locale.US, so ensure periods and commas are right.
        String inStr2 = inStr; // sb.toString();

        inStr2 = inStr2.replaceAll(" ", "" );

        int idx1 = inStr2.lastIndexOf(',');
        int idx1a = inStr2.indexOf(',');

        int idx2 = inStr2.lastIndexOf('.');
        int idx2a = inStr2.indexOf('.');

        // has both commas and periods. Comma is last and only one comma. Remove periods and change comma to period 
        if( idx1>=0 && idx2>=0 && idx2<idx1 && idx1==idx1a )
        {
            inStr2 = inStr2.replaceAll("\\.", "" );
            inStr2 = inStr2.replaceAll(",", "." );
        }

        // has both commas and periods. Period is last and only once period. remove the commas.
        if( idx1>=0 && idx2>=0 && idx1<idx2 && idx2==idx2a )
            inStr2 = inStr2.replaceAll(",", "" );

        // has multiple periods and no commas - remove the periods
        else if( idx1<0 && idx2>=0 && idx2 != idx2a )
            inStr2 = inStr2.replaceAll("\\.", "" );

        // has multiple commas - remove the commas
        else if( idx1>=0 && idx1!=idx1a )
            inStr2 = inStr2.replaceAll(",", "" );

        // Has one comma and NOT US - make it a period.
        else if( idx1>=0 && idx1==idx1a && !locale.equals( Locale.US) )
            inStr2 = inStr2.replaceAll(",", "." );

        // Has one comma and US - remove it
        else if( idx1>=0 && idx1==idx1a && locale.equals( Locale.US) )
            inStr2 = inStr2.replaceAll(",", "" );


        // Final scrub. Removes non-numeric chars.
        inStr2 = inStr2.replaceAll("[^\\d.]", "");
        
        
        // LogService.logIt( "NumberUtils.makeUSFloatStr() " + locale.toString() + " -> " + inStr + " -> " + inStr2 );
        return inStr2;


        //try
        //{
        //    return (new Float( inStr2 )).toString();
        //}

        //catch( NumberFormatException e )
        //{
        //    LogService.logIt( e, "NumberUtils.makeUSFloatStr() original String=" + inStr + ", modified String=" + inStr2 );
        //}

        //return "0";


    }




    /**
     * Always returns a string matching this pattern: ###,##0.00 where
     * the # signs are optional (will not show up if 0, and the 0 symbols are numbers that always
     * show up.
     *
     * This is set to avoid any effects from rounding inside the decimal format class.
     *
     */
    public static String getTwoDecimalFormattedAmount( double theNumber )
    {
        DecimalFormat decimalFormat = new DecimalFormat( "###,##0.000" );

        String temp = decimalFormat.format( theNumber );

        return temp.substring( 0 , temp.length() - 1 );
    }


    public static String getOneDecimalFormattedAmount( double theNumber )
    {
        DecimalFormat decimalFormat = new DecimalFormat( "###,##0.00" );

        String temp = decimalFormat.format( theNumber );

        return temp.substring( 0 , temp.length() - 1 );
    }





}