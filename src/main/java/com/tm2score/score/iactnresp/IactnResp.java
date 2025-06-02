/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ResponseLevelType;
import com.tm2score.service.LogService;
import com.tm2score.sim.InteractionScoreUtils;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.SimletItemType;
import com.tm2score.simlet.SimletSubnodeType;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.interview.InterviewQuestion;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.ct5.Ct5ItemType;
import com.tm2score.ct5.event.Ct5ResumeUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.ScoreManager;
import com.tm2score.score.ScoreUtils;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.SimletCompetencyScore;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.xml.IntnHist;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Mike
 */
public class IactnResp implements ScorableResponse
{

    // This is the data from the result xml
    public Clicflic.History.Intn intnResultObjO = null;
    public IntnHist intnResultObj = null;
    public TestEvent testEvent;

    protected SimJ simJ;

    // This is the interaction inside the sim
    public SimJ.Intn intnObj = null;

    // This is the simlet within which the interaction is included.
    protected SimletScore simletScore = null;

    // This is the competencyScore object for this item, if any.
    public SimletCompetencyScore simletCompetencyScore = null;
    public SimletCompetencyScore overrideSimletCompetencyScore = null;

    // This is the interaction item that was 'clicked' to submit this item response.
    // This is sometimes the scored interaction item and sometimes it is just a submit button.
    protected IactnItemResp clickedIactnItemResp = null;

    // this is a list of all interaction items that have input or score data or are individually scored.
    protected List<IactnItemResp> iactnItemRespLst = null;

    protected List<RadioButtonGroupResp> radBtnGrpScrList = null;

    protected List<PrevIactnResp> prvIactnRespList = null;

    protected List<String> forcedRiskFactorsList = null;

    protected float[] maxPointsArray;

    /**
     * Used to store score2, score3, etc.
     * metaScores[2] = item score 2
     * metaScores[3] = item score 3
     * metaScores[4] = item score 4
     * metaScores[5] = item score 5
     * metaScores[6] = item score 6
     */
    protected float[] metaScores;

    protected boolean validItemsCanHaveZeroMaxPoints = false;

    protected String teIpCountry;



    public IactnResp( Clicflic.History.Intn iob, TestEvent testEvent)
    {
        intnResultObjO = iob;
        if( iob!=null )
            intnResultObj = new IntnHist(iob);

        this.testEvent=testEvent;
    }

    @Override
    public String toString()
    {
        return "IactnResp{ " + ( intnObj == null ? " intn is null" :  intnObj.getName() + ", id=" + intnObj.getId() + ", nodeSeq=" + intnObj.getSeq() ) + ( intnResultObj==null ? " intnResultObj is null" : ", sel SubSeq=" + intnResultObj.getSnseq() ) + ", ct5ItemId=" + this.getCt5ItemId() + ", ct5ItemPartId=" + this.getCt5ItemPartId() + "}";
    }


    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle()
    {
        return false;
    }


    @Override
    public String getTopic()
    {
        if( intnObj==null )
            return null;

        String tsp = intnObj.getTextscoreparam1();

        if( tsp==null || tsp.trim().isEmpty() )
            return null;

        return IvrStringUtils.getTagValueWithDecode(tsp, Constants.TOPIC_KEY );
    }

    /**
     * map of topic name, int[]
     *    int[0] = number correct
     *    int[1] = number total this topic.
     *    int[2] = number of items that were partially correct.
     *    int[3] = Total number of items this iactn resp this topic
     */
    @Override
    public Map<String,int[]> getTopicMap()
    {
        return ScoreUtils.getSingleTopicTopicMap( getTopic(), correct(), getPartialCreditAssigned() );
    }


    @Override
    public boolean getUsesOrContributesPointsToSimletCompetency( SimletCompetencyScore smltCs )
    {
        // check if this is a multiple choice or multi correct checkbox with points.
        if( smltCs.getCompetencyScoreType()!=null && smltCs.getCompetencyScoreType().isPointAccum() && intnObj.getCt5Itemtypeid()==Ct5ItemType.MULT_CHOICE.getCt5ItemTypeId() )
        {
            // Nothing selected, return match based on item level.
            if( getSelectedIntnItems()==null || getSelectedIntnItems().isEmpty() )
                return simletCompetencyId()==smltCs.competencyScoreObj.getId();

            // see if selected radio has a different competencyscoreid
            for( IactnItemResp ir : getSelectedIntnItems() )
            {
                if( !ir.getG2ChoiceFormatType().getIsAnyRadio())
                    continue;

                //LogService.logIt( "IactnResp.getUsesOrContributesPointsToSimletCompetency() itemId=" + this.intnObj.getUniqueid() + ", ir.intnItemObj.getCompetencyscoreid()=" + ir.intnItemObj.getCompetencyscoreid() + ", simletCompetencyId()=" + simletCompetencyId() + ", smltCs.competencyScoreObj.getId()=" + smltCs.competencyScoreObj.getId() + " smltCs=" + smltCs.competencyScoreObj.getName() );
                // has custom not same as the item-level.
                if( ir.intnItemObj.getCompetencyscoreid()>0 && ir.intnItemObj.getCompetencyscoreid()!=simletCompetencyId() )
                {
                    if( ir.intnItemObj.getCompetencyscoreid()==smltCs.competencyScoreObj.getId() )
                    {
                        //LogService.logIt( "IactnResp.getUsesOrContributesPointsToSimletCompetency() itemId=" + this.intnObj.getUniqueid() + ", custom competencyscoreid, matches the calling SimletCs. returning true. " );
                        overrideSimletCompetencyScore = smltCs;
                        return true;
                    }

                    //LogService.logIt( "IactnResp.getUsesOrContributesPointsToSimletCompetency() itemId=" + this.intnObj.getUniqueid() + ", custom competencyscoreid does NOT match the calling SimletCs. returning false. " );
                    return false;
                }

                // no custom, use item level
                else
                {
                    //LogService.logIt( "IactnResp.getUsesOrContributesPointsToSimletCompetency() itemId=" + this.intnObj.getUniqueid() + ", no custom competencyscoreid, returning " + (simletCompetencyId()==smltCs.competencyScoreObj.getId()) );
                    return simletCompetencyId()==smltCs.competencyScoreObj.getId();
                }
            }
        }

        // check if this is a multi correct checkbox with points.
        /* NOTE - This will only work if the individual checkboxes are considered to be true/false items.
        if( smltCs.getCompetencyScoreType()!=null && smltCs.getCompetencyScoreType().isPointAccum() && intnObj.getCt5Itemtypeid()==Ct5ItemType.MULT_CORRECT_ANSWER.getCt5ItemTypeId() && intnObj.getMultiplechoiceformat()==Ct5MultipleChoiceFormatType.CHECKBOXES.getCt5MultipleChoiceFormatTypeId() )
        {
            // Nothing checked, return match on item.
            if( getSelectedIntnItems()==null || getSelectedIntnItems().isEmpty() )
                return simletCompetencyId()==smltCs.competencyScoreObj.getId();

            // see if it has ANY custom topics.
            boolean hasAnyCustom = false;
            boolean hasMatch = false;
            for( IactnItemResp ir : getSelectedIntnItems() )
            {
                if( !ir.getG2ChoiceFormatType().getIsAnyCheckbox() )
                    continue;

                // has any custom not same as the item.
                if(ir.intnItemObj.getCompetencyscoreid()>0 && ir.intnItemObj.getCompetencyscoreid()!=simletCompetencyId() )
                {
                    hasAnyCustom=true;

                    // see if there is a match
                    if( ir.intnItemObj.getCompetencyscoreid()==smltCs.competencyScoreObj.getId() )
                    {
                        overrideSimletCompetencyScore = smltCs;
                        hasMatch = true;
                    }
                }
                // this checkbox uses item level, so see if there is a match.
                else if( simletCompetencyId()==smltCs.competencyScoreObj.getId() )
                    hasMatch=true;
            }

            // no custom values set, use the item-level setting.
            if( !hasAnyCustom )
                return simletCompetencyId()==smltCs.competencyScoreObj.getId();
            else
                return hasMatch;
        }
        */

        // standard check.
        return this.simletCompetencyId()==smltCs.competencyScoreObj.getId();
    }


    @Override
    public float getDisplayOrder()
    {
        return this.intnResultObj.getSeq();
    }

    @Override
    public int getCt5ItemId()
    {
        return this.intnObj==null ? 0 : intnObj.getCt5Itemid();
    }

    @Override
    public int getCt5ItemPartId()
    {
        return 0;
    }





    /**
     * After init is called, this IactnResp will contain the intnObj, SimletScore,
     * SimletCompetencyScore, and  all interaction item responses that are relevant
     * for either scoring or manual report preparation.
     *
     */
    public void init( SimJ sj , List<SimletScore> simletScoreList, TestEvent te, boolean validItemsCanHaveZeroMaxPoints ) throws Exception
    {
        try
        {
            simJ = sj;

            if( te!=null )
                teIpCountry = te.getIpCountry();

            this.validItemsCanHaveZeroMaxPoints = validItemsCanHaveZeroMaxPoints;

            // Next, look first by unique ids - this implies that the SimJ object has changed a bit. So be sure to use the
            if( intnResultObj.getUnqid()!=null && !intnResultObj.getUnqid().isBlank())
            {
                int ct = 0;
                SimJ.Intn ii=null ;

                for( SimJ.Intn intn : sj.getIntn() )
                {
                    if( intn.getUniqueid()!= null && !intn.getUniqueid().isEmpty() && intn.getUniqueid().equals( intnResultObj.getUnqid() ) )
                    {
                        // LogService.logIt( "IactnResp.init() FOUND Sim.intn by uniqueId=" + intnResultObj.getUnqid()  );
                        ct++;
                        ii=intn;
                        // intnObj = intn;
                        // break;
                    }
                }

                // only accept if unique
                if( ct==1 && ii!=null)
                    intnObj=ii;
            }

            if( intnObj == null )
            {
                // next find the interaction in the descriptor
                for( SimJ.Intn intn : sj.getIntn() )
                {
                    if( intn.getSeq() == intnResultObj.getNdseq() )
                    {
                        intnObj = intn;
                        break;
                    }
                }
            }

            if( intnObj==null && intnResultObj.getUnqid()!=null && !intnResultObj.getUnqid().isBlank() )
                intnObj = Ct5ResumeUtils.getResumeIntnByUniqueId(intnResultObj.getUnqid());
            
            // next get all IactnItemResp objects
            iactnItemRespLst = new ArrayList<>();

            // Not an important interaction response then. Ignore.
            if( intnObj == null  )
            {
                LogService.logIt( "IactnResp.init() could not find an interaction Object in SimDescriptor for seq=" + intnResultObj.getNdseq() );
                return;
            }

            // LogService.logIt( "IactnResp.init() AAA.1 uniqueId=" + intnObj.getUniqueid() );


            // LogService.logIt( "TestEvent.initScoreAndResponseLists() AAA.2 uniqueId=" + intnObj.getUniqueid() );
            if( intnObj.getTextscoreparam1() != null && !intnObj.getTextscoreparam1().isEmpty() )
                intnObj.setTextscoreparam1( UrlEncodingUtils.decodeKeepPlus(intnObj.getTextscoreparam1(), "UTF8") );


            if( intnObj.getSimletid() > 0 )
            {
                for( SimletScore ss : simletScoreList )
                {
                    if( ss.simletObj.getId() == intnObj.getSimletid() )
                    {
                        simletScore = ss;

                        if( intnObj.getCompetencyscoreid() > 0 )
                            simletCompetencyScore = ss.getSimletCompetencyScore( intnObj.getCompetencyscoreid() );

                        break;
                    }
                }                
            }


            // This is possible since interaction could be from a sim template. However, in this case we will ignore it since Sim Templates do not
            // afford any capability to add metadata to interactions for use in scoring or reports..
            if( simletScore == null )
            {
                LogService.logIt( "IactnResp.init() could not find a SimletScore for this IactnResp looking for simletId=" + intnObj.getSimletid() + ", intnObj.seq=" + intnObj.getSeq() + ", " + intnObj.getName() );
                return;
            }

            IactnItemResp iir;

            boolean keep;

            //boolean slctd;

            // for each interaction item
            for( SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem() )
            {
                keep = false;

                //slctd = false;

                // for simlet competency interactions
                // Note - When a simlet measures a competency that is not measured by this sim, the
                // interaction is included with competencyscoreid=0 but scoretype >0. This interaction is
                // not used in auto scoring for a competency but may be used for computing task scores, so include it.
                if( intnObj.getCompetencyscoreid()>0 || intnObj.getScoretype()> 0 )
                {
                    if( intItemObj.getSmltiactnitmtypeid()> 0)
                        keep=true;

                    else if( getG2ChoiceFormatType( intItemObj.getFormat() ).supportsNodeLevelSimletAutoScoring() )
                        keep=true;

                    else if( getG2ChoiceFormatType( intItemObj.getFormat() ).getIsFormInputCollector() )
                        keep = true;

                    //else if( intItemObj.getDrgtgt()==1 ) // intItemObj.getRadiobuttongroup() > 0 )
                    //    keep = true;
                }

                // Any File Upload Button.
                else if( getG2ChoiceFormatType( intItemObj.getFormat() ).getIsFileUpload() ) // intItemObj.getRadiobuttongroup() > 0 )
                    keep = true;

                // Anything dragable should be kept.
                else if( intItemObj.getDragable()==1 ) // intItemObj.getRadiobuttongroup() > 0 )
                    keep = true;

                // Any drag target should be kept.
                else if( intItemObj.getDrgtgt()==1 ) // intItemObj.getRadiobuttongroup() > 0 )
                    keep = true;

                // if it has a radio button group number assigned, keep it.
                else if( getG2ChoiceFormatType( intItemObj.getFormat() ).getIsAnyRadio() ) // intItemObj.getRadiobuttongroup() > 0 )
                    keep = true;

                // Any choice that supports subnode-level and has a competencyscoreid
                else if( getG2ChoiceFormatType( intItemObj.getFormat() ).supportsSubnodeLevelSimletAutoScoring() && intItemObj.getCompetencyscoreid() > 0 )
                    keep = true;

                // for non-competency interactions
                else if( intnObj.getNoncompetencyquestiontypeid()>0 )
                {
                    // Non-competency interactions must have a SimletInterationItemTypeId set in each interaction item.
                    if( intItemObj.getSmltiactnitmtypeid()> 0)
                        keep=true;
                }

                // Any interaction item that has its correct field designated should be include.
                else if( intItemObj.getIscorrect() > 0 )
                    keep=true;

                // if this is the clicked interaction item, always keep.
                if( intnResultObj.getSnseq() == intItemObj.getSeq() )
                    keep = true;

                if( !keep )
                    continue;

                iir =  IactnRespFactory.getIactnItemResp(this, intItemObj, intnResultObjO, testEvent );

                iir.init( simletScore, te );

                if( ScoreManager.DEBUG_SCORING )
                   LogService.logIt( "IactnResp.init() BBB.1 "  + this.intnObj.getName() + " " + this.intnObj.getSeq()  + " ADDING " + iir.toString() );
                iactnItemRespLst.add( iir );

                if( intnResultObj.getSnseq()>0 && intItemObj.getSeq()==intnResultObj.getSnseq() )
                    clickedIactnItemResp = iir;
            }

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "IactnResp.init() BBB.2 "  + this.intnObj.getName() + " " + this.intnObj.getSeq()  + " iactnItemRespLst contains " + iactnItemRespLst.size() );

            // Setup the Radio Button Groups. Must have more than one.
            if( intnObj.getRadiobuttongroup()!=null && intnObj.getRadiobuttongroup().size()>1 )
            {
                radBtnGrpScrList = new ArrayList<>();

                RadioButtonGroupResp rbgs;

                for( SimJ.Intn.Radiobuttongroup rbgo : intnObj.getRadiobuttongroup() )
                {
                    rbgs = new RadioButtonGroupResp( this, rbgo);

                    rbgs.init(te);

                    radBtnGrpScrList.add(rbgs);
                }
            }

            // now do prev sub seqs
            if( intnObj.getIncprevselections()==1 &&
                intnResultObj.getPrvsubseqs() != null &&
                !intnResultObj.getPrvsubseqs().trim().isEmpty() )
            {
                prvIactnRespList = new ArrayList<>();

                String[] vals = intnResultObj.getPrvsubseqs().split( "~" );

                int v;

                PrevIactnResp pir;

                // IactnItemResp iir;

                for( int i=0; i<vals.length; i++ )
                {
                    try
                    {
                        v = Integer.parseInt( vals[i] );

                        // LogService.logIt( "IactnResp.init() found PrevIactnResp seq=" + v + " " + (clickedIactnItemResp == null) );
                        //
                        if( clickedIactnItemResp == null || clickedIactnItemResp.intnItemObj.getSeq()== v )
                            continue;

                        for( IactnItemResp ii  : iactnItemRespLst )
                        {
                            if( ii.intnItemObj.getSeq() == v ) // intnResultObj.getSnseq() )
                            {
                                pir = new PrevIactnResp(this, ii, (prvIactnRespList.size()+1 ));

                                pir.init(te);
                                // LogService.logIt( "IactnResp.init() ADDING PrevIactnResp seq=" + v );
                                prvIactnRespList.add( pir );
                                break;
                            }
                        }
                    }

                    catch( NumberFormatException e )
                    {
                        LogService.logIt( e, "IactnResp.init() Error parsing prevsubseqs list. " +  intnResultObj.getPrvsubseqs() + ", current size=" + prvIactnRespList.size() );
                    }
                }
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IactnResp.init() " + toString() );
            throw e;
        }
    }


    @Override
    public void calculateScore() throws Exception
    {}



    @Override
    public boolean requiresMaxPointIncrement()
    {
        return false;
    }



    @Override
    public List<MergableScoreObject> getMergableScoreObjects()
    {
        return new ArrayList<>();
    }


    /**
     * Note that this is for interactions scored at the interaction level only.
     * @return
     */
    @Override
    public synchronized float[] getMaxPointsArray()
    {
        if( maxPointsArray != null )
            return maxPointsArray;

        if( intnObj==null || intnObj.getMaxpoints() == null || intnObj.getMaxpoints().isEmpty() )
            return new float[4];

        maxPointsArray = InteractionScoreUtils.getPointsArray( intnObj.getMaxpoints() );

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

        if( this.intnObj != null )
        {
            out[0] = (float)( intnObj.getTruescore() );
            out[1] = (float)( intnObj.getScoreparam1() );
            out[2] = (float)( intnObj.getScoreparam2() );
            out[3] = (float)( intnObj.getScoreparam3() );

            out[10] = intnObj.getTextscoreparam1();
        }

        return out;
    }


    public List<PrevIactnResp> getPrevIactRespList()
    {
        return prvIactnRespList == null ? new ArrayList<>() : prvIactnRespList;
    }


    public boolean hasMultipleIactnLevelScores()
    {
       return intnObj.getIncprevselections()==1 && prvIactnRespList != null && !prvIactnRespList.isEmpty();
    }

    public Locale getSimLocale()
    {
        String lang = simJ==null ? null : simJ.getLang();

        if( lang!=null && !lang.isBlank() )
            return I18nUtils.getLocaleFromCompositeStr(lang);
        return null;
    }

    @Override
    public boolean saveAsItemResponse()
    {
        if( !hasValidScore() )
            return false;

        // if this item is auto scorable
        if( isAutoScorable() )
            return true;

        // if this item has any interaction items that are themselves auto scorable.
        if( !getAllScorableIntItemResponses().isEmpty() )
            return true;

        return false;
    }

    public SimletScore getSimletScore() {
        return simletScore;
    }

    public boolean getIsRedFlag()
    {
        if( clickedIactnItemResp != null )
            return clickedIactnItemResp.getIsRedFlag();

        return false;
    }

    public SimJ.Intn getIntnObj()
    {
        return intnObj;
    }


    public List<IactnItemResp> getIactnItemRespLst()
    {
        return iactnItemRespLst;
    }


    protected float invertScore( float fv )
    {
        try
        {
            String minStr = intnObj.getMinpoints();

            float min = 0;

            //if( minStr == null || minStr.isEmpty() )
            //    return fv;

            if( minStr != null && !minStr.isBlank() )
                min = InteractionScoreUtils.getPointsArray( minStr )[0];


            //if( minStr == null || minStr.isEmpty() )
            //    return fv;

            //float min = InteractionScoreUtils.getPointsArray( minStr )[0];

            float max = getMaxPointsArray()[0];

            if( max <= min || fv < min || fv > max )
                return fv;

            return min + (max - fv );
        }

        catch( Exception e )
        {
            LogService.logIt(e, "IactnResp.invertScore() " + toString() );

            return fv;
        }
    }




    public String getSvValue( int idx )
    {
        if( intnResultObj == null )
            return null;

        String r = null;

        if( idx == 1 )
            r = intnResultObj.getSv1();

        if( idx == 2 )
            r = intnResultObj.getSv2();

        if( idx == 3 )
            r = intnResultObj.getSv3();

        if( idx == 4 )
            r = intnResultObj.getSv4();

        if( idx == 5 )
            r = intnResultObj.getSv5();

        if( idx == 6 )
            r = intnResultObj.getSv6();

        if( idx == 7 )
            r = intnResultObj.getSv7();

        if( idx == 8 )
            r = intnResultObj.getSv8();

        if( idx == 9 )
            r = intnResultObj.getSv9();

        if( idx == 10 )
            r = intnResultObj.getSv10();

        if( idx == 11 )
            r = intnResultObj.getSv11();

        if( idx == 12 )
            r = intnResultObj.getSv12();

        return UrlEncodingUtils.decodeKeepPlus( r );
    }





    public G2ChoiceFormatType getG2ChoiceFormatType( int formatTypeId )
    {
        return G2ChoiceFormatType.getValue(formatTypeId);
    }


    /*
     * An interaction item is scorable if it is:
     *   1. It is associated with a simletCompetencyScore and has an itemtype compatible with CompetencyScore
     *   2.
     *
     *
     *
     */
    @Override
    public boolean isAutoScorable()
    {
        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "IactnResp.isAutoScorable() AAA.1 intnObj=" + (intnObj==null ? "null" : intnObj.getName()) );

        if( intnObj==null )
            return false;

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "IactnResp.isAutoScorable() AAA.2  intnObj.getCompetencyscoreid()=" + intnObj.getCompetencyscoreid() + ", hasCheckboxDragTargets()=" + hasCheckboxDragTargets() );

        //No competency or task score
        if( intnObj.getCompetencyscoreid()<=0 ) // && (simletScore.simletTaskScoreList==null || simletScore.simletTaskScoreList.isEmpty() )  )
            return false;

        if( hasCheckboxDragTargets() )
            return false;

        SimletItemType sit = getSimletItemType();

        if( ScoreManager.DEBUG_SCORING )
            LogService.logIt( "IactnResp.isAutoScorable() AAA.3 " + intnObj.getName() + ", SimletItemType=" + sit.getName() +  ",  SimletItemType.isPoints()=" + sit.isPoints() );

        if( sit.isPoints() )
        {

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "IactnResp.isAutoScorable() AAA.4 " + intnObj.getName() +  " IS POINTS validItemsCanHaveZeroMaxPoints=" +  validItemsCanHaveZeroMaxPoints + ", " );

            // this can indicate a survey
            if( validItemsCanHaveZeroMaxPoints )
                return true;

            return InteractionScoreUtils.hasAnyPointsValues( getMaxPointsArray() ) || (simletCompetencyScore!=null && simletCompetencyScore.getSimletCompetencyClass().isUnscored() );
        }

        // if dichotomous, must have a correct choice indicated.
        if( sit.isDichotomous() )
        {
            //if( hasDichotomousDragTargets() )
            //  LogService.logIt( "IactnResp.isAutoScorable() " + toString() + ", has DICHOTOMOUS DRAG TARGETS! ");

            if( ScoreManager.DEBUG_SCORING )
                LogService.logIt( "IactnResp.isAutoScorable() AAA.6 " + intnObj.getName() +  " IS Dichotomous. hasCorrectInteractionItems()=" + hasCorrectInteractionItems() + ",  validItemsCanHaveZeroMaxPoints=" +  validItemsCanHaveZeroMaxPoints + ", " );
            
            
            // this indicates a survey
            if( validItemsCanHaveZeroMaxPoints )
                return true;

            if( hasDichotomousDragTargets() )
            {
                if( hasCheckboxDragTargets() )
                    return false;

                return true;
            }

            return hasCorrectInteractionItems() || (simletCompetencyScore!=null && simletCompetencyScore.getSimletCompetencyClass().isUnscored() );
        }

        // if( getSimletItemType().supportsAutoScoring() )
        if( sit.isTyping() || sit.isDataEntry() )
            return true;

        if( sit.isAutoEssay() )
            return true;

        if( sit.isImageCapture() )
            return true;

        // at this point, there is nothing to score.
        return false; // getSimletItemType().supportsAutoScoring();
    }

    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        // if simlet item type is dichotomous, or if there is not simlet itemtype and has correct
        return getSimletItemType().isDichotomous() || ( getSimletItemType().equals( SimletItemType.NA ) && hasCorrectInteractionItems() );
    }



    public boolean getHasMultipleRadioButtonGroups()
    {
        return radBtnGrpScrList != null && !radBtnGrpScrList.isEmpty();
    }


    public List<IactnItemResp> getDragTargetIntItems()
    {
        List<IactnItemResp> out = new ArrayList<>();

        for( IactnItemResp iir : this.iactnItemRespLst )
        {
            if( iir.isDragTarget()  )
                out.add( iir );
        }

        return out;
    }

    public boolean hasDragTargets()
    {
        for( IactnItemResp iir : this.iactnItemRespLst )
        {
            if( iir.isDragTarget() )
                return true;
        }

        return false;
    }

    public boolean hasCheckboxDragTargets()
    {
        for( IactnItemResp iir : this.iactnItemRespLst )
        {
            if( iir.isDragTarget() && iir.intnItemObj.getDrgtgtCorrectseqids()!= null && !iir.intnItemObj.getDrgtgtCorrectseqids().isEmpty() && iir.intnItemObj.getDrgtgtcheckbox()==1 )
                return true;
        }

        return false;
    }

    public boolean hasDichotomousDragTargets()
    {
        for( IactnItemResp iir : this.iactnItemRespLst )
        {
            if( iir.isDragTarget() && iir.intnItemObj.getDrgtgtCorrectseqids()!= null && !iir.intnItemObj.getDrgtgtCorrectseqids().isEmpty() )
                return true;
        }

        return false;
    }


    @Override
    public boolean isPendingExternalScore()
    {
        return false;
    }

    public List<IactnItemResp> getSelectedIntnItems()
    {
        List<IactnItemResp> out = new ArrayList<>();

        List<Integer> selSnSqLst = getSelectedSnSeqs();

        // LogService.logIt( "IactnResp.getSelectedIntnItems() BBB intn.unique=" + this.intnObj.getUniqueid() + ", size=" + selSnSqLst.size() + ", selectedSubnodeSeq=" + intnResultObj.getSnseq() );

        for( IactnItemResp iir : iactnItemRespLst )
        {
            // skip intn items at radio button group level
            if( iir.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                continue;

            // skip int items scored at that level.
            if( iir.supportsSubnodeLevelSimletAutoScoring() && iir.isAutoScorable( ) )
                continue;

            if( !iir.g2ChoiceFormatType.getIsClickable() )
                continue;

            // LogService.logIt( "IactnResp.getSelectedIntnItems() have Eligible seq: " + iir.intnItemObj.getSeq() + ", c1: " + (iir.intnItemObj.getSeq() == intnResultObj.getSnseq()) + ", c2: " +  selSnSqLst.contains( new Integer( iir.intnItemObj.getSeq() ) ) );

            if( iir.intnItemObj.getSeq()==intnResultObj.getSnseq() || selSnSqLst.contains( iir.intnItemObj.getSeq() ) )
                out.add( iir );
        }

        return out;
    }

    public List<Integer> getSelectedSnSeqs()
    {
        if( hasDragTargets() )
            return getDragTargetTenantSubnodeSeqs();

        List<Integer> out = new ArrayList<>();

        try
        {
            String v;

            // look for check radio buttons or checkboxes
            if( intnResultObj!=null && intnResultObj.getValue()!=null && !intnResultObj.getValue().isEmpty() )
            {
                String[] toks = intnResultObj.getValue().split( "~" );

                for( int i=0; i<toks.length-1; i+=2 )
                {
                    if( toks[i].trim()==null )
                        continue;

                    v = toks[i+1];

                    // only true means checked or clicked
                    if( v.equalsIgnoreCase( "true" ) )
                        out.add( Integer.parseInt(toks[i]) );
                }
            }

            // if list doesn't contain what was clicked or there is nothing contained.
            if( this.clickedIactnItemResp != null && !out.contains( clickedIactnItemResp.intnItemObj.getSeq() ) )
            {
                if( clickedIactnItemResp.intnItemObj.getFormat()!=G2ChoiceFormatType.SUBMIT.getG2ChoiceFormatTypeId() || out.isEmpty() )
                    out.add( clickedIactnItemResp.intnItemObj.getSeq() );
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IactnResp.getSelectedSnSeqs() " + toString()  );
        }

        return out;
    }






    public boolean hasCorrectInteractionItems()
    {
        return hasXInteractionItems( 0 );
    }

    public boolean hasInteractionItemScores()
    {
        return hasXInteractionItems( 1 );
    }

    /**
     * typeId=0 means has correct,
     * typeId=1 means has itemscore
     * @param typeId
     * @return
     */
    protected boolean hasXInteractionItems( int typeId )
    {
        if( intnObj==null )
            return false;

        for( IactnItemResp iir : iactnItemRespLst )
        {
            // skip intn items at radio button group level
            if( iir.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                continue;

            // skip int items scored at that level.
            if( iir.supportsSubnodeLevelSimletAutoScoring() && iir.isAutoScorable( ) )
                continue;

            if( !iir.g2ChoiceFormatType.getIsClickable() || iir.g2ChoiceFormatType.getIsSubmit() )
                continue;

            if( typeId==0 && iir.intnItemObj.getIscorrect()!=0 )
                return true;

            if( typeId==1 && iir.intnItemObj.getItemscore()!=0 )
                return true;

        }

        if( typeId==0 && hasDichotomousDragTargets() )
            return true;

        return false;
    }



    public List<RadioButtonGroupResp> getAllScorableRadioButtonGroupResponses()
    {
        List<RadioButtonGroupResp> out = new ArrayList<>();

        if( radBtnGrpScrList==null || radBtnGrpScrList.isEmpty() )
            return out;

        for( RadioButtonGroupResp rbgs : radBtnGrpScrList )
        {
            if( rbgs.isAutoScorable( ) )
                out.add( rbgs );
        }

        return out;
    }


    @Override
    public List<IactnItemResp> getAllScorableIntItemResponses()
    {
        List<IactnItemResp> out = new ArrayList<>();

        for( IactnItemResp iir : iactnItemRespLst )
        {
            // skip intn items at radio button group level
            if( iir.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                continue;

            if( iir.isAutoScorable( ) )
                out.add( iir );
        }

        return out;
    }



    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        boolean isNonComp = intnObj.getNoncompetencyquestiontypeid()>0;

        List<TextAndTitle> out = new ArrayList<>();

        // LogService.logIt( "IactnResp.getTextAndTitleList() starting. " + intnObj.getName() + " getSimletItemType().supportsManualScoringViaReport()=" + getSimletItemType().supportsManualScoringViaReport() + ", isNonComp=" + isNonComp );

        // must either have a non-competency type assigned or be a competency item that has an item type that supports manual scoring.
        if( !isNonComp && ( intnObj.getCompetencyscoreid()<=0 || !getSimletItemType().supportsManualScoringViaReport() ) )
            return out;

        // Store question item here.
        SimJ.Intn.Intnitem q = null;

        // look for an interaction item designated as the question.
        for( SimJ.Intn.Intnitem iitm : intnObj.getIntnitem() )
        {
            if( iitm.getSmltiactnitmtypeid() == SimletSubnodeType.QUESTION.getSimletSubnodeTypeId() )
                q = iitm;
        }

        boolean redFlag = false;

        String question = null;

        String idt = getTextAndTitleIdentifier();

        if( q != null )
        {
            // If has a title, use it.
          if( q.getTitle() != null && !q.getTitle().isEmpty() )
              question = UrlEncodingUtils.decodeKeepPlus( q.getTitle() );  // iitm.title is URL Encoded

          // else, use the question content.
          else if( q.getContent() != null && !q.getContent().isEmpty() )
              question = UrlEncodingUtils.decodeKeepPlus( q.getContent() );// iitm.content is URL Encoded
        }

        List<String> values = new ArrayList<>();

        String v;

        String t;

        long upldFileId;

        // for each interaction item in response
        for( IactnItemResp iir : iactnItemRespLst )
        {
            // skip intn items at radio button group level
            if( iir.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                continue;

            // skip int items scored at interaction item level.
            if( iir.supportsSubnodeLevelSimletAutoScoring() && iir.isAutoScorable() && iir.intnItemObj.getSmltiactnitmtypeid()!=SimletSubnodeType.VALUE_FOR_REPORT.getSimletSubnodeTypeId() )
                continue;

            // If this is a value for the report.
            if( iir.intnItemObj.getSmltiactnitmtypeid()==SimletSubnodeType.VALUE_FOR_REPORT.getSimletSubnodeTypeId() )
            {
                upldFileId = iir.upldUsrFileId;

                if( iir.getIsRedFlag() )
                    redFlag = true;

                // get the value
                v = iir.getValueForReport( question );

                // get the title
                t = iir.getFieldTitleForReport( question );

                // if have any value and a title
                if( v!= null && !v.isEmpty() )
                {
                    // If this interaction item does not have a field-level title, add to list.
                    if( t == null )
                        values.add( v );

                    // if it has a field-level title, we should treat it as a valid pair
                    else
                        out.add(new TextAndTitle( v , t, redFlag, upldFileId, testEvent==null ? this.getCt5ItemId() : testEvent.getNextTextTitleSequenceId(), null, idt ) );
                }
            }
        }

        // if have values
        if( !values.isEmpty() )
        {
            if( question == null )
                question = "";

            StringBuilder sb = new StringBuilder();

            for( String val : values )
            {
                if( sb.length()>0 )
                    sb.append( ", " );

                sb.append( val );
            }

            out.add(new TextAndTitle( sb.toString() , question, redFlag, 0, testEvent==null ? this.getCt5ItemId() : testEvent.getNextTextTitleSequenceId(), null, idt ) );
        }

        return out;
    }



    public boolean isSimletEndpoint()
    {
        if( iactnItemRespLst == null )
            return false;

        // look at the clicked iactnItemResp
        for( IactnItemResp iir : iactnItemRespLst )
        {
            if( iir.intnResultObj.getSnseq() > 0 )
            {
                for( SimJ.Intn.Intnitem ii : this.intnObj.getIntnitem() )
                {
                    // find the right one
                    if( ii.getSeq() != iir.intnResultObj.getSnseq() )
                        continue;

                    if( ii.getEndpoint() == 1 )
                        return true;
                }
            }
        }

        return false;

    }

    @Override
    public long getSimletNodeId()
    {
        if( intnObj != null )
            return intnObj.getSimletnodeid();

        return 0;
    }

    @Override
    public String getSimletNodeUniqueId()
    {
        if( intnObj != null )
            return intnObj.getSimletnodeuniqueid();

        return null;
    }

    @Override
    public int getSimletNodeSeq()
    {
        if( intnObj != null )
            return intnObj.getSimletnodeseq();

        return 0;
    }



    @Override
    public long getSimletActId()
    {
        if( simletScore != null )
            return simletScore.simletObj.getAid();

        if( intnObj != null )
            return intnObj.getSimletaid();

        return 0;
    }


    @Override
    public int simletVersionId()
    {
        if( simletScore != null )
            return simletScore.simletObj.getVersion();

        if( intnObj != null )
            return intnObj.getSimletversionid();

        return 0;
    }


    @Override
    public long simletId()
    {
        if( simletScore != null )
            return simletScore.simletObj.getId();

        if( intnObj != null )
            return intnObj.getSimletid();

        return 0;
    }


    @Override
    public float getResponseTime()
    {
        if( intnResultObj != null )
            return intnResultObj.getCtime();

        return 0;
    }


    @Override
    public SimletItemType getSimletItemType()
    {
        return intnObj == null ? SimletItemType.NA : SimletItemType.getValue( intnObj.getScoretype() );
    }

    @Override
    public boolean measuresSmltCompetency( long simletCompetencyId )
    {
        return simletCompetencyId() == simletCompetencyId;
    }

    @Override
    public long simletCompetencyId()
    {
        //if( overrideSimletCompetencyScore!=null && overrideSimletCompetencyScore.competencyScoreObj!=null )
        //    return overrideSimletCompetencyScore.competencyScoreObj.getId();

        return simletCompetencyScore == null ? 0 : simletCompetencyScore.competencyScoreObj.getId();
    }

    @Override
    public int getCt5SubtopicId()
    {
        return intnObj == null ? 0 : intnObj.getCt5Subtopicid();
    }


    @Override
    public long simCompetencyId()
    {
        return intnObj == null ? 0 : intnObj.getSimcompetencyid();
    }


    @Override
    public int simletItemTypeId()
    {
        return intnObj == null ? SimletItemType.NA.getSimletItemTypeId() : intnObj.getScoretype();
    }

    @Override
    public boolean experimental()
    {
        return intnObj == null ? false : intnObj.getExperimental()==1;
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
        // depends on item type
        return intnResultObj.getCorrect();
    }


    @Override
    public boolean getPartialCreditAssigned()
    {
        if( getSimletItemType().isDichotomous() )
            return false;

        if( getSimletItemType().isPoints() )
            return itemScore()>0 && itemScore()<getMaxPointsArray()[0];

        return false;
    }

    /**
     * remember that intnResultObj.correct=
                 0 means wrong answer
                 1 means correct,
                -1 means not answered,
                -2 means timed out
     * @return
     */
    @Override
    public boolean correct()
    {
        // Designed to handle single radio button selections, multi checkbox selections, single link selections.
        // depends on item type
        if( getSimletItemType().isDichotomous() )
        {
            if( this.hasDichotomousDragTargets() )
                return getDragTargetItemCorrect();

            List<IactnItemResp> sirl = getSelectedIntnItems();

            // First, look for unanswered correct checkboxes.
            for( IactnItemResp irr : this.iactnItemRespLst )
            {
                // if not clickable or was selected, skip for now.
                if( !irr.g2ChoiceFormatType.getIsClickable() || sirl.contains( irr ) )
                    continue;

                // if any unanswerede checks that are correct, it's wrong.
                if( irr.g2ChoiceFormatType.getIsAnyCheckbox() && irr.intnItemObj.getIscorrect() == 1 )
                    return false;
            }

            int c = 0;
            int ic = 0;
            int sbmt = 0;

            for( IactnItemResp irr : sirl )
            {
                // only one selected item, if it's correct ...
                if( sirl.size() == 1 )
                    return irr.intnItemObj.getIscorrect() == 1;

                // count clicked submits
                if( irr.g2ChoiceFormatType.getIsSubmit() )
                     sbmt++;

                if( irr.intnItemObj.getIscorrect() == 1 )
                    c++;

                //
                if( irr.intnItemObj.getIscorrect() != 1 )
                    ic++;
            }

            // nothing correct clicked.
            if( c == 0 )
                return false;

            // more incorrect than submit buttons. (All submit buttons are incorrect)
            if( ic > sbmt )
                return false;

            // if( c>0 && ic == 0 )
            return true;

            // return intnResultObj.getCorrect()==1;
        }

        // To support item analysis, for non-dichotomous items if itemscore=max points we can consider the item correct.
        else if( simletCompetencyScore != null &&
            SimCompetencyClass.getValue( simletCompetencyScore.competencyScoreObj.getClassid() ).getSupportsQuasiDichotomous() )
        {
            if( itemScore()==getMaxPointsArray()[0] && itemScore()>0 )
                return true;
        }

        return false;
    }




    protected boolean getDragTargetItemCorrect()
    {
        if( !hasDichotomousDragTargets() )
            return false;

        if( hasCheckboxDragTargets() )
            return false;

        // List<IactnItemResp> irl = getDragTargetIntItems();

        List<Integer> correctTenantSeqs;

        List<Integer> tenantSeqs;

        boolean tgtIsCorrect;

        for( IactnItemResp iir : getDragTargetIntItems() )
        {
            // ignore any drag target that does not have any correct seq ids.
            if( iir.intnItemObj.getDrgtgtCorrectseqids()==null || iir.intnItemObj.getDrgtgtCorrectseqids().isEmpty() )
                continue;

            correctTenantSeqs = iir.getDragTargetCorrectTenantSeqs();

            tgtIsCorrect = false;

            tenantSeqs = iir.getDragTargetTenantSeqs();

            for( Integer i : tenantSeqs )
            {
                if( correctTenantSeqs.contains(i) )
                {
                    tgtIsCorrect = true;
                    break;
                }
            }

            if( !tgtIsCorrect )
                return false;
        }

        return true;
    }


    protected float getDragTargetItemScore()
    {
        float total = 0;

        // LogService.logIt( "IactnResp.getDragTargetItemScore() AAA hasDichotomousDragTargets()=" + hasDichotomousDragTargets() + ", hasCheckboxDragTargets()=" + hasCheckboxDragTargets() );
        if( hasDichotomousDragTargets() )
        {
            // indicates scoring at subnode level (treats like checkbox true/false.
            if( hasCheckboxDragTargets() )
                return 0;

            // all correctly dragged subnodes.
            List<Integer> cs = getDragTargetCorrectTenantSubnodeSeqs();

            // LogService.logIt( "IactnResp.getDragTargetItemScore() AAA getDragTargetCorrectTenantSubnodeSeqs()=" + cs.size() );

            // add item scores for all correctly dragged subnodes
            for( IactnItemResp iir : this.iactnItemRespLst )
            {
                // this is the draggable intn item.
                if( iir.intnItemObj.getDragable()!=1 )
                    continue;

                // LogService.logIt( "IactnResp.getDragTargetItemScore() Reviewing Draggable intn item  " + iir.intnItemObj.getSeq() + " " + iir.intnItemObj.getContent() + " iir.intnItemObj.getDragable()=" + iir.intnItemObj.getDragable() + " cs.contains(seq)=" + cs.contains( iir.intnItemObj.getSeq()) + ", iir.itemScore()=" + iir.intnItemObj.getItemscore() + ", total=" + total );

                if( !cs.contains( iir.intnItemObj.getSeq() ) )
                    continue;

                // this is the item score of the draggable intn item.
                total += iir.intnItemObj.getItemscore();
            }

            return total;
        }

        // List<IactnItemResp> irl = getDragTargetIntItems();

        // LogService.logIt( "IactnResp.getDragTargetItemScore() 1111 found " + irl.size() + " drag targets." );

        Set<Integer> allTenantSeqs = new HashSet<>();

        for( IactnItemResp iir : getDragTargetIntItems() )
        {
            allTenantSeqs.addAll( iir.getDragTargetTenantSeqs() );
        }

        // LogService.logIt( "IactnResp.getDragTargetItemScore() 222 found " + allTenantSeqs.size() + " drag tenants." );

        for( IactnItemResp iir : this.iactnItemRespLst )
        {
            // LogService.logIt( "IactnResp.getDragTargetItemScore() Reviwing  " + iir.intnItemObj.getSeq() + " " + iir.intnItemObj.getContent() + " iir.intnItemObj.getDragable()=" + iir.intnItemObj.getDragable() + " tenant=" + allTenantSeqs.contains( new Integer( iir.intnItemObj.getSeq() ) ) + ", iir.itemScore()=" + iir.itemScore() );
            if( iir.intnItemObj.getDragable()!=1 || !allTenantSeqs.contains(  iir.intnItemObj.getSeq() ) )
                continue;

            total += iir.itemScore();
        }

        if( total<0 )
            total=0;

        return total;
    }

    // returns a list of the tenant subnode seqs that were placed onto the correct drag target.
    protected List<Integer> getDragTargetCorrectTenantSubnodeSeqs()
    {
        List<Integer> out = new ArrayList<>();

       // if( !hasDichotomousDragTargets() )
       //     return false;

        // List<IactnItemResp> irl = getDragTargetIntItems();

        List<Integer> correctTenantSeqs;

        List<Integer> tenantSeqs;

        int correctSeq;

        for( IactnItemResp iir : getDragTargetIntItems() )
        {
            if( iir.intnItemObj.getDrgtgtCorrectseqids()==null || iir.intnItemObj.getDrgtgtCorrectseqids().isEmpty() )
            {
                // LogService.logIt( "IactnResp.getDragTargetCorrectTenantSubnodeSeqs() Drag Target has no correctseqids. DragTarget=" + iir.intnItemObj.getContent() );
                continue;
            }

            correctSeq = 0;

            correctTenantSeqs = iir.getDragTargetCorrectTenantSeqs();

            tenantSeqs = iir.getDragTargetTenantSeqs();

            // LogService.logIt( "IactnResp.getDragTargetCorrectTenantSubnodeSeqs() correctTenantSeqs=" + correctTenantSeqs.size() + ", tenantSeqs=" + tenantSeqs.size()  + ", correctseqids str=" + iir.intnItemObj.getDrgtgtCorrectseqids());

            for( Integer i : tenantSeqs )
            {
                // LogService.logIt( "IactnResp.getDragTargetCorrectTenantSubnodeSeqs() checking tenant seq=" + i + ", contains=" + correctTenantSeqs.contains(i) + ", correctseqids str=" + iir.intnItemObj.getDrgtgtCorrectseqids());
                if( correctTenantSeqs.contains(i) )
                {
                    correctSeq=i.intValue() ;
                    break;
                }
            }

            if( correctSeq>0 )
                out.add( correctSeq );
        }

        return out;
    }

    protected List<Integer> getDragTargetTenantSubnodeSeqs()
    {
        if( hasDichotomousDragTargets() )
            return this.getDragTargetCorrectTenantSubnodeSeqs();

        List<Integer> out = new ArrayList<>();

        for( IactnItemResp iir : getDragTargetIntItems() )
        {
            out.addAll( iir.getDragTargetTenantSeqs() );
        }

        return out;
    }

    protected List<String> getDragTargetAndTenantSubnodeSeqs()
    {
        List<String> out = new ArrayList<>();

        String t = "";

        for( IactnItemResp iir : getDragTargetIntItems() )
        {
            t = iir.intnItemObj.getSeq() + "";

            for( Integer tenantSeq : iir.getDragTargetTenantSeqs() )
            {
                t += "," + tenantSeq;
            }

            out.add( t );
        }

        return out;
    }



    protected String getDragTargetCorrectTenantSubnodeSeqStr()
    {
        List<Integer> csns = getDragTargetCorrectTenantSubnodeSeqs();

        StringBuilder sb = new StringBuilder();

        for( Integer csn : csns )
        {
            if( sb.length()>0 )
                sb.append( ";" );

            sb.append( csn.toString() );
        }

        return sb.toString();
    }



    @Override
    public float itemScore()
    {
        if( hasDragTargets() )
            return getDragTargetItemScore();

        float f = 0;

        List<IactnItemResp> irl = getSelectedIntnItems();

        // LogService.logIt( "IactnResp.itemScore() itm=" + this.intnObj.getUniqueid() + ", found " +  irl.size() + " selected intn items." );

        for( IactnItemResp iir : irl )
        {
            // LogService.logIt( "IactnResp.itemScore() adding itemscore for " + iir.intnItemObj.getContent() + " (" + iir.intnItemObj.getSeq() + ") itemscore=" + iir.intnItemObj.getItemscore() + ", total=" + f );
            f += iir.intnItemObj.getItemscore();
        }

        if( irl.size() == 1 && intnObj.getTextscoreparam1() != null && intnObj.getTextscoreparam1().toLowerCase().indexOf( "[invert]" ) >= 0 )
            f = invertScore( f );

        if( f<0 )
            f=0;

        return f;
    }

    @Override
    public float getAggregateItemScore( SimCompetencyClass simCompetencyClass )
    {
        float f = 0;

        for( IactnItemResp iir : getSelectedIntnItems() )
        {
            f += simCompetencyClass.getAggregatePoints( iir );
        }

        return f;


        //if( clickedIactnItemResp == null )
        //    return 0;

        //return simCompetencyClass.getAggregatePoints( clickedIactnItemResp );
    }


    public int getNonCompetencyQuestionTypeId()
    {
        if( intnObj == null )
            return 0;

        return intnObj.getNoncompetencyquestiontypeid();
    }

    public void populateItemResponseCore( ItemResponse ir )
    {
         ir.setSimNodeSeq( intnObj.getSeq() );
         ir.setSimletId( this.intnObj.getSimletid() );
         ir.setSimletAid( this.intnObj.getSimletaid() );
         ir.setSimletVersionId( this.intnObj.getSimletversionid() );
         ir.setSimletNodeId( this.intnObj.getSimletnodeid() );
         ir.setSimletNodeSeq( this.intnObj.getSimletnodeseq() );
         // ir.setSimletItemTypeId( this.getSimletItemType().getSimletItemTypeId() );
         ir.setSimletNodeUniqueId( StringUtils.getUrlDecodedValue( this.intnObj.getUniqueid() ) );

         if( intnResultObj != null )
         {
            // 0 = wrong, 1=right, -1=not answered, -2=timeout
            ir.setItemResponseTypeId( correct() ? 1 : intnResultObj.getCorrect() );

            ir.setResponseTime( intnResultObj.getCtime() );

            // Note:  Default value is 1. this value is never 0.
            ir.setClickCount( intnResultObj.getClickCount()>0 ? intnResultObj.getClickCount() : 1 );

            // Note:  Default value is 1. this value is never 0.
            ir.setShowCount( intnResultObj.getShowCount()>0 ? intnResultObj.getShowCount() : 1 );

            ir.setAccessibleForm( intnResultObj.getAccessibleForm() );
         }
    }

    @Override
    public String getExtItemId()
    {
        List<IactnItemResp> irl = getSelectedIntnItems();

        for( IactnItemResp iir : irl )
        {
            if( iir.getIntnItemObj().getExtitemid() != null && !iir.getIntnItemObj().getExtitemid().isEmpty() )
                return iir.getIntnItemObj().getExtitemid();
        }

        if( clickedIactnItemResp != null )
            return clickedIactnItemResp.getExtItemId();

        return null;
    }

    @Override
    public String getSelectedExtPartItemIds()
    {
        List<IactnItemResp> iirl = getSelectedIntnItems();

        // if there is more than one selected intn item, screen out any submit buttons
        // int iirlSize = iirl==null ? 0 : iirl.size();

        if( iirl!=null && !iirl.isEmpty() )
        {
            ListIterator<IactnItemResp> iter = iirl.listIterator();

            IactnItemResp iir;

            while( iter.hasNext() )
            {
                iir = iter.next();

                // LogService.logIt( "IactnResp.getSelectedExtPartIds() unique="  + this.intnObj.getUniqueid() + ", format=" + iir.intnItemObj.getFormat()  );
                if( iir.intnItemObj.getFormat()==G2ChoiceFormatType.SUBMIT.getG2ChoiceFormatTypeId() )
                    iter.remove();
            }

            if( iirl.isEmpty() )
            {
                // LogService.logIt( "IactnResp.getSelectedExtPartIds() No selected intn items found other than submit. Checking IactnItem values. unique="  + this.intnObj.getUniqueid() );
                for( IactnItemResp iir2 : iactnItemRespLst )
                {
                    if( iir2.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                        return null;

                    // skip int items scored at that level.
                    if( iir2.supportsSubnodeLevelSimletAutoScoring() && iir2.isAutoScorable( ) )
                        return null;
                }

                iirl = getSelectedIntnItems();
            }
        }

        if( iirl!=null && !iirl.isEmpty() )
        {
            StringBuilder sb = new StringBuilder();
            for( IactnItemResp iir : iirl )
            {
                if( iir.getIntnItemObj().getExtitempartid()!=null && !iir.getIntnItemObj().getExtitempartid().isEmpty() )
                {
                    if( sb.length()>0 )
                        sb.append(";");
                    sb.append( UrlEncodingUtils.decodeKeepPlus(  iir.getIntnItemObj().getExtitempartid() ) );
                }
            }

            if( sb.length()>0 )
                return sb.toString();
        }

        return null;
    }


    protected String getTextAndTitleIdentifier()
    {
        String idt = UrlEncodingUtils.decodeKeepPlus(getExtItemId());

        if( idt == null )
            idt = UrlEncodingUtils.decodeKeepPlus(intnObj.getUniqueid());

        if( idt==null || idt.isEmpty() )
            idt = UrlEncodingUtils.decodeKeepPlus(intnObj.getId());

        if( idt == null || idt.isEmpty() )
            idt = Integer.toString( intnObj.getSeq() );

        return idt;
    }


    @Override
    public TextAndTitle getItemScoreTextTitle( int includeItemScoreTypeId )
    {
        // LogService.logIt( "IactnResp.getItemScoreTextTitle() START " + toString() );

        IncludeItemScoresType iist = IncludeItemScoresType.getValue(includeItemScoreTypeId);

        if( iist.isNone() )
            return null;

        String itemLevelId = getTextAndTitleIdentifier(); // UrlEncodingUtils.decodeKeepPlus( getExtItemId() );
        String ques = null;
        String title = itemLevelId;

        for( SimJ.Intn.Intnitem iitm : this.intnObj.getIntnitem() )
        {
            if( iitm.getIsquestionstem()==1 )
            {
                ques = StringUtils.getUrlDecodedValue( iitm.getContent() );

                if( ques!=null )
                    ques = StringUtils.truncateStringWithTrailer(ques, 256, true );
            }
        }

        if( ques!=null )
            title += "\n" + ques;


        //if( title == null || title.trim().isEmpty() )
        //    title = UrlEncodingUtils.decodeKeepPlus( intnObj.getUniqueid() );

        //if( title==null || title.isEmpty() )
        //    title = UrlEncodingUtils.decodeKeepPlus( intnObj.getId() );

       // if( title == null || title.isEmpty() )
        //    title = Integer.toString( intnObj.getSeq() );

        String text = null;

        if( iist.isIncludeCorrect() )
        {
            text = correct() ? "Correct" : (getPartialCreditAssigned() ? "Partial" : "Incorrect" );
        }

        else if( iist.isIncludeNumericScore() )
        {
            Locale loc = simJ==null ? null : I18nUtils.getLocaleFromCompositeStr( simJ.getLang() );

            if( loc==null )
                loc = Locale.US;

            text = I18nUtils.getFormattedNumber(loc, itemScore(), 1 ); //  Float.toString( itemScore() );
        }

        else if( iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore( itemScore() );

        else if( iist.isResponseOrResponseCorrect() )
        {

            text = getSelectedExtPartItemIds();

            // LogService.logIt( "IactnResp.getItemScoreTextTitle() BBB.1 Unique=" + intnObj.getUniqueid() + ", text=" + text );

            if( text==null || text.isEmpty() )
            {
                List<IactnItemResp> iirl = getSelectedIntnItems();

                // if there is more than one selected intn item, screen out any submit buttons
                // int iirlSize = iirl==null ? 0 : iirl.size();

                if( iirl!=null && !iirl.isEmpty() )
                {
                    ListIterator<IactnItemResp> iter = iirl.listIterator();

                    IactnItemResp iir;

                    while( iter.hasNext() )
                    {
                        iir = iter.next();

                        // LogService.logIt( "IactnResp.getItemScoreTextTitle() CCC.1 Unique=" + intnObj.getUniqueid() + ", format=" + iir.intnItemObj.getFormat() );
                        if( iir.intnItemObj.getFormat()==G2ChoiceFormatType.SUBMIT.getG2ChoiceFormatTypeId() )
                            iter.remove();

                    }

                    if( iirl.isEmpty() )
                    {
                        // if this IactnResp has active IactnItemResp values and there is only a submit button here.
                        // LogService.logIt( "IactnResp.getItemScoreTextTitle() CCC.2 iirl is empty.  Unique=" + intnObj.getUniqueid()  );

                        for( IactnItemResp iir2 : iactnItemRespLst )
                        {
                            if( iir2.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                                return null;

                            // skip int items scored at that level.
                            if( iir2.supportsSubnodeLevelSimletAutoScoring() && iir2.isAutoScorable( ) )
                                return null;
                        }

                        // include the selected on
                        iirl = getSelectedIntnItems();
                    }
                }

                if( iirl!=null && !iirl.isEmpty() )
                {
                    StringBuilder sb = new StringBuilder();
                    for( IactnItemResp iir : iirl )
                    {
                        if( sb.length()>0 )
                            sb.append("; ");
                        sb.append( StringUtils.truncateString( StringUtils.getUrlDecodedValue( iir.getIntnItemObj().getContent() ), 255) );// + " (" + iir.getIntnItemObj().getSeq() + ")" );
                    }
                    text = sb.toString();
                }
            }

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
         populateItemResponseCore( ir );

        if( intnObj.getIncprevselections()==1 )
            ir.setRepeatItemSimNodeSeq( intnObj.getSeq() );

         if( !hasMultipleIactnLevelScores() )
         {
             // If only one choice was selected but you can still include prev, mark it as a prev.
             if( intnObj.getIncprevselections()==1 )
             {
                ir.setIdentifier( ResponseLevelType.PREV_INTERACTION.computeIdentifier( ir, 1 ) );
                ir.setResponseLevelId( ResponseLevelType.PREV_INTERACTION.getResponseLevelId() );
             }

             else
             {
                ir.setIdentifier( ResponseLevelType.INTERACTION.computeIdentifier( ir, 0 ) );
                ir.setResponseLevelId( ResponseLevelType.INTERACTION.getResponseLevelId() );
             }
         }
         else
         {
             ir.setIdentifier( ResponseLevelType.PREV_INTERACTION.computeIdentifier( ir, prvIactnRespList.size()+1 ) );
             ir.setResponseLevelId( ResponseLevelType.PREV_INTERACTION.getResponseLevelId() );
         }

         if( overrideSimletCompetencyScore!=null && overrideSimletCompetencyScore.competencyScoreObj!=null )
         {
             ir.setSimCompetencyId( overrideSimletCompetencyScore.competencyScoreObj.getSimcompetencyid() );
             ir.setCompetencyScoreId( overrideSimletCompetencyScore.competencyScoreObj.getId() );
         }

         else if( simletCompetencyScore!=null )
         {
             ir.setSimCompetencyId( simletCompetencyScore.competencyScoreObj.getSimcompetencyid() );
             ir.setCompetencyScoreId( simletCompetencyId() );
         }

         ScoredItemParadigmType sipt = ScoredItemParadigmType.getValue(this);

         ir.setItemParadigmTypeId( sipt.getScoredItemParadigmTypeId() );

         ir.setItemScore( itemScore() );

         ir.setMetascore1( getMetaScore(1) );
         ir.setMetascore2( getMetaScore(2) );
         ir.setMetascore3( getMetaScore(3) );
         ir.setMetascore4( getMetaScore(4) );
         ir.setMetascore5( getMetaScore(5) );
         ir.setMetascore6( getMetaScore(6) );
         ir.setMetascore7( getMetaScore(7) );
         ir.setMetascore8( getMetaScore(8) );
         ir.setMetascore9( getMetaScore(9) );

         ir.setSimletItemTypeId( simletItemTypeId() );

         List<Integer> ssnsqs = getSelectedSnSeqs();

         // If what's clicked is in the list of selected
         if( clickedIactnItemResp != null && ssnsqs.contains( clickedIactnItemResp.intnItemObj.getSeq() ) )
         {
             ir.setSelectedValue(StringUtils.truncateString(UrlEncodingUtils.decodeKeepPlus( clickedIactnItemResp.intnItemObj.getContent() ), 1900 )   );
             ir.setSubnodeSeq( intnResultObj.getSnseq() );
             ir.setScoreParam1( clickedIactnItemResp.intnItemObj.getScoreparam1() );
             ir.setScoreParam2( clickedIactnItemResp.intnItemObj.getScoreparam2() );
             ir.setScoreParam3( clickedIactnItemResp.intnItemObj.getScoreparam3() );
             ir.setTrueScore( clickedIactnItemResp.intnItemObj.getTruescore() );
         }

        if( getSimletItemType().isDichotomous() )
        {
            ir.setCorrect( correct() ? 1 : 0 );

            ir.setCorrectSubnodeSeqIds( getCorrectSubnodeSeqStr() );
        }

        String slctdSubSeqs = "";
        String slctdSubFormatTypeIds = "";

        for( IactnItemResp iir : iactnItemRespLst )
        {
            //if selected
            if( ssnsqs.contains( (int)( iir.intnItemObj.getSeq() )  ) )
            {
                if( slctdSubFormatTypeIds.length() > 0 )
                    slctdSubFormatTypeIds += ";";

                if( !slctdSubSeqs.isEmpty() )
                    slctdSubSeqs += ";";

                slctdSubSeqs += Integer.toString( iir.intnItemObj.getSeq() );
                slctdSubFormatTypeIds += iir.intnItemObj.getFormat();
            }
        }

        ir.setSelectedSubnodeSeqIds( slctdSubSeqs );
        ir.setSelectedSubFormatTypeIds(slctdSubFormatTypeIds);

        // To support item analysis, for non-dichotomous items if itemscore=max points we can consider the item correct.
        if( !getSimletItemType().isDichotomous() &&
            simletCompetencyScore != null &&
            SimCompetencyClass.getValue( simletCompetencyScore.competencyScoreObj.getClassid() ).getSupportsQuasiDichotomous() )
        {
            if( itemScore()==getMaxPointsArray()[0] && itemScore()>0 )
            {
                ir.setCorrect(1);
                ir.setCorrectSubnodeSeqIds( ir.getSelectedSubnodeSeqIds() );
            }
        }


        String selValue = "";

        // For drag targets, put target,tenant,tenant;target,tenant,target,target  - to capture which target on which tenant.
        if( hasDragTargets() )
        {
            List<String> dragTgtsAndTenants = this.getDragTargetAndTenantSubnodeSeqs();

            for( String s : dragTgtsAndTenants )
            {
                if( !selValue.isEmpty() )
                    selValue += ";";

                selValue += s;
            }

            ir.setSelectedValue(selValue );
        }


        else if( 1==1 )
        {
            for( Integer ssnq : this.getSelectedSnSeqs() )
            {
                for( SimJ.Intn.Intnitem iitm : intnObj.getIntnitem() )
                {
                    if( iitm.getSeq()!=ssnq )
                        continue;

                    if( (!getHasMultipleRadioButtonGroups() && G2ChoiceFormatType.getValue(iitm.getFormat()).getIsAnyRadio()) ||  // single radio
                          iitm.getFormat()==G2ChoiceFormatType.BUTTON.getG2ChoiceFormatTypeId() || // single button
                          (G2ChoiceFormatType.getValue(iitm.getFormat()).getIsAnyCheckbox() && iitm.getCompetencyscoreid()<=0) ) // checkbox at intn level.
                    {
                        if( !selValue.isBlank() )
                            selValue +=", ";
                        selValue += StringUtils.truncateString( UrlEncodingUtils.decodeKeepPlus(iitm.getContent()), 40);
                    }
                }
            }
            if( !selValue.isBlank() )
                ir.setSelectedValue( StringUtils.truncateString(selValue, 1900));
        }

    }

    public String getCorrectSubnodeSeqStr()
    {
        if( this.hasDichotomousDragTargets() )
            return getDragTargetCorrectTenantSubnodeSeqStr();

        String crctSubSeqs = "";

        for( IactnItemResp iir : iactnItemRespLst )
        {
            // skip intn items at radio button group level
            if( iir.g2ChoiceFormatType.getIsAnyRadio() && getHasMultipleRadioButtonGroups() )
                continue;

            // skip int items scored at that level.
            if( iir.supportsSubnodeLevelSimletAutoScoring() && iir.isAutoScorable( ) )
                continue;

            if( !iir.g2ChoiceFormatType.getIsClickable() || iir.g2ChoiceFormatType.getIsSubmit() )
                continue;

            if( iir.intnItemObj.getIscorrect()== 1 )
            {
                if( crctSubSeqs.length() > 0 )
                    crctSubSeqs += ";";

                crctSubSeqs += iir.intnItemObj.getSeq();
            }
        }

        return crctSubSeqs;

    }


   /**
     * Returns - unless the scorableresponse has a textScoreParam1 of [SCALEDSCOREFLOOR]value  where value is the min value for the scaled competency score that is to be
     * enforced based on the fact that this person made this selection.
     * @return
     */
    @Override
    public float getFloor()
    {
        if( clickedIactnItemResp != null )
            return clickedIactnItemResp.getFloor();

        return 0;
        //String s = StringUtils.getBracketedArtifactFromString( intnObj.getTextscoreparam1() , Constants.SCALEDSCOREFLOOR );

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
        if( clickedIactnItemResp != null )
            return clickedIactnItemResp.getCeiling();

        return 0;

        //String s = StringUtils.getBracketedArtifactFromString( intnObj.getTextscoreparam1() , Constants.SCALEDSCORECEILING );

        //return s == null || s.length()==0 ? 0 : Float.parseFloat(s);
    }


    /**
     * Returns null - unless the scorableresponse has a textScoreParam1 of [SCORETEXTCAVEAT]value sentence  -  where value sentence is a sentence that should be appended to
     * the scoretext for this competency in any report.
     * @return
     */
    @Override
    public String getCaveatText()
    {
        if( clickedIactnItemResp != null )
            return clickedIactnItemResp.getCaveatText();

        return null;


        // return StringUtils.getBracketedArtifactFromString( intnObj.getTextscoreparam1() , Constants.SCORETEXTCAVEAT );
    }


    @Override
    public InterviewQuestion getScoreTextInterviewQuestion()
    {
        if( clickedIactnItemResp != null )
            return clickedIactnItemResp.getScoreTextInterviewQuestion();

        return null;
        // return InterviewQuestion.getFromScoreText( intnObj.getTextscoreparam1() );
    }


    @Override
    public boolean hasValidScore()
    {
        return true;
    }



    @Override
    public boolean hasMetaScore( int i )
    {
        if( i==2 )
            return getSimletItemType().hasScore2();
        if( i==3 )
            return getSimletItemType().hasScore3();
        if( i==4 )
            return getSimletItemType().hasScore4();
        if( i==5 )
            return getSimletItemType().hasScore5();
        if( i==6 )
            return getSimletItemType().hasScore6();
        if( i==7 )
            return getSimletItemType().hasScore7();
        if( i==8 )
            return getSimletItemType().hasScore8();
        if( i==9 )
            return getSimletItemType().hasScore9();
        if( i==10 )
            return getSimletItemType().hasScore10();
        if( i==11 )
            return getSimletItemType().hasScore11();
        if( i==12 )
            return getSimletItemType().hasScore12();

        return false;
    }


    @Override
    public float getMetaScore( int i )
    {
        return metaScores == null || metaScores.length<i+1 ? 0 : metaScores[i];
    }

    @Override
    public List<String> getForcedRiskFactorsList() {
        return forcedRiskFactorsList;
    }

    public boolean getValidItemsCanHaveZeroMaxPoints() {
        return validItemsCanHaveZeroMaxPoints;
    }

    @Override
    public float getTotalItemCountIncrementValue()
    {
        return 1;
    }



}
