package com.tm2score.custom.uminn;



public enum UMinnCompetencyType
{
    

    CONS(1,"Conscientiousness", "Conscientiousness", "cons", 1, new float[]{80f,86f}),
    ASPIR(2,"Aspiring to Excellence", "Aspiring to Excellence", "aspir", 2, new float[]{80f,85f}),
    INTEG(3,"Integrity", "Integrity", "integ", 3, new float[]{82f,87f}),
    ACCOUNT(4,"Accountability", "Accountability", "account", 4, new float[]{81f,86f}),
    TEAMW(5,"Teamwork", "Teamwork", "teamw", 5, new float[]{81f,86f}),
    PCCARE(6,"Patient-Centered Care", "Patient Care", "pccare", 6, new float[]{76f,84f}),
    STRESS(7,"Stress Tolerance", "Stress Tolerance", "stress", 7, new float[]{80f,86f});

    private final int uminnCompetencyTypeId;

    private final String name;
    
    private final String name2;
    
    private final String key;
    
    private final int displayOrder;
    
    /*
      string belowavglow,belowavghigh,avglow,avghigh,aboveavglow,aboveavghigh
    */
    float[] scoreRanges;


    private UMinnCompetencyType( int s , String n, String n2, String  k, int d, float[] scoreRanges )
    {
        this.uminnCompetencyTypeId = s;

        this.name = n;
        this.name2 = n2;
        
        this.key=k;
        
        this.displayOrder = d;
        this.scoreRanges=scoreRanges;
    }

    public static UMinnCompetencyType getValue( int id )
    {
        UMinnCompetencyType[] vals = UMinnCompetencyType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUminnCompetencyTypeId() == id )
                return vals[i];
        }

        return null;
    }
    
    public boolean getIsLow( float score )
    {
        return score>=scoreRanges[1];
    }
    
    public boolean getIsHigh( float score )
    {
        return score<scoreRanges[0];
    }
    
    public String getScoreNameToUse( float score )
    {
        if( score<scoreRanges[0])
            return "Below Average";
        if( score<scoreRanges[1])
            return "Average";
        return "Above Average";
    }

    public String getKey() {
        return key;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }


    public int getUminnCompetencyTypeId()
    {
        return uminnCompetencyTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getName2() {
        return name2;
    }

}
