package com.tm2score.entity.proctor;

import com.tm2score.entity.user.User;
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
@Table( name = "proctorentry" )
@NamedQueries( {
    @NamedQuery( name = "ProctorEntry.findByTestKeyId", query = "SELECT o FROM ProctorEntry AS o WHERE o.testKeyId=:testKeyId" )   
})
public class ProctorEntry implements Serializable, Comparable<ProctorEntry>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "proctorentryid" )
    private long proctorEntryId;

    @Column( name = "testkeyid" )
    private long testKeyId = 0;

    @Column( name = "proctoruserid" )
    private long proctorUserId = 0;

    @Column( name = "note" )
    private String note;

    @Column( name = "idverified" )
    private int idVerified;

    @Column( name = "otheractionscomplete" )
    private int otherActionsComplete;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="entrydate")
    private Date entryDate;
    
    @Transient
    private User proctorUser;
    

    public String toString() {
        return "ProctorEntry{" + "proctorEntryId=" + proctorEntryId + ", testKeyId=" + testKeyId + ", proctorUserId=" + proctorUserId + '}';
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProctorEntry other = (ProctorEntry) obj;
        if (this.proctorEntryId != other.proctorEntryId) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (int) (this.proctorEntryId ^ (this.proctorEntryId >>> 32));
        return hash;
    }



    @Override
    public int compareTo(ProctorEntry o) {

        if( entryDate != null && o.getEntryDate() != null )
            return entryDate.compareTo( o.getEntryDate() );

        return new Long( proctorEntryId ).compareTo( o.getProctorEntryId() );
    }


    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public int getIdVerified() {
        return idVerified;
    }

    public void setIdVerified(int idVerified) {
        this.idVerified = idVerified;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getOtherActionsComplete() {
        return otherActionsComplete;
    }

    public void setOtherActionsComplete(int otherActionsComplete) {
        this.otherActionsComplete = otherActionsComplete;
    }

    public long getProctorEntryId() {
        return proctorEntryId;
    }

    public void setProctorEntryId(long proctorEntryId) {
        this.proctorEntryId = proctorEntryId;
    }

    public long getProctorUserId() {
        return proctorUserId;
    }

    public void setProctorUserId(long proctorUserId) {
        this.proctorUserId = proctorUserId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public User getProctorUser() {
        return proctorUser;
    }

    public void setProctorUser(User proctorUser) {
        this.proctorUser = proctorUser;
    }



}
