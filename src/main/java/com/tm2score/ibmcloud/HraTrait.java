/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2score.ibmcloud;

import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.format.FormatCompetency;
import com.tm2score.format.ScoreFormatUtils;
import com.tm2score.global.Constants;
import com.tm2score.global.I18nUtils;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.score.TextAndTitle;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker
 */
public class HraTrait implements Serializable, Comparable<HraTrait>
{
    String name;
    int hraTraitTypeId;    
    float hraScore;
    float score;
    float confidence;
    Locale locale;
    
    List<SentinoTrait> sentinoTraitList;

    public HraTrait( TextAndTitle tt ) throws Exception
    {
        if( tt==null  )
            throw new Exception( "HraTrait() TextAndTitle is null" );
        
        if( tt==null || tt.getText()==null )
            throw new Exception( "HraTrait() TextAndTitle invalid: text is null.");
        
        if( !tt.getText().startsWith("HraTrait;") )
            throw new Exception( "HraTrait() TextAndTitle invalid: text is invalid: " + tt.getText() );

        String[] vals=tt.getText().split(";" );
        if( vals.length<5 )
            throw new Exception( "HraTrait() Packed string appears invalid, text for trait has too few tokens. text=" + tt.getText()  );
        
        this.name=tt.getTitle();
        this.hraTraitTypeId=Integer.parseInt(vals[1]);
        this.hraScore = Float.parseFloat(vals[2]);
        this.score = Float.parseFloat(vals[3]);
        this.confidence = Float.parseFloat(vals[4]);
    }

    
    public HraTrait( String nameLoc, int hraTraitTypeId, float hraScore, float score, float confidence )
    {
        this.name=nameLoc;
        this.hraTraitTypeId=hraTraitTypeId;
        this.hraScore = hraScore;
        this.score = score;
        this.confidence = confidence;
    }
        
    public HraTrait( int hraTraitTypeId )
    {
        this.hraTraitTypeId=hraTraitTypeId;
    }

    public HraTrait( int hraTraitTypeId, List<SentinoTrait> sentinoTraitList )
    {
        this.hraTraitTypeId=hraTraitTypeId;
        this.sentinoTraitList = sentinoTraitList;
    }

    @Override
    public int compareTo(HraTrait o) 
    {
        return getName().compareTo(o.getName());
    }
    
    
    
    
    public void calculate()
    {
        score=0;
        confidence=0;
        hraScore=0;
        
        // LogService.logIt( "HRATrait.calculate() " + this.getName() + ", sentinoTraitList=" + (sentinoTraitList==null ? "null" : sentinoTraitList.size() ) );
        
        if( sentinoTraitList==null || sentinoTraitList.isEmpty() )
            return;
        
        int count = 0;
        
        for( SentinoTrait st : sentinoTraitList )
        {
            //if( st.getConfidence()>=SentinoUtils.MIN_SENTINO_TRAIT_CONFIDENCE )
            //{   
                count++;
                confidence+=st.getConfidence();
                score += st.getQuantile(); 
            //}
        }
        
        // convert to averages
        if( count>0 )
        {
            confidence = confidence/((float)count);
            score = score/((float)count);
            hraScore = SentinoUtils.convertSentinoScoreToHraScore( score );
        }

        // LogService.logIt( "HRATrait.calculate() " + this.getName() + ", count=" + count + ", hraScore=" + hraScore + ", score=" + score + ", confidence=" + confidence  );
        
    }

    public float getHraScore() 
    {
        return hraScore;
    }
    
    public String getHraScoreStr()
    {
        if( locale==null )
            locale=Locale.US;
        
        return I18nUtils.getFormattedNumber(locale, hraScore, 0);        
    }

    public String getNameXhtml()
    {
        return StringUtils.replaceStandardEntities(  getName() );
    }

    
    public String getDescripStr()
    {
        if( locale==null )
            locale=Locale.US;
        
        return getHraTraitType().getDescription(locale);
    }

    public String getDescripStrXhtml()
    {
        return StringUtils.replaceStandardEntities(  getDescripStr() );
    }

    
    public String getScoreTextStr()
    {
        if( locale==null )
            locale=Locale.US;
        
        return getHraTraitType().getScoreText(locale, hraScore);
    }
    
    public String getScoreTextStrXhtml()
    {
        return StringUtils.replaceStandardEntities(  getScoreTextStr() );
    }
    
    
    public String getColorGraphUrl()
    {            
        try
        {
            StringBuilder imgUrl = new StringBuilder();

            int totalPix = Constants.CT2_COLORGRAPHWID_EML;
            
            int ptrPos = 0;

            if( hraScore<=0 )
                ptrPos = 0 - Math.round(FormatCompetency.MARKER_LEFT_ADJ);

            else if( hraScore>=100 )
                ptrPos = totalPix - Math.round(FormatCompetency.MARKER_LEFT_ADJ);

            else
            {
                int specAdj = hraScore<=0 ? 1 : 0;
                ptrPos = Math.round(((float)totalPix)*(hraScore/100f)) - Math.round(FormatCompetency.MARKER_LEFT_ADJ) + specAdj;
            }
                
            List<ScoreCategoryRange> scrLst = ScoreFormatUtils.getOverallScoreCatInfoList(false,ScoreColorSchemeType.FIVECOLOR);

            // List<ScoreCategoryRange> scrLst = CT2MetaScoreGraphicCellEvent.getOverallScoreCatInfoList(true);            
            ScoreCategoryRange scr;
            int rangePix;
            for( int i=0;i<scrLst.size() && i<5; i++ )
            {
                scr = scrLst.get(i);
                
                rangePix = scr.getAdjustedRangePix( ScoreFormatType.NUMERIC_0_TO_100);
                
                if( imgUrl.length()>0 )
                    imgUrl.append( "," );

                // imgUrl.append( Constants.SCORE_GRAPH_COLS[i] + Math.round( 0.2f*totalPix ) );
                imgUrl.append( Constants.SCORE_GRAPH_COLS[i] + rangePix );
            }

            return RuntimeConstants.getStringValue("baseprotocol") +  "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/ct2scorechart/" + StringUtils.alphaCharsOnly(name) + ".png?ss=" + imgUrl.toString() + "&tw=" + totalPix + "&p=" + ptrPos; //  + "&cs=" + Constants.IBMINSIGHT_SCORE_GRAPH_COLORS;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "InsightReportTrait.getColorGraphUrl() name=" + name + ", score=" + score + ", hraScore=" + hraScore );
        }

        // LogService.logIt( "CT2HtmlScoreFormatter.getColorGraph() size=" + sb.length() ); // + "\n" + sb.toString() );

        return null;
        // return "<tr " + style + "><td style=\"width:20px\"><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + " colspan=\"2\">" + value + "</td>" + "<td colspan=\"1\"><img style=\"margin-left:20px\" src=\"" + sct.getImageUrl( baseImageUrl, useRatingAndColors()) + "\"/></td></tr>\n";
    }
    
    
    
    public boolean isValid()
    {
        return this.confidence>=SentinoUtils.MIN_SENTINO_TRAIT_CONFIDENCE;
    }
    
    
    
    /**
     * Format is 
     *    title = Name for users
     *    text = HraTrait;HraTraitTypeId;hraScore;score;confidence
     * 
     * @param locale
     */
    public TextAndTitle getScoreTextAndTitle( Locale locale )
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append( "HraTrait;" +  hraTraitTypeId + ";" + SentinoUtils.roundScore( hraScore ) + ";" + score + ";" + confidence );
        
        String title = getHraTraitType().getName(locale);
                
        return new TextAndTitle( sb.toString(), title );
    }
    
    

    
    
    
    public String getStringSummary()
    {
        return hraTraitTypeId + "; hraScore=" + hraScore + "; Avg Sentino Score=" + score + "; Avg Sentino Confidence=" + confidence;
    }
    
    public HraTraitType getHraTraitType()
    {
        return HraTraitType.getValue( this.hraTraitTypeId );
    }
    

    public float getScore() {
        return score;
    }

    public float getConfidence() {
        return confidence;
    }

    public String getName() 
    {
        if( locale==null )
            locale=Locale.US;
        
        return getHraTraitType().getName(locale);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    
}
