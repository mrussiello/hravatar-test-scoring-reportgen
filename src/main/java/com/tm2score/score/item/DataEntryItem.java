/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.item;

import com.tm2score.service.LogService;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class DataEntryItem {

    //static float CHARS_PER_WORD = 4.5f;

    String keyStr;

    String typedStr;

    //float seconds;

    // raw words per minute
    //float wpm=0;

    /**
     *
     */
    //float accuracyAdjustedWpm=0;

    //
    //float accuracy=0;
    
    /**
     * This is used by pure data entry and 10 key where the text is a single number or an address line, and it is either correct or not correct.
     */
    //boolean oneWordPerLine = false;
    
    int typedKeystrokes = 0;
    int errCount = 0;
    String keyValue = "";
    int seq;
    

    boolean hasValidScore = false;

    public DataEntryItem( int seq, String key, String typed)
    {
        keyStr = key;
        typedStr = typed;
        this.seq = seq;
        //seconds = secs;

        // LogService.logIt( "DataEntryItem() " + key + ", typed=" + typed );
    }

    public void calculate()
    {
        if( keyStr==null || keyStr.trim().isEmpty() )
            return;

        keyStr = keyStr.trim();
        
        if( typedStr==null || typedStr.trim().isEmpty() )
        {
            hasValidScore = true;
            errCount=1;
            typedKeystrokes=0;
            return;
        }

        List<String> keys = new ArrayList<>();
        
        if( keyStr.indexOf("|")>0 )
        {
            String[] vals = keyStr.split("\\|");
            
            for( String v : vals )
            {
                if( v.trim().isEmpty() )
                    continue;
                keys.add( v.trim() );  
                if( keyValue.isEmpty() )
                    keyValue=v.trim();
            }
        }
        else
        {
            keys.add(keyStr);
            keyValue=keyStr;
        }
        
        typedStr = typedStr.trim();
        typedKeystrokes = typedStr.length();
        
        boolean hasMatch = false;
        
        for( String k : keys )
        {
            if( typedStr.equals( k ) )
            {
                hasMatch=true;
                keyValue=k;
                break;
            }
        }

        // LogService.logIt( "DataEntryItem.calculate() match=" + hasMatch + ", typed=" + typedStr );
        
        errCount = hasMatch ? 0 : 1;
        
        hasValidScore = true;
    }
    
    public String getSelectedValueString()
    {
        return seq + "~" + (correct() ? "1" : "0" ) + "~" + typedStr;
    }
    
    public int getTypedKeystrokes()
    {
        return typedKeystrokes;
    }
    
    public boolean correct()
    {
        return getHasValidScore() && errCount<=0;
    }
  
    public boolean getHasValidScore() {
        return hasValidScore;
    }

    public String getTypedStr() {
        return typedStr;
    }

    public String getKeyValue() {
        return keyValue;
    }

}


