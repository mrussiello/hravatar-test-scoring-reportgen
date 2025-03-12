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
public class FindlyOfficeManagerScoreInfo extends BaseFindlyScoreInfo implements FindlyScoreInfo
{

    @Override
    public void populateTestEventAndCreateTestEventScoreList( TestEvent te) throws Exception
    {
        try
        {
            this.te = te;

            setScoreObjects( te );

            te.setTotalTestTime( findlyTestInfo.getTimeElapsed() );

            String overallStr = this.getMetricValueString( "Rating" );
            
            if( overallStr == null )
                overallStr = "";
            
            float scr = 0;
            
            if( overallStr.equalsIgnoreCase( "Excellent" ))
                scr = 100.0f;
            
            else if( overallStr.equalsIgnoreCase( "Good" ))
                scr = 80.0f;
            
            else if( overallStr.equalsIgnoreCase( "Acceptable" ))
                scr = 70.0f;
            
            else if( overallStr.equalsIgnoreCase( "Questionable" ))
                scr = 50.0f;
            
            else if( overallStr.equalsIgnoreCase( "Unacceptable" ))
                scr = 20.0f;
            
            te.setOverallScore( scr );

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
            tes.setScoreText( overallStr );
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
            LogService.logIt(e, "BaseFindlyScoreInfo.FindlyOfficeManagerScoreInfo() " + te.toString() );
        }

    }
    

}
