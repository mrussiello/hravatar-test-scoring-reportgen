package com.tm2score.jobdesc;

import com.tm2score.entity.event.jobdesc.JobDescrip;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

import javax.naming.InitialContext;


import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;



@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class JobDescripFacade
{
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

}
