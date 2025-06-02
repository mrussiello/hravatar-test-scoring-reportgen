package com.tm2score.global;


import com.tm2score.service.LogService;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;



/**
 * Various utilities for localizing information
 */
public class I18nUtils
{
    private static final Set<String> RTL;
    private static final Set<String> NONASCII;


    static {
        Set<String> lang = new HashSet<>();
        lang.add("ar");
        lang.add("dv");
        lang.add("fa");
        lang.add("ha");
        lang.add("he");
        lang.add("ji");
        lang.add("ps");
        lang.add("ur");
        lang.add("yi");
        RTL = Collections.unmodifiableSet(lang);

        lang = new HashSet<>();
        lang.add("ru");
        lang.add("zh");
        NONASCII = Collections.unmodifiableSet(lang);

    }

    public static boolean isTextRTL( Locale locale )
    {
      if( locale == null )
          return false;

      return RTL.contains(locale.getLanguage());
    }

    public static boolean isTextNonAscii( Locale locale )
    {
      if( locale == null )
          return false;

      return NONASCII.contains(locale.getLanguage());
    }
    
    public static char getThousandsSeparator( Locale locale )
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        return symbols.getGroupingSeparator();
    }
    
    
    public static char getDecimalSeparator( Locale locale )
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        return symbols.getDecimalSeparator();
    }
    
    

    /**
     * get a formatted number string with set digits.
     *
     * @param _locale The current java.util.Locale
     * @param _number The number to be formatted (double)
     * @param _fixedFractionDigits The number of digits to the right of decimal point.
     *
     */
    public static String getFormattedNumber( Locale _locale,
                                             double _number,
                                             int _fixedFractionDigits )
    {
        if( _locale == null )
            _locale = Locale.US;

        NumberFormat nf = NumberFormat.getNumberInstance(  _locale  );
        nf.setRoundingMode(  _fixedFractionDigits>0 ? RoundingMode.HALF_UP : RoundingMode.FLOOR );        
        nf.setMaximumFractionDigits(_fixedFractionDigits );        
        nf.setMinimumFractionDigits(_fixedFractionDigits);
        return nf.format( _number );
    }



    /**
     * get a formatted integer string
     *
     * @param _locale The current java.util.Locale
     * @param _number The number to be formatted (int)
     *
     */
     public static String getFormattedInteger( Locale _locale,
                                               long _number )
    {
         if( _locale == null )
             _locale = Locale.US;

        NumberFormat numberFormatter = NumberFormat.getIntegerInstance(  _locale  );

        numberFormatter.setRoundingMode( RoundingMode.FLOOR );
                
        return numberFormatter.format( _number );

    }


    /**
     * get a formatted currency string with set digits.
     *
     * @param _locale The current java.util.Locale
     * @param _amount The number to be formatted (double)
     * @param _maxFractionDigits The number of digits to the right of decimal point.
     *
     */
     public static String getFormattedCurrency( Locale _locale,
                                               double _amount,
                                               int _maxFractionDigits )
    {

         if( _locale == null )
             _locale = Locale.US;

        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(  _locale  );

        numberFormatter.setMaximumFractionDigits( _maxFractionDigits );

        numberFormatter.setRoundingMode(  _maxFractionDigits>0 ? RoundingMode.HALF_UP : RoundingMode.FLOOR );
        
        return numberFormatter.format( _amount );

    }




    /**
     * get a formatted percentage string with set digits.
     *
     * @param _locale The current java.util.Locale
     * @param _number The number to be formatted (double)
     * @param _maxFractionDigits The number of digits to the right of decimal point.
     *
     */
    public static String getFormattedPercent(  Locale _locale,
                                               double _number,
                                               int _maxFractionDigits )
    {



        NumberFormat numberFormatter = NumberFormat.getPercentInstance(  _locale  );

        numberFormatter.setMaximumFractionDigits( _maxFractionDigits );

        numberFormatter.setRoundingMode( _maxFractionDigits>0 ? RoundingMode.HALF_UP : RoundingMode.FLOOR );
        
        return numberFormatter.format( _number );

    }



    /**
     * get a formatted date string in the desired style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param _style desired style.
     *
     */
    public static String getFormattedDate(  Locale _locale,
                                            Date _date,
                                            int _style )
    {

        if( _locale == null )
            _locale = Locale.US;

        DateFormat dateFormatter = DateFormat.getDateInstance(  _style, _locale  );

        return dateFormatter.format( _date );

    }


    /**
     * get a formatted date string in the desired style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param _style desired style.
     *
     */
    //public static String getFormattedDate(  Locale _locale,
    //                                        Date _date,
    //                                        String _pattern )
    //{

    //    if( _locale == null )
    //        _locale = Locale.US;

    //    SimpleDateFormat dateFormatter = new SimpleDateFormat( _pattern , _locale );

    //    return dateFormatter.format( _date );

    //}

    //public static String getFormattedDate(  Locale _locale,
    //                                        TimeZone timezone,
    //                                        Date _date,
    //                                        String _pattern )
    //{

    //    if( _locale == null )
    //        _locale = Locale.US;

    //    SimpleDateFormat dateFormatter = new SimpleDateFormat( _pattern , _locale );

    //    if( timezone != null )
    //        dateFormatter.setTimeZone( timezone );
                
    //    return dateFormatter.format( _date );

    //}





    /**
     * get a formatted date string in the standard style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     *
     */
    public static String getFormattedDate(  Locale _locale, TimeZone _tz, Date _date)
    {

        if( _date==null )
            return "";
        
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, _locale); 
        return dateFormatter.format( _date );
        // return getFormattedDate(  _locale, _tz, _date, DateFormat.LONG  );
    }


    /**
     * get a formatted date and time string in the desired style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param timezone TODO
     * @param _style desired style.
     *
     */
    public static String getFormattedDateTime(  Locale _locale,
                                                Date _date,
                                                int _dateStyle,
                                                int _timeStyle,
                                                TimeZone timezone )
    {
        if( _locale == null )
            _locale = Locale.US;

        DateFormat dateFormatter = DateFormat.getDateTimeInstance(  _dateStyle, _timeStyle, _locale  );

        if( timezone != null )
            dateFormatter.setTimeZone( timezone );

        return dateFormatter.format( _date );

    }



    /**
     * get a formatted date time string in the standard style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param timezone TODO
     *
     */
    public static String getFormattedDateTime(  Locale _locale,
                                                Date _date,
                                                TimeZone timezone )
    {
        if( _locale == null )
            _locale = Locale.US;

        if( _date == null )
            _date = new Date();

        if( timezone == null )
            timezone = TimeZone.getDefault();

        return getFormattedDateTime(  _locale, _date, DateFormat.LONG , DateFormat.LONG, timezone );
    }


    public static String getLanguageFromLocaleStr( String inStr )
    {
        return getLocaleFromCompositeStr( inStr ).getLanguage();
    }
    
    public static String getLanguageFromLocale( Locale l )
    {
        return l==null ? "en" : l.getLanguage();
    }
    

    public static Locale getLocaleFromCompositeStr( String inStr )
    {
        try
        {
            if( inStr == null || inStr.trim().isEmpty() || inStr.equals("ttln") || inStr.equals("brln") )
                return Locale.US;


            String[] pieces = inStr.indexOf("-")>0 ? inStr.split("-") : inStr.split( "_" );

            if( pieces.length == 0 )
                return Locale.US;

            if( pieces.length == 1 )
                return Locale.of( pieces[0] );

            else
                return Locale.of( pieces[0] , pieces[1] );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "getLocaleFromCompositeStr( " + inStr + " ) " );
            return Locale.US;
        }
    }
}