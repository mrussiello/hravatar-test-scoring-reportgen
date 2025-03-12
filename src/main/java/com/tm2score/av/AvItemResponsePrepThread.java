/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.amazoncloud.AmazonTranscribeUtils;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.file.BucketType;
import com.tm2score.file.FileXferUtils;
import com.tm2score.file.HttpFileUtils;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.googlecloud.GoogleApiException;
import com.tm2score.googlecloud.GoogleSpeechUtils;
import com.tm2score.googlecloud.GoogleStorageUtils;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.ivr.IvrScoreException;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.ScoreUtils;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.twilio.TwilioIvrUtils;
import com.tm2score.twilio.TwilioRestUtils;
import com.tm2score.user.UserFacade;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.voicevibes.VoiceVibesStatusType;
import com.tm2score.voicevibes.VoiceVibesUtils;
import com.tm2score.xml.JaxbUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author miker_000
 */
public class AvItemResponsePrepThread implements Runnable {

    private static Map<Long,Date> ID_LAST_PROCESS_DATE_MAP = null;
    private static Date LAST_MAP_CLEAN_DATE = null;
    private static final int MIN_MNUTES_BETWEEN_AV_PROCS = 5;
    
    public boolean AMAZON_TRANSCRIBE_OK = true; // RuntimeConstants.getBooleanValue("UseAmazonTranscribe");  // TRUE!!!!!
    public static int MAX_SPEECH_TEXT_ERRORS = 3;
    public static int MAX_VOICE_VIBES_POST_ERRORS = 3;
    
   // FIX THIS
    private static float MAX_BASE64_DURATION = 59;
    private static int SPEECH2TEXT_ASYNCH_WAIT_SECS = 10;

    private static boolean deleteSourceAudios = true;
    
    EventFacade eventFacade;
    AvEventFacade avEventFacade;
    UserFacade userFacade;
    
    long testEventId;
    boolean stopDeleteSourceAudios;
    int initialDelaySecs = 0;
    // TestEvent testEvent=null;
    boolean retrySpeechToExtIfError = true;
    boolean retryVoiceVibesPostIfError = false;
    
    FileXferUtils fileXferUtils;
    
    LanguageUtils languageUtils = null;
    
    AvItemResponse iirForVibes = null;
    
    boolean forceVibesOff;
    
    boolean forceNonEssentialTranscriptsOff;
    
    TestEvent te;
    
    
    
    
    public AvItemResponsePrepThread( boolean stopDeleteSrc, boolean forceVibesOff, boolean forceNonEssentialTranscriptsOff)
    {        
        init();
        
        stopDeleteSourceAudios = stopDeleteSrc;
        this.forceVibesOff=forceVibesOff;
        this.forceNonEssentialTranscriptsOff=forceNonEssentialTranscriptsOff;

        if( stopDeleteSourceAudios )
            deleteSourceAudios = false;
        
    }
    
    public AvItemResponsePrepThread( TestEvent te, boolean stopDeleteSrc, boolean forceVibesOff, boolean forceNonEssentialTranscriptsOff)
    {        
        init();
        
        this.te=te;
        this.testEventId=te.getTestEventId();
        stopDeleteSourceAudios = stopDeleteSrc;
        this.forceVibesOff=forceVibesOff;
        this.forceNonEssentialTranscriptsOff=forceNonEssentialTranscriptsOff;
        
        if( stopDeleteSourceAudios )
            deleteSourceAudios = false;
    }
    
    
    private synchronized static void init()
    {
        if( ID_LAST_PROCESS_DATE_MAP==null )
        {
            ID_LAST_PROCESS_DATE_MAP = new ConcurrentHashMap<>();
            LAST_MAP_CLEAN_DATE = new Date();
        }
        
        cleanLastProcessDateMap();
    }
    
    private static synchronized void cleanLastProcessDateMap()
    {
        if( ID_LAST_PROCESS_DATE_MAP==null )
            return;

        GregorianCalendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -30 );
        Date cutDate = cal.getTime();
        
        if( LAST_MAP_CLEAN_DATE!=null && cutDate.before(LAST_MAP_CLEAN_DATE) )
            return;

        LAST_MAP_CLEAN_DATE = new Date();
        
        Date d;
        for( Long aid : ID_LAST_PROCESS_DATE_MAP.keySet() )
        {
            d = ID_LAST_PROCESS_DATE_MAP.get(aid);
            if( d==null || d.before( cutDate ) )
                ID_LAST_PROCESS_DATE_MAP.remove(aid);
        }
    }

    private void addAvItemResponseProc( long avItemResponseId )
    {
        if( ID_LAST_PROCESS_DATE_MAP==null )
            init();
        
        ID_LAST_PROCESS_DATE_MAP.put( avItemResponseId, new Date() );
    }

    private boolean okToProcessAvItemResponse( long avItemResponseId )
    {
        if( ID_LAST_PROCESS_DATE_MAP==null )
        {
            init();
            return true;
        }
        
        Date d = ID_LAST_PROCESS_DATE_MAP.get( avItemResponseId );
        if( d==null )
            return true;
        GregorianCalendar cal = new GregorianCalendar();
        cal.add( Calendar.MINUTE, -1*MIN_MNUTES_BETWEEN_AV_PROCS );
        if( d.before( cal.getTime() ) )
            return true;
        return false;
    }

    
    @Override
    public void run() {
        try
        {
            if( this.initialDelaySecs>0 )
                Thread.sleep(initialDelaySecs*1000 );
            
            // LogService.logIt( "AvItemResponsePrepThread.run() STARTING Cleaning Operation. testEventId=" + testEventId );
            
            
            if( avEventFacade==null )
                avEventFacade = AvEventFacade.getInstance();
            
            cleanUnscoredAvItemResponseRecords();            
                        
            // LogService.logIt( "AvItemResponsePrepThread.run() Cleaning Operation COMPLETE. testEventId=" + testEventId ); 
        }
        catch( IOException e )
        {
            LogService.logIt( "AvItemResponsePrepThread.run() IO Exception. Processing stopped for testEvent. Will be able to try again. ERROR " + e.toString() + ", testEventId=" + testEventId );
            // EmailUtils.getInstance().sendEmailToAdmin( "AvItemResponsePrepThread Error testEventId=" + testEventId, "AvItemResponsePrepThread Error stops all processing for this testevent. TestEventId=" + testEventId + "\n\n" +  e.toString() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AvItemResponsePrepThread.run() testEventId=" + testEventId );
            EmailUtils.getInstance().sendEmailToAdmin( "AvItemResponsePrepThread Error testEventId=" + testEventId, "AvItemResponsePrepThread Error stops all processing for this testevent. TestEventId=" + testEventId + "\n\n" +  e.toString() );
        }
    }
    
    
    /**
     * The steps are:
     * 
     * Copy from Twilio (to HR Avatar)
     * Copy from Twilio to Google Cloud
     * Post to Voice Vibes if needed
     * Speech to Text (Google)
     * Delete from Google Cloud
     * Wait (120 sec)
     * Check Voice Vibes Status
     * 
     * 
     * if Ready, 
     *      Delete from Voice Vibes
     * 
     * if ready and allowed,
     *      Delete from Twilio
     * 
     * @param testEventId
     * @throws Exception 
     */
    private void cleanUnscoredAvItemResponseRecords() throws Exception
    {
        if( eventFacade==null )
            eventFacade = EventFacade.getInstance();

        if( te==null )
            te = eventFacade.getTestEvent(testEventId, true );

        if( te==null )
            throw new Exception( "TestEvent is null for testEventId=" + testEventId );
        
        if( te.getTestKey()==null )
            te.setTestKey( eventFacade.getTestKey(te.getTestKeyId(), false ) );
        
        if( te.getProduct()==null )
            te.setProduct(eventFacade.getProduct( te.getProductId() ));

        Product product = te.getProduct();
        
        if( te.getOrg()==null )
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            te.setOrg( userFacade.getOrg( te.getOrgId() ));
        }

        if( te.getSuborg()==null && te.getSuborgId()>0 )
        {
            if( userFacade==null )
                userFacade=UserFacade.getInstance();
            te.setSuborg( userFacade.getSuborg( te.getSuborgId() ));
        }
        
        if( te.getReport()==null && te.getReportId()>0 )
            te.setReport( eventFacade.getReport( te.getReportId() ) );
        
        boolean englishTranslationsOff = ReportUtils.getReportFlagBooleanValue( "englishtransoff", te.getTestKey(), te.getProduct(), te.getSuborg(), te.getOrg(), te.getReport() );
        
        if( avEventFacade==null )
            avEventFacade = AvEventFacade.getInstance();

        List<AvItemResponse> irl = avEventFacade.getAvItemResponsesForTestEventId( testEventId );

        if( irl==null || irl.isEmpty() )
            return;

        // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() teid=" + testEventId + " found " + irl.size() + " AvItemResponses. forceVibesOff=" + forceVibesOff );

        TwilioIvrUtils twilioIvrUtils = null;

        TwilioRestUtils twilioRestUtils = null;
        
        Date now = new Date();

        boolean isOneHourOld = now.getTime() - te.getLastAccessDate().getTime() > 60*60*1000;
                
        
        Calendar nowMinus30 = new GregorianCalendar();
        nowMinus30.add( Calendar.MINUTE, -30 );
        boolean ignorePendingAudios = te.getLastAccessDate().before(nowMinus30.getTime() );
        
        VoiceVibesUtils voiceVibesUtils = new VoiceVibesUtils();

        Speech2TextResult s2tResult;            

        boolean chg;
        
        String googleUri;
        String base64Str;
        Object[] longS2TResponse;
        String googleOperationName;
        boolean useAsynchSpeech2Text;
        boolean needsSpeechToText;
        // boolean needsSpeechTextTranslation;
        
        Locale testAdminLocale = null;        
        Locale testContentLocale = null;
        
        testAdminLocale = te.getLocaleStr()!=null && !te.getLocaleStr().isEmpty() ? I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() ) : Locale.US;        
        
        if( product != null  && !product.getLangStr().isEmpty() )
            testContentLocale = I18nUtils.getLocaleFromCompositeStr( product.getLangStr() );
        else
            testContentLocale = testAdminLocale;
        
        // IVR and VOT use Product.strParam6 to designate the locale the content - the language the person should be speaking in.
        // However, the country that the person is speaking in may also be needed. 
        //if( product!=null && ( product.getProductType().getIsIvr() || product.getProductType().getIsVot() ) && product.getStrParam6()!=null && !product.getStrParam6().isEmpty() )
        //    testContentLocale = I18nUtils.getLocaleFromCompositeStr( product.getStrParam6() );

            // Sims with recorded media language is in p.strParam7
        if( product!=null && (product.getProductType().getIsSimOrCt5Direct()) && product.getStrParam7()!=null && !product.getStrParam7().isBlank() )
            testContentLocale = I18nUtils.getLocaleFromCompositeStr( product.getStrParam7() );
        
        
        // Locale mediaLocale;
        
        //boolean needsVoiceVibesDelay = false;
        //boolean needsPass2 = false;

        // This is used to limit Vibes calls to one per test event. 
        iirForVibes = null;
                
        // PASS 1 - Determine which IIR to perform Vibes on. 
        // Rule is to perform on the longest duration audio of all audios for which vibes was collected.
        for( AvItemResponse iir : irl )
        {
            try
            {
                if( iir.getMediaLocale()==null )
                    iir.setMediaLocale( iir.getLangCode()==null || iir.getLangCode().isBlank() ? testContentLocale : I18nUtils.getLocaleFromCompositeStr( iir.getLangCode() ) );
                
                chg=false;

                if( iir.getVoiceVibesStatusType().isPostError() && iir.getVoiceVibesPostErrorCount()>=AvItemResponsePrepThread.MAX_VOICE_VIBES_POST_ERRORS)
                {
                    iir.setVoiceVibesStatusTypeId(VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                    chg = true;
                }
                
                if( iir.getVoiceVibesStatusType().isPostError() )
                {
                    if( retryVoiceVibesPostIfError )
                        chg = resetAvItemResponseForVoiceVibesPostError( iir );
                    else
                    {
                        iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                        chg = true;                    
                    }
                }
                
                if( iir.getVoiceVibesStatusType().isUnset() && forceVibesOff )
                {
                    // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Setting VoiceVibesStatusType to unset" );
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                    chg = true;                                    
                }                    
                               
                else if( iir.getVoiceVibesStatusType().isUnset() && 
                        ( ( iir.getMediaLocale()!=null && !iir.getMediaLocale().getLanguage().equalsIgnoreCase( "en" )) ||
                          iir.getAvItemScoringStatusType().isAnySkipped() || 
                          iir.getAvItemScoringStatusType().isAnyInvalid() || 
                         !iir.getAvItemType().requiresRecordVoice() || 
                         !iir.getAvItemType().supportsVoiceVibesAnalysis() ))
                {
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                    chg = true;                                    
                }

                // Designate as the one if already designated before.
                if( iirForVibes==null && !iir.getVoiceVibesStatusType().isUnset() && !iir.getVoiceVibesStatusType().isNotRequired() )
                {
                    if( iir.getVoiceVibesStatusType().isAnalysisCompleteOrDeletedOrDeleteError() )
                        iirForVibes = iir;
                    // needs to be posted
                    else if( iir.getVoiceVibesStatusType().needsPost())
                        iirForVibes = iir;
                    // posted
                    else if( iir.getVoiceVibesStatusType().isPending())
                        iirForVibes = iir;
                    // Non-permanent error
                    else if( iir.getVoiceVibesStatusType().isAnyError() && !iir.getVoiceVibesStatusType().isAnyPermanentError())
                        iirForVibes = iir;
                    // if permanent error, don't use it ever again. Ignore it. 
                    //else if( retryVoiceVibesPostIfError && iir.getVoiceVibesStatusType().isPermanentPostError() )
                    //    iirForVibes = iir;
                }

                if( forceNonEssentialTranscriptsOff && iir.getSpeechTextStatusType().isNotStarted() && !iir.getAvItemType().getRequiresTranscription() )
                {
                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.NOT_REQUIRED.getSpeechTextStatusTypeId() );
                    chg = true;                    
                }
                
                if( chg )
                {
                    saveAvItemResponse( iir );
                    chg=false;
                }
            }
            catch( Exception e )
            {
                LogService.logIt( e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Pass 1 avItemResponseId=" + iir.getAvItemResponseId() +", teid=" + iir.getTestEventId() );
                throw e;
            }
        }
        
        // This means we haven't found an iir that has been designated already for vibes. So look for one. 
        if( !forceVibesOff && iirForVibes==null )
        {
            List<AvItemResponse> vibesRequestedList = new ArrayList<>();
            for( AvItemResponse iir : irl )
            {
                if( iir.getMediaLocale()==null )
                    iir.setMediaLocale( iir.getLangCode()==null || iir.getLangCode().isBlank() ? testContentLocale : I18nUtils.getLocaleFromCompositeStr( iir.getLangCode() ) );
                
                if( iir.getVoiceVibesStatusType().isUnset() && isVibesRequested(iir, te, testAdminLocale, iir.getMediaLocale() ) )
                    vibesRequestedList.add( iir );
            }

            // request for one
            if( vibesRequestedList.size()==1 )
                iirForVibes = vibesRequestedList.get(0);
            
            // requested for multiple
            else if( vibesRequestedList.size()>1 )
            {
                Collections.sort( vibesRequestedList, new AvItemScoreDurationComparator() );
                Collections.reverse( vibesRequestedList );
                iirForVibes = vibesRequestedList.get(0);
            }
            
            //if( iirForVibes!=null && iirForVibes.getVoiceVibesStatusType().isUnset() )
            //{
            //    iirForVibes.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_POSTED.getVoiceVibesStatusTypeId() );
            //    saveAvItemResponse( iirForVibes );
            //}
        }
        
        // At this point  we should have designated an iirForVibes. It could still be in unset status.
        
        List<AvItemResponse> processedIrl = new ArrayList<>();
                       
        AvItemResponse iir = null;
        
        boolean hasLiveVoiceVibesPost = false;
        
        // PASS 2 - perform the work.
        for( AvItemResponse ir : irl )
        {            
            try
            {
                // Always reload a fresh iir because of time delays, it could be elsewhere in the process.
                iir = avEventFacade.getAvItemResponse(ir.getAvItemResponseId(), true );

                // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() BBZ AvItemResponseId=" + iir.getAvItemResponseId() );
                
                iir.setMediaLocale( ir.getMediaLocale() );
                
                if( iir.getMediaLocale()==null )
                    iir.setMediaLocale( iir.getLangCode()==null || iir.getLangCode().isBlank() ? testContentLocale : I18nUtils.getLocaleFromCompositeStr( iir.getLangCode() ) );
                
                processedIrl.add( iir );

                // Skip item responses that are ready for scoring. 
                // Skip item responses that are ready for scoring. 
                if( iir.getAvItemScoringStatusType().isReadyForScoring()  )
                    continue;

                // check for parallel process too close in time to this one.
                if( !okToProcessAvItemResponse( iir.getAvItemResponseId() ) )
                    continue;

                addAvItemResponseProc( iir.getAvItemResponseId() );
                
                chg=false;

                // Skip item responses that are ready for scoring. 
                if( iir.getAvItemScoringStatusType().isScoringCompleteOrError() )
                {
                    if( iir.getSpeechTextStatusType().isTempError() && iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS)
                    {
                        iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                        chg=true;
                    }

                    //if( iir.getVoiceVibesStatusType().isPostError() && iir.getVoiceVibesPostErrorCount()>=AvItemResponsePrepThread.MAX_VOICE_VIBES_POST_ERRORS)
                    //{
                    //    iir.setVoiceVibesStatusTypeId(VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                    //    chg=true;
                    //}

                    if( chg )
                    {
                        saveAvItemResponse( iir );
                        chg=false;
                    }

                    continue;
                }

                // invalid items need to have any audio removed.  Invalid happens when 
                //           there is no recUrl from Twilio, or 
                //           recorded audio is too small or too long
                //           item timeout
                //           item submitted with no dtmf entry.

                // if invalid - delete any audio and set status to invalid_read_for_scoring.

                // skipped items need to have audio removed.  Skipped happens when a person requests too many repeats of the question.
                // if skipped, delete any audio and set status to skipped_ready_for_scoring.

                //try
                //{
                chg=false;

                
                // This is usually true so that Service Not Available errors can be overcome by waiting.
                if( retrySpeechToExtIfError && iir.getSpeechTextStatusType().isTempError() ) // ==IvrItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() )
                {
                    chg = resetAvItemResponseForSpeechToTextError( iir );                    
                }

                // Otherwise, make all temp errors permanent. 
                else if( iir.getSpeechTextStatusType().isTempError() ) // && iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS ) // ==IvrItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() )
                {
                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                    iir.setScoringStatusTypeId( AvItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );
                    chg = true;
                }


                //if( retryVoiceVibesPostIfError && iir.getVoiceVibesStatusType().isPostError() )
                //{
                //    chg = resetAvItemResponseForVoiceVibesPostError( iir );
                //}

                //else if( iir.getVoiceVibesStatusType().isPostError() )
                //{
                //    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                //    chg = true;
                //}


                if( chg )
                {
                    saveAvItemResponse( iir );
                    chg=false;
                }


                if( iir.getAvItemScoringStatusType().isReadyForScoring() || iir.getAvItemScoringStatusType().isScoringCompleteOrError() )
                    continue;

                // this means nothing to do. 
                //if( iir.isReadyForScoring() ) // && !iir.needsSpeechTranslation( testContentLocale ) )
                //    continue;

                ignorePendingAudios = iir.getCreateDate().before(nowMinus30.getTime() );

                Tracker.addAvItemResponseStart();

                if( iir.getVoiceVibesStatusType().isUnset() || iir.getEssayStatusType().isUnset() )
                {
                    setVibesAndEssayStatusTypes( iir, te, testAdminLocale, iir.getMediaLocale() );
                    //this.setVoiceVibesStatusType(iir, te, testAdminLocale, testContentLocale);
                    chg=true;
                }

                // No voice needed. Nothing to do. 
                if( !iir.getAvItemType().requiresRecordVoice() )
                {
                    if( iir.getAudioStatusTypeId()!=AvItemAudioStatusType.NOT_PRESENT.getAudioStatusTypeId() )
                    {
                        iir.setAudioStatusTypeId( AvItemAudioStatusType.NOT_PRESENT.getAudioStatusTypeId() );
                        chg=true;
                    }

                    if( iir.getSpeechTextStatusTypeId()!=AvItemSpeechTextStatusType.NOT_REQUIRED.getSpeechTextStatusTypeId() )
                    {
                        iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.NOT_REQUIRED.getSpeechTextStatusTypeId() );
                        chg=true;
                    }

                    if( iir.getScoringStatusTypeId()==AvItemScoringStatusType.INVALID.getScoringStatusTypeId() )
                    {
                        iir.setScoringStatusTypeId( AvItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );
                        chg=true;
                        Tracker.addAvItemResponseCompletion();
                    }

                    if( iir.getScoringStatusTypeId()==AvItemScoringStatusType.SKIPPED.getScoringStatusTypeId() )
                    {
                        iir.setScoringStatusTypeId( AvItemScoringStatusType.SKIPPED_READY_FOR_SCORING.getScoringStatusTypeId() );
                        chg=true;
                        Tracker.addAvItemResponseCompletion();
                    }

                    if( iir.getScoringStatusTypeId()==AvItemScoringStatusType.NOT_READY_FOR_SCORING.getScoringStatusTypeId() )
                    {
                        iir.setScoringStatusTypeId( AvItemScoringStatusType.READY_FOR_SCORING.getScoringStatusTypeId() );
                        chg=true;
                        Tracker.addAvItemResponseCompletion();
                    }


                    if( chg )
                    {
                        if( avEventFacade==null )
                            avEventFacade = AvEventFacade.getInstance();

                        avEventFacade.saveAvItemResponse(iir);                    
                    }

                    // No need to continue. All the rest if for recorded audio.
                    continue;
                }

                boolean sourceAudioMissing = false;
                // At this point, required voice.
                // IvrItemType ivrItemType = iir.getIvrItemType();

                if( iir.getAvItemAudioStatusType().isPendingFromSource() && ignorePendingAudios )
                {
                    //LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() AAAX " );

                    if( avEventFacade==null )
                        avEventFacade = AvEventFacade.getInstance();

                    if( iir.getAudioUri()!=null && !iir.getAudioUri().isEmpty() )
                    {
                        iir.setAudioStatusTypeId( AvItemAudioStatusType.READY_REMOTELY.getAudioStatusTypeId() );
                        //LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Marked iir as READY because wait time is over. " + iir.toString() + ", testEventId=" + iir.getTestEventId() );                    
                    }

                    // No Audio URI! Big problem.
                    else
                    {
                        iir.setAudioStatusTypeId( AvItemAudioStatusType.NOT_PRESENT.getAudioStatusTypeId() );
                        iir.setScoringStatusTypeId( AvItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );
                        //LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Marked iir as Invalid because no longer pending and no audioUri present. " + iir.toString() + ", testEventId=" + iir.getTestEventId() );                    
                    }

                    avEventFacade.saveAvItemResponse(iir);
                    chg=false;
                    Tracker.addAvItemResponseCompletion();
                }

                // Skip if no voice present.
                if( iir.getAudioStatusTypeId()==AvItemAudioStatusType.NOT_PRESENT.getAudioStatusTypeId() )
                {
                    if( !iir.getAvItemScoringStatusType().isScoringCompleteOrError() )
                        iir.setScoringStatusTypeId( AvItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );

                    if( !iir.getSpeechTextStatusType().isCompleteOrPermanentError() )
                        iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                    if( !iir.getVoiceVibesStatusType().isNotRequiredOrCompleteOrPermanentError() )
                        iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );

                    avEventFacade.saveAvItemResponse(iir);

                    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() SKIPPING processing for AvItemResponse becuase no audio present. " + iir.toString() + ", testEventId=" + iir.getTestEventId() );
                    continue;

                }   

                // Audios not ready on Twilio. So skip it.
                if( iir.getAvItemAudioStatusType().isPendingFromSource() )
                {
                    LogService.logIt("AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() SKIPPING processing for AvItemResponse becuase audio is still pending. " + iir.toString() + ", testEventId=" + iir.getTestEventId() + ", ignorePendingAudios=" + ignorePendingAudios + ", iir.getCreateDate()=" + iir.getCreateDate().toString() + ", nowMinus15=" + nowMinus30.toString() );
                    continue;
                }

                // Determine if we need to do VoiceVibes on this.
                //if( iir.getSpeechTextStatusTypeId()==IvrItemSpeechTextStatusType.NOT_STARTED.getSpeechTextStatusTypeId() && !ivrItemType.requiresRecordVoice() )
                //{
                //    iir.setSpeechTextStatusTypeId( IvrItemSpeechTextStatusType.NOT_REQUIRED.getSpeechTextStatusTypeId() );
                //    chg = true;
                //} 

                // Step 1 - copy to HR Avatar if needed.

                // Need to store audio in dbms.
                if( ( iir.getSaveLocalAudio()==1 || iir.getAvItemType().getStoreRecordedAudio() ) && ( iir.getAudioBytes()==null || iir.getAudioBytes().length==0 )   )
                {
                    if( iir.getAudioStatusTypeId()==AvItemAudioStatusType.READY_REMOTELY.getAudioStatusTypeId() )
                    {

                        if( twilioIvrUtils == null )
                            twilioIvrUtils = new TwilioIvrUtils();

                        twilioIvrUtils.saveAudioFileAsMp3ToDb(iir);
                        // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() saved MP3 to DB for item " + iir.getItemUniqueId() + ", testEventId=" + iir.getTestEventId() );
                        chg=false;  // Twilio saves it.

                        if( iir.getAudioSize()==0 )
                            LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() saved MP3 is zero length! for item " + iir.getItemUniqueId() + ", testEventId=" + iir.getTestEventId() );                        

                        if( iir.getAudioSize()==0 || iir.getAudioBytes()==null || iir.getAudioBytes().length==0 )
                        {
                            // Unable to get audio from Twilio. This may be because it's still in processing at Twilio. So, we need to waid longer. 
                            if( isOneHourOld )
                                sourceAudioMissing = true;

                            else
                            {
                                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() saved MP3 was empty and test is less than an hour old, so waiting.  for item " + iir.getItemUniqueId() + ", testEventId=" + iir.getTestEventId() + ", " + iir.getAudioSize() + " bytes." );
                                if( chg )
                                {
                                    if( avEventFacade==null )
                                            avEventFacade=AvEventFacade.getInstance();

                                    avEventFacade.saveAvItemResponse(iir);  
                                    chg=false;
                                    continue;                                    
                                }
                            }
                        }

                        //needsPass2 = true;
                    }

                    else
                        LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() unable to save MP3 to DB for item " + iir.getItemUniqueId() + ", testEventId=" + iir.getTestEventId() + " because the ivrAudioStatusType is not in Recorded Status" );                        
                }

                googleUri = null;
                base64Str = null;            

                ////////////////////////////////////
                // Handle case of no source audio. 
                ////////////////////////////////////
                if( sourceAudioMissing )
                {
                    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Source Audio File is missing for iir.id=" + iir.getAvItemResponseId() +", itemUniqueId=" + iir.getItemUniqueId() + ", testEventId=" + iir.getTestEventId() + ", disabling the AvItemResponse and skipping all other processing." );
                    iir.setScoringStatusTypeId( AvItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );
                    iir.setAudioStatusTypeId( AvItemAudioStatusType.NOT_STORED_LOCALLY_DELETED_REMOTELY.getAudioStatusTypeId() );
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                    if( avEventFacade==null )
                            avEventFacade=AvEventFacade.getInstance();

                    avEventFacade.saveAvItemResponse(iir);  
                    chg=false;
                    Tracker.addAvItemResponseCompletion();
                    continue;
                }


                needsSpeechToText = iir.requiresSpeechToText() && !iir.isSpeechToTextCompleteOrPermanentError() && (!forceNonEssentialTranscriptsOff || iir.getAvItemType().getRequiresTranscription());

                boolean useAmazonTranscribe = AMAZON_TRANSCRIBE_OK;

                if( useAmazonTranscribe && !AmazonTranscribeUtils.isValidLocale( iir.getMediaLocale() ) )
                    useAmazonTranscribe = false;

                // Turning this off makes most of the transcription done through Amazon
                if( 1==2 && useAmazonTranscribe && iir.getDuration()<=MAX_BASE64_DURATION )
                    useAmazonTranscribe = false;

                // needs speech to text. Must do this before deleting any source audios. 
                if( needsSpeechToText && useAmazonTranscribe  ) 
                {
                    if( twilioIvrUtils == null )
                        twilioIvrUtils = new TwilioIvrUtils();

                    // IF stored on Twilio, move to S3
                    boolean isTwilio = iir.getAvItemType().getIsTwilio(); //  twilioIvrUtils.getIsTwilio( iir );

                    boolean isLocalHost = RuntimeConstants.getBooleanValue( "isLocalHostForTranscription" );

                    // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() AAA.11 Using AmazonTranscribe. isLocalHost=" + isLocalHost + ", isTwilio=" + isTwilio + ", avItemResponseId=" + iir.getAvItemResponseId() + ", testEventId=" + iir.getTestEventId() );

                    if( isTwilio || isLocalHost )
                    {
                        if( iir.getGoogleStorageName()!= null && !iir.getGoogleStorageName().isEmpty() )
                            googleUri = iir.getGoogleStorageName(); // GoogleStorageUtils.getGoogleUriForVoiceFile( iir.getGoogleStorageName() );

                        //else if( isLocalHost )
                        //{
                        //    googleUri = "http://" + RuntimeConstants.getStringValue("mediaServerDomain") + "/" + RuntimeConstants.getStringValue("mediaServerWebapp") + "/ful/hra" + iir.getAudioUri();
                        
                        //}
                        else
                        {
                            // Store to S3
                            String audFilename = te.getTestEventId() + "_" + iir.getAvItemResponseId() + "_" + (new Date()).getTime() + ".wav";
                            String audContentType = "audio/wav";
                            String audDirectory = RuntimeConstants.getStringValue( "aws.voicestoragebucketdirectory" );

                            InputStream inptStrm = null;
                            if( isLocalHost )
                            {
                                String audFileUri = "http://" + RuntimeConstants.getStringValue("mediaServerDomain") + "/" + RuntimeConstants.getStringValue("mediaServerWebapp") + "/ful/hra" + iir.getAudioUri();
                                inptStrm = HttpFileUtils.getBinaryFileAsInputStream( audFileUri );
                            }
                            
                            else
                            {                                                                
                                inptStrm = twilioIvrUtils.getAudioFileAsInputStream(iir);                                
                            }
                            
                            // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Copying Twilio file to S3 for Amazon Transcribe testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );

                            //if( RuntimeConstants.getBooleanValue("useAwsTempUrlsForMedia") )
                            //    googleUri=FileXferUtils.getPresignedUrlAws( "", audFilename,BucketType.USERUPLOAD.getBucketTypeId(), "temporaryvoicefiles/", 15 );
                            
                            //else
                            //    googleUri = RuntimeConstants.getStringValue( "tempVoiceFilesBaseUrl" ) + "/" + audFilename; 
                            
                            googleUri = "s3://" + BucketType.USERUPLOAD.getBucket() + "/" + BucketType.USERUPLOAD.getBaseKey() + audDirectory + "/" + audFilename;

                            try
                            {
                                if( fileXferUtils==null )
                                    fileXferUtils = new FileXferUtils();

                                // int length = 0;

                                fileXferUtils.saveFileToAws(audDirectory, audFilename, inptStrm, iir.getAudioSize(), audContentType, BucketType.USERUPLOAD.getBucketTypeId() );
                                // fileXferUtils.saveFile(audDirectory, audFilename, inptStrm, audContentType, 0, BucketType.USERUPLOAD.getBucketTypeId() );                                
                                // googleUri = GoogleStorageUtils.storeVoiceFile( inptStrm, audFilename, audContentType );
                            }

                            catch( Exception e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Calling fileXferUtils.saveFile to copy Twilio Audio file. Exception testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" ); 
                                chg = true;
                                googleUri=null;
                            }

                            if( chg )
                            {
                                saveAvItemResponse( iir );
                                chg=false;
                            }

                            if( googleUri != null && !googleUri.isBlank() )
                            {
                                iir.setGoogleStorageName( audFilename );
                                chg=true;                             
                            }
                            else
                            {
                                // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Was unable to store twilio audio file on S3. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );                                
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                chg=true;  
                                needsSpeechToText=false;
                                // throw new Exception( "Cannot Store large audio on Google Cloud. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );                                                        
                            }
                        }                        
                    } // Twilio or local host

                    // Already present on S3 (from video or something)
                    else
                    {
                        // googleUri = iir.getAudioUri();
                        googleUri = ScoreUtils.getS3UrlFromAudioUri( iir.getAudioUri() );
                                                             
                        if( googleUri == null || googleUri.isBlank() )
                        {
                            LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Non-Twilio. No available Audio URI. Expected an AudioUri. Setting temporary error. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                            iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );                                
                            iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                            if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                            {
                                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Non-Twilio. No available Audio URI. Expected an AudioUri. MAX ERROR COUNT reached. Setting error to permanent. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                            }

                            chg=true;  
                            needsSpeechToText=false;
                            // throw new Exception( "Cannot Store large audio on Google Cloud. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );                                                        
                        }
                    }

                    // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe DDD isLocalHost=" + isLocalHost  + ", audioUri=" + googleUri + " testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );                    

                    // Locale loc = getLocale(); //   I18nUtils.getLocaleFromCompositeStr(langCode);
                    if( chg )
                    {
                        if( avEventFacade==null )
                            avEventFacade=AvEventFacade.getInstance();

                        avEventFacade.saveAvItemResponse(iir);  
                        chg = false;
                    }

                    if( googleUri==null || googleUri.isBlank() )
                    {
                        LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() googleUri is empty so ignoring and marking speech text as error. Settnig temporary error count. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                        iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );                                
                        iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                        if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                        {
                            LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() googleUri is empty so ignoring and marking speech text as error. MAX ERROR COUNT reached. Setting error to permanent. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                            iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                        }

                        chg=true;  
                        needsSpeechToText = false;
                    }

                    // Still needs Speech 2 text.
                    if( needsSpeechToText )
                    {
                        String aout = iir.getSpeechTextStatusType().isNotStarted() ? null : "PENDING";
                        String transJobName = iir.getSpeechTextThirdPartyId();

                        if( transJobName==null || transJobName.isBlank() )
                        {
                            transJobName = StringUtils.generateRandomString(11) + "-" + googleUri.substring( googleUri.lastIndexOf("/")+1, googleUri.length() );
                            iir.setSpeechTextThirdPartyId(transJobName);
                            chg = true;
                        }

                        AmazonTranscribeUtils atu = null; // new AmazonTranscribeUtils();
                        // Not started.
                        if( iir.getSpeechTextStatusType().isNotStarted() )
                        {
                            try
                            {
                                atu = new AmazonTranscribeUtils();
                                String mediaFormat = "wav";

                                iir.setSpeechTextRequestDate( new Date() );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.REQUESTED.getSpeechTextStatusTypeId() );
                                if( (iir.getLangCode()==null || iir.getLangCode().isBlank()) && iir.getMediaLocale()!=null )
                                    iir.setLangCode( iir.getMediaLocale().toString() );
                                chg = true;
                                saveAvItemResponse( iir );
                                chg=false;

                                if( iir.getDuration()>0 && iir.getDuration()< AmazonTranscribeUtils.MIN_AMAZON_S2T_DURATION )
                                    throw new Exception( "Start TranscriptionJob Action. NOT SENT to AMAZON. FAILED: Duration too short. Duration=" + iir.getDuration() + ", testEventId=" + iir.getTestEventId() );
                                
                                // Returns FAILED, PENDING, SUCCESS
                                aout = atu.startTranscriptionJob( iir.getMediaLocale(), googleUri, mediaFormat, null, transJobName );    

                                if( aout==null )
                                    throw new Exception( "Start TranscriptionJob Action returned null. iir.avItemResponseId=" + iir.getAvItemResponseId()  + ", testEventId=" + iir.getTestEventId() );

                                if( aout.contains("FAILED") )
                                {
                                    throw new Exception( "Start TranscriptionJob Action. Transcription operation failed. " + aout + ",  iir.avItemResponseId=" + iir.getAvItemResponseId()  + ", testEventId=" + iir.getTestEventId() );
                                }

                                saveAvItemResponse( iir );
                                chg=false;

                                // For successful request, wait
                                // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Started job. Now waiting " + SPEECH2TEXT_ASYNCH_WAIT_SECS + " seconds for Amazon to process." );

                                // Let Google work
                                Thread.sleep( SPEECH2TEXT_ASYNCH_WAIT_SECS*1000 );
                            }

                            catch( IvrScoreException e )
                            {
                                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() ERROR " + e.toString() + ", Amazon Transcribe. Starting Transcription Job. IvrScoreException. Incrementing temporary error count. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                {
                                    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() ERROR " + e.toString() + ", Amazon Transcribe. Starting Transcription Job. IvrScoreException. MAX ERROR COUNT reached. Making error permanent. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                                }

                                if( (iir.getLangCode()==null || iir.getLangCode().isBlank()) && iir.getMediaLocale()!=null )
                                    iir.setLangCode( iir.getMediaLocale().toString() );
                                    
                                transJobName=null;
                                chg = true;
                            }
                            catch( Exception e )
                            {
                                if( e.toString().contains( "Duration too short") || e.toString().contains( "InternalFailureException") )
                                    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() ERROR Amazon Transcribe.  Starting Transcription Job.  testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString());
                                
                                else
                                    LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe.  Starting Transcription Job. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.getMessage());
                                
                                iir.appendNotes( e.toString() + "\n" ); 
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                if( (iir.getLangCode()==null || iir.getLangCode().isBlank()) && iir.getMediaLocale()!=null )
                                    iir.setLangCode( iir.getMediaLocale().toString() );
                                    
                                transJobName=null;

                                chg = true;
                            }

                        }

                        if( chg )
                        {
                            saveAvItemResponse( iir );
                            chg=false;
                        }                            

                        if( iir.getSpeechTextStatusType().isRequested())
                        {
                            try
                            {
                                if( atu==null )
                                    atu = new AmazonTranscribeUtils();

                                String atr = atu.checkTranscriptionJobStatus(transJobName);

                                if( atr == null )
                                    throw new Exception( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Checking status.  Returned null. transJobName=" + transJobName );

                                if( atr.contains( "FAILED" ) )
                                    throw new Exception( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Amazon Transcribe. Checking status. Returned FAILED. transJobName=" + transJobName + ", atr=" + atr );

                                else if( atr.indexOf( "SUCCESS" )>=0 )
                                {                                
                                    // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Checking status.  Returned Success: " + atr + ", transJobName=" + transJobName + ", testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                                    String transcriptUri= atr.substring( 8, atr.length() );
                                    Object[] tjrs = atu.getTranscriptionText(  transcriptUri, true );

                                    String txt = (String) tjrs[0];
                                    Float confidence = (Float) tjrs[1];

                                    // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. AvItemResponseId=" + iir.getAvItemResponseId() +  ",  Transcribed text length is " + (txt==null ? 0 : txt.length()) + ", avg confidence=" + confidence );

                                    List<String> rl = new ArrayList<>();
                                    rl.add( txt );

                                    List<Object[]> ol = new ArrayList<>();                                    
                                    ol.add( new Object[]{rl,confidence} );

                                    s2tResult = new Speech2TextResult(ol);

                                    if( (iir.getLangCode()==null || iir.getLangCode().isBlank()) && iir.getMediaLocale()!=null )
                                        iir.setLangCode( iir.getMediaLocale().toString() );
                                    
                                    iir.setSpeechText( s2tResult.encodeTranscriptForStorage());                                    
                                    // iir.setSpeechText(txt);
                                    iir.setSpeechTextConfidence(confidence);
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.COMPLETE.getSpeechTextStatusTypeId() );
                                    chg = true;
                                    // saveLvAvScore(lvas);
                                }
                                
                                // else if( atr.indexOf( "PENDING" )>=0 )
                                //     LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Checking status. TransJob is still pending. , transJobName=" + transJobName + ", testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );                                

                            }

                            catch( IvrScoreException e )
                            {
                                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() ERROR " + e.toString() + " Amazon Transcribe. Checking status. IvrScoreException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                                iir.appendNotes( e.toString() + "\n" );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                {
                                    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() ERROR " + e.toString() + " Amazon Transcribe. Checking status. IvrScoreException. MAX ERROR COUNT reached. Making error permanent. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                                }

                                if( (iir.getLangCode()==null || iir.getLangCode().isBlank()) && iir.getMediaLocale()!=null )
                                    iir.setLangCode( iir.getMediaLocale().toString() );
                                    
                                googleOperationName=null;
                                chg = true;
                            }   

                            catch( Exception e )
                            {
                                if( e.toString().contains( "The input media file length is too small." ) )
                                    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() MEDIA ERROR Amazon Transcribe. Checking status. Exception testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                else
                                    LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Checking status. Exception testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" ); 
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                {
                                    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon Transcribe. Checking status. Exception. MAX ERROR COUNT reached. Making error permanent. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                                }

                                if( (iir.getLangCode()==null || iir.getLangCode().isBlank()) && iir.getMediaLocale()!=null )
                                    iir.setLangCode( iir.getMediaLocale().toString() );
                                    
                                chg = true;
                            }                            
                        }

                        if( chg )
                        {
                            saveAvItemResponse( iir );
                            chg=false;
                        }


                        // Success!
                        if( iir.getSpeechTextStatusType().isCompleteOrPermanentError() && 
                            (isTwilio || isLocalHost) && 
                             iir.getGoogleStorageName() !=null && !iir.getGoogleStorageName().isEmpty() )
                        {
                            if( fileXferUtils==null )
                                fileXferUtils = new FileXferUtils();

                            String audDirectory = RuntimeConstants.getStringValue( "aws.voicestoragebucketdirectory" );
                            String filename = iir.getGoogleStorageName().substring( iir.getGoogleStorageName().lastIndexOf("/")+1, iir.getGoogleStorageName().length() );

                            try
                            {
                                // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon S2T Deleting Twilio Temp File on S3 filename=" + filename + " directory=" + audDirectory + ", testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                                fileXferUtils.deleteFileAws(audDirectory, filename, BucketType.USERUPLOAD.getBucketTypeId() );
                                // fileXferUtils.deleteFile( audDirectory, filename, BucketType.USERUPLOAD.getBucketTypeId() );
                                iir.setGoogleStorageName( null );
                                chg=true;
                            }
                            catch( Exception e )
                            {
                                LogService.logIt( e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Amazon S2T Error Deleting Twilio Temp File on S3 testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                            }
                        }

                        if( chg )
                        {
                            saveAvItemResponse( iir );
                            chg=false;
                        }

                    }  // End Still needs Speech/Text Amazon

                }  // needs AMAZON speech to text and ok to use base64



                // Need to use Google.
                else if( needsSpeechToText && !useAmazonTranscribe  ) 
                {
                    if( twilioIvrUtils == null )
                        twilioIvrUtils = new TwilioIvrUtils();

                    String s2tCountryCode = iir.getMediaLocale().getCountry();

                    if( te.getIpCountry()!=null && !te.getIpCountry().trim().isEmpty() )
                        s2tCountryCode = te.getIpCountry().trim();

                    String langCode = iir.getMediaLocale().getLanguage() + "-" + s2tCountryCode; // StringUtils.replaceStr( langCode, "_", "-" );

                    LogService.logIt( "AvItemResponsePrepThread. Google S2T. AAA.11 Iniitially set S2T locale to " + langCode + " based on TestTaker IP Country=" + te.getIpCountry() + " and testContentCountry=" + iir.getMediaLocale().getCountry() + ", TestEventId=" + te.getTestEventId() );

                    if( iir.getMediaLocale().getLanguage().equalsIgnoreCase( "zh" ) )
                    {
                        langCode = s2tCountryCode.equalsIgnoreCase("HK") ? "cmn-Hans-HK" : "cmn-Hans-CN";
                        LogService.logIt( "AvItemResponsePrepThread. Google S2T. Changed S2T locale to " + langCode + " because it's chinese. TestEventId=" + te.getTestEventId() );
                    }

                    if( !GoogleSpeechUtils.isLangCodeValidForSpeech2Text(langCode) )
                    {
                        // try the testAdminLocale
                        if( testAdminLocale !=null && testAdminLocale.getCountry()!=null && GoogleSpeechUtils.isLangCodeValidForSpeech2Text(iir.getMediaLocale().getLanguage() + "-" + testAdminLocale.getCountry()) )
                        {
                            langCode = iir.getMediaLocale().getLanguage() + "-" + testAdminLocale.getCountry();
                            LogService.logIt( "AvItemResponsePrepThread. Google S2T. S2T locale not supported. Changed to " + langCode + " based on testAdminLocale. TestEventId=" + te.getTestEventId() );
                        }

                        else // if( GoogleSpeechUtils.isLangCodeValidForSpeech2Text(iir.getMediaLocale().getLanguage() + "-" + iir.getMediaLocale().getCountry()) )
                        {
                            langCode = iir.getMediaLocale().getLanguage() + "-" + iir.getMediaLocale().getCountry();
                            LogService.logIt( "AvItemResponsePrepThread. Google S2T. locale not supported. Changed  to " + langCode + " based on iir.getMediaLocale(). TestEventId=" + te.getTestEventId() );
                        }                            
                    }


                    // For small files, use Base64 String
                    if( iir.getDuration()<=MAX_BASE64_DURATION )
                    {
                        base64Str = twilioIvrUtils.getAudioFileAsBase64( iir );

                        if( base64Str==null || base64Str.isEmpty() )
                        {
                            iir.appendNotes( "Base64Str came back null or empty!\n" ); 
                            iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                            iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                            //if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                            //    iir.setSpeechTextStatusTypeId( IvrItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                            chg = true;
                            LogService.logIt( " Google S2T. Could not encode audio to Base64! Returned: " + base64Str );
                            base64Str="";
                            needsSpeechToText = false;
                            // throw new Exception( "Could not encode audio to Base64." );
                        }

                        // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. got Base64 for item " + iir.getItemUniqueId() + ", testEventId=" + iir.getTestEventId() + ", base64Str.length=" + base64Str.length() + ", langCode=" + langCode );
                    }

                    // for larger files, use Google Storage.
                    else
                    {
                        if( iir.getGoogleStorageName()!= null && !iir.getGoogleStorageName().isEmpty() )
                            googleUri = GoogleStorageUtils.getGoogleUriForVoiceFile( iir.getGoogleStorageName() );

                        else
                        {
                            // Store to Google cloud
                            String audFilename = te.getTestEventId() + "_" + iir.getAvItemResponseId() + "_" + (new Date()).getTime() + ".wav";
                            String audContentType = "audio/wav";

                            InputStream inptStrm = twilioIvrUtils.getAudioFileAsInputStream(iir);

                            try
                            {
                                googleUri = GoogleStorageUtils.storeVoiceFile( inptStrm, audFilename, audContentType );
                            }

                            catch( GoogleApiException e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()   Google S2T. Calling GoogleStorageUtils.storeVoiceFile. GoogleApiException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" ); 
                                chg = true;
                                googleUri=null;
                            }

                            if( chg )
                            {
                                saveAvItemResponse( iir );
                                chg=false;
                            }

                            if( googleUri != null )
                            {
                                iir.setGoogleStorageName( audFilename );
                                chg=true;                             
                            }
                            else
                            {
                                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Google S2T. Cannot Store large audio on Google Cloud. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );                                
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                chg=true;  
                                needsSpeechToText=false;
                                // throw new Exception( "Cannot Store large audio on Google Cloud. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() );                                                        
                            }
                        }
                    }

                    // Locale loc = getLocale(); //   I18nUtils.getLocaleFromCompositeStr(langCode);
                    if( chg )
                    {
                        if( avEventFacade==null )
                            avEventFacade=AvEventFacade.getInstance();

                        avEventFacade.saveAvItemResponse(iir);  
                        chg = false;
                    }

                    if( needsSpeechToText )
                        //    iir.getSpeechTextStatusType().isNotStarted() || 
                        //iir.getSpeechTextStatusType().isRequested() ||
                        //( iir.getSpeechTextStatusType().isError() && iir.getSpeechTextErrorCount()< MAX_SPEECH_TEXT_ERRORS ) )                            
                    {
                        useAsynchSpeech2Text = googleUri != null && !googleUri.isEmpty();

                        googleOperationName = null;
                        s2tResult = null;

                        // Long Operation, not requested.
                        if( useAsynchSpeech2Text && iir.getSpeechTextStatusType().isNotStarted() )
                        {
                            try
                            {
                                longS2TResponse = GoogleSpeechUtils.requestTranscribeAudioAsynch(googleUri, langCode, iir.getAvItemType().getMaxAlternativesForSpeechToText(), iir.getAvItemType().getAudioSampleRate(), null );                                
                                googleOperationName = (String) longS2TResponse[0];
                                s2tResult = (Speech2TextResult) longS2TResponse[1];
                            }

                            catch( GoogleApiException e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. Asynch Post Request. GoogleApiException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" ); 
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                googleOperationName=null;
                                chg = true;
                            }
                            catch( IvrScoreException e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. Asynch. IvrScoreException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                googleOperationName=null;
                                chg = true;
                            }

                            if( chg )
                            {
                                saveAvItemResponse( iir );
                                chg=false;
                            }

                            if( googleOperationName!=null && !googleOperationName.isEmpty() && ( iir.getSpeechTextThirdPartyId()==null || iir.getSpeechTextThirdPartyId().isEmpty() ) )
                            {
                                iir.setSpeechTextThirdPartyId(googleOperationName);
                                iir.setSpeechTextRequestDate( new Date() );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.REQUESTED.getSpeechTextStatusTypeId() );
                                chg = true;

                                // For successful request, wait
                                // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. Asynch. Waiting " + SPEECH2TEXT_ASYNCH_WAIT_SECS + " seconds for Google." );

                                // Let Google work
                                Thread.sleep( SPEECH2TEXT_ASYNCH_WAIT_SECS*1000 );
                            }

                            else
                            {
                                String ee = "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. Asynch. No error thrown but no Google Operation Name found. testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId();
                                LogService.logIt(ee );
                                iir.appendNotes( ee + "\n" );                                
                                chg = true;
                            }                            
                        }

                        if( useAsynchSpeech2Text && iir.getSpeechTextStatusType().isRequested())
                        {
                            try
                            {
                                s2tResult = GoogleSpeechUtils.requestTranscribeAudioAsynchResults( iir.getSpeechTextThirdPartyId() );                                
                            }

                            catch( GoogleApiException e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Google S2T. Asynch Get Results. GoogleApiException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" ); 
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                chg = true;
                            }
                            catch( IvrScoreException e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Google S2T. Asynch Get Results. IvrScoreException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" );
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                googleOperationName=null;
                                chg = true;
                            }   

                            if( chg )
                            {
                                saveAvItemResponse( iir );
                                chg=false;
                            }
                        }

                        if( chg )
                        {
                            saveAvItemResponse( iir );
                            chg=false;
                        }

                        // Instant recognize
                        if( !useAsynchSpeech2Text )
                        {
                            try 
                            {
                                s2tResult = GoogleSpeechUtils.transcribeAudioSynch(base64Str, langCode, iir.getAvItemType().getMaxAlternativesForSpeechToText(), iir.getAvItemType().getAudioSampleRate(), null );
                            }

                            catch( GoogleApiException e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. Synch. GoogleApiException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" );
                                s2tResult = null;
                                chg = true;
                            }

                            catch( IvrScoreException e )
                            {
                                LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. Synch. IvrScoreException testEventId=" + testEventId + ", irr.id=" + iir.getAvItemResponseId() + ", " + e.toString() );
                                iir.appendNotes( e.toString() + "\n" );
                                s2tResult = null;                                
                                chg = true;
                            }

                            if( chg )
                            {
                                saveAvItemResponse( iir );
                                chg=false;
                            }

                            if( s2tResult==null || s2tResult.isUnsuccessful() )
                            {
                                iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() );
                                iir.setSpeechTextErrorCount( iir.getSpeechTextErrorCount()+1 );
                                iir.setSpeechTextConfidence( 0 );

                                if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );

                                iir.setSpeechText( null );
                                chg=true;

                                // Error count Exceeded. Save audio so can troubleshoot.
                                if( iir.getSpeechTextErrorCount()>=MAX_SPEECH_TEXT_ERRORS )
                                {
                                    iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
                                    chg = true;

                                    // if we don't normally store the audio for this item type but it's an error, store it before we delete it from Twilio.
                                    if( iir.getAvItemType().getIsTwilio() && !iir.getAvItemType().getStoreRecordedAudio() && iir.getAudioBytes()==null  )
                                    {
                                        if( twilioIvrUtils == null )
                                            twilioIvrUtils = new TwilioIvrUtils();

                                        twilioIvrUtils.saveAudioFileAsMp3ToDb(iir);
                                        // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. saved MP3 to DB for item " + iir.getItemUniqueId() + ", testEventId=" + iir.getTestEventId() + " because speechToText was unsuccessful.");
                                        chg=false; // Twilio saves it.
                                    }

                                    // Delete from Google no need to keep it there. 
                                    if( iir.getGoogleStorageName()  !=null && !iir.getGoogleStorageName().isEmpty() )
                                    {
                                        GoogleStorageUtils.deleteVoiceFile( iir.getGoogleStorageName() );
                                        iir.setGoogleStorageName( null );
                                        //if( avEventFacade==null )
                                        //    avEventFacade=IvrEventFacade.getInstance();

                                        //avEventFacade.saveAvItemResponse(iir);   
                                        chg=true;
                                    }
                                }


                                // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()  Google S2T. No speech to text result returned for " + iir.toString() + ", " + ( s2tResult==null ? "Speech2TextResult is null" : s2tResult.toString() ) );
                            }
                        } // Synch Request

                        if( chg )
                        {
                            saveAvItemResponse( iir );
                            chg=false;
                        }

                        // Success!
                        if( s2tResult!=null && s2tResult.isSuccessful() )
                        {
                            iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.COMPLETE.getSpeechTextStatusTypeId() );
                            iir.setSpeechTextConfidence( s2tResult.getAvgConfidence() );
                            iir.setSpeechText( s2tResult.encodeTranscriptForStorage());

                            if( iir.getGoogleStorageName()  !=null && !iir.getGoogleStorageName().isEmpty() )
                            {
                                GoogleStorageUtils.deleteVoiceFile( iir.getGoogleStorageName() );
                                iir.setGoogleStorageName( null );
                                chg=true;
                                //needsPass2 = true;
                            }                                
                        }

                    }  // End Still need Google Speech/Text

                }  // END Speech Text Google needs speech to text and ok to use base64

                if( chg )
                {
                    saveAvItemResponse( iir );
                    chg=false;
                }

                ////////////////////////////////////////////////////////
                // TRANSLATE TEXT (If needed)
                ////////////////////////////////////////////////////////

                if( !englishTranslationsOff && iir.needsSpeechTranslation( iir.getMediaLocale() ) )
                {
                    String textToTranslate = getTextForTranslation( iir.getSpeechText() );

                    // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Performing Translation to English. TextToTranslate=" + textToTranslate + ",  avItemResponseId=" + iir.getAvItemResponseId() + ", testEventId=" + iir.getTestEventId() );

                    if( textToTranslate==null || textToTranslate.isBlank() )
                    {
                       LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() textToTranslate is empty. Setting SpeechTextEnglish to blank. avItemResponseId=" + iir.getAvItemResponseId() + ", testEventId=" + iir.getTestEventId() );
                       iir.setSpeechTextEnglish( "" );
                       chg = true;
                    }

                    else
                    {
                        if( languageUtils==null )
                            languageUtils = new LanguageUtils();

                        Object[] out = languageUtils.translateText(textToTranslate, iir.getMediaLocale(), Locale.US, false );
                        // Object[] out = GoogleTranslateUtils.translateText(textToTranslate, iir.getMediaLocale(), Locale.US, false );

                        String speechTextEnglish = "";

                        int errorCode = out[4]==null ? 0 : ((Integer)out[4]);

                        if( errorCode > 0 )
                           throw new Exception( "Google Translate Received Error Code: " + errorCode +", message=" + (String)out[5] );

                        if( out[1]==null )
                            LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Translation API returned null." + iir.toString() );
                        else
                           speechTextEnglish = (String)out[1];

                       iir.setSpeechTextEnglish( StringUtils.truncateString( speechTextEnglish, 2000 ) );
                    }

                   chg = true;
                }



                ////////////////////////////////////////////////////////
                // SAVE
                ////////////////////////////////////////////////////////
                if( chg )
                {
                    saveAvItemResponse( iir );
                    chg=false;
                }

                if( te.getOrg()==null )
                {
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();
                    te.setOrg(userFacade.getOrg( te.getOrgId()));
                }


                ////////////////////////////////////////////////////////
                // POST TO VOICE VIBES
                ////////////////////////////////////////////////////////                
                if( iir.getVoiceVibesStatusType().needsPost() ||  
                    iir.getVoiceVibesStatusType().isPostError() )
                {
                    if( iir.getVoiceVibesStatusType().isPostError() && (iir.getAudioUri()==null || iir.getAudioUri().isBlank()) )
                    {
                        iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                        chg=true;
                    }

                    else
                    {
                        // already posted one.
                        if( hasLiveVoiceVibesPost )
                        {
                            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                            chg=true;
                        }
                        
                        else
                        {
                            voiceVibesUtils.postAudioToVoiceVibes(te.getOrg(), te, iir, irl, base64Str );
                            // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() AAA return voiceVibesId=" + iir.getVoiceVibesId()  + ", avItemResponseId=" + iir.getAvItemResponseId() );
                            chg=false;  // VVUtils saves it.
                            //needsVoiceVibesDelay = true;

                            // this will prevent repeat postings.
                            if( iir.getVoiceVibesStatusType().isPending() || iir.getVoiceVibesStatusType().isAnalysisComplete() )
                                hasLiveVoiceVibesPost=true;
                            
                            if( iir.getVoiceVibesStatusType().isPostError() )
                            {
                                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() ERROR Post to Voice Vibes failed for avItemResponseId=" + iir.getAvItemResponseId() );                            
                                // EmailUtils.getInstance().sendEmailToAdmin( "AvItemResponsePrepThread Voice Vibes Error testEventId=" + testEventId + " avItemResponseId=" + iir.getAvItemResponseId(), "A Voice Vibes POST error occurred for avItemResponseId=" + iir.getAvItemResponseId() + ", TestEventId=" + testEventId );
                            }
                            if( iir.getVoiceVibesStatusType().equals(VoiceVibesStatusType.POST_ERROR_PERMANENT))
                            {
                                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() PERMENANET POST ERROR Post to Voice Vibes failed for avItemResponseId=" + iir.getAvItemResponseId() );                            
                                EmailUtils.getInstance().sendEmailToAdmin( "AvItemResponsePrepThread Voice Vibes Permenent POST Error testEventId=" + testEventId + " avItemResponseId=" + iir.getAvItemResponseId(), "A Voice Vibes Permanent POST Error occurred for avItemResponseId=" + iir.getAvItemResponseId() + ", TestEventId=" + testEventId );
                            }
                        }                        
                    }
                }

                ////////////////////////////////////////////////////////
                // CHECK FOR VV RESULTS
                ////////////////////////////////////////////////////////
                if( iir.getVoiceVibesStatusType().isPending() )
                {
                    voiceVibesUtils.checkForVoiceVibesResults( te.getOrg(), te, iir );
                    // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() BBB return voiceVibesId=" + iir.getVoiceVibesId() + ", avItemResponseId=" + iir.getAvItemResponseId() + " revised voiceVibesStatusTypeId=" + iir.getVoiceVibesStatusTypeId() + " " + iir.getVoiceVibesStatusType().getKey() );

                    if( iir.getVoiceVibesStatusType().isAnalysisCompleteOrError() )
                        chg=false;  // VVUtils saves it.
                }

                ////////////////////////////////////////////////////////
                // DELETE VOICE VIBES SOURCE
                ////////////////////////////////////////////////////////
                if( iir.getVoiceVibesStatusType().readyForDelete() )
                {
                    if( voiceVibesUtils.getVoiceVibesCredentialsAreDemo( te.getOrg(), te ) )
                        LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() skipped deltion of audio because of demo account. VoiceVibesId=" + iir.getVoiceVibesId() + ", avItemResponseId=" + iir.getAvItemResponseId() );
                    else
                    {
                        voiceVibesUtils.deleteVoiceVibesRecording( te.getOrg(), te, iir );
                        // LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords()deleted audio for voiceVibesId=" + iir.getVoiceVibesId() + ", avItemResponseId=" + iir.getAvItemResponseId() );
                        chg=false;  // VVUtils saves it.
                    }
                }

                if( chg )
                {
                    saveAvItemResponse( iir );
                    chg=false;
                }

                ////////////////////////////////////////////////////////
                // DELETE SOURCE
                ////////////////////////////////////////////////////////
                if( deleteSourceAudios && 
                    !iir.getAvItemAudioStatusType().isDeletedRemotely() && 
                      iir.isReadyToDeleteSourceAudio() )
                {
                    // if get here without an exception, can delete the source audio on Twilio
                    if( twilioRestUtils==null )
                        twilioRestUtils = new TwilioRestUtils();

                    boolean deleted = twilioRestUtils.deleteAudioFiles( iir.getAudioThirdPartyId(), iir.getExtraThirdPartyAudioIds() );

                    if( deleted )
                    {
                        LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() DELETED Source Audio for iir.id=" + iir.getAvItemResponseId() + " 3rdPArtyId=" + iir.getAudioThirdPartyId() );

                        if( iir.getAudioStatusTypeId()==AvItemAudioStatusType.STORED_LOCALLY_READY_NOT_DELETED_REMOTELY.getAudioStatusTypeId()  )
                        {
                            iir.setAudioStatusTypeId( AvItemAudioStatusType.STORED_LOCALLY_DELETED_REMOTELY.getAudioStatusTypeId() );
                            iir.setAudioUri(null);
                        }
                        else 
                        {
                            iir.setAudioStatusTypeId( AvItemAudioStatusType.NOT_STORED_LOCALLY_DELETED_REMOTELY.getAudioStatusTypeId() );
                            iir.setAudioUri(null);
                        }

                        chg = true;
                    }
                    else
                        LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() NONFATAL ERROR Unable to DELETE Source Audios for iir.id=" + iir.getAvIntnElementTypeId() + " 3rdPartyId=" + iir.getAudioThirdPartyId() + " extraAudioThirdPartyIds=" + iir.getExtraThirdPartyAudioIds() + ", iir.requiresSpeechToText()=" + iir.requiresSpeechToText() +   ( iir.requiresSpeechToText() ? ", iir.speechToTextSttusType=" + iir.getSpeechTextStatusType().getName() : "" ) );
                }

                //else
                //    LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() SKIPPED DELETE of Source Audio for iir.id=" + iir.getAvIntnElementTypeId() + " 3rdPartyId=" + iir.getAudioThirdPartyId() + ", deleteSourceAudios=" + deleteSourceAudios + ", iir.requiresSpeechToText()=" + iir.requiresSpeechToText() +   ( iir.requiresSpeechToText() ? ", iir.speechToTextSttusType=" + iir.getSpeechTextStatusType().getName() : "" ) );

                ////////////////////////////////////////////////////////
                // SAVE
                ////////////////////////////////////////////////////////
                if( iir.getAvItemScoringStatusType().equals( AvItemScoringStatusType.INVALID) )
                {
                    iir.setScoringStatusTypeId( AvItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );
                    chg=true;
                    Tracker.addAvItemResponseCompletion();
                }
                else if( iir.getAvItemScoringStatusType().equals( AvItemScoringStatusType.SKIPPED) )
                {
                    iir.setScoringStatusTypeId( AvItemScoringStatusType.SKIPPED_READY_FOR_SCORING.getScoringStatusTypeId() );
                    chg=true;
                    Tracker.addAvItemResponseCompletion();
                }

                else if( iir.isReadyForScoring() && iir.getAvItemScoringStatusType().equals( AvItemScoringStatusType.NOT_READY_FOR_SCORING) )
                {
                    iir.setScoringStatusTypeId( AvItemScoringStatusType.READY_FOR_SCORING.getScoringStatusTypeId() );
                    chg=true;
                    Tracker.addAvItemResponseCompletion();
                }

                //}
                //catch( Exception e )
                //{
                //    LogService.logIt(e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() testEventId=" + testEventId + ", " + iir.toString() );
                //    throw e;
                //}

                if( chg )
                {
                    saveAvItemResponse( iir );
                    chg=false;
                } 
            }
            
            // OUTER PASS 2 CATCH
            catch( IOException e )
            {
                LogService.logIt( "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() ERROR " + e.toString() + " Pass 2 avItemResponseId=" + ( iir==null ? "null" : iir.getAvItemResponseId() +", teid=" + iir.getTestEventId() ) );
                throw e;
            }            
            catch( Exception e )
            {
                LogService.logIt( e, "AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() Pass 2 avItemResponseId=" + ( iir==null ? "null" : iir.getAvItemResponseId() +", teid=" + iir.getTestEventId() ) );
                throw e;
            }            
        } // Next AvItemResponse
        
        StringBuilder sb = new StringBuilder();
        
        for( AvItemResponse ir : processedIrl )
        {
            if( ir.isReadyForScoring() )
                continue;
            
            if( sb.length()>0 )
                sb.append( ",\nPending AvItemResponse: " );

            sb.append( ir.getAvItemResponseId() + ", " + ir.getItemUniqueId() + ", not ready for scoring. Speech Status Type: " + ir.getSpeechTextStatusType().getName() + " Audio Status Type: " + ir.getAvItemAudioStatusType().getName() + ", Voice Vibes: " + ir.getVoiceVibesStatusType().getKey() );
        }
        
        // LogService.logIt("AvItemResponsePrepThread.cleanUnscoredAvItemResponseRecords() testEventId=" + testEventId + ", Process Complete! " + ( sb.length()==0 ? "ALL AvItemResponses now ready for scoring." : "Still waiting on the following AvItemResponses: " + sb.toString()) );
    }
    


    private String getTextForTranslation( String inStr )
    {
        try
        {
            if( inStr == null || inStr.trim().isEmpty() )
                return "";

            inStr = inStr.trim();

            Speech2TextResult s2tr = new Speech2TextResult( inStr );

            return s2tr.getConcatTranscript();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AvItemResponsePrepThread.getTextForTranslation() inStr=" + inStr );
            
            return "";
        }
    }
    



    public void setInitialDelaySecs(int seconds) {
        this.initialDelaySecs = seconds;
    }



    
    private void setVibesAndEssayStatusTypes( AvItemResponse iir, TestEvent te, Locale testAdminLocale, Locale mediaLocale ) throws Exception
    {
        if( iir==null )
            return;
        
        boolean needsEssayStg = iir.getEssayScoreStatusTypeId()==AvItemEssayStatusType.NOT_SET.getEssayStatusTypeId();
        
        boolean needsVibesStg = iir.getVoiceVibesStatusTypeId()==VoiceVibesStatusType.NOT_SET.getVoiceVibesStatusTypeId();
        
        // already set
        if( !needsVibesStg && !needsEssayStg )
            return;
        
        if( mediaLocale==null )
            mediaLocale = testAdminLocale;
        
        if( needsVibesStg )
        {
            if( forceVibesOff )
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                needsVibesStg=false;                            
            }
            
            else if( mediaLocale!=null && !mediaLocale.getLanguage().equalsIgnoreCase("en"))
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                needsVibesStg=false;            
            }

            else if( iir.getAvItemScoringStatusType().isAnySkipped() || 
                iir.getAvItemScoringStatusType().isAnyInvalid() || 
                !iir.getAvItemType().requiresRecordVoice() || 
                !iir.getAvItemType().supportsVoiceVibesAnalysis() )
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                needsVibesStg=false;            
            }
            
            else if( iirForVibes!=null && iirForVibes.getAvItemResponseId()!=iir.getAvItemResponseId() )
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                needsVibesStg=false;                            
            }
            
            else
            {
                VoiceVibesUtils voiceVibesUtils = new VoiceVibesUtils();

                if( !voiceVibesUtils.isVoiceVibesOn() )
                {
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                    needsVibesStg=false;            
                }
            }
        }
        
        // already set
        if( !needsVibesStg && !needsEssayStg )
            return;
        
        if( needsVibesStg && iirForVibes!=null && iirForVibes.getAvItemResponseId()==iir.getAvItemResponseId() )
        {
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_POSTED.getVoiceVibesStatusTypeId() );
            needsVibesStg=false;   
        }

        SimJ.Intn theIntn = null;

        if( te.getSimXmlObj()==null )
        {
            SimDescriptor sd = eventFacade.getSimDescriptor(te.getSimId(), te.getSimVersionId(), true );

            if( sd == null )
                throw new Exception( "No sim descriptor found for simId=" + te.getSimVersionId() + ", " + te.toString() );

            te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ) );
        }

        if( iir.getItemUniqueId()!=null && !iir.getItemUniqueId().isEmpty() )
        {
            int ct = 0;
            SimJ.Intn ii=null;
            for( SimJ.Intn intn : te.getSimXmlObj().getIntn() )
            {
                if( intn.getUniqueid()!=null && intn.getUniqueid().equals( iir.getItemUniqueId() ) )
                {
                    ct++;
                    ii = intn;
                    //theIntn = intn;
                    // break;
                }
            }
            if( ii!=null && ct==1 )
                theIntn=ii;
        }        
        
        if( theIntn==null )
        {
            for( SimJ.Intn intn : te.getSimXmlObj().getIntn() )
            {
                if( intn.getSeq()==iir.getItemSeq() )
                {
                    theIntn = intn;
                    break;
                }
            }
        }
        
        if( needsVibesStg )
        {
            if( iir.getUploadedUserFileId()> 0 )
            {
                if( theIntn != null && theIntn.getTextscoreparam1()!=null && IvrStringUtils.containsKey("[VIBES]", theIntn.getTextscoreparam1(), true ) )
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_POSTED.getVoiceVibesStatusTypeId() );
                else
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );              
            }

            else
            {
                if( theIntn != null && theIntn.getTextscoreparam1()!=null && IvrStringUtils.containsKey("[NOVIBES]", theIntn.getTextscoreparam1(), true ) )
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_REQUIRED.getVoiceVibesStatusTypeId() );
                else
                    iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_POSTED.getVoiceVibesStatusTypeId() );              
            }
        }
        
        if( needsEssayStg )
        {
            if( theIntn != null && theIntn.getTextscoreparam1()!=null && IvrStringUtils.containsKey("[ESSAYPROMPT]", theIntn.getTextscoreparam1(), true ) )
                iir.setEssayStatusTypeId( AvItemEssayStatusType.NOT_REQUESTED.getEssayStatusTypeId() );
            else
                iir.setEssayStatusTypeId( AvItemEssayStatusType.NOT_REQUIRED.getEssayStatusTypeId() );                          
        }

        
    }

    
    private boolean isVibesRequested( AvItemResponse iir, TestEvent te, Locale testAdminLocale, Locale mediaLocale ) throws Exception
    {
        if( iir==null )
            return false;
                
        if( mediaLocale==null )
            mediaLocale = testAdminLocale;
        
        if( mediaLocale!=null && !mediaLocale.getLanguage().equalsIgnoreCase("en"))
            return false;
        

        SimJ.Intn theIntn = null;

        if( te.getSimXmlObj()==null )
        {
            SimDescriptor sd = eventFacade.getSimDescriptor(te.getSimId(), te.getSimVersionId(), true );

            if( sd == null )
                throw new Exception( "No sim descriptor found for simId=" + te.getSimVersionId() + ", " + te.toString() );

            te.setSimXmlObj( JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ) );
        }

        if( iir.getItemUniqueId()!=null && !iir.getItemUniqueId().isEmpty() )
        {
            int ct = 0;
            SimJ.Intn ii=null;
            for( SimJ.Intn intn : te.getSimXmlObj().getIntn() )
            {
                if( intn.getUniqueid()!=null && intn.getUniqueid().equals( iir.getItemUniqueId() ) )
                {
                    ct++;
                    ii = intn;
                    //theIntn = intn;
                    // break;
                }
            }
            if( ii!=null && ct==1 )
                theIntn=ii;
        }        
        
        if( theIntn==null )
        {
            for( SimJ.Intn intn : te.getSimXmlObj().getIntn() )
            {
                if( intn.getSeq()==iir.getItemSeq() )
                {
                    theIntn = intn;
                    break;
                }
            }
        }
        
        if( theIntn==null )
            return false;
        
        if( iir.getUploadedUserFileId()> 0 )
            return theIntn.getTextscoreparam1()!=null && IvrStringUtils.containsKey("[VIBES]", theIntn.getTextscoreparam1(), true ); 

        else
            return !(theIntn.getTextscoreparam1()!=null && IvrStringUtils.containsKey("[NOVIBES]", theIntn.getTextscoreparam1(), true ));
    }
    
    

    public boolean isRetrySpeechToExtIfError() {
        return retrySpeechToExtIfError;
    }

    public void setRetrySpeechToExtIfError(boolean retrySpeechToExtIfError) {
        this.retrySpeechToExtIfError = retrySpeechToExtIfError;
    }

    public boolean isRetryVoiceVibesPostIfError() {
        return retryVoiceVibesPostIfError;
    }

    public void setRetryVoiceVibesPostIfError(boolean retryVoiceVibesPostIfError) {
        this.retryVoiceVibesPostIfError = retryVoiceVibesPostIfError;
    }


    private boolean resetAvItemResponseForSpeechToTextError( AvItemResponse iir )
    {
        if( !iir.getSpeechTextStatusType().isTempError() ) //!=IvrItemSpeechTextStatusType.ERROR_TEMPORARY.getSpeechTextStatusTypeId() )
            return false;
        
        if( iir.getSpeechText()!=null && !iir.getSpeechText().trim().isEmpty() )
        {
            iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.COMPLETE.getSpeechTextStatusTypeId() );
            return true;
        }
        
        if( iir.getSpeechTextErrorCount()>=AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS )
        {
            iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.ERROR_PERMANENT.getSpeechTextStatusTypeId() );
            iir.setScoringStatusTypeId( AvItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );
            return true;
        }
            
        else
        {
            iir.setSpeechTextStatusTypeId( AvItemSpeechTextStatusType.NOT_STARTED.getSpeechTextStatusTypeId() );
            return true;
        }
        
        // iir.setSpeechTextErrorCount( 0 );
    }

    
    private boolean resetAvItemResponseForVoiceVibesPostError( AvItemResponse iir )
    {
        if( !iir.getVoiceVibesStatusType().isPostError() )
            return false;
        
        if( iir.getVoiceVibesResponseStr()!=null && !iir.getVoiceVibesResponseStr().isEmpty()  )
        {
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.ANALYSIS_COMPLETE.getVoiceVibesStatusTypeId()  );
            return true;
        }
        
        if( iir.getVoiceVibesId()!=null && !iir.getVoiceVibesId().isEmpty() )
        {
            iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POSTED.getVoiceVibesStatusTypeId()  );
            return true;
        }
        
        else 
        {
            if( iir.getVoiceVibesPostErrorCount()>=AvItemResponsePrepThread.MAX_VOICE_VIBES_POST_ERRORS )
            {
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.POST_ERROR_PERMANENT.getVoiceVibesStatusTypeId() );
                // iir.setScoringStatusTypeId( IvrItemScoringStatusType.INVALID_READY_FOR_SCORING.getScoringStatusTypeId() );
            }
            
            else
                iir.setVoiceVibesStatusTypeId( VoiceVibesStatusType.NOT_POSTED.getVoiceVibesStatusTypeId()  );
            
            return true;
        }
        
        // iir.setVoiceVibesPostErrorCount( 0 );
    }
    

    private void saveAvItemResponse( AvItemResponse iir ) throws Exception
    {
        if( avEventFacade==null )
            avEventFacade=AvEventFacade.getInstance();

        avEventFacade.saveAvItemResponse(iir);  
    }


    
}
