package com.tm2score.bot;


public enum ActionCodeType
{
    SAME_BOT_INSTANCE( 0, "Same Bot Instance" ),
    NEW_BOT_INSTANCE( 1, "Go to New Bot Instance" ),
    SHOW_INTN( 10, "Show Interaction" ),
    HIDE_INTN( 11, "Hide Interaction" ),
    SHOW_INTN_ITEM( 15, "Show Interaction Item" ),
    HIDE_INTN_ITEM( 16, "Hide Interaction Item" ),
    TOGGLE_INTN_ITEM( 17, "Toggle Interaction Item" ),
    SHOW_LOCAL_DIALOG( 18, "Show Local Dialog" ),
    END_SESSION( 999, "End Bot Session" );

    private final int actionCodeTypeId;

    private String key;

    private ActionCodeType( int p , String key )
    {
        this.actionCodeTypeId = p;

        this.key = key;
    }

    
    public boolean getIsShowHideIntn()
    {
        return equals( SHOW_INTN ) || equals( HIDE_INTN );
    }
    
    public boolean getIsShowIntn()
    {
        return equals( SHOW_INTN );
    }
    
    public boolean getIsHideIntn()
    {
        return equals( HIDE_INTN );
    }
    
    public boolean getIsShowHideToggleIntnItem()
    {
        return equals( SHOW_INTN_ITEM ) || equals( HIDE_INTN_ITEM ) || equals( TOGGLE_INTN_ITEM );
    }
    
    public boolean getIsShowIntnItem()
    {
        return equals( SHOW_INTN_ITEM );
    }
    
    public boolean getIsHideIntnItem()
    {
        return equals( HIDE_INTN_ITEM );
    }
    
    public boolean getIsToggleIntnItem()
    {
        return equals( TOGGLE_INTN_ITEM );
    }
    
    public boolean getIsShowLocalDialog()
    {
        return equals( SHOW_LOCAL_DIALOG );
    }
    
    
    public int getActionCodeTypeId()
    {
        return this.actionCodeTypeId;
    }


    public String getName()
    {
        return key;
    }




    public static ActionCodeType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }



    public static ActionCodeType getValue( int id )
    {
        ActionCodeType[] vals = ActionCodeType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getActionCodeTypeId() == id )
                return vals[i];
        }

        return SAME_BOT_INSTANCE;
    }

}
