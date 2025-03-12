/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.coretest2;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.XmlUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * CT3 Sim - Non-Standard Risk Factor String. This is a string with the following format:
 * CT3RiskFactorTypeId;int1;int2;int3;float1;float2;float3;str1;str2|CT3RiskFactorTypeId;int1;int2;int3;float1;float2;float3;str1;str2|
 *
 *
 * @author Mike
 */
public enum CT3RiskFactorType {


    NONE(0,"No factor", false, "", "" ),
    AIMS_FAKING(1,"AIMS Faking Indicated", true, "", "g.CT3Risk_AIMSFaking" ),  // Looks at top 3 AIMS scales - do they go together? Looks at bottom three - do they go together?
    LOW_CITIZENSHIP(2,"Low Citizenship", true, "", "g.CT3Risk_LowCitizenship" ),    // Looks at citizenship. If less than 45 it's a risk factor.
    UNPRODUCTIVE(3,"Unproductive Behavior", true, "", "g.CT3Risk_UnproductiveBeh" ),   // Looks at AIMSFaking" ),  // Looks at Biodata unproductive - if red, it's a risk factor.
    FLIGHT_RISK(4,"Flight Risk", true, "", "g.CT3Risk_Flight" ),
    BAD_ATTITUDE_RISK(5,"Bad Attitude", true, "", "g.CT3Risk_BadAttitude" ),  // Looks at AIMS Attitude Scale. If less than 50 - risk factor.
    LOW_INTEGRITY(6,"Low Integrity", true, "", "g.CT3Risk_LowIntegrity" ),    // Looks at integrity. If less than 45 it's a risk factor.
    LOW_DRIVE(7,"Low Drive", true, "", "g.CT3Risk_LowDrive" ),    // Looks at drive. If less than 45 it's a risk factor.
    LOW_TEAMWORK(8,"Low Teamwork", true, "", "g.CT3Risk_LowTeamwork" ),    // Looks at teamwork. If less than 45 it's a risk factor.

    SALES_DRIVE(100,"Low Sales Drive", false, "100;competitive_min_score;0;0;0;0;0;;;", "g.CT3Risk_LowSalesDrive" ),
    CUSTFACING_LOWEXPRESSIVE(101,"Low Expressive", false, "100;expressive_min_score;0;0;0;0;0;;;", "g.CT3Risk_LowExpressive" ),
    CUSTFACING_RELATIONSHIPS(102,"Low Relationships", false, "100;min_score;0;0;0;0;0;;;", "g.CT3Risk_LowRelationships" ),

    GENERIC_RANGEVAL(200,"Generic Range Value", false, "200;min_score;max_score;0;0;0;0;CompetencyEnglishName;Risk Factor Text;", "" );

    private final int ct3RiskFactorTypeId;

    private final String name;

    private final boolean standard;

    private final String configuration;

    private final String key;


    private CT3RiskFactorType( int s , String n, boolean std, String c, String k)
    {
        this.ct3RiskFactorTypeId = s;
        this.name = n;
        this.standard = std;
        this.configuration = c;
        this.key = k;
    }

    public static CT3RiskFactorType getValue( int id )
    {
        CT3RiskFactorType[] vals = CT3RiskFactorType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCT3RiskFactorTypeId() == id )
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
        SimCompetencyScore scs3;

        float score;
        float score2;
        float score3;

        if( equals( AIMS_FAKING ) )
        {
            int badCount = 0;

            List<SimCompetencyScore> top3 = new ArrayList<>();
            List<SimCompetencyScore> bot3 = new ArrayList<>();

            List<SimCompetencyScore> scsl = new ArrayList<>();

            for( SimCompetencyScore sc : te.getSimCompetencyScoreList() )
            {
                if( sc.hasAnyScoreData() && sc.getHasScoreableData() && sc.getSimCompetencyClass().isAIMS() )
                    scsl.add( sc );
            }

            if( scsl.size()<6 )
                return false;

            Collections.sort( scsl, new CT3SimCompetencyScoreScaleScoreComparator() );

            for( int i=0;i<3 && i<scsl.size();i++ )
            {
                scs = scsl.get(i);
                bot3.add( scs);
            }

            Collections.reverse( scsl );

            for( int i=0;i<3 && i<scsl.size();i++ )
            {
                scs = scsl.get(i);
                top3.add( scs);
            }

            // First Pair
            if( containsOppositeSimCompetencyScores( "Corporate Citizenship","Exhibits a Positive Work Attitude", top3, bot3 ) )
            {
                badCount++;
            }

            if( containsOppositeSimCompetencyScores( "Needs Structure","Seeks Perfection", top3, bot3 ) )
            {
                scs = getTestEventScoreValue( te, "Needs Structure" );
                
                if( scs == null || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                    return false;
                
                score = scs.getUnweightedScaledScore( false );

                scs2 = getTestEventScoreValue( te, "Seeks Perfection" );

                if( scs2 == null || !scs2.hasAnyScoreData() || !scs.getHasScoreableData() )
                    return false;
                
                score2 = scs2.getUnweightedScaledScore( false );

                if( score2 > score || (score2 + 8.91f < score ) )
                    badCount++;
            }

            if( containsOppositeSimCompetencyScores( "Expressive and Outgoing","Develops Relationships", top3, bot3 ) )
            {
                scs = getTestEventScoreValue( te, "Develops Relationships" );

                if( scs == null || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                    return false;
                
                score = scs.getUnweightedScaledScore( false );

                scs2 = getTestEventScoreValue( te, "Expressive and Outgoing" );
                
                if( scs2 == null || !scs2.hasAnyScoreData() || !scs.getHasScoreableData() )
                    return false;
                
                score2 = scs2.getUnweightedScaledScore( false );

                if( score2 > score || (score2 + 42.47 < score ) )
                    badCount++;
            }

            if( containsOppositeSimCompetencyScores( "Innovative and Creative","Enjoys Problem-Solving", top3, bot3 ) )
                badCount++;

            if( badCount > 0 )
                return true;

            scs = getTestEventScoreValue( te, "Enjoys Problem-Solving" );

            if( scs == null || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            scs2 = getTestEventScoreValue( te, "Corporate Citizenship" );

            if( scs2 == null || !scs2.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score2 = scs2.getUnweightedScaledScore( false );

            scs3 = getTestEventScoreValue( te, "Exhibits a Positive Work Attitude" );

            if( scs3 == null || !scs3.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;
                
            score3 = scs3.getUnweightedScaledScore( false );

            if( score>=100f && score2>=100f && score3>=100f )
                return true;

            return false; // badCount > 1;
        }

        if( equals( LOW_CITIZENSHIP ) )
        {
            Integer minVal = params == null ? 45 : (Integer) params.get( "int1" );

            if( minVal == null || minVal<=0 )
                minVal = 45;

            scs = getTestEventScoreValue( te, "Corporate Citizenship" );

            if( scs == null || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            if( score <0 )
                return false;

            return score< minVal;
        }

        if( equals( LOW_INTEGRITY ) )
        {
            Integer minVal = params == null ? 45 : (Integer) params.get( "int1" );
            if( minVal == null || minVal<=0 )
                minVal = 45;
            scs = getTestEventScoreValue( te, "Integrity" );
            if( scs == null || !scs.getSimCompetencyClass().isAIMS() || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;
            score = scs.getUnweightedScaledScore( false );
            if( score <0 )
                return false;
            return score< minVal;
        }
        if( equals( LOW_DRIVE ) )
        {
            Integer minVal = params == null ? 45 : (Integer) params.get( "int1" );
            if( minVal == null || minVal<=0 )
                minVal = 45;
            scs = getTestEventScoreValue( te, "Drive" );
            if( scs == null || !scs.getSimCompetencyClass().isAIMS() || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;
            score = scs.getUnweightedScaledScore( false );
            if( score <0 )
                return false;
            return score< minVal;
        }
        if( equals( LOW_TEAMWORK ) )
        {
            Integer minVal = params == null ? 45 : (Integer) params.get( "int1" );
            if( minVal == null || minVal<=0 )
                minVal = 45;
            scs = getTestEventScoreValue( te, "Teamwork" );
            if( scs == null || !scs.getSimCompetencyClass().isAIMS() || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;
            score = scs.getUnweightedScaledScore( false );
            if( score <0 )
                return false;
            return score< minVal;
        }


        if( equals( UNPRODUCTIVE ) )
        {
            scs = getTestEventScoreValue( te, "History Survey - Unproductive Behavior" );

            if( scs == null || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            int scoreColorSchemeTypeId = te.getSimXmlObj().getScorecolorscheme();

            int scoreCategoryTypeId = scs.getScoreCategoryTypeId( ScoreColorSchemeType.getType( scoreColorSchemeTypeId ) );

            return ScoreCategoryType.getValue(scoreCategoryTypeId).red();
        }

        if( equals( FLIGHT_RISK ) )
        {
            scs = getTestEventScoreValue( te, "History Survey - Tenure" );

            if( scs == null || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            int scoreColorSchemeTypeId = te.getSimXmlObj().getScorecolorscheme();

            int scoreCategoryTypeId = scs.getScoreCategoryTypeId( ScoreColorSchemeType.getType( scoreColorSchemeTypeId ) );

            return ScoreCategoryType.getValue(scoreCategoryTypeId).red();
        }

        if( equals( BAD_ATTITUDE_RISK ) )
        {
            Integer minVal = params == null ? 45 : (Integer) params.get( "int1" );

            if( minVal<=0 )
                minVal = 45;

            scs = getTestEventScoreValue( te, "Exhibits a Positive Work Attitude" );

            if( scs == null || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            if( score <0 )
                return false;

            return score< minVal;
        }

        if( equals( SALES_DRIVE ) )
        {
            Integer minVal = params == null ? 20 : (Integer) params.get( "int1" );

            if( minVal<=0 )
                minVal = 45;

            scs = getTestEventScoreValue( te, "Competitive" );

            if( scs == null  || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            if( score <0 )
                return false;

            return score< minVal;
        }

        if( equals( CUSTFACING_LOWEXPRESSIVE ) )
        {
            Integer minVal = params == null ? 20 : (Integer) params.get( "int1" );

            if( minVal<=0 )
                minVal = 0;

            scs = getTestEventScoreValue( te, "Expressive and Outgoing" );

            if( scs == null  || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            // LogService.logIt( "Ct3RiskFactorType.isRiskFactorPresent() Expressive: minVal=" + minVal + ", score=" + score );

            if( score >=0 && score< minVal )
                return true;

            return score >= 0 && score< minVal;
            /*
            scs = getTestEventScoreValue( te, "Develops Relationships" );

            if( scs == null )
                return false;

            score = scs.getUnweightedScaledScore( false );

            if( score <0 )
                return false;

            return score< minVal;
            */
        }

        if( equals( CUSTFACING_RELATIONSHIPS ) )
        {
            Integer minVal = params == null ? 20 : (Integer) params.get( "int1" );

            if( minVal<=0 )
                minVal = 0;

            // LogService.logIt( "Ct3RiskFactorType.isRiskFactorPresent() Develops Relationships: minVal=" + minVal + ", from Params=" + (params==null ? "null" : (Integer) params.get( "int1" ))  );

            scs = getTestEventScoreValue( te, "Develops Relationships" );

            if( scs == null  || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            // LogService.logIt( "Ct3RiskFactorType.isRiskFactorPresent() Develops Relationships: minVal=" + minVal + ", score=" + score + ", computed result=" + (score >=0 && score< minVal) );

            return score >= 0 && score< minVal;

            /*
            if( score >=0 && score< minVal )
                return true;

            if( score < 0 )
                return false;

            return score >= 0 && score< minVal;

            scs = getTestEventScoreValue( te, "Expressive and Outgoing" );

            if( scs == null )
                return false;

            score = scs.getUnweightedScaledScore( false );

            if( score <0 )
                return false;

            return score < minVal;
            */
        }

        if( equals( GENERIC_RANGEVAL ) )
        {
            Integer minVal = params == null ? -10000 : (Integer) params.get( "int1" );

            if( minVal<0 )
                minVal = -99999;

            Integer maxVal = params == null ? 100 : (Integer) params.get( "int2" );

            if( maxVal<0 )
                maxVal = 99999;

            scs = getTestEventScoreValue( te, (String)params.get("str1") );

            // LogService.logIt( "CT3RiskFactorType.isPresent() Generic Range minVal=" + minVal + ", maxVal=" + maxVal + ", scs for: " + ((String)params.get("str1")) + ", found it=" + (scs != null ) );

            if( scs == null  || !scs.hasAnyScoreData() || !scs.getHasScoreableData() )
                return false;

            score = scs.getUnweightedScaledScore( false );

            if( score <0 )
                return false;

            return score>maxVal || score<minVal;
        }


        return false;
    }

    private boolean containsOppositeSimCompetencyScores( String scsNameEng1,String scsNameEng2, List<SimCompetencyScore> top3, List<SimCompetencyScore> bot3 )
    {
        if( containsSimCompetencyScore( scsNameEng1, top3 ) && containsSimCompetencyScore( scsNameEng2, bot3 ) )
            return true;

        if( containsSimCompetencyScore( scsNameEng1, bot3 ) && containsSimCompetencyScore( scsNameEng2, top3 ) )
            return true;

        return false;
    }




    private boolean containsSimCompetencyScore( String scsNameEng, List<SimCompetencyScore> scsl )
    {
        for( SimCompetencyScore scs : scsl )
        {
            if( scs.getSimCompetencyObj().getNameenglish()!= null && scs.getSimCompetencyObj().getNameenglish().equalsIgnoreCase( scsNameEng ) )
                return true;
            if( scs.getSimCompetencyObj().getName()!= null && scs.getSimCompetencyObj().getName().equalsIgnoreCase( scsNameEng ) )
                return true;
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

            // LogService.logIt( "CT3RiskFactorType.getTestEventScoreValue() seeking " + engName + ", found: " + ne + ", n=" + n );

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

        try
        {
            
            String[] cv = configStr.trim().split( ";" );

            if( cv.length <1 )
                return params;

            int id,int1,int2,int3;
            float float1,float2,float3;
            String str1,str2;

            CT3RiskFactorType rft;

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
                throw new Exception("CT3RiskFactorTypeId invalid:" + id );

            params.put("id", id);
            params.put("int1", int1);
            params.put("int2", int2);
            params.put("int3", int3);
            params.put("float1", float1);
            params.put("float2", float2);
            params.put("float3", float3);
            params.put( "str1", str1 );
            params.put( "str2", str2 );
        }
        catch( NumberFormatException e )
        {
            LogService.logIt( "Ct3RiskFactorType.getParamsFromConfigStr() NONFATAL, " + e.toString() + " ConfigStr appears invalid. configStr=" + StringUtils.truncateStringWithTrailer(configStr, 120, false ) );
        }

        return params;
    }










    public int getCT3RiskFactorTypeId()
    {
        return ct3RiskFactorTypeId;
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
