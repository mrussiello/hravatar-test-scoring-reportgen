package com.tm2score.service;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import jakarta.ejb.Stateless;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.util.Locale;




/**
 * This is a REQUEST-LEVEL Backing Bean for JSF Actions.
 *
 * This bean contains injected resources, which means that it cannot (and should not) be serialized and restored easily.
 * Do not move to session.
 *
 * @author Mike
 *
 */

import javax.naming.InitialContext;
import org.apache.commons.validator.routines.EmailValidator;

@Stateless
public class EmailUtils
{
    public static final String HTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                                             "<html><head><meta content=\"text/html;charset=UTF-8\" http-equiv=\"Content-Type\"><title></title></head><body bgcolor=\"#ffffff\" text=\"#000000\">\n";

    public static final String HTML_FOOTER = "</body></html>";

    public static String OVERRIDE_BLOCK = "overrideblock";

    public static String OVERRIDE_FULLBLOCK = "overrideblockFULL";
    
    public static String CONTENT = "content";

    public static String TO = "to";

    public static String FROM = "from";

    public static String CC = "cc";

    public static String BCC = "bcc";

    public static String SUBJECT = "subject";

    public static String MIME_TYPE = "mime";

    public static String ATTACH_BYTES = "attach_bytes_";
    public static String ATTACH_MIME = "attach_mime_";
    public static String ATTACH_FN = "attach_name_";

    public static String[] VALID_SUPPORT_ADDRESSES = { "support" , "help" , "payments" , "info" , "sales", "scoring" };


    //@EJB
    private EmailerFacade emailerFacade;



    /**
     * Convenience method to get/create current instance of this bean
     *
     * @return
     */
    public static EmailUtils getInstance()
    {
        try
        {
            return (EmailUtils) InitialContext.doLookup( "java:module/EmailUtils" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EmailUtils.getInstance() " );

            return null;
        }


        //FacesContext fc = FacesContext.getCurrentInstance();

        //return (EmailUtils) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "emailUtils" );
    }


    public void setEmailFacade()
    {
    	if( emailerFacade == null )
    		emailerFacade = EmailerFacade.getInstance();
    }



    public void sendEmailToAdmin( String subj, String msg )
    {
        try
        {
            if(!subj.toLowerCase().contains("tm2score") )
                subj = "Tm2Score " + subj;

            // prepare to send
            Map<String, Object> emailMap = new HashMap<>();

            emailMap.put( EmailUtils.MIME_TYPE , "text/plain" );

            emailMap.put( EmailUtils.SUBJECT, subj );

            msg = EmailUtils.addNoReplyMessage(msg, false, Locale.US );

            emailMap.put( EmailUtils.CONTENT, msg );

            emailMap.put( EmailUtils.TO, RuntimeConstants.getStringValue("system-admin-email") );

            // emailMap.put( EmailUtils.FROM, RuntimeConstants.getStringValue("support-email") + "|" + MessageFactory.getStringMessage( locale , "g.SupportEmailKey", null ) );
            emailMap.put( EmailUtils.FROM, RuntimeConstants.getStringValue("no-reply-email") );

            sendEmail( emailMap );

            Tracker.addEmailSent();

            Tracker.addEmailToAdmin();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "EmailUtils.sendEmailToAdmin(" + subj + ", msg=" + msg + " )" );
        }
    }

    /*
    public void sendSvcEmail( String toList, String subj, String content )
    {
    	try
    	{
    		LogService.logIt( "EmailUtils.sendSvcEmail( to=" + toList + ", subj=" + subj + ", content=" + content );

    		Map<String,Object> mp = new HashMap<>();

    		mp.put( TO, toList );
    		mp.put( FROM, RuntimeConstants.getStringValue("support-email") ); // new InternetAddress( RuntimeConstants.getStringValue("support-email") ) );
    		mp.put( SUBJECT, subj );
    		mp.put( CONTENT, content );
    		mp.put( MIME_TYPE, "text/plain" );

    		sendEmail( mp );
    	}

    	catch( Exception e )
    	{
    		LogService.logIt( e, "EmailUtils.sendSvcEmail to=" + toList + ", subj=" + subj + ", msg=" + content );
    	}
    }
    */

    public static boolean isNoReplyAddress( String a )
    {
        return a!=null && (a.toLowerCase().startsWith("no-reply") || a.toLowerCase().startsWith("noreply"));
    }

    public static String addNoReplyMessage( String content, boolean html, Locale locale )
    {
        if( content==null )
            content="";
        
        if( html )
            return content + "<p style=\"font-family: arial,calibri,sans-serif;width:600px;\">" + MessageFactory.getStringMessage(locale, "g.EmailBoxNotMonitored") + "</p>";
        
        else
            return content + "\n\n" + MessageFactory.getStringMessage(locale, "g.EmailBoxNotMonitored");
    }
    
    
    
    /**
     * Sends an email message fia a JMS queue.
     *
     * @param messageInfoMap must have the following values: <pre>
     *
     *
     *      to          List<InternetAddress>
     *      cc          List<InternetAddress>
     *      subject     String
     *      from        InternetAddress
     *      mime        mime type. Defaults to text/plain supports text/html
     *      content     String, content of the message
     *      attachments ordered params starting at 0
     *         attach_bytes_0 - bytes    byte[]
     *         attach_mime_0 - mime     String
     *         attach_name_0 - filename String
     *
     *
     *
     * </pre>
     */
    public boolean sendEmail( Map<String,Object> messageInfoMap ) throws Exception
    {
        try
        {
            if( emailerFacade == null )
                emailerFacade = EmailerFacade.getInstance();

            return emailerFacade.sendEmail( messageInfoMap );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "EmailUtils.sendEmail()" );

            throw new STException( e );
        }
    }


    public static String cleanEmailAddress( String em )
    {
        if( em==null )
            return em;
        
        return StringUtils.removeWhitespaceAndControlChars(em);
    }

    
    

    public static boolean validateEmail( InternetAddress iAddr ) throws Exception
    {
        try
        {
            if( 1==1 )
                return EmailValidator.getInstance().isValid( iAddr.getAddress() );
            //LogService.logIt( "EmailUtils.validateEmail() email=" + iAddr.getAddress() );
            
            else 
               iAddr.validate();
        }

        catch( AddressException e )
        {
            LogService.logIt( "Email found invalid: " + ( iAddr==null ? "null" : iAddr.getAddress()) );

            String[] params = new String[2];

            params[0] = iAddr.getAddress();

            params[1] = e.getMessage();

            throw new STException( "g.InvalidEmailAddress" , params );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "EmailUtils.validateEmail() " + ( iAddr == null ? "address is null" : iAddr.getAddress() ) );

            return false;
        }

        return true;
    }



    public static boolean validateEmailNoErrors( String email )
    {
        try
        {
            if( email==null )
                return false;
            
            return validateEmail( new InternetAddress( email ) );
        }

        catch( Exception e )
        {
            return false;
        }
    }


    public static String correctNameForSend( String name )
    {
        if( name==null || name.isBlank() )
            return name;
        return StringUtils.replaceStr(name, ",", "" );
    }

    public static boolean validateEmail( String email ) throws Exception
    {
        try
        {
            return validateEmail( new InternetAddress( email ) );
        }

        catch( AddressException e )
        {
            String[] params = new String[2];

            params[0] = email;

            params[1] = e.getMessage();

            throw new STException( "g.InvalidEmailAddress" , params );
        }
    }





}
