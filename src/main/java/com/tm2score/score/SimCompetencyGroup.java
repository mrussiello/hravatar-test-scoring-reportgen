/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.sim.OverallRawScoreCalcType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class SimCompetencyGroup {

    String name;

    int simCompetencyGroupTypeId = 0;

    List<SimCompetencyScore> simCompetencyScoreList;

    SimJ simJ;

    OverallRawScoreCalcType overallRawScoreCalcType;
    
    List<TestEventScore> testEventScoreList;



    public SimCompetencyGroup( String name, int simCompetencyGroupTypeId, SimJ simJ, List<SimCompetencyScore> scl, List<TestEventScore> tesl, OverallRawScoreCalcType overallRawScoreCalcType)
    {
        this.name = name;
        this.simCompetencyGroupTypeId = simCompetencyGroupTypeId;
        this.simCompetencyScoreList = scl;
        this.overallRawScoreCalcType = overallRawScoreCalcType;

        if( simCompetencyScoreList == null )
            simCompetencyScoreList = new ArrayList<>();
        
        testEventScoreList = tesl;
        
        if( testEventScoreList == null )
            testEventScoreList = new ArrayList<>();
        
    }

    
    public float calculateGroupScore()
    {
        if( simCompetencyScoreList.isEmpty() && testEventScoreList.isEmpty() )
            return 0;

        if( simCompetencyScoreList.size()==1 )
            return simCompetencyScoreList.get(0).getUnweightedScaledScore(true);

        if( testEventScoreList.size()==1 )
            return testEventScoreList.get(0).getScore();

        float scsScr = 0;
        float totalWeights = 0;
        boolean hasWeights = false;

        if( !simCompetencyScoreList.isEmpty() )
        {
            for( SimCompetencyScore scs : this.simCompetencyScoreList )
            {
                if( !scs.getHasScoreableData() )
                    continue;
                
                // don't include combos.
                //if( scs.getSimCompetencyClass().getIsCombo() )
                //    continue;
                
                if( scs.getSimCompetencyObj().getWeight() > 0 )
                {
                    hasWeights=true;
                    break;
                }
            }        
        }
        
        else if( !testEventScoreList.isEmpty() )
        {
            for( TestEventScore tes : testEventScoreList )
            {
                if( !tes.getTestEventScoreType().getIsCompetency() )
                    continue;
                
                if( tes.getWeight()>0 )
                {
                    hasWeights=true;
                    break;                    
                }
            }
        }
        
        float rawTotal = 0;
        float groupScore = 0;

        // LogService.logIt( "SimCompetencyGroup.calculateGroupScore() simCompetencyScoreList.size=" + simCompetencyScoreList.size());
        
        if( !simCompetencyScoreList.isEmpty() )
        {
            for( SimCompetencyScore scs : this.simCompetencyScoreList )
            {
                if( !scs.getHasScoreableData() )
                    continue;

                if( overallRawScoreCalcType.equals(OverallRawScoreCalcType.SIMCOMPETENCYRANKS ) ) // scs.isOnet() && useRankValuesAsWeights )
                {
                    scsScr = scs.getRankValueWeightedScaledScore( false );
                    totalWeights += scs.getRankValueWeight();
                    rawTotal += scsScr;
                }

                else if( overallRawScoreCalcType.equals(OverallRawScoreCalcType.ONETIMPORTANCE ) )
                {
                    scsScr = scs.getImportanceValueWeightedScaledScore( false );
                    totalWeights += scs.getImportanceValueWeight();
                    rawTotal += scsScr;
                }

                else // if( overallRawScoreCalcType.getIsAnyWeights()  )
                {
                    if( hasWeights )
                    {
                        // hasWeights = true;
                        scsScr = scs.getUserWeightedScaledScore( false ); //  overallRawScoreCalcType.getUsesRawCompetencyScores() ? scs.getUserWeight()*scs.getRawScore() : scs.getUserWeightedScaledScore( true );
                        
                        if( overallRawScoreCalcType.getIsSum() )
                            totalWeights = 1;
                        else
                            totalWeights += scs.getUserWeight();
                        
                        // scs.setWeightUsed( scs.getUserWeight() );
                        rawTotal += scsScr;
                        
                        // LogService.logIt( "SimCompetencyGroup.calculateGroupScore() Added score=" + scs.getUnweightedScaledScore(false) + ", weight=" + scs.getUserWeight() );
                        
                    }

                    else
                    {
                        scsScr = scs.getUnweightedScaledScore( false ); //  overallRawScoreCalcType.getUsesRawCompetencyScores() ? scs.getRawScore() : scs.getUnweightedScaledScore( true );

                        if( overallRawScoreCalcType.getIsSum() )
                            totalWeights = 1;
                        else
                            totalWeights++;
                        // scs.setWeightUsed( 1f );
                        rawTotal += scsScr;
                    }
                }
            }
        }

        else if( !testEventScoreList.isEmpty() )
        {
            for( TestEventScore tes : this.testEventScoreList )
            {
                if( hasWeights )
                {
                    scsScr = tes.getScore()*tes.getWeight();
                    if( overallRawScoreCalcType.getIsSum() )
                        totalWeights = 1;
                    else
                        totalWeights += tes.getWeight();
                    rawTotal += scsScr;
                    
                }
                else
                {
                    scsScr = tes.getScore();
                    if( overallRawScoreCalcType.getIsSum() )
                        totalWeights = 1;
                    else
                        totalWeights++;
                    rawTotal += scsScr;                    
                }
                
            }
        }                        

        if( totalWeights>0 )
            groupScore =  rawTotal/totalWeights;            
        
        else
            groupScore = rawTotal;

        // LogService.logIt( "SimCompetencyGroup.calculateGroupScore() rawTotal=" + rawTotal + ", totalWeights=" + totalWeights + ", groupScore=" + groupScore );
        
        
        //if( overallRawScoreCalcType.getUsesRawCompetencyScores() )
        //{
        //    float mean = simJ==null || simJ.getRawtoscaledfloatparam2()<=0 ? RuntimeConstants.getFloatValue( "TgtSimScaledScoreMeanSCORE2" ) : simJ.getRawtoscaledfloatparam1();
         //   float std = simJ==null || simJ.getRawtoscaledfloatparam2()<=0 ? RuntimeConstants.getFloatValue( "TgtSimScaledScoreStdevSCORE2" ) : simJ.getRawtoscaledfloatparam2();

        //    groupScore = groupScore*std + mean;                
        //}
        
        // LogService.logIt( "SimCompetencyGroup.calculateScore() " + name + ", type=" + this.simCompetencyGroupTypeId + ", rawTotal=" + rawTotal + ", totalWeights=" + totalWeights + ", groupScore=" + groupScore );

        return groupScore;
    }


    public void addSimCompetencyScore( SimCompetencyScore scs )
    {
        // see if present.
        for( SimCompetencyScore s : this.simCompetencyScoreList )
        {
            if( s.getNameEnglish()!=null && scs.getNameEnglish()!=null && !scs.getNameEnglish().isEmpty() && s.getNameEnglish().equals( scs.getNameEnglish() ))
                return;
        }

        simCompetencyScoreList.add( scs );
    }

    public List<SimCompetencyScore> getSimCompetencyList() {
        return simCompetencyScoreList;
    }

    public void setSimCompetencyList(List<SimCompetencyScore> simCompetencyList) {
        this.simCompetencyScoreList = simCompetencyList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OverallRawScoreCalcType getOverallRawScoreCalcType() {
        return overallRawScoreCalcType;
    }

    public void setOverallRawScoreCalcType(OverallRawScoreCalcType overallRawScoreCalcType) {
        this.overallRawScoreCalcType = overallRawScoreCalcType;
    }

    public int getSimCompetencyGroupTypeId() {
        return simCompetencyGroupTypeId;
    }

    public void setSimCompetencyGroupTypeId(int simCompetencyGroupTypeId) {
        this.simCompetencyGroupTypeId = simCompetencyGroupTypeId;
    }

    public List<SimCompetencyScore> getSimCompetencyScoreList() {
        return simCompetencyScoreList;
    }



}
