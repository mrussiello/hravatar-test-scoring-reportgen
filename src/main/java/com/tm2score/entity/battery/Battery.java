package com.tm2score.entity.battery;

import com.tm2score.battery.BatteryScoreType;
import com.tm2score.battery.BatteryType;
import com.tm2score.entity.event.TestEvent;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreColorSchemeType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.service.LogService;
import com.tm2score.sim.ScoreTextParser;
import java.io.Serializable;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Cacheable
@Entity
@Table( name = "battery" )
@NamedQueries( {
        @NamedQuery( name = "Battery.findById", query = "SELECT o FROM Battery AS o WHERE o.batteryId=:batteryId" )
})
public class Battery implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "batteryid" )
    private int batteryId;

    @Column( name = "batterytypeid" )
    private int batteryTypeId;
    
    @Column( name = "moduleordertypeid" )
    private int moduleOrderTypeId;

    @Column( name = "name" )
    private String name;

    @Column( name = "batterystatustypeid" )
    private int batteryStatusTypeId;

    @Column( name = "reentercmpltsims" )
    private int reenterCmpltSims;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;

    @Column( name = "productids" )
    private String productIds;

    @Column( name = "weights" )
    private String weights;

    @Column( name = "lang" )
    private String localeStr;

    @Column( name = "batteryscoretypeid" )
    private int batteryScoreTypeId;

    @Column(name="scoreformattypeid")
    private int scoreFormatTypeId;

    @Column( name = "timelimitseconds" )
    private int timeLimitSeconds;
    
    @Column( name = "textparam1" )
    private String textParam1;

    @Column( name = "strparam1" )
    private String strParam1;

    @Column( name = "strparam2" )
    private String strParam2;

    @Column( name = "scoreparam1" )
    private float scoreParam1;

    @Column( name = "scoreparam2" )
    private float scoreParam2;

    @Column( name = "scoreparam3" )
    private float scoreParam3;

    @Column( name = "scoreparam4" )
    private float scoreParam4;

    @Column(name="greenmin")
    private float greenMin = 4.0f;

    @Column(name="yellowmin")
    private float yellowMin = 2.5f;

    @Column(name="yellowgreenmin")
    private float yellowGreenMin = 4.0f;

    @Column(name="redyellowmin")
    private float redYellowMin = 2f;

    @Column(name="scorecolorschemetypeid")
    private int scoreColorSchemeTypeId = 0;



    @Column(name="scoretext")
    private String scoreText;


    @Transient
    private ScoreTextParser scoreTextParser;


    public String toString() {
        return "Battery{" + "batteryId=" + batteryId + ", name=" + name + ", orgId=" + orgId + ", productIds=" + productIds + '}';
    }

    public BatteryType getBatteryType()
    {
        return BatteryType.getValue( batteryTypeId );
    }

    

    private void initScoreTextParser()
    {
        if( scoreTextParser == null )
            scoreTextParser = new ScoreTextParser( scoreText, ScoreColorSchemeType.getValue( this.scoreColorSchemeTypeId ) );
    }

    public String getRedText()
    {
        initScoreTextParser();

        return scoreTextParser.getRedText();
    }

    public String getRedYellowText()
    {
        initScoreTextParser();

        return scoreTextParser.getRedYellowText();
    }

    public String getYellowText()
    {
        initScoreTextParser();

        return scoreTextParser.getYellowText();
    }

    public String getYellowGreenText()
    {
        initScoreTextParser();

        return scoreTextParser.getYellowGreenText();
    }

    public String getGreenText()
    {
        initScoreTextParser();

        return scoreTextParser.getGreenText();
    }

    public List<TestEvent> setTestEventsInOrder(  List<TestEvent> tel )
    {
        if( tel == null || tel.isEmpty() )
            return tel;

        List<Integer> pids = getProductIdList();

        List<TestEvent> out = new ArrayList<>();

        for( Integer pid : pids )
        {
            for( TestEvent te : tel )
            {
                if( te.getProductId() == pid )
                {
                    out.add( te );
                    break;
                }
            }
        }

        return out;
    }


    public BatteryScore scoreTestBattery( List<TestEvent> tel, BatteryScore bs )
    {
        try
        {
            // tel = setTestEventsInOrder(  tel );

            List<Float> wts = getWeightsList();

            // LogService.logIt( "Battery.scoreTestBattery() weights=" + weights + ", pids=" + productIds  );

            Float wt;
            TestEvent te;

            if( bs==null )
                bs = new BatteryScore();

            bs.setBatteryId( batteryId );
            bs.setBatteryScoreTypeId( batteryScoreTypeId );
            bs.setScoreFormatTypeId( scoreFormatTypeId );
            
            // If this Battery Score doesn't really have a score. Don't create one.
            if( !getBatteryScoreType().needsScore() )
                return bs;
            
            float totalS = 0;
            float totalWts = 0;
            float wtCount = 0;
            float teScore;
            boolean useWts = getBatteryScoreType().equals( BatteryScoreType.WEIGHTED_AVERAGE );
            Integer pid;
            List<Integer> pidl = getProductIdList();
            
            for( int i=0; i<pidl.size(); i++ )
            {
                pid = pidl.get(i);
                wt = useWts && wts.size() > i ? wts.get(i) : 1f;
                
                te=null;
                for( TestEvent tex : tel )
                {
                    if( tex.getProductId()==pid )
                    {
                        // LogService.logIt( "Battery.scoreTestBattery() Found testEventId=" + tex.getTestEventId() + ", productId=" + tex.getProductId() + ", score=" + tex.getOverallScore() );
                        te=tex;
                        break;
                    }                    
                }
                
                // not found.
                if( te==null )
                {
                    LogService.logIt( "Battery.scoreTestBattery() productId=" + pid + ". Could not find test event matching this product id in battery. Using te.score=0" );
                    teScore=0;
                    // continue;
                }
                // something wrong.
                else if( te.getTestEventStatusType().getIsError()  )
                {
                    LogService.logIt( "Battery.scoreTestBattery() te=" + te.getTestEventId()+ " Skipped in battery scoring process due to an error. Using te.score=0" );
                    teScore=0;
                    //continue;
                }
                
                // not scored or higher
                else if( !te.getTestEventStatusType().getIsScoredOrHigher() )
                {
                    LogService.logIt( "Battery.scoreTestBattery() te=" + te.getTestEventId() + " is not in a scored or higher status Skipped in battery scoring process due to an error. " );
                    teScore=0;
                }
                
                else
                    teScore = te.getOverallScore();

                // LogService.logIt( "Battery.scoreTestBattery() pid=" + pid + ", wt=" + wt + ", teScore=" + teScore );
                
                totalS += teScore * wt;

                totalWts +=  wt;

                wtCount++;
            }
            
            /*
            for( int i=0; i< tel.size(); i++ )
            {
                te = tel.get(i);

                if( te.getTestEventStatusType().getIsError() )
                {
                    LogService.logIt( "Battery.scoreTestBattery() te=" + te.toString() + " Skipped in battery scoring process due to an error. " );
                    continue;
                }

                wt = useWts && wts.size() > i ? wts.get(i) : 1f;

                teScore = te.getOverallScore(); // convertTeScore( te.getOverallScore(), te.getScoreFormatTypeId() );

                totalS += teScore * wt.floatValue();

                totalWts +=  wt.floatValue();

                wtCount++;
            }
            */

            // LogService.logIt( "Battery.scoreTestBattery() totalWts= = " + totalWts + ", totalS=" + totalS + ", wtCount=" + wtCount );


            totalS = totalWts > 0 ? totalS/totalWts : totalS;


            //if( bs==null )
            //    bs = new BatteryScore();

            //bs.setBatteryId( batteryId );
            bs.setRawScore( totalS );
            bs.setScore(totalS);

            boolean hasCatScoreInfo = greenMin != yellowMin && ( greenMin > 0 || yellowMin > 0 );

            bs.setScoreCategoryId( ScoreCategoryType.UNRATED.getScoreCategoryTypeId() );

            // next do score text
            if( totalS >= greenMin && greenMin>0 && hasCatScoreInfo )
            {
                bs.setScoreText( getGreenText() );
                bs.setScoreCategoryId( ScoreCategoryType.GREEN.getScoreCategoryTypeId() );
            }

            else if( totalS >= yellowMin && yellowMin>0 && hasCatScoreInfo )
            {
                bs.setScoreText(getYellowText() );
                bs.setScoreCategoryId( ScoreCategoryType.YELLOW.getScoreCategoryTypeId() );
            }

            else if( hasCatScoreInfo )
            {
                bs.setScoreText( getRedText() );
                bs.setScoreCategoryId( ScoreCategoryType.RED.getScoreCategoryTypeId() );
            }

        }

        catch( Exception e )
        {
            LogService.logIt( e, "Battery.scoreTestBattery() batteryId=" + batteryId );
            if( bs != null )
                bs.appendErrorTxt( e.getMessage() );
        }

        return bs;

    }


    public ScoreFormatType getScoreFormatType()
    {
        return ScoreFormatType.getValue( scoreFormatTypeId );
    }

    //protected float convertTeScore( float scrIn, int teScoreFormatTypeId )
    //{
        //if( 1==1 )
    //        return scrIn;

        //if( teScoreFormatTypeId == this.scoreFormatTypeId )
       //     return scrIn;

        //return getScoreFormatType().convertSourceToLinearScore( teScoreFormatTypeId, scrIn );
    //}

    public List<Float> getWeightsList()
    {
        List<Float> out = new ArrayList<>();

        if( weights == null || weights.isEmpty() )
            return out;

        StringTokenizer st = new StringTokenizer( weights, ";" );

        String tk;
        Float fl;

        while( st.hasMoreTokens() )
        {
            try
            {
                tk = st.nextToken();

                fl = (Float) Float.parseFloat(tk);

                out.add( fl );
            }
            catch( NumberFormatException e )
            {
                out.add( (Float)(1f) );
            }
        }

        return out;
    }

    public BatteryScoreType getBatteryScoreType()
    {
        return BatteryScoreType.getValue( this.batteryScoreTypeId );
    }

    public List<Integer> getProductIdList()
    {
        List<Integer> out = new ArrayList<>();

        if( productIds == null || productIds.trim().isEmpty() )
            return out;

        String[] pids = productIds.split( "," );

        int id=0;

        for( String pid : pids )
        {
            if( pid == null )
                continue;

            pid = pid.trim();

            if( pid.isEmpty() )
                continue;

            id = 0;

            try
            {
                id = Integer.parseInt( pid );

                // if real and not a duplicate.
                if( id > 0 && !out.contains( id ) )
                    out.add( id );
            }

            catch( NumberFormatException e )
            {
                LogService.logIt( e, "Battery.getProductIdList() productIds=" + productIds + ", pid=" + pid + ", id=" + id );
            }
        }

        return out;
    }


    public int getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(int batteryId) {
        this.batteryId = batteryId;
    }

    public int getBatteryStatusTypeId() {
        return batteryStatusTypeId;
    }

    public void setBatteryStatusTypeId(int batteryStatusTypeId) {
        this.batteryStatusTypeId = batteryStatusTypeId;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getProductIds() {
        return productIds;
    }

    public void setProductIds(String productIds) {
        this.productIds = productIds;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
    }

    public int getBatteryScoreTypeId() {
        return batteryScoreTypeId;
    }

    public void setBatteryScoreTypeId(int batteryScoreTypeId) {
        this.batteryScoreTypeId = batteryScoreTypeId;
    }

    public float getScoreParam1() {
        return scoreParam1;
    }

    public void setScoreParam1(float scoreParam1) {
        this.scoreParam1 = scoreParam1;
    }

    public float getScoreParam2() {
        return scoreParam2;
    }

    public void setScoreParam2(float scoreParam2) {
        this.scoreParam2 = scoreParam2;
    }

    public float getScoreParam3() {
        return scoreParam3;
    }

    public void setScoreParam3(float scoreParam3) {
        this.scoreParam3 = scoreParam3;
    }

    public float getScoreParam4() {
        return scoreParam4;
    }

    public void setScoreParam4(float scoreParam4) {
        this.scoreParam4 = scoreParam4;
    }

    public int getReenterCmpltSims() {
        return reenterCmpltSims;
    }

    public void setReenterCmpltSims(int reenterCmpltSims) {
        this.reenterCmpltSims = reenterCmpltSims;
    }

    public int getModuleOrderTypeId() {
        return moduleOrderTypeId;
    }

    public void setModuleOrderTypeId(int moduleOrderTypeId) {
        this.moduleOrderTypeId = moduleOrderTypeId;
    }

    public String getWeights() {
        return weights;
    }

    public void setWeights(String weights) {
        this.weights = weights;
    }

    public float getGreenMin() {
        return greenMin;
    }

    public void setGreenMin(float greenMin) {
        this.greenMin = greenMin;
    }

    public String getScoreText() {
        return scoreText;
    }

    public void setScoreText(String s) {

        if( s != null && s.isEmpty() )
            s = null;

        this.scoreText = s;
    }

    public float getYellowMin() {
        return yellowMin;
    }

    public void setYellowMin(float yellowMin) {
        this.yellowMin = yellowMin;
    }

    public int getScoreFormatTypeId() {
        return scoreFormatTypeId;
    }

    public void setScoreFormatTypeId(int scoreFormatTypeId) {
        this.scoreFormatTypeId = scoreFormatTypeId;
    }

    public float getYellowGreenMin() {
        return yellowGreenMin;
    }

    public void setYellowGreenMin(float yellowGreenMin) {
        this.yellowGreenMin = yellowGreenMin;
    }

    public float getRedYellowMin() {
        return redYellowMin;
    }

    public void setRedYellowMin(float redYellowMin) {
        this.redYellowMin = redYellowMin;
    }

    public int getScoreColorSchemeTypeId() {
        return scoreColorSchemeTypeId;
    }

    public void setScoreColorSchemeTypeId(int scoreColorSchemeTypeId) {
        this.scoreColorSchemeTypeId = scoreColorSchemeTypeId;
    }

    public int getBatteryTypeId() {
        return batteryTypeId;
    }

    public void setBatteryTypeId(int batteryTypeId) {
        this.batteryTypeId = batteryTypeId;
    }

    public String getTextParam1() {
        return textParam1;
    }

    public void setTextParam1(String textParam1) {
        this.textParam1 = textParam1;
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public String getStrParam2() {
        return strParam2;
    }

    public void setStrParam2(String strParam2) {
        this.strParam2 = strParam2;
    }

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(int timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }



}
