package com.tm2score.act;

import com.tm2score.service.LogService;
import com.tm2score.sim.NonCompetencyItemType;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.util.UrlEncodingUtils;
import java.util.ArrayList;
import java.util.List;

public enum G2ChoiceFormatType
{
    BUTTON(0,"button" ),
    TEXT(1,"text"  ),
    HOTSPOT(2,"hotspot" ),
    TEXT_STATE_VAR(3,"text_statevarvalue" ),
    AUDIO_CHNL(4,"audiochannel" ),
    PERCENT_COMPLETE_BAR(5,"percentcompletebar" ),
    LIST_RQD_CLIPS(6,"comborequiredclips" ),
    TIMER(7,"timer" ),
    DRAG_TARGET(8,"dragtgt" ),
    CLIP_GROUP_STATUS(9,"clipgroupstatus" ),
    PREV_ENTERED_TEXT(10,"preventeredtxt" ),
    TEXT_BOX(100,"textbox" ),
    VIDEO_BUTTON(11,"videobutton" ),
    COMBO(101,"combo" ),
    CHECK_BOX(102,"checkbox" ),
    SUBMIT(103,"submit" ),
    RADIO(104,"radio" ),
    HIGHLIGHTRADIO(105,"radio2" ),
    MOSTLEASTRADIO(106,"radiomostleast" ),
    HOTSPOT_CHECK_BOX(107,"hotspotcheckbox" ),
    HLRADIO_CHECK_BOX(108,"hlradiocheckbox" ),
    SLIDER_THUMB(109,"sliderthumb" ),
    ACTIVESWF(110,"activeswf" ),
    FILEUPLOADBTN(111,"fileuploadbutton" ),
    HLRADIO_HOTSPOT(112,"radio2_hotspot" ),
    PIN_IMAGE( 113, "pin_image" ),
    MEDIA_CAPTURE( 114, "audio_video_capture" ),
    INTN_CLK_STRM( 115, "interaction_click_strm" ),
    IFRAME( 116, "iframe" );


    private final int g2ChoiceFormatTypeId;

    private final String key;


    private G2ChoiceFormatType( int p , String key )
    {
        this.g2ChoiceFormatTypeId = p;

        this.key = key;
    }
    
    public boolean getIsTextOrButton()
    {
        return this.equals(BUTTON) || this.equals(TEXT);
    }

    public boolean getIsAnyRadio()
    {
        return this.equals(RADIO) || this.equals(HIGHLIGHTRADIO) || this.equals(MOSTLEASTRADIO) || equals(HLRADIO_HOTSPOT);
    }

    public String getValueForReport( SimJ.Intn intnObj,
                                     SimJ.Intn.Intnitem intnItemObj,
                                     String respValue,
                                     boolean wasSelected,
                                     String question )
    {

        String content = UrlEncodingUtils.decodeKeepPlus( intnItemObj.getContent() );
        String title = UrlEncodingUtils.decodeKeepPlus( intnItemObj.getTitle() );

        // for a drag target of any kind, the respValue is the seq ids of the
        // itemresponses dragged on top of this target, with comma delim
        if( equals( DRAG_TARGET ) || intnItemObj.getDrgtgt()==1 )
        {
            if( respValue == null || respValue.isEmpty() )
                return null;

            String[] sqs = respValue.split( "," );

            List<SimJ.Intn.Intnitem> tenants = new ArrayList<>();

            int seq;

            for( String s : sqs )
            {
                try
                {
                    seq = Integer.parseInt( s );

                    for( SimJ.Intn.Intnitem iit : intnObj.getIntnitem() )
                    {
                        if( iit.getSeq() == seq )
                            tenants.add( iit );

                    }
                }

                catch( NumberFormatException e )
                {
                    LogService.logIt( e, "G2ChoiceFormatType.getValueForReport() processing drag target seqs: " + respValue );
                }
            }

            StringBuilder sb = new StringBuilder();

            String t;

            for( SimJ.Intn.Intnitem iit : tenants )
            {
                content = UrlEncodingUtils.decodeKeepPlus( iit.getContent() );
                title = UrlEncodingUtils.decodeKeepPlus( iit.getTitle() );

                t = title == null || title.isEmpty() ? content : title;

                if( t != null && !t.isEmpty() )
                {
                    if( sb.length()> 0 )
                        sb.append( ", " );

                    sb.append( t );
                }
            }
        }

        if( equals( SUBMIT ) )
            return null;

        // clickable buttons
        if( equals( BUTTON )|| equals( VIDEO_BUTTON ) || equals( TEXT ) || equals( HOTSPOT ) || equals( TEXT_STATE_VAR ) || equals( PREV_ENTERED_TEXT ) )
        {
            if( wasSelected )
                return title == null || title.isEmpty() ? content : title;

            return null;
        }

        // Radio buttons can go two ways, they can be correct/incorrect or they can just be selected.
        if(  equals( RADIO ) || equals( HIGHLIGHTRADIO ) || equals( HLRADIO_HOTSPOT ) )
        {
            if( wasSelected )
                return title == null || title.isEmpty() ? content : title;

            return null;
        }

        if( equals( CHECK_BOX ) || equals( HOTSPOT_CHECK_BOX ) || equals( HLRADIO_CHECK_BOX ) )
        {
            // Has a intn item competency it's a true/false. return resp value.
            if( intnItemObj.getCompetencyscoreid()>0 )
                return respValue;
            
            // if was checked
            if( respValue != null && respValue.equalsIgnoreCase( "true" ) )
                return question == null || question.isEmpty() ? "selected" : (title == null || title.isEmpty() ? content : title);

            // not checked.
            return null;
        }

        // For a combo or text box item the value is the respValue
        if( equals( COMBO ) || equals( TEXT_BOX ) || equals( SLIDER_THUMB) || equals( ACTIVESWF ) || equals( PIN_IMAGE ) || equals( INTN_CLK_STRM )  || equals( IFRAME ))
            return respValue;

        if( equals( FILEUPLOADBTN )   )
            return respValue != null && !respValue.isEmpty() ? "UPLOAD:" + respValue : null;

        if( equals( MEDIA_CAPTURE ) )
            return respValue;
        
        // anything else - no value!
        LogService.logIt( "G2ChoiceFormatType.getValueForReport() No valid code to find value for this format: " + intnItemObj.getFormat() );

        return null;
    }


    public String getFieldTitleForReport(   SimJ.Intn intnObj,
                                            SimJ.Intn.Intnitem intnItemObj,
                                            String question )
    {
        String content = UrlEncodingUtils.decodeKeepPlus( intnItemObj.getContent() );
        String title = UrlEncodingUtils.decodeKeepPlus( intnItemObj.getTitle() );

        // for a drag target of any kind, the title is the content or title field.
        if( equals( DRAG_TARGET ) || intnItemObj.getDrgtgt()==1 )
            return title==null || title.isEmpty() ? content : title;

        if( equals( CHECK_BOX ) || equals( HOTSPOT_CHECK_BOX ) || equals( HLRADIO_CHECK_BOX ) )
        {
            if( question != null && !question.isEmpty() )
            {
                if( intnItemObj.getCompetencyscoreid()>0 )
                    return question + " (" + ( content ) + ")";
                
                return null;
            }

            return title==null || title.isEmpty() ? content : title;
        }

        NonCompetencyItemType ncit = intnObj !=null && intnObj.getNoncompetencyquestiontypeid()>0 ? NonCompetencyItemType.getValue( intnObj.getNoncompetencyquestiontypeid() ) : null;

        // Writing samples always return the question
        if( ncit != null && ( ncit.equals( NonCompetencyItemType.WRITING_SAMPLE ) && equals( TEXT_BOX ) ) )
        {
            if( title==null || title.isBlank() )
                return question;

            else if( question!=null && !question.isBlank() )
                return title + " (" + question + ")";

            return title;
        } // question;

        
        // AV Upload or FileUpload samples always return the quesion
        if( ncit != null && (ncit.equals(NonCompetencyItemType.AV_UPLOAD) || ncit.equals(NonCompetencyItemType.FILEUPLOAD)) && ( equals(FILEUPLOADBTN) || equals(MEDIA_CAPTURE) ) )
        {
            if( title==null || title.isBlank() )
                return question;

            else if( question!=null && !question.isBlank() )
                return title + " (" + question + ")";

            return title;
        } // question;
        

        // For a combo or text box or file upload item
        if( equals( COMBO ) || equals( TEXT_BOX ) || equals( SLIDER_THUMB ) || equals( ACTIVESWF ) || equals( FILEUPLOADBTN ) || equals( PIN_IMAGE ) || equals( INTN_CLK_STRM ) || equals( IFRAME )  )
            return title==null || title.isBlank() ? content : title;

        return null;
    }



    public boolean responseIndicatesSelection( String s )
    {
        if( s == null || s.trim().isEmpty() )
            return false;

        s = s.trim();

        if( getIsClickable() && !getIsFormInputCollector() )
            return true;

        if( equals( COMBO ) )
            return true;

        if( equals( FILEUPLOADBTN ) )
            return true;

        if( equals( RADIO ) ||  equals( HIGHLIGHTRADIO ) || equals( HLRADIO_HOTSPOT ) || equals( CHECK_BOX ) || equals( HOTSPOT_CHECK_BOX ) || equals( HLRADIO_CHECK_BOX )  )
            return s.equalsIgnoreCase( "true" );

        if( equals( MOSTLEASTRADIO ) )
            return s.equalsIgnoreCase( "m" ) || s.equalsIgnoreCase( "l" );
        
        return false;
    }

    
    
    public boolean supportsNodeLevelSimletAutoScoring()
    {
        return getIsClickable() || equals(DRAG_TARGET);
    }

    public boolean generatesItsOwnPoints()
    {
        return equals( COMBO ) || getIsSliderThumb() || getIsIFrame();
    }
    
    public boolean supportsSubnodeLevelSimletAutoScoring()
    {
        return equals( COMBO ) || getIsTextBox() || getIsSliderThumb() || getIsActiveSwf() || getIsPinImage() || getIsIntnClickStream() || getIsIFrame() || getIsAnyCheckbox(); // || getIsFileUpload();

    }

    
    public boolean getIsMediaCapture()
    {
        return equals( MEDIA_CAPTURE );
    }

            
    public boolean getIsIFrame()
    {
        return equals( IFRAME );
    }
    
    public boolean getIsFileUpload()
    {
        return equals(FILEUPLOADBTN);
    }
    
    public boolean getIsCombo()
    {
        return equals( COMBO );
    }

    public boolean getIsSliderThumb()
    {
        return equals( SLIDER_THUMB );
    }
    
    public boolean getIsPinImage()
    {
        return equals( PIN_IMAGE );
    }

    public boolean getIsIntnClickStream()
    {
        return equals(INTN_CLK_STRM);
    }
    
    public boolean getIsActiveSwf()
    {
        return equals( ACTIVESWF );
    }


    public boolean getIsTextBox()
    {
        return equals( TEXT_BOX );
    }


    public boolean getHasUserTextInText1()
    {
        return equals(TEXT);
    }


    public boolean getIsAnyCheckbox()
    {
        return  equals( CHECK_BOX ) ||
                equals( HOTSPOT_CHECK_BOX ) ||
                equals( HLRADIO_CHECK_BOX );
    }

    public boolean getIsClickable()
    {
    	return !equals( AUDIO_CHNL ) &&
    	       !equals(PERCENT_COMPLETE_BAR) &&
    	       !equals(DRAG_TARGET) &&
               !equals( TEXT_BOX );

    	       //( !getIsFormInputCollector() || equals( CHECK_BOX ) || equals( RADIO ) ||  equals( HIGHLIGHTRADIO ) || equals( MOSTLEASTRADIO ) || equals(COMBO)   );
    }


    public boolean getIsFormInputCollector()
    {
    	return  equals( TEXT_BOX ) ||
                equals( COMBO ) ||
                equals( CHECK_BOX ) ||
                equals( HOTSPOT_CHECK_BOX ) ||
                equals( HLRADIO_CHECK_BOX ) ||
                equals( RADIO ) ||
                equals( HIGHLIGHTRADIO ) ||
                equals( HLRADIO_HOTSPOT ) ||
                equals( MOSTLEASTRADIO ) ||
                equals( FILEUPLOADBTN ) || 
                equals( INTN_CLK_STRM );
    }



    /**
     * content is text
     * intParam6 - 0=two tone, 1=shade
     */
    public boolean getIsSubmit()
    {
    	return equals( SUBMIT );
    }


    public int getG2ChoiceFormatTypeId()
    {
        return this.g2ChoiceFormatTypeId;
    }


    public String getKey()
    {
        return key;
    }

    public static G2ChoiceFormatType getValue( int id )
    {
        G2ChoiceFormatType[] vals = values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getG2ChoiceFormatTypeId() == id )
                return vals[i];
        }

        return BUTTON;
    }

}
