/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.profile.alt;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.profile.Profile;
import com.tm2score.entity.profile.ProfileEntry;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.profile.ProfileUsageType;


/**
 *
 * @author miker_000
 */
public class TestEventAltScoreCalculator extends BaseAltScoreCalculator implements AltScoreCalculator
{
    TestEvent testEvent;
    
    Profile profile;
    
    public TestEventAltScoreCalculator( TestEvent te, Profile p ) throws Exception
    {
        this.testEvent = te;
        this.profile = p;
        
        if( !p.getProfileUsageType().equals( ProfileUsageType.ALTERNATE_OVERALL_COMPETENCY_WEIGHTS ))
            throw new Exception( "Profile has the wrong ProfileUsageType: " + p.toString()  );
    }
    
    
    @Override
    public float getScore()
    {
        float totalScore = 0;
        float totalWeight = 0;
        
        ProfileEntry pe;
        
        for( TestEventScore tes : testEvent.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
        {
            pe = profile.getLiveProfileEntry( tes.getName(), tes.getNameEnglish(), true );
            
            if( pe == null )
                continue;
            
            // LogService.logIt( "TestEventAltScoreCalculator getScore() for competency: " + tes.getName() + ", score=" + tes.getScore() + ", weight=" + pe.getWeight() );
            totalScore += tes.getScore() * pe.getWeight();
            
            totalWeight += pe.getWeight();
        }
        
        if( totalWeight <= 0 )
            return -1;

        // LogService.logIt( "TestEventAltScoreCalculator getScore() totalScore sum=" + totalScore + " totalWeightSum=" + totalWeight );

        
        totalScore = Math.round( totalScore / totalWeight );
        
        return totalScore;
    }
    
    @Override
    protected boolean getHasWeights()
    {
        ProfileEntry pe;
        
        for( TestEventScore tes : testEvent.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
        {
            pe = profile.getLiveProfileEntry( tes.getName(), tes.getNameEnglish(), true );
            
            if( pe == null )
                continue;
            
            if( pe.getWeight() >0 )
                return true;
        }
        
        return false;
    }
    
    
}
