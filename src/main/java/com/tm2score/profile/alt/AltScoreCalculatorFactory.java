/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.profile.alt;

import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.profile.Profile;
import com.tm2score.profile.ProfileUsageType;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class AltScoreCalculatorFactory {
    
    public static AltScoreCalculator getAltScoreCalculator( BatteryScore batteryScore, Battery battery, List<TestEvent> tel, TestEvent testEvent, Profile p ) throws Exception
    {
        //if( p.getProfileUsageType().equals( ProfileUsageType.ALTERNATE_BATTERY_COMPETENCY_WEIGHTS) || p.getProfileUsageType().equals( ProfileUsageType.ALTERNATE_BATTERY_OVERALL_WEIGHTS) )
        //    return new TestBatteryAltScoreCalculator( batteryScore, battery, tel, p );
        
        return new TestEventAltScoreCalculator( testEvent, p );        
    }


    
}
