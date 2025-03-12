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
import com.tm2score.entity.event.SurveyEvent;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import com.tm2score.xml.JaxbUtils;
import java.util.Date;

/**
 *
 * @author Mike
 */
public class CT5DirectTestEventScorer extends StandardTestEventScorer implements TestEventScorer {
    
    Ct5EventFacade ct5EventFacade = null;
    
    Ct5TestEvent ct5Te = null;
    
    @Override
    public void scoreTestEvent( TestEvent tex, SimDescriptor sd, boolean skipVersionCheck) throws Exception
    {
        try
        {
            if( !tex.getTestEventStatusType().getIsCompleteOrHigher() )
                throw new Exception( "TestEvent is not complete or higher. " );
                
            if( tex.getResultXml()==null || tex.getResultXml().isBlank() )
                setResultXmlInTestEvent(tex, null, sd, skipVersionCheck );

            if( tex.getResultXml()==null || tex.getResultXml().isBlank() )
                throw new Exception( "Unable to create ResultXml" );
                        
            super.scoreTestEvent(tex, sd, skipVersionCheck);
            
            if( te!=null && te.getTestEventStatusType().getIsScoredOrHigherNoErrors()  )
            {
                if( ct5Te==null )
                {
                    if( ct5EventFacade==null )
                        ct5EventFacade = Ct5EventFacade.getInstance();
                    ct5Te = ct5EventFacade.getCt5TestEventForTestEventIdAndSurveyEventId(te.getTestEventId(), 0);
                    if( ct5Te==null )
                    {
                        LogService.logIt( "NonFatal. Ct5TestEvent not found. This could be OK as long as there is Result XML. CT5DirectTestEventScorer.scoreTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) );
                        // throw new ScoringException( "Ct5TestEvent not found. CT5DirectTestEventScorer.scoreTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
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
            LogService.logIt(e, "CT5DirectTestEventScorer.scoreTestEvent() testEventId=" + (tex==null ? "null" : tex.getTestEventId() ) );            
            throw new ScoringException( "msg=" + e.getMessage() + ", CT5DirectTestEventScorer.scoreTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoreUtils.getExceptionPermanancy(e) , tex );
        }
    }

    
     public void setResultXmlInTestEvent(TestEvent te, SurveyEvent se, SimDescriptor sd, boolean skipVersionCheck) throws Exception
    {
        try
        {
            long simId=0;
            int simVersionId=0;
            
            if( te!=null )
            {
                simId = te.getSimId();
                simVersionId = te.getSimVersionId();                
            }
            else if( se!=null )
            {
                simId = se.getSimId();
                simVersionId = se.getSimVersionId();
            }

            //LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() BBB.1 " );
            
            if( sd!=null && sd.getSimId()!=simId )
            {
                LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() sd.simId=" + sd.getSimId() + " does not match testOrSurveyEvent.simId=" + simId + ", testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
                sd=null;
            }
            
            if( sd!=null && sd.getSimVersionId()!=simVersionId && !skipVersionCheck )
            {
                LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() sd.simVersionId=" + sd.getSimVersionId() + " does not match testOrSurveyEvent.simVersionId=" + simVersionId + ", testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
                sd=null;
            }
            
            if( sd==null )
            {
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                sd=eventFacade.getSimDescriptor(simId, simVersionId, true);
            }
            
            if( sd==null )
            {
                LogService.logIt("SimDescriptor sd is null. CT5DirectTestEventScorer.setResultXmlInTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
                throw new ScoringException( "SimDescriptor sd is null. CT5DirectTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }

            //LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() BBB.2 " );
            
            if( sd.getXml()==null || sd.getXml().isBlank() )
            {
                LogService.logIt("SimDescriptor.xml is null or empty. SimDescriptorId=" + sd.getSimDescriptorId() + ", testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
                throw new ScoringException( "SimDescriptor.xml is null or empty. SimDescriptorId=" + sd.getSimDescriptorId() + ", CT5DirectTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }

            //User user = null;
                        
            if( te!=null && te.getUser()==null )
                te.setUser( UserFacade.getInstance().getUser( te.getUserId()));
            
            if( te!=null )
                user = te.getUser();
            else if( se!=null )
                user = UserFacade.getInstance().getUser( se.getUserId());
            
            if( ct5EventFacade==null )
                ct5EventFacade = Ct5EventFacade.getInstance();
            ct5Te = ct5EventFacade.getCt5TestEventForTestEventIdAndSurveyEventId(te==null ? 0 : te.getTestEventId(), se==null ? 0 : se.getSurveyEventId());
            if( ct5Te==null )
            {
                LogService.logIt("Ct5TestEvent not found. CT5DirectTestEventScorer.setResultXmlInTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
                throw new ScoringException( "Ct5TestEvent not found. CT5DirectTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
            
            if( ct5Te.getCt5TestEventStatusTypeId()<TestEventStatusType.COMPLETED.getTestEventStatusTypeId() && te!=null )
            {
                if( te.getTestEventStatusType().getIsDeactivated() )
                {
                    ct5Te.setCt5TestEventStatusTypeId(TestEventStatusType.DEACTIVATED.getTestEventStatusTypeId() );
                    ct5EventFacade.saveCt5TestEvent(ct5Te);
                    LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() TestEvent was deactivated so deactivating  ct5TestEvent. " );
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
                            LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() TestEvent was marked complete - appears that battery time limit exceeeded, so marking Ct5TestEvent as complete to allow for partial scoring." );
                        }
                    } 
                }
                else
                {
                    // test key expired, force it to be complete.
                    if( te.getTestEventStatusType().getIsCompleteOrHigher() && tk.getExpireDate()!=null && tk.getExpireDate().before(new Date()) )
                    {
                        ct5Te.setCt5TestEventStatusTypeId(TestEventStatusType.COMPLETED.getTestEventStatusTypeId() );
                        ct5Te.setPercentComplete(100);
                        ct5EventFacade.saveCt5TestEvent(ct5Te);
                        LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() TestKey is complete past expiration date, and TestEvent was marked complete - appears that so marking partial Ct5TestEvent as complete to allow for partial scoring." );
                    }
                    else
                        LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() STERR Ct5TestEvent is not in complete or higher status. TestEvent.testEventStatusType=" + te.getTestEventStatusType().getKey() + " " + te.getTestEventStatusTypeId() + ", testEventId=" + te.getTestEventId() + ", ct5TestEventId=" + ct5Te.getCt5TestEventId() );
                }
                
            }
            
            //LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() BBB.3 " );
            
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
            
            //LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() BBB.4 " );
            SimJ simJ = JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() );
            
            //LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() BBB.5 " );
            if( simJ==null )
            {
                LogService.logIt("SimDescriptor.xml did not parse to a SimJ object. simId=" + sd.getSimId() + " simVersionId=" + sd.getSimVersionId() + ", simDescriptorId=" + sd.getSimDescriptorId() + ", testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
                throw new ScoringException( "SimDescriptor.xml did not parse to a SimJ object. simId=" + sd.getSimId() + " simVersionId=" + sd.getSimVersionId() + ", simDescriptorId=" + sd.getSimDescriptorId() + ", CT5DirectTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
                            
            Clicflic cf = Ct5ResultXmlGenerator.createResultXmlObj(te, se, ct5Te, simJ, user );
            
            //LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() BBB.6 " );
            if( cf==null )
            {
                LogService.logIt("Unable to generate ResultXml for Ct5DirectTest. testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
                throw new ScoringException( "Unable to generate ResultXml for Ct5DirectTest. CT5DirectTestEventScorer.setResultXmlInTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoringException.PERMANENT , te );
            }
            
            //LogService.logIt("CT5DirectTestEventScorer.setResultXmlInTestEvent() BBB.7 " );
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            if( te!=null )
            {
                te.setResultXml( JaxbUtils.marshalImoResultXml(cf));
                eventFacade.saveTestEvent(te);
            }
            
            if( se!=null )
            {
                se.setResultXml( JaxbUtils.marshalImoResultXml(cf));
                eventFacade.saveSurveyEvent(se);                
            }
            
        }
        catch( ScoringException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "CT5DirectTestEventScorer.setResultXmlInTestEvent() testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()) );
            throw e;
        }
    }
    

}
