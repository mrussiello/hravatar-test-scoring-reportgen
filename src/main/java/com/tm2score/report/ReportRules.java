/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2score.entity.corp.Corp;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.user.Org;
import com.tm2score.entity.user.Suborg;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class ReportRules {
    
    
    public Map<String,String> reportRules;
    //public List<NVPair> reportRules = null;
    
    public ReportRules( Org org, Suborg suborg, Product product, Report report, Corp corp)
    {
        reportRules = StringUtils.getReportFlagMap(org, suborg, report, product, corp);
        
        // LogService.logIt( "ReportRules() orgId=" + (org==null ? "null" : org.getOrgId()) + ", suborgId=" + (suborg==null ? "null" : suborg.getSuborgId()) + ", productId=" + (product==null ? "null" : product.getProductId())  + ", reportId=" + (report==null ? "null" : report.getReportId()) );
        //if( org!=null )
        //    reportRules=org.getReportFlagList( suborg, report, product);
    }
        
    
    public String getReportRuleAsString( String name )
    {
        if( name == null || name.isEmpty() || reportRules == null || reportRules.isEmpty() )
            return null;

        return reportRules.get(name);
    }
    
    
    public int getReportRuleAsInt( String name )
    {
        //if(1==1)
        //    return null;
        String sv = getReportRuleAsString( name );

        if( sv == null || sv.trim().isEmpty() )
            return 0;

        try
        {
            int v = Integer.parseInt( sv );
            return v;
        }

        catch( NumberFormatException e )
        {
            LogService.logIt( e, "ReportData.getReportRuleAsInt() " + name + ", value=" + sv );
        }

        return 0;
    }
    
    public float getReportRuleAsFloat( String name )
    {
        //if(1==1)
        //    return null;
        String sv = getReportRuleAsString( name );

        if( sv == null || sv.trim().isEmpty() )
            return 0;

        try
        {
            float v = Float.parseFloat( sv );
            return v;
        }

        catch( NumberFormatException e )
        {
            LogService.logIt( e, "ReportData.getReportRuleAsFloat() " + name + ", value=" + sv );
        }

        return 0;
    }
    

    public boolean getReportRuleAsBoolean( String name )
    {
       return getReportRuleAsInt( name ) == 1;
    }    
}
