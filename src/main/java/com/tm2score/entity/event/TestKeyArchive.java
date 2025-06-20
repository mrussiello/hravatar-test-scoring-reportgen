package com.tm2score.entity.event;

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
@Table( name = "testkeyarchive" )
@NamedQueries( {
        @NamedQuery( name = "TestKeyArchive.findByUserAndCorpId", query = "SELECT o FROM TestKeyArchive AS o WHERE o.userId=:userId AND o.corpId=:corpId" ),
        @NamedQuery ( name="TestKeyArchive.findByOrgAndExtRef", query="SELECT o FROM TestKeyArchive AS o  WHERE o.orgId = :orgId AND o.extRef=:extRef" ),
        @NamedQuery( name = "TestKeyArchive.findByTestKeyId", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyId=:testKeyId" ),
        @NamedQuery( name = "TestKeyArchive.findByStatusInAccessOrder", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId ORDER BY o.lastAccessDate" ),
        @NamedQuery( name = "TestKeyArchive.findByStatusInAccessOrderSkipOrgs", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId AND o.orgId NOT IN :orgIdsToSkipList ORDER BY o.lastAccessDate" ),
        @NamedQuery( name = "TestKeyArchive.findByStatusAndMaxErrorsInAccessOrder", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId AND o.errorCnt<=:maxErrors ORDER BY o.lastAccessDate" ),
        @NamedQuery( name = "TestKeyArchive.findByCompleteStatusInAccessOrder", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyStatusTypeId IN (100,101) ORDER BY o.lastAccessDate" ),
        @NamedQuery( name = "TestKeyArchive.findByStatusAndMaxErrorsInAccessOrderSkipOrgs", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId AND o.errorCnt<=:maxErrors AND o.orgId NOT IN :orgIdsToSkipList ORDER BY o.lastAccessDate" ),
        @NamedQuery( name = "TestKeyArchive.findByCompleteStatusInAccessOrderSkipOrgs", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyStatusTypeId IN (100,101) AND o.orgId NOT IN :orgIdsToSkipList ORDER BY o.lastAccessDate" )


} )
public class TestKeyArchive implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testkeyarchiveid" )
    private long testKeyArchiveId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "pin" )
    private String pin;

    @Column( name = "pinsave" )
    private String pinsave;

    @Column( name = "statustypeid" )
    private int testKeyStatusTypeId;

    @Column( name = "testkeyauthtypeid" )
    private int testKeyAuthTypeId;

    @Column( name = "orgautotestid" )
    private int orgAutoTestId;

    @Column( name = "testkeyproctortypeid" )
    private int testKeyProctorTypeId;

    @Column(name="testkeyadmintypeid")
    private int testKeyAdminTypeId;

    @Column(name="testkeysourcetypeid")
    private int testKeySourceTypeId;

    @Column(name="apitypeid")
    private int apiTypeId;

    @Column( name = "mediadeliverymodetypeid" )
    private int mediaDeliveryModeTypeId = 0;

    @Column( name = "orderid" )
    private long orderId;

    @Column( name = "orderitemid" )
    private long orderItemId;

    @Column( name = "creditid" )
    private long creditId;

    @Column( name = "creditindex" )
    private int creditIndex;

    @Column( name = "batteryid" )
    private int batteryId;

    @Column( name = "productid" )
    private int productId;

    @Column( name = "producttypeid" )
    private int productTypeId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;

    @Column( name = "skinid" )
    private int skinId;

    @Column( name = "corpid" )
    private int corpId;

    @Column( name = "userid" )
    private long userId = 0;

    @Column( name = "jobid" )
    private int jobId;

    @Column( name = "authorizinguserid" )
    private long authorizingUserId = 0;

    @Column(name="frccountry")
    private String frcCountry;
    
    
    @Column( name = "lang" )
    private String localeStr;

    @Column( name = "langreport" )
    private String localeStrReport;

    @Column( name = "namerqd" )
    private int nameRqd;

    @Column( name = "demorqd" )
    private int demoRqd;

    @Column( name= "releaserqd" )
    private int releaseRqd=0;

    @Column( name = "releasecode" )
    private int releaseCode;

    @Column(name="extref")
    private String extRef;

    @Column(name="firstdistcomplete")
    private int firstDistComplete;

    //@Column( name = "reportid" )
    //private long reportId;

    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;

    @Column(name="returnurl")
    private String returnUrl;

    @Column(name="errorreturnurl")
    private String errorReturnUrl;

    @Column(name="emailcandidateok")
    private int emailCandidateOk;

    @Column(name="emaillogomessageok")
    private int emailLogoMessageOk;

    @Column(name="emailonettasklistok")
    private int enhancedAccessibilityTypeId;

    @Column( name = "includeresume" )
    private int includeResume;
    
    @Column(name="emailactivitylistok")
    private int emailActivityListOk;

    @Column(name="emailoverallscoresok")
    private int emailOverallScoresOk;

    @Column(name="emailcompetencyscoresok")
    private int emailCompetencyScoresOk;

    @Column(name="emailtaskscoresok")
    private int emailTaskScoresOk;

    @Column(name="emailaltscoresok")
    private int emailAltScoresOk;

    @Column(name="customemailmessagetext")
    private String customEmailMessageText;

    @Column(name="tempnameemail")
    private String tempNameEmail;

    @Column(name="resultposturl")
    private String resultPostUrl;

    @Column(name="resultposttypeid")
    private int resultPostTypeId;

    @Column(name="requestaccessible")
    private int requestAccessible;

    @Column(name="customparameters")
    private String customParameters;

    @Column(name="reminderdays")
    private int reminderDays;

    @Column(name="cumseconds")
    private int cumSeconds;

    @Column(name="errortxt")
    private String errorTxt;

    @Column(name="errorcnt")
    private int errorCnt;

    
    @Column(name="custom1")
    private String custom1;

    @Column(name="custom2")
    private String custom2;

    @Column(name="custom3")
    private String custom3;

    /**
     * If>0, send warning X days prior to expiration
     */
    @Column(name="expirewarndays")
    private int expireWarnDays;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="startdate")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="expiredate")
    private Date expireDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="firstaccessdate")
    private Date firstAccessDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastaccessdate")
    private Date lastAccessDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="sendstartdate")
    private Date sendStartDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastemaildate")
    private Date lastEmailDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lasttextdate")
    private Date lastTextDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastexpirewarningdate")
    private Date lastExpireWarningDate;



    public TestKey getTestKey()
    {
        TestKey tk = new TestKey();

        tk.setTestKeyArchiveId(testKeyArchiveId);
        tk.setAuthorizingUserId(authorizingUserId);
        tk.setCorpId(corpId);
        tk.setExpireDate(expireDate);
        tk.setExpireWarnDays( expireWarnDays );
        tk.setLastExpireWarningDate(lastExpireWarningDate);
        tk.setFirstAccessDate(firstAccessDate);
        tk.setLastAccessDate(lastAccessDate);
        tk.setLocaleStr(localeStr);
        tk.setLocaleStrReport(localeStrReport);
        tk.setOrgId(orgId);
        tk.setOrderId(orderId);
        tk.setOrderItemId(orderItemId);
        tk.setCreditId(creditId);
        tk.setCreditIndex(creditIndex);
        tk.setBatteryId( batteryId );
        tk.setProductId(productId);
        tk.setProductTypeId(productTypeId);
        tk.setSkinId(skinId);
        tk.setStartDate(startDate);
        tk.setSuborgId(suborgId);
        tk.setTestKeyId(testKeyId);
        tk.setOrgAutoTestId(orgAutoTestId);
        tk.setTestKeyStatusTypeId(testKeyStatusTypeId);
        tk.setTestKeyAuthTypeId(testKeyAuthTypeId);
        tk.setTestKeyProctorTypeId(testKeyProctorTypeId);
        tk.setTestKeyAdminTypeId(testKeyAdminTypeId);
        tk.setTestKeySourceTypeId(testKeySourceTypeId);
        tk.setApiTypeId(apiTypeId);
        tk.setMediaDeliveryModeTypeId(mediaDeliveryModeTypeId);
        tk.setUserId(userId);
        tk.setJobId(jobId);
        tk.setNameRqd(nameRqd);
        tk.setDemoRqd(demoRqd);
        tk.setReleaseRqd(releaseRqd);
        tk.setReleaseCode(releaseCode);
        tk.setExtRef(extRef);
        tk.setFirstDistComplete(firstDistComplete);
        tk.setErrorTxt(errorTxt);
        tk.setErrorCnt(errorCnt);
        tk.setEmailResultsTo(emailResultsTo);
        tk.setTextResultsTo(textResultsTo);
        tk.setTempNameEmail(tempNameEmail);
        tk.setFrcCountry(frcCountry);

        tk.setReturnUrl(returnUrl);
        tk.setErrorReturnUrl(errorReturnUrl);
        tk.setEmailCandidateOk(emailCandidateOk);
        tk.setEmailLogoMessageOk(emailLogoMessageOk);
        tk.setEmailActivityListOk(emailActivityListOk);
        tk.setEnhancedAccessibilityTypeId(enhancedAccessibilityTypeId);
        tk.setIncludeResume(includeResume);
        tk.setEmailOverallScoresOk(emailOverallScoresOk);
        tk.setEmailCompetencyScoresOk(emailCompetencyScoresOk);
        tk.setEmailTaskScoresOk(emailTaskScoresOk);
        tk.setEmailAltScoresOk(emailAltScoresOk);
        tk.setCustomEmailMessageText(customEmailMessageText);

        tk.setResultPostUrl( resultPostUrl );
        tk.setResultPostTypeId( resultPostTypeId );

        tk.setSendStartDate(sendStartDate);
        tk.setLastEmailDate(lastEmailDate);
        tk.setLastTextDate(lastTextDate);

        tk.setRequestAccessible( requestAccessible );
        tk.setCustomParameters(customParameters);
        tk.setCustom1( custom1 );
        tk.setCustom2( custom2 );
        tk.setCustom3( custom3 );
        tk.setReminderDays( reminderDays );
        tk.setCumSeconds( cumSeconds );

        tk.setPinsave(pinsave);

        tk.setTestKeyArchive( this );

        return tk;
    }


    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestKeyArchive other = (TestKeyArchive) obj;
        if (this.testKeyArchiveId != other.testKeyArchiveId) {
            return false;
        }
        return true;
    }

    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
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

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(int productTypeId) {
        this.productTypeId = productTypeId;
    }

    public int getSkinId() {
        return skinId;
    }

    public void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int s) {
        this.suborgId = s;
    }

    public long getTestKeyArchiveId() {
        return testKeyArchiveId;
    }

    public void setTestKeyArchiveId(long testKeyArchiveId) {
        this.testKeyArchiveId = testKeyArchiveId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public int getTestKeyStatusTypeId() {
        return testKeyStatusTypeId;
    }

    public void setTestKeyStatusTypeId(int testKeyStatusTypeId) {
        this.testKeyStatusTypeId = testKeyStatusTypeId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getTestKeyAuthTypeId() {
        return testKeyAuthTypeId;
    }

    public void setTestKeyAuthTypeId(int testKeyAuthTypeId) {
        this.testKeyAuthTypeId = testKeyAuthTypeId;
    }

    public int getTestKeyProctorTypeId() {
        return testKeyProctorTypeId;
    }

    public void setTestKeyProctorTypeId(int testKeyProctorTypeId) {
        this.testKeyProctorTypeId = testKeyProctorTypeId;
    }

    public long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public int getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(int batteryId) {
        this.batteryId = batteryId;
    }

    public int getDemoRqd() {
        return demoRqd;
    }

    public void setDemoRqd(int demoRqd) {
        this.demoRqd = demoRqd;
    }

    public int getNameRqd() {
        return nameRqd;
    }

    public void setNameRqd(int nameRqd) {
        this.nameRqd = nameRqd;
    }

    public int getReleaseRqd() {
        return releaseRqd;
    }

    public void setReleaseRqd(int releaseRqd) {
        this.releaseRqd = releaseRqd;
    }

    public String getExtRef() {
        return extRef;
    }

    public void setExtRef(String extRef) {
        this.extRef = extRef;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public int getReleaseCode() {
        return releaseCode;
    }

    public void setReleaseCode(int releaseCode) {
        this.releaseCode = releaseCode;
    }

    //public long getReportId() {
    //    return reportId;
    //}

   // public void setReportId(long reportId) {
    //    this.reportId = reportId;
    //}

    public String getErrorTxt() {
        return errorTxt;
    }

    public void setErrorTxt(String errorTxt) {
        
        if( errorTxt!=null && errorTxt.length()>1990 )
            errorTxt=com.tm2score.util.StringUtils.truncateStringFromFront(errorTxt, 1990 );
        
        this.errorTxt = errorTxt;
    }

    public String getEmailResultsTo() {
        return emailResultsTo;
    }

    public void setEmailResultsTo(String emailResultsTo) {
        this.emailResultsTo = emailResultsTo;
    }

    public String getTextResultsTo() {
        return textResultsTo;
    }

    public void setTextResultsTo(String textResultsTo) {
        this.textResultsTo = textResultsTo;
    }

    public long getAuthorizingUserId() {
        return authorizingUserId;
    }

    public void setAuthorizingUserId(long authorizingUserId) {
        this.authorizingUserId = authorizingUserId;
    }

    public int getEmailCandidateOk() {
        return emailCandidateOk;
    }

    public void setEmailCandidateOk(int emailCandidateOk) {
        this.emailCandidateOk = emailCandidateOk;
    }

    public int getEmailLogoMessageOk() {
        return emailLogoMessageOk;
    }

    public void setEmailLogoMessageOk(int emailLogoMessageOk) {
        this.emailLogoMessageOk = emailLogoMessageOk;
    }

    public int getEnhancedAccessibilityTypeId() {
        return enhancedAccessibilityTypeId;
    }

    public void setEnhancedAccessibilityTypeId(int i) {
        this.enhancedAccessibilityTypeId = i;
    }

    public int getEmailActivityListOk() {
        return emailActivityListOk;
    }

    public void setEmailActivityListOk(int emailActivityListOk) {
        this.emailActivityListOk = emailActivityListOk;
    }

    public String getCustomEmailMessageText() {
        return customEmailMessageText;
    }

    public void setCustomEmailMessageText(String customEmailMessageText) {
        this.customEmailMessageText = customEmailMessageText;
    }

    public Date getFirstAccessDate() {
        return firstAccessDate;
    }

    public void setFirstAccessDate(Date firstAccessDate) {
        this.firstAccessDate = firstAccessDate;
    }


    public String getTempNameEmail() {
        return tempNameEmail;
    }

    public void setTempNameEmail(String tempNameEmail) {
        this.tempNameEmail = tempNameEmail;
    }

    public int getTestKeyAdminTypeId() {
        return testKeyAdminTypeId;
    }

    public void setTestKeyAdminTypeId(int testKeyAdminTypeId) {
        this.testKeyAdminTypeId = testKeyAdminTypeId;
    }

    public int getEmailOverallScoresOk() {
        return emailOverallScoresOk;
    }

    public void setEmailOverallScoresOk(int emailOverallScoresOk) {
        this.emailOverallScoresOk = emailOverallScoresOk;
    }

    public int getEmailCompetencyScoresOk() {
        return emailCompetencyScoresOk;
    }

    public void setEmailCompetencyScoresOk(int emailCompetencyScoresOk) {
        this.emailCompetencyScoresOk = emailCompetencyScoresOk;
    }

    public int getEmailTaskScoresOk() {
        return emailTaskScoresOk;
    }

    public void setEmailTaskScoresOk(int emailTaskScoresOk) {
        this.emailTaskScoresOk = emailTaskScoresOk;
    }

    public int getEmailAltScoresOk() {
        return emailAltScoresOk;
    }

    public void setEmailAltScoresOk(int emailAltScoresOk) {
        this.emailAltScoresOk = emailAltScoresOk;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public int getFirstDistComplete() {
        return firstDistComplete;
    }

    public void setFirstDistComplete(int firstdistcomplete) {
        this.firstDistComplete = firstdistcomplete;
    }

    public String getResultPostUrl() {
        return resultPostUrl;
    }

    public void setResultPostUrl(String resultPostUrl) {
        this.resultPostUrl = resultPostUrl;
    }

    public int getResultPostTypeId() {
        return resultPostTypeId;
    }

    public void setResultPostTypeId(int resultPostTypeId) {
        this.resultPostTypeId = resultPostTypeId;
    }

    public Date getLastEmailDate() {
        return lastEmailDate;
    }

    public void setLastEmailDate(Date lastEmailDate) {
        this.lastEmailDate = lastEmailDate;
    }

    public Date getLastTextDate() {
        return lastTextDate;
    }

    public void setLastTextDate(Date lastTextDate) {
        this.lastTextDate = lastTextDate;
    }
    public String getLocaleStrReport() {
        return localeStrReport;
    }

    public void setLocaleStrReport(String localeStrReport) {
        this.localeStrReport = localeStrReport;
    }

    public int getRequestAccessible() {
        return requestAccessible;
    }

    public void setRequestAccessible(int requestAccessible) {
        this.requestAccessible = requestAccessible;
    }

    public String getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }

    public int getTestKeySourceTypeId() {
        return testKeySourceTypeId;
    }

    public void setTestKeySourceTypeId(int testKeySourceTypeId) {
        this.testKeySourceTypeId = testKeySourceTypeId;
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

    public int getMediaDeliveryModeTypeId() {
        return mediaDeliveryModeTypeId;
    }

    public void setMediaDeliveryModeTypeId(int mediaDeliveryModeTypeId) {
        this.mediaDeliveryModeTypeId = mediaDeliveryModeTypeId;
    }

    public String getErrorReturnUrl() {
        return errorReturnUrl;
    }

    public void setErrorReturnUrl(String errorReturnUrl) {
        this.errorReturnUrl = errorReturnUrl;
    }

    public String getPinsave() {
        return pinsave;
    }

    public void setPinsave(String pinsave) {
        this.pinsave = pinsave;
    }

    public int getApiTypeId() {
        return apiTypeId;
    }

    public void setApiTypeId(int apiTypeId) {
        this.apiTypeId = apiTypeId;
    }

    public int getOrgAutoTestId() {
        return orgAutoTestId;
    }

    public void setOrgAutoTestId(int orgAutoTestId) {
        this.orgAutoTestId = orgAutoTestId;
    }

    public int getReminderDays() {
        return reminderDays;
    }

    public void setReminderDays(int reminderDays) {
        this.reminderDays = reminderDays;
    }

    public String getFrcCountry() {
        return frcCountry;
    }

    public void setFrcCountry(String frcCountry) {
        this.frcCountry = frcCountry;
    }

    public long getCreditId() {
        return creditId;
    }

    public void setCreditId(long creditId) {
        this.creditId = creditId;
    }

    public int getCumSeconds() {
        return cumSeconds;
    }

    public void setCumSeconds(int cumSeconds) {
        this.cumSeconds = cumSeconds;
    }

    public int getCreditIndex() {
        return creditIndex;
    }

    public void setCreditIndex(int creditIndex) {
        this.creditIndex = creditIndex;
    }

    public int getExpireWarnDays() {
        return expireWarnDays;
    }

    public void setExpireWarnDays(int expireWarnDays) {
        this.expireWarnDays = expireWarnDays;
    }

    public Date getLastExpireWarningDate() {
        return lastExpireWarningDate;
    }

    public void setLastExpireWarningDate(Date lastExpireWarningDate) {
        this.lastExpireWarningDate = lastExpireWarningDate;
    }

    public int getErrorCnt() {
        return errorCnt;
    }

    public void setErrorCnt(int errorCnt) {
        this.errorCnt = errorCnt;
    }

    public Date getSendStartDate() {
        return sendStartDate;
    }

    public void setSendStartDate(Date sendStartDate) {
        this.sendStartDate = sendStartDate;
    }

    public int getIncludeResume() {
        return includeResume;
    }

    public void setIncludeResume(int includeResume) {
        this.includeResume = includeResume;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}
