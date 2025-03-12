/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr;

import com.tm2score.global.Constants;
import com.tm2score.service.LogService;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class IvrStringUtils {

    // public static final String AUDIOTAG = "[AUDIO]";
    // public static final String POINTSTAG = "[POINTS]";
    // public static final String TEXTINPUTTYPETAG = "[TEXTINPUTTYPE]";
    // public static final String ESSAYPROMPTTAG = "[ESSAYPROMPT]";

    
    /*
    public static String decodeStr( String inStr )
    {
        if( inStr==null || inStr.isEmpty() )
            return inStr;
        
        try
        {
            return URLDecoder.decode(inStr, "UTF8" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "IvrStringUtils.decodeString() " + inStr );
            return inStr;
        }
    }
    */
    
    
    /**
     * Returns data[0]=Essay promptId
     *         data[1]=Min words
     *         data[2]=Max Words
     * @return 
     */
    public static int[] getEssayScoreInfo( String txt )
    {
        int[] out = new int[3];
        
        if( txt == null || txt.isEmpty() )
            return out;
        
        txt = UrlEncodingUtils.decodeKeepPlus( txt );
        
        if( !IvrStringUtils.containsKey( "[" + Constants.ESSAYPROMPT_KEY + "]", txt, true) )
            return out;
        
        String tagVal = getTagValue(txt, Constants.ESSAYPROMPT_KEY );
        
        if( tagVal==null || tagVal.trim().isEmpty() )
            return out;
        
        tagVal = tagVal.trim();
        
        String[] vals = tagVal.split( "," );
        
        if( vals.length>0 )
        {
            try
            {
                out[0]=Integer.parseInt( vals[0].trim() );
            }
            catch( NumberFormatException e )            
            {
                LogService.logIt( e, "IvrStringUtils.getEssayScoreInfo() getting promptId from txt ");
            }
        }

        if( vals.length>1 )
        {
            try
            {
                out[1]=Integer.parseInt( vals[1].trim() );
            }
            catch( NumberFormatException e )            
            {
                LogService.logIt( e, "IvrStringUtils.getEssayScoreInfo() getting minWords from txt ");
            }
        }

        if( vals.length>2 )
        {
            try
            {
                out[2]=Integer.parseInt( vals[2].trim() );
            }
            catch( NumberFormatException e )            
            {
                LogService.logIt( e, "IvrStringUtils.getEssayScoreInfo() getting maxWords from txt ");
            }
        }
        
        return out;
    }
    
    public static String getPointsValueFmTextScoreParam(String txt) {
        return getTagValue(txt, Constants.POINTS_KEY );
    }
    
    public static int getTextInputTypeIdFrmTextScrParam( String txt )
    {
        String tv = getTagValue(txt,  Constants.TEXTINPUTTYPE_KEY );
        
        if( tv==null || tv.trim().isEmpty() )
            return 0;
        
        try
        {
            return Integer.parseInt(tv);
        }
        catch( NumberFormatException e)
        {
            LogService.logIt( e, "IvrStringUtils.getTextInputTypeFrmTextScrParam() txt=" + txt );
            return 0;
        }
    }
    
    
    public static String getTagValueWithDecode( String txt, String tagName)
    {
        if (txt == null || tagName==null || tagName.trim().isEmpty() ) {
            return null;
        }
        
        txt = UrlEncodingUtils.decodeKeepPlus(txt);
        
        return getTagValue( txt,  tagName );
    }
    
        
    // the only place a non-tagged value can live is before any tags. 
    public static String getNonTaggedValue( String txt )
    {
        if (txt == null )
            return null;
        
        txt = txt.trim();
        
        if( txt.isEmpty() )
            return "";
        
        if( txt.indexOf( "[" ) < 0 )
            return txt;
        
        if( txt.indexOf( "[" )==0 )
            return "";
        
        return txt.substring(0, txt.indexOf( "[" ) );
    }
    
    
    public static String getTagValue(String txt, String tagName ) {
        
        if (txt == null || tagName==null || tagName.trim().isEmpty() ) {
            return null;
        }
        
        tagName = tagName.trim();
        
        if( !tagName.startsWith("[") )
            tagName = "[" + tagName;
        
        if( !tagName.endsWith("]") )
            tagName += "]";
        
        
        int idx = txt.indexOf(tagName);
        
        if (idx < 0 || idx + tagName.length() == txt.length()) {
            return null;
        }
        
        int idx2 = txt.indexOf("[", idx + tagName.length() );
        
        return txt.substring(idx + tagName.length(), idx2>0 ? idx2 : txt.length());
    }
    
    
    public static String removeTextInputTypeBracket( String inStr )
    {
        return removeTailBracket( inStr, Constants.TEXTINPUTTYPE_KEY );
    }
    
    
    public static String removeAudioBracket( String inStr )
    {
        return removeTailBracket( inStr, Constants.AUDIO_KEY );
    }

    public static String removePointsBracket( String inStr )
    {
        return removeTailBracket( inStr, Constants.POINTS_KEY );
    }

    
    public static String removeTailBracket( String inStr, String name )
    {
        if(inStr == null || inStr.trim().isEmpty() ) 
            return "";

        if( !name.startsWith("["))
            name = "[" + name;
        
        if(!inStr.contains( name ) ) 
            return inStr;

        return inStr.substring(0, inStr.indexOf( name ) );
    }


    
    public static boolean containsKey( String key, String text, boolean decode)
    {
        if( text==null || text.isEmpty() || key==null || key.isEmpty() )
            return false;
        
        if( decode )
            text = UrlEncodingUtils.decodeKeepPlus( text );
        
        return text.contains(key);
    }
          
    
    
    public static List<String> getTextScoreParam1MatchVals( String textScoreParam1 )
    {
        List<String> out = new ArrayList<>();
        
        if( textScoreParam1==null || textScoreParam1.isEmpty() )
            return out;
        
        String[] vals = textScoreParam1.split(";");

        // LogService.logIt( "IvrStringUtils.getTextScoreParam1MatchVals() parsing TextScorePAram1: " + textScoreParam1 + ", tokens=" + vals.length );

        for( String v : vals )
        {
            if( v==null || v.trim().isEmpty() )
                continue;

            v = v.trim();

            // LogService.logIt( "IvrStringUtils.getTextScoreParam1MatchVals() AAA token: " + v );
            if( v.indexOf("|")>0 )
            {   
                v = v.substring(0, v.indexOf("|") );
                v = v.trim();
            }

            // LogService.logIt( "IvrStringUtils.getTextScoreParam1MatchVals() BBB token: " + v );
            if( v.isEmpty() )
                continue;

            if( v.equals("defaultnextintn"))
                continue;

            out.add(v);                
        }
                
        return out;
    }

    public static int getSingleDtmf( String dtmf )
    {
        return getDtmfInt(  dtmf, true );
    }
    
    public static int getMultiDtmf( String dtmf )
    {
        return getDtmfInt(  dtmf, false );
    }

    public static int getDtmfInt( String dtmf, boolean single )
    {    
        if( dtmf==null || dtmf.trim().isEmpty() )
            return -1;
        
        dtmf = dtmf.trim();
        
        String d = dtmf;
        
        if( d.endsWith("#"))
            d = d.substring(0,d.length()-1);
        
        if( d.isEmpty() )
            return -1;
        
        
        if( single && d.length()>1 )
            d = d.substring(d.length()-1, d.length() );
        
        try
        {
            return Integer.parseInt(d);
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( e, "IvrStringUtils.getDtmfInt() Unable to parse: " + dtmf + ", conditioned=" + d + " single=" + single );
            return -1;
        }
    }

    
    public static String getDtmfString( String dtmf )
    {    
        if( dtmf==null || dtmf.trim().isEmpty() )
            return "";
        
        dtmf = dtmf.trim();
        
        if( dtmf.endsWith("#"))
            dtmf = dtmf.substring(0,dtmf.length()-1);
        
        return dtmf;
    }

       
    
    public static String removeAllTailBrackets( String inStr )
    {
        inStr = removeAudioBracket( inStr );
        
        inStr = removeTextInputTypeBracket( inStr );
        
        return removePointsBracket( inStr );
    }
    
           
    public static String removeAllBracketsForVoice(String inStr) {
        
        if (inStr == null || inStr.trim().isEmpty()) {
            return "";
        }
        
        // remove the last one.
        inStr =  removeAllTailBrackets( inStr );
    
        int idx = inStr.indexOf("[");
        
        int idx2;
        
        while( idx>=0 )
        {
            idx2 = inStr.indexOf("]",idx );
            
            if( idx2 <0 || idx2<idx )
                return inStr;
            
            // remove the leading bracket.
            if( idx==0 )
                inStr = inStr.substring(idx2+1, inStr.length() );
            
            // cut out the bracket
            else
                inStr = inStr.substring(0, idx) + inStr.substring(idx2+1, inStr.length() );

            // look for next bracket
            idx = inStr.indexOf("[");
        }
        
        return inStr;               
    }


    
    public static String removeLeadingBrackets(String inStr) {
        if (inStr == null || inStr.trim().isEmpty()) {
            return "";
        }
        
        inStr = inStr.trim();
        
        while( inStr.startsWith("[") && inStr.indexOf("]")>0 )
        {
            inStr = inStr.substring(inStr.indexOf("]")+1, inStr.length());
        }
        
        return inStr;
    }

    public static String removeLeadingBracket(String inStr) {
        if (inStr == null || inStr.trim().isEmpty()) {
            return "";
        }
        
        inStr = inStr.trim();
        
        if (!inStr.startsWith("[")) {
            return inStr;
        }
        
        int idx = inStr.indexOf("]");
        if (idx < 0) {
            return inStr;
        }
        return inStr.substring(idx + 1, inStr.length());
    }
    
       
    
    
    
}
