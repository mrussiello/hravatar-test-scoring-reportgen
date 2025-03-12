package com.tm2score.sim;



public enum SimCompetencyVisibilityType
{
    SHOW_IF_HAVE_RESPONSES(0,"Score and Show in reports if item responses present (default)"),
    HIDE_IN_REPORTS(1,"Score but Hide in Reports"),
    SHOW_IN_ALL_REPORTS(2,"Score and Show in reports even if item responses NOT present"),
    HIDE_IN_REPORTS_INCLUDEEXCEL(3,"Score and Show only in Excel Download"),
    HIDE_IN_REPORTS_INCLUDEITEMSRESPONSES(4,"Score but Hide in Reports Except Item Responses");

    private final int simCompetencyVisibilityTypeId;

    private final String name;


    private SimCompetencyVisibilityType( int s , String n )
    {
        this.simCompetencyVisibilityTypeId = s;

        this.name = n;
    }

    
    public boolean getShowInReports()
    {
        return equals(SHOW_IF_HAVE_RESPONSES ) || equals(SHOW_IN_ALL_REPORTS);
    }

    public boolean getShowItemScoresInReports()
    {
        return getShowInReports() || equals(HIDE_IN_REPORTS_INCLUDEITEMSRESPONSES);
    }
    
    
    public static SimCompetencyVisibilityType getValue( int id )
    {
        SimCompetencyVisibilityType[] vals = SimCompetencyVisibilityType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimCompetencyVisibilityTypeId() == id )
                return vals[i];
        }

        return SHOW_IF_HAVE_RESPONSES;
    }


    public int getSimCompetencyVisibilityTypeId()
    {
        return simCompetencyVisibilityTypeId;
    }

    public String getName()
    {
        return name;
    }

}
