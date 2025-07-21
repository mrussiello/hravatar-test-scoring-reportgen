/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.item;

import com.tm2score.entity.essay.EssayPrompt;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.essay.EssayFacade;
import com.tm2score.essay.EssayScoreStatusType;
import com.tm2score.service.LogService;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.user.User;
import com.tm2score.essay.AiEssayScoringThread;
import com.tm2score.essay.AiEssayScoringUtils;
import com.tm2score.essay.EssayPlagiarismCheckType;
import com.tm2score.essay.LocalEssayScoringUtils;
import com.tm2score.essay.UnscoredEssayType;
import com.tm2score.essay.copyscape.CopyScapeUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.Tracker;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.util.TextProcessingUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class ScoredEssayIntnItem {

    // Dummy Essay prompt will have the system perform spell/plagiarism checks but does not incorporate an essay score.
    // So, it still produces a score.
    public static final int DUMMY_ESSAY_PROMPT_ID = 999999;
    private static Boolean AI_SCORING_ON;
    // private static Boolean USE_AI_FOR_SCORE2;

    public static final float AVG_WORDS_PER_MIN_PLAG_CHECK = 15f;
    private static final float MAX_WORDS_PER_MIN_PLAG_CHECK = 27f;
    private static final int MIN_WORDS_FORCE_WEB_PLAG_CHECK = 360;
    private static final int MIN_WORDS_WEB_PLAG_CHECK = 25;
    private static final float MAX_DUPLICATE_WORDS_PCT = 35;
    private static final float MAX_DUPLICATE_LONGWORDS_PCT = 30;

    //private static String[] VALID_LANGUAGES = new String[]
    //{
    //    "en"
    //};

    SimJ simJ;
    String localeStr;

    // Machine Score goes between 0 - 100.0
    float machineScore = 0;

    // Confidence goes between 0 - 1.0
    float confidence = 0;
    float confidence2 = 0;

    int spellErrors = 0;
    int otherErrors = 0;
    int plagiarized = 0;
    Map<Integer, Float> essayMetaScoreMap = null;

    int maxPlagCheckRows = 0;

    float wpm = 0;
    float highWpm = 0;
    int speedError = 0;
    int totalWords = 0;
    float pctDupWords = 0;
    float pctDupLongWords = 0;

    int minWords = 1;
    int maxWords = 0;
    String summaryText;

    int essayPromptId;
    int ct5ItemId;
    int ct5ItemPartId;
    String essayStr;
    String question;
    String idealResponse;
    String aiInstructions;
    boolean omitGrammarMech;
    int equivWords = 0;
    float composeTime = 0;

    boolean hasValidScore = false;
    long testEventId = 0;
    User user;
    int nodeSeqId = 0;
    int subnodeSeqId = 0;

    boolean pendingExternalScores = false;

    EssayFacade essayFacade = null;
    // DiscernUtils discernUtils = null;
    int webPlagCheckOk = 0;
    String transCompare = null;
    float transCompareScore = -1;
    String translatedText = null;

    Locale txtLocale;
    String teIpCountry;
    String errMsg;

    Map<String, Integer> misSpells;
    List<String> spellWordsToIgnore;

    int unscoredEssayId;

    // 0=Essay
    int unscoredEssayTypeId = 0;

    public boolean hasSpellingGrammarStyle = false;
    public boolean hasValidAiComputedScore = false;

    boolean itemLevelAiScoringOk = false;
    boolean submittedForAiScoring = false;
    boolean includeSummary = false;

    public ScoredEssayIntnItem(long testEventId,
            User user,
            String teIpCountry,
            String localeStr,
            SimJ simJ,
            SimJ.Intn intnObj,
            SimJ.Intn.Intnitem intItemObj,
            int promptId,
            int ct5ItemId,
            int ct5ItemPartId,
            String essayStr,
            String question,
            int minWds,
            int maxWords,
            float cTime,
            int webPlagCheckOk,
            int maxPlagCheckRows,
            String transCompare)
    {
        this.testEventId = testEventId;
        this.user = user;
        this.teIpCountry = teIpCountry;
        this.localeStr = localeStr;
        this.simJ = simJ;
        this.nodeSeqId = intnObj.getSeq();
        this.subnodeSeqId = intItemObj.getSeq();
        this.essayPromptId = promptId;
        this.ct5ItemId = ct5ItemId;
        this.ct5ItemPartId = ct5ItemPartId;
        this.essayStr = essayStr;
        this.question = question;
        this.minWords = minWds > 0 ? minWds : 1;
        this.maxWords = maxWords;
        this.composeTime = cTime;
        this.webPlagCheckOk = webPlagCheckOk;
        this.maxPlagCheckRows = maxPlagCheckRows;
        this.transCompare = transCompare;
        this.omitGrammarMech = intnObj.getCt5Int13()==1;

        if( intnObj.getTextscoreparam1()!=null && !intnObj.getTextscoreparam1().isBlank() )
            this.idealResponse = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.IDEAL_RESPONSE_KEY );

        if( (idealResponse==null || idealResponse.isBlank()) &&  intItemObj.getTextscoreparam1()!=null && !intItemObj.getTextscoreparam1().isBlank() )
            this.idealResponse = StringUtils.getBracketedArtifactFromString(intItemObj.getTextscoreparam1(), Constants.IDEAL_RESPONSE_KEY );

        if( intnObj.getTextscoreparam1()!=null && !intnObj.getTextscoreparam1().isBlank() )
            this.aiInstructions = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.AI_INSTRUCTIONS_KEY );

        if( (aiInstructions==null || aiInstructions.isBlank()) &&  intItemObj.getTextscoreparam1()!=null && !intItemObj.getTextscoreparam1().isBlank() )
            this.aiInstructions = StringUtils.getBracketedArtifactFromString(intItemObj.getTextscoreparam1(), Constants.AI_INSTRUCTIONS_KEY );

        if (intItemObj.getLangcode() != null && !intItemObj.getLangcode().isBlank())
            txtLocale = I18nUtils.getLocaleFromCompositeStr(intItemObj.getLangcode());

        else if (simJ.getLang() != null && !simJ.getLang().isBlank())
            txtLocale = I18nUtils.getLocaleFromCompositeStr(simJ.getLang());

        if( intnObj.getCt5Int25()==1 || intnObj.getCt5Int25()==2 )
        {
            itemLevelAiScoringOk=true;
        }
        
        if( intnObj.getCt5Int25()==2 || intnObj.getCt5Int25()==3 )
            includeSummary=true;

        setSpellWordsToIgnore(StringUtils.getBracketedArtifactFromString(intItemObj.getTextscoreparam1(), Constants.SPELLING_IGNORE_KEY));

        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") ndseq=" + nodeSeqId + ", snSeq=" + subnodeSeqId + ", promptId=" + promptId + ", min/max words=" + this.minWords + "/" + this.maxWords );
    }

    public ScoredEssayIntnItem( int unscoredEssayTypeId,
            long testEventId,
            User user,
            Locale loc,
            String teIpCountry,
            int nodeSeq,
            int subnodeSeq,
            int promptId,
            int ct5ItemId,
            int ct5ItemPartId,
            String essayStr,
            String question,
            String idealResponse,
            String aiInstructions,
            boolean omitGrammarMech,
            boolean includeSummary,
            boolean itemLevelAiScoringOk,
            int minWds,
            int maxWords,
            float cTime,
            int webPlagCheckOk,
            int maxPlagCheckRows,
            List<String> spellWordsToIgnore)
    {
        this.testEventId = testEventId;
        this.user = user;
        this.txtLocale = loc;
        this.teIpCountry = teIpCountry;
        this.nodeSeqId = nodeSeq;
        this.subnodeSeqId = subnodeSeq;
        this.essayPromptId = promptId;
        this.ct5ItemId = ct5ItemId;
        this.ct5ItemPartId = ct5ItemPartId;
        this.essayStr = essayStr;
        this.question = question;
        this.idealResponse=idealResponse;
        this.aiInstructions=aiInstructions;
        this.minWords = minWds > 0 ? minWds : 1;
        this.maxWords = maxWords;
        this.composeTime = cTime;
        this.webPlagCheckOk = webPlagCheckOk;
        this.spellWordsToIgnore = spellWordsToIgnore;
        this.unscoredEssayTypeId=unscoredEssayTypeId;
        this.includeSummary=includeSummary;
        this.itemLevelAiScoringOk=itemLevelAiScoringOk;
        this.maxPlagCheckRows = maxPlagCheckRows;
        this.omitGrammarMech=omitGrammarMech;

        LogService.logIt("ScoredEssayIntnItem(testEventId=" + testEventId + ") ndseq=" + nodeSeq + ", snSeq=" + subnodeSeq + ", promptId=" + promptId + ", min/max words=" + this.minWords + "/" + this.maxWords);

    }

    static synchronized void initVariables()
    {
        if (AI_SCORING_ON!=null)
            return;

        AI_SCORING_ON = AiEssayScoringUtils.getAiEssayScoringOn();
        // USE_AI_FOR_SCORE2 = AiEssayScoringUtils.getAiEssayScoringOn() && AiEssayScoringUtils.getAiEssayScoringUseScore2();
    }

    public void calculate()
    {
        try
        {
            if (AI_SCORING_ON==null)
                initVariables();

            if (essayStr!= null && !essayStr.isEmpty())
            {
                String essayWithoutQuestion = removeQuestionFromEssayStr();

                totalWords = StringUtils.numWords(essayWithoutQuestion); //    DataEntryItem.getEquivWords(essayStr);
                equivWords = totalWords;

                if (maxWords>0 && equivWords>maxWords)
                {
                    essayStr = StringUtils.truncateToMaxWordCt(essayStr, maxWords);
                    equivWords = maxWords;
                }

                int wordAdj = Math.round(0.03f * ((float) minWords));

                // LogService.logIt( "ScoredEssayIntnItem.calculate() AAA.0 minWords=" + minWords +", equivWords=" + equivWords + ", maxWords=" + maxWords + ", totalWords=" + totalWords + ", wordAdj=" + wordAdj + ", testEventId=" + testEventId + "\n\n" + essayStr + "\n\n" + essayWithoutQuestion);

                if (equivWords>=(minWords - wordAdj))
                {
                    // LogService.logIt( "ScoredEssayIntnItem.calculate() AAA.01 testEventId=" + testEventId);
                    //if( essayPromptId <= 0 )
                    //    return;

                    if (essayFacade == null)
                        essayFacade = EssayFacade.getInstance();

                    UnscoredEssay ue = testEventId <= 0 ? null : essayFacade.getUnscoredEssayForMinStatus(testEventId, nodeSeqId, subnodeSeqId, EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());

                    EssayPrompt ep = null;

                    // boolean hasValidTextForDiscern = hasValidTextForDiscern(essayWithoutQuestion);
                    boolean hasValidTextForAi = hasValidTextForAi(essayWithoutQuestion);
                    boolean hasValidTextForSummary = hasValidTextForSummary(essayWithoutQuestion);
                    
                    if( !hasValidTextForSummary && includeSummary )
                    {
                        LogService.logIt( "ScoredEssayIntnItem.calculate() AAA.01b essay text not valid for summary so turnning includeSummary off. testEventId=" + testEventId);
                        includeSummary=false;
                    }
                    
                    UnscoredEssayType unscoredEssayType = getUnscoredEssayType();

                    // Found an unscored essay. Yay.
                    if (ue!=null)
                    {
                        LogService.logIt("ScoredEssayIntnItem.calculate() BBB.0  unscoredEssayId=" + ue.getUnscoredEssayId() + ", hasValidTextForAi=" + hasValidTextForAi + ", testEventId=" + testEventId + ", unscoredEssayId=" + ue.getUnscoredEssayId() + ", ue.getWpm()=" + ue.getWpm());
                        unscoredEssayId = ue.getUnscoredEssayId();

                        // No matter what, copy the summary text.
                        summaryText = ue.getSummary();

                        EssayScoreStatusType scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                        
                        if( isSummaryOnly() && summaryText!=null && !summaryText.isBlank() )
                            scoreStatus = EssayScoreStatusType.SKIPPED_DUMMYPROMPT;

                        if ((unscoredEssayType.getLocalPlagCheckOk() && ue.getSimilarUnscoredEssayId()>0) || (unscoredEssayType.getWebPlagCheckOk() && ue.getDuplicateContentWebPct()>=Constants.WEB_PLAG_CHECK_MAX_MATCH ))
                            plagiarized = 1;

                        hasSpellingGrammarStyle = ue.getHasSpellingGrammarStyle() == 1;
                        
                        wpm = ue.getWpm();
                        highWpm = ue.getHighWpm();

                        transCompareScore = calculateTranslateCompareScore(ue);
                        translatedText = ue.getTranslatedEssay();

                        // Discern Required and Discern turned OFF
                        if ( !AI_SCORING_ON && (scoreStatus.submitted() || scoreStatus.unsubmitted()))
                        {
                            LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.1 Primary scoring method is Turned Off. testEventId=" + testEventId + ", unscoredEssayId=" + ue.getUnscoredEssayId() );

                            ue.setScoreDate(new Date());
                            ue.setScoreStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId());
                            if( includeSummary )
                                ue.setSummaryStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId());
                            essayFacade.saveUnscoredEssay(ue, true);
                            errMsg = "Discern status in progress (incomplete or unsubmitted) but discern is not turned on so marked this as failed.";
                            scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();

                            //confidence = 0;
                            confidence = unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1)  ? 0 : 1;

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && confidence>Constants.MIN_CONFIDENCE_AI : false; // machineScore>0;

                            totalWords = equivWords;

                            //if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //{
                            //    essayMetaScoreMap = ue.getMetaScoreMap();
                            //    confidence2 = ue.getComputedConfidence2();
                            //}
                        }

                        transCompareScore = ue.getTranslateCompareScore();
                        if (transCompareScore>=0)
                            hasValidScore = true;

                        // update ct5ItemId in ue
                        if (ct5ItemId>0 && ue.getCt5ItemId()<= 0)
                        {
                            ue.setCt5ItemId(ct5ItemId);
                            ue.setCt5ItemPartId(ct5ItemPartId);
                            essayFacade.saveUnscoredEssay(ue, false);
                        }

                        // Discern Required and Discern OFF
                        /*
                        if ( (!USE_AI_FOR_PRIMARY && !DiscernUtils.isDiscernOn()) || (USE_AI_FOR_PRIMARY && !AiEssayScoringUtils.getAiEssayScoringOn()) )
                        {
                            ue.setScoreStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId());
                            ue.setScoreDate(new Date());
                            discernFacade.saveUnscoredEssay(ue, true);
                            scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            confidence = 0;
                            totalWords = equivWords;
                            hasValidScore=true;
                            LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.2 Primary scoring is not enabled. testEventId=" + testEventId + ", unscoredEssayId=" + ue.getUnscoredEssayId() );

                            if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            {
                                essayMetaScoreMap = ue.getMetaScoreMap();
                                confidence2 = ue.getComputedConfidence2();
                            }
                        }
                        */
                        
                        if (scoreStatus.skipped())
                        {
                            if (!isSummaryOnly() && ue.getScoreDate() == null)
                                ue.setScoreDate(new Date());
                            
                            if( includeSummary && ue.getSummaryDate()==null )
                                ue.setSummaryDate( new Date() );

                            hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            //confidence = 0;
                            confidence = unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1)  ? 0 : 1;

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && confidence>Constants.MIN_CONFIDENCE_AI : false;
                            
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.3 testEventId=" + testEventId + ". macineScore=" + machineScore );
                            //if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //{
                            //    essayMetaScoreMap = ue.getMetaScoreMap();
                            //    confidence2 = ue.getComputedConfidence2();
                            //}
                            hasValidScore = true;
                        }
                        
                        else if ( (AI_SCORING_ON && (!hasValidTextForAi || scoreStatus.invalidTextForDiscern())))
                        {
                            if( !scoreStatus.invalidTextForDiscern() )
                            {
                                ue.setScoreStatusTypeId(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId());
                                ue.setScoreDate(new Date());
                                essayFacade.saveUnscoredEssay(ue, true);
                            }

                            scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                            hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            // confidence = 0;
                            confidence = unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1)  ? 0 : 1;

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && confidence>Constants.MIN_CONFIDENCE_AI : false;
                            
                            totalWords = equivWords;
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.43 testEventId=" + testEventId + ". macineScore=" + machineScore );
                            //if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //{
                            //    essayMetaScoreMap = ue.getMetaScoreMap();
                            //    confidence2 = ue.getComputedConfidence2();
                            //}
                        }

                        // AI will use the question in place of the dummy prompt, so don't need this for that.
                        else if (usesDummyEssayPrompt() && (!AI_SCORING_ON || !itemLevelAiScoringOk) )
                        {
                            LogService.logIt("ScoredEssayIntnItem.calculate() BBB.44B Setting to Skipped  DummyPrompt because uses DummyPrompt and USE_AI_FOR_PRIMARY-" + AI_SCORING_ON + ", itemLevelAiScoringOk=" + itemLevelAiScoringOk + ", testEventId=" + testEventId + ". machineScore=" + machineScore + ", plagiarized=" + plagiarized + ", ue.getScoreFmErrorRates()=" + ue.getScoreFmErrorRates());
                            ue.setScoreDate(new Date());
                            ue.setScoreStatusTypeId(EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId());
                            ue.setCt5ItemId(ct5ItemId);
                            ue.setCt5ItemPartId(ct5ItemPartId);

                            essayFacade.saveUnscoredEssay(ue, true);

                            scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                            hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            // confidence = 0;
                            confidence = unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1) ? 0 : 1;

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && confidence>Constants.MIN_CONFIDENCE_AI : false;
                            
                            totalWords = equivWords;

                            // if (USE_AI_FOR_SCORE2 && ue.getScoreDate2()!= null && ue.getEssayScoreStatusType2().completed())
                            //{
                            //    essayMetaScoreMap = ue.getMetaScoreMap();
                            //    confidence2 = ue.getComputedConfidence2();
                            //}
                            // LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.44 testEventId=" + testEventId + ". machineScore=" + machineScore + ", plagiarized=" + plagiarized + ", ue.getScoreFmErrorRates()=" + ue.getScoreFmErrorRates());
                        }

                        // if in incomplete status, see if there are new scores present.
                        //if (!AI_SCORING_ON && scoreStatus.submitted())
                        //{
                            // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Checking for new scores unscoredEssayId=" + ue.getUnscoredEssayId() );
                        //    checkDiscernForNewScores(ue);
                        //}

                        if (scoreStatus.unsubmitted() && (AI_SCORING_ON && hasValidTextForAi) )
                        {
                            LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.55 Existing UnscoredEssay was unsubmitted but should be submitted, so submitting. testEventId=" + testEventId + ". unscoredeEssayId=" + unscoredEssayId );
                            ep = essayPromptId>0 && !this.usesDummyEssayPrompt() ? essayFacade.getEssayPrompt(essayPromptId) : null;
                            ue.setEssayPromptId(essayPromptId);
                            ue.setCt5ItemId(ct5ItemId);
                            ue.setCt5ItemPartId(ct5ItemPartId);

                            // submit it to AI if AI is primary and allowed
                            if (isAnyAiScoringOk() && AI_SCORING_ON)
                                submitForAiScoring(ue, false);

                            //else
                            //{
                                // do this first since it will be inline and scores will be available for reporting.
                            //    if (isAnyAiScoringOk() && USE_AI_FOR_SCORE2 && hasValidTextForAi)
                            //        submitForAiScoring(ue, false);

                            //    submitForExternal(ep, ue, essayWithoutQuestion);

                            //}
                            scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                            pendingExternalScores = true;
                        }

                        if(scoreStatus.unsubmitted() )
                        {
                            LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.555 Existing UnscoredEssay is unsubmitted. Possible ERROR. Should have either been marked invalid or submitted at this point. Marking skipped. testEventId=" + testEventId + ", unscoredeEssayId=" + unscoredEssayId );
                            ue.setScoreDate(new Date());
                            if( AI_SCORING_ON && !hasValidTextForAi )
                                ue.setScoreStatusTypeId(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId());
                            else
                                ue.setScoreStatusTypeId(EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId());

                            if( includeSummary && !hasValidTextForSummary )
                                ue.setSummaryStatusTypeId(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId());
                            else if( includeSummary )
                                ue.setSummaryStatusTypeId(EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId());
                            
                            ue.setCt5ItemId(ct5ItemId);
                            ue.setCt5ItemPartId(ct5ItemPartId);
                            essayFacade.saveUnscoredEssay(ue, true);

                            // scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                            hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            // confidence = 0;
                            confidence = unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1) ? 0 : 1;

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && confidence>Constants.MIN_CONFIDENCE_AI : false;
                            totalWords = equivWords;
                            //if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //{
                            //    essayMetaScoreMap = ue.getMetaScoreMap();
                            //    confidence2 = ue.getComputedConfidence2();
                            //}
                        }

                        // submitted but nothing. Need to resubmit if not already done for this cycle. 
                        else if (scoreStatus.submitted() )
                        {
                            pendingExternalScores = true;
                            if( !submittedForAiScoring && AI_SCORING_ON  )
                            {
                                LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.666A Re-submitting because it looks like an issue. testEventId=" + testEventId + ", unscoredeEssayId=" + unscoredEssayId );
                                if (isAnyAiScoringOk() && AI_SCORING_ON)
                                    submitForAiScoring(ue, true);

                                //else
                                //{
                                    // do this first since it will be inline and scores will be available for reporting.
                                //    if (isAnyAiScoringOk() && USE_AI_FOR_SCORE2 && hasValidTextForAi)
                                 //       submitForAiScoring(ue, true);

                                //    submitForExternal(ep, ue, essayWithoutQuestion);

                                //}
                                scoreStatus = EssayScoreStatusType.getValue(ue.getScoreStatusTypeId());
                            }
                            else if( AI_SCORING_ON ) // || USE_AI_FOR_SCORE2)
                                LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.666B not Re-submitting because ScoredEssayInfoItem.submittedForAiScoring is true. testEventId=" + testEventId + ", unscoredeEssayId=" + unscoredEssayId );
                        }

                        else if (scoreStatus.completed())
                        {
                            confidence = ue.getComputedConfidence();
                            machineScore = ue.getComputedScore();

                            // LogService.logIt( "ScoredEssayIntnItem.calculate() reading completed UnscoredEssay. BBB.5555 testEventId=" + testEventId + ". machineScore=" + machineScore + ", confidence=" + confidence );

                            totalWords = ue.getTotalWords();
                            spellErrors = ue.getSpellingErrors();
                            otherErrors = ue.getGrammarErrors() + ue.getStyleErrors();
                            hasSpellingGrammarStyle = ue.getHasSpellingGrammarStyle() == 1;

                            pctDupWords = ue.getPctDuplicateWords();
                            pctDupLongWords = ue.getPctDuplicateLongWords();
                            plagiarized = unscoredEssayType.getLocalPlagCheckOk() && ue.getSimilarUnscoredEssayId()> 0 ? 1 : 0;

                            hasValidScore=true;

                            if (plagiarized<=0 && pctDupWords>MAX_DUPLICATE_WORDS_PCT)
                            {
                                plagiarized = 1;
                                LogService.logIt("ScoredEssayIntnItem(testEventId=" + testEventId + ") XXX Invalidating Essay score due to too many duplicate words. pctDupWords=" + pctDupWords + ", testEventId=" + testEventId);
                            } else if (plagiarized <= 0 && pctDupLongWords > MAX_DUPLICATE_LONGWORDS_PCT)
                            {
                                plagiarized = 1;
                                LogService.logIt("ScoredEssayIntnItem(testEventId=" + testEventId + ") XXX Invalidating Essay score due to too many duplicate LONG words. pctDupLongWords=" + pctDupLongWords + ", testEventId=" + testEventId);
                            }

                            //if (!AI_SCORING_ON && confidence > Constants.MIN_CONFIDENCE_DISCERN)
                            //{
                                //hasValidScore = true;
                            //    hasValidAiComputedScore = machineScore>0;
                            //}

                            //if (!AI_SCORING_ON && confidence <= Constants.MIN_CONFIDENCE_DISCERN)
                            //    errMsg = "Discern Machine Confidence (" + confidence + ") is below minimum (" + Constants.MIN_CONFIDENCE_DISCERN + ").";

                            if (AI_SCORING_ON && confidence > Constants.MIN_CONFIDENCE_AI)
                            {
                                //hasValidScore = true;
                                hasValidAiComputedScore = true; // machineScore>0;
                            }

                            if (AI_SCORING_ON && confidence <= Constants.MIN_CONFIDENCE_AI)
                                errMsg = "AI Machine Confidence (" + confidence + ") is below minimum (" + Constants.MIN_CONFIDENCE_AI + ").";

                            if (AI_SCORING_ON ) // || (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed()))
                                essayMetaScoreMap = ue.getMetaScoreMap();

                            //if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //    confidence2 = ue.getComputedConfidence2();

                        }

                        // Just cause to start over.
                        else if (scoreStatus.cancelled())
                            ue = null;

                        // denote as not scorable.
                        else if (scoreStatus.failed())
                        {
                            // confidence = ue.getComputedConfidence();
                            // machineScore = ue.getComputedScore();
                            // totalWords = equivWords;

                            // hasValidScore=ue.getComputedScore()>0;

                            LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.6 testEventId=" + testEventId + ", unscoredeEssayId=" + unscoredEssayId );
                            hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            // confidence = 0;
                            confidence = unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1) ? 0 : 1;

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && confidence>Constants.MIN_CONFIDENCE_AI : false;
                            totalWords = equivWords;
                            //if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //{
                            //    essayMetaScoreMap = ue.getMetaScoreMap();
                            //    confidence2 = ue.getComputedConfidence2();
                            //}

                            //if( !hasValidScore )
                            errMsg = "Essay scoring process failed.";
                        }

                        else if (scoreStatus.notEnabled() ) // || (!USE_AI_FOR_PRIMARY && !hasValidTextForDiscern) || (USE_AI_FOR_PRIMARY && !hasValidTextForAi) )
                        {
                            //confidence = ue.getComputedConfidence();
                            //machineScore = ue.getComputedScore();
                            //ue.setComputedConfidence(confidence);
                            //ue.setComputedScore(machineScore);
                            //totalWords = equivWords;
                            LogService.logIt( "ScoredEssayIntnItem.calculate() BBB.66 unable to process score Not Enabled. scoreStatus=" + scoreStatus.getName() + ", hasValidTextForAi=" + hasValidTextForAi + ", testEventId=" + testEventId + ", unscoredeEssayId=" + unscoredEssayId );
                            // hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            // confidence = 0;
                            confidence = unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1) ? 0 : 1;

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && confidence>Constants.MIN_CONFIDENCE_AI : false;
                            totalWords = equivWords;
                            //if (USE_AI_FOR_SCORE2 && ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //{
                            //    essayMetaScoreMap = ue.getMetaScoreMap();
                            //    confidence2 = ue.getComputedConfidence2();
                            //}
                        }

                        if (hasValidScore)
                        {
                            // sets error rats
                            recomputeWritingAnalysis(ue);
                        }
                    }

                    // If at this point, ue is null, we need to submit a new request.
                    if (ue==null)
                    {
                        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Submitting new essay." );

                        if (ep==null && essayPromptId>0 && !usesDummyEssayPrompt())
                            ep = essayFacade.getEssayPrompt(essayPromptId);

                        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Submitting new essay BBB - Have Promopt" );
                        ue = new UnscoredEssay();
                        ue.setEssay(essayStr);
                        ue.setLocaleStr(localeStr);
                        if (user != null)
                            ue.setUserId(user.getUserId());
                        ue.setEssayPromptId(essayPromptId);
                        ue.setUnscoredEssayTypeId(unscoredEssayTypeId);
                        ue.setCt5ItemId(ct5ItemId);
                        ue.setCt5ItemPartId(ct5ItemPartId);
                        ue.setNodeSequenceId(nodeSeqId);
                        ue.setScoreStatusTypeId(EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());
                        ue.setSubnodeSequenceId(subnodeSeqId);
                        ue.setTestEventId(testEventId);
                        ue.setCreateDate(new Date());
                        ue.setSecondsToCompose((int) composeTime);

                        int[] vals = null;
                        // LogService.logIt( "ScoredEssayIntnItem.calculate()  Submitting new essay CCC" );

                        Object[] d1 = LocalEssayScoringUtils.getWritingAnalysis(essayStr, getTextLocale(), teIpCountry, getWordsToIgnoreLc(), getUnscoredEssayType().getSupportsSpellingGrammarAnalysis());

                        vals = (int[]) d1[0];
                        misSpells = (Map<String, Integer>) d1[1];
                        // LogService.logIt( "ScoredEssayIntnItem.calculate()  CCC.1 Submitting new essay misSpells.size=" + misSpells.size() );

                        ue.setTotalWords(vals[4]);
                        ue.setSpellingErrors(vals[1]);
                        ue.setGrammarErrors(vals[2]);
                        ue.setStyleErrors(vals[3]);
                        ue.setPctDuplicateWords(vals[5]);
                        ue.setPctDuplicateLongWords(vals[6]);
                        ue.setHasSpellingGrammarStyle(vals[7]);
                        
                        hasSpellingGrammarStyle = ue.getHasSpellingGrammarStyle()==1;
                        totalWords = ue.getTotalWords();
                        spellErrors = ue.getSpellingErrors();
                        otherErrors = ue.getGrammarErrors() + ue.getStyleErrors();
                        pctDupWords = ue.getPctDuplicateWords();
                        pctDupLongWords = ue.getPctDuplicateLongWords();
                        
                        setHighWordsPerMinute(ue);
                        highWpm = ue.getHighWpm();
                        wpm = ue.getWpm();

                        transCompareScore = calculateTranslateCompareScore(ue);
                        if (transCompareScore>=0)
                            hasValidScore = true;

                        // LogService.logIt( "ScoredEssayIntnItem.calculate()  CCC.2Starting Plag Check." );
                        Date procStart = new Date();

                        int maxRowsToCheck = EssayPlagiarismCheckType.getValue(performWebDupContentCheck() ? 1 : 0).getMaxRowsToCheck(getUnscoredEssayType());

                        maxPlagCheckRows = Math.max(maxPlagCheckRows, maxRowsToCheck);

                        boolean useCt5ItemId = ct5ItemId>0 && usesDummyEssayPrompt();
                        // boolean useCt5ItemId = ct5ItemId>0 && ct5ItemPartId>0 && usesDummyEssayPrompt();

                        // no local plag check if transcompare.
                        UnscoredEssay similarUE = !getUnscoredEssayType().getLocalPlagCheckOk() || transCompareScore>=0 || testEventId<=0 ? null : essayFacade.findSimilarEssayForPrompt(testEventId, essayPromptId, ct5ItemId, ct5ItemPartId, essayStr, maxRowsToCheck, useCt5ItemId);

                        Tracker.addResponseTime("Local Plagiarism Test", new Date().getTime() - procStart.getTime());

                        // Used to indicate that the score should be forced (usually to 0)
                        float forceScore = -1;

                        if (transCompareScore<0 && ue.getTotalWords()>0)
                        {
                            float[] errsPerWord = new float[4];

                            // fractErrs[0] = vals==null ? 0 :  ((float) vals[0])/((float) ue.getTotalWords() );
                            errsPerWord[1] = vals==null ? 0 : ((float) vals[1]) / ((float) ue.getTotalWords());
                            errsPerWord[2] = vals==null ? 0 :  ((float) vals[2] + vals[3]) / ((float) ue.getTotalWords());
                            //fractErrs[3] =  vals==null ? 0 : ((float) vals[3])/((float) ue.getTotalWords() );

                            if (getUnscoredEssayType().getSupportsSpellingGrammarAnalysis() &&  errsPerWord[1] > 0.25f || errsPerWord[2] > 0.25f)
                            {
                                LogService.logIt("ScoredEssayIntnItem.calculate()  CCC.2B Invalidating Essay score due to to high error rates. errsPerWord[1]=" + errsPerWord[1] + ", errsPerWord[2]=" + errsPerWord[2] + ", pctDupWords=" + pctDupWords + ", testEventId=" + testEventId);
                                forceScore = 0;
                            }

                            else if (unscoredEssayType.getLocalPlagCheckOk() && pctDupWords>MAX_DUPLICATE_WORDS_PCT)
                            {
                                plagiarized = 1;
                                LogService.logIt("ScoredEssayIntnItem.calculate()  CCC.3 Invalidating Essay score due to too many duplicate words. pctDupWords=" + pctDupWords + ", testEventId=" + testEventId);

                                if( unscoredEssayType.getStopScoringOnPlagarism() )
                                    forceScore = 0;
                            } else if (unscoredEssayType.getLocalPlagCheckOk() && pctDupLongWords>MAX_DUPLICATE_LONGWORDS_PCT)
                            {
                                plagiarized = 1;
                                LogService.logIt("ScoredEssayIntnItem.calculate()  CCC.4 Invalidating Essay score due to too many duplicate LONG words. pctDupLongWords=" + pctDupLongWords + ", testEventId=" + testEventId);
                                if( unscoredEssayType.getStopScoringOnPlagarism() )
                                    forceScore = 0;
                            }
                        }

                        float webDuplicateContentPercent = 0;
                        String webDuplicateContentUrl;
                        // String webDupOut;

                        Object[] webDuplicateContentInfo = null;

                        // do not machine score or check for plag if it's a transcompare or if it's a survey (testEventId<=0)
                        if (testEventId>0 && transCompareScore < 0 && forceScore<0 && performWebDupContentCheck())
                        {
                            // Only look for dup content if there is no plagarism detected and we haven't already forced the score to 0 and essay is in english
                            if (similarUE==null && isTextLocaleOKForWebDupContentCheck(getTextLocale()))
                            {
                                // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") No local dupe content." );
                                procStart = new Date();
                                webDuplicateContentInfo = getWebDuplicateContentInfo(essayWithoutQuestion);
                                Tracker.addResponseTime("External Web Duplicate Content Test", new Date().getTime() - procStart.getTime());
                            } else
                            {
                                if (similarUE!=null)
                                    LogService.logIt("ScoredEssayIntnItem.calculate() CCC.5 Found similar UnscoredEssay testEventId=" + testEventId + ", similarUE=" + similarUE.toString());

                                if (!isTextLocaleOKForWebDupContentCheck(getTextLocale()))
                                    LogService.logIt("ScoredEssayIntnItem.calculate() CCC.6 skipping web dup check because language of test incompatible " + getTextLocale().toString());
                            }
                        }

                        if (webDuplicateContentInfo!=null && webDuplicateContentInfo.length>2)
                        {
                            webDuplicateContentPercent = webDuplicateContentInfo[0] == null ? 0 : (Float) webDuplicateContentInfo[0];
                            webDuplicateContentUrl = (String) webDuplicateContentInfo[1];
                            // webDupOut = (String) webDuplicateContentInfo[2];

                            if (webDuplicateContentPercent > 0)
                            {
                                // LogService.logIt( "ScoredEssayIntItem.webDuplicateContentInfo() CCC.7 webDuplicateContentPercent=" + webDuplicateContentPercent + ", webDuplicateContentUrl=" + webDuplicateContentUrl + ", output=" + webDupOut );
                                ue.setDuplicateContentWebUrl(webDuplicateContentUrl + ", PERCENT MATCH: " + webDuplicateContentPercent);

                            }

                            ue.setDuplicateContentWebPct(webDuplicateContentPercent);
                        }

                        if (unscoredEssayType.getWebPlagCheckOk() && webDuplicateContentPercent >= Constants.WEB_PLAG_CHECK_MAX_MATCH)
                        {
                            plagiarized = 1;
                            if( unscoredEssayType.getStopScoringOnPlagarism() )
                                forceScore = 0;
                            LogService.logIt("ScoredEssayIntnItem.calculate() CCC.7 setting plagiarized=1 doe to web dup content percent=" + webDuplicateContentPercent + ", forceScore=" + 0);
                        }

                        boolean aiScoreOkButDiscernNotOk = false;

                        if (testEventId <= 0)
                        {
                             LogService.logIt("ScoredEssayIntnItem.calculate() CCC.8 setting forceScore=0 due to testEventId=0" );
                             forceScore = 0;
                        }

                        else if (unscoredEssayType.getLocalPlagCheckOk() && similarUE!=null)
                        {
                            ue.setSimilarUnscoredEssayId(similarUE.getUnscoredEssayId());
                            plagiarized = 1;

                            if( unscoredEssayType.getStopScoringOnPlagarism() )
                                forceScore = 0;
                             LogService.logIt("ScoredEssayIntnItem.calculate() CCC.9 setting plagiarized=1 due to similar unscoredEssay. similarUE.unscoredEssayId=" + similarUE.getUnscoredEssayId() +", forceScore=" + forceScore + ", unscoredEssayId=" + ue.getUnscoredEssayId() );
                        } // THIS IS WHRE WE SEE IF the Language is OK for scoring. If not, we just set forceScore to 0 unless it was plagiarized.

                        else if (!AI_SCORING_ON && ( plagiarized!=1 || !unscoredEssayType.getStopScoringOnPlagarism()) && !getEligibleForMachineScoring() && !usesDummyEssayPrompt())
                        {
                            forceScore = 0;
                            aiScoreOkButDiscernNotOk = true; //  plagiarized!=1 || !unscoredEssayType.getStopScoringOnPlagarism(); //  && !usesDummyEssayPrompt();
                        }

                        // Some triggered a forced, non-AI-based score.
                        if (forceScore>-1)
                        {
                            ue.setScoreDate(new Date());
                            ue.setComputedScore(forceScore);
                            ue.setComputedConfidence(1);
                            ue.setScoreStatusTypeId(EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId());

                            hasValidScore = true;
                            machineScore = plagiarized==1 && unscoredEssayType.getStopScoringOnPlagarism() ? 0 : forceScore; // CHG 3/15/2018 ue.getScoreFmErrorRates();

                            if ( unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1) )
                                ue.setComputedConfidence(0);

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && ue.getComputedConfidence()>Constants.MIN_CONFIDENCE_AI : false; // machineScore>0;

                            if (testEventId > 0)
                                essayFacade.saveUnscoredEssay(ue, true);

                            unscoredEssayId = ue.getUnscoredEssayId();
                            LogService.logIt("ScoredEssayIntnItem.calculate() CCC.10 Forcing Score.  forceScore=" + forceScore + ", confidence=" + ue.getComputedConfidence() + ", unscoredEssayId=" + ue.getUnscoredEssayId() );

                        } //Primary Score method NOT availalble.
                        else if (AI_SCORING_ON && !AiEssayScoringUtils.getAiEssayScoringOn())
                        {
                            ue.setScoreDate(new Date());
                            ue.setScoreStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId());
                            if( includeSummary )
                                ue.setSummaryStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId());
                            //hasValidScore = false;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();

                            if ( unscoredEssayType.getStopScoringOnPlagarism() &&  (plagiarized == 1 || ue.getHasSpellingGrammarStyle()!=1) )
                                ue.setComputedConfidence(0);
                            else
                                ue.setComputedConfidence(1);

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && ue.getComputedConfidence()>Constants.MIN_CONFIDENCE_AI : false; // machineScore>0;

                            essayFacade.saveUnscoredEssay(ue, true);
                            unscoredEssayId = ue.getUnscoredEssayId();
                            errMsg = "Discern is currently disabled.";

                            //if (isAnyAiScoringOk() && USE_AI_FOR_SCORE2 && hasValidTextForAi )
                           // {
                            //    submitForAiScoring(ue, false);
                            //    if (ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //    {
                            //        essayMetaScoreMap = ue.getMetaScoreMap();
                            //        confidence2 = ue.getComputedConfidence2();
                            //    }
                            //}
                            hasValidScore=true;

                        } // no valid text for Discern

                        else if ( AI_SCORING_ON && !hasValidTextForAi )
                        {
                            LogService.logIt("ScoredEssayIntnItem.calculate() CCC.10 Text is invalid for AI. USE_AI_FOR_PRIMARY=" + AI_SCORING_ON + ", itemLevelAiScoringOk=" + itemLevelAiScoringOk + ",  testEventId=" + testEventId );
                            ue.setScoreDate(new Date());
                            ue.setScoreStatusTypeId(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId());
                            ue.setComputedScore(0);
                            ue.setComputedConfidence(1);

                            hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            if( unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized == 1 || ue.getHasSpellingGrammarStyle() != 1))
                                ue.setComputedConfidence(0);
                            else
                                ue.setComputedConfidence(1);

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && ue.getComputedConfidence()>Constants.MIN_CONFIDENCE_AI : false; // machineScore>0;

                            essayFacade.saveUnscoredEssay(ue, true);
                            unscoredEssayId = ue.getUnscoredEssayId();

                            //if (isAnyAiScoringOk() && USE_AI_FOR_SCORE2 && hasValidTextForAi )
                            //{
                            //    submitForAiScoring(ue, false);
                            //    if (ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //    {
                            //        essayMetaScoreMap = ue.getMetaScoreMap();
                            //        confidence2 = ue.getComputedConfidence2();
                            //    }
                            //}

                            errMsg = "Invalid text for discern";
                        } // Uses Discern but also uses dummy prompt.

                        else if( usesDummyEssayPrompt() && (!AI_SCORING_ON || !itemLevelAiScoringOk) )
                        {
                            LogService.logIt("ScoredEssayIntnItem.calculate() CCC.8 Setting to SkippedDummyPrompt. USE_AI_FOR_PRIMARY=" + AI_SCORING_ON + ", itemLevelAiScoringOk=" + itemLevelAiScoringOk + ",  testEventId=" + testEventId );
                            ue.setScoreDate(new Date());
                            ue.setScoreStatusTypeId(EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId());
                            ue.setComputedScore(0);
                            ue.setComputedConfidence(1);

                            hasValidScore = true;
                            machineScore = (unscoredEssayType.getStopScoringOnPlagarism() && plagiarized==1) || !unscoredEssayType.getSupportsSpellingGrammarAnalysis() ? 0 : ue.getScoreFmErrorRates();
                            if( unscoredEssayType.getStopScoringOnPlagarism() && (plagiarized==1 || ue.getHasSpellingGrammarStyle()!=1))
                                ue.setComputedConfidence(0);

                            hasValidAiComputedScore = AI_SCORING_ON ? itemLevelAiScoringOk && ue.getComputedConfidence()>Constants.MIN_CONFIDENCE_AI : false; // machineScore>0;

                            essayFacade.saveUnscoredEssay(ue, true);
                            unscoredEssayId = ue.getUnscoredEssayId();
                            errMsg = "Dummy Prompt";

                            //if (isAnyAiScoringOk() && USE_AI_FOR_SCORE2 && hasValidTextForAi)
                            //{
                            //    submitForAiScoring(ue, false);
                            //     if (ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                            //    {
                            //        essayMetaScoreMap = ue.getMetaScoreMap();
                            //        confidence2 = ue.getComputedConfidence2();
                            //    }
                            //}

                        } 
                        // At this point we can send for AI or Discern/AI scoring.
                        else
                        {
                            // LogService.logIt( "ScoredEssayIntnItem.calculate()  Submitting for ext scores." );
                            // submitForExternal(ep, ue, essayWithoutQuestion );

                            if (isAnyAiScoringOk() && AI_SCORING_ON)
                                submitForAiScoring(ue, false);

                            else
                            {
                                //if (isAnyAiScoringOk() && USE_AI_FOR_SCORE2)
                                //{
                                //    submitForAiScoring(ue, false);
                                //    if (ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                                //    {
                                //        essayMetaScoreMap = ue.getMetaScoreMap();
                                //        confidence2 = ue.getComputedConfidence2();
                                //    }
                                //}

                                //submitForExternal(ep, ue, essayWithoutQuestion);
                            }
                            pendingExternalScores = true;

                        }

                        // catch case where valid text but did not submit due to spelling, plagiarism, ...
                        //if (isAnyAiScoringOk() && !AI_SCORING_ON && USE_AI_FOR_SCORE2 && hasValidTextForAi && (forceScore <= -1 || aiScoreOkButDiscernNotOk) && !submittedForAiScoring )
                        //{
                        //    submitForAiScoring(ue, false);
                        //    if (ue.getScoreDate2() != null && ue.getEssayScoreStatusType2().completed())
                        //    {
                        //        essayMetaScoreMap = ue.getMetaScoreMap();
                        //        confidence2 = ue.getComputedConfidence2();
                        //    }
                        //}
                        
                        if( isSummaryOnly() && !submittedForAiScoring )
                        {
                            if( !hasValidTextForSummary )
                            {
                                ue.setSummaryStatusTypeId( EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId() );
                                essayFacade.saveUnscoredEssay(ue, true);
                                hasValidScore = true;
                            }

                            else if( ue.getEssaySummaryStatusType().unsubmitted())
                            {
                                hasValidScore = false;
                                submitForAiScoring(ue, false);
                                pendingExternalScores = true;
                            }
                            
                            else
                                LogService.logIt("ScoredEssayIntnItem.calculate() CCC.9 summaryOnly and not submittesForAiScoring and has valid text for summary but UnscoredEssay not in unsubmitted state. status=" + ue.getEssaySummaryStatusType().getName() + ", testEventId=" + testEventId );
                        }
                    }
                } 
                else
                {
                    // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ") Essay equivalent words (" + equivWords + ") is below minimum words (" + minWords + "). ndSeq=" + nodeSeqId + ", snSeq=" + subnodeSeqId );
                    hasValidScore = false;
                }
            } else
            {
                LogService.logIt("ScoredEssayIntnItem(testEventId=" + testEventId + ") Essay Str is empty. ndSeq=" + nodeSeqId + ", snSeq=" + subnodeSeqId);
                hasValidScore = false;
            }

            // LogService.logIt( "ScoredEssayIntnItem.calculate() Finished, machineScore="+ machineScore + ", equivWords=" + equivWords + ", confidence=" + confidence );
        } catch (Exception e)
        {
            LogService.logIt(e, "ScoredEssayIntnItem.calculate() " + toString());
            EmailUtils.getInstance().sendEmailToAdmin("ScoredEssay Error", "ScoredEssayIntnItem.calculate() " + toString() + ", ERROR: " + e.getMessage());
            hasValidScore = false;
            errMsg = "Exception thrown during processing: ScoredEssayIntnItem.calculate() " + toString();
        }

    }

    private void setSpellWordsToIgnore(String inStr)
    {
        if (inStr == null || inStr.isBlank())
            return;

        if (spellWordsToIgnore == null)
            this.spellWordsToIgnore = new ArrayList<>();
        for (String s : inStr.split(","))
        {
            s = s.trim();
            if (s.isBlank())
                continue;
            if (!spellWordsToIgnore.contains(s.toLowerCase()))
                spellWordsToIgnore.add(s.toLowerCase());
        }
    }

    private void setHighWordsPerMinute(UnscoredEssay ue)
    {
        if (ue == null || ue.getTotalWords() <= 20 || ue.getSecondsToCompose() <= 1)
            return;

        wpm = 60f * ((float) ue.getTotalWords()) / ((float) ue.getSecondsToCompose());

        ue.setWpm(wpm);

        if (wpm >= MAX_WORDS_PER_MIN_PLAG_CHECK)
        {
            highWpm = wpm;
            ue.setHighWpm(wpm);
        }
    }

    private float calculateTranslateCompareScore(UnscoredEssay ue)
    {
        if (ue.getTranslateCompareScore()>=0 || this.transCompare == null || this.transCompare.isBlank() || !transCompare.contains(","))
            return ue.getTranslateCompareScore();

        int idx = transCompare.indexOf(",");
        String compLang;
        String compStr;
        float compVal = -1;
        try
        {
            compLang = transCompare.substring(0, idx).trim();
            if (compLang.isBlank())
                return -1;
            Locale compLoc = I18nUtils.getLocaleFromCompositeStr(compLang);

            compStr = transCompare.substring(idx + 1, transCompare.length()).trim();

            if (compStr.isBlank())
                return -1;

            LanguageUtils lu = new LanguageUtils();
            String tt = lu.getTextTranslation(essayStr, txtLocale, compLoc, true);

            ue.setTranslatedEssay(tt);
            translatedText = tt;

            compVal = TextProcessingUtils.getTextSimilarity(tt, compStr);
            ue.setTranslateCompareScore(compVal);

            if (essayFacade == null)
                essayFacade = EssayFacade.getInstance();
            essayFacade.saveUnscoredEssay(ue, true);

            LogService.logIt("ScoredEssayIntnItem.calculateTranslateCompareScore() transCompare=" + transCompare + ", translated text=" + tt + ", compValScore=" + compVal);
        } catch (Exception e)
        {
            LogService.logIt(e, "ScoredEssayIntnItem.calculateTranslateCompareScore() transCompare=" + transCompare);
        }
        return compVal;
    }

    private String removeQuestionFromEssayStr()
    {
        if (essayStr == null || essayStr.isBlank() || question == null || question.isBlank())
            return essayStr;
        String t = essayStr.toLowerCase();
        String q = question.toLowerCase();

        int idx = t.indexOf(q);
        if (idx<0)
            return essayStr;

        LogService.logIt("ScoredEssayIntnItem(testEventId=" + testEventId + ") Question appears in EssayStr. Removing. ndSeq=" + nodeSeqId + ", snSeq=" + subnodeSeqId);

        List<Integer> il = new ArrayList<>();
        while (idx >= 0)
        {
            il.add(idx);
            idx = t.indexOf(q, idx + q.length());
        }

        StringBuilder sb = new StringBuilder();
        idx = 0;
        for (int x : il)
        {
            if (idx < x)
                sb.append(essayStr.substring(idx, x));
            idx = x + q.length();
        }
        if (idx < essayStr.length())
            sb.append(essayStr.substring(idx, essayStr.length()));

        return sb.toString();
    }

    public Map<Integer, Float> getEssayMetaScoreMap()
    {
        return essayMetaScoreMap;
    }

    private boolean getEligibleForMachineScoring()
    {
        if (transCompareScore >= 0)
            return false;

        if( AI_SCORING_ON==null )
            initVariables();

        if( AI_SCORING_ON )
            return true;

        Locale loc = getTextLocale();

        if (loc == null || loc.getLanguage() == null)
            return true;

        if (loc.getLanguage() != null && loc.getLanguage().equalsIgnoreCase("en"))
            return true;

        return false;
    }

    /*
    private boolean isLocaleValidForMachineScoring()
    {
        Locale l = getTestLocale();

        String lang = l.getLanguage().toLowerCase();

        for( String lg : ScoredEssayIntnItem.VALID_LANGUAGES )
        {
            if( lang.equals( lg ) )
                return true;
        }

        return false;
    }
     */
    private boolean performWebDupContentCheck()
    {
        return getUnscoredEssayType().getWebPlagCheckOk() && (webPlagCheckOk == 1 && getTotalWords() > MIN_WORDS_WEB_PLAG_CHECK) || getTotalWords() > MIN_WORDS_FORCE_WEB_PLAG_CHECK;
    }

    private boolean isTextLocaleOKForWebDupContentCheck(Locale loc)
    {
        if (loc == null)
            return false;

        String lang = loc.getLanguage().toLowerCase();

        if (lang == null || lang.isEmpty())
            return false;

        if (lang.equals("he") || lang.equals("ar") || lang.equals("zh") || lang.equals("jp"))
            return false;

        return true;
    }

    private Locale getTextLocale()
    {
        if (txtLocale != null)
            return txtLocale;

        if (simJ == null || simJ.getLang() == null || simJ.getLang().isEmpty())
            return Locale.US;

        return I18nUtils.getLocaleFromCompositeStr(simJ.getLang());
    }

    /*
    private void submitForExternal(EssayPrompt ep, UnscoredEssay ue, String essayWithoutQuestion) throws Exception
    {
        if (essayFacade == null)
            essayFacade = EssayFacade.getInstance();

        if (ep == null && !usesDummyEssayPrompt())
            ep = essayFacade.getEssayPrompt(ue.getEssayPromptId());

        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() Start prompt =" + ep.toString() );
        if (!DiscernUtils.isDiscernOn())
        {
            ue.setScoreDate(new Date());
            ue.setScoreStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId());
        }

        // save to make sure we have it stored.
        essayFacade.saveUnscoredEssay(ue, true);
        unscoredEssayId = ue.getUnscoredEssayId();

        // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() Saved UnscoredEssay" );
        if (!DiscernUtils.isDiscernOn() || usesDummyEssayPrompt())
            return;

        if (discernUtils == null)
            discernUtils = new DiscernUtils();

        // if not submitted to discern. Need to submit.
        if (ue.getDiscernEssayId() <= 0)
        {
            // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() ue.getDiscernEssayId() <= 0 " );

            int discernEssayId = discernUtils.saveDiscernEssay(ep, "test", essayWithoutQuestion == null ? essayStr : essayWithoutQuestion);

            if (discernEssayId > 0)
                ue.setDiscernEssayId(discernEssayId);

            else
                throw new Exception("Error submitting essay to Discern.");

            // LogService.logIt( "ScoredEssayIntnItem(testEventId=" + testEventId + ").submitForExternal() ZZZ" );
        }

        ue.setScoreStatusTypeId(EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId());
        essayFacade.saveUnscoredEssay(ue, true);
    }
    */

    private boolean submitForAiScoring(UnscoredEssay ue, boolean forceRescore)
    {
        LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() START submittedForAiScoring="+ submittedForAiScoring);
        
        if( submittedForAiScoring && ((AiEssayScoringUtils.getAiEssayScoringUseScore2() && !ue.getEssayScoreStatusType2().unsubmitted()) || (!AiEssayScoringUtils.getAiEssayScoringUseScore2() && !ue.getEssayScoreStatusType().unsubmitted()))  )
        {
            LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() AAA.1A Method called but Already submitted or errored! Returning. ");
            return false;
        }
        
        if( isSummaryOnly() && submittedForAiScoring && !ue.getEssaySummaryStatusType().unsubmitted() )
        {
            LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() AAA.1B Method called but Already submitted or errored! Returning. ");
            return false;
        }
        try
        {
            if (!AiEssayScoringUtils.getAiEssayScoringOn())
            {
                LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() BBB.1 Method called but AiEssayScoring is not on. Returning. ");
                if( AiEssayScoringUtils.getAiEssayScoringUseScore2() )
                    ue.setScoreStatusTypeId2(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId() );

                else
                    ue.setScoreStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId() );
                
                if( includeSummary )
                    ue.setSummaryStatusTypeId(EssayScoreStatusType.FAILED_NOTENABLED.getEssayScoreStatusTypeId() );

                if( essayFacade==null )
                    essayFacade=EssayFacade.getInstance();
                essayFacade.saveUnscoredEssay(ue, false );
                return false;
            }
            
            submittedForAiScoring = true;

            if ( !isSummaryOnly() && (ue.getEssay()==null || ue.getEssay().isBlank() || ue.getEssay().trim().length()<Constants.MIN_TEXT_LENGTH_FOR_AI_SCORING) )
            {
                LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() CCC.1 Returning false. Too short. length=" + (ue.getEssay() == null ? "null" : ue.getEssay().length()));

                if( AiEssayScoringUtils.getAiEssayScoringUseScore2() )
                    ue.setScoreStatusTypeId2(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId() );

                else
                    ue.setScoreStatusTypeId(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId() );
                if( essayFacade==null )
                    essayFacade=EssayFacade.getInstance();
                essayFacade.saveUnscoredEssay(ue, false );

                if( !includeSummary )
                    return false;
            }

            if ( includeSummary && (ue.getEssay()==null || ue.getEssay().isBlank() || ue.getEssay().trim().length()<Constants.MIN_TEXT_LENGTH_FOR_AI_SUMMARY) )
            {
                LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() CCC.1B Too short for summary. length=" + (ue.getEssay() == null ? "null" : ue.getEssay().length()));

                ue.setSummaryStatusTypeId(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId() );
                if( essayFacade==null )
                    essayFacade=EssayFacade.getInstance();
                essayFacade.saveUnscoredEssay(ue, false );

                if( isSummaryOnly() )
                    return false;
            }
            
            
            if (AiEssayScoringUtils.getAiEssayScoringOn())
            {
                // Make sure the appropriate status is set to Submitted.
                if (ue.getUnscoredEssayId() <= 0
                        || (!AiEssayScoringUtils.getAiEssayScoringUseScore2() && ue.getScoreStatusTypeId() != EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId())
                        || (AiEssayScoringUtils.getAiEssayScoringUseScore2() && ue.getScoreStatusTypeId2() != EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId()))
                {
                    if (AiEssayScoringUtils.getAiEssayScoringUseScore2())
                        ue.setScoreStatusTypeId2(EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId());
                    else
                        ue.setScoreStatusTypeId(EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId());

                    if( includeSummary )
                        ue.setSummaryStatusTypeId(EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId());
                    
                    if (essayFacade == null)
                        essayFacade = EssayFacade.getInstance();
                    essayFacade.saveUnscoredEssay(ue, true);
                }
                unscoredEssayId = ue.getUnscoredEssayId();

                LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() CCC.1 unscoredEssayId=" + unscoredEssayId);

                String forcePromptStr = null;
                // Need to use the question as the prompt.
                if (usesDummyEssayPrompt() || essayPromptId<=0)
                {
                    forcePromptStr = question;
                    if (!isSummaryOnly() && (forcePromptStr==null || forcePromptStr.isBlank()))
                    {
                        LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() DDD.1 Cannot call AI because forcePromptStr is empty but usesDummyPrompt()=" + usesDummyEssayPrompt() + ", and essayPromptId=" + essayPromptId + ", unscoredEssayId=" + unscoredEssayId);
                        if( AiEssayScoringUtils.getAiEssayScoringUseScore2() )
                            ue.setScoreStatusTypeId2(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId() );

                        else
                            ue.setScoreStatusTypeId(EssayScoreStatusType.INVALID_TEXT_FOR_DISCERN.getEssayScoreStatusTypeId() );
                        if( essayFacade==null )
                            essayFacade=EssayFacade.getInstance();
                        essayFacade.saveUnscoredEssay(ue, false );
                        
                        if( !includeSummary )
                            return false;
                    }
                }

                AiEssayScoringThread aiest = new AiEssayScoringThread(ue, true, omitGrammarMech, forceRescore, forcePromptStr, idealResponse, aiInstructions, includeSummary ? (isSummaryOnly() ? 2 : 1) : 0);

                // ok to do via Thread (primary).
                if ( !AiEssayScoringUtils.getAiEssayScoringUseScore2())
                {
                    LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() EEE.1 Thread. unscoredEssayId=" + unscoredEssayId);
                    new Thread(aiest).start();
                    return true;
                } // Do inline for Score2. This way, the score 2 values will be available when score1 is ready and can be used in the results.

                // Score 2 should always be inline.
                else
                {
                    LogService.logIt("ScoredEssayIntnItem.submitForAiScoring() EEE.2 Inline. unscoredEssayId=" + unscoredEssayId);
                    return aiest.performEssayScore();
                }
            }
        } catch (Exception e)
        {
            // LogService.logIt(e, "ScoredEssayIntnItem.submitForAiScoring() " + toString());
            LogService.logIt(e, "ScoredEssayIntnItem.submitForAiScoring() XXX.1 " + toString());
            if( AiEssayScoringUtils.getAiEssayScoringUseScore2() )
                ue.setScoreStatusTypeId2(EssayScoreStatusType.FAILED.getEssayScoreStatusTypeId() );

            else
                ue.setScoreStatusTypeId(EssayScoreStatusType.FAILED.getEssayScoreStatusTypeId() );
            
            if( includeSummary )
                ue.setSummaryStatusTypeId( EssayScoreStatusType.FAILED.getEssayScoreStatusTypeId() );
            if( essayFacade==null )
                essayFacade=EssayFacade.getInstance();
            try
            {
                essayFacade.saveUnscoredEssay(ue, false );
            }
            catch( Exception ee )
            {
                LogService.logIt( e, "ScoredEssayIntnItem.submitForAiScoring() YYY.1 Handling exception and setting status. testEventId=" + testEventId + ", unscoredEssayId=" + unscoredEssayId);
            }
        }
        return false;
    }

    /*
    private void checkDiscernForNewScores(UnscoredEssay ue) throws Exception
    {
        if( AI_SCORING_ON )
            return;

        if (usesDummyEssayPrompt())
        {
            ue.setScoreDate(new Date());
            ue.setScoreStatusTypeId(EssayScoreStatusType.SKIPPED_DUMMYPROMPT.getEssayScoreStatusTypeId());
            if (essayFacade == null)
                essayFacade = EssayFacade.getInstance();
            essayFacade.saveUnscoredEssay(ue, true);
            return;
        }

        if (!DiscernUtils.isDiscernOn())
            return;

        if (essayFacade == null)
            essayFacade = EssayFacade.getInstance();

        if (discernUtils == null)
            discernUtils = new DiscernUtils();

        discernUtils.checkForNewScore(ue);

        if (ue.getScoreStatusTypeId()==EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() && ue.getTotalWords() > 0 && ue.getComputedScore() > 0)
        {
            float maxScore = 100;

            //float newScore = ue.getComputedScore();
            //float scoreAdj = 0;
            float[] fractErrs = new float[4];
            int totalErrors = 0;

            hasSpellingGrammarStyle = ue.getHasSpellingGrammarStyle()==1;
            // fractErrs[0] = ((float) vals[0])/((float) ue.getTotalWords() );
            // fractErrs[1] = ((float) ue.getSpellingErrors())/((float) ue.getTotalWords() );

            if (hasSpellingGrammarStyle)
            {
                fractErrs[2] = ((float) ue.getSpellingErrors() + ue.getGrammarErrors() + ue.getStyleErrors()) / ((float) ue.getTotalWords());

                totalErrors = ue.getSpellingErrors() + ue.getGrammarErrors() + ue.getStyleErrors();

                //fractErrs[3] = ((float) vals[3])/((float) ue.getTotalWords() );
                // Fractional Error Counts
                if (fractErrs[2] >= 0.20f)
                    maxScore = Math.min(10, maxScore);

                else if (fractErrs[2] >= 0.15f)
                    maxScore = Math.min(20, maxScore);

                else if (fractErrs[2] >= 0.10f)
                    maxScore = Math.min(40, maxScore);

                //else if( fractErrs[3] >= 0.10f )
                //    maxScore = Math.min( 15, maxScore);
                // Absolute Error Counts
                if (totalErrors > 20)
                    maxScore = Math.min(20, maxScore);

                else if (totalErrors > 15)
                    maxScore = Math.min(40, maxScore);
            }
            //else if( fractErrs[2]< 0.15f )
            //{
            //    scoreAdj = -1*totalErrors;
            //}

            float newScore = Math.min(maxScore, ue.getComputedScore());

            //if( newScore != ue.getComputedScore() )
            //    LogService.logIt( "ScoredEssayIntnItem.checkForNewScores() modified Discern Score of " + ue.getComputedScore() + " to " + newScore + ", total Errors=" + totalErrors + ", Error Fraction=" + fractErrs[2] );
            ue.setComputedScore(newScore);
            //else if( totalErrors > 10 )
            //    maxScore = Math.min( 30, maxScore);

            //else if( totalErrors > 5 )
            //    maxScore = Math.min( 40, maxScore);
        }

        essayFacade.saveUnscoredEssay(ue, true);

        // At this point we don't need to keep the data in the discern database. Because of the way the REST service works, all records are loaded with each new essay, so why bother.
        if (ue.getScoreStatusTypeId() == EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() && ue.getDiscernEssayId() > 0)
        {
            essayFacade.deleteDiscernEssayInfo(ue.getDiscernEssayId());
        }
    }
    */

    private Object[] getWebDuplicateContentInfo(String essayStr)
    {
        try
        {
            if (essayStr == null || essayStr.length() < 50)
                return null;

            Tracker.addWebDuplicateContentCheck();

            // LogService.logIt( "ScoredEssayIntnItem() Performing Web Plag Check. " + this.toString() );
            CopyScapeUtils csu = new CopyScapeUtils();

            return csu.submitEssayForWebDuplicateContentCheck(essayStr, 0);
        } catch (STException e)
        {
            LogService.logIt("ScoredEssayIntnItem() NON-FATAL: " + e.toString() + ", " + essayStr);
            return null;
        } catch (IOException e)
        {
            LogService.logIt("ScoredEssayIntnItem() NON-FATAL: " + e.toString() + ", " + essayStr);
            return null;
        } catch (Exception e)
        {
            LogService.logIt(e, "ScoredEssayIntnItem() " + essayStr);
            return null;
        }
    }

    private void recomputeWritingAnalysis(UnscoredEssay ue)
    {
        try
        {
            //if( !getUnscoredEssayType().getSupportsSpellingGrammarAnalysis() )
            //    return;

            Object[] d1 = LocalEssayScoringUtils.getWritingAnalysis(essayStr, getTextLocale(), teIpCountry, getWordsToIgnoreLc(), getUnscoredEssayType().getSupportsSpellingGrammarAnalysis());

            if (d1 == null || d1[0] == null)
                return;

            int[] vals = (int[]) d1[0];
            this.spellErrors = vals[1];
            this.otherErrors = vals[2] + vals[3];
            this.pctDupWords = vals[5];
            this.pctDupLongWords = vals[6];
            this.hasSpellingGrammarStyle = vals[7] == 1;

            misSpells = (Map<String, Integer>) d1[1];

            if (ue != null)
            {
                ue.setTotalWords(vals[4]);
                ue.setSpellingErrors(vals[1]);
                ue.setGrammarErrors(vals[2]);
                ue.setStyleErrors(vals[3]);
                ue.setPctDuplicateWords(vals[5]);
                ue.setPctDuplicateLongWords(vals[6]);

                ue.setHasSpellingGrammarStyle(vals[7]);

                if (essayFacade == null)
                    essayFacade = EssayFacade.getInstance();
                essayFacade.saveUnscoredEssay(ue, false);
            }
        } catch (Exception e)
        {
            LogService.logIt(e, "ScoredEssayIntnItem.recomputeWritingAnalysis() " + essayStr);
        }

    }

    public boolean getHasSpellingGrammarStyle()
    {
        return hasSpellingGrammarStyle;
    }

    public List<String> getWordsToIgnoreLc()
    {
        List<String> out = new ArrayList<>();

        if (spellWordsToIgnore != null)
        {
            for (String s : spellWordsToIgnore)
            {
                if (!s.isBlank())
                    out.add(s.toLowerCase());
            }
        }

        if (user == null)
            return out;

        if (user.getFirstName() != null && !user.getFirstName().isBlank())
            out.add(user.getFirstName().toLowerCase());

        if (user.getLastName() != null && !user.getLastName().isBlank())
            out.add(user.getLastName().toLowerCase());

        if (user.getEmail() != null && !user.getEmail().isBlank())
            out.add(user.getEmail().toLowerCase());

        return out;
    }

    boolean isSummaryOnly()
    {
        return includeSummary && !itemLevelAiScoringOk;
    }
    
    // We can use AI Scoring IF the item is designated in descriptor to use AI Scoring, or it the essayPromptId is valid and not dummy
    boolean isAnyAiScoringOk()
    {
        return itemLevelAiScoringOk || (essayPromptId>0 && !usesDummyEssayPrompt());
    }

    boolean hasValidTextForAi(String essayWithoutQuestion)
    {
        String s = essayWithoutQuestion!=null && !essayWithoutQuestion.isBlank() ? essayWithoutQuestion : essayStr;

        return s!=null && !s.isBlank() && s.length()>=Constants.MIN_TEXT_LENGTH_FOR_AI_SCORING;
    }
    
    boolean hasValidTextForSummary(String essayWithoutQuestion)
    {
        String s = essayWithoutQuestion!=null && !essayWithoutQuestion.isBlank() ? essayWithoutQuestion : essayStr;

        return s!=null && !s.isBlank() && s.length()>=Constants.MIN_TEXT_LENGTH_FOR_AI_SUMMARY;
    }


    /*
    boolean hasValidTextForDiscern(String essayWithoutQuestion)
    {
        String s = essayWithoutQuestion!=null && !essayWithoutQuestion.isBlank() ? essayWithoutQuestion : essayStr;

        if( s==null || s.length()<Constants.MIN_TEXT_LENGTH_FOR_AI_SCORING )
            return false;

        return DiscernUtils.hasValidTextForDiscern(s);
    }
    */

    @Override
    public String toString()
    {
        return "ScoredEssayIntnItem{" + "machineScore=" + machineScore + ", confidence=" + confidence + ", minWords=" + minWords + ", promptId=" + essayPromptId + ", equivWords=" + equivWords + ", hasValidScore=" + hasValidScore + ", testEventId=" + testEventId + ", nodeSeqId=" + nodeSeqId + ", subnodeSeqId=" + subnodeSeqId + ", pendingExternalScores=" + pendingExternalScores + ", essayStr=" + essayStr + '}';
    }


    public boolean usesDummyEssayPrompt()
    {
        return !getUnscoredEssayType().getIsEssay() || essayPromptId==DUMMY_ESSAY_PROMPT_ID;
    }

    public boolean getHasValidScore()
    {
        return hasValidScore;
    }

    public float getMachineScore()
    {
        return machineScore;
    }

    public float getConfidence()
    {
        return confidence;
    }

    public boolean isPendingExternalScores()
    {
        return pendingExternalScores;
    }

    public int getSpellErrors()
    {
        return spellErrors;
    }

    public int getOtherErrors()
    {
        return otherErrors;
    }

    public int getPlagiarized()
    {
        return plagiarized;
    }

    public int getTotalWords()
    {
        return totalWords;
    }

    public String getErrMsg()
    {
        return errMsg;
    }

    public int getUnscoredEssayId()
    {
        return unscoredEssayId;
    }

    public Map<String, Integer> getMisSpells()
    {
        return misSpells;
    }

    public float getTransCompareScore()
    {
        return transCompareScore;
    }

    public String getTranslatedText()
    {
        return translatedText;
    }

    public int getSubnodeSeqId()
    {
        return subnodeSeqId;
    }

    public int getSpeedError()
    {
        return speedError;
    }

    public void setSpeedError(int speedError)
    {
        this.speedError = speedError;
    }

    public float getWpm()
    {
        return wpm;
    }

    public float getHighWpm()
    {
        return highWpm;
    }

    public float getConfidence2()
    {
        return confidence2;
    }

    public boolean getHasValidSummary()
    {
        return this.summaryText!=null && !this.summaryText.isBlank();
    }
    
    public boolean getHasValidAiComputedScore()
    {
        return hasValidAiComputedScore;
    }

    private UnscoredEssayType getUnscoredEssayType()
    {
        return UnscoredEssayType.getValue(unscoredEssayTypeId);
    }

    public String getSummaryText()
    {
        return summaryText;
    }

    public int getCt5ItemPartId()
    {
        return ct5ItemPartId;
    }

}
