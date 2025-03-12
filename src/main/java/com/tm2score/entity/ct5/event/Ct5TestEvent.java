package com.tm2score.entity.ct5.event;


import com.tm2score.event.TestEventStatusType;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
@Table( name = "ct5testevent" )
@NamedQueries({
        @NamedQuery( name = "Ct5TestEvent.findByTestEventIdAndSurveyEventId", query = "SELECT o FROM Ct5TestEvent AS o WHERE o.testEventId=:testEventId AND o.surveyEventId=:surveyEventId" )      
})
public class Ct5TestEvent implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ct5testeventid")
    private long ct5TestEventId;

    @Column(name="testeventid")
    private long testEventId;

    @Column(name="surveyeventid")
    private long surveyEventId;
            
    @Column(name="testkeyid")
    private long testKeyId;
        
    @Column(name="ct5testeventstatustypeid")
    private int ct5TestEventStatusTypeId;

    @Column(name="orgid")
    private int orgId;
        
    @Column(name="suborgid")
    private int suborgId;
        
    @Column(name="userId")
    private long userId;
        
    @Column(name="productId")
    private int productId;
        
    @Column(name="simid")
    private long simId;
        
    @Column(name="simversionid")
    private int simVersionId;
        
    @Column(name="ct5testid")
    private int ct5TestId;
        
    @Column(name="percentcomplete")
    private float percentComplete;
        
    @Column(name="testkeytoken")
    private int testKeyToken;
        
    @Column(name="totaltesttime")
    private float totalTestTime;
    
    @Column(name="timeout")
    private int timeout;
    
    
    
    // This is the date of the last time the totalTestTime was updated.
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lasttimeupdate")
    private Date lastTimeUpdate;
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Transient
    private List<Ct5ItemResponse> ct5ItemResponseList;

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Ct5TestEvent{" + "ct5TestEventId=" + ct5TestEventId + ", testEventId=" + testEventId + ", percentComplete=" + percentComplete + ", testKeyToken=" + testKeyToken + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (this.ct5TestEventId ^ (this.ct5TestEventId >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Ct5TestEvent other = (Ct5TestEvent) obj;
        return this.ct5TestEventId == other.ct5TestEventId;
    }
    
    public TestEventStatusType getTestEventStatusType()
    {
        return TestEventStatusType.getValue(this.ct5TestEventStatusTypeId);
    }
    
    public float getTotalTestTimeSecs()
    {
        if( this.ct5ItemResponseList==null )
            return 0;
        float ttime = 0;
        for( Ct5ItemResponse ir : this.ct5ItemResponseList )
        {
            ttime += ir.getTimeMillisecs();
        }
        return ttime/1000;
    }
    
    

    public long getCt5TestEventId() {
        return ct5TestEventId;
    }

    public void setCt5TestEventId(long ct5TestEventId) {
        this.ct5TestEventId = ct5TestEventId;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public int getCt5TestEventStatusTypeId() {
        return ct5TestEventStatusTypeId;
    }

    public void setCt5TestEventStatusTypeId(int ct5TestEventStatusTypeId) {
        this.ct5TestEventStatusTypeId = ct5TestEventStatusTypeId;
    }

    public int getTestKeyToken() {
        return testKeyToken;
    }

    public void setTestKeyToken(int testKeyToken) {
        this.testKeyToken = testKeyToken;
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

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public float getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    public long getSimId() {
        return simId;
    }

    public void setSimId(long simId) {
        this.simId = simId;
    }

    public int getSimVersionId() {
        return simVersionId;
    }

    public void setSimVersionId(int simVersionId) {
        this.simVersionId = simVersionId;
    }

    public int getCt5TestId() {
        return ct5TestId;
    }

    public void setCt5TestId(int ct5TestId) {
        this.ct5TestId = ct5TestId;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
    }

    public List<Ct5ItemResponse> getCt5ItemResponseList() {
        return ct5ItemResponseList;
    }

    public void setCt5ItemResponseList(List<Ct5ItemResponse> ct5ItemResponseList) {
        this.ct5ItemResponseList = ct5ItemResponseList;
    }

    public Date getLastTimeUpdate() {
        return lastTimeUpdate;
    }

    public void setLastTimeUpdate(Date lastTimeUpdate) {
        this.lastTimeUpdate = lastTimeUpdate;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public float getTotalTestTime() {
        return totalTestTime;
    }

    public void setTotalTestTime(float totalTestTime) {
        this.totalTestTime = totalTestTime;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getSurveyEventId() {
        return surveyEventId;
    }

    public void setSurveyEventId(long surveyEventId) {
        this.surveyEventId = surveyEventId;
    }



}
