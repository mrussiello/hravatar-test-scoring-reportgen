/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.ivr;

/**
 *
 * @author miker_000
 */
public class IvrScoreException extends Exception {
    
    /**
     * 0 = Non-Fatal.
     * 1 = Fatal (bubble it)
     */
    private int typeId = 0;
    
    public IvrScoreException( int type, String msg )
    {
        super(msg);
        typeId = type;
    }

    @Override
    public String toString() {
        return "IvrScoreException{" + "typeId=" + typeId + ", " + super.getMessage() + '}';
    }
    
    public boolean isFatal()
    {
        return typeId == 1;
    }

    public int getTypeId() {
        return typeId;
    }
    
    
    
}
