/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.amazoncloud;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import java.util.Locale;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

/**
 *
 * @author miker_000
 */
public class AmazonTranslateUtils 
{
    private static String[] VALID_LANGS = new String[] { "af","sq","am","ar","az","bn","bs","bg","zh","zh-TW","hr","cs","da","fa-AF","nl","en","et","fi","fr","fr-CA","ka","de","el","ha","he","hi","hu","id","it","he","ja","ko","lv","ms","no","fa","ps","pl","pt","ro","ru","sr","sk","sl","so","es","sw","sv","tl","ta","th","tr","uk","ur","vi"};
    
    TranslateClient amazonTranslate;
    
    private synchronized void initClient() throws Exception
    {
        if( amazonTranslate!=null )
            return;
        
        try
        {
            AwsBasicCredentials creds = AwsBasicCredentials.builder().accessKeyId(RuntimeConstants.getStringValue( "awsAccessKeyTranslate" )).secretAccessKey(RuntimeConstants.getStringValue( "awsSecretKeyTranslate" )).build();            
            StaticCredentialsProvider bac = StaticCredentialsProvider.create(creds );
            amazonTranslate = TranslateClient.builder().region(getClientRegion()).credentialsProvider(bac).build();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranslateUtils.initClient() " );
            throw e;
        }
    }
    
    public Region getClientRegion()
    {
        int rid = RuntimeConstants.getIntValue("awsRekognitionRegionId");
        
        if( rid==1 )
            return Region.US_EAST_1;
        if( rid==12 )
            return Region.US_WEST_2;
        return Region.US_EAST_1;
    }
    
    
    public String translateText( Locale sourceLocale, Locale targetLocale, String sourceText ) throws Exception
    {
        String[] out = new String[]{"",""};
        try
        {
            initClient();
            
            if( targetLocale==null )
                targetLocale = Locale.US;
            
            if( sourceText==null || sourceText.isBlank() )
            {
                LogService.logIt( "TranslateClientUtils.translateText() sourceText is empty. Returning empty String." );
                return "";
            }
            
            if( sourceText.length()>5000 )
            {
                LogService.logIt( "TranslateClientUtils.translateText() Truncating text to 5000 chars.  Amazon limitation." );
                sourceText = sourceText.substring(0, 4999 );
            }
                        
            String srcLang = sourceLocale==null ? "auto" : sourceLocale.getLanguage();            
            
            String tgtLang = targetLocale.getLanguage();            
            
            if( !srcLang.equals("auto") && !isValidLang( srcLang ) )
                throw new Exception( "Source Language is not valid. " + srcLang );
            
            if( !isValidLang( tgtLang ) )
                throw new Exception( "Target Language is not valid. " + tgtLang );
            
            if( !isValidLang( tgtLang ) )
                throw new Exception( "Target Language is not valid. " + tgtLang );
            
            initClient();
            
            TranslateTextRequest  ttr = TranslateTextRequest.builder().sourceLanguageCode(srcLang).targetLanguageCode(tgtLang).text(sourceText).build();
            
            TranslateTextResponse  result = amazonTranslate.translateText(ttr);
            
            String textOut = result.translatedText();
            
            LogService.logIt( "TranslateClientUtils.translateText() COMPLETE. SourceText size=" + sourceText.length() + ", Translated text size=" + textOut.length() );
            
            return textOut;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AmazonTranscribeUtils.translateText() sourceLocale=" + sourceLocale.toString() + ", targetLocale=" + (targetLocale==null ? "null" : targetLocale.toString() ) + ", sourceText=" + sourceText );
            throw e;
        }
    }
    
    
    public static boolean isValidLang( String ln )
    {
        for( String tl : VALID_LANGS )
        {
            if( tl.equalsIgnoreCase(ln) )
                return true;
        }
        return false;
    }
    
    
}
