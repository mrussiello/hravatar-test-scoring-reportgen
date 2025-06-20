/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.api;

import com.tm2score.av.AvEventFacade;
import com.tm2score.custom.bestjobs.BestJobsReportUtils;
import com.tm2score.custom.bestjobs.EeoMatch;
import com.tm2score.custom.coretest2.cefr.CefrScoreType;
import com.tm2score.entity.ai.MetaScore;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.file.UploadedUserFile;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.file.FileUploadFacade;
import com.tm2score.file.MediaTempUrlSourceType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyVisibilityType;
import com.tm2score.sim.SimJUtils;
import com.tm2score.user.EthnicCategoryType;
import com.tm2score.user.RacialCategoryType;
import com.tm2score.user.ReportDownloadType;
import com.tm2score.user.UserFacade;
import com.tm2score.util.Base64Encoder;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.JaxbUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Mike
 */
public class AssessmentStatusCreator {

    UserFacade userFacade;

    // @Inject
    EventFacade eventFacade;

    FileUploadFacade fileUploadFacade;
    AvEventFacade avEventFacade;


    SimJUtils equivSimJUtils;

    Locale rptLocale;

    /**
     * Creates a new instance of AssessmentOrderResource
     */
    public AssessmentStatusCreator() {
    }

    public String getAssessmentResultFromTestKey( AssessmentResult arr,
                                                  TestKey testKey,
                                                  int includeScoreCode,
                                                  String reportLanguage,
                                                  String reportTitle,
                                                  byte[] reportBytes) throws Exception
    {

        try
        {
            if( testKey==null )
                throw new Exception( "TestKey is null" );

            AssessmentResult.AssessmentStatus aoas = new AssessmentResult.AssessmentStatus();

            aoas.setStatusDate( getXmlDate( new GregorianCalendar() ) );

            aoas.setStatusCode( testKey.getTestKeyStatusTypeId() );

            arr.setAssessmentStatus(aoas);

            // LogService.logIt("AssessmentStatusCreator.getAssessmentResultFromTestKey() START " + testKey.toString() );

            int orgId = testKey.getOrgId();

            AssessmentResult.ClientId aoac = new AssessmentResult.ClientId();
            arr.setClientId(aoac);
            aoac.setIdValue( createIdValue( "OrgId", EncryptUtils.urlSafeEncrypt( orgId ) ));

            AssessmentResult.ClientOrderId aoaco = new AssessmentResult.ClientOrderId();
            arr.setClientOrderId(aoaco);
            aoaco.setIdValue( createIdValue( "OrderId", testKey.getExtRef() ) );

            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            User authUser = userFacade.getUser( testKey.getAuthorizingUserId() );

            if( authUser == null )
                throw new Exception( "Cannot find authUser " + testKey.getAuthorizingUserId() );

            Org org = testKey.getOrg();
            if( org==null )
                org = userFacade.getOrg( testKey.getOrgId() );

            Suborg suborg = testKey.getSuborg();
            if( suborg==null && testKey.getSuborgId()>0)
            {
                suborg=userFacade.getSuborg(testKey.getSuborgId());
                testKey.setSuborg(suborg);
            }

            Report report = null;

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            // LogService.logIt("AssessmentStatusCreator.getAssessmentResultFromTestKey() " + testKey.toString() );

            AssessmentResult.UserArea aoaUA = new AssessmentResult.UserArea();

            arr.setUserArea( aoaUA );

            aoaUA.setTestKey( EncryptUtils.urlSafeEncrypt( testKey.getTestKeyId() ) );

            List<IdValue> idvl = new ArrayList<>();

            Product product = eventFacade.getProduct( testKey.getProductId() );

            idvl.add( this.createIdValue( "AssessmentId" , EncryptUtils.urlSafeEncrypt( product.getProductId() ) ) );
            idvl.add( this.createIdValue( "AssessmentStatusTypeId" , Integer.toString( testKey.getTestKeyStatusTypeId() ) ) );
            idvl.add( this.createIdValue( "AssessmentStatusTypeName" , testKey.getTestKeyStatusType().getKey() ) );

            //Added for HRNX change and duplicate logic.
            String duplicateOrderId;
            for( int i=1;i<=20;i++ )
            {
                duplicateOrderId = testKey.getCustomParameterValue( "dupordid" + i );

                if( duplicateOrderId!=null && !duplicateOrderId.isEmpty() )
                    idvl.add( this.createIdValue( "DuplicateClientOrderId" + i , duplicateOrderId ) );
                else
                    break;
            }


            aoaUA.idValue = idvl;

            AssessmentResult.AssessmentSubject aoau = new AssessmentResult.AssessmentSubject();
            arr.setAssessmentSubject(aoau);

            AssessmentResult.AssessmentSubject.SubjectId aoauSid = new AssessmentResult.AssessmentSubject.SubjectId();
            AssessmentResult.AssessmentSubject.PersonName aoauNm = new AssessmentResult.AssessmentSubject.PersonName();
            AssessmentResult.AssessmentSubject.ContactMethod aoauEm = new AssessmentResult.AssessmentSubject.ContactMethod();
            AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors aoauDemo = new AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors();

            // aoaUA.setAssessmentStatusTypeId( testKey.getTestKeyStatusTypeId() );

            User user = null;

            if( testKey.getCustom1()!=null && !testKey.getCustom1().isEmpty() )
                aoauSid.getIdValue().add( createIdValue( "customfield1" , testKey.getCustom1() ) );
            if( testKey.getCustom2()!=null && !testKey.getCustom2().isEmpty() )
                aoauSid.getIdValue().add( this.createIdValue( "customfield2" , testKey.getCustom2() ) );
            if( testKey.getCustom3()!=null && !testKey.getCustom3().isEmpty() )
                aoauSid.getIdValue().add( this.createIdValue( "customfield3" , testKey.getCustom3() ) );

            if( testKey.getUserId()>0 )
            {
                user = userFacade.getUser( testKey.getUserId() );

                if( user == null )
                    throw new Exception( "User not found. " + testKey.toString() );

                // user.setResume( userFacade.getResumeForUser( testKey.getUserId() ));
                
                aoauSid.getIdValue().add( this.createIdValue( "userid" , EncryptUtils.urlSafeEncrypt( user.getUserId() ) ) );

                if( user.getExtRef()!=null && !user.getExtRef().isEmpty() )
                    aoauSid.getIdValue().add( this.createIdValue( "userreferenceid" , user.getExtRef() ) );

                aoau.setSubjectId(aoauSid);

                aoauNm.setGivenName( user.getFirstName() );
                aoauNm.setGivenName( user.getLastName() );

                aoau.setPersonName(aoauNm);

                if( user.getEmail()!= null && user.getEmail().isEmpty() && user.getUserType().getNamed() )
                {
                    aoauEm.setInternetEmailAddress(user.getEmail() );
                    aoau.setContactMethod(aoauEm);
                }

                if( user.getHasDemoInfo() || user.getGenderTypeId()>0 || user.getBirthYear()>0 || user.getCountryCode()!=null && !user.getCountryCode().isEmpty()  )
                {
                    AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors.BiologicalDescriptors aoauDemoBio = new AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors.BiologicalDescriptors();

                    if( user.getGenderTypeId()> 0 )
                    {
                        aoauDemoBio.setGenderCode( user.getGenderTypeId() );
                        aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                    }

                    if( user.getBirthYear()>0 )
                    {
                        GregorianCalendar gc = new GregorianCalendar( user.getBirthYear(), 0, 1 );
                        aoauDemoBio.setBirthDate( getXmlDate( gc.getTime() ) );
                        aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                    }

                    if( user.getRacialCategories() != null && user.getRacialCategories().length()> 0 )
                    {
                        StringBuilder sb = new StringBuilder();

                        RacialCategoryType rct;

                        for( int rc : user.getRacialCategoryIdList() )
                        {
                            rct = RacialCategoryType.getType(rc);

                            if( rct==null )
                                continue;

                            if( sb.length()>0 )
                                sb.append(",");

                            sb.append( rct.getName( Locale.US ) );
                        }

                        if( sb.length()> 0 )
                        {
                            aoauDemoBio.setRaceCode( sb.toString() );
                            aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                        }
                    }

                    if( user.getEthnicCategoryId()>0 )
                    {
                        EthnicCategoryType ect = EthnicCategoryType.getType( user.getEthnicCategoryId() );

                        if( ect != null )
                        {
                            aoauDemoBio.setEnthicityCode( ect.getName( Locale.US ) );
                            aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                        }
                    }

                    if( user.getCountryCode()!=null && !user.getCountryCode().isEmpty() )
                    {
                        aoauDemoBio.setNationalityCode(user.getCountryCode());
                        aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                    }

                    if( user.getLocaleStr()!=null && !user.getLocaleStr().isBlank() )
                        aoauDemoBio.setLanguageCode( user.getLocaleStr() );
                }
            }



            if( aoauDemo.getBiologicalDescriptors()!=null )
                aoau.setAssessmentPersonDescriptors(aoauDemo);

            arr.setAssessmentStatusTypeId( testKey.getTestKeyStatusTypeId() );
            arr.setAssessmentStatusTypeName( testKey.getTestKeyStatusType().getKey() );


            boolean scoreReady = testKey.getTestKeyStatusTypeId()>= TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() && testKey.getTestKeyStatusTypeId()<= TestKeyStatusType.DISTRIBUTION_ERROR.getTestKeyStatusTypeId();

            // LogService.logIt("AssessmentStatusCreator.getAssessmentResultFromTestKey() scoreReady=" + scoreReady + ", testKeyStatusTypeId=" + testKey.getTestKeyStatusTypeId() );

            float percentComplete = testKey.getTestKeyStatusType().getIsCompleteOrHigher() ? 100f : computePercentComplete( testKey );

            aoas.setLastAccessDate( getXmlDate( testKey.getLastAccessDate() ) );

            aoas.setPercentComplete(percentComplete);
            aoas.setStatusName( testKey.getTestKeyStatusType().getName() );


            // Incomplete, Complete, Scored, API Error, Other Error
            if( testKey.getTestKeyStatusTypeId()<=TestKeyStatusType.STARTED.getTestKeyStatusTypeId() )
               aoas.setStatus( "Incomplete" );
            else if( testKey.getTestKeyStatusTypeId()<= TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId()  )
               aoas.setStatus( "Complete" );
            else if( scoreReady )
               aoas.setStatus( "Scored" );
            else
            {
                aoas.setStatus( "Other Error" );
                aoas.setDetails( "Error: " + testKey.getTestKeyStatusType().getKey() );
                aoas.setErrorMessage( "Error: " + testKey.getTestKeyStatusType().getKey() + " testKeyStatusTypeId=" + testKey.getTestKeyStatusTypeId() );

                if( testKey.getTestKeyStatusType().equals( TestKeyStatusType.SCORE_ERROR ) ||  testKey.getTestKeyStatusType().equals( TestKeyStatusType.REPORT_ERROR ) )
                    aoas.setErrorCode( 206 );
                else if( testKey.getTestKeyStatusType().equals( TestKeyStatusType.EXPIRED ) ||  testKey.getTestKeyStatusType().equals( TestKeyStatusType.DEACTIVATED ) )
                    aoas.setErrorCode( 205 );
                else
                    aoas.setErrorCode( 100 );
            }

            // Always include Battery Id to indicate this is a Battery.
            if( testKey.getBatteryId()>0 && product.getProductType().getIsAnyBattery() )
                aoas.setBatteryId( testKey.getBatteryId() );

            String resultsViewUrl = null;

            Battery b = null;
            String userAgent = null;

            if( includeScoreCode>0 && scoreReady )
            {
                boolean hideFlag;
                boolean hasBattScore=false;

                if( testKey.getBatteryId()>0 && product.getProductType().getIsAnyBattery() )
                {
                    b = eventFacade.getBattery( testKey.getBatteryId() );

                    if( b!=null && b.getBatteryScoreType().needsScore() )
                    {
                        // BatteryFacade batteryFacade = BatteryFacade.getInstance();
                        BatteryScore bs = eventFacade.getBatteryScoreForTestKey(testKey.getTestKeyId());

                        if( bs==null )
                        {
                            LogService.logIt( "AssessmentStatusCreator.getAssessmentResultFromTestKey() NONFATAL ERROR Battery batteryId=" + b.getBatteryId() + " requires a BatteryScore but none found. Could be from a change. testKeyId=" + testKey.getTestKeyId() );
                            // throw new Exception( "Cannot find BatteryScore for battery TestKeyId=" + testKey.getTestKeyId() );
                        }
                        else
                        {
                            hasBattScore=true;
                            aoas.setBatteryOverallScore( bs.getScore() );
                        }

                        //resultsViewUrl = RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/r.xhtml?b=" + testKey.getTestKeyIdEncrypted();

                        // resultsViewUrl = RuntimeConstants.getStringValue("adminappbasuri") + "/r.xhtml?b=" + testKey.getTestKeyIdEncrypted();

                        //if( org!=null && org.getReportDownloadTypeId()==ReportDownloadType.ALL_OK.getReportDownloadTypeId() )
                        //    resultsViewUrl += "&am=SR";

                        //resultsViewUrl += "&r=0";

                        //aoas.setBatteryResultsViewUrl( resultsViewUrl );
                    }

                    if( b!=null )
                    {
                        resultsViewUrl = RuntimeConstants.getStringValue("adminappbasuri") + "/r.xhtml?b=" + testKey.getTestKeyIdEncrypted();

                        if( org!=null && org.getReportDownloadTypeId()==ReportDownloadType.ALL_OK.getReportDownloadTypeId() )
                            resultsViewUrl += "&am=SR";

                        resultsViewUrl += "&r=0";

                        aoas.setBatteryResultsViewUrl( resultsViewUrl );
                    }
                }

                // byte[] newReport = null;

                if( testKey.getTestEventList()== null )
                    testKey.setTestEventList( eventFacade.getTestEventsForTestKeyId( testKey.getTestKeyId(), true) );

                List<TestEvent> tel = testKey.getTestEventList(); // eventFacade.getTestEventsForTestKeyId(testKey.getTestKeyId(), true );

                if( tel.size()<1 )
                    throw new Exception( "No TestEvent found for TestKey " + testKey.getTestKeyId( ) );

                AssessmentResult.Results rslts;

                List<TestEventScore> tesl;
                Product teProduct;

                TestEventScore otes;
                List<TestEventScore> rptTesl;

                List<AssessmentResult.Results> aoaRl = new ArrayList<>();
                arr.results = aoaRl;

                setReportLocale( testKey, tel );

                String scrTxt;

                List<UploadedUserFile> uufList;

                Locale teLocale;

                String riasec;
                String eeoc;

                BestJobsReportUtils bjru = null;

                for( TestEvent te : tel )
                {
                    equivSimJUtils = null;

                    if( te.getUserAgent()!=null && !te.getUserAgent().isBlank() )
                        userAgent = te.getUserAgent();

                    if( te.getTestEventStatusTypeId() != TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId() )
                        throw new Exception( "Test Event is not in proper status for reporting results (120) " + te.toString() );

                    resultsViewUrl = RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/r.xhtml?t=" + te.getTestEventIdEncrypted();

                    if( org!=null && org.getReportDownloadTypeId()==ReportDownloadType.ALL_OK.getReportDownloadTypeId() )
                        resultsViewUrl += "&am=SR";

                    resultsViewUrl += "&r=0";

                    report = te.getReport();
                    if( report==null && te.getReportId()>0 )
                    {
                        report = eventFacade.getReport( te.getReportId() );
                        te.setReport(report);
                    }

                    tesl = eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true );

                    teProduct = eventFacade.getProduct( te.getProductId() );

                    riasec = null;
                    eeoc = null;

                    // if want in english but test is not in english but product references an equivalent simId (p.long4)
                    if( teProduct.getProductType().getIsSimOrCt5Direct() &&
                        rptLocale.getLanguage().equalsIgnoreCase("en") &&
                        !teProduct.getLocaleFmLangStr().getLanguage().equalsIgnoreCase( rptLocale.getLanguage() )
                        && teProduct.getLongParam4()>0 )
                    {
                        SimDescriptor sd = eventFacade.getSimDescriptor( teProduct.getLongParam4(), -1, true );
                        equivSimJUtils = new SimJUtils( JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() ) );
                    }

                    teLocale = teProduct.getLocaleFmLangStr();

                    if( teLocale==null )
                        teLocale = te.getLocaleStr() ==null || te.getLocaleStr().isEmpty() ? Locale.US : I18nUtils.getLocaleFromCompositeStr( te.getLocaleStr() );

                    uufList = getFullUploadedUserFileList( te.getTestEventId() );

                    rslts = new AssessmentResult.Results();
                    rslts.setProfile( teProduct.getName() );
                    rslts.setProfileEnglish( teProduct.getNameEnglish() );
                    rslts.setLangStr( rptLocale.toString() );
                    rslts.setAssessmentId( EncryptUtils.urlSafeEncrypt( teProduct.getProductId() ) );

                    aoaRl.add(rslts);

                    Collections.sort( tesl );

                    otes = null;

                    rptTesl = new ArrayList<>();

                    for( TestEventScore tes : tesl )
                    {
                        if( tes.getTestEventScoreType().getIsReport() )
                        {
                            rptTesl.add( tes );
                            if( riasec==null || riasec.isBlank() )
                                riasec = StringUtils.getBracketedArtifactFromString(tes.getTextParam1(), Constants.RIASEC_COMPACT_INFO_KEY);
                            if( eeoc==null || eeoc.isBlank() )
                                eeoc = StringUtils.getBracketedArtifactFromString(tes.getTextParam1(), Constants.EEOCAT_COMPACT_INFO_KEY);
                        }
                        else if( tes.getTestEventScoreTypeId()==TestEventScoreType.OVERALL.getTestEventScoreTypeId() )
                            otes = tes;
                    }

                    GregorianCalendar gcal = new GregorianCalendar();

                    gcal.setTime( te.getLastAccessDate() );

                    rslts.setAssessmentCompleteDate( getXmlDate( gcal ) );

                    if( otes != null )
                    {
                        AssessmentResult.Results.AssessmentOverallResult overa = new AssessmentResult.Results.AssessmentOverallResult();

                        overa.setProductTypeId(teProduct.getProductTypeId() );
                        overa.setConsumerProductTypeId(teProduct.getConsumerProductTypeId());

                        overa.setScoreNumeric(otes.getScore() );
                        overa.setInterval(otes.getScoreCategoryId() );
                        overa.setResultsViewUrl( resultsViewUrl );

                        if( te.getIpCountry()!=null && !te.getIpCountry().isBlank() )
                            overa.getIdValue().add(createIdValue("IPCountry", te.getIpCountry()));

                        String cefrLevel = StringUtils.getBracketedArtifactFromString( otes.getTextParam1(), Constants.CEFRLEVEL);
                        if( cefrLevel!=null )
                        {
                            if( CefrScoreType.getFromText(cefrLevel).equals(CefrScoreType.UNKNOWN))
                                cefrLevel=null;
                        }
                        if( cefrLevel!=null && !cefrLevel.isBlank() )
                        {
                            overa.getIdValue().add(createIdValue("CEFRLevel", cefrLevel));
                            String cefrText = StringUtils.getBracketedArtifactFromString( otes.getTextParam1(), Constants.CEFRLEVELTEXT);
                            if( cefrText!=null && !cefrText.isBlank() )
                                overa.getIdValue().add(createIdValue("CEFRLevelText", cefrText));
                        }


                        hideFlag =  (report!=null && report.getIncludeOverallScore()==0) || ReportUtils.getReportFlagBooleanValue("ovroff", testKey, teProduct, suborg, org, report );
                        overa.setHideOverall(hideFlag ? 1 : 0);
                        hideFlag = (report!=null && report.getIncludeNumericScores()==0) || ReportUtils.getReportFlagBooleanValue("ovrnumoff", testKey, teProduct, suborg, org, report );
                        overa.setHideOverallNumeric(hideFlag ? 1 : 0);
                        hideFlag = (report!=null && report.getIncludeScoreText()==0) || ReportUtils.getReportFlagBooleanValue("ovrscrtxtoff", testKey, teProduct, suborg, org, report );
                        overa.setHideOverallText(hideFlag ? 1 : 0);

                        hideFlag = (report!=null && report.getIncludeCompetencyScores()==0) || ReportUtils.getReportFlagBooleanValue("cmptysumoff", testKey, teProduct, suborg, org, report );
                        overa.setHideDetail(hideFlag ? 1 : 0);
                        hideFlag = (report!=null && report.getIncludeSubcategoryNumeric()==0) || ReportUtils.getReportFlagBooleanValue("cmptynumoff", testKey, teProduct, suborg, org, report );
                        overa.setHideDetailNumeric(hideFlag ? 1 : 0);
                        hideFlag = (report!=null && report.getIncludeSubcategoryInterpretations()==0) || ReportUtils.getReportFlagBooleanValue("cmptytxtoff", testKey, teProduct, suborg, org, report );
                        overa.setHideDetailText(hideFlag ? 1 : 0);


                        if( otes.getScoreText() != null && !otes.getScoreText().isEmpty() )
                        {
                            scrTxt = otes.getScoreText();

                            if( equivSimJUtils!=null )
                            {
                                String s = equivSimJUtils.getOverallScoreText( otes.getScoreCategoryId(), otes.getScore() );

                                if( s!=null && !s.isEmpty() )
                                    scrTxt = UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );
                            }

                            overa.setScoreText( scrTxt );
                        }

                        if( otes.getHasValidOverallZScoreNorm() && te.getProduct()!=null && te.getProduct().getConsumerProductType().getIsJobSpecific())
                            overa.setPercentileAllApprox( otes.getOverallZScorePercentile() );

                        if( otes.getHasValidOverallNorm() )
                        {
                            overa.setPercentileAll(otes.getPercentile() );
                            overa.setPercentileAllCount(otes.getOverallPercentileCount() );
                        }
                        if( otes.getHasValidCountryNorm())
                        {
                            overa.setPercentileCountry(otes.getCountryPercentile());
                            overa.setPercentileCountryCount(otes.getCountryPercentileCount() );
                        }
                        if( otes.getHasValidAccountNorm() )
                        {
                            overa.setPercentileClient(otes.getAccountPercentile());
                            overa.setPercentileClientCount(otes.getAccountPercentileCount());
                        }

                        setUploadedMediaInfoOverall( testKey.getOrgId(), otes, teLocale.toString(), overa, uufList );

                        rslts.setAssessmentOverallResult(overa);

                        // If not a battery, add the values here to help other systems.
                        // if( testKey.getBatteryId()<=0 ||
                        //     (b!=null && !b.getBatteryScoreType().needsScore() && ( aoas.getBatteryResultsViewUrl()==null || aoas.getBatteryResultsViewUrl().isEmpty() ) ) )
                        // {
                        //     aoas.setBatteryOverallScore( otes.getScore() );
                        //    aoas.setBatteryResultsViewUrl( resultsViewUrl );
                        //}

                        if( testKey.getBatteryId()<=0 || !hasBattScore ) // ( b!=null && !b.getBatteryScoreType().needsScore()) )
                            aoas.setBatteryOverallScore( otes.getScore() );

                        // If nothing placed there yet, put something there.
                        if( aoas.getBatteryResultsViewUrl()==null || aoas.getBatteryResultsViewUrl().isBlank() )
                            aoas.setBatteryResultsViewUrl( resultsViewUrl );

                    }

                    if( testKey.getMetaScoreList()==null )
                        testKey.setMetaScoreList( eventFacade.getReportableMetaScoreListForTestKey(testKey.getTestKeyId() ) );
                    // Add meta scores (aI Scores)
                    if( testKey.getMetaScoreList()!=null && !testKey.getMetaScoreList().isEmpty() )
                    {
                        AssessmentResult.AssessmentStatus.MetaScores aoasmeta;                    
                        for( MetaScore metaScore : testKey.getMetaScoreList() )
                        {
                            aoasmeta = new AssessmentResult.AssessmentStatus.MetaScores();
                            aoasmeta.setType(metaScore.getMetaScoreTypeId());
                            aoasmeta.setScoreNumeric( metaScore.getScore());
                            aoasmeta.setConfidence(metaScore.getConfidence());
                            aoasmeta.setScoreText( metaScore.getScoreText());
                            aoasmeta.setMetaScoreInputTypeIds( metaScore.getMetaScoreInputTypeIds() );
                            aoas.getMetaScores().add( aoasmeta);
                        }
                    }
                    
                    
                    List<AssessmentResult.Results.DetailResult> dtlResLst = new ArrayList<>();

                    AssessmentResult.Results.DetailResult dtl;


                    for( TestEventScore tes : tesl )
                    {
                        if( !tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY) )
                            continue;

                        if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                            continue;

                        dtl = new AssessmentResult.Results.DetailResult();

                        setScoreInfo( tes, dtl );

                        setUploadedMediaInfoCompetency( testKey.getOrgId(), tes, teLocale.toString(), dtl, uufList );

                        dtlResLst.add(dtl);
                    }

                    // Job Matches
                    if( riasec!=null && !riasec.isBlank() )
                    {
                        if( bjru==null )
                            bjru = new BestJobsReportUtils(true);

                        List<Profile> pl = bjru.getBestProfilesListWithData(riasec, ",", 0 );
                        if( pl!=null )
                        {
                            for( Profile pr : pl )
                            {
                                dtl = new AssessmentResult.Results.DetailResult();
                                setRiasecScoreInfo( pr, 500, "Job Match", dtl );
                                dtlResLst.add(dtl);
                            }
                        }
                    }

                    // EEOC Categories
                    if( eeoc!=null && !eeoc.isBlank() )
                    {
                        if( bjru==null )
                            bjru = new BestJobsReportUtils(true);
                        List<EeoMatch> el = BestJobsReportUtils.getEeoCategoryListWithData(eeoc, ",");
                        if( el!=null )
                        {
                            for( EeoMatch er : el )
                            {
                                dtl = new AssessmentResult.Results.DetailResult();
                                setEeocScoreInfo( er, 501, "EEOC Category", dtl );
                                dtlResLst.add(dtl);
                            }
                        }
                    }

                    rslts.detailResult = dtlResLst;

                    if( (includeScoreCode==2 && !rptTesl.isEmpty() ) || includeScoreCode==3  )
                    {
                        String reportLangStr = te.getLocaleStrReport();

                        if( reportLangStr == null || reportLangStr.isEmpty() )
                            reportLangStr = te.getLocaleStr();

                        if( reportLangStr == null || reportLangStr.isEmpty() )
                            reportLangStr = "en_US";

                        AssessmentResult.Results.SupportingMaterials rpts = new AssessmentResult.Results.SupportingMaterials();

                        List<AssessmentResult.Results.SupportingMaterials.ReportId> rptLst = new ArrayList<>();

                        AssessmentResult.Results.SupportingMaterials.ReportId rpt;

                        if( includeScoreCode==3 && reportBytes != null && reportBytes.length > 0 )
                        {
                            rpt = new AssessmentResult.Results.SupportingMaterials.ReportId();
                            idvl = new ArrayList<>();

                            idvl.add(createIdValue("ReportId" , reportTitle == null || reportTitle.isEmpty() ? "Custom Report" : reportTitle  ) );
                            idvl.add( createIdValue( "MimeType" , "application/pdf" ) );
                            idvl.add( createIdValue( "ReportLangStr" , reportLanguage == null || reportLanguage.isEmpty() ? reportLangStr : reportLanguage ) );

                            rpt.idValue = idvl;

                            rpt.setEncodedContent( new String( Base64Encoder.encode( reportBytes ) ) );

                            rptLst.add(rpt);
                        }


                        else if( !testKey.getOmitPdfReportFromResultsPost() )
                        {
                            for( TestEventScore tes : rptTesl )
                            {
                                if( tes.getReportBytes()==null || tes.getReportBytes().length==0 )
                                    continue;

                                rpt = new AssessmentResult.Results.SupportingMaterials.ReportId();
                                idvl = new ArrayList<>();

                                idvl.add( createIdValue( "ReportId" , tes.getName() ) );
                                idvl.add( createIdValue( "MimeType" , "application/pdf" ) );
                                idvl.add( createIdValue( "ReportLangStr" , reportLangStr ) );
                                idvl.add( createIdValue( "PdfDownloadUrl" , tes.getReportDirectDownloadLink() ) );

                                if( tes.getTextParam1()!=null && !tes.getTextParam1().isBlank() )
                                    idvl.add( createIdValue( "CompactInfoStr" , tes.getTextParam1() ) );

                                rpt.idValue = idvl;

                                rpt.setEncodedContent( new String( Base64Encoder.encode( tes.getReportBytes() ) ) );

                                rptLst.add(rpt);
                            }
                        }

                        if( !rptLst.isEmpty() )
                        {
                            rpts.reportId = rptLst;
                            rslts.setSupportingMaterials(rpts);
                        }
                    }
                }

                if( userAgent!=null && !userAgent.isBlank() )
                    aoaUA.idValue.add( this.createIdValue("UserAgent", userAgent));

            }
        }

        catch( Exception e )
        {
            LogService.logIt(e, "AssessmentStatusCreator.getAssessmentResultFromTestKey() " + testKey.toString() );
            // Tracker.addApiError();

            throw e;
        }



        try
        {
            String out = JaxbUtils.marshalAssessmentResultXml(arr);
            // Next, convert to XML Object

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "AssessmentStatusCreator.getAssessmentResultFromTestKey() Marshalling error. " + testKey.toString() );


            throw e;
        }

    }



    private void setReportLocale( TestKey testKey, List<TestEvent> tel )
    {
        try
        {
                if( testKey.getOrg()==null )
                    testKey.setOrg( userFacade.getOrg( testKey.getOrgId() ));

                // this is usually filled in by now.
                if( testKey.getLocaleStrReport()!=null && !testKey.getLocaleStrReport().isEmpty() )
                     rptLocale = I18nUtils.getLocaleFromCompositeStr( testKey.getLocaleStrReport() );

                else if( testKey.getOrg()!=null && testKey.getOrg().getDefaultReportLang()!=null && !testKey.getOrg().getDefaultReportLang().isEmpty() )
                    rptLocale = I18nUtils.getLocaleFromCompositeStr( testKey.getOrg().getDefaultReportLang() );

                else
                {
                    for( TestEvent te : tel )
                    {
                        if( te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
                        {
                            rptLocale = I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );
                            break;
                        }

                        if( te.getProduct()==null )
                            te.setProduct( eventFacade.getProduct( te.getProductId() ) );

                        if( te.getReportId()>0 && te.getReport()==null )
                            te.setReport( eventFacade.getReport( te.getReportId() ));

                        if( te.getReport().getLocaleStr()!=null && !te.getReport().getLocaleStr().isEmpty() )
                        {
                            rptLocale = I18nUtils.getLocaleFromCompositeStr( te.getReport().getLocaleStr() );
                            break;
                        }
                    }
                }

                if( rptLocale == null )
                {
                    if( testKey.getAuthorizingUserId()>0 && testKey.getAuthUser()==null )
                    {
                        if( userFacade==null )
                            userFacade = UserFacade.getInstance();

                        testKey.setAuthUser( userFacade.getUser( testKey.getAuthorizingUserId() ));
                    }

                    if( testKey.getAuthUser()!=null )
                        rptLocale = I18nUtils.getLocaleFromCompositeStr( testKey.getAuthUser().getLocaleStr() );
                }

                if( rptLocale == null )
                    rptLocale = Locale.US;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AssessmentStatusCreator.setReportLocale() " + testKey.toString() );

            if( rptLocale == null )
                rptLocale = Locale.US;
        }
    }



    private void setScoreInfo( TestEventScore tes,  AssessmentResult.Results.DetailResult dtl ) throws Exception
    {
        List<IdValue> idvl = new ArrayList<>();

        SimCompetencyClass scc = SimCompetencyClass.getValue( tes.getSimCompetencyClassId() );

        String nm = tes.getName();

        if( equivSimJUtils!=null )
        {
            String s = equivSimJUtils.getCompetencyName( tes );

            if( s!=null && !s.isEmpty() )
                nm = UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );
        }

        idvl.add( createIdValue( "ScoreName" , nm ) );

        idvl.add( createIdValue( "ScoreNameEnglish" , tes.getNameEnglish() != null && !tes.getNameEnglish().isEmpty() ? tes.getNameEnglish() : tes.getName() ) );
        idvl.add( createIdValue( "ScoreTypeId" , Integer.toString( tes.getSimCompetencyClassId() ) ) );
        idvl.add( createIdValue( "ScoreTypeName" , scc.getName()  ) );

        dtl.idValue = idvl;

        dtl.setInterval( tes.getScoreCategoryId() );

        dtl.setScoreNumeric( tes.getScore() );

        AssessmentResult.Results.DetailResult.Metascores ms = new AssessmentResult.Results.DetailResult.Metascores();
        boolean hms = false;
        if( tes.getScore2()!=0 )
        {
            ms.setName2( scc.getMetaName(2) );
            ms.setScore2(tes.getScore2());
            if( scc.getMetaIsCanonical(2) )
                ms.setCanon2(1);
            hms=true;
        }
        if( tes.getScore3()!=0 )
        {
            ms.setName3( scc.getMetaName(3) );
            ms.setScore3(tes.getScore3());
            if( scc.getMetaIsCanonical(3) )
                ms.setCanon3(1);
            hms=true;
        }
        if( tes.getScore4()!=0 )
        {
            ms.setName4( scc.getMetaName(4) );
            ms.setScore4(tes.getScore4());
            if( scc.getMetaIsCanonical(4) )
                ms.setCanon4(1);
            hms=true;
        }
        if( tes.getScore5()!=0 )
        {
            ms.setName5( scc.getMetaName(5) );
            ms.setScore5(tes.getScore5());
            if( scc.getMetaIsCanonical(5) )
                ms.setCanon5(1);
            hms=true;
        }
        if( tes.getScore6()!=0 )
        {
            ms.setName6( scc.getMetaName(6) );
            ms.setScore6(tes.getScore6());
            if( scc.getMetaIsCanonical(6) )
                ms.setCanon6(1);
            hms=true;
        }
        if( tes.getScore7()!=0 )
        {
            ms.setName7( scc.getMetaName(7) );
            ms.setScore7(tes.getScore7());
            if( scc.getMetaIsCanonical(7) )
                ms.setCanon7(1);
            hms=true;
        }
        if( tes.getScore8()!=0 )
        {
            ms.setName8( scc.getMetaName(8) );
            ms.setScore8(tes.getScore8());
            if( scc.getMetaIsCanonical(8) )
                ms.setCanon8(1);
            hms=true;
        }
        if( tes.getScore9()!=0 )
        {
            ms.setName9( scc.getMetaName(9) );
            ms.setScore9(tes.getScore9());
            if( scc.getMetaIsCanonical(9) )
                ms.setCanon9(1);
            hms=true;
        }

        if( hms )
            dtl.setMetascores(ms );

        if( tes.getScoreText() != null && !tes.getScoreText().isEmpty() )
        {
            String scrTxt = tes.getScoreText();

            if( equivSimJUtils!=null )
            {
                String s = equivSimJUtils.getCompetencyScoreText( tes );

                if( s!=null && !s.isEmpty() )
                    scrTxt = UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );
            }

            dtl.setScoreText( scrTxt );
        }

        if( tes.getHasValidOverallNorm() )
        {
            dtl.setPercentileAll( tes.getPercentile() );
            dtl.setPercentileAllCount( tes.getOverallPercentileCount() );
        }
        if( tes.getHasValidCountryNorm())
        {
            dtl.setPercentileCountry( tes.getCountryPercentile());
            dtl.setPercentileCountryCount( tes.getCountryPercentileCount() );
        }
        if( tes.getHasValidAccountNorm() )
        {
            dtl.setPercentileClient(tes.getAccountPercentile());
            dtl.setPercentileClientCount( tes.getAccountPercentileCount());
        }
        
        String cefrTypeId = StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRTYPE);
        if( cefrTypeId!=null && !cefrTypeId.isBlank() )
        {
            dtl.getIdValue().add(createIdValue("CEFRType", cefrTypeId ));
            String cefrLevel = StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRLEVEL);
            if( cefrLevel!=null )
            {
                if( CefrScoreType.getFromText(cefrLevel).equals(CefrScoreType.UNKNOWN))
                    cefrLevel=null;
            }
            if( cefrLevel!=null && !cefrLevel.isBlank() )
            {
                dtl.getIdValue().add(createIdValue("CEFRLevel", cefrLevel));
                String cefrText = StringUtils.getBracketedArtifactFromString( tes.getTextParam1(), Constants.CEFRLEVELTEXT);
                if( cefrText!=null && !cefrText.isBlank() )
                    dtl.getIdValue().add(createIdValue("CEFRLevelText", cefrText));
            }            
        }
        
        
    }

    private void setEeocScoreInfo( EeoMatch er, int scoreTypeId, String scoreTypeName, AssessmentResult.Results.DetailResult dtl ) throws Exception
    {
        List<IdValue> idvl = new ArrayList<>();

        String nm = er.getEeoTitle();
        idvl.add( createIdValue( "ScoreName" , nm ) );
        idvl.add( createIdValue( "ScoreNameEnglish" , nm ) );
        idvl.add( createIdValue( "ScoreTypeId" , Integer.toString( scoreTypeId ) ) );
        idvl.add( createIdValue( "ScoreTypeName" , scoreTypeName  ) );
        dtl.idValue = idvl;

        dtl.setScoreNumeric( er.getAveragePercentMatch() );

        AssessmentResult.Results.DetailResult.Metascores ms = new AssessmentResult.Results.DetailResult.Metascores();
        ms.setName2( "Rank" );
        ms.setScore2( er.getRank()+ 0f);

        ms.setName3( "Average Percent Match" );
        ms.setScore3(er.getAveragePercentMatch());

        ms.setName4( "EEOC Type" );
        ms.setScore5(er.getEeoJobCategoryId() + 0f);

        dtl.setMetascores(ms );
    }


    private void setRiasecScoreInfo( Profile p, int scoreTypeId, String scoreTypeName, AssessmentResult.Results.DetailResult dtl ) throws Exception
    {
        List<IdValue> idvl = new ArrayList<>();

        String nm = p.getName();
        idvl.add( createIdValue( "ScoreName" , nm ) );
        idvl.add( createIdValue( "ScoreNameEnglish" , nm ) );
        idvl.add( createIdValue( "ScoreTypeId" , Integer.toString( scoreTypeId ) ) );
        idvl.add( createIdValue( "ScoreTypeName" , scoreTypeName  ) );
        dtl.idValue = idvl;

        dtl.setScoreNumeric( p.getFloatParam1() );

        AssessmentResult.Results.DetailResult.Metascores ms = new AssessmentResult.Results.DetailResult.Metascores();
        ms.setName2( "Rank" );
        ms.setScore2( p.getIntParam1() + 0f);

        ms.setName3( "Combined Match" );
        ms.setScore3(p.getFloatParam1());

        ms.setName4( "Riasec Match" );
        ms.setScore4(p.getFloatParam2());

        ms.setName5( "Education Experience Match" );
        ms.setScore5(p.getFloatParam3());

        ms.setName6( "Profile Match" );
        ms.setScore6(p.getFloatParam4());

        ms.setName7( p.getStrParam4() );
        ms.setScore7(0f);
        dtl.setMetascores(ms );
    }


    //private XMLGregorianCalendar getXmlDate( Date d ) throws Exception
    //{
    //    return DatatypeFactory.newInstance().newXMLGregorianCalendar( convertDateToString( d ) );
    //}





    private void setUploadedMediaInfoOverall( int orgId, TestEventScore otes, String langStr, AssessmentResult.Results.AssessmentOverallResult overa, List<UploadedUserFile> uufList ) throws Exception
    {
        try
        {
            if( uufList==null || uufList.isEmpty() )
                return;

            if( !otes.getTestEventScoreType().getIsOverall() )
            {
                LogService.logIt( "setUploadedMediaInfoOverall() TES is not overall type." );
                return;
            }
            if( overa==null )
            {
                LogService.logIt( "setUploadedMediaInfoOverall() no AssessmentOverallResult. Nothing to write to." );
                return;
            }

            List<TextAndTitle> ttll = otes.getTextBasedResponseList( NonCompetencyItemType.AV_UPLOAD.getTitle(), true, false);

            ttll.addAll( otes.getTextBasedResponseList( NonCompetencyItemType.FILEUPLOAD.getTitle(), true, true) );

            if( ttll.isEmpty() )
                return;

            //String baseMedUrl;

            //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
            //    baseMedUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps");
            //else
            //    baseMedUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl");

            for( TextAndTitle ttl : ttll )
            {
                if( ttl.getUploadedUserFileId()<=0 )
                    continue;

                for( UploadedUserFile uuf : uufList )
                {
                    if( uuf.getUploadedUserFileId()!=ttl.getUploadedUserFileId() )
                        continue;

                    if( uuf.getFilename()==null || uuf.getFilename().trim().isEmpty() )
                        continue;

                    if( !uuf.getConversionStatusType().getIsComplete() )
                        continue;

                    AssessmentResult.Results.AssessmentOverallResult.UploadedMedia um = new AssessmentResult.Results.AssessmentOverallResult.UploadedMedia();

                    if( ttl.getSequenceId()>0 )
                        um.setSequenceId( ttl.getSequenceId() );

                    um.setQuestion( ttl.getTitle() );
                    um.setDuration( uuf.getDuration() );
                    um.setWidth( uuf.getWidth() );
                    um.setHeight( uuf.getHeight() );
                    um.setMimeType( uuf.getMime() );
                    um.setIdentifier( "uuf-" + uuf.getUploadedUserFileId() );
                    um.setLangStr( langStr );

                    um.setUrl( ReportUtils.getMediaTempUrlSourceLink( orgId, uuf, 0, uuf.getFilename(), MediaTempUrlSourceType.FILE_UPLOAD) );

                    // um.setUrl( baseMedUrl + uuf.getDirectory() + "/" + uuf.getFilename() );

                    if( uuf.getThumbFilename()!=null && !uuf.getThumbFilename().isEmpty() )
                    {
                        String fn = uuf.getThumbFilename();
                        if( fn!=null && fn.contains( ".AWSCOUNT." ) )
                            fn = StringUtils.replaceStr( fn, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );
                        else if( fn!=null && fn.contains(  ".IDX." ) )
                            fn = StringUtils.replaceStr( fn, ".IDX." , ".1." );
                        um.setThumbUrl( ReportUtils.getMediaTempUrlSourceLink( orgId, uuf, 1, uuf.getThumbFilename(), MediaTempUrlSourceType.FILE_UPLOAD_THUMB) );
                        //um.setThumbUrl( baseMedUrl + uuf.getDirectory() + "/" + fn );
                    }

                    if( uuf.getAvItemResponse()!=null )
                    {
                        um.setTranscript( uuf.getAvItemResponse().getSpeechText() );
                        um.setTranscriptEnglish( uuf.getAvItemResponse().getSpeechTextEnglish() );
                    }

                    overa.getUploadedMedia().add(um);
                }
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AssessmentStatusCreator.setUploadedMediaInfoOverall() " + otes.toString() );
        }
    }



    private void setUploadedMediaInfoCompetency( int orgId, TestEventScore tes, String langStr, AssessmentResult.Results.DetailResult dtl, List<UploadedUserFile> uufList ) throws Exception
    {
        try
        {
            if( uufList==null || uufList.isEmpty() )
                return;

            if( !tes.getTestEventScoreType().getIsCompetency())
            {
                LogService.logIt( "setUploadedMediaInfoCompetency() TES is not competency type." );
                return;
            }
            if( dtl==null )
            {
                LogService.logIt( "setUploadedMediaInfoCompetency() no DetailResult. Nothing to write to." );
                return;
            }

            boolean isVoice = tes.getSimCompetencyClass().isScoredAudio();

            List<TextAndTitle> ttll = tes.getTextBasedResponseList( null, true, false);

            if( ttll==null || ttll.isEmpty() )
                return;

            AvItemResponse iir;

            //String baseMedUrl;

            //if( RuntimeConstants.getBooleanValue("useAwsMediaServer") )
            //    baseMedUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrlHttps");
            //else
            //    baseMedUrl = RuntimeConstants.getStringValue( "uploadedUserFileBaseUrl");

            String baseAdminUrl = RuntimeConstants.getStringValue( "adminappbasuri") + "/audiopb/";

            for( TextAndTitle ttl : ttll )
            {
                if( ttl.getUploadedUserFileId()<=0 )
                    continue;

                AssessmentResult.Results.DetailResult.UploadedMedia um = new AssessmentResult.Results.DetailResult.UploadedMedia();
                um.setQuestion( ttl.getTitle() );

                // Ivr responses have no uuf. Just an avItemResponse
                if( isVoice )
                {
                    if( avEventFacade == null )
                        avEventFacade = AvEventFacade.getInstance();

                    iir = avEventFacade.getAvItemResponse(ttl.getUploadedUserFileId(), false );

                    if( iir==null )
                        continue;

                    if( iir.getAudioBytes()==null )
                        continue;

                    if( !iir.getAudioStatusType().isStoredLocally() )
                        continue;

                    if( ttl.getSequenceId()>0 )
                        um.setSequenceId( ttl.getSequenceId() );

                    um.setMimeType( "audio/mpeg" );
                    um.setDuration( iir.getDuration() );
                    um.setIdentifier( "iir-" + iir.getAvItemResponseId() );
                    um.setLangStr( langStr );
                    um.setTranscript( iir.getSpeechText() );
                    um.setTranscriptEnglish( iir.getSpeechTextEnglish() );

                    // /ta/audiopb/testEventIdStr/iirIdStr/audio.mp3
                    um.setUrl( baseAdminUrl + tes.getTestEventIdEncrypted() + "/" + iir.getAvItemResponseIdEncrypted() + "/audio.mp3" );

                    dtl.getUploadedMedia().add(um);
                }

                else
                {
                    for( UploadedUserFile uuf : uufList )
                    {
                        if( uuf.getAvItemResponseId()!=ttl.getUploadedUserFileId() && uuf.getUploadedUserFileId()!=ttl.getUploadedUserFileId() )
                            continue;

                        // if( uuf.getUploadedUserFileId()!=ttl.getUploadedUserFileId() )
                        //     continue;

                        if( uuf.getFilename()==null || uuf.getFilename().trim().isEmpty() )
                            continue;

                        if( !uuf.getConversionStatusType().getIsComplete() )
                            continue;

                        if( ttl.getSequenceId()>0 )
                            um.setSequenceId( ttl.getSequenceId() );

                        um.setDuration( uuf.getDuration() );
                        um.setWidth( uuf.getWidth() );
                        um.setHeight( uuf.getHeight() );
                        um.setMimeType( uuf.getMime() );
                        um.setIdentifier( "uuf-" + uuf.getUploadedUserFileId() );
                        um.setLangStr( langStr );

                        um.setUrl( ReportUtils.getMediaTempUrlSourceLink(orgId, uuf, 0, uuf.getFilename(), MediaTempUrlSourceType.FILE_UPLOAD ) );
                        // um.setUrl( baseMedUrl + uuf.getDirectory() + "/" + uuf.getFilename() );

                        if( uuf.getThumbFilename()!=null && !uuf.getThumbFilename().isEmpty() )
                        {
                            String fn = uuf.getThumbFilename();
                            if( fn!=null && fn.contains( ".AWSCOUNT." ) )
                                fn = StringUtils.replaceStr( fn, ".AWSCOUNT." , "-" + StringUtils.padIntegerToLength( 1, 5 ) + "." );
                            else if( fn!=null && fn.contains(  ".IDX." ) )
                                fn = StringUtils.replaceStr( fn, ".IDX." , ".1." );

                            um.setThumbUrl( ReportUtils.getMediaTempUrlSourceLink(orgId, uuf, 1, fn, MediaTempUrlSourceType.FILE_UPLOAD_THUMB ) );
                            // um.setThumbUrl( baseMedUrl + uuf.getDirectory() + "/" + fn );
                            // um.setThumbUrl( baseMedUrl + uuf.getDirectory() + "/" + uuf.getThumbFilename() );
                        }

                        if( uuf.getAvItemResponse()!=null )
                        {
                            um.setTranscript( uuf.getAvItemResponse().getSpeechText() );
                            um.setTranscriptEnglish( uuf.getAvItemResponse().getSpeechTextEnglish() );
                        }

                        dtl.getUploadedMedia().add(um);
                    }
                }
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AssessmentStatusCreator.setUploadedMediaInfoCompetency() " + tes.toString() );
        }
    }



    private XMLGregorianCalendar getXmlDate( Date d ) throws Exception
    {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.setTimeZone( TimeZone.getTimeZone("UTC"));

        return getXmlDate( gc );
    }


    private XMLGregorianCalendar getXmlDate( GregorianCalendar gc ) throws Exception
    {
        // return getXmlDate( gc.getTime() );
        return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
    }

    private String convertDateToString( Date d )
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        format.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return format.format(d);
    }



    private IdValue createIdValue( String name, String value )
    {
        IdValue idv = new IdValue();

        idv.setName(name);
        idv.setValue(value);

        return idv;
    }

    private float computePercentComplete( TestKey tk )
    {
        try
        {
            TestKeyStatusType tkst = tk.getTestKeyStatusType();

            if( tkst.equals(TestKeyStatusType.ACTIVE) || tkst.equals(TestKeyStatusType.DEACTIVATED) )
                return 0;

            // conditions where it could have partial test events (started, expired, suspended)
            if( !tkst.equals(TestKeyStatusType.STARTED) && !tkst.equals(TestKeyStatusType.EXPIRED) && !tkst.equals(TestKeyStatusType.STOPPED_PROCTOR) )
            {
                // indicates test key status is complete or higher, not deactivated and not expired, it must have been completed.
                if( tkst.getIsCompleteOrHigher() )
                    return 100;

                //
                return 0;
            }

            //if( tk.getTestKeyStatusType().getIsCompleteOrHigher() )
            //    return 100f;

            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();

            if( tk.getTestEventList()== null )
                tk.setTestEventList( eventFacade.getTestEventsForTestKeyId( tk.getTestKeyId(), true) );

            List<TestEvent> tel = tk.getTestEventList(); // eventFacade.getTestEventsForTestKeyId( tk.getTestKeyId(), true);

            if( tel==null || tel.isEmpty() )
                return 0;

            float total = 0;

            for( TestEvent te : tel )
            {
                if( te.getTestEventStatusType().getIsCompleteOrHigher() )
                    total += 100;
                else
                    total += te.getPercentComplete();
            }

            return total/((float)tel.size());
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AssessmentStatusCreator.computePercentComplete() testKeyId=" + tk.getTestKeyId()  );
            return 0;
        }
    }

    private List<UploadedUserFile> getFullUploadedUserFileList( long testEventId )
    {
        List<UploadedUserFile> out = null;

        try
        {
            if( fileUploadFacade==null )
                fileUploadFacade = FileUploadFacade.getInstance();

            out = fileUploadFacade.getUploadedUserFilesForTestEvent(testEventId, -1);

            if( out.isEmpty() )
                return out;

            for( UploadedUserFile uuf : out )
            {
                if( uuf.getAvItemResponseId()<=0 )
                    continue;

                if( avEventFacade==null )
                    avEventFacade = AvEventFacade.getInstance();

                uuf.setAvItemResponse(avEventFacade.getAvItemResponse(uuf.getAvItemResponseId(), false ) );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "AssessmentStatusCreator.getFullUploadedUserFileList() testEventId=" + testEventId );
        }

        if( out==null )
            out = new ArrayList<>();

        return out;
    }



}
