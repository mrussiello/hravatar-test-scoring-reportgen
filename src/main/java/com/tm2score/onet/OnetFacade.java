/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.onet;

import com.tm2score.custom.bestjobs.EeoJobCategoryType;
import com.tm2score.entity.onet.Soc;
import com.tm2score.entity.onet.SocToEeoCategory;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author Mike
 */
@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class OnetFacade
{

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";

    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;

    public static OnetFacade getInstance()
    {
        try
        {
            return (OnetFacade) InitialContext.doLookup( "java:module/OnetFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }

    
    public EeoJobCategoryType getEeoJobCategoryTypeForSoc( String soc ) throws Exception
    {
        SocToEeoCategory sec = getSocToEeoCategory( soc );
        return sec==null ? null : sec.getEeoJobCategoryType();
    }
    


    public SocToEeoCategory getSocToEeoCategory( String soc ) throws Exception
    {
        if( soc==null || soc.length()<7 )
            return null;
        
        String ss = soc;
        
        if( ss.length()>7 )
            ss = ss.substring(0, 7);
        
        try
        {
            // LogService.logIt( "OnetFacade.getSocToEeoCategory() soc=" + soc + ", conditioned=" + ss );
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            return (SocToEeoCategory) emmirror.createNamedQuery( "Soc2Eeoc.findForSoc" ).setParameter( "socCode", ss ).getSingleResult();
        }
        catch( NoResultException e )
        {
            // LogService.logIt(  "OnetFacade.getSocToEeoCategory( No match found for soc=" + soc + ", conditioned soc=" + ss + ") " );
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getSocToEeoCategory( " + soc + ", conditioned soc=" + ss + " ) " );
            throw new STException( e );
        }
    }
    

    public Map<String,Integer> getSocEducExpValueMap( String elementId ) throws Exception
    {
        Map<String,Integer> out = new HashMap<>();

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT onetsoc_code,category,data_value from education_training_experience as e where data_value=" +
                        " (select max(data_value) from education_training_experience where onetsoc_code=e.onetsoc_code AND element_id='" + elementId + "' )";

        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
        
            ResultSet rs = stmt.executeQuery(sqlStr);

            String rawSoc;

            while( rs.next() )
            {
                rawSoc = rs.getString(1);

                if( rawSoc.indexOf(".")>0 )
                    rawSoc = rawSoc.substring(0, rawSoc.indexOf(".") );

                if( out.get(rawSoc) == null )
                    out.put( rawSoc, new Integer( rs.getInt(2)) );
            }

            rs.close();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getSocEducExpValueMap() elementId=" + elementId );

            throw new STException(e);
        }

        return out;
    }


    
    /**
     * Returns int[0] = employment
     * Returns int[1] = annual Salary average.
     * 
     * @param socCode
     * @return
     * @throws Exception 
     */
    public int[] getBlsEmploymentData( String socCode ) throws Exception
    {
        int[] out = new int[] {0,0};

        String socConditioned = socCode;
        
        if( socConditioned.indexOf('.')>0 )
            socConditioned = socConditioned.substring(0,socConditioned.indexOf('.'));
        
        socConditioned = StringUtils.sanitizeForSqlQuery(socConditioned);
        
        String sqlStr = "SELECT employment, averageannualsalary FROM bls_employment_data WHERE onetsoccode='" + socConditioned + "' ";

        try
        {
            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                ResultSet rs = stmt.executeQuery( sqlStr );

                if( rs.next() )
                {
                    out[0] = rs.getInt( 1 );
                    out[1] = rs.getInt( 2 );
                }
                
                else
                {
                    LogService.logIt( "OnetFacade.getBlsEmploymentData() BLS Data did not include data for soc: " + sqlStr );
                }

                rs.close();
            }

            catch( Exception e )
            {
                LogService.logIt( e, "OnetFacade.getBlsEmploymentData() " );
            }

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getBlsEmploymentData() " );

            return out;
        }
    }

    /**
     * int[0] = educ code
     * int[1] = training code
     * int[2] = relatedExperience Code
     *
     * @param socCode
     * @return
     */
    public int[] getEducTrainingExpForSoc( String socCode )
    {
        float[] maxVals = new float[] {0,0,0};

        int[] outList = new int[] {0,0,0};

        socCode = StringUtils.sanitizeForSqlQuery(socCode);
        String sqlStr = "SELECT scale_id, category, data_value FROM education_training_experience WHERE onetsoc_code='" + socCode + "' ORDER BY scale_id,data_value DESC";

        try
        {
            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                ResultSet rs = stmt.executeQuery( sqlStr );

                String scale;
                int category;
                float dataVal;
                while( rs.next() )
                {
                    scale = rs.getString( 1 );
                    category = rs.getInt( 2 );
                    dataVal = rs.getFloat( 3 );

                    // Education
                    if( scale.equalsIgnoreCase( "RL" ) )
                    {
                        if(dataVal > maxVals[0] )
                        {
                            maxVals[0] = dataVal;
                            outList[0] = category;
                        }
                    }

                    // Training
                    if( scale.equalsIgnoreCase( "OJ" ) || scale.equalsIgnoreCase( "PT" ) )
                    {
                        if(dataVal > maxVals[1] )
                        {
                            maxVals[1] = dataVal;
                            outList[1] = category;
                        }
                    }

                    // Experience
                    if( scale.equalsIgnoreCase( "RW" ) )
                    {
                        if(dataVal > maxVals[2] )
                        {
                            maxVals[2] = dataVal;
                            outList[2] = category;
                        }
                    }
                }

                rs.close();
            }

            catch( Exception e )
            {
                LogService.logIt( e, "OnetFacade.getEducTrainingExpForSoc() " );
            }

            return outList;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getEducTrainingExpForSoc() " );

            return outList;
        }



    }





    public List<Soc> getRelatedSocList( String socCode )
    {
        List<Soc> outList = new ArrayList<>();

        List<String> socsInList = new ArrayList<>();
        
        if( socCode == null || socCode.isEmpty() )
            return outList;

        socCode = StringUtils.sanitizeForSqlQuery(socCode);
        try
        {
            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                String sqlStr = "SELECT r.related_onetsoc_code FROM related_occupations AS r INNER JOIN occupation_data AS od ON od.onetsoc_code=r.related_onetsoc_code WHERE od.hasdata=1 AND r.onetsoc_code='" + socCode + "'";

                // String sqlStr = "(SELECT r.related_onetsoc_code FROM related_occupations AS r INNER JOIN occupation_data AS od ON od.onetsoc_code=r.related_onetsoc_code WHERE od.hasdata=1 AND r.onetsoc_code='" + socCode + "' ) " + 
                //                 " UNION ALL "  + 
                //                "(SELECT r.related_onetsoc_code FROM career_starters_matrix AS r INNER JOIN occupation_data AS od ON od.onetsoc_code=r.related_onetsoc_code WHERE od.hasdata=1 AND r.onetsoc_code='" + socCode + "' ) ";

                ResultSet rs = stmt.executeQuery( sqlStr );

                String soc;

                while( rs.next() )
                {
                    soc = rs.getString( 1 );   // soc

                    if( soc != null && !socsInList.contains( soc ) )
                    {
                        outList.add( this.getSoc( soc ) );
                        socsInList.add( soc );
                    }
                }

                rs.close();
            }

            catch( Exception e )
            {
                LogService.logIt( e, "OnetFacade.getRelatedSocList() " );
            }

            return outList;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getRelatedSocList() " );

            return outList;
        }

    }
    


    public List<String> getAlternateTitlesList( String socCode ) throws Exception
    {
        List<String> outList = new ArrayList<>();

        if( socCode == null || socCode.isEmpty() )
            return outList;

        socCode = StringUtils.sanitizeForSqlQuery(socCode);
        try
        {
            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                String sqlStr = "SELECT r.alternate_title FROM alternate_titles AS r WHERE r.onetsoc_code='" + socCode + "' ORDER BY r.alternate_title";

                ResultSet rs = stmt.executeQuery( sqlStr );

                String s;

                while( rs.next() )
                {
                    outList.add( rs.getString( 1 ) );   // soc
                }

                rs.close();
            }

            catch( Exception e )
            {
                LogService.logIt( e, "OnetFacade.getAlternateTitlesList() " );
            }

            return outList;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getAlternateTitlesList() " );

            return outList;
        }
        
    }
    

    public Soc getSoc( String socCode )
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "Soc.findBySoc" );

            q.setParameter( "socCode" , socCode );

            return (Soc) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getSoc() " );

            return null;
        }
    }




    public List<OnetElement> getKSAsForSoc( String socCode, int compToShow, OnetElementType onetElementType ) throws Exception
    {
        try
        {
            if( compToShow <= 0 )
                compToShow = 30;

            List<OnetElement> outList = new ArrayList<>();

            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            String sqlStr = null;            
            
            socCode = StringUtils.sanitizeForSqlQuery(socCode);
        
            if( onetElementType.equals( OnetElementType.ABILITY ) )
                sqlStr = "SELECT a.element_id,a.data_value,c.element_name,c.description FROM abilities AS a INNER JOIN content_model_reference AS c ON a.element_id=c.element_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;
                
            else if( onetElementType.equals( OnetElementType.KNOWLEDGE ) )
                sqlStr = "SELECT a.element_id,a.data_value,c.element_name,c.description FROM knowledge AS a INNER JOIN content_model_reference AS c ON a.element_id=c.element_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;
                
            else if( onetElementType.equals( OnetElementType.SKILL ) )
                sqlStr = "SELECT a.element_id,a.data_value,c.element_name,c.description FROM skills AS a INNER JOIN content_model_reference AS c ON a.element_id=c.element_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;

            else if( onetElementType.equals( OnetElementType.WK_STYLE ) )
                sqlStr = "SELECT a.element_id,a.data_value,c.element_name,c.description FROM work_styles AS a INNER JOIN content_model_reference AS c ON a.element_id=c.element_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;
            
            else if( onetElementType.equals( OnetElementType.WK_ACTIVITY ) )
                sqlStr = "SELECT a.element_id,a.data_value,c.element_name,c.description FROM work_activities AS a INNER JOIN content_model_reference AS c ON a.element_id=c.element_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;
            
            else if( onetElementType.equals( OnetElementType.WK_CONTEXT ) )
                sqlStr = "SELECT a.element_id,a.data_value,c.element_name,c.description FROM work_context AS a INNER JOIN content_model_reference AS c ON a.element_id=c.element_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='CX' ORDER BY a.data_value DESC LIMIT " + compToShow;
            
            else if( onetElementType.equals( OnetElementType.TASK ) )
                sqlStr = "SELECT a.task_id,a.data_value,c.task,'' FROM task_ratings AS a INNER JOIN task_statements AS c ON a.task_id=c.task_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;

            
            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                OnetElement sc;


                ResultSet rs = stmt.executeQuery(sqlStr);

                while( rs.next() )
                {
                    sc = new OnetElement();

                    sc.setOnetElementId( rs.getString( 1 ));

                    sc.setOnetElementTypeId( onetElementType.getOnetElementTypeId() );

                    //sc.setOnetSocCode( socCode );

                    sc.setImportance( rs.getFloat(2));

                    sc.setName( rs.getString( 3 ));

                    sc.setDescription( rs.getString( 4 ));

                    // LogService.logIt( "OnetFacade.getCompetenciesForSoc() " + sc.getName() + ", containedalready=" + outList.contains( sc ) );

                    if( !outList.contains( sc ) )
                        outList.add( sc );
                }

                rs.close();
                
                Collections.sort( outList, new OnetImportanceComparator() );

                if( outList.size() > compToShow )
                    outList = outList.subList( 0, compToShow);

                // Get DWAs
                if( onetElementType.equals( OnetElementType.TASK ) )
                {
                    List<String[]> sl;

                    String[] sa;
                    
                    OnetTaskFreqType otft;
                    
                    for( OnetElement oe : outList )
                    {
                        sqlStr = "SELECT d.dwa_id,dr.dwa_title FROM tasks_to_dwas as d INNER JOIN dwa_reference as dr On dr.dwa_id=d.dwa_id WHERE d.task_id='" + oe.getOnetElementId() + "' ";
                        
                        sl = new ArrayList<>();
                        
                        rs = stmt.executeQuery(sqlStr);

                        while( rs.next() )
                        {
                            sa = new String[] {rs.getString(1),rs.getString(2)};
                            
                            sl.add( sa );
                        }                    

                        rs.close();
                        
                        oe.setDetWorkActivities(sl);
                        
                        otft = calculateOnetTaskFreqType( socCode, Integer.parseInt( oe.getOnetElementId()) );
                        
                        oe.setOnetFreqTypeId( otft==null ? 0 : otft.getOnetTaskFreqTypeId() );
                    }
                }
                
                if( onetElementType.equals( OnetElementType.WK_CONTEXT ) )
                {
                    for( OnetElement oe : outList )
                    {
                        float tgtVal = oe.getImportance();
                        
                        int rounded = Math.round(tgtVal);
                        
                        if( rounded<1 )
                            rounded=1;
                        
                        if( rounded>5 )
                            rounded=5;
                        
                        sqlStr = "SELECT c.category_description FROM work_context AS a INNER JOIN work_context_categories AS c ON a.element_id=c.element_id WHERE a.element_id='" + oe.getOnetElementId() + "' AND (a.scale_id='CXP' OR a.scale_id='CTP') AND c.category=" + rounded + " ORDER BY a.data_value DESC LIMIT 1";
                        
                        rs = stmt.executeQuery(sqlStr);

                        if( rs.next() )
                        {
                            oe.setContextCategory( rs.getString(1));
                        }

                        rs.close();
                    }
                }
            
                // LogService.logIt( "OnetFacade.getKSAsForSoc() " + socCode + ", " + onetElementType.getName() +", out contains " + outList.size()  );
                return outList;
            }

            catch( Exception e )
            {
                throw e;
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.abilities() " + socCode );

            throw new STException( e );
        }
    }
    
    
    public OnetJobZoneType getOnetJobZoneType( String socCode ) throws Exception
    {
        String sqlStr=null;

        try
        {
            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            socCode = StringUtils.sanitizeForSqlQuery(socCode);
            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                sqlStr = "SELECT a.job_zone FROM job_zones AS a WHERE a.onetsoc_code='" + socCode + "' ";

                int zoneId = 0;
                ResultSet rs = stmt.executeQuery(sqlStr);

                if( rs.next() )
                    zoneId = rs.getInt(1);

                rs.close();
                
                return OnetJobZoneType.getValueForZoneId(zoneId);
            }

            catch( Exception e )
            {
                throw e;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getOnetJobZoneType() soc=" + socCode  + ", sql=" + sqlStr );

            throw new STException( e );
        }
    }



    public boolean getIsGreenJob( String socCode ) throws Exception
    {
        return false;
    }
    


    public OnetTaskFreqType calculateOnetTaskFreqType( String socCode, int taskId ) throws Exception
    {
        String sqlStr=null;

        try
        {
            // List<SimTask> outList = new ArrayList<>();

            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            socCode = StringUtils.sanitizeForSqlQuery(socCode);
            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                sqlStr = "SELECT a.category,a.data_value FROM task_ratings AS a WHERE a.onetsoc_code='" + socCode + "' AND a.task_id=" + taskId + " AND a.scale_id='FT' AND a.recommend_suppress='N'";

                ResultSet rs = stmt.executeQuery(sqlStr);

                float cat;
                float val;

                float wsum = 0;

                float sumCats = 0;

                while( rs.next() )
                {
                    cat = rs.getFloat(1);
                    val = rs.getFloat(2);

                    wsum += cat*val;
                    sumCats += cat;
                }

                rs.close();

                float avg = sumCats > 0 ? wsum/sumCats : 0;

                int categoryId = Math.round( avg );

                // LogService.logIt( "OnetFacade.calcFreqTypeForTask() taskId=" + taskId + ", avg=" + avg + ", catId=" + categoryId );

                return OnetTaskFreqType.getValue( categoryId );

            }

            catch( Exception e )
            {
                throw e;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.calculateOnetTaskFreqType() soc=" + socCode + ", taskId=" + taskId + ", sql=" + sqlStr );

            throw new STException( e );
        }
    }



    public List<OnetElement> getTasksForSoc( String socCode, int compToShow ) throws Exception
    {
        try
        {
            if( compToShow <= 0 )
                compToShow = 30;

            List<OnetElement> outList = new ArrayList<>();

            DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

            if( pool == null )
                throw new Exception( "Can not find Datasource" );

            socCode = StringUtils.sanitizeForSqlQuery(socCode);
            try (Connection con = pool.getConnection();
                 Statement stmt = con.createStatement() )
            {
                con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

                OnetElement st;

                String sqlStr = "SELECT a.element_id,a.data_value,c.element_name FROM work_activities AS a INNER JOIN content_model_reference AS c ON a.element_id=c.element_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;

                ResultSet rs = stmt.executeQuery(sqlStr);

                while( rs.next() )
                {
                    st = new OnetElement();


                    st.setOnetElementId( rs.getString( 1 ));

                    st.setOnetElementTypeId( OnetElementType.WK_ACTIVITY.getOnetElementTypeId() );

                    st.setImportance( rs.getFloat(2));

                    st.setName( rs.getString( 3 ));

                    if( !outList.contains( st ) )
                        outList.add( st );
                }

                rs.close();

                // now, for each task see if there is a detailed work activity
                for( OnetElement t : outList )
                {
                    sqlStr = "SELECT title,dwacode FROM detwkactivities WHERE soc='" + socCode + "' AND elementid='" + t.getOnetElementId() + "'";

                    rs = stmt.executeQuery(sqlStr);

                    if( rs.next() )
                    {
                        t.setStrParam1(rs.getString( 1 ));
                    }

                    rs.close();
                }


                sqlStr = "SELECT a.task_id,a.data_value,c.task FROM task_ratings AS a INNER JOIN task_statements AS c ON a.task_id=c.task_id WHERE a.onetsoc_code='" + socCode + "' AND a.scale_id='IM' AND a.recommend_suppress='N' ORDER BY a.data_value DESC LIMIT " + compToShow;

                rs = stmt.executeQuery(sqlStr);

                while( rs.next() )
                {
                    st = new OnetElement();

                    // st.setCreateDate( new Date() );

                    st.setOnetElementId( rs.getInt(1) + "" );

                    st.setOnetElementTypeId( OnetElementType.TASK.getOnetElementTypeId() );

                    // st.setOnetSocCode( socCode );

                    st.setImportance( rs.getFloat(2));

                    // st.setRankValue( st.getOnetImportance() );

                    st.setName( rs.getString( 3 ));

                    // st.setName( st.getOnetName() );

                    // LogService.logIt( "OnetFacade.getTasksForSoc() " + st.getName() + ", contained already=" + outList.contains( st ) );

                    if( !outList.contains( st ) )
                        outList.add( st );
                }

                rs.close();

                Collections.sort( outList, new OnetImportanceComparator() );

                //int idx = 1;

                for( OnetElement tt : outList )
                {
                   // tt.setSystemRank( idx );
                    //tt.setUserRank( idx );
                    //idx++;

                    if( tt.getOnetElementType().equals( OnetElementType.TASK ) )
                    {
                        tt.setOnetFreqTypeId( calculateOnetTaskFreqType( socCode, Integer.parseInt( tt.getOnetElementId()) ).getOnetTaskFreqTypeId() );

                    }
                }

                if( outList.size() > compToShow )
                    return outList.subList( 0, compToShow);

                else
                    return outList;
            }

            catch( Exception e )
            {
                throw e;
            }

        }
        catch( Exception e )
        {
            LogService.logIt( e, "OnetFacade.getTasksForSoc() " + socCode );

            throw new STException( e );
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    

}
