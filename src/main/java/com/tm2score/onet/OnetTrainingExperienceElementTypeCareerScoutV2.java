package com.tm2score.onet;



public enum OnetTrainingExperienceElementTypeCareerScoutV2
{
    EXPERIENCE_AND_TRAINING_CAT(201,"Experience And Training", 2, 0.05f, 56f ),
    EDUCATION_CAT(202,"Education", 50, 0.2f, 44f );

    private final int onetTrainingExperienceElementTypeId;

    private final String name;
    
    private final float gapCostLow;
    private final float gapCostHigh;
    private final float calcWeight;
            
            


    private OnetTrainingExperienceElementTypeCareerScoutV2( int s, String nm, float gapCostLow, float gapCostHigh, float calcWeight )
    {
        this.onetTrainingExperienceElementTypeId = s;

        this.name = nm;
        this.gapCostLow = gapCostLow;
        this.gapCostHigh = gapCostHigh;
        this.calcWeight = calcWeight;
    }



    public static OnetTrainingExperienceElementTypeCareerScoutV2 getValue( int id )
    {
        OnetTrainingExperienceElementTypeCareerScoutV2[] vals = OnetTrainingExperienceElementTypeCareerScoutV2.values();

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
    
    
    public String getName()
    {
        return name;
    }

}
