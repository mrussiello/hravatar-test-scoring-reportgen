/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.findly;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ScoreFormatType;
import java.util.Locale;

/**
 *
 * 
<Scores>
<NumScores>1</NumScores>
<Score>
<UserInfo>
<eTicketID>464223281218661178</eTicketID>
<ScoreID>22785440</ScoreID>
<ExternalID/>
<FirstName>Jonathan</FirstName>
<LastName>Tundag</LastName>
<ID>ZLAd_W07gO4*</ID>
<AdditionalData>
<UserData/>
</AdditionalData>
</UserInfo>
<TestInfo>
<TestID>ENCCSCSA</TestID>
<TestName>Call Center Customer Service Simulation</TestName>
<Date>20160513</Date>
<DateAsNumber>20160513</DateAsNumber>
<TimeStart>08:41:00</TimeStart>
<TimeElapsed>00:50:00</TimeElapsed>
</TestInfo>
<ScoreInfo>
<ScoreType>SIMULATION</ScoreType>
<ReportURL>
https://webtest.skillcheck.com/onlinetesting/servlet/com.skillcheck.session_management.SK_Servlet?ID=HR-AVATAR-EVAL&MODE=REPORTRETRIEVAL,scoreKey.22785440,pdf
</ReportURL>
<NumMetrics>2</NumMetrics>
<Metric>
<MetricName>ScoreTotal</MetricName>
<MetricDesc>Overall Rating</MetricDesc>
<MetricValue>Moderate</MetricValue>
</Metric>
<Metric>
<MetricName>ScaleScores</MetricName>
<MetricDesc>Competencies and their scores</MetricDesc>
<MetricValue>
<NumScales>5</NumScales>
<Scale>
<Name>Customer Service</Name>
<Score>Moderate</Score>
</Scale>
<Scale>
<Name>Adherence</Name>
<Score>Moderate</Score>
</Scale>
<Scale>
<Name>Multi Tasking</Name>
<Score>Moderate</Score>
</Scale>
<Scale>
<Name>Keyboarding</Name>
<Score>High</Score>
</Scale>
<Scale>
<Name>Call Management</Name>
<Score>High</Score>
</Scale>
</MetricValue>
</Metric>
</ScoreInfo>
</Score>
</Scores> * 
 * 
 * 
<?xml version="1.0" encoding="UTF-8"?>
<Scores>
    <NumScores>1</NumScores>
    <Score>
        <UserInfo>
            <eTicketID>5066041546880503209</eTicketID>
            <ScoreID>23354617</ScoreID>
            <ExternalID />
            <FirstName>Cleve</FirstName>
            <LastName>Adams</LastName>
            <ID>FV6J1fPpNvI*</ID>
            <AdditionalData>
                <UserData />
            </AdditionalData>
        </UserInfo>
        <TestInfo>
            <TestID>ENTAMSPS</TestID>
            <TestName>Profile - Management Success</TestName>
            <Date>8/22/2016</Date>
            <DateAsNumber>20160822</DateAsNumber>
            <TimeStart>09:28</TimeStart>
            <TimeElapsed>4800</TimeElapsed>
        </TestInfo>
        <ScoreInfo>
            <ScoreType>IDENTITY</ScoreType>
            <NumMetrics>2</NumMetrics>
            <Metric>
                <MetricName>OverallScore</MetricName>
                <MetricDesc>Overall score (not a percentage)</MetricDesc>
                <MetricValue>79</MetricValue>
            </Metric>
            <Metric>
                <MetricName>ScaleScores</MetricName>
                <MetricDesc>Scale names and scores</MetricDesc>
                <MetricValue>
                    <NumScales>6</NumScales>
                    <Scale>
                        <Name>MSP Adapts and Leads in Change (S)</Name>
                        <Score>62</Score>
                    </Scale>
                    <Scale>
                        <Name>MSP Demonstrates Interpersonal Effectiveness (S)</Name>
                        <Score>95</Score>
                    </Scale>
                    <Scale>
                        <Name>MSP Demonstrates Performance Orientation/Drive (S)</Name>
                        <Score>26</Score>
                    </Scale>
                    <Scale>
                        <Name>MSP Influences Others (S)</Name>
                        <Score>66</Score>
                    </Scale>
                    <Scale>
                        <Name>MSP Manages Business Complexity (S)</Name>
                        <Score>84</Score>
                    </Scale>
                    <Scale>
                        <Name>MSP Manages People and Resources (S)</Name>
                        <Score>86</Score>
                    </Scale>
                </MetricValue>
            </Metric>
        </ScoreInfo>
    </Score>
</Scores> * 
 * 
 * 
 * @author Mike
 */
public interface FindlyScoreInfo {

    public void populateTestEventAndCreateTestEventScoreList( TestEvent te) throws Exception;

    public ScoreFormatType getScoreFormatType();



}
