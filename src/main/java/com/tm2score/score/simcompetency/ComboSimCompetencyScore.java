/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.simcompetency;

import com.tm2score.entity.event.TestEventResponseRating;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventResponseRatingUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.iactnresp.IactnItemResp;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyCombinationType;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.UrlEncodingUtils;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class ComboSimCompetencyScore {
    
    SimCompetencyScore scs;
    
    List<SimCompetencyScore> members;
    
    List<ScorableResponse> autoScorableResponseList = null;
    
    SimCompetencyCombinationType comboType;
        
    // float totalCorrect;
    float totalScoresForAvgs;
    // float totalPoints;
    // int totalScorableItems;
    float countForAvgs;
    // float rawScore = 0;
    // float maxPointsPerItem = 0;
    
    float totalWeights=0;
    
    long testEventId = 0;
    
    int tier = 1;
    
    Map<Long,Float> simCompetencyIdWeightMap;
        
    
    public ComboSimCompetencyScore( SimCompetencyScore scs, List<SimCompetencyScore> members, List<ScorableResponse> srl, long testEventId, Map<Long,Float> simCompetencyIdWeightMap) throws Exception
    {
        this.scs = scs;
        this.members = members;
        this.testEventId=testEventId;
        this.simCompetencyIdWeightMap=simCompetencyIdWeightMap;

        comboType = SimCompetencyCombinationType.getValue( scs.getSimCompetencyObj().getCombinationtype() );   
        
        if( comboType.isIndividualItemLevel() )
        {
            parseItemIdList( srl );
            // LogService.logIt( "ComboSimCompetencyScore() Individual Item level Combo Sim Competency has " + autoScorableResponseList.size() + " scorable responses." );
        }
        
        else if( members==null || members.isEmpty() )
        {
            LogService.logIt("ComboSimCompetencyScore() " + scs.getName() + ", ENG=" + scs.getNameEnglish() + ", Combo SimCompetency is competency-level but has no members." );
            
            if( this.members==null )
                this.members = new ArrayList<>();
            // throw new Exception( "Combo SimCompetency is competency-level has no members." );            
        }
        
        else
        {
            for( SimCompetencyScore m : members )
            {
                if( m.getSimCompetencyClass().getIsCombo() )
                    tier = 2;
            }
        }
    }
    
    
    
    public void calculateScore() throws Exception
    {
        try
        {
            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "ComboSimCompetencyScore.calculateScore() START Scoring " + toString() );
            
            scs.hasScoreableData=false;
            scs.totalCorrect=0;
            totalScoresForAvgs=0;
            scs.totalPoints=0;
            scs.totalScorableItems=0;
            countForAvgs=0;
            scs.rawScore=0;
            scs.totalMaxPoints = 0;
            scs.maxPointsPerItem=0;
            
            if( scs.getCaveatList() == null )
                scs.setCaveatList( new ArrayList<>() );
            
            scs.metaScores = new float[13];
            
            scs.competencyScoreType = comboType.getForcedCompetencyScoreType();
            
            if( comboType.isIndividualItemLevel() )
            {
                ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scs.scoreFormatTypeId );  
                scs.scaledScoreFloor = scoreFormatType.getMin();
                scs.scaledScoreCeiling = scoreFormatType.getMax();
            }
            
            if( comboType.isIndividualItemLevel() && comboType.equals(SimCompetencyCombinationType.ITEM_LEVEL_AVERAGE_RATINGS) )
            {
                TestEventResponseRatingUtils terrUtils = new TestEventResponseRatingUtils();
                
                List<TestEventResponseRating> terrl = terrUtils.getTestEventResponseRatingList( testEventId );
                
                Object[] data =  terrUtils.getAverageResponseRatingForSimCompetency(terrl, this.scs.simCompetencyObj.getId() );
                if( data!=null && ((Integer)data[0])>0 )
                {
                    int count = ((Integer)data[0]);
                    float averageRating = ((Float)data[1]);
                    
                    // Note - ratings are 1 - 10
                    
                    ScoreFormatType ratingSft = ScoreFormatType.NUMERIC_1_TO_10;
                    ScoreFormatType scSft = ScoreFormatType.getValue( scs.scoreFormatTypeId );  
                    float transformedScore = ScoreFormatType.convertFromAToBToLinearScore( averageRating, ratingSft, scSft );

                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "ComboSimCompetencyScore.calculateScore() Average Rating: " + averageRating + ", transformedScore=" + transformedScore);
                    
                    scs.averagePoints = transformedScore; //   averageRating;
                    scs.fractionCorrect =  transformedScore/(scSft.getMax()>100 ? scSft.getMax() : 10f); //  averageRating/10f;
                    totalScoresForAvgs =  count*transformedScore; // count*averageRating;
                    countForAvgs = count;
                    
                    finalizeScoreStatsItemLevel();
                }
            }
            else if( comboType.isIndividualItemLevel() )
            {
                if( autoScorableResponseList == null || autoScorableResponseList.isEmpty() )
                {
                    LogService.logIt( "ComboSimCompetencyScore.calculateScore() " + scs.getName() + ", ENG=" + scs.getNameEnglish() + ", Combo SimCompetency is item -level but has no scorable responses. simCompetencyId=" + scs.simCompetencyObj.getId() + ", testEventId=" + (scs.testEvent==null ? "null" : scs.testEvent.getTestEventId()));
                    return;
                    // throw new Exception( "Combo SimCompetency Item Type has no member scorable responses!" );
                }
                
                // this is an array of mapping values (like a lookup table) that is used to map computed item score values to 'revised' item score values to be used in this combination competency only.
                JsonArray itemScoreMapArray = null;
                if( scs!=null && scs.scJo!=null && scs.scJo.containsKey("comboitemscoremaps") && !scs.scJo.isNull("comboitemscoremaps"))
                    itemScoreMapArray = scs.scJo.getJsonArray("comboitemscoremaps");
                
                for( ScorableResponse sr : autoScorableResponseList )
                {
                    updateScoreStatsForScorableResp(sr, itemScoreMapArray );
                }
                
                totalScoresForAvgs = scs.totalPoints;
                
                finalizeScoreStatsItemLevel();
            }
            
            else
            {
                if( members == null || members.isEmpty() )
                {
                    LogService.logIt( "ComboSimCompetencyScore.calculateScore() " + scs.getName() + ", ENG=" + scs.getNameEnglish() + ", Combo SimCompetency is competency-level but has no members. simCompetencyId=" + scs.simCompetencyObj.getId() + ", testEventId=" + (scs.testEvent==null ? "null" : scs.testEvent.getTestEventId()) );
                    return;
                    // throw new Exception( "Combo SimCompetency has no members." );
                }
            
                for( SimCompetencyScore scsx : members )
                {
                    if( scsx.isPendingExternalScores()  )
                    {
                        LogService.logIt( "ComboSimCompetencyScore.calculateScore() Member is pending external scores. Skipping.  Member score (" + scsx.getName() + ", Eng=" + scsx.getNameEnglish() + ") because there is no score data and the member competency is not set to score if no responses. " + scs.getName() + ", ENG=" + scs.getNameEnglish() + ", simCompetencyId=" + scs.simCompetencyObj.getId() + ", testEventId=" + (scs.testEvent==null ? "null" : scs.testEvent.getTestEventId()) );
                        continue;
                    }
                    
                    if( !scsx.getSimCompetencyClass().getIsCombo() && !scsx.getHasScoreableData() && scsx.simCompetencyObj.getScoreifnoresponses()!=1 )
                    {
                        LogService.logIt( "ComboSimCompetencyScore.calculateScore() Skipping Member score (" + scsx.getName() + ", Eng=" + scsx.getNameEnglish() + ") because there is no score data and the member competency is not set to score if no responses. " + scs.getName() + ", ENG=" + scs.getNameEnglish() + ", simCompetencyId=" + scs.simCompetencyObj.getId() + ", testEventId=" + (scs.testEvent==null ? "null" : scs.testEvent.getTestEventId()) );
                        continue;
                    }
                    
                    // collect caveats and interview questions
                    //if( 1==2 )
                    //{
                    //    scs.getCaveatList().addAll( scsx.getCaveatList() );
                    //    scs.getScoreTextInterviewQuestionList().addAll( scsx.getScoreTextInterviewQuestionList() );
                    //}
                    updateScoreStats( scsx );
                }
    
                finalizeScoreStats();                           
            }
            
            calculateRawAndScaledScores();
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ComboSimCompetencyScore.calculateScore() " + toString() );
        }
    }

    
    private void parseItemIdList( List<ScorableResponse> srl ) throws Exception
    {
        String unqIds = null;
        
        try
        {        
            unqIds = scs.simCompetencyObj.getCombinationitemuniqueids();
            
            if( unqIds==null )
                unqIds = "";
            unqIds = unqIds.trim();
            
            autoScorableResponseList = new ArrayList<>();
            
            String[] unqIdArr = unqIds.split(",");
            
            String unqId;
            String seqStr;
            int seqId;
            ScorableResponse sr;
            
            for( int i=0; i<unqIdArr.length-1; i+=2 )
            {
                unqId=unqIdArr[i].trim();
                seqStr=unqIdArr[i+1].trim();
                
                if( unqId.isBlank() || seqStr.isBlank() )
                    continue;
                seqId = Integer.parseInt(seqStr);
                
                sr = getMatchingScorableResponse( unqId, seqId, srl );
                
                if( sr==null )
                {
                    if( !comboType.equals(SimCompetencyCombinationType.ITEM_LEVEL_AVERAGE_RATINGS) )
                        LogService.logIt( "ComboSimCompetencyScore.parseItemIdList() Cannot find Matching Scorable Response for UniqueId=" + unqId + ", Intn.Item.SeqId=" + seqId + ", " + toString() );
                    continue;
                }
                
                // LogService.logIt( "ComboSimCompetencyScore.parseItemIdList() FOUND Matching Scorable Response for Intn.UniqueId=" + unqId + ", Intn.Item.SeqId=" + seqId + ", " + toString() );
                autoScorableResponseList.add( sr );                
            }
        
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ComboSimCompetencyScore.parseItemIdList() unqIds=" + unqIds  + ", " + toString() );            
            throw e;
        }
    }
    
    private ScorableResponse getMatchingScorableResponse( String uniqueId, int subnodeSeqId, List<ScorableResponse> srl )
    {
        if( srl==null )
            return null;
        
        IactnItemResp iir;
        
        for( ScorableResponse sr : srl )
        {
            if( sr.getSimletNodeUniqueId()==null )
                continue;
            
            if( !UrlEncodingUtils.decodeKeepPlus(sr.getSimletNodeUniqueId()).equals(uniqueId) )
                continue;
            
            if( subnodeSeqId<=0 )
                return sr;
            
            if( !(sr instanceof IactnItemResp) )
                continue;
            
            
            iir = (IactnItemResp) sr;
            
            if( iir.intnItemObj==null || iir.intnItemObj.getSeq()!=subnodeSeqId )
                continue;
            
            return iir;            
        }
        
        return null;
    }
    
    
    
    private void updateScoreStats( SimCompetencyScore scsx ) throws Exception
    {
        try
        {
           // LogService.logIt( "ComboSimCompetencyScore.updateScoreStats() AAAA Updating from " + scsx.getName() + ", scsx.totalCorrect=" + scsx.totalCorrect + ", scsx.totalPoints=" + scsx.totalPoints + ", scsx.totalMaxPoints=" +  scsx.totalMaxPoints + ", scsx.totalScorableItems=" + scsx.totalScorableItems);

            scs.totalMaxPoints += scsx.totalMaxPoints;      
            scs.totalScorableItems += scsx.totalScorableItems;
            scs.totalPoints += scsx.totalPoints;
            scs.totalCorrect += scsx.totalCorrect;

            // if( ScoreManager.DEBUG_SCORING )
            //    LogService.logIt( "ComboSimCompetencyScore.updateScoreStats() BBBB totalCorrect=" + scs.totalCorrect + ", totalPoints=" + scs.totalPoints + ", totalMaxPoints=" +  scs.totalMaxPoints + ", totalScorableItems=" + scs.totalScorableItems + ", scs.competencyScoreType: " + (scs.competencyScoreType==null ? "null" : "not null") + ", scsx.competencyScoreType: " + (scsx.competencyScoreType==null ? "null" : "not null") );


            // if null, want to use same competencyScoreType as the member.
            if( scs.competencyScoreType == null )
                scs.competencyScoreType = scsx.competencyScoreType;

            if( scs.competencyScoreType!=null && scs.competencyScoreType.equals( CompetencyScoreType.AVG_MAX_MINUS_ABS_TRUE_DIFF ) && scs.maxPointsPerItem<scsx.maxPointsPerItem )
                scs.maxPointsPerItem = scsx.maxPointsPerItem;

            if( scsx.scaledScoreCeiling> scs.scaledScoreCeiling )
                scs.scaledScoreCeiling = scsx.scaledScoreCeiling;

            if( scsx.scaledScoreFloor> scs.scaledScoreFloor )
                scs.scaledScoreFloor = scsx.scaledScoreFloor;


            for( int i=2; i<=12; i++ )
            {
                // LogService.logIt( "SimCompetencyScore adding simletCompetencyScore.metascore[] index=" + i + " value=" + scs.getMetaScore(i));

                if( scs.metaScores!=null && scs.metaScores.length>i )
                    scs.metaScores[i] += scsx.getMetaScore(i);
            }        

            // Item level Scores or percent correct (item level)
            if( comboType.isItemLevel() )
            {
                updateScoreStatsItemLevel( scsx );
                //return;
            }


            else if( comboType.isAverage() || comboType.isSum() )
            {
                float weight = simCompetencyIdWeightMap==null || !simCompetencyIdWeightMap.containsKey(scsx.getSimCompetencyObj().getId()) || simCompetencyIdWeightMap.get(scsx.getSimCompetencyObj().getId())==null ? 1 : simCompetencyIdWeightMap.get(scsx.getSimCompetencyObj().getId());
                totalScoresForAvgs += ( comboType.isRaw() ? scsx.rawScore : scsx.scaledScore ) * weight; // scsx.getComboWeight();
                totalWeights += weight; // scsx.getComboWeight();
                countForAvgs++;
                // LogService.logIt( "ComboSimCompetencyScore.updateScoreStats() TTT.1 adding weighted value. isRaw=" + comboType.isRaw() + ", score=" + ( comboType.isRaw() ? scsx.rawScore : scsx.scaledScore ) + ", weight=" + weight + ", scsx.name=" + scsx.getName() + ", this ComboCompetency: " + this.scs.toString() );
                //return;
            }
        }
        catch( Exception e )        
        {
            LogService.logIt( e, "ComboSimCompetencyScore.updateScoreStats() isRaw=" + comboType.isRaw() + ", scsx=" + scsx.toString() + ", this ComboCompetency: " + this.scs.toString() );
        }
        
    }
    
    private float reviseItemScorePerMap( ScorableResponse sr, JsonArray itemScoreMapArray ) throws Exception
    {
        float itemScore = sr.itemScore();
        
        if( itemScoreMapArray==null || itemScoreMapArray.size()<=0 )
            return itemScore;
        
        JsonObject jo;
        String ct5ItemIdentifier=null;
        int seq=0;
        
        for( JsonValue jv : itemScoreMapArray )
        {
            if( !jv.getValueType().equals(JsonValue.ValueType.OBJECT) )
                continue;
            
            jo = (JsonObject)jv;            
            if( !jo.containsKey("ct5itemidentifier") )
            {
                LogService.logIt( "ComboSimCompetencyScore.reviseItemScorePerMap() Map object has not ct5itemidentifier field. " + sr.toString() );
                continue;
            }
            
            ct5ItemIdentifier = JsonUtils.getStringFmJson( jo, "ct5itemidentifier");
            if( !ct5ItemIdentifier.equals( sr.getSimletNodeUniqueId() ) )
                continue;
            seq = jo.containsKey("seq") ? jo.getInt("seq") : 0;
            if( seq>0 && seq!=sr.getCt5ItemPartId())
            {
                LogService.logIt( "ComboSimCompetencyScore.reviseItemScorePerMap() Map object has matching ct5itemidentifier=" + ct5ItemIdentifier + " but non-matching seq=" + seq +", ignoring. sr.getCt5ItemPartId()()=" +sr.getCt5ItemPartId() );
                continue;
            }
            
            LogService.logIt( "ComboSimCompetencyScore.reviseItemScorePerMap() Map object has matching ct5itemidentifier=" + ct5ItemIdentifier + " and seq=" + seq );
            // OK we have the map. So not get the values. 
            String mapVals = JsonUtils.getStringFmJson(jo, "mapvals");            
            if( mapVals == null || mapVals.isBlank() )
                throw new Exception( "ComboSimCompetencyScore.reviseItemScorePerMap() mapvals not found for ct5itemidentifier " + ct5ItemIdentifier + " and seq=" + seq + ", even though there is an entry for this itemId / itempartId combination." );
            
            String[] mapValsArray = mapVals.split(",");
            if( mapValsArray.length<2 )
                throw new Exception( "ComboSimCompetencyScore.reviseItemScorePerMap() mapvals array is less than 2 (minimum) for ct5itemidentifier " + ct5ItemIdentifier + " and seq=" + seq + ", even though there is an entry for this itemId / itempartId combination." );
            
            float high;
            float newItemScore = 0;            
            for( int i=0;i<mapValsArray.length - 1; i+=2 )
            {
                high = Float.parseFloat(mapValsArray[i]);
                newItemScore = Float.parseFloat(mapValsArray[i+1]);
                
                if( itemScore<high )
                {
                    LogService.logIt( "ComboSimCompetencyScore.reviseItemScorePerMap() FFF.1 ct5itemidentifier=" + ct5ItemIdentifier + ", seq=" + seq + " revising itemScore from " + itemScore + " to " + newItemScore );
                    return newItemScore;
                }                
            }
            
            // if we get here, the itemScore is above the highest score in the map.
            LogService.logIt( "ComboSimCompetencyScore.reviseItemScorePerMap() FFF.2 ct5itemidentifier=" + ct5ItemIdentifier + ", seq=" + seq + " revising itemScore from " + itemScore + " to " + newItemScore );
            return newItemScore; 
        }
        
        LogService.logIt( "ComboSimCompetencyScore.reviseItemScorePerMap() No Map object found for ct5itemidentifier " + ct5ItemIdentifier + " and seq=" + seq + ", using original itemscore=" + itemScore );
        return itemScore;
    }

    
    private void updateScoreStatsForScorableResp( ScorableResponse sr, JsonArray itemScoreMapArray) throws Exception
    {
        try
        {
            // LogService.logIt( "ComboSimCompetencyScore.updateScoreStatsForScorableResp() AAAA Updating from " + scsx.getName() + ", scsx.totalCorrect=" + scsx.totalCorrect + ", scsx.totalPoints=" + scsx.totalPoints + ", scsx.totalMaxPoints=" +  scsx.totalMaxPoints + ", scsx.totalScorableItems=" + scsx.totalScorableItems);
            float itemScore = sr.itemScore();
                    
            if( itemScoreMapArray!=null && sr.getCt5ItemId()>0 )
                itemScore = reviseItemScorePerMap( sr, itemScoreMapArray );
            
            scs.totalMaxPoints += sr.getMaxPointsArray()[0]; // scsx.totalMaxPoints;      
            scs.totalScorableItems++; //  += scsx.totalScorableItems;
            scs.totalPoints += itemScore; // sr.itemScore();
            scs.totalCorrect += sr.correct() ? 1 : 0; // scsx.totalCorrect;

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "ComboSimCompetencyScore.updateScoreStatsForScorableResp() BBBB totalCorrect=" + scs.totalCorrect + ", totalPoints=" + scs.totalPoints + ", totalMaxPoints=" +  scs.totalMaxPoints + ", totalScorableItems=" + scs.totalScorableItems );

            // if null, want to use same competencyScoreType as the member.
            // if( scs.competencyScoreType == null )
             //    scs.competencyScoreType = scsx.competencyScoreType;

            // if( scs.competencyScoreType.equals( CompetencyScoreType.AVG_MAX_MINUS_ABS_TRUE_DIFF ) && scs.maxPointsPerItem<scsx.maxPointsPerItem )
            //     scs.maxPointsPerItem = scsx.maxPointsPerItem;

            // if( scsx.scaledScoreCeiling> scs.scaledScoreCeiling )
            //      scs.scaledScoreCeiling = scsx.scaledScoreCeiling;

            // if( scsx.scaledScoreFloor> scs.scaledScoreFloor )
            //     scs.scaledScoreFloor = scsx.scaledScoreFloor;


            // Metascores are always 0 for this type of combo.
            //for( int i=2; i<9; i++ )
            //{
                // LogService.logIt( "SimCompetencyScore adding simletCompetencyScore.metascore[] index=" + i + " value=" + scs.getMetaScore(i));

             //   if( scs.metaScores!=null && scs.metaScores.length>i )
             //       scs.metaScores[i] += sr.getMetaScore(i); // scsx.getMetaScore(i);
            //}        

            countForAvgs++;            
        }
        catch( Exception e )        
        {
            LogService.logIt(e, "ComboSimCompetencyScore.updateScoreStatsForScorableResp() sr=" + sr.toString() + ", this ComboCompetency: " + this.scs.toString() );
        }        
    }


    
    private void updateScoreStatsItemLevel( SimCompetencyScore scsx )
    {
        for( SimletCompetencyScore slcs : scsx.smltCompScrList )
        {
            // totalCorrect += slcs.getTotalCorrect();            
            totalScoresForAvgs += slcs.getTotalPoints();
            countForAvgs += slcs.getTotalScorableItems();
        }        
    }
    
    private void finalizeScoreStatsItemLevel() throws Exception
    {
       // LogService.logIt( "ComboSimCompetencyScore.finalizeScoreStatsItemLevel() totalScores=" + this.totalScoresForAvgs + ", count=" + this.countForAvgs + ", totalCorrect=" + scs.totalCorrect + ", totalPoints=" + scs.totalPoints + ", totalMaxPoints=" +  scs.totalMaxPoints + ", totalScorableItems=" + scs.totalScorableItems);        

        if( countForAvgs>0 )
            scs.hasScoreableData=true;
        else
            return;
        
        // Metascores are always averaged, so complete the average.
        //if( members.size()> 0 )
        //{
        //    for( int i=2; i<6; i++ )
        //    {
        //        scs.metaScores[i] /= ((float) members.size() );
        //    }   
        //}
        
        scs.fractionScoreValue = 0;
        scs.totalScoreValue = 0;

        // ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scs.scoreFormatTypeId );  
        
        // Handle specific cases first.
        if( comboType.equals( SimCompetencyCombinationType.ITEM_LEVEL_ITEMS_CORRECT ) )
        {
            scs.fractionOfPoints =  scs.totalScorableItems > 0 ? ( (float)scs.totalCorrect )/( (float) scs.totalScorableItems) : 0;
            scs.totalScoreValue = scs.totalCorrect;
            scs.fractionScoreValue = scs.fractionOfPoints;            
            scs.rawScore = scs.totalCorrect;
        }

        else if( comboType.equals( SimCompetencyCombinationType.ITEM_LEVEL_SUM_SCORES ) )
        {
            scs.totalScoreValue = totalScoresForAvgs;
            scs.fractionOfPoints = totalScoresForAvgs;
            scs.fractionScoreValue = scs.fractionOfPoints;
            scs.rawScore = totalScoresForAvgs;
        }
        
        else if( comboType.equals( SimCompetencyCombinationType.ITEM_LEVEL_PERCENT_CORRECT ) )
        {
            scs.fractionOfPoints =  scs.totalScorableItems > 0 ? ( (float)scs.totalCorrect )/( (float) scs.totalScorableItems) : 0;
            scs.totalScoreValue = scs.fractionOfPoints;
            scs.fractionScoreValue = scs.fractionOfPoints;     
            scs.rawScore = 100f*scs.fractionOfPoints;
        }
        
        //else if( comboType.equals( SimCompetencyCombinationType.PERCENT_AVAILABLE_POINTS ) )
        //{
        //    scs.fractionOfPoints =  scs.totalMaxPoints > 0 ? ( (float)scs.totalPoints )/( (float) scs.totalMaxPoints) : 0;
        //    scs.totalScoreValue = scs.fractionOfPoints;
        //    scs.fractionScoreValue = scs.fractionOfPoints;     
        //    scs.rawScore = 100f*scs.fractionOfPoints;
        //}
        
        // Next handle general cases
        
        // GENERAL CASE be sure this is after all specific cases
        else if( comboType.isAverage() )
        {
            if( countForAvgs>0 )
            {
                scs.averagePoints = totalScoresForAvgs/ ( totalWeights>0 ? totalWeights : countForAvgs );
                //LogService.logIt( "ComboSimCompetencyScore.finalizeScoreStats() averagePoints=" + scs.averagePoints );
                // if( comboType.equals( SimCompetencyCombinationType.AVERAGE_ITEM_SCORES ) )
                // scs.rawScore = totalScores/count;
            }
            else
                scs.averagePoints = 0;
            
            scs.fractionScoreValue = scs.averagePoints;
            scs.totalScoreValue = scs.averagePoints; 
            scs.rawScore = scs.averagePoints;
        }
        
        else
            throw new Exception( "ComboSimCompetencyScore.finalizeScoreStatsItemLevel() Cannot Process for this SimCompetencyCombinationType: " + comboType.getName() );
                
        // GENERAL CASE be sure this is after all specific cases
        //else if( comboType.isSum() )
        //{
        //    scs.fractionOfPoints = scs.totalMaxPoints > 0 ? totalScoresForAvgs / scs.totalMaxPoints : 0;
        //    scs.totalScoreValue = totalScoresForAvgs;            
        //    scs.fractionScoreValue = scs.fractionOfPoints;            
        //    scs.rawScore = totalScoresForAvgs;
        //}

    }
    

    private void finalizeScoreStats() throws Exception
    {
       //LogService.logIt( "ComboSimCompetencyScore.finalizeScoreStats() totalScores=" + totalScores + ", count=" + count + ", totalWeights=" + totalWeights + ", totalCorrect=" + scs.totalCorrect + ", totalPoints=" + scs.totalPoints + ", totalMaxPoints=" +  scs.totalMaxPoints + ", totalScorableItems=" + scs.totalScorableItems);
        

        if( countForAvgs>0 )
            scs.hasScoreableData=true;
        else
            return;
        
        // Metascores are always averaged, so complete the average.
        if( !members.isEmpty() )
        {
            for( int i=2; i<=9; i++ )
            {
                scs.metaScores[i] /= ((float) members.size() );
            }   
        }
        
        scs.fractionScoreValue = 0;
        scs.totalScoreValue = 0;

        
        // Handle specific cases first.
        if( comboType.equals( SimCompetencyCombinationType.ITEMS_CORRECT ) )
        {
            scs.fractionOfPoints =  scs.totalScorableItems > 0 ? ( (float)scs.totalCorrect )/( (float) scs.totalScorableItems) : 0;
            scs.totalScoreValue = scs.totalCorrect;
            scs.fractionScoreValue = scs.fractionOfPoints;            
            scs.rawScore = scs.totalCorrect;
        }

        else if( comboType.equals( SimCompetencyCombinationType.SUM_ITEM_SCORES ) )
        {
            scs.totalScoreValue = totalScoresForAvgs;
            scs.fractionOfPoints = totalScoresForAvgs;
            scs.fractionScoreValue = scs.fractionOfPoints;
            scs.rawScore = totalScoresForAvgs;
        }
        
        else if( comboType.equals( SimCompetencyCombinationType.PERCENT_CORRECT ) )
        {
            scs.fractionOfPoints =  scs.totalScorableItems > 0 ? ( (float)scs.totalCorrect )/( (float) scs.totalScorableItems) : 0;
            scs.totalScoreValue = scs.fractionOfPoints;
            scs.fractionScoreValue = scs.fractionOfPoints;     
            scs.rawScore = 100f*scs.fractionOfPoints;
        }
        
        else if( comboType.equals( SimCompetencyCombinationType.PERCENT_AVAILABLE_POINTS ) )
        {
            scs.fractionOfPoints =  scs.totalMaxPoints > 0 ? ( (float)scs.totalPoints )/( (float) scs.totalMaxPoints) : 0;
            scs.totalScoreValue = scs.fractionOfPoints;
            scs.fractionScoreValue = scs.fractionOfPoints;     
            scs.rawScore = 100f*scs.fractionOfPoints;
        }

        // Next handle general cases
        
        // GENERAL CASE be sure this is after all specific cases
        else if( comboType.isAverage() )
        {
            if( countForAvgs>0 )
            {
                scs.averagePoints = totalScoresForAvgs/ ( totalWeights>0 ? totalWeights : countForAvgs );
                //LogService.logIt( "ComboSimCompetencyScore.finalizeScoreStats() averagePoints=" + scs.averagePoints );
                // if( comboType.equals( SimCompetencyCombinationType.AVERAGE_ITEM_SCORES ) )
                // scs.rawScore = totalScores/count;
            }
            else
                scs.averagePoints = 0;
            
            scs.fractionScoreValue = scs.averagePoints;
            scs.totalScoreValue = scs.averagePoints; 
            scs.rawScore = scs.averagePoints;
        }
                
        // GENERAL CASE be sure this is after all specific cases
        else if( comboType.isSum() )
        {
            scs.fractionOfPoints = scs.totalMaxPoints > 0 ? totalScoresForAvgs / scs.totalMaxPoints : 0;
            scs.totalScoreValue = totalScoresForAvgs;            
            scs.fractionScoreValue = scs.fractionOfPoints;            
            scs.rawScore = totalScoresForAvgs;
        }
    }

    
    private void calculateRawAndScaledScores() throws Exception
    {
        ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scs.scoreFormatTypeId );  
        
        scs.setSimCompetencyScoreTypes();

        // OK, calculate the raw score
        if( scs.rawScoreCalcType.getIsZScore() )
        {
            float relevantValue =  scs.totalScoreValue;

                // Use the local sim-specific value if present (std >0).
            if( scs.simCompetencyObj.getStddeviation()>0 )
            {
                // LogService.logIt( "ComboSimCompetencyScore.calculateRawAndScaledScores() Calculating Z Score for competency " + scs.getName() + ", using mean/sd=" + scs.simCompetencyObj.getMean() + " , " + scs.simCompetencyObj.getStddeviation() + ", relevantValue=" + relevantValue );
                
                scs.rawScore = (relevantValue-scs.simCompetencyObj.getMean())/scs.simCompetencyObj.getStddeviation();
            }
            
            // this is effectively setting it to 0
            else
                scs.rawScore = -20f; //relevantValue; // scs.simletCompetencyStat.convertToZ( relevantValue );
            
            // scs.rawScore = scs.simletCompetencyStat.convertToZ( relevantValue );
        }
        else
        {
            // do nothing (was set above)
        }

        if( scs.scaledScoreCalcType.getIsTransform() )
        {
            scs.scaledScore = NumberUtils.applyNormToZScore( scs.rawScore, scs.simCompetencyObj.getScaledmean(), scs.simCompetencyObj.getScaledstddeviation() );
        }

        else if( scs.scaledScoreCalcType.getEqualsRawScore() )
        {
            scs.scaledScore = scs.rawScore;
        }

        else
        {
            scs.scaledScore = scoreFormatType.getUnweightedScaledScore(scs.competencyScoreType, scs.rawScore, scs.maxPointsPerItem, scs.simCompetencyObj.getLookuptable() );
            // scaledScore = scoreFormatType.getUnweightedScaledScore( competencyScoreType, rawScore, maxPointsPerItem, simCompetencyObj.getLookuptable() );
        }
        
        if( comboType.isIndividualItemLevel() )
        {
            scs.scaledScoreFloor = scoreFormatType.getMinScoreToGiveTestTaker();
            scs.scaledScoreCeiling = scoreFormatType.getMax();
        }


        if(  scs.scaledScore<scoreFormatType.getMinScoreToGiveTestTaker() )
            scs.scaledScore=scoreFormatType.getMinScoreToGiveTestTaker();

        if(  scs.scaledScore>scoreFormatType.getMaxScoreToGiveTestTaker())
            scs.scaledScore=scoreFormatType.getMaxScoreToGiveTestTaker();
                
        if( 1==2 || ScoreManager.DEBUG_SCORING )
            LogService.logIt( "ComboSimCompetencyScore.calculateRawAndScaledScores() simCompetencyObj=" + scs.simCompetencyObj.getName() +", scoreFormatType=" + scoreFormatType.getKey() + ", rawScore=" + scs.rawScore + ", scaledScore () before ceiling =" + scs.scaledScore + ", scaledScoreCeiling=" + scs.scaledScoreCeiling + ", scs.scaledScoreFloor=" + scs.scaledScoreFloor  ); //  + ", luukup=" +  simCompetencyObj.getLookuptable() );
        
        if( scs.scaledScoreCeiling!=0 && scs.scaledScore > scs.scaledScoreCeiling )
        {
            LogService.logIt( "ComboSimCompetencyScore.calculateRawAndScaledScores() Applying ScaledScoreCeiling " + scs.scaledScoreCeiling + " to score of " + scs.scaledScore );
            scs.scaledScore = scs.scaledScoreCeiling;
        }

        if( scs.scaledScoreFloor!=0 && scs.scaledScore < scs.scaledScoreFloor )
        {
            LogService.logIt( "ComboSimCompetencyScore.calculateRawAndScaledScores() Applying scaledScoreFloor " + scs.scaledScoreFloor + " to score of " + scs.scaledScore );
            scs.scaledScore = scs.scaledScoreFloor;
        }           
    }

    
    @Override
    public String toString() {
        return "ComboSimCompetencyScore{" + "scs=" + scs.toString() +  ", testEventId=" + (scs.testEvent==null ? "null" : scs.testEvent.getTestEventId()) + '}';
    }

    public int getTier() {
        return tier;
    }
    
    
}
