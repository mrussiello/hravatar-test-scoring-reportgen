/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.bot;

/**
 *
 * @author miker_000
 */
public enum ChatMessageType {
   
    INITIAL( 0, "Initial (typed by system)" ),
    USER_MSG( 1, "User Message (typed by user)"),
    BOT_MSG( 10, "Bot Message (typed by bot)"),
    STILL_THERE( 11, "Are you still there message (typed by system)" );

    private final int chatMessageTypeId;

    private final String name;

    private ChatMessageType( int c , String n )
    {
        this.chatMessageTypeId = c;

        this.name = n;
    }


    public int getChatMessageTypeId()
    {
        return this.chatMessageTypeId;
    }


    public String getName()
    {
        return name;
    }




    public static ChatMessageType getValue( int id )
    {
        ChatMessageType[] vals = ChatMessageType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getChatMessageTypeId() == id )
                return vals[i];
        }

        return INITIAL;
    }
    
    
}
