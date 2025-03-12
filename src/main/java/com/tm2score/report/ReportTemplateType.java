package com.tm2score.report;



public enum ReportTemplateType
{
    STD_SELECTION(0,"Standard Selection", "com.tm2score.format.StdSelection"),
    STD_INTERVIEW(1,"Standard Interview", "com.tm2score.format.StdInterview"),
    STD_DEVELOPMENT(2,"Standard Development", "com.tm2score.format.StdDevelopment"),
    STD_REFERENCE(100,"Standard Reference", "com.tm2score.format.StdSelection"),
        CUSTOM(200,"Custom", null );

    private final int reportTemplateTypeId;

    private final String name;

    private final String implementationClass;


    private ReportTemplateType( int s , String n, String c )
    {
        this.reportTemplateTypeId = s;

        this.name = n;

        this.implementationClass = c;
    }


    public boolean getIsCustom()
    {
        return equals( CUSTOM );
    }

    public String getImplementationClass()
    {
        return implementationClass;
    }


    public static ReportTemplateType getValue( int id )
    {
        ReportTemplateType[] vals = ReportTemplateType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getReportTemplateTypeId() == id )
                return vals[i];
        }

        return STD_SELECTION;
    }


    public int getReportTemplateTypeId()
    {
        return reportTemplateTypeId;
    }

    public String getName()
    {
        return name;
    }

}
