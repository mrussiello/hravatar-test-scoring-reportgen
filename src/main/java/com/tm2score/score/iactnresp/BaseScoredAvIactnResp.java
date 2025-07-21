/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score.iactnresp;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.av.AvItemEssayStatusType;
import com.tm2score.av.AvItemScorer;
import com.tm2score.av.AvItemScorerFactory;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.ItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ResponseLevelType;
import com.tm2score.global.I18nUtils;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.imo.xml.Clicflic;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.MergableScoreObject;
import com.tm2score.score.ScoredItemParadigmType;
import com.tm2score.score.SimletScore;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.InteractionScoreUtils;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import com.tm2score.voicevibes.VoiceVibesResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class BaseScoredAvIactnResp  extends IactnResp implements ScorableResponse {
    
    public AvItemResponse avItemResponse;
    public AvItemScorer avItemScorer;
    public VoiceVibesResult voiceVibesResult;
            
    public BaseScoredAvIactnResp( Clicflic.History.Intn intRespObj, SimJ.Intn intn, TestEvent testEvent) throws Exception
    {
        super(intRespObj, testEvent );
        
        try
        {
            intnObj = intn;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseScoredAvIactnResp() ");
            
            throw e;
        }
    }

    public BaseScoredAvIactnResp( SimJ.Intn intn, AvItemResponse iir, Locale locale, TestEvent testEvent) throws Exception
    {
        super(null, testEvent);
        
        try
        {
            intnObj = intn;
            avItemResponse = iir;
            avItemScorer = AvItemScorerFactory.getAvItemScorer(iir.getAvItemTypeId(), locale, testEvent.getIpCountry(), testEvent.getUser(), testEvent  );
            intnResultObj=null;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseScoredAvIactnResp() " + (iir==null ? "AvItemResponse is null" : iir.toString()) );
            
            throw e;
        }
    }


    public void initAv( AvItemResponse iir, Locale simLocale, TestEvent testEvent) throws Exception
    {
        try
        {
            Locale localeToUse = simLocale;
            
            if( this.intnObj!=null && intnObj.getIntnitem()!=null )
            {
                G2ChoiceFormatType gft;
                
                for( SimJ.Intn.Intnitem ii : intnObj.getIntnitem() )
                {
                    gft = G2ChoiceFormatType.getValue( ii.getFormat() );
                    
                    if( gft!=null && ( gft.equals(G2ChoiceFormatType.MEDIA_CAPTURE) || gft.equals(G2ChoiceFormatType.FILEUPLOADBTN) )  && ii.getLangcode()!=null && !ii.getLangcode().isBlank() )
                        localeToUse = I18nUtils.getLocaleFromCompositeStr( ii.getLangcode() );
                }
            }
            
            avItemResponse = iir;
            avItemScorer = iir!=null ? AvItemScorerFactory.getAvItemScorer(iir.getAvItemTypeId(), localeToUse,  testEvent.getIpCountry(), testEvent.getUser(), testEvent ) : null;
            intnResultObj=null;
                    
            // EssayStatus type should be set by now so if not set, then it's a problem. 
            // this can only happen for old code
            if( iir!=null && iir.getAvItemEssayStatusType().isUnset() && !iir.getAvItemType().supportsEssayScoring() )
                iir.setAvItemEssayStatusTypeId( AvItemEssayStatusType.NOT_REQUIRED.getEssayStatusTypeId() );
            // else if( iir.getEssayStatusType().isUnset() )
            //     iir.setEssayStatusTypeId( AvItemEssayStatusType.NOT_REQUIRED.getEssayStatusTypeId() );
            
            
            if( avItemScorer!=null && avItemScorer.getSelectedIntnItem( intnObj, avItemResponse) != null )
                clickedIactnItemResp = IactnRespFactory.getIactnItemResp(this, this.avItemScorer.getSelectedIntnItem(intnObj, avItemResponse), null, testEvent, 1);                        
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseScoredAvIactnResp.initAv() " + (iir==null ? "AvItemResponse is null" : iir.toString()) );
            
            throw e;
        }
    }

    public AvItemScorer getAvItemScorer() {
        return avItemScorer;
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
        
        if( avItemScorer!= null )
            return avItemScorer.getMaxPointsArray();

        if( intnObj==null || intnObj.getMaxpoints() == null || intnObj.getMaxpoints().isEmpty() )
            return new float[4];

        maxPointsArray = InteractionScoreUtils.getPointsArray( intnObj.getMaxpoints() );

        return maxPointsArray;
    }
    
    
    
    
    @Override
    public boolean allowsSupplementaryCompetencyLevelTextAndTitle() 
    {
        if( avItemScorer != null )
            return avItemScorer.allowsSupplementaryCompetencyLevelTextAndTitle();
        
        return false;
    }
    
    
    @Override
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId)
    {
        if( avItemScorer!=null )        
        {
            TextAndTitle tt = this.avItemScorer.getItemScoreTextTitle(includeItemScoreTypeId, this);
            if( tt!=null )
            {
                tt.setOrder( this.intnResultObjO.getSq()*100 );
                tt.setSequenceId(intnResultObjO.getSq()*100);
            }
            return tt;
        }
        
        return null;
    }

    

    
    
    
    
    @Override
    public String toString()
    {
        return "BaseScoredAvIactnResp{ " + ( intnObj == null ? " intn is null" :  intnObj.getName() + ", id=" + intnObj.getId() + ", nodeSeq=" + intnObj.getSeq() ) + ( avItemResponse==null ? " avItemResponse is null" : avItemResponse.toString()) + ", ct5ItemId=" + this.getCt5ItemId() + ", ct5ItemPartId=" + this.getCt5ItemPartId() + "}";
    }
    
    
    @Override
    public boolean isAutoScorable()
    {
        return true;
    }    
    
    @Override
    public boolean isPendingExternalScore()
    {
        if( avItemScorer!=null && avItemScorer.isPendingScoring() )
            return true;
        
        if( avItemResponse == null )
            return false;
        
        if( !avItemResponse.isScoreCompleteOrError() )
            return true;
            
        return !avItemResponse.getAvItemEssayStatusType().isNotRequired() && !avItemResponse.getAvItemEssayStatusType().isCompleteOrPermanentError();
    }
    
    @Override
    public void calculateScore() throws Exception
    {
        if( avItemResponse!=null )
        {
            if( (avItemResponse.getItemUniqueId()==null || avItemResponse.getItemUniqueId().isEmpty()) && intnObj!=null && !intnObj.getUniqueid().isEmpty() )
                avItemResponse.setItemUniqueId( this.intnObj.getUniqueid() );            
        }
        
        if( avItemScorer!=null )
            avItemScorer.scoreAvItem(intnObj, avItemResponse);
        
        metaScores = avItemScorer!=null ? avItemScorer.getMetaScores( avItemResponse ) : null; 
    }
    
    
    @Override
    public List<MergableScoreObject> getMergableScoreObjects() 
    {
        List<MergableScoreObject> out = new ArrayList<>();
        
        if( hasVoiceVibesReport() )
        {
            VoiceVibesResult vvr = getVoiceVibesResult();
            
            if( vvr != null )
                out.add( vvr );
        }
        
        return out;
    }
    
    
    
    public Map<Integer,String> getTextInputTypeValues()
    {
        if( avItemResponse==null || avItemResponse.getAvItemScoringStatusType().isAnyInvalid() || this.avItemScorer==null )
            return null;
        
        return avItemScorer.getTextInputTypeMap( this.getIntnObj(), this.getAvItemResponse() );
    }
    
    
    
    @Override
    public boolean getPartialCreditAssigned()
    {
        if( avItemScorer==null )
            return false;
        
        return avItemScorer.getPartialCreditAssigned(avItemResponse);
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
        if( avItemScorer==null )
            return false;
        
        return avItemScorer.isCorrect(avItemResponse);
    }
    

    @Override
    public float itemScore()
    {
        /*
         Type 1 items are scored on a 
        */
        
        if( avItemResponse!=null )
        {
            if( getSimletItemType().isPoints()  )
            {
                if( this.avItemResponse.getAvItemType().is100Score() && getMaxPointsArray()[0]>0 )
                    return avItemResponse.getAssignedPoints()*getMaxPointsArray()[0]/100f;
                
                else
                    return avItemResponse.getAssignedPoints();
            }
            
            return avItemResponse.getScore();
        }
        
        return 0;
    }
           
    

    @Override
    public float getAggregateItemScore( SimCompetencyClass simCompetencyClass )
    {
        return 0;
    }
    
    @Override
    public List<Integer> getSelectedSnSeqs()
    {
        List<Integer> out = new ArrayList<>();
        
        if( avItemScorer==null )
            return out;
        
        SimJ.Intn.Intnitem selIntnItemObj = avItemScorer.getSelectedIntnItem( intnObj, avItemResponse );

        if( selIntnItemObj!=null)
            out.add( selIntnItemObj.getSeq() );

        return out;        
    }
    
    
    
    
    
    
    @Override
    public float getDisplayOrder()
    {
        return avItemResponse!=null ? avItemResponse.getDisplayOrder() : 0;
    }
    
    
    
    @Override
    public void init( SimJ sj , List<SimletScore> simletScoreList, TestEvent te, boolean validItemsCanHaveZeroMaxPoints ) throws Exception
    {
        try
        {
            simJ = sj;

            this.validItemsCanHaveZeroMaxPoints = validItemsCanHaveZeroMaxPoints;
            
            String uniqueId = intnResultObj!=null ? intnResultObj.getUnqid() : null;
            
            if( uniqueId==null && avItemResponse!=null )
                uniqueId = avItemResponse.getItemUniqueId();
            
            // Next, look first by unique ids - this implies that the SimJ object has changed a bit. So be sure to use the
            if(  uniqueId!=null && !uniqueId.isEmpty() )
            {
                // LogService.logIt( "TestEvent.initScoreAndResponseLists() Seeking Sim.intn by uniqueId=" + intRespObj.getUnqid()  );
                int ct = 0;
                SimJ.Intn ii = null;
                
                for( SimJ.Intn intn : simJ.getIntn() )
                {
                    if( intn.getUniqueid()!= null && !intn.getUniqueid().isEmpty() && intn.getUniqueid().equals( uniqueId ) )
                    {
                        // LogService.logIt( "IactnResp.init() FOUND Sim.intn by uniqueId=" + intnResultObj.getUnqid()  );
                        ii=intn;
                        ct++;
                        // intnObj = intn;
                        // foundIt = true;
                        // break;
                    }
                }
                if( ct==1 && ii!=null )
                    intnObj = ii;
            }

            if( intnObj == null )
            {
                // next find the interaction in the descriptor
                for( SimJ.Intn intn : simJ.getIntn() )
                {
                    if( intn.getSeq() == intnResultObj.getNdseq() )
                    {
                        intnObj = intn;
                        break;
                    }
                }
            }
            
            // next get all IactnItemResp objects
            iactnItemRespLst = new ArrayList<>();

            // Not an important interaction response then. Ignore.
            if( intnObj == null  )
            {
                LogService.logIt( "IactnResp.init() could not find an interaction Object in SimDescriptor for seq=" + intnResultObj.getNdseq() );
                return;
            }

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

            if( intnObj.getTextscoreparam1() != null && !intnObj.getTextscoreparam1().isEmpty() )
                intnObj.setTextscoreparam1( UrlEncodingUtils.decodeKeepPlus(intnObj.getTextscoreparam1(), "UTF8") );


            // This is possible since interaction could be from a sim template. However, in this case we will ignore it since Sim Templates do not
            // afford any capability to add metadata to interactions for use in scoring or reports..
            if( simletScore == null )
            {
                LogService.logIt( "IactnResp.init() could not find a SimletScore for this IactnResp looking for simletId=" + intnObj.getSimletid() + ", intnObj.seq=" + intnObj.getSeq() + ", " + intnObj.getName() );
                return;
            }
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseScoredAvIactnResp.init() " + toString() );

            throw e;
        }
    }
    
       
    
    public ScoredItemParadigmType getScoredItemParadigmType()
    {
        if( avItemResponse!=null )
           return avItemResponse.getAvItemType().getScoredItemParadigmType();
        
        return ScoredItemParadigmType.UNKNOWN;
    }

    public AvItemResponse getAvItemResponse() {
        return avItemResponse;
    }

    
    @Override
    public void populateItemResponse( ItemResponse ir )
    {
         populateItemResponseCore( ir );

        if( intnObj.getIncprevselections()==1 )
            ir.setRepeatItemSimNodeSeq( intnObj.getSeq() );        
         
         ir.setResponseLevelId( ResponseLevelType.INTERACTION.getResponseLevelId() );

         ir.setIdentifier( ResponseLevelType.INTERACTION.computeIdentifier( ir, 0 ) );

         if( simletCompetencyScore!=null )
             ir.setSimCompetencyId( simletCompetencyScore.competencyScoreObj.getSimcompetencyid() );

         ir.setCompetencyScoreId( simletCompetencyId() );

         ScoredItemParadigmType sipt = ScoredItemParadigmType.getValue(this);

         ir.setItemParadigmTypeId( sipt.getScoredItemParadigmTypeId() );
         
         ir.setItemScore( itemScore() );
         
         if( intnResultObj!=null )
             ir.setSelectedSubnodeSeqIds( Integer.toString( intnResultObj.getSnseq() ) );
         
         else if( avItemResponse!=null && avItemResponse.getSelectedSubnodeSeq()>0 )
             ir.setSelectedSubnodeSeqIds( Integer.toString( avItemResponse.getSelectedSubnodeSeq() ) );
         
         if( avItemResponse!=null )
         {
             ir.setSubnodeSeq( avItemResponse.getItemSubSeq() );
             ir.setSimletSubnodeSeq( avItemResponse.getItemSubSeq() );
             
             if( avItemResponse.getDuration()>0 )
                 ir.setResponseTime( avItemResponse.getDuration() );
             
             ir.setSelectedValue( Long.toString( avItemResponse.getAvItemResponseId() ) );             
         }
         
         // LogService.logIt( "BaseScoredAvIactnResp.populateItemResponse() ir.getSubnodeSeq()=" + ir.getSubnodeSeq() + ", ir.getSelectedSubnodeSeqIds()=" + ir.getSelectedSubnodeSeqIds() );
         
         if( ir.getSubnodeSeq()>0 )
             ir.setSubnodeFormatTypeId( getSimJSubnode(ir.getSubnodeSeq()).getFormat() );

         if( ir.getSelectedSubnodeSeqIds()!=null && !ir.getSelectedSubnodeSeqIds().trim().isEmpty() )
         {
             String[] sqs = ir.getSelectedSubnodeSeqIds().split(",");
             
             if( sqs.length>0 && sqs[0]!=null && !sqs[0].trim().isEmpty() )
             {
                int snSq = Integer.parseInt(sqs[0].trim());

                ir.setSelectedSubFormatTypeIds( getSimJSubnode(snSq).getFormat() + "" );                
             }             
         }
         
         
         // these are only present for Scored Audio Item (Type 3).
         
         ir.setMetascore1( getMetaScore(1) );
         
         // Total Error Rate errors per word
         ir.setMetascore2( getMetaScore(2) );
         
         // Spelling errors per word
         ir.setMetascore3( getMetaScore(3) );
         
         // grammar errors per word
         ir.setMetascore4( getMetaScore(4) );
         
         // style errors per word
         ir.setMetascore5( getMetaScore(5) );

         // Essay Machine Score, if any
         ir.setMetascore6( getMetaScore(6) );

         // Essay Maching Confidence, if any
         ir.setMetascore7( getMetaScore(7) );

         // Essay plagiarized (1 or 0) if any  1 means plagiarized
         ir.setMetascore8( getMetaScore(8) );

         // nothing
         ir.setMetascore9( getMetaScore(9) );
         

         
         ir.setSimletItemTypeId( simletItemTypeId() );

         SimJ.Intn.Intnitem iitm = avItemScorer==null ? null : avItemScorer.getSelectedIntnItem( intnObj, avItemResponse );
         
        // If what's clicked is in the list of selected 
        if( iitm != null  )
        {
             ir.setSelectedValue( StringUtils.truncateString(UrlEncodingUtils.decodeKeepPlus( iitm.getContent() ), 1900 )   );
             
             ir.setSubnodeSeq( intnResultObj==null ? iitm.getSeq() : intnResultObj.getSnseq() );
             
             // if Intn Item has any settings.
             if( iitm.getScoreparam1()>0 || iitm.getScoreparam2()>0 || iitm.getScoreparam3()>0 ||iitm.getTruescore()>0 )
             {
                ir.setScoreParam1( iitm.getScoreparam1() );
                ir.setScoreParam2( iitm.getScoreparam2() );
                ir.setScoreParam3( iitm.getScoreparam3() );
                ir.setTrueScore( iitm.getTruescore() );
             }
             
             // Else use Intn
             else if( intnObj!=null )
             {
                ir.setScoreParam1( intnObj.getScoreparam1() );
                ir.setScoreParam2( intnObj.getScoreparam2() );
                ir.setScoreParam3( intnObj.getScoreparam3() );
                ir.setTrueScore( intnObj.getTruescore() );                 
             }
        }
        else if( intnObj!=null )
        {
           ir.setScoreParam1( intnObj.getScoreparam1() );
           ir.setScoreParam2( intnObj.getScoreparam2() );
           ir.setScoreParam3( intnObj.getScoreparam3() );
           ir.setTrueScore( intnObj.getTruescore() );                 
        }
         
         //else
         //{//
         //    ir.setSelectedValue(avItemScorer.getSelectedValueForItemResponse(intnObj, avItemResponse )  );             
         //}
        
        if( getSimletItemType().isDichotomous() || getSimletItemType().isPoints())
            ir.setCorrect( correct() ? 1 : 0 );

        if( getSimletItemType().isDichotomous() )
            ir.setCorrectSubnodeSeqIds( getCorrectSubnodeSeqStr() );                
        
    }
        
    
    private SimJ.Intn.Intnitem getSimJSubnode(int snSeq )
    {
        if( this.intnObj==null )
            return null;
        
        for( SimJ.Intn.Intnitem i : intnObj.getIntnitem() )
        {
            if( i.getSeq()==snSeq )
                return i;
        }
        
        return null;
    }
    
    
    @Override
    public List<TextAndTitle> getTextAndTitleList()
    {
        // LogService.logIt("BaseScoredAvIactnRespgetTextAndTitleList() uniqueId=" + intnObj.getUniqueid() + ", " + (avItemScorer==null ? "null" : (avItemScorer.getTextAndTitleList()!=null) )  );        
        
        if( avItemScorer!=null && avItemScorer.getTextAndTitleList()!=null )
        {            
            List<TextAndTitle> out = avItemScorer.getTextAndTitleList();
            if( out!=null )
            {
                int orderIndex = 1;
                for( TextAndTitle tt : out )
                {
                    // tt.setOrder( intnResultObjO.getSq()*100 + orderIndex );
                    tt.setSequenceId( intnResultObjO.getSq()*100 + orderIndex );
                    orderIndex++;
                }
                return out;
            }            
        }

        return new ArrayList<>();
    }
    
    
    @Override
    public boolean hasValidScore()
    {
        return this.avItemResponse!=null && this.avItemScorer!=null && !avItemScorer.isPendingScoring();
    }    
    
    public boolean hasVoiceVibesReport()
    {
        return this.avItemResponse!=null && this.avItemResponse.hasVoiceVibesReport();
        // return this.avItemResponse.getVoiceVibesStatusType().readyForScoring() && avItemResponse.getVoiceVibesResponseStr()!=null && !avItemResponse.getVoiceVibesResponseStr().isEmpty();
    }
    
    public synchronized VoiceVibesResult getVoiceVibesResult()
    {
        if( voiceVibesResult!=null )
            return voiceVibesResult;
        
        if( avItemResponse==null || !avItemResponse.getAvItemType().supportsVoiceVibesAnalysis() || !hasVoiceVibesReport() )
            return null;
        
        voiceVibesResult = new VoiceVibesResult( avItemResponse.getVoiceVibesResponseStr(), null, getWordCount() );
        
        return voiceVibesResult;
    }

    
    public int getWordCount()
    {
        if( this.avItemResponse==null || avItemResponse.getSpeechText()==null || avItemResponse.getSpeechText().isEmpty() )
            return 0;
        
        try
        {
            Speech2TextResult s2tr = new Speech2TextResult( avItemResponse.getSpeechText() );

            return StringUtils.numWords( s2tr.getConcatTranscript(0, "" ) );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "BaseScoredAvIactnRespgetWordCount() " );
            
            return 0;
        }
    }
    
    @Override
    public List<CaveatScore> getCaveatScoreList()
    {
        if( this.avItemScorer!=null )
            return this.avItemScorer.getCaveatScoreList();
        
        return new ArrayList<>();
    }
    
}
