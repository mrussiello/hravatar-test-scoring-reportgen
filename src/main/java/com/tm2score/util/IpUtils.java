/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.user.UserFacade;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParsingException;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

/**
 *
 * @author Mike
 */
//@Stateless
public class IpUtils {

    UserFacade userFacade;
    //@PersistenceContext
    //EntityManager em;

    /*
    public static IpFacade getInstance()
    {
        try
        {
            return (IpFacade) InitialContext.doLookup( "java:module/IpFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IpUtils.getInstance() " );

            return null;
        }
    }
    */


    /*
    public void updateIpLocationData( User user , String ipAddress ) throws Exception
    {
        if( ipAddress == null )
        {
            LogService.logIt( "IpUtils.updateIpLocationData() ipAddress is null. " + user.toString() );
            return;
        }

        String[] ipData = getIPLocationData( ipAddress );

        user.setIpCountry( ipData[0] );
        user.setIpState( ipData[1] );
        user.setIpCity( ipData[2] );
        user.setIpZip( ipData[3] );
        user.setIpTimezone( ipData[4] );

        if( user.getIpCountry()!=null && !user.getIpCountry().isEmpty() )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();

             Country ctry = userFacade.getCountryByCode( user.getIpCountry() );

             if( ctry != null )
                 user.setGeographicRegionId( ctry.getGeographicRegionId() );

             if( ctry != null )
                 user.setCountryCode( ctry.getCountryCode() );
        }

        if( user.getIpTimezone()!=null && !user.getIpTimezone().isEmpty() )
            user.setTimeZoneId( user.getIpTimezone() );
    }
    */


    /*
    public String[] getIPLocationDataNoError( String ipAddress )
    {
        try
        {
            return getIPLocationData( ipAddress );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "IpUtils.getIPLocationDataNoError() ipAddress=" + ipAddress );
            return new String[5];
        }
    }
    */


    public static boolean getIsIpAddressValid( String ipAddress )
    {
        try
        {
            return InetAddressValidator.getInstance().isValid(ipAddress);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "IpUtils.getIsIpAddressValid() " + ipAddress );
        }
        return true;
    }



    /**
     * returns
     *    data[0]=Country
     *    data[1]=state
     *    data[2]=city
     *    data[3]=zip code
     *    data[4]=time zone
     *
     * @param ipAddress
     * @return
     * @throws Exception
     */
    public String[] getIPLocationData( String ipAddress, int count) throws Exception
    {
        // LogService.logIt( "IpUtils.getIPLocationData() AAA Start " + ipAddress );

        String[] out = new String[5];


        if( ipAddress==null || ipAddress.equalsIgnoreCase("127.0.0.1") )
        {
            out[0] = "US";
            out[1] = "VA";
            out[2] = "Aldie";
            out[3] = "20105";
            out[4] = "-5";
            return out;
        }

        if( ipAddress.indexOf(",")>0 )
            ipAddress=ipAddress.substring(0,ipAddress.indexOf(",")).trim();

        if( !getIsIpAddressValid( ipAddress ) )
        {
            LogService.logIt("IpFacade.updateIpLocationData() ipAddress is NOT Valid. ipAddress=" + ipAddress );
            return out;
        }


        // String uri = RuntimeConstants.getStringValue("FreeGeoIpURI") + ipAddress;
        String uri = RuntimeConstants.getStringValue("FreeGeoIpURI") + ipAddress + "?access_key=" + RuntimeConstants.getStringValue( "IpStackAccessKey" );

        String resultStr = null;

        try
        {
            // CloseableHttpResponse r = null;

            //int statusCode = 0;

            count++;

            // CloseableHttpClient client = null;

            try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
            {
                // CloseableHttpClient client =  HttpClients.createDefault(); //  HttpClientBuilder.create().build();

                HttpGet get = new HttpGet( uri );

                get.addHeader( "Accept", "application/json" );

                //LogService.logIt( "IpUtils.getIPLocationData() BBB sending to  uri=" + uri );
                //try( CloseableHttpResponse r = client.execute(get ) )
                //{
                resultStr = client.execute(get, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "IpUtils.getIPLocationData() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    String ss = EntityUtils.toString(entity2);
                    EntityUtils.consume(entity2);
                    if( status<200 || status>=300 )
                        throw new IOException( "IpUtils.getIPLocationData() statusCode="+ status + ", reason=" + response.getReasonPhrase() + " response=" + ss );
                    return ss;
                    } );
                    //LogService.logIt( "IpUtils.getIPLocationData() uri=" + uri + ", Response Code : " + r.getCode() );

                    //statusCode = r.getCode();

                    //if( statusCode != HttpStatus.SC_OK )
                    //{
                        //LogService.logIt( "IpUtils.getIPLocationData() ERROR connecting to service. BBB.1 Method failed: " + r.getReasonPhrase() + ", url=" + uri );
                        //return out;
                        // throw new Exception( "Logon failed with code " + r.getReasonPhrase() );
                    //}

                    //LogService.logIt( "IpUtils.getIPLocationData() CCC Parsing Response." );

                    //resultStr = getJsonFromResponse( r );

                    //if( r.getEntity()!=null )
                    //    EntityUtils.consume(r.getEntity());
                //}
                //LogService.logIt( "IpUtils.getIPLocationData() DDD uri=" + uri + ", resultStr: " + resultStr );
            }

            catch( ConnectionRequestTimeoutException e )
            {
                LogService.logIt( "IpUtils.getIPLocationData() STERR " + e.toString() + ", req stats=" + HttpUtils.getConnManagerStatsStr() ); 
                HttpUtils.resetPooledConnectionManagerIfNeeded();
                if( count<=3 )
                {
                    Thread.sleep(5000);
                    return getIPLocationData( ipAddress, count);
                }

                return out;
            }        
            
            catch( IOException e )
            {
                LogService.logIt("IpUtils.getIPLocationData() ERROR getting IP Data. " + e.toString() +", ip=" + ipAddress + ", uri=" + uri + ", count=" + count + ", resultStr=" + resultStr );

                if( count<=3 )
                {
                    Thread.sleep(5000);
                    return getIPLocationData( ipAddress, count);
                }

                return out;
            }

            //catch( SSLHandshakeException e )
            //{
            //    LogService.logIt("IpUtils.getIPLocationData() ERROR getting IP Data. " + e.toString() +", ip=" + ipAddress + ", uri=" + uri );
           //     return out;
            //}

            catch( Exception e )
            {
                LogService.logIt(e, "IpUtils.getIPLocationData() EEE Exception getting IP Data ip=" + ipAddress + ", uri=" + uri + ", resultStr=" + resultStr );
                throw e;
            }

            //finally
            //{
                //if( r != null )
                //    r.close();
            //}


            JsonReader jr = Json.createReader(new StringReader( resultStr ));

            JsonObject jo = jr.readObject();

            String countryCode = getStringFmJson( jo, "country_code" );  // jo.containsKey( "country_code" ) && !jo.isNull( "country_code" ) ? jo.getString( "country_code" ) : null;
            String state = getStringFmJson( jo, "region_name" );  // jo.containsKey( "region_name" ) && !jo.isNull( "region_name" ) ? jo.getString( "region_name" ) : null;

            if( state==null ) // && jo.containsKey( "region_code" ) && !jo.isNull( "region_code" ) )
                state = getStringFmJson( jo, "region_code" );  // jo.getString( "region_code" );

            String city =  getStringFmJson( jo, "city" );  // jo.containsKey( "city" ) && !jo.isNull( "city" ) ? jo.getString( "city" ) : null;
            // String zipCode = jo.containsKey( "zip_code" ) && !jo.isNull( "zip_code" ) ? jo.getString( "zip_code" ) : null;
            String zipCode =  getStringFmJson( jo, "zip" );  // jo.containsKey( "zip" ) && !jo.isNull( "zip" ) ? jo.getString( "zip" ) : null;

            String timeZone = null; //  getStringFmJson( jo, "time_zone" );  // jo.containsKey( "time_zone" ) && !jo.isNull( "time_zone" ) ? jo.getString( "time_zone" ) : null;

            JsonObject jo2 = jo.containsKey( "time_zone" ) ? jo.getJsonObject( "time_zone") : null;

            if( jo2 != null )
                timeZone = getStringFmJson( jo2, "id" );

            out[0] = countryCode;
            out[1] = state;
            out[2] = city;
            out[3] = zipCode;
            out[4] = timeZone;

            // LogService.logIt( "IpUtils.getIPLocationData() " + ipAddress + ", country=" + countryCode + ", state=" + state + ", city=" + city );

            return out;
        }
        catch( JsonParsingException e )
        {
            LogService.logIt("IpUtils.getIPLocationData( " + ipAddress + " ) " + e.toString() + ", uri=" + uri + ", resultStr=" + resultStr );
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "IpUtils.getIPLocationData( " + ipAddress + " ) uri=" + uri );

            return out;
        }
    }


    private String getStringFmJson( JsonObject jo, String key )
    {
        if( key==null || key.isEmpty() || jo==null || !jo.containsKey(key) || jo.isNull(key) )
            return null;

        try
        {
            return jo.getString(key);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "IPUtils.getStringFmJson() key=" + key );
            return null;
        }
    }

    private   String getJsonFromResponse(CloseableHttpResponse response) throws IOException, Exception {

        StringBuilder sb = null;

        try {
            if ( response != null ) {

                String line = "";
                sb = new StringBuilder();

                InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent(), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                while ( (line = bufferedReader.readLine()) != null ) {
                        sb.append(line);
                }
            }
        } catch (IOException e) {
                throw new Exception(e.getMessage());
        } finally {
                if ( response != null ) {
                        response.close();
                }
        }

        return sb==null ? "" : sb.toString();
    }




}
