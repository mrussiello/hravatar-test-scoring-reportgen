/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.user;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.service.LogService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author miker
 */

@Named
@SessionScoped
public class ReportDownloadBean implements Serializable {
    
    transient StreamedContent sc = null;
            
    
    public static ReportDownloadBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        if( fc==null )
            return null;
        
        return (ReportDownloadBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "reportDownloadBean" );
    }
    
    
    
    public void clearBean()
    {
        sc=null;
    }
    
    public StreamedContent getReportFileForDownload()
    {
        UserBean eb = UserBean.getInstance();
        if( !eb.getUserLoggedOnAsAdmin() )
            return null;

        
        HttpServletRequest req = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
        
        // LogService.logIt( "ReportDownloadRequest.getreportfileForDownload() " + req.getRequestURI() );
        
        // String pv;
        // Iterator<String> iter = req.getParameterNames().asIterator();
        
        //while( iter.hasNext() )
        //{
        //    pv = iter.next();
            
        //    LogService.logIt( "ReportDownloadRequest.getreportfileForDownload() ParamName: " + pv + ", value=" + req.getParameter(pv) );
        //}
        
        String tesid = req.getParameter("tesid");
                
        long tesId = tesid!=null && !tesid.isBlank() ? Long.parseLong( tesid ) : 0;

        if( tesId<=0 && sc!=null )
            return sc;
        
        sc = null;
        
        TestEventScore tes = null;
        
        if( tesId>0 )
        {
            for( TestEventScore ts : eb.getReportTestEventScoreList() )
            {
                if( tesId==ts.getTestEventScoreId() && ts.getHasReport() )
                {
                    tes=ts;
                    break;
                }
            }
        }        

        // LogService.logIt( "ReportDownloadBean.getreportfileForDownload() tesId=" + tesId + ", tesid=" + tesid + " tes=" + (tes==null ? "null" : "Present") );
        
        if( tes==null )
        {
            LogService.logIt( "ReportDownloadBean.getreportfileForDownload() tesId=0 Ignoring request." );
            return null;
        }
        
        try
        {
            byte[] bytes = tes.getReportBytes();

            // LogService.logIt( "ReportDownloadRequest.getreportfileForDownload() report size is " + bytes.length );

            ByteArrayInputStream baos = new ByteArrayInputStream( bytes );
            
            sc = DefaultStreamedContent.builder().contentType( "application/pdf" ).name( tes.getReportFilename() ).stream( () -> baos ).build();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportDownloadBean.getreportfileForDownload() " );
            return null;
        }

        return sc;
    }
    
    
    
    
}
