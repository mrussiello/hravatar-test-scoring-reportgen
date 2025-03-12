/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.essay;

import com.tm2score.entity.discern.Essay;
import com.tm2score.entity.discern.EssayGrade;
import com.tm2score.util.TextProcessingUtils;
import com.tm2score.entity.essay.EssayPrompt;
import com.tm2score.entity.essay.UnscoredEssay;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.util.StringUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import javax.sql.DataSource;


/**
 *
 * @author Mike
 */
@Stateless
@PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class DiscernFacade
{
    // private static EntityManagerFactory factory;
    // private static EntityManagerFactory discernFactory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;

    @PersistenceContext( name = "persistence/discern", unitName = "discern" )
    EntityManager discern;
    
    public static int ESSAY_PLAG_CHECK_MAX_OFFSET = 2000;
    

    public static DiscernFacade getInstance()
    {
        try
        {
            return (DiscernFacade) InitialContext.doLookup( "java:module/DiscernFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernFacade.getInstance() " );

            return null;
        }
    }

    
    public Essay saveDiscernEssay( Essay r ) throws Exception
    {
        // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
        // EntityManager em = (EntityManager) envCtx.lookup( "persistence/discern" );
        try
        {
            if( r.getEssayText()==null || r.getEssayText().isBlank() )
                throw new Exception( "Essay.EssayText is required" );

            if( r.getProblemId()<= 0 )
                throw new Exception( "Essay.problemId is required" );

            if( r.getCreateDate() == null )
                r.setCreateDate( new Date() );

            r.setLastUpdate( new Date() );

            if( r.getEssayId() > 0 )
                discern.merge( r );

            else
                discern.persist( r );

            discern.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernFacade.saveDiscernEssay() " + r.toString() );
            throw new STException( e );
        }

        return r;
    }
    
    /*
    public EssayGrade saveDiscernEssayGrade( EssayGrade r ) throws Exception
    {
        Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
        EntityManager em = (EntityManager) envCtx.lookup( "persistence/discern" );
        try
        {
            if( r.getEssayId()<= 0 )
                throw new Exception( "EssayGrade.essayId is required" );

            if( r.getCreateDate() == null )
                r.setCreateDate( new Date() );

            r.setLastUpdate( new Date() );

            if( r.getEssayGradeId() > 0 )
                em.merge( r );

            else
                em.persist( r );

            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernFacade.saveDiscernEssayGrade() " + r.toString() );
            throw new STException( e );
        }

        return r;
    }
    */
    
    

    public UnscoredEssay getUnscoredEssayForMinStatus( long testEventId, int nodeSequenceId, int subnodeSequenceId, int minScoreStatusTypeId ) throws Exception
    {
        try
        {
            // if( factory == null )
            //     factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = factory.createEntityManager();

            Query q = em.createNamedQuery( "UnscoredEssay.findByTestEventIdAndItemAndMinStatusTypeId" );

            q.setParameter( "testEventId", testEventId );
            q.setParameter( "nodeSequenceId", nodeSequenceId );
            q.setParameter( "subnodeSequenceId", subnodeSequenceId );
            q.setParameter( "minScoreStatusTypeId", minScoreStatusTypeId );
            
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<UnscoredEssay> uel = q.getResultList();

            Collections.sort( uel );

            return uel.isEmpty() ? null : uel.get( uel.size()-1 );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getUnscoredEssayForMinStatus( testEventId=" + testEventId + ", nodeSeq=" + nodeSequenceId + ", subnodeSeq=" + subnodeSequenceId + ", minScoreStatusTypeId=" + minScoreStatusTypeId + " ) " );

            throw new STException( e );
        }
    }

    public void deleteDiscernEssayInfo( int discernEssayId ) throws Exception
    {
        // LogService.logIt("discernFacade.deleteDiscernEssayInfo() discernEssayId=" + discernEssayId );

        // First, get the TestEvents that need to be updated.
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/discern" );

        if( pool == null )
            throw new Exception( "Can not find Datasource for discern" );

        String sqlStr = "DELETE FROM freeform_data_essaygrade WHERE essay_id=" + discernEssayId ;

        // Doing it one at a time to ensure that we don't lock the entire testevent table!!!!!
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            stmt.executeUpdate(sqlStr);

            sqlStr = "DELETE FROM freeform_data_essay WHERE id=" + discernEssayId ;

            stmt.executeUpdate(sqlStr);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "discernFacade.deleteDiscernEssayInfo() discernEssayId=" + discernEssayId +", sqlStr=" + sqlStr );

            throw e;
        }

        clearDbmsCache();       
    }

    public void deleteCompletedUnscoredEssaysForTestEvent( long testEventId ) throws Exception
    {
        // LogService.logIt("discernFacade.deleteCompletedUnscoredEssaysForTestEvent() testEventId=" + testEventId );

        // Only delete scored essays so we are sure that discern is not doing anything with them.
        List<UnscoredEssay> uel = this.getUnscoredEssays(testEventId, EssayScoreStatusType.SCORECOMPLETE.getEssayScoreStatusTypeId() );
        
        int count = 0;
        
        for( UnscoredEssay ue : uel )
        {
            if( ue.getDiscernEssayId()>0 )
            {
                deleteDiscernEssayInfo(  ue.getDiscernEssayId() );
                count++;
            }
        }
        
        if( count<=0 )
            return;
        
        // First, get the TestEvents that need to be updated.
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );

        if( pool == null )
            throw new Exception( "Can not find Datasource for discern" );

        String sqlStr = "DELETE FROM unscoredessay WHERE testeventid=" + testEventId ;

        // Doing it one at a time to ensure that we don't lock the entire testevent table!!!!!
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            stmt.executeUpdate(sqlStr);
        }

        catch( Exception e )
        {
            LogService.logIt( e, "discernFacade.deleteCompletedUnscoredEssaysForTestEvent() testEventId=" + testEventId +", sqlStr=" + sqlStr );

            throw e;
        }
        
        clearDbmsCache();        
    }
    
    
    
    public void clearDbmsCache()
    {
        try
        {
            //if( factory == null )
            //    factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = factory.createEntityManager();
            em.getEntityManagerFactory().getCache().evictAll();
            
            //if( discernFactory==null )
            //    discernFactory=DiscernPersistenceManager.getInstance().getEntityManagerFactory();

            //em = discernFactory.createEntityManager();
            discern.getEntityManagerFactory().getCache().evictAll();
            
        }

        catch( Exception e )
        {
            LogService.logIt( e, "UserFacade.clearDbmsCache() " );
        }
    }
            

    
    
    public UnscoredEssay getUnscoredEssay( int unscoredEssayId ) throws Exception
    {
        try
        {
            //if( factory == null )
            //    factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = factory.createEntityManager();
            
            return (UnscoredEssay) em.createNamedQuery( "UnscoredEssay.findByUnscoredEssayId", UnscoredEssay.class ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "unscoredEssayId", unscoredEssayId ).getSingleResult();
        }
        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernFacade.getUnscoredEssay( " + unscoredEssayId + " )" );

            throw new STException( e );
        }
    }
    

    public List<UnscoredEssay> getUnscoredEssaysForStatsUpdate() throws Exception
    {
        try
        {
            //if( factory == null )
            //    factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = factory.createEntityManager();

            Query q = em.createNamedQuery( "UnscoredEssay.findByNoStats" );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getUnscoredEssaysForStatsUpdate() " );

            throw new STException( e );
        }

    }


    /**
     * returns null if grading record is not successful
     *
     * returns
     *    [0] score - Float
     *    [1] confidence - Float
     *    [2] Date
     *    [3] EssayGradeId - Integer
     *
     * @param essayGradeId
     * @return
     * @throws Exception
     */
    public Object[] checkForEssayScoreDirect( int discernEssayId, int discernEssayGradeId ) throws Exception
    {
        Object[] out = null;
       
        try
        {
            //if( discernFactory == null )
            //    discernFactory = DiscernPersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = discernFactory.createEntityManager();

            // LogService.logIt("discernFacade.checkForEssayScoreDirect() discernEssayGradeId=" + discernEssayId );

            // EssayGrade eg = em.find( EssayGrade.class, discernEssayGradeId );
            
            Query q = discern.createNamedQuery( discernEssayId>0 ?  "EssayGrade.findByEssayIdAndGraderType" : "EssayGrade.findByIdAndGraderType", EssayGrade.class );
            
            if( discernEssayId>0 )
                q.setParameter("discernEssayId", discernEssayId );
            else
                q.setParameter("discernEssayGradeId", discernEssayGradeId );
                
            q.setParameter("graderType", "ML" );
            
            // could have been changed by another app
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            
            List<EssayGrade> egl = q.getResultList();
            
            if( egl==null || egl.isEmpty() )
                return out;
            
            EssayGrade eg = egl.get(egl.size()-1);
            
            out = new Object[4];
            
            String ts = eg.getTargetScores();

            float score = 0;
            ts = ts.trim();
            if( ts.indexOf( "[" )==0  )
                ts = ts.substring(1, ts.length()-1);
            score = Float.parseFloat(ts);            
            
            out[0] = score;
            out[1] = eg.getConfidence();
            out[2] = eg.getLastUpdate();
            out[3] = eg.getEssayGradeId();            
            
            return out;
        }
        
        catch( NoResultException e )
        {
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "DiscernFacade.checkForEssayScoreDirect( discernEssayId=" + discernEssayId + " ) " );
            throw new STException( e );
        }
        
    }    

    
        


    /**
     * returns null if grading record is not successful
     *
     * returns
     *    [0] score - Float
     *    [1] confidence - Float
     *    [2] Date
     *    [3] EssayGradeId
     *
     * @param essayGradeId
     * @return
     * @throws Exception
     *
    public Object[] checkForEssayScoreViaEssayIdDirect( int discernEssayId ) throws Exception
    {
        // LogService.logIt("discernFacade.checkForEssayGradeViaEssayIdDirect() discernEssayId=" + discernEssayId );

        // First, get the TestEvents that need to be updated.
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/discern" );

        if( pool == null )
            throw new Exception( "Can not find Datasource for discern" );

        String sqlStr = "SELECT target_scores,confidence,modified,id FROM freeform_data_essaygrade WHERE essay_id=" + discernEssayId + " and grader_type='ML' AND success=1";

        // Doing it one at a time to ensure that we don't lock the entire testevent table!!!!!
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery(sqlStr);

            Object[] out = null;

            if( rs.next() )
            {
                out = new Object[4];

                String ts = rs.getString(1);

                float score = 0;

                ts = ts.trim();

                if( ts.indexOf( "[" )==0  )
                    ts = ts.substring(1, ts.length()-1);

                score = Float.parseFloat(ts);

                float confidence = rs.getFloat(2);
                Date modified = rs.getDate(3);

                out[0] = score;
                out[1] = confidence;
                out[2] = modified;
                out[3] = rs.getInt(4);
            }

            rs.close();

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "discernFacade.checkForEssayGradeViaEssayIdDirect() discernEssayId=" + discernEssayId + ", sqlStr=" + sqlStr );

            return null;
        }
    }
    */


    /**
     * returns null if grading record is not successful
     *
     * returns
     *    [0] score - Float
     *    [1] confidence - Float
     *    [2] Date
     *    [3] EssayGradeId
     *
     * @param essayGradeId
     * @return
     * @throws Exception
     *
    public Object[] checkForEssayScoreViaEssayIdDirectOLD( int discernEssayId ) throws Exception
    {
        // LogService.logIt("discernFacade.checkForEssayGradeViaEssayIdDirect() discernEssayId=" + discernEssayId );

        // First, get the TestEvents that need to be updated.
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/discern" );

        if( pool == null )
            throw new Exception( "Can not find Datasource for discern" );

        String sqlStr = "SELECT target_scores,confidence,modified,id FROM freeform_data_essaygrade WHERE essay_id=" + discernEssayId + " and grader_type='ML' AND success=1";

        // Doing it one at a time to ensure that we don't lock the entire testevent table!!!!!
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery(sqlStr);

            Object[] out = null;

            if( rs.next() )
            {
                out = new Object[4];

                String ts = rs.getString(1);

                float score = 0;

                ts = ts.trim();

                if( ts.indexOf( "[" )==0  )
                    ts = ts.substring(1, ts.length()-1);

                score = Float.parseFloat(ts);

                float confidence = rs.getFloat(2);
                Date modified = rs.getDate(3);

                out[0] = score;
                out[1] = confidence;
                out[2] = modified;
                out[3] = rs.getInt(4);
            }

            rs.close();

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "discernFacade.checkForEssayGradeViaEssayIdDirect() discernEssayId=" + discernEssayId + ", sqlStr=" + sqlStr );

            return null;
        }
    }
    */
    
    


    public UnscoredEssay findSimilarEssayForPrompt( long testEventId, int essayPromptId, int ct5ItemId, int ct5ItemPartId, String essayText, int maxRowsToCheck, boolean useCt5ItemId) throws Exception
    {
        try
        {
            //long teid = 0;

            List<UnscoredEssay> uel ;

            double similarity;

            int maxRows = 500;
            int offset = 0;
            
            if( maxRowsToCheck <= ESSAY_PLAG_CHECK_MAX_OFFSET)
                maxRowsToCheck = ESSAY_PLAG_CHECK_MAX_OFFSET;
            
            do
            {
                uel = useCt5ItemId ? getEssaysForCt5ItemPartId(testEventId, ct5ItemId, ct5ItemPartId, maxRows, offset ) : getEssaysForPrompt(testEventId, essayPromptId, maxRows, offset );

                for( UnscoredEssay ue : uel )
                {
                    if( ue.getEssay()==null || ue.getEssay().isEmpty() )
                        continue;

                    //if( ue.getTestEventId()>teid )
                     //   teid = ue.getTestEventId();

                    similarity = TextProcessingUtils.getTextSimilarityVal(essayText, ue.getEssay() );

                    if( similarity > 0.95f )
                    {
                        LogService.logIt("ReportFacade.getEssaysForPrompt( testEventId=" + testEventId + ", essayPromptId=" + essayPromptId + " ) Found similar essay from previously submitted essays to check. Matching unscoredEssayId=" + ue.getUnscoredEssayId() );
                        return ue;
                    }

                }

                offset += maxRows;
                
                if( offset< maxRowsToCheck )
                    uel = useCt5ItemId ? getEssaysForCt5ItemPartId(testEventId, ct5ItemId, ct5ItemPartId, maxRows, offset ) : getEssaysForPrompt(testEventId, essayPromptId, maxRows, offset );
                // teid++;

            } while( offset<maxRowsToCheck && !uel.isEmpty() );

            
            // nothing. Now check required.
            for( UnscoredEssay ue : getRqdEssaysForPromptForPlagCheck(essayPromptId) )
            {
                if( ue.getEssay()==null || ue.getEssay().isEmpty() )
                    continue;

                similarity = TextProcessingUtils.getTextSimilarityVal(essayText, ue.getEssay() );

                if( similarity > 0.95f )
                {
                    LogService.logIt("ReportFacade.getEssaysForPrompt( testEventId=" + testEventId + ", essayPromptId=" + essayPromptId + " ) Found similar essay from required essays to check. Matching unscoredEssayId=" + ue.getUnscoredEssayId() );
                    return ue;
                }
            }            
            
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ReportFacade.getEssaysForPrompt( testEventId=" + testEventId + ", essayPromptId=" + essayPromptId + " ) " );

            throw new STException( e );
        }


    }



    /*
    public List<UnscoredEssay> getEssaysForPrompt( long testEventId, int essayPromptId, int maxRows, long minTestEventId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "";

        List<UnscoredEssay> ol = new ArrayList<>();

        UnscoredEssay ue;

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             sql = "SELECT unscoredEssayId,testEventId,essay FROM unscoredessay WHERE testeventid>=" + minTestEventId + " AND essaypromptid='" + essayPromptId + "' ORDER BY testeventid LIMIT " + maxRows;

             ResultSet rs = stmt.executeQuery( sql );

            // long id;

             while( rs.next() )
             {
                 ue = new UnscoredEssay();

                 ue.setUnscoredEssayId(rs.getInt(1));
                 ue.setTestEventId(rs.getInt(2));
                 ue.setEssay(rs.getString(3));

                 ol.add( ue );
             }

             rs.close();

             return ol;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getEssaysForPrompt() " + sql );

            throw new STException( e );
        }
    }
    */

    public List<UnscoredEssay> getEssaysForPrompt( long testEventId, int essayPromptId, int maxRows, int offset) throws Exception
    {
        if( offset<0 )
            offset=0;
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "";

        List<UnscoredEssay> ol = new ArrayList<>();

        UnscoredEssay ue;

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             sql = "SELECT unscoredEssayId,testEventId,essay FROM unscoredessay WHERE essaypromptid=" + essayPromptId + " AND includeinplagcheck=0 ORDER BY testeventid DESC LIMIT " + maxRows + " OFFSET " + offset;

             ResultSet rs = stmt.executeQuery( sql );

            // long id;

             while( rs.next() )
             {
                 ue = new UnscoredEssay();

                 ue.setUnscoredEssayId(rs.getInt(1));
                 ue.setTestEventId(rs.getInt(2));
                 ue.setEssay(rs.getString(3));

                 ol.add( ue );
             }

             rs.close();

             return ol;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getEssaysForPrompt() " + sql );

            throw new STException( e );
        }
    }

    
    public List<UnscoredEssay> getEssaysForCt5ItemPartId( long testEventId, int ct5ItemId, int ct5ItemPartId, int maxRows, int offset) throws Exception
    {
        if( offset<0 )
            offset=0;
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "";

        List<UnscoredEssay> ol = new ArrayList<>();

        UnscoredEssay ue;

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             sql = "SELECT unscoredEssayId,testEventId,essay FROM unscoredessay WHERE ct5itemid=" + ct5ItemId + " AND ct5itempartid=" + ct5ItemPartId + " AND includeinplagcheck=0 ORDER BY testeventid DESC LIMIT " + maxRows + " OFFSET " + offset;

             ResultSet rs = stmt.executeQuery( sql );

            // long id;

             while( rs.next() )
             {
                 ue = new UnscoredEssay();

                 ue.setUnscoredEssayId(rs.getInt(1));
                 ue.setTestEventId(rs.getInt(2));
                 ue.setEssay(rs.getString(3));

                 ol.add( ue );
             }

             rs.close();

             return ol;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getEssaysForPrompt() " + sql );

            throw new STException( e );
        }
    }

    
    public List<UnscoredEssay> getRqdEssaysForPromptForPlagCheck( int essayPromptId ) throws Exception
    {
        try
        {
            //if( factory == null )
            //    factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "UnscoredEssay.findByEssayPromptIdAndPlagCheck" );

            q.setParameter( "essayPromptId", essayPromptId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            q.setMaxResults(200);
            return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getRqdEssaysForPromptForPlagCheck( essayPromptId=" + essayPromptId + " ) " );
            throw new STException( e );
        }
    }

    
    


    /*
    public UnscoredEssay findSimilarEssayForPrompt_OLD( long testEventId, int essayPromptId, String essayText ) throws Exception
    {
        try
        {

            int maxRows = 200;

            int offset = 0;

            List<UnscoredEssay> uel ;

            double similarity;

            do
            {
                uel = getEssaysForPrompt( testEventId, essayPromptId, maxRows, offset );

                for( UnscoredEssay ue : uel )
                {
                    if( ue.getEssay()==null || ue.getEssay().isEmpty() )
                        continue;

                    similarity = EssayScoringUtils.getTextSimilarityVal(essayText, ue.getEssay() );

                    if( similarity > 0.95f )
                        return ue;
                }

                offset += maxRows;

            } while( uel.size() >= maxRows );

            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getEssaysForPrompt( testEventId=" + testEventId + ", essayPromptId=" + essayPromptId + " ) " );

            throw new STException( e );
        }


    }


    public List<UnscoredEssay> getEssaysForPrompt_OLD( long testEventId, int essayPromptId, int maxRows, int offset ) throws Exception
    {
        try
        {
            if( factory == null )
                factory = PersistenceManager.getInstance().getEntityManagerFactory();

            EntityManager em = factory.createEntityManager();

            Query q = em.createNamedQuery( "UnscoredEssay.findOthersByPromptId" );

            q.setParameter( "testEventId", testEventId );
            q.setParameter( "essayPromptId", essayPromptId );

            q.setMaxResults(maxRows);
            q.setFirstResult(offset);

            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getEssaysForPrompt( testEventId=" + testEventId + ", essayPromptId=" + essayPromptId + " ) " );

            throw new STException( e );
        }


    }
    */



    public List<UnscoredEssay> getUnscoredEssays( long testEventId, int minScoreStatusTypeId ) throws Exception
    {
        try
        {
            //if( factory == null )
            //    factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = factory.createEntityManager();

            Query q = em.createNamedQuery( "UnscoredEssay.findByTestEventIdAndMinStatusTypeId" );

            q.setParameter( "testEventId", testEventId );
            q.setParameter( "minScoreStatusTypeId", minScoreStatusTypeId );
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getResultList();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getUnscoredEssays( testEventId=" + testEventId + ", minScoreStatusTypeId=" + minScoreStatusTypeId + " ) " );
            throw new STException( e );
        }
    }



    public UnscoredEssay saveUnscoredEssay( UnscoredEssay r, boolean withValidation) throws Exception
    {
        //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

        //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );
        // EntityTransaction utx = em.getTransaction();
        try
        {
            if( r.getEssayPromptId()<=0 )
                throw new Exception( "UnscoredEssay.essayPromptId invalid " + r.getEssayPromptId() );

            if( r.getEssay()==null || r.getEssay().length()== 0 )
                throw new Exception( "UnscoredEssay.essay is required" );

            if( r.getSecondsToCompose()<0 )
                r.setSecondsToCompose(0);
            else if( r.getSecondsToCompose()> 1000000 )
                r.setSecondsToCompose( 1000000 );
                        
            r.setEssay( StringUtils.removeNonAscii( r.getEssay() ));

            if( withValidation )
            {
                if( r.getTestEventId()<=0 )
                    throw new Exception( "UnscoredEssay.testEventId invalid " + r.getTestEventId() );

                if( r.getNodeSequenceId()<=0 )
                    throw new Exception( "UnscoredEssay.getNodeSequenceId invalid " + r.getNodeSequenceId() );

                if( r.getSubnodeSequenceId()<=0 )
                    throw new Exception( "UnscoredEssay.getSubnodeSequenceId invalid " + r.getSubnodeSequenceId() );

                r.setLastUpdate( new Date() );
            }

            if( r.getCreateDate() == null )
                r.setCreateDate( new Date() );


            // utx.begin();
            if( r.getUnscoredEssayId() > 0 )
            {
                em.merge( r );
            }

            else
            {
                em.detach( r );
                em.persist( r );
            }

            // // em.flush();
            // utx.commit();
        }

        catch( Exception e )
        {
            LogService.logIt(e, "DiscernFacade.saveUnscoredEssay() " + r.toString() );
            // if( utx.isActive() )
                // utx.rollback();
            throw new STException( e );
        }

        return r;
    }



    /*
    public EssayPrompt getEssayPrompt( String prompt ) throws Exception
    {
        try
        {
            if( factory == null )
                factory = PersistenceManager.getInstance().getEntityManagerFactory();

            EntityManager em = factory.createEntityManager();

            Query q = em.createNamedQuery( "EssayPrompt.findByPrompt" );

            q.setParameter( "prompt", prompt );

            // q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (EssayPrompt) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getEssayPrompt( prompt=" + prompt + " ) " );

            throw new STException( e );
        }
    }
    */


    public EssayPrompt getEssayPrompt( int essayPromptId ) throws Exception
    {
        try
        {
            //if( factory == null )
            //    factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = factory.createEntityManager();

            Query q = emmirror.createNamedQuery( "EssayPrompt.findByEssayPromptId" );

            q.setParameter( "essayPromptId", essayPromptId );

                q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (EssayPrompt) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReportFacade.getEssayPrompt( essayPromptId=" + essayPromptId + " ) " );

            throw new STException( e );
        }
    }



}
