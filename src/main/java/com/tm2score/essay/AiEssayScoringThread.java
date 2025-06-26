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
    String forcePromptStr;
    
    public AiEssayScoringThread( UnscoredEssay ue, boolean forceRescore, boolean autoUpdate, String forcePromptStr )
    {
        this.unscoredEssay=ue;
        this.forceRescore=forceRescore;
        this.autoUpdate=autoUpdate;
        this.forcePromptStr=forcePromptStr;
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
        return AiEssayScoringUtils.computeAiEssayScore(unscoredEssay, autoUpdate, forceRescore, forcePromptStr);
    }
    
    
}
