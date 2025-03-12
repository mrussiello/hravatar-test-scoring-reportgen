package com.tm2score.act;

public enum ClickActionType
{
    NOTHING(0,"nothing"  ),
    GOTORESPONSE(1,"gotoresponse" ),
    SHOWCHOICEPANEL(2,"showpanel"),
    EXTERNALLINK(3,"externlink" ),
    DOWNLOAD(4,"download"  ),
    JUMPTOTIME(5,"jumptotime"),
    SHAREPANEL(6,"sharepanel" ),
    JAVASCRIPT(7,"javascript" ),
    SHOWFEEDBACK(8,"feedback" ),
    HIDEINTERACTION(9,"hideinteraction" ),
    PAUSECLIP(10,"pauseclip"  ),
    COMMENTS(11,"commentspanel"  ),
    PREVIOUS(12,"previous"  ),
    NEXT(13,"next"  ),
    FFWD(14,"ffwd"  ),
    PLAY(15,"play" ),
    FULLSCREEN(16,"togfullscreen" ),
    CAPTIONS(17,"togcaptions" ),
    FONTSIZE(18,"togfontsize" ),
    HTTPGET(19,"silenthttp" ),
    SOUND(20,"sound" ),
    PREVIOUSINORDER(21,"previousinorder" ),
    NEXTINORDER(22,"nextinorder" ),
    REPLAYITEM(23,"playreplayitem" ),
    SHOWHIDEITEM(24,"showhideitem" ),
    PLAYSWF(25,"playswf" ),
    REPLAYCLIP(26,"replay" ),
    NEXTCLIPGROUP(27,"nextclipgroup" ),
    PREVCLIPGROUP(28,"prevclipgroup" ),
    LASTORDEREDRESP(29,"lastordrdresp" );

    private final int clickActionTypeId;

    private String key;

    private ClickActionType( int p , String key  )
    {
        this.clickActionTypeId = p;

        this.key = key;
    }


    public boolean getIsClickable()
    {
    	return !equals( NOTHING );
    }

    public boolean getIsGeneratesScorableHistory()
    {
    	return  equals( GOTORESPONSE ) ||
                equals( NEXT ) ||
                equals( PREVIOUS ) ||
                equals( NEXTCLIPGROUP ) ||
                equals( PREVCLIPGROUP ) ||
                equals( LASTORDEREDRESP ) ||
                equals( NEXTINORDER ) ||
                equals( PREVIOUSINORDER );
    }



    public String getKey()
    {
        return key;
    }



    public static ClickActionType getValue( int id )
    {
        ClickActionType[] vals = values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getClickActionTypeId() == id )
                return vals[i];
        }

        return NOTHING;
    }

    public int getClickActionTypeId()
    {
        return clickActionTypeId;
    }

}
