/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ibmcloud;

import com.tm2score.service.LogService;
import com.tm2score.util.JsonUtils;
import java.io.StringReader;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParsingException;

/**
 *
 * @author miker_000
 */
public class IbmApiException extends Exception {
    
    int code;
    String message;
    String note;
    String json;

    public IbmApiException( String note, int code, String message )
    {
        this.note = note;
        this.code=code;
        this.message=message;
    }

    
    public IbmApiException( String note, String jsonStr )
    {
        this.json = jsonStr;
        this.note = note;
        
        parseJsonString();
    }

    public IbmApiException( String note, JsonObject errJo )
    {
        
        this.note = note;
        
        try
        {
            parseJsonErrorObject( errJo );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "IbmApiException() " + note );
        }
    }
    
    
    @Override
    public String toString() {
        return "IbmApiException{" + "code=" + code + ", message=" + message + ", note=" + note + '}';
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
            
            if( jo.containsKey("code") )
                code = jo.getInt("code");
            
            if( jo.containsKey("error"))
                message = jo.getString("error");                
        }
        
        catch( JsonParsingException e )
        {
            LogService.logIt( "IbmException.parseJsonString() Error parsing json String for exception. Json appears to be invalid. " + e.toString() + ", json=" + json );            
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "IbmException.parseJsonString() json=" + json );
        }
        
    }

    private void parseJsonErrorObject( JsonObject errJo ) throws Exception
    {
        if( errJo==null )
            return;

        try
        {      
            if( errJo.containsKey("code") && !errJo.isNull("code") )
                code = errJo.getInt("code");
            
            if( errJo.containsKey("message") && !errJo.isNull("message"))
                message = errJo.getString("message");                
        }
        
        catch( Exception e )
        {
            LogService.logIt(e, "IbmException.parseJsonErrorObject() " + JsonUtils.convertJsonObjectToString(errJo) );
        }
        
    }

    
}
