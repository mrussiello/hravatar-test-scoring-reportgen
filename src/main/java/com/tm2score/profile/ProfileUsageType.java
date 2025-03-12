package com.tm2score.profile;

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.model.SelectItem;


public enum ProfileUsageType
{
    // Show preferred ranges in PDF report
    REPORT_RANGES(0, "Report Ranges (Default) - blue line, colors, competency weights", true ),
        
    // Calculate Alt Overall - not used in main score, only to produce alt scores.
    // Str1 = name of alt score
    ALTERNATE_OVERALL_COMPETENCY_WEIGHTS(2, "Alternate Overall Score Competency Weights", true),
    
    // Calculate Alt Battery Score using Overall Score Weights - not unsed in main battery score, 
    // only to produce alternate battery scores
    // Str1 = name of alt score
    // Each Entry = Name of Product
    ALTERNATE_BATTERY_OVERALL_WEIGHTS(3, "Alternate Battery Overall Weights", true ),
    
    // Calculate Alt Battery Score using Individual competency Weights Across All Tests - not unsed in main battery score, 
    // only to produce alternate battery scores
    // Str1 = name of alt score
    ALTERNATE_BATTERY_COMPETENCY_WEIGHTS(4, "Alternate Battery Competency Weights", true ),

    CT3_ALTERNATE_OVERALL_CALC(5, "CT3 Overall Score Calculation", false),

    // Used to create a candidate report. Typically used to generate best jobs matches against different jobs based on Interests Only.
    CT3_INTERESTS_MATCH_CALC(6, "CT3 Job Interest Match", false);
    
    
    private final int profieUsageTypeId;

    private final String name;
    
    private final boolean usesProfileEntries;
    
    

    private ProfileUsageType( int typeId, String k, boolean ndsProfEntries )
    {
        profieUsageTypeId = typeId;
        name = k;
        this.usesProfileEntries = ndsProfEntries;
    }


    public boolean getIsReportRanges()
    {
        return equals( REPORT_RANGES );
    }
    
    public int getProfileUsageTypeId()
    {
        return profieUsageTypeId;
    }

    public static ProfileUsageType getValue( int id )
    {
        for( ProfileUsageType val : ProfileUsageType.values() )
        {
            if( id == val.getProfileUsageTypeId())
                return val;
        }

        return REPORT_RANGES;
    }

    public boolean getUsesProfileEntries() {
        return usesProfileEntries;
    }

    
    

    public static List<SelectItem> getSelectItemList()
    {
        List<SelectItem> outMap = new ArrayList<>();

        ProfileUsageType[] vals = ProfileUsageType.values();

       for( int i=0 ; i<vals.length ; i++ )
        {
            outMap.add(new SelectItem( vals[i].getProfileUsageTypeId(), vals[i].name )  );
        }

        return outMap;
    }



    public String getName()
    {
        return name;
    }
}
