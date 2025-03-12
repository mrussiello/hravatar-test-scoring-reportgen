package com.tm2score.entity.user;

import com.tm2score.global.I18nUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.RoleType;
import com.tm2score.user.UserType;
import com.tm2score.util.MessageFactory;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
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

@Cacheable
@Entity
@Table( name = "xuser" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="User.findByUserId", query="SELECT o FROM User AS o  WHERE o.userId = :userId" ),
    @NamedQuery ( name="User.findByUsername", query="SELECT o FROM User AS o  WHERE o.username = :uname" ),
    @NamedQuery ( name="User.findUserByEmailAndOrgId", query="SELECT o FROM User AS o WHERE o.email = :uemail AND o.orgId=:orgId" ),
    @NamedQuery ( name="User.findByMinRoleAndOrgId", query="SELECT o FROM User AS o WHERE o.orgId=:orgId AND o.roleId>=:roleId" )
})
public class User implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="userid")
    private long userId;

    @Column(name="usertypeid")
    private int userTypeId=0;

    @Column(name="extref")
    private String extRef;

    @Column(name="altidentifier")
    private String altIdentifier;

    @Column( name = "altidentifiername" )
    private String altIdentifierName;

    @Column(name="firstname")
    private String firstName;

    @Column(name="lastname")
    private String lastName;

    @Column(name="email")
    private String email;

    @Column(name="username")
    private String username;

    @Column(name="roleid")
    private int roleId = 0;

    @Column(name="orgid")
    private int orgId = 0;

    @Column(name="suborgid")
    private int suborgId = 0;

    @Column(name="userstatustypeid")
    private int userStatusTypeId = 0;

    @Column(name="language")
    private String localeStr = null;

    @Column(name="countrycode")
    private String countryCode = "US";

    @Column(name="nationality")
    private String nationality;
        
    @Column(name="ipcountry")
    private String ipCountry;

    @Column(name="employerurl")
    private String employerUrl;

    @Column(name="title")
    private String title;


    @Column(name="ipstate")
    private String ipState;

    @Column(name="ipcity")
    private String ipCity;

    @Column(name="ipzip")
    private String ipZip;
    
    @Column(name="iptimezone")
    private String ipTimezone;
    
    
    @Column(name="geographicregionid")
    private int geographicRegionId = 0;

    @Column(name="timezoneid")
    private  String timeZoneId;

    @Column(name="ethniccategoryid")
    private int ethnicCategoryId = 0;

    @Column(name="racialcategories")
    private String racialCategories;


    @Column(name="phoneareacode")
    private String phoneAreaCode;

    @Column(name="phoneprefix")
    private String phonePrefix;

    @Column(name="phonelast")
    private String phoneLast;

    /**
     * 0 = unknown
     * 1 = Male
     * 2 = Female
     */
    @Column(name="gendertypeid")
    private int genderTypeId = 0;

    /**
     * 0 = unknown
     * > 0  = Year of Birth
     */
    @Column(name="birthyear")
    private int birthYear = 0;

    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;

    @Column(name="usercompanystatustypeid")
    private int userCompanyStatusTypeId;

    @Column( name = "resetpwd" )
    private int resetPwd;

    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lockoutdate")
    private Date lockoutDate;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="passwordstartdate")
    private Date passwordStartDate;


    @Transient
    private String password;

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + ", username=" + username + ", roleId=" + roleId + ", orgId=" + orgId + ", suborgId=" + suborgId + ", userStatusTypeId=" + userStatusTypeId + ", localeStr=" + localeStr + ", countryCode=" + countryCode + '}';
    }






    public boolean getHasDemoInfo()
    {
        return ethnicCategoryId > 0 && racialCategories != null && !racialCategories.isEmpty();
    }

    public boolean getHasNameEmail()
    {
        return firstName != null && !firstName.isEmpty() &&
               lastName != null && !lastName.isEmpty() &&
               email != null && !email.isEmpty();
    }


    public boolean getHasName()
    {
        return (firstName != null && !firstName.isEmpty()) ||
               (lastName != null && !lastName.isEmpty());
    }


    public boolean getHasIpCountry()
    {
        return ipCountry!=null && !ipCountry.isBlank();
    }

    public String getIpCountryName()
    {
        if( !getHasIpCountry() )
            return null;
        
        Locale l = this.getLocaleToUseDefaultUS();
        
        String s = MessageFactory.getStringMessage(l, "cntry." + ipCountry.toUpperCase());
        return s!=null && !s.isBlank() ? s : ipCountry;
    }
    
    
    public boolean getHasIpState()
    {
        return ipState!=null && !ipState.isBlank();
    }

    public boolean getHasIpCity()
    {
        return ipCity!=null && !ipCity.isBlank();
    }
    
    

    // @Transient
    public TimeZone getTimeZone()
    {
        if( timeZoneId != null && !timeZoneId.isEmpty() )
            return TimeZone.getTimeZone( timeZoneId );

        return TimeZone.getDefault();
    }

    public RoleType getRoleType()
    {
        return RoleType.getValue( roleId );
    }

    public UserType getUserType()
    {
        return UserType.getValue( userTypeId );
    }

    public Locale getLocaleToUseDefaultNull()
    {
        if( localeStr == null || localeStr.isEmpty() )
            return null;

        return I18nUtils.getLocaleFromCompositeStr( localeStr );
    }


    public Locale getLocaleToUseDefaultUS()
    {
        if( localeStr == null || localeStr.isEmpty() )
            return Locale.US;

        return I18nUtils.getLocaleFromCompositeStr( localeStr );
    }

    /*
    public void setLocaleToUse( Locale l )
    {
        localeStr = l == null ? Locale.US.toString() : l.toString();

        // countryCode = l.getCountry();
    }
    */

    public void setRacialCategoryIdList(  List<Integer> idl )
    {
        String s = "";

        for( Integer id : idl )
        {
            if( !s.isEmpty() )
                s += ",";

            s += id.toString();
        }

        racialCategories = s;
    }


    public List<Integer> getRacialCategoryIdList()
    {
        List<Integer> out = new ArrayList<>();

        if( this.racialCategories == null || racialCategories.trim().isEmpty() )
            return out;

        String[] pids = racialCategories.split( "," );

        int id=0;

        for( String pid : pids )
        {
            if( pid == null )
                continue;

            pid = pid.trim();

            if( pid.isEmpty() )
                continue;

            id = 0;

            try
            {
                id = Integer.parseInt( pid );

                // if real and not a duplicate.
                if( id > 0 && !out.contains( id ) )
                    out.add( id );
            }

            catch( NumberFormatException e )
            {
                LogService.logIt( e, "User.getRacialCategoryIdList() racialCategories=" + racialCategories + ", pid=" + pid + ", id=" + id );
            }
        }

        return out;
    }



    @Override
    public boolean equals( Object o )
    {
        if( o instanceof User )
        {
            User u = (User) o;

            return userId == u.getUserId();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (int) (this.userId ^ (this.userId >>> 32));
        return hash;
    }


    public void setRacialCategoryIdStrList(  List<String> idl )
    {
        String s = "";

        for( String id : idl )
        {
            if( !s.isEmpty() )
                s += ",";

            s += id;
        }

        racialCategories = s;
    }


    public List<String> getRacialCategoryIdStrList()
    {
        List<String> out = new ArrayList<>();

        if( racialCategories == null || racialCategories.trim().isEmpty() )
            return out;

        String[] pids = racialCategories.split( "," );

        int id=0;

        for( String pid : pids )
        {
            if( pid == null )
                continue;

            pid = pid.trim();

            if( pid.isEmpty() )
                continue;

            id = 0;

            try
            {
                id = Integer.parseInt( pid );

                // if real and not a duplicate.
                if( id > 0 && !out.contains( Integer.toString( id ) ) )
                    out.add( Integer.toString( id ) );
            }

            catch( NumberFormatException e )
            {
                LogService.logIt( e, "User.getRacialCategoryIdStrList() racialCategories=" + racialCategories + ", pid=" + pid + ", id=" + id );
            }
        }

        return out;
    }



    public boolean getHasAltIdentifierInfo()
    {
        return altIdentifier!=null && !altIdentifier.isEmpty(); // && altIdentifierName!=null && !altIdentifierName.isEmpty();
    }


    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDefaultLocaleStr() {
        return localeStr + "_" + countryCode;
    }

    public void setDefaultLocaleStr(String defaultLocaleStr) {
        // this.defaultLocaleStr = defaultLocaleStr;
    }

    public String getEmailEd() {

        if( email != null && email.trim().startsWith( "{" ) )
            return null;

        return email;
    }

    public String getFirstNameEd() {

        if( this.firstName != null && firstName.trim().startsWith( "{" ) )
            return null;

        return firstName;
    }

    public String getLastNameEd() {

        if( this.lastName != null && lastName.trim().startsWith( "{" ) )
            return null;

        return lastName;
    }
    
    public String getMobilePhoneEd() {

        if( this.phonePrefix != null && phonePrefix.trim().startsWith( "{" ) )
            return null;

        return phonePrefix;
    }
    
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getGeographicRegionId() {
        return geographicRegionId;
    }

    public void setGeographicRegionId(int geographicRegionId) {
        this.geographicRegionId = geographicRegionId;
    }

    public String getIpCity() {
        return ipCity;
    }

    public void setIpCity(String ipCity) {
        this.ipCity = ipCity;
    }

    public String getIpState() {
        return ipState;
    }

    public void setIpState(String ipState) {
        this.ipState = ipState;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getUserStatusTypeId() {
        return userStatusTypeId;
    }

    public void setUserStatusTypeId(int userStatusTypeId) {
        this.userStatusTypeId = userStatusTypeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String l) {
        this.localeStr = l;
    }

    // @Transient
    public String getFullname()
    {
        if( getUserType().getUserId() || getUserType().getUsername() )
            return email;
        
        if( getUserType().getAnonymous() )
            return "Anonymous";
        
        if( getUserType().getPseudo() )
            return "Pseudonymized";
        
        String fullName = firstName;

        if( fullName == null )
            fullName = "";

        if( lastName != null && lastName.length() > 0 )
        {
            if( fullName.length() > 0 )
                fullName += " ";

            fullName += lastName;
        }

        return fullName;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
    }

    public int getEthnicCategoryId() {
        return ethnicCategoryId;
    }

    public void setEthnicCategoryId(int ethnicCategoryId) {
        this.ethnicCategoryId = ethnicCategoryId;
    }

    public String getEthnicCategoryIdStr() {
        return Integer.toString( ethnicCategoryId );
    }

    public void setEthnicCategoryIdStr(String s)
    {
        this.ethnicCategoryId = s==null || s.length()==0 ? 0 : Integer.parseInt( s );
    }

    public String getRacialCategories() {
        return racialCategories;
    }

    public void setRacialCategories(String racialCategories) {
        this.racialCategories = racialCategories;
    }


    public int getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(int userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getExtRef() {
        return extRef;
    }

    public void setExtRef(String extRef) {
        this.extRef = extRef;
    }

    public String getIpCountry() {
        return ipCountry;
    }

    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public String getPhoneAreaCode() {
        return phoneAreaCode;
    }

    public void setPhoneAreaCode(String phoneAreaCode) {
        this.phoneAreaCode = phoneAreaCode;
    }

    public String getMobilePhone() {
        return phonePrefix;
    }

    public String getOfficePhone() {
        return phoneLast;
    }



    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public String getPhoneLast() {
        return phoneLast;
    }

    public void setPhoneLast(String phoneLast) {
        this.phoneLast = phoneLast;
    }

    public int getGenderTypeId() {
        return genderTypeId;
    }

    public void setGenderTypeId(int genderTypeId) {
        this.genderTypeId = genderTypeId;
    }

    public int getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }

    public String getAltIdentifier() {
        return altIdentifier;
    }

    public void setAltIdentifier(String altIdentifier) {
        this.altIdentifier = altIdentifier;
    }

    public String getAltIdentifierName() {
        return altIdentifierName;
    }

    public void setAltIdentifierName(String altIdentifierName) {
        this.altIdentifierName = altIdentifierName;
    }

    public String getEmailResultsTo() {
        return emailResultsTo;
    }

    public void setEmailResultsTo(String emailResultsTo) {
        this.emailResultsTo = emailResultsTo;
    }

    public String getTextResultsTo() {
        return textResultsTo;
    }

    public void setTextResultsTo(String textResultsTo) {
        this.textResultsTo = textResultsTo;
    }

    public int getUserCompanyStatusTypeId() {
        return userCompanyStatusTypeId;
    }

    public void setUserCompanyStatusTypeId(int userCompanyStatusTypeId) {
        this.userCompanyStatusTypeId = userCompanyStatusTypeId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getEmployerUrl() {
        return employerUrl;
    }

    public void setEmployerUrl(String employerUrl) {
        this.employerUrl = employerUrl;
    }

    public String getIpZip() {
        return ipZip;
    }

    public void setIpZip(String ipZip) {
        this.ipZip = ipZip;
    }

    public String getIpTimezone() {
        return ipTimezone;
    }

    public void setIpTimezone(String ipTimezone) {
        this.ipTimezone = ipTimezone;
    }

    public Date getLockoutDate() {
        return lockoutDate;
    }

    public void setLockoutDate(Date lockoutDate) {
        this.lockoutDate = lockoutDate;
    }

    public Date getPasswordStartDate() {
        return passwordStartDate;
    }

    public void setPasswordStartDate(Date passwordStartDate) {
        this.passwordStartDate = passwordStartDate;
    }

    public int getResetPwd() {
        return resetPwd;
    }

    public void setResetPwd(int resetPwd) {
        this.resetPwd = resetPwd;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }


}
