/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.essay;

import com.tm2score.essay.EssayScoreStatusType;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Basic;
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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "unscoredessay" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="UnscoredEssay.findByEssayPromptIdAndPlagCheck", query="SELECT o FROM UnscoredEssay AS o WHERE o.essayPromptId=:essayPromptId AND o.includeInPlagCheck=1 ORDER BY o.unscoredEssayId DESC" ),
    @NamedQuery ( name="UnscoredEssay.findByUnscoredEssayId", query="SELECT o FROM UnscoredEssay AS o WHERE o.unscoredEssayId=:unscoredEssayId" ),
    @NamedQuery ( name="UnscoredEssay.findByTestEventId", query="SELECT o FROM UnscoredEssay AS o WHERE o.testEventId=:testEventId" ),
    @NamedQuery ( name="UnscoredEssay.findByTestEventIdAndMinStatusTypeId", query="SELECT o FROM UnscoredEssay AS o WHERE o.testEventId=:testEventId AND o.scoreStatusTypeId>=:minScoreStatusTypeId" ),
    @NamedQuery ( name="UnscoredEssay.findByTestEventIdAndItemAndMinStatusTypeId", query="SELECT o FROM UnscoredEssay AS o WHERE o.testEventId=:testEventId AND o.nodeSequenceId=:nodeSequenceId AND o.subnodeSequenceId=:subnodeSequenceId AND o.scoreStatusTypeId>=:minScoreStatusTypeId" ),
    @NamedQuery ( name="UnscoredEssay.findByNoStats", query="SELECT o FROM UnscoredEssay AS o WHERE o.totalWords=-1" ),
    @NamedQuery ( name="UnscoredEssay.findOthersByPromptId", query="SELECT o FROM UnscoredEssay AS o WHERE o.testEventId<>:testEventId AND o.essayPromptId=:essayPromptId" )
})
public class UnscoredEssay implements Serializable, Comparable<UnscoredEssay>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="unscoredessayid")
    private int unscoredEssayId;

    /*
     0 = Essay
     1 = AV Transcript
     
    */
    @Column(name="unscoredessaytypeid")
    private int unscoredEssayTypeId;
    
    @Column(name="testeventid")
    private long testEventId;

    @Column(name="avitemresponseid")
    private long avItemResponseId;

    @Column(name="scorestatustypeid")
    private int scoreStatusTypeId;
    
    @Column(name="localestr")
    private String localeStr;
    
    @Column(name="nodesequenceid")
    private int nodeSequenceId;

    @Column(name="subnodesequenceid")
    private int subnodeSequenceId;

    @Column(name="essaypromptid")
    private int essayPromptId;

    @Column(name="ct5itemid")
    private int ct5ItemId;

    @Column(name="ct5itempartid")
    private int ct5ItemPartId;

    @Column(name="userid")
    private long userId;

    @Column(name="discernessayid")
    private int discernEssayId;

    @Column(name="discernessaygradeid")
    private int discernEssayGradeId;

    @Column(name="computedscore")
    private float computedScore;

    @Column(name="computedconfidence")
    private float computedConfidence;
    
    @Column(name="scorestatustypeid2")
    private int scoreStatusTypeId2;

    @Column(name="computedscore2")
    private float computedScore2;

    @Column(name="computedconfidence2")
    private float computedConfidence2;
    
    
    @Column(name="totalwords")
    private int totalWords = -1;

    @Column(name="secondstocompose")
    private int secondsToCompose;

    @Column(name="wpm")
    private float wpm;
    
    @Column(name="highwpm")
    private float highWpm;
    
    @Column(name="hasspellinggrammarstyle")
    private int hasSpellingGrammarStyle;

    @Column(name="spellingerrors")
    private int spellingErrors;

    @Column(name="grammarerrors")
    private int grammarErrors;

    @Column(name="styleerrors")
    private int styleErrors;

    @Column(name="similarunscoredessayid")
    private int similarUnscoredEssayId;

    @Column(name="duplicatecontentweburl")
    private String duplicateContentWebUrl;

    @Column(name="duplicatecontentwebpct")
    private float duplicateContentWebPct;

    @Column(name="pctduplicatewords")
    private float pctDuplicateWords;

    @Column(name="pctduplicatelongwords")
    private float pctDuplicateLongWords;

    @Column(name="translatecomparescore")
    private float translateCompareScore=-1;

    @Column(name="includeinplagcheck")
    private int includeInPlagCheck;

    @Column(name="metascore1")
    private float metaScore1;

    @Column(name="metascoretypeid1")
    private int metaScoreTypeId1;
    
    @Column(name="metascore2")
    private float metaScore2;

    @Column(name="metascoretypeid2")
    private int metaScoreTypeId2;
    
    @Column(name="metascore3")
    private float metaScore3;

    @Column(name="metascoretypeid3")
    private int metaScoreTypeId3;
    
    @Column(name="metascore4")
    private float metaScore4;

    @Column(name="metascoretypeid4")
    private int metaScoreTypeId4;
    
    @Column(name="metascore5")
    private float metaScore5;

    @Column(name="metascoretypeid5")
    private int metaScoreTypeId5;
    

    @Column(name="essay")
    private String essay;


    @Column(name="translatedessay")
    private String translatedEssay;
    
    
    @Column(name="usernote")
    private String userNote;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="scoredate")
    private Date scoreDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="scoredate2")
    private Date scoreDate2;
    

    @Override
    public int compareTo(UnscoredEssay o) {
        return createDate.compareTo( o.getCreateDate() );
    }

    @Override
    public String toString() {
        return "UnscoredEssay{" + "unscoredEssayId=" + unscoredEssayId + ", scoreStatusTypeId=" + scoreStatusTypeId + ", nodeSequenceId=" + nodeSequenceId + ", subnodeSequenceId=" + subnodeSequenceId + ", essayPromptId=" + essayPromptId + ", discernEssayId=" + discernEssayId + ", discernEssayGradeId=" + discernEssayGradeId + ", computedScore=" + computedScore + ", computedConfidence=" + computedConfidence + ", secondsToCompose=" + this.secondsToCompose + ", includeInPlagCheck=" + includeInPlagCheck + ", essay=" + essay + '}';
    }

    
    /**
     * Returns a 0-100 score based on error rates.
     * @return 
     */
    public float getScoreFmErrorRates()   
    {
        if( totalWords > 0 && hasSpellingGrammarStyle==1)
        {
            float totalErrsPerWd = ((float) grammarErrors + styleErrors + spellingErrors)/((float) totalWords );

            // WAY too many errors per word
            if(  totalWords<=30 || totalErrsPerWd>=0.2f)
                return 50*(1 - totalErrsPerWd);
            
            if( totalWords<=50 || totalErrsPerWd>=0.1f)
                return 60f*(1 - totalErrsPerWd);

            if( totalWords<=70 || totalErrsPerWd>=0.05f)
                return 70f*(1 - totalErrsPerWd);
                                    
            return 100f*(1 - totalErrsPerWd);
        }
        
        return 0;
        
    }

    public EssayScoreStatusType getEssayScoreStatusType()
    {
        return EssayScoreStatusType.getValue( scoreStatusTypeId );
    }

    public EssayScoreStatusType getEssayScoreStatusType2()
    {
        return EssayScoreStatusType.getValue( scoreStatusTypeId2 );
    }
    
    public Map<Integer,Float> getMetaScoreMap()
    {
        Map<Integer,Float> out = new HashMap<>();
        
        if( this.metaScoreTypeId1>0 )
            out.put( this.metaScoreTypeId1, metaScore1);
        if( this.metaScoreTypeId2>0 )
            out.put( this.metaScoreTypeId2, metaScore2);
        if( this.metaScoreTypeId3>0 )
            out.put( this.metaScoreTypeId3, metaScore3);
        if( this.metaScoreTypeId4>0 )
            out.put( this.metaScoreTypeId4, metaScore4);
        if( this.metaScoreTypeId5>0 )
            out.put( this.metaScoreTypeId5, metaScore5);
        
        return out;
    }
    
    
    
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getUnscoredEssayId() {
        return unscoredEssayId;
    }

    public void setUnscoredEssayId(int unscoredEssayId) {
        this.unscoredEssayId = unscoredEssayId;
    }

    public int getEssayPromptId() {
        return essayPromptId;
    }

    public void setEssayPromptId(int essayPromptId) {
        this.essayPromptId = essayPromptId;
    }

    public int getDiscernEssayId() {
        return discernEssayId;
    }

    public void setDiscernEssayId(int discernEssayId) {
        this.discernEssayId = discernEssayId;
    }

    public int getDiscernEssayGradeId() {
        return discernEssayGradeId;
    }

    public void setDiscernEssayGradeId(int discernEssayGradeId) {
        this.discernEssayGradeId = discernEssayGradeId;
    }

    public float getComputedScore() {
        return computedScore;
    }

    public void setComputedScore(float computedScore) {
        this.computedScore = computedScore;
    }

    public String getEssay() {
        return essay;
    }

    public void setEssay(String essay) {
        this.essay = essay;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getScoreStatusTypeId() {
        return scoreStatusTypeId;
    }

    public void setScoreStatusTypeId(int scoreStatusTypeId) {
        this.scoreStatusTypeId = scoreStatusTypeId;
    }

    public int getSubnodeSequenceId() {
        return subnodeSequenceId;
    }

    public void setSubnodeSequenceId(int subnodeSequenceId) {
        this.subnodeSequenceId = subnodeSequenceId;
    }

    public float getComputedConfidence() {
        return computedConfidence;
    }

    public void setComputedConfidence(float computedConfidence) {
        this.computedConfidence = computedConfidence;
    }

    public Date getScoreDate() {
        return scoreDate;
    }

    public void setScoreDate(Date scoreDate) {
        this.scoreDate = scoreDate;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public int getNodeSequenceId() {
        return nodeSequenceId;
    }

    public void setNodeSequenceId(int nodeSequenceId) {
        this.nodeSequenceId = nodeSequenceId;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public int getSpellingErrors() {
        return spellingErrors;
    }

    public void setSpellingErrors(int spellingErrors) {
        this.spellingErrors = spellingErrors;
    }

    public int getGrammarErrors() {
        return grammarErrors;
    }

    public void setGrammarErrors(int grammarErrors) {
        this.grammarErrors = grammarErrors;
    }

    public int getStyleErrors() {
        return styleErrors;
    }

    public void setStyleErrors(int styleErrors) {
        this.styleErrors = styleErrors;
    }

    public int getSimilarUnscoredEssayId() {
        return similarUnscoredEssayId;
    }

    public void setSimilarUnscoredEssayId(int similarUnscoredEssayId) {
        this.similarUnscoredEssayId = similarUnscoredEssayId;
    }

    public String getDuplicateContentWebUrl() {
        return duplicateContentWebUrl;
    }

    public void setDuplicateContentWebUrl(String duplicateContentWebUrl) {
        this.duplicateContentWebUrl = duplicateContentWebUrl;
    }

    public float getDuplicateContentWebPct() {
        return duplicateContentWebPct;
    }

    public void setDuplicateContentWebPct(float duplicateContentWebPct) {
        this.duplicateContentWebPct = duplicateContentWebPct;
    }

    public int getSecondsToCompose() {
        return secondsToCompose;
    }

    public void setSecondsToCompose(int secondsToCompose) {
        this.secondsToCompose = secondsToCompose;
    }

    public float getPctDuplicateWords() {
        return pctDuplicateWords;
    }

    public void setPctDuplicateWords(float pctDuplicateWords) {
        this.pctDuplicateWords = pctDuplicateWords;
    }

    public float getPctDuplicateLongWords() {
        return pctDuplicateLongWords;
    }

    public void setPctDuplicateLongWords(float pctDuplicateLongWords) {
        this.pctDuplicateLongWords = pctDuplicateLongWords;
    }

    public int getHasSpellingGrammarStyle() {
        return hasSpellingGrammarStyle;
    }

    public void setHasSpellingGrammarStyle(int hasSpellingGrammarStyle) {
        this.hasSpellingGrammarStyle = hasSpellingGrammarStyle;
    }

    public float getTranslateCompareScore() {
        return translateCompareScore;
    }

    public void setTranslateCompareScore(float translateCompareScore) {
        this.translateCompareScore = translateCompareScore;
    }

    public String getTranslatedEssay() {
        return translatedEssay;
    }

    public void setTranslatedEssay(String translatedEssay) {
        this.translatedEssay = translatedEssay;
    }

    public int getIncludeInPlagCheck() {
        return includeInPlagCheck;
    }

    public void setIncludeInPlagCheck(int includeInPlagCheck) {
        this.includeInPlagCheck = includeInPlagCheck;
    }

    public float getHighWpm() {
        return highWpm;
    }

    public void setHighWpm(float highWpm) {
        this.highWpm = highWpm;
    }

    public float getWpm() {
        return wpm;
    }

    public void setWpm(float wpm) {
        this.wpm = wpm;
    }

    public int getCt5ItemId() {
        return ct5ItemId;
    }

    public void setCt5ItemId(int ct5ItemId) {
        this.ct5ItemId = ct5ItemId;
    }

    public int getCt5ItemPartId() {
        return ct5ItemPartId;
    }

    public void setCt5ItemPartId(int ct5ItemPartId) {
        this.ct5ItemPartId = ct5ItemPartId;
    }

    public float getComputedScore2()
    {
        return computedScore2;
    }

    public void setComputedScore2(float computedScore2)
    {
        this.computedScore2 = computedScore2;
    }

    public float getComputedConfidence2()
    {
        return computedConfidence2;
    }

    public void setComputedConfidence2(float computedConfidence2)
    {
        this.computedConfidence2 = computedConfidence2;
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

    public Date getScoreDate2()
    {
        return scoreDate2;
    }

    public void setScoreDate2(Date scoreDate2)
    {
        this.scoreDate2 = scoreDate2;
    }

    public String getLocaleStr()
    {
        return localeStr;
    }

    public void setLocaleStr(String localeStr)
    {
        this.localeStr = localeStr;
    }

    public int getScoreStatusTypeId2()
    {
        return scoreStatusTypeId2;
    }

    public void setScoreStatusTypeId2(int scoreStatusTypeId2)
    {
        this.scoreStatusTypeId2 = scoreStatusTypeId2;
    }

    public int getUnscoredEssayTypeId()
    {
        return unscoredEssayTypeId;
    }

    public void setUnscoredEssayTypeId(int unscoredEssayTypeId)
    {
        this.unscoredEssayTypeId = unscoredEssayTypeId;
    }

    public long getAvItemResponseId()
    {
        return avItemResponseId;
    }

    public void setAvItemResponseId(long avItemResponseId)
    {
        this.avItemResponseId = avItemResponseId;
    }



}
