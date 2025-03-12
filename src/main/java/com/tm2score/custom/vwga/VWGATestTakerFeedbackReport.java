/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.vwga;

import com.tm2score.custom.coretest2.devel.*;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class VWGATestTakerFeedbackReport extends BaseVWGATestTakerFeedbackTemplate implements ReportTemplate
{
    boolean comboCompetenciesOnly = true;
    
    
    
    
    public VWGATestTakerFeedbackReport()
    {
        super();
        
        this.devel = true;
        // this.redYellowGreenGraphs=true;

    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            specialInit();
            
            // LogService.logIt( "CT2TestTakerFeedbackReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );

            addCoverPageV2(true);
            
            addNewPage();
            
            addReportInfoHeader();
            
            for( int i=1;i<=5;i++ )
                addCustomInfo( i );

            addAbilitiesInfo();

            addKSInfo();

            addAIMSInfo();

            addEQInfo();
            
            addBiodataInfo();
            
            addPreparationNotesSection();
            
            //addNewPage();

            //addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CT2TestTakerFeedbackReport.generateReport() " );

            throw new STException( e );
        }
    }

    @Override
    public void addAnyCompetenciesInfo(  java.util.List<TestEventScore> teslst, String titleKey, String titleText, String subtitleKey, String subtitleText, String detailKey, String descripKey, String caveatHeaderKey, String caveatFooterKey, boolean singleColumn, boolean withInterview, boolean noInterviewLimit, boolean repeatHeadersNewPages) throws Exception
    {
        if( teslst==null || teslst.isEmpty() )
            return;
        
        List<TestEventScore> teslst2 = new ArrayList<>();
        
        for( TestEventScore tes : teslst )
        {
            if( !comboCompetenciesOnly || tes.getSimCompetencyClass().getIsCombo() )
                teslst2.add(tes);
        }
        
        super.addAnyCompetenciesInfo(teslst2, titleKey, titleText, subtitleKey, subtitleText, detailKey, descripKey, caveatHeaderKey, caveatFooterKey, singleColumn, withInterview, noInterviewLimit, repeatHeadersNewPages);
    }
    
    
    
    private void specialInit()
    {
        if( developmentReportUtils== null )
        {
            
            comboCompetenciesOnly = reportData.getReportRuleAsInt("combocompetenciesonly")==1;
            
            if( reportData.getReport().getStrParam6() !=null && !reportData.getReport().getStrParam6().isEmpty() )
                bundleToUse = reportData.getReport().getStrParam6();

            Locale loc = reportData.getLocale();
            
            if( bundleToUse==null || bundleToUse.isEmpty() )
            {
                
                defaultBundleToUse = "vwgafeedback.properties";
                
                //String stub = "";
                if( loc.getLanguage().equalsIgnoreCase( "en" ) )                
                    bundleToUse = "vwgafeedback.properties";   
                else
                   bundleToUse = "vwgafeedback_" + loc.getLanguage().toLowerCase() + ".properties";   
            }
            
            String bundleToUse2 = "candidatefeedback.properties";
                    
            if( !loc.getLanguage().equalsIgnoreCase( "en" ) )                
                bundleToUse2 = "candidatefeedback_" + loc.getLanguage().toLowerCase() + ".properties";   

            developmentReportUtils = new CT2DevelopmentReportUtils( bundleToUse, bundleToUse2, defaultBundleToUse );
        }        
    }
    

    
}
