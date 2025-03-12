/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp.itss;

import com.tm2score.custom.hraph.bsp.*;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TESScoreComparator;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyVisibilityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Mike
 */
public class BaseItssHtmlScoreFormatter extends BspHtmlScoreFormatter implements ScoreFormatter
{

    public Itss itss;

    public BaseItssHtmlScoreFormatter()
    {
        super();
    }

    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            LogService.logIt( "ItssHtmlScoreFormatter.getEmailContent() STARTING" );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection( tog, includeTop, topNoteHtml, "g.CoreTestAdminScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]).booleanValue();
                sb.append( getRowSpacer( rowStyle0 ) );
            }

            // not batt.
            if( getTestEvent()!=null )
            {
                if( getTestKey().getTestEventList().size() >1 )
                {
                    String style = tog ? rowStyle1 : rowStyle2;
                    String value = getTestEvent().getProduct().getName();
                    String label = lmsg(  "g.TestC" , null );
                    if( value != null && value.length() > 0 )
                        sb.append( getRowTitle( style, label + " " + value, null, null, null ) );
                }

                if( isIncludeOverall()  )
                {
                    tog = !tog;
                    out = getStandardOverallScoreSection( tog );
                    sb.append( (String) out[0] );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Report Section
                out = getStandardReportSection(tog, false, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Foundation Competency Section
                out = getItssCompetenciesSection("g.FoundationComps", itss.getCompetencies(), null);
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                
                // RIASEC Competency Section
                out = getItssCompetenciesSection("g.CareerInterest", itss.getRiasecCompetencies(), new TESScoreComparator(true));
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }
                
                // Alt Scores Section
                out = getItssAltScoresSection( "g.RoleFitScores" );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                // Writing Sample Section
                out = getStandardWritingSampleSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                
                // Identity Image Capture Section
                out = getStandardImageCaptureSection(tog, null, includeTop );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                

                // Notes section
                // Has Uploaded Files Section
                out = this.getNotesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]).booleanValue();
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "ItssHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }
    
    
    protected Object[] getItssAltScoresSection( String key )
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        // boolean includeIt = true;

        boolean tog = true;

        //if( 1==1 )
        //
            // SimCompetencyClass scc;

        List<TestEventScore> tesList =  te.getTestEventScoreList( TestEventScoreType.ALT_OVERALL.getTestEventScoreTypeId() ); //  getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );
        
        Collections.sort( tesList, new TESScoreComparator() );
        Collections.reverse(tesList);
        
        if( tesList.size() > 0 )
        {
            String style = rowStyle1;

            // String key = "g.FoundationComps";

            // sb.append( getRowTitle( rowStyleHdr, lmsg( key , null ), isIncludeSubcategoryNumeric() ? lmsg( "g.Score" ) : null, isIncludeSubcategoryNorms() ? lmsg( "g.Percentile" ) : null , this.isIncludeSubcategoryCategory() ? lmsg(this.useRatingAndColors() ? "g.Rating" : "g.MatchJob" ) : null ) );
            sb.append( getRowTitle( rowStyleHdr, lmsg( key , null ), lmsg( "g.Score" , null ), lmsg( "g.Rank" , null ), null ) );

            String label;
            String value,value2;
            
            int count = 0;
            
            for( TestEventScore tes : tesList )
            {
                count++;
                label = tes.getName();
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;

                value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), 2 );

                value2 = Integer.toString(count);

                // LogService.logIt( "ItssHtmlScoreFormatter.getItssCompetenciesSection() tes.name=" + tes.getName() +", tes.getIncludeNumericScoreInResults()=" + tes.getIncludeNumericScoreInResults() + ", value=" + value + ", score=" + tes.getScore() + ", hasColor=" + tes.getScoreCategoryType().hasColor() + ", showColorRating=" + showColorRating + ", isIncludeSubcategoryCategory()=" + isIncludeSubcategoryCategory() );
                sb.append( getRow(style, label, value, value2, false ) );                                
            }
        }
        //}


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    
    
    protected Object[] getItssCompetenciesSection( String key, String[] nameList, Comparator comparator)
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        // boolean includeIt = true;

        boolean tog = true;

        //if( 1==1 )
        //
            // SimCompetencyClass scc;

        List<TestEventScore> tesList =  getSpecTestEventScoreList( nameList ); //  getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

        List<TestEventScore> tesList2 = new ArrayList<>();

        for( TestEventScore tes : tesList )
        {
            // Skip competencies or task-competencies that were not automatically scored.
            if( tes.getScore()<0 )
                continue;

            // if supposed to hide
            if( !SimCompetencyVisibilityType.getValue( tes.getHide() ).getShowInReports() )
                continue;

            if( hasProfile() )
                tes.setProfileBoundaries(getProfileEntryData(tes.getName(), tes.getNameEnglish() ) );

            // scc = SimCompetencyClass.getValue(tes.getSimCompetencyClassId());

            // if( typeId == 0 && scc.getIsDirectCompetency() || scc.getIsAggregate() )
           tesList2.add( tes );
        }
        
        if( comparator != null )
            Collections.sort( tesList2, comparator );
        // Collections.sort( tesList2, new TESNameComparator() );

        
        if( tesList2.size() > 0 )
        {
            String style = rowStyle1;

            // String key = "g.FoundationComps";

            sb.append( getRowTitle( rowStyleHdr, lmsg( key , null ), isIncludeSubcategoryNumeric() ? lmsg( "g.Score" ) : null, isIncludeSubcategoryNorms() ? lmsg( "g.Percentile" ) : null , this.isIncludeSubcategoryCategory() ? lmsg(this.useRatingAndColors() ? "g.Rating" : "g.MatchJob" ) : null ) );

            String label;
            String value,value2;
            boolean showColorRating=true;

            for( TestEventScore tes : tesList2 )
            {
                showColorRating=true;
                
                label = tes.getName();
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;

                if( tes.getIncludeNumericScoreInResults() )
                   value =  I18nUtils.getFormattedNumber(locale, tes.getScore(), tes.getScoreFormatType().getScorePrecisionDigits() );

                else
                    value =  "-";

                value2 = "";

                if( !isIncludeSubcategoryNumeric() )
                    value = "";

                //if( !tes.getIncludeNumericScoreInResults() )
                //    value = "-";

                // showColorRating = isIncludeSubcategoryCategory() && tes.getScoreCategoryType().hasColor();

                if( !tes.getIncludeNumericScoreInResults() )
                    showColorRating= false;

                LogService.logIt( "ItssHtmlScoreFormatter.getItssCompetenciesSection() tes.name=" + tes.getName() +", tes.getIncludeNumericScoreInResults()=" + tes.getIncludeNumericScoreInResults() + ", value=" + value + ", score=" + tes.getScore() + ", hasColor=" + tes.getScoreCategoryType().hasColor() + ", showColorRating=" + showColorRating + ", isIncludeSubcategoryCategory()=" + isIncludeSubcategoryCategory() );

                if( showColorRating  )
                    sb.append(getRowWithColorGraphAndCategoryStars(style, label, value, false, tes.getScoreCategoryType(), tes, showColorRating, false, false, false) );

                else
                    sb.append( getRow(style, label, value, value2, false ) );
                                
            }
        }
        //}


        out[0] = sb.toString();
        out[1] = tog;
        return out;
    }
    
    
    public List<TestEventScore> getSpecTestEventScoreList(  String[] nameList )
    {
        List<TestEventScore> out = new ArrayList<>();
        
        TestEventScore tes;
        
        for( String name : nameList )
        {
            tes = getTestEventScore( name );
            
            if( tes != null )
                out.add( tes );
        }
        
        if( out.isEmpty() )
        {
            out.addAll( te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) );
        }
        
        // Collections.sort( out, new TESNameComparator() );
                
        return out;
        
    }
    
    private TestEventScore getTestEventScore( String name )
    {
        if( name==null || name.isEmpty() )
            return null;
        
        for( TestEventScore tes : te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) )
        {
            if( tes.getName()!=null && tes.getName().equals(name))
                return tes;
            if( tes.getNameEnglish()!=null && tes.getNameEnglish().equals(name))
                return tes;
        }
        
        return null;
    }
    
    

}
