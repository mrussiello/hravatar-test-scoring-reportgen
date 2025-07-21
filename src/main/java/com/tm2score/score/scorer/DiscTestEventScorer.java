/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.custom.disc.DiscReportUtils;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.EventFacade;
import com.tm2score.service.LogService;
import java.util.List;

/**
 *
 * @author Mike
 */
public class DiscTestEventScorer extends CT5DirectTestEventScorer implements TestEventScorer {
    
    
    /*
      IMPORTANT = THIS scorer is NOT used currently. It was an experiment to make all scores DISC add up to 100. This made the average score 25 which just looks too low. 
    
       DISC is currently scored with the Ct5DirectTestEventScorer.
    */
    
    @Override
    public int setCompetencyTestEventScores( int counter ) throws Exception
    {
        int out = 0;
        try
        {
            // LogService.logIt( "DiscTestEventScorer.setCompetencyTestEventScores() testEventId=" + te.getTestEventId() );

            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();
            
            
            // Normal handling. 
            out = super.setCompetencyTestEventScores(counter);
            
            List<TestEventScore> tesl = eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true );
            
            float total = 0;
            float count = 0;
            for( TestEventScore tes : tesl )
            {
                if( !isDiscCompetency(tes) )
                    continue;
                
                total += tes.getScore();
                count++;
            }

            if( total<=0 || count<=0 )
                return out;
            
            float percent;
            for( TestEventScore tes : tesl )
            {
                // LogService.logIt( "DiscTestEventScorer.setCompetencyTestEventScores() BBB Checking/counting " + tes.getName() + ", isDiscCompetency=" + isDiscCompetency(tes) + ", testEventId=" + te.getTestEventId() );
                if( !isDiscCompetency(tes) )
                    continue;
                
                percent = 100*tes.getScore()/total;
                tes.setScore2(tes.getScore());
                tes.setScore( percent);
                eventFacade.saveTestEventScore(tes);
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DiscTestEventScorer.setCompetencyTestEventScores() testEventId=" + this.toString() );
        }
        return out;
    }
    
    private boolean isDiscCompetency(TestEventScore tes)
    {
        if( tes==null || !tes.getTestEventScoreType().getIsCompetency() )
            return false;
        
        for( String s : DiscReportUtils.DISC_COMPETENCY_NAMES )
        {
            if( s.equalsIgnoreCase( tes.getName()) || (tes.getNameEnglish()!=null && s.equalsIgnoreCase( tes.getNameEnglish() ) ))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString()
    {
        return "DiscTestEventScorer() testEventId=" + (this.te==null ? "null" : te.getTestEventId() + ", testKeyId=" + te.getTestKeyId());
    }
    

}
