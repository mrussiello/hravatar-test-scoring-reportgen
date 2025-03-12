package com.tm2score.entity.corp;

import java.io.Serializable;
import jakarta.persistence.Cacheable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Cacheable
@Entity
@Table( name = "corp" )
@NamedQueries({
	@NamedQuery( name = "Corp.findByCorpId", query = "SELECT o FROM Corp AS o WHERE o.corpId=:corpId" )
})
public class Corp implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "corpid" )
    private int corpId;

    @Column( name = "name" )
    private String name = "";

    @Column( name = "altidentifiername" )
    private String altIdentifierName;

    @Column( name = "proctorparams" )
    private String proctorParams;
    
      
    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAltIdentifierName() {
        return altIdentifierName;
    }

    public void setAltIdentifierName(String altIdentifierName) {
        this.altIdentifierName = altIdentifierName;
    }

    public String getProctorParams() {
        return proctorParams;
    }

    public void setProctorParams(String proctorParams) {
        this.proctorParams = proctorParams;
    }



}
