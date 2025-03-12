/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.googlecloud;

import com.tm2score.service.LogService;
import com.tm2score.util.JsonUtils;
import java.io.StringReader;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

/**
 *
 * @author miker_000
 */
public class GoogleApiException extends Exception {
    
    int code;
    String message;
    String note;
    String json;

    public GoogleApiException( String note, int code, String message )
    {
        this.note = note;
        this.code=code;
        this.message=message;
    }

    
    public GoogleApiException( String note, String jsonStr )
    {
        this.json = jsonStr;
        this.note = note;
        
        parseJsonString();
    }

    public GoogleApiException( String note, JsonObject errJo )
    {
        
        this.note = note;
        
        try
        {
            parseJsonErrorObject( errJo );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "GoogleApiException() " + note );
        }
    }
    
    
    @Override
    public String toString() {
        return "GoogleException{" + "code=" + code + ", message=" + message + ", note=" + note + '}';
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
    
    
    
    
    private void parseJsonString()
    {
        if( json==null || json.isEmpty() )
            return;

        try
        {
            JsonReader jr = Json.createReader(new StringReader( json ));

            JsonObject jo = jr.readObject();
            
            if( !jo.containsKey("error") )                
                throw new Exception( "json has no error element." );

            JsonObject errJo = jo.getJsonObject("error");
            
            parseJsonErrorObject( errJo );
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleException.parseJsonString() json=" + json );
        }
        
    }

    private void parseJsonErrorObject( JsonObject errJo ) throws Exception
    {
        if( errJo==null )
            return;

        try
        {      
            if( errJo.containsKey("code") )
                code = errJo.getInt("code");
            
            if( errJo.containsKey("message"))
                message = errJo.getString("message");                
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "GoogleException.parseJsonErrorObject() " + JsonUtils.convertJsonObjecttoString(errJo) );
        }
        
    }

    
}
