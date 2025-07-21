/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.essay;

import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Arabic;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.AustralianEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.language.NewZealandEnglish;
import org.languagetool.language.SouthAfricanEnglish;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.Chinese;
import org.languagetool.language.Dutch;
import org.languagetool.language.French;
import org.languagetool.language.Italian;
import org.languagetool.language.Japanese;
import org.languagetool.language.Portuguese;
import org.languagetool.language.Spanish;
import org.languagetool.language.Russian;
import org.languagetool.language.Romanian;
import org.languagetool.language.BrazilianPortuguese;
import org.languagetool.language.Persian;
import org.languagetool.language.Greek;


// import org.languagetool.language.Serbian;
import org.languagetool.language.Polish;
import org.languagetool.language.Slovak;




        
        
        
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
// import org.languagetool.rules.patterns.PatternRule;




/**
 *
 * @author Mike
 */
public class LocalEssayScoringUtils {

    private static String[] SPELLING_RULE_CATEGORY_NAMES = new String[] { "Possible Typo", "Commonly Confused Words", "Nonstandard Phrases" };

    private static String[] GRAMMAR_RULE_CATEGORY_NAMES = new String[] { "Grammar","Collocations","Punctuation Errors" };

    private static String[] STYLE_RULE_CATEGORY_NAMES = new String[] { "Miscellaneous", "Slang","Redundant Phrases", "Bad style", "Semantic" };
    
    private static String[] SUPPORTED_LANGS = new String[] {"de","en","es","ru","ro","nl","fr","it","ja","pt","pl","gr","sr","sk","fa","zh","ar"};


    public static List<String> SPELLING = null;
    public static List<String> GRAMMAR = null;
    public static List<String> STYLE = null;


    public static synchronized void init()
    {
        if( SPELLING == null )
        {
            SPELLING = new ArrayList<>();
            GRAMMAR = new ArrayList<>();
            STYLE = new ArrayList<>();

            for( String s : SPELLING_RULE_CATEGORY_NAMES )
                SPELLING.add( s.toLowerCase() );

            for( String s : GRAMMAR_RULE_CATEGORY_NAMES )
                GRAMMAR.add( s.toLowerCase() );

            for( String s : STYLE_RULE_CATEGORY_NAMES )
                STYLE.add( s.toLowerCase() );
        }
    }



    public static boolean isValidWriting( String text, Locale locale, String countryCode, List<String> wordsToIgnoreLc)
    {
        if( text == null || text.isEmpty() )
            return true;

        try
        {
            int numWords = StringUtils.numWords( text );

            int[] vals = getWritingErrorCount(text, locale, countryCode, wordsToIgnoreLc );

            float[] fractErrs = new float[4];

            if( numWords > 0 )
            {
                fractErrs[0] = ((float) vals[0])/((float) numWords );
                fractErrs[1] = ((float) vals[1])/((float) numWords );
                fractErrs[2] = ((float) vals[2])/((float) numWords );
                fractErrs[3] = ((float) vals[3])/((float) numWords );
            }

             // LogService.logIt("LocalEssayScoringUtils.isValidWriting() numWords=" + numWords + " total errors=" + vals[0] + ", Spelling Errors: " + vals[1] + ", grammar errors=" + vals[2]  + " style errors=" + vals[2] + ", Locale=" + locale.toString() + ", Text=" + text );

             return fractErrs[1] < 0.3f && fractErrs[2]<0.1f && fractErrs[3]<0.1f;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "LocalEssayScoringUtils.isValidWriting() Locale=" + locale.toString() + ", Text=" + text );

            return true;
        }
    }

    
    
    

    /**
     * Returns
     *    data[0] = total matches (errors)
     *    data[1] = spelling errors
     *    data[2] = grammar errors
     *    data[3] = style errors
     *    data[4] = total words
     * 
     *   data[7] = was spelling/grammar/style performed.
     * 
     *
     * @param text
     * @param locale
     * @return
     * @throws Exception
     */
    public static int[] getWritingErrorCount( String text, Locale locale, String authorCountryCode, List<String> wordsToIgnoreLc) throws Exception
    {
        Object[] anal = getWritingAnalysis(text, locale, authorCountryCode, wordsToIgnoreLc, true );
        return (int[]) anal[0];
    }    

    
    public static boolean getWritingAnalysisSupported( String language ) throws Exception
    {
        for( String l : LocalEssayScoringUtils.SUPPORTED_LANGS )
        {
            if( l.equalsIgnoreCase(language))
                return true;
        }
        return false;        
    }
    
    /**
     * Returns
     *    
     *  out[0]= int[]
     *            data[0] = total matches (errors)
     *            data[1] = spelling errors
     *            data[2] = grammar errors
     *            data[3] = style errors
     *            data[4] = total words
     *            data[5] = percent words that are duplicated at least 3 times
     *            data[6] = percent of words 4 or more chars that are duplicated 3 times or more.
     *            data[7] = 0 if no spelling/grammar/style analysis was done. 1 if this analysis was performed.
     *
     * out[1] = Map<String, Integer> - misspelled words, counts
     * 
     * 
     * @param text
     * @param locale
     * @return
     * @throws Exception
     */
    public static Object[] getWritingAnalysis( String text, Locale locale, String authorCountryCode, List<String> wordsToIgnoreLc, boolean spellingGrammarOk) throws Exception
    {
        if( locale == null )
            locale = Locale.US;

        String language = locale.getLanguage();
        String country = locale.getCountry();

        Object[] data = getWritingAnalysisForCountry(text, language, country, spellingGrammarOk, wordsToIgnoreLc );
        
        if( !spellingGrammarOk )
            return data;
        
        // No need to take a second look. No authorCountryCode or same as locale.country or language is not english (only have different code for different versions of english and portuguese).
        if( authorCountryCode==null || authorCountryCode.isEmpty() || authorCountryCode.equalsIgnoreCase( country ) || ( !language.equalsIgnoreCase( "en") && !language.equalsIgnoreCase( "pt") ) )
            return data;
        
        
        Object[] data2 = getWritingAnalysisForCountry(text, language, authorCountryCode, spellingGrammarOk, wordsToIgnoreLc );
        
        if( data2==null || data2[0]==null || data[0]==null )
            return data;
        
        int[] errs = (int[]) data[0];
        int[] errs2 = (int[]) data2[0];
        
        //Integer errs = (Integer) data[0];
        //Integer errs2 = (Integer) data2[0];
        
        // return the array with the fewest errors.
        return errs[0]<errs2[0] ? data : data2;
        
    }
    
    
    
    
    /**
     * Returns
     *    
     *  out[0]= int[]
     *            data[0] = total matches (errors)
     *            data[1] = spelling errors
     *            data[2] = grammar errors
     *            data[3] = style errors
     *            data[4] = total words
     *            data[5] = percent words that are duplicated at least 3 times
     *            data[6] = percent of words 4 or more chars that are duplicated 3 times or more.
     *            data[7] = 0 if no spelling/grammar/style analysis was done. 1 if this analysis was performed.
     *
     * out[1] = Map<String, Integer> - misspelled words, counts
     * 
     */    
    public static Object[] getWritingAnalysisForCountry( String text, String language, String country, boolean spellingGrammarOk, List<String> wordsToIgnoreLc) throws Exception
    {
        init();

        // LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() START language=" + language + ", country=" + country  );        

        Object[] out = new Object[2];
        
        int[] nums = new int[8];
        
        out[0] = nums;
        
        Map<String, Integer> spErrs = new TreeMap<>();
        
        out[1] = spErrs;

        if( text == null || text.isEmpty() )
            return out;

        //LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() AA11" );        
        nums[4] = StringUtils.numWords(text);

        
        
        //LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() AAA numwords=" + out[4] );        
        
        try
        {
            String textLc = text.toLowerCase();
            List<String> words = Arrays.asList(textLc.split(" "));

            // dd[0]=unique words 
            // dd[1]=unique words that are dupes
            // dd[2]=unique words 4 chars or more
            // dd[3]=unique words 4 chars or more duped 3 times or more
            int[] dd = new int[4];
            Set<String> wds = new HashSet<>();
            int freq;
            for( String w : words )
            {
                if( !wds.add( w ) )
                    continue;
                dd[0]++;

                freq = Collections.frequency( words, w );                
                if( freq>=3 )
                    dd[1]++;

                if( w.length()>=4 )
                {
                    dd[2]++;
                    if( freq>=3 )
                        dd[3]++;
                }

            }


            // Percent of words that are dupes
            nums[5] = dd[0]<=0 ? 0 : (int)(100*((float)dd[1])/((float)dd[0]));

            // percent of words 4 chars or more that are duped 3 times or more.
            nums[6] = dd[2]<=0 ? 0 : (int)(100*((float)dd[3])/((float)dd[2]));
            //if( locale == null )
            //    locale = Locale.US;

            // String langCode = locale.getLanguage();
            // String country = locale.getCountry();
            
            if( !spellingGrammarOk )
            {
                nums[7]=0;
                return out;
            }
            
            Language lang = null;


            if( language.equalsIgnoreCase( "de" ) )
            {
                if( country.equalsIgnoreCase( "DE" ) )
                    lang = new GermanyGerman();
                
                else
                    lang = new GermanyGerman();
            }

            else if( language.equalsIgnoreCase( "en" ) )
            {
                // Adjustments
                if( country.equalsIgnoreCase( "IN" ) )
                    country="GB";
                else if( country.equalsIgnoreCase( "IE" ) )
                    country="GB";                
                
                if( country.equalsIgnoreCase( "GB" ))
                    lang = new BritishEnglish();
                else if( country.equalsIgnoreCase( "AU" ))
                    lang = new AustralianEnglish();
                else if( country.equalsIgnoreCase( "NZ" ))
                    lang = new NewZealandEnglish();
                else if( country.equalsIgnoreCase( "CA" ))
                    lang = new CanadianEnglish();
                else if( country.equalsIgnoreCase( "ZA" ))
                    lang = new SouthAfricanEnglish();
                else
                    lang = new AmericanEnglish();
            }
            
            else if( language.equalsIgnoreCase( "es" ) )
                lang = new Spanish();

            else if( language.equalsIgnoreCase( "ru" ) )
                lang = new Russian();

            else if( language.equalsIgnoreCase( "ro" ) )
                lang = new Romanian();

            else if( language.equalsIgnoreCase( "nl" ) )
                lang = new Dutch();

            else if( language.equalsIgnoreCase( "fr" ) )
                lang = new French();

            else if( language.equalsIgnoreCase( "it" ) )
                lang = new Italian();

            else if( language.equalsIgnoreCase( "ja" ) )
                lang = new Japanese();

            else if( language.equalsIgnoreCase( "pt" ) )
            {
                if( country.equalsIgnoreCase( "BR" ) )
                    lang = new BrazilianPortuguese();
                    
                else
                    lang = new Portuguese();
            }

            else if( language.equalsIgnoreCase( "pl" ) )
                lang = new Polish();

            else if( language.equalsIgnoreCase( "gr" ) )
                lang = new Greek();

            else if( language.equalsIgnoreCase( "ar" ) )
                 lang = new Arabic();

            else if( language.equalsIgnoreCase( "sk" ) )
                lang = new Slovak();

            else if( language.equalsIgnoreCase( "fa" ) )
                lang = new Persian();

            else if( language.equalsIgnoreCase( "zh" ) )
                lang = new Chinese();

            // Unsupported Language. Just return 0's
            else
            {
                nums[7]=0;
                //lang = new AmericanEnglish();
                LogService.logIt("LocalEssayScoringUtils.getWritingAnalysisForCountry() Unsupported language. Returning empty values. Language: " + language + ", words=" + nums[4] + ", rule matches=" + nums[0] + ", spell=" + nums[1] + ", grammar=" + nums[2] + ", style=" + nums[3] );  
                return out;
            }
            
            //if( RuntimeConstants.getBooleanValue("httpsONLY") )
            //{
            //    NativeLibrary.addSearchPath("linux-aarch64/libhunspell.so", "/usr/lib64/libhunspell-1.3.so.0");
            //    NativeLibrary.addSearchPath("libhunspell.so", "/usr/lib64/libhunspell-1.3.so.0");
            //}
            
            JLanguageTool langTool = null;
            
        //LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() BBB " ); 
            int tryCount = 0;
            
            while( langTool==null && tryCount<5 )
            {
                try
                {
                    tryCount++;
                    langTool = new JLanguageTool( lang );
                }
                catch( ConcurrentModificationException e )
                {
                    langTool = null;
                    if( tryCount<5 )
                    {
                        LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() BBB.2 NONFATAL " + e.toString() + " tryCount=" + tryCount + " will try again after ~1 sec wait." );
                        long v = Math.round(Math.random()*1000f);
                        
                        Thread.sleep(1000 + v );
                    } 
    
                    else
                        throw e;
                }
            }
        //LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() CCC " );        
            // langTool.activateDefaultPatternRules();
        //LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() DDD " );        
            List<RuleMatch> matches = langTool.check( text );
        
            // LogService.logIt( "LocalEssayScoringUtils.getWritingErrorCount() EEE matches=" + matches.size() );        

            String category;
            
            // String wd;

            nums[0] = matches.size();
            
            int fm;
            int to;
            String sperr;
            Integer spct;
            int sperrSkips = 0;

            for( RuleMatch m : matches )
            {
                category = m.getRule().getCategory().getName().toLowerCase();                
                
                if( SPELLING.contains( category ) || m.getRule() instanceof SpellingCheckRule )
                {
                    nums[1]++;
                   
                    fm = m.getFromPos();
                    to = m.getToPos();
                    if( fm>=0 && to>=fm && to<=text.length() )
                    {
                        sperr = text.substring(fm,to);
                        
                        sperr = sperr.trim().toLowerCase();
                        
                        if( sperr.isEmpty() )
                            continue;
                        
                        if( wordsToIgnoreLc!=null && wordsToIgnoreLc.contains(sperr) )
                        {
                            sperrSkips++;
                            continue;
                        }
                        
                        spct = spErrs.get( sperr );
                        
                        spErrs.put(sperr, (int)( spct==null ? 1 : spct + 1 ) );
                    }
                }

                else if( GRAMMAR.contains( category ) )
                    nums[2]++;

                else if( STYLE.contains( category ) )
                    nums[3]++;

                else
                {
                    // LogService.logIt( "LocalEssayScoringUtils.getWritingAnalysisForCountry() Unidentified Rule Category match. Adding to GRAMMAR errors.  Rule=" + m.getRule().getDescription() + ", id=" + m.getRule().getId() + ", category=" + m.getRule().getCategory().getName() );
                    nums[2]++;
                }
            }
            
            if( sperrSkips>0 )
            {
                nums[1]-=sperrSkips;
                if( nums[1]<0 )
                    nums[1]=0;
            }
            
            //if( !language.equalsIgnoreCase( "en" ) )
            //{
            //    LogService.logIt("LocalEssayScoringUtils.getWritingAnalysisForCountry() END language=" + language + ", words=" + nums[4] + ", rule matches=" + nums[0] + ", spell=" + nums[1] + ", grammar=" + nums[2] + ", style=" + nums[3] );                
            //}
            
            // indicate that spelling/style/grammar anal was done.
            nums[7]=1;
            
        }

        catch( Exception e )
        {
            LogService.logIt(e, "LocalEssayScoringUtils.getWritingAnalysisForCountry() language=" + language + ", country=" + country + ", Text=" + text );

            throw e;
        }

        return out;
    }

}
