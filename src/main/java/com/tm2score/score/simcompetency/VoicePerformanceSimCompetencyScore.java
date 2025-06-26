/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.simcompetency;

import com.tm2score.global.WeightedObject;
import com.tm2score.entity.event.TestEvent;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.global.NumberUtils;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.MergableScoreObjectCombiner;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.iactnresp.BaseScoredAvIactnResp;
import com.tm2score.service.LogService;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.voicevibes.VoiceVibesResult;
import com.tm2score.voicevibes.VoiceVibesScaleType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class VoicePerformanceSimCompetencyScore extends SimCompetencyScore implements WeightedObject
{
    private static final float STANDARD_RAW_MEAN = 196.47f;
    private static final float STANDARD_RAW_SD = 46.27f;
    

    public VoicePerformanceSimCompetencyScore( SimJ.Simcompetency sc, TestEvent te, boolean useTotalItems, boolean validItemsCanHaveZeroMaxPoints )
    {
        super( sc, te, useTotalItems, validItemsCanHaveZeroMaxPoints );
    }

    
    @Override
    public void init()
    {
        maxPointsPerItem = 100;
        totalMaxPoints = 400;
    }
    
    
    @Override
    public void calculateScore( int simCompetencyScoreCalcTypeId ) throws Exception
    {
        try
        {
            init();
            
            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "VoicePerformanceSimCompetencyScore.calculateScore() START SimCompetency =" + this.getName() + " - testEvent: " + ( testEvent == null ? "null" : testEvent.toString() )  );

            initForScoring();
            
            if( testEvent == null || testEvent.getSimletScoreList() == null )
            {
                LogService.logIt( "VoicePerformanceSimCompetencyScore.calculateScore() CANNOT SCORE SimCompetency - testEvent: " + ( testEvent == null ? "null" : testEvent.toString() ) + ", testEvent.getSimletScoreList()=" + (testEvent.getSimletScoreList() == null ? 0 : testEvent.getSimletScoreList().size() ) );
                return;
            }
            
            if( competencyScoreType==null )
                competencyScoreType = CompetencyScoreType.VOICE_PERFORMANCE_INDEX; 
            
            // metaScores = new float[9];

            // LogService.logIt( "VoicePerformanceSimCompetencyScore.calculateScore() BBBB." );
            // List<AvItemResponse> iirList = new ArrayList<>();

            List<MergableScoreObject> vvrList = new ArrayList<>();
            
            VoiceVibesResult vvr = null;
            
            for( ScorableResponse sr : testEvent.getAutoScorableResponseList() )
            {       
                AvItemResponse iir;
                BaseScoredAvIactnResp sair;
                if( sr instanceof BaseScoredAvIactnResp )
                {
                    sair = (BaseScoredAvIactnResp) sr;
                    
                    iir = sair.getAvItemResponse();
                    
                    if( iir==null || iir.getVoiceVibesStatusType().isAnyPermanentError() || iir.getVoiceVibesStatusType().isNotRequired() )
                        continue;
                    
                    if( iir.getVoiceVibesStatusType().isPending() )
                        pendingExternalScores = true;
                    
                    if( iir.hasVoiceVibesReport() )
                    {
                        vvr = new VoiceVibesResult( iir.getVoiceVibesResponseStr(), null, sair.getWordCount() );
                        
                        if( vvr.hasResult( VoiceVibesScaleType.VIBE_CONFIDENT) )
                            vvrList.add( vvr );
                    }
                }
            }
                
            LogService.logIt( "VoicePerformanceSimCompetencyScore.calculateScore() Found " + vvrList.size() + " valid vibes reports." ); 

            totalScorableItems = vvrList.size();                

            if( vvrList.isEmpty() )
            {
                hasScoreableData = false;
                return;
            }

            hasScoreableData = true;

            if( vvrList.size()>1 )
            {
                List<MergableScoreObject> vvrList2 = MergableScoreObjectCombiner.combineLikeObjects( vvrList );

                LogService.logIt( "VoicePerformanceSimCompetencyScore.calculateScore() combining valid vibes reports. TestEventId=" + testEvent.getTestEventId() ); 

                if( !vvrList2.isEmpty() )
                    vvrList = vvrList2;
            }

            for( int i=0; i<vvrList.size(); i++ )
            {
                vvr = (VoiceVibesResult) vvrList.get(i);

                if( vvr!=null && vvr.hasResult(VoiceVibesScaleType.VIBE_CONFIDENT) )
                    break;
                vvr = null;
            } 

            if( vvr==null )
            {
                LogService.logIt( "VoicePerformanceSimCompetencyScore.calculateScore() Unable to find a VoiceVibesResult object to use.  TestEventId=" + testEvent.getTestEventId() ); 
                hasScoreableData = false;
                return;                            
            }

            float confidence = vvr.getVoiceVibesScaleScore(VoiceVibesScaleType.VIBE_CONFIDENT ).getScore();
            float assertive = vvr.getVoiceVibesScaleScore(VoiceVibesScaleType.VIBE_ASSERTIVE ).getScore();
            float boring = vvr.getVoiceVibesScaleScore(VoiceVibesScaleType.VIBE_BORING ).getScore();
            float detached = vvr.getVoiceVibesScaleScore(VoiceVibesScaleType.VIBE_DETACHED ).getScore();

            float composite = confidence + assertive + (100-boring) + (100-detached);

            this.totalPoints = composite;
            this.totalScoreValue = composite;
            this.averagePoints = composite;
            this.fractionScoreValue = composite;

            setSimCompetencyScoreTypes();

            ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scoreFormatTypeId );            


            rawScore = 0; 

            if( rawScoreCalcType.getIsZScore() )
            {
                if( simCompetencyObj.getStddeviation()>0 ) 
                    rawScore = (composite - simCompetencyObj.getMean())/simCompetencyObj.getStddeviation();
                else
                {
                    simCompetencyObj.setMean(STANDARD_RAW_MEAN);
                    simCompetencyObj.setStddeviation(STANDARD_RAW_SD);
                    rawScore = (composite - STANDARD_RAW_MEAN)/STANDARD_RAW_SD;
                }
            }

            else
                rawScore = composite/4f;


            scaledScore = 0;

            if( scaledScoreCalcType.getIsTransform() ) // && simCompetencyObj.getScaledstddeviation()>0 )
                scaledScore = NumberUtils.applyNormToZScore( rawScore, simCompetencyObj.getScaledmean(), simCompetencyObj.getScaledstddeviation() ); // (simCompetencyObj.getScaledstddeviation()*rawScore) + simCompetencyObj.getMean();

            else if( scaledScoreCalcType.getEqualsRawScore())
                scaledScore = rawScore;

            else
                scaledScore = scoreFormatType.getUnweightedScaledScore( competencyScoreType, rawScore, maxPointsPerItem, simCompetencyObj.getLookuptable() );

            if(  scaledScore<scoreFormatType.getMinScoreToGiveTestTaker() )
                scaledScore=scoreFormatType.getMinScoreToGiveTestTaker();

            if(  scaledScore>scoreFormatType.getMaxScoreToGiveTestTaker())
                scaledScore=scoreFormatType.getMaxScoreToGiveTestTaker();
                        
            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "VoicePerformanceSimCompetencyScore.calculateScore() scaled score=" + scaledScore + ", raw score=" + rawScore + ",  composite total=" + composite + ", confidence=" + confidence + ", assertive=" + assertive + ", boring=" + boring + ", detached=" + detached ); 
                                   
            metaScores = new float[16];  
            metaScores[0] = confidence;
            metaScores[1] = assertive;
            metaScores[2] = boring;
            metaScores[3] = detached;
            metaScores[4] = composite;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "VoicePerformanceSimCompetencyScore.calculateScore() " + toString() );

            scaledScore = 0;

            rawScore = 0;

            throw e;

        }
    }

    

}
