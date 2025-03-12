/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.report;

import com.tm2score.entity.purchase.Product;
import com.tm2score.entity.report.Report;
import com.tm2score.global.STException;
import com.tm2score.purchase.ProductType;
import com.tm2score.service.LogService;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.Stateless;
import javax.naming.InitialContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import javax.sql.DataSource;

/**
 *
 * @author Mike
 */
@Stateless
// @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
public class LangEquivFacade
{

    // private static final String PERSISTENCE_UNIT_NAME = "tm2";

    // private static EntityManagerFactory tm2Factory;
    @PersistenceContext( name = "persistence/tm2", unitName = "tm2" )
    EntityManager em;

    @PersistenceContext( name = "persistence/tm2mirror", unitName = "tm2mirror" )
    EntityManager emmirror;

    public static LangEquivFacade getInstance()
    {
        try
        {
            return (LangEquivFacade) InitialContext.doLookup( "java:module/LangEquivFacade" );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getInstance() " );

            return null;
        }
    }

    public Product findProductWithTgtLangPointingToEngEquivSimId( long engEquivSimId, String localeStr ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT p.productid FROM product AS p WHERE (p.producttypeid=" + ProductType.SIM.getProductTypeId() + " OR p.producttypeid=" + ProductType.CT5DIRECTTEST.getProductTypeId() + ") AND p.longparam1>0 AND p.longparam4=" + engEquivSimId + " AND p.lang LIKE '" + localeStr + "%'" ;

        // LogService.logIt( "EventFacade.getAverageItemScoreForItemScore() " + sqlStr );

        // List<Long> ol = new ArrayList<>();
        int productId = 0;
        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                productId = rs.getInt(1);

            rs.close();
            
            return productId>0 ? getProduct(productId) : null;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.findProductWithTgtLangPointingToEngEquivSimId() " + sqlStr  );

            throw new STException( e );
        }
    }
    
    
    public long findReportIdWithTgtLangPointingToEngEquiv( long engEquivReportId, String localeStr ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT r.reportid FROM report AS r WHERE r.longparam1=" + engEquivReportId + " AND r.localestr LIKE '" + localeStr + "%'" ;

        // LogService.logIt( "EventFacade.getAverageItemScoreForItemScore() " + sqlStr );

        // List<Long> ol = new ArrayList<>();

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                return rs.getLong(1);

            rs.close();
            
            return 0;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.findReportIdPointingToEnglishEquivalent() " + sqlStr  );

            throw new STException( e );
        }
    }
    
    public boolean isReportAnEnglishEquivalent( long sourceReportId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT r.reportid FROM report AS r WHERE r.longparam1=" + sourceReportId ;

        // LogService.logIt( "EventFacade.getAverageItemScoreForItemScore() " + sqlStr );

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                return true;

            rs.close();
            
            return false;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.isReportAnEnglishEquivalent() " + sqlStr  );

            throw new STException( e );
        }
        
    }
    
    
    public boolean isProductAnEnglishEquivalent( Product p ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT p.productid FROM product AS p WHERE (p.producttypeid=" + ProductType.SIM.getProductTypeId() + " OR p.producttypeid=" + ProductType.CT5DIRECTTEST.getProductTypeId() + ") AND p.longparam4=" + p.getLongParam1();

        // LogService.logIt( "EventFacade.getAverageItemScoreForItemScore() " + sqlStr );

        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                return true;

            rs.close();
            
            return false;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.isProductAnEnglishEquivalent() " + sqlStr  );

            throw new STException( e );
        }
        
    }
    
    
    public Product findAnyProductWithSocAndConsumerTypeThatIsInEnglish( String onetSoc, int consumerProductTypeId ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT p.productid FROM product AS p WHERE p.producttypeid=" + ProductType.SIM.getProductTypeId() + 
                " AND p.longparam1>0 AND p.onetsoc='" + onetSoc + "' AND p.consumerproducttypeid=" + consumerProductTypeId + " " +
                " AND p.lang LIKE 'en%' ";

        // LogService.logIt( "LangEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv() " + sqlStr );

        int productId = 0;
                        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                productId = rs.getInt(1);

            rs.close();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv() " + sqlStr  );

            throw new STException( e );
        }
        
        return productId>0 ? getProduct( productId ) : null;
    }
    
    
    
    public Product findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv( String onetSoc, int consumerProductTypeId, String localeStr ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT p.productid FROM product AS p WHERE (p.producttypeid=" + ProductType.SIM.getProductTypeId() + " OR p.producttypeid=" + ProductType.CT5DIRECTTEST.getProductTypeId() + ") AND p.longparam4>0 AND p.onetsoc='" + onetSoc + "' AND p.consumerproducttypeid=" + consumerProductTypeId + " ";
        
        if( localeStr!=null && !localeStr.isEmpty() )
            sqlStr += " AND p.lang LIKE '" + localeStr + "%' ";

        // LogService.logIt( "LangEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv() " + sqlStr );

        int productId = 0;
                        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                productId = rs.getInt(1);

            rs.close();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv() " + sqlStr  );

            throw new STException( e );
        }
        
        return productId>0 ? getProduct( productId ) : null;
    }
            
    
    public Product findAnyProductWithNameAndConsumerTypeThatNamesAnEnglishEquiv( String name, String nameEnglish, int consumerProductTypeId, String localeStr ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT p.productid FROM product AS p WHERE (p.producttypeid=" + ProductType.SIM.getProductTypeId() + " OR p.producttypeid=" + ProductType.CT5DIRECTTEST.getProductTypeId() + ") AND p.longparam4>0 AND p.consumerproducttypeid=" + consumerProductTypeId + " ";
        
        if( localeStr!=null && !localeStr.isEmpty() )
            sqlStr += " AND p.lang LIKE '" + localeStr + "%' ";

        if( name.indexOf("(")>0 )
            name = name.substring(0,name.indexOf("(")).trim();
        
        if( nameEnglish!=null && nameEnglish.indexOf("(")>0 )
            nameEnglish = nameEnglish.substring(0,nameEnglish.indexOf("(")).trim();
        
        sqlStr += " AND (name LIKE '" + name + "%' ";
        
        if( nameEnglish!=null && !nameEnglish.isEmpty() )
            sqlStr += " OR nameenglish LIKE '" + nameEnglish + "%' ";
        
        sqlStr += ") ";
        
        // LogService.logIt( "LangEquivFacade.findAnyProductWithSocAndConsumerTypeThatNamesAnEnglishEquiv() " + sqlStr );

        int productId = 0;
                        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                productId = rs.getInt(1);

            rs.close();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.findAnyProductWithNameAndConsumerTypeThatNamesAnEnglishEquiv() " + sqlStr  );

            throw new STException( e );
        }
        
        return productId>0 ? getProduct( productId ) : null;
        
    }
            
    
    
    public Product getProduct( int productId ) throws Exception
    {
        try
        {
            if( productId <= 0 )
                throw new Exception( "productId is invalid " + productId );

            // if( tm2Factory == null ) tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

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
    
    
    public Report findAnyReportWithSameNameAndClassThatNamesAnEnglishEquiv( String name, String nameEnglish, String impClass, String localeStr ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT r.reportid FROM report AS r WHERE r.longparam1>0 " ;
        
        if( localeStr!=null && !localeStr.isEmpty() )
            sqlStr += " AND r.localestr LIKE '" + localeStr + "%' ";

        // LogService.logIt( "EventFacade.getAverageItemScoreForItemScore() " + sqlStr );

        long reportId = 0;
        
        if( impClass == null || impClass.isEmpty() )
        {
            sqlStr += " AND ( r.implementationclass IS NULL OR r.implementationclass='' ) ";
        }
        
        else
            sqlStr += " AND r.implementationclass='" +  impClass + "' ";
        
        if( name.indexOf("(")>0 )
            name = name.substring(0,name.indexOf("(")).trim();
        
        if( nameEnglish!=null && nameEnglish.indexOf("(")>0 )
            nameEnglish = nameEnglish.substring(0,nameEnglish.indexOf("(")).trim();
        
        sqlStr += " AND (name LIKE '" + name + "%' ";
        
        if( nameEnglish!=null && !nameEnglish.isEmpty() )
            sqlStr += " OR nameenglish LIKE '" + nameEnglish + "%' ";
        
        sqlStr += ") ";
        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            if( rs.next() )
                reportId = rs.getLong(1);

            rs.close();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.findAnyReportWithSameNameAndClassThatNamesAnEnglishEquiv() " + sqlStr  );

            throw new STException( e );
        }
        
        return reportId>0 ? getReport( reportId ) : null;
    }
    
    
    public long findAnyReportWithSameClassThatIsAnEnglishEquiv( String impClass ) throws Exception
    {
        DataSource pool = (DataSource) new InitialContext().lookup( "jdbc/tm2mirror" );

        if( pool == null )
            throw new Exception( "Can not find Datasource" );

        String sqlStr = "SELECT r.reportid FROM report AS r WHERE r.implementationclass='" + impClass + "'" ;

        // LogService.logIt( "EventFacade.getAverageItemScoreForItemScore() " + sqlStr );

        List<Long> rids = new ArrayList<>();
        
        try (Connection con = pool.getConnection(); Statement stmt = con.createStatement() )
        {
            con.setTransactionIsolation( Connection.TRANSACTION_READ_UNCOMMITTED );

            ResultSet rs = stmt.executeQuery( sqlStr );

            while( rs.next() )
            {
                rids.add( rs.getLong(1) );
            }

            rs.close();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "LangEquivFacade.findAnyReportWithSameClassThatIsAnEnglishEquiv() " + sqlStr  );

            throw new STException( e );
        }
        
        for( Long rid : rids )
        {
            if( isReportAnEnglishEquivalent( rid  ) )
                return rid;
        }
        
        return 0;
    }
    
    
    public Report getReport( long reportId ) throws Exception
    {
        try
        {
            if( reportId <= 0 )
                throw new Exception( "reportId is invalid " + reportId );

            // if( tm2Factory == null ) tm2Factory = PersistenceManager.getInstance().getEntityManagerFactory();

            // EntityManager em = tm2Factory.createEntityManager();

            // else it's a system type (0 or 1)
            return emmirror.find( Report.class, reportId );
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



}
