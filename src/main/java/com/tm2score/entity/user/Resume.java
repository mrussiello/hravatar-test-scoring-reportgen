package com.tm2score.entity.user;


import com.tm2score.global.I18nUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.ResumeEducation;
import com.tm2score.user.ResumeExperience;
import com.tm2score.util.JsonUtils;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


@Entity
@Table( name = "xresume" )
@NamedQueries( {
        @NamedQuery( name = "Resume.findByResumeId", query = "SELECT o FROM Resume AS o WHERE o.resumeId=:resumeId"),        
        @NamedQuery( name = "Resume.findByUserId", query = "SELECT o FROM Resume AS o WHERE o.userId=:userId")        
} )
public class Resume implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "resumeid" )
    private int resumeId;

    @Column( name = "userid" )
    private long userId;
    
    @Column( name = "orgid" )
    private int orgId;
        
    /*
     0 = active
     99 = archived
    */
    @Column( name = "resumestatustypeid" )
    private int resumeStatusTypeId;

    @Column( name = "localestr" )
    private String localeStr;
    

    /*
    {
      "summary": "",
      "experience": [
        {
          "title": "",
          "company": "",
          "period": "",
          "accomplishments": [ "", "", "" ]
        }
      ],
      "education": [
        {
          "degree": "",
          "institution": "",
          "year": ""
        }
      ],
    "otherquals":["", "", ""]
    }    
    */    
    @Column( name = "jsonstr" )
    private String jsonStr;    
    
    @Column( name = "uploadedtext" )
    private String uploadedText;

    @Column( name = "uploadfilename" )
    private String uploadFilename;
        
    @Column( name = "plaintext" )
    private String plainText;
    
    @Column( name = "lastaicallhistoryid" )
    private long lastAiCallHistoryId;
    
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "createdate" )
    private Date createDate;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastupdate" )
    private Date lastUpdate;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastparsedate" )
    private Date lastParseDate;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastinputdate" )
    private Date lastInputDate;

    @Transient
    String summary;
    
    @Transient
    String objective;
    
    @Transient
    List<ResumeEducation> education;
    
    @Transient
    List<ResumeExperience> experience;
        
    @Transient
    List<String> otherQuals;
        

    
    public void clearForNewParseResults()
    {
        summary=null;
        objective=null;
        education=null;
        experience=null;
        otherQuals=null;
    }    
    
    
    public boolean getHasAnyFormData()
    {
        return (summary!=null && !summary.isBlank()) || 
               (objective!=null && !objective.isBlank()) || 
               (education!=null && !education.isEmpty()) || 
               (experience!=null && !experience.isEmpty()) ||
               (otherQuals!=null && !otherQuals.isEmpty());
    }
    
    public Locale getLocale()
    {
        if( localeStr==null || localeStr.isBlank() )
            return Locale.US;
        
        return I18nUtils.getLocaleFromCompositeStr(localeStr);
    }
    
    
    public synchronized void parseJsonStr() throws Exception
    {
        clearForNewParseResults();
                
        if( jsonStr==null || jsonStr.isBlank() )
        {
            this.summary="";
            this.objective="";

            return;
        }

        try
        {
            JsonObject  jo = JsonUtils.convertJsonStringToObject(jsonStr );
            
            if( jo==null )
                return;
            
            if( jo.containsKey("summary") && !jo.isNull("summary"))
                summary = JsonUtils.getStringFmJson(jo, "summary");

            if( jo.containsKey("objective") && !jo.isNull("objective"))
                objective = JsonUtils.getStringFmJson(jo, "objective");

            if( jo.containsKey("education") && !jo.isNull("education"))
            {
                education = new ArrayList<>();
                JsonArray ja = jo.getJsonArray("education");
                for( JsonObject eo : ja.getValuesAs(JsonObject.class))
                {
                    education.add( new ResumeEducation(eo ) );                            
                }
            }
            if( jo.containsKey("experience") && !jo.isNull("experience"))
            {
                experience = new ArrayList<>();
                JsonArray ja = jo.getJsonArray("experience");
                for( JsonObject eo : ja.getValuesAs(JsonObject.class))
                {
                    experience.add( new ResumeExperience(eo ) );                            
                }
            }
            if( jo.containsKey("otherquals") && !jo.isNull("otherquals"))
            {
                otherQuals = new ArrayList<>();
                JsonArray ja = jo.getJsonArray("otherquals");
                for( String s : ja.getValuesAs(String::toString))
                {
                    if( s!=null && !s.isBlank() )
                        otherQuals.add(s);
                }
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "Resume.parseJsonStr() " + toString() );
            throw e;
        }
    }
    
    public int getResumeId() {
        return resumeId;
    }

    public void setResumeId(int resumeId) {
        this.resumeId = resumeId;
    }

    public int getResumeStatusTypeId() {
        return resumeStatusTypeId;
    }

    public void setResumeStatusTypeId(int resumeStatusTypeId) {
        this.resumeStatusTypeId = resumeStatusTypeId;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUploadedText() {
        return uploadedText;
    }

    public void setUploadedText(String uploadedText) {
        this.uploadedText = uploadedText;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public Date getLastParseDate() {
        return lastParseDate;
    }

    public void setLastParseDate(Date lastParseDate) {
        this.lastParseDate = lastParseDate;
    }

    public Date getLastInputDate() {
        return lastInputDate;
    }

    public void setLastInputDate(Date lastInputDate) {
        this.lastInputDate = lastInputDate;
    }

    public long getLastAiCallHistoryId() {
        return lastAiCallHistoryId;
    }

    public void setLastAiCallHistoryId(long lastAiCallHistoryId) {
        this.lastAiCallHistoryId = lastAiCallHistoryId;
    }

    public String getUploadFilename() {
        return uploadFilename;
    }

    public void setUploadFilename(String uploadFilename) {
        this.uploadFilename = uploadFilename;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
    }

    
}
