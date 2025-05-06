/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tm2score.service.LogService;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map.Entry;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;

/**
 *
 * @author miker_000
 */
public class JsonUtils {

    
    public static JsonObject getJsonObject( String s )
    {
        try
        {
            s = s.replaceAll("\n", "\\n");
            JsonReader jsonReader = Json.createReader(new StringReader( s ) );
            JsonObject o = jsonReader.readObject();
            jsonReader.close();
            return o;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JsonUtils.getJsonObject() " + s );

            return null;
        }
    }
    
    
    public static JsonObjectBuilder getJsonObjectBuilder() 
    {
        return Json.createObjectBuilder();
    }    
    
    public static JsonArrayBuilder getJsonArrayBuilder() 
    {
        return Json.createArrayBuilder();
    }    
    
    public static String getPrettyJsonStr( String jsonStr )
    {
        if( jsonStr==null || jsonStr.trim().isEmpty() )
            return "";
        
        // JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonElement el = JsonParser.parseString(jsonStr); // .parse(jsonStr);
        return gson.toJson(el); // done        
    }
    
    
    public static JsonObjectBuilder jsonObjectToBuilder(JsonObject jo) 
    {
        JsonObjectBuilder job = Json.createObjectBuilder();

        for (Entry<String, JsonValue> entry : jo.entrySet()) 
        {
            job.add(entry.getKey(), entry.getValue());
        }

        return job;
    }    

    public static String getJsonObjectAsString( JsonObject jo )
    {
        if( jo==null )
            return "null";
        try
        {            
            StringWriter os = new StringWriter();
            JsonWriter jsonWriter = Json.createWriter( os );
            jsonWriter.writeObject(jo);
            jsonWriter.close();
            return os.toString();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JsonUtils.getJsonObjectAsString() " );
            return null;
        }
    }
    
    
    
    public static JsonObject convertJsonStringtoObject(String jsonStr) throws Exception
    {
        try
        {
            JsonReader r = Json.createReader( new StringReader(jsonStr) );

            return r.readObject();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JsonUtils.convertJsonStringtoObject() jsonStr=" + jsonStr  );
        }

        return null;
    }


    public static String convertJsonObjecttoString( JsonObject jo ) throws Exception
    {
        try
        {
            StringWriter sw = new StringWriter();
            JsonWriter jw = Json.createWriter(sw);
            jw.writeObject(jo);
            jw.close();

           return sw.getBuffer().toString();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "JsonUtils.convertJsonObjecttoString() " );
            
            return null;
        }
              
    }
    
    public static String getStringFmJson(JsonObject o, String key) {
        
        if( o==null || !o.containsKey(key) || o.isNull(key) )
                return null;
        
        try {
            return o.getString(key);
        } catch (Exception e) {
            LogService.logIt(e, "JsonUtils.getStringFmJson() NONFatal key=" + key);
            return null;
        }
    }
    
    
}
