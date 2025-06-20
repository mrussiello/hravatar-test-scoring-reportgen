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
@Table( name = "userjobmap" )
@NamedQueries({
        @NamedQuery( name = "UserJobMap.findForJobId", query = "SELECT o FROM UserJobMap AS o WHERE o.jobId=:jobId" ),
        @NamedQuery( name = "UserJobMap.findForUserId", query = "SELECT o FROM UserJobMap AS o WHERE o.userId=:userId" )
})
public class UserJobMap implements Serializable, Comparable<UserJobMap>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "userjobmapid" )
    private long userJobMapId;

    @Column( name = "jobid" )
    private int jobId;

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
    public int compareTo(UserJobMap o) {
        
        if( o.jobId!=jobId)
            return Integer.valueOf(jobId).compareTo(o.jobId );
        
        if( scoreDate!=null && o.scoreDate!=null )
            return Float.valueOf(score).compareTo(o.score);
        
        return Long.valueOf(userJobMapId).compareTo(o.userJobMapId);
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserJobMap other = (UserJobMap) obj;
        if (this.userJobMapId != other.userJobMapId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (int) (this.userJobMapId ^ (this.userJobMapId >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "UserJobMap{" + "userJobMapId=" + userJobMapId + ", jobId=" + jobId + ", userId=" + userId + ", score=" + score + '}';
    }

    public long getUserJobMapId() {
        return userJobMapId;
    }

    public void setUserJobMapId(long userJobMapId) {
        this.userJobMapId = userJobMapId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
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
