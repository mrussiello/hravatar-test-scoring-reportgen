/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2score.score.item.ScoredEssayIntnItem;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.essay.AiEssayScoringUtils;
import com.tm2score.score.CaveatScoreType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.HtmlUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class ScoredEssayIactnResp extends IactnResp implements ScorableResponse {

    /**
     * ScoreParam3 is the maximum number of points assigned by this item. If set
     * to zero, this value is 100. TextScoreParam1 is the title of the field in
     * the writing-sample section of the report.
     *
     */
    float transCompareScore = -1;
    float completeScore;
    float machineScore;
    float confidence;
    // int equivWords;

    boolean hasValidScore = false;

    List<ScoredEssayIntnItem> items;

    float maxPoints = 100;

    float points = 0;

    boolean pendingExternalScores = false;

    List<CaveatScore> caveatList2;
    // String caveatStr = null;

    // ScoredEssayIntnItem dei;

    int webPlagCheckOk = 0;

    Map<Integer, Map<String, Integer>> misSpellsMap;

    // IactnItemResp iir;
    public ScoredEssayIactnResp(Clicflic.History.Intn iob, TestEvent testEvent)
    {
        super(iob, testEvent);

    }

    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle()
    {
        return false;
    }

    @Override
    public void init(SimJ sj, List<SimletScore> simletScoreList, TestEvent te, boolean validItemsCanHaveZeroMaxPoints) throws Exception
    {
        super.init(sj, simletScoreList, te, validItemsCanHaveZeroMaxPoints);

        if (te != null && te.getOrg() != null)
            webPlagCheckOk = te.getOrg().getWebPlagCheckOk(); // ==1;
    }

    @Override
    public String toString()
    {
        return "ScoredEssayIactnResp{ " + (intnObj == null ? " intn is null" : intnObj.getName() + ", id" + intnObj.getId() + ", nodeSeq=" + intnObj.getSeq()) + (intnResultObj == null ? " intnResultObj is null" : ", sel SubSeq=" + intnResultObj.getSnseq()) + ", ct5ItemId=" + this.getCt5ItemId() + ", ct5ItemPartId=" + this.getCt5ItemPartId() + "}";
    }

    @Override
    public void calculateScore() throws Exception
    {
        if (hasValidScore)
            return;

        // LogService.logIt( "ScoredEssayIactnResp.scoring intn " + intnObj.getSeq() + ", " + intnObj.getUniqueid() );
        if (intnObj.getScoreparam3() > 0)
            maxPoints = intnObj.getScoreparam3();

        items = new ArrayList<>();
        // first, get the scorable strings and their templates

        ScoredEssayIntnItem seii;

        IactnItemResp iir;

        int scrCt = 0;
        float tCompleteScr = 0;
        float tMachScr = 0;
        int tMachScrCt = 0;
        float tConf = 0;
        float tClarity = 0;
        int tClarityCt = 0;
        float tArgument = 0;
        int tArgumentCt = 0;
        float tMechanics = 0;
        int tMechanicsCt = 0;
        float tIdeal = 0;
        int tIdealCt = 0;

        float tTransCompareScr = 0;
        int transCompareScrCt = 0;

        float spellErrors = 0;
        float otherErrors = 0;
        int plagiarized = 0;
        float highWpm = 0;
        float wpm = 0;
        float totalWords = 0;
        //boolean hasSpellingGrammarStyle = false;

        int maxPlagCheckRows = testEvent != null && testEvent.getOrg() != null ? ReportUtils.getReportFlagIntValue("essayscoremaxlookback", null, testEvent.getProduct(), testEvent.getSuborg(), testEvent.getOrg(), testEvent.getReport()) : 0;

        int orderIndex=1;
        for (SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem())
        {
            // Only look at Text Boxes
            if (intItemObj.getFormat()!=G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() ) // || intItemObj.getScoreparam1() <= 0)
                continue;

            // LogService.logIt( "ScoredEssayIactnResp.scoring intn.item AAA.1 " + intnObj.getSeq()+ "-" + intItemObj.getSeq() + ", prompt=" + ((int)intItemObj.getScoreparam1()) );
            // use this only to get the response value.
            iir = IactnRespFactory.getIactnItemResp(this, intItemObj, intnResultObjO, testEvent, orderIndex ); // new IactnItemResp( this, intItemObj, intnResultObj );

            orderIndex++;
            
            String transCompare = null;
            if (intItemObj.getTextscoreparam1() != null && !intItemObj.getTextscoreparam1().isBlank())
                transCompare = StringUtils.getBracketedArtifactFromString(intItemObj.getTextscoreparam1(), Constants.TRANSLATECOMPARE);

            seii = new ScoredEssayIntnItem(simletScore.getTestEvent().getTestEventId(),
                    simletScore.getTestEvent().getUser(),
                    teIpCountry, // teIpCountry
                    simletScore.getTestEvent().getLocaleStr(),
                    simJ,
                    intnObj,
                    intItemObj,
                    (int) intItemObj.getScoreparam1(),
                    intnObj.getCt5Itemid(),
                    intItemObj.getCt5Itempartid(),
                    iir.getRespValue(),
                    getQuestionText(),
                    intItemObj.getFillblankminwords(),
                    intItemObj.getFillblankmaxwords(),
                    intnResultObj.getCtime(),
                    webPlagCheckOk,
                    maxPlagCheckRows,
                    transCompare);

            items.add(seii);

            seii.calculate();

            if (seii.isPendingExternalScores())
                pendingExternalScores = true;

            if (!seii.getHasValidScore())
            {
                LogService.logIt( "ScoredEssayIactnResp.scoring intn.item AAA.1 Essay does not have a valid score. Skipping. pendingExternalScores=" + pendingExternalScores + ", " + seii.toString() );
                continue;
            }

            scrCt++;            
            tCompleteScr = seii.getMachineScore();
            
            if( seii.getHasValidAiComputedScore() )
            {
                tMachScr += seii.getMachineScore();
                tConf += seii.getConfidence();
                tMachScrCt++;
            }
            
            Map<Integer, Float> essayMetaScoreMap = seii.getEssayMetaScoreMap();

            // only use these values if have enough confidence
            if (essayMetaScoreMap != null
                    && AiEssayScoringUtils.getAiEssayScoringOn()
                    && ((AiEssayScoringUtils.getAiEssayScoringUseScore2() && seii.getConfidence2()>Constants.MIN_CONFIDENCE_AI)
                    || (!AiEssayScoringUtils.getAiEssayScoringUseScore2() && seii.getConfidence()>Constants.MIN_CONFIDENCE_AI)))
            {
                if (essayMetaScoreMap.containsKey(CaveatScoreType.CLARITY.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId())>0)
                {
                    tClarity += essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId());
                    tClarityCt++;
                }
                if (essayMetaScoreMap.containsKey(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId())>0)
                {
                    tArgument += essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId());
                    tArgumentCt++;
                }
                if (essayMetaScoreMap.containsKey(CaveatScoreType.MECHANICS.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId())>0 )
                {
                    tMechanics += essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId());
                    tMechanicsCt++;
                }
                if (essayMetaScoreMap.containsKey(CaveatScoreType.IDEAL.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId())>0 )
                {
                    tIdeal += essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId());
                    tIdealCt++;
                }
            }

            if (seii.getTransCompareScore()>=0)
            {
                tTransCompareScr += seii.getTransCompareScore();
                transCompareScrCt++;
            }

            totalWords += seii.getTotalWords();
            plagiarized = seii.getPlagiarized() == 1 ? 1 : plagiarized;
            highWpm = Math.max(highWpm, seii.getHighWpm());
            wpm += seii.getTotalWords() * seii.getWpm();

            if (seii.getHasSpellingGrammarStyle())
            {
                //hasSpellingGrammarStyle = true;
                spellErrors += seii.getSpellErrors();
                otherErrors += seii.getOtherErrors();
            }

            if (seii.getMisSpells() != null && !seii.getMisSpells().isEmpty())
            {
                if (misSpellsMap == null)
                    misSpellsMap = new HashMap<>();

                misSpellsMap.put(intItemObj.getSeq(), seii.getMisSpells());
            }
        }

        if (!pendingExternalScores && scrCt>0)
        {
            LogService.logIt( "ScoredEssayIactnResp.calculateScore() BBB.1A " + intnObj.getSeq() + ", scrCt=" + scrCt + ", completeScore=" + completeScore + ", tMachScr=" + tMachScr + ", tConf=" + tConf + ", totalWords=" + totalWords );
            completeScore = tCompleteScr/((float) scrCt);          
            // LogService.logIt( "ScoredEssayIactnResp.calculateScore() BBB.1B " + intnObj.getSeq() + ", scrCt=" + scrCt + ", completeScore=" + completeScore + ", tMachScr=" + tMachScr + ", tConf=" + tConf + ", totalWords=" + totalWords );
            
            machineScore = tMachScrCt>0 ? tMachScr/((float) tMachScrCt) : 0;
            confidence = tMachScrCt>0 ? tConf/((float) tMachScrCt) : 0;
            hasValidScore = true;

            if (transCompareScrCt > 0)
                transCompareScore = tTransCompareScr / ((float) transCompareScrCt);

            points = transCompareScore>=0 && transCompareScrCt>0 ? maxPoints*transCompareScore : maxPoints*completeScore/100;

            LogService.logIt( "ScoredEssayIactnResp.calculateScore() BBB.1C points=" + points + ", maxPoints=" + maxPoints + ", seq=" + intnObj.getSeq() + ", scrCt=" + scrCt + ", completeScore=" + completeScore + ", tMachScr=" + tMachScr + ", tConf=" + tConf + ", totalWords=" + totalWords );
            
            if (tClarityCt>1)
                tClarity = tClarity / ((float) tClarityCt);

            if (tArgumentCt>1)
                tArgument = tArgument / ((float) tArgumentCt);

            if (tMechanicsCt>1)
                tMechanics = tMechanics / ((float) tMechanicsCt);

            if (tIdealCt>1)
                tIdeal = tIdeal / ((float) tIdealCt);

            
            metaScores = new float[16];

            // spell Errors and other Errors are represented here as errors per 100 words.
            if (totalWords>0)
            {
                spellErrors =100 * spellErrors/totalWords;
                otherErrors =100 * otherErrors/totalWords;
            }

            wpm = totalWords>0 ? wpm / ((float) totalWords) : 0;

            metaScores[2] = machineScore;
            metaScores[3] = confidence;
            metaScores[4] = intnObj.getCt5Int13()==1 ? 0 : spellErrors;
            metaScores[5] = intnObj.getCt5Int13()==1 ? 0 : otherErrors;
            metaScores[6] = intnObj.getCt5Int13()==1 ? 0 : totalWords;
            metaScores[7] = intnObj.getCt5Int25()==1 || intnObj.getCt5Int25()==2 ? 1 : 0; // 1=indicates if AI Scoring, 2=ai and summary, 3=summary only
            metaScores[8] = plagiarized == 1 ? 1 : 0;
            metaScores[9] = transCompareScore;
            metaScores[10] = intnObj.getCt5Int13()==1 ? 0 : wpm;
            metaScores[11] = intnObj.getCt5Int13()==1 ? 0 : highWpm;

            metaScores[12] = tClarity;
            metaScores[13] = tArgument;
            metaScores[14] = intnObj.getCt5Int13()==1 ? 0 : tMechanics;
            metaScores[15] = tIdeal;

            caveatList2=new ArrayList<>();
            // LogService.logIt( "ScoredEssayIactnResp.calculateScore() BBB.2 scrCt=" + scrCt + ", spellErrors=" + spellErrors + ", otherErrors=" + otherErrors + ", machineScore=" + machineScore + ", totalWords=" + totalWords + ", transCompareScore=" + transCompareScore + ", points=" + points + ", wpm=" + wpm );
            if (plagiarized == 1)
            {
                // forcedRiskFactorsList = new ArrayList<>();
                // forcedRiskFactorsList.add( );        
                caveatList2.add( new CaveatScore( caveatList2.size()+1, CaveatScoreType.PLAGIARIZED.getCaveatScoreTypeId(), 1, 0, null, this.getSimLocale()));
                // caveatStr = "[" + Constants.ESSAY_PLAGIARIZED + "]";
            }

            if (intnObj.getCt5Int13()<=0 && wpm>0)
            {
                caveatList2.add( new CaveatScore( caveatList2.size()+1, CaveatScoreType.WPM.getCaveatScoreTypeId(), wpm, 0, null, this.getSimLocale()));

                // already there.
                //if (caveatStr != null && caveatStr.contains("[" + Constants.ESSAY_WPM + "]"))
                //{
                //} else if (caveatStr != null && !caveatStr.isBlank())
                //    caveatStr += "[" + Constants.ESSAY_WPM + "]" + wpm;

                //else
                //    caveatStr = "[" + Constants.ESSAY_WPM + "]" + wpm;
            }

            if (intnObj.getCt5Int13()<=0 && highWpm > 0)
            {
                caveatList2.add( new CaveatScore( caveatList2.size()+1, CaveatScoreType.WPM_HI.getCaveatScoreTypeId(), highWpm, 0, null, getSimLocale()));
                
                // already there.
                //if (caveatStr != null && caveatStr.contains("[" + Constants.ESSAY_HIGH_WPM + "]"))
                //{
                //} else if (caveatStr != null && !caveatStr.isBlank())
                //    caveatStr += "[" + Constants.ESSAY_HIGH_WPM + "]" + highWpm;

                //else
                //    caveatStr = "[" + Constants.ESSAY_HIGH_WPM + "]" + highWpm;

            }

            // LogService.logIt( "ScoredEssayIactnResp.calculateScore() BBB.3 final machineScore for item. total words=" + totalWords + ", machine score=" + machineScore + ", final confidence=" + confidence + ", final points=" + points );
        }
    }

    public long getSimCompetencyId()
    {
        if (simletCompetencyScore == null)
            return 0;

        return this.simletCompetencyScore.competencyScoreObj.getSimcompetencyid();
    }

    public String getTranslatedText(int snseq)
    {
        // LogService.logIt( "ScoredEssayIactnResp.getTranslatedText() snseq=" + snseq );
        if (items == null || items.isEmpty())
            return null;

        for (ScoredEssayIntnItem seii : items)
        {
            // LogService.logIt( "ScoredEssayIactnResp.getTranslatedText() HAve SEII.snseq=" + seii.getSubnodeSeqId() + ", Seeking snseq=" + snseq + ", transtext=" + seii.getTranslatedText() );
            if (seii.getSubnodeSeqId() == snseq)
            {
                return seii.getTranslatedText();
            }
        }
        return null;
    }

    public String getEssayTextAll()
    {
        StringBuilder sb = new StringBuilder();
        for( String s : getEssayText() )
        {
            if( sb.length()>0 )
                sb.append("\n\n" );
            sb.append( s );
        }
        return sb.toString();
    }
    
    public List<String> getEssayText()
    {
        List<String> out = new ArrayList<>();
        String text = null;

        // look for an interaction item designated as the question.
        for (IactnItemResp iir : getEssayIntItemList())
        {
            text = iir.getRespValue();
            if (text != null && !text.isEmpty())
                out.add(text);
        }

        return out;
    }

    public List<IactnItemResp> getEssayIntItemList()
    {
        List<IactnItemResp> out = new ArrayList<>();
        IactnItemResp iir;

        int orderIndex=1;
        
        // look for an interaction item designated as the question.
        for (SimJ.Intn.Intnitem iitm : intnObj.getIntnitem())
        {
            if (iitm.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() ) // && iitm.getScoreparam1() > 0)
            {
                iir = IactnRespFactory.getIactnItemResp(this, iitm, intnResultObjO, testEvent, orderIndex); // new IactnItemResp( this, iitm, intnResultObj );
                out.add(iir);
                orderIndex++;
            }
        }

        return out;
    }

    private String getQuestionText()
    {
        String q = null;
        // look for an interaction item designated as the question.
        for (SimJ.Intn.Intnitem iitm : intnObj.getIntnitem())
        {
            if (iitm.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() ) //&& iitm.getScoreparam1() > 0)
            {
                if (iitm.getQuestionid()!= null && !iitm.getQuestionid().isBlank())
                {
                    for (SimJ.Intn.Intnitem iitm2 : intnObj.getIntnitem())
                    {
                        if (iitm2.getId() != null && iitm2.getId().equals(iitm.getQuestionid()))
                        {
                            q = UrlEncodingUtils.decodeKeepPlus(iitm2.getContent());
                            q = HtmlUtils.removeAllHtmlTags(q);
                            break;
                        }
                    }
                }
            }
        }

        // Old IMOs may have a question designated ising the isquestionstem parameter.
        for (SimJ.Intn.Intnitem iitm : intnObj.getIntnitem())
        {
            if (iitm.getFormat() == G2ChoiceFormatType.TEXT.getG2ChoiceFormatTypeId() && iitm.getIsquestionstem() == 1)
            {
                q = UrlEncodingUtils.decodeKeepPlus(iitm.getContent());
                q = HtmlUtils.removeAllHtmlTags(q);
                break;
            }
        }

        return q;
    }

    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        List<TextAndTitle> out = new ArrayList<>();

        // LogService.logIt( "IactnResp.getTestRespList() starting. " + intnObj.getName() + " getSimletItemType().supportsManualScoringViaReport()=" + getSimletItemType().supportsManualScoringViaReport() + ", isNonComp=" + isNonComp );
        // Store question item here.
        //SimJ.Intn.Intnitem q = null;
        String title;
        String text;
        String transtext;
        String summary;
        IactnItemResp iir;
        // TextAndTitle ttl;
        String misSpellsStr;
        Map<String, Integer> msps;

        String idt = getTextAndTitleIdentifier();

        ScoredEssayIntnItem seii;
        
        int orderIndex = 1;
        
        // look for an interaction item designated as the question.
        for (SimJ.Intn.Intnitem iitm : intnObj.getIntnitem())
        {
            if (iitm.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() ) // && iitm.getScoreparam1() > 0)
            {
                iir = IactnRespFactory.getIactnItemResp(this, iitm, intnResultObjO, testEvent, orderIndex ); // new IactnItemResp( this, iitm, intnResultObj );

                orderIndex++;
                
                text = iir.getRespValue();

                if (text == null || text.isEmpty())
                    continue;

                seii = getScoredEssayIntnItem( iitm );
                
                summary = seii!=null ? seii.getSummaryText() : null;
                
                transtext = getTranslatedText(iitm.getSeq());
                
                // title = XMLUtils.decodeURIComponent( iitm.getTitle());
                // title = StringUtils.getBracketedArtifactFromString(Constants.TRANSLATECOMPARE, iitm.getTextscoreparam1() );
                // if( title!=null && !title.isBlank() || (iitm.getTextscoreparam1().contains("[") || iitm.getTextscoreparam1().contains("]")) )
                if ( iitm.getTextscoreparam1()!=null && (iitm.getTextscoreparam1().contains("[") || iitm.getTextscoreparam1().contains("]")))
                    title = StringUtils.getBracketedArtifactFromString(Constants.ESSAYTITLE, iitm.getTextscoreparam1());
                else if( iitm.getTextscoreparam1()!=null )
                    title = UrlEncodingUtils.decodeKeepPlus(iitm.getTextscoreparam1());
                else
                    title = null;

                if ((title==null || title.isEmpty()) && iitm.getQuestionid() != null && !iitm.getQuestionid().isBlank())
                {
                    for (SimJ.Intn.Intnitem iitm2 : intnObj.getIntnitem())
                    {
                        if (iitm2.getId() != null && iitm2.getId().equals(iitm.getQuestionid()))
                        {
                            title = UrlEncodingUtils.decodeKeepPlus(iitm2.getContent());
                            // LogService.logIt( "ScoredEssayIactnResp.getTestRespList() title FROM QUESTION=" + title );

                            //title = StringUtils.removeTag( title , "u" );
                            //title = StringUtils.removeTag( title , "i" );
                            //title = StringUtils.removeTag( title , "b" );
                            break;
                        }
                    }
                }

                if (title==null || title.isEmpty())
                    title = UrlEncodingUtils.decodeKeepPlus(iitm.getContent());

                if (title==null)
                    title = "";

                msps = misSpellsMap == null ? null : misSpellsMap.get((int) (iitm.getSeq()));
                misSpellsStr = msps == null ? null : getMisSpellsStr(msps);

                String itemid = iitm.getExtitempartid();
                if (itemid == null || itemid.isBlank())
                    itemid = Integer.toString(iitm.getSeq());

                //if( seii!=null && seii.getHasValidSummary() )
                //     out.add(new TextAndTitle(seii.getSummaryText(), Constants.AI_SUMMARY_TEXTTITLE_KEY, false, getSimCompetencyId(), testEvent==null ? this.getCt5ItemId() : testEvent.getNextTextTitleSequenceId(), null, idt + "-summary-" + "-" + itemid, null));                    
                
                // LogService.logIt( "ScoredEssayIactnResp.getTestRespList() title=" + title );
                out.add(new TextAndTitle(text, title, false, getSimCompetencyId(), intnResultObjO.getSq()*100 + iir.orderIndex, misSpellsStr, idt + "-" + itemid, transtext, summary ));            
                
            
            }
        }        

        return out;
    }
    
    private ScoredEssayIntnItem getScoredEssayIntnItem( SimJ.Intn.Intnitem iitm )
    {
        if( this.items==null )
        {
            LogService.logIt( "ScoredEssayIactnResp.getScoredEssayIntnItem() items list is null or empty. ct5ItemPartId=" + iitm.getCt5Itempartid() );
            return null;
        }
        for( ScoredEssayIntnItem seii : items )
        {
            if( seii.getSubnodeSeqId()==iitm.getSeq() || seii.getCt5ItemPartId()==iitm.getCt5Itempartid())
                return seii;
        }
        return null;
    }

    private String getMisSpellsStr(Map<String, Integer> vals)
    {
        if (vals == null || vals.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        Integer ct;

        for (String k : vals.keySet())
        {
            ct = vals.get(k);

            if (ct == null || ct <= 0)
                continue;

            if (sb.length() > 0)
                sb.append(", ");

            sb.append(k + " (" + ct + ")");
        }

        return sb.toString();
    }

    @Override
    public String getExtItemId()
    {
        for (SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem())
        {
            //if( intItemObj.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            //    LogService.logIt( "DataEntryIactnResp.calculateScore() HAVE INT ITEM Content=" + intItemObj.getContent() +", TextScoreParam1=" + intItemObj.getTextscoreparam1() );

            if (intItemObj.getFormat() != G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() || intItemObj.getScoreparam1() <= 0)
                continue;

            if (intItemObj.getExtitemid() != null && !intItemObj.getExtitemid().isEmpty())
                return intItemObj.getExtitemid();
        }

        return null;
    }

    @Override
    public String getSelectedExtPartItemIds()
    {
        return null;
    }

    @Override
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId)
    {
        IncludeItemScoresType iist = IncludeItemScoresType.getValue(includeItemScoreTypeId);

        if (iist.isNone())
            return null;
        
        // These are picked up by the Intn Item responses if they are present.
        if( this.getAllScorableIntItemResponses() !=null && !getAllScorableIntItemResponses().isEmpty() )
            return null;

        String itemLevelId = getTextAndTitleIdentifier(); // UrlEncodingUtils.decodeKeepPlus( getExtItemId() );
        String title = itemLevelId;
        String ques = this.getQuestionText();
        if (ques!=null)
            ques = StringUtils.truncateStringWithTrailer(ques, 512, true);

        if (ques != null)
            title += "\n" + ques;

        String text = null;
        
        if (iist.isIncludeNumericScore() || iist.isIncludeCorrect())
        {
            text = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1); //  Float.toString( itemScore() );
            // text = Float.toString( itemScore() );
        } else if (iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore(itemScore());

        else if (iist.isResponseOrResponseCorrect())
        {
            text = getEssayTextAll();
            if (text!=null)
                text = StringUtils.truncateStringWithTrailer(text, 2048, true);
            // LogService.logIt( "ScoredEssayIactnResp.getItemScoreTextTitle() DDD.1 unique=" + this.intnObj.getUniqueid() + ", simletCompetencyScore=" + (simletCompetencyScore==null ? "null" : "present, class=" + simletCompetencyScore.getSimletCompetencyClass().getName()) );

            String scoreText;
            if (simletCompetencyScore!=null && simletCompetencyScore.getSimletCompetencyClass().isUnscored())
                scoreText = null;  // return;
            
            else
                scoreText = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1); //  Float.toString( itemScore() );
            // text = Float.toString( itemScore() );

            if( scoreText!=null && !scoreText.isBlank() )
            {
                if( text==null )
                    text="";
                text += " (" + scoreText + ")";
            }
        
        }

        if (text==null || text.isEmpty())
            return null;

        text = StringUtils.replaceStr(text, "[", "{");
        title = StringUtils.replaceStr(title, "[", "{");
        itemLevelId = StringUtils.replaceStr(itemLevelId, "[", "{");
        ques = StringUtils.replaceStr(ques, "[", "{");
        TextAndTitle tt = new TextAndTitle(text, title, intnResultObjO.getSq()*100, itemLevelId, ques);
        tt.setOrder( this.intnResultObjO.getSq()*100 );
        return tt;
    }

    /*
    private void addMisSpellsVals( Map<String,Integer> vals1 )
    {
        if( this.misSpells==null )
            this.misSpells = new TreeMap<>();

        if( vals1==null || vals1.isEmpty() )
            return;

        Integer count1,count2;

        int count;

        for( String k : vals1.keySet() )
        {
            count1 = vals1.get(k);

            count2 = misSpells.get(k);

            count = (count1==null ? 1 : count1) + (count2==null ? 0 : count2);

            misSpells.put( k , count );
        }
    }
     */
    @Override
    public boolean isPendingExternalScore()
    {
        return pendingExternalScores;
    }

    @Override
    public boolean hasCorrectInteractionItems()
    {
        return false;
    }

    @Override
    public boolean hasInteractionItemScores()
    {
        return false;
    }

    @Override
    public float itemScore()
    {
        return points;
    }

    @Override
    public float getFloor()
    {
        return 0;
    }

    @Override
    public float getCeiling()
    {
        return 0;
    }

    /*
    @Override
    public String getCaveatText()
    {
        return caveatStr;
    }
    */
    
    @Override
    public List<CaveatScore> getCaveatScoreList()
    {
        if( caveatList2==null )
            caveatList2 = new ArrayList<>();
        
        return caveatList2;
    }
    

    @Override
    public InterviewQuestion getScoreTextInterviewQuestion()
    {
        return null;
    }

    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return false;
    }

    @Override
    public boolean saveAsItemResponse()
    {
        return false;
    }

    @Override
    public float getAggregateItemScore(SimCompetencyClass simCompetencyClass)
    {
        return 0;
    }

    @Override
    public synchronized float[] getMaxPointsArray()
    {
        maxPointsArray = new float[]
        {
            maxPoints, 0, 0, 0
        };

        return maxPointsArray;
    }

    @Override
    public boolean hasValidScore()
    {
        return hasValidScore;
    }

}
