/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.hraph.bsp;

import com.tm2score.custom.coretest2.*;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.*;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyVisibilityType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Mike
 */
public class BspHtmlScoreFormatter extends CT2HtmlScoreFormatter implements ScoreFormatter
{



    public BspHtmlScoreFormatter()
    {
        super();
    }

    @Override
    public String getEmailContent( boolean tog, boolean includeTop, String topNoteHtml ) throws Exception
    {
        try
        {
            // LogService.logIt( "BspHtmlScoreFormatter.getEmailContent() STARTING" );

            StringBuilder sb = new StringBuilder();

            // Header Section
            Object[] out = getStandardHeaderSection(tog, includeTop, false, topNoteHtml, "g.CoreTestAdminScoringCompleteMsg", null );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = (Boolean) out[1];
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
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Report Section
                out = getStandardReportSection(tog, false, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Competency Section
                out = getBspCompetenciesSection();
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }

                // Writing Sample Section
                out = getStandardWritingSampleSection(tog, null );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


                // Notes section
                // Has Uploaded Files Section
                out = this.getNotesSection( tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = (Boolean) out[1];
                    sb.append( getRowSpacer( rowStyle0 ) );
                }


            }  // testEvent

            return sb.toString();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "BspHtmlScoreFormatter.getEmailContent() " );

            throw new STException( e );
        }
   }


    
    protected Object[] getNotesSection( boolean tog ) throws Exception
    {
        return getStandardHRANotesSection(tog, null );
    }
    
    
    protected Object[] getBspCompetenciesSection()
    {
        Object[] out = new Object[2];

        StringBuilder sb = new StringBuilder();

        // boolean includeIt = true;

        boolean tog = true;

        //if( 1==1 )
        //
            // SimCompetencyClass scc;

        List<TestEventScore> tesList =  this.getTestEventScoreListToShow(); //  getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() );

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
        
        // Collections.sort( tesList2, new TESNameComparator() );

        

        if( tesList2.size() > 0 )
        {
            String style = rowStyle1;

            String key = "g.CoreCompetencies";

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

                LogService.logIt( "BspHtmlScoreFormatter.getBspComptencySection() tes.name=" + tes.getName() +", tes.getIncludeNumericScoreInResults()=" + tes.getIncludeNumericScoreInResults() + ", value=" + value + ", score=" + tes.getScore() + ", hasColor=" + tes.getScoreCategoryType().hasColor() + ", showColorRating=" + showColorRating + ", isIncludeSubcategoryCategory()=" + isIncludeSubcategoryCategory() );

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
    
    
    private List<TestEventScore> getTestEventScoreListToShow()
    {
        List<TestEventScore> out = new ArrayList<>();
        
        TestEventScore tes;
        
        // String name;
        
        for( BspCompetencyType bct : BspCompetencyType.getListDevRpt() )
        {
            // name = bct.getName();
            
            tes = getTestEventScore( bct.getName() );
            
            if( tes==null )
                tes = getTestEventScore( bct.getHraName() );
            
            if( tes != null )
                out.add( tes );
        }
        
        if( out.isEmpty() )
        {
            out.addAll( te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId() ) );
        }
        
        Collections.sort( out );  
        
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
