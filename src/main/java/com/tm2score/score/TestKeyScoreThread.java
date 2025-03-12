/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestKey;
import com.tm2score.service.LogService;

/**
 *
 * @author miker_000
 */
public class TestKeyScoreThread extends BaseScoreManager implements Runnable
{
    TestKey testKey;
    TestEvent partialBatteryTestEvent;
    int delaySeconds = 0;
    boolean partiallyCompleteBatteriesOk;
    
    public TestKeyScoreThread(TestKey tk, boolean partiallyCompleteBatteriesOk, int delaySeconds)
    {
        this.testKey = tk;
        this.partiallyCompleteBatteriesOk = partiallyCompleteBatteriesOk;
        this.delaySeconds=delaySeconds;
    }

    public TestKeyScoreThread(TestEvent te, int delaySeconds)
    {
        this.partialBatteryTestEvent = te;
        this.delaySeconds=delaySeconds;
    }

    
    @Override
    public void run() 
    {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        try
        {
            if( delaySeconds>0 )
                Thread.sleep( delaySeconds*1000 );
            
            // LogService.logIt( "TestKeyScoreThread.run() START, TestKeyId=" + (this.testKey==null ? "NULL" : testKey.getTestKeyId() ) );
            
            // Normal case
            if( testKey!=null && partialBatteryTestEvent==null )
                scoreTestKey(testKey, partiallyCompleteBatteriesOk, false );
            
            // Partially Completed Battery Test Event case.
            else if( partialBatteryTestEvent!=null )
                scorePartiallyCompleteBatteryTestEvent( partialBatteryTestEvent,  false );
        }
        catch( ScoringException e )
        {
            saveErrorInfo( e );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "TestKeyScoreThread.run() " + e.getMessage() + " testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId() ) + "partialBattery testEventId=" + (partialBatteryTestEvent==null ? "null" : partialBatteryTestEvent.getTestEventId() ) );
        }
    }
    
    
}
