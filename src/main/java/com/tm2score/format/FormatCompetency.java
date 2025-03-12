/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.format;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.score.ScoreCategoryRange;
import java.util.List;

/**
 *
 * @author Mike
 */
public class FormatCompetency {

    public static int MARKER_WIDTH = 7;
    public static float MARKER_LEFT_ADJ = 3.5f;

    int totalPix;
    TestEventScore testEventScore;
    List<ScoreCategoryRange> scoreCategoryRangeList;

    public FormatCompetency( TestEventScore tes, int totalPixWid )
    {
        this.testEventScore = tes;
        totalPix = totalPixWid;

        scoreCategoryRangeList = testEventScore.getScoreCatInfoList(totalPixWid);

        finalizeScoreCatRangeValues();
    }


    /**
     * Used to adjust all range pix values to equal total pix by adjusting the last one.
     */
    private void finalizeScoreCatRangeValues()
    {
        if( scoreCategoryRangeList.isEmpty() )
            return;

        int pix = 0;

        for( ScoreCategoryRange scr : scoreCategoryRangeList )
        {
            pix += scr.getRangePix(testEventScore.getScoreFormatType());
        }

        int adj = totalPix - pix;

        ScoreCategoryRange last = scoreCategoryRangeList.get( scoreCategoryRangeList.size()-1 );

        last.setAdjRange(adj);
    }

    
    public int getPointerLeft(boolean useRawScore)
    {
        float score2Use = 0;
        
        if( testEventScore !=null )
            score2Use = useRawScore ? testEventScore.getOverallRawScoreToShow() : testEventScore.getScore();
        
        if( totalPix <= 0 || testEventScore==null || score2Use<=0 )
            return 0 - Math.round(MARKER_LEFT_ADJ);

        ScoreFormatType sft = testEventScore.getScoreFormatType();
                
        if( score2Use>=sft.getMax() )
            return totalPix - Math.round(MARKER_LEFT_ADJ);

        int specAdj = score2Use<=0 ? 1 : 0;

        return Math.round(((float)totalPix)*((score2Use-sft.getMin())/(sft.getMax()-sft.getMin()))) - Math.round(MARKER_LEFT_ADJ) + specAdj;
    }

    public String getScoreCategoryStarsFilename()
    {
        if( testEventScore == null )
            return null;

        ScoreCategoryType sct = testEventScore.getScoreCategoryType();

        if( sct.black() )
            return "report_detail_stars_0.png";

        else if( sct.red() )
            return "report_detail_stars_1.png";

        else if( sct.redYellow() )
            return "report_detail_stars_2.png";

        else if( sct.yellow() )
            return "report_detail_stars_3.png";

        else if( sct.yellowGreen() )
            return "report_detail_stars_4.png";

        else if( sct.green() )
            return "report_detail_stars_5.png";

        else if( sct.white() )
            return "report_detail_stars_6.png";

        else
            return "report_detail_stars_0.png";


    }

    public TestEventScore getTestEventScore() {
        return testEventScore;
    }

    public void setTestEventScore(TestEventScore testEventScore) {
        this.testEventScore = testEventScore;
    }

    public List<ScoreCategoryRange> getScoreCategoryRangeList() {
        return scoreCategoryRangeList;
    }

    public void setScoreCategoryRangeList(List<ScoreCategoryRange> scoreCategoryRangeList) {
        this.scoreCategoryRangeList = scoreCategoryRangeList;
    }



}
