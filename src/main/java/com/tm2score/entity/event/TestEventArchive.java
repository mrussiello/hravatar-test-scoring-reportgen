package com.tm2score.entity.event;

import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.user.User;
import com.tm2score.event.TestEventStatusType;
import java.io.Serializable;

import java.util.Date;
import jakarta.persistence.Cacheable;

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


@Cacheable
@Entity
@Table( name = "testeventarchive" )
@NamedQueries( {
        @NamedQuery( name = "TestEventArchive.findByTestKeyId", query = "SELECT o FROM TestEventArchive AS o WHERE o.testKeyId=:testKeyId" ),
        @NamedQuery( name = "TestEventArchive.findByTestEventId", query = "SELECT o FROM TestEventArchive AS o WHERE o.testEventId=:testEventId" ),
        @NamedQuery( name = "TestEventArchive.findByNotUserNotEventIdOrgIdProductIdIpAddressDate", query = "SELECT o FROM TestEventArchive AS o WHERE o.testEventId<>:notTestEventId AND o.userId<>:notUserId AND o.orgId=:orgId AND o.productId=:productId AND o.ipAddress=:ipAddress AND o.lastAccessDate>=:minLastAccessDate AND o.lastAccessDate<=:maxLastAccessDate ORDER BY o.lastAccessDate DESC" ),
        @NamedQuery( name = "TestEventArchive.findRecentByUserIdAndOrgId", query = "SELECT o FROM TestEventArchive AS o WHERE o.orgId=:orgId AND o.userId=:userId ORDER BY o.lastAccessDate DESC" )
} )
public class TestEventArchive implements Serializable, Comparable<TestEventArchive>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testeventarchiveid" )
    private long testEventArchiveId;

    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "testeventstatustypeid" )
    private int testEventStatusTypeId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "batteryid" )
    private int batteryId;

    @Column( name = "productid" )
    private int productId;

    @Column( name = "pin" )
    private String pin;

    @Column( name = "producttypeid" )
    private int productTypeId;

    @Column( name = "simid" )
    private long simId = 0;

    @Column( name = "simversionid" )
    private int simVersionId = 0;

    @Column(name="reportid")
    private long reportId = 0;

    @Column( name = "creditid" )
    private long creditId;

    @Column( name = "creditsused" )
    private int creditsUsed;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;

    @Column( name = "userid" )
    private long userId = 0;

    @Column( name = "corpid" )
    private int corpId;

    @Column( name = "skinid" )
    private int skinId;

    @Column( name = "lang" )
    private String localeStr;

    @Column( name = "langreport" )
    private String localeStrReport;

    @Column( name = "overallscore" )
    private float overallScore;

    @Column( name = "overallpercentile" )
    private float overallPercentile;

    @Column( name = "accountpercentile" )
    private float accountPercentile = -1;

    @Column( name = "countrypercentile" )
    private float countryPercentile = -1;

    @Column( name = "overallpercentilecount" )
    private int overallPercentileCount;

    @Column( name = "accountpercentilecount" )
    private int accountPercentileCount;

    @Column( name = "countrypercentilecount" )
    private int countryPercentileCount;

    @Column(name="percentilecountry")
    private String percentileCountry;

    @Column( name = "excludefmnorms" )
    private int excludeFmNorms;



    @Column( name = "scoreformattypeid" )
    private int scoreFormatTypeId;

    @Column( name = "scorecolorschemetypeid" )
    private int scoreColorSchemeTypeId;
    
    @Column( name = "overallrating" )
    private int overallRating;

    @Column( name = "resultxml" )
    private String resultXml;

    @Column( name = "percentcomplete" )
    private float percentComplete = 0;

    @Column( name = "proctoruserid" )
    private long proctorUserId;

    @Column( name = "releasecode" )
    private int releaseCode;

    @Column( name = "expertypeid" )
    private int experTypeId;

    @Column( name = "eductypeid" )
    private int educTypeId;

    @Column( name = "traintypeid" )
    private int trainTypeId;

    @Column( name = "useragentid" )
    private int userAgentId;

    @Column( name = "useragent" )
    private String userAgent;

    @Column( name = "ipaddress" )
    private String ipAddress;

    @Column( name = "ipcountry" )
    private String ipCountry;

    @Column( name = "ipState" )
    private String ipState;

    @Column( name = "ipcity" )
    private String ipCity;

    @Column(name="geographicregionid")
    private int geographicRegionId = 0;

    @Column(name="extref")
    private String extRef;

    @Column( name = "thirdpartytesteventid" )
    private String thirdPartyTestEventId;

    @Column( name = "thirdpartytestaccountid" )
    private String thirdPartyTestAccountId;

    @Column(name="errortxt")
    private String errorTxt;

    @Column(name="textstr1")
    private String textStr1;

    @Column(name="totaltesttime")
    private float totalTestTime;

    @Column(name="stdhrascoring")
    private int stdHraScoring;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="startdate")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastaccessdate")
    private Date lastAccessDate;

    @Transient
    private TestKey testKey;

    @Transient
    private Product product;

    @Transient
    private User user;



    public TestEvent getTestEvent()
    {
        TestEvent te = new TestEvent();

        te.setLastAccessDate(lastAccessDate);
        te.setLocaleStr(localeStr);
        te.setLocaleStrReport(localeStrReport);
        te.setCorpId(corpId);
        te.setSkinId(skinId);
        te.setOrgId(orgId);
        te.setSuborgId(suborgId);
        te.setOverallRating(overallRating);
        te.setOverallScore(overallScore);
        te.setOverallPercentile(overallPercentile);
        te.setAccountPercentile(accountPercentile);
        te.setCountryPercentile(countryPercentile);
        te.setOverallPercentileCount(overallPercentileCount);
        te.setAccountPercentileCount(accountPercentileCount);
        te.setCountryPercentileCount(countryPercentileCount);
        te.setPercentileCountry(percentileCountry);
        te.setExcludeFmNorms(excludeFmNorms);
        te.setScoreFormatTypeId(scoreFormatTypeId);
        te.setScoreColorSchemeTypeId( scoreColorSchemeTypeId );
        te.setPercentComplete(percentComplete);
        te.setBatteryId( batteryId );
        te.setReportId(reportId);
        te.setCreditId(creditId);
        te.setCreditsUsed(creditsUsed);
        te.setProductId(productId);
        te.setProductTypeId(productTypeId);
        te.setPin(pin);
        te.setSimId(simId);
        te.setSimVersionId(simVersionId);
        te.setResultXml(resultXml );
        te.setStartDate(startDate);
        te.setTestEventArchiveId( testEventArchiveId );
        te.setTestEventId(testEventId);
        te.setTestEventStatusTypeId(testEventStatusTypeId);
        te.setTestKeyId(testKeyId);
        te.setUserId(userId);
        te.setProctorUserId(proctorUserId);
        te.setReleaseCode(releaseCode);
        te.setExtRef(extRef);
        te.setErrorTxt( errorTxt );
        te.setEducTypeId(educTypeId);
        te.setExperTypeId(experTypeId);
        te.setTrainTypeId(trainTypeId);

        te.setUserAgentId(userAgentId);
        te.setUserAgent(userAgent);
        te.setIpAddress(ipAddress);
        te.setIpCountry(ipCountry);
        te.setIpState(ipState);
        te.setIpCity(ipCity);
        te.setGeographicRegionId(geographicRegionId);
        te.setTextStr1(textStr1 );

        te.setThirdPartyTestEventId(thirdPartyTestEventId);
        te.setThirdPartyTestAccountId(thirdPartyTestAccountId);
        te.setTotalTestTime(totalTestTime);
        te.setStdHraScoring(stdHraScoring);

        return te;
    }


    public TestEventStatusType getTestEventStatusType()
    {
        return TestEventStatusType.getValue( testEventStatusTypeId );
    }

    @Override
    public int compareTo(TestEventArchive o) {

        if( startDate != null && o.getStartDate() != null )
            return startDate.compareTo( o.getStartDate() );

        return new Long( testEventId ).compareTo( new Long( o.getTestEventId() ) );
    }



    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestEventArchive other = (TestEventArchive) obj;
        if (this.testEventArchiveId != other.testEventArchiveId) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.testEventArchiveId ^ (this.testEventArchiveId >>> 32));
        hash = 97 * hash + (int) (this.testEventId ^ (this.testEventId >>> 32));
        return hash;
    }



    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public int getTestEventStatusTypeId() {
        return testEventStatusTypeId;
    }

    public void setTestEventStatusTypeId(int testEventStatusTypeId) {
        this.testEventStatusTypeId = testEventStatusTypeId;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TestKey getTestKey() {
        return testKey;
    }

    public void setTestKey(TestKey testKey) {
        this.testKey = testKey;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public float getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    public int getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(int overallRating) {
        this.overallRating = overallRating;
    }

    public float getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(float overallScore) {
        this.overallScore = overallScore;
    }

    public String getResultXml() {
        return resultXml;
    }

    public void setResultXml(String r) {

        if( r != null && r.trim().length() == 0 )
            r = null;

        this.resultXml = r;
    }

    public long getTestEventArchiveId() {
        return testEventArchiveId;
    }

    public void setTestEventArchiveId(long testEventArchiveId) {
        this.testEventArchiveId = testEventArchiveId;
    }

    public long getProctorUserId() {
        return proctorUserId;
    }

    public void setProctorUserId(long proctorUserId) {
        this.proctorUserId = proctorUserId;
    }

    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
    }

    public int getSkinId() {
        return skinId;
    }

    public void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public int getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(int batteryId) {
        this.batteryId = batteryId;
    }

    public long getCreditId() {
        return creditId;
    }

    public void setCreditId(long creditId) {
        this.creditId = creditId;
    }

    public int getReleaseCode() {
        return releaseCode;
    }

    public void setReleaseCode(int releaseCode) {
        this.releaseCode = releaseCode;
    }

    public String getExtRef() {
        return extRef;
    }

    public void setExtRef(String extRef) {
        this.extRef = extRef;
    }

    public int getScoreFormatTypeId() {
        return scoreFormatTypeId;
    }

    public void setScoreFormatTypeId(int scoreFormatTypeId) {
        this.scoreFormatTypeId = scoreFormatTypeId;
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

    public String getErrorTxt() {
        return errorTxt;
    }

    public void setErrorTxt(String errorTxt) {
        this.errorTxt = errorTxt;
    }

    public int getEducTypeId() {
        return educTypeId;
    }

    public void setEducTypeId(int educTypeId) {
        this.educTypeId = educTypeId;
    }

    public int getExperTypeId() {
        return experTypeId;
    }

    public void setExperTypeId(int experTypeId) {
        this.experTypeId = experTypeId;
    }

    public int getTrainTypeId() {
        return trainTypeId;
    }

    public void setTrainTypeId(int trainTypeId) {
        this.trainTypeId = trainTypeId;
    }

    public int getSuborgId()
    {
        return suborgId;
    }

    public void setSuborgId(int suborgId)
    {
        this.suborgId = suborgId;
    }

    public int getCreditsUsed()
    {
        return creditsUsed;
    }

    public void setCreditsUsed(int creditsUsed)
    {
        this.creditsUsed = creditsUsed;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(int productTypeId) {
        this.productTypeId = productTypeId;
    }

    public int getUserAgentId() {
        return userAgentId;
    }

    public void setUserAgentId(int userAgentId) {
        this.userAgentId = userAgentId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpCountry() {
        return ipCountry;
    }

    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public String getIpState() {
        return ipState;
    }

    public void setIpState(String ipState) {
        this.ipState = ipState;
    }

    public String getIpCity() {
        return ipCity;
    }

    public void setIpCity(String ipCity) {
        this.ipCity = ipCity;
    }

    public int getGeographicRegionId() {
        return geographicRegionId;
    }

    public void setGeographicRegionId(int geographicRegionId) {
        this.geographicRegionId = geographicRegionId;
    }


    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public float getOverallPercentile() {
        return overallPercentile;
    }

    public void setOverallPercentile(float overallPercentile) {
        this.overallPercentile = overallPercentile;
    }

    public String getTextStr1() {
        return textStr1;
    }

    public void setTextStr1(String textStr1) {
        this.textStr1 = textStr1;
    }

    public float getTotalTestTime() {
        return totalTestTime;
    }

    public void setTotalTestTime(float totalTestTime) {
        this.totalTestTime = totalTestTime;
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

    public int getOverallPercentileCount() {
        return overallPercentileCount;
    }

    public void setOverallPercentileCount(int overallPercentileCount) {
        this.overallPercentileCount = overallPercentileCount;
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

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getExcludeFmNorms() {
        return excludeFmNorms;
    }

    public void setExcludeFmNorms(int excludeFmNorms) {
        this.excludeFmNorms = excludeFmNorms;
    }

    public String getLocaleStrReport() {
        return localeStrReport;
    }

    public void setLocaleStrReport(String localeStrReport) {
        this.localeStrReport = localeStrReport;
    }

    public String getThirdPartyTestEventId() {
        return thirdPartyTestEventId;
    }

    public void setThirdPartyTestEventId(String thirdPartyTestEventId) {
        this.thirdPartyTestEventId = thirdPartyTestEventId;
    }

    public String getThirdPartyTestAccountId() {
        return thirdPartyTestAccountId;
    }

    public void setThirdPartyTestAccountId(String thirdPartyTestAccountId) {
        this.thirdPartyTestAccountId = thirdPartyTestAccountId;
    }

    public String getPercentileCountry() {
        return percentileCountry;
    }

    public void setPercentileCountry(String percentileCountry) {
        this.percentileCountry = percentileCountry;
    }

    public int getScoreColorSchemeTypeId() {
        return scoreColorSchemeTypeId;
    }

    public void setScoreColorSchemeTypeId(int scoreColorSchemeTypeId) {
        this.scoreColorSchemeTypeId = scoreColorSchemeTypeId;
    }

    public int getStdHraScoring() {
        return stdHraScoring;
    }

    public void setStdHraScoring(int stdHraScoring) {
        this.stdHraScoring = stdHraScoring;
    }

}
