/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score;

import com.tm2score.service.LogService;
import com.tm2score.user.UserUtils;
import java.util.List;

/**
 *
 * @author Mike
 */
public class TestEventRescoreThread implements Runnable
{
    List<Long> testEventIdList;

    UserUtils userUtils;

    String simDescripXml;

    String description;

    boolean percentilesOnly = false;

    boolean clearExternal = false;
    
    boolean skipVersionCheck = false;
    boolean resetSpeechText = false;
    
    boolean survey;


    public TestEventRescoreThread( List<Long> tel, UserUtils uu, String description, String simDescripXml, boolean percentilesOnly, boolean clearExternal, boolean skipVersionCheck, boolean resetSpeechText, boolean survey)
    {
        this.testEventIdList = tel;
        this.userUtils = uu;
        this.description = description;
        this.simDescripXml = simDescripXml;
        this.percentilesOnly = percentilesOnly;
        this.clearExternal = clearExternal;
        this.skipVersionCheck = skipVersionCheck;
        this.resetSpeechText=resetSpeechText;
        this.survey=survey;
    }

    @Override
    public void run()
    {
        try
        {
            LogService.logIt( "TestEventRescoreThread.run() STARTING testEventIdList.size=" + testEventIdList.size() + ", " + description );
            userUtils.rescoreTestOrSurveyEvents(testEventIdList, simDescripXml, true, percentilesOnly, clearExternal, skipVersionCheck, resetSpeechText, survey );
            LogService.logIt( "TestEventRescoreThread.run() COMPLETED testEventIdList.size=" + testEventIdList.size() + ", " + description );
        }
        
        catch( ScoringException e )
        {
            LogService.logIt( "TestEventRescoreThread.run() STERR " + e.toString() + ", testEventIdList.size=" + testEventIdList.size() + ", " + description );
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestEventRescoreThread.run() testEventIdList.size=" + testEventIdList.size() + ", " + description );
        }
    }



}
