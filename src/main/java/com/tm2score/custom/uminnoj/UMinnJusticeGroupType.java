package com.tm2score.custom.uminnoj;

import com.tm2score.score.simcompetency.SimCompetencyScore;



public enum UMinnJusticeGroupType
{
   
    CLINICAL_SUPERVISORS(1,"Clinical Supervisors", "OJCS", new int[]{1,2,3,4}),
    PROGRAM_LEADERS(2,"Program Leaders", "OJPL", new int[]{1,2,3,4}),
    INTERPROF(3,"Interprofessional Team Members", "OJIPTM", new int[]{1,2,3}),
    OPERATIONS(4,"Operations Staff", "OJOS", new int[]{1}),
    CONSULTANTS(5,"Consultants", "OJCON", new int[]{1,2,3}),
    COLLEAGUES(6,"Colleagues", "OJCOL", new int[]{1,2,3,4}),
    PATIENTS(7,"Patients and Families", "OJPAT", new int[]{1});

    private final int uminnJusticeGroupTypeId;
    private final String name;    
    private final String idStub;
    private final int[] justiceTypeIds;
        

    private UMinnJusticeGroupType( int s , String n, String idStub, int[] justiceTypeIds )
    {
        this.uminnJusticeGroupTypeId = s;
        this.name = n;
        this.idStub = idStub;
        this.justiceTypeIds=justiceTypeIds;
    }

    public static UMinnJusticeGroupType getValue( int id )
    {
        UMinnJusticeGroupType[] vals = UMinnJusticeGroupType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUminnJusticeGroupTypeId() == id )
                return vals[i];
        }

        return null;
    }
    
    public static UMinnJusticeGroupType getValueForGroupSimCompetency(SimCompetencyScore scs)
    {
        String name = scs.getName();
        
        for( UMinnJusticeGroupType gt : UMinnJusticeGroupType.values() )
        {
            if( name.equalsIgnoreCase( gt.getName() ) )
                return gt;
        }
        return null;
    }

    public int getUminnJusticeGroupTypeId() {
        return uminnJusticeGroupTypeId;
    }

    public String getName() {
        return name;
    }

    public String getIdStub() {
        return idStub;
    }

    public int[] getJusticeTypeIds() {
        return justiceTypeIds;
    }

    

}
