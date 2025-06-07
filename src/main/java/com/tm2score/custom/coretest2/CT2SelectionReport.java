/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class CT2SelectionReport extends BaseCT2ReportTemplate implements ReportTemplate
{

    public CT2SelectionReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "CTSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPageV2(true);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();

            addResponseRatingSection();

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

            
            for( int i=1;i<=5;i++ )
                addCustomInfo( i );

            addAbilitiesInfo();

            addKSInfo();

            addInterestsInfo();
            
            addAIMSInfo();

            addEQInfo();
            
            addBiodataInfo();

            addAIInfo();

            addWritingSampleInfo();
            
            addIbmInsightSection();

            // LogService.logIt( "CT2SelectionReport");
            
            if( !reportData.getReportRuleAsBoolean("hideimagecaptureinfo") )
                addIdentityImageCaptureSection();

            addProctorCertificationsSection();
            
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

            addResumeSection();

            
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
            LogService.logIt( e, "CTSelectionReport.generateReport() " );

            //if( reportData!=null && reportData.getTestEvent()!=null && reportData.getTestEvent().getTestEventId()>0 )
            //    TestEventLogUtils.createTestEventLogEntry( reportData.getTestEvent().getTestEventId(),  "Ct2SelectionReport.generateReport()  Exception generating report. " + e.toString() );                    
                        
            throw new STException( e );
        }
    }


}
