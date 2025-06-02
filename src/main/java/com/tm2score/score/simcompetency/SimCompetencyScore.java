/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.simcompetency;

import com.tm2score.global.WeightedObject;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.service.LogService;
import com.tm2score.sim.InteractionScoreUtils;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyScoreCalculationType;
import com.tm2score.sim.SimCompetencyType;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2builder.sim.xml.InterviewQuestionObj;
import com.tm2builder.sim.xml.SimJ;
import com.tm2builder.sim.xml.SimJ.Simcompetency;
import com.tm2score.entity.profile.ProfileEntry;
import com.tm2score.global.DisplayOrderObject;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.UserRankObject;
import com.tm2score.report.ReportData;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.TopicComparator;
import com.tm2score.sim.CategoryDistType;
import com.tm2score.sim.SimCompetencyRawScoreCalcType;
import com.tm2score.sim.SimCompetencyScaledScoreCalcType;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import jakarta.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Mike
 */
public class SimCompetencyScore implements WeightedObject, DisplayOrderObject, UserRankObject, Comparable<SimCompetencyScore>
{
    protected ReportData reportData = null;

    protected boolean validItemsCanHaveZeroMaxPoints = false;

    protected SimJ.Simcompetency simCompetencyObj = null;

    protected TestEvent testEvent = null;


    protected List<SimletCompetencyScore> smltCompScrList;

    protected float scaledScore = 0;

    protected float rawScore = 0;

    protected float totalScoreValue = 0;
    protected float fractionScoreValue = 0;

    protected boolean hasScoreableData = false;
    protected boolean boolean1 = false;

    protected List<TextAndTitle> textBasedResponseList = null;

    protected List<InterviewQuestion> interviewQuestionList;

    protected float weightUsed = 0;
    // protected float comboWeight = 0;

    protected SimCompetencyClass simCompetencyClass;

    protected float totalCorrect = 0;
    //protected int totalCorrectCountAdjusted = 0;

    protected float totalScorableItems = 0;
    //protected int totalScorableItemsCountAdjusted = 0;

    protected List<TextAndTitle> itemScoreTextAndTitleList;

    protected float totalPoints = 0;

    protected float fractionCorrect = 0;

    protected float fractionOfPoints = 0;

    protected float averagePoints = 0;

    protected float totalMaxPoints = 0;

    protected float scaledScoreCeiling = 0;

    protected float scaledScoreFloor = 0;

    protected float maxPointsPerItem = 0;

    protected List<String> caveatList;

    protected List<String> forcedRiskFactorsList;

    protected List<InterviewQuestion> scoreTextInterviewQuestionList;

    protected List<MergableScoreObject> mergableScoreObjectList = null;

    // 0-3, 1-5, or 0-100 or 'other'
    protected int scoreFormatTypeId = 0;

    protected int scoreColorSchemeTypeId = 0;

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
    protected float[] metaScores;

    protected CompetencyScoreType competencyScoreType = null;

    /**
     * This indicates that the total number of items should be used rather than just the items that were answered,
     * when scoring sim competencies.
     */
    protected boolean useTotalItems = false;

    protected boolean pendingExternalScores = false;

    protected ProfileEntry profileEntry;

    // protected SimletCompetencyStat simletCompetencyStat;

    protected SimCompetencyRawScoreCalcType rawScoreCalcType;
    protected SimCompetencyScaledScoreCalcType scaledScoreCalcType;

    protected JsonObject scJo=null;


    public SimCompetencyScore( SimJ.Simcompetency sc, TestEvent te, boolean useTotalItems, boolean validItemsCanHaveZeroMaxPoints )
    {
        this.simCompetencyObj = sc;

        // LogService.logIt( "SimCompetencyScore() " + sc.getName() + ", interview questions: " + sc.getInterviewquestion().size() );

        this.testEvent = te;

        this.useTotalItems = useTotalItems;

        this.validItemsCanHaveZeroMaxPoints = validItemsCanHaveZeroMaxPoints;
    }


    public void init() throws Exception
    {
        profileEntry = testEvent.hasProfile() ? testEvent.getProfile().getLiveProfileEntry(simCompetencyObj.getName(), simCompetencyObj.getNameenglish(), true ) : null;

        if( profileEntry != null )
            applyProfileEntry();

        if( simCompetencyObj.getCt5Configjson()!=null && !simCompetencyObj.getCt5Configjson().isBlank() )
        {
            try
            {
                this.scJo = JsonUtils.convertJsonStringToObject(simCompetencyObj.getCt5Configjson());
            }
            catch( Exception e )
            {
                LogService.logIt(e, "SimCompetencyScore.init() " + toString() + ", configJson: " + simCompetencyObj.getCt5Configjson() );
                throw e;
            }
        }
    }

    @Override
    public int compareTo(SimCompetencyScore o) {

        return getName().compareTo(o.getName() );
    }



    public void applyProfileEntry()
    {
        if( this.profileEntry == null )
            return;

        //LogService.logIt( "SimCompetencyScore.applyProfileEntry() " + toString() + ", " + profileEntry.toString() + ", scoreColorSchemeTypeId=" + scoreColorSchemeTypeId );

        if( profileEntry.getWeight()>0 )
        {
            simCompetencyObj.setWeight( profileEntry.getWeight() );
        }

        if( profileEntry.getScoreCategoryRangeStr()!= null && !profileEntry.getScoreCategoryRangeStr().isEmpty() )
        {
            try
            {
                List[] data = profileEntry.parseColorStr();

                // int color;
                float score;

                String vals = "";

                List<Integer> colors = (List<Integer>) data[0];
                List<Float> scores = (List<Float>) data[1];

                if( scoreColorSchemeTypeId== ScoreColorSchemeType.THREECOLOR.getScoreColorSchemeTypeId() && ( colors.size() < 3 || colors.size() > 4 ) )
                {
                    LogService.logIt( "SimCompetencyScore.applyProfileEntry() Wrong number of entries in ProfileEntry.scoreCategoryRangeStr.  Expecting 3 or 4, found " + colors.size() );
                    return;
                }

                if( scoreColorSchemeTypeId== ScoreColorSchemeType.FIVECOLOR.getScoreColorSchemeTypeId() &&  ( colors.size() < 5 || colors.size() > 6 ) )
                {
                    LogService.logIt( "SimCompetencyScore.applyProfileEntry() Wrong number of entries in ProfileEntry.scoreCategoryRangeStr.  Expecting 5 or 6, found " + colors.size() );
                    return;
                }

                if( scoreColorSchemeTypeId== ScoreColorSchemeType.SEVENCOLOR.getScoreColorSchemeTypeId() &&  ( colors.size() < 7 || colors.size() > 8 ) )
                {
                    LogService.logIt( "SimCompetencyScore.applyProfileEntry() Wrong number of entries in ProfileEntry.scoreCategoryRangeStr.  Expecting 7 or 8, found " + colors.size() );
                    return;
                }

                if( scoreColorSchemeTypeId== ScoreColorSchemeType.FIVECOLOR.getScoreColorSchemeTypeId() )    // && ( this.simCompetencyObj.getCategorydisttype()==CategoryDistType.LINEAR.getCategoryDistTypeId() || simCompetencyObj.getCategorydisttype()==CategoryDistType.NORMAL.getCategoryDistTypeId() ) )
                {
                    for( int i=0;i<colors.size(); i++ )
                    {
                        // color = colors.get(i);
                        score = scores.get(i);

                        // Red
                        if( i == 0 )
                        {
                            this.simCompetencyObj.setRedyellowmin( score );
                            vals += "RY Min=" + score + ",";
                        }

                        // Red-Yellow
                        else if( i == 1 )
                        {
                            this.simCompetencyObj.setYellowmin( score );
                            vals += "Y Min=" + score + ",";
                        }

                        // Yellow
                        else if( i == 2 )
                        {
                            this.simCompetencyObj.setYellowgreenmin( score );
                            vals += "YG Min=" + score + ",";
                        }

                        // Yellow-Green
                        else if( i == 3 )
                        {
                            this.simCompetencyObj.setGreenmin( score );
                            vals += "G Min=" + score + ",";
                        }

                        // Green
                        else if( i==4 && colors.size()==6 )
                        {
                            this.simCompetencyObj.setHighcliffmin( score );

                            int hiCliffClr = colors.get(i+1);
                            if( hiCliffClr == 4  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId() );
                            else if( hiCliffClr == 3  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.YELLOW.getScoreCategoryTypeId() );
                            else if( hiCliffClr == 2  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId() );
                            else if( hiCliffClr == 1  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.RED.getScoreCategoryTypeId() );

                            vals += "High Cliff Min=" + score + ", Hi Cliff Color=" + hiCliffClr;
                        }
                    }

                    // LogService.logIt( "SimCompetencyScore.applyProfileEntry() Vals=" + vals );
                }

                else if( scoreColorSchemeTypeId== ScoreColorSchemeType.SEVENCOLOR.getScoreColorSchemeTypeId() )
                {
                    for( int i=0;i<colors.size(); i++ )
                    {
                        // color = colors.get(i);
                        score = scores.get(i);

                        // Red
                        if( i == 0 )
                        {
                            this.simCompetencyObj.setRedmin( score );
                            vals += "R Min=" + score + ",";
                        }

                        // Red-Yellow
                        else if( i == 1 )
                        {
                            this.simCompetencyObj.setRedyellowmin( score );
                            vals += "RY Min=" + score + ",";
                        }

                        // Yellow
                        else if( i == 2 )
                        {
                            this.simCompetencyObj.setYellowmin( score );
                            vals += "Y Min=" + score + ",";
                        }

                        // Yellow-Green
                        else if( i == 3 )
                        {
                            this.simCompetencyObj.setYellowgreenmin( score );
                            vals += "YG Min=" + score + ",";
                        }
                        // Green
                        else if( i == 4 )
                        {
                            this.simCompetencyObj.setGreenmin( score );
                            vals += "G Min=" + score + ",";
                        }
                        // White
                        else if( i == 5 )
                        {
                            this.simCompetencyObj.setWhitemin( score );
                            vals += "W Min=" + score + ",";
                        }

                        // Cliff
                        else if( i==6 && colors.size()==8 )
                        {
                            this.simCompetencyObj.setHighcliffmin( score );

                            int hiCliffClr = colors.get(i+1);
                            if( hiCliffClr == 4  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId() );
                            else if( hiCliffClr == 3  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.YELLOW.getScoreCategoryTypeId() );
                            else if( hiCliffClr == 2  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId() );
                            else if( hiCliffClr == 1  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.RED.getScoreCategoryTypeId() );

                            vals += "High Cliff Min=" + score + ", Hi Cliff Color=" + hiCliffClr;
                        }
                    }

                    // LogService.logIt( "SimCompetencyScore.applyProfileEntry() Vals=" + vals );
                }


                else if( scoreColorSchemeTypeId== ScoreColorSchemeType.THREECOLOR.getScoreColorSchemeTypeId()  )
                {
                    for( int i=0;i<colors.size(); i++ )
                    {
                        // Red
                        if( i == 0 )
                            this.simCompetencyObj.setYellowmin( scores.get(i));

                        // Yellow
                        else if( i == 1 )
                            this.simCompetencyObj.setGreenmin( scores.get(i));

                        // Green
                        else if( i==2 && colors.size()==4 )
                        {
                            this.simCompetencyObj.setHighcliffmin( scores.get(i));

                            int hiCliffClr = colors.get(i+1);
                            if( hiCliffClr <= 4 && hiCliffClr >= 2 )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.YELLOW.getScoreCategoryTypeId() );
                            else if( hiCliffClr == 1  )
                                simCompetencyObj.setHighclifflevel( ScoreCategoryType.RED.getScoreCategoryTypeId() );
                        }
                    }
                }
            }

            catch( Exception e )
            {
                LogService.logIt( e, "SimCompetencyScore.applyProfileEntry() " + profileEntry.toString() );
            }
        }
    }


    @Override
    public String toString()
    {
        return "SimCompetencyScore{ id=" + simCompetencyObj.getId() + ", name=" + simCompetencyObj.getName() + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId())  + " } ";
    }


    public SimCompetencyClass getSimCompetencyClass()
    {
        return SimCompetencyClass.getValue( simCompetencyObj.getClassid() );
    }

    public SimletCompetencyScore getSingleSimletCompetencyScore()
    {
        if( smltCompScrList==null || smltCompScrList.isEmpty() || smltCompScrList.size()>1 )
            return null;

        return smltCompScrList.get( 0 );
    }

    /*
    public boolean getHideInReports()
    {
        if( simCompetencyObj == null )
            return false;

        return simCompetencyObj.getHide()>0;
    }
    */

    @Override
    public String getName()
    {
        if( simCompetencyObj == null || simCompetencyObj.getName() == null )
            return "";

        return UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getName() );
    }

    @Override
    public String getNameEnglish()
    {
        if( simCompetencyObj == null || ( simCompetencyObj.getNameenglish() == null && simCompetencyObj.getName()==null ) )
            return "";

        if( simCompetencyObj.getNameenglish() != null && !simCompetencyObj.getNameenglish().isEmpty() )
            return UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getNameenglish() );

        return UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getName() );
    }


    public boolean hasAnyScoreData()
    {
        if( simCompetencyObj.getScoreifnoresponses()==1 )
            return true;

        if( hasScoreableData || scaledScore>0 )
            return true;

        if( textBasedResponseList != null && !textBasedResponseList.isEmpty() )
            return true;

        if( interviewQuestionList!=null && !interviewQuestionList.isEmpty() )
            return true;

        return false;
    }

    public boolean getHasUnscoredItems()
    {
        if( this.getSimCompetencyClass().getCollectsSamples() && hasAnyScoreData() )
            return true;

        return textBasedResponseList != null && !textBasedResponseList.isEmpty();
    }


    @Override
    public int getDisplayOrder()
    {
        if( this.getSimCompetencyObj()!=null )
            return getSimCompetencyObj().getDisporder();

        return 0;
    }

    //@Override
    //public void setDisplayOrder(int r) {
    //}

    @Override
    public int getUserRank()
    {
        if( this.getSimCompetencyObj()!=null )
            return getSimCompetencyObj().getUserrank();

        return 0;
    }



    protected void initForScoring()
    {
            scaledScoreFloor = 0;
            scaledScoreCeiling = 0;
            maxPointsPerItem = 0;
            rawScore = 0;
            scaledScore = 0;
            totalCorrect = 0;
            //totalCorrectCountAdjusted=0;
            totalScorableItems = 0;
            //totalScorableItemsCountAdjusted = 0;
            totalPoints = 0;
            fractionCorrect = 0;
            fractionOfPoints = 0;
            totalMaxPoints = 0;
            averagePoints = 0;
            caveatList = new ArrayList<>();
            forcedRiskFactorsList = new ArrayList<>();
            scoreTextInterviewQuestionList = new ArrayList<>();
            smltCompScrList = new ArrayList<>();
            textBasedResponseList = new ArrayList<>();
            mergableScoreObjectList = new ArrayList<>();
            itemScoreTextAndTitleList=null;
    }

    public void calculateScore( int simCompetencyScoreCalcTypeId ) throws Exception
    {
        try
        {
            //if( ScoreManager.DEBUG_SCORING )
            //    LogService.logIt( "SimCompetencyScore.calculateScore() START SimCompetency =" + this.getName() + " - testEvent: " + ( testEvent == null ? "null" : testEvent.toString() )  );

            initForScoring();
            float total;
            float totalWeights;

            if( testEvent == null || testEvent.getSimletScoreList() == null )
            {
                LogService.logIt( "SimCompetencyScore.calculateScore() CANNOT SCORE SimCompetency - testEvent: " + ( testEvent == null ? "null" : testEvent.toString() ) + ", testEvent.getSimletScoreList()=" + (testEvent.getSimletScoreList() == null ? 0 : testEvent.getSimletScoreList().size() ) );
                return;
            }

            // if this is an aggregate, we must do special.
            if( getSimCompetencyClass().getIsAggregate() )
            {
                calculateAggregateScore();
                return;
            }

            // if this is a combo simcompetency (combination of scores across other Sim Competencies)
            if( getSimCompetencyClass().getIsCombo() )
            {
                // LogService.logIt( "SimCompetencyScore.calculateScore() CANNOT SCORE COMBO SimCompetency directly." );
                return;
            }

            SimletCompetencyScore slcs;

            SimCompetencyScoreCalculationType simCompetencyScoreCalculationType = SimCompetencyScoreCalculationType.getValue( simCompetencyScoreCalcTypeId );

            metaScores = new float[13];

            if( simCompetencyObj.getIncludeitemscorestype()>0 )
                itemScoreTextAndTitleList = new ArrayList<>();

            //if( ScoreManager.DEBUG_SCORING )
            //   LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.1 starting. testEvent.getSimletScoreList().size()=" + testEvent.getSimletScoreList().size() );

            for( SimletScore ss : testEvent.getSimletScoreList() )
            {
                slcs = ss.getCompetencyScoreForSimCompetencyId( simCompetencyObj.getId() );

                // if this simlet measures this competency score.
                if( slcs != null )
                {
                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.2  Starting for SimletScore=" + ss.toString() + ", simletCompetencyScore present ? "  + (slcs!=null) );

                    // May need this if no responses are present.
                    if( competencyScoreType==null && simCompetencyObj.getScoreifnoresponses()==1 )
                        competencyScoreType = slcs.getCompetencyScoreType();

                    slcs.calculateScore( testEvent.getAutoScorableResponseList(), scoreFormatTypeId, simCompetencyObj.getIncludeitemscorestype() );

                    if( slcs.isPendingExternalScores() )
                    {
                        if( ScoreManager.DEBUG_SCORING )
                           LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.2b SimletComptency is pending external scores. SimletScore=" + ss.toString() + ", simletCompetencyScore "  + slcs.toString() );
                        pendingExternalScores = true;
                        continue;
                    }

                    if( slcs.getScaledScoreCeiling()> scaledScoreCeiling )
                        scaledScoreCeiling = slcs.getScaledScoreCeiling();

                    if( slcs.getScaledScoreFloor()> scaledScoreFloor )
                        scaledScoreFloor = slcs.getScaledScoreFloor();

                    caveatList.addAll( slcs.getCaveatList() );

                    forcedRiskFactorsList.addAll(slcs.getForceRiskFactorsList() );

                    scoreTextInterviewQuestionList.addAll(slcs.getScoreTextInterviewQuestionList() );

                    mergableScoreObjectList.addAll( slcs.getMergableScoreObjectList() );

                    if( simCompetencyObj.getIncludeitemscorestype()>0 && slcs.getItemScoreTextAndTitleList()!=null )
                        itemScoreTextAndTitleList.addAll( slcs.getItemScoreTextAndTitleList() );


                    if( ScoreManager.DEBUG_SCORING )
                       LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.3 scoring simlet " + ss.simletObj.getName() + ", " + slcs.toString() + ", hasDataToAutoScore=" + slcs.hasDataToAutoScore() + ", scs.rawscore=" + slcs.getRawScore() + ", slcs.totalScorableItems=" + slcs.getTotalScorableItems() );

                    if( slcs.hasDataToAutoScore() )
                    {
                        // Metascores are always averaged - if they have this
                        for( int i=2; i<=12; i++ )
                        {
                            // LogService.logIt( "SimCompetencyScore adding simletCompetencyScore.metascore[] index=" + i + " value=" + scs.getMetaScore(i));

                            metaScores[i] += slcs.getMetaScore(i);
                        }

                        smltCompScrList.add( slcs );
                    }

                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.4 smltCompScrList=" + smltCompScrList.size() );

                    textBasedResponseList.addAll( slcs.getTextBasedResponses( testEvent.getAllResponseList(), false ) );
                    // textBasedResponseList.addAll( slcs.getTextBasedResponses( testEvent.getAllIactnResponseList() , false ) );
                }

                //else
                //    LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.10 No simletCompetencyScore found for SimletScore=" + ss.toString() );

            }

            if( pendingExternalScores )
                return;

            // Metascores are always averaged, so complete the average.
            if( smltCompScrList.size()>1 )
            {
                for( int i=2; i<=12; i++ )
                {
                    metaScores[i] /= ((float) smltCompScrList.size() );
                }
            }


            if( ScoreManager.DEBUG_SCORING && smltCompScrList.isEmpty() )
                LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.5 " + this.simCompetencyObj.getName() + " has zero SimletCompetencyScores. Setting to not scoreable. " );

            hasScoreableData = simCompetencyObj.getScoreifnoresponses()==1 || !smltCompScrList.isEmpty();

            total = 0;
            totalWeights = 0;
            maxPointsPerItem = 0;
            rawScore = 0;
            scaledScore = 0;
            totalCorrect = 0;
            float totalCorrectCountAdjusted = 0;
            totalScorableItems = 0;
            float totalScorableItemsCountAdjusted = 0;
            totalPoints = 0;
            fractionCorrect = 0;
            fractionOfPoints = 0;
            totalMaxPoints = 0;
            averagePoints = 0;

            if( !hasScoreableData )
            {
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.6 " + this.getName() + ",  hasScoreableData=" + hasScoreableData );
                return;
            }

            // CompetencyScoreType competencyScoreType = null;

            //if( simCompetencyScoreCalculationType.equals( SimCompetencyScoreCalculationType.ACROSSALL ) )
            //{
            //    calculateScoreAcrossAllSimlets( scoreFormatTypeId );

            //    return;
            //}

            // Remember that Tasks are always calculated across all.
            boolean calculateAcrossAll = simCompetencyScoreCalculationType.equals( SimCompetencyScoreCalculationType.ACROSSALL ) || getSimCompetencyClass().getRequiresCalcAcrossAll();

            //if( ScoreManager.DEBUG_SCORING )
            //    LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.7 " + this.getName() + ",  smltCompScrList.size() = " + smltCompScrList.size() + ", calculateAcrossAll=" + calculateAcrossAll + ", metascores[2,3,4]=" + metaScores[2] + ","+ metaScores[3] + ","+ metaScores[4] );

            // LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AAA.3 calculateAcrossAll=" + calculateAcrossAll + ", useTotalItems=" + useTotalItems + ", simCompetencyObj.getMaxpoints()=" + simCompetencyObj.getMaxpoints() + ", smltCompScrList=" + smltCompScrList.size() );

            // this indicates that maxPoints is NOT taken from the descriptor. So need to calculate it as go through the scoring process.
            boolean incrMaxPoints = true;

            if( calculateAcrossAll && useTotalItems && simCompetencyObj.getMaxpoints()!= null && !simCompetencyObj.getMaxpoints().isEmpty() )
            {
                incrMaxPoints = false;
                totalMaxPoints = InteractionScoreUtils.getPointsArray( simCompetencyObj.getMaxpoints() )[0];

                if( totalMaxPoints <= 0 )
                {
                    totalMaxPoints = 0;
                    incrMaxPoints = true;
                }
            }

            // LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.4 totalMaxPoints=" + totalMaxPoints + ", incrMaxPoints=" + incrMaxPoints );
            
            ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scoreFormatTypeId );

            for( SimletCompetencyScore scr : smltCompScrList )
            {
                if( calculateAcrossAll )
                {
                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.8  Simlet " + scr.simletScore.simletObj.getName() +", competencyScoreType=" + scr.getCompetencyScoreType().getName() + ", scr.totalScorableItems=" + scr.getTotalScorableItems() );

                    if( scr.getCompetencyScoreType().isDichotomous() )
                    {
                        if( competencyScoreType != null && !competencyScoreType.isDichotomous() )
                            throw new Exception( "Set to calculate sim competency scores across all simlets, but different simlets have different score types." );

                        // need this because SimCompetency doesn't define the score type. That is always done at the simlet level. They all need to be the same for a
                        // given sim competency.
                        competencyScoreType = scr.getCompetencyScoreType();
                        totalScorableItems += scr.getTotalScorableItems();
                        totalCorrect += scr.getTotalCorrect();

                        totalScorableItemsCountAdjusted += scr.getTotalScorableItemsCountAdjusted();
                        totalCorrectCountAdjusted += scr.getTotalCorrectCountAdjusted();

                        // account for total points even for dichotomous items.
                        totalPoints += scr.getTotalPoints();

                        if( incrMaxPoints )
                            totalMaxPoints += scr.getMaxPoints();

                        // LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.8b  Added for dichotomous. totalScorableItems=" + totalScorableItems + ", simletScore.totalScorableItems=" + scr.getTotalScorableItems() );
                    }

                    // Is points type and has points
                    else if( scr.getCompetencyScoreType().isPointAccum() && ( scr.getMaxPoints()!=0 || validItemsCanHaveZeroMaxPoints ) )
                    {
                        if( ScoreManager.DEBUG_SCORING )
                            LogService.logIt( "SimCompetencyScore.calculateScore()  SCS.AA.PP.1  POINTS Simlet " + scr.simletScore.simletObj.getName() + ", competency=" + scr.competencyScoreObj.getName() +", competencyScoreType=" + scr.getCompetencyScoreType().getName() + ", scr.getMaxPoints()=" + scr.getMaxPoints() + ", totalPoints=" + totalPoints );

                        if( competencyScoreType != null && !competencyScoreType.isPointAccum() )
                            throw new Exception( "Set to calculate sim competency scores across all simlets, but different simlets have different score types." );

                        if( ScoreManager.DEBUG_SCORING )
                            LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.PP.2 POINTS Adding " + scr.getTotalScorableItems() + " scorable items, adding " + scr.getTotalPoints() + " points to score for competency. total correct in simletcompetencyscore=" + scr.getTotalCorrect() + ", max points in simletcompetencyscore=" + scr.getMaxPoints() );

                        // need this because SimCompetency doesn't define the score type. That is always done at the simlet level. They all need to be the same for a
                        // given sim competency.
                        competencyScoreType = scr.getCompetencyScoreType();
                        totalScorableItems += scr.getTotalScorableItems();
                        totalPoints += scr.getTotalPoints();

                        if( incrMaxPoints )
                            totalMaxPoints += scr.getMaxPoints();

                        // Add total correct anyway, to handle quasi-dichotomous
                        totalCorrect += scr.getTotalCorrect();
                        
                        // add in any additional max points (from prev iactn responses.
                        //else
                        //    totalMaxPoints += scr.getAdditionalMaxPoints();
                    }

                    else if( scr.getCompetencyScoreType().isTrueDifference()  )
                    {
                        if( competencyScoreType != null && competencyScoreType.getCompetencyScoreTypeId()!=scr.getCompetencyScoreType().getCompetencyScoreTypeId() )
                            throw new Exception( "Set to calculate sim competency scores across all simlets, but different simlets have different score types." );

                        competencyScoreType = scr.getCompetencyScoreType();

                        totalScorableItems += scr.getTotalScorableItems();
                        totalPoints += scr.getTotalPoints();

                        if( scr.getCompetencyScoreType().equals( CompetencyScoreType.AVG_MAX_MINUS_ABS_TRUE_DIFF ) )
                            maxPointsPerItem = scr.getMaxPointsPerItem();

                        if( incrMaxPoints )
                            totalMaxPoints += scr.getMaxPoints();

                        // Add total correct anyway, to handle quasi-dichotomous
                        totalCorrect += scr.getTotalCorrect();
                    }

                    else if( scr.getCompetencyScoreType().isAvgZscoreDiff() )
                    {
                        if( competencyScoreType != null && competencyScoreType.getCompetencyScoreTypeId()!=scr.getCompetencyScoreType().getCompetencyScoreTypeId() )
                            throw new Exception( "Set to calculate sim competency scores across all simlets, but different simlets have different score types." );

                        competencyScoreType = scr.getCompetencyScoreType();

                        totalScorableItems += scr.getTotalScorableItems();
                        totalPoints += scr.getTotalPoints();

                        // Add total correct anyway, to handle quasi-dichotomous
                        totalCorrect += scr.getTotalCorrect();
                    }

                    // Is data entry or essay
                    else if( scr.getCompetencyScoreType().isTypingSpeedAccuracy() ||
                            scr.getCompetencyScoreType().isDataEntry() ||
                            scr.getCompetencyScoreType().isScoredEssay() ||
                            scr.getCompetencyScoreType().isScoredChat() ||
                            scr.getCompetencyScoreType().isScoredVoiceSample() ||
                            scr.getCompetencyScoreType().isScoredAvUpload() ||
                            scr.getCompetencyScoreType().isIdentityImageCapture() )
                    {
                        if( ScoreManager.DEBUG_SCORING )
                            LogService.logIt( "SimCompetencyScore.calculateScore() " + this.getName() + ", Assign competencyScoreType to " + scr.getCompetencyScoreType() );
                        competencyScoreType = scr.getCompetencyScoreType();
                        totalScorableItems += scr.getTotalScorableItems();
                        totalPoints += scr.getTotalPoints();

                        if( incrMaxPoints )
                            totalMaxPoints += scr.getMaxPoints();
                    }
                }

                else if( simCompetencyScoreCalculationType.equals( SimCompetencyScoreCalculationType.INDIVIDUALLY_EVEN ) )
                {
                    totalScorableItems += scr.getTotalScorableItems();
                    total += scr.getUnweightedScaledScore();
                    totalWeights += 1;
                }

                else if( simCompetencyScoreCalculationType.equals( SimCompetencyScoreCalculationType.INDIVIDUALLY_COVERAGE ) )
                {
                    totalScorableItems += scr.getTotalScorableItems();
                    total += scr.getCoverageWeightedScaledScore();
                    totalWeights += scr.getCoverageWeight();
                }

                if( !calculateAcrossAll )
                {
                    if( competencyScoreType   == null )
                        competencyScoreType = scr.getCompetencyScoreType();

                    float tp = scr.getTotalPoints();

                    // Always represented as 0-100 at this point.
                    if( scr.getCompetencyScoreType().isTypingSpeedAccuracy() ||
                        scr.getCompetencyScoreType().isDataEntry() ||
                        scr.getCompetencyScoreType().isScoredEssay() ||
                        scr.getCompetencyScoreType().isScoredChat() ||
                        scr.getCompetencyScoreType().isScoredVoiceSample() ||
                        scr.getCompetencyScoreType().isScoredAvUpload() ||
                        scr.getCompetencyScoreType().isIdentityImageCapture())
                    {
                        if( scoreFormatType.equals(ScoreFormatType.NUMERIC_0_TO_100))
                            tp = ScoreFormatType.convertFromAToBToLinearScore(tp, ScoreFormatType.NUMERIC_0_TO_100, scoreFormatType);
                    }

                    totalPoints += tp;

                    total += tp;

                    totalCorrect += scr.getTotalCorrect();
                    totalCorrectCountAdjusted += scr.getTotalCorrectCountAdjusted();

                    if( incrMaxPoints )
                        totalMaxPoints += scr.getMaxPoints();
                }
            }
            
            if( simCompetencyObj.getFrcmaxitems()>0 )
            {
                totalScorableItems = simCompetencyObj.getFrcmaxitems();
                totalScorableItemsCountAdjusted = simCompetencyObj.getFrcmaxitems();
            }

            if( simCompetencyObj.getScoreifnoresponses()!=1 && totalScorableItems <= 0 )
            {
                LogService.logIt( "SimCompetencyScore.calculateScore() " + this.simCompetencyObj.getName() + " has zero scorable items. Setting to not-scoreable" );
                hasScoreableData=false;
                return;
            }

            Map<String,int[]> topicMap = null;

            if( getSimCompetencyClass().getProducesTopics() )
            {
                // TO DO!
                caveatList = new ArrayList<>();

                topicMap = new TreeMap<>();
                Map<String,int[]> tm;

                int[] d1;
                int[] d2;
                int frcTotalItms;

                for( SimletCompetencyScore scr : smltCompScrList )
                {
                    /*
                     * map of topic name, int[]
                     *    int[0] = number correct        ( for this item this means points )
                     *    int[1] = number total this topic.  max points for competency
                     *    int[2] = number of items that were partially correct.  ( 0 )
                     *    int[3] = number of items total for this topic from this SimletCompetencyScore
                     */
                    tm=scr.getTopicMap();

                    if( tm ==null )
                        continue;

                    for( String k : tm.keySet() )
                    {
                        d1=tm.get(k);

                        // no new items for this topic in simlet competency score
                        if( d1==null )
                            continue;

                        // get existing values for global tm
                        d2=topicMap.get(k);

                        // map of topic name, int[]
                        //    int[0] = number correct average value (if not a sum type) ( for this item this means points )
                        //    int[1] = number total this topic.  max points for competency
                        //    int[2] = number of items that were partially correct.  ( 0 )
                        //    int[3] = number of items total for this topic from this SimletCompetencyScore
                        //    int[4] = Flag to be used downstream. 0 means add items (default). 1 means average number correct using total number of items in int[3].
                        if( d2==null )
                            d2 = new int[5];

                        // Add scores or number of correct
                        d2[0] += d1[0];

                        // Total.
                        if( scr.getCompetencyScoreType().isAddTotalsForTopics() )
                        {
                            // Add totals or max points
                            d2[1] += d1[1];
                            d2[4] = 0;
                        }
                        else
                        {
                            // just use the steady max
                            d2[1] = d1[1];

                            // set flag
                            d2[4] = 1;
                        }

                        // partially correct items questions
                        d2[2] += d1[2];

                        // Sum up total items.
                        d2[3] += d1[3];

                        if( simCompetencyObj!=null )
                        {
                            frcTotalItms = getForcedTotalItemsFromDescrip( k );
                            if( frcTotalItms>0 )
                                d2[1]=frcTotalItms;
                        }
                        
                        topicMap.put( k, d2 );
                    }
                }

                List<String> topicMapKeyList = new ArrayList<>();
                topicMapKeyList.addAll(topicMap.keySet());

                Collections.sort(topicMapKeyList, new TopicComparator(simCompetencyObj) );

                for( String k : topicMapKeyList )
                {
                    d2 = topicMap.get(k);

                    // Not all topics are sums, if they are average, then do the average here.
                    if( d2[4]==1 && d2[3]>0 )
                    {
                        // total points / number of items = average per item.
                        float avg = ((float) d2[0])/((float) d2[3]);

                        d2[0] = ((int) avg);
                    }

                    caveatList.add( Constants.TOPIC_KEY + "~" + k + "~" + d2[0] + "~" + d2[1] + "~" + d2[2] );
                }
            }


            //
            //if( scoreFormatType.isNumeric() && totalPoints<0 )
            //    totalPoints = 0;
            setSimCompetencyScoreTypes();

            if( calculateAcrossAll )
            {
                fractionScoreValue = 0;
                totalScoreValue = 0;
                
                if( competencyScoreType.isDichotomous() )
                {
                    boolean usesAdjustedScorableResponseCounts = false;
                    if( competencyScoreType.isPercentOfTotal() && simCompetencyObj.getMinansweredcount()>0 && totalScorableItems<simCompetencyObj.getMinansweredcount() )
                    {
                        LogService.logIt( "SimCompetencyScore.calculateScore()  CompetencyScoreType=" + competencyScoreType.getName() + " and Min Answered Count=" + simCompetencyObj.getMinansweredcount() + " and totalScorableItems=" + totalScorableItems + " so adjusting totalScorableItems to minAnsweredCount for score calculations." );
                        totalScorableItems = simCompetencyObj.getMinansweredcount();
                    }

                    if( totalScorableItems>0 && totalScorableItemsCountAdjusted>0 && totalScorableItems!=((int)totalScorableItemsCountAdjusted) && Math.abs( ((float)totalScorableItems)-totalScorableItemsCountAdjusted)>0.2f )
                    {
                        LogService.logIt( "SimCompetencyScore.calculateScore() Using totalCorrectCountAdjusted=" + totalCorrectCountAdjusted + " and totalScorableItemsCountAdjusted=" + totalScorableItemsCountAdjusted + " in fractionCorrect calculation. totalCorrect=" + totalCorrect + ", totalScorableItems=" + totalScorableItems + ", CompetencyScoreType=" + competencyScoreType.getName() );
                        fractionCorrect = totalCorrectCountAdjusted/totalScorableItemsCountAdjusted;
                        totalScoreValue = totalCorrectCountAdjusted;
                        usesAdjustedScorableResponseCounts = true;
                    }
                    else
                    {
                        fractionCorrect = totalScorableItems > 0 ? ( (float)totalCorrect )/( (float) totalScorableItems) : 0;
                        totalScoreValue = totalCorrect;
                    }
                    
                    fractionScoreValue = fractionCorrect;


                    // If scoring is based on percent correct and there are ct5Subtopics present with weights, adjust fraction correct using a weighted average.
                    if( topicMap!=null &&
                        !usesAdjustedScorableResponseCounts &&
                        competencyScoreType.isPercentCorrect() &&
                        simCompetencyObj!=null && simCompetencyObj.getCt5Subtopics()!=null && !simCompetencyObj.getCt5Subtopics().getCt5Subtopic().isEmpty() )
                    {
                        boolean hasWeight = false;
                        int[] tmVals;
                        for( SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic stp : simCompetencyObj.getCt5Subtopics().getCt5Subtopic() )
                        {
                            if( stp.getWeight()>0 )
                            {
                                tmVals = topicMap.get(StringUtils.getUrlDecodedValue(stp.getName()) );
                                if( tmVals==null || tmVals[3]<=0 )
                                {
                                    LogService.logIt( "SimCompetencyScore.calculateScore() calc weighted topics. Topic " + StringUtils.getUrlDecodedValue(stp.getName()) + " has " + (tmVals==null ? " no topic map" : " zero total items." ) + ", " + this.getName() + ", " + this.getNameEnglish() );
                                    continue;
                                }

                                hasWeight=true;
                                break;
                            }
                        }

                        if( hasWeight )
                        {
                            // topicMap of topic name, int[]
                            //    int[0] = number correct
                            //    int[1] = number total this topic.  max points for competency
                            //    int[2] = number of items that were partially correct.  ( 0 )
                            //    int[3] = number of items total for this topic

                            float tot = 0;
                            float totWgts=0;
                            float wgt;

                            // topic fraction correct
                            float tpcFrcCrct;
                            for( SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic stp : simCompetencyObj.getCt5Subtopics().getCt5Subtopic() )
                            {
                                tmVals = topicMap.get(StringUtils.getUrlDecodedValue(stp.getName()) );
                                if( tmVals==null || tmVals[3]<=0 )
                                {
                                    LogService.logIt( "SimCompetencyScore.calculateScore() calc weighted topics. Topic " + StringUtils.getUrlDecodedValue(stp.getName()) + " has " + (tmVals==null ? " no topic map" : " zero total items." ) + ", " + this.getName() + ", " + this.getNameEnglish() );
                                    continue;
                                }

                                // LogService.logIt( "SimCompetencyScore.calculateScore() calc weighted topics. Topic " + StringUtils.getUrlDecodedValue(stp.getName()) + " has " + tmVals[0] + " correct items of " + tmVals[3] + " total items. weight=" + stp.getWeight() + ", simCompetency=" + this.getName() + ", " + this.getNameEnglish() );
                                tpcFrcCrct = ((float) tmVals[0])/((float) tmVals[3]);
                                wgt = stp.getWeight()<=0 ? 1f : stp.getWeight();
                                tot += tpcFrcCrct*wgt;
                                totWgts += wgt;
                            }
                            // Now do NOTOPIC

                            tmVals = topicMap.get("NOTOPIC" );
                            if( tmVals!=null && tmVals[3]>0 )
                            {
                                LogService.logIt( "SimCompetencyScore.calculateScore() calc weighted topics. Topic=" + "NOTOPIC" + " has " + tmVals[0] + " correct items of " + tmVals[3] + " total items. weight=1 simCompetency=" + this.getName() + ", " + this.getNameEnglish() );

                                tpcFrcCrct = ((float) tmVals[0])/((float) tmVals[3]);
                                wgt = 1f;
                                tot += tpcFrcCrct*wgt;
                                totWgts += wgt;
                            }
                            
                            if( totWgts<=0 )
                            {
                                LogService.logIt( "SimCompetencyScore.calculateScore() NONFATAL. TotalWeights INVALID while calculating weighted topic score for " + this.getName() + ", " + this.getNameEnglish() + ", unweighted fraction=" + fractionScoreValue + ", weighted fraction=" + tot + ", hasWeight=" + hasWeight + ", setting totWgts=1 "  );
                                totWgts=1;
                            }

                            tot = tot/totWgts;
                            LogService.logIt( "SimCompetencyScore.calculateScore() calc weighted topic score for " + this.getName() + ", " + this.getNameEnglish() + ", unweighted fraction=" + fractionScoreValue + ", weighted fraction=" + tot  );
                            fractionScoreValue = tot;
                            fractionCorrect = tot;
                        }
                    }

                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "SimCompetencyScore.calculateScore() DDD dichotomous items. totalScorableItems=" + totalScorableItems + ", totalCorrect=" + totalCorrect + ", fractionScoreValue=fractionCorrect=" + fractionCorrect );
                }

                else if( competencyScoreType.isAverage() || competencyScoreType.isScoredEssay() || competencyScoreType.isScoredChat() || competencyScoreType.isTypingSpeedAccuracy() || competencyScoreType.isDataEntry() || competencyScoreType.isScoredVoiceSample() || competencyScoreType.isScoredAvUpload() || competencyScoreType.isIdentityImageCapture() )
                {
                    if( competencyScoreType.isAverage() && simCompetencyObj.getMinansweredcount()>0 && totalScorableItems<simCompetencyObj.getMinansweredcount() )
                    {
                        LogService.logIt( "SimCompetencyScore.calculateScore()  CompetencyScoreType=" + competencyScoreType.getName() + " and " + simCompetencyObj.getMinansweredcount() + " and totalScorableItems=" + totalScorableItems + " so adjusting totalScorableItems to minAnsweredCount for score calculations." );
                        totalScorableItems = simCompetencyObj.getMinansweredcount();
                    }

                    averagePoints = totalScorableItems > 0 ? ( (float)totalPoints )/( (float) totalScorableItems) : 0;

                    // For Essays and Typing, this is always 0-100 at this point.

                    // For avg abs

                    if( ( competencyScoreType.isScoredEssay() || competencyScoreType.isScoredChat() || competencyScoreType.isScoredVoiceSample() || competencyScoreType.isTypingSpeedAccuracy() || competencyScoreType.isDataEntry() || competencyScoreType.isScoredAvUpload() || competencyScoreType.isIdentityImageCapture() ) && !scoreFormatType.equals( ScoreFormatType.NUMERIC_0_TO_100 ))
                        averagePoints = ScoreFormatType.convertFromAToBToLinearScore( averagePoints, ScoreFormatType.NUMERIC_0_TO_100, scoreFormatType );

                    // LogService.logIt( "SimCompetencyScore computed SimCompetencyScore competencyScoreType=" + competencyScoreType.getName() + ", averagePoints=" + averagePoints + " totalPoints=" + totalPoints + ", totalScorableItems=" + totalScorableItems );
                    fractionScoreValue = averagePoints;
                    totalScoreValue = averagePoints;
                }

                else if( competencyScoreType.isPointAccum() )
                {
                    fractionOfPoints = totalMaxPoints != 0 ? totalPoints / totalMaxPoints : 0;
                    totalScoreValue = totalPoints;
                    fractionScoreValue = fractionOfPoints;
                }

                else if( competencyScoreType.isTrueDifference())
                {

                    fractionOfPoints = totalMaxPoints > 0 ? totalPoints / totalMaxPoints : 0;
                    totalScoreValue = totalPoints;
                    fractionScoreValue = fractionOfPoints;
                }

                // setSimCompetencyScoreTypes();

                // OK, calculate the raw score
                if( rawScoreCalcType.getIsZScore() )
                {
                    float relevantValue =  totalScoreValue;

                    if( competencyScoreType.isDichotomous() && !competencyScoreType.equals( CompetencyScoreType.TOTAL_CORRECT) )
                        relevantValue = fractionScoreValue;

                    if( competencyScoreType.isPointAccum() && competencyScoreType.isPercentOfTotal() )
                        relevantValue = fractionScoreValue;
                    
                    // Use the local sim-specific value if present (std >0).
                    if( simCompetencyObj.getStddeviation()>0 )
                    {
                        rawScore = (relevantValue-simCompetencyObj.getMean())/simCompetencyObj.getStddeviation();
                        // LogService.logIt( "SimCompetencyScore.calculate() calculated Z-score for raw score Competency=" + this.simCompetencyObj.getName() + ", relevantValue=" + relevantValue + ", used LOCAL stats=" + this.simCompetencyObj.getMean() + "," + simCompetencyObj.getStddeviation() + ", RawScore=" + rawScore );

                        if( ScoreManager.DEBUG_SCORING )
                            LogService.logIt( "SimCompetencyScore.calculate() calculated Z-score for raw score Competency=" + this.simCompetencyObj.getName() + ", relevantValue=" + relevantValue + ", used LOCAL stats=" + this.simCompetencyObj.getMean() + "," + simCompetencyObj.getStddeviation() + ", RawScore (before possible inversion)=" + rawScore + ", invert=" + simCompetencyObj.getInvertcomputedzscore() );

                        if( simCompetencyObj.getInvertcomputedzscore()==1 )
                        {
                            rawScore = -1*rawScore;

                            if( ScoreManager.DEBUG_SCORING )
                                LogService.logIt( "SimCompetencyScore.calculate() inverted Z-score for raw score. RawScore AFTER inversion)=" + rawScore );

                        }
                    }
                    //else if( simletCompetencyStat!=null )
                    //{
                    //    rawScore = simletCompetencyStat.convertToZ( relevantValue );
                    //    LogService.logIt( "SimCompetencyScore.calculate() calculated Z-score for raw score Competency=" + this.simCompetencyObj.getName() + ", relevantValue=" + relevantValue + ", used SimletSTATs=" + simletCompetencyStat.getMean() + "," + simletCompetencyStat.getStandardDeviation() + ", RawScore=" + rawScore );
                    //}
                    else
                    {
                        rawScore = -20f; // simletCompetencyStat.convertToZ( relevantValue );
                        // LogService.logIt( "SimCompetencyScore.calculate() Could not calculate Z-score for raw score because no stats or simletstats available. Competency=" + this.simCompetencyObj.getName() + ", relevantValue=" + relevantValue + ", Setting RawScore=" + rawScore );
                    }

                }

                // Not Z-Score
                else
                {
                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "SimCompetencyScore.calculateScore() XX1 simCompetencyObj=" + simCompetencyObj.getName() + ", fractionOfPoints=" + fractionOfPoints + ", totalScoreValue =" + totalScoreValue   ); //  + ", luukup=" +  simCompetencyObj.getLookuptable() );
                    rawScore = scoreFormatType.getUnweightedRawScore(   competencyScoreType,
                                                                        fractionScoreValue,
                                                                        totalScoreValue,
                                                                        simCompetencyObj.getMean(),
                                                                        simCompetencyObj.getStddeviation() );
                }

                if( scaledScoreCalcType.getIsTransform() )
                {
                    if( simCompetencyObj.getScaledstddeviation()>0 )
                        scaledScore = NumberUtils.applyNormToZScore( rawScore, simCompetencyObj.getScaledmean(), simCompetencyObj.getScaledstddeviation() );
                    else
                        scaledScore = 0;

                    // These types are such that the bigger the raw value, the lower the score should be - they need to be inverted.
                    //if( competencyScoreType.isAvgZscoreDiff() || competencyScoreType.isTrueDifference() )
                    //   scaledScore = scoreFormatType.invertScore( scaledScore );
                }

                else if( scaledScoreCalcType.getEqualsRawScore() )
                    scaledScore = rawScore;

                else
                    scaledScore = scoreFormatType.getUnweightedScaledScore( competencyScoreType, rawScore, maxPointsPerItem, simCompetencyObj.getLookuptable() );

                // LogService.logIt( "SimCompetencyScore.calculateScore() Calc Across All. simCompetencyObj=" + simCompetencyObj.getName() + ", rawScore=" + rawScore + ", scaledScore () before ceiling and floor=" + scaledScore  ); //  + ", luukup=" +  simCompetencyObj.getLookuptable() );

                if(  scaledScore<scoreFormatType.getMinScoreToGiveTestTaker() )
                    scaledScore=scoreFormatType.getMinScoreToGiveTestTaker();

                if(  scaledScore>scoreFormatType.getMaxScoreToGiveTestTaker())
                    scaledScore=scoreFormatType.getMaxScoreToGiveTestTaker();


                //if( ScoreManager.DEBUG_SCORING )
                //   LogService.logIt( "SimCompetencyScore.calculateScore() XX2 simCompetencyObj=" + simCompetencyObj.getName() + ", rawScore=" + rawScore + ", scaledScore () before ceiling =" + scaledScore + ", scaledScoreCeiling=" + scaledScoreCeiling  ); //  + ", luukup=" +  simCompetencyObj.getLookuptable() );
//score = scoreFormatType.getUnweightedScore( competencyScoreType, fractionScoreValue, totalScoreValue, simCompetencyObj.getMean(), simCompetencyObj.getStddeviation());

                // at this point, score = score in the desired numerical format,

                // rawScore = competencyScoreType.getIsNormScale() || competencyScoreType.isRawTotal() ? totalScoreValue : fractionScoreValue;

                 // LogService.logIt( "SimCompetencyScore.calculateScore() " + this.getName() + ", competencyScoreType=" + competencyScoreType.getName() + ",  Across All. fractionScoreValue=" + fractionScoreValue + ", totalScoreValue=" + totalScoreValue +", Score=" + score + ", RawScore=" + rawScore );
                 //rawScore = score;

            } // end across all

            // Scores calculated at simlet level and already expressed in terms of ScoreFormatType
            else
            {

                if( competencyScoreType.isDichotomous() )
                    totalScoreValue = totalCorrect;

                else if( competencyScoreType.isAverage())
                    totalScoreValue = averagePoints;

                else if( competencyScoreType.isPointAccum())
                    totalScoreValue = totalPoints;

                // setSimCompetencyScoreTypes();

                // NOTE - For this to work, the sim competency can only appear in one simlet. If it appears in multiple, then the stats won't work.
                // OK, calculate the raw score
                if( rawScoreCalcType.getIsZScore() )
                {
                    // Use the local sim-specific value if present (std >0).
                    if( simCompetencyObj.getStddeviation()>0 )
                        rawScore = (totalScoreValue-simCompetencyObj.getMean())/simCompetencyObj.getStddeviation();

                    // -20 standard deivations - effectively 0
                    else
                        rawScore = -20; // simletCompetencyStat.convertToZ( totalScoreValue );


                    if( scaledScoreCalcType.getIsTransform() )
                        scaledScore = NumberUtils.applyNormToZScore( rawScore, simCompetencyObj.getScaledmean(), simCompetencyObj.getScaledstddeviation() );

                    if( scaledScoreCalcType.getEqualsRawScore())
                        scaledScore = rawScore;

                    else
                        scaledScore = totalWeights > 0 ? total/totalWeights : 0;
                }

                else
                {
                    scaledScore = totalWeights > 0 ? total/totalWeights : 0;
                    rawScore = scaledScore;
                }


                if( competencyScoreType.isScoredEssay() || competencyScoreType.isScoredChat() || competencyScoreType.isScoredVoiceSample() || competencyScoreType.isTypingSpeedAccuracy() || competencyScoreType.isDataEntry() || competencyScoreType.isScoredAvUpload() || competencyScoreType.isIdentityImageCapture()  )
                {
                    // do nothing, nothing!
                }

                //if( ScoreManager.DEBUG_SCORING )
                //    LogService.logIt( "SimCompetencyScore.calculateScore() XX0 " + this.getName() + ", total=" + total + ", Individually. TotalWeights=" + totalWeights );
            }


            if( getSimCompetencyClass().equals( SimCompetencyClass.SCOREDTYPING ) )
            {
                Locale locale = getReportingLocale(); // I18nUtils.getLocaleFromCompositeStr( testEvent.getLocaleStr() );

                //if( ScoreManager.DEBUG_SCORING )
                //    LogService.logIt( "SimCompetencyScore.calculateScore() " + this.getName() + ", metascores[2,3,4]=" + metaScores[2] + ","+ metaScores[3] + ","+ metaScores[4] );

                caveatList = new ArrayList<>();
                caveatList.add( MessageFactory.getStringMessage( locale , "g.WordPerMinX" , new String[]{ Integer.toString( Math.round( metaScores[2] ) )} ) );
                caveatList.add( MessageFactory.getStringMessage( locale , "g.WordPerMinAccAdjX" , new String[]{ Integer.toString( Math.round( metaScores[3] ) )} ) );
                caveatList.add( MessageFactory.getStringMessage( locale , "g.AccuracyX" , new String[]{ Integer.toString( Math.round( metaScores[4] ) )} ) );
            }

            else if( getSimCompetencyClass().equals( SimCompetencyClass.SCOREDDATAENTRY ) )
            {
                Locale locale = getReportingLocale(); // I18nUtils.getLocaleFromCompositeStr( testEvent.getLocaleStr() );

                // if( ScoreManager.DEBUG_SCORING )
                //    LogService.logIt( "SimCompetencyScore.calculateScore() " + this.getName() + ", reportData=" + (reportData==null ? "null" : "not null") + ", metascores[2,3,4,5,6,7,8,9]=" + metaScores[2] + ","+ metaScores[3] + ","+ metaScores[4] + ", " + (metaScores.length>5 ? metaScores[5] : "NA" ) + ", " + (metaScores.length>6 ? metaScores[6] : "NA" ) + ", " + (metaScores.length>7 ? metaScores[7] : "NA" ) + ", " + (metaScores.length>8 ? metaScores[8] : "NA" ) + ", " + (metaScores.length>9 ? metaScores[9] : "NA" ) );


                caveatList = new ArrayList<>();

                boolean fflag = reportData==null || !reportData.getReportRuleAsBoolean( "hidedataentryksph" );
                if( fflag )
                    caveatList.add( MessageFactory.getStringMessage( locale , "g.KeystrokesPerHourX" , new String[]{ Integer.toString( Math.round( metaScores[2] ) )} ) );

                fflag = reportData==null || !reportData.getReportRuleAsBoolean( "hidedataentrygrosserrors" );
                if( fflag )
                    caveatList.add( MessageFactory.getStringMessage( locale , "g.GrossErrorsXOfY" , new String[]{ Integer.toString( Math.round( metaScores[5]) ), Integer.toString( Math.round( metaScores[6]))} ) );

                fflag = reportData==null || !reportData.getReportRuleAsBoolean( "hidedataentryaaksph" );
                if( fflag )
                    caveatList.add( MessageFactory.getStringMessage( locale , "g.KeystrokesPerHourAccAdjX" , new String[]{ Integer.toString( Math.round( metaScores[3] ) )} ) );

                fflag = reportData==null || !reportData.getReportRuleAsBoolean( "hidedataentryaccuracy" );
                if( fflag )
                    caveatList.add( MessageFactory.getStringMessage( locale , "g.AccuracyX" , new String[]{ Integer.toString( Math.round( metaScores[4] ) )} ) );

                fflag = reportData!=null && reportData.getReportRuleAsBoolean( "showdataentryseconds" ) && metaScores.length>7 && metaScores[7]>0;
                if( fflag )
                    caveatList.add( MessageFactory.getStringMessage( locale , "g.AvgSecondsPerPageX" , new String[]{ Integer.toString( Math.round( metaScores[7] ) )} ) );

                // LogService.logIt( "SimCompetencyScore.calculateScore() Scored Data Entry Class. " + this.getName() + ", metascores[2,3,4,5]=" + metaScores[2] + ","+ metaScores[3] + ","+ metaScores[4] + ","+ metaScores[5] + "," +  metaScores[6] + ", caveatList.size=" + caveatList.size() );
            }


            /* MOVED above to support use of topic map for competency scoring.
            else if( getSimCompetencyClass().getProducesTopics() )
            {
                // TO DO!
                caveatList = new ArrayList<>();

                Map<String,int[]> topicMap = new TreeMap<>();
                Map<String,int[]> tm = null;

                int[] d1;
                int[] d2;



                for( SimletCompetencyScore scr : smltCompScrList )
                {
                    //map of topic name, int[]
                    //    int[0] = number correct        ( for this item this means points )
                    //    int[1] = number total this topic.  max points for competency
                    //    int[2] = number of items that were partially correct.  ( 0 )
                    //    int[3] = number of items total for this topic from this SimletCompetencyScore
                    tm=scr.getTopicMap();

                    if( tm ==null )
                        continue;

                    for( String k : tm.keySet() )
                    {
                        d1=tm.get(k);

                        // no new items for this topic in simlet competency score
                        if( d1==null )
                            continue;

                        // get existing values
                        d2=topicMap.get(k);


                         //* map of topic name, int[]
                         //*    int[0] = number correct average value (if not a sum type) ( for this item this means points )
                         //*    int[1] = number total this topic.  max points for competency
                         //*    int[2] = number of items that were partially correct.  ( 0 )
                         //*    int[3] = number of items total for this topic from this SimletCompetencyScore
                         //*    int[4] = Flag to be used downstream. 0 means add items (default). 1 means average number correct using total number of items in int[3].
                        if( d2==null )
                            d2 = new int[5];

                        // Add scores or number of correct
                        d2[0] += d1[0];

                        // Total.
                        if( scr.getCompetencyScoreType().isAddTotalsForTopics() )
                        {
                            // Add totals or max points
                            d2[1] += d1[1];
                            d2[4] = 0;
                        }
                        else
                        {
                            // just use the steady max
                            d2[1] = d1[1];

                            // set flag
                            d2[4] = 1;
                        }

                        // partially correct items questions
                        d2[2] += d1[2];

                        // Sum up total items.
                        d2[3] += d1[3];

                        topicMap.put( k, d2 );
                    }
                }

                for( String k : topicMap.keySet() )
                {
                    d2 = topicMap.get(k);

                    // Not all topics are sums, if they are average, then do the average here.
                    if( d2[4]==1 && d2[3]>0 )
                    {
                        // total points / number of items = average per item.
                        float avg = ((float) d2[0])/((float) d2[3]);

                        d2[0] = ((int) avg);
                    }

                    caveatList.add( Constants.TOPIC_KEY + "~" + k + "~" + d2[0] + "~" + d2[1] + "~" + d2[2] );
                }
            }
            */

            if( getSimCompetencyClass().equals( SimCompetencyClass.SCOREDAUDIO ) )
            {
                // TO DO!
                caveatList = new ArrayList<>();
            }

            if( getSimCompetencyClass().equals( SimCompetencyClass.SCOREDAVUPLOAD ) )
            {
                // TO DO!
                caveatList = new ArrayList<>();
            }

            if( getSimCompetencyClass().equals( SimCompetencyClass.SCOREDESSAY ) )
            {
                // LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.9 ScoredEssay metaScores.length=" + metaScores.length + ", metaScores[6]=" + metaScores[6] + ", metaScores[10]=" + metaScores[10] );
                Locale locale = getReportingLocale(); // I18nUtils.getLocaleFromCompositeStr( testEvent.getLocaleStr() );

                int plagiarizedCount = 0;
                float wpm = 0;
                float totalWords = 0;
                if( metaScores.length>=11 && metaScores[6]>0 && metaScores[10]>0 )
                {
                    totalWords += metaScores[6];
                    wpm += metaScores[6]*metaScores[10];
                }
                if( totalWords>0 )
                    wpm = wpm/totalWords;

                float highWpm = 0;

                if( caveatList==null )
                    caveatList=new ArrayList<>();

                for( String cav : caveatList )
                {
                    if( cav != null && cav.contains( "[" + Constants.ESSAY_PLAGIARIZED + "]" ))
                        plagiarizedCount++;

                    if( cav!=null && cav.contains( "[" + Constants.ESSAY_HIGH_WPM + "]" ))
                    {
                        // LogService.logIt( "SimCompetencyScore.calculateScore() GGG.1: caveat String=" + cav + ", testEventId=" + (this.testEvent==null ? "null" : this.testEvent.getTestEventId()) );
                        String cv = StringUtils.getBracketedArtifactFromString(cav, Constants.ESSAY_HIGH_WPM);

                        if( cv!=null && !cv.isBlank() )
                            highWpm = Math.max(highWpm, Float.parseFloat(cv) );
                        else
                            LogService.logIt( "SimCompetencyScore.calculateScore() NONFATAL: Caveatlist contains High WPM BUT no value after brackets! cv=" + cv + ", caveat String=" + cav + ", testEventId=" + (this.testEvent==null ? "null" : this.testEvent.getTestEventId()) );
                    }
                }

                // redo caveats
                caveatList = new ArrayList<>();

                if( plagiarizedCount > 0 )
                {
                    String temp;

                    if( getTotalScorableItems() > 1 )
                        temp = MessageFactory.getStringMessage( locale , "g.EssayPlagiarizedXofY" , new String[]{ Integer.toString( plagiarizedCount ), Integer.toString( (int)getTotalScorableItems() ) } );
                    else
                        temp = MessageFactory.getStringMessage( locale , "g.EssayPlagiarized" , null );

                    caveatList.add( temp );

                    forcedRiskFactorsList.add( temp );
                }

                // If no plagiarized or if some unplagiarized
                if( plagiarizedCount <=0 || getTotalScorableItems()>plagiarizedCount )
                {
                    // If there is a machine score present
                    if( metaScores[2] > 0 )
                    {
                        caveatList.add( MessageFactory.getStringMessage( locale , "g.EssayMachineScoreX" , new String[]{ Integer.toString( Math.round( metaScores[2] ) )} ) );
                        caveatList.add( MessageFactory.getStringMessage( locale , "g.EssayMachineConfidenceX" , new String[]{ Integer.toString( Math.round( 100*metaScores[3] ) )} ) );
                    }

                    caveatList.add( MessageFactory.getStringMessage( locale , "g.EssaySpellErrorsPer100WordsX" , new String[]{ Integer.toString( Math.round( metaScores[4] ) )} ) );
                    caveatList.add( MessageFactory.getStringMessage( locale , "g.EssayOtherErrorsPer100WordsX" , new String[]{ Integer.toString( Math.round( metaScores[5] ) )} ) );

                    if( metaScores.length>6 && metaScores[6]>0 )
                        caveatList.add( MessageFactory.getStringMessage( locale , "g.EssayWordCountX" , new String[]{ Integer.toString( Math.round( metaScores[6] ) )} ) );

                    // LogService.logIt( "SimCompetencyScore.calculateScore() Essay scoring. metaScores.length=" + metaScores.length +  ", metaScores[6]=" + metaScores[6] );

                }

                if( wpm>0 )
                {
                    String temp = MessageFactory.getStringMessage( locale , "g.EssayAvgWpmX" , new String[]{ NumberUtils.getTwoDecimalFormattedAmount(wpm )} );
                    caveatList.add( temp );
                }

                if( highWpm > 0 )
                {
                    String temp = MessageFactory.getStringMessage( locale , "g.EssayHighWpmX" , new String[]{ NumberUtils.getTwoDecimalFormattedAmount(highWpm )} );

                    caveatList.add( temp );

                    forcedRiskFactorsList.add( temp );
                }

                // translateCompare score
                if( metaScores.length>9 && metaScores[9]>0 )
                {
                    caveatList.add( MessageFactory.getStringMessage( locale , "g.EssayTransCompareScoreX" , new String[]{ Integer.toString( Math.round( 100*metaScores[9] ) )} ) );
                }

                // LogService.logIt( "SimCompetencyScore.calculateScore() SCS.AA.10 ScoredEssay caveatList.size=" + caveatList.size() + ", wpm=" + wpm + ", highWpm=" + highWpm );
            }

            if( getSimCompetencyClass().equals( SimCompetencyClass.SCOREDCHAT ) )
            {
                if( caveatList==null )
                    caveatList = new ArrayList<>();

                Locale locale = getReportingLocale(); // I18nUtils.getLocaleFromCompositeStr( testEvent.getLocaleStr() );
                caveatList.add( MessageFactory.getStringMessage( locale , "g.ChatAvgRespTimeSecsFull" , new String[]{ I18nUtils.getFormattedNumber(locale, metaScores[4], 2 )} ) );
                caveatList.add( MessageFactory.getStringMessage( locale , "g.ChatRapportFull" , new String[]{ I18nUtils.getFormattedNumber(locale, metaScores[2], 1 )} ) );
                caveatList.add( MessageFactory.getStringMessage( locale , "g.ChatNegExpressionsFull" , new String[]{ I18nUtils.getFormattedNumber(locale, metaScores[5], 1 )} ) );
                caveatList.add( MessageFactory.getStringMessage( locale , "g.ChatSpellErrorsRateFull" , new String[]{ I18nUtils.getFormattedNumber(locale, metaScores[3], 2 )} ) );
                // LogService.logIt( "SimCompetencyScore.calculateScore() Scored Data Entry Class. " + this.getName() + ", metascores[2,3,4,5]=" + metaScores[2] + ","+ metaScores[3] + ","+ metaScores[4] + ","+ metaScores[5] + "," +  metaScores[6] + ", caveatList.size=" + caveatList.size() );
            }


            if( scaledScoreCeiling != 0 && scaledScore > scaledScoreCeiling )
            {
                // LogService.logIt( "SimCompetencyScore.calculateScore() Applying ScaledScoreCeiling " + scaledScoreCeiling + " to score of " + scaledScore );
                scaledScore = scaledScoreCeiling;
            }

            if( scaledScoreFloor != 0 && scaledScore < scaledScoreFloor )
            {
                // LogService.logIt( "SimCompetencyScore.calculateScore() Applying scaledScoreFloor " + scaledScoreFloor + " to score of " + scaledScore );
                scaledScore = scaledScoreFloor;
            }

            // LogService.logIt( "SimCompetencyScore.calculateScore() END, " + getName() + ", Final scaled score=" + scaledScore + ", totalScorableItems=" + totalScorableItems + ", rawScore=" + rawScore );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "SimCompetencyScore.calculateScore() " + e.toString() + ", " + toString() );
            scaledScore = 0;
            rawScore = 0;
            throw e;

        }
    }

    
    private int getForcedTotalItemsFromDescrip( String topicName )
    {
        if( this.simCompetencyObj==null || topicName==null || topicName.isBlank() )
            return 0;
        
        if( topicName.equals( "NOTOPIC" ))
            return simCompetencyObj.getFrcmaxitems();
        
        if( simCompetencyObj.getCt5Subtopics()==null || simCompetencyObj.getCt5Subtopics().getCt5Subtopic().isEmpty() )
            return 0;
        
        String n;
        for( SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic stp : simCompetencyObj.getCt5Subtopics().getCt5Subtopic() )
        {
            n = StringUtils.getUrlDecodedValue( stp.getName() );
            if( n!=null && n.equals(topicName ) )
                return stp.getFrcmaxitems();
        }
        return 0;
    }

    public boolean isPendingExternalScores()
    {
        return pendingExternalScores;
    }

    public float getMetaScore( int idx )
    {
        if( metaScores==null || metaScores.length<idx+1 )
            return 0;

        return metaScores[idx];
    }



    public void calculateAggregateScore()
    {
        SimCompetencyClass scc = getSimCompetencyClass();

        if( !scc.getIsAggregate() )
            return;

        totalScorableItems = 0;
        totalPoints = 0;
        totalMaxPoints = 0;

        boolean incrMaxPoints = true;

        if( useTotalItems && simCompetencyObj.getMaxpoints() != null && !simCompetencyObj.getMaxpoints().isEmpty() )
        {
            incrMaxPoints = false;
            totalMaxPoints = Float.parseFloat( simCompetencyObj.getMaxpoints() ); // scc.getAggregateMaxPoints( InteractionScoreUtils.getPointsArray( simCompetencyObj.getMaxpoints() ) );
        }

        //LogService.logIt( "SimCompetencyScore.calculateAggregateScore() " + scc.getName() + ", maxPoinsStr=" + simCompetencyObj.getMaxpoints() + ", scanning responses: " + testEvent.getAutoScorableResponseList().size() );

        // Aggregate scores are always points accumulation and are always calculated across all simlets..
        for( ScorableResponse sr : testEvent.getAutoScorableResponseList() )
        {
                totalPoints += sr.getAggregateItemScore( scc );

                if( incrMaxPoints )
                    totalMaxPoints += scc.getAggregateMaxPoints( sr.getMaxPointsArray() );

                // Add in repeat exposures - this is now done when XML is generated during Sim Build
                //else if( sr.requiresMaxPointIncrement() )
                //    totalMaxPoints += scc.getAggregateMaxPoints( sr.getMaxPointsArray() );
        }

       //LogService.logIt( "SimCompetencyScore.calculateAggregateScore() " + scc.getName() + " AFTER totalPoints=" + totalPoints +", totalMaxPoints=" + totalMaxPoints );


        if( totalMaxPoints > 0 )
            hasScoreableData = true;

        if( !hasScoreableData )
            return;

        fractionOfPoints = totalMaxPoints > 0 ? totalPoints / totalMaxPoints : 0;

        ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scoreFormatTypeId );

        // this is used by the logic to create TestEventScore objects.
        competencyScoreType = simCompetencyObj.getStddeviation()==0 ? CompetencyScoreType.PERCENT_OF_TOTAL : CompetencyScoreType.NORM_SCALE_POINTS;


        rawScore = scoreFormatType.getUnweightedRawScore(   competencyScoreType,
                                                               fractionOfPoints,
                                                               totalPoints,
                                                               0,
                                                               0);


        scaledScore = scoreFormatType.getUnweightedScaledScore(competencyScoreType, rawScore, 0, simCompetencyObj.getLookuptable() );



        //scaledScore = scoreFormatType.getUnweightedScore(  competencyScoreType, // simCompetencyObj.getStddeviation()==0 ? CompetencyScoreType.PERCENT_OF_TOTAL : CompetencyScoreType.NORM_SCALE_POINTS,
        //                                             fractionOfPoints,
        //                                             totalPoints,
        //                                             simCompetencyObj.getMean(),
        //                                             simCompetencyObj.getStddeviation());

        // rawScore = simCompetencyObj.getStddeviation()==0 ? fractionOfPoints : totalPoints;

         // LogService.logIt( "SimCompetencyScore.calculateAggregateScore() " + scc.getName() + " AFTER totalPoints=" + totalPoints +", totalMaxPoints=" + totalMaxPoints + ", score=" + scaledScore + ", rawScore=" + rawScore );
    }


    public int getScoreCategoryTypeId( ScoreColorSchemeType scst )
    {
        if( !getHasScoreableData() )
            return ScoreCategoryType.UNRATED.getScoreCategoryTypeId();

        ScoreCategoryType scoreCat = ScoreCategoryType.getForScore(scst,
                                                                    scaledScore,
                                                                    simCompetencyObj.getHighcliffmin(),
                                                                    simCompetencyObj.getWhitemin(),
                                                                    simCompetencyObj.getGreenmin(),
                                                                    simCompetencyObj.getYellowgreenmin(),
                                                                    simCompetencyObj.getYellowmin(),
                                                                    simCompetencyObj.getRedyellowmin(),
                                                                    simCompetencyObj.getRedmin(),
                                                                    0,
                                                                    simCompetencyObj.getCategorydisttype(),
                                                                    simCompetencyObj.getHighclifflevel() );

        if( simCompetencyObj.getCategoryadjustmentthreshold()>0 && scaledScore <= simCompetencyObj.getCategoryadjustmentthreshold() )
            scoreCat = scoreCat.adjustOneLevelUp( scst );

        return scoreCat.getScoreCategoryTypeId();
    }


    protected void setSimCompetencyScoreTypes() throws Exception
    {
        rawScoreCalcType = SimCompetencyRawScoreCalcType.getValue( simCompetencyObj.getRawscorecalctypeid() );
        scaledScoreCalcType = SimCompetencyScaledScoreCalcType.getValue( simCompetencyObj.getScaledscorecalctypeid() );

        // LogService.logIt( "SimCompetencyScore.setSimCompetencyScoreTypes() simCompetencyObj.getRawscorecalctypeid()=" + simCompetencyObj.getRawscorecalctypeid() + ", simCompetencyObj.getScaledscorecalctypeid()=" + simCompetencyObj.getScaledscorecalctypeid() + ", rawScoreCalcType=" + rawScoreCalcType.getName() );

        if( scaledScoreCalcType.getIsTransform() && !rawScoreCalcType.getIsZScore() )
            throw new Exception( "Raw score calc type must be Z-score if Scaled Score calc type is transform. " );

        if( scaledScoreCalcType.getIsTransform() && simCompetencyObj.getScaledstddeviation() ==0 )
        {
            LogService.logIt("SimCompetencyScore.setSimCompetencyScoreTypes() STERR Scaled score calc type is transform but Scaled Standard Deviation is 0! Shifting to DEFAULT." );
            scaledScoreCalcType = SimCompetencyScaledScoreCalcType.DEFAULT;
        }

        if( rawScoreCalcType.getIsZScore() && this.simCompetencyObj.getStddeviation()<=0 )
        {
            //if( simletCompetencyStat== null )
            //{
                LogService.logIt( "SimCompetencyScore.setSimCompetencyScoreTypes() SimCompetency requires stats but No standard deviation available for Z-Score Transformation. Changing Score type to default." + this.toString() );
                rawScoreCalcType = SimCompetencyRawScoreCalcType.DEFAULT;
                scaledScoreCalcType = SimCompetencyScaledScoreCalcType.DEFAULT;
            //}

            //else if( !simletCompetencyStat.getHasDataForScoreCals() )
            //{
            //    LogService.logIt( "SimCompetencyScore.setSimCompetencyScoreTypes() SimCompetency requires stats but SimletCompetencyStat does not have score calc information.  Changing Score type to default." + this.toString() );
            //    rawScoreCalcType = SimCompetencyRawScoreCalcType.DEFAULT;
            //    scaledScoreCalcType = SimCompetencyScaledScoreCalcType.DEFAULT;
            //}
        }
    }

    public String getScoreCategoryInfoString( ScoreColorSchemeType scst )
    {
        return scst.getScoreCategoryInfoString( getSimCompetencyObj(), scoreFormatTypeId );
    }


    public List<TextAndTitle> getItemScoreTextAndTitleList()
    {
        return itemScoreTextAndTitleList;
    }

    public float getUserWeight()
    {
        return simCompetencyObj.getWeight();
    }


    public float getUserWeightedScaledScore( boolean forOverall )
    {
        return getUserWeight() * getUnweightedScaledScore( forOverall );
    }



    public float getImportanceValueWeightedScaledScore( boolean forOverall )
    {
        return getImportanceValueWeight() * getUnweightedScaledScore( forOverall );

    }

    public float getImportanceValueWeight()
    {
        if( !isOnet() || simCompetencyObj.getOnetimportance() <= 0 )
            return 1f;

        return simCompetencyObj.getOnetimportance();
    }

    public float getRankValueWeight()
    {
        if( !isOnet() || simCompetencyObj.getRankvalue() <= 0 )
            return 1f;

        return simCompetencyObj.getRankvalue();
    }

    public float getRankValueWeightedScaledScore( boolean forOverall )
    {
        return getRankValueWeight() * getUnweightedScaledScore( forOverall );
    }

    public float getRawScore()
    {
        return rawScore;
    }


    public float getUnweightedScaledScore( boolean forOverall )
    {
        // if we need to use the categoryScore for overall
        if( forOverall && simCompetencyObj.getUsecategforoverall()==1 && ScoreFormatType.getValue( scoreFormatTypeId ).isNumeric() )
        {
            int catId = getScoreCategoryTypeId( ScoreColorSchemeType.getType( scoreColorSchemeTypeId ));

            return ScoreCategoryType.getValue( catId ).getNumericEquivScore( scoreFormatTypeId );
        }

        return scaledScore;
    }

    public boolean isOnet()
    {
        return simCompetencyObj.getType() == SimCompetencyType.ONET_ELEMENT.getSimCompetencyTypeId() || simCompetencyObj.getType() == SimCompetencyType.ONET_GROUP.getSimCompetencyTypeId();
    }


    public boolean getHasScoreableData()
    {
        return hasScoreableData;
    }

    /*
    public String getTextBaseResponseString()
    {
        try
        {
            StringBuilder sb = new StringBuilder();

            if( textBasedResponseList == null )
                return sb.toString();

            for( TextAndTitle tat : textBasedResponseList )
            {
                if( tat.isValidForReport() )
                {
                    if( sb.length()>0 )
                        sb.append( Constants.DELIMITER );

                    sb.append( tat.getTitle() + Constants.DELIMITER + tat.getText() );
                    // sb.append( XMLUtils.encodeURIComponent( tat.getTitle()  ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( tat.getText() )  );
                }
            }

            return sb.toString();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "SimCompetencyScore.getTextBaseResponseString() " + toString() );

            return null;
        }
    }
    */


    public void collectInterviewQuestions()
    {


        interviewQuestionList = new ArrayList<>();

        interviewQuestionList.addAll( scoreTextInterviewQuestionList );

        // Do not include standard interview questions if there are scoreText interview questions present.
        if( simCompetencyObj == null || !interviewQuestionList.isEmpty() )
            return;

        InterviewQuestion iq;

        // LogService.logIt( "SimCompetencyScore.collectInterviewQuestions() found " + simCompetencyObj.getInterviewquestion().size() + " total questions in SimCompetency" );

        for( InterviewQuestionObj iqo : simCompetencyObj.getInterviewquestion() )
        {
            iq = new InterviewQuestion( simCompetencyObj );

            iq.load( iqo );

            interviewQuestionList.add( iq );
        }
    }



    public String getScoreText( ScoreColorSchemeType scst )
    {
        if( simCompetencyObj == null )
            return null;

        String t = "";

        /*
        if( caveatList != null )
        {
            for( String ct : caveatList )
            {
                if( ct == null || ct.isEmpty() )
                    continue;

                if( !t.isEmpty() )
                    t += "\n";

                t += "- " + ct + "\n";
            }

            if( !t.isEmpty() )
                t += "\n";
        }
        */

        CategoryDistType cdt = CategoryDistType.getValue( simCompetencyObj.getCategorydisttype() );

        if( (cdt==null || cdt.getLinear() ) &&  simCompetencyObj.getHighcliffmin()> 0 && simCompetencyObj.getHighclifflevel()>0 && scaledScore >= simCompetencyObj.getHighcliffmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getHighclifftext() == null ? "" : simCompetencyObj.getHighclifftext() );

        else if( scaledScore >= simCompetencyObj.getGreenmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getGreentext() == null ? "" : simCompetencyObj.getGreentext() );

        else if( scst.getIsFiveColor() && scaledScore >= simCompetencyObj.getYellowgreenmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowgreentext() == null ? "" : simCompetencyObj.getYellowgreentext() );

        else if( scaledScore >= simCompetencyObj.getYellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowtext()==null ? "" : simCompetencyObj.getYellowtext() );

        else if( scst.getIsFiveColor() && scaledScore >= simCompetencyObj.getRedyellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedyellowtext() == null ? "" : simCompetencyObj.getRedyellowtext() );

        else
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedtext() == null ? "" : simCompetencyObj.getRedtext() );

       return t;
    }





    protected Locale getReportingLocale()
    {
        if( testEvent!=null )
        {
            if( testEvent!=null && testEvent.getLocaleStrReport()!=null && !testEvent.getLocaleStrReport().isEmpty() )
                return I18nUtils.getLocaleFromCompositeStr( testEvent.getLocaleStrReport() );

            if( testEvent.getLocaleStr()!=null && !testEvent.getLocaleStr().isEmpty() )
                return I18nUtils.getLocaleFromCompositeStr( testEvent.getLocaleStr() );
        }

        return Locale.US;
    }

    @Override
    public float getWeightUsed() {
        return weightUsed;
    }

    @Override
    public void setWeightUsed(float weightUsed) {
        this.weightUsed = weightUsed;
    }

    public List<InterviewQuestion> getInterviewQuestionList() {
        return interviewQuestionList;
    }

    public List<TextAndTitle> getTextBasedResponseList() {
        return textBasedResponseList;
    }

    public float getTotalScoreValue() {
        return totalScoreValue;
    }

    public float getFractionScoreValue() {
        return fractionScoreValue;
    }

    public Simcompetency getSimCompetencyObj() {
        return simCompetencyObj;
    }

    public float getTotalScorableItems() {
        return totalScorableItems;
    }

    public float getTotalMaxPoints() {
        return totalMaxPoints;
    }

    public CompetencyScoreType getCompetencyScoreType()
    {
        return competencyScoreType;
    }

    public float getAveragePoints() {
        return averagePoints;
    }

    public void setScoreFormatTypeId(int scoreFormatTypeId) {
        this.scoreFormatTypeId = scoreFormatTypeId;
    }

    public void setScoreColorSchemeTypeId(int scoreColorSchemeTypeId) {
        this.scoreColorSchemeTypeId = scoreColorSchemeTypeId;
    }

    public List<String> getCaveatList() {
        return caveatList;
    }

    public void setCaveatList( List<String> cl ) {
        caveatList = cl;
    }

    public float getTotalCorrect() {
        return totalCorrect;
    }

    public float getTotalPoints() {
        return totalPoints;
    }

    public List<String> getForcedRiskFactorsList() {
        return forcedRiskFactorsList;
    }

    public void setForcedRiskFactorsList(List<String> forcedRiskFactorsList) {
        this.forcedRiskFactorsList = forcedRiskFactorsList;
    }

    public List<InterviewQuestion> getScoreTextInterviewQuestionList() {
        return scoreTextInterviewQuestionList;
    }

    public List<MergableScoreObject> getMergableScoreObjectList() {
        return mergableScoreObjectList == null ? new ArrayList<>() : mergableScoreObjectList;
    }

    //public float getComboWeight() {
    //    return comboWeight;
    //}

    //public void setComboWeight(float comboWeight) {
    //    this.comboWeight = comboWeight;
    //}

    public void setReportData(ReportData reportData) {
        this.reportData = reportData;
    }

    public boolean getBoolean1() {
        return boolean1;
    }

    public void setBoolean1(boolean boolean1) {
        this.boolean1 = boolean1;
    }


}
