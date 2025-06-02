/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.ct5.event;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.ct5.Ct5ItemType;
import com.tm2score.entity.ct5.event.Ct5ItemResponse;
import com.tm2score.entity.ct5.event.Ct5TestEvent;
import com.tm2score.entity.event.SurveyEvent;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class Ct5ResultXmlGenerator {
    
    public static SimJ resumeSimJ;
    
    
    
    public static Clicflic createResultXmlObj( TestEvent te, SurveyEvent se, Ct5TestEvent ct5Te, SimJ simJ, User user) throws Exception
    {
        // LogService.logIt( "Ct5ResultXmlGenerator.createResultXmlObj() START ");
        
        try
        {
            if( te==null && se==null  )
                throw new ScoringException( "TestEvent and SurveyEvent are both null ", ScoringException.NON_PERMANENT, null );

            if( te!=null && !te.getTestEventStatusType().getIsCompleteOrHigher() )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() TestEvent is not in a completed or higher status. " + te.getTestEventStatusTypeId() + ", testEventId=" + te.getTestEventId(), ScoringException.NON_PERMANENT, null );

            if( se!=null && !se.getTestEventStatusType().getIsCompleteOrHigher() )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() SurveyEvent is not in a completed or higher status. " + se.getSurveyEventStatusTypeId() + ", surveyEventId=" + se.getSurveyEventId(), ScoringException.NON_PERMANENT, null );

            if( ct5Te==null  )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() Ct5TestEvent is null ", ScoringException.NON_PERMANENT, null );

            if( !ct5Te.getTestEventStatusType().getIsCompleteOrHigher()  )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() Ct5TestEvent.ct5TestEventStatusTypeId is not in completed or higher status. " + ct5Te.getCt5TestEventStatusTypeId() + ", testEventId=" + te.getTestEventId(), ScoringException.NON_PERMANENT, null );

            if( te!=null && ct5Te.getTestEventId()!=te.getTestEventId()  )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() Ct5TestEvent.testEventId=" + ct5Te.getTestEventId() + " does not match testEvent.testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()), ScoringException.NON_PERMANENT, null );

            if( se!=null && ct5Te.getSurveyEventId()!=se.getSurveyEventId()  )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() Ct5TestEvent.surveyEventId=" + ct5Te.getSurveyEventId() + " does not match surveyEvent.surveyEventId=" + se.getSurveyEventId(), ScoringException.NON_PERMANENT, null );


            if( ct5Te.getCt5ItemResponseList()==null  )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() Ct5TestEvent.ct5ItemResponseList is null. testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()), ScoringException.NON_PERMANENT, null );

            if( ct5Te.getCt5ItemResponseList().isEmpty()  )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() Ct5TestEvent.ct5ItemResponseList does not contain any Ct5ItemResponses. testEventId=" + te.getTestEventId(), ScoringException.NON_PERMANENT, null );

            if( simJ==null )
                throw new ScoringException( "Ct5ResultXmlGenerator.createResultXmlObj() SimJ is null ", ScoringException.NON_PERMANENT, null );

            Clicflic cf = new Clicflic();

            Clicflic.Event cfe = new Clicflic.Event();

            if( te!=null )
            {
                cfe.setId( te.getTestEventIdEncrypted());
                cfe.setPctcomplete( te.getPercentComplete() );
                cfe.setXref(te.getExtRef() );
            }
            else if( se!=null )
            {
                cfe.setId( se.getSurveyEventIdEncrypted() );
                cfe.setPctcomplete( se.getPercentComplete() );
            }

            cfe.setTtime( ct5Te.getTotalTestTimeSecs() );
            cfe.setMajver( simJ.getMajorversionid() );
            cfe.setMinver( simJ.getSimverbldnum());
            cf.setEvent(cfe);

            // record timeout.
            if( ct5Te.getTimeout()>0 )
                cfe.setTmout(1);
            
            Clicflic.User u = new Clicflic.User();

            if( user!=null )
            {
                u.setFn( user.getFirstName());
                u.setLn( user.getLastName());
                u.setEm( user.getEmail());
                u.setPh( user.getMobilePhone());
            }
            cf.setUser(u);

            Clicflic.History ch = new Clicflic.History();
            cf.setHistory(ch);

            Clicflic.History.Intn cfi;
            SimJ.Intn sji;

            Collections.sort( ct5Te.getCt5ItemResponseList() );

            for( Ct5ItemResponse ir : ct5Te.getCt5ItemResponseList() )
            {
                // skip incomplete item responses.
                if( !ir.getIsComplete() )
                    continue;
                                
                cfi = new Clicflic.History.Intn();
                sji = getSimJIntn( simJ, ir.getCt5ItemId() );

                // Check for resume item.
                if( sji==null && ir.getCt5ItemId()>0 )
                    sji = Ct5ResumeUtils.getResumeIntn( ir.getCt5ItemId() );
                
                if( sji==null )
                {
                    LogService.logIt( "Ct5ResultXmlGenerator.createResultXmlObj() NON-FATAL SimJ.intn for Ct5ItemId=" + ir.getCt5ItemId() + " not found in SimJ. Assuming this is because of a forced version mismatch and ingnoring.");
                    continue;
                }

                // if item set to ignore items that are skipped.
                if( ir.getResponseCode()==-1 )
                {
                    // Item specifies skip
                    if( sji.getCt5Int8()==1 )
                    {
                        // LogService.logIt( "Ct5ResultXmlGenerator.createResultXmlObj() Skipping entry for Ct5ItemId=" + ir.getCt5ItemId() + " because item indicates skipped and item.ct5Int8 indicates ignore if skipped.");
                        continue;
                    }
                    
                    // SimCompetency specifies skip
                    if( sji.getCt5Topicid()>0 )
                    {
                        SimJ.Simcompetency sc = getSimCompetencyForItem( sji, simJ );
                        
                        if( sc!=null && sc.getIgnoreunanswereditems()>=1 )
                        {
                            // LogService.logIt( "Ct5ResultXmlGenerator.createResultXmlObj() Skipping entry for Ct5ItemId=" + ir.getCt5ItemId() + " because itemresponse indicates skipped and SimCompetency indicates ignore if skipped.");
                            continue;                            
                        }
                    }
                }                
                
                cfi.setU( sji.getUniqueid() );
                cfi.setId( sji.getId() );
                cfi.setNs( sji.getSeq() );
                cfi.setPass(1);
                cfi.setChcclk(1);
                cfi.setSq( ir.getResponseSeq() );

                cfi.setCx( ir.getShowCount() );
                cfi.setCk( ir.getResponseCount() );
                cfi.setAf( ir.getAccessibleForm() );

                int crct = 0;
                try
                {
                    crct=ir.getResponseCode();                
                }
                catch( NumberFormatException e )
                {}

                cfi.setCrct( crct );
                float ct = 0;
                //try
                //{
                    ct = (float) ir.getTotalItemTime();
                    // ct = ir.getTimeMillisecs()/1000;
                //}
                //catch( Exception e )
                //{
                //    LogService.logIt( e, "Ct5ResultXmlGenerator.createResultXmlObj() NON-FATAL error converting to seconds. " + ir.getTimeMillisecs() );
                //}
                cfi.setCt( ct );
                Ct5ItemType ct5ItemType = Ct5ItemType.getValue(sji.getCt5Itemtypeid());
                cfi.setSs( ct5ItemType.getSelectedSubnodeSeq(ir,sji) ); // only really used in mult choice

                cfi.setValue( ct5ItemType.getResultXmlIntnValue(ir,sji));
                
                ch.getIntnOrClip().add(cfi);
            }

            return cf;
        }
        catch( ScoringException e )
        {
            LogService.logIt( "Ct5ResultXmlGenerator.createResultXmlObj() " + e.toString() + ", testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()));
            throw e;
        }        
        catch( Exception e )
        {
            LogService.logIt( e, "Ct5ResultXmlGenerator.createResultXmlObj() testEventId=" + (te==null ? "null" : te.getTestEventId()) + ", surveyEventId=" + (se==null ? "null" : se.getSurveyEventId()));
            throw e;
        }
    }
    
    private static SimJ.Simcompetency getSimCompetencyForItem( SimJ.Intn sji, SimJ simJ )
    {
        if( sji==null || simJ==null )
            return null;
        
        for( SimJ.Simcompetency sc : simJ.getSimcompetency() )
        {
            if( sji.getSimcompetencyid()==sc.getId() )
                return sc;
            
            if( sji.getRadiobuttongroup()!=null )
            {
                for( SimJ.Intn.Radiobuttongroup rbg : sji.getRadiobuttongroup() )
                {
                    if( rbg.getSimcompetencyid()==sc.getId() )
                        return sc;
                }
            }
            
            for( SimJ.Intn.Intnitem ii : sji.getIntnitem() )
            {
                if( ii.getSimcompetencyid()==sc.getId() )
                    return sc;
            }
        }
        
        return null;
    }
    
    private static SimJ.Intn getSimJIntn( SimJ simJ, int ct5ItemId )
    {
        if( simJ==null || simJ.getIntn()==null )
            return null;
        
        for( SimJ.Intn n : simJ.getIntn() )
        {
            if( n.getCt5Itemid()==ct5ItemId )
                return n;
        }
        
        return null;
    }
}
