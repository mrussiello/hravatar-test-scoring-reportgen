/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest2.*;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.event.EventFacade;
import com.tm2score.event.NormFacade;
import com.tm2score.global.Constants;
import com.tm2score.score.ScoreUtils;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.xml.JaxbUtils;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class CT2TestEventScorer extends StandardTestEventScorer {


    @Override
    public Map<String,Object> getPercentile( int productId, int alternateSimDescriptorId, TestEvent te, TestEventScore tes, int orgId, String countryCode) throws Exception
    {
        Date procStart = new Date();

        if( normFacade == null )
            normFacade = NormFacade.getInstance();

        int minSimVersionIdForMajorVersion = 0;
        
        if( te.getSimXmlObj()!=null && te.getSimXmlObj().getMinsimveridformajorversion()>0 )
            minSimVersionIdForMajorVersion = te.getSimXmlObj().getMinsimveridformajorversion();
        
        Map<String,Object> o = normFacade.getPercentile(productId, ScoreUtils.getPercentileScoreTypeIdForTestEvent(te), te.getSimId(), te.getSimVersionId(), te.getTestEventId(), minSimVersionIdForMajorVersion, tes, orgId, countryCode, null, null, null);

        if( alternateSimDescriptorId>0 && !normFacade.hasValidPercentiles( o, orgId, countryCode ) )
        {
            LogService.logIt( "CT2TestEventScorer.getPercentile() using alternateSimDescriptorId=" + alternateSimDescriptorId + " for percentiles." );
            if( eventFacade==null)
                eventFacade=EventFacade.getInstance();
            SimDescriptor sd2 = eventFacade.getSimDescriptor( alternateSimDescriptorId );
            if( sd2!=null && sd2.getXml()!=null && !sd2.getXml().isBlank())
            {
                SimJ simJ2 = JaxbUtils.ummarshalSimDescriptorXml( sd2.getXml() );                
                minSimVersionIdForMajorVersion = simJ2.getMinsimveridformajorversion();
                
                Map<String,Object> o2 = normFacade.getPercentile(productId, ScoreUtils.getPercentileScoreTypeIdForTestEvent(te), sd2.getSimId(), sd2.getSimVersionId(), te.getTestEventId(), minSimVersionIdForMajorVersion, tes, orgId, countryCode, null, null, null);        
                normFacade.combinePercentileValues( o, o2 );
            }
        }
        
        Tracker.addResponseTime( "Get Percentile", new Date().getTime() - procStart.getTime() );        
        
        return o;
    }



    @Override
    public String getAdditionalTextScoreContentPacked() throws Exception
    {

        String rfstr = null;
        
        if( te !=null && te.getProduct()!=null )
        {
            if( te.getProduct().getProductType().getIsSim() )
                rfstr = te.getProduct().getStrParam2();
            
            else if( te.getProduct().getProductType().getIsCt5Direct())
                rfstr = te.getProduct().getPreviewHead();
        }

        try
        {

            // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() AAA rfstr=" + rfstr );

            StringBuilder sb = new StringBuilder();

            Locale locale = reportLocale; //  te.getReport()!=null && te.getReport().getLocaleForReportGen()!=null ? te.getReport().getLocaleForReportGen() : I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() ); // I18nUtils.getLocaleFromCompositeStr( te.getLocaleStrReport() );

            //List<String> ids = new ArrayList<>();
            //List<String> riskFactors = new ArrayList<>();

            // String riskTxt;

            for( CT3RiskFactorType ct3Rft : CT3RiskFactorType.values() )
            {
                if( !ct3Rft.isStandard() )
                    continue;

                // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() TESTING FOR " + ct3Rft.name() );

                if( ct3Rft.isRiskFactorPresent( te, null ) )
                {
                    // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() PRESENT " + ct3Rft.name() );
                    if( sb.length() > 0 )
                        sb.append( Constants.DELIMITER );

                    sb.append( ct3Rft.getCT3RiskFactorTypeId() + Constants.DELIMITER + ct3Rft.getRiskText( locale, null ) + "[FACET]riskfactorid:" + ct3Rft.getCT3RiskFactorTypeId() + Constants.DELIMITER + Constants.DELIMITER  );
                    // riskFactors.add( MessageFactory.getStringMessage( locale , ct3Rft.getKey()) );
                }
            }

            if( rfstr!=null && !rfstr.isEmpty()  )
            {
                String[] g = rfstr.split("\\|");

                CT3RiskFactorType rft;

                Map<String,Object> params;

                for( String cs : g )
                {
                    if( cs==null || cs.isEmpty() || cs.indexOf( ';' )<=0 )
                        continue;

                    // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() Examining Extra String " + cs );

                    params = CT3RiskFactorType.getParamsFromConfigStr(cs);

                    Integer ti = (Integer) params.get( "id" );

                    if( ti == null || ti<= 0 )
                        continue;

                    rft = CT3RiskFactorType.getValue(ti);

                    // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() Extra Risk: " + rft.getName() );

                    if( rft.isRiskFactorPresent(te,params ))
                    {
                        // LogService.logIt( "CT2TestEventScorer.getAdditionalTextScoreContentPacked() Extra Risk: " + rft.getName() + " IS PRESENT!" );

                        if( sb.length() > 0 )
                            sb.append( Constants.DELIMITER );

                        sb.append( rft.getCT3RiskFactorTypeId() + Constants.DELIMITER + rft.getRiskText( locale, params ) + "[FACET]riskfactorid:" + rft.getCT3RiskFactorTypeId() + Constants.DELIMITER + Constants.DELIMITER );
                    }
                }

            }

            if( sb.length() > 0 )
                return ";;;" + CT3Constants.CT3RISKFACTORS + ";;;" + Constants.DELIMITER + sb.toString();

            return "";

        }

        catch( Exception e )
        {
            LogService.logIt( e, "getAdditionalTextScoreContentPacked() " + rfstr + ", " + te.toString() );

            throw e;
        }
    }

    
    public String toString()
    {
        return "Ct2TestEventScorer() ";
    }

}
