package com.tm2score.onet;



public enum OnetJobZoneType
{
    ZONE1(201,1,"Job Zone 1","onetjobzone1"),
    ZONE2(202,2,"Job Zone 2","onetjobzone2"),
    ZONE3(203,3,"Job Zone 3","onetjobzone3"),
    ZONE4(204,4,"Job Zone 4","onetjobzone4"),
    ZONE5(205,5,"Job Zone 5","onetjobzone5");

    private final int onetJobZoneTypeId;
    private final int jobZoneId;

    private final String stub;
    
    private final String name;


    private OnetJobZoneType( int s , int jzid, String nm, String stb  )
    {
        this.onetJobZoneTypeId = s;
        this.jobZoneId = jzid;
        this.stub = stb;
        this.name = nm;
    }



    public boolean isWithinLimits( OnetJobZoneType minZone, OnetJobZoneType maxZone, int gapLimit )
    {
        int minZoneId = minZone==null ? 1 : minZone.getJobZoneId();
        int maxZoneId = maxZone==null ? 1 : maxZone.getJobZoneId();
        
        minZoneId = Math.max(1, minZoneId-gapLimit);
        maxZoneId = Math.min(5, maxZoneId+gapLimit);
        //maxZoneId += gapLimit;
        
        return jobZoneId >= minZoneId && jobZoneId <= maxZoneId;
    }
    
    public static OnetJobZoneType getValue( int id )
    {
        OnetJobZoneType[] vals = OnetJobZoneType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOnetJobZoneTypeId() == id )
                return vals[i];
        }

        return null;
    }
    
    public static OnetJobZoneType getValueForZoneId( int id )
    {
        OnetJobZoneType[] vals = OnetJobZoneType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getJobZoneId() == id )
                return vals[i];
        }

        return null;
    }
    
    
    public static float getGapCostLow()
    {
        return 33f;
    }

    public static float getGapCostHigh()
    {
        return 15f;
    }


    public String getNameKey()
    {
        return stub + ".name";
    }

    public String getNamexKey()
    {
        return stub + ".namex";
    }
    
    public String getExperienceKey()
    {
        return stub + ".exp";
    }

    public String getDescripKey()
    {
        return stub + ".descrip";
    }
    
    
    public String getEducationKey()
    {
        return stub + ".educ";
    }

    public String getTrainingKey()
    {
        return stub + ".training";
    }
    
    public String getShortDescKey()
    {
        return stub + ".shortdesc";
    }
    
    
    public int getOnetJobZoneTypeId()
    {
        return onetJobZoneTypeId;
    }

    
    
    public String getName()
    {
        return name;
    }

    public int getJobZoneId() {
        return jobZoneId;
    }

    public String getStub() {
        return stub;
    }
    
}
