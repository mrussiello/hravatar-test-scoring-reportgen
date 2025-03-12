/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2score.score.item;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class IntnClkStrmItem {

    SimJ simJ;
    
    float points = 0;
    
    int intnSeq = 0;
    int intnSnSeq = 0;
    
    int selNdSeq=0;
    int selSnSeq=0;
    
    
    boolean hasValidScore = false;
    
    /*
     int[0]=nseq
     int[1]=sseq clicked by user
    */
    // List<int[]> responsePairs;
    
    int respNdSeq;
    int respSnSeq;
    String respTxt;

    /*
     * dat[0]=UniqueId = HKM,alt,ctrl,shft,key 
    */
    // String responseHotKeyVals;
    String responseHotKeyVal = null;
    
    
    /*
     * dat[0]=Integer nseq
       dat[1]=Integer sseq
       dat[2]=Text fields to match against. (loose match only).
       dat[3]=Float points
    */
    List<Object[]> clickScoreValues;
    
    
    /*
     * dat[0] = Integer = node seq of equivalent node or 0
     * dat[1] = Integer = subnode seq of equivalent node or 0
     * dat[2] = String = HKM,alt,ctrl,shft,key 
     * dat[3] = Float = points
    */
    List<Object[]> hotKeyScoreValues;
    

    public IntnClkStrmItem( int intnNdSeq, int intnSnSeq, String keyStr, String respStr, SimJ simJ )
    {
        this.simJ=simJ;
        this.intnSeq = intnNdSeq;
        this.intnSnSeq = intnSnSeq;
        parseResponseStr( respStr );
        parseScoreStr( keyStr );

        // LogService.logIt("IntnClkStrmItem() Intn.NodeSeq=" + intnSeq + "-" + intnSnSeq + ", keyStr=" + keyStr + ", responseStr=" + respStr + ", keys=" + this.clickScoreValues.size() + " respNdSeq=" + respNdSeq + ", respSnSeq=" + respSnSeq +  ", respFrmInpt=" + respTxt );
    }
    
    
    /**
     * The response string should be in format nodeseq,subnodeseq,textinput nodeseq,subnodeseq, etc.
     * @param respStr 
     */
    private void parseResponseStr( String respStr )
    {
        try
        {
            //if( responsePairs!=null )
            //    return;
            
            //responsePairs = new ArrayList<>();
            
            // responseHotKeyVals = new ArrayList<>();
            // LogService.logIt( "IntnClkStrmItem.parseResponseStr() AAA parsed " + respStr );            
            
            if( respStr==null || respStr.trim().isEmpty() )
                return;
            
            respStr = respStr.trim();
            
            int idx = respStr.indexOf("HKM;");
            
            if( idx>=0 )
            {
                responseHotKeyVal = respStr.substring(idx, respStr.length() );
                // LogService.logIt("IntnClkStrmItem.parseResponseStr() PARSED A HOT KEY RESPONSE Intn.NodeSeq=" + intnSeq + "-" + intnSnSeq + ", parsed " + respStr  + ", to responseHotKeyVal=" + responseHotKeyVal );
                return;
            }
            
            idx = respStr.indexOf(",");
            
            if( idx<=0 )
                return;
            
            respNdSeq = Integer.parseInt( respStr.substring(0,idx) );
            
            int idx2 = respStr.indexOf(",", idx+1 );
            
            if( idx2<=0 )
                return;
            
            respSnSeq = Integer.parseInt( respStr.substring(idx+1,idx2) );
            
            respTxt = respStr.substring(idx2+1,respStr.length() ); 
            
            // LogService.logIt("IntnClkStrmItem.parseResponseStr() PARSED A CLICK RESPONSE Intn " + intnSeq + "-" + intnSnSeq + ", parsed " + respStr  + ", to NdSeq=" + respNdSeq + ", respSnSeq=" + respSnSeq + ", respFrmInpt=" + respTxt );            
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "IntnClkStrmItem.parseResponseStr() ERROR PARSING RESPONSE Intn " + intnSeq + "-" + intnSnSeq + ", " + respStr );
        }        
    } 
    
    
    /**
     * The format of the ClickStream.textScoreParam1 values are ether hot key values or click values. 
     * 
     * Hot key values start with "HKM;alt;ctr;shft;code|equiv uniqueid or empty|equiv subnodeseq or 0|points
     * 
     * Click values are in format uniqueid|subnodeseq|text match values delim by ~|points 
     * 
     * Notice both have 4 tokens each. 
     *    Hot Keys
     *          0 - key code str
     *          1 - equivalent unique id or empty
     *          2 - equivalent subnode seq or 0
     *          3 - points
     * 
     *    Click Values
     *          0 - unique id of node containing clickable or the form node (if comparing input)
     *          1 - subnode seq of subnode containing clickable or the form node (if comparing input)
     *          2 - any text values for matching, delimited by ~  example michael~John~Mary Jane~Steven or "true" 
     *          3 - points
     * 
     * @param inStr 
     */
    private void parseScoreStr( String keyStr )
    {
        try
        {
            if( clickScoreValues!=null )
                return;
            
            clickScoreValues = new ArrayList<>();            
            hotKeyScoreValues = new ArrayList<>();
            
            if( keyStr==null || keyStr.trim().isEmpty() )
                return;
            
            keyStr = keyStr.trim();
            
            String[] ra = keyStr.split( "\\|" );
            
            String temp,hkm,uid,sseq,inpt,pts;
            
            int nodeSeq;
            int subnodeSeq;
            List<String> matchLst;
            
            for( int i=0; i<ra.length-3; i+=4 )
            {
                temp = ra[i].trim();
                
                if( temp==null || temp.trim().isEmpty() )
                    continue;
                
                temp = temp.trim();
                
                // Hot Key Value
                if( temp.toLowerCase().startsWith("hkm;") )
                {
                    hkm = temp;
                    uid = ra[i+1].trim();
                    sseq = ra[i+2].trim();                    
                    pts = ra[i+3].trim();
                    
                    nodeSeq=getNodeSeqForUniqueId( uid );
                    
                    if( nodeSeq <= 0 || sseq.isEmpty() )
                        subnodeSeq=0;
                    else
                        subnodeSeq = Integer.parseInt( sseq );
                    
                    // LogService.logIt("IntnClkStrmItem.parseResponseStr() PARSE HOT KEY TARGET Intn " + intnSeq + "-" + intnSnSeq + ", HKM " + hkm + ", " + uid + "-" + sseq );
                    hotKeyScoreValues.add( new Object[]{nodeSeq, subnodeSeq, hkm, new Float(pts)} );                    
                }
                
                // click value
                else
                {
                    uid = temp;
                    sseq = ra[i+1].trim();
                    inpt = ra[i+2].trim();                    
                    pts = ra[i+3].trim();
                    
                    if( pts.isEmpty() )
                        pts = "0";

                    nodeSeq=getNodeSeqForUniqueId( uid );
                    
                    subnodeSeq = Integer.parseInt( sseq );
                    
                    matchLst = getMatchStrList( inpt );
                    
                    
                    if( matchLst.isEmpty() )
                        clickScoreValues.add( new Object[]{nodeSeq, subnodeSeq, null, new Float(pts)} );                                        
                    else
                    {
                        // LogService.logIt("IntnClkStrmItem.parseScoreStr() PARSE CLICK TARGET Intn " + intnSeq + "-" + intnSnSeq + ", uniqueId=" + uid + ", Intn.NodeSeq=" + nodeSeq + "-" + subnodeSeq + ", matchLst.size()=" + matchLst.size() );
                        for( String match : matchLst )
                        {
                            // LogService.logIt("IntnClkStrmItem.parseScoreStr() PARSE CLICK TARGET Intn " + intnSeq + "-" + intnSnSeq + ", uniqueId=" + uid + ", Intn.NodeSeq=" + nodeSeq + "-" + subnodeSeq + ", match=" + match );
                            clickScoreValues.add( new Object[]{nodeSeq, subnodeSeq, match, new Float(pts)} );                                                                    
                            // LogService.logIt("IntnClkStrmItem.parseScoreStr() match=" + match );
                        }
                    }
                }                                
            } 
            
            // LogService.logIt("IntnClkStrmItem.parseScoreStr() Intn" + intnSeq + "-" + intnSnSeq + ", parsed " + keyStr + " to " + clickScoreValues.size() + " CLICK TARGETS and " + hotKeyScoreValues.size() + " HOT KEY TARGETS." );
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "IntnClkStrmItem.parseScoreStr() Intn.NodeSeq=" + intnSeq + "-" + intnSnSeq + ", " + keyStr );
        }
    }

    
    private List<String> getMatchStrList( String inStr )
    {
        List<String> out = new ArrayList<>();
        
        if( inStr==null || inStr.trim().isEmpty() )
            return out;
        
        for( String m : inStr.split( "~" ) )
        {
            m=m.trim();
            if( !m.isEmpty() )
                out.add(m);
        }
        
        return out;
    }
    
        
    private int getNodeSeqForUniqueId( String uniqueId )
    {
        if( simJ==null || uniqueId==null || uniqueId.trim().isEmpty() )
            return 0;
        
        for( SimJ.Intn ii : simJ.getIntn() )
        {
            if( ii.getUniqueid()!=null && !ii.getUniqueid().isEmpty() && ii.getUniqueid().equals(uniqueId) )
                return ii.getSeq();
        }

        return 0;
    }
    
    
    public float getPoints()
    {
        return this.points;
    }
    
    
    public void calculate()
    {
        // LogService.logIt( "IntnClkStrmItem.calculate() AAA.1 Intn " + intnSeq + "-" + intnSnSeq + ", respNdSeq=" + respNdSeq + "-" + respSnSeq + ", clickScoreValues.size=" + clickScoreValues.size() + ", hasValidScore=" + hasValidScore );

        if( hasValidScore )
            return;
        
        points = 0;
        String tgtTxtToMtch;
        
        if( responseHotKeyVal != null && !responseHotKeyVal.isEmpty() )
        {
            for( Object[] dat : this.hotKeyScoreValues )
            {
                if( ((String)dat[2]).toLowerCase().equalsIgnoreCase( responseHotKeyVal ) )
                {
                    selNdSeq = ((Integer)dat[0]).intValue();
                    selSnSeq = ((Integer)dat[1]).intValue();
                    points = ((Float)dat[3]).floatValue();
                    // LogService.logIt( "IntnClkStrmItem.calculate() AAA.2 Intn " + intnSeq + "-" + intnSnSeq + ", Have HOT KEY MATCH for " +  responseHotKeyVal + ", assigning points=" + points + ", sel Seqs=" + selNdSeq + "-" + selSnSeq );
                    hasValidScore = true;
                    return;
                }
            }
        }

        // LogService.logIt( "IntnClkStrmItem.calculate() AAAA Intn " + intnSeq + "-" + intnSnSeq + ", ResponseNd=" + respNdSeq + "-" + respSnSeq );

        
        // RespNdSeq indicates that there was a click (not a hot key)
        if( respNdSeq>0 && respSnSeq>0 )
        {
            boolean match = false;
            Map<Integer,String> respTxtMap;
            // String respTxtForSnSeq;
            int tgtSnSeq;
            
            for( Object[] dat : clickScoreValues )
            {
                // LogService.logIt("IntnClkStrmItem.calculate() 000.0 Intn " + intnSeq + "-" + intnSnSeq + ", Click Node NodeSeq=" + (Integer)dat[0] + "-" +  (Integer)dat[1] + ", text=" + (String) dat[2] );
                
                // If the click was not from the target node, skip it. 
                if( (Integer)dat[0]!=respNdSeq )
                    continue;
                
                tgtSnSeq = (Integer)dat[1];
                tgtTxtToMtch = (String) dat[2];

                // LogService.logIt("IntnClkStrmItem.calculate() 000.1 tgtSnSeq=" + tgtSnSeq + ", respSnSeq=" + respSnSeq + ", match=" + (tgtSnSeq==respSnSeq) + ", tgtTxtToMtch=" + tgtTxtToMtch );
                
                // if RESPONSE intn item matches TARGET
                if( tgtSnSeq==respSnSeq )
                {                    
                    // LogService.logIt( "IntnClkStrmItem.calculate() BBB.0 Intn " + intnSeq + "-" + intnSnSeq + ", tgtTxtToMtch=" + tgtTxtToMtch + ", respTxt=" + respTxt );
                    
                    // No target text to match, we have a hit! 
                    if( tgtTxtToMtch==null || tgtTxtToMtch.trim().isEmpty() )
                    {
                        // LogService.logIt("IntnClkStrmItem.calculate() BBB.1 Intn " + intnSeq + "-" + intnSnSeq + ", Have NON_INPUT CLICK MATCH AA for responseNdSeq=" + respNdSeq + "-" + respSnSeq + ", inputStr=" + respTxt );                    
                        match=true;
                    }
                    
                    // has target text to match, so if we have some response text, check it. 
                    else if( respTxt!=null && !respTxt.isEmpty() ) 
                    {
                        respTxtMap = getTextInputForSnSeq( 0 , respTxt );

                        for( String respTxtForSnSeq : respTxtMap.values() )
                        {
                            // LogService.logIt("IntnClkStrmItem.calculate() BBB.2 Intn " + intnSeq + "-" + intnSnSeq + ", checking score text=" + respTxtForSnSeq + ", input=" + respTxt );                    
                            if( StringUtils.isLooseMatch( tgtTxtToMtch, respTxtForSnSeq ) )
                            {
                                match=true;
                                // LogService.logIt("IntnClkStrmItem.calculate() BBB.3 Intn " + intnSeq + "-" + intnSnSeq + ", Have INPUT CLICK MATCH AA for responseNdSeq=" + respNdSeq + "-" + respSnSeq + ", SCORE VALUE=" + tgtTxtToMtch + " and RESPONSE VALUE=" + respTxtForSnSeq +  ", input=" + respTxt );                    
                            }
                            
                        }
                        
                       //  LogService.logIt("IntnClkStrmItem.calculate() BBB.1a Intn " + intnSeq + "-" + intnSnSeq + ", COMPARING SCORE VALUE=" + tgtTxtToMtch + " and RESPONSE VALUE=" + respTxtForSnSeq );                    
                        //else
                        //    LogService.logIt("IntnClkStrmItem.calculate() BBB.3 Intn " + intnSeq + "-" + intnSnSeq + ", INPUT CLICK MISMATCH SCORE VALUE " + tgtTxtToMtch + " and RESPONSE VALUE=" + respTxtForSnSeq );                    
                        
                    }
                    
                    if( match )
                    {
                        points = ((Float)dat[3]).floatValue();
                        selNdSeq = respNdSeq;
                        selSnSeq = respSnSeq;
                        // LogService.logIt("IntnClkStrmItem.calculate() BBB.4 Intn " + intnSeq + "-" + intnSnSeq + ", ASSIGNING POINTS=" + points + ", sel Seqs=" + selNdSeq + "-" + selSnSeq + ", input=" + respTxt );                    
                        hasValidScore = true;
                        return;
                    }
                }

                // At this point, the target intn item was not clicked, but we may have text input from it. 
                
                // LogService.logIt("IntnClkStrmItem.calculate() CCC.11 Intn " + intnSeq + "-" + intnSnSeq + ", tgtTxtToMtch=" + tgtTxtToMtch + ", respTxt=" + respTxt );
                
                // If there is target text
                if( tgtTxtToMtch!=null && !tgtTxtToMtch.isEmpty() && respTxt!=null && !respTxt.isEmpty() )
                {
                    respTxtMap = getTextInputForSnSeq( tgtSnSeq , respTxt );

                    //LogService.logIt("IntnClkStrmItem.calculate() CCC Intn " + intnSeq + "-" + intnSnSeq + ", Testing TARGET TEXT=" + tgtTxtToMtch + ", RESPONSE TEXT FOR SN SEQ=" + respTxtForSnSeq );
                    // LogService.logIt("IntnClkStrmItem.calculate() CCC.1a Intn " + intnSeq + "-" + intnSnSeq + ", COMPARING SCORE VALUE=" + tgtTxtToMtch + " and RESPONSE VALUE=" + respTxtForSnSeq + ", " + StringUtils.isLooseMatch( tgtTxtToMtch, respTxtForSnSeq ) );                    
                    
                    for( String respTxtForSnSeq : respTxtMap.values() )
                    {
                        if( StringUtils.isLooseMatch( tgtTxtToMtch, respTxtForSnSeq ) )
                        {
                            match=true;
                            // LogService.logIt("IntnClkStrmItem.calculate() CCC.1 Intn " + intnSeq + "-" + intnSnSeq + ", Have INPUT CLICK MATCH AA for responseNdSeq=" + respNdSeq + "-" + respSnSeq + ", TARGET VALUE=" + tgtTxtToMtch + " and RESPONSE VALUE=" + respTxtForSnSeq );                    
                        } 
                    }
                    //if( StringUtils.isLooseMatch( tgtTxtToMtch, respTxtForSnSeq ) )
                    //{
                    //    match=true;
                    //    LogService.logIt("IntnClkStrmItem.calculate() CCC.1 Intn " + intnSeq + "-" + intnSnSeq + ", Have INPUT CLICK MATCH AA for responseNdSeq=" + respNdSeq + "-" + respSnSeq + ", TARGET VALUE=" + tgtTxtToMtch + " and RESPONSE VALUE=" + respTxtForSnSeq );                    
                    //} 
                    //else
                    //    LogService.logIt("IntnClkStrmItem.calculate() CCC.2 Intn " + intnSeq + "-" + intnSnSeq + ", INPUT CLICK MISMATCH TARGET VALUE " + tgtTxtToMtch + " and RESPONSE VALUE=" + respTxtForSnSeq );                    
                    
                    if( match )
                    {
                        points = ((Float)dat[3]).floatValue();
                        selNdSeq = respNdSeq;
                        selSnSeq = tgtSnSeq;
                        // LogService.logIt("IntnClkStrmItem.calculate() CCC.3 Intn. " + intnSeq + "-" + intnSnSeq + ", ASSIGNING POINTS=" + points + ", sel Seqs=" + selNdSeq + "-" + selSnSeq );                    
                        hasValidScore = true;
                        return;
                    }
                }
            }
        }
                
        hasValidScore = true;

        // LogService.logIt( "IntnClkStrmItem.calculate() END Intn" + intnSeq + "-" + intnSnSeq + ", Finish hasValidScore=" + hasValidScore + ", final points=" + points );
    }

    
    /**
     * Returns Map<Integer,String>
     *        Integer = seq of frm input intn item
     *        String = usr input for form element
     * 
     * @param intSnSeq
     * @param inStr
     * @return 
     */
    public Map<Integer,String> getTextInputForSnSeq( int intSnSeq , String inStr )
    {
        Map<Integer,String> out = new HashMap<>();
        
        if( inStr==null || inStr.isEmpty() )
            return out;
        
        String seqStr;
        String valStr;
        // int frmIntnItmSeq;
        //String frmIntnItemTxt;
        //String[] frmVals;
        
        String[] vals = inStr.split( "~" );
        
        for( int i=0;i<vals.length-1; i+=2  )
        {
            seqStr = vals[i].trim();
            
            if( seqStr==null || seqStr.isEmpty() )
                continue;
            
            if( intSnSeq>0 && intSnSeq != Integer.parseInt( seqStr ))
                continue;
            
            // found! Yay!
            valStr = vals[i+1].trim(); 
            valStr = UrlEncodingUtils.decodeKeepPlus( valStr ); // resp value is url-encoded
            valStr = StringUtils.replaceStr( valStr, "@#@" , "~" );
            valStr = StringUtils.replaceStr( valStr, "&#&" , "^" );
            valStr = StringUtils.replaceStr( valStr, "$#$" , "|" );

            out.put( intSnSeq, valStr );
            
            //if( valStr.indexOf("~")>0 )
            //{
            //    frmVals = valStr.split("~");
                
                //if( frmVals.length>=2 )
                //{
                //    frmIntnItmSeq = Integer.parseInt( frmVals[0] );
                //    frmIntnItemTxt = frmVals[1];
                    
                //    out.put( frmIntnItmSeq, frmIntnItemTxt );
                //}
                
            //}
            
            //else
            //{
            //    out.put( new Integer(0), valStr );
            //}
            
            return out;
        }
        
        // not found.
        return out;
    }
    

    public boolean getHasValidScore() {
        return hasValidScore;
    }

        
}


