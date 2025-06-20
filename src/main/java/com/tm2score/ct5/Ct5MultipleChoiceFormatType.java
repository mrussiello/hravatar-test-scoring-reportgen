/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ct5;

/**
 *
 * @author miker_000
 */
public enum Ct5MultipleChoiceFormatType {

    DEFAULT_RADIO(0,"c5mcst.radio" ),   
    BUTTONS(1,"c5mcst.buttons" ),
    COMBO(2,"c5mcst.combo" ),
    CHECKBOXES(3,"c5mcst.checkboxes" ),
    HOTSPOTS(4,"c5mcst.hotspots" ),
    FILLBLANK(5,"c5mcst.fillblank" ),
    MULTI_RADIO(6,"c5mcst.multiradio" ),
    SWIPE(7,"c5mcst.swipe" );

    
    private final int ct5MultipleChoiceFormatTypeId;

    private final String key;
    


    private Ct5MultipleChoiceFormatType( int s , String n )
    {
        this.ct5MultipleChoiceFormatTypeId = s;

        this.key = n;
    }

    public boolean getIsSwipe()
    {
        return equals( SWIPE );
    }
        
    public boolean getIsHotSpots()
    {
        return equals(HOTSPOTS);
    }

    public boolean getIsRadio()
    {
        return equals( DEFAULT_RADIO );
    }

    public boolean getIsButtons()
    {
        return equals( BUTTONS );
    }
    
    public boolean getIsCheckboxes()
    {
        return equals( CHECKBOXES );
    }

    public boolean getIsFillBlank()
    {
        return equals( FILLBLANK );
    }

    public boolean getIsComboOrFillBlank()
    {
        return equals( FILLBLANK ) || equals( COMBO );
    }
    
        
    public boolean getIsCombo()
    {
        return equals( COMBO );
    }
    
    public static Ct5MultipleChoiceFormatType getValue( int id )
    {
        Ct5MultipleChoiceFormatType[] vals = Ct5MultipleChoiceFormatType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCt5MultipleChoiceFormatTypeId() == id )
                return vals[i];
        }

        return DEFAULT_RADIO;
    }

        
    
    public String getName()
    {
        return key;
    }
    
    

    public int getCt5MultipleChoiceFormatTypeId()
    {
        return ct5MultipleChoiceFormatTypeId;
    }
    
    
    
}
