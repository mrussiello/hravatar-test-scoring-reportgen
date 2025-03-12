/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.twilio;

import com.tm2score.email.SmsBlockFacade;
import com.tm2score.entity.email.SmsBlock;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.util.GooglePhoneUtils;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.net.URI;

/**
 *
 * @author miker_000
 */
public class TwilioThread implements Runnable
{
    private static Boolean TWILIO_ON = null;
    private static String TWILIO_SID = null;
    private static String TWILIO_AUTH = null;
    private static String MSG_STATUS_CALLBACK = null;

    
    
    String to;
    String from;
    String msg;
    String countryCode;
    
    
    public TwilioThread( String to, String countryCode, String from, String msg)
    {
        this.to=to;
        this.from=from;
        this.msg=msg;
        this.countryCode=countryCode;
    }
    
    public void run() 
    {
        // LogService.logIt( "TwilioThread.run() START to=" + to + ", from=" + from + ", " + msg );
        
        if( TWILIO_SID == null )
        {
            TWILIO_ON = RuntimeConstants.getBooleanValue( "twilio.textingon" );
            TWILIO_SID = RuntimeConstants.getStringValue( "twilio.sid" );
            TWILIO_AUTH = RuntimeConstants.getStringValue( "twilio.auhtoken" );
            MSG_STATUS_CALLBACK = RuntimeConstants.getStringValue( "twilio.msgstatuscallbackurl" );
        }

        if( from == null || from.isEmpty() )
        {
            if( RuntimeConstants.getBooleanValue( "twilio.useSandbox" ) )
                from = RuntimeConstants.getStringValue( "twilio.sandboxphonenumber" );

            else
                from = RuntimeConstants.getStringValue( "twilio.fromnumber" );
        }

        to = GooglePhoneUtils.getFormattedPhoneNumberE164(to, countryCode );

        if( to==null )
        {
            LogService.logIt( "TwilioThread.run() AAA.1 TO phone number is null after E164 conversion. countryCode=" + countryCode + ", from=" + from + ", " + msg );
            return;            
        }
        
        try
        {
            if( !GooglePhoneUtils.isNumberValid(to, countryCode ) )
            {
                LogService.logIt( "TwilioThread.run() BBB.1 To number is not valid for country code using E164. Trying Intl. to=" + to + ", countryCode=" + countryCode + ", " + msg );
                to = GooglePhoneUtils.getFormattedPhoneNumberIntl(to, countryCode);
                LogService.logIt( "TwilioThread.run() BBB.2 to number changed to to=" + to + " using Intl version, countryCode=" + countryCode + ", valid=" + GooglePhoneUtils.isNumberValid(to, countryCode ) );

                if( to==null )
                {
                    LogService.logIt( "TwilioThread.run() BBB.3 TO phone number is null after conversion to Intl format. countryCode=" + countryCode + ", from=" + from + ", " + msg );
                    return;            
                }        
            }
            
            // Strip leading 0 off phone if needed.
            if( !GooglePhoneUtils.isNumberValid(to, countryCode ) && countryCode!=null && countryCode.equalsIgnoreCase("US") && to.indexOf("0")>=0 )
            {
                if( to.startsWith("+0"))
                    to = to.substring(2, to.length() );
                else if( to.startsWith("0"))
                    to = to.substring(1, to.length() );                
                to = GooglePhoneUtils.getFormattedPhoneNumberE164(to, countryCode );
                LogService.logIt( "TwilioThread.run() to number changed to to=" + to + " uafter stripping leading numbers, countryCode=" + countryCode + ", valid=" + GooglePhoneUtils.isNumberValid(to, countryCode ) );                
            }
            
            if( !GooglePhoneUtils.isNumberValid(to, countryCode ) )
            {
                LogService.logIt( "To phone number is invalid " + to + " for country code: " + countryCode );
                return;
                // throw new Exception( "To phone number is invalid " + to + " for country code: " + countryCode );
            }

            if( msg == null || msg.isEmpty() )
                throw new Exception( "No message: " + msg );

            if( TWILIO_ON )
            {
                Twilio.init( TWILIO_SID, TWILIO_AUTH);
                
                Message message = null;
                                
                String callbackUrl = MSG_STATUS_CALLBACK + GooglePhoneUtils.cleanPhoneNumberForBlock(to);
                
                try
                {
                    message = Message.creator( new PhoneNumber(to), new PhoneNumber(from), msg ).setStatusCallback(URI.create(callbackUrl)).create();
                    
                    if( message.getStatus()!=null && (message.getStatus().equals(Message.Status.FAILED) || message.getStatus().equals(Message.Status.UNDELIVERED)) )
                        SmsBlockFacade.getInstance().createSmsBlock(to, false);
                    
                }
                catch( com.twilio.exception.ApiException e )
                {
                    LogService.logIt("TwilioThread.run() Sending API Text.  Twilio " + e.toString() + ", to=" + to + ", from=" + from + ", " + msg + ", Twilio Error Code: " + e.getCode() );
                    if( e.getCode()==20021 || e.getCode()==20023 || e.getCode()==21202 || e.getCode()==21610 || e.getCode()==21211 || e.getCode()==21216 || e.getCode()==21217 || e.getCode()==21613 || e.getCode()==21614 || e.getCode()==21615 || e.getMessage().contains("Attempt to send to unsubscribed recipient") )
                    {
                            createSmsBlock(to, true);
                            return;                
                    }

                    else if( e.getCode()==21203 || e.getCode()==21214 || e.getCode()==21215 || e.getCode()==21612 )
                    {
                        createSmsBlock(to, false);
                        return;                                
                    }
                    Tracker.addDistributionError();            
                }
                /*
                catch( com.twilio.exception.ApiException ee )
                {
                    Tracker.addDistributionError();   
                    
                    int code = ee.getCode();
                    if( code==21610 || 
                            code==21612 || 
                            (ee.getMessage()!=null && ee.getMessage().equalsIgnoreCase("Attempt to send to unsubscribed recipient")) ||
                            (ee.getMessage()!=null && ee.getMessage().contains("phone number is not currently reachable")))
                    {
                        LogService.logIt( "TwilioThread.run() " + ee.toString() + ", Sending Text. to=" + to );
                        return;
                    }                            
                        
                    
                    String tx = to;

                    if( !to.startsWith("0") )
                    {
                        LogService.logIt( ee, "TwilioThread.run() Sending Text. to=" + to );
                    }
                        
                    if( to.startsWith( "00" ) )
                    {
                        tx = to.substring( 2, to.length() );
                    }
                    else if( to.startsWith("0") )
                    {
                        tx = to.substring(1,to.length() );
                    }
                    
                    if( !tx.equals( to ))
                    {
                        to=tx;
                        // LogService.logIt( "TwilioThread.run() Sending Text. trying again with to=" + tx );
                        try
                        {
                            message = Message.creator( new PhoneNumber(tx), new PhoneNumber(from), msg ).create();                    
                        }
                        catch( Exception eex )
                        {
                            LogService.logIt( eex, "TwilioThread.run() Error resending text. " + eex.toString() + ", to=" + to );                            
                        }
                    } 
                    return;
                }
                */
                /**
                TwilioRestClient client = new TwilioRestClient(TWILIO_SID, TWILIO_AUTH);

                    // Get the main account (The one we used to authenticate the client
                Account mainAccount = client.getAccount();

                SmsFactory smsFactory = mainAccount.getSmsFactory();
                Map<String, String> smsParams = new HashMap<>();
                smsParams.put("To", to ); // Replace with a valid phone number
                smsParams.put("From", from ); // Replace with a valid phone number in your account.

                smsParams.put("Body", msg );
                smsFactory.create(smsParams);
                */

               // LogService.logIt( "TwilioThread.run() SENT to=" + to + ", from=" + from + ", " + msg );

            }

            else
                LogService.logIt( "TwilioThread.run() to=" + to + ", from=" + from + ", " + msg + " TEXT NOT SENT, TEXTING TURNED OFF." );

        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "TwilioThread.run() ERROR to=" + to + ", from=" + from + ", " + msg );
            
            Tracker.addDistributionError();

            // throw new STException(e);
        }
        
    }
    
    private SmsBlock createSmsBlock( String phoneNumber, boolean fullBlock )
    {
        try
        {
            SmsBlockFacade smsBlockFacade = SmsBlockFacade.getInstance();
            return smsBlockFacade.createSmsBlock(phoneNumber, fullBlock);
        }
        catch( Exception e )
        {
            LogService.logIt(e, "PhoneUtils.createSmsBlock() phoneNumber=" + phoneNumber + ", fullBlock=" + fullBlock );
            return null;
        }
    }
    
    
}
