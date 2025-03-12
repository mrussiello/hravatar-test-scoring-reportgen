/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.findly;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreStatusType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Mike
 */
public class FindlyUnscorableScoreInfo extends BaseFindlyScoreInfo implements FindlyScoreInfo
{

    @Override
    public void populateTestEventAndCreateTestEventScoreList( TestEvent te ) throws Exception
    {
        try
        {
            this.te = te;

            Date start = te.getStartDate();
            Date last = te.getLastAccessDate();
            
            if( start !=null && last !=null && !start.equals( last ) && start.getTime()<last.getTime() )
            {
                te.setTotalTestTime(  (last.getTime() - start.getTime())/(1000) );
            }

            float overallScr = 0; //  getOverallScore();

            if( getOverallScore()>=0 )
                overallScr = getOverallScore();
            
            te.setOverallScore( overallScr );

            if( te.getTestEventScoreList() == null )
                te.setTestEventScoreList(new ArrayList<TestEventScore>());

            List<TestEventScore> tesl=te.getTestEventScoreList();

            TestEventScore tes = new TestEventScore();
            tes.setTestEventScoreTypeId( TestEventScoreType.OVERALL.getTestEventScoreTypeId() );
            tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
            tes.setTestEventId( te.getTestEventId() );
            tes.setCreateDate(new Date() );
            tes.setDateParam1(new Date());
            tes.setName( "Overall Score" );
            tes.setRawScore( te.getOverallScore() );
            tes.setScore( te.getOverallScore() );
            tes.setScoreFormatTypeId( ScoreFormatType.NUMERIC_0_TO_100.getScoreFormatTypeId() );
            
            TestEventScore tes2 = getMatchingExistingTestEventScore( tes );
            if( tes2!=null && tes2.getTestEventScoreId()>0 )
                tes.setTestEventScoreId( tes2.getTestEventScoreId() );
            
            tesl.add( tes );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "FindlyUnscorableScoreInfo.populateTestEventAndCreateTestEventScoreList() " + te.toString() );
        }

    }
    
    
    
    
}
