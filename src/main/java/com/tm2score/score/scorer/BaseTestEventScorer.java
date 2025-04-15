/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.score.iactnresp.IactnResp;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.EventFacade;
import com.tm2score.event.NormFacade;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.global.Constants;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest2.cefr.CefrScoreType;
import com.tm2score.custom.coretest2.cefr.CefrType;
import com.tm2score.custom.coretest2.cefr.CefrUtils;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.Percentile;
import com.tm2score.entity.event.TestEventArchive;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.proctor.SuspiciousActivity;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.User;
import com.tm2score.event.OnlineProctoringType;
import com.tm2score.event.PercentileScoreType;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TESScoreComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.global.DisplayOrderComparator;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.UserRankComparator;
import com.tm2score.global.WeightedObjectComparator;
import com.tm2score.proctor.ProctorFacade;
import com.tm2score.proctor.ProctorHelpUtils;
import com.tm2score.proctor.ProctorUtils;
import com.tm2score.proctor.SuspiciousActivityType;
import com.tm2score.profile.ProfileUtils;
import com.tm2score.report.ReportData;
import com.tm2score.score.simcompetency.ComboSimCompetencyScore;
import com.tm2score.score.ComboSimCompetencyScoreUtils;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.MergableScoreObjectCombiner;
import com.tm2score.report.ReportRules;
import com.tm2score.report.SampleReportUtils;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.score.SimCompetencyGroup;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.metascorer.IbmInsightMetaScorer;
import com.tm2score.score.metascorer.MetaScorer;
import com.tm2score.service.Tracker;
import com.tm2score.sim.CategoryDistType;
import com.tm2score.sim.OverallRawScoreCalcType;
import com.tm2score.sim.OverallScaledScoreCalcType;
import com.tm2score.sim.ScorePresentationType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyCombinationType;
import com.tm2score.sim.SimCompetencyRawScoreCalcType;
import com.tm2score.sim.SimCompetencySortType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.sim.SimJUtils;
import com.tm2score.simlet.SimletItemType;
import com.tm2score.user.UserFacade;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.JaxbUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class BaseTestEventScorer
{
    public Locale reportLocale;

    // @Inject
    public EventFacade eventFacade;
    public UserFacade userFacade;

    public NormFacade normFacade;

    public TestKey tk;
    public TestEvent te;
    public SimDescriptor sd;

    public List<String> forcedRiskFactorsList;

    public List<MetaScorer> metaScorerList;

    public boolean validItemsCanHaveZeroMaxPoints = false;


    public StringBuilder scrSum;
    public User user = null;
    public boolean hasCustomColorRanges = true;
    public Locale simLocale = null;

    public float ovrRawScr = 0;
    public float ovrScaledScr = 0;

    public float normRawScore = -99;

    public float totalWeights = 0;
    public int nonlinearSimCompetencyCount=0;

    // boolean useRankValuesAsWeights = te.getSimXmlObj().getOverallscorecalctype()==OverallScoreCalcType.SIMCOMPETENCYRANKS.getOverallScoreCalcTypeId(); //  .getUserankvaluesasweights()==1;

    public boolean hasWeights = false;

    public int scoreFormatTypeId; //  = te.getScoreFormatTypeId(); //  te.getSimXmlObj().getScoreformat();

    public ScoreColorSchemeType scoreColorSchemeType; //  = ScoreColorSchemeType.getValue( te.getSimXmlObj().getScorecolorscheme() );

    // LogService.logIt("BaseTestEventScorer.score() te.getSimXmlObj().getScorecolorscheme()=" + te.getSimXmlObj().getScorecolorscheme() );

    public OverallRawScoreCalcType overallRawScoreCalcType; //  = OverallRawScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype() );

    public OverallScaledScoreCalcType overallScaledScoreCalcType; //  = OverallScaledScoreCalcType.getValue( te.getSimXmlObj().getOverallscaledscorecalctype() );

    public boolean pendingExternalScores = false;

    public int scsNotScorableCt = 0;

    public int scsScorableCt = 0;
    public int scsScorableZeroCt = 0;
    public float scsScr;
    //float wtUsed;

    public float weightPct = 0;

    public String competencyName;
    public String frcCountry = null;

    public List<ItemResponse> oldItemResponseList;
    public List<TestEventScore> oldTestEventScoreList;

    public ReportRules reportRules;






    public void initForScoring(boolean skipVersionCheck) throws Exception
    {
        // LogService.logIt( "BaseTestEventScorer.initForScoring() starting process. TestEvent " + te.toString() );

        scrSum = new StringBuilder();

        // scrSum.append( "BaseTestEventScorer.initForScoring() START " + te.toString() + "\n" );

        if( te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED.getTestEventStatusTypeId() &&
            te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
            throw new ScoringException( "TestEvent is not correct status type. Expecting completed or completed pending external. ", ScoringException.NON_PERMANENT, te );

        if( eventFacade == null )
            eventFacade = EventFacade.getInstance();

        if( userFacade == null )
            userFacade = UserFacade.getInstance();

        if( tk==null )
            tk = te.getTestKey();

        if( tk==null )
            tk = eventFacade.getTestKey( te.getTestKeyId(), true );

        te.setTestKey(tk);

        if( tk.getAuthUser()==null )
            tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));

        // Ensure we have these so load from DBMS
        oldItemResponseList = eventFacade.getItemResponsesForTestEvent( te.getTestEventId() );

        //if( tk.getProduct()==null )
        //    tk.setProduct( eventFacade.getProduct( tk.getProductId() ) );

        if( te.getTestEventScoreList()==null )
            te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ) );

        oldTestEventScoreList= new ArrayList<>(); // te.getTestEventScoreList();
        oldTestEventScoreList.addAll( te.getTestEventScoreList() );
        for( TestEventScore tes : oldTestEventScoreList )
            tes.setTempBoolean1(false);


        //UserFacade userFacade = null;

        //User user = null;

        forcedRiskFactorsList = new ArrayList<>();

        if( te.getSimDescriptor()==null )
            te.setSimDescriptor(sd);

        if( te.getSimDescriptor() == null )
            throw new ScoringException( "Cannot find SimDescriptor for SimDescriptorId=" + te.getProduct().getSimDescriptorId(), ScoringException.PERMANENT, te );

        if( te.getSimDescriptor().getXml() == null || te.getSimDescriptor().getXml().isEmpty() )
            throw new ScoringException( "SimDescriptor XML is empty. simDescriptorId=" + te.getProduct().getSimDescriptorId(), sd.getSimDescriptorId()>0 ? ScoringException.PERMANENT : ScoringException.NON_PERMANENT, te );

        te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( te.getSimDescriptor().getXml() ) );

        if( te.getSimXmlObj().getSimid() != te.getSimId() || te.getSimXmlObj().getSimverid() != te.getSimVersionId() )
        {
            if( skipVersionCheck && te.getSimXmlObj().getSimid() == te.getSimId() )
                LogService.logIt( "BaseTestEventScorer.initForScoring() TestEventId=" + te.getTestEventId() + " has a MISMATCH between sim versions between TestEvent and SimDescriptor: TestEvent: " + te.getSimId() + ", v" + te.getSimVersionId() + ", SimDescriptor: " + te.getSimXmlObj().getSimid() + " v" + te.getSimXmlObj().getSimverid() + " IGNORING BECAUSE SkipVersionCheck is checked." );

            else
                throw new ScoringException( "Cannot score TestEvent because mismatch between simIds and/or versions between TestEvent and SimDescriptor: TestEvent: " + te.getSimId() + ", v" + te.getSimVersionId() + ", SimDescriptor: " + te.getSimXmlObj().getSimid() + " v" + te.getSimXmlObj().getSimverid(), sd.getSimDescriptorId()>0 ? ScoringException.PERMANENT : ScoringException.NON_PERMANENT, te );
        }

        // boolean hasCustomColorRanges = true;

        if( te.getProfile()!=null && te.getProfile().getProfileUsageType().getIsReportRanges() && te.getProfile().getStrParam2()!=null && !te.getProfile().getStrParam2().isEmpty() )
        {
            applyProfileToSimXmlObj();
            // hasCustomColorRanges = true;
        }

        if( te.getProduct()== null )
            te.setProduct( eventFacade.getProduct( te.getProductId() ) );

        if( te.getOrg()== null )
            te.setOrg( userFacade.getOrg( te.getOrgId() ) );

        if( te.getProduct().getProductType().getUsesImoResultXml() )
        {
            // Get the score xml and it's object.
            if( te.getResultXml() == null || te.getResultXml().isEmpty() )
                throw new ScoringException( "Result XML is empty. ", ScoringException.PERMANENT, te );

            te.setResultXmlObj( JaxbUtils.ummarshalImoResultXml( te.getResultXml() ) );

            if( te.getTotalTestTime()<=0 )
                te.setTotalTestTime( te.getResultXmlObj().getEvent().getTtime() );
        }

        reportRules = new ReportRules( te.getOrg(), null, te.getProduct(), null, null );

        simLocale = I18nUtils.getLocaleFromCompositeStr( te.getSimXmlObj().getLang() );

        if( te.getUserId()>0 && tk.getUserId()<= 0 )
        {
            // LogService.logIt( "BaseTestEventScorer.initForScoring() Setting TestKey.userId to value found in TestEvent " + te.toString()  );

            tk.setUserId( te.getUserId() );
            eventFacade.saveTestKey(tk);
        }


        if( !validItemsCanHaveZeroMaxPoints )
        {
            String initVals = te.getSimXmlObj().getInitvals();

            if( initVals!=null && !initVals.isBlank() )
            {
                int iv1 = getInitValAsInt( 1001, te.getSimXmlObj().getInitvals() );
                validItemsCanHaveZeroMaxPoints = iv1==1; //  iv1!= null && !iv1.isBlank() && (iv1.equals( "1" ) || iv1.equals( "1.0") );
            }
        }

        // LogService.logIt( "BaseTestEventScorer.initForScoring() End TestEventId=" + te.getTestEventId() + ", validItemsCanHaveZeroMaxPoints=" + validItemsCanHaveZeroMaxPoints  );
    }


    public void initTestEvent() throws Exception
    {
            // LogService.logIt( "BaseTestEventScorer.scoreTestEvent() starting to score TestEvent " + te.toString() + ", simXmlObj contains " + te.getSimXmlObj().getSimlet().size() + " simlets. reportLocale=" + reportLocale.toString() + ", te.getSimXmlObj().getScoreformat()=" + te.getSimXmlObj().getScoreformat() );

            // Indicate scoring has started.
            te.setTestEventStatusTypeId( TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() );

            te.setScoreFormatTypeId( te.getSimXmlObj().getScoreformat() );
            te.setScoreColorSchemeTypeId( te.getSimXmlObj().getScorecolorscheme() );
            te.setEducTypeId( te.getSimXmlObj().getEduc() );
            te.setExperTypeId( te.getSimXmlObj().getExper() );
            te.setTrainTypeId( te.getSimXmlObj().getTrn() );
            te.setStdHraScoring( SimJUtils.getHasAnyNormativeScoring( te.getSimXmlObj() ) && (reportRules==null || !reportRules.getReportRuleAsBoolean("bellgraphsoff")) ? 1 : 0 );

            setExcludeFmNorms();

            Date startDate = te.getStartDate();
            Date lastDate = te.getLastAccessDate();

            if( te.getTotalTestTime()<=0 && startDate!=null && lastDate!=null && lastDate.getTime()>startDate.getTime() )
            {
                float ttime = ( (float)(lastDate.getTime() - startDate.getTime()) )/1000f;

                te.setTotalTestTime( ttime );
            }

            if( te.getLocaleStrReport()==null || te.getLocaleStrReport().isEmpty() )
            {
                Locale l = getBaseReportingLocale();

                if( l!=null )
                    te.setLocaleStrReport( l.toString() );
            }

            eventFacade.saveTestEvent(te);

            if( te.getProduct()==null )
                te.setProduct( eventFacade.getProduct( te.getProductId() ));

            if( te.getOrg()==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                te.setOrg( userFacade.getOrg( te.getOrgId() ) );
            }

            if( te.getSuborg()==null && te.getSuborgId()>0 )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                te.setSuborg( userFacade.getSuborg( te.getSuborgId() ) );
            }
            if( te.getUser()==null && te.getUserId()>0 )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                te.setUser( userFacade.getUser( te.getUserId() ));
            }

            if( te.getReport()==null && te.getReportId()>0 )
            {
                if( eventFacade==null )
                    eventFacade = EventFacade.getInstance();
                te.setReport( eventFacade.getReport( te.getReportId() ));
            }

            // OK initialize all the score data.
            te.initScoreAndResponseLists( validItemsCanHaveZeroMaxPoints );

            for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
                scs.setReportData(getReportDataForScoring());

            scoreFormatTypeId = te.getScoreFormatTypeId(); //  te.getSimXmlObj().getScoreformat();

            scoreColorSchemeType = ScoreColorSchemeType.getValue( te.getSimXmlObj().getScorecolorscheme() );

            // LogService.logIt("BaseTestEventScorer.score() te.getSimXmlObj().getScorecolorscheme()=" + te.getSimXmlObj().getScorecolorscheme() );

            overallRawScoreCalcType = OverallRawScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype() );

            overallScaledScoreCalcType = OverallScaledScoreCalcType.getValue( te.getSimXmlObj().getOverallscaledscorecalctype() );

            if( overallScaledScoreCalcType.equals( OverallScaledScoreCalcType.NORMAL_TRANS ) )
            {
                if( te.getSimXmlObj().getRawtoscaledfloatparam2()==0 )
                    throw new Exception( "BaseTestEventScorer. OverallScaledScoreCalcType set for Norm Transformation but standard deviation (rawToScaledFloatParam2) is 0." );
            }

            for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
            {
                if( scs.getSimCompetencyObj().getWeight()>0 )
                {
                    hasWeights=true;
                    break;
                }
            }
    }


    public float getEquivalentZScore( float score, float mean, float std )
    {
        return std>0 ? (score - mean)/std : 0;
    }



    public ReportData getReportDataForScoring()
    {
        ReportData rd = new ReportData( tk,  te, te.getReport(), te.getUser(), te.getOrg(), te.getProfile() );

        return rd;
    }


    public void scoreSimCompetencies() throws Exception
    {
        List<SimCompetencyScore> comboSimCompetencyScoreList = new ArrayList<>();

        nonlinearSimCompetencyCount=0;

        // PASS 1A score each non-Combo SimCompetency
        for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
        {
            // Store and skip Combos for now.
            if( scs.getSimCompetencyClass().getIsCombo() )
            {
                comboSimCompetencyScoreList.add( scs );
                // continue;
            }

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() AAA.1 Calculating score for " + scs.toString() + ", scoreColorSchemeType.getScoreColorSchemeTypeId()=" + scoreColorSchemeType.getScoreColorSchemeTypeId() );

            //competencyName = scs.getName();

            //if( scs.getNameEnglish()!= null && !scs.getNameEnglish().equalsIgnoreCase( competencyName ) )
            //    competencyName += " (" + scs.getNameEnglish() + ")";

            scs.setScoreFormatTypeId(scoreFormatTypeId);

            scs.setScoreColorSchemeTypeId( scoreColorSchemeType.getScoreColorSchemeTypeId() );

            scs.init();

            // Must come before collecting interview questions.
            scs.calculateScore( te.getSimXmlObj().getUsesmltcompcvrg() );

            if( scs.isPendingExternalScores() )
            {
                // LogService.logIt( "BaseTestEventScorer.scoreTestEvent() Found Pending Scores for: " + scs.toString() );
                pendingExternalScores = true;
            }
        }

        // List<SimCompetencyScore> memberScsL;

        // Pass 1B - Score each Combo SimCompetency
        // skip for pending scores
        if( !pendingExternalScores )
        {
            boolean hasTier2 = false;
            Map<Long,Float> simCompetencyIdWeightMap;
            for( SimCompetencyScore scs : comboSimCompetencyScoreList )
            {
                simCompetencyIdWeightMap = new HashMap<>();
                ComboSimCompetencyScore cscs = new ComboSimCompetencyScore( scs, ComboSimCompetencyScoreUtils.getMembers(scs, te.getSimCompetencyScoreList(), comboSimCompetencyScoreList, simCompetencyIdWeightMap ), te.getAutoScorableResponseList(), te.getTestEventId(), simCompetencyIdWeightMap );
                if( cscs.getTier()>1 )
                {
                    hasTier2=true;
                    continue;
                }
                
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() BBB.1 Calculating score for COMBO Sim Competency:  " + scs.toString() + ", scoreColorSchemeType.getScoreColorSchemeTypeId()=" + scoreColorSchemeType.getScoreColorSchemeTypeId() );

                cscs.calculateScore();
            }
            
            if( hasTier2 )
            {
                for( SimCompetencyScore scs : comboSimCompetencyScoreList )
                {
                    simCompetencyIdWeightMap = new HashMap<>();
                    ComboSimCompetencyScore cscs = new ComboSimCompetencyScore( scs, ComboSimCompetencyScoreUtils.getMembers(scs, te.getSimCompetencyScoreList(), comboSimCompetencyScoreList, simCompetencyIdWeightMap ), te.getAutoScorableResponseList(), te.getTestEventId(), simCompetencyIdWeightMap );
                    if( cscs.getTier()<=1 )
                        continue;

                    if( 1==2 || ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() BBB.1.tier2 Calculating score for COMBO Sim Competency:  " + scs.toString() + ", scoreColorSchemeType.getScoreColorSchemeTypeId()=" + scoreColorSchemeType.getScoreColorSchemeTypeId() );

                    cscs.calculateScore();
                }
            }            
        }

        // now complete the scoring of each SimCompetency
        for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
        {
            competencyName = scs.getName();

            if( scs.getNameEnglish()!= null && !scs.getNameEnglish().equalsIgnoreCase( competencyName ) )
                competencyName += " (" + scs.getNameEnglish() + ")";

            if( scs.isPendingExternalScores() )
            {
                pendingExternalScores = true;
                continue;
            }

            forcedRiskFactorsList.addAll( scs.getForcedRiskFactorsList() );

            scs.collectInterviewQuestions();

            // LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() CCC.1 SimCompetency " + scs.getName() + ", scs.weightUsed=" + scs.getWeightUsed() + ", te.getSimXmlObj().getIncludecompetenciesoverall()=" + te.getSimXmlObj().getIncludecompetenciesoverall() );
            // If competency set to include in overall only if below X and score is above X, skip it.

            if( scs.getSimCompetencyObj().getIncludeinoverallifbelow() > 0 && scs.getUnweightedScaledScore( false ) >= scs.getSimCompetencyObj().getIncludeinoverallifbelow() )
                continue;

            // NOTE: textBasedResponses are collected within the scs.calculateScore method above.
            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() CCC.2 collected text-based responses for SimCompetency " + scs.getName() + ", found: " + scs.getTextBasedResponseList().size() );

            // If we have an Identity Image Capture competency. Don't include in overall score.
            if(  scs.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDIMAGEUPLOAD ) )
                continue;

            // if have non-task type of competency and not supposed to include in overall, skip it.
            if(  ( scs.getSimCompetencyClass().getIsDirectCompetency() || scs.getSimCompetencyClass().getIsAggregate() ) && te.getSimXmlObj().getIncludecompetenciesoverall()!=1 )
                continue;

            if(  scs.getSimCompetencyClass().getIsInterest() && te.getSimXmlObj().getIncludeinterestoverall() != 1 )
                continue;

            if(  scs.getSimCompetencyClass().getIsExperience() && te.getSimXmlObj().getIncludeexperienceoverall() != 1 )
                continue;

            if(  scs.getSimCompetencyClass().getIsBiodata() && te.getSimXmlObj().getIncludebiodataoverall() != 1 )
                continue;

            // If this is supposed to be a z-score but there was no local std deviation to use and no simlet competency stat record, ignore in weight calcs.
            // if( SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid() ).getIsZScore() && scs.getSimCompetencyObj().getStddeviation()<=0 && ( scs.getSimletCompetencyStat()==null || !scs.getSimletCompetencyStat().getHasDataForScoreCals() ) )
            if( SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid() ).getIsZScore() &&
                scs.getSimCompetencyObj().getStddeviation()<=0 )
            {
                LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() CCC.3 Skipping SimCompetency in overall because there is no StdDeviation info present. " + scs.toString() );
                continue;
            }

            // LogService.logIt( "BaseTestEventScorer.scoreTestEvent collected interview questions for SimCompetency " + scs.getInterviewQuestionList().size() );

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies CCC.4 scs=" + competencyName + ", scs.getRawScore()=" + scs.getRawScore() + ", scs.getHasScoreableData()=" + scs.getHasScoreableData() );

            if( overallScaledScoreCalcType==null )
                overallScaledScoreCalcType = OverallScaledScoreCalcType.getValue( te.getSimXmlObj().getOverallscaledscorecalctype() );

            if( overallRawScoreCalcType==null )
                overallRawScoreCalcType = OverallRawScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype() );

            // if overall raw score is a combination of raw comp scores and then it's transformed, it must be a z score.
            // so, if the competency raw score is not a z score, then we should not include this score in the overall.
            if( overallRawScoreCalcType.getUsesRawCompetencyScores() &&
                overallScaledScoreCalcType.getIsTransform() &&
                !SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid() ).getIsZScore()    )
            {
                LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() CCC.5 Skipping SimCompetency in overall because the overall.scaled score is a transform and the overall.raw is a combo of competency raw scores but this competency.raw score is not a z score. " + scs.toString() );
                continue;
            }

            if( ScoreManager.DEBUG_SCORING )
               LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies() DDD.1 SimCompetency " + scs.getName() + ", hasScoreableData=" + scs.getHasScoreableData() );

            if( scs.getHasScoreableData() )
            {
                scsScr = 0;

                scsScorableCt++;

                // These are the importance values used when the SimCompetency was established.
                // Any non-Onet competencies, tasks, groups, aggregates, will get a rank value of 1
                if( overallRawScoreCalcType.equals(OverallRawScoreCalcType.SIMCOMPETENCYRANKS ) ) // scs.isOnet() && useRankValuesAsWeights )
                {
                    scsScr = scs.getRankValueWeightedScaledScore( true );
                    ovrRawScr += scsScr;
                    totalWeights += scs.getRankValueWeight();
                    //wtUsed = scs.getRankValueWeight();
                    scs.setWeightUsed( scs.getRankValueWeight() );
                }

                else if( overallRawScoreCalcType.equals(OverallRawScoreCalcType.ONETIMPORTANCE ) )
                {
                    scsScr = scs.getImportanceValueWeightedScaledScore( true );
                    ovrRawScr += scsScr; // .getRankValueWeightedScore();
                    totalWeights += scs.getImportanceValueWeight();
                    //wtUsed =  scs.getImportanceValueWeight();
                    scs.setWeightUsed( scs.getImportanceValueWeight() );
                }

                else if( overallRawScoreCalcType.getIsAnyWeights() )
                {
                    // LogService.logIt( "BaseTestEventScorer.scoreSimCompetencies scs=" + competencyName + ", scs.getUserWeight()=" + scs.getUserWeight() + ", scs.getUserWeightedScaledScore( true )=" + scs.getUserWeightedScaledScore( true ) );
                    if( scs.getUserWeight()>0 || hasWeights )
                    {
                        // for Sim Competencies that have a raw score that is a z score and the distribution is such that a 0 is the "best" convert to
                        // a non-linear score value by multiplying the absolute value of the simcompetency raw score (a z score) by -1. This means 0 will be the highest score for
                        // these sim competencies.
                        if( overallRawScoreCalcType.getUsesRawCompetencyScores() &&
                            SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid()).getIsZScore() &&
                            scs.getSimCompetencyObj().getCategorydisttype()==CategoryDistType.NORMAL.getCategoryDistTypeId() )
                        {
                            scsScr = Math.abs(scs.getRawScore())*-1f*scs.getUserWeight();
                            nonlinearSimCompetencyCount++;
                        }

                        else if( overallRawScoreCalcType.getUsesRawCompetencyScores() &&
                                 !SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid()).getIsZScore() &&
                                 ( scs.getCompetencyScoreType().isTrueDifference() || scs.getCompetencyScoreType().isAvgZscoreDiff() ) )
                        {
                            scsScr = getEquivalentZScore( scs.getUnweightedScaledScore( true ), scs.getSimCompetencyObj().getScaledmean(), scs.getSimCompetencyObj().getScaledstddeviation() );
                            scsScr *= scs.getUserWeight();
                        }

                        else
                            scsScr = overallRawScoreCalcType.getUsesRawCompetencyScores() ? scs.getRawScore()*scs.getUserWeight() : scs.getUserWeightedScaledScore( true );

                        ovrRawScr += scsScr;

                        if( overallRawScoreCalcType.getIsSum() )
                            totalWeights = 1;
                            
                        else
                            totalWeights += scs.getUserWeight();
                        //wtUsed =  scs.getUserWeight();
                        scs.setWeightUsed( scs.getUserWeight() );
                    }

                    // No weights, everything gets a weight of 1
                    else
                    {

                        // for Sim Competencies that have a raw score that is a z score and the distribution is such that a 0 is the "best" convert to
                        // a non-linear score value by multiplying the absolute value of the simcompetency raw score (a z score) by -1. This means 0 will be the highest score for
                        // these sim competencies.
                        if( overallRawScoreCalcType.getUsesRawCompetencyScores() &&
                            SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid()).getIsZScore() &&
                            scs.getSimCompetencyObj().getCategorydisttype()==CategoryDistType.NORMAL.getCategoryDistTypeId() )
                        {
                            scsScr = Math.abs(scs.getRawScore())*-1f;
                            nonlinearSimCompetencyCount++;
                        }

                        else if( overallRawScoreCalcType.getUsesRawCompetencyScores() &&
                                 !SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid()).getIsZScore() &&
                                 ( scs.getCompetencyScoreType().isTrueDifference() || scs.getCompetencyScoreType().isAvgZscoreDiff() ) )
                            scsScr = getEquivalentZScore( scs.getUnweightedScaledScore( true ), scs.getSimCompetencyObj().getScaledmean(), scs.getSimCompetencyObj().getScaledstddeviation() );

                        else
                            scsScr = overallRawScoreCalcType.getUsesRawCompetencyScores() ? scs.getRawScore() : scs.getUnweightedScaledScore( true );


                        ovrRawScr += scsScr;

                        if( overallRawScoreCalcType.getIsSum() )
                            totalWeights = 1;
                            
                        else
                            totalWeights++;
                        
                        //wtUsed = 1;
                        scs.setWeightUsed( 1f );
                    }
                }

                else if( overallRawScoreCalcType.getIsSum() )
                {
                    scsScr = overallRawScoreCalcType.equals( OverallRawScoreCalcType.RAW_SCORES_SUM ) ? scs.getRawScore() : scs.getUnweightedScaledScore( true );
                    ovrRawScr += scsScr;
                }

                if( scsScr == 0 && !overallRawScoreCalcType.getUsesRawCompetencyScores() && !overallRawScoreCalcType.getIsSum() )
                    scsScorableZeroCt++;

                scrSum.append( "Competency: " + competencyName + ", Unweighted Score Used in Overall Calc: " + ( overallRawScoreCalcType.getUsesRawCompetencyScores() ? scs.getRawScore() : scs.getUnweightedScaledScore( true ) ) + " Weight: " + scs.getWeightUsed() + ", scr*wt: " + scsScr + "\n" );
            }

            else
            {
                scsNotScorableCt++;
                scrSum.append( "Competency: " + competencyName + ", NOT SCORABLE.\n" );
            }
        }
    }

    public void calculateMetaScores() throws Exception
    {
        try
        {
            collectMetaScorers();

            if( metaScorerList==null )
                return;

            for( MetaScorer ms : metaScorerList )
            {
                LogService.logIt( "BaseTestEventScorer.calculateMetaScores() Scoring " + ms.toString() );
                ms.calculate();
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseTestEventScorer.calculateMetaScores() testEventId=" + (te==null ? "null" : te.getTestEventId()) );
        }
    }


    public void collectMetaScorers() throws Exception
    {
        if( IbmInsightMetaScorer.requiresMetaScore( te, te.getAllIactnResponseList() ) )
        {
            if( metaScorerList==null )
                metaScorerList = new ArrayList<>();

            metaScorerList.add( new IbmInsightMetaScorer( te, simLocale, reportLocale, te.getAutoScorableResponseList(), te.getAllIactnResponseList() ) );
        }
    }


    public List<SimCompetencyGroup> getSimCompetencyGroupList() throws Exception
    {
            return new ArrayList<>();
    }



    public void setReportLocale() throws Exception
    {
            if( te.getReport()==null )
            {
                long reportId = 0;

                if( te.getReportId() >0 )
                    reportId = te.getReportId();
                else if( te.getProduct().getLongParam2() > 0 )
                    reportId = te.getProduct().getLongParam2();
                else if( te.getSimDescriptor().getReportId()> 0 )
                    reportId = te.getSimDescriptor().getReportId();

                Report r = reportId>0 ? eventFacade.getReport(reportId) : null;

                te.setReport(r);
            }

            if( te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
                reportLocale = I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

            else if( te.getReport()!=null && te.getReport().getLocaleStr()!= null && !te.getReport().getLocaleStr().isEmpty() )
                reportLocale =  I18nUtils.getLocaleFromCompositeStr( te.getReport().getLocaleStr() );

            else if( tk!=null && tk.getAuthUser()!=null && tk.getAuthUser().getLocaleStr()!=null && !tk.getAuthUser().getLocaleStr().isEmpty()  )
                reportLocale =  I18nUtils.getLocaleFromCompositeStr( tk.getAuthUser().getLocaleStr() );

            else if( tk.getSuborgId()>0 && tk.getSuborg()!=null && tk.getSuborg().getDefaultReportLang()!=null && !tk.getSuborg().getDefaultReportLang().isEmpty() )
                reportLocale = I18nUtils.getLocaleFromCompositeStr( tk.getSuborg().getDefaultReportLang() );

            else if( tk.getOrg()!=null && tk.getOrg().getDefaultReportLang()!=null && !tk.getOrg().getDefaultReportLang().isEmpty() )
                reportLocale = I18nUtils.getLocaleFromCompositeStr( tk.getOrg().getDefaultReportLang() );

            else
                reportLocale = Locale.US;
    }


    public Map<String,Object> getPercentile( int productId, int alternateSimDescriptorId, TestEvent te, TestEventScore tes, int orgId, String countryCode) throws Exception
    {
        Date procStart = new Date();

        if( normFacade == null )
            normFacade = NormFacade.getInstance();

        int minSimVersionIdForMajorVersion = 0;

        if( te.getSimXmlObj()!=null && te.getSimXmlObj().getMinsimveridformajorversion()>0 )
            minSimVersionIdForMajorVersion = te.getSimXmlObj().getMinsimveridformajorversion();

        Map<String,Object> o = normFacade.getPercentile(productId, ScoreUtils.getPercentileScoreTypeIdForTestEvent(te), te.getSimId(), te.getSimVersionId(), te.getTestEventId(), minSimVersionIdForMajorVersion, tes, orgId, countryCode, null, null, null);

        if( alternateSimDescriptorId>0 && !normFacade.hasValidPercentiles( o, orgId, countryCode ) && tes.getSimletCompetencyId()<=0 )
        {
            LogService.logIt( "BaseTestEventScorer.getPercentile() using alternateSimDescriptorId=" + alternateSimDescriptorId + " for percentiles." );
            if( eventFacade==null)
                eventFacade=EventFacade.getInstance();
            SimDescriptor sd2 = eventFacade.getSimDescriptor( alternateSimDescriptorId );
            if( sd2!=null && sd2.getXml()!=null && !sd2.getXml().isBlank())
            {
                SimJ simJ2 = JaxbUtils.ummarshalSimDescriptorXml( sd2.getXml() );
                minSimVersionIdForMajorVersion = simJ2.getMinsimveridformajorversion();

                Map<String,Object> o2 = normFacade.getPercentile(productId, ScoreUtils.getPercentileScoreTypeIdForTestEvent(te), sd2.getSimId(), sd2.getSimVersionId(), te.getTestEventId(), minSimVersionIdForMajorVersion, tes, orgId, countryCode, null, null, null);
                normFacade.combinePercentileValues( o, o2 );
            }
        }

        Tracker.addResponseTime( "Get Percentile", new Date().getTime() - procStart.getTime() );

        return o;
    }





    public Percentile createPercentileForScore( TestKey tk, TestEvent te, TestEventScore tes) throws Exception
    {
        try
        {
            if( te.getTestEventId()<=0 )
                throw new Exception( "TestEvent.TestEventId is 0" );

            if( tes.getTestEventScoreId()<=0 )
                LogService.logIt("BaseTestEventScorer.createPercentile() TestEventScore.TestEventScoreId is 0 " + te.toString() + ", " + tes.toString() );
                // throw new Exception( "TestEventScore.TestEventScoreId is 0" );

            if( te.getExcludeFmNorms()==1 )
                throw new Exception( "TestEvent.excludeFmNorms is set." );

            Date procStart = new Date();
            if( normFacade==null )
                normFacade = NormFacade.getInstance();

            Percentile p = normFacade.getExistingPercentileRecordForTestEvent(te, tes);

            if( p == null )
                p = new Percentile();

            if( tes.getTestEventScoreId()<= 0)
            {
                if( eventFacade == null )
                    eventFacade = EventFacade.getInstance();

                eventFacade.flushEntityManager();
            }

            if( tk!=null )
                tk.populatePercentileObj(p);

            te.populatePercentileObj(p);
            tes.populatePercentileObj(p);

            p.setPercentileScoreTypeId(ScoreUtils.getPercentileScoreTypeIdForTestEvent(te) );

            if( p.getPercentileScoreTypeId()==PercentileScoreType.WEIGHTED_AVG_ZSCORES.getPercentileScoreTypeId() )
                p.setScore2( p.getScore() );

            else
                p.setScore2( -1f );

            Tracker.addResponseTime( "Create Percentile", new Date().getTime() - procStart.getTime() );

            return p;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTestEventScorer.createPercentile() " );
            return null;
        }
    }


    public void setExcludeFmNorms() throws Exception
    {
            // If already set to exclude
            if( te.getExcludeFmNorms()==1 )
            {}

            else if( te.getOrg()!=null && te.getOrg().getExcludeFromNorms()==1 )
                te.setExcludeFmNorms(1);

            else if( te.getSuborg()!=null && te.getSuborg().getExcludeFromNorms()==1 )
                te.setExcludeFmNorms(1);

            else
            {
                user = te.getUser();

                if( user == null )
                {
                      if( userFacade == null )
                          userFacade = UserFacade.getInstance();

                      user = userFacade.getUser( te.getUserId() );
                      te.setUser(user);
                }

                if( user != null && user.getUserType().getNamed() && user.getEmail()!= null )
                {
                    for( String stub : Constants.AUTO_EXCLUDE_EMAILS )
                    {
                        if( user.getEmail().indexOf( stub ) >= 0 )
                        {
                            te.setExcludeFmNorms(1);
                            break;
                        }
                    }
                }
            }

    }




    public void recalculatePercentilesForTestEvent( TestEvent testEvent, SimDescriptor simDescriptor ) throws Exception
    {
    }


    public void applyProfileToSimXmlObj() throws Exception
    {
        ProfileUtils.applyProfileToSimXmlObj( te );
    }


    public static String getScoreTextForOverallScore( ScoreColorSchemeType scst, float ovrScr, TestEvent te ) throws Exception
    {
        try
        {
            SimJ simJ = te.getSimXmlObj();

            String t;

            if( scst.getIsSevenColor() && ovrScr >= simJ.getWhitemin() )
                t = simJ.getWhitetext();

            else if( ovrScr >= simJ.getGreenmin() )
                t = simJ.getGreentext();

            else if( scst.getIsFiveOrSevenColor() && ovrScr >= simJ.getYellowgreenmin() )
                t = simJ.getYellowgreentext();

            else if( ovrScr >= simJ.getYellowmin() )
                t = simJ.getYellowtext();

            else if( scst.getIsFiveOrSevenColor() && ovrScr >= simJ.getRedyellowmin() )
                t = simJ.getRedyellowtext();

            else if( scst.getIsThreeColor() || scst.getIsFiveColor() || (scst.getIsSevenColor() && ovrScr >= simJ.getRedmin()) )
                t = simJ.getRedtext();

            else if( scst.getIsSevenColor() )
                t = simJ.getBlacktext();

            else
                t = simJ.getRedtext();

            if( t == null )
                t = "";

            t =  UrlEncodingUtils.decodeKeepPlus( t );

            return t;
        }

        catch( Exception e )
        {
           LogService.logIt( e, "ScoreManagerBean.getScoreTextForOvrAllScore() "  + ovrScr + ", " + te.toString()  );

           throw new ScoringException( e.getMessage() + "ScoreManagerBean.getScoreTextForOvrAllScore() " ,ScoreUtils.getExceptionPermanancy(e), te );
        }
    }



    protected String packInterviewQuestions( List<InterviewQuestion> iql ) throws Exception
    {
        if( iql == null )
            return "";

        // LogService.logIt( "ScoreManagerBean.packInterviewQuestions() list size=" + iql.size() );

        StringBuilder sb = new StringBuilder();

        for( InterviewQuestion iq : iql )
        {
            if( sb.length()>0 )
                sb.append( Constants.DELIMITER );

            sb.append( iq.getQuestion() + Constants.DELIMITER +  iq.getAnchorHi() + Constants.DELIMITER + iq.getAnchorMed() + Constants.DELIMITER + iq.getAnchorLow() + Constants.DELIMITER + iq.getScoreBreadth() );
            // sb.append( XMLUtils.encodeURIComponent( iq.getQuestion() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorHi() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorMed() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorLow() )  );
        }

        //if( sb.length()>0 )
        //    LogService.logIt( "ScoreManagerBean.packInterviewQuestions() returning " + sb.toString() );

        return sb.toString();
    }


    protected String packTextBasedResponses( List<TextAndTitle> ttl ) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        String str;

        for( TextAndTitle tt : ttl )
        {
            if( sb.length()>0 )
                sb.append( Constants.DELIMITER );

            sb.append( tt.getTitle() + Constants.DELIMITER + tt.getText() + Constants.DELIMITER + tt.getFlags()  );

            str = (tt.getString1()!=null && !tt.getString1().isEmpty() ? tt.getString1() : "") + "~" + (tt.getString2()!=null && !tt.getString2().isEmpty() ? tt.getString2() : "") + "~" + (tt.getString3()!=null && !tt.getString3().isEmpty() ? tt.getString3() : "");

            sb.append( Constants.DELIMITER + str );
            // sb.append( XMLUtils.encodeURIComponent( tt.getTitle() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( tt.getText() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( tt.getFlags() ) );
        }

        return sb.toString();
    }


    protected String packInterestAndExperienceQuestions( List<IactnResp> interestLst, List<IactnResp> experLst ) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        sb.append(packResponses(interestLst, NonCompetencyItemType.INTEREST_TASK1.getTitle(), false ) );
        sb.append(packResponses(experLst, NonCompetencyItemType.EXPERIENCE_TASK1.getTitle(), false ) );

        // LogService.logIt( "ScoreManagerBean.packInterestAndExperienceQuestions() interestLst=" + interestLst.size() + ", experLst=" + experLst.size() + ", " + sb.toString() );

        return sb.toString();
    }


    protected String packGeneralNoncompetencyResponses( List<IactnResp> allIrl ) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        // PACK THE MIN QUALS

        // get the relevant item responses.
        List<IactnResp> irl = getNonCompetencyItemRespList(allIrl, NonCompetencyItemType.MIN_QUALS.getNonCompetencyItemTypeId());
        sb.append(packResponses(irl, NonCompetencyItemType.MIN_QUALS.getTitle(), false ) );

        // PACK THE APPLICANT INFO

        // get the relevant item responses.
        irl = getNonCompetencyItemRespList(allIrl, NonCompetencyItemType.APPLICANT_INFO.getNonCompetencyItemTypeId());
        sb.append(packResponses(irl, NonCompetencyItemType.APPLICANT_INFO.getTitle(), false ) );

        // PACK THE WRITING SAMPLES

        //LogService.logIt( "BaseTestEventScorer.packGeneralNoncompetencyResponses() result=" + sb.toString() );
        // get the relevant item responses.
        irl = getNonCompetencyItemRespList(allIrl, NonCompetencyItemType.WRITING_SAMPLE.getNonCompetencyItemTypeId());
        // Include scored essay inputs
        irl.addAll( getScoredEssayIntnResps( allIrl ) );
        // irl.addAll( getUnscoredFillBlankIntnResps( allIrl ) );
        sb.append(packResponses(irl, NonCompetencyItemType.WRITING_SAMPLE.getTitle(), true ) );

        // PACK THE SPEAKING SAMPLES

        // get the relevant item responses.
        irl = getNonCompetencyItemRespList(allIrl, NonCompetencyItemType.SPEAKING_SAMPLE.getNonCompetencyItemTypeId());
        // Include scored audio inputs
        // irl.addAll( getScoredAudioIntnResps( allIrl ) );
        sb.append(packResponses(irl, NonCompetencyItemType.SPEAKING_SAMPLE.getTitle(), true ) );

        // PACK THE AV UPLOADS

        // get the relevant item responses for Manual AV Uploads.
        irl = getNonCompetencyItemRespList(allIrl, NonCompetencyItemType.AV_UPLOAD.getNonCompetencyItemTypeId());
        // Include scored audio/video items inputs
        // irl.addAll( getScoredAvIntnResps( allIrl ) );
        sb.append(packResponses(irl, NonCompetencyItemType.AV_UPLOAD.getTitle(), true ) );

        // PACK THE FILE UPLOADS

        // get the relevant item responses for File Uploads.
        irl = getNonCompetencyItemRespList(allIrl, NonCompetencyItemType.FILEUPLOAD.getNonCompetencyItemTypeId());
        // LogService.logIt( "BaseTestEventScorer.packGeneralNoncompetencyResponses() AAA.1 File Upload irl.size=" + irl.size() );
        sb.append(packResponses(irl, NonCompetencyItemType.FILEUPLOAD.getTitle(), true ) );

        // LogService.logIt( "BaseTestEventScorer.packGeneralNoncompetencyResponses() AAA output=" + sb.toString() );

        // Next Risk Factors
        if( forcedRiskFactorsList != null && !forcedRiskFactorsList.isEmpty() )
        {
            List<TextAndTitle> ttl = new ArrayList<>();

            TextAndTitle tt;

            for( String rf : forcedRiskFactorsList )
            {
                if( rf == null || rf.isEmpty() )
                    continue;

                tt = new TextAndTitle( rf, "RF" );
                ttl.add( tt );
            }

            if( !ttl.isEmpty() )
                sb.append( ";;;" + Constants.STD_RISKFACTORSKEY + ";;;" + Constants.DELIMITER + packTextBasedResponses(ttl) );
        }

        sb.append( getMetaScoreContentPacked() );

        sb.append( getAdditionalTextScoreContentPacked() );

        return sb.toString();
    }

    public String getMetaScoreContentPacked() throws Exception
    {
        StringBuilder sb = new StringBuilder();

        if( metaScorerList==null )
            return "";

        String key;
        List<TextAndTitle> ttl;

        for( MetaScorer ms : metaScorerList )
        {
            key = ms.getMetaScoreContentKey();
            ttl = ms.getTextAndTitleList();

            if( key!=null && !key.isEmpty() && ttl!=null && !ttl.isEmpty() )
                sb.append( ";;;" + key + ";;;" + Constants.DELIMITER + packTextBasedResponses(ttl) );

        }

        return sb.toString();
    }


    // This is a value that can be
    public String getAdditionalTextScoreContentPacked() throws Exception
    {
        return "";
    }


    protected String packResponses( List<IactnResp> irl, String sectionName, boolean includeResponseRatingSimCompetencyIds ) throws Exception
    {
        // LogService.logIt( "BaseTestEventScorer.packResponses() AAA " + (irl==null ? "null" : irl.size()) );

        if( irl==null || irl.isEmpty() )
            return "";

        StringBuilder sb = new StringBuilder();

        List<TextAndTitle> ttl;

        String tmp;
        String simCompetencyIds;
        
        for( IactnResp ir : irl )
        {
            // LogService.logIt( "ScoreManagerBean.packResponses() AAAA ir.getSimletItemType().isText()=" + ir.getSimletItemType().isText() + " ir.getSimletItemType()=" + ir.getSimletItemType().toString() );

            // if no non-competency question type is present, this item must have a simlet competency defined and an itemtype assigned that supports manual scoring via report.
            if( ir.intnObj.getNoncompetencyquestiontypeid()<= 0)
            {
                // be sure that a simlet competency is is set and that the itemtype supports manual scoring via report
                if( ir.getIntnObj().getCompetencyscoreid()<=0 || !ir.getSimletItemType().supportsManualScoringViaReport() )
                    continue;
            }

            // LogService.logIt( "BaseTestEventScorer.packResponses() Getting ttl for " + ir.intnObj.getName() );

            // this method returns a list of TTLs for each title/text pair to include in report for this item.
            ttl = ir.getTextAndTitleList();

            if( includeResponseRatingSimCompetencyIds && ttl!=null && !ttl.isEmpty() )
            {
                simCompetencyIds = getResponseRatingSimCompetencyIds( ir );
                if( simCompetencyIds!=null && !simCompetencyIds.isBlank() )
                {
                    for( TextAndTitle tt : ttl )
                        tt.setString3( simCompetencyIds );
                }
            }
            
            // LogService.logIt( "BaseTestEventScorer.packResponses() ttl list==" + ttl.size() );

            tmp = packTextBasedResponses( ttl );

            if( tmp != null && !tmp.isEmpty() )
            {
                if( sb.length() > 0 )
                    sb.append( Constants.DELIMITER );

                sb.append( tmp );
            }
        }

        // LogService.logIt( "BaseTestEventScorer.packResponses() " + ";;;" + title+ ";;;" + Constants.DELIMITER + sb.toString() );

        if( sb.length() > 0 )
            return ";;;" + sectionName+ ";;;" + Constants.DELIMITER + sb.toString();

        return "";
    }

    public String getResponseRatingSimCompetencyIds( IactnResp ir )
    {
        String uid = ir.getSimletNodeUniqueId();
        if( uid==null || uid.isBlank() )
            return null;      
        return getResponseRatingSimCompetencyIds( uid );
    }
    
    
    public String getResponseRatingSimCompetencyIds( String uid )
    {
        if( uid==null || uid.isBlank() )
            return null;      
                
        // uid += "," + ir.getSimletSubnodeSeq();

        StringBuilder out = new StringBuilder();
        String scids;
        for( SimJ.Simcompetency sc : te.getSimXmlObj().getSimcompetency() )
        {
            if( !SimCompetencyClass.getValue( sc.getClassid()).getIsCombo() )
                continue;
            
            if( sc.getCombinationtype()!=SimCompetencyCombinationType.ITEM_LEVEL_AVERAGE_RATINGS.getSimCompetencyCombinationTypeId() )
                continue;
            
            scids = sc.getCombinationitemuniqueids();
            
            if( scids==null || sc.getCombinationitemuniqueids().isBlank() || !scids.contains(uid) )
                continue;
            
            if( !out.isEmpty() )
                out.append(",");
            
            out.append(Long.toString(sc.getId()) );
        }
        return out.toString();
    }
    
    /*
    public List<IactnResp> getUnscoredFillBlankIntnResps( List<IactnResp> irl )
    {
        List<IactnResp> out = new ArrayList<>();
        if( te.getSimCompetencyScoreList()==null || irl==null || irl.isEmpty() )
            return out;

        SimCompetencyScore simCompetencyScore;

        for( IactnResp ir : irl )
        {
            if( ir.getIactnItemRespLst()==null )
                continue;

            // TO DO. Need to find FillBlank response
            for( IactnItemResp iir : ir.getIactnItemRespLst() )
            {
                // must be a text box.
                if( !iir.getG2ChoiceFormatType().getIsTextBox() )
                    continue;

                // must have a sim competency.
                if( iir.getIntnItemObj().getSimcompetencyid()<=0 )
                    continue;

                // no competency score.
                if( iir.getIntnItemObj().getCompetencyscoreid()<=0 )
                    continue;

                simCompetencyScore=null;
                for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
                {
                    if( scs.getSimCompetencyObj().getId()==iir.getIntnItemObj().getSimcompetencyid() )
                    {
                        simCompetencyScore=scs;
                        break;
                    }
                }
                if( simCompetencyScore==null )
                    continue;

                // essays handles somewhere else.
                if( simCompetencyScore.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDESSAY) )
                    continue;

                // not ignored in scoring.
                if( simCompetencyScore.getSimCompetencyObj().getHide()<=0 || simCompetencyScore.getSimCompetencyObj().getHide()==2 )
                    continue;

                // must be set to inlucde response
                if( simCompetencyScore.getSimCompetencyObj().getIncludeitemscorestype()!=3 && simCompetencyScore.getSimCompetencyObj().getIncludeitemscorestype()!=5 )
                    continue;

                out.add( ir );
            }


        }
        return out;
    }
    */


    public List<IactnResp> getScoredEssayIntnResps( List<IactnResp> irl )
    {
        List<IactnResp> out = new ArrayList<>();

        for( IactnResp ir : irl )
        {
            //if( nonCompetencyItemTypeId == NonCompetencyItemType.WRITING_SAMPLE.getNonCompetencyItemTypeId() )
            //    LogService.logIt( "BaseTestEventScorer.getNonCompetencyItemRespList() InterationResponse=" + ir.getIntnObj().getSeq() + ", noncompetencyitemtypeId=" + ir.getNonCompetencyQuestionTypeId() );
            if( ir.getSimletItemType().equals( SimletItemType.AUTO_ESSAY ) )
                out.add( ir );

        }
        return out;
    }

    public List<IactnResp> getScoredAudioIntnResps( List<IactnResp> irl )
    {
        List<IactnResp> out = new ArrayList<>();

        for( IactnResp ir : irl )
        {
            //if( nonCompetencyItemTypeId == NonCompetencyItemType.WRITING_SAMPLE.getNonCompetencyItemTypeId() )
            //    LogService.logIt( "BaseTestEventScorer.getNonCompetencyItemRespList() InterationResponse=" + ir.getIntnObj().getSeq() + ", noncompetencyitemtypeId=" + ir.getNonCompetencyQuestionTypeId() );
            if( ir.getSimletItemType().equals( SimletItemType.AUTO_AUDIO ) )
                out.add( ir );

        }
        return out;
    }

    public List<IactnResp> getScoredAvIntnResps( List<IactnResp> irl )
    {
        List<IactnResp> out = new ArrayList<>();

        for( IactnResp ir : irl )
        {
            //if( nonCompetencyItemTypeId == NonCompetencyItemType.WRITING_SAMPLE.getNonCompetencyItemTypeId() )
            //    LogService.logIt( "BaseTestEventScorer.getNonCompetencyItemRespList() InterationResponse=" + ir.getIntnObj().getSeq() + ", noncompetencyitemtypeId=" + ir.getNonCompetencyQuestionTypeId() );
            if( ir.getSimletItemType().equals(SimletItemType.AUTO_AV_UPLOAD ) )
                out.add( ir );
        }
        return out;
    }




    public List<IactnResp> getNonCompetencyItemRespList( List<IactnResp> irl, int nonCompetencyItemTypeId)
    {
       // LogService.logIt( "BaseTestEventScorer.getNonCompetencyItemRespList() seeking typeId=" + nonCompetencyItemTypeId + ", IactRespList.size=" + irl.size() );

        List<IactnResp> out = new ArrayList<>();

        for( IactnResp ir : irl )
        {
            //if( nonCompetencyItemTypeId == NonCompetencyItemType.FILEUPLOAD.getNonCompetencyItemTypeId() )
            // LogService.logIt( "BaseTestEventScorer.getNonCompetencyItemRespList() Intn=" + ir.getIntnObj().getUniqueid() + ", noncompetencyitemtypeId=" + ir.getNonCompetencyQuestionTypeId() );
            if( ir.getNonCompetencyQuestionTypeId()==nonCompetencyItemTypeId )
            {
                out.add( ir );
            }
        }

        return out;
    }

    public int getInitValAsInt( int id, String initVals )
    {
        float fv = getInitValAsFloat(  id,  initVals );
        return (int) fv;
    }


    public float getInitValAsFloat( int id, String initVals )
    {
        String iv = getInitValAsString(  id,  initVals );
        if( iv==null || iv.isBlank() )
            return 0;
        try
        {
            return Float.parseFloat(iv);
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( "BaseTestEventScorer.getInitValAsFloat() unable to parse iv=" + iv + ", initValStr=" + initVals );
            return 0;
        }
    }



    public String getInitValAsString( int id, String initVals )
    {
        try
        {
            if( initVals == null || initVals.isEmpty() )
                return null;

            String[] vals = initVals.split( "," );
            String n,v;
            int idx;

            for( int i=0;i<vals.length-1;i+=2 )
            {
                n = vals[i];
                v = vals[i+1];

                idx = Integer.parseInt( n );

                if( idx == id )
                   return v.trim();
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseTestEventScorer.getInitValAsString() id=" + id + ", String=" + initVals );
        }

        return null;
    }


    public TestEventScore getMatchingExistingTestEventScore( TestEventScore newTes)
    {
        if( ( oldTestEventScoreList==null || oldTestEventScoreList.isEmpty() ) && te!=null && te.getTestEventScoreList()!=null )
        {
            oldTestEventScoreList = new ArrayList<>();
            oldTestEventScoreList.addAll( te.getTestEventScoreList() );
            for( TestEventScore tes : oldTestEventScoreList )
                tes.setTempBoolean1(false);
        }

        if( oldTestEventScoreList==null || oldTestEventScoreList.isEmpty() || newTes.getName()==null || newTes.getName().isEmpty() )
            return null;

        for( TestEventScore tes : oldTestEventScoreList )
        {
            // different type
            if( tes.getTestEventScoreTypeId()!=newTes.getTestEventScoreTypeId() )
                continue;

            // different class for competency type.
            if( tes.getTestEventScoreType().getIsCompetency() &&  tes.getSimCompetencyClassId()!=newTes.getSimCompetencyClassId() )
                continue;

            // Overall may not match because name may have changed.
            if(  tes.getTestEventScoreTypeId()==TestEventScoreType.OVERALL.getTestEventScoreTypeId() ||
                 tes.getName()!=null && tes.getName().equals( newTes.getName() ) )
            {
                // indicate found (and used)
                tes.setTempBoolean1(true);
                return tes;
            }

            if( newTes.getNameEnglish()!=null && !newTes.getNameEnglish().isEmpty() && tes.getNameEnglish()!=null && tes.getNameEnglish().equals( newTes.getNameEnglish() ) )
            {
                // indicate found (and used)
                tes.setTempBoolean1(true);
                return tes;
            }
        }

        return null;
    }


    public void deleteOldTestEventScores()
    {
        try
        {
            if( oldTestEventScoreList==null || oldTestEventScoreList.isEmpty()  )
                return;


            for( TestEventScore tes : oldTestEventScoreList )
            {
                if(  tes.getTestEventScoreType().getIsOverall() || tes.getTestEventScoreType().getIsReport() || tes.getTestEventScoreType().getIsLevelScores() )
                    continue;

                // re-used.
                if( tes.isTempBoolean1() )
                    continue;

                if( eventFacade == null )
                    eventFacade = EventFacade.getInstance();

                eventFacade.deleteEntity( tes );
            }

            oldTestEventScoreList=null;

        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseTestEventScorer.deleteOldTestEventScores() te=" + te.getTestEventId() );
        }
    }

    public ItemResponse getMatchingExistingItemResponse( ItemResponse newIr )
    {
        if( oldItemResponseList==null || oldItemResponseList.isEmpty() || newIr.getIdentifier()==null || newIr.getIdentifier().isEmpty() )
            return null;

        for( ItemResponse ir : oldItemResponseList )
        {
            if( ir.getIdentifier()!=null && ir.getIdentifier().equals( newIr.getIdentifier() ) )
                return ir;
        }

        // identifier may have changed.
        for( ItemResponse ir : oldItemResponseList )
        {
            if( ir.getIdentifier()!=null && ir.getIdentifier().equals( newIr.getIdentifier() ) )
                return ir;
        }


        return null;
    }


    public void finalizeScore() throws Exception
    {
        try
        {
            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();

            te.setOverallScore( ovrScaledScr );

            te.setScoreFormatTypeId( scoreFormatTypeId );
            if( scoreColorSchemeType!=null )
                te.setScoreColorSchemeTypeId(scoreColorSchemeType.getScoreColorSchemeTypeId());

            // This is the reportId to be used for presentation. May be changed on rescores/rereports, so remove here.
            te.setReportId( 0 );
            te.setTestEventStatusTypeId( TestEventStatusType.SCORED.getTestEventStatusTypeId() );

            eventFacade.saveTestEvent(te);

            eventFacade.reorderItemResponses( te.getTestEventId(), 0 );

            deleteOldTestEventScores();

            // Set CEFR Score
            if( te.getSimXmlObj().getCefr()==1 )
            {
                 List<TestEventScore> tesl = eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true );
                 te.setTestEventScoreList(tesl);

                 Object[] data = CefrUtils.getCefrScoreInfoForOverall(te, tesl);
                 CefrScoreType st = (CefrScoreType) data[0];
                 String stub = (String) data[1];
                 if( !st.equals( CefrScoreType.UNKNOWN ))
                 {
                     TestEventScore otes = te.getOverallTestEventScore();
                     String textParam1 = otes.getTextParam1();
                     if( textParam1==null )
                         textParam1="";
                     textParam1 += "[" + Constants.CEFRLEVEL + "]" + st.getTextVal();
                     textParam1 += "[" + Constants.CEFRLEVELTEXT + "]" + CefrUtils.getCefrScoreDescription( simLocale, st, stub );
                     otes.setTextParam1(textParam1);
                     
                    String scrTxt = CefrUtils.getGeneralOverallScoreTextForCefrScore(te.getSimXmlObj(), st, scoreColorSchemeType);
                    String scrFmt = I18nUtils.getFormattedNumber( Locale.US , st.getNumericEquivalentScore(scoreColorSchemeType), 1 );
                    scrTxt  = performStandardSubstitutions( scrTxt, scrFmt );
                    otes.setScoreText( scrTxt );                     
                    eventFacade.saveTestEventScore(otes);
                     
                 }
            }
        }
        catch( ScoringException e )
        {
            if( te!=null && te.getTestEventArchiveId()<=0 && e.toString().contains("Unarchived TestEvent record does not exist") )
            {
                if( eventFacade==null )
                    eventFacade=EventFacade.getInstance();
                TestEventArchive tex = eventFacade.getTestEventArchiveForTestEventId( te.getTestEventId() );
                if( tex!=null )
                {
                    LogService.logIt( "BaseTestEventScorer.finalizeScore() " + e.toString() + ", FOUND TestEventArchive. Adjusting and saving." );
                    te.setTestEventArchiveId( tex.getTestEventArchiveId() );
                    eventFacade.saveTestEvent(te);
                }
                else
                    LogService.logIt( "BaseTestEventScorer.finalizeScore() " + e.toString() + ", TestEventArchive NOT FOUND." );
            }
            else
            {
                LogService.logIt( "BaseTestEventScorer.finalizeScore() " + e.toString() + ", Is not an unarchived issue. testEventId=" + te.getTestEventId() );
                throw e;
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseTestEventScorer.finalizeScore() " );
            throw e;
        }
    }


    public void finalizeRemoteProctorScoring() throws Exception
    {
        if( tk.getOnlineProctoringType().equals( OnlineProctoringType.NONE))
            return;

        try
        {
            ProctorUtils proctorUtils = null;
            if( te.getRemoteProctorEvent()==null )
            {
                proctorUtils = new ProctorUtils();
                proctorUtils.setupRemoteProctorEvent( simLocale, te.getUser().getTimeZone(), te );
            }

            if( te.getRemoteProctorEvent()==null )
            {
                LogService.logIt( "BaseTestEventScorer.finalizeRemoteProctorScoring() No RemoteProctorEvent found for testEventId=" + te.getTestEventId() );
                return;
            }

            setSameIpTestEvents( proctorUtils );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseTestEventScorer.finalizeRemoteProctorScoring() " );
        }
    }


    private void setSameIpTestEvents( ProctorUtils proctorUtils ) throws Exception
    {
        // LogService.logIt( "BaseTestEventScorer.setSameIpTestEvents() AAA testEventId=" + te.getTestEventId() );
        if( tk.getOnlineProctoringType().equals( OnlineProctoringType.NONE) )
            return;

        try
        {
            RemoteProctorEvent rpe = te.getRemoteProctorEvent();

            if( proctorUtils == null )
                proctorUtils = new ProctorUtils();


            if( rpe==null || rpe.getSuspiciousActivityList()==null )
            {
                proctorUtils.setupRemoteProctorEvent( simLocale, te.getUser().getTimeZone(), te );
                rpe = te.getRemoteProctorEvent();
            }

            if( rpe==null )
                return;

            ProctorFacade proctorFacade = null;

            // Check same Ip
            if( rpe.getSameIpTestEventInfo()==null || rpe.getSameIpTestEventInfo().isBlank() )
            {
                List<Object[]> data = proctorUtils.getSuspiciousTestEventInfoBasedOnIpAddress(te);

                // No events found.
                if( data==null || data.isEmpty() )
                    return;

                // store data.
                rpe.setSameIpTestEventInfo( ProctorHelpUtils.packSameIpTestEventInfo(data));

                if( proctorFacade==null )
                    proctorFacade = ProctorFacade.getInstance();
                proctorFacade.saveRemoteProctorEvent( rpe );
            }


            // LogService.logIt( "BaseTestEventScorer.setSameIpTestEvents() BBB testEventId=" + te.getTestEventId() + ", suspiciousActivityList.size=" + (rpe.getSuspiciousActivityList()==null ? "null" : rpe.getSuspiciousActivityList().size()) );

            if( rpe.getSameIpTestEventInfo()==null || rpe.getSameIpTestEventInfo().isBlank() )
                return;

            // Suspicious activity is created only for Premium.
            //if( !tk.getOnlineProctoringType().getIsAnyPremium() )
            //    return;

            SuspiciousActivity sameIpSa = null;
            for( SuspiciousActivity sa : rpe.getSuspiciousActivityList() )
            {
                if( sa.getSuspiciousActivityType().equals( SuspiciousActivityType.SAME_IP_TEST_EVENTS) )
                {
                    sameIpSa = sa;
                    break;
                }
            }

            // already have it.
            if( sameIpSa!=null )
                return;

            sameIpSa = new SuspiciousActivity();
            sameIpSa.setTestEventId( rpe.getTestEventId());
            sameIpSa.setTestKeyId( rpe.getTestKeyId() );
            sameIpSa.setCreateDate( new Date() );
            sameIpSa.setLastUpdate( new Date() );
            sameIpSa.setSuspiciousActivityTypeId( SuspiciousActivityType.SAME_IP_TEST_EVENTS.getSuspiciousActivityTypeId() );
            sameIpSa.setIntParam1(1);
                if( proctorFacade==null )
                    proctorFacade = ProctorFacade.getInstance();
            proctorFacade.saveSuspiciousActivity( sameIpSa );
            rpe.getSuspiciousActivityList().add( sameIpSa );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseTestEventScorer.setSameIpTestEvents() testEventId=" + te.getTestEventId() );
        }
    }


    public int setItemResponses(List<ScorableResponse> scoreableResponseList) throws Exception
    {
        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "BaseTestEventScorer.setItemResponses START scoreableResponseList.size=" + (scoreableResponseList==null ? "null" : scoreableResponseList.size())   );
        ItemResponse itemResponse;
        ItemResponse ir2;

        // IactnResp ir;
        int count = 0;

        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();


        // now create the Item Response records for item analysis down the road.
        for( ScorableResponse sr : scoreableResponseList ) //te.getAutoScorableResponseList() )
        {
            if( !sr.saveAsItemResponse() )
            {
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "BaseTestEventScorer AAA.0 NOT Saving Item Response because set to NOT save as item response. sr=" + sr.toString() + ", sr.hasValidScore()=" + sr.hasValidScore() + ", isAutoScorable()=" + sr.isAutoScorable() + ", getAllScorableIntItemResponses().isEmpty()=" + sr.getAllScorableIntItemResponses().isEmpty()   );
                continue;
            }

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "BaseTestEventScorer AAA Saving Item Responses. sr=" + sr.toString() + " isAutoScorable()=" + sr.isAutoScorable( ) + ", sr.getAllScorableIntItemResponses().size()=" + sr.getAllScorableIntItemResponses().size() + ", count=" + count   );

            if( sr.isAutoScorable( ) )
            {
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "BaseTestEventScorer BBB Saving AutoScorable Responses. " + sr.toString() + ", count=" + count );
                itemResponse = new ItemResponse( sr );

                count++;
                itemResponse.setDisplayOrder( count );
                itemResponse.setTestEventId( te.getTestEventId());

                sr.populateItemResponse(itemResponse);

                itemResponse.setSimId( te.getSimId() );
                itemResponse.setSimVersionId( te.getSimVersionId() );

                ir2 = getMatchingExistingItemResponse( itemResponse );
                if( ir2!=null && ir2.getItemResponseId()>0 )
                    itemResponse.setItemResponseId( ir2.getItemResponseId() );

                eventFacade.saveItemResponse(itemResponse);
            }
        }

        return count;
    }


    public int setCompetencyGroupTestEventScores( int counter ) throws Exception
    {
        TestEventScore tes2;

        // Overall
        TestEventScore tes;

        String tp = "";

        // for overall
        Map<String,Object> norm;

        float percentile;
        int pcount;
        Percentile pctObj;
        // tesl.add(tes);

        String cc = te.getIpCountry();

        if( frcCountry != null && !frcCountry.isEmpty() )
            cc = frcCountry;

        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();

        List<SimCompetencyGroup> scgl = getSimCompetencyGroupList(); //   new ArrayList<>();

        for( SimCompetencyGroup scgg : scgl )
        {
            if( scgg != null && !scgg.getSimCompetencyScoreList().isEmpty() )
            {
                tes = new TestEventScore();

                tes.setTestEvent(te);
                tes.setTestEventId( te.getTestEventId() );
                tes.setTestEventScoreTypeId( TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() );
                tes.setDisplayOrder( ++counter );
                tes.setName( scgg.getName() );
                tes.setNameEnglish( scgg.getName() );
                tes.setScoreFormatTypeId( scoreFormatTypeId );
                tes.setIntParam1( scgg.getSimCompetencyGroupTypeId() );
                tes.setScore( scgg.calculateGroupScore() );
                tes.setRawScore( tes.getScore() );

                tes2 = getMatchingExistingTestEventScore(tes);
                if( tes2!=null && tes2.getTestEventScoreId()>0 )
                    tes.setTestEventScoreId( tes2.getTestEventScoreId() );

                eventFacade.saveTestEventScore(tes);

                if( te.getProduct()==null )
                    te.setProduct( eventFacade.getProduct( te.getProductId() ));

                if( tes.getScore()>=0 && te.getProduct().getIntParam7()==1 )
                {
                    norm = getPercentile(te.getProductId(), te.getProduct().getIntParam24(), te, tes, te.getOrgId(), cc );

                    percentile = norm==null ? -1 : ((Float)norm.get("percentile")).floatValue();
                    pcount = norm==null ? 0 : ((Integer)norm.get("count")).intValue();

                    tes.setPercentile( percentile );
                    tes.setOverallPercentileCount( pcount );

                    if(percentile<0 )
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
                        //    norm.put( "percentile" , (float) (-1) );
                        //    norm.put( "count" , (int)(0)) ;
                        //}

                        percentile = norm==null ? -1 : ((Float)norm.get("percentilecc")).floatValue();
                        pcount = norm==null ? 0 : ((Integer)norm.get("countcc")).intValue();

                        tes.setCountryPercentile( percentile );
                        tes.setCountryPercentileCount( pcount );
                    }
                    eventFacade.saveTestEventScore(tes);

                    // ALWAYS DO THIS AFTER SAVING TestEventScore
                    if( te.getExcludeFmNorms()==0 )
                    {
                        pctObj = createPercentileForScore(tk, te, tes );

                        if( pctObj != null )
                            eventFacade.savePercentile( pctObj );
                    }

               }

            }
        }

        return counter;
    }


    public int setCompetencyTestEventScores( int counter ) throws Exception
    {
        TestEventScore tes2;

        // Overall
        TestEventScore tes;

        String tp = "";

        // for overall
        Map<String,Object> norm;

        float percentile;
        int pcount;
        Percentile pctObj;
        // tesl.add(tes);

        String cc = te.getIpCountry();

        if( frcCountry != null && !frcCountry.isEmpty() )
            cc = frcCountry;

        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();

        SimCompetencySortType scst = SimCompetencySortType.getValue( this.te.getSimXmlObj().getSimcompetencyreportingsorttype() );

        // Next, sort the score objects in descending weight, name order
        switch (scst) {
            case DISPLAYORDER -> Collections.sort( te.getSimCompetencyScoreList(), new DisplayOrderComparator() );
            case RANK -> Collections.sort( te.getSimCompetencyScoreList(), new UserRankComparator() );
            case WEIGHT -> Collections.sort( te.getSimCompetencyScoreList(), new WeightedObjectComparator(hasWeights, !hasWeights) );
            default -> Collections.sort( te.getSimCompetencyScoreList() );
        }

        SimletCompetencyScore smltcs;
        String scrTxt;
        String scrFmt;

        boolean computePercentiles = !reportRules.getReportRuleAsBoolean("percentilesoff");

        for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
        {
            scs.setBoolean1(false);
        }

        List<TextAndTitle> ttl;

        // Do competencies first
        for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
        {
            // LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() scs=" + scs.getName() + ", hasAnyScoreData=" + scs.hasAnyScoreData() + ", scs.getHasScoreableData()=" + scs.getHasScoreableData() + ", scs.getTotalScorableItems()=" + scs.getTotalScorableItems() );

            if( !scs.hasAnyScoreData() )
                continue;

            // Skip Competencies that have no items.
            if( te.getSimXmlObj().getInclcompswnoitmsinscrdata()==0 && scs.getSimCompetencyObj().getScoreifnoresponses()==0 && scs.getTotalScorableItems()==0 && !scs.getHasUnscoredItems() )
            {
                LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() Skipping Create TES for scs=" + scs.getName() + ", hasAnyScoreData=" + scs.hasAnyScoreData() + ", scs.getHasScoreableData()=" + scs.getHasScoreableData() + ", scs.getTotalScorableItems()=" + scs.getTotalScorableItems() + ", scs.getHasUnscoredItems()=" + scs.getHasUnscoredItems() + ", scs.getSimCompetencyObj().getScoreifnoresponses()=" + scs.getSimCompetencyObj().getScoreifnoresponses() );
                continue;
            }

            // LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() BBB scs.getSimCompetencyObj().getId()=" + scs.getSimCompetencyObj().getId() );

            tes = new TestEventScore();

            tes.setTestEvent(te);
            tes.setTestEventId( te.getTestEventId() );
            tes.setTestEventScoreTypeId( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );
            tes.setDisplayOrder( ++counter );
            tes.setName( scs.getName() );
            tes.setNameEnglish( scs.getNameEnglish() );
            tes.setScoreFormatTypeId( scoreFormatTypeId );
            tes.setSimCompetencyClassId( scs.getSimCompetencyObj().getClassid() );

            tes2 = getMatchingExistingTestEventScore(tes);
            if( tes2!=null && tes2.getTestEventScoreId()>0 )
                tes.setTestEventScoreId( tes2.getTestEventScoreId() );

            if( scs.getSimCompetencyClass().getSupportsSubclass() && scs.getSimCompetencyObj().getSubclassid()>=0 )
            {
                tes.setIntParam1( scs.getSimCompetencyClass().getSimCompetencyClassId() );
                tes.setSimCompetencyClassId( scs.getSimCompetencyObj().getSubclassid() );
            }

            else
                tes.setSimCompetencyClassId( scs.getSimCompetencyClass().getSimCompetencyClassId() );

            tes.setSimCompetencyId( scs.getSimCompetencyObj().getId() );

            tes.setHide( scs.getSimCompetencyObj().getHide() );
            tes.setIntParam2( scs.getSimCompetencyObj().getIncludeitemscorestype() );

            smltcs = scs.getSingleSimletCompetencyScore();
            if( smltcs != null )
            {
                tes.setSimletId( smltcs.simletScore.simletObj.getId() );
                tes.setSimletVersionId( smltcs.simletScore.simletObj.getVersion() );
                tes.setSimletCompetencyId( smltcs.competencyScoreObj.getId() );
            }

            if( scs.getHasScoreableData() && !scs.getSimCompetencyClass().isUnscored() )
            {
                tes.setScore( scs.getUnweightedScaledScore( false ) );
                tes.setRawScore( scs.getRawScore() );
                tes.setScoreCategoryId( scs.getScoreCategoryTypeId( scoreColorSchemeType ) );
                tes.setReportFileContentTypeId( scs.getSimCompetencyObj().getPresentationtype() );

                scrTxt = scs.getScoreText( scoreColorSchemeType );
                scrFmt = I18nUtils.getFormattedNumber( Locale.US , tes.getScore(), 0 );
                scrTxt  = performStandardSubstitutions( scrTxt, scrFmt );
                tes.setScoreText( scrTxt );
                tes.setWeight( scs.getWeightUsed() );

                // tes.setIntParam2( scs.getSimCompetencyObj().getIncludeitemscorestype() );

                tes.setFractionUsed( scs.getFractionScoreValue() );
                tes.setTotalUsed( scs.getTotalScoreValue() );
                tes.setScorableItemResponses( (int) scs.getTotalScorableItems() );

                tes.setMean( scs.getSimCompetencyObj().getMean() );
                tes.setStdDeviation( scs.getSimCompetencyObj().getStddeviation() );

                tes.setScore2( scs.getMetaScore( 2 ));
                tes.setScore3( scs.getMetaScore( 3 ));
                tes.setScore4( scs.getMetaScore( 4 ));
                tes.setScore5( scs.getMetaScore( 5 ));
                tes.setScore6( scs.getMetaScore( 6 ));
                tes.setScore7( scs.getMetaScore( 7 ));
                tes.setScore8( scs.getMetaScore( 8 ));
                tes.setScore9( scs.getMetaScore( 9 ));
                tes.setScore10( scs.getMetaScore( 10 ));
                tes.setScore11( scs.getMetaScore( 11 ));
                tes.setScore12( scs.getMetaScore( 12 ));


                if( scs.getCompetencyScoreType() != null )
                {
                    tes.setScoreTypeIdUsed( scs.getCompetencyScoreType().getCompetencyScoreTypeId() );
                    tes.setMaxValueUsed( scs.getCompetencyScoreType().isDichotomous() ? scs.getTotalScorableItems() : scs.getTotalMaxPoints() );
                }


                // These values added for analysis
                tes.setTotalCorrect( scs.getTotalCorrect() );
                tes.setTotalPoints( scs.getTotalPoints() );
                tes.setMaxTotalCorrect( scs.getTotalScorableItems() );
                tes.setMaxTotalPoints( scs.getTotalMaxPoints() );
                tes.setTotalScorableItems( scs.getTotalScorableItems() );

                tes.setInterviewQuestions( packInterviewQuestions( scs.getInterviewQuestionList() ));
            }
            
            else if( scs.getSimCompetencyClass().getIsCombo() && scs.getSimCompetencyObj().getCombinationtype()==SimCompetencyCombinationType.ITEM_LEVEL_AVERAGE_RATINGS.getSimCompetencyCombinationTypeId() )
            {
                tes.setReportFileContentTypeId( scs.getSimCompetencyObj().getPresentationtype() );

                scrTxt = scs.getScoreText( scoreColorSchemeType );
                scrFmt = I18nUtils.getFormattedNumber( Locale.US , 0, 0 );
                scrTxt  = performStandardSubstitutions( scrTxt, scrFmt );
                tes.setScoreText( scrTxt );
                tes.setWeight( scs.getWeightUsed() );

                // tes.setIntParam2( scs.getSimCompetencyObj().getIncludeitemscorestype() );

                String itemIdStr = scs.getSimCompetencyObj().getCombinationitemuniqueids();
                int totalItems= itemIdStr==null || itemIdStr.isBlank() ? 0 : (itemIdStr.split(",").length)/2; 

                tes.setScorableItemResponses( totalItems );

                tes.setMean( scs.getSimCompetencyObj().getMean() );
                tes.setStdDeviation( scs.getSimCompetencyObj().getStddeviation() );                
            }

            else
            {
                // tes.setHide( 1 );
                tes.setScore( -1 );
                tes.setRawScore( 0 );
                tes.setInterviewQuestions(null);
            }

            ttl = scs.getTextBasedResponseList();
            
            // insert combo simcompetencyids that match the item for any text and title
            if( !ttl.isEmpty() && (scs.getSimCompetencyClass().isScoredAvUpload() || scs.getSimCompetencyClass().isScoredEssay()) )
            {
                String simCompetencyIds;
                for( TextAndTitle tt : ttl )
                {
                    simCompetencyIds=getResponseRatingSimCompetencyIds(tt.getString2());
                    
                    if( simCompetencyIds!=null && !simCompetencyIds.isBlank() )
                        tt.setString3(simCompetencyIds);
                }
            }

            tes.setTextbasedResponses( packTextBasedResponses( ttl ) );

            tes.setReportAndScoringFlags( simLocale );

            tp = "";

            // save the competency description if present
            if( scs.getSimCompetencyObj() != null &&  scs.getSimCompetencyObj().getDescrip() != null )
                tp += "[" + Constants.DESCRIPTIONKEY + "]" + UrlEncodingUtils.decodeKeepPlus( scs.getSimCompetencyObj().getDescrip(), "UTF8" );
            //    tes.setTextParam1(  );

            CefrType cefrType = null;
            CefrScoreType cefrScoreType = null;
            if( scs.getSimCompetencyObj()!= null &&  scs.getSimCompetencyObj().getCefr()>0 )
            {
                cefrType = CefrType.getValue(scs.getSimCompetencyObj().getCefr());
                cefrScoreType = CefrUtils.getCefrScoreTypeForSimCompetency(te, scs);
                LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() BBB.5 cefrType=" + (cefrType==null ? "null" : cefrType.getName()) + ", cefrScoreType=" + (cefrScoreType==null ? "null" : cefrScoreType.getName()) ); 
            
                // Overwrite the score text in this situation.
                if( cefrType!=null && cefrScoreType!=null && cefrScoreType.getCefrScoreTypeId()>0 )
                {
                    scrTxt =  CefrUtils.getGeneralSimCompetencyScoreTextForCefrScore(scs.getSimCompetencyObj(), cefrScoreType, scoreColorSchemeType); // scs.getScoreText( scoreColorSchemeType );
                    scrFmt = I18nUtils.getFormattedNumber( Locale.US , cefrScoreType.getNumericEquivalentScore(scoreColorSchemeType), 0 );
                    scrTxt  = performStandardSubstitutions( scrTxt, scrFmt );
                    tes.setScoreText( scrTxt );
                }
            }

            String cvs = "";
            for( String cv : scs.getCaveatList() )
            {
                if( cv==null )
                    continue;

                if( cv.isBlank() )
                    continue;

                if( cefrType!= null && cefrScoreType!=null && !cefrScoreType.equals( CefrScoreType.UNKNOWN) )
                {
                    if( !CefrUtils.showTopicCaveatForCefrScore( cv, cefrScoreType, simLocale ))
                        continue;
                }


                if( !cvs.isBlank() )
                    cvs += Constants.DELIMITER;

                cvs += cv; // URLDecoder.decode( cv, "UTF8" );
            }

            if( !cvs.isBlank() )
                tp += "[" + Constants.CAVEATSKEY + "]" + cvs;

            String categInfo = scs.getScoreCategoryInfoString( scoreColorSchemeType );

            if( !categInfo.isEmpty() )
                tp += "[" + Constants.CATEGORYINFOKEY + "]" + categInfo;

            List<TextAndTitle> itemScoreTtl = new ArrayList<>();
            SimCompetencyScore commentParentScs = getCommentSimCompetencyScore( scs, true );
            SimCompetencyScore commentScs = getCommentSimCompetencyScore( scs, false );

            // LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() CCC.3 scs.name=" + scs.getNameEnglish() + ", commentParent=" + (commentParentScs==null ? "null" : commentParentScs.getNameEnglish()) + ", commentScs=" + (commentScs==null ? "null" : commentScs.getNameEnglish())  );
            
            // Only collectd response info if this sim competency does not have a comment parent. If it does, the parent will collect.
            if( commentParentScs==null )
            {
                if( scs.getItemScoreTextAndTitleList() !=null && !scs.getItemScoreTextAndTitleList().isEmpty() )
                    itemScoreTtl.addAll( scs.getItemScoreTextAndTitleList() );

                // this allows unscored survey responses to be presented using existing logic.
                else if( itemScoreTtl.isEmpty() && scs.getSimCompetencyClass().isUnscored() && scs.getHasUnscoredItems() && scs.getTextBasedResponseList()!=null )
                    itemScoreTtl.addAll( scs.getTextBasedResponseList() );
            }

            // merge in comments if present.
            if( commentScs!=null )
            {
                List<TextAndTitle> commentItemScoreTtl = new ArrayList<>();
                if( commentScs.getItemScoreTextAndTitleList() !=null && !commentScs.getItemScoreTextAndTitleList().isEmpty() )
                    commentItemScoreTtl.addAll( commentScs.getItemScoreTextAndTitleList() );

                // this allows unscored survey responses to be presented using existing logic.
                else if( commentItemScoreTtl.isEmpty() && commentScs.getSimCompetencyClass().isUnscored() && commentScs.getHasUnscoredItems() && commentScs.getTextBasedResponseList()!=null )
                    commentItemScoreTtl.addAll( commentScs.getTextBasedResponseList() );

                if( !commentItemScoreTtl.isEmpty() )
                {
                    itemScoreTtl = mergeCommentItemScoreTtlWithTtl( commentItemScoreTtl, itemScoreTtl );
                    
                    // LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() CCC.4 After merging itemScoreTtl.size=" + itemScoreTtl.size() + ", scs.name=" + scs.getNameEnglish() + "" );                    
                }

                commentScs.setBoolean1(true);
            }

            if( !itemScoreTtl.isEmpty() )
                tp += "[" + Constants.ITEMSCOREINFOKEY + "]" + packTextBasedResponses(itemScoreTtl);

            if( scs.getSimCompetencyObj().getPresentationtype()==ScorePresentationType.SPECTRUM.getScorePresentationTypeId() )
            {
                 tp += "[" + Constants.COMPETENCYSPECTRUMKEY + "]" +
                        (scs.getSimCompetencyObj().getLowendname()==null ? "" : scs.getSimCompetencyObj().getLowendname()) +
                         Constants.DELIMITER  + (scs.getSimCompetencyObj().getHighendname()==null ? "" : scs.getSimCompetencyObj().getHighendname()) ;
            }

            if( tp.isEmpty() )
                tp = null;

            if( cefrType!= null && cefrScoreType!=null && !cefrScoreType.equals( CefrScoreType.UNKNOWN) )
            {
                // LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() FFF.1 Competency=" + scs.getName() + ", CEFR=" + scs.getSimCompetencyObj().getCefr() + ", Type=" + cefrType.getName() + ", ScoreType=" + cefrScoreType.getName());
                tp += "[" + Constants.CEFRTYPE + "]" + cefrType.getCefrTypeId();
                tp += "[" + Constants.CEFRLEVEL + "]" + cefrScoreType.getTextVal();
                tp += "[" + Constants.CEFRLEVELTEXT + "]" + CefrUtils.getCefrScoreDescription(simLocale, cefrScoreType, cefrType.getStub());
            }

            List<MergableScoreObject> mergableScoreObjectList = scs.getMergableScoreObjectList();

            if( mergableScoreObjectList!=null && !mergableScoreObjectList.isEmpty() )
            {
                List<MergableScoreObject> distinctList = MergableScoreObjectCombiner.combineLikeObjects( mergableScoreObjectList );

                StringBuilder sbx = new StringBuilder();

                for( MergableScoreObject mso : distinctList )
                {
                    sbx.append( mso.getPackedTokenStringForTestEventScore() );
                }

                if( sbx.length()>0 )
                {
                    if( tp==null )
                        tp="";

                    tp += sbx.toString();
                }
            }

            tes.setTextParam1( tp );

            //if( tes.getTextParam1() != null && tes.getTextParam1().isEmpty() )
            //    tes.setTextParam1( null );

             if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "BaseTestEventScorer.setCompetencyTestEventScores() saving " + tes.getName() + ", raw=" + tes.getRawScore() + ", scaled score=" + tes.getScore() + ", totalUsed=" + tes.getTotalUsed() );

            eventFacade.saveTestEventScore(tes);

            Thread.sleep( 100 );   // 500

            if( te.getProduct()==null )
                    te.setProduct( eventFacade.getProduct( te.getProductId() ));

            if( computePercentiles && tes.getScore()>= 0 && te.getProduct().getIntParam7()==1 )
            {
                norm = getPercentile(te.getProductId(), te.getProduct().getIntParam24(), te, tes, te.getOrgId(), cc);

                percentile = norm==null ? -1 : ((Float)norm.get("percentile"));
                pcount = norm==null ? 0 : ((Integer)norm.get("count"));

                tes.setPercentile( percentile );
                tes.setOverallPercentileCount( pcount );
                if( percentile<=0 )
                {
                    tes.setAccountPercentile( -1 );
                    tes.setAccountPercentileCount( 0 );
                    tes.setCountryPercentile( -1 );
                    tes.setCountryPercentileCount( 0 );
                }

                else
                {
                    // norm = getPercentile(te.getProductId(), te, tes, te.getOrgId(), null);
                    percentile = norm==null ? -1 : ((Float)norm.get("percentileorg"));
                    pcount = norm==null ? 0 : ((Integer)norm.get("countorg"));

                    tes.setAccountPercentile( percentile );
                    tes.setAccountPercentileCount( pcount );

                    //norm = cc == null || cc.length()==0 ? null : getPercentile(te.getProductId(), te, tes, 0, cc);
                    //if( norm==null )
                    //{
                    //    norm = new HashMap<>();
                    //    norm.put( "percentile" , new Float(-1) );
                    //    norm.put( "count" , new Integer(0)) ;
                    //}

                    percentile = norm==null ? -1 : ((Float)norm.get("percentilecc"));
                    pcount = norm==null ? 0 : ((Integer)norm.get("countcc"));

                    tes.setCountryPercentile( percentile );
                    tes.setCountryPercentileCount( pcount );
                }

                eventFacade.saveTestEventScore(tes);

                // ALWAYS DO THIS AFTER SAVING TestEventScore
                if( te.getExcludeFmNorms()==0 )
                {
                    pctObj = createPercentileForScore(tk, te, tes );

                    if( pctObj != null )
                        eventFacade.savePercentile( pctObj );
                }
            }
            else
            {
                tes.setPercentile( -1 );
                tes.setOverallPercentileCount( 0 );
                tes.setAccountPercentile( -1 );
                tes.setAccountPercentileCount( 0 );
                tes.setCountryPercentile( -1 );
                tes.setCountryPercentileCount( 0 );
                eventFacade.saveTestEventScore(tes);
            }

            // tesl.add(tes);
        }

        return counter;
    }



    public SimCompetencyScore getCommentSimCompetencyScore( SimCompetencyScore scs, boolean returnParent )
    {
        if( scs==null || scs.getSimCompetencyObj()==null || scs.getSimCompetencyClass().getIsCombo() || scs.getSimCompetencyClass().getIsAggregateOrTask() )
            return null;

        // if return parent, scs must be an unscored competency.
        if( returnParent && !scs.getSimCompetencyClass().isUnscored() )
            return null;

        String n1, n2;
        for( SimCompetencyScore sc : te.getSimCompetencyScoreList() )
        {
            if( sc.getSimCompetencyObj()==null || sc.getSimCompetencyObj().getId()==scs.getSimCompetencyObj().getId() )
                continue;

            if( sc.getSimCompetencyClass().getIsCombo() || sc.getSimCompetencyClass().getIsAggregateOrTask() )
                continue;

            n1 = returnParent ? sc.getNameEnglish() : scs.getNameEnglish();
            n2 = returnParent ? scs.getNameEnglish() : sc.getNameEnglish();

            if( !n2.startsWith(n1) )
                continue;

            // decode
            n2 = StringUtils.getUrlDecodedValue(n2);

            if( n2.toLowerCase().endsWith("(for comments)") )
                return sc;
        }

        return null;
    }

    public List<TextAndTitle> mergeCommentItemScoreTtlWithTtl( List<TextAndTitle> commentTtl, List<TextAndTitle> mainTtl ) throws Exception
    {
        List<TextAndTitle> out = new ArrayList<>();

        if( mainTtl==null )
            throw new Exception( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() AAA.1 mainTtl is null" );

        if( commentTtl==null )
            throw new Exception( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() AAA.2 commentTtl is null" );

        LogService.logIt( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() AAA.3 mainTtl.size=" + mainTtl.size() + ", commentTtl.size=" + commentTtl.size() );
        
        if( commentTtl.isEmpty() )
            return mainTtl;

        if( mainTtl.isEmpty() )
            return commentTtl;

        String lastItemIdent = null;
        String currentItemIdent;
        for( TextAndTitle mttl : mainTtl )
        {
            currentItemIdent = mttl.getString1();
            if( currentItemIdent==null || currentItemIdent.isBlank() )
                throw new Exception( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() CCC.1 ItemScoreText and title has no string1 value (Item Unique Id). Something wrong. " + mttl.toString() );

            // if we are changing to a new Item, see if there is a comment for the last itemId
            if( lastItemIdent!=null && !lastItemIdent.equals(currentItemIdent ) )
            {
                for( TextAndTitle cttl : commentTtl )
                {
                    if( cttl.getString1()==null || cttl.getString1().isBlank() )
                        throw new Exception( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() CCC.2 Comment ItemScoreText and title has no string1 value (Item Unique Id). Something wrong. " + cttl.toString() );

                    if( cttl.getString1().equals(lastItemIdent ))
                    {
                        LogService.logIt( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() CCC.4 Adding commentTtl for " + cttl.getString1() );
                        out.add( cttl );
                        break;
                    }
                }
            }
            lastItemIdent = currentItemIdent;
            out.add(mttl );
        }

        if( lastItemIdent!=null && !lastItemIdent.isBlank() )
        {
            for( TextAndTitle cttl : commentTtl )
            {
                if( cttl.getString1()==null || cttl.getString1().isBlank() )
                    throw new Exception( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() DDD.2 Comment ItemScoreText and title has no string1 value (Item Unique Id). Something wrong. " + cttl.toString() );

                if( cttl.getString1().equals(lastItemIdent ))
                {
                    LogService.logIt( "BaseTestEventScorer.mergeCommentItemScoreTtlWithTtl() DDD.4 Adding commentTtl for " + cttl.getString1() );
                    out.add( cttl );
                    break;
                }
            }
        }

        return out;
    }

    public String getOverallScoreScoreText() throws Exception
    {
        boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(te.getOrg(), te); // && te.getOverallTestEventScore()!=null; //  && te.getOverallTestEventScore().getRawScore()>=0;

        float rawScoreToUseAsOverall = 0;

        if( useRawOverallScore )
        {
            if( OverallRawScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype() ).getUsesRawCompetencyScores() )
                rawScoreToUseAsOverall = calculateOldOverallWeightedAvgScore();
            else
                rawScoreToUseAsOverall = ovrRawScr;

            // LogService.logIt( "BaseTestEventScorer.setOverallScoreAndPercentile() useRawOverallScore=true testEventId=" + te.getTestEventId()+ ", rawScoreToUseAsOverall=" + rawScoreToUseAsOverall );
        }

        return getScoreTextForOverallScore( scoreColorSchemeType, useRawOverallScore ? rawScoreToUseAsOverall  : ovrScaledScr, te );
    }



    public int setOverallScoreAndPercentile( int counter ) throws Exception
    {
        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();

        if( te.getProduct()==null )
            te.setProduct( eventFacade.getProduct( te.getProductId() ));

        TestEventScore tes2;

        // Overall
        TestEventScore tes = new TestEventScore();

        tes.setTestEvent(te);
        tes.setName(UrlEncodingUtils.decodeKeepPlus( te.getSimXmlObj().getNameuser() != null && !te.getSimXmlObj().getNameuser().isEmpty() ? te.getSimXmlObj().getNameuser() : te.getSimXmlObj().getName() ) );
        // tes.setName( "Overall" );
        tes.setTestEventId( te.getTestEventId() );
        tes.setTestEventScoreTypeId( TestEventScoreType.OVERALL.getTestEventScoreTypeId() );
        tes.setScore( ovrScaledScr );
        tes.setRawScore(ovrRawScr);
        tes.setDateParam1(new Date());

        if( te.getResultXmlObj()!=null && te.getResultXmlObj().getEvent()!=null && te.getResultXmlObj().getEvent().getTmout()>0 )
            tes.setIntParam1(te.getResultXmlObj().getEvent().getTmout());

        boolean usesZScoreOverall = (overallRawScoreCalcType!=null && overallRawScoreCalcType.getRawNormalized() && this.normRawScore>-10) && te.getProduct()!=null && te.getProduct().getConsumerProductType().getIsJobSpecific();
        if( usesZScoreOverall )
        {
            float zScorePercentile = NumberUtils.convertZScoreToPercentile( ovrRawScr );
            if( zScorePercentile<1 )
                zScorePercentile=1;
            tes.setOverallZScorePercentile( zScorePercentile );
            tes.setOverallZScorePercentileValid(1);
        }
        else
        {
            tes.setOverallZScorePercentile( 0 );
            tes.setOverallZScorePercentileValid(0);
        }

        boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(te.getOrg(), te); // && te.getOverallTestEventScore()!=null; //  && te.getOverallTestEventScore().getRawScore()>=0;

        float rawScoreToUseAsOverall = 0;

        if( useRawOverallScore )
        {
            if( OverallRawScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype() ).getUsesRawCompetencyScores() )
                rawScoreToUseAsOverall = calculateOldOverallWeightedAvgScore();
            else
                rawScoreToUseAsOverall = ovrRawScr;

            // LogService.logIt( "BaseTestEventScorer.setOverallScoreAndPercentile() useRawOverallScore=true testEventId=" + te.getTestEventId()+ ", rawScoreToUseAsOverall=" + rawScoreToUseAsOverall );
        }

        String scrTxt = getOverallScoreScoreText();
        String scrFmt = I18nUtils.getFormattedNumber( Locale.US , useRawOverallScore ? rawScoreToUseAsOverall : ovrScaledScr, 1 );
        scrTxt  = performStandardSubstitutions( scrTxt, scrFmt );
        tes.setScoreText( scrTxt );
        // tes.setScoreText( getScoreTextForOverallScore(scoreColorSchemeType, useRawOverallScore ? rawScoreToUseAsOverall  : ovrScaledScr, te ) );


        tes.setScoreCategoryId( te.getOverallRating() );
        tes.setScoreFormatTypeId( scoreFormatTypeId );
        tes.setWeight( hasWeights ? 1 : 0 );
        tes.setDisplayOrder( counter );

        tes2 = getMatchingExistingTestEventScore(tes);
        if( tes2!=null && tes2.getTestEventScoreId()>0 )
        {
            tes.setTestEventScoreId( tes2.getTestEventScoreId() );
            tes.setDateParam2( tes2.getDateParam2());
            // tes.setDisplayOrder( tes2.getDisplayOrder() );
        }

        String tp = "";

        // save the report overview text.
        if( te.getSimXmlObj() != null &&  te.getSimXmlObj().getReportoverviewtext() != null )
        {
            tp +=  "[" + Constants.DESCRIPTIONKEY + "]" + UrlEncodingUtils.decodeKeepPlus( te.getSimXmlObj().getReportoverviewtext(), "UTF8" );
            // tes.setTextParam1( Constants.DESCRIPTIONKEY + URLDecoder.decode( te.getSimXmlObj().getReportoverviewtext(), "UTF8" ) );
        }


        float frcMin = 0;
        float frcMax = 0;


        if( ScoreFormatType.getValue( scoreFormatTypeId ).equals( ScoreFormatType.OTHER_SCORED ) )
        {
            long reportId = 0;
            if( te.getProduct()!=null )
            {
                if( te.getProduct().getLongParam2()>0 )
                    reportId = te.getProduct().getLongParam2();
                else if( te.getProduct().getLongParam3()>0 )
                    reportId = te.getProduct().getLongParam3();
                else if( te.getProduct().getLongParam5()>0 )
                    reportId = te.getProduct().getLongParam5();
            }

            Report report = reportId>0 ? eventFacade.getReport(reportId) : null;

            if( report!=null && report.getFloatParam1() < report.getFloatParam2() )
            {
                frcMin = report.getFloatParam1();
                frcMax = report.getFloatParam2();
            }
        }

        if( hasCustomColorRanges )
        {
            String categInfo = scoreColorSchemeType.getScoreCategoryInfoStringForSim( te.getSimXmlObj(), frcMin, frcMax );

            if( !categInfo.isEmpty() )
                tp += "[" + Constants.CATEGORYINFOKEY + "]" + categInfo;
        }

        // This is a tweak so that the admin system will show the correct score for accounts set to use weighted average.
        if( useRawOverallScore ) // te.getOrg().getShowOverallRawScore()==1 && ProductType.getValue( te.getProductTypeId()).getIsSim()  && ConsumerProductType.getValue( te.getProduct().getConsumerProductTypeId()).getIsJobSpecific() && !useRawOverallScore )
            tp += "[" + Constants.OVERRIDESHOWRAWSCOREKEY + "]" + NumberUtils.getOneDecimalFormattedAmount(rawScoreToUseAsOverall );

        tes.setTextParam1( tp );

        if( tes.getTextParam1() != null && tes.getTextParam1().isEmpty() )
            tes.setTextParam1(null);

        // set all the non-competency responses. This should include all AV_Upload and File Upload NonComp Responses and all ScoreAVUpload Item Responses. They should all be here.
        tes.setTextbasedResponses( packGeneralNoncompetencyResponses( te.getAllIactnResponseList() ));

        eventFacade.saveTestEventScore(tes);

        Thread.sleep( 500 );

        // String frcCountry = null;

        if( tk!=null && tk.getFrcCountry()!=null && !tk.getFrcCountry().isEmpty() )
            frcCountry = tk.getFrcCountry();

        else if( te.getSuborg()!=null && te.getSuborg().getPercentileCountry()!=null && !te.getSuborg().getPercentileCountry().isEmpty() )
            frcCountry = te.getSuborg().getPercentileCountry();

        else if( te.getOrg()!=null && te.getOrg().getPercentileCountry()!=null && !te.getOrg().getPercentileCountry().isEmpty() )
            frcCountry = te.getOrg().getPercentileCountry();

        String cc = te.getIpCountry();

        if( frcCountry != null && !frcCountry.isEmpty() )
            cc = frcCountry;

        boolean computePercentiles = !reportRules.getReportRuleAsBoolean("percentilesoff");

        if( computePercentiles )
        {
            // for overall
            Map<String,Object> norm = getPercentile(te.getProductId(), te.getProduct().getIntParam24(), te, tes, te.getOrgId(), cc);

            var percentile = norm==null ? -1 :  ((Float)norm.get("percentile"));
            int pcount = norm==null ? 0 : ((Integer)norm.get("count"));

            // LogService.logIt( "BaseTestEventScorer Overall Percentiles: " + percentile + "-" + pcount );

            te.setOverallPercentile( percentile );
            te.setOverallPercentileCount( pcount );
            tes.setPercentile( percentile );
            tes.setOverallPercentileCount( pcount );

            if( !tes.getHasValidOverallNorm() && tes.getHasValidOverallZScoreNorm() && te.getProduct()!=null && te.getProduct().getConsumerProductType().getIsJobSpecific() )
            {
                // LogService.logIt( "BaseTestEventScorer.setOverallScoreAndPercentile() Setting TestEvent.overallPercentile to ZScore Percentile. TestEventId=" + te.getTestEventId() );
                te.setOverallPercentile( tes.getOverallZScorePercentile() );
                te.setOverallPercentileCount( 0 );

                tes.setAccountPercentile( -1 );
                te.setAccountPercentile( -1 );
                te.setAccountPercentileCount( 0 );
                tes.setAccountPercentileCount( 0 );
                tes.setCountryPercentile( -1 );
                te.setCountryPercentile( -1 );
                te.setCountryPercentileCount( 0 );
                tes.setCountryPercentileCount( 0 );
            }


            else
            {
                // For the Org
                // norm = getPercentile(te.getProductId(), te, tes, te.getOrgId(), null);
                percentile = norm==null ? -1 : ((Float)norm.get("percentileorg"));
                pcount = norm==null ? 0 : ((Integer)norm.get("countorg"));

                tes.setAccountPercentile( percentile );
                te.setAccountPercentile( percentile );
                te.setAccountPercentileCount( pcount );
                tes.setAccountPercentileCount( pcount );

                //String cc = te.getIpCountry();

                //if( frcCountry != null && !frcCountry.isEmpty() )
                //    cc = frcCountry;

                // for the country
                //norm = cc == null || cc.length()==0 ? null : getPercentile(te.getProductId(), te, tes, 0, cc);

                //if( norm==null )
                //{
                //    norm = new HashMap<>();
                //    norm.put( "percentile" , new Float(-1) );
                //    norm.put( "count" , new Integer(0)) ;
                //}

                percentile = norm==null ? -1 : ((Float)norm.get("percentilecc"));
                pcount = norm==null ? 0 : ((Integer)norm.get("countcc"));

                tes.setCountryPercentile( percentile );
                te.setCountryPercentile( percentile );
                te.setCountryPercentileCount( pcount );
                tes.setCountryPercentileCount( pcount );
                te.setPercentileCountry(cc);
                tes.setPercentileCountry(cc);
            }
        }
        else
        {
            te.setOverallPercentile( -1 );
            te.setOverallPercentileCount( 0 );
            tes.setPercentile( -1 );
            tes.setOverallPercentileCount( 0 );
            tes.setAccountPercentile( -1 );
            te.setAccountPercentile( -1 );
            te.setAccountPercentileCount( 0 );
            tes.setAccountPercentileCount( 0 );
            tes.setCountryPercentile( -1 );
            te.setCountryPercentile( -1 );
            te.setCountryPercentileCount( 0 );
            tes.setCountryPercentileCount( 0 );
        }

        // LogService.logIt( "BaseTestEventScorer Overall Percentiles: overall: " + tes.getPercentile()+ "-" + tes.getOverallPercentileCount() + ", Account: " + tes.getAccountPercentile() + "-" + tes.getAccountPercentileCount() + ", country: " + tes.getCountryPercentile() + "-" + tes.getCountryPercentileCount() );

        eventFacade.saveTestEventScore(tes);

        // Count sim competency scores that were not scorable. If more than 50% are 0,
        float pctNotScorable = te.getSimCompetencyScoreList().size()>0 ? 100f*((float)scsNotScorableCt)/((float)te.getSimCompetencyScoreList().size()) : 0;

        float pctScorableZero = scsScorableCt>0 ? 100f*((float)scsScorableZeroCt)/((float)scsScorableCt) : 0;

        // if 3/4 not scorable or more, or 85% of scorable are zero, don't include in norms.
        if( pctNotScorable>=75 || pctScorableZero>=85 )
        {
            LogService.logIt( "BaseTestEventScorer.setOverallScoreAndPercentile() Disabling from norms because of a trigger condition: pctNotScorable=" + pctNotScorable + " (min 75), pctScorableZero=" + pctScorableZero + " (min 85)" );
            te.setExcludeFmNorms( 1 );
        }

        Percentile pctObj;

        // ALWAYS DO THIS AFTER SAVING TestEventScore
        if( te.getExcludeFmNorms()==0 )
        {
            pctObj = createPercentileForScore(tk, te, tes );

            if( pctObj != null )
                eventFacade.savePercentile(pctObj);
        }

        return counter;

    }



    public float calculateOldOverallWeightedAvgScore()
    {
        if( te==null || te.getSimCompetencyScoreList()==null )
            return 0;

        float ovrRawScr2=0;
        float totalWeights2 = 0;

        StringBuilder sb = new StringBuilder();

        // now complete the scoring of each SimCompetency
        for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
        {
            // If competency set to include in overall only if below X and score is above X, skip it.
            if( scs.getSimCompetencyObj().getIncludeinoverallifbelow() > 0 && scs.getUnweightedScaledScore( false ) >= scs.getSimCompetencyObj().getIncludeinoverallifbelow() )
                continue;

            // If we have an Identity Image Capture competency. Don't include in overall score.
            if(  scs.getSimCompetencyClass().equals( SimCompetencyClass.SCOREDIMAGEUPLOAD ) )
                continue;

            // If we have a task-type of competency, and not supposed to include tasks in the overall score, don't include it.
            if(  scs.getSimCompetencyClass().getIsTask() )
                continue;

            // if have non-task type of competency and not supposed to include in overall, skip it.
            if(  ( scs.getSimCompetencyClass().getIsDirectCompetency() || scs.getSimCompetencyClass().getIsAggregate() ) && te.getSimXmlObj().getIncludecompetenciesoverall() != 1 )
                continue;

            if(  scs.getSimCompetencyClass().getIsInterest() && te.getSimXmlObj().getIncludeinterestoverall() != 1 )
                continue;

            if(  scs.getSimCompetencyClass().getIsExperience() && te.getSimXmlObj().getIncludeexperienceoverall() != 1 )
                continue;

            if(  scs.getSimCompetencyClass().getIsBiodata() && te.getSimXmlObj().getIncludebiodataoverall() != 1 )
                continue;

            // If this is supposed to be a z-score but there was no local std deviation to use and no simlet competency stat record, ignore in weight calcs.
            //if( SimCompetencyRawScoreCalcType.getValue( scs.getSimCompetencyObj().getRawscorecalctypeid() ).getIsZScore() && scs.getSimCompetencyObj().getStddeviation()<=0 )
            //    continue;

            if( scs.getHasScoreableData() )
            {
                sb.append( "Competency " + scs.getName() + " weight=" + + scs.getUserWeight() + ", Scaled score=" + scs.getUnweightedScaledScore(true) + ", ");

// LogService.logIt( "BaseTestEventScorer.calculateOldOverallWeightedAvgScore scs=" + competencyName + ", scs.getUserWeight()=" + scs.getUserWeight() + ", scs.getUserWeightedScaledScore( true )=" + scs.getUserWeightedScaledScore( true ) );
                if( scs.getUserWeight() > 0 || hasWeights )
                {
                    if( CategoryDistType.getValue( scs.getSimCompetencyObj().getCategorydisttype() ).getLinear() && scs.getSimCompetencyObj().getUsecategforoverall()!=1 )
                    {
                        ovrRawScr2 += scs.getUserWeightedScaledScore( true );
                        sb.append( "linear, scoreUsed=" + scs.getUserWeightedScaledScore( true ) );
                    }

                    else
                    {
                        int catId = ScoreUtils.getScoreCategoryTypeId( scs.getSimCompetencyObj(), scs.getUnweightedScaledScore(true), scoreColorSchemeType );
                        ovrRawScr2 +=  ScoreCategoryType.getValue( catId ).getNumericEquivScore( te.getSimXmlObj().getScoreformat() )*scs.getUserWeight();
                        sb.append( "non-linear, catId=" + ScoreCategoryType.getValue( catId ).getName(Locale.US) + ", scoreUsed=" + ScoreCategoryType.getValue( catId ).getNumericEquivScore( te.getSimXmlObj().getScoreformat() )*scs.getUserWeight() );
                    }

                    totalWeights2 += scs.getUserWeight();
                }

                // No weights, everything gets a weight of 1
                else
                {
                    if( CategoryDistType.getValue( scs.getSimCompetencyObj().getCategorydisttype() ).getLinear() && scs.getSimCompetencyObj().getUsecategforoverall()!=1 )
                    {
                       //ovrRawScr2 += scs.getUserWeightedScaledScore( true );
                        //sb.append( " no weights, linear, scoreUsed=" + scs.getUserWeightedScaledScore( true ) );
                        ovrRawScr2 += scs.getUnweightedScaledScore( true );
                        sb.append( " no weights, linear, scoreUsed=" + scs.getUnweightedScaledScore( true ) );
                    }

                    else
                    {
                        int catId = ScoreUtils.getScoreCategoryTypeId( scs.getSimCompetencyObj(), scs.getUnweightedScaledScore(true), scoreColorSchemeType );
                        ovrRawScr2 +=  ScoreCategoryType.getValue( catId ).getNumericEquivScore( te.getSimXmlObj().getScoreformat() );
                        sb.append( " no weights, non-linear, catId=" + ScoreCategoryType.getValue( catId ).getName(Locale.US) + ", scoreUsed=" + ScoreCategoryType.getValue( catId ).getNumericEquivScore( te.getSimXmlObj().getScoreformat() ) );
                    }

                    //ovrRawScr2 += scs.getUnweightedScaledScore( true );
                    totalWeights2++;
                    //sb.append( " linear, scoreUsed=" + scs.getUserWeightedScaledScore( true ) );
                }

                sb.append( "\n" );
            }
        }

        float sv = totalWeights2 > 0 ? ovrRawScr2 / totalWeights2 : 0;

        // LogService.logIt( "BaseTestEventScorer.calculateOldOverallWeightedAvgScore() " +  sb.toString() + " totalWeights=" + totalWeights2 + " totalOvrScr2=" + ovrRawScr2 + ", score value computed=" + sv );

        ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scoreFormatTypeId );

        if(  sv<scoreFormatType.getMinScoreToGiveTestTaker() )
            sv=scoreFormatType.getMinScoreToGiveTestTaker();

        if(  sv>scoreFormatType.getMaxScoreToGiveTestTaker())
            sv=scoreFormatType.getMaxScoreToGiveTestTaker();

        return sv;
    }



   public void calculateOverallScores() throws Exception
   {
        if( this.overallRawScoreCalcType.getIsAnyWeights() )
        {
            scrSum.append( "Total weights=" + totalWeights + ", total weights*scores=" + ovrRawScr + "\n" );

            // next, get the overall score
            if( totalWeights > 0 )
                ovrRawScr = ovrRawScr/totalWeights;
        }

        else if( overallRawScoreCalcType.getIsSum())
        {
            scrSum.append( "Sum of raw scores=" + ovrRawScr + "\n" );
        }


        scrSum.append( "Overall Raw Score: " + ovrRawScr  + "\n" );

        // LogService.logIt( "BaseTestEventScorer.scoreTestEvent() AFTER applying weights. ovrRawScr=" + ovrRawScr + ", totalWeights=" + totalWeights + ", testEventId=" + te.getTestEventId() );

        if( overallScaledScoreCalcType==null )
            overallScaledScoreCalcType=OverallScaledScoreCalcType.getValue( te.getSimXmlObj().getOverallscaledscorecalctype() );

        ScoreFormatType scoreFormatType = ScoreFormatType.getValue( scoreFormatTypeId );

        if( overallScaledScoreCalcType.equals(OverallScaledScoreCalcType.NORMAL_TRANS ) && overallRawScoreCalcType.getUsesRawCompetencyScores() )
        {

            if( overallRawScoreCalcType.getRawNormalized() )
            {
                float rawMean = te.getSimXmlObj().getRawtoscaledfloatparam3();
                float rawStd = te.getSimXmlObj().getRawtoscaledfloatparam4();

                if( rawStd<=0 )
                {
                    rawMean=RuntimeConstants.getFloatValue( "SimRawScoreZConversionMeanDefault" );
                    rawStd=RuntimeConstants.getFloatValue( "SimRawScoreZConversionStdevDefault" );
                }

                //if( nonlinearSimCompetencyCount>1 )
                //    rawMean += RuntimeConstants.getFloatValue( "SimRawScoreZConversionMeanMultiNonlinearAdjustment" );

                normRawScore = (ovrRawScr - rawMean)/rawStd;
                ovrScaledScr = NumberUtils.applyNormToZScore(normRawScore, te.getSimXmlObj().getRawtoscaledfloatparam1(), te.getSimXmlObj().getRawtoscaledfloatparam2() );
            }

            else
                ovrScaledScr = NumberUtils.applyNormToZScore(ovrRawScr, te.getSimXmlObj().getRawtoscaledfloatparam1(), te.getSimXmlObj().getRawtoscaledfloatparam2() );

        }

        else if( overallScaledScoreCalcType.getEqualsRawScore() )
        {
            ovrScaledScr = ovrRawScr;
        }

        else
        {
            ovrScaledScr = overallScaledScoreCalcType.getScaledScore( te.getProductId(),
                                                                        ovrRawScr ,
                                                                        te.getSimXmlObj().getMean(),
                                                                        te.getSimXmlObj().getStddeviation(),
                                                                        te.getSimXmlObj().getRawtoscaledfloatparam1(),
                                                                        te.getSimXmlObj().getRawtoscaledfloatparam2(),
                                                                        scoreFormatTypeId,
                                                                        te.getSimXmlObj().getLookuptable(),
                                                                        scrSum );
        }

        scrSum.append( "Overall Scaled Score Prior to Floor/Ceiling: " + ovrScaledScr  + "\n" );

        if(  ovrScaledScr<scoreFormatType.getMinScoreToGiveTestTaker() )
            ovrScaledScr=scoreFormatType.getMinScoreToGiveTestTaker();

        if(  ovrScaledScr>scoreFormatType.getMaxScoreToGiveTestTaker())
            ovrScaledScr=scoreFormatType.getMaxScoreToGiveTestTaker();

        scrSum.append( "Overall Scaled Score After to Floor/Ceiling: " + ovrScaledScr  + "\n" );

        // LogService.logIt( "BaseTestEventScorer.scoreTestEvent() ovrScaledScr=" + ovrScaledScr + ",  scoreFormatTypeId=" + scoreFormatTypeId );
        // LogService.logIt( scrSum.toString() );

        //if( normFacade == null )
        //   normFacade = NormFacade.getInstance();

        // save the test event
        te.setOverallScore(ovrScaledScr);

        boolean useRawOverallScore = ScoreUtils.getIncludeRawOverallScore(te.getOrg(), te)  && te.getOverallTestEventScore()!=null; //  && te.getOverallTestEventScore().getRawScore()>=0;

        te.setOverallRating( ScoreCategoryType.getForScore(scoreColorSchemeType, useRawOverallScore ? te.getOverallTestEventScore().getOverallRawScoreToShow() : ovrScaledScr, 0, te.getSimXmlObj().getWhitemin(), te.getSimXmlObj().getGreenmin(), te.getSimXmlObj().getYellowgreenmin(), te.getSimXmlObj().getYellowmin(), te.getSimXmlObj().getRedyellowmin() , te.getSimXmlObj().getRedmin(), te.getSimXmlObj().getScoreformat(), 0, 0 ).getScoreCategoryTypeId() );
   }


    protected Locale getBaseReportingLocale() throws Exception
    {
        if( te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

        if( te.getReportId()>0 )
        {
            if( te.getReport()==null )
            {
                if( eventFacade==null )
                    eventFacade = EventFacade.getInstance();

                te.setReport( eventFacade.getReport( te.getReportId() ));
            }

            if( te.getReport()!=null && te.getReport().getLocaleStr()!=null && !te.getReport().getLocaleStr().isEmpty() )
                return I18nUtils.getLocaleFromCompositeStr( te.getReport().getLocaleStr() );
        }

        if( tk.getAuthorizingUserId()>0 )
        {
            if(tk.getAuthUser()==null )
            {
                if( userFacade==null )
                    userFacade=UserFacade.getInstance();

                tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));
            }

            if( tk.getAuthUser()!=null && tk.getAuthUser().getLocaleStr()!=null && !tk.getAuthUser().getLocaleStr().isEmpty() )
                return I18nUtils.getLocaleFromCompositeStr( tk.getAuthUser().getLocaleStr() );
        }

        if( te.getLocaleStr()!=null && !te.getLocaleStr().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() );

        return Locale.US;
    }


    public String performStandardSubstitutions( String inStr, String scoreFormatted ) throws Exception
    {
        try
        {
            if( inStr==null || inStr.isBlank() )
                return inStr;

            if( user==null )
                user = tk.getUser();

            if( user==null )
                user = te.getUser();

            if( user==null )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                user = userFacade.getUser(te.getUserId() );
            }

            inStr = StringUtils.replaceStr( inStr, "[FIRSTNAME]", user.getFirstName() );
            inStr = StringUtils.replaceStr( inStr, "[LASTNAME]", user.getLastName() );
            inStr = StringUtils.replaceStr( inStr, "[EMAIL]", user.getEmail());
            inStr = StringUtils.replaceStr( inStr, "[USERID]", user.getEmail());
            inStr = StringUtils.replaceStr( inStr, "[SCORE]", scoreFormatted);

            if( inStr.indexOf("[HIGHCOMPETENCYNAMES:")>=0 || inStr.indexOf("[LOWCOMPETENCYNAMES:")>=0 )
            {
                    List<TestEventScore> scsl  = new ArrayList<>();

                    TestEventScore tes;
                    // Do competencies first
                    for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
                    {
                        if( !scs.hasAnyScoreData() )
                            continue;

                        if( !SimCompetencyVisibilityType.getValue( scs.getSimCompetencyObj().getHide() ).getShowInReports() )
                            continue;

                        // Skip Competencies that have no items.
                        if( te.getSimXmlObj().getInclcompswnoitmsinscrdata()==0 && scs.getSimCompetencyObj().getScoreifnoresponses()==0 && scs.getTotalScorableItems()==0 && !scs.getHasUnscoredItems() )
                            continue;

                        tes = new TestEventScore();
                        tes.setName( scs.getName() );
                        tes.setScore( scs.getUnweightedScaledScore( false ) );
                        scsl.add( tes );
                    }


                    String cut;
                    float cutf;
                    String fullKey ;
                    int idx;
                    StringBuilder sb;
                    if( inStr.indexOf("[HIGHCOMPETENCYNAMES:")>=0 )
                    {
                        Collections.sort( scsl, new TESScoreComparator( true ) );
                        idx = inStr.indexOf("[HIGHCOMPETENCYNAMES:");
                        cut = inStr.substring( idx + 21, inStr.indexOf("]", idx+20) );
                        cutf = cut.isBlank() ? 0 : Float.parseFloat( cut );
                        fullKey = "[HIGHCOMPETENCYNAMES:" + cut + "]";
                        sb = new StringBuilder();
                        for( TestEventScore tesx : scsl )
                        {
                            if( tesx.getScore()<cutf )
                                break;
                            if( sb.length()>0 )
                                sb.append( ", " );
                            sb.append( tesx.getName() );
                        }
                        inStr = StringUtils.replaceStr(inStr, fullKey, sb.length()>0 ? sb.toString() : MessageFactory.getStringMessage( reportLocale,"g.None") );
                    }

                    if( inStr.indexOf("[LOWCOMPETENCYNAMES:")>=0 )
                    {
                        Collections.sort( scsl, new TESScoreComparator( false ) );
                        idx = inStr.indexOf("[LOWCOMPETENCYNAMES:");
                        cut = inStr.substring( idx + 20, inStr.indexOf("]", idx+19) );
                        cutf = cut.isBlank() ? 0 : Float.parseFloat( cut );
                        fullKey = "[LOWCOMPETENCYNAMES:" + cut + "]";
                        sb = new StringBuilder();
                        for( TestEventScore tesx : scsl )
                        {
                            if( tesx.getScore()>cutf )
                                break;
                            if( sb.length()>0 )
                                sb.append( ", " );
                            sb.append( tesx.getName() );
                        }
                        inStr = StringUtils.replaceStr(inStr, fullKey, sb.length()>0 ? sb.toString() : MessageFactory.getStringMessage( reportLocale,"g.None") );
                    }
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e,"BaseTestEventScorer.performStandardSubstitutions() " + inStr );
        }

        return inStr;
    }



    public boolean isValidItemsCanHaveZeroMaxPoints() {
        return validItemsCanHaveZeroMaxPoints;
    }

    public void setValidItemsCanHaveZeroMaxPoints(boolean validItemsCanHaveZeroMaxPoints) {
        this.validItemsCanHaveZeroMaxPoints = validItemsCanHaveZeroMaxPoints;
    }


    public String getScoreStatusStr()
    {
        if( scrSum!=null )
            return scrSum.toString();

        return "";
    }
}
