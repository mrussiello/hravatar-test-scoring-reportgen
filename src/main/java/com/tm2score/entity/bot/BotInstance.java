package com.tm2score.entity.bot;

import com.tm2score.global.I18nUtils;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
@Table( name = "botinstance" )
@NamedQueries({
    @NamedQuery ( name="BotInstance.findByBotInstanceId", query="SELECT o FROM BotInstance AS o WHERE o.botInstanceId=:botInstanceId" )
})
public class BotInstance implements Serializable, Comparable<BotInstance>
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="botinstanceid")
    private int botInstanceId;


    @Column(name="name")
    private String name;

    @Column(name="alias")
    private String alias;

    @Column(name="purpose")
    private String purpose;

    @Column(name="localestr")
    private String localeStr = "en_US";
    
    @Column(name="version")
    private int version;
    
    /**
     * Max Input Messages allowed. 0=Default=30
     */
    @Column(name="intparam1")
    private int intParam1;

    /**
     * 
     */
    @Column(name="intparam2")
    private int intParam2;

    /**
     * Initial message delay, millisecs
     */
    @Column(name="intparam3")
    private int intParam3;
    
    /**
     * Are you still there message delay, millisecs
     */
    @Column(name="intparam4")
    private int intParam4;
    
    /**
     * Bot Response delay, millisecs
     */
    @Column(name="intparam5")
    private int intParam5;
    
    /**
     * session exit delay, millisecs
     */
    @Column(name="intparam6")
    private int intParam6;
    
    /**
     * Action Code on session exit. 0=nothing, 10=record response text, no submit, 20=record response text, with submit.
     */
    @Column(name="intparam7")
    private int intParam7;
    
    /**
     * Max Points for Empathy
     */
    @Column(name="intparam8")
    private int intParam8;
    
    @Column(name="intparam9")
    private int intParam9;
    
    @Column(name="intparam10")
    private int intParam10;
    
    
    /**
     * Max Points. Forces exit when this number of points is accumulated.
     */
    @Column(name="floatparam1")
    private float floatParam1;

    /**
     * Spelling mean errors per 100 words.
     */
    @Column(name="floatparam2")
    private float floatParam2;

    /**
     * Spelling errors per 100 words, standard deviation.
    */
    @Column(name="floatparam3")
    private float floatParam3;

    /**
     * Response Time Weight
    */
    @Column(name="floatparam4")
    private float floatParam4;
    
    /**
     * Response Time Mean
    */
    @Column(name="floatparam5")
    private float floatParam5;
    
    /**
     * Response Time Standard Deviation
    */
    @Column(name="floatparam6")
    private float floatParam6;
    
    /**
     * Rapport/Empathy points mean
     */
    @Column(name="floatparam7")
    private float floatParam7;
    
    /**
     * Rapport/Empathy points sd
     */
    @Column(name="floatparam8")
    private float floatParam8;
    
    /**
     * Rapport/Empathy points weight
     */
    @Column(name="floatparam9")
    private float floatParam9;
    
    /**
     * Scaled Overall Score Mean (default 65)
     */
    @Column(name="floatparam10")
    private float floatParam10;
    
    /**
     * Scaled Overall Score Stdev (default 15)
     */
    @Column(name="floatparam11")
    private float floatParam11;
    
    /**
     */
    @Column(name="floatparam12")
    private float floatParam12;
    
    /**
     * Spelling weight
     */
    @Column(name="floatparam13")
    private float floatParam13;
    
    @Column(name="floatparam14")
    private float floatParam14;

    @Column(name="floatparam15")
    private float floatParam15;

    @Column(name="floatparam16")
    private float floatParam16;

    @Column(name="floatparam17")
    private float floatParam17;

    @Column(name="floatparam18")
    private float floatParam18;

    @Column(name="floatparam19")
    private float floatParam19;
    
    @Column(name="floatparam20")
    private float floatParam20;
    
    
    /**
     * Opening statements (presumably from a customer). Delimited by |
     */
    @Column(name="strparam1")
    private String strParam1;

    /*
     Competency config string - sets the max points and weight for each competency allowed to be used in calculating the total points. 
    
     Competency|max points|weight|mean|sd|Competency Name|Max Points|weight|mean|sd|Competency Name|Max Points|weight|mean|sd ... 
    */
    @Column(name="strparam2")
    private String strParam2;

    /**
     * Rapport/Empathy Competency Name.  Empathy is scored as 100 * the number of empathy points assigned, divided by the empathy competency max points in the competency config.
     */
    @Column(name="strparam3")
    private String strParam3;

    /**
     * Spelling Competency Name. Spelling score is assigned as error rate (errors / total words), converted to a Z-Score using Spelling mean and STD, then transformed to 0-100 with mean 65 and standard deviation 15.
     */
    @Column(name="strparam4")
    private String strParam4;

    
    /**
     * Rapport Competency Name.  Rapport is scored as 100 * the number of rapport points assigned, divided by the rapport competency max points in the competency config.
     */
    @Column(name="strparam5")
    private String strParam5;

    
    @Column(name="note")
    private String note;
    
        
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    
    @Transient
    private List<BotIntent> botIntentList;

    
    // this is a trusted function
    public void sanitizeInput()
    {
    }


    @Override
    public int compareTo( BotInstance b )
    {
        if( getNameAlias()!=null && !getNameAlias().isEmpty() && b.getNameAlias()!=null  )
            return getNameAlias().compareTo( b.getNameAlias() );
        
        if( createDate != null && b.createDate != null )
            return b.getCreateDate().compareTo( createDate );

        return new Integer( b.getBotInstanceId() ).compareTo( new Integer( botInstanceId ) );
    }

    @Override
    public String toString() {
        return "BotInstance{" + "botInstanceId=" + botInstanceId + ", name=" + name + ", alias=" + alias + '}';
    }

    public BotIntent getBotIntentForName( String name )
    {
        if( name==null || name.trim().isEmpty() || botIntentList==null )
            return null;
        
        name = name.trim();
        
        for( BotIntent bi : this.botIntentList )
        {
            if( bi.getName().equals( name ) )
                return bi;
        }
        
        return null;
    }
    
    public Locale getLocale()
    {
        if( localeStr==null || localeStr.isEmpty() )
            localeStr = "en_US";
        
        return I18nUtils.getLocaleFromCompositeStr( localeStr);
    }
    
    public List<String> getInitialStmtList()
    {
        List<String> out = new ArrayList<>();
        
        if( strParam1==null || strParam1.isEmpty() )
            return out;
        
        for( String s : strParam1.split("\\|") )
        {
            s = s.trim();
            
            if( !s.isEmpty() )
                out.add( s );
        }
        
        return out;
    }
    
    /*
     Competency config string - sets the max points and weight for each competency allowed to be used in calculating the total points.     
     Competency|max points|weight|mean|sd|Competency Name|Max Points|weight|mean|sd|Competency Name|Max Points|weight|mean|sd ... 
    
     returns Map <Competency Name, float[] where 
         float[0] = max points
         float[1] = weight
         float[2] = mean
         float[3] = standard deviation
    */
    public Map<String,float[]> getCompetencyConfigMap() throws Exception
    {
        
        Map<String,float[]> out = new HashMap<>();
        
        if( strParam2==null || strParam2.trim().isEmpty() )
            return out;
        
        try
        {
            strParam2=strParam2.trim();

            String[] vals = strParam2.split("\\|");
            String nm;
            String temp;
            float[] numvals;

            for( int i=0; i<vals.length-4;i+=5 )
            {
                nm=vals[i].trim();

                if( nm.isEmpty() )
                    throw new Exception( "Name is invalid (empty) i=" + i );

                numvals = new float[4];

                for( int j=0;j<4; j++ )
                {
                    temp=vals[i + j+1].trim();            
                    numvals[j] = temp.isEmpty() ? 0 : Float.parseFloat(temp);                
                }

                out.put( nm, numvals );
            }
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BotInstance.getCompetencyConfigMap() strParam2=" + strParam2 );
            throw e;
        }

        return out;
    }
    
    
    public String getNameAlias()
    {
        return (name==null ? "" : name ) + "_" + (alias==null ? "" : alias );
    }
    
    public String getBotInstanceIdEncrypted() {
        try
        {
            return EncryptUtils.urlSafeEncrypt( Integer.toString( this.botInstanceId ));
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BotInstance.getBotInstanceIdEncrypted() " + botInstanceId );
            return Integer.toString(botInstanceId);
        }
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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public List<BotIntent> getBotIntentList() {
        return botIntentList;
    }

    public void setBotIntentList(List<BotIntent> botIntentList) {
        this.botIntentList = botIntentList;
    }

    public int getIntParam4() {
        return intParam4;
    }

    public void setIntParam4(int intParam4) {
        this.intParam4 = intParam4;
    }

    public int getIntParam5() {
        return intParam5;
    }

    public void setIntParam5(int intParam5) {
        this.intParam5 = intParam5;
    }

    public int getIntParam6() {
        return intParam6;
    }

    public void setIntParam6(int intParam6) {
        this.intParam6 = intParam6;
    }

    public int getIntParam7() {
        return intParam7;
    }

    public void setIntParam7(int intParam7) {
        this.intParam7 = intParam7;
    }

    public int getIntParam8() {
        return intParam8;
    }

    public void setIntParam8(int intParam8) {
        this.intParam8 = intParam8;
    }

    public int getIntParam9() {
        return intParam9;
    }

    public void setIntParam9(int intParam9) {
        this.intParam9 = intParam9;
    }

    public int getIntParam10() {
        return intParam10;
    }

    public void setIntParam10(int intParam10) {
        this.intParam10 = intParam10;
    }

    public float getFloatParam4() {
        return floatParam4;
    }

    public void setFloatParam4(float floatParam4) {
        this.floatParam4 = floatParam4;
    }

    public float getFloatParam5() {
        return floatParam5;
    }

    public void setFloatParam5(float floatParam5) {
        this.floatParam5 = floatParam5;
    }

    public float getFloatParam6() {
        return floatParam6;
    }

    public void setFloatParam6(float floatParam6) {
        this.floatParam6 = floatParam6;
    }

    public String getStrParam4() {
        return strParam4;
    }

    public void setStrParam4(String strParam4) {
        this.strParam4 = strParam4;
    }

    public String getStrParam5() {
        return strParam5;
    }

    public void setStrParam5(String strParam5) {
        this.strParam5 = strParam5;
    }

    public float getFloatParam7() {
        return floatParam7;
    }

    public void setFloatParam7(float floatParam7) {
        this.floatParam7 = floatParam7;
    }

    public float getFloatParam8() {
        return floatParam8;
    }

    public void setFloatParam8(float floatParam8) {
        this.floatParam8 = floatParam8;
    }

    public float getFloatParam9() {
        return floatParam9;
    }

    public void setFloatParam9(float floatParam9) {
        this.floatParam9 = floatParam9;
    }

    public float getFloatParam10() {
        return floatParam10;
    }

    public void setFloatParam10(float floatParam10) {
        this.floatParam10 = floatParam10;
    }

    public float getFloatParam11() {
        return floatParam11;
    }

    public void setFloatParam11(float floatParam11) {
        this.floatParam11 = floatParam11;
    }

    public float getFloatParam12() {
        return floatParam12;
    }

    public void setFloatParam12(float floatParam12) {
        this.floatParam12 = floatParam12;
    }

    public float getFloatParam13() {
        return floatParam13;
    }

    public void setFloatParam13(float floatParam13) {
        this.floatParam13 = floatParam13;
    }

    public float getFloatParam14() {
        return floatParam14;
    }

    public void setFloatParam14(float floatParam14) {
        this.floatParam14 = floatParam14;
    }

    public float getFloatParam15() {
        return floatParam15;
    }

    public void setFloatParam15(float floatParam15) {
        this.floatParam15 = floatParam15;
    }

    public float getFloatParam16() {
        return floatParam16;
    }

    public void setFloatParam16(float floatParam16) {
        this.floatParam16 = floatParam16;
    }

    public float getFloatParam17() {
        return floatParam17;
    }

    public void setFloatParam17(float floatParam17) {
        this.floatParam17 = floatParam17;
    }

    public float getFloatParam18() {
        return floatParam18;
    }

    public void setFloatParam18(float floatParam18) {
        this.floatParam18 = floatParam18;
    }

    public float getFloatParam19() {
        return floatParam19;
    }

    public void setFloatParam19(float floatParam19) {
        this.floatParam19 = floatParam19;
    }

    public float getFloatParam20() {
        return floatParam20;
    }

    public void setFloatParam20(float floatParam20) {
        this.floatParam20 = floatParam20;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }


}
