package com.tm2score.entity.user;

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


@Entity
@Table( name = "orgautotest" )
@XmlRootElement
@NamedQueries({
@NamedQuery ( name="OrgAutoTest.getByOrgAutoTestId", query="SELECT o FROM OrgAutoTest AS o WHERE o.orgAutoTestId=:orgAutoTestId" )
})
public class OrgAutoTest implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="orgautotestid")
    private int orgAutoTestId;

    @Column(name="name")
    private String name;

    @Column(name="orgid")
    private int orgId;

    @Column(name="suborgid")
    private int suborgId;

    @Column( name = "productid" )
    private int productId;

    @Column(name="orgautoteststatustypeid")
    private int orgAutoTestStatusTypeId = 1;
    
    @Column(name="authuserid")
    private long authUserId = 0;

    @Column( name = "maxevents" )
    private int maxEvents;

    @Column( name = "eventcount" )
    private int eventCount;

    @Column(name="lang")
    private String lang;

    @Column(name="sendexpirenoticetypeid")
    private int sendExpireNoticeTypeId = 1;

    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="expiredate")
    private Date expireDate;


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.orgAutoTestId;
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
        final OrgAutoTest other = (OrgAutoTest) obj;
        if (this.orgAutoTestId != other.orgAutoTestId) {
            return false;
        }
        return true;
    }
    
    
    
    @Override
    public String toString() {
        return "OrgAutoTest{" + "orgAutoTestId=" + orgAutoTestId + ", name=" + name + ", orgId=" + orgId + ", suborgId=" + suborgId + ", productId=" + productId + ", orgAutoTestStatusTypeId=" + orgAutoTestStatusTypeId + ", maxEvents=" + maxEvents + ", eventCount=" + eventCount + '}';
    }


    public String getOatPin()
    {
        return "OAT" + Integer.toHexString( orgAutoTestId ) + "UX";
    }

    public int getOrgAutoTestId() {
        return orgAutoTestId;
    }

    public void setOrgAutoTestId(int orgAutoTestId) {
        this.orgAutoTestId = orgAutoTestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getOrgAutoTestStatusTypeId() {
        return orgAutoTestStatusTypeId;
    }

    public void setOrgAutoTestStatusTypeId(int orgAutoTestStatusTypeId) {
        this.orgAutoTestStatusTypeId = orgAutoTestStatusTypeId;
    }

    public long getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(long authUserId) {
        this.authUserId = authUserId;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getSendExpireNoticeTypeId() {
        return sendExpireNoticeTypeId;
    }

    public void setSendExpireNoticeTypeId(int sendExpireNoticeTypeId) {
        this.sendExpireNoticeTypeId = sendExpireNoticeTypeId;
    }

    public String getEmailResultsTo() {
        return emailResultsTo;
    }

    public void setEmailResultsTo(String emailResultsTo) {
        this.emailResultsTo = emailResultsTo;
    }

    public String getTextResultsTo() {
        return textResultsTo;
    }

    public void setTextResultsTo(String textResultsTo) {
        this.textResultsTo = textResultsTo;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }
    


}
