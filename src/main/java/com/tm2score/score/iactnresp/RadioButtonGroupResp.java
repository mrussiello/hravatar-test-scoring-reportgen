/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2score.entity.event.ItemResponse;
import com.tm2score.event.ItemResponseType;
import com.tm2score.event.ResponseLevelType;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.service.LogService;
import com.tm2score.sim.InteractionScoreUtils;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.SimletItemType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class RadioButtonGroupResp  implements ScorableResponse
{
    /**
     * For Radio button groups where different radio buttons in the same group belong to different competencies. 
     * 
     * Format in RadioButtonIntnItem.textScoreParam1=[RBCOMP]Competency Name   - should be at the end of textScoreParam1
     */
    public static String RBG_COMP_SUBSTITUTION_TAG = "[RBCOMP]";
    //
    SimJ.Intn.Radiobuttongroup radioButtonGroupObj = null;

    // This is the interaction inside the sim
    IactnResp iactnResp = null;

    // This is the competencyScore object for this item, if any.
    SimletCompetencyScore simletCompetencyScore = null;

        // this is a list of all interaction items that have input or score data or are individually scored.
    List<IactnItemResp> iactnItemRespLst = null;

    IactnItemResp selectedIactnItemResp = null;

    float[] maxPointsArray;

    boolean validItemsCanHaveZeroMaxPoints = false;

    public RadioButtonGroupResp( IactnResp ir, SimJ.Intn.Radiobuttongroup rbgo)
    {
        this.radioButtonGroupObj = rbgo;
        this.iactnResp = ir;

        validItemsCanHaveZeroMaxPoints = ir.getValidItemsCanHaveZeroMaxPoints();
    }

    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle() 
    {
        return false;
    }
    
    public void init(TestEvent testEvent) throws Exception
    {
        // LogService.logIt( "RadioButtonGroupResp.init() " );        
        
        if( radioButtonGroupObj == null || iactnResp == null )
            return;

        // get the SimletCompetencyScore
        if( iactnResp.simletScore != null && radioButtonGroupObj.getCompetencyscoreid()>0 )
        {
            simletCompetencyScore = iactnResp.simletScore.getSimletCompetencyScore( radioButtonGroupObj.getCompetencyscoreid() );  //  iactnResp.simletScore.getCompetencyScoreForSimletCompetencyId( radioButtonGroupObj.getCompetencyscoreid() );
        }


        // Decode Text Score Param 1 for the Radio Button Group
        if( radioButtonGroupObj.getTextscoreparam1() != null && !radioButtonGroupObj.getTextscoreparam1().isEmpty() )
            radioButtonGroupObj.setTextscoreparam1( UrlEncodingUtils.decodeKeepPlus(radioButtonGroupObj.getTextscoreparam1(), "UTF8") );

        iactnItemRespLst = new ArrayList<>();

        // LogService.logIt( "RadioButtonGroupScore.init() id=" + radioButtonGroupObj.getRadiobuttongroupid() + " total intitemresponses to parse: " + iactnResp.iactnItemRespLst.size() );

        for( IactnItemResp iir : iactnResp.iactnItemRespLst )
        {
            if( iir.g2ChoiceFormatType.getIsAnyRadio() && iir.intnItemObj.getRadiobuttongroup() == radioButtonGroupObj.getRadiobuttongroupid() )
            {
                iactnItemRespLst.add( iir );

                if( iir.getWasSelected() )
                {
                    selectedIactnItemResp = iir;                    
                }
            }
        }

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "RadioButtonGroupResp.init() AAA.1 Intn=" + this.iactnResp.intnObj.getUniqueid() + " rbg id=" + this.radioButtonGroupObj.getRadiobuttongroupid() + ", selectedIactnItemResp=" + (selectedIactnItemResp==null ? "null" : selectedIactnItemResp.intnItemObj.getSeq() ) );
                
        if( selectedIactnItemResp != null )
        {
            // Check for substituted competency. 
            if( iactnResp.simletScore != null && selectedIactnItemResp.intnItemObj.getTextscoreparam1()!=null && !selectedIactnItemResp.intnItemObj.getTextscoreparam1().isEmpty() )
            {
                String subComp = IvrStringUtils.getTagValueWithDecode( selectedIactnItemResp.intnItemObj.getTextscoreparam1() , RBG_COMP_SUBSTITUTION_TAG );
                
                if( ScoreManager.DEBUG_SCORING )
                    LogService.logIt( "RadioButtonGroupResp.init() AAA.2 Intn=" + this.iactnResp.intnObj.getUniqueid() + " rbg id=" + this.radioButtonGroupObj.getRadiobuttongroupid() + ", subComp=" + subComp + ", selectedIactnItemResp.intnItemObj.getTextscoreparam1()=" + selectedIactnItemResp.intnItemObj.getTextscoreparam1() );
                
                if( subComp!=null && !subComp.trim().isEmpty() )
                {
                    subComp = subComp.trim();
                    
                    SimletCompetencyScore scs2 = iactnResp.simletScore.getSimletCompetencyScoreByName( subComp );
                    
                    if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "RadioButtonGroupResp.init() AAA.3 Intn=" + this.iactnResp.intnObj.getUniqueid() + " rbg id=" + this.radioButtonGroupObj.getRadiobuttongroupid() + ", subComp=" + subComp + ", replacing with scs2=" + (scs2==null ? "null" : scs2.toString() ) );
                    
                    if( scs2!=null )
                        simletCompetencyScore = scs2;
                    
                    else if( ScoreManager.DEBUG_SCORING )
                        LogService.logIt( "RadioButtonGroupResp.init() AAA.5 Cannot find Simlet Competency To Replace. Intn=" + this.iactnResp.intnObj.getUniqueid() + " rbg id=" + this.radioButtonGroupObj.getRadiobuttongroupid() + ", seeking subComp=" + subComp + ", selectedIactnItemResp.intnItemObj.getTextscoreparam1()=" + selectedIactnItemResp.intnItemObj.getTextscoreparam1() );
                }
            }
        }
        
        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "RadioButtonGroupScore.init() Intn=" + this.iactnResp.intnObj.getUniqueid() + ", id=" + radioButtonGroupObj.getRadiobuttongroupid() + ", " + (simletCompetencyScore==null ? "simletCompetencyScore=null" : simletCompetencyScore.toString() ) + ",  iactnItemRespLst=" + iactnItemRespLst.size() + ", selectedIactnItemResp " + (selectedIactnItemResp!=null) );

    }

    @Override
    public int getCt5ItemId()
    {
        return iactnResp==null ? 0 : iactnResp.getCt5ItemId();
    }
    
    @Override
    public int getCt5ItemPartId()
    {
        return this.radioButtonGroupObj==null ? 0 : this.radioButtonGroupObj.getCt5Itempartid();
    }
    
    
    @Override
    public boolean getUsesOrContributesPointsToSimletCompetency( SimletCompetencyScore smltCs )
    {
        // check if this is a multiple choice or multi correct checkbox with points.
        if( smltCs.getCompetencyScoreType().isPointAccum() )
        {
            // see if the selected choice matches this simcompetency.
            if( selectedIactnItemResp!=null && selectedIactnItemResp.intnItemObj.getCompetencyscoreid()>0 )
                return selectedIactnItemResp.intnItemObj.getCompetencyscoreid()==smltCs.competencyScoreObj.getId();
        }
        
        // standard check.
        return simletCompetencyId()==smltCs.competencyScoreObj.getId();
    }
    
    
    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        return new ArrayList<>();
    }    
    
    
    @Override
    public String getTopic()
    {
        if( radioButtonGroupObj==null )
            return null;
        
        String tsp = radioButtonGroupObj.getTextscoreparam1();
        
        if( tsp==null || tsp.trim().isEmpty() )
            return null;
        
        return IvrStringUtils.getTagValueWithDecode(tsp, Constants.TOPIC_KEY );
    }
    
    @Override
    public Map<String,int[]> getTopicMap()  
    {
        return ScoreUtils.getSingleTopicTopicMap( getTopic(), correct(), getPartialCreditAssigned() );
    }
    
    
    
    @Override
    public float getDisplayOrder()
    {
        String s = Math.round(iactnResp.getDisplayOrder()) + ".00" + this.radioButtonGroupObj.getRadiobuttongroupid();
        return Float.parseFloat( s );
    }

    
    
    

    @Override
    public void calculateScore() throws Exception
    {}


    @Override
    public boolean isPendingExternalScore()
    {
        return false;
    }

    @Override
    public List<MergableScoreObject> getMergableScoreObjects()
    {
        return new ArrayList<>();
    }
    

    @Override
    public String toString() {
        return "RadioButtonGroupScore{ iactn=" + this.iactnResp.intnObj.getName() + "("  +  this.iactnResp.intnObj.getSeq() +  "), num (rbgid)=" + this.radioButtonGroupObj.getRadiobuttongroupid() + ", maxPoints[0]=" + (maxPointsArray == null ? "null" : Float.toString( maxPointsArray[0]))  + ", ct5ItemId=" + this.getCt5ItemId() + ", ct5ItemPartId=" + this.getCt5ItemPartId() + '}';
    }


    @Override
    public boolean requiresMaxPointIncrement()
    {
        return false;
    }

    /**
     * int[0] = min
     * int[1] = max
     * 
     * @return 
     */
    private float[] getMinMaxPointsForSubComp()
    {
        if( selectedIactnItemResp==null )
            return null;
        
        if( simletCompetencyScore==null )
            return null;
        
        if( simletCompetencyScore.competencyScoreObj==null ||  simletCompetencyScore.competencyScoreObj.getId() == radioButtonGroupObj.getCompetencyscoreid() )
            return null;
        
        if( this.iactnResp==null || this.iactnResp.intnObj==null )
            return null;
        
        // OK at this point we need to find all for this id. 
        // min , max
        float[] out = new float[] {999999,-999999};

        String c = null;
        
        for( SimJ.Intn.Intnitem iitm : iactnResp.intnObj.getIntnitem() )
        {
            if( !G2ChoiceFormatType.getValue( iitm.getFormat() ).getIsAnyRadio()  )
                continue;

            if( iitm.getRadiobuttongroup()!=radioButtonGroupObj.getRadiobuttongroupid() )
                continue;
            
            if( iitm.getTextscoreparam1()==null || iitm.getTextscoreparam1().trim().isEmpty() )
                continue;
            
            c = IvrStringUtils.getTagValueWithDecode( iitm.getTextscoreparam1(), RadioButtonGroupResp.RBG_COMP_SUBSTITUTION_TAG );
            
            if( c==null )
                continue;
            
            c = c.trim();
            
            // match. 
            if( c.equalsIgnoreCase( simletCompetencyScore.competencyScoreObj.getName() ) || ( simletCompetencyScore.competencyScoreObj.getNameenglish() != null && simletCompetencyScore.competencyScoreObj.getNameenglish().equalsIgnoreCase(c)) )
            {
                if( iitm.getItemscore()>out[1] )
                    out[1]=iitm.getItemscore();
                
                if( iitm.getItemscore()<out[0] )
                    out[0]=iitm.getItemscore();
            }
        }
        
        if( out[0]==999999 )
            out[0]=0;

        if( out[1]==-999999 )
            out[1]=0;
        
        return out;
    }
    

    /**
     * As long as it's connected to a competency and it has an item type that supports autoscoring its in.
     * @return
     *
    public boolean supportsRadBtnGrpSimletAutoScoring()
    {
        return this.is
        // must be connected to a simlet competency.
        if( simletCompetencyId()==0 )
            return false;

        // simlet item type must support autoscoreing.
        if( !getSimletItemType().supportsAutoScoring() )
            return false;

        return true;

        //for( IactnItemResp iir : iactnItemRespLst )
        //{
        //    if( iir.intnItemObj.getItemscore()!=0 || iir.intnItemObj.getIscorrect()==1 )
        //        return true;
        //}

        //return false;
    }*/


    @Override
    public synchronized float[] getMaxPointsArray()
    {
        if( maxPointsArray != null )
            return maxPointsArray;

        if(radioButtonGroupObj==null  )
            return new float[4];
        
        float[] substitutedCompMinMaxPoints = getMinMaxPointsForSubComp();

        if(radioButtonGroupObj.getMaxpoints() == null || radioButtonGroupObj.getMaxpoints().isEmpty() )
            return substitutedCompMinMaxPoints==null ? new float[4] : new float[] {substitutedCompMinMaxPoints[1],0,0,0};
        
        
        maxPointsArray = InteractionScoreUtils.getPointsArray( radioButtonGroupObj.getMaxpoints() );

        if( substitutedCompMinMaxPoints!=null )
        {
            maxPointsArray[0]=substitutedCompMinMaxPoints[1];
        }
        
        return maxPointsArray;
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
        Object[] out = new Object[11];
        out[0] = Float.valueOf(0);
        out[1] = Float.valueOf(0);
        out[2] = Float.valueOf(0);
        out[3] = Float.valueOf(0);
        
        if( this.radioButtonGroupObj != null )
        {
            out[0] = radioButtonGroupObj.getTruescore();
            out[1] =  radioButtonGroupObj.getScoreparam1();
            out[2] =  radioButtonGroupObj.getScoreparam2();
            out[3] =  radioButtonGroupObj.getScoreparam3();
            
            out[10] = radioButtonGroupObj.getTextscoreparam1();            
        }
        
        return out;
    }
    
    


    @Override
    public boolean measuresSmltCompetency( long simletCompetencyId )
    {
        if( simletCompetencyId() == simletCompetencyId )
            return true;
        
        return false;
    }

    @Override
    public long simletId()
    {
        return this.iactnResp==null || iactnResp.simletScore==null ? 0 : iactnResp.simletScore.simletId();
    }

    @Override
    public long simletCompetencyId()
    {
        return simletCompetencyScore == null ? 0 : simletCompetencyScore.competencyScoreObj.getId();
    }
    
    
    @Override
    public int getCt5SubtopicId()
    {
        return radioButtonGroupObj == null ? 0 : radioButtonGroupObj.getCt5Subtopicid();
    }
    
    
    @Override
    public long simCompetencyId()
    {
        return this.radioButtonGroupObj == null ? 0 : radioButtonGroupObj.getSimcompetencyid();
    }
        

    @Override
    public int simletItemTypeId()
    {
        return radioButtonGroupObj == null ? SimletItemType.NA.getSimletItemTypeId() : radioButtonGroupObj.getScoretype();
    }

    @Override
    public SimletItemType getSimletItemType()
    {

        return radioButtonGroupObj == null ? SimletItemType.NA : SimletItemType.getValue( radioButtonGroupObj.getScoretype() );

    }

    /**
     *
     * @return
     */
    @Override
    public boolean experimental()
    {
        return iactnResp.experimental();
    }

    
    public boolean getPartialCreditAssigned()
    {
        if( getSimletItemType().isDichotomous() )
            return false;
        
        if( getSimletItemType().isPoints() )
            return itemScore()>0 && itemScore()<getMaxPointsArray()[0];
        
        return false;
    }
    
    
    
    @Override
    public boolean correct()
    {
        try
        {
            if( selectedIactnItemResp == null )
                return false;

            if( getSimletItemType().isDichotomous()  )
            {
                return selectedIactnItemResp.intnItemObj.getIscorrect()==1;
            }

            // LogService.logIt( "RadioButtonGroup simletCompetencyScore=" + (simletCompetencyScore==null ? "null" : "not null " + simletCompetencyScore.toString() ) );

            // To support item analysis, for non-dichotomous items if itemscore=max points we can consider the item correct.
            if( simletCompetencyScore != null && SimCompetencyClass.getValue( simletCompetencyScore.competencyScoreObj.getClassid() ).getSupportsQuasiDichotomous() )
            {
                // Marked as correct
                if( selectedIactnItemResp.intnItemObj.getIscorrect()==1 )
                    return true;

                if( itemScore()==getMaxPointsArray()[0] && itemScore()>0 )
                    return true;
            }

            return false;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "RadioButtonGroup() " + toString() );
            return false;
        }
    }


    @Override
    public float itemScore()
    {
        // LogService.logIt( "RadioButtonGroupResp.itemScore() isPoints=" + getSimletItemType().isPoints() +", selectedIactnItemResp=" + (selectedIactnItemResp==null ? "null" : selectedIactnItemResp.toString() ) + ", " + toString() );
        
        if( getSimletItemType().isPoints() && selectedIactnItemResp != null )
        {
            float fv = selectedIactnItemResp.intnItemObj.getItemscore();

            if( radioButtonGroupObj.getTextscoreparam1() != null && radioButtonGroupObj.getTextscoreparam1().toLowerCase().indexOf( "[invert]" ) >= 0 )
                    fv = invertScore( fv );

            return fv;
        }

        return 0;
    }

    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return false;
    }


    @Override
    public boolean saveAsItemResponse()
    {
        // if this item is auto scorable
        return isAutoScorable();
    }


    @Override
    public boolean isAutoScorable()
    {
        //No competency or task score
        if( radioButtonGroupObj.getCompetencyscoreid()<=0 ) // && ( iactnResp.simletScore.simletTaskScoreList==null || iactnResp.simletScore.simletTaskScoreList.isEmpty() ) )
            return false;

        if( getSimletItemType().isPoints() )
        {
            if( validItemsCanHaveZeroMaxPoints )
                return true;

            return InteractionScoreUtils.hasAnyPointsValues( getMaxPointsArray() );
        }

        if( getSimletItemType().isDichotomous() )
        {
            for( IactnItemResp iir : iactnItemRespLst )
            {
                if( iir.intnItemObj.getIscorrect()==1 )
                    return true;
            }
        }

        return false;

    }


    private float invertScore( float fv )
    {
        try
        {
            String minStr = radioButtonGroupObj.getMinpoints();

            if( minStr == null || minStr.isEmpty() )
                return fv;

            float min = InteractionScoreUtils.getPointsArray( minStr )[0];

            float max = getMaxPointsArray()[0];

            if( max <= min || fv < min || fv > max )
                return fv;

            return min + (max - fv );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "RadioButtonGroupScore.invertScore() " + toString() );

            return fv;
        }
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

    public int getItemResponseTypeId()
    {
        // if the interaction timed out.
        if( iactnResp != null && iactnResp.getItemResponseTypeId() == ItemResponseType.NOT_ANSWERED_TIMEOUT.getItemResponseTypeId() )
            return ItemResponseType.NOT_ANSWERED_TIMEOUT.getItemResponseTypeId();

        // if this radio button group was answered.
        if( selectedIactnItemResp != null )
            return correct() ? ItemResponseType.ANSWERED2.getItemResponseTypeId() : ItemResponseType.ANSWERED.getItemResponseTypeId();

        // else, not answered.
        return ItemResponseType.NOT_ANSWERED.getItemResponseTypeId();
    }


    @Override
    public String getExtItemId()
    {
        if( selectedIactnItemResp != null )
            return selectedIactnItemResp.getExtItemId();
        
        return null;
    }
        
    
    @Override
    public String getSelectedExtPartItemIds()
    {
        if( selectedIactnItemResp != null )
            return selectedIactnItemResp.getSelectedExtPartItemIds();
        return null;
    }
            
    
    
    @Override
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId)
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

        String intnLevelQues = null;
        for( SimJ.Intn.Intnitem iitm : iactnResp.intnObj.getIntnitem() )
        {
            if( iitm.getIsquestionstem()==1 )
            {
                intnLevelQues = StringUtils.getUrlDecodedValue( iitm.getContent() );

                if( intnLevelQues!=null )
                    intnLevelQues = StringUtils.truncateStringWithTrailer(intnLevelQues, 255, true );
            }
        }
        
        String intnItemLevelQues = null; // UrlEncodingUtils.decodeKeepPlus( getExtItemId() );
        if( this.iactnResp!=null && this.iactnResp.intnObj!=null )
        {
            for( SimJ.Intn.Intnitem iitm : iactnResp.intnObj.getIntnitem() )
            {
                if( iitm.getRadiobuttongroup()!=radioButtonGroupObj.getRadiobuttongroupid() )
                    continue;

                if( iitm.getIsquestionstem()==1 )
                    continue;
                
                if( iitm.getFormat()==1 && iitm.getContent()!=null )
                {
                    intnItemLevelQues = StringUtils.truncateString( UrlEncodingUtils.decodeKeepPlus(iitm.getContent()), 255);
                    break;
                }                    
            }
        } 
        
        if( intnItemLevelQues==null || intnItemLevelQues.isBlank() )
            intnItemLevelQues = "Group " + (radioButtonGroupObj.getRadiobuttongroupid()+1);
        
        String title = itemLevelId;
        
        // Now combine.
        if( intnLevelQues!=null && !intnLevelQues.isBlank() )
            title += "\n" + intnLevelQues;

        if( intnItemLevelQues!=null && !intnItemLevelQues.isBlank() )
            title += "\n" + intnItemLevelQues;
        
        
        //String title = UrlEncodingUtils.decodeKeepPlus(getExtItemId());        
        //if( title == null || title.isEmpty() )
        //{
        //    title = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getUniqueid());
            
        //    if( title==null || title.isEmpty() )
        //        title = UrlEncodingUtils.decodeKeepPlus(iactnResp.intnObj.getId());

        //    if( title == null || title.isEmpty() )
        //        title = Integer.toString( iactnResp.intnObj.getSeq() );
            
        //    title += "_rbg" + (radioButtonGroupObj.getRadiobuttongroupid()+1);
        //}
        
        String text = null; 
        
        if( iist.isIncludeCorrect() )
            text = correct() ? "Correct" : (getPartialCreditAssigned() ? "Partial" : "Incorrect" );
            // text = correct() ? "Correct" : "Incorrect";
                
        else if( iist.isIncludeNumericScore() )
        {
            text = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1 ); //  Float.toString( itemScore() );
            // text = Float.toString( itemScore() );
        }

        else if( iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore( itemScore() );

        else if( iist.isResponseOrResponseCorrect() )
        {
            //if( this.iactnResp!=null )
            //{
            //    String ques = null;
            //    for( SimJ.Intn.Intnitem iitm : this.iactnResp.intnObj.getIntnitem() )
            //    {
            //        if( iitm.getIsquestionstem()==1 )
            //        {
            //            ques = StringUtils.getUrlDecodedValue( iitm.getContent() );

            //            if( ques!=null )
            //                ques = StringUtils.truncateStringWithTrailer(ques, 256, true );
            //        }
            //    }

            //    if( ques!=null )
            //        title = ques + " (" + title + ")";            
            //}            
                        
            text = getSelectedExtPartItemIds();
            
            if( ( text == null || text.isEmpty() ) && selectedIactnItemResp!=null )
                text = StringUtils.truncateString( selectedIactnItemResp.getIntnItemObj().getContent(), 20) + " (" + selectedIactnItemResp.getIntnItemObj().getSeq() + ")";
            
            if( iist.isResponseCorrect() )
                text += " (" + (correct() ? "Correct" : (getPartialCreditAssigned() ? "Partial" : "Incorrect" )) + ")";            
        }        
        
        if( text == null || text.isEmpty() )      
            return null;
        
        text = StringUtils.replaceStr( text, "[", "{" );
        title = StringUtils.replaceStr(title, "[", "{" );
        itemLevelId = StringUtils.replaceStr(itemLevelId, "[", "{" );
        intnLevelQues = StringUtils.replaceStr(intnLevelQues, "[", "{" );
        intnItemLevelQues = StringUtils.replaceStr(intnItemLevelQues, "[", "{" );
                
        return new TextAndTitle( text, title, 0, itemLevelId, intnLevelQues, intnItemLevelQues );        
    }

    
    
    
    
    @Override
    public void populateItemResponse( ItemResponse ir )
    {
        iactnResp.populateItemResponseCore( ir );

         ir.setResponseLevelId( ResponseLevelType.RADIOBUTTONGROUP.getResponseLevelId() );

         ir.setIdentifier( ResponseLevelType.RADIOBUTTONGROUP.computeIdentifier( ir, ( radioButtonGroupObj.getRadiobuttongroupid()+1) ) );

         if( simletCompetencyScore != null )
           ir.setSimCompetencyId( simletCompetencyScore.competencyScoreObj.getSimcompetencyid() );

         else if( iactnResp.simletCompetencyScore!=null )
             ir.setSimCompetencyId( iactnResp.simletCompetencyScore.competencyScoreObj.getSimcompetencyid() );

        ir.setCompetencyScoreId( simletCompetencyId() );

        ir.setItemResponseTypeId( getItemResponseTypeId() );

        // ir.setTrueScore( iactnResp.intnObj.getTruescore() );

        ir.setRadioButtonGroupId( radioButtonGroupObj.getRadiobuttongroupid() + 1 );

        ir.setItemScore( itemScore() );

         ir.setItemParadigmTypeId( ScoredItemParadigmType.getValue(this).getScoredItemParadigmTypeId() );


        if( selectedIactnItemResp != null )
        {
           ir.setSelectedSubnodeSeqIds( Integer.toString( selectedIactnItemResp.intnItemObj.getSeq() ) );
           ir.setScoreParam1( selectedIactnItemResp.intnItemObj.getScoreparam1());
           ir.setScoreParam2( selectedIactnItemResp.intnItemObj.getScoreparam2() );
           ir.setScoreParam3( selectedIactnItemResp.intnItemObj.getScoreparam3() );
           ir.setSelectedValue(StringUtils.truncateString(UrlEncodingUtils.decodeKeepPlus( selectedIactnItemResp.intnItemObj.getContent() ), 1900 ) );
           ir.setSubnodeSeq( selectedIactnItemResp.intnItemObj.getSeq() );
           ir.setSubnodeFormatTypeId( selectedIactnItemResp.g2ChoiceFormatType.getG2ChoiceFormatTypeId() );

           ir.setSelectedSubFormatTypeIds( Integer.toString( ir.getSubnodeFormatTypeId() ) );
        }

        ir.setSimletItemTypeId( simletItemTypeId() );

        if( getSimletItemType().isDichotomous() )
        {
            ir.setCorrect( correct() ? 1 : 0 );

            String crctSubSeqs = "";

            for( SimJ.Intn.Intnitem iir : iactnResp.intnObj.getIntnitem() )
            {
                if( iir.getRadiobuttongroup() == radioButtonGroupObj.getRadiobuttongroupid() )
                {
                    if( iir.getIscorrect()==1 )
                    {
                        if( crctSubSeqs.length() > 0 )
                            crctSubSeqs += ";";

                        crctSubSeqs += iir.getSeq();
                    }
                }
            }

            ir.setCorrectSubnodeSeqIds(crctSubSeqs);
        }

        // To support item analysis, for non-dichotomous items if itemscore=max points we can consider the item correct.
        else if( simletCompetencyScore != null &&
                 SimCompetencyClass.getValue( simletCompetencyScore.competencyScoreObj.getClassid() ).getSupportsQuasiDichotomous() )
        {
            if( itemScore() == getMaxPointsArray()[0] && itemScore()>0 )
            {
                ir.setCorrect(1);
                ir.setCorrectSubnodeSeqIds( ir.getSelectedSubnodeSeqIds() );
            }
        }



    }


    @Override
    public float getAggregateItemScore( SimCompetencyClass simCompetencyClass )
    {
        if( selectedIactnItemResp == null )
            return 0;

        return simCompetencyClass.getAggregatePoints( selectedIactnItemResp );
    }




   /**
     * Returns - unless the scorableresponse has a textScoreParam1 of [SCALEDSCOREFLOOR]value  where value is the min value for the scaled competency score that is to be
     * enforced based on the fact that this person made this selection.
     * @return
     */
    @Override
    public float getFloor()
    {
        if( selectedIactnItemResp != null )
            return selectedIactnItemResp.getFloor();

        return 0;
        //String s = StringUtils.getBracketedArtifactFromString( radioButtonGroupObj.getTextscoreparam1() , Constants.SCALEDSCOREFLOOR );

        //return s == null || s.length()==0 ? 0 : Float.parseFloat(s);
    }


    /**
     * Returns - unless the scorableresponse has a textScoreParam1 of [SCALEDSCORECEILING]value  where value is the max value for the scaled competency score that is to be
     * enforced based on the fact that this person made this selection.
     * @return
     */
    @Override
    public float getCeiling()
    {
        if( selectedIactnItemResp != null )
            return selectedIactnItemResp.getCeiling();

        return 0;
        //String s = StringUtils.getBracketedArtifactFromString( radioButtonGroupObj.getTextscoreparam1() , Constants.SCALEDSCORECEILING );

        //return s == null || s.length()==0 ? 0 : Float.parseFloat(s);
    }


    /**
     * Returns null - unless the scorableresponse has a textScoreParam1 of [SCORETEXTCAVEAT]value sentence  -  where value sentence is a sentence that should be appended to
     * the scoretext for this competency in any report.
     * @return
     *
    @Override
    public String getCaveatText()
    {
        if( selectedIactnItemResp != null )
            return selectedIactnItemResp.getCaveatText();

        return null;
    }
    */
    

    @Override
    public List<CaveatScore> getCaveatScoreList()
    {
        if( selectedIactnItemResp != null )
            return selectedIactnItemResp.getCaveatScoreList();

        return new ArrayList<>();
    }


    @Override
    public InterviewQuestion getScoreTextInterviewQuestion()
    {
        if( selectedIactnItemResp != null )
            return selectedIactnItemResp.getScoreTextInterviewQuestion();

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

    public IactnResp getIactnResp() {
        return iactnResp;
    }

    
    
    public IactnItemResp getSelectedIactnItemResp() {
        return selectedIactnItemResp;
    }

    @Override
    public float getTotalItemCountIncrementValue() 
    {
        return 1;
    }

}
