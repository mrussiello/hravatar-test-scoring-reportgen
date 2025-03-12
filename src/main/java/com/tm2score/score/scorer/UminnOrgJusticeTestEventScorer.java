/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.custom.uminnoj.UMinnJusticeGroupType;
import com.tm2score.custom.uminnoj.UMinnJusticeDimensionType;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.SimletScore;
import com.tm2score.score.iactnresp.IactnResp;
import com.tm2score.score.iactnresp.RadioButtonGroupResp;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * This class needs to calculate the average values for each Justice Type overall and for each Group Type. 
 * 
 * There 
 *     
 * 
 * @author miker_000
 */
public class UminnOrgJusticeTestEventScorer  extends CT5DirectTestEventScorer {
    
    
    /*
      Map of DimensionType, float scores
    */
    Map<UMinnJusticeDimensionType,Float> dimensionTypeScoreMap = null; 

    /*
      Map of DimensionTypeId-JusticeGroupTypeId, Float score for this combination
    */
    Map<String,Float> dimensionTypeGroupScoreMap = null; 
    
    
   @Override
   public void calculateOverallScores() throws Exception
   {       
       // overall average
       float total=0;
       float count=0;
       
       // type averages (across groups)
       float[] typeTotals=new float[5];
       float[] typeCounts=new float[5];
       
       int itemNumber;
       UMinnJusticeGroupType groupType;
       UMinnJusticeDimensionType dimensionType;
       
       if( dimensionTypeScoreMap==null )
           dimensionTypeScoreMap = new HashMap<>();
       
       if( dimensionTypeGroupScoreMap==null )
           dimensionTypeGroupScoreMap = new HashMap<>();
       
       dimensionTypeScoreMap.clear();
       dimensionTypeGroupScoreMap.clear();
       
       // Map of dimensionTypeId-justiceGroupTypeId, float[total,count]
       Map<String,float[]> dimensionGroupTypePairMap = new HashMap<>();
       float[] pair;
       String pairName;
       
       SimletCompetencyScore slcs;
       
       // SimCompetencyScores represent GROUPS.
       for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
       {
           groupType = UMinnJusticeGroupType.getValueForGroupSimCompetency(scs);

            LogService.logIt( "UminnOrgJusticeTestEventScorer.calculateOverallScores() simCompetency=" + scs.getName() + ", groupType=" + (groupType==null ? "null" : groupType.getName()) );
           
            if( groupType==null )
                continue;
            
            for( SimletScore ss : te.getSimletScoreList() )
            {
                // look for a simletCompetencyScore matching this SimCompetency within this SimletScore
                slcs = ss.getCompetencyScoreForSimCompetencyId( scs.getSimCompetencyObj().getId() );

                // ignore if not found.
                if( slcs==null )
                    continue;

                LogService.logIt( "UminnOrgJusticeTestEventScorer.calculateOverallScores() Have SimletCompetencyScore for simCompetency=" + scs.getName() + ", simletCompetencyId=" + slcs.competencyScoreObj.getId() );
                
                // now look for all items that go with this simletcompetency score.
                for( ScorableResponse sr : te.getAutoScorableResponseList() )
                {
                    if( !sr.hasValidScore() )
                    {
                        LogService.logIt( "UminnOrgJusticeTestEventScorer.calculateOverallScores() ScorableResponse does not have a valid score. simletCompetencyId=" + sr.simletCompetencyId() + ", " + sr.toString() );
                        continue;
                    }
                    
                    // now check that item matches SimletCompetencyScore (GROUP)
                    if( sr.simletCompetencyId()!=slcs.competencyScoreObj.getId() )
                        continue;

                    // Item is part of Group, so count in total.
                    total += sr.itemScore();
                    count++;

                    // Item Number
                    itemNumber = getItemNumberFromScorableResponse( sr );

                    LogService.logIt( "UminnOrgJusticeTestEventScorer.calculateOverallScores() Have matching Scorable response for SimCompetency=" + scs.getName() + " and simletCompetencyId=" + sr.simletCompetencyId() + ", itemScore=" + sr.itemScore() +", itemNumber=" + itemNumber );
                    
                    // Dimension for this item.
                    dimensionType = itemNumber>0 ? UMinnJusticeDimensionType.getValueForItemNumber(itemNumber) : null;
                    if( dimensionType!=null )
                    {
                        // add to dimension stats 
                        typeTotals[dimensionType.getUminnJusticeDimensionTypeId()] += sr.itemScore();
                        typeCounts[dimensionType.getUminnJusticeDimensionTypeId()]++;
                       
                        
                        // add to dimension-group stats
                        pairName = dimensionType.getUminnJusticeDimensionTypeId() + "-" + groupType.getUminnJusticeGroupTypeId();
                        pair = dimensionGroupTypePairMap.get(pairName);
                        if( pair==null)
                        {
                            pair=new float[2];
                            dimensionGroupTypePairMap.put(pairName,pair);
                        }
                        pair[0]+=sr.itemScore();
                        pair[1]++;
                    }
                }                
            }
        }

        // overall 
        ovrRawScr = count>0 ? total/count : 0;
        ovrScaledScr = ovrRawScr;

        LogService.logIt( "UminnOrgJusticeTestEventScorer.calculateOverallScores() total=" + total + ", count=" + count + ", overall average=" + ovrRawScr );
        
        // individual dimension types. We will use these in 
        for( UMinnJusticeDimensionType jt : UMinnJusticeDimensionType.values() )
        {
            LogService.logIt( "UminnOrgJusticeTestEventScorer.calculateOverallScores() UMinnJusticeDimensionType=" + jt.getName() + ", total=" + typeTotals[jt.getUminnJusticeDimensionTypeId()] + ", count=" + typeCounts[jt.getUminnJusticeDimensionTypeId()] );
            dimensionTypeScoreMap.put(jt, typeCounts[jt.getUminnJusticeDimensionTypeId()]>0 ? typeTotals[jt.getUminnJusticeDimensionTypeId()]/typeCounts[jt.getUminnJusticeDimensionTypeId()] : 0 );
        }
        
        float groupScore;
        //  dimension-group pairs
        for( String pairNm : dimensionGroupTypePairMap.keySet() )
        {
            // groupTypeId = Integer.parseInt(pairNm.substring( pairNm.lastIndexOf("-")+1, pairNm.length()).trim() );
            
            pair = dimensionGroupTypePairMap.get(pairNm);
            groupScore=pair[1]>0 ? pair[0]/pair[1] : 0;
            dimensionTypeGroupScoreMap.put(pairNm, groupScore);            
        }

       
        scrSum.append( "Overall Raw and Scaled Score: " + ovrRawScr  + "\n" );
        // save the test event
        te.setOverallScore(ovrScaledScr);
        
    }
    
    // Justice Groups are Competencies
    // Justice Dimensions are Competency Groups
    @Override
    public int setCompetencyGroupTestEventScores( int counter ) throws Exception
    {
        TestEventScore tes2;
        TestEventScore tes;

        Float jscore;
        if( dimensionTypeScoreMap==null )
        {
            LogService.logIt( "UminnOrgJusticeTestEventScorer.setCompetencyGroupTestEventScores() justiceTypeScoreMap is null. Something wrong. Calculating, then continuing." );
            calculateOverallScores();
        }
        
        for( UMinnJusticeDimensionType jt : UMinnJusticeDimensionType.values() )
        {
            jscore = dimensionTypeScoreMap.containsKey(jt) ? dimensionTypeScoreMap.get(jt) : null;
            
            if( jscore==null )
                LogService.logIt( "UminnOrgJusticeTestEventScorer.setCompetencyGroupTestEventScores() justiceTypeScoreMap does not contain a score for " + jt.getName() );
            
            tes = new TestEventScore();

            tes.setTestEvent(te);
            tes.setTestEventId( te.getTestEventId() );
            tes.setTestEventScoreTypeId( TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() );
            tes.setDisplayOrder( ++counter );
            tes.setName( jt.getName() );
            tes.setNameEnglish( jt.getName() );
            tes.setScoreFormatTypeId( ScoreFormatType.OTHER_SCORED.getScoreFormatTypeId() );
            tes.setIntParam1( jt.getUminnJusticeDimensionTypeId() );
            tes.setScore( jscore==null ? 0 : jscore );
            tes.setRawScore( tes.getScore() );

            tes2 = getMatchingExistingTestEventScore(tes);
            if( tes2!=null && tes2.getTestEventScoreId()>0 )
                tes.setTestEventScoreId( tes2.getTestEventScoreId() );

            Float val;
            for( UMinnJusticeGroupType gt : UMinnJusticeGroupType.values() )
            {
                val = dimensionTypeGroupScoreMap.get( jt.getUminnJusticeDimensionTypeId() + "-" + gt.getUminnJusticeGroupTypeId() );
                if( val!=null )
                {
                    switch (gt.getUminnJusticeGroupTypeId()) {
                        case 1:
                            tes.setScore2( val );
                            break;
                        case 2:
                            tes.setScore3( val );
                            break;
                        case 3:
                            tes.setScore4( val );
                            break;
                        case 4:
                            tes.setScore5( val );
                            break;
                        case 5:
                            tes.setScore6( val );
                            break;
                        case 6:
                            tes.setScore7( val );
                            break;
                        case 7:
                            tes.setScore8( val );
                            break;
                        default:
                            break;
                    }
                }    
            }
            
            eventFacade.saveTestEventScore(tes);
        }
        
        return counter;
    }    
    
    
   private int getItemNumberFromScorableResponse( ScorableResponse sr )
   {
        IactnResp ir = null;
       
        if( sr instanceof RadioButtonGroupResp )
           ir = ((RadioButtonGroupResp)sr).getIactnResp();
        
        else if( sr instanceof IactnResp )
           ir = (IactnResp)sr;
        
        else
        {
           LogService.logIt( "UminnOrgJusticeTestEventScorer.getItemNumberFromScorableResponse() ScorableResponse has neither a RadioButtonGroup or an IactnResp. " + sr.toString());
           return 0;
        }            
       
        if( ir==null )
        {
            LogService.logIt( "UminnOrgJusticeTestEventScorer.getItemNumberFromScorableResponse() RadioButtonGroup has no IactnResp." + sr.toString() );
            return 0;
        }
       
        if( ir.getIntnObj()==null )
        {
           LogService.logIt( "UminnOrgJusticeTestEventScorer.getItemNumberFromScorableResponse() ScorableResponse has no intnObj" + sr.toString() );
           return 0;
        }
        
        String u = ir.getIntnObj().getUniqueid();
        
        if( u==null || u.isBlank() || !u.contains("-") )
        {
           LogService.logIt( "UminnOrgJusticeTestEventScorer.getItemNumberFromScorableResponse() ScorableResponse.intnObj.uniqueId is invalid: u=" + u + ", " + sr.toString() );
           return 0;
        }
        String ss = u.substring( u.lastIndexOf('-')+1, u.length() ).trim();
        if( ss.isBlank() )
        {
           LogService.logIt( "UminnOrgJusticeTestEventScorer.getItemNumberFromScorableResponse() ScorableResponse.intnObj.uniqueId is invalid: u=" + u + ", ss=" + ss );
           return 0;
        }
        try
        {
            return Integer.valueOf(ss);
        }
        catch( NumberFormatException e )
        {
           LogService.logIt( "UminnOrgJusticeTestEventScorer.getItemNumberFromScorableResponse() " + e.toString() + ", ScorableResponse.intnObj.uniqueId is invalid: u=" + u + ", ss=" + ss );
           return 0;
        }
   }
   
    
}
