/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.iactnresp;

import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.event.ResponseLevelType;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.item.DataEntryItem;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class DataEntryIactnResp extends IactnResp implements ScorableResponse
{
    /**
     * ScoreParam1 is the maximum number of points assigned by this item. If set to zero, this value is 100.
     * ScoreParam2 is the equivalent Accuracy-Adjusted KSH that gives the max points.
     * ScoreParam3 is the Accuracy-Adjusted KSH that gives 0 points.
     *
     *
     */


    boolean hasAValidScore = false;



    // keystrokes per hour
    float ksh=0;
    float accuracyAdjustedKsh=0;
    float accuracy=0;

    float maxPoints = 100;
    float maxPointsAdjKsh = 10000;
    float zeroPointsAdjKsh = 0; // 4000;

    float points = 0;


    // For Data Entry Items
    float totalErrors=0;
    float totalCorrect=0;
    float totalKeystrokes=0;
    float accurateKeystrokes=0;

    List<DataEntryItem> dataEntryItemList;




    public DataEntryIactnResp( Clicflic.History.Intn iob )
    {
        super(iob, null );

        // LogService.logIt( "DataEntryIactnResp() " + toString() );
    }


    @Override
    public String toString()
    {
        return "DataEntryIactnResp{ " + ( intnObj == null ? " intn is null" :  intnObj.getName() + ", id=" + intnObj.getId() + ", nodeSeq=" + intnObj.getSeq() ) + ( intnResultObj==null ? " intnResultObj is null" : ", sel SubSeq=" + intnResultObj.getSnseq() ) + "}";
    }




    @Override
    public void calculateScore() throws Exception
    {
        // LogService.logIt( "DataEntryIactnResp.calculateScore() Start " + toString() );
        if( hasAValidScore )
            return;

        float seconds = intnResultObj.getCtime();

        if( seconds <=0 )
        {
            LogService.logIt( "DataEntryIactnResp.calculateScore() no time found - cannot score. " + toString() );
            return;
        }

        if( intnObj.getScoreparam1() > 0 )
            maxPoints = intnObj.getScoreparam1();

        if( intnObj.getScoreparam2() > 0 )
            maxPointsAdjKsh = intnObj.getScoreparam2();

        if( intnObj.getScoreparam3() > 0 )
            zeroPointsAdjKsh = intnObj.getScoreparam3();

        if( maxPointsAdjKsh<=zeroPointsAdjKsh)
        {
            maxPointsAdjKsh = 10000;
            zeroPointsAdjKsh = 0; // 4000;
        }

        // List<DataEntryItem> items = new ArrayList<>();
        // first, get the scorable strings and their templates

        DataEntryItem dei;

        IactnItemResp iir;

        dataEntryItemList = new ArrayList<>();

        //int scrCt=0;
        float tKs=0;
        float tAdjKs=0;
        //float tAcc=0;

        int orderIndex=1;
        for( SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem() )
        {
            //if( intItemObj.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            //    LogService.logIt( "DataEntryIactnResp.calculateScore() HAVE INT ITEM Content=" + intItemObj.getContent() +", TextScoreParam1=" + intItemObj.getTextscoreparam1() );

            if( intItemObj.getFormat() != G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() || intItemObj.getTextscoreparam1()==null || intItemObj.getTextscoreparam1().isEmpty() )
                continue;

            iir =  IactnRespFactory.getIactnItemResp(this, intItemObj, intnResultObjO, testEvent, orderIndex );  // new IactnItemResp( this, intItemObj, intnResultObj );

            orderIndex++;
            
            String keyStr = intItemObj.getTextscoreparam1();

            keyStr = conditionKeyStr( keyStr );

            //LogService.logIt( "DataEntryIactnResp.calculateScore() keyStr=" + keyStr + ", respVal=" + iir.getRespValue() );

            dei = new DataEntryItem( intItemObj.getSeq(), keyStr , iir.getRespValue() );

            dei.calculate();

            if( !dei.getHasValidScore() )
                continue;

            dataEntryItemList.add( dei );

            if( dei.correct() )
                totalCorrect++;
            else
            {
                // LogService.logIt( "DataEntryIactnResp.calculateScore() found typing errors in item " + this.intnResultObj.getUnqid() + " seq=" + this.intnResultObj.getNdseq() + "-" + intItemObj.getSeq() +  ", keyStr=" + intItemObj.getTextscoreparam1() + ", typedStr=" + iir.getRespValue() );

                totalErrors++;
            }

            // scrCt++;
            tKs += dei.getTypedKeystrokes();
            tAdjKs +=  dei.correct() ? dei.getTypedKeystrokes() : 0;
        }


        // now, average everything
        if( (totalCorrect + totalErrors)>0 )
        {
            ksh = (tKs/seconds)*60*60;
            accuracyAdjustedKsh = (tAdjKs/seconds)*60*60;

            accuracy = 100f*totalCorrect/(totalCorrect+totalErrors); //  +  tAcc/((float) scrCt);
            hasAValidScore = true;

            //if( accuracyAdjustedKsh >= maxPointsAdjKsh )
            //    points = maxPoints;

            // else if( accuracyAdjustedKsh <= zeroPointsAdjKsh )
            if( accuracyAdjustedKsh <= zeroPointsAdjKsh )
                points = 0;

            else
            {
                points = maxPoints*(accuracyAdjustedKsh - zeroPointsAdjKsh)/(maxPointsAdjKsh - zeroPointsAdjKsh );

                if( points>maxPoints )
                    points = maxPoints;
            }

            metaScores = new float[16];
            metaScores[2] = ksh;                                   // averaged in competency score
            metaScores[3] = accuracyAdjustedKsh;                   // averaged in competency score
            metaScores[4] = accuracy;                              // averaged in competency scores
            metaScores[5] = totalErrors;                           // summed in competency scores
            metaScores[6] = totalCorrect + totalErrors;            // summed in competency scores
            metaScores[7] = intnResultObj.getCtime();              // Averaged in competency scores
        }

        // LogService.logIt( "DataEntryIactnResp.calculateScore() For ITEM: maxPoints=" + maxPoints + ", points=itemScore=" + points + ", ksh=" + ksh + ", accuracyAdjKsh=" + accuracyAdjustedKsh + ", accuracy=" + accuracy + " totalCorrect=" + totalCorrect + ", totalErrors=" + totalErrors + ", total items=" + (totalCorrect + totalErrors) );
    }


    private String conditionKeyStr( String keyStr )
    {
        if( keyStr==null || keyStr.isBlank() )
            return keyStr;

        String pts = IvrStringUtils.getTagValue(keyStr, Constants.POINTS_KEY );
        if( pts==null || pts.isBlank() )
            return keyStr;

        if( pts.indexOf("|")<0 )
            return pts;

        StringBuilder sb = new StringBuilder();
        String[] vals = pts.split("\\|" );
        String v;
        for( int i=0; i<vals.length-1; i+=2 )
        {
            v=vals[i].trim();
            if( v.isBlank() )
                continue;
            if( sb.length()>0 )
                sb.append( "|" );
            sb.append(v);
        }
        return sb.toString();
    }


    @Override
    public String getExtItemId()
    {
        for( SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem() )
        {
            //if( intItemObj.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            //    LogService.logIt( "DataEntryIactnResp.calculateScore() HAVE INT ITEM Content=" + intItemObj.getContent() +", TextScoreParam1=" + intItemObj.getTextscoreparam1() );

            if( intItemObj.getFormat() != G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() || intItemObj.getTextscoreparam1()==null || intItemObj.getTextscoreparam1().isEmpty() )
                continue;

            if( intItemObj.getExtitemid()!=null && !intItemObj.getExtitemid().isEmpty() )
                return intItemObj.getExtitemid();
        }

        return null;
    }

    @Override
    public String getSelectedExtPartItemIds()
    {
        return null;
    }

    @Override
    public TextAndTitle getItemScoreTextTitle( int includeItemScoreTypeId )
    {
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
                    ques = StringUtils.truncateStringWithTrailer(ques, 512, true );
            }
        }

        if( ques!=null )
            title += "\n" + ques;

        String text = null;

        if( iist.isIncludeCorrect() )
        {
            text = getFullKeyResponseStr(); //  Float.toString( itemScore() );
        }

        else if( iist.isIncludeNumericScore() )
        {
            text = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1 ); //  Float.toString( itemScore() );
        }

        else if( iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore( itemScore() );

        else if( iist.isResponseOrResponseCorrect() )
        {
            text = getFullKeyResponseStr(); // Float.toString( itemScore() );
        }

        if( text == null || text.isEmpty() )
            return null;

        text = StringUtils.replaceStr( text, "[", "{" );
        title = StringUtils.replaceStr(title, "[", "{" );
        itemLevelId = StringUtils.replaceStr(itemLevelId, "[", "{" );
        ques = StringUtils.replaceStr(ques, "[", "{" );
        TextAndTitle tt = new TextAndTitle( text, title, intnResultObjO.getSq()*100, itemLevelId, ques );
        tt.setOrder(intnResultObjO.getSq()*100);
        return tt;
    }


    private String getFullKeyResponseStr()
    {
        StringBuilder sb = new StringBuilder();

        if( dataEntryItemList==null || dataEntryItemList.isEmpty() )
            return sb.toString();

        // sb.append( Constants.DATA_ENTRY_RESP_VALS_KEY );

        for( DataEntryItem dei : this.dataEntryItemList )
        {
            sb.append( dei.getKeyValue() + " | " + dei.getTypedStr() + " | " + (dei.correct() ? "Correct" : "Incorrect" ) + "\n" );
        }

        return sb.toString();
    }


    @Override
    public void populateItemResponse( ItemResponse ir )
    {
         populateItemResponseCore( ir );

         ir.setResponseLevelId( ResponseLevelType.INTERACTION.getResponseLevelId() );

         ir.setIdentifier( ResponseLevelType.INTERACTION.computeIdentifier( ir, 0 ) );

         if( simletCompetencyScore!=null )
             ir.setSimCompetencyId( simletCompetencyScore.competencyScoreObj.getSimcompetencyid() );

         ir.setCompetencyScoreId( simletCompetencyId() );

         ScoredItemParadigmType sipt = ScoredItemParadigmType.getValue(this);

         ir.setItemParadigmTypeId( sipt.getScoredItemParadigmTypeId() );

         //ir.setItemResponseTypeId( getItemResponseTypeId() );

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


         if( dataEntryItemList!=null )
         {
             StringBuilder sb = new StringBuilder();
             String svs;
             for( DataEntryItem dei : this.dataEntryItemList )
             {
                 svs = dei.getSelectedValueString();

                 if( svs.isBlank() )
                     continue;

                 if( sb.length()>0  )
                     sb.append( "~" );

                 sb.append( svs );
             }

             svs = sb.toString();

             if( svs.length()>1999 )
                 svs = svs.substring(0, 1999);

             ir.setSelectedValue( svs );
         }
    }



    @Override
    public boolean hasMultipleIactnLevelScores()
    {
       return false;
    }



    @Override
    public boolean hasCorrectInteractionItems()
    {
        return false;
    }


    @Override
    public boolean hasInteractionItemScores()
    {
        return false;
    }

    @Override
    public float itemScore()
    {
        return points;
    }

    @Override
    public float getFloor()
    {
        return 0;
    }


    @Override
    public float getCeiling()
    {
        return 0;
    }


    /*
    @Override
    public String getCaveatText()
    {
        return null;
    }
    */

    @Override
    public List<CaveatScore> getCaveatScoreList()
    {
        return new ArrayList<>();
    }


    @Override
    public InterviewQuestion getScoreTextInterviewQuestion()
    {
        return null;
    }


    @Override
    public boolean isScoredDichotomouslyForTaskCalcs()
    {
        return false;
    }

    @Override
    public boolean saveAsItemResponse()
    {
        return hasAValidScore;
    }

    @Override
    public float getAggregateItemScore( SimCompetencyClass simCompetencyClass )
    {
        return 0;
    }

    @Override
    public synchronized float[] getMaxPointsArray()
    {
        maxPointsArray = new float[]{maxPoints,0,0,0};

        return maxPointsArray;
    }


    @Override
    public boolean hasValidScore()
    {
        return hasAValidScore;
    }



}

