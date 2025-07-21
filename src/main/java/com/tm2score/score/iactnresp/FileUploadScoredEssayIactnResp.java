/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.imo.xml.Clicflic;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.user.User;
import com.tm2score.essay.EssayFacade;
import com.tm2score.essay.EssayScoreStatusType;
import com.tm2score.essay.UnscoredEssayType;
import com.tm2score.file.UploadedFileHelpUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.CaveatScoreType;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.item.ScoredEssayIntnItem;
import com.tm2score.service.LogService;
import com.tm2score.simlet.SimletSubnodeType;
import com.tm2score.util.HtmlUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class FileUploadScoredEssayIactnResp extends ScoredEssayIactnResp implements ScorableResponse {

    String localeStr;
    User user;
    IactnItemResp fileUploadIactnItemResp = null;
    
    
    /*
      essayResults[0]=computedScore; 
      essayResults[1]=computedConfidence 
      essayResults[2]= Total Words
      essayResults[3]=clarity;
      essayResults[4]=argument stren;
      essayResults[5]=mechanics;
      essayResults[6]=ideal match;
      essayResults[7]=plagiarized;
    */
    float[] essayResults;
    String essaySummary;
    
    int nodeSeqId = 0;
    int subnodeSeqId = 0;
    
    long uploadedUserFileId;
    String uploadedText;  
    
    UploadedFileHelpUtils uploadedFileHelpUtils;
    
    
    EssayFacade discernFacade;
    
    
    // IactnItemResp iir;
    public FileUploadScoredEssayIactnResp(Clicflic.History.Intn iob, TestEvent testEvent)
    {
        super(iob, testEvent);
        localeStr = testEvent.getLocaleStr();
        user = testEvent.getUser();
        // LogService.logIt( "FileUploadScoredEssayIactnResp.constructor uniqueId=" + (iob==null ? "null" : iob.getU()) );        
        

    }

    @Override
    public void init(SimJ sj, List<SimletScore> simletScoreList, TestEvent te, boolean validItemsCanHaveZeroMaxPoints) throws Exception
    {
        super.init(sj, simletScoreList, te, validItemsCanHaveZeroMaxPoints);
        
        // reset
        pendingExternalScores=false;
        hasValidScore=false;
        points=0;

        // LogService.logIt( "FileUploadScoredEssayIactnResp.init() intn " + intnObj.getSeq() + ", " + intnObj.getUniqueid() );        
    }

    @Override
    public String toString()
    {
        return "FileUploadScoredEssayIactnResp{ " + (intnObj == null ? " intn is null" : intnObj.getName() + ", id=" + intnObj.getId() + ", nodeSeq=" + intnObj.getSeq()) + (intnResultObj == null ? " intnResultObj is null" : ", sel SubSeq=" + intnResultObj.getSnseq() + ", value=" + (intnResultObj.getValue()==null ? "null" : intnResultObj.getValue())) + ", ct5ItemId=" + this.getCt5ItemId() + ", uploadedUserFileId=" + this.uploadedUserFileId + "}";
    }

    @Override
    public void calculateScore() throws Exception
    {
        try
        {           
            if( hasValidScore )
                return;
            
            // LogService.logIt( "FileUploadScoredEssayIactnResp.scoring intn " + intnObj.getSeq() + ", " + intnObj.getUniqueid() );
            if (intnObj.getScoreparam3()>0)
                maxPoints = intnObj.getScoreparam3();


            int orderIndex = 1;
            for (SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem())
            {
                // Only look at Text Boxes
                if (intItemObj.getFormat()==G2ChoiceFormatType.FILEUPLOADBTN.getG2ChoiceFormatTypeId() ) // || intItemObj.getScoreparam1() <= 0)
                {
                    fileUploadIactnItemResp = IactnRespFactory.getIactnItemResp(this, intItemObj, intnResultObjO, testEvent, orderIndex );
                    break;
                }
            }

            if( fileUploadIactnItemResp==null )
            {
                LogService.logIt( "FileUploadScoredEssayIactnResp.calculateScore() AAA.1 No FileUpload intItemObj Found. Skipping. " + toString() );
                return;
            }

            nodeSeqId = intnObj.getSeq();
            subnodeSeqId = fileUploadIactnItemResp.intnItemObj.getSeq();

            // LogService.logIt( "FileUploadScoredEssayIactnResp.calculateScore() AAA.2 testEventId=" + testEvent.getTestEventId() + ", nodeSeqId=" + nodeSeqId + ",  subnodeSeqId=" + subnodeSeqId +", ct5ItemId=" + intnObj.getCt5Itemid() + ", ct5ItemPartId=" + fileUploadIactnItemResp.intnItemObj.getCt5Itempartid() );
            
            parseUploadedFile();
            if( uploadedText==null || uploadedText.isBlank() )
            {
                LogService.logIt( "FileUploadScoredEssayIactnResp.calculateScore() AAA.3 No uploaded text could be extracted from upload. Skipping. " + toString() );
                return;
            }

            calculateAiScores();

            if( pendingExternalScores )
                return;

            if( essayResults==null || essayResults.length<8 )
            {
                LogService.logIt( "FileUploadScoredEssayIactnResp.calculateScore() AAA.3 Not pending external but essayResults is " + (essayResults==null ? "null" : "length=" + essayResults.length) + ", " + toString() );
                return;
            }
            
            completeScore = essayResults[0];
            confidence = essayResults[1];
            
            if( completeScore<=0 || confidence<=0 )
            {
                LogService.logIt( "FileUploadScoredEssayIactnResp.calculateScore() AAA.5 AI Results appear invalid. completeScore=" + completeScore + ", confidence=" + confidence + ", " + toString() );
                return;                
            }
                        
            metaScores = new float[16];
            metaScores[2] = essayResults[0]; // computed
            metaScores[3] = essayResults[1]; // confidence
            metaScores[6] = intnObj.getCt5Int13()==1 ? 0 : essayResults[2]; // total words
            metaScores[8] = essayResults[7]; // plagarized
            metaScores[12] = essayResults[3]; // clarity
            metaScores[13] = essayResults[4]; // argument
            metaScores[14] = intnObj.getCt5Int13()==1 ? 0 : essayResults[5]; // mechanics
            metaScores[15] = essayResults[6]; // ideal
            caveatList2=new ArrayList<>();
            
            points = maxPoints*completeScore/100;
            
            hasValidScore = true;

            LogService.logIt( "FileUploadScoredEssayIactnResp.calculateScore() AAA.10 AI FINISH. points=" + points + ", completeScore=" + completeScore + ", confidence=" + confidence + ", " + toString() );
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FileUploadScoredEssayIactnResp.calculateScore() XXX.1 " + toString() );
            throw e;
        }
    }

    
    @Override
    public float itemScore()
    {
        return points;
    }
    

    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        boolean isNonComp = intnObj.getNoncompetencyquestiontypeid()>0;

        List<TextAndTitle> out = new ArrayList<>();

        // LogService.logIt( "FileUploadScoredEssayIactnResp.getTextAndTitleList() AAA.1 starting. " + StringUtils.getUrlDecodedValue(intnObj.getName() ) + " getSimletItemType().supportsManualScoringViaReport()=" + getSimletItemType().supportsManualScoringViaReport() + ", isNonComp=" + isNonComp );

        // must either have a non-competency type assigned, or be a competency item that has an item type that supports manual scoring.
        if( !isNonComp && (intnObj.getCompetencyscoreid()<=0 || !getSimletItemType().supportsManualScoringViaReport() ) )
        {
            // LogService.logIt( "FileUploadScoredEssayIactnResp.getTextAndTitleList() AAA.2 Skipping this IactnResp. " + StringUtils.getUrlDecodedValue(intnObj.getName() ) + " getSimletItemType().supportsManualScoringViaReport()=" + getSimletItemType().supportsManualScoringViaReport() + ", isNonComp=" + isNonComp );
            return out;
        }

        // Store question item here.
        SimJ.Intn.Intnitem q = null;

        // look for an interaction item designated as the question.
        for( SimJ.Intn.Intnitem iitm : intnObj.getIntnitem() )
        {
            if( iitm.getSmltiactnitmtypeid() == SimletSubnodeType.QUESTION.getSimletSubnodeTypeId() )
                q = iitm;
        }

        boolean redFlag = false;

        String question = null;

        String idt = getTextAndTitleIdentifier();

        if( q != null )
        {
            // If has a title, use it.
          if( q.getTitle() != null && !q.getTitle().isEmpty() )
              question = UrlEncodingUtils.decodeKeepPlus( q.getTitle() );  // iitm.title is URL Encoded

          // else, use the question content.
          else if( q.getContent() != null && !q.getContent().isEmpty() )
              question = UrlEncodingUtils.decodeKeepPlus( q.getContent() );// iitm.content is URL Encoded
        }

        List<String> values = new ArrayList<>();

        String v;

        String t;

        long upldFileId;

        // for each interaction item in response
        for( IactnItemResp iir : iactnItemRespLst )
        {
            // LogService.logIt( "FileUploadScoredEssayIactnResp.getTextAndTitleList() BBB.1 starting for iactnItemResp=" + StringUtils.getUrlDecodedValue( iir.intnItemObj.getId() ) + ", seq=" + iir.intnItemObj.getSeq() + ", iir.intnItemObj.getSmltiactnitmtypeid()=" + iir.intnItemObj.getSmltiactnitmtypeid() );
            
            // skip intn items at radio button group level
            if( iir.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                continue;

            // skip int items scored at interaction item level.
            if( iir.supportsSubnodeLevelSimletAutoScoring() && iir.isAutoScorable() && iir.intnItemObj.getSmltiactnitmtypeid()!=SimletSubnodeType.VALUE_FOR_REPORT.getSimletSubnodeTypeId() )
                continue;

            // LogService.logIt( "FileUploadScoredEssayIactnResp.getTextAndTitleList() BBB.2 "  );
            
            // If this is a value for the report.
            if( iir.intnItemObj.getSmltiactnitmtypeid()==SimletSubnodeType.VALUE_FOR_REPORT.getSimletSubnodeTypeId() )
            {
                upldFileId = iir.upldUsrFileId;

                if( iir.getIsRedFlag() )
                    redFlag = true;

                // get the value
                v = iir.getValueForReport( question );

                // get the title
                t = iir.getFieldTitleForReport( question );
                
                //if( t!=null && essaySummary!=null && !essaySummary.isBlank() )
                //    t = HtmlUtils.removeAllHtmlTags(t) + "\n\n" + essaySummary;

                // if have any value and a title
                if( v!= null && !v.isEmpty() )
                {
                    // If this interaction item does not have a field-level title, add to list.
                    if( t==null )
                        values.add( v );

                    // if it has a field-level title, we should treat it as a valid pair
                    else
                        out.add(new TextAndTitle( v , t, redFlag, upldFileId, intnResultObjO.getSq()*100 + iir.orderIndex, null, idt, null, essaySummary ) );
                }
            }
        }

        // if have values
        if( !values.isEmpty() )
        {
            if( question == null )
                question = "";

            StringBuilder sb = new StringBuilder();

            for( String val : values )
            {
                if( sb.length()>0 )
                    sb.append( ", " );

                sb.append( val );
            }

            out.add(new TextAndTitle( sb.toString() , question, redFlag, 0, intnResultObjO.getSq()*100, null, idt ) );
        }

        return out;
        
        
        /*
        List<TextAndTitle> out = new ArrayList<>();
        
        // if essay summary available, use it.
        if( essaySummary!=null && !essaySummary.isBlank() )
        {
            for (SimJ.Intn.Intnitem iitm : intnObj.getIntnitem())
            {
                if (iitm.getFormat() == G2ChoiceFormatType.FILEUPLOADBTN.getG2ChoiceFormatTypeId() ) // && iitm.getScoreparam1() > 0)
                {
                    String idt = getTextAndTitleIdentifier();
                    String itemid = iitm.getExtitempartid();
                    if (itemid == null || itemid.isBlank())
                        itemid = Integer.toString(iitm.getSeq());

                    out.add(new TextAndTitle(essaySummary, Constants.AI_SUMMARY_TEXTTITLE_KEY, false, getSimCompetencyId(), testEvent==null ? this.getCt5ItemId() : testEvent.getNextTextTitleSequenceId(), null, idt + "-summary-" + "-" + itemid, null));                                
                    break;
                }
            }            
        }
        
        return out;
        */
    }

    @Override
    public String getExtItemId()
    {
        for (SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem())
        {
            //if( intItemObj.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            //    LogService.logIt( "DataEntryIactnResp.calculateScore() HAVE INT ITEM Content=" + intItemObj.getContent() +", TextScoreParam1=" + intItemObj.getTextscoreparam1() );

            if (intItemObj.getFormat() != G2ChoiceFormatType.FILEUPLOADBTN.getG2ChoiceFormatTypeId() )// || intItemObj.getScoreparam1() <= 0)
                continue;

            if (intItemObj.getExtitemid() != null && !intItemObj.getExtitemid().isEmpty())
                return intItemObj.getExtitemid();
        }

        return null;
    }


    @Override
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId)
    {
        return null;
    }
    
        
    private void parseUploadedFile() throws Exception
    {
        try
        {            
            if( uploadedFileHelpUtils==null )
                uploadedFileHelpUtils = new UploadedFileHelpUtils();
            
            UploadedUserFile uuf = null;

            if( nodeSeqId>0 && subnodeSeqId>0 )
                uuf = uploadedFileHelpUtils.getUploadedUserFile(testEvent.getTestEventId(), nodeSeqId, subnodeSeqId);
    
            if( uuf==null )
            {
                uploadedUserFileId = uploadedFileHelpUtils.getUploadedUserFileIdFromResultXml(intnResultObj);

                if( uploadedUserFileId<=0 )
                {
                    LogService.logIt( "FileUploadScoredEssayIntnResp.parseUploadedFile() Unable to parse uploadedUserFileId from resultXml. " + intnResultObj.toString() +", " + toString());
                    return;
                }

                uuf = uploadedFileHelpUtils.getUploadedUserFile(uploadedUserFileId);
            }
            
            if( uuf==null )
                throw new Exception( "Unable to load UploadedUserFile for uploadedUserFileId=" + uploadedUserFileId );
            
            if( uuf.getTestEventId()!=this.testEvent.getTestEventId() )
                throw new Exception( "UploadedUserFile.testEventId mismatch: " + uuf.getTestEventId() + " vs TestEvent.testEventId=" + testEvent.getTestEventId());
            
            uploadedUserFileId = uuf.getUploadedUserFileId();
            
            uploadedText = uploadedFileHelpUtils.parseUploadedUserFileForText(uuf);
            
            // LogService.logIt( "FileUploadScoredEssayIntnResp.parseUploadedFile() uploadedUserFileId=" + uploadedUserFileId + ", uploadedText.length=" + (uploadedText==null ? "null" : uploadedText.length()) );            
        }
        catch( Exception e )
        {
            LogService.logIt( "FileUploadScoredEssayIntnResp.parseUploadedFile() " + toString() );
            throw e;
        }
    }
        
    
    private void calculateAiScores() throws Exception
    {
        try
        {
            if( uploadedText==null || uploadedText.isBlank() )
                throw new Exception("uploadedText is null or blank");
                            
            if( discernFacade==null )
                discernFacade=EssayFacade.getInstance();
            
            // Check for existing Unscored Essay.
            UnscoredEssay ue = discernFacade.getUnscoredEssayForMinStatus( testEvent.getTestEventId(), nodeSeqId, subnodeSeqId, EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId());

            if( ue!=null && ue.getSummaryDate()!=null && ue.getSummary()!=null && !ue.getSummary().isBlank() )
                essaySummary=ue.getSummary();
            
            if( ue!=null && ue.getEssayScoreStatusType().getIsCancelledOrHigher() )
            {
                pendingExternalScores=false;
                LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() AAA.1 UnscoredEssayScoreStatusType is in a failed or skipped state: " + ue.getEssayScoreStatusType().getName() );                
                essayResults = new float[8];
                essayResults[7]=ue.getSimilarUnscoredEssayId()>0 ? 1 : 0;
                return;
            }
            
            else if( ue!=null && ue.getEssayScoreStatusType().getIsComplete() )
            {
                pendingExternalScores=false;
                
                boolean validResult = ue.getComputedConfidence()>Constants.MIN_CONFIDENCE_AI;
                
                essayResults = new float[8];
                essayResults[0]=validResult ? ue.getComputedScore() : 0; 
                essayResults[1]=validResult ? ue.getComputedConfidence() : 0; 
                essayResults[2]=ue.getTotalWords();
                
                Map<Integer,Float> essayMetaScoreMap = ue.getMetaScoreMap();                
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.CLARITY.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId())>0)
                    essayResults[3]=essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId())>0)
                    essayResults[4]=essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.MECHANICS.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId())>0 )
                    essayResults[5]=essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.IDEAL.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId())>0 )
                    essayResults[6]=essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId());

                essayResults[7]=ue.getSimilarUnscoredEssayId()>0 ? 1 : 0;
                
                // LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() BBB.1 Found existing results. MachineScore=" + essayResults[0] + ", Confidence=" + essayResults[1] + ", Total Words=" + essayResults[2] );                
                return;                
            }
            
            else if( ue!=null && ue.getEssayScoreStatusType().submitted())
            {
                LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() CCC.1 UnscoredEssay exists in submitted status. Letting it flow through for re-submission. " + toString() );                
                //pendingExternalScores=true;
                //return;
            }

            if( ue!=null && ue.getEssayScoreStatusType().unsubmitted())
            {
                LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() DDD.1 UnscoredEssay is not yet submitted. Letting it flow through. Should submit again below." );                
            }
            
            String questionText = this.getQuestionText();
            String aiPrompt=null;
            String idealResponse=null;
            String aiInstructions=null;
            
            // Parse AI scoring text
            if( intnObj.getTextscoreparam1()!=null && !intnObj.getTextscoreparam1().isBlank() )
            {
                aiPrompt = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.AI_PROMPT_KEY );
                idealResponse = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.IDEAL_RESPONSE_KEY );
                aiInstructions = StringUtils.getBracketedArtifactFromString(intnObj.getTextscoreparam1(), Constants.AI_INSTRUCTIONS_KEY );
            }     
            
            Locale locale = localeStr==null || localeStr.isBlank() ? Locale.US : I18nUtils.getLocaleFromCompositeStr(localeStr);
            if( locale==null )
                locale = Locale.US;
            
            int maxPlagCheckRows = testEvent != null && testEvent.getOrg() != null ? ReportUtils.getReportFlagIntValue("essayscoremaxlookback", null, testEvent.getProduct(), testEvent.getSuborg(), testEvent.getOrg(), testEvent.getReport()) : 0;
            
            ScoredEssayIntnItem seii = new ScoredEssayIntnItem( UnscoredEssayType.UPLOADED_FILE.getUnscoredEssayTypeId(),
                                                       testEvent.getTestEventId(), 
                                                                user,
                                                                locale, 
                                                                teIpCountry,
                                                                nodeSeqId,
                                                                subnodeSeqId,
                                                                ScoredEssayIntnItem.DUMMY_ESSAY_PROMPT_ID, // promptId, 
                                                                intnObj.getCt5Itemid(),
                                                                0,
                                                                uploadedText, 
                                                                aiPrompt==null || aiPrompt.isBlank() ? questionText : aiPrompt,
                                                                idealResponse,
                                                                aiInstructions,
                                                                intnObj!=null && intnObj.getCt5Int13()==1, // omit grammar                    
                                                                intnObj.getCt5Int25()==2 || intnObj.getCt5Int25()==3, // include summary
                                                                intnObj.getCt5Int25()==1 || intnObj.getCt5Int25()==2, // itemLevelAiScoringOk,
                                                                0, // minWds, 
                                                                0, // maxWords, 
                                                                0, // cTime, 
                                                                0,
                                                                maxPlagCheckRows,
                                                                getWordsToIgnoreLc() ); //this.getWordsToIgnoreLc() ); // webPlagCheckOk )
            
            seii.calculate();
                       
            // if pending
            if( seii.isPendingExternalScores() )
            {
                // LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() Essay Scoring is pending. "  );
                pendingExternalScores = true;
                return;
            }
            
            pendingExternalScores = false;
            essaySummary = seii.getSummaryText();
            
            
            // This indicates a permanent failure
            if( !seii.getHasValidScore() )
            {
                LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() Score is invalid: " + seii.getErrMsg() );
                essayResults = new float[8];
                essayResults[7]=seii.getPlagiarized();
                return;
            }
            
            if( seii.getHasValidScore() )
            {
                pendingExternalScores = false;
                boolean validResult = seii.getHasValidAiComputedScore();

                essayResults = new float[8];
                essayResults[0]=validResult ? seii.getMachineScore() : 0; // 0-100
                essayResults[1]=validResult ? seii.getConfidence() : 0;   // 0-1
                essayResults[2]=seii.getTotalWords();  // 0 or 1
                
                Map<Integer,Float> essayMetaScoreMap = seii.getEssayMetaScoreMap();               
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.CLARITY.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId())>0)
                    essayResults[3]=essayMetaScoreMap.get(CaveatScoreType.CLARITY.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId())>0)
                    essayResults[4]=essayMetaScoreMap.get(CaveatScoreType.ARGUMENT.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.MECHANICS.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId())>0 )
                    essayResults[5]=essayMetaScoreMap.get(CaveatScoreType.MECHANICS.getCaveatScoreTypeId());
                if (validResult && essayMetaScoreMap.containsKey(CaveatScoreType.IDEAL.getCaveatScoreTypeId()) && essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId())>0 )
                    essayResults[6]=essayMetaScoreMap.get(CaveatScoreType.IDEAL.getCaveatScoreTypeId());

                essayResults[7]=seii.getPlagiarized();
                
                // LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() GGG.1 Successful Process. MachineScore=" + essayResults[0] + ", Confidence=" + essayResults[1] + ", Plagiarized=" + essayResults[2] );                
            }
        }
        catch( Exception e )
        {
            LogService.logIt( "FileUploadScoredEssayIntnResp.calculateAiScores() " + toString() );
            throw e;
        }
    }
    
    private String getQuestionText()
    {
        String q = null;
        if( this.fileUploadIactnItemResp!=null && fileUploadIactnItemResp.intnItemObj!=null )
        {
            if (fileUploadIactnItemResp.intnItemObj.getQuestionid()!= null && !fileUploadIactnItemResp.intnItemObj.getQuestionid().isBlank())
            {
                for (SimJ.Intn.Intnitem iitm2 : intnObj.getIntnitem())
                {
                    if (iitm2.getId() != null && iitm2.getId().equals(fileUploadIactnItemResp.intnItemObj.getQuestionid()))
                    {
                        q = UrlEncodingUtils.decodeKeepPlus(iitm2.getContent());
                        q = HtmlUtils.removeAllHtmlTags(q);
                        break;
                    }
                }
            }            
        }
        
        // look for an interaction item designated as the question.
        for (SimJ.Intn.Intnitem iitm : intnObj.getIntnitem())
        {
            if (iitm.getFormat() == G2ChoiceFormatType.FILEUPLOADBTN.getG2ChoiceFormatTypeId() )
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
    
    
    public List<String> getWordsToIgnoreLc()
    {
        List<String> out = new ArrayList<>();
        
        if( user==null )
            return out;

        if( user.getFirstName()!=null && !user.getFirstName().isBlank() )
            out.add( user.getFirstName().toLowerCase() );

        if( user.getLastName()!=null && !user.getLastName().isBlank() )
            out.add( user.getLastName().toLowerCase() );

        if( user.getEmail()!=null && !user.getEmail().isBlank() )
            out.add( user.getEmail().toLowerCase() );

        return out;        
    }


    @Override
    public boolean hasMetaScore( int i )
    {
        return switch (i)
        {
            case 2 -> true;
            case 3 -> true;
            case 6 -> true;
            case 8 -> true;
            case 12 -> true;
            case 13 -> true;
            case 14 -> true;
            case 15 -> true;
            default -> false;
        };
    }
    
    
    
}
