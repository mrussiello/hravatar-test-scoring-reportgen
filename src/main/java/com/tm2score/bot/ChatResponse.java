/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.bot;

import com.tm2score.service.LogService;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

/**
 *
 * @author miker_000
 */
public class ChatResponse {
    
    List<ChatMessage> chatMessageList;

    boolean initComplete = false;
    
    
    public int getNextIndex()
    {
        if( chatMessageList==null )
            chatMessageList = new ArrayList<>();

        return chatMessageList.size();
    }
    
    
    public int getCurrentBotInstanceId( int initialBotInstanceId )
    {
        if( chatMessageList==null || chatMessageList.isEmpty() )
            return initialBotInstanceId;
        
        int iid = initialBotInstanceId;

        for( ChatMessage cm : chatMessageList )
        {
            if( cm.getType()==ChatMessageType.BOT_MSG.getChatMessageTypeId() )
            {
                if( cm.getNextBotInstanceId()>0 )
                    iid = cm.getNextBotInstanceId();
                else if( cm.getBotInstanceId()>0 )
                    iid=cm.getBotInstanceId();
            }                
        }
        
        return iid;
    }
    
    
    public boolean getIsComplete()
    {
        if( chatMessageList==null )
            return false;
        
        for( ChatMessage cm : chatMessageList )
        {
            if( cm.getType()==ChatMessageType.BOT_MSG.getChatMessageTypeId() && cm.getActionCode()==ActionCodeType.END_SESSION.getActionCodeTypeId() )
                return true;
        }
        
        
        return false;
    }
    
    public ChatMessage getChatMessageForIndex( int index )
    {
        if( index<0 || chatMessageList==null || chatMessageList.size()<=index )
            return null;
        
        return chatMessageList.get(index);
    }
    

    public int getHitBotMessageCount()
    {
        return countBotMessages( false, true );
    }
    
    public int getMissedBotMessageCount()
    {
        return countBotMessages( true, false );
    }
    
    public int getTotalBotMessageCount()
    {
        return countBotMessages( true, true );
    }

    private int countBotMessages( boolean missedOk, boolean notMissedOk )
    {
        if( chatMessageList==null )
            return 0;
        
        int count = 0;
        
        for( ChatMessage ce : chatMessageList )
        {
            if( ce.getType()==ChatMessageType.BOT_MSG.getChatMessageTypeId() )
            {
                if( ce.getIntent()!=null && !ce.getIntent().isEmpty() && notMissedOk )
                    count++;

                else if( (ce.getIntent()==null || ce.getIntent().isEmpty()) && missedOk )
                    count++;
            }
        }
        
        return count;
    }
    

    
    public int getUserMessageCount()
    {
        int count = 0;
        
        if( chatMessageList==null )
            return 0;
        
        for( ChatMessage ce : chatMessageList )
        {
            if( ce.getType()==ChatMessageType.USER_MSG.getChatMessageTypeId() )
                count++;
        }
        
        return count;        
    }

    public float getAvgUserRespTime()
    {
        float total = 0;
        float count = 0;
        
        if( chatMessageList==null )
            return 0f;
        
        for( ChatMessage ce : chatMessageList )
        {
            if( ce.getType()==ChatMessageType.USER_MSG.getChatMessageTypeId() )
            {
                total+= ce.getSeconds();
                count++;
            }
        }
        
        return count>0 ? total/count : 0f;
    }
    
    
    public List<String> getAllUserResponses()
    {
        List<String> out = new ArrayList<>();
        
        if( chatMessageList==null )
            return out;
        
        for( ChatMessage ce : chatMessageList )
        {
            if( ce.getType()==ChatMessageType.USER_MSG.getChatMessageTypeId() )
            {
                out.add( ce.getContent() );
            }
        }
        
        return out;
    }
    
        
    public float getPointsForCompetency( String c )
    {
        if( c==null || c.isEmpty() )
            return 0;
        
        if( this.chatMessageList==null )
            return 0;
        
        float points = 0;
        
        for( ChatMessage cm : this.chatMessageList )
        {
            if( !cm.getIsCompetencyMatch( c ) )
                continue;
            
            points += cm.getPoints();
        }
        
        return points;
    }
    
    
    
    public void readFromJsonStr( String jsonStr ) throws Exception
    {
        try
        {
            chatMessageList = new ArrayList<>();
            
             JsonReader jr = Json.createReader(new StringReader(jsonStr));
             
             JsonObject top = jr.readObject();
             
             if( !top.containsKey("chatentries") )
                 return;
             
             JsonArray ja = top.getJsonArray("chatentries" );
             
             JsonObject jce;
             ChatMessage ce;
             
             for( JsonValue jv : ja )
             {
                 jce = (JsonObject) jv;
                 ce = new ChatMessage();                 
                 ce.readFromJsonObject(jce);
                 chatMessageList.add( ce );
             }     
        }
        catch( Exception e )
        {
            LogService.logIt(e, "ChatResponse.readFromJsonStr() " + toString() );
            throw e; //  new BotException( e.getMessage(), BotErrorCodeType.JSON_PARSE_ERROR.getBotErrorCodeTypeId(), null );
        }        
    }

    public List<ChatMessage> getChatMessageList() 
      {
       
        if( chatMessageList == null )
            chatMessageList = new ArrayList<>();

        return chatMessageList;
    }

    public String getMessagesAsTextStr( boolean includeSecs, Locale locale )
    {
        StringBuilder sb = new StringBuilder();
        
        for( ChatMessage cm : getChatMessageList()  )
        {
            if( sb.length()>0 )
                sb.append( "|" );
            
            sb.append( cm.getMessageAsTextStr( includeSecs, locale ) );
        }
        
        return sb.toString();
    }
    
    public boolean isInitComplete() {
        return initComplete;
    }
    
}
