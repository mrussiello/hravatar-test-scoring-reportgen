package com.tm2score.entity.event;

import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.iactnresp.IactnRespFactory;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.score.iactnresp.IactnResp;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.global.ErrorTxtObject;
import com.tm2score.score.*;
import com.tm2score.sim.MultiCompetenciesPerItemType;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.service.LogService;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.ct5.event.Ct5ResumeUtils;
import com.tm2score.entity.proctor.RemoteProctorEvent;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.user.User;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.global.NumberUtils;
import com.tm2score.service.EncryptUtils;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.IntnHist;
import java.io.Serializable;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.Locale;

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
@Table( name = "testevent" )
@NamedQueries( {
        @NamedQuery( name = "TestEvent.findByTestKeyId", query = "SELECT o FROM TestEvent AS o WHERE o.testKeyId=:testKeyId" ),
        @NamedQuery( name = "TestEvent.findByTestEventId", query = "SELECT o FROM TestEvent AS o WHERE o.testEventId=:testEventId" ),
        @NamedQuery( name = "TestEvent.findRecentByUserIdAndOrgId", query = "SELECT o FROM TestEvent AS o WHERE o.orgId=:orgId AND o.userId=:userId ORDER BY o.lastAccessDate DESC" )
        
} )
public class TestEvent implements Serializable, Comparable<TestEvent>, ErrorTxtObject
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
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
    private float overallPercentile = -1;

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

    @Column( name = "proctoruserid" )
    private long proctorUserId;

    @Column( name = "percentcomplete" )
    private float percentComplete = 0;

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

    @Column(name="textstr1")
    private String textStr1;

    @Column(name="extref")
    private String extRef;

    @Column( name = "thirdpartytesteventid" )
    private String thirdPartyTestEventId;

    @Column( name = "thirdpartytestaccountid" )
    private String thirdPartyTestAccountId;
    
    @Column(name="errortxt")
    private String errorTxt;

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
    private Org org;

    @Transient
    private User user;

    @Transient
    private Suborg suborg;

    @Transient
    private SimDescriptor simDescriptor;

    @Transient
    private Report report;

    @Transient
    private Report report2;

    @Transient
    private Report report3;

    @Transient
    private Profile profile;

    @Transient
    SimJ simXmlObj;

    @Transient
    Clicflic resultXmlObj;

    @Transient
    long testEventArchiveId = 0;
    
    @Transient
    int textTitleSequenceId = 0;

    @Transient
    private int multCompetenciesPerItemTypeId = 0;

    @Transient
    List<TestEventResponseRating> testEventResponseRatingList;
    
    @Transient
    List<SimCompetencyScore> simCompetencyScoreList = null;

    @Transient
    List<SimletScore> simletScoreList = null;

    @Transient
    List<ScorableResponse> autoScorableResponseList = null;

    @Transient
    List<ScorableResponse> allResponseList = null;

    @Transient
    List<TestEventScore> testEventScoreList = null;

    @Transient
    SurveyEvent surveyEvent;

    @Transient
    private List<Profile> altScoreProfileList;

    @Transient
    private RemoteProctorEvent remoteProctorEvent;
    
    @Transient
    private boolean partialBatteryTestEvent = false;
    

    @Override
    public int compareTo(TestEvent o) {

        if( startDate != null && o.getStartDate() != null )
            return startDate.compareTo( o.getStartDate() );

        return ((Long)( testEventId )).compareTo( (Long)( o.getTestEventId() ) );
    }

    public String getTestEventIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( testEventId );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "getTestEventIdEncrypted() " + toString()  );

            return "";
        }
    }

    public String getOverallScore4Show()
    {
        if( this.getScorePrecisionDigits()==2 )
            return NumberUtils.getTwoDecimalFormattedAmount(getOverallScore());
        if( this.getScorePrecisionDigits()==1 )
            return NumberUtils.getOneDecimalFormattedAmount(getOverallScore());
        return Integer.toString(Math.round(overallScore) );
    }

    
    
    public int getScorePrecisionDigits()
    {
        return getScoreFormatType().getScorePrecisionDigits();
    }

    
    

    public TestEventStatusType getTestEventStatusType()
    {
        return TestEventStatusType.getValue( testEventStatusTypeId );
    }

    public List<IactnResp> getAllIactnResponseList()
    {
        if( allResponseList == null )
            return null;

        List<IactnResp> out = new ArrayList<>();

        for( ScorableResponse sr : allResponseList )
        {
            if( sr instanceof IactnResp )
            {
                out.add( (IactnResp) sr );
            }
        }

        return out;
    }


    public boolean hasProfile()
    {
        return profile != null;
    }

    public void populatePercentileObj( Percentile p )
    {
        p.setTestEventId(testEventId);
        p.setSimId( simId );
        p.setSimVersionId( simVersionId );
        p.setOrgId(orgId);
        p.setSuborgId(suborgId);
        p.setIpCountry(ipCountry);
        p.setProductId(productId);
    }



    public List<TestEventScore> getTestEventScoreList( int testEventScoreTypeId )
    {
        List<TestEventScore> out = new ArrayList<>();

        if( this.testEventScoreList==null )
            return out;

        for( TestEventScore tes : this.testEventScoreList )
        {
            if( tes.getTestEventScoreTypeId() == testEventScoreTypeId )
                out.add( tes );
        }

        return out;
    }

    @Override
    public String toString() {
        return "TestEvent{" + "testEventId=" + testEventId + ", testEventStatusTypeId=" + testEventStatusTypeId + ", testKeyId=" + testKeyId + ", productId=" + productId + ", simId=" + simId + ", percentComplete=" + percentComplete + ", lastAccessDate=" + lastAccessDate + '}';
    }

    public ScoreColorSchemeType getScoreColorSchemeType()
    {
        return ScoreColorSchemeType.getValue(scoreColorSchemeTypeId);
    }
    
    public ScoreFormatType getScoreFormatType()
    {
        return ScoreFormatType.getValue( scoreFormatTypeId );
    }


    public boolean getIsSurveyEvent()
    {
        return surveyEvent!=null;
    }

    public TestEventScore getOverallTestEventScore()
    {
        if( this.testEventScoreList == null )
            return null;

        for( TestEventScore tes : this.testEventScoreList )
        {
            if( tes.getTestEventScoreType().equals( TestEventScoreType.OVERALL ) )
                return tes;
        }

        return null;
    }

    public ScoreCategoryType getScoreCategoryType()
    {
        return ScoreCategoryType.getType( overallRating );
    }


    public TestEventArchive getTestEventArchive()
    {
        TestEventArchive tea = new TestEventArchive();

        tea.setTestEventArchiveId(testEventArchiveId);
        tea.setLastAccessDate(lastAccessDate);
        tea.setLocaleStr(localeStr);
        tea.setLocaleStrReport(localeStrReport);
        tea.setCorpId(corpId);
        tea.setSkinId(skinId);
        tea.setOrgId(orgId);
        tea.setSuborgId(suborgId);
        tea.setOverallRating(overallRating);
        tea.setOverallScore(overallScore);
        tea.setOverallPercentile(overallPercentile);
        tea.setAccountPercentile(accountPercentile);
        tea.setCountryPercentile(countryPercentile);
        tea.setOverallPercentileCount(overallPercentileCount);
        tea.setAccountPercentileCount(accountPercentileCount);
        tea.setCountryPercentileCount(countryPercentileCount);
        tea.setPercentileCountry(percentileCountry);
        tea.setExcludeFmNorms(excludeFmNorms);
        tea.setPercentComplete(percentComplete);
        tea.setBatteryId(batteryId);
        tea.setProductId(productId);
        tea.setProductTypeId(productTypeId);
        tea.setPin(pin);
        tea.setSimId(simId);
        tea.setSimVersionId(simVersionId);
        tea.setReportId(reportId);
        tea.setCreditId(creditId);
        tea.setCreditsUsed(creditsUsed);
        tea.setResultXml(resultXml);
        tea.setStartDate(startDate);
        tea.setScoreFormatTypeId(scoreFormatTypeId);
        tea.setScoreColorSchemeTypeId( scoreColorSchemeTypeId );
        tea.setTestEventId(testEventId);
        tea.setTestEventStatusTypeId(testEventStatusTypeId);
        tea.setTestKeyId(testKeyId);
        tea.setUserId(userId);
        tea.setProctorUserId(proctorUserId);
        tea.setReleaseCode(releaseCode);
        tea.setExtRef(extRef);
        tea.setErrorTxt( errorTxt );
        tea.setEducTypeId(educTypeId);
        tea.setExperTypeId(experTypeId);
        tea.setTrainTypeId(trainTypeId);
        tea.setThirdPartyTestEventId(thirdPartyTestEventId);
        tea.setThirdPartyTestAccountId(thirdPartyTestAccountId);

        tea.setUserAgentId(userAgentId);
        tea.setUserAgent(userAgent);
        tea.setIpAddress(ipAddress);
        tea.setIpCountry(ipCountry);
        tea.setIpState(ipState);
        tea.setIpCity(ipCity);
        tea.setGeographicRegionId(geographicRegionId);

        tea.setTextStr1(textStr1 );

        tea.setTotalTestTime(totalTestTime);
        tea.setStdHraScoring(stdHraScoring);

        return tea;
    }


    public TestEventScore getTestEventScoreForReportId( long reportId, String langStr )
    {
        if( testEventScoreList==null )
            return null;
        
        if( langStr==null || langStr.trim().isEmpty() )
            langStr = Locale.US.toString();
        
        langStr = langStr.trim();
        
        for( TestEventScore tes : testEventScoreList )
        {
            if( tes.getTestEventScoreType().getIsReport() && tes.getReportId()==reportId )
            {
                // no language in TES, Use it.
                if( tes.getStrParam1()==null || tes.getStrParam1().isEmpty()  )
                    return tes;
                
                // tes has a language, must match this language.
                if( tes.getStrParam1().equals(langStr) )
                    return tes;
            }
        }

        return null;
    }

    public boolean getUseBellGraphs()
    {
        return this.stdHraScoring==1;
    }
    
    public void appendErrorTxt( String t )
    {
        if( t == null )
            return;

        if( errorTxt == null )
            errorTxt = t;

        else
            errorTxt = t + "\n" + errorTxt;

        if( errorTxt != null && errorTxt.length()>1000 )
            errorTxt = errorTxt.substring(0,1000 );
    }




    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + (int) (this.testEventId ^ (this.testEventId >>> 32));
        return hash;
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestEvent other = (TestEvent) obj;
        if (this.testEventId != other.testEventId) {
            return false;
        }
        return true;
    }


    public synchronized void initScoreAndResponseLists( boolean validItemsCanHaveZeroMaxPoints ) throws Exception
    {
        simCompetencyScoreList = new ArrayList<>();
        simletScoreList = new ArrayList<>();
        autoScorableResponseList = new ArrayList<>();
        allResponseList = new ArrayList<>();

        multCompetenciesPerItemTypeId = simXmlObj.getMulticompetenciestypeid();
        
        textTitleSequenceId=0;


        if( simXmlObj == null )
            throw new ScoringException( "TestEvent.initScoreAndResponseLists() simXmlObj is null." , ScoringException.NON_PERMANENT , this );

        for( SimJ.Simcompetency sc : simXmlObj.getSimcompetency() )
        {
            simCompetencyScoreList.add( SimScoreObjFactory.createSimCompetencyScore( sc, this, simXmlObj.getUsetotalitemsforcompcalcs()==1, validItemsCanHaveZeroMaxPoints ) );
        }

        getMultiCompetenciesPerItemType().addOtherSimCompetencies( simCompetencyScoreList );

        for( SimJ.Simlet sl : simXmlObj.getSimlet() )
        {
            simletScoreList.add( SimScoreObjFactory.createSimletScore( sl, this, simXmlObj.getUsetotalitemsforcompcalcs()==1,validItemsCanHaveZeroMaxPoints ) ); //   SimletScore( sl, this, simXmlObj.getUsetotalitemsforcompcalcs()==1,validItemsCanHaveZeroMaxPoints ) );
        }

        // If not a sim, return at this point. No result XML. 
        if( !product.getProductType().getUsesImoResultXml() )
            return;
        
        if( resultXmlObj == null )
            throw new ScoringException( "TestEvent.initScoreAndResponseLists() resultXmlObj is null." , ScoringException.PERMANENT , this );

        IactnResp iactnResp;

        List<ScorableResponse> srl = new ArrayList<>();

        // boolean foundIt;

        // LogService.logIt( "TestEvent.initScoreAndResponseLists() simCompetencyScoreList=" + simCompetencyScoreList.size() + ", simletScoreList=" +  simletScoreList.size() +  " starting with Intn / clip responses in REsult XML: " + resultXmlObj.getHistory().getIntnOrClip().size() + ", intns in SimJ=" + simXmlObj.getIntn().size()  + ", validItemsCanHaveZeroMaxPoints=" + validItemsCanHaveZeroMaxPoints );

        SimJ.Intn intnObj;

        Clicflic.History.Intn intRespObjO;
        IntnHist intRespObj;
        
        if( resultXmlObj.getHistory()==null )
        {
            throw new ScoringException( "TestEvent.initScoreAndResponseLists() resultXmlObj has no History element." , ScoringException.PERMANENT , this );
        }
        
        // get all interaction responses
        for( Object o : resultXmlObj.getHistory().getIntnOrClip())
        {
            // Skip Clips. Only want Interactions
            if( o instanceof Clicflic.History.Clip )
                continue;
            
            intRespObjO = (Clicflic.History.Intn)o;
            
            if( intRespObjO == null )
                continue;
            
            intRespObj = new IntnHist(intRespObjO);

            // foundIt = false;

            intnObj = null;

            // Next, look first by unique ids - this implies that the SimJ object has changed a bit. So be sure to use the
            if( intRespObj.getUnqid()!=null && !intRespObj.getUnqid().isEmpty() )
            {
                // LogService.logIt( "TestEvent.initScoreAndResponseLists() Seeking Sim.intn by uniqueId=" + intRespObj.getUnqid()  );
                int ct = 0;
                SimJ.Intn ii=null;
                
                for( SimJ.Intn intn : simXmlObj.getIntn() )
                {
                    if( intn.getUniqueid()!=null && !intn.getUniqueid().isEmpty() && intn.getUniqueid().equals( intRespObj.getUnqid() ) )
                    {
                        ii=intn;
                        ct++;
                        // LogService.logIt( "TestEvent.initScoreAndResponseLists() FOUND Sim.intn by uniqueId=" + intRespObj.getUnqid()  );
                        //intnObj = intn;
                        // foundIt = true;
                        //break;
                    }
                }
                if( ii!=null && ct==1 )
                    intnObj = ii;
            }

            // if not found by unique id, then look by nodeseq
            if( intnObj==null )
            {
                // Find the matching Interaction
                for( SimJ.Intn intn : simXmlObj.getIntn() )
                {
                    if( intn.getSeq() == intRespObj.getNdseq() )
                    {
                        intnObj = intn;
                        // foundIt = true;
                        break;
                    }
                }
            }

            // Interaction still not found in SimXmlObj - check to see if it is a resume intn.
            if( intnObj==null && intRespObj.getUnqid()!=null && !intRespObj.getUnqid().isBlank() )
                intnObj = Ct5ResumeUtils.getResumeIntnByUniqueId(intRespObj.getUnqid());
            
            
            // Interaction not found in SimXmlObj - skip it.
            if( intnObj==null )
            {
                LogService.logIt( "TestEvent.initScoreAndResponseLists() Unable to find IntnObj in SimJ for intnRespObj.ndSeq=(" + intRespObj.getNdseq() +") validItemsCanHaveZeroMaxPoints=" + validItemsCanHaveZeroMaxPoints );
                continue;
            }

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "TestEvent.initScoreAndResponseLists() STARTING For Interaction unique=" + intnObj.getUniqueid() + " (" + intRespObj.getNdseq() +") validItemsCanHaveZeroMaxPoints=" + validItemsCanHaveZeroMaxPoints );
            
            // create object
            iactnResp = IactnRespFactory.getIactnResp(intRespObjO, intnObj, simXmlObj, this ); //   new IactnResp( intRespObj );

            iactnResp.init( simXmlObj , simletScoreList, this, validItemsCanHaveZeroMaxPoints );

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "TestEvent.initScoreAndResponseLists() have " + iactnResp.toString() + ", autoscorable="+ iactnResp.isAutoScorable() );

            // if this item is not found in SimXMLObj, it must not be important so ignore it.
            if( iactnResp.getIntnObj()==null )
            {
                LogService.logIt( "TestEvent.initScoreAndResponseLists() iactnResp.intnObj is null! testEventId=" + testEventId + ", " + iactnResp.toString() );
                continue;
            }

            // if this item has no simlet score, it must be from a sim template and is ignored for scoring and reports.
            if( iactnResp.getSimletScore()==null )
            {
                if( iactnResp.getIntnObj().getSimletid()> 0 )
                    throw new ScoringException( "TestEvent.initScoreAndResponseLists() adding " + iactnResp.toString() + " simletId in IntnObj is " + iactnResp.getIntnObj().getSimletid() + ", but this simlet was not found." , ScoringException.PERMANENT , this );

                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "TestEvent.initScoreAndResponseLists() iactnResp.simletScore is null! testEventId=" + testEventId + ", " + iactnResp.toString() );
                continue;
            }

            // LogService.logIt( "TestEvent.initScoreAndResponseLists() BBB.3 iactnResp.isAutoScorable()=" + iactnResp.isAutoScorable() );

            srl.clear();

            // Add previous before adding the real thing
            if( iactnResp.hasMultipleIactnLevelScores() && iactnResp.isAutoScorable() )
                srl.addAll( iactnResp.getPrevIactRespList() );

            if( iactnResp.isAutoScorable() )
                srl.add( iactnResp );
            
            // add to the 'All' List since not part of srl (added below)
            else
                allResponseList.add( iactnResp );

            srl.addAll( iactnResp.getAllScorableRadioButtonGroupResponses() );

            // items can also contain interaction items that are scorable by themselves, so add them here as scorable responses if applicable
            srl.addAll( iactnResp.getAllScorableIntItemResponses() );

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "TestEvent.initScoreAndResponseLists() Interaction " + iactnResp.getIntnObj().getName() + " (" + iactnResp.getIntnObj().getSeq() + ") has yielded a total of " + srl.size() + " scorable responses. iactnResp.getAllScorableIntItemResponses()=" + iactnResp.getAllScorableIntItemResponses().size() + ", iactnResp.isAutoScorable()=" + iactnResp.isAutoScorable() +", iactnResp.getAllScorableRadioButtonGroupResponses()=" + iactnResp.getAllScorableRadioButtonGroupResponses().size() );

            // add to the 'All' List
            // allResponseList.add( iactnResp );

            allResponseList.addAll( srl );
            
            autoScorableResponseList.addAll( srl );

            // this can happen when the interaction node is configured to allow multiple exposurs of the same item to be scored.
            //if( iactnResp.hasMultipleIactnLevelScores() )
            //{
            //    allResponseList.addAll( iactnResp.getPrevIactRespList() );
            //}

            // if auto scorable, add to that list
            //if( iactnResp.isAutoScorable() )
           //     autoScorableResponseList.add( iactnResp );

            // items that have radiobuttongroup responses should also be included.
            //srl.addAll( iactnResp.getAllScorableRadioButtonGroupResponses() );
        }

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "TestEvent.initScoreAndResponseLists() END testEventId=" + testEventId + ", allResponseList.size=" + allResponseList.size() + " autoScorableResponseList.size=" + autoScorableResponseList.size()  );
    }



    public int getNextTextTitleSequenceId()
    {
        textTitleSequenceId++;
        
        return textTitleSequenceId;
    }


    public MultiCompetenciesPerItemType getMultiCompetenciesPerItemType()
    {
        return MultiCompetenciesPerItemType.getValue( multCompetenciesPerItemTypeId );
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

    public int getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(int batteryId) {
        this.batteryId = batteryId;
    }

    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
    }

    public long getCreditId() {
        return creditId;
    }

    public void setCreditId(long creditId) {
        this.creditId = creditId;
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

    public float getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    public long getProctorUserId() {
        return proctorUserId;
    }

    public void setProctorUserId(long proctorUserId) {
        this.proctorUserId = proctorUserId;
    }

    public int getReleaseCode() {
        return releaseCode;
    }

    public void setReleaseCode(int releaseCode) {
        this.releaseCode = releaseCode;
    }

    public String getResultXml() {
        return resultXml;
    }

    public void setResultXml(String resultXml) {
        this.resultXml = resultXml;
    }

    public int getSkinId() {
        return skinId;
    }

    public void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public SimDescriptor getSimDescriptor() {
        return simDescriptor;
    }

    public void setSimDescriptor(SimDescriptor simDescriptor) {
        this.simDescriptor = simDescriptor;
    }

    public Clicflic getResultXmlObj() {
        return resultXmlObj;
    }

    public void setResultXmlObj(Clicflic resultXmlObj) {
        this.resultXmlObj = resultXmlObj;
    }

    public SimJ getSimXmlObj() {
        return simXmlObj;
    }

    public void setSimXmlObj(SimJ simXmlObj) {
        this.simXmlObj = simXmlObj;
    }

    public List<ScorableResponse> getAllResponseList() {
        return allResponseList;
    }


    //public void setAllResponseList(List<ScorableResponse> allResponseList) {
    //    this.allResponseList = allResponseList;
    //}


    public List<ScorableResponse> getAutoScorableResponseList() {
        return autoScorableResponseList;
    }

    //public void setAutoScorableResponseList(List<ScorableResponse> autoScorableResponseList) {
    //    this.autoScorableResponseList = autoScorableResponseList;
    //}

    public List<SimCompetencyScore> getSimCompetencyScoreList() {
        return simCompetencyScoreList;
    }

    public void setSimCompetencyScoreList(List<SimCompetencyScore> simCompetencyScoreList) {
        this.simCompetencyScoreList = simCompetencyScoreList;
    }

    public List<SimletScore> getSimletScoreList() {
        return simletScoreList;
    }

    public void setSimletScoreList(List<SimletScore> simletScoreList) {
        this.simletScoreList = simletScoreList;
    }

    public long getTestEventArchiveId() {
        return testEventArchiveId;
    }

    public void setTestEventArchiveId(long testEventArchiveId) {
        this.testEventArchiveId = testEventArchiveId;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public List<TestEventScore> getTestEventScoreList() {
        return testEventScoreList;
    }

    public void setTestEventScoreList(List<TestEventScore> testEventScoreList) {
        this.testEventScoreList = testEventScoreList;
    }

    public TestKey getTestKey() {
        return testKey;
    }

    public void setTestKey(TestKey testKey) {
        this.testKey = testKey;
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

    @Override
    public String getErrorTxt() {
        return errorTxt;
    }

    @Override
    public void setErrorTxt(String e) {

        if( e!=null && e.length() > 999 )
            e = StringUtils.truncateString( e, 999 );
        
        this.errorTxt = e;
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

    public int getMultCompetenciesPerItemTypeId() {
        return multCompetenciesPerItemTypeId;
    }

    public void setMultCompetenciesPerItemTypeId(int multCompetenciesPerItemTypeId) {
        this.multCompetenciesPerItemTypeId = multCompetenciesPerItemTypeId;
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

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public Report getReport2() {
        return report2;
    }

    public void setReport2(Report report2) {
        this.report2 = report2;
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

    public Suborg getSuborg() {
        return suborg;
    }

    public void setSuborg(Suborg suborg) {
        this.suborg = suborg;
    }

    public SurveyEvent getSurveyEvent() {
        return surveyEvent;
    }

    public void setSurveyEvent(SurveyEvent surveyEvent) {
        this.surveyEvent = surveyEvent;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
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

    public List<Profile> getAltScoreProfileList() {
        return altScoreProfileList;
    }

    public void setAltScoreProfileList(List<Profile> altScoreProfileList) {
        this.altScoreProfileList = altScoreProfileList;
    }

    public RemoteProctorEvent getRemoteProctorEvent() {
        return remoteProctorEvent;
    }

    public void setRemoteProctorEvent(RemoteProctorEvent remoteProctorEvent) {
        this.remoteProctorEvent = remoteProctorEvent;
    }

    public boolean getPartialBatteryTestEvent() {
        return partialBatteryTestEvent;
    }

    public void setPartialBatteryTestEvent(boolean partialBatteryTestEvent) {
        this.partialBatteryTestEvent = partialBatteryTestEvent;
    }

    public int getScoreColorSchemeTypeId() {
        return scoreColorSchemeTypeId;
    }

    public void setScoreColorSchemeTypeId(int scoreColorSchemeTypeId) {
        this.scoreColorSchemeTypeId = scoreColorSchemeTypeId;
    }

    public Report getReport3() {
        return report3;
    }

    public void setReport3(Report report3) {
        this.report3 = report3;
    }

    public List<TestEventResponseRating> getTestEventResponseRatingList() {
        return testEventResponseRatingList;
    }

    public void setTestEventResponseRatingList(List<TestEventResponseRating> testEventResponseRatingList) {
        this.testEventResponseRatingList = testEventResponseRatingList;
    }

    public int getStdHraScoring() {
        return stdHraScoring;
    }

    public void setStdHraScoring(int stdHraScoring) {
        this.stdHraScoring = stdHraScoring;
    }

}
