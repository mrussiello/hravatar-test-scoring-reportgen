/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.api;

import com.tm2score.entity.event.TestKey;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventLogUtils;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import java.util.GregorianCalendar;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Mike
 */
public class DefaultResultPosterWS implements ResultPoster {

    TestKey testKey;

    // @Inject
    EventFacade eventFacade;

    public DefaultResultPosterWS( TestKey tk )
    {
        this.testKey = tk;
    }
    
    
    @Override
    public void postTestResults() throws Exception
    {
        AssessmentResult arr = null;
        AssessmentResult.AssessmentStatus aoas = null;
        String payload = null;
 
        String url = null;

        if( testKey !=null )
            url = testKey.getResultPostUrl();
        
        try
        {

            if( url==null || url.isEmpty() )
                throw new Exception( "ResultPostUrl is missing: " + url );

            LogService.logIt( "DefaultResultPosterWS.postTestResults() Start " + testKey.toString() );

            // Prepare the content
            arr = new AssessmentResult();
            aoas = new AssessmentResult.AssessmentStatus();

            AssessmentStatusCreator asc = new AssessmentStatusCreator();

            // Since includeScoreCode for AssessmentStatusRequest and AssessmentOrderRequest are sligntly different, need to translate here. 
            // The AssessmentStatusCreator uses includeScoreCode values asociated with AssessmentStatusRequest.
            
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

            LogService.logIt( "DefaultResultPosterWS.postTestResults() have payload size=" + payload.length() + ", testKeyId=" + testKey.getTestKeyId() + ", url=" + url );

            Client client = ClientBuilder.newClient();
            
            WebTarget myResource = client.target( url );

            Invocation.Builder builder = myResource.request( new MediaType[] {MediaType.TEXT_XML_TYPE,MediaType.TEXT_PLAIN_TYPE,MediaType.TEXT_HTML_TYPE, MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE });

            Entity ent = Entity.xml( arr );

            String response = builder.post( ent, String.class );

            LogService.logIt( "DefaultResultPosterWS.postTestResults() back from call response." + response );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DefaultResultPosterWS.postTestResults() url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload ) );
            TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, 0, "DefaultResultPosterWS.postTestResults() ConnectException, url=" + url + ", error: " + e.toString() + ", payload.size=" + (payload==null ? "null" : payload.length()), null, null );                                
            EmailUtils.getInstance().sendEmailToAdmin( "DefaultResultPosterWS.postTestResults() ConnectException", e.toString() + "  url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload ) );

            throw e;
        }


    }

    private XMLGregorianCalendar getXmlDate( GregorianCalendar gc ) throws Exception
    {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
    }




}
