/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.file;

import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.file.ConversionStatusType;
import com.tm2score.file.FileContentType;
import com.tm2score.file.UploadedFileProcessingType;
import com.tm2score.file.UploadedUserFileStatusType;
import com.tm2score.file.UploadedUserFileType;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
@Table( name = "uploadeduserfile" )
@NamedQueries( {
    @NamedQuery( name = "UploadedUserFile.findTestEventIdAndNodeUniqueSubnode", query = "SELECT o FROM UploadedUserFile AS o WHERE o.testEventId=:testEventId AND o.nodeUniqueId=:nodeUniqueId AND o.subnodeSeq=:subnodeSeq AND o.uploadedUserFileTypeId<100" ),
    @NamedQuery( name = "UploadedUserFile.findByUploadedUserFileId", query = "SELECT o FROM UploadedUserFile AS o WHERE o.uploadedUserFileId=:uploadedUserFileId" ),
    @NamedQuery( name = "UploadedUserFile.findTestEventIdAndNodeSubnode", query = "SELECT o FROM UploadedUserFile AS o WHERE o.testEventId=:testEventId AND o.nodeSeq=:nodeSeq AND o.subnodeSeq=:subnodeSeq AND o.uploadedUserFileTypeId<100" ),
    @NamedQuery( name = "UploadedUserFile.findTestEventId", query = "SELECT o FROM UploadedUserFile AS o WHERE o.testEventId=:testEventId AND o.uploadedUserFileTypeId<100" ),
    @NamedQuery( name = "UploadedUserFile.findTestEventIdAndType", query = "SELECT o FROM UploadedUserFile AS o WHERE o.testEventId=:testEventId AND o.uploadedUserFileTypeId=:uploadedUserFileTypeId" ),
    @NamedQuery( name = "UploadedUserFile.findTestKeyIdAndType", query = "SELECT o FROM UploadedUserFile AS o WHERE o.testKeyId=:testKeyId AND o.uploadedUserFileTypeId=:uploadedUserFileTypeId" ),
    @NamedQuery( name = "UploadedUserFile.findByTestKeyIdAndTypeWithMaxTestEventId", query = "SELECT o FROM UploadedUserFile AS o WHERE o.testKeyId=:testKeyId AND o.uploadedUserFileTypeId=:uploadedUserFileTypeId AND o.testEventId<=:maxTestEventId ORDER BY o.uploadedUserFileId" )
} )
public class UploadedUserFile implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "uploadeduserfileid" )
    private long uploadedUserFileId;

    @Column( name = "uploadeduserfilestatustypeid" )
    private int uploadedUserFileStatusTypeId;

    @Column( name = "uploadeduserfiletypeid" )
    private int uploadedUserFileTypeId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    
    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "actid" )
    private long actId;

    @Column( name = "nodeseq" )
    private int nodeSeq;

    @Column( name = "subnodeseq" )
    private int subnodeSeq;

    @Column( name = "nodeuniqueid" )
    private String nodeUniqueId;
    
    @Column( name = "fileprocessingtypeid" )
    private int fileProcessingTypeId;

    @Column( name = "avitemresponseid" )
    private long avItemResponseId;

    @Column( name = "avitemtypeid" )
    private int avItemTypeId;

    
    @Column( name = "initialfilesize" )
    private int initialFileSize;

    @Column( name = "initialfilename" )
    private String initialFilename;

    @Column( name = "initialfilecontenttypeid" )
    private int initialFileContentTypeId;

    @Column( name = "orientation" )
    private int orientation;

    @Column( name = "initialmime" )
    private String initialMime;

    @Column( name = "filename" )
    private String filename;

    @Column( name = "mime" )
    private String mime;

    @Column( name = "filecontenttypeid" )
    private int fileContentTypeId;

    @Column( name = "conversionstatustypeid" )
    private int conversionStatusTypeId;
    
    @Column( name = "errorcount" )
    private int errorCount;
    
    

    @Column( name = "thumbfilename" )
    private String thumbFilename;    
    
    @Column( name = "thumbwidth" )
    private int thumbWidth;    
    
    @Column( name = "thumbheight" )
    private int thumbHeight;    
    
    @Column( name = "maxthumbindex" )
    private int maxThumbIndex;    
        
    @Column( name = "failedthumbindices" )
    private String failedThumbIndices;    

    @Column( name = "passedthumbindices" )
    private String passedThumbIndices;    
    
    @Column( name = "pretestthumbindices" )
    private String preTestThumbIndices;    
    
    
    @Column( name = "r1" )
    private int r1;

    @Column( name = "r2" )
    private long r2;

    @Column( name = "simid" )
    private long simId;

    @Column( name = "simversionid" )
    private int simVersionId;
        
    @Column( name = "productid" )
    private int productId;


    @Column( name = "width" )
    private int width;

    @Column( name = "height" )
    private int height;

    @Column( name = "duration" )
    private float duration;

    @Column( name = "note" )
    private String note;

    /**
     * For images, this is a type indicator
     *    0=unknown
     *    1=face but face not detected. 
     *    2=face but low confidence.
     *    3=face but multi faces.
     * 
     *    20=face detected. 
     *    
     */
    @Column( name = "meta1" )
    private float meta1;

    /**
     * For images with faces, this is confidence
     *    
     */
    @Column( name = "meta2" )
    private float meta2;

    /**
     * For images with faces, this is pitch
     *    
     */
    @Column( name = "meta3" )
    private float meta3;

    /**
     * For images with faces, this is yaw
     *    
     */
    @Column( name = "meta4" )
    private float meta4;
    

    @Column( name = "meta5" )
    private float meta5;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupload")
    private Date lastUpload;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="conversionstatusdate")
    private Date conversionStatusDate;


    @Transient
    private AvItemResponse avItemResponse;

    @Transient
    String tempStr1;

    @Transient
    String tempStr2;

    @Transient
    String tempStr3;

    @Transient
    int tempInt1;
    
    @Transient
    int tempInt2;
    
    @Transient
    int tempInt3;
    
    @Transient
    private Set<Integer> preTestIndexSet;

    @Transient
    private Map<Integer,Integer> failedIndexMap;
    
    @Transient
    private Set<Integer> passedIndexSet;
        
    
    @Transient
    private boolean failedImage;
    
    @Transient
    private boolean preTestImage;
    
    @Transient
    private int proctorImageErrorTypeId;
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (int) (this.uploadedUserFileId ^ (this.uploadedUserFileId >>> 32));
        hash = 89 * hash + (int) (this.actId ^ (this.actId >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UploadedUserFile other = (UploadedUserFile) obj;
        if (this.uploadedUserFileId != other.uploadedUserFileId) {
            return false;
        }
        return true;
    }

    public UploadedUserFileType getUploadedUserFileType()
    {
        return UploadedUserFileType.getValue( uploadedUserFileTypeId );
    }


    @Override
    public String toString() {
        return "UploadedUserFile{" + "uploadedUserFileId=" + uploadedUserFileId + ", testEventId=" + testEventId + ", actId=" + actId + ", nodeSeq=" + nodeSeq + ", subnodeSeq=" + subnodeSeq + ", initialFileSize=" + initialFileSize + ", initialFilename=" + initialFilename + ", initialFileContentTypeId=" + initialFileContentTypeId + ", initialMime=" + initialMime + ", filename=" + filename + ", mime=" + mime + ", fileContentTypeId=" + fileContentTypeId + ", conversionStatusTypeId=" + conversionStatusTypeId + ", r1=" + r1 + ", r2=" + r2 + '}';
    }
    
    
    
    
    public boolean hasFailedIndex( int idx )
    {
        if( failedIndexMap==null )
            initFailedIndexMap();
        return failedIndexMap.containsKey( idx );
    }
    public boolean hasPassedIndex( int idx )
    {
        if( passedIndexSet==null )
            initPassedIndexSet();
        return passedIndexSet.contains( idx );
    }
    
    public int getFailedIndexErrorTypeId( int idx )
    {
        if( !hasFailedIndex( idx ) )
            return -1;
        
        if( failedIndexMap==null )
            initFailedIndexMap();
        
        Integer t = failedIndexMap.get(idx);
        return t==null ? -1 : t;
    }

    public synchronized void initFailedIndexMap()
    {
        if( failedIndexMap==null )
            failedIndexMap = new TreeMap<>();        
        initIndexMap( failedIndexMap, failedThumbIndices );
    }

    public synchronized void initPassedIndexSet()
    {
        if( passedIndexSet==null )
            passedIndexSet = new HashSet<>();        
        initIndexSet( passedIndexSet, passedThumbIndices );
    }

    public synchronized void initIndexSet( Set<Integer> indexMap, String srcStr )
    {
        if( indexMap==null )
            return;        
        if( srcStr==null || srcStr.isBlank() )
            return;        
        int idx;        
        for( String s : srcStr.split(",") )
        {
            if( s.isBlank() )
                continue;
            idx = Integer.parseInt(s);
            
            // all good.
            indexMap.add( idx);
        }
    }

    
    public synchronized void initIndexMap( Map<Integer,Integer> indexMap, String srcStr )
    {
        if( indexMap==null )
            return;
        
        if( srcStr==null || srcStr.isBlank() )
            return;
        
        int errorTypeId;
        int idx;
        
        for( String s : srcStr.split(",") )
        {
            if( s.isBlank() )
                continue;
            
            errorTypeId=0;
            if( s.indexOf(":")>0 )
            {
                // new format
                if( s.length()>s.indexOf(":")+1 )
                {
                    idx = Integer.parseInt( s.substring(0, s.indexOf(":")));
                    errorTypeId = Integer.parseInt( s.substring(s.indexOf(":")+1,s.length()));
                }
                
                // something wrong.
                else
                    idx=-1;
            }    
            
            // old format
            else
                idx = Integer.parseInt( s );
            
            // all good.
            if( idx>=0 )
                indexMap.put( idx, errorTypeId );
        }
    }

    
    public boolean hasPreTestIndex( int idx )
    {
        if( preTestIndexSet==null )
            initPreTestIndexSet();

        return preTestIndexSet.contains( idx );
    }
    
    
    public synchronized void initPreTestIndexSet()
    {
        if( preTestIndexSet==null )
            preTestIndexSet = new TreeSet<>();
        
        if( preTestThumbIndices==null || preTestThumbIndices.isBlank() )
            return;
        
        for( String s : preTestThumbIndices.split(",") )
        {
            if( s.isBlank() )
                continue;
            preTestIndexSet.add(Integer.valueOf(s) );
        }
    }

    
        
    public String rotationCss( boolean withMargin )
    {
        // orientation = 90;
        
        if( orientation!=90 && orientation!=270 )
            return "";
        
        float w = getThumbFilename()!=null && !getThumbFilename().isEmpty() && thumbWidth>0 ? thumbWidth : width;
                
        float h = getThumbFilename()!=null && !getThumbFilename().isEmpty() && thumbHeight>0 ? thumbHeight : height;
        
        float factor = w>0 && w>200 ? 200f/w : 1f;
        
        float winW = w*factor;
        
        float winH = h*factor;
        
        float tm = Math.max( (winW-winH)/2, 0);
        
        
        //if( orientation==90 || orientation==270 )
        //    minHeight = ";max-height:200px;";
        
        return "transform-origin:center;transform:rotate(" + orientation + "deg);" + ( withMargin ?  "margin-top:" + ((int) tm) + "px" : "" );
    }
    
    public String getRotationOffsetCss()
    {  
        // orientation=90;        
        if( orientation!=90 && orientation!=270 )
            return "";
        
        float w = getThumbFilename()!=null && !getThumbFilename().isEmpty() && thumbWidth>0 ? thumbWidth : width;
                
        float h = getThumbFilename()!=null && !getThumbFilename().isEmpty() && thumbHeight>0 ? thumbHeight : height;
        
        float factor = w>0 && w>200 ? 200f/w : 1f;
        
        float winW = w*factor;
        
        float winH = h*factor;
        
        float nh = winW>winH ?  winW : winH;
        // float tm = Math.max( (winW-winH)/2, 0);        
        
        return "height:" + ((int)( nh )) + "px;";
        
        //if( orientation==90 || orientation==270 )
        //    minHeight = ";max-height:200px;";
        
        // return minWid + "transform-origin:center;transform:rotate(" + orientation + "deg);";
    }
    
    
    public UploadedUserFileStatusType getUploadedUserFileStatusType()
    {
        return UploadedUserFileStatusType.getValue( this.uploadedUserFileStatusTypeId );
    }

    
    
    
    public boolean hasImageFile()
    {
        return getFileContentType().getIsImage() && filename!=null && !filename.trim().isEmpty();
    }

    public FileContentType getFileContentType()
    {
        if( this.fileContentTypeId>0 )
            return FileContentType.getValue( this.fileContentTypeId );
        
        return FileContentType.getFileContentTypeFromContentType( mime, filename );
    }

    public ConversionStatusType getConversionStatusType()
    {
        return ConversionStatusType.getValue( this.conversionStatusTypeId );
    }

    public UploadedFileProcessingType getFileProcessingType()
    {
        return UploadedFileProcessingType.getValue( this.getFileProcessingTypeId() );
    }




    public String getDirectory()
    {
        if( getUploadedUserFileType().getIsResponse() )
            return "/" + r1 + "/" + r2 + "/" + actId + "/" + testEventId;

        else if( getUploadedUserFileType().getIsAnyPremiumRemoteProctoring() )
            return "/" + r1 + "/" + r2;   
        
        return "/" + r1 + "/" + r2 + "/" + actId + "/" + testEventId;
    }


    public long getUploadedUserFileId() {
        return uploadedUserFileId;
    }

    public void setUploadedUserFileId(long uploadedUserFileId) {
        this.uploadedUserFileId = uploadedUserFileId;
    }


    public long getActId() {
        return actId;
    }

    public void setActId(long actId) {
        this.actId = actId;
    }

    public int getNodeSeq() {
        return nodeSeq;
    }

    public void setNodeSeq(int nodeSeq) {
        this.nodeSeq = nodeSeq;
    }

    public int getSubnodeSeq() {
        return subnodeSeq;
    }

    public void setSubnodeSeq(int subnodeSeq) {
        this.subnodeSeq = subnodeSeq;
    }

    public int getInitialFileSize() {
        return initialFileSize;
    }

    public void setInitialFileSize(int initialFileSize) {
        this.initialFileSize = initialFileSize;
    }

    public String getInitialFilename() {
        return initialFilename;
    }

    public void setInitialFilename(String initialFilename) {
        this.initialFilename = initialFilename;
    }

    public int getInitialFileContentTypeId() {
        return initialFileContentTypeId;
    }

    public void setInitialFileContentTypeId(int initialFileContentTypeId) {
        this.initialFileContentTypeId = initialFileContentTypeId;
    }

    public String getInitialMime() {
        return initialMime;
    }

    public void setInitialMime(String initialMime) {
        this.initialMime = initialMime;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public int getFileContentTypeId() {
        return fileContentTypeId;
    }

    public void setFileContentTypeId(int fileContentTypeId) {
        this.fileContentTypeId = fileContentTypeId;
    }

    public int getConversionStatusTypeId() {
        return conversionStatusTypeId;
    }

    public void setConversionStatusTypeId(int conversionStatusTypeId) {
        this.conversionStatusDate = new Date();
        this.conversionStatusTypeId = conversionStatusTypeId;
    }

    public int getR1() {
        return r1;
    }

    public void setR1(int r1) {
        this.r1 = r1;
    }

    public long getR2() {
        return r2;
    }

    public void setR2(long r2) {
        this.r2 = r2;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpload() {
        return lastUpload;
    }

    public void setLastUpload(Date lastUpload) {
        this.lastUpload = lastUpload;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public long getSimId() {
        return simId;
    }

    public void setSimId(long simId) {
        this.simId = simId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getFileProcessingTypeId() {
        return fileProcessingTypeId;
    }

    public void setFileProcessingTypeId(int fileProcessingTypeId) {
        this.fileProcessingTypeId = fileProcessingTypeId;
    }

    public long getAvItemResponseId() {
        return avItemResponseId;
    }

    public void setAvItemResponseId(long avItemResponseId) {
        this.avItemResponseId = avItemResponseId;
    }

    public int getSimVersionId() {
        return simVersionId;
    }

    public void setSimVersionId(int simVersionId) {
        this.simVersionId = simVersionId;
    }

    public String getThumbFilename() {
        return thumbFilename;
    }

    public void setThumbFilename(String thumbFilename) {
        this.thumbFilename = thumbFilename;
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
        this.thumbWidth = thumbWidth;
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
        this.thumbHeight = thumbHeight;
    }

    public int getOrientation() {
        
        //if( 1==1 )
        //    return 90;
        
        return orientation;
    }

    public int getOrientationForIText() {
        
        //if( 1==1 )
        //    return 90;
        switch (orientation) {
            case 0:
                return 0;
            case 90:
            case -270:
                return -90;
            case -90:
            case 270:
                return 90;
            default:
                break;
        }
        
        return orientation;
    }
    
    
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getNodeUniqueId() {
        return nodeUniqueId;
    }

    public void setNodeUniqueId(String nodeUniqueId) {
        this.nodeUniqueId = nodeUniqueId;
    }

    public int getUploadedUserFileStatusTypeId() {
        return uploadedUserFileStatusTypeId;
    }

    public void setUploadedUserFileStatusTypeId(int uploadedUserFileStatusTypeId) {
        this.uploadedUserFileStatusTypeId = uploadedUserFileStatusTypeId;
    }

    public AvItemResponse getAvItemResponse() {
        return avItemResponse;
    }

    public void setAvItemResponse(AvItemResponse avItemResponse) {
        this.avItemResponse = avItemResponse;
    }

    public int getAvItemTypeId() {
        return avItemTypeId;
    }

    public void setAvItemTypeId(int avItemTypeId) {
        this.avItemTypeId = avItemTypeId;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public Date getConversionStatusDate() {
        return conversionStatusDate;
    }

    public void setConversionStatusDate(Date conversionStatusDate) {
        this.conversionStatusDate = conversionStatusDate;
    }

    public int getUploadedUserFileTypeId() {
        return uploadedUserFileTypeId;
    }

    public void setUploadedUserFileTypeId(int uploadedUserFileTypeId) {
        this.uploadedUserFileTypeId = uploadedUserFileTypeId;
    }

    public float getMeta1() {
        return meta1;
    }

    public void setMeta1(float meta1) {
        this.meta1 = meta1;
    }

    public float getMeta2() {
        return meta2;
    }

    public void setMeta2(float meta2) {
        this.meta2 = meta2;
    }

    public float getMeta3() {
        return meta3;
    }

    public void setMeta3(float meta3) {
        this.meta3 = meta3;
    }

    public float getMeta4() {
        return meta4;
    }

    public void setMeta4(float meta4) {
        this.meta4 = meta4;
    }

    public float getMeta5() {
        return meta5;
    }

    public void setMeta5(float meta5) {
        this.meta5 = meta5;
    }

    public int getMaxThumbIndex() {
        return maxThumbIndex;
    }

    public void setMaxThumbIndex(int maxThumbIndex) {
        this.maxThumbIndex = maxThumbIndex;
    }

    public String getTempStr1() {
        return tempStr1;
    }

    public void setTempStr1(String tempStr1) {
        this.tempStr1 = tempStr1;
    }

    public String getTempStr2() {
        return tempStr2;
    }

    public void setTempStr2(String tempStr2) {
        this.tempStr2 = tempStr2;
    }

    public String getTempStr3() {
        return tempStr3;
    }

    public void setTempStr3(String tempStr3) {
        this.tempStr3 = tempStr3;
    }

    public int getTempInt1() {
        return tempInt1;
    }

    public void setTempInt1(int tempInt1) {
        this.tempInt1 = tempInt1;
    }

    public int getTempInt2() {
        return tempInt2;
    }

    public void setTempInt2(int tempInt2) {
        this.tempInt2 = tempInt2;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public String getFailedThumbIndices() {
        return failedThumbIndices;
    }

    public void setFailedThumbIndices(String failedThumbIndices) {
        this.failedThumbIndices = failedThumbIndices;
    }

    public String getPreTestThumbIndices() {
        return preTestThumbIndices;
    }

    public void setPreTestThumbIndices(String preTestThumbIndices) {
        this.preTestThumbIndices = preTestThumbIndices;
    }

    public boolean isFailedImage() {
        return failedImage;
    }

    public void setFailedImage(boolean failedImage) {
        this.failedImage = failedImage;
    }

    public boolean isPreTestImage() {
        return preTestImage;
    }

    public void setPreTestImage(boolean preTestImage) {
        this.preTestImage = preTestImage;
    }

    public int getProctorImageErrorTypeId() {
        return proctorImageErrorTypeId;
    }

    public void setProctorImageErrorTypeId(int proctorImageErrorTypeId) {
        this.proctorImageErrorTypeId = proctorImageErrorTypeId;
    }

    public Map<Integer, Integer> getFailedIndexMap() {
        return failedIndexMap;
    }

    public Set<Integer> getPassedIndexSet() {
        return passedIndexSet;
    }
        
    public String getPassedThumbIndices() {
        return passedThumbIndices;
    }

    public void setPassedThumbIndices(String passedThumbIndices) {
        this.passedThumbIndices = passedThumbIndices;
    }

    public int getTempInt3() {
        return tempInt3;
    }

    public void setTempInt3(int tempInt3) {
        this.tempInt3 = tempInt3;
    }
    
    
}
