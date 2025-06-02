/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.user;

import com.tm2score.service.LogService;
import com.tm2score.util.JsonUtils;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.io.Serializable;

/**
 *
 * @author miker
 */
public class ResumeEducation implements Serializable {
    
    String degree;
    String institution;
    String year;
    
    public ResumeEducation(JsonObject jo)
    {
        parseJo(jo);
    }
    
    private void parseJo( JsonObject jo )
    {
        try
        {
            degree = JsonUtils.getStringFmJson(jo, "degree");
            institution = JsonUtils.getStringFmJson(jo, "institution");
            year = JsonUtils.getStringFmJson(jo, "year");
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ResumeEducation.parseJo() " + toString() );
        }
    }
    
    public JsonObjectBuilder getJsonObjectBuilder()
    {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("degree", degree==null ? "" : degree );
        job.add("institution", institution==null ? "" : institution );
        job.add("year", year==null ? "" : year );
        return job;
    }

    public String getDegree() {
        return degree;
    }

    public String getInstitution() {
        return institution;
    }

    public String getYear() {
        return year;
    }
    
    
}
