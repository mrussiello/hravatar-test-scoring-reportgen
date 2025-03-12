/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2builder.sim.xml.InterviewQuestionObj;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.custom.coretest2.CT3Constants;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.score.TextAndTitle;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class SampleReportUtils {


    static String SAMPLE_ESSAY_CONTENT = "g.SampleEssayContentKey"; // "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas venenatis lobortis mi ut tincidunt. Nulla in sem eget metus aliquet feugiat vel eget odio. Fusce varius leo lectus, et ullamcorper est tempor et. Cras semper eleifend lacus in rhoncus. Integer ac mauris euismod, hendrerit nisi vitae, porttitor tortor. Integer ut leo sit amet nisl finibus auctor at quis massa. Nullam at erat in sem placerat consectetur nec a diam. Donec non lectus euismod, pulvinar elit nec, dapibus nulla. Phasellus a cursus quam, in pharetra nisi. Fusce porta rutrum turpis a varius. Proin dignissim vitae diam ac fermentum. Morbi neque quam, interdum lobortis neque ac, porttitor hendrerit neque. Vestibulum ut erat consequat, luctus nunc non, maximus justo. Phasellus vel lorem quam. Ut at accumsan arcu. Aliquam erat volutpat. Aliquam quis urna eget est bibendum interdum ultrices vitae diam. Praesent a augue eget elit posuere fermentum ut ut lorem. Morbi magna est, dignissim sit amet risus sed, efficitur ultrices nisl. Pellentesque dignissim enim quis sem rutrum, et condimentum libero mattis. Aliquam venenatis, risus nec hendrerit rhoncus, neque nisi euismod dolor, non dignissim justo lacus vel felis. Curabitur mauris quam, euismod vehicula convallis id, dictum a mauris. Praesent vehicula lectus libero. Morbi in feugiat massa. Donec et dapibus quam, sed feugiat nibh. Integer quam magna, pellentesque vulputate urna quis, ullamcorper scelerisque mi.";

    static String SAMPLE_ESSAY_TITLE = "g.SampleEssayTitleKey"; //  "This is the essay question.";
    
    static String SAMPLE_AUDIO_CONTENT = "g.SampleAudioContentKey";
    
    static String SAMPLE_AUDIO_TITLE = "g.SampleAudioTitleKey";

    static String[] BIODATA_PERF_CAVEATS = new String[] {"Below average productivity history", "Below average performance reviews"};
    static String[] BIODATA_TENURE_CAVEATS = new String[] {"Frequent job changes", "Potential long commute"};
    static String[] BIODATA_UNPROD_CAVEATS = new String[] {"History of frequent extra time off", "May not follow rules if doesn't agree with them"};

    static String[] BIODATA_PERF_INTERVIEW = new String[] {"How does your work compare with your peers? Do you produce more or less? How do you know?", "What kind of feedback have you received about your performance from your managers and your peers?"};
    static String[] BIODATA_TENURE_INTERVIEW = new String[] {"Review your last few jobs with me, explaining why you left the old job and what attracted you to the new one.", "What is the longest distance you have had to commute to work? What did you do during the commute? How long did you keep that job?"};
    static String[] BIODATA_UNPROD_INTERVIEW = new String[] {"From time to time we all need to take time off to deal with personal issues. Can you tell me about the kind of things you've needed time off to take care of during the past 12 months? ", "Not all rules make sense at all times. Tell me about a time when you were faced with a rule you didn't agree with. What did you do?"};



    public static String getScoreText( SimJ.Simcompetency simCompetencyObj, float scaledScore, ScoreColorSchemeType scst )
    {
        if( simCompetencyObj == null )
            return null;

        String t = "";


        if( simCompetencyObj.getHighcliffmin()> 0 && simCompetencyObj.getHighclifflevel()>0 && scaledScore >= simCompetencyObj.getHighcliffmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getHighclifftext() == null ? "" : simCompetencyObj.getHighclifftext() );

        else if( scst.getIsSevenColor() && scaledScore >= simCompetencyObj.getWhitemin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getWhitetext() == null ? "" : simCompetencyObj.getWhitetext() );

        else if( scaledScore >= simCompetencyObj.getGreenmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getGreentext() == null ? "" : simCompetencyObj.getGreentext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simCompetencyObj.getYellowgreenmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowgreentext() == null ? "" : simCompetencyObj.getYellowgreentext() );

        else if( scaledScore >= simCompetencyObj.getYellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowtext()==null ? "" : simCompetencyObj.getYellowtext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simCompetencyObj.getRedyellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedyellowtext() == null ? "" : simCompetencyObj.getRedyellowtext() );

        else if( scst.getIsSevenColor() && scaledScore >= simCompetencyObj.getRedmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedtext() == null ? "" : simCompetencyObj.getRedtext() );

        else if( scst.getIsSevenColor()  )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getBlacktext() == null ? "" : simCompetencyObj.getBlacktext() );

        else
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedtext() == null ? "" : simCompetencyObj.getRedtext() );

       return t;
    }



    public static List<String> getStandardCaveatList( Locale locale, String competencyName, int simCompetencyClassId )
    {
        List<String> out = new ArrayList<>();

        SimCompetencyClass scc = SimCompetencyClass.getValue(simCompetencyClassId);

        if( scc.equals( SimCompetencyClass.SCOREDTYPING ) )
        {
            out.add( MessageFactory.getStringMessage( locale , "g.WordPerMinX" , new String[]{ Integer.toString( Math.round( 60 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.WordPerMinAccAdjX" , new String[]{ Integer.toString( Math.round( 50 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.AccuracyX" , new String[]{ Integer.toString( Math.round( 85 ) )} ) );
        }

        else if( scc.equals( SimCompetencyClass.SCOREDDATAENTRY ) )
        {
            out.add( MessageFactory.getStringMessage( locale , "g.KeystrokesPerHourX" , new String[]{ Integer.toString( Math.round( 8200 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.GrossErrorsX" , new String[]{ Integer.toString( Math.round( 1 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.KeystrokesPerHourAccAdjX" , new String[]{ Integer.toString( Math.round( 7790 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.AccuracyX" , new String[]{ Integer.toString( Math.round( 95 ) )} ) );
        }

        else if( scc.equals( SimCompetencyClass.SCOREDESSAY ) )
        {
            out.add( MessageFactory.getStringMessage( locale , "g.EssayMachineScoreX" , new String[]{ Integer.toString( Math.round( 80 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.EssayMachineConfidenceX" , new String[]{ Integer.toString( Math.round( 75 ) )} ) );
            out.add( MessageFactory.getStringMessage( locale , "g.EssayWordCountX" , new String[]{ Integer.toString( Math.round( 247 ) )} ) );                
        }

        else if( scc.equals( SimCompetencyClass.SCOREDAUDIO ) )
        {
            // TODO  TO DO!!!
        }

        else if( scc.equals( SimCompetencyClass.SCOREDAVUPLOAD ) )
        {
            // TODO  TO DO!!!
        }

        else if( scc.equals( SimCompetencyClass.SCOREDBIODATA ) )
        {
            String[] vs = null;


            if( competencyName.contains( "Performance" ) )
                vs = BIODATA_PERF_CAVEATS ;

            if( competencyName.contains( "Tenure" ) )
                vs = BIODATA_TENURE_CAVEATS;

            if( competencyName.contains( "Unproductive" ) )
                vs = BIODATA_UNPROD_CAVEATS;

            if( vs != null )
            {
                for( String v : vs )
                {
                    out.add( v );
                }
            }
        }

        return out;
    }


    public static int getScoreCategoryTypeId( SimJ.Simcompetency sjc, float score, ScoreColorSchemeType scst )
    {
        ScoreCategoryType scoreCat = ScoreCategoryType.getForScore(scst,
                                                                    score,
                                                                    sjc.getHighcliffmin(), 
                                                                    sjc.getWhitemin(),
                                                                    sjc.getGreenmin(),
                                                                    sjc.getYellowgreenmin(),
                                                                    sjc.getYellowmin(),
                                                                    sjc.getRedyellowmin(), 
                                                                    sjc.getRedmin(),
                                                                    0,
                                                                    sjc.getCategorydisttype(),
                                                                    sjc.getHighclifflevel() );

        if( sjc.getCategoryadjustmentthreshold()>0 && score <= sjc.getCategoryadjustmentthreshold() )
            scoreCat = scoreCat.adjustOneLevelUp( scst );

        return scoreCat.getScoreCategoryTypeId();
    }




    public static String packGeneralNoncompetencyResponses( Locale locale, boolean hasWriting, boolean hasAudio, boolean hasAimsCorpCit, boolean hasAimsIntegrity, boolean includeRiskFactors)
    {
        StringBuilder sb = new StringBuilder();

        List<TextAndTitle> ttl;

        if( hasWriting )
        {
            ttl = new ArrayList<>();

            ttl.add( new TextAndTitle( MessageFactory.getStringMessage(locale, SAMPLE_ESSAY_CONTENT), MessageFactory.getStringMessage(locale, SAMPLE_ESSAY_TITLE)  ) );

            sb.append( packResponses( ttl, NonCompetencyItemType.WRITING_SAMPLE.getTitle() ) );
        }

        if( hasAudio )
        {
            ttl = new ArrayList<>();

            ttl.add(new TextAndTitle( MessageFactory.getStringMessage(locale, SAMPLE_AUDIO_CONTENT), MessageFactory.getStringMessage(locale, SAMPLE_AUDIO_TITLE), RuntimeConstants.getLongValue( "sampleAudioIvrItemResponseId" ), 0  ) );

            sb.append( packResponses( ttl, NonCompetencyItemType.SPEAKING_SAMPLE.getTitle() ) );
        }

        if( includeRiskFactors )
        {
            //Add Sample risk factor
            ttl = new ArrayList<>();

            if( hasAimsCorpCit )
                ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_LowCitizenship" ),"" ));
            if( hasAimsIntegrity )
                ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_LowIntegrity" ),"" ));

            //ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_AIMSFaking" ),"" ));
            //ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_AIMSFaking" ),"" ));
            //ttl.add( new TextAndTitle( MessageFactory.getStringMessage( locale, "g.CT3Risk_AIMSFaking" ),"" ));


            sb.append( packResponses( ttl, CT3Constants.CT3RISKFACTORS ) );
        }
        
        return sb.toString();
    }




    public static String packInterviewQuestions( SimJ.Simcompetency sjc )
    {
        SimCompetencyClass scc = SimCompetencyClass.getValue(sjc.getClassid());

        List<InterviewQuestion> iql = new ArrayList<>();

        InterviewQuestion iqq;

        if( scc.equals( SimCompetencyClass.SCOREDBIODATA ) )
        {
            String[] vs = null;

            if( sjc.getName().contains( "Performance" ) )
                vs = BIODATA_PERF_INTERVIEW ;

            if( sjc.getName().contains( "Tenure" ) )
                vs = BIODATA_TENURE_INTERVIEW;

            if( sjc.getName().contains( "Unproductive" ) )
                vs = BIODATA_UNPROD_INTERVIEW;

            if( vs != null )
            {
                for( String v : vs )
                {
                    iqq = new InterviewQuestion( v, "", "", "" );

                    iql.add( iqq );
                }
            }
        }

        else
        {
            for( InterviewQuestionObj iqo : sjc.getInterviewquestion() )
            {
                iqq = new InterviewQuestion( sjc );

                iqq.load( iqo );

                iql.add( iqq );

                if( iql.size() > 1 )
                    break;
            }
        }


        // LogService.logIt( "ScoreManagerBean.packInterviewQuestions() list size=" + iql.size() );

        StringBuilder sb = new StringBuilder();

        for( InterviewQuestion iq : iql )
        {
            if( sb.length()>0 )
                sb.append( Constants.DELIMITER );

            sb.append( iq.getQuestion() + Constants.DELIMITER +  iq.getAnchorHi() + Constants.DELIMITER + iq.getAnchorMed() + Constants.DELIMITER + iq.getAnchorLow() + Constants.DELIMITER + iq.getScoreBreadth() );
            // sb.append( XMLUtils.encodeURIComponent( iq.getQuestion() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorHi() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorMed() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( iq.getAnchorLow() )  );

        }

        return sb.toString();
    }




    protected static String packResponses( List<TextAndTitle> ttl, String title )
    {
        // LogService.logIt( "ScoreManagerBean.packResponses() AAA " + irl.size() + " title=" + title );

        if( ttl == null || ttl.isEmpty() )
            return "";

        StringBuilder sb = new StringBuilder();

        String tmp;


        tmp = packTextBasedResponses( ttl );

        if( tmp != null && !tmp.isEmpty() )
        {
            if( sb.length() > 0 )
                sb.append( Constants.DELIMITER );

            sb.append( tmp );
        }

        // LogService.logIt( "BaseTestEventScorer.packResponses() " + ";;;" + title+ ";;;" + Constants.DELIMITER + sb.toString() );

        if( sb.length() > 0 )
            return ";;;" + title+ ";;;" + Constants.DELIMITER + sb.toString();

        return "";
    }


    protected static String packTextBasedResponses( List<TextAndTitle> ttl )
    {
        StringBuilder sb = new StringBuilder();

        for( TextAndTitle tt : ttl )
        {
            if( sb.length()>0 )
                sb.append( Constants.DELIMITER );

            sb.append( tt.getTitle() + Constants.DELIMITER + tt.getText() + Constants.DELIMITER + tt.getFlags() + Constants.DELIMITER + (tt.getString1()==null || tt.getString1().isBlank() ? "" : tt.getString1() ) + "~" + (tt.getString2()==null || tt.getString2().isBlank() ? "" : tt.getString2() ) + "~" + (tt.getString3()==null || tt.getString3().isBlank() ? "" : tt.getString3() ) );
            // sb.append( XMLUtils.encodeURIComponent( tt.getTitle() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( tt.getText() ) + Constants.DELIMITER + XMLUtils.encodeURIComponent( tt.getFlags() ) );
        }

        return sb.toString();
    }



}
