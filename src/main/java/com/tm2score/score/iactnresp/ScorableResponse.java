/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2score.entity.event.ItemResponse;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.SimletItemType;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mike
 */
public interface ScorableResponse
{
    void calculateScore() throws Exception;

    boolean correct();

    int getCt5SubtopicId();
    

    
    boolean getPartialCreditAssigned();
    
    String getTopic();

    /**
     * map of topic name, int[]
     *    int[0] = number correct
     *    int[1] = number total this topic.
     *    int[2] = number of items that were partially correct.
     *    int[3] = total number of items this topic. 
     */
    Map<String,int[]> getTopicMap();

    boolean experimental();
    
    String getExtItemId();
            
    String getSelectedExtPartItemIds();
            

    float getAggregateItemScore( SimCompetencyClass simCompetencyClass );

    List<IactnItemResp> getAllScorableIntItemResponses();

    /**
     * Returns null - unless the scorableresponse has a textScoreParam1 of [SCORETEXTCAVEAT]value sentence  -  where value sentence is a sentence that should be appended to
     * the scoretext for this competency in any report.
     * @return
     */
    // String getCaveatText();
    
    List<CaveatScore> getCaveatScoreList();


    /**
     * A list of Strings that will bubble up into an overall list of forced risk factors.
     *
     * @return
     */
    List<String> getForcedRiskFactorsList();


    /**
     * Returns - unless the scorableresponse has a textScoreParam1 of [SCALEDSCORECEILING]value  where value is the max value for the scaled competency score that is to be
     * enforced based on the fact that this person made this selection.
     * @return
     */
    float getCeiling();

    float getDisplayOrder();


    List<TextAndTitle> getTextAndTitleList();
    
    TextAndTitle getItemScoreTextTitle( int includeItemScoreTypeId );

    int getCt5ItemId();
    
    int getCt5ItemPartId();
    
    
    /**
     * Returns - unless the scorableresponse has a textScoreParam1 of [SCALEDSCOREFLOOR]value  where value is the min value for the scaled competency score that is to be
     * enforced based on the fact that this person made this selection.
     * @return
     */
    float getFloor();

    float[] getMaxPointsArray();
    
    /**
     * Returns an array of
     * data[0] = itemScore         (Float)
     * data[1] = scoreParam1       (Float)
     * data[2] = scoreParam2       (Float)
     * data[3] = scoreParam3       (Float)
     * data[10] = textScoreParam1  (String)
     * 
     * @return 
     */
    Object[] getScoreParamsArray();

    float getMetaScore( int index);

    float getResponseTime();

    InterviewQuestion getScoreTextInterviewQuestion();

    SimletItemType getSimletItemType();

    long getSimletNodeId();

    int getSimletNodeSeq();

    String getSimletNodeUniqueId();

    boolean hasMetaScore( int index );

    boolean hasValidScore();

    boolean isAutoScorable();

    boolean isPendingExternalScore();

    boolean isScoredDichotomouslyForTaskCalcs();

    float itemScore();

    boolean measuresSmltCompetency( long simletCompetencyId );

    void populateItemResponse( ItemResponse ir );

    boolean requiresMaxPointIncrement();

    boolean saveAsItemResponse();

    long getSimletActId();

    long simletCompetencyId();

    long simCompetencyId();
    
    long simletId();

    int simletItemTypeId();

    int simletVersionId();
    
    List<MergableScoreObject> getMergableScoreObjects();
    
    boolean allowsSupplementaryCompetencyLevelTextAndTitle();
    
    /*
      Normally should return 1 unless the response has a weightfortotalitemcount set to something above 0 and other than 1.
    */
    float getTotalItemCountIncrementValue();
    
    boolean getUsesOrContributesPointsToSimletCompetency( SimletCompetencyScore smltCs );

}
