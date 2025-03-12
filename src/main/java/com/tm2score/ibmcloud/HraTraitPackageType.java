package com.tm2score.ibmcloud;

import java.util.ArrayList;
import java.util.List;

public enum HraTraitPackageType
{
    /*
     For Cultural Fit, just use CPI.
    
     For Performance, use 
    */
    
    
    NONE(0, "None" ), 
    CULTURE(1, "Cultural Fit" ), 
    PERFORMANCE(2,"Performance" ), 
    BOTH(10,"Both" );
    
    private final String name;
    private final int hraPackageTypeId;


    private HraTraitPackageType( int p, String name )
    {
        this.hraPackageTypeId=p;
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public int getHraPackageTypeId() {
        return hraPackageTypeId;
    }
    
    public List<HraTraitType> getHraTraitTypeList()
    {
        List<HraTraitType> out = new ArrayList<>();
        
        for( HraTraitType tt : HraTraitType.values() )
        {
            if( tt.getIsCulture() && getIncludeCulture() )
                out.add(tt);
            
            else if(tt.getIsPerformance() && getIncludePerformance() )
                out.add(tt);
                
        }
        
        return out;
                    
    }
    
    public boolean getIncludeCulture()
    {
        return equals(CULTURE) || equals(BOTH);
    }

    public boolean getIncludePerformance()
    {
        return equals(PERFORMANCE) || equals(BOTH);
    }

    
    public static HraTraitPackageType getValue( int id )
    {
        HraTraitPackageType[] vals = HraTraitPackageType.values();

        for( int i = 0; i < vals.length; i++ )
        {
            if( vals[i].getHraPackageTypeId() == id )
                return vals[i];
        }

        return NONE;
    }
      
}
