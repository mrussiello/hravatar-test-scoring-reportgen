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
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class FindlyProofreadingScoreInfo extends BaseFindlyScoreInfo implements FindlyScoreInfo
{

    @Override
    public void populateTestEventAndCreateTestEventScoreList( TestEvent te) throws Exception
    {
        try
        {
            this.te = te;

            setScoreObjects( te );

            te.setTotalTestTime( findlyTestInfo.getTimeElapsed() );

            te.setOverallScore( getMetricValue( "Score") );

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
            tes.setScoreText( ""); //  getStringMetricValue( "Grade" ) );
            tes.setScoreFormatTypeId( ScoreFormatType.NUMERIC_0_TO_100.getScoreFormatTypeId() );
            TestEventScore tes2 = getMatchingExistingTestEventScore( tes );

            if( tes2!=null && tes2.getTestEventScoreId()>0 )
                tes.setTestEventScoreId( tes2.getTestEventScoreId() );

            FindlyPercentile fp = getFindlyPercentile();

            if( fp != null )
            {
                te.setOverallPercentile( fp.percentile );
                te.setOverallPercentileCount( fp.scoreCount );
                tes.setPercentile(fp.percentile);
                tes.setOverallPercentileCount( fp.scoreCount );
            }

            tesl.add( tes );

        }

        catch( Exception e )
        {
            LogService.logIt(e, "FindlyProofreadingScoreInfo.populateTestEventAndCreateTestEventScoreList() " + te.toString() );
        }

    }

}
