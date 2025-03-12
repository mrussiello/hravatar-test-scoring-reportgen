package com.tm2score.event;

import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.battery.BatteryScoreStatusType;
import com.tm2score.entity.event.*;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.*;
import javax.sql.DataSource;

@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class NormFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    //@PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    //EntityManager emmirror;


    public static NormFacade getInstance()
    {
        try
        {
            return (NormFacade) InitialContext.doLookup( "java:module/NormFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NormFacade.getInstance() " );

            return null;
        }
    }



    public float[] getBatteryPercentile( Battery battery, BatteryScore bs, int orgId, String countryCode) throws Exception
    {
        Map<String,Object> d = getNormData(battery, bs, orgId, countryCode );

        Integer ttl = (Integer) d.get( "totalall" );

        Float f =  (Float) d.get( "percent" );

        float[] out = new float[2];

        if( ttl != null && f != null && ttl.intValue() >= Constants.MIN_NORM_COUNT_FOR_BATTERY )
            out[0] = f.floatValue();

        else
            out[0] = -1;

        out[1] = ttl;

        // LogService.logIt("NormFacade.getBatteryPercentile() Found ttl of " + ttl.intValue() + " results for norm calculation. " + bs.toString() );

        return out;
    }


    public Percentile getExistingPercentileRecordForTestEvent( TestEvent testEvent, TestEventScore tes ) throws Exception
    {
        try
        {
            if( testEvent==null )
                throw new Exception( "TestEvent is required. Was null." );
            
            if( tes==null )
                throw new Exception( "TestEventScore is required. Was null." );
            
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();
            TypedQuery<Percentile> q = em.createNamedQuery( "Percentile.findByTestEventIdTestEventScoreId", Percentile.class );

            q.setParameter( "testEventId", testEvent.getTestEventId() );
            q.setParameter( "testEventScoreId", tes.getTestEventScoreId() );

            return q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NormFacade.getExistingPercentileRecordForTestEvent() " + (testEvent==null ? "testEvent is null" :  testEvent.toString()) + ", " + (tes==null ? "TestEventScore is null" : tes.toString() )  );

            throw new STException( e );
        }

    }

    public boolean hasValidPercentiles( Map<String,Object> o, int orgId, String countryCode )
    {
        if( o==null || o.get("percentile")==null )
            return false;
        
        if( ((Float)o.get("percentile"))<0 )
            return false;
        
        else if( orgId>0 && ( o.get("percentileorg")==null || ((Float)o.get("percentileorg"))<0) )
            return false;
        
        else if( countryCode!=null && !countryCode.isBlank() && ( o.get("percentilecc")==null || ((Float)o.get("percentilecc"))<0) )
            return false;
                
        return true;
    }

    public void combinePercentileValues( Map<String,Object> o, Map<String,Object> o2 )
    {
        if( o==null || o2==null || o.get("percentile")==null || o2.get("percentile")==null)
            return;
        
        LogService.logIt( "NormFacade.combinePercentileValues() o.percentile=" + ((Float)o.get("percentile")) + " o2.percentile=" + ((Float)o2.get("percentile")) + ", o2.count="+ ((Integer)o.get("count"))  );
        
        if( ((Float)o.get("percentile"))<0 && ((Float)o2.get("percentile"))>=0)
        {
            o.put("percentile", o2.get("percentile"));
            o.put("count", o2.get("count"));
        }

        if( o.get("percentileorg")!=null && ((Float)o.get("percentileorg"))<0 && ((Float)o2.get("percentileorg"))>=0 )
        {
            o.put("percentileorg", o2.get("percentileorg"));
            o.put("countorg", o2.get("countorg"));
        }
        
        if( o.get("percentilecc")!=null && ((Float)o.get("percentilecc"))<0 && ((Float)o2.get("percentilecc"))>=0 )
        {
            o.put("percentilecc", o2.get("percentilecc"));
            o.put("countcc", o2.get("countcc"));
        }

    }

    
    
    /**
     * Returns percentile  Float  This value is -1 if there is not enough records.
     *         count       Integer  the actual count found.
     *         versionIdUsed   Integer - this is the sim versionid that was used for the data provided. 
     * 
     *         percentileorg
     *         countorg
     * 
     *         percentilecc
     *         countcc
     * 
     * @param productId
     * @param testEvent
     * @param tes
     * @param orgId
     * @param countryCode
     * @return
     * @throws Exception 
     */
    public Map<String,Object> getPercentile( int productId, int percentileScoreTypeId, long simId, int simVersionId, long testEventId, int minSimVersionIdForMajorVersion, TestEventScore tes, int orgId, String countryCode, String custom1, String custom2, String custom3) throws Exception
    {
        // First, look at hits WITH sim versionid. this forces to look at current simversionid only.
        Map<String,Object> d = getNormData(percentileScoreTypeId, simId, simVersionId, testEventId, tes, orgId, countryCode, custom1, custom2, custom3, false, true, 0);

        Integer ttl = (Integer) d.get( "totalall" );
        Float f =  (Float) d.get( "percent" );
        Integer vusd = (Integer) d.get( "versionused" );

	if( ttl>=20000) 
            LogService.logIt("NormFacade.getPercentile() AA Found count of " + ttl.intValue() + " results, version used=" + vusd + ", percentile=" + f + " for norm calculation. " + tes.toString() );

        Map<String,Object> out = new HashMap<>();

        out.put( "count",  ttl);      
        out.put( "percentile", (float)-1 );
        out.put( "versionused", vusd );

        out.put( "countorg", (Integer) d.get( "totalorg" ) );
        out.put( "countcc", (Integer) d.get( "totalcc" ) );        
        out.put( "percentileorg", (float) -1 );
        out.put( "percentilecc", (float) -1 );
        
                
        // If found enough hits with current version.  
        // If versionid = 1, we only need to look for the all versions count, since that will be used in a second query if we look for the version count. 
        int minToUse = simVersionId>1 ? Constants.MIN_NORM_COUNT_FOR_TEST_VERSION : Constants.MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS;
        
        if( ttl!=null && f!=null && ttl>=minToUse )
        {
            // LogService.logIt( "NormFacade.getPercentile() BBB.1 testEventId=" + testEvent.getTestEventId() +", testEventScore.name=" + tes.getName() + ", minToUse=" + minToUse + ", ttl=" + ttl );
            out.put( "percentile", f);

            ttl = (Integer) d.get( "totalorg" );                
            if( ttl!=null && ttl>=minToUse )
                out.put( "percentileorg", (Float) d.get( "percentorg" ) );

            ttl = (Integer) d.get( "totalcc" );
            if( ttl!=null && ttl>=minToUse )
                out.put( "percentilecc", (Float) d.get( "percentcc" ) );
            
            return out;
        }

        // Jump down to min for this Major Version.
        int curVersionId = minSimVersionIdForMajorVersion; 
        
        // If there are versions below
        if( curVersionId>0 && curVersionId<simVersionId )
        {
            minToUse = Constants.MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS;
            
            d = getNormData(percentileScoreTypeId, simId, simVersionId, testEventId, tes, orgId, countryCode, custom1, custom2, custom3, false, true, curVersionId);

            ttl = (Integer) d.get( "totalall" );
            f =  (Float) d.get( "percent" );
            vusd = (Integer) d.get( "versionused" );

            out.put( "versionused", vusd );
            out.put( "count",  ttl);
            out.put( "countorg", (Integer) d.get( "totalorg" ) );
            out.put( "countcc", (Integer) d.get( "totalcc" ) );        

            // Use the bigger value until we get down to the bottom of the Major version, then use the smaller value. This helps us avoid going below the major version.
            // If we have enough hits, return 
            // if( ttl != null && f != null && ttl.intValue() >= (curVersionId > minSimVersionIdForMajorVersion ? Constants.MIN_NORM_COUNT_FOR_TEST_VERSION : Constants.MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS ) )
            // Use the bigger value until we get down to the bottom of the Major version, then use the smaller value. This helps us avoid going below the major version.
            // If we have enough hits, return 
            // if( ttl != null && f != null && ttl.intValue() >= (curVersionId > 1 ? Constants.MIN_NORM_COUNT_FOR_TEST_VERSION : Constants.MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS ) )
            if( ttl != null && f!=null && ttl>=minToUse )
            {
                // LogService.logIt( "NormFacade.getPercentile() CCC.1 testEventId=" + testEvent.getTestEventId() +", testEventScore.name=" + tes.getName() + ", minToUse=" + minToUse + ", ttl=" + ttl );
                
                out.put( "percentile", f);
                
                ttl = (Integer) d.get( "totalorg" );                
                if( ttl != null && ttl >= minToUse )
                    out.put( "percentileorg", (Float) d.get( "percentorg" ) );
                
                ttl = (Integer) d.get( "totalcc" );
                if( ttl != null && ttl >= minToUse )
                    out.put( "percentilecc", (Float) d.get( "percentcc" ) );
                
                return out;
            }
        }  

        // look for minimal hits without using sim versionid
        minToUse = Constants.MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS;
        
        d = getNormData(percentileScoreTypeId, simId, simVersionId, testEventId, tes, orgId, countryCode, custom1, custom2, custom3, false, false, 0);

        ttl = (Integer) d.get( "totalall" );
        f =  (Float) d.get( "percent" );
        vusd = 0;

        out.put( "count",  ttl);
        out.put( "countorg", (Integer) d.get( "totalorg" ) );
        out.put( "countcc", (Integer) d.get( "totalcc" ) );        

        if( ttl!=null && f!=null && ttl>=minToUse )
        {
            // LogService.logIt( "NormFacade.getPercentile() DDD.1 testEventId=" + testEvent.getTestEventId() +", testEventScore.name=" + tes.getName() + ", minToUse=" + minToUse + ", ttl=" + ttl );
            
            out.put( "percentile", f);

            ttl = (Integer) d.get( "totalorg" );                
            if( ttl != null && ttl>=minToUse )
                out.put( "percentileorg", (Float) d.get( "percentorg" ) );

            ttl = (Integer) d.get( "totalcc" );
            if( ttl != null && ttl>=minToUse )
                out.put( "percentilecc", (Float) d.get( "percentcc" ) );
        }
        
        out.put( "versionused", (int) vusd );
        
        return out;
            
         
        /*
            // int curVersionId = testEvent.getSimVersionId() - 1; // versionIncrement;
            //if( curVersionId)

            while( curVersionId>0 ) // && curVersionId>=minSimVersionIdForMajorVersion )
            {
                d = getNormData(productId, percentileScoreTypeId, testEvent , tes, orgId, countryCode, custom1, custom2, custom3, false, true, curVersionId );

                ttl = (Integer) d.get( "totalall" );

                f =  (Float) d.get( "percent" );

                out.put( "count",  ttl);

                // Use the bigger value until we get down to the bottom of the Major version, then use the smaller value. This helps us avoid going below the major version.
                if( ttl != null && f != null && ttl.intValue() >= (curVersionId > minSimVersionIdForMajorVersion ? Constants.MIN_NORM_COUNT_FOR_TEST_VERSION : Constants.MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS ) )
                {
                    out.put( "percentile", f);
                    break;
                }

                else
                    out.put( "percentile", (float)(-1) );

                curVersionId -= 1;
            }

            // If we've covered ALL Versions and still don't have a percentile, then use the smaller standard.
            if( ((Float)out.get( "percentile" )).floatValue() == -1f )
            {
                // look for hits without using sim versionid
                d = getNormData( productId, percentileScoreTypeId, testEvent, tes, orgId, countryCode, custom1, custom2, custom3, false, true, 0 );

                ttl = (Integer) d.get( "totalall" );

                f =  (Float) d.get( "percent" );

                out.put( "count",  ttl);

                if( ttl != null && f != null && ttl.intValue() >= Constants.MIN_NORM_COUNT_FOR_TEST_ALLVERSIONS )
                    out.put( "percentile", f);

                else
                    out.put( "percentile", (float)(-1) );
            }

        } // calculateStandardPercentile( tes );

        //if( tes.getSimCompetencyId()==0)
        //    LogService.logIt( "NormFacade.getPercentile() orgId=" + orgId + ", Country=" + countryCode + " returning " + ((Float)out.get( "percentile" )).floatValue() + ", count=" + ((Integer)out.get( "count" )).intValue() );
        
        return out;
        */
    }


    /*
    private float calculateStandardPercentile( TestEventScore tes )
    {

        ScoreFormatType sft = ScoreFormatType.getValue( tes.getScoreFormatTypeId() );

        return sft.getStandardPercentile( tes.getScore() );
    }


    private float calculateStandardPercentile( Battery b, BatteryScore bs )
    {

        ScoreFormatType sft = ScoreFormatType.getValue( b.getScoreFormatTypeId() );

        return sft.getStandardPercentile( bs.getScore() );
    }
    */






    /**
     * Returns a Map with the following keys:
     * 
     * versionused = INTEGER sim version id used
     * totalall = INTEGER number of test events used in the calculation.
     * totalunder = INTEGER number of test events that had score below or equal to the provided score (raw or not per below).
     * fraction = FLOAT - the fraction of totalunder/totalall
     * percent = FLOAT - a two decimal percentage.
     * 
     * totalorg - INTEGER number 
     * totalunderorg INTEGER
     * percentorg  FLOAT
     * 
     * totalcc  INTEGER
     * totalundercc INTEGER
     * percentcc FLOAT
     * 
     *
     * @param tes
     * @param orgId
     * @return
     * @throws Exception
     */
    private Map<String,Object> getNormData( int percentileScoreTypeId, long simId, int simVersionId, long testEventId, TestEventScore tes, int orgId, String countryCode, String custom1, String custom2, String custom3, boolean raw, boolean useSimVersionId, int minSimVersionId) throws Exception
    {
        boolean useScore2 = percentileScoreTypeId==PercentileScoreType.WEIGHTED_AVG_ZSCORES.getPercentileScoreTypeId() && RuntimeConstants.getBooleanValue( "UseScore2ForPercentiles" );

        // Simlets always use raw scores.
        if( tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) && tes.getSimletId()>0 && tes.getSimletVersionId()>0 && tes.getSimletCompetencyId()>0 )
            raw = true;
        
        String sqlStr = null;

        String s1 = null;
        
        // several counts
        //   overall all scores 
        //   overall for above tgt score
        //   if( orgid present ) account for all scores 
        //   if( orgid present ) account for above tgt score 
        //   if( countrycode present ) country for all scores 
        //   if( countrycode present ) country for above tgt score 

        float tgtScore = tes.getScore();
        
        String scoreCol = "p.score";
        if( raw )
        {
            scoreCol = "p.rawscore";
            tgtScore = tes.getRawScore();
        }
        else if( useScore2 )
            scoreCol = "p.score2";
        

        s1 = "SELECT COUNT(if(" + scoreCol + ">=0,1,null)) AS 'cnt', COUNT(if( " + scoreCol + "<=" + tgtScore + " AND " + scoreCol + ">=0,1,null)) AS 'cnt2'";            
        
        if( orgId>0 )
            s1 += ", COUNT(if(" + scoreCol + ">=0 AND p.orgid=" + orgId + ",1,null)) AS 'cnt3', COUNT(if( " + scoreCol + "<=" + tgtScore + " AND " + scoreCol + ">=0 AND p.orgid=" + orgId + ",1,null)) AS 'cnt4'";            

        if( countryCode!=null && !countryCode.isBlank() )
            s1 += ", COUNT(if(" + scoreCol + ">=0 AND p.ipcountry IS NOT NULL AND p.ipcountry='" + countryCode + "',1,null)) AS 'cnt5', COUNT(if( " + scoreCol + "<=" + tgtScore + " AND " + scoreCol + ">=0 AND p.ipcountry IS NOT NULL AND p.ipcountry='" + countryCode + "',1,null)) AS 'cnt6'";            
        
        //if( raw )
        //    s1 = "SELECT COUNT(if( p.rawscore>=0,1,null)) AS 'cnt', COUNT(if( p.rawscore<=" + tes.getRawScore() + " AND p.rawscore>=0,1,null)) AS 'cnt2'";
            
        //else if( useScore2 )
        //    s1 = "SELECT COUNT(if( p.score2>=0,1,null)) AS 'cnt', COUNT(if( p.score2<=" + tes.getScore() + " AND p.score2>=0,1,null)) AS 'cnt2'";        
        
        //else
        //    s1 = "SELECT COUNT(if( p.score>=0,1,null)) AS 'cnt', COUNT(if( p.score<=" + tes.getScore() + " AND p.score>=0,1,null)) AS 'cnt2'";
        
        s1 += " FROM percentile AS p ";

        int versionIdUsed = -1;
        
        String whereStr;
        
        String whereStr2 = "";

        if( tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) && tes.getSimletId()>0 && tes.getSimletVersionId()>0 && tes.getSimletCompetencyId()>0 )
        {
            whereStr = " WHERE p.simletid=" + tes.getSimletId() +
                           " AND p.simletversionid=" + tes.getSimletVersionId() +
                           " AND p.simletcompetencyid=" + tes.getSimletCompetencyId() + " ";
        }

        else //  if( testEvent !=null )
        {
            // There are three cases.
            // First, don't use sim version id -> this is used when we want to catch this for all sim versions.
            // second, minSimVersionId is > 0. In this case we look for all events for all test events that used this version or higher.
            // finally, if minSimVersionId=0 we will use the exact sim version.
            if( useSimVersionId )
            {
                if( minSimVersionId > 0 )
                {
                    whereStr = " WHERE p.simid=" + simId + " AND p.simversionid>=" + minSimVersionId + " AND p.simcompetencyid=" + tes.getSimCompetencyId() + " ";
                    versionIdUsed = minSimVersionId;
                }

                else
                {
                    whereStr = " WHERE p.simid=" + simId + " AND p.simversionid=" + simVersionId + " AND p.simcompetencyid=" + tes.getSimCompetencyId() + " ";
                    versionIdUsed = simVersionId;
                }
            }

            else
            {
                whereStr = " WHERE p.simid=" + simId + " AND p.simcompetencyid=" + tes.getSimCompetencyId() + " ";
                versionIdUsed = 0;
            }

        }
        
        whereStr += " AND p.testeventscoretypeid=" + tes.getTestEventScoreTypeId() + " ";
        

        //else
        //    whereStr = " WHERE p.productid=" + productId + " AND p.simcompetencyid=" + tes.getSimCompetencyId() + " ";

        whereStr += " AND p.testeventid<>" + testEventId + " ";
                        
        //if( orgId>0 )
        //    whereStr += " AND p.orgid=" + orgId + " ";

        
        //if( countryCode!=null && !countryCode.isEmpty() )
        //    whereStr += " AND p.ipcountry IS NOT NULL AND p.ipcountry='" + countryCode + "' ";

        if( custom1!=null )
            custom1 = StringUtils.sanitizeForSqlQuery(custom1);
        if( custom1!=null && !custom1.isEmpty() )
            whereStr += " AND p.custom1 IS NOT NULL AND p.custom1='" + custom1 + "' ";

        if( custom2!=null )
            custom2 = StringUtils.sanitizeForSqlQuery(custom2);
        if( custom2!=null && !custom2.isEmpty() )
            whereStr += " AND p.custom2 IS NOT NULL AND p.custom2='" + custom2 + "' ";

        if( custom3!=null )
            custom3 = StringUtils.sanitizeForSqlQuery(custom3);
        if( custom3!=null && !custom3.isEmpty() )
            whereStr += " AND p.custom3 IS NOT NULL AND p.custom3='" + custom3 + "' ";

        if( raw )
            whereStr2 = " AND p.scoretypeid=" + percentileScoreTypeId + " ";

        else
        {

            if( raw )
            {}
            
            // No change for new scoring since we use old and new.
            else if( percentileScoreTypeId==PercentileScoreType.WEIGHTED_AVG_ZSCORES.getPercentileScoreTypeId() )
            {
                //if( RuntimeConstants.getBooleanValue( "UseScore2ForPercentiles" ) )
                //{
                    // score2 can be less than 0 for invalid scores, which should never be counted.
                //    whereStr2 += " AND p.score2>=0 ";
                //}

                //else
                //{
                    // No need to set scoretypeid because this is done in later in the where clas
                //}    
            } 

            // only use old scoring for old scoring.
            else if( percentileScoreTypeId==PercentileScoreType.LEGACY.getPercentileScoreTypeId() )
                whereStr2 += " AND p.scoretypeid=0 ";

            // Something else, only use that type
            else
                whereStr2 += " AND p.scoretypeid=" + percentileScoreTypeId + " ";

            // OLD Approach
            // whereStr += " AND p.score<=" + tes.getScore() + " ";
        }
        
        
        
        sqlStr = s1 + whereStr + whereStr2;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        //String t;
        int total = 0;
        int under = 0;
        
        int totalorg = 0;
        int underorg = 0;
        
        int totalcc = 0;
        int undercc = 0;

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
            
            // LogService.logIt( "NormFacade.getNormData() AAAA " + sqlStr );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if(  rs.next() )
            {
                total = rs.getInt(1);
                under = rs.getInt(2);
                
                if( orgId>0 )
                {
                    totalorg = rs.getInt(3);
                    underorg = rs.getInt(4);                    
                }
                
                if( countryCode!=null && !countryCode.isBlank() )
                {
                    totalcc = rs.getInt(orgId>0 ? 5 : 3);
                    undercc = rs.getInt(orgId>0 ? 6 : 4);                                        
                }
                // LogService.logIt( "NormFacade.getNormData() Setting t1=" + t1 );
            }

            rs.close();

            Map<String,Object> out = new HashMap<>();

            out.put( "versionused", (int)versionIdUsed );
            
            // overalls
            out.put( "totalall", (int) total );
            out.put( "totalunder", (int) under );
            float fraction = total==0 ? 0 : ((float) under)/((float) total);
            out.put( "fraction", (float) fraction );
            out.put( "percent", (float) ((float) Math.rint( 100*100*fraction ) )/100f );

            // org
            out.put( "totalorg", (int) totalorg );
            out.put( "totalunderorg", (int) underorg );
            fraction = totalorg==0 ? 0 : ((float) underorg)/((float) totalorg);
            out.put( "percentorg", (float) ((float) Math.rint( 100*100*fraction ) )/100f );
            
            // country
            out.put( "totalcc", (int) totalcc );
            out.put( "totalundercc", (int) undercc );
            fraction = totalcc==0 ? 0 : ((float) undercc)/((float) totalcc);
            out.put( "percentcc", (float) ((float) Math.rint( 100*100*fraction ) )/100f );
            
            // LogService.logIt( "NormFacade.getNormData() total=" + total + ", under=" + under + ", fraction=" + fraction + ", " + sqlStr );

            return out;

            /*
            // LogService.logIt( "NormFacade.getNormData() Setting totalall= " + total );

            if( raw )
            {
                whereStr += " AND p.rawscore<=" + tes.getRawScore() + " ";
            }

            else if( percentileScoreTypeId==PercentileScoreType.WEIGHTED_AVG_ZSCORES.getPercentileScoreTypeId() )
            {
                if( RuntimeConstants.getBooleanValue( "UseScore2ForPercentiles" ) )
                    whereStr += " AND p.score2<=" + tes.getScore() + " "; // + " AND score2>=0 ";

                else
                    whereStr += " AND p.score<=" + tes.getScore() + " ";
                    // whereStr += " AND ( ( p.score<=" + tes.getScore() + " AND p.scoretypeid=0 ) OR ( p.score<=" + tes.getScore() + " AND p.scoretypeid=1 ) ) ";
            }

            else if( percentileScoreTypeId==PercentileScoreType.LEGACY.getPercentileScoreTypeId() )
                whereStr += " AND p.score<=" + tes.getScore() + " "; //  + " AND p.scoretypeid=0";

            // Score typeId=0, legacy, only look at legacy scores.
            else
                whereStr += " AND p.score<=" + tes.getScore() + " " ; //  + " AND p.scoretypeid=" + percentileScoreTypeId;

                // OLD Approach
                // whereStr += " AND p.score<=" + tes.getScore() + " ";

            sqlStr = s1 + whereStr + whereStr2;

            // LogService.logIt( "NormFacade.getNormData() BBBBB " + sqlStr );

            int under = 0;

            rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
            {
                under = rs.getInt(1);
            }

            rs.close();

            out.put( "totalunder", new Integer( under ) );

            float fraction = total==0 ? 0 : ((float) under)/((float) total);

            out.put( "fraction", new Float( fraction ) );

            out.put( "percent", new Float( ((float) Math.rint( 100*100*fraction ) )/100f ) );

            // LogService.logIt( "NormFacade.getNormData() total=" + total + ", under=" + under + ", fraction=" + fraction + ", " + sqlStr );

            return out;
            */
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NormFacade.getNormData() " + sqlStr );
            throw new STException( e );
        }

    }




    /**
     * Returns a Map with the following keys:
     * totalall = INTEGER number of test events used in the calculation.
     * totalunder = INTEGER number of test events that had score below or equal to the provided score (raw or not per below).
     * fraction = FLOAT - the fraction of totalunder/totalall
     * percent = FLOAT - a two decimal percentage.
     *
     * @param tes
     * @param orgId
     * @return
     * @throws Exception
     *
    public Map<String,Object> getNormDataOLD( int productId,
                                           TestEvent testEvent,
                                           TestEventScore tes,
                                           int orgId,
                                           String countryCode,
                                           boolean raw) throws Exception
    {
        String sqlStr = null;

        String s1 = "SELECT count(1)" +  ( testEvent.getTestEventStatusTypeId()>=TestEventStatusType.SCORED.getTestEventStatusTypeId() ? "" : "+1 " ) +  " as 'cnt' FROM testeventscore AS tes INNER JOIN testevent AS te ON te.testeventid=tes.testeventid ";

        String s2 = " UNION ALL SELECT count(1) as 'cnt' FROM testeventscore AS tes INNER JOIN testeventarchive AS te ON te.testeventid=tes.testeventid ";

        String whereStr;

        if( tes.getTestEventScoreType().equals( TestEventScoreType.COMPETENCY ) && tes.getSimletId()>0 && tes.getSimletVersionId()>0 && tes.getSimletCompetencyId()>0 )
        {
            whereStr = " WHERE te.excludefmnorms=0 AND te.testeventstatustypeid>=" + TestEventStatusType.SCORED.getTestEventStatusTypeId() +
                           " AND te.testeventstatustypeid<" + TestEventStatusType.EXPIRED.getTestEventStatusTypeId() +
                           " AND tes.testeventscorestatustypeid="  + TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() +
                           " AND tes.simletid=" + tes.getSimletId() +
                           " AND tes.simletversionid=" + tes.getSimletVersionId() +
                           " AND tes.simletcompetencyid=" + tes.getSimletCompetencyId() +
                           " AND tes.testeventscoretypeid=" + tes.getTestEventScoreTypeId() +
                           " AND tes.scoreformattypeid=" + tes.getScoreFormatTypeId();

            // Simlets always use raw scores.
            raw=true;
        }

        else
            whereStr = " WHERE te.excludefmnorms=0 AND te.productid=" + productId + " AND te.testeventstatustypeid>=" + TestEventStatusType.SCORED.getTestEventStatusTypeId() + " AND te.testeventstatustypeid<" + TestEventStatusType.EXPIRED.getTestEventStatusTypeId() + " AND tes.simcompetencyid=" + tes.getSimCompetencyId() + " AND tes.testeventscorestatustypeid="  + TestEventScoreStatusType.ACTIVE.getTestEventScoreStatusTypeId() + " AND tes.testeventscoretypeid=" + tes.getTestEventScoreTypeId() + " AND tes.scoreformattypeid=" + tes.getScoreFormatTypeId();

        if( orgId>0 )
            whereStr += " AND te.orgid=" + orgId + " ";

        if( countryCode!=null && !countryCode.isEmpty() )
            whereStr += " AND te.ipcountry IS NOT NULL AND te.ipcountry='" + countryCode + "' ";

        sqlStr = s1 + whereStr + " " + s2 + whereStr;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        //String t;
        int t1=0;
        int t2=0;


        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            // LogService.logIt( "NormFacade.getNormData() AAAA " + sqlStr );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
            {
                t1 = rs.getInt(1);
                // LogService.logIt( "NormFacade.getNormData() Setting t1=" + t1 );
            }

            if( rs.next() )
            {
                t2 = rs.getInt(1);
                // LogService.logIt( "NormFacade.getNormData() Setting t2=" + t2 );
            }

            rs.close();

            Map<String,Object> out = new HashMap<>();

            int total = t1 + t2;

            out.put( "totalall", new Integer((int) total) );

            // LogService.logIt( "NormFacade.getNormData() Setting totalall= " + total + ", t1=" + t1 + ", t2=" + t2 );

            if( raw )
                whereStr += " AND tes.rawscore<=" + tes.getRawScore() + " ";

            else
                whereStr += " AND tes.score<=" + tes.getScore() + " ";

            sqlStr = s1 + whereStr + " " + s2 + whereStr;

            // LogService.logIt( "NormFacade.getNormData() BBBBB " + sqlStr );

            t1 = 0;
            t2 = 0;

            rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
            {
                t1 = rs.getInt(1);
            }

            if( rs.next() )
            {
                t2 = rs.getInt(1);
            }

            rs.close();

            int under = t1 + t2;

            out.put( "totalunder", new Integer( under) );

            float fraction = total==0 ? 0 : ((float) under)/((float) total);

            out.put( "fraction", new Float( fraction ) );

            out.put( "percent", new Float( ((float) Math.rint( 100*100*fraction ) )/100f ) );

            // LogService.logIt( "NormFacade.getNormData() total=" + total + ", under=" + under + ", fraction=" + fraction + ", " + sqlStr );

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NormFacade.getNormData() " + sqlStr );

            throw new STException( e );
        }

    }
    */





    public Map<String,Object> getNormData( Battery battery, BatteryScore bs, int orgId, String countryCode) throws Exception
    {
        String sqlStr = null;

        String s1 = "SELECT count(1)" +  ( bs.getBatteryScoreId()>0 ? "" : "+1 " ) +  " as 'cnt' FROM batteryscore AS bs ";

        if( countryCode!=null && countryCode.trim().isEmpty() )
            countryCode = null;
        
        if( countryCode !=null )
            s1 += " INNER JOIN xuser AS u ON bs.userid=u.userid ";
        
        String whereStr = " WHERE bs.excludefmnorms=0 AND bs.batteryid=" + battery.getBatteryId() + " AND bs.batteryscorestatustypeid=" + BatteryScoreStatusType.ACTIVE.getBatteryScoreStatusTypeId();

        if( orgId>0 )
            whereStr += " AND bs.orgid=" + orgId + " ";

        if( countryCode !=null )
            whereStr += " AND u.ipcountry IS NOT NULL AND u.ipcountry='" + countryCode + "' ";
        
        sqlStr = s1 + whereStr;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        int t1=0;

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
        
            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
            {
                t1 = rs.getInt(1);
            }


            rs.close();

            Map<String,Object> out = new HashMap<>();

            int total = t1;

            out.put( "totalall", (int) total );

            whereStr += " AND bs.score<=" + bs.getScore() + " ";

            sqlStr = s1 + whereStr;

            t1 = 0;

            rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                t1 = rs.getInt(1);

            rs.close();

            int under = t1;

            out.put( "totalunder", (int) under );

            float fraction = total==0 ? 0 : ((float) under)/((float) total);

            out.put( "fraction", fraction );

            out.put( "percent", ((float) Math.rint( 100*100*fraction ) )/100f );

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "NormFacade.getNormData() " + sqlStr );

            throw new STException( e );
        }

    }


}
