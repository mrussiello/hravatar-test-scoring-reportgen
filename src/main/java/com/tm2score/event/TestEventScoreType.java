package com.tm2score.event;


public enum TestEventScoreType
{
    OVERALL(0,"Overall"),
    COMPETENCY(1,"Competency"),
    TASK(2,"Task"),
    REPORT(3,"Report"),
    KNOWLEDGE(4,"Knowledge"),
    SKILLS(5,"Skills"),
    ABILITIES(6,"Abilities"),
    LEVEL_SCORES(7,"Level Scores"),
    COMPETENCYGROUP(8,"Competency Group"),
    ALT_OVERALL( 9 ,"Alt Overall Score" );

    private final int testEventScoreTypeId;

    private String key;


    private TestEventScoreType( int p , String key )
    {
        this.testEventScoreTypeId = p;

        this.key = key;
    }

    public boolean getIsOverall()
    {
        return equals( OVERALL );
    }

    public boolean getIsReport()
    {
        return equals( REPORT );
    }

    public boolean getIsCompetency()
    {
        return equals( COMPETENCY );
    }
    
    public boolean getIsLevelScores()
    {
        return equals( LEVEL_SCORES );
    }

    public boolean getIsAltOverallScores()
    {
        return equals( ALT_OVERALL );
    }

    
    /*
    public TestEventScoreType getForSimCompetencyClass( SimCompetencyClass simCompetencyClass )
    {
        if( simCompetencyClass == null )
            return null;

        if( simCompetencyClass.equals( SimCompetencyClass.NONCOGNITIVE ) )
            return COMPETENCY;
        if( simCompetencyClass.equals( SimCompetencyClass.EQ ) )
            return COMPETENCY;
        if( simCompetencyClass.equals( SimCompetencyClass.ABILITY ) || simCompetencyClass.equals( SimCompetencyClass.ABILITY_COMBO ) )
            return COMPETENCY;
        if( simCompetencyClass.equals( SimCompetencyClass.CORESKILL )|| simCompetencyClass.equals( SimCompetencyClass.SKILL_COMBO ) )
            return COMPETENCY;
        if( simCompetencyClass.equals( SimCompetencyClass.KNOWLEDGE ) )
            return COMPETENCY;
        if( simCompetencyClass.equals( SimCompetencyClass.AGGREGATEABILITY ) )
            return ABILITIES;
        if( simCompetencyClass.equals( SimCompetencyClass.AGGREGATEKNOWLEDGE ) )
            return KNOWLEDGE;
        if( simCompetencyClass.equals( SimCompetencyClass.AGGREGATESKILL ) )
            return SKILLS;

        return null;
    }
    */



    public int getTestEventScoreTypeId()
    {
        return this.testEventScoreTypeId;
    }


    public String getKey()
    {
        return key;
    }


    public static TestEventScoreType getValue( int id )
    {
        TestEventScoreType[] vals = TestEventScoreType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getTestEventScoreTypeId() == id )
                return vals[i];
        }

        return OVERALL;
    }

}
