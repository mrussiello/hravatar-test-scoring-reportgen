/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.findly;

import com.tm2score.entity.event.TestEvent;
import com.tm2score.entity.event.TestEventScore;
import com.tm2score.entity.user.User;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreStatusType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.findly.xml.Scores;
import com.tm2score.findly.xml.Scores.Score.ScoreInfo.Metric.MetricValue;
import com.tm2score.service.LogService;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.user.UserFacade;
import com.tm2score.util.StringUtils;
import com.tm2score.xml.JaxbUtils;
import java.util.ArrayList;
import java.util.List;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import jakarta.xml.bind.JAXBElement;

/**
 *
 * @author Mike
 */
public class BaseFindlyScoreInfo {

    public Scores.Score.ScoreInfo scoreInfo;

    Scores.Score.TestInfo findlyTestInfo;
    Scores.Score.ScoreInfo findlyScoreInfo;
    public List<Scores.Score.ScoreInfo.Metric> metrics;

    TestEvent te;

    UserFacade userFacade;


    public void populateTestEventAndCreateTestEventScoreList( TestEvent te ) throws Exception
    {
        try
        {
            this.te = te;

            setScoreObjects( te );

            te.setTotalTestTime( findlyTestInfo.getTimeElapsed() );

            float overallScr = getOverallScore();

            if( overallScr<0 )
                overallScr = 0;

            te.setOverallScore( overallScr );

            if( te.getTestEventScoreList() == null )
                te.setTestEventScoreList(new ArrayList<TestEventScore>());

            List<TestEventScore> tesl=te.getTestEventScoreList();

            TestEventScore tes = new TestEventScore();
            tes.setTestEventScoreTypeId( TestEventScoreType.OVERALL.getTestEventScoreTypeId() );
            tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
            tes.setTestEventId( te.getTestEventId() );
            tes.setCreateDate(new Date() );
            tes.setName( "Overall Score" );
            tes.setRawScore( te.getOverallScore() );
            tes.setScore( te.getOverallScore() );
            tes.setScoreFormatTypeId( ScoreFormatType.NUMERIC_0_TO_100.getScoreFormatTypeId() );

            TestEventScore tes2 = getMatchingExistingTestEventScore( tes );
            if( tes2!=null && tes2.getTestEventScoreId()>0 )
                tes.setTestEventScoreId( tes2.getTestEventScoreId() );
            
            FindlyPercentile fp = getFindlyPercentile();

            if( fp != null )
            {
                te.setOverallPercentile( fp.percentile );
                te.setOverallPercentileCount( fp.scoreCount );
                tes.setPercentile(fp.percentile);
                tes.setOverallPercentileCount( fp.scoreCount );
            }

            tesl.add( tes );

            List<FindlyCompetency> levelScores = getLevelScores();

            List<FindlyCompetency> scaleScores = getScaleScores();
            // List<FindlyCompetency> scaleScores = getScaleScores();

            // LogService.logIt( "BaseFindlyScoreInfo.populateTestEventAndCreateTestEventScoreList() Found " + scaleScores.size() + " Scale Scores.");

            int displayOrder = 1;

            int simCompetencyClassId = SimCompetencyClass.NONCOGNITIVE.getSimCompetencyClassId();

            int testEventScoreTypeId = TestEventScoreType.COMPETENCY.getTestEventScoreTypeId();

            if( FindlyTestType.getValue( te.getProduct().getIntParam7() ).getIsSkillsKnowledgeCompetency() )
            {
                testEventScoreTypeId = TestEventScoreType.SKILLS.getTestEventScoreTypeId();
                simCompetencyClassId = SimCompetencyClass.CORESKILL.getSimCompetencyClassId();
            }

            else if( FindlyTestType.getValue( te.getProduct().getIntParam7() ).getIsCognitiveCompetency() )
            {
                testEventScoreTypeId = TestEventScoreType.ABILITIES.getTestEventScoreTypeId();
                simCompetencyClassId = SimCompetencyClass.ABILITY.getSimCompetencyClassId();
            }

            for( FindlyCompetency fc : levelScores )
            {
                tes = new TestEventScore();
                tes.setCreateDate( new Date() );
                tes.setTestEventScoreTypeId( TestEventScoreType.LEVEL_SCORES.getTestEventScoreTypeId() );
                tes.setSimCompetencyClassId( 0 );
                tes.setTestEventId( te.getTestEventId() );
                tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
                tes.setDisplayOrder(displayOrder);
                displayOrder++;
                tes.setScore( fc.score);
                tes.setName( fc.name );
                tes.setRawScore( fc.score );
                tes.setScoreFormatTypeId( ScoreFormatType.NUMERIC_0_TO_100.getScoreFormatTypeId() );

                tes2 = getMatchingExistingTestEventScore( tes );
                if( tes2!=null && tes2.getTestEventScoreId()>0 )
                    tes.setTestEventScoreId( tes2.getTestEventScoreId() );
                
                tesl.add( tes );
            }


            for( FindlyCompetency fc : scaleScores )
            {
                tes = new TestEventScore();
                tes.setCreateDate( new Date() );
                tes.setTestEventScoreTypeId( testEventScoreTypeId );
                tes.setSimCompetencyClassId(simCompetencyClassId);
                tes.setTestEventId( te.getTestEventId() );
                tes.setTestEventScoreStatusTypeId( TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() );
                tes.setDisplayOrder(displayOrder);
                displayOrder++;
                tes.setScore( fc.score);
                tes.setName( fc.name );
                tes.setRawScore( fc.score );
                tes.setScoreFormatTypeId( ScoreFormatType.NUMERIC_0_TO_100.getScoreFormatTypeId() );
                
                tes2 = getMatchingExistingTestEventScore( tes );
                if( tes2!=null && tes2.getTestEventScoreId()>0 )
                    tes.setTestEventScoreId( tes2.getTestEventScoreId() );

                tesl.add( tes );
            }
        }

        catch( Exception e )
        {
            LogService.logIt(e, "BaseFindlyScoreInfo.populateTestEventAndCreateTestEventScoreList() " + te.toString() );
        }

    }


    protected FindlyPercentile getFindlyPercentile() throws Exception
    {
        FindlyPercentile fp = new FindlyPercentile();
        fp.name="Overall";

        Scores.Score.ScoreInfo.Metric metric = null;

        for( Scores.Score.ScoreInfo.Metric m : metrics )
        {
            if( m.getMetricName().equalsIgnoreCase("NormData") )
            {
                metric = m;
                break;
            }
        }

        if( metric == null )
            return null;

        float percentile = 0;
        int numScoresUsed = 0;

        Scores.Score.ScoreInfo.Metric.MetricValue metricValue = metric.getMetricValue();

        for( Serializable s: metricValue.getContent() ){

            // LogService.logIt(  "Type is: " +   s.getClass().toString() );

            if( s instanceof String )
            {}
            else
            {
                String tag = ((JAXBElement)s).getName().getLocalPart();

                if( tag != null && tag.equalsIgnoreCase( "Percentile" ) )
                {
                    percentile = Float.parseFloat( ((JAXBElement)s).getValue().toString() );
                }
                if( tag != null && tag.equalsIgnoreCase( "NumScoresUsed" ) )
                {
                    numScoresUsed = Integer.parseInt( ((JAXBElement)s).getValue().toString() );
                }
            }
        }

        fp.percentile = percentile;
        fp.scoreCount = numScoresUsed;

        // LogService.logIt( "BaseFindlyScoreInfo.getFindlyPercentile() percentile=" + percentile + ", num scores=" + numScoresUsed );

        return fp;
    }



    protected List<FindlyCompetency> getLevelScores() throws Exception
    {
        List<FindlyCompetency> out = new ArrayList<>();

        if( this.metrics==null )
            return out;

        Scores.Score.ScoreInfo.Metric metric = null;

        for( Scores.Score.ScoreInfo.Metric m : metrics )
        {
            if( m.getMetricName().equalsIgnoreCase("LevelScores") )
            {
                metric = m;
                break;
            }
        }

        if( metric == null )
            return out;

        float beg=0;
        float med=0;
        float high=0;
        String tag;

        Scores.Score.ScoreInfo.Metric.MetricValue metricValue = metric.getMetricValue();

        for( Serializable s: metricValue.getContent() ){

            if( s instanceof String )
                continue;

            tag = ((JAXBElement)s).getName().getLocalPart();

            if( tag == null || tag.isEmpty() )
                continue;

            if( tag.equalsIgnoreCase( "ScoreBeg" ) )
                beg = Float.parseFloat( ((JAXBElement)s).getValue().toString() );
            else if( tag.equalsIgnoreCase( "ScoreInt" ) )
                med = Float.parseFloat( ((JAXBElement)s).getValue().toString() );
            else if( tag.equalsIgnoreCase( "ScoreAdv" ) )
                high = Float.parseFloat( ((JAXBElement)s).getValue().toString() );
        }

        FindlyCompetency fc = new FindlyCompetency();
        fc.name = "Beginner-Level Questions";
        fc.score = beg;
        if( beg >= 0 && beg<=100)
            out.add( fc );

        fc = new FindlyCompetency();
        fc.name = "Medium-Level Questions";
        fc.score = med;
        if( med >= 0 && med<=100 )
            out.add( fc );

        fc = new FindlyCompetency();
        fc.name = "Expert-Level Questions";
        fc.score = high;

        if( high >= 0 && high<=100 )
            out.add( fc );

        return out;
    }



    protected List<FindlyCompetency> getScaleScores() throws Exception
    {
        List<FindlyCompetency> out = new ArrayList<>();

        if( this.metrics==null )
            return out;

        Scores.Score.ScoreInfo.Metric metric = null;

        for( Scores.Score.ScoreInfo.Metric m : metrics )
        {
            if( m.getMetricName().equalsIgnoreCase("ScaleScores") )
            {
                metric = m;
                break;
            }
        }

        if( metric == null )
            return out;

         Scores.Score.ScoreInfo.Metric.MetricValue.Scale scale;

         List<Scores.Score.ScoreInfo.Metric.MetricValue.Scale> sl = new ArrayList<>();

        Scores.Score.ScoreInfo.Metric.MetricValue metricValue = metric.getMetricValue();

        for( Serializable s: metricValue.getContent() ){

            // LogService.logIt(  "Type is: " +   s.getClass().toString() );

            if( s instanceof JAXBElement )
            {
                // LogService.logIt(  "Type is: " +  (((JAXBElement)s).getValue().toString() ) );

                if( ((JAXBElement)s).getValue() instanceof Scores.Score.ScoreInfo.Metric.MetricValue.Scale )
                {
                    scale = (Scores.Score.ScoreInfo.Metric.MetricValue.Scale)(((JAXBElement)s).getValue() );

                    sl.add( scale );
                }

            }

            else
            {

            }
        }

        FindlyCompetency fc;

        for( MetricValue.Scale s : sl )
        {
            fc = new FindlyCompetency();
            fc.name = s.getName();
            fc.score = s.getScore();
            out.add( fc );

            // LogService.logIt(  "Added: " +   fc.toString() );

        }

        Collections.sort( out );

        return out;
    }

    protected String getStringMetricValue( String name )  throws NumberFormatException
    {
        if( this.metrics==null )
            return "";

        String str = null;

        for( Scores.Score.ScoreInfo.Metric m : metrics )
        {
            if( m.getMetricName().equalsIgnoreCase(name) )
            {
                Scores.Score.ScoreInfo.Metric.MetricValue metricValue = m.getMetricValue();

                for( Serializable s: metricValue.getContent() ){

                    // LogService.logIt(  "Type is: " +   s.getClass().toString() );

                    if( s instanceof String )
                        str = ((String)s).trim();
                }

            }
        }

        // LogService.logIt( "getStringMetricValue() " + name + " : " + str );

        return str;
    }


    
    protected boolean hasMetricValue( String name )
    {
        if( this.metrics==null )
            return false;

        for( Scores.Score.ScoreInfo.Metric m : metrics )
        {
            if( m.getMetricName().equalsIgnoreCase(name) )
                return true;
        }

        return false;        
    }

    protected float getMetricValue( String name )  throws NumberFormatException
    {
        if( this.metrics==null )
            return -1;

        float score = -1;

        for( Scores.Score.ScoreInfo.Metric m : metrics )
        {
            if( m.getMetricName().equalsIgnoreCase(name) )
            {
                Scores.Score.ScoreInfo.Metric.MetricValue metricValue = m.getMetricValue();

                for( Serializable s: metricValue.getContent() ){

                    // LogService.logIt(  "Type is: " +   s.getClass().toString() );

                    if( s instanceof String )
                    {
                        if(!((String)s).trim().isEmpty() )
                        {
                            score = Float.parseFloat( ((String)s).trim()  );
                            break;
                        }
                    }
                }

            }
        }

        // LogService.logIt( "getMetricValue() " + name + " : " + score );

        return score;
    }

    
    protected String getMetricValueString( String name )  throws NumberFormatException
    {
        if( this.metrics==null )
            return null;

        for( Scores.Score.ScoreInfo.Metric m : metrics )
        {
            if( m.getMetricName().equalsIgnoreCase(name) )
            {
                Scores.Score.ScoreInfo.Metric.MetricValue metricValue = m.getMetricValue();

                for( Serializable s: metricValue.getContent() ){

                    // LogService.logIt(  "Type is: " +   s.getClass().toString() );

                    if( s instanceof String )
                    {
                        return (String) s;
                    }
                }

            }
        }

        // LogService.logIt( "getMetricValueString() " + name  );

        return null;
    }
    

    protected float getOverallScore() throws NumberFormatException
    {
        if( hasMetricValue( "ScoreTotal" ) )
            return getMetricValue( "ScoreTotal" );
        
        if( hasMetricValue( "Overall" ) )
            return getMetricValue( "Overall" );
               
        if( hasMetricValue( "OverallScore" ) )
            return getMetricValue( "OverallScore" ); 
        
        return -1;
    }



    protected void setScoreObjects( TestEvent te ) throws Exception
    {
        String scoreXml = te.getResultXml();

        if( scoreXml == null || scoreXml.isEmpty() )
            throw new Exception( "ScoreXml is not present." );

        try
        {
            Scores scores = JaxbUtils.ummarshalFindlyScoreXml( scoreXml );

            Scores.Score findlyScore = scores.getScore();

            //Scores.Score.UserInfo findlyUserInfo = findlyScore.getUserInfo();
            findlyTestInfo = findlyScore.getTestInfo();
            findlyScoreInfo = findlyScore.getScoreInfo();

            metrics = findlyScoreInfo.getMetric();

            te.setTotalTestTime( findlyTestInfo.getTimeElapsed() );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseFindlyScoreInfo.setScoreObjects() " + te.toString() );
        }


    }


    /*
    private static String getMetricValueText( Serializable s )
    {
        if( s instanceof String )
            return (String)s;

        return null;
    }

    private static MetricValue. getMetricValueText( Serializable s )
    {
        if( s instanceof String )
            return (String)s;

        return null;
    }
    */


    private static void dumpMetricValue( Scores.Score.ScoreInfo.Metric.MetricValue c ){
        //     LogService.logIt(  "Metric Value contains: " +   c.getContent().size() );
        for( Serializable s: c.getContent() ){

            // LogService.logIt(  "Type is: " +   s.getClass().toString() );

            if( s instanceof String ){
                LogService.logIt(  "YES STRING: " +   (String)s );
            } else
            {
                String tag = ((JAXBElement)s).getName().getLocalPart();

                //Scores.Score.ScoreInfo.Metric.MetricValue chunk = (Scores.Score.ScoreInfo.Metric.MetricValue)((JAXBElement)s).getValue();
                LogService.logIt( "dumpMetricValue() NOT STRING tag=" + tag + ":" + ((JAXBElement)s).getValue().toString() );
                //dumpMetricValue( chunk );
                //LogService.logIt( ":" + tag + ")" );
            }
        }
    }

    public FindlyCompetency getFindlyCompetency( String tagVal, String name ) throws Exception
    {
        if( this.metrics==null )
            return null;

        FindlyCompetency fc = null;

        float val;

        val = this.getMetricValue( tagVal );

        if( val>0 )
        {
            fc = new FindlyCompetency();
            fc.score = val;
            fc.name = name;
        }

        return fc;

    }

    /*
    private String getReportFilename( TestEvent te ) throws Exception
    {
        String out = "";

        if( userFacade == null ) userFacade = UserFacade.getInstance();

        User user = userFacade.getUser( te.getUserId() );

        if( user.getUserType().getNamed() )
        {
            out = StringUtils.alphaCharsOnly( user.getLastName() );

            out = StringUtils.removeChar( out , ' ' );

            if( out.length() > 20 )
                out = out.substring(0, 20 );

            out += "_";
        }

        else if( user.getUserType().getUsername() ||  user.getUserType().getUserId() )
        {
            out = StringUtils.alphaNumCharsOnly( user.getEmail() );

            out = StringUtils.removeChar( out , ' ' );

            if( out.length() > 20 )
                out = out.substring(0, 20 );

            out += "_";
        }
        
        
        String reportName = "";

        if( te.getReport() != null )
            reportName = te.getReport().getName();

        else
            reportName = "Score_Report";

        String comprRptNm = StringUtils.removeChar( reportName , ' ' );

        comprRptNm = StringUtils.alphaCharsOnly( comprRptNm );

        if( comprRptNm.length() > 14 )
            comprRptNm = comprRptNm.substring(0, 14 );

        out += comprRptNm + "_";

        Calendar c = new GregorianCalendar();

        out += c.get( Calendar.YEAR ) + "-" + ( c.get( Calendar.MONTH ) + 1 ) + "-" + c.get( Calendar.DAY_OF_MONTH );

        return out;
    }
    */



    public String getOverallScoreDescripStr()
    {
        return null;
    }

    public List<FindlyCompetency> getCompetencies()
    {
        return null;
    }

    public ScoreFormatType getScoreFormatType()
    {
        return ScoreFormatType.NUMERIC_0_TO_100;
    }

    
    public TestEventScore getMatchingExistingTestEventScore( TestEventScore newTes )
    {
        if( te.getTestEventScoreList()==null || te.getTestEventScoreList().isEmpty() || newTes.getName()==null || newTes.getName().isEmpty() )
            return null;
        
        for( TestEventScore tes : te.getTestEventScoreList() )
        {
            if( tes.getTestEventScoreTypeId()!=newTes.getTestEventScoreTypeId() )
                continue;
                
            if(  tes.getName()!=null && tes.getName().equals( newTes.getName() ) )
                return tes;

            if( newTes.getNameEnglish()!=null && !newTes.getNameEnglish().isEmpty() && tes.getNameEnglish()!=null && tes.getNameEnglish().equals( newTes.getNameEnglish() ) )
                return tes;
        }
        
        return null;        
    }
    
    
    
}
