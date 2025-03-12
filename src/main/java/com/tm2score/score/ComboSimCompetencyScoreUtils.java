/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyCombinationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class ComboSimCompetencyScoreUtils {
    
       
    
    public static List<SimCompetencyScore> getMembers( SimCompetencyScore scs, List<SimCompetencyScore> scsl, List<SimCompetencyScore> comboScsl, Map<Long,Float> simCompetencyIdWeightMap) throws Exception
    {
        List<SimCompetencyScore> out = new ArrayList<>();
        
        try
        {
            SimCompetencyCombinationType scct = SimCompetencyCombinationType.getValue( scs.getSimCompetencyObj().getCombinationtype() );
            
            String scids = scs.getSimCompetencyObj().getCombinationsimcompetencyids();

            if( scids==null || scids.isBlank() )
                return out;

            String scwts = scs.getSimCompetencyObj().getCombinationsimcompetencyweights();
            
            String[] sca = scids.split(",");
            String[] scw = scwts==null || scwts.isEmpty() ? null : scwts.split(",");
            
            long scId;

            SimCompetencyScore scsx;
            
            String scid=null;
            String wt;
            float weight;
            
            for( int i=0;i<sca.length; i++ )
            {
                try
                {
                    scid=sca[i];
                    
                    scid=scid.trim();
                    
                    scId = Long.parseLong(scid);
                    
                    wt = scw==null || scw.length<=i ? null : scw[i];
                    
                    weight = wt==null || wt.isBlank() ? 1 : Float.parseFloat(wt);
                    
                    scsx = getSimCompetencyScore(scId, scsl, comboScsl );
                    
                    if( scsx==null )
                        throw new Exception( "Cannot find SimCompetency for Id=" + scId + ", wt=" + wt );
                              
                    if( scct.isAverage() && weight<0 )
                        weight=1;
                    
                    if( simCompetencyIdWeightMap!=null )
                        simCompetencyIdWeightMap.put(scId, weight);
                    // scsx.setComboWeight(weight);
                    
                    out.add( scsx );
                }
                catch( NumberFormatException e )
                {
                    LogService.logIt( "ComboSimCompetencyScoreUtils.getMembers() scid=" + scid + ", " + e.getMessage() );
                }
            }
    
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ComboSimCompetencyScoreUtils.getMembers() " );
            
            throw e;
        }
    }
    
    
    public static SimCompetencyScore getSimCompetencyScore( long scId, List<SimCompetencyScore> scsl, List<SimCompetencyScore> comboScsl)
    {
        for( SimCompetencyScore scs : scsl )
        {
            if( scs.getSimCompetencyObj().getId() == scId )
                return scs;
        }

        if( comboScsl!=null )
        {
            for( SimCompetencyScore scs : comboScsl )
            {
                if( scs.getSimCompetencyObj().getId() == scId )
                    return scs;
            }
        }
        
        return null;
    }
}
