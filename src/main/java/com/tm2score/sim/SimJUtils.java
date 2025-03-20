/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.sim;

import com.tm2builder.sim.xml.InterviewQuestionObj;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class SimJUtils {
    
    private SimJ simJ;
    
    public SimJUtils( SimJ simJ )
    {
        this.simJ = simJ;
        
        // LogService.logIt( "SimJUtils() loading SimJ=" + simJ.getName() + " (SimId=" + simJ.getSimid() + ", imoId="  + simJ.getImonum() + ") " + simJ.getNameenglish()  );
    }

    public SimJ getSimJ() {
        return simJ;
    }

    public static boolean getHasAnyNormativeScoring( SimJ simJx )
    {
        if( simJx==null )
            return false;

        OverallRawScoreCalcType orsct = OverallRawScoreCalcType.getValue( simJx.getOverallscorecalctype() );
        OverallScaledScoreCalcType ossct = OverallScaledScoreCalcType.getValue( simJx.getOverallscaledscorecalctype() );
        
        SimCompetencyRawScoreCalcType scrsct;
        SimCompetencyScaledScoreCalcType scssct;
        SimCompetencyClass scc;
        ScoreFormatType sft = ScoreFormatType.getValue( simJx.getScoreformat() );
        
        
        // If uses HRA Standard Scoring or just 0-100 scale, then we need to make sure that it either has any normative scored non-KS competencies or it has 
        if( simJx.getUsesstdhrascoring()==1 || sft.equals( ScoreFormatType.NUMERIC_0_TO_100 ) )
        {
            // if it uses normalized overall scores but doesn't use standard raw and mean (to simulate percent correct) then yes
            if( orsct.getRawNormalized() )
            {
                // if overall raw mean/sd is not 0 or 1
                if( simJx.getMean()!=0f || simJx.getStddeviation()!=1f )
                    return true;                
            }
            
            if( ossct.getIsNCE() || ossct.getIsTransform() )
            {
                // if overall scaled mean/sd not 65 / 15
                if( simJx.getRawtoscaledfloatparam1()!=65f || simJx.getRawtoscaledfloatparam2()!=15f )
                    return true;                
            }
            
            // Now check competencies. Look for transformed competencies that are either not KS or are do not use standard mean/sd to simulate percent correct.
            for( SimJ.Simcompetency sc : simJx.getSimcompetency() )
            {
                scc = SimCompetencyClass.getValue( sc.getClassid() );
                scrsct = SimCompetencyRawScoreCalcType.getValue( sc.getRawscorecalctypeid() );
                
                if( scrsct.getIsZScore() )
                {
                    // non KS - yes!
                    if( !scc.isKS() )
                        return true;
                    
                    if( sc.getMean()!=0.65f || sc.getStddeviation()!=0.15f )
                        return true;
                }

                scssct = SimCompetencyScaledScoreCalcType.getValue( sc.getScaledscorecalctypeid() );
                
                if( scssct.getIsTransform() )
                {
                    // non KS - yes!
                    if( !scc.isKS() )
                        return true;
                    
                    if( sc.getScaledmean()!=65f || sc.getScaledstddeviation()!=15f )
                        return true;
                }
            }
            
            return false;
        }
        
        else
        {
            if( orsct.getRawNormalized() || ossct.getIsTransform() || ossct.getIsNCE() )
                return true;
            
            // Now check competencies. Look for transformed competencies that are either not KS or are do not use standard mean/sd to simulate percent correct.
            for( SimJ.Simcompetency sc : simJx.getSimcompetency() )
            {
                scrsct = SimCompetencyRawScoreCalcType.getValue( sc.getRawscorecalctypeid() );                
                if( scrsct.getIsZScore() )
                    return true;

                scssct = SimCompetencyScaledScoreCalcType.getValue( sc.getScaledscorecalctypeid() );                
                if( scssct.getIsTransform() )
                    return true;
            }
            
            return false;            
        }
    }
    
    
    public String getReportOverviewText()
    {
        return simJ.getReportoverviewtext();
    }
    
    
    public  List<InterviewQuestion> getInterviewQuestions( TestEventScore tes, int max )
    {
        List<InterviewQuestionObj> iql = getInterviewQuestions( tes.getName(), tes.getNameEnglish() );
        
        if( iql==null || iql.isEmpty() )
            return tes.getInterviewQuestionList(max);
        
        Collections.shuffle(iql);
        
        List<InterviewQuestion> out = new ArrayList<>();
        
        for( InterviewQuestionObj iqo : iql )
        {
            out.add( new InterviewQuestion(iqo) );
            
            if( out.size()==max )
                break;
        }
        
        return out;
    }

    
    
    public  List<InterviewQuestionObj> getInterviewQuestions( String name, String nameEnglish )
    {
        if( name==null || name.isEmpty() )
            return new ArrayList<>();
        
        SimJ.Simcompetency sc = getSimCompetencyForName( name, nameEnglish );
        
        if( sc == null )
            return new ArrayList<>();
        
        return sc.getInterviewquestion();
    }

    
    public String getOverallScoreText( int category, float score )
    {
        ScoreCategoryType sct = ScoreCategoryType.getValue(category);
        
        if( sct == null )
            return null;
        
        if( sct.equals( ScoreCategoryType.WHITE ) )
            return UrlEncodingUtils.decodeKeepPlus(simJ.getWhitetext() );
        if( sct.equals( ScoreCategoryType.GREEN ) )
            return UrlEncodingUtils.decodeKeepPlus(simJ.getGreentext() );
        if( sct.equals( ScoreCategoryType.YELLOWGREEN ) )
            return UrlEncodingUtils.decodeKeepPlus(simJ.getYellowgreentext() );
        if( sct.equals( ScoreCategoryType.YELLOW ) )
            return UrlEncodingUtils.decodeKeepPlus(simJ.getYellowtext() );
        if( sct.equals( ScoreCategoryType.REDYELLOW ) )
            return UrlEncodingUtils.decodeKeepPlus(simJ.getRedyellowtext() );
        if( sct.equals( ScoreCategoryType.RED ) )
            return UrlEncodingUtils.decodeKeepPlus(simJ.getRedtext() );
        if( sct.equals( ScoreCategoryType.BLACK ) )
            return UrlEncodingUtils.decodeKeepPlus(simJ.getBlacktext() );
        
        return null;        
    }

    public String getCompetencyName( TestEventScore tes )
    {
        return getCompetencyName( tes.getName(), tes.getNameEnglish() );
    }
    
    public String getCompetencyDescription( TestEventScore tes )
    {
        return getCompetencyDescription( tes.getName(), tes.getNameEnglish() );
    }
    
 
    public String getCompetencyScoreText( TestEventScore tes )
    {
        return getCompetencyScoreText( tes.getName(), tes.getNameEnglish(), tes.getScoreCategoryId(), tes.getScore() );
    }
    
    
    public String getCompetencyName( String name, String nameEnglish )
    {
        SimJ.Simcompetency sc = getSimCompetencyForName( name, nameEnglish );
        
        // LogService.logIt( "SimJUtils.getCompetencyName() name=" + name + ", nameEnglish=" + nameEnglish + " SimCompetency found=" + ( sc==null ? "null" : sc.getName() + "(" + sc.getNameenglish() + ")" ) );
        
        if( sc == null )
            return null;
        
        return UrlEncodingUtils.decodeKeepPlus( sc.getName() );
    }
    
    public String getCompetencyDescription( String name, String nameEnglish )
    {
        SimJ.Simcompetency sc = getSimCompetencyForName( name, nameEnglish );
        
        if( sc == null )
            return null;
        
        return UrlEncodingUtils.decodeKeepPlus( sc.getDescrip() );
    }
    
    
    public String getCompetencyScoreText( String name, String nameEnglish, int category, float score )
    {
        SimJ.Simcompetency sc = getSimCompetencyForName( name, nameEnglish );
        
        if( sc == null )
            return null;
        
        ScoreCategoryType sct = ScoreCategoryType.getValue(category);
        
        if( sct == null )
            return null;
        
        if( sct.equals( ScoreCategoryType.WHITE ) )
            return UrlEncodingUtils.decodeKeepPlus(sc.getWhitetext());
        if( sct.equals( ScoreCategoryType.GREEN ) )
            return UrlEncodingUtils.decodeKeepPlus(sc.getGreentext());
        if( sct.equals( ScoreCategoryType.YELLOWGREEN ) )
            return UrlEncodingUtils.decodeKeepPlus(sc.getYellowgreentext());
        if( sct.equals( ScoreCategoryType.YELLOW ) )
            return UrlEncodingUtils.decodeKeepPlus(sc.getYellowtext());
        if( sct.equals( ScoreCategoryType.REDYELLOW ) )
            return UrlEncodingUtils.decodeKeepPlus(sc.getRedyellowtext());
        if( sct.equals( ScoreCategoryType.RED ) )
            return UrlEncodingUtils.decodeKeepPlus(sc.getRedtext());
        if( sct.equals( ScoreCategoryType.BLACK ) )
            return UrlEncodingUtils.decodeKeepPlus(sc.getBlacktext());
        
        return null;
    }
    
    public SimJ.Simcompetency getSimCompetencyForName( String name, String nameEnglish )
    {
        if( ( name==null || name.isEmpty() ) && ( nameEnglish==null || nameEnglish.isEmpty() ) )
            return null;
        
        for( SimJ.Simcompetency sc : simJ.getSimcompetency() )
        {
            if( StringUtils.isValidNameMatch( UrlEncodingUtils.decodeKeepPlus(sc.getName()), UrlEncodingUtils.decodeKeepPlus(sc.getNameenglish()), name, nameEnglish ))
                return sc;
            
            //if( sc.getName()!=null && sc.getName().equals(name) )
            //    return sc;

            //if( sc.getNameenglish()!=null && sc.getNameenglish().equals(name) )
            //    return sc;

            //if( sc.getName()!=null && sc.getName().equals(nameEnglish) )
            //    return sc;

            //if( sc.getNameenglish()!=null && sc.getNameenglish().equals(nameEnglish) )
            //    return sc;
        }
        
        return null;
    }
    
}
