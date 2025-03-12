package com.tm2score.entity.user;

import com.tm2score.user.GeographicRegionType;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Entity
@Table( name = "countrytype" )
@NamedQueries({
    @NamedQuery ( name="Country.findByCode", query="SELECT o FROM Country AS o WHERE o.countryCode=:cc" )
})
public class Country implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @Column(name="countryid")
    private int countryId;

    @Column(name="countrycode")
    private String countryCode;

    @Column(name="name")
    private String name;

    @Column(name="geographicregionid")
    private int geographicRegionId;


    public GeographicRegionType getGeographicRegionType()
    {
        return GeographicRegionType.getValue( geographicRegionId );
    }

    public String getCountryCode()
    {
        return countryCode;
    }

    public void setCountryCode( String countryCode )
    {
        this.countryCode = countryCode;
    }

    public int getCountryId()
    {
        return countryId;
    }

    public void setCountryId( int countryId )
    {
        this.countryId = countryId;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public int getGeographicRegionId()
    {
        return geographicRegionId;
    }

    public void setGeographicRegionId( int geographicRegionId )
    {
        this.geographicRegionId = geographicRegionId;
    }



}
