/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class StdSelection extends BaseReportTemplate implements ReportTemplate
{

    public StdSelection()
    {
        super();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            addCoverPage();

            addNewPage();

            addReportInfoHeader();

            addSummarySection();

            addComparisonSection();

            boolean addedPage = addAssessmentOverview();

            if( !addedPage )
                addNewPage();

            addDetailedReportInfoHeader();

            // Tasks before competencies
            if( reportData.getReport().getIncludeTaskInfo() == 1)
                addTasksInfo();

            addCompetencyInfo();

            // Tasks after competencies
            if( reportData.getReport().getIncludeTaskInfo() == 2)
                addTasksInfo();

            addWritingSampleInfo();

            addBiodataInfo();

            addInterestInfo();

            addExperienceInfo();

            addMinQualsApplicantDataInfo();

            addPreparationNotesSection();

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
            LogService.logIt( e, "StdSelection.generateReport() " );

            throw new STException( e );
        }
    }

}
