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
public class CT2GenericReport extends BaseCT2ReportTemplate implements ReportTemplate
{

    public CT2GenericReport()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            LogService.logIt( "CT2GenericReport.generateReport() STARTING for testEventId=" + reportData.getTestEvent().getTestEventId() + ", reportId=" + reportData.getReport().getReportId()  );
            
            addCoverPage(false);

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

            //String rule = reportData.getReportRule( "hidecompetencydetail" );
            
            //if( rule!=null )
            //    rule = rule.trim().toLowerCase();
            
            if( !reportData.getReportRuleAsBoolean("hidecompetencydetail") ) //  rule==null || rule.equalsIgnoreCase( "false") || rule.equals( "0") )
            {
                for( int i=1;i<=5;i++ )
                    addCustomInfo( i );

                addAbilitiesInfo();

                addKSInfo();

                addAIMSInfo();

                addBiodataInfo();

                addEQInfo();

            }
            
            //rule = reportData.getReportRule( "hidewritingsampleinfo" );
            
            //if( rule!=null )
            //    rule = rule.trim().toLowerCase();            
            
            if( !reportData.getReportRuleAsBoolean("hidewritingsampleinfo") ) // rule==null || rule.equalsIgnoreCase( "false") || rule.equals( "0") )
                addWritingSampleInfo();
            
            //rule = reportData.getReportRule( "hideimagecaptureinfo" );
            
            //if( rule!=null )
            //    rule = rule.trim().toLowerCase();            
            
            if( !reportData.getReportRuleAsBoolean("hideimagecaptureinfo") ) //  rule==null || rule.equalsIgnoreCase( "false") || rule.equals( "0") )
                addIdentityImageCaptureSection();

            // addCompetencyInfo();

            // Tasks after competencies
            if( reportData.getReport().getIncludeTaskInfo() == 2)
                addTasksInfo();

            addSuspensionsSection();

            addItemScoresSection();
            
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
            LogService.logIt( e, "CT2GenericReport.generateReport() " );

            //if( reportData!=null && reportData.getTestEvent()!=null && reportData.getTestEvent().getTestEventId()>0 )
            //    TestEventLogUtils.createTestEventLogEntry( reportData.getTestEvent().getTestEventId(),  "Ct2SelectionReport.generateReport()  Exception generating report. " + e.toString() );                    
                        
            throw new STException( e );
        }
    }


}
