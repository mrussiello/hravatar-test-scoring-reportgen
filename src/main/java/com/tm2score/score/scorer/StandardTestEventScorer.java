/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.entity.event.Percentile;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.event.NormFacade;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.score.SimCompetencyGroup;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import com.tm2score.sim.OverallRawScoreCalcType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.sim.SimCompetencyRawScoreCalcType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class StandardTestEventScorer extends BaseTestEventScorer implements TestEventScorer
{
    
    
    @Override
    public void scoreTestEvent( TestEvent testEvent, SimDescriptor simDescriptor, boolean skipVersionCheck) throws Exception
    {
       try
       {
           te = testEvent;
           sd = simDescriptor;

            LogService.logIt( "StandardTestEventScorer.scoreTestEvent() starting process. TestEvent " + te.toString() + ", validItemsCanHaveZeroMaxPoints=" + validItemsCanHaveZeroMaxPoints );

            initForScoring(skipVersionCheck);

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // AT THIS POINT, IT SEEMS LIKE WE SHOULD BE ABLE TO SCORE!
            ///////////////////////////////////////////////////////////////////////////////////////////////

            setReportLocale();
            
            initTestEvent();
                 
            // if( ScoreManager.DEBUG_SCORING )
               LogService.logIt( "StandardTestEventScorer.score() te.stdHraScoring=" + te.getStdHraScoring() + ", validItemsCanHaveZeroMaxPoints=" + validItemsCanHaveZeroMaxPoints );

            // float ovrRawScr = 0;
            // float ovrScaledScr = 0;

            // float totalWeights = 0;

            // boolean useRankValuesAsWeights = te.getSimXmlObj().getOverallscorecalctype()==OverallScoreCalcType.SIMCOMPETENCYRANKS.getOverallScoreCalcTypeId(); //  .getUserankvaluesasweights()==1;

            // boolean hasWeights = false;
            scoreSimCompetencies();

            if( ScoreManager.DEBUG_SCORING )
               LogService.logIt( "StandardTestEventScorer.scoreTestEvent() Ready to calculate overall score. ovrRawScr=" + ovrRawScr + ", totalWeights=" + totalWeights + ", testEventId=" + te.getTestEventId() + ", pendingExternalScores=" + pendingExternalScores );

            if( pendingExternalScores )
            {
                scrSum.append( "Pending External Scores. \n" );
                te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
                LogService.logIt( "StandardTestEventScorer.scoreTestEvent() EXITING - Test Event is Pending External Scores. TestEventId=" + te.getTestEventId() );
                // LogService.logIt( scrSum.toString() );
                return;
            }
            
            calculateMetaScores();

            calculateOverallScores();
            
            // LogService.logIt( scrSum.toString() );   
            
            int counter = 1;

            setOverallScoreAndPercentile( counter );

            counter = setCompetencyTestEventScores(counter);

            counter = setCompetencyGroupTestEventScores(counter);

            setItemResponses(te.getAutoScorableResponseList());
            
            finalizeRemoteProctorScoring();
            
            finalizeScore();        

            if( ScoreManager.DEBUG_SCORING )            
                LogService.logIt( "StandardTestEventScorer.scoreTestEvent() COMPLETED SCORING te.scoreFormatTypeId=" + te.getScoreFormatTypeId() + ", teId=" + te.getTestEventId() );
       }

       catch( ScoringException e )
       {
           LogService.logIt( "StandardTestEventScorer.scoreTestEvent() "  + te.toString() + "\n" + (scrSum!=null ? scrSum.toString() : "" ) );
           throw e;
       }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
            LogService.logIt( e, "StandardTestEventScorer.scoreTestEvent() XXX.1 "  + (te==null ? "testEvent id null" : te.toString()) );
            throw new ScoringException( "msg=" + e.getMessage() + ", StandardTestEventScorer.scoreTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoreUtils.getExceptionPermanancy(e), te );
       }
    }
    
    
    
    @Override
    public void recalculatePercentilesForTestEvent( TestEvent testEvent, SimDescriptor simDescriptor ) throws Exception
    {
       try
       {
            te = testEvent;

            // LogService.logIt( "StandardTestEventScorer.recalculatePercentilesForTestEvent() starting process. TestEvent " + te.toString() );

            if( te.getTestEventStatusTypeId() < TestEventStatusType.SCORED.getTestEventStatusTypeId() )
                throw new ScoringException( "TestEvent is not correct status type. Expecting SCORED. ", ScoringException.NON_PERMANENT, te );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            if( te.getProduct()==null )
                te.setProduct( eventFacade.getProduct(te.getProductId()));
            
            if( normFacade == null )
               normFacade = NormFacade.getInstance();

            // MJR 01092018 eventFacade.clearPercentileEntriesForEvent(te);

            TestEventScore otes = te.getOverallTestEventScore();

            String frcCountry = null;

            if( tk!=null && tk.getFrcCountry()!=null && !tk.getFrcCountry().isEmpty() )
                frcCountry = tk.getFrcCountry();

            else if( te.getSuborg()!=null && te.getSuborg().getPercentileCountry()!=null && !te.getSuborg().getPercentileCountry().isEmpty() )
                frcCountry = te.getSuborg().getPercentileCountry();

            else if( te.getOrg()!=null && te.getOrg().getPercentileCountry()!=null && !te.getOrg().getPercentileCountry().isEmpty() )
                frcCountry = te.getOrg().getPercentileCountry();

            String cc = te.getIpCountry();

            if( frcCountry != null && !frcCountry.isEmpty() )
                cc = frcCountry;

            
            Map<String,Object> norm = getPercentile(te.getProductId(), te.getProduct().getIntParam24(), te, otes, te.getOrgId(), cc);
            
            float percentile = norm==null ? -1 : ((Float)norm.get("percentile")).floatValue();
            int pcount = norm==null ? 0 : ((Integer)norm.get("count")).intValue();
                        
            te.setOverallPercentile( percentile );
            te.setOverallPercentileCount( pcount );
            otes.setPercentile( percentile );
            otes.setOverallPercentileCount( pcount );

            if( !otes.getHasValidOverallNorm() && otes.getHasValidOverallZScoreNorm() && te.getProduct()!=null && te.getProduct().getConsumerProductType().getIsJobSpecific() )
            {
                LogService.logIt( "StandardTestEventScorer.recalculatePercentilesForTestEvent() Setting TestEvent.overallPercentile to ZScore Percentile. TestEventId=" + te.getTestEventId() );
                te.setOverallPercentile( otes.getOverallZScorePercentile() );
                te.setOverallPercentileCount( 0 );            
                otes.setAccountPercentile( -1 );
                te.setAccountPercentile( -1 );
                te.setAccountPercentileCount( 0 );
                otes.setAccountPercentileCount( 0 );
                otes.setCountryPercentile( -1 );
                te.setCountryPercentile( -1 );
                te.setCountryPercentileCount( 0 );
                otes.setCountryPercentileCount( 0 );
            }
            
            // has a valid count-based norm.
            else
            {

                //String frcCountry = null;

                //if( tk!=null && tk.getFrcCountry()!=null && !tk.getFrcCountry().isEmpty() )
                //    frcCountry = tk.getFrcCountry();

                //else if( te.getSuborg()!=null && te.getSuborg().getPercentileCountry()!=null && !te.getSuborg().getPercentileCountry().isEmpty() )
                //    frcCountry = te.getSuborg().getPercentileCountry();

                //else if( te.getOrg()!=null && te.getOrg().getPercentileCountry()!=null && !te.getOrg().getPercentileCountry().isEmpty() )
                //    frcCountry = te.getOrg().getPercentileCountry();

                // Org Percentile
                //norm = getPercentile(te.getProductId(), te, otes, te.getOrgId(), null);

                percentile = norm==null ? -1 : ((Float)norm.get("percentileorg")).floatValue();
                pcount = norm==null ? 0 : ((Integer)norm.get("countorg")).intValue();

                otes.setAccountPercentile( percentile );
                te.setAccountPercentile( percentile );
                te.setAccountPercentileCount( pcount );
                otes.setAccountPercentileCount( pcount );

                // Country Percentile
                //norm = cc == null || cc.length()==0 ? null : getPercentile(te.getProductId(), te, otes, 0, cc);

                //if( norm==null )
                //{
                //    norm = new HashMap<>();
               //     norm.put( "percentile" , new Float(-1) );
               //    norm.put( "count" , new Integer(0)) ;
               //}

                percentile = norm==null ? -1 : ((Float)norm.get("percentilecc")).floatValue();
                pcount = norm==null ? 0 : ((Integer)norm.get("countcc")).intValue();

                otes.setCountryPercentile( percentile );
                te.setCountryPercentile( percentile );
                te.setCountryPercentileCount( pcount );
                otes.setCountryPercentileCount( pcount );
                te.setPercentileCountry(cc);
            }
            
            eventFacade.saveTestEventScore(otes);

            Thread.sleep( 100 );

            if( otes.getScore()<0 )
                te.setExcludeFmNorms( 1 );

            eventFacade.saveTestEvent( te );

            Thread.sleep( 100 );

            Percentile pctObj;

            // ALWAYS DO THIS AFTER SAVING TestEventScore
            if( te.getExcludeFmNorms()==0 )
            {
                pctObj = createPercentileForScore(tk, te, otes );

                if( pctObj != null )
                    eventFacade.savePercentile(pctObj);

            }

            for( TestEventScore tes : te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
            {
                if( tes.getScore()>= 0 && te.getProduct().getIntParam7()==1 )
                {
                    norm = getPercentile(te.getProductId(), te.getProduct().getIntParam24(), te, tes, te.getOrgId(), cc );
                    
                    percentile = norm==null ? -1 : ((Float)norm.get("percentile"));
                    pcount = norm==null ? 0 : ((Integer)norm.get("count"));
                    
                    tes.setPercentile( percentile );
                    tes.setOverallPercentileCount( pcount );

                    if( percentile<0 )
                    {
                        tes.setAccountPercentile( -1 );
                        tes.setAccountPercentileCount( 0 ); 
                        tes.setCountryPercentile( -1 );
                        tes.setCountryPercentileCount( 0 );
                    }
                    
                    else
                    {
                        // norm = getPercentile(te.getProductId(), te, tes, te.getOrgId(), null);

                        percentile = norm==null ? -1 : ((Float)norm.get("percentileorg")).floatValue();
                        pcount = norm==null ? 0 : ((Integer)norm.get("countorg")).intValue();
                        tes.setAccountPercentile( percentile );
                        tes.setAccountPercentileCount( pcount );

                        //norm = cc == null || cc.length()==0 ? null : getPercentile(te.getProductId(), te, tes, 0, cc);

                        //if( norm==null )
                        //{
                        //    norm = new HashMap<>();
                       //     norm.put( "percentile" , new Float(-1) );
                        //    norm.put( "count" , new Integer(0)) ;
                        //}

                        percentile = norm==null ? -1 : ((Float)norm.get("percentilecc")).floatValue();
                        pcount = norm==null ? 0 : ((Integer)norm.get("countcc")).intValue();

                        tes.setCountryPercentile( percentile );
                        tes.setCountryPercentileCount( pcount );
                        tes.setPercentileCountry(cc);
                    }
                    
                    eventFacade.saveTestEventScore(tes);

                    Thread.sleep( 100 );

                    // ALWAYS DO THIS AFTER SAVING TestEventScore
                    if( te.getExcludeFmNorms()==0 )
                    {
                        pctObj = createPercentileForScore(tk, te, tes );

                        if( pctObj != null )
                            eventFacade.savePercentile(pctObj);
                    }
                }
            }

            eventFacade.saveTestEvent(te);

            // LogService.logIt( "StandardTestEventScorer.recalculatePercentilesForTestEvent() COMPLETED Process te.testEventId=" + te.getTestEventId());
       }

       catch( ScoringException e )
       {
           LogService.logIt( e, "StandardTestEventScorer.recalculatePercentilesForTestEvent() "  + te.toString() );
           throw e;
       }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt( e, "StandardTestEventScorer.recalculatePercentilesForTestEvent() "  + te.toString() );

           throw new ScoringException( e.getMessage() + "StandardTestEventScorer.recalculatePercentilesForTestEvent() " , ScoreUtils.getExceptionPermanancy(e) , te );
       }
    }


    
    @Override
    public List<SimCompetencyGroup> getSimCompetencyGroupList() throws Exception
    {
            List<SimCompetencyGroup> scgl = new ArrayList<>();

            String groupName = reportRules.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.CUSTOM.getSimCompetencyGroupTypeId() );            
            SimCompetencyGroup scg = createSimCompetencyGroup( groupName==null || groupName.isBlank() ? "Custom 1" : groupName, SimCompetencyGroupType.CUSTOM.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.CUSTOM.getSimCompetencyClassId(),SimCompetencyClass.CUSTOM_COMBO.getSimCompetencyClassId()}, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );
            
            groupName = reportRules.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.CUSTOM2.getSimCompetencyGroupTypeId() );
            scg = createSimCompetencyGroup( groupName==null || groupName.isBlank() ? "Custom 2" : groupName, SimCompetencyGroupType.CUSTOM2.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.CUSTOM2.getSimCompetencyClassId()}, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );
            
            groupName = reportRules.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.CUSTOM3.getSimCompetencyGroupTypeId() );
            scg = createSimCompetencyGroup( groupName==null || groupName.isBlank() ? "Custom 3" : groupName, SimCompetencyGroupType.CUSTOM3.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.CUSTOM3.getSimCompetencyClassId()}, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );
            
            groupName = reportRules.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.CUSTOM4.getSimCompetencyGroupTypeId() );
            scg = createSimCompetencyGroup( groupName==null || groupName.isBlank() ? "Custom 4" : groupName, SimCompetencyGroupType.CUSTOM4.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.CUSTOM4.getSimCompetencyClassId()}, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );
            
            groupName = reportRules.getReportRuleAsString("competencygrouptitle" + SimCompetencyGroupType.CUSTOM5.getSimCompetencyGroupTypeId() );
            scg = createSimCompetencyGroup( groupName==null || groupName.isBlank() ? "Custom 5" : groupName, SimCompetencyGroupType.CUSTOM5.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.CUSTOM5.getSimCompetencyClassId()}, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );
            
            scg = createSimCompetencyGroup( "Abilities", SimCompetencyGroupType.ABILITY.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.ABILITY.getSimCompetencyClassId(),SimCompetencyClass.ABILITY_COMBO.getSimCompetencyClassId()  }, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );

            scg = createSimCompetencyGroup( "Knowledge and Skills", SimCompetencyGroupType.SKILLS.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.CORESKILL.getSimCompetencyClassId(),  SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId(), SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId(), SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId(), SimCompetencyClass.KNOWLEDGE.getSimCompetencyClassId(), SimCompetencyClass.SKILL_COMBO.getSimCompetencyClassId() }, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );

            scg = createSimCompetencyGroup( "Attitudes, Interests, and Motivations", SimCompetencyGroupType.PERSONALITY.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId(),SimCompetencyClass.NONCOG_COMBO.getSimCompetencyClassId(),SimCompetencyClass.INTERESTS_COMBO.getSimCompetencyClassId() }, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );

            scg = createSimCompetencyGroup( "Past Behaviors", SimCompetencyGroupType.BIODATA.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.SCOREDBIODATA.getSimCompetencyClassId() }, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );

            scg = createSimCompetencyGroup( "Emotional Intelligence Factors", SimCompetencyGroupType.EQ.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.EQ.getSimCompetencyClassId() }, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );
            
            scg = createSimCompetencyGroup( "AI-Derived Traits", SimCompetencyGroupType.AI.getSimCompetencyGroupTypeId(), new int[] { SimCompetencyClass.VOICE_PERFORMANCE_INDEX.getSimCompetencyClassId() }, te.getSimCompetencyScoreList(), overallRawScoreCalcType );
            if( scg != null )
                scgl.add( scg );
            
            return scgl;        
    }
    
    
    

    public SimCompetencyGroup createSimCompetencyGroup( String name, int simCompetencyGroupTypeId, int[] simCompetencyClassIds, List<SimCompetencyScore> simCompetencyScoreList, OverallRawScoreCalcType overallRawScoreCalcType )
    {
        List<SimCompetencyScore> scsl = new ArrayList<>();

        boolean match;

        for( SimCompetencyScore scs : simCompetencyScoreList )
        {
            if( !scs.getHasScoreableData() )
                continue;
            
            // Do not include combo sim competencies in these calculations.
            //if( scs.getSimCompetencyClass().getIsCombo() )
            //    continue;
            
            match = false;
            for( int i=0; i<simCompetencyClassIds.length; i++ )
            {
                if( scs.getSimCompetencyClass().getSimCompetencyClassId()==simCompetencyClassIds[i] )
                    match=true;
            }

            if( !match )
                continue;

            // Do not include SimCompetency scores where it's supposed to be Z-score but there's not mean/sd
            if( scs.getSimCompetencyObj().getRawscorecalctypeid()==SimCompetencyRawScoreCalcType.ZSCORE_BASED_ON_TOTALS.getSimCompetencyRawScoreCalcTypeId() && 
                scs.getSimCompetencyObj().getStddeviation()<=0 )
                continue;
            
            scsl.add( scs );
        }

        if( scsl.isEmpty() )
            return null;

        return new SimCompetencyGroup( name, simCompetencyGroupTypeId, te.getSimXmlObj(), scsl, null, overallRawScoreCalcType );
    }



    @Override
    public String toString()
    {
        return "StandardTestEventScorer() testEventId=" + (this.te==null ? "null" : te.getTestEventId() + ", testKeyId=" + te.getTestKeyId());
    }

}
