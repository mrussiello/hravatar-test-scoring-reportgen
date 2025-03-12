/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.item;

import com.tm2score.service.LogService;

/**
 *
 * @author Mike
 */
public class TypingItem {

    static float MIN_WORDS_TYPED_FRACTION = 0.85f;
    
    static float CHARS_PER_WORD = 4.5f;

    String keyStr;

    String typedStr;

    float seconds;
    
    boolean useWordsTypedAsTotalKeyWords = false;

    // raw words per minute
    float wpm=0;

    /**
     *
     */
    float accuracyAdjustedWpm=0;

    // 100 - mistakes per 100 words  0 mistakes = 100
    float accuracy=0;
    

    boolean hasValidScore = false;

    public TypingItem( String key, String typed, float secs, boolean useWordsTypedAsTotalKeyWords)
    {
        keyStr = key;
        typedStr = typed;
        seconds = secs;
        this.useWordsTypedAsTotalKeyWords=useWordsTypedAsTotalKeyWords;
        
        // LogService.logIt( "TypingItem() " + key + ", typed=" + typed + ", secs=" + secs );
    }

    public void calculate()
    {
        if( seconds<=4 )
            return;

        if( typedStr==null || typedStr.isBlank() )
            return;

        if( keyStr==null || keyStr.isBlank() )
            return;
        
        // Convert key to word array.
        String[] wordsKey = keyStr.split(" ");

        String[] wordsTyped = typedStr.split(" ");

        // truncate key words if needed.
        if( useWordsTypedAsTotalKeyWords && wordsKey.length>wordsTyped.length )
        {
            String[] ta = new String[wordsTyped.length];
            for( int i=0;i<wordsTyped.length;i++ )
            {
                ta[i] = wordsKey[i];
            }
            LogService.logIt( "TypingItem.calculate() truncating wordsKey from " + wordsKey.length + " to " + ta.length + ", keyStr=" + keyStr );
            wordsKey=ta;
        }


        
        // LogService.logIt( "TypingItem.calculate() typedStr=" + typedStr  + ", keyStr=" + keyStr );
        // LogService.logIt( "TypingItem.calculate() keyStr=" + keyStr + " keyWordCount=" + wordsKey.length + ", typedStr=" + typedStr + ", typedWordCount=" + wordsTyped.length );
        // LogService.logIt( "TypingItem.calculate() keyWordCount=" + wordsKey.length + ", typedWordCount=" + wordsTyped.length );

        if( wordsKey.length<=0 )
            return;

        int errs = 0;

        String wk,wt;

        int bandBot,bandTop;

        boolean matched;

        // String procStr="";

        float equivWordsKey = 0;
        float equivWordsTyped = 0;
        
        float totalWordsKey = wordsKey.length;
        float totalWordsTyped = wordsTyped.length;
        
        //float equivWordsTypedCorrectly = 0;

        // get the exact number of real 'words' in the key
        for (String w : wordsKey) {

            if( w==null || w.isBlank() )
                continue;

            equivWordsKey += getEquivWords(w);
        }
        
        // get the exact number of real 'words' in the typed response
        for (String w : wordsTyped) {

            if( w==null || w.isBlank() )
                continue;

            equivWordsTyped += getEquivWords( w );
        }

        if( totalWordsTyped < MIN_WORDS_TYPED_FRACTION*totalWordsKey )
        {
            LogService.logIt( "TypingItem.calculate() totalWordsTyped=" + totalWordsTyped + " is Less than required fraction of totalWordsKey=" + totalWordsKey + " . Skipping. Note: equivWordsTyped=" + equivWordsTyped + ", equivWordsKey=" + equivWordsKey );
            return;
        }
        
        // accuracy = 100 - mistakes per 100 words.
        float maxAccuracy = 100f;
        float maxAdjWpm = 200;
        
        // if the number of equivalent words is way off. 
        if( equivWordsKey>0 )
        {
            // less than half the words entered. 
            if( equivWordsTyped<=0 )
                maxAccuracy=0f;
            
            // more words in key than typed
            else if( equivWordsKey > equivWordsTyped )
            {
                // max proportion of words typed to key.
                maxAccuracy = 100*((float)equivWordsTyped)/((float)equivWordsKey);
            }
            
            // more words typed than in key
            else if( equivWordsTyped > equivWordsKey )
            {
                // max proportion of words typed to key.
                maxAccuracy = 100 - 100*((float)(equivWordsTyped-equivWordsKey))/((float)equivWordsKey);
                
                if( maxAccuracy<0 )
                    maxAccuracy=0;
            }            
        } 

        // LogService.logIt( "TypingItem.calculate() seconds=" + seconds + ", equivWordsKey=" + equivWordsKey + ", equivWordsTyped=" + equivWordsTyped );

        for( int ik=0; ik<wordsKey.length; ik++ )
        {
            matched = false;

            wk = wordsKey[ik];

            if( wk == null || wk.isBlank() )
                continue;

            bandBot = ik<=0 || ik - errs  - ik/20 <=0  ? 0 : ik - errs - ik/20;

            if( bandBot < 0 )
                bandBot = 0;

            bandTop = ik + errs + 1 + ik/20;


            for( int it=bandBot; it<bandTop; it++ )
            {
                if( it>=wordsTyped.length )
                    break;

                wt = wordsTyped[it];

                if( wt==null || wt.isBlank() )
                    continue;

                if( wt.equals( wk ) )
                {
                    matched=true;
                    break;
                }
            }

            if( !matched )
            {
                errs++;

                // procStr += " No Match for " + wk + " idx=" + ik + ", band=" + bandBot + "-" + bandTop + "\n";
            }

            //else
            //    equivWordsTypedCorrectly += getEquivWords( wk );
        }

        //if( equivWordsTypedCorrectly > equivWordsTyped )
        //    equivWordsTypedCorrectly = equivWordsTyped;

        float errsPer100 = equivWordsKey>0 ? 100f*((float) errs)/((float)equivWordsKey) : errs;

        accuracy = 100f - errsPer100;

        if( accuracy<0 )
            accuracy=0;
        
        if( accuracy>maxAccuracy )
        {
            LogService.logIt( "TypingItem.calculate() Changed accuracy from computed accuracy=" + accuracy + " to maxAccuracy=" + maxAccuracy + " due to max accuracy constraint." );
            accuracy = maxAccuracy;
        }

        
        // this is to deal with people who type way too and care nothing for accuracy.
        if( accuracy<=40 )
            maxAdjWpm = (float) Math.rint(accuracy/4f);
        
        else if( accuracy<=80 )
            maxAdjWpm = (float) Math.rint(accuracy/2f);
        
        wpm = seconds>0 ? 60*equivWordsTyped/seconds : 0;

        accuracyAdjustedWpm = wpm*accuracy/100f;
        
        if( accuracyAdjustedWpm > maxAdjWpm )
            accuracyAdjustedWpm = maxAdjWpm;

        // accuracyAdjustedWpm = seconds>0 ? 60*equivWordsTypedCorrectly/seconds : 0;

        // LogService.logIt( "TypingItem.calculate() Finished. Errors=" + errs + " wpm=" + wpm + ", adjustedWpm=" + accuracyAdjustedWpm + ", accuracy="+ accuracy );
        //LogService.logIt( "TypingItem.calculate() Process Str=" + procStr );

        hasValidScore = seconds>0 && equivWordsKey>0;

// if( !procStr.isEmpty() )

    }

    
    public String getSelectedValueString( int seq )
    {
        return seq + "~0~" + typedStr;
    }
    
    
    
    public static float getEquivWords( String inStr )
    {
       if( inStr==null || inStr.length()==0 )
           return 0;

        return ((float) inStr.length() )/((float)CHARS_PER_WORD );
        // return Math.round( ( (float) inStr.length() )/CHARS_PER_WORD );
    }

    public float getWpm() {
        return wpm;
    }

    public float getAccuracyAdjustedWpm() {
        return accuracyAdjustedWpm;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public boolean getHasValidScore() {
        return hasValidScore;
    }

}


