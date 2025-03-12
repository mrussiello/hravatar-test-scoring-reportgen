package com.tm2score.sim;



public enum CategoryDistType
{
    LINEAR(0,"Linear w/Cliff (R-RY-Y-YG-G-Cliff) or (R-Y-G-Cliff)"),
    NORMAL(1,"Normal (R-Y-G-Y-R) or (R-G-R)" );


    private final int categoryDistTypeId;

    private final String name;



    private CategoryDistType( int s , String n )
    {
        this.categoryDistTypeId = s;

        this.name = n;
    }

    public boolean getLinear()
    {
        return equals( LINEAR);        
    }

    public boolean getIsNormal()
    {
        return equals( NORMAL );        
    }

    
    public static CategoryDistType getValue( int id )
    {
        CategoryDistType[] vals = CategoryDistType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCategoryDistTypeId() == id )
                return vals[i];
        }

        return LINEAR;
    }


    public int getCategoryDistTypeId()
    {
        return categoryDistTypeId;
    }

    public String getName()
    {
        return name;
    }

}
