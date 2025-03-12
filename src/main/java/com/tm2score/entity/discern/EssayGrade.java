package com.tm2score.entity.discern;


import java.io.Serializable;
import java.util.Date;
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




@Entity
@Table( name = "freeform_data_essaygrade" )
@NamedQueries({
    @NamedQuery( name = "EssayGrade.findByEssayIdAndGraderType", query = "SELECT o FROM EssayGrade AS o WHERE o.essayId=:discernEssayId AND o.graderType=:graderType AND o.success=1 ORDER BY o.lastUpdate" ),
    @NamedQuery( name = "EssayGrade.findByIdAndGraderType", query = "SELECT o FROM EssayGrade AS o WHERE o.essayGradeId=:discernEssayGradeId AND o.graderType=:graderType AND o.success=1 ORDER BY o.lastUpdate" )
})
public class EssayGrade implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int essayGradeId;

    @Column(name="essay_id")
    private int essayId;
    
    @Column(name="target_scores")
    private String targetScores;

    @Column(name="grader_type")
    private String graderType;
    
    @Column(name="feedback")
    private String feedback;
    
    @Column(name="annotated_text")
    private String annotatedText;
    
    @Column(name="premium_feedback_scores")
    private String premiumFeedbackScores;
    
    @Column(name="success")
    private int success;

    @Column(name="user_id")
    private int userId;

    @Column(name="confidence")
    private float confidence;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified")
    private Date lastUpdate;

    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public int getEssayGradeId() {
        return essayGradeId;
    }

    public void setEssayGradeId(int essayGradeId) {
        this.essayGradeId = essayGradeId;
    }

    public int getEssayId() {
        return essayId;
    }

    public void setEssayId(int essayId) {
        this.essayId = essayId;
    }

    public String getTargetScores() {
        return targetScores;
    }

    public void setTargetScores(String targetScores) {
        this.targetScores = targetScores;
    }

    public String getGraderType() {
        return graderType;
    }

    public void setGraderType(String graderType) {
        this.graderType = graderType;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getAnnotatedText() {
        return annotatedText;
    }

    public void setAnnotatedText(String annotatedText) {
        this.annotatedText = annotatedText;
    }

    public String getPremiumFeedbackScores() {
        return premiumFeedbackScores;
    }

    public void setPremiumFeedbackScores(String premiumFeedbackScores) {
        this.premiumFeedbackScores = premiumFeedbackScores;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
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


}
