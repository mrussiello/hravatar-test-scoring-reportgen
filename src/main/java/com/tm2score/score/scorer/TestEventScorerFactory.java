/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.scorer;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.service.LogService;
import java.lang.reflect.Constructor;

/**
 *
 * @author Mike
 */
public class TestEventScorerFactory {

    public static TestEventScorer getTestEventScorer( TestEvent te ) throws Exception
    {
        // TestEventScorer ts;

        // This comes first.
        if( te.getSimDescriptor()!=null && te.getSimDescriptor().getScoreModuleClass()!=null && !te.getSimDescriptor().getScoreModuleClass().isBlank())
        {
            Class<TestEventScorer> tc = (Class<TestEventScorer>) Class.forName( te.getSimDescriptor().getScoreModuleClass() );

            Constructor ctor = tc.getDeclaredConstructor();
            TestEventScorer ts = (TestEventScorer) ctor.newInstance();            
            // ts = tc.newInstance();
            
            // LogService.logIt( "TestEventScorerFactory.getTestEventScorer() Creating module for Class " + te.getSimDescriptor().getScoreModuleClass() + ", module found: " + (ts!=null) );
            
            if( ts!=null )
                return ts;
        }

        if( te.getProduct()!= null && te.getProduct().getProductType().getIsCt5Direct())
            return new CT5DirectTestEventScorer();

        if( te.getProduct()!= null && te.getProduct().getProductType().getIsIFrameTest())
            return new IFrameTestEventScorer();

        if( te.getProduct()!= null && te.getProduct().getProductType().getIsFindly() )
            return new FindlyTestEventScorer();

        //if( te.getProduct()!= null && (te.getProduct().getProductType().getIsIvr() || te.getProduct().getProductType().getIsVot()) )
        //    return new IvrTestEventScorer();

        return new StandardTestEventScorer();
    }
}
