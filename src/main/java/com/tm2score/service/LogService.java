package com.tm2score.service;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.naming.NamingException;


/**
 * Provides logging services to the application.
 */
public class LogService
{
    public static boolean USE_SERVER_LOG = false;

    private static Logger logger = null;

    public static void init() throws NamingException, Exception
    {
        if( logger == null )
        {
            String filePattern =null;

            int maxSize = 1000000;

            int rotate = 100;

            boolean append = true;

            try
            {
               filePattern = (String) RuntimeConstants.getValue( "services/log/logfilepattern" );
            }
            catch( Exception e )
            {
                System.out.println( "Error loading logfilepattern " + e.toString() );
            }

            if( filePattern == null )
                filePattern = "c:/work/tm2score5/log/tm2score_%g_%u.log";


            try
            {
                logger = Logger.getLogger( "com.tm2score" );

                logger.setLevel( Level.ALL );

                FileHandler fh = new FileHandler( filePattern, maxSize, rotate, append );

                SimpleFormatter sf = new SimpleFormatter();

                fh.setFormatter( sf );

                logger.addHandler( fh );
                // logger.fine( "Logfile Init" );
            }

            catch( Exception e )
            {
                System.out.println( "LogService.init() Getting logger: " + e.toString() );

                StringWriter sw = new StringWriter();

                PrintWriter pw = new PrintWriter( sw );

                e.printStackTrace( pw );

                System.out.println( sw.toString() );

                throw new Exception( "LogService.init() Getting logger: " + e.toString() );
            }
        }
    }

    public static Logger getLogger()
    {
        if( logger == null )
        {
            try
            {
                init();
            }

            catch( Exception e )
            {}
        }

        return logger;
    }


    public static void logIt( String _message )
    {
        if( USE_SERVER_LOG )
        {
            System.out.println( _message );

            return;
        }

        try
        {
            if( logger == null )
                init();

            logger.fine( _message );
        }
        catch( Exception e )
        {
            String m = "Logging exception with message: " + _message + ", threw new Exception " + e.toString();
            System.out.print(m);

        }
    }


    public static void logItNoTrack( Exception e, String message )
    {
        logIt( e, message, false );
    }
    
    public static void logIt( Exception e, String message )
    {
        logIt( e, message, true );
    }
    
    public static void logIt( Exception e, String message, boolean trackit )
    {
        try
        {
            // no stack trace
            if( e instanceof STException )
            {
                logIt( "STERR: " +  message + ", " + ( e.getMessage() != null ? e.getMessage() : "" ) + "{" + ( (STException) e ).getKey() + "}" );

                return;
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter( sw );
            e.printStackTrace( pw );

            logIt( "UERR: " +  message + "\n" + sw.toString() );

            try
            {
                if( trackit )
                    Tracker.addError();
            }

            catch( Exception ex )
            {}
        }

        catch( Exception ee )
        {
            String m = "Logging exception with message: " + message + ", underlying exception was: " + ( e == null ? "NULL" : e.toString() ) + ", threw new Exception " + ee.toString();
            System.out.print(m);
            // logIt( "Logging exception with message: " + message + ", underlying exception was: " + ( e == null ? "NULL" : e.toString() ) + ", threw new Exception " + ee.toString() );
        }
    }

    public static void logIt( Throwable e, String message )
    {
        try
        {
            // no stack trace if BHFF
            if( e instanceof STException )
            {
                logIt( "STERR: " +  message + ", " + e.getMessage() + "{" + ( (STException) e ).getKey() + "}" );

                return;
            }

            StringWriter sw = new StringWriter();

            PrintWriter pw = new PrintWriter( sw );

            e.printStackTrace( pw );

            logIt( "UERR: " +  message + "\n" + sw.toString() );

            try
            {
                Tracker.addError();
            }

            catch( Exception ex )
            {}

        }

        catch( Exception ee )
        {
            String m = "Logging exception with message: " + message + ", underlying exception was: " + ( e == null ? "NULL" : e.toString() ) + ", threw new Exception " + ee.toString();
            System.out.print(m);
            // logIt( "Logging exception with message: " + message + ", underlying exception was: " + ( e == null ? "NULL" : e.toString() ) + ", threw new Exception " + ee.toString() );
        }
    }
}
