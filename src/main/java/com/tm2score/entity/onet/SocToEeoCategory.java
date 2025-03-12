/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.entity.onet;

import com.tm2score.custom.bestjobs.EeoJobCategoryType;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

/**
 *
 * @author Mike
 */
@Entity
@Table( name = "soctoeeocategory" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="Soc2Eeoc.findForSoc", query="SELECT o FROM SocToEeoCategory AS o WHERE o.socCode=:socCode" )
})
public class SocToEeoCategory implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="soctoeeocategoryid")
    private long socToEeoCategoryId;

    @Column(name="eeocategorytypeid")
    private int eeoCategoryTypeId;

    @Column(name="soccode")
    private String socCode;

    public EeoJobCategoryType getEeoJobCategoryType()
    {
        return EeoJobCategoryType.getValue(eeoCategoryTypeId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.eeoCategoryTypeId;
        hash = 67 * hash + Objects.hashCode(this.socCode);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SocToEeoCategory other = (SocToEeoCategory) obj;
        return this.socToEeoCategoryId == other.socToEeoCategoryId;
    }
    
    
    
    public long getSocToEeoCategoryId() {
        return socToEeoCategoryId;
    }

    public void setSocToEeoCategoryId(long socToEeoCategoryId) {
        this.socToEeoCategoryId = socToEeoCategoryId;
    }

    public int getEeoCategoryTypeId() {
        return eeoCategoryTypeId;
    }

    public void setEeoCategoryTypeId(int eeoCategoryTypeId) {
        this.eeoCategoryTypeId = eeoCategoryTypeId;
    }

    public String getSocCode() {
        return socCode;
    }

    public void setSocCode(String socCode) {
        this.socCode = socCode;
    }

    
    
    
}
