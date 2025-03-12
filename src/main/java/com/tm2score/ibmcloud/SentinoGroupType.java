package com.tm2score.ibmcloud;

public enum SentinoGroupType
{
    BIG5(1, "BIG5", "big5" ),
    HEXACO(2, "Hexaco", "hexaco" ),
    CPI(3, "CPI", "cpi" ),
    SIXFACTOR(4, "6 Factor Personality", "6fpq" ),
    BISBAS(5, "Behavioral Inhibition/Activation Systems", "bisbas" ),
    AB5C(6, "Abridged Big Five Dimensional Circumplex facets", "ab5c" ),
    VIA(7, "Values in Action Character Survey", "via" ),
    MPQ(8, "Multidimensional Personality Questionnaire", "mpq" ),
    NEO(9, "neo", "neo" );

    private final int sentinoGroupTypeId;
    private final String sentinoKey;
    private String name;


    private SentinoGroupType( int p,
                         String n,
                         String sentinoKey )
    {
        this.sentinoGroupTypeId = p;
        this.name = n;
        this.sentinoKey=sentinoKey;
    }
        
    
   
    public String getName()
    {
        return name;
    }

    public int getSentinoGroupTypeId() {
        return sentinoGroupTypeId;
    }
    
    public static SentinoGroupType getForSentinoKey( String key )
    {
        SentinoGroupType[] vals = SentinoGroupType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getSentinoKey().equals(key) )
                return vals[i];
        }

        return null;
    }

    public String getSentinoKey() {
        return sentinoKey;
    }
      
    
    
    public static SentinoGroupType getValue( int id )
    {
        SentinoGroupType[] vals = SentinoGroupType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getSentinoGroupTypeId() == id )
                return vals[i];
        }

        return BIG5;
    }
      
      
}
