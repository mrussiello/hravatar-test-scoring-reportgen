/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.iactnresp;

import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.event.ResponseLevelType;
import com.tm2score.global.I18nUtils;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.item.TypingItem;
import com.tm2score.sim.IncludeItemScoresType;
import com.tm2score.util.StringUtils;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class TypingIactnResp extends IactnResp implements ScorableResponse
{
    /**
     * ScoreParam1 is the maximum number of points assigned by this item. If set to zero, this value is 100.
     * ScoreParam2 is the equivalent Accuracy-Adjusted WPM that gives the max points.
     * ScoreParam3 is the Accuracy-Adjusted WPM that gives 0 points.
     *
     * Intn.TextScoreParam1 = should have [DATAENTRY] or [TENKEY] if it's really a data entry item. 
     * Intn.TextScoreParam1 = should have None or [TYPING] it's a typing item. 
     * 
     */


    boolean hasAValidScore = false;

    // raw words per minute
    float wpm=0;
    float accuracyAdjustedWpm=0;
    float accuracy=0;

    float maxPoints = 100;
    float maxPointsAdjWpm = 100;
    float zeroPointsAdjWpm = 0;

    float points = 0;
        
    StringBuilder allTypedText = null;



    public TypingIactnResp( Clicflic.History.Intn iob )
    {
        super(iob, null );
    }


    @Override
    public String toString()
    {
        return "TypingIactnResp{ " + ( intnObj == null ? " intn is null" :  intnObj.getName() + ", id" + intnObj.getId() + ", nodeSeq=" + intnObj.getSeq() ) + ( intnResultObj==null ? " intnResultObj is null" : ", sel SubSeq=" + intnResultObj.getSnseq() ) + ", ct5ItemId=" + this.getCt5ItemId() + ", ct5ItemPartId=" + this.getCt5ItemPartId() + "}";
    }




    @Override
    public void calculateScore() throws Exception
    {
        if( hasAValidScore )
            return;

        if( intnObj.getScoreparam1() > 0 )
            maxPoints = intnObj.getScoreparam1();

        if( intnObj.getScoreparam2() > 0 )
            maxPointsAdjWpm = intnObj.getScoreparam2();

        if( intnObj.getScoreparam3() > 0 )
            zeroPointsAdjWpm = intnObj.getScoreparam3();

        if( maxPointsAdjWpm<=zeroPointsAdjWpm)
        {
            maxPointsAdjWpm = 100;
            zeroPointsAdjWpm = 0;
        }

        // List<TypingItem> items = new ArrayList<>();
        // first, get the scorable strings and their templates

        TypingItem dei;

        IactnItemResp iir;

        int scrCt=0;
        float tWpm=0;
        float tAdjWpm=0;
        float tAcc=0;
        
        allTypedText = new StringBuilder();

        for( SimJ.Intn.Intnitem intItemObj : intnObj.getIntnitem() )
        {
            //if( intItemObj.getFormat() == G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() )
            //    LogService.logIt( "DataEntryIactnResp.calculateScore() HAVE INT ITEM Content=" + intItemObj.getContent() +", TextScoreParam1=" + intItemObj.getTextscoreparam1() );

            if( intItemObj.getFormat() != G2ChoiceFormatType.TEXT_BOX.getG2ChoiceFormatTypeId() || intItemObj.getTextscoreparam1()==null || intItemObj.getTextscoreparam1().isEmpty() )
                continue;

            iir =  IactnRespFactory.getIactnItemResp(this, intItemObj, intnResultObjO, testEvent );  // new IactnItemResp( this, intItemObj, intnResultObj );

            boolean wordsTypedLimitsKeyWords = intnObj.getCt5Int2()==1;
            dei = new TypingItem( intItemObj.getTextscoreparam1() , iir.getRespValue(), intnResultObj.getCtime(), wordsTypedLimitsKeyWords );

            if( iir.getRespValue()!=null && !iir.getRespValue().isBlank() )
            {
                // items.add( dei );
                if( allTypedText.length()>0 )
                    allTypedText.append( "\n\n" );
                allTypedText.append( iir.getRespValue() + " (" +  intnResultObj.getCtime() + " Sec)" );
            }
            
            dei.calculate();

            if( !dei.getHasValidScore() )
                continue;

            scrCt++;
            tWpm += dei.getWpm();
            tAdjWpm += dei.getAccuracyAdjustedWpm();
            tAcc += dei.getAccuracy();
        }

        // now, average everything
        if( scrCt>0 )
        {
            wpm = tWpm/((float) scrCt);
            accuracyAdjustedWpm = tAdjWpm/((float) scrCt);
            accuracy = tAcc/((float) scrCt);
            hasAValidScore = true;

            if( accuracyAdjustedWpm >= maxPointsAdjWpm )
                points = maxPoints;

            else if( accuracyAdjustedWpm <= zeroPointsAdjWpm )
                points = 0;

            else
            {
                points = maxPoints*(accuracyAdjustedWpm - zeroPointsAdjWpm)/(maxPointsAdjWpm - zeroPointsAdjWpm );
            }

            metaScores = new float[5];

            metaScores[2] = wpm;                       // averaged in competency score
            metaScores[3] = accuracyAdjustedWpm;       // averaged in competency score
            metaScores[4] = accuracy;                  // averaged in competency score
        }

        // LogService.logIt( "TypingIactnResp.calculateScore() For ITEM: maxPoints=" + maxPoints + ", points=itemScore=" + points + ", wpm=" + wpm + ", accuracyAdjWpm=" + accuracyAdjustedWpm + ", accuracy=" + accuracy );
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
                    ques = StringUtils.truncateStringWithTrailer(ques, 256, true );
            }
        }
        if( ques!=null )
            title += "\n" + ques;            
        
        String text = null;
        
        if( iist.isIncludeNumericScore() || iist.isIncludeCorrect() )
        {
            text = I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1 ); //  Float.toString( itemScore() );
            // text = Float.toString( itemScore() );
        }

        else if( iist.isIncludeAlphaScore())
            text = IncludeItemScoresType.convertNumericToAlphaScore( itemScore() );

        else if( iist.isResponseOrResponseCorrect() )
        {
            if( allTypedText!=null && allTypedText.length()>0 )
            {
                text = allTypedText.toString();
                if( iist.isResponseCorrect() )
                    text += " (" + I18nUtils.getFormattedNumber(Locale.US, itemScore(), 1 ) + ")";            
            }
            else
                text = Float.toString( itemScore() );
            
        }
        
        if( text == null || text.isBlank())      
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
         
         // LogService.logIt( "TypingIactnResp.populateItemResponse() allTypedText=" + (allTypedText==null ? "null" : allTypedText.toString() ) );
         
        if( allTypedText!=null && allTypedText.length()>0 )
            ir.setSelectedValue(allTypedText.toString() );
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

