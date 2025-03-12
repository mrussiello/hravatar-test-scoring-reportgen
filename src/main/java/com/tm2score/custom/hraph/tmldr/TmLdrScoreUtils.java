/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.tmldr;

import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.sim.SimCompetencyGroupType;
import com.tm2score.sim.SimCompetencyVisibilityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class TmLdrScoreUtils {
    
    
    public static boolean TESTMODE = true;
    
    public static java.util.List<TestEventScore> getTmLdrTestEventScoreList( int tmLdrCompetencyTypeId, java.util.List<TestEventScore> tesl )
    {
        java.util.List<TestEventScore> out = new ArrayList<>();

        String nm;

        for( TestEventScore tes : tesl )
        {
            // if supposed to hide
            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;
            
            if( tmLdrCompetencyTypeId == TmLdrCompetencyType.ABILITY.getTmLdrCompetencyTypeId() )
            {
                if( tes.getSimCompetencyClassId() == SimCompetencyClass.ABILITY.getSimCompetencyClassId() )
                    out.add( tes );

                else if( tes.getSimCompetencyClassId() == SimCompetencyClass.ABILITY_COMBO.getSimCompetencyClassId() )
                    out.add( tes );
                
                else if( tes.getSimCompetencyClassId() == SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() )
                    out.add( tes );

            }

            else if( tmLdrCompetencyTypeId == TmLdrCompetencyType.PERSONALCOMPETENCE.getTmLdrCompetencyTypeId() )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() && tes.getSimCompetencyClassId() != SimCompetencyClass.EQ.getSimCompetencyClassId() )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.persCompetencyNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }

            }

            else if( tmLdrCompetencyTypeId == TmLdrCompetencyType.PEOPLEMANAGEMENT.getTmLdrCompetencyTypeId() )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.CORESKILL.getSimCompetencyClassId() && tes.getSimCompetencyClassId() != SimCompetencyClass.KNOWLEDGE.getSimCompetencyClassId()  )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.peopleMgmtNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }
            }

            else if( tmLdrCompetencyTypeId == TmLdrCompetencyType.COMMITMENT.getTmLdrCompetencyTypeId() )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.commitmentNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }
            }

            else if( tmLdrCompetencyTypeId ==TmLdrCompetencyType.MOTIVATION.getTmLdrCompetencyTypeId() )
            {
                if( tes.getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() )
                    continue;

                if( tes.getNameEnglish()!= null && !tes.getNameEnglish().isEmpty() )
                    nm = tes.getNameEnglish();

                else
                    nm = tes.getName();

                for( String s : BaseTmLdrReportTemplate.motivationNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add( tes );
                }

            }
        }

        // TESTING ONLY!
        if( TESTMODE && out.isEmpty() )
        {
            out = tesl;

            if( out.size()> 2 )
                out = out.subList( 0 , 2 );
        }


        Collections.sort( out );

        return out;

    }

    public static java.util.List<SimCompetencyScore> getTmLdrSimCompetencyScoreList( int tmLdrCompetencyTypeId, java.util.List<SimCompetencyScore> scsl )
    {
        java.util.List<SimCompetencyScore> out = new ArrayList<>();

        String nm;

        for( SimCompetencyScore scs : scsl )
        {
            // if supposed to hide
            //if( scs.getSimCompetencyObj().getHide()==1 )
            //    continue;
            
            if( !SimCompetencyVisibilityType.getValue( scs.getSimCompetencyObj().getHide() ).getShowInReports() )
                continue;

            if( tmLdrCompetencyTypeId == TmLdrCompetencyType.ABILITY.getTmLdrCompetencyTypeId() )
            {
                if( scs.getSimCompetencyClass().getSimCompetencyClassId()== SimCompetencyClass.ABILITY.getSimCompetencyClassId() )
                    out.add(scs );

                else if( scs.getSimCompetencyClass().getSimCompetencyClassId()== SimCompetencyClass.ABILITY_COMBO.getSimCompetencyClassId() )
                    out.add(scs );

                else if( scs.getSimCompetencyClass().getSimCompetencyClassId() == SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() )
                    out.add(scs );

            }

            else if( tmLdrCompetencyTypeId == TmLdrCompetencyType.PERSONALCOMPETENCE.getTmLdrCompetencyTypeId() )
            {
                if( scs.getSimCompetencyClass().getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() && scs.getSimCompetencyClass().getSimCompetencyClassId() != SimCompetencyClass.EQ.getSimCompetencyClassId() )
                    continue;

                if( scs.getNameEnglish()!= null && !scs.getNameEnglish().isEmpty() )
                    nm = scs.getNameEnglish();

                else
                    nm = scs.getName();

                for( String s : BaseTmLdrReportTemplate.persCompetencyNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add(scs );
                }

            }

            else if( tmLdrCompetencyTypeId == TmLdrCompetencyType.PEOPLEMANAGEMENT.getTmLdrCompetencyTypeId() )
            {
                if( scs.getSimCompetencyClass().getSimCompetencyClassId() != SimCompetencyClass.CORESKILL.getSimCompetencyClassId() && scs.getSimCompetencyClass().getSimCompetencyClassId() != SimCompetencyClass.KNOWLEDGE.getSimCompetencyClassId()  )
                    continue;

                if( scs.getNameEnglish()!= null && !scs.getNameEnglish().isEmpty() )
                    nm = scs.getNameEnglish();

                else
                    nm = scs.getName();

                for( String s : BaseTmLdrReportTemplate.peopleMgmtNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add(scs );
                }
            }

            else if( tmLdrCompetencyTypeId == TmLdrCompetencyType.COMMITMENT.getTmLdrCompetencyTypeId() )
            {
                if( scs.getSimCompetencyClass().getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() )
                    continue;

                if( scs.getNameEnglish()!= null && !scs.getNameEnglish().isEmpty() )
                    nm = scs.getNameEnglish();

                else
                    nm = scs.getName();

                for( String s : BaseTmLdrReportTemplate.commitmentNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add(scs );
                }
            }

            else if( tmLdrCompetencyTypeId == TmLdrCompetencyType.MOTIVATION.getTmLdrCompetencyTypeId() )
            {
                if( scs.getSimCompetencyClass().getSimCompetencyClassId() != SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId() )
                    continue;

                if( scs.getNameEnglish()!= null && !scs.getNameEnglish().isEmpty() )
                    nm = scs.getNameEnglish();

                else
                    nm = scs.getName();

                for( String s : BaseTmLdrReportTemplate.motivationNames )
                {
                    if( s!=null && !s.isEmpty() && s.equalsIgnoreCase( nm ) )
                        out.add(scs );
                }

            }
        }

        // TESTING ONLY!
        if( TESTMODE && out.isEmpty() )
        {
            out = scsl;

            if( out.size()> 2 )
                out = out.subList( 0 , 2 );
        }
        // Collections.sort( out );

        return out;

    }
    
    public static TestEventScore getGroupTestEventScore( TmLdrCompetencyType tmLdrCompetencyType, List<TestEventScore> tesl ) 
    {
        if( tesl == null || tmLdrCompetencyType == null )
            return null;
        
        for( TestEventScore tes : tesl )
        {
            if( tes.getTestEventScoreTypeId() != TestEventScoreType.COMPETENCYGROUP.getTestEventScoreTypeId() )
                continue;
            
            if( tes.getIntParam1() == SimCompetencyGroupType.CUSTOM.getSimCompetencyGroupTypeId() + tmLdrCompetencyType.getTmLdrCompetencyTypeId() )
                return tes;
        }
        
        return null;
    }


}
