/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.bot;

import com.tm2score.global.I18nUtils;
import com.tm2score.service.LogService;
import com.tm2score.util.MessageFactory;
import java.util.Locale;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;

/**
 *
 * @author miker_000

{ type:       0=initial, 1=user entered, 10=bot return
  content:    the message content.
  content2:    alt message content (intial only).
  content3:    alt message content (intial only).
  content4:    alt message content (intial only).
  intent:     intent name (bot return only),
  dialoguestate: dialog state (bot return only)
  points:     points assigned
  seconds     response time in seconds (user messages only)
  index:      index starting with 0
  actioncode: actionCodeTypeId
  botinstanceid: botInstanceId that generated this message (bot return only).
  nextbotinstanceid: nextBotInstanceId (if advance to another bot)
  int1: int1
  int2: int2
  int3: int3
  str1: str1
  str2: str2
  str3: str3
}
* 
* 
*/

public class ChatMessage 
{
    int type;
    String content;
    String content2;
    String content3;
    String content4;
    String intent;
    float points;
    float seconds;
    String competency;
    int actionCode;
    int botInstanceId;
    int nextBotInstanceId;
    int index;
    String dialogState;


    /**
     * 0 = not missed.
     * 1 = missed.
     */
    int int1;
    int int2;
    int int3;
    
    String str1;
    String str2;
    String str3;
        
    // Not stored
    

    public String getMessageAsTextStr( boolean includeSecs, Locale locale )
    {
        // First, escape all | chars to a -
        if( content==null )
            return "";
        
        if( locale==null )
            locale = Locale.US;
        
        String c = content.replaceAll("\\|", "-" );
        
        String m = type + "|" + c;
        
        if( type == ChatMessageType.USER_MSG.getChatMessageTypeId() && includeSecs )
            m += " " + MessageFactory.getStringMessage(locale, "g.ChatRTTextSecs" , new String[] {I18nUtils.getFormattedNumber(locale, seconds, 1)} );
        
        return m;
    }
    
        
    public boolean isMissedMessage()
    {
        return intent==null || intent.trim().isEmpty() || int1==1;
    }
    
    
    public void readFromJsonObject( JsonObject jo ) throws Exception
    {
        try
        {
            
            if( jo.containsKey("type") )
                type = jo.getInt("type");
            
            if( jo.containsKey("index") )
                index = jo.getInt("index");
            
            if( jo.containsKey("content") )
                content = jo.getString("content" , null );
            
            if( jo.containsKey("content2") )
                content2 = jo.getString("content2" , null );
            
            if( jo.containsKey("content3") )
                content3 = jo.getString("content3" , null );
            
            if( jo.containsKey("content4") )
                content4 = jo.getString("content4" , null );

            if( jo.containsKey("intent") )
                intent = jo.getString("intent" , null );

            if( jo.containsKey("dialogstate") )
                dialogState = jo.getString("dialogstate" , null );

            JsonNumber jn;
            
            if( jo.containsKey("points") )
            {
                jn = jo.getJsonNumber("points");                
                points = (float) jn.doubleValue();
            }

            if( jo.containsKey("seconds") )
            {
                jn = jo.getJsonNumber("seconds");                
                seconds = (float) jn.doubleValue();
            }
            
            if( jo.containsKey("competency") )
                competency = jo.getString("competency" , null );
            
            if( jo.containsKey("botinstanceid") )
                botInstanceId = jo.getInt("botinstanceid");
            
            if( jo.containsKey("actioncode") )
                actionCode = jo.getInt("actioncode");
            
            if( jo.containsKey("nextbotinstanceid") )
                nextBotInstanceId = jo.getInt("nextbotinstanceid");
            
            if( jo.containsKey("int1") )
                int1 = jo.getInt("int1");
            
            if( jo.containsKey("int2") )
                int2 = jo.getInt("int2");
            
            if( jo.containsKey("int3") )
                int3 = jo.getInt("int3");
            
            if( jo.containsKey("str1") )
                str1 = jo.getString("str1" , null );
            
            if( jo.containsKey("str2") )
                str2 = jo.getString("str2" , null );
            
            if( jo.containsKey("str3") )
                str3 = jo.getString("str3" , null );
            
        }
        catch(Exception e )
        {
            LogService.logIt(e, "ChatEntry.readFromJsonObject() " + toString() );
            throw e; //  new Exception( e.getMessage(), BotErrorCodeType.JSON_PARSE_ERROR.getBotErrorCodeTypeId(), null );            
        }
    }
    
    
    public boolean getIsCompetencyMatch( String c )
    {
        // LogService.logIt( "ChatMessage.getIsCompetencyMatch() competency=" + competency + ", c=" + c + ", " + toString() );
        if( c==null || c.isEmpty() )
            return false;
        
        if( competency==null )
            return false;
        
        return competency.trim().equalsIgnoreCase( c.trim() );
    }
    

    @Override
    public String toString() {
        return "ChatEntry{" + "type=" + type + ", index=" + index + ", competency=" + competency + ", content=" + content + ", intent=" + intent + ", points=" + points + ", actionCode=" + actionCode + ", nextBotInstanceId=" + nextBotInstanceId + '}';
    }
    
    
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public float getPoints() {
        return points;
    }

    public void setPoints(float points) {
        this.points = points;
    }

    public int getActionCode() {
        return actionCode;
    }

    public void setActionCode(int actionCode) {
        this.actionCode = actionCode;
    }

    public int getNextBotInstanceId() {
        return nextBotInstanceId;
    }

    public void setNextBotInstanceId(int nextBotInstanceId) {
        this.nextBotInstanceId = nextBotInstanceId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getBotInstanceId() {
        return botInstanceId;
    }

    public void setBotInstanceId(int botInstanceId) {
        this.botInstanceId = botInstanceId;
    }

    public String getContent2() {
        return content2;
    }

    public void setContent2(String content2) {
        this.content2 = content2;
    }

    public String getContent3() {
        return content3;
    }

    public void setContent3(String content3) {
        this.content3 = content3;
    }

    public String getContent4() {
        return content4;
    }

    public void setContent4(String content4) {
        this.content4 = content4;
    }

    public String getDialogState() {
        return dialogState;
    }

    public void setDialogState(String dialogState) {
        this.dialogState = dialogState;
    }

    public String getCompetency() {
        return competency;
    }

    public void setCompetency(String competency) {
        this.competency = competency;
    }

    public int getInt1() {
        return int1;
    }

    public void setInt1(int int1) {
        this.int1 = int1;
    }

    public int getInt2() {
        return int2;
    }

    public void setInt2(int int2) {
        this.int2 = int2;
    }

    public int getInt3() {
        return int3;
    }

    public void setInt3(int int3) {
        this.int3 = int3;
    }

    public String getStr1() {
        return str1==null ? "" : str1;
    }

    public void setStr1(String str1) {
        this.str1 = str1;
    }

    public String getStr2() {
        return  str2==null ? "" : str2;
    }

    public void setStr2(String str2) {
        this.str2 = str2;
    }

    public String getStr3() {
        return  str3==null ? "" : str3;
    }

    public void setStr3(String str3) {
        this.str3 = str3;
    }

    public float getSeconds() {
        return seconds;
    }

    public void setSeconds(float seconds) {
        this.seconds = seconds;
    }
    
    
}
