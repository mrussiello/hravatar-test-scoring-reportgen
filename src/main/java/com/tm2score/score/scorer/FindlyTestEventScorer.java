/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.findly.*;
import com.tm2score.entity.event.Percentile;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.NormFacade;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.findly.xml.Scores;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.JaxbUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class FindlyTestEventScorer extends BaseTestEventScorer implements TestEventScorer {

    @Override
    public void scoreTestEvent( TestEvent testEvent, SimDescriptor simDescriptor, boolean skipVersionCheck) throws Exception
    {
       try
       {
           FindlyScoreUtils.init();

           te = testEvent;

            LogService.logIt( "FindlyTestEventScorer.scoreTestEvent() starting process. TestEvent " + te.toString() + ", eticketId=" + te.getThirdPartyTestEventId() + ", Findly Account=" + te.getThirdPartyTestAccountId() );

            StringBuilder scrSum = new StringBuilder();

            scrSum.append( "FindlyTestEventScorer.scoreTestEvent() START " + te.toString() );

            boolean hasResultXml = te.getResultXml()!=null && !te.getResultXml().isEmpty();
            
            if( te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED.getTestEventStatusTypeId() &&
                te.getTestEventStatusTypeId() != TestEventStatusType.COMPLETED_PENDING_EXTERNAL_SCORES.getTestEventStatusTypeId() )
                throw new ScoringException( "TestEvent is not correct status type. Expecting completed or completed pending external. ", ScoringException.NON_PERMANENT, te );

            if( !hasResultXml && ( te.getThirdPartyTestEventId() == null || te.getThirdPartyTestEventId().isEmpty() || te.getThirdPartyTestAccountId()==null || te.getThirdPartyTestAccountId().isEmpty() ) )
                throw new  ScoringException( "TestEvent does not have Findly test event data. eTicketId=" + te.getThirdPartyTestEventId() + ", accountId=" + te.getThirdPartyTestAccountId() , ScoringException.NON_PERMANENT, te );

            if( te.getThirdPartyTestAccountId()!=null && te.getThirdPartyTestAccountId().equalsIgnoreCase( FindlyScoreUtils.DEMO_FINDLY_CREDS[0] ) )
                te.setExcludeFmNorms(1);

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            if( userFacade == null )
                userFacade = UserFacade.getInstance();

            tk = eventFacade.getTestKey( te.getTestKeyId(), true );

            tk.setAuthUser( userFacade.getUser( tk.getAuthorizingUserId() ));

            if( te.getOrg()==null )
                te.setOrg( userFacade.getOrg( te.getOrgId() ));

            User user = null;

            String scoreXml = null;

            // byte[] findlyReportBytes = null;

            if( te.getTestEventScoreList()==null )
                te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent(te.getTestEventId(), true ) );
            
            // MJR 01092018 if( te.getTestEventScoreList() != null )
            // MJR 01092018 {
                // MJR 01092018 eventFacade.clearScoresForEvent(te, false );
                // MJR 01092018 te.setTestEventScoreList( null );
            // MJR 01092018 }

            if( te.getResultXml() != null && !te.getResultXml().isEmpty() )
                scoreXml = te.getResultXml();

      // scoreXml = null;  // REMOVE
            FindlyScoreUtils fsu = new FindlyScoreUtils();

            boolean isFinalFindly = false;
            boolean isValidFindly = false;
            
            boolean unscorable = false;

            if( scoreXml == null || scoreXml.isEmpty() )
            {
                scoreXml = fsu.getFindlyScoreXmlStr( te, te.getOrg() );

                isValidFindly = FindlyScoreUtils.validScoreXml(scoreXml);
                isFinalFindly = FindlyScoreUtils.isInvalidScoreXmlAFinalFindlyResponse( scoreXml );
                                    
                if( !isValidFindly )
                {
                    LogService.logIt( "FindlyTestEventScorer.scoreTestEvent() ScoreXml is not valid: " + te.toString() +", isFinalFindly=" + isFinalFindly + ", " + scoreXml );
                    
                    
                    String tx = StringUtils.getBracketedArtifactFromString(te.getTextStr1(), Constants.FINDLYATTEMPTSKEY );                    
                    String tp1 = tx!=null && !tx.isBlank() ? tx : te.getTextStr1();

                    int attempts = 0;

                    if( tp1!=null && !tp1.isEmpty() )
                    {
                        try
                        {
                            attempts = Integer.parseInt(tp1);
                        }
                        catch( NumberFormatException e )
                        {}
                    }

                    attempts = attempts+1;
                    
                    if( !isFinalFindly && attempts > 10 )
                        throw new ScoringException( "TestEvent ScoreXml is not valid. Too many attempts. Attempts=" + attempts +", scoreXml=" + scoreXml, ScoringException.PERMANENT, te );

                    te.setTextStr1( StringUtils.addBracketedArtifactToStr( te.getTextStr1(), Constants.FINDLYATTEMPTSKEY, Integer.toString(attempts)  ) );
                    // te.setTextStr1( attempts + "" );

                    eventFacade.saveTestEvent(te);

                    if( !isFinalFindly )
                        throw new ScoringException( "TestEvent ScoreXml is not valid. Will try again. Attempts=" + attempts +", scoreXml=" + scoreXml, ScoringException.NON_PERMANENT, te );
                    // throw new Exception( "ScoreXml is not valid: " + te.toString() + ", " + scoreXml );
                }

                
                
                if( isFinalFindly )
                {
                    te.setResultXml(scoreXml);

                    eventFacade.saveTestEvent(te);                        
                }
            }
            
            else
            {
                isValidFindly = FindlyScoreUtils.validScoreXml(scoreXml);
                isFinalFindly = FindlyScoreUtils.isInvalidScoreXmlAFinalFindlyResponse( scoreXml );
            }

            // findlyReportBytes = fsu.getFindlyPdfReport( te, te.getOrg() );

            if( te.getReport()==null )
            {
                long reportId = 0;

                if( te.getReportId() >0 )
                    reportId = te.getReportId();
                else if( te.getProduct().getLongParam2() > 0 )
                    reportId = te.getProduct().getLongParam2();

                Report r = reportId>0 ? eventFacade.getReport(reportId) : null;

                te.setReport(r);
            }

            if( te.getLocaleStrReport()!=null && !te.getLocaleStrReport().isEmpty() )
                reportLocale = I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

            else if( te.getReport()!=null && te.getReport().getLocaleStr()!= null && !te.getReport().getLocaleStr().isEmpty() )
                reportLocale =  I18nUtils.getLocaleFromCompositeStr( te.getReport().getLocaleStr() );

            else if( tk!=null && tk.getAuthUser()!=null && tk.getAuthUser().getLocaleStr()!=null && !tk.getAuthUser().getLocaleStr().isEmpty()  )
                reportLocale =  I18nUtils.getLocaleFromCompositeStr( tk.getAuthUser().getLocaleStr() );

            else if( tk.getSuborgId()>0 && tk.getSuborg()!=null && tk.getSuborg().getDefaultReportLang()!=null && !tk.getSuborg().getDefaultReportLang().isEmpty() )
                reportLocale = I18nUtils.getLocaleFromCompositeStr( tk.getSuborg().getDefaultReportLang() );

            else if( tk.getOrg()!=null && tk.getOrg().getDefaultReportLang()!=null && !tk.getOrg().getDefaultReportLang().isEmpty() )
                reportLocale = I18nUtils.getLocaleFromCompositeStr( tk.getOrg().getDefaultReportLang() );

            else
                reportLocale = Locale.US;

            LogService.logIt( "FindlyTestEventScorer.scoreTestEvent() starting to score TestEvent " + te.toString() + ", reportLocale=" + reportLocale.toString() );

            // Indicate scoring has started.
            te.setTestEventStatusTypeId( TestEventStatusType.SCORING_STARTED.getTestEventStatusTypeId() );

            // If already set to exclude
            if( te.getExcludeFmNorms()==1 )
            {}

            else if( te.getOrg()!=null && te.getOrg().getExcludeFromNorms()==1 )
                te.setExcludeFmNorms(1);

            else if( te.getSuborg()!=null && te.getSuborg().getExcludeFromNorms()==1 )
                te.setExcludeFmNorms(1);

            else
            {
                user = te.getUser();

                if( user == null )
                {
                      if( userFacade == null )
                          userFacade = UserFacade.getInstance();

                      user = userFacade.getUser( te.getUserId() );
                }

                if( user != null && user.getEmail()!= null && user.getUserType().getNamed() )
                {
                    for( String stub : Constants.AUTO_EXCLUDE_EMAILS )
                    {
                        if( user.getEmail().indexOf( stub ) > 0 )
                        {
                            te.setExcludeFmNorms(1);
                            break;
                        }
                    }
                }
            }

            if( te.getUserId()>0 && tk.getUserId()<= 0 )
            {
                LogService.logIt( "FindlyTestEventScorer.scoreTestEvent() Setting TestKey.userId to match TestEvent " + te.toString() );
                tk.setUserId( te.getUserId() );
                eventFacade.saveTestKey(tk);
            }

            eventFacade.saveTestEvent(te);

            Scores scores = isValidFindly ? JaxbUtils.ummarshalFindlyScoreXml( scoreXml ) : null;

            String scoreType = isValidFindly ? scores.getScore().getScoreInfo().getScoreType() : "UNSCORABLE";

            FindlyScoreInfo fsi = null;

            if( scoreType.equalsIgnoreCase("STANDARD") )
                fsi = new FindlyStandardScoreInfo();

            else if( scoreType.equalsIgnoreCase("OFFICE MANAGER") )
                fsi = new FindlyOfficeManagerScoreInfo();

            else if( scoreType.equalsIgnoreCase("TYPING") )
                fsi = new FindlyTypingScoreInfo();

            else if( scoreType.equalsIgnoreCase("DATA_ENTRY") )
                fsi = new FindlyDataEntryScoreInfo();

            else if( scoreType.equalsIgnoreCase("PROOFREADING") )
                fsi = new FindlyProofreadingScoreInfo();

            else if( scoreType.equalsIgnoreCase("CALLCENTER") || scoreType.equalsIgnoreCase("IDENTITY") || scoreType.equalsIgnoreCase("TALENTSCOUT"))
                fsi = new FindlyScalesOnlyScoreInfo();

            else
            {
                LogService.logIt( "Cannot determine a FindlyScoringObject to use. " + scoreType + ", scoring using FindlyUnscorableScoreInfo. " + ", " + te.toString() );
                fsi = new FindlyUnscorableScoreInfo();
                unscorable = true;
            }

            fsi.populateTestEventAndCreateTestEventScoreList(te);

            for( TestEventScore tes : te.getTestEventScoreList() )
            {
                tes.setTestEventId( te.getTestEventId() );
                eventFacade.saveTestEventScore(tes);
            }

            eventFacade.saveTestEvent(testEvent);

            if( normFacade == null )
               normFacade = NormFacade.getInstance();

            Thread.sleep( 500 );

            TestEventScore overallTes = te.getOverallTestEventScore();

            Map<String,Object> norm=null;
            float percentile;
            int pcount;

            // DO NOT DO OVERALL PERCENTILE FOR OVERALL UNLESS NONE FROM FINDLY
            String cc = te.getIpCountry();


            if( !unscorable )
            {
                norm = getPercentile(te.getProductId(), 0, te, overallTes, te.getOrgId(), cc );
                if( overallTes.getPercentile()<= 0 && overallTes.getOverallPercentileCount()<=0 )
                {
                    LogService.logIt( "FindlyTestEventScorer.scoreTestEvent() getting overall percentile from HR Avatar database. findly  percentile=" + overallTes.getPercentile() + ", findly count=" + overallTes.getOverallPercentileCount() );
                    
                    percentile = norm==null ? -1 : ((Float)norm.get("percentile")).floatValue();
                    pcount = norm==null ? 0 : ((Integer)norm.get("count")).intValue();
                    
                    overallTes.setPercentile( percentile );
                    te.setOverallPercentile( percentile );
                    te.setOverallPercentileCount( pcount );
                    overallTes.setOverallPercentileCount( pcount );
                }

                //if( norm==null )
                //    norm = getPercentile(te.getProductId(), te, overallTes, te.getOrgId(), cc);
                
                percentile = norm==null ? -1 : ((Float)norm.get("percentileorg")).floatValue();
                pcount = norm==null ? 0 : ((Integer)norm.get("countorg")).intValue();
                                    
                overallTes.setAccountPercentile( percentile );
                te.setAccountPercentile( percentile );
                te.setAccountPercentileCount( pcount );
                overallTes.setAccountPercentileCount( pcount );

                // String cc = te.getIpCountry();

                //norm = cc == null || cc.length()==0 ? null : getPercentile(te.getProductId(), te, overallTes, 0, cc);
                
                //if( norm==null )
                //{
                //    norm = new HashMap<>();
                //    norm.put( "percentile" , new Float(-1) );
                //    norm.put( "count" , new Integer(0)) ;
                //}

                percentile = norm==null ? -1 : ((Float)norm.get("percentilecc")).floatValue();
                pcount = norm==null ? 0 : ((Integer)norm.get("countcc")).intValue();
                
                overallTes.setCountryPercentile( percentile );
                te.setCountryPercentile( percentile );
                te.setCountryPercentileCount( pcount );
                overallTes.setCountryPercentileCount( pcount );

                eventFacade.saveTestEventScore(overallTes);
            }
            
            Percentile pctObj;

            // ALWAYS DO THIS AFTER SAVING TestEventScore
            if( !unscorable && te.getExcludeFmNorms()==0 )
            {
                pctObj = createPercentileForScore( te, overallTes );

                if( pctObj != null )
                    eventFacade.savePercentile(pctObj);

                /*
                for( TestEventScore tes : te.getTestEventScoreList() )
                {
                    if( tes.getTestEventScoreType().getIsOverall() )
                        continue;

                    if( tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) )
                    {
                        pctObj = createPercentileForScore( te, tes );

                        if( pctObj != null )
                            eventFacade.savePercentile(pctObj);


                    }
                }
                */
            }

            ScoreFormatType sft = fsi.getScoreFormatType();

            te.setScoreFormatTypeId( sft.getScoreFormatTypeId() );

            // This is the reportId to be used for presentation. May be changed on rescores/rereports, so remove here.
            te.setReportId( 0 );
            te.setTestEventStatusTypeId( TestEventStatusType.SCORED.getTestEventStatusTypeId() );

            eventFacade.saveTestEvent(te);

            LogService.logIt( "FindlyTestEventScorer.scoreTestEvent() COMPLETED SCORING. unscorable=" + unscorable + ", " + te.toString() );
       }

       catch( ScoringException e )
       {
           LogService.logIt( e, "FindlyTestEventScorer.scoreTestEvent() "  + te.toString() );
           throw e;
       }

       // unforseen exceptions are permanent. Disable this TestEvent until fixed.
       catch( Exception e )
       {
           LogService.logIt( e, "FindlyTestEventScorer.scoreTestEvent() "  + te.toString() );

           throw new ScoringException( e.getMessage() + "FindlyTestEventScorer.scoreTestEvent() " , ScoreUtils.getExceptionPermanancy(e) , te );
       }
    }


    
    
    public Map<String,Object> getPercentile( int productId, int alternateProductId, TestEvent te, TestEventScore tes, int orgId, String countryCode) throws Exception
    {
        if( normFacade == null )
            normFacade = NormFacade.getInstance();

        return normFacade.getPercentile(productId, ScoreUtils.getPercentileScoreTypeIdForTestEvent(te), te.getSimId(), te.getSimVersionId(), te.getTestEventId(), 0, tes, orgId, countryCode, null, null, null);
    }
    

    private Percentile createPercentileForScore( TestEvent te, TestEventScore tes ) throws Exception
    {
        try
        {
            if( te.getTestEventId()<=0 )
                throw new Exception( "TestEvent.TestEventId is 0" );

            if( tes.getTestEventScoreId()<=0 )
                LogService.logIt( "FindlyTestEventScorer.createPercentile() TestEventScore.TestEventScoreId is 0 " + te.toString() + ", " + tes.toString() );
                // throw new Exception( "TestEventScore.TestEventScoreId is 0" );

            if( te.getExcludeFmNorms()==1 )
                throw new Exception( "TestEvent.excludeFmNorms is set." );

            if( normFacade==null )
                normFacade = NormFacade.getInstance();

            Percentile p = normFacade.getExistingPercentileRecordForTestEvent(te, tes);

            if( p == null )
                p = new Percentile();

            if( tes.getTestEventScoreId()<= 0)
            {
                if( eventFacade == null )
                    eventFacade = EventFacade.getInstance();

                eventFacade.flushEntityManager();
            }


            te.populatePercentileObj(p);
            tes.populatePercentileObj(p);

            return p;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FindlyTestEventScorer.createPercentile() " );
            return null;
        }
    }

}
