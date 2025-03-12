package com.tm2score.entity.battery;

import com.tm2score.event.ScoreFormatType;
import com.tm2score.global.ErrorTxtObject;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
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
@Table( name = "batteryscore" )
@NamedQueries( {
        @NamedQuery( name = "BatteryScore.findByTestKeyId", query = "SELECT o FROM BatteryScore AS o WHERE o.testKeyId=:testKeyId" )
} )
public class BatteryScore implements Serializable, ErrorTxtObject
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "batteryscoreid" )
    private long batteryScoreId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "batteryid" )
    private int batteryId;


    @Column( name = "userid" )
    private long userId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;


    @Column( name = "batteryscoretypeid" )
    private int batteryScoreTypeId;

    @Column( name = "batteryscorestatustypeid" )
    private int batteryScoreStatusTypeId;

    @Column(name="scoreformattypeid")
    private int scoreFormatTypeId;

    @Column( name = "score" )
    private float score = 0;

    @Column( name = "rawscore" )
    private float rawScore;

    //@Column( name = "percentile" )
    //private float percentile = 0;

    @Column( name = "accountpercentile" )
    private float accountPercentile = -1;

    @Column( name = "accountpercentilecount" )
    private int accountPercentileCount = 0;

    @Column( name = "countrypercentile" )
    private float countryPercentile = -1;

    @Column( name = "countrypercentilecount" )
    private int countryPercentileCount = 0;
    
    @Column( name = "excludefmnorms" )
    private int excludeFmNorms = 0;



    @Column( name = "scoretext" )
    private String scoreText;

    @Column( name = "scorecategoryid" )
    private int scoreCategoryId;

    @Column(name="errortxt")
    private String errorTxt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="startdate")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastaccessdate")
    private Date lastAccessDate;


    @Override
    public String toString() {
        return "BatteryScore{" + "batteryScoreId=" + batteryScoreId + ", batteryId=" + batteryId + ", testKeyId=" + testKeyId + ", score=" + score + '}';
    }


    public String getBatteryScoreIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( batteryScoreId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "getBatteryScoreIdEncrypted() batteryScoreId=" + batteryScoreId );
            return "";
        }
    }
    
    public String getOrgIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( orgId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "getOrgIdEncrypted() batteryScoreId=" + batteryScoreId + ", orgId=" + orgId );
            return "";
        }
    }    
    
    public ScoreFormatType getScoreFormatType()
    {
        return ScoreFormatType.getValue( scoreFormatTypeId );
    }

    public long getBatteryScoreId() {
        return batteryScoreId;
    }

    public void setBatteryScoreId(long batteryScoreId) {
        this.batteryScoreId = batteryScoreId;
    }

    public int getBatteryScoreTypeId() {
        return batteryScoreTypeId;
    }

    public void setBatteryScoreTypeId(int batteryScoreTypeId) {
        this.batteryScoreTypeId = batteryScoreTypeId;
    }

    public float getRawScore() {
        return rawScore;
    }

    public void setRawScore(float rawScore) {
        this.rawScore = rawScore;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public int getScoreCategoryId() {
        return scoreCategoryId;
    }

    public void setScoreCategoryId(int scoreCategoryId) {
        this.scoreCategoryId = scoreCategoryId;
    }

    public int getScoreFormatTypeId() {
        return scoreFormatTypeId;
    }

    public void setScoreFormatTypeId(int scoreFormatTypeId) {
        this.scoreFormatTypeId = scoreFormatTypeId;
    }

    public String getScoreText() {
        return scoreText;
    }

    public void setScoreText(String s)
    {
        if( s != null && s.isEmpty() )
            s = null;

        this.scoreText = s;
    }

    @Override
    public String getErrorTxt() {
        return errorTxt;
    }

    @Override
    public void setErrorTxt(String t) {

        if( t != null && t.isEmpty() )
            t = null;

        this.errorTxt = t;
    }

    @Override
    public void appendErrorTxt( String t )
    {
        if( t == null )
            return;

        if( errorTxt == null )
            errorTxt = t;

        else if( t != null )
            errorTxt = t + "\n" + errorTxt;

        if( errorTxt != null && errorTxt.length()>1000 )
            errorTxt = errorTxt.substring(0,1000 );
    }

    public int getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(int batteryId) {
        this.batteryId = batteryId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    //public float getPercentile() {
    //    return percentile;
    //}

    //public void setPercentile(float percentile) {
    //    this.percentile = percentile;
    //}

    public int getBatteryScoreStatusTypeId() {
        return batteryScoreStatusTypeId;
    }

    public void setBatteryScoreStatusTypeId(int batteryScoreStatusTypeId) {
        this.batteryScoreStatusTypeId = batteryScoreStatusTypeId;
    }

    public float getAccountPercentile() {
        return accountPercentile;
    }

    public void setAccountPercentile(float accountPercentile) {
        this.accountPercentile = accountPercentile;
    }

    public float getCountryPercentile() {
        return countryPercentile;
    }

    public void setCountryPercentile(float countryPercentile) {
        this.countryPercentile = countryPercentile;
    }

    public int getExcludeFmNorms() {
        return excludeFmNorms;
    }

    public void setExcludeFmNorms(int excludeFmNorms) {
        this.excludeFmNorms = excludeFmNorms;
    }

    public int getAccountPercentileCount() {
        return accountPercentileCount;
    }

    public void setAccountPercentileCount(int accountPercentileCount) {
        this.accountPercentileCount = accountPercentileCount;
    }

    public int getCountryPercentileCount() {
        return countryPercentileCount;
    }

    public void setCountryPercentileCount(int countryPercentileCount) {
        this.countryPercentileCount = countryPercentileCount;
    }



}
