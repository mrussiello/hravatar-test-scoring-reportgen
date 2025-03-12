package com.tm2score.sim;



public enum ScorePresentationType
{
    DEFAULT(0,"Default Graphical"),
    SPECTRUM(1,"Spectrum - Low and High Values" );


    private final int scorePresentationTypeId;

    private final String name;


    
    

    private ScorePresentationType( int s , String n )
    {
        this.scorePresentationTypeId = s;
        this.name = n;
    }

    public static ScorePresentationType getValue( int id )
    {
        ScorePresentationType[] vals = ScorePresentationType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getScorePresentationTypeId() == id )
                return vals[i];
        }

        return DEFAULT;
    }

    public boolean getIsDefault()
    {
        return equals( DEFAULT);        
    }


    public boolean getIsSpectrum()
    {
        return equals( SPECTRUM );        
    }
    
    

    public int getScorePresentationTypeId()
    {
        return scorePresentationTypeId;
    }

    public String getName()
    {
        return name;
    }

}
