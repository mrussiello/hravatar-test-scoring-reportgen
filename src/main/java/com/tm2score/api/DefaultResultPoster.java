/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.api;

import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventLogUtils;
import com.tm2score.score.ScoringException;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.util.Base64Encoder;
import com.tm2score.util.HttpUtils;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
 *
 * @author Mike
 */
public class DefaultResultPoster implements ResultPoster {

    TestKey testKey;

    // @Inject
    EventFacade eventFacade;

    public DefaultResultPoster( TestKey tk )
    {
        this.testKey = tk;
    }
    @Override
    public void postTestResults() throws Exception
    {
        AssessmentResult arr;
        AssessmentResult.AssessmentStatus aoas;
        String payload = null;
        String url = null;

        if( testKey !=null )
            url = testKey.getResultPostUrl();
        
        int statusCode = 0;
        try
        {

            if( url==null || url.isEmpty() )
                throw new Exception( "ResultPostUrl is missing: " + url );

            // LogService.logIt( "DefaultResultPoster.postTestResults() Start testKeyId=" + testKey.getTestKeyId() );

            // Prepare the content
            arr = new AssessmentResult();
            aoas = new AssessmentResult.AssessmentStatus();

            AssessmentStatusCreator asc = new AssessmentStatusCreator();

            // Since includeScoreCode for AssessmentStatusRequest and AssessmentOrderRequest are sligntly different, need to translate here. 
            // The AssessmentStatusCreator uses includeScoreCode values asociated with AssessmentStatusRequest. Default is 2=Include everything.
            
            // include everything (score and PDFs)
            int includeScoreCodeFinal = 2;
            
            String includeScoreCodeStr = testKey.getCustomParameterValue( "includeScoreCode" );
            
            if( includeScoreCodeStr!=null && !includeScoreCodeStr.isEmpty() )
            {
                int tempCode = Integer.parseInt( includeScoreCodeStr );
                
                // include scores but no PDFs
                if( tempCode == 1 )
                    includeScoreCodeFinal = 1;
                
                // Inlude no score info
                else if( tempCode == 4 )
                    includeScoreCodeFinal = 0;                    
            }
            
            payload = asc.getAssessmentResultFromTestKey(arr, testKey, includeScoreCodeFinal, null, null, null );

            // LogService.logIt( "DefaultResultPoster.postTestResults() have payload size=" + payload.length() + ", testKeyId=" + testKey.getTestKeyId() + ", url=" + url + ", includeScoreCodeFinal=" + includeScoreCodeFinal );

            // CloseableHttpResponse r = null;

            Map<String,String> basicAuthCreds = testKey.getBasicAuthParmsForResultsPost();
            final String payload2 = payload;

            try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
            {
                HttpPost post = new HttpPost( url );
                post.setHeader( HttpHeaders.CONTENT_TYPE, "application/xml");
                

                if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
                {
                    String un = basicAuthCreds.get( "username" );
                    String pwd = basicAuthCreds.get( "password" );

                    String b6 = Base64Encoder.encodeString( un + ":" + pwd );

                    post.setHeader( "Authorization", "Basic " + b6  );

                    // LogService.logIt( "DefaultResultPoster.postTestResults() Set basic Auth header: Basic " + b6);
                }
                //List<NameValuePair> al = new ArrayList<>();

                // al.add( new NameValuePair("name","ABC" ) );

                post.setEntity( new StringEntity(payload, ContentType.APPLICATION_XML.withCharset(StandardCharsets.UTF_8)) );
                // post.setEntity(new UrlEncodedFormEntity(al));

                //try( CloseableHttpResponse r = client.execute( post ) )
                //{
                client.execute(post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "IpUtils.getIPLocationData() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    // String ss = EntityUtils.toString(entity2);
                    
                    if( status==307 )
                    {
                        String location = null;
                        Header[] hdrs = response.getHeaders("Location" );

                        for( Header h : hdrs )
                        {
                            location = h.getValue();                        
                            if( location!=null && !location.isBlank() )
                                break;
                        }

                        // LogService.logIt( "DefaultResultPoster.postTestResults() Method failed with Temp Redirect: " + response.getReasonPhrase() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId() + ", location hdr=" + location );

                        if( location!=null && !location.isBlank() )
                        {
                            HttpPost post2 = new HttpPost( location );

                            post2.setHeader( HttpHeaders.CONTENT_TYPE, "application/xml");

                            if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
                            {
                                String un = basicAuthCreds.get( "username" );
                                String pwd = basicAuthCreds.get( "password" );
                                String b6 = Base64Encoder.encodeString( un + ":" + pwd );
                                post2.setHeader( "Authorization", "Basic " + b6  );
                            }

                            post2.setEntity( new StringEntity(payload2) );
                            return client.execute(post2, (ClassicHttpResponse response2) -> {
                                    int status2 = response2.getCode();
                                    // LogService.logIt( "DefaultResultPoster.postTestResults() statusCode="+ statusCode );
                                    final HttpEntity entity22 = response2.getEntity();
                                    if( entity22!=null )
                                         EntityUtils.consume(entity22);
                                    if( status2<200 || status2>=300 )
                                        throw new IOException( "DefaultResultPoster.postTestResults() statusCode="+ status2 + ", reason=" + response2.getReasonPhrase() );
                                    return null;
                                });                            
                        }                                            
                    }
                    
                    if( status<200 || status>=300 )
                        throw new IOException( "DefaultResultPoster.postTestResults() statusCode="+ status + ", reason=" + response.getReasonPhrase() );
                    
                    if( entity2!=null )
                        EntityUtils.consume(entity2);
                                        
                    return null;
                    });

                    //else
                    TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, 2, "DefaultResultPoster.postTestResults() Result Post Succeeded. url=" + url, null, null );                    
                
                    //if( r.getEntity()!=null )
                    //    EntityUtils.consume(r.getEntity());
                // }                                
                // LogService.logIt( "DefaultResultPoster.postTestResults() Response Code : " + r.getCode() );
            }

            //catch( ScoringException e )
            //{
            //    LogService.logIt( "DefaultResultPoster.postTestResults() XXX.1 posting results. " + e.toString() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId() );
            //    throw e;
            //    
            //}
            catch( ConnectionRequestTimeoutException e )
            {
                LogService.logIt( "DefaultResultPoster.postTestResults() STERR " + e.toString() + ", url=" + url + ", payload=" + (payload==null ? "null" : payload ) + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
                HttpUtils.resetPooledConnectionManagerIfNeeded();
                Tracker.addDistributionError();
                TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, 0, "DefaultResultPoster.postTestResults() url=" + url + ", error: " + e.toString(), null, null ); 
                throw new ScoringException( "DefaultResultPoster.postTestResults() posting results. " + e.toString() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId(),ScoringException.NON_PERMANENT,this.testKey);
            }        
            catch( IOException e )
            {
                LogService.logIt( "DefaultResultPoster.postTestResults() XXX.2 posting results. " + e.toString() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId() );
                Tracker.addDistributionError();
                TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, 0, "DefaultResultPoster.postTestResults() url=" + url + ", error: " + e.toString(), null, null ); 
                throw new ScoringException( "DefaultResultPoster.postTestResults() posting results. " + e.toString() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId(),ScoringException.NON_PERMANENT,this.testKey);
            }
            
            catch( Exception e )
            {
                LogService.logIt( e, "DefaultResultPoster.postTestResults() XXX.3 Exception posting results. url=" + url + ", testKeyId=" + testKey.getTestKeyId() );
                throw e;
            }
            //finally
            //{
                //if( r != null )
                //    r.close();
            //}
            // LogService.logIt( "DefaultResultPoster.postTestResults() back from call response." + response );
        }
        catch( ScoringException e )
        {
            throw e;
        }        
        catch( ConnectException e )
        {
            LogService.logItNoTrack( e, "DefaultResultPoster.postTestResults() ZZZ.1 ERROR - ConnectException " + e.toString() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload )  );
            TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, 0, "DefaultResultPoster.postTestResults() ConnectException, url=" + url + ", error: " + e.toString() + ", payload.size=" + (payload==null ? "null" : payload.length()), null, null );                                
            EmailUtils.getInstance().sendEmailToAdmin( "DefaultResultPoster.postTestResults() ConnectException", e.toString() + "  url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload ) );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DefaultResultPoster.postTestResults() ZZZ.2 url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload ) );
            TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, 0, "DefaultResultPoster.postTestResults() Exception, url=" + url + ", error: " + e.toString() + ", payload.size=" + (payload==null ? "null" : payload.length()), null, null );                                
            // EmailUtils.getInstance().sendEmailToAdmin( "DefaultResultPoster.postTestResults() Exception", e.toString() + " " + testKey.toString() + " payload=" + (payload==null ? "null" : payload ) );
            throw e;
        }
    }

    //private boolean isStatusCodeOk( int statusCode )
    //{
    //    return statusCode>=200 && statusCode<300;
    //}
    
    
    //private XMLGregorianCalendar getXmlDate( GregorianCalendar gc ) throws Exception
    //{
    //    return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
    //}




}
