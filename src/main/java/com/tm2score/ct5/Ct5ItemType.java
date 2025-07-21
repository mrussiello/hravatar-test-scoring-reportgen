/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ct5;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2score.entity.ct5.event.Ct5ItemResponse;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.XmlUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author miker_000
 */
public enum Ct5ItemType {

    MULT_CHOICE(0,"c5it.MultipleChoice", "multchoice" ),
    MULT_CORRECT_ANSWER(1,"c5it.MultipleCorrect", "multcorect"),
    FILL_BLANK(2,"c5it.FillBlank", "fillblank" ),
    ESSAY(3,"c5it.Essay", "essay"),
    RATING(4,"c5it.Rating", "rating"),
    MATCHING(5,"c5it.Matching", "matching"),
    FILE_UPLOAD(6,"c5it.FileUpload", "fileupload" ),
    SCORED_MEDIA(10,"c5it.ScoredMedia", "scoredmedia" ),
    RESUME(11,"c5it.Resume", "resume" ),
    VIDEO_INTERVIEW(50,"c5it.VideoInterview", "videointerview" ),
    INFORMATIONAL(100,"c5it.Informational", "info");


    private final int ct5ItemTypeId;

    private final String key;

    private final String renderPageStub;


    private Ct5ItemType( int s , String n, String w )
    {
        this.ct5ItemTypeId = s;

        this.key = n;
        this.renderPageStub = w;
    }


    //public boolean getUsesInlineIntnItems()
    //{
    //    return getIsInfo() || getIsAnyEssay() || getIsResume() || getIsAnyMultipleChoice() || getIsAnyMultipleCorrect()|| getIsFillBlank();
    //}

    //public boolean getIsSurvey()
    //{
    //    return equals(SURVEY_MULT_CHOICE) || equals(SURVEY_MULT_CORRECT_ANSWER) || equals(SURVEY_ESSAY) || equals(SURVEY_RATING);
    //}

    public boolean getIsScoredMedia()
    {
        return equals(SCORED_MEDIA);
    }

    public boolean getIsFileUpload()
    {
        return equals(FILE_UPLOAD);
    }

    public boolean getIsResume()
    {
        return equals(RESUME);
    }


    public boolean getUsesChoices()
    {
        return getIsMultipleChoice() || getIsMultipleCorrect() || getIsMatching(); //  || equals(SURVEY_MULT_CHOICE) || equals(SURVEY_MULT_CORRECT_ANSWER);
    }

    public boolean getUsesQuestion()
    {
        return !equals( INFORMATIONAL );
    }

    //public boolean getIsKsa()
    //{
    //    return equals(FILL_BLANK) || equals(ESSAY) || getIsMultipleChoice() || getIsMultipleCorrect() || getIsMatching(); //  || getIsSurvey();
    //}

    public boolean getIsMatching()
    {
        return equals(MATCHING);
    }

    public boolean getIsAnyRating()
    {
        return equals( RATING ); //  ||  equals( SURVEY_RATING );
    }



    //public boolean getSupportsBranching()
    //{
    //    return getIsInfo() || getIsAnyMultipleChoice() || getIsAnyMultipleCorrect() || getIsRating() || getIsEssay() || getIsResume() || getIsFillBlank(); // getIsSurvey() ||
    //}

    public boolean getIsAnyMultipleChoice()
    {
        return equals( MULT_CHOICE ); //  || equals( SURVEY_MULT_CHOICE );
    }


    public boolean getIsMultipleChoice()
    {
        return equals( MULT_CHOICE );
    }

    public boolean getIsMultipleCorrect()
    {
        return equals( MULT_CORRECT_ANSWER );
    }

    public boolean getIsAnyMultipleCorrect()
    {
        return equals( MULT_CORRECT_ANSWER ); //  || equals( SURVEY_MULT_CORRECT_ANSWER );
    }

    public boolean getIsFillBlank()
    {
        return equals( FILL_BLANK );
    }

    public boolean getIsEssay()
    {
        return equals( ESSAY );
    }

    public boolean getIsAnyEssay()
    {
        return equals( ESSAY ); //  || equals( SURVEY_ESSAY );
    }

    public boolean getIsInfo()
    {
        return equals( INFORMATIONAL );
    }

    public boolean getIsVideo()
    {
        return equals( VIDEO_INTERVIEW );
    }
    public boolean getIsVideoOrScoredMedia()
    {
        return equals( VIDEO_INTERVIEW ) || equals( SCORED_MEDIA );
    }

    public boolean getIsVideoOrScoredMediaOrGenFileUpload()
    {
        return equals( VIDEO_INTERVIEW ) || equals( SCORED_MEDIA ) || equals( FILE_UPLOAD );
    }


    public boolean getIsRating()
    {
        return equals(RATING);
    }


    public boolean getIsInfoRequired()
    {
        return equals( INFORMATIONAL );
    }

    //public boolean getUsesTopic()
    //{
    //    return !getIsVideo() && !getIsInfo() && !getIsResume();
    //}

    //public boolean getUsesSubtopic()
    //{
    //    return  getUsesTopic() && !getIsEssay(); //  && !getIsSurvey();
    //}


    //public boolean okForCt5TestType( Ct5TestType ct5TestType )
    //{
    //    if( ct5TestType.getIsDefault() && getIsVideo() )
    //        return false;

    //    return true;
    //}



    /*
     For Format is a string of the raw responses. Always a pair of itempartid,value (string)
        rating:   question:value
        mult choice:  sel choice:true
        mult corr answer: choice:true,choice:false,choice:false, ...
        matching dragable:tgt,dragable:tgt,dragable:tgt
        drag drop: tgt:dragable1;dragable2;dragable3,tgt:dragable4, ...
        fill blank: question:URL-Encoded(text entered)
        essay: blank
    */
    public int getSelectedSubnodeSeq( Ct5ItemResponse ir, SimJ.Intn sji )
    {
        if( ir==null || ir.getResponseStr1()==null || ir.getResponseStr1().isBlank() || !ir.getIsComplete() )
            return 0;


        if( getIsMultipleChoice() )
        {
            Map<Integer,String> respMap = getResponseMap(ir,sji);
            List<Integer> ks = new ArrayList<>();
            ks.addAll(respMap.keySet() );
            if( !ks.isEmpty() )
                return ks.get(0);
        }

        SimJ.Intn.Intnitem sjii = getSubmitButtonIntnItem( sji );

        if( sjii!=null )
            return sjii.getCt5Itempartid();

        return 0;
    }




    /*
     responseStr1:
     For Format is a string of the raw responses. Always a pair of itempartid,value (string)
        rating:   question:value
        mult choice:  sel choice:true
        mult corr checkbox answer: choice:true,choice:false,choice:false, ...
        mult corr combo or fillblank answer: choice:URL-Encoded(text),choice:URL-Encoded(text),choice:URL-Encoded(text), ...
        matching dragable:tgt,dragable:tgt,dragable:tgt
        drag drop: tgt:dragable1;dragable2;dragable3,tgt:dragable4, ...
        fill blank: question:URL-Encoded(text entered)
        essay: blank
        resume: blank
    */
    public String getResultXmlIntnValue(Ct5ItemResponse ir, SimJ.Intn sji )
    {

        // LogService.logIt( "Ct5ItemType.getResultXmlIntnValue() START " + sji.getUniqueid() + ", " + ir.toString() + ", isResume=" + getIsResume());

        // no response?
        if( ir==null || !ir.getIsComplete() )
            return "";


        String DELIM = "~";
        SimJ.Intn.Intnitem sjii = null;

        // Essay. Stored in responseText.
        if( getIsEssay() )
        {
            sjii = getFirstSimJIntnItem( sji, G2ChoiceFormatType.TEXT_BOX , Ct5ItemPartType.WIDGET );
            String rt = ir.getResponseText();
            if( rt==null )
                return "";
            rt = StringUtils.replaceStr(rt, DELIM, "@#@" );
            rt = StringUtils.replaceStr(rt, "|", "&#&" );
            rt = StringUtils.replaceStr(rt, "^", "&#&" );
            return (sjii==null ? "" : sjii.getCt5Itempartid()) + DELIM + XmlUtils.encodeURIComponent(rt);
            // return (sjii==null ? "" : sjii.getCt5Itempartid()) + DELIM + StringUtils.getUrlEncodedValue(rt);
        }

        String commentStr = null;
        if( sji.getCommentboxid()>0 )
        {
            sjii = this.getOptionalCommentIntnItem(sji);
            String rt = ir.getResponseText();
            if( rt!=null && !rt.isBlank() )
            {
                rt = StringUtils.replaceStr(rt, DELIM, "@#@" );
                rt = StringUtils.replaceStr(rt, "|", "&#&" );
                rt = StringUtils.replaceStr(rt, "^", "&#&" );
                commentStr = (sjii==null ? "" : sjii.getCt5Itempartid()) + DELIM + XmlUtils.encodeURIComponent(rt);
                // commentStr = (sjii==null ? "" : sjii.getCt5Itempartid()) + DELIM + StringUtils.getUrlEncodedValue(rt);
            }
        }

        // not used for these items.
        if( (getIsAnyMultipleChoice() && sji.getMultiplechoiceformat()!=Ct5MultipleChoiceFormatType.COMBO.getCt5MultipleChoiceFormatTypeId()) || getIsInfo() )
            return commentStr==null ? "" : commentStr;

        if( !getIsVideoOrScoredMediaOrGenFileUpload() && !getIsResume() && (ir.getResponseStr1()==null || ir.getResponseStr1().isBlank()) )
            return commentStr==null ? "" : commentStr;

        // will only include the non-hidden targets if matching.
        Map<Integer,String> respMap = getResponseMap(ir,sji);

        if( getIsFillBlank() )
        {
            sjii = getQuestionSimJIntnItem( sji );
            if( sjii!=null  )
            {
                String rt = respMap.get(sjii.getCt5Itempartid());
                // LogService.logIt( "Ct5ItemType.respMap.get(" + sjii.getCt5Itempartid() + ") is " + rt );
                if( rt==null )
                    rt="";
                rt = StringUtils.replaceStr(rt, "+", "%20" );
                SimJ.Intn.Intnitem sjii2 = getFirstSimJIntnItem( sji, G2ChoiceFormatType.TEXT_BOX, Ct5ItemPartType.WIDGET );
                return (sjii2==null ? "0" : sjii2.getCt5Itempartid()) + DELIM + rt;    // aleady encoded.
            }
            return "";
        }

        if( getIsAnyRating() )
        {
            sjii = getQuestionSimJIntnItem( sji );
            if( sjii!=null  )
            {
                String rt = respMap.get(sjii.getCt5Itempartid());
                // LogService.logIt( "Ct5ItemType.respMap.get(" + sjii.getCt5Itempartid() + ") is " + rt );
                if( rt==null )
                    rt="";
                SimJ.Intn.Intnitem sjii2 = getFirstSimJIntnItem( sji, G2ChoiceFormatType.SLIDER_THUMB, Ct5ItemPartType.WIDGET );
                return (sjii2==null ? "0" : sjii2.getCt5Itempartid()) + DELIM + rt + (commentStr==null ? "" : DELIM + commentStr ); // float value
            }
            return commentStr==null ? "" : commentStr;
        }

        StringBuilder sb = new StringBuilder();

        // mult choice combo needs selected values in the value field.
        if( getIsAnyMultipleChoice() && sji.getMultiplechoiceformat()==Ct5MultipleChoiceFormatType.COMBO.getCt5MultipleChoiceFormatTypeId() )
        {
            String val;
            SimJ.Intn.Intnitem sjii2 = getFirstSimJIntnItem( sji, G2ChoiceFormatType.COMBO, Ct5ItemPartType.WIDGET );
            for( Integer ipid : respMap.keySet() )
            {
                val = respMap.get(ipid);
                if( val!=null && val.trim().equals("true") )
                {
                    sb.append( sjii2.getCt5Itempartid() + DELIM + ipid.toString() );

                    // Only one selected choice is currently supported.
                    break;
                }
            }
            if( commentStr!=null && !commentStr.isBlank() )
            {
                if( sb.length()>0 )
                    sb.append( DELIM );
                sb.append(commentStr);
            }
            return sb.toString();


        }

        if( getIsAnyMultipleCorrect() )
        {
            // Ct5MultipleChoiceFormatType mcft = Ct5MultipleChoiceFormatType.getValue( sji.getMultiplechoiceformat() );

            String val;
            for( Integer ipid : respMap.keySet() )
            {
                val = respMap.get(ipid);
                if( sb.length()>0 )
                    sb.append( DELIM );
                if( val==null )
                    val="";
                val=val.trim();

                val = StringUtils.replaceStr(val, "+", "%20" );
                sb.append( ipid.toString() + DELIM + val );
            }
        }

        // tgt~draggable1,draggable2~tgt2~draggable~tgt~draggable,draggable,draggable ...
        if( getIsMatching() )
        {
            // key-dragtarget.ct5itempartid, list=draggable ct5itempartids
            Map<Integer,List<Integer>> mp = new HashMap<>();
            String val;
            Integer tgt;
            List<Integer> dl;

            // will only include the non-hidden targets if matching.
            // format is draggable.ct5ItemPartId:target.ct5itempartid

            // for each draggable
            for( Integer dbl : respMap.keySet() )
            {
                if( dbl<=0 )
                    continue;
                // val is tgt for this dbl
                val = respMap.get(dbl);
                tgt = val==null || val.isBlank() ? 0 : Integer.parseInt(val);
                if( tgt<=0 )
                    continue;
                dl = mp.get(tgt);
                if( dl==null )
                {
                    dl=new ArrayList<>();
                    mp.put(tgt, dl);
                }
                dl.add(dbl);
            }

            // for each target
            for( Integer tgt2 : mp.keySet() )
            {
                dl = mp.get(tgt2);
                if( dl.isEmpty() )
                    continue;

                if( sb.length()>0 )
                    sb.append( DELIM );

                sb.append( tgt2 + DELIM );
                for( int i=0;i<dl.size();i++ )
                {
                    if( i>0 )
                        sb.append(",");
                    sb.append(dl.get(i) );
                }
            }
        }

        // File upload.
        // 6~medcap_81-6%3B909283%3Bvideo%2Fwebm~7~false
        // 6~medcap_81-6;909283;video/webm~7~false     DECODED
        // video   itempartid~ct5itemid-ct5itempartid;initialfilesize;initialmime

        // LogService.logIt( "Ct5ItemType.getResultXmlIntnValue()=" + getIsVideoOrScoredMedia() + ", ir.getUploadedUserFile()=" + (ir.getUploadedUserFile()==null ? "null" : "not null") );

        if( getIsVideoOrScoredMediaOrGenFileUpload() && ir.getUploadedUserFile()!=null )
        {
            sjii = getFirstSimJIntnItem( sji, G2ChoiceFormatType.MEDIA_CAPTURE, Ct5ItemPartType.WIDGET );

            if( sjii==null && getIsFileUpload() )
                sjii = getFirstSimJIntnItem( sji, G2ChoiceFormatType.FILEUPLOADBTN, Ct5ItemPartType.WIDGET );

            String encVals = "medcap_" + sji.getCt5Itemid() + "-" + (sjii==null ? "0" : sjii.getCt5Itempartid()) + ";" + ir.getUploadedUserFile().getInitialFileSize() + ";" + ir.getUploadedUserFile().getInitialMime() + ";" + ir.getUploadedUserFile().getUploadedUserFileId();
            encVals = StringUtils.getUrlEncodedValue(encVals);

            sb.append( (sjii==null ? "0" : sjii.getCt5Itempartid()) + DELIM + encVals );
        }

        if( getIsResume() )
        {
            // LogService.logIt( "Ct5ItemType.getResultXmlIntnValue() START for RESUME " + sji.getUniqueid() + ", " + ir.toString() + ", ir.getUploadedUserFileId=" + (ir.getUploadedUserFile()==null ? "null" : ir.getUploadedUserFile().getUploadedUserFileId()) );
            
            String res = "";

            if( ir.getUploadedUserFile()!=null )
            {
                sjii = getFirstSimJIntnItem( sji, G2ChoiceFormatType.FILEUPLOADBTN, Ct5ItemPartType.WIDGET );

                String encVals = "medcap_" + sji.getCt5Itemid() + "-" + (sjii==null ? "0" : sjii.getCt5Itempartid()) + ";" + ir.getUploadedUserFile().getInitialFileSize() + ";" + ir.getUploadedUserFile().getInitialMime();
                encVals = StringUtils.getUrlEncodedValue(encVals);
                res = (sjii==null ? "0" : sjii.getCt5Itempartid()) + DELIM + encVals;
            }

            sjii = getFirstSimJIntnItem( sji, G2ChoiceFormatType.TEXT_BOX , Ct5ItemPartType.WIDGET );
            String rt = ir.getResponseText();
            if( rt!=null && !rt.isBlank() )
            {
                rt = StringUtils.replaceStr(rt, DELIM, "@#@" );
                rt = StringUtils.replaceStr(rt, "|", "&#&" );
                rt = StringUtils.replaceStr(rt, "^", "&#&" );

                if( !res.isBlank() )
                    res += DELIM;

                res += (sjii==null ? "" : sjii.getCt5Itempartid()) + DELIM + XmlUtils.encodeURIComponent(rt);
            }
            sb.append( res );
        }

        return sb.toString() + (commentStr==null ? "" : DELIM + commentStr );
    }



    /*
     For Format is a string of the raw responses. Always a pair of itempartid,value (string)
        rating:   question:value
        mult choice:  sel choice:true
        mult corr checkbox answer: choice:true,choice:false,choice:false, ...
        mult corr combo or fillblank answer: choice:URL-Encoded(text),choice:URL-Encoded(text),choice:URL-Encoded(text), ...
        matching dragable:tgt,dragable:tgt,dragable:tgt
        drag drop: tgt:dragable1;dragable2;dragable3,tgt:dragable4, ...
        fill blank: question:URL-Encoded(text entered)
        essay: blank
        resume: blank
    */
    public Map<Integer,String> getResponseMap( Ct5ItemResponse ir, SimJ.Intn sji )
    {
        Map<Integer,String> respMap = new HashMap<>();

        if( ir==null || ir.getResponseStr1()==null || ir.getResponseStr1().isBlank() || !ir.getIsComplete())
            return respMap;

        int ct5ItemPartId;
        String[] vals;
        for( String pair : ir.getResponseStr1().split(",") )
        {
            pair=pair.trim();
            if(!pair.contains(":"))
            {
                LogService.logIt( "Ct5ItemType.getSelectedSubnodeSeq() skipping pair because missing colon. " + pair + ", full String=" + ir.getResponseStr1() + ", ct5ItemId=" + ir.getCt5ItemId() + ", ct5ItemResponseId=" + ir.getCt5ItemResponseId() );
                continue;
            }
            vals = pair.split(":");
            if( vals.length<2 || vals[1]==null || vals[1].isBlank() )
            {
                // LogService.logIt( "Ct5ItemType.getSelectedSubnodeSeq() skipping pair there is no colon or no value after colon. " + pair + ", full String=" + ir.getResponseStr1() + ", ct5ItemId=" + ir.getCt5ItemId() + ", ct5ItemResponseId=" + ir.getCt5ItemResponseId() );
                continue;
            }
            try
            {
                ct5ItemPartId=Integer.parseInt(vals[0]);
            }
            catch(NumberFormatException e)
            {
                LogService.logIt( "Ct5ItemType.getSelectedSubnodeSeq() skipping pair NumberFormatException: " + e.toString() + "," + pair + ", full String=" + ir.getResponseStr1() + ", ct5ItemId=" + ir.getCt5ItemId() + ", ct5ItemResponseId=" + ir.getCt5ItemResponseId() );
                continue;
            }
            respMap.put(ct5ItemPartId, vals[1]);
        }

        // dumpRespMap( ir.getCt5ItemId(), respMap );

        return respMap;
    }

    protected void dumpRespMap( int ct5ItemId, Map<Integer,String> respMap )
    {
        StringBuilder sb = new StringBuilder();
        for( Integer s : respMap.keySet() )
        {
            if( !sb.isEmpty() )
                sb.append("\n");
            sb.append( s.toString() + "=" + respMap.get(s) );
        }
        LogService.logIt( "RespMap ct5ItemId=" + ct5ItemId + ":\n" + sb.toString() );
    }


    private SimJ.Intn.Intnitem  getQuestionSimJIntnItem( SimJ.Intn sji )
    {
        if( sji==null )
            return null;
        for( SimJ.Intn.Intnitem sjii : sji.getIntnitem() )
        {
            if( sjii.getIsquestionstem()==1 )
                return sjii;
        }
        for( SimJ.Intn.Intnitem sjii : sji.getIntnitem() )
        {
            if( sjii.getCt5Itemparttypeid()==Ct5ItemPartType.QUESTION.getCt5ItemPartTypeId() )
                return sjii;
        }

        return null;
    }

    private SimJ.Intn.Intnitem getSubmitButtonIntnItem( SimJ.Intn sji )
    {
        if( sji==null )
            return null;
        for( SimJ.Intn.Intnitem sjii : sji.getIntnitem() )
        {
            if( sjii.getCt5Itemparttypeid()==Ct5ItemPartType.SUBMIT.getCt5ItemPartTypeId() )
                return sjii;
        }
        return null;
    }

    private SimJ.Intn.Intnitem getOptionalCommentIntnItem( SimJ.Intn sji )
    {
        if( sji==null )
            return null;
        for( SimJ.Intn.Intnitem sjii : sji.getIntnitem() )
        {
            if( sjii.getCt5Itemparttypeid()==Ct5ItemPartType.OPTIONAL_COMMENT.getCt5ItemPartTypeId() )
                return sjii;
        }
        return null;
    }


    private SimJ.Intn.Intnitem getFirstSimJIntnItem( SimJ.Intn sji, G2ChoiceFormatType fmt, Ct5ItemPartType ct5ItemPartType )
    {
        if( sji==null || fmt==null )
            return null;
        for( SimJ.Intn.Intnitem sjii : sji.getIntnitem() )
        {
            if( sjii.getFormat()==fmt.getG2ChoiceFormatTypeId() && sjii.getCt5Itemparttypeid()==ct5ItemPartType.getCt5ItemPartTypeId() )
                return sjii;
        }
        return null;
    }


    public static Ct5ItemType getValue( int id )
    {
        Ct5ItemType[] vals = Ct5ItemType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCt5ItemTypeId() == id )
                return vals[i];
        }

        return MULT_CHOICE;
    }


    public String getName()
    {
        return key;
    }

    public int getCt5ItemTypeId()
    {
        return ct5ItemTypeId;
    }

}
