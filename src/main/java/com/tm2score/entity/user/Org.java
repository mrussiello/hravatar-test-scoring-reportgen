package com.tm2score.entity.user;

import com.tm2score.global.Constants;
import com.tm2score.report.ReportUtils;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@Cacheable
@Entity
@Table( name = "org" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="Org.findByAffiliateIdAndAffiliateAccountTypeId", query="SELECT o FROM Org AS o WHERE o.affiliateId=:affiliateId AND o.affiliateAccountTypeId=:affiliateAccountTypeId" ),
})
public class Org implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="orgid")
    private int orgId;

    @Column(name="name")
    private String name;

    
    @Column(name="adminuserid")
    private long adminUserId = 0;

    @Column( name = "affiliateid" )
    private String affiliateId;

    @Column(name="orgstatustypeid")
    private int orgStatusTypeId = 0;

    @Column( name = "orgcreditusagetypeid" )
    private int orgCreditUsageTypeId = 0;

    @Column( name = "orgidtouseforcredits" )
    private int orgIdToUseForCredits = 0;
    
    //@Column( name = "orgcreditusagemaxevents" )
    //private int orgCreditUsageMaxEvents = 0;

    //@Column( name = "orgcreditusageeventcount" )
    //private int orgCreditUsageEventCount = 0;
    
    @Column(name="showoverallrawscore")
    private int showOverallRawScore;

    
    @Column(name="defaultmessagetext")
    private String defaultMessageText;

    @Column( name = "defaultcorpid" )
    private int defaultCorpId;
    
    @Column( name = "supportsendemail" )
    private String supportSendEmail;
            
    @Column(name="orgbasednorms")
    private int orgBasedNorms = 0;

    @Column( name = "companyurl" )
    private String companyUrl;
        
    
    @Column(name="reportlogourl")
    private String reportLogoUrl;

    @Column(name="excludefromnorms")
    private int excludeFromNorms;

    @Column( name = "defaulttesttakerlang" )
    private String defaultTestTakerLang;

    @Column( name = "defaultreportlang" )
    private String defaultReportLang;

    @Column( name = "webplagcheckok" )
    private int webPlagCheckOk = 0;

    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;

    @Column(name="findlyaccountcredentials")
    private String findlyAccountCredentials;

    @Column(name="voicevibescredentials")
    private String voiceVibesCredentials;
    
    @Column( name = "candidateimageviewtypeid" )
    private int candidateImageViewTypeId = 0;
    
    @Column( name = "affiliateaccounttypeid" )
    private int affiliateAccountTypeId = 0;

    
    
    /*
     0 = none
     1 = both
     2= Live video interviewing only , no testing
     3= testing only, no live video interviewing    
    */
    @Column( name = "aiok" )
    private int aiOk;
    
    /*
     * 0 (default) requires a logon to view candidate audio or videos, or a valid limited access link.
     * 10 - Anyone can view even without a logon
    */
    @Column( name = "candidateaudiovideoviewtypeid" )
    private int candidateAudioVideoViewTypeId = 0;
    
    @Column(name="customfieldname1")
    private String customFieldName1;

    @Column(name="customfieldname2")
    private String customFieldName2;

    @Column(name="customfieldname3")
    private String customFieldName3;

    @Column(name="includescorecalcinfoinreports")
    private int includeScoreCalcInfoInReports;
    
    @Column(name="percentilecountry")
    private String percentileCountry;

    @Column(name="testkeyemailsubj")
    private String testKeyEmailSubj;

    @Column(name="testkeyemailmsg")
    private String testKeyEmailMsg;

    @Column(name="testkeysmsmsg")
    private String testKeySmsMsg;
    
    @Column(name="customstringvals")
    private String customStringVals;
    
    @Column( name = "reportdownloadtypeid" )
    private int reportDownloadTypeId;
    
    @Column( name = "hideprocimagespdf" )
    private int hideProcImagesPdf = 0;
    

    @Column(name="includeenglishreport")
    private int includeEnglishReport;
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="orgcreditusageenddate")
    private Date orgCreditUsageEndDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="orgcreditusagestartdate")
    private Date orgCreditUsageStartDate;

    
    @Column(name="hqcity")
    private String hqCity;

    @Column(name="hqcountry")
    private String hqCountry = "US";

    @Column(name="campaigncode")
    private String campaignCode;
        
    @Column(name="ratingnames")
    private String ratingNames;    
    
    
    
    /**
     * 
     * packed string ruleid1|value1|ruleid2|value2;
     */
    @Column(name="reportflags")
    private String reportFlags;

    /**
     * 0 = none
     * 1 = invitations only
     * 2 = invitations and reminders only
     * 10 = all (invitations, reminders, reports, etc).
     */   
    @Column(name="cconcandemails")
    private int ccOnCandEmails;

    
    
    @Transient
    private String[] ratingNamesArray;
    
    

    @Override
    public boolean equals( Object o )
    {
        if( o instanceof Org )
        {
            Org u = (Org) o;

            return orgId == u.getOrgId();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 71 * hash + (int) (this.orgId ^ (this.orgId >>> 32));
        return hash;
    }
    
    public String getCustomStringValue( String tagName )
    {
        String s = StringUtils.getBracketedArtifactFromString( customStringVals,  tagName );
        s = StringUtils.replaceStr(s, "{{", "[" );
        return StringUtils.replaceStr(s, "}}", "]" );
        //return StringUtils.getBracketedArtifactFromString( customStringVals,  tagName );
    }
    
    public String getPostTestContactStr()
    {
        return getCustomStringValue(Constants.CSVPOSTTESTCONTACTSTR );
    }

    
    
    
    public String getOrgIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( orgId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "Org.getOrgIdEncrypted() orgId=" + orgId );
            return "";
        }
    }

    public boolean getInternationalSmsOk()
    {
       return ReportUtils.getReportFlagBooleanValue( "intlsmsok", null, null, null, this, null);
    }
    
    
    public boolean getIsSmsOk()
    {
       return !ReportUtils.getReportFlagBooleanValue( "allsmsoff", null, null, null, this, null);
    }
    
    
    public String getRatingName1()
    {
        return getRatingName(1);
    }

    public String getRatingName2()
    {
        return getRatingName(2);
    }

    public String getRatingName3()
    {
        return getRatingName(3);
    }

    public String getRatingName4()
    {
        return getRatingName(4);
    }

    public String getRatingName5()
    {
        return getRatingName(5);
    }

    public String getRatingName6()
    {
        return getRatingName(6);
    }

    public String getRatingName7()
    {
        return getRatingName(7);
    }

    public String getRatingName8()
    {
        return getRatingName(8);
    }

    public String getRatingName9()
    {
        return getRatingName(9);
    }

    public String getRatingName10()
    {
        return getRatingName(10);
    }
    

    public int getRatingNameCount()
    {
        int ct = 0;
        for( String s : getRatingNamesArray() )
        {
            if( s!=null && !s.isBlank() )
                ct++;
            else
                break;
        }
        return ct;
    }
    
    public String getRatingName(int idx)
    {
        return getRatingNamesArray()[idx-1];
    }
    
    public String[] getRatingNamesArray()
    {
        if( ratingNamesArray!=null )
            return ratingNamesArray;
        
        if( ratingNames == null || ratingNames.isBlank() )
        {
            ratingNamesArray = new String[10];
            return ratingNamesArray;
        }

        List<String> out = new ArrayList<>();

        for( String s : ratingNames.split( ";" ) )
        {
            if( s==null )
                continue;

            s = s.trim();

            out.add( s );
        }
        
        while( out.size()<10 )
        {
            out.add("");
        }

	String[] sa = new String[out.size()];
        out.toArray(sa);
        ratingNamesArray=sa;
        return sa;
    }
    
    
    
    /*
    public List<NVPair> getReportFlagList(Suborg suborg, com.tm2score.entity.report.Report report, Product product)
    {
        List<NVPair> out = new ArrayList<>();
        
        List<NVPair> l2;
              
        // Start with Report - lowest level
        if( report != null )
            out = report.getReportFlagList();

        // Product is the next level.
        if( product!=null )
        {
            l2 = product.getReportFlagList();
            
            // LogService.logIt( "Org.getReportFlagList()  Product id=" + product.getProductId() + "  has " + l2.size() + " report flags." );
            
            for( NVPair pr : out )
            {
                if( !hasNvPair( pr, l2 ) )
                {
                    l2.add( pr );
                }
            }

            out=l2;            
        }        
        
        // LogService.logIt( "Org.getReportFlagList() AAA.1 out has " + out.size() + " report flags." );        
        
            // Next level is Org
        if( reportFlags !=null && !reportFlags.isEmpty() )
        {
            l2 = getReportFlagList();
            
            for( NVPair pr : out )
            {
                // Only add from out if not in l2
                if( !hasNvPair( pr, l2 ) )
                {
                    l2.add( pr );
                }
            }
            
            out=l2;
        }

        // LogService.logIt( "Org.getReportFlagList() AAA.2 out has " + out.size() + " report flags." );        
        
        // Highest level is Suborg list
        if( suborg != null )
        {
            l2 = suborg.getReportFlagList();
            
            for( NVPair pr : out )
            {
                if( !hasNvPair( pr, l2 ) )
                {
                    l2.add( pr );
                }
            }

            out=l2;
        }

        // LogService.logIt( "Org.getReportFlagList() AAA.3 out has " + out.size() + " report flags." );        
        
        //for( NVPair pr : out )
        //{
        //    LogService.logIt( "Org.getReportFlagList() AAA.4 " + pr.getName() + "=" + pr.getValue() );                
        //}        
        
        return out;
    }

    
    public boolean hasNvPair( NVPair nvp, List<NVPair> pl  )
    {
        if( nvp == null )
            return false;
        
        for( NVPair pr : pl )
        {
            if( pr.getName()!=null && pr.getName().equals( nvp.getName() ) )
                return true;
        }
        
        return false;
    }
    

    
    public List<NVPair> getReportFlagList()
    {
        return StringUtils.parseNVPairsList( reportFlags, "|" );          
    }
    */

    
    
    @Override
    public String toString() {
        return "Org{" + "orgId=" + orgId + ", name=" + name;
    }
    
    
    
    public boolean getHasCustomSupportSendEmail()
    {
        return this.supportSendEmail!=null && !this.supportSendEmail.isBlank() && EmailUtils.validateEmailNoErrors(supportSendEmail);
    }
    
    private void correctDefaultMessageText()
    {
        if( defaultMessageText!=null && !defaultMessageText.isBlank() && !defaultMessageText.contains("{TK") )
            defaultMessageText="{" + Constants.TKCOMPLETEMSG + "}" + defaultMessageText.trim();                
    }

    public String getTestTakerCompleteEmail()
    {
        correctDefaultMessageText();        
        return StringUtils.getCurleyBracketedArtifactFromString(defaultMessageText, Constants.TKCOMPLETEMSG);
    }

    public String getTestTakerCompleteEmailSubj()
    {
        correctDefaultMessageText();                
        return StringUtils.getCurleyBracketedArtifactFromString(defaultMessageText, Constants.TKCOMPLETEMSGSUBJ);
    }

    
    
    public String getTestKeyReminderEmail()
    {
        correctDefaultMessageText();        
        return StringUtils.getCurleyBracketedArtifactFromString(defaultMessageText, Constants.TKREMINDEREMAIL);
    }

    public String getTestKeyReminderEmailSubj()
    {
        correctDefaultMessageText();        
        return StringUtils.getCurleyBracketedArtifactFromString(defaultMessageText, Constants.TKREMINDEREMAILSUBJ);
    }
    
    public String getTestKeyReminderTextMsg()
    {
        correctDefaultMessageText();        
        return StringUtils.getCurleyBracketedArtifactFromString(defaultMessageText, Constants.TKREMINDERTEXT);
    }

    public long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getDefaultMessageText() {
        return defaultMessageText;
    }

    public void setDefaultMessageText(String defaultMessageText) {
        this.defaultMessageText = defaultMessageText;
    }

    public int getOrgBasedNorms() {
        return orgBasedNorms;
    }

    public void setOrgBasedNorms(int orgBasedNorms) {
        this.orgBasedNorms = orgBasedNorms;
    }

    public String getReportLogoUrl() {
        return reportLogoUrl;
    }

    public void setReportLogoUrl(String reportLogoUrl) {
        this.reportLogoUrl = reportLogoUrl;
    }

    public int getExcludeFromNorms() {
        return excludeFromNorms;
    }

    public void setExcludeFromNorms(int excludeFromNorms) {
        this.excludeFromNorms = excludeFromNorms;
    }
    public String getDefaultTestTakerLang() {
        return defaultTestTakerLang;
    }

    public void setDefaultTestTakerLang(String defaultTestTakerLang) {
        this.defaultTestTakerLang = defaultTestTakerLang;
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

    public String getDefaultReportLang() {
        return defaultReportLang;
    }

    public void setDefaultReportLang(String defaultReportLang) {
        this.defaultReportLang = defaultReportLang;
    }
    

    

    public String getReportFlags() {
        return reportFlags;
    }

    public void setReportFlags(String reportFlags) {
        this.reportFlags = reportFlags;
    }


    public String getFindlyAccountCredentials() {
        return findlyAccountCredentials;
    }

    public void setFindlyAccountCredentials(String findlyAccountCredentials) {
        this.findlyAccountCredentials = findlyAccountCredentials;
    }

    public String getCustomFieldName1() {
        return customFieldName1;
    }

    public void setCustomFieldName1(String customFieldName1) {
        this.customFieldName1 = customFieldName1;
    }

    public String getCustomFieldName2() {
        return customFieldName2;
    }

    public void setCustomFieldName2(String customFieldName2) {
        this.customFieldName2 = customFieldName2;
    }

    public String getCustomFieldName3() {
        return customFieldName3;
    }

    public void setCustomFieldName3(String customFieldName3) {
        this.customFieldName3 = customFieldName3;
    }

    public int getIncludeScoreCalcInfoInReports() {
        return includeScoreCalcInfoInReports;
    }

    public void setIncludeScoreCalcInfoInReports(int includeScoreCalcInfoInReports) {
        this.includeScoreCalcInfoInReports = includeScoreCalcInfoInReports;
    }

    public String getPercentileCountry() {
        return percentileCountry;
    }

    public void setPercentileCountry(String percentileCountry) {
        this.percentileCountry = percentileCountry;
    }

    public String getTestKeyEmailSubj() {
        return testKeyEmailSubj;
    }

    public void setTestKeyEmailSubj(String testKeyEmailSubj) {
        this.testKeyEmailSubj = testKeyEmailSubj;
    }

    public String getTestKeyEmailMsg() {
        return testKeyEmailMsg;
    }

    public void setTestKeyEmailMsg(String testKeyEmailMsg) {
        this.testKeyEmailMsg = testKeyEmailMsg;
    }

    public String getTestKeySmsMsg() {
        return testKeySmsMsg;
    }

    public void setTestKeySmsMsg(String testKeySmsMsg) {
        this.testKeySmsMsg = testKeySmsMsg;
    }

    public int getIncludeEnglishReport() {
        return includeEnglishReport;
    }

    public void setIncludeEnglishReport(int includeEnglishReport) {
        this.includeEnglishReport = includeEnglishReport;
    }

    public String getVoiceVibesCredentials() {
        return voiceVibesCredentials;
    }

    public void setVoiceVibesCredentials(String voiceVibesCredentials) {
        this.voiceVibesCredentials = voiceVibesCredentials;
    }

    public int getWebPlagCheckOk() {
        return webPlagCheckOk;
    }

    public void setWebPlagCheckOk(int webPlagCheckOk) {
        this.webPlagCheckOk = webPlagCheckOk;
    }

    public String getAffiliateId() {
        return affiliateId;
    }

    public void setAffiliateId(String affiliateId) {
        this.affiliateId = affiliateId;
    }

    public int getShowOverallRawScore() {
        return showOverallRawScore;
    }

    public void setShowOverallRawScore(int showOverallRawScore) {
        this.showOverallRawScore = showOverallRawScore;
    }

    public int getCandidateImageViewTypeId() {
        return candidateImageViewTypeId;
    }

    public void setCandidateImageViewTypeId(int candidateImageViewTypeId) {
        this.candidateImageViewTypeId = candidateImageViewTypeId;
    }

    public int getCandidateAudioVideoViewTypeId() {
        return candidateAudioVideoViewTypeId;
    }

    public void setCandidateAudioVideoViewTypeId(int candidateAudioVideoViewTypeId) {
        this.candidateAudioVideoViewTypeId = candidateAudioVideoViewTypeId;
    }

    public int getOrgStatusTypeId() {
        return orgStatusTypeId;
    }

    public void setOrgStatusTypeId(int orgStatusTypeId) {
        this.orgStatusTypeId = orgStatusTypeId;
    }

    public int getOrgCreditUsageTypeId() {
        return orgCreditUsageTypeId;
    }

    public void setOrgCreditUsageTypeId(int orgCreditUsageTypeId) {
        this.orgCreditUsageTypeId = orgCreditUsageTypeId;
    }

    //public int getOrgCreditUsageMaxEvents() {
    //    return orgCreditUsageMaxEvents;
    //}

   // public void setOrgCreditUsageMaxEvents(int orgCreditUsageMaxEvents) {
    //    this.orgCreditUsageMaxEvents = orgCreditUsageMaxEvents;
    //}

    //public int getOrgCreditUsageEventCount() {
    //    return orgCreditUsageEventCount;
    //}

    //public void setOrgCreditUsageEventCount(int orgCreditUsageEventCount) {
   //     this.orgCreditUsageEventCount = orgCreditUsageEventCount;
    //}

    public Date getOrgCreditUsageEndDate() {
        return orgCreditUsageEndDate;
    }

    public void setOrgCreditUsageEndDate(Date orgCreditUsageEndDate) {
        this.orgCreditUsageEndDate = orgCreditUsageEndDate;
    }

    public Date getOrgCreditUsageStartDate() {
        return orgCreditUsageStartDate;
    }

    public void setOrgCreditUsageStartDate(Date orgCreditUsageStartDate) {
        this.orgCreditUsageStartDate = orgCreditUsageStartDate;
    }

    public int getAffiliateAccountTypeId() {
        return affiliateAccountTypeId;
    }

    public void setAffiliateAccountTypeId(int affiliateAccountTypeId) {
        this.affiliateAccountTypeId = affiliateAccountTypeId;
    }

    public int getReportDownloadTypeId() {
        return reportDownloadTypeId;
    }

    public void setReportDownloadTypeId(int reportDownloadTypeId) {
        this.reportDownloadTypeId = reportDownloadTypeId;
    }


    public int getAiOk() {
        return aiOk;
    }

    public void setAiOk(int aiOk) {
        this.aiOk = aiOk;
    }

    public boolean getTestAiOk() {
        return aiOk==1 || aiOk==3;
    }

    public int getHideProcImagesPdf() {
        return hideProcImagesPdf;
    }

    public void setHideProcImagesPdf(int hideProcImagesPdf) {
        this.hideProcImagesPdf = hideProcImagesPdf;
    }

    public int getDefaultCorpId() {
        return defaultCorpId;
    }

    public void setDefaultCorpId(int defaultCorpId) {
        this.defaultCorpId = defaultCorpId;
    }

    public String getSupportSendEmail() {
        return supportSendEmail;
    }

    public void setSupportSendEmail(String supportSendEmail) {
        this.supportSendEmail = supportSendEmail;
    }

    public String getCustomStringVals() {
        return customStringVals;
    }

    public void setCustomStringVals(String customStringVals) {
        this.customStringVals = customStringVals;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public void setCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl;
    }

    public String getHqCity() {
        return hqCity;
    }

    public void setHqCity(String hqCity) {
        this.hqCity = hqCity;
    }

    public String getHqCountry() {
        return hqCountry;
    }

    public void setHqCountry(String hqCountry) {
        this.hqCountry = hqCountry;
    }

    public String getCampaignCode() {
        return campaignCode;
    }

    public void setCampaignCode(String campaignCode) {
        this.campaignCode = campaignCode;
    }

    public int getOrgIdToUseForCredits() {
        return orgIdToUseForCredits;
    }

    public void setOrgIdToUseForCredits(int orgIdToUseForCredits) {
        this.orgIdToUseForCredits = orgIdToUseForCredits;
    }

    public String getRatingNames() {
        return ratingNames;
    }

    public void setRatingNames(String ratingNames) {
        this.ratingNames = ratingNames;
    }

    public int getCcOnCandEmails() {
        return ccOnCandEmails;
    }

    public void setCcOnCandEmails(int ccOnCandEmails) {
        this.ccOnCandEmails = ccOnCandEmails;
    }



}
