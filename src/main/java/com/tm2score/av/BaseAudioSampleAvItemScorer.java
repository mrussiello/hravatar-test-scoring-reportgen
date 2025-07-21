/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.ivr.item.*;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import com.tm2score.essay.AiEssayScoringUtils;
import com.tm2score.essay.EssayFacade;
import com.tm2score.essay.LocalEssayScoringUtils;
import com.tm2score.essay.UnscoredEssayType;
import com.tm2score.global.Constants;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.CaveatScoreType;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.item.ScoredEssayIntnItem;
import com.tm2score.service.LogService;
import com.tm2score.util.HtmlUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.voicevibes.VoiceVibesResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class BaseAudioSampleAvItemScorer extends BaseAvItemScorer implements AvItemScorer {
    
    private static Boolean AI_SCORING_ON;
    
    static int MIN_WORDS_FOR_SCORED_RESPONSE = 10;
    
    boolean itemLevelAiScoringOk = false;
    
    // int[0]=promptid, int[1]=min words, int[2]=max words
    public int[] essayPromptInfo;
    
    // float[0]=machine score, 
    // float[1]=machine confidence
    // float[2]=Plagiarized ? 1 : 0
    public float[] essayResults;
    
    int ct5ItemId;
    String questionText;
    String aiPrompt;
    String idealResponse;
    String aiInstructions;
    
    List<CaveatScore> caveatList2;

    
    public BaseAudioSampleAvItemScorer( Locale loc, String teIpCountry, User user, TestEvent testEvent) {
        this.locale=loc;
        this.teIpCountry=teIpCountry;
        this.user=user;
        this.testEvent=testEvent;
    }
    
    static synchronized void initVariables()
    {
        if (AI_SCORING_ON != null)
            return;

        AI_SCORING_ON = AiEssayScoringUtils.getAiEssayScoringOn();
    }
    
    
    @Override
    public void scoreAvItem( SimJ.Intn intnObj, AvItemResponse iir ) throws Exception
    {
        if (AI_SCORING_ON==null)
            initVariables();
        
        if( scoringComplete )
            return;
        
        if( iir==null )
            throw new Exception( "BaseAudioSampleAvItemScorer.scoreIvrItem() avItemResponse is null!");

        if( iir.getMediaLocale()!=null )
            locale =  iir.getMediaLocale();
        
        if( intnObj!=null )
        {
            if( intnObj.getCt5Int25()==1 )
                itemLevelAiScoringOk=true;    
            
            ct5ItemId = intnObj.getCt5Itemid();
            
            questionText = getQuestionText(intnObj);
            
            if( intnObj.getTextscoreparam1()!=null && !intnObj.getTextscoreparam1().isBlank() )
            {
                aiPrompt = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.AI_PROMPT_KEY );
                idealResponse = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.IDEAL_RESPONSE_KEY );
                aiInstructions = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.AI_INSTRUCTIONS_KEY );
            }            
        }
        
        textAndTitleList=new ArrayList<>();
        
        this.selectedValue=null;
        
        if( iir.getAvItemScoringStatusType().isSkipped() || iir.getAvItemScoringStatusType().isAnyInvalid()  )
        {
            iir.setConfidence( 0 );
            iir.setSimilarity(0);
            iir.setRawScore( 0 );
            iir.setScore( 0 );                
            iir.setAssignedPoints( 0 );

            //if( iir.getSpeechTextStatusType().isError() )
            //    iir.setScoringStatusTypeId( IvrItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId()  );                
            //else
            iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED_SKIPPED.getScoringStatusTypeId()  );
        }

        else if( iir.getAvItemScoringStatusType().isScoreError() )
        {}

        else
        {            
            // if( iir.getAudioBytes()==null || iir.getAudioBytes().length==0 )
            //     iir.setScoringStatusTypeId( IvrItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() ); 
            
            iir.setSimilarity(0);

            String text = null;

            float featuresScore = 0;
            StringBuilder sb = new StringBuilder();

            // if( iir.getSpeechTextStatusType().isComplete() )
            if( iir.getSpeechTextStatusType().isCompleteOrPermanentError() || iir.getSpeechTextStatusType().isNotRequired() )   // MJR CHANGED 05052018!!! Was if( iir.getSpeechTextStatusType().isComplete() ). Changed because speechtext errors were preventing video from appearing.
            {
                Speech2TextResult s2tr = getSpeechToTextResult( iir );

                if( s2tr!=null )
                {
                    iir.setConfidence( s2tr.getAvgConfidence() );

                    if( locale == null )
                        locale = Locale.US;

                    text = s2tr.getConcatTranscript(0, "" );

                    if( text==null )
                        text = "";
                    
                    // This is set in Tm2Convert
                    if( iir.getAvItemEssayStatusType().requiresEssayScore() )
                    {
                        if( !text.isBlank() )
                        {
                            doEssayScore(text, iir, intnObj );
                        
                            if( pendingExternalScores )
                                return;
                        }
                        
                        else
                            iir.setAvItemEssayStatusTypeId( AvItemEssayStatusType.ERROR.getEssayStatusTypeId() );
                    }
                    
                    // Next, Do error counts on the writing itself.

                    /*
                     * Returns
                     *    data[0] = total matches (errors)
                     *    data[1] = spelling errors
                     *    data[2] = grammar errors
                     *    data[3] = style errors
                     *    data[4] = total words
                     */
                    int[] errs = LocalEssayScoringUtils.getWritingErrorCount(text, locale, teIpCountry, getWordsToIgnoreLc() );

                    // ;total words;totalerrors;spelling errors;grammar errors;style errors;essay machine score;essay machine confidence;essay plagiarized flag;clarity;argument;mechanics;ideal

                    sb.append( ";" + errs[4] + ";" + errs[0] + ";" + errs[1] + ";" + errs[2] + ";" + errs[3] );

                    if( essayResults!=null && essayResults.length>=3 )
                    {
                        sb.append( ";" + essayResults[0] + ";" + essayResults[1] + ";" + essayResults[2] + ";");
                        
                        iir.setEssayMachineScore(essayResults[0]);
                        iir.setEssayConfidence(essayResults[1]);
                        iir.setEssayPlagiarized( (int) essayResults[2]);
                        
                        if( essayResults.length>=7 )
                        {
                            sb.append( essayResults[3] + ";" + essayResults[4] + ";" + essayResults[5] + ";" + essayResults[6] + ";");
                            iir.setMetaScore1( essayResults[3]);
                            iir.setMetaScoreTypeId1( CaveatScoreType.CLARITY.getCaveatScoreTypeId() );
                            iir.setMetaScore2( essayResults[4]);
                            iir.setMetaScoreTypeId2( CaveatScoreType.ARGUMENT.getCaveatScoreTypeId() );
                            iir.setMetaScore3( essayResults[5]);
                            iir.setMetaScoreTypeId3( CaveatScoreType.MECHANICS.getCaveatScoreTypeId() );
                            iir.setMetaScore4( essayResults[6]);
                            iir.setMetaScoreTypeId4( CaveatScoreType.IDEAL.getCaveatScoreTypeId() );
                        }
                        sb.append( "0;0;0;0;" );
                    }
                    else
                        sb.append( ";0;0;0;0;0;0;0;" );
                    
                    iir.setScoreStr( sb.toString() );
                    
                    /*
                    *   out[0] = total error rate
                    *   out[1] = spelling error rate
                    *   out[2] = grammar error rate
                    *   out[3] = style error rate                    
                    */
                    float[] ers = getErrorRates(errs);

                    // Similarity in this case is total error rate (errors per word).
                    iir.setSimilarity( ers[0] );

                    featuresScore = 100*computeSpeechTextQualityFactor( text, locale, ers, iir.getConfidence() );  // 0 - 100f

                    // LogService.logIt( "BaseAudioSampleAvItemScorer.scoreIvrItem() featuresScore=" + featuresScore + ", numWords=" + errs[4] + " total errors=" + errs[0] + " (" + ers[0] + " per wd), Spelling Errors: " + errs[1] + " (" + ers[1] + " per wd), grammar errors=" + errs[2] + " (" + ers[2] + " per wd), style errors=" + errs[3] + " (" + ers[3] + " per wd), Locale=" + locale.toString() + ", scoreStr=" + iir.getScoreStr() );            
                }

                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORED.getScoringStatusTypeId() );
            }
            
            //else if(  iir.getSpeechTextStatusType().isPermanentError() )
            //{
            //    
            //}
            
            else
                iir.setScoringStatusTypeId(AvItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() );
               
            
            // Parse Voice Vibes Result
            if( iir.hasVoiceVibesReport() )
            {
                VoiceVibesResult vvr = new VoiceVibesResult( iir.getVoiceVibesResponseStr(), Locale.US, StringUtils.numWords( text==null ? "" : text ) );

                // LogService.logIt( "BaseAudioSampleAvItemScorer.scoreIvrItem() Calculating Voice Vibes. avItemResponse.avItemResponseId=" + iir.getAvItemResponseId() + ", "  + vvr.toString() );

                iir.setVoiceVibesOverallScore( vvr.getOverallScore() );

                iir.setVoiceVibesOverallScoreHra( vvr.computeHRAVoiceVibesOverallScore() );   // 0 - 100f         
            }

            iir.setScoreStr( sb.toString() );
            
            float rawScore = 0;
            
            // iir.getEssayMachineScore() is 0-100
            if( iir.getAvItemEssayStatusType().isComplete() && iir.getEssayMachineScore()>0 && iir.getEssayConfidence()>0 )
                rawScore = iir.getEssayMachineScore();

            else if( iir.getVoiceVibesOverallScoreHra()>0 )
                rawScore = iir.getVoiceVibesOverallScoreHra();
            
            else if( featuresScore>0 )
                rawScore = featuresScore;
                
            iir.setRawScore(rawScore );
                        
            // LogService.logIt( "BaseAudioSampleAvItemScorer.scoreIvrItem() avItemResponseId=" + iir.getAvItemResponseId() + " featuresScore=" + featuresScore + ", iir.getVoiceVibesOverallScoreHra()=" + iir.getVoiceVibesOverallScoreHra() + ", essayScore=" + iir.getEssayMachineScore() + ", rawScore=" + iir.getRawScore() );
                        
            iir.setScore( convertRawScoreToFinal(iir.getRawScore()) );
            iir.setAssignedPoints( iir.getScore() );       
        } 
        
        
        // Prepare Text and Title
        if( iir.getSpeechTextStatusType().isCompleteOrPermanentError() || iir.getSpeechTextStatusType().isNotRequired() )
        {
            Speech2TextResult s2t = new Speech2TextResult( iir.getSpeechText() ); 

            String text = s2t.getConcatTranscript( 0, "," );

            if( text == null  )
                text = "";

            // text += "[AUDIOPB]" + this.avItemResponse.getAvItemResponseId();

            String q = AvIntnElementType.STEM1.getIntnStringElement(intnObj );

            if( (q==null || q.isEmpty()) &&  iir.getUploadedUserFileId()>0 )
            {
                SimJ.Intn.Intnitem simJSub = getSimJSubnode(intnObj, iir.getItemSubSeq() );

                // if there is a title, use that as the question for reports.
                if( simJSub!=null &&  simJSub.getTitle()!=null && !simJSub.getTitle().isBlank() )
                    q = UrlEncodingUtils.decodeKeepPlus( simJSub.getTitle(), "UTF8" );
                
                // no title, use the content as the qustion
                else if( simJSub!=null &&  simJSub.getContent()!=null && !simJSub.getContent().isBlank() )
                    q = UrlEncodingUtils.decodeKeepPlus( simJSub.getContent(), "UTF8" );
                
                else
                    q="Uploaded File";
            }

            if( q==null || q.isEmpty() )
                q = "";
      
            String idt = this.getTextAndTitleIdentifier(null, intnObj );
            
            addTextAndTitle(new TextAndTitle( text , q, false, iir.getAvItemResponseId(), testEvent==null ? 0 : testEvent.getNextTextTitleSequenceId(), iir.getSpeechTextEnglish(), idt ) );
        }
        
        scoringComplete = true;
    }
    
    
    protected SimJ.Intn.Intnitem getSimJSubnode(SimJ.Intn intn , int snSeq )
    {
        if( intn ==null )
            return null;
        
        for( SimJ.Intn.Intnitem i : intn.getIntnitem() )
        {
            if( i.getSeq()==snSeq )
                return i;
        }
        
        return null;
    }    
    
    
    private void doEssayScore( String text, AvItemResponse iir, SimJ.Intn intnObj ) throws Exception
    {
        int unscoredEssayId = 0;
        
        try
        {
            if( text==null || text.trim().isEmpty() )
                throw new Exception( "Essay Text is invalid: " + text );
                                    
            if( iir.getAvItemEssayStatusType().isNotRequired() )
                return;
            
            if( iir.getAvItemEssayStatusType().isError() )
            {
                essayResults = null;
                return;
            }

            boolean validResult = false;
            
            if( iir.getAvItemEssayStatusType().isComplete() && iir.getUnscoredEssayId()>0 )
            {
                UnscoredEssay ue = EssayFacade.getInstance().getUnscoredEssay( iir.getUnscoredEssayId() );
                
                if( ue==null )
                    throw new Exception( "UnscoredEssay not found for ID=" + iir.getUnscoredEssayId() );

                validResult = ue.getComputedConfidence()>Constants.MIN_CONFIDENCE_AI;
                
                essayResults = new float[7];
                essayResults[0]=validResult ? ue.getComputedScore() : 0; 
                essayResults[1]=validResult ? ue.getComputedConfidence() : 0; 
                essayResults[2]=ue.getSimilarUnscoredEssayId()>0 ? 1 : 0; 
                
                Map<Integer,Float> essayMetaScoreMap = ue.getMetaScoreMap();                
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.CLARITY.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId())>0)
                    essayResults[3]=essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId())>0)
                    essayResults[4]=essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.MECHANICS.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId())>0 )
                    essayResults[5]=essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.IDEAL.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId())>0 )
                    essayResults[6]=essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId());
                // LogService.logIt( "BaseAudioSampleAvItemScorer.doEssayScore() Found existing result. MachineScore=" + essayResults[0] + ", Confidence=" + essayResults[1] + ", Plagiarized=" + essayResults[2] );
                return;
                
            }

            if( essayPromptInfo==null && intnObj.getTextscoreparam1()!=null && !intnObj.getTextscoreparam1().isBlank() )
                essayPromptInfo = IvrStringUtils.getEssayScoreInfo(intnObj.getTextscoreparam1());
            
            if( (essayPromptInfo==null || essayPromptInfo[0]<=0) && (questionText==null || questionText.isBlank()) && (aiPrompt==null || aiPrompt.isBlank()) )
                throw new Exception( "No EssayPrompt, Question, or AI Prompt found in Intn. Intn.textScoreParam1=" + intnObj.getTextscoreparam1() );
            
            
            ScoredEssayIntnItem seii = new ScoredEssayIntnItem( UnscoredEssayType.AV_TRANSCRIPT.getUnscoredEssayTypeId(),
                                                       iir.getTestEventId(), 
                                                                user,
                                                                locale, 
                                                                teIpCountry,
                                                                iir.getItemSeq(),
                                                                iir.getItemSubSeq(),
                                                                essayPromptInfo==null || essayPromptInfo[0]<=0 ? ScoredEssayIntnItem.DUMMY_ESSAY_PROMPT_ID : essayPromptInfo[0], // promptId, 
                                                                ct5ItemId,
                                                                0,
                                                                text, 
                                                                aiPrompt==null || aiPrompt.isBlank() ? questionText : aiPrompt,
                                                                idealResponse,
                                                                aiInstructions,
                                                                true, // omit grammar                    
                                                                intnObj!=null && intnObj.getCt5Int24()==1,
                                                                itemLevelAiScoringOk,
                                                                essayPromptInfo==null ? 0 : essayPromptInfo[1], // minWds, 
                                                                essayPromptInfo==null ? 0 : essayPromptInfo[2], // maxWords, 
                                                                0, // cTime, 
                                                                0,
                                                                0,
                                                                this.getWordsToIgnoreLc() ); // webPlagCheckOk )
            
            seii.calculate();
            
            unscoredEssayId = seii.getUnscoredEssayId();
            
            // if pending
            if( seii.isPendingExternalScores() )
            {
                // LogService.logIt( "BaseAudioSampleAvItemScorer.doEssayScore() Essay Scoring is pending. "  );
                pendingExternalScores = true;
                setAvItemEssayStatus(iir, AvItemEssayStatusType.REQUESTED, unscoredEssayId );
                return;
            }
            
            pendingExternalScores = false;

            
            // This indicates a permanent failure
            if( !seii.getHasValidScore() )
            {
                LogService.logIt( "BaseAudioSampleAvItemScorer.doEssayScore() Score is invalid: " + seii.getErrMsg() );
                setEssayScoreError( iir, seii.getErrMsg(), unscoredEssayId ); 
                return;
            }
            
            if( seii.getHasValidScore() )
            {
                pendingExternalScores = false;
                setAvItemEssayStatus(iir, AvItemEssayStatusType.COMPLETE, unscoredEssayId );
                validResult = seii.getHasValidAiComputedScore();
            }
            
            //tMachScr += seii.getMachineScore();
            //tConf += seii.getConfidence();

            //totalWords += seii.getTotalWords();
            //spellErrors += seii.getSpellErrors();
            //otherErrors += seii.getOtherErrors();
            //plagiarized = seii.getPlagiarized()==1 ? 1 : plagiarized;
            
            if( iir.getAvItemEssayStatusType().isComplete() )
            {
                essayResults = new float[7];
                essayResults[0]=validResult ? seii.getMachineScore() : 0; // 0-100
                essayResults[1]=validResult ? seii.getConfidence() : 0;   // 0-1
                essayResults[2]=seii.getPlagiarized();  // 0 or 1
                
                Map<Integer,Float> essayMetaScoreMap = iir.getMetaScoreMap();                
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.CLARITY.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId())>0)
                    essayResults[3]=essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId())>0)
                    essayResults[4]=essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.MECHANICS.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId())>0 )
                    essayResults[5]=essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.IDEAL.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId())>0 )
                    essayResults[6]=essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId());
                // LogService.logIt( "BaseAudioSampleAvItemScorer.doEssayScore() Found existing result. MachineScore=" + essayResults[0] + ", Confidence=" + essayResults[1] + ", Plagiarized=" + essayResults[2] );
                
                LogService.logIt( "BaseAudioSampleAvItemScorer.doEssayScore() MachineScore=" + essayResults[0] + ", Confidence=" + essayResults[1] + ", Plagiarized=" + essayResults[2] );
                return;
            }
                        
            caveatList2=new ArrayList<>();
            // LogService.logIt( "ScoredEssayIactnResp.calculateScore() BBB.2 scrCt=" + scrCt + ", spellErrors=" + spellErrors + ", otherErrors=" + otherErrors + ", machineScore=" + machineScore + ", totalWords=" + totalWords + ", transCompareScore=" + transCompareScore + ", points=" + points + ", wpm=" + wpm );
            if( essayResults!=null && essayResults[2] == 1)
            {
                caveatList2.add( new CaveatScore( caveatList2.size()+1, CaveatScoreType.PLAGIARIZED.getCaveatScoreTypeId(), 1, 0, null, Locale.US));
            }
            // OK let's do this. 
            
        }
        
        catch( Exception e )
        {
            setEssayScoreError( iir, e.toString(), unscoredEssayId );
            LogService.logIt( e, "BaseAudioSampleAvItemScorer.doEssayScore() " + iir.toString() + ", Text=" + text );
            throw e;
        }
    }
    

    private void setAvItemEssayStatus( AvItemResponse iir, AvItemEssayStatusType essayStatusType, int unscoredEssayId ) 
    {
        try
        {
            if( unscoredEssayId>0 )
                iir.setUnscoredEssayId( unscoredEssayId );
            
            iir.setAvItemEssayStatusTypeId( essayStatusType.getEssayStatusTypeId() );
            
            //if( avEventFacade==null )
            AvEventFacade avEventFacade = AvEventFacade.getInstance();
            avEventFacade.saveAvItemResponse(iir);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseAudioSampleAvItemScorer.setAvItemEssayStatus() " + (iir==null ? "avItemResponse is null" : iir.toString() ));
        }
    }
    

    
    private void setEssayScoreError( AvItemResponse iir, String errMsg, int unscoredEssayId ) 
    {
        iir.appendNotes(errMsg);

        setAvItemEssayStatus( iir, AvItemEssayStatusType.ERROR, unscoredEssayId ); 
        essayResults=null;

        // If there's an error, this is no longer pending. Just skip it.
        pendingExternalScores=false;
        LogService.logIt( "BaseAudioSampleAvItemScorer.setEssayScoreError() " + (iir==null ? "avItemResponse is null" : iir.toString() ) + ", errMsg=" + errMsg );
    }
    
    
    private float[] computeMetaScores( AvItemResponse iir )
    {
        float[] metas = new float[16];
        
        if( iir==null || iir.getScoreStr()==null || iir.getScoreStr().isEmpty() )
            return metas;
        
        // ;total words;totalerrors;spelling errors;grammar errors;style errors;essay machine score;essay machine confidence;essay plagiarized flag
        String tmp = iir.getScoreStr();
        
        if( tmp.startsWith(";") )
            tmp = tmp.substring(1,tmp.length());
        
        if( tmp.endsWith(";") )
            tmp = tmp.substring(0,tmp.length()-1);
        
        String[] vals = tmp.split(";" );
        float[] errs = new float[16];
        
        for( int i=0;i<vals.length; i++ )
        {
            errs[i] = Float.parseFloat( vals[i] );
        }
            
        /*
        *   out[0] = total error rate (errors per word)  0-1
        *   out[1] = spelling error rate (errors per word)  0-1
        *   out[2] = grammar error rate (errors per word)  0-1
        *   out[3] = style error rate (errors per word)  0-1
        */
        float[] ers = getErrorRates(errs);
            
        // ;total words;totalerrors;spelling errors;grammar errors;style errors;essay machine score;essay machine confidence;essay plagiarized flag
        metas[2] = errs[0]; // total words
        metas[3] = ers[1];  // spelling error rate (0-1) 
        metas[4] = ers[2];  // grammar error rate (0-1)   
        metas[5] = ers[3];  // style error rate (0-1) 
        metas[6] = errs[5]; // Essay machine Score
        metas[7] = errs[6]; // Confidence
        metas[8] = errs[7]; // Plagiarized
        
        metas[12] = errs[8];  // clarity
        metas[13] = errs[9];  // arg
        metas[14] = errs[10]; //mech
        metas[15] = errs[11]; // ideal
        
        // LogService.logIt( "BaseAudioSampleAvItemScorer.computeMetaScores() scoreStr=" + iir.getScoreStr() + ", metas[12]=" + metas[12] + ", metas[13]=" + metas[13] + ", metas[14]=" + metas[14] + ", metas[15]=" + metas[15] );
        
        return metas;

    }
        
    @Override
    public float[] getMetaScores( AvItemResponse iir )
    {
        return computeMetaScores(iir);
    }
    
    
    
    /**
     * rawScore = errorFactor*confidence. 0=very bad. 1=very high.
     * @param rawScore
     * @return 
     */
    private float convertRawScoreToFinal( float rawScore )
    {
        if( 1==1 )
            return rawScore;

        return 0;
    }
    

    private static float[] getErrorRates( int[] errs )
    {
        float[] ers2 = new float[errs.length];
        
        for( int i=0;i<errs.length;i++ )
            ers2[i]=errs[i];
        
        return getErrorRates( ers2 );
    }
    
    /**
     * Returns error values in terms of per word. 
     *   out[0] = total error rate
     *   out[1] = spelling error rate
     *   out[2] = grammar error rate
     *   out[3] = style error rate
     *  
     * @param errs
     * @param text
     * @return 
     */
    private static float[] getErrorRates( float[] errs )
    {
        int numWords = (int) errs[4];

        float[] fractErrs = new float[4];

        if( numWords > 0 )
        {
            fractErrs[0] = ((float) errs[0])/((float) numWords );
            
            if( fractErrs[0]>1 )
                fractErrs[0]=1;
            
            fractErrs[1] = ((float) errs[1])/((float) numWords );
            fractErrs[2] = ((float) errs[2])/((float) numWords );
            fractErrs[3] = ((float) errs[3])/((float) numWords );
        } 
        
        return fractErrs;
    }
    
    /**
     * Computes weighted average of ( error factor * confidenceFactor )  and Voice Vibes Overall score. 
     * 
     * @param text
     * @param locale
     * @param errorRates
     * @param confidence
     * @return 
     */
    public static float computeSpeechTextQualityFactor( String text, Locale locale, float[] errorRates, float confidence )
    {
        if( text == null || text.isEmpty() )
            return 0;

        try
        {
            /** Adjusts confidence factor
             * confidenceFactor is = 1 if confidence 100 - 0.85, 
             *                       0.9 if confidence is 0.85 - 0.7, 
             *                       0.8 if confidence is 0.7 - 0.5; 
             *                       0.7 for confidence below 0.5
             */
            float confidenceFactor = IvrItemScoringUtils.getConfidenceFactor( confidence );  // 0.7 - 1f
            
            int numWords = StringUtils.numWords( text );

            // has words
            if( numWords >= MIN_WORDS_FOR_SCORED_RESPONSE )
            {
                // float[] errorRates = getErrorRates( errs, text );
                                
                // Let's calculate an error factor 0=Many errors, 1=no errors. 
                float errFactor = 1f - errorRates[0];  // 0 - 1f

                /*
                // No errors or almost no errors
                if( errorRates[0] <= 0.001f )
                    errFactor = 1;

                // less than .0025 per word  1 per 400
                else if( errorRates[0] <= 0.0025f)
                    errFactor = .95f;
                
                // less than .005 per word   1 per 200
                else if( errorRates[0] <= 0.005f)
                    errFactor = .90f;
                
                // less than .0075 per word   1 per 133
                else if( errorRates[0] <= 0.0075f)
                    errFactor = .85f;
                
                // less than .01 per word  100
                else if( errorRates[0] <= 0.01f)
                    errFactor = .8f;
                
                // less than .025 per word    50
                else if( errorRates[0] <= 0.02f)
                    errFactor = .7f;
                
                // less than .05 per word   25
                else if( errorRates[0] <= 0.04f)
                    errFactor = .6f;
                
                // less than .1 per word   10
                else if( errorRates[0] <= 0.1f)
                    errFactor = .5f;

                // less than .1 per word   5
                else if( errorRates[0] <= 0.2f)
                    errFactor = .4f;

                else 
                    errFactor = .2f;
                */
                
                return errFactor*confidenceFactor;  // 0 - 1f
            }
            else
                return 0;

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseAudioSampleAvItemScorer.isValidWriting() Locale=" + locale.toString() + ", Text=" + text );

            return 0;
        }


    }
    
    
    private String getQuestionText( SimJ.Intn intnObj)
    {
        String q = null;
        // look for an interaction item designated as the question.
        for (SimJ.Intn.Intnitem iitm : intnObj.getIntnitem())
        {
            if (iitm.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() && iitm.getScoreparam1() > 0)
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
    public List<CaveatScore> getCaveatScoreList()
    {
        if( caveatList2==null )
            caveatList2 = new ArrayList<>();
        
        return caveatList2;
    }
    
    
        
    @Override
    public SimJ.Intn.Intnitem getSelectedIntnItem( SimJ.Intn intn, AvItemResponse avItemResponse )
    {
        return null;
    }

}
