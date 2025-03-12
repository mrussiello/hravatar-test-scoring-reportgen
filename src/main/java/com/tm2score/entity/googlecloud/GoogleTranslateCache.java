package com.tm2score.entity.googlecloud;



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
@Table( name = "googletranslatecache" )
@NamedQueries( {
    //@NamedQuery( name = "GoogleTranslateCache.findSrcCompress", query = "SELECT o FROM GoogleTranslateCache AS o WHERE o.srcCompress=:srcCompress AND o.srcText=:srcText" ),
    //@NamedQuery( name = "GoogleTranslateCache.findSrcCompressAndSrcLang", query = "SELECT o FROM GoogleTranslateCache AS o WHERE o.srcCompress=:srcCompress AND o.srcLang=:srcLang AND o.srcText=:srcText" ),
    //@NamedQuery( name = "GoogleTranslateCache.findSrcCompressAndTgtLang", query = "SELECT o FROM GoogleTranslateCache AS o WHERE o.srcCompress=:srcCompress AND o.tgtLang=:tgtLang AND o.srcText=:srcText" ),
    @NamedQuery( name = "GoogleTranslateCache.findSrcCompressAndSrcAndTgtLangs", query = "SELECT o FROM GoogleTranslateCache AS o WHERE o.srcCompress=:srcCompress AND o.srcLang=:srcLang AND o.tgtLang=:tgtLang" )
} )
public class GoogleTranslateCache implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "googletranslatecacheid" )
    private int googleTranslateCacheId;

    /**
     * Note this is only 80 chars long, so it's not a unique key. But it's helpful and unique in many cases.
     */
    @Column( name = "srccompress" )
    private String srcCompress;

    @Column( name = "srclang" )
    private String srcLang;

    @Column( name = "tgtlang" )
    private String tgtLang;

    @Column( name = "srctext" )
    private String srcText;

    @Column( name = "tgttext" )
    private String tgtText;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Override
    public String toString() {
        return "GoogleTranslateCache{" + "googleTranslateCacheId=" + googleTranslateCacheId + ", srcCompress=" + srcCompress + ", srcLang=" + srcLang + ", tgtLang=" + tgtLang + ", lastUpdate=" + lastUpdate + '}';
    }

    
    
    
    
    public int getGoogleTranslateCacheId() {
        return googleTranslateCacheId;
    }

    public void setGoogleTranslateCacheId(int googleTranslateCacheId) {
        this.googleTranslateCacheId = googleTranslateCacheId;
    }


    public String getSrcLang() {
        return srcLang;
    }

    public void setSrcLang(String srcLang) {
        this.srcLang = srcLang;
    }

    public String getTgtLang() {
        return tgtLang;
    }

    public void setTgtLang(String tgtLang) {
        this.tgtLang = tgtLang;
    }


    public String getSrcText() {
        return srcText;
    }

    public void setSrcText(String srcText) {
        this.srcText = srcText;
    }

    public String getTgtText() {
        return tgtText;
    }

    public void setTgtText(String tgtText) {
        this.tgtText = tgtText;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getSrcCompress() {
        return srcCompress;
    }

    public void setSrcCompress(String srcCompress) {
        this.srcCompress = srcCompress;
    }


    
}
