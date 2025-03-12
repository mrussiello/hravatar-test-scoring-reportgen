package com.tm2score.user;



public enum CandidateAudioVideoViewType
{
    LOGON_REQUIRED(0,"cavvt.logonrequired") , 
    ALL_OK(10,"cavvt.all");


    private final int candidateAudioVideoViewTypeId;

    private String key;


    private CandidateAudioVideoViewType( int level , String key )
    {
        this.candidateAudioVideoViewTypeId = level;

        this.key = key;
    }



    public boolean getLogonRequired()
    {
        return candidateAudioVideoViewTypeId != ALL_OK.getCandidateAudioVideoViewTypeId();
    }
    

    public int getCandidateAudioVideoViewTypeId()
    {
        return this.candidateAudioVideoViewTypeId;
    }



    public static CandidateAudioVideoViewType getValue( int id )
    {
        CandidateAudioVideoViewType[] vals = CandidateAudioVideoViewType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCandidateAudioVideoViewTypeId() == id )
                return vals[i];
        }

        return LOGON_REQUIRED;
    }


    public String getKey()
    {
        return key;
    }

}
