package com.tm2score.entity.bot;

import com.tm2score.bot.ActionCodeType;
import java.io.Serializable;
import java.util.Date;

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






@Entity
@Table( name = "botintent" )
@NamedQueries({
    @NamedQuery ( name="BotIntent.findByBotIntentId", query="SELECT o FROM BotIntent AS o WHERE o.botIntentId=:botIntentId" ),
    @NamedQuery ( name="BotIntent.findByBotInstanceId", query="SELECT o FROM BotIntent AS o WHERE o.botInstanceId=:botInstanceId  ORDER BY o.createDate DESC" )
})
public class BotIntent implements Serializable, Comparable<BotIntent>
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="botintentid")
    private int botIntentId;

    @Column(name="botinstanceid")
    private int botInstanceId;


    @Column(name="name")
    private String name;
    
    @Column(name="version")
    private int version;

    @Column(name="points")
    private float points;

    @Column(name="competencyname")
    private String competencyName;

    @Column(name="actioncodetypeid")
    private int actionCodeTypeId;
    
    @Column(name="useasdefault")
    private int useAsDefault;

    
    /**
     * Max Uses per Chat
     * 
     * Default (0) is 1. 
     * If more than 0, this indicates how many times the person can 'hit' this intent and get points. In 99% of cases, it should be once.
     */
    @Column(name="intparam1")
    private int intParam1;

    /**
     * Show Intn Item - Seq Id of Intn Item to show. 
     * Hide Intn Item - Seq Id of Intn Item to hide. 
     * Toggle Intn Item - Seq Id of Intn Item to toggle. 
     */
    @Column(name="intparam2")
    private int intParam2;

    /*
     Next BotInstanceId (when action codeTypeId forwards to a new Bot Instance.
    */
    @Column(name="intparam3")
    private int intParam3;
    
    @Column(name="floatparam1")
    private float floatParam1;

    @Column(name="floatparam2")
    private float floatParam2;

    @Column(name="floatparam3")
    private float floatParam3;

    @Column(name="strparam1")
    private String strParam1;

    @Column(name="strparam2")
    private String strParam2;

    @Column(name="strparam3")
    private String strParam3;
    
    @Column(name="note")
    private String note;
    
        
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;


    @Transient
    private boolean tempBoolean1;

    // this is a trusted function
    public void sanitizeInput()
    {
    }


    @Override
    public int compareTo( BotIntent b )
    {
        if( getName()!=null && !getName().isEmpty() && b.getName()!=null  )
            return getName().compareTo( b.getName() );
        
        if( createDate != null && b.createDate != null )
            return b.getCreateDate().compareTo( createDate );

        return new Integer( b.getBotIntentId() ).compareTo( new Integer( botIntentId ) );
    }

    public ActionCodeType getActionCodeType()
    {
        return ActionCodeType.getValue(actionCodeTypeId );
    }
    
    @Override
    public String toString() {
        return "BotIntent{" + "botIntentId=" + botIntentId + ", name=" + name + "}";
    }
    
    public boolean getIsCompetencyMatch( String c )
    {
        if( c==null || c.trim().isEmpty() || this.competencyName==null )
            return false;
        
        return c.trim().equalsIgnoreCase( this.competencyName.trim());
    }

    public int getBotIntentId() {
        return botIntentId;
    }

    public void setBotIntentId(int botIntentId) {
        this.botIntentId = botIntentId;
    }

    public int getBotInstanceId() {
        return botInstanceId;
    }

    public void setBotInstanceId(int botInstanceId) {
        this.botInstanceId = botInstanceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public float getPoints() {
        return points;
    }

    public void setPoints(float points) {
        this.points = points;
    }

    public String getCompetencyName() {
        return competencyName;
    }

    public void setCompetencyName(String competencyName) {
        this.competencyName = competencyName;
    }

    public int getActionCodeTypeId() {
        return actionCodeTypeId;
    }

    public void setActionCodeTypeId(int actionCode) {
        this.actionCodeTypeId = actionCode;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }

    public int getIntParam3() {
        return intParam3;
    }

    public void setIntParam3(int intParam3) {
        this.intParam3 = intParam3;
    }

    public float getFloatParam1() {
        return floatParam1;
    }

    public void setFloatParam1(float floatParam1) {
        this.floatParam1 = floatParam1;
    }

    public float getFloatParam2() {
        return floatParam2;
    }

    public void setFloatParam2(float floatParam2) {
        this.floatParam2 = floatParam2;
    }

    public float getFloatParam3() {
        return floatParam3;
    }

    public void setFloatParam3(float floatParam3) {
        this.floatParam3 = floatParam3;
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public String getStrParam2() {
        return strParam2;
    }

    public void setStrParam2(String strParam2) {
        this.strParam2 = strParam2;
    }

    public String getStrParam3() {
        return strParam3;
    }

    public void setStrParam3(String strParam3) {
        this.strParam3 = strParam3;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public boolean isTempBoolean1() {
        return tempBoolean1;
    }

    public void setTempBoolean1(boolean tempBoolean1) {
        this.tempBoolean1 = tempBoolean1;
    }

    public int getUseAsDefault() {
        return useAsDefault;
    }

    public void setUseAsDefault(int useAsDefault) {
        this.useAsDefault = useAsDefault;
    }

        

}
