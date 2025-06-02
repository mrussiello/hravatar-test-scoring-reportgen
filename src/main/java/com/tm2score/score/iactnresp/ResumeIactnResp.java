/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.iactnresp;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.ai.AiCallStatusType;
import com.tm2score.ai.AiRequestUtils;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.user.Resume;
import com.tm2score.entity.user.User;
import com.tm2score.file.BucketType;
import com.tm2score.file.FileContentType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.file.FileXferUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.user.UserFacade;
import com.tm2score.util.HtmlUtils;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.MsWordUtils;
import com.tm2score.util.PdfUtils;
import com.tm2score.util.UrlEncodingUtils;
import jakarta.json.JsonObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Mike
 */
public class ResumeIactnResp extends IactnResp implements ScorableResponse
{
    /**
     * ScoreParam3 is the maximum number of points assigned by this item. If set to zero, this value is 100.
     * TextScoreParam1 is the title of the field in the writing-sample section of the report.
     *
     */
    private static Boolean useAwsForS3;
    private static String dirBase;
    
    

    // IactnItemResp iir;
    Resume resume;
    
    User user;
    
    boolean pendingExternalScores = false;
    
    UserFacade userFacade;
    FileUploadFacade fileUploadFacade;
    
    
    public ResumeIactnResp( Clicflic.History.Intn iob, SimJ.Intn intn, TestEvent testEvent)
    {        
        super(iob, testEvent);
        
        this.intnObj=intn;
        this.user = testEvent.getUser();
        // LogService.logIt( "ResumeIactnResp () testEventId=" + testEvent.getTestEventId() );

    }
    
    private synchronized void initLocal()
    {
        if( useAwsForS3!=null )
            return;
        
        useAwsForS3 = RuntimeConstants.getBooleanValue( "useAwsMediaServer" );        
        dirBase = RuntimeConstants.getStringValue( "userFileUploadBaseDir" );   // /hra or /ful/hra or locals  
    }
    

    @Override
    public void init( SimJ sj , List<SimletScore> simletScoreList, TestEvent te, boolean validItemsCanHaveZeroMaxPoints ) throws Exception
    {
        // this will end at the search for a SimletScore.
        super.init(sj, simletScoreList, te, validItemsCanHaveZeroMaxPoints);
        
        if( simletScoreList!=null && !simletScoreList.isEmpty() )
            simletScore = simletScoreList.get(0);
        
        IactnItemResp iir;

        boolean keep;

        //boolean slctd;

        // for each interaction item
        for( SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem() )
        {
            keep = false;

            if( intnObj.getCompetencyscoreid()>0 || intnObj.getScoretype()> 0 )
            {
                if( intItemObj.getSmltiactnitmtypeid()>0)
                    keep=true;

                else if( getG2ChoiceFormatType( intItemObj.getFormat() ).supportsNodeLevelSimletAutoScoring() )
                    keep=true;

                else if( getG2ChoiceFormatType( intItemObj.getFormat() ).getIsFormInputCollector() )
                    keep = true;
            }

            // Any File Upload Button.
            else if( getG2ChoiceFormatType( intItemObj.getFormat() ).getIsFileUpload() ) // intItemObj.getRadiobuttongroup() > 0 )
                keep = true;

            // Any choice that supports subnode-level and has a competencyscoreid
            else if( getG2ChoiceFormatType( intItemObj.getFormat() ).supportsSubnodeLevelSimletAutoScoring() && intItemObj.getCompetencyscoreid() > 0 )
                keep = true;

            // for non-competency interactions
            else if( intnObj.getNoncompetencyquestiontypeid()>0 )
            {
                // Non-competency interactions must have a SimletInterationItemTypeId set in each interaction item.
                if( intItemObj.getSmltiactnitmtypeid()> 0)
                    keep=true;
            }

            // if this is the clicked interaction item, always keep.
            if( intnResultObj.getSnseq() == intItemObj.getSeq() )
                keep = true;

            if( !keep )
                continue;

            iir =  IactnRespFactory.getIactnItemResp(this, intItemObj, intnResultObjO, testEvent );

            iir.init( simletScore, te );

            if( ScoreManager.DEBUG_SCORING )
               LogService.logIt( "ResumeIactnResp.init() BBB.1 "  + this.intnObj.getName() + " " + this.intnObj.getSeq()  + " ADDING " + iir.toString() );
            iactnItemRespLst.add( iir );

            if( intnResultObj.getSnseq()>0 && intItemObj.getSeq()==intnResultObj.getSnseq() )
                clickedIactnItemResp = iir;
        }
        
        initLocal();
        
        LogService.logIt( "ResumeIactnResp.init() uniqueId=" + intnResultObj.getUnqid() + ", userId=" + te.getUserId() + ", iactnItemRespLst.size=" + iactnItemRespLst.size() + ", testEventId=" + te.getTestEventId() + ", useAwsForS3=" + useAwsForS3 );

        if( te.getUserId()<=0 )
            throw new Exception( "ResumeIactnResp.init() TestEvent.userId is invalid " + te.getUserId() );
        if( user==null  )
            loadUser( te.getUserId() );
        
        if( user==null )
            throw new Exception( "ResumeIactnResp.init() user is null for userId=" + te.getUserId());
        
        createOrUpdateResume();
    }
    
    private void createOrUpdateResume() throws Exception
    {
        try
        {
            if( resume==null )
                loadResume( user.getUserId() );
        
            LogService.logIt( "ResumeIactnResp.createOrUpdateResume() Existing resume is " + (resume==null ? "null" : "not null. resumeId=" + resume.getResumeId()) + ", userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId() ) );

            if( resume==null )
            {
                resume = new Resume();
                resume.setUserId( user.getUserId());
                resume.setOrgId( user.getOrgId());
                resume.setCreateDate(new Date());                
            }
            
            String newlySubmittedResumeText = getEssayText();
            
            // Nothing new.
            if( (newlySubmittedResumeText==null || newlySubmittedResumeText.isBlank()) && getUploadedUserFileId()<=0 )
            {
                LogService.logIt( "ResumeIactnResp.createOrUpdateResume() Resume Iactn Response has no text or upload. Returning without changing or saving anything. resume=" + (resume==null ? "null" : "not null. resumeId=" + resume.getResumeId()) + ", userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId() ) );
                return;
            }
            
            boolean isChange = false;
            
            // Text overrides upload.
            if( newlySubmittedResumeText!=null && !newlySubmittedResumeText.isBlank() )
            {
                newlySubmittedResumeText = newlySubmittedResumeText.trim();
                isChange = resume.getPlainText()==null || !resume.getPlainText().equalsIgnoreCase(newlySubmittedResumeText);
                resume.setPlainText(newlySubmittedResumeText ); 
                
                // clear upload
                resume.setUploadFilename(null);
                resume.setUploadedText(null);                
                resume.setLastInputDate( new Date() );
            }
            
            else if( getUploadedUserFileId()>0 )
            {
                String[] vals = getTextFromUploadedUserFile(getUploadedUserFileId()); 
                String uploadedFilename = vals[1];
                String newlySubmittedUploadedText = vals[0];
                
                if( newlySubmittedUploadedText==null || newlySubmittedUploadedText.isBlank() )
                {
                    LogService.logIt("ResumeIactnResp.createOrUpdateResume() unable to extract Text from uploaded resume file. userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId() ) );
                    if( newlySubmittedResumeText==null || newlySubmittedResumeText.isBlank() )
                    {
                        LogService.logIt( "ResumeIactnResp.createOrUpdateResume() Resume Iactn Response has no written text and no upload text. Returning without changing or saving anything. resume=" + (resume==null ? "null" : "not null. resumeId=" + resume.getResumeId()) + ", userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId() ) );
                        return;
                    }
                }
 
                if( newlySubmittedUploadedText!=null && !newlySubmittedUploadedText.isBlank() )
                {
                    newlySubmittedUploadedText=newlySubmittedUploadedText.trim();
                    isChange = resume.getUploadedText()==null || !resume.getUploadedText().equals( newlySubmittedUploadedText);
                    resume.setUploadFilename(uploadedFilename);
                    resume.setUploadedText(newlySubmittedUploadedText);
                    
                    // clear Plain Text
                    resume.setPlainText(null);
                    resume.setLastInputDate( new Date() );
                }
            }
            
            if( isChange )
                resume.setLastInputDate(new Date());
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            userFacade.saveResume(resume);
            
            boolean needsParse = isChange || (resume.getLastInputDate()!=null && (resume.getLastParseDate()==null || resume.getLastParseDate().before( resume.getLastInputDate())) );
            if( needsParse )
            {
                parseAndStoreResumeInfo();
            }
            
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ResumeIactnResp.createOrUpdateResume() userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId() ) );
            throw e;
        }        
    }
    
    /*
    {
      "summary": "",
      "objective": "",
      "experience": [
        {
          "title": "",
          "company": "",
          "period": "",
          "accomplishments": [ "", "", ""
          ]
        }
      ],
      "education": [
        {
          "degree": "",
          "institution": "",
          "year": ""
        }
      ]
    }    
    */
    private void parseAndStoreResumeInfo()
    {
        try
        {
            LogService.logIt("ResumeIactnResp.parseAndStoreResumeInfo() START userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId()) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
            JsonObject resJo = AiRequestUtils.parseResume( resume, user );

            if( resJo==null )
            {
                LogService.logIt("ResumeIactnResp.parseAndStoreResumeInfo() Resume Parse returned null. userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId()) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
                return;
            }

            int aiCallHistoryId = resJo.containsKey("aicallhistoryid") ? resJo.getInt("aicallhistoryid") : 0;
            int aiCallTypeId = resJo.containsKey("aicalltypeid") ? resJo.getInt("aicalltypeid") : 0;

            String status = JsonUtils.getStringFmJson(resJo, "status");

            LogService.logIt("ResumeIactnResp.parseAndStoreResumeInfo() Resume Parse returned status=" + status + ", aiCallHistoryId=" + aiCallHistoryId + ", userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId()) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
                       
            if( status.equals( AiCallStatusType.ERROR.getStatusStr() ) )
            {
                String msg = "Call to AI experienced an error. Error code=" + (resJo.containsKey("errorcode") ? resJo.getInt("errorcode") : "none") + ", Error message=" + (resJo.containsKey("errormessage") ? JsonUtils.getStringFmJson( resJo, "errormessage") : "none") + ", aiCallHistoryId=" + aiCallHistoryId;
                LogService.logIt("AdminResumeUtils.parseAndStoreResumeInfo() ERROR: " + msg + ", json returned=" + JsonUtils.convertJsonObjecttoString(resJo) );
                return;
            }

            if( !status.equals( AiCallStatusType.COMPLETED.getStatusStr() ) )
            {
                String msg = "Call to AI is in unexpected status=" + status;
                LogService.logIt("AdminResumeUtils.parseAndStoreResumeInfo() " + msg + ", aiCallHistoryId=" + aiCallHistoryId + ", json returned=" + JsonUtils.convertJsonObjecttoString(resJo) );
                return;
            }            
            
            LogService.logIt("ResumeIactnResp.handleAiCallResult() status=success, aiCallHistoryId=" + aiCallHistoryId );
            if( !resJo.containsKey("resultjo") || resJo.isNull("resultjo") )
            {
                LogService.logIt("ResumeIactnResp.parseAndStoreResumeInfo() Returned Json is completed but there is no resultjo field. aiCallHistoryId=" + aiCallHistoryId + ", aiCallTypeId=" + aiCallTypeId + ", json returned=" + JsonUtils.convertJsonObjecttoString(resJo) );
                return;
            }

            JsonObject resJo2 = resJo.getJsonObject("resultjo");
            
            if( resJo2==null || !resJo2.containsKey("resultstr") || resJo2.isNull("resultstr") )
            {
                LogService.logIt("ResumeIactnResp.parseAndStoreResumeInfo() Returned Json is completed but there is no viable resultstr field in resjo2. aiCallHistoryId=" + aiCallHistoryId + ", aiCallTypeId=" + aiCallTypeId + ", json returned=" + JsonUtils.convertJsonObjecttoString(resJo) );
                return;
            }
            
            String resultJoStr = JsonUtils.getStringFmJson(resJo2, "resultstr");
            if( resultJoStr==null || resultJoStr.isBlank() )
            {
                LogService.logIt("ResumeIactnResp.parseAndStoreResumeInfo() Returned Json is completed but there is no valid 'resultstr' element in the resultjo field. aiCallHistoryId=" + aiCallHistoryId + ", aiCallTypeId=" + aiCallTypeId + ", json returned=" + JsonUtils.convertJsonObjecttoString(resJo) );
                return;
            }
            
            
            JsonObject resultJo = JsonUtils.convertJsonStringToObject(resultJoStr);
            
            if( resultJo==null )
            {
                LogService.logIt("ResumeIactnResp.parseAndStoreResumeInfo() Returned Json is completed but there is no JsonObject in the resultjo field. aiCallHistoryId=" + aiCallHistoryId + ", aiCallTypeId=" + aiCallTypeId + ", json returned=" + JsonUtils.convertJsonObjecttoString(resJo) );
                return;
            }
            
            resume.setLastAiCallHistoryId( aiCallHistoryId );
            resume.setLastParseDate(new Date() );
            resume.setLastUpdate( new Date() );
            resume.setJsonStr( JsonUtils.convertJsonObjecttoString(resultJo));

            resume.parseJsonStr();
            
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            userFacade.saveResume(resume);                        
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ResumeIactnResp.parseAndStoreResumeInfo() userId=" + (user==null ? "null" : user.getUserId() ) + ", testEventId=" + (testEvent==null ? "null" : testEvent.getTestEventId()) + ", resumeId=" + (resume==null ? "null" : resume.getResumeId()) );
        }
    }
    
    
    
    private String[] getTextFromUploadedUserFile( long uploadedUserFileId ) throws Exception
    {
        InputStream fis = null;
        try
        {
            initLocal();            
            
            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();
            
            UploadedUserFile uuf = fileUploadFacade.getUploadedUserFile(uploadedUserFileId, true );
            
            if( uuf==null )
                throw new Exception( "Unable to load UploadedUserFile. " );
            
            String initFilename = uuf.getInitialFilename();
            String uploadedFilename = uuf.getFilename();

            if( !uploadedFilename.toLowerCase().endsWith(".pdf") && !uploadedFilename.toLowerCase().endsWith(".doc") && !uploadedFilename.toLowerCase().endsWith(".docx") && !uploadedFilename.toLowerCase().endsWith(".txt") )
                throw new Exception( "Uploaded Resume File has an unrecognized file type. uploadedFilename=" + uploadedFilename );

            String cntntHdr = uuf.getMime(); // uf.getHeader( "content-type" );

            FileContentType fct  = FileContentType.getFileContentTypeFromContentType(cntntHdr, uploadedFilename);

            if( !fct.getIsPdf() && !fct.getIsWord() && !fct.equals(FileContentType.TEXT_PLAIN) )
                throw new Exception( "Uploaded Resume File has an unrecognized file type. uploadedFilename=" + uploadedFilename + ", fileContentType=" + fct.toString());
            
            FileXferUtils fxfer = new FileXferUtils();
            
            fis = fxfer.getFileInputStream( uuf.getDirectory(), uuf.getFilename(), BucketType.USERUPLOAD.getBucketTypeId() );            
            if( fis==null )
                throw new Exception( "Could not obtain InputStream for uploaded resume file.");
            
            String text;

            if( fct.getIsPdf() )
            {
                text = PdfUtils.convertPdfToText(fis);
            }
            else if( fct.getIsWord() )
            {
                text = MsWordUtils.convertWordToText(fis, uploadedFilename);
                if( (text==null || text.isBlank()) && fct.getBaseExtension().equalsIgnoreCase("docx") )
                    LogService.logIt( "ResumeIactnResp.processUploadResumeFile() unable to parse Word document. It may be an old .doc version. uploadedFilename=" + uploadedFilename );
            }
            else
            {
                try (Scanner scanner = new Scanner(fis, StandardCharsets.UTF_8))
                {
                    text = scanner.useDelimiter("\\A").next();
                }
            }

            LogService.logIt( "ResumeIactnResp.processUploadResumeFile() text.length=" + (text==null ? "null" : text.length() ) );

            return new String[] {text,initFilename};
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ResumeIactnResp.createOrUpdateResume() uploadedUserFileId=" + uploadedUserFileId );
            throw e;
        }
        finally
        {
            if( fis!=null )
                fis.close();
        }
    }
    
    private void loadUser( long userId ) throws Exception
    {
        try
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            user = userFacade.getUser(userId);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ResumeIactnResp.loadUser() " + toString() );
            throw e;
        }
    }

    private void loadResume( long userId ) throws Exception
    {
        try
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            resume = userFacade.getResumeForUser(userId);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ResumeIactnResp.loadResume() userId=" + userId + ", " + toString() );
            throw e;
        }
    }

    
    @Override
    public String toString()
    {
        return "ResumeIactnResp{ " + ( intnObj == null ? " intn is null" :  intnObj.getName() + ", id=" + intnObj.getId() + ", nodeSeq=" + intnObj.getSeq() ) + ", ct5ItemId=" + getCt5ItemId() + ", ct5ItemPartId=" + this.getCt5ItemPartId() + "}";
    }
    
    public long getSimCompetencyId()
    {
        return 0;
    }

    public String getEssayText()
    {
        StringBuilder sb = new StringBuilder();
        String t;

        // look for an interaction item designated as the question.
        for( IactnItemResp iir : getEssayIntItemList() )
        {
            t = iir.getRespValue();
            if( t != null && !t.isBlank())
            {
                if( !sb.isEmpty() )
                    sb.append("\n\n");
                sb.append(t.trim() );
            }
        }

        return sb.toString();
    }


    public List<IactnItemResp> getEssayIntItemList()
    {
        List<IactnItemResp> out = new ArrayList<>();
        IactnItemResp iir;

        // look for an interaction item designated as the question.
        for( SimJ.Intn.Intnitem iitm : intnObj.getIntnitem() )
        {
            if( iitm.getFormat()==G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            {
                iir = IactnRespFactory.getIactnItemResp(this, iitm, intnResultObjO, testEvent ); // new IactnItemResp( this, iitm, intnResultObj );
                out.add( iir );
            }
        }

        return out;
    }
    
    private String getQuestionText()
    {
        String q = null;
        // look for an interaction item designated as the question.
        for( SimJ.Intn.Intnitem iitm : intnObj.getIntnitem() )
        {
            if( iitm.getFormat()==G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            {
                if( iitm.getQuestionid()!=null && !iitm.getQuestionid().isBlank() )
                {
                    for( SimJ.Intn.Intnitem iitm2 : intnObj.getIntnitem() )
                    {
                        if( iitm2.getId()!=null && iitm2.getId().equals( iitm.getQuestionid() ) )
                        {
                            q = UrlEncodingUtils.decodeKeepPlus( iitm2.getContent() );
                            q = HtmlUtils.removeAllHtmlTags(q);
                            break;
                        }
                    }
                }
            }
        }
        return q;
    }
    
    
    private long getUploadedUserFileId()
    {
        if( iactnItemRespLst==null || iactnItemRespLst.isEmpty() )
            return 0;
        
        for( IactnItemResp iir : iactnItemRespLst )
        {
            if( iir.getIsUploadedFile() )
            {
                // LogService.logIt( "ResumeIactnResp.getTextAndTitleList() FFF.1 IactnItemResp is UploadedFile type. " + iir.toString() );
                return iir.upldUsrFileId;
            }
        }
        return 0;
    }

    
    
    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        List<TextAndTitle> out = new ArrayList<>();

        // LogService.logIt( "ResumeIactnResp.getTextAndTitleList() starting. " + intnObj.getName() + " getSimletItemType().supportsManualScoringViaReport()=" + getSimletItemType().supportsManualScoringViaReport() + ", isNonComp=" + isNonComp );

        String title = getQuestionText();
        String t = getEssayText();

        // can happen for uploads.
        if( t==null )
            t="";
        
        String idt = getTextAndTitleIdentifier();
        
        String itemid = null;

        for( SimJ.Intn.Intnitem iitm : intnObj.getIntnitem() )
        {
            if( iitm.getFormat()==G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            {
                itemid = iitm.getExtitempartid();

                if( itemid==null || itemid.isBlank() )
                    itemid = Integer.toString( iitm.getSeq() );
                break;
            }
        }                  
        
        // LogService.logIt( "ResumeIactnResp.getTestRespList() title=" + title );
        out.add(new TextAndTitle( t, title, false, getUploadedUserFileId(), this.testEvent==null ? this.getCt5ItemId() : testEvent.getNextTextTitleSequenceId(), null, idt + "-" + itemid, null ) );

        return out;
    }
    
    
    @Override
    public TextAndTitle getItemScoreTextTitle( int includeItemScoreTypeId )
    {
        return null;
    }
    
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
        return 0;
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


    @Override
    public String getCaveatText()
    {
        return null;
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
    public float getAggregateItemScore( SimCompetencyClass simCompetencyClass )
    {
        return 0;
    }

    @Override
    public synchronized float[] getMaxPointsArray()
    {
        return new float[]{0,0,0,0};
    }


    @Override
    public boolean hasValidScore()
    {
        return false;
    }

    public ScoredItemParadigmType getScoredItemParadigmType()
    {
        return ScoredItemParadigmType.UNKNOWN;
    }
    


}
