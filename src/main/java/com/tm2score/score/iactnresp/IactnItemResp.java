/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.event.ResponseLevelType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.global.Constants;
import com.tm2score.service.LogService;
import com.tm2score.sim.InteractionScoreUtils;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.SimletItemType;
import com.tm2score.util.StringUtils;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.sim.FillBlankType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2builder.sim.xml.SimJ.Intn.Intnitem;
import com.tm2score.ct5.Ct5ItemType;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.essay.AiEssayScoringThread;
import com.tm2score.essay.AiEssayScoringUtils;
import com.tm2score.essay.EssayFacade;
import com.tm2score.essay.EssayScoreStatusType;
import com.tm2score.essay.UnscoredEssayType;
import com.tm2score.file.UploadedFileHelpUtils;
import com.tm2score.file.UploadedUserFileType;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.NumberUtils;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.CaveatScoreType;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.item.ScoredEssayIntnItem;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.IntnHist;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 *
 * @author Mike
 */
public class IactnItemResp implements ScorableResponse {
    // This is the competencyScore object for this item, if any.

    SimletCompetencyScore simletCompetencyScore = null;

    Clicflic.History.Intn intnResultObjO = null;
    IntnHist intnResultObj = null;
    TestEvent testEvent;

    IactnResp iactnResp = null;

    public SimJ.Intn.Intnitem intnItemObj = null;

    String respValue = null;

    G2ChoiceFormatType g2ChoiceFormatType = null;

    long upldUsrFileId = 0;

    float[] maxPointsArray;

    List<String> forcedRiskFactorsList;

    boolean validItemsCanHaveZeroMaxPoints = false;

    // Object[0] = String value to match
    // Object[1] = Points to assign
    List<Object[]> textPointsLst = null;

    // This is the index for the textPointsLst that matched the inputted response.
    int textPointsMatchedIndex = -1;

    String summaryText;
    boolean hasPendingScore;
    boolean calcsComplete;
    int orderIndex = 0;

    public IactnItemResp(IactnResp ir, SimJ.Intn.Intnitem ii, Clicflic.History.Intn iro, TestEvent testEvent, int orderIndex)
    {
        this.iactnResp = ir;
        this.intnItemObj = ii;
        this.intnResultObjO = iro;
        this.intnResultObj = iro == null ? null : new IntnHist(iro);
        this.testEvent = testEvent;
        this.orderIndex=orderIndex;

        this.validItemsCanHaveZeroMaxPoints = ir.getValidItemsCanHaveZeroMaxPoints();

        if (intnItemObj.getTextscoreparam1() != null && !intnItemObj.getTextscoreparam1().isEmpty())
        {
            try
            {
                intnItemObj.setTextscoreparam1(UrlEncodingUtils.decodeKeepPlus(intnItemObj.getTextscoreparam1())); //  URLDecoder.decode(intnItemObj.getTextscoreparam1(), "UTF8") );
            } catch (Exception e)
            {
                LogService.logIt(e, "IactnItemResp() decoding textScoreParam1()seq=" + ii.getSeq() + ", content" + ii.getContent() + ", textScoreParam1=" + ii.getTextscoreparam1());
            }
        }

        if (intnItemObj.getText1() != null && !intnItemObj.getText1().isEmpty())
        {
            try
            {
                intnItemObj.setText1(UrlEncodingUtils.decodeKeepPlus(intnItemObj.getText1()));
            } catch (Exception e)
            {
                LogService.logIt(e, "IactnItemResp() decoding Text1() seq=" + ii.getSeq() + ", content" + ii.getContent() + ", text1=" + ii.getText1());
            }
        }
    }

    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle()
    {
        return false;
    }

    @Override
    public int getCt5ItemId()
    {
        return iactnResp == null ? 0 : iactnResp.getCt5ItemId();
    }

    @Override
    public int getCt5ItemPartId()
    {
        return this.intnItemObj == null ? 0 : this.intnItemObj.getCt5Itempartid();
    }

    @Override
    public String getTopic()
    {
        if (intnItemObj == null)
            return null;

        String tsp = intnItemObj.getTextscoreparam1();

        if (tsp == null || tsp.trim().isEmpty())
            return null;

        return IvrStringUtils.getTagValue(tsp, Constants.TOPIC_KEY);
    }

    @Override
    public Map<String, int[]> getTopicMap()
    {
        return ScoreUtils.getSingleTopicTopicMap(getTopic(), correct(), getPartialCreditAssigned());
    }

    @Override
    public float getDisplayOrder()
    {
        String s = Math.round(iactnResp.getDisplayOrder()) + ".00" + this.intnItemObj.getSeq();
        return Float.parseFloat(s);
    }

    @Override
    public List<MergableScoreObject> getMergableScoreObjects()
    {
        return new ArrayList<>();
    }

    public void init(SimletScore ss, TestEvent te)
    {
        try
        {
            g2ChoiceFormatType = G2ChoiceFormatType.getValue(intnItemObj.getFormat());

            if (intnItemObj.getCompetencyscoreid()>0 && 
                (g2ChoiceFormatType.supportsSubnodeLevelSimletAutoScoring() || 
                (isDragTarget() && intnItemObj.getDrgtgtcheckbox()==1)) || 
                 g2ChoiceFormatType.getIsFileUpload() && iactnResp.intnObj.getCt5Int25()==3 )
                simletCompetencyScore = ss.getSimletCompetencyScore(intnItemObj.getCompetencyscoreid());

            if (getIsAnyUploadedFile() && upldUsrFileId<=0 )
            {
                FileUploadFacade fuf = FileUploadFacade.getInstance();

                UploadedUserFile uuf = fuf.getUploadedUserFile(te.getTestEventId(), null, iactnResp.intnObj.getSeq(), intnItemObj.getSeq(), UploadedUserFileType.RESPONSE.getUploadedUserFileTypeId(), false);
                // UploadedUserFile uuf = fuf.getUploadedUserFile(te.getTestEventId(), iactnResp.intnObj.getUniqueid(), iactnResp.intnObj.getSeq(), intnItemObj.getSeq(), false);

                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "IactnItemResp.init() getting UploadedUserFile() for testEventId=" + te.getTestEventId() + ", nodeSeq=" + iactnResp.intnObj.getSeq() + ", subnodeSeq=" +intnItemObj.getSeq()  + " uploadedUserFile=" + (uuf==null ? "Null" : uuf.toString() ) );
                
                if (uuf != null)
                    upldUsrFileId = uuf.getUploadedUserFileId();

                // LogService.logIt( "IactnItemResp.init() getting UploadedUserFile() " + (uuf==null ? "Null" : uuf.toString() ) );
            }

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "IactnItemResp.init() " + toString() );

        } catch (Exception e)
        {
            LogService.logIt(e, "IactnItemResp.init() " + toString());
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.intnItemObj);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final IactnItemResp other = (IactnItemResp) obj;

        // requires same node seg and seq
        if (this.intnItemObj == null || other.intnItemObj == null
                || this.intnItemObj.getSeq() != other.intnItemObj.getSeq()
                || this.iactnResp.intnObj.getSeq() != other.iactnResp.intnObj.getSeq())
            return false;

        return true;
    }

    @Override
    public void calculateScore() throws Exception
    {
        // LogService.logIt( "IactnItemResp.calculateScore() " + toString() );
        
        if( calcsComplete )
            return;
        
        if (needsAiSummaryCalc())
            doAiTextSummary();
        
        calcsComplete = true;

        //
    }

    private void doAiTextSummary() throws Exception
    {
        if (summaryText!= null && !summaryText.isBlank())
            return;

        try
        {
            String textStr;
            
            if( getIsEssay())
                textStr = getRespValue();
            
            else if( getIsFileUploadButton() )
            {
                UploadedFileHelpUtils uploadedFileHelpUtils = new UploadedFileHelpUtils();
                UploadedUserFile uuf = uploadedFileHelpUtils.getUploadedUserFile(upldUsrFileId);
                if( uuf==null )
                {
                    LogService.logIt( "IactnItemResp.doAiTextSummary() AAA.1 STERR UploadedUserFile is null for uploadedUserFileId=" + this.upldUsrFileId  + ", " + toString());
                    return;
                }
                textStr = uploadedFileHelpUtils.parseUploadedUserFileForText(uuf);
            }
            else
            {
                LogService.logIt( "IactnItemResp.doAiTextSummary() AAA.2 Item is neither Essay or FileUpload Button. Something wrong. " + toString());
                return;
            }
            
            if( textStr==null || textStr.isBlank() )
            {
                LogService.logIt( "IactnItemResp.doAiTextSummary() AAA.3 STERR textStr is empty. Cannot create summary. " + toString());
                return;
            }
            
            EssayFacade discernFacade=EssayFacade.getInstance();
            
            // Check for existing Unscored Essay.
            UnscoredEssay ue = discernFacade.getUnscoredEssayForMinStatus( testEvent.getTestEventId(), this.iactnResp.intnObj.getSimletnodeseq(), this.intnItemObj.getSeq(), EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());

            if( ue!=null && ue.getSummary()!=null && !ue.getSummary().isBlank() )
            {
                summaryText=ue.getSummary();
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "IactnItemResp.doAiTextSummary() BBB.1 Found valid summaryText in unscoredEssay. " + toString());
                return;
            }
            
            if( textStr.length()<Constants.MIN_TEXT_LENGTH_FOR_AI_SUMMARY )
            {
                LogService.logIt( "IactnItemResp.doAiTextSummary() BBB.2 TextStr is too short for AI Summary calcs. textStr.length=" + textStr.length() + ", requires at least " + Constants.MIN_TEXT_LENGTH_FOR_AI_SUMMARY + ",  " + toString());
                return;
            }
            
            if( ue!=null && ue.getEssaySummaryStatusType().getIsCancelledOrHigher() )
            {
                LogService.logIt( "IactnItemResp.doAiTextSummary() BBB.3 UnscoredEssay scoreStatusType is cancelled or higher. " + toString());
                return;
            }

            if( ue!=null && ue.getEssaySummaryStatusType().submitted())
            {
                LogService.logIt( "IactnItemResp.doAiTextSummary() BBB.3B UnscoredEssay scoreStatusType is submitted - still pending. hasPendingScore=" + hasPendingScore + ", " + toString());
                
                if( !hasPendingScore )
                {
                    AiEssayScoringThread aiest = new AiEssayScoringThread(ue, true, false, true, null, null, null, 2 );
                    new Thread(aiest).start();
                    LogService.logIt( "IactnItemResp.doAiTextSummary() BBB.3C Re-submitted for summary. unscoredEssayId=" + ue.getUnscoredEssayId() + ", " + toString());                    
                }
                
                hasPendingScore = true;
                return;
            }

            if (!AiEssayScoringUtils.getAiEssayScoringOn())
            {
                LogService.logIt( "IactnItemResp.doAiTextSummary() BBB.4 AiEssayScoring is not On. Cannot request Summary. " + toString());
                return;
            }
            
            if( ue==null )
            {
                ue = new UnscoredEssay();
                ue.setEssay(textStr);
                if (iactnResp.simletScore.getTestEvent() != null)
                    ue.setUserId(iactnResp.simletScore.getTestEvent().getUserId());
                if( iactnResp.simletScore.getTestEvent().getLocaleStr()!=null && !iactnResp.simletScore.getTestEvent().getLocaleStr().isBlank() )
                    ue.setLocaleStr(iactnResp.simletScore.getTestEvent().getLocaleStr());
                else
                    ue.setLocaleStr(Locale.US.toString());
                ue.setEssayPromptId(ScoredEssayIntnItem.DUMMY_ESSAY_PROMPT_ID);
                ue.setUnscoredEssayTypeId(getIsEssay() ? UnscoredEssayType.ESSAY.getUnscoredEssayTypeId() : UnscoredEssayType.UPLOADED_FILE.getUnscoredEssayTypeId() );
                ue.setCt5ItemId( iactnResp.getCt5ItemId() );
                ue.setCt5ItemPartId(this.intnItemObj.getCt5Itempartid());
                ue.setNodeSequenceId(iactnResp.getSimletNodeSeq());
                ue.setSummaryStatusTypeId(EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());
                ue.setSubnodeSequenceId(this.intnItemObj.getSeq());
                if( iactnResp.simletScore.getTestEvent()!=null )
                    ue.setTestEventId(iactnResp.simletScore.getTestEvent().getTestEventId());
                ue.setCreateDate(new Date());                


                ue.setTotalWords(StringUtils.numWords(textStr));                
                ue = discernFacade.saveUnscoredEssay(ue, true );                
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "IactnItemResp.doAiTextSummary() BBB.5 Created new UnscoredEssay for summary calcs. unscoredEssayId=" + ue.getUnscoredEssayId() + ", " + toString());
            }
            
            if( ue.getEssaySummaryStatusType().unsubmitted() )
            {
                this.hasPendingScore = true;
                ue.setSummaryStatusTypeId(EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());
                ue = discernFacade.saveUnscoredEssay(ue, true );                                
                AiEssayScoringThread aiest = new AiEssayScoringThread(ue, true, false, false, null, null, null, 2 );
                new Thread(aiest).start();
                
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "IactnItemResp.doAiTextSummary() CCC.1 Submitted for summary. unscoredEssayId=" + ue.getUnscoredEssayId() + ", " + toString());
            }
            
            else if( ue.getEssaySummaryStatusType().submitted() )
            {
                LogService.logIt( "IactnItemResp.doAiTextSummary() CCC.1 UnscoredEssay is already submitted. unscoredEssayId=" + ue.getUnscoredEssayId() + ", " + toString());
                this.hasPendingScore = true;
            }
            
            else
            {                
                LogService.logIt( "IactnItemResp.doAiTextSummary() DDD.1 UnscoredEssay is in an unexpected status: " + ue.getEssaySummaryStatusType().getName() + ", unscoredEssayId=" + ue.getUnscoredEssayId() + ", " + toString());
            }
        } catch (Exception e)
        {
            LogService.logIt(e, "IactnItemResp.doAiTextSummary() " + toString());
        }
    }

    @Override
    public boolean getUsesOrContributesPointsToSimletCompetency(SimletCompetencyScore smltCs)
    {
        // standard check.
        return this.simletCompetencyId() == smltCs.competencyScoreObj.getId();
    }

    @Override
    public String toString()
    {
        return "IactnItemResp{ iactn=" + (iactnResp == null || iactnResp.intnObj == null ? "iactnResp.intnObj is null" : StringUtils.getUrlDecodedValue(iactnResp.intnObj.getName()))
                + ", ct5ItemId=" + this.getCt5ItemId() + ", ct5ItemPartId=" + this.getCt5ItemPartId() + ", upldUsrFileId=" + this.upldUsrFileId
                + " (" + (iactnResp.intnObj != null ? iactnResp.intnObj.getSeq() : "") + "-" + (intnItemObj == null ? "intnItemObj is null" : intnItemObj.getSeq()) + "), content=" + (intnItemObj == null ? "" : intnItemObj.getContent())
                + " simletCompetencyScore.name=" + (simletCompetencyScore == null ? "simletCompetencyScore is null" : simletCompetencyScore.competencyScoreObj.getName()) + ", maxPoints[0]=" + (getMaxPointsArray() == null ? "null" : Float.toString(getMaxPointsArray()[0])) + '}';
    }

    public List<Integer> getDragTargetTenantSeqs()
    {
        List<Integer> out = new ArrayList<>();

        String v = getRespValue();

        // LogService.logIt( "IntnItemResp.getDragTargetTenantSeqs() Start for Drag tenant: " + this.intnItemObj.getContent() +  " (" + this.iactnResp.intnObj.getSeq() + "-" + this.intnItemObj.getSeq() + ") response value=" + v );
        if (v == null || v.isBlank())
            return out;

        String[] vals = v.split(",");

        for (String val : vals)
        {
            try
            {
                if (val.isBlank())
                    continue;

                // LogService.logIt( "IntnItemResp.getDragTargetTenantSeqs() adding " + val );
                out.add(Integer.valueOf(val));
            } catch (NumberFormatException e)
            {
                LogService.logIt(e, "IactnItemResp.getDragTargetTenantSeqs() " + toString() + ", " + v);
            }
        }

        return out;
    }

    public List<Integer> getDragTargetCorrectTenantSeqs()
    {
        List<Integer> out = new ArrayList<>();

        String v = this.intnItemObj.getDrgtgtCorrectseqids();

        // LogService.logIt( "IactnItemResp.getDragTargetCorrectTenantSeqs() intnItemObj.getDrgtgtCorrectseqids()=" + v );
        if (v == null || v.isBlank())
            return out;

        String[] vals = v.split(";");

        for (String val : vals)
        {
            try
            {
                if (val.isBlank())
                    continue;

                // LogService.logIt( "IactnItemResp.getDragTargetCorrectTenantSeqs() adding " + val );
                out.add(Integer.valueOf(val));
            } catch (NumberFormatException e)
            {
                LogService.logIt(e, "IactnItemResp.getDragTargetCorrectTenantSeqs() " + toString() + ", " + v);
            }
        }

        return out;
    }

    public boolean isDragTarget()
    {
        return intnItemObj.getDrgtgt() == 1;
    }

    @Override
    public boolean requiresMaxPointIncrement()
    {
        return false;
    }

    @Override
    public long simletId()
    {
        if (simletCompetencyScore != null)
            return simletCompetencyScore.simletScore.simletObj.getId();

        return 0;
    }

    public boolean supportsRadioButtonGroupAutoScoring()
    {
        if (intnItemObj == null)
            return false;

        return g2ChoiceFormatType.getIsAnyRadio();
    }

    public boolean needsAiSummaryCalc()
    {
        if (!getIsEssay() && !getIsFileUploadButton())
            return false;

        if( getIsFileUploadButton() && upldUsrFileId<=0 )
        {
            LogService.logIt("IactnItemResp.needsAiSummaryCalc() AAA.1 FileUploadButton but UploadedUserFileId<=0. " + StringUtils.getUrlDecodedValue(this.iactnResp.intnObj.getName()));
            return false;            
        }
        
        if (iactnResp==null)
        {
            LogService.logIt("IactnItemResp.needsAiSummaryCalc() AAA.2 IactnResp is null. Unexpected. " + StringUtils.getUrlDecodedValue(this.iactnResp.intnObj.getName()));
            return false;
        }

        if (iactnResp.simletCompetencyScore==null)
        {
            // LogService.logIt("IactnItemResp.needsAiSummaryCalc() AAA.3 IactnResp.simCompetencyScore is null. Unexpected. " + StringUtils.getUrlDecodedValue(this.iactnResp.intnObj.getName()));
            return false;
        }

        if (intnItemObj.getCompetencyscoreid() <= 0)
        {
            // LogService.logIt("IactnItemResp.needsAiSummaryCalc() AAA.4 IactnResp.intnItemObj.getCompetencyscoreid()<=0. Unexpected. " + StringUtils.getUrlDecodedValue(this.iactnResp.intnObj.getName()));
            return false;
        }

        // If there NOT summary only (3), then we do not need this.
        if (iactnResp.intnObj.getCt5Int25()!=3)
        {
            // LogService.logIt("IactnItemResp.needsAiSummaryCalc() AAA.5 IactnResp.intnObj.getCt5Int25()=" + iactnResp.intnObj.getCt5Int25() + ", " + StringUtils.getUrlDecodedValue(this.iactnResp.intnObj.getName()));
            return false;
        }

        // LogService.logIt("IactnItemResp.needsAiSummaryCalc() ZZZ.1 simletCompetencyScore.competencyScoreType=" + iactnResp.simletCompetencyScore.getCompetencyScoreType().getName() +", " + StringUtils.getUrlDecodedValue(this.iactnResp.intnObj.getName()));
        
        // Only needs it here if the competencyScoreType is Not a scored essay.
        return !iactnResp.simletCompetencyScore.getCompetencyScoreType().isScoredEssay();
    }

    public boolean subnodeGeneratesItsOwnPoints()
    {
        if (intnItemObj == null)
            return false;

        if (g2ChoiceFormatType.generatesItsOwnPoints())
            return true;

        if (g2ChoiceFormatType.getIsTextBox() && IvrStringUtils.getTagValue(intnItemObj.getTextscoreparam1(), Constants.POINTS_KEY) != null && !IvrStringUtils.getTagValue(intnItemObj.getTextscoreparam1(), Constants.POINTS_KEY).isEmpty())
            return true;

        return false;
    }

    public boolean supportsSubnodeLevelSimletAutoScoring()
    {
        if (intnItemObj == null)
            return false;

        if (g2ChoiceFormatType.getIsAnyCheckbox())
            return intnItemObj.getSimcompetencyid() > 0;

        if (isDragTarget() && intnItemObj.getDrgtgtcheckbox() == 1 && intnItemObj.getSimcompetencyid() > 0)
            return true;
        
        // File upload button with 
        if( this.getIsFileUploadButton() && iactnResp.intnObj.getCt5Int25()==3 )
            return true;

        return g2ChoiceFormatType.supportsSubnodeLevelSimletAutoScoring();
    }

    public G2ChoiceFormatType getG2ChoiceFormatType()
    {
        return g2ChoiceFormatType; // G2ChoiceFormatType.getValue(  intnItemObj.getFormat()  );
    }

    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return supportsSubnodeLevelSimletAutoScoring() && hasCorrectRespForSubnodeDichotomousScoring();
    }

    @Override
    public synchronized float[] getMaxPointsArray()
    {
        if (maxPointsArray != null)
            return maxPointsArray;

        if (intnItemObj == null || intnItemObj.getMaxpoints() == null || intnItemObj.getMaxpoints().isEmpty())
            return new float[4];

        maxPointsArray = InteractionScoreUtils.getPointsArray(intnItemObj.getMaxpoints());

        return maxPointsArray;
    }

    /**
     * Returns an array of data[0] = itemScore (Float) data[1] = scoreParam1
     * (Float) data[2] = scoreParam2 (Float) data[3] = scoreParam3 (Float)
     * data[10] = textScoreParam1 (String)
     *
     * @return
     */
    @Override
    public Object[] getScoreParamsArray()
    {
        Object[] out = new Object[11];
        out[0] = (float) (0);
        out[1] = (float) (0);
        out[2] = (float) (0);
        out[3] = (float) (0);

        if (intnItemObj != null)
        {
            out[0] = (float) (intnItemObj.getTruescore());
            out[1] = (float) (intnItemObj.getScoreparam1());
            out[2] = (float) (intnItemObj.getScoreparam2());
            out[3] = (float) (intnItemObj.getScoreparam3());

            out[10] = intnItemObj.getTextscoreparam1();
        }

        return out;
    }

    @Override
    public boolean isPendingExternalScore()
    {
        return hasPendingScore;
    }

    @Override
    public boolean measuresSmltCompetency(long id)
    {
        return simletCompetencyId() == id;
    }

    @Override
    public int getCt5SubtopicId()
    {
        return intnItemObj == null ? 0 : intnItemObj.getCt5Subtopicid();
    }

    @Override
    public long simCompetencyId()
    {
        return intnItemObj == null ? 0 : intnItemObj.getSimcompetencyid();
    }

    @Override
    public long simletCompetencyId()
    {
        if (simletCompetencyScore != null)
            return simletCompetencyScore.competencyScoreObj.getId();

        return 0;
    }

    /**
     * correct= 0 means answered, wrong answer or not scored 1 means answered
     * correct, -1 means not answered, -2 means timed out
     *
     * @return
     */
    public int getItemResponseTypeId()
    {
        // if this int item was answered correctly
        if (correct())
            return 1;

        if (getG2ChoiceFormatType().getIsIFrame())
        {
            return getRespValue() != null && getRespValue().indexOf(";") > 0 ? 0 : -1;
        }

        // all intn click stream items are incorrect or timed out.
        if (getG2ChoiceFormatType().getIsIntnClickStream())
            return iactnResp.getItemResponseTypeId() == -2 ? -2 : 0;

        // if this int item was answered at all
        if (getRespValue() != null)
            return 0;

        // not answered, either it's not answered or timed out.
        return iactnResp.getItemResponseTypeId() < 0 ? iactnResp.getItemResponseTypeId() : -1;
    }

    public String getValueForReport(String question)
    {
        getRespValue();

        return g2ChoiceFormatType.getValueForReport(iactnResp.getIntnObj(), intnItemObj, respValue, getWasSelected(), question);
    }

    public String getFieldTitleForReport(String question)
    {
        return g2ChoiceFormatType.getFieldTitleForReport(iactnResp.getIntnObj(), intnItemObj, question);
    }

    public boolean getIsRedFlag()
    {
        return intnItemObj.getRedflag() == 1 && this.getWasSelected();
    }

    private void computeTextPointsMatch()
    {
        if (!g2ChoiceFormatType.getIsTextBox())
            return;

        if (textPointsMatchedIndex >= 0)
        {
            // LogService.logIt( "IactnItemResp.computeTextPointsMatch() AAA.1  intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq()+ " " + this.intnResultObj.getUnqid() + ", have existing index=" + textPointsMatchedIndex + ", textPointsLst.size=" + textPointsLst.size() );
            return;
        }

        //LogService.logIt( "IactnItemResp.computeTextPointsMatch() AAA.2  intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() );
        // List<Object[]> string, points
        computeTextPointsLst();

        if (textPointsLst == null || textPointsLst.isEmpty())
        {
            // LogService.logIt( "IactnItemResp.computeTextPointsMatch() BBB.1  intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() + " " + this.intnResultObj.getUnqid()+ " textPointsList=" + (textPointsLst==null ? "null" : textPointsLst.size()) );
            return;
        }

        //LogService.logIt( "IactnItemResp.computeTextPointsMatch() BBB.2  intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq()+ " " + this.intnResultObj.getUnqid() + " textPointsList=" + textPointsLst.size() );
        getRespValue();

        if (respValue == null || respValue.isEmpty())
            return;

        // LogService.logIt( "IactnItemResp.computeTextPointsMatch() CCC.1 After compute computeTextPointsLst() textPointsLst.size=" + textPointsLst.size() + ", respValue=" + respValue );
        FillBlankType fbt = FillBlankType.getValue(intnItemObj.getFillblankcontenttype());

        String v;

        Object[] vals;

        Locale loc;
        if (intnItemObj.getLangcode() != null && !intnItemObj.getLangcode().isBlank())
            loc = I18nUtils.getLocaleFromCompositeStr(intnItemObj.getLangcode());
        else
            loc = this.iactnResp.getSimLocale();

        boolean caseSensitive = false;
        if (intnItemObj != null && iactnResp != null && iactnResp.getIntnObj() != null)
            caseSensitive = Ct5ItemType.getValue(iactnResp.getIntnObj().getCt5Itemtypeid()).getIsFillBlank() ? intnItemObj.getCt5Int1() == 1 : intnItemObj.getCt5Int2() == 1;

        for (int i = 0; i < textPointsLst.size(); i++)
        {
            vals = textPointsLst.get(i);

            v = ((String) vals[0]).trim();

            // LogService.logIt( "IactnItemResp.computeTextPointsMatch() DDD.1 Testing " + v );
            if (fbt.valuesMatch(respValue, v, loc, caseSensitive))
            {
                textPointsMatchedIndex = i;
                // LogService.logIt( "IactnItemResp.computeTextPointsMatch() EEE.1 After compute computeTextPointsLst() RespValue=" + respValue + " matches key text " + v );
                return;
            }
        }
    }

    private synchronized void computeTextPointsLst()
    {
        // LogService.logIt( "IactnItemResp.computeTextPointsLst() START AAA.1" );
        if (!g2ChoiceFormatType.getIsTextBox())
            return;

        // LogService.logIt( "IactnItemResp.computeTextPointsLst() START AAA.2" );
        if (textPointsLst != null)
            return;

        // LogService.logIt( "IactnItemResp.computeTextPointsLst() START AAA.3" );
        String cv = intnItemObj.getTextscoreparam1();

        if (cv == null || cv.isEmpty())
        {
            // LogService.logIt( "IactnItemResp.computeTextPointsLst() AAA.X  cv is empty. intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() );
            return;
        }

        String tv = IvrStringUtils.getTagValue(cv, Constants.POINTS_KEY);

        // LogService.logIt( "IactnItemResp.computeTextPointsLst() AAA  intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq()+ " " + this.intnResultObj.getUnqid() + ", textScoreParam1=" + cv+ ", TagValue=" + tv );
        // null means tag not present.
        if (tv == null)
            return;

        // Have a points string, so signal we will use this
        textPointsLst = new ArrayList<>();

        // not null but empty, return.
        if (tv.trim().isEmpty())
            return;

        String[] vals = tv.split("\\|");

        String v, p;
        float pts;

        for (int i = 0; i < vals.length - 1; i += 2)
        {
            v = vals[i].trim();
            p = vals[i + 1].trim();

            // LogService.logIt( "IactnItemResp.computeTextPointsLst() BBB  intnItemSeq" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() + " " + this.intnResultObj.getUnqid() + ", v=" + v+ ", p=" + p );
            if (v.isEmpty())
                continue;

            try
            {
                pts = p.isEmpty() ? 0 : Float.parseFloat(p);
            } catch (NumberFormatException e)
            {
                LogService.logIt("IactnItemResp.getTextPointsLst() Unable to parse points value: " + p + ", TextScoreParam1.Points=" + tv);
                pts = 0;
            }

            if (textPointsLst == null)
                textPointsLst = new ArrayList<>();

            textPointsLst.add(new Object[]
            {
                v, ((Float) pts)
            });
        }
    }

    /*
      this is for Legacy IMOs where the
     */
    private Map<String, String> getComboSelectMap()
    {
        Map<String, String> out = new HashMap<>();

        String inStr = intnItemObj.getText1();

        if (inStr == null || inStr.isBlank())
            return out;

        String t1 = StringUtils.getUrlDecodedValue(intnItemObj.getText1());

        t1 = StringUtils.replaceStr(t1, "\n", "|");

        // LogService.logIt( "IactnItemResp.getComboSelectMap() AAA.1 test1=" + t1 );
        String[] vals = t1.split("\\|");

        String val;
        String lbl;
        for (int i = 0; i < vals.length - 1; i += 2)
        {
            val = vals[i].trim();
            lbl = vals[i + 1].trim();

            if (val.isBlank())
                continue;

            // LogService.logIt( "IactnItemResp.getComboSelectMap() Adding " + val + " / " + lbl );
            out.put(val, lbl);
        }

        return out;
    }

    public String getCorrectValue()
    {
        if (isDragTarget() && intnItemObj.getDrgtgtcheckbox() == 1)
        {
            String selValue = "";
            for (Integer s : this.getDragTargetCorrectTenantSeqs())
            {
                if (!selValue.isEmpty())
                    selValue += ";";
                selValue += s.toString();
            }
            return selValue;
        }

        if (g2ChoiceFormatType.getIsCombo())
        {
            if (intnItemObj.getCorrect() != null && !intnItemObj.getCorrect().isBlank())
            {
                return intnItemObj.getCorrect();
            }

            //Map<String,String> vm = getComboSelectMap();
            //for( String v : vm.keySet() )
            //{
            //    if( !v.equals( "0" ) )
            //        return vm.get(v);
            //}
        }

        if (g2ChoiceFormatType.getIsSliderThumb() || g2ChoiceFormatType.getIsActiveSwf())
            return intnItemObj.getCorrect();

        if (g2ChoiceFormatType.getIsTextBox()) // && simletCompetencyScore.getCompetencyScoreType().isDichotomous()  )
        {
            // Format of textscoreparam1 for a Text Box can just be a list of text values delimited by |
            // or it can be a POINTS tag in format text|points|text|points|...

            // So, first see if there's anything in a points tag.
            computeTextPointsMatch();

            if (textPointsLst != null)
            {
                // return the matched correct value.
                if (textPointsMatchedIndex >= 0 && textPointsMatchedIndex < textPointsLst.size())
                    return (String) textPointsLst.get(textPointsMatchedIndex)[0];

                // none matched, just return the first one.
                for (Object[] oa : textPointsLst)
                {
                    return (String) oa[0];
                }

                // should not happen.
                return null;
            }

            // At this point, there was no
            String cv = intnItemObj.getTextscoreparam1();

            cv = IvrStringUtils.getNonTaggedValue(cv);

            if (cv == null || cv.isEmpty())
                return null;

            String[] vals = cv.split("\\|");

            for (String v : vals)
            {
                v = v.trim();

                if (!v.isEmpty())
                    return v;
            }
        }

        return null;
    }

    public String getRespValueForItemScore()
    {
        getRespValue();

        if (isDragTarget() && isAutoScorable())
        {
            String selValue = "";
            for (Integer s : getDragTargetTenantSeqs())
            {
                for (IactnItemResp irp : iactnResp.iactnItemRespLst)
                {
                    if (irp.intnItemObj.getSeq() == s)
                    {
                        if (!selValue.isEmpty())
                            selValue += ";";

                        selValue += UrlEncodingUtils.decodeKeepPlus(irp.intnItemObj.getContent());
                        break;
                    }
                }

            }
            return selValue;
        }

        if (g2ChoiceFormatType.getIsSliderThumb())
        {
            try
            {
                float f = respValue == null || respValue.isBlank() ? 0 : Float.parseFloat(respValue);

                return I18nUtils.getFormattedNumber(Locale.US, f, 4);
            } catch (NumberFormatException e)
            {
                LogService.logIt("IactnItemResp.getRespValueForItemScore() Unable to parse respValue for slider thumb: " + respValue);
                return respValue;
            }
        }

        if (!g2ChoiceFormatType.getIsCombo())
            return respValue;

        if (respValue == null || respValue.isEmpty())
            return "";

        // here we want to get a list of all selected choices.
        StringBuilder sb = new StringBuilder();

        // Map of value , label
        Map<String, String> selMap = intnItemObj.getCt5Str1() != null && !intnItemObj.getCt5Str1().isBlank() ? getComboValueLabelMap() : getComboSelectMap();

        try
        {
            String lbl;
            for (String rv : respValue.split(","))
            {
                if (rv.isBlank())
                    continue;

                rv = rv.trim();

                lbl = selMap.get(rv);

                // LogService.logIt( "IactnItemResp.getRespValueForItemScore() Seeking label for " + rv + " found " + lbl );
                if (sb.length() > 0)
                    sb.append(",");

                if (lbl == null || lbl.isBlank())
                {
                    lbl = rv;
                    sb.append(lbl);
                } else
                    sb.append(lbl + " (" + rv + ")");

            }
        } catch (Exception e)
        {
            LogService.logIt(e, "IactnItemResp.getRespValueForItemScore() " + this.toString());
        }
        return sb.toString();
    }

    public String getRespValue()
    {
        try
        {
            // LogService.logIt( "IactnItemResp.getRespValue() respValue=" + respValue + ", intnResultObj.getValue()=" + intnResultObj.getValue() );

            if (respValue != null)
                return respValue;

            if (intnResultObj == null)
                return respValue;

            /*
            if( isDragTarget() && intnItemObj.getDrgtgtcheckbox()==1 )
            {
               String selValue = "";
               for( Integer s : getDragTargetTenantSeqs() )
               {
                   if( !selValue.isEmpty() )
                       selValue += ";";

                   selValue += s.toString();
               }
               respValue = selValue;
               return respValue;
            }
             */
            String t = intnResultObj.getValue();

            if (t == null || t.isBlank())
                return respValue;

            // String[] pcs = t.split( ";" );
            StringTokenizer st = new StringTokenizer(t, "~");

            String t1, t2;

            int seq;

            while (st.hasMoreTokens())
            {
                t1 = st.nextToken().trim();

                if (!st.hasMoreTokens())
                    continue;

                t2 = st.nextToken().trim();

                seq = Integer.parseInt(t1);

                // LogService.logIt( "IactnItemResp.getRespValue() seq=" + seq + ", intnItemObj.getSeq()=" + intnItemObj.getSeq() + ", t2=" + t2 );
                if (seq == intnItemObj.getSeq())
                {
                    respValue = UrlEncodingUtils.decodeKeepPlus(t2); // resp value is url-encoded

                    respValue = StringUtils.replaceStr(respValue, "@#@", "~");
                    respValue = StringUtils.replaceStr(respValue, "&#&", "^");
                    respValue = StringUtils.replaceStr(respValue, "$#$", "|");

                    break;
                }
            }
        } catch (Exception e)
        {
            LogService.logIt(e, "IactnItemResp.getRespValue() ");
        }

        return respValue;
    }


    /*
     *
     * SimletCompetencyScore Types:
            NONE(0,"No Score"),
            PERCENT_CORRECT(1,"Percent Correct"),
            PERCENT_OF_TOTAL(2,"Pct Available Points"),
            NORM_SCALE(3,"Normalized Scale");     *
     *
     * Simlet ItemTypes:
            NA(0,"NA - Not a Simlet Item" ),
            DICHOTOMOUS(1,"Dichotomous - Right or Wrong" ),
            POINTS(2,"Each Choice Has Points" ),
            ESSAY(3, "Text Choices Saved for Presentation" ),
            VOICE(4, "Voice Recording" ),
            UPLOAD(5, "File Upload" ),
            OTHER_TRACK(99,"Other Trackable Response" );
     *
     *
     */
    @Override
    public int simletItemTypeId()
    {
        if (supportsSubnodeLevelSimletAutoScoring())
        {
            if (simletCompetencyScore != null && simletCompetencyScore.competencyScoreObj != null && simletCompetencyScore.competencyScoreObj.getClassid() == SimCompetencyClass.SCOREDCHAT.getSimCompetencyClassId())
                return SimletItemType.AUTO_CHAT.getSimletItemTypeId();

            String cv = intnItemObj.getCorrect();

            if (cv == null || cv.isEmpty())
                return SimletItemType.AUTO_POINTS.getSimletItemTypeId();

            return SimletItemType.AUTO_DICHOTOMOUS.getSimletItemTypeId();
        }

        return iactnResp.intnObj.getScoretype();
    }

    @Override
    public SimletItemType getSimletItemType()
    {
        if (supportsSubnodeLevelSimletAutoScoring())
        {
            if (simletCompetencyScore != null && simletCompetencyScore.competencyScoreObj != null && simletCompetencyScore.competencyScoreObj.getClassid() == SimCompetencyClass.SCOREDCHAT.getSimCompetencyClassId())
                return SimletItemType.AUTO_CHAT;

            if (getMaxPointsArray() != null && getMaxPointsArray()[0] > 0) //  && (simletCompetencyScore==null || !simletCompetencyScore.getCompetencyScoreType().isDichotomous()) )
            {
                return SimletItemType.AUTO_POINTS;
            }

            if (simletCompetencyScore != null && simletCompetencyScore.competencyScoreObj != null && CompetencyScoreType.getValue(simletCompetencyScore.competencyScoreObj.getScoretype()).isPointAccum())
                return SimletItemType.AUTO_POINTS;

            if( getIsFileUploadButton() )
                return SimletItemType.MANUAL_UPLOAD;
            
            // String cv = intnItemObj.getCorrect();
            // if( cv == null || cv.isEmpty() )
            //     return SimletItemType.AUTO_POINTS;
            return SimletItemType.AUTO_DICHOTOMOUS;
        }

        return iactnResp.getSimletItemType();
    }

    @Override
    public boolean experimental()
    {
        return iactnResp.intnObj.getExperimental() == 1 || intnItemObj.getExperimental() == 1;
    }

    @Override
    public boolean getPartialCreditAssigned()
    {
        if (getSimletItemType().isDichotomous())
            return false;

        if (getSimletItemType().isPoints())
            return itemScore() > 0 && itemScore() < getMaxPointsArray()[0];

        return false;
    }

    @Override
    public boolean correct()
    {
        return correct(false);
    }

    public boolean correct(boolean fmItemScore)
    {

        if (simletCompetencyScore == null || simletCompetencyScore.getCompetencyScoreType() == null)
            return false;

        // LogService.logIt( "IactnItemResp.correct() START Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() + ", unique=" + this.iactnResp.intnObj.getUniqueid() + ", respValue=" + getRespValue() + ", dichot=" + simletCompetencyScore.getCompetencyScoreType().isDichotomous() + ", score Type=" + simletCompetencyScore.getCompetencyScoreType().getName() );
        if (isDragTarget() && isAutoScorable()) // && simletCompetencyScore.getCompetencyScoreType().isDichotomous() )
        {
            List<SimJ.Intn.Intnitem> cil = getDragTargetCorrectTenants();
            // Has at least one correct tenant
            return cil != null && !cil.isEmpty();
        }

        if (g2ChoiceFormatType.getIsAnyCheckbox()) // && simletCompetencyScore.getCompetencyScoreType().isDichotomous() )
        {
            String v = getRespValue();

            // Selected
            if (v != null && v.equalsIgnoreCase("true"))
            {
                if (intnItemObj.getScoreparam1() > 0)
                    return true;
            } // not selected
            else
            {
                if (intnItemObj.getScoreparam1() < 0)
                    return true;
            }

            return false;
        }

        if (simletCompetencyScore.getCompetencyScoreType().isDichotomous()
                || (g2ChoiceFormatType.getIsCombo() && SimCompetencyClass.getValue(simletCompetencyScore.competencyScoreObj.getClassid()).getSupportsQuasiDichotomous()))
        {
            // LogService.logIt( "IactnItemResp.correct() AAAA.1 " );

            // an interaction item is only scored correct if it is a combo box with a correct response specified
            if (g2ChoiceFormatType.getIsCombo())
            {
                // list of possible correct values
                String cv = intnItemObj.getCorrect();

                // no correct values - cannot be correct
                if (cv == null || cv.isEmpty())
                    return false;

                // decode
                cv = StringUtils.getUrlDecodedValue(cv);

                getRespValue();

                // no submitted value, cannot be correct
                if (respValue == null || respValue.isEmpty())
                    return false;

                //LogService.logIt( "IactnItemResp.correct() START Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() + ", unique=" + this.iactnResp.intnObj.getUniqueid() + ", cv=" + cv + ", respValue=" + respValue );
                // only one correct value. Check for match.
                if (cv.indexOf(";") < 0)
                {
                    if (respValue.trim().equalsIgnoreCase(cv.trim()))
                        return true;
                    else
                        return false;
                } // several values are correct
                else
                {
                    String[] cvs = cv.split(";");
                    String[] respVals = respValue.split(",");
                    String v;
                    // boolean fnd;

                    // for each correct value. These are always in pairs.
                    for (int i = 0; i < cvs.length - 1; i += 2)
                    {
                        v = cvs[i];

                        // no value - skip this one
                        if (v.isBlank())
                            continue;

                        v = v.trim();

                        //LogService.logIt( "IactnItemResp.correct() AAA.1 seeking " + v + ", respValue=" + respValue );
                        // no hope.
                        if (!respValue.contains(v))
                            continue;
                        //     return false;

                        // fnd=false;
                        for (String rv : respVals)
                        {
                            rv = rv.trim();

                            //LogService.logIt( "IactnItemResp.correct() AAA.2 comparing " + v + ", with " + rv + "  to get: " + rv.trim().equals(v) );
                            if (rv.trim().equals(v))
                                return true;
                            // fnd=true;
                        }

                        // next possible corect value
                        // if( !fnd )
                        //     return false;
                    }

                    // no match found.
                    return false;
                    //return true;
                }
            }

            // an interaction item is only scored correct if it is a combo box with a correct response specified
            if (g2ChoiceFormatType.getIsSliderThumb() || g2ChoiceFormatType.getIsActiveSwf())
            {
                String cv = intnItemObj.getCorrect();

                if (cv == null || cv.isEmpty())
                    return false;

                cv = StringUtils.getUrlDecodedValue(cv);

                // List<String> crctVals = new ArrayList<>();
                getRespValue();

                if (respValue == null || respValue.isEmpty())
                    return false;

                if (respValue.trim().equalsIgnoreCase(cv.trim()))
                    return true;
            }

            if (g2ChoiceFormatType.getIsTextBox()) // && simletCompetencyScore.getCompetencyScoreType().isDichotomous() )
            {
                // Format of textscoreparam1 for a Text Box can just be a list of text values delimited by |
                // or it can be a POINTS tag in format text|points|text|points|...

                // LogService.logIt( "IactnItemResp.correct() TextBox AAA Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() + ", unique=" + this.iactnResp.intnObj.getUniqueid() + ", respValue=" + respValue );
                // LogService.logIt( "IactnItemResp.correct() AAA " );
                // So, first see if there's anything in a points tag.
                computeTextPointsMatch();

                // If textPointsLst is not null, that means a POINTS tag was found.
                if (textPointsLst != null)
                {
                    // LogService.logIt( "IactnItemResp.correct() TextBox BBB Found POINTS. textPointsMatchedIndex=" + textPointsMatchedIndex );
                    return textPointsMatchedIndex >= 0;
                } // No POINTS Tag found.
                else
                {
                    return textBoxMatchesSample();
                }
            }
        } // To support item analysis, for non-dichotomous items if itemscore=max points we can consider the item correct.
        else if (SimCompetencyClass.getValue(simletCompetencyScore.competencyScoreObj.getClassid()).getSupportsQuasiDichotomous())
        {
            if (g2ChoiceFormatType.getIsIntnClickStream())
                return itemScore() > 0;

            // iFrame ahas format maxpoints;points, so if they match, it's correct.
            if (g2ChoiceFormatType.getIsIFrame())
            {
                String v = getRespValue();

                if (v == null || v.trim().isEmpty() || !v.contains(";"))
                    return false;

                String[] vals = v.split(";");

                if (vals.length < 3)
                    return false;

                return Float.parseFloat(vals[1].trim()) == Float.parseFloat(vals[2].trim());
            }

            if (!fmItemScore && itemScore() == getMaxPointsArray()[0] && itemScore() > 0)
                return true;
        }

        return false;
    }

    /**
     * data[0] = 0 or 1 (1==correct) data[1] = points if any.
     *
     * @return
     *
     * private float[] getComboScore() { float[] out = new float[2];
     *
     * if( !g2ChoiceFormatType.getIsCombo() ) return out;
     *
     * String cv = intnItemObj.getCorrect();
     *
     * if( cv == null || cv.isEmpty() ) return out;
     *
     * cv = StringUtils.getUrlDecodedValue(cv);
     *
     * getRespValue();
     *
     * // no response if( respValue == null || respValue.isEmpty() ) return out;
     *
     * // LogService.logIt( "IactnItemResp.correct() " +
     * this.iactnResp.intnObj.getUniqueid() + ", cv=" + cv + ", respValue=" +
     * respValue );
     *
     * if( cv.indexOf(";")<0 ) { if( respValue.trim().equalsIgnoreCase(
     * cv.trim()) ) { out[0]=1; out[1]=intnItemObj.getItemscore(); return out; }
     * }
     *
     * else { String[] cvs = cv.split(";" ); String[] respVals =
     * respValue.split( "," ); String v; String p; float pts; boolean fnd;
     *
     * // for each correct value for( int i=0; i<cvs.length-1; i+=2 ) { v =
     * cvs[i];
     *
     * if( v.isBlank()) continue; v = v.trim(); p = cvs[i+1].trim(); pts =
     * p.isBlank() ? 0 : NumberUtils.parseFloatInputStr( Locale.US, p );
     *
     * // LogService.logIt( "IactnItemResp.correct() AAA.1 seeking " + v + ",
     * respValue=" + respValue );
     *
     * // no hope. if( !respValue.contains(v) ) return out;
     *
     * fnd=false; for( String rv : respVals ) { rv=rv.trim();
     *
     * // LogService.logIt( "IactnItemResp.correct() AAA.2 comparing " + v + ",
     * with " + rv ); if( rv.trim().equals(v) ) { out[0]=1; out[1]=pts; return
     * out; } } }
     *
     * return true; }
     *
     *
     * return out; }
     */
    private boolean textBoxMatchesSample()
    {
        getRespValue();

        if (respValue == null || respValue.isEmpty())
            return false;

        FillBlankType fbt = FillBlankType.getValue(intnItemObj.getFillblankcontenttype());

        String cv = intnItemObj.getTextscoreparam1();

        Locale loc;
        if (intnItemObj.getLangcode() != null && !intnItemObj.getLangcode().isBlank())
            loc = I18nUtils.getLocaleFromCompositeStr(intnItemObj.getLangcode());
        else
            loc = iactnResp.getSimLocale();

        cv = IvrStringUtils.getNonTaggedValue(cv);

        if (cv == null || cv.isEmpty())
            return false;

        boolean caseSensitive = false;
        if (intnItemObj != null && iactnResp != null && iactnResp.getIntnObj() != null)
            caseSensitive = Ct5ItemType.getValue(iactnResp.getIntnObj().getCt5Itemtypeid()).getIsFillBlank() ? intnItemObj.getCt5Int1() == 1 : intnItemObj.getCt5Int2() == 1;

        String[] vals = cv.split("\\|");

        for (String v : vals)
        {
            v = v.trim();

            if (fbt.valuesMatch(respValue, v, loc, caseSensitive))
                return true;
        }

        return false;
    }

    public boolean hasCorrectRespForSubnodeDichotomousScoring()
    {
        if (g2ChoiceFormatType.getIsCombo() || g2ChoiceFormatType.getIsSliderThumb() || g2ChoiceFormatType.getIsActiveSwf())
        {
            String cv = intnItemObj.getCorrect();

            return cv != null && !cv.isEmpty();
        }

        if (g2ChoiceFormatType.getIsIntnClickStream() || g2ChoiceFormatType.getIsIFrame())
            return true;

        if (g2ChoiceFormatType.getIsTextBox() && simletCompetencyScore.getCompetencyScoreType().isDichotomous())
        {
            computeTextPointsMatch();

            if (textPointsLst != null)
                return !textPointsLst.isEmpty();

            // textPointsLst is null.
            String cv = intnItemObj.getTextscoreparam1();

            cv = IvrStringUtils.getNonTaggedValue(cv);

            return cv != null && !cv.isEmpty();
        }

        return false;
    }

    @Override
    public float itemScore()
    {
        // LogService.logIt( "IactnItemResp.itemScore() AAA.1 " );

        if (isDragTarget() && isAutoScorable())
        {
            //LogService.logIt( "IactnItemResp.itemScore() AAA.2 Drag Tgt IntnItem. Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() );
            List<SimJ.Intn.Intnitem> cil = getDragTargetCorrectTenants();

            //LogService.logIt( "IactnItemResp.itemScore() AAA.3 Drag Tgt IntnItem. Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() + ", correct DragTgtTenants.size=" + cil.size() + ", drag target item score=" + intnItemObj.getItemscore() );
            // Has at least one correct tenant
            if (cil != null && !cil.isEmpty())
            {
                // checked, so return itemscore if set to do so.
                //if( intnItemObj.getScoreparam1()>0 )
                return intnItemObj.getItemscore();
            }

            // not selected. Not sure how/when this would be used.
            //else
            //{
            //    if( intnItemObj.getScoreparam1()<0 )
            //        return intnItemObj.getItemscore();
            //}
            return 0;
        }

        // LogService.logIt( "IactnItemResp.itemScore() AAA.1 Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() );
        if (g2ChoiceFormatType.getIsCombo())
        {
            // LogService.logIt( "IactnItemResp.itemScore() AAA.2 COMBO Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() + ", dicotomous=" + simletCompetencyScore.getCompetencyScoreType().isDichotomous() + ", corect=" + correct( true ) );

            // If dichtomous, just use itemscore if correct.
            if (simletCompetencyScore.getCompetencyScoreType().isDichotomous() && correct(true))
                return intnItemObj.getItemscore();

            // Not dichotomous, need to pull points from correct String.
            String cv = intnItemObj.getCorrect();

            if (cv == null || cv.isEmpty())
                return 0;

            cv = StringUtils.getUrlDecodedValue(cv);

            getRespValue();

            if (respValue == null || respValue.isEmpty())
                return 0;

            // LogService.logIt( "IactnItemResp.itemScore() AAA.1 " + this.iactnResp.intnObj.getUniqueid() + ", cv=" + cv + ", respValue=" + respValue );
            float fv = 0;
            try
            {
                if (cv.indexOf(";") < 0)
                {
                    if (respValue.trim().equalsIgnoreCase(cv.trim()))
                        return NumberUtils.parseFloatInputStr(Locale.US, cv.trim());
                    else
                        return 0;
                } else
                {
                    String[] cvs = cv.split(";");
                    String[] respVals = respValue.split(",");
                    String v;
                    float f;
                    boolean fnd;

                    // for each correct value
                    for (int i = 0; i < cvs.length - 1; i += 2)
                    {
                        v = cvs[i];
                        if (v.isBlank())
                            continue;
                        v = v.trim();

                        f = NumberUtils.parseFloatInputStr(Locale.US, cvs[i + 1].trim());

                        // no hope.
                        if (!respValue.contains(v))
                            continue;

                        fnd = false;
                        for (String rv : respVals)
                        {
                            rv = rv.trim();

                            if (rv.trim().equals(v))
                                fnd = true;
                        }

                        if (fnd)
                            fv += f;
                    }

                    // LogService.logIt( "IactnItemResp.itemScore() AAA.1 " + this.iactnResp.intnObj.getUniqueid() + ", cv=" + cv + ", respValue=" + respValue + ", fv=" + fv );
                    return fv;
                }
            } catch (NumberFormatException e)
            {
                LogService.logIt(e, "IactnItemResp.itemScore() Could not parse either correct str=" + cv + ", or reeposne value=" + respValue + " for combo box interaction item. " + this.toString());
                return 0;
            }
        }
        // END COMBO BOX

        // BEGIN SLIDER
        if (g2ChoiceFormatType.getIsSliderThumb() || g2ChoiceFormatType.getIsActiveSwf())
        {
            String v = getRespValue();

            try
            {

                if (v == null || v.trim().isEmpty())
                    return 0;

                float fv = NumberUtils.parseFloatInputStr(Locale.US, v.trim());

                if (intnItemObj.getTextscoreparam1() != null && !intnItemObj.getTextscoreparam1().isBlank() && intnItemObj.getTextscoreparam1().toLowerCase().indexOf("[invert]") >= 0)
                {
                    fv = invertScore(fv);

                }

                return fv;
            } catch (NumberFormatException e)
            {
                LogService.logIt(e, "IactnItemResp.itemScore() Could not parse value=" + v + " as a float on combo box interaction item. " + this.toString());
            }

            return 0;
        }

        // format is maxpoints;totalpoints;other  So get the second value (totalpoints).
        if (g2ChoiceFormatType.getIsIFrame())
        {
            String v = getRespValue();
            try
            {
                if (v == null || v.trim().isEmpty() || v.indexOf(";") < 0)
                    return 0;

                String[] vals = v.split(";");

                if (vals.length < 2)
                    return 0;

                return Float.parseFloat(vals[1].trim());
            } catch (NumberFormatException e)
            {
                LogService.logIt(e, "IactnItemResp.itemScore() Could not parse value=" + v + " as a float on iFrame interaction item. " + this.toString());
                return 0;
            }
        }

        if (g2ChoiceFormatType.getIsTextBox())
        {
            // LogService.logIt( "IactnItemResp.itemScore() BBB Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() );
            if ((simletCompetencyScore.getCompetencyScoreType().isPointAccum() || simletCompetencyScore.getCompetencyScoreType().isPercentOfTotal() || simletCompetencyScore.getCompetencyScoreType().isDichotomous())
                    && intnItemObj.getTextscoreparam1() != null
                    && !intnItemObj.getTextscoreparam1().isEmpty())
            {
                // LogService.logIt( "IactnItemResp.itemScore() CCC Seq=" + this.intnResultObj.getNdseq() + "-" + this.intnItemObj.getSeq() );

                // test for POINTS tag and match.
                computeTextPointsMatch();

                // Non-null indicates that a POINTS tag was present.
                if (textPointsLst != null)
                    return this.textPointsMatchedIndex >= 0 ? (Float) textPointsLst.get(textPointsMatchedIndex)[1] : 0;

                // If No POINTS tag was present, see if it matches.
                else
                    return textBoxMatchesSample() ? intnItemObj.getItemscore() : 0;
            }

            //if( (simletCompetencyScore.getCompetencyScoreType().isPointAccum() || simletCompetencyScore.getCompetencyScoreType().isPercentOfTotal() )  &&
            //    intnItemObj.getTextscoreparam1()!= null &&
            ///    !intnItemObj.getTextscoreparam1().isEmpty() &&
            //    intnItemObj.getItemscore() != 0 )
            //{
            //   return textBoxMatchesSample() ? intnItemObj.getItemscore() : 0;
            //}


            return 0;
        }

        // If it's a TF item
        if (g2ChoiceFormatType.getIsAnyCheckbox() && intnItemObj.getCompetencyscoreid() > 0)
        {
            String v = getRespValue();

            // Selected
            if (v != null && v.equalsIgnoreCase("true"))
            {
                if (intnItemObj.getScoreparam1() > 0)
                    return intnItemObj.getItemscore();
            } // not selected
            else
            {
                if (intnItemObj.getScoreparam1() < 0)
                    return intnItemObj.getItemscore();
            }

            return 0;
        }

        return intnItemObj.getItemscore();
    }

    protected List<SimJ.Intn.Intnitem> getDragTargetCorrectTenants()
    {
        List<SimJ.Intn.Intnitem> out = new ArrayList<>();

        if (intnItemObj.getDrgtgtCorrectseqids() == null || intnItemObj.getDrgtgtCorrectseqids().isBlank())
        {
            LogService.logIt("IactnItemResp.getDragTargetCorrectTenants() item does not have any correct seqids=" + intnItemObj.getDrgtgtCorrectseqids());
            return out;
        }

        List<Integer> correctTenantSeqs = getDragTargetCorrectTenantSeqs();

        // LogService.logIt( "IactnItemResp.getDragTargetCorrectTenants() correctTenantSeqs.size=" + correctTenantSeqs.size() + ", tenantSeqs=" + getDragTargetTenantSeqs().size() );
        // for each tenant present, see if it is correct
        for (Integer i : getDragTargetTenantSeqs())
        {
            // is it correct?
            if (correctTenantSeqs.contains(i))
            {
                // LogService.logIt( "IactnItemResp.getDragTargetCorrectTenants() Seq " + i + " is correct! seeking the IactnItemResp for seq " + i );
                // find the IntnItem and add to list.
                for (IactnItemResp irp : this.iactnResp.iactnItemRespLst)
                {
                    if (irp.intnItemObj.getSeq() == i)
                    {
                        out.add(irp.intnItemObj);
                        break;
                    }
                }
            }
        }

        return out;
    }

    private float invertScore(float fv)
    {
        try
        {
            String minStr = intnItemObj.getMinpoints();

            float min = 0;

            //if( minStr == null || minStr.isEmpty() )
            //    return fv;
            if (minStr != null && !minStr.isBlank())
                min = InteractionScoreUtils.getPointsArray(minStr)[0];

            float max = getMaxPointsArray()[0];

            if (max <= min || fv < min || fv > max)
                return fv;

            return min + (max - fv);
        } catch (Exception e)
        {
            LogService.logIt(e, "IactnItemResp.invertScore() " + toString());

            return fv;
        }
    }

    @Override
    public boolean isAutoScorable()
    {

        if (!supportsSubnodeLevelSimletAutoScoring())
            return false;

        //No competency or task score
        if (getIntnItemObj().getCompetencyscoreid()<=0) // && ( iactnResp.simletScore.simletTaskScoreList==null || iactnResp.simletScore.simletTaskScoreList.isEmpty() ) )
            return false;

        if (subnodeGeneratesItsOwnPoints())
            return true;

        if (isDragTarget() && intnItemObj.getDrgtgtcheckbox()==1 && intnItemObj.getDrgtgtCorrectseqids() != null && !intnItemObj.getDrgtgtCorrectseqids().isBlank())
            return true;

        if (getSimletItemType().isPoints())
        {
            if (validItemsCanHaveZeroMaxPoints)
                return true;

            // LogService.logIt( "IactnItemResp.isAutoScorable() isPoints. MaxPointsArray=" + getMaxPointsArray() + ", intnItemObj.getMaxpoints()=" + intnItemObj.getMaxpoints() );
            return InteractionScoreUtils.hasAnyPointsValues(getMaxPointsArray());

        }

        if (getSimletItemType().isDichotomous())
        {
            if (g2ChoiceFormatType.getIsAnyCheckbox())
                return true;

            if (g2ChoiceFormatType.getIsTextBox() || g2ChoiceFormatType.getIsCombo() || g2ChoiceFormatType.getIsSliderThumb() || g2ChoiceFormatType.getIsIntnClickStream() || g2ChoiceFormatType.getIsPinImage())
                return hasCorrectRespForSubnodeDichotomousScoring();
        }

        if (getSimletItemType().isManualUpload() )
        {
            return iactnResp.getIntnObj().getCt5Int25()>0;
        }
                
        return false; // getSimletItemType().isDichotomous();
    }

    @Override
    public boolean saveAsItemResponse()
    {
        // if this item is auto scorable
        return isAutoScorable();
    }

    public boolean getIsAnyUploadedFile()
    {
        return g2ChoiceFormatType.equals(G2ChoiceFormatType.FILEUPLOADBTN) || g2ChoiceFormatType.equals(G2ChoiceFormatType.MEDIA_CAPTURE);
    }

    public boolean getIsFileUploadButton()
    {
        return g2ChoiceFormatType.equals(G2ChoiceFormatType.FILEUPLOADBTN);
    }

    public boolean getIsEssay()
    {
        return g2ChoiceFormatType.equals(G2ChoiceFormatType.TEXT_BOX);
    }

    public boolean getWasSelected()
    {
        getG2ChoiceFormatType();

        // non clickable items are not
        if (!g2ChoiceFormatType.getIsClickable() || g2ChoiceFormatType.getIsSubmit())
            return false;

        // if stored in the result object as a selected item
        if (intnResultObj.getSnseq() == intnItemObj.getSeq())
            return true;

        // if stored in the text
        getRespValue();

        if (respValue != null && !respValue.isEmpty() && g2ChoiceFormatType.responseIndicatesSelection(respValue))
            return true;

        return false;
    }

    @Override
    public List<IactnItemResp> getAllScorableIntItemResponses()
    {
        return new ArrayList<>();
    }

    @Override
    public int simletVersionId()
    {
        return iactnResp == null ? 0 : iactnResp.simletVersionId();
    }

    @Override
    public long getSimletActId()
    {
        return iactnResp == null ? 0 : iactnResp.getSimletActId();
    }

    @Override
    public long getSimletNodeId()
    {
        return iactnResp == null ? 0 : iactnResp.getSimletNodeId();
    }

    @Override
    public int getSimletNodeSeq()
    {
        return iactnResp == null ? 0 : iactnResp.getSimletNodeSeq();
    }

    @Override
    public String getSimletNodeUniqueId()
    {
        return iactnResp == null ? null : iactnResp.getSimletNodeUniqueId();
    }

    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        //LogService.logIt( "IactnItemResp.getTextAndTitleList() START " + toString() );
        List<TextAndTitle> out = new ArrayList<>();
        return out;

        /*
        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "IactnItemResp.getTextAndTitleList() AAA.1 Ct5Int25=" + iactnResp.getIntnObj().getCt5Int25() + ", summaryText=" + (summaryText==null ? "null" : summaryText.length()) );
        
        if( getIsFileUploadButton() && iactnResp.getIntnObj().getCt5Int25()==3 && summaryText!=null && !summaryText.isBlank() )
        {
            String idt = getTextAndTitleIdentifier();
            String itemid = intnItemObj.getExtitempartid();
            if (itemid == null || itemid.isBlank())
                itemid = Integer.toString(intnItemObj.getSeq());

            LogService.logIt( "IactnItemResp.getTextAndTitleList() AAA.2 Adding TextTitle. Ct5Int25=" + iactnResp.getIntnObj().getCt5Int25() + ", summaryText=" + (summaryText==null ? "null" : summaryText.length()) );
            // out.add(new TextAndTitle(summaryText, Constants.AI_SUMMARY_TEXTTITLE_KEY, false, intnItemObj.getSimcompetencyid(), iactnResp.testEvent==null ? this.getCt5ItemId() : iactnResp.testEvent.getNextTextTitleSequenceId(), null, idt + "-summary-" + "-" + itemid, null));                                            
        }
        
        return out;
        */
    }

    protected String getTextAndTitleIdentifier()
    {
        String idt = UrlEncodingUtils.decodeKeepPlus(getExtItemId());

        if( idt == null )
            idt = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getUniqueid());

        if( idt==null || idt.isEmpty() )
            idt = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getId());

        if( idt == null || idt.isEmpty() )
            idt = Integer.toString( iactnResp.intnObj.getSeq() );

        return idt;
    }
    
    
    
    @Override
    public float getResponseTime()
    {
        return iactnResp == null ? 0 : iactnResp.getResponseTime();
    }

    @Override
    public String getExtItemId()
    {
        if (intnItemObj != null)
            return intnItemObj.getExtitemid();

        return null;
    }

    @Override
    public String getSelectedExtPartItemIds()
    {
        if (intnItemObj != null)
            return intnItemObj.getExtitempartid();

        return null;
    }

    @Override
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId)
    {
        LogService.logIt("IactnItemResp.getItemScoreTextTitle() START " + toString());

        IncludeItemScoresType iist = IncludeItemScoresType.getValue(includeItemScoreTypeId);

        if (iist.isNone())
            return null;
        
        if( this.getIsFileUploadButton() )
            return null;

        String itemLevelId = UrlEncodingUtils.decodeKeepPlus(getExtItemId());
        if (itemLevelId == null || itemLevelId.isEmpty())
        {
            itemLevelId = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getUniqueid());

            if (itemLevelId == null || itemLevelId.isEmpty())
                itemLevelId = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getId());

            if (itemLevelId == null || itemLevelId.isEmpty())
                itemLevelId = Integer.toString(iactnResp.intnObj.getSeq());
        }

        String intnLevelQues = null;
        for (SimJ.Intn.Intnitem iitm : iactnResp.intnObj.getIntnitem())
        {
            if (iitm.getIsquestionstem() == 1)
            {
                intnLevelQues = StringUtils.getUrlDecodedValue(iitm.getContent());

                if (intnLevelQues != null)
                    intnLevelQues = StringUtils.truncateStringWithTrailer(intnLevelQues, 512, true);
            }
        }

        String intnItemLevelQues = null; // UrlEncodingUtils.decodeKeepPlus( getExtItemId() );
        if (intnItemObj != null && !intnItemObj.getContent().equalsIgnoreCase("TextBox"))
            intnItemLevelQues = StringUtils.truncateString(UrlEncodingUtils.decodeKeepPlus(intnItemObj.getContent()), 255); //  + " (" + intnItemObj.getSeq() + ")";

        String title = itemLevelId;

        // Now combine.
        if (intnLevelQues != null && !intnLevelQues.isBlank())
            title += "\n" + intnLevelQues;

        if (intnItemLevelQues != null && !intnItemLevelQues.isBlank())
            title += "\n" + intnItemLevelQues;

        //if( intnItemLevelQues == null || intnItemLevelQues.isEmpty() )
        //{
        //    intnItemLevelQues = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getUniqueid());
        //    if( intnItemLevelQues==null || intnItemLevelQues.isEmpty() )
        //        intnItemLevelQues = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getId());
        //    if( intnItemLevelQues == null || intnItemLevelQues.isEmpty() )
        //        intnItemLevelQues = Integer.toString( iactnResp.intnObj.getSeq() );
        //    if( intnItemObj!=null )
        //        intnItemLevelQues +=  " " + StringUtils.truncateString( UrlEncodingUtils.decodeKeepPlus(intnItemObj.getContent()), 255); //  + " (" + intnItemObj.getSeq() + ")";
        //}
        String text = null;

        if (iist.isIncludeCorrect())
            text = correct() ? "Correct" : (getPartialCreditAssigned() ? "Partial" : "Incorrect");

        else if (iist.isIncludeNumericScore())
        {
            text = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1); //  Float.toString( itemScore() );
            // text = Float.toString( itemScore() );
        } else if (iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore(itemScore());

        else if (iist.isResponseOrResponseCorrect())
        {
            //if( iactnResp!=null )
            //{
            //    String intnLevelQues = null;
            //   for( SimJ.Intn.Intnitem iitm : iactnResp.intnObj.getIntnitem() )
            //    {
            //        if( iitm.getIsquestionstem()==1 )
            //        {
            //            intnLevelQues = StringUtils.getUrlDecodedValue( iitm.getContent() );

            //            if( intnLevelQues!=null )
            //                intnLevelQues = StringUtils.truncateStringWithTrailer(intnLevelQues, 255, true );
            //        }
            //    }
            //if( intnLevelQues!=null )
            //    intnItemLevelQues = intnLevelQues + "\n" + intnItemLevelQues;
            // }
            text = StringUtils.truncateString(getRespValueForItemScore(), 255);

            // LogService.logIt("IactnItemResp.getItemScoreTextTitle() text=" + text + ", title=" + itemLevelId );
            if (iist.isResponseCorrect())
                text += " (" + (correct() ? "Correct" : (getPartialCreditAssigned() ? "Partial" : "Incorrect")) + ")";
        }

        if (text == null || text.isEmpty())
            return null;

        text = StringUtils.replaceStr(text, "[", "{");
        title = StringUtils.replaceStr(title, "[", "{");
        itemLevelId = StringUtils.replaceStr(itemLevelId, "[", "{");
        intnLevelQues = StringUtils.replaceStr(intnLevelQues, "[", "{");
        intnItemLevelQues = StringUtils.replaceStr(intnItemLevelQues, "[", "{");

        TextAndTitle tt = new TextAndTitle(text, title, intnResultObjO.getSq()*100 + orderIndex, itemLevelId, intnLevelQues, intnItemLevelQues);
        tt.setOrder(iactnResp.intnResultObjO.getSq()*100 + orderIndex);
        return tt;
    }

    @Override
    public void populateItemResponse(ItemResponse ir)
    {
        iactnResp.populateItemResponseCore(ir);

        ir.setResponseLevelId(ResponseLevelType.INTERACTIONITEM.getResponseLevelId());

        ir.setIdentifier(ResponseLevelType.INTERACTIONITEM.computeIdentifier(ir, intnItemObj.getSeq()));

        ir.setSimletSubnodeSeq(intnItemObj.getSeq());

        // ir.setTrueScore( iactnResp == null ? 0 : iactnResp.intnObj.getTruescore() );
        if (simletCompetencyScore != null)
            ir.setSimCompetencyId(simletCompetencyScore.competencyScoreObj.getSimcompetencyid());

        ir.setCompetencyScoreId(simletCompetencyId());

        // indicates response recorded, not, or timed out.
        ir.setItemResponseTypeId(getItemResponseTypeId());

        ir.setItemScore(itemScore());

        ir.setItemParadigmTypeId(ScoredItemParadigmType.getValue(this).getScoredItemParadigmTypeId());

        ir.setTrueScore(intnItemObj.getTruescore());
        ir.setScoreParam1(intnItemObj.getScoreparam1());
        ir.setScoreParam2(intnItemObj.getScoreparam2());
        ir.setScoreParam3(intnItemObj.getScoreparam3());

        if (g2ChoiceFormatType.getIsSliderThumb())
        {
            ir.setTrueScore(intnItemObj.getScoreparam1());
        }

        ir.setSelectedValue(StringUtils.truncateString(getRespValue(), 1900));
        ir.setSubnodeSeq(intnItemObj.getSeq());

        if (isDragTarget() && isAutoScorable())
        {
            ir.setSelectedSubnodeSeqIds(getRespValue());
            ir.setSubnodeFormatTypeId(G2ChoiceFormatType.CHECK_BOX.getG2ChoiceFormatTypeId());
            // ir.setSelectedSubFormatTypeIds( Integer.toString( ir.getSubnodeFormatTypeId() ) );
        } else
        {
            ir.setSelectedSubnodeSeqIds(Integer.toString(ir.getSubnodeSeq()));
            ir.setSubnodeFormatTypeId(intnItemObj.getFormat());
        }
        ir.setSelectedSubFormatTypeIds(Integer.toString(ir.getSubnodeFormatTypeId()));

        ir.setSimletItemTypeId(simletItemTypeId());

        if (getSimletItemType().isDichotomous())
        {
            ir.setCorrect(correct() ? 1 : 0);

            ir.setCorrectSubnodeSeqIds(getCorrectValue());
        } // To support item analysis, for non-dichotomous items if itemscore=max points we can consider the item correct.
        else if (simletCompetencyScore != null
                && SimCompetencyClass.getValue(simletCompetencyScore.competencyScoreObj.getClassid()).getSupportsQuasiDichotomous())
        {
            if (this.getG2ChoiceFormatType().getIsIntnClickStream() && itemScore() > 0)
                ir.setCorrect(1);

            else if (itemScore() == getMaxPointsArray()[0] && itemScore() > 0)
                ir.setCorrect(1);
        }

        ir.setSelectedSubnodeSeqIds(null);
        ir.setSelectedSubFormatTypeIds(null);

        // ir.setMetascore1( getMetaScore(1) );
        // ir.setMetascore2( getMetaScore(2) );
        // ir.setMetascore3( getMetaScore(3) );
        // ir.setMetascore4( getMetaScore(4) );
        // ir.setMetascore5( getMetaScore(5) );
        // ir.setMetascore6( getMetaScore(6) );
    }

    private Map<String, String> getComboValueLabelMap()
    {
        Map<String, String> out = new HashMap<>();

        String inStr = this.intnItemObj.getCt5Str1();

        if (inStr == null)
            return out;

        String[] arr = inStr.split("\\|");

        String n, v;
        for (int i = 0; i <= arr.length - 2; i += 2)
        {
            n = StringUtils.getUrlDecodedValue(arr[i]).trim();
            v = StringUtils.getUrlDecodedValue(arr[i + 1]).trim();

            if (n.isBlank())
                continue;

            if (v.isBlank())
                v = n;

            if (out.containsKey(v))
                continue;

            out.put(v, n);
        }

        return out;
    }

    public Intnitem getIntnItemObj()
    {
        return intnItemObj;
    }

    @Override
    public float getAggregateItemScore(SimCompetencyClass simCompetencyClass)
    {
        return 0;
    }

    /**
     * Returns - unless the scorableresponse has a textScoreParam1 of
     * [SCALEDSCOREFLOOR]value where value is the min value for the scaled
     * competency score that is to be enforced based on the fact that this
     * person made this selection.
     *
     * @return
     */
    @Override
    public float getFloor()
    {
        String s = StringUtils.getBracketedArtifactFromString(intnItemObj.getTextscoreparam1(), Constants.SCALEDSCOREFLOOR);

        return s == null || s.length() == 0 ? 0 : Float.parseFloat(s);
    }

    /**
     * Returns - unless the scorableresponse has a textScoreParam1 of
     * [SCALEDSCORECEILING]value where value is the max value for the scaled
     * competency score that is to be enforced based on the fact that this
     * person made this selection.
     *
     * @return
     */
    @Override
    public float getCeiling()
    {
        String s = StringUtils.getBracketedArtifactFromString(intnItemObj.getTextscoreparam1(), Constants.SCALEDSCORECEILING);

        return s == null || s.length() == 0 ? 0 : Float.parseFloat(s);
    }

    /**
     * Returns null - unless the scorableresponse has a textScoreParam1 of
     * [SCORETEXTCAVEAT]value sentence - where value sentence is a sentence that
     * should be appended to the scoretext for this competency in any report.
     *
     * @return
     *
     * @Override public String getCaveatText() { //if(
     * intnItemObj.getTextscoreparam1() != null &&
     * !intnItemObj.getTextscoreparam1().isEmpty() &&
     * intnItemObj.getTextscoreparam1().indexOf( "CAVEAT" )>0 ) //
     * LogService.logIt( "IactnItemResp found caveat. textScoreParam1=" +
     * intnItemObj.getTextscoreparam1() + ", output=" +
     * StringUtils.getBracketedArtifactFromString(
     * intnItemObj.getTextscoreparam1() , Constants.SCORETEXTCAVEAT ) );
     *
     * return StringUtils.getBracketedArtifactFromString(
     * intnItemObj.getTextscoreparam1() , Constants.SCORETEXTCAVEAT ); }
     */
    @Override
    public List<CaveatScore> getCaveatScoreList()
    {
        List<CaveatScore> out = new ArrayList<>();

        String s = StringUtils.getBracketedArtifactFromString(intnItemObj.getTextscoreparam1(), Constants.SCORETEXTCAVEAT);
        if (s != null && !s.isBlank())
        {
            Locale loc;
            if (intnItemObj.getLangcode() != null && !intnItemObj.getLangcode().isBlank())
                loc = I18nUtils.getLocaleFromCompositeStr(intnItemObj.getLangcode());
            else
                loc = this.iactnResp.getSimLocale();

            CaveatScore cs = new CaveatScore(0, CaveatScoreType.SCORE_TEXT.getCaveatScoreTypeId(), 0, 0, s, loc);
            out.add(cs);
        }

        return out;
    }

    @Override
    public InterviewQuestion getScoreTextInterviewQuestion()
    {
        return InterviewQuestion.getFromScoreText(intnItemObj.getTextscoreparam1());
    }

    @Override
    public boolean hasMetaScore(int i)
    {
        return false;
    }

    @Override
    public float getMetaScore(int i)
    {
        return 0;
    }

    @Override
    public boolean hasValidScore()
    {
        return true;
    }

    @Override
    public List<String> getForcedRiskFactorsList()
    {
        return forcedRiskFactorsList;
    }

    public void setForcedRiskFactorsList(List<String> forcedRiskFactorsList)
    {
        this.forcedRiskFactorsList = forcedRiskFactorsList;
    }

    @Override
    public float getTotalItemCountIncrementValue()
    {
        if (intnItemObj != null && intnItemObj.getCt5Float1() > 0)
            return intnItemObj.getCt5Float1();

        return 1;
    }

}
