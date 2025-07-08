/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.amazoncloud.AmazonRekognitionUtils;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.essay.AiEssayScoringUtils;
import com.tm2score.file.ConversionStatusType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.service.LogService;
import com.tm2score.voicevibes.VoiceVibesStatusType;
import com.tm2score.voicevibes.VoiceVibesUtils;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class AvScoringUtils {
    
    AvEventFacade avEventFacade;
    FileUploadFacade fileUploadFacade;
    AmazonRekognitionUtils arUtils;
            

    
    public void resetSpeechTextForTestEvent( long testEventId ) throws Exception
    {
        try
        {
            if( avEventFacade==null )
                avEventFacade=AvEventFacade.getInstance();
            
            List<AvItemResponse> airl = avEventFacade.getAvItemResponsesForTestEventId(testEventId);
            
            if( airl==null || airl.isEmpty() )
                return;
            
            for( AvItemResponse irr : airl )
            {
                if( !irr.requiresSpeechToText() )
                {
                    LogService.logIt( "AvScoringUtils.resetSpeechTextForTestEvent() Skipping AvItemResponseId=" + irr.getAvItemResponseId() + " Speech Text not required., testEventId=" + testEventId );
                    continue;
                }
                                
                LogService.logIt( "AvScoringUtils.resetSpeechTextForTestEvent() Resetting AvItemResponseId=" + irr.getAvItemResponseId() + ", Old scoringStatusTypeId=" + irr.getScoringStatusTypeId() + ", speechTextStatusTypeId=" + irr.getSpeechTextStatusTypeId() + ", speechTextErrorCount=" + irr.getSpeechTextErrorCount() + ", speechText=" + irr.getSpeechText() + ", testEventId=" + testEventId );
                irr.setSpeechText(null);
                irr.setSpeechTextErrorCount(0);
                irr.setSpeechTextStatusTypeId(AvItemSpeechTextStatusType.NOT_STARTED.getSpeechTextStatusTypeId() );
                irr.setScoringStatusTypeId( AvItemScoringStatusType.NOT_READY_FOR_SCORING.getScoringStatusTypeId() );
                avEventFacade.saveAvItemResponse(irr);
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AvScoringUtils.resetSpeechTextForTestEvent() testEventId=" + testEventId );
            throw e;
        }
    }
    
    
    public int getAvItemResponsesNotReadyForScoring( List<UploadedUserFile> uploadedUserFileList, List<AvItemResponse> avItemResponseList, Locale testAdminLocale, Locale mediaLocale, boolean forceVibesOff, SimJ simJ ) throws Exception
    {
        if( uploadedUserFileList==null )
            throw new Exception( "uploadedUserFileList is null!" );
        
        if( avItemResponseList==null )
            throw new Exception( "avItemResponseList is null!" );
        
        AvItemResponse iir;
        
        int notReadyCount=0;
        
        for( UploadedUserFile uuf : uploadedUserFileList )
        {
            if( !uuf.getFileProcessingType().requiresAvItemResponse() )
                continue;
            
            iir = getAvItemResponseForItem(  uuf.getUploadedUserFileId(), avItemResponseList );

            if( iir==null && uuf.getConversionStatusType().getIsError() )
            {
                LogService.logIt( "AvScoringUtils.getAvItemResponsesNotReadyForScoring() AvItemResponse is not present for UploadedUserFileId=" + uuf.getUploadedUserFileId() + " most likely because UUF has a conversion error. Skipping. Conversion Error: " + uuf.getNote()  );
                continue;
            }
            
            // Should not happen. An exception should already have been thrown.
            if( iir ==null )
                throw new Exception( "AvItemResponse is not present for UploadedUserFileId=" + uuf.getUploadedUserFileId() + " should have been created by the convert program." );
            
            
            if( iir.getVoiceVibesStatusTypeId()==VoiceVibesStatusType.NOT_SET.getVoiceVibesStatusTypeId()  )
                setVoiceVibesStatusType(iir, testAdminLocale, mediaLocale, forceVibesOff, simJ );

            if( iir.getAvItemEssayStatusType().isUnset() )
                setAvItemEssayStatusType(iir, testAdminLocale, mediaLocale, simJ );
            
            // Not ready. 
            if( !iir.isReadyForScoring() )
                notReadyCount++;
        }
        
        return notReadyCount;
        
    }
    
    public int getAvItemResponsesPendingScoring(List<AvItemResponse> avItemResponseList) throws Exception
    {
        if( avItemResponseList==null )
            throw new Exception( "avItemResponseList is null!" );
        
        int count=0;        
        for( AvItemResponse iir : avItemResponseList )
        {            
            if( iir.isPendingScoring() )
                count++;
        }        
        return count;        
    }
    
    
    public void scoreItems(TestEvent te, List<AvItemResponse> avItemResponseList, Locale testAdminLocale, boolean englishTranslationsOff ) throws Exception 
    {
        if( avItemResponseList==null )
            throw new Exception( "AvScoringUtils.scoreItems() avItemResponseList is null. ");

        AvItemType ivrItemType;
        
        AvItemScorer ivrItemScorer;
        
        SimJ.Intn intn = null;
        
        // Locale testAdminLocale = getLocale(); //  te.getLocaleStr()!=null && !te.getLocaleStr().isEmpty() ? I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() ) : Locale.US;
        
        Locale testContentLocale = te.getProduct() != null ? te.getProduct().getLocaleFmLangStr() : testAdminLocale;
        
        // IVR and VOT use Product.strParam6 to designate the locale the content - the language the person should be speaking in. 
        //if( te.getProduct()!=null && ( te.getProduct().getProductType().getIsIvr() || te.getProduct().getProductType().getIsVot() ) && te.getProduct().getStrParam6()!=null && !te.getProduct().getStrParam6().isEmpty() )
        //        testContentLocale = I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam6() );
        
        for( AvItemResponse iir : avItemResponseList )
        {
            try
            {
                // Always rescore the item so that changes can be incorporated into score.
                
                // Skip errored or skipped
                if( iir.getAvItemScoringStatusType().isScoreError() || iir.getAvItemScoringStatusType().isScoreSkipped())
                {
                    continue;
                }

                // reset completed iirs and rescore them!
                if( iir.getAvItemScoringStatusType().isScoreComplete() )
                {
                    iir.setScoringStatusTypeId(AvItemScoringStatusType.READY_FOR_SCORING.getScoringStatusTypeId() );
                }
                
                // Always rescore the item so that changes can be incorporated into score.
                if( !iir.isReadyForScoring() )
                    continue;
                
                if( iir.containsPendingAudio() )
                {
                    LogService.logIt( "Still has pending audio! locale=" + testAdminLocale.toString()  + ", " + iir.toString() + ". Skipping this item for now." );
                    continue;
                    // throw new Exception( "Still has pending audio!" );
                }
                
                if( iir.requiresSpeechToText() && !iir.isSpeechToTextCompleteOrPermanentError() )
                {
                    LogService.logIt( "Requires SpeechToText but not complete. locale=" + testAdminLocale.toString()  + ", " + iir.toString() + ". Skipping this item for now." );
                    continue;
                    // throw new Exception( "Requires SpeechToText but not complete. " + iir.toString() );
                }
                
                if( !englishTranslationsOff && iir.needsSpeechTranslation(testContentLocale) )
                {
                    LogService.logIt( "Requires SpeechText Translation but not complete. locale=" + testAdminLocale.toString()  + ", " + iir.toString() + ". Skipping this item for now." );
                    continue;
                    // throw new Exception( "Requires SpeechText Translation but not complete. locale=" + testAdminLocale.toString() + ", " + iir.toString() );
                }
                
                ivrItemType = iir.getAvItemType(); 
                
                ivrItemScorer = AvItemScorerFactory.getAvItemScorer(ivrItemType.getAvItemTypeId(), testAdminLocale, te.getIpCountry(), te.getUser(), te );
                
                intn = getSimIntn(te, iir.getItemSeq(), iir.getItemUniqueId() );

                if( intn==null )
                {
                    LogService.logIt( "Cannot find SimJ.intn for itemSeq=" + iir.getItemSeq() + ", itemUniqueId=" + iir.getItemUniqueId() + ", it appears this item was removed without changing Sim Version. Skipping this item." );
                    iir.setScoringStatusTypeId( AvItemScoringStatusType.SCORE_ERROR.getScoringStatusTypeId() );
                    if( avEventFacade==null )
                        avEventFacade = AvEventFacade.getInstance();                
                    avEventFacade.saveAvItemResponse( iir );
                    continue;
                    // throw new Exception( "Cannot find SimJ.intn for itemSeq=" + iir.getItemSeq() + ", itemUniqueId=" + iir.getItemUniqueId() );
                }
                
                ivrItemScorer.scoreAvItem(intn, iir);
                
                if( avEventFacade==null )
                    avEventFacade = AvEventFacade.getInstance();                
                avEventFacade.saveAvItemResponse( iir );
            }

            catch( Exception e )
            {
               LogService.logIt( e, "AvScoringUtils.scoreItems() Error Scoring avItemResponse. testEventId="  + te.getTestEventId() + ", " + iir.toString() );

               throw e;
            }
        }
    }
    
    public static SimJ.Intn getSimIntn( SimJ simJ, AvItemResponse iir ) throws Exception
    {
        return getSimIntn( simJ, iir.getItemSeq(), iir.getItemUniqueId() );
    }
    
    
    public static SimJ.Intn getSimIntn( TestEvent te, int itemSeq, String itemUniqueId) throws Exception
    {
        return getSimIntn( te.getSimXmlObj(), itemSeq, itemUniqueId );
    }
    
    public static SimJ.Intn getSimIntn( SimJ simJ, int itemSeq, String itemUniqueId ) throws Exception
    {
        if( simJ==null )
            throw new Exception( "simJ is null!" );
        
        if( itemUniqueId!=null && !itemUniqueId.trim().isEmpty() )
        {
            int count = 0;
            SimJ.Intn out = null;

            for( SimJ.Intn intn : simJ.getIntn() )
            {
                if( intn.getUniqueid()!=null && intn.getUniqueid().trim().equals( itemUniqueId.trim() ) )
                {
                    out = intn;
                    count++;
                }
            }            

            // only accept match if unique.
            if( out!=null && count==1 )
                return out;
            
            //for( SimJ.Intn intn : simJ.getIntn() )
            //{
            //    if( intn.getUniqueid()!=null && intn.getUniqueid().trim().equals( itemUniqueId.trim() ) )
            //        return intn;
            //}            
        }
        
        for( SimJ.Intn intn : simJ.getIntn() )
        {
            if( intn.getSeq()==itemSeq )
                return intn;
        }
        
        return null;        
    }
    
    public void saveAllAvItemResponses(List<AvItemResponse> avItemResponseList) throws Exception
    {
        if( avItemResponseList==null )
            throw new Exception( "saveAllAvItemResponses() avItemResponseList is null. ");
        
        for( AvItemResponse iir : avItemResponseList )
        {
            try
            {
                if( avEventFacade==null )
                    avEventFacade = AvEventFacade.getInstance();
                
                avEventFacade.saveAvItemResponse( iir );
            }

            catch( Exception e )
            {
               LogService.logIt( e, "AvTestEventScorer.saveAllAvItemResponses() Error Scoring avItemResponse. " + iir.toString() );

               throw e;
            }
        }
    }
    
    
    public AvItemResponse getAvItemResponseForItem( long uploadedUserFileId, List<AvItemResponse> avItemResponseList )
    {
        if( avItemResponseList==null )
            return null;
        
        for( AvItemResponse iir : avItemResponseList )
        {
            if( iir.getUploadedUserFileId()==uploadedUserFileId )
                return iir;
        }
        
        return null;
    }

    public AvItemResponse getAvItemResponseForItemUniqueId( String itemUniqueId, List<AvItemResponse> avItemResponseList )
    {
        if( avItemResponseList==null )
            return null;
        
        for( AvItemResponse iir : avItemResponseList )
        {
            if( iir.getItemUniqueId()!=null && iir.getItemUniqueId().equals(itemUniqueId) )
                return iir;
        }
        
        return null;
    }

    
    
    public UploadedUserFile getUploadedUserFileForItem( String nodeUniqueId, int ndSeq, List<UploadedUserFile> uploadedUserFileList, List<AvItemResponse> avItemResponseList )
    {
        if( uploadedUserFileList==null )
            return null;
        
        // First try to match on uniqueId
        if( nodeUniqueId!=null && !nodeUniqueId.isEmpty()  )
        {
            int ct=0;
            UploadedUserFile uu=null;
            for( UploadedUserFile uuf : uploadedUserFileList )
            {
                if( uuf.getNodeUniqueId()!=null && uuf.getNodeUniqueId().equals( nodeUniqueId ) )
                {
                    ct++;
                    uu=uuf;
                    // return uuf;
                }
            } 
            
            if( ct==1 && uu!=null )
                return uu;

            // For a time the uniqueid was not stored in UploadedUserFiles but it was stored in AvItemResponses, so use that. 
            if( avItemResponseList!=null )
            {
                for( AvItemResponse iir : avItemResponseList )
                {
                    if( iir.getItemUniqueId()!=null && iir.getItemUniqueId().equals( nodeUniqueId) )
                    {
                        ct=0;
                        uu = null;
                        
                        for( UploadedUserFile uuf : uploadedUserFileList )
                        {
                            if( uuf.getUploadedUserFileId()==iir.getUploadedUserFileId() )
                            {
                                ct++;
                                uu = uuf;
                                // return uuf;
                            }
                        } 
                        
                        if( ct==1 && uu!=null )
                            return uu;
                    }
                }
            }
        }
        
        // No luck, match on seq. This can change with builds so is not reliable.
        for( UploadedUserFile uuf : uploadedUserFileList )
        {
            if( uuf.getNodeSeq()==ndSeq )
                return uuf;
        }
        
        return null;
    }
    
    
    
    public void checkVideoOrientations( List<UploadedUserFile> uploadedUserFileList ) throws Exception
    {
        if( uploadedUserFileList==null )
            return;
        
        for( UploadedUserFile uuf : uploadedUserFileList )
        {
            checkVideoOrientation( uuf );
        }
    }
    
    public void checkVideoOrientation( UploadedUserFile uuf ) throws Exception
    {
        // LogService.logIt( "AvScoringUtils.checkVideoOrientation() AAA" );
        if( uuf==null || 
            !uuf.getUploadedUserFileStatusType().getAvailable() ||
            uuf.getOrientation()!=0 ||
            !uuf.getConversionStatusType().equals( ConversionStatusType.COMPLETE ) ||  
            uuf.getThumbFilename()==null || 
            uuf.getThumbFilename().isBlank() || 
            !uuf.getFileContentType().getIsVideo()   )
            return;
        
        if( arUtils==null )
            arUtils = new AmazonRekognitionUtils();
            
        // LogService.logIt( "AvScoringUtils.checkVideoOrientation() BBB " );
        // Data[0] == SUCCESS or ERROR
        // data[1] = null or FaceDetail for success, message for Error
        // data[2] = null, 0, or orientation. Orientation=0 good, XX=XX degrees counterclockwise (must rotate XX clockwise)
        Object[] cout = arUtils.getSingleFaceDetails(uuf, true, true );

        if( cout==null || cout[0]==null || cout.length<3 || !((String)cout[0]).equals("SUCCESS") )
        {
            LogService.logIt( "AvScoringUtils.checkVideoOrientation() unable to get single face details." );
            return;
        }
        
        Integer o = (Integer) cout[2];
        
        if( o!=null && o!=0 )
            uuf.setOrientation(o);

        // LogService.logIt( "AvScoringUtils.checkVideoOrientation() CCC Orietnation=" + (o!=null ? o.toString() : "null") );
        
        uuf.setConversionStatusTypeId( ConversionStatusType.COMPLETE_ORIENTATIONSET.getConversionStatusTypeId() );
        
        if( fileUploadFacade==null )
            fileUploadFacade = FileUploadFacade.getInstance();
        
        fileUploadFacade.saveUploadedUserFile(uuf);
    }
    
    
    
    public int countUploadedUserFilesNotReadyForScoring( List<UploadedUserFile> uploadedUserFileList, List<AvItemResponse> avItemResponseList ) throws Exception
    {
        if( uploadedUserFileList==null )
            throw new Exception( "UploadedUserFileList is null!" );
        
        int count=0;
        
        AvItemResponse iir;
        
        for( UploadedUserFile uuf : uploadedUserFileList )
        {
            if( !uuf.getConversionStatusType().getReadyForScoring() )
            {
                count++;
                continue;
            }
            
            if( !uuf.getFileProcessingType().requiresAvItemResponse() )
                continue;
            
            
            iir = this.getAvItemResponseForItem( uuf.getUploadedUserFileId(), avItemResponseList );
            
            if( iir==null && uuf.getConversionStatusType().getIsError() )
            {
                LogService.logIt( "UploadedUserFile " + uuf.getUploadedUserFileId() + " has a conversion error and AvItemResponse is not present. Will skip during scoring." );
                continue;
            }
            
            if( iir==null )
                throw new Exception( "UploadedUserFile " + uuf.getUploadedUserFileId() + " requires an AvItemResponse but none is present. " );
        }
        
        return count;
    }
    
    
    public static void setVoiceVibesStatusType( AvItemResponse iir, Locale testAdminLocale, Locale testContentLocale, boolean forceVibesOff, SimJ simJ ) throws Exception
    {
        if( iir==null )
            return;
        
        // already set
        if( iir.getVoiceVibesStatusTypeId()!=VoiceVibesStatusType.NOT_SET.getVoiceVibesStatusTypeId() )
            return;
        
        if( testContentLocale==null )
            testContentLocale = testAdminLocale;
        
        AvEventFacade avEventFacade = AvEventFacade.getInstance();
        
        if( forceVibesOff || !iir.getAvItemType().supportsVoiceVibesAnalysis() )
        {
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
            avEventFacade.saveAvItemResponse(iir);
            return;                        
        }
        
        if( testContentLocale!=null && !testContentLocale.getLanguage().equalsIgnoreCase("en") )
        {
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
            avEventFacade.saveAvItemResponse(iir);
            return;            
        }
        
        VoiceVibesUtils voiceVibesUtils = new VoiceVibesUtils();
        
        if( !voiceVibesUtils.isVoiceVibesOn() || 
            //!includeVoiceVibesInTestScores() ||
            !iir.getAvItemType().supportsVoiceVibesAnalysis() || 
            !iir.getAvItemType().requiresRecordVoice() || 
            iir.getAvItemScoringStatusType().isInvalid() || 
            iir.getAvItemScoringStatusType().isSkipped() )
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );

        // Need to check
        else
        {
            SimJ.Intn theIntn = getSimIntn( simJ, iir );
            
            if( theIntn != null && theIntn.getTextscoreparam1()!=null &&  IvrStringUtils.containsKey("[NOVIBES]", theIntn.getTextscoreparam1(), true )  )
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
            else
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_POSTED.getVoiceVibesStatusTypeId() );              
        }

        avEventFacade.saveAvItemResponse(iir);
        
    }
    
    
    public static void setAvItemEssayStatusType( AvItemResponse iir, Locale testAdminLocale, Locale testContentLocale, SimJ simJ ) throws Exception
    {
        if( iir==null )
            return;
        
        // already set
        if( !iir.getAvItemEssayStatusType().isUnset() )
            return;
        
        if( testContentLocale==null )
            testContentLocale = testAdminLocale;
        
        AvEventFacade avEventFacade = AvEventFacade.getInstance();
        
        if( !iir.getAvItemType().supportsEssayScoring() )
        {
            iir.setAvItemEssayStatusTypeId(AvItemEssayStatusType.NOT_REQUIRED.getEssayStatusTypeId() );
            avEventFacade.saveAvItemResponse(iir);
            return;                        
        }
        
        //if( testContentLocale!=null && !testContentLocale.getLanguage().equalsIgnoreCase("en") )
        //{
        //    iir.setAvItemEssayStatusTypeId(AvItemEssayStatusType.NOT_REQUIRED.getEssayStatusTypeId() );
        //    avEventFacade.saveAvItemResponse(iir);
        //    return;            
        //}
                

        SimJ.Intn theIntn = getSimIntn( simJ, iir );

        if( theIntn != null && theIntn.getTextscoreparam1()!=null &&  IvrStringUtils.containsKey("[ESSAYPROMPT]", theIntn.getTextscoreparam1(), true )  )
        {
            if( !RuntimeConstants.getBooleanValue( "discernOn") )
            {
                iir.setAvItemEssayStatusTypeId(AvItemEssayStatusType.ERROR.getEssayStatusTypeId() );
                iir.appendNotes( "AvScoringUtils.setAvItemEssayStatusType() set EssayStatusType to errored because Discern is not on.");
            }
            else
                iir.setAvItemEssayStatusTypeId(AvItemEssayStatusType.NOT_REQUESTED.getEssayStatusTypeId() );
        }
        
        else if( theIntn!=null && theIntn.getCt5Int25()==1 )
        {
            if( !AiEssayScoringUtils.getAiEssayScoringOn() )
            {
                iir.setAvItemEssayStatusTypeId(AvItemEssayStatusType.ERROR.getEssayStatusTypeId() );
                iir.appendNotes( "AvScoringUtils.setAvItemEssayStatusType() set EssayStatusType to errored because AI Scoring is not on.");
            }
            else
                iir.setAvItemEssayStatusTypeId(AvItemEssayStatusType.NOT_REQUESTED.getEssayStatusTypeId() );
        }
            
        else
            iir.setAvItemEssayStatusTypeId(AvItemEssayStatusType.NOT_REQUIRED.getEssayStatusTypeId() );
        
        avEventFacade.saveAvItemResponse(iir);
    }
    
    
    
    
    
        
}
