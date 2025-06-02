package com.tm2score.user;

import com.tm2score.entity.user.OrgTraits;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class OrgTraitsFacade
{
    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;
    

    public static OrgTraitsFacade getInstance()
    {
        try
        {
            return (OrgTraitsFacade) InitialContext.doLookup( "java:module/OrgTraitsFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OrgTraitsFacade.getInstance() " );
            return null;
        }
    }
    
    
    public OrgTraits getOrgTraitsByOrgId(int orgId ) throws Exception
    {
        try
        {
            return (OrgTraits) emmirror.createNamedQuery( "OrgTraits.findByOrgId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("orgId", orgId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "OrgTraitsFacade.getOrgTraitsByOrgId( orgId=" + orgId + " ) " );
            throw new STException( e );
        }
        
    }    

}
