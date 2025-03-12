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
public enum Ct5ItemPartType {

    QUESTION(0,"Question"),
    CHOICE(1,"Choice"),
    INFO(2,"Info"),
    OPTIONAL_COMMENT(3,"Optional Comment"),
    WIDGET(10,"Widget"),
    SUBMIT(11,"Submit");

    private final int ct5ItemPartTypeId;

    private final String key;


    private Ct5ItemPartType( int s , String n )
    {
        this.ct5ItemPartTypeId = s;

        this.key = n;
    }

    public boolean getIsQuestion()
    {
        return equals( QUESTION );
    }

    public boolean getIsChoice()
    {
        return equals( CHOICE );
    }
    
    public boolean getIsInfo()
    {
        return equals( INFO );
    }
    
    
    
    public static Ct5ItemPartType getValue( int id )
    {
        Ct5ItemPartType[] vals = Ct5ItemPartType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCt5ItemPartTypeId() == id )
                return vals[i];
        }

        return QUESTION;
    }

    
    public String getName()
    {
        return key;
    }
    

    public int getCt5ItemPartTypeId()
    {
        return ct5ItemPartTypeId;
    }
    
}
