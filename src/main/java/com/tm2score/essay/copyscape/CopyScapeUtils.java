/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.essay.copyscape;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.HttpUtils;
import com.tm2score.xml.JaxbUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

/**
 *
 * @author Mike
 */
public class CopyScapeUtils
{

    private static boolean DEBUG = false;

    private static String COPYSCAPE_USERNAME = null;
    private static String COPYSCAPE_APIKEY = null;
    private static String COPYSCAPE_DOMAIN = null;

    private static String ADD_TEXT_METHOD = "addTextForChecking";
    private static String ADD_TEXT_URI = "v2.add-text-for-checking";

    private static String GET_TEXT_STATUS_METHOD = "getTextStatus";
    private static String GET_TEXT_STATUS_URI = "v2.get-text-status";

    private static String GET_PLAG_PERCENT_METHOD = "getPlagiarismPercent";
    private static String GET_PLAG_PERCENT_URI = "v2.get-plagiarism-percent";




    private static synchronized void init()
    {
        if( COPYSCAPE_USERNAME != null )
            return;

        COPYSCAPE_USERNAME = RuntimeConstants.getStringValue( "copyscape_username" );
        COPYSCAPE_APIKEY = RuntimeConstants.getStringValue( "copyscape_apikey" );
        COPYSCAPE_DOMAIN = RuntimeConstants.getStringValue( "copyscape_api_domain" );
    }



    public Object[] submitEssayForWebDuplicateContentCheck( String text, int tryCount) throws Exception
    {
        // CloseableHttpResponse r = null;
        
        com.tm2score.essay.copyscape.xml.Response responseXml = null;
        String r = null;
        
        try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
        {
            if( COPYSCAPE_USERNAME == null )
                init();

            if( text == null || text.isEmpty() )
                throw new Exception( "text is empty." );

            String url = COPYSCAPE_DOMAIN;


            // HttpHost targetHost = new HttpHost(url, 80, "http");

            // CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost post = new HttpPost( url );

            List<NameValuePair> al = new ArrayList<>();

            al.add( new BasicNameValuePair( "u", CopyScapeUtils.COPYSCAPE_USERNAME ) );
            al.add(new BasicNameValuePair( "k", CopyScapeUtils.COPYSCAPE_APIKEY ) );
            al.add( new BasicNameValuePair( "o", "csearch" ) );
            al.add( new BasicNameValuePair( "e", "UTF-8" ) );
            al.add( new BasicNameValuePair( "t", text ) );
            al.add( new BasicNameValuePair( "c", "1" ) );
            al.add( new BasicNameValuePair( "f", "xml" ) );
            al.add( new BasicNameValuePair( "x", DEBUG ? "1" : "0" ) );


              //  post.setEntity( new StringEntity(payload) );
            post.setEntity(new UrlEncodedFormEntity(al));

            //try( CloseableHttpResponse r = client.execute( post ) )
            //{
            r = client.execute(post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "CopyScapeUtils.submitEssayForPlagCheck() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "CopyScapeUtils.submitEssayForPlagCheck()statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    });

                // String ss = EntityUtils.toString(r.getEntity()) ;

                // LogService.logIt( "CopyScapeUtils.submitEssayForPlagCheck() url=" + url + " Response Code : " + r.getCode() + ", response=" + ss );

            responseXml = JaxbUtils.ummarshalCopyScapeXml( r );

            if( responseXml == null )
            {
                if( tryCount<1 )
                    throw new STException( "g.PassThru", "Plagiarism Server Error. Cound not parse the result XML from CopyScape. XML=" + r + ", the text=" + text );

                throw new Exception( "Plagiarism Server Error. Cound not parse the result XML from CopyScape. XML=" + r + ", the text=" + text );
            }

            if( responseXml.getError()!= null && !responseXml.getError().isEmpty()  )
            {
                if( tryCount<1 )
                    throw new STException( "g.PassThru", "Plagiarism Server Error. " + responseXml.getError() + ", text=" + text + ", respnse=" + r );
                throw new Exception( "Plagiarism Server Error. " + responseXml.getError() + ", text=" + text + ", respnse=" + r );

            }
                
            //    if( r.getEntity()!=null )
            //        EntityUtils.consume(r.getEntity());

            int count = responseXml.getCount();
            int queryWords = responseXml.getQuerywords();

            //int allWordsMatched = ( responseXml.getAllwordsmatched() == null ? 0 : responseXml.getAllwordsmatched() );
            //float pctMatched = ( responseXml.getAllpercentmatched() == null ? 0 : responseXml.getAllpercentmatched() );

            float highestPercentMatched = 0;
            String contentUrl = null;

            if( responseXml.getResult() != null )
            {
                for( com.tm2score.essay.copyscape.xml.Response.Result res : responseXml.getResult() )
                {


                    if( res.getPercentmatched() != null && res.getPercentmatched()>highestPercentMatched )
                    {
                        highestPercentMatched = res.getPercentmatched();
                        contentUrl= res.getViewurl();
                    }
                }
            }



            String out = "count=" + count + ", queryWords=" + queryWords + ", highestPercentMatched=" + highestPercentMatched;

            // LogService.logIt( "CopyScapeUtils.submitEssayForPlagCheck() " + out );

            Object[] d = new Object[3];

            d[0] = highestPercentMatched;
            d[1] = contentUrl;
            d[2] = out;

            return d; // "SUCCESS: " + out;
            // }
        }

        catch( ConnectionRequestTimeoutException e )
        {
            LogService.logIt( "CopyScapeUtils.submitEssayForPlagCheck() STERR " + e.toString() + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
            HttpUtils.resetPooledConnectionManagerIfNeeded();
            if( tryCount<1 )
                throw new STException( "g.PassThru", "Plagiarism Server Error, text=" + text + ", response=" + r );

            LogService.logIt( "CopyScapeUtils.submitEssayForPlagCheck() NON-FATAL " + e.toString() +", text=" + text + ", response=" + r );
            throw new Exception( "Plagiarism Server Error. text=" + text + ", response=" + r );
        }        
        catch( IOException e )
        {
            String csError = responseXml==null ? null : responseXml.getError();

            if( tryCount<1 )
                throw new STException( "g.PassThru", "Plagiarism Server Error. " + csError + ", text=" + text + ", response=" + r );

            LogService.logIt( "CopyScapeUtils.submitEssayForPlagCheck() NON-FATAL " + e.toString() +", " + csError + ", text=" + text + ", response=" + r );
            throw new Exception( "Plagiarism Server Error. " + csError + ", text=" + text + ", response=" + r );
            //LogService.logIt( "CopyScapeUtils.submitEssayForPlagCheck() NON-FATAL " + e.toString() );
            //throw e;
        }
        
        catch( STException e )
        {
            LogService.logIt( "STERR CopyScapeUtils.submitEssayForPlagCheck() NON-FATAL " + e.toString() );
            
            try
            {
                Thread.sleep( 5000 );
                tryCount++;
                return submitEssayForWebDuplicateContentCheck( text, tryCount );
            }
            catch( InterruptedException ee )
            {
                LogService.logIt( ee, "CopyScapeUtils.submitEssayForPlagCheck() Trying a another time. tryCount=" + tryCount );
                throw e;                
            }
        }
        
        catch( Exception e )
        {
            String msg = e.getMessage().toLowerCase();
            if( msg.contains("insufficient credit remaining") )
            {
                LogService.logIt( "STERR CopyScapeUtils.submitEssayForPlagCheck() NON-FATAL " + e.toString() + " Low credits. Sending Email to Admin." );
                EmailUtils eu = new EmailUtils();
                eu.sendEmailToAdmin("HR Avatar - Copyscape Credits Empty", "Error checking for Plagiarism. CopyScapeUtils.submitEssayForPlagCheck() NON-FATAL " + e.toString());
                throw new STException( "Copyscape Credits Empty" );
            }
            
            LogService.logIt( e, "CopyScapeUtils.submitEssayForPlagCheck() NON-FATAL" );
            throw e;
        }
        //finally
        //{
            //if( r != null )
            //    r.close();
        //}
    }


}
