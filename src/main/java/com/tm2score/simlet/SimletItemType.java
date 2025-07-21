package com.tm2score.simlet;



public enum SimletItemType
{
    NA(0,"NA - Not a Simlet Item" ),
    AUTO_DICHOTOMOUS(1,"(Auto) Dichotomous - Right or Wrong" ),
    AUTO_POINTS(2,"(Auto) Each Choice Has Points" ),
    AUTO_TYPING(6,"(Auto) Typing Speed/Accuracy" ),
    AUTO_DATA_ENTRY(11,"(Auto) Data Entry" ),
    AUTO_ESSAY(7,"(Auto) Scored Essay" ),
    AUTO_AUDIO(8,"(Auto) Scored Audio (via phone)" ),
    AUTO_CHAT(12,"(Auto) Scored Chat" ),
    MANUAL_TEXT(3, "(Manual) Text Choices Saved for Presentation" ),
    MANUAL_AUDIO(4, "(Manual) Voice/Audio Recording (via phone)" ),
    MANUAL_UPLOAD(5, "(Manual) File Upload" ),
    AUTO_AV_UPLOAD(9, "(Auto) Audio/Video File Upload" ),
    IMAGE_CAPTURE(10, "(Auto) Participant Image Capture" ),
    RESUME_CAPTURE(13, "(Manual) Resume Capture" ),
    OTHER_TRACK(99,"Other Trackable Response" );

    private final int simletItemTypeId;

    private final String name;


    private SimletItemType( int s , String n )
    {
        this.simletItemTypeId = s;

        this.name = n;
    }

    public boolean isDichotomous()
    {
        return equals( AUTO_DICHOTOMOUS );
    }

    public boolean isPoints()
    {
        return equals( AUTO_POINTS );
    }

    public boolean isTyping()
    {
        return equals(AUTO_TYPING );
    }

    public boolean isDataEntry()
    {
        return equals(AUTO_DATA_ENTRY );
    }

    public boolean isManualUpload()
    {
        return equals( MANUAL_UPLOAD );
    }

    public boolean isAutoEssay()
    {
        return equals( AUTO_ESSAY );
    }

    public boolean isAutoChat()
    {
        return equals( AUTO_CHAT );
    }

    public boolean isAutoAudio()
    {
        return equals( AUTO_AUDIO );
    }

    public boolean isAutoAvUpload()
    {
        return equals( AUTO_AV_UPLOAD );
    }
    
    public boolean isImageCapture()
    {
        return equals( IMAGE_CAPTURE );
    }


    
    public boolean supportsManualTextTitle()
    {
        return equals( MANUAL_TEXT ) || equals(AUTO_AV_UPLOAD ) || equals( MANUAL_UPLOAD ) || equals( AUTO_AUDIO ) || equals(IMAGE_CAPTURE); 
    }
    
    public boolean isManualText()
    {
        return equals( MANUAL_TEXT );
    }

    /**
     * Auto Data Entry=Raw KSH
     * Auto Typing=Raw WPM
     * Image Capture = For Average confidence Kind of a raw score)
     * @return
     */
    public boolean hasScore2()
    {
        return equals(AUTO_TYPING) || equals(AUTO_DATA_ENTRY) || equals( AUTO_ESSAY ) || equals( AUTO_CHAT ) || equals(AUTO_AV_UPLOAD ) || equals(AUTO_AUDIO) || equals(IMAGE_CAPTURE );
    }

    /**
     * Auto Data Entry=Accuracy Adjusted KSH
     * Auto Typing=Accuracy Adjusted Words Per Minute
     * Image Capture - Number of images compared.
     *
     * @return
     */
    public boolean hasScore3()
    {
        return equals(AUTO_TYPING ) || equals(AUTO_DATA_ENTRY) || equals( AUTO_ESSAY ) || equals( AUTO_CHAT ) || equals(AUTO_AV_UPLOAD ) || equals(AUTO_AUDIO) || equals(IMAGE_CAPTURE );
    }

    /**
     * Auto Data Entry=Accuracy (0-100%)
     * Auto TYPING=Accuracy (0-100%)
     *
     * @return
     */
    public boolean hasScore4()
    {
        return equals(AUTO_TYPING ) || equals(AUTO_DATA_ENTRY) || equals( AUTO_ESSAY ) || equals( AUTO_CHAT ) || equals(AUTO_AV_UPLOAD ) || equals(AUTO_AUDIO);
    }

    /**
     * Auto Data Entry=Gross Errors
     *
     * @return
     */
    public boolean hasScore5()
    {
        return equals( AUTO_ESSAY ) || equals( AUTO_CHAT ) || equals(AUTO_DATA_ENTRY)  || equals(AUTO_AV_UPLOAD ) || equals(AUTO_AUDIO);
    }

    public boolean hasScore6()
    {
        return equals( AUTO_ESSAY ) || equals(AUTO_AV_UPLOAD )|| equals(AUTO_DATA_ENTRY) || equals(AUTO_AUDIO);
    }

    public boolean hasScore7()
    {
        return equals( AUTO_ESSAY ) || equals(AUTO_AV_UPLOAD ) || equals(AUTO_AUDIO)|| equals(AUTO_DATA_ENTRY);
    }

    public boolean hasScore8()
    {
        return equals( AUTO_ESSAY ) || equals(AUTO_AV_UPLOAD ) || equals(AUTO_AUDIO);
    }

    public boolean hasScore9()
    {
        return equals(AUTO_ESSAY);
    }

    public boolean hasScore10()
    {
        return equals(AUTO_ESSAY);
    }

    public boolean hasScore11()
    {
        return equals(AUTO_ESSAY);
    }

    public boolean hasScore12()
    {
        return equals(AUTO_ESSAY) ||  equals(AUTO_AV_UPLOAD );
    }
    public boolean hasScore13()
    {
        return equals(AUTO_ESSAY) ||  equals(AUTO_AV_UPLOAD );
    }
    public boolean hasScore14()
    {
        return equals(AUTO_ESSAY) ||  equals(AUTO_AV_UPLOAD );
    }
    public boolean hasScore15()
    {
        return  equals(AUTO_ESSAY) ||  equals(AUTO_AV_UPLOAD );
    }

    
    /*
    public boolean isManualText()
    {
        return equals( MANUAL_TEXT );
    }
    *
    */




    public boolean supportsManualScoringViaReport()
    {
        return equals( MANUAL_TEXT ) || equals( AUTO_ESSAY ) || equals( AUTO_CHAT ) || equals( AUTO_AUDIO ) || equals(AUTO_AV_UPLOAD ) || equals( MANUAL_UPLOAD )  || equals( RESUME_CAPTURE ) || equals( IMAGE_CAPTURE );
    }



    public static SimletItemType getValue( int id )
    {
        SimletItemType[] vals = SimletItemType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getSimletItemTypeId() == id )
                return vals[i];
        }

        return NA;
    }


    public int getSimletItemTypeId()
    {
        return simletItemTypeId;
    }

    public String getName()
    {
        return name;
    }

}
