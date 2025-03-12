package com.tm2score.sim;



public enum NonCompetencyItemType
{
    NONE(0,"None", "NONE", null),
    INTEREST_TASK1(1,"Interest - Simlet Task 1", "INTEREST", "g.Interest" ),
    INTEREST_TASK2(2,"Interest - Simlet Task 2", "INTEREST", "g.Interest"),
    EXPERIENCE_TASK1(3,"Experience - Simlet Task 1", "EXPERIENCE", "g.Experience"),
    EXPERIENCE_TASK2(4,"Experience - Simlet Task 2", "EXPERIENCE", "g.Experience"),
    MIN_QUALS(5,"Min Quals", "MINQUALS", "g.MinQuals"),
    APPLICANT_INFO(6,"Applicant Info", "APPLICANTINFO", "g.ApplicantData" ),
    INTEREST_GENERAL(7,"Interest - General","GENINTEREST","g.InterestGeneral"),
    EXPERIENCE_GENERAL(8,"Experience - General","GENEXPERIENCE","g.ExperienceGeneral"),
    WRITING_SAMPLE(9,"Writing Sample","WRITINGSAMPLE","g.WritingSamples" ),
    SPEAKING_SAMPLE(10,"Speaking Sample","SPEAKINGSAMPLE","g.SpeakingSamples" ),
    FILEUPLOAD(11,"General Uploaded File","UPLOADEDFILE","g.UploadedFiles"),
    AV_UPLOAD(12,"Uploaded Audio/Video File","UPLOADEDAV","g.UploadedAVFiles" );


    private final int nonCompetencyItemTypeId;

    private final String name;

    private final String title;

    private final String langKey;


    private NonCompetencyItemType( int s , String n, String t, String l )
    {
        this.nonCompetencyItemTypeId = s;

        this.name = n;

        this.title = t;

        this.langKey = l;
    }

    public String getTitle() {
        return title;
    }


    public String getLangKeyForReport( boolean continued )
    {
        return continued ? langKey + "Contd" : langKey;
    }

    public static NonCompetencyItemType getValue( int id )
    {
        NonCompetencyItemType[] vals = NonCompetencyItemType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getNonCompetencyItemTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getNonCompetencyItemTypeId()
    {
        return nonCompetencyItemTypeId;
    }


    public String getName()
    {
        return name;
    }

}
