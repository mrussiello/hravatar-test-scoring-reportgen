/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.essay;

import com.tm2score.ai.AiCallType;
import com.tm2score.ai.AiRequestUtils;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import jakarta.json.JsonObject;

/**
 *
 * @author miker
 */
public class AiEssayScoringUtils {
    
    static Boolean AI_ESSAYS_ON;
    static Boolean USE_SCORE_2;
    
    private static synchronized void init()
    {
        if( AI_ESSAYS_ON!=null )
            return;
        AI_ESSAYS_ON = RuntimeConstants.getBooleanValue("ai-essay-scoring-ok");
        USE_SCORE_2 = RuntimeConstants.getBooleanValue("ai-essay-scoring-use-score2");
    }
    
    public static boolean getAiEssayScoringOn()
    {
        if( AI_ESSAYS_ON==null)
            init();

        return AI_ESSAYS_ON;        
    }
            
    public static boolean getAiEssayScoringUseScore2()
    {
        if( USE_SCORE_2==null)
            init();

        return USE_SCORE_2;        
    }
            
    
    public static boolean computeAiEssayScore( UnscoredEssay unscoredEssay, boolean autoUpdate, boolean forceRescore, boolean omitGrammarMech, String forcePromptStr, String idealResponseStr, String aiInstructionsStr)
    {
        try
        {
            if( AI_ESSAYS_ON==null)
                init();
            
            if( !AI_ESSAYS_ON )
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() AI Essay Scoring is disabled. unscoredEssayId=" + (unscoredEssay==null ? "null" : unscoredEssay.getUnscoredEssayId()+ ", userId=" + unscoredEssay.getUserId()) );
                return false;
            }
                        
            if( unscoredEssay==null )
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() unscoredEssay is null" );
                return false;
            }

            if( unscoredEssay.getUnscoredEssayId()<=0 )
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() UnscoredEssay.unscoredEssayId is invalid: " + unscoredEssay.getUnscoredEssayId() );
                return false;
            }
            
            if( (unscoredEssay.getEssay()==null || unscoredEssay.getEssay().isBlank()) ) 
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() UnscoredEssay does not have an Essay to score. unscoredEssayId=" + unscoredEssay.getUnscoredEssayId() );
                return false;
            }
            
            if( (forcePromptStr==null || forcePromptStr.isBlank()) &&  unscoredEssay.getEssayPromptId()<=0 )
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() UnscoredEssay has an invalid essaypromptid: " + unscoredEssay.getEssayPromptId() );
                return false;
            }

            if( !forceRescore && !USE_SCORE_2 && unscoredEssay.getScoreDate()!=null && unscoredEssay.getComputedScore()!=0 )                    
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() UnscoredEssay has already been scored by AI and forceRescore is false. unscoredEssayId=" + unscoredEssay.getUnscoredEssayId() );
                return false;
            }

            if( !forceRescore && USE_SCORE_2 && unscoredEssay.getScoreDate2()!=null && unscoredEssay.getComputedScore2()!=0 )                    
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() UnscoredEssay has already been scored by AI and forceRescore is false. unscoredEssayId=" + unscoredEssay.getUnscoredEssayId() );
                return false;
            }
            
            JsonObject responseJo = AiRequestUtils.doEssayScoringCall(unscoredEssay, AiCallType.ESSAY_SCORE, USE_SCORE_2, autoUpdate, forceRescore, omitGrammarMech, forcePromptStr, idealResponseStr, aiInstructionsStr);
            
            if( !AiRequestUtils.wasAiCallSuccess( responseJo ) )
            {
                LogService.logIt("AiRequestUtils.computeAiEssayScore() AI Call failed. unscoredEssayId=" + unscoredEssay.getUnscoredEssayId() );
                return false;
            }
            
            return true;
        }
        catch( Exception e )
        {
            Tracker.addAiCallError();
            LogService.logIt(e, "AiRequestUtils.computeAiEssayScore() unscoredEssayId=" + (unscoredEssay==null ? "null" : unscoredEssay.getUnscoredEssayId()+ ", userId=" + unscoredEssay.getUserId()) );
            return false;
        }
    }

    public static boolean computeAiEssaySummary( UnscoredEssay unscoredEssay, boolean autoUpdate, boolean forceRedo)
    {
        try
        {
            if( AI_ESSAYS_ON==null)
                init();
            
            if( !AI_ESSAYS_ON )
            {
                LogService.logIt("AiRequestUtils.computeAiEssaySummary() AI Essay Scoring is disabled. unscoredEssayId=" + (unscoredEssay==null ? "null" : unscoredEssay.getUnscoredEssayId()+ ", userId=" + unscoredEssay.getUserId()) );
                return false;
            }
                        
            if( unscoredEssay==null )
            {
                LogService.logIt("AiRequestUtils.computeAiEssaySummary() unscoredEssay is null" );
                return false;
            }

            if( unscoredEssay.getUnscoredEssayId()<=0 )
            {
                LogService.logIt("AiRequestUtils.computeAiEssaySummary() UnscoredEssay.unscoredEssayId is invalid: " + unscoredEssay.getUnscoredEssayId() );
                return false;
            }
            
            if( (unscoredEssay.getEssay()==null || unscoredEssay.getEssay().isBlank()) ) 
            {
                LogService.logIt("AiRequestUtils.computeAiEssaySummary() UnscoredEssay does not have an Essay to score. unscoredEssayId=" + unscoredEssay.getUnscoredEssayId() );
                return false;
            }

            if( !forceRedo && unscoredEssay.getSummaryDate()!=null && unscoredEssay.getSummary()!=null && !unscoredEssay.getSummary().isBlank() )                    
            {
                LogService.logIt("AiRequestUtils.computeAiEssaySummary() UnscoredEssay has already been summarized by AI and forceRedo is false. unscoredEssayId=" + unscoredEssay.getUnscoredEssayId() );
                return false;
            }

            JsonObject responseJo = AiRequestUtils.doEssaySummaryCall(unscoredEssay, AiCallType.ESSAY_SUMMARY, autoUpdate, forceRedo);
            
            if( !AiRequestUtils.wasAiCallSuccess( responseJo ) )
            {
                LogService.logIt("AiRequestUtils.computeAiEssaySummary() AI Call failed. unscoredEssayId=" + unscoredEssay.getUnscoredEssayId() );
                return false;
            }
            
            return true;
        }
        catch( Exception e )
        {
            Tracker.addAiCallError();
            LogService.logIt(e, "AiRequestUtils.computeAiEssaySummary() unscoredEssayId=" + (unscoredEssay==null ? "null" : unscoredEssay.getUnscoredEssayId()+ ", userId=" + unscoredEssay.getUserId()) );
            return false;
        }
    }
}
