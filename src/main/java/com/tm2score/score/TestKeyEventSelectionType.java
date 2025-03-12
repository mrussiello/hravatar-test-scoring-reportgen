package com.tm2score.score;

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.model.SelectItem;



public enum TestKeyEventSelectionType
{
    ALL(0, "All Eligible Events" ),
    ODD(1, "Odd Numbers Only" ),
    EVEN(2, "Even Numbers Only" ),
    THREEA(10, "0-2 Only" ),
    THREEB(11, "3-5 Only" ),
    THREEC(12, "6-9 Only" ),
    FOURA(20, "0-1 Only" ),
    FOURB(21, "2-4 Only" ),
    FOURC(22, "5-7 Only" ),
    FOURD(23, "8-10 Only" ),
    FIVEA(30, "0-1 Only" ),
    FIVEB(31, "2-3 Only" ),
    FIVEC(32, "4-5 Only" ),
    FIVED(33, "6-7 Only" ),
    FIVEE(34, "8-9 Only" ),
    NONE(99, "None" ); 

    private final int testKeyEventSelectionType;

    private String key;


    private TestKeyEventSelectionType( int p,
                         String key )
    {
        this.testKeyEventSelectionType = p;
        this.key = key;
    }

    
    public boolean getIsAllOrNone()
    {
        return equals(NONE) || equals(ALL);
    }
    
    
    
    public boolean keep( long id )
    {
        if( equals( NONE ) )
            return false;
        if( equals( ALL ) )
            return true;
        
        int lastDigit = lastDigit( id ) ;
        
        
        if( equals( ODD ) )
            return lastDigit % 2 == 1;
        if( equals( EVEN ) )
            return lastDigit % 2 == 0;
        if( equals( THREEA ) )
            return lastDigit>=0 && lastDigit<=2; //  " right(" + varName + ",1) IN (0,1,2) ";
        if( equals( THREEB ) )
            return lastDigit>=3 && lastDigit<=5; //  " right(" + varName + ",1) IN (3,4,5) ";
        if( equals( THREEC ) )
            return lastDigit>=6 && lastDigit<=9; //  " right(" + varName + ",1) IN (6,7,8,9) ";
        if( equals( FOURA ) )
            return lastDigit>=0 && lastDigit<=1; //  " right(" + varName + ",1) IN (0,1) ";
        if( equals( FOURB ) )
            return lastDigit>=2 && lastDigit<=4; //  " right(" + varName + ",1) IN (2,3,4) ";
        if( equals( FOURC ) )
            return lastDigit>=5 && lastDigit<=7; //  " right(" + varName + ",1) IN (5,6,7) ";
        if( equals( FOURD ) )
            return lastDigit>=8 && lastDigit<=10; //  " right(" + varName + ",1) IN (8,9,10) ";
        if( equals( FIVEA ) )
            return lastDigit>=0 && lastDigit<=1; //  " right(" + varName + ",1) IN (0,1) ";
        if( equals( FIVEB ) )
            return lastDigit>=2 && lastDigit<=3; //  " right(" + varName + ",1) IN (2,3) ";
        if( equals( FIVEC ) )
            return lastDigit>=4 && lastDigit<=5; //  " right(" + varName + ",1) IN (4,5) ";
        if( equals( FIVED ) )
            return lastDigit>=6 && lastDigit<=7; //  " right(" + varName + ",1) IN (6,7) ";
        if( equals( FIVEE ) )
            return lastDigit>=8 && lastDigit<=9; //  " right(" + varName + ",1) IN (8,9) ";
        
        return true;        
    }
    
    private int lastDigit(long n) 
    { 
        // return the last digit 
        return (int) (n % 10); 
    } 
    
    public String getSearchCondition( String varName )
    {
        if( equals( ALL ) )
            return "";
        if( equals( ODD ) )
            return " mod(" + varName + ",2)=1 ";
        if( equals( EVEN ) )
            return " mod(" + varName + ",2)=0 ";
        if( equals( THREEA ) )
            return " right(" + varName + ",1) IN (0,1,2) ";
        if( equals( THREEB ) )
            return " right(" + varName + ",1) IN (3,4,5) ";
        if( equals( THREEC ) )
            return " right(" + varName + ",1) IN (6,7,8,9) ";
        if( equals( FOURA ) )
            return " right(" + varName + ",1) IN (0,1) ";
        if( equals( FOURB ) )
            return " right(" + varName + ",1) IN (2,3,4) ";
        if( equals( FOURC ) )
            return " right(" + varName + ",1) IN (5,6,7) ";
        if( equals( FOURD ) )
            return " right(" + varName + ",1) IN (8,9,10) ";
        if( equals( FIVEA ) )
            return " right(" + varName + ",1) IN (0,1) ";
        if( equals( FIVEB ) )
            return " right(" + varName + ",1) IN (2,3) ";
        if( equals( FIVEC ) )
            return " right(" + varName + ",1) IN (4,5) ";
        if( equals( FIVED ) )
            return " right(" + varName + ",1) IN (6,7) ";
        if( equals( FIVEE ) )
            return " right(" + varName + ",1) IN (8,9) ";
        
        return "";
    }
    
    
    public String getName()
    {
        return this.key;
    }

    public int getTestKeyEventSelectionTypeId() {
        return testKeyEventSelectionType;
    }
    
    public static List<SelectItem> getSelectItemList()
    {
        List<SelectItem> out = new ArrayList<>();

        for (TestKeyEventSelectionType val : TestKeyEventSelectionType.values())
        {
            out.add(new SelectItem(new Integer(val.getTestKeyEventSelectionTypeId()), val.key));
        }

        return out;
    }
    
    
    
    public static TestKeyEventSelectionType getValue( int id )
    {
        TestKeyEventSelectionType[] vals = TestKeyEventSelectionType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getTestKeyEventSelectionTypeId() == id )
                return vals[i];
        }

        return ALL;
    }
    

    

}
