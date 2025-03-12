/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2score.entity.event.ItemResponse;
import com.tm2score.event.ItemResponseType;
import com.tm2score.event.ResponseLevelType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.SimletItemType;
import com.tm2score.interview.InterviewQuestion;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to store and score item responses when the same interaction is responded to multiple times and the flag in the interaction says
 * that each response should be scored.
 *
 * @author Mike
 */
public class PrevIactnResp implements ScorableResponse
{

    IactnResp iactnResp = null;

    IactnItemResp clickedIactnItemResp = null;

    // 1 - N
    int index = 0;

    boolean validItemsCanHaveZeroMaxPoints = false;



    public PrevIactnResp( IactnResp ir, IactnItemResp iir, int idx)
    {
        this.iactnResp = ir;

        this.clickedIactnItemResp = iir;

        this.index = idx;

        this.validItemsCanHaveZeroMaxPoints = ir.getValidItemsCanHaveZeroMaxPoints();
    }

    @Override
    public String toString()
    {
        return "PrevIactnResp{ Iactn: " + iactnResp.intnObj.getName() + ", Unq Id=" + iactnResp.intnObj.getUniqueid() + ", Seq=" + iactnResp.intnObj.getSeq() + "-" + clickedIactnItemResp.intnItemObj.getSeq() + ", " + clickedIactnItemResp.intnItemObj.getContent() + ", itemScore=" + itemScore() + "}";
    }


    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle() 
    {
        return false;
    }
    
    public void init( TestEvent te )
    {}

    @Override
    public void calculateScore() throws Exception
    {}

    
    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        return new ArrayList<>();
    }    
    
    
    @Override
    public String getExtItemId()
    {
        return null;
    }
            
    @Override
    public String getSelectedExtPartItemIds()
    {
        return null;
    }
            
    
    @Override
    public int getCt5ItemId()
    {
        return iactnResp==null ? 0 : iactnResp.getCt5ItemId();
    }
    
    @Override
    public int getCt5ItemPartId()
    {
        return 0;
    }
    
    
    @Override
    public boolean getUsesOrContributesPointsToSimletCompetency( SimletCompetencyScore smltCs )
    {
        // standard check.
        return this.simletCompetencyId()==smltCs.competencyScoreObj.getId();
    }
    
    
    @Override
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId)
    {
        return null;
    }

    
    
    @Override
    public String getTopic()
    {
        return null;
    }
    
    @Override
    public Map<String,int[]> getTopicMap()  
    {
        return null;
    }
    
    
    @Override
    public List<MergableScoreObject> getMergableScoreObjects()
    {
        return new ArrayList<>();
    }
    
    @Override
    public float getDisplayOrder()
    {
        // String s = Math.round( iactnResp.getDisplayOrder()) + "." + index;

        // Want these to appear before the final one, which is an IactnResp not a PrevIactnResp
        // String s = Math.round( iactnResp.getDisplayOrder()-1) + ".50" + index;

        float base = (float) Math.round( iactnResp.getDisplayOrder()-1);
        
        float add = 0.5f + (((float)index)/100f);
        
        return base + add;
        // String s = Math.round( iactnResp.getDisplayOrder()-1) + ".50" + index;
        
        // return Float.parseFloat( s );
    }


    @Override
    public boolean isPendingExternalScore()
    {
        return false;
    }

    @Override
    public boolean requiresMaxPointIncrement()
    {
        return true;
    }

    @Override
    public float[] getMaxPointsArray()
    {
        if( iactnResp==null )
            return new float[4];

        return iactnResp.getMaxPointsArray();
    }

    
    /**
     * Returns an array of
     * data[0] = itemScore         (Float)
     * data[1] = scoreParam1       (Float)
     * data[2] = scoreParam2       (Float)
     * data[3] = scoreParam3       (Float)
     * data[10] = textScoreParam1  (String)
     * 
     * @return 
     */
    @Override
    public Object[] getScoreParamsArray()
    {
        if( iactnResp==null )
        {
            Object[] out = new Object[11];
            out[0] = Float.valueOf(0);
            out[1] = Float.valueOf(0);
            out[2] = Float.valueOf(0);
            out[3] = Float.valueOf(0);
            return out;
        }
        
        return iactnResp.getScoreParamsArray();
    }
    

    @Override
    public boolean saveAsItemResponse()
    {
        // if this item is auto scorable
        return clickedIactnItemResp!= null && iactnResp != null && isAutoScorable( );
    }

    public SimletScore getSimletScore() {
        return  iactnResp == null ? null : iactnResp.simletScore;
    }

    public boolean getIsRedFlag()
    {
        if( clickedIactnItemResp != null )
            return clickedIactnItemResp.getIsRedFlag();

        return false;
    }

    public SimJ.Intn getIntnObj()
    {
        return  iactnResp == null ? null : iactnResp.intnObj;
    }


    /*
     * An interaction item is scorable if it is:
     *   1. It is associated with an interaction that is autoscroable. // simletCompetencyScore and has an itemtype compatible with CompetencyScore
     *   2.
     *
     *
     *
     */
    @Override
    public boolean isAutoScorable()
    {
        return clickedIactnItemResp != null && iactnResp != null && iactnResp.isAutoScorable(); //  .intnObj.getCompetencyscoreid()>0 && this.iactnResp.intnObj.; // &&
        // return clickedIactnItemResp.intnItemObj.getIscorrect()==1 || clickedIactnItemResp.intnItemObj.getItemscore()!=0;
    }

    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        // Prev Iactn Responses are never scored dichotomously.
        return false;
    }



    @Override
    public long simletId()
    {
        return iactnResp == null ? 0 : iactnResp.simletId();
    }



    @Override
    public SimletItemType getSimletItemType()
    {
        return iactnResp == null ? null : iactnResp.getSimletItemType();
    }

    @Override
    public boolean measuresSmltCompetency( long simletCompetencyId )
    {
        return iactnResp == null ? false : iactnResp.measuresSmltCompetency( simletCompetencyId );
    }

    @Override
    public long simletCompetencyId()
    {
        return iactnResp == null ? 0 : iactnResp.simletCompetencyId();
    }

    @Override
    public int getCt5SubtopicId()
    {
        return iactnResp == null ? 0 : iactnResp.getCt5SubtopicId();
    }
    
    
    
    @Override
    public long simCompetencyId()
    {
        return iactnResp == null ? 0 : iactnResp.simCompetencyId();
    }
    
    
    

    @Override
    public int simletItemTypeId()
    {
        return iactnResp == null ? 0 : iactnResp.simletItemTypeId();
    }

    @Override
    public boolean experimental()
    {
        return iactnResp == null ? false : iactnResp.experimental();
    }


    /**
     * correct=  0 means answered, wrong answer or not scored
                 1 means answered correct,
                -1 means not answered,
                -2 means timed out
     * @return
     */
    public int getItemResponseTypeId()
    {
        // PrevIactnResp is never a timeout and is never not answered, so ....

        // depends on item type
        return ItemResponseType.ANSWERED.getItemResponseTypeId();
    }



    @Override
    public boolean getPartialCreditAssigned()
    {
        return false;
    }


    /**
     * correct=  0 means wrong answer
                 1 means correct,
                -1 means not answered,
                -2 means timed out
     * @return
     */
    @Override
    public boolean correct()
    {
        // prev iactn responses are never correct.
        // depends on item type
        //if( getSimletItemType().isDichotomous() )
        //    return clickedIactnItemResp.intnResultObj.getCorrect()==1;

        return false;
    }


    @Override
    public float itemScore()
    {
        return clickedIactnItemResp == null ? 0 : clickedIactnItemResp.intnItemObj.getItemscore(); //   .intnResultObj.getItmscr();
    }


    @Override
    public List<IactnItemResp> getAllScorableIntItemResponses()
    {
        return new ArrayList<>();
    }

    @Override
    public int simletVersionId()
    {
        return iactnResp == null ? 0 : iactnResp.simletVersionId();
    }

    @Override
    public long getSimletActId()
    {
        return iactnResp == null ? 0 : iactnResp.getSimletActId();
    }

    @Override
    public long getSimletNodeId()
    {
        return iactnResp == null ? 0 : iactnResp.getSimletNodeId();
    }

    @Override
    public int getSimletNodeSeq()
    {
        return iactnResp == null ? 0 : iactnResp.getSimletNodeSeq();
    }

    @Override
    public String getSimletNodeUniqueId()
    {
        return iactnResp == null ? null : iactnResp.getSimletNodeUniqueId();
    }

    @Override
    public float getResponseTime()
    {
        return iactnResp == null ? 0 : iactnResp.getResponseTime();
    }


    @Override
    public void populateItemResponse( ItemResponse ir )
    {
        iactnResp.populateItemResponseCore( ir );

        if( iactnResp.intnObj.getIncprevselections()==1 )
            ir.setRepeatItemSimNodeSeq( iactnResp.intnObj.getSeq() );        
        
         ir.setIdentifier( ResponseLevelType.PREV_INTERACTION.computeIdentifier( ir, index ) );

         ir.setResponseLevelId( ResponseLevelType.PREV_INTERACTION.getResponseLevelId() );

         if( iactnResp.simletCompetencyScore!=null )
             ir.setSimCompetencyId( iactnResp.simletCompetencyScore.competencyScoreObj.getSimcompetencyid() );

         ir.setCompetencyScoreId( simletCompetencyId() );

         ir.setItemResponseTypeId( getItemResponseTypeId() );

         ir.setTrueScore( iactnResp == null ? 0 : iactnResp.intnObj.getTruescore() );

         ir.setItemParadigmTypeId( ScoredItemParadigmType.getValue(iactnResp).getScoredItemParadigmTypeId() );

         ir.setItemScore( itemScore() );

         if( clickedIactnItemResp != null )
         {
             ir.setScoreParam1( clickedIactnItemResp.intnItemObj.getScoreparam1() );
             ir.setScoreParam2( clickedIactnItemResp.intnItemObj.getScoreparam2() );
             ir.setScoreParam3( clickedIactnItemResp.intnItemObj.getScoreparam3() );
             ir.setSelectedValue(StringUtils.truncateString(UrlEncodingUtils.decodeKeepPlus( clickedIactnItemResp.intnItemObj.getContent() ), 1900) );
             ir.setSubnodeFormatTypeId( clickedIactnItemResp.g2ChoiceFormatType.getG2ChoiceFormatTypeId() );
             ir.setSelectedSubFormatTypeIds( Integer.toString( ir.getSubnodeFormatTypeId() ));
             ir.setSubnodeSeq( clickedIactnItemResp.intnItemObj.getSeq() );
             ir.setSelectedSubnodeSeqIds( Integer.toString( ir.getSubnodeSeq() ));
         }

         ir.setSimletItemTypeId( simletItemTypeId() );

        if( getSimletItemType().isDichotomous() )
        {
            ir.setCorrect( correct() ? 1 : 0 );

            ir.setCorrectSubnodeSeqIds( iactnResp.getCorrectSubnodeSeqStr() );
        }

        // To support item analysis, for non-dichotomous items if itemscore=max points we can consider the item correct.
        else if( !getSimletItemType().isDichotomous() &&
            iactnResp.simletCompetencyScore != null &&
            SimCompetencyClass.getValue( iactnResp.simletCompetencyScore.competencyScoreObj.getClassid() ).getSupportsQuasiDichotomous() )
        {
            if( itemScore()==getMaxPointsArray()[0] && itemScore()>0 )
            {
                ir.setCorrect(1);
                ir.setCorrectSubnodeSeqIds( ir.getSelectedSubnodeSeqIds() );
            }
        }


    }


    @Override
    public float getAggregateItemScore( SimCompetencyClass simCompetencyClass )
    {
        if( clickedIactnItemResp == null )
            return 0;

        return simCompetencyClass.getAggregatePoints( clickedIactnItemResp );
    }


   /**
     * Returns - unless the scorableresponse has a textScoreParam1 of [SCALEDSCOREFLOOR]value  where value is the min value for the scaled competency score that is to be
     * enforced based on the fact that this person made this selection.
     * @return
     */
    @Override
    public float getFloor()
    {
        return this.iactnResp.getFloor();
    }


    /**
     * Returns - unless the scorableresponse has a textScoreParam1 of [SCALEDSCORECEILING]value  where value is the max value for the scaled competency score that is to be
     * enforced based on the fact that this person made this selection.
     * @return
     */
    @Override
    public float getCeiling()
    {
        return this.iactnResp.getCeiling();
    }


    /**
     * Returns null - unless the scorableresponse has a textScoreParam1 of [SCORETEXTCAVEAT]value sentence  -  where value sentence is a sentence that should be appended to
     * the scoretext for this competency in any report.
     * @return
     */
    @Override
    public String getCaveatText()
    {
        return null; // this.iactnResp.getCaveatText();
    }


    @Override
    public InterviewQuestion getScoreTextInterviewQuestion()
    {
        return null;
    }

    @Override
    public boolean hasMetaScore( int i )
    {
        return false;
    }


    @Override
    public float getMetaScore( int i )
    {
        return 0;
    }


    @Override
    public boolean hasValidScore()
    {
        return true;
    }

    @Override
    public List<String> getForcedRiskFactorsList()
    {
        return null;
    }

    @Override
    public float getTotalItemCountIncrementValue() 
    {
        return 1;
    }
    
}
