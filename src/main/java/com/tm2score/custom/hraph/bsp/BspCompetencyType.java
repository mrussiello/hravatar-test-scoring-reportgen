package com.tm2score.custom.hraph.bsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;





public enum BspCompetencyType
{
    

    ANAL(1,"Analytical Thinking", "Analytical Thinking", "anal", 1, true ),
    WRITTEN(2,"Written Communication", "Writing", "writing", 2, true ),
    SVC(3,"Service Orientation", "Resolving Conflicts and Meeting Customer Needs", "svc", 3, true ),
    COOP(4,"Collaboration and Cooperation", "Team Building", "coop", 4, true ),
    ADAP(5,"Adaptability - General", "Maintaining Flexibility and Adaptability", "adapgen", 5, false ),
    DIG_ADAP(6,"Adaptability - Digital", "Digital Adaptability", "digadap", 7, false ),
    OVERALL_ADAP(7,"Adaptability", "Adaptability", "adap", 5, true ),
    DIG_MINDSET(8,"Digital Mindset", "Digital Mindset", "digmind", 6, true );

    private final int bspCompetencyTypeId;

    private final String name;
    
    private final String hraName;
    
    private final String stub;
    
    private final int displayOrder;
    
    private final boolean devReport;


    private BspCompetencyType( int s , String n, String n2, String  k, int d, boolean dev )
    {
        this.bspCompetencyTypeId = s;

        this.name = n;
        this.hraName = n2;
        
        this.stub=k;
        
        this.displayOrder = d;
        
        this.devReport = dev;
    }

    
    public static List<BspCompetencyType> getListDevRpt()
    {
        List<BspCompetencyType> out = new ArrayList<>();
        
        for( BspCompetencyType b : BspCompetencyType.values() )
        {
            if( !b.devReport )
                continue;
            
            out.add(b);
        }
        
        Collections.sort( out, new BspCompetencyTypeDOComparator() );
        
        return out;
    }

    
    public boolean includeDevSuggestions()
    {
        return true; //  !equals(WRITTEN);
    }
    
    /*
    public static List<String> getNameList()
    {
        List<String> out = new ArrayList<>();
        
        for( BspCompetencyType b : BspCompetencyType.values() )
        {
            out.add(b.getName());
        }
        
        return out;
    }
    */


    public static BspCompetencyType getValue( int id )
    {
        BspCompetencyType[] vals = BspCompetencyType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getBspCompetencyTypeId() == id )
                return vals[i];
        }

        return null;
    }

    public String getStub() {
        return stub;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }


    public int getBspCompetencyTypeId()
    {
        return bspCompetencyTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getHraName() {
        return hraName;
    }

}
