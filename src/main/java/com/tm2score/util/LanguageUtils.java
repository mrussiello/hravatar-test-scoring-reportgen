/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

import com.tm2score.amazoncloud.AmazonTranslateUtils;
import com.tm2score.entity.googlecloud.GoogleTranslateCache;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.googlecloud.GoogleCloudFacade;
import com.tm2score.googlecloud.GoogleTranslateUtils;
import com.tm2score.service.LogService;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class LanguageUtils {
  
    static Boolean USE_GOOGLE_TRANSLATE = null;
    
    GoogleCloudFacade googleCloudFacade;
    
    
    private static synchronized void init()
    {
        if( USE_GOOGLE_TRANSLATE==null )
        {
            USE_GOOGLE_TRANSLATE = RuntimeConstants.getBooleanValue( "useGoogleTranslate" );
        }
    }

    
    public GoogleTranslateCache addCacheRecord( String srcText, String tgtText, Locale srcLocale, Locale tgtLocale ) throws Exception
    {
        try
        {
            if( srcText==null || srcText.isEmpty() )
                throw new Exception( "Source text is empty." );
                        
            if( tgtText==null || tgtText.isEmpty() )
                throw new Exception( "tgtText is empty." );

            if( googleCloudFacade==null )
                googleCloudFacade = GoogleCloudFacade.getInstance();
            
            GoogleTranslateCache gtc = googleCloudFacade.getCacheRecordForKey( srcLocale.getLanguage().toLowerCase(), tgtLocale.getLanguage().toLowerCase(), srcText );
            
            if( gtc!=null )
                gtc.setTgtText(tgtText);
            
            else
            {            
                gtc = new GoogleTranslateCache();                
                gtc.setSrcLang( srcLocale.getLanguage().toLowerCase() );
                gtc.setTgtLang( tgtLocale.getLanguage().toLowerCase() );
                gtc.setSrcCompress( GoogleTranslateUtils.compressString(srcText));
                gtc.setSrcText(srcText);
                gtc.setTgtText(tgtText);
            }
            
            googleCloudFacade.saveGoogleTranslateCache(gtc);
            return gtc;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleCloudFacade.addCacheRecord( srcText=" + srcText + ", tgtText=" + tgtText + ", srcLocale=" + ( srcLocale==null ? "null" : srcLocale.toString()) + ", tgtLocale=" + ( tgtLocale==null ? "null" : tgtLocale.toString() ) );

            throw new STException( e );
        }
    }
    
    
    
    public String getKeyValueStrict( Locale srcLocale, Locale tgtLocale, String key, String[] params)
    {
        init();
        
        return getKeyValueStrict(Constants.DEFAULT_RESOURCE_BUNDLE, srcLocale, tgtLocale, key, params, USE_GOOGLE_TRANSLATE );
    }
    

    public String getKeyValueStrict( String bundleNameNoLang, Locale srcLocale, Locale tgtLocale, String key, String[] params)
    {
        init();
        
        return getKeyValueStrict(bundleNameNoLang, srcLocale, tgtLocale, key, params, USE_GOOGLE_TRANSLATE );
    }
    

    /**
     * Returns the key value in locale language if present, or returns null if not present.
     * 
     * If not present, and useGoogleTranslate==true, will check GoogleTranslate Cache first, and then go to GoogleTranslate.
     * 
     * @param key
     * @param tgtLocale
     * @param useGoogleTranslate
     * @throws Exception 
     */
    public String getKeyValueStrict( String bundleNameNoLang, Locale srcLocale, Locale tgtLocale, String key, String[] params, boolean useGoogleTranslate)
    {
        try
        {
            if( bundleNameNoLang== null )
                bundleNameNoLang = Constants.DEFAULT_RESOURCE_BUNDLE;
            
            // get the resource bundle value. Could be in English at this point. English is the top of the tree.
            String base = MessageFactory.getStringMessage( bundleNameNoLang, tgtLocale, key, params);
            
            if( PropertyUtils.hasKeyForLocale( key, tgtLocale) )
                return base;
            
            // if base not found, it doesn't even exist in english.
            if( base==null || base.isEmpty() || !useGoogleTranslate )
                return null;
            
            // at this point, there is no entry for this key in the target language. So, we need to get the translation. 
            
            return performTextTranslation(base, srcLocale, tgtLocale, params!=null && params.length>0, true );
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "LanguageUtils.getKeyValueStrict() key=" + key + ", tgtLocale=" + (tgtLocale==null ? "null" : tgtLocale.toString()) + ", useGoogleTranslate=" + useGoogleTranslate );            
            return null;
        }
    }


    /**
     * Returns the key value in locale language if present, or returns null if not present.
     * 
     * If not present, and useGoogleTranslate==true, will check GoogleTranslate Cache first, and then go to GoogleTranslate.
     * 
     * @param key
     * @param tgtLocale
     * @param useGoogleTranslate
     * @throws Exception 
     */
    public String getTextTranslation( String srcText, Locale srcLocale, Locale tgtLocale, boolean isDynamic )
    {
        if( srcText == null )
            return "";
        
        if( srcText.trim().isEmpty() )
            return srcText;
        
        try
        {
            return performTextTranslation(srcText, srcLocale, tgtLocale, isDynamic, true );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "LanguageUtils.getTextTranslation() srcLocale=" + srcLocale.toString() + ", targetLocale=" + tgtLocale.toString() + ", srcText=" + srcText );
            return null;
        }
    }
    
    
        /**
     * Returns 
     * 
     *    data[0]=sourceText
     *    data[1]=translated text
     *    data[2]=source language
     *    data[3]=target language
     *    data[4]=Error code if any
     *    data[5]=Error message, if any
     *    data[6]=detected language (only if sourceLang is null)
     * 
     * @param text
     * @param sourceLocale
     * @param targetLocale
     * @return
     * @throws Exception 
     */
    public Object[] translateText( String text, Locale sourceLocale, Locale targetLocale, boolean unescapeHtmlEntities)
    {
        Object[] out = new Object[7];
        
        out[0]=text;
        
        String jsonResponse = null;
        
        try
        {
            init();

            boolean hasHtml = GoogleTranslateUtils.isHtml( text );
            
            // LogService.logIt( "GoogleTranslateUtils.translateText() hasHtml=" + hasHtml );
            
            // If it has html we don't need to worry about entities.
            if( hasHtml )
                unescapeHtmlEntities = false;
            
            String tt = performTextTranslation( text, sourceLocale, targetLocale, true, unescapeHtmlEntities );

            out[1]=tt;
            out[2] = sourceLocale.toString();
            out[3]= targetLocale.toString();
                        
            return out;
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "LanguageUtils.translateText() text=" + text + ", sourceLocale=" + (sourceLocale==null ? "null" : sourceLocale.toString()) + ", targetLocale=" + (targetLocale==null ? "null" : targetLocale.toString())  + ", jsonResponse=" + jsonResponse );            
            out[4]=(int)(999);
            out[5]=e.toString();            
            return out;            
        }
        
    }

    
    private String performTextTranslation( String srcText, Locale srcLocale, Locale tgtLocale, boolean isDynamic, boolean unescapeHtmlCharEntities) throws Exception
    {
        try
        {
            if( srcLocale == null )
                srcLocale = Locale.US;
            
            if( srcText==null || srcText.isBlank())
                return "";
            
            GoogleTranslateCache gtc = null;
    
            boolean useCache =  !isDynamic || isDynamic && srcText.length()<=80;

            // LogService.logIt( "LanguageUtils.performTextTranslation() AAA useCache=" + useCache + ", srcText=" + StringUtils.truncateStringWithTrailer(srcText, 100, false) + ", sourceLocale=" + (srcLocale==null ? "null" : srcLocale.toString()) + ", targetLocale=" + (tgtLocale==null ? "null" : tgtLocale.toString()) );
            
            if( useCache )
            {
                if( googleCloudFacade == null )
                    googleCloudFacade = GoogleCloudFacade.getInstance();
            
                gtc = googleCloudFacade.getCacheRecordForKey(srcLocale.getLanguage(), tgtLocale.getLanguage(), srcText );
            }
            
            if( gtc!=null )
                return gtc.getTgtText();
            
            boolean useAws = true;
            
            if( srcLocale!=null && !AmazonTranslateUtils.isValidLang( srcLocale.getLanguage() ) )
                useAws=false;
            
            if( tgtLocale!=null && !AmazonTranslateUtils.isValidLang( tgtLocale.getLanguage() ) )
                useAws=false;
            
            String tt = null;
            
            if( useAws )
            {
                AmazonTranslateUtils atu = new AmazonTranslateUtils();
                tt = atu.translateText(srcLocale, tgtLocale, srcText );
            }
                            
            else
            {
                LogService.logIt( "LanguageUtils.performTextTranslation() USING GOOGLE TRANSLATE " + ", sourceLocale=" + (srcLocale==null ? "null" : srcLocale.toString()) + ", targetLocale=" + (tgtLocale==null ? "null" : tgtLocale.toString()) );
                tt = GoogleTranslateUtils.translateTextNoErrors(srcText, srcLocale, tgtLocale, unescapeHtmlCharEntities );
            }
            
            if( tt!=null )
            {
                tt = tt.trim();

                if( tt.isEmpty() )
                    tt= null;
            }
                        
            if( useCache && tt!=null && !tt.isEmpty() ) // && !hasParams
            {                
                //if( googleCloudFacade == null )
                //    googleCloudFacade = GoogleCloudFacade.getInstance();
            
                gtc = addCacheRecord(srcText, tt, srcLocale, tgtLocale );
                
                LogService.logIt( "LanguageUtils.performTextTranslation() Saved new " + gtc.toString() );
            }
            
            return tt;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "LanguageUtils.performTextTranslation() srcText=" + srcText + ", sourceLocale=" + srcLocale.toString() );
            
            throw e;            
        }
    }
    
}
