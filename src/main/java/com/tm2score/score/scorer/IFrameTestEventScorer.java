/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class IFrameTestEventScorer implements TestEventScorer
{
    TestKey tk;
    EventFacade eventFacade;    
    
    @Override
    public void scoreTestEvent( TestEvent te, SimDescriptor sd, boolean skipVersionCheck) throws Exception
    {
        try
        {
            if( !te.getTestEventStatusType().getIsCompleteOrHigher() )
                throw new Exception( "TestEvent is not complete or higher. " );
                

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            te.setTestEventStatusTypeId( TestEventStatusType.SCORED.getTestEventStatusTypeId() );
            eventFacade.saveTestEvent(te);   
            
            tk = eventFacade.getTestKey( te.getTestKeyId(), true );
            if( !tk.getTestKeyStatusType().getIsScoreComplete() )
            {
                tk.setTestKeyStatusTypeId( TestKeyStatusType.SCORED.getTestKeyStatusTypeId() );
                tk.setErrorCnt(0);
                eventFacade.saveTestKey(tk);
            }            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "IFrameTestEventScorer.scoreTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId() ) );
        }
    }


    @Override
    public void recalculatePercentilesForTestEvent( TestEvent te, SimDescriptor sd ) throws Exception
    {        
    }
    
    @Override
    public String getScoreStatusStr()
    {
        return "";
    }


    @Override
    public String toString()
    {
        return "IFrameTestEventScorer() ";
    }

    @Override
    public void setClearExternal(boolean clearExternal)
    {
        // does nothing in this class.
    }

    
    
}
