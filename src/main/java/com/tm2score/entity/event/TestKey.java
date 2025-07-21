package com.tm2score.entity.event;

import com.tm2score.entity.proctor.ProctorEntry;
import com.tm2score.api.ApiType;
import com.tm2score.entity.ai.MetaScore;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.entity.corp.Corp;
import com.tm2score.entity.proctor.ProctorSuspension;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.entity.user.User;
import com.tm2score.event.OnlineProctoringType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.ErrorTxtObject;
import com.tm2score.json.JsonUtils;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.UserAnonymityType;
import com.tm2score.util.UrlEncodingUtils;
import java.io.Serializable;
import java.net.URLDecoder;

import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
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
import java.util.ArrayList;

@Entity
@Table(name = "testkey")
@NamedQueries(
{
    @NamedQuery(name = "TestKey.findByOrgAndExtRef", query = "SELECT o FROM TestKey AS o  WHERE o.orgId = :orgId AND o.extRef=:extRef"),
    @NamedQuery(name = "TestKey.findByPin", query = "SELECT o FROM TestKey AS o WHERE o.pin=:pin"),
    @NamedQuery(name = "TestKey.findByTestKeyId", query = "SELECT o FROM TestKey AS o WHERE o.testKeyId=:testKeyId"),
    @NamedQuery(name = "TestKey.findByStatusInAccessOrder", query = "SELECT o FROM TestKey AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId ORDER BY o.lastAccessDate"),
    @NamedQuery(name = "TestKey.findByStatusAndMaxErrorsInAccessOrder", query = "SELECT o FROM TestKey AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId AND o.errorCnt<=:maxErrors ORDER BY o.lastAccessDate"),
    @NamedQuery(name = "TestKey.findByStatusInAccessOrderSkipOrgs", query = "SELECT o FROM TestKey AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId AND o.orgId NOT IN :orgIdsToSkipList ORDER BY o.lastAccessDate"),
    @NamedQuery(name = "TestKey.findByStatusAndMaxErrorsInAccessOrderSkipOrgs", query = "SELECT o FROM TestKey AS o WHERE o.testKeyStatusTypeId=:testKeyStatusTypeId AND o.errorCnt<=:maxErrors AND o.orgId NOT IN :orgIdsToSkipList ORDER BY o.lastAccessDate")
})
public class TestKey implements Serializable, ErrorTxtObject {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testkeyid")
    private long testKeyId;

    @Column(name = "pin")
    private String pin;

    @Column(name = "statustypeid")
    private int testKeyStatusTypeId;

    @Column(name = "testkeyauthtypeid")
    private int testKeyAuthTypeId;

    @Column(name = "testkeyproctortypeid")
    private int testKeyProctorTypeId;

    @Column(name = "testkeyadmintypeid")
    private int testKeyAdminTypeId;

    @Column(name = "testkeysourcetypeid")
    private int testKeySourceTypeId;

    @Column(name = "orgautotestid")
    private int orgAutoTestId;

    @Column(name = "apitypeid")
    private int apiTypeId;

    @Column(name = "mediadeliverymodetypeid")
    private int mediaDeliveryModeTypeId = 0;

    @Column(name = "frccountry")
    private String frcCountry;

    @Column(name = "orderid")
    private long orderId;

    @Column(name = "orderitemid")
    private long orderItemId;

    @Column(name = "creditid")
    private long creditId;

    @Column(name = "creditindex")
    private int creditIndex;

    @Column(name = "batteryid")
    private int batteryId;

    @Column(name = "productid")
    private int productId;

    @Column(name = "producttypeid")
    private int productTypeId;

    @Column(name = "orgid")
    private int orgId;

    @Column(name = "suborgid")
    private int suborgId;

    @Column(name = "skinid")
    private int skinId;

    @Column(name = "corpid")
    private int corpId;

    @Column(name = "userid")
    private long userId = 0;

    @Column(name = "jobid")
    private int jobId;

    @Column(name = "authorizinguserid")
    private long authorizingUserId = 0;

    @Column(name = "namerqd")
    private int nameRqd = -1;

    @Column(name = "demorqd")
    private int demoRqd = -1;

    @Column(name = "releaserqd")
    private int releaseRqd = -1;

    @Column(name = "releasecode")
    private int releaseCode;

    @Column(name = "lang")
    private String localeStr;

    @Column(name = "langreport")
    private String localeStrReport;

    @Column(name = "emailresultsto")
    private String emailResultsTo;

    @Column(name = "textresultsto")
    private String textResultsTo;

    @Column(name = "returnurl")
    private String returnUrl;

    @Column(name = "errorreturnurl")
    private String errorReturnUrl;

    @Column(name = "extref")
    private String extRef;

    @Column(name = "errortxt")
    private String errorTxt;

    @Column(name = "errorcnt")
    private int errorCnt;

    @Column(name = "firstdistcomplete")
    private int firstDistComplete;

    @Column(name = "resultposturl")
    private String resultPostUrl;

    @Column(name = "resultposttypeid")
    private int resultPostTypeId;

    @Column(name = "customparameters")
    private String customParameters;

    //@Column( name = "reportid" )
    //private long reportId;
    @Column(name = "emailcandidateok")
    private int emailCandidateOk;

    @Column(name = "emaillogomessageok")
    private int emailLogoMessageOk;

    @Column(name = "emailonettasklistok")
    private int enhancedAccessibilityTypeId;

    @Column(name = "includeresume")
    private int includeResume;

    @Column(name = "emailactivitylistok")
    private int emailActivityListOk;

    @Column(name = "emailoverallscoresok")
    private int emailOverallScoresOk;

    @Column(name = "emailcompetencyscoresok")
    private int emailCompetencyScoresOk;

    @Column(name = "emailtaskscoresok")
    private int emailTaskScoresOk;

    @Column(name = "emailaltscoresok")
    private int emailAltScoresOk;

    @Column(name = "customemailmessagetext")
    private String customEmailMessageText;

    @Column(name = "tempnameemail")
    private String tempNameEmail;

    @Column(name = "requestaccessible")
    private int requestAccessible = -1;

    @Column(name = "custom1")
    private String custom1;

    @Column(name = "custom2")
    private String custom2;

    @Column(name = "custom3")
    private String custom3;

    @Column(name = "reminderdays")
    private int reminderDays;

    /**
     * If>0, send warning X days prior to expiration
     */
    @Column(name = "expirewarndays")
    private int expireWarnDays;

    @Column(name = "cumseconds")
    private int cumSeconds;

    @Transient
    private String pinsave;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "startdate")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiredate")
    private Date expireDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "firstaccessdate")
    private Date firstAccessDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastaccessdate")
    private Date lastAccessDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sendstartdate")
    private Date sendStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastemaildate")
    private Date lastEmailDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lasttextdate")
    private Date lastTextDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastexpirewarningdate")
    private Date lastExpireWarningDate;

    @Transient
    private TestKeyArchive testKeyArchive;

    @Transient
    private long testKeyArchiveId;

    @Transient
    private Product batteryProduct;

    @Transient
    private List<TestEvent> testEventList;

    @Transient
    private Battery battery;

    @Transient
    private BatteryScore batteryScore;

    //@Transient
    //private Report report;
    @Transient
    private Org org;

    @Transient
    private Suborg suborg;

    @Transient
    private User user;

    @Transient
    private User authUser;

    @Transient
    private Product product;

    @Transient
    private Corp corp;

    @Transient
    private String[] tempDemoVals = null;

    @Transient
    private String startUrl;

    @Transient
    private List<ProctorEntry> proctorEntryList;

    @Transient
    private List<ProctorSuspension> proctorSuspensionList;

    @Transient
    private List<MetaScore> metaScoreList;

    //@Transient
    //private HtmlScoreFormatter htmlScoreFormatter;
    @Override
    public String toString()
    {
        return "TestKey{" + "testKeyId=" + testKeyId + ", pin=" + pin + ", testKeyStatusTypeId=" + testKeyStatusTypeId + ", orgId=" + orgId + '}';
    }

    public TestKeyStatusType getTestKeyStatusType()
    {
        return TestKeyStatusType.getValue(testKeyStatusTypeId);
    }

    public TestKeyArchive getTestKeyArchive() throws Exception
    {
        //if( testKeyArchiveId <= 0 || testKeyArchive == null )
        //    throw new Exception( "TestKeyArchiveId invalid " + testKeyArchiveId );

        TestKeyArchive tka = testKeyArchive == null ? new TestKeyArchive() : testKeyArchive;

        tka.setTestKeyArchiveId(testKeyArchiveId);
        tka.setAuthorizingUserId(authorizingUserId);
        tka.setCorpId(corpId);
        tka.setExpireDate(expireDate);
        tka.setExpireWarnDays(expireWarnDays);
        tka.setLastExpireWarningDate(lastExpireWarningDate);
        tka.setFirstAccessDate(firstAccessDate);
        tka.setLastAccessDate(lastAccessDate);
        tka.setLocaleStr(localeStr);
        tka.setLocaleStrReport(localeStrReport);
        tka.setOrderId(orderId);
        tka.setOrderItemId(orderItemId);
        tka.setCreditId(creditId);
        tka.setCreditIndex(creditIndex);
        tka.setOrgId(orgId);
        tka.setBatteryId(batteryId);
        tka.setProductId(productId);
        tka.setProductTypeId(productTypeId);
        tka.setSkinId(skinId);
        tka.setStartDate(startDate);
        tka.setSuborgId(suborgId);
        tka.setTestKeyId(testKeyId);
        tka.setOrgAutoTestId(orgAutoTestId);
        tka.setTestKeyStatusTypeId(testKeyStatusTypeId);
        tka.setTestKeyAuthTypeId(testKeyAuthTypeId);
        tka.setTestKeyProctorTypeId(testKeyProctorTypeId);
        tka.setTestKeyAdminTypeId(testKeyAdminTypeId);
        tka.setTestKeySourceTypeId(testKeySourceTypeId);
        tka.setApiTypeId(apiTypeId);
        tka.setMediaDeliveryModeTypeId(mediaDeliveryModeTypeId);
        tka.setUserId(userId);
        tka.setJobId(jobId);
        tka.setNameRqd(nameRqd);
        tka.setDemoRqd(demoRqd);
        tka.setReleaseRqd(releaseRqd);
        tka.setReleaseCode(releaseCode);
        tka.setFirstDistComplete(firstDistComplete);

        tka.setExtRef(extRef);
        tka.setErrorTxt(errorTxt);
        tka.setErrorCnt(errorCnt);
        tka.setEmailResultsTo(emailResultsTo);
        tka.setTextResultsTo(textResultsTo);
        tka.setTempNameEmail(tempNameEmail);
        tka.setFrcCountry(frcCountry);

        tka.setReturnUrl(returnUrl);
        tka.setErrorReturnUrl(errorReturnUrl);
        tka.setEmailCandidateOk(emailCandidateOk);
        tka.setEmailLogoMessageOk(emailLogoMessageOk);
        tka.setEmailActivityListOk(emailActivityListOk);
        tka.setEnhancedAccessibilityTypeId(enhancedAccessibilityTypeId);
        tka.setIncludeResume(includeResume);
        tka.setEmailOverallScoresOk(emailOverallScoresOk);
        tka.setEmailCompetencyScoresOk(emailCompetencyScoresOk);
        tka.setEmailTaskScoresOk(emailTaskScoresOk);
        tka.setEmailAltScoresOk(emailAltScoresOk);

        tka.setResultPostUrl(resultPostUrl);
        tka.setResultPostTypeId(resultPostTypeId);
        tka.setRequestAccessible(requestAccessible);
        tka.setCustomParameters(customParameters);
        tka.setCustom1(custom1);
        tka.setCustom2(custom2);
        tka.setCustom3(custom3);
        tka.setReminderDays(reminderDays);
        tka.setCumSeconds(cumSeconds);

        tka.setCustomEmailMessageText(customEmailMessageText);

        tka.setSendStartDate(sendStartDate);
        tka.setLastEmailDate(lastEmailDate);
        tka.setLastTextDate(lastTextDate);

        if (pin != null && !pin.isEmpty() && (tka.getPinsave() == null || tka.getPinsave().isEmpty()))
            tka.setPinsave(pin);

        else if (pinsave != null && !pinsave.isEmpty())
            tka.setPinsave(pinsave);

        return tka;
    }

    public void populatePercentileObj(Percentile p)
    {
        p.setCustom1(custom1);
        p.setCustom2(custom2);
        p.setCustom3(custom3);
        p.setOrgId(orgId);
        p.setSuborgId(suborgId);
    }

    public MetaScore getMetaScore(int metaScoreTypeId)
    {
        if (this.metaScoreList == null)
            return null;

        for (MetaScore m : this.metaScoreList)
        {
            if (m.getMetaScoreTypeId() == metaScoreTypeId)
                return m;
        }

        return null;
    }

    public boolean getOrgCreditUsageCounted()
    {
        return creditId > 0 || requestAccessible == 2;
    }

    public void setOrgCreditUsageCounted()
    {
        requestAccessible = 2;
    }

    public int getProctoringIdCaptureTypeId()
    {
        return getIntCustomParameterValue("idcaptype");
    }

    public String getUserEmail()
    {
        if (userId > 0)
            return user == null ? null : user.getEmailEd();

        return getTempEmail();
    }

    public String getUserFirstName()
    {
        if (userId > 0)
            return user == null ? null : user.getFirstNameEd();

        return this.getTempFirstName();
    }

    public String getUserLastName()
    {
        if (userId > 0)
            return user == null ? null : user.getLastNameEd();

        return this.getTempLastName();
    }

    public String getUserAltId()
    {
        if (userId > 0)
            return user == null ? null : user.getAltIdentifier();

        return this.getTempAltId();
    }

    public String getUserMobilePhone()
    {
        if (userId > 0 && user != null)
            return user.getMobilePhoneEd();

        return getTempMobilePhone();
    }

    public UserAnonymityType getUserAnonymityType()
    {
        return UserAnonymityType.getValue(this.getAnonymityType());
    }

    public String getUserFullname()
    {
        if (userId > 0 && user != null && user.getFullname() != null && !user.getFullname().isEmpty())
            return user.getFullname();

        if (getUserAnonymityType().getHasUsernameOrUserId())
            return getTempEmail();

        User u = new User();
        u.setFirstName(this.getTempFirstName());
        u.setLastName(getTempLastName());
        return u.getFullname();
    }

    public String getTempFirstName()
    {
        initTempVals();
        return tempDemoVals[0];
    }

    public String getTempLastName()
    {
        initTempVals();
        return tempDemoVals[1];
    }

    public String getTempAltId()
    {
        initTempVals();
        return tempDemoVals[4];
    }

    public String getTempMobilePhone()
    {
        initTempVals();
        return tempDemoVals[3];
    }

    public boolean getHideMediaInReports()
    {
        String s = getCustomParameterValue("hidemediainreports");

        return s != null && s.equalsIgnoreCase("1");
    }

    public boolean getFeedbackReportOk()
    {
        String s = getCustomParameterValue("fbkkreportok");

        return s != null && s.equalsIgnoreCase("1");
    }

    public int getAnonymityType()
    {
        return getIntCustomParameterValue("anonimitytype");
    }

    public OnlineProctoringType getOnlineProctoringType()
    {
        return OnlineProctoringType.getValue(getIntCustomParameterValue("onlineproctortype"));
    }

    public int getIntCustomParameterValue(String nm)
    {
        String s = getCustomParameterValue(nm);

        if (s == null)
            return 0;

        return Integer.parseInt(s);
    }

    public String getCustomParameterValue(String name)
    {
        if (getCustomParameters() == null || getCustomParameters().isEmpty())
            return null;

        JsonObject jo = JsonUtils.getJsonObject(getCustomParameters());

        return jo.getString(name, null);
    }

    public void setIntCustomParameterValue(String nm, int val)
    {
        int x = getIntCustomParameterValue(nm);

        // if( val==0 && x==0 )
        if (val == x)
            return;

        setCustomParameterValue(nm, Integer.toString(val));
    }

    public void setCustomParameterValue(String name, String value)
    {
        JsonObject jo = getCustomParameters() == null || getCustomParameters().isEmpty() ? null : JsonUtils.getJsonObject(getCustomParameters());

        JsonObjectBuilder job = jo == null ? Json.createObjectBuilder() : JsonUtils.jsonObjectToBuilder(jo);

        job.add(name, value);

        setCustomParameters(JsonUtils.getJsonObjectAsString(job.build()));
    }

    public String getReportMediaViewUrl()
    {
        return getCustomParameterValue("rptMedVwUrl");
    }

    public boolean getOmitPdfReportFromResultsPost()
    {
        String s = getCustomParameterValue("omitPdfReportFromResultsPost");

        return s != null && s.equalsIgnoreCase("1");
    }

    public Map<String, String> getBasicAuthParmsForResultsPost()
    {
        String s = getCustomParameterValue("basicAuthParmsForResultsPost");

        if (s == null || s.trim().isEmpty())
            return null;

        String delim = ";";

        if (!s.contains(";") && s.indexOf(":") > 0)
            delim = ":";

        String[] sa = s.split(delim);

        if (sa.length < 2)
            return null;

        String un = sa[0].trim();
        String pwd = sa[1].trim();

        if (un.isEmpty() || pwd.isEmpty())
            return null;

        Map<String, String> out = new HashMap<>();

        out.put("username", un);
        out.put("password", pwd);

        return out;
    }

    public String getAssistiveTechnologyTypeOtherValue()
    {
        return getCustomParameterValue("assisttechother");
    }

    public String getAssistiveTechnologyTypeIds()
    {
        return getCustomParameterValue("assisttechtypeids");
    }

    public List<Integer> getAssistiveTechnologyTypeIdList()
    {
        List<Integer> out = new ArrayList<>();

        String s = getAssistiveTechnologyTypeIds();

        if (s == null || s.isBlank())
            return out;

        String[] pids = s.split(",");
        int id;

        for (String pid : pids)
        {
            if (pid == null || pid.isBlank())
                continue;
            pid = pid.trim();

            id = 0;

            try
            {
                id = Integer.parseInt(pid);

                // if real and not a duplicate.
                if (id > 0 && !out.contains(id))
                    out.add(id);
            } catch (NumberFormatException e)
            {
                LogService.logIt(e, "TestKey.getAssistiveTechnologyTypeIdStrList() types=" + getCustomParameterValue("assisttechtypeids") + ", pid=" + pid + ", id=" + id);
            }
        }
        return out;
    }

    public ApiType getApiType()
    {
        return ApiType.getValue(apiTypeId);
    }

    public boolean getHasResultsToEmailPersonalTestTaker()
    {
        if (getTestEventList() == null || authorizingUserId > 0 || user == null || !user.getRoleType().getIsPersonalUser())
            return false;

        for (TestEvent te : this.getTestEventList())
        {
            if (!te.getScoreFormatType().isUnscored())
                return true;
        }

        return false;
    }

    public boolean getHasReportsToEmailTestTaker()
    {
        if (getTestEventList() == null)
        {
            LogService.logIt("TestKey.getHasReportsToEmailTestTaker() testEventList is null testKeyId=" + testKeyId);
            return false;
        }

        for (TestEvent te : this.getTestEventList())
        {
            // LogService.logIt( "TestKey.getHasReportsToEmailTestTaker() BBB testEventId=" + te.getTestEventId() + ", testKeyId=" + testKeyId + ", te.report=" + (te.getReport()==null ? "null" : te.getReport().getReportId() + " , emailTT=" + te.getReport().getEmailTestTaker())  + ", te.report2=" + (te.getReport2()==null ? "null" : te.getReport2().getReportId() + " , emailTT=" + te.getReport2().getEmailTestTaker()));
            if (te.getReport() != null && te.getReport().getEmailTestTaker() == 1)
                return true;

            if (te.getReport2() != null && te.getReport2().getEmailTestTaker() == 1)
                return true;

            if (te.getReport3() != null && te.getReport3().getEmailTestTaker() == 1)
                return true;

            for (TestEventScore tes : te.getTestEventScoreList(TestEventScoreType.REPORT.getTestEventScoreTypeId()))
            {
                // LogService.logIt( "TestKey.getHasReportsToEmailTestTaker() CCC tes.getHasReport()=" + tes.getHasReport() + ", tes.getReport()=" + (tes.getReport()==null ? "null" : "not null email test taker=" + tes.getReport().getEmailTestTaker())  + ", testEventId=" + te.getTestEventId());
                if (tes.getHasReport() && tes.getReport() != null && tes.getReport().getEmailTestTaker() == 1)
                    return true;
            }
        }

        return false;
    }

    @Override
    public void appendErrorTxt(String t)
    {
        if (t == null)
            return;

        if (errorTxt == null)
            errorTxt = t;

        else if (t != null)
            errorTxt = t + "\n" + errorTxt;

        if (errorTxt != null && errorTxt.length() > 1990)
            errorTxt = errorTxt.substring(0, 1990);
    }

    public String getTempEmail()
    {
        initTempVals();
        return tempDemoVals[2];
    }

    public void setTempEmail(String s)
    {
        initTempVals();
        tempDemoVals[2] = s;
    }

    public synchronized void initTempVals()
    {
        if (tempDemoVals == null)
        {
            tempDemoVals = new String[5];

            Map<String, String> m = getTempNameMap();

            tempDemoVals[0] = m.get("fname");
            tempDemoVals[1] = m.get("lname");
            tempDemoVals[2] = m.get("email");
            tempDemoVals[3] = m.get("mobile");
            tempDemoVals[4] = m.get("altid");
        }
    }

    public synchronized Map<String, String> getTempNameMap()
    {
        Map<String, String> out = new HashMap<>();

        if (tempNameEmail == null || tempNameEmail.isEmpty())
            return out;

        try
        {

            String[] toks = tempNameEmail.split(";");

            //if( toks.length<3 )
            //   return out;
            if (toks.length > 0)
                out.put("fname", URLDecoder.decode(toks[0], "UTF8"));
            if (toks.length > 1)
                out.put("lname", URLDecoder.decode(toks[1], "UTF8"));
            if (toks.length > 2)
                out.put("email", UrlEncodingUtils.decodeKeepPlus(toks[2]));
            if (toks.length > 3)
                out.put("mobile", URLDecoder.decode(toks[3], "UTF8"));
            if (toks.length > 4)
                out.put("altid", UrlEncodingUtils.decodeKeepPlus(toks[4]));
        } catch (Exception e)
        {
            LogService.logIt(e, "TestKey.getTempNameMap() " + tempNameEmail + ", " + toString());
        }

        return out;
    }

    public int getIncludeEnglishReportValue()
    {
        String t = getCustomParameterValue("includeEnglishPdfReport");

        if (t == null || t.trim().isEmpty() || t.trim().equalsIgnoreCase("0"))
            return 0;

        try
        {
            return Integer.parseInt(t.trim());
        } catch (NumberFormatException e)
        {
            LogService.logIt(e, "TestKey.getIncludeEnglishReportValue() tk.customParameters=" + getCustomParameters() + ", " + toString());

            return 0;
        }

    }

    public int getRcScriptId()
    {
        return getIntCustomParameterValue("rcscrpid");
    }

    public boolean getOnlineProcLockedBrowser()
    {
        return getIntCustomParameterValue("pplock") == 1;
    }

    public int getBatteryId()
    {
        return batteryId;
    }

    public void setBatteryId(int batteryId)
    {
        this.batteryId = batteryId;
    }

    public int getCorpId()
    {
        return corpId;
    }

    public void setCorpId(int corpId)
    {
        this.corpId = corpId;
    }

    public int getDemoRqd()
    {
        return demoRqd;
    }

    public void setDemoRqd(int demoRqd)
    {
        this.demoRqd = demoRqd;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    public String getExtRef()
    {
        return extRef;
    }

    public void setExtRef(String extRef)
    {
        this.extRef = extRef;
    }

    public String getLocaleStr()
    {
        return localeStr;
    }

    public void setLocaleStr(String localeStr)
    {
        this.localeStr = localeStr;
    }

    public int getNameRqd()
    {
        return nameRqd;
    }

    public void setNameRqd(int nameRqd)
    {
        this.nameRqd = nameRqd;
    }

    public long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(long orderId)
    {
        this.orderId = orderId;
    }

    public long getOrderItemId()
    {
        return orderItemId;
    }

    public void setOrderItemId(long orderItemId)
    {
        this.orderItemId = orderItemId;
    }

    public int getOrgId()
    {
        return orgId;
    }

    public void setOrgId(int orgId)
    {
        this.orgId = orgId;
    }

    public String getPin()
    {
        return pin;
    }

    public void setPin(String pin)
    {
        this.pin = pin;
    }

    public int getProductId()
    {
        return productId;
    }

    public void setProductId(int productId)
    {
        this.productId = productId;
    }

    public int getProductTypeId()
    {
        return productTypeId;
    }

    public void setProductTypeId(int productTypeId)
    {
        this.productTypeId = productTypeId;
    }

    public int getReleaseCode()
    {
        return releaseCode;
    }

    public void setReleaseCode(int releaseCode)
    {
        this.releaseCode = releaseCode;
    }

    public int getReleaseRqd()
    {
        return releaseRqd;
    }

    public void setReleaseRqd(int releaseRqd)
    {
        this.releaseRqd = releaseRqd;
    }

    public int getSkinId()
    {
        return skinId;
    }

    public void setSkinId(int skinId)
    {
        this.skinId = skinId;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public int getSuborgId()
    {
        return suborgId;
    }

    public void setSuborgId(int suborgId)
    {
        this.suborgId = suborgId;
    }

    public int getTestKeyAuthTypeId()
    {
        return testKeyAuthTypeId;
    }

    public void setTestKeyAuthTypeId(int testKeyAuthTypeId)
    {
        this.testKeyAuthTypeId = testKeyAuthTypeId;
    }

    public long getTestKeyId()
    {
        return testKeyId;
    }

    public String getTestKeyIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt(testKeyId);
        } catch (Exception e)
        {
            LogService.logIt(e, "TestKey.getTestKeyIdEncrypted() " + toString());

            return "";
        }
    }

    public void setTestKeyId(long testKeyId)
    {
        this.testKeyId = testKeyId;
    }

    public int getTestKeyProctorTypeId()
    {
        return testKeyProctorTypeId;
    }

    public void setTestKeyProctorTypeId(int testKeyProctorTypeId)
    {
        this.testKeyProctorTypeId = testKeyProctorTypeId;
    }

    public int getTestKeyStatusTypeId()
    {
        return testKeyStatusTypeId;
    }

    public void setTestKeyStatusTypeId(int testKeyStatusTypeId)
    {
        this.testKeyStatusTypeId = testKeyStatusTypeId;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public Date getLastAccessDate()
    {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate)
    {
        this.lastAccessDate = lastAccessDate;
    }

    public Product getBatteryProduct()
    {
        return batteryProduct;
    }

    public void setBatteryProduct(Product batteryProduct)
    {
        this.batteryProduct = batteryProduct;
    }

    public List<TestEvent> getTestEventList()
    {
        return testEventList;
    }

    public void setTestEventList(List<TestEvent> testEventList)
    {
        this.testEventList = testEventList;
    }

    public Battery getBattery()
    {
        return battery;
    }

    public void setBattery(Battery battery)
    {
        this.battery = battery;
    }

    public BatteryScore getBatteryScore()
    {
        return batteryScore;
    }

    public void setBatteryScore(BatteryScore batteryScore)
    {
        this.batteryScore = batteryScore;
    }

    public long getTestKeyArchiveId()
    {
        return testKeyArchiveId;
    }

    public void setTestKeyArchiveId(long testKeyArchiveId)
    {
        this.testKeyArchiveId = testKeyArchiveId;
    }

    public void setTestKeyArchive(TestKeyArchive testKeyArchive)
    {
        this.testKeyArchive = testKeyArchive;
    }

    //public long getReportId() {
    //    return reportId;
    //}
    //public void setReportId(long reportId) {
    //    this.reportId = reportId;
    //}
    public Org getOrg()
    {
        return org;
    }

    public void setOrg(Org org)
    {
        this.org = org;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    @Override
    public String getErrorTxt()
    {
        return errorTxt;
    }

    public void setErrorTxt(String errorTxt)
    {

        if (errorTxt != null && errorTxt.length() > 1990)
            errorTxt = com.tm2score.util.StringUtils.truncateStringFromFront(errorTxt, 1990);

        this.errorTxt = errorTxt;
    }

    public String getEmailResultsTo()
    {
        return emailResultsTo;
    }

    public void setEmailResultsTo(String emailResultsTo)
    {
        this.emailResultsTo = emailResultsTo;
    }

    public String getTextResultsTo()
    {
        return textResultsTo;
    }

    public void setTextResultsTo(String textResultsTo)
    {
        this.textResultsTo = textResultsTo;
    }

    //public HtmlScoreFormatter getHtmlScoreFormatter() {
    //    return htmlScoreFormatter;
    //}
    //public void setHtmlScoreFormatter(HtmlScoreFormatter htmlScoreFormatter) {
    //    this.htmlScoreFormatter = htmlScoreFormatter;
    //}
    //public Report getReport() {
    //    return report;
    //}
    //public void setReport(Report report) {
    //    this.report = report;
    //}
    public long getAuthorizingUserId()
    {
        return authorizingUserId;
    }

    public void setAuthorizingUserId(long authorizingUserId)
    {
        this.authorizingUserId = authorizingUserId;
    }

    public int getEmailCandidateOk()
    {
        return emailCandidateOk;
    }

    public void setEmailCandidateOk(int emailCandidateOk)
    {
        this.emailCandidateOk = emailCandidateOk;
    }

    public int getEmailLogoMessageOk()
    {
        return emailLogoMessageOk;
    }

    public void setEmailLogoMessageOk(int emailLogoMessageOk)
    {
        this.emailLogoMessageOk = emailLogoMessageOk;
    }

    public int getEnhancedAccessibilityTypeId()
    {
        return enhancedAccessibilityTypeId;
    }

    public void setEnhancedAccessibilityTypeId(int i)
    {
        this.enhancedAccessibilityTypeId = i;
    }

    public int getEmailActivityListOk()
    {
        return emailActivityListOk;
    }

    public void setEmailActivityListOk(int emailActivityListOk)
    {
        this.emailActivityListOk = emailActivityListOk;
    }

    public String getCustomEmailMessageText()
    {
        return customEmailMessageText;
    }

    public void setCustomEmailMessageText(String customEmailMessageText)
    {
        this.customEmailMessageText = customEmailMessageText;
    }

    public Date getFirstAccessDate()
    {
        return firstAccessDate;
    }

    public void setFirstAccessDate(Date firstAccessDate)
    {
        this.firstAccessDate = firstAccessDate;
    }

    public String getTempNameEmail()
    {
        return tempNameEmail;
    }

    public void setTempNameEmail(String tempNameEmail)
    {
        this.tempNameEmail = tempNameEmail;
    }

    public int getTestKeyAdminTypeId()
    {
        return testKeyAdminTypeId;
    }

    public void setTestKeyAdminTypeId(int testKeyAdminTypeId)
    {
        this.testKeyAdminTypeId = testKeyAdminTypeId;
    }

    public int getEmailOverallScoresOk()
    {
        return emailOverallScoresOk;
    }

    public void setEmailOverallScoresOk(int emailOverallScoresOk)
    {
        this.emailOverallScoresOk = emailOverallScoresOk;
    }

    public int getEmailCompetencyScoresOk()
    {
        return emailCompetencyScoresOk;
    }

    public void setEmailCompetencyScoresOk(int emailCompetencyScoresOk)
    {
        this.emailCompetencyScoresOk = emailCompetencyScoresOk;
    }

    public int getEmailTaskScoresOk()
    {
        return emailTaskScoresOk;
    }

    public void setEmailTaskScoresOk(int emailTaskScoresOk)
    {
        this.emailTaskScoresOk = emailTaskScoresOk;
    }

    public int getEmailAltScoresOk()
    {
        return emailAltScoresOk;
    }

    public void setEmailAltScoresOk(int emailAltScoresOk)
    {
        this.emailAltScoresOk = emailAltScoresOk;
    }

    public User getAuthUser()
    {
        return authUser;
    }

    public void setAuthUser(User authUser)
    {
        this.authUser = authUser;
    }

    public Suborg getSuborg()
    {
        return suborg;
    }

    public void setSuborg(Suborg suborg)
    {
        this.suborg = suborg;
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = returnUrl;
    }

    public int getFirstDistComplete()
    {
        return firstDistComplete;
    }

    public void setFirstDistComplete(int firstdistcomplete)
    {
        this.firstDistComplete = firstdistcomplete;
    }

    public String getResultPostUrl()
    {
        return resultPostUrl;
    }

    public void setResultPostUrl(String resultPostUrl)
    {
        this.resultPostUrl = resultPostUrl;
    }

    public int getResultPostTypeId()
    {
        return resultPostTypeId;
    }

    public void setResultPostTypeId(int resultPostTypeId)
    {
        this.resultPostTypeId = resultPostTypeId;
    }

    public Date getLastEmailDate()
    {
        return lastEmailDate;
    }

    public void setLastEmailDate(Date lastEmailDate)
    {
        this.lastEmailDate = lastEmailDate;
    }

    public Date getLastTextDate()
    {
        return lastTextDate;
    }

    public void setLastTextDate(Date lastTextDate)
    {
        this.lastTextDate = lastTextDate;
    }

    public String getLocaleStrReport()
    {
        return localeStrReport;
    }

    public void setLocaleStrReport(String localeStrReport)
    {
        this.localeStrReport = localeStrReport;
    }

    public int getRequestAccessible()
    {
        return requestAccessible;
    }

    public void setRequestAccessible(int requestAccessible)
    {
        this.requestAccessible = requestAccessible;
    }

    public String getCustomParameters()
    {
        return customParameters;
    }

    public void setCustomParameters(String customParameters)
    {
        this.customParameters = customParameters;
    }

    public int getTestKeySourceTypeId()
    {
        return testKeySourceTypeId;
    }

    public void setTestKeySourceTypeId(int testKeySourceTypeId)
    {
        this.testKeySourceTypeId = testKeySourceTypeId;
    }

    public String getCustom1()
    {
        return custom1;
    }

    public void setCustom1(String custom1)
    {
        this.custom1 = custom1;
    }

    public String getCustom2()
    {
        return custom2;
    }

    public void setCustom2(String custom2)
    {
        this.custom2 = custom2;
    }

    public String getCustom3()
    {
        return custom3;
    }

    public void setCustom3(String custom3)
    {
        this.custom3 = custom3;
    }

    public int getMediaDeliveryModeTypeId()
    {
        return mediaDeliveryModeTypeId;
    }

    public void setMediaDeliveryModeTypeId(int mediaDeliveryModeTypeId)
    {
        this.mediaDeliveryModeTypeId = mediaDeliveryModeTypeId;
    }

    public String getErrorReturnUrl()
    {
        return errorReturnUrl;
    }

    public void setErrorReturnUrl(String errorReturnUrl)
    {
        this.errorReturnUrl = errorReturnUrl;
    }

    public String getPinsave()
    {
        return pinsave;
    }

    public void setPinsave(String pinsave)
    {
        this.pinsave = pinsave;
    }

    public int getApiTypeId()
    {
        return apiTypeId;
    }

    public void setApiTypeId(int apiTypeId)
    {
        this.apiTypeId = apiTypeId;
    }

    public int getOrgAutoTestId()
    {
        return orgAutoTestId;
    }

    public void setOrgAutoTestId(int orgAutoTestId)
    {
        this.orgAutoTestId = orgAutoTestId;
    }

    public int getReminderDays()
    {
        return reminderDays;
    }

    public void setReminderDays(int reminderDays)
    {
        this.reminderDays = reminderDays;
    }

    public Product getProduct()
    {
        return product;
    }

    public void setProduct(Product product)
    {
        this.product = product;
    }

    public Corp getCorp()
    {
        return corp;
    }

    public void setCorp(Corp corp)
    {
        this.corp = corp;
    }

    public String getStartUrl()
    {
        return startUrl;
    }

    public void setStartUrl(String startUrl)
    {
        this.startUrl = startUrl;
    }

    public String getFrcCountry()
    {
        return frcCountry;
    }

    public void setFrcCountry(String frcCountry)
    {
        this.frcCountry = frcCountry;
    }

    public long getCreditId()
    {
        return creditId;
    }

    public void setCreditId(long creditId)
    {
        this.creditId = creditId;
    }

    public int getCumSeconds()
    {
        return cumSeconds;
    }

    public void setCumSeconds(int cumSeconds)
    {
        this.cumSeconds = cumSeconds;
    }

    public int getCreditIndex()
    {
        return creditIndex;
    }

    public void setCreditIndex(int creditIndex)
    {
        this.creditIndex = creditIndex;
    }

    public int getExpireWarnDays()
    {
        return expireWarnDays;
    }

    public void setExpireWarnDays(int expireWarnDays)
    {
        this.expireWarnDays = expireWarnDays;
    }

    public Date getLastExpireWarningDate()
    {
        return lastExpireWarningDate;
    }

    public void setLastExpireWarningDate(Date lastExpireWarningDate)
    {
        this.lastExpireWarningDate = lastExpireWarningDate;
    }

    public int getErrorCnt()
    {
        return errorCnt;
    }

    public void setErrorCnt(int errorCnt)
    {
        this.errorCnt = errorCnt;
    }

    public Date getSendStartDate()
    {
        return sendStartDate;
    }

    public void setSendStartDate(Date sendStartDate)
    {
        this.sendStartDate = sendStartDate;
    }

    public List<ProctorEntry> getProctorEntryList()
    {
        return proctorEntryList;
    }

    public void setProctorEntryList(List<ProctorEntry> proctorEntryList)
    {
        this.proctorEntryList = proctorEntryList;
    }

    public List<ProctorSuspension> getProctorSuspensionList()
    {
        return proctorSuspensionList;
    }

    public void setProctorSuspensionList(List<ProctorSuspension> proctorSuspensionList)
    {
        this.proctorSuspensionList = proctorSuspensionList;
    }

    public int getIncludeResume()
    {
        return includeResume;
    }

    public void setIncludeResume(int includeResume)
    {
        this.includeResume = includeResume;
    }

    public List<MetaScore> getMetaScoreList()
    {

        /*
        if( 1==2 && (metaScoreList==null || metaScoreList.isEmpty()) )
        {
            int count=0;
            List<MetaScore> out = new ArrayList<>();
            MetaScore ms;
            for( MetaScoreType t : MetaScoreType.values() )
            {
                if( t.getMetaScoreTypeId()==0 )
                    continue;
                ms = new MetaScore();
                ms.setMetaScoreTypeId(t.getMetaScoreTypeId());
                ms.setScore((float) (100f*Math.random()));
                ms.setScoreText( StringUtils.generateRandomString(200));
                ms.setCreateDate(new Date());
                ms.setLastUpdate(new Date());
                ms.setConfidence( 0.5f + (float)(0.5f*Math.random()));
                ms.setMetaScoreInputTypeIds("1,2,3,4");
                out.add(ms);
                count++;
                if( count>=2 )
                    break;
            }
            metaScoreList = out;
        }
         */
        return metaScoreList;
    }

    public void setMetaScoreList(List<MetaScore> metaScoreList)
    {
        this.metaScoreList = metaScoreList;
    }

    public int getJobId()
    {
        return jobId;
    }

    public void setJobId(int jobId)
    {
        this.jobId = jobId;
    }

}
