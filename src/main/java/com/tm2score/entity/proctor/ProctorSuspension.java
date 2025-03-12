package com.tm2score.entity.proctor;


import com.tm2score.entity.user.User;
import com.tm2score.util.StringUtils;
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
@Table( name = "proctorsuspension" )
@NamedQueries( {
    @NamedQuery( name = "ProctorSuspension.findByTestKeyId", query = "SELECT o FROM ProctorSuspension AS o WHERE o.testKeyId=:testKeyId ORDER BY o.proctorSuspensionId" )
})
public class ProctorSuspension implements Serializable, Comparable<ProctorSuspension>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "proctorsuspensionid" )
    private int proctorSuspensionId;

    @Column( name = "testkeyid" )
    private long testKeyId = 0;

    /*
     0=proctor
     1=automated system suspension    
    */
    @Column( name = "proctorsuspensiontypeid" )
    private int proctorSuspensionTypeId = 0;

    
    
    /*
     0=active
     1=removed    
    */
    @Column( name = "proctorsuspensionstatustypeid" )
    private int proctorSuspensionStatusTypeId = 0;

    
    @Column( name = "proctoruserid" )
    private long proctorUserId = 0;

    @Column( name = "removeuserid" )
    private long removeUserId = 0;


    @Column( name = "note" )
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="removedate")
    private Date removeDate;
    
    @Transient
    private User proctorUser;
    
    @Transient
    private User removeUser;
    

    @Override
    public String toString() {
        return "ProctorSuspension{" + "proctorSuspensionId=" + proctorSuspensionId + ", testKeyId=" + testKeyId + ", proctorUserId=" + proctorUserId  + ", createDate=" + (createDate==null ? "null" : createDate.toString()) + '}';
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProctorSuspension other = (ProctorSuspension) obj;
        if (this.proctorSuspensionId != other.proctorSuspensionId) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (this.proctorSuspensionId ^ (this.proctorSuspensionId >>> 32));
        return hash;
    }


    public String getNoteXhtml()
    {
        return StringUtils.replaceStandardEntities(note);
    }
    
    
    @Override
    public int compareTo(ProctorSuspension o) {

        if( createDate != null && o.getCreateDate() != null )
            return createDate.compareTo( o.getCreateDate() );

        return Integer.valueOf(proctorSuspensionId).compareTo( o.getProctorSuspensionId() );
    }

    public int getProctorSuspensionId() {
        return proctorSuspensionId;
    }

    public void setProctorSuspensionId(int proctorSuspensionId) {
        this.proctorSuspensionId = proctorSuspensionId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public long getProctorUserId() {
        return proctorUserId;
    }

    public void setProctorUserId(long proctorUserId) {
        this.proctorUserId = proctorUserId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getRemoveDate() {
        return removeDate;
    }

    public void setRemoveDate(Date removeDate) {
        this.removeDate = removeDate;
    }

    public User getProctorUser() {
        return proctorUser;
    }

    public void setProctorUser(User proctorUser) {
        this.proctorUser = proctorUser;
    }

    public long getRemoveUserId() {
        return removeUserId;
    }

    public void setRemoveUserId(long removeUserId) {
        this.removeUserId = removeUserId;
    }

    public User getRemoveUser() {
        return removeUser;
    }

    public void setRemoveUser(User removeUser) {
        this.removeUser = removeUser;
    }

    public int getProctorSuspensionStatusTypeId() {
        return proctorSuspensionStatusTypeId;
    }

    public void setProctorSuspensionStatusTypeId(int proctorSuspensionStatusTypeId) {
        this.proctorSuspensionStatusTypeId = proctorSuspensionStatusTypeId;
    }

    public int getProctorSuspensionTypeId() {
        return proctorSuspensionTypeId;
    }

    public void setProctorSuspensionTypeId(int proctorSuspensionTypeId) {
        this.proctorSuspensionTypeId = proctorSuspensionTypeId;
    }



}
