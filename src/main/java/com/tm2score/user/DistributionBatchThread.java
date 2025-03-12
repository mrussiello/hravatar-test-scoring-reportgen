/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.user;

import com.tm2score.dist.DistManager;
import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.STException;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import java.util.Set;

/**
 *
 * @author miker
 */
public class DistributionBatchThread implements Runnable {
    
    Set<Long> testKeyIds;
    
    EventFacade eventFacade;
    
    public DistributionBatchThread( Set<Long> testKeyIds )
    {
        this.testKeyIds=testKeyIds;
    }

    @Override
    public void run() {
        
        long testKeyId=0;
        if( testKeyIds==null || testKeyIds.isEmpty() )
            return;
        
        eventFacade = EventFacade.getInstance();

        TestKey tk; //  = eventFacade.getTestKey(userBean.getTestKeyId3(), true);
        StringBuilder sb = new StringBuilder();
        String msg;
        try
        {
            DistManager smb = new DistManager();            
            int[] tko;
            int[] tkot = new int[4];
            
            for( long tkid: testKeyIds )
            {
                testKeyId=tkid;
                
                tk=eventFacade.getTestKey(testKeyId, true);
                if( tk==null )
                    throw new Exception( "TestKey not found for testKeyId=" + testKeyId );
                
                if( !tk.getTestKeyStatusType().equals( TestKeyStatusType.REPORTS_COMPLETE ) &&
                    !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_STARTED ) &&
                    !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_COMPLETE ) &&
                     !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_ERROR )   )
                {

                    msg = "DistributionBatchThread.run() TestKey Status is not valid for redistribution, so skipping. status=" + tk.getTestKeyStatusType().getTestKeyStatusTypeId() + ", testKeyId=" + testKeyId;
                    LogService.logIt( "DistributionBatchThread.run() " + msg );
                    sb.append( msg + "\n");
                    continue;
                }

                try
                {
                    // tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() );
                    tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId() );

                    eventFacade.saveTestKey(tk);

                    Tracker.addRedistribution();

                    BaseScoreManager.addTestKeyToDateMap( tk.getTestKeyId() );

                    tko = smb.distributeTestKeyResults(tk, false, 0, 0, 0 );

                    BaseScoreManager.removeTestKeyFromDateMap(tk.getTestKeyId() );

                    if( tko[0] > 0 || tko[1] > 0 || tko[2]>0 )
                    {
                        tkot[0]+=tko[0];
                        tkot[1]+=tko[1];
                        tkot[2]+=tko[2];
                        LogService.logIt( "DistributionBatchThread.run() TestKeyId: " + testKeyId + " has been redistributed. " + tko[0] + " administrator emails sent and " + tko[1] + " text messages sent and " + tko[2] + " Test-Taker emails sent." );
                    } 
                    else                   
                        LogService.logIt( "DistributionBatchThread.run() TestKeyId: " + testKeyId + " has been redistributed. but no emails or text messages have been sent (none needed, no errors)." );
                }
                catch( STException e )
                {
                    sb.append( e.toString() + " testKeyId=" + testKeyId + "\n" );
                    BaseScoreManager.removeTestKeyFromDateMap(testKeyId);
                }
                catch( ScoringException e )
                {
                    LogService.logIt( "DistributionBatchThread.run() " + e.toString() + ", testKeyId=" + testKeyId );
                    sb.append( e.toString() + " testKeyId=" + testKeyId + "\n" );
                    BaseScoreManager.removeTestKeyFromDateMap(testKeyId);
                }        
                catch( Exception e )
                {
                    LogService.logIt( e, "DistributionBatchThread.run() testKeyId=" + testKeyId );
                    sb.append( e.toString() + " testKeyId=" + testKeyId + "\n" );
                    BaseScoreManager.removeTestKeyFromDateMap(testKeyId);
                }    
            }
            msg = "DistributionBatchThread.run() PROCESS COMPLETE. Totals: " + tkot[0] + " administrator emails sent, " + tkot[1] + " text messages sent, and " + tkot[2] + " Test-Taker emails sent. " + (sb.isEmpty() ? "" : " ERRORS:\n" + sb.toString());
            LogService.logIt( msg );                
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DistributionBatchThread.run() testKeyId=" + testKeyId + ", testKeyIds.size=" + (testKeyIds==null ? "null" : testKeyIds.size()) );
        }
    }
    
    
}
