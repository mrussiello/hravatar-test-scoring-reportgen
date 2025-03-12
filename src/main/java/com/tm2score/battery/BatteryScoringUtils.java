/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.battery;

import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class BatteryScoringUtils {
    
    public static String getEarlyExitWarningMessage( Locale locale, String inStr )
    {
        if( inStr==null || inStr.isBlank() )
            return null;
        
        if( locale==null )
            locale = Locale.US;
        
        try
        {            
            int idx = inStr.indexOf(":");
            if( idx<0 )
                throw new Exception( "Cannot parse inStr, no : found." );
            int sourceCode = Integer.parseInt( inStr.substring(0,idx));
            int idx2 = inStr.indexOf(":",idx+1);
            if( idx2<=idx )
                throw new Exception( "Cannot parse inStr, no idx2 for : found." );
            float percentComplete = Float.parseFloat( inStr.substring(idx+1,idx2));
            return MessageFactory.getStringMessage(locale, "g.BatteryEarlyExit." + sourceCode, new String[]{I18nUtils.getFormattedNumber(locale, percentComplete, 1 ), RuntimeConstants.getStringValue("baseadmindomain")} );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BatteryScoringUtils.getEarlyExitWarningMessage() inStr=" + inStr );
            return null;
        }
    }
}
