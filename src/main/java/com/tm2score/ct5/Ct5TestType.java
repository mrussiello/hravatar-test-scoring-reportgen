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
public enum Ct5TestType {

    DEFAULT(0,"c5tt.Default", "ct5"),
    VIDEO_INTERVIEW(1,"c5tt.VideoInterview", "ct5"),
    REVERSE_IMO(10,"c5tt.ReverseImo", "ct5"),
    GAME_TYPE_1(20,"c5tt.GameType1", "g1");

    private final int ct5TestTypeId;

    private final String key;
    
    private final String stub;


    private Ct5TestType( int s , String n , String stub )
    {
        this.ct5TestTypeId = s;

        this.key = n;
        this.stub=stub;
    }
    
    public boolean getIsReverse()
    {
        return equals( REVERSE_IMO );
    }
    
    public boolean getIsGameType1()
    {
        return equals( GAME_TYPE_1 );
    }
    
    public boolean getIsVideo()
    {
        return equals( VIDEO_INTERVIEW );
    }
    
    public boolean getIsDefault()
    {
        return equals( DEFAULT );
    }
        
    public static Ct5TestType getValue( int id )
    {
        Ct5TestType[] vals = Ct5TestType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCt5TestTypeId() == id )
                return vals[i];
        }

        return DEFAULT;
    }

    public String getStub() {
        return stub;
    }

    
    
    public String getName()
    {
        return key;
    }
       

    public int getCt5TestTypeId()
    {
        return ct5TestTypeId;
    }
    
}
