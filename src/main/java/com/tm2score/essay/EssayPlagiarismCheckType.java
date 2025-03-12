package com.tm2score.essay;



public enum EssayPlagiarismCheckType
{
    DEFAULT(0,"Default", 0),
    WEB(1,"Default Local plus Web Check", 0),
    LOCAL_1000(11,"1000 Local Only", 1000),
    LOCAL_1000_WEB(21,"1000 Local plus Web Check", 1000),
    LOCAL_2000(12,"2000 Local Only", 2000),
    LOCAL_2000_WEB(22,"2000 Local plus Web Check", 2000),
    LOCAL_4000(14,"4000 Local Only", 4000),
    LOCAL_4000_WEB(24,"4000 Local plus Web Check", 4000),
    LOCAL_6000(16,"6000 Local Only", 6000),
    LOCAL_6000_WEB(26,"6000 Local plus Web Check", 6000),
    LOCAL_8000(18,"8000 Local Only", 8000),
    LOCAL_8000_WEB(28,"8000 Local plus Web Check", 8000);


    private final int essayPlagiarismCheckTypeId;

    private final String name;
    private final int maxRows;


    private EssayPlagiarismCheckType( int s , String n, int mr )
    {
        this.essayPlagiarismCheckTypeId = s;
        this.name = n;
        this.maxRows = mr;
    }

    public int getMaxRowsToCheck()
    {
        return maxRows;
    }


    public static EssayPlagiarismCheckType getValue( int id )
    {
        EssayPlagiarismCheckType[] vals = EssayPlagiarismCheckType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getEssayPlagiarismCheckTypeId() == id )
                return vals[i];
        }

        return DEFAULT;
    }


    public int getEssayPlagiarismCheckTypeId()
    {
        return essayPlagiarismCheckTypeId;
    }

    public String getName()
    {
        return name;
    }

}
