/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.custom.disc;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.event.EventFacade;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.service.LogService;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.style.PieStyler.LabelType;
import org.knowm.xchart.style.Styler.ChartTheme;

/**
 *
 * @author miker_000
 */
public class DiscReportUtils {

    public static final String[] DISC_COMPETENCY_NAMES = new String[]{"Dominance", "Influence", "Steadiness", "Compliance"};
    public static final String[] DISC_COMPETENCY_STUBS = new String[]{"d", "i", "s", "c"};

    private final String bundleName;
    private Properties customProperties;


    public DiscReportUtils( String bundleName)
    {
        this.bundleName=bundleName;
    }


    /*
    public static BufferedImage getDiscPieGraphImage( Map<String,Object[]> scoreValMap, int scrDigits, int wid, int hgt )
    {
        try
        {
            LogService.logIt( "DiscReportUtils.getDiscPieGraphImage() wid=" + wid + ", hgt=" + hgt  );

            DefaultPieDataset dataset = new DefaultPieDataset();
            float scrAdj;
            String name;
            for( String s : scoreValMap.keySet() )
            {
                name = (String) scoreValMap.get(s)[0];
                scrAdj = (Float) scoreValMap.get(s)[1];

                BigDecimal bd = new BigDecimal(Float.toString(scrAdj));
                bd = bd.setScale(scrDigits, RoundingMode.DOWN); // Truncate to 2 decimal places
                float trimmed = bd.floatValue(); // trimmed = 3.14
                dataset.setValue(name + "(" + trimmed + ")", trimmed);
            }

            JFreeChart chart = ChartFactory.createPieChart(null,   // chart title
                                                            dataset,          // data
                                                            false,       // include legend
                                                            false,
                                                            false);
            
            chart.getPlot().setBackgroundPaint(java.awt.Color.WHITE);
            chart.getPlot().setOutlinePaint( java.awt.Color.WHITE );
            
            return chart.createBufferedImage(wid, hgt);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DiscReportUtils.getDiscPieGraphImage() " );
            throw e;
        }
    }
    */

    public static BufferedImage getDiscPieGraphImage2( Map<String,Object[]> scoreValMap, int scrDigits, int wid, int hgt ) throws Exception
    {
        try
        {
            LogService.logIt( "DiscReportUtils.getDiscPieGraphImage2() wid=" + wid + ", hgt=" + hgt  );

            //Color[] sliceColors = new Color[] 
            //{
            //    new Color(3, 150, 255),    // 0077cc
            //    new Color(232, 246, 255),   // e8f6ff
            //    new Color(73, 178, 255),   // #398ac4
            //    new Color(150, 211, 255)   // 96d3ff
            //};

            Color[] sliceColors = new Color[] 
            {
                new Color(238, 238, 238),   // eeeeee
                new Color(90, 154, 203),   // #A9ACB
                new Color(248, 148, 29),   // f8941d
                new Color(0, 119, 204)    // 0077cc

            };
            
            PieChart chart = new PieChartBuilder().width(wid).height(hgt).theme(ChartTheme.GGPlot2).build();

            // Customize Chart
            chart.getStyler().setSeriesColors(sliceColors);
            chart.getStyler().setChartPadding(0);
            chart.getStyler().setCircular(true);
            chart.getStyler().setLegendVisible(true);
            Font font = new Font("Arial", Font.PLAIN, 10);
            chart.getStyler().setLegendFont(font);            
            Font font2 = new Font("Arial", Font.PLAIN, 8);
            chart.getStyler().setLabelsFont(font2);
            chart.getStyler().setLabelsFontColor( Color.BLACK );
            //chart.getStyler().setLegendLayout(LegendLayout.Horizontal );
            //chart.getStyler().setLegendPosition( LegendPosition.OutsideS);
            // chart.getStyler().setLegendPadding(30);
            chart.getStyler().setLabelsDistance(0.5);  // 0.98f
            chart.getStyler().setChartBackgroundColor(Color.white);
            chart.getStyler().setPlotBackgroundColor( Color.white );
            // chart.getStyler().setPlotBorderVisible(true);
            // chart.getStyler().setPlotBorderColor(Color.red);
            // chart.getStyler()
            chart.getStyler().setLabelType( LabelType.Name);
            chart.getStyler().setLabelsVisible(true);
            chart.getStyler().setStartAngleInDegrees(0);
            chart.getStyler().setForceAllLabelsVisible(true);
            chart.getStyler().setAntiAlias( true );
            chart.getStyler().setAnnotationTextPanelPadding( 0 );
            
            // chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
            // chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
    
            float scrAdj;
            String name;
            // String[] cols = new String[]{"d", "i", "s", "c" };
            String[] cols = new String[]{"c", "s", "i", "d" };
            
            float totalVal = 0;
            for( String s : cols )
            {
                scrAdj = (Float) scoreValMap.get(s)[1];
                totalVal += scrAdj;                
                LogService.logIt( "DiscReportUtils.getDiscPieGraphImage2() letter=" + s + ", scrAdj=" + scrAdj );
            }
            
            // LogService.logIt( "DiscReportUtils.getDiscPieGraphImage2() totalVal=" + totalVal );
            String ltr;
            
            int smallSliceCount = 0;            
            for( int i=0;i<4;i++ )
            {
                ltr = cols[i];
                scrAdj = (Float) scoreValMap.get(ltr)[1];
                if( totalVal>0 && 100f*scrAdj/totalVal<=20f )
                    smallSliceCount++;
            }
            
            // LogService.logIt( "DiscReportUtils.getDiscPieGraphImage2() smallSliceCount=" + smallSliceCount );
            if( smallSliceCount>1 )
            {
                List<Object[]> sortedCols = new ArrayList<>();
                for( int i=0;i<4;i++ )
                {
                    ltr = cols[i];
                    scrAdj = (Float) scoreValMap.get(ltr)[1];
                    sortedCols.add(new Object[]{ltr,scrAdj});
                }
                Collections.sort( sortedCols, new DiscScoreNameValueComparator() );
                Collections.reverse(sortedCols);
                
                // move smallest to second largest
                cols = new String[]{ (String)sortedCols.get(0)[0],(String)sortedCols.get(3)[0],(String)sortedCols.get(1)[0],(String)sortedCols.get(2)[0]}; 
            }
            
            String newSort = "";            
            for( String s : cols )
            {
                newSort += s + ",";                
            }
            // LogService.logIt( "DiscReportUtils.getDiscPieGraphImage2() newSort=" + newSort);
                        
            for( String s : cols )
            {
                name = (String) scoreValMap.get(s)[0];
                scrAdj = (Float) scoreValMap.get(s)[1];
                BigDecimal bd = new BigDecimal(Float.toString(scrAdj));
                bd = bd.setScale(scrDigits, RoundingMode.DOWN); // Truncate to 2 decimal places
                float trimmed = bd.floatValue(); // trimmed = 3.14
                // dataset.setValue(name + "(" + trimmed + ")", trimmed);
                chart.addSeries(name + " (" + trimmed + ")", trimmed);
            }
            
            Path tempFile = Files.createTempFile("discpie-", ".png" );
            Path pathFilename = tempFile.toAbsolutePath();
            LogService.logIt( "DiscReportUtils.getDiscPieGraphImage2() newSort=" + newSort + ", smallSliceCount=" + smallSliceCount + ", writing NIO tempFile to " +  pathFilename.toString());
            
            //File tempFile2 = File.createTempFile("discpie-", ".png");
            //tempFile2.deleteOnExit();
            
            //String filenamePath = tempFile2.getAbsolutePath();
            //LogService.logIt( "DiscReportUtils.getDiscPieGraphImage2() writing tempFile2 to " +  filenamePath );
            
            

            // BitmapEncoder.saveBitmap(chart, "/work/tm2score5/log/sample-chart", BitmapFormat.PNG);
            // BitmapEncoder.saveBitmapWithDPI(chart, "/work/tm2score5/log/sample-chart-300", BitmapFormat.PNG, 300);
            // BitmapEncoder.saveBitmapWithDPI(chart, filenamePath, BitmapFormat.PNG, 300);
            BitmapEncoder.saveBitmapWithDPI(chart, pathFilename.toString(), BitmapFormat.PNG, 300);
            
            File imageFile = new File(pathFilename.toString()); 
            
            // Read the image into a BufferedImage object
            BufferedImage bi = ImageIO.read(imageFile);    
            
            imageFile.delete();
            
            return bi;
            
            
            // return BitmapEncoder.getBufferedImage(chart);
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DiscReportUtils.getDiscPieGraphImage2() " );
            throw e;
        }
    }




    public static String getCompetencyStub( int[] topTraitIndexes )
    {
        if( topTraitIndexes[1]<0 )
            return getCompetencyStubLetter( topTraitIndexes[0] );
        
        return getCompetencyStubLetter( topTraitIndexes[0] ) + getCompetencyStubLetter( topTraitIndexes[1] );
    }


    public static String getCompetencyStubLetter( int index )
    {
        return DISC_COMPETENCY_STUBS[index];
    }

    /**
     * Returns
     *    data[0] = high value INDEX (0,1,2, or 3).      *
     *    data[1] = second highest value INDEX IF:
     *                the 2nd highest value is>=70 or
     *                the highest value is>=40 AND 2nd highest value is >=highest value minus 25
     *                otherwise -1.
     *
     * @param discScoreVals
     * @return
     */
    public static int[] getTopTraitIndexes( float[] discScoreVals )
    {
        int[] out = new int[2];
        out[1]=-1;
        float highVal=-1;
        int idx=0;

        if( discScoreVals==null || discScoreVals.length<4 )
            return out;

        for( int i=0; i<4; i++ )
        {
            if( discScoreVals[i]>highVal )
            {
                highVal=discScoreVals[i];
                idx=i;
            }
            
            else if( discScoreVals[i]==highVal )
            {
                int indexToUse = getTieBreakerIndex( i, idx );
                LogService.logIt( "DiscReeportUtils.getTopTraitIndexes() TOP value Tie Breaker: i=" + i + ", existing idx=" + idx +", tieBreaker index=" + indexToUse );
                idx = indexToUse;
            }
            
        }
        out[0]=idx;

        // get second highest value.
        int idx2=-1;
        float highVal2=-1;
        for( int i=0; i<4; i++ )
        {
            // already used
            if( i==idx )
                continue;

            if( discScoreVals[i]>highVal2 )
            {
                highVal2=discScoreVals[i];
                idx2=i;
            }

            else if( discScoreVals[i]==highVal2 )
            {
                int indexToUse = getTieBreakerIndex( i, idx2 );
                LogService.logIt( "DiscReportUtils.getTopTraitIndexes() Secondary value Tie Breaker: i=" + i + ", existing idx=" + idx2 +", tieBreaker index=" + indexToUse );
                idx2 = indexToUse;
            }
        }

        
        if( highVal>0 && ((highVal-highVal2)/highVal)<0.2f )
            out[1]=idx2;
        else
            out[1]=-1;

            /*
        if( highVal2>=75f )
            out[1]=idx2;
        else if( highVal>=40 && highVal2>=highVal-25 )
            out[1]=idx2;
        else
            out[1]=-1;
        */

        return out;
    }
    
    public static int getTieBreakerIndex( int idx1, int idx2 )
    {
        // same - should not happen
        if( idx1==idx2 )
            return idx1;
        
        // Dominance beats all
        if( idx1==0 || idx2==0 )
            return 0;

        // influence beats all except dominance.
        if( idx1==1 || idx2==1 )
            return 1;
        
        // at this point must have 1 steadiness and 1 compliance.
        
        // compliance always beats steadiness
        return 3;
        
    }

    public static float[] getDiscScoreVals( TestEvent te )
    {
        float[] out = new float[4];

        // TESTING ONLY
        if( 1==2 )
        {
            // out = new float[]{50,15,40,25};
            out = new float[]{35,50,50,5};
            return out;
        }

        if( te==null )
        {
            LogService.logIt( "DiscReportUtils.getDiscScoreVals() testEvent is NULL!" );
            return out;
        }

        if( te.getTestEventScoreList()==null )
        {
            try
            {
                te.setTestEventScoreList(EventFacade.getInstance().getTestEventScoresForTestEvent( te.getTestEventId(), true));
            }
            catch( Exception e )
            {
                LogService.logIt( e, "DiscReportUtils.getDiscScoreVals() testEventId=" + te.getTestEventId() );
            }
        }
        String nameEnglish;
        for( int i=0; i<4; i++ )
        {
            nameEnglish = DISC_COMPETENCY_NAMES[i].toLowerCase();

            for( TestEventScore tes : te.getTestEventScoreList( TestEventScoreType.COMPETENCY.getTestEventScoreTypeId()) )
            {
                if( (tes.getName()!=null && tes.getName().toLowerCase().equals(nameEnglish )) ||
                    (tes.getNameEnglish()!=null && tes.getNameEnglish().toLowerCase().equals(nameEnglish )) )
                {
                    out[i] = tes.getScore();
                    break;
                }
            }
        }

        return out;
    }

    public String getKey( String key )
    {
        if( customProperties==null )
            getProperties();

        try
        {
            if( customProperties==null )
            {
                LogService.logIt( "DiscReportUtils.getKey() customProperties is null. Cannot load. Returning null. key=" + key );
                return null;
            }

            String s = customProperties.getProperty( key, "KEY NOT FOUND" );

            if( s.startsWith( "KEY NOT FOUND") )
                s += " (" + key + ")";

            return s;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DiscReportUtils.getKey() " + key );
            return null;
        }
    }

    public synchronized Properties getProperties()
    {
        if( customProperties== null )
            loadProperties();
        return customProperties;
    }

    private synchronized void loadProperties()
    {
        try
        {
            Properties prop = new Properties();
            InputStream in = getClass().getResourceAsStream( bundleName );

            if( in!=null )
            {
                prop.load(in);
                in.close();
            }
            else
                LogService.logIt( "DiscReportUtils.loadProperties() BBB.1 Unable to load properties for Bundle=" + bundleName );

            customProperties = prop;
            LogService.logIt( "DiscReportUtils.loadProperties() " + bundleName + ", Properties files has " + customProperties.size() + " keys. " );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DiscReportUtils.loadProperties() " );
        }
    }

}
