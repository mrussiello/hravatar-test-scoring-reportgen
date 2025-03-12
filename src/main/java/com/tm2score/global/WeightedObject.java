/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.global;

/**
 *
 * @author Mike
 */
public interface WeightedObject
{
    int getDisplayOrder();
    
    float getWeightUsed();

    void setWeightUsed( float w );

    String getName();

    String getNameEnglish();

}
