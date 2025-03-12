/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.SurveyEvent;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.ScoringException;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import com.tm2score.xml.JaxbUtils;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Mike
 */
public class StandardSurveyEventScorer extends BaseTestEventScorer implements TestEventScorer
{

    @Override
    public void scoreTestEvent( TestEvent te, SimDescriptor sd, boolean skipVersionCheck) throws Exception
    {
       try
       {
            SurveyEvent surveyEvent = te.getSurveyEvent();

            // LogService.logIt("StandardSurveyEventScorer.scoreTestEvent() starting process. TestEventId=" + te.getTestEventId() + ", surveyEventId=" + (te.getSurveyEvent()==null ? "null" : te.getSurveyEvent().getSurveyEventId()) );

            if( te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED.getTestEventStatusTypeId() &&
                te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
                throw new ScoringException( "TestEvent is not correct status type. Expecting completed or completed pending external. ", ScoringException.NON_PERMANENT, te );

            if( eventFacade == null ) 
                eventFacade = EventFacade.getInstance();

            if( te.getSimDescriptor()==null )
                te.setSimDescriptor(sd);

            te.setSimId( sd.getSimId() );
            te.setSimVersionId( sd.getSimVersionId() );

            if( te.getSimDescriptor() == null )
                throw new ScoringException( "Cannot find SimDescriptor for SimDescriptorId=" + te.getProduct().getSimDescriptorId(), ScoringException.PERMANENT, te );

            if( te.getSimDescriptor().getXml() == null || te.getSimDescriptor().getXml().isEmpty() )
                throw new ScoringException( "SimDescriptor XML is empty. simDescriptorId=" + te.getProduct().getSimDescriptorId(), sd.getSimDescriptorId()>0 ? ScoringException.PERMANENT : ScoringException.NON_PERMANENT, te );

            te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( te.getSimDescriptor().getXml() ) );

            if( te.getSimXmlObj().getSimid() != te.getSimId() || te.getSimXmlObj().getSimverid() != te.getSimVersionId() )
                throw new ScoringException( "Cannot score TestEvent because mismatch between simIds and/or versions between TestEvent and SimDescriptor: TestEvent: " + te.getSimId() + ", v" + te.getSimVersionId() + ", SimDescriptor: " + te.getSimXmlObj().getSimid() + " v" + te.getSimXmlObj().getSimverid(), sd.getSimDescriptorId()>0 ? ScoringException.PERMANENT : ScoringException.NON_PERMANENT, te );

            // Get the score xml and it's object.
            if( te.getResultXml() == null || te.getResultXml().isEmpty() )
                throw new ScoringException( "Result XML is empty. ", ScoringException.PERMANENT, te );

            te.setResultXmlObj( JaxbUtils.ummarshalImoResultXml( te.getResultXml() ) );

            // List<SimJ.Simcompetency> simCompLst =

            // te.setTotalTestTime( te.getResultXmlObj().getEvent().getTtime() );

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // AT THIS POINT, IT SEEMS LIKE WE SHOULD BE ABLE TO SCORE!
            ///////////////////////////////////////////////////////////////////////////////////////////////
            initForSurvey();
            
            // LogService.logIt("StandardSurveyEventScorer.scoreTestEvent() starting to score SurveyEvent " + surveyEvent.toString() + ", simXmlObj contains " + te.getSimXmlObj().getSimlet().size() + " simlets." );

            if( te.getSimXmlObj().getSimcompetency() == null || te.getSimXmlObj().getSimcompetency().isEmpty() )
            {
                LogService.logIt( "StandardSurveyEventScorer.scoreTestEvent() Survey contains no Sim Competencies, wrapping scoring." );
                surveyEvent.setSurveyEventStatusTypeId( TestEventStatusType.SCORED.getTestEventStatusTypeId() );

                eventFacade.saveSurveyEvent(surveyEvent);

                return;
            }

            // Indicate scoring has started.
            surveyEvent.setSurveyEventStatusTypeId(TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() );
            te.setTestEventStatusTypeId(surveyEvent.getSurveyEventStatusTypeId());

            eventFacade.saveSurveyEvent(surveyEvent);
                        
            if( te.getProduct()==null )
                te.setProduct( eventFacade.getProduct( te.getProductId() ));
                                    
            if( te.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                
                te.setOrg( userFacade.getOrg( te.getOrgId() ) );
            }
            
            
            // OK initialize all the score data.
            te.initScoreAndResponseLists( true );

            //for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
            //    scs.setReportData(reportData);
            
            // float ovrRawScr = 0;
            // float ovrScaledScr = 0;

            // float totalWeights = 0;

            // boolean useRankValuesAsWeights = te.getSimXmlObj().getOverallscorecalctype()==OverallScoreCalcType.SIMCOMPETENCYRANKS.getOverallScoreCalcTypeId(); //  .getUserankvaluesasweights()==1;

            // boolean hasWeights = false;

            scoreFormatTypeId = te.getSimXmlObj().getScoreformat();

            scoreColorSchemeType = ScoreColorSchemeType.getValue( te.getSimXmlObj().getScorecolorscheme() );

            // OverallScoreCalcType overallScoreCalcType = OverallScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype() );

            //boolean pendingExternalScores = false;

            // now score each SimCompetency
            for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
            {
                // LogService.logIt( "StandardSurveyEventScorer.scoreTestEvent() Calculating score for " + scs.toString() );

                scs.setScoreFormatTypeId(scoreFormatTypeId);
                scs.setScoreColorSchemeTypeId( scoreColorSchemeType.getScoreColorSchemeTypeId() );

                scs.init(); 
                                
                // Must come before collecting interview questions.
                scs.calculateScore( te.getSimXmlObj().getUsesmltcompcvrg() );
            }

            // LogService.logIt( "StandardSurveyEventScorer.scoreTestEvent()  surveyEventId=" + surveyEvent.getSurveyEventId() );

            ItemResponse itemResponse;
            ItemResponse ir2;

            // IactnResp ir;
            
            Set<String> savedItemResponseIdentifiers = new HashSet<>();

            // now create the Item Response records for item analysis down the road.
            for( ScorableResponse sr : te.getAutoScorableResponseList() )
            {
                if( !sr.saveAsItemResponse() )
                    continue;

                if( sr.isAutoScorable() )
                {
                    itemResponse = new ItemResponse( sr );

                    itemResponse.setSurveyEventId( surveyEvent.getSurveyEventId());

                    sr.populateItemResponse(itemResponse);

                    if( itemResponse.getIdentifier()!=null && !itemResponse.getIdentifier().isBlank() && savedItemResponseIdentifiers.contains( itemResponse.getIdentifier() ) )
                        continue;
                    
                    itemResponse.setSimId( te.getSimId() );
                    itemResponse.setSimVersionId( te.getSimVersionId() );

                    ir2 = getMatchingExistingItemResponse( itemResponse );                    
                    if( ir2!=null && ir2.getItemResponseId()>0 )
                        itemResponse.setItemResponseId( ir2.getItemResponseId() );
                    
                    eventFacade.saveItemResponse(itemResponse);

                    // add identifier to saved list.
                    if( itemResponse.getIdentifier()!=null && !itemResponse.getIdentifier().isBlank() )
                        savedItemResponseIdentifiers.add( itemResponse.getIdentifier() );                    
                }

                /*
                for( IactnItemResp iir : sr.getAllScorableIntItemResponses() )
                {
                    itemResponse = new ItemResponse( iir );

                    itemResponse.setSurveyEventId( surveyEvent.getSurveyEventId());

                    iir.populateItemResponse(itemResponse);

                    // prevents the same item response identifier from being saved at item and interaction item level.
                    if( itemResponse.getIdentifier()!=null && !itemResponse.getIdentifier().isBlank() && savedItemResponseIdentifiers.contains( itemResponse.getIdentifier() ) )
                        continue;
                    
                    itemResponse.setSimId( te.getSimId() );
                    itemResponse.setSimVersionId( te.getSimVersionId() );

                    ir2 = getMatchingExistingItemResponse( itemResponse );                    
                    if( ir2!=null && ir2.getItemResponseId()>0 )
                        itemResponse.setItemResponseId( ir2.getItemResponseId() );
                    
                    eventFacade.saveItemResponse(itemResponse);
                }
                */
            }


            surveyEvent.setSurveyEventStatusTypeId( TestEventStatusType.SCORED.getTestEventStatusTypeId() );
            te.setTestEventStatusTypeId(surveyEvent.getSurveyEventStatusTypeId());

            eventFacade.saveSurveyEvent(surveyEvent);

            eventFacade.reorderItemResponses( 0,surveyEvent.getSurveyEventId() );

            LogService.logIt( "StandardSurveyEventScorer.scoreTestEvent() COMPLETED SCORING " + surveyEvent.toString() + " " );
       }

       catch( ScoringException e )
       {
           LogService.logIt(e, "StandardSurveyEventScorer.scoreTestEvent() "  + te.toString() );
           throw e;
       }
       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt(e, "StandardSurveyEventScorer.scoreTestEvent() testEventId="  + (te==null ? "testEvent is null" : te.getTestEventId() + " surveyEventId=" + (te.getSurveyEvent()==null ? "null" : te.getSurveyEvent().getSurveyEventId())) );
           throw new ScoringException( e.getMessage() + "StandardSurveyEventScorer.scoreTestEvent() " , ScoreUtils.getExceptionPermanancy(e) , te );
       }
    }

    
    

    public void initForSurvey() throws Exception
    {
        if( te==null || te.getSurveyEvent()==null )
            return;
        
        if( eventFacade == null )
            eventFacade = EventFacade.getInstance();
        
        if( eventFacade==null )
        {
            LogService.logIt( "StandardSurveyEventScorer.initForSurvey() eventFacade is null! ???? te.surveyEventId=" + te.getSurveyEvent().getSurveyEventId() );
            throw new Exception( "StandardSurveyEventScorer.initForSurvey() eventFacade is null!" );
        }
        
        oldItemResponseList = eventFacade.getItemResponsesForSurveyEvent( te.getSurveyEvent().getSurveyEventId() );
        oldTestEventScoreList = null; // eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true );        
    }
    



}
