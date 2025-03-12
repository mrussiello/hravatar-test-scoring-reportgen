package com.tm2score.sim;

import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.interview.InterviewQuestion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/*
 * Defines the breadth of scores for which this interview question is meant to apply.
 *
 *
 * @author Mike
 */
public enum InterviewQuestionBreadthType
{
    // DEFAULT
    ALL(0,"All Scores" ),
    LOW_RED(5,"Low Red Scores" ),
    LOW(10,"All Low Scores" ),
    MEDIUM(15,"Medium Scores" ),
    HIGH(20,"All High Scores" ),
    HIGH_RED(25,"High Red Scores" );



    private final int interviewQuestionBreadthTypeId;

    private final String key;


    private InterviewQuestionBreadthType( int s , String n )
    {
        this.interviewQuestionBreadthTypeId = s;

        this.key = n;
    }


    public static List<InterviewQuestion> getInterviewQuestionForScoreAndCategory(  ScoreCategoryType sct,
                                                                                    ScoreFormatType sft,
                                                                                    float score,
                                                                                    List<InterviewQuestion> icl,
                                                                                    int minToReturn )
    {
        if( icl.size()<= minToReturn )
            return icl;

        boolean isLowRed = sft.getIsLow( score ) && (sct.red() || sct.redYellow() );
        boolean isLow = sft.getIsLow( score );
        boolean isHigh = sft.getIsHigh( score );
        boolean isMedium = !isLow && !isHigh;
        boolean isHighRed = sft.getIsHigh( score ) && (sct.red() || sct.redYellow() );

        List<InterviewQuestion> ol = new ArrayList<>();

        for( InterviewQuestion iq : icl )
        {
            iq.setBoolean1(false);
        }

        // First, get direct matches.
        for( InterviewQuestion iq : icl )
        {
            if( iq.getInterviewQuestionBreadthType().equals( LOW_RED ) && isLowRed )
            {
                ol.add( iq );
                iq.setBoolean1(true);
            }
            else if( iq.getInterviewQuestionBreadthType().equals( LOW ) && isLow )
            {
                ol.add( iq );
                iq.setBoolean1(true);
            }
            else if( iq.getInterviewQuestionBreadthType().equals( MEDIUM ) && isMedium )
            {
                ol.add( iq );
                iq.setBoolean1(true);
            }
            else if( iq.getInterviewQuestionBreadthType().equals( HIGH ) && isHigh )
            {
                ol.add( iq );
                iq.setBoolean1(true);
            }
            else if( iq.getInterviewQuestionBreadthType().equals( HIGH_RED ) && isHighRed )
            {
                ol.add( iq );
                iq.setBoolean1(true);
            }

            
            if( ol.size() >= minToReturn )
            {
                // Shuffle these if more than 1 
                if( ol.size()>1 )
                    Collections.shuffle( ol );
                
                return ol;
            }
        }

        // Shuffle these if more than 1 
        if( !ol.isEmpty() && ol.size()>1 )
            Collections.shuffle( ol );
                
        // Next, add alls. Keep these at the end.
        for( InterviewQuestion iq : icl )
        {
            if( iq.getInterviewQuestionBreadthType().equals( ALL ) )
            {
                ol.add( iq );
                iq.setBoolean1(true);
            }

            if( ol.size() >= minToReturn )
                return ol;
        }

        for( InterviewQuestion iq : icl )
        {
            if( iq.getBoolean1() )
                continue;

            ol.add( iq );
            iq.setBoolean1(true);

            if( ol.size() >= minToReturn )
                return ol;
        }


        return ol;
    }


    public static InterviewQuestionBreadthType getValue( int id )
    {
        InterviewQuestionBreadthType[] vals = InterviewQuestionBreadthType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getInterviewQuestionsBreadthTypeId() == id )
                return vals[i];
        }

        return ALL;
    }

    public String getName()
    {
        return key;
    }


    public int getInterviewQuestionsBreadthTypeId()
    {
        return interviewQuestionBreadthTypeId;
    }

    public String getKey()
    {
        return key;
    }

}
