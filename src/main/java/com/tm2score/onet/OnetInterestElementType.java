package com.tm2score.onet;



public enum OnetInterestElementType
{
    REALISTIC(1,"1.B.1.a", "Realistic"),
    INVESTIGATIVE(2,"1.B.1.b", "Investigative"),
    ARTISTIC(3,"1.B.1.c", "Artistic"),
    SOCIAL(4,"1.B.1.d", "Social"),
    ENTERPRISING(5,"1.B.1.e", "Enterprising"),
    CONVENTIONAL(6,"1.B.1.f", "Conventional");


    private final int onetInterestElementTypeId;

    private final String onetElementId;
    
    private final String name;


    private OnetInterestElementType( int s , String eid, String nm  )
    {
        this.onetInterestElementTypeId = s;

        this.onetElementId=eid;
        this.name = nm;
    }



    public static OnetInterestElementType getValue( int id )
    {
        OnetInterestElementType[] vals = OnetInterestElementType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOnetInterestElementTypeId() == id )
                return vals[i];
        }

        return null;
    }


    public int getOnetInterestElementTypeId()
    {
        return onetInterestElementTypeId;
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
