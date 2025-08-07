/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventFacade;
import com.tm2score.report.ReportManager;
import com.tm2score.service.LogService;
import java.util.ArrayList;

/**
 *
 * @author Mike
 */
public class RescoreRereportThread extends Thread
{
    long testKeyId;
    boolean rescoreOnly = false;
    boolean skipCompleted = false;
    boolean createReportsAsArchived = false;

    // @Inject
    EventFacade eventFacade;

    //public RescoreRereportThread( long testKeyId )
    //{
    //   this.testKeyId = testKeyId;
    //}
    
    public RescoreRereportThread( long testKeyId, boolean rescoreOnly, boolean createReportsAsArchived)
    {
        this.testKeyId = testKeyId;
        this.rescoreOnly = rescoreOnly;
        this.createReportsAsArchived=createReportsAsArchived;
    }

    @Override
    public void run()
    {
        try
        {
            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            if( testKeyId <= 0 )
                throw new Exception( "testKeyId invalid " + testKeyId );

            TestKey tk = eventFacade.getTestKey(testKeyId, true );

            if( tk == null )
            {
                LogService.logIt( "RescoreRereportThread.run() ERROR testKey not found for testKeyId=" + testKeyId );
                return;
            }

            LogService.logIt( "RescoreRereportThread.run() START testKeyId=" + testKeyId );
            ScoreManager sm = new ScoreManager();

            /*
                * int[0] = testkeys scored.
                * int[1] = testevents scored.
                * int[2] = testkeys placed in pending status
                * int[3] = testevents placed in pending status
                * int[4] = testevents skipped because they are incompleted (only if partiallyCompleteBatteriesOk=true)
            
            */
            int[] tko = sm.rescoreTestKey(testKeyId, rescoreOnly, false );

            if( tko[0] <= 0 && tko[2]<=0 )
            {
                LogService.logIt( "RescoreRereportThread.run() ERROR TestKey rescoring failed. testKeyId=" + testKeyId );
                return;
            }

            if( tko[2]>0 )
            {
                 LogService.logIt( "RescoreRereportThread.run() testKeyId=" + testKeyId + "TestKey is in a status of awaiting external scores. Cannot regenerate reports. Reports will be generated on next batch cycle after external scores are compelted." );
                 return;
            }

            Thread.sleep( 10000 );
            
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            
            TestKey tkx = eventFacade.getTestKey(testKeyId, true );
            
            if( tkx==null || !tkx.getTestKeyStatusType().getIsScoredOrHigher() )
            {
                 LogService.logIt( "RescoreRereportThread.run() testKeyId=" + testKeyId + "TestKey is not in scored status or higher after re-scoring. Letting batch process handle reports. returning. testKeyId=" + testKeyId );
                 return;                
            }

            ReportManager rm = new ReportManager();

            //if( rmb.getTestEventScoreList() == null )
            rm.setTestEventScoreList( new ArrayList<>() );

            int ct = rm.genOrRegenReportsTestKey(testKeyId, 0, false, false, null, skipCompleted, createReportsAsArchived );

            LogService.logIt( "RescoreRereportThread.run() FINISH testKeyId=" + testKeyId + " created " + ct + " reports." );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RescoreRereportThread.run() testKeyId=" + testKeyId );
        }

    }

}
