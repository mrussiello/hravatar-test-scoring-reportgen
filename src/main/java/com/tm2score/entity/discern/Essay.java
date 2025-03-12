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
@Table( name = "freeform_data_essay" )
@NamedQueries({
})
public class Essay implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int essayId;

    @Column(name="problem_id")
    private int problemId;

    @Column(name="user_id")
    private int userId;

    @Column(name="organization_id")
    private Integer organizationId;
    
    @Column(name="essay_text")
    private String essayText;

    @Column(name="additional_predictors")
    private String additionalPredictors;
    
    @Column(name="essay_type")
    private String essayType;

    @Column(name="has_been_ml_graded")
    private int hasBeenMlGraded;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="modified")
    private Date lastUpdate;

    @Override
    public String toString() {
        return "Essay{" + "essayId=" + essayId + ", problemId=" + problemId + ", essayType=" + essayType + ", hasBeenMlGraded=" + hasBeenMlGraded + '}';
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public int getEssayId() {
        return essayId;
    }

    public void setEssayId(int essayId) {
        this.essayId = essayId;
    }

    public int getProblemId() {
        return problemId;
    }

    public void setProblemId(int problemId) {
        this.problemId = problemId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getEssayText() {
        return essayText;
    }

    public void setEssayText(String essayText) {
        this.essayText = essayText;
    }

    public String getAdditionalPredictors() {
        return additionalPredictors;
    }

    public void setAdditionalPredictors(String additionalPredictors) {
        this.additionalPredictors = additionalPredictors;
    }

    public String getEssayType() {
        return essayType;
    }

    public void setEssayType(String essayType) {
        this.essayType = essayType;
    }

    public int getHasBeenMlGraded() {
        return hasBeenMlGraded;
    }

    public void setHasBeenMlGraded(int hasBeenMlGraded) {
        this.hasBeenMlGraded = hasBeenMlGraded;
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
