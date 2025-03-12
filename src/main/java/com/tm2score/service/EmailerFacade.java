/*
 * Created on Dec 30, 2006
 *
 */
package com.tm2score.service;

import com.tm2score.email.EmailBlockFacade;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.Session;

import javax.naming.InitialContext;

@Stateless
public class EmailerFacade
{

    public EmailerFacade( ConnectionFactory cf, Queue q )
    {
        this.connectionFactory=cf;
        this.queue=q;
    }

    public EmailerFacade()
    {}


    public static EmailerFacade getInstance()
    {
        try
        {
            return (EmailerFacade) InitialContext.doLookup( "java:module/EmailerFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }

    @Resource( mappedName = "jms/ConnectionFactory" )
    protected ConnectionFactory connectionFactory;

    @Resource( mappedName = "jms/seenthatemailqueue" )
    protected Queue queue;

    public boolean sendEmail( Map<String, Object> messageInfoMap ) throws Exception
    {
        Connection connection = null;
        Session session = null;
        MessageProducer messageProducer = null;
        MapMessage message = null;

        try
        {
            String to1 = (String) messageInfoMap.get( EmailUtils.TO );     
            
            // First, Make sure there is no Full Email Block on the emails
            correctAllEmailsForBlocks( messageInfoMap );

            String to = (String) messageInfoMap.get( EmailUtils.TO );        
            if( to==null || to.isEmpty() )
            {
                LogService.logIt( "EmailerFacade.sendEmail() After block processing, there are no to emails. Subj=" + (String) messageInfoMap.get( EmailUtils.SUBJECT ) + ", Original To: " + to1 );
                return false;
            }
            
            String from = (String) messageInfoMap.get( EmailUtils.FROM );        
            if( from==null || from.isEmpty() )
                return false;

            
            // JMS connection
            connection = connectionFactory.createConnection();

            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );

            messageProducer = session.createProducer( queue );

            message = session.createMapMessage();

            // now copy all keys to MapMessage

            Set<String> keys = messageInfoMap.keySet();

            Object obj;

            for( String key : keys )
            {
                // LogService.logIt( "Setting object: " + key + ", " + messageInfoMap.get( key ) );

                obj = messageInfoMap.get( key );

                if( obj instanceof String )
                    message.setString( key, (String) obj );

                if( obj instanceof byte[] )
                    message.setBytes( key, (byte[]) obj );
            }

            messageProducer.send( message );
        }

        catch( JMSException e )
        {
            LogService.logIt( e, "EmailerBean.sendEmail()" );
            return false;

        }

        finally
        {
            if( connection != null )
            {
                try
                {
                    connection.close();
                }
                catch( JMSException e )
                {}
            } // if

        } // finally
        
        return true;
    }
    
    private void correctAllEmailsForBlocks( Map<String, Object> messageInfoMap ) throws Exception
    {
        String emails = (String) messageInfoMap.get(EmailUtils.TO );        
        String o = correctForBlocks( emails );        
        if( o==null || o.isBlank() )
            messageInfoMap.remove(EmailUtils.TO );
        else
            messageInfoMap.put(EmailUtils.TO, o );

        emails = (String) messageInfoMap.get(EmailUtils.CC );        
        o = correctForBlocks( emails );        
        if( o==null || o.isBlank() )
            messageInfoMap.remove(EmailUtils.CC );
        else
            messageInfoMap.put(EmailUtils.CC, o );

        emails = (String) messageInfoMap.get(EmailUtils.BCC );        
        o = correctForBlocks( emails );        
        if( o==null || o.isBlank() )
            messageInfoMap.remove(EmailUtils.BCC );
        else
            messageInfoMap.put(EmailUtils.BCC, o );

        emails = (String) messageInfoMap.get(EmailUtils.FROM );        
        o = correctForBlocks( emails );        
        if( o==null || o.isBlank() )
            messageInfoMap.remove(EmailUtils.FROM );
        else
            messageInfoMap.put(EmailUtils.FROM, o );
    }
    
    private String correctForBlocks( String emailStr ) throws Exception
    {
        // First, Make sure there is no Full Email Block on the emails
        
        if( emailStr==null )
            return null;
        
        String[] emails;
        
        if( emailStr.indexOf(";")>0 )
            emails = emailStr.split( ";" );
        
        else
            emails = emailStr.split( "," );
        
        StringBuilder emails2 = new StringBuilder();
        
        EmailBlockFacade elf = EmailBlockFacade.getInstance();
        
        String eml;
        
        for( String em : emails )
        {
            eml=em.trim();
            
            if( eml.indexOf("|")>=0 )
            {
                eml = eml.substring(0, eml.indexOf("|"));
                eml = eml.trim();
            }
            
            if( eml.isEmpty() )
            {
                LogService.logIt( "EmailerFacade.correctForBlocks() skipping because edited email is empty. " + em );
                continue;                
            }
            
            if( elf.hasEmailBlock(eml, true, true) )
            {
                LogService.logIt( "EmailerFacade.correctForBlocks() skipping due to full email block. " + em );
                continue;
            }
            
            if( emails2.length()>0 )
                emails2.append(",");
            
            emails2.append(em);
        }
               
        if( emails2.length()<=0 )
            return null;
        
        return emails2.toString();
        
    }
    
    
    
}
