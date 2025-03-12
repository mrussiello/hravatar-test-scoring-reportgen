package com.tm2score.custom.misc;

import com.tm2score.event.TestEventStatusType;
import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.*;
import javax.sql.DataSource;


//@RequestScoped
@Stateless // ( name = "EventFacade", mappedName="EventFacade" )
public class IframeTestFacade
{
    
    // private static final String PERSISTENCE_UNIT_NAME = "tm2";
    private static EntityManagerFactory tm2Factory;

    public static IframeTestFacade getInstance()
    {
        try
        {
            return (IframeTestFacade) InitialContext.doLookup( "java:module/IframeTestFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "IframeTestFacade.getInstance() " );

            return null;
        }
    }

    
    

    public Set<Long> getTestEventIdsToAutoComplete( int hoursSinceLastAccess ) throws Exception
    {
        Set<Long> out = new HashSet<>();
        
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );
                
        GregorianCalendar cal = new GregorianCalendar();
        cal.add( Calendar.HOUR, -1*hoursSinceLastAccess );
        java.sql.Timestamp sDate = new java.sql.Timestamp( cal.getTime().getTime() );
        
        String sqlStr = "SELECT te.testeventid FROM testevent AS te WHERE te.testeventstatustypeid=" + TestEventStatusType.STARTED.getTestEventStatusTypeId() + " AND te.producttypeid=41  AND te.lastaccessdate<'" + sDate + "'";

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
            LogService.logIt( e, "IframeTestFacade.getTestEventIdsToAutoComplete() " + sqlStr );
            throw new STException( e );
        }        
    }
    
    
    

}
