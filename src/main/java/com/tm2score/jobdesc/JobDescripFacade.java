package com.tm2score.jobdesc;

import com.tm2score.entity.jobdesc.JobDescrip;
import com.tm2score.entity.jobdesc.UserJobMap;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class JobDescripFacade
{
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;
    
    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;
    

    public static JobDescripFacade getInstance()
    {
        try
        {
            return (JobDescripFacade) InitialContext.doLookup( "java:module/JobDescripFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JobDescripFacade.getInstance() " );
            return null;
        }
    }
    
    
    public List<UserJobMap> getUserJobMapListForUser( long userId ) throws Exception
    {
        try
        {
            if( userId<=0 )
                return new ArrayList<>();

            Query q = em.createNamedQuery( "UserJobMap.findForUserId" );
            q.setParameter( "userId", userId );
            return (List<UserJobMap>) q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JobDescripFacade.getUserJobMapListForUser( userId=" + userId + " ) " );

            throw new STException( e );
        }
        
    }
        
    public JobDescrip getJobDescrip( int jobDescripId ) throws Exception
    {
        try
        {
            return (JobDescrip) emmirror.createNamedQuery( "JobDescrip.findByJobDescripId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter("jobDescripId", jobDescripId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "JobDescripFacade.getJobDescrip( " + jobDescripId + " ) " );
            throw new STException( e );
        }
    }      
    
    
    public UserJobMap saveUserJobMap( UserJobMap ujdm ) throws Exception
    {
        try
        {
            if( ujdm.getUserJobMapId() > 0 )
            {
                em.merge(ujdm );
            }

            else
            {
                em.detach(ujdm );
                em.persist(ujdm );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return ujdm;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "JobDescripFacade.saveUserJobMap() " + ( ujdm == null ? "UserJobMap is null" : ujdm.toString() ) );

            throw new STException( e );
        }
    }
    

}
