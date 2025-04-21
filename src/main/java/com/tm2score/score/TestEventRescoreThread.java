/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score;

import com.tm2score.entity.user.User;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventStatusType;
import com.tm2score.service.EmailUtils;
import com.tm2score.service.LogService;
import com.tm2score.user.UserUtils;
import static jakarta.ws.rs.SeBootstrap.start;
import java.util.Date;
import java.util.List;
import static org.primefaces.component.timeline.TimelineBase.PropertyKeys.end;

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

    List<Long[]> simIdVersionIdPairs;
    Date startDate;
    Date endDate;
    int orgId;
    int suborgId;

    boolean survey;
    User user;

    EventFacade eventFacade;


    public TestEventRescoreThread( User initiatingUser, List<Long[]> simIdVersionIdPairs, int orgId, int suborgId, Date start, Date end, String simDescripXml, UserUtils uu, String description, boolean percentilesOnly, boolean clearExternal, boolean skipVersionCheck, boolean resetSpeechText, boolean survey)
    {
        this.user=initiatingUser;
        this.simIdVersionIdPairs = simIdVersionIdPairs;
        this.orgId = orgId;
        this.suborgId = suborgId;
        this.startDate = start;
        this.endDate = end;
        this.simDescripXml = simDescripXml;
        this.userUtils = uu;
        this.description = description;
        this.percentilesOnly = percentilesOnly;
        this.clearExternal = clearExternal;
        this.skipVersionCheck = skipVersionCheck;
        this.resetSpeechText=resetSpeechText;
        this.survey=survey;
    }


    public TestEventRescoreThread( User initiatingUser, List<Long> tel, UserUtils uu, String description, String simDescripXml, boolean percentilesOnly, boolean clearExternal, boolean skipVersionCheck, boolean resetSpeechText, boolean survey)
    {
        this.user=initiatingUser;
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
        StringBuilder sb = new StringBuilder();
        try
        {
            if( simIdVersionIdPairs==null || simIdVersionIdPairs.isEmpty() )
            {
                LogService.logIt( "TestEventRescoreThread.run() STARTING testEventIdList.size=" + testEventIdList.size() + ", " + description );
                userUtils.rescoreTestOrSurveyEvents(testEventIdList, simDescripXml, true, percentilesOnly, clearExternal, skipVersionCheck, resetSpeechText, survey );
                LogService.logIt( "TestEventRescoreThread.run() COMPLETED testEventIdList.size=" + testEventIdList.size() + ", " + description );
                if( user!=null )
                {
                    EmailUtils emu = new EmailUtils();
                    emu.sendEmailToAdmin( "TestEvent Rescore Process COMPLETE", "TestEventRescoreThread.run() COMPLETED testEventIdList.size=" + testEventIdList.size() + ", " + description );
                }

                return;
            }

            LogService.logIt( "TestEventRescoreThread.run() STARTING simIdVersionIdPairs.size=" + simIdVersionIdPairs.size() + ", " + description );
            int count = 0;
            String msg;


            if( eventFacade==null )
                eventFacade=EventFacade.getInstance();

            for( Long[] simIdPair : simIdVersionIdPairs )
            {
                testEventIdList = eventFacade.getTestEventIdsForSimIdAndOrOrg(simIdPair[0], simIdPair[1].intValue(), orgId, suborgId, 0, percentilesOnly ? TestEventStatusType.SCORED.getTestEventStatusTypeId() : TestEventStatusType.COMPLETED.getTestEventStatusTypeId(), TestEventStatusType.REPORT_COMPLETE.getTestEventStatusTypeId(), new int[]{TestEventStatusType.SCORE_ERROR.getTestEventStatusTypeId(), TestEventStatusType.REPORT_ERROR.getTestEventStatusTypeId()}, startDate, endDate, 0, 0 );

                if( testEventIdList.isEmpty() )
                    continue;

                LogService.logIt( "TestEventRescoreThread.run() Starting for simId=" + simIdPair[0] + ", version " + simIdPair[1] +", events found=" + testEventIdList.size() );
                userUtils.rescoreTestOrSurveyEvents(testEventIdList, simDescripXml, true, percentilesOnly, clearExternal, skipVersionCheck, resetSpeechText, survey );

                count+=testEventIdList.size();
                msg = "Finished scoring simId=" + simIdPair[0] + ", version " + simIdPair[1] + ", Total of " + testEventIdList.size() + " events. Total events score for all sims=" + count;
                LogService.logIt( "TestEventRescoreThread.run() " + msg );
                sb.append( msg + "\n" );            
            }

            LogService.logIt( "TestEventRescoreThread.run() COMPLETED All " +simIdVersionIdPairs.size() + " SimIdVersionId pairs. Total rescored=" + count + ", " + description  + "\n\n" + sb.toString());
        }

        catch( ScoringException e )
        {
            LogService.logIt( "TestEventRescoreThread.run() STERR " + e.toString() + ", testEventIdList.size=" + testEventIdList.size() + ", " + description   + "\n\n" + sb.toString());
            if( user!=null )
            {
                EmailUtils emu = new EmailUtils();
                emu.sendEmailToAdmin( "TestEvent Rescore Process FAILED", "TestEventRescoreThread.run() FAILED STERR " + e.toString() + ", testEventIdList.size=" + testEventIdList.size() + ", " + description  + "\n\n" + sb.toString() );
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestEventRescoreThread.run() testEventIdList.size=" + testEventIdList.size() + ", " + description   + "\n\n" + sb.toString());
            if( user!=null )
            {
                EmailUtils emu = new EmailUtils();
                emu.sendEmailToAdmin( "TestEvent Rescore Process FAILED", "TestEventRescoreThread.run() FAILED " + e.toString() + ", testEventIdList.size=" + testEventIdList.size() + ", " + description  + "\n\n" + sb.toString() );
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }



}
