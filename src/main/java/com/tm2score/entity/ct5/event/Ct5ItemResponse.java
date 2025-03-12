package com.tm2score.entity.ct5.event;


import com.tm2builder.sim.xml.SimJ;
import com.tm2score.ct5.Ct5ItemResponseStatusType;
import com.tm2score.entity.file.UploadedUserFile;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
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
@Table( name = "ct5itemresponse" )
@NamedQueries({
        @NamedQuery( name = "Ct5ItemResponse.findByCt5TestEventId", query = "SELECT o FROM Ct5ItemResponse AS o WHERE o.ct5TestEventId=:ct5TestEventId" )
})
public class Ct5ItemResponse implements Serializable, Cloneable, Comparable<Ct5ItemResponse>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="ct5itemresponseid")
    private long ct5ItemResponseId;

    @Column(name="ct5testeventid")
    private long ct5TestEventId;
    
    @Column(name="ct5itemid")
    private int ct5ItemId;
    
    @Column(name="ct5itempartid")
    private int ct5ItemPartId;
    
    @Column(name="ct5itemresponsestatustypeid")
    private int ct5ItemResponseStatusTypeId;

    @Column(name="responsetext")
    private String responseText;

    /*
     1+ order of response in test event.
    */
    @Column(name="responseseq")
    private int responseSeq;
    
    
    /*
     For Format is a string of the raw responses. Always a pair of itempartid,value (string)
        rating:   question:value
        mult choice:  sel choice:true
        mult corr answer: choice:true,choice:false,choice:false, ...
        matching:  tgt:dragable,tgt:dragable,tgt:draggable
        drag drop: tgt:dragable1;dragable2;dragable3,tgt:dragable4, ...
        fill blank: question:URL-Encoded(text entered)
        essay: blank    
    */
    @Column(name="responsestr1")
    private String responseStr1;

    /*
     For Format is a comma-delimited string of the scores    
    rating: question:value
    mult choice:  sel choice:0 (wrong), 1 (correct)
    mult corr answer: choice1:0 or 1,choice2:0 or 1, ...
    matching:  tgt1:0 or 1,tgt2:0 or 1, ...
    drag drop: tgt1:0 or 1,tgt:0 or 1, ...
    fill blank: question:0 or 1
    essay: blank    
    */
    @Column(name="scorestr1")
    private String scoreStr1;

    
    /**
     * 0=Answered wrong
     * 1=Answered correct
     * -1=Skipped or not answered
     * -2=Timed out
     */
    @Column(name="responsecode")
    private int responseCode;
    
    @Column(name="points")
    private float points;
    
    @Column(name="showcount")
    private int showCount;
    
    @Column(name="responsecount")
    private int responseCount;
    
    @Column(name="accessibleform")
    private int accessibleForm;
        
    /*
      Video Interview Item - number of tries (without errors).
    */
    @Column(name="intparam1")
    private int intParam1;
    
    /*
      Scored Media Item - replay
    */
    @Column(name="intparam2")
    private int intParam2;

    
    //@Column(name="timemillisecs")
    //private int timeMillisecs;
        
    @Column(name="uploadeduserfileid")
    private long uploadedUserFileId;
    
    @Column(name="totalitemtime")
    private float totalItemTime;
    
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
    UploadedUserFile uploadedUserFile;
    
    
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Ct5ItemResponse{" + "ct5ItemResponseId=" + ct5ItemResponseId + ", ct5TestEventId=" + ct5TestEventId + ", ct5ItemId=" + ct5ItemId + ", ct5ItemPartId=" + ct5ItemPartId + '}';
    }

    @Override
    public int compareTo(Ct5ItemResponse o) {
        
        return ((Integer)this.responseSeq).compareTo( o.getResponseSeq());
    }

    public boolean getIsComplete()
    {
        return getCt5ItemResponseStatusType().getIsComplete();
    }
    
    public Ct5ItemResponseStatusType getCt5ItemResponseStatusType()
    {
        return Ct5ItemResponseStatusType.getValue(this.getCt5ItemResponseStatusTypeId() );
    }
    
    private Map<Integer,Float> parseScoreStr()
    {
        Map<Integer,Float> out = new TreeMap<>();
        if( this.scoreStr1==null || this.scoreStr1.isBlank() )
            return out;
        
        String[] nv;        
        for( String pair : scoreStr1.split(",") )
        {
            nv = pair.split(":");
            if(nv.length<2 )
                continue;
            out.put(Integer.valueOf(nv[0]), Float.valueOf(nv[1].trim()));
        }
        return out;
    }
    
    
    private Map<Integer,String> parseResponseStr()
    {
        Map<Integer,String> out = new TreeMap<>();
        if( this.responseStr1==null )
            return out;
        
        String[] nv;        
        for( String pair : responseStr1.split(",") )
        {
            nv = pair.split(":");
            if(nv.length<2 )
                continue;
            out.put(Integer.valueOf(nv[0]), nv[1].trim());
        }
        return out;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (int) (this.ct5ItemResponseId ^ (this.ct5ItemResponseId >>> 32));
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
        final Ct5ItemResponse other = (Ct5ItemResponse) obj;
        return this.ct5ItemResponseId == other.ct5ItemResponseId;
    }

    
    public boolean matchesSimJIntn( SimJ.Intn intn)
    {
        if( intn==null )
            return false;
        
        return intn.getCt5Itemid()==ct5ItemId;
    }
    
    public long getCt5ItemResponseId() {
        return ct5ItemResponseId;
    }

    public void setCt5ItemResponseId(long ct5ItemResponseId) {
        this.ct5ItemResponseId = ct5ItemResponseId;
    }

    public long getCt5TestEventId() {
        return ct5TestEventId;
    }

    public void setCt5TestEventId(long ct5TestEventId) {
        this.ct5TestEventId = ct5TestEventId;
    }

    public int getCt5ItemResponseStatusTypeId() {
        return ct5ItemResponseStatusTypeId;
    }

    public void setCt5ItemResponseStatusTypeId(int ct5ItemResponseStatusTypeId) {
        this.ct5ItemResponseStatusTypeId = ct5ItemResponseStatusTypeId;
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

    public int getCt5ItemId() {
        return ct5ItemId;
    }

    public void setCt5ItemId(int ct5ItemId) {
        this.ct5ItemId = ct5ItemId;
    }

    public int getCt5ItemPartId() {
        return ct5ItemPartId;
    }

    public void setCt5ItemPartId(int ct5ItemPartId) {
        this.ct5ItemPartId = ct5ItemPartId;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseStr1() {
        return responseStr1;
    }

    public void setResponseStr1(String responseStr1) {
        this.responseStr1 = responseStr1;
    }

    public int getTimeMillisecs() {
        
        return (int) (totalItemTime*1000f);
    }

    public long getUploadedUserFileId() {
        return uploadedUserFileId;
    }

    public void setUploadedUserFileId(long uploadedUserFileId) {
        this.uploadedUserFileId = uploadedUserFileId;
    }

    public UploadedUserFile getUploadedUserFile() {
        return uploadedUserFile;
    }

    public void setUploadedUserFile(UploadedUserFile uploadedUserFile) {
        this.uploadedUserFile = uploadedUserFile;
    }

    public String getScoreStr1() {
        return scoreStr1;
    }

    public void setScoreStr1(String scoreStr1) {
        this.scoreStr1 = scoreStr1;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseSeq() {
        return responseSeq;
    }

    public void setResponseSeq(int responseSeq) {
        this.responseSeq = responseSeq;
    }

    public float getPoints() {
        return points;
    }

    public void setPoints(float points) {
        this.points = points;
    }

    public float getTotalItemTime() {
        return totalItemTime;
    }

    public void setTotalItemTime(float totalItemTime) {
        this.totalItemTime = totalItemTime;
    }

    public Date getLastTimeUpdate() {
        return lastTimeUpdate;
    }

    public void setLastTimeUpdate(Date lastTimeUpdate) {
        this.lastTimeUpdate = lastTimeUpdate;
    }

    public int getShowCount() {
        return showCount;
    }

    public void setShowCount(int showCount) {
        this.showCount = showCount;
    }

    public int getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(int responseCount) {
        this.responseCount = responseCount;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }

    public int getAccessibleForm() {
        return accessibleForm;
    }

    public void setAccessibleForm(int accessibleForm) {
        this.accessibleForm = accessibleForm;
    }


}
