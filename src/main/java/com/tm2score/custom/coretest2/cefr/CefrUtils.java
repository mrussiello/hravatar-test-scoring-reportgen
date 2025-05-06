/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.custom.coretest2.cefr;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.global.Constants;
import com.tm2score.score.iactnresp.ScorableResponse;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import com.tm2score.util.UrlEncodingUtils;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker
 */
public class CefrUtils 
{
    public static String getCefrScoreDescription( Locale locale, CefrScoreType cefrScoreType, String stub )
    {
        return cefrScoreType.getDescription(locale, stub);
    }
    
    
    public static ScoreCategoryType getCefrScoreCategoryType( SimJ simJ, CefrScoreType cefrScoreType, ScoreColorSchemeType scst )
    {
        float scaledScore = cefrScoreType.getNumericEquivalentScore( scst );

        if( scst.getIsSevenColor() && scaledScore >= simJ.getWhitemin() )
            return ScoreCategoryType.WHITE;

        else if( scaledScore >= simJ.getGreenmin() )
            return ScoreCategoryType.GREEN;

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simJ.getYellowgreenmin() )
            return ScoreCategoryType.YELLOWGREEN;

        else if( scaledScore >= simJ.getYellowmin() )
            return ScoreCategoryType.YELLOW;

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simJ.getRedyellowmin() )
            return ScoreCategoryType.REDYELLOW;

        else if( scst.getIsSevenColor() && scaledScore >= simJ.getRedmin() )
            return ScoreCategoryType.RED;

        else if( scst.getIsSevenColor()  )
            return ScoreCategoryType.BLACK;

       return ScoreCategoryType.RED;
    }
    
    public static String getGeneralOverallScoreTextForCefrScore( SimJ simJ, CefrScoreType cefrScoreType, ScoreColorSchemeType scst )
    {
        if( simJ == null )
            return null;

        String t = "";

        float scaledScore = cefrScoreType.getNumericEquivalentScore( scst );

        if( scst.getIsSevenColor() && scaledScore >= simJ.getWhitemin() )
            t += UrlEncodingUtils.decodeKeepPlus( simJ.getWhitetext() == null ? "" : simJ.getWhitetext() );

        else if( scaledScore >= simJ.getGreenmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simJ.getGreentext() == null ? "" : simJ.getGreentext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simJ.getYellowgreenmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simJ.getYellowgreentext() == null ? "" : simJ.getYellowgreentext() );

        else if( scaledScore >= simJ.getYellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simJ.getYellowtext()==null ? "" : simJ.getYellowtext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simJ.getRedyellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simJ.getRedyellowtext() == null ? "" : simJ.getRedyellowtext() );

        else if( scst.getIsSevenColor() && scaledScore >= simJ.getRedmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simJ.getRedtext() == null ? "" : simJ.getRedtext() );

        else if( scst.getIsSevenColor()  )
            t += UrlEncodingUtils.decodeKeepPlus( simJ.getBlacktext() == null ? "" : simJ.getBlacktext() );

        else
            t +=  UrlEncodingUtils.decodeKeepPlus( simJ.getRedtext() == null ? "" : simJ.getRedtext() );

       return t;
    }

    public static String getGeneralSimCompetencyScoreTextForCefrScore( SimJ.Simcompetency simCompetencyObj, CefrScoreType cefrScoreType, ScoreColorSchemeType scst )
    {
        if( simCompetencyObj == null )
            return null;

        String t = "";

        float scaledScore = cefrScoreType.getNumericEquivalentScore( scst );

        if( simCompetencyObj.getHighcliffmin()> 0 && simCompetencyObj.getHighclifflevel()>0 && scaledScore >= simCompetencyObj.getHighcliffmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getHighclifftext() == null ? "" : simCompetencyObj.getHighclifftext() );

        else if( scst.getIsSevenColor() && scaledScore >= simCompetencyObj.getWhitemin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getWhitetext() == null ? "" : simCompetencyObj.getWhitetext() );

        else if( scaledScore >= simCompetencyObj.getGreenmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getGreentext() == null ? "" : simCompetencyObj.getGreentext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simCompetencyObj.getYellowgreenmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowgreentext() == null ? "" : simCompetencyObj.getYellowgreentext() );

        else if( scaledScore >= simCompetencyObj.getYellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getYellowtext()==null ? "" : simCompetencyObj.getYellowtext() );

        else if( scst.getIsFiveOrSevenColor() && scaledScore >= simCompetencyObj.getRedyellowmin() )
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedyellowtext() == null ? "" : simCompetencyObj.getRedyellowtext() );

        else if( scst.getIsSevenColor() && scaledScore >= simCompetencyObj.getRedmin() )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedtext() == null ? "" : simCompetencyObj.getRedtext() );

        else if( scst.getIsSevenColor()  )
            t += UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getBlacktext() == null ? "" : simCompetencyObj.getBlacktext() );

        else
            t +=  UrlEncodingUtils.decodeKeepPlus( simCompetencyObj.getRedtext() == null ? "" : simCompetencyObj.getRedtext() );

       return t;
    }

    
    
    
    /*
     Returns 
    data[0] CefrScoreType
    data[1] stub  (reading,listen, or both)
    */
    public static Object[] getCefrScoreInfoForOverall( TestEvent te, List<TestEventScore> tesl )
    {
        Object[] out = new Object[2];
        // Go thru Sim Competencies and get the lowest CEFR Level found. 
        CefrScoreType cefrScoreType = null;
        CefrScoreType cefrScoreTypeSc;
        int count = 0;
        CefrType cefrType=null;
        for( TestEventScore tes : tesl )
        {
            if( !tes.getTestEventScoreType().getIsCompetency() )
                continue;
            
            cefrScoreTypeSc = getCefrScoreTypeForTes( tes );
            
            if( cefrScoreTypeSc==null || cefrScoreTypeSc.equals(CefrScoreType.UNKNOWN))
                continue;
            
            // lowest score
            if( cefrScoreType==null || cefrScoreTypeSc.getCefrScoreTypeId()<cefrScoreType.getCefrScoreTypeId() )
                cefrScoreType=cefrScoreTypeSc;  
            
            if( !getCefrTypeForTes( tes ).equals(CefrType.NONE))
            {
                cefrType = getCefrTypeForTes( tes );
                count++;
            }
        }
        out[0] = cefrScoreType==null ? CefrScoreType.UNKNOWN : cefrScoreType;
        out[1] = cefrType!=null && count<=1 ? cefrType.getStub() : "both";
        return out;
    }

    public static String getCefrScoreTextForTes( Locale locale, TestEventScore tes )
    {        
        // get localized version
        CefrScoreType st = getCefrScoreTypeForTes( tes );
        CefrType typ = getCefrTypeForTes( tes );
        
        if( locale!=null && typ!=null && !typ.equals(CefrType.NONE) )
            return CefrUtils.getCefrScoreDescription( locale, st, typ.getStub() );
        
        if( locale!=null && st!=null && st.equals(CefrScoreType.UNKNOWN) && typ!=null && !typ.equals(CefrType.NONE))
            return st.getDescription(locale, typ.getStub());
        
        return getCefrScoreTextForTes(tes);
    }
    
    
    public static String getCefrScoreTextForTes( TestEventScore tes )
    {
        if( tes.getTextParam1()==null || tes.getTextParam1().isBlank() )
            return null;
        return StringUtils.getBracketedArtifactFromString(tes.getTextParam1(), Constants.CEFRLEVELTEXT );
    }
    
    public static CefrScoreType getCefrScoreTypeForTes( TestEventScore tes )
    {
        if( tes.getTextParam1()==null || tes.getTextParam1().isBlank() )
            return CefrScoreType.UNKNOWN;
        String val = StringUtils.getBracketedArtifactFromString(tes.getTextParam1(), Constants.CEFRLEVEL );
        return CefrScoreType.getFromText(val);
    }

    public static CefrType getCefrTypeForTes( TestEventScore tes )
    {
        if( tes.getTextParam1()==null || tes.getTextParam1().isBlank() )
            return CefrType.NONE;
        String val = StringUtils.getBracketedArtifactFromString(tes.getTextParam1(), Constants.CEFRTYPE );
        return CefrType.getValue( val!=null && !val.isBlank() ? Integer.parseInt(val) : 0);
    }
    
    
    /*
     Should only show the last pass level (cefrScoreType) plus 1 (the one they failed)
    */
    public static boolean showTopicCaveatForCefrScore( String caveat, CefrScoreType cefrScoreType, Locale locale )
    {
        if( caveat==null || caveat.isBlank() || !caveat.contains("TOPIC~") )
            return true;
        
        CefrScoreType cefrScoreTypeForCaveat = CefrScoreType.getValueForCaveat(caveat, locale);
        
        return cefrScoreTypeForCaveat.getCefrScoreTypeId()<=(cefrScoreType.getCefrScoreTypeId()+1);
    }
    
    public static CefrScoreType getCefrScoreTypeForSimCompetency( TestEvent te, SimCompetencyScore scs )
    {
        // for each level, calculate percent correct
        // index=cefrScoreTypeId
        float[] countArray = new float[8];
        float[] percentCorrectArray = new float[8];
        List<ScorableResponse> irList = te.getAutoScorableResponseList();
        
        CefrScoreType cefrScoreType;
        SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic ct5Subtopic;
                
        for( ScorableResponse sr : irList )
        {
            // response is wrong comptency.
            if( sr.simCompetencyId()!=scs.getSimCompetencyObj().getId() )
                continue;
            
            ct5Subtopic = getCt5SubtopicFromSimCompetency(sr.getCt5SubtopicId(),  scs.getSimCompetencyObj());
            
            // LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() BBB.1 " + scs.getName() + ", ct5Subtopic=" + (ct5Subtopic==null ? "null" : ct5Subtopic.getName()) );
            // No ct5Subtopic - skip
            if( ct5Subtopic==null )
            {
                LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() BBB.2A NO Ct5Subtopic found for Ct5SubtopicId=" + sr.getCt5SubtopicId() + ", itemId=" + sr.getCt5ItemId() + ", correct=" + sr.correct() );
                continue;
            }
            
            cefrScoreType = getCefrScoreTypeFromCt5Subtopic( ct5Subtopic );

            LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() BBB.2 Subtopic.cefrScoreType=" + cefrScoreType.getName() + ", correct=" + sr.correct() );
            
            countArray[cefrScoreType.getCefrScoreTypeId()]++;
            
            if( sr.correct() )
                percentCorrectArray[cefrScoreType.getCefrScoreTypeId()]++;
        }

        
        // finalize scores
        for( int i=1;i<percentCorrectArray.length; i++ )
        {
            // LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() CCC.1 count[" + i + "]=" + countArray[i] + ", raw percentCorrect[i]=" + percentCorrectArray[i] );
            percentCorrectArray[i] = (countArray[i]<=0) ? 0 : 100f*percentCorrectArray[i]/countArray[i];
            LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() CCC.2 final percentCorrect[" + i + "]=" + percentCorrectArray[i] );
        }

        cefrScoreType = null;
                    
        // check from bottom
        for( int i=1;i<percentCorrectArray.length; i++ )
        {   

            LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() CCC.2A count[" + i + "]=" + countArray[i] + ", raw percentCorrect[i]=" + percentCorrectArray[i] );
            
            // no items at this level, skip it (should not happen except for advanced)
            if( countArray[i]<=0 )
            {
                continue;
            }
            
            // failed this level.
            if( percentCorrectArray[i]<66.7f )
            {
                LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() DDD.1 failed level " + i );
                
                // last passed is present.
                if( cefrScoreType!=null )
                    return cefrScoreType;
                
                // bottom rung
                if( i<=1 )
                    return CefrScoreType.PREA1;
                
                // not bottom but not passed. Go 1 down.
                return CefrScoreType.getValue(i-1) ;                  
            }

            // last passed
            cefrScoreType = CefrScoreType.getValue(i);

            LogService.logIt( "CefrUtils.getCefrScoreTypeForSimCompetency() DDD.2 passed level " + i );
            
            // if passed the highest level. Return it.
            if( i==7 )
                return CefrScoreType.C2; 
        }
        
        // Should only happen if no CEFR items found.
        return cefrScoreType==null ? CefrScoreType.UNKNOWN : cefrScoreType;
    }

    
    private static CefrScoreType getCefrScoreTypeFromCt5Subtopic( SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic ct5Subtopic )
    {
        if( ct5Subtopic==null || ct5Subtopic.getDescrip()==null || ct5Subtopic.getDescrip().isBlank() )
            return CefrScoreType.UNKNOWN;
        
        String val = StringUtils.getBracketedArtifactFromString( StringUtils.getUrlDecodedValue(ct5Subtopic.getDescrip()), Constants.CEFRLEVEL);
        
        // LogService.logIt( "CefrUtils.getCefrScoreTypeFromCt5Subtopic() val=" + val + ", descript=" + ct5Subtopic.getDescrip() );
        return CefrScoreType.getFromText(val);
    }
    
    private static SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic getCt5SubtopicFromSimCompetency( int ct5SubtopicId, SimJ.Simcompetency sc )
    {
        if( ct5SubtopicId<=0 || sc==null || sc.getCt5Subtopics()==null )
            return null;
        
        for( SimJ.Simcompetency.Ct5Subtopics.Ct5Subtopic st : sc.getCt5Subtopics().getCt5Subtopic() )
        {
            if( st.getCt5Subtopicid()==ct5SubtopicId )
                return st;
        }
        return null;
        
    }
    
}
