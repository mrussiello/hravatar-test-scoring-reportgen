package com.tm2score.custom.coretest2;

import com.tm2score.event.*;
import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.battery.BatteryScoreStatusType;
import com.tm2score.entity.event.*;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.*;
import javax.sql.DataSource;

@Stateless
@PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class Ct2NormFacade
{
    static List<Long> ct3SimIdList = null;

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;



    public static Ct2NormFacade getInstance()
    {
        try
        {
            return (Ct2NormFacade) InitialContext.doLookup( "java:module/Ct2NormFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct2NormFacade.getInstance() " );

            return null;
        }
    }


    /**
     * List of ct3Sim simid
     *
     * @return
     */
    private List<Long> getCt2SimIdList() throws Exception
    {
        String sqlStr = "SELECT simid FROM ct3sim WHERE ct3simstatustypeid>=120 and ct3simstatustypeid<=151";

        List<Long> out = new ArrayList<>();

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery(sqlStr);

            while( rs.next() )
            {
                out.add( rs.getLong(1));
            }

            rs.close();

            return out;

        }

        catch( Exception e )
        {
            LogService.logIt( e, "NormFacade.getCt2SimIdList() " + sqlStr );

            throw new STException( e );
        }
    }

    private String getCt3SimIdStr() throws Exception
    {
        if( ct3SimIdList == null )
            ct3SimIdList = getCt2SimIdList();

        StringBuilder sb = new StringBuilder();

        for( Long l : ct3SimIdList )
        {
            if( sb.length()>0 )
                sb.append(",");

            sb.append( l.toString() );
        }

        return sb.toString();
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
     */
    public Map<String,Object> getOverallCt2NormData( float score,
                                                     boolean raw,
                                                     int orgId,
                                                     String countryCode ) throws Exception
    {

        String sqlStr = null;

        String s1 = "SELECT COUNT(1) FROM percentile AS p WHERE p.simletid=0 AND p.simletcompetencyid=0 AND p.simcompetencyid=0 AND p.simid IN (" + getCt3SimIdStr() + ") AND p.testeventid>=" + RuntimeConstants.getIntValue( "minCt2SimTestEventIdForNorms" ) + " "; //2925";

        String whereStr = " ";

        if( orgId>0 )
            whereStr += " AND p.orgid=" + orgId + " ";

        if( countryCode!=null && !countryCode.isEmpty() )
            whereStr += " AND p.ipcountry IS NOT NULL AND p.ipcountry='" + countryCode + "' ";

        sqlStr = s1 + whereStr;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        //String t;
        int total = 0;

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            // LogService.logIt( "NormFacade.getNormData() AAAA " + sqlStr );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
            {
                total = rs.getInt(1);
                // LogService.logIt( "NormFacade.getNormData() Setting t1=" + t1 );
            }

            rs.close();

            Map<String,Object> out = new HashMap<>();

            out.put( "totalall", new Integer((int) total) );

            // LogService.logIt( "NormFacade.getNormData() Setting totalall= " + total + ", t1=" + t1 + ", t2=" + t2 );

            if( raw )
                whereStr += " AND p.rawscore<=" + score + " ";

            else
                whereStr += " AND p.score<=" + score + " ";

            sqlStr = s1 + whereStr;

            // LogService.logIt( "NormFacade.getNormData() BBBBB " + sqlStr );

            int under = 0;

            rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
            {
                under = rs.getInt(1);
            }

            rs.close();

            out.put( "totalunder", new Integer( under) );

            float fraction = total==0 ? 0 : ((float) under)/((float) total);

            out.put( "fraction", new Float( fraction ) );

            out.put( "percent", new Float( ((float) Math.rint( 100*100*fraction ) )/100f ) );

            // LogService.logIt( "NormFacade.getNormData() total=" + total + ", under=" + under + ", fraction=" + fraction + ", " + sqlStr );

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "Ct2NormFacade.getOverallCt2NormData() " + sqlStr );

            throw new STException( e );
        }

    }


}
