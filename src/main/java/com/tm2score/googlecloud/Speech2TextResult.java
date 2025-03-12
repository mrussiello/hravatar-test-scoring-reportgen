/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.googlecloud;

import com.tm2score.ivr.IvrScoreException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class Speech2TextResult {
    private IvrScoreException ivrScoreException;
    
    /**
     * Google returns an array of "Result" where each result is
     *    An array of alternative transcripts
     *    plus a confidence for the first transcript
     * 
     * List of Object[] where each object[] represents one result.
     *   data[0] = List<String> - of all alternative transcripts for a result
     *   data[1] = Float - the confidence of the first alternative transcript in the result.
     */
    private List<Object[]> resultList;

    public Speech2TextResult( IvrScoreException e )
    {
        ivrScoreException=e;
    }
    
    public Speech2TextResult( List<Object[]> rl )
    {
        this.resultList=rl;
    }

    // Unpacks the db speech to text value.
    /**
     *    
     * expects a Packed string 
     * tran1a,tran1b,tran1c;confidence1;tran2a,tran2b|confidence2; ... 
     * 
     * where tranXY is url encoded.
     * 
     * 
     * @param dbSpeech2TextValue
     * @throws Exception 
     */
    public Speech2TextResult( String dbSpeech2TextValue ) throws Exception
    {
        List<Object[]> rl = new ArrayList<>();

        if( dbSpeech2TextValue!=null && !dbSpeech2TextValue.isEmpty() )
        {            
            String[] pairs = dbSpeech2TextValue.split(";");
            
            String trans,conStr;
            
            for( int i=0;i<pairs.length-1;i+=2 )
            {
                trans = pairs[i];
                conStr = pairs[i+1];
                
                if( trans==null || trans.isEmpty() )
                    continue;
                
                if( conStr==null || conStr.isEmpty() )
                    conStr = "0";
                
                 rl.add( new Object[]{parsePackedTranString( trans ),Float.parseFloat(conStr)} );
            }
        }
        
        resultList=rl;
    }
    
    
    public List<String> getConcatStrList()
    {
        List<String> out = new ArrayList<>();
        
        String t;
        
        for( int i=0; i<10; i++ )
        {
            t = getConcatTranscript(i, null );
            
            // found an index that doesn't have anything anywhere.
            if( t==null || t.trim().isEmpty() )
                break;
            
            out.add( t.trim() );
        }
        
        return out;
    }
    
    /**
     * When set to provide only one alternative, this provides a concat of all results for that alternative index.
     * 
     * Uses only the first string in each result.
     * 
     * @return 
     */
    public String getConcatTranscript()
    {
        return getConcatTranscript(0, null );
    }

    public String getConcatTranscript( int index, String delim )
    {
        StringBuilder sb = new StringBuilder();
        
        if( resultList==null || resultList.isEmpty() )
            return sb.toString();
        
        List<String> tl;
        String t;
        
        if( delim==null )
            delim = "";
        
        for( Object[] a : resultList )
        {
            if( a==null )
                continue;
            
            tl = (List<String>) a[0];
            
            if( tl==null || tl.isEmpty() || tl.size()<index+1 )
                continue;
            
            t = tl.get( index );
            
            if( t==null || t.trim().isEmpty() )
                continue;
                        
            t=t.trim();
            
            if( sb.length()>0 )
                sb.append( delim + " " );
            
            sb.append(t);
        }
        
        return sb.toString();
    }

    /**
     * This lets you get the last logical thing the client said as the answer.
     * 
     * @return 
     */
    public List<String> getLastResultTranscriptAlts()
    {
        if( resultList==null || resultList.isEmpty() )
            return new ArrayList<>();
        
        Object[] a; //  = resultList.get( resultList.size()-1 );

        for( int i=resultList.size()-1; i>=0;  i-- )
        {
            a = resultList.get(i);
            
            if( a==null || a[0]==null || ((List<String>)a[0]).isEmpty() )
                continue;
            
            return (List<String>)a[0];                
        }
        
        return new ArrayList<>();
    }

    
    
    
    /**
     * Expects a string 
     *    token1,token2,token3, ... 
     *    where each token is URL-encoded.
     * 
     * @param inStr
     * @return
     * @throws Exception 
     */
    private List<String> parsePackedTranString( String inStr ) throws Exception
    {
        List<String> out = new ArrayList<>();
        
        if( inStr==null || inStr.isEmpty() )
            return out;
        
        
        for( String v : inStr.split(",") )
        {
            if( v==null || v.isEmpty() )
                continue;
            
            out.add( URLDecoder.decode(v, "UTF8" ) );
        }  
        
        return out;
    }
    

    @Override
    public String toString() {
        return "Speech2TextResult{" + "ivrScoreException=" + ivrScoreException.toString() + ", confidence=" + getAvgConfidence() + ", resultList.size=" + (resultList==null ? "is null" : resultList.size()) + '}';
    }
    
    
    /**
     * returns a Packed string 
     * tran1a,tran1b,tran1c;confidence1;tran2a,tran2b|confidence2; ... 
     * 
     * where tranXY is url encoded.
     * 
     * @return
     * @throws Exception 
     */
    public String encodeTranscriptForStorage() throws Exception
    {
        StringBuilder sb = new StringBuilder();
        
        if( resultList==null || resultList.isEmpty() )
            return sb.toString();
        
        List<String> t;
        
        StringBuilder tsb;
        
        for( Object[] a : resultList )
        {
            if( a==null )
                continue;
            
            t = (List<String>) a[0];
            
            if( t==null || t.isEmpty() )
                continue;
                                
            // t=t.trim();
            
            if( sb.length()>0 )
                sb.append( ";" );
            
            tsb = new StringBuilder();
            
            for( String v : t )
            {
               if( tsb.length()>0 )
                   tsb.append( "," );
               
               tsb.append( URLEncoder.encode( v, "UTF8") );                
            }

            sb.append( tsb.toString() + ";" + ((Float)a[1]).floatValue());            
        }
        
        return sb.toString();
    }
    
    public float getAvgConfidence()
    {
        if( resultList==null || resultList.isEmpty() )
            return 0;
        
        float total=0;
        float ct=0;
        
        for( Object[] a : resultList )
        {
            if( a[1]==null )
                a[1]=new Float(0);
            
            total += ((Float)a[1]).floatValue();
            ct++;
        }
        
        if( ct<=0 )
            return 0;
        
        return total/ct;
    }
    
    /**
     * When specified to present multiple alternatives, this provides a list of each guess.
     * 
     * @return 
     */
    public List<String> getTranscriptAlts()
    {
        List<String> out = new ArrayList<>();
        
        if( resultList==null || resultList.isEmpty() )
            return out;
        
        for( Object[] a : resultList )
        {
            if( a[0]!=null )
                out.addAll( (List<String>) a[0] );
        }
        
        return out;
    }
    

    public float getLastConfidence()
    {
        if( resultList==null || resultList.isEmpty() )
            return  0;
        
        Object[] a; //  = resultList.get( resultList.size()-1 );

        for( int i=resultList.size()-1; i>=0;  i-- )
        {
            a = resultList.get(i);
            
            if( a==null || a[0]==null || ((List<String>)a[0]).isEmpty() )
                continue;
            
            return a[1]==null ? 0 : ((Float) a[1]).floatValue();                
        }
        
        return 0;        
    }
    
    public float getHighestConfidence()
    {
        if( resultList==null || resultList.isEmpty() )
            return 0;
        
        float cMax=0;
        
        for( Object[] a : resultList )
        {
            if( a[1]==null )
                a[1]=new Float(0);
            
            if( ((Float)a[1]).floatValue()> cMax )
                cMax = ((Float)a[1]).floatValue();
        }
        
        return cMax;
    }

    public String getHighestConfidenceEntry()
    {
        if( resultList==null || resultList.isEmpty() )
            return null;
        
        float cMax=0;
        Object[] oMax = null;
        
        for( Object[] a : resultList )
        {
            if( a[1]==null )
                a[1]=new Float(0);
            
            if( ((Float)a[1]).floatValue()> cMax )
            {
                cMax = ((Float)a[1]).floatValue();
                oMax = a;
            }
        }
        
        if( oMax==null )
            return null;
        
        if( oMax[0]==null )
            return null;
        
        List<String> alts = (List<String>) oMax[0];
        
        if( alts.isEmpty() )
            return null;
        
        return alts.get(0);
    }
    
    
    
    
    public boolean isUnsuccessful()
    {
        return ivrScoreException!=null || resultList==null || resultList.isEmpty();
    }
    
    public boolean isSuccessful()
    {
        return ivrScoreException==null && resultList!=null && !resultList.isEmpty();
    }
    
    

    public IvrScoreException getIvrScoreException() {
        return ivrScoreException;
    }

    public List<Object[]> getResultList() {
        return resultList;
    }

    public void setResultList(List<Object[]> rl) {
        this.resultList = rl;
    }    

}
