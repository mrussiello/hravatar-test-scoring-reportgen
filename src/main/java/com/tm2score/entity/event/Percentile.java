package com.tm2score.entity.event;

import java.io.Serializable;
import jakarta.persistence.Cacheable;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Cacheable(false)
@Entity
@Table( name = "percentile" )
@NamedQueries( {
    @NamedQuery ( name="Percentile.findByTestEventIdTestEventScoreId", query="SELECT o FROM Percentile AS o WHERE o.testEventId = :testEventId AND o.testEventScoreId=:testEventScoreId" ),
    @NamedQuery ( name="Percentile.findByTestEventId", query="SELECT o FROM Percentile AS o WHERE o.testEventId = :testEventId" )
} )
public class Percentile implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "percentileid" )
    private long percentileId;

    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "testeventscoreid" )
    private long testEventScoreId;

    @Column( name = "testeventscoretypeid" )
    private int testEventScoreTypeId;

    
    /**
     * Uses PercentileScoreType - indicates 
     */
    @Column( name = "scoretypeid" )
    private int percentileScoreTypeId;

    @Column( name = "score" )
    private float score = 0;

    @Column( name = "rawscore" )
    private float rawScore;

    @Column( name = "score2" )
    private float score2 = 0;

    
    
    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;

    @Column( name = "simid" )
    private long simId;

    @Column( name = "simversionid" )
    private int simVersionId;


    @Column( name = "ipcountry" )
    private String ipCountry;

    @Column( name = "custom1" )
    private String custom1;

    @Column( name = "custom2" )
    private String custom2;

    @Column( name = "custom3" )
    private String custom3;

    @Column( name = "productid" )
    private int productId;

    @Column( name = "simletid" )
    private long simletId;

    @Column( name = "simletversionid" )
    private int simletVersionId;

    @Column( name = "simletcompetencyid" )
    private long simletCompetencyId;

    @Column( name = "simcompetencyid" )
    private long simCompetencyId;

    @Override
    public String toString() {
        return "Percentile{" + "percentileId=" + percentileId + ", testEventId=" + testEventId + ", testEventScoreId=" + testEventScoreId + ", score=" + score + ", rawScore=" + rawScore + ", orgId=" + orgId + ", suborgId=" + suborgId + ", simId=" + simId + ", productId=" + productId + ", simletCompetencyId=" + simletCompetencyId + '}';
    }

    


    public long getPercentileId() {
        return percentileId;
    }

    public void setPercentileId(long percentileId) {
        this.percentileId = percentileId;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public long getTestEventScoreId() {
        return testEventScoreId;
    }

    public void setTestEventScoreId(long testEventScoreId) {
        this.testEventScoreId = testEventScoreId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getRawScore() {
        return rawScore;
    }

    public void setRawScore(float rawScore) {
        this.rawScore = rawScore;
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

    public String getIpCountry() {
        return ipCountry;
    }

    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public long getSimletId() {
        return simletId;
    }

    public void setSimletId(long simletId) {
        this.simletId = simletId;
    }

    public int getSimletVersionId() {
        return simletVersionId;
    }

    public void setSimletVersionId(int simletVersionId) {
        this.simletVersionId = simletVersionId;
    }

    public long getSimletCompetencyId() {
        return simletCompetencyId;
    }

    public void setSimletCompetencyId(long simletCompetencyId) {
        this.simletCompetencyId = simletCompetencyId;
    }

    public long getSimCompetencyId() {
        return simCompetencyId;
    }

    public void setSimCompetencyId(long simCompetencyId) {
        this.simCompetencyId = simCompetencyId;
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

    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    public String getCustom3() {
        return custom3;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    public int getTestEventScoreTypeId() {
        return testEventScoreTypeId;
    }

    public void setTestEventScoreTypeId(int testEventScoreTypeId) {
        this.testEventScoreTypeId = testEventScoreTypeId;
    }

    public int getPercentileScoreTypeId() {
        return percentileScoreTypeId;
    }

    public void setPercentileScoreTypeId(int percentileScoreTypeId) {
        this.percentileScoreTypeId = percentileScoreTypeId;
    }

    public float getScore2() {
        return score2;
    }

    public void setScore2(float score2) {
        this.score2 = score2;
    }


}
