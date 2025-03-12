/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.onet;

import com.tm2score.onet.OnetElement;
import com.tm2score.onet.OnetJobZoneType;
import com.tm2score.util.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "occupation_data" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="Soc.findBySoc", query="SELECT o FROM Soc AS o WHERE o.hasData=1 AND o.socCode=:socCode" ),
    @NamedQuery ( name="Soc.findAll", query="SELECT o FROM Soc AS o WHERE o.hasData=1 ORDER BY o.socCode" ),
    @NamedQuery ( name="Soc.findAllWithTasksOnly", query="SELECT o FROM Soc AS o WHERE o.hasData=1 AND o.hasTasks=1 ORDER BY o.socCode" )
})
public class Soc implements Serializable, Comparable<Soc>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name="onetsoc_code")
    private String socCode;

    @Column(name="title")
    private String title;

    @Column(name="description")
    private String description;

    @Column(name="hasdata")
    private int hasData;

    @Column(name="hastasks")
    private int hasTasks;

    
    @Transient
    private OnetJobZoneType jobZoneType;
    
    
    
    @Transient
    private List<String> alternateTitlesList;

    @Transient
    private List<Soc> relatedJobs;

    @Transient
    private List<OnetElement> tasks;

    @Transient
    private List<OnetElement> workActivities;

    @Transient
    private List<OnetElement> workStyles;

    @Transient
    private List<OnetElement> workContexts;

    
    @Transient
    private List<OnetElement> skills;

    @Transient
    private List<OnetElement> abilities;
    
    @Transient
    private List<OnetElement> knowledge;
        
    
    @Transient
    private boolean greenJob = false;
    
    @Transient
    private int blsEmployment = 0;
    
    @Transient
    private int blsAverageAnnualSalary = 0;
    
    public Soc()
    {}

    public Soc( String sc )
    {
        this.socCode=sc;
    }

    
    public boolean hasAllData()
    {
        return abilities!=null && knowledge!=null && skills!=null && tasks!=null && alternateTitlesList!=null && 
                relatedJobs!=null && workActivities!=null && workStyles!=null && workContexts!=null &&
                jobZoneType!=null && blsEmployment>0;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "Soc socCode=" + socCode + ", title=" + title + ",\n jobZoneType=" + (jobZoneType==null ? "null" : jobZoneType.getName() )); 
        sb.append( ",\n alternateTitlesList=" + (alternateTitlesList==null ? "null" : alternateTitlesList.size() ) );  
        sb.append( ",\n relatedJobs=" + (relatedJobs==null ? "null" : relatedJobs.size() ) );  
        sb.append( ",\n tasks=" + (tasks==null ? "null" : tasks.size() ) );  
        sb.append( ",\n workActivities=" + (workActivities==null ? "null" : workActivities.size() ) );  
        sb.append( ",\n workStyles=" + (workStyles==null ? "null" : workStyles.size() ) );  
        sb.append( ",\n workContexts=" + (workContexts==null ? "null" : workContexts.size() ) );  
        sb.append( ",\n skills=" + (skills==null ? "null" : skills.size() ) );  
        sb.append( ",\n abilities=" + (abilities==null ? "null" : abilities.size() ) );  
        sb.append( ",\n knowledge=" + (knowledge==null ? "null" : knowledge.size() ) );  
        sb.append( "\n blsEmployment=" + blsEmployment +", blsAverageAnnualSalary=" + blsAverageAnnualSalary );
        
        return sb.toString();
    }
    
    public Map<String,String> getSocParts()
    {
        Map<String,String> m = new HashMap<>();

         m.put( "soc", "" );
         m.put( "top2", "" );
         m.put( "top3", "" );
         m.put( "top5", "" );
         m.put( "top6", "" );
         m.put( "middle4", "" );

        if( socCode == null || socCode.length()<7 )
            return m;

        m.put( "soc", socCode );

        String t = socCode.substring(0,2);

        m.put( "top2", t );

        t = socCode.substring(0,4);

        m.put( "top3" , t );

        t = socCode.substring(0,6);

        m.put( "top5" , t );

        t = socCode.substring(0,7);

        m.put( "top6" , t );

        m.put( "middle4" , socCode.substring( 3, 7 ) );

        return m;
    }

    @Override
    public int compareTo(Soc o)
    {
       return this.socCode.compareTo( o.getSocCode() );
    }
    
    public String getTitleSingular()
    {
        String t = this.title;
        
        if( t.toLowerCase().endsWith("s") )
            t = t.substring(0, t.length()-1 );
        
        return t;
    }
    
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Soc other = (Soc) obj;
        if (!Objects.equals(this.socCode, other.socCode))
        {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.socCode);
        return hash;
    }

    public String getRelatedJobsAsString( int max )
    {
        if( this.relatedJobs==null )
            return "";
        
        List<String> rjl = new ArrayList<>();

        for( Soc rj : relatedJobs )
        {
            rjl.add( rj.getTitle() );
        }
        
        return getJobListAsString( max, rjl );
    }

    
    
    public String getAlternateTitlesAsString( int max )
    {
        return getJobListAsString( max, alternateTitlesList );
    }

    public String getJobListAsString( int max, List<String> sl )
    {
        if( sl==null || sl.isEmpty() )
            return "";
        
        StringBuilder sb = new StringBuilder();
        
        if( sl.size()> max )
        {
            Collections.shuffle( sl );
            
            sl = sl.subList(0,max );
        }
        
        for( String t : sl )
        {
            if( sb.length()>0 )
                sb.append( ", " );
            
            sb.append( t );
        }
        
        return sb.toString();
    }

    
    public String getTitleTruncated()
    {
        return StringUtils.truncateStringWithTrailer(title, 20, true);
    }

    public String getTitleTruncatedWithSoc()
    {
        return "(" + socCode + ") " + StringUtils.truncateStringWithTrailer(title, 30, true);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSocCode() {
        return socCode;
    }

    public void setSocCode(String onetSoc) {
        this.socCode = onetSoc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public int getHasData() {
        return hasData;
    }

    public void setHasData(int hasData) {
        this.hasData = hasData;
    }

    public int getHasTasks() {
        return hasTasks;
    }

    public void setHasTasks(int hasTasks) {
        this.hasTasks = hasTasks;
    }


    public List<String> getAlternateTitlesList() {
        return alternateTitlesList;
    }

    public void setAlternateTitlesList(List<String> alternateTitlesList) {
        this.alternateTitlesList = alternateTitlesList;
    }

    public List<Soc> getRelatedJobs() {
        return relatedJobs;
    }

    public void setRelatedJobs(List<Soc> relatedJobs) {
        this.relatedJobs = relatedJobs;
    }

    public List<OnetElement> getTasks() {
        return tasks;
    }

    public void setTasks(List<OnetElement> tasks) {
        this.tasks = tasks;
    }

    public List<OnetElement> getWorkActivities() {
        return workActivities;
    }

    public void setWorkActivities(List<OnetElement> workActivities) {
        this.workActivities = workActivities;
    }

    public List<OnetElement> getSkills() {
        return skills;
    }

    public void setSkills(List<OnetElement> skills) {
        this.skills = skills;
    }

    public List<OnetElement> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<OnetElement> abilities) {
        this.abilities = abilities;
    }

    public List<OnetElement> getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(List<OnetElement> knowledge) {
        this.knowledge = knowledge;
    }

    
    
    public boolean isGreenJob() {
        return greenJob;
    }

    public void setGreenJob(boolean greenJob) {
        this.greenJob = greenJob;
    }


    public List<OnetElement> getWorkStyles() {
        return workStyles;
    }

    public void setWorkStyles(List<OnetElement> workStyles) {
        this.workStyles = workStyles;
    }

    public List<OnetElement> getWorkContexts() {
        return workContexts;
    }

    public void setWorkContexts(List<OnetElement> workContexts) {
        this.workContexts = workContexts;
    }


    public OnetJobZoneType getJobZoneType() {
        return jobZoneType;
    }

    public void setJobZoneType(OnetJobZoneType jobZoneType) {
        this.jobZoneType = jobZoneType;
    }

    public int getBlsEmployment() {
        return blsEmployment;
    }

    public void setBlsEmployment(int blsEmployment) {
        this.blsEmployment = blsEmployment;
    }

    public int getBlsAverageAnnualSalary() {
        return blsAverageAnnualSalary;
    }

    public void setBlsAverageAnnualSalary(int blsAverageAnnualSalary) {
        this.blsAverageAnnualSalary = blsAverageAnnualSalary;
    }



}
