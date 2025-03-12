package com.tm2score.event;

import com.tm2score.entity.battery.Battery;
import com.tm2score.entity.battery.BatteryScore;
import com.tm2score.entity.event.*;
import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.entity.sim.SimDescriptor;
import com.tm2score.global.Constants;
import com.tm2score.global.RuntimeConstants;
import com.tm2score.global.STException;
import com.tm2score.score.ScoringException;
import com.tm2score.score.TestKeyEventSelectionType;
import com.tm2score.service.LogService;
import com.tm2score.service.Tracker;
import com.tm2score.util.StringUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.*;
import javax.sql.DataSource;


//@RequestScoped
@Stateless // ( name = "EventFacade", mappedName="EventFacade" )
public class EventFacade
{
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;
    

    
    
    public static EventFacade getInstance()
    {
        try
        {
            // return (EventFacade) InitialContext.doLookup( "java:global/tm2score2/EventFacade" );
            return (EventFacade) InitialContext.doLookup( "java:module/EventFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }

    /*
      Moved to EJB so that it's inside a transaction and if there's an error we don't end up with two copies.
    */
    public void archiveTestKey( TestKey testKey )  throws Exception
    {
        try
        {
            if( testKey.getTestKeyId() <= 0 )
                throw new Exception( "EventFacade.archiveTestKey() testkeyid invalid " + testKey.getTestKeyId() );

            if( testKey.getTestKeyArchiveId()>0 )
                return;

            // LogService.logIt( "EventArchiver.archiveTestKey() archiving TestKey: " + testKey.getTestKeyId() + " status=" + testKey.getTestKeyStatusType().name() );

            flushEntityManager();
            // check for existing archive
            TestKeyArchive tka = getTestKeyArchiveForTestKeyId( testKey.getTestKeyId() );
            // only save if not saved already.
            if( tka == null )
            {
                saveTestKeyArchive( testKey.getTestKeyArchive() );
                Tracker.addTestKeyArchive();
            }

            if( testKey.getTestEventList()==null )
                testKey.setTestEventList( getTestEventsForTestKeyId(testKey.getTestKeyId(), true ) );
            
            List<TestEvent> tel = testKey.getTestEventList(); // eventFacade.getTestEventsForTestKeyId(testKey.getTestKeyId(), false );            
            deleteTestKey( testKey.getTestKeyId() );

            for( TestEvent te : tel )
            {
                if( te.getTestEventArchiveId()>0 )
                    continue;

                archiveTestEvent(te);
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.archiveTestKey() " + testKey.toString() );
            throw e;
        }
    }
    
    
    
    
    
    
    private void archiveTestEvent( TestEvent testEvent )  throws Exception
    {
        try
        {
            if( testEvent.getTestEventId() <= 0 )
                throw new Exception( "EventFacade.archiveTestEvent() testeventid invalid " + testEvent.getTestEventId() );

            // LogService.logIt( "EventArchiver.archiveTestEvent() archiving testEventId: " + testEvent.getTestEventId() + ", testKeyId=" + testEvent.getTestKeyId() + ", status=" + testEvent.getTestEventStatusType().getKey() );

            if( testEvent.getTestEventArchiveId()>0 )
                return;

            flushEntityManager();
            
            // check for existing archive
            TestEventArchive tka = getTestEventArchiveForTestEventId( testEvent.getTestEventId() );
            if( tka != null && tka.getTestKeyId() != testEvent.getTestKeyId() )
                throw new Exception( "Archiving of testEventId=" + testEvent.getTestEventId() + " failed. Found an archived TestEvent that has the same testEventId=" + testEvent.getTestEventId() + " but a different tea.TestKeyId=" + tka.getTestKeyId() + ". Expected te.testKeyId=" + testEvent.getTestKeyId() + ", TestEventArchiveId=" + tka.getTestEventArchiveId() );

            // only save if not saved already.
            if( tka == null )
            {
                tka = saveTestEventArchive( testEvent.getTestEventArchive() );
                
                // LogService.logIt( "EventArchiver.archiveTestEvent() Saved testEventId=" + tka.getTestEventId() + " as a NEW TestEventArchive.Id=" + tka.getTestEventArchiveId() );
                Tracker.addTestEventArchive();
                // Thread.sleep(100);
                deleteTestEvent( testEvent.getTestEventId() );

            }
            
            else if( tka.getTestKeyId() == testEvent.getTestKeyId() )
            {
                LogService.logIt( "EventFacade.archiveTestEvent()  testEventId: " + testEvent.getTestEventId() + " is already archived! However, TestKeyIds match. Skipping and deleting the TestEvent record. " );
                //tka = eventFacade.saveTestEventArchive( testEvent.getTestEventArchive() );
                deleteTestEvent( testEvent.getTestEventId() );
            }
            else
                throw new Exception( "XXX Found an archived TestEvent that has the same testEventId=" + testEvent.getTestEventId() + " but a different tea.TestKeyId=" + tka.getTestKeyId() + ". Expected te.testKeyId=" + testEvent.getTestKeyId() + ", TestEventArchiveId=" + tka.getTestEventArchiveId() );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.archiveTestEvent() " +  testEvent.toString() );
            throw e;
            // throw new STException(e);
        }

    }
    
    
    

    public boolean areAllTestEventsCompletedForTestKey( TestKey tk ) throws Exception
    {
        try
        {
            // TestKey tk = this.getTestKey(tkId, true);

            if( tk==null )
                throw new Exception( "TestKey Null");
            
            if( tk.getProductId()<=0 )
                throw new Exception( "testKey.productId invalid: " + tk.getProductId() );

            if( tk.getTestEventList()==null )
                tk.setTestEventList( getTestEventsForTestKeyId(tk.getTestKeyId(), true) );
            
            List<TestEvent> tel = tk.getTestEventList();
            
            if( tel.isEmpty() )
                return false;

            if( tk.getBatteryId()>0 )
            {
                TestEvent tex;
                
                Battery b = this.getBattery( tk.getBatteryId() );
                
                for( Integer pid :  b.getProductIdList() )
                {
                    tex=null;
                    
                    for( TestEvent te : tel )
                    {
                        if( te.getProductId()==pid )
                        {
                            tex=te;
                            break;
                        }
                    }
                    
                    if( tex==null || !tex.getTestEventStatusType().getIsCompleteOrHigher())
                        return false;
                }
                
                return true;
                
            }
            
            else
            {
                TestEvent te = tel.get(0);
                    
                if( te.getProductId()!=tk.getProductId() )
                    throw new Exception( "PRoductId mismatch. Expected TestEvent.prodictId=" + tk.getProductId() + " but got " + te.getProductId() );
                    
                return te.getTestEventStatusType().getIsCompleteOrHigher();
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.areAllTestEventsCompletedForTestKey(" + (tk==null ? "null" : tk.getTestKeyId() ) + ")" );
            
            throw e;
        }
    }
    

    public SurveyEvent getSurveyEventForTestKey( long testKeyId ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "SurveyEvent.findByTestKeyId" );

            q.setParameter( "testKeyId", testKeyId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (SurveyEvent) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getSurveyEventForTestKey( " + testKeyId + " ) " );

            throw new STException( e );
        }
    }

    
    public float[] getAverageItemScoreForItemScore( ItemResponse ir ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT AVG(ir.itemscore), COUNT(1) FROM itemresponse AS ir WHERE ir.simletid=" + ir.getSimletId() + 
                        " AND ir.simletversionid=" + ir.getSimletVersionId() + " AND ir.simletnodeseq=" + ir.getSimletNodeSeq() + 
                        " AND ir.identifier='" + StringUtils.sanitizeForSqlQuery(ir.getIdentifier()) + "' ";

        // LogService.logIt( "EventFacade.getAverageItemScoreForItemScore() " + sqlStr );

        List<Long> ol = new ArrayList<>();

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             ResultSet rs = stmt.executeQuery( sqlStr );


            float[] out = new float[2];
            
             if( rs.next() )
             {
                 out[0] = rs.getFloat(1);
                 out[1] = rs.getFloat(2);
             }

             rs.close();

             return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getAverageItemScoreForItemScore() " + sqlStr + ", " + ir.toString() );

            throw new STException( e );
        }
        
        
    }
    

    
    
    
    public TestKey getTestKey( long testKeyId, boolean refresh ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            if( refresh )
            {
                TestKey tk = (TestKey) em.createNamedQuery( "TestKey.findByTestKeyId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "testKeyId", testKeyId ).getSingleResult();

                if( tk != null )
                    return tk;

                TestKeyArchive tka = getTestKeyArchiveForTestKeyId( testKeyId );

                return tka != null ? tka.getTestKey() : null;
            }

            TestKey tk =  em.find( TestKey.class,  testKeyId );

            if( tk!= null )
                return tk;

            TestKeyArchive tka = getTestKeyArchiveForTestKeyId( testKeyId );

            if( tka != null )
                return tka.getTestKey();

            return null;
        }

        catch( NoResultException e )
        {
            TestKeyArchive tka = getTestKeyArchiveForTestKeyId( testKeyId );

            if( tka != null )
                return tka.getTestKey();

            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestKey( " + testKeyId + ", " + refresh + " ) " );

            throw new STException( e );
        }
    }
    
    
    
    

    public TestKey getTestKeyForOrgAndEventRef( int orgId, String extRef ) throws Exception
    {
        try
        {
            if( orgId <= 0 || extRef == null || extRef.trim().isEmpty() )
                return null;

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            TypedQuery<TestKey> q = em.createNamedQuery( "TestKey.findByOrgAndExtRef", TestKey.class );

            q.setParameter( "orgId", orgId );

            q.setParameter( "extRef", extRef );

            List<TestKey> ul = q.getResultList();

            if( ul.isEmpty() )
            {
                TypedQuery<TestKeyArchive> qq = em.createNamedQuery( "TestKeyArchive.findByOrgAndExtRef", TestKeyArchive.class );

                qq.setParameter( "orgId", orgId );

                qq.setParameter( "extRef", extRef );

                List<TestKeyArchive> ula = qq.getResultList();

                if( ula.isEmpty() )
                    return null;

                if( ula.size()> 1 )
                    throw new Exception( "ERROR: there is more than one row with this orgId/eventRef combination." );

                return ula.get( 0 ).getTestKey();
            }

            if( ul.size()> 1 )
                throw new Exception( "ERROR: there is more than one row with this orgId/eventRef combination." );

            return ul.get( 0 );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestKeyForOrgAndEventRef( orgId=" + orgId + " , extRef=" +  extRef + " )" );

            return null;
        }
    }





    public TestEventArchive getTestEventArchiveForTestEventId( long testEventId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "TestEventArchive.findByTestEventId" );

            q.setParameter( "testEventId", testEventId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return (TestEventArchive) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestEventArchiveForTestEventId( " + testEventId + " ) " );

            throw new STException( e );
        }
    }



    public TestKeyArchive getTestKeyArchiveForTestKeyId( long testKeyId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "TestKeyArchive.findByTestKeyId" );

            q.setParameter( "testKeyId", testKeyId );

            return (TestKeyArchive) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestKeyArchiveForTestKeyId( " + testKeyId + " ) " );

            throw new STException( e );
        }
    }



    public TestEventScore getTestEventScore( long testEventScoreId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            return em.find(TestEventScore.class, testEventScoreId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.getTestEventScore( " + testEventScoreId + " ) " );

            throw new STException( e );
        }
    }




    public Battery getBattery( int batteryId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            return emmirror.find( Battery.class, batteryId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getBattery( " + batteryId + " ) " );

            throw new STException( e );
        }
    }


    public BatteryScore getBatteryScoreForTestKey( long testKeyId ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            TypedQuery q = em.createNamedQuery( "BatteryScore.findByTestKeyId", BatteryScore.class );

            q.setParameter( "testKeyId", testKeyId );

            return (BatteryScore) q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getBatteryScoreForTestKey( " + testKeyId + " ) " );

            throw new STException( e );
        }
    }

    
    public List<TestEventScore> getTestEventScoresForTestEvent( long testEventId, boolean refresh ) throws Exception
    {
        try
        {
            // LogService.logIt( "EventFacade.getTestEventScoresForTestEvent() " + testEventId );

            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "TestEventScore.findByTestEventId" );

            q.setParameter( "testEventId", testEventId );

            if( refresh )
                q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            List<TestEventScore> sl = (List<TestEventScore>) q.getResultList();

            Collections.sort( sl );

            return sl;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestEventScoresForTestEvent( " + testEventId + " ) " );

            throw new STException( e );
        }

    }
    
    
    public List<Long> getIncompleteBatteryTestEventIdsToScore(  int maxQuantity, boolean forReportGen, int minSecondsComplete ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        if( maxQuantity<=0 )
            maxQuantity=100;
        
        String secsComplete = "";
        if( minSecondsComplete >0 )
        {
            Calendar cal = new GregorianCalendar();
            cal.add( Calendar.SECOND, -1*minSecondsComplete );
            java.sql.Timestamp t = new java.sql.Timestamp( cal.getTime().getTime() );
            //java.sql.Date d = new java.sql.Date( cal.getTime().getTime() );
            secsComplete = " AND te.lastaccessdate<'" + t.toString() + "' ";
        }
        
        String sql = "SELECT te.testeventid FROM testevent te INNER JOIN testkey tk ON tk.testkeyid=te.testkeyid WHERE tk.statustypeid<100 AND tk.batteryid>0 and tk.productid<>te.productid AND te.testeventstatustypeid IN  (" + (forReportGen ? "110" : "100,101") + ") " + secsComplete + " LIMIT " + maxQuantity;

        // LogService.logIt( "EventFacade.getIncompleteBatteryTestEventIdsToScore() " + sql );
        List<Long> ol = new ArrayList<>();

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
            ResultSet rs = stmt.executeQuery( sql );

            // LogService.logIt( "EventFacade.getIncompleteBatteryTestEventIdsToScore() " + sql );
             while( rs.next() )
             {
                 ol.add( rs.getLong(1) );
             }
             rs.close();
             return ol;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getIncompleteBatteryTestEventIdsToScore() " + sql );
            throw new STException( e );
        }
    }
    

    public List<TestKey> getNextBatchOfTestKeysToScore( int testKeyStatusTypeId, int quantity, boolean includeArchive, int includeMaxErrors, List<Integer> orgIdsToSkip) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            TestKeyEventSelectionType tkest = RuntimeConstants.getTestKeyEventSelectionType();
            
            TestKeyStatusType tkst = TestKeyStatusType.getValue( testKeyStatusTypeId );
            boolean useMaxErrors = tkst.getIsAnyScoreRptDistError() && includeMaxErrors>=0;
            
            // EntityManager em = tm2Factory.createEntityManager();

            List<TestKey> out = new ArrayList<>();

            TypedQuery q;
            
            if( quantity==0 )
                quantity=Constants.DEFAULT_TESTKEY_BATCH_SIZE;

            if( includeArchive || testKeyStatusTypeId == TestKeyStatusType.COMPLETED_PENDING_EXTERNAL.getTestKeyStatusTypeId() )
            {                
                if( orgIdsToSkip!=null && !orgIdsToSkip.isEmpty() )
                {
                    q = em.createNamedQuery( useMaxErrors ? "TestKeyArchive.findByStatusAndMaxErrorsInAccessOrderSkipOrgs" : "TestKeyArchive.findByStatusInAccessOrderSkipOrgs", TestKeyArchive.class );
                    q.setParameter( "orgIdsToSkipList", orgIdsToSkip );
                }

                else
                    q = em.createNamedQuery( useMaxErrors ? "TestKeyArchive.findByStatusAndMaxErrorsInAccessOrder" : "TestKeyArchive.findByStatusInAccessOrder", TestKeyArchive.class );
                                    
                q.setParameter( "testKeyStatusTypeId", testKeyStatusTypeId );

                if( useMaxErrors )
                    q.setParameter( "maxErrors", includeMaxErrors );

                q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
                
                if( quantity>0 )
                    q.setMaxResults(quantity);
                
                List<TestKeyArchive> tkal = q.getResultList();

                for( TestKeyArchive tka : tkal )
                {
                    if( tkest.keep( tka.getTestKeyId() ) )
                        out.add( tka.getTestKey() );
                }                
            } 
            
            if( quantity>0 && out.size()>=quantity )
            {
                return out;
            }
            
            if( orgIdsToSkip!=null && !orgIdsToSkip.isEmpty() )
            {
                q = em.createNamedQuery( useMaxErrors ? "TestKey.findByStatusAndMaxErrorsInAccessOrderSkipOrgs" : "TestKey.findByStatusInAccessOrderSkipOrgs", TestKey.class );
                q.setParameter( "orgIdsToSkipList", orgIdsToSkip );
            }
            else
                q = em.createNamedQuery( useMaxErrors ? "TestKey.findByStatusAndMaxErrorsInAccessOrder" : "TestKey.findByStatusInAccessOrder", TestKey.class );

            q.setParameter( "testKeyStatusTypeId", testKeyStatusTypeId );

            if( useMaxErrors )
                q.setParameter( "maxErrors", includeMaxErrors );
                            
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            if( quantity>0 && quantity>out.size() )
                q.setMaxResults(quantity - out.size() );

            //if( out.isEmpty() )
            //    return q.getResultList();

            List<TestKey> tkl = q.getResultList();
            
            for( TestKey tk : tkl )
            {
                if( tkest.keep( tk.getTestKeyId() ) )
                    out.add( tk );
            }
            
            
            // out.addAll( tkl );

            //if( RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes") > 0 )
            //    removeTestKeysRequiringDelay( out );
            
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.getNextBatchOfTestKeysToScore( " + quantity + " ) " );

            throw new STException( e );
        }
    }

    public List<TestKey> getNextBatchOfTestKeyArchivesToScore( int testKeyStatusTypeId, List<Integer> orgIdsToSkip, int quantity) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
             //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            TestKeyEventSelectionType tkest = RuntimeConstants.getTestKeyEventSelectionType();
            
            // EntityManager em = tm2Factory.createEntityManager();

            TypedQuery q;
            if( orgIdsToSkip!=null && !orgIdsToSkip.isEmpty() )
            {
                q = em.createNamedQuery( "TestKeyArchive.findByStatusInAccessOrderSkipOrgs", TestKeyArchive.class );
                q.setParameter( "orgIdsToSkipList", orgIdsToSkip );
            }
            else
                q = em.createNamedQuery( "TestKeyArchive.findByStatusInAccessOrder", TestKeyArchive.class );

            q.setParameter( "testKeyStatusTypeId", testKeyStatusTypeId );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            q.setMaxResults(quantity <= 0 ? 20 : quantity );

            List<TestKeyArchive> tkal = q.getResultList();

            List<TestKey> tkl = new ArrayList<>();

            for( TestKeyArchive tka : tkal )
            {
                if( tkest.keep(tka.getTestKeyId() ) )
                    tkl.add(tka.getTestKey() );
            }

            //if( RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes") > 0 )
            // removeTestKeysRequiringDelay( tka );            
            
            return tkl;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.getNextBatchOfTestKeyArchivesToScore( " + quantity + " ) " );
            throw new STException( e );
        }
    }

    
    
    /*
    private void removeTestKeysRequiringDelay( List<TestKey> tkl )
    {
        if( RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes") > 0 )
        {
            // In order to give Optional Interest Inventory Tests time to have companion Job Specific Tests 
            // fully scored (some will be delayed by essay scoring), 
            // force them to wait 30 mins.
            ListIterator<TestKey> li = tkl.listIterator();

            TestKey tk;

            Calendar cal=null;
            
            while( li.hasNext() )
            {
                tk = li.next();
                
                // Only this type of test gets Delayed, so that others it will use for scoring can complete their scoring. 
                if( tk.getTestKeySourceTypeId()==TestKeySourceType.OPTIONALAUTOTEST.getTestKeySourceTypeId() )
                {
                    if( cal == null )
                    {
                        cal = new GregorianCalendar();

                        cal.add( Calendar.MINUTE, -1*RuntimeConstants.getIntValue( "Hra_OptionalTest_ScoreDelay_Minutes") );
                    }

                    // if not at least 30 mins old, skip this one. 
                    if( tk.getLastAccessDate()!=null && tk.getLastAccessDate().after( cal.getTime() ) )
                    {
                        LogService.logIt( "EventFacade.removeTestKeysRequiringDelay() waiting to score OptionalSurvey test TestKeyId=" + tk.getTestKeyId() );
                        li.remove();
                    }
                }
            }
        }
    }
    */    
    

    public List<SurveyEvent> getNextBatchOfSurveyEventsToScore(int testEventStatusTypeId, int maxErrors) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //     tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            TestKeyEventSelectionType tkest = RuntimeConstants.getTestKeyEventSelectionType();
            
            // EntityManager em = tm2Factory.createEntityManager();

            boolean useMaxErrors = TestEventStatusType.getValue( testEventStatusTypeId).equals(TestEventStatusType.SCORE_ERROR) && maxErrors>=0;
            
            TypedQuery q = em.createNamedQuery( useMaxErrors ? "SurveyEvent.findByStatusIdMaxErrors" : "SurveyEvent.findByStatusId", SurveyEvent.class );

            // q.setParameter( "surveyEventStatusTypeId", TestEventStatusType.COMPLETED.getTestEventStatusTypeId() );
            q.setParameter( "surveyEventStatusTypeId", testEventStatusTypeId );
            if( useMaxErrors )
                q.setParameter( "maxErrors", maxErrors );
                
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            q.setMaxResults( Constants.DEFAULT_TESTKEY_BATCH_SIZE );

            List<SurveyEvent> sel = q.getResultList();
            
            ListIterator<SurveyEvent> iter = sel.listIterator();
            
            SurveyEvent se;
            
            while( iter.hasNext() )
            {
                se = iter.next();
                
                if( !tkest.keep( se.getSurveyEventId() ))
                    iter.remove();                
            }
            
            return sel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getNextBatchOfSurveyEventsToScore() " );

            throw new STException( e );
        }
    }


    public List<Long> getTestEventIdsForSimIdAndOrOrg( long simId, int simVersionId, int orgId, int suborgId, long minTestEventId, int minTestEventStatusTypeId, int maxTestEventStatusTypeId, int[] otherStatusCodesToInclude, Date startDate, Date endDate, int maxRows, int offset) throws Exception
    {
        if( simId<=0 && orgId<= 0 )
            throw new Exception( "EventFacade.getTestEventIdsForSimIdAndOrOrg() either simId or orgId must be defined above zero.");

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "";

        List<Long> ol = new ArrayList<>();

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             sql = "SELECT testeventid FROM testevent WHERE ((testeventstatustypeid>=" + minTestEventStatusTypeId + " AND testeventstatustypeid<=" + maxTestEventStatusTypeId + ") ";

             if( otherStatusCodesToInclude != null && otherStatusCodesToInclude.length>0 )
             {
                 StringBuilder inStmt = new StringBuilder();
                 
                 for( int esc : otherStatusCodesToInclude )
                 {
                     if( inStmt.length()>0 )
                         inStmt.append(",");
                     
                     inStmt.append( esc );
                 }
                 
                 sql += " OR testeventstatustypeid IN (" + inStmt + ") ";
             }
             
             sql += ") ";
                          
             String sqlAdd = "";

             if( simId > 0 )
             {
                 sqlAdd += " AND simid=" + simId + " AND simversionid=" + simVersionId + " ";
             }

             if( orgId>0 )
             {
                 sqlAdd += " AND orgid=" + orgId + " ";

                 if( suborgId>0 )
                    sqlAdd += " AND suborgid=" + suborgId + " ";
             }

             if( minTestEventId > 0 )
             {
                 sqlAdd += " AND testeventid>=" + minTestEventId + " ";
             }
             
             if( startDate!=null )
             {
                 java.sql.Timestamp sDate = new java.sql.Timestamp( startDate.getTime() );
                 sqlAdd += " AND startdate>='" + sDate.toString() + "' ";
             }

             if( endDate!=null )
             {
                 java.sql.Timestamp sDate = new java.sql.Timestamp( endDate.getTime() );
                 sqlAdd += " AND lastaccessdate<='" + sDate.toString() + "' ";
             }

             sql += sqlAdd + " ORDER BY testeventid ";

             if( maxRows>0 )
             {
                 sql += " LIMIT " + maxRows + " ";

                 if( offset>0 )
                    sql += " OFFSET " + offset + " ";
             }
                                      
            ResultSet rs = stmt.executeQuery( sql );

            // LogService.logIt( "EventFacade.getTestEventIdsForSimIdAndOrOrg() AAA Checking TestEvent table. " + sql );

            // long id;

             while( rs.next() )
             {
                 ol.add( rs.getLong(1) );
             }

             rs.close();

             sql = "SELECT testeventid FROM testeventarchive WHERE ((testeventstatustypeid>=" + minTestEventStatusTypeId + " AND testeventstatustypeid<=" + maxTestEventStatusTypeId + ") "; //  

             if( otherStatusCodesToInclude != null && otherStatusCodesToInclude.length>0 )
             {
                 StringBuilder inStmt = new StringBuilder();
                 
                 for( int esc : otherStatusCodesToInclude )
                 {
                     if( inStmt.length()>0 )
                         inStmt.append(",");
                     
                     inStmt.append( esc );
                 }
                 
                 sql += " OR testeventstatustypeid IN (" + inStmt + ") ";
             }

            sql += ") " + sqlAdd + " ORDER BY testeventid ";

            if( maxRows>0 )
            {
                sql += " LIMIT " + maxRows + " ";

                if( offset>0 )
                   sql += " OFFSET " + offset + " ";
            }
            
            LogService.logIt( "EventFacade.getTestEventIdsForSimIdAndOrOrg() BBB.1 checking testeventarchive table. vals from testevent=" + ol.size() + ", sql=" + sql );

             rs = stmt.executeQuery( sql );

             while( rs.next() )
             {
                 ol.add( rs.getLong(1) );
             }

             rs.close();

             Collections.sort(ol);

             LogService.logIt( "EventFacade.getTestEventIdsForSimIdAndOrOrg() CCC.1 total records found=" + ol.size() );
             return ol;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestEventIdsForSimIdAndOrOrg() " + sql );
            throw new STException( e );
        }

    }
    
    

    public List<TestEvent> getTestEventsForTestKeyId( long testKeyId, boolean removeDeactivated) throws Exception
    {
        try
        {
            // LogService.logIt("EventFacade.getTestEventsForTestKey() " + testKeyId );

            // if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "TestEventArchive.findByTestKeyId",  TestEventArchive.class );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            q.setParameter( "testKeyId", testKeyId );

            List<TestEvent> tel = new ArrayList<>();

            List<TestEventArchive> tkal = q.getResultList();

            for( TestEventArchive tka : tkal )
            {
                tel.add( tka.getTestEvent() );
            }

            q = em.createNamedQuery( "TestEvent.findByTestKeyId" );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            q.setParameter( "testKeyId", testKeyId );

            tel.addAll( q.getResultList() );

            Collections.sort( tel );
            
            if( removeDeactivated )
            {
                TestEvent teo;

                ListIterator<TestEvent> titor = tel.listIterator();

                while( titor.hasNext() )
                {
                    teo = titor.next();

                    if( teo.getTestEventStatusType().getIsDeactivated() )
                        titor.remove();
                }
            }

            return tel;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.getTestEventsForTestKey( " + testKeyId + " ) " );

            throw new STException( e );
        }
    }


    public TestEvent getTestEvent( long testEventId, boolean refresh ) throws Exception
    {
        try
        {
            // LogService.logIt( "EventFacade.getTestEvent() " + testEventId );
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            if( refresh )
            {
                TestEvent te = (TestEvent) em.createNamedQuery( "TestEvent.findByTestEventId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "testEventId", testEventId ).getSingleResult();

                if( te != null )
                    return te;

                TestEventArchive tea = getTestEventArchiveForTestEventId( testEventId );

                return tea != null ? tea.getTestEvent() : null;
            }

            TestEvent te =  em.find( TestEvent.class, testEventId );

            if( te != null )
                return te;

            TestEventArchive tea = getTestEventArchiveForTestEventId( testEventId );

            return tea != null ? tea.getTestEvent() : null;
        }

        catch( NoResultException e )
        {
            TestEventArchive tea = getTestEventArchiveForTestEventId( testEventId );

            if( tea != null )
                return tea.getTestEvent();

            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestEvent( " + testEventId + ", " + refresh + " ) " );

            throw new STException( e );
        }
    }

    public SurveyEvent getSurveyEvent( long surveyEventId ) throws Exception
    {
        try
        {
            // LogService.logIt( "EventFacade.getTestEvent() " + testEventId );
            // if( tm2Factory == null )
             //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            return em.find( SurveyEvent.class, surveyEventId );
        }
        catch( NoResultException e )
        {
            return null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getSurveyEvent( " + surveyEventId + " ) " );
            throw new STException( e );
        }
    }

    
    
    public SimDescriptor getSimDescriptor( long simDescriptorId ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            return (SimDescriptor) em.createNamedQuery( "SimDescriptor.findById" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "simDescriptorId", simDescriptorId ).getSingleResult();

        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getSimDescriptor( " + simDescriptorId + " ) " );

            throw new STException( e );
        }
    }



    public void deleteBatteryScore( BatteryScore bs ) throws Exception
    {
        deleteEntity( bs );
    }




    public void deleteEntity( Object o ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            // EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            try
            {
                o = em.merge( o );

                em.remove( o );

                // em.flush();
            }

            catch( Exception ee )
            {
                throw ee;
            }
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.deleteEntity() " + o.toString() );

            throw new STException( e );
        }
    }


    /*
    public void clearExternalScoresForEvent( TestEvent te ) throws Exception
    {
        try
        {
           // if( clearExternal )
            //{
                DiscernFacade discernFacade = DiscernFacade.getInstance();
                List<UnscoredEssay> uel = discernFacade.getUnscoredEssays( te.getTestEventId(), EssayScoreStatusType.NOTSUBMITTED.getEssayScoreStatusTypeId() );

                for( UnscoredEssay ue : uel )
                    deleteEntity(ue);
            //}
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.clearScoresForEvent() " + te.toString() );

            throw new STException( e );
        }
    }
    */


    /*
    public void clearPercentileEntriesForEvent( TestEvent te ) throws Exception
    {
        List<Percentile> pel = this.getPercentileEntriesForTestEvent( te.getTestEventId() );

        for( Percentile pe : pel )
        {
            deleteEntity( pe );
        }
    }
    */



    
    /*
    public void clearItemResponsesForEvent( TestEvent te ) throws Exception
    {
        List<ItemResponse> irl = getItemResponsesForTestEvent( te.getTestEventId() );

        for( ItemResponse ir : irl )
        {
            deleteEntity( ir );
        }
    }
    */


    public List<ItemResponse> getItemResponsesForSurveyEvent( long surveyEventId ) throws Exception
    {
        try
        {
            if( surveyEventId<=0 )
                return new ArrayList<>();

            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            String qs = "ItemResponse.findBySurveyEventId";

            Query q = em.createNamedQuery( qs );

            q.setParameter( "surveyEventId", surveyEventId );

            List<ItemResponse> out = (List<ItemResponse>) q.getResultList();

            Collections.sort(out);

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getItemResponsesForSurveyEvent( surveyEventId=" + surveyEventId + " ) " );

            throw new STException( e );
        }

    }
    
    
    public void deleteItemResponsesForSurveyEventId( long surveyEventId ) throws Exception
    {
        deleteItemResponses( 0, surveyEventId );
    }

    public void deleteItemResponsesForTestEventId( long testEventId ) throws Exception
    {
        deleteItemResponses( testEventId, 0 );        
    }
    
    private void deleteItemResponses( long testEventId, long surveyEventId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql;
        
        if( testEventId>0 )
            sql = "DELETE FROM itemresponse WHERE testeventid=" + testEventId + " ";
        else
            sql = "DELETE FROM itemresponse WHERE surveyeventid=" + surveyEventId + " ";
            
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            stmt.executeUpdate( sql );
            
            if( testEventId>0 )
                sql = "DELETE FROM tempitemresponse WHERE testeventid=" + testEventId + " ";
            else
                sql = "DELETE FROM tempitemresponse WHERE surveyeventid=" + surveyEventId + " ";
            
            stmt.executeUpdate( sql );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "eventFacade.deleteItemResponses() " + sql );
            throw new STException( e );
        }                
    }
    
    
    public List<ItemResponse> getItemResponsesForTestEvent( long testEventId ) throws Exception
    {
        try
        {
            if( testEventId<=0 )
                return new ArrayList<>();

            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "ItemResponse.findByTestEventId" );

            q.setParameter( "testEventId", testEventId );

            List<ItemResponse> out = (List<ItemResponse>) q.getResultList();

            Collections.sort(out);

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getItemResponsesForTestEvent( testEventId=" + testEventId + " ) " );

            throw new STException( e );
        }
    }
    
    

    public List<ItemResponse> getItemResponseArchivesForTestEvent( long testEventId ) throws Exception
    {
        try
        {
            if( testEventId<=0 )
                return new ArrayList<>();

            //if( tm2Factory == null )
             //   tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "ItemResponseArchive.findByTestEventId" );

            q.setParameter( "testEventId", testEventId );

            List<ItemResponseArchive> iral = (List<ItemResponseArchive>) q.getResultList();

            Collections.sort(iral);
            
            List<ItemResponse> out = new ArrayList<>();

            for( ItemResponseArchive ira : iral )
            {
                out.add( ira.getItemResponse() );
            }
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getItemResponsesForTestEvent( testEventId=" + testEventId + " ) " );

            throw new STException( e );
        }
    }
    

    public List<Percentile> getPercentileEntriesForTestEvent( long testEventId ) throws Exception
    {
        try
        {
            if( testEventId<=0 )
                return new ArrayList<>();

            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            Query q = em.createNamedQuery( "Percentile.findByTestEventId" );

            q.setParameter( "testEventId", testEventId );

           return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getPercentileEntriesForTestEvent( testEventId=" + testEventId + " ) " );

            throw new STException( e );
        }
    }



    /*
    public void clearItemResponsesForSurveyEvent( SurveyEvent te ) throws Exception
    {
        List<ItemResponse> irl = this.getItemResponsesForSurveyEvent( te.getSurveyEventId() );
        
        for( ItemResponse ir : irl )
        {
            this.deleteEntity(ir);
        }
    }
    */


    public TestKey getTestKeyForPin( String pin ) throws Exception
    {
        try
        {
            // if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            TypedQuery<TestKey> q = em.createNamedQuery( "TestKey.findByPin", TestKey.class );

            q.setParameter( "pin", pin );

            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );

            return q.getSingleResult();
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getTestKeyForPin( " + pin + " ) " );

            throw new STException( e );
        }
    }




    /*
    public void deleteTestEventScore( long testEventScoreId ) throws Exception
    {
        try
        {
            if( testEventScoreId <= 0 )
                throw new Exception( "testEventScoreId<=0 " + testEventScoreId );

            Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            TestEventScore a = em.find( TestEventScore.class, testEventScoreId );

            if( a != null )
            {
            	try
            	{
	                a = em.merge( a );

	                em.remove( a );

	                // em.flush();
            	}

            	catch( Exception ee )
            	{
            		throw ee;
            	}
            }
        }

        catch( NoResultException e )
        {
            return;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.deleteTestEventScore( testEventScoreId=" + testEventScoreId + " ) " );

            throw new STException( e );
        }
    }
    *
    */

    public TestKeyArchive saveTestKeyArchive( TestKeyArchive tka ) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( tka.getTestKeyArchiveId() > 0 )
            {
                em.merge( tka );
            }

            else
            {
                em.detach( tka );

                em.persist( tka );
            }

            em.flush();

            return tka;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.saveTestKeyArchive() " + ( tka == null ? "testKeyArchive is null" : tka.toString() ) );

            throw new STException( e );
        }
    }

    public void deleteTestKey( long testKeyId ) throws Exception
    {
        try
        {
            if( testKeyId <= 0 )
                throw new Exception( "testKeyId<=0 " + testKeyId );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            TestKey a = em.find( TestKey.class, testKeyId );

            if( a != null )
            {
            	try
            	{
	                a = em.merge( a );

	                em.remove( a );

	                // em.flush();
            	}

            	catch( Exception ee )
            	{
            		throw ee;
            	}
            }
        }

        catch( NoResultException e )
        {
            return;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.deleteTestKey( testKeyId=" + testKeyId + " ) " );

            throw new STException( e );
        }
    }


    public void deleteTestEvent( long testEventId ) throws Exception
    {
        try
        {
            if( testEventId <= 0 )
                throw new Exception( "testEventId<=0 " + testEventId );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            TestEvent a = em.find( TestEvent.class, testEventId );

            if( a != null )
            {
            	try
            	{
                    a = em.merge( a );

                    em.remove( a );

                    em.flush();
            	}

            	catch( Exception ee )
            	{
            		throw ee;
            	}
            }
        }

        catch( NoResultException e )
        {
            return;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.deleteTestEvent( testEventId=" + testEventId + " ) " );

            throw new STException( e );
        }
    }




    // @TransactionAttribute(value=NOT_SUPPORTED)
    public TestEventArchive saveTestEventArchive( TestEventArchive tea ) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( tea.getTestEventArchiveId() > 0 )
            {
                em.merge( tea );
            }

            else
            {
                em.detach( tea );

                em.persist( tea );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return tea;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.saveTestEventArchive() " + ( tea == null ? "testEventArchive is null" : tea.toString() ) );

            throw new STException( e );
        }
    }


    // @TransactionAttribute(value=NOT_SUPPORTED)
    public TestKey saveTestKey( TestKey tk ) throws Exception
    {
        try
        {
            // If it's archived, just leave it there.
            if( tk.getTestKeyArchiveId()>0 )
            {
                TestKeyArchive tka = tk.getTestKeyArchive();
                saveTestKeyArchive( tka );
                return tka.getTestKey();
                // throw new Exception( "Cannot save an archived version of TestKey testKeyId=" + testKey.getTestKeyId() + ", testKeyArchiveId=" + testKey.getTestKeyArchiveId() );
            }

            // Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( tk.getTestKeyId() > 0 )
            {                
                if( !unarchivedTestKeyExists(tk.getTestKeyId() ) )
                    throw new ScoringException( "Unarchived TestKey record does not exist. Has probably been archived. testKeyId=" + tk.getTestKeyId(), ScoringException.NON_PERMANENT, null  );
                em.merge(tk );
            }

            else
            {
                em.detach(tk );
                em.persist(tk );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            // em.flush();

            return tk;
        }
        catch( ScoringException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.saveTestKey() " + ( tk == null ? "testKey is null" : tk.toString() ) );

            throw new STException( e );
        }
    }
    
    public boolean unarchivedTestEventExists( long testEventId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "SELECT testeventid FROM testevent WHERE testeventid=" + testEventId + " ";
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
            ResultSet rs = stmt.executeQuery( sql );

            // long id;
            boolean exists = false;
            if( rs.next() )
                exists=true;
            rs.close();
            return exists;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "eventFacade.unarchivedTestEventExists() " + sql );
            throw new STException( e );
        }        
    }
    
    public boolean unarchivedTestKeyExists( long testKeyId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "SELECT testkeyid FROM testkey WHERE testkeyid=" + testKeyId + " ";
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );
            ResultSet rs = stmt.executeQuery( sql );

            // long id;
            boolean exists = false;
            if( rs.next() )
                exists=true;

            rs.close();
            return exists;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "eventFacade.unarchivedTestKeyExists() " + sql );
            throw new STException( e );
        }        
    }


    public TestEventScore saveTestEventScore( TestEventScore tes ) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            boolean ndsFlush = tes.getTestEventScoreId() <= 0;

            
            //if( ScoreManager.DEBUG_SCORING )
            //    LogService.logIt( "EventFacade.saveTestEventScore() Saving testEventId=" + tes.getTestEventId() + ", name=" + tes.getName() + ", raw=" + tes.getRawScore() + ", score=" + tes.getScore() );
            
            if( tes.getTestEventScoreId() > 0 )
            {
                em.merge(tes );
            }

            else
            {
                // em.detach( testEventScore );

                em.persist(tes );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            if( ndsFlush )
                em.flush();

            return tes;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.saveTestEventScore() " + ( tes == null ? "testEventScore is null" : tes.toString() ) );

            throw new STException( e );
        }
    }


    public BatteryScore saveBatteryScore( BatteryScore bs ) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( bs.getBatteryScoreId() > 0 )
            {
                em.merge( bs );
            }

            else
            {
                em.detach( bs );

                em.persist( bs );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return bs;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.saveBatteryScore() " + ( bs == null ? "BatteryScore is null" : bs.toString() ) );

            throw new STException( e );
        }
    }



    public ItemResponse saveItemResponse( ItemResponse ir ) throws Exception
    {
        try
        {
            if( ir.isArchive() )
                throw new Exception( "Cannot save an archive ItemResponse." );
            
            if( ir.getTestEventId()<=0 && ir.getSurveyEventId()<= 0 )
                throw new Exception( "Either ItemResponse.testEventId or ItemResponse.surveyEventId must be non-zero." );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getItemResponseId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                em.detach( ir );

                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            // em.flush();

            return ir;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.saveItemResponse() " + ir.toString() );

            throw new STException( e );
        }
    }


    /**
     * Returns
     *     float[0] = number of events
     *     float[1] = mean value of raw scores
     *     float[2] = standard deviation of raw scores
     *
     * @param productId
     * @return
     * @throws Exception
     */
    public float[] getRawScoreStatisticsForProductId( int productId ) throws Exception
    {
        float[] out = new float[3];

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql = "SELECT COUNT(1), AVG(rawscore),STD(rawscore) FROM percentile WHERE productid=" + productId + " AND simletid=0 AND simletcompetencyid=0";

        List<Long> ol = new ArrayList<>();

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
             con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

             ResultSet rs = stmt.executeQuery( sql );

            // long id;

             if( rs.next() )
             {
                 out[0] = rs.getFloat( 1 );
                 out[1] = rs.getFloat( 2 );
                 out[2] = rs.getFloat( 3 );
             }

             rs.close();

             return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getRawScoreStatisticsForProductId() " + sql );

            throw new STException( e );
        }

    }

    public Percentile savePercentile( Percentile ir ) throws Exception
    {
        try
        {
            // LogService.logIt( "EventFacade.savePercentile() " + ir.toString() );

            if( ir.getTestEventId()<=0 || ir.getTestEventScoreId()<=0 )
                throw new Exception( "Percentile.testEventId or Percentile.TestEventScoreId must be non-zero." );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getPercentileId() > 0 )
            {
                em.merge( ir );
            }

            else
            {
                // em.detach( ir );

                em.persist( ir );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            // em.flush();

            return ir;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.savePercentile() " + ir.toString() );

            throw new STException( e );
        }
    }



    public SimDescriptor getSimDescriptor( long simId, int simVersionId, boolean refresh ) throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            List<SimDescriptor> l = null;

            if( simVersionId<=0 )
            {
                if( refresh )
                    l = em.createNamedQuery( "SimDescriptor.findBySimId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "simId", simId ).getResultList();

                else
                    l = em.createNamedQuery( "SimDescriptor.findBySimId" ).setParameter( "simId", simId ).getResultList();
            }

            else
            {
                if( refresh )
                    l = em.createNamedQuery( "SimDescriptor.findBySimIdAndSimVersionId" ).setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" ).setParameter( "simId", simId ).setParameter( "simVersionId", simVersionId ).getResultList();

                else
                    l = em.createNamedQuery( "SimDescriptor.findBySimIdAndSimVersionId" ).setParameter( "simId", simId ).setParameter( "simVersionId", simVersionId ).getResultList();
            }

            return l!=null && l.size()>0 ? l.get(0) : null;

            // return em.find( SimDescriptor.class, new Long( simDescriptorId ) );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.getSimDescriptor( simId=" + simId + ", simVersionId=" + simVersionId + ", refresh=" + refresh + " ) " );

            throw new STException( e );
        }
    }


    public SurveyEvent saveSurveyEvent( SurveyEvent surveyEvent ) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( surveyEvent.getSurveyEventId() > 0 )
            {
                //LogService.logIt( "EventFacade.saveTestEvent() BBB Before merge " + testEvent.toString() );
                em.merge( surveyEvent );
                //LogService.logIt( "EventFacade.saveTestEvent() BBB AFTER merge " + testEvent.toString() );
            }

            else
            {
                em.detach( surveyEvent );

                em.persist( surveyEvent );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            // em.flush();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.saveSurveyEvent() " + surveyEvent.toString()  );
        }
        return surveyEvent;
    }


    public void reorderItemResponses( long testEventId , long surveyEventId )
    {
        try
        {
            List<ItemResponse> irl;

            if( testEventId > 0 )
                irl = getItemResponsesForTestEvent( testEventId );

            else
                irl = getItemResponsesForSurveyEvent( surveyEventId );

            Collections.sort( irl );

            int ct = 1;

            //String irUnqId="";
            //String irPre = "";
            //String irAfter = "";
            float ex;
            
            for( ItemResponse ir : irl )
            {
                //irPre += "," + ir.getDisplayOrder();
               // irAfter += "," + ct;
               // irUnqId += ", " + ir.getSimletNodeUniqueId();
                ex = ir.getDisplayOrder();
                
                if( ex != (float) ct )
                {                
                    ir.setDisplayOrder( ct );
                    saveItemResponse(ir);
                }
                
                ct++;
            }

            //LogService.logIt("EventFacade.reorderItemResponses \npre=" + irPre + "\n" + irAfter + "\n" + irUnqId );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.reorderItemResponses() testEventId=" + testEventId + ", surveyEventId=" + surveyEventId  );
        }

    }

    //private void flushEntityManager()
    //{
   //     try
    //    {
    //        Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
    //        EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );
    //        em.flush();
    //    }
   ////     catch( Exception e )
    //    {
    //        LogService.logIt( e, "EventFacade.flushEntityManager() " );
    //    }
    //}
    

    // @TransactionAttribute(value=NOT_SUPPORTED)
    public TestEvent saveTestEvent( TestEvent te ) throws Exception
    {
        try
        {
            if( te.getIsSurveyEvent() )
            {
                return saveSurveyEvent(te.getSurveyEvent() ).getTestEvent();
            }
            //LogService.logIt( "EventFacade.saveTestEvent() " + testEvent.toString() );

            if( te.getTestEventArchiveId()> 0 )
            {
                TestEventArchive tea = te.getTestEventArchive();
                saveTestEventArchive(tea);
                return tea.getTestEvent();
            }

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );
            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( te.getTestEventId() > 0 )
            {
                if( !unarchivedTestEventExists(te.getTestEventId() ) )
                    throw new ScoringException( "Unarchived TestEvent record does not exist. Has probably been archived. testEventId=" + te.getTestEventId(), ScoringException.NON_PERMANENT, null  );
                //LogService.logIt( "EventFacade.saveTestEvent() BBB Before merge " + testEvent.toString() );
                em.merge(te );
                //LogService.logIt( "EventFacade.saveTestEvent() BBB AFTER merge " + testEvent.toString() );
            }

            else
            {
                em.detach(te );
                em.persist(te );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return te;
        }

        catch( ScoringException e )
        {
            throw e;
        }        
        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.saveTestEvent() " + ( te == null ? "testEvent is null" : te.toString() ) );

            throw new STException( e );
        }
    }


    public void flushEntityManager() throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            em.flush();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.flushEntityManager() " );

            throw new STException( e );
        }
    }


    public Date getLastScoringUpdate( long simId ) throws Exception
    {
        if( simId<=0 )
            return null;
        
        String sql = "SELECT lastscoringupdate FROM simdescriptor WHERE simid=" + simId + " ORDER BY simversionid DESC LIMIT 1";
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
        
        int count = 0;        
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sql );
             
            Date d = null;
            if( rs.next() )
                d = rs.getDate(1);

            rs.close();
            return d;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.getLastScoringUpdate( simId=" + simId + " ) " );
            return null;
        }           
    }
    
    


    public Product getProduct( int productId ) throws Exception
    {
        try
        {
            if( productId <= 0 )
                throw new Exception( "productId is invalid " + productId );

            //if( tm2Factory == null ) tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            // else it's a system type (0 or 1)
            return emmirror.find( Product.class,  productId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getProduct( " + productId + " )" );

            throw new STException( e );
        }
    }



    public Report getReport( long reportId ) throws Exception
    {
        try
        {
            if( reportId <= 0 )
                throw new Exception( "reportId is invalid " + reportId );

            //if( tm2Factory == null ) tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            //EntityManager em = tm2Factory.createEntityManager();

            // else it's a system type (0 or 1)
            return emmirror.find( Report.class,  reportId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getReport( " + reportId + " )" );

            throw new STException( e );
        }
    }


    public void saveTestEventLog( TestEventLog tel )
    {
        try
        {
            if( tel.getLog()==null )
                return; // tel;

            //if( tel.getTestEventId()<=0 )
            //    throw new Exception( "TestEventId=0" );

            if( tel.getLogDate()==null )
                tel.setLogDate( new Date() );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( tel.getTestEventLogId() > 0 )
            {
                em.merge( tel );
            }

            else
            {
                // em.detach( tel );

                em.persist( tel );
            }

            // This causes any exceptions to be thrown here instead of in the EJB transaction.
            // Makes it easier to figure out what went wrong.
            em.flush();

            return; //  tel;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "EventFacade.saveTestEventLog() " + ( tel == null ? "testEvent is null" : tel.toString() ) );
            // throw new STException( e );
        }
        // return null;
    }

    // out[0] = creditId
    // out[1] = creditIndex
    public long[] checkCreditIdForTestKey( long testKeyId ) throws Exception
    {
        // out[0] = creditId
        // out[1] = creditIndex
        long[] out = new long[2];

        if( testKeyId <= 0 || testKeyId<=0 )
            return out;

        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sql;

            sql = "(SELECT creditid AS 'cid', creditindex as 'cdx' from testkeyarchive WHERE testkeyid=" + testKeyId + " AND creditid>0) " +
                     " UNION ALL " +
                     "(SELECT creditid AS 'cid', creditindex as 'cdx' from testkey WHERE testkeyid=" + testKeyId  + " AND creditid>0) ";

        // LogService.logIt( "PurchaseFacade.findTestingCreditIdToUseForRef( orgId=" + orgId + ", candidateUserId=" + candidateUserId + " ) sql=" + sql );
        try (Connection con = pool.getConnection();
             Statement stmt = con.createStatement() )
        {
            ResultSet rs = stmt.executeQuery( sql );

            if( rs.next() )
            {
                out[0] = rs.getLong(1);
                out[1] = rs.getInt(2);
            }
            rs.close();
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "EventFacade.checkCreditIdForTesTKey( testKeyId=" + testKeyId + " ) " );
            throw new STException( e );
        }
    }

}
