package com.tm2score.entity.profile;

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






@Entity
@Table( name = "profileproductmap" )
@NamedQueries({
    @NamedQuery ( name="ProfileProductMap.findByOrgIdAndProductId", query="SELECT o FROM ProfileProductMap AS o WHERE o.orgId=:orgId AND o.productId=:productId" ),
    @NamedQuery ( name="ProfileProductMap.findByProductId", query="SELECT o FROM ProfileProductMap AS o WHERE o.productId=:productId" )
})
public class ProfileProductMap implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="profileproductmapid")
    private int profileProductMapId;

    @Column(name="profileid")
    private int profileId;

    @Column(name="orgid")
    private int orgId;

    @Column(name="productid")
    private int productId;

    @Transient
    private Profile profile;


    @Override
    public String toString() {
        return "ProfileProductMap{" + "profileProductMapId=" + profileProductMapId + ", profileId=" + profileId + ", orgId=" + orgId + ", productId=" + productId + '}';
    }

    public int getProfileProductMapId() {
        return profileProductMapId;
    }

    public void setProfileProductMapId(int profileProductMapId) {
        this.profileProductMapId = profileProductMapId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    
    
}
