/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.metrobank.moss;

import com.tm2score.custom.hraph.bsp.itss.*;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.report.ReportTemplate;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimJUtils;
import com.tm2score.xml.JaxbUtils;

/**
 * CoreTest Selection Report.
 *
 * @author Mike
 */
public class MossSelectionReport extends BaseItssReportTemplate implements ReportTemplate
{

    public MossSelectionReport()
    {
        super();
        
        this.itss = new MossData();
    }


    @Override
    public byte[] generateReport() throws Exception
    {
        try
        {
            // LogService.logIt( "MossSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            
            EventFacade eventFacade = EventFacade.getInstance();
            
            SimDescriptor simDescriptor = eventFacade.getSimDescriptor( reportData.te.getSimId(), reportData.te.getSimVersionId(), true );

            if( simDescriptor == null )
                throw new Exception( "SimDescriptor for Sim " + reportData.te.getSimId() + ", V" + reportData.te.getSimVersionId() + " not found." );
                        
            reportData.equivSimJUtils =  new SimJUtils(JaxbUtils.ummarshalSimDescriptorXml( simDescriptor.getXml() ));
            
            addCoverPage(false);

            addNewPage();

            addReportInfoHeader();

            addCompetencySummaryChart();
                        
            addNewPage();

            addWritingSampleInfo();

            addNewPage();
            
            addRoleFitCompetencySummary();
            
            addAltRoleFitScoreSummary();
            
            addFindingsRecommendationsSummary();
                        
            addIdentityImageCaptureSection();
            
            addPreparationNotesSection();

            // addCalculationSection(false);
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "MossSelectionReport.generateReport() " );

            throw new STException( e );
        }
    }


}
