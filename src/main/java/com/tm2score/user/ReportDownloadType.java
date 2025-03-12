package com.tm2score.user;



public enum ReportDownloadType
{
    ALL_OK(0,"rdldt.all") , 
    LOGON_REQUIRED(1,"rdldt.logonrequired");


    private final int reportDownloadTypeId;

    private String key;


    private ReportDownloadType( int level , String key )
    {
        this.reportDownloadTypeId = level;

        this.key = key;
    }



    public boolean getLogonRequired()
    {
        return reportDownloadTypeId != ALL_OK.getReportDownloadTypeId();
    }
    

    public int getReportDownloadTypeId()
    {
        return this.reportDownloadTypeId;
    }

    public static ReportDownloadType getValue( int id )
    {
        ReportDownloadType[] vals = ReportDownloadType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getReportDownloadTypeId() == id )
                return vals[i];
        }

        return LOGON_REQUIRED;
    }

    public String getKey()
    {
        return key;
    }

}
