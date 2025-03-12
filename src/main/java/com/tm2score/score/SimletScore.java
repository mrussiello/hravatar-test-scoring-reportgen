/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.score.iactnresp.IactnResp;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.service.LogService;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class SimletScore
{
    public SimJ.Simlet simletObj;

    public List<SimJ.Intn> intnObjList = null;

    public List<SimletCompetencyScore> simletCompetencyScoreList = null;

    public TestEvent testEvent = null;
    

    public SimletScore( SimJ.Simlet smltObj, TestEvent te, boolean useTotalItems, boolean validItemsCanHaveZeroMaxPoints )
    {
        this.simletObj = smltObj;

        this.testEvent = te;

        simletCompetencyScoreList = new ArrayList<>();

        intnObjList = new ArrayList<>();

        try
        {
            for( SimJ.Intn intn : te.getSimXmlObj().getIntn() )
            {
                if( intn.getSimletid() == simletObj.getId() )
                    intnObjList.add( intn );
            }

            for( SimJ.Simlet.Competencyscore scsObj : simletObj.getCompetencyscore() )
            {
                simletCompetencyScoreList.add( SimScoreObjFactory.createSimletCompetencyScore(te, this, scsObj, useTotalItems, validItemsCanHaveZeroMaxPoints ) );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SimletScore() Simlet: " + (smltObj==null ? "SimletObj is null" : smltObj.getName() + " (" + smltObj.getId() + ")") + ", " + (te==null ? "TestEvent is null." : te.toString() ) );
        }
    }


    @Override
    public String toString()
    {
        return "SimletScore{" + "simlet=" + simletObj.getName() + ", id=" + simletObj.getId() + ", competencies=" + this.simletCompetencyScoreList.size() + '}';
    }


    public SimletCompetencyScore getCompetencyScoreForSimCompetencyId( long simCompetencyId )
    {
        // LogService.logIt( "SimletScore.getCompetencyScoreForSimCompetencyId() simCompetencyId=" + simCompetencyId + ", simletCompetencyScores=" + (simletCompetencyScoreList == null ? 0 : simletCompetencyScoreList.size() ) );

        if( simletCompetencyScoreList == null )
            return null;

        for( SimletCompetencyScore scs : simletCompetencyScoreList )
        {
            // LogService.logIt( "SimletScore.getCompetencyScoreForSimCompetencyId() seeking simCompetencyId=" + simCompetencyId + ", have " + scs.competencyScoreObj.getSimcompetencyid() );
            if( scs.competencyScoreObj.getSimcompetencyid() == simCompetencyId )
                return scs;          
        }

        return null;

    }

    public SimletCompetencyScore getSimletCompetencyScore( long id )
    {
        if( simletCompetencyScoreList == null )
            return null;

        for( SimletCompetencyScore scs : simletCompetencyScoreList )
        {
            if( scs.competencyScoreObj.getId() == id )
                return scs;
        }

        return null;
    }

    public SimletCompetencyScore getSimletCompetencyScoreByName( String name )
    {
        LogService.logIt( "SimletScore.getSimletCompetencyScoreByName() AAA name=" + name + ", simletCompetencyScoreList=" + simletCompetencyScoreList.size() );
        
        if( simletCompetencyScoreList == null )
            return null;

        String nm;
        
        for( SimletCompetencyScore scs : simletCompetencyScoreList )
        {
            nm = UrlEncodingUtils.decodeKeepPlus(scs.competencyScoreObj.getName());
            
            //LogService.logIt( "SimletScore.getSimletCompetencyScoreByName() BBB comparing to " + nm );
            if( nm.equalsIgnoreCase(name) )
                return scs;

            nm = UrlEncodingUtils.decodeKeepPlus(scs.competencyScoreObj.getNameenglish());
            
            //LogService.logIt( "SimletScore.getSimletCompetencyScoreByName() CCC comparing to " + nm );
            
            if( nm!=null && nm.equalsIgnoreCase(name) )
                return scs;
        }

        return null;
    }
    
    
    public IactnResp getEndpointIaction()
    {
        if( testEvent == null )
            return null;

        for( IactnResp ir : testEvent.getAllIactnResponseList() )
        {
            // LogService.logIt( "SimletScore.getEndPointIactn() simletScore=" + simletId() + ", " +  ir.toString() + " IS ENDPOINT: " + ir.isSimletEndpoint() );

            if( ir.simletId() == simletId() && ir.isSimletEndpoint() )
                return ir;
        }

        LogService.logIt( "SimletScore.getEndPointIactn() simletScore=" + simletId()  + " no endpoint found for this simlet!" );

        return null;
    }


    public long simletId()
    {
        return simletObj == null ? 0 : simletObj.getId();
    }

    public TestEvent getTestEvent() {
        return testEvent;
    }



}
