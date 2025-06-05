/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ws.rest;

import com.tm2score.dist.DistManager;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.I18nUtils;
import com.tm2score.report.ReportManager;
import com.tm2score.score.BaseScoreManager;
import com.tm2score.score.RescoreRereportThread;
import com.tm2score.score.ScoreManager;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.user.UserFacade;
import com.tm2score.util.Base64Encoder;
import com.tm2score.util.JsonUtils;
import com.tm2score.util.MessageFactory;
import jakarta.enterprise.context.RequestScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response; 
import java.util.ArrayList;
import java.util.Locale;

/**
 *
 * {
 *    tran:  transaction name
 *  *            
 *    tran = emailtestkey
 *             tkid  encrypted
 *             toaddr
 *             fmaddr
 *             fnname
 *             subj
 *             note
 * 
 *    tran = disttestkey
 *             tkid  encrypted
 *             teid  encrypted (optional)
 *             candidatefbkonly boolean
 *             fbkreportid int
 * 
 *    tran = scoretestkey
 *             tkid  encrypted
 * 
 *    tran = testeventreport
 *             tkid  encrypted
 *             teid  encrypted
 *             reportid  int
 *             langstr
 *             includeenglish   int
 *             frccalc   int
 *             savetes   int
 *             return { "data":{"bytes":"str base 64 enc"}}
 * 
 *    
 *    tran = samplereport
 *             productid int
 *             reportid int
 *             withprofile   boolean
 *             report2   boolean
 *             langstr
 *             return { "data":{"bytes":"str base 64 enc"}}
 * }
 * 
 * The purpose of this resource is to start scoring of a completed test key immediately.
 */
@Path("distws")
@RequestScoped
public class DistributionResource extends BaseApiResource {
    
    
    EventFacade eventFacade;
    
    
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response doPost( @Context HttpServletRequest request , @Context HttpHeaders headers, String jsonContent ) 
    {        
        try
        {
            // LogService.logIt( "DistributionResource.ScoreResource() START. jsonContent=" + jsonContent );
            // Authenticate
            try
            {
                 authenticateRequest( headers );
            }
            catch( ApiException e )
            {
                LogService.logIt( e, "DistributionResource.doPost() AA.1 Authentication Error. jsonContent=" + jsonContent );
                return Response.status( e.getHttpResponseCode(), "Unable to authenticate." ).build();
            }
                        
            if( jsonContent==null || jsonContent.isBlank() )
                throw new ApiException( "Payload is missing.", 150, Response.Status.BAD_REQUEST.getStatusCode() );
                        
            
            // Parse the Json cntent
            JsonObject jo = JsonUtils.getJsonObject(jsonContent);
            
            tran = jo.containsKey( "tran" ) ? jo.getString( "tran" ) : null;
            
            if( tran == null || tran.isBlank() )
                throw new ApiException( "tran is missing.", 155, Response.Status.BAD_REQUEST.getStatusCode() );
                        
                        
            JsonObjectBuilder outJob = null;
            
            if( tran.equals( "emailtestkey" ) )
                outJob = doEmailTestKey( jo );
            
            else if( tran.equals( "disttestkey" ) )
                outJob = doDistTestKey(  jo );
            
            else if( tran.equals( "scoretestkey" ) )
                outJob = doScoreTestKey( jo );
            
            else if( tran.equals( "testeventreport" ) )
                outJob = doTestEventReport( jo );
            
            else if( tran.equals( "samplereport" ) )
                outJob = doSampleReport( jo );
            
            else
                throw new ApiException( "tran is invalid=" + tran, 160, Response.Status.BAD_REQUEST.getStatusCode() );
                            
            JsonObject jo2 = outJob.build();            
            String out = JsonUtils.convertJsonObjectToString(jo2);            
            // LogService.logIt( "DistributionResource.ScoreResource() COMPLETE. output=" + out );
            return Response.ok( out, MediaType.APPLICATION_JSON).status( Response.Status.OK.getStatusCode() ).build();            
        }        
        catch( ApiException e )
        {
            LogService.logIt( "DistributionResource.doPost() " + e.toString()  + ", tran=" + tran + ", orgId=" + getOrgId() + ", jsonContent=" + jsonContent );
            String subj = "DistributionResource Exception tran=" + tran;     
            sendErrorEmail( subj, "tran=" + tran + ", API Exception=" + e.toString() );            
            return Response.status( e.getHttpResponseCode(), "Server Error: DistributionResource.ScoreResource() tran=" + tran + ", orgId=" + getOrgId() + ", " + e.toString() + ", jsonContent=" + jsonContent ).build();            
        }                
        catch( Exception e )
        {
            LogService.logIt( e, "DistributionResource.doPost() jsonContent=" + jsonContent );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "DistributionResource.ScoreResource() Unknown Exception tran=" + tran + ", authUserId=" + (authUser==null ? "null" : authUser.getUserId()) + ", " + e.toString() + ", jsonContent=" + jsonContent ).build();            
        }
    }
    



    
    private JsonObjectBuilder doEmailTestKey( JsonObject joIn ) throws Exception
    {
        long testKeyId=0;
        try
        {
            String tkid = joIn.containsKey( "tkid" ) ? joIn.getString( "tkid" ) : null;
                                    
            if( tkid == null || tkid.isBlank() )
                throw new ApiException( "DistributionResource.doEmailTestKey() tkid is missing.", 180, Response.Status.BAD_REQUEST.getStatusCode() );
                     
            try
            {
                testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt(tkid) );
            }
            catch( NumberFormatException ee )
            {
                LogService.logIt(  "DistributionResource.doEmailTestKey() NumberFormatException parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                throw new ApiException( "Error parsing tkid", 181, Response.Status.BAD_REQUEST.getStatusCode() );
            }
            catch( Exception ee )
            {
                LogService.logIt(  ee, "DistributionResource.doEmailTestKey() Exception parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                throw new ApiException( "Error parsing tkid", 182, Response.Status.BAD_REQUEST.getStatusCode() );                
            }
            
            if( testKeyId<=0 )
                throw new ApiException( "DistributionResource.doEmailTestKey() testKeyId is invalid=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            if( eventFacade==null )
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(testKeyId, true);
            if( tk==null )
                throw new ApiException( "Error DistributionResource.doEmailTestKey() TestKey not found for testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            UserFacade uf = UserFacade.getInstance();
            uf.clearSharedCache();
            uf.clearSharedCacheDiscern();
            
            String toAddr = JsonUtils.getStringFmJson(joIn, "toaddr");
            if( toAddr == null || toAddr.isEmpty() )
                throw new ApiException( "Error DistributionResource.doEmailTestKey() toAddress invalid. testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            String fromName = JsonUtils.getStringFmJson(joIn, "fmname");
            if( fromName == null || fromName.isBlank() )
                throw new ApiException( "Error DistributionResource.doEmailTestKey() fromName invalid. testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            String fromAddr = JsonUtils.getStringFmJson(joIn, "fmaddr");
            if( fromAddr == null || fromAddr.isEmpty() )
                throw new ApiException( "Error DistributionResource.doEmailTestKey() fromAddr invalid. testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            String subj = JsonUtils.getStringFmJson(joIn, "subj");
            if( subj == null || subj.isEmpty() )
                throw new ApiException( "Error DistributionResource.doEmailTestKey() subj invalid. testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            String note = JsonUtils.getStringFmJson(joIn, "note");
            
            DistManager dmb = new DistManager();

            if( !tk.getTestKeyStatusType().getIsReportGeneratedOrHigher() )
            {
                String msg = MessageFactory.getStringMessage( dmb.getEmailLocaleFromTestKey(tk), "g.TestKeyScoringNotCompleteCannotDist" );
                LogService.logIt( "DistributionResource.doEmailTestKey() " + msg + ", testKeyId=" + testKeyId );
                Tracker.addWebServiceError();
                throw new ApiException( "Error DistributionResource.doEmailTestKey() " + msg + ", testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );
            }

            int sent = dmb.sendTestKeyResultsViaEmail(tk, toAddr, fromName, fromAddr, subj, note, 1 );
            
            JsonObjectBuilder jobOut = Json.createObjectBuilder();
            jobOut.add("sentcount", sent );
            
            return getGeneralSuccessJson( jobOut, null );
        }
        catch( ApiException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DistributionResource.doEmailTestKey() testKeyId=" + testKeyId + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
            throw new ApiException( "Error DistributionResource.doEmailTestKey() testKeyId=" + testKeyId, 181, Response.Status.BAD_REQUEST.getStatusCode() );
        }
    }
    

    private JsonObjectBuilder doDistTestKey( JsonObject joIn ) throws Exception
    {
        long testKeyId=0;
        try
        {
            String tkid = joIn.containsKey( "tkid" ) ? joIn.getString( "tkid" ) : null;
                                    
            if( tkid == null || tkid.isBlank() )
                throw new ApiException( "DistributionResource.doDistTestKey() AAA.1 tkid is missing.", 180, Response.Status.BAD_REQUEST.getStatusCode() );
                     
            try
            {
                testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt(tkid) );
            }
            catch( NumberFormatException ee )
            {
                LogService.logIt(  "DistributionResource.doDistTestKey() AAA.2 NumberFormatException parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                throw new ApiException( "Error parsing tkid", 181, Response.Status.BAD_REQUEST.getStatusCode() );
            }
            catch( Exception ee )
            {
                LogService.logIt(  ee, "DistributionResource.doDistTestKey() AAA.3 Exception parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                throw new ApiException( "Error parsing tkid", 182, Response.Status.BAD_REQUEST.getStatusCode() );                
            }
            
            if( testKeyId <= 0 )
                throw new ApiException( "Error DistributionResource.doDistTestKey() AAA.4 testKeyId invalid: " + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            if( eventFacade==null )
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(testKeyId, true);

            if( tk==null )
                throw new ApiException( "Error DistributionResource.doDistTestKey() AAA.5 TestKey not found for testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            UserFacade uf = UserFacade.getInstance();
            uf.clearSharedCache();
            uf.clearSharedCacheDiscern();
            
            if( !tk.getTestKeyStatusType().equals( TestKeyStatusType.REPORTS_COMPLETE ) &&
                !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_STARTED ) &&
                !tk.getTestKeyStatusType().equals( TestKeyStatusType.DISTRIBUTION_COMPLETE )  )
            {
                LogService.logIt( "DistributionResource.doDistTestKey() AAA.6 ERROR. TestKey Status is not valid for redistribution: testKeyStatusTypeId=" + tk.getTestKeyStatusType().getTestKeyStatusTypeId() + ", testKeyId=" + tk.getTestKeyId() );
                throw new ApiException( "Error DistributionResource.doDistTestKey() AAA.6 ERROR. TestKey Status is not valid for redistribution: testKeyStatusTypeId=" + tk.getTestKeyStatusType().getTestKeyStatusTypeId() + ", testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );
            }

            // tk.setTestKeyStatusTypeId( TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() );
            tk.setTestKeyStatusTypeId( TestKeyStatusType.DISTRIBUTION_STARTED.getTestKeyStatusTypeId() );

            Tracker.addRedistribution();

            boolean candidateFbkOnly = false;
            long testEventId = 0;            
            

            if( joIn.containsKey("candidatefbkonly") && joIn.getBoolean("candidatefbkonly"))
                candidateFbkOnly = true;
            
            String teid = JsonUtils.getStringFmJson(joIn, "teid");
            if( teid!=null && !teid.isBlank() )
            {
                try
                {
                    testEventId = Long.parseLong( EncryptUtils.urlSafeDecrypt(teid) );
                }
                catch( NumberFormatException ee )
                {
                    LogService.logIt(  "DistributionResource.doDistTestKey() BBB.1 NumberFormatException parsing " + teid );
                    throw new ApiException( "Error parsing teid", 181, Response.Status.BAD_REQUEST.getStatusCode() );
                }
            }
            
            int fbkReportId = joIn.containsKey("fbkreportid") ? joIn.getInt("fbkreportid") : 0;

            LogService.logIt( "DistributionResource.doDistTestKey() EEE.1 testKeyId=" + testKeyId + ", testEventId=" + testEventId + ", fbkReportId=" + fbkReportId + ", candidateFbkOnly=" + candidateFbkOnly );
            
            BaseScoreManager.addTestKeyToDateMap( tk.getTestKeyId() );
            
            // Disabled since reports are not attached, they are downloaded and the download will regen the report.
            /*
            if( 1==2 && testEventId>0 && candidateFbkOnly && fbkReportId>0 )
            {
                LogService.logIt( "DistributionResource.doDistTestKey() EEE.2 checking TestEventScore for existing report bytes. testKeyId=" + testKeyId + ", testEventId=" + testEventId + ", fbkReportId=" + fbkReportId );
                TestEvent te = eventFacade.getTestEvent(testEventId, true );
                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(testEventId, true ));
                TestEventScore rtes = null;
                for( TestEventScore tes : te.getTestEventScoreList(TestEventScoreType.REPORT.getTestEventScoreTypeId()))
                {
                    if( tes.getReportId()==fbkReportId )
                    {
                        rtes = tes;
                        break;
                    }                    
                }
                
                if( rtes==null || !rtes.getHasReport() )
                {
                    LogService.logIt( "DistributionResource.doDistTestKey() EEE.3 No testeventScore with report bytes. Regenerating and saving. testKeyId=" + testKeyId + ", testEventId=" + testEventId + ", fbkReportId=" + fbkReportId );
                    ReportManager rm = new ReportManager();

                    rm.setTestEventScoreList( new ArrayList<>() );

                    TestEventScore tesx = rm.generateReportForTestEventAndLanguage(testEventId, fbkReportId, te.getLocaleStrReport(), 0, false, false, null );

                    byte[] out = tesx==null || tesx.getReportBytes()==null || tesx.getReportBytes().length==0 ? new byte[0] : tesx.getReportBytes();

                    if( tesx!=null && out !=null && out.length>0 )
                        eventFacade.saveTestEventScore(tesx);
                    else
                        LogService.logIt( "DistributionResource.doDistTestKey() EEE.Unable to regenerate report. Bytes are null or empty. testKeyId=" + testKeyId + ", testEventId=" + testEventId + ", fbkReportId=" + fbkReportId );

                }
                
            }
            */
            
            DistManager dmb = new DistManager();
            
            int[] tko = dmb.distributeTestKeyResults(tk, candidateFbkOnly, fbkReportId, testEventId, 0 );
            
            BaseScoreManager.removeTestKeyFromDateMap( tk.getTestKeyId() );

            if( tko[0]>0 || tko[1]>0 || tko[2]>0 )
            {
                String msg = "SUCCESS TestKeyId: " + testKeyId + " has been redistributed. " + tko[0] + " administrator emails sent and " + tko[1] + " text messages sent and " + tko[2] + " Test-Taker emails sent.";
                LogService.logIt( "DistributionResource.doDistTestKey() FFF.1 " + msg );
                return getGeneralSuccessJson( null, msg );
            }
        
            else
            {
                LogService.logIt( "DistributionResource.doDistTestKey() FFF.3. no results sent but no error provided. Most likely a parallel process error where this test key was in the wrong state. Please try again in a few minutes. If problem persists, please check logfiles. testKeyId=" + testKeyId );
                return  getGeneralErrorJson( null, "DistributionResource.doDistTestKey() FFF.3. no results sent but no error was provided. Most likely a parallel process error where this test key was in the wrong state. Please try again in a few minutes. If problem persists, please check logfiles. testKeyId=" + testKeyId );
            }
        }
        catch( ApiException e )
        {
            LogService.logIt( "DistributionResource.doDistTestKey() XXX.1 " + e.toString() + ", testKeyId=" + testKeyId );
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DistributionResource.doDistTestKey() YYY.1 testKeyId=" + testKeyId );
            throw new ApiException( "Error DistributionResource.doDistTestKey() YYY.1 testKeyId=" + testKeyId, 181, Response.Status.BAD_REQUEST.getStatusCode() );
        }
    }
    

    private JsonObjectBuilder doScoreTestKey( JsonObject joIn ) throws Exception
    {
        long testKeyId=0;
        try
        {
            String tkid = joIn.containsKey( "tkid" ) ? joIn.getString( "tkid" ) : null;
                                    
            if( tkid == null || tkid.isBlank() )
                throw new ApiException( "DistributionResource.doScoreTestKey() tkid is missing.", 180, Response.Status.BAD_REQUEST.getStatusCode() );
                     
            try
            {
                testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt(tkid) );
            }
            catch( NumberFormatException ee )
            {
                LogService.logIt(  "DistributionResource.doScoreTestKey() NumberFormatException parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                throw new ApiException( "Error parsing tkid", 181, Response.Status.BAD_REQUEST.getStatusCode() );
            }
            catch( Exception ee )
            {
                LogService.logIt(  ee, "DistributionResource.doScoreTestKey() Exception parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                throw new ApiException( "Error parsing tkid", 182, Response.Status.BAD_REQUEST.getStatusCode() );                
            }
            
            if( testKeyId <= 0 )
                throw new ApiException( "Error DistributionResource.doScoreTestKey() testKeyId invalid: " + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            if( eventFacade==null )
                eventFacade = EventFacade.getInstance();

            TestKey tk = eventFacade.getTestKey(testKeyId, true);

            if( tk==null )
                throw new ApiException( "Error DistributionResource.doScoreTestKey() TestKey not found for testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            UserFacade uf = UserFacade.getInstance();
            uf.clearSharedCache();
            uf.clearSharedCacheDiscern();
            String msg;
            
            if( !tk.getTestKeyStatusType().getIsCompleteOrHigher() )
            {
                msg = "DistributionResource.doScoreTestKey() ERROR  TestKey not yet completed. testKeyStatusTypeId=" + tk.getTestKeyStatusType().getTestKeyStatusTypeId() + ", testKeyId=" + testKeyId;                
                LogService.logIt( msg );
                Tracker.addWebServiceError();            
                throw new ApiException( msg, 160, Response.Status.BAD_REQUEST.getStatusCode() );
            }

            if( !ScoreManager.isScoringFirstTimeOrRepeatAllowed( testKeyId ))
            {
                msg = "DistributionResource.doScoreTestKey()  ERROR. TestKey status is invalid for rescore. Possibly not enough time since the last rescore. testKeyId=" + testKeyId;
                LogService.logIt( msg );
                throw new ApiException( msg, 160, Response.Status.BAD_REQUEST.getStatusCode() );
            }
            
            RescoreRereportThread rrt = new RescoreRereportThread( testKeyId, true );

            rrt.start();

            msg = "SUCCESS started re-score and re-reports for testkeyid=" + testKeyId;
            
            return getGeneralSuccessJson( null, msg );
        }
        catch( ApiException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DistributionResource.doScoreTestKey() testKeyId=" + testKeyId );
            throw new ApiException( "Error DistributionResource.doScoreTestKey() " + e.toString() + ", testKeyId=" + testKeyId, 181, Response.Status.BAD_REQUEST.getStatusCode() );
        }
    }


    private JsonObjectBuilder doTestEventReport( JsonObject joIn ) throws Exception
    {
        long testEventId = 0;
        long reportId;
        String langStr;
        int includeEnglishReport;
        int forceCalcSection;
        int saveTes;
                
        long testKeyId=0;
        try
        {
            String tkid = joIn.containsKey( "tkid" ) ? joIn.getString( "tkid" ) : null;                                                
            if( tkid!=null && !tkid.isBlank() )
            {
                try
                {
                    testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt(tkid) );
                }
                catch( NumberFormatException ee )
                {
                    LogService.logIt(  "DistributionResource.doTestEventReport() NONFATAL NumberFormatException parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                }
                catch( Exception ee )
                {
                    LogService.logIt(  ee, "DistributionResource.doTestEventReport() NONFATAL Exception parsing " + tkid + ", jsonContent=" + JsonUtils.convertJsonObjectToString(joIn) );
                }
            }
            
            UserFacade uf = UserFacade.getInstance();
            uf.clearSharedCache();
            uf.clearSharedCacheDiscern();
            String msg;
            
            String teid = JsonUtils.getStringFmJson(joIn, "teid");
            if( teid!=null && !teid.isBlank() )
            {
                try
                {
                    testEventId = Long.parseLong( EncryptUtils.urlSafeDecrypt(teid) );
                }
                catch( NumberFormatException ee )
                {
                    LogService.logIt(  "DistributionResource.doTestEventReport() NumberFormatException parsing " + teid );
                    throw new ApiException( "Error parsing teid", 181, Response.Status.BAD_REQUEST.getStatusCode() );
                }
            }
            
            if( testEventId <= 0 )
                throw new ApiException( "Error DistributionResource.doTestEventReport() testEventId invalid: " + testEventId + ", testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            if( eventFacade==null )
                eventFacade = EventFacade.getInstance();

            TestEvent te = eventFacade.getTestEvent(testEventId, true);

            if( te==null )
                throw new ApiException( "Error DistributionResource.doTestEventReport() TestEvent not found for testEventId=" + testEventId + ", testKeyId=" + testKeyId, 160, Response.Status.BAD_REQUEST.getStatusCode() );
            
            reportId = joIn.containsKey("reportid") ? joIn.getInt("reportid") : 0;
            
            langStr = JsonUtils.getStringFmJson(joIn, "langstr");

            includeEnglishReport = joIn.containsKey("includeenglish") ? joIn.getInt("includeenglish") : 0;

            forceCalcSection = joIn.containsKey("frccalc") ? joIn.getInt("frccalc") : 0;

            saveTes = joIn.containsKey("savetes") ? joIn.getInt("savetes") : 0;

            // LogService.logIt( "DistributionResource.doTestEventReport() EEE.1 testEventId=" + testEventId + ", reportId=" + reportId + ", langStr=" + langStr  + ",saveTes=" + saveTes  );
            
            ReportManager rm = new ReportManager();

            rm.setTestEventScoreList( new ArrayList<>() );

            TestEventScore tes = rm.generateReportForTestEventAndLanguage(testEventId, reportId, langStr, includeEnglishReport, forceCalcSection==1, false, null );

            byte[] out = tes==null || tes.getReportBytes()==null || tes.getReportBytes().length==0 ? new byte[0] : tes.getReportBytes();

            if( saveTes==1 && tes!=null && out !=null && out.length>0 )
            {
                eventFacade.saveTestEventScore(tes);
            }
            
            String bytes64 = new String(Base64Encoder.encode(out));
            JsonObjectBuilder jobOut = Json.createObjectBuilder();                        
            jobOut.add( "bytes", bytes64 ); 
            // LogService.logIt( "DistributionResource.doTestEventReport() EEE.2 testEventId=" + testEventId + ", reportId=" + reportId + ", langStr=" + langStr  + ", output is " + (out==null ? "null" : out.length) + " saveTes=" + saveTes + ", tes=" + (tes==null ? null : tes.getTestEventScoreId())  );

            msg = "SUCCESS for testkeyid=" + testKeyId;
            
            return getGeneralSuccessJson( jobOut, msg );
        }
        catch( ApiException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DistributionResource.doTestEventReport() testEventId=" + testEventId + ", testKeyId=" + testKeyId );
            throw new ApiException( "Error DistributionResource.doTestEventReport() " + e.toString() + ", testEventId=" + testEventId + ", testKeyId=" + testKeyId, 181, Response.Status.BAD_REQUEST.getStatusCode() );
        }
    }
    
    
    private JsonObjectBuilder doSampleReport( JsonObject joIn ) throws Exception
    {
        int productId=0;
        int reportId=0;
        
        try
        {
            productId = joIn.containsKey("productid") ? joIn.getInt("productid") : 0;            
            reportId = joIn.containsKey("reportid") ? joIn.getInt("reportid") : 0;
            
            if( productId <= 0 )
                throw new ApiException( "Error DistributionResource.doSampleReport() productId invalid " + productId, 160, Response.Status.BAD_REQUEST.getStatusCode() );

            boolean withProfile = joIn.containsKey("withprofile") ? joIn.getBoolean("withprofile") : false;

            boolean useReport2 = joIn.containsKey("report2") ? joIn.getBoolean("report2") : false;

            boolean useReport3 = joIn.containsKey("report3") ? joIn.getBoolean("report3") : false;
            
            Locale locale = Locale.US;
            String langStr = JsonUtils.getStringFmJson(joIn, "langstr");
            if( langStr!=null && !langStr.isBlank() )
                locale = I18nUtils.getLocaleFromCompositeStr(langStr);
                        
            ReportManager rm = new ReportManager();

            rm.setTestEventScoreList( new ArrayList<>() );

            TestEventScore tes = rm.generateSampleReport(productId, reportId, withProfile, useReport2, useReport3, locale );

            byte[] out = tes==null || tes.getReportBytes()==null || tes.getReportBytes().length==0 ? new byte[0] : tes.getReportBytes();

            // LogService.logIt( "DistributionResource.doSampleReport() useReport2=" + useReport2 + ",  useReport3=" + useReport3 + ", withProfile=" + withProfile + ", productId=" + productId + ", reportId=" + reportId + ", output is " + out.length );

            Tracker.addSampleReport();
            
            String bytes64 = new String(Base64Encoder.encode(out));
            JsonObjectBuilder jobOut = Json.createObjectBuilder();                        
            jobOut.add( "bytes", bytes64 ); 
            // LogService.logIt( "DistributionResource.doSampleReport() testEventId=" + testEventId + ", reportId=" + reportId + ", langStr=" + langStr  + ", output is " + out.length + " saveTes=" + saveTes  );

            String msg = "SUCCESS for productId=" + productId;
            
            return getGeneralSuccessJson( jobOut, msg );
        }
        catch( ApiException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DistributionResource.doSampleReport() productId=" + productId + ", reportId=" + reportId );
            throw new ApiException( "Error DistributionResource.doSampleReport() " + e.toString() + ", productId=" + productId + ", reportId=" + reportId, 181, Response.Status.BAD_REQUEST.getStatusCode() );
        }
    }
    
    

    protected JsonObjectBuilder getGeneralSuccessJson( JsonObjectBuilder dataJob, String msg )
    {
        JsonObjectBuilder job = Json.createObjectBuilder();                        
        job.add( "status", "complete" ); 
        if( dataJob!=null )
            job.add( "data", dataJob );
        if( msg!=null && !msg.isBlank() )
            job.add( "msg", msg );
        return job;                        
    }
    
    protected JsonObjectBuilder getGeneralErrorJson( Exception e, String userMessage )
    {
        JsonObjectBuilder job = Json.createObjectBuilder();                        
        job.add( "status", "error" );                        
        job.add("cause", e==null || e.getCause()==null ? "null" : e.getCause().toString()  );                        
        job.add("error", e==null || e.getMessage()==null ? "" : e.getMessage() );                        
        job.add("exception", userMessage==null ? "" : userMessage );                        
        return job;                        
    }
    
    
    
}
