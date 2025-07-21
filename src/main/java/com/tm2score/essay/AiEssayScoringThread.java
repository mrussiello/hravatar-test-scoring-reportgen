/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.essay;

import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.service.LogService;

/**
 *
 * @author miker
 */
public class AiEssayScoringThread implements Runnable
{
    UnscoredEssay unscoredEssay;
    boolean forceRescore;
    boolean autoUpdate;
    boolean omitGrammarMech;
    String forcePromptStr;
    String idealResponseStr;
    String aiInstructionsStr;
    
    /*
      0 = score, no summary
      1 = score AND summary
      2 = summary ONLY
    */
    int summaryCode;
    
    
    public AiEssayScoringThread( UnscoredEssay ue, boolean autoUpdate, boolean omitGrammarMech, boolean forceRescore, String forcePromptStr, String idealResponseStr, String aiInstructionsStr, int summaryCode)
    {
        this.unscoredEssay=ue;
        this.omitGrammarMech=omitGrammarMech;
        this.forceRescore=forceRescore;
        this.autoUpdate=autoUpdate;
        this.forcePromptStr=forcePromptStr;
        this.idealResponseStr=idealResponseStr;
        this.aiInstructionsStr=aiInstructionsStr;
        this.summaryCode = summaryCode;
    }

    
    @Override
    public void run()
    {
        try
        {
            performEssayScore();
        }
        catch( Exception e )
        {
            LogService.logIt(e,"AiEssayScoringThread.run() unscoredEssayId=" + (unscoredEssay==null ? "null" : unscoredEssay.getUnscoredEssayId()) );
        }
    }
    
    
    public boolean performEssayScore() throws Exception
    {
        try
        {
            // Need a summary.
            if( summaryCode==1 || summaryCode==2 )
            {
                boolean resultOk = unscoredEssay.getSummary()!=null && !unscoredEssay.getSummary().isBlank();
                
                if( !resultOk || forceRescore )
                    resultOk = AiEssayScoringUtils.computeAiEssaySummary(unscoredEssay, autoUpdate, forceRescore );
                
                // only need Summary.
                if( summaryCode==2 )
                    return resultOk;
                
                if( !resultOk )
                    LogService.logIt( "AiEssayScoringThread.performEssayScore() Summary call was NOT OK.");
            }

            return AiEssayScoringUtils.computeAiEssayScore(unscoredEssay, autoUpdate, forceRescore, omitGrammarMech, forcePromptStr, idealResponseStr, aiInstructionsStr);
        }
        catch(Exception e )
        {
            LogService.logIt( e, "AiEssayScoringThread.performEssayScore() " + toString());
            throw e;
        }
    }

    @Override
    public String toString()
    {
        return "AiEssayScoringThread{" + "unscoredEssayId=" + (unscoredEssay==null ? "null" : unscoredEssay.getUnscoredEssayId()) + ", forceRescore=" + forceRescore + ", autoUpdate=" + autoUpdate + ", summaryCode=" + summaryCode + '}';
    }
}
