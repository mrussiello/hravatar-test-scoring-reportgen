/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.format;

import com.itextpdf.text.DocumentException;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.global.Constants;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.util.STStringTokenizer;
import com.tm2score.util.StringUtils;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Mike
 */
public class ScoreFormatUtils
{

    public static java.util.List<ScoreCategoryRange> getOverallScoreCatInfoList(boolean forEmail, ScoreColorSchemeType scoreColorSchemeType)
    {
        int wid = forEmail ? Constants.CT2_COLORGRAPHWID_EML : Constants.CT2_COLORGRAPHWID;

        java.util.List<ScoreCategoryRange> scrl = new ArrayList<>();

        ScoreCategoryRange scr;
        boolean seven = scoreColorSchemeType!=null && scoreColorSchemeType.getIsSevenColor();
        boolean three = scoreColorSchemeType!=null && scoreColorSchemeType.getIsThreeColor();
        
        if( seven )
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.BLACK.getScoreCategoryTypeId(), 0,15, wid );
            scrl.add( scr );
            scr = new ScoreCategoryRange(ScoreCategoryType.RED.getScoreCategoryTypeId(), 15,35, wid );
            scrl.add( scr );            
        }      
        else
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.RED.getScoreCategoryTypeId(), 0,20, wid );
            scrl.add( scr );
        }
        if( !three )
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.REDYELLOW.getScoreCategoryTypeId(), 20,35, wid );
            scrl.add( scr );
        }
        scr = new ScoreCategoryRange(ScoreCategoryType.YELLOW.getScoreCategoryTypeId(), 50,65, wid );
        scrl.add( scr );
        
        if( !three )
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.YELLOWGREEN.getScoreCategoryTypeId(), 65,80, wid );
            scrl.add( scr );
        }
        
        //scr = new ScoreCategoryRange(ScoreCategoryType.GREEN.getScoreCategoryTypeId(), 80,100, wid );
        //scrl.add( scr );

        if( seven )
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.GREEN.getScoreCategoryTypeId(), 80,90, wid );
            scrl.add( scr );            
            scr = new ScoreCategoryRange(ScoreCategoryType.WHITE.getScoreCategoryTypeId(), 90,100, wid );
            scrl.add( scr );
        }      
        else
        {
            scr = new ScoreCategoryRange(ScoreCategoryType.GREEN.getScoreCategoryTypeId(), 80,100, wid );
            scrl.add( scr );            
        }
        
        
        return scrl;

    }

    public static String getDescripFromTextParam( String t )
    {
        if( t==null )
            return "";

        String o = StringUtils.getBracketedArtifactFromString( t , Constants.DESCRIPTIONKEY );

        if( o != null && !o.isEmpty() )
            return o;

        // has a key but has something before the key, use that.
        if( t.indexOf( "[" ) > 0 )
            return t.substring( 0 , t.indexOf( "[" ) );

        // has a key at the start.
        if( t.indexOf( "[" ) == 0 )
            return "";
        
        // no key, use whatever is there.
        return t;
    }

    public static  java.util.List<TextAndTitle> getNonCompTextListTable( TestEvent te, NonCompetencyItemType ncit ) throws Exception
    {
        try
        {
            // LogService.logIt( "ScoreFormatUtils.getNonCompTextListTable() " + ncit.getName() );

            TestEventScore tes = te.getOverallTestEventScore();

            if( tes == null )
                throw new Exception( "No Overall TestEventScore found." );


            // LogService.logIt( "ScoreFormatUtils.getNonCompTextListTable() " + ncit.getName() + ", textaseResponses: " + (tes.getTextbasedResponses() == null ? "null" : tes.getTextbasedResponses() ) );


            if( tes.getTextbasedResponses() == null || tes.getTextbasedResponses().isEmpty() )
                return new ArrayList<>();

            return unpackTextBasedResponses( tes.getTextbasedResponses(), ncit.getTitle() );
        }

        catch( IOException | DocumentException e )
        {
            LogService.logIt( e, "ScoreFormatUtils.getNonCompTextListTable() " + ncit.getName() );

            throw e;
        }

    }

    public static  java.util.List<TextAndTitle> getTextTitleList( TestEvent te, String categoryTitle ) throws Exception
    {
        try
        {
            // LogService.logIt( "ScoreFormatUtils.getTextTitleList() " + categoryTitle );

            TestEventScore tes = te.getOverallTestEventScore();

            if( tes == null )
            {
                LogService.logIt( "ScoreFormatUtils.getTextTitleList() NONFATAL ERROR No Overall TestEventScore found in test event!!!, testEventId=" + te.getTestEventId() + ", testKeyId=" + te.getTestKeyId() );
                return new ArrayList<>();
            }


            // LogService.logIt( "ScoreFormatUtils.getTextTitleList() " + categoryTitle + ", textbasedResponses: " + (tes.getTextbasedResponses() == null ? "null" : tes.getTextbasedResponses() ) );


            if( tes.getTextbasedResponses() == null || tes.getTextbasedResponses().isEmpty() )
                return new ArrayList<>();

            return unpackTextBasedResponses( tes.getTextbasedResponses(), categoryTitle );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ScoreFormatUtils.getTextTitleList() " + categoryTitle );
            throw e;
        }

    }


    public static  java.util.List<String> getSingleStringBasedResponseList( TestEvent te, String categoryTitle ) throws Exception
    {
        try
        {
            // LogService.logIt( "ScoreFormatUtils.getTextTitleList() " + categoryTitle );

            TestEventScore tes = te.getOverallTestEventScore();

            if( tes == null )
                throw new Exception( "No Overall TestEventScore found." );


            // LogService.logIt( "ScoreFormatUtils.getTextTitleList() " + categoryTitle + ", textbasedResponses: " + (tes.getTextbasedResponses() == null ? "null" : tes.getTextbasedResponses() ) );


            if( tes.getTextbasedResponses() == null || tes.getTextbasedResponses().isEmpty() )
                return new ArrayList<>();

            return unpackSingleStringBasedResponses( tes.getTextbasedResponses(), categoryTitle );
        }

        catch( IOException | DocumentException e )
        {
            LogService.logIt( e, "ScoreFormatUtils.getSingleStringBasedResponseList() " + categoryTitle );

            throw e;
        }

    }



    public static java.util.List<String> unpackSingleStringBasedResponses( String inStr, String key )
    {
        java.util.List<String> out = new ArrayList<>();

        if( inStr == null || inStr.isEmpty() )
            return out;

        if( key != null )
        {

            String k = ";;;" + key+ ";;;" + Constants.DELIMITER;

            int ki = inStr.indexOf(k);

            if( ki < 0 )
                return out;

            int kie = inStr.indexOf( ";;;" , ki + k.length() );

            inStr = inStr.substring( ki + k.length(), kie < 0 ? inStr.length() : kie );
        }

        if( inStr.isEmpty() )
            return out;

        STStringTokenizer st = new STStringTokenizer( inStr, Constants.DELIMITER );

        while( st.hasMoreTokens() )
        {
            out.add( st.nextToken() );
        }

        return out;
    }


    public static java.util.List<TextAndTitle> unpackTextBasedResponses( String inStr )
    {
        return unpackTextBasedResponses( inStr, null );
    }

    public static java.util.List<TextAndTitle> unpackTextBasedResponses( String inStr, String key )
    {
        java.util.List<TextAndTitle> out = new ArrayList<>();

        if( inStr == null || inStr.isEmpty() )
            return out;

        if( key != null )
        {

            String k = ";;;" + key+ ";;;" + Constants.DELIMITER;

            int ki = inStr.indexOf(k);

            if( ki < 0 )
                return out;

            int kie = inStr.indexOf( ";;;" , ki + k.length() );

            inStr = inStr.substring( ki + k.length(), kie < 0 ? inStr.length() : kie );
        }

        if( inStr.isEmpty() )
            return out;

        TextAndTitle tt;

        STStringTokenizer st = new STStringTokenizer( inStr, Constants.DELIMITER );

        String ttl,text,flgs,string1,string2,string3,string4;
        
        // Have to add back the original delim. which was removed above.
        boolean hasString1 = ( st.countDelims() + 1) % 4 == 0;
                
        /// boolean hasString1 = st.countDelims()%4 == 0;

        // LogService.logIt( "ScoreFormatUtils.unpackTextBasedResponses() key=" + key + ", inStr=" +  inStr + " Tokens=" + st.countTokens() + ", hasString1=" + hasString1 + ", delims=" + st.countDelims() );
        
        while( st.hasMoreTokens() )
        {
            ttl = st.nextToken();

            if( !st.hasMoreTokens() )
                text = "";
            else
                text = st.nextToken();

            if( !st.hasMoreTokens() )
                flgs= "";
            else
                flgs = st.nextToken();

            string1 = hasString1 && st.hasMoreTokens() ? st.nextToken() : null;
            string2 = null;
            string3 = null;
            string4=null;
            if( string1!=null && !string1.isEmpty() )
            {
                try
                {
                    string1 = StringUtils.getUrlDecodedValue(string1);

                    if( string1.contains("~") )
                    {
                        String[] ds = string1.split("~");
                        
                        // Note, if nothing there then there will be no length to array.
                        string1 = ds.length>0 ? ds[0] : "";
                        if( ds.length>1 )
                            string2 = ds[1];
                        if( ds.length>2 )
                            string3 = ds[2];
                        if( ds.length>3 )
                            string4 = ds[3];
                    }
                    //LogService.logIt( "TestResultUtils.getTextBasedResponseList() parsed string1=" + string1 );
                }
                catch( Exception e )
                {
                    LogService.logIt( e,"ScoreFormatUtils.unpackTextBasedResponses() URLDecoding DDD string1 error. NON-FATAL  error=" + e.toString() + ", decoding: string1=" + string1 + ", inStr=" + inStr );
                } 
            }            

            
            tt = new TextAndTitle( text, ttl, false, 0, 0, string1, string2, string3, string4 );
            // tt = new TextAndTitle( XMLUtils.decodeURIComponentNoErrors(text),XMLUtils.decodeURIComponentNoErrors( ttl ), false, 0, string1 );

            tt.setFlags(flgs);

            out.add( tt );
            
            // LogService.logIt( "TestResultUtils.getTextBasedResponseList() adding " + tt.toString() );
        }

        return out;
    }


    /*
    public List<UploadedFileInfo> getUploadedFileInfoList( TestEventScore tes, Locale locale ) throws Exception
    {
        List<UploadedFileInfo> out = new ArrayList<>();

        List<TextAndTitle> ttl = tes.getTextBasedResponseList(null, true, true );

        if( ttl.isEmpty() || tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() || tes.getSimCompetencyClassId()==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() )
            return out;

        UploadedUserFile uuf;

        UploadedFileInfo ufi;

        FileUploadFacade fileUploadFacade = FileUploadFacade.getInstance();

        for( TextAndTitle tt : ttl )
        {
            // LogService.logIt( "TestResultUtils.getUploadedFileInfoList tt=" + tt.getTitle() + ", " + tt.getText() + ", " + tt.getFlags() + ", " + tt.getUploadedUserFileId() );

            if( tt.getUploadedUserFileId() <= 0 )
                continue;

            uuf = fileUploadFacade.getUploadedUserFile(  tt.getUploadedUserFileId(), false ); //, nodeSeq, subnodeSeq, true)

            if( uuf == null )
                continue;

            // refresh if not ready
            if( uuf.getConversionStatusTypeId() != ConversionStatusType.NA.getConversionStatusTypeId() && !uuf.getConversionStatusType().getIsComplete() ) // uuf.getConversionStatusTypeId() != ConversionStatusType.COMPLETE.getConversionStatusTypeId() )
                uuf = fileUploadFacade.getUploadedUserFile(  tt.getUploadedUserFileId(), true );

            ufi = new UploadedFileInfo( tes, tt, uuf, locale );

            out.add( ufi );
        }

        return out;
    }
    */




}
