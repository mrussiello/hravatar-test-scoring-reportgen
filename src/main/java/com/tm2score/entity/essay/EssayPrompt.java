/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.essay;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Basic;
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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "essayprompt" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="EssayPrompt.findByPrompt", query="SELECT o FROM EssayPrompt AS o WHERE o.prompt=:prompt" ),
    @NamedQuery ( name="EssayPrompt.findByEssayPromptId", query="SELECT o FROM EssayPrompt AS o WHERE o.essayPromptId=:essayPromptId" )
})
public class EssayPrompt implements Serializable, Comparable<EssayPrompt>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="essaypromptid")
    private int essayPromptId;

    @Column(name="statustypeid")
    private int statusTypeId=0;

    @Column(name="discernuserid")
    private int discernUserId;

    @Column(name="discernorganizationid")
    private int discernOrganizationid;

    @Column(name="discerncourseid")
    private int discernCourseId;

    @Column(name="discernproblemid")
    private int discernProblemId;

    @Column(name="prompt")
    private String prompt;

    @Column(name="problemstatement")
    private String problemStatement;


    @Column(name="usernote")
    private String userNote;

    @Column(name="localestr")
    private String localeStr="en_US";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.essayPromptId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EssayPrompt other = (EssayPrompt) obj;
        if (this.essayPromptId != other.essayPromptId) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(EssayPrompt o) {

        if( prompt!=null && !prompt.isEmpty() && o.getPrompt()!=null && !o.getPrompt().isEmpty())
            return prompt.compareTo( o.getPrompt() );

        return new Integer( this.essayPromptId ).compareTo( o.getEssayPromptId() );
    }

    @Override
    public String toString() {
        return "EssayPrompt{" + "essayPromptId=" + essayPromptId + ", statusTypeId=" + statusTypeId + ", discernProblemId=" + discernProblemId + ", prompt=" + prompt + ", problemStatement=" + problemStatement + ", lastUpdate=" + lastUpdate + '}';
    }







    public int getEssayPromptId() {
        return essayPromptId;
    }

    public void setEssayPromptId(int essayPromptId) {
        this.essayPromptId = essayPromptId;
    }

    public int getStatusTypeId() {
        return statusTypeId;
    }

    public void setStatusTypeId(int statusTypeId) {
        this.statusTypeId = statusTypeId;
    }

    public int getDiscernUserId() {
        return discernUserId;
    }

    public void setDiscernUserId(int discernUserId) {
        this.discernUserId = discernUserId;
    }

    public int getDiscernOrganizationid() {
        return discernOrganizationid;
    }

    public void setDiscernOrganizationid(int discernOrganizationid) {
        this.discernOrganizationid = discernOrganizationid;
    }

    public int getDiscernCourseId() {
        return discernCourseId;
    }

    public void setDiscernCourseId(int discernCourseId) {
        this.discernCourseId = discernCourseId;
    }

    public int getDiscernProblemId() {
        return discernProblemId;
    }

    public void setDiscernProblemId(int discernProblemId) {
        this.discernProblemId = discernProblemId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getProblemStatement() {
        return problemStatement;
    }

    public void setProblemStatement(String problemStatement) {
        this.problemStatement = problemStatement;
    }

    public String getUserNote() {
        return userNote;
    }

    public void setUserNote(String userNote) {
        this.userNote = userNote;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
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




}
