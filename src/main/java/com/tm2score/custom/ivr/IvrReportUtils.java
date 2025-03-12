/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.ivr;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.simlet.CompetencyScoreType;
import com.tm2score.voicevibes.VoiceVibesResult;
import com.tm2score.voicevibes.VoiceVibesScaleScore;
import com.tm2score.voicevibes.VoiceVibesScaleType;
import com.tm2score.voicevibes.VoiceVibesWordSpotType;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class IvrReportUtils {
   
    public static boolean isStandardVoiceReport( long reportId )
    {
        for( long l : RuntimeConstants.getLongArray( "stdVoiceReportIds", ";" ) )
        {
            if( reportId == l )
                return true;
        }
        
        return false;        
    }    
    
    
    public static void setVoiceVibesScoresForSampleReport( Locale l, List<TestEventScore> tesl ) throws Exception
    {
        for( TestEventScore tes : tesl )
        {
            // if( tes.getSimCompetencyClassId()!=SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() && tes.getSimCompetencyClassId()!=SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() )
            if( tes.getSimCompetencyClassId()!=SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() && tes.getSimCompetencyClassId()!=SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() && tes.getScoreTypeIdUsed()!=CompetencyScoreType.SCORED_AV_UPLOAD.getCompetencyScoreTypeId() )
                continue;
            
            // Need to create a vibes result. 
            VoiceVibesResult vvr = createRandomVoiceVibesResult( tes );
            
            String tp = tes.getTextParam1();
            
            if( tp==null )
                tp="";
            
            tp += vvr.getPackedTokenStringForTestEventScore();
            
            tes.setTextParam1( tp );
        }
    }
    
    private static VoiceVibesResult createRandomVoiceVibesResult( TestEventScore tes ) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        
        int wordCount = (int) Math.round( Math.random()*300f );
        
        sb.append( "[VVWORDCOUNT]" + wordCount );
        
        VoiceVibesScaleScore wordSpotScaleScore = null;
        
        StringBuilder ss;
        
        sb.append( "[VVSCALES]" );

        ss = new StringBuilder();

        VoiceVibesScaleScore vvss;
        
        for( VoiceVibesScaleType vvst : VoiceVibesScaleType.values() )
        {
            vvss = new VoiceVibesScaleScore( vvst, (float) Math.random()*100f );

            if( vvss.getVoiceVibesScaleType().isWordspot() )
                wordSpotScaleScore = vvss;

            if( ss.length()>0 )
                ss.append( ";" );

            ss.append( vvss.getVoiceVibesScaleType().getVoiceVibesScaleTypeId() + ";" + vvss.getScore() );
        }

        sb.append( ss.toString() );
             
        if( wordSpotScaleScore != null  )
        {
            Integer wsCount;
            
            sb.append( "[VVWORDSPOTS]" );
            
            ss = new StringBuilder();
            
            // Map<String,Integer> wordSpotMap = wordSpotScaleScore.getWordSpotMap();
            
            for( VoiceVibesWordSpotType vwst : VoiceVibesWordSpotType.values() )
            {
                wsCount = ((int) (Math.random()*4f)) - 1;
                
                if( wsCount <0 )
                    continue;
                
                if( wsCount==0 )
                    continue;
                                
                if( ss.length()>0 )
                    ss.append( ";" );
                
                ss.append( vwst.getVoiceVibesWordSpotTypeId() + ";" + wsCount.toString() );
            }
            
            sb.append( ss.toString() );
            
        }
        
        return VoiceVibesResult.getFromPackedString( sb.toString() );
    }
    
}
