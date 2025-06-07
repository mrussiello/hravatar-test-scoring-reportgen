/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.report;

import com.tm2score.report.ReportTemplateType;
import com.tm2score.util.NVPair;
import com.tm2score.util.StringUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.Basic;
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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Mike
 */
@Cacheable
@Entity
@Table( name = "report" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="Report.findByReportId", query="SELECT o FROM Report AS o WHERE o.reportId=:reportId" )
})
public class Report implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="reportid")
    private long reportId;

    @Column(name="reportstatustypeid")
    private int reportStatusTypeId=0;

    @Column(name="reporttemplatetypeid")
    private int reportTemplateTypeId=0;

    @Column(name="reportpurposetypeid")
    private int reportPurposeTypeId;

    @Column(name="implementationclass")
    private String implementationClass;

    @Column(name="emailformatterclass")
    private String emailFormatterClass;

    @Column(name="testtakeremailformatterclass")
    private String testtakerEmailFormatterClass;

    @Column(name="title")
    private String title;

    @Column(name="name")
    private String name;

    @Column(name="userid")
    private long userId;

    @Column(name="orgid")
    private long orgId;

    @Column(name="suborgid")
    private long suborgId;

    @Column(name="nopdfdoc")
    private int noPdfDoc = 0;

    @Column(name="includeinterview")
    private int includeInterview = 1;

    @Column(name="emailtesttaker")
    private int emailTestTaker = 0;

    @Column( name = "nameenglish" )
    private String nameEnglish;


    @Column(name="includenumericscores")
    private int includeNumericScores = 1;

    @Column(name="includecolorscores")
    private int includeColorScores = 1;

    @Column(name="includeoverallscore")
    private int includeOverallScore = 1;

    @Column(name="includeoverallcategory")
    private int includeOverallCategory = 0;

    @Column(name="includeoverviewtext")
    private int includeOverviewText = 1;

    @Column(name="includecompetencyscores")
    private int includeCompetencyScores = 1;

    @Column(name="includecompetencycolorscores")
    private int includeCompetencyColorScores = 1;

    @Column(name="includeitemscores")
    private int includeItemScores = 0;

    @Column(name="includeibminsight")
    private int includeIbmInsight = 1;
    
    @Column(name="includetaskscores")
    private int includeTaskScores = 1;

    @Column(name="includecompetencydescriptions")
    private int includeCompetencyDescriptions = 1;

    @Column(name="includeeductypedescrip")
    private int includeEducTypeDescrip = 1;

    @Column(name="includetrainingtypedescrip")
    private int includeTrainingTypeDescrip = 1;

    @Column(name="includerelatedexpertypedescrip")
    private int includeRelatedExperTypeDescrip = 1;

    @Column(name="includetaskinfo")
    private int includeTaskInfo = 1;  // 0=no, 1=yes first, 2=yes, after competencies

    @Column(name="maxinterviewquestionspercompetency")
    private int maxInterviewQuestionsPerCompetency = 2;

    @Column(name="includebiodatainfo")
    private int includeBiodataInfo = 1;

    @Column(name="includewritingsampleinfo")
    private int includeWritingSampleInfo = 1;

    @Column(name="includescorecalculationinfo")
    private int includeScoreCalculationInfo = 1;
    
    
    @Column(name="includetaskinterestinfo")
    private int includeTaskInterestInfo = 1;

    @Column(name="includetaskexperienceinfo")
    private int includeTaskExperienceInfo = 1;

    @Column(name="includeminqualsinfo")
    private int includeMinQualsInfo = 1;

    @Column(name="includeapplicantdatainfo")
    private int includeApplicantDataInfo = 1;

    @Column(name="includescoretextinfo")
    private int includeScoreText = 1;

    @Column(name="includeredflags")
    private int includeRedFlags = 1;

    /*
     0=No
     1=Summary Only
     2=Full
    */
    @Column(name="includeresume")
    private int includeResume = 0;
    
    /*
     0=No
     1=Summary Only
     2=Full
    */
    @Column(name="includejobdescrip")
    private int includeJobDescrip = 0;

    @Column(name="includenorms")
    private int includeNorms = 1;

    @Column(name="includesubcategorynorms")
    private int includeSubcategoryNorms = 1;

    @Column(name="includesubcategorynumeric")
    private int includeSubcategoryNumeric = 1;

    @Column(name="includesubcategorycategory")
    private int includeSubcategoryCategory = 1;

    @Column(name="includesubcategoryinterpretations")
    private int includeSubcategoryInterpretations = 1;

    /**
     * packed string ruleid1|value1|ruleid2|value2;
     */
    @Column(name="reportflags")
    private String reportFlags;


    @Column(name="intparam1")
    private int intParam1;

    @Column(name="intparam2")
    private int intParam2;

    @Column(name="intparam3")
    private int intParam3;
    
    /**
     * English Equivalent Report Id
     */
    @Column(name="longparam1")
    private long longParam1;

    

    @Column(name="floatparam1")
    private float floatParam1;

    @Column(name="floatparam2")
    private float floatParam2;

    @Column(name="floatparam3")
    private float floatParam3;

    /**
     * CT2 Report - Custom Key for Detail Text on cover page.
     */
    @Column(name="strparam1")
    private String strParam1;

    /**
     * CT2 Report - Custom Key for Report Title
     */
    @Column(name="strparam2")
    private String strParam2;

    /**
     * CT2 Report - Custom Report Title (overrides key and default title for report).
     */
    @Column(name="strparam3")
    private String strParam3;

    @Column(name="strparam4")
    private String strParam4;

    @Column(name="strparam5")
    private String strParam5;

    @Column(name="strparam6")
    private String strParam6;
    
    /**
     * CT2 Report - Custom Detail Text on cover page (overrides key and default text).
     */
    @Column(name="textparam1")
    private String textParam1;

    /**
     * CT2 Development Report Text
     * 
     */
    @Column(name="textparam2")
    private String textParam2;

    
    /**
     * Custom Report Prep notes. Removes most system-generated notes.  Delimit with |
     * 
     */
    @Column(name="textparam3")
    private String textParam3;

    /**
     * Custom Report notes for the intro to each DETAIL Section. Uses keys using Competency GroupId
     * 
     * [DETAILINTRO1]The text with returns ... 
     * [DETAILINTRO2]The text with returns ... 
     * 
    ABILITY(1,"Ability"),
    PERSONALITY(2,"Personality"),
    BIODATA(3,"Biodata"),
    SKILLS(4,"Skills"),
    EQ( 5, "Emotional Intelligence"),
    AI( 6, "AI-Derived"),
    INTERESTS( 7, "Interests")     * 
     * 
     */
    @Column(name="textparam4")
    private String textParam4;

    @Column(name="useorgbasednorms")
    private int useOrgBasedNorms = 0;


    @Column(name="localestr")
    private String localeStr;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Transient
    private Locale localeForReportGen = null;



    public List<NVPair> getReportFlagList()
    {
        return StringUtils.parseNVPairsList( reportFlags, "|" );        
    }


    public ReportTemplateType getReportTemplateType()
    {
        return ReportTemplateType.getValue( reportTemplateTypeId );
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    @Override
    public String toString() {
        return "Report[ id=" + reportId + ", name: "  + name + "]";
    }

    public int getIncludeColorScores() {
        return includeColorScores;
    }

    public void setIncludeColorScores(int includeColorScores) {
        this.includeColorScores = includeColorScores;
    }

    public int getIncludeCompetencyDescriptions() {
        return includeCompetencyDescriptions;
    }

    public void setIncludeCompetencyDescriptions(int includeCompetencyDescriptions) {
        this.includeCompetencyDescriptions = includeCompetencyDescriptions;
    }

    public int getIncludeCompetencyScores() {
        return includeCompetencyScores;
    }

    public void setIncludeCompetencyScores(int includeCompetencyScores) {
        this.includeCompetencyScores = includeCompetencyScores;
    }

    public int getIncludeEducTypeDescrip() {
        return includeEducTypeDescrip;
    }

    public void setIncludeEducTypeDescrip(int includeEducTypeDescrip) {
        this.includeEducTypeDescrip = includeEducTypeDescrip;
    }

    public int getIncludeInterview() {
        return includeInterview;
    }

    public void setIncludeInterview(int includeInterview) {
        this.includeInterview = includeInterview;
    }

    public int getIncludeNumericScores() {
        return includeNumericScores;
    }

    public void setIncludeNumericScores(int includeNumericScores) {
        this.includeNumericScores = includeNumericScores;
    }

    public int getIncludeOverallScore() {
        return includeOverallScore;
    }

    public void setIncludeOverallScore(int includeOverallScore) {
        this.includeOverallScore = includeOverallScore;
    }

    public int getIncludeRelatedExperTypeDescrip() {
        return includeRelatedExperTypeDescrip;
    }

    public void setIncludeRelatedExperTypeDescrip(int includeRelatedExperTypeDescrip) {
        this.includeRelatedExperTypeDescrip = includeRelatedExperTypeDescrip;
    }

    public int getIncludeTaskInfo() {
        return includeTaskInfo;
    }

    public void setIncludeTaskInfo(int includeTaskInfo) {
        this.includeTaskInfo = includeTaskInfo;
    }

    public int getIncludeTrainingTypeDescrip() {
        return includeTrainingTypeDescrip;
    }

    public void setIncludeTrainingTypeDescrip(int includeTrainingTypeDescrip) {
        this.includeTrainingTypeDescrip = includeTrainingTypeDescrip;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public int getMaxInterviewQuestionsPerCompetency() {
        return maxInterviewQuestionsPerCompetency;
    }

    public void setMaxInterviewQuestionsPerCompetency(int maxInterviewQuestionsPerCompetency) {
        this.maxInterviewQuestionsPerCompetency = maxInterviewQuestionsPerCompetency;
    }

    /*
    public String getNameForReportDocument( String simName )
    {
        String n = name;

        if( title !=null && !title.isEmpty() )
            n = title;

        if( simName == null )
            simName = "";

        return StringUtils.replaceStr( n, "[SIMNAME]" , simName );
    }
    */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public int getReportStatusTypeId() {
        return reportStatusTypeId;
    }

    public void setReportStatusTypeId(int reportStatusTypeId) {
        this.reportStatusTypeId = reportStatusTypeId;
    }

    public int getReportTemplateTypeId() {
        return reportTemplateTypeId;
    }

    public void setReportTemplateTypeId(int reportTemplateTypeId) {
        this.reportTemplateTypeId = reportTemplateTypeId;
    }

    public long getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(long suborgId) {
        this.suborgId = suborgId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public int getIncludeTaskExperienceInfo() {
        return includeTaskExperienceInfo;
    }

    public void setIncludeTaskExperienceInfo(int includeTaskExperienceInfo) {
        this.includeTaskExperienceInfo = includeTaskExperienceInfo;
    }

    public int getIncludeTaskInterestInfo() {
        return includeTaskInterestInfo;
    }

    public void setIncludeTaskInterestInfo(int includeTaskInterestInfo) {
        this.includeTaskInterestInfo = includeTaskInterestInfo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIncludeTaskScores() {
        return includeTaskScores;
    }

    public void setIncludeTaskScores(int includeTaskScores) {
        this.includeTaskScores = includeTaskScores;
    }

    public int getIncludeApplicantDataInfo() {
        return includeApplicantDataInfo;
    }

    public void setIncludeApplicantDataInfo(int includeApplicantDataInfo) {
        this.includeApplicantDataInfo = includeApplicantDataInfo;
    }

    public int getIncludeMinQualsInfo() {
        return includeMinQualsInfo;
    }

    public void setIncludeMinQualsInfo(int includeMinQualsInfo) {
        this.includeMinQualsInfo = includeMinQualsInfo;
    }

    public int getIncludeScoreText() {
        return includeScoreText;
    }

    public void setIncludeScoreText(int includeScoreText) {
        this.includeScoreText = includeScoreText;
    }

    public int getEmailTestTaker() {
        return emailTestTaker;
    }

    public void setEmailTestTaker(int emailTestTaker) {
        this.emailTestTaker = emailTestTaker;
    }

    public String getEmailFormatterClass() {
        return emailFormatterClass;
    }

    public void setEmailFormatterClass(String emailFormatterClass) {
        this.emailFormatterClass = emailFormatterClass;
    }

    public String getTesttakerEmailFormatterClass() {
        return testtakerEmailFormatterClass;
    }

    public void setTesttakerEmailFormatterClass(String testtakerEmailFormatterClass) {
        this.testtakerEmailFormatterClass = testtakerEmailFormatterClass;
    }

    public int getIncludeNorms() {
        return includeNorms;
    }

    public void setIncludeNorms(int includeNorms) {
        this.includeNorms = includeNorms;
    }

    public int getUseOrgBasedNorms() {
        return useOrgBasedNorms;
    }

    public void setUseOrgBasedNorms(int useOrgBasedNorms) {
        this.useOrgBasedNorms = useOrgBasedNorms;
    }

    public int getIncludeBiodataInfo() {
        return includeBiodataInfo;
    }

    public void setIncludeBiodataInfo(int includeBiodataInfo) {
        this.includeBiodataInfo = includeBiodataInfo;
    }

    public int getIncludeOverviewText() {
        return includeOverviewText;
    }

    public void setIncludeOverviewText(int includeOverviewText) {
        this.includeOverviewText = includeOverviewText;
    }

    public int getNoPdfDoc() {
        return noPdfDoc;
    }

    public void setNoPdfDoc(int noPdfDoc) {
        this.noPdfDoc = noPdfDoc;
    }

    public int getIncludeWritingSampleInfo() {
        return includeWritingSampleInfo;
    }

    public void setIncludeWritingSampleInfo(int includeWritingSampleInfo) {
        this.includeWritingSampleInfo = includeWritingSampleInfo;
    }

    public int getIncludeRedFlags() {
        return includeRedFlags;
    }

    public void setIncludeRedFlags(int includeRedFlags) {
        this.includeRedFlags = includeRedFlags;
    }

    public int getIncludeSubcategoryNorms() {
        return includeSubcategoryNorms;
    }

    public void setIncludeSubcategoryNorms(int includeSubcategoryNorms) {
        this.includeSubcategoryNorms = includeSubcategoryNorms;
    }

    public int getIncludeSubcategoryNumeric() {
        return includeSubcategoryNumeric;
    }

    public void setIncludeSubcategoryNumeric(int includeSubcategoryNumeric) {
        this.includeSubcategoryNumeric = includeSubcategoryNumeric;
    }

    public int getIncludeSubcategoryCategory() {
        return includeSubcategoryCategory;
    }

    public void setIncludeSubcategoryCategory(int includeSubcategoryCategory) {
        this.includeSubcategoryCategory = includeSubcategoryCategory;
    }

    public int getReportPurposeTypeId() {
        return reportPurposeTypeId;
    }

    public void setReportPurposeTypeId(int reportPurposeTypeId) {
        this.reportPurposeTypeId = reportPurposeTypeId;
    }

    public int getIncludeSubcategoryInterpretations() {
        return includeSubcategoryInterpretations;
    }

    public void setIncludeSubcategoryInterpretations(int includeSubcategoryInterpretations) {
        this.includeSubcategoryInterpretations = includeSubcategoryInterpretations;
    }

    public int getIncludeOverallCategory() {
        return includeOverallCategory;
    }

    public void setIncludeOverallCategory(int includeOverallCategory) {
        this.includeOverallCategory = includeOverallCategory;
    }

    public int getIncludeCompetencyColorScores() {
        return includeCompetencyColorScores;
    }

    public void setIncludeCompetencyColorScores(int includeCompetencyColorScores) {
        this.includeCompetencyColorScores = includeCompetencyColorScores;
    }

    public Locale getLocaleForReportGen() {
        return localeForReportGen;
    }

    public void setLocaleForReportGen(Locale localeForReportGen) {
        this.localeForReportGen = localeForReportGen;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }

    public int getIntParam3() {
        return intParam3;
    }

    public void setIntParam3(int intParam3) {
        this.intParam3 = intParam3;
    }

    public float getFloatParam1() {
        return floatParam1;
    }

    public void setFloatParam1(float floatParam1) {
        this.floatParam1 = floatParam1;
    }

    public float getFloatParam2() {
        return floatParam2;
    }

    public void setFloatParam2(float floatParam2) {
        this.floatParam2 = floatParam2;
    }

    public float getFloatParam3() {
        return floatParam3;
    }

    public void setFloatParam3(float floatParam3) {
        this.floatParam3 = floatParam3;
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public String getStrParam2() {
        return strParam2;
    }

    public void setStrParam2(String strParam2) {
        this.strParam2 = strParam2;
    }

    public String getStrParam3() {
        return strParam3;
    }

    public void setStrParam3(String strParam3) {
        this.strParam3 = strParam3;
    }

    public String getReportFlags() {
        return reportFlags;
    }

    public void setReportFlags(String reportFlags) {
        this.reportFlags = reportFlags;
    }

    public int getIncludeScoreCalculationInfo() {
        return includeScoreCalculationInfo;
    }

    public void setIncludeScoreCalculationInfo(int includeScoreCalculationInfo) {
        this.includeScoreCalculationInfo = includeScoreCalculationInfo;
    }

    public String getTextParam1() {
        return textParam1;
    }

    public void setTextParam1(String textParam1) {
        this.textParam1 = textParam1;
    }

    public String getTextParam2() {
        return textParam2;
    }

    public void setTextParam2(String textParam2) {
        this.textParam2 = textParam2;
    }

    public String getTextParam3() {
        return textParam3;
    }

    public void setTextParam3(String textParam3) {
        this.textParam3 = textParam3;
    }

    public String getStrParam4() {
        return strParam4;
    }

    public void setStrParam4(String strParam4) {
        this.strParam4 = strParam4;
    }

    public String getStrParam5() {
        return strParam5;
    }

    public void setStrParam5(String strParam5) {
        this.strParam5 = strParam5;
    }

    public String getStrParam6() {
        return strParam6;
    }

    public void setStrParam6(String strParam6) {
        this.strParam6 = strParam6;
    }

    public long getLongParam1() {
        return longParam1;
    }

    public void setLongParam1(long longParam1) {
        this.longParam1 = longParam1;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public int getIncludeItemScores() {
        return includeItemScores;
    }

    public void setIncludeItemScores(int includeItemScores) {
        this.includeItemScores = includeItemScores;
    }

    public int getIncludeIbmInsight() {
        return includeIbmInsight;
    }

    public void setIncludeIbmInsight(int includeIbmInsight) {
        this.includeIbmInsight = includeIbmInsight;
    }

    public String getTextParam4() {
        return textParam4;
    }

    public void setTextParam4(String textParam4) {
        this.textParam4 = textParam4;
    }

    public int getIncludeResume() {
        return includeResume;
    }

    public void setIncludeResume(int includeResume) {
        this.includeResume = includeResume;
    }

    public int getIncludeJobDescrip() {
        return includeJobDescrip;
    }

    public void setIncludeJobDescrip(int includeJobDescrip) {
        this.includeJobDescrip = includeJobDescrip;
    }



}
