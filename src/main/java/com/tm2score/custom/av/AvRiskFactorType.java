/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.av;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.util.MessageFactory;
import com.tm2score.xml.XmlUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * AV Sim - Non-Standard Risk Factor String. This is a string with the following format:
 * AvRiskFactorTypeId;int1;int2;int3;float1;float2;float3;str1;str2|AvRiskFactorTypeId;int1;int2;int3;float1;float2;float3;str1;str2|
 *
 *
 * @author Mike
 */
public enum AvRiskFactorType {


    NONE(0,"No factor", false, "", "" ),
    AIMS_CHECK(1,"Aims Response Inconsistency", true, "", "g.AVRisk_AimsInconsistency" ),  
    BEHAVIORAL_RISK(4,"Biodata Risk", true, "", "g.AVRisk_Biodata" ),
    BAD_ATTITUDE_RISK(5,"Bad Attitude", true, "", "g.CT3Risk_BadAttitude" );  // Looks at AIMS Attitude Scale. If less than 50 - risk factor.

    private final int avRiskFactorTypeId;

    private final String name;

    private final boolean standard;

    private final String configuration;

    private final String key;


    private AvRiskFactorType( int s , String n, boolean std, String c, String k)
    {
        this.avRiskFactorTypeId = s;
        this.name = n;
        this.standard = std;
        this.configuration = c;
        this.key = k;
    }

    public static AvRiskFactorType getValue( int id )
    {
        AvRiskFactorType[] vals = AvRiskFactorType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getAvRiskFactorTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public String getRiskText( Locale locale, Map<String,Object> params )
    {
        if( key != null && !key.isEmpty() )
            return MessageFactory.getStringMessage( locale , key );

        else if( params != null && params.get( "str2" )!=null )
        {
            return (String) params.get( "str2" );
        }

        return "";
    }


    public boolean isRiskFactorPresent( TestEvent te, Map<String,Object> params )
    {
        //if( 1==1 )
        //    return true;

        if( equals( NONE ) )
            return false;

        SimCompetencyScore scs;
        SimCompetencyScore scs2;

        float score;
        float score2;

        if( equals( AIMS_CHECK ) )
        {
            float avgScore1 = 0;
            float avgScore2 = 0;
            float count = 0;
            
            scs = getTestEventScoreValue( te, "Exhibits a Positive Work Attitude" );
            scs2 = getTestEventScoreValue( te, "Attitude-Check" );            
            if( scs!=null && scs.hasAnyScoreData() && scs2!=null && scs2.hasAnyScoreData() )
            {
                count++;
                avgScore1 += scs.getUnweightedScaledScore(false);
                avgScore2 += scs.getUnweightedScaledScore(false);
            }

            scs = getTestEventScoreValue( te, "Driven to Succeed" );
            scs2 = getTestEventScoreValue( te, "Driven-Check" );            
            if( scs!=null && scs.hasAnyScoreData() && scs2!=null && scs2.hasAnyScoreData() )
            {
                count++;
                avgScore1 += scs.getUnweightedScaledScore(false);
                avgScore2 += scs.getUnweightedScaledScore(false);
            }

            scs = getTestEventScoreValue( te, "Works Well With Teams" );
            scs2 = getTestEventScoreValue( te, "Teams-Check" );            
            if( scs!=null && scs.hasAnyScoreData() && scs2!=null && scs2.hasAnyScoreData() )
            {
                count++;
                avgScore1 += scs.getUnweightedScaledScore(false);
                avgScore2 += scs.getUnweightedScaledScore(false);
            }

            if( count<=0 )
                return false;
            
            avgScore1 = avgScore1/count;
            avgScore2 = avgScore2/count;
            

            // his attitude score but 
            if( avgScore1 >75 && avgScore2<40 )
                return true;
            
            return false; 
        }

        if( equals( BAD_ATTITUDE_RISK ) )
        {
            Integer minVal = params == null ? 45 : (Integer) params.get( "int1" );

            if( minVal<=0 )
                minVal = 45;

            scs = getTestEventScoreValue( te, "Exhibits a Positive Work Attitude" );

            if( scs == null || !scs.hasAnyScoreData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            if( score <0 )
                return false;

            return score< minVal;
        }
        
        if( equals( BEHAVIORAL_RISK ) )
        {
            scs = getTestEventScoreValue( te, "Behavioral Track Record" );

            if( scs == null || !scs.hasAnyScoreData() )
                return false;

            int scoreColorSchemeTypeId = te.getSimXmlObj().getScorecolorscheme();

            int scoreCategoryTypeId = scs.getScoreCategoryTypeId( ScoreColorSchemeType.getType( scoreColorSchemeTypeId ) );

            return ScoreCategoryType.getValue(scoreCategoryTypeId).red();
        }
        

        

        return false;
    }




    private SimCompetencyScore getTestEventScoreValue( TestEvent te, String engName )
    {
        String n, ne;

        engName = XmlUtils.encodeURIComponent( engName );

        for( SimCompetencyScore scs : te.getSimCompetencyScoreList() )
        {

            ne = scs.getSimCompetencyObj().getNameenglish();

            // LogService.logIt( "AvRiskFactorType.getTestEventScoreValue() seeking " + engName + ", found: " + ne + ", n=" + n );

            if( ne != null && !ne.isEmpty() )
            {
                if( ne.equalsIgnoreCase( engName ) )
                    return scs.hasAnyScoreData() ? scs : null;
            }

            else
            {
                n = scs.getSimCompetencyObj().getName();

                if( n != null && n.equalsIgnoreCase( engName ) )
                    return scs.hasAnyScoreData() ? scs : null;
            }



        }


        return null;
    }



    public static Map<String,Object> getParamsFromConfigStr( String configStr ) throws Exception
    {
        Map<String,Object> params = new HashMap<>();



        if( configStr==null || configStr.isEmpty() || configStr.indexOf( ';' )<=0 )
            return params;

        String[] cv = configStr.trim().split( ";" );

        if( cv.length <1 )
            return params;

        int id,int1,int2,int3;
        float float1,float2,float3;
        String str1,str2;

        AvRiskFactorType rft;

        int1=0;
        int2=0;
        int3=0;
        float1=0;
        float2=0;
        float3=0;
        str1="";
        str2="";

        id = Integer.parseInt( cv[0] );
        if( cv.length>1 )
            int1 = Integer.parseInt( cv[1] );
        if( cv.length>2 )
            int2 = Integer.parseInt( cv[2] );
        if( cv.length>3 )
            int3 = Integer.parseInt( cv[3] );

        if( cv.length>4 )
            float1 = Float.parseFloat( cv[4] );
        if( cv.length>5 )
            float2 = Float.parseFloat( cv[5] );
        if( cv.length>6 )
            float3 = Float.parseFloat( cv[6] );

        if( cv.length>7 )
            str1 = cv[7];

        if( cv.length>8 )
            str2 = cv[8];

        if( id <= 0 )
            throw new Exception("AvRiskFactorTypeId invalid:" + id );

        params.put( "id", new Integer(id) );
        params.put( "int1", new Integer(int1) );
        params.put( "int2", new Integer(int2) );
        params.put( "int3", new Integer(int3) );
        params.put( "float1", new Float(float1) );
        params.put( "float2", new Float(float2) );
        params.put( "float3", new Float(float3) );
        params.put( "str1", str1 );
        params.put( "str2", str2 );

        return params;
    }










    public int getAvRiskFactorTypeId()
    {
        return avRiskFactorTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getConfiguration() {
        return configuration;
    }

    public boolean isStandard() {
        return standard;
    }

    public String getKey() {
        return key;
    }


}
