package com.tm2score.entity.purchase;

import com.tm2score.global.I18nUtils;
import com.tm2score.purchase.ConsumerProductType;
import com.tm2score.purchase.ProductType;
import com.tm2score.util.NVPair;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import java.util.Locale;
import java.util.StringTokenizer;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Date;


@Cacheable
@Entity
@Table( name = "product" )
public class Product implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "productid" )
    private int productId;

    @Column( name = "producttypeid" )
    private int productTypeId;

    @Column( name = "productstatustypeid" )
    private int productStatusTypeId;

    @Column( name = "creditusagetypeid" )
    private int creditUsageTypeId;

    @Column( name = "consumerproducttypeid" )
    private int consumerProductTypeId;
 
    @Column( name = "name" )
    private String name;

    @Column( name = "nameenglish" )
    private String nameEnglish;


    @Column(name="detailview")
    private String detailView;


    /**
     * Sim - Name of Sim
     * Findly - name of Findly Test
     */
    @Column( name = "strparam1" )
    private String strParam1;


    /**
     *
     * CT3 Sim - Non-Standard Risk Factor String. This is a string with the following format:
     *     CT3RiskFactorTypeId;int1;int2;int3;float1;float2;float3;str1;str2|CT3RiskFactorTypeId;int1;int2;int3;float1;float2;float3;str1;str2|
     *  ...
     *
     */
    @Column( name = "strparam2" )
    private String strParam2;


    /**
     * Findly - Findly Test Code (Test ID)
     */
    @Column( name = "strparam3" )
    private String strParam3;


    /**
     *
     * Findly Category
     */
    @Column( name = "strparam4" )
    private String strParam4;

    /**
     * Findly - Test Description
     */
    @Column( name = "strparam5" )
    private String strParam5;

    /**
     * Findly - LangStr
     */
    @Column( name = "strparam6" )
    private String strParam6;

    /**
     * Findly Online Category
     */
    @Column( name = "strparam7" )
    private String strParam7;

    /**
     * Findly Test Type
     */
    @Column( name = "strparam8" )
    private String strParam8;

    /**
     * Sim or Battery - Email to. This replaces the email to in the test key.
     * Findly Test ReportType
     */
    @Column( name = "strparam9" )
    private String strParam9;

    /**
     * Sim or Battery - Text to. This replaces the Text to in the test key.
     * Findly WS Test Type
     */
    @Column( name = "strparam10" )
    private String strParam10;

    @Column(name="strparam11")
    private String strParam11;

    
    /**
     * Any Sim - Keywords to add to Mashup
     */
    @Column(name="strparam12")
    private String strParam12;


    /**
     * Any Sim or Ct5Direct - Sample Report URL
     */
    @Column(name="strparam13")
    private String strParam13;
    
    

    /*
     * Credit Purchase - number of credits in this package.
     * Battery Type - indicates batteryId
     * Credit: # of Credits,
     * Resource: 0=all, 1=assessment, 2=preview, 3=training, 4=technology, 5=production
     */
    @Column( name = "intparam1" )
    private int intParam1;

    /*
     *
     * Requires larger screen.
     *
     */
    @Column( name = "intparam2" )
    private int intParam2;



    /**
     * FINDLY - Tokens used
     * Sim - Calculate Competency Percentiles.
     */
    @Column( name = "intparam3" )
    private int intParam3;

    /**
     * Findly Admin Time - minutes
     */
    @Column( name = "intparam4" )
    private int intParam4;

    /**
     * Findly Item Count
     */
    @Column( name = "intparam5" )
    private int intParam5;


    /**
     * Findly - 1=Mobile Ready, 2=Not Mobile Ready
     * Sim or Ct5Direct or iFrame or Battery - 
     *      0=no file upload, 
     *      1=Includes general file upload, 
     *      2=recorded video file upload. 
     *      3=recorded audio-only upload, 
     *      4=captured image-only media file upload for proctoring, 
     *      5=captured image only not for proctoring  
     *      6=captured images for proctoring, and audio. 
     *        Typically used as a flag for a NOTE in the email message sent to user.
     */
    @Column( name = "intparam6" )
    private int intParam6;


    /**
     * Findly - FindlyTestTypeId
     * Sim - Calculate Competency Percentiles.
     */
    @Column( name = "intparam7" )
    private int intParam7;

    /**
     * Findly - Skills CategoryTypeId
     * Sim - Skills CategoryTypeId
     */
    @Column( name = "intparam8" )
    private int intParam8;

    
    /**
     * Sim - CT3StandardModuleTypeId for Cog Sim if included in sim.
     * WHOLE - CT3StandardModuleTypeId for Cog Sim if included in sim.
     */
    @Column( name = "intparam9" )
    private int intParam9;

    /**
     * Sim - NOT eligible for unlimited accounts.
     */
    @Column( name = "intparam10" )
    private int intParam10;

    
    /*
     * Sim - 0=default
             1=This is a short version of another sim, with no video 
             2=This is a short version of another sim, with video 
    */    
    @Column( name = "intparam11" )
    private int intParam11;

    
    /*
     *  Sim - Legacy Product Id. When an Org is set to show legacy products, if there is a 
              legacy product id it will show the detail for this product rather than the actual 
              product found in the catalog.
    */
    @Column( name = "intparam12" )
    private int intParam12;

    /*
     Sim - 0=Voice Vibes is not required for scoring.
           1=Voice vibes IS required for scoring.
    */
    @Column( name = "intparam13" )
    private int intParam13;

    @Column( name = "intparam14" )
    private int intParam14;

    @Column( name = "intparam15" )
    private int intParam15;
    
    @Column( name = "intparam24" )
    private int intParam24;
    
    
    
   @Column( name = "floatparam1" )
    private float floatParam1;

    @Column( name = "floatparam2" )
    private float floatParam2;

    @Column( name = "imageuri" )
    private String imageUri;

    @Column( name = "simdescriptorid" )
    private long simDescriptorId;

    @Column( name = "longparam1" )
    private long longParam1;

    @Column( name = "longparam2" )
    private long longParam2;

    @Column( name = "longparam3" )
    private long longParam3;

    /**
     * English Equivalent SimId
     */
    @Column( name = "longparam4" )
    private long longParam4;

    @Column( name = "longparam5" )
    private long longParam5;    
    
    @Column(name="onetsoc")
    private String onetSoc;

    @Column(name="onetversion")
    private String onetVersion;

    @Column( name = "lang" )
    private String langStr = "en_US";

    /**
     * Note this is used for the Ct3 Risk Factors Str for CT5 Direct Tests.
     */
    @Column(name="previewhead")
    private String previewHead;

    
    /**
     * Note this is used as the cognitive Ct3StandardModuleType for Ct5Direct Sims created by the Ct3SimBuilder automated build system. 
     */
    @Column(name="previewwidth")
    private int previewWidth;
    
    

    @Transient
    private Date tempDate;

    @Transient
    private Locale tempLocale;


    public ProductType getProductType()
    {
        return ProductType.getValue(productTypeId);
    }

    public boolean getNeedsNameEnglish()
    {
        if( nameEnglish == null || nameEnglish.isEmpty() )
            return false;

        if( name ==null || name.isEmpty() )
            return false;

        if( name.equalsIgnoreCase( nameEnglish ) )
            return false;

        return true;
    }

    public String getNameWithEnglishIfNeeded()
    {
        if( getNeedsNameEnglish() )
                return name + " (English: " + nameEnglish + ")";

        return name;
    }


    public ConsumerProductType getConsumerProductType()
    {
        return ConsumerProductType.getValue( this.consumerProductTypeId );
    }



    public String toString() {
        return "Product{" + "productId=" + productId + ", name=" + getNameWithEnglishIfNeeded() + ", type=" + productTypeId + ", simDescriptorId=" + simDescriptorId + "}";
    }

    public boolean getHasIcon()
    {
        return imageUri != null && !imageUri.isEmpty();
    }

    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + this.productId;
        return hash;
    }


    @Override
    public boolean equals( Object o )
    {
        if( o instanceof Product )
        {
            Product b = (Product) o;

            if( b.getProductId() == productId )
                return true;
        }

        return false;
    }


    public List<NVPair> getReportFlagList()
    {
        List<NVPair> out = new ArrayList<>();

        if( strParam11==null || strParam11.isEmpty() )
            return out;

        StringTokenizer st = new StringTokenizer( strParam11, "|" );

        String rule;
        String value;

        while( st.hasMoreTokens() )
        {
            rule = st.nextToken();

            if( !st.hasMoreTokens() )
                break;

            value = st.nextToken();

            if( rule != null && !rule.isEmpty() && value!=null && !value.isEmpty() )
                out.add( new NVPair( rule,value ) );
        }

        return out;
    }
    
    

    public Locale getLocaleFmLangStr()
    {
        if( langStr!=null && !langStr.isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr(langStr);
        
        return Locale.US;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public int getProductId()
    {
        return productId;
    }

    public void setProductId( int productId )
    {
        this.productId = productId;
    }

    public int getProductStatusTypeId()
    {
        return productStatusTypeId;
    }

    public void setProductStatusTypeId( int productStatusTypeId )
    {
        this.productStatusTypeId = productStatusTypeId;
    }


    public int getProductTypeId()
    {
        return productTypeId;
    }

    public void setProductTypeId( int productTypeId )
    {
        this.productTypeId = productTypeId;
    }

    public String getStrParam1()
    {
        return strParam1;
    }

    public void setStrParam1( String strParam1 )
    {
        this.strParam1 = strParam1;
    }

    public String getStrParam2()
    {
        return strParam2;
    }

    public void setStrParam2( String strParam2 )
    {
        this.strParam2 = strParam2;
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

    public long getLongParam1() {
        return longParam1;
    }

    public void setLongParam1(long longParam1) {
        this.longParam1 = longParam1;
    }

    public long getLongParam2() {
        return longParam2;
    }

    public void setLongParam2(long longParam2) {
        this.longParam2 = longParam2;
    }

    public long getLongParam3() {
        return longParam3;
    }

    public void setLongParam3(long longParam3) {
        this.longParam3 = longParam3;
    }

    public long getSimDescriptorId() {
        return simDescriptorId;
    }

    public void setSimDescriptorId(long simDescriptorId) {
        this.simDescriptorId = simDescriptorId;
    }

    public String getStrParam3() {
        return strParam3;
    }

    public void setStrParam3(String strParam3) {
        this.strParam3 = strParam3;
    }

    public int getCreditUsageTypeId() {
        return creditUsageTypeId;
    }

    public void setCreditUsageTypeId(int creditUsageTypeId) {
        this.creditUsageTypeId = creditUsageTypeId;
    }

    public Locale getTempLocale() {
        return tempLocale;
    }

    public void setTempLocale(Locale locale) {
        this.tempLocale = locale;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
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

    public String getOnetSoc() {
        return onetSoc;
    }

    public void setOnetSoc(String onetSoc) {
        this.onetSoc = onetSoc;
    }

    public String getOnetVersion() {
        return onetVersion;
    }

    public void setOnetVersion(String onetVersion) {
        this.onetVersion = onetVersion;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
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

    public String getStrParam6() {
        return strParam6;
    }

    public void setStrParam6(String strParam6) {
        this.strParam6 = strParam6;
    }

    public String getStrParam7() {
        return strParam7;
    }

    public void setStrParam7(String strParam7) {
        this.strParam7 = strParam7;
    }

    public String getStrParam8() {
        return strParam8;
    }

    public void setStrParam8(String strParam8) {
        this.strParam8 = strParam8;
    }

    public String getStrParam9() {
        return strParam9;
    }

    public void setStrParam9(String strParam9) {
        this.strParam9 = strParam9;
    }

    public String getStrParam10() {
        return strParam10;
    }

    public void setStrParam10(String strParam10) {
        this.strParam10 = strParam10;
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

    public String getDetailView() {
        return detailView;
    }

    public void setDetailView(String detailView) {
        this.detailView = detailView;
    }

    public long getLongParam4() {
        return longParam4;
    }

    public void setLongParam4(long longParam4) {
        this.longParam4 = longParam4;
    }

    public long getLongParam5() {
        return longParam5;
    }

    public void setLongParam5(long longParam5) {
        this.longParam5 = longParam5;
    }

    public int getConsumerProductTypeId() {
        return consumerProductTypeId;
    }

    public void setConsumerProductTypeId(int consumerProductTypeId) {
        this.consumerProductTypeId = consumerProductTypeId;
    }

    public String getLangStr() {
        
        if( langStr==null || langStr.isEmpty() )
            langStr = "en_US";
        
        return langStr;
    }

    public void setLangStr(String langStr) {
        this.langStr = langStr;
    }

    public String getStrParam11() {
        return strParam11;
    }

    public void setStrParam11(String strParam11) {
        this.strParam11 = strParam11;
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

    public int getIntParam11() {
        return intParam11;
    }

    public void setIntParam11(int intParam11) {
        this.intParam11 = intParam11;
    }

    public int getIntParam12() {
        return intParam12;
    }

    public void setIntParam12(int intParam12) {
        this.intParam12 = intParam12;
    }

    public int getIntParam13() {
        return intParam13;
    }

    public void setIntParam13(int intParam13) {
        this.intParam13 = intParam13;
    }

    public int getIntParam14() {
        return intParam14;
    }

    public void setIntParam14(int intParam14) {
        this.intParam14 = intParam14;
    }

    public int getIntParam15() {
        return intParam15;
    }

    public void setIntParam15(int intParam15) {
        this.intParam15 = intParam15;
    }

    public Date getTempDate() {
        return tempDate;
    }

    public void setTempDate(Date tempDate) {
        this.tempDate = tempDate;
    }

    public String getStrParam12() {
        return strParam12;
    }

    public void setStrParam12(String strParam12) {
        this.strParam12 = strParam12;
    }

    public String getStrParam13() {
        return strParam13;
    }

    public void setStrParam13(String strParam13) {
        this.strParam13 = strParam13;
    }

    public int getIntParam24() {
        return intParam24;
    }

    public void setIntParam24(int intParam24) {
        this.intParam24 = intParam24;
    }

    public String getPreviewHead() {
        return previewHead;
    }

    public void setPreviewHead(String previewHead) {
        this.previewHead = previewHead;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }



}
