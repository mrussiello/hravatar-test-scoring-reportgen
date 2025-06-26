/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.av;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.event.AvItemResponse;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.user.User;
import com.tm2score.global.Constants;
import com.tm2score.googlecloud.Speech2TextResult;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public class BaseAvItemScorer {

    public static float MIN_ADJUSTED_SIMILARITY_FOR_CORRECT_MATCH = 0.35f;
    public static float FEATURES_SCORE_WEIGHT = 1f; // 0.5f;  // changed by Mike 1-16-2018 to let VoiceVibes dominate WHEN present.
    public static float VOICEVIBES_SCORE_WEIGHT = 5f; // 1f;
    public static float ESSAY_SCORE_WEIGHT = 20f; // 1f;
    public Locale locale;
    public String teIpCountry;
    
    // protected float[] metaScores;
    public String selectedValue = null;
    
    public List<TextAndTitle> textAndTitleList = null;
    
    public boolean pendingExternalScores = false;
    
    public boolean scoringComplete = false;
    
    public User user;
    public TestEvent testEvent;
    
    // protected float points = 0;
        
    public List<String> getWordsToIgnoreLc()
    {
        List<String> out = new ArrayList<>();
        
        if( user==null )
            return out;

        if( user.getFirstName()!=null && !user.getFirstName().isBlank() )
            out.add( user.getFirstName().toLowerCase() );

        if( user.getLastName()!=null && !user.getLastName().isBlank() )
            out.add( user.getLastName().toLowerCase() );

        if( user.getEmail()!=null && !user.getEmail().isBlank() )
            out.add( user.getEmail().toLowerCase() );

        return out;        
    }
    
    
    public boolean getPartialCreditAssigned( AvItemResponse avItemResponse )
    {
        return false;
    }    
    
    public boolean isCorrect( AvItemResponse avItemResponse )
    {
        return false;
    }

    public void addTextAndTitle( TextAndTitle ttl )
    {
        if( textAndTitleList ==null )
            textAndTitleList=new ArrayList<>();
        
        if( ttl!=null )
            textAndTitleList.add(ttl);
    }
    
    
    public List<TextAndTitle> getTextAndTitleList()
    {
        return textAndTitleList;
    }

    public boolean allowsSupplementaryCompetencyLevelTextAndTitle() 
    {
       return false;
    }
    
    public boolean isPendingScoring()
    {
        return pendingExternalScores;
    }
    
    
    public String getTextAndTitleIdentifier( ScorableResponse sr, SimJ.Intn intn)
    {
        String idt = sr==null ? null : UrlEncodingUtils.decodeKeepPlus( sr.getExtItemId() );
        
        if( idt == null || idt.isEmpty() )
        {            
            idt = UrlEncodingUtils.decodeKeepPlus( intn.getUniqueid() );
            
            if( idt==null || idt.isEmpty() )
                idt = UrlEncodingUtils.decodeKeepPlus(intn.getId());

            if( idt == null || idt.isEmpty() )
                idt = Integer.toString( intn.getSeq() );
        } 
        
        return idt;
    }
    
    public TextAndTitle getItemScoreTextTitle(int includeItemScoreTypeId, ScorableResponse sr )
    {
        return null;
    }
    
    public SimJ.Intn.Intnitem getSelectedIntnItem( SimJ.Intn intn, AvItemResponse avItemResponse )
    {
        if( avItemResponse == null )
            return null;
        
        if( avItemResponse.getSelectedSubnodeSeq()<=0 )
        {
            for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
            {
                if( iitm.getFormat()==G2ChoiceFormatType.SUBMIT.getG2ChoiceFormatTypeId() )
                    return iitm; 
            }            
        }
        
        else
        {
            for( SimJ.Intn.Intnitem iitm : intn.getIntnitem() )
            {
                if( iitm.getSeq()==avItemResponse.getSelectedSubnodeSeq() )
                    return iitm; 
            }
        }
        
        return null;
    }
    
    public float[] getMaxPointsArray()
    {
        return new float[4];
    }
    
    public float[] getMetaScores( AvItemResponse iir )
    {
        return null;
    }

    
    public Speech2TextResult getSpeechToTextResult( AvItemResponse avItemResponse ) throws Exception
    {
        if( avItemResponse!= null )
            return new Speech2TextResult( avItemResponse.getSpeechText() );
        
        throw new Exception( "AvItemResponse is null" );
    }
    
    public List<String> getSpeechTextVals( Speech2TextResult sr, boolean concatResults, boolean lastResultOnly ) throws Exception
    {        
        if( concatResults )
          return sr.getConcatStrList();
                    
        if( lastResultOnly )
            return sr.getLastResultTranscriptAlts();
        
        return sr.getTranscriptAlts();
    }
    
    /*
    public List<String> getMatchValuesFmTextScoreParams( SimJ.Intn intn ) throws Exception
    {
        List<String> out = new ArrayList<>();
        
        if( intn.getTextscoreparam1()==null || intn.getTextscoreparam1().trim().isEmpty() )
            return out;
        
        // Must remove any brackets.
        String tsp = IvrStringUtils.removeAllBracketsForVoice( UrlEncodingUtils.decodeKeepPlus( intn.getTextscoreparam1() ) );
        
        // choices are always delimited by ;
        for( String m : tsp.split(";") )
        {
            if( m.trim().isEmpty() )
                continue;
            
            m = removeBranchingInfo( m.trim() );
            
            m = m.trim();
            
            if( m.isEmpty() )
                continue;
                        
            // choice may have 
            out.add( m );
        }
        
        return out;
    }
    */
    
    protected String removeBranchingInfo( String inStr )
    {
        if( inStr == null || !inStr.contains("|") )
            return inStr;
        
        return inStr.substring(0,inStr.indexOf("|") );
    }
        
    /*
    public Map<String,Float> getIntnLevelMatchPointsMap( SimJ.Intn intn ) 
    {
        Map<String,Float> out = new HashMap<>();
        
        if( intn.getTextscoreparam1()==null || intn.getTextscoreparam1().isEmpty() )
            return out;
        
        String tsp = IvrStringUtils.getPointsValueFmTextScoreParam(UrlEncodingUtils.decodeKeepPlus( intn.getTextscoreparam1() ));
        
        if( tsp == null || tsp.trim().isEmpty() )
            return out;
        
        // LogService.logIt( "BaseAvItemScorer.getIntnLevelMatchPointsMap()  pointsStr=" + tsp );
        
        String[] vals = tsp.split( ";" );
        
        String val;
        String ptStr;
        float pts;
        
        for( int i=0;i<vals.length-1; i+=2 )
        {
            val = vals[i];
            ptStr = vals[i+1];
            
            if( val==null || val.trim().isEmpty() )
                continue;
            if( ptStr==null || ptStr.trim().isEmpty() )
                ptStr="0";
            
            val = val.trim();
            ptStr = ptStr.trim();
            
            try
            {
                pts = Float.parseFloat(ptStr );
                out.put( val, pts);
            }
            catch( NumberFormatException e )
            {
                LogService.logIt(e,"BaseAvItemScorer NONFATAL Unable to parse Points value: " + ptStr + ", Full Points Str=" + tsp );
                
            }
        }
        
        return out;
    }
    */
        
    public boolean isResponseCorrect( float similarity , float confidenceFactor )
    {
        float adjSimilarity = similarity*confidenceFactor;
        
        return adjSimilarity >= MIN_ADJUSTED_SIMILARITY_FOR_CORRECT_MATCH;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public String getSelectedValueForItemResponse( SimJ.Intn intn, AvItemResponse avItemResponse )
    {
        if( selectedValue!=null && selectedValue.contains("[AUDIO]") )
            selectedValue = selectedValue.substring(0,selectedValue.indexOf("[AUDIO]") ); 
                
        if( selectedValue!=null && selectedValue.length()>254 )
            return selectedValue.substring( 0,254 );
        
        return selectedValue;
    }

    //public float getPoints() {
    //    return points;
    //}
    
    public Map<Integer,String> getTextInputTypeMap( SimJ.Intn intn, AvItemResponse avItemResponse )
    {
        return new HashMap<>();
    }
    
    public int getSingleTextInputTypeId( String textScoreParam, AvItemResponse avItemResponse )
    {
        if( textScoreParam==null || textScoreParam.trim().isEmpty() || !IvrStringUtils.containsKey("[" + Constants.TEXTINPUTTYPE_KEY + "]", textScoreParam, true) )
            return 0;
            
        int textInputTypeId = IvrStringUtils.getTextInputTypeIdFrmTextScrParam(textScoreParam);
        
        return textInputTypeId<=0 ? 0 : textInputTypeId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    
    
}

