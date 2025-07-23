/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.score;

import com.tm2score.service.LogService;
import com.tm2score.util.HtmlUtils;

/**
 *
 * @author Mike
 */
public class TextAndTitle implements Comparable<TextAndTitle>
{
    private String text;
    private String title;
    private int order = 0;
    private boolean redFlag = false;
    private int sequenceId=0;
    
    /**
     * For uploaded user files, this is the uploadedUserFileId
     * For audio samples (without an uploaded user files, IVR tests only) this is the AVItemResponseId
     * For ScoredEssays, this is the simCompetencyId
     * 
     */
    private long uploadedUserFileId = 0;
    private String string1 = null;
    private String string2 = null;
    private String string3 = null;
    private String string4 = null;
    
    private int displaySeq;
    

    public TextAndTitle( String text, String title)
    {
        this.text = text;
        this.title = title;
    }

    public TextAndTitle( String text, String title, long uploadedUserFileId, int sequenceId)
    {
        this.text = text;
        this.title = title;
        this.uploadedUserFileId = uploadedUserFileId;
        this.sequenceId=sequenceId;
    }

    public TextAndTitle( String text, String title, boolean rf, long uploadedUserFileId, int sequenceId, String string1, String string2)
    {
        this(  text,  title,  rf,  uploadedUserFileId,  sequenceId,  string1,  string2,  null,  null);
    }

    public TextAndTitle( String text, String title, boolean rf, long uploadedUserFileId, int sequenceId, String string1, String string2, String string3)
    {
        this(  text,  title,  rf,  uploadedUserFileId,  sequenceId,  string1,  string2,  string3,  null);
    }

    public TextAndTitle( String text, String title, boolean rf, long uploadedUserFileId, int sequenceId, String string1, String string2, String string3, String string4)
    {
        this.text = text;
        this.title = HtmlUtils.removeAllHtmlTags(title);
        this.redFlag = rf;
        this.uploadedUserFileId = uploadedUserFileId;
        this.string1=string1;
        this.string2=string2;
        this.string3=string3;
        this.string4=string4;
        this.sequenceId=sequenceId;
    }
    
    public TextAndTitle( String text, String title, int sequenceId, String string1, String string2)
    {
        this.text = text;
        this.title = HtmlUtils.removeAllHtmlTags(title);
        this.string1=string1;
        this.string2=string2;
        this.sequenceId=sequenceId;
    }
    
    public TextAndTitle( String text, String title, int sequenceId, String string1, String string2, String string3)
    {
        this.text = text;
        this.title = HtmlUtils.removeAllHtmlTags(title);
        this.string1=string1;
        this.string2=string2;
        this.string3=string3;
        this.sequenceId=sequenceId;
    }
    
    @Override
    public String toString() {
        return "TextAndTitle{" + "title=" + title + ", text.length=" + (text==null ? "null" : text.length()) + ", order=" + order + ", uploadedUserFileId=" + uploadedUserFileId + '}';
    }





    @Override
    public int compareTo(TextAndTitle o)
    {
        if( o.getOrder()!=0 && order!=0 )
            return ((Integer)( order )).compareTo( o.getOrder() );

        if( o.getSequenceId()!=0 && sequenceId!=0 )
            return ((Integer)( o.getSequenceId() )).compareTo( o.getSequenceId() );

        if( title != null && o.getTitle() != null )
            return title.compareTo( o.getTitle() );

        return 0;
    }

    public boolean isValidForReport()
    {
        if( title == null )
            title = "";

        return text != null && !text.isEmpty();
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = HtmlUtils.removeAllHtmlTags(title);
    }

    public boolean getRedFlag() {
        return redFlag;
    }

    public void setRedFlag(boolean redFlag) {
        this.redFlag = redFlag;
    }

    public String getFlags()
    {
        String f = "";

        if( redFlag )
            f += "red:1";

        if( uploadedUserFileId>0 )
        {
            if( !f.isEmpty() )
                f +=",";

            f += "uuf:" + uploadedUserFileId;
        }
        
        if( sequenceId>0 )
        {
            if( !f.isEmpty() )
                f +=",";

            f += "seq:" + sequenceId;
        }

        return f;
    }

    public void setFlags( String flags )
    {
        if( flags == null || flags.isEmpty() )
            return;

        if( flags.contains("red:1") )
            redFlag = true;

        if( flags.contains("uuf:") )
        {
            int idx = flags.indexOf(",", flags.indexOf( "uuf:" )+4 );
                     
            if( idx<0 )
                idx = flags.indexOf("seq:", flags.indexOf( "uuf:" )+4 );
            
            String t = flags.substring( flags.indexOf( "uuf:" )+4 , idx>0 ? idx : flags.length() );
            
            if( t.length()> 0 )
            {
                try
                {
                    uploadedUserFileId = Long.parseLong(t);
                }
                catch( NumberFormatException e )
                {
                    LogService.logIt(e, "TextAndTitle.setFlags() unable to parse uploadedUserFileId flags=" + flags );
                }
            }
        }    
        
        if( flags.contains("seq:") )
        {
            int idx = flags.indexOf(",", flags.indexOf( "seq:" )+4 );
                                 
            String t = flags.substring( flags.indexOf( "seq:" )+4 , idx>0 ? idx : flags.length() );
            
            if( t.length()> 0 )
            {
                try
                {
                    sequenceId = Integer.parseInt(t);
                }
                catch( NumberFormatException e )
                {
                    LogService.logIt(e, "TextAndTitle.setFlags() unable to parse uploadedUserFileId flags=" + flags );
                }
            }
        }            
    }

    public long getUploadedUserFileId() {
        return uploadedUserFileId;
    }

    public void setUploadedUserFileId(long uploadedUserFileId) {
        this.uploadedUserFileId = uploadedUserFileId;
    }

    public String getString1() {
        return string1;
    }

    public void setString1(String string1) {
        this.string1 = string1;
    }

    public String getString2() {
        return string2;
    }

    public void setString2(String string2) {
        this.string2 = string2;
    }

    public String getString3() {
        return string3;
    }

    public void setString3(String string3) {
        this.string3 = string3;
    }

    public int getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(int sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getString4()
    {
        return string4;
    }

    public void setString4(String string4)
    {
        this.string4 = string4;
    }



    
    
}
