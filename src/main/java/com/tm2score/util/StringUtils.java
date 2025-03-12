package com.tm2score.util;

import com.google.common.net.InternetDomainName;
import com.tm2score.entity.corp.Corp;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;




/**
 * This class is a set of static utility methods for working with String objects
 */
public class StringUtils
{
    private static final char[] alphaDigits = { '2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','P','Q','R','S','T','U','V','W','X','Y','Z' };

    private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
    
    // public static String removeNonAscii
    
    public static boolean isPureAscii(String v) 
    {
        return asciiEncoder.canEncode(v);
    }
    
    
    public static String removePunctuation(String str )
    {
        if( str==null )
            return str;        
        return str.replaceAll("\\p{P}", "");        
    }
    
    
    public static String removeWhitespaceAndControlChars(String str )
    {
        if( str==null )
            return str;        
        return removeNonPrintable(str).replaceAll("[\\r\\n\\t\\s]", "").toLowerCase();        
    }
    
    public static String removeNonAscii(String str){
        if( str==null )
            return str;        
        return str.replaceAll("[^\\x00-\\x7F]", "");
    }

    public static String removeNonPrintable(String str){ // All Control Char
        if( str==null )
            return str;        
        return str.replaceAll("[\\p{C}]", "");
    }

    public static String removeAllControlChars(String str)
    {
        if( str==null )
            return str;        
        return removeNonPrintable(str).replaceAll("[\\r\\n\\t]", "");
    }    
    
    
    
    public static boolean isValidURL(String url) {

        if( !url.toLowerCase().startsWith("http") )
           url = "http://" + url;

        return com.tm2score.util.HttpUtils.getURLFromString(url)!=null;
    }    
    
    
    public static String getTopDomain( String u )
    {
        if( u==null )
            return null;

        if( u.isBlank() )
            return "";
        
        u = u.toLowerCase();
        if( !u.startsWith("http") )
            u = "http://" + u;
        
        // int idx = u.indexOf("//");
        
        URL url = com.tm2score.util.HttpUtils.getURLFromString(u);
        if( url==null ) 
        {
            LogService.logIt("StringUtils.getTopDomain() Cannot parse URL: urlStr=" + u );
            return u;
        }   
        
        String host = url.getHost();
        String domain = null;
        try
        {
            InternetDomainName idn = InternetDomainName.from(host);
            while (idn.isTopPrivateDomain() == false && (idn.hasParent()) ) {
                idn = idn.parent();
            }
            domain = idn.toString();
            if (idn.isUnderPublicSuffix()) {
                domain =idn.topPrivateDomain().toString();
            }
        }
        catch( IllegalArgumentException e )
        {
            LogService.logIt( "StringUtils.getTopDomain() ERROR " + e.toString() + ", url=" + u );
            return host;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "StringUtils.getTopDomain() url=" + u );
            return host;
        }
        
        //LogService.logIt("StringUtils.getTopDomain() parsed domain is: " + domain + ", url=" + u );
        
        return domain;        
    }
    
    
    
    public static boolean getBooleanReportFlag( String name, Org org, Suborg suborg, Report report, Product product, Corp corp)            
    {
        Map<String,String> mp = StringUtils.getReportFlagMap(org, suborg, report, product, corp);
        return mp.get(name)!=null && mp.get(name).equals("1");
    }
    
    
    public static Map<String,String> getReportFlagMap( Org org, Suborg suborg, Report report, Product product, Corp corp)
    {
        Map<String,String> out = new HashMap<>();
        
        // Start with Report - lowest level
        if( report != null )
            out =  getReportFlagMap(report.getReportFlags());

        // Product is the next level.
        if( product!=null )
            out.putAll( getReportFlagMap(product.getStrParam11()) );        
                
        // Next level is Org
        if( org!=null )
            out.putAll( getReportFlagMap(org.getReportFlags()) );        

        // Highest level is Suborg list
        if( suborg != null )
            out.putAll( getReportFlagMap(suborg.getReportFlags()) );        

        // Highest level is Corp list
        if( corp != null )
            out.putAll( getReportFlagMap(corp.getProctorParams()) );        
        
        return out;
    }
    
    
    public static Map<String,String> getReportFlagMap( String inStr )
    {
        Map<String,String> out = new HashMap<>();
        if( inStr==null || inStr.isEmpty() )
            return out;

        StringTokenizer st = new StringTokenizer( inStr, "|" );
        String rule;
        String value;

        while( st.hasMoreTokens() )
        {
            rule = st.nextToken();
            if( !st.hasMoreTokens() )
                break;
            value = st.nextToken();
            if( rule != null && !rule.isEmpty() && value!=null && !value.isEmpty() )
                out.put( rule,value );
        }
        return out;
    }
    
    
    public static String padIntegerToLength( int theInt, int theLength )
    {
    	String s = Integer.toString(theInt);
        StringBuilder sb = new StringBuilder();
        
        while( sb.length()<theLength-s.length() )
            sb.append("0");
        sb.append( s );
        return sb.toString();
    }
    
    
    public static List<Integer> getIntList( String inStr )
    {
        List<Integer> l = new ArrayList<>();

        if( inStr==null || inStr.isBlank() )
            return l;

        String[] ids = inStr.split( "," );
        for( int i=0; i<ids.length; i++ )
        {
            if( ids[i].isBlank() )
                continue;
            try
            {
                l.add(Integer.valueOf(ids[i].trim()) );
            }
            catch( Exception e )
            {
                LogService.logIt( e, "StringUtils.getIntList() " + inStr );
            }
        }
        return l;
    }
    
    public static List<Long> getLongList( String inStr )
    {
        List<Long> l = new ArrayList<>();

        if( inStr==null || inStr.isBlank() )
            return l;

        String[] ids = inStr.split( "," );
        for( int i=0; i<ids.length; i++ )
        {
            if( ids[i].isBlank() )
                continue;
            try
            {
                l.add(Long.valueOf(ids[i].trim()) );
            }
            catch( Exception e )
            {
                LogService.logIt( e, "StringUtils.getLongList() " + inStr );
            }
        }
        return l;
    }
    
    
    public static NVPair getNVPairFromList( String name, String inStr, String delim )
    {
        if( name==null || name.isEmpty() )
            return null;

        List<NVPair> out = parseNVPairsList( inStr, delim );
        
        for( NVPair p : out )
        {
            if( p.getName()!=null && p.getName().equals(name ) )
                return p;
        }
        
        return null;
    }

    
    
    
    public static boolean isLooseMatch( String a , String b )
    {
        if( a==null || b==null )
            return false;
        
        a=a.trim().toLowerCase();
        b=b.trim().toLowerCase();
        
        if( a.equalsIgnoreCase(b))
            return true;
        
        String ans = StringUtils.removeChar( a, ' ');
        String bns = StringUtils.removeChar( b, ' ');
        
        ans = StringUtils.removeChar( ans, '\t');
        bns = StringUtils.removeChar( bns, '\t');
        
        return ans.equalsIgnoreCase(bns);        
    }


    public static boolean isLooseCharMatch( String a , String b )
    {
        if( a==null || b==null )
            return false;
        
        a=a.trim().toLowerCase();
        b=b.trim().toLowerCase();
        
        if( a.equalsIgnoreCase(b))
            return true;
        
        String ans = StringUtils.removeChar( a, ' ');
        String bns = StringUtils.removeChar( b, ' ');
        
        ans = StringUtils.removeChar( ans, '\t');
        bns = StringUtils.removeChar( bns, '\t');
        
        String[] vWords = bns.trim().split("");      
        float matchCount=0;
        float totalCount=0;

        for( String vWd : vWords )
        {
            vWd = vWd.trim();

            if( vWd.isEmpty() )
                continue;
            
            totalCount++;

            if( ans.contains(vWd) )
                matchCount++;
        }

        // 70% match = Loose match
        if( totalCount>0 && matchCount/totalCount >= .7f ) 
            return true;
        
        return false;        
    }

    
    public static List<NVPair> parseNVPairsList( String inStr, String delim )
    {
        List<NVPair> out = new ArrayList<>();

        if( inStr==null || inStr.isEmpty() )
            return out;

        StringTokenizer st = new StringTokenizer( inStr, delim );

        String rule;
        String value;

        // LogService.logIt( "StringUtils.parseNVPairsList()  " + inStr );
        
        while( st.hasMoreTokens() )
        {
            rule = st.nextToken();

            if( !st.hasMoreTokens() )
                break;

            value = st.nextToken();
            
            // LogService.logIt( "StringUtils.parseNVPairsList() rule=" + rule + ", value=" + value );

            if( rule != null && !rule.isEmpty() && value!=null && !value.isEmpty() )
                out.add( new NVPair( rule,value ) );
        }

        
        return out;
    }

    
    public static String getUrlDecodedValue( String inStr )
    {
        if( inStr==null || inStr.isBlank() )
            return inStr;
        
        try
        {
            return URLDecoder.decode(inStr, "UTF8" );
        }
        catch( IllegalArgumentException e )
        {
            // Sometimes a string has a [] key where the last digit of the previous key is %. Ignore these.
            if( inStr.contains("%[") )
                return inStr;
            
            LogService.logIt( "StringUtils.getUrlDecodedValue() "  + e.toString() + ", " + inStr );
            return inStr;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "StringUtils.getUrlDecodedValue() " + inStr );
            return inStr;
        }
    }
    

    public static String getUrlEncodedValue( String inStr )
    {
        if( inStr==null || inStr.isBlank() )
            return inStr;
        
        try
        {
            return URLEncoder.encode(inStr, "UTF8" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "StringUtils.getUrlEncodedValue() " + inStr );
            return inStr;
        }
    }

    
    
    public static boolean isValidNameMatch( String n1, String ne1, String n2, String ne2 )
    {
        if( n1==null )
            n1="";
        else
            n1=n1.trim();
        
        if( n2==null)
            n2="";
        else
            n2=n2.trim();

        if( ne1==null )
            ne1="";
        else
            ne1=ne1.trim();

        if( ne2==null )
            ne2="";
        else
            ne2=ne2.trim();

        if( !n1.isEmpty()  )
        {
            if( n1.equalsIgnoreCase(n2) || n1.equalsIgnoreCase(ne2) )
                return true;
        }

        if( !ne1.isEmpty()  )
        {
            if( ne1.equalsIgnoreCase(n2) || ne1.equalsIgnoreCase(ne2) )
                return true;

        }

        return false;
    }

    /**
     * Returns a random alphanumeric string of the desired length
     *
     * @param length
     * @return
     */
    public static String generateRandomString( int length )
    {
        StringBuilder sb = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        int index;
        for( int i=0 ; i<length ; i++ )
        {
            index = secureRandom.nextInt(alphaDigits.length);

            sb.append( alphaDigits[ index ] );
        }

        return sb.toString();
    }    



    public static String removeQamail( String em )
    {
        if( em == null || em.toLowerCase().indexOf( "qamail" )!=0 || em.indexOf( "." )<=0 )
            return em;

        return em.substring(em.indexOf( "." ) +1, em.length() );
    }

    /*
    public static String removeNonAscii( String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
            return inStr;

        return inStr.replaceAll( "[^\\p{ASCII}]", "");
    }
    */



    public static String truncateToMaxWordCt( String s , int maxWords )
    {
         if( s==null || s.isEmpty() || maxWords <= 0 || numWords( s )< maxWords )
             return s;

         StringBuilder sb = new StringBuilder();

         Scanner scnr = new Scanner( s );

         String words[];

         String line;
         int c = 0;
         int cx;

         while( scnr.hasNextLine() && c<maxWords )
         {
             line = scnr.nextLine();

             cx = numWords( line );

             if( c+cx <= maxWords )
             {
                 sb.append( ( sb.length()>0 ? "\n" : "" ) + line );
                 c += cx;
                 continue;
             }

             words = line.trim().split(" ");

             for( int i=0;i<words.length;i++ )
             {
                 sb.append( (i>0 ? " " : "" ) + words[i] );
                 c++;

                 if( c>=maxWords )
                      break;
             }
         }

        return sb.toString();
    }

    /**
     * Calculates number of words where empty lines are treated as zero words. A word is a contiguous series of alphanumeric characters.
     *
     * @param s
     * @return
     */
    public static int numWords( String s )
    {
        if( s==null || s.isBlank() )
            return 0;

        if( 1==1 )
            return s.trim().split("\\s+").length;        
        
        Scanner scnr = new Scanner( s );

         String words[];

         int alphaNumWords=0;         
         int alphaWords =0;

         while( scnr.hasNextLine() )
         {
             words= scnr.nextLine().trim().split(" ");

            for(String word : words) 
            {
                if (!StringUtils.alphaNumCharsOnly(word).isBlank()) 
                {
                    alphaNumWords++;
                }
                if (!StringUtils.alphaCharsOnly(word).isBlank()) 
                {
                    alphaWords++;
                }
            }
         }
         
        // if( alphaWords<=0 || alphaWords<alphaNumWords/4 )
        //     return alphaWords<=0 ? 0 : alphaWords; 
         
        //if( alphaWords<=0 || alphaWords<alphaNumWords/4 )
        //    return 0; 

        return alphaNumWords;
    }

    public static String alphaCharsOnly( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        String outStr = "";

        for( int i=0 ; i<inStr.length() ; i++ )
        {
            if( Character.isLetter( inStr.charAt( i ) ) )
                outStr += inStr.charAt( i );
        }

        return outStr;
    }

    public static String alphaNumCharsOnly( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        String outStr = "";

        for( int i=0 ; i<inStr.length() ; i++ )
        {
            if( Character.isLetterOrDigit(inStr.charAt( i ) ) )
                outStr += inStr.charAt( i );
        }

        return outStr;
    }
    

    public static String removeBracketedArtifactFmStr( String inStr, String name )
    {
        if( inStr==null || inStr.isBlank() )
            return inStr;
        
        if( name==null || name.isBlank() )
            return inStr;
        
        return addBracketedArtifactToStr( inStr, name, null );
    }
    
    /*
     if value is null, the bracketed artifact is removed completely.
    */
    public static String addBracketedArtifactToStr( String inStr, String name, String value )
    {
        if( inStr==null )
            inStr = "";
        
        String t = inStr.trim();

        if( name==null || name.isBlank() )
            return null;

        name = name.trim();

        if( name.startsWith("[" ) )
            name = name.substring(1, name.length() );

        if( name.endsWith( "]") )
            name = name.substring(0,name.length()-1);

        if(name.isEmpty() )
            return null;

        // Is key already tehre?
        int idx = t.indexOf( "[" + name + "]" );

        // if there, is there a following key
        int idx2 = idx>=0 ? t.indexOf(  "[" , idx+2 + name.length() ) : -1;
        
        // no following key
        if( idx2 < 0 )
            idx2 = t.length();

        // key doesn't exist yet, so add to end.
        if( idx<0 )
            return t + "[" + name + "]" + value;

        // remove key
        String ta = idx>0 ? t.substring( 0, idx ) : "";
        String tb = idx2<t.length() ? t.substring(idx2, t.length()) : "";
        
        return ta + tb + (value!=null ? "[" + name + "]" + value : "" );        
    }

    public static String getCurleyBracketedArtifactFromString( String inStr, String name )
    {
        return getBracketedArtifact( inStr, name, "{", "}" );
    }
        

    public static String getBracketedArtifactFromString( String inStr, String name )
    {
        return getBracketedArtifact( inStr, name, "[", "]" );
        /*
        String t = inStr;

        if( name == null || t == null || t.isEmpty() || !t.contains(name) )
            return null;

        name = name.trim();

        if( name.startsWith("[" ) )
            name = name.substring(1, name.length() );

        if( name.endsWith( "]") )
            name = name.substring(0,name.length()-1);

        if(name.isEmpty() )
            return null;

        int idx = t.indexOf( "[" + name + "]" );

        if( idx <0 )
            return null;

        int idx2 = t.indexOf(  "[" , idx+2 + name.length() );

        if( idx2 < 0 )
            idx2 = t.length();

        return t.substring( idx + 2 + name.length() , idx2 ).trim();
        */
    }

    private static String getBracketedArtifact( String inStr, String name, String openStr, String closeStr )
    {
        String t = inStr;

        if( name == null || t == null || t.isEmpty() || !t.contains(name) )
            return null;

        name = name.trim();

        if( name.startsWith(openStr) )
            name = name.substring(openStr.length(), name.length() );

        if( name.endsWith(closeStr) )
            name = name.substring(0,name.length()-closeStr.length());

        if(name.isEmpty() )
            return null;

        int idx = t.indexOf( openStr + name + closeStr );

        if( idx<0 )
            return null;

        int idx2 = t.indexOf( openStr , idx + openStr.length() + name.length() + closeStr.length() );

        if( idx2 < 0 )
            idx2 = t.length();

        return t.substring( idx + openStr.length() + name.length() + closeStr.length(), idx2 ).trim();
    }
    

    public static boolean getHasHtml( String inStr )
    {
        if( inStr == null )
            return true;
        
        inStr=inStr.toLowerCase();

        if( !inStr.contains("<") || !inStr.contains(">") ) 
            return false;
        
        if( inStr.contains("<b") || 
            inStr.contains("<u") || 
            inStr.contains("<i") || 
            inStr.contains("<span") || 
            inStr.contains("<div") || 
            inStr.contains("<p") || 
            inStr.contains("<table") || 
            inStr.contains("<td") || 
            inStr.contains("<tr") || 
            inStr.contains("<ol") || 
            inStr.contains("<img") || 
            inStr.contains("<a") )
            return true;
        
        return false;
    }

    
    
    public static String convertHtml2PlainText( String inStr , boolean eliminateBlankLines )
    {
        String outStr = inStr;
        try
        {
            if( inStr == null || inStr.length() == 0 )
                return "";

            // first, convert all br tags to hard returns
            outStr = replaceStr( outStr , "<br>", "\n" );
            outStr = replaceStr( outStr , "<br/>", "\n" );
            outStr = replaceStr( outStr , "<br />", "\n" );
            outStr = replaceStr( outStr , "<p>", "\n" );
            outStr = replaceStr( outStr , "</li>", "\n" );
            outStr = replaceStr( outStr , "</ul>", "\n" );
            outStr = replaceStr( outStr , "</ol>", "\n" );
            outStr = replaceStr( outStr , "</tr>", "\n" );
            outStr = replaceStr( outStr , "</td>", "     " );

            outStr = replaceStr( outStr , "&nbsp;", " " );
            outStr = replaceStr( outStr , "&#160;", " " );
            outStr = replaceStr( outStr , "&quot;", "\"" );

            int first=0;

            int last=0;

            while( true )
            {
                first = outStr.indexOf( "<" );

                last = outStr.indexOf( ">" );

                if( first > -1 && last > -1 )
                {
                    outStr = outStr.substring( 0 , first ) + outStr.substring( last + 1 , outStr.length() );
                }

                else
                    break;
            }

            if( eliminateBlankLines )
            {
                String finalStr = "";

                String[] segments = outStr.split( "\n" );

                for( int i=0; i<segments.length ; i++ )
                {
                    if( segments[i] == null || segments[i].trim().length() == 0 )
                        continue;

                    if( finalStr.length() > 0 )
                        finalStr += "\n";

                    finalStr += segments[i];
                }

                outStr = finalStr;
            }

            return outStr;
        }

        catch( Exception e )
        {
            LogService.logIt( e , "StringUtils.convertHtml2PlainText() inStr=" + inStr + ", outStr=" + outStr );

            return outStr;
        }
    }
    

    public static String replaceStandardEntities( String inStr )
    {
        if( inStr == null )
            return "";

        // String s = inStr;

        inStr = inStr.replaceAll( "&" , "&amp;" );

        inStr = inStr.replaceAll( "  " , " &#160;" );

        inStr = inStr.replaceAll( "<" , "&lt;" );

        inStr = inStr.replaceAll( ">" , "&gt;" );

        inStr = inStr.replaceAll( "\"" , "&quot;" );

        inStr = inStr.replaceAll( "`" , "'" );

        inStr = StringUtils.replaceStr(inStr, "\\\'", "'");
        // inStr = inStr.replaceAll( "\'" , "'" );


        // put at end!
        if( inStr.contains("\n") )
            inStr = replaceStr( inStr , "\n" , "<br />" );

        else
            inStr = replaceStr( inStr , "\r" , "<br />" );

        //if( inStr.indexOf("'") >=0 )
        //    LogService.logIt( "StringUtils.replaceStdEntities() " + s + " ->> " + inStr );

        return inStr;
    }



    public static String truncateStringWithTrailer( String inStr , int maxLength, boolean lastWhitespace )
    {
        if( inStr == null )
            return "";

        if( inStr.length() <= maxLength )
            return inStr;

        if( maxLength < 4 )
            return inStr.substring( 0 , maxLength -1 );

        if( lastWhitespace )
            return truncateString( inStr , maxLength ) + "...";

        else
            return inStr.substring( 0 , maxLength -1 ) + "...";
    }



    public static String truncateStringFromFront( String inStr , int index )
    {
    	if( inStr == null || inStr.length() < index )
    		return inStr;


        // hard truncate
        return inStr.substring( inStr.length()-index , inStr.length() );
    }
    
    
    /**
     * Returns a truncated String that is truncated at the latest whitespace prior to index.
     */
    public static String truncateString( String inStr , int index )
    {
    	if( inStr == null || inStr.length() < index )
    		return inStr;

        // get most previous whiteSpace index
        int pwi = getPreviousWhitespaceIndex( inStr , index );

        if( pwi > inStr.length() - 1 )
        	pwi = inStr.length() - 1;

        // if found a whitespace character
        if( pwi > 0 )
            return inStr.substring( 0 , pwi );

        // hard truncate
        return inStr.substring( 0 , index );
    }



    /**
     * Returns the index of the next occurance of a whitespace character within the provided string, of
     * of the String length if there is none.
     */
    public static int getNextWhitespaceIndex( String inStr , int index )
    {
        // if no length to string, return 0
        if( inStr.length() == 0 )
            return 0;

        // if already at length, return length
        if( index >= inStr.length() )
            return inStr.length();


        // get first char
        char ch = inStr.charAt( index );

        while( !Character.isWhitespace( ch )  )
        {
            index++;

            if( index == inStr.length() )
                return index;

            // get next ch
            ch = inStr.charAt( index );

        }  // while

        return index;
    }



    /**
     * Returns the index of the most recent previous occurance of a whitespace character within the provided string, or 0
     * if there is none.
     */
    public static int getPreviousWhitespaceIndex( String inStr , int index )
    {

        // if no length to string, return 0
        if( inStr == null || inStr.length() == 0 )
            return 0;

        // if already at length, return length
        if( index >= inStr.length() )
            return inStr.length() - 1;

        // get first char
        char ch = inStr.charAt( index );

        while( !Character.isWhitespace( ch ) && index > 0 )
        {
            index--;

            if( index == 0 )
                return 0;

            // get next ch
            ch = inStr.charAt( index );

        }  // while

        return index;
    }



    /**
     * Removes a tag from a string.  Intended to completely remove a tag from HTML.
     *
     * For example, to remove all image tags, tagOut should be "img"
     *
     * @param inStr
     * @param tagOut
     * @return
     *
    public static String removeTag( String inStr , String tagOut )
    {
        if( inStr == null || inStr.length() == 0 )
            return "";

        int index = inStr.indexOf( "<" + tagOut , 0 );

        if( index < 0 )
            return inStr;

        int endIndex;

        int nextStartIndex = 0;

        StringBuilder sb = new StringBuilder();

        if( index > 0 )
            sb.append( inStr.substring( 0 , index ) );

        while( index >= 0 )
        {
            endIndex = inStr.indexOf( ">" , index );

            if( endIndex < 0 )
                break;

            if( inStr.charAt( endIndex - 1 ) == '/' )
            {
                nextStartIndex = endIndex + 1;
            }

            else
            {
                endIndex = inStr.indexOf( "</" + tagOut + ">" , index );

                if( endIndex < index )
                    break;

                else
                    nextStartIndex = endIndex + ("</" + tagOut + ">").length();
            }

            index = inStr.indexOf( "<" + tagOut , nextStartIndex );

            if( index < 0 )
                sb.append( inStr.substring( nextStartIndex , inStr.length() ) );

            else if( index > nextStartIndex )
                sb.append( inStr.substring( nextStartIndex , index ) );

        }

        return sb.toString();
    }
    */
    
    

    public static String sanitizeStringForCSSOnly( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        // HTML tag, including Script tag - Cross-site scripting
        // matches < and </ plus any letter, any number,
        inStr = inStr.replaceAll( "((%3C)|<)((%2F)|\\/)*[ac-hj-tv-zAC-HJ-TV-Z0-9%]+((%3E)|>)" , "" );

        // IMG src= tag.  - Cross-site scripting
        inStr = inStr.replaceAll( "((%3C)|<)((%69)|[iI]|(%49))((%6D)|[mM]|(%4D))((%67)|[gG]|(%47))[^\n]+((%3E)|>)" , "" );

        return inStr;
    }

    public static String sanitizeAllHtml( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        // HTML tag, including Script tag - Cross-site scripting
        // matches < and </ plus any letter, any number,
        inStr = inStr.replaceAll( "((%3C)|<)((%2F)|\\/)*[a-zA-Z0-9%]+((%3E)|>)" , "" );

        // IMG src= tag.  - Cross-site scripting
        inStr = inStr.replaceAll( "((%3C)|<)((%69)|[iI]|(%49))((%6D)|[mM]|(%4D))((%67)|[gG]|(%47))[^\n]+((%3E)|>)" , "" );

        return inStr;
    }


    public static String sanitizeStringFull( String in )
    {
        if( in == null || in.length() == 0 )
            return in;

        // inStr = escapeChar( inStr, '\'' , '\\' ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );

        // SQL Injection - screen metacharacters
        String inStr = in.replaceAll( "(\\%27)|(\\')|(\\-\\-)|(\\%23)|(#)" , "" );

        // Single quote and or combination
        inStr = inStr.replaceAll( "\\w*((%27)|(\\'))((%6F)|[oO]|(%4F))((%72)|[rR]|(%52))" , "" );

        // Single quote and union combination
        inStr = inStr.replaceAll( "\\w*((%27)|(\\'))[uU][nN][iI][oO][nN]" , "" );

        inStr = sanitizeStringForCSSOnly( inStr );

        // inStr = inStr.replaceAll( "<" , "%3C" );
        // LogService.logIt( "Sanitizing: " + in + ", returning " + inStr );

        return inStr; // inStr.replaceAll( ">" , "%3E" );
    }

    public static String sanitizeForSqlQuery( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        inStr = replaceStr( inStr, "\\" , "\\\\" );
        
        // return escapeChar( inStr, '\'' , '\\' ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );
        return replaceStr( inStr, "\'" , "\'\'" ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );
        
        // return escapeChar( inStr, '\'' , '\\' ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );
    }



    public static String replaceChar( String inStr, char out, String in )
    {
      if ( ( inStr == null ) || ( inStr.length() == 0 ) )
        return ( "" );

      StringBuffer outStr = new StringBuffer( "" );

      for ( int i = 0; i < inStr.length(); i++ )
      {
        if ( inStr.charAt( i ) == out )
          outStr.append( in );
        else
          outStr.append( inStr.charAt( i ) );
      }

      return ( outStr.toString() );
    }

    
    
    public static String escapeChar( String inStr, char charToEscape , char escapeChar )
    {
      if ( ( inStr == null ) || ( inStr.length() == 0 ) )
        return ( "" );

      StringBuffer outStr = new StringBuffer( "" );

      for ( int i = 0; i < inStr.length(); i++ )
      {
        if ( inStr.charAt( i ) == charToEscape )
        {
            if( i == 0 || inStr.charAt( i-1 ) != escapeChar )
                outStr.append( escapeChar );

            outStr.append( charToEscape );
        }
        else
          outStr.append( inStr.charAt( i ) );
      }

      return ( outStr.toString() );
    }



    public static String removeCurrencyPercentSymbols( String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
            return inStr;

        inStr = removeChar( inStr, '$' );
        inStr = removeChar( inStr, '%' );
        inStr = removeChar( inStr, '\u00A3' );
        inStr = removeChar( inStr, '\u20A0' );

        return inStr;

    }


    public static boolean isCurlyBracketed( String inStr )
    {
        if( inStr == null || inStr.trim().isEmpty() )
            return false;

        inStr = inStr.trim();

        return inStr.indexOf('{')==0 && inStr.lastIndexOf('}')==inStr.length()-1;

    }


    public static String removeChar( String inStr, char out )
    {
      StringBuffer outStr = new StringBuffer();

      for ( int i = 0;i < inStr.length();i++ )
      {
        if ( inStr.charAt( i ) != out )
          outStr.append( inStr.charAt( i ) );
      }

      return outStr.toString();
    }



    public static String replaceStr( String inStr , String oldPiece , String newPiece  )
    {

        if( inStr == null || inStr.length() == 0 )
            return "";

        if( oldPiece == null || oldPiece.length() == 0 )
            return inStr;

        if( newPiece == null )
            newPiece = "";

        StringBuilder outStr = new StringBuilder();

        int index = inStr.indexOf( oldPiece , 0 );

        if( index < 0 )
            return inStr;

        int lastIndex = 0;

        while( index >= 0 )
        {
            if( index > 0 )
                outStr.append( inStr.substring( lastIndex , index ) );

            outStr.append( newPiece );

            lastIndex =  index + oldPiece.length();

            if( lastIndex >= inStr.length() )
                break;

            index = inStr.indexOf( oldPiece , lastIndex );
        }

        // attach tail
        if( lastIndex < inStr.length() )
            outStr.append( inStr.substring( lastIndex , inStr.length() ) );

        return outStr.toString();
    }


    public static  String replaceStr(   String inStr ,
                                        String findStr ,
                                        String replaceStr ,
                                        boolean ignoreCase) throws Exception
    {
        try
        {
            if( inStr == null || inStr.length() == 0 )
                return inStr;

            if( findStr == null || findStr.length() == 0 )
                return inStr;

            if( replaceStr == null )
                replaceStr = "";

            if( !ignoreCase )
                return inStr.replaceAll( findStr , replaceStr );

            // work on upper case
            findStr = findStr.toUpperCase();

            String tempInStr = inStr.toUpperCase();

            int index = tempInStr.indexOf( findStr );

            int startIndex = 0;

            String outStr = "";

            while( index >= 0 && index < tempInStr.length() )
            {
                outStr += inStr.substring( startIndex , index );

                outStr += replaceStr;

                index += findStr.length();

                startIndex = index;

                if( index < inStr.length() )
                    index = tempInStr.indexOf( findStr , index );
            }

            if( startIndex < inStr.length() )
                outStr += inStr.substring( startIndex , inStr.length() );

            return outStr;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "replaceStr( inStr=" + inStr + ", findStr=" + findStr + ", replaceStr=" + replaceStr + ", replaceStr=" + replaceStr + " ) " + e.toString() );

            throw new STException( e );
        }
    }


}