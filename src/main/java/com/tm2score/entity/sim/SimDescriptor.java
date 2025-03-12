package com.tm2score.entity.sim;

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


///////////////////////////////////////////////////////////////////
// NOTE THAT THIS TABLE IS IN THE TM2 DATABASE!
///////////////////////////////////////////////////////////////////

@Entity
@Table( name = "simdescriptor" )
@XmlRootElement
@NamedQueries({
    @NamedQuery( name = "SimDescriptor.findById", query = "SELECT o FROM SimDescriptor AS o WHERE o.simDescriptorId=:simDescriptorId" ),
    @NamedQuery( name = "SimDescriptor.findBySimIdAndSimVersionId", query = "SELECT o FROM SimDescriptor AS o WHERE o.simId=:simId AND o.simVersionId=:simVersionId" ),
    @NamedQuery( name = "SimDescriptor.findBySimId", query = "SELECT o FROM SimDescriptor AS o WHERE o.simId=:simId ORDER BY o.simVersionId DESC" )

})
public class SimDescriptor implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="simdescriptorid")
    private long simDescriptorId;

    @Column(name="simid")
    private long simId;

    @Column(name="simversionid")
    private int simVersionId;

    @Column(name="actid")
    private long actId;

    @Column(name="costarperformanceid")
    private long costarPerformanceId = 0;

    @Column(name="nameforuser")
    private String nameForUser;
    /**
     * 0=no upload
     * 1=requires file upload but no media.
     * 2=requires recorded media file upload.
     * 
     */
    @Column(name="fileupload")
    private int fileUpload;


    
    @Column(name="imonum")
    private long imoNum = 0;

    @Column(name="scoremoduleclass")
    private String scoreModuleClass;

    @Column(name="reportid")
    private long reportId = 0;

    @Column(name="xml")
    private String xml;

    @Column(name="onetsoc")
    private String onetSoc;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastscoringupdate")
    private Date lastScoringUpdate;


    public String toString() {
        return "SimDescriptor{" + "simDescriptorId=" + simDescriptorId + ", simId=" + simId + ", simVersionId=" + simVersionId + ", actId=" + actId + "}";
    }


    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SimDescriptor other = (SimDescriptor) obj;
        if (this.simDescriptorId != other.simDescriptorId) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.simDescriptorId ^ (this.simDescriptorId >>> 32));
        hash = 97 * hash + (int) (this.simId ^ (this.simId >>> 32));
        hash = 97 * hash + this.simVersionId;
        return hash;
    }

    public long getActId() {
        return actId;
    }

    public void setActId(long actId) {
        this.actId = actId;
    }

    public long getCostarPerformanceId() {
        return costarPerformanceId;
    }

    public void setCostarPerformanceId(long costarPerformanceId) {
        this.costarPerformanceId = costarPerformanceId;
    }

    public long getImoNum() {
        return imoNum;
    }

    public void setImoNum(long imoNum) {
        this.imoNum = imoNum;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getNameForUser() {
        return nameForUser;
    }

    public void setNameForUser(String nameForUser) {
        this.nameForUser = nameForUser;
    }

    public long getSimDescriptorId() {
        return simDescriptorId;
    }

    public void setSimDescriptorId(long simDescriptorId) {
        this.simDescriptorId = simDescriptorId;
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

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public String getScoreModuleClass() {
        return scoreModuleClass;
    }

    public void setScoreModuleClass(String scoreModuleClass) {
        this.scoreModuleClass = scoreModuleClass;
    }

    public String getOnetSoc() {
        return onetSoc;
    }

    public void setOnetSoc(String onetSoc) {
        this.onetSoc = onetSoc;
    }

    public int getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(int fileUpload) {
        this.fileUpload = fileUpload;
    }

    public Date getLastScoringUpdate() {
        return lastScoringUpdate;
    }

    public void setLastScoringUpdate(Date lastScoringUpdate) {
        this.lastScoringUpdate = lastScoringUpdate;
    }



}
