/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.user;

import com.tm2score.dist.DistManager;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.user.User;
import com.tm2score.global.RuntimeConstants;
// import com.tm2score.essay.DiscernBean;
import com.tm2score.report.ReportManager;
import com.tm2score.score.ScoreManager;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author Mike
 */
@Named
@SessionScoped
public class UserBean implements Serializable
{
    private User user;

    private long logonHistoryId = 0;
    private int testKeyEventSelectionTypeId = 0;
    
    private String simIdVersionIdPairs;
    private long simId;
    private int simVersionId;
    private long simId2;
    private int simVersionId2;
    private int simOrgId;
    private int simSuborgId;
    private int simOrgId2;
    private int simSuborgId2;
    private long testKeyId;
    private long testKeyId2;
    private long testKeyId3;
    private long testKeyId4;
    private long testKeyId5;
    private long testKeyId6;
    private long testEventId;
    private long testEventId2; // = 58;
    private long minTestEventId;
    private long reportId;
    private long reportId2; // = 4;
    private int productId3; // = 4;
    private long reportId3; // = 4;
    private long reportId4; // = 4;
    private String testEventIds = null;
    private String testEventIds2 = null;
    
    private int intParam1 = 1;
    
    
    private String testKeyIdz2 = null;
    private String testEventIdz2 = null;

    private String textRecipPh = "+1-571-213-5677";

    private String simDescripXml;

    private String localeStr2 = null;
    private String localeStr3 = null;
    private String localeStr4 = null;
    private boolean forceCalcSection = false;
    private boolean forceCalcSection2 = false;
    private boolean forceCalcSection3 = false;

    private boolean clearExternal = false;
    private boolean clearExternal2 = false;
    private boolean resetSpeechText = false;
    private boolean sendResendCandidateReportEmails = false;
    private boolean sendResendCandidateReportEmails2 = false;
    private boolean sendResendCandidateReportEmails3 = false;
    
    private boolean skipVersionCheck = false;
    
    private int simOrgId3;

    private Date startDate;
    private Date endDate;

    private Date startDate3;
    private Date endDate3;

    private Date maxLastSendDate;
    private Date maxLastSendDate2;
    private Date maxLastSendDate3;
    
    // private int failedLogonAttempts=0;
    
    
    private String pin;

    private List<TestEventScore> reportTestEventScoreList;

    private TestEventScore testEventScore;

    private boolean booleanParam1;
    
    private String testKeyIdsStr;
    private String testKeyIdsStr2;
    
    private String resultStr1;




    /** Creates a new instance of UserBean */
    public UserBean()
    {
    }

    public static UserBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (UserBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "userBean" );
    }

    public void clearBean()
    {
        clear();
        
        simId=0;
        simVersionId=0;
        simId2=0;
        simVersionId2=0;
        simOrgId=0;
        simSuborgId=0;
        simOrgId2=0;
        simSuborgId2=0;
        testKeyId=0;
        testKeyId2=0;
        testKeyId3=0;
        testKeyId4=0;
        testKeyId5=0;
        testEventId=0;
        testEventId2=0; // = 58;
        reportId=0;
        reportId2=0; // = 4;
        productId3=0; // = 4;
        reportId3=0; // = 4;
        reportId4=0; // = 4;
        testEventIds = null;
        testEventIds2 = null;

        textRecipPh = null;

        simDescripXml = null;

        localeStr2 = null;
        localeStr3 = null;
        forceCalcSection = false;
        forceCalcSection2 = false;
        forceCalcSection3 = false;

        clearExternal = false;
        clearExternal2 = false;

        skipVersionCheck = false;

        simOrgId3=0;

        startDate = null;
        endDate = null;

        startDate3 = null;
        endDate3 = null;   
        reportTestEventScoreList=null;
        testEventScore=null;
        booleanParam1=false;
        testKeyIdsStr=null;        
        testKeyIdsStr2=null;   
        resultStr1=null;
    }
    
    public void clear()
    {
        setSimDescripXml( null );
        setTestEventId( 0 );
        setTestKeyId( 0 );
        setTestKeyId2( 0 );
        simId=0;
        simVersionId=0;
        testEventIds = null;
        testKeyIdsStr = null;
        setPin( null );
        resultStr1=null;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    
    public boolean getScoringDebug()
    {
        return ScoreManager.DEBUG_SCORING;
    }

    public boolean getReportingDebug()
    {
        return ReportManager.DEBUG_REPORTS;
    }

    public boolean getDistDebug()
    {
        return DistManager.DEBUG_DIST;
    }

    public boolean getUserLoggedOnAsAdmin()
    {
        return user != null && user.getUserId()>0 && ( user.getRoleId() >= RoleType.ADMIN.getRoleTypeId() );
    }

    public String getBaseLogoUrl()
    {
        return RuntimeConstants.getStringValue("baselogourl");
    }
    
    public String getBaseIconUrl()
    {
        return RuntimeConstants.getStringValue("baseiconurl");
    }
    
    public long getUserId()
    {
        if( user == null )
            return 0;

        return user.getUserId();
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public String getSimDescripXml() {
        return simDescripXml;
    }

    public void setSimDescripXml(String s) {
        this.simDescripXml = s;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public long getTestKeyId2() {
        return testKeyId2;
    }

    public void setTestKeyId2(long testKeyId2) {
        this.testKeyId2 = testKeyId2;
    }

    public long getReportId2() {
        return reportId2;
    }

    public void setReportId2(long reportId2) {
        this.reportId2 = reportId2;
    }

    public long getTestEventId2() {
        return testEventId2;
    }

    public void setTestEventId2(long testEventId2) {
        this.testEventId2 = testEventId2;
    }

    public List<TestEventScore> getReportTestEventScoreList() {
        return reportTestEventScoreList;
    }

    public void setReportTestEventScoreList(List<TestEventScore> reportTestEventScoreList) {
        this.reportTestEventScoreList = reportTestEventScoreList;
    }

    public TestEventScore getTestEventScore() {
        return testEventScore;
    }

    public void setTestEventScore(TestEventScore testEventScore) {
        this.testEventScore = testEventScore;
    }

    public long getTestKeyId3() {
        return testKeyId3;
    }

    public void setTestKeyId3(long testKeyId3) {
        this.testKeyId3 = testKeyId3;
    }

    public String getTextRecipPh() {
        return textRecipPh;
    }

    public void setTextRecipPh(String textRecipPh) {
        this.textRecipPh = textRecipPh;
    }

    public String getTestEventIds() {
        return testEventIds;
    }

    public void setTestEventIds(String testEventIds) {
        this.testEventIds = testEventIds;
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

    public int getSimOrgId() {
        return simOrgId;
    }

    public void setSimOrgId(int simOrgId) {
        this.simOrgId = simOrgId;
    }

    public int getSimSuborgId() {
        return simSuborgId;
    }

    public void setSimSuborgId(int simSuborgId) {
        this.simSuborgId = simSuborgId;
    }

    public int getProductId3() {
        return productId3;
    }

    public void setProductId3(int productId3) {
        this.productId3 = productId3;
    }

    public long getReportId3() {
        return reportId3;
    }

    public void setReportId3(long reportId3) {
        this.reportId3 = reportId3;
    }

    public String getLocaleStr2() {
        return localeStr2;
    }

    public void setLocaleStr2(String localeStr2) {
        this.localeStr2 = localeStr2;
    }

    public boolean getBooleanParam1() {
        return booleanParam1;
    }

    public void setBooleanParam1(boolean booleanParam1) {
        this.booleanParam1 = booleanParam1;
    }

    public long getSimId2() {
        return simId2;
    }

    public void setSimId2(long simId2) {
        this.simId2 = simId2;
    }

    public int getSimVersionId2() {
        return simVersionId2;
    }

    public void setSimVersionId2(int simVersionId2) {
        this.simVersionId2 = simVersionId2;
    }

    public long getReportId4() {
        return reportId4;
    }

    public void setReportId4(long reportId4) {
        this.reportId4 = reportId4;
    }

    public int getSimOrgId2() {
        return simOrgId2;
    }

    public void setSimOrgId2(int simOrgId2) {
        this.simOrgId2 = simOrgId2;
    }

    public int getSimSuborgId2() {
        return simSuborgId2;
    }

    public void setSimSuborgId2(int simSuborgId2) {
        this.simSuborgId2 = simSuborgId2;
    }

    public String getLocaleStr3() {
        return localeStr3;
    }

    public void setLocaleStr3(String localeStr3) {
        this.localeStr3 = localeStr3;
    }

    public long getTestKeyId4() {
        return testKeyId4;
    }

    public void setTestKeyId4(long testKeyId4) {
        this.testKeyId4 = testKeyId4;
    }

    public String getTestKeyIdsStr() {
        return testKeyIdsStr;
    }

    public void setTestKeyIdsStr(String testKeyIdsStr) {
        this.testKeyIdsStr = testKeyIdsStr;
    }

    public boolean isForceCalcSection() {
        return forceCalcSection;
    }

    public void setForceCalcSection(boolean forceCalcSection) {
        this.forceCalcSection = forceCalcSection;
    }

    public boolean isForceCalcSection2() {
        return forceCalcSection2;
    }

    public void setForceCalcSection2(boolean forceCalcSection2) {
        this.forceCalcSection2 = forceCalcSection2;
    }

    public boolean isForceCalcSection3() {
        return forceCalcSection3;
    }

    public void setForceCalcSection3(boolean forceCalcSection3) {
        this.forceCalcSection3 = forceCalcSection3;
    }

    public int getSimOrgId3() {
        return simOrgId3;
    }

    public void setSimOrgId3(int simOrgId3) {
        this.simOrgId3 = simOrgId3;
    }

    public Date getStartDate3() {
        return startDate3;
    }

    public void setStartDate3(Date startDate3) {
        this.startDate3 = startDate3;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    
    
    public Date getEndDate3() {
        return endDate3;
    }

    public void setEndDate3(Date endDate3) {
        this.endDate3 = endDate3;
    }

    public boolean isClearExternal() {
        return clearExternal;
    }

    public void setClearExternal(boolean clearExternal) {
        this.clearExternal = clearExternal;
    }

    public boolean isClearExternal2() {
        return clearExternal2;
    }

    public void setClearExternal2(boolean clearExternal2) {
        this.clearExternal2 = clearExternal2;
    }

    public long getTestKeyId5() {
        return testKeyId5;
    }

    public void setTestKeyId5(long testKeyId5) {
        this.testKeyId5 = testKeyId5;
    }

    public long getLogonHistoryId() {
        return logonHistoryId;
    }

    public void setLogonHistoryId(long logonHistoryId) {
        this.logonHistoryId = logonHistoryId;
    }

    public boolean isSkipVersionCheck() {
        return skipVersionCheck;
    }

    public void setSkipVersionCheck(boolean skipVersionCheck) {
        this.skipVersionCheck = skipVersionCheck;
    }

    public int getTestKeyEventSelectionTypeId() {
        return testKeyEventSelectionTypeId;
    }

    public void setTestKeyEventSelectionTypeId(int testKeyEventSelectionTypeId) {
        this.testKeyEventSelectionTypeId = testKeyEventSelectionTypeId;
    }

    public String getLocaleStr4() {
        return localeStr4;
    }

    public void setLocaleStr4(String localeStr4) {
        this.localeStr4 = localeStr4;
    }

    public boolean isResetSpeechText() {
        return resetSpeechText;
    }

    public void setResetSpeechText(boolean resetSpeechText) {
        this.resetSpeechText = resetSpeechText;
    }

    public String getTestEventIds2() {
        return testEventIds2;
    }

    public void setTestEventIds2(String testEventIds2) {
        this.testEventIds2 = testEventIds2;
    }

    public String getTestKeyIdsStr2() {
        return testKeyIdsStr2;
    }

    public void setTestKeyIdsStr2(String testKeyIdsStr2) {
        this.testKeyIdsStr2 = testKeyIdsStr2;
    }

    public boolean getSendResendCandidateReportEmails() {
        return sendResendCandidateReportEmails;
    }

    public void setSendResendCandidateReportEmails(boolean sendResendCandidateReportEmails) {
        this.sendResendCandidateReportEmails = sendResendCandidateReportEmails;
    }

    public long getMinTestEventId() {
        return minTestEventId;
    }

    public void setMinTestEventId(long minTestEventId) {
        this.minTestEventId = minTestEventId;
    }

    public Date getMaxLastSendDate() {
        return maxLastSendDate;
    }

    public void setMaxLastSendDate(Date maxLastSendDate) {
        this.maxLastSendDate = maxLastSendDate;
    }

    public boolean getSendResendCandidateReportEmails2() {
        return sendResendCandidateReportEmails2;
    }

    public void setSendResendCandidateReportEmails2(boolean sendResendCandidateReportEmails2) {
        this.sendResendCandidateReportEmails2 = sendResendCandidateReportEmails2;
    }

    public Date getMaxLastSendDate2() {
        return maxLastSendDate2;
    }

    public void setMaxLastSendDate2(Date maxLastSendDate2) {
        this.maxLastSendDate2 = maxLastSendDate2;
    }

    public boolean getSendResendCandidateReportEmails3() {
        return sendResendCandidateReportEmails3;
    }

    public void setSendResendCandidateReportEmails3(boolean sendResendCandidateReportEmails3) {
        this.sendResendCandidateReportEmails3 = sendResendCandidateReportEmails3;
    }

    public Date getMaxLastSendDate3() {
        return maxLastSendDate3;
    }

    public void setMaxLastSendDate3(Date maxLastSendDate3) {
        this.maxLastSendDate3 = maxLastSendDate3;
    }

    public String getTestKeyIdz2() {
        return testKeyIdz2;
    }

    public void setTestKeyIdz2(String testKeyIdz2) {
        this.testKeyIdz2 = testKeyIdz2;
    }

    public String getTestEventIdz2() {
        return testEventIdz2;
    }

    public void setTestEventIdz2(String testEventIdz2) {
        this.testEventIdz2 = testEventIdz2;
    }

    public long getTestKeyId6() {
        return testKeyId6;
    }

    public void setTestKeyId6(long testKeyId6) {
        this.testKeyId6 = testKeyId6;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public String getResultStr1() {
        return resultStr1;
    }

    public void setResultStr1(String resultStr1) {
        this.resultStr1 = resultStr1;
    }

    public String getSimIdVersionIdPairs() {
        return simIdVersionIdPairs;
    }

    public void setSimIdVersionIdPairs(String simIdVersionIdPairs) {
        this.simIdVersionIdPairs = simIdVersionIdPairs;
    }
    
    
    
}
