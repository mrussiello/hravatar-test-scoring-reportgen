/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.ct5.event.Ct5EventFacade;
import com.tm2score.ct5.event.Ct5ResultXmlGenerator;
import com.tm2score.entity.ct5.event.Ct5ItemResponse;
import com.tm2score.entity.ct5.event.Ct5TestEvent;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import com.tm2score.xml.JaxbUtils;

/**
 *
 * @author Mike
 */
public class CT5DirectAvTestEventScorer extends AvTestEventScorer implements TestEventScorer {
    
    Ct5EventFacade ct5EventFacade = null;
    
    Ct5TestEvent ct5Te = null;
    
    @Override
    public void scoreTestEvent( TestEvent te, SimDescriptor sd, boolean skipVersionCheck) throws Exception
    {
        try
        {
            if( !te.getTestEventStatusType().getIsCompleteOrHigher() )
                throw new Exception( "TestEvent is not complete or higher. " );
                
            if( te.getResultXml()==null || te.getResultXml().isBlank() )
                setResultXmlInTestEvent(te, sd, skipVersionCheck );

            if( te.getResultXml()==null || te.getResultXml().isBlank() )
                throw new Exception( "Unable to create ResultXml" );
                        
            super.scoreTestEvent(te, sd, skipVersionCheck);
            
            if( te.getTestEventStatusType().getIsScoredOrHigherNoErrors()  )
            {
                if( ct5Te==null )
                {
                    if( ct5EventFacade==null )
                        ct5EventFacade = Ct5EventFacade.getInstance();
                    ct5Te = ct5EventFacade.getCt5TestEventForTestEventIdAndSurveyEventId(te.getTestEventId(), 0);
                    if( ct5Te==null )
                    {
                        LogService.logIt( "Ct5TestEvent not found. CT5DirectAvTestEventScorer.scoreTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) );
                        throw new ScoringException( "Ct5TestEvent not found. CT5DirectAvTestEventScorer.scoreTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
                    }
                }
                if( ct5Te!=null )
                {
                    ct5Te.setCt5TestEventStatusTypeId(TestEventStatusType.SCORED.getTestEventStatusTypeId() );
                    ct5EventFacade.saveCt5TestEvent(ct5Te);
                }
            }
            
        }
        catch( ScoringException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "CT5DirectAvTestEventScorer.scoreTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId() ) );            
            throw new ScoringException( "msg=" + e.getMessage() + ", CT5DirectTestEventScorer.scoreTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoreUtils.getExceptionPermanancy(e), te );
        }
    }

    
    public void setResultXmlInTestEvent(TestEvent te, SimDescriptor sd, boolean skipVersionCheck ) throws Exception
    {
        try
        {
            if( sd!=null && sd.getSimId()!=te.getSimId() )
            {
                LogService.logIt( "CT5DirectAvTestEventScorer.setResultXmlInTestEvent() sd.simId=" + sd.getSimId() + " does not match testEvent.simId=" + te.getSimId() + ", testEventId=" + te.getTestEventId() );
                sd=null;
            }
            
            if( sd!=null && sd.getSimVersionId()!=te.getSimVersionId() && !skipVersionCheck )
            {
                LogService.logIt( "CT5DirectAvTestEventScorer.setResultXmlInTestEvent() sd.simVersionId=" + sd.getSimVersionId() + " does not match testEvent.simVersionId=" + te.getSimVersionId() + ", testEventId=" + te.getTestEventId() + " testEvent.simId=" + te.getSimId() );
                sd=null;
            }
            
            if( sd==null )
            {
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                sd=eventFacade.getSimDescriptor(te.getSimId(), te.getSimVersionId(), true);
            }
            
            if( sd==null )
            {
                LogService.logIt( "SimDescriptor sd is null. CT5DirectAvTestEventScorer.setResultXmlInTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) );
                throw new ScoringException( "SimDescriptor sd is null. CT5DirectAvTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
            
            if( sd.getXml()==null || sd.getXml().isBlank() )
            {
                LogService.logIt( "SimDescriptor.xml is null or empty. SimDescriptorId=" + sd.getSimDescriptorId() + ", testEventId=" + (te==null ? "null" : te.getTestEventId()) );
                throw new ScoringException( "SimDescriptor.xml is null or empty. SimDescriptorId=" + sd.getSimDescriptorId() + ", CT5DirectTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
                 
            if( te.getUser()==null )
            {
                te.setUser( UserFacade.getInstance().getUser( te.getUserId()));
            }
            
            if( ct5EventFacade==null )
                ct5EventFacade = Ct5EventFacade.getInstance();    
            ct5Te = ct5EventFacade.getCt5TestEventForTestEventIdAndSurveyEventId(te.getTestEventId(), 0);
            if( ct5Te==null )
            {
                LogService.logIt( "Ct5TestEvent not found. CT5DirectAvTestEventScorer.setResultXmlInTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) );
                throw new ScoringException( "Ct5TestEvent not found. CT5DirectAvTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
            
            if( ct5Te.getCt5TestEventStatusTypeId()<TestEventStatusType.COMPLETED.getTestEventStatusTypeId() )
            {
                if( te.getTestEventStatusType().getIsDeactivated() )
                {
                    ct5Te.setCt5TestEventStatusTypeId(TestEventStatusType.DEACTIVATED.getTestEventStatusTypeId() );
                    ct5EventFacade.saveCt5TestEvent(ct5Te);
                    LogService.logIt("CT5DirectAvTestEventScorer.setResultXmlInTestEvent() TestEvent was deactivated so deactivating  ct5TestEvent. " );
                    return;
                }

                if( tk==null )
                {
                    if( eventFacade==null )
                        eventFacade=EventFacade.getInstance();
                    tk = eventFacade.getTestKey(te.getTestKeyId(), true );
                }
                
                
                // if there is a battery, there could be a battery time limit that has been exceeded.
                if( tk.getBatteryId()>0 )
                {
                    if( tk.getBattery()==null )
                    {
                        if( eventFacade==null )
                            eventFacade=EventFacade.getInstance();
                         tk.setBattery(eventFacade.getBattery( tk.getBatteryId()) );
                    }
                    
                    if( tk.getBattery().getTimeLimitSeconds()>0 )
                    {
                        if( te.getTestEventStatusType().getIsCompleteOrHigher() && te.getPercentComplete()>=100f && tk.getBatteryId()>0 )
                        {
                            ct5Te.setCt5TestEventStatusTypeId(TestEventStatusType.COMPLETED.getTestEventStatusTypeId() );
                            ct5Te.setPercentComplete(100);
                            ct5EventFacade.saveCt5TestEvent(ct5Te);
                            LogService.logIt("CT5DirectAvTestEventScorer.setResultXmlInTestEvent() TestEvent was marked complete - appears that battery time limit exceeeded, so marking Ct5TestEvent as complete to allow for partial scoring." );
                            // ok to continue;
                        }
                    } 
                }
                else
                {
                    LogService.logIt("CT5DirectAvTestEventScorer.setResultXmlInTestEvent() STERR Ct5TestEvent is not in complete or higher status. TestEvent.testEventStatusType=" + te.getTestEventStatusType().getKey() + " " + te.getTestEventStatusTypeId() + ", testEventId=" + te.getTestEventId() + ", ct5TestEventId=" + ct5Te.getCt5TestEventId() );
                }
            }
            
            ct5Te.setCt5ItemResponseList( ct5EventFacade.getCt5ItemResponsesForCt5Test( ct5Te.getCt5TestEventId() ));
            
            FileUploadFacade fuf = null;
            for( Ct5ItemResponse ir : ct5Te.getCt5ItemResponseList() )
            {
                if( ir.getUploadedUserFileId()>0 )
                {
                    if( fuf==null )
                        fuf = FileUploadFacade.getInstance();
                    ir.setUploadedUserFile(fuf.getUploadedUserFile(ir.getUploadedUserFileId(), true));
                }
            }
            
            SimJ simJ = JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() );
            
            if( simJ==null )
            {
                LogService.logIt( "SimDescriptor.xml did not parse to a SimJ object. simId=" + sd.getSimId() + " simVersionId=" + sd.getSimVersionId() + ", simDescriptorId=" + sd.getSimDescriptorId() + ", testEventId=" + (te==null ? "null" : te.getTestEventId()) );
                throw new ScoringException( "SimDescriptor.xml did not parse to a SimJ object. simId=" + sd.getSimId() + " simVersionId=" + sd.getSimVersionId() + ", simDescriptorId=" + sd.getSimDescriptorId() + ", CT5DirectTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
                            
            Clicflic cf = Ct5ResultXmlGenerator.createResultXmlObj(te, null, ct5Te, simJ, te.getUser() );
            
            if( cf==null )
            {
                LogService.logIt( "Unable to generate ResultXml for Ct5DirectTest. testEventId=" + (te==null ? "null" : te.getTestEventId()) );
                throw new ScoringException( "Unable to generate ResultXml for Ct5DirectTest. CT5DirectAvTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
            
            te.setResultXml( JaxbUtils.marshalImoResultXml(cf));
            
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            eventFacade.saveTestEvent(te);
        }
        catch( ScoringException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CT5DirectAvTestEventScorer.setResultXmlInTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) );
            throw e;
        }
    }
    

}
