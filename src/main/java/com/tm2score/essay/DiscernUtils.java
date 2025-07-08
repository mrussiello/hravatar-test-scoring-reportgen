/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.essay;

import com.tm2score.entity.discern.Essay;
import com.tm2score.entity.essay.EssayPrompt;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.xml.XmlUtils;
import java.util.Date;

/**
 *
 * @author Mike
 */
public class DiscernUtils {

    public static final boolean USE_API_ESSAY = false;
    //public static final boolean USE_API_ESSAYGRADE = false;
        
    //private static final String DISCERN_LOGON_PATH = "/essay_site/login/";
    //private static final String DISCERN_ESSAY_PATH = "/essay_site/api/v1/essay/?format=json";
    // private static final String DISCERN_ESSAYGRADE_PATH = "/essay_site/api/v1/essaygrade/?format=json";

    //private static final String DISCERN_PROBLEM_URI = "/essay_site/api/v1/problem/";
    //private static final String DISCERN_ESSAY_URI = "/essay_site/api/v1/essay/";
    //private static final String DISCERN_ESSAYGRADE_URI = "/essay_site/api/v1/essaygrade/";

    private static Boolean discernOn = null;


    DiscernFacade discernFacade;
    // static DiscernBean discernBean = null;

    // static Client client;

    public static boolean isDiscernOn()
    {
        if( discernOn==null )
            discernOn = RuntimeConstants.getBooleanValue( "discernOn" );

        return discernOn;
    }

    
    public void checkForNewScore( UnscoredEssay ue ) throws Exception
    {
        if( ue.getScoreStatusTypeId()!= EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId() )
            throw new Exception( "Essay not in correct status. UnscoredEssayId=" + ue.getUnscoredEssayId() );

        if( ue.getDiscernEssayId()<=0 )
            throw new Exception( "UnscoredEssay.discernEssayId invalid: " + ue.getDiscernEssayId() + ", UnscoredEssayId=" + ue.getUnscoredEssayId() );

        //if( USE_API_ESSAYGRADE )
        //{
            //checkForNewScoreApi( ue );
        //    return;
        //}

        // this just checks the database rather than making a web services call to Discern. More reliable. 
        Object[] data = getEssayGradeViaEssayIdDirect( ue.getDiscernEssayId() );

        if( data != null )
        {
            ue.setComputedScore( (Float) data[0] );
            ue.setComputedConfidence( (Float) data[1] );
            ue.setScoreDate( (Date) data[2] );
            ue.setDiscernEssayGradeId( (Integer) data[3]);

            // Force boundaries
            if( ue.getComputedScore()<0 )
                ue.setComputedScore( 0 );

            if( ue.getComputedScore()>100 )
                ue.setComputedScore( 100 );

            ue.setScoreStatusTypeId( EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() );
        }
    }
    
    
    /*
    public void checkForNewScoreApi( UnscoredEssay ue ) throws Exception
    {
            if( ue.getScoreStatusTypeId() != EssayScoreStatusType.SUBMITTED.getEssayScoreStatusTypeId() )
                throw new Exception( "Essay not in correct status. UnscoredEssayId=" + ue.getUnscoredEssayId() );

            if( ue.getDiscernEssayId()<=0 )
                throw new Exception( "UnscoredEssay.discernEssayId invalid: " + ue.getDiscernEssayId() + ", UnscoredEssayId=" + ue.getUnscoredEssayId() );
            
            if( !isDiscernOn() )
                return;

            Date procStart = new Date();

            // // getDiscernBean();

            if( !DiscernBean.getLoggedOnToDiscern() )
                loginToDiscern();

            String essayUri = DISCERN_ESSAY_URI + ue.getDiscernEssayId() + "/";

            // response should be like
            // { "additional_predictors": "[]",
            //   "created": "2013-12-21T09:25:11",
            //   "essay_text": "This is my very first essay submitted for scoring here in this here place. Yeah!",
            // "essay_type": "test",
            // "essaygrades": ["/essay_site/api/v1/essaygrade/180/"],
            // "has_been_ml_graded": true,
            // "id": 64,
            // "modified": "2013-12-21T09:25:17",
            // "organization": null,
            // "problem": "/essay_site/api/v1/problem/6/",
            // "resource_uri": "/essay_site/api/v1/essay/64/",
            // "user": "/essay_site/api/v1/user/1/"}
            JsonStructure jsonst = sendGetToDiscern( essayUri );

            if( !jsonst.getValueType().equals( JsonValue.ValueType.OBJECT ) )
                throw new Exception( "Improper response type detected." + jsonst.getValueType().toString() );

            JsonObject jo = (JsonObject)jsonst;

            boolean scored = jo.getBoolean("has_been_ml_graded");

            if( !scored )
                return;

            JsonArray ja = jo.getJsonArray("essaygrades");

            int idx = ja.size()- 1;

            String essayGradeUri;
            int essayGradeId;
            Object[] gradeInfo;
            boolean foundScore = false;

            while( idx>= 0 )
            {
                essayGradeUri = ja.getString( idx );

                if( essayGradeUri==null || essayGradeUri.isEmpty() )
                {
                    LogService.logIt( "DiscernUtils.checkForNewScore() " + ue.toString() + ", essayGradeUri at index " + idx + " is empty " );
                    idx--;
                    continue;
                }

                essayGradeId = Integer.parseInt( essayGradeUri.substring( essayGradeUri.indexOf( "essaygrade/" ) + "essaygrade/".length(), essayGradeUri.length()-1 ) );

                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "DiscernUtils.checkForNewScore() essayGradeUri=" + essayGradeUri + ", essayGradeId=" + essayGradeId );

                if( essayGradeId>0 )
                {
                    Object[] data = getEssayGradeViaEssayGradeIdDirect( essayGradeId );

                    if( data != null )
                    {
                        ue.setComputedScore( (Float) data[0] );
                        ue.setComputedConfidence( (Float) data[1] );
                        ue.setScoreDate( (Date) data[2] );
                        ue.setDiscernEssayGradeId( (Integer) data[3]);

                        // Force boundaries
                        if( ue.getComputedScore()<0 )
                            ue.setComputedScore( 0 );

                        if( ue.getComputedScore()>100 )
                            ue.setComputedScore( 100 );

                        ue.setScoreStatusTypeId( EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() );

                        return;
                    }
                }


                gradeInfo = getEssayGradeViaApi( essayGradeId );

                if( gradeInfo != null )
                {
                    ue.setComputedScore( (Float) gradeInfo[0] );
                    ue.setComputedConfidence( (Float) gradeInfo[1] );
                    ue.setScoreDate( (Date) gradeInfo[2] );
                    ue.setDiscernEssayGradeId(essayGradeId);

                    // Force boundaries
                    if( ue.getComputedScore()<0 )
                        ue.setComputedScore( 0 );

                    if( ue.getComputedScore()>100 )
                        ue.setComputedScore( 100 );

                    foundScore = true;
                    break;
                }

                idx--;
            }

            ue.setScoreStatusTypeId( foundScore ? EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() : EssayScoreStatusType.FAILED.getEssayScoreStatusTypeId() );

            Tracker.addResponseTime( "Discern - Check for new Score", new Date().getTime() - procStart.getTime() );

    }
    */

    public static boolean hasValidTextForDiscern( String t )
    {
        if( t==null || t.isBlank() )
            return false;
        
        // Remove non-Ascii characters (Discern does this).
        String tt = t.replaceAll( "[^\\x00-\\x7F]", "" );
        
        tt = XmlUtils.stripNonValidXMLCharacters(tt);

        if( t!=null && tt.length()!=t.length() )
        {
            LogService.logIt( "DiscernUtils.hasValidTextForDiscern() new length=" + tt.length() + ", previous length=" + t.length() );
        }
        
        return tt!=null && !tt.isBlank();
    }
    

    public int saveDiscernEssay( EssayPrompt ep, String essayType, String essayText ) throws Exception
    {
        // Remove non-Ascii characters (Discern does this).
        essayText = essayText.replaceAll( "[^\\x00-\\x7F]", "" );
        
        essayText = XmlUtils.stripNonValidXMLCharacters(essayText);

        //if( USE_API_ESSAY )
        //    return  saveDiscernEssayApi( ep, essayType, essayText );
        
        try
        {
            if( discernFacade==null )
                discernFacade = DiscernFacade.getInstance();
            
            Essay essay = new Essay();

            essay.setEssayType(essayType);
            essay.setEssayText(essayText);
            essay.setUserId( ep.getDiscernUserId() );
            essay.setProblemId( ep.getDiscernProblemId() );
            essay.setAdditionalPredictors("[]");

            discernFacade.saveDiscernEssay(essay);

            return essay.getEssayId();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernUtils.saveDiscernEssay() " + ep.toString() + ", essay=" + essayText );
            throw e;
        }        
    }

    /*
    public int saveDiscernEssayApi( EssayPrompt ep, String essayType, String essayText ) throws Exception
    {
        if( !isDiscernOn() )
            return 0;
        
        if( !DiscernBean.getLoggedOnToDiscern() )
            loginToDiscern();

        String problemUri = DISCERN_PROBLEM_URI + ep.getDiscernProblemId() + "/";

        // Next, create the prompt on the Discern System.
        // { "essay_type": "train", "essay_text": the_text, "problem": the_problem_uri, }
        JsonObject jso = Json.createObjectBuilder().add( "essay_type", essayType )
                                                    .add( "essay_text" , essayText )
                                                    .add( "problem", problemUri )
                                                    .build();

        // Response should look like:
        JsonStructure jsonst = sendPostToDiscern(  jso, DISCERN_ESSAY_PATH );

        if( !jsonst.getValueType().equals( JsonValue.ValueType.OBJECT ) )
            throw new Exception( "Improper response type detected." + jsonst.getValueType().toString() );

        JsonObject jo = (JsonObject)jsonst;

        int id = jo.getInt( "id" );

        return id;
    }
    */
    
    

    /**
     * returns null if grading record is not successful
     *
     * returns
     *    [0] score - Float
     *    [1] confidence - Float
     *    [2] Date
     *
     * @param essayGradeId
     * @return
     * @throws Exception
     *
    private Object[] getEssayGradeViaApi( int essayGradeId ) throws Exception
    {
        try
        {
            if( !isDiscernOn() )
                return null;

            // getDiscernBean();

            if( !DiscernBean.getLoggedOnToDiscern() )
                loginToDiscern();

            if( essayGradeId<=0 )
                throw new Exception( "essayGradeId invalid " + essayGradeId );

            String essayUri = DISCERN_ESSAYGRADE_URI + essayGradeId + "/";

            // response should be like
            // {"annotated_text": "",
            //  "confidence": "0.590020714",
            //  "created": "2013-12-21T09:25:17",
            //  "essay": "/essay_site/api/v1/essay/64/",
            //  "feedback": "",
            //  "grader_type": "ML",
            //  "id": 180,
            //  "modified": "2013-12-21T09:25:17",
            //  "premium_feedback_scores": "[]",
            //  "resource_uri": "/essay_site/api/v1/essaygrade/180/",
            //  "success": true,
            //  "target_scores": "[32]",
            //  "user": null}
            JsonStructure jsonst = sendGetToDiscern( essayUri );

            if( !jsonst.getValueType().equals( JsonValue.ValueType.OBJECT ) )
                throw new Exception( "Improper response type detected." + jsonst.getValueType().toString() );

            JsonObject jo = (JsonObject)jsonst;

            boolean success = jo.getBoolean("success" );

            if( !success )
            {
                LogService.logIt( "DiscernUtils.getEssayGrade() essayGradeId=" + essayGradeId + ", not successful." );
                return null;
            }

            int[] scrs = parseJsonStringAsIntArray( jo.getString( "target_scores" ) );

            if( scrs.length <1 )
            {
                LogService.logIt( "DiscernUtils.getEssayGrade() essayGradeId=" + essayGradeId + ", no scores in target_scores array." );
                return null;
            }

            float score = scrs[0];

            float confidence = Float.parseFloat( jo.getString( "confidence" ) );

            String dateStr = jo.getString( "created" );

            Date dt = dateStr==null || dateStr.isEmpty() ? new Date() : parseJsonDateTime( dateStr );

            return new Object[] { score, confidence, dt };
        }

        catch( jakarta.ws.rs.NotAuthorizedException e )
        {
            LogService.logIt( e, "DiscernUtils.getEssayGrade() 401 Error on EssayGrade Get. essayGradeId=" + essayGradeId );

           return getEssayGradeViaEssayGradeIdDirect( essayGradeId );

        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernUtils.getEssayGrade() essayGradeId=" + essayGradeId );

            throw e;
        }
    }
    */

    /**
     * returns null if grading record is not successful
     *
     * returns
     *    [0] score - Float
     *    [1] confidence - Float
     *    [2] Date
     *
     * @param discernEssayGradeId
     * @return
     * @throws Exception
     *
    private Object[] getEssayGradeViaEssayGradeIdDirect( int discernEssayGradeId ) throws Exception
    {
        try
        {
            if( discernFacade == null )
                discernFacade = DiscernFacade.getInstance();

            return discernFacade.checkForEssayScoreDirect(0, discernEssayGradeId );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernUtils.getEssayGradeViaEssayGradeIdDirect() " );

            return null;
        }
    }
    */

    private Object[] getEssayGradeViaEssayIdDirect( int discernEssayId ) throws Exception
    {
        try
        {
            if( discernFacade == null )
                discernFacade = DiscernFacade.getInstance();

            return discernFacade.checkForEssayScoreDirect( discernEssayId, 0 );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernUtils.getEssayGradeViaEssayIdDirect() discernEssayId=" + discernEssayId );

            return null;
        }
    }




    /*
    public void logoffDiscern() throws Exception
    {
        if( !isDiscernOn() )
            throw new Exception( "Discern is not enabled." );

        // getDiscernBean();

        if( !DiscernBean.getLoggedOnToDiscern() )
            return;

        DiscernBean.logOffDiscern();
    }
    */


    /*
    public void loginToDiscern() throws Exception
    {
        // LogService.logIt( "DiscernUtils.loginToDiscern() Start " );
        
        if( !isDiscernOn() )
            throw new Exception( "Discern is not enabled." );

        // getDiscernBean();

        if( DiscernBean.getLoggedOnToDiscern() )
            return;

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "DiscernUtils.loginToDiscern() BBB " );
        
        Date procStart = new Date();

        // discernBean = new DiscernBean();

        if( DiscernBean.getClient()==null )
        {
            DiscernClientFilter dcf = DiscernBean.getClientFilter() != null ? DiscernBean.getClientFilter() : new DiscernClientFilter();

            DiscernBean.setClientFilter(dcf);
            DiscernBean.setClient( ClientBuilder.newClient().register( dcf ) );
        }

        // LogService.logIt( "DiscernUtils.loginToDiscern() CCC " );
        
        // if( client )
        Client client = DiscernBean.getClient();

        WebTarget target = client.target( RuntimeConstants.getStringValue("discernBaseUri") + ":" + RuntimeConstants.getStringValue("discernPort") + DISCERN_LOGON_PATH );

       
        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "DiscernUtils.loginToDiscern() login URL=" + target.getUri() );

        Invocation.Builder ib = target.request(MediaType.APPLICATION_JSON);

        JsonObject jso = Json.createObjectBuilder().add("username", RuntimeConstants.getStringValue("discernUsername") ).add( "password", RuntimeConstants.getStringValue("discernPassword") ).build();

        Entity ent = Entity.json( jso);

        String response = ib.header( "Content-Type", "application/json" ).post( ent, String.class );

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "DiscernUtils.processLoginToDiscern() back from call response." + response );

        JsonReader reader = Json.createReader(new StringReader( response ) );
        JsonStructure jsonst = reader.read();

        if( !jsonst.getValueType().equals( JsonValue.ValueType.OBJECT ) )
            throw new Exception( "Improper response type detected." + jsonst.getValueType().toString() );

        JsonObject jo = (JsonObject)jsonst;

        String message = jo.getString( "message" );
        boolean success = jo.getBoolean( "success" );

        if( success )
        {
            // setStringInfoMessage( "Login to Discern was successful." );
            DiscernBean.setLoggedOnToDiscern( true );
        }

        else
        {
            EmailUtils.getInstance().sendEmailToAdmin( "Discern Error", "Discern login failed with message: " + message );
            throw new Exception( "Discern login failed. " + message );
        }

        Tracker.addResponseTime( "Discern - Login", new Date().getTime() - procStart.getTime() );


    }
    */





    /*
    private JsonStructure sendGetToDiscern( String path ) throws Exception
    {
        try
        {
            if( !isDiscernOn() )
                throw new Exception( "Discern is not enabled." );

            Date procStart = new Date();

            Cookie authCookie = DiscernBean.getClientFilter().getSessionCookie();
            // Cookie csrfCookie = DiscernBean.getClientFilter().getCsrfToken();

            if( authCookie==null )
                throw new Exception( "No AuthCookie found. Cannot make request." );

            Client client = DiscernBean.getClient(); // ClientBuilder.newClient();

            if( client==null )
                throw new Exception( "Client is null!" );

            String tgtUrl=RuntimeConstants.getStringValue("discernBaseUri") + ":" + RuntimeConstants.getStringValue("discernPort") + path;

            if( ScoreManager.DEBUG_SCORING )
               LogService.logIt( "DiscernUtils.sendGetToDiscern() AAA Setting url " + tgtUrl );

            WebTarget target = client.target( tgtUrl );

            Invocation.Builder ib = target.request(MediaType.APPLICATION_JSON);

            if( ScoreManager.DEBUG_SCORING )
               LogService.logIt( "DiscernUtils.sendGetToDiscern() BBB Sending get to " + tgtUrl + ", authCookie=" + authCookie.getValue() );

            String response = ib.header( "Content-Type", "application/json" ).cookie( authCookie ).get( String.class);

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "DiscernUtils.sendToDiscern() CCC result is " + response );

            JsonReader reader = Json.createReader(new StringReader( response ) );
            JsonStructure jsonst = reader.read();

            Tracker.addResponseTime( "Discern - Send Get to Discern", new Date().getTime() - procStart.getTime() );

            return jsonst;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DiscernUtils.sendGetToDiscern() path=" + path );

            throw e;
        }
    }
    */



    /*
    private JsonStructure sendPostToDiscern( JsonObject jso, String path ) throws Exception
    {
        String response = null;

        try
        {
            if( !isDiscernOn() )
                throw new Exception( "Discern is not enabled." );

            Date procStart = new Date();

            Entity ent = Entity.json( jso);

            Cookie authCookie = DiscernBean.getClientFilter().getSessionCookie();
            Cookie csrfCookie = DiscernBean.getClientFilter().getCsrfToken();

            if( authCookie==null )
                throw new Exception( "No AuthCookie found. Cannot make request." );

            Client client = DiscernBean.getClient(); // ClientBuilder.newClient();

            if( client==null )
                throw new Exception( "Client is null!" );

            String tgtUrl=RuntimeConstants.getStringValue("discernBaseUri") + ":" + RuntimeConstants.getStringValue("discernPort") + path;

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "DiscernUtils.sendToDiscern() AAA Setting url " + tgtUrl );

            WebTarget target = client.target( tgtUrl );

            Invocation.Builder ib = target.request(MediaType.APPLICATION_JSON);

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "DiscernUtils.sendToDiscern() BBB Sending post to " + tgtUrl + ", authCookie=" + authCookie.getValue() + ", csrfCookie=" + ( csrfCookie==null ? "null" : csrfCookie.getValue() ) );

            StringWriter sw = new StringWriter();
            JsonWriter jw = Json.createWriter( sw );
            jw.writeObject(jso);
            jw.close();

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "DiscernUtils.sendToDiscern() BBB-X SENDING String=" + sw.toString() );

            response = ib.header( "Content-Type", "application/json" ).cookie( authCookie ).cookie( csrfCookie ).post( ent, String.class);

            if( response != null )
                response = XmlUtils.stripNonValidXMLCharacters( response );

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "DiscernUtils.sendToDiscern() CCC result is " + response );

            JsonReader reader = Json.createReader(new StringReader( response ) );
            JsonStructure jsonst = reader.read();

            Tracker.addResponseTime( "Discern - Send Post to Discern", new Date().getTime() - procStart.getTime() );

            return jsonst;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernUtils.sendToDiscern() response is " + response );

            throw e;
        }
    }
    */


    /*
    public static int[] parseJsonStringAsIntArray( String inStr  )
    {
            String s = inStr.replaceAll("\\[", "" );
            s = s.replaceAll("\\]", "" );
            String[] vals = s.split(",");

            int[] out = new int[ vals.length ];

            for( int i=0;i<vals.length; i++ )
                out[i] = Integer.parseInt( vals[i] );

            return out;
    }
    */


    /*
    public static Date parseJsonDateTime( String dateString)
    {
        if (dateString == null) return null;
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (dateString.contains("T"))
            dateString = dateString.replace('T', ' ');

        //if (dateString.contains("Z"))
        //    dateString = dateString.replace("Z", "+0000");
        //else
        //    dateString = dateString.substring(0, dateString.lastIndexOf(':')) + dateString.substring(dateString.lastIndexOf(':')+1);

        try {
            return fmt.parse(dateString);
        }
        catch (ParseException e) {
            LogService.logIt( e, "Could not parse datetime: " + dateString);
            return null;
        }
    }
    */

}
