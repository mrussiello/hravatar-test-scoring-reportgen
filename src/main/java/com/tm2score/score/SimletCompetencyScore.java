/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.iactnresp.IactnResp;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.global.Constants;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.iactnresp.IactnItemResp;
import com.tm2score.score.iactnresp.RadioButtonGroupResp;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.DichotomousScoreType;
import com.tm2score.simlet.ScoreCombinationType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class SimletCompetencyScore
{
    protected boolean validItemsCanHaveZeroMaxPoints=false;

    public SimletScore simletScore = null;

    public SimJ.Simlet.Competencyscore competencyScoreObj = null;

    protected CompetencyScoreType competencyScoreType = null;

    protected float totalPoints = 0;

    protected float totalCorrect = 0;
    protected float totalCorrectCountAdjusted = 0;

    protected float totalScorableItems = 0;
    protected float totalScorableItemsCountAdjusted = 0;

    protected int totalValidScorableItems = 0;

    protected float maxPoints = 0;

    protected float maxPointsPerItem = 0;

    protected float averagePoints = 0;

    // extra max points from prevIactnResponses
    // protected float additionalMaxPoints = 0;

    protected float fractionOfPoints = 0;

    protected float fractionCorrect = 0;

    protected ScoreFormatType scoreFormatType = null;

    protected boolean useTotalItemsSimLevel = false;
    protected boolean useTotalItemsSimletCompetencyLevel = false;

    protected float rawScore = 0;

    protected float scaledScore = 0;

    protected float scaledScoreCeiling = 0;
    protected float scaledScoreFloor = 0;

    protected List<String> caveatList = null;
    protected List<String> forceRiskFactorsList = null;

    protected List<InterviewQuestion> scoreTextInterviewQuestionList;

    protected List<MergableScoreObject> mergableScoreObjectList = null;

    protected boolean pendingExternalScores = false;

    /**
     * Used to store score2, score3, etc.
     * metaScores[2] = score 2
     * metaScores[3] = score 3
     * metaScores[4] = score 4
     * metaScores[5] = score 5
     * metaScores[6] = score 6
     * metaScores[7] = score 7
     * metaScores[8] = score 8
     * metaScores[9] = score 9
     * metaScores[10] = score 10
     * metaScores[11] = score 11
     * metaScores[12] = score 12
     */
    float[] metaScores;

    /**
     * map of topic name, int[]
     *    int[0] = number correct
     *    int[1] = number total this topic.
     *    int[2] = number of items that were partially correct.
     *    int[3] = number of items total for this topic from this SimletCompetencyScore
     */
    protected Map<String,int[]> topicMap = null;


    protected List<TextAndTitle> itemScoreTextAndTitleList;


    public SimletCompetencyScore( SimletScore ss, SimJ.Simlet.Competencyscore scsObj, boolean useTotalItemsSimLevel, boolean validItemsCanHaveZeroMaxPoints )
    {
        simletScore = ss;
        competencyScoreObj = scsObj;
        this.useTotalItemsSimLevel = useTotalItemsSimLevel;
        this.validItemsCanHaveZeroMaxPoints = validItemsCanHaveZeroMaxPoints;
        if( scsObj!=null && scsObj.getDichotomousscoretypeid()==DichotomousScoreType.TOTAL_ITEMS.getDichotomousScoreTypeId() )
            useTotalItemsSimletCompetencyLevel=true;

    }

    protected void initForScoring( List<ScorableResponse> irList )
    {
        getCompetencyScoreType();

        totalScorableItems = 0;
        totalScorableItemsCountAdjusted = 0;

        totalValidScorableItems = 0;
        totalCorrect = 0;
        totalCorrectCountAdjusted = 0;
        totalPoints = 0;
        itemScoreTextAndTitleList = null; // new ArrayList<>();

        // first, read the
        if( competencyScoreType.isDichotomous() )
        {}

        else if( competencyScoreType.isPointAccum())
        {
            // LogService.logIt( "SimletCompetencyScore.initForScoring() AAA.0 " + competencyScoreType.getName() + ", useTotalItemsSimLevel=" + useTotalItemsSimLevel + ", useTotalItemsSimletCompetencyLevel=" + useTotalItemsSimletCompetencyLevel + ", competencyScoreObj.getMaxpoints()=" + competencyScoreObj.getMaxpoints());
            if( useTotalItemsSimLevel || useTotalItemsSimletCompetencyLevel )
            {
                maxPoints = competencyScoreObj.getMaxpoints();
            }
        }

        long subCompId;

        // calculate the total scorable items in this simlet for this competency
        if( (useTotalItemsSimLevel || useTotalItemsSimletCompetencyLevel) &&
                ( competencyScoreType.isPointAccum() ||
                  competencyScoreType.isDichotomous()||
                  competencyScoreType.isAvgZscoreDiff() ||
                  competencyScoreType.isTrueDifference() ||
                  competencyScoreType.isScoredEssay() ||
                  competencyScoreType.isDataEntry() ||
                  competencyScoreType.isScoredChat() ||
                  competencyScoreType.isScoredAvUpload() ) )
        {
            for( SimJ.Intn intn : simletScore.intnObjList )
            {
                if( intn.getCompetencyscoreid()==competencyScoreObj.getId() )
                {
                    // If we can score this item multiple times.
                    if( intn.getIncprevselections()==1 && intn.getMaxselections()>1 )
                    {
                        totalScorableItems += intn.getMaxselections();
                        totalScorableItemsCountAdjusted += intn.getMaxselections();
                        //LogService.logIt( "SimletCompetencyScore.initForScoring() AAA.1 sr=" + intn.getUniqueid() + ", Adding weighted Item Count adding max selections=" + intn.getMaxselections() + ", totalScorableItems=" + totalScorableItems  + ", totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted  );
                    }

                    // else just count it once.
                    else
                    {
                        totalScorableItems++;
                        totalScorableItemsCountAdjusted++;
                        //LogService.logIt( "SimletCompetencyScore.initForScoring() AAA.2 sr=" + intn.getUniqueid() + ", Adding 1. totalScorableItems=" + totalScorableItems  + ", totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted  );
                    }


                }

                for( SimJ.Intn.Radiobuttongroup rbg : intn.getRadiobuttongroup() )
                {
                    subCompId = getSubstituteSimCompetencyIdForRbg( irList, intn, rbg );

                    // there is a sub'd competency.
                    if( subCompId> 0 )
                    {
                        // the subbed competency matches.
                        if( subCompId==competencyScoreObj.getId())
                        {
                            totalScorableItems++;
                            totalScorableItemsCountAdjusted++;
                            //LogService.logIt( "SimletCompetencyScore.initForScoring() AAA.3 sr=" + intn.getUniqueid() + ", Adding 1. totalScorableItems=" + totalScorableItems  + ", totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted  );
                        }
                    }

                    // no subbed competency
                    else if( rbg.getCompetencyscoreid()==competencyScoreObj.getId())
                    {
                        // avoid double-counting
                        if( rbg.getRadiobuttongroupid()>0 || intn.getCompetencyscoreid()!=competencyScoreObj.getId())
                        {
                            totalScorableItems++;
                            totalScorableItemsCountAdjusted++;
                            //LogService.logIt( "SimletCompetencyScore.initForScoring() AAA.4 sr=" + intn.getUniqueid() + ", Adding 1. totalScorableItems=" + totalScorableItems  + ", totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted  );
                        }
                    }
                }

                for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
                {
                    if( ii.getCompetencyscoreid()==competencyScoreObj.getId() )
                    {
                        totalScorableItems++;
                        totalScorableItemsCountAdjusted += ii.getCt5Float1()>0 ? ii.getCt5Float1() : 1f;
                        //LogService.logIt( "SimletCompetencyScore.initForScoring() AAA.5 sr=" + intn.getUniqueid() + ", Adding " +  (ii.getCt5Float1()>0 ? ii.getCt5Float1() : 1f) + " totalScorableItems=" + totalScorableItems  + ", totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted  );
                    }
                }
            }
        }

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "SimletCompetencyScore.initForScoring() " + this.competencyScoreObj.getName() + ", totalScorableItems=" + totalScorableItems + ", maxPoints=" + maxPoints + ", irList.size=" + irList.size() + ", useTotalItemsSimLevel=" + useTotalItemsSimLevel + ", useTotalItemsSimletCompetencyLevel=" + useTotalItemsSimletCompetencyLevel );
    }


    public SimCompetencyClass getSimletCompetencyClass()
    {
        if( this.competencyScoreObj==null )
            return null;
        return SimCompetencyClass.getValue( competencyScoreObj.getClassid() );
    }

    protected long getSubstituteSimCompetencyIdForRbg(List<ScorableResponse> irList, SimJ.Intn intn, SimJ.Intn.Radiobuttongroup rbg )
    {
        String c = null;

        for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
        {
            if( !G2ChoiceFormatType.getValue( iitm.getFormat() ).getIsAnyRadio() )
                continue;

            if( iitm.getRadiobuttongroup()!=rbg.getRadiobuttongroupid() )
                continue;

            if( iitm.getTextscoreparam1()==null || iitm.getTextscoreparam1().trim().isEmpty() )
                continue;

            c = IvrStringUtils.getTagValueWithDecode( iitm.getTextscoreparam1(), RadioButtonGroupResp.RBG_COMP_SUBSTITUTION_TAG );

            if( c!=null && !c.trim().isEmpty() )
                break;
        }

        // no subs found.
        if( c==null )
           return 0;

        c = c.trim();

        if( c.isEmpty() )
            return 0;

        RadioButtonGroupResp rbgr;

        IactnItemResp iir;

        for( ScorableResponse sr : irList )
        {
            // Rad But
            if( !(sr instanceof RadioButtonGroupResp) )
               continue;

            rbgr = (RadioButtonGroupResp) sr;

            // wrong interation.
            if( rbgr.getIactnResp().intnObj.getSeq()!= intn.getSeq() )
                continue;

            iir = rbgr.getSelectedIactnItemResp();

            // no selected response.
            if( iir==null )
                continue;

            // wrong rab but group
            if( iir.intnItemObj.getRadiobuttongroup()!=rbg.getRadiobuttongroupid() )
                continue;

            // This competency.
            return rbgr.simletCompetencyId();
        }

        // no sub found.
        return 0;
    }



    /**
     * map of topic name, int[]
     *    int[0] = number correct        ( for this item this means points )
     *    int[1] = number total this topic.  max points for competency
     *    int[2] = number of items that were partially correct.  ( 0 )
     *    int[3] = number of items total that were answered.
     */
    private void addTopicResultsToMap( ScorableResponse sr )
    {
        if( sr==null )
            return;

        if( !SimCompetencyClass.getValue( competencyScoreObj.getClassid() ).getProducesTopics() )
            return;

        if( !competencyScoreType.isDichotomous() && !competencyScoreType.isPointAccum() && !competencyScoreType.isScoredChat())
            return;

        Map<String,int[]> tmx = sr.getTopicMap();

        if( tmx==null )
            return;

        // No other data, use this one.
        // Map<String,int[]>
        if( topicMap==null )
        {
            topicMap = tmx;
            return;
        }

        // Merge with others.
        int[] d1;
        int[] d2;

        for( String k : tmx.keySet() )
        {
            d1=tmx.get(k);

            if( d1==null )
                continue;

            d2=topicMap.get(k);

            if( d2==null )
                d2 = new int[4];

            // add the score number. May need to divide by number of items later on.
            d2[0] += d1[0];

            // This is just a total items counter.
            d2[3]++;

            // add totals for presentation. When add this is typically the number of items. But for others it can be the total points possible.
            if( competencyScoreType.isAddTotalsForTopics() )
                d2[1] += d1[1];
            else
                d2[1] = d1[1];

            // add partials
            d2[2] += d1[2];

            topicMap.put( k, d2 );
        }
    }

    public void calculateScore( List<ScorableResponse> irList, int scoreFormatTypeId, int includeItemScoreTypeId ) throws Exception
    {
        scoreFormatType = ScoreFormatType.getValue( scoreFormatTypeId );

        scaledScoreFloor = scoreFormatType.getMinScoreToGiveTestTaker();
        scaledScoreCeiling = scoreFormatType.getMaxScoreToGiveTestTaker();

        // Sets values such as totalScorableItems
        initForScoring( irList );

        String caveat;
        InterviewQuestion iq;

        caveatList = new ArrayList<>();

        forceRiskFactorsList = new ArrayList<>();

        scoreTextInterviewQuestionList = new ArrayList<>();

        mergableScoreObjectList = new ArrayList<>();

        List<ScorableResponse> irsUsedList = new ArrayList<>();

        //boolean useAutoMaxPoints = maxPoints <= 0;
        float itemPoints;

        TextAndTitle itemScoreTextTitle;

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "SimletCompetencyScore.calculateScore() AAA. Competency=" + competencyScoreObj.getName() + ", responses in irList=" + irList.size() );

        // talley the items
        for( ScorableResponse sr : irList )
        {
            // this could happen either before or after "calculateScore" is called.
            if( sr.isPendingExternalScore() )
            {
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimletCompetencyScore.calculateScore() AAA.2 scorable response is pending scores " + sr.toString() + ", competency=" + this.competencyScoreObj.getName() );
                pendingExternalScores=true;
                continue;
            }

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "SimletCompetencyScore.calculateScore() BBB.1 for item: " + sr.toString() );
            // Tell the item to score itself.
            sr.calculateScore();

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "SimletCompetencyScore.calculateScore() BBB.2 hasValidScore=" + sr.hasValidScore() + ", SimletItemType=" + sr.getSimletItemType().getName() );

            if( sr.isPendingExternalScore() )
            {
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimletCompetencyScore.calculateScore() BBB.3 scorable response is pending scores " + sr.toString() + ", competency=" + this.competencyScoreObj.getName() );
                pendingExternalScores=true;
                continue;
            }

            if( sr.experimental() )
                continue;

            if( !sr.hasValidScore() )
                continue;

            // only include items for this competency.
            // if( sr.simletCompetencyId()!=competencyScoreObj.getId() )  )
            if( !sr.getUsesOrContributesPointsToSimletCompetency(this) ) //&& !sr.measuresSmltCompetency( competencyScoreObj.getId() ) )
            {
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimletCompetencyScore.calculateScore() BBB.4 scorable response doesn't contribute to competency id " + competencyScoreObj.getId() + ", competency=" + this.competencyScoreObj.getName() + ", " + sr.toString() );
                continue;
            }

            // Looks like we will use this response in this SimletCompetencyScore.
            irsUsedList.add( sr );


            // LogService.logIt( "SimletCompetencyScore.calculateScore() " + toString() + ", using scorable response: " + sr.toString() + ", sr.simletCompetencyId=" + sr.simletCompetencyId() );
            mergableScoreObjectList.addAll( sr.getMergableScoreObjects() );

            totalValidScorableItems++;

            if( ScoreManager.DEBUG_SCORING )
               LogService.logIt( "SimletCompetencyScore.calculateScore() BBB.5 Using response for this competency.  totalValidScorableItems=" + totalValidScorableItems + ", irsUsedList=" + irsUsedList.size() );

            if( sr.getFloor()!= 0 && sr.getFloor()<scaledScoreFloor )
                scaledScoreFloor = sr.getFloor();

            if( sr.getCeiling()!=0 && sr.getCeiling()>scaledScoreCeiling )
                scaledScoreCeiling = sr.getCeiling();

            caveat = sr.getCaveatText();

            if( sr.getForcedRiskFactorsList() != null )
                forceRiskFactorsList.addAll( sr.getForcedRiskFactorsList() );

            if( caveat != null && !caveat.isEmpty() )
                caveatList.add( caveat );

            iq = sr.getScoreTextInterviewQuestion();

            if( iq != null )
                this.scoreTextInterviewQuestionList.add( iq );

            // dichotomous scoring
            if( competencyScoreType.isDichotomous() && sr.getSimletItemType().isDichotomous() )
            {
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimletCompetencyScore.calculateScore() XXD.1 scoring dichotomous item. Correct=" + sr.correct()  );

                if( sr.correct() )
                {
                    totalCorrect++;
                    totalCorrectCountAdjusted += sr.getTotalItemCountIncrementValue();
                }

                // Add topic info to map if there is a topic set.
                addTopicResultsToMap( sr );

                // indicates we are using the number of answered items, rather than the gross total number of items in the simlet.
                // Account for MaxPoints even for Dichotomous items.
                if( !useTotalItemsSimLevel && !useTotalItemsSimletCompetencyLevel )
                {
                    float[] mpa = sr.getMaxPointsArray();

                    if( mpa != null )
                        maxPoints += mpa[0];

                    totalScorableItems++;
                    totalScorableItemsCountAdjusted += sr.getTotalItemCountIncrementValue();
                    // LogService.logIt( "SimletCompetencyScore.calculateScore() XXZ.1 Adding weighted Item Count adding " + sr.getTotalItemCountIncrementValue() + ", totalCorrect=" + totalCorrect + ", totalCorrectCountAdjusted=" + totalCorrectCountAdjusted + ", totalScorableItems=" + totalScorableItems  + ", totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted  );

                }


                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimletCompetencyScore.calculateScore() XXD.2 adding " + sr.toString() + ", correct=" +sr.correct() + ", totalCorrect=" + totalCorrect + " out of totalScorableItems=" + totalScorableItems );
            }

            // points accum scoring
            else if( (competencyScoreType.isPointAccum() || competencyScoreType.isDichotomous() ) && sr.getSimletItemType().isPoints()  )
            {
                itemPoints = sr.itemScore();

                if( itemPoints< 0 && competencyScoreObj.getScoringint1() !=1 )
                    itemPoints = 0;

                totalPoints += itemPoints;

                // LogService.logIt( "SimletCompetencyScore.calculateScore() scoring points item. Correct=" + sr.correct() );
                // Even for non-dichotomous items, it is possible that this value would be set if the item is determined to be 'quasi-dichotomous' so record it.
                if( sr.correct() )
                {
                    totalCorrect++;
                    totalCorrectCountAdjusted += sr.getTotalItemCountIncrementValue();
                }

                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimletCompetencyScore.calculateScore() DDD.1 adding Points Accum " + sr.toString() + ", itemScore=" + sr.itemScore() +", totalPoints=" + totalPoints + ", correct=" +sr.correct() + ", totalCorrect=" + totalCorrect + " out of totalScorableItems=" + totalScorableItems );


                // Add topic info to map if there is a topic set.
                addTopicResultsToMap( sr );

                // indicates we are using the number of answered items, rather than the gross total number of items in the simlet.
                if( !useTotalItemsSimLevel && !useTotalItemsSimletCompetencyLevel )
                {
                    float[] mpa = sr.getMaxPointsArray();

                    if( mpa != null )
                        maxPoints += mpa[0];

                    totalScorableItems++;
                    totalScorableItemsCountAdjusted += sr.getTotalItemCountIncrementValue();

                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "SimletCompetencyScore.calculateScore() DDD.2 adding totalScoreItems " + sr.toString() + " maxPoints=" + maxPoints  + ", correct=" +sr.correct() + ", totalCorrect=" + totalCorrect + " out of totalScorableItems=" + totalScorableItems );
                }
            }

            // difference-oriented scoring
            else if( competencyScoreType.isTrueDifference()  )
            {
                boolean isAbsoluteValues = competencyScoreType.equals( CompetencyScoreType.TOTAL_ABS_TRUE_DIFF ) || competencyScoreType.equals( CompetencyScoreType.AVG_MAX_MINUS_ABS_TRUE_DIFF ) ;

                // Mean or true is in scoreparam1,
                float diff = sr.itemScore() - ((Float)sr.getScoreParamsArray()[1]);

                if( isAbsoluteValues )
                    diff = Math.abs(diff);

                if( competencyScoreType.equals( CompetencyScoreType.AVG_MAX_MINUS_ABS_TRUE_DIFF ) )
                {
                    float maxPtsItem = this.competencyScoreObj.getScoringfloat1();

                    if( maxPtsItem <= 0 )
                    {
                        float[] mpa = sr.getMaxPointsArray();

                        if( mpa != null && mpa.length>0 )
                            maxPtsItem = mpa[0];
                    }

                    diff = maxPtsItem - diff;

                    if( maxPtsItem > maxPointsPerItem )
                        maxPointsPerItem = maxPtsItem;
                }

                itemPoints = diff;

                totalPoints += itemPoints;

                // if( itemPoints< 0 && competencyScoreObj.getScoringint1() !=1 )
                //     itemPoints = 0;

                // totalPoints += itemPoints;
                // LogService.logIt( "SimletCompetencyScore.calculateScore() adding Points Accum " + sr.toString() + ", itemScore=" + sr.itemScore() +", totalPoints=" + totalPoints );

                // LogService.logIt( "SimletCompetencyScore.calculateScore() scoring points item. Correct=" + sr.correct() );
                // Even for non-dichotomous items, it is possible that this value would be set if the item is determined to be 'quasi-dichotomous' so record it.
                if( sr.correct() )
                {
                    totalCorrect++;
                    totalCorrectCountAdjusted += sr.getTotalItemCountIncrementValue();
                }

                if( !useTotalItemsSimLevel && !useTotalItemsSimletCompetencyLevel )
                {
                    totalScorableItems++;
                    totalScorableItemsCountAdjusted += sr.getTotalItemCountIncrementValue();
                }

            }


            else if( competencyScoreType.isAvgZscoreDiff()  )
            {
                // HRA SJTs use this method
                boolean isAbsoluteValues = competencyScoreType.equals( CompetencyScoreType.AVG_ABS_ITEM_ZSCORE_DIFF );

                // mean is


                // mean is scoreparam1, sd is scoreparam2
                float mean = ((Float)sr.getScoreParamsArray()[1]); // mean is essentially the true score.
                float sd = ((Float)sr.getScoreParamsArray()[2]);

                float zscore = sd == 0 ? 0 : ( sr.itemScore() - mean ) / sd;

                if( isAbsoluteValues )
                    zscore = Math.abs(zscore);

                itemPoints = zscore;

                totalPoints += itemPoints;

                // if( itemPoints< 0 && competencyScoreObj.getScoringint1() !=1 )
                //     itemPoints = 0;

                // totalPoints += itemPoints;
                // LogService.logIt( "SimletCompetencyScore() adding Points Accum " + sr.toString() + ", itemScore=" + sr.itemScore() +", totalPoints=" + totalPoints );

                // LogService.logIt( "SimletCompetencyScore.calculateScore() scoring points item. Correct=" + sr.correct() );
                // Even for non-dichotomous items, it is possible that this value would be set if the item is determined to be 'quasi-dichotomous' so record it.
                if( sr.correct() )
                {
                    totalCorrect++;
                    totalCorrectCountAdjusted += sr.getTotalItemCountIncrementValue();
                }

                if( !useTotalItemsSimLevel && !useTotalItemsSimletCompetencyLevel )
                {
                    totalScorableItems++;
                    totalScorableItemsCountAdjusted += sr.getTotalItemCountIncrementValue();
                }

            }

            // Typing
            else if( (  (competencyScoreType.isTypingSpeedAccuracy() && sr.getSimletItemType().isTyping()) ||
                        (competencyScoreType.isDataEntry() && sr.getSimletItemType().isDataEntry())  ) &&
                    sr.hasValidScore()  )
            {
                itemPoints = sr.itemScore();

                if( itemPoints< 0 && competencyScoreObj.getScoringint1() !=1 )
                    itemPoints = 0;

                totalPoints += itemPoints;

                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimletCompetencyScore() adding typing/data entry item " + sr.toString() + ", itemScore=" + sr.itemScore() +", totalPoints=" + totalPoints );

                // indicates we are using the number of answered items, rather than the gross total number of items in the simlet.
                if( !useTotalItemsSimLevel && !useTotalItemsSimletCompetencyLevel )
                {
                    float[] mpa = sr.getMaxPointsArray();

                    if( mpa != null )
                        maxPoints += mpa[0];

                    totalScorableItems++;
                    totalScorableItemsCountAdjusted += sr.getTotalItemCountIncrementValue();
                }
            }

            // Scored Essay or Audio or chat
            else if( (( competencyScoreType.isScoredChat() && sr.getSimletItemType().isAutoChat() ) ||
                      ( competencyScoreType.isScoredEssay() && sr.getSimletItemType().isAutoEssay() ) ||
                      (competencyScoreType.isScoredVoiceSample()&& sr.getSimletItemType().isAutoAudio()) ||
                       ( competencyScoreType.isScoredAvUpload() && sr.getSimletItemType().isAutoAvUpload() ) ||
                      (competencyScoreType.isIdentityImageCapture()&& sr.getSimletItemType().isImageCapture())) &&
                      sr.hasValidScore()  )
            {
                itemPoints = sr.itemScore();

                if( itemPoints< 0 && competencyScoreObj.getScoringint1() !=1 )
                    itemPoints = 0;

                totalPoints += itemPoints;

                // Add topic info to map if there is a topic set.
                addTopicResultsToMap( sr );


                //if( ScoreManager.DEBUG_SCORING )
                // LogService.logIt( "SimletCompetencyScore() Essay or media or chat adding " + sr.toString() + ", itemScore=" + sr.itemScore() +", totalPoints=" + totalPoints );

                // indicates we are using the number of answered items, rather than the gross total number of items in the simlet.
                if( !useTotalItemsSimLevel && !useTotalItemsSimletCompetencyLevel )
                {
                    float[] mpa = sr.getMaxPointsArray();

                    if( mpa != null )
                        maxPoints += mpa[0];

                    totalScorableItems++;
                    totalScorableItemsCountAdjusted += sr.getTotalItemCountIncrementValue();
                }
            }

            if( includeItemScoreTypeId>0 )
            {
                itemScoreTextTitle = sr.getItemScoreTextTitle( includeItemScoreTypeId );

                //if( ScoreManager.DEBUG_SCORING )
                // LogService.logIt( "SimletCompetencyScore.calculateScore() sr=" + sr.getSimletNodeUniqueId() + ", ItemScoreTextTitle is " + (itemScoreTextTitle==null ? "null" : "not null " + itemScoreTextTitle.getTitle() )  );

                if( itemScoreTextTitle != null )
                {
                    if( itemScoreTextAndTitleList == null )
                        itemScoreTextAndTitleList = new ArrayList<>();

                    itemScoreTextAndTitleList.add( itemScoreTextTitle );
                }
            }
        }

        if( pendingExternalScores )
            return;


        //if( scaledScoreFloor != 0 || scaledScoreCeiling != 0 )
        //    LogService.logIt( "SimletCompetencyScore.calculateScore() scaledScoreFloor=" + scaledScoreFloor + ", scaledScoreCeiling=" + scaledScoreCeiling );

        // LogService.logIt( "SimletCompetencyScore.calculateScore() GGG.1 maxPoints=" + maxPoints + ", totalCorrect=" + totalCorrect + ", totalValidScorableItems=" + totalValidScorableItems + ", totalScorableItems=" + totalScorableItems );

        // maxPoints += additionalMaxPoints;

        // next, calculate the score
        if( competencyScoreType.isDichotomous() )
        {
            if( competencyScoreObj.getDichotomousscoretypeid() == DichotomousScoreType.RQD_ITEMS.getDichotomousScoreTypeId() )
                totalScorableItems = competencyScoreObj.getRqditems() >= totalCorrect ? competencyScoreObj.getRqditems() : totalCorrect;

            // Note that this value incorporates multiple randomly-selected items on a single clip.
            if( competencyScoreObj.getDichotomousscoretypeid() == DichotomousScoreType.TOTAL_ITEMS.getDichotomousScoreTypeId() )
                totalScorableItems = competencyScoreObj.getTotalitems() >= totalCorrect ? competencyScoreObj.getTotalitems() : totalCorrect;

            if( totalScorableItems>0 && totalScorableItemsCountAdjusted>0 && totalScorableItems!=(int)totalScorableItemsCountAdjusted && Math.abs( totalScorableItems-totalScorableItemsCountAdjusted)>0.2f)
            {
                LogService.logIt( "SimletCompetencyScore.calculateScore() Using totalCorrectCountAdjusted=" + totalCorrectCountAdjusted + " and totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted + " in fractionCorrect calculation. totalCorrect=" + totalCorrect + ", totalScorableItems=" + totalScorableItems + ", CompetencyScoreType=" + competencyScoreType.getName() );
                fractionCorrect = totalCorrectCountAdjusted/totalScorableItemsCountAdjusted;
            }
            else
            {
                fractionCorrect = totalScorableItems > 0 ? ( (float)totalCorrect )/( (float)totalScorableItems) : 0;
            }
            // Apply any ceilings or floors
            if( fractionCorrect > competencyScoreObj.getMaxscore() && competencyScoreObj.getMaxscore()>0 && competencyScoreObj.getMaxscore()<=1.0 )
                fractionCorrect = competencyScoreObj.getMaxscore();

            if( fractionCorrect < competencyScoreObj.getMinscore() && competencyScoreObj.getMinscore()>0 && competencyScoreObj.getMinscore()<=1.0 )
                fractionCorrect = competencyScoreObj.getMinscore();
        }

        else if( competencyScoreType.isPointAccum() )
        {
            fractionOfPoints = maxPoints > 0 ? totalPoints/maxPoints : 0;

            averagePoints = totalScorableItems > 0 ? totalPoints/totalScorableItems : 0;

            // Apply any ceilings or floors
            if( fractionOfPoints > competencyScoreObj.getMaxscore() && competencyScoreObj.getMaxscore()>0 && competencyScoreObj.getMaxscore()<=1.0 )
                fractionOfPoints = competencyScoreObj.getMaxscore();

            if( fractionOfPoints < competencyScoreObj.getMinscore() && competencyScoreObj.getMinscore()>0 && competencyScoreObj.getMinscore()<=1.0 )
                fractionOfPoints = competencyScoreObj.getMinscore();
        }

        else if( competencyScoreType.isTrueDifference()  )
        {
            fractionOfPoints = maxPoints > 0 ? totalPoints/maxPoints : 0;

            averagePoints = totalScorableItems > 0 ? totalPoints/totalScorableItems : 0;
        }

        else if( competencyScoreType.isAvgZscoreDiff() )
        {
            fractionOfPoints = 0; // maxPoints > 0 ? totalPoints/maxPoints : 0;

            // this is the average z-score value or abs(z-score) value
            averagePoints = totalScorableItems > 0 ? totalPoints/totalScorableItems : 0;
        }

        else if( competencyScoreType.isTypingSpeedAccuracy() || competencyScoreType.isDataEntry() || competencyScoreType.isScoredEssay() || competencyScoreType.isScoredChat() || competencyScoreType.isScoredVoiceSample()|| competencyScoreType.isScoredAvUpload()|| competencyScoreType.isIdentityImageCapture())
        {
             averagePoints = totalScorableItems > 0 ? totalPoints/totalScorableItems : 0;
        }

        if( scoreFormatType == null )
            scoreFormatType = ScoreFormatType.NUMERIC_1_TO_5;

        if( competencyScoreType.isAverage() )
            rawScore = averagePoints;

        else if( competencyScoreType.isTypingSpeedAccuracy() ||competencyScoreType.isDataEntry() || competencyScoreType.isScoredEssay() || competencyScoreType.isScoredChat() || competencyScoreType.isScoredVoiceSample()  || competencyScoreType.isScoredAvUpload() || competencyScoreType.isIdentityImageCapture() )
        {
            rawScore = averagePoints;
        }

        else
            rawScore = scoreFormatType.getUnweightedRawScore(   competencyScoreType,
                                                                competencyScoreType.isPointAccum() ? fractionOfPoints : fractionCorrect,
                                                                competencyScoreType.isPointAccum() ? totalPoints : totalCorrect,
                                                                competencyScoreObj.getStddeviation(),
                                                                competencyScoreObj.getMeanscore() );


        scaledScore = scoreFormatType.getUnweightedScaledScore(competencyScoreType, rawScore, 0, null );

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "SimletCompetencyScore.calculateScore() FINAL " + competencyScoreObj.getName() + ", totalPoints=" + totalPoints + ", fractionPoints=" + this.fractionOfPoints + ", totalCorrect=" + this.totalCorrect + ", fractionCorrect=" + this.fractionCorrect + ",  totalScorableItems=" + totalScorableItems + ", averagePoints=" + averagePoints + ", scaledScoreFloor=" + scaledScoreFloor + ", scaledScoreCeiling=" + scaledScoreCeiling  );

        combineMetaScores( irsUsedList ); // irList );

        completeTopicMap();
    }


    /**
     * This method is intended to adjust the total items per topic in cases where total items are to be used, or the competency score type is dichotomous and total or required items are set to be used.
     */
    protected void completeTopicMap()
    {
        // neither should ever happen
        if( competencyScoreObj==null || competencyScoreType==null )
        {
            LogService.logIt( "SimletCompetencyScore.completeTopicMap() Missing an object. competencyScoreObj: " + (competencyScoreObj!=null) + ", competencyScoreType: " + (competencyScoreType!=null) );
            return;
        }

        if( !useTotalItemsSimLevel && !useTotalItemsSimletCompetencyLevel && (!competencyScoreType.isDichotomous() || ( competencyScoreObj.getDichotomousscoretypeid() != DichotomousScoreType.TOTAL_ITEMS.getDichotomousScoreTypeId() && competencyScoreObj.getDichotomousscoretypeid() != DichotomousScoreType.RQD_ITEMS.getDichotomousScoreTypeId()  ) ) )
            return;

        // If zero items were answered, there will be no Topic Map. So create it.
        if( topicMap==null )
            topicMap = new HashMap<>();

        Map<String,Integer> allItemsTopicMap = new HashMap<>();

        boolean requiredOnly = competencyScoreType.isDichotomous() && competencyScoreObj.getDichotomousscoretypeid()==DichotomousScoreType.RQD_ITEMS.getDichotomousScoreTypeId();

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "SimletCompetencyScore.completeTopicMap() Starting for " + competencyScoreObj.getName() + ", requiredOnly=" + requiredOnly + ", totalScorableItems=" + totalScorableItems );

        int totalItemsThatCanBeExposedThisCompetency = this.competencyScoreObj.getTotalitems();
        int totalItemsMatchingCompetency = 0;
        for( SimJ.Intn intn : simletScore.intnObjList )
        {
            // Competency match. Now check Topic.
            if( intn.getCompetencyscoreid()>0 && intn.getCompetencyscoreid()==competencyScoreObj.getId() )
            {
                totalItemsMatchingCompetency++;
                // Since competency defined at interaction level don't count anything else (like radio button groups)
                continue;
            }

            for( SimJ.Intn.Radiobuttongroup rbg : intn.getRadiobuttongroup() )
            {
                // Competency match. Now check Topic.
                if( rbg.getCompetencyscoreid()==competencyScoreObj.getId())
                    totalItemsMatchingCompetency++;
            }

            for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
            {
                // Competency match. Now check Topic.
                if( ii.getCompetencyscoreid()==competencyScoreObj.getId() )
                    totalItemsMatchingCompetency++;
            }
        }

        // adjust for total number of items that can actually be exposed.
        // This assumes that any clip with multiple randomized items has both items with the same topic.
        int itemsPerClip = 1;
        if( !requiredOnly && totalItemsThatCanBeExposedThisCompetency < totalItemsMatchingCompetency )
        {
            float itemRatio = totalItemsThatCanBeExposedThisCompetency>0 ? ((float)totalItemsMatchingCompetency)/((float)totalItemsThatCanBeExposedThisCompetency) : 1000;
            itemsPerClip = Math.round(itemRatio);

            // LogService.logIt( "SimletCompetencyScore.completeTopicMap() totalItemsThatCanBeExposedThisCompetency=" + totalItemsThatCanBeExposedThisCompetency + ", totalItemsMatchingCompetency=" + totalItemsMatchingCompetency + ", itemRatio=" + itemRatio + ", itemsPerClip=" + itemsPerClip );
        }


        for( SimJ.Intn intn : simletScore.intnObjList )
        {
            if( requiredOnly && intn.getRqdcompletion()!=1 )
                continue;

            // Competency match. Now check Topic.
            if( intn.getCompetencyscoreid()>0 && intn.getCompetencyscoreid()==competencyScoreObj.getId() )
            {
                processTopicTextScoreParam1( intn.getTextscoreparam1(), allItemsTopicMap );

                // Since competency defined at interaction level don't count anything else (like radio button groups)
                continue;
            }

            for( SimJ.Intn.Radiobuttongroup rbg : intn.getRadiobuttongroup() )
            {
                // Competency match. Now check Topic.
                if( rbg.getCompetencyscoreid()==competencyScoreObj.getId())
                    processTopicTextScoreParam1( rbg.getTextscoreparam1(), allItemsTopicMap );
            }

            for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
            {
                // Competency match. Now check Topic.
                if( ii.getCompetencyscoreid()==competencyScoreObj.getId() )
                    processTopicTextScoreParam1( ii.getTextscoreparam1(), allItemsTopicMap );
            }
        }

        int[] topicVals;
        Integer topicCount;
        for( String topic : allItemsTopicMap.keySet() )
        {
            topicCount = allItemsTopicMap.get( topic );
            if( topicCount<=0 )
                continue;

            // if items per clip is more than 1, we need to adjust.
            if( itemsPerClip>1 )
            {
                topicCount = (int) topicCount/itemsPerClip;

                if( topicCount<=0 )
                    continue;
            }

            topicVals = topicMap.get( topic );

            if( topicVals==null )
                topicVals=new int[4];

            topicVals[1] = topicCount;
            topicVals[3] = topicCount;

            topicMap.put( topic, topicVals );
        }
    }

    private void processTopicTextScoreParam1( String tsp, Map<String,Integer> allItemsTopicMap )
    {
        String topic = null;

        // no topic tag for this item.
        if( tsp!=null && !tsp.trim().isEmpty() )
            topic = IvrStringUtils.getTagValueWithDecode(tsp, Constants.TOPIC_KEY );

        // No topic tag
        if( topic==null || topic.trim().isEmpty() )
            topic = "NOTOPIC";

        // increment count
        topic = topic.trim();
        Integer topicCount = allItemsTopicMap.get(topic);
        if( topicCount==null )
            topicCount = (int)(0);
        topicCount++;
        allItemsTopicMap.put( topic, topicCount );
    }

    public boolean isPendingExternalScores()
    {
        return pendingExternalScores;
    }


    protected void combineMetaScores( List<ScorableResponse> irl )
    {
        if( competencyScoreObj.getScore2Combinationtype()>0 )
            combineMetaScores( irl, 2, competencyScoreObj.getScore2Combinationtype() );

        if( competencyScoreObj.getScore3Combinationtype()>0 )
            combineMetaScores( irl, 3 , competencyScoreObj.getScore3Combinationtype() );

        if( competencyScoreObj.getScore4Combinationtype()>0 )
            combineMetaScores( irl, 4 , competencyScoreObj.getScore4Combinationtype() );

        if( competencyScoreObj.getScore5Combinationtype()>0 )
            combineMetaScores( irl, 5 , competencyScoreObj.getScore5Combinationtype() );

        if( competencyScoreObj.getScore6Combinationtype()>0 )
            combineMetaScores( irl, 6 , competencyScoreObj.getScore6Combinationtype() );

        if( competencyScoreObj.getScore7Combinationtype()>0 )
            combineMetaScores( irl, 7 , competencyScoreObj.getScore7Combinationtype() );

        if( competencyScoreObj.getScore8Combinationtype()>0 )
            combineMetaScores( irl, 8 , competencyScoreObj.getScore8Combinationtype() );

        if( competencyScoreObj.getScore9Combinationtype()>0 )
            combineMetaScores( irl, 9 , competencyScoreObj.getScore9Combinationtype() );

        if( competencyScoreObj.getScore10Combinationtype()>0 )
            combineMetaScores( irl, 10 , competencyScoreObj.getScore10Combinationtype() );

        if( competencyScoreObj.getScore11Combinationtype()>0 )
            combineMetaScores( irl, 11 , competencyScoreObj.getScore11Combinationtype() );

        if( competencyScoreObj.getScore12Combinationtype()>0 )
            combineMetaScores( irl, 12 , competencyScoreObj.getScore12Combinationtype() );
    }

    protected void combineMetaScores( List<ScorableResponse> irl, int index, int scoreCombinationTypeId )
    {
        if( metaScores==null )
            metaScores = new float[13];

        metaScores[index] = ScoreCombinationType.getValue( scoreCombinationTypeId ).combineScores( irl , index );

        // LogService.logIt( "SimletCompetencyScore.combineMetaScores() XXX.1 " + competencyScoreObj.getName() + ", combining for index=" + index + " irl.size()=" + irl.size() + ", value=" + metaScores[index] );

    }


    public float getMetaScore( int idx )
    {
        if( metaScores==null || metaScores.length<idx+1 )
            return 0;

        return metaScores[idx];
    }


    public float getCoverageWeight()
    {
        if( competencyScoreObj.getCoverage() <= 0 )
            return 1;

        return competencyScoreObj.getCoverage();
    }


    public float getCoverageWeightedScaledScore()
    {
        if( competencyScoreObj.getCoverage() <= 0 )
            return getUnweightedScaledScore();

        return competencyScoreObj.getCoverage() * getUnweightedScaledScore();
    }

    public float getRawScore()
    {
        return rawScore;

        //if( competencyScoreType.isAverage() )
       //     return averagePoints;

        //if( competencyScoreType.getIsNormScale() || competencyScoreType.isRawTotal() )
        //    return competencyScoreType.isPointAccum() ? totalPoints : totalCorrect;

        //return competencyScoreType.isPointAccum() ? fractionOfPoints : fractionCorrect;

    }


    public float getUnweightedScaledScore()
    {
        return scaledScore;

        //if( !hasDataToAutoScore() )
        //    return 0;

        //if( scoreFormatType == null )
        //    scoreFormatType = ScoreFormatType.NUMERIC_1_TO_5;

        //if( competencyScoreType.isAverage() )
        //    return averagePoints;

        //return scoreFormatType.getUnweightedScore(  competencyScoreType,
        //                                            competencyScoreType.isDichotomous() ? fractionCorrect : fractionOfPoints,
        //                                            competencyScoreType.isDichotomous() ? totalCorrect : totalPoints,
        //                                            competencyScoreObj.getStddeviation(),
        //                                            competencyScoreObj.getMeanscore());
    }


    public boolean hasDataToAutoScore()
    {
        // LogService.logIt( "SimletCompetencyScore hasDataToAutoScore() totalValidScorableItems=" + totalValidScorableItems + ", totalScorableItems=" + totalScorableItems + ", name=" + this.simletScore.simletObj.getName() );
        return totalValidScorableItems>0 && totalScorableItems>0;
    }


    public CompetencyScoreType getCompetencyScoreType()
    {
        if( competencyScoreType == null )
            competencyScoreType = competencyScoreObj == null ? CompetencyScoreType.NONE : CompetencyScoreType.getValue( competencyScoreObj.getScoretype() );

        // LogService.logIt( "SimletCompetencyScore.getCompetencyScoreType() id=" + competencyScoreObj.getScoretype() + ", name=" + competencyScoreType.getName() );

        return competencyScoreType;
    }

    @Override
    public String toString() {
        return "SimletCompetencyScore{" + "simlet=" + simletScore.simletObj.getName() + ", competencyScoreObj=" + (competencyScoreObj==null ? "null" : competencyScoreObj.getName() + " competencyScoreObj.id=" + competencyScoreObj.getId() )  + '}';
    }

    public List<TextAndTitle> getTextBasedResponses( List<ScorableResponse> allResponses, boolean includeNonCompetencyQs )
    {
        List<TextAndTitle> out = new ArrayList<>();

        for( ScorableResponse ir : allResponses )
        {

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "SimletCompetencyScore.getTextResponses() AAA " + this.toString() + ", " + ir.toString() + " ir.simletCompetencyId()=" + ir.simletCompetencyId() + " competencyScoreObj.getId()=" + competencyScoreObj.getId() );

            // same competency
            if( ir.simletCompetencyId() != competencyScoreObj.getId() )
                continue;

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "SimletCompetencyScore.getTextResponses() BBB ir.getSimletItemType()=" + ir.getSimletItemType().getName() );

            // If this IactnResp doesn't allow supplementary TextAndTitle, and it's not a Manual Text item, skip it.
            // allowsSupplementaryCompetencyLevelTextAndTitle only returns true for IvrIactnResponses type (4,6,8 = DTMF input items) right now. SimletItemType must be a Manual Text, AV Upload, or Manual Upload.
            // this means must have a simlet competency id present.
            if( !ir.allowsSupplementaryCompetencyLevelTextAndTitle() && !ir.getSimletItemType().supportsManualTextTitle() )
                continue;

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "SimletCompetencyScore.getTextResponses() CCC " );

            // if no non-competency question types and this is a non competency question ...
            // includeNonCompetencyQs is always false right now. would never get here since noncompetencyid is 0 if competencyid>0
            if( ir instanceof IactnResp && !includeNonCompetencyQs && ((IactnResp)ir).intnObj.getNoncompetencyquestiontypeid() > 0 )
                continue;

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "SimletCompetencyScore.getTextResponses() DDD adding " + ir.getTextAndTitleList().size() + " text-based responses." );

            // So, this only collects text/titles that are either AvItemTypes (4,6,8 - DTMF inputs), or (Manual Text or AV Upload or Manual Upload) simletItemTypes. No NonCompetencyItemTypes.

            out.addAll( ir.getTextAndTitleList() );
        }

        return out;
    }

    public float getTotalPoints() {
        return totalPoints;
    }

    public float getTotalCorrect() {
        return totalCorrect;
    }

    public float getTotalScorableItems() {
        return totalScorableItems;
    }

    public float getMaxPoints() {
        return maxPoints;
    }

    //public float getAdditionalMaxPoints() {
    //    return additionalMaxPoints;
    //}

    public float getAveragePoints() {
        return averagePoints;
    }

    public float getScaledScoreCeiling() {
        return scaledScoreCeiling;
    }

    public float getScaledScoreFloor() {
        return scaledScoreFloor;
    }

    public List<String> getCaveatList() {
        return caveatList;
    }

    public List<InterviewQuestion> getScoreTextInterviewQuestionList() {
        return scoreTextInterviewQuestionList;
    }

    public TestEvent getTestEvent()
    {
        return simletScore == null ? null : simletScore.getTestEvent();
    }

    public List<String> getForceRiskFactorsList() {
        return forceRiskFactorsList;
    }

    public boolean getValidItemsCanHaveZeroMaxPoints() {
        return validItemsCanHaveZeroMaxPoints;
    }

    public void setValidItemsCanHaveZeroMaxPoints(boolean validItemsCanHaveZeroMaxPoints) {
        this.validItemsCanHaveZeroMaxPoints = validItemsCanHaveZeroMaxPoints;
    }

    public float getMaxPointsPerItem() {
        return maxPointsPerItem;
    }

    public List<MergableScoreObject> getMergableScoreObjectList() {
        return mergableScoreObjectList == null ? new ArrayList<>() : mergableScoreObjectList;
    }

    public Map<String, int[]> getTopicMap() {
        return topicMap;
    }

    public List<TextAndTitle> getItemScoreTextAndTitleList() {
        return itemScoreTextAndTitleList;
    }

    public float getTotalCorrectCountAdjusted() {
        return totalCorrectCountAdjusted;
    }

    public float getTotalScorableItemsCountAdjusted() {
        return totalScorableItemsCountAdjusted;
    }



}
