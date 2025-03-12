/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.scorer;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.EventFacade;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class KbMgrTestEventScorer  extends CT2TestEventScorer {
    
    
   @Override
   public void calculateOverallScores() throws Exception
   {
        // ASSUMES:
        //   1. Each competency score is a total points. Will still work for other "points" types.  And that the competency raw score is derived from CompetencyScoreType
        //   2. Overall Raw Score is equal to the sum of all competency raw scores. 
        //   3. Overall Scaled Score is set to use raw score with no changes. Will be over-ridden below and set to SCT.id
       
        // process normally. 
        super.calculateOverallScores();       
       
        // Calculate "Score"
       
        int basics = 0;
        int lows = 0;
        
        // Do competencies first
        for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
        {
            if( !scs.hasAnyScoreData() )
                continue;
            
            if( !scs.getCompetencyScoreType().isPointAccum() )
            {
                LogService.logIt( "KbMgrTestEventScorer.calculateOverallScores() testEventId=" + te.getTestEventId() + ", Skipping non-points competencyscoretype. Competency=" + scs.getName() );                
                continue;
            }
            
            ScoreCategoryType sct = ScoreCategoryType.getValue( scs.getScoreCategoryTypeId(scoreColorSchemeType) );
            
            if( sct==null )
                continue;
            
            if( sct.red() )
                lows++;
            
            if( sct.redYellow() )
                basics++;
        }
        
        ScoreCategoryType sctFinal = null;
        
        if( lows >= 3 )
            sctFinal = ScoreCategoryType.RED;
        
        else if( lows >= 1 && lows <=2  )
            sctFinal = ScoreCategoryType.YELLOW;
        
        else if( basics >= 3  )
            sctFinal = ScoreCategoryType.YELLOW;
        
        else
            sctFinal = ScoreCategoryType.GREEN;
        
        scrSum.append( "KbMgrTestEventScorer: Basics=" + basics + ", Lows=" + lows + ", Final ScoreCategory=" + sctFinal.getName( reportLocale ) + ", overallScore=" + sctFinal.getScoreCategoryTypeId() + "\n" );
        // LogService.logIt( "KbMgrTestEventScorer.calculateOverallScores() testEventId=" + te.getTestEventId() + ", basics=" + basics + ", lows=" + lows + ", category selected=" + sctFinal.getName( Locale.US ) + ", overallScore=" + sctFinal.getScoreCategoryTypeId() );
                
        // Now set the final rating. 
        te.setOverallRating( sctFinal.getScoreCategoryTypeId() );   
        te.setOverallScore( sctFinal.getScoreCategoryTypeId()  );
        
        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();
        
        eventFacade.saveTestEvent(te);
   }
    
   @Override
    public int setOverallScoreAndPercentile( int counter ) throws Exception
    {            
        // process normally. 
        counter = super.setOverallScoreAndPercentile(counter);
     
        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();

        te.setTestEventScoreList( eventFacade.getTestEventScoresForTestEvent( te.getTestEventId(), true ) );
        
        TestEventScore otes = te.getOverallTestEventScore();
        
        if( otes==null )
            return counter;
        
        otes.setScoreCategoryId( te.getOverallRating() );
        
        ScoreCategoryType sct = ScoreCategoryType.getValue( te.getOverallRating() );
        
        String scrText = sct.getName( reportLocale );
        
        otes.setScoreText(scrText);
        
        otes.setScore( sct.getScoreCategoryTypeId() );        
        te.setOverallScore( sct.getScoreCategoryTypeId() );

        LogService.logIt( "KbMgrTestEventScorer.setOverallScoreAndPercentile() testEventId=" + te.getTestEventId() + ", set overall score=" + te.getOverallScore() + ", set OTES scoretext=" + scrText );
        
        eventFacade.saveTestEventScore(otes);   
        eventFacade.saveTestEvent(te);
        
        return counter;
   }
    
    // This is a value that can be
    @Override
    public String getAdditionalTextScoreContentPacked() throws Exception
    {
        return "";
    }

    
    
   @Override
    public void finalizeScore() throws Exception
    {               
        if( eventFacade==null )
            eventFacade=EventFacade.getInstance();

        //te.setOverallScore( ovrScaledScr );

        te.setScoreFormatTypeId( scoreFormatTypeId );
        if( scoreColorSchemeType!=null )
            te.setScoreColorSchemeTypeId(scoreColorSchemeType.getScoreColorSchemeTypeId());

        // This is the reportId to be used for presentation. May be changed on rescores/rereports, so remove here.
        te.setReportId( 0 );
        te.setTestEventStatusTypeId( TestEventStatusType.SCORED.getTestEventStatusTypeId() );

        eventFacade.saveTestEvent(te);

        eventFacade.reorderItemResponses( te.getTestEventId(), 0 );
        
        deleteOldTestEventScores();
    }
    
    
    
    
}
