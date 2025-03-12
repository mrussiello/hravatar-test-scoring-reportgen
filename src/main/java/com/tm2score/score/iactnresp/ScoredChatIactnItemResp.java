/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.global.I18nUtils;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.item.ScoredChatIntnItem;
import com.tm2score.service.LogService;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class ScoredChatIactnItemResp extends IactnItemResp 
{
    // public static String POINTS_KEY = "[POINTS]";
    
    ScoredChatIntnItem scii = null;
    
    
    
    public ScoredChatIactnItemResp( IactnResp ir, SimJ.Intn.Intnitem ii, Clicflic.History.Intn iro)
    {
        super(ir, ii, iro, null );        
    }
    
    
    
    @Override
    public void init( SimletScore ss, TestEvent te )
    {
        try
        {
            g2ChoiceFormatType = G2ChoiceFormatType.getValue(  intnItemObj.getFormat()  );

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "ScoredChatIactnItemResp.init() AAA intnItemObj.getCompetencyscoreid()=" + intnItemObj.getCompetencyscoreid() + ", subnodeAutoSupported=" + g2ChoiceFormatType.supportsSubnodeLevelSimletAutoScoring() );
            
            if( intnItemObj.getCompetencyscoreid() > 0 && g2ChoiceFormatType.supportsSubnodeLevelSimletAutoScoring() )
                simletCompetencyScore = ss.getSimletCompetencyScore( intnItemObj.getCompetencyscoreid() );

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "ScoredChatIactnItemResp.init() BBB simletCompetencyScore=" + (simletCompetencyScore==null ? "null" : simletCompetencyScore.toString() ) );
            
            String respStr = getRespValue();

            scii = new ScoredChatIntnItem( (iactnResp==null ? null : iactnResp.getIntnObj()), intnItemObj, respStr, te );            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "IFrameIactnItemResp.init() " + toString() );
        }
    }
    
    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle() 
    {
        return true;
    }    
    
    @Override
    public void calculateScore() throws Exception
    {
        scii.calculate();
    }    
    
    @Override
    public String toString() {
        return "ScoredChatIactnItemResp{ iactn=" + (iactnResp==null || iactnResp.intnObj==null ? "iactnResp.intnObj is null" : iactnResp.intnObj.getName()) +
                " ("  +  (iactnResp.intnObj != null ? iactnResp.intnObj.getSeq() : "" ) + "-" + ( intnItemObj==null ? "intnItemObj is null" : intnItemObj.getSeq()) +  "), content=" + (intnItemObj==null ? "" : intnItemObj.getContent()) +
                " simletCompetencyScore.name=" + (simletCompetencyScore==null ? "simletCompetencyScore is null" : simletCompetencyScore.competencyScoreObj.getName()) + ", maxPoints[0]=" + (getMaxPointsArray() == null ? "null" : Float.toString( getMaxPointsArray()[0])) + '}';
    }   
    
    @Override
    public boolean hasMetaScore( int i )
    {
        return i<=5;
    }


    @Override
    public float getMetaScore( int i )
    {
        return scii!=null ? scii.getMetaScore(i) : 0;
    }
    
    
    
    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return true;
    }

    @Override
    public boolean correct()
    {
        return false;
    }

    @Override
    public boolean hasCorrectRespForSubnodeDichotomousScoring()
    {
        return true;
    }

    @Override
    public float itemScore()
    {
        return scii!=null && scii.getHasValidScore() ? scii.getOverallScore() : 0;
    }
    
    @Override
    public TextAndTitle getItemScoreTextTitle( int includeItemScoreTypeId )
    {
        IncludeItemScoresType iist = IncludeItemScoresType.getValue(includeItemScoreTypeId);
        
        if( iist.isNone() )
            return null;
        
        String itemLevelId = UrlEncodingUtils.decodeKeepPlus( getExtItemId() );
        if( itemLevelId == null || itemLevelId.isEmpty() )
        {
            itemLevelId = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getUniqueid());
            
            if( itemLevelId==null || itemLevelId.isEmpty() )
                itemLevelId = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getId());

            if( itemLevelId == null || itemLevelId.isEmpty() )
                itemLevelId = Integer.toString( iactnResp.intnObj.getSeq() );
        }
        
        String ques = null;
        String title = itemLevelId;   
        
        for( SimJ.Intn.Intnitem iitm : iactnResp.intnObj.getIntnitem() )
        {
            if( iitm.getIsquestionstem()==1 )
            {
                ques = StringUtils.getUrlDecodedValue( iitm.getContent() );

                if( ques!=null )
                    ques = StringUtils.truncateStringWithTrailer(ques, 255, true );
            }
        }

        if( ques!=null )
            title += "\n" + ques;            
        
        String text;
        
        if( iist.isIncludeCorrect() )
            text = itemScore()>0 ? "Partial" : "Incorrect";

        else
            text = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1 ) + " of 100"; //  Float.toString( itemScore() );
        
        if( text == null || text.isBlank())      
            return null;
        
        text = StringUtils.replaceStr( text, "[", "{" );
        title = StringUtils.replaceStr(title, "[", "{" );
        itemLevelId = StringUtils.replaceStr(itemLevelId, "[", "{" );
        ques = StringUtils.replaceStr(ques, "[", "{" );                
        return new TextAndTitle( text, title, 0, itemLevelId, ques );        
    }

    
    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        LogService.logIt( "ScoredChatIactnItemResp. getTextAndTitleList() " );
        if( scii==null || !scii.getHasValidScore() )
            return new ArrayList<>();
        
        return scii.getTextAndTitleList();
        
    }
    
    @Override
    public boolean saveAsItemResponse()
    {
        return true;
    }
    
    
    
    /**
     * map of topic name, int[]
     *    int[0] = number correct        ( for this item this means points )
     *    int[1] = number total this topic.  max points for competency
     *    int[2] = number of items that were partially correct.  ( 0 )
     *    int[3] = total number of items this topic. 
     */
    @Override
    public Map<String,int[]> getTopicMap()  
    {
        if( scii==null || !scii.getHasValidScore() )
            return null;
        
        return scii.getTopicMap();
    }
    
    
    
    

    @Override
    public void populateItemResponse( ItemResponse ir )
    {
        super.populateItemResponse(ir);
        
        if( scii!=null )
            ir.setSelectedValue( StringUtils.truncateString( scii.getSelectedRespForItemResponse( getRespValue() ), 1900 )   );        
    }
            
}
