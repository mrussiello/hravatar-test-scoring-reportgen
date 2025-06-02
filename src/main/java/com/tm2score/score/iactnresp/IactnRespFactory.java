/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.iactnresp;

import com.tm2score.imo.xml.Clicflic;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.act.IframeItemType;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.SimletItemType;

/**
 *
 * @author Mike
 */
public class IactnRespFactory {

    public static IactnResp getIactnResp( Clicflic.History.Intn intRespObj, SimJ.Intn intn, SimJ simJ, TestEvent testEvent) throws Exception
    {
        if( intn != null )
        {
            SimletItemType sit = SimletItemType.getValue( intn.getScoretype() );

            // Need to check for a AV Upload item that has a Simlet Item Type of points or Dichot.
            // Sim competency id is always at the intn level for this type of interaction.
            if( ( sit.isDichotomous() || sit.isPoints() ) && intn.getSimcompetencyid()>0 )
            {
                // find the sim competency.
                for( SimJ.Simcompetency sc : simJ.getSimcompetency() )
                {
                    if( sc.getId()==intn.getSimcompetencyid() )
                    {
                        // if the sim competency is actually a ScoreAv, then return a ScoreAvIactnResp
                        if( SimCompetencyClass.getValue(sc.getClassid()).isScoredAvUpload() )
                            return new ScoredAvIactnResp( intRespObj, intn, testEvent );                        
                    }
                }
                // SimJ.
            }
            
            if( sit.equals(SimletItemType.AUTO_TYPING ) )
                return new TypingIactnResp( intRespObj);

            if( sit.equals(SimletItemType.AUTO_DATA_ENTRY ) )
                return new DataEntryIactnResp( intRespObj);
                        
            if( sit.equals( SimletItemType.AUTO_ESSAY ) )
                return new ScoredEssayIactnResp( intRespObj, testEvent);

            //if( sit.equals( SimletItemType.AUTO_CHAT ) )
            //     return new ScoredChatIactnResp( intRespObj);

            if( sit.equals( SimletItemType.AUTO_AV_UPLOAD ) )
                return new ScoredAvIactnResp( intRespObj, intn, testEvent );

            if( sit.equals( SimletItemType.RESUME_CAPTURE ) )
                return new ResumeIactnResp( intRespObj, intn, testEvent );

            //if( sit.equals(SimletItemType.IMAGE_CAPTURE ) )
            //    return new ImageCaptureIactnResp( intRespObj, intn );

        }

        return new IactnResp( intRespObj, testEvent);
    }

    
    
    public static IactnItemResp getIactnItemResp( IactnResp iactnResp, SimJ.Intn.Intnitem intnItemObj, Clicflic.History.Intn intRespObj, TestEvent testEvent)
    {
        if( intnItemObj != null && intnItemObj.getFormat()==G2ChoiceFormatType.PIN_IMAGE.getG2ChoiceFormatTypeId() )
            return new PinImageIactnItemResp( iactnResp, intnItemObj, intRespObj);

        if( intnItemObj != null && intnItemObj.getFormat()==G2ChoiceFormatType.INTN_CLK_STRM.getG2ChoiceFormatTypeId() )
            return new IntnClkStrmIactnItemResp( iactnResp, intnItemObj, intRespObj);

        // If it's an iFrame we need to use the IframItemTypeId in the descrip.        
        if( intnItemObj != null && intnItemObj.getFormat()==G2ChoiceFormatType.IFRAME.getG2ChoiceFormatTypeId() )
        {
            if( intnItemObj.getIframeitemtype()==IframeItemType.CHAT.getIframeItemTypeId() )
                return new ScoredChatIactnItemResp( iactnResp, intnItemObj, intRespObj);
            else
                return new IFrameIactnItemResp( iactnResp, intnItemObj, intRespObj);
        }

        return new IactnItemResp( iactnResp, intnItemObj, intRespObj, testEvent);
    }

}
