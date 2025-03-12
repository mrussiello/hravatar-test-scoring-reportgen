/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.findly;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.Org;
import com.tm2score.event.EventFacade;
import com.tm2score.findly.xml.Scores;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.HttpUtils;
import com.tm2score.xml.JaxbUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

/**
 *
 *
"STANDARD") )
                fsi = new FindlyStandardScoreInfo();

            else if( scoreType.equalsIgnoreCase("TYPING") )
                fsi = new FindlyTypingScoreInfo();

            else if( scoreType.equalsIgnoreCase("DATA_ENTRY") )
                fsi = new FindlyDataEntryScoreInfo();

            else if( scoreType.equalsIgnoreCase("CALLCENTER") || scoreType.equalsIgnoreCase("IDENTITY") || scoreType.equalsIgnoreCase("TALENTSCOUT") *
 * @author Mike
 */
public class FindlyScoreUtils {

    static String[] LOW_COST_INTL_AFFILIATES = new String[] { "hraph", "dragnet", "midot" };
    static String[] VALID_SCORE_TYPES = new String[] { "STANDARD", "TYPING" , "DATA_ENTRY", "CALLCENTER", "IDENTITY", "TALENTSCOUT", "PROOFREADING", "OFFICE MANAGER" };
    static String ACCTID = "accountid";
    static String USERNAME = "username";
    static String PASSWORD = "password";

    static String[] MAIN_FINDLY_CREDS = null;
    public static String[] DEMO_FINDLY_CREDS = null;
    static String[] LOW_COST_INTL_FINDLY_CREDS = null;

    static Boolean FORCE_DEMO = new Boolean( true );

    EventFacade eventFacade;


    public static synchronized void init()
    {
        if( MAIN_FINDLY_CREDS == null )
        {
            MAIN_FINDLY_CREDS = new String[] { RuntimeConstants.getStringValue( "Findly-Main-Account-AccountId" ), RuntimeConstants.getStringValue( "Findly-Main-Account-Username" ), RuntimeConstants.getStringValue( "Findly-Main-Account-Password" ) };
            DEMO_FINDLY_CREDS = new String[] { RuntimeConstants.getStringValue( "Findly-Demo-Account-AccountId" ), RuntimeConstants.getStringValue( "Findly-Demo-Account-Username" ), RuntimeConstants.getStringValue( "Findly-Demo-Account-Password" ) };
            LOW_COST_INTL_FINDLY_CREDS = new String[] { RuntimeConstants.getStringValue( "Findly-Intl-Account-AccountId" ), RuntimeConstants.getStringValue( "Findly-Intl-Account-Username" ), RuntimeConstants.getStringValue( "Findly-Intl-Account-Password" ) };
        }
    }

    public void logonToFindly( Map<String,String> credentials ) throws Exception
    {
        init();

        if( credentials == null )
            throw new Exception( "No credentials provided." );

        String accountId = credentials.get( FindlyScoreUtils.ACCTID );
        String username = credentials.get( FindlyScoreUtils.USERNAME );
        String password = credentials.get( FindlyScoreUtils.PASSWORD );

        String url = "https://webtest.skillcheck.com/onlinetesting/servlet/com.skillcheck.session_management.SK_Servlet?ID=" + URLEncoder.encode( accountId, "UTF8" ) + "&MODE=WEBLOGIN," + URLEncoder.encode(username, "UTF8" ) + "," + URLEncoder.encode(password, "UTF8" );


        try
        {
            // CloseableHttpResponse r = null;

            // int statusCode = 0;

            try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
            {

                HttpGet get = new HttpGet( url );

                //try( CloseableHttpResponse r = client.execute(get ) )
                //{
                
                client.execute(get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "FindlyUtils.processLogonToFindly() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    if( entity2!=null )
                        EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "FindlyUtils.processLogonToFindly() statusCode="+ status + ", reason=" + response.getReasonPhrase() );
                    return null;
                    } );

                   // LogService.logIt( "FindlyUtils.logonToFindly() url=" + url + ", Response Code : " + r.getCode() );

                    //statusCode = r.getCode();

                    //if( statusCode != HttpStatus.SC_OK )
                    //{
                    //    LogService.logIt( "FindlyUtils.processLogonToFindly() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                    //    throw new Exception( "Logon failed with code " + r.getReasonPhrase() );
                    //}
                    //EntityUtils.consume(r.getEntity());
                //}
            }

            //finally
            //{
                //if( r != null )
                //    r.close();
            //}
        }
        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "FindlyScoreUtils.logonToFindly() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
        }        
        catch( IOException e )
        {
            LogService.logIt( "FindlyScoreUtils.logonToFindly() STERR " + e.toString() + ", accountId=" + accountId + ", username=" + username + ", password=" + password + ", url=" + url );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FindlyScoreUtils.logonToFindly() accountId=" + accountId + ", username=" + username + ", password=" + password + ", url=" + url );
        }
    }


    public byte[] getFindlyPdfReport( TestEvent te, Org o ) throws Exception
    {
        init();

        if( te.getThirdPartyTestAccountId() == null || te.getThirdPartyTestAccountId().isEmpty() )
            throw new Exception( "FindlyUtils.getFindlyPdfReport() Testevent does not have a valid Findly Account ID:" + te.getThirdPartyTestAccountId() );

        if( te.getThirdPartyTestEventId() == null || te.getThirdPartyTestEventId().isEmpty() )
            throw new Exception( "FindlyUtils.getFindlyPdfReport() Testevent does not have a valid eTicket in it:" + te.getThirdPartyTestEventId() + ", Findly accountId: " + te.getThirdPartyTestAccountId() );

        Map<String,String> credentials = getFindlyAccountCredentials( te.getThirdPartyTestAccountId(), o );

        String accountId = credentials.get( ACCTID );
        String username = credentials.get( USERNAME );
        String password = credentials.get( PASSWORD );

        // SHOW_START

        String url = "https://webtest.skillcheck.com/onlinetesting/servlet/com.skillcheck.session_management.SK_Servlet?ReportRetrieval";

        try
        {
            // CloseableHttpResponse r = null;

            // int statusCode = 0;

            try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
            {

                HttpPost post = new HttpPost( url );

                List<NameValuePair> al = new ArrayList<>();

                // String eTicketId="sadasfjkhsaflkjsafdl";

                al.add( new BasicNameValuePair( "ID", accountId ) );
                al.add( new BasicNameValuePair( "username", username ) );
                al.add( new BasicNameValuePair( "password", password ) );
                al.add( new BasicNameValuePair( "eticketid", te.getThirdPartyTestEventId() ) );  //
                al.add( new BasicNameValuePair( "format", "pdf" ) );

                post.setEntity(new UrlEncodedFormEntity(al));

                //try( CloseableHttpResponse r = client.execute( post ) )
                //{
                byte[] out = client.execute(post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "FindlyUtils.getFindlyPdfReport() statusCode="+ statusCode );
                    if( status<200 || status>=300 )
                        throw new IOException( "FindlyUtils.getFindlyPdfReport() statusCode="+ status + ", reason=" + response.getReasonPhrase() );
                    final HttpEntity entity2 = response.getEntity();
                    byte[] bytes = EntityUtils.toByteArray(entity2);
                    EntityUtils.consume(entity2);
                    return bytes;
                    } );

                    //statusCode = r.getCode();

                    //LogService.logIt( "FindlyUtils.getFindlyPdfReport() url=" + url + ", Response Code : " + statusCode );

                    //if( statusCode != HttpStatus.SC_OK )
                    //{
                    //    LogService.logIt( "FindlyUtils.getFindlyPdfReport() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                    //    throw new Exception( "Result report request failed with code " + r.getReasonPhrase() );
                    //}

                   // byte[] out = EntityUtils.toByteArray(r.getEntity());

                LogService.logIt( "FindlyUtils.getFindlyPdfReport() Byte Array is: " + out.length + " bytes in length." );

                //EntityUtils.consume(r.getEntity());
                return out;
                //}
            }

            //finally
            //{
                //if( r != null )
                //    r.close();
            //}
        }

        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "FindlyTestUtils.getFindlyPdfReport() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
        }        
        catch( IOException e )
        {
            LogService.logIt( "FindlyTestUtils.getFindlyPdfReport() STERR " + e.toString() + ", Returning Null for PDF report from Findly. accountId=" + accountId + ", url=" + url + " " + te.toString() );
            // throw new STException( e );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FindlyTestUtils.getFindlyPdfReport() Returning Null for PDF report from Findly. accountId=" + accountId + ", url=" + url + " " + te.toString() );
            // throw new STException( e );
        }

        return null;
    }

    public String getFindlyScoreXmlStr( TestEvent te, Org o ) throws Exception
    {
        init();

        if( te.getThirdPartyTestAccountId() == null || te.getThirdPartyTestAccountId().isEmpty() )
            throw new Exception( "FindlyUtils.getFindlyScoreXmlStr() Testevent does not have a valid Findly Account ID:" + te.getThirdPartyTestAccountId() );

        if( te.getThirdPartyTestEventId() == null || te.getThirdPartyTestEventId().isEmpty() )
            throw new Exception( "FindlyUtils.getFindlyScoreXmlStr() Testevent does not have a valid eTicket in it:" + te.getThirdPartyTestEventId() + ", Findly accountId: " + te.getThirdPartyTestAccountId() );

        Map<String,String> credentials = getFindlyAccountCredentials( te.getThirdPartyTestAccountId(), o );

        String accountId = credentials.get( ACCTID );
        String username = credentials.get( USERNAME );
        String password = credentials.get( PASSWORD );

        // SHOW_START

        String url = "https://webtest.skillcheck.com/onlinetesting/servlet/com.skillcheck.session_management.SK_Servlet?ReportRetrieval";

        try
        {
            // CloseableHttpResponse r = null;

            //int statusCode = 0;

            try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
            {

                HttpPost post = new HttpPost( url );

                List<NameValuePair> al = new ArrayList<>();

                // String eTicketId="sadasfjkhsaflkjsafdl";

                al.add( new BasicNameValuePair( "ID", accountId ) );
                al.add( new BasicNameValuePair( "username", username ) );
                al.add( new BasicNameValuePair( "password", password ) );
                al.add( new BasicNameValuePair( "eticketid", te.getThirdPartyTestEventId() ) );  //
                al.add( new BasicNameValuePair( "format", "xml" ) );

                post.setEntity(new UrlEncodedFormEntity(al));

                //try( CloseableHttpResponse r = client.execute( post ) )
                //{
                String s = client.execute(post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "FindlyUtils.getFindlyScoreXmlStr() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "FindlyUtils.getFindlyScoreXmlStr() statusCode="+ status + ", reason=" + response.getReasonPhrase() + ", response=" + ss );
                    return ss;
                    });

                    //statusCode = r.getCode();

                    // LogService.logIt( "FindlyUtils.getFindlyScoreXmlStr() url=" + url + ", Response Code : " + statusCode );

                    //if( statusCode != HttpStatus.SC_OK )
                    //{
                    //    LogService.logIt( "FindlyUtils.getFindlyScoreXmlStr() Method failed: " + r.getReasonPhrase() + ", url=" + url );

                    //    throw new Exception( "Result report request failed with code " + r.getReasonPhrase() );
                    //}

                    //BufferedReader reader = new BufferedReader(new InputStreamReader( r.getEntity().getContent()));

                    //String inputLine;
                    //StringBuilder response = new StringBuilder();

                    //while ((inputLine = reader.readLine()) != null) {
                    //    response.append(inputLine);
                    //}
                    //reader.close();

                    //String scoreXml =  response.toString();

                    // LogService.logIt( "FindlyUtils.getFindlyScoreXmlStr() scoreXml=" + scoreXml );
                    //EntityUtils.consume(r.getEntity());
                    
                    return s; // scoreXml;
                // }
            }

            //finally
            //{
                //if( r != null )
                //    r.close();
            //}
        }

        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "FindlyTestUtils.getFindlyScoreXmlStr() STERR " + e.toString() + ", url=" + url  + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            throw new STException( e );
        }        
        catch( IOException e )
        {
            LogService.logIt( "FindlyTestUtils.getFindlyScoreXmlStr() STERR " + e.toString() + ", accountId=" + accountId + ", url=" + url + " " + te.toString() );
            throw new STException( e );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "FindlyTestUtils.getFindlyScoreXmlStr() accountId=" + accountId + ", url=" + url + " " + te.toString() );
            throw new STException( e );
        }
    }

    
    public static boolean isInvalidScoreXmlAFinalFindlyResponse( String scoreXml )
    {
        try
        {
            if( scoreXml==null || scoreXml.isEmpty() )
                return false;

            if( scoreXml.indexOf( "Scores" )>0 || scoreXml.indexOf( "NumScores" )>0 )
                return true;

            return false;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FindlyScoreUtils.isInvalidScoreXmlAFinalFindlyResponse() " + scoreXml );
            return false;
        }        
    }
    

    public static boolean validScoreXml( String scoreXml )
    {
        try
        {
            if( scoreXml==null || scoreXml.isEmpty() )
                return false;

            else if( scoreXml.indexOf( "Scores" )<0 )
            {
                LogService.logIt( "FindlyScoreUtils.validScoreXml() Doesn't contain scores: " + scoreXml );
                return false;
            }

            Scores scores = JaxbUtils.ummarshalFindlyScoreXml( scoreXml );

            if( scores.getNumScores()<= 0 || scores.getScore()==null ||
                scores.getScore().getScoreInfo()==null ||
                scores.getScore().getScoreInfo().getScoreType()==null ||
                scores.getScore().getScoreInfo().getScoreType().isEmpty() )
                return false;

            String scoreType = null;

            if( scores.getScore()!=null && scores.getScore().getScoreInfo()!=null )
                scoreType = scores.getScore().getScoreInfo().getScoreType();

            if( !validScoreType( scoreType ) )
                return false;

            return true;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FindlyScoreUtils.validScoreXml() " + scoreXml );
            return false;
        }
    }

    public static boolean validScoreType( String scoreType )
    {
        if( scoreType==null || scoreType.isEmpty() )
            return false;

        for( String s : FindlyScoreUtils.VALID_SCORE_TYPES )
        {
            if( s.equalsIgnoreCase( scoreType ) )
                return true;
        }

        return false;
    }


    public Map<String,String> getFindlyAccountCredentials( String accountId, Org o ) throws Exception
    {
        init();

        try
        {
            if( accountId == null || accountId.isEmpty() )
                throw new Exception( "AccountId is invalid: " + accountId );

            if( o.getFindlyAccountCredentials()!=null && o.getFindlyAccountCredentials().startsWith( accountId ) )
                return parseOrgAccountCredentials( o.getFindlyAccountCredentials() );

            String[] vals = null;

            if( accountId.equals( MAIN_FINDLY_CREDS[0]) )
                vals = MAIN_FINDLY_CREDS;

            else if( accountId.equals( DEMO_FINDLY_CREDS[0]) )
                vals = DEMO_FINDLY_CREDS;

            else if( accountId.equals( LOW_COST_INTL_FINDLY_CREDS[0]) )
                vals = LOW_COST_INTL_FINDLY_CREDS;

            else
                vals = MAIN_FINDLY_CREDS;

            Map<String,String> out = new HashMap<>();

            out.put( ACCTID, vals[0] );
            out.put( USERNAME, vals[1] );
            out.put( PASSWORD, vals[2] );
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "FindlyTestUtils.getFindlyAccountCredentials() accountId=" + accountId + ", " + ( o==null ? "Org is Null!" : o.toString() ) );
            throw e;
        }
    }


    public Map<String,String> parseOrgAccountCredentials( String credStr ) throws Exception
    {
        Map<String,String> out = new HashMap<>();

        if( credStr!= null && !credStr.isEmpty() )
        {
            String[] vals = credStr.split( ";" );

            if( vals.length<3 )
                throw new Exception( "Couldn't parse FindlyAccountCredentials: " + credStr );

            out.put( ACCTID, vals[0] );
            out.put( USERNAME, vals[1] );
            out.put( PASSWORD, vals[2] );
            return out;
        }

        return null;
    }
}
