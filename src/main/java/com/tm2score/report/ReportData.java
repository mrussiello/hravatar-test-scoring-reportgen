/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.itextpdf.text.pdf.PdfWriter;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.event.TestKey;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.entity.user.User;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.json.JsonUtils;
import com.tm2score.service.LogService;
import com.tm2score.sim.InterviewQuestionBreadthType;
import com.tm2score.sim.SimJUtils;
import com.tm2score.util.LanguageUtils;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.STStringTokenizer;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.awt.ComponentOrientation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import jakarta.json.JsonObject;
import java.net.URI;
import java.net.URISyntaxException;


/**
 *
 * @author Mike
 */
public class ReportData
{
    public Report r;
    
    public Report equivR;
    
    public Report r2Use;

    public TestKey tk;

    public TestEvent te;

    public Profile p;

    public  User u;

    public Org o;

    public Suborg s;

    // public List<NVPair> reportRules;
    public ReportRules reportRules;
    
    /**
     * Note - only used when report must pull specific values from a SimDescriptor.simXml object. 
     * 
     * This is typically for a custom report
     */
    public SimJUtils simJUtils;
    
    
    /**
     * Note - only used when report must pull specific values from a SimDescriptor.simXml object. 
     * 
     * This is typically for a custom report, or for a language equivalent report.
     */
    public SimJUtils equivSimJUtils;

    /**
     * Map of Topic Name : Equivalent Topic Name
     */
    private Map<String,String> equivTopicNameMap;
    
    
    
    public boolean needsTestContentTrans = false;
    public boolean needsKeyCheck = false;
    public boolean forceCalcSection = false;
    
    
     //public static String redDotFilename = "dot-red.png";
     //public static String yellowDotFilename = "dot-yellow.png";
     //public static String greenDotFilename = "dot-green.png";
     //public static String redYellowDotFilename = "dot-orange.png";
     //public static String yellowGreenDotFilename = "dot-light-green.png";


     public static String redDotFilename = "redsquare2.png";
     public static String yellowDotFilename = "yellowsquare2.png";
     public static String greenDotFilename = "greensquare2.png";
     public static String redYellowDotFilename = "redyellowsquare2.png";
     public static String yellowGreenDotFilename = "yellowgreensquare2.png";


     public static String hraLogoBlackTextFilename;
     public static String hraLogoBlackTextPurpleFilename;

     public static String hraLogoWhiteTextFilename;
     public static String hraLogoWhiteTextPurpleFilename;

     public static String hraLogoBlackTextSmallFilename;
     public static String hraLogoBlackTextSmallPurpleFilename;
     
     
     public static String hraLogoWhiteTextSmallFilename;     
     public static String hraLogoWhiteTextSmallPurpleFilename;
     
     public static String hraCoverPageFilename;
     public static String hraCoverPageBlueArrowFilename;
     
     public static String rainbowBarFilename;


     
     

    public ReportData( TestKey tk, TestEvent te, Report r, User u, Org o, Profile p )
    {
        this.tk = tk;
        this.te = te;
        this.r = r;
        this.u = u;
        this.o = o;
        this.p = p;
        this.s = tk.getSuborg();

        reportRules = new ReportRules( o, s, te==null ? null : te.getProduct(), r, null ); 
        
        init();
        // this.reportRules = o.getReportFlagList(s, r, te.getProduct() );
    }

    
    public static synchronized void init()
    {
        if( hraLogoBlackTextFilename!=null && !hraLogoBlackTextFilename.isBlank() )
            return;
        
        hraLogoBlackTextFilename = RuntimeConstants.getStringValue("hraLogoBlackTextFilename");
        hraLogoBlackTextPurpleFilename = RuntimeConstants.getStringValue("hraLogoBlackTextPurpleFilename");

        hraLogoWhiteTextFilename = RuntimeConstants.getStringValue("hraLogoWhiteTextFilename");
        hraLogoWhiteTextPurpleFilename = RuntimeConstants.getStringValue("hraLogoWhiteTextPurpleFilename");

        hraLogoBlackTextSmallFilename = RuntimeConstants.getStringValue("hraLogoBlackTextSmallFilename");
        hraLogoBlackTextSmallPurpleFilename = RuntimeConstants.getStringValue("hraLogoBlackTextSmallPurpleFilename");


        hraLogoWhiteTextSmallFilename = RuntimeConstants.getStringValue("hraLogoWhiteTextSmallFilename");    
        hraLogoWhiteTextSmallPurpleFilename = RuntimeConstants.getStringValue("hraLogoWhiteTextSmallPurpleFilename");
        
        hraCoverPageFilename = RuntimeConstants.getStringValue("hraCoverPageFilename");
        hraCoverPageBlueArrowFilename = RuntimeConstants.getStringValue("hraCoverIncludedArrowFilename");

        rainbowBarFilename = RuntimeConstants.getStringValue("rainbowBarFilename");        
    }
    
    @Override
    public String toString()
    {
        
        String out = "ReportData: ";
        
        if( te!=null )
            out += te.toString();
        
        if( r!=null )
            out += r.toString();
        
        return out;
    }
    
    public void setR2Use()
    {
        if( r2Use!=null )
            return;
        
        r2Use = r;
        
        if( equivR !=null )
        {
            r2Use = equivR;
            
            reportRules = new ReportRules( o, s, te==null ? null : te.getProduct(), r2Use, null ); 
        }        
    }
    
    public Report getR2Use()
    {
        setR2Use();
        
        return r2Use;
    }
    
    //public boolean useEngEquivValue()
    //{
    //    return this.equivSimJUtils!=null;        
    //}
    
    
    /*
    public String getReportRule( String name )
    {
        //if(1==1)
        //    return null;
        setR2Use();

        if( name == null || name.isEmpty() || reportRules == null || reportRules.isEmpty() )
            return null;

        for( NVPair p : reportRules )
        {
            if( p.getName().equals( name ) )
                return (String) p.getValue();
        }

        return null;
    }
    */

    /*
    public int getReportRuleAsInt( String name )
    {
        //if(1==1)
        //    return null;
        String sv = getReportRule( name );

        if( sv == null || sv.trim().isEmpty() )
            return 0;

        try
        {
            int v = Integer.parseInt( sv );
            return v;
        }

        catch( NumberFormatException e )
        {
            LogService.logIt( e, "ReportData.getReportRuleAsInt() " + name + ", value=" + sv );
        }

        return 0;
    }
    */

    public String getReportRuleAsString( String name )
    {
       if( reportRules!=null )
           return reportRules.getReportRuleAsString(name);
        
       return ReportUtils.getReportFlagStringValue(name, this.getTestKey(), this.getTestEvent().getProduct(), this.getSuborg(), this.getOrg(), this.getR2Use() );
        
       // return getReportRuleAsInt( name ) == 1;
    }    
    
    public boolean getReportRuleAsBoolean( String name )
    {
       if( reportRules!=null )
           return reportRules.getReportRuleAsBoolean(name);
        
       return ReportUtils.getReportFlagBooleanValue(name, this.getTestKey(), this.getTestEvent().getProduct(), this.getSuborg(), this.getOrg(), this.getR2Use() );
        
       // return getReportRuleAsInt( name ) == 1;
    }    
    
    public int getReportRuleAsInt( String name )
    {
       if( reportRules!=null )
           return reportRules.getReportRuleAsInt(name);
        
       // LogService.logIt( "ReportData.getReportRuleAsInt(" + name + ") tk: " + (tk==null) + ", sub: " + (this.getSuborg()==null) + ", org: " + (this.getOrg()==null) + ", r2u: " + this.getR2Use()  );
       return ReportUtils.getReportFlagIntValue(name, this.getTestKey(), this.getTestEvent().getProduct(), this.getSuborg(), this.getOrg(), this.getR2Use() );
        
       // return getReportRuleAsInt( name ) == 1;
    }    
    
    public float getReportRuleAsFloat( String name )
    {
       if( reportRules!=null )
           return reportRules.getReportRuleAsFloat(name);
        
       // LogService.logIt( "ReportData.getReportRuleAsInt(" + name + ") tk: " + (tk==null) + ", sub: " + (this.getSuborg()==null) + ", org: " + (this.getOrg()==null) + ", r2u: " + this.getR2Use()  );
       return ReportUtils.getReportFlagFloatValue(name, this.getTestKey(), this.getTestEvent().getProduct(), this.getSuborg(), this.getOrg(), this.getR2Use() );
        
       // return getReportRuleAsInt( name ) == 1;
    }    
    
    
    
    public boolean hasUserInfo()
    {
        return  u!=null && u.getUserType().getNamedUserIdUsername();
    }

    
    public boolean getUsesNonAscii()
    {
        Locale l = getLocale();
        
        // Any right to left
        if( I18nUtils.isTextRTL( l ) )
            return true;
        
        if( I18nUtils.isTextNonAscii(l) )
            return true;
                
        // Check the product language
        if( te!=null && this.te.getProduct()!=null && te.getProduct().getLangStr()!=null && !te.getProduct().getLangStr().isEmpty() && I18nUtils.isTextRTL( I18nUtils.getLocaleFromCompositeStr(te.getProduct().getLangStr() ) ) )
            return true;

        if( te!=null && this.te.getProduct()!=null && te.getProduct().getLangStr()!=null && !te.getProduct().getLangStr().isEmpty() && I18nUtils.isTextNonAscii( I18nUtils.getLocaleFromCompositeStr(te.getProduct().getLangStr() ) ) )
            return true;
        
        return false;
    }

    
   
    
    public Locale getLocale()
    {
        setR2Use();
                
        if( r2Use!=null && getR2Use().getLocaleForReportGen()!=null )
            return getR2Use().getLocaleForReportGen();

        if( te!=null && te.getLocaleStrReport() != null && !te.getLocaleStrReport().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

        else if( r2Use != null && getR2Use().getLocaleStr()!= null && !getR2Use().getLocaleStr().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( getR2Use().getLocaleStr() );

        return Locale.US;
    }

    
    public Locale getTestContentLocale()
    {
        // IVR and VOT use Product.strParam6 to designate the locale the content - the language the person should be speaking in. 
        //if( te !=null && te.getProduct()!=null && ( te.getProduct().getProductType().getIsIvr() || te.getProduct().getProductType().getIsVot() ) && te.getProduct().getStrParam6()!=null && !te.getProduct().getStrParam6().isEmpty() )
        //    return I18nUtils.getLocaleFromCompositeStr( te.getProduct().getStrParam6() );        
        
        return ( te!=null && te.getProduct()!=null ) ? te.getProduct().getLocaleFmLangStr() : Locale.US;
    }

    public boolean getIsLTR()
    {
        return ComponentOrientation.getOrientation( getLocale() ).isLeftToRight();
    }

    public TimeZone getTimeZone()
    {
        return u==null ? TimeZone.getDefault() : u.getTimeZone();
    }

    public int getTextRunDirection()
    {
        return getIsLTR() ? PdfWriter.RUN_DIRECTION_LTR : PdfWriter.RUN_DIRECTION_RTL;
    }


    public String getSimAuthorizationDateFormatted()
    {
        return I18nUtils.getFormattedDate(getLocale() , getTimeZone(), tk.getStartDate() );
    }


    public String getSimStartDateFormatted()
    {
        return I18nUtils.getFormattedDate(getLocale() , getTimeZone(), tk.getFirstAccessDate());
    }


    public String getSimStartDateTimeFormatted()
    {
        return I18nUtils.getFormattedDateTime(getLocale() , tk.getFirstAccessDate(), getTimeZone() );
    }


    public String getSimCompleteDateFormatted()
    {
        return I18nUtils.getFormattedDate(getLocale() , getTimeZone(), tk.getLastAccessDate() );
    }

    public String getSimCompleteDateTimeFormatted()
    {
        return I18nUtils.getFormattedDateTime(getLocale() , tk.getLastAccessDate(), getTimeZone() );
    }

    public String getBaseImageUrl()
    {
        return RuntimeConstants.getStringValue( "baseurl" ) + "/resources/images/coretest";
    }


    public boolean hasCustLogo()
    {
        return (o!= null && o.getReportLogoUrl()!= null && !o.getReportLogoUrl().isBlank()) || (s!=null && s.getReportLogoUrl()!= null && !s.getReportLogoUrl().isBlank()); //  custLogoFilename != null && !custLogoFilename.isEmpty();
    }


    public URL getCustLogoUrl()
    {
       try
       {
           return hasCustLogo() ? com.tm2score.util.HttpUtils.getURLFromString( s!=null && s.getReportLogoUrl()!= null && !s.getReportLogoUrl().isBlank() ? s.getReportLogoUrl() : o.getReportLogoUrl() ) : null; // com.tm2score.util.HttpUtils.getURLFromString( baseImageUrl + custLogoFilename );
       }
       catch( Exception e )
       {
           LogService.logIt(e, "ReportData.getCustLogoUrl() " );
           return null;
       }
    }


    public URL getLocalImageUrl( String fn )
    {
        
       try
       {
           if( fn.toLowerCase().startsWith("http") )
               return (new URI(fn)).toURL();
    
           return (new URI(getBaseImageUrl() + "/" + fn)).toURL();
       }

       catch( MalformedURLException | URISyntaxException e )
       {
           LogService.logIt(e, "ReportData.getImageUrl() " );
           return null;
       }
    }


    public URL getRedDotUrl()
    {
        return getLocalImageUrl( redDotFilename );
    }

    public URL getRedYellowDotUrl()
    {
        return getLocalImageUrl( redYellowDotFilename );
    }

    public URL getYellowDotUrl()
    {
        return getLocalImageUrl( yellowDotFilename );
    }

    public URL getYellowGreenDotUrl()
    {
        return getLocalImageUrl( yellowGreenDotFilename );
    }

    public URL getGreenDotUrl()
    {
        return getLocalImageUrl( greenDotFilename );
    }

    public URL getHRALogoBlackTextUrl()
    {
        return getLocalImageUrl( hraLogoBlackTextFilename );
    }
    
    public URL getHRALogoBlackTextUrl( boolean devel )
    {
        return getLocalImageUrl( devel ? hraLogoBlackTextPurpleFilename : hraLogoBlackTextFilename );
    }
    
    

    public URL getHRALogoBlackTextSmallUrl()
    {
        return getLocalImageUrl( hraLogoBlackTextSmallFilename );
    }


    public URL getHRALogoBlackTextSmallUrl( boolean devel )
    {
        return getLocalImageUrl(  devel ? hraLogoBlackTextSmallPurpleFilename : hraLogoBlackTextSmallFilename );
    }
    
    public URL getHRALogoWhiteTextSmallUrl()
    {
        return getLocalImageUrl( hraLogoWhiteTextSmallFilename );
    }

    public URL getHRALogoWhiteTextSmallUrl( boolean devel )
    {
        return getLocalImageUrl( devel ? hraLogoWhiteTextSmallPurpleFilename : hraLogoWhiteTextSmallFilename );
    }
    
    
    public URL getHRALogoWhiteTextUrl()
    {
        return getLocalImageUrl( hraLogoWhiteTextFilename );
    }

    public URL getHRALogoWhiteTextUrl( boolean devel )
    {
        return getLocalImageUrl( devel ? hraLogoWhiteTextPurpleFilename : hraLogoWhiteTextFilename );
    }

    public URL getRainbowBarUrl()
    {
        return getLocalImageUrl( rainbowBarFilename );
    }

    public URL getHRACoverPageUrl()
    {
        return getLocalImageUrl( hraCoverPageFilename );
    }

    public URL getHRACoverPageBlueArrowUrl()
    {
        return getLocalImageUrl( hraCoverPageBlueArrowFilename );
    }


    public String getOrgName() {
        return o.getName();
    }

    public String getReportName() {

        String simName = te.getProduct() != null ? te.getProduct().getName() : null;
        
        // LogService.logIt( "ReportData.getReportName() r " + (r==null ? "is null" : " not null, title=" + r.getTitle() + ", str2=" + r.getStrParam2() ) );
        
        String ttl = "";

        setR2Use();
        
        if( r2Use!=null && getR2Use().getStrParam3()!=null && !getR2Use().getStrParam3().isEmpty() )
        {
            ttl = getR2Use().getStrParam3();
            
            //if( needsKeyCheck  )
            //{
            if( !getLocale().getLanguage().equalsIgnoreCase( r2Use.getLocaleStr()==null || r2Use.getLocaleStr().isEmpty() ? "en" : I18nUtils.getLocaleFromCompositeStr( r2Use.getLocaleStr()).getLanguage() ) )
                ttl = (new LanguageUtils()).getTextTranslation(ttl, getLocale(), getLocale(), needsKeyCheck);
                
            //}
        }
        
        else if( r2Use!=null && getR2Use().getStrParam2()!=null && !getR2Use().getStrParam2().isEmpty() )
        {
            String key = getR2Use().getStrParam2(); // "g.TestResultsAndInterviewGuide";
        
            if( needsKeyCheck )
            {
                ttl = (new LanguageUtils()).getKeyValueStrict( getTestContentLocale(), getLocale(), key, null );
            
                if( ttl == null )
                    ttl = MessageFactory.getStringMessage(getLocale(), key, null );
            }
            else
                ttl = MessageFactory.getStringMessage(getLocale(), key, null );
            
            if( ttl ==null )
            {
                ttl = ( getR2Use().getTitle() );
                if( !getLocale().getLanguage().equalsIgnoreCase( r2Use.getLocaleStr()==null || r2Use.getLocaleStr().isEmpty() ? "en" : I18nUtils.getLocaleFromCompositeStr( r2Use.getLocaleStr()).getLanguage() ) )
                    ttl = (new LanguageUtils()).getTextTranslation(ttl, getLocale(), getLocale(), needsKeyCheck);
            }
        }
        
        else if( getR2Use().getTitle()!=null && !getR2Use().getTitle().isEmpty() )
        {
            ttl = getR2Use().getTitle();
            
            if( !getLocale().getLanguage().equalsIgnoreCase( r2Use.getLocaleStr()==null || r2Use.getLocaleStr().isEmpty() ? "en" : I18nUtils.getLocaleFromCompositeStr( r2Use.getLocaleStr()).getLanguage() ) )
                ttl = (new LanguageUtils()).getTextTranslation(ttl, getLocale(), getLocale(), needsKeyCheck);
            
        }
        
        else
        {
            ttl = getR2Use().getName();

            if( !getLocale().getLanguage().equalsIgnoreCase( r2Use.getLocaleStr()==null || r2Use.getLocaleStr().isEmpty() ? "en" : I18nUtils.getLocaleFromCompositeStr( r2Use.getLocaleStr()).getLanguage() ) )
                ttl = (new LanguageUtils()).getTextTranslation(ttl, getLocale(), getLocale(), needsKeyCheck);
        }
        
        if( simName == null )
            simName = "";

        return StringUtils.replaceStr( ttl, "[SIMNAME]" , simName );
    }

    public String getSimName() {

        String testNameToUse = getThirdPartyTestName();

        if( testNameToUse != null && !testNameToUse.isEmpty() )
            return testNameToUse;
        
        return ReportUtils.getTestNameToUseInReporting( te, te.getProduct(), getLocale() );

        //if( te.getProduct().getNameEnglish()!=null && !te.getProduct().getNameEnglish().isEmpty() && getLocale().getLanguage().equalsIgnoreCase("en") )
        //    return te.getProduct().getNameEnglish();
        
        //return te.getProduct().getName();
    }

    public String getUserName() {

        if( u!=null && u.getUserType().getPseudo() )
            return MessageFactory.getStringMessage( getLocale(), "g.Pseudonymized", null );
                    
        if( u!=null && (u.getUserType().getUsername() || u.getUserType().getUserId()) )
            return u.getEmail();
                    
        if( u==null || u.getUserType().getAnonymous() || u.getFullname()==null || u.getFullname().isEmpty() )
            return MessageFactory.getStringMessage( getLocale(), "g.Anonymous", null ) + (u==null || u.getExtRef()==null || u.getExtRef().isEmpty() ? "" : " (" + u.getExtRef() + ")");

        return u.getFullname();
    }

    public User getUser() {
        return u;
    }

    public boolean includeOverallScore()
    {
        return getR2Use().getIncludeOverallScore()==1;
    }

    public boolean includeInterview()
    {
        return getR2Use().getIncludeInterview()==1 && !getReportRuleAsBoolean(  "allnointerview" );
        //String noInterview = getReportRule( "allnointerview" );

        //if( noInterview != null && noInterview.equals( "1" ) )
        //    return false;
        
        // return getR2Use().getIncludeInterview()==1;
    }

    public boolean includeNumericScores()
    {
        return getR2Use().getIncludeNumericScores()==1;
    }

    public boolean includeColorScores()
    {
        return getR2Use().getIncludeColorScores()==1;
    }

    public boolean includeCompetencyScores()
    {
        return getR2Use().getIncludeCompetencyScores()==1;
    }

    public boolean includeTaskScores()
    {
        return getR2Use().getIncludeTaskScores()==1;
    }

    public boolean includeCompetencyDescriptions()
    {
        return getR2Use().getIncludeCompetencyDescriptions()==1;
    }

    public boolean includeEducTypeDescrip()
    {
        return getR2Use().getIncludeEducTypeDescrip()==1;
    }

    public boolean includeTrainingTypeDescrip()
    {
        return getR2Use().getIncludeTrainingTypeDescrip()==1;
    }

    public boolean includeRelatedExperTypeDescrip()
    {
        return getR2Use().getIncludeRelatedExperTypeDescrip()==1;
    }

    public boolean includeTaskInfo()
    {
        return getR2Use().getIncludeTaskInfo()==1 || getR2Use().getIncludeTaskInfo()==2;
    }

    public boolean includeTaskInterestInfo()
    {
        return getR2Use().getIncludeTaskInterestInfo()==1;
    }

    public boolean includeTaskExperienceInfo()
    {
        return getR2Use().getIncludeTaskExperienceInfo()==1;
    }

    public boolean includeBiodataInfo()
    {
        return getR2Use().getIncludeBiodataInfo()==1;
    }

    public boolean includeMinQuals()
    {
        return getR2Use().getIncludeMinQualsInfo()==1;
    }

    public boolean includeApplicantData()
    {
        return getR2Use().getIncludeApplicantDataInfo()==1;
    }

    public boolean includeScoreText()
    {
        return getR2Use().getIncludeScoreText()==1;
    }

    public Org getOrg() {
        return o;
    }

    public Report getReport() {
        
        setR2Use();
        
        return r2Use;
    }

    public Suborg getSuborg() {
        return s;
    }

    
    
    public TestEvent getTestEvent() {
        return te;
    }

    public TestKey getTestKey() {
        return tk;
    }


    public boolean hasProfile()
    {
        return p != null;
    }

    public float[] getOverallProfileData()
    {
        if( !hasProfile() )
            return null;

        return p.getOverallProfileData();
    }


    public float[] getProfileEntryData( String name, String nameEnglish)
    {
        if( !hasProfile() )
            return null;

        return p.getProfileEntryData(name, nameEnglish, false);
    }


    public String getCustomParameterValue( String name )
    {
        // LogService.logIt( "ReportData.getCustomParameterValue() " + (tk==null ? "tk is null" : tk.getCustomParameters() ) );

        if( tk == null )
            return null;

        if( tk.getCustomParameters()==null || tk.getCustomParameters().isEmpty() )
            return null;

        JsonObject jo = JsonUtils.getJsonObject( tk.getCustomParameters() );

        return jo.getString( name, null );
    }    
    

    public int getIncludeEnglishReportValue()
    {
        String t = getCustomParameterValue( "includeEnglishPdfReport" );
        
        if( t==null || t.trim().isEmpty() || t.trim().equalsIgnoreCase( "0" ) )
            return 0;
        
        try
        {
            return Integer.parseInt( t.trim() );
        }
        catch( NumberFormatException e )
        {
            LogService.logIt(e, "ReportData.getIncludeEnglishReportValue() tk.customParameters=" + (this.tk==null ? "testKey is null" : tk.getCustomParameters() + ", " + tk.toString() )  );
            
            return 0;
        }
  
    }
    
    
    public String getThirdPartyTestName()
    {
        return getCustomParameterValue( "thirdPartyTestName" );
    }


    public String getThirdPartyTestEventIdentifier()
    {
        return getCustomParameterValue( "thirdPartyTestEventIdentifier" );
    }


    public String getThirdPartyTestEventIdentifierName()
    {
        return getCustomParameterValue( "thirdPartyTestEventIdentifierName" );
    }

    public String getReportCompanyName()
    {
        return getCustomParameterValue( "reportCompanyName" );
    }

    public String getReportCompanyImageUrl()
    {
        return getCustomParameterValue( "reportCompanyImageUrl" );
    }

    public String getReportCompanyAdminName()
    {
        return getCustomParameterValue( "reportCompanyAdminName" );
    }


    public String getCompetencyName( TestEventScore tes )
    {
        
        if( equivSimJUtils==null )
        {
            // Report is in english but test content is not in english, use English Name if possible.
            if( getLocale().getLanguage().equals("en") && this.getTestContentLocale()!=null && !getTestContentLocale().getLanguage().equals("en") )
            {
                if( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() )
                    return tes.getNameEnglish();
            }
            
            return tes.getName();
        }

        String s = equivSimJUtils.getCompetencyName( tes );
        
        if( s!=null && !s.isEmpty() )
            return s;
        
        if( getLocale().getLanguage().equals("en") )
        {
            if( tes.getNameEnglish()!=null && !tes.getNameEnglish().isEmpty() )
                return tes.getNameEnglish();
        }

        //String cns = ReportUtils.getCompetencyNameToUseInReporting( te, tes, te.getSimXmlObj(), te.getProduct(), getLocale() );        
        //if( cns!=null )
        //    return cns;
        
        return tes.getName();
        
    }
    
    public String getOverallScoreText( TestEventScore otes )
    {
        if( this.equivSimJUtils==null )
            return otes.getScoreText();
        
        // TestEventScore tes = getTestEvent().getOverallTestEventScore();
        
        String s = equivSimJUtils.getOverallScoreText( otes.getScoreCategoryId(), otes.getScore() );
        
        try
        {
            return s==null || s.isEmpty() ? otes.getScoreText() : UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportData.getOverallScoreText( TES ) Decoding " + s );
            
            return s;
        }
    }
    
    public String getOverallScoreText()
    {
        if( this.equivSimJUtils==null )
            return getTestEvent().getOverallTestEventScore().getScoreText();
        
        TestEventScore tes = getTestEvent().getOverallTestEventScore();
        
        String s = equivSimJUtils.getOverallScoreText( tes.getScoreCategoryId(), tes.getScore() );

        try
        {
            return s==null || s.isEmpty() ? getTestEvent().getOverallTestEventScore().getScoreText() : UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportData.getOverallScoreText() Decoding " + s );
            
            return s;
        }
    }
    
    public String getCompetencyScoreText( TestEventScore tes )
    {
        if( equivSimJUtils==null )
            return tes.getScoreText();
                
        String s = equivSimJUtils.getCompetencyScoreText( tes );
        
        try
        {
            return s==null || s.isEmpty() ? tes.getScoreText() : UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );        
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportData.getCompetencyScoreText( TES ) Decoding " + s );
            
            return s;
        }
    }

    public String getCompetencyDescription( TestEventScore tes )
    {
        if( this.equivSimJUtils==null )
            return ScoreFormatUtils.getDescripFromTextParam( tes.getTextParam1() );
                
        String s = equivSimJUtils.getCompetencyDescription( tes );
        
        try
        {
            return s==null || s.isEmpty() ? ScoreFormatUtils.getDescripFromTextParam( tes.getTextParam1() ) : UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );        
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportData.getCompetencyDescription() Decoding " + s );
            
            return s;
        }
    }
    
    
    public static String[] getSpectrumVals( TestEventScore tes )
    {
        String[] out = new String[]{"",""};
        
        String t = StringUtils.getBracketedArtifactFromString(tes.getTextParam1() , Constants.COMPETENCYSPECTRUMKEY );
        if( t==null || t.isBlank() )
            return out;
        STStringTokenizer st = new STStringTokenizer( t, Constants.DELIMITER );
        
        if( st.hasMoreTokens() )
            out[0] = st.nextToken();
        if( st.hasMoreTokens() )
            out[1] = st.nextToken();
        return out;
    }
    
    public String getReportOverviewText()
    {
        if( equivSimJUtils==null )
            return ScoreFormatUtils.getDescripFromTextParam( getTestEvent().getOverallTestEventScore().getTextParam1() );
        
        String s = equivSimJUtils.getReportOverviewText();
        
        try
        {
            return s==null || s.isEmpty() ? ScoreFormatUtils.getDescripFromTextParam( getTestEvent().getOverallTestEventScore().getTextParam1() ) : UrlEncodingUtils.decodeKeepPlus(s, "UTF8" );        
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportData.getReportOverviewText() Decoding " + s );
            
            return s;
        }
    }
    
    
    
    /**
     * data[0]=Key  (usually the word TOPIC)
     * data[1]=Topic Name
     * data[2]=Value
     * 
     * @param topicCaveatListIn 
     */
    public void swapTopicNamesForLang( List<String[]> topicCaveatListIn )
    {
        // In order to swap we need both SimJs and we need some topics. 
        if( topicCaveatListIn==null || topicCaveatListIn.isEmpty() || equivSimJUtils==null || simJUtils==null )
            return;
        
        // reate the swap map if needed. 
        if( equivTopicNameMap==null )
            createEquivTopicNameMap();
        
        ReportUtils.swapTopicNames( equivTopicNameMap, topicCaveatListIn );
    }
    
    private synchronized void createEquivTopicNameMap()
    {
        if( equivTopicNameMap!=null )
            return;
        
        equivTopicNameMap = ReportUtils.createEquivTopicNameMap( simJUtils, equivSimJUtils ); //  new HashMap<>();
        
        if( equivSimJUtils==null || simJUtils==null )
            return;        
    }
    
    
    public List<String> getCaveatList( TestEventScore tes )
    {
        if( tes.getCaveatList().isEmpty() || equivSimJUtils==null || simJUtils==null )
            return tes.getCaveatList();
        
        
        
        // Too hard. Just leaving Caveats in foreign language.
        // if( 1==1 )
        return tes.getCaveatList();
    }
    
    public List<InterviewQuestion> getInterviewQuestionList( TestEventScore tes, int max )
    {
        if( equivSimJUtils==null )
            return tes.getInterviewQuestionList(max);
        
        List<InterviewQuestion> iql = equivSimJUtils.getInterviewQuestions(tes,max);
        
        if( iql.size()>=0 )
            return InterviewQuestionBreadthType.getInterviewQuestionForScoreAndCategory( tes.getScoreCategoryType(), tes.getScoreFormatType(), tes.getScore(), iql, max );
        
        return tes.getInterviewQuestionList(max);
    }

    public boolean getNeedsTestContentTrans() {
        return needsTestContentTrans;
    }

    public void setNeedsTestContentTrans(boolean b) {
        this.needsTestContentTrans = b;
    }
    
    

    public boolean getNeedsKeyCheck() {
        return needsKeyCheck;
    }

    public void setNeedsKeyCheck(boolean needsKeyCheck) {
        this.needsKeyCheck = needsKeyCheck;
    }

}
