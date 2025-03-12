/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.interview;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.global.Constants;
import com.tm2builder.sim.xml.InterviewQuestionObj;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.service.LogService;
import com.tm2score.sim.InterviewQuestionBreadthType;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;

/**
 *
 * @author Mike
 */
public class InterviewQuestion
{
    private SimJ.Simcompetency simCompetencyObj = null;

    private String question;

    private String anchorLow;

    private String anchorMed;

    private String anchorHi;

    private int scoreBreadth;

    private TestEventScore testEventScore;

    private boolean boolean1=false;

    public InterviewQuestion()
    {
    }

    public InterviewQuestion( String q, String l, String m, String h )
    {
        this.question = q;
        this.anchorLow = l;
        this.anchorMed = m;
        this.anchorHi = h;
    }


    public InterviewQuestion( InterviewQuestionObj iqo )
    {
        try
        {
            question = UrlEncodingUtils.decodeKeepPlus( iqo.getQuestion(), "UTF8" );

            for( InterviewQuestionObj.Anchor anc : iqo.getAnchor() )
            {
                if( anc.getType()==1)
                    anchorLow=UrlEncodingUtils.decodeKeepPlus( anc.getValue(), "UTF8" );
                if( anc.getType()==5)
                    anchorMed=UrlEncodingUtils.decodeKeepPlus( anc.getValue(), "UTF8" );
                if( anc.getType()==10)
                    anchorHi=UrlEncodingUtils.decodeKeepPlus( anc.getValue(), "UTF8" );
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "InterviewQuestion() Constructor Error " + iqo.getQuestion() );
        }
    }



    public InterviewQuestion( SimJ.Simcompetency sc )
    {
        this.simCompetencyObj = sc;
    }

    public InterviewQuestionBreadthType getInterviewQuestionBreadthType()
    {
        return InterviewQuestionBreadthType.getValue(scoreBreadth);
    }

    public static InterviewQuestion getFromScoreText( String txt )
    {
        if( txt == null || txt.isEmpty() )
            return null;

        String q = StringUtils.getBracketedArtifactFromString(txt , Constants.INTERVIEWKEY );

        if( q == null || q.isEmpty() )
            return null;

        String l = StringUtils.getBracketedArtifactFromString(txt , Constants.ANCHORLOWKEY );
        if( l == null )
            l = "";

        String m = StringUtils.getBracketedArtifactFromString(txt , Constants.ANCHORMEDKEY );
        if( m == null )
            m = "";

        String h = StringUtils.getBracketedArtifactFromString(txt , Constants.ANCHORHIKEY );
        if( h == null )
            h = "";

        return new InterviewQuestion( q, l, m, h );
    }



    public boolean isCompetency()
    {
        return this.simCompetencyObj != null;
    }

    public long getSimCompetencyId()
    {
        if( simCompetencyObj == null )
            return 0;

        return simCompetencyObj.getId();
    }

    public boolean hasAnchors()
    {
        return ( anchorHi != null && !anchorHi.isEmpty() ) ||
                ( anchorMed != null && !anchorMed.isEmpty() ) ||
                ( anchorLow != null && !anchorLow.isEmpty() );
    }

    public void load( InterviewQuestionObj iqo )
    {
        this.question = UrlEncodingUtils.decodeKeepPlus( iqo.getQuestion() );

        this.scoreBreadth = iqo.getBreadth();

        int typ;

        for( InterviewQuestionObj.Anchor a : iqo.getAnchor() )
        {
            typ = a.getType();

            if( typ <= 0 )
                continue;

            if( typ == 1 )
                anchorLow = UrlEncodingUtils.decodeKeepPlus( a.getValue() );

            else if( typ == 5 )
                anchorMed = UrlEncodingUtils.decodeKeepPlus( a.getValue() );

            else if( typ == 10 )
                anchorHi = UrlEncodingUtils.decodeKeepPlus( a.getValue() );
        }
    }

    public String getAnchorHi() {
        return anchorHi == null ? "" : anchorHi;
    }

    public String getAnchorLow() {
        return anchorLow == null ? "" : anchorLow;
    }

    public String getAnchorMed() {
        return anchorMed == null ? "" : anchorMed;
    }

    public String getQuestion() {
        return question == null ? "" : question;
    }

    public void setAnchorMed(String anchorMed) {
        this.anchorMed = anchorMed;
    }

    public void setAnchorHi(String anchorHi) {
        this.anchorHi = anchorHi;
    }

    public void setAnchorLow(String anchorLow) {
        this.anchorLow = anchorLow;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public TestEventScore getTestEventScore() {
        return testEventScore;
    }

    public void setTestEventScore(TestEventScore testEventScore) {
        this.testEventScore = testEventScore;
    }

    public int getScoreBreadth() {
        return scoreBreadth;
    }

    public void setScoreBreadth(int scoreBreadth) {
        this.scoreBreadth = scoreBreadth;
    }

    public boolean getBoolean1() {
        return boolean1;
    }

    public void setBoolean1(boolean boolean1) {
        this.boolean1 = boolean1;
    }


}
