/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.pci;



import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class PciAvReport extends BasePciAvReportTemplate implements ReportTemplate
{
    
    
    public PciAvReport()
    {
        super();
        
        this.devel = false;
        
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            initPciSpecial();
            // this.redYellowGreenGraphs=false;
            // LogService.logIt( "CTSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(true);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();

            addComparisonSection();

            if( reportData.getReportRuleAsBoolean("cmptysumoff") ||
                reportData.getReport().getIncludeCompetencyScores()!=1 ||
                ( reportData.getReport().getIncludeSubcategoryCategory()!=1 &&
                  reportData.getReport().getIncludeSubcategoryNumeric()!=1 && 
                  reportData.getReport().getIncludeCompetencyColorScores()!=1 )  )            
            {}
            else
                addNewPage();

            addAssessmentOverview();

            addDetailedReportInfoHeader();
            
            addAltScoreSection();            

            // Tasks before competencies
            if( reportData.getReport().getIncludeTaskInfo() == 1)
                addTasksInfo();

            addAbilitiesInfo();

            addKSInfo();

            addAIMSInfo();

            addBiodataInfo();

            addEQInfo();
            
            addAIInfo();

            addWritingSampleInfo();
            
            addAvUploadSampleInfo();
            
            addIbmInsightSection();

            addIdentityImageCaptureSection();

            addSuspiciousActivitySection();

            addSuspensionsSection();

            addItemScoresSection();


            // addCompetencyInfo();

            // Tasks after competencies
            if( reportData.getReport().getIncludeTaskInfo() == 2)
                addTasksInfo();

            addTopJobMatchesSummarySection();

            addMinQualsApplicantDataInfo();

            addEducTrainingInfo();

            addReportRiskFactorSection();

            addUploadedFilesSection();

            addPreparationNotesSection();

            addCalculationSection(true);

            if( !reportData.getReportRuleAsBoolean( "usernotesoff" ) )
                addNewPage();

            addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "PciReport.generateReport() " );
            throw new STException( e );
        }
    }


    
    

}
