/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.profile.alt;


/**
 *
 * @author miker_000
 */
public abstract class BaseAltScoreCalculator {
    
    protected abstract boolean getHasWeights();
    
    public boolean hasValidScore()
    {
        return getHasWeights();
        
    }
    
}
