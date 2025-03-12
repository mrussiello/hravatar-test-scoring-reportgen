package com.tm2score.av;

import com.tm2builder.sim.xml.SimJ;
import com.tm2score.util.UrlEncodingUtils;
import java.net.URLDecoder;

public enum AvIntnElementType
{
    SCREENQ(1, "SCREENQ" ),
    IVRQ(2, "IVRQ" ),
    STEM1(11, "STEM1" ),
    STEM2(12, "STEM2" ),
    STEM3(13, "STEM3" ),
    STEM4(14, "STEM4" ),
    STEM5(15, "STEM5" ),
    STEM6(16, "STEM6" ),
    STEM7(17, "STEM7" ),
    STEM8(18, "STEM8" ),
    SCREENPRELUDE(101,"SCREENPRELUDE"),
    IVRPRELUDE(102,"IVRPRELUDE"); 

    private final int avIntnElementTypeId;

    private String key;


    private AvIntnElementType( int p,
                         String key )
    {
        this.avIntnElementTypeId = p;
        this.key = key;
    }

    
    public String getJsonKey()
    {
        return key.toLowerCase();
    }

    public String getSeqJsonKey()
    {
        return key.toLowerCase() + "seq";
    }
    
    
    public String getKey()
    {
        return this.key;
    }

    public int getAvIntnElementTypeId() {
        return avIntnElementTypeId;
    }
    
    public static AvIntnElementType getValue( int id )
    {
        AvIntnElementType[] vals = AvIntnElementType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getAvIntnElementTypeId() == id )
                return vals[i];
        }

        return null;
    }

    public static AvIntnElementType getValue( String inKey )
    {
        if( inKey==null || inKey.isEmpty() )
            return null;
        
        AvIntnElementType[] vals = AvIntnElementType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].key.equals(inKey) )
                return vals[i];
        }

        return null;
    }

    
    public String getIntnStringElement( SimJ.Intn intn ) throws Exception
    {
        
        for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
        {
            if( ii.getContent()==null || ii.getContent().isEmpty() )
                continue;
            
            if( ii.getContent().startsWith( "%5B" + getKey() + "%5D" ) )
            {
                String t = UrlEncodingUtils.decodeKeepPlus( ii.getContent(), "UTF8" );
                        
                String compx = "[" + getKey() + "]";
                return t.substring(compx.length(), t.length()) ;
            }            
        }
        
        return "";
    }
    
    
    public SimJ.Intn.Intnitem getIntnItemElement( SimJ.Intn intn ) throws Exception
    {
        for( SimJ.Intn.Intnitem ii : intn.getIntnitem() )
        {
            if( ii.getContent()==null || ii.getContent().isEmpty() )
                continue;
            
            if( ii.getContent().startsWith( "%5B" + getKey() + "%5D" ) )
                return ii;            
        }
        
        return null;
    }
    
    
    

}
