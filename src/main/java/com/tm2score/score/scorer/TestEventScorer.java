/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.sim.SimDescriptor;

/**
 *
 * @author Mike
 */
public interface TestEventScorer
{

    public void setClearExternal( boolean clearExternal );
    
    public void scoreTestEvent( TestEvent te, SimDescriptor sd, boolean skipVersionCheck ) throws Exception;


    public void recalculatePercentilesForTestEvent( TestEvent te, SimDescriptor sd ) throws Exception;
    
    public String getScoreStatusStr();
}
