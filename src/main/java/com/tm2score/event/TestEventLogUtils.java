/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.event;


import com.tm2score.entity.event.TestEventLog;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 *
 * @author miker_000
 */
public class TestEventLogUtils {
   
    
    
    /**
     * Levels
     *  2 - info
        1 - warning
        0 - error
     */
    public static void createTestEventLogEntry( long testEventId, String logEntry ) 
    {
        createTestEventLogEntry( testEventId, 2, logEntry, null, null ); 
    }
    
    /**
     *  2 - info
        1 - warning
        0 - error
     */
    public static void createTestEventLogEntry( long testEventId, int level, String logEntry, String ipAddress, String userAgent ) 
    {
        try
        {
            TestEventLog tel = doTestEventLogEntry(0, testEventId, level, logEntry, ipAddress, userAgent );

            if( tel!=null )
                EventFacade.getInstance().saveTestEventLog(tel);
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "TestEventLogUtils.createTestEventLogEntry() testEventId=" + testEventId + ", legEntry=" + logEntry );
        }
    }
    

    public static void createTestKeyLogEntry( long testKeyId, long testEventId, int level, String logEntry, String ipAddress, String userAgent )
    {
        try
        {
            TestEventLog tel = doTestEventLogEntry( testKeyId, testEventId, level, logEntry, ipAddress, userAgent );

            if( tel!=null )
                EventFacade.getInstance().saveTestEventLog(tel);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestEventLogUtils.createTestKeyLogEntry() testKeyId=" + testKeyId + ",testEventId=" + testEventId + ", legEntry=" + logEntry );
        }        
    }
    
        
    private static TestEventLog doTestEventLogEntry( long testKeyId, long testEventId, int level, String logEntry, String ipAddress, String userAgent)
    {
        if( testEventId<=0 && testKeyId<=0 )
            return null;
        
        if( level<0 || level>2 )
            level=2;
        
        if( logEntry==null || logEntry.isEmpty() )
            return null;
        
        TestEventLog tel = new TestEventLog();

        tel.setTestKeyId( testKeyId );
        tel.setTestEventId(testEventId);
        tel.setLevel(level);
        tel.setLog(logEntry);
        tel.setIpAddress(ipAddress);
        tel.setUserAgent(userAgent);
        
        return tel;
    }
}
