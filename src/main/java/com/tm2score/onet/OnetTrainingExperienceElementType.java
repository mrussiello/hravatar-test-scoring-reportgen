package com.tm2score.onet;



public enum OnetTrainingExperienceElementType
{
    EXPERIENCE_CAT(101,"3.A.1","Experience", 50, 5, 44 ),
    EDUCATION_CAT(102,"2.D.1","Education", 50, 20, 44 ),
    TRAINING_CAT(103,"3.A.2 or 3,A.3","Training", 20, 5, 12 );

    private final int onetTrainingExperienceElementTypeId;

    private final String onetElementId;
    
    private final String name;
    
    private final float gapCostLow;
    private final float gapCostHigh;
    private final float calcWeight;
            
            


    private OnetTrainingExperienceElementType( int s , String eid, String nm, float gapCostLow, float gapCostHigh, float calcWeight )
    {
        this.onetTrainingExperienceElementTypeId = s;

        this.onetElementId=eid;
        this.name = nm;
        this.gapCostLow = gapCostLow;
        this.gapCostHigh = gapCostHigh;
        this.calcWeight = calcWeight;
    }



    public static OnetTrainingExperienceElementType getValue( int id )
    {
        OnetTrainingExperienceElementType[] vals = OnetTrainingExperienceElementType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOnetTrainingExperienceElementTypeId() == id )
                return vals[i];
        }

        return null;
    }

    public float getGapCostLow() {
        return gapCostLow;
    }

    public float getGapCostHigh() {
        return gapCostHigh;
    }

    public float getCalcWeight() {
        return calcWeight;
    }

    

    public int getOnetTrainingExperienceElementTypeId()
    {
        return onetTrainingExperienceElementTypeId;
    }

    public String getOnetElementId()
    {
        return onetElementId;
    }

    
    
    public String getName()
    {
        return name;
    }

}
