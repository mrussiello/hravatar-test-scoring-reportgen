package com.tm2score.score;

import com.tm2score.score.iactnresp.IactnItemResp;
import com.tm2score.score.iactnresp.IactnResp;
import com.tm2score.score.iactnresp.RadioButtonGroupResp;
import com.tm2score.act.G2ChoiceFormatType;
import com.tm2builder.sim.xml.SimJ;



public enum ScoredItemParadigmType
{
    UNKNOWN(0,"Unknown"),
    MULTIPLE_CHOICE(1,"Multiple Choice"), // Ivr Type 2,4,112,122   // Iactn or RBG
    MULTIPLE_SELECT(2,"Multiple Selection"),                        // Iactn
    FILL_BLANK(3,"Fill Blank"),                                     // IactnItem
    TYPING_SPEED(4,"Typing Speed/Accuracy"),                        // Typing
    ESSAY(5,"SCORED_ESSAY"),                                        // Essay
    MATCHING(6,"Matching"),                                         // Iactn
    PIN_IMAGE(7,"Pin Image"),                                       // Pin Image
    IVR_RECORDING(8,"Ivr Voice Recording"),   // Ivr Type 1,3,5     // IVR
    AV_UPLOAD(9,"AV Upload"), // Ivr Type 101                       // IVR
    IDENTITY_IMAGE(10,"Identity Image Capture"),                    // Image Capture
    DATA_ENTRY_SPEED(11,"Data Entry Speed/Accuracy"),               // Data Entry
    CLICKSTREAM(12,"Clickstream Points"),                           // ClickStream,
    IFRAME(13,"IFRAME Points"),                                     // IFrame
    TRUE_FALSE(14,"True False");                                    // True / False 

    private final int scoredItemParadigmTypeId;

    private final String name;


    private ScoredItemParadigmType( int s , String n )
    {
        this.scoredItemParadigmTypeId = s;

        this.name = n;
    }

    public boolean isTrueFalse()
    {
        return equals( TRUE_FALSE );
    }

    public boolean isIFrame()
    {
        return equals( IFRAME );
    }

    public boolean isClickStream()
    {
        return equals( CLICKSTREAM );
    }

    public boolean isTyping()
    {
        return equals( TYPING_SPEED );
    }

    public boolean isPinImage()
    {
        return equals( PIN_IMAGE );
    }

    public static ScoredItemParadigmType getValue( IactnResp ir )
    {
        if( !ir.isAutoScorable() )
            return UNKNOWN;

        if( ir.simletCompetencyScore!=null && ir.simletCompetencyScore.getCompetencyScoreType().isScoredEssay() )
            return ESSAY;

        if( ir.simletCompetencyScore!=null && ir.simletCompetencyScore.getCompetencyScoreType().isScoredChat() )
            return IFRAME;

        if( ir.simletCompetencyScore!=null &&  ir.simletCompetencyScore.getCompetencyScoreType().isScoredVoiceSample()  )
            return IVR_RECORDING;

        if( ir.simletCompetencyScore!=null &&  ir.simletCompetencyScore.getCompetencyScoreType().isScoredAvUpload() )
            return AV_UPLOAD;

        if( ir.simletCompetencyScore!=null &&  ir.simletCompetencyScore.getCompetencyScoreType().isIdentityImageCapture())
            return IDENTITY_IMAGE;

        if( ir.simletCompetencyScore!=null && ir.simletCompetencyScore.getCompetencyScoreType().isTypingSpeedAccuracy() )
            return TYPING_SPEED;

        if( ir.simletCompetencyScore!=null && ir.simletCompetencyScore.getCompetencyScoreType().isDataEntry())
            return DATA_ENTRY_SPEED;

        //if( ir instanceof IvrIactnResp )
        //{
        //    IvrIactnResp ivrIr = (IvrIactnResp) ir;
            
        //    return ivrIr.getScoredItemParadigmType();
        //}
        
        int clkableCt = 0;
        int drgTgtCt = 0;

        for( SimJ.Intn.Intnitem ii :  ir.intnObj.getIntnitem() )
        {
            if( ii.getDrgtgt()==1 )
                drgTgtCt++;

            // skipper.
            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsAnyCheckbox() && ii.getSimcompetencyid()>0 )
                continue;
                        
            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsAnyCheckbox() )
                return MULTIPLE_SELECT;

            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsAnyRadio())
                return MULTIPLE_CHOICE;

            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsFileUpload() || G2ChoiceFormatType.getValue( ii.getFormat() ).getIsMediaCapture() )
                return MULTIPLE_CHOICE;

            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsPinImage() )
                return PIN_IMAGE;
            
            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsIntnClickStream())
                return CLICKSTREAM;

            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsIFrame())
                return IFRAME;
                        
            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsSubmit() )
                continue;

            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsFormInputCollector() )
                continue;

            if( G2ChoiceFormatType.getValue( ii.getFormat() ).getIsClickable() )
                clkableCt++;

        }

        if( clkableCt> 0 || drgTgtCt>0 )
        {
            return drgTgtCt>1 ? MATCHING : MULTIPLE_CHOICE;
        }

        return UNKNOWN;
    }

    
    public static ScoredItemParadigmType getValue( RadioButtonGroupResp rbs )
    {
        return ScoredItemParadigmType.MULTIPLE_CHOICE;
    }

    
    public static ScoredItemParadigmType getValue( IactnItemResp iir )
    {
        if( !iir.isAutoScorable() )
            return UNKNOWN;

        if( iir.isDragTarget() && iir.intnItemObj.getDrgtgtcheckbox()==1 && iir.getIntnItemObj().getSimcompetencyid()>0 )
            return TRUE_FALSE;
        
        G2ChoiceFormatType gft = G2ChoiceFormatType.getValue(iir.intnItemObj.getFormat());

        if( gft.getIsSliderThumb() || gft.getIsCombo() )
            return ScoredItemParadigmType.MULTIPLE_CHOICE;

        if( gft.getIsPinImage() )
            return PIN_IMAGE;
                    
        if( gft.getIsIntnClickStream())
            return CLICKSTREAM;
                    
        if( gft.getIsIFrame())
            return IFRAME;

        if( gft.getIsAnyCheckbox() && iir.getIntnItemObj().getSimcompetencyid()>0 )
            return TRUE_FALSE;
        
        if( gft.getIsTextBox() )
            return FILL_BLANK;

        return UNKNOWN;
    }



    public static ScoredItemParadigmType getValue( int id )
    {
        ScoredItemParadigmType[] vals = ScoredItemParadigmType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getScoredItemParadigmTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }


    public int getScoredItemParadigmTypeId()
    {
        return scoredItemParadigmTypeId;
    }

    public String getName()
    {
        return name;
    }

}
