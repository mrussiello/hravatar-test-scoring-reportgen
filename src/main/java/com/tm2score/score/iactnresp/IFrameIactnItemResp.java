/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.score.SimletScore;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class IFrameIactnItemResp extends IactnItemResp
{
    // public static String POINTS_KEY = "[POINTS]";

    public IFrameIactnItemResp( IactnResp ir, SimJ.Intn.Intnitem ii, Clicflic.History.Intn iro)
    {
        super(ir, ii, iro, null);
    }



    @Override
    public void init( SimletScore ss, TestEvent te )
    {
        try
        {
            g2ChoiceFormatType = G2ChoiceFormatType.getValue(  intnItemObj.getFormat()  );

            if( intnItemObj.getCompetencyscoreid() > 0 && g2ChoiceFormatType.supportsSubnodeLevelSimletAutoScoring() )
                simletCompetencyScore = ss.getSimletCompetencyScore( intnItemObj.getCompetencyscoreid() );

            //String respStr = getRespValue();

            //float[] mpa = getMaxPointsArray();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IFrameIactnItemResp.init() " + toString() );
        }
    }

    @Override
    public void calculateScore() throws Exception
    {
        // LogService.logIt( "IFrameIactnItemResp.calculateScore() IntnItem (" + iactnResp.intnObj.getSeq() + "," + intnItemObj.getSeq() + " points assigned=" + icsi.getPoints() + ", maxPoints=" + (getMaxPointsArray() == null ? "null" : Float.toString( getMaxPointsArray()[0])) );
    }

    @Override
    public String toString() {
        return "IFrameIactnItemResp{ iactn=" + (iactnResp==null || iactnResp.intnObj==null ? "iactnResp.intnObj is null" : iactnResp.intnObj.getName()) +
                " ("  +  (iactnResp.intnObj != null ? iactnResp.intnObj.getSeq() : "" ) + "-" + ( intnItemObj==null ? "intnItemObj is null" : intnItemObj.getSeq()) +  "), content=" + (intnItemObj==null ? "" : intnItemObj.getContent()) +
                " simletCompetencyScore.name=" + (simletCompetencyScore==null ? "simletCompetencyScore is null" : simletCompetencyScore.competencyScoreObj.getName()) + ", maxPoints[0]=" + (getMaxPointsArray() == null ? "null" : Float.toString( getMaxPointsArray()[0])) + '}';
    }

    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return true;
    }

    @Override
    public boolean correct()
    {
        return false;
    }

    @Override
    public boolean hasCorrectRespForSubnodeDichotomousScoring()
    {
        return true;
    }

    @Override
    public float itemScore()
    {
        return 0;
    }

    /**
     * map of topic name, int[]
     *    int[0] = number correct        ( for this item this means points )
     *    int[1] = number total this topic.  max points for competency
     *    int[2] = number of items that were partially correct.  ( 0 )
     *    int[3] = total number of items this topic.
     */
    @Override
    public Map<String,int[]> getTopicMap()
    {
        return null;
    }



    @Override
    public void populateItemResponse( ItemResponse ir )
    {
        super.populateItemResponse(ir);

        ir.setSelectedValue( StringUtils.truncateString( getRespValue(), 1900 )   );

    }
}
