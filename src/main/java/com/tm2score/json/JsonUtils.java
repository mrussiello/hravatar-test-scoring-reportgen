/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.json;

import com.tm2score.service.LogService;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map.Entry;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;

/**
 *
 * @author Mike
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

    public static String getJsonObjectAsString( JsonObject jo )
    {
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


    public static JsonObject getChildJsonObject( JsonObject jo, String name ) throws Exception
    {
        try
        {
            return (JsonObject) jo.getJsonObject(name);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JsonUtils.getChildJsonObject() " + name + ", JSON=" + getJsonObjectAsString( jo ) );

            return null;
        }
    }

    public static void appendToJsonObject( JsonObject jo, String name, String value ) throws Exception
    {
        try
        {
            jo = jsonObjectToBuilder(jo).add(name, value).build();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JsonUtils.appendToJsonObject() name=" + name + ", value=" + value + ", JSON=" + getJsonObjectAsString( jo ) );
        }
    }


    public static JsonObjectBuilder jsonObjectToBuilder(JsonObject jo) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        for (Entry<String, JsonValue> entry : jo.entrySet()) {
            job.add(entry.getKey(), entry.getValue());
        }

        return job;
    }    

}
