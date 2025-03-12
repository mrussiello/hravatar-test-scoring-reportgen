/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.profile;

import com.itextpdf.text.BaseColor;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.profile.Profile;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.service.LogService;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public class ProfileUtils {

    public static Profile getLiveProfileForProductIdAndOrgId( long testEventId, int productId, int orgId, int profileUsageTypeId )
    {
        try
        {
            ProfileFacade pf = ProfileFacade.getInstance();
            
            List<Profile> pl = pf.getProfileListForProductIdAndOrgIdAndProfileUsageType(productId, orgId, profileUsageTypeId);
                 
            for( Profile p : pl )
            {
                if( p.getProfileStatusType().isActive() )
                {
                    pf.loadProfile(p);
                    return p;
                }                
            }
            
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ProfileUtils.getLiveProfileForProductIdAndOrgId()  testEventId=" + testEventId + ", productId=" + productId + ", orgId=" + orgId );
        }

        return null;
    }
    
    
    /**
     * scoreCategoryRangeStr is an agg string of ranges and scores. 
     * 
     * Examples - 
     * 
     * Simple 5 colors: r18,ry28,y48,yg75,g85

     * 5 color + Red Cliff: r18,ry28,y48,yg75,g85,r95

     * 5 color + Yellow Cliff: r18,ry28,y48,yg75,g85,y95
      
     * Normal 5 color: r18,y28,g48,y75,r85

     * Will be used in place of score category ranges in Sim Competency.
     */
    public static List[] parseColorStr( String scoreCategoryRangeStr ) throws Exception
    {

        List[] out = new List[] {new ArrayList<>(),new ArrayList<>()} ;

        try
        {
            String t = scoreCategoryRangeStr;

            if( t == null )
                t = "";

            else
                t = t.trim();

            if( t.isEmpty() )
                return out;

            // LogService.logIt( "CT2GraphicsServlet.doGet() sections=" + t );

            String[] ts = t.split(",");

            // size in pixels
            float minScore;

            // 1=red, 2=red yellow, 3=yellow, 4=yellow green, 5=green, 6=white
            int color;

            List<Integer> colors = out[0];
            List<Float> minScores = out[1];

            for( String c : ts )
            {
                if( c==null || c.trim().isEmpty() )
                    continue;

                color = 0;
                minScore=0;

                c = c.trim().toLowerCase();

                if( c.startsWith("ry") )
                {
                    color = 2;
                    minScore = Float.parseFloat( c.substring(2, c.length() ) );
                }

                else if( c.startsWith("r") )
                {
                    color = 1;
                    minScore = Float.parseFloat( c.substring(1, c.length() ) );
                }

                else if( c.startsWith("yg") )
                {
                    color = 4;
                    minScore = Float.parseFloat( c.substring(2, c.length() ) );
                }

                else if( c.startsWith("y") )
                {
                    color = 3;
                    minScore = Float.parseFloat( c.substring(1, c.length() ) );
                }

                else if( c.startsWith("g") )
                {
                    color = 5;
                    minScore = Float.parseFloat( c.substring(1, c.length() ) );
                }
                else if( c.startsWith("w") )
                {
                    color = 6;
                    minScore = Float.parseFloat( c.substring(1, c.length() ) );
                }

                else
                    continue;

                if( color>0 && minScore>0 )
                {
                    colors.add( color );
                    minScores.add(minScore );
                }
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProfileUtils.parseColorStr() scoreCategoryRangeStr=" + scoreCategoryRangeStr );

            throw e;
        }

        return out;

    }
    
    
    /**
     * Returns array of RGB codes.  
     * 
     * Input string format rRRGGBB,ryRRG,yRRGGBB,ygRRGGBB,gRRGGBB

    *  data[0] = Red color or null   RRGGBB
    *  data[1] = RedYellow or null
    *  data[2] = yellow or null
    *  data[3] = yellow-green or null
    *  data[4] = green or null
    *  data[5] = scoreWhite or null
    *  data[6] = scoreBlack or null
    * 
     * @param t
     * @return 
     */
    public static String[] parseBaseColorStr( String t )
    {
        String[] out = new String[7];

        try
        {
            if( t == null )
                t = "";

            else
                t = t.trim();

            if( t.isEmpty() )
                return out;

            String[] ts = t.split(",");

            for( String c : ts )
            {
                if( c==null || c.trim().isEmpty() )
                    continue;

                c = c.trim().toLowerCase();

                if( c.startsWith("ry") )
                    out[1] = c.substring(2, c.length() );

                else if( c.startsWith("r") )
                    out[0] = c.substring(1, c.length() );

                else if( c.startsWith("yg") )
                    out[3] = c.substring(2, c.length() );

                else if( c.startsWith("y") )
                    out[2] = c.substring(1, c.length() );

                else if( c.startsWith("g") )
                    out[4] = c.substring(1, c.length() );
                else if( c.startsWith("w") )
                    out[5] = c.substring(1, c.length() );
                else if( c.startsWith("b") )
                    out[6] = c.substring(1, c.length() );
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProfileUtils.parseBaseColorStr() str=" + t );

            throw e;
        }

        return out;        
    }
    

    /**
     * Input string format rRRGGBB,ryRRG,yRRGGBB,ygRRGGBB,gRRGGBB

    *  data[0] = Red color or null   RRGGBB
    *  data[1] = RedYellow or null
    *  data[2] = yellow or null
    *  data[3] = yellow-green or null
    *  data[4] = green or null
    *  data[5] = scoreWhite or null
    *  data[6] = scoreBlack or null
    * 
     * 
     */
    public static BaseColor[] parseBaseColorStrAsBaseColors( String t )
    {
        String[] tt = parseBaseColorStr( t );

        BaseColor[] out = new BaseColor[7];
        
        Color col;
        int red, green, blue;
        
        for( int i=0;i<7 && i<tt.length; i++ )
        {
            if( tt[i] != null && !tt[i].isEmpty() )
            {
                col = Color.decode( "#" + tt[i] );
                red = col.getRed();
                green = col.getGreen();
                blue = col.getBlue();
                
                // LogService.logIt( "ProfileUtils.parseBaseColorStrAsBaseColors() ClrsStr=" + t + ", tt[i]=" + tt[i] + ", red=" + red + ", green=" + green + ", blue=" + blue );
                
                out[i] = new BaseColor( red, green, blue );
            }            
        }        

        return out;
        
    }
    

    
    public static void applyProfileToSimXmlObj( TestEvent te ) throws Exception
    {
        if( te==null || te.getProfile()==null ||te.getSimXmlObj()== null )
            return;
        
        if( !te.getProfile().getProfileUsageType().getIsReportRanges() )
            return;
        
        if( te.getProfile().getStrParam2() == null || te.getProfile().getStrParam2().isEmpty() )
            return;
        
        int scoreColorSchemeTypeId = te.getSimXmlObj().getScorecolorscheme();
        
        try
        {
            List[] data = ProfileUtils.parseColorStr( te.getProfile().getStrParam2() );

            // int color;
            float score;

            String vals = "";

            List<Integer> colors = (List<Integer>) data[0];
            List<Float> scores = (List<Float>) data[1];

            if( scoreColorSchemeTypeId== ScoreColorSchemeType.THREECOLOR.getScoreColorSchemeTypeId() && ( colors.size()!=2 ) )
            {
                LogService.logIt( "ProfileUtils.applyProfileToSimXmlObj() Wrong number of entries in ProfileEntry.scoreCategoryRangeStr.  Expecting 2, found " + colors.size() );
                return;
            }

            if( scoreColorSchemeTypeId== ScoreColorSchemeType.FIVECOLOR.getScoreColorSchemeTypeId() &&  ( colors.size()!=4 ) )
            {
                LogService.logIt( "ProfileUtils.applyProfileToSimXmlObj()  Wrong number of entries in ProfileEntry.scoreCategoryRangeStr.  Expecting 4, found " + colors.size() );
                return;
            }

            if( scoreColorSchemeTypeId== ScoreColorSchemeType.SEVENCOLOR.getScoreColorSchemeTypeId() &&  ( colors.size()!=6 ) )
            {
                LogService.logIt( "ProfileUtils.applyProfileToSimXmlObj()  Wrong number of entries in ProfileEntry.scoreCategoryRangeStr.  Expecting 6, found " + colors.size() );
                return;
            }

            if( scoreColorSchemeTypeId== ScoreColorSchemeType.FIVECOLOR.getScoreColorSchemeTypeId() || scoreColorSchemeTypeId== ScoreColorSchemeType.SEVENCOLOR.getScoreColorSchemeTypeId() )
            {
                boolean seven = scoreColorSchemeTypeId== ScoreColorSchemeType.SEVENCOLOR.getScoreColorSchemeTypeId();
                
                for( int i=0;i<colors.size(); i++ )
                {
                    // color = colors.get(i);
                    score = scores.get(i);

                    
                    // Red
                    if( i == 0 )
                    {
                        if( seven )
                            te.getSimXmlObj().setRedmin(score);
                        else
                            te.getSimXmlObj().setRedyellowmin(score);
                        // this.simCompetencyObj.setRedyellowmin( score );
                        vals += (seven ? "R Min=" : "RY Min=") + score + ",";
                    }

                    // Red-Yellow
                    else if( i == 1 )
                    {
                        if( seven )
                            te.getSimXmlObj().setRedyellowmin(score);
                        else
                            te.getSimXmlObj().setYellowmin(score);
                        vals += (seven ? "RY Min=" : "Y Min=") + score + ",";
                    }

                    // Yellow
                    else if( i == 2 )
                    {
                        if( seven )
                            te.getSimXmlObj().setYellowmin(score);
                        else
                            te.getSimXmlObj().setYellowgreenmin(score);
                        vals += (seven ? "Y Min=" : "YG Min=") + score + ",";
                    }

                    // Yellow-Green
                    else if( i == 3 )
                    {
                        if( seven )
                            te.getSimXmlObj().setYellowgreenmin(score);
                        else
                            te.getSimXmlObj().setGreenmin(score);
                        te.getSimXmlObj().setGreenmin( score );
                        vals += (seven ? "YG Min=" : "G Min=") + score + ",";
                    }

                    else if(seven && i == 4 )
                    {
                        te.getSimXmlObj().setGreenmin(score);
                        vals += "G Min=" + score + ",";
                    }
                    else if(seven && i == 5 )
                    {
                        te.getSimXmlObj().setWhitemin(score);
                        vals += "W Min=" + score + ",";
                    }
                }

                // LogService.logIt( "ProfileUtils.applyProfileToSimXmlObj() Vals=" + vals );
            }

            else if( scoreColorSchemeTypeId== ScoreColorSchemeType.THREECOLOR.getScoreColorSchemeTypeId()  )
            {
                for( int i=0;i<colors.size(); i++ )
                {
                    // Red
                    if( i == 0 )
                        te.getSimXmlObj().setYellowmin( scores.get(i));

                    // Yellow
                    else if( i == 1 )
                        te.getSimXmlObj().setGreenmin( scores.get(i));
                }
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ProfileUtils.applyProfileToSimXmlObj() " );
            
            throw e;
        }
    }
    
    

}
