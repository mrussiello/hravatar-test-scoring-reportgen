package com.tm2score.entity.user;

import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import java.io.Serializable;
import java.util.Date;
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


@Cacheable(false)
@Entity
@Table( name = "limitedaccesslink" )
@NamedQueries( {
        @NamedQuery( name = "LimitedAccessLink.findByEmailAndTestKeyId", query = "SELECT o FROM LimitedAccessLink AS o WHERE o.testKeyId=:testKeyId AND o.email=:email ORDER BY o.expireDate DESC" )
} )
public class LimitedAccessLink implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "limitedaccesslinkid" )
    private long limitedAccessLinkId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "statustypeid" )
    private int statusTypeId = 1;

    @Column( name = "email" )
    private String email;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="expiredate")
    private Date expireDate;



    @Override
    public String toString() {
        return "LimitedAccessLink{" + "limitedAccessLinkId=" + limitedAccessLinkId + ", testKeyId=" + testKeyId + '}';
    }
        
    public boolean getIsExpired()
    {
        return expireDate!=null && expireDate.before(new Date() );
    }

    public String getLimitedAccessLinkIdIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( this.limitedAccessLinkId );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getLimitedAccessLinkIdIdEncrypted() " + toString() );

            return "";
        }
    }

    
    
    public long getLimitedAccessLinkId() {
        return limitedAccessLinkId;
    }

    public void setLimitedAccessLinkId(long limitedAccessLinkId) {
        this.limitedAccessLinkId = limitedAccessLinkId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public int getStatusTypeId() {
        return statusTypeId;
    }

    public void setStatusTypeId(int statusTypeId) {
        this.statusTypeId = statusTypeId;
    }

    public boolean getStatusTypeIdBoolean() {
        return statusTypeId==1;
    }

    public void setStatusTypeIdBoolean(boolean b ) {
        this.statusTypeId = b ? 1 : 0;
    }




}
