package com.tm2score.entity.event;

import com.tm2score.av.AvItemAudioStatusType;
import com.tm2score.av.AvItemEssayStatusType;
import com.tm2score.av.AvItemScoringStatusType;
import com.tm2score.av.AvItemSpeechTextStatusType;
import com.tm2score.av.AvItemType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.voicevibes.VoiceVibesAccountType;
import com.tm2score.voicevibes.VoiceVibesStatusType;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author Mike
 */
@Entity
@Table(name = "avitemresponse")
@NamedQueries(
{
    @NamedQuery(name = "AvItemResponse.findByAvItemResponseId", query = "SELECT o FROM AvItemResponse AS o WHERE o.avItemResponseId=:avItemResponseId"),
    @NamedQuery(name = "AvItemResponse.findByTestEventId", query = "SELECT o FROM AvItemResponse AS o WHERE o.testEventId=:testEventId ORDER BY o.avItemResponseId"),
    // @NamedQuery( name = "AvItemResponse.findByTestEventIdAndItemUniqueId", query = "SELECT o FROM AvItemResponse AS o WHERE o.testEventId=:testEventId AND o.itemUniqueId=:itemUniqueId ORDER BY o.avItemResponseId" ),
    @NamedQuery(name = "AvItemResponse.findByTestEventIdAndItemSeq", query = "SELECT o FROM AvItemResponse AS o WHERE o.testEventId=:testEventId AND o.itemSeq=:itemSeq ORDER BY o.avItemResponseId")
})
public class AvItemResponse implements Comparable<AvItemResponse>, Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avitemresponseid")
    private long avItemResponseId;

    @Column(name = "testeventid")
    private long testEventId;

    @Column(name = "simid")
    private long simId;

    @Column(name = "simversionid")
    private int simVersionId;

    @Column(name = "itemseq")
    private int itemSeq;

    @Column(name = "itemsubseq")
    private int itemSubSeq;

    @Column(name = "uploadeduserfileid")
    private long uploadedUserFileId;

    @Column(name = "unscoredessayid")
    private int unscoredEssayId;

    @Column(name = "essaystatustypeid")
    private int essayStatusTypeId;

    @Column(name = "itemuniqueid")
    private String itemUniqueId;

    @Column(name = "avItemTypeId")
    private int avItemTypeId;

    @Column(name = "ivrTranTypeId")
    private int ivrTranTypeId;

    @Column(name = "scoringstatustypeid")
    private int scoringStatusTypeId;

    @Column(name = "speechtextsimilarity")
    private float speechTextSimilarity;

    @Column(name = "similarity")
    private float similarity;

    @Column(name = "confidence")
    private float confidence;

    @Column(name = "rawscore")
    private float rawScore;

    @Column(name = "score")
    private float score;

    @Column(name = "scorestr")
    private String scoreStr;

    @Column(name = "assignedpoints")
    private float assignedPoints;

    @Column(name = "playcount")
    private int playCount;

    @Column(name = "displayorder")
    private int displayOrder;

    @Column(name = "savelocalaudio")
    private int saveLocalAudio;

    @Column(name = "audiostatustypeid")
    private int audioStatusTypeId;

    @Column(name = "avintnelementtypeid")
    private String avIntnElementTypeId;

    @Column(name = "dtmf")
    private String dtmf;

    @Column(name = "selectedsubnodeseq")
    private int selectedSubnodeSeq;

    @Column(name = "duration")
    private float duration;

    @Column(name = "langcode")
    private String langCode;

    /**
     * Packed string tran1a,tran1b,tran1c;confidence1;tran2a,tran2b|confidence2;
     * ...
     */
    @Column(name = "speechtext")
    private String speechText;

    @Column(name = "speechtextconfidence")
    private float speechTextConfidence;

    @Column(name = "speechtextstatustypeid")
    private int speechTextStatusTypeId;

    @Column(name = "essaymachinescore")
    private float essayMachineScore;

    @Column(name = "essayconfidence")
    private float essayConfidence;

    @Column(name = "essayplagiarized")
    private int essayPlagiarized;

    @Column(name = "speechtexterrorcount")
    private int speechTextErrorCount;

    @Column(name = "speechtextthirdpartyid")
    private String speechTextThirdPartyId;

    @Column(name = "audiouri")
    private String audioUri;

    @Column(name = "audiothirdpartyid")
    private String audioThirdPartyId;

    @Column(name = "extrathirdpartyaudioids")
    private String extraThirdPartyAudioIds;

    @Column(name = "voicevibesstatustypeid")
    private int voiceVibesStatusTypeId;

    @Column(name = "voicevibesaccounttypeid")
    private int voiceVibesAccountTypeId;

    @Column(name = "voicevibesresponsestr")
    private String voiceVibesResponseStr;

    @Column(name = "voicevibesoverallscore")
    private float voiceVibesOverallScore;

    @Column(name = "voicevibesoverallscorehra")
    private float voiceVibesOverallScoreHra;

    @Column(name = "voicevibesposterrorcount")
    private int voiceVibesPostErrorCount;

    @Column(name = "voicevibesid")
    private String voiceVibesId;

    @Column(name = "googlestoragename")
    private String googleStorageName;

    @Column(name = "speechtextenglish")
    private String speechTextEnglish;

    @Column(name = "aicalltypeid")
    private int aiCallTypeId;

    @Column(name = "aicallhistoryid")
    private long aiCallHistoryId;

    @Column(name = "metascore1")
    private float metaScore1;

    @Column(name = "metascoretypeid1")
    private int metaScoreTypeId1;

    @Column(name = "metascore2")
    private float metaScore2;

    @Column(name = "metascoretypeid2")
    private int metaScoreTypeId2;

    @Column(name = "metascore3")
    private float metaScore3;

    @Column(name = "metascoretypeid3")
    private int metaScoreTypeId3;

    @Column(name = "metascore4")
    private float metaScore4;

    @Column(name = "metascoretypeid4")
    private int metaScoreTypeId4;

    @Column(name = "metascore5")
    private float metaScore5;

    @Column(name = "metascoretypeid5")
    private int metaScoreTypeId5;

    @Column(name = "notes")
    private String notes;

    @Column(name = "audiobytes")
    private byte[] audioBytes;

    @Column(name = "audiosize")
    private int audioSize;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "speechtextrequestdate")
    private Date speechTextRequestDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "voicevibesrequestdate")
    private Date voiceVibesRequestDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdate")
    private Date createDate;

    @Transient
    private Locale mediaLocale;

    public AvItemResponse()
    {
    }

    @Override
    public int compareTo(AvItemResponse o)
    {

        if (this.itemUniqueId != null && !this.itemUniqueId.isEmpty() && o.getItemUniqueId() != null)
            return itemUniqueId.compareTo(o.getItemUniqueId());

        return 0;
    }

    @Override
    public String toString()
    {
        return "AvItemResponse{" + "avItemResponseId=" + avItemResponseId + ", testEventId=" + testEventId + ", itemSeq=" + itemSeq + ", itemUniqueId=" + itemUniqueId + ", avItemTypeId=" + avItemTypeId + ", ivrTranTypeId=" + ivrTranTypeId + ", selectedSubnodeSeq=" + selectedSubnodeSeq + " duration=" + duration + ", voiceVibesStatusTypeId=" + voiceVibesStatusTypeId + ", speechTextEnglish=" + (speechTextEnglish == null ? "null" : speechTextEnglish) + '}';
    }

    public boolean hasPlayableMedia()
    {
        if (audioBytes != null && audioBytes.length > 100)
            return true;

        if (audioUri != null && !audioUri.isEmpty())
            return true;

        if (uploadedUserFileId > 0)
            return true;

        return false;
    }

    public boolean isVoiceVibesPostReadyForResultsPull() throws Exception
    {
        if (!getVoiceVibesStatusType().equals(VoiceVibesStatusType.POSTED))
            throw new Exception("VoiceVibesStatusTypeId is not in a Posted state. Expected " + VoiceVibesStatusType.POSTED.getVoiceVibesStatusTypeId() + " but found " + this.voiceVibesStatusTypeId);

        if (this.voiceVibesRequestDate == null)
            throw new Exception("VoiceVibesRequestDate is null.");

        Calendar cal = new GregorianCalendar();

        cal.add(Calendar.MINUTE, -1 * Constants.VIBES_WAIT_TIME_MINS);

        return voiceVibesRequestDate.before(cal.getTime());

    }

    public AvItemScoringStatusType getAvItemScoringStatusType()
    {
        return AvItemScoringStatusType.getValue(this.scoringStatusTypeId);
    }

    public AvItemAudioStatusType getAvItemAudioStatusType()
    {
        return AvItemAudioStatusType.getValue(this.audioStatusTypeId);
    }

    public VoiceVibesAccountType getVoiceVibesAccountType()
    {
        return VoiceVibesAccountType.getValue(this.voiceVibesAccountTypeId);
    }

    public AvItemType getAvItemType()
    {
        return AvItemType.getValue(avItemTypeId);
    }

    public boolean containsPendingAudio()
    {
        return audioUri != null && !audioUri.isEmpty() && getAvItemAudioStatusType().isPendingFromSource();
    }

    public boolean needsSpeechTranslation(Locale loc)
    {
        if (loc.getLanguage().equalsIgnoreCase("en") || !requiresSpeechToText() || this.getSpeechTextStatusType().isNotRequired())
            return false;

        if (speechTextEnglish != null) // && !speechTextEnglish.isEmpty() )
            return false;

        return getSpeechTextStatusType().isComplete() && speechText != null;
    }

    public boolean isReadyToDeleteSourceAudio()
    {
        if (uploadedUserFileId > 0)
            return false;

        // needs speech to text and either it's not complete or there haven't been enough tries
        if (requiresSpeechToText())
        {
            // error but try again later.
            if (getSpeechTextStatusType().isTempError()) // && this.speechTextErrorCount<AvItemResponsePrepThread.MAX_SPEECH_TEXT_ERRORS ) // && getSpeechTextErrorCount()< IvrTestEventScorer.MAX_SPEECH_TEXT_ERRORS )
                return false;

            // Not complete or error for any reason
            if (!getSpeechTextStatusType().isComplete() && !getSpeechTextStatusType().isPermanentError()) // && !getSpeechTextStatusType().isError()  )
                return false;
        }

        // voice vibes not ready
        if (!getVoiceVibesStatusType().isReadyToDeleteSourceAudio())
            return false;

        // hasn't been copied.
        if ((saveLocalAudio == 1 || getAvItemType().getStoreRecordedAudio()) && (getAudioBytes() == null || audioBytes.length == 0))
            return false;

        return true;
    }

    public boolean hasVoiceVibesReport()
    {
        return getVoiceVibesStatusType().hasValidReport() && getVoiceVibesResponseStr() != null && !getVoiceVibesResponseStr().isEmpty();
    }

    public boolean isScoreCompleteOrError()
    {
        return this.getAvItemScoringStatusType().isScoringCompleteOrError();
    }

    public VoiceVibesStatusType getVoiceVibesStatusType()
    {
        return VoiceVibesStatusType.getValue(voiceVibesStatusTypeId);
    }

    public AvItemEssayStatusType getEssayStatusType()
    {
        return AvItemEssayStatusType.getValue(essayStatusTypeId);
    }

    public boolean isValidRawResponse()
    {
        if (this.getAvItemScoringStatusType().isSkipped())
            return true;

        //if (getAvItemType().requiresDistractorValues() && selectedSubnodeSeq <= 0)
        //    return false;

        if (getAvItemScoringStatusType().isInvalid())
            return false;

        return true;
    }

    public boolean isPendingScoring() throws Exception
    {
        return this.getEssayStatusType().isRequested();
    }

    public boolean isReadyForScoring() throws Exception
    {
        // contains pending audio
        if (getAvItemAudioStatusType().isPendingFromSource())
            return false;

        // if already marked as skipped or invalid. will be treated as wrong or ignored.
        // if( getIvrItemScoringStatusType().isSkipped() || getIvrItemScoringStatusType().isInvalid() || getIvrItemScoringStatusType().isReadyForScoring() )
        if (getAvItemScoringStatusType().isReadyForScoring())
            return true;

        // if has required distractor values
        //if (getAvItemType().requiresDistractorValues())
        //{
        //    if (selectedSubnodeSeq <= 0)
        //        throw new Exception("AvItemResponse.isReadyForScoring() AvItemResponse uses dtmf but no selected subnode seq. " + toString());

        //    return true;
        //}

        // audio present, and required, but no bytes.  audio not stored in dbms
        if (getAvItemType().getStoreRecordedAudio() && getAudioStatusType().isPresent() && (audioBytes == null || audioBytes.length == 0))
        {
            // LogService.logIt( "AvItemResponse.isReadyForScoring() avItemType stores audio and AudioStatusType.isPresent but no audioBytes. avItemResponseId=" + avItemResponseId + ", testEventId=" + testEventId  );
            return false;
        }

        // Voice Vibes done?
        if (!getVoiceVibesStatusType().readyForScoring())
        {
            // LogService.logIt( "AvItemResponse.isReadyForScoring() avItemType not ready for Voice Vibes.  avItemResponseId=" + avItemResponseId + ", testEventId=" + testEventId );
            return false;
        }

        // Speech to Text Done
        if (requiresSpeechToText() && !isSpeechToTextCompleteOrPermanentError())
        {
            // LogService.logIt( "AvItemResponse.isReadyForScoring() avItemType Needs speech to text.  avItemResponseId=" + avItemResponseId + ", testEventId=" + testEventId );
            return false;
        }

        return true;
    }

    public AvItemAudioStatusType getAudioStatusType()
    {
        return AvItemAudioStatusType.getValue(this.audioStatusTypeId);
    }

    public boolean requiresSpeechToText()
    {
        return getAvItemType().requiresRecordVoice(); //  && (speechText==null );
    }

    public boolean isSpeechToTextCompleteOrPermanentError()
    {
        return getSpeechTextStatusType().isComplete()
                || getSpeechTextStatusType().isNotRequired()
                || getSpeechTextStatusType().isPermanentError();
    }

    public AvItemSpeechTextStatusType getSpeechTextStatusType()
    {
        return AvItemSpeechTextStatusType.getValue(this.speechTextStatusTypeId);
    }
    //public AvItemSpeechTextStatusType getSpeechTextStatusType()
    //{
    //    return AvItemSpeechTextStatusType.getValue( this.speechTextStatusTypeId );
    //}

    public void appendNotes(String note)
    {
        if (notes == null)
            notes = "";

        notes += note + "\n";

        if (notes.length() > 2000)
            notes = notes.substring(notes.length() - 2000, notes.length());
    }

    public String getAvItemResponseIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt(new Long(avItemResponseId).toString());
        } catch (Exception e)
        {
            LogService.logIt(e, "AvItemResponse.getAvItemResponseIdEncrypted() " + toString());

            return "";
        }
    }

    public long getAvItemResponseId()
    {
        return avItemResponseId;
    }

    public void setAvItemResponseId(long avItemResponseId)
    {
        this.avItemResponseId = avItemResponseId;
    }

    public long getTestEventId()
    {
        return testEventId;
    }

    public void setTestEventId(long testEventId)
    {
        this.testEventId = testEventId;
    }

    public long getSimId()
    {
        return simId;
    }

    public void setSimId(long simId)
    {
        this.simId = simId;
    }

    public int getItemSeq()
    {
        return itemSeq;
    }

    public void setItemSeq(int itemSeq)
    {
        this.itemSeq = itemSeq;
    }

    public String getAvIntnElementTypeId()
    {
        return avIntnElementTypeId;
    }

    public void setAvIntnElementTypeId(String t)
    {
        this.avIntnElementTypeId = t;
    }

    public String getItemUniqueId()
    {
        return itemUniqueId;
    }

    public void setItemUniqueId(String itemUniqueId)
    {
        this.itemUniqueId = itemUniqueId;
    }

    public int getAvItemTypeId()
    {
        return avItemTypeId;
    }

    public void setAvItemTypeId(int ivrItemTypeId)
    {
        this.avItemTypeId = ivrItemTypeId;
    }

    public float getDuration()
    {
        return duration;
    }

    public void setDuration(float duration)
    {
        this.duration = duration;
    }

    public String getSpeechText()
    {
        return speechText;
    }

    public void setSpeechText(String speechText)
    {
        this.speechText = speechText;
    }

    public String getSpeechTextEnglish()
    {
        return speechTextEnglish;
    }

    public void setSpeechTextEnglish(String speechTextEnglish)
    {
        this.speechTextEnglish = speechTextEnglish;
    }

    public int getSimVersionId()
    {
        return simVersionId;
    }

    public void setSimVersionId(int simVersionId)
    {
        this.simVersionId = simVersionId;
    }

    public byte[] getAudioBytes()
    {
        return audioBytes;
    }

    public void setAudioBytes(byte[] audioBytes)
    {
        this.audioBytes = audioBytes;
    }

    public Date getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(Date createDate)
    {
        this.createDate = createDate;
    }

    public String getAudioUri()
    {
        return audioUri;
    }

    public void setAudioUri(String audioUri)
    {
        this.audioUri = audioUri;
    }

    public int getAudioStatusTypeId()
    {
        return audioStatusTypeId;
    }

    public void setAudioStatusTypeId(int audioStatusTypeId)
    {
        this.audioStatusTypeId = audioStatusTypeId;
    }

    public String getDtmf()
    {
        return dtmf;
    }

    public void setDtmf(String dtmf)
    {
        this.dtmf = dtmf;
    }

    public int getSelectedSubnodeSeq()
    {
        return selectedSubnodeSeq;
    }

    public void setSelectedSubnodeSeq(int selectedSubnodeSeq)
    {
        this.selectedSubnodeSeq = selectedSubnodeSeq;
    }

    public int getIvrTranTypeId()
    {
        return ivrTranTypeId;
    }

    public void setIvrTranTypeId(int ivrTranTypeId)
    {
        this.ivrTranTypeId = ivrTranTypeId;
    }

    public String getAudioThirdPartyId()
    {
        return audioThirdPartyId;
    }

    public void setAudioThirdPartyId(String audioThirdPartyId)
    {
        this.audioThirdPartyId = audioThirdPartyId;
    }

    public int getScoringStatusTypeId()
    {
        return scoringStatusTypeId;
    }

    public void setScoringStatusTypeId(int scoringStatusTypeId)
    {
        this.scoringStatusTypeId = scoringStatusTypeId;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public int getAudioSize()
    {
        return audioSize;
    }

    public void setAudioSize(int audioSize)
    {
        this.audioSize = audioSize;
    }

    public float getSpeechTextConfidence()
    {
        return speechTextConfidence;
    }

    public void setSpeechTextConfidence(float speechTextConfidence)
    {
        this.speechTextConfidence = speechTextConfidence;
    }

    public float getSpeechTextSimilarity()
    {
        return speechTextSimilarity;
    }

    public void setSpeechTextSimilarity(float speechTextSimilarity)
    {
        this.speechTextSimilarity = speechTextSimilarity;
    }

    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    public String getExtraThirdPartyAudioIds()
    {
        return extraThirdPartyAudioIds;
    }

    public void setExtraThirdPartyAudioIds(String extraThirdPartyAudioIds)
    {
        this.extraThirdPartyAudioIds = extraThirdPartyAudioIds;
    }

    public int getSpeechTextStatusTypeId()
    {
        return speechTextStatusTypeId;
    }

    public void setSpeechTextStatusTypeId(int speechTextStatusTypeId)
    {
        this.speechTextStatusTypeId = speechTextStatusTypeId;
    }

    public int getSpeechTextErrorCount()
    {
        return speechTextErrorCount;
    }

    public void setSpeechTextErrorCount(int speechTextErrorCount)
    {
        this.speechTextErrorCount = speechTextErrorCount;
    }

    public int getPlayCount()
    {
        return playCount;
    }

    public void setPlayCount(int playCount)
    {
        this.playCount = playCount;
    }

    public float getSimilarity()
    {
        return similarity;
    }

    public void setSimilarity(float similarity)
    {
        this.similarity = similarity;
    }

    public float getConfidence()
    {
        return confidence;
    }

    public void setConfidence(float confidence)
    {
        this.confidence = confidence;
    }

    public float getRawScore()
    {
        return rawScore;
    }

    public void setRawScore(float rawScore)
    {
        this.rawScore = rawScore;
    }

    public int getDisplayOrder()
    {
        return displayOrder;
    }

    public void setDisplayOrder(int displayorder)
    {
        this.displayOrder = displayorder;
    }

    public String getScoreStr()
    {
        return scoreStr;
    }

    public void setScoreStr(String scoreStr)
    {
        this.scoreStr = scoreStr;
    }

    public int getVoiceVibesStatusTypeId()
    {
        return voiceVibesStatusTypeId;
    }

    public void setVoiceVibesStatusTypeId(int voicevibesStatusTypeId)
    {
        this.voiceVibesStatusTypeId = voicevibesStatusTypeId;
    }

    public String getVoiceVibesResponseStr()
    {
        return voiceVibesResponseStr;
    }

    public void setVoiceVibesResponseStr(String voiceVibesResponseStr)
    {
        this.voiceVibesResponseStr = voiceVibesResponseStr;
    }

    public String getVoiceVibesId()
    {
        return voiceVibesId;
    }

    public void setVoiceVibesId(String voiceVibesId)
    {
        this.voiceVibesId = voiceVibesId;
    }

    public Date getVoiceVibesRequestDate()
    {
        return voiceVibesRequestDate;
    }

    public void setVoiceVibesRequestDate(Date voiceVibesRequestDate)
    {
        this.voiceVibesRequestDate = voiceVibesRequestDate;
    }

    public String getGoogleStorageName()
    {
        return googleStorageName;
    }

    public void setGoogleStorageName(String googleStorageName)
    {
        this.googleStorageName = googleStorageName;
    }

    public int getVoiceVibesAccountTypeId()
    {
        return voiceVibesAccountTypeId;
    }

    public void setVoiceVibesAccountTypeId(int voiceVibesAccountTypeId)
    {
        this.voiceVibesAccountTypeId = voiceVibesAccountTypeId;
    }

    public String getSpeechTextThirdPartyId()
    {
        return speechTextThirdPartyId;
    }

    public void setSpeechTextThirdPartyId(String speechTextThirdPartyId)
    {
        this.speechTextThirdPartyId = speechTextThirdPartyId;
    }

    public Date getSpeechTextRequestDate()
    {
        return speechTextRequestDate;
    }

    public void setSpeechTextRequestDate(Date speechTextRequestDate)
    {
        this.speechTextRequestDate = speechTextRequestDate;
    }

    public float getVoiceVibesOverallScore()
    {
        return voiceVibesOverallScore;
    }

    public void setVoiceVibesOverallScore(float voiceVibesOverallScore)
    {
        this.voiceVibesOverallScore = voiceVibesOverallScore;
    }

    public float getVoiceVibesOverallScoreHra()
    {
        return voiceVibesOverallScoreHra;
    }

    public void setVoiceVibesOverallScoreHra(float voiceVibesOverallScoreHra)
    {
        this.voiceVibesOverallScoreHra = voiceVibesOverallScoreHra;
    }

    public int getVoiceVibesPostErrorCount()
    {
        return voiceVibesPostErrorCount;
    }

    public void setVoiceVibesPostErrorCount(int voiceVibesPostErrorCount)
    {
        this.voiceVibesPostErrorCount = voiceVibesPostErrorCount;
    }

    public int getSaveLocalAudio()
    {
        return saveLocalAudio;
    }

    public void setSaveLocalAudio(int saveLocalAudio)
    {
        this.saveLocalAudio = saveLocalAudio;
    }

    public float getAssignedPoints()
    {
        return assignedPoints;
    }

    public void setAssignedPoints(float assignedPoints)
    {
        this.assignedPoints = assignedPoints;
    }

    public int getItemSubSeq()
    {
        return itemSubSeq;
    }

    public void setItemSubSeq(int itemSubSeq)
    {
        this.itemSubSeq = itemSubSeq;
    }

    public long getUploadedUserFileId()
    {
        return uploadedUserFileId;
    }

    public void setUploadedUserFileId(long uploadedUserFileId)
    {
        this.uploadedUserFileId = uploadedUserFileId;
    }

    public int getUnscoredEssayId()
    {
        return unscoredEssayId;
    }

    public void setUnscoredEssayId(int unscoredEssayId)
    {
        this.unscoredEssayId = unscoredEssayId;
    }

    public int getEssayScoreStatusTypeId()
    {
        return essayStatusTypeId;
    }

    public void setEssayStatusTypeId(int essayStatusTypeId)
    {
        this.essayStatusTypeId = essayStatusTypeId;
    }

    public float getEssayMachineScore()
    {
        return essayMachineScore;
    }

    public void setEssayMachineScore(float essayMachineScore)
    {
        this.essayMachineScore = essayMachineScore;
    }

    public float getEssayConfidence()
    {
        return essayConfidence;
    }

    public void setEssayConfidence(float essayConfidence)
    {
        this.essayConfidence = essayConfidence;
    }

    public int getEssayPlagiarized()
    {
        return essayPlagiarized;
    }

    public void setEssayPlagiarized(int essayPlagiarized)
    {
        this.essayPlagiarized = essayPlagiarized;
    }

    public String getLangCode()
    {
        return langCode;
    }

    public void setLangCode(String langCode)
    {
        this.langCode = langCode;
    }

    public Locale getMediaLocale()
    {

        if (mediaLocale == null && langCode != null && !langCode.isBlank())
            mediaLocale = I18nUtils.getLocaleFromCompositeStr(langCode);

        return mediaLocale;
    }

    public void setMediaLocale(Locale mediaLocale)
    {
        this.mediaLocale = mediaLocale;
    }

    public int getAiCallTypeId()
    {
        return aiCallTypeId;
    }

    public void setAiCallTypeId(int aiCallTypeId)
    {
        this.aiCallTypeId = aiCallTypeId;
    }

    public long getAiCallHistoryId()
    {
        return aiCallHistoryId;
    }

    public void setAiCallHistoryId(long aiCallHistoryId)
    {
        this.aiCallHistoryId = aiCallHistoryId;
    }

    public float getMetaScore1()
    {
        return metaScore1;
    }

    public void setMetaScore1(float metaScore1)
    {
        this.metaScore1 = metaScore1;
    }

    public int getMetaScoreTypeId1()
    {
        return metaScoreTypeId1;
    }

    public void setMetaScoreTypeId1(int metaScoreTypeId1)
    {
        this.metaScoreTypeId1 = metaScoreTypeId1;
    }

    public float getMetaScore2()
    {
        return metaScore2;
    }

    public void setMetaScore2(float metaScore2)
    {
        this.metaScore2 = metaScore2;
    }

    public int getMetaScoreTypeId2()
    {
        return metaScoreTypeId2;
    }

    public void setMetaScoreTypeId2(int metaScoreTypeId2)
    {
        this.metaScoreTypeId2 = metaScoreTypeId2;
    }

    public float getMetaScore3()
    {
        return metaScore3;
    }

    public void setMetaScore3(float metaScore3)
    {
        this.metaScore3 = metaScore3;
    }

    public int getMetaScoreTypeId3()
    {
        return metaScoreTypeId3;
    }

    public void setMetaScoreTypeId3(int metaScoreTypeId3)
    {
        this.metaScoreTypeId3 = metaScoreTypeId3;
    }

    public float getMetaScore4()
    {
        return metaScore4;
    }

    public void setMetaScore4(float metaScore4)
    {
        this.metaScore4 = metaScore4;
    }

    public int getMetaScoreTypeId4()
    {
        return metaScoreTypeId4;
    }

    public void setMetaScoreTypeId4(int metaScoreTypeId4)
    {
        this.metaScoreTypeId4 = metaScoreTypeId4;
    }

    public float getMetaScore5()
    {
        return metaScore5;
    }

    public void setMetaScore5(float metaScore5)
    {
        this.metaScore5 = metaScore5;
    }

    public int getMetaScoreTypeId5()
    {
        return metaScoreTypeId5;
    }

    public void setMetaScoreTypeId5(int metaScoreTypeId5)
    {
        this.metaScoreTypeId5 = metaScoreTypeId5;
    }

}
