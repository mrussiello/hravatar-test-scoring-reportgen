/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.ct5.event;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.ct5.Ct5ItemType;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.xml.JaxbUtils;

/**
 *
 * @author miker
 */
public class Ct5ResumeUtils {
    
    public static SimJ resumeSimJ;
    
    public static synchronized void init() throws Exception
    {
        if( resumeSimJ!=null )
            return;
        
        try
        {
            SimDescriptor sd = EventFacade.getInstance().getSimDescriptor(RuntimeConstants.getLongValue("resumesimid"), RuntimeConstants.getIntValue("resumesimversionid"), true);
            if( sd==null )
            {
                LogService.logIt( "Ct5ResumeUtils.init() Unable to load Resume Sim Descriptor. simId=" + RuntimeConstants.getLongValue("resumesimid") + ", simVersionId=" + RuntimeConstants.getLongValue("resumesimversionid") );
                throw new Exception( "Ct5ResumeUtils.init() Unable to load Resume Sim Descriptor. simId=" + RuntimeConstants.getLongValue("resumesimid") + ", simVersionId=" + RuntimeConstants.getLongValue("resumesimversionid"));
            }            
            resumeSimJ = JaxbUtils.ummarshalSimDescriptorXml( sd.getXml());
        }
        catch( Exception e )
        {
            LogService.logIt(e, "Ct5ResumeUtils.init() ");
            throw e;
        }
    }
    
    public static SimJ.Intn getResumeIntn( int ct5ItemId )
    {
        if( ct5ItemId<=0 )
            return null;
        
        try
        {
            if( resumeSimJ==null )
                init();
            
            for( SimJ.Intn intn : resumeSimJ.getIntn() )
            {
                if( intn.getCt5Itemtypeid()!=Ct5ItemType.RESUME.getCt5ItemTypeId() )
                    continue;
                
                if( intn.getCt5Itemid()==ct5ItemId )
                    return intn;
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ResumeAdminUtils.getStandardResumeIntn() Error Error Finding Resume Intn by ct5ItemId=" + ct5ItemId );
        }

        return null;   
    }
    
    public static SimJ.Intn getResumeIntnByUniqueId( String uniqueId )
    {
        if( uniqueId==null || uniqueId.isBlank() )
            return null;
        
        try
        {
            if( resumeSimJ==null )
                init();
            
            for( SimJ.Intn intn : resumeSimJ.getIntn() )
            {
                if( intn.getCt5Itemtypeid()!=Ct5ItemType.RESUME.getCt5ItemTypeId() )
                    continue;
                
                if( intn.getUniqueid()!=null && intn.getUniqueid().equals(uniqueId) )
                    return intn;
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ResumeAdminUtils.getResumeIntnByUniqueId() Error Finding Resume Intn by uniqueId=" + uniqueId );
        }

        return null;   
    }
    
    
}
