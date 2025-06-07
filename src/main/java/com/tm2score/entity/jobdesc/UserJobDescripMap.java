package com.tm2score.entity.jobdesc;

import java.io.Serializable;
import jakarta.persistence.Cacheable;

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
import java.util.Date;


@Cacheable
@Entity
@Table( name = "userjobdescripmap" )
@NamedQueries({
        @NamedQuery( name = "UserJobDescripMap.findForJobDescripId", query = "SELECT o FROM UserJobDescripMap AS o WHERE o.jobDescripId=:jobDescripId" ),
        @NamedQuery( name = "UserJobDescripMap.findForUserId", query = "SELECT o FROM UserJobDescripMap AS o WHERE o.userId=:userId" )
})
public class UserJobDescripMap implements Serializable, Comparable<UserJobDescripMap>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "userjobdescripmapid" )
    private long userJobDescripMapId;

    @Column( name = "jobdescripid" )
    private int jobDescripId;

    @Column( name = "userid" )
    private long userId;

    @Column( name = "aicallhistoryid" )
    private long aiCallHistoryId;

    @Column( name = "score" )
    private float score;

    @Temporal(TemporalType.TIMESTAMP)
    @Column( name = "createdate" )
    private Date createDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column( name = "scoredate" )
    private Date scoreDate;

    @Override
    public int compareTo(UserJobDescripMap o) {
        
        if( o.jobDescripId!=jobDescripId)
            return Integer.valueOf(jobDescripId).compareTo(o.jobDescripId );
        
        if( scoreDate!=null && o.scoreDate!=null )
            return Float.valueOf(score).compareTo(o.score);
        
        return Long.valueOf(userJobDescripMapId).compareTo(o.userJobDescripMapId);
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserJobDescripMap other = (UserJobDescripMap) obj;
        if (this.userJobDescripMapId != other.userJobDescripMapId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (int) (this.userJobDescripMapId ^ (this.userJobDescripMapId >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "UserJobDescripMap{" + "userJobDescripMapId=" + userJobDescripMapId + ", jobDescripId=" + jobDescripId + ", userId=" + userId + ", score=" + score + '}';
    }

    public long getUserJobDescripMapId() {
        return userJobDescripMapId;
    }

    public void setUserJobDescripMapId(long userJobDescripMapId) {
        this.userJobDescripMapId = userJobDescripMapId;
    }

    public int getJobDescripId() {
        return jobDescripId;
    }

    public void setJobDescripId(int jobDescripId) {
        this.jobDescripId = jobDescripId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAiCallHistoryId() {
        return aiCallHistoryId;
    }

    public void setAiCallHistoryId(long aiCallHistoryId) {
        this.aiCallHistoryId = aiCallHistoryId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getScoreDate() {
        return scoreDate;
    }

    public void setScoreDate(Date scoreDate) {
        this.scoreDate = scoreDate;
    }


}
