/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.proctor;


import com.tm2score.entity.user.User;
import com.tm2score.proctor.SuspiciousActivityType;
import com.tm2score.proctor.SuspiciousKeyCodeType;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
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

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "suspiciousactivity" )
@NamedQueries( {
    @NamedQuery( name = "SuspiciousActivity.findByTestEventId", query = "SELECT o FROM SuspiciousActivity AS o WHERE o.suspiciousActivitySourceTypeId=0 AND o.testEventId=:testEventId" ),
    @NamedQuery( name = "SuspiciousActivity.findByTestEventIdOrTestKeyId", query = "SELECT o FROM SuspiciousActivity AS o WHERE o.suspiciousActivitySourceTypeId=0 AND ( o.testEventId=:testEventId OR (o.testKeyId=:testKeyId AND o.testEventId=0) ) ORDER BY o.testKeyId" ),   
    @NamedQuery( name = "SuspiciousActivity.findByTestKeyIdAndNoTestEventId", query = "SELECT o FROM SuspiciousActivity AS o WHERE o.suspiciousActivitySourceTypeId=0 AND o.testKeyId=:testKeyId AND o.testEventId=:testEventId ORDER BY o.testKeyId" )    
} )
public class SuspiciousActivity implements Serializable, Comparable<SuspiciousActivity>
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "suspiciousactivityid" )
    private long suspiciousActivityId;
    
    // 0=Proctoring
    // 1=LVI
    @Column( name = "suspiciousactivitysourcetypeid" )
    private int suspiciousActivitySourceTypeId;
    
    @Column( name = "lvcallid" )
    private long lvCallId;

    
    @Column( name = "suspiciousactivitytypeid" )
    private int suspiciousActivityTypeId;
    
    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "milliseconds" )
    private long milliseconds;

    // This is used to match comments to users for specific video files.
    @Column( name = "uploadeduserfileid" )
    private long uploadedUserFileId;

    @Column( name = "floatparam1" )
    private float floatParam1;

    /*
    This is typically the total number of instances detected.
    */
    @Column( name = "intparam1" )
    private int intParam1;
    
    /*
    This is typically the number of instances detected during post-test processing.
    */
    @Column( name = "intparam2" )
    private int intParam2;

    
    @Column( name = "keycode" )
    private int keyCode;

    @Column( name = "userid" )
    private long userId;

    
    
    @Column( name = "note" )
    private String note;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
    
    
    @Transient
    private Locale locale;
    
    @Transient
    private User user;
    
    
    @Override
    public int compareTo(SuspiciousActivity o) 
    {
        if( testKeyId!=o.getTestKeyId() )
            return ((Long)testKeyId).compareTo(o.getTestKeyId() );

        if( createDate!=null && o.getCreateDate()!=null )
            return createDate.compareTo( o.getCreateDate() );
        
        return ((Long)suspiciousActivityId).compareTo(o.getSuspiciousActivityId() );
    }

    @Override
    public String toString() {
        return "SuspiciousActivity{" + "suspiciousActivityId=" + suspiciousActivityId + ", suspiciousActivityTypeId=" + suspiciousActivityTypeId + ", testKeyId=" + testKeyId + ", testEventId=" + testEventId + ", milliseconds=" + milliseconds + '}';
    }

    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (this.suspiciousActivityId ^ (this.suspiciousActivityId >>> 32));
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
        final SuspiciousActivity other = (SuspiciousActivity) obj;
        if (this.suspiciousActivityId != other.suspiciousActivityId) {
            return false;
        }
        return true;
    }

    public SuspiciousKeyCodeType getSuspiciousKeyCodeType()
    {
        return SuspiciousKeyCodeType.getValue(keyCode);
    }

    public String getSuspiciousActivityTypeName()
    {
        return getSuspiciousActivityType().getName(locale);
    }
    
    public SuspiciousActivityType getSuspiciousActivityType()
    {
        return SuspiciousActivityType.getValue(this.suspiciousActivityTypeId);
    }
    
    public int getFloatParam1AsInt()
    {
        return Math.round(floatParam1);
    }
    
    public long getSuspiciousActivityId() {
        return suspiciousActivityId;
    }

    public void setSuspiciousActivityId(long suspiciousActivityId) {
        this.suspiciousActivityId = suspiciousActivityId;
    }

    public int getSuspiciousActivityTypeId() {
        return suspiciousActivityTypeId;
    }

    public void setSuspiciousActivityTypeId(int suspiciousActivityTypeId) {
        this.suspiciousActivityTypeId = suspiciousActivityTypeId;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }
    
    public int getSeconds()
    {
        return (int) (milliseconds/1000);
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
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

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getUploadedUserFileId() {
        return uploadedUserFileId;
    }

    public void setUploadedUserFileId(long uploadedUserFileId) {
        this.uploadedUserFileId = uploadedUserFileId;
    }

    public float getFloatParam1() {
        return floatParam1;
    }

    public void setFloatParam1(float floatParam1) {
        this.floatParam1 = floatParam1;
    }

    public int getSuspiciousActivitySourceTypeId() {
        return suspiciousActivitySourceTypeId;
    }

    public void setSuspiciousActivitySourceTypeId(int suspiciousActivitySourceTypeId) {
        this.suspiciousActivitySourceTypeId = suspiciousActivitySourceTypeId;
    }

    public long getLvCallId() {
        return lvCallId;
    }

    public void setLvCallId(long lvCallId) {
        this.lvCallId = lvCallId;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public Date getLastUpdate() {
        
        if(lastUpdate==null)
            return createDate;
        
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }

    
}
