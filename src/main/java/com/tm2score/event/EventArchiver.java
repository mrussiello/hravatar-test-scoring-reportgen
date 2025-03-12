package com.tm2score.event;

import com.tm2score.entity.event.*;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;


public class EventArchiver
{
    // @Inject
    EventFacade eventFacade;

    public void archiveTestKey( TestKey testKey )  throws Exception
    {
        try
        {
            if( testKey.getTestKeyId() <= 0 )
                throw new Exception( "EventArchiver.archiveTestKey() testkeyid invalid " + testKey.getTestKeyId() );

            if( testKey.getTestKeyArchiveId()>0 )
                return;

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            eventFacade.archiveTestKey(testKey);
        }
        catch( STException e )
        {
            //throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventArchiver.archiveTestKey() " + testKey.toString() );
            //throw new STException(e);
        }
    }

}
