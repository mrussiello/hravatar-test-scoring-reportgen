/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.misc;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.proctor.ProctorFacade;
import com.tm2score.proctor.RemoteProctorEventStatusType;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author Mike
 */
public class IframeTestAutoCompleteThread implements Runnable {

    EmailUtils emailUtils;
    EventFacade eventFacade;
    ProctorFacade proctorFacade;
    
    @Override
    public void run() {

        // LogService.logIt( "IframeTestAutoCompleteThread.run() Starting"  );

        try
        {
            if( RuntimeConstants.getBooleanValue( "autoIframeCompletionOk" ) )
            {
                // LogService.logIt( "IframeTestAutoCompleteThread.run() Performing Auto Complete." );       
                
                IframeTestFacade iff = IframeTestFacade.getInstance();
                
                Set<Long> teids = iff.getTestEventIdsToAutoComplete( Constants.IFRAMETEST_AUTOCOMPLETE_HOURS );
                
                TestEvent te;
                RemoteProctorEvent rpe;
                TestKey tk;
                
                int count = 0;
                
                for( Long teid : teids )
                {
                    if( proctorFacade==null )
                        proctorFacade=ProctorFacade.getInstance();
                    
                    rpe = proctorFacade.getRemoteProctorEventForTestEventId(teid);
                    
                    if( rpe!=null && !rpe.getRemoteProctorEventStatusType().getEventCompleteOrHigher() )
                    {
                        rpe.setRemoteProctorEventStatusTypeId( RemoteProctorEventStatusType.EVENT_COMPLETE.getRemoteProctorEventStatusTypeId() );
                        proctorFacade.saveRemoteProctorEvent( rpe );
                    }
                    
                    if( eventFacade==null )
                        eventFacade = EventFacade.getInstance();
                    
                    te = eventFacade.getTestEvent( teid, true);                    
                    if( te.getTestEventStatusType().equals( TestEventStatusType.STARTED ) )
                    {
                        te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED.getTestEventStatusTypeId() );
                        eventFacade.saveTestEvent(te);
                    }
                    
                    tk = eventFacade.getTestKey( te.getTestKeyId(), true);                    
                    if( tk.getTestKeyStatusType().equals( TestKeyStatusType.STARTED ) )
                    {
                        tk.setTestKeyStatusTypeId( TestKeyStatusType.COMPLETED.getTestKeyStatusTypeId() );
                        eventFacade.saveTestKey(tk);
                    }
                    
                    LogService.logIt( "IframeTestAutoCompleteThread.run() Autocompleted TestKeyId=" + te.getTestKeyId() + ", te=" + te.getTestEventId() ); 
                    count++;
                }
                
                // LogService.logIt( "IframeTestAutoCompleteThread.run() PROCESS COMPLETE. auto-completed " + count + " IFrame test keys." );                 
            }
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "IframeTestAutoCompleteThread.run() Uncaught Exception during autobatch." );            
            if( emailUtils==null )
                emailUtils = EmailUtils.getInstance();            
           emailUtils.sendEmailToAdmin( "IframeTestAutoCompleteThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }
    }
}
