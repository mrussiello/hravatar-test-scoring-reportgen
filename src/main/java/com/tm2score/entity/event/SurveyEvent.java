package com.tm2score.entity.event;

import com.tm2score.event.TestEventStatusType;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
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
@Table( name = "surveyevent" )
@NamedQueries( {
        @NamedQuery( name = "SurveyEvent.findByTestKeyId", query = "SELECT o FROM SurveyEvent AS o WHERE o.testKeyId=:testKeyId" ),
        @NamedQuery( name = "SurveyEvent.findByStatusId", query = "SELECT o FROM SurveyEvent AS o WHERE o.surveyEventStatusTypeId=:surveyEventStatusTypeId ORDER BY o.surveyEventId" ),
        @NamedQuery( name = "SurveyEvent.findByStatusIdMaxErrors", query = "SELECT o FROM SurveyEvent AS o WHERE o.surveyEventStatusTypeId=:surveyEventStatusTypeId AND o.errorCnt<=:maxErrors ORDER BY o.surveyEventId" )

        
} )
public class SurveyEvent implements Serializable, Comparable<SurveyEvent>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "surveyeventid" )
    private long surveyEventId;

    @Column( name = "surveyeventstatustypeid" )
    private int surveyEventStatusTypeId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "simid" )
    private long simId;

    @Column( name = "simversionid" )
    private int simVersionId;
    
    @Column( name = "productid" )
    private int productId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "userid" )
    private long userId = 0;

    @Column( name = "lang" )
    private String localeStr;

    @Column( name = "resultxml" )
    private String resultXml;

    @Column( name = "percentcomplete" )
    private float percentComplete = 0;
    
    @Column( name = "errorcnt" )
    private int errorCnt;
    

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="startdate")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Transient
    private TestEvent testEvent;

    public TestEvent getEquivalentTestEvent()
    {
        if( testEvent != null )
            return testEvent;

        TestEvent te = new TestEvent();
        te.setSurveyEvent(this);
        te.setProductId(productId);
        te.setResultXml(resultXml);
        te.setUserId(userId);
        te.setOrgId(orgId);
        te.setTestKeyId(testKeyId);
        te.setSimId(simId);
        te.setSimVersionId(simVersionId);
        te.setPercentComplete(percentComplete);
        te.setTestEventStatusTypeId(surveyEventStatusTypeId);

        testEvent = te;
        return testEvent;
    }



    public boolean getIsComplete()
    {
        return percentComplete>=100f || getTestEventStatusType().getIsComplete();
    }


    public TestEventStatusType getTestEventStatusType()
    {
        return TestEventStatusType.getValue( surveyEventStatusTypeId );
    }

    @Override
    public int compareTo(SurveyEvent o) {

        if( startDate != null && o.getStartDate() != null )
            return startDate.compareTo( o.getStartDate() );

        return ((Long) surveyEventId ).compareTo( o.getSurveyEventId() );
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (int) (this.surveyEventId ^ (this.surveyEventId >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "SurveyEvent{" + "surveyEventId=" + surveyEventId + ", surveyEventStatusTypeId=" + surveyEventStatusTypeId + ", testKeyId=" + testKeyId + ", productId=" + productId + ", orgId=" + orgId + ", userId=" + userId + ", percentComplete=" + percentComplete + ", startDate=" + startDate + '}';
    }

    public String getSurveyEventIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( surveyEventId );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "getSurveyEventIdEncrypted() " + toString()  );

            return "";
        }
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SurveyEvent other = (SurveyEvent) obj;
        return this.surveyEventId == other.surveyEventId;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public float getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getResultXml() {
        return resultXml;
    }

    public void setResultXml(String resultXml) {
        this.resultXml = resultXml;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public long getSurveyEventId() {
        return surveyEventId;
    }

    public void setSurveyEventId(long surveyEventId) {
        this.surveyEventId = surveyEventId;
    }

    public int getSurveyEventStatusTypeId() {
        return surveyEventStatusTypeId;
    }

    public void setSurveyEventStatusTypeId(int surveyEventStatusTypeId) {
        this.surveyEventStatusTypeId = surveyEventStatusTypeId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public TestEvent getTestEvent() {
        return testEvent;
    }

    public void setTestEvent(TestEvent testEvent) {
        this.testEvent = testEvent;
    }

    public int getErrorCnt() {
        return errorCnt;
    }

    public void setErrorCnt(int errorCnt) {
        this.errorCnt = errorCnt;
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

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }




}
