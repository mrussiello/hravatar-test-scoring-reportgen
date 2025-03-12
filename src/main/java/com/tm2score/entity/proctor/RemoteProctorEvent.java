/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.proctor;

import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.event.OnlineProctoringType;
import com.tm2score.proctor.RemoteProctorEventStatusType;
import java.io.Serializable;
import java.util.ArrayList;
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

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "remoteproctorevent" )
@NamedQueries( {
    @NamedQuery( name = "RemoteProctorEvent.findByTestEventId", query = "SELECT o FROM RemoteProctorEvent AS o WHERE o.testEventId=:testEventId" ),
    @NamedQuery( name = "RemoteProctorEvent.findByTestKeyId", query = "SELECT o FROM RemoteProctorEvent AS o WHERE o.testKeyId=:testKeyId" )

} )
public class RemoteProctorEvent implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "remoteproctoreventid" )
    private long remoteProctorEventId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "testserverid" )
    private String testServerId;
    
    @Column( name = "onlineproctoringtypeid" )
    private int onlineProctoringTypeId;
    
    @Column( name = "suspiciousactivitythresholdtypeid" )
    private int suspiciousActivityThresholdTypeId;
    
    /*
     0 = Event Not Completed
     10 = Event completed. 
     20 = Post Media Processing and Conversion Completed
     100 = Analysis Completed
     
    */
    @Column( name = "remoteproctoreventstatustypeid" )
    private int remoteProctorEventStatusTypeId;
    
    @Column( name = "mediatypeid" )
    private int mediaTypeId;
    
    /*
     0 = Not started
     10 = Ready for Post Media Processing
     20 = Post Media Processing Complete
     30 = Conversions Completed    
     100 = All recordings ready     
    */
    @Column( name = "recordingstatustypeid" )
    private int recordingStatusTypeId;

    
    /**
     * Number of uploaded user files.
     */
    @Column( name = "uploadedfilecount" )
    private int uploadedFileCount;
    

    /**
     * 
     */
    @Column( name = "totaldurationsecs" )
    private int totalDurationSecs;
    
    
    /*
     0 = Normal. Minimal or no issues detected.
     10 = Low Risk Issues Detected
     20 = Medium Risk Issues Detected
     30 = High Risk Issues Detected
    */
    @Column( name = "analysisresulttypeid" )
    private int analysisResultTypeId;

    
    
    
    
    /**
     * Packed String 
     * 0=suspended, 1=unsuspended
     * 
     * Date (Long MS);0 or 1;Date (Long MS);0 or 1; ..
     */
    @Column( name = "suspensionhistory" )
    private String suspensionHistory;

    /**
     * Packed String 
     * 
     * testeventid;userid;date(long); ... 
     * 
     */
    @Column( name = "sameiptesteventinfo" )
    private String sameIpTestEventInfo;
    
    /**
     * LONG;LONG;LONG;...
     */
    @Column( name = "imagesdisableddates" )
    private String imagesDisabledDates;
    
        
    @Column( name = "unsuspendedsuspiciousactivitycount" )
    private int unsuspendedSuspiciousActivityCount;

    @Column( name = "idfacematchpercent" )
    private float idFaceMatchPercent=-1;

    @Column( name = "idfacematchconfidence" )
    private float idFaceMatchConfidence;

    /**
     * Total number of thumbs found.
     */
    @Column( name = "thumbsprocessed" )
    private int thumbsProcessed;
    
    /**
     * Thumbs that had a face.
     */
    @Column( name = "thumbspassed" )
    private int thumbsPassed;
    
    /**
     * thumb pairs that had same face
     */
    @Column( name = "thumbpairspassed" )
    private int thumbPairsPassed;
    
    /**
     * thumb pairs that had different faces
     */
    @Column( name = "thumbpairsfailed" )
    private int thumbPairsFailed;
    
    /**
     * thumbs that had more than one face
     */
    @Column( name = "multifacethumbs" )
    private int multiFaceThumbs;
        
    /**
     * this is a score based on thumb comparison.
     * 100=same face, consistent.
     * 50 = too many failed images.
     * <50 mult faces found.
     */
    @Column( name = "thumbscore" )
    private float thumbScore;

    /**
     * combination of multi-face, thumbscore, and id score
     * 
     */
    @Column( name = "overallproctorscore" )
    private float overallProctorScore  = -1;

    
    
    
    
    @Column( name = "note" )
    private String note;
    

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="eventcompletedate")
    private Date eventCompleteDate;

    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    
    
    @Transient
    private List<SuspiciousActivity> suspiciousActivityList;
    
    @Transient
    private List<String[]> suspensionHistoryList;
    

    @Transient
    private List<UploadedUserFile> uploadedUserFileList;
    
    
    /*
     data[0] = date time str
     data[1] = action
     data[2] = name
    */
    @Transient
    private List<String[]> suspensionInfoList;
    
    @Transient
    private boolean hasIncompleteUploadedFiles;
    
    
    @Override
    public String toString() {
        return "RemoteProctorEvent{" + "remoteProctorEventId=" + remoteProctorEventId + ", testEventId=" + testEventId + ", onlineProctoringTypeId=" + onlineProctoringTypeId + ", remoteProctorEventStatusTypeId=" + remoteProctorEventStatusTypeId + ", recordingStatusTypeId=" + recordingStatusTypeId + ", uploadedFileCount=" + uploadedFileCount + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (int) (this.remoteProctorEventId ^ (this.remoteProctorEventId >>> 32));
        hash = 53 * hash + (int) (this.testEventId ^ (this.testEventId >>> 32));
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
        final RemoteProctorEvent other = (RemoteProctorEvent) obj;
        if (this.remoteProctorEventId != other.remoteProctorEventId) {
            return false;
        }
        if (this.testEventId != other.testEventId) {
            return false;
        }
        return true;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException 
    {
        return (RemoteProctorEvent) super.clone(); 
    }

    
    
    public OnlineProctoringType getOnlineProctoringType()
    {
        return OnlineProctoringType.getValue( this.onlineProctoringTypeId );
    }
    
    public boolean getHasSuspensionHistory()
    {
        return suspensionHistory!=null && !suspensionHistory.isBlank();
    }
    
    public List<UploadedUserFile> getUploadedUserFileListForRecordings()
    {
        return getUploadedUserFileList( 0 );
    }
    
    
    public List<UploadedUserFile> getUploadedUserFileListForIds()
    {
        return getUploadedUserFileList( 2 );
    }
    
    public List<UploadedUserFile> getUploadedUserFileListForPhotos()
    {
        return getUploadedUserFileList( 1 );
    }
    
    private List<UploadedUserFile> getUploadedUserFileList( int typeCode )
    {
        List<UploadedUserFile> out = new ArrayList<>();
        
        if( this.uploadedUserFileList==null )
            return out;
        
        for( UploadedUserFile uuf : this.uploadedUserFileList )
        {
            if( typeCode==0 && uuf.getUploadedUserFileType().getIsRemoteProctoring() )
                out.add(uuf);
            
            else if( typeCode==1 && uuf.getUploadedUserFileType().getIsRemoteProctoringImagesOnly())
                out.add(uuf);

            else if( typeCode==2 && uuf.getUploadedUserFileType().getIsRemoteProctoringId() )
                out.add(uuf);
            
            //if( uuf.getThumbFilename()==null || uuf.getThumbFilename().isBlank() || uuf.getMaxThumbIndex()<=0 )
            //    continue;
            
           // //if( facePhotos && uuf.getUploadedUserFileType().getIsRemoteProctoringWithFaceImages() )
             //   out.add(uuf);
            //else if ( !facePhotos && uuf.getUploadedUserFileType().getIsRemoteProctoringId() )
            //    out.add(uuf);
        }
        return out;
    }
    
    public SuspiciousActivity getSuspiciousActivity( int typeId, long userId, long uploadedUserFileId )
    {
        if( this.suspiciousActivityList==null )
            return null;
        for( SuspiciousActivity sa : suspiciousActivityList )
        {
            if( ( typeId<=0 || sa.getSuspiciousActivityTypeId()==typeId) && 
                  sa.getUserId()==userId && 
                  sa.getUploadedUserFileId()==uploadedUserFileId )
                return sa;
        }
        return null;
    }

        
    public List<String[]> parseSuspensionHistory()
    {
        List<String[]> out = new ArrayList<>();
        
        if( suspensionHistory==null || suspensionHistory.isBlank() )
            return out;
        
        String[] vals = suspensionHistory.split(";");
        
        for( int i=0;i<vals.length-3;i+=4 )
        {
            out.add( new String[] {vals[i],vals[i+1],vals[i+2], vals[i+3]} ); 
        }        
        return out;
    }
        
        
    public RemoteProctorEventStatusType getRemoteProctorEventStatusType()
    {
        return RemoteProctorEventStatusType.getValue( this.remoteProctorEventStatusTypeId );
    }
    
    public long getRemoteProctorEventId() {
        return remoteProctorEventId;
    }

    public void setRemoteProctorEventId(long remoteProctorEventId) {
        this.remoteProctorEventId = remoteProctorEventId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public int getOnlineProctoringTypeId() {
        return onlineProctoringTypeId;
    }

    public void setOnlineProctoringTypeId(int onlineProctoringTypeId) {
        this.onlineProctoringTypeId = onlineProctoringTypeId;
    }

    public int getSuspiciousActivityThresholdTypeId() {
        return suspiciousActivityThresholdTypeId;
    }

    public void setSuspiciousActivityThresholdTypeId(int suspiciousActivityThresholdTypeId) {
        this.suspiciousActivityThresholdTypeId = suspiciousActivityThresholdTypeId;
    }

    public int getRemoteProctorEventStatusTypeId() {
        return remoteProctorEventStatusTypeId;
    }

    public void setRemoteProctorEventStatusTypeId(int remoteProctorEventStatusTypeId) {
        this.remoteProctorEventStatusTypeId = remoteProctorEventStatusTypeId;
    }

    public int getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(int mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public int getRecordingStatusTypeId() {
        return recordingStatusTypeId;
    }

    public void setRecordingStatusTypeId(int recordingStatusTypeId) {
        this.recordingStatusTypeId = recordingStatusTypeId;
    }

    public int getUploadedFileCount() {
        return uploadedFileCount;
    }

    public void setUploadedFileCount(int uploadedFileCount) {
        this.uploadedFileCount = uploadedFileCount;
    }

    public int getAnalysisResultTypeId() {
        return analysisResultTypeId;
    }

    public void setAnalysisResultTypeId(int analysisResultTypeId) {
        this.analysisResultTypeId = analysisResultTypeId;
    }

    public String getSuspensionHistory() {
        return suspensionHistory;
    }

    public void setSuspensionHistory(String suspensionHistory) {
        this.suspensionHistory = suspensionHistory;
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

    public Date getEventCompleteDate() {
        return eventCompleteDate;
    }

    public void setEventCompleteDate(Date eventCompleteDate) {
        this.eventCompleteDate = eventCompleteDate;
    }
    
    public int getTotalDurationSecs() {
        return totalDurationSecs;
    }

    public void setTotalDurationSecs(int totalDurationSecs) {
        this.totalDurationSecs = totalDurationSecs;
    }

    public List<SuspiciousActivity> getSuspiciousActivityList() {
        return suspiciousActivityList;
    }

    public void setSuspiciousActivityList(List<SuspiciousActivity> suspiciousActivityList) {
        this.suspiciousActivityList = suspiciousActivityList;
    }


    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getUnsuspendedSuspiciousActivityCount() {
        return unsuspendedSuspiciousActivityCount;
    }

    public void setUnsuspendedSuspiciousActivityCount(int unsuspendedSuspiciousActivityCount) {
        this.unsuspendedSuspiciousActivityCount = unsuspendedSuspiciousActivityCount;
    }

    public String getTestServerId() {
        return testServerId;
    }

    public void setTestServerId(String testServerId) {
        this.testServerId = testServerId;
    }

    public boolean isHasIncompleteUploadedFiles() {
        return hasIncompleteUploadedFiles;
    }

    public void setHasIncompleteUploadedFiles(boolean hasIncompleteUploadedFiles) {
        this.hasIncompleteUploadedFiles = hasIncompleteUploadedFiles;
    }

    public List<String[]> getSuspensionInfoList() {
        return suspensionInfoList;
    }

    public void setSuspensionInfoList(List<String[]> suspensionInfoList) {
        this.suspensionInfoList = suspensionInfoList;
    }

    public List<String[]> getSuspensionHistoryList() {
        return suspensionHistoryList;
    }

    public void setSuspensionHistoryList(List<String[]> suspensionHistoryList) {
        this.suspensionHistoryList = suspensionHistoryList;
    }

    public float getIdFaceMatchPercent() {
        return idFaceMatchPercent;
    }

    public void setIdFaceMatchPercent(float idFaceMatchPercent) {
        this.idFaceMatchPercent = idFaceMatchPercent;
    }


    public float getIdFaceMatchConfidence() {
        return idFaceMatchConfidence;
    }

    public void setIdFaceMatchConfidence(float idFaceMatchConfidence) {
        this.idFaceMatchConfidence = idFaceMatchConfidence;
    }

    public List<UploadedUserFile> getUploadedUserFileList() {
        return uploadedUserFileList;
    }

    public void setUploadedUserFileList(List<UploadedUserFile> uploadedUserFileList) {
        this.uploadedUserFileList = uploadedUserFileList;
    }

    public int getThumbsProcessed() {
        return thumbsProcessed;
    }

    public void setThumbsProcessed(int thumbsProcessed) {
        this.thumbsProcessed = thumbsProcessed;
    }

    public int getThumbsPassed() {
        return thumbsPassed;
    }

    public void setThumbsPassed(int thumbsPassed) {
        this.thumbsPassed = thumbsPassed;
    }

    public int getThumbPairsPassed() {
        return thumbPairsPassed;
    }

    public void setThumbPairsPassed(int thumbPairsPassed) {
        this.thumbPairsPassed = thumbPairsPassed;
    }

    public int getThumbPairsFailed() {
        return thumbPairsFailed;
    }

    public void setThumbPairsFailed(int thumbPairsFailed) {
        this.thumbPairsFailed = thumbPairsFailed;
    }

    public int getMultiFaceThumbs() {
        return multiFaceThumbs;
    }

    public void setMultiFaceThumbs(int multiFaceThumbs) {
        this.multiFaceThumbs = multiFaceThumbs;
    }

    public float getThumbScore() {
        return thumbScore;
    }

    public void setThumbScore(float thumbScore) {
        this.thumbScore = thumbScore;
    }

    public float getOverallProctorScore() {
        return overallProctorScore;
    }

    public void setOverallProctorScore(float overallProctorScore) {
        this.overallProctorScore = overallProctorScore;
    }

    public String getSameIpTestEventInfo() {
        return sameIpTestEventInfo;
    }

    public void setSameIpTestEventInfo(String sameIpTestEventInfo) {
        this.sameIpTestEventInfo = sameIpTestEventInfo;
    }

    public String getImagesDisabledDates() {
        return imagesDisabledDates;
    }

    public void setImagesDisabledDates(String imagesDisabledDates) {
        this.imagesDisabledDates = imagesDisabledDates;
    }

    
    
    
}
