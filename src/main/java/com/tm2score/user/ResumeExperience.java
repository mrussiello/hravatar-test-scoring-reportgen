/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.user;

import com.tm2score.service.LogService;
import com.tm2score.util.JsonUtils;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker
 */
public class ResumeExperience implements Serializable {
    
    String title;
    String company;
    String period;
    List<String> accomplishments;

    public ResumeExperience( JsonObject jo )
    {
        parseJo(jo);
    }
    
    private void parseJo( JsonObject jo )
    {
        try
        {
            title = JsonUtils.getStringFmJson(jo, "title");
            company = JsonUtils.getStringFmJson(jo, "company");
            period = JsonUtils.getStringFmJson(jo, "period");
            JsonArray ja = jo.getJsonArray("accomplishments" );
            
            accomplishments = new ArrayList<>();
            if( ja!=null && !ja.isEmpty() )
                accomplishments.addAll( ja.getValuesAs(JsonString::getString));
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ResumeExperience.parseJo() " + toString() );
        }
    }
    
    public JsonObjectBuilder getJsonObjectBuilder()
    {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("title", title==null ? "" : title );
        job.add("company", company==null ? "" : company );
        job.add("period", period==null ? "" : period );
        JsonArrayBuilder jab = Json.createArrayBuilder();
        
        if( this.accomplishments!=null )
        {
            for( String a : accomplishments )
            {
                if( a!=null && !a.isBlank() )
                    jab.add(a);
            }
        }
        job.add("accomplishments",jab);
        return job;
    }


    @Override
    public String toString() {
        return "ResumeExperience{" + "title=" + title + ", company=" + company + ", period=" + period + '}';
    }
    
    
    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getPeriod() {
        return period;
    }

    public List<String> getAccomplishments() {
        return accomplishments;
    }
    
    
    
}
