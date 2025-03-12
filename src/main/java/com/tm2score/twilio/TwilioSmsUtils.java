/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.twilio;

import com.tm2score.email.SmsBlockFacade;
import com.tm2score.entity.email.SmsBlock;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.util.Locale;
//import com.twilio.http.TwilioRestClient;
// import com.twilio.rest.api.v2010.Account;
//import com.twilio.sdk.TwilioRestClient;

//import com.twilio.sdk.resource.factory.SmsFactory;
//import com.twilio.sdk.resource.instance.Account;

/**
 *
 * @author Mike
 */
public class TwilioSmsUtils
{
    
    // returns -3 if blocked full
    //         -2 if phone blocked temp
    //         -1 if phone invalid
    //         0 if not sent
    //         1 if sent
    public static int sendTextMessageViaThread( String to, String countryCode, Locale locale, String from, String msg) throws Exception
    {
        // LogService.logIt("TwilioSmsUtils.sendTextMessageViaThread() START to=" + to + ", from=" + from + ", " + msg );
        
        try
        {
            if( msg == null || msg.isEmpty() )
                throw new Exception( "No message: " + msg );

            if( locale==null )
                locale = Locale.US;

            SmsBlockFacade smsBlockFacade = SmsBlockFacade.getInstance();
            SmsBlock smsBlock = smsBlockFacade.getActiveSmsBlock(to);
            
            if( smsBlock!=null && smsBlock.getIsActiveBlock() )
                return smsBlock.getSmsBlockReasonId()==1 ? -3 : -2;
                        
            msg += " " + MessageFactory.getStringMessage(locale, "g.SMSSenderId", new String[]{RuntimeConstants.getStringValue("default-site-name")} );

            TwilioThread tt = new TwilioThread( to, countryCode,  from,  msg );
        
            Thread t = new Thread( tt );            
            t.start();
            return 1;
        
        }
        // Twilio ApiException
        //catch( ApiException e )
        //{
        //    LogService.logIt("TwilioSmsUtils.sendTextMessage() Twilio " + e.toString() + ", to=" + to + ", from=" + from + ", " + msg );
        //    return 0;
        //}
        catch( Exception e )
        {
            LogService.logIt(  e, "TwilioSmsUtils.sendTextMessageViaThread() " );
            return 0;
        }
    }
    
}
