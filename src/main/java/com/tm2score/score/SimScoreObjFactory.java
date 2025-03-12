/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.score.simcompetency.VoicePerformanceSimCompetencyScore;
import com.tm2score.sim.SimCompetencyClass;

/**
 *
 * @author miker_000
 */
public class SimScoreObjFactory {
    
    public static SimCompetencyScore createSimCompetencyScore( SimJ.Simcompetency sc, TestEvent te, boolean useTotalItems, boolean validItemsCanHaveZeroMaxPoints ) throws Exception
    {
        if( te==null )
            throw new Exception( "SimScoreObjFactory.createSimCompetencyScore() need TestEvent to determine ProductType" );
        
        //if( te.getProductTypeId() == ProductType.IVR.getProductTypeId() )
        //    return new SimCompetencyScore( sc, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
            // return new IvrSimCompetencyScore( sc, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
        SimCompetencyClass scc = SimCompetencyClass.getValue( sc.getClassid() );
        
        //if( scc.equals( SimCompetencyClass.SCOREDIMAGEUPLOAD ) )
        //    return new ImageCaptureSimCompetencyScore( sc, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
            
        if( scc.equals( SimCompetencyClass.VOICE_PERFORMANCE_INDEX ) )
            return new VoicePerformanceSimCompetencyScore( sc, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
            
        //if( scc.equals( SimCompetencyClass.SCOREDCHAT ) )
        //    return new ScoredChatSimCompetencyScore( sc, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
            
        return new SimCompetencyScore( sc, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
    }

    public static SimletScore createSimletScore( SimJ.Simlet smltObj, TestEvent te, boolean useTotalItems, boolean validItemsCanHaveZeroMaxPoints )
    {
        return new SimletScore( smltObj, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
    }
    
    
    public static SimletCompetencyScore createSimletCompetencyScore( TestEvent te, SimletScore ss, SimJ.Simlet.Competencyscore scsObj, boolean useTotalItems, boolean validItemsCanHaveZeroMaxPoints ) throws Exception
    {
        if( te==null )
            throw new Exception( "SimScoreObjFactory.createSimletCompetencyScore() need TestEvent to determine ProductType" );
        
        //if( te.getProductTypeId() == ProductType.IVR.getProductTypeId() )
        //    return new SimletCompetencyScore( ss, scsObj, useTotalItems, validItemsCanHaveZeroMaxPoints  );
            // return new IvrSimletCompetencyScore( ss, scsObj, useTotalItems, validItemsCanHaveZeroMaxPoints );
        
        return new SimletCompetencyScore( ss, scsObj, useTotalItems, validItemsCanHaveZeroMaxPoints  );
        
    }

}
