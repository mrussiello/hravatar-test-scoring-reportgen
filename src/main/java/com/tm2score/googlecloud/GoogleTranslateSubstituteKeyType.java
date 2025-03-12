package com.tm2score.googlecloud;



    /*
    
ull Name:	[FLNM]   111	
First Name:	[FNM]    112
Last Name:	[LNM]    113
Email:	[EM]             114
Company:	[CMPNY]  115
Custom 1:	[CUST1]  116
Custom 2:	[CUST2]  117
Custom 3:	[CUST3]  118
Custom 4:	[CUST4]  119
Cross Ref:	[REF]    120
Unique Event ID:	[EVT] 121
Clip Order Number:	[CLIPORDER]
Most recent non-zero Clip Order Number:	[LASTCLIPORDER]
Sim Section Name:	[SECTIONNAME]	Simlets Only
Sim Section Number:	[SECTIONNUMBER]	Simlets Only
Previous Section:	[PREVSECTIONNUMBER]	Simlets Only
Total Sections:	[TOTALSECTIONS]	Simlets Only
Remaining Sections:	[REMAININGSECTIONS]	Simlets Only
Previous Remaining Sections:	[PREVREMAININGSECTIONS]	Simlets Only
Sim Swap String X (1-10):	[SWAPSTRINGX]    
    
    */

public enum GoogleTranslateSubstituteKeyType
{
    FLNM(111,"[FLNM]",null),
    FNM(112,"[FNM]",null),
    LNM(113,"[LNM]",null),
    EM(114,"[EM]",null),
    CMPNY(115,"[CMPNY]",null),
    CUST1(116,"[CUST1]",null),
    CUST2(117,"[CUST2]",null),
    CUST3(118,"[CUST3]",null),
    CUST4(119,"[CUST4]",null),
    EVT(120,"[EVT]",null),
    CLIPORDER(121,"[CLIPORDER]",null),
    LASTCLIPORDER(122,"[LASTCLIPORDER]",null),
    SECTIONNAME(123,"[SECTIONNAME]",null),
    SECTIONNUMBER(124,"[SECTIONNUMBER]",null),
    PREVSECTIONNUMBER(125,"[PREVSECTIONNUMBER]",null),
    TOTALSECTIONS(126,"[TOTALSECTIONS]",null),
    REMAININGSECTIONS(127,"[REMAININGSECTIONS]",null),
    PREVREMAININGSECTIONS(128,"[PREVREMAININGSECTIONS]",null),
    SWAPSTRING1(129,"[SWAPSTRING1]",null),
    SWAPSTRING2(130,"[SWAPSTRING2]",null),
    SWAPSTRING3(131,"[SWAPSTRING3]",null),
    SWAPSTRING4(132,"[SWAPSTRING4]",null),
    SWAPSTRING5(133,"[SWAPSTRING5]",null),
    SWAPSTRING6(134,"[SWAPSTRING6]",null),
    SWAPSTRING7(135,"[SWAPSTRING7]",null),
    SWAPSTRING8(136,"[SWAPSTRING8]",null),
    SWAPSTRING9(137,"[SWAPSTRING9]",null),
    SWAPSTRING10(138,"[SWAPSTRING10]",null),
    STEM1(139,"[STEM1]",null),
    STEM2(140,"[STEM2]",null),
    STEM3(141,"[STEM3]",null),
    STEM4(142,"[STEM4]",null),
    STEM5(143,"[STEM5]",null),
    STEM6(144,"[STEM6]",null),
    STEM7(145,"[STEM7]",null),
    STEM8(146,"[STEM8]",null),
    STEM9(147,"[STEM9]",null),
    STEM10(148,"[STEM10]",null),
    QUESTION(149,"[QUESTION]",null),
    SCREENQ(150,"[SCREENQ]",null),
    IVRQ(151,"[IVRQ]",null),
    AUDIO(152,"[AUDIO]",null),
    VIDEO(153,"[VIDEO]",null),
    
    COMPANY(200,"[COMPANY]",null),
    DEPARTMENT(201,"[DEPARTMENT]",null),
    APPLICANT(202,"[APPLICANT]",null),
    CANDIDATE(203,"[CANDIDATE]",null),
    EMPLOYEE(204,"[EMPLOYEE]",null),
    TEST(205,"[TEST]",null),
    TESTKEY(206,"[TESTKEY]",null),
    EXPIRE(207,"[EXPIRE]",null),
    URL(208,"[URL]",null);

    private final int googleTranslateSubstituteKeyTypeId;

    private final String key;
    
    private final String substituteKey;


    private GoogleTranslateSubstituteKeyType( int p , String key, String subKey )
    {
        this.googleTranslateSubstituteKeyTypeId = p;

        this.key = key;
        
        this.substituteKey = subKey;
    }

    public int getGoogleTranslateSubstituteKeyTypeId() {
        return googleTranslateSubstituteKeyTypeId;
    }



    public static GoogleTranslateSubstituteKeyType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }
    
    public String getSubstituteKey()
    {
        if( substituteKey!=null && !substituteKey.isEmpty() )
            return substituteKey;
        
        return "[999" + googleTranslateSubstituteKeyTypeId + "]";
    }

    public static GoogleTranslateSubstituteKeyType getValue( int id )
    {
        GoogleTranslateSubstituteKeyType[] vals = GoogleTranslateSubstituteKeyType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getGoogleTranslateSubstituteKeyTypeId() == id )
                return vals[i];
        }

        return FLNM;
    }

}
