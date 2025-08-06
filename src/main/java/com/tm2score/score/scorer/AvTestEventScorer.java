/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.av.AvEventFacade;
import com.tm2score.av.AvItemResponsePrepThread;
import com.tm2score.av.AvItemSpeechTextStatusType;
import com.tm2score.av.AvItemType;
import com.tm2score.av.AvScoringUtils;
import com.tm2score.custom.av.AvRiskFactorType;
import com.tm2score.custom.coretest2.CT3Constants;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.ScoringException;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.score.iactnresp.ScoredAvIactnResp;
import com.tm2score.service.LogService;
import com.tm2score.sim.OverallRawScoreCalcType;
import com.tm2score.sim.OverallScaledScoreCalcType;
import com.tm2score.user.UserFacade;
import com.tm2score.voicevibes.VoiceVibesStatusType;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class AvTestEventScorer extends StandardTestEventScorer implements TestEventScorer {
    
    private static boolean deleteSourceAudios = false;
    List<UploadedUserFile> uploadedUserFileList;
    List<AvItemResponse> avItemResponseList;
    AvScoringUtils avScoringUtils;
        
    AvEventFacade avEventFacade;
    FileUploadFacade fileUploadFacade;
    
    @Override
    public void scoreTestEvent( TestEvent testEvent, SimDescriptor simDescriptor, boolean skipVersionCheck) throws Exception
    {
       try
       {
           te = testEvent;
           sd = simDescriptor;

            // LogService.logIt( "AvTestEventScorer.scoreTestEvent() starting process. TestEventId=" + te.getTestEventId() );

            initForScoring(false);

            ///////////////////////////////////////////////////////////////////////////////////////////////
            // AT THIS POINT, IT SEEMS LIKE WE SHOULD BE ABLE TO SCORE!
            ///////////////////////////////////////////////////////////////////////////////////////////////

            setReportLocale();
            
            initTestEvent();
            
            loadUploadedUserFiles();
            
            loadAvItemResponses();
            
            // Do this after the UUFs and IIRs are loaded to make sure UniqueIds are set. They are not set until the first time they pass through this scoring system. 
            checkAvItemResponseAndUploadedUserFileUniqueIds();
            
            setAvItemResponseLocalesIfNeeded();
                        
            if( avScoringUtils==null )
                avScoringUtils = new AvScoringUtils();
            
            correctVoiceVibesStatusInAvItemResponses();

            // LogService.logIt( "AvTestEventScorer.scoreTestEvent() BBB uploadedUserFileList=" + uploadedUserFileList.size() + ", avItemResponseList=" + avItemResponseList.size() + ", TestEventId=" + te.getTestEventId() );
            
            int uufsWaitingForConversion = avScoringUtils.countUploadedUserFilesNotReadyForScoring( uploadedUserFileList, avItemResponseList );

            // LogService.logIt( "AvTestEventScorer.scoreTestEvent() CCC uufsWaitingForConversion=" + uufsWaitingForConversion + ", TestEventId=" + te.getTestEventId() );
            
            if( uufsWaitingForConversion>0  )
            {
                // need to wait.
                scrSum.append( "Pending External Scores. " + uufsWaitingForConversion + " UploadedUserFiles still waiting for conversion.\n" );
                te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
                //LogService.logIt( "AvTestEventScorer.scoreTestEvent() EXITING - Test Event is Pending External Scores. UploadedUserFile conversions TestEventId=" + te.getTestEventId() + ". There are " + uufsWaitingForConversion + " UploadedUserFiles still waiting for conversion." );
                //LogService.logIt( scrSum.toString() );
                return;                
            }
            
            // at this point all UUFs are converted. So now check that we have set the orientation for them. 
            avScoringUtils.checkVideoOrientations( uploadedUserFileList ); 
            
            Locale mediaLocale = te.getProduct() != null ? te.getProduct().getLocaleFmLangStr() : getLocale();
                                    
            // IVR and VOT use Product.strParam6 to designate the locale the content - the language the person should be speaking in. 
            //if( te.getProduct()!=null && ( te.getProduct().getProductType().getIsIvr() || te.getProduct().getProductType().getIsVot() ) && te.getProduct().getStrParam6()!=null && !te.getProduct().getStrParam6().isEmpty() )
            //    mediaLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam6() );
            
            // Sims with recorded media language is in p.strParam7
            if( te.getProduct()!=null && te.getProduct().getProductType().getIsSimOrCt5Direct() && te.getProduct().getStrParam7()!=null && !te.getProduct().getStrParam7().isBlank() )
                mediaLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam7() );
            
            boolean forceVibesOff = reportRules!=null && reportRules.getReportRuleAsBoolean("voicevibesoff");
            // boolean forceNonEssentialTranscriptsOff = reportRules!=null && reportRules.getReportRuleAsBoolean("transcriptionoff");
            
            int unreadyAvItemResponseCount = avScoringUtils.getAvItemResponsesNotReadyForScoring(uploadedUserFileList, avItemResponseList, getLocale(), mediaLocale, forceVibesOff, te.getSimXmlObj() );
            // LogService.logIt( "AvTestEventScorer.scoreTestEvent() DDD unreadyAvItemResponseCount=" + uufsWaitingForConversion + ", unreadyAvItemResponseCount="+ unreadyAvItemResponseCount + ", TestEventId=" + te.getTestEventId() );
            
            if( unreadyAvItemResponseCount>0 )
            {
                finalizeAvItemResponsesForScoring();
                
                scrSum.append( "Finalizing AVItemResponses. " + unreadyAvItemResponseCount + " AvItemResponses not yet ready for scoring yet.\n" );
                te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
                //LogService.logIt( "AvTestEventScorer.scoreTestEvent() EXITING - Test Event is Finalizing AVItemResponses. AvItemResponses. TestEventId=" + te.getTestEventId() + ", " + unreadyAvItemResponseCount + " AvItemResponses not yet ready." );
                //LogService.logIt( scrSum.toString() );
                return;                                
            }
                     
            // Go through and score each of the items. Save in the avItemResponse.
            
            boolean englishTranslationsOff = ReportUtils.getReportFlagBooleanValue( "englishtransoff", te.getTestKey(), te.getProduct(), te.getSuborg(), te.getOrg(), te.getReport() );
            
            
            // NOTE - Scoring Items will start the Essay Scoring process if needed for an Item. 
            // Essay scoring is NOT NEEDED to finalize an AvItemResponse.
            avScoringUtils.scoreItems(te, avItemResponseList, getLocale(), englishTranslationsOff );

            int pendingAvItemResponseCount = avScoringUtils.getAvItemResponsesPendingScoring(avItemResponseList);
            // LogService.logIt( "AvTestEventScorer.scoreTestEvent() EEE pendingAvItemResponseCount=" + pendingAvItemResponseCount + ", TestEventId=" + te.getTestEventId() );
            
            if( pendingAvItemResponseCount>0 )
            {
                scrSum.append( "Pending External Scores (typically Essay scores). " + pendingAvItemResponseCount + " AvItemResponses in Pending status.\n" );
                te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
                //LogService.logIt( "AvTestEventScorer.scoreTestEvent() EXITING - Test Event is Pending External Scores(typically Essay scores). AvItemResponses. TestEventId=" + te.getTestEventId() + ", " + pendingAvItemResponseCount + " AvItemResponses in Pending status." );
                //LogService.logIt( scrSum.toString() );
                return;                                
            }
            
            updateObjectsInIactnResponses();
            
            // boolean hasWeights = false;
            scoreSimCompetencies();

            // re-save IvrItem Responses.
            avScoringUtils.saveAllAvItemResponses(avItemResponseList);
            
            
            // LogService.logIt( "AvTestEventScorer.scoreTestEvent() Ready to calculate overall score. ovrRawScr=" + ovrRawScr + ", totalWeights=" + totalWeights + ", testEventId=" + te.getTestEventId() + ", pendingExternalScores=" + pendingExternalScores );

            // Pending for yet another reason.
            if( pendingExternalScores )
            {
                scrSum.append( "Pending External Scores. \n" );
                te.setTestEventStatusTypeId( TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() );
                eventFacade.saveTestEvent(te);
                //LogService.logIt( "AvTestEventScorer.scoreTestEvent() EXITING - Test Event is Pending External Scores POST Competencies. TestEventId=" + te.getTestEventId() );
                //LogService.logIt( scrSum.toString() );
                return;
            }

            calculateMetaScores();            
            
            calculateOverallScores();
            
            int counter = 1;

            setOverallScoreAndPercentile( counter );

            counter = setCompetencyTestEventScores(counter);

            counter = setCompetencyGroupTestEventScores(counter);

            setItemResponses(te.getAutoScorableResponseList());
            
            finalizeRemoteProctorScoring();
            
            finalizeScore();                    
            
            //initiateAiMetaScores();
            
            // LogService.logIt( "AvTestEventScorer.scoreTestEvent() COMPLETED SCORING te.scoreFormatTypeId=" + te.getScoreFormatTypeId() + ", teId=" + te.getTestEventId() );
       }

       catch( ScoringException e )
       {
           LogService.logIt( e, "AvTestEventScorer.scoreTestEvent() "  + te.toString() + "\n" + (scrSum!=null ? scrSum.toString() : "" ) );
           throw e;
       }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt( e, "AvTestEventScorer.scoreTestEvent() "  + te.toString() );
           
           throw new ScoringException( e.getMessage() + "AvTestEventScorer.scoreTestEvent() " + "\n" + (scrSum!=null ? scrSum.toString() : "" ) , ScoreUtils.getExceptionPermanancy(e) , te );
       }
    }


    /**
     * Conversion program sets VibesStatus to Not Required or Not Started but doesn't know Locale. So, if set to Not Started here we need to 
     * Check the locale of the content to make sure VoiceVibes will work. 
     * @throws Exception 
     */
    protected void correctVoiceVibesStatusInAvItemResponses() throws Exception
    {
            if( avItemResponseList==null )
                throw new Exception( "AvTestEventScorer.correctVoiceVibesStatusInAvItemResponses() avItemResponseList is null. ");

            Locale testAdminLocale = te.getLocaleStr()==null || te.getLocaleStr().isEmpty() ? simLocale : I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() );
            
            Locale testContentLocale = te.getProduct() != null ? te.getProduct().getLocaleFmLangStr() : testAdminLocale;
            
            // IVR and VOT use Product.strParam6 to designate the locale the content - the language the person should be speaking in. 
            //if( te.getProduct()!=null && ( te.getProduct().getProductType().getIsIvr() || te.getProduct().getProductType().getIsVot() ) && te.getProduct().getStrParam6()!=null && !te.getProduct().getStrParam6().isEmpty() )
            //        testContentLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam6() );

            // Sims with recorded media language is in p.strParam7
            if( te.getProduct()!=null && te.getProduct().getProductType().getIsSimOrCt5Direct() && te.getProduct().getStrParam7()!=null && !te.getProduct().getStrParam7().isBlank() )
                testContentLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam7() );
            
            if( testContentLocale==null || testContentLocale.getLanguage().equalsIgnoreCase("en") )
                return;

            
            // At this point, VoiceVibesStatus must be OFF.
            
            // make sure it's off 
            for( AvItemResponse iir : avItemResponseList )
            {
                if( !iir.getVoiceVibesStatusType().equals( VoiceVibesStatusType.NOT_REQUIRED ))
                {
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                    
                    if( avEventFacade==null )
                        avEventFacade = AvEventFacade.getInstance();
                    
                    avEventFacade.saveAvItemResponse(iir);
                }
            } 
    }

    
    
    protected void finalizeAvItemResponsesForScoring() throws Exception
    {
            if( avItemResponseList==null )
                throw new Exception( "AvTestEventScorer.finalizeItemResponsesForScoring() avItemResponseList is null. ");

            int notReadyCount = 0;
            
            //Locale testAdminLocale = te.getLocaleStr()==null || te.getLocaleStr().isEmpty() ? simLocale : I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() );
            
            //Locale testContentLocale = te.getProduct() != null ? te.getProduct().getLocaleFmLangStr() : testAdminLocale;
            
            // Pass 1 - set Ready Remotely if needed. 
            for( AvItemResponse iir : avItemResponseList )
            {
                // Disable AI if org doesn't have AI Enabled.
                if( !te.getOrg().getTestAiOk() )
                {
                    AvItemType ivrItemType = iir.getAvItemType();
                    
                    // No Voice Vibes any Circumstances. 
                    if( te.getProduct()==null || te.getProduct().getIntParam13()<=0 )
                        iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                                        
                    // No speech text if it's a video interview question.
                    if( ivrItemType==null || ivrItemType.equals( AvItemType.TYPE101) || ivrItemType.equals( AvItemType.TYPE102) )
                        iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                                        
                    if( avEventFacade==null )
                        avEventFacade = AvEventFacade.getInstance();
                    avEventFacade.saveAvItemResponse(iir);
                }
                
                // Already ready for scoring.
                if( !iir.isReadyForScoring() ) // || iir.needsSpeechTranslation( simLocale ) )
                {
                    notReadyCount++;
                }
            } 
            
            if( notReadyCount>0 )
	        LogService.logIt( "AvTestEventScorer.finalizeItemResponsesForScoring() notReadyCount=" + notReadyCount + ", testEventId=" + te.getTestEventId() );
            
            if( notReadyCount==0 )
                return;

            boolean forceVibesOff = reportRules!=null && reportRules.getReportRuleAsBoolean("voicevibesoff");
            boolean forceNonEssentialTranscriptsOff = reportRules!=null && reportRules.getReportRuleAsBoolean("transcriptionoff");

            
            // Create a thread to do the prep. 
            AvItemResponsePrepThread iirPrepThread = new AvItemResponsePrepThread( te, deleteSourceAudios ? false : true, forceVibesOff, forceNonEssentialTranscriptsOff );
            
            // int inProgCountThisBatch = IvrPrepBatchManager.getPrepInProgressCount();
            
            //if( inProgCountThisBatch > 0 )
            iirPrepThread.setInitialDelaySecs( 0 );
            // ivrPrepThread.setInitialDelaySecs( inProgCountThisBatch*30 );
            
            // Slow down a bit (10 secs)
            Thread.sleep(10*1000);
            
            new Thread(iirPrepThread).start();
    }
    
    
    
    protected void updateObjectsInIactnResponses() throws Exception
    {
        ScoredAvIactnResp sair;
        
        UploadedUserFile uuf;
        AvItemResponse iir;
        
        for( ScorableResponse sr : te.getAutoScorableResponseList() )
        {
            if( sr instanceof ScoredAvIactnResp )
            {
                sair = (ScoredAvIactnResp) sr;
                iir = null;
                uuf = avScoringUtils.getUploadedUserFileForItem( sair.getIntnObj().getUniqueid(), sair.getIntnObj().getSeq(), uploadedUserFileList, avItemResponseList );
                
                if( uuf==null )
                {
                    LogService.logIt( "AvTestEventScorer.updateObjectsInIactnResponses() NONFATAL Cannot find UploadedUserFile for ScoredAvIactnResp " + sair.toString() );
                    
                    iir = avScoringUtils.getAvItemResponseForItemUniqueId( sair.getIntnObj().getUniqueid(), avItemResponseList);
                    
                    if( iir!=null && iir.getUploadedUserFileId()>0 )
                    {
                        for( UploadedUserFile u : uploadedUserFileList )
                        {
                            if( u.getUploadedUserFileId()==iir.getUploadedUserFileId() )
                            {
                                uuf=u;
                                
                                if( u.getNodeUniqueId()==null || u.getNodeUniqueId().isEmpty() )
                                {
                                    u.setNodeUniqueId( sair.getIntnObj().getUniqueid() );
                                    
                                    if( this.fileUploadFacade==null )
                                        fileUploadFacade = FileUploadFacade.getInstance();
                                    
                                    fileUploadFacade.saveUploadedUserFile(u);
                                }    
                                break;
                            }
                        }
                    }
                    
                    // continue;
                    // throw new Exception( "updateObjectsInIactnResponses() Cannot find UploadedUserFile for ScoredAvIactnResp " + sair.toString() );
                }

                if( uuf!=null && !uuf.getFileProcessingType().requiresAvItemResponse() )
                    continue;
                
                if( uuf!= null && iir==null  )
                    iir = avScoringUtils.getAvItemResponseForItem( uuf.getUploadedUserFileId(), avItemResponseList );
                
                if( iir==null )
                {
                    LogService.logIt( "AvTestEventScorer.updateObjectsInIactnResponses() NONFATAL Cannot find AvItemResponse for ScoredAvIactnResp " + sair.toString() + ", UploadedUserFileId=" + ( uuf==null ? "UUF is null" : uuf.getUploadedUserFileId()) );
                }
                
                sair.initAv( iir, getLocale(), te );
            }
        }
    }            
        
    protected void loadUploadedUserFiles() throws Exception
    {
        if( fileUploadFacade == null )
            fileUploadFacade = FileUploadFacade.getInstance();
        
        uploadedUserFileList = fileUploadFacade.getUploadedUserFilesForTestEvent(te.getTestEventId(), -1 );        
    }
    
    /**
     * Note that UniqueIds are not set into AvItemResponses and UploadedUserFile objects by the test engine or file conversion system. 
     * @throws Exception 
     */
    protected void checkAvItemResponseAndUploadedUserFileUniqueIds() throws Exception
    {
        if( avItemResponseList!=null )
        {
            for( AvItemResponse iir : avItemResponseList )
            {
                if( iir.getItemUniqueId()==null || iir.getItemUniqueId().isEmpty() )
                {
                    if( te.getSimXmlObj()!=null )
                    {
                        for( SimJ.Intn intn : te.getSimXmlObj().getIntn() )
                        {
                            if( intn.getSeq()==iir.getItemSeq() )
                            {
                                // if there is a unique id in the intn. 
                                if( intn.getUniqueid()!=null && !intn.getUniqueid().isEmpty() )
                                {
                                    iir.setItemUniqueId( intn.getUniqueid() );

                                    if( avEventFacade==null )
                                        avEventFacade = AvEventFacade.getInstance();
                                    
                                    avEventFacade.saveAvItemResponse(iir);
                                    
                                    if( iir.getUploadedUserFileId()>0 && uploadedUserFileList!=null )
                                    {
                                        for( UploadedUserFile uuf : uploadedUserFileList )
                                        {
                                            if( uuf.getUploadedUserFileId()==iir.getUploadedUserFileId() )
                                            {
                                                // needs a unique id
                                                if( uuf.getNodeUniqueId()==null || uuf.getNodeUniqueId().isEmpty() )
                                                {
                                                    uuf.setNodeUniqueId( iir.getItemUniqueId());

                                                    if( fileUploadFacade==null )
                                                        fileUploadFacade = FileUploadFacade.getInstance();

                                                    fileUploadFacade.saveUploadedUserFile(uuf);

                                                }

                                                // don't need to check other UUFs
                                                break;
                                            }
                                        }
                                    }
                                }

                                // Don't need to check other Intns
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        if( uploadedUserFileList==null )
            return;
        
        // Not all UploadedUserFile objects have AvItemResponses associated. They may just be simple file uploads, so still go through. 
        for( UploadedUserFile uuf : uploadedUserFileList )
        {
            if( uuf.getNodeUniqueId()==null || uuf.getNodeUniqueId().isEmpty() )
                updateNodeUniqueIdForUploadedUserFile( uuf );
        }        
    }
    
    
    protected void setAvItemResponseLocalesIfNeeded() throws Exception
    {
        if( avItemResponseList!=null )
        {
            SimJ.Intn intn;
            G2ChoiceFormatType gft;
            
            Locale testAdminLocale = null;        
            Locale testContentLocale = null;

            testAdminLocale = te.getLocaleStr()!=null && !te.getLocaleStr().isEmpty() ? I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() ) : Locale.US;        

            if( te.getProduct() != null  && !te.getProduct().getLangStr().isEmpty() )
                testContentLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getLangStr() );
            else
                testContentLocale = testAdminLocale;
        
            Locale mediaLocale = null;
            
            // IVR and VOT use Product.strParam6 to designate the locale the content - the language the person should be speaking in.
            // However, the country that the person is speaking in may also be needed. 
            //if( te.getProduct()!=null && ( te.getProduct().getProductType().getIsIvr() || te.getProduct().getProductType().getIsVot() ) && te.getProduct().getStrParam6()!=null && !te.getProduct().getStrParam6().isBlank() )
            //    mediaLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam6() );
            
            // Sims with recorded media language is in p.strParam7
            if( te.getProduct()!=null && te.getProduct().getProductType().getIsSimOrCt5Direct() && te.getProduct().getStrParam7()!=null && !te.getProduct().getStrParam7().isBlank() )
                mediaLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam7() );
            
            if( mediaLocale==null )
                mediaLocale=testContentLocale;
            
            for( AvItemResponse iir : avItemResponseList )
            {
                if( iir.getLangCode()==null || iir.getLangCode().isBlank() )
                {
                    // Find the matching Interaction.
                    intn=null;
                    for( SimJ.Intn itn : te.getSimXmlObj().getIntn() )
                    {
                        if( itn.getSeq()==iir.getItemSeq() )
                        {
                            intn=itn;
                            break;
                        }
                    }
                    
                    if( intn!=null )
                    {
                        // Find the media capture Interaction Item
                        for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
                        {
                            // See if there is a specific langCode setting for this interaction item.
                            gft = G2ChoiceFormatType.getValue( ii.getFormat() );
                            if( gft!=null && (gft.getIsFileUpload() || gft.getIsMediaCapture()) && ii.getLangcode()!=null && !ii.getLangcode().isBlank() )
                            {
                                // save the code.
                                iir.setLangCode( ii.getLangcode() );
                                break;
                            }
                        }
                    }

                    // still not found
                    if( iir.getLangCode()==null || iir.getLangCode().isBlank() )
                        iir.setLangCode( mediaLocale.toString() );
                    
                    if( avEventFacade==null )
                        avEventFacade = AvEventFacade.getInstance();

                    avEventFacade.saveAvItemResponse(iir);
                }  // Next iir
                
                
            }
        }
        
    }
    
    protected void updateNodeUniqueIdForUploadedUserFile(UploadedUserFile uuf ) throws Exception
    {
        if( uuf==null || ( uuf.getNodeUniqueId()!=null && !uuf.getNodeUniqueId().isEmpty() ) )
            return;
        
        if( avItemResponseList!=null && uuf.getAvItemResponseId()>0 )
        {
            for( AvItemResponse iir : avItemResponseList )
            {
                if( iir.getAvItemResponseId()==uuf.getAvItemResponseId() )
                {
                    if( iir.getItemUniqueId()!=null && !iir.getItemUniqueId().isEmpty() )
                    {
                        uuf.setNodeUniqueId( iir.getItemUniqueId());
                        
                        if( fileUploadFacade==null )
                            fileUploadFacade = FileUploadFacade.getInstance();
                        
                        fileUploadFacade.saveUploadedUserFile(uuf);
                        
                        return;
                    }
                    
                    // no unique id there. So just break. 
                    break;
                }
            }
        }
        
        // at this point, must match on NodeSeq
        if( te.getSimXmlObj()!=null )
        {
            for( SimJ.Intn intn : te.getSimXmlObj().getIntn() )
            {
                if( intn.getSeq()==uuf.getNodeSeq() )
                {
                    if( intn.getUniqueid()!=null && !intn.getUniqueid().isEmpty() )
                    {
                        uuf.setNodeUniqueId( intn.getUniqueid() );
                        
                        if( fileUploadFacade==null )
                            fileUploadFacade = FileUploadFacade.getInstance();
                        
                        fileUploadFacade.saveUploadedUserFile(uuf);
                        
                        return;                        
                    }
                    
                    break;
                }
            }
        }
        
        // if get to this point, no unique found
        LogService.logIt( "AvTestEventScorer.updateNodeUniqueIdForUploadedUserFile() No UniqueId found for " + uuf.toString() + " testEventId=" + te.getTestEventId() );
    }
    
    protected void loadAvItemResponses() throws Exception
    {
        if( avEventFacade == null )
            avEventFacade = AvEventFacade.getInstance();
                
        avItemResponseList = avEventFacade.getAvItemResponsesForTestEventId(te.getTestEventId() );
        
        // update the displayorders.
        int count=1;
        
        for( AvItemResponse iir : avItemResponseList )
        {
            if( iir.getDisplayOrder()!=count )
            {
                iir.setDisplayOrder(count);
                avEventFacade.saveAvItemResponse(iir);
            }
            count++;
        }
    }
    
    
    public Locale getLocale()
    {
        if( te.getLocaleStr()!=null && !te.getLocaleStr().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() );
        
        if( this.simLocale!=null )
            return this.simLocale;
        
        return Locale.US;
        // return te.getLocaleStr()!=null && !te.getLocaleStr().isEmpty() ? I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() ) : Locale.US;
    }
    
    
    
    @Override
    public void initTestEvent() throws Exception
    {
        super.initTestEvent();
        
        
            // LogService.logIt( "BaseTestEventScorer.scoreTestEvent() starting to score TestEvent " + te.toString() + ", simXmlObj contains " + te.getSimXmlObj().getSimlet().size() + " simlets. reportLocale=" + reportLocale.toString() + ", te.getSimXmlObj().getScoreformat()=" + te.getSimXmlObj().getScoreformat() );

            // Indicate scoring has started.
            te.setTestEventStatusTypeId( TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() );

            te.setScoreFormatTypeId( te.getSimXmlObj().getScoreformat() );                                    
            te.setScoreColorSchemeTypeId( te.getSimXmlObj().getScorecolorscheme() );
            te.setEducTypeId( te.getSimXmlObj().getEduc() );
            te.setExperTypeId( te.getSimXmlObj().getExper() );
            te.setTrainTypeId( te.getSimXmlObj().getTrn() );

            setExcludeFmNorms();
            
            Date startDate = te.getStartDate();
            Date lastDate = te.getLastAccessDate();
            
            if( te.getTotalTestTime()<=0 && startDate!=null && lastDate!=null && lastDate.getTime()>startDate.getTime() )
            {
                float ttime = ( (float)(lastDate.getTime() - startDate.getTime()) )/1000f;
                
                te.setTotalTestTime( ttime );
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
            
            // OK initialize all the score data.
            te.initScoreAndResponseLists( validItemsCanHaveZeroMaxPoints );
            
            for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
                scs.setReportData(getReportDataForScoring());
            
            
            scoreFormatTypeId = te.getScoreFormatTypeId(); //  te.getSimXmlObj().getScoreformat();

            scoreColorSchemeType = ScoreColorSchemeType.getValue( te.getSimXmlObj().getScorecolorscheme() );

            // LogService.logIt("BaseTestEventScorer.score() te.getSimXmlObj().getScorecolorscheme()=" + te.getSimXmlObj().getScorecolorscheme() );
            
            overallRawScoreCalcType = OverallRawScoreCalcType.getValue( te.getSimXmlObj().getOverallscorecalctype() );

            overallScaledScoreCalcType = OverallScaledScoreCalcType.getValue( te.getSimXmlObj().getOverallscaledscorecalctype() );

            // boolean pendingExternalScores = false;

            // int scsNotScorableCt = 0;

            // int scsScorableCt = 0;
            //  int scsScorableZeroCt = 0;
            // float scsScr;
            //float wtUsed;
            
            // float weightPct = 0;            

            // String competencyName;

            for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
            {
                if( scs.getSimCompetencyObj().getWeight()>0 )
                {
                    hasWeights=true;
                    break;
                }
            }                
    }
    
    
    @Override
    public String getAdditionalTextScoreContentPacked() throws Exception
    {

        try
        {

            // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() AAA rfstr=" + rfstr );

            StringBuilder sb = new StringBuilder();

            Locale locale = reportLocale; //  te.getReport()!=null && te.getReport().getLocaleForReportGen()!=null ? te.getReport().getLocaleForReportGen() : I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() ); // I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

            //List<String> ids = new ArrayList<>();
            //List<String> riskFactors = new ArrayList<>();

            // String riskTxt;

            for( AvRiskFactorType ct3Rft : AvRiskFactorType.values() )
            {
                if( !ct3Rft.isStandard() )
                    continue;

                // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() TESTING FOR " + ct3Rft.name() );

                if( ct3Rft.isRiskFactorPresent( te, null ) )
                {
                    // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() PRESENT " + ct3Rft.name() );
                    if( sb.length() > 0 )
                        sb.append( Constants.DELIMITER );

                    sb.append( ct3Rft.getAvRiskFactorTypeId() + Constants.DELIMITER + ct3Rft.getRiskText( locale, null ) + "[FACET]riskfactorid:" + ct3Rft.getAvRiskFactorTypeId() + Constants.DELIMITER  );
                    // riskFactors.add( MessageFactory.getStringMessage( locale , ct3Rft.getKey()) );
                }
            }


            if( sb.length() > 0 )
                return ";;;" + CT3Constants.CT3RISKFACTORS + ";;;" + Constants.DELIMITER + sb.toString();

            return "";

        }

        catch( Exception e )
        {
            LogService.logIt( e, "getAdditionalTextScoreContentPacked() " + te.toString() );

            throw e;
        }
    }
    
    
    
}
