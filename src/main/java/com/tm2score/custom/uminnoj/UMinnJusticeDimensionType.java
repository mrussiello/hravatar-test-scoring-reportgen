package com.tm2score.custom.uminnoj;

import com.tm2score.custom.uminn.*;



public enum UMinnJusticeDimensionType
{
    

    INTERPERSONAL(1,"Interpersonal Justice", new int[]{1,2,3,4}),
    INFORMATIONAL(2,"Informational Justice", new int[]{5,6,7,8}),
    PROCEDURAL(3,"Procedural Justice", new int[]{9,10,11,12}),
    DISTRIBUTIVE(4,"Distributive Justice", new int[]{13,14,15,16});

    private final int uminnJusticeDimensionTypeId;
    private final String name;    
    private final int[] itemNumbers;
        

    private UMinnJusticeDimensionType( int s , String n, int[] itemNumbers )
    {
        this.uminnJusticeDimensionTypeId = s;
        this.name = n;
        this.itemNumbers = itemNumbers;
    }

    
    public static UMinnJusticeDimensionType getValueForItemNumber( int itmNumber )
    {
        UMinnJusticeDimensionType[] vals = UMinnJusticeDimensionType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].includesItemNumber( itmNumber ) )
                return vals[i];
        }
        
        return null;
    }
    
    public boolean includesItemNumber( int itmNumber )
    {
        for( int ii : itemNumbers )
        {
            if( itmNumber==ii )
                return true;
        }
        return false;
    }
    
    public static UMinnJusticeDimensionType getValue( int id )
    {
        UMinnJusticeDimensionType[] vals = UMinnJusticeDimensionType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUminnJusticeDimensionTypeId() == id )
                return vals[i];
        }

        return null;
    }

    public int getUminnJusticeDimensionTypeId() {
        return uminnJusticeDimensionTypeId;
    }

    public String getName() {
        return name;
    }

    public int[] getItemNumbers() {
        return itemNumbers;
    }
    

}
