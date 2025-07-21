package com.tm2score.entity.event;

import com.tm2score.entity.report.Report;
import com.tm2score.event.ScoreCategoryType;
import com.tm2score.event.ScoreFormatType;
import com.tm2score.event.TestEventScoreType;
import com.tm2score.format.FormatCompetency;
import com.tm2score.global.Constants;
import com.tm2score.global.DisplayOrderObject;
import com.tm2score.global.ErrorTxtObject;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.interview.InterviewQuestion;
import com.tm2score.ivr.IvrStringUtils;
import com.tm2score.report.ReportUtils;
import com.tm2score.score.CaveatScore;
import com.tm2score.score.CaveatScoreType;
import com.tm2score.score.ScoreCategoryRange;
import com.tm2score.score.TextAndTitle;
import com.tm2score.score.simcompetency.SimCompetencyScore;
import com.tm2score.service.EncryptUtils;
import com.tm2score.service.LogService;
import com.tm2score.sim.InterviewQuestionBreadthType;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2score.sim.SimCompetencyClass;
import com.tm2score.util.STStringTokenizer;
import com.tm2score.util.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;


@Entity
@Table( name = "testeventscore" )
@NamedQueries( {
        @NamedQuery( name = "TestEventScore.findByTestEventId", query = "SELECT o FROM TestEventScore AS o WHERE o.testEventId=:testEventId" )
} )
public class TestEventScore implements Serializable, Comparable<TestEventScore>, ErrorTxtObject, Cloneable, DisplayOrderObject
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testeventscoreid" )
    private long testEventScoreId;

    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "testeventscorestatustypeid" )
    private int testEventScoreStatusTypeId;

    // overall or subscore
    @Column( name = "testeventscoretypeid" )
    private int testEventScoreTypeId;

    @Column( name = "hide" )
    private int hide;

    @Column( name = "hidenumericscore" )
    private int hideNumericScore;



    // non-numeric, numeric 0-5, 0-100, other
    @Column( name = "scoreformattypeid" )
    private int scoreFormatTypeId;

    @Column( name = "scorecategoryid" )
    private int scoreCategoryId;

    @Column( name = "name" )
    private String name;

    @Column( name = "nameenglish" )
    private String nameEnglish;

    /*
    
     For Overall type, a value of 1 indicates the test had an overall timeout.
     For SimCompetencyType it's the alt SimCompetencyClassId (set to original SimCompetencyClassId when the TestEventScore.simCompetencyClassId is set to a subclass.)
     For SimCompetencyGroup type, it's the SimCompetencyGroupTypeId
    */
    @Column( name = "intparam1" )
    private int intParam1 = 0;

    
    /*
     For competency type, it's the includeItemScoresTypeId
    */
    @Column( name = "intparam2" )
    private int intParam2 = 0;

    @Column( name = "longparam1" )
    private long longParam1 = 0;

    @Column( name = "longparam2" )
    private long longParam2 = 0;

    @Column( name = "strparam1" )
    private String strParam1;

    /*
     For SimCompetencyGroup Type this is the name of the Group to use.
    */
    @Column( name = "strparam2" )
    private String strParam2;

    

    @Column( name = "displayorder" )
    private int displayOrder;

    @Column( name = "score" )
    private float score = 0;

    @Column( name = "score2" )
    private float score2;

    @Column( name = "score3" )
    private float score3;

    @Column( name = "score4" )
    private float score4;

    @Column( name = "score5" )
    private float score5;

    @Column( name = "score6" )
    private float score6;

    @Column( name = "score7" )
    private float score7;

    @Column( name = "score8" )
    private float score8;

    @Column( name = "score9" )
    private float score9;
    
    @Column( name = "score10" )
    private float score10;

    @Column( name = "score11" )
    private float score11;

    @Column( name = "score12" )
    private float score12;
    
    @Column( name = "score13" )
    private float score13;
    
    @Column( name = "score14" )
    private float score14;
    
    @Column( name = "score15" )
    private float score15;
    
    
    @Column( name = "percentile" )
    private float percentile = -1;

    @Column( name = "accountpercentile" )
    private float accountPercentile = -1;

    @Column( name = "countrypercentile" )
    private float countryPercentile = -1;

    @Column( name = "overallpercentilecount" )
    private int overallPercentileCount;

    @Column( name = "accountpercentilecount" )
    private int accountPercentileCount;

    @Column( name = "countrypercentilecount" )
    private int countryPercentileCount;

    @Column(name="percentilecountry")
    private String percentileCountry;


    @Column( name = "scoretext" )
    private String scoreText;

    @Column( name = "textparam1" )
    private String textParam1;

    @Column( name = "textbasedresponses" )
    private String textbasedResponses;

    @Column( name = "interviewquestions" )
    private String interviewQuestions;

    @Column( name = "rawscore" )
    private float rawScore;

    @Column( name = "weight" )
    private float weight;

    /**
     * this is the total value (total points or total correct
     * used in calculating the score.
     * Across all simlets or aggregate or task competencies only.
     * 
     * For Overall Score when overall raw score is calculated from weighted average of comeptency z scores and then converted to a new z score, this is the Z Score Percentile.
     */
    @Column( name = "totalused" )
    private float totalUsed = 0;

    /**
     * This is the max value (max possible points or max correct responses)
     * used in calculating the score.
     * Across all simlets or aggregate or task competencies only.
     */
    @Column( name = "maxvalueused" )
    private float maxValueUsed = 0;


    @Column( name = "totalcorrect" )
    private float totalCorrect = 0;

    @Column( name = "totalpoints" )
    private float totalPoints = 0;

    @Column( name = "maxtotalcorrect" )
    private float maxTotalCorrect = 0;

    @Column( name = "maxtotalpoints" )
    private float maxTotalPoints = 0;

    @Column( name = "totalscorableitems" )
    private float totalScorableItems = 0;

    /*
     Overall - contains last score date.
     Report - contains most recent send date (email or text) to CANDIDATE for this REPORT.
    */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="dateparam1")
    private Date dateParam1;
    
    /*
     Overall - contains most recent send (email or text) date to ADMINISTRATOR(S) for this TestEvent
    */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="dateparam2")
    private Date dateParam2;
    



    /**
     * This is the fraction correct or fraction of total points used (if appropriate)
     * Across all simlets or aggregate or task competencies only.
     * 
     * For Overall Score when overall raw score is calculated from weighted average of competency z scores and then converted to a new z score, this indicates that totalUsed represents a Z-score derived percentile.
     * 
     */
    @Column( name = "fractionused" )
    private float fractionUsed = 0;

    /**
     * This is mean used if a normalized scale was used for calculation.
     * Across all simlets or aggregate or task competencies only.
     */
    @Column( name = "mean" )
    private float mean = 0;

    /**
     * This is std deviation used if a normalized scale was used for calculation.
     * Across all simlets or aggregate or task competencies only.
     */
    @Column( name = "stddeviation" )
    private float stdDeviation = 0;

    @Column( name = "scorableitemresponses" )
    private int scorableItemResponses = 0;


    /**
     * This is the competencyScoreTypeId of the SimCompetency, based on its member simlets.
     */
    @Column( name = "scoretypeidused" )
    private int scoreTypeIdUsed = 0;

    @Column( name = "simcompetencyclassid" )
    private int simCompetencyClassId = 0;

    @Column( name = "simcompetencyid" )
    private long simCompetencyId = 0;

    @Column( name = "simletid" )
    private long simletId = 0;

    @Column( name = "simletversionid" )
    private int simletVersionId = 0;

    @Column( name = "simletcompetencyid" )
    private long simletCompetencyId = 0;




    @Column( name = "reportid" )
    private long reportId;

    @Column( name = "reportbytes" )
    private byte[] reportBytes;

    /**
     * For Competency Types, this is the scorePresentationTypeId
     * 
     */
    @Column( name = "reportfilecontenttypeid" )
    private int reportFileContentTypeId;

    @Column( name = "reportfilename" )
    private String reportFilename;

    @Column(name="errortxt")
    private String errorTxt;

    @Transient
    private TestEvent testEvent;

    @Transient
    private Report report;

    // Used for al la cart report creations on admin screen.
    @Transient
    private Date createDate;

    @Transient
    private float[] profileBoundaries;

    @Transient
    private FormatCompetency formatCompetency;

    @Transient
    private boolean includeNumericScoreInResults = true;

    @Transient
    private boolean tempBoolean1 = false;
    

    public void setReportAndScoringFlags( Locale simLocale, SimCompetencyScore simCompetencyScore)
    {
        
        
        //if( simLocale == null ||
        //    simLocale.equals( Locale.US ) ||
        //    simLocale.getLanguage().toLowerCase().equals( "en" ) )
        //    return;

        if( !getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) || (simCompetencyClassId!=SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() && simCompetencyClassId!=SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId() && simCompetencyClassId!=SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId()) )
            return;
        
        // Essay with a valid AI score for this Essay Competency or if uses AI Scoring and .
        if( simCompetencyClassId==SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() && ((score2>0 && score3>0) || (score7>0 && score3>0)) )
            return;
        
        // Any Essay no Valid AI Score, but not plagiarized and has a score above the min.
        if( simCompetencyClassId==SimCompetencyClass.SCOREDESSAY.getSimCompetencyClassId() && score>getScoreFormatType().getMinScoreToGiveTestTaker() )  //  && (simLocale == null || simLocale.equals( Locale.US ) || simLocale.getLanguage().toLowerCase().equals( "en" ))
        {
            boolean plag = false;
            if( simCompetencyScore!=null )
            {
                for( CaveatScore cs : simCompetencyScore.getCaveatList2() )
                {
                    if( cs.getCaveatScoreType().equals( CaveatScoreType.PLAGIARIZED) || cs.getCaveatScoreType().equals( CaveatScoreType.PLAGIARIZED_XY) )
                    {
                        plag = true;
                        break;
                    }
                }
            }
            if( !plag )
                return;
        }

        // We have a valid AI score for this AV competency.
        if( simCompetencyClassId==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() && score6>0 && score7>0 )
            return;

        // if this ScoredAv competency uses correct/incorrect items (AvItemType 112 or 122)
        if( simCompetencyClassId==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() && simCompetencyScore!=null && !simCompetencyScore.getCompetencyScoreType().isScoredAvUpload())
            return;

        // We have a valid VIBES score for this AV competency. Currently NOT used in Score under any circumstances.
        if( simCompetencyClassId==SimCompetencyClass.SCOREDAVUPLOAD.getSimCompetencyClassId() && 1==2 )
            return;
                
        // Scored Essay not English, hide it!
        hideNumericScore=1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException 
    {
        return (TestEventScore) super.clone(); 
    }


    public boolean getUsesPercentCorrectScoring()
    {
        return this.getTestEventScoreType().getIsCompetency() && getSimCompetencyClass().isKnowledgeSkillAbility() && mean==0.65f && stdDeviation==0.15f;
    }
    
    
    public String getReportDirectDownloadLink()
    {
        if( !getTestEventScoreType().getIsReport() ||  !getHasReport() )
            return null;

        try
        {
           return RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/rptdnldx/" + getTestEventIdEncrypted() + "/" + getTestEventScoreIdEncrypted() + "/" + this.getReportFilename();

           // return RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/rptdnldx/" + URLEncoder.encode( getTestEventIdEncryptedPlain(), "UTF8" ) + "/" + URLEncoder.encode( getTestEventScoreIdEncryptedPlain(), "UTF8" ) + "/" + this.getReportFilename();
        // return RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/rptdnldx/" + URLEncoder.encode( getTestEventIdEncrypted(), "UTF8" ) + "/" + URLEncoder.encode( getTestEventScoreIdEncrypted(), "UTF8" ) + "/" + this.getReportFilename();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestEventScore.getReportDirectDownloadLink() " + toString() );
        }

        return RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/rptdnld?te=" + this.getTestEventId() + "&tes=" +this.getTestEventScoreId() + "&r=2";
    }
    
    /*
    public String getOverallScore4Show()
    {
        if( this.getScorePrecisionDigits()==2 )
            return NumberUtils.getTwoDecimalFormattedAmount(getScore());
        if( this.getScorePrecisionDigits()==1 )
            return NumberUtils.getOneDecimalFormattedAmount(getScore());
        return Integer.toString(Math.round(getScore()) );
    }
    */

    
    
    public float getOverallRawScoreToShow()
    {
        String tpVal = IvrStringUtils.getTagValue( textParam1, Constants.OVERRIDESHOWRAWSCOREKEY );
        
        if( tpVal!=null && !tpVal.isEmpty() )
        {
            try
            {
                return Float.parseFloat( tpVal );
            }
            catch( NumberFormatException e )
            {
                LogService.logIt( e, "TestEventScore.getOverallRawScoreToShow() Unable to parse detected value=" + tpVal + ", returning recorded raw score=" + this.getRawScore() + ", " + toString() );
            }
        }
        
        return this.getRawScore();
    }
    
    public int getScorePrecisionDigits()
    {
        return getScoreFormatType().getScorePrecisionDigits();
    }

    

    public String getTestEventScoreIdEncrypted()
    {
        return encryptStr( this.testEventScoreId );
    }

    public String getTestEventIdEncrypted()
    {
        return encryptStr( this.testEventId );
    }

    private String encryptStr( long id )
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( Long.valueOf(id).toString() );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "TestEventScore.enryptStr() " + toString() );

            return "";
        }
    }

    public boolean getHasValidNorms()
    {
        return getHasValidOverallNorm() ||
            getHasValidCountryNorm() ||
            getHasValidAccountNorm();
    }

    public boolean getHasValidOverallZScoreNorm()
    {
        return getOverallZScorePercentileValid()==1f;
    }
    
    public boolean getHasValidOverallNorm()
    {
        return percentile>=0 && ( overallPercentileCount>Constants.MIN_PERCENTILE_COUNT || overallPercentileCount==0 );
    }

    public boolean getHasValidCountryNorm()
    {
        return countryPercentile>=0 && ( countryPercentileCount>Constants.MIN_PERCENTILE_COUNT || countryPercentileCount==0 );
    }

    public boolean getHasValidAccountNorm()
    {
        return accountPercentile>=0 && ( accountPercentileCount>Constants.MIN_PERCENTILE_COUNT || accountPercentileCount==0 );
    }

    public void populatePercentileObj( Percentile p )
    {
        p.setTestEventScoreId(testEventScoreId);
        p.setTestEventScoreTypeId( testEventScoreTypeId );
        p.setScore(score);
        p.setRawScore(rawScore);
        p.setSimletId(simletId);
        p.setSimletVersionId(simletVersionId);
        p.setSimletCompetencyId(simletCompetencyId);
        p.setSimCompetencyId(simCompetencyId);


    }

    public float getOverallZScorePercentile() {
        return totalUsed;
    }

    public void setOverallZScorePercentile(float f) {
        this.totalUsed = f;
    }

    public float getOverallZScorePercentileValid() {
        return fractionUsed;
    }

    public void setOverallZScorePercentileValid(float f) {
        this.fractionUsed = f;
    }
    
    public List<CaveatScore> getCaveatScoreList()
    {
        return getCaveatScoreList(false, false );
    }
    public List<CaveatScore> getTopicCaveatScoreList()
    {
        return getCaveatScoreList(true, false );
    }
    public List<CaveatScore> getNonTopicCaveatScoreList()
    {
        return getCaveatScoreList(false, true );
    }


    private List<CaveatScore> getCaveatScoreList(boolean topicsOnly, boolean nonTopicsOnly )
    {
        List<CaveatScore> out = new ArrayList<>();

        if( textParam1 == null || textParam1.isEmpty() )
            return out;

        int ct = 1;

        String csb = StringUtils.getBracketedArtifactFromString( textParam1 , Constants.CAVEAT2_KEY + ct );
        CaveatScore cs ;
        
        while( csb!=null && !csb.isBlank() && ct<30 )
        {            
            cs = new CaveatScore(out.size()+1, csb,null);
            if( cs.getHasValidInfo() )
            {
                if( (topicsOnly && cs.getIsTopic()) || (nonTopicsOnly && !cs.getIsTopic()) || (!topicsOnly && !nonTopicsOnly) )
                    out.add( cs );
            }
            ct++;
            csb = StringUtils.getBracketedArtifactFromString( textParam1 , Constants.CAVEAT2_KEY + ct );
        } 
        
        // Check for legacy and convert if needed.
        if( out.isEmpty() && textParam1.contains(Constants.CAVEATSKEY) )
        {
            String cl = StringUtils.getBracketedArtifactFromString( textParam1 , Constants.CAVEATSKEY );
            if( cl == null || cl.isEmpty() )
                return out;

            // Topic
            if( (cl.contains("TOPIC") || cl.contains( "NOTOPIC")) && (topicsOnly || (!topicsOnly && !nonTopicsOnly)) )
                out.addAll( ReportUtils.getTopicCaveatScoreListFromLegacyCaveatStr(cl) );
            
            //Not a Topic
            else if( nonTopicsOnly || (!topicsOnly && !nonTopicsOnly) )
                out.addAll( ReportUtils.getLegacyCaveatScoreListFromLegacyCaveatStr(cl));
        }

        return out;
    }
    

    /*
    public List<String> getCaveatList()
    {
        List<String> out = new ArrayList<>();

        if( textParam1 == null || textParam1.isEmpty() )
            return out;

        String cl = StringUtils.getBracketedArtifactFromString( textParam1 , Constants.CAVEATSKEY );

        if( cl == null || cl.isEmpty() )
            return out;

        STStringTokenizer st = new STStringTokenizer( cl , Constants.DELIMITER );

        String t;

        while( st.hasMoreTokens() )
        {
            t = st.nextToken();

            t = t.trim();

            if( t.isEmpty() )
                continue;

            out.add( t );
        }

        return out;
    }
    */

    public List<ScoreCategoryRange> getScoreCatInfoList()
    {
        List<ScoreCategoryRange> out = new ArrayList<>();

        if( textParam1 == null || textParam1.isEmpty() )
            return out;

        String cl = StringUtils.getBracketedArtifactFromString(textParam1 , Constants.CATEGORYINFOKEY );

        if( cl == null || cl.isEmpty() )
            return out;

        String[] sl = cl.split( "~" );

        ScoreCategoryRange scr;

        for( String s : sl )
        {
            s = s.trim();

            if( s.isEmpty() )
                continue;

            scr =  new ScoreCategoryRange( s );

            if( scr.getIsValid() )
                out.add( scr );
        }

        return out;
    }

    public List<ScoreCategoryRange> getScoreCatInfoListForCat( int tgtScoreCategoryTypeId )
    {
        List<ScoreCategoryRange> out = new ArrayList<>();

        for( ScoreCategoryRange scr : getScoreCatInfoList() )
        {
            if( scr.getScoreCategoryTypeId()==tgtScoreCategoryTypeId )
                out.add( scr );
        }

        return out;
    }




    public TestEventScoreType getTestEventScoreType()
    {
        return TestEventScoreType.getValue( this.testEventScoreTypeId );
    }


    public boolean getHasTextResponses()
    {
        return getTextBasedResponseList( null, false ).size()>0 || getTextBasedResponseList( null, true ).size()>0;
    }


    public boolean getHasExperienceOrInterestResponses()
    {
        return getTextBasedResponseList( NonCompetencyItemType.EXPERIENCE_TASK1.getTitle(), false ).size()>0 ||
               getTextBasedResponseList( NonCompetencyItemType.INTEREST_TASK1.getTitle(), false ).size()>0;
    }


    public List<TextAndTitle> getTextBasedResponseList( String title, boolean includeFileUploads )
    {
        return getTextBasedResponseList( title, includeFileUploads, false );
    }


    public List<TextAndTitle> getTextBasedResponseList( String title, boolean includeFileUploads, boolean fileUploadsOnly )
    {
        List<TextAndTitle> out = new ArrayList<>();

        if( textbasedResponses==null || textbasedResponses.isEmpty() )
            return out;

        int i;

        String ky;

        String t = textbasedResponses;

        if( title != null && !title.isEmpty() )
        {
            ky =  ";;;" + title + ";;;" + Constants.DELIMITER;

            i = textbasedResponses.indexOf( ";;;" + title + ";;;" + Constants.DELIMITER );

            if( i < 0 )
                return out;

            t = textbasedResponses.substring( i + ky.length() );
            
            // remove any following titles
            if( t.contains(";;;") )
                t = t.substring(0, t.indexOf(";;;") );
        }

        if( t.length()==0 )
            return out;

        return parseTextBasedResponseList( t,  includeFileUploads );
    }


    public List<TextAndTitle> parseTextBasedResponseList( String txtBsdResp, boolean includeFileUploads )
    {
        // LogService.logIt( "TestEventScore.parseTextBasedResponseList() AAA " + name + ", Prepared txtBsdResp=" + txtBsdResp );
        
        List<TextAndTitle> out = new ArrayList<>();

        if( txtBsdResp==null || txtBsdResp.isEmpty() )
            return out;

        String t = txtBsdResp;

        // the relevant title (if any) should be removed by this point, but be sure to remove any following titles and content.
        if( t.contains(";;;") )
            t = t.substring(0, t.indexOf( ";;;" ) );

        if( t.length()==0 )
            return out;

        STStringTokenizer st = new STStringTokenizer( t, Constants.DELIMITER );

        String q,a,r;

        boolean upld;

        long uufid;
        int sequenceId;
        
        String string1;
        String string2;
        String string3;
        String string4;

        while( st.hasMoreTokens() )
        {
            q = st.nextToken();

            a = st.hasMoreTokens()? st.nextToken() : null;

            // This will catch UNSCORED uploads only.
            upld =  a != null && a.startsWith( "UPLOAD:" );

            if( upld && !includeFileUploads )
                continue;

            r = st.hasMoreTokens()? st.nextToken() : null;

            uufid = 0;
            sequenceId=0;

            if( r != null && r.contains("uuf:") )
            {
                int idx = r.indexOf(",", r.indexOf( "uuf:" )+4 );
                if( idx<0 )
                    idx = r.length();
                uufid = Long.parseLong( r.substring( r.indexOf( "uuf:" )+4, idx ) );
            }
            
            if( r != null && r.contains("seq:") )
            {
                int idx = r.indexOf(",", r.indexOf( "seq:" )+4 );
                if( idx<0 )
                    idx = r.length();
                sequenceId = Integer.parseInt( r.substring( r.indexOf( "seq:" )+4, idx ) );
            }
            
            // LogService.logIt( "TestEventScore.parseTextBasedResponseList() txtBsdResp=" + txtBsdResp + ", uplod=" + upld + ", uufid=" + uufid + ", r=" + r + ", includeFileUploads=" + includeFileUploads + ", simCompetencyClassId=" + simCompetencyClassId );
            
            //if(     uufid>0 && 
            //        !includeFileUploads && 
            //        simCompetencyClassId!=SimCompetencyClass.SCOREDAUDIO.getSimCompetencyClassId()  )
            //    continue;
            
            string1 = st.hasMoreTokens()? st.nextToken() : null;
            
            string2 = null;
            string3 = null;
            string4 = null;
            if( string1!=null && !string1.isEmpty() )
            {
                try
                {
                    if( string1.contains("~") )
                    {
                        String[] ds = string1.split("~");
                        string1 = ds.length>0 ? ds[0] : "";
                        if( ds.length>1 )
                            string2 = ds[1];
                        if( ds.length>2 )
                            string3 = ds[2];
                        if( ds.length>3 )
                            string4 = ds[3];
                    }
                    //LogService.logIt( "TestResultUtils.getTextBasedResponseList() parsed string1=" + string1 );
                }
                catch( Exception e )
                {
                    LogService.logIt( "TestEventScore.getTextBasedResponseList() URLDecoding DDD string1 error. NON-FATAL  error=" + e.toString() + ", decoding: string1=" + string1 );
                } 
            }            

            // if( q != null && a != null && a.trim().length()>0)
            if( q != null && a != null )
                out.add(new TextAndTitle( a, q, r!=null && r.indexOf( "red:1")>=0, uufid, sequenceId, string1, string2, string3, string4 ) );
        }

        return out;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestEventScore other = (TestEventScore) obj;
        if (this.testEventScoreId != other.testEventScoreId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (this.testEventScoreId ^ (this.testEventScoreId >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "TestEventScore{" + "testEventScoreId=" + testEventScoreId + ", testEventId=" + testEventId + ", testEventScoreTypeId=" + testEventScoreTypeId + ", scoreCategoryId=" + scoreCategoryId + ", name=" + name + '}';
    }


    @Override
    public void appendErrorTxt( String t )
    {
        if( t == null )
            return;

        if( errorTxt == null )
            errorTxt = t;

        else if( t != null )
            errorTxt = t + "\n" + errorTxt;

        if( errorTxt != null && errorTxt.length()>1000 )
            errorTxt = errorTxt.substring(0,1000 );
    }





    @Override
    public int compareTo(TestEventScore o) {

        if( displayOrder != o.getDisplayOrder() )
            return ( (Integer) displayOrder ).compareTo(  o.getDisplayOrder() );

        if( name != null && !name.isEmpty() && o.getName() != null && !o.getName().isEmpty() )
            return name.compareTo( o.getName() );

        return ( (Long) this.testEventScoreId ).compareTo( o.getTestEventScoreId() );
    }


    public boolean getHasReport()
    {
        return reportBytes != null && reportBytes.length > 0;
    }

    public ScoreCategoryType getScoreCategoryType()
    {
        return ScoreCategoryType.getType( scoreCategoryId );
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRawScore() {
        return rawScore;
    }

    public void setRawScore(float rawScore) {
        this.rawScore = rawScore;
    }

    public byte[] getReportBytes() {
        return reportBytes;
    }

    public void setReportBytes(byte[] reportBytes) {
        this.reportBytes = reportBytes;
    }

    public int getReportFileContentTypeId() {
        return reportFileContentTypeId;
    }

    public void setReportFileContentTypeId(int reportFileContentTypeId) {
        this.reportFileContentTypeId = reportFileContentTypeId;
    }

    public String getReportFilename() {
        return reportFilename;
    }

    public void setReportFilename(String reportFilename) {
        this.reportFilename = reportFilename;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getScoreText() {
        return scoreText;
    }

    public void setScoreText(String scoreText) {
        this.scoreText = scoreText;
    }

    public TestEvent getTestEvent() {
        return testEvent;
    }

    public void setTestEvent(TestEvent testEvent) {
        this.testEvent = testEvent;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public long getTestEventScoreId() {
        return testEventScoreId;
    }

    public void setTestEventScoreId(long testEventScoreId) {
        this.testEventScoreId = testEventScoreId;
    }

    public int getTestEventScoreTypeId() {
        return testEventScoreTypeId;
    }

    public void setTestEventScoreTypeId(int testEventScoreTypeId) {
        this.testEventScoreTypeId = testEventScoreTypeId;
    }

    public int getScoreCategoryId() {
        return scoreCategoryId;
    }

    public void setScoreCategoryId(int scoreCategoryId) {
        this.scoreCategoryId = scoreCategoryId;
    }

    public ScoreFormatType getScoreFormatType()
    {
        return ScoreFormatType.getValue(scoreFormatTypeId);
    }

    public int getScoreFormatTypeId() {
        return scoreFormatTypeId;
    }

    public void setScoreFormatTypeId(int scoreFormatTypeId) {
        this.scoreFormatTypeId = scoreFormatTypeId;
    }

    public String getTextbasedResponses() {
        return textbasedResponses;
    }


    public FormatCompetency getFormatCompetency(boolean forEmail)
    {
        int wid = forEmail ? Constants.CT2_COLORGRAPHWID_EML : Constants.CT2_COLORGRAPHWID;

        if( this.formatCompetency == null )
            formatCompetency = new FormatCompetency( this, wid );

        return formatCompetency;
    }


    public List<ScoreCategoryRange> getScoreCatInfoList( int totalRangeWid )
    {
        List<ScoreCategoryRange> out = new ArrayList<>();

        if( textParam1 == null || textParam1.isEmpty() )
            return out;

        String cl = StringUtils.getBracketedArtifactFromString(textParam1 , Constants.CATEGORYINFOKEY );

        // LogService.logIt( "TestEventScore.getScoreCatInfoList() totalRangeWid=" + totalRangeWid + ", cl=" + cl);
        
        if( cl == null || cl.isEmpty() )
            return out;

        String[] sl = cl.split( "~" );

        ScoreCategoryRange scr;

        for( String s : sl )
        {
            s = s.trim();

            if( s.isEmpty() )
                continue;

            scr = new ScoreCategoryRange( s, totalRangeWid );

            if( scr.getIsValid() )
                out.add( scr );
        }

        return out;
    }





    public List<InterviewQuestion> getInterviewQuestionList( int max )
    {
        List<InterviewQuestion> iql = new ArrayList<>();

        // LogService.logIt( "TestEventScore.getInterviewQuestionList() AAA tes.name=" + name + ", interviewQuestions=" + interviewQuestions );
        if( interviewQuestions == null || interviewQuestions.isEmpty() )
            return iql;

        STStringTokenizer st = new STStringTokenizer( interviewQuestions, Constants.DELIMITER );

        // LogService.logIt( "TestEventScore.getInterviewQuestionList() AAA Tokens found=" + st.countTokens() );

        InterviewQuestion iq;

        String tq = null;

        while( st.hasMoreTokens() )
        {
            iq = new InterviewQuestion();

            iq.setTestEventScore( this );

            iq.setQuestion( tq==null ? st.nextToken() : tq );

            tq = null;

            // LogService.logIt( "TestEventScore.getInterviewQuestionList() BBB Question=" + iq.getQuestion() );

            if( st.hasMoreTokens() )
                iq.setAnchorHi( st.nextToken() );

            if( st.hasMoreTokens() )
                iq.setAnchorMed( st.nextToken() );

            if( st.hasMoreTokens() )
                iq.setAnchorLow( st.nextToken() );

            if( st.hasMoreTokens() )
            {
                // In old format, this could be the next question.
                tq = st.nextToken();

                try
                {
                    // if this works, it's breadth.
                    iq.setScoreBreadth( Integer.parseInt( tq ) );
                    tq=null;
                }
                catch( NumberFormatException e )
                {}
            }

            if( iq.getQuestion()!=null && !iq.getQuestion().isEmpty())
            {
                iql.add( iq );
            }

            //if( max > 0 && out.size() >= max )
            //    return out;
        }
        
        // LogService.logIt( "TestEventScore.getInterviewQuestionList() CCC iql.size=" + iql.size() );
        

        return InterviewQuestionBreadthType.getInterviewQuestionForScoreAndCategory( getScoreCategoryType(), getScoreFormatType(), score, iql, max );
    }

    public void setTextbasedResponses(String t) {

        if( t!=null && t.isBlank() )
            t=null;

        this.textbasedResponses = t;
    }

    public String getInterviewQuestions() {
        return interviewQuestions;
    }

    public void setInterviewQuestions(String t) {

        if( t != null && t.trim().isEmpty() )
            t = null;

        this.interviewQuestions = t;
    }

    public int getHide() {
        return hide;
    }

    public void setHide(int hide) {
        this.hide = hide;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public int getTestEventScoreStatusTypeId() {
        return testEventScoreStatusTypeId;
    }

    public void setTestEventScoreStatusTypeId(int testEventScoreStatusTypeId) {
        this.testEventScoreStatusTypeId = testEventScoreStatusTypeId;
    }


    public SimCompetencyClass getSimCompetencyClass()
    {
        return SimCompetencyClass.getValue( simCompetencyClassId );
    }


    @Override
    public String getErrorTxt() {
        return errorTxt;
    }

    @Override
    public void setErrorTxt(String e) {

        this.errorTxt = e;
    }

    public float getTotalUsed() {
        return totalUsed;
    }

    public void setTotalUsed(float totalUsed) {
        this.totalUsed = totalUsed;
    }

    
    public float getMaxValueUsed() {
        return maxValueUsed;
    }

    public void setMaxValueUsed(float maxValueUsed) {
        this.maxValueUsed = maxValueUsed;
    }

    public float getFractionUsed() {
        return fractionUsed;
    }

    public void setFractionUsed(float fractionUsed) {
        this.fractionUsed = fractionUsed;
    }

    

    public float getMean() {
        return mean;
    }

    public void setMean(float mean) {
        this.mean = mean;
    }

    public float getStdDeviation() {
        return stdDeviation;
    }

    public void setStdDeviation(float stdDeviation) {
        this.stdDeviation = stdDeviation;
    }

    public int getScoreTypeIdUsed() {
        return scoreTypeIdUsed;
    }

    public void setScoreTypeIdUsed(int scoreTypeIdUsed) {
        this.scoreTypeIdUsed = scoreTypeIdUsed;
    }

    public int getSimCompetencyClassId() {
        return simCompetencyClassId;
    }

    public void setSimCompetencyClassId(int simCompetencyClassId) {
        this.simCompetencyClassId = simCompetencyClassId;
    }

    public long getSimCompetencyId() {
        return simCompetencyId;
    }

    public void setSimCompetencyId(long simCompetencyId) {
        this.simCompetencyId = simCompetencyId;
    }

    public int getScorableItemResponses() {
        return scorableItemResponses;
    }

    public void setScorableItemResponses(int scorableItemResponses) {
        this.scorableItemResponses = scorableItemResponses;
    }

    public float getPercentile() {
        return percentile;
    }

    public void setPercentile(float percentile) {
        this.percentile = percentile;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public float getAccountPercentile() {
        return accountPercentile;
    }

    public void setAccountPercentile(float accountPercentile) {
        this.accountPercentile = accountPercentile;
    }

    public float getCountryPercentile() {
        return countryPercentile;
    }

    public void setCountryPercentile(float countryPercentile) {
        this.countryPercentile = countryPercentile;
    }

    public String getTextParam1() {
        return textParam1;
    }

    public void setTextParam1(String textParam1) {
        this.textParam1 = textParam1;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getOverallPercentileCount() {
        return overallPercentileCount;
    }

    public void setOverallPercentileCount(int overallPercentileCount) {
        this.overallPercentileCount = overallPercentileCount;
    }

    public int getAccountPercentileCount() {
        return accountPercentileCount;
    }

    public void setAccountPercentileCount(int accountPercentileCount) {
        this.accountPercentileCount = accountPercentileCount;
    }

    public int getCountryPercentileCount() {
        return countryPercentileCount;
    }

    public void setCountryPercentileCount(int countryPercentileCount) {
        this.countryPercentileCount = countryPercentileCount;
    }

    public float getScore2() {
        return score2;
    }

    public void setScore2(float score2) {
        this.score2 = score2;
    }

    public float getScore3() {
        return score3;
    }

    public void setScore3(float score3) {
        this.score3 = score3;
    }

    public float getScore4() {
        return score4;
    }

    public void setScore4(float score4) {
        this.score4 = score4;
    }

    public float getScore5() {
        return score5;
    }

    public void setScore5(float score5) {
        this.score5 = score5;
    }

    public float getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(float totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public float getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(float totalPoints) {
        this.totalPoints = totalPoints;
    }

    public float getMaxTotalCorrect() {
        return maxTotalCorrect;
    }

    public void setMaxTotalCorrect(float maxTotalCorrect) {
        this.maxTotalCorrect = maxTotalCorrect;
    }

    public float getMaxTotalPoints() {
        return maxTotalPoints;
    }

    public void setMaxTotalPoints(float maxTotalPoints) {
        this.maxTotalPoints = maxTotalPoints;
    }

    public float getTotalScorableItems() {
        return totalScorableItems;
    }

    public void setTotalScorableItems(float totalScorableItems) {
        this.totalScorableItems = totalScorableItems;
    }

    public long getSimletId() {
        return simletId;
    }

    public void setSimletId(long simletId) {
        this.simletId = simletId;
    }

    public int getSimletVersionId() {
        return simletVersionId;
    }

    public void setSimletVersionId(int simletVersionId) {
        this.simletVersionId = simletVersionId;
    }

    public long getSimletCompetencyId() {
        return simletCompetencyId;
    }

    public void setSimletCompetencyId(long simletComeptencyId) {
        this.simletCompetencyId = simletComeptencyId;
    }

    public float[] getProfileBoundaries() {
        return profileBoundaries;
    }

    public void setProfileBoundaries(float[] profileBoundaries) {
        this.profileBoundaries = profileBoundaries;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameenglish) {
        this.nameEnglish = nameenglish;
    }

    public boolean getIncludeNumericScoreInResults() {
        return hideNumericScore==0;
    }


    public int getHideNumericScore() {
        return hideNumericScore;
    }

    public void setHideNumericScore(int hideNumericScore) {
        this.hideNumericScore = hideNumericScore;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }

    public long getLongParam1() {
        return longParam1;
    }

    public void setLongParam1(long longParam1) {
        this.longParam1 = longParam1;
    }

    public long getLongParam2() {
        return longParam2;
    }

    public void setLongParam2(long longParam2) {
        this.longParam2 = longParam2;
    }

    public String getPercentileCountry() {
        return percentileCountry;
    }

    public void setPercentileCountry(String percentileCountry) {
        this.percentileCountry = percentileCountry;
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

    public boolean isTempBoolean1() {
        return tempBoolean1;
    }

    public void setTempBoolean1(boolean tempBoolean1) {
        this.tempBoolean1 = tempBoolean1;
    }

    public float getScore6() {
        return score6;
    }

    public void setScore6(float score6) {
        this.score6 = score6;
    }

    public float getScore7() {
        return score7;
    }

    public void setScore7(float score7) {
        this.score7 = score7;
    }

    public float getScore8() {
        return score8;
    }

    public void setScore8(float score8) {
        this.score8 = score8;
    }

    public float getScore9() {
        return score9;
    }

    public void setScore9(float score9) {
        this.score9 = score9;
    }

    public float getScore10() {
        return score10;
    }

    public void setScore10(float score10) {
        this.score10 = score10;
    }

    public float getScore11() {
        return score11;
    }

    public void setScore11(float score11) {
        this.score11 = score11;
    }

    public float getScore12() {
        return score12;
    }

    public void setScore12(float score12) {
        this.score12 = score12;
    }

    public Date getDateParam1() {
        return dateParam1;
    }

    public void setDateParam1(Date dateParam1) {
        this.dateParam1 = dateParam1;
    }

    public Date getDateParam2() {
        return dateParam2;
    }

    public void setDateParam2(Date dateParam2) {
        this.dateParam2 = dateParam2;
    }

    public float getScore13()
    {
        return score13;
    }

    public void setScore13(float score13)
    {
        this.score13 = score13;
    }

    public float getScore14()
    {
        return score14;
    }

    public void setScore14(float score14)
    {
        this.score14 = score14;
    }

    public float getScore15()
    {
        return score15;
    }

    public void setScore15(float score15)
    {
        this.score15 = score15;
    }




}
