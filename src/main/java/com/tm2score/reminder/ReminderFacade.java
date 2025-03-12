package com.tm2score.reminder;

import com.tm2score.entity.purchase.Credit;
import com.tm2score.entity.user.OrgAutoTest;
import com.tm2score.event.TestKeyStatusType;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.user.OrgAutoTestStatusType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.*;
import javax.sql.DataSource;


//@RequestScoped
@Stateless // ( name = "EventFacade", mappedName="EventFacade" )
public class ReminderFacade
{
    
    
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    //@PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    //EntityManager emmirror;

    public static ReminderFacade getInstance()
    {
        try
        {
            // return (EventFacade) InitialContext.doLookup( "java:global/tm2score2/EventFacade" );
            return (ReminderFacade) InitialContext.doLookup( "java:module/ReminderFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }


    public Set<Long> getTestKeyIdsForInvitations(int hoursBefore, int windowMins) throws Exception
    {
        Set<Long> out = new HashSet<>();
        
        //if( reminderDays <= 0 )
        //    return out;
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DAY_OF_MONTH, 1 );
        java.sql.Timestamp expDate = new java.sql.Timestamp( cal.getTime().getTime() );
        
        cal = new GregorianCalendar();
        java.sql.Timestamp ssDate = new java.sql.Timestamp( cal.getTime().getTime() );
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
                
        String sqlStr = "SELECT tk.testkeyid FROM testkey AS tk WHERE tk.sendstartdate IS NOT NULL and tk.sendstartdate<='" + ssDate.toString() + "' AND tk.statustypeid=" + TestKeyStatusType.ACTIVE.getTestKeyStatusTypeId() + " AND tk.expiredate IS NOT NULL AND tk.expiredate>'" + expDate.toString() + "' AND tk.lastemaildate IS NULL AND tk.lasttextdate IS NULL ";

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            while( rs.next() )
            {
                out.add( rs.getLong(1) );
            }

             rs.close();

             return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReminderFacade.getTestKeyIdsForInvitations() " + sqlStr );

            throw new STException( e );
        }        
    }
    
    
    public Set<Long> getTestKeyIdsForReminders(int hoursBefore, int windowMins) throws Exception
    {
        Set<Long> out = new HashSet<>();
        
        //if( reminderDays <= 0 )
        //    return out;
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DAY_OF_MONTH, 1 );
        java.sql.Timestamp expDate = new java.sql.Timestamp( cal.getTime().getTime() );
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
                
        String sqlStr = "SELECT tk.testkeyid FROM testkey AS tk WHERE tk.reminderdays>0 AND tk.statustypeid IN (" + TestKeyStatusType.ACTIVE.getTestKeyStatusTypeId() + "," + TestKeyStatusType.STARTED.getTestKeyStatusTypeId() + ") AND tk.expiredate IS NOT NULL AND tk.expiredate>'" + expDate.toString() + "' AND ( (tk.lastemaildate IS NOT NULL AND UNIX_TIMESTAMP(tk.lastemaildate)<=UNIX_TIMESTAMP() - (tk.reminderdays*24*60*60) ) or ( tk.lasttextdate IS NOT NULL AND UNIX_TIMESTAMP(tk.lasttextdate)<=UNIX_TIMESTAMP() - (tk.reminderdays*24*60*60)  ) ) ";

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            while( rs.next() )
            {
                out.add( rs.getLong(1) );
            }

             rs.close();

             return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReminderFacade.getTestKeyIdsForReminders() " + sqlStr );

            throw new STException( e );
        }        
    }
    
    public Set<Long> getTestKeyIdsForExpirationReminders(int daysBefore) throws Exception
    {
        Set<Long> out = new HashSet<>();
        
        //if( reminderDays <= 0 )
        //    return out;
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.DAY_OF_MONTH, -1*daysBefore );
        
        // No emails in last 24 hours.
        java.sql.Timestamp maxLastWarnDate = new java.sql.Timestamp( cal.getTime().getTime() );
        
        cal = new GregorianCalendar();
        cal.add( Calendar.DAY_OF_MONTH, daysBefore );
        cal.add( Calendar.HOUR, 1 );
        
        java.sql.Timestamp expDateEnd = new java.sql.Timestamp( cal.getTime().getTime() );

        //cal = new GregorianCalendar();
        // cal.add( Calendar.DAY_OF_MONTH, daysBefore - 1 );        
        //java.sql.Timestamp expDateStrt = new java.sql.Timestamp( cal.getTime().getTime() );
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
                
        String sqlStr = "SELECT tk.testkeyid FROM testkey AS tk WHERE tk.statustypeid<=" + TestKeyStatusType.STARTED.getTestKeyStatusTypeId() + " AND tk.expirewarndays=" + daysBefore + " AND tk.expiredate IS NOT NULL AND tk.expiredate<='" + expDateEnd.toString() + "' AND (tk.lastexpirewarningdate IS NULL OR tk.lastexpirewarningdate<'" + maxLastWarnDate.toString() + "') ";

        // LogService.logIt( "ReminderFacade.getTestKeyIdsForExpirationReminders() BBB " + sqlStr );
        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            while( rs.next() )
            {
                out.add( rs.getLong(1) );
            }

             rs.close();

             return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReminderFacade.getTestKeyIdsForExpirationReminders() " + sqlStr );
            throw new STException( e );
        }        
    }

    
    public List<Credit> getPendingCreditZeros() throws Exception
    {
        try
        {
            //if( tm2Factory == null )
            //    tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

           // EntityManager em = tm2Factory.createEntityManager();

            TypedQuery<Credit> q = em.createNamedQuery("Credit.findByPendingCreditZeroStatusTypeId", Credit.class);
            q.setHint( "jakarta.persistence.cache.retrieveMode", "BYPASS" );
            return q.getResultList();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReminderFacade.getPendingCreditZeros() " );
            throw new STException( e );
        }
    }

    
    /**
     * Returns 
     *    data[0] = orgid
     *    data[1] = number of credits.
     * 
     * @param expireDays
     * @return
     * @throws Exception 
     */
    public List<Integer[]> getOrgInfoListForCreditsExpiration( int expireDays ) throws Exception
    {
        List<Integer[]> out = new ArrayList<>();
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );
        if( pool == null )
            throw new Exception( "Can not find Datasource" );
                
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = new GregorianCalendar(); 
        cal.add( Calendar.DAY_OF_MONTH, expireDays );
        cal.setTimeZone(tz);
        java.sql.Timestamp ts = new java.sql.Timestamp( cal.getTimeInMillis() );

        cal = new GregorianCalendar(); 
        cal.add( Calendar.DAY_OF_MONTH, expireDays + 1 );
        cal.setTimeZone(tz);
        java.sql.Timestamp ts2 = new java.sql.Timestamp( cal.getTimeInMillis() );
                
        // Get all about to expire.
        String sqlStr = "SELECT c.orgid,sum(c.remainingcount) FROM org AS o INNER JOIN credit AS c ON c.orgid=o.orgid WHERE o.orgstatustypeid=0 AND o.adminuserid>0 AND o.orgcreditusagetypeid>=1 AND o.orgcreditusagetypeid<=3 AND c.creditstatustypeid=1 and c.credittypeid=1 AND c.creditsourcetypeid IN (1,2,3) AND c.remainingcount>0 AND c.expiredate IS NOT NULL AND c.expiredate>='" + ts.toString() + "' AND c.expiredate<'" +  ts2.toString()+  "' group by c.orgid order by c.orgid ";

                
        // "SELECT o.orgid FROM org AS o WHERE o.orgstatustypeid=0 AND o.adminuserid>0 AND o.orgcreditusagetypeid=10 AND o.orgcreditusageenddate IS NOT NULL AND o.orgcreditusageenddate>='" + ts.toString() + "' AND o.orgcreditusageenddate<'" + ts2.toString() + "' ";
        // String sqlStr = "SELECT o.orgid FROM org AS o WHERE o.orgstatustypeid=0 AND o.adminuserid>0 AND o.orgcreditusagetypeid IN (1,2) AND o.orgcreditusageenddate IS NOT NULL AND o.orgcreditusageenddate>='" + ts.toString() + "' AND o.orgcreditusageenddate<'" + ts2.toString() + "' ";
        
        LogService.logIt( "ReminderFacade.getOrgInfoListForCreditsExpiration() AAA " + sqlStr );
        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );
            while( rs.next() )
            {
                out.add( new Integer[] {rs.getInt(1),rs.getInt(2)} );
            }

            rs.close();             
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReminderFacade.getOrgInfoListForCreditsExpiration() " + sqlStr );

            throw new STException( e );
        }                
    }
    
    
    public Set<Integer> getOrgIdListForExpiration( int expireDays ) throws Exception
    {
        Set<Integer> out = new HashSet<>();
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );
                
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = new GregorianCalendar(); 
        cal.add( Calendar.DAY_OF_MONTH, expireDays );
        cal.setTimeZone(tz);
        java.sql.Timestamp ts = new java.sql.Timestamp( cal.getTimeInMillis() );

        cal = new GregorianCalendar(); 
        cal.add( Calendar.DAY_OF_MONTH, expireDays + 1 );
        cal.setTimeZone(tz);
        java.sql.Timestamp ts2 = new java.sql.Timestamp( cal.getTimeInMillis() );
                
        // Get all about to expire.
        String sqlStr = "SELECT o.orgid FROM org AS o WHERE o.orgstatustypeid=0 AND o.adminuserid>0 AND o.orgcreditusagetypeid=10 AND o.orgcreditusageenddate IS NOT NULL AND o.orgcreditusageenddate>='" + ts.toString() + "' AND o.orgcreditusageenddate<'" + ts2.toString() + "' ";
        // String sqlStr = "SELECT o.orgid FROM org AS o WHERE o.orgstatustypeid=0 AND o.adminuserid>0 AND o.orgcreditusagetypeid IN (1,2) AND o.orgcreditusageenddate IS NOT NULL AND o.orgcreditusageenddate>='" + ts.toString() + "' AND o.orgcreditusageenddate<'" + ts2.toString() + "' ";
        
        // LogService.logIt( "ReminderFacade.getOrgIdListForExpiration() AAA " + sqlStr );
        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            while( rs.next() )
            {
                out.add( rs.getInt(1) );
            }

            rs.close();
             
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReminderFacade.getOrgIdListForExpiration() " + sqlStr );

            throw new STException( e );
        }                
    }
    
    public Set<Integer> getOrgAutoTestIdListForExpiration( int hoursBefore, int minutesWindow ) throws Exception
    {
        Set<Integer> out = new HashSet<>();
        
        //if( reminderDays <= 0 )
        //    return out;
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );
                
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(tz);
        java.sql.Timestamp ts = new java.sql.Timestamp( cal.getTimeInMillis() );
        // java.sql.Timestamp sDate = new java.sql.Timestamp( cal.getTime().getTime() );


        
        if( hoursBefore>0 )
        {
        }
        
        
        // Get all about to expire.
        String sqlStr = "SELECT o.orgautotestid FROM orgautotest AS o WHERE o.orgautoteststatustypeid=" + OrgAutoTestStatusType.ACTIVE.getOrgAutoTestStatusTypeId() + " ";
        
        sqlStr += " AND o.expiredate<'" + ts.toString() + "' ";

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            while( rs.next() )
            {
                out.add( rs.getInt(1) );
            }

             rs.close();

            if( hoursBefore>0 && minutesWindow>0 )
            {
                cal = new GregorianCalendar();
                cal.setTimeZone(tz);
                cal.add( Calendar.HOUR, hoursBefore );
                ts = new java.sql.Timestamp( cal.getTimeInMillis() );
                // sDate = new java.sql.Timestamp( cal.getTime().getTime() );

                Calendar cal2 = new GregorianCalendar();
                cal2.setTimeZone(tz);
                cal2.add( Calendar.HOUR, hoursBefore );
                cal2.add( Calendar.MINUTE, -1*minutesWindow );
                java.sql.Timestamp ts2 = new java.sql.Timestamp( cal2.getTimeInMillis());
                // java.sql.Timestamp sDate2 = new java.sql.Timestamp( cal2.getTime().getTime() );
                sqlStr = "SELECT o.orgautotestid FROM orgautotest AS o WHERE o.orgautoteststatustypeid=" + OrgAutoTestStatusType.ACTIVE.getOrgAutoTestStatusTypeId() + " AND o.expiredate<='" + ts.toString() + "' AND o.expiredate>='" + ts2.toString() + "' ";
                
                // LogService.logIt( "ReminderFacade.getTestKeyIdsForReminders() Warning sqlStr=" + sqlStr );
                rs = stmt.executeQuery( sqlStr );
                while( rs.next() )
                {
                    out.add( rs.getInt(1) );
                }
                rs.close();             
            }
            
            sqlStr = "SELECT o.orgautotestid FROM orgautotest AS o WHERE o.orgautoteststatustypeid=" + OrgAutoTestStatusType.ACTIVE.getOrgAutoTestStatusTypeId() + " AND o.maxevents>0 AND o.eventcount>=o.maxevents";

            rs = stmt.executeQuery( sqlStr );
            while( rs.next() )
            {
                out.add( rs.getInt(1) );
            }
            rs.close();             
             
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReminderFacade.getTestKeyIdsForReminders() " + sqlStr );

            throw new STException( e );
        }        
    }

    

    public OrgAutoTest getOrgAutoTest( int orgAutoTestId ) throws Exception
    {
        try
        {
            if( orgAutoTestId <= 0 )
                throw new Exception( "orgAutoTestId is invalid " + orgAutoTestId );

            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            // else it's a system type (0 or 1)
            return em.find(OrgAutoTest.class, orgAutoTestId );
        }

        catch( NoResultException e )
        {
            return null;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "getOrgAutoTest( " + orgAutoTestId + " )" );

            throw new STException( e );
        }
    }
    

    public OrgAutoTest saveOrgAutoTest( OrgAutoTest ir ) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getOrgAutoTestId() > 0 )
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
            LogService.logIt( e, "EventFacade.saveOrgAutoTest() " + ir.toString() );

            throw new STException( e );
        }
    }

    
    public Credit saveCredit( Credit ir ) throws Exception
    {
        try
        {
            //Context envCtx = (Context) new InitialContext().lookup( "java:comp/env" );

            //EntityManager em = (EntityManager) envCtx.lookup( "persistence/tm2" );

            if( ir.getCreditId() > 0 )
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
            LogService.logIt( e, "EventFacade.saveCredit() " + ir.toString() );
            throw new STException( e );
        }
    }

    
    

}
