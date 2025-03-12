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
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.item.IntnClkStrmItem;
import com.tm2score.service.LogService;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class IntnClkStrmIactnItemResp extends IactnItemResp 
{
    // public static String POINTS_KEY = "[POINTS]";
    
    IntnClkStrmItem icsi = null;
    
    public IntnClkStrmIactnItemResp( IactnResp ir, SimJ.Intn.Intnitem ii, Clicflic.History.Intn iro)
    {
        super(ir, ii, iro, null);        
    }
    
    
    
    @Override
    public void init( SimletScore ss, TestEvent te )
    {
        try
        {
            g2ChoiceFormatType = G2ChoiceFormatType.getValue(  intnItemObj.getFormat()  );

            if( intnItemObj.getCompetencyscoreid() > 0 && g2ChoiceFormatType.supportsSubnodeLevelSimletAutoScoring() )
                simletCompetencyScore = ss.getSimletCompetencyScore( intnItemObj.getCompetencyscoreid() );

            String respStr = getRespValue();
            String keyStr = IvrStringUtils.getTagValueWithDecode( intnItemObj.getTextscoreparam1(), Constants.POINTS_KEY ) ;
            // String hotKeyStr = intnItemObj.get

            icsi = new IntnClkStrmItem( intnResultObj.getNdseq(), intnItemObj.getSeq(), keyStr, respStr, te.getSimXmlObj() );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "IntnClkStrmIactnItemResp.init() " + toString() );
        }
    }
    
    @Override
    public void calculateScore() throws Exception
    {
        icsi.calculate();
        
        // LogService.logIt( "IntnClkStrmIactnItemResp.calculateScore() IntnItem (" + iactnResp.intnObj.getSeq() + "," + intnItemObj.getSeq() + " points assigned=" + icsi.getPoints() + ", maxPoints=" + (getMaxPointsArray() == null ? "null" : Float.toString( getMaxPointsArray()[0])) );
    }    
    
    @Override
    public String toString() {
        return "IntnClkStrmIactnItemResp{ iactn=" + (iactnResp==null || iactnResp.intnObj==null ? "iactnResp.intnObj is null" : iactnResp.intnObj.getName()) +
                " ("  +  (iactnResp.intnObj != null ? iactnResp.intnObj.getSeq() : "" ) + "-" + ( intnItemObj==null ? "intnItemObj is null" : intnItemObj.getSeq()) +  "), content=" + (intnItemObj==null ? "" : intnItemObj.getContent()) +
                " simletCompetencyScore.name=" + (simletCompetencyScore==null ? "simletCompetencyScore is null" : simletCompetencyScore.competencyScoreObj.getName()) + ", maxPoints[0]=" + (getMaxPointsArray() == null ? "null" : Float.toString( getMaxPointsArray()[0])) + '}';
    }    
    
    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return true;
    }

    @Override
    public boolean correct()
    {
        return icsi!=null && icsi.getHasValidScore() && icsi.getPoints()==intnItemObj.getItemscore();
    }

    @Override
    public boolean hasCorrectRespForSubnodeDichotomousScoring()
    {
        return true;
    }

    @Override
    public float itemScore()
    {
        return icsi!=null && icsi.getHasValidScore() ? icsi.getPoints() : 0;
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
        
                
        String text = null;
        
        if( iist.isIncludeCorrect() )
            text = correct() ? "Correct" : (getPartialCreditAssigned() ? "Partial" : "Incorrect" );
            // text = correct() ? "Correct" : "Incorrect";

        else if( iist.isIncludeNumericScore() )
        {
            text = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1 ); //  Float.toString( itemScore() );
        }

        else if( iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore( itemScore() );

        else if( iist.isResponseOrResponseCorrect() )
        {
            text = Float.toString( itemScore() );            
            if( iist.isResponseCorrect() )
                text += " (" + (correct() ? "Correct" : (getPartialCreditAssigned() ? "Partial" : "Incorrect" )) + ")";
        }
        
        if( text == null || text.isEmpty() )      
            return null;
        
        text = StringUtils.replaceStr( text, "[", "{" );
        title = StringUtils.replaceStr(title, "[", "{" );
        itemLevelId = StringUtils.replaceStr(itemLevelId, "[", "{" );
        ques = StringUtils.replaceStr(ques, "[", "{" );                
        return new TextAndTitle( text, title, 0, itemLevelId, ques );        
    }

    
    
    

    @Override
    public void populateItemResponse( ItemResponse ir )
    {
        super.populateItemResponse(ir);
        
        ir.setSelectedValue( StringUtils.truncateString( getRespValue(), 1900 )   );
        
    }
}
