/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.custom.hraph.tmldr.*;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.score.SimCompetencyGroup;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class TmLdrTestEventScorer  extends StandardTestEventScorer implements TestEventScorer {
    
    
    @Override
    public List<SimCompetencyGroup> getSimCompetencyGroupList() throws Exception
    {
            List<SimCompetencyGroup> scgl = new ArrayList<>();
 
            List<SimCompetencyScore> scsl = null;
            
            SimCompetencyGroup scg = null;
            
            String name = null;
            
            TmLdrCompetencyType tmLdrCompetencyType;

            for( int i=1;i<=5;i++ )
            {                
                tmLdrCompetencyType = TmLdrCompetencyType.getValue(i);
                
                // key = "g.TmLdrAssessOver_" + i + "_a";
                
                name = MessageFactory.getStringMessage( Locale.US, tmLdrCompetencyType.getKey(), null );
                
                scsl = TmLdrScoreUtils.getTmLdrSimCompetencyScoreList( tmLdrCompetencyType.getTmLdrCompetencyTypeId(), te.getSimCompetencyScoreList() );  
                
                if( scsl.isEmpty() )
                    continue;
                
                scg = new SimCompetencyGroup( name, SimCompetencyGroupType.CUSTOM.getSimCompetencyGroupTypeId() + tmLdrCompetencyType.getTmLdrCompetencyTypeId(), te.getSimXmlObj(), scsl, null, overallRawScoreCalcType );
                
                scgl.add( scg );                
            }
            
            return scgl;        
    }
    
    
    @Override
    public String getOverallScoreScoreText() throws Exception
    {        
        
        return TmLdrTestEventScorer.getScoreTextForOverallScore( te );
    }
    
    
    
    public static String getScoreTextForOverallScore( TestEvent te) throws Exception
    {
        try
        {
            float scrValue = te.getOverallTestEventScore() == null ? te.getOverallScore() : te.getOverallTestEventScore().getOverallRawScoreToShow();        
            
            String val = MessageFactory.getStringMessage( Locale.US, "g.TmLdrAssessOverScore_1") + " ";
            
            if( scrValue>=70 )
                val += MessageFactory.getStringMessage( Locale.US, "g.TmLdrAssessOverScoreHigh");

            else if( scrValue< 70 && scrValue>=30 )
                val += MessageFactory.getStringMessage( Locale.US, "g.TmLdrAssessOverScoreMedium");

            else
                val += MessageFactory.getStringMessage( Locale.US, "g.TmLdrAssessOverScoreLow");

            val += " " + MessageFactory.getStringMessage( Locale.US, "g.TmLdrAssessOverScore_2");
            
            // LogService.logIt( "TmLdrTestEventScorer.getScoreTextForOverallScore() testEventId=" + te.getTestEventId() );
            
            return val;
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "TmLdrTestEventScorer.getOverallScoreText() ");
            
            return null;
        }
    }
    
    
    
}
